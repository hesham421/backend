<!-- Source: content OUTSIDE all PHASE markers (trailing / between-phase sections — e.g. Plan Index, DB Alignment Manifest, Error Catalog, Agent Handoff Summary) -->

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