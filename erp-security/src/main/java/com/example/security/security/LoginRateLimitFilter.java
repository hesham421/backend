package com.example.security.security;

import com.example.erp.common.i18n.LocalizationService;
import com.example.erp.common.web.ApiError;
import com.example.erp.common.web.ApiResponse;
import com.example.security.exception.SecurityErrorCodes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * Brute-force protection for the login endpoint(s).
 *
 * Rate-limit key is IP + username (from the request body), not IP alone, so a
 * single noisy IP cannot lock out every account and a single targeted account
 * cannot be hammered from many rotating IPs without also tripping the per-IP
 * component of the key.
 *
 * Generic by design (matches on {@link #PROTECTED_PATHS}) so it also covers
 * POST /api/auth/signup once that endpoint exists.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> PROTECTED_PATHS = Set.of("/api/auth/login", "/api/auth/signup");

    private final LoginRateLimiterService rateLimiterService;
    private final LocalizationService localizationService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && PROTECTED_PATHS.contains(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String username = extractUsername(cachedRequest);
        String ip = extractClientIp(cachedRequest);
        String key = ip + "|" + (username == null ? "" : username.toLowerCase(Locale.ROOT));

        if (!rateLimiterService.tryConsume(key)) {
            log.warn("Blocked login attempt (rate limit exceeded) — ip={}, username={}, path={}",
                    ip, username, request.getRequestURI());
            writeTooManyRequests(request, response, rateLimiterService.secondsUntilUnblocked(key));
            return;
        }

        filterChain.doFilter(cachedRequest, response);
    }

    private String extractUsername(CachedBodyHttpServletRequest request) {
        try {
            byte[] body = request.getCachedBody();
            if (body.length == 0) {
                return null;
            }
            JsonNode node = objectMapper.readTree(body);
            JsonNode usernameNode = node.get("username");
            return usernameNode != null ? usernameNode.asText(null) : null;
        } catch (IOException ex) {
            log.debug("Could not parse login request body for rate limiting", ex);
            return null;
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletRequest request, HttpServletResponse response, long retryAfterSeconds)
            throws IOException {
        Locale locale = resolveLocale(request);
        String message = localizationService.getMessage(SecurityErrorCodes.RATE_LIMIT_LOGIN_EXCEEDED, locale);

        ApiError error = new ApiError(SecurityErrorCodes.RATE_LIMIT_LOGIN_EXCEEDED, message);
        error.setPath(request.getRequestURI());
        ApiResponse<Void> body = ApiResponse.fail(message, error);

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        if (retryAfterSeconds > 0) {
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private Locale resolveLocale(HttpServletRequest request) {
        String header = request.getHeader("Accept-Language");
        if (header != null && header.toLowerCase(Locale.ROOT).startsWith("ar")) {
            return Locale.forLanguageTag("ar");
        }
        return Locale.ENGLISH;
    }
}
