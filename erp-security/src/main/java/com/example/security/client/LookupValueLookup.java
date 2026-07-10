package com.example.security.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Minimal local view of erp-masterdata's {@code LookupValueResponse}
 * (GET /api/lookups/{lookupCode}) — only the {@code code} field is needed to validate
 * DATA_ACCESS_LEVEL against LOV-SEC-002 (execution-plan-SEC-gaps.md Section 3).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LookupValueLookup(String code) {
}
