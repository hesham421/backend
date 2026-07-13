package com.example.erp.notification.client;

import com.example.erp.common.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves the current authenticated username (the only identity {@code SecurityContextHelper}
 * exposes — there is no numeric-user-id accessor anywhere in erp-common-utils/erp-security's
 * JWT principal, only the username string) to Security's numeric {@code USERS_PK}, needed by
 * API-NOTIF-003's "recipientId defaults to caller's own id" rule. Same same-JVM HTTP self-call
 * pattern as {@link SecUserProfileClient} — erp-notification has no Maven dependency on
 * erp-security.
 *
 * <p>Calls {@code POST /api/users/search} (the platform's standard {@code
 * BaseSearchContractRequest} search contract) filtering by {@code username EQUALS}, since no
 * dedicated "get by username" or "/me" endpoint exists in erp-security today.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUserClient {

    private final RestTemplate internalApiRestTemplate;

    @Value("${server.port:7272}")
    private int serverPort;

    public Optional<Long> resolveUserIdByUsername(String username) {
        String url = "http://localhost:" + serverPort + "/api/users/search";

        Map<String, Object> filter = Map.of("field", "username", "operator", "EQUALS", "value", username);
        Map<String, Object> body = Map.of("filters", List.of(filter), "page", 0, "size", 1);

        HttpHeaders headers = forwardedAuthHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<ApiResponse<PageContent>> response = internalApiRestTemplate.exchange(
                    url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});
            PageContent page = response.getBody() != null ? response.getBody().getData() : null;
            if (page == null || page.content() == null || page.content().isEmpty()) {
                return Optional.empty();
            }
            return Optional.ofNullable(page.content().get(0).id());
        } catch (HttpClientErrorException ex) {
            log.warn("User lookup for username '{}' failed with {}", username, ex.getStatusCode());
            return Optional.empty();
        }
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

    /** Minimal local view of Spring Data's {@code Page<T>} JSON shape — only {@code content} is needed. */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private record PageContent(List<UserLookup> content) {
    }
}
