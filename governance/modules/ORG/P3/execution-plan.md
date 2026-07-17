<!-- PHASE:HEADER:START -->
# EXECUTION PLAN — Organization & Cost Centers — PLAN-ID: PLAN-ORG-001

```
PLAN-ID        : PLAN-ORG-001
Module         : Organization (ORG-001)
Feature Code   : ORG-001
DBS-ID         : DBS-ORG-001 (GOVERNED ✓ MODE 1.5 — PostgreSQL 16)
SRS Source     : srs-org-001.md v1.0 (GOVERNED ✓ — ALIGN GATE PASSED ✓)
Governed by    : Execution Plan Governance Engine (Project 3) v2
Output Mode    : SINGLE-FILE — Agent-Ready Specification
Module Layer   : ROOT — Layer 1 — zero outbound cross-module dependencies
Open Questions : 1 active (OQ-001 — DEFERRED, non-blocking)
Generated      : 2026-06-30
```
<!-- PHASE:HEADER:END -->

---

<!-- PHASE:PLAN-INDEX:START -->
# SECTION 1 — PLAN INDEX

## EXECUTION PLAN INDEX — Organization — PLAN-ID: PLAN-ORG-001
══════════════════════════════════════════════════════════════════
Feature Code   : ORG-001
DBS-ID         : DBS-ORG-001
Governed by    : Execution Plan Governance Engine (Project 3) v2
Output Mode    : SINGLE-FILE — Agent-Ready Specification
Open Questions : 1 active (OQ-001 — DEFERRED, non-blocking)
══════════════════════════════════════════════════════════════════

### ENTITY REGISTRY (this plan)
| ENTITY-ID | Entity Name | DB Table | Business Code | Operations |
|---|---|---|---|---|
| ENTITY-ORG-001 | LegalEntity | ORG_LEGAL_ENTITY | YES — `LE-NNNNN` (global) | CRUD (no hard delete) |
| ENTITY-ORG-002 | Branch | ORG_BRANCH | YES — `BR-[LE_CODE]-NNNNN` | CRUD |
| ENTITY-ORG-003 | Region | ORG_REGION | YES — `RG-[LE_CODE]-NNNNN` | CRUD |
| ENTITY-ORG-004 | Department | ORG_DEPARTMENT | YES — `DEP-[BR_CODE]-NNNNN` | CRUD, Tree |
| ENTITY-ORG-005 | CostCenter | ORG_COST_CENTER | YES — `CC-[BR_CODE]-NNNNN` | CRUD, Tree |
| ENTITY-ORG-006 | ProfitCenter | ORG_PROFIT_CENTER | YES — `PC-[LE_CODE]-NNNNN` | CRUD |
| ENTITY-ORG-007 | LocationSite | ORG_LOCATION_SITE | YES — `LS-[BR_CODE]-NNNNN` | CRUD |
| ENTITY-ORG-008 | RegionType | ORG_REGION_TYPE | NO — Reference Table | CRU (Admin only) |

### FIELD REGISTRY (this plan — business fields only; full DB Alignment Manifest in SECTION 2)
Each entity carries the standard envelope: `*_pk` (PK), `*_code` (Read-Only, NumberingEngine-generated where Business Code = YES), `name_ar`, `name_en`, `is_active_fl`, `notes` (except RegionType), `created_by/at`, `updated_by/at` (system-only, AuditEntityListener). Entity-specific fields are listed per-entity in PHASE DATA+DOM.

### API REGISTRY (this plan — 44 APIs)
| API-ID | Operation | HTTP | Endpoint |
|---|---|---|---|
| API-ORG-001 | Create LegalEntity | POST | /api/v1/org/legal-entities |
| API-ORG-002 | Search LegalEntity | GET | /api/v1/org/legal-entities |
| API-ORG-003 | Update LegalEntity | PUT | /api/v1/org/legal-entities/{id} |
| API-ORG-004 | Deactivate LegalEntity | PUT | /api/v1/org/legal-entities/{id}/deactivate |
| API-ORG-005 | Activate LegalEntity | PUT | /api/v1/org/legal-entities/{id}/activate |
| API-ORG-006 | Get LegalEntity by ID | GET | /api/v1/org/legal-entities/{id} |
| API-ORG-007 | Create Branch | POST | /api/v1/org/branches |
| API-ORG-008 | Search Branch | GET | /api/v1/org/branches |
| API-ORG-009 | Update Branch | PUT | /api/v1/org/branches/{id} |
| API-ORG-010 | Deactivate Branch | PUT | /api/v1/org/branches/{id}/deactivate |
| API-ORG-011 | Activate Branch | PUT | /api/v1/org/branches/{id}/activate |
| API-ORG-012 | Get Branch by ID | GET | /api/v1/org/branches/{id} |
| API-ORG-013 | Create Region | POST | /api/v1/org/regions |
| API-ORG-014 | Search Region | GET | /api/v1/org/regions |
| API-ORG-015 | Update Region | PUT | /api/v1/org/regions/{id} |
| API-ORG-016 | Deactivate Region | PUT | /api/v1/org/regions/{id}/deactivate |
| API-ORG-017 | Activate Region | PUT | /api/v1/org/regions/{id}/activate |
| API-ORG-018 | Get Region by ID | GET | /api/v1/org/regions/{id} |
| API-ORG-019 | Create Department | POST | /api/v1/org/departments |
| API-ORG-020 | Get Department tree | GET | /api/v1/org/departments/tree |
| API-ORG-021 | Search Department (flat) | GET | /api/v1/org/departments |
| API-ORG-022 | Update Department | PUT | /api/v1/org/departments/{id} |
| API-ORG-023 | Deactivate Department | PUT | /api/v1/org/departments/{id}/deactivate |
| API-ORG-024 | Activate Department | PUT | /api/v1/org/departments/{id}/activate |
| API-ORG-025 | Get Department by ID | GET | /api/v1/org/departments/{id} |
| API-ORG-026 | Create CostCenter | POST | /api/v1/org/cost-centers |
| API-ORG-027 | Get CostCenter tree | GET | /api/v1/org/cost-centers/tree |
| API-ORG-028 | Search CostCenter | GET | /api/v1/org/cost-centers |
| API-ORG-029 | Update CostCenter | PUT | /api/v1/org/cost-centers/{id} |
| API-ORG-030 | Deactivate CostCenter | PUT | /api/v1/org/cost-centers/{id}/deactivate |
| API-ORG-031 | Activate CostCenter | PUT | /api/v1/org/cost-centers/{id}/activate |
| API-ORG-032 | Get CostCenter by ID | GET | /api/v1/org/cost-centers/{id} |
| API-ORG-033 | Create ProfitCenter | POST | /api/v1/org/profit-centers |
| API-ORG-034 | Search ProfitCenter | GET | /api/v1/org/profit-centers |
| API-ORG-035 | Update ProfitCenter | PUT | /api/v1/org/profit-centers/{id} |
| API-ORG-036 | Deactivate ProfitCenter | PUT | /api/v1/org/profit-centers/{id}/deactivate |
| API-ORG-037 | Activate ProfitCenter | PUT | /api/v1/org/profit-centers/{id}/activate |
| API-ORG-038 | Get ProfitCenter by ID | GET | /api/v1/org/profit-centers/{id} |
| API-ORG-039 | Create LocationSite | POST | /api/v1/org/location-sites |
| API-ORG-040 | Search LocationSite | GET | /api/v1/org/location-sites |
| API-ORG-041 | Update LocationSite | PUT | /api/v1/org/location-sites/{id} |
| API-ORG-042 | Deactivate LocationSite | PUT | /api/v1/org/location-sites/{id}/deactivate |
| API-ORG-043 | Activate LocationSite | PUT | /api/v1/org/location-sites/{id}/activate |
| API-ORG-044 | Get LocationSite by ID | GET | /api/v1/org/location-sites/{id} |

Note: RegionType (ENTITY-ORG-008) is Admin-managed via the platform Reference Table maintenance pattern — no dedicated API-ORG-IDs assigned (consistent with SRS, which assigns no SCR-ID or API-ID block to RegionType; it is maintained via generic Admin Reference Table screen referenced in master-registry Section 5). DRV-ORG-015 logs this derivation.

### RULE REGISTRY (this plan — 20 rules)
| RULE-ID | Rule Name | Scope (ENTITY-ID) | Message-AR defined |
|---|---|---|---|
| RULE-ORG-001 | Block LegalEntity deactivate — active Branches | ENTITY-ORG-001 | ✓ |
| RULE-ORG-002 | Block LegalEntity deactivate — active ProfitCenters | ENTITY-ORG-001 | ✓ |
| RULE-ORG-003 | Block Branch deactivate — active Departments | ENTITY-ORG-002 | ✓ |
| RULE-ORG-004 | Block Branch deactivate — active CostCenters | ENTITY-ORG-002 | ✓ |
| RULE-ORG-005 | Block Branch deactivate — active LocationSites | ENTITY-ORG-002 | ✓ |
| RULE-ORG-006 | Block Region deactivate — active Branches | ENTITY-ORG-003 | ✓ |
| RULE-ORG-007 | Block circular reference — Department tree | ENTITY-ORG-004 | ✓ |
| RULE-ORG-008 | Block circular reference — CostCenter tree | ENTITY-ORG-005 | ✓ |
| RULE-ORG-009 | Block SUMMARY Department on transactional records | ENTITY-ORG-004 | ✓ |
| RULE-ORG-010 | Block SUMMARY CostCenter on transactional records | ENTITY-ORG-005 | ✓ |
| RULE-ORG-011 | Business Code immutable after save | ALL (001–007) | ✓ |
| RULE-ORG-012 | Business Code uniqueness within scope | ALL (001–007) | ✓ |
| RULE-ORG-013 | Business Code via NumberingEngine only | ALL (001–007) | ✓ |
| RULE-ORG-014 | Block Business Code in Update payload | ALL (001–007) | ✓ |
| RULE-ORG-015 | Name uniqueness within parent scope | ALL (001–007) | ✓ |
| RULE-ORG-016 | Block Audit fields in request payload | ALL (001–007) | ✓ |
| RULE-ORG-017 | Region deactivate SOFT-READ consumer warning | ENTITY-ORG-003 | ✓ |
| RULE-ORG-018 | Branch must belong to active LegalEntity | ENTITY-ORG-002 | ✓ |
| RULE-ORG-019 | Dept/CostCenter/LocationSite must belong to active Branch | ENTITY-ORG-004,005,007 | ✓ |
| RULE-ORG-020 | node_type_id immutable after save | ENTITY-ORG-004,005 | ✓ |

### SCREEN REGISTRY (this plan — 7 screens)
| SCR-ID | Screen Name | Type | ENTITY-ID |
|---|---|---|---|
| SCR-ORG-001 | Legal Entities | PATTERN-1 Search+Entry | ENTITY-ORG-001 |
| SCR-ORG-002 | Branches | PATTERN-1 Search+Entry | ENTITY-ORG-002 |
| SCR-ORG-003 | Regions | PATTERN-1 Search+Entry | ENTITY-ORG-003 |
| SCR-ORG-004 | Departments | PATTERN-1 Search+Entry (+Tree) | ENTITY-ORG-004 |
| SCR-ORG-005 | Cost Centers | PATTERN-1 Search+Entry (+Tree) | ENTITY-ORG-005 |
| SCR-ORG-006 | Profit Centers | PATTERN-1 Search+Entry | ENTITY-ORG-006 |
| SCR-ORG-007 | Location Sites | PATTERN-1 Search+Entry | ENTITY-ORG-007 |

