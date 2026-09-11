package com.erp.sec.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.sec.domain.UserDomain;
import com.erp.sec.dto.LoginRequest;
import com.erp.sec.dto.LoginResponse;
import com.erp.sec.entity.ActiveSession;
import com.erp.sec.entity.AuditLogEntry;
import com.erp.sec.entity.User;
import com.erp.sec.exception.SecErrorCodes;
import com.erp.sec.repository.ActiveSessionRepository;
import com.erp.sec.repository.AuditLogEntryRepository;
import com.erp.sec.repository.UserRepository;
import com.erp.sec.security.JwtTokenIssuer;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for API-SEC-001 (login). Pre-authentication by contract (SVC-API-INT.md
 * {@code Security : screen SEC_LOGIN · public — no permission required}), so no
 * {@code @PreAuthorize} gate exists here. Wrong password, unknown username and non-ACTIVE user are
 * indistinguishable to the caller (POL-SEC-004).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    /** USER_STATUS code (CHK_SEC_USER_STATUS) a login identity must carry (REQ-SEC-001). */
    private static final String STATUS_ACTIVE = "ACTIVE";

    /** AUDIT_EVENT_TYPE codes (CHK_SEC_AUDIT_LOG_EVENT_TYPE). */
    private static final String EVENT_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    private static final String EVENT_LOGIN_FAILED = "LOGIN_FAILED";

    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private final UserRepository repository;
    private final ActiveSessionRepository activeSessionRepository;
    private final AuditLogEntryRepository auditLogEntryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenIssuer jwtTokenIssuer;

    /**
     * API-SEC-001. {@code noRollbackFor} keeps the LOGIN_FAILED row REQ-SEC-002 mandates: the 401
     * is a {@code LocalizedException}, which would otherwise roll the audit insert back. It is the
     * only write on that path, so nothing else survives that should not.
     */
    @Transactional(noRollbackFor = LocalizedException.class)
    public ServiceResult<LoginResponse> login(LoginRequest request, String ipAddress) {
        log.info("Login attempt for username: {}", request.getUsername());

        User user = repository.findByUsername(request.getUsername()).orElse(null);

        if (!credentialsMatchActiveUser(user, request.getPassword())) {
            auditLogEntryRepository.save(AuditLogEntry.builder()
                .eventTypeCode(EVENT_LOGIN_FAILED)
                .actor(user)
                .occurredAt(Instant.now())
                .targetRef(request.getUsername())
                .detailsAr("محاولة دخول فاشلة")
                .detailsEn("Failed login attempt")
                .ipAddress(ipAddress)
                .build());
            log.info("Login failed for username: {}", request.getUsername());
            throw new LocalizedException(Status.UNAUTHORIZED,
                SecErrorCodes.SEC_401_INVALID_CREDENTIALS);
        }

        Instant now = Instant.now();
        String tokenRef = UUID.randomUUID().toString();

        activeSessionRepository.save(ActiveSession.builder()
            .user(user)
            .tokenRef(tokenRef)
            .startedAt(now)
            .lastActivityAt(now)
            .ipAddress(ipAddress)
            .build());

        user.setLastLoginAt(now);
        repository.save(user);

        auditLogEntryRepository.save(AuditLogEntry.builder()
            .eventTypeCode(EVENT_LOGIN_SUCCESS)
            .actor(user)
            .occurredAt(now)
            .targetRef(String.valueOf(user.getUserPk()))
            .detailsAr("تسجيل دخول ناجح")
            .detailsEn("Successful login")
            .ipAddress(ipAddress)
            .build());

        log.info("Login succeeded for User ID: {}", user.getUserPk());

        return ServiceResult.success(LoginResponse.builder()
            .accessToken(jwtTokenIssuer.issue(user, tokenRef, now))
            .tokenType(TOKEN_TYPE_BEARER)
            .expiresIn(jwtTokenIssuer.getExpiresInSeconds())
            .build());
    }

    /**
     * POL-SEC-004: one verdict for an unknown username, a non-ACTIVE account and a wrong password,
     * so the response, its message and its code cannot distinguish the three. This is the
     * PLATFORM-STD credential check (ADR-SEC-002), not a RULE-SEC-* decision — no SEC Domain object
     * models it, and {@code UserDomain} supplies the account facts it reads.
     */
    private boolean credentialsMatchActiveUser(User user, String rawPassword) {
        if (user == null) {
            return false;
        }
        UserDomain account = UserDomain.from(user);
        return account.isActive()
            && STATUS_ACTIVE.equals(account.getStatusCode())
            && passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
}
