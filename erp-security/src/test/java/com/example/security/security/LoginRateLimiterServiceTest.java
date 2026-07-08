package com.example.security.security;

import com.example.security.config.properties.LoginRateLimitProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GAP: registry-security.md §8.6 — no rate limiting on /api/auth/login.
 */
class LoginRateLimiterServiceTest {

    private final LoginRateLimitProperties properties = new LoginRateLimitProperties(5, 60, 300);
    private final LoginRateLimiterService service = new LoginRateLimiterService(properties);

    @Test
    void sixthAttemptWithinWindow_forSameKey_isBlocked() {
        String key = "127.0.0.1|alice";

        for (int i = 0; i < 5; i++) {
            assertThat(service.tryConsume(key)).as("attempt #%d should be allowed", i + 1).isTrue();
        }

        assertThat(service.tryConsume(key)).as("6th attempt should be blocked").isFalse();
        // Still blocked on a subsequent call, since it is now serving out the lockout.
        assertThat(service.tryConsume(key)).isFalse();
    }

    @Test
    void differentIpUsernamePairs_areNotCrossThrottled() {
        String keyA = "127.0.0.1|alice";
        String keyB = "127.0.0.1|bob";
        String keyC = "10.0.0.5|alice";

        for (int i = 0; i < 5; i++) {
            assertThat(service.tryConsume(keyA)).isTrue();
        }
        assertThat(service.tryConsume(keyA)).as("keyA should now be blocked").isFalse();

        // Different username, same IP - independent bucket.
        assertThat(service.tryConsume(keyB)).as("keyB (different username) unaffected").isTrue();
        // Same username, different IP - independent bucket.
        assertThat(service.tryConsume(keyC)).as("keyC (different IP) unaffected").isTrue();
    }
}
