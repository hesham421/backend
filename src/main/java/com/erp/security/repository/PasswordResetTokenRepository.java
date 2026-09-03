package com.erp.security.repository;

import com.erp.security.entity.PasswordResetToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-SEC-006 (PasswordResetToken). Lookup is by the SHA-256 hash of the
 * presented raw token (RULE-SEC-004 / DRV-005). {@code findActiveByUserId} backs the "single
 * active" half of RULE-SEC-007 — the service marks any prior live tokens used before issuing a new one.
 */
@Repository
public interface PasswordResetTokenRepository
    extends JpaRepository<PasswordResetToken, Long>,
            JpaSpecificationExecutor<PasswordResetToken> {

    Optional<PasswordResetToken> findByToken(String token);

    @Query("SELECT t FROM PasswordResetToken t WHERE t.userAccount.id = :userId AND t.used = false")
    List<PasswordResetToken> findActiveByUserId(@Param("userId") Long userId);
}
