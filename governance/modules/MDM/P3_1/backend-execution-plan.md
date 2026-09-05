<!-- Backend Execution Plan — Governed by Execution Plan Governance Engine (Project 3.1, PASS 1, v3.0 LIGHT) -->
<!-- Consumes: srs-MDM.md v1.3 (AUTHORITATIVE) + db-script-MDM.md / DBS-MDM-001 (AUTHORITATIVE) -->

# BACKEND EXECUTION PLAN — Master Data (MDM)

## EXECUTION PLAN INDEX — Master Data (MDM) — PLAN-ID: PLAN-MDM-001
══════════════════════════════════════════════════════════════════
Feature Code   : MDM-001 (srs-MDM.md v1.3, 2026-09-04)
DBS-ID         : DBS-MDM-001 (db-script-MDM.md, GATE PASSED — DDL + seed, 2026-09-04)
DB_TARGET      : POSTGRESQL_16
Task Type      : 🆕 New Feature — full module, first backend pass
Plan Name      : New Feature — Reference Data Lookup Management — Master Data — BE
Governed by    : Execution Plan Governance Engine (Project 3.1) v3.0 (light)
Output Mode    : SINGLE-FILE — Agent-Ready Specification
Open Questions : None active — OQ-MDM-001, OQ-MDM-002 both RESOLVED upstream (see §OQ LOG)
Governance Reduced : NO — both srs.md and db-script.md present; Gate DB = PASSED ✓
══════════════════════════════════════════════════════════════════

### PASS 1 ENTRY GATE (evaluated)

| Check | Result |
|---|---|
| SRS attached + feature code | YES — MDM-001, srs-MDM.md v1.3 |
| DB Script attached (DBS-ID) | YES — DBS-MDM-001 |
| Gate DB passed | PASSED ✓ (DDL + seed) |
| Entities in SRS = tables in DB | 2 = 2 (LookupType→MDM_LOOKUP_TYPE, LookupValue→MDM_LOOKUP_VALUE) |
| Registry loaded, no conflicts | ✓ — project-registry.md (v1.1.4) — MDM row present, P2 COMPLETE |
| Naming: 3-way consistent (SRS/DB/Registry) | ✓ — no violations |
| module-registry-MDM.md loaded | ✓ — used for consistency (scope note, dependencies, no LOVs owned/consumed) |
| EXCEPTION modules detected | None |
| Extracted | 2 entities, 14 fields, 11 APIs, 6 rules (+1 DB-structural, see DRV-003), 0 XM dependencies, 0 open OQs |

Proceeding directly per Zero-Question Protocol (PROJECT-3-REGISTRY.md §5.5) — srs.md and db-script.md answer every element needed for this plan; no ambiguity requiring a Modify checkpoint.

### SRS ANALYSIS SUMMARY

| Item | Value |
|---|---|
| Task Type | 🆕 New Feature |
| Plan Name | New Feature — Reference Data Lookup Management — Master Data — BE |
| Extracted Entities | ENTITY-MDM-001 LookupType (MASTER), ENTITY-MDM-002 LookupValue (DETAIL) |
| API Count | 11 |
| Rules Count | 6 (RULE-MDM-001..006) — of which Client Policy: 0 |
| EXCEPTION modules | None |
| Approval Workflow | No (RULE-13 = OFF) |
| XM Dependencies | 0 — MDM is a provider consumed via REST API; no cross-module FK |
| Open Questions | 0 — OQ-MDM-001/002 RESOLVED |
| Phases Required (PASS 1) | CORE, DATA+DOM, SVC+API, DOC, INT-C, INT-R, SEC-BE, ALIGN-BE |

---

### ENTITY REGISTRY (this plan)
| ENTITY-ID | Entity Name | DB Table | Business Code | Operations |
|---|---|---|---|---|
| ENTITY-MDM-001 | LookupType (master) | MDM_LOOKUP_TYPE | NO — BC-RULE-0 (natural key `typeCode`) | Create, Read, Update, Deactivate |
| ENTITY-MDM-002 | LookupValue (detail) | MDM_LOOKUP_VALUE | NO — BC-RULE-0 (natural key `valueCode`) | Create, Read, Update, Deactivate |

### FIELD REGISTRY (this plan)
| FIELD-ID | Field Name | DBF-ID | Type | Read-Only |
|---|---|---|---|---|
| FIELD-0001 | lookupTypePk | DBF-0001 | Long | System (PK) |
| FIELD-0002 | typeCode | DBF-0002 | String(50) | YES (after create — RULE-MDM-002) |
| FIELD-0003 | nameAr | DBF-0003 | String(200) | No |
| FIELD-0004 | nameEn | DBF-0004 | String(100) | No |
| FIELD-0005 | isActiveFl | DBF-0005 | Boolean | System |
| FIELD-0006 | notes | DBF-0006 | String(2000) | No |
| FIELD-0007 | lookupValuePk | DBF-0007 | Long | System (PK) |
| FIELD-0008 | lookupTypeFk | DBF-0008 | Long | System (set once at create, see DRV-003) |
| FIELD-0009 | valueCode | DBF-0009 | String(50) | YES (after create — RULE-MDM-004) |
| FIELD-0010 | nameAr | DBF-0010 | String(200) | No |
| FIELD-0011 | nameEn | DBF-0011 | String(100) | No |
| FIELD-0012 | sortOrder | DBF-0012 | Short | No |
| FIELD-0013 | isActiveFl | DBF-0013 | Boolean | System |
| FIELD-0014 | notes | DBF-0014 | String(2000) | No |

Note: `createdBy`/`createdAt`/`updatedBy`/`updatedAt` exist on both tables (AuditEntityListener) but carry **no FIELD-ID / no DBF-ID** — db-script-MDM.md §2 states this explicitly ("Audit columns ... no DBF-ID"). No Business Code fields exist in this module (BC-RULE-0 = NO on both entities).

### API REGISTRY (this plan)
| API-ID | Operation | HTTP | Endpoint |
|---|---|---|---|
| API-MDM-001 | Create LookupType | POST | /api/v1/mdm/lookup-types |
| API-MDM-002 | Search LookupTypes | GET | /api/v1/mdm/lookup-types |
| API-MDM-003 | Update LookupType | PUT | /api/v1/mdm/lookup-types/{id} |
| API-MDM-004 | Deactivate LookupType (soft) | DELETE | /api/v1/mdm/lookup-types/{id} |
| API-MDM-005 | Get LookupType by id | GET | /api/v1/mdm/lookup-types/{id} |
| API-MDM-006 | Create LookupValue | POST | /api/v1/mdm/lookup-types/{typeId}/values |
| API-MDM-007 | List LookupValues under type | GET | /api/v1/mdm/lookup-types/{typeId}/values |
| API-MDM-008 | Update LookupValue | PUT | /api/v1/mdm/lookup-values/{id} |
| API-MDM-009 | Deactivate LookupValue (soft) | DELETE | /api/v1/mdm/lookup-values/{id} |
| API-MDM-010 | Get LookupValue by id | GET | /api/v1/mdm/lookup-values/{id} |
| API-MDM-011 | Consume active values by typeCode (platform) | GET | /api/v1/mdm/lookups/{typeCode} |

### RULE REGISTRY (this plan)
| RULE-ID | Rule Name | Scope | ENTITY-ID | Message-AR defined |
|---|---|---|---|---|
| RULE-MDM-001 | Type code uniqueness | CREATE/UPDATE | ENTITY-MDM-001 | ✓ |
| RULE-MDM-002 | Type code immutable after creation | UPDATE | ENTITY-MDM-001 | ✓ |
| RULE-MDM-003 | Value code uniqueness within type | CREATE/UPDATE | ENTITY-MDM-002 | ✓ |
| RULE-MDM-004 | Value code immutable after creation | UPDATE | ENTITY-MDM-002 | ✓ |
| RULE-MDM-005 | Both names (AR/EN) mandatory | CREATE/UPDATE | ENTITY-MDM-001, ENTITY-MDM-002 | ✓ |
| RULE-MDM-006 | Soft deactivation only; block deactivating a type with active values | ALL | ENTITY-MDM-001, ENTITY-MDM-002 | ✓ |

Note on "RULE-MDM-007": db-script-MDM.md cites this label for the `LOOKUP_TYPE_FK` NOT NULL constraint. It does **not** appear in srs-MDM.md §A4 as a governed business rule (no Message-AR/EN). Treated as a DB-structural integrity constraint, not a domain RULE-ID — see Derivation Log DRV-003. No RULE-ID is invented for it in this plan.

### SCREEN REGISTRY (this plan)
| SCR-ID | Screen Name | Type | ENTITY-ID |
|---|---|---|---|
| SCR-MDM-001 | إدارة القوائم المرجعية (Reference Data Lookup Management) | COMPOSITE (Master + Detail, PATTERN-1, CORE-9) | ENTITY-MDM-001 (master) + ENTITY-MDM-002 (detail) |

### LOV REGISTRY (this plan)
None owned or consumed. MDM's own entities (LookupType/LookupValue) **are** the platform's shared LOV mechanism (srs-MDM §A5) — there is no LOV-ID for this module.

### QUERY REFERENCE CATALOG SUMMARY
| QR-ID | Operation | Phase | Entity |
|---|---|---|---|
| QR-MDM-0001 | FIND_ONE LookupType by PK | DATA+DOM | ENTITY-MDM-001 |
| QR-MDM-0002 | FIND_BY_CRITERIA LookupType search | DATA+DOM | ENTITY-MDM-001 |
| QR-MDM-0003 | EXISTS LookupType by typeCode | DATA+DOM | ENTITY-MDM-001 |
| QR-MDM-0004 | SAVE LookupType | DATA+DOM | ENTITY-MDM-001 |
| QR-MDM-0005 | UPDATE LookupType | DATA+DOM | ENTITY-MDM-001 |
| QR-MDM-0006 | COUNT active LookupValue under type (usage check) | SVC+API | ENTITY-MDM-002 |
| QR-MDM-0007 | UPDATE LookupType (soft deactivate) | DATA+DOM | ENTITY-MDM-001 |
| QR-MDM-0008 | FIND_ONE LookupValue by PK | DATA+DOM | ENTITY-MDM-002 |
| QR-MDM-0009 | FIND_BY_CRITERIA LookupValue under type | DATA+DOM | ENTITY-MDM-002 |
| QR-MDM-0010 | EXISTS LookupValue by (lookupTypeFk, valueCode) | DATA+DOM | ENTITY-MDM-002 |
| QR-MDM-0011 | EXISTS LookupType by PK (parent existence check) | SVC+API | ENTITY-MDM-001 |
| QR-MDM-0012 | SAVE LookupValue | DATA+DOM | ENTITY-MDM-002 |
| QR-MDM-0013 | UPDATE LookupValue | DATA+DOM | ENTITY-MDM-002 |
| QR-MDM-0014 | UPDATE LookupValue (soft deactivate) | DATA+DOM | ENTITY-MDM-002 |
| QR-MDM-0015 | FIND active LookupValues by parent typeCode (consumption) | SVC+API | ENTITY-MDM-001 + ENTITY-MDM-002 (join) |

