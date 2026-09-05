package com.erp.notif.crossmodule;

import com.erp.notif.dto.DispatchRequest;
import com.erp.notif.service.DispatchService;
import com.erp.security.event.AccountActivationRequestedEvent;
import com.erp.security.event.PasswordResetRequestedEvent;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * NOTIF-owned bridge for the SEC auth events (see the events' own javadoc: "SEC never calls NOTIF
 * directly ... NOTIF, once built, listens for this ApplicationEvent"). Fires only AFTER_COMMIT so a
 * slow/failed email send can never roll back the token/user-creation transaction that triggered it,
 * and never lets a dispatch failure escape back to the originating (often unauthenticated, e.g.
 * forgot-password) HTTP request — this is best-effort delivery, tracked via NOTIF_LOG, not a
 * guarantee the caller should fail on.
 *
 * <p>Builds a one-click {@code actionLink} (frontend URL + token as a query param) rather than
 * making the end user copy/paste a raw token — the raw token is still passed as a variable too, for
 * any channel/template that still wants it, but EMAIL's template links instead.
 */
@Component
@Slf4j
public class SecurityAuthEventListener {

    public static final String TEMPLATE_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String TEMPLATE_ACCOUNT_ACTIVATION = "ACCOUNT_ACTIVATION";

    private static final DateTimeFormatter EXPIRY_FORMAT =
        DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);

    private final DispatchService dispatchService;
    private final String frontendUrl;

    public SecurityAuthEventListener(DispatchService dispatchService,
                                     @Value("${app.frontend-url}") String frontendUrl) {
        this.dispatchService = dispatchService;
        this.frontendUrl = frontendUrl;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        dispatch(TEMPLATE_PASSWORD_RESET, "/reset-password", "Reset Password", "إعادة تعيين كلمة المرور",
            event.userAccountId(), event.email(), event.rawToken(), event.expiresAt(), event.preferredLangId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAccountActivationRequested(AccountActivationRequestedEvent event) {
        dispatch(TEMPLATE_ACCOUNT_ACTIVATION, "/activate", "Activate Account", "تفعيل الحساب",
            event.userAccountId(), event.email(), event.rawToken(), event.expiresAt(), event.preferredLangId());
    }

    private void dispatch(String templateCode, String frontendPath, String ctaLabelEn, String ctaLabelAr,
                          Long userAccountId, String email, String rawToken, LocalDateTime expiresAt,
                          String preferredLangId) {
        try {
            String actionLink = frontendUrl + frontendPath + "?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
            dispatchService.dispatchSystem(DispatchRequest.builder()
                .recipientId(userAccountId)
                .templateCode(templateCode)
                .channelHint(List.of("EMAIL"))
                .moduleCode("SEC")
                .referenceId(userAccountId)
                .referenceType("USER_ACCOUNT")
                .variables(Map.of(
                    "email", email,
                    "token", rawToken,
                    "actionLink", actionLink,
                    "ctaLabelEn", ctaLabelEn,
                    "ctaLabelAr", ctaLabelAr,
                    "expiresAt", expiresAt.format(EXPIRY_FORMAT),
                    "lang", preferredLangId != null ? preferredLangId : "EN"))
                .build());
        } catch (Exception ex) {
            log.warn("Failed to dispatch {} notification for user {}: {}",
                templateCode, userAccountId, ex.getMessage());
        }
    }
}
