package com.erp.sec.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.util.TokenHasher;
import com.erp.notif.crossmodule.DispatchCommand;
import com.erp.notif.crossmodule.NotificationDispatchApi;
import com.erp.sec.domain.PasswordResetTokenDomain;
import com.erp.sec.dto.ConfirmationResponse;
import com.erp.sec.dto.PasswordResetCompleteRequest;
import com.erp.sec.dto.PasswordResetRequest;
import com.erp.sec.entity.AuditLogEntry;
import com.erp.sec.entity.PasswordResetToken;
import com.erp.sec.entity.User;
import com.erp.sec.exception.SecErrorCodes;
import com.erp.sec.repository.AuditLogEntryRepository;
import com.erp.sec.repository.PasswordResetTokenRepository;
import com.erp.sec.repository.UserRepository;
import com.erp.sec.security.InternalCallerContext;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for API-SEC-003 / API-SEC-004. Both are pre-authentication by contract
 * (SVC-API-INT.md {@code Security : screen SEC_PWD_RESET · public — no permission required}), so
 * neither carries a {@code @PreAuthorize} gate. The raw token leaves this class only through the
 * notification variables; only its SHA-256 hash is persisted or compared.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    /** AUDIT_EVENT_TYPE codes (CHK_SEC_AUDIT_LOG_EVENT_TYPE). */
    private static final String EVENT_PASSWORD_RESET_REQUESTED = "PASSWORD_RESET_REQUESTED";
    private static final String EVENT_PASSWORD_RESET_COMPLETED = "PASSWORD_RESET_COMPLETED";

    /** Seeded by V11__notif_email_channel_seed.sql / updated by V12 — never invented here. */
    private static final String TEMPLATE_PASSWORD_RESET = "PASSWORD_RESET";
    private static final String CHANNEL_EMAIL = "EMAIL";
    private static final String MODULE_CODE = "SEC";
    private static final String REFERENCE_TYPE = "SEC_PWD_RESET_TOKEN";

    private static final String REQUEST_CONFIRMATION_AR =
        "إذا كان البريد الإلكتروني مسجلًا فسيتم إرسال رابط إعادة التعيين";
    private static final String REQUEST_CONFIRMATION_EN =
        "If that email is registered, a reset link has been sent";
    private static final String COMPLETE_CONFIRMATION_AR = "تم تحديث كلمة المرور";
    private static final String COMPLETE_CONFIRMATION_EN = "Your password has been updated";

    private final PasswordResetTokenRepository repository;
    private final UserRepository userRepository;
    private final AuditLogEntryRepository auditLogEntryRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationDispatchApi notificationDispatchApi;

    /**
     * API-SEC-003 — the same generic confirmation is returned whether or not the email resolves to
     * a user, so the response never reveals which (Response line, REQ-SEC-006).
     */
    @Transactional
    public ServiceResult<ConfirmationResponse> request(PasswordResetRequest request) {
        log.info("Password reset requested");

        userRepository.findByEmail(request.getEmail()).ifPresent(this::issueToken);

        return ServiceResult.success(ConfirmationResponse.builder()
            .messageAr(REQUEST_CONFIRMATION_AR)
            .messageEn(REQUEST_CONFIRMATION_EN)
            .build());
    }

    /** API-SEC-004 — RULE-SEC-006 is decided by {@code PasswordResetTokenDomain.assertUsable}. */
    @Transactional
    public ServiceResult<ConfirmationResponse> complete(PasswordResetCompleteRequest request) {
        log.info("Password reset completion submitted");

        PasswordResetToken token = repository
            .findByTokenHash(TokenHasher.sha256Hex(request.getToken()))
            .orElseThrow(() -> new LocalizedException(
                Status.CONFLICT, SecErrorCodes.SEC_409_RESET_TOKEN_INVALID));

        Instant now = Instant.now();
        PasswordResetTokenDomain.from(token).assertUsable(now);

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.markUsed();
        repository.save(token);

        auditLogEntryRepository.save(AuditLogEntry.builder()
            .eventTypeCode(EVENT_PASSWORD_RESET_COMPLETED)
            .actor(user)
            .occurredAt(now)
            .targetRef(String.valueOf(user.getUserPk()))
            .detailsAr("تم إتمام إعادة تعيين كلمة المرور")
            .detailsEn("Password reset completed")
            .build());

        log.info("Password reset completed for User ID: {}", user.getUserPk());

        return ServiceResult.success(ConfirmationResponse.builder()
            .messageAr(COMPLETE_CONFIRMATION_AR)
            .messageEn(COMPLETE_CONFIRMATION_EN)
            .build());
    }

    private void issueToken(User user) {
        String rawToken = UUID.randomUUID().toString();
        PasswordResetToken saved = repository.save(PasswordResetToken.builder()
            .user(user)
            .tokenHash(TokenHasher.sha256Hex(rawToken))
            .build());

        auditLogEntryRepository.save(AuditLogEntry.builder()
            .eventTypeCode(EVENT_PASSWORD_RESET_REQUESTED)
            .actor(user)
            .occurredAt(Instant.now())
            .targetRef(String.valueOf(user.getUserPk()))
            .detailsAr("تم طلب إعادة تعيين كلمة المرور")
            .detailsEn("Password reset requested")
            .build());

        dispatchResetNotification(user, saved, rawToken);
    }

    /**
     * REQ-SEC-029, the SRS A8 SOFT/optional integration, called from the anonymous API-SEC-003:
     * {@link InternalCallerContext} supplies the principal NOTIF's {@code isAuthenticated()} gate
     * requires. Propagation intent: NOTIF's {@code dispatch} declares plain {@code REQUIRED} and
     * joins this transaction — a failure inside it still marks the shared transaction
     * rollback-only, which this catch cannot undo (see execution-state.json gap #5).
     */
    private void dispatchResetNotification(User user, PasswordResetToken token, String rawToken) {
        try {
            InternalCallerContext.call(() -> notificationDispatchApi.dispatch(new DispatchCommand(
                user.getUserPk(),
                TEMPLATE_PASSWORD_RESET,
                List.of(CHANNEL_EMAIL),
                MODULE_CODE,
                token.getPwdResetTokenPk(),
                REFERENCE_TYPE,
                Map.of("token", rawToken, "expiresAt", String.valueOf(token.getExpiresAt())))));
        } catch (RuntimeException e) {
            log.warn("Password-reset notification dispatch failed for User ID {} — the reset "
                + "request itself still succeeds (REQ-SEC-029 is optional)", user.getUserPk(), e);
        }
    }
}