---

## DB ALIGNMENT MANIFEST — Master Data (MDM) — PLAN-ID: PLAN-MDM-001 / DBS-ID: DBS-MDM-001
══════════════════════════════════════════════════════════════════
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0001 | DBF-0001 | Long | — | ✓ |
| FIELD-0002 | DBF-0002 | String(50) | — | ✓ |
| FIELD-0003 | DBF-0003 | String(200) | — | ✓ |
| FIELD-0004 | DBF-0004 | String(100) | — | ✓ |
| FIELD-0005 | DBF-0005 | Boolean | — | ✓ |
| FIELD-0006 | DBF-0006 | String(2000) | — | ✓ |
| FIELD-0007 | DBF-0007 | Long | — | ✓ |
| FIELD-0008 | DBF-0008 | Long | intra-module FK → ENTITY-MDM-001 | ✓ |
| FIELD-0009 | DBF-0009 | String(50) | — | ✓ |
| FIELD-0010 | DBF-0010 | String(200) | — | ✓ |
| FIELD-0011 | DBF-0011 | String(100) | — | ✓ |
| FIELD-0012 | DBF-0012 | Short | — | ✓ |
| FIELD-0013 | DBF-0013 | Boolean | — | ✓ |
| FIELD-0014 | DBF-0014 | String(2000) | — | ✓ |
══════════════════════════════════════════════════════════════════
Legend: ✓ = aligned | ✗ = type mismatch (finding) | ⏸ = XM deferred
Result: 14/14 aligned, 0 mismatches, 0 deferred. No cross-module (XM) FK exists in this module.

---

## OPEN QUESTIONS LOG — CONTINUATION — Master Data (MDM)
──────────────────────────────────────────────────────────────────
| OQ-ID | Question | Status | Raised | Resolved |
|---|---|---|---|---|
| OQ-MDM-001 | Admin surface: composite screen vs. provider-only | RESOLVED | MODE 1 | MODE 1 v1.1 |
| OQ-MDM-002 | Governed source for FILE_FILE_TYPE seed enumeration | RESOLVED | MODE 1 v1.2 | MODE 1 v1.3 |

No new OQ raised during PASS 1. Both srs.md and db-script.md fully answer every element needed to generate this plan (Zero-Question Protocol, PROJECT-3-REGISTRY.md §5.5, Steps 1–2 satisfied for all elements; Step 4/ERP-best-practice satisfied for the handful of implementation-detail derivations below). Open Questions: **0 active / None**.

---

## DERIVATION LOG — Master Data (MDM)
──────────────────────────────────────────────────────────────────
| DRV-ID | Element | Criterion | Basis |
|---|---|---|---|
| DRV-001 | Domain behavior placement = embedded in Entity methods (not separate `domain/` classes) | Zero-Question Step 4 (ERP best practice) | Two simple reference entities, no multi-step orchestration; domain-profile.md v3 "تعقيد متوسط مقصود" (avoid over-engineering) governs the choice |
| DRV-002 | Base Java package: `com.erp.mdm` | Zero-Question Step 4 (ERP/Spring Boot convention) | Not specified in srs/db-script; standard per-module package convention consistent with other Foundation modules |
| DRV-003 | db-script-MDM.md's "RULE-MDM-007" (LOOKUP_TYPE_FK NOT NULL) is **not** promoted to a domain RULE-ID | Criterion-check against srs.md §A4 — absent; db-script only | srs-MDM.md §A4 defines RULE-MDM-001..006 only. The NOT NULL FK is a DB-structural master-detail integrity constraint, already fully enforced by DDL (BLOCK 5d, `FK_MDM_LOOKUP_VALUE_TYPE`). Treated as structural, not business-rule — no Message-AR/EN exists to carry, so no ERR-ID is minted for "missing type" beyond the standard parent-existence 404 (see DRV-004). Flagged for P1/P2 label reconciliation — non-blocking, does not gate ALIGN-BE. |
| DRV-004 | API-MDM-006 / API-MDM-007 (nested under `{typeId}`) return HTTP 404 (ERR-0007) when the parent LookupType id does not exist | Zero-Question Step 4 (REST best practice for nested resources) | Not explicitly stated in srs.md; standard practice for a path-nested parent that does not exist |
| DRV-005 | API-MDM-011 (QR-MDM-0015) requires a JOIN from LookupType to LookupValue | Join Governance Table — "Response requires parent entity data" | Path parameter is `typeCode` (natural key on the MASTER), but the response is the DETAIL rows — an intra-module (not cross-module) join is required to resolve type→values in one round trip |
| DRV-006 | API-MDM-011 is **not** gated by PERM_MDM_LOOKUP_* — only standard JWT authentication | Criterion-2 — explicit in srs.md §B5 ("قراءة لأي مُصادَق عليه" — readable by any authenticated caller, US-MDM-003) | This is the platform-wide provider endpoint, not part of the SCR-MDM-001 admin screen |
| DRV-007 | Two controllers: `MdmLookupController` (SCR-MDM-001, API-MDM-001..010) and `MdmLookupConsumptionController` (API-MDM-011, platform-wide) | CORE-9 — "one Controller per Composite Screen" | API-MDM-011 does not serve the composite screen (see DRV-006), so it sits outside SCR-MDM-001's single-controller rule without violating it |
| DRV-008 | RULE-MDM-006's "block" clause applies to deactivating **LookupType** only (blocked when it still has active LookupValues); LookupValue deactivation has no equivalent child-check | Criterion-2 — explicit in srs.md §A4 RULE-MDM-006 statement ("MUST prevent hard-deleting a LookupType that still has LookupValues") | LookupValue is a leaf — no rows depend on it |
| DRV-009 | Deactivating a LookupType does **not** cascade to deactivate its LookupValues | Criterion-2 — explicit in srs.md §A4 RULE-MDM-006 ("deactivation cascades no change to external consumers") | Explicit no-cascade instruction; each LookupValue is deactivated independently by its own admin action |
| DRV-010 | FIELD-ID sequence 0001–0014 assigned continuously across both entities, 1:1 with DBF-0001–0014; audit columns carry no FIELD-ID | Section 4 FIELD-ID Assignment Rules + db-script explicit "no DBF-ID" statement | Direct, unambiguous 1:1 source mapping — no invention |
| DRV-011 | Search default: `isActiveFl` filter defaults to `1` (active only) when the caller does not supply it, on both API-MDM-002 and API-MDM-007 | Criterion-2 — explicit in srs.md §B2 ("استبعاد المعطَّلة افتراضيًا في العرض" → RULE-MDM-006) | Applies the same default-exclude-inactive behavior SRS states for the type search screen to the nested value list, for consistency |

---

<!-- PHASE:CORE:START -->
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
<!-- PHASE:CORE:END -->

<!-- PHASE:DATA-DOM:START -->
## PHASE DATA+DOM — Entity & Domain Specifications

### ENTITY-MDM-001 — LookupType (نوع القائمة المرجعية) — MASTER
────────────────────────────────────────────────────────────────────────
SOURCE BINDINGS:
  DB Table       : MDM_LOOKUP_TYPE
  PK Column      : ID
  PK Sequence    : SEQ_MDM_LOOKUP_TYPE
  PK Trigger     : none (PostgreSQL — PK value obtained via `nextval('SEQ_MDM_LOOKUP_TYPE')`, no trigger construct)
  DBS-ID ref     : DBS-MDM-001

BUSINESS CODE: NOT APPLICABLE — BC-RULE-0 = NO. `typeCode` is a natural key (see FIELDS below), not a system-generated Business Code.

SOFT DEACTIVATION:
  Governed by CORE Deactivation Policy. DB binding: isActiveFl → IS_ACTIVE_FL — DBF-0005.
  Pre-deactivation usage check REQUIRED (DRV-008): block deactivation while ≥1 active
  LookupValue exists under this type (RULE-MDM-006) — see QR-MDM-0006.

AUDIT COLUMNS (AuditEntityListener — no DBF-ID, per db-script-MDM.md §2):
  createdBy / createdAt / updatedBy / updatedAt — String / LocalDateTime / String / LocalDateTime.
  ⚠ MUST NOT appear in CreateRequest/UpdateRequest DTOs. Never set manually.

────────────────────────────────────────────────────────────────────────
FIELDS:
────────────────────────────────────────────────────────────────────────
| FIELD-ID | Java Property | DB Column | DBF-ID | DB Type | Null | Read-Only | Constraint | Label-AR | Label-EN |
|---|---|---|---|---|---|---|---|---|---|
| FIELD-0001 | lookupTypePk | ID | DBF-0001 | BIGINT | No | System | PK — SEQ_MDM_LOOKUP_TYPE | المعرف | ID |
| FIELD-0002 | typeCode | TYPE_CODE | DBF-0002 | VARCHAR(50) | No | YES (RULE-MDM-002) | UNIQUE — UQ_MDM_LOOKUP_TYPE_CODE | رمز النوع | Type Code |
| FIELD-0003 | nameAr | NAME_AR | DBF-0003 | VARCHAR(200) | No | No | NOT NULL (RULE-MDM-005) | الاسم بالعربي | Name (Arabic) |
| FIELD-0004 | nameEn | NAME_EN | DBF-0004 | VARCHAR(100) | No | No | NOT NULL (RULE-MDM-005) | الاسم بالإنجليزي | Name (English) |
| FIELD-0005 | isActiveFl | IS_ACTIVE_FL | DBF-0005 | SMALLINT | No | System | DEFAULT 1, CHECK IN (0,1) | نشط | Active |
| FIELD-0006 | notes | NOTES | DBF-0006 | VARCHAR(2000) | Yes | No | — | ملاحظات | Notes |
| — | createdBy/createdAt/updatedBy/updatedAt | CREATED_BY/CREATED_AT/UPDATED_BY/UPDATED_AT | none (AuditEntityListener) | VARCHAR(255)/TIMESTAMP | Yes | System | — | أنشئ بواسطة / تاريخ الإنشاء / عُدِّل بواسطة / تاريخ التعديل | Created By / Created At / Updated By / Updated At |

DTO MEMBERSHIP:
  CreateRequest : typeCode, nameAr, nameEn, notes?
  UpdateRequest : nameAr, nameEn, notes? (typeCode EXCLUDED — RULE-MDM-002)
  ResponseDTO   : all fields including typeCode, isActiveFl, createdBy, createdAt, updatedBy, updatedAt

