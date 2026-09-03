package com.erp.security.domain;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.entity.AccountActivationToken;
import com.erp.security.exception.SecErrorCodes;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Domain companion for ENTITY-SEC-007 (AccountActivationToken). Owns every "is this operation
 * allowed?" decision for the activation artifact — single-use consumability (not used, not
 * expired) under RULE-SEC-008. No Spring/JPA annotations, no repository access; constructed only
 * via the static factories. The "single active" half of RULE-SEC-008 (superseding prior live
 * tokens) is enforced at the service layer, which owns the persistence lookup.
 */
public final class AccountActivationTokenDomain {

    // Client-bindable activation TTL default (business-policies-SEC ⚠Client, RULE-SEC-008).
    public static final Duration ACTIVATION_TOKEN_TTL = Duration.ofHours(24); // policy default

    private final boolean used;
    private final LocalDateTime expiresAt;

    private AccountActivationTokenDomain(boolean used, LocalDateTime expiresAt) {
        this.used = used;
        this.expiresAt = expiresAt;
    }

    /**
     * Issue-time factory: computes the expiry from the activation TTL policy default. The hashed
     * token value itself is set on the entity by the service (RULE-SEC-004).
     */
    public static AccountActivationTokenDomain issue(LocalDateTime now) {
        return new AccountActivationTokenDomain(false, now.plus(ACTIVATION_TOKEN_TTL));
    }

    /** Reconstructs a Domain view over a persisted entity — no validation. */
    public static AccountActivationTokenDomain from(AccountActivationToken entity) {
        return new AccountActivationTokenDomain(
            Boolean.TRUE.equals(entity.getUsed()),
            entity.getExpiresAt());
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt == null || !now.isBefore(expiresAt);
    }

    public boolean isConsumable(LocalDateTime now) {
        return !used && !isExpired(now);
    }

    /**
     * RULE-SEC-008 — an activation token may be consumed exactly once, while unexpired. The
     * service marks it used (entity.markUsed()) after this returns.
     */
    public void assertConsumable(LocalDateTime now) {
        if (used) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.ACCOUNT_ACTIVATION_TOKEN_USED);
        }
        if (isExpired(now)) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.ACCOUNT_ACTIVATION_TOKEN_EXPIRED);
        }
    }

    public boolean isUsed() {
        return used;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
