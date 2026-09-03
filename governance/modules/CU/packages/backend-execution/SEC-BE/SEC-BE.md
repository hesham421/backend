<!-- Source: PHASE:SEC-BE -->

## PHASE SEC-BE — Backend Security Specifications
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓

SCOPE: CU is Backend-only with NO screens → no SCR-IDs, no SEC_PAGES rows,
CORE-9 Composite Screen governance does NOT apply (srs-CU PART B).

API-LEVEL ENFORCEMENT:
  Configuration management endpoints are administrative. Every API-CU-00x method
  requires authorization enforced at the API level before request processing
  (config-admin authority). HTTP 403 on failure → mapped via LocalizedException.
  The concrete authority/permission source is the Security (SEC) module; CU
  declares the enforcement requirement, SEC provides the mechanism.

SECURITY SEED DATA REQUIREMENTS: none (no SEC_PAGES/PERMISSIONS rows — no screens).

SEC-BE Governance Rules:
  SEC-IMPL-RULE-1 — every configuration endpoint enforces authorization at API level
  SEC-IMPL-RULE-3 — HTTP 403 mapped via LocalizedException carrying correct ERR-ID
─────────────────────────────────────────────────────────────────
