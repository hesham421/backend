<!-- Source: PHASE:SVC-API / SUB:SVC-API-CONSUMPTION -->
<!-- Context: see SVC-API-HEADER.md for phase-level strategy, registry table, and intro -->

### SUB-GROUP: PLATFORM CONSUMPTION — API-MDM-011

<!-- API:API-MDM-011:START -->
### API-MDM-011 — Consume active values by typeCode (platform-wide)
─────────────────────────────────────────────────────────────────
Endpoint         : GET /api/v1/mdm/lookups/{typeCode}
Controller       : MdmLookupConsumptionController → method: getActiveValues
Service          : MdmLookupValueService → method: findActiveByTypeCode
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : typeCode: String
  Query Params   : active (Boolean, default true — the only supported value is true;
                    the endpoint always returns active-only rows per srs-MDM §B5)

RESPONSE:
  Success code   : 200
  Response DTO   : List<LookupValueLite> — { valueCode, nameAr, nameEn, sortOrder }
                   (a lean projection — no lookupValuePk/isActiveFl/audit fields; consumers store
                   only the code, per srs-MDM §A2/A7 SOFT-reference pattern)
  Paginated      : NO — small, fully-loaded reference lists by design

VALIDATIONS: None.
ERRORS: None. Unknown typeCode, or a typeCode whose LookupType is itself inactive, both resolve to
        an empty list with HTTP 200 — never 404 — since this is a consumption/list-shaped endpoint
        (project standard: empty result → 200, not 404) and a typo here must not throw an
        exception into a downstream module's normal flow.

SERVICE ORCHESTRATION:
  1. [load] — QR-MDM-0015 FIND active LookupValues joined to their LookupType by typeCode,
              filtering both LookupType.isActiveFl = 1 AND LookupValue.isActiveFl = 1,
              ordered by sortOrder.

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0015
  Table      : MDM_LOOKUP_VALUE JOIN MDM_LOOKUP_TYPE
  Join       : REQUIRED — DRV-005 (path param is the parent's natural key, not the child's PK)
  Transaction: READ_ONLY

SECURITY:
  Screen     : NOT gated by SCR-MDM-001 permissions (DRV-006). Requires only standard JWT
               authentication (any authenticated caller/service) — srs-MDM §B5, US-MDM-003.

LOCALIZATION: Both nameAr and nameEn always returned; caller resolves display language.
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-011:END -->