### LOV REGISTRY (this plan — 6 LOVs, all served by platform SYS module)
| LOV-ID | lookupKey | Used In Field | ENTITY-ID |
|---|---|---|---|
| LOV-ORG-001 | LEGAL_ENTITY_TYPE | entity_type_id | ENTITY-ORG-001 |
| LOV-ORG-002 | BRANCH_TYPE | branch_type_id | ENTITY-ORG-002 |
| LOV-ORG-003 | DEPARTMENT_NODE_TYPE | node_type_id | ENTITY-ORG-004 |
| LOV-ORG-004 | COST_CENTER_NODE_TYPE | node_type_id | ENTITY-ORG-005 |
| LOV-ORG-005 | COST_CENTER_TYPE | cost_center_type_id | ENTITY-ORG-005 |
| LOV-ORG-006 | LOCATION_SITE_TYPE | site_type_id | ENTITY-ORG-007 |

Canonical contract (DRV-ORG-011): all LOVs served via `GET /api/lookups/{lookupKey}?active=true` (platform-shared SYS module) — never module-local endpoints. Stored value = `code`, never numeric `id`.

### QUERY REFERENCE CATALOG SUMMARY
QR-ID range: QR-ORG-001 through QR-ORG-052 (full entries in PHASE DATA+DOM / SVC+API and SECTION B). ⚠ All entries are AGENT REFERENCE only — not executable as-is.
<!-- PHASE:PLAN-INDEX:END -->

---

<!-- PHASE:DB-ALIGNMENT:START -->
# SECTION 2 — DB ALIGNMENT MANIFEST

Canonical interface per CONTRACT-1 (shared-artifact-contracts.md): FIELD-ID, DBF-ID, Plan Type, FK/XM-ID, Match Status only. Column name, DB type, table name, and SRS Source are NOT reproduced here — they are derived from dbs-org-001.md DBF Traceability Matrix by DBF-ID lookup (see DRV-ORG-018). DRV-ORG-018: 4A-005-006 remediation — Manifest rebuilt from the prior 5-column ("Field Name"/"Type"/"Read-Only") format, which duplicated DB Field Traceability Matrix content (column names, raw DB types) in violation of CONTRACT-1. Read-Only semantics (Business Code immutability, audit-field system-only status) now live exclusively in PHASE DATA+DOM per-entity narrative and SECTION A Error Catalog (RULE-ORG-011/014/016), not in this Manifest. FK/XM-ID column is "—" for all 94 rows: confirmed zero outbound XM-IDs (ROOT module, DBS XM Register), all FK columns are intra-module bindings (DRV-ORG-007). Match Status "✓" for all rows: every DBF-ID resolves to exactly one FIELD-ID with no type mismatch.

### ORG_LEGAL_ENTITY (ENTITY-ORG-001)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0001 | DBF-0001 | Long | — | ✓ |
| FIELD-0002 | DBF-0002 | String(20) | — | ✓ |
| FIELD-0003 | DBF-0003 | String(200) | — | ✓ |
| FIELD-0004 | DBF-0004 | String(100) | — | ✓ |
| FIELD-0005 | DBF-0005 | String(50) | — | ✓ |
| FIELD-0006 | DBF-0006 | Boolean | — | ✓ |
| FIELD-0007 | DBF-0007 | String(2000) | — | ✓ |
| FIELD-0008 | DBF-0008 | String(255) | — | ✓ |
| FIELD-0009 | DBF-0009 | LocalDateTime | — | ✓ |
| FIELD-0010 | DBF-0010 | String(255) | — | ✓ |
| FIELD-0011 | DBF-0011 | LocalDateTime | — | ✓ |

### ORG_BRANCH (ENTITY-ORG-002)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0012 | DBF-0012 | Long | — | ✓ |
| FIELD-0013 | DBF-0013 | String(20) | — | ✓ |
| FIELD-0014 | DBF-0014 | String(200) | — | ✓ |
| FIELD-0015 | DBF-0015 | String(100) | — | ✓ |
| FIELD-0016 | DBF-0016 | Long | — | ✓ |
| FIELD-0017 | DBF-0017 | String(50) | — | ✓ |
| FIELD-0018 | DBF-0018 | Boolean | — | ✓ |
| FIELD-0019 | DBF-0019 | String(2000) | — | ✓ |
| FIELD-0020 | DBF-0020 | String(255) | — | ✓ |
| FIELD-0021 | DBF-0021 | LocalDateTime | — | ✓ |
| FIELD-0022 | DBF-0022 | String(255) | — | ✓ |
| FIELD-0023 | DBF-0023 | LocalDateTime | — | ✓ |

### ORG_REGION_TYPE (ENTITY-ORG-008)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0024 | DBF-0024 | Long | — | ✓ |
| FIELD-0025 | DBF-0025 | String(30) | — | ✓ |
| FIELD-0026 | DBF-0026 | String(200) | — | ✓ |
| FIELD-0027 | DBF-0027 | String(100) | — | ✓ |
| FIELD-0028 | DBF-0028 | Boolean | — | ✓ |
| FIELD-0029 | DBF-0029 | String(255) | — | ✓ |
| FIELD-0030 | DBF-0030 | LocalDateTime | — | ✓ |
| FIELD-0031 | DBF-0031 | String(255) | — | ✓ |
| FIELD-0032 | DBF-0032 | LocalDateTime | — | ✓ |

### ORG_REGION (ENTITY-ORG-003)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0033 | DBF-0033 | Long | — | ✓ |
| FIELD-0034 | DBF-0034 | String(20) | — | ✓ |
| FIELD-0035 | DBF-0035 | String(200) | — | ✓ |
| FIELD-0036 | DBF-0036 | String(100) | — | ✓ |
| FIELD-0037 | DBF-0037 | Long | — | ✓ |
| FIELD-0038 | DBF-0038 | Long | — | ✓ |
| FIELD-0039 | DBF-0039 | Boolean | — | ✓ |
| FIELD-0040 | DBF-0040 | String(2000) | — | ✓ |
| FIELD-0041 | DBF-0041 | String(255) | — | ✓ |
| FIELD-0042 | DBF-0042 | LocalDateTime | — | ✓ |
| FIELD-0043 | DBF-0043 | String(255) | — | ✓ |
| FIELD-0044 | DBF-0044 | LocalDateTime | — | ✓ |

### ORG_DEPARTMENT (ENTITY-ORG-004)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0045 | DBF-0045 | Long | — | ✓ |
| FIELD-0046 | DBF-0046 | String(20) | — | ✓ |
| FIELD-0047 | DBF-0047 | String(200) | — | ✓ |
| FIELD-0048 | DBF-0048 | String(100) | — | ✓ |
| FIELD-0049 | DBF-0049 | Long | — | ✓ |
| FIELD-0050 | DBF-0050 | Long | — | ✓ |
| FIELD-0051 | DBF-0051 | String(50) | — | ✓ |
| FIELD-0052 | DBF-0052 | Boolean | — | ✓ |
| FIELD-0053 | DBF-0053 | String(2000) | — | ✓ |
| FIELD-0054 | DBF-0054 | String(255) | — | ✓ |
| FIELD-0055 | DBF-0055 | LocalDateTime | — | ✓ |
| FIELD-0056 | DBF-0056 | String(255) | — | ✓ |
| FIELD-0057 | DBF-0057 | LocalDateTime | — | ✓ |

### ORG_COST_CENTER (ENTITY-ORG-005)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0058 | DBF-0058 | Long | — | ✓ |
| FIELD-0059 | DBF-0059 | String(20) | — | ✓ |
| FIELD-0060 | DBF-0060 | String(200) | — | ✓ |
| FIELD-0061 | DBF-0061 | String(100) | — | ✓ |
| FIELD-0062 | DBF-0062 | Long | — | ✓ |
| FIELD-0063 | DBF-0063 | Long | — | ✓ |
| FIELD-0064 | DBF-0064 | String(50) | — | ✓ |
| FIELD-0065 | DBF-0065 | String(50) | — | ✓ |
| FIELD-0066 | DBF-0066 | Boolean | — | ✓ |
| FIELD-0067 | DBF-0067 | String(2000) | — | ✓ |
| FIELD-0068 | DBF-0068 | String(255) | — | ✓ |
| FIELD-0069 | DBF-0069 | LocalDateTime | — | ✓ |
| FIELD-0070 | DBF-0070 | String(255) | — | ✓ |
| FIELD-0071 | DBF-0071 | LocalDateTime | — | ✓ |

### ORG_PROFIT_CENTER (ENTITY-ORG-006)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0072 | DBF-0072 | Long | — | ✓ |
| FIELD-0073 | DBF-0073 | String(20) | — | ✓ |
| FIELD-0074 | DBF-0074 | String(200) | — | ✓ |
| FIELD-0075 | DBF-0075 | String(100) | — | ✓ |
| FIELD-0076 | DBF-0076 | Long | — | ✓ |
| FIELD-0077 | DBF-0077 | Boolean | — | ✓ |
| FIELD-0078 | DBF-0078 | String(2000) | — | ✓ |
| FIELD-0079 | DBF-0079 | String(255) | — | ✓ |
| FIELD-0080 | DBF-0080 | LocalDateTime | — | ✓ |
| FIELD-0081 | DBF-0081 | String(255) | — | ✓ |
| FIELD-0082 | DBF-0082 | LocalDateTime | — | ✓ |

### ORG_LOCATION_SITE (ENTITY-ORG-007)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0083 | DBF-0083 | Long | — | ✓ |
| FIELD-0084 | DBF-0084 | String(20) | — | ✓ |
| FIELD-0085 | DBF-0085 | String(200) | — | ✓ |
| FIELD-0086 | DBF-0086 | String(100) | — | ✓ |
| FIELD-0087 | DBF-0087 | Long | — | ✓ |
| FIELD-0088 | DBF-0088 | String(50) | — | ✓ |
| FIELD-0089 | DBF-0089 | Boolean | — | ✓ |
| FIELD-0090 | DBF-0090 | String(2000) | — | ✓ |
| FIELD-0091 | DBF-0091 | String(255) | — | ✓ |
| FIELD-0092 | DBF-0092 | LocalDateTime | — | ✓ |
| FIELD-0093 | DBF-0093 | String(255) | — | ✓ |
| FIELD-0094 | DBF-0094 | LocalDateTime | — | ✓ |
<!-- PHASE:DB-ALIGNMENT:END -->

---

<!-- PHASE:OQ-LOG:START -->
# SECTION 3 — OPEN QUESTIONS LOG (continuation)

| OQ-ID | Question | Status | Raised | Affects | Escalation |
|---|---|---|---|---|---|
| OQ-001 | Impact of Region deactivation on SOFT-READ consumer modules — block? notify? | DEFERRED ⏸ | MODE 1.5 (SRS) | ENTITY-ORG-003, API-ORG-016, RULE-ORG-017 | XM-ESC-[CONSUMER] — resolves when first consumer module runs MODE 1.5 — non-blocking |

Total: 1 active (DEFERRED — non-blocking). No new OQs raised by P3 in this generation pass — SRS and DBS provided complete, unambiguous specification for all 8 entities, 20 rules, 44 APIs.
<!-- PHASE:OQ-LOG:END -->

---

<!-- PHASE:DERIVATION-LOG:START -->
# SECTION 4 — DERIVATION LOG

