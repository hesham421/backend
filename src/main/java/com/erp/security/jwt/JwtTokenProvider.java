package com.erp.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Issues and verifies the single internal platform access token (SSO, auth-only — CORE.md).
 * HS256 over the shared secret; subject = username, plus userId and the caller's granted
 * permission codes as authorities. Refresh/reset/activation opaque tokens are NOT JWTs — they
 * are random strings hashed at rest by AuthService (RULE-SEC-004 / DRV-005), never minted here.
 */
@Component
public class JwtTokenProvider {

    static final String CLAIM_USER_ID = "userId";
    static final String CLAIM_AUTHORITIES = "authorities";

    private static final int OPAQUE_TOKEN_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SecretKey signingKey;
    private final long accessExpirationMs;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.expiration-ms}") long accessExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
    }

    public String generateAccessToken(String username, Long userId, Collection<String> authorities) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(username)
            .claim(CLAIM_USER_ID, userId)
            .claim(CLAIM_AUTHORITIES, new ArrayList<>(authorities))
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(accessExpirationMs)))
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();
    }

    /** Verifies signature and expiry, returning the parsed token; throws {@code JwtException} on failure. */
    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
    }

    /** TTL of the access token in seconds — the {@code expiresIn} carried back in TokenResponse. */
    public long getAccessTokenTtlSeconds() {
        return accessExpirationMs / 1000;
    }

    /** A 256-bit cryptographically random opaque token (raw value returned to the caller, hashed at rest). */
    public String generateOpaqueToken() {
        byte[] bytes = new byte[OPAQUE_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
