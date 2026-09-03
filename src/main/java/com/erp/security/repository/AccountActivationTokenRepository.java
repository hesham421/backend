package com.erp.security.repository;

import com.erp.security.entity.AccountActivationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for ENTITY-SEC-007 (AccountActivationToken). Lookup is by the SHA-256 hash of the
 * presented raw token (RULE-SEC-004 / DRV-005). Single-use consumption is decided by
 * AccountActivationTokenDomain and applied via the entity's {@code markUsed()} helper.
 */
@Repository
public interface AccountActivationTokenRepository
    extends JpaRepository<AccountActivationToken, Long>,
            JpaSpecificationExecutor<AccountActivationToken> {

    Optional<AccountActivationToken> findByToken(String token);
}