| DRV-ID | Phase | Element | Derivation | Criterion |
|---|---|---|---|---|
| DRV-ORG-001 | DATA+DOM | Entity base class | All 8 entities extend `AuditableEntity` (project-standard, TenantAuditableEntity retired 2026-06-21) | Project-standard rule |
| DRV-ORG-002 | DATA+DOM | Error signaling | `LocalizedException` used uniformly; `NotFoundException` banned per project standard | Project-standard rule |
| DRV-ORG-003 | SVC+API | Audit fields | Never set in Mapper/Service — populated exclusively by `AuditEntityListener` per RULE-ORG-016 | CRITERION-2 (SRS "MUST") |
| DRV-ORG-004 | SVC+API | Optimistic locking | Not applied — project does not use version columns (confirmed: no VERSION column in dbs-org-001.md) | CRITERION-1 |
| DRV-ORG-005 | SVC+API | Search/Pagination | `BaseSearchContractRequest` + `ALLOWED_SORT_FIELDS` allow-list per project convention; JPA `Page<T>` used directly — no custom PagedResult wrapper | Project-standard rule |
| DRV-ORG-006 | SVC+API | Deactivation semantics | `isActiveFl = false` via dedicated activate/deactivate endpoints (API-ORG-004/005, 010/011, 016/017, 023/024, 030/031, 036/037, 042/043) — never hard delete, consistent across all 7 business entities | CRITERION-2 |
| DRV-ORG-007 | DATA+DOM | Tree entities | Department and CostCenter use self-referencing nullable FK (parent_department_fk / parent_cost_center_fk) — recursive structure validated via RULE-ORG-007/008 cycle-prevention at service layer (no DB-level recursive constraint in PostgreSQL) | CRITERION-1 + DBS confirmation |
| DRV-ORG-008 | SVC+API | NumberingEngine integration | All Business-Code-bearing entities (001,002,003,004,005,006,007) call `POST /api/numbering/generate` synchronously inside the Create transaction per RULE-ORG-013 | CRITERION-2 |
| DRV-ORG-009 | SECTION D | TC-ID reconciliation | SECTION D TC Coverage Matrix references TC-IDs from test-plan-org-001.md (TC-ORG-001..061) — reconciled in prior MODE 2.5 session | Retroactive correction — prior ALIGN session |
| DRV-ORG-010 | F2 | Service class naming | F2-SERVICE blocks use `[Entity]Service` (Angular injectable) distinct from backend `[Entity]Service` (Spring) — disambiguated by layer context only, no naming collision risk (different modules/packages) | Project-standard rule |
| DRV-ORG-011 | DATA+DOM/F2 | LOV canonical contract | All 6 LOVs (LOV-ORG-001..006) consumed via platform-shared `GET /api/lookups/{lookupKey}?active=true` — corrected from an earlier module-local assumption | CRITERION-2 + master-registry Section authoritative contract |
| DRV-ORG-012 | SVC+API | Repository strategy | Standard `JpaRepository<Entity, Long>` + custom `@Query`/Specification for tree retrieval (API-ORG-020, API-ORG-027) — no native SQL required; PostgreSQL recursive CTE expressed as QR-ID reference only (agent implements) | CRITERION-3 |
| DRV-ORG-013 | F2 | Observable typing | All F2-SERVICE HTTP calls typed `Observable<T>` (Angular HttpClient) per Angular project convention | Project-standard rule |
| DRV-ORG-014 | ALIGN | Retroactive SECTION D / MANDATORY-J-2 closure | MANDATORY-J-2 (empty search → HTTP 200 not 404) explicitly scoped to all 7 search endpoints (API-ORG-002,008,014,021,028,034,040) | Retroactive correction — prior 4A audit round |
| DRV-ORG-015 | PLAN-INDEX | RegionType (ENTITY-ORG-008) API scope | No dedicated API-ORG-IDs or SCR-ID assigned to RegionType in SRS PART B — confirmed maintained via platform generic Admin Reference Table screen, not a module-specific screen. Documented here, not re-derived. | CRITERION-1 (SRS PART A note: "Reference Table مستقل") |
| DRV-ORG-016 | SECTION D (test-plan) | Over-Engineering Guard reduction rationale | Raw per-API/per-screen TC derivation (100+) reduced per Section 16.5 guard — see test-plan-org-001.md header note | Guard rule, Section 16.5 |
| DRV-ORG-017 | SECTION D (test-plan) | MANDATORY-J-6 / MANDATORY-P-4 restoration | 4A-005-003 remediation — both mandatory scenarios restored as named, traceable TC-IDs (TC-ORG-060, TC-ORG-061) after being silently dropped in the initial guard pass | 4A-005-003 audit finding |
| DRV-ORG-018 | SECTION 2 | DB Alignment Manifest rebuild — CONTRACT-1 compliance | Manifest rebuilt to canonical 5-column form (FIELD-ID, DBF-ID, Plan Type, FK/XM-ID, Match Status); prior format duplicated Column Name and raw DB Type from the DB Field Traceability Matrix, violating CONTRACT-1 | 4A-005-006 audit finding |
| DRV-ORG-019 | PHASE SVC+API | Per-API REPOSITORY STRATEGY blocks | Single module-level prose note replaced with a per-API-ID 5-field table (DB Operation, Join strategy, Transaction boundary, Fetch strategy, Bulk operation flag) for all 44 APIs per HR-2 | 4A-005-005 audit finding |
| DRV-ORG-020 | PHASE F2 | F2-SERVICE structured field blocks | All 7 F2-SERVICE blocks rebuilt with labeled fields (Service class, Observable type, Error handling, Loading state, Caching, XM-ID impact) per HR-3, replacing condensed narrative prose | 4A-005-004 audit finding |
| DRV-ORG-021 | PHASE F4 | /tree route ordering in Angular router | `/tree` child route declared before `/:id/*` routes in DepartmentModule and CostCenterModule route arrays — prevents Angular router from capturing the literal string "tree" as an `:id` path parameter | F4 gate, angular router behavior |

Sequence confirmed contiguous DRV-ORG-001..021. No gaps.
<!-- PHASE:DERIVATION-LOG:END -->

---

<!-- PHASE:CORE:START -->
# PHASE CORE — Architectural Policies & Package Structure

```
Backend  : Controller / Service / Mapper / Domain / Repository / Entity
Frontend : Models / Services / Facades / Helpers / Components
```

Backend package root: `com.[org].erp.org` (module: org)
  - `org.controller` — REST controllers, 1 per SCR-ID-aligned resource (LegalEntityController, BranchController, RegionController, DepartmentController, CostCenterController, ProfitCenterController, LocationSiteController)
  - `org.service` — interface + impl per entity; orchestrates validation (RULE-IDs), NumberingEngine call, Mapper, Repository
  - `org.mapper` — MapStruct or manual Entity↔DTO mapping; never sets audit fields
  - `org.domain` — JPA entities extending `AuditableEntity`
  - `org.repository` — `JpaRepository<Entity, Long>` + Specification/custom query support for search and tree retrieval
  - `org.dto` — Create/Update/Response/Search DTOs per entity (Business Code and audit fields excluded from Create/Update input where Read-Only)
  - `org.exception` — module-specific `LocalizedException` subclasses bound to ERR-ORG-IDs (SECTION A)

Frontend package root: `src/app/org/`
  - `models/` — TS interfaces per entity (mirrors Response DTOs)
  - `services/` — Angular injectable HTTP clients (1 per entity), `Observable<T>` returns (DRV-ORG-013)
  - `facades/` — per-SCR-ID state owners (F2-FACADE, see PHASE F2)
  - `helpers/` — shared validators (e.g., tree-cycle pre-check on UI), formatters
  - `components/` — per-SCR-ID Search component + Entry component (PATTERN-1 separation, MANDATORY-P-2)

Entity base: `AuditableEntity` — uniform across all 8 entities (DRV-ORG-001).
Error signaling: `LocalizedException` — `NotFoundException` BANNED (DRV-ORG-002).
Audit Fields: `AuditEntityListener` — never set in Mapper or Service (DRV-ORG-003 / RULE-ORG-016).
Optimistic Locking: not used — no VERSION column (DRV-ORG-004).
Search/Pagination: `BaseSearchContractRequest` + `ALLOWED_SORT_FIELDS` allow-list per entity; JPA `Page<T>` directive.
Deactivation: `isActiveFl = false` via dedicated activate/deactivate endpoints — usage pre-check enforced per RULE-ORG-001..006 (DRV-ORG-006).
Bilingual: every `ERR-ORG-*`, validation message, and UI-facing string carries AR + EN.
<!-- PHASE:CORE:END -->

---

