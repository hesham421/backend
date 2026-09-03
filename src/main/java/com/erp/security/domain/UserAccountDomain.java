package com.erp.security.domain;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.entity.UserAccount;
import com.erp.security.exception.SecErrorCodes;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Domain companion for ENTITY-SEC-001 (UserAccount). Owns every "is this operation allowed?"
 * decision for the identity entity: RULE-SEC-002 (required fields), RULE-SEC-003 (password
 * complexity), RULE-SEC-004 (store hashed only), RULE-SEC-005 (lockout), RULE-SEC-009
 * (login only when ACTIVE) and RULE-SEC-012 (status lifecycle). No Spring/JPA annotations,
 * no repository access; constructed only via the static factories below. The service supplies
 * uniqueness (RULE-SEC-001) existence flags — this class never touches persistence.
 */
public final class UserAccountDomain {

    // LOV-SEC-002 lifecycle codes (runtime codes, no DB lookup table).
    public static final String STATUS_PENDING_ACTIVATION = "PENDING_ACTIVATION";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";

    // RULE-SEC-012 state machine: PENDING_ACTIVATION -> ACTIVE ⇄ INACTIVE (no transition into PENDING).
    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
        STATUS_PENDING_ACTIVATION, Set.of(STATUS_ACTIVE),
        STATUS_ACTIVE, Set.of(STATUS_INACTIVE),
        STATUS_INACTIVE, Set.of(STATUS_ACTIVE)
    );

    // Client-bindable authentication policy defaults (business-policies-SEC ⚠Client, not pinned by SRS).
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 5; // policy default (RULE-SEC-005)
    public static final int PASSWORD_MIN_LENGTH = 8; // policy default (RULE-SEC-003)
    public static final Duration LOCK_DURATION = Duration.ofMinutes(15); // policy default (RULE-SEC-005)

    private final String username;
    private final String email;
    private final String userStatusId;
    private final boolean active;
    private final int failedLoginCount;
    private final LocalDateTime lockedUntil;

    private UserAccountDomain(String username, String email, String userStatusId,
                             boolean active, int failedLoginCount, LocalDateTime lockedUntil) {
        this.username = username;
        this.email = email;
        this.userStatusId = userStatusId;
        this.active = active;
        this.failedLoginCount = failedLoginCount;
        this.lockedUntil = lockedUntil;
    }

    /**
     * Construction-time validation for create: RULE-SEC-002 (required user-supplied fields) then
     * RULE-SEC-001 (username/email uniqueness, pre-checked by the service). The initial lifecycle
     * state is PENDING_ACTIVATION (A6). passwordHash is not set here — it is populated by the
     * activation/reset flow (RULE-SEC-004), never carried in the create body.
     */
    public static UserAccountDomain create(String username, String email, String fullName,
                                           boolean usernameAlreadyTaken, boolean emailAlreadyTaken) {
        if (isBlank(username) || isBlank(email) || isBlank(fullName)) {
            throw new LocalizedException(Status.VALIDATION_ERROR, SecErrorCodes.USER_ACCOUNT_FIELDS_REQUIRED);
        }
        if (usernameAlreadyTaken) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecErrorCodes.USER_ACCOUNT_USERNAME_DUPLICATE, username);
        }
        if (emailAlreadyTaken) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecErrorCodes.USER_ACCOUNT_EMAIL_DUPLICATE, email);
        }
        return new UserAccountDomain(username, email, STATUS_PENDING_ACTIVATION, true, 0, null);
    }

    /** Reconstructs a Domain view over a persisted entity — no validation. */
    public static UserAccountDomain from(UserAccount entity) {
        return new UserAccountDomain(
            entity.getUsername(),
            entity.getEmail(),
            entity.getUserStatusId(),
            Boolean.TRUE.equals(entity.getIsActive()),
            entity.getFailedLoginCount() == null ? 0 : entity.getFailedLoginCount(),
            entity.getLockedUntil());
    }

    /**
     * RULE-SEC-003 — password complexity decision (min length + letters and digits). Static because
     * it is evaluated on a raw candidate password before any entity exists (activation/reset).
     */
    public static void assertPasswordMeetsComplexity(String rawPassword) {
        boolean hasLetter = false;
        boolean hasDigit = false;
        if (rawPassword != null) {
            for (int i = 0; i < rawPassword.length(); i++) {
                char c = rawPassword.charAt(i);
                if (Character.isLetter(c)) {
                    hasLetter = true;
                } else if (Character.isDigit(c)) {
                    hasDigit = true;
                }
            }
        }
        if (rawPassword == null || rawPassword.length() < PASSWORD_MIN_LENGTH || !hasLetter || !hasDigit) {
            throw new LocalizedException(Status.VALIDATION_ERROR, SecErrorCodes.USER_ACCOUNT_PASSWORD_COMPLEXITY);
        }
    }

    /**
     * RULE-SEC-004 — invariant that only a hash is stored. Guards the value about to be persisted:
     * the hash must be present and must not equal the raw password.
     */
    public static void assertStoredHashed(String rawPassword, String passwordHash) {
        if (isBlank(passwordHash) || passwordHash.equals(rawPassword)) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.USER_ACCOUNT_PASSWORD_NOT_HASHED);
        }
    }

    /**
     * RULE-SEC-009 (login blocked unless ACTIVE) + RULE-SEC-005 (temporary lock still in effect).
     * Called by the auth service before verifying credentials.
     */
    public void assertLoginAllowed(LocalDateTime now) {
        if (!STATUS_ACTIVE.equals(userStatusId) || !active) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.USER_ACCOUNT_LOGIN_NOT_ACTIVE);
        }
        if (lockedUntil != null && now.isBefore(lockedUntil)) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.USER_ACCOUNT_LOCKED);
        }
    }

    /**
     * RULE-SEC-005 — decides the next failed-login state after a bad attempt: increments the counter
     * and, on reaching the threshold, sets a lock expiry. Decision only; the service applies the
     * returned values to the entity and saves.
     */
    public LockDecision registerFailedLogin(LocalDateTime now) {
        int next = failedLoginCount + 1;
        LocalDateTime lockUntil = next >= MAX_FAILED_LOGIN_ATTEMPTS ? now.plus(LOCK_DURATION) : null;
        return new LockDecision((short) next, lockUntil);
    }

    /**
     * RULE-SEC-012 — validates a userStatusId lifecycle transition
     * (PENDING_ACTIVATION -> ACTIVE ⇄ INACTIVE). Deactivation/reactivation permitted, no cascade.
     */
    public void assertCanTransitionTo(String targetStatusId) {
        if (targetStatusId != null && targetStatusId.equals(userStatusId)) {
            return;
        }
        Set<String> allowed = ALLOWED_TRANSITIONS.getOrDefault(userStatusId, Set.of());
        if (targetStatusId == null || !allowed.contains(targetStatusId)) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION,
                SecErrorCodes.USER_ACCOUNT_INVALID_STATUS_TRANSITION, userStatusId, targetStatusId);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getUserStatusId() {
        return userStatusId;
    }

    public boolean isActive() {
        return active;
    }

    /** Result of RULE-SEC-005 evaluation: the new counter and (optional) lock expiry. */
    public record LockDecision(Short failedLoginCount, LocalDateTime lockedUntil) {
    }
}
