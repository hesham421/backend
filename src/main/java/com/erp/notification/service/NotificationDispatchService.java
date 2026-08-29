package com.erp.notification.service;

import com.erp.common.security.InternalCaller;
import com.erp.notification.channel.ChannelSender;
import com.erp.notification.entity.NotificationLog;
import com.erp.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.erp.notification.config.NotificationAsyncConfig.DISPATCH_EXECUTOR;

/**
 * Post-persist dispatch, run off the request thread via {@code @Async} — a distinct bean since
 * {@code @Async} only works via Spring's proxy on cross-bean calls. {@link #dispatchAsync}
 * deliberately carries no {@code @PreAuthorize}: it continues work already authorized at ingress.
 *
 * <p>"Ingress" context is only ever propagated, never guaranteed present: {@code
 * NotificationAsyncConfig}'s task decorator carries over whatever {@code SecurityContext} existed
 * when the notification was queued, but public/unauthenticated endpoints (forgot-password, signup
 * activation) queue with an empty context. {@link ChannelSender#send} reaches into {@code
 * SecurityUserApi -> UserService.searchUsers()}, which is {@code @PreAuthorize}-gated — with no
 * real principal and no fallback, that lookup was silently denied and logged as "no email on
 * file" for every notification triggered by an unauthenticated flow. Same fix as {@link
 * FailedNotificationSweepScheduler#retryOne}: wrap the dispatch in {@link InternalCaller}.
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
        InternalCaller.run(() -> dispatchWithRetry(logEntry));
    }

    /**
     * No method-level {@code @Transactional} here — {@code NotificationLogRepository#save} is
     * itself transactional, so each attempt commits independently without holding a connection
     * across the backoff sleeps.
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
