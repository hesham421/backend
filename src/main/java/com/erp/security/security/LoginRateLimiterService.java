package com.erp.security.security;

import com.erp.security.config.properties.LoginRateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.max;

/**
 * In-memory login-attempt rate limiter, keyed by "ip|username". Not shared across instances —
 * fine for the current single-instance deployment, but must move to Redis if ever scaled out.
 */
@Component
@RequiredArgsConstructor
public class LoginRateLimiterService {

    private final LoginRateLimitProperties properties;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> lockedUntil = new ConcurrentHashMap<>();

    /** @return false if the key just exceeded maxAttempts or is still serving a prior lockout. */
    public boolean tryConsume(String key) {
        Instant blockedUntil = lockedUntil.get(key);
        if (blockedUntil != null) {
            if (Instant.now().isBefore(blockedUntil)) {
                return false;
            }
            lockedUntil.remove(key, blockedUntil);
        }

        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());
        if (bucket.tryConsume(1)) {
            return true;
        }

        lockedUntil.put(key, Instant.now().plusSeconds(properties.lockoutSeconds()));
        return false;
    }

    /**
     * Seconds remaining until {@code key} is unblocked, for a {@code Retry-After} response
     * header. Returns 0 if the key is not currently blocked.
     */
    public long secondsUntilUnblocked(String key) {
        Instant until = lockedUntil.get(key);
        if (until == null) {
            return 0;
        }
        return max(0, Duration.between(Instant.now(), until).getSeconds());
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(properties.maxAttempts())
                .refillGreedy(properties.maxAttempts(), Duration.ofSeconds(properties.windowSeconds()))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
