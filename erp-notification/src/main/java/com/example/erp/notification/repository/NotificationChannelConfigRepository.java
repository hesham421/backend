package com.example.erp.notification.repository;

import com.example.erp.notification.entity.NotificationChannelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationChannelConfigRepository
    extends JpaRepository<NotificationChannelConfig, Long>,
            JpaSpecificationExecutor<NotificationChannelConfig> {

    // QR-NOTIF-001 — FIND NotificationChannelConfig by channelTypeId. Used by the fan-out
    // processor (RULE-NOTIF-003/005 — enabled-status check per requested channel).
    Optional<NotificationChannelConfig> findByChannelTypeId(String channelTypeId);

    // Used to expand the channelHint="ALL" sentinel (RULE-NOTIF-002) into the concrete set of
    // channel codes without hardcoding them in the processor.
    List<NotificationChannelConfig> findAllByOrderByChannelTypeIdAsc();
}
