package com.example.security.client;

import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.example.erp.common.web.ApiResponse;
import com.example.security.exception.SecurityErrorCodes;
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
 * Consumes ORG_BRANCH over its REST API (XM-SEC-001, execution-plan-SEC-gaps.md Section 6.1) —
 * erp-security has no Maven dependency on erp-org, so this is a same-JVM HTTP self-call to
 * {@code GET /api/v1/org/branches/{id}} (API-ORG-012) rather than a shared JPA object graph.
 *
 * The target endpoint is authenticated + permission-gated (BRANCH_VIEW); since there is no
 * service-to-service credential anywhere in this codebase yet, the caller's own incoming
 * {@code Authorization} header is forwarded as-is (DRV-SEC-005-style agent decision — flagged
 * in the Phase 3 handoff, not specified by execution-plan-SEC-gaps.md).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrgBranchClient {

    private final RestTemplate internalApiRestTemplate;

    @Value("${server.port:7272}")
    private int serverPort;

    /**
     * RULE-SEC-034 — reject if the referenced ORG_BRANCH does not exist or is not active.
     * Throws LocalizedException(ERR-SEC-1034) in both cases, per execution-plan-SEC-gaps.md
     * Section 3 (SEC_USER_PROFILE).
     */
    public void assertActiveBranch(Long branchId) {
        String url = "http://localhost:" + serverPort + "/api/v1/org/branches/" + branchId;
        HttpEntity<Void> entity = new HttpEntity<>(forwardedAuthHeaders());

        OrgBranchLookup branch;
        try {
            ResponseEntity<ApiResponse<OrgBranchLookup>> response = internalApiRestTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
            branch = response.getBody() != null ? response.getBody().getData() : null;
        } catch (HttpClientErrorException ex) {
            // Any 4xx from the internal lookup (404 branch missing, 403 caller lacks
            // BRANCH_VIEW, etc.) means this module cannot positively confirm the branch
            // is active — treat all of them as "not usable" rather than letting the
            // underlying HTTP error leak as a 500. See OrgBranchClient class javadoc for
            // the known BRANCH_VIEW cross-module permission gap that can cause the 403 case.
            log.warn("Branch lookup for id {} failed with {} — treating as inactive/unresolvable", branchId, ex.getStatusCode());
            branch = null;
        }

        if (branch == null || !Boolean.TRUE.equals(branch.isActive())) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.SEC_USER_PROFILE_BRANCH_INACTIVE, branchId);
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
}