────────────────────────────────────────────────────────────────────────
DOMAIN RULES:
────────────────────────────────────────────────────────────────────────
RULE-MDM-001 — Type code uniqueness:
  Trigger    : On create or update of LookupType
  Statement  : The system MUST prevent creating a LookupType whose typeCode already exists.
  Message-AR : رمز النوع مستخدَم مسبقًا — اختر رمزًا فريدًا.
  Message-EN : This type code already exists — choose a unique code.
  Scope      : CREATE
  DB Enforce : UNIQUE constraint UQ_MDM_LOOKUP_TYPE_CODE
  ERR-ID     : ERR-0001
  Owned by   : domain (Entity method `LookupType.validateNewCode(...)`, backed by service-level EXISTS check QR-MDM-0003)

RULE-MDM-002 — Type code immutable after creation:
  Trigger    : On update of LookupType
  Statement  : The system MUST prevent modifying typeCode after creation.
  Message-AR : لا يمكن تعديل رمز النوع بعد الإنشاء — القيمة مرجع تعتمد عليه موديولات أخرى.
  Message-EN : Type code cannot be changed after creation — other modules reference it.
  Scope      : UPDATE
  DB Enforce : app-level (typeCode excluded entirely from UpdateRequest DTO)
  ERR-ID     : ERR-0003
  Owned by   : domain (Entity method rejects any attempt to set typeCode post-creation)

RULE-MDM-005 — Both names mandatory (applies to LookupType and LookupValue):
  Trigger    : On save (create/update)
  Statement  : The system MUST require both nameAr and nameEn before saving a LookupType or a LookupValue.
  Message-AR : الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي.
  Message-EN : Both the Arabic name and the English name are required.
  Scope      : CREATE, UPDATE
  DB Enforce : NOT NULL on NAME_AR, NAME_EN
  ERR-ID     : ERR-0002 (LookupType) / ERR-0009 (LookupValue)
  Owned by   : domain (bean-validation @NotBlank + Entity invariant)

RULE-MDM-006 — Soft deactivation only; block deactivating a type with active values:
  Trigger    : On deactivate request for LookupType
  Statement  : The system MUST deactivate reference data via isActiveFl (soft) rather than
               hard-delete, and MUST prevent hard-deleting/deactivating a LookupType that still
               has active LookupValues; deactivation cascades no change to external consumers.
  Message-AR : يُستبعَد العنصر بالتعطيل لا بالحذف؛ لا يمكن حذف نوع يحتوي قيمًا.
  Message-EN : Items are deactivated, not deleted; a type that still has values cannot be hard-deleted.
  Scope      : DELETE (soft)
  DB Enforce : app-level (QR-MDM-0006 usage check) — no hard-delete endpoint exists at all (DRV-008/009)
  ERR-ID     : ERR-0005
  Owned by   : domain, with service-level orchestration for the usage count (Section 8.1 CORE Deactivation Policy)

────────────────────────────────────────────────────────────────────────
CROSS-MODULE DEPENDENCIES: None. Consumed via API (provider pattern) — see srs-MDM §A7 / db-script §3 (XM Register: none).

REPOSITORY OPERATIONS REQUIRED:
  → QR-MDM-0001 : FIND_ONE by PK
  → QR-MDM-0002 : FIND_BY_CRITERIA (search: typeCode, nameAr, nameEn, isActiveFl)
  → QR-MDM-0003 : EXISTS by typeCode (uniqueness check)
  → QR-MDM-0004 : SAVE (create)
  → QR-MDM-0005 : UPDATE (nameAr/nameEn/notes only)
  → QR-MDM-0007 : UPDATE (soft deactivate)
  → QRC full entries in SECTION B.
────────────────────────────────────────────────────────────────────────

### ENTITY-MDM-002 — LookupValue (قيمة القائمة المرجعية) — DETAIL
────────────────────────────────────────────────────────────────────────
SOURCE BINDINGS:
  DB Table       : MDM_LOOKUP_VALUE
  PK Column      : ID
  PK Sequence    : SEQ_MDM_LOOKUP_VALUE
  PK Trigger     : none (PostgreSQL sequence, no trigger construct)
  DBS-ID ref     : DBS-MDM-001

BUSINESS CODE: NOT APPLICABLE — BC-RULE-0 = NO. `valueCode` is a natural key, unique within its parent type.

SOFT DEACTIVATION:
  Governed by CORE Deactivation Policy. DB binding: isActiveFl → IS_ACTIVE_FL — DBF-0013.
  No usage/child check required (DRV-008 — LookupValue is a leaf entity).

AUDIT COLUMNS (AuditEntityListener — no DBF-ID): createdBy/createdAt/updatedBy/updatedAt — same rules as ENTITY-MDM-001.

────────────────────────────────────────────────────────────────────────
FIELDS:
────────────────────────────────────────────────────────────────────────
| FIELD-ID | Java Property | DB Column | DBF-ID | DB Type | Null | Read-Only | Constraint | Label-AR | Label-EN |
|---|---|---|---|---|---|---|---|---|---|
| FIELD-0007 | lookupValuePk | ID | DBF-0007 | BIGINT | No | System | PK — SEQ_MDM_LOOKUP_VALUE | المعرف | ID |
| FIELD-0008 | lookupTypeFk | LOOKUP_TYPE_FK | DBF-0008 | BIGINT | No | System (set once, at create) | FK → MDM_LOOKUP_TYPE(ID) — FK_MDM_LOOKUP_VALUE_TYPE, NOT NULL (DRV-003) | نوع القائمة | Lookup Type |
| FIELD-0009 | valueCode | VALUE_CODE | DBF-0009 | VARCHAR(50) | No | YES (RULE-MDM-004) | UNIQUE(lookupTypeFk, valueCode) — UQ_MDM_LOOKUP_VALUE_TYPE_CODE | رمز القيمة | Value Code |
| FIELD-0010 | nameAr | NAME_AR | DBF-0010 | VARCHAR(200) | No | No | NOT NULL (RULE-MDM-005) | الاسم بالعربي | Name (Arabic) |
| FIELD-0011 | nameEn | NAME_EN | DBF-0011 | VARCHAR(100) | No | No | NOT NULL (RULE-MDM-005) | الاسم بالإنجليزي | Name (English) |
| FIELD-0012 | sortOrder | SORT_ORDER | DBF-0012 | SMALLINT | Yes | No | — | ترتيب العرض | Sort Order |
| FIELD-0013 | isActiveFl | IS_ACTIVE_FL | DBF-0013 | SMALLINT | No | System | DEFAULT 1, CHECK IN (0,1) | نشط | Active |
| FIELD-0014 | notes | NOTES | DBF-0014 | VARCHAR(2000) | Yes | No | — | ملاحظات | Notes |
| — | createdBy/createdAt/updatedBy/updatedAt | CREATED_BY/CREATED_AT/UPDATED_BY/UPDATED_AT | none (AuditEntityListener) | VARCHAR(255)/TIMESTAMP | Yes | System | — | (same as ENTITY-MDM-001) | (same) |

DTO MEMBERSHIP:
  CreateRequest : valueCode, nameAr, nameEn, sortOrder? (lookupTypeFk comes from the `{typeId}` path param — never a body field)
  UpdateRequest : nameAr, nameEn, sortOrder? (valueCode EXCLUDED — RULE-MDM-004; lookupTypeFk EXCLUDED — immutable parent)
  ResponseDTO   : all fields including valueCode, sortOrder, isActiveFl, createdBy, createdAt, updatedBy, updatedAt

────────────────────────────────────────────────────────────────────────
DOMAIN RULES:
────────────────────────────────────────────────────────────────────────
RULE-MDM-003 — Value code uniqueness within type:
  Trigger    : On create or update of LookupValue
  Statement  : The system MUST prevent creating a LookupValue whose valueCode already exists under the same lookupTypeFk.
  Message-AR : رمز القيمة مستخدَم مسبقًا ضمن هذا النوع — اختر رمزًا فريدًا.
  Message-EN : This value code already exists under this type — choose a unique code.
  Scope      : CREATE
  DB Enforce : UNIQUE constraint UQ_MDM_LOOKUP_VALUE_TYPE_CODE (composite: LOOKUP_TYPE_FK, VALUE_CODE)
  ERR-ID     : ERR-0008
  Owned by   : domain (Entity method, backed by QR-MDM-0010)

RULE-MDM-004 — Value code immutable after creation:
  Trigger    : On update of LookupValue
  Statement  : The system MUST prevent modifying valueCode after creation.
  Message-AR : لا يمكن تعديل رمز القيمة بعد الإنشاء — القيمة مرجع تخزّنه موديولات أخرى.
  Message-EN : Value code cannot be changed after creation — other modules store it.
  Scope      : UPDATE
  DB Enforce : app-level (valueCode excluded entirely from UpdateRequest DTO)
  ERR-ID     : ERR-0010
  Owned by   : domain

RULE-MDM-005 — Both names mandatory: see full text under ENTITY-MDM-001 (Scope here: LookupValue half). ERR-ID: ERR-0009.

RULE-MDM-006 — Soft deactivation: see full text under ENTITY-MDM-001. For LookupValue, only the soft-deactivate-not-hard-delete half applies (no child-usage check, DRV-008). No dedicated ERR-ID beyond standard 404 (ERR-0012) — deactivation itself cannot fail once the row is found.

────────────────────────────────────────────────────────────────────────
CROSS-MODULE DEPENDENCIES: None directly. `lookupTypeFk` is an INTRA-MODULE FK to ENTITY-MDM-001 (same module, same DBS-ID) — not an XM dependency.

REPOSITORY OPERATIONS REQUIRED:
  → QR-MDM-0008 : FIND_ONE by PK
  → QR-MDM-0009 : FIND_BY_CRITERIA (list under type, filter isActiveFl)
  → QR-MDM-0010 : EXISTS by (lookupTypeFk, valueCode)
  → QR-MDM-0011 : EXISTS LookupType by PK (parent existence — DRV-004)
  → QR-MDM-0012 : SAVE (create)
  → QR-MDM-0013 : UPDATE (nameAr/nameEn/sortOrder)
  → QR-MDM-0014 : UPDATE (soft deactivate)
  → QRC full entries in SECTION B.
────────────────────────────────────────────────────────────────────────
<!-- PHASE:DATA-DOM:END -->

<!-- PHASE:SVC-API:START -->
## PHASE SVC+API — Service & API Contract Specifications

Gate Status: PASSED ✓ — 11 APIs (≥ 8 threshold) → split into semantic SUB-groups per PROJECT-3-REGISTRY.md §5.7.4.

<!-- SUB:SVC-API-LOOKUP-TYPE:START -->
### SUB-GROUP: LOOKUP TYPE (Master) — API-MDM-001..005

