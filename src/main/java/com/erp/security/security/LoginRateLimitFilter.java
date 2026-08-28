package com.erp.security.security;

import com.erp.common.i18n.LocalizationService;
import com.erp.common.web.ApiError;
import com.erp.common.web.ApiResponse;
import com.erp.security.exception.SecurityErrorCodes;
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
import java.util.Map;

/**
 * Brute-force protection keyed on IP + an identifier field from the request body (not IP
 * alone), so neither a single noisy IP nor rotating IPs targeting one account can bypass it.
 * reset-password has no email field, so it deliberately keys on the reset token instead.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final Map<String, String> PROTECTED_PATH_IDENTIFIER_FIELD = Map.of(
            "/api/auth/login", "username",
            "/api/auth/signup", "username",
            "/api/auth/forgot-password", "email",
            "/api/auth/reset-password", "token"
    );

    private final LoginRateLimiterService rateLimiterService;
    private final LocalizationService localizationService;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && PROTECTED_PATH_IDENTIFIER_FIELD.containsKey(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String identifierField = PROTECTED_PATH_IDENTIFIER_FIELD.get(request.getRequestURI());
        String identifier = extractField(cachedRequest, identifierField);
        String ip = extractClientIp(cachedRequest);
        String key = ip + "|" + (identifier == null ? "" : identifier.toLowerCase(Locale.ROOT));

        if (!rateLimiterService.tryConsume(key)) {
            log.warn("Blocked attempt (rate limit exceeded) — ip={}, {}={}, path={}",
                    ip, identifierField, identifier, request.getRequestURI());
            writeTooManyRequests(request, response, rateLimiterService.secondsUntilUnblocked(key));
            return;
        }

        filterChain.doFilter(cachedRequest, response);
    }

    private String extractField(CachedBodyHttpServletRequest request, String fieldName) {
        try {
            byte[] body = request.getCachedBody();
            if (body.length == 0) {
                return null;
            }
            JsonNode node = objectMapper.readTree(body);
            JsonNode fieldNode = node.get(fieldName);
            return fieldNode != null ? fieldNode.asText(null) : null;
        } catch (IOException ex) {
            log.debug("Could not parse request body for rate limiting", ex);
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
