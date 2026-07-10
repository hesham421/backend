package com.example.security.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Self-service token expiration configuration (PASSWORD_RESET_TOKEN / ACCOUNT_ACTIVATION_TOKEN).
 *
 * Bound to properties with prefix: erp.security.self-service-token
 *
 * TTL values are an agent implementation detail (not mandated by execution-plan-SEC-gaps.md
 * Section 3, which leaves token generation/lifetime to the implementer) — defaults follow
 * common industry practice: 24h for account activation, 1h for password reset.
 *
 * @author ERP Team
 */
@Validated
@ConfigurationProperties(prefix = "erp.security.self-service-token")
public record SelfServiceTokenProperties(

        /**
         * Account activation token TTL in seconds. Default: 86400 (24 hours).
         */
        long activationExpirationSeconds,

        /**
         * Password reset token TTL in seconds. Default: 3600 (1 hour).
         */
        long resetExpirationSeconds
) {
    public SelfServiceTokenProperties {
        if (activationExpirationSeconds <= 0) {
            activationExpirationSeconds = 86400;
        }
        if (resetExpirationSeconds <= 0) {
            resetExpirationSeconds = 3600;
        }
    }
}
