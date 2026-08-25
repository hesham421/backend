package com.erp.security.repository;

import com.erp.security.entity.AccountActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for ACCOUNT_ACTIVATION_TOKEN (ENTITY-SEC-012).
 */
@Repository
public interface AccountActivationTokenRepository extends JpaRepository<AccountActivationToken, Long> {

    Optional<AccountActivationToken> findByToken(String token);
}
