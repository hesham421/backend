package com.erp.notification.repository;

import com.erp.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * QR-NOTIF-003 (SAVE NotificationLog) — save() is inherited from {@link JpaRepository}, invoked
 * once per fan-out channel by {@code NotificationEventProcessor} (RULE-NOTIF-003).
 */
@Repository
public interface NotificationLogRepository
    extends JpaRepository<NotificationLog, Long>,
            JpaSpecificationExecutor<NotificationLog> {

    /**
     * Post-implementation-audit remediation, Item 2 — candidates for
     * {@code FailedNotificationSweepScheduler}: terminally FAILED rows that haven't yet exhausted
     * the separate sweep-retry ceiling ({@link NotificationLog#MAX_SWEEP_RETRY_COUNT}).
     */
    List<NotificationLog> findByNotificationStatusIdAndSweepRetryCountLessThan(String notificationStatusId, short sweepRetryCountCeiling);
}
