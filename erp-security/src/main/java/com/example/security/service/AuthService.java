package com.example.security.service;

import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.security.config.properties.CookieProperties;
import com.example.security.config.properties.JwtProperties;
import com.example.security.config.properties.SelfServiceTokenProperties;
import com.example.security.dto.ActivateAccountRequest;
import com.example.security.dto.ForgotPasswordRequest;
import com.example.security.dto.ResetPasswordRequest;
import com.example.security.dto.SignupRequest;
import com.example.security.dto.SignupResponse;
import com.example.security.entity.AccountActivationToken;
import com.example.security.entity.PasswordResetToken;
import com.example.security.entity.RefreshToken;
import com.example.security.entity.UserAccount;
import com.example.erp.common.web.CookieUtils;
import com.example.erp.common.web.SameSite;
import com.example.security.event.AccountActivationRequestedEvent;
import com.example.security.event.PasswordResetRequestedEvent;
import com.example.security.exception.SecurityErrorCodes;
import com.example.security.repository.AccountActivationTokenRepository;
import com.example.security.repository.PasswordResetTokenRepository;
import com.example.security.repository.RefreshTokenRepository;
import com.example.security.repository.UserAccountRepository;
import com.example.security.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Authentication Service.
 *
 * Handles login, refresh, and logout operations.
 * Configuration loaded from {@link JwtProperties} and {@link CookieProperties}.
 *
 * @author ERP Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwt;
    private final RefreshTokenRepository refreshTokenRepo;
    private final UserAccountRepository userAccountRepo;
    private final PasswordEncoder passwordEncoder;
    private final AccountActivationTokenRepository accountActivationTokenRepo;
    private final PasswordResetTokenRepository passwordResetTokenRepo;
    private final ApplicationEventPublisher eventPublisher;

    // Configuration Properties (injected via constructor)
    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;
    private final SelfServiceTokenProperties selfServiceTokenProperties;

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    public record Tokens(String access, long accessExpSeconds, String refresh) {}

    @Transactional
    public Tokens login(String username, String password, HttpServletResponse response) {
        // Diagnostic: verify stored hash format and encoder compatibility before delegating to Spring.
        // Raw password is intentionally NOT logged (OWASP A09 – sensitive data in logs).
        userAccountRepo.findByUsernameIgnoreCase(username).ifPresent(u -> {
            String hash = u.getPassword();
            String prefix = (hash != null && hash.length() >= 4) ? hash.substring(0, 4) : "null";
            boolean matches = passwordEncoder.matches(password, hash);
            log.info("[AUTH-DIAG] user={} hashPrefix={} bcryptMatch={}",
                    username, prefix, matches);
        });
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        UserDetails principal = (UserDetails) auth.getPrincipal();
        List<String> authorities = principal.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).toList();

        // ابني الـ JWTs
        String jti = UUID.randomUUID().toString();

        // اربط التوكن بكـيان المستخدم
        UserAccount userEntity = userAccountRepo
            .findByUsernameIgnoreCase(principal.getUsername())
            .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.USER_NOT_FOUND));

        String access = jwt.generateAccess(principal.getUsername(), authorities, userEntity.getId());
        String refresh = jwt.generateRefresh(principal.getUsername(), jti);

        refreshTokenRepo.save(RefreshToken.builder()
                .jti(jti)
                .user(userEntity)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.refreshExpirationSeconds()))
                .revoked(false)
                .build());

        attachRefreshCookie(response, refresh);
        return new Tokens(access, jwtProperties.accessExpirationSeconds(), refresh);
    }

    @Transactional
    public Tokens refresh(HttpServletRequest request, HttpServletResponse response) {
        String raw = readRefreshCookie(request);
        var claims = jwt.parse(raw).getBody();
        String username = claims.getSubject();
        String jti = claims.getId();

        var db = refreshTokenRepo.findByJti(jti)
            .orElseThrow(() -> new LocalizedException(Status.UNAUTHORIZED, SecurityErrorCodes.REFRESH_REVOKED));
        if (db.isRevoked() || db.getExpiresAt().isBefore(Instant.now())) {
            throw new LocalizedException(Status.UNAUTHORIZED, SecurityErrorCodes.REFRESH_EXPIRED_OR_REVOKED);
        }

        // أوقف القديم ودوّر الجديد
        db.setRevoked(true);
        refreshTokenRepo.save(db);

        var userEntity = userAccountRepo
            .findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new LocalizedException(Status.NOT_FOUND, SecurityErrorCodes.USER_NOT_FOUND));

        var user = userDetailsService.loadUserByUsername(username);
        var authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();

        String newJti = UUID.randomUUID().toString();
        String access = jwt.generateAccess(username, authorities, userEntity.getId());
        String newRefresh = jwt.generateRefresh(username, newJti);

        refreshTokenRepo.save(RefreshToken.builder()
                .jti(newJti)
                .user(userEntity)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.refreshExpirationSeconds()))
                .revoked(false)
                .build());

        attachRefreshCookie(response, newRefresh);
        return new Tokens(access, jwtProperties.accessExpirationSeconds(), newRefresh);
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Logout must be idempotent: even if the refresh cookie is missing/expired,
        // we still clear client-side session cookies and return success.
        try {
            String raw = null;
            try {
                raw = readRefreshCookie(request);
            } catch (LocalizedException ex) {
                // Missing cookie is a normal case (e.g., token stored outside cookies, already cleared, or expired).
                raw = null;
            }

            if (raw == null || raw.isBlank()) {
                return;
            }

            var claims = jwt.parse(raw).getBody();
            String jti = claims.getId();

            refreshTokenRepo.findByJti(jti)
                .ifPresent(rt -> { rt.setRevoked(true); refreshTokenRepo.save(rt); });
        } catch (Exception ex) {
            log.debug("Best-effort refresh token revocation failed during logout", ex);
        } finally {
            clearRefreshCookie(response);
        }
    }

    private void attachRefreshCookie(HttpServletResponse response, String token) {
        // Use header-based Set-Cookie to include SameSite attribute (Cookie API lacks SameSite)
        SameSite sameSite = "STRICT".equalsIgnoreCase(cookieProperties.sameSite()) ? SameSite.STRICT :
                            "NONE".equalsIgnoreCase(cookieProperties.sameSite()) ? SameSite.NONE : SameSite.LAX;
        CookieUtils.addCookie(response, REFRESH_COOKIE_NAME, token,
                            (int) jwtProperties.refreshExpirationSeconds(),
                            cookieProperties.domain(),
                            cookieProperties.path(),
                            cookieProperties.secure(),
                            sameSite);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        CookieUtils.deleteCookie(response, REFRESH_COOKIE_NAME,
                                cookieProperties.domain(),
                                cookieProperties.path());
    }

    private String readRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) throw new LocalizedException(Status.UNAUTHORIZED, SecurityErrorCodes.NO_REFRESH_COOKIE);
        for (Cookie c : request.getCookies()) {
            if (REFRESH_COOKIE_NAME.equals(c.getName())) return c.getValue();
        }
        throw new LocalizedException(Status.UNAUTHORIZED, SecurityErrorCodes.NO_REFRESH_COOKIE);
    }

    /**
     * Login with full user information
     * @return UserInfo containing tokens and user details
     */
    @Transactional
    public com.example.security.dto.UserInfo loginWithUserInfo(String username, String password,
                                                                HttpServletResponse response) {
        // Diagnostic: verify stored hash format and encoder compatibility before delegating to Spring.
        userAccountRepo.findByUsernameIgnoreCase(username).ifPresentOrElse(u -> {
            String hash = u.getPassword();
            String prefix = (hash != null && hash.length() >= 4) ? hash.substring(0, 4) : "null";
            boolean matches = passwordEncoder.matches(password, hash);
            log.info("[AUTH-DIAG] loginWithUserInfo: user={} hashPrefix={} bcryptMatch={} enabled={}",
                    username, prefix, matches, u.isEnabled());
        }, () -> log.warn("[AUTH-DIAG] loginWithUserInfo: user={} NOT FOUND in database", username));

        // Authenticate user
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        UserDetails principal = (UserDetails) auth.getPrincipal();
        List<String> authorities = principal.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).toList();

        // Get user entity with roles and permissions
        UserAccount userEntity = userAccountRepo
                .findByUsernameWithRoles(principal.getUsername())
                .orElseThrow(() -> new LocalizedException(
                        Status.NOT_FOUND, SecurityErrorCodes.USER_NOT_FOUND));

        // Generate tokens
        String jti = UUID.randomUUID().toString();
        String access = jwt.generateAccess(principal.getUsername(), authorities, userEntity.getId());
        String refresh = jwt.generateRefresh(principal.getUsername(), jti);

        // Save refresh token
        refreshTokenRepo.save(RefreshToken.builder()
                .jti(jti)
                .user(userEntity)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.refreshExpirationSeconds()))
                .revoked(false)
                .build());

        attachRefreshCookie(response, refresh);

        // Convert to UserDto
        com.example.security.dto.UserDto userDto = com.example.security.mapper.UserMapper.toDto(userEntity);

        // Return UserInfo with tokens and user details
        return new com.example.security.dto.UserInfo(
                access,
                jwtProperties.accessExpirationSeconds(),
                refresh,
                jwtProperties.refreshExpirationSeconds(),
                userDto
        );
    }

    /**
     * Self-registration (API-SEC-040, SCR-SEC-008).
     * RULE-SEC-040/041: username/email must be globally unique. RULE-SEC-030: account MUST
     * start disabled. RULE-SEC-031: publishes AccountActivationRequestedEvent instead of
     * calling NotificationService directly.
     */
    @Transactional
    public SignupResponse signup(SignupRequest req) {
        log.info("Processing self-registration signup for username: {}", req.username());

        if (userAccountRepo.existsByUsernameIgnoreCase(req.username())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecurityErrorCodes.SIGNUP_USERNAME_ALREADY_EXISTS, req.username());
        }
        if (userAccountRepo.existsByEmailIgnoreCase(req.email())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecurityErrorCodes.SIGNUP_EMAIL_ALREADY_EXISTS, req.email());
        }

        UserAccount user = UserAccount.builder()
                .username(req.username())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .enabled(false) // RULE-SEC-030 — self-registered accounts MUST start disabled
                .build();
        UserAccount saved = userAccountRepo.save(user);

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plusSeconds(selfServiceTokenProperties.activationExpirationSeconds());
        accountActivationTokenRepo.save(AccountActivationToken.builder()
                .token(token)
                .user(saved)
                .expiresAt(expiresAt)
                .usedFl(false)
                .build());

        // RULE-SEC-031 — publish event instead of calling NotificationService directly
        eventPublisher.publishEvent(new AccountActivationRequestedEvent(saved.getId(), token, expiresAt));

        log.info("Self-registered new account (disabled, pending activation) username: {}", saved.getUsername());
        return new SignupResponse(saved.getId(), saved.getUsername(), saved.isEnabled());
    }

    /**
     * Account activation (API-SEC-041, SCR-SEC-008).
     * RULE-SEC-032: requires a valid, unused, non-expired token before flipping ENABLED to 1.
     * RULE-SEC-033: rejects an already-used/expired token; marks it used immediately on success.
     */
    @Transactional
    public void activateAccount(ActivateAccountRequest req) {
        log.info("Processing account activation");

        AccountActivationToken token = accountActivationTokenRepo.findByToken(req.token())
                .orElseThrow(() -> new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.ACTIVATION_TOKEN_INVALID_OR_EXPIRED));

        if (token.isUsed()) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.TOKEN_ALREADY_USED);
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.ACTIVATION_TOKEN_INVALID_OR_EXPIRED);
        }

        token.setUsed(true);
        accountActivationTokenRepo.save(token);

        UserAccount user = token.getUser();
        user.setEnabled(true);
        userAccountRepo.save(user);
        log.info("Activated account username: {}", user.getUsername());
    }

    /**
     * Forgot Password (API-SEC-042, SCR-SEC-009).
     * RULE-SEC-038 (anti-enumeration): ALWAYS behaves identically to the caller regardless of
     * whether the email exists — the lookup below only changes internal side effects (token
     * issuance + event publish), never the method's outcome/response.
     * RULE-SEC-039: invalidates any prior unexpired token for the same user when issuing a new one.
     * RULE-SEC-031: publishes PasswordResetRequestedEvent instead of calling NotificationService.
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        log.info("Processing forgot-password request");

        userAccountRepo.findByEmailIgnoreCase(req.email()).ifPresent(user -> {
            List<PasswordResetToken> priorTokens = passwordResetTokenRepo
                    .findByUser_IdAndUsedFlFalseAndExpiresAtAfter(user.getId(), Instant.now());
            priorTokens.forEach(t -> t.setUsed(true));
            passwordResetTokenRepo.saveAll(priorTokens);

            String token = UUID.randomUUID().toString();
            Instant expiresAt = Instant.now().plusSeconds(selfServiceTokenProperties.resetExpirationSeconds());
            passwordResetTokenRepo.save(PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(expiresAt)
                    .usedFl(false)
                    .build());

            eventPublisher.publishEvent(new PasswordResetRequestedEvent(user.getId(), token, expiresAt));
        });

        // RULE-SEC-038 — response is identical whether or not the email existed; nothing
        // beyond this point may branch on the Optional above.
        log.info("Forgot-password request processed");
    }

    /**
     * Reset Password (API-SEC-043, SCR-SEC-009).
     * RULE-SEC-032/033: requires a valid, unused, non-expired token; marks it used immediately
     * on success.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        log.info("Processing password reset");

        PasswordResetToken token = passwordResetTokenRepo.findByToken(req.token())
                .orElseThrow(() -> new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.RESET_TOKEN_INVALID_OR_EXPIRED));

        if (token.isUsed()) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.TOKEN_ALREADY_USED);
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.RESET_TOKEN_INVALID_OR_EXPIRED);
        }

        token.setUsed(true);
        passwordResetTokenRepo.save(token);

        UserAccount user = token.getUser();
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userAccountRepo.save(user);
        log.info("Password reset completed for username: {}", user.getUsername());
    }
}
