package com.example.erp.notification.repository;

import com.example.erp.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * QR-NOTIF-003 (SAVE NotificationLog) — save() is inherited from {@link JpaRepository}, invoked
 * once per fan-out channel by {@code NotificationEventProcessor} (RULE-NOTIF-003).
 */
@Repository
public interface NotificationLogRepository
    extends JpaRepository<NotificationLog, Long>,
            JpaSpecificationExecutor<NotificationLog> {
}
