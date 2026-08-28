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
 * RULE-SEC-031: reacts to events published by {@link AuthService}, reaching erp-notification
 * only via {@link NotificationDispatchApi} — never its service/entity classes directly.
 * {@code AFTER_COMMIT} so the listener only fires once the triggering transaction has committed.
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
