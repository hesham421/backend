<!-- Source: PHASE:PLAN-INDEX -->

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
