package com.erp.notif.service;

import com.erp.notif.entity.NotificationTemplate;
import java.util.Map;

/**
 * DRV-004 (OQ-NOTIF-001) — provider-agnostic send abstraction. The concrete provider per channel
 * (SMTP / SMS / WhatsApp / Push / internal) is a later implementation decision resolved from the
 * channel's {@code configJson}; the design here stays neutral. Dispatch resolves an implementation
 * generically and delegates the actual send, applying {@link com.erp.notif.domain.RetryPolicy}.
 */
public interface ChannelProvider {

    /**
     * Attempts to deliver the rendered template to the recipient over the given channel. Never
     * throws for a delivery failure — it returns a failed {@link ChannelSendResult} so the caller
     * can drive the RULE-NOTIF-002 retry/FAILED path.
     */
    ChannelSendResult send(String channelTypeId, Long recipientId, NotificationTemplate template,
                           String configJson, Map<String, String> variables);
}
