package com.erp.notification.channel;

import com.erp.notification.entity.NotificationLog;
import lombok.extern.slf4j.Slf4j;

/**
 * Phase-1 placeholder, formerly registered for ALL channel types (EMAIL/SMS/WHATSAPP/PUSH/
 * INTERNAL). AQ-010/AQ-011 are now RESOLVED — EMAIL is enabled and dispatched by
 * {@link EmailChannelSender}, the sole registered {@link ChannelSender} bean; SMS/WhatsApp/Push
 * remain disabled ({@code NOTIF_CHANNEL_CONFIG.is_enabled_fl = 0}) and never reach {@code
 * send()} at all — {@code NotificationEventProcessor} marks those rows {@code CHANNEL_DISABLED}
 * at persist time, before dispatch.
 *
 * <p>Deliberately left {@code @Component}-free: {@code NotificationDispatchService} autowires a
 * single {@code ChannelSender} by type with no {@code @Qualifier}, so only one implementation
 * may be a registered bean at a time. This class stays in the codebase as the documented
 * fallback/reference implementation — re-annotate it with {@code @Component} (and remove
 * {@code EmailChannelSender}'s) if EMAIL is ever disabled again and no real adapter is needed.
 */
@Slf4j
public class StubChannelSender implements ChannelSender {

    @Override
    public boolean send(NotificationLog logEntry) {
        log.debug("STUB channel send — NOTIF_LOG id={}, channel={}, recipient={} (AQ-010/AQ-011 provider pending)",
                logEntry.getId(), logEntry.getNotificationTypeId(), logEntry.getRecipientId());
        return true;
    }
}
