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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * Consumes the MasterData Lookup service over its generic consumption endpoint
 * (GET /api/lookups/{lookupCode}, master-registry.md Section 8 "LOOKUP CONSUMPTION RULES")
 * to validate DATA_ACCESS_LEVEL (LOV-SEC-002) per execution-plan-SEC-gaps.md Section 3 —
 * erp-security has no Maven dependency on erp-masterdata, so this is a same-JVM HTTP
 * self-call, mirroring {@link OrgBranchClient}'s pattern (including forwarding the caller's
 * own Authorization header — no service-to-service credential exists in this codebase yet).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MasterDataLookupClient {

    private static final String DATA_ACCESS_LEVEL_LOOKUP_CODE = "DATA_ACCESS_LEVEL";

    private final RestTemplate internalApiRestTemplate;

    @Value("${server.port:7272}")
    private int serverPort;

    /**
     * RULE-SEC-035 — dataAccessLevel must be one of the active LOV-SEC-002 codes
     * (BRANCH_ONLY / BRANCH_AND_CHILDREN / ALL). Throws LocalizedException(ERR-SEC-1035)
     * if not — the plan binds exactly one ERR-ID to RULE-SEC-035, covering both the
     * "missing" and "not a valid LOV code" scenarios.
     */
    public void assertValidDataAccessLevel(String dataAccessLevel) {
        String url = "http://localhost:" + serverPort + "/api/lookups/" + DATA_ACCESS_LEVEL_LOOKUP_CODE;
        HttpEntity<Void> entity = new HttpEntity<>(forwardedAuthHeaders());

        ResponseEntity<ApiResponse<List<LookupValueLookup>>> response = internalApiRestTemplate.exchange(
                url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
        List<LookupValueLookup> values = response.getBody() != null ? response.getBody().getData() : null;

        boolean valid = values != null && values.stream()
                .anyMatch(v -> v.code() != null && v.code().equalsIgnoreCase(dataAccessLevel));

        if (!valid) {
            throw new LocalizedException(Status.BAD_REQUEST, SecurityErrorCodes.SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED, dataAccessLevel);
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
