package com.example.security.security;

import com.example.security.config.properties.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT Token Service.
 *
 * Generates and validates JWT access and refresh tokens.
 * Configuration is loaded from {@link JwtProperties}.
 *
 * @author ERP Team
 */
@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessExpSeconds;
    private final long refreshExpSeconds;

    public JwtService(JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(
            jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
        this.accessExpSeconds = jwtProperties.accessExpirationSeconds();
        this.refreshExpSeconds = jwtProperties.refreshExpirationSeconds();
    }

    public String generateAccess(String username, List<String> authorities, Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claims(Map.of(
                    "authorities", authorities,
                    "userId", userId
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessExpSeconds)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefresh(String username, String jti) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshExpSeconds)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
    }

    public String extractUsername(String token) { return parse(token).getPayload().getSubject(); }

    public String extractJti(String token) { return parse(token).getPayload().getId(); }

    public Long extractUserId(String token) {
        Object v = parse(token).getPayload().get("userId");
        return v == null ? null : ((Number) v).longValue();
    }

    /**
     * Get access token expiration in seconds.
     */
    public long getAccessExpirationSeconds() {
        return accessExpSeconds;
    }

    /**
     * Get refresh token expiration in seconds.
     */
    public long getRefreshExpirationSeconds() {
        return refreshExpSeconds;
    }
}
