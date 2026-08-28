package com.erp.notification.channel;

import com.erp.notification.entity.NotificationLog;
import lombok.extern.slf4j.Slf4j;

/**
 * Phase-1 placeholder, kept as the documented fallback now that {@link EmailChannelSender} is
 * the sole registered {@link ChannelSender} bean. Left {@code @Component}-free since only one
 * implementation may be registered at a time — re-annotate this one if EMAIL is ever disabled.
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
