package com.erp.security.domain;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.entity.PasswordResetToken;
import com.erp.security.exception.SecErrorCodes;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Domain companion for ENTITY-SEC-006 (PasswordResetToken). Owns every "is this operation
 * allowed?" decision for the reset artifact — single-use consumability (not used, not expired)
 * under RULE-SEC-007. No Spring/JPA annotations, no repository access; constructed only via the
 * static factories. The "single active" half of RULE-SEC-007 (superseding prior live tokens) is
 * enforced at the service layer, which owns the persistence lookup.
 */
public final class PasswordResetTokenDomain {

    // Client-bindable reset TTL default (business-policies-SEC ⚠Client, RULE-SEC-007).
    public static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(60); // policy default

    private final boolean used;
    private final LocalDateTime expiresAt;

    private PasswordResetTokenDomain(boolean used, LocalDateTime expiresAt) {
        this.used = used;
        this.expiresAt = expiresAt;
    }

    /**
     * Issue-time factory: computes the expiry from the reset TTL policy default. The hashed
     * token value itself is set on the entity by the service (RULE-SEC-004).
     */
    public static PasswordResetTokenDomain issue(LocalDateTime now) {
        return new PasswordResetTokenDomain(false, now.plus(RESET_TOKEN_TTL));
    }

    /** Reconstructs a Domain view over a persisted entity — no validation. */
    public static PasswordResetTokenDomain from(PasswordResetToken entity) {
        return new PasswordResetTokenDomain(
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
     * RULE-SEC-007 — a reset token may be consumed exactly once, while unexpired. The service
     * marks it used (entity.markUsed()) after this returns.
     */
    public void assertConsumable(LocalDateTime now) {
        if (used) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.PASSWORD_RESET_TOKEN_USED);
        }
        if (isExpired(now)) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.PASSWORD_RESET_TOKEN_EXPIRED);
        }
    }

    public boolean isUsed() {
        return used;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
