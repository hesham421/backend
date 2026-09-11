package com.erp.sec.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.util.SecurityContextHelper;
import com.erp.common.util.TokenHasher;
import com.erp.notif.crossmodule.DispatchCommand;
import com.erp.notif.crossmodule.NotificationDispatchApi;
import com.erp.sec.domain.PasswordResetTokenDomain;
import com.erp.sec.dto.ConfirmationResponse;
import com.erp.sec.dto.PasswordResetCompleteRequest;
import com.erp.sec.dto.PasswordResetRequest;
import com.erp.sec.entity.ActiveSession;
import com.erp.sec.entity.AuditLogEntry;
import com.erp.sec.entity.PasswordResetToken;
import com.erp.sec.entity.User;
import com.erp.sec.exception.SecErrorCodes;
import com.erp.sec.repository.ActiveSessionRepository;
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
    private static final String EVENT_SESSION_TERMINATED = "SESSION_TERMINATED";

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
    private final ActiveSessionRepository activeSessionRepository;
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

        terminateOpenSessions(user, now);

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

    /**
     * A reset is the action a user takes to lock an attacker out, but {@code JwtAuthenticationFilter}
     * authenticates against {@code ActiveSession.tokenRef}/{@code terminatedAt} alone — independent
     * of the password hash — so a token issued before the reset would otherwise survive it. Mirrors
     * the termination loop of {@code UserService.deactivate}; no {@code assertCanTerminate} guard is
     * needed because the query already returns only non-terminated rows, and a 409 raised here would
     * abort a reset that has already succeeded. The principal is {@code SYSTEM}, since API-SEC-004
     * is pre-authentication and the context therefore carries no caller.
     */
    private void terminateOpenSessions(User user, Instant now) {
        String principal = SecurityContextHelper.getCurrentUsername();
        List<ActiveSession> openSessions =
            activeSessionRepository.findNonTerminatedByUser(user.getUserPk());
        for (ActiveSession session : openSessions) {
            session.terminate(principal);
            auditLogEntryRepository.save(AuditLogEntry.builder()
                .eventTypeCode(EVENT_SESSION_TERMINATED)
                .actor(user)
                .occurredAt(now)
                .targetRef(String.valueOf(session.getActiveSessionPk()))
                .detailsAr("إنهاء الجلسة بسبب إعادة تعيين كلمة المرور")
                .detailsEn("Session terminated because the password was reset")
                .build());
        }
        activeSessionRepository.saveAll(openSessions);

        log.info("Password reset terminated sessions: {} for User ID: {}",
            openSessions.size(), user.getUserPk());
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
     * requires. Propagation intent: the call goes through NOTIF's {@code dispatchIndependently}
     * entry point, which declares {@code REQUIRES_NEW} — the dispatch commits or rolls back on its
     * own, so a failure inside it can never mark this transaction rollback-only and the
     * PasswordResetToken + audit rows still commit (the catch below is therefore effective).
     *
     * <p>The recipient's {@code email} travels among the dispatch variables because that is NOTIF's
     * published contract to callers: {@code DefaultChannelProvider} reads the destination address
     * from {@code variables.get("email")} "since NOTIF has no crossmodule contact-lookup for a bare
     * recipientId", and returns {@code failure("missing recipient email address")} without it.
     * ENT-SEC-001's {@code email} field is specified for exactly this — "used for password-reset
     * delivery".
     */
    private void dispatchResetNotification(User user, PasswordResetToken token, String rawToken) {
        try {
            InternalCallerContext.call(() -> notificationDispatchApi.dispatchIndependently(
                new DispatchCommand(
                    user.getUserPk(),
                    TEMPLATE_PASSWORD_RESET,
                    List.of(CHANNEL_EMAIL),
                    MODULE_CODE,
                    token.getPwdResetTokenPk(),
                    REFERENCE_TYPE,
                    Map.of("token", rawToken,
                        "expiresAt", String.valueOf(token.getExpiresAt()),
                        "email", user.getEmail()))));
        } catch (RuntimeException e) {
            log.warn("Password-reset notification dispatch failed for User ID {} — the reset "
                + "request itself still succeeds (REQ-SEC-029 is optional)", user.getUserPk(), e);
        }
    }
}
