package com.erp.security.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "erp.security.cookie")
public record CookieProperties(
    
    /**
     * Leave blank for a host-only cookie (no Domain attribute) — required for single-label
     * hosts like "localhost", since RFC-2965 cookie jars reject a dotless Domain value.
     */
    String domain,
    
    /**
     * Cookie path.
     * Default: /
     */
    String path,
    
    /**
     * Whether cookie requires HTTPS.
     * MUST be true in production!
     * Default: false (for development)
     */
    boolean secure,
    
    /**
     * Whether cookie is HTTP-only (not accessible via JavaScript).
     * Should always be true for security tokens.
     * Default: true
     */
    boolean httpOnly,
    
    /**
     * SameSite attribute for CSRF protection.
     * Options: Strict, Lax, None
     * Default: Lax
     */
    String sameSite
) {
    
    /**
     * Default constructor with sensible defaults.
     */
    public CookieProperties {
        if (path == null || path.isBlank()) {
            path = "/";
        }
        if (sameSite == null || sameSite.isBlank()) {
            sameSite = "Lax";
        }
    }
}
