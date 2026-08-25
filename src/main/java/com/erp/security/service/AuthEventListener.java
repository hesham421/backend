package com.erp.security.service;

import com.erp.notification.crossmodule.NotificationDispatchApi;
import com.erp.security.event.AccountActivationRequestedEvent;
import com.erp.security.event.PasswordResetRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

/**
 * RULE-SEC-031 — reacts to {@link AccountActivationRequestedEvent} and
 * {@link PasswordResetRequestedEvent}, published by {@link AuthService} via
 * {@code ApplicationEventPublisher} instead of calling erp-notification directly. The listener
 * stays in this module (the module that defines the events, per this codebase's event-isolation
 * convention — see {@code create-service}'s "Publishing Domain Events") and reaches
 * erp-notification only via {@link NotificationDispatchApi}, injected directly — never by
 * importing erp-notification's service/entity classes.
 *
 * <p>{@code AFTER_COMMIT} — mirrors erp-notification's own
 * {@code NotificationDispatchTrigger} pattern: the listener only fires once the triggering
 * signup/reset-request transaction has actually committed.
 */
@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private static final String MODULE_CODE = "SECURITY";

    private final NotificationDispatchApi notificationDispatchApi;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAccountActivationRequested(AccountActivationRequestedEvent event) {
        notificationDispatchApi.dispatch(event.userIdFk(), List.of("EMAIL"), "ACCOUNT_ACTIVATION_REQUESTED",
                Map.of("token", event.token(), "expiresAt", event.expiresAt().toString()),
                "HIGH", MODULE_CODE);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        notificationDispatchApi.dispatch(event.userIdFk(), List.of("EMAIL"), "PASSWORD_RESET_REQUESTED",
                event.contextData(), "HIGH", MODULE_CODE);
    }
}
