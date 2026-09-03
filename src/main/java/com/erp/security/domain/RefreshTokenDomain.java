package com.erp.security.domain;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.entity.RefreshToken;
import com.erp.security.exception.SecErrorCodes;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Domain companion for ENTITY-SEC-005 (RefreshToken). Owns every "is this operation allowed?"
 * decision for the refresh session artifact — usability (not revoked, not expired) and the
 * RULE-SEC-006 rotate-on-refresh decision. No Spring/JPA annotations, no repository access;
 * constructed only via the static factories. Token-value hashing is the service's concern —
 * this class reasons only about the flags and expiry.
 */
public final class RefreshTokenDomain {

    // Client-bindable session TTL defaults (business-policies-SEC ⚠Client, RULE-SEC-006).
    public static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15); // policy default
    public static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);    // policy default

    private final boolean revoked;
    private final LocalDateTime expiresAt;

    private RefreshTokenDomain(boolean revoked, LocalDateTime expiresAt) {
        this.revoked = revoked;
        this.expiresAt = expiresAt;
    }

    /**
     * Issue-time factory: computes the expiry from the refresh TTL policy default. The hashed
     * token value itself is set on the entity by the service (RULE-SEC-004).
     */
    public static RefreshTokenDomain issue(LocalDateTime now) {
        return new RefreshTokenDomain(false, now.plus(REFRESH_TOKEN_TTL));
    }

    /** Reconstructs a Domain view over a persisted entity — no validation. */
    public static RefreshTokenDomain from(RefreshToken entity) {
        return new RefreshTokenDomain(
            Boolean.TRUE.equals(entity.getRevoked()),
            entity.getExpiresAt());
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt == null || !now.isBefore(expiresAt);
    }

    public boolean isUsable(LocalDateTime now) {
        return !revoked && !isExpired(now);
    }

    /**
     * RULE-SEC-006 — a refresh token may drive a rotation only while usable (present, not
     * revoked, not expired). The service revokes this token (entity.revoke()) and issues a
     * fresh one after this returns.
     */
    public void assertCanRotate(LocalDateTime now) {
        if (revoked) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.REFRESH_TOKEN_REVOKED);
        }
        if (isExpired(now)) {
            throw new LocalizedException(Status.BUSINESS_RULE_VIOLATION, SecErrorCodes.REFRESH_TOKEN_EXPIRED);
        }
    }

    public boolean isRevoked() {
        return revoked;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
