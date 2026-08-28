package com.erp.security.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Self-service token expiration config. TTL defaults (24h activation, 1h reset) are an
 * implementation choice, not mandated by execution-plan-SEC-gaps.md Section 3.
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
