package com.erp.notification.event;

import com.erp.notification.dto.NotificationSendRequest;

/**
 * Plain Spring event (not {@code @TransactionalEventListener}-consumed) so the listener runs
 * synchronously, inline, sharing the publisher's own transaction.
 */
public record NotificationRequestedEvent(NotificationSendRequest request) {
}