<!-- API:API-MDM-001:START -->
### API-MDM-001 — Create LookupType
─────────────────────────────────────────────────────────────────
Endpoint         : POST /api/v1/mdm/lookup-types
Controller       : MdmLookupController → method: createLookupType
Service          : MdmLookupTypeService → method: create
─────────────────────────────────────────────────────────────────
REQUEST:
  Content-Type   : application/json
  Request Body   : LookupTypeCreateRequest
    Fields:
      typeCode : String  REQUIRED — maps to TYPE_CODE, UNIQUE (RULE-MDM-001)
      nameAr   : String  REQUIRED — maps to NAME_AR (RULE-MDM-005)
      nameEn   : String  REQUIRED — maps to NAME_EN (RULE-MDM-005)
      notes    : String  OPTIONAL — maps to NOTES
    Excluded fields : lookupTypePk (system, SEQ_MDM_LOOKUP_TYPE), isActiveFl (system, default 1),
                       createdBy/createdAt/updatedBy/updatedAt (AuditEntityListener)

RESPONSE:
  Success code   : 201
  Response DTO   : LookupTypeResponse (all FIELD-0001..0006 + audit fields)
  Paginated      : NO

VALIDATIONS:
  1. RULE-MDM-001 — Type code uniqueness:
       Statement  : The system MUST prevent creating a LookupType whose typeCode already exists.
       Trigger    : On create
       Message-AR : رمز النوع مستخدَم مسبقًا — اختر رمزًا فريدًا.
       Message-EN : This type code already exists — choose a unique code.
  2. RULE-MDM-005 — Both names mandatory:
       Statement  : The system MUST require both nameAr and nameEn before saving.
       Trigger    : On save
       Message-AR : الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي.
       Message-EN : Both the Arabic name and the English name are required.

ERRORS:
  ERR-0001 → RULE-MDM-001 → HTTP 409
             Message-AR: رمز النوع مستخدَم مسبقًا — اختر رمزًا فريدًا.
             Message-EN: This type code already exists — choose a unique code.
  ERR-0002 → RULE-MDM-005 → HTTP 400
             Message-AR: الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي.
             Message-EN: Both the Arabic name and the English name are required.

SERVICE ORCHESTRATION:
  1. [validate]  — bean validation on request body (nameAr/nameEn required — RULE-MDM-005)
  2. [validate]  — RULE-MDM-001: QR-MDM-0003 EXISTS check on typeCode; if true → ERR-0001
  3. [persist]   — QR-MDM-0004 SAVE; PK from SEQ_MDM_LOOKUP_TYPE; isActiveFl defaults to 1

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0003 (EXISTS), QR-MDM-0004 (SAVE)
  Table      : MDM_LOOKUP_TYPE
  Transaction: READ_WRITE
  Sequence   : SEQ_MDM_LOOKUP_TYPE

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_CREATE (VIEW required as prerequisite — CORE-9)

LOCALIZATION: Error responses carry messageAr + messageEn from SECTION A. Name responses return both nameAr and nameEn.
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-001:END -->

<!-- API:API-MDM-002:START -->
### API-MDM-002 — Search LookupTypes
─────────────────────────────────────────────────────────────────
Endpoint         : GET /api/v1/mdm/lookup-types
Controller       : MdmLookupController → method: searchLookupTypes
Service          : MdmLookupTypeService → method: search
─────────────────────────────────────────────────────────────────
REQUEST:
  Query Params   : typeCode (String, optional, LIKE), nameAr (String, optional, LIKE),
                   nameEn (String, optional, LIKE), isActiveFl (Boolean, optional — default true
                   when omitted, DRV-011), page (int), size (int), sortBy, sortDir

RESPONSE:
  Success code   : 200
  Response DTO   : Page<LookupTypeResponse>
  Paginated      : YES — JPA Page<T>

VALIDATIONS: None (read-only search).
ERRORS: None dedicated — empty result set returns HTTP 200 with empty content (project standard), never 404.

SERVICE ORCHESTRATION:
  1. [load] — QR-MDM-0002 FIND_BY_CRITERIA with SpecBuilder.build(request, ALLOWED_SORT_FIELDS)
              and PageableBuilder.from(request, ALLOWED_SORT_FIELDS)
  ALLOWED_SORT_FIELDS: typeCode, nameAr, nameEn, isActiveFl

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0002
  Table      : MDM_LOOKUP_TYPE
  Transaction: READ_ONLY

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_VIEW
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-002:END -->

<!-- API:API-MDM-003:START -->
### API-MDM-003 — Update LookupType
─────────────────────────────────────────────────────────────────
Endpoint         : PUT /api/v1/mdm/lookup-types/{id}
Controller       : MdmLookupController → method: updateLookupType
Service          : MdmLookupTypeService → method: update
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : id: Long
  Request Body   : LookupTypeUpdateRequest
    Fields:
      nameAr : String  REQUIRED — maps to NAME_AR (RULE-MDM-005)
      nameEn : String  REQUIRED — maps to NAME_EN (RULE-MDM-005)
      notes  : String  OPTIONAL — maps to NOTES
    Excluded fields : typeCode (RULE-MDM-002 — immutable), isActiveFl, audit fields, lookupTypePk

RESPONSE:
  Success code   : 200
  Response DTO   : LookupTypeResponse

VALIDATIONS:
  1. RULE-MDM-002 — Type code immutable: enforced by DTO shape (typeCode not accepted); if a client
     sends `typeCode` in the body it is silently ignored by the mapper — this is not itself an error
     condition unless a project-wide "unknown field" strictness policy exists (none declared here).
  2. RULE-MDM-005 — Both names mandatory (same as API-MDM-001).

ERRORS:
  ERR-0003 → RULE-MDM-002 → HTTP 409 — (reserved; used only if a stricter client contract later
             rejects a typeCode field explicitly present in the request body)
             Message-AR: لا يمكن تعديل رمز النوع بعد الإنشاء — القيمة مرجع تعتمد عليه موديولات أخرى.
             Message-EN: Type code cannot be changed after creation — other modules reference it.
  ERR-0004 → RULE-MDM-005 → HTTP 400
             Message-AR: الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي.
             Message-EN: Both the Arabic name and the English name are required.
  ERR-0006 → PLATFORM-STD → HTTP 404 — LookupType not found for given id.

SERVICE ORCHESTRATION:
  1. [load]     — QR-MDM-0001 FIND_ONE by PK; not found → ERR-0006
  2. [validate] — RULE-MDM-005 bean validation
  3. [persist]  — QR-MDM-0005 UPDATE (nameAr/nameEn/notes only)

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0001, QR-MDM-0005
  Table      : MDM_LOOKUP_TYPE
  Transaction: READ_WRITE

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_UPDATE
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-003:END -->

<!-- API:API-MDM-004:START -->
### API-MDM-004 — Deactivate LookupType (soft)
─────────────────────────────────────────────────────────────────
Endpoint         : DELETE /api/v1/mdm/lookup-types/{id}
Controller       : MdmLookupController → method: deactivateLookupType
Service          : MdmLookupTypeService → method: deactivate
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : id: Long

RESPONSE:
  Success code   : 204

VALIDATIONS:
  1. RULE-MDM-006 — Block deactivation while active LookupValues exist under this type.

ERRORS:
  ERR-0005 → RULE-MDM-006 → HTTP 409
             Message-AR: يُستبعَد العنصر بالتعطيل لا بالحذف؛ لا يمكن حذف نوع يحتوي قيمًا.
             Message-EN: Items are deactivated, not deleted; a type that still has values cannot be hard-deleted.
  ERR-0006 → PLATFORM-STD → HTTP 404 — LookupType not found for given id.

SERVICE ORCHESTRATION:
  1. [load]     — QR-MDM-0001 FIND_ONE by PK; not found → ERR-0006
  2. [validate] — QR-MDM-0006 COUNT active LookupValue WHERE lookupTypeFk = :id; count > 0 → ERR-0005
  3. [persist]  — QR-MDM-0007 UPDATE isActiveFl = 0 (no cascade to LookupValue rows — DRV-009)

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0001, QR-MDM-0006, QR-MDM-0007
  Table      : MDM_LOOKUP_TYPE (+ read-only usage count on MDM_LOOKUP_VALUE)
  Transaction: READ_WRITE

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_DELETE
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-004:END -->

<!-- API:API-MDM-005:START -->
### API-MDM-005 — Get LookupType by id
─────────────────────────────────────────────────────────────────
Endpoint         : GET /api/v1/mdm/lookup-types/{id}
Controller       : MdmLookupController → method: getLookupType
Service          : MdmLookupTypeService → method: getById
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : id: Long

RESPONSE:
  Success code   : 200
  Response DTO   : LookupTypeResponse

VALIDATIONS: None.
ERRORS:
  ERR-0006 → PLATFORM-STD → HTTP 404 — LookupType not found for given id.

SERVICE ORCHESTRATION:
  1. [load] — QR-MDM-0001 FIND_ONE by PK; not found → LocalizedException(NOT_FOUND, ERR-0006)

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0001
  Table      : MDM_LOOKUP_TYPE
  Transaction: READ_ONLY

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_VIEW
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-005:END -->

<!-- SUB:SVC-API-LOOKUP-TYPE:END -->

<!-- SUB:SVC-API-LOOKUP-VALUE:START -->
### SUB-GROUP: LOOKUP VALUE (Detail) — API-MDM-006..010

<!-- API:API-MDM-006:START -->
### API-MDM-006 — Create LookupValue
─────────────────────────────────────────────────────────────────
Endpoint         : POST /api/v1/mdm/lookup-types/{typeId}/values
Controller       : MdmLookupController → method: createLookupValue
Service          : MdmLookupValueService → method: create
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : typeId: Long
  Request Body   : LookupValueCreateRequest
    Fields:
      valueCode : String  REQUIRED — maps to VALUE_CODE, UNIQUE within typeId (RULE-MDM-003)
      nameAr    : String  REQUIRED — maps to NAME_AR (RULE-MDM-005)
      nameEn    : String  REQUIRED — maps to NAME_EN (RULE-MDM-005)
      sortOrder : Short   OPTIONAL — maps to SORT_ORDER
    Excluded fields : lookupValuePk (system), lookupTypeFk (from path param, never body),
                       isActiveFl, audit fields

RESPONSE:
  Success code   : 201
  Response DTO   : LookupValueResponse

VALIDATIONS:
  1. RULE-MDM-003 — Value code uniqueness within type.
  2. RULE-MDM-005 — Both names mandatory.

ERRORS:
  ERR-0007 → PLATFORM-STD → HTTP 404 — parent LookupType not found for given typeId (DRV-004).
  ERR-0008 → RULE-MDM-003 → HTTP 409
             Message-AR: رمز القيمة مستخدَم مسبقًا ضمن هذا النوع — اختر رمزًا فريدًا.
             Message-EN: This value code already exists under this type — choose a unique code.
  ERR-0009 → RULE-MDM-005 → HTTP 400
             Message-AR: الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي.
             Message-EN: Both the Arabic name and the English name are required.

