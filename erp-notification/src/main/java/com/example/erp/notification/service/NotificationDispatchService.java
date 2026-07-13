package com.example.erp.notification.service;

import com.example.erp.notification.channel.ChannelSender;
import com.example.erp.notification.entity.NotificationLog;
import com.example.erp.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.example.erp.notification.config.NotificationAsyncConfig.DISPATCH_EXECUTOR;

/**
 * Post-persist dispatch orchestration (CORE.md): "for each PENDING row, attempt channel send;
 * on failure, incrementRetry() up to 5 (RULE-NOTIF-004), then markFailed(); on success,
 * markSent()." Runs off the request thread ({@link Async}) so RULE-NOTIF-004's exponential
 * backoff never blocks a caller of API-NOTIF-001/002.
 *
 * <p>A distinct {@code @Service} bean (not a method on {@link NotificationEventProcessor}) —
 * {@code @Async} only takes effect through Spring's proxy on a call from a DIFFERENT bean;
 * self-invocation from within the same class would silently run synchronously.
 *
 * <p><b>Deliberate A.5.2 exception:</b> {@link #dispatchAsync} carries no {@code @PreAuthorize}.
 * It is not a new authorization boundary — it is a continuation of work already authorized at
 * the REST/Event ingress ({@code NotificationEventProcessor.send()}/{@code schedule()}'s own
 * {@code isAuthenticated()} gate, or the Spring Event listener's same-process trust), reached
 * only via {@code NotificationDispatchTrigger}'s post-commit event, never directly by a
 * controller or another module. It also could not practically carry one: Spring Security's
 * context does not propagate to the {@code @Async} executor thread in this codebase (no
 * {@code DelegatingSecurityContextExecutor} configured), so evaluating any SpEL authorization
 * expression here would fail closed on every dispatch regardless of the original caller.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationDispatchService {

    /** RULE-NOTIF-004 — exponential backoff between attempts, ms. 5 attempts total (4 waits). */
    private static final long[] BACKOFF_MILLIS = {2000L, 3000L, 4500L, 6750L};

    private final NotificationLogRepository logRepository;
    private final ChannelSender channelSender;

    @Async(DISPATCH_EXECUTOR)
    public void dispatchAsync(Long notificationLogId) {
        NotificationLog logEntry = logRepository.findById(notificationLogId).orElse(null);
        if (logEntry == null || !NotificationLog.STATUS_PENDING.equals(logEntry.getNotificationStatusId())) {
            // Nothing to dispatch — e.g. RULE-NOTIF-005 already resolved this row to
            // CHANNEL_DISABLED at persist time, or the row was removed/changed concurrently.
            return;
        }
        dispatchWithRetry(logEntry);
    }

    /**
     * No method-level {@code @Transactional} here on purpose: {@link NotificationLogRepository
     * #save} is itself transactional (Spring Data's {@code SimpleJpaRepository}), so each
     * attempt's outcome commits independently — a DB connection is never held open across the
     * multi-second backoff sleeps between attempts.
     */
    private void dispatchWithRetry(NotificationLog logEntry) {
        while (true) {
            boolean sent = channelSender.send(logEntry);
            int attemptsMadeBefore = logEntry.getRetryCount();
            boolean terminal = sent || attemptsMadeBefore + 1 >= NotificationLog.MAX_RETRY_COUNT;

            recordAttemptOutcome(logEntry, sent, terminal);

            if (terminal) {
                log.info("Dispatched NOTIF_LOG id={} channel={} — {}",
                        logEntry.getId(), logEntry.getNotificationTypeId(), logEntry.getNotificationStatusId());
                return;
            }
            sleepBeforeRetry(attemptsMadeBefore);
        }
    }

    private void recordAttemptOutcome(NotificationLog logEntry, boolean sent, boolean terminal) {
        if (sent) {
            logEntry.markSent();
        } else {
            logEntry.incrementRetry();
            if (terminal) {
                logEntry.markFailed();
            }
        }
        logRepository.save(logEntry);
    }

    private void sleepBeforeRetry(int attemptIndex) {
        long delay = BACKOFF_MILLIS[Math.min(attemptIndex, BACKOFF_MILLIS.length - 1)];
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
