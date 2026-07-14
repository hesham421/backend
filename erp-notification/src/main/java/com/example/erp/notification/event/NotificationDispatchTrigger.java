package com.example.erp.notification.event;

import com.example.erp.notification.service.NotificationDispatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges the synchronous persist step to the async dispatch step, only after the persisting
 * transaction has actually committed — see {@link NotificationLogPersistedEvent} javadoc.
 */
@Component
@RequiredArgsConstructor
public class NotificationDispatchTrigger {

    private final NotificationDispatchService dispatchService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationLogPersisted(NotificationLogPersistedEvent event) {
        dispatchService.dispatchAsync(event.notificationLogId());
    }
}
