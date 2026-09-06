package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.common.util.TokenHasher;
import com.erp.security.domain.AccountActivationTokenDomain;
import com.erp.security.domain.PasswordResetTokenDomain;
import com.erp.security.domain.RefreshTokenDomain;
import com.erp.security.domain.UserAccountDomain;
import com.erp.security.dto.ActivateAccountRequest;
import com.erp.security.dto.ForgotPasswordRequest;
import com.erp.security.dto.LoginRequest;
import com.erp.security.dto.LogoutRequest;
import com.erp.security.dto.RefreshTokenRequest;
import com.erp.security.dto.ResetPasswordRequest;
import com.erp.security.dto.TokenResponse;
import com.erp.security.entity.AccountActivationToken;
import com.erp.security.entity.PasswordResetToken;
import com.erp.security.entity.RefreshToken;
import com.erp.security.entity.UserAccount;
import com.erp.security.event.PasswordResetRequestedEvent;
import com.erp.security.exception.SecErrorCodes;
import com.erp.security.jwt.JwtTokenProvider;
import com.erp.security.repository.AccountActivationTokenRepository;
import com.erp.security.repository.PasswordResetTokenRepository;
import com.erp.security.repository.RefreshTokenRepository;
import com.erp.security.repository.UserAccountRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for the six authentication flows (API-SEC-001..006). Every "is this allowed?"
 * decision is delegated to the SEC Domain objects (UserAccountDomain, RefreshTokenDomain,
 * PasswordResetTokenDomain, AccountActivationTokenDomain); this class only loads data, applies the
 * returned decisions, hashes secrets, mints tokens and persists. Passwords are BCrypt-hashed;
 * opaque refresh/reset/activation tokens are stored as their SHA-256 hash (RULE-SEC-004 / DRV-005).
 *
 * <p>No {@code @PreAuthorize} on these methods: login/refresh/forgot/reset/activate are public
 * pre-auth endpoints and logout only requires an authenticated caller (SVC-API-AUTH spec), so none
 * carries a specific authority. This is the spec-mandated deviation from build-create-service A.5.2.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AccountActivationTokenRepository accountActivationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;

    // noRollbackFor: a rejected login (bad credentials / locked / inactive) is signalled by throwing
    // LocalizedException, but the RULE-SEC-005 failed-attempt bookkeeping written just before the
    // throw MUST persist — the default rollback-on-RuntimeException would silently undo it and the
    // lockout could never engage. The only write on the reject path is that intended counter update,
    // so committing it (rather than rolling back) is exactly the desired behaviour.
    @Transactional(noRollbackFor = LocalizedException.class)
    public ServiceResult<TokenResponse> login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());
        LocalDateTime now = LocalDateTime.now();

        UserAccount user = userAccountRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new LocalizedException(
                Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.USER_ACCOUNT_INVALID_CREDENTIALS));

        UserAccountDomain domain = UserAccountDomain.from(user);
        // RULE-SEC-009 (active) + RULE-SEC-005 (lock still in effect) before verifying the secret.
        domain.assertLoginAllowed(now);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // RULE-SEC-005 — record the failed attempt / apply the lock decision, then reject.
            UserAccountDomain.LockDecision decision = domain.registerFailedLogin(now);
            user.setFailedLoginCount(decision.failedLoginCount());
            user.setLockedUntil(decision.lockedUntil());
            userAccountRepository.save(user);
            throw new LocalizedException(
                Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.USER_ACCOUNT_INVALID_CREDENTIALS);
        }

        user.setFailedLoginCount((short) 0);
        user.setLockedUntil(null);
        userAccountRepository.save(user);

        TokenResponse response = issueTokenPair(user, now);
        log.info("Login succeeded for user ID: {}", user.getId());
        return ServiceResult.success(response);
    }

    @Transactional
    public ServiceResult<TokenResponse> refresh(RefreshTokenRequest request) {
        log.info("Refresh token rotation requested");
        LocalDateTime now = LocalDateTime.now();

        RefreshToken token = refreshTokenRepository.findByToken(TokenHasher.sha256Hex(request.getRefreshToken()))
            .orElseThrow(() -> new LocalizedException(
                Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.REFRESH_TOKEN_REVOKED));

        // RULE-SEC-006 — usable (not revoked, not expired) before rotating.
        RefreshTokenDomain.from(token).assertCanRotate(now);

        // RULE-SEC-009 / RULE-SEC-005 — the account must still be login-eligible: a user deactivated
        // (or locked) after the refresh token was issued must not keep minting access tokens, exactly
        // as login() blocks it. Without this a disabled user retains access until token expiry.
        UserAccountDomain.from(token.getUserAccount()).assertLoginAllowed(now);

        token.revoke();
        refreshTokenRepository.save(token);

        TokenResponse response = issueTokenPair(token.getUserAccount(), now);
        log.info("Refresh token rotated for user ID: {}", token.getUserAccount().getId());
        return ServiceResult.success(response);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        log.info("Logout requested");
        // RULE-SEC-006 — idempotent revoke: unknown or already-revoked token is a silent no-op (204).
        refreshTokenRepository.findByToken(TokenHasher.sha256Hex(request.getRefreshToken())).ifPresent(token -> {
            if (!Boolean.TRUE.equals(token.getRevoked())) {
                token.revoke();
                refreshTokenRepository.save(token);
            }
        });
    }

    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        log.info("Password reset requested");
        LocalDateTime now = LocalDateTime.now();

        // Neutral by design (no account enumeration): silently do nothing unless an ACTIVE account matches.
        userAccountRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            if (!UserAccountDomain.STATUS_ACTIVE.equals(user.getUserStatusId())
                || !Boolean.TRUE.equals(user.getIsActive())) {
                return;
            }
            // RULE-SEC-007 (single active): supersede any prior live reset tokens.
            List<PasswordResetToken> active = passwordResetTokenRepository.findActiveByUserId(user.getId());
            active.forEach(PasswordResetToken::markUsed);
            passwordResetTokenRepository.saveAll(active);

            String raw = jwtTokenProvider.generateOpaqueToken();
            PasswordResetToken token = PasswordResetToken.builder()
                .token(TokenHasher.sha256Hex(raw))
                .expiresAt(PasswordResetTokenDomain.issue(now).getExpiresAt())
                .used(false)
                .userAccount(user)
                .build();
            passwordResetTokenRepository.save(token);

            eventPublisher.publishEvent(new PasswordResetRequestedEvent(
                user.getId(), user.getEmail(), raw, token.getExpiresAt(), user.getPreferredLangId()));
            log.info("Password reset token issued for user ID: {}", user.getId());
        });
    }

    @Transactional
    public ServiceResult<Void> resetPassword(ResetPasswordRequest request) {
        log.info("Password reset submission received");
        LocalDateTime now = LocalDateTime.now();

        PasswordResetToken token = passwordResetTokenRepository.findByToken(TokenHasher.sha256Hex(request.getToken()))
            .orElseThrow(() -> new LocalizedException(
                Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.PASSWORD_RESET_TOKEN_USED));

        // RULE-SEC-007 (valid/unused/unexpired) then RULE-SEC-003 (complexity).
        PasswordResetTokenDomain.from(token).assertConsumable(now);
        UserAccountDomain.assertPasswordMeetsComplexity(request.getNewPassword());

        UserAccount user = token.getUserAccount();
        // RULE-SEC-009 — mirror requestReset: only an ACTIVE account can complete a reset, so a token
        // issued before the account was deactivated cannot silently "succeed" on a disabled account.
        // Use assertActive (NOT assertLoginAllowed): a valid reset token is an out-of-band proof of
        // ownership, so the transient RULE-SEC-005 login lock must not block the recovery path — a
        // successful reset itself clears the lock below.
        UserAccountDomain.from(user).assertActive();

        String encoded = passwordEncoder.encode(request.getNewPassword());
        UserAccountDomain.assertStoredHashed(request.getNewPassword(), encoded); // RULE-SEC-004
        user.setPasswordHash(encoded);
        // A completed reset is a fresh start: clear any RULE-SEC-005 failed-attempt lock so the user
        // can log in immediately with the new password.
        user.setFailedLoginCount((short) 0);
        user.setLockedUntil(null);
        userAccountRepository.save(user);

        token.markUsed();
        passwordResetTokenRepository.save(token);

        // A password change must terminate every pre-existing session (RULE-SEC-007): revoke all live
        // refresh tokens so a session opened before the reset (e.g. by an attacker) cannot outlive it.
        List<RefreshToken> liveSessions = refreshTokenRepository.findByUserAccount_IdAndRevokedFalse(user.getId());
        liveSessions.forEach(RefreshToken::revoke);
        refreshTokenRepository.saveAll(liveSessions);

        log.info("Password reset applied for user ID: {}", user.getId());
        return ServiceResult.success(null);
    }

    @Transactional
    public ServiceResult<Void> activate(ActivateAccountRequest request) {
        log.info("Account activation submission received");
        LocalDateTime now = LocalDateTime.now();

        AccountActivationToken token = accountActivationTokenRepository.findByToken(TokenHasher.sha256Hex(request.getToken()))
            .orElseThrow(() -> new LocalizedException(
                Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.ACCOUNT_ACTIVATION_TOKEN_USED));

        // RULE-SEC-008 (valid/unused/unexpired).
        AccountActivationTokenDomain.from(token).assertConsumable(now);

        UserAccount user = token.getUserAccount();
        UserAccountDomain userDomain = UserAccountDomain.from(user);
        // RULE-SEC-009 — PENDING_ACTIVATION -> ACTIVE transition.
        userDomain.assertCanTransitionTo(UserAccountDomain.STATUS_ACTIVE);

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            UserAccountDomain.assertPasswordMeetsComplexity(request.getNewPassword());
            String encoded = passwordEncoder.encode(request.getNewPassword());
            UserAccountDomain.assertStoredHashed(request.getNewPassword(), encoded); // RULE-SEC-004
            user.setPasswordHash(encoded);
        }
        user.setUserStatusId(UserAccountDomain.STATUS_ACTIVE);
        userAccountRepository.save(user);

        token.markUsed();
        accountActivationTokenRepository.save(token);
        log.info("Account activated for user ID: {}", user.getId());
        return ServiceResult.success(null);
    }

    private TokenResponse issueTokenPair(UserAccount user, LocalDateTime now) {
        List<String> authorities = userAccountRepository.findGrantedPermissionCodes(user.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getId(), authorities);

        String rawRefresh = jwtTokenProvider.generateOpaqueToken();
        RefreshToken refresh = RefreshToken.builder()
            .token(TokenHasher.sha256Hex(rawRefresh))
            .expiresAt(RefreshTokenDomain.issue(now).getExpiresAt())
            .revoked(false)
            .userAccount(user)
            .build();
        refreshTokenRepository.save(refresh);

        return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(rawRefresh)
            .expiresIn(jwtTokenProvider.getAccessTokenTtlSeconds())
            .build();
    }
}