SERVICE ORCHESTRATION:
  1. [load]     — QR-MDM-0011 EXISTS LookupType by PK (typeId); not found → ERR-0007
  2. [validate] — QR-MDM-0010 EXISTS by (typeId, valueCode); true → ERR-0008
  3. [validate] — RULE-MDM-005 bean validation → ERR-0009
  4. [persist]  — QR-MDM-0012 SAVE; lookupTypeFk = typeId; PK from SEQ_MDM_LOOKUP_VALUE

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0011, QR-MDM-0010, QR-MDM-0012
  Table      : MDM_LOOKUP_VALUE (+ existence check on MDM_LOOKUP_TYPE)
  Join       : NONE for the SAVE itself — QR-MDM-0011 is a separate EXISTS check, not a join (DRV-004)
  Transaction: READ_WRITE
  Sequence   : SEQ_MDM_LOOKUP_VALUE

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_CREATE
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-006:END -->

<!-- API:API-MDM-007:START -->
### API-MDM-007 — List LookupValues under type
─────────────────────────────────────────────────────────────────
Endpoint         : GET /api/v1/mdm/lookup-types/{typeId}/values
Controller       : MdmLookupController → method: listLookupValues
Service          : MdmLookupValueService → method: search
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : typeId: Long
  Query Params   : isActiveFl (Boolean, optional — default true when omitted, DRV-011),
                   page (int), size (int), sortBy, sortDir

RESPONSE:
  Success code   : 200
  Response DTO   : Page<LookupValueResponse>
  Paginated      : YES

VALIDATIONS: None beyond parent existence.
ERRORS:
  ERR-0007 → PLATFORM-STD → HTTP 404 — parent LookupType not found for given typeId (DRV-004).

SERVICE ORCHESTRATION:
  1. [load] — QR-MDM-0011 EXISTS LookupType by PK; not found → ERR-0007
  2. [load] — QR-MDM-0009 FIND_BY_CRITERIA WHERE lookupTypeFk = :typeId
  ALLOWED_SORT_FIELDS: valueCode, nameAr, nameEn, sortOrder, isActiveFl

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0011, QR-MDM-0009
  Table      : MDM_LOOKUP_VALUE
  Transaction: READ_ONLY

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_VIEW
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-007:END -->

<!-- API:API-MDM-008:START -->
### API-MDM-008 — Update LookupValue
─────────────────────────────────────────────────────────────────
Endpoint         : PUT /api/v1/mdm/lookup-values/{id}
Controller       : MdmLookupController → method: updateLookupValue
Service          : MdmLookupValueService → method: update
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : id: Long
  Request Body   : LookupValueUpdateRequest
    Fields:
      nameAr    : String  REQUIRED
      nameEn    : String  REQUIRED
      sortOrder : Short   OPTIONAL
    Excluded fields : valueCode (RULE-MDM-004), lookupTypeFk (immutable parent), isActiveFl, audit fields

RESPONSE:
  Success code   : 200
  Response DTO   : LookupValueResponse

VALIDATIONS:
  1. RULE-MDM-004 — Value code immutable (enforced by DTO shape).
  2. RULE-MDM-005 — Both names mandatory.

ERRORS:
  ERR-0010 → RULE-MDM-004 → HTTP 409 — (reserved, same basis as ERR-0003)
             Message-AR: لا يمكن تعديل رمز القيمة بعد الإنشاء — القيمة مرجع تخزّنه موديولات أخرى.
             Message-EN: Value code cannot be changed after creation — other modules store it.
  ERR-0011 → RULE-MDM-005 → HTTP 400
             Message-AR: الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي.
             Message-EN: Both the Arabic name and the English name are required.
  ERR-0012 → PLATFORM-STD → HTTP 404 — LookupValue not found for given id.

SERVICE ORCHESTRATION:
  1. [load]     — QR-MDM-0008 FIND_ONE by PK; not found → ERR-0012
  2. [validate] — RULE-MDM-005 bean validation
  3. [persist]  — QR-MDM-0013 UPDATE (nameAr/nameEn/sortOrder only)

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0008, QR-MDM-0013
  Table      : MDM_LOOKUP_VALUE
  Transaction: READ_WRITE

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_UPDATE
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-008:END -->

<!-- API:API-MDM-009:START -->
### API-MDM-009 — Deactivate LookupValue (soft)
─────────────────────────────────────────────────────────────────
Endpoint         : DELETE /api/v1/mdm/lookup-values/{id}
Controller       : MdmLookupController → method: deactivateLookupValue
Service          : MdmLookupValueService → method: deactivate
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : id: Long

RESPONSE:
  Success code   : 204

VALIDATIONS: None beyond existence (DRV-008 — no child/usage check for LookupValue).
ERRORS:
  ERR-0012 → PLATFORM-STD → HTTP 404 — LookupValue not found for given id.

SERVICE ORCHESTRATION:
  1. [load]    — QR-MDM-0008 FIND_ONE by PK; not found → ERR-0012
  2. [persist] — QR-MDM-0014 UPDATE isActiveFl = 0

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0008, QR-MDM-0014
  Table      : MDM_LOOKUP_VALUE
  Transaction: READ_WRITE

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_DELETE
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-009:END -->

<!-- API:API-MDM-010:START -->
### API-MDM-010 — Get LookupValue by id
─────────────────────────────────────────────────────────────────
Endpoint         : GET /api/v1/mdm/lookup-values/{id}
Controller       : MdmLookupController → method: getLookupValue
Service          : MdmLookupValueService → method: getById
─────────────────────────────────────────────────────────────────
REQUEST:
  Path Params    : id: Long

RESPONSE:
  Success code   : 200
  Response DTO   : LookupValueResponse

VALIDATIONS: None.
ERRORS:
  ERR-0012 → PLATFORM-STD → HTTP 404 — LookupValue not found for given id.

SERVICE ORCHESTRATION:
  1. [load] — QR-MDM-0008 FIND_ONE by PK; not found → ERR-0012

REPOSITORY OPERATION:
  QR-ID      : QR-MDM-0008
  Table      : MDM_LOOKUP_VALUE
  Transaction: READ_ONLY

SECURITY:
  Screen     : SCR-MDM-001
  Permission : PERM_MDM_LOOKUP_VIEW
─────────────────────────────────────────────────────────────────
<!-- API:API-MDM-010:END -->

<!-- SUB:SVC-API-LOOKUP-VALUE:END -->

<!-- SUB:SVC-API-CONSUMPTION:START -->
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

<!-- SUB:SVC-API-CONSUMPTION:END -->

