package com.erp.notif.service;

import com.erp.notif.entity.NotificationTemplate;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Default, provider-agnostic {@link ChannelProvider} — logs the send and reports success, so the
 * dispatch happy path yields SENT and the retry/FAILED wiring stays exercisable.
 *
 * <p>TODO: concrete ChannelProvider per channel is a later implementation decision (OQ-NOTIF-001) —
 * resolve the real SMTP/SMS/WhatsApp/Push provider from the channel's configJson when chosen.
 */
@Component
@Slf4j
public class LoggingChannelProvider implements ChannelProvider {

    @Override
    public ChannelSendResult send(String channelTypeId, Long recipientId, NotificationTemplate template,
                                  String configJson, Map<String, String> variables) {
        log.info("Dispatching notification via channel {} to recipient {} using template {}",
            channelTypeId, recipientId, template != null ? template.getTemplateCode() : null);
        return ChannelSendResult.ok();
    }
}
