package com.example.security.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Refresh Token Cleanup Configuration Properties.
 *
 * Bound to properties with prefix: erp.security.token-cleanup
 *
 * Example in application.properties:
 * erp.security.token-cleanup.cron=0 0 3 * * *
 * erp.security.token-cleanup.revoked-retention-days=30
 *
 * @author ERP Team
 */
@Validated
@ConfigurationProperties(prefix = "erp.security.token-cleanup")
public record RefreshTokenCleanupProperties(

    /**
     * Cron expression for the scheduled cleanup job.
     * Removes expired refresh tokens, and revoked refresh tokens older
     * than revokedRetentionDays, in the same run.
     * Default: daily at 03:00
     */
    @NotBlank(message = "Token cleanup cron expression is required")
    String cron,

    /**
     * Number of days a revoked refresh token is retained before deletion.
     * Default: 30
     */
    @Min(value = 1, message = "Revoked token retention must be at least 1 day")
    long revokedRetentionDays
) {

    /**
     * Default constructor with sensible defaults.
     */
    public RefreshTokenCleanupProperties {
        if (cron == null || cron.isBlank()) {
            cron = "0 0 3 * * *";
        }
        if (revokedRetentionDays <= 0) {
            revokedRetentionDays = 30;
        }
    }
}
