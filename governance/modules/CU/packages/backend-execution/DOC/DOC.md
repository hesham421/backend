<!-- Source: PHASE:DOC -->

## PHASE DOC — Contract Stabilization (INTERNAL-ONLY, v2.0)
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓

### DOC-1: API Contract Summary (internal self-check — NOT the frontend source, CONTRACT-12)
API-ID     │ Endpoint                                  │ Method │ Request DTO                 │ Response DTO           │ Stability
───────────┼───────────────────────────────────────────┼────────┼─────────────────────────────┼────────────────────────┼──────────
API-CU-001 │ /api/v1/common/configurations             │ POST   │ ConfigurationCreateRequest  │ ConfigurationResponse  │ STABLE
API-CU-002 │ /api/v1/common/configurations             │ GET    │ SearchRequest (params)      │ Page<ConfigurationResp>│ STABLE
API-CU-003 │ /api/v1/common/configurations/{key}       │ PUT    │ ConfigurationUpdateRequest  │ ConfigurationResponse  │ STABLE
API-CU-004 │ /api/v1/common/configurations/{key}       │ DELETE │ —                           │ confirmation           │ STABLE
API-CU-005 │ /api/v1/common/configurations/{key}       │ GET    │ —                           │ ConfigurationResponse  │ STABLE

### DOC-2: DTO Typing Rules
  Business Code: N/A (CU owns none). No LOV/ENUM fields.

### DOC-3: Pagination & Filter Standards
  JPA Page<T> used directly; SearchRequest extends BaseSearchContractRequest;
  empty result → HTTP 200 (never 404); filters: configKey LIKE, isActiveFl EXACT.

DOC GATE CHECK:
  [✓] All API-IDs appear in summary  [✓] Error Catalog complete AR+EN
  [✓] All APIs STABLE                [✓] Pagination standard declared
DOC Gate: PASSED ✓
⚠ v2.0: DOC-1 is INTERNAL-ONLY. PASS 2 gates on real API Docs, not this table (CONTRACT-12).
─────────────────────────────────────────────────────────────────
