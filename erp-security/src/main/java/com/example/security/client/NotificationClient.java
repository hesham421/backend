package com.example.security.client;

import com.example.security.repository.UserAccountRepository;
import com.example.security.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Dispatches account-activation and password-reset notifications via erp-notification's
 * POST /api/v1/notifications/send (API-NOTIF-001) — erp-security has no Maven dependency on
 * erp-notification, so this is a same-JVM HTTP self-call, mirroring {@link OrgBranchClient}'s
 * pattern.
 *
 * <p>XM-SEC-005 — {@code /send} requires an authenticated JWT
 * ({@code @PreAuthorize("isAuthenticated()")} on {@code NotificationEventProcessor.send()}),
 * but this client is called from an AFTER_COMMIT listener reacting to anonymous,
 * unauthenticated flows (self-registration, forgot-password) — there is no caller JWT to
 * forward. Resolved via a dedicated, roleless service account ({@code svc-notification}, seeded
 * by {@code db/scripts/005_notification_service_account_seed.sql}): this client mints a real
 * JWT for that account via {@link JwtService#generateAccess(String, List, Long)} — the exact
 * same code path {@code AuthService} uses for real logins — and sends it as a normal Bearer
 * token. No {@code SecurityConfig}/{@code JwtAuthenticationFilter} changes needed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClient {

    private static final String MODULE_CODE = "SECURITY";
    private static final String SERVICE_USERNAME = "svc-notification";

    private final RestTemplate internalApiRestTemplate;
    private final JwtService jwtService;
    private final UserAccountRepository userAccountRepository;

    @Value("${server.port:7272}")
    private int serverPort;

    private volatile Long serviceAccountUserId;

    public void sendAccountActivation(Long userId, String token, Instant expiresAt) {
        send(userId, "ACCOUNT_ACTIVATION_REQUESTED", Map.of(
                "token", token,
                "expiresAt", expiresAt.toString()
        ));
    }

    public void sendPasswordReset(Long userId, Map<String, Object> contextData) {
        send(userId, "PASSWORD_RESET_REQUESTED", contextData);
    }

    private void send(Long recipientId, String templateCode, Map<String, Object> contextData) {
        String bearerToken;
        try {
            bearerToken = mintServiceToken();
        } catch (RuntimeException ex) {
            // Service account not seeded yet (005_notification_service_account_seed.sql not
            // applied) or another lookup failure — best-effort, see class javadoc.
            log.warn("Could not mint service token for notification dispatch (recipient={}, templateCode={}): {}",
                    recipientId, templateCode, ex.getMessage());
            return;
        }

        String url = "http://localhost:" + serverPort + "/api/v1/notifications/send";

        Map<String, Object> body = Map.of(
                "recipientId", recipientId,
                "channelHint", List.of("EMAIL"),
                "templateCode", templateCode,
                "contextData", contextData,
                "priority", "HIGH",
                "moduleCode", MODULE_CODE
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(bearerToken);

        try {
            internalApiRestTemplate.postForEntity(url, new HttpEntity<>(body, headers), Void.class);
        } catch (RestClientException ex) {
            // Best-effort — never fails the already-committed signup/reset flow that
            // triggered this; matches erp-notification's own "never fails the send"
            // fallback philosophy (NotificationEventProcessor DRV-NOTIF-002).
            log.warn("Notification dispatch failed for recipient={} templateCode={}: {}",
                    recipientId, templateCode, ex.getMessage());
        }
    }

    private String mintServiceToken() {
        Long userId = serviceAccountUserId;
        if (userId == null) {
            userId = userAccountRepository.findByUsernameIgnoreCase(SERVICE_USERNAME)
                    .orElseThrow(() -> new IllegalStateException(
                            "Service account '" + SERVICE_USERNAME + "' not found — has "
                                    + "005_notification_service_account_seed.sql been applied?"))
                    .getId();
            serviceAccountUserId = userId;
        }
        return jwtService.generateAccess(SERVICE_USERNAME, List.of(), userId);
    }
}
