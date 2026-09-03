<!-- Source: PHASE:DOC -->

## PHASE DOC — Contract Stabilization (INTERNAL-ONLY, v2.0)
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓ (re-passed v1.3)
DOC-1 API Contract Summary: API-SEC-001..020 all STABLE (auth endpoints public; user/role/page/permission/module/lookup authenticated; API-SEC-019 self-scoped).
DOC-2 DTO Typing: LOV fields (preferredLangId, userStatusId) = String code (never ENUM); passwordHash never in any DTO; no Business Code. **moduleFk = Long (FK); ModuleResponse.moduleCode = String.**
DOC-3 Pagination: JPA Page<T>; SearchRequest extends BaseSearchContractRequest; empty → 200; filters username/email LIKE, status/isActiveFl/moduleFk EXACT.
DOC GATE: PASSED ✓ ⚠ INTERNAL-ONLY — PASS 2 gates on real API Docs (CONTRACT-12).
─────────────────────────────────────────────────────────────────