**API Governance compliance for this phase:**
- RULE-ERR-CARRY: every RULE-ID listed under each API's Validations has a matching ERR-ID under that same API's Errors. ✓ (verified per block above)
- RULE-PLATFORM-ERR: all 404s use RULE-ID = PLATFORM-STD with a DRV-ID (DRV-004 for parent-existence 404s; standard entity-not-found otherwise, logged as project-standard, no dedicated DRV needed beyond CORE's QRC default). ✓
- RULE-REPO-DRV: no EAGER fetch, no compound UPDATE, one native-adjacent join (QR-MDM-0015) — carries DRV-005. ✓

<!-- PHASE:SVC-API:END -->

<!-- PHASE:DOC:START -->
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
<!-- PHASE:DOC:END -->

<!-- PHASE:INT-C:START -->
## PHASE INT-C — Integration Contract Specifications

## INT-C SUMMARY — Master Data (MDM) — PLAN-ID: PLAN-MDM-001
══════════════════════════════════════════════════════════════════════════
None. db-script-MDM.md §3 XM Register is empty — MDM has zero cross-module dependencies.
MDM is a pure provider: consumers call API-MDM-011 and store the returned `code` as a SOFT
reference (no FK, no XM-ID, in either direction). No `createdBy` SEC read is modeled as an XM
either — it is the platform-standard audit pattern (SOFT, no FK), consistent with CU/FILE/NOTIF.

INBOUND XM STUBS: None declared. srs-MDM.md §A7 and module-registry-MDM.md list FILE and NOTIF
as future INTEGRATION CANDIDATES (repointing their private LOVs onto MDM), but that repointing is
explicitly a separate, future, governed amendment on FILE's/NOTIF's own artifacts — not an XM this
plan owns or stubs (srs-MDM §A2 "ما لا يشمله هذا الموديول").
══════════════════════════════════════════════════════════════════════════

**INT-C GATE CHECK:**
[✓] All XM-IDs from DB Script XM Register accounted for (none exist)
[✓] Classification declared for each XM-ID (n/a)
[✓] All DEFERRED have unblock condition (none DEFERRED)
[✓] No new XM-IDs invented
[✓] Open RXEs acknowledged (none open for MDM — see XM-RESOLUTION-EVENT-PROTOCOL.md; no RXE raised)
[✓] Inbound XM stubs use INBOUND-STUB notation (none needed — no inbound stub declared)
INT-C Gate: PASSED ✓
<!-- PHASE:INT-C:END -->

<!-- PHASE:INT-R:START -->
## PHASE INT-R — Runtime Activation Status

## INT-R STATUS — Master Data (MDM) — PLAN-ID: PLAN-MDM-001
══════════════════════════════════════════════════════════════════════════
None. No XM-IDs exist for this module (see PHASE INT-C). No runtime activation status to declare.
══════════════════════════════════════════════════════════════════════════
<!-- PHASE:INT-R:END -->

<!-- PHASE:SEC-BE:START -->
## PHASE SEC-BE — Backend Security Specifications

### SEC-BE — SCR-MDM-001 — إدارة القوائم المرجعية (Reference Data Lookup Management)
─────────────────────────────────────────────────────────────────
API-level enforcement:
  API-MDM-001..010 each require permission verification before processing (see SECURITY block
  in each API contract above). API-MDM-011 is the one exception — auth-only, not screen-gated
  (DRV-006).

EXCEPTION module scope: None — Security (SEC) is not an EXCEPTION module; standard integration.
─────────────────────────────────────────────────────────────────

SECURITY SEED DATA REQUIREMENTS:

Tier 1 — Module grant (two-tier RBAC, per domain-profile.md v3 / SEC two-tier RBAC amendment):
  SEC_MODULE   : one row for this module —
                 moduleCode = MDM, moduleName(AR/EN) = البيانات المرجعية / Master Data
  SEC_ROLE_MODULE : grant R1 (مدير البيانات المرجعية / Master Data Administrator) → MDM
                     grant R2 (مدير النظام / System Administrator) → MDM
                     (module grant is a prerequisite for any screen permission inside it —
                      no orphan screen permission, per domain-profile.md two-tier rule)

Tier 2 — Screen registration:
  SEC_PAGES : one row for the composite screen —
              page_code = MDM_LOOKUP, page_name = إدارة القوائم المرجعية, module_fk = MDM
  PERMISSIONS (4 rows, CORE-9):
    ────────────────────────────────────────────────────────
    Permission Name              │ Roles Assigned
    ─────────────────────────────┼──────────────────────────
    PERM_MDM_LOOKUP_VIEW         │ R1, R2
    PERM_MDM_LOOKUP_CREATE       │ R1, R2
    PERM_MDM_LOOKUP_UPDATE       │ R1, R2
    PERM_MDM_LOOKUP_DELETE       │ R1, R2
    ────────────────────────────────────────────────────────
  Column names for SEC_MODULE / SEC_ROLE_MODULE / SEC_PAGES / PERMISSIONS come from
  db-script-SEC.md (owning module) — not redefined here; MDM only supplies the seed row values.

SEC-BE Governance Rules:
  SEC-IMPL-RULE-1 — Every SCR-ID (here: SCR-MDM-001) has permission verification enforced at
                    the API level — API-MDM-001..010, no exceptions.
  SEC-IMPL-RULE-3 — HTTP 403 responses mapped via LocalizedException.
  SEC-IMPL-RULE-4 — SCR-MDM-001 verified in SEC_PAGES before launch.

Note: canView/canCreate/canEdit/canDelete UI-level behavior is out of scope for this backend-only
pass — specified later in PROJECT-3-FRONTEND-ENGINE.md Phase SEC-FE, consuming this same seed data.
<!-- PHASE:SEC-BE:END -->

<!-- PHASE:ALIGN-BE:START -->
## ALIGN-BE GATE — Master Data (MDM) — PLAN-ID: PLAN-MDM-001
═══════════════════════════════════════════════════════════════════════════

TRACEABILITY CHECKS                                        │ Status
───────────────────────────────────────────────────────────┼──────────────
All FIELD-IDs used in phases appear in Plan Index          │ ✓ (14/14)
All API-IDs used in phases appear in Plan Index            │ ✓ (11/11)
All RULE-IDs used in phases appear in Plan Index           │ ✓ (6/6)
All ERR-IDs used in Error Catalog appear correctly         │ ✓ (12/12)
All QR-IDs in QRC appear in Plan Index QRC Summary         │ ✓ (15/15)
Derivation Log complete — no undocumented inferences       │ ✓ (11 DRV entries — see Derivation Log)
DB Structural Alignment confirms field coverage            │ ✓ (DB Alignment Manifest — 14/14 ✓)
───────────────────────────────────────────────────────────┼──────────────
BUSINESS CODE CHECKS                                       │ Status
───────────────────────────────────────────────────────────┼──────────────
Business Code excluded from POST/PUT request bodies        │ ✓ n/a — BC-RULE-0 = NO, no Business Code field exists
Business Code always present in GET/response DTOs          │ ✓ n/a
───────────────────────────────────────────────────────────┼──────────────
LOCALIZATION CHECKS                                        │ Status
───────────────────────────────────────────────────────────┼──────────────
All RULE-IDs have Message-AR defined                       │ ✓ (6/6)
All API error responses: messageAr + messageEn             │ ✓ (12/12 ERR-IDs)
───────────────────────────────────────────────────────────┼──────────────
SECURITY CHECKS                                            │ Status
───────────────────────────────────────────────────────────┼──────────────
Every API-ID serving a screen has permission declared      │ ✓ (API-MDM-001..010; API-MDM-011 intentionally exempt — DRV-006)
Every SCR-ID has SEC-BE block                              │ ✓ (SCR-MDM-001)
───────────────────────────────────────────────────────────┼──────────────
QUERY REFERENCE CATALOG CHECKS                             │ Status
───────────────────────────────────────────────────────────┼──────────────
Every API-ID with DB operation has QR-ID in QRC            │ ✓
Every QR-ID has agent-reference warning label               │ ✓ (SECTION B header + per-entry note)
No QR entry references ENUM for LOV fields                 │ ✓ n/a — no LOV field in this module
No QR entry joins to a lookups table                        │ ✓ — QR-MDM-0015's join is intra-module master↔detail, not a join "to get a display name from a lookups table" (this module IS the lookup provider)
Every QR-ID states exact sequence name (not placeholder)   │ ✓ (SEQ_MDM_LOOKUP_TYPE / SEQ_MDM_LOOKUP_VALUE)
───────────────────────────────────────────────────────────┼──────────────
CROSS-MODULE DEPENDENCY CHECKS                             │ Status
───────────────────────────────────────────────────────────┼──────────────
All DEFERRED items (⏸) have XM-ID + workarounds             │ ✓ n/a — 0 XM-IDs, 0 DEFERRED
All OQ references point to valid OQ-IDs in OQ Log          │ ✓ n/a — 0 open OQ references
Inbound XM stubs use INBOUND-STUB notation                 │ ✓ n/a — none declared
───────────────────────────────────────────────────────────┼──────────────
ARTIFACT BINDING CHECKS (Section 2A compliance)             │ Status
───────────────────────────────────────────────────────────┼──────────────
No placeholder [TABLE_NAME] in any phase                   │ ✓
No placeholder [LOOKUP_CODE] in any phase                  │ ✓ n/a
No placeholder [SEQ_NAME] — all sequences are exact         │ ✓
No RULE block shows "see SRS" — all text is inline         │ ✓
Every LOV-ID has exact LOOKUP_CODE bound from SRS           │ ✓ n/a
Every sequence name matches SEQ_[TABLE] from db-script      │ ✓
Every column name traces to a DBF-ID in DB Traceability    │ ✓ (14/14; audit columns explicitly no-DBF-ID per source)
Every Message-AR is exact text — not paraphrase             │ ✓
Business Code format stated explicitly                      │ ✓ n/a (BC-RULE-0 = NO, stated explicitly as such)
DB Alignment Manifest: 5 columns only                       │ ✓
───────────────────────────────────────────────────────────┼──────────────
PLAN COMPLETENESS CHECKS                                   │ Status
───────────────────────────────────────────────────────────┼──────────────
Canonical architecture declared in PHASE CORE               │ ✓
Domain behavior placement declared in PHASE CORE            │ ✓ (embedded in Entity methods — DRV-001)
Entity inheritance declared per module type                 │ ✓ (AuditableEntity, both entities)
No orgUnitId in any DTO described in the plan                │ ✓
No audit fields in any CreateRequest/UpdateRequest           │ ✓
Error signaling strategy declared (LocalizedException)       │ ✓
All ERR-IDs have 4-registration points declared              │ ✓ (CORE phase, project-standard)
All search operations declare ALLOWED_SORT_FIELDS            │ ✓ (API-MDM-002, API-MDM-007)
Empty search result → HTTP 200 declared (not HTTP 404)       │ ✓ (API-MDM-002, API-MDM-007, API-MDM-011)
Pre-deactivation usage check declared per deactivate op       │ ✓ (API-MDM-004; API-MDM-009 explicitly exempted — DRV-008)
Inbound XM stubs use INBOUND-STUB notation                   │ ✓ n/a
═══════════════════════════════════════════════════════════════════════════
ALIGN-BE GATE RESULT: PASSED ✓
Auto-correction applied: None required — no ✗ encountered during generation.
═══════════════════════════════════════════════════════════════════════════

**Table 1 — Entity & Field Coverage:**
| ENTITY-ID / FIELD-ID | DATA+DOM | SVC+API | QR-ID | XM-ID | Status |
|---|---|---|---|---|---|
| ENTITY-MDM-001 | ✓ | ✓ | QR-MDM-0001..0005,0007 | — | ✓ |
| ENTITY-MDM-002 | ✓ | ✓ | QR-MDM-0008..0014 | — | ✓ |
| FIELD-0001..0006 | ✓ | ✓ | (via entity ops) | — | ✓ |
| FIELD-0007..0014 | ✓ | ✓ | (via entity ops) | — | ✓ |

**Table 2 — Validations Coverage:**
| RULE-ID | SVC+API | ERR-ID | Status |
|---|---|---|---|
| RULE-MDM-001 | ✓ | ERR-0001 | ✓ |
| RULE-MDM-002 | ✓ | ERR-0003 | ✓ |
| RULE-MDM-003 | ✓ | ERR-0008 | ✓ |
| RULE-MDM-004 | ✓ | ERR-0010 | ✓ |
| RULE-MDM-005 | ✓ | ERR-0002, ERR-0004, ERR-0009, ERR-0011 | ✓ |
| RULE-MDM-006 | ✓ | ERR-0005 | ✓ |

**Table 3 — XM Dependency Gate:** None — 0 XM-IDs in this module.
<!-- PHASE:ALIGN-BE:END -->

---

## SECTION A — ERROR CATALOG (CANONICAL)

Canonical location: this table (OPTION A). SVC+API phase blocks reference it by ERR-ID only.

## ERROR CATALOG — Master Data (MDM) — PLAN-ID: PLAN-MDM-001
══════════════════════════════════════════════════════════════════════════════════
| ERR-ID | RULE-ID | API-ID | HTTP | Trigger | Message-AR | Message-EN |
|---|---|---|---|---|---|---|
| ERR-0001 | RULE-MDM-001 | API-MDM-001 | 409 | Duplicate typeCode on create | رمز النوع مستخدَم مسبقًا — اختر رمزًا فريدًا. | This type code already exists — choose a unique code. |
| ERR-0002 | RULE-MDM-005 | API-MDM-001 | 400 | Missing nameAr/nameEn (LookupType create) | الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي. | Both the Arabic name and the English name are required. |
| ERR-0003 | RULE-MDM-002 | API-MDM-003 | 409 | Attempt to modify typeCode | لا يمكن تعديل رمز النوع بعد الإنشاء — القيمة مرجع تعتمد عليه موديولات أخرى. | Type code cannot be changed after creation — other modules reference it. |
| ERR-0004 | RULE-MDM-005 | API-MDM-003 | 400 | Missing nameAr/nameEn (LookupType update) | الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي. | Both the Arabic name and the English name are required. |
| ERR-0005 | RULE-MDM-006 | API-MDM-004 | 409 | Deactivate a type that still has active values | يُستبعَد العنصر بالتعطيل لا بالحذف؛ لا يمكن حذف نوع يحتوي قيمًا. | Items are deactivated, not deleted; a type that still has values cannot be hard-deleted. |
| ERR-0006 | PLATFORM-STD | API-MDM-003, API-MDM-004, API-MDM-005 | 404 | LookupType not found by id | النوع غير موجود. | LookupType not found. |
| ERR-0007 | PLATFORM-STD | API-MDM-006, API-MDM-007 | 404 | Parent LookupType not found for given typeId | نوع القائمة الأب غير موجود. | Parent LookupType not found. |
| ERR-0008 | RULE-MDM-003 | API-MDM-006 | 409 | Duplicate valueCode within type on create | رمز القيمة مستخدَم مسبقًا ضمن هذا النوع — اختر رمزًا فريدًا. | This value code already exists under this type — choose a unique code. |
| ERR-0009 | RULE-MDM-005 | API-MDM-006 | 400 | Missing nameAr/nameEn (LookupValue create) | الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي. | Both the Arabic name and the English name are required. |
| ERR-0010 | RULE-MDM-004 | API-MDM-008 | 409 | Attempt to modify valueCode | لا يمكن تعديل رمز القيمة بعد الإنشاء — القيمة مرجع تخزّنه موديولات أخرى. | Value code cannot be changed after creation — other modules store it. |
| ERR-0011 | RULE-MDM-005 | API-MDM-008 | 400 | Missing nameAr/nameEn (LookupValue update) | الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي. | Both the Arabic name and the English name are required. |
| ERR-0012 | PLATFORM-STD | API-MDM-008, API-MDM-009, API-MDM-010 | 404 | LookupValue not found by id | القيمة غير موجودة. | LookupValue not found. |
══════════════════════════════════════════════════════════════════════════════════
Total Errors: 12

Platform-standard entries (ERR-0006, ERR-0007, ERR-0012) carry RULE-ID = PLATFORM-STD per
RULE-PLATFORM-ERR — see Derivation Log DRV-004 for the parent-existence pair (ERR-0007);
ERR-0006/ERR-0012 are the standard "entity not found by id" case (project-standard, no
dedicated DRV beyond the CORE FIND_ONE default already declared in PROJECT-3-BACKEND-ENGINE.md §7.5).

---

## SECTION B — QUERY REFERENCE CATALOG (FULL — AGENT REFERENCE) — Master Data (MDM)

```
╔══════════════════════════════════════════════════════════════════╗
║  ⚠ AGENT REFERENCE ONLY — ALL ENTRIES MUST BE REWRITTEN         ║
║  Rewrite every query using actual JPA entity class names, actual  ║
║  mapped field property names, and the project's own pagination/   ║
║  query strategy. These entries express INTENT only.               ║
╚══════════════════════════════════════════════════════════════════╝
```

QR-MDM-0001 — Find LookupType by id
──────────────────────────────────────────────────────────────────
Phase: DATA+DOM │ API-ID: API-MDM-003, API-MDM-004, API-MDM-005 │ Entity: ENTITY-MDM-001 │ Operation: FIND_ONE
Intent: Load a single LookupType for read, update, or pre-deactivate checks.
Logical Specification:
  SELECT *
  FROM   MDM_LOOKUP_TYPE
  WHERE  ID = :id
Join Justification: NONE required.
Transaction: READ_ONLY. Pagination: NO.
Result shape: full entity or LocalizedException(NOT_FOUND, ERR-0006).
──────────────────────────────────────────────────────────────────

QR-MDM-0002 — Search LookupTypes
──────────────────────────────────────────────────────────────────
Phase: DATA+DOM │ API-ID: API-MDM-002 │ Entity: ENTITY-MDM-001 │ Operation: FIND_BY_CRITERIA
Intent: Power the admin search grid — filter by code/name/active flag.
Logical Specification:
  SELECT *
  FROM   MDM_LOOKUP_TYPE
  WHERE  (:typeCode IS NULL OR TYPE_CODE LIKE %:typeCode%)
  AND    (:nameAr IS NULL OR NAME_AR LIKE %:nameAr%)
  AND    (:nameEn IS NULL OR NAME_EN LIKE %:nameEn%)
  AND    IS_ACTIVE_FL = COALESCE(:isActiveFl, 1)
  ORDER BY [sortBy] [ASC/DESC]
  LIMIT :size OFFSET :page*size
Join Justification: NONE required.
Transaction: READ_ONLY. Pagination: YES. Filter fields: typeCode(LIKE), nameAr(LIKE), nameEn(LIKE), isActiveFl(EXACT, default 1 — DRV-011).
Result shape: paginated projection of all fields.
──────────────────────────────────────────────────────────────────

QR-MDM-0003 — Exists LookupType by typeCode
──────────────────────────────────────────────────────────────────
Phase: DATA+DOM │ API-ID: API-MDM-001 │ Entity: ENTITY-MDM-001 │ Operation: EXISTS
Intent: Enforce RULE-MDM-001 uniqueness before insert.
Logical Specification:
  SELECT COUNT(*) > 0
  FROM   MDM_LOOKUP_TYPE
  WHERE  TYPE_CODE = :typeCode
Join Justification: NONE required. Transaction: READ_ONLY. Pagination: NO.
──────────────────────────────────────────────────────────────────

QR-MDM-0004 — Save LookupType
──────────────────────────────────────────────────────────────────
Phase: DATA+DOM │ API-ID: API-MDM-001 │ Entity: ENTITY-MDM-001 │ Operation: SAVE
Intent: Persist a newly-created LookupType.
Logical Specification:
  INSERT INTO MDM_LOOKUP_TYPE (ID, TYPE_CODE, NAME_AR, NAME_EN, IS_ACTIVE_FL, NOTES, CREATED_BY, CREATED_AT)
  VALUES (nextval('SEQ_MDM_LOOKUP_TYPE'), :typeCode, :nameAr, :nameEn, 1, :notes, [audit], [audit])
Transaction: READ_WRITE. Sequence: SEQ_MDM_LOOKUP_TYPE.NEXTVAL — exact name from db-script-MDM.md BLOCK 1.
──────────────────────────────────────────────────────────────────

QR-MDM-0005 — Update LookupType
──────────────────────────────────────────────────────────────────
Phase: DATA+DOM │ API-ID: API-MDM-003 │ Entity: ENTITY-MDM-001 │ Operation: UPDATE
Intent: Persist edits to nameAr/nameEn/notes only — typeCode is immutable (RULE-MDM-002).
Logical Specification:
  UPDATE MDM_LOOKUP_TYPE
  SET    NAME_AR = :nameAr, NAME_EN = :nameEn, NOTES = :notes, UPDATED_BY = [audit], UPDATED_AT = [audit]
  WHERE  ID = :id
Transaction: READ_WRITE.
──────────────────────────────────────────────────────────────────

QR-MDM-0006 — Count active LookupValues under a type (usage check)
──────────────────────────────────────────────────────────────────
Phase: SVC+API │ API-ID: API-MDM-004 │ Entity: ENTITY-MDM-002 │ Operation: COUNT
Intent: Enforce RULE-MDM-006 — block deactivating a LookupType that still has active values.
Logical Specification:
  SELECT COUNT(*)
  FROM   MDM_LOOKUP_VALUE
  WHERE  LOOKUP_TYPE_FK = :id
  AND    IS_ACTIVE_FL = 1
Join Justification: NONE required (single table, filtered by FK column). Transaction: READ_ONLY.
Result shape: count only; count > 0 → ERR-0005.
──────────────────────────────────────────────────────────────────

QR-MDM-0007 — Soft-deactivate LookupType
──────────────────────────────────────────────────────────────────
Phase: DATA+DOM │ API-ID: API-MDM-004 │ Entity: ENTITY-MDM-001 │ Operation: UPDATE
Intent: Flip isActiveFl to 0 — no cascade to LookupValue rows (DRV-009).
Logical Specification:
  UPDATE MDM_LOOKUP_TYPE
  SET    IS_ACTIVE_FL = 0, UPDATED_BY = [audit], UPDATED_AT = [audit]
  WHERE  ID = :id
Transaction: READ_WRITE.
──────────────────────────────────────────────────────────────────

QR-MDM-0008 — Find LookupValue by id
──────────────────────────────────────────────────────────────────
Phase: DATA+DOM │ API-ID: API-MDM-008, API-MDM-009, API-MDM-010 │ Entity: ENTITY-MDM-002 │ Operation: FIND_ONE
Logical Specification:
  SELECT * FROM MDM_LOOKUP_VALUE WHERE ID = :id
Join Justification: NONE required. Transaction: READ_ONLY.
Result shape: full entity or LocalizedException(NOT_FOUND, ERR-0012).
──────────────────────────────────────────────────────────────────

QR-MDM-0009 — List LookupValues under a type
──────────────────────────────────────────────────────────────────
Phase: DATA+DOM │ API-ID: API-MDM-007 │ Entity: ENTITY-MDM-002 │ Operation: FIND_BY_CRITERIA
Logical Specification:
  SELECT *
  FROM   MDM_LOOKUP_VALUE
  WHERE  LOOKUP_TYPE_FK = :typeId
  AND    IS_ACTIVE_FL = COALESCE(:isActiveFl, 1)
  ORDER BY [sortBy] [ASC/DESC]
  LIMIT :size OFFSET :page*size
Join Justification: NONE required (filter is a direct FK column, not a related entity). Transaction: READ_ONLY. Pagination: YES.
Filter fields: isActiveFl(EXACT, default 1 — DRV-011).
──────────────────────────────────────────────────────────────────

QR-MDM-0010 — Exists LookupValue by (lookupTypeFk, valueCode)
──────────────────────────────────────────────────────────────────
Phase: DATA+DOM │ API-ID: API-MDM-006 │ Entity: ENTITY-MDM-002 │ Operation: EXISTS
Intent: Enforce RULE-MDM-003 — uniqueness scoped to the parent type.
Logical Specification:
  SELECT COUNT(*) > 0
  FROM   MDM_LOOKUP_VALUE
  WHERE  LOOKUP_TYPE_FK = :typeId AND VALUE_CODE = :valueCode
Transaction: READ_ONLY.
──────────────────────────────────────────────────────────────────

QR-MDM-0011 — Exists LookupType by id (parent existence check)
──────────────────────────────────────────────────────────────────
Phase: SVC+API │ API-ID: API-MDM-006, API-MDM-007 │ Entity: ENTITY-MDM-001 │ Operation: EXISTS
Intent: DRV-004 — confirm the nested parent exists before proceeding; 404 (ERR-0007) if not.
Logical Specification:
  SELECT COUNT(*) > 0 FROM MDM_LOOKUP_TYPE WHERE ID = :typeId
Transaction: READ_ONLY.
──────────────────────────────────────────────────────────────────

QR-MDM-0012 — Save LookupValue
──────────────────────────────────────────────────────────────────
Phase: DATA+DOM │ API-ID: API-MDM-006 │ Entity: ENTITY-MDM-002 │ Operation: SAVE
Logical Specification:
  INSERT INTO MDM_LOOKUP_VALUE (ID, LOOKUP_TYPE_FK, VALUE_CODE, NAME_AR, NAME_EN, SORT_ORDER, IS_ACTIVE_FL, CREATED_BY, CREATED_AT)
  VALUES (nextval('SEQ_MDM_LOOKUP_VALUE'), :typeId, :valueCode, :nameAr, :nameEn, :sortOrder, 1, [audit], [audit])
Transaction: READ_WRITE. Sequence: SEQ_MDM_LOOKUP_VALUE.NEXTVAL — exact name from db-script-MDM.md BLOCK 1.
──────────────────────────────────────────────────────────────────

QR-MDM-0013 — Update LookupValue
──────────────────────────────────────────────────────────────────
Phase: DATA+DOM │ API-ID: API-MDM-008 │ Entity: ENTITY-MDM-002 │ Operation: UPDATE
Logical Specification:
  UPDATE MDM_LOOKUP_VALUE
  SET    NAME_AR = :nameAr, NAME_EN = :nameEn, SORT_ORDER = :sortOrder, UPDATED_BY = [audit], UPDATED_AT = [audit]
  WHERE  ID = :id
Transaction: READ_WRITE.
──────────────────────────────────────────────────────────────────

QR-MDM-0014 — Soft-deactivate LookupValue
──────────────────────────────────────────────────────────────────
Phase: DATA+DOM │ API-ID: API-MDM-009 │ Entity: ENTITY-MDM-002 │ Operation: UPDATE
Logical Specification:
  UPDATE MDM_LOOKUP_VALUE
  SET    IS_ACTIVE_FL = 0, UPDATED_BY = [audit], UPDATED_AT = [audit]
  WHERE  ID = :id
Transaction: READ_WRITE.
──────────────────────────────────────────────────────────────────

QR-MDM-0015 — Find active values by parent typeCode (platform consumption)
──────────────────────────────────────────────────────────────────
Phase: SVC+API │ API-ID: API-MDM-011 │ Entity: ENTITY-MDM-001 + ENTITY-MDM-002 │ Operation: FIND_BY_CRITERIA
Intent: DRV-005 — the public provider endpoint takes a typeCode (parent natural key), so an
  intra-module join to the master table is required to filter by the parent's code and active flag.
Logical Specification:
  SELECT lv.VALUE_CODE, lv.NAME_AR, lv.NAME_EN, lv.SORT_ORDER
  FROM   MDM_LOOKUP_VALUE lv
  JOIN   MDM_LOOKUP_TYPE  lt ON lv.LOOKUP_TYPE_FK = lt.ID
  WHERE  lt.TYPE_CODE = :typeCode
  AND    lt.IS_ACTIVE_FL = 1
  AND    lv.IS_ACTIVE_FL = 1
  ORDER BY lv.SORT_ORDER
Join Justification: Required — DRV-005 (intra-module master↔detail join, not a "lookups table" join
  in the forbidden sense — this module IS the lookup provider). Transaction: READ_ONLY. Pagination: NO.
Result shape: projection — valueCode/nameAr/nameEn/sortOrder only (LookupValueLite).
──────────────────────────────────────────────────────────────────
```

---

## SECTION C — REGISTRY UPDATE BLOCK

## REGISTRY UPDATE — 2026-09-04
────────────────────────────────────────────────────────────────
Source          : Project 3.1 — PASS 1 (Backend)
Feature Code    : MDM-001
DBS-ID          : DBS-MDM-001
Plan ID         : PLAN-MDM-001
────────────────────────────────────────────────────────────────
New Entities    : None (already registered by P2 — ENTITY-MDM-001, ENTITY-MDM-002)
New Tables      : None (already registered by P2 — MDM_LOOKUP_TYPE, MDM_LOOKUP_VALUE)
New Lookups     : None (MDM is itself the shared lookup provider — no LOV-ID minted)
New APIs        : API-MDM-001, API-MDM-002, API-MDM-003, API-MDM-004, API-MDM-005, API-MDM-006,
                  API-MDM-007, API-MDM-008, API-MDM-009, API-MDM-010, API-MDM-011 (11 total)
FIELD-IDs Assigned : FIELD-0001 .. FIELD-0014 (14 total)
ERR-IDs Assigned   : ERR-0001 .. ERR-0012 (12 total)
QR-IDs Created  : QR-MDM-0001 .. QR-MDM-0015 (15 total)
XM-IDs Open     : None
OQ-IDs Open     : None
Gate Status     : ALIGN-BE PASSED ✓
Next Action     : Trigger Project 4.1 — Backend Audit Gate. (Test Generation Engine may run
                  separately, outside the pipeline, whenever test artifacts are needed for MDM.)
────────────────────────────────────────────────────────────────
Table Registry rows to add (project-registry.md §7): already present (added at P2/MODE 1.5).
Global XM Index rows to add (project-registry.md §8): none.
Pipeline Status Grid: MDM · P3.1 = done.
────────────────────────────────────────────────────────────────

---

## AGENT HANDOFF SUMMARY (BACKEND) — Master Data (MDM)

### What the Agent Receives
```
✓ backend-execution-plan-MDM.md — this file (complete backend specification)
✓ srs-MDM.md v1.3 — functional requirements (read for clarification)
✓ db-script-MDM.md / DBS-MDM-001 — database DDL (actual table and column names)
✓ OQ Log — 0 open items for MDM
```

### Agent Reading Order
```
1. PLAN HEADER + PLAN INDEX — full scope (2 entities, 11 APIs, 6 rules, 0 XM)
2. DB ALIGNMENT MANIFEST — FIELD-ID → DBF-ID mapping (14/14 aligned)
3. OQ LOG — empty, nothing blocked
4. PHASE CORE — architectural patterns (domain-in-Entity, two controllers per DRV-007)
5. PHASE DATA+DOM — entity structure and domain rules
6. PHASE SVC+API — API contracts and orchestration
7. PHASE DOC — internal contract self-check (informational only)
8. PHASE INT-C + INT-R — both empty (0 XM) — nothing to integrate
9. PHASE SEC-BE — security seed data (Tier 1 SEC_MODULE grant + Tier 2 4 permissions)
10. SECTION B — Query Reference Catalog — rewrite every entry before use
11. SECTION A — Error Catalog — use ERR-IDs in all error handling code
12. AFTER implementation: run api-doc-generator — required before PASS 2 (frontend)
```

### Agent Implementation Rules — Quick Reference
```
QRC — NEVER copy-paste SECTION B entries as production code. Read intent → rewrite using
      actual entity/field names. Use SpecBuilder.build() + PageableBuilder.from() for search.
DEFERRED XM — not applicable, 0 XM-IDs in this module.
OPEN QUESTIONS — not applicable, 0 open OQ.
```

### Backend Plan Completeness Self-Check
```
[✓] PHASE CORE: canonical architecture declared (backend + frontend layers)
[✓] PHASE CORE: domain behavior placement declared (embedded in Entity methods — DRV-001)
[✓] PHASE CORE: entity inheritance declared per module type (AuditableEntity, both entities)
[✓] PHASE CORE: error signaling strategy declared (LocalizedException)
[✓] PHASE CORE: transaction scope declared
[✓] Every ENTITY-ID has complete field table with DBF-ID bindings
[✓] Every RULE-ID has full text (Statement + Message-AR + Message-EN)
[✓] Every RULE-ID has "Owned by: domain layer" declared
[✓] Every API-ID: Errors field covers all RULE-IDs in Validations
[✓] Every platform-standard ERR-ID (404/500): RULE-ID = PLATFORM-STD + DRV-ID
[✓] Every Repository deviation (EAGER/compound/native): DRV-ID in Derivation Log (DRV-005 for the one join)
[✓] No orgUnitId in any DTO shape described
[✓] No audit fields in any CreateRequest/UpdateRequest shape
[✓] All ERR-IDs registered in 4 places declared (CORE phase, project-standard)

CROSS-CUTTING CHECKS:
[✓] All inbound XM references use INBOUND-STUB notation — n/a, none exist
[✓] Derivation Log entries present for every non-obvious inference (11 entries)
[✓] ALIGN-BE gate passed ✓

STRUCTURAL SELF-CHECK (AMEND-P3-M):
[✓] Every phase has exactly one PHASE:{key}:START/END pair, using exactly one of the eight
    canonical keys (CORE, DATA-DOM, SVC-API, DOC, INT-C, INT-R, SEC-BE, ALIGN-BE)
[✓] Every API-ID has exactly one dedicated <!-- API:{id} --> block — none merged, none missing
    (verified for all 11 API-MDM-IDs)
[✓] No section or heading label repeats anywhere in this document
[✓] Trailing content (this Agent Handoff Summary + Self-Check) sits after PHASE:ALIGN-BE:END,
    with a heading containing no "PHASE" word and no marker of its own
[✓] Every SUB threshold was checked while writing: SVC+API has 11 APIs (≥ 8 threshold) →
    split into SUB:SVC-API-LOOKUP-TYPE / SUB:SVC-API-LOOKUP-VALUE / SUB:SVC-API-CONSUMPTION,
    each carrying the SVC-API phase-key prefix (AMEND-P3-N). DATA+DOM (2 entities, < 5) and
    INT-C/INT-R (0 XM, < 5) do not split.
[✓] No SUB:INT-C-{X} / SUB:INT-R-{X} labels exist to collide — both phases are empty (0 XM)
```

---

╔══════════════════════════════════════════════════════════════════╗
║           BACKEND EXECUTION PLAN — PASS 1 COMPLETE ✓              ║
╠═══════════════════════╦══════════════════════════════════════════╣
║ Plan Name             ║ New Feature — Reference Data Lookup      ║
║                       ║ Management — Master Data — BE            ║
║ Plan ID               ║ PLAN-MDM-001                              ║
║ Output                ║ backend-execution-plan-MDM.md — Agent-Ready║
║ Phases Complete       ║ CORE✓ DATA+DOM✓ SVC+API✓ DOC✓ INT-C✓    ║
║                       ║ INT-R✓ SEC-BE✓ ALIGN-BE✓                 ║
║ Open Questions        ║ None                                      ║
║ XM DEFERRED           ║ None (0 XM-IDs — MDM is a pure provider)  ║
║ Blocked Elements      ║ None                                      ║
║ QR-IDs Generated      ║ 15 — see SECTION B                        ║
║ Next Stage            ║ Project 4.1 (Backend Audit) → Backend     ║
║                       ║ implementation → api-doc-generator →       ║
║                       ║ GATE: BACKEND MODULE COMPLETE → PASS 2     ║
╚═══════════════════════╩══════════════════════════════════════════╝

*End of backend-execution-plan-MDM.md — Master Data (MDM), Project 3.1 PASS 1, v3.0 (light), 2026-09-04.*
*Governed by: Execution Plan Governance Engine (Project 3.1). Consumed srs-MDM.md v1.3 + db-script-MDM.md (DBS-MDM-001). ALIGN-BE PASSED ✓.*
*Test artifacts (backend-test-plan-MDM.md + test-execution-manifest-MDM.md) to be generated separately by the standalone Test Generation Engine, outside this pipeline, when needed.*
