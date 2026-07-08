package com.example.security.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Login Rate Limit Configuration Properties.
 *
 * Bound to properties with prefix: erp.security.rate-limit.login
 *
 * Example in application.properties:
 * erp.security.rate-limit.login.max-attempts=5
 * erp.security.rate-limit.login.window-seconds=60
 * erp.security.rate-limit.login.lockout-seconds=300
 *
 * @author ERP Team
 */
@Validated
@ConfigurationProperties(prefix = "erp.security.rate-limit.login")
public record LoginRateLimitProperties(

    /**
     * Maximum login attempts allowed per IP+username within windowSeconds.
     * Default: 5
     */
    int maxAttempts,

    /**
     * Sliding window (in seconds) over which attempts are counted.
     * Default: 60
     */
    long windowSeconds,

    /**
     * How long (in seconds) an IP+username is blocked once maxAttempts is exceeded.
     * Default: 300
     */
    long lockoutSeconds
) {

    /**
     * Default constructor with sensible defaults.
     */
    public LoginRateLimitProperties {
        if (maxAttempts <= 0) {
            maxAttempts = 5;
        }
        if (windowSeconds <= 0) {
            windowSeconds = 60;
        }
        if (lockoutSeconds <= 0) {
            lockoutSeconds = 300;
        }
    }
}
