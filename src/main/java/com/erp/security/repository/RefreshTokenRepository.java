package com.erp.security.repository;

import com.erp.security.entity.RefreshToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-SEC-005 (RefreshToken). Lookup is by the SHA-256 hash of the presented
 * raw token (RULE-SEC-004 / DRV-005) — the raw value is never stored. Rotation and revocation
 * are decided by RefreshTokenDomain and applied via the entity's own {@code revoke()} helper.
 */
@Repository
public interface RefreshTokenRepository
    extends JpaRepository<RefreshToken, Long>,
            JpaSpecificationExecutor<RefreshToken> {

    Optional<RefreshToken> findByToken(String token);

    /**
     * All still-live (not-yet-revoked) refresh tokens for a user — used to revoke every existing
     * session when the password changes (RULE-SEC-007), so a session opened before the reset cannot
     * outlive it.
     */
    List<RefreshToken> findByUserAccount_IdAndRevokedFalse(Long userId);
}
