package com.example.security.service;

import com.example.security.client.NotificationClient;
import com.example.security.event.AccountActivationRequestedEvent;
import com.example.security.event.PasswordResetRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * RULE-SEC-031 — reacts to {@link AccountActivationRequestedEvent} and
 * {@link PasswordResetRequestedEvent}, published by {@link AuthService} via
 * {@code ApplicationEventPublisher} instead of calling erp-notification directly. The listener
 * stays in this module (the module that defines the events, per this codebase's event-isolation
 * convention — see {@code create-service}'s "Publishing Domain Events") and reaches
 * erp-notification only via {@link NotificationClient}'s REST self-call, never by importing
 * erp-notification's service/entity classes directly.
 *
 * <p>{@code AFTER_COMMIT} — mirrors erp-notification's own
 * {@code NotificationDispatchTrigger} pattern: the listener only fires once the triggering
 * signup/reset-request transaction has actually committed.
 */
@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private final NotificationClient notificationClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAccountActivationRequested(AccountActivationRequestedEvent event) {
        notificationClient.sendAccountActivation(event.userIdFk(), event.token(), event.expiresAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        notificationClient.sendPasswordReset(event.userIdFk(), event.token(), event.expiresAt());
    }
}