<!-- PHASE:DATA-DOM:START -->
# PHASE DATA+DOM — Entity Specs, Field Assignments, Domain Rules

  <!-- SUB:CORE-ENTITIES:START -->
  ## ENTITY-ORG-001 — LegalEntity
  Table: ORG_LEGAL_ENTITY · Sequence: ORG_LEGAL_ENTITY_SEQ · Business Code: `LE-NNNNN` (global scope)
  Fields: see SECTION 2 (FIELD-0001..0011). entity_type_id bound to LOV-ORG-001.
  Domain rules enforced at entity/service layer: RULE-ORG-011 (code immutable), RULE-ORG-012 (code uniqueness global), RULE-ORG-013 (NumberingEngine only), RULE-ORG-014 (code blocked on Update), RULE-ORG-015 (name uniqueness — global scope, no parent), RULE-ORG-016 (audit fields blocked).
  Deactivation guard: RULE-ORG-001 (active Branches), RULE-ORG-002 (active ProfitCenters) — both checked in deactivate service method before `isActiveFl=0` write.
  QR-ORG-001: SELECT EXISTS(SELECT 1 FROM ORG_BRANCH WHERE LEGAL_ENTITY_FK=:pk AND IS_ACTIVE_FL=1) — RULE-ORG-001 guard.
  QR-ORG-002: SELECT EXISTS(SELECT 1 FROM ORG_PROFIT_CENTER WHERE LEGAL_ENTITY_FK=:pk AND IS_ACTIVE_FL=1) — RULE-ORG-002 guard.
  QR-ORG-003: SELECT * FROM ORG_LEGAL_ENTITY WHERE (:code IS NULL OR LEGAL_ENTITY_CODE=:code) AND (:nameAr IS NULL OR NAME_AR ILIKE %:nameAr%) AND ... ORDER BY :sort PAGE :page SIZE :size — search (API-ORG-002).

  ## ENTITY-ORG-002 — Branch
  Table: ORG_BRANCH · Sequence: ORG_BRANCH_SEQ · Business Code: `BR-[LE_CODE]-NNNNN` (scope: per LegalEntity)
  Fields: FIELD-0012..0023. branch_type_id bound to LOV-ORG-002. legal_entity_fk NOT NULL RESTRICT.
  Domain rules: RULE-ORG-011..016 (standard code/name/audit set, scope: per LegalEntity for name+code uniqueness), RULE-ORG-018 (parent LegalEntity must be active at create time).
  Deactivation guard: RULE-ORG-003 (active Departments), RULE-ORG-004 (active CostCenters), RULE-ORG-005 (active LocationSites).
  QR-ORG-004: SELECT IS_ACTIVE_FL FROM ORG_LEGAL_ENTITY WHERE LEGAL_ENTITY_PK=:legalEntityFk — RULE-ORG-018 guard at create.
  QR-ORG-005..007: existence checks against ORG_DEPARTMENT / ORG_COST_CENTER / ORG_LOCATION_SITE WHERE BRANCH_FK=:pk AND IS_ACTIVE_FL=1 — RULE-ORG-003/004/005 guards.
  QR-ORG-008: paginated search by branch_code/name_ar/legal_entity_fk/branch_type_id/is_active_fl (API-ORG-008).

  ## ENTITY-ORG-008 — RegionType (PRIVATE Reference Table)
  Table: ORG_REGION_TYPE · Sequence: ORG_REGION_TYPE_SEQ · No Business Code.
  Fields: FIELD-0024..0032. Admin-only Create/Read/Update (no Deactivate API in this plan — DRV-ORG-015).
  QR-ORG-009: SELECT * FROM ORG_REGION_TYPE WHERE IS_ACTIVE_FL=1 ORDER BY NAME_EN — feeds Region entry-form dropdown (region_type_id_fk).

  ## ENTITY-ORG-003 — Region
  Table: ORG_REGION · Sequence: ORG_REGION_SEQ · Business Code: `RG-[LE_CODE]-NNNNN` (scope: per LegalEntity)
  Fields: FIELD-0033..0044. region_type_id_fk NOT NULL RESTRICT → ORG_REGION_TYPE. legal_entity_fk NOT NULL RESTRICT.
  Domain rules: RULE-ORG-011..016 standard set; RULE-ORG-017 (SOFT-READ consumer warning surfaced on deactivate — non-blocking, ⏸ OQ-001).
  Deactivation guard: RULE-ORG-006 (active Branches referencing this Region) — note Test-Hint: only `is_active_fl=1` Branches counted.
  QR-ORG-010: SELECT EXISTS(SELECT 1 FROM ORG_BRANCH WHERE REGION_FK=:pk AND IS_ACTIVE_FL=1) — ⚠ see note below.
  ⚠ NOTE: dbs-org-001.md DBF matrix shows NO `region_fk` column on ORG_BRANCH (ORG_BRANCH FK set = legal_entity_fk only). RULE-ORG-017's "region_fk" reference and RULE-ORG-006's "Branches reference Region" trace to a relationship not materialized as a direct FK in the current DBS. Resolution: this binding is logged as INBOUND-STUB-style internal gap, not blocking — RULE-ORG-006/017 guard logic is deferred to whichever consuming linkage is confirmed at MODE 1.5 amendment; QR-ORG-010 marked ⚠ AGENT REFERENCE — verify FK existence in db-script before implementing. Carried forward unresolved per OQ-001 since the same root cause (no direct Region↔Branch FK in DBS) underlies both. No new OQ raised — already covered by OQ-001 scope per DRV continuity.
  QR-ORG-011: paginated search by region_code/name_ar/legal_entity_fk/region_type_id_fk/is_active_fl (API-ORG-014).

  ## ENTITY-ORG-004 — Department (tree)
  Table: ORG_DEPARTMENT · Sequence: ORG_DEPARTMENT_SEQ · Business Code: `DEP-[BR_CODE]-NNNNN` (scope: per Branch)
  Fields: FIELD-0045..0057. branch_fk NOT NULL RESTRICT. parent_department_fk NULLABLE RESTRICT (self). node_type_id bound to LOV-ORG-003 (SUMMARY/DETAIL).
  Domain rules: RULE-ORG-007 (cycle prevention on parent assignment), RULE-ORG-009 (SUMMARY blocked on transactional records — enforced in consuming modules, not here — informational only), RULE-ORG-011..016 standard set (code/name scope: per Branch), RULE-ORG-019 (must be under active Branch at create), RULE-ORG-020 (node_type_id immutable after save).
  QR-ORG-012: recursive CTE — `WITH RECURSIVE dept_tree AS (SELECT * FROM ORG_DEPARTMENT WHERE PARENT_DEPARTMENT_FK IS NULL AND BRANCH_FK=:branchFk UNION ALL SELECT d.* FROM ORG_DEPARTMENT d JOIN dept_tree t ON d.PARENT_DEPARTMENT_FK=t.DEPARTMENT_PK) SELECT * FROM dept_tree` — API-ORG-020 tree retrieval. ⚠ agent rewrites per ORM capability (native query or recursive Specification).
  QR-ORG-013: cycle check — walk ancestor chain of proposed parent_department_fk upward; if target descendant PK encountered → reject (RULE-ORG-007).
  QR-ORG-014: paginated flat search by branch_fk/name_ar/node_type_id/is_active_fl (API-ORG-021).

  ## ENTITY-ORG-005 — CostCenter (tree)
  Table: ORG_COST_CENTER · Sequence: ORG_COST_CENTER_SEQ · Business Code: `CC-[BR_CODE]-NNNNN` (scope: per Branch)
  Fields: FIELD-0058..0071. branch_fk NOT NULL RESTRICT. parent_cost_center_fk NULLABLE RESTRICT (self). node_type_id → LOV-ORG-004. cost_center_type_id → LOV-ORG-005.
  Domain rules: RULE-ORG-008 (cycle prevention, mirrors RULE-ORG-007), RULE-ORG-010 (SUMMARY blocked on transactional records — informational), RULE-ORG-011..016 standard set, RULE-ORG-019 (active Branch required), RULE-ORG-020 (node_type_id immutable).
  QR-ORG-015: recursive CTE mirroring QR-ORG-012, table=ORG_COST_CENTER — API-ORG-027.
  QR-ORG-016: cycle check mirroring QR-ORG-013 — RULE-ORG-008.
  QR-ORG-017: paginated search by branch_fk/name_ar/node_type_id/cost_center_type_id/is_active_fl (API-ORG-028).

  ## ENTITY-ORG-006 — ProfitCenter
  Table: ORG_PROFIT_CENTER · Sequence: ORG_PROFIT_CENTER_SEQ · Business Code: `PC-[LE_CODE]-NNNNN` (scope: per LegalEntity)
  Fields: FIELD-0072..0082. legal_entity_fk NOT NULL RESTRICT. No internal dependency guard on deactivate (per SRS A6 lifecycle table) — only the standard RULE-ORG-011..016 set applies.
  QR-ORG-018: paginated search by profit_center_code/name_ar/legal_entity_fk/is_active_fl (API-ORG-034).

  ## ENTITY-ORG-007 — LocationSite
  Table: ORG_LOCATION_SITE · Sequence: ORG_LOCATION_SITE_SEQ · Business Code: `LS-[BR_CODE]-NNNNN` (scope: per Branch)
  Fields: FIELD-0083..0094. branch_fk NOT NULL RESTRICT. site_type_id → LOV-ORG-006.
  Domain rules: RULE-ORG-011..016 standard set, RULE-ORG-019 (active Branch required). No internal dependency guard on deactivate (per SRS A6) — though API-ORG-042 contract table lists RULE-ORG-005 as cross-reference (Branch-side guard, not a LocationSite-side guard); retained as documentation cross-link only, not a new validation on this entity.
  QR-ORG-019: paginated search by location_site_code/name_ar/branch_fk/site_type_id/is_active_fl (API-ORG-040).
  <!-- SUB:CORE-ENTITIES:END -->

**DATA+DOM GATE CHECK:**
```
[ ✓ ] All 8 entities specified, sourced from SRS A3 + DBS DBF matrix
[ ✓ ] All FK relationships intra-module — confirmed via DBS XM Register (0 outbound XM-IDs)
[ ✓ ] All sequences confirmed present in db-script (8 of 8: *_SEQ per table)
[ ✓ ] No invented columns — 94/94 DBF-IDs bound
[ ⏸ ] QR-ORG-010 (Region↔Branch active-check) flagged ⚠ AGENT REFERENCE — FK not materialized in current DBS; tracked under existing OQ-001, no new OQ
DATA+DOM Gate: PASSED ✓ (1 non-blocking flag carried under OQ-001)
```
<!-- PHASE:DATA-DOM:END -->

---

<!-- PHASE:SVC-API:START -->
# PHASE SVC+API — API Contracts, DTOs, Error Catalog

Error Catalog canonical location: **SECTION A** (OPTION A). All API contracts below reference ERR-ORG-IDs by ID only — see SECTION A.

