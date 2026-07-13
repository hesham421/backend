package com.example.erp.notification.repository;

import com.example.erp.notification.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository
    extends JpaRepository<NotificationTemplate, Long>,
            JpaSpecificationExecutor<NotificationTemplate> {

    // QR-NOTIF-002 — FIND NotificationTemplate by templateCode (active only). Used by
    // NotificationEventProcessor to resolve the template for a fan-out channel (RULE-NOTIF-006).
    Optional<NotificationTemplate> findByTemplateCodeAndIsActiveFlTrue(String templateCode);

    // RULE-NOTIF-007 — uniqueness pre-check on Create. No existsByTemplateCodeAndIdNot():
    // templateCode is immutable post-create and excluded entirely from UpdateRequest's body
    // (create-repository A.2.5 — AndIdNot only for fields mutable on update).
    boolean existsByTemplateCode(String templateCode);
}
