package com.example.erp.notification.channel;

import com.example.erp.notification.entity.NotificationLog;

/**
 * Channel dispatch adapter contract (CORE.md — "Channel dispatch adapters ... live in a
 * module-local channel/ package ... invoked by the retry/dispatch orchestration in service/").
 * One implementation is registered per deployment; see {@link StubChannelSender} for the
 * current Phase-1 registration and why real per-provider adapters are not built yet.
 */
public interface ChannelSender {

    /**
     * Attempts one delivery attempt for the given log row. Must not throw for an ordinary
     * delivery failure — return {@code false} so the caller can apply RULE-NOTIF-004's
     * retry/backoff policy.
     */
    boolean send(NotificationLog logEntry);
}
