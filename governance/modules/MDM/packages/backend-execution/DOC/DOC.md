<!-- Source: PHASE:DOC -->

## PHASE DOC — Contract Stabilization (internal-only, v2.0 — does not gate PASS 2, see CONTRACT-12)

### DOC-1: API Contract Summary
─────────────────────────────────────────────────────────────────
| API-ID | Endpoint | Method | Request DTO | Response DTO | Stability |
|---|---|---|---|---|---|
| API-MDM-001 | /api/v1/mdm/lookup-types | POST | LookupTypeCreateRequest | LookupTypeResponse | STABLE |
| API-MDM-002 | /api/v1/mdm/lookup-types | GET | search params | Page\<LookupTypeResponse\> | STABLE |
| API-MDM-003 | /api/v1/mdm/lookup-types/{id} | PUT | LookupTypeUpdateRequest | LookupTypeResponse | STABLE |
| API-MDM-004 | /api/v1/mdm/lookup-types/{id} | DELETE | — | 204 | STABLE |
| API-MDM-005 | /api/v1/mdm/lookup-types/{id} | GET | — | LookupTypeResponse | STABLE |
| API-MDM-006 | /api/v1/mdm/lookup-types/{typeId}/values | POST | LookupValueCreateRequest | LookupValueResponse | STABLE |
| API-MDM-007 | /api/v1/mdm/lookup-types/{typeId}/values | GET | search params | Page\<LookupValueResponse\> | STABLE |
| API-MDM-008 | /api/v1/mdm/lookup-values/{id} | PUT | LookupValueUpdateRequest | LookupValueResponse | STABLE |
| API-MDM-009 | /api/v1/mdm/lookup-values/{id} | DELETE | — | 204 | STABLE |
| API-MDM-010 | /api/v1/mdm/lookup-values/{id} | GET | — | LookupValueResponse | STABLE |
| API-MDM-011 | /api/v1/mdm/lookups/{typeCode} | GET | — | List\<LookupValueLite\> | STABLE |
─────────────────────────────────────────────────────────────────
Unstable APIs: None. Frontend-governed contracts: None.

### DOC-2: DTO Typing Rules
LOV field typing: not applicable — no LOV-typed field exists in this module's own DTOs.
Business Code: not applicable — BC-RULE-0 = NO throughout.

### DOC-3: Pagination & Filter Standards
Standard project pagination applies to API-MDM-002 and API-MDM-007 (JPA Page\<T\>, BaseSearchContractRequest, empty→200). API-MDM-011 is intentionally unpaginated (small, fully-loaded reference lists by design).

**DOC GATE CHECK:**
[✓] All API-IDs from SVC+API appear in API Contract Summary
[✓] Error Catalog complete with Arabic + English messages
[✓] All APIs marked STABLE
[✓] Pagination standard declared
DOC Gate: PASSED ✓
