<!-- Source: PHASE:CORE -->

## PHASE CORE — Architectural Policies
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓

CANONICAL ARCHITECTURE — inherited as-is from PROJECT-3-BACKEND-ENGINE.md §8.1 (non-negotiable, applies to all modules): controller/ (REST only) → service/ (orchestration) → domain/or-Entity (business rules) → repository/ (data access) → entity/ → dto/ → exception/ → config/. No layer-boundary deviations declared for this module.

MODULE-SPECIFIC DECLARATIONS (required by CORE):
  Base package        : com.erp.mdm (DRV-002)
  Domain behavior      : embedded in Entity methods (DRV-001) — `LookupType` and `LookupValue`
                          own their own invariant checks (e.g. code-immutability guard); the
                          Service layer orchestrates persistence + cross-entity checks (usage
                          count before deactivate) and calls into the Entity for field-level rules.
  Entity base           : AuditableEntity (standard, uniform) — both LookupType and LookupValue.
                          Neither is a session artifact; the AuditableEntity exception does not apply.
  Error signaling        : LocalizedException — NotFoundException is BANNED (project standard).
  Transaction scope      : READ_ONLY for all GET/search/exists operations;
                            READ_WRITE for all create/update/deactivate operations.
                            No REQUIRES_NEW anywhere in this module (no nested cross-module calls).
  ERR-ID registration    : every ERR-ID in SECTION A registered in the 4 standard points —
                            ErrorCodes constant + messages.properties + i18n JSON + ErpErrorMapperService.
  Search contract        : SearchRequest extends BaseSearchContractRequest;
                            ALLOWED_SORT_FIELDS declared per search Service (see SVC+API).
  Deactivation           : isActiveFl = false (SMALLINT 0) — record preserved, never deleted.
                            Pre-deactivation usage check required only for LookupType
                            (DRV-008) — LookupValue deactivation has no usage check.

TYPE MAPPING (POSTGRESQL_16 → Java — project-standard, no DRV needed):
  BIGINT                 → Long
  SMALLINT (any *_FL col)→ Boolean
  SMALLINT (SORT_ORDER)  → Short
  VARCHAR(N)             → String
  TIMESTAMP              → LocalDateTime

ARCHITECTURAL POLICIES (non-architectural, module-applicable):
  - LOV values     : not applicable — this module IS the LOV mechanism; no lookup values are
                     loaded by MDM's own backend at runtime for its own fields.
  - Business Code  : not applicable — BC-RULE-0 = NO for both entities (natural keys only).
  - Arabic + English: both name fields required on LookupType and LookupValue (RULE-MDM-005).

CONTROLLERS (DRV-007):
  MdmLookupController            — serves SCR-MDM-001 — API-MDM-001..010 — permission-gated
  MdmLookupConsumptionController — platform-wide provider endpoint — API-MDM-011 — auth-only
─────────────────────────────────────────────────────────────────
