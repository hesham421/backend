package com.example.erp.notification.channel;

import com.example.erp.notification.entity.NotificationLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Phase-1 placeholder registered for ALL channel types (EMAIL/SMS/WHATSAPP/PUSH/INTERNAL).
 *
 * <p>CORE.md names the real per-channel adapters (Email via Apache Camel route, Push via
 * Firebase Admin SDK, SMS/WhatsApp via a pluggable adapter) but explicitly marks their provider
 * as "TBD — AQ-010/AQ-011, does not affect schema or this plan's contracts". Standing up an
 * actual message broker/Camel route/Firebase project/SMS-WhatsApp provider account is
 * infrastructure work with no concrete configuration anywhere in this plan to build against —
 * inventing one would be indistinguishable from fabricating undocumented behavior. This stub
 * keeps RULE-NOTIF-003/004/005's log-status bookkeeping and {@code NotificationDispatchService}'s
 * retry/backoff orchestration fully real and independently testable, while leaving the actual
 * provider call as a documented, deliberate placeholder — always reports success.
 */
@Slf4j
@Component
public class StubChannelSender implements ChannelSender {

    @Override
    public boolean send(NotificationLog logEntry) {
        log.debug("STUB channel send — NOTIF_LOG id={}, channel={}, recipient={} (AQ-010/AQ-011 provider pending)",
                logEntry.getId(), logEntry.getNotificationTypeId(), logEntry.getRecipientId());
        return true;
    }
}
