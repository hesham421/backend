package com.example.security.repository;

import com.example.security.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PASSWORD_RESET_TOKEN (ENTITY-SEC-011).
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    // RULE-SEC-039 — invalidate any prior unexpired token for the same user when issuing a new one
    List<PasswordResetToken> findByUser_IdAndUsedFlFalseAndExpiresAtAfter(Long userId, Instant now);
}
