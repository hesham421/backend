package com.example.erp.notification.client;

import com.example.erp.common.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Resolves the recipient's language preference from Security for RULE-NOTIF-006 — Security is
 * a live, non-deferred PERMANENT EXCEPTION dependency here (SRS A7 CONTRACT-7 note, not an
 * XM-ID). erp-notification has no Maven dependency on erp-security, so this is a same-JVM HTTP
 * self-call to {@code GET /api/v1/security/user-profiles/{userId}}, mirroring
 * {@code com.example.security.client.OrgBranchClient}'s identical pattern (including forwarding
 * the caller's own incoming Authorization header — no service-to-service credential exists in
 * this codebase yet).
 *
 * <p>Same known coupling as OrgBranchClient's BRANCH_VIEW gap (see
 * governance-repo memory: plan_sec_002_phase3_state): the target endpoint may be
 * permission-gated in a way the calling principal does not hold. Rather than fail the send —
 * which RULE-NOTIF-006 explicitly forbids doing for a missing template, and this is the same
 * spirit for a missing language signal — any resolution failure (404 user has no profile, 403
 * caller lacks the view permission, profile has no preferredLang set) falls back to
 * {@link #DEFAULT_LANGUAGE} rather than blocking the notification.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecUserProfileClient {

    public static final String DEFAULT_LANGUAGE = "EN";

    private final RestTemplate internalApiRestTemplate;

    @Value("${server.port:7272}")
    private int serverPort;

    public String resolvePreferredLanguage(Long recipientId) {
        String url = "http://localhost:" + serverPort + "/api/v1/security/user-profiles/" + recipientId;
        HttpEntity<Void> entity = new HttpEntity<>(forwardedAuthHeaders());

        SecUserProfileLookup profile;
        try {
            ResponseEntity<ApiResponse<SecUserProfileLookup>> response = internalApiRestTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
            profile = response.getBody() != null ? response.getBody().getData() : null;
        } catch (HttpClientErrorException ex) {
            log.warn("User profile lookup for recipient {} failed with {} — falling back to {}",
                    recipientId, ex.getStatusCode(), DEFAULT_LANGUAGE);
            profile = null;
        }

        if (profile == null || profile.preferredLang() == null || profile.preferredLang().isBlank()) {
            return DEFAULT_LANGUAGE;
        }
        return profile.preferredLang();
    }

    private HttpHeaders forwardedAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            String authorization = servletAttrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null) {
                headers.set(HttpHeaders.AUTHORIZATION, authorization);
            }
        }
        return headers;
    }
}
