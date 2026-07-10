package com.example.security.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Cookie Configuration Properties.
 * 
 * Bound to properties with prefix: erp.security.cookie
 * 
 * Example in application.properties:
 * erp.security.cookie.domain=localhost
 * erp.security.cookie.path=/
 * erp.security.cookie.secure=false
 * erp.security.cookie.http-only=true
 * erp.security.cookie.same-site=Lax
 * 
 * @author ERP Team
 */
@Validated
@ConfigurationProperties(prefix = "erp.security.cookie")
public record CookieProperties(
    
    /**
     * Cookie domain. Leave blank/unset for a host-only cookie (no Domain
     * attribute) — required for single-label hosts like "localhost": browsers
     * accept an explicit Domain=localhost, but RFC-2965 cookie jars (Python's
     * requests/http.cookiejar, Java's CookieManager) reject storing it back
     * because the value has no embedded dot, so the cookie never gets resent.
     * Default: none (host-only)
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
