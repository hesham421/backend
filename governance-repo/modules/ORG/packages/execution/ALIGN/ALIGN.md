<!-- Source: PHASE:ALIGN -->


---

# PHASE ALIGN — Traceability Alignment Audit

```
╔══════════════════════════════════════════════════════════════════════════════════════════════╗
║  PHASE ALIGN — TRACEABILITY ALIGNMENT AUDIT — ORG-001 — PLAN-ORG-001                       ║
╠══════════════════════════════════════════════════════════════════════════════════════════════╣
║  ALIGN runs after SEC — before any ADDITIONAL phase (APPROVAL, MULTI-VERSION, etc.)         ║
╠══════════════════════════════════════════════════════════════════════════════════════════════╣

ALIGN-1: ENTITY-ID COVERAGE
───────────────────────────
SRS entities   : 8 (ENTITY-ORG-001..008)
Plan entities  : 8 (ENTITY-ORG-001..008)
STATUS         : ALIGNED ✓

ALIGN-2: API-ID COVERAGE
─────────────────────────
SRS APIs       : 47 (API-ORG-001..047)
Plan APIs      : 47 (API-ORG-001..047)
STATUS         : ALIGNED ✓

ALIGN-3: RULE-ID COVERAGE
──────────────────────────
SRS rules      : 20 (RULE-ORG-001..020)
Plan rules     : 20 (in DATA+DOM, SVC+API, F3 phases)
  - 18 directly covered by TC-IDs
  - 2 deferred (RULE-ORG-009, RULE-ORG-010) — documented in DEFERRED registry
STATUS         : ALIGNED ✓ (with documented deferred)

ALIGN-4: ERR-ID COVERAGE
─────────────────────────
RULE-ID→ERR-ID mapping (every rule that has user-facing message must have ERR-ID):
  RULE-ORG-001 → ERR-0001 ✓ │ RULE-ORG-002 → ERR-0002 ✓
  RULE-ORG-003 → ERR-0003 ✓ │ RULE-ORG-004 → ERR-0004 ✓
  RULE-ORG-005 → ERR-0005 ✓ │ RULE-ORG-006 → ERR-0006 ✓
  RULE-ORG-007 → ERR-0007 ✓ │ RULE-ORG-008 → ERR-0008 ✓
  RULE-ORG-009 → ERR-0009 ✓ │ RULE-ORG-010 → ERR-0010 ✓
  RULE-ORG-011 → ERR-0011 ✓ │ RULE-ORG-012 → ERR-0012 ✓
  RULE-ORG-013 → ERR-0013 ✓ │ RULE-ORG-014 → ERR-0014 ✓
  RULE-ORG-015 → ERR-0015 ✓ │ RULE-ORG-016 → no ERR-ID (arch) ✓ documented
  RULE-ORG-017 → ERR-0017 ✓ │ RULE-ORG-018 → ERR-0018 ✓
  RULE-ORG-019 → ERR-0019 ✓ │ RULE-ORG-020 → ERR-0020 ✓
  ERR-0016 — unassigned (RULE-ORG-016 architectural — no user message)
  ERR-0100, ERR-0101 — PLATFORM-STD (DRV-ORG-002)
STATUS         : ALIGNED ✓

ALIGN-5: FIELD-ID ↔ DBF-ID COVERAGE
──────────────────────────────────────
Total FIELD-IDs: FIELD-0001..0061 (61 fields)
DB bindings    : all 61 trace to confirmed DBF-IDs in DBS-ORG-001
Audit fields   : excluded from FIELD-ID assignment — managed by AuditEntityListener
STATUS         : ALIGNED ✓ — see DB Alignment Manifest (Section 4)

ALIGN-6: LOV-ID COVERAGE
──────────────────────────
LOV-ORG-001..006 — MD_LOOKUP_DETAIL — all have:
  ✓ LOOKUP_CODE assigned
  ✓ Endpoint: GET /api/lookups/{lookupKey}?active=true (master-registry canonical)
  ✓ Used in FIELD-ID within DATA+DOM
  ✓ Referenced in F1 (model), F2 (facade init), F3 (validation)
LOV-ORG-007 — Reference Table (ORG_REGION_TYPE) — has:
  ✓ DRV-ORG-001 deviation documented
  ✓ Endpoint: GET /api/v1/org/region-types?active=true (API-ORG-020)
  ✓ Stored as NUMBER FK (not DETAIL_CODE)
STATUS         : ALIGNED ✓

ALIGN-7: SCR-ID COVERAGE
──────────────────────────
SCR-IDs: SCR-ORG-001..007 (7 screens)
  ✓ Each has F1 model specification
  ✓ Each has F2 screen init specification
  ✓ Each has F2 facade specification
  ✓ Each has F3 validation rules
  ✓ Each has SEC block
  ✓ Each linked to SEC_PAGES entry
STATUS         : ALIGNED ✓

ALIGN-7B: F2-SERVICE COVERAGE
──────────────────────────────
F2-SERVICE blocks: 47 (API-ORG-001..047) — all 47 API-IDs covered
  ✓ Each block specifies: HTTP method, endpoint, request shape, response shape
  ✓ Each block specifies: error routing per ERR-ID and HTTP status
  ✓ Each block specifies: loading behavior, caching, XM-ID impact
  ✓ FRONTEND CONTRACTS block declared (error routing + state + pre-deactivation)
STATUS         : ALIGNED ✓

ALIGN-7C: F2-LOV-SERVICE COVERAGE
──────────────────────────────────
F2-LOV-SERVICE blocks: 7 (LOV-ORG-001..007) — all 7 LOV-IDs covered
  ✓ Each block specifies: LOOKUP_CODE, method name, endpoint, return type, DB column
  ✓ LOV-ORG-007 (Reference Table) documented with DRV-ORG-001 cross-reference
STATUS         : ALIGNED ✓

ALIGN-8: QR-ID COMPLETENESS
──────────────────────────────
Assigned       : QR-ORG-0001..0048 (48 QR-IDs)
All referenced : every API contract in SVC+API cites QR-IDs
All documented : see Section 11 (QRC)
STATUS         : ALIGNED ✓

ALIGN-9: DRV-ID COMPLETENESS
──────────────────────────────
All deviations from standard patterns assigned DRV-IDs:
  DRV-ORG-001 ✓ | DRV-ORG-002 ✓ | DRV-ORG-003 ✓ | DRV-ORG-004 ✓
  DRV-ORG-005 ✓ | DRV-ORG-006 ✓ | DRV-ORG-007 ✓ | DRV-ORG-008 ✓
  DRV-ORG-009 ✓ (4A-004-003 remediation — SECTION D TC-ID reconciliation)
STATUS         : ALIGNED ✓

ALIGN-10: INBOUND-STUB COMPLETENESS
─────────────────────────────────────
INBOUND-STUB-ORG-001 — Finance → CostCenter/ProfitCenter — DEFERRED ✓ documented
INBOUND-STUB-ORG-002 — Inventory → LocationSite — DEFERRED ✓ documented
STATUS         : ALIGNED ✓

ALIGN-11: XM-ID COVERAGE
──────────────────────────
XM Register: EMPTY — ROOT MODULE
No outbound XM dependencies to trace
STATUS         : ALIGNED ✓ (ROOT MODULE)

ALIGN-12: SECURITY ALIGNMENT
──────────────────────────────
Every API-ID has SECURITY block citing: Screen → Permission
  API-ORG-001..006 → SCR-ORG-001 → PERM_LEGAL_ENTITY_*
  API-ORG-007..013 → SCR-ORG-002 → PERM_BRANCH_*
  API-ORG-014..020 → SCR-ORG-003 → PERM_REGION_*
  API-ORG-021..027 → SCR-ORG-004 → PERM_DEPARTMENT_*
  API-ORG-028..034 → SCR-ORG-005 → PERM_COST_CENTER_*
  API-ORG-035..040 → SCR-ORG-006 → PERM_PROFIT_CENTER_*
  API-ORG-041..047 → SCR-ORG-007 → PERM_LOCATION_SITE_*
STATUS         : ALIGNED ✓

╠══════════════════════════════════════════════════════════════════════════════════════════════╣
║  ALIGN GATE SUMMARY                                                                          ║
╠══════════════════════════════════════════════════════════════════════════════════════════════╣
║  ALIGN-1   Entity coverage            : ALIGNED ✓                                           ║
║  ALIGN-2   API coverage               : ALIGNED ✓                                           ║
║  ALIGN-3   Rule coverage              : ALIGNED ✓ (2 deferred — documented)                ║
║  ALIGN-4   ERR-ID mapping             : ALIGNED ✓                                           ║
║  ALIGN-5   FIELD-ID↔DBF-ID            : ALIGNED ✓                                           ║
║  ALIGN-6   LOV-ID coverage            : ALIGNED ✓                                           ║
║  ALIGN-7   SCR-ID coverage            : ALIGNED ✓                                           ║
║  ALIGN-7B  F2-SERVICE coverage (47)   : ALIGNED ✓ (Service class + Observable type added)  ║
║  ALIGN-7C  F2-LOV-SERVICE coverage (7): ALIGNED ✓                                           ║
║  ALIGN-8   QR-ID completeness         : ALIGNED ✓                                           ║
║  ALIGN-9   DRV-ID completeness        : ALIGNED ✓ (DRV-ORG-001..009)                       ║
║  ALIGN-10  Inbound-Stub completeness  : ALIGNED ✓                                           ║
║  ALIGN-11  XM-ID coverage             : ALIGNED ✓ (ROOT MODULE)                             ║
║  ALIGN-12  Security alignment         : ALIGNED ✓                                           ║
║  ALIGN-13  Error routing declared     : ALIGNED ✓                                           ║
║  ALIGN-14  REPOSITORY STRATEGY (47)   : ALIGNED ✓ (5-field block per API — AMEND-P3-B)    ║
║  ALIGN-15  SECTION D TC reconciliation: ALIGNED ✓ (DRV-ORG-009 — post-4A correction)      ║
╠══════════════════════════════════════════════════════════════════════════════════════════════╣
║  OVERALL ALIGN GATE: PASSED ✓                                                                ║
╚══════════════════════════════════════════════════════════════════════════════════════════════╝
```

