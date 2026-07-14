package com.example.erp.notification.event;

/**
 * Fired once per fan-out channel row that was persisted as PENDING (RULE-NOTIF-003). Consumed
 * by {@code NotificationDispatchTrigger} with {@code @TransactionalEventListener(phase =
 * AFTER_COMMIT)} — deliberately NOT dispatched directly from within
 * {@code NotificationEventProcessor}'s own {@code @Transactional} method, because the
 * dispatch runs {@code @Async} on a separate thread that would otherwise race the enclosing
 * transaction's commit (risk of the dispatch thread reading a NOTIF_LOG row that isn't visible
 * yet).
 */
public record NotificationLogPersistedEvent(Long notificationLogId) {
}
