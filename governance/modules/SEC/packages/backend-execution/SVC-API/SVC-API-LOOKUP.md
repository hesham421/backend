<!-- Source: PHASE:SVC-API / SUB:SVC-API-LOOKUP -->
<!-- Context: see SVC-API-HEADER.md for phase-level strategy, registry table, and intro -->

<!-- API:API-SEC-016:START -->
### API-SEC-016 — Lookups (LOV values)
GET /api/v1/security/lookups/{lookupKey} | LookupController.get → LookupService.get
REQUEST path lookupKey ∈ {SEC_PREFERRED_LANG, SEC_USER_STATUS} | RESPONSE 200 [{code,labelAr,labelEn}]
VALIDATIONS: none (runtime-loaded codes) | ERRORS: ERR-0012 → unknown lookupKey → 404
BINDING: LOV-SEC-001 SEC_PREFERRED_LANG (AR,EN); LOV-SEC-002 SEC_USER_STATUS (PENDING_ACTIVATION,ACTIVE,INACTIVE).
Note (v1.3): Module is NOT a lookupKey — modules are served by API-SEC-020 (CRUD) / API-SEC-019 (dashboard), being a reference entity not a LOV.
REPO: QR-SEC-0022 (runtime lookup resolution — no lookup table; resolved from CU i18n / code registry) — READ_ONLY
SECURITY: authenticated.
<!-- API:API-SEC-016:END -->
