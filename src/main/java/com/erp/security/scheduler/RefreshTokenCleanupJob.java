package com.erp.security.scheduler;

import com.erp.security.config.properties.RefreshTokenCleanupProperties;
import com.erp.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Removes expired tokens plus revoked tokens past the retention window, aged off CREATED_AT
 * since REFRESH_TOKENS has no separate "revoked at" timestamp.
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
