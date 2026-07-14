package com.example.erp.file.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Encrypted Upload/Download/Delete Token configuration (ARCH-REF-1.10 AD-FILE-02/03).
 *
 * Bound to properties with prefix: erp.file.token
 *
 * Example in application.properties:
 * erp.file.token.secret=${FILE_TOKEN_SECRET}
 * erp.file.token.ttl-minutes=100
 */
@Validated
@ConfigurationProperties(prefix = "erp.file.token")
public record FileTokenProperties(

    /**
     * Secret used to derive the AES-256 key (via SHA-256) for token encryption.
     * In production, use environment variable: ${FILE_TOKEN_SECRET}
     */
    @NotBlank(message = "File token secret is required")
    String secret,

    /**
     * Token time-to-live in minutes (RULE-FILE-002). Default: 100.
     */
    @Min(value = 1, message = "Token TTL must be at least 1 minute")
    long ttlMinutes
) {

    public FileTokenProperties {
        if (ttlMinutes <= 0) {
            ttlMinutes = 100;
        }
    }
}