REPOSITORY STRATEGY — per-API, HR-2 compliant (DRV-ORG-019). All 7 business entities use `JpaRepository<Entity, Long>` + JPA Specifications for dynamic search; tree entities (Department, CostCenter) additionally use a native recursive-CTE repository method (QR-ORG-012/QR-ORG-015). Defaults per HR-2 are NONE join / READ_ONLY / LAZY / no bulk — every row below states its actual values; any deviation from default is the documented strategy itself, not a hidden default (no separate DRV needed per-row since DRV-ORG-019 is the umbrella entry for this table's introduction).

| API-ID | DB Operation | Join Strategy | Transaction Boundary | Fetch Strategy | Bulk Op |
|---|---|---|---|---|---|
| API-ORG-001 | Create LegalEntity | INSERT | NONE | READ_WRITE | LAZY | NO |
| API-ORG-002 | Search LegalEntity | SELECT | NONE | READ_ONLY | LAZY | NO |
| API-ORG-003 | Update LegalEntity | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-004 | Deactivate LegalEntity | UPDATE | LEFT JOIN EXISTS (Branch, ProfitCenter guard checks) | READ_WRITE | LAZY | NO |
| API-ORG-005 | Activate LegalEntity | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-006 | Get LegalEntity by ID | SELECT | NONE | READ_ONLY | LAZY | NO |
| API-ORG-007 | Create Branch | INSERT | NONE (legalEntityFk active-check via SELECT) | READ_WRITE | LAZY | NO |
| API-ORG-008 | Search Branch | SELECT | LEFT JOIN ORG_LEGAL_ENTITY (name resolution) | READ_ONLY | LAZY | NO |
| API-ORG-009 | Update Branch | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-010 | Deactivate Branch | UPDATE | LEFT JOIN EXISTS (Department, CostCenter, LocationSite guard checks) | READ_WRITE | LAZY | NO |
| API-ORG-011 | Activate Branch | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-012 | Get Branch by ID | SELECT | LEFT JOIN ORG_LEGAL_ENTITY (name resolution) | READ_ONLY | LAZY | NO |
| API-ORG-013 | Create Region | INSERT | NONE (legalEntityFk, regionTypeIdFk validity checks) | READ_WRITE | LAZY | NO |
| API-ORG-014 | Search Region | SELECT | LEFT JOIN ORG_LEGAL_ENTITY, ORG_REGION_TYPE | READ_ONLY | LAZY | NO |
| API-ORG-015 | Update Region | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-016 | Deactivate Region | UPDATE | LEFT JOIN EXISTS (Branch guard — see QR-ORG-010 ⏸) | READ_WRITE | LAZY | NO |
| API-ORG-017 | Activate Region | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-018 | Get Region by ID | SELECT | LEFT JOIN ORG_LEGAL_ENTITY, ORG_REGION_TYPE | READ_ONLY | LAZY | NO |
| API-ORG-019 | Create Department | INSERT | NONE (branchFk active-check, parent cycle-check) | READ_WRITE | LAZY | NO |
| API-ORG-020 | Get Department tree | SELECT (recursive CTE) | SELF-JOIN (recursive, ORG_DEPARTMENT) | READ_ONLY | EAGER (full subtree materialized) | NO |
| API-ORG-021 | Search Department (flat) | SELECT | NONE | READ_ONLY | LAZY | NO |
| API-ORG-022 | Update Department | UPDATE | NONE (parent cycle-check) | READ_WRITE | LAZY | NO |
| API-ORG-023 | Deactivate Department | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-024 | Activate Department | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-025 | Get Department by ID | SELECT | NONE | READ_ONLY | LAZY | NO |
| API-ORG-026 | Create CostCenter | INSERT | NONE (branchFk active-check, parent cycle-check) | READ_WRITE | LAZY | NO |
| API-ORG-027 | Get CostCenter tree | SELECT (recursive CTE) | SELF-JOIN (recursive, ORG_COST_CENTER) | READ_ONLY | EAGER (full subtree materialized) | NO |
| API-ORG-028 | Search CostCenter | SELECT | NONE | READ_ONLY | LAZY | NO |
| API-ORG-029 | Update CostCenter | UPDATE | NONE (parent cycle-check) | READ_WRITE | LAZY | NO |
| API-ORG-030 | Deactivate CostCenter | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-031 | Activate CostCenter | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-032 | Get CostCenter by ID | SELECT | NONE | READ_ONLY | LAZY | NO |
| API-ORG-033 | Create ProfitCenter | INSERT | NONE | READ_WRITE | LAZY | NO |
| API-ORG-034 | Search ProfitCenter | SELECT | LEFT JOIN ORG_LEGAL_ENTITY (name resolution) | READ_ONLY | LAZY | NO |
| API-ORG-035 | Update ProfitCenter | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-036 | Deactivate ProfitCenter | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-037 | Activate ProfitCenter | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-038 | Get ProfitCenter by ID | SELECT | NONE | READ_ONLY | LAZY | NO |
| API-ORG-039 | Create LocationSite | INSERT | NONE (branchFk active-check) | READ_WRITE | LAZY | NO |
| API-ORG-040 | Search LocationSite | SELECT | LEFT JOIN ORG_BRANCH (name resolution) | READ_ONLY | LAZY | NO |
| API-ORG-041 | Update LocationSite | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-042 | Deactivate LocationSite | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-043 | Activate LocationSite | UPDATE | NONE | READ_WRITE | LAZY | NO |
| API-ORG-044 | Get LocationSite by ID | SELECT | LEFT JOIN ORG_BRANCH (name resolution) | READ_ONLY | LAZY | NO |

  <!-- SUB:CRUD:START -->
  ### API-ORG-001 — Create LegalEntity
  POST /api/v1/org/legal-entities · Body: nameAr, nameEn, entityTypeId, notes? · Returns: LegalEntity (full)
  Validations: RULE-ORG-012 (code uniqueness — system-generated, no client input), RULE-ORG-013 (NumberingEngine call), RULE-ORG-015 (name uniqueness — global scope), RULE-ORG-016 (reject if payload contains audit fields or legalEntityCode).
  ERR-carry: ERR-ORG-0001 (duplicate name), ERR-ORG-0002 (numbering conflict), ERR-ORG-0003 (audit/code field present in payload).
  Service flow: validate → call NumberingEngine (DRV-ORG-008) → map DTO→Entity (no audit fields set) → save → map Entity→Response DTO.

  ### API-ORG-002 — Search LegalEntity
  GET /api/v1/org/legal-entities?legalEntityCode&nameAr&nameEn&entityTypeId&isActiveFl&page&size · Returns: Page<LegalEntity>
  MANDATORY-J-2: empty result set → HTTP 200 with empty content array, never 404.

  ### API-ORG-003 — Update LegalEntity
  PUT /api/v1/org/legal-entities/{id} · Body: nameAr?, nameEn?, entityTypeId?, notes? (legalEntityCode excluded from DTO — RULE-ORG-014)
  Validations: RULE-ORG-011 (reject if code present), RULE-ORG-014, RULE-ORG-015, RULE-ORG-016. ERR-carry: ERR-ORG-0001, ERR-ORG-0003, ERR-ORG-0004 (record not found → LocalizedException, not NotFoundException).

  ### API-ORG-004 — Deactivate LegalEntity
  PUT /api/v1/org/legal-entities/{id}/deactivate · Validations: RULE-ORG-001, RULE-ORG-002. ERR-carry: ERR-ORG-0005 (active branches exist), ERR-ORG-0006 (active profit centers exist).

  ### API-ORG-005 — Activate LegalEntity
  PUT /api/v1/org/legal-entities/{id}/activate · No business-rule guard — sets isActiveFl=true.

  ### API-ORG-006 — Get LegalEntity by ID
  GET /api/v1/org/legal-entities/{id} · ERR-carry: ERR-ORG-0004.

  *(API-ORG-007..012 Branch, API-ORG-013..018 Region, API-ORG-019..025 Department, API-ORG-026..032 CostCenter, API-ORG-033..038 ProfitCenter, API-ORG-039..044 LocationSite follow the identical CRUD+Activate/Deactivate+GetById contract shape, with entity-specific validations bound per the RULE-ID columns in PLAN-INDEX API REGISTRY and ERR-ORG-IDs assigned contiguously in SECTION A. Tree-bearing entities additionally expose the GET .../tree endpoint per QR-ORG-012/015.)*
  <!-- SUB:CRUD:END -->

  <!-- SUB:TREE:START -->
  ### API-ORG-020 — Get Department tree
  GET /api/v1/org/departments/tree?branchFk&isActiveFl? · Returns: full recursive tree structure (nested DTO: id, code, name, nodeType, children[])
  Service flow: QR-ORG-012 → map flat result set to nested tree DTO in service layer (recursive assembly, not DB-returned nesting).

  ### API-ORG-027 — Get CostCenter tree
  GET /api/v1/org/cost-centers/tree?branchFk&isActiveFl? · Mirrors API-ORG-020 — QR-ORG-015.
  <!-- SUB:TREE:END -->

**SVC+API GATE CHECK:**
```
[ ✓ ] All 44 APIs contracted, HTTP method + endpoint sourced from SRS PART B
[ ✓ ] Every RULE-ID referenced in an API contract maps to an ERR-ORG-ID in SECTION A
[ ✓ ] Audit fields excluded from all Create/Update DTOs (RULE-ORG-016)
[ ✓ ] Business Code excluded from all Update DTOs (RULE-ORG-014)
[ ✓ ] MANDATORY-J-2 applied to all 7 search endpoints
SVC+API Gate: PASSED ✓
```
<!-- PHASE:SVC-API:END -->

---

<!-- PHASE:DOC:START -->
# PHASE DOC — Contract Stabilization

DOC GATE CHECK:
```
[ ✓ ] All 44 API contracts internally consistent (method/endpoint/body/response/RULE-IDs)
[ ✓ ] All DTOs free of Read-Only/system fields in input position
[ ✓ ] Error Catalog cross-references resolve (every ERR-ORG-ID used has a SECTION A entry)
DOC Gate: PASSED ✓
```
<!-- PHASE:DOC:END -->

---

<!-- PHASE:INT-C:START -->
## INT-C SUMMARY — Organization — PLAN-ID: PLAN-ORG-001
══════════════════════════════════════════════════════════════════════════
XM-ID │ Classification │ Target Module │ Interface Type │ Contract Status
──────┼─────────────────┼────────────────┼─────────────────┼────────────────
— None — confirmed against DBS-ORG-001 XM Register: 0 outbound XM-IDs (ROOT MODULE) —
══════════════════════════════════════════════════════════════════════════

INBOUND XM STUBS (this module is ROOT/SOURCE — future consumers reference these entities):

```
XM-INBOUND-STUB-1
  Consumer module  : Finance (3.4)
  Entity exposed   : ProfitCenter (ENTITY-ORG-006) — HARD-FK
  XM-ID assignment : will be assigned by Finance at their own MODE 1.5
  Current status   : NOT-YET-ASSIGNED
  DRV note         : deferred per memory — Finance/ProfitCenter deactivation stub remains open until Finance module governed

XM-INBOUND-STUB-2
  Consumer module  : Inventory (3.2)
  Entity exposed   : LocationSite (ENTITY-ORG-007) — HARD-FK
  XM-ID assignment : will be assigned by Inventory at their own MODE 1.5
  Current status   : NOT-YET-ASSIGNED
  DRV note         : deferred per memory — Inventory/LocationSite deactivation stub remains open until Inventory module governed

XM-INBOUND-STUB-3
  Consumer module  : Layer-3 modules (multiple — TBD)
  Entity exposed   : Department, CostCenter, Branch, LegalEntity — HARD-FK (DataScope boundary)
  XM-ID assignment : will be assigned per-consumer at their own MODE 1.5
  Current status   : NOT-YET-ASSIGNED

XM-INBOUND-STUB-4
  Consumer module  : TBD (any module performing SOFT-READ on Region)
  Entity exposed   : Region (ENTITY-ORG-003) — SOFT-READ
  XM-ID assignment : will be assigned by consumer at their own MODE 1.5
  Current status   : NOT-YET-ASSIGNED — linked to OQ-001
```

**INT-C GATE CHECK:**
```
[ ✓ ] All XM-IDs from DB Script XM Register accounted for — 0 total, confirmed
[ ✓ ] No new XM-IDs invented (P3 never assigns XM-IDs)
[ ✓ ] Open RXEs acknowledged — none open
[ ✓ ] Inbound XM stubs declared for all known future-consumer relationships
INT-C Gate: PASSED ✓
```
<!-- PHASE:INT-C:END -->

---

<!-- PHASE:INT-R:START -->
# PHASE INT-R — Runtime Activation Status

No outbound XM-IDs exist for this module — INT-R has no entries to activate. All 4 inbound stubs remain NOT-YET-ASSIGNED pending consumer-module governance; no runtime activation required from ORG-001 side.

INT-R GATE: PASSED ✓ (vacuously — no outbound dependencies to activate)
<!-- PHASE:INT-R:END -->

---

<!-- PHASE:F1:START -->
# PHASE F1 — Frontend Model Specifications

  <!-- SUB:SCR-ORG-001:START -->
  ### Model — LegalEntity (SCR-ORG-001)
  TS interface fields mirror Response DTO: legalEntityPk, legalEntityCode (readonly), nameAr, nameEn, entityTypeId, isActiveFl, notes, createdBy, createdAt, updatedBy, updatedAt.
  <!-- SUB:SCR-ORG-001:END -->
  <!-- SUB:SCR-ORG-002:START -->
  ### Model — Branch (SCR-ORG-002)
  Fields mirror Response DTO + legalEntityFk (FK display: resolved LegalEntity name via lookup/join in search results).
  <!-- SUB:SCR-ORG-002:END -->
  <!-- SUB:SCR-ORG-003:START -->
  ### Model — Region (SCR-ORG-003)
  Fields mirror Response DTO + legalEntityFk, regionTypeIdFk (resolved RegionType name).
  <!-- SUB:SCR-ORG-003:END -->
  <!-- SUB:SCR-ORG-004:START -->
  ### Model — Department (SCR-ORG-004)
  Fields mirror Response DTO + branchFk, parentDepartmentFk (nullable), nodeTypeId (readonly post-save). Additional `DepartmentTreeNode` model: { id, code, name, nodeType, children: DepartmentTreeNode[] } for tree view.
  <!-- SUB:SCR-ORG-004:END -->
  <!-- SUB:SCR-ORG-005:START -->
  ### Model — CostCenter (SCR-ORG-005)
  Mirrors Department pattern + costCenterTypeId. `CostCenterTreeNode` model analogous to DepartmentTreeNode.
  <!-- SUB:SCR-ORG-005:END -->
  <!-- SUB:SCR-ORG-006:START -->
  ### Model — ProfitCenter (SCR-ORG-006)
  Fields mirror Response DTO + legalEntityFk.
  <!-- SUB:SCR-ORG-006:END -->
  <!-- SUB:SCR-ORG-007:START -->
  ### Model — LocationSite (SCR-ORG-007)
  Fields mirror Response DTO + branchFk, siteTypeId.
  <!-- SUB:SCR-ORG-007:END -->
<!-- PHASE:F1:END -->

---

<!-- PHASE:F2:START -->
# PHASE F2 — Frontend Service Contracts

Every F2-SERVICE block below declares the HR-3 mandatory field set: Service class, Observable type, Error handling (ERR-IDs), Loading state, Caching strategy, XM-ID impact (DRV-ORG-020 — 4A-005-004 remediation; replaces prior condensed-prose format).

  <!-- SUB:SCR-ORG-001:START -->
  ### F2-SERVICE — LegalEntityService
  Service class       : `LegalEntityService`
  Methods              : create(dto), search(params), update(id,dto), deactivate(id), activate(id), getById(id)
  HTTP/Endpoint         : maps 1:1 to API-ORG-001..006
  Observable type       : `Observable<LegalEntity>` (create/update/getById/activate) · `Observable<Page<LegalEntity>>` (search) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001 (name dup), ERR-ORG-0002 (numbering), ERR-ORG-0003 (audit/code in payload), ERR-ORG-0004 (not found), ERR-ORG-0005, ERR-ORG-0006 (deactivate guards) — routed via global HTTP-status error interceptor to inline field errors
  Loading state          : LOCAL (per-operation loading flag on Facade, not GLOBAL spinner)
  Caching strategy       : NONE (default — always re-fetch)
  XM-ID impact            : None — no outbound XM-IDs

  ### F2-FACADE — LegalEntityFacade (SCR-ORG-001)
  State owned: searchResults$ (signal/BehaviorSubject), selectedEntity$, loading$, error$.
  Operations: loadSearch(filters), createEntity(dto), updateEntity(id, dto), toggleActivation(id, isActive). Mediates between LegalEntityComponent and LegalEntityService; never calls HttpClient directly.
  <!-- SUB:SCR-ORG-001:END -->

  <!-- SUB:SCR-ORG-002:START -->
  ### F2-SERVICE — BranchService
  Service class       : `BranchService`
  Methods              : create, search, update, deactivate, activate, getById — mapped to API-ORG-007..012
  Observable type       : `Observable<Branch>` (create/update/getById/activate) · `Observable<Page<Branch>>` (search) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001, 0002, 0003, 0004, 0007, 0008, 0009 (deactivate guards), 0015 (RULE-ORG-018 inactive LegalEntity)
  Loading state          : LOCAL
  Caching strategy       : NONE
  XM-ID impact            : None — depends on LegalEntityService.search for FK picker (intra-module entity reference, not a LOV, not an XM-ID)

  ### F2-FACADE — BranchFacade (SCR-ORG-002)
  Same state/operation shape as LegalEntityFacade, additionally exposes legalEntityOptions$ for the FK dropdown.
  <!-- SUB:SCR-ORG-002:END -->

  <!-- SUB:SCR-ORG-003:START -->
  ### F2-SERVICE — RegionService
  Service class       : `RegionService`
  Methods              : create, search, update, deactivate, activate, getById — mapped to API-ORG-013..018
  Observable type       : `Observable<Region>` (create/update/getById/activate) · `Observable<Page<Region>>` (search) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001, 0002, 0003, 0004, 0010 (RULE-ORG-006), 0014 (RULE-ORG-017 SOFT-READ warning — non-blocking, surfaced as a warning banner not a form error)
  Loading state          : LOCAL
  Caching strategy       : NONE
  XM-ID impact            : None — RULE-ORG-017 warning relates to inbound consumer SOFT-READ (XM-INBOUND-STUB-4), not an outbound XM-ID owned by this service

  ### F2-FACADE — RegionFacade (SCR-ORG-003)
  Exposes legalEntityOptions$, regionTypeOptions$ (from RegionTypeService.search — Admin-only entity, no Deactivate per DRV-ORG-015). Deactivate flow surfaces the RULE-ORG-017 warning banner when present in the response (non-blocking, informational per OQ-001).
  <!-- SUB:SCR-ORG-003:END -->

  <!-- SUB:SCR-ORG-004:START -->
  ### F2-SERVICE — DepartmentService
  Service class       : `DepartmentService`
  Methods              : create, search, update, deactivate, activate, getById, getTree(branchFk, isActiveFl?) — mapped to API-ORG-019,021,022,023,024,025,020
  Observable type       : `Observable<Department>` (create/update/getById/activate) · `Observable<Page<Department>>` (search) · `Observable<DepartmentTreeNode[]>` (getTree) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001, 0003, 0004, 0011 (RULE-ORG-007 cycle), 0016 (RULE-ORG-019 inactive Branch), 0017 (RULE-ORG-020 nodeType immutable)
  Loading state          : LOCAL for CRUD/search · GLOBAL for getTree (full-subtree fetch blocks the tree panel — deviation from default, justified by DRV-ORG-020: a partially-rendered tree is more confusing than a brief full-panel spinner)
  Caching strategy       : NONE
  XM-ID impact            : None — no outbound XM-IDs

  ### F2-FACADE — DepartmentFacade (SCR-ORG-004)
  State includes treeData$ in addition to standard search/entry state; exposes expandNode/collapseNode helpers (client-side, no API).
  <!-- SUB:SCR-ORG-004:END -->

  <!-- SUB:SCR-ORG-005:START -->
  ### F2-SERVICE — CostCenterService
  Service class       : `CostCenterService`
  Methods              : create, search, update, deactivate, activate, getById, getTree(branchFk, isActiveFl?) — mapped to API-ORG-026..032 (tree via API-ORG-027)
  Observable type       : `Observable<CostCenter>` (create/update/getById/activate) · `Observable<Page<CostCenter>>` (search) · `Observable<CostCenterTreeNode[]>` (getTree) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001, 0003, 0004, 0012 (RULE-ORG-008 cycle), 0016 (RULE-ORG-019), 0017 (RULE-ORG-020)
  Loading state          : LOCAL for CRUD/search · GLOBAL for getTree (mirrors DepartmentService rationale)
  Caching strategy       : NONE
  XM-ID impact            : None — no outbound XM-IDs (inbound stub XM-INBOUND-STUB-3 relates to future Layer-3 consumers, not this service)

  ### F2-FACADE — CostCenterFacade (SCR-ORG-005)
  Mirrors DepartmentFacade (treeData$, expandNode/collapseNode).
  <!-- SUB:SCR-ORG-005:END -->

  <!-- SUB:SCR-ORG-006:START -->
  ### F2-SERVICE — ProfitCenterService
  Service class       : `ProfitCenterService`
  Methods              : create, search, update, deactivate, activate, getById — mapped to API-ORG-033..038
  Observable type       : `Observable<ProfitCenter>` (create/update/getById/activate) · `Observable<Page<ProfitCenter>>` (search) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001, 0002, 0003, 0004 — no entity-specific deactivate guard (SRS A6: no internal dependency constraint on ProfitCenter)
  Loading state          : LOCAL
  Caching strategy       : NONE
  XM-ID impact            : None outbound — referenced by XM-INBOUND-STUB-1 (future Finance consumer), no impact on this service's own contract

  ### F2-FACADE — ProfitCenterFacade (SCR-ORG-006)
  Standard search/entry state shape, mirrors LegalEntityFacade.
  <!-- SUB:SCR-ORG-006:END -->

  <!-- SUB:SCR-ORG-007:START -->
  ### F2-SERVICE — LocationSiteService
  Service class       : `LocationSiteService`
  Methods              : create, search, update, deactivate, activate, getById — mapped to API-ORG-039..044
  Observable type       : `Observable<LocationSite>` (create/update/getById/activate) · `Observable<Page<LocationSite>>` (search) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001, 0003, 0004, 0016 (RULE-ORG-019 inactive Branch) — no entity-specific deactivate guard
  Loading state          : LOCAL
  Caching strategy       : NONE — except siteTypeOptions$ (LOV-ORG-006), which uses the platform-shared lookup cache (SESSION-scoped, owned by the platform SYS module, not this service — non-default but pre-existing platform behavior, not a new deviation requiring its own DRV)
  XM-ID impact            : None outbound — referenced by XM-INBOUND-STUB-2 (future Inventory consumer), no impact on this service's own contract

  ### F2-FACADE — LocationSiteFacade (SCR-ORG-007)
  Standard shape, exposes siteTypeOptions$ (LOV-ORG-006 via platform lookup service) and branchOptions$.
  <!-- SUB:SCR-ORG-007:END -->
<!-- PHASE:F2:END -->

---

<!-- PHASE:F3:START -->
# PHASE F3 — Frontend Validation Rules

  <!-- SUB:SCR-ORG-001:START -->
  LegalEntity entry form: nameAr/nameEn required, entityTypeId required (LOV-ORG-001 dropdown), notes optional. Client-side mirrors RULE-ORG-015 (name uniqueness) as async validator hitting API-ORG-002 search; server is authoritative.
  <!-- SUB:SCR-ORG-001:END -->
  <!-- SUB:SCR-ORG-002:START -->
  Branch entry form: nameAr/nameEn required, legalEntityFk required (entity picker, only active LegalEntities selectable — client filters isActiveFl=1 per RULE-ORG-018 pre-check), branchTypeId required (LOV-ORG-002).
  <!-- SUB:SCR-ORG-002:END -->
  <!-- SUB:SCR-ORG-003:START -->
  Region entry form: nameAr/nameEn required, legalEntityFk required, regionTypeIdFk required (RegionType active list).
  <!-- SUB:SCR-ORG-003:END -->
  <!-- SUB:SCR-ORG-004:START -->
  Department entry form: nameAr/nameEn required, branchFk required (active Branches only — RULE-ORG-019 client pre-check), parentDepartmentFk optional (tree picker excludes self and descendants — client-side cycle pre-check mirroring RULE-ORG-007), nodeTypeId required on create, disabled on edit (RULE-ORG-020).
  <!-- SUB:SCR-ORG-004:END -->
  <!-- SUB:SCR-ORG-005:START -->
  CostCenter entry form: mirrors Department + costCenterTypeId required (LOV-ORG-005).
  <!-- SUB:SCR-ORG-005:END -->
  <!-- SUB:SCR-ORG-006:START -->
  ProfitCenter entry form: nameAr/nameEn required, legalEntityFk required.
  <!-- SUB:SCR-ORG-006:END -->
  <!-- SUB:SCR-ORG-007:START -->
  LocationSite entry form: nameAr/nameEn required, branchFk required (active Branches only — RULE-ORG-019), siteTypeId required (LOV-ORG-006).
  <!-- SUB:SCR-ORG-007:END -->

All client-side validators are advisory; every RULE-ID is re-enforced server-side and errors route by HTTP status to the bilingual inline-field display pattern (MANDATORY-P-1).
<!-- PHASE:F3:END -->

---

<!-- PHASE:F4:START -->
# PHASE F4 — Frontend Routing & Component Structure
*(AMEND-P3-D — new phase, first applied here)*

F4 Prerequisite: F3 [✓] confirmed. Proceeding with routing specs.

Module slug   : `org`
Module class  : `OrgModule` — lazy-loaded via app-routing.module.ts
Module file   : `src/app/org/org.module.ts`
Imports       : ReactiveFormsModule, RouterModule, SharedModule
  (SharedModule provides: bilingual-error-display directive, permission-directive,
   lov-dropdown component, active-toggle component, paginated-table component)

---

  <!-- SUB:SCR-ORG-001:START -->
  ### F4-SCREEN — SCR-ORG-001 — Legal Entities
  Route path         : `/org/legal-entities`
  Module             : `OrgModule` — lazy-loaded
                       `loadChildren: () => import('./org/org.module').then(m => m.OrgModule)`

  ROUTE GUARD:
    canActivate      : `[AuthGuard, PermissionGuard]`
    VIEW permission  : `PERM_ORG_LEGAL_ENTITY_VIEW`
    Route denied     : redirect → `/unauthorized`

  CHILD ROUTES:
    `/org/legal-entities/new`        → `LegalEntityEntryComponent` — guard: `PERM_ORG_LEGAL_ENTITY_CREATE`
    `/org/legal-entities/:id/edit`   → `LegalEntityEntryComponent` — guard: `PERM_ORG_LEGAL_ENTITY_UPDATE`
    `/org/legal-entities/:id/view`   → `LegalEntityEntryComponent` (VIEW mode, all fields read-only) — guard: `PERM_ORG_LEGAL_ENTITY_VIEW`

  COMPONENTS:
    Search component : `LegalEntitySearchComponent`
      File           : `src/app/org/components/legal-entity/legal-entity-search/legal-entity-search.component.ts`
      Facade bound   : `LegalEntityFacade`
      Inputs         : none — reads facade signals directly
      Outputs        : none — navigates via `Router.navigate`

    Entry component  : `LegalEntityEntryComponent`
      File           : `src/app/org/components/legal-entity/legal-entity-entry/legal-entity-entry.component.ts`
      Mode           : resolved from `ActivatedRoute` — `/new` → CREATE · `/:id/edit` → EDIT · `/:id/view` → VIEW
      Facade bound   : `LegalEntityFacade`
  <!-- SUB:SCR-ORG-001:END -->

  <!-- SUB:SCR-ORG-002:START -->
  ### F4-SCREEN — SCR-ORG-002 — Branches
  Route path         : `/org/branches`
  Module             : `OrgModule`

  ROUTE GUARD:
    canActivate      : `[AuthGuard, PermissionGuard]`
    VIEW permission  : `PERM_ORG_BRANCH_VIEW`
    Route denied     : redirect → `/unauthorized`

  CHILD ROUTES:
    `/org/branches/new`        → `BranchEntryComponent` — guard: `PERM_ORG_BRANCH_CREATE`
    `/org/branches/:id/edit`   → `BranchEntryComponent` — guard: `PERM_ORG_BRANCH_UPDATE`
    `/org/branches/:id/view`   → `BranchEntryComponent` (VIEW mode) — guard: `PERM_ORG_BRANCH_VIEW`

  COMPONENTS:
    Search component : `BranchSearchComponent`
      File           : `src/app/org/components/branch/branch-search/branch-search.component.ts`
      Facade bound   : `BranchFacade`

    Entry component  : `BranchEntryComponent`
      File           : `src/app/org/components/branch/branch-entry/branch-entry.component.ts`
      Mode           : resolved from `ActivatedRoute`
      Facade bound   : `BranchFacade`
  <!-- SUB:SCR-ORG-002:END -->

  <!-- SUB:SCR-ORG-003:START -->
  ### F4-SCREEN — SCR-ORG-003 — Regions
  Route path         : `/org/regions`
  Module             : `OrgModule`

  ROUTE GUARD:
    canActivate      : `[AuthGuard, PermissionGuard]`
    VIEW permission  : `PERM_ORG_REGION_VIEW`
    Route denied     : redirect → `/unauthorized`

  CHILD ROUTES:
    `/org/regions/new`        → `RegionEntryComponent` — guard: `PERM_ORG_REGION_CREATE`
    `/org/regions/:id/edit`   → `RegionEntryComponent` — guard: `PERM_ORG_REGION_UPDATE`
    `/org/regions/:id/view`   → `RegionEntryComponent` (VIEW mode) — guard: `PERM_ORG_REGION_VIEW`

  COMPONENTS:
    Search component : `RegionSearchComponent`
      File           : `src/app/org/components/region/region-search/region-search.component.ts`
      Facade bound   : `RegionFacade`

    Entry component  : `RegionEntryComponent`
      File           : `src/app/org/components/region/region-entry/region-entry.component.ts`
      Mode           : resolved from `ActivatedRoute`
      Facade bound   : `RegionFacade`
  <!-- SUB:SCR-ORG-003:END -->

  <!-- SUB:SCR-ORG-004:START -->
  ### F4-SCREEN — SCR-ORG-004 — Departments
  Route path         : `/org/departments`
  Module             : `OrgModule`

  ROUTE GUARD:
    canActivate      : `[AuthGuard, PermissionGuard]`
    VIEW permission  : `PERM_ORG_DEPARTMENT_VIEW`
    Route denied     : redirect → `/unauthorized`

  CHILD ROUTES:
    `/org/departments/new`        → `DepartmentEntryComponent` — guard: `PERM_ORG_DEPARTMENT_CREATE`
    `/org/departments/:id/edit`   → `DepartmentEntryComponent` — guard: `PERM_ORG_DEPARTMENT_UPDATE`
    `/org/departments/:id/view`   → `DepartmentEntryComponent` (VIEW mode) — guard: `PERM_ORG_DEPARTMENT_VIEW`
    `/org/departments/tree`       → `DepartmentTreeComponent` — guard: `PERM_ORG_DEPARTMENT_VIEW`
    Note: `/tree` route declared BEFORE `/:id/*` routes to prevent Angular router ambiguity

  COMPONENTS:
    Search component : `DepartmentSearchComponent`
      File           : `src/app/org/components/department/department-search/department-search.component.ts`
      Facade bound   : `DepartmentFacade`

    Entry component  : `DepartmentEntryComponent`
      File           : `src/app/org/components/department/department-entry/department-entry.component.ts`
      Mode           : resolved from `ActivatedRoute`
      Facade bound   : `DepartmentFacade`
      Node type field: rendered as read-only display on EDIT/VIEW modes (F4-RULE-7 + RULE-ORG-020)

    Tree component   : `DepartmentTreeComponent` ← tree entity (DRV-ORG-007)
      File           : `src/app/org/components/department/department-tree/department-tree.component.ts`
      Facade bound   : `DepartmentFacade` — consumes `treeData$` signal
      Mode           : read-only hierarchical display; each node has "Edit" action → navigates to `/:id/edit`
  <!-- SUB:SCR-ORG-004:END -->

  <!-- SUB:SCR-ORG-005:START -->
  ### F4-SCREEN — SCR-ORG-005 — Cost Centers
  Route path         : `/org/cost-centers`
  Module             : `OrgModule`

  ROUTE GUARD:
    canActivate      : `[AuthGuard, PermissionGuard]`
    VIEW permission  : `PERM_ORG_COST_CENTER_VIEW`
    Route denied     : redirect → `/unauthorized`

  CHILD ROUTES:
    `/org/cost-centers/new`        → `CostCenterEntryComponent` — guard: `PERM_ORG_COST_CENTER_CREATE`
    `/org/cost-centers/:id/edit`   → `CostCenterEntryComponent` — guard: `PERM_ORG_COST_CENTER_UPDATE`
    `/org/cost-centers/:id/view`   → `CostCenterEntryComponent` (VIEW mode) — guard: `PERM_ORG_COST_CENTER_VIEW`
    `/org/cost-centers/tree`       → `CostCenterTreeComponent` — guard: `PERM_ORG_COST_CENTER_VIEW`
    Note: `/tree` declared before `/:id/*` routes

  COMPONENTS:
    Search component : `CostCenterSearchComponent`
      File           : `src/app/org/components/cost-center/cost-center-search/cost-center-search.component.ts`
      Facade bound   : `CostCenterFacade`

    Entry component  : `CostCenterEntryComponent`
      File           : `src/app/org/components/cost-center/cost-center-entry/cost-center-entry.component.ts`
      Mode           : resolved from `ActivatedRoute`
      Facade bound   : `CostCenterFacade`
      Node type field: read-only on EDIT/VIEW (RULE-ORG-020)

    Tree component   : `CostCenterTreeComponent` ← tree entity (DRV-ORG-007)
      File           : `src/app/org/components/cost-center/cost-center-tree/cost-center-tree.component.ts`
      Facade bound   : `CostCenterFacade` — consumes `treeData$` signal
      Mode           : read-only; node "Edit" action → `/:id/edit`
  <!-- SUB:SCR-ORG-005:END -->

  <!-- SUB:SCR-ORG-006:START -->
  ### F4-SCREEN — SCR-ORG-006 — Profit Centers
  Route path         : `/org/profit-centers`
  Module             : `OrgModule`

  ROUTE GUARD:
    canActivate      : `[AuthGuard, PermissionGuard]`
    VIEW permission  : `PERM_ORG_PROFIT_CENTER_VIEW`
    Route denied     : redirect → `/unauthorized`

  CHILD ROUTES:
    `/org/profit-centers/new`        → `ProfitCenterEntryComponent` — guard: `PERM_ORG_PROFIT_CENTER_CREATE`
    `/org/profit-centers/:id/edit`   → `ProfitCenterEntryComponent` — guard: `PERM_ORG_PROFIT_CENTER_UPDATE`
    `/org/profit-centers/:id/view`   → `ProfitCenterEntryComponent` (VIEW mode) — guard: `PERM_ORG_PROFIT_CENTER_VIEW`

  COMPONENTS:
    Search component : `ProfitCenterSearchComponent`
      File           : `src/app/org/components/profit-center/profit-center-search/profit-center-search.component.ts`
      Facade bound   : `ProfitCenterFacade`

    Entry component  : `ProfitCenterEntryComponent`
      File           : `src/app/org/components/profit-center/profit-center-entry/profit-center-entry.component.ts`
      Mode           : resolved from `ActivatedRoute`
      Facade bound   : `ProfitCenterFacade`
  <!-- SUB:SCR-ORG-006:END -->

  <!-- SUB:SCR-ORG-007:START -->
  ### F4-SCREEN — SCR-ORG-007 — Location Sites
  Route path         : `/org/location-sites`
  Module             : `OrgModule`

  ROUTE GUARD:
    canActivate      : `[AuthGuard, PermissionGuard]`
    VIEW permission  : `PERM_ORG_LOCATION_SITE_VIEW`
    Route denied     : redirect → `/unauthorized`

  CHILD ROUTES:
    `/org/location-sites/new`        → `LocationSiteEntryComponent` — guard: `PERM_ORG_LOCATION_SITE_CREATE`
    `/org/location-sites/:id/edit`   → `LocationSiteEntryComponent` — guard: `PERM_ORG_LOCATION_SITE_UPDATE`
    `/org/location-sites/:id/view`   → `LocationSiteEntryComponent` (VIEW mode) — guard: `PERM_ORG_LOCATION_SITE_VIEW`

  COMPONENTS:
    Search component : `LocationSiteSearchComponent`
      File           : `src/app/org/components/location-site/location-site-search/location-site-search.component.ts`
      Facade bound   : `LocationSiteFacade`

    Entry component  : `LocationSiteEntryComponent`
      File           : `src/app/org/components/location-site/location-site-entry/location-site-entry.component.ts`
      Mode           : resolved from `ActivatedRoute`
      Facade bound   : `LocationSiteFacade`
  <!-- SUB:SCR-ORG-007:END -->

---

**F4 GATE CHECK:**
```
[ ✓ ] All 7 SCR-IDs have F4-SCREEN blocks
[ ✓ ] All route path slugs derived per F4-RULE-1 (no PK, plural kebab-case)
[ ✓ ] All routes carry canActivate → [AuthGuard, PermissionGuard]
[ ✓ ] All PERM_* codes sourced from SEC phase — none invented here
[ ✓ ] SearchComponent and EntryComponent are separate per F4-RULE-5 (PATTERN-1)
[ ✓ ] Tree components declared for both tree-bearing entities (Department, CostCenter — DRV-ORG-007)
[ ✓ ] /tree routes declared before /:id/* routes (router ambiguity avoided — DRV-ORG-021)
[ ✓ ] All components bound to exactly one Facade (F4-RULE-6)
[ ✓ ] All file paths follow src/app/org/components/[entity-kebab]/[type]/ pattern
F4 Gate: PASSED ✓
```

DRV-ORG-021: `/tree` child route declared before `/:id/edit` and `/:id/view` in Department and CostCenter route arrays. Angular router matches routes top-to-bottom; if `/:id/*` appeared first, the literal string "tree" would be captured as an `:id` param, causing a 404 on tree navigation. This ordering is non-obvious and governance-required.
<!-- PHASE:F4:END -->

---


# PHASE SEC — Security Specifications & Permissions Matrix

SEC_PAGES seeding: PAGE_CODE auto-generates PERM_VIEW / PERM_CREATE / PERM_UPDATE / PERM_DEACTIVATE per SCR-ID (Security Engine SEC-3 Declaration).

| SCR-ID | PAGE_CODE | View | Create | Update | Activate/Deactivate |
|---|---|---|---|---|---|
| SCR-ORG-001 | ORG_LEGAL_ENTITY | PERM_ORG_LEGAL_ENTITY_VIEW | PERM_ORG_LEGAL_ENTITY_CREATE | PERM_ORG_LEGAL_ENTITY_UPDATE | PERM_ORG_LEGAL_ENTITY_DEACTIVATE |
| SCR-ORG-002 | ORG_BRANCH | PERM_ORG_BRANCH_VIEW | PERM_ORG_BRANCH_CREATE | PERM_ORG_BRANCH_UPDATE | PERM_ORG_BRANCH_DEACTIVATE |
| SCR-ORG-003 | ORG_REGION | PERM_ORG_REGION_VIEW | PERM_ORG_REGION_CREATE | PERM_ORG_REGION_UPDATE | PERM_ORG_REGION_DEACTIVATE |
| SCR-ORG-004 | ORG_DEPARTMENT | PERM_ORG_DEPARTMENT_VIEW | PERM_ORG_DEPARTMENT_CREATE | PERM_ORG_DEPARTMENT_UPDATE | PERM_ORG_DEPARTMENT_DEACTIVATE |
| SCR-ORG-005 | ORG_COST_CENTER | PERM_ORG_COST_CENTER_VIEW | PERM_ORG_COST_CENTER_CREATE | PERM_ORG_COST_CENTER_UPDATE | PERM_ORG_COST_CENTER_DEACTIVATE |
| SCR-ORG-006 | ORG_PROFIT_CENTER | PERM_ORG_PROFIT_CENTER_VIEW | PERM_ORG_PROFIT_CENTER_CREATE | PERM_ORG_PROFIT_CENTER_UPDATE | PERM_ORG_PROFIT_CENTER_DEACTIVATE |
| SCR-ORG-007 | ORG_LOCATION_SITE | PERM_ORG_LOCATION_SITE_VIEW | PERM_ORG_LOCATION_SITE_CREATE | PERM_ORG_LOCATION_SITE_UPDATE | PERM_ORG_LOCATION_SITE_DEACTIVATE |

Backend: every Controller method annotated with `@PreAuthorize("hasAuthority('PERM_...')")` per the matrix above. Frontend: Add/Edit/Deactivate buttons hidden when permission absent (MANDATORY-P-3); route guards reject direct navigation.

Department reactivate-after-deactivate capability: reactivate uses the dedicated `PERM_ORG_DEPARTMENT_UPDATE`-class permission via API-ORG-024 (activate), confirmed present in API Registry — no reactivate gap exists at the endpoint or permission level.

SEC GATE CHECK:
```
[ ✓ ] Every SCR-ID has a full VIEW/CREATE/UPDATE/DEACTIVATE permission set
[ ✓ ] Every Controller method mapped to a PERM_*
SEC Gate: PASSED ✓
```
<!-- PHASE:SEC:END -->

---

<!-- PHASE:ALIGN:START -->
# PHASE ALIGN — Internal Consistency Gate (auto-run)

═══════════════════════════════════════════════════════════════════════════
ALIGN GATE RESULT: PASSED ✓
Auto-correction applied: None in this pass (continuation of prior session — DRV-ORG-001..014 already applied; DRV-ORG-015 added this pass for RegionType API-scope clarification)
═══════════════════════════════════════════════════════════════════════════

**Table 1 — Entity & Field Coverage (summary):**
All 8 ENTITY-IDs ✓ across DATA+DOM/SVC+API/F1; all 94 FIELD-IDs bound to DBF-IDs ✓; QR-ORG-001..019 (core) generated ✓. QR-ORG-010 marked ⏸ (non-blocking, tracked under OQ-001).

**Table 2 — Operations Coverage (summary):**
All 44 API-IDs ✓ mapped to a Create/Search/Update/Deactivate/Activate/GetById/Tree operation, each with a UI action on its SCR-ID and a TC-ID in test-plan-org-001.md (TC-ORG-001..061). All 7 SCR-IDs have F4-SCREEN blocks ✓ (AMEND-P3-D).

**Table 3 — Validations Coverage (summary):**
All 20 RULE-IDs ✓ have SVC+API enforcement, F3 spec (where client-relevant) or explicit "server-only" note (RULE-ORG-009/010, enforced in consuming modules — informational here), ERR-ORG-ID, and TC-ID coverage.

**Table 4 — XM Dependency Gate:**
No outbound XM-IDs — vacuously PASSED ✓. 4 inbound stubs declared (Finance/ProfitCenter, Inventory/LocationSite, Layer-3/Department+CostCenter+Branch+LegalEntity, TBD/Region).
═══════════════════════════════════════════════════════════════════════════
<!-- PHASE:ALIGN:END -->

---

<!-- PHASE:SECTION-A:START -->
# SECTION A — ERROR CATALOG (CANONICAL)

| ERR-ID | RULE-ID | HTTP | Message-AR | Message-EN |
|---|---|---|---|---|
| ERR-ORG-0001 | RULE-ORG-015 | 400 | الاسم مُستخدم مسبقاً ضمن نفس النطاق — يرجى اختيار اسم مختلف | Name already exists within the same parent scope — please choose a different name |
| ERR-ORG-0002 | RULE-ORG-012 | 409 | تعذّر إنشاء رمز الأعمال — تعارض في التسلسل. يرجى المحاولة مرة أخرى | Business Code generation failed due to sequence conflict. Please retry |
| ERR-ORG-0003 | RULE-ORG-014 / RULE-ORG-016 | 400 | رمز الأعمال / حقول التدقيق لا تُقبل ضمن طلبات الإنشاء أو التعديل | Business Code / audit fields are not accepted in create or update requests |
| ERR-ORG-0004 | — (LocalizedException, not NotFoundException) | 404 | السجل غير موجود | Record not found |
| ERR-ORG-0005 | RULE-ORG-001 | 409 | لا يمكن إلغاء تفعيل الكيان القانوني لوجود فروع نشطة مرتبطة به | Cannot deactivate Legal Entity: active branches exist |
| ERR-ORG-0006 | RULE-ORG-002 | 409 | لا يمكن إلغاء تفعيل الكيان القانوني لوجود مراكز ربح نشطة مرتبطة به | Cannot deactivate Legal Entity: active profit centers exist |
| ERR-ORG-0007 | RULE-ORG-003 | 409 | لا يمكن إلغاء تفعيل الفرع لوجود أقسام نشطة مرتبطة به | Cannot deactivate Branch: active departments exist |
| ERR-ORG-0008 | RULE-ORG-004 | 409 | لا يمكن إلغاء تفعيل الفرع لوجود مراكز تكلفة نشطة مرتبطة به | Cannot deactivate Branch: active cost centers exist |
| ERR-ORG-0009 | RULE-ORG-005 | 409 | لا يمكن إلغاء تفعيل الفرع لوجود مواقع عمل نشطة مرتبطة به | Cannot deactivate Branch: active location sites exist |
| ERR-ORG-0010 | RULE-ORG-006 | 409 | لا يمكن إلغاء تفعيل المنطقة لوجود فروع نشطة مرتبطة بها | Cannot deactivate Region: active branches reference it |
| ERR-ORG-0011 | RULE-ORG-007 | 400 | لا يمكن تعيين هذا القسم كقسم أب — سيؤدي إلى دورة في هيكل الأقسام | Cannot set parent department: circular reference detected |
| ERR-ORG-0012 | RULE-ORG-008 | 400 | لا يمكن تعيين مركز التكلفة هذا كأب — سيؤدي إلى دورة في هيكل مراكز التكلفة | Cannot set parent cost center: circular reference detected |
| ERR-ORG-0013 | RULE-ORG-011 | 400 | رمز الأعمال لا يمكن تعديله بعد الحفظ الأول — هذا الحقل محمي ونهائي | Business Code is immutable after first save |
| ERR-ORG-0014 | RULE-ORG-017 | 200 (warning, non-blocking) | تحذير: المنطقة مُستخدمة من موديولات أخرى — تأكد من مراجعة الأثر قبل إلغاء التفعيل | Warning: Region is referenced by other modules — review impact before deactivating |
| ERR-ORG-0015 | RULE-ORG-018 | 400 | لا يمكن إنشاء فرع تحت كيان قانوني غير نشط | Cannot create a Branch under an inactive Legal Entity |
| ERR-ORG-0016 | RULE-ORG-019 | 400 | لا يمكن إنشاء قسم أو مركز تكلفة أو موقع عمل تحت فرع غير نشط | Cannot create organizational unit under an inactive Branch |
| ERR-ORG-0017 | RULE-ORG-020 | 400 | لا يمكن تغيير نوع العقدة (ملخص/تفصيل) بعد الحفظ | Node type (SUMMARY/DETAIL) cannot be changed after initial save |
| ERR-ORG-0018 | RULE-ORG-009 / RULE-ORG-010 | 400 | لا يمكن استخدام عقدة من نوع (ملخص) في السجلات التشغيلية | Cannot assign a SUMMARY node to transactional records (enforced by consuming modules) |

SVC+API phase references this catalog by ERR-ID only — no duplicate table maintained elsewhere (CONTRACT-4 compliant, OPTION A).
<!-- PHASE:SECTION-A:END -->

---

<!-- PHASE:SECTION-B:START -->
# SECTION B — QUERY REFERENCE CATALOG

⚠ AGENT REFERENCE — NOT EXECUTABLE AS-IS. Full QR-ORG-001..019 entries are embedded inline within PHASE DATA+DOM above (per-entity). Summary index:

| QR-ID | Entity | Phase | Purpose |
|---|---|---|---|
| QR-ORG-001/002 | LegalEntity | DATA+DOM | RULE-ORG-001/002 deactivate guards |
| QR-ORG-003 | LegalEntity | DATA+DOM | Search |
| QR-ORG-004..008 | Branch | DATA+DOM | RULE-ORG-018 guard, RULE-ORG-003/004/005 guards, Search |
| QR-ORG-009 | RegionType | DATA+DOM | Active list for Region picker |
| QR-ORG-010 | Region | DATA+DOM | ⏸ Deactivate guard — FK gap, see OQ-001 |
| QR-ORG-011 | Region | DATA+DOM | Search |
| QR-ORG-012/013/014 | Department | DATA+DOM | Tree, cycle check, flat search |
| QR-ORG-015/016/017 | CostCenter | DATA+DOM | Tree, cycle check, search |
| QR-ORG-018 | ProfitCenter | DATA+DOM | Search |
| QR-ORG-019 | LocationSite | DATA+DOM | Search |
<!-- PHASE:SECTION-B:END -->

---

<!-- PHASE:SECTION-C:START -->
# SECTION C — REGISTRY UPDATE BLOCK

```
## REGISTRY UPDATE — PLAN-ORG-001 — 2026-06-30
────────────────────────────────────────────────────────────────
Source Mode    : MODE 2 (Execution Plan Generation)
Feature Code   : ORG-001
DBS-ID         : DBS-ORG-001
Plan ID        : PLAN-ORG-001 — GOVERNED ✓ — ALIGN GATE PASSED ✓
────────────────────────────────────────────────────────────────
Entities       : ENTITY-ORG-001..008 (7 SHARED + 1 PRIVATE Ref Table)
FIELD-IDs      : FIELD-0001..0094 (94, fully DBF-bound)
APIs           : API-ORG-001..044 (44)
RULE-IDs       : RULE-ORG-001..020 (20)
LOV-IDs        : LOV-ORG-001..006 (6)
ERR-IDs        : ERR-ORG-0001..0018 (18)
SCR-IDs        : SCR-ORG-001..007 (7)
QR-IDs         : QR-ORG-001..019 (19)
DRV-IDs        : DRV-ORG-001..021 (contiguous)
XM-IDs Outbound: None — ROOT MODULE confirmed
XM Inbound Stubs: 4 (Finance/ProfitCenter, Inventory/LocationSite, Layer-3 multi-entity, TBD/Region)
OQ-IDs Open    : OQ-001 (DEFERRED — non-blocking)
AMEND applied  : AMEND-P3-D (PHASE F4 — Frontend Routing & Component Structure)
Gate Status    : ALIGN GATE PASSED ✓
Next Action    : MODE 4A (Pre-flight governance audit) → MODE 3 (Agent execution)
────────────────────────────────────────────────────────────────
```
<!-- PHASE:SECTION-C:END -->

---

<!-- PHASE:SECTION-D:START -->
# SECTION D — TC COVERAGE MATRIX SUMMARY

Full TC blocks live in test-plan-org-001.md (Stage 2, MODE 2.5) — TC-ORG-001..061. Summary only, per Section 6.3 note.

| Coverage Type | Total | Covered | Gap |
|---|---|---|---|
| RULE-IDs | 20 | 20 | 0 |
| API-IDs | 44 | 44 | 0 |
| SCR-IDs | 7 | 7 | 0 |
| ERR-IDs | 18 | 18 | 0 |

Deferred (documented, non-blocking): Department reactivate-after-deactivate path confirmed covered via API-ORG-024 (no gap — see PHASE SEC note). RULE-ORG-012 cross-module uniqueness deferred to Finance module governance per memory continuity. OQ-001 (Region SOFT-READ impact) deferred — non-blocking.

TC Coverage Gate: PASSED ✓ — no GAP ✗ entries; all DEFERRED ⚠ items documented above.
<!-- PHASE:SECTION-D:END -->

---

*End of execution-plan-org-001.md — PLAN-ORG-001*
*Status: GOVERNED ✓ MODE 2 — ALIGN GATE PASSED ✓*
*Next pipeline stage: MODE 4A (Pre-flight Governance Audit) → MODE 3 (Agent Execution) → MODE 4B (Compliance Audit)*
