package com.erp.security.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

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
