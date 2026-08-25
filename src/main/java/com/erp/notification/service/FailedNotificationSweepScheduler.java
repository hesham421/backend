package com.erp.notification.service;

import com.erp.notification.channel.ChannelSender;
import com.erp.notification.entity.NotificationLog;
import com.erp.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Post-implementation-audit remediation, Item 2 — the durable-retry half of the mechanism the
 * approved recommendation
 * ({@code governance/project-artifacts/INTERFACE-VS-REST-AND-POM-STRUCTURE-RECOMMENDATION.md})
 * said must be retained, which the audit found absent from the delivered code entirely (not
 * merely orphaned — never carried over).
 *
 * <p><b>Why this shape, not a resurrected generic {@code FailedCallRecordBase}/outbox:</b> the
 * audit's premise was a GENERIC, reusable, cross-module outbox. Checked directly: the only
 * current call site with a genuinely network-shaped failure mode is
 * {@link com.erp.notification.channel.EmailChannelSender}'s real Gmail SMTP send — the 5
 * interface-injection call sites this recommendation was originally about no longer have one (a
 * same-JVM method call doesn't fail over the network). Building a generic, multi-module,
 * polymorphic retry-registry abstraction today would have exactly one consumer and would
 * duplicate state {@link NotificationLog} already tracks (status, retry count) in a second,
 * parallel table — worse, not better, than reusing what's there. This sweep instead reuses
 * {@code NOTIF_LOG} directly: {@code sweepRetryCount} (V15 migration) is a second, independent
 * counter from RULE-NOTIF-004's fast in-process {@code retryCount}, so this slower, later sweep
 * doesn't disturb that existing ceiling. If a second genuinely network-shaped call site appears
 * later, extracting a shared base at that point (two real consumers, not a hypothetical one) is
 * the point at which a generic mechanism stops being speculative.
 *
 * <p>Reuses the same {@link ChannelSender} bean {@link NotificationDispatchService} uses — see
 * that interface's only current implementation, {@code EmailChannelSender}'s javadoc, for why a
 * single unqualified bean is this codebase's deliberate simplification (only one channel is
 * currently enabled).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FailedNotificationSweepScheduler {

    private final NotificationLogRepository logRepository;
    private final ChannelSender channelSender;

    /**
     * Every 15 minutes — deliberately much slower than RULE-NOTIF-004's ~2-7s in-process backoff
     * steps, since this exists specifically for outages that outlast that window.
     */
    @Scheduled(fixedDelay = 15 * 60 * 1000L)
    public void sweep() {
        List<NotificationLog> candidates = logRepository.findByNotificationStatusIdAndSweepRetryCountLessThan(
                NotificationLog.STATUS_FAILED, NotificationLog.MAX_SWEEP_RETRY_COUNT);
        if (candidates.isEmpty()) {
            return;
        }
        log.info("Failed-notification sweep — {} candidate(s)", candidates.size());
        for (NotificationLog logEntry : candidates) {
            retryOne(logEntry);
        }
    }

    private void retryOne(NotificationLog logEntry) {
        boolean sent = channelSender.send(logEntry);
        if (sent) {
            logEntry.markSent();
            log.info("Failed-notification sweep recovered NOTIF_LOG id={}", logEntry.getId());
        } else {
            logEntry.incrementSweepRetry();
            log.warn("Failed-notification sweep re-attempt failed for NOTIF_LOG id={} (sweepRetryCount={})",
                    logEntry.getId(), logEntry.getSweepRetryCount());
        }
        logRepository.save(logEntry);
    }
}
