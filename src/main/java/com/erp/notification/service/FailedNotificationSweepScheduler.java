package com.erp.notification.service;

import com.erp.common.security.InternalCaller;
import com.erp.notification.channel.ChannelSender;
import com.erp.notification.entity.NotificationLog;
import com.erp.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Durable-retry sweep for failed sends — reuses {@code NOTIF_LOG} directly rather than a generic outbox, since
 * EmailChannelSender's Gmail SMTP call is the only call site with a real network failure mode; {@code
 * sweepRetryCount} is a second counter, independent of RULE-NOTIF-004's fast retryCount.
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
        // Scheduled tasks run on a background thread with no SecurityContext; channelSender.send()
        // reaches into SecurityUserApi -> UserService.searchUsers(), which is @PreAuthorize-gated.
        boolean sent = InternalCaller.call(() -> channelSender.send(logEntry));
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
