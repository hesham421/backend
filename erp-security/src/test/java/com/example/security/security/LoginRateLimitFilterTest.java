package com.example.security.security;

import com.example.erp.common.i18n.LocalizationService;
import com.example.security.config.properties.LoginRateLimitProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

/**
 * GAP: registry-security.md §8.6 — no rate limiting on /api/auth/login.
 */
@ExtendWith(MockitoExtension.class)
class LoginRateLimitFilterTest {

    @Mock
    private LocalizationService localizationService;

    private LoginRateLimitFilter filter;
    // findAndRegisterModules() picks up jackson-datatype-jsr310 (Instant support),
    // matching the Spring Boot autoconfigured ObjectMapper bean used at runtime.
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        lenient().when(localizationService.getMessage(anyString(), any(Locale.class)))
                .thenReturn("Too many attempts. Please try again later");

        LoginRateLimitProperties properties = new LoginRateLimitProperties(1, 60, 300);
        LoginRateLimiterService rateLimiterService = new LoginRateLimiterService(properties);
        filter = new LoginRateLimitFilter(rateLimiterService, localizationService, objectMapper);
    }

    private MockHttpServletRequest loginRequest(String ip, String username) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr(ip);
        String body = "{\"username\":\"" + username + "\",\"password\":\"secret\"}";
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        request.setContentType("application/json");
        return request;
    }

    @Test
    void firstAttempt_isAllowedThrough_andBodyRemainsReadableDownstream() throws Exception {
        MockHttpServletRequest request = loginRequest("127.0.0.1", "alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        String downstreamBody = new String(
                ((jakarta.servlet.http.HttpServletRequest) chain.getRequest()).getInputStream().readAllBytes(),
                StandardCharsets.UTF_8);
        assertThat(downstreamBody).contains("\"username\":\"alice\"");
        assertThat(response.getStatus()).isNotEqualTo(429);
    }

    @Test
    void secondAttempt_withinWindow_forSameIpAndUsername_isBlockedWith429() throws Exception {
        // maxAttempts = 1, so the 2nd attempt for the same key must be blocked.
        filter.doFilter(loginRequest("127.0.0.1", "alice"), new MockHttpServletResponse(), new MockFilterChain());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(loginRequest("127.0.0.1", "alice"), response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).contains("RATE_LIMIT_LOGIN_EXCEEDED");
        int retryAfter = Integer.parseInt(response.getHeader("Retry-After"));
        assertThat(retryAfter).isBetween(295, 300);
        assertThat(chain.getRequest()).as("downstream chain must not be invoked once blocked").isNull();
    }

    @Test
    void differentUsername_sameIp_isNotThrottledByOtherKeysAttempts() throws Exception {
        filter.doFilter(loginRequest("127.0.0.1", "alice"), new MockHttpServletResponse(), new MockFilterChain());

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(loginRequest("127.0.0.1", "bob"), response, chain);

        assertThat(response.getStatus()).isNotEqualTo(429);
        assertThat(chain.getRequest()).isNotNull();
    }
}
