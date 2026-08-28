package com.erp.notification.event;

/**
 * Fired per fan-out channel row persisted as PENDING. Consumed via
 * {@code @TransactionalEventListener(AFTER_COMMIT)}, not dispatched inside the persisting
 * transaction, since {@code @Async} dispatch would otherwise race the commit.
 */
public record NotificationLogPersistedEvent(Long notificationLogId) {
}
