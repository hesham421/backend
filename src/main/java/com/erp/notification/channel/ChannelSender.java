package com.erp.notification.channel;

import com.erp.notification.entity.NotificationLog;

/**
 * Channel dispatch adapter contract; one implementation is registered per deployment — see
 * {@link StubChannelSender} for why real per-provider adapters aren't built yet.
 */
public interface ChannelSender {

    /**
     * Attempts one delivery attempt for the given log row. Must not throw for an ordinary
     * delivery failure — return {@code false} so the caller can apply RULE-NOTIF-004's
     * retry/backoff policy.
     */
    boolean send(NotificationLog logEntry);
}
