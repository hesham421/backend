package com.example.security.scheduler;

import com.example.security.config.properties.RefreshTokenCleanupProperties;
import com.example.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Scheduled cleanup of stale REFRESH_TOKENS rows.
 *
 * Each run removes:
 * 1) Expired tokens (EXPIRES_AT in the past) — regardless of revoked status.
 * 2) Revoked tokens older than the configured retention window, using
 *    CREATED_AT as the age reference (REFRESH_TOKENS has no separate
 *    "revoked at" timestamp).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenCleanupProperties properties;

    @Scheduled(cron = "${erp.security.token-cleanup.cron:0 0 3 * * *}")
    public void cleanup() {
        Instant now = Instant.now();

        long expiredDeleted = refreshTokenRepository.deleteByExpiresAtBefore(now);

        Instant revokedCutoff = now.minus(properties.revokedRetentionDays(), ChronoUnit.DAYS);
        long revokedDeleted = refreshTokenRepository.deleteByRevokedTrueAndCreatedAtBefore(revokedCutoff);

        log.info("Refresh token cleanup: removed {} expired, {} revoked (older than {} days)",
                expiredDeleted, revokedDeleted, properties.revokedRetentionDays());
    }
}
