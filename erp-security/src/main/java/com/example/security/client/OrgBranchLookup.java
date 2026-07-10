package com.example.security.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Minimal local view of erp-org's {@code BranchResponse} (GET /api/v1/org/branches/{id},
 * API-ORG-012). erp-security has no Maven dependency on erp-org (XM-SEC-001), so only the
 * fields actually needed for RULE-SEC-034's active-branch check are declared here; all other
 * JSON properties on the real response are ignored.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrgBranchLookup(Long id, Boolean isActive) {
}
