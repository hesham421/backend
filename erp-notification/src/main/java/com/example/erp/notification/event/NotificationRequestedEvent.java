package com.example.erp.notification.event;

import com.example.erp.notification.dto.NotificationSendRequest;

/**
 * CORE.md ingress path 2 — "Spring Events (same-process, same-transaction) — for callers
 * already inside the same Spring context/transaction." A plain Spring application event
 * (not {@code @TransactionalEventListener}-consumed) so the default synchronous, same-thread
 * {@code ApplicationEventPublisher} behavior is preserved — the listener runs inline, sharing
 * the publisher's own transaction, per CORE's "same-transaction" requirement.
 */
public record NotificationRequestedEvent(NotificationSendRequest request) {
}
