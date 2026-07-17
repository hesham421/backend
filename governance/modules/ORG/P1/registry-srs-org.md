# REGISTRY EXTRACT — registry-srs-ORG
══════════════════════════════════════════════════════════════════
Module          : Organization (ORG prefix)
Source artifact : srs.md (srs-org-001.md)
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4 audit
══════════════════════════════════════════════════════════════════

## HEADER

| Field | Value |
|---|---|
| Module Name | Organization (الهيكل التنظيمي) |
| Module Prefix | ORG |
| Feature Code | ORG-001 |
| OQ Count | 1 active (OQ-001 — DEFERRED) |
| Gate Status | GOVERNED ✓ MODE 2 — ALIGN GATE PASSED ✓ |

---

## ENTITIES

| ENTITY-ID | Entity Name | Type |
|---|---|---|
| ENTITY-ORG-001 | LegalEntity | SHARED |
| ENTITY-ORG-002 | Branch | SHARED |
| ENTITY-ORG-003 | Region | SHARED |
| ENTITY-ORG-004 | Department | SHARED |
| ENTITY-ORG-005 | CostCenter | SHARED |
| ENTITY-ORG-006 | ProfitCenter | SHARED |
| ENTITY-ORG-007 | LocationSite | SHARED |
| ENTITY-ORG-008 | RegionType | PRIVATE (Reference Table) |

---

## RULES

| RULE-ID | Short Title | Test-Hint |
|---|---|---|
| RULE-ORG-001 | Prevent LegalEntity deactivation — active branches | — |
| RULE-ORG-002 | Prevent LegalEntity deactivation — active profit centers | — |
| RULE-ORG-003 | Prevent Branch deactivation — active departments | — |
| RULE-ORG-004 | Prevent Branch deactivation — active cost centers | — |
| RULE-ORG-005 | Prevent Branch deactivation — active location sites | — |
| RULE-ORG-006 | Prevent Region deactivation — active branches | Check active branches only (is_active_fl=1) — not blocked if linked branches are inactive |
| RULE-ORG-007 | Prevent circular reference — Department tree | — |
| RULE-ORG-008 | Prevent circular reference — CostCenter tree | — |
| RULE-ORG-009 | Prevent SUMMARY Department on transactional records | Enforced at application layer in consuming modules — not in Organization directly |
| RULE-ORG-010 | Prevent SUMMARY CostCenter on transactional records | Enforced at application layer in consuming modules — not in Organization directly |
| RULE-ORG-011 | Business Code immutable after save | — |
| RULE-ORG-012 | Business Code uniqueness within defined scope | — |
| RULE-ORG-013 | Business Code generated via NumberingEngine only | — |
| RULE-ORG-014 | Reject Business Code in Update payload | — |
| RULE-ORG-015 | Name uniqueness within parent scope | — |
| RULE-ORG-016 | Reject audit fields in request payload | — |
| RULE-ORG-017 | Region deactivation — SOFT-READ consumer check/warning | — |
| RULE-ORG-018 | Branch must belong to active LegalEntity | — |
| RULE-ORG-019 | Department/CostCenter/LocationSite require active Branch | — |
| RULE-ORG-020 | node_type_id immutable after save (Department/CostCenter) | — |

---

## LOVs

| LOV-ID | LOV Name |
|---|---|
| LOV-ORG-001 | Legal Entity Type (LEGAL_ENTITY_TYPE) |
| LOV-ORG-002 | Branch Type (BRANCH_TYPE) |
| LOV-ORG-003 | Department Node Type (DEPARTMENT_NODE_TYPE) |
| LOV-ORG-004 | Cost Center Node Type (COST_CENTER_NODE_TYPE) |
| LOV-ORG-005 | Cost Center Type (COST_CENTER_TYPE) |
| LOV-ORG-006 | Location Site Type (LOCATION_SITE_TYPE) |

---

## LIFECYCLE STATES

All entities (ENTITY-ORG-001..007): is_active_fl — two states only: Active (1) ↔ Inactive (0). No workflow, no approval flow. (ENTITY-ORG-008 RegionType also uses is_active_fl.)

---

## DEPENDENCIES

| Type | Target ENTITY-ID | Target Module | XM Candidate |
|---|---|---|---|
| — | None — Organization does not consume external entities | — | No |

Note: ORG-001 is ROOT MODULE — zero outbound XM dependencies.

---

## SCREENS

| SCR-ID | page_code | Screen Name | Pattern |
|---|---|---|---|
| SCR-ORG-001 | LEGAL_ENTITY | إدارة الكيانات القانونية | PATTERN-1 — Search + Entry |
| SCR-ORG-002 | BRANCH | إدارة الفروع | PATTERN-1 — Search + Entry |
| SCR-ORG-003 | REGION | إدارة المناطق | PATTERN-1 — Search + Entry |
| SCR-ORG-004 | DEPARTMENT | إدارة الأقسام | PATTERN-3 — Specialized (Tree Hierarchy) |
| SCR-ORG-005 | COST_CENTER | إدارة مراكز التكلفة | PATTERN-3 — Specialized (Tree Hierarchy) |
| SCR-ORG-006 | PROFIT_CENTER | إدارة مراكز الربح | PATTERN-1 — Search + Entry |
| SCR-ORG-007 | LOCATION_SITE | إدارة مواقع العمل | PATTERN-1 — Search + Entry |

---

## APIs

| API-ID | Method | Endpoint | Owning SCR-ID |
|---|---|---|---|
| API-ORG-001 | POST | /api/v1/org/legal-entities | SCR-ORG-001 |
| API-ORG-002 | GET | /api/v1/org/legal-entities | SCR-ORG-001 |
| API-ORG-003 | PUT | /api/v1/org/legal-entities/{id} | SCR-ORG-001 |
| API-ORG-004 | PUT | /api/v1/org/legal-entities/{id}/deactivate | SCR-ORG-001 |
| API-ORG-005 | PUT | /api/v1/org/legal-entities/{id}/activate | SCR-ORG-001 |
| API-ORG-006 | GET | /api/v1/org/legal-entities/{id} | SCR-ORG-001 |
| API-ORG-007 | POST | /api/v1/org/branches | SCR-ORG-002 |
| API-ORG-008 | GET | /api/v1/org/branches | SCR-ORG-002 |
| API-ORG-009 | PUT | /api/v1/org/branches/{id} | SCR-ORG-002 |
| API-ORG-010 | PUT | /api/v1/org/branches/{id}/deactivate | SCR-ORG-002 |
| API-ORG-011 | PUT | /api/v1/org/branches/{id}/activate | SCR-ORG-002 |
| API-ORG-012 | GET | /api/v1/org/branches/{id} | SCR-ORG-002 |
| API-ORG-013 | POST | /api/v1/org/regions | SCR-ORG-003 |
| API-ORG-014 | GET | /api/v1/org/regions | SCR-ORG-003 |
| API-ORG-015 | PUT | /api/v1/org/regions/{id} | SCR-ORG-003 |
| API-ORG-016 | PUT | /api/v1/org/regions/{id}/deactivate | SCR-ORG-003 |
| API-ORG-017 | PUT | /api/v1/org/regions/{id}/activate | SCR-ORG-003 |
| API-ORG-018 | GET | /api/v1/org/regions/{id} | SCR-ORG-003 |
| API-ORG-019 | POST | /api/v1/org/departments | SCR-ORG-004 |
| API-ORG-020 | GET | /api/v1/org/departments/tree | SCR-ORG-004 |
| API-ORG-021 | GET | /api/v1/org/departments | SCR-ORG-004 |
| API-ORG-022 | PUT | /api/v1/org/departments/{id} | SCR-ORG-004 |
| API-ORG-023 | PUT | /api/v1/org/departments/{id}/deactivate | SCR-ORG-004 |
| API-ORG-024 | PUT | /api/v1/org/departments/{id}/activate | SCR-ORG-004 |
| API-ORG-025 | GET | /api/v1/org/departments/{id} | SCR-ORG-004 |
| API-ORG-026 | POST | /api/v1/org/cost-centers | SCR-ORG-005 |
| API-ORG-027 | GET | /api/v1/org/cost-centers/tree | SCR-ORG-005 |
| API-ORG-028 | GET | /api/v1/org/cost-centers | SCR-ORG-005 |
| API-ORG-029 | PUT | /api/v1/org/cost-centers/{id} | SCR-ORG-005 |
| API-ORG-030 | PUT | /api/v1/org/cost-centers/{id}/deactivate | SCR-ORG-005 |
| API-ORG-031 | PUT | /api/v1/org/cost-centers/{id}/activate | SCR-ORG-005 |
| API-ORG-032 | GET | /api/v1/org/cost-centers/{id} | SCR-ORG-005 |
| API-ORG-033 | POST | /api/v1/org/profit-centers | SCR-ORG-006 |
| API-ORG-034 | GET | /api/v1/org/profit-centers | SCR-ORG-006 |
| API-ORG-035 | PUT | /api/v1/org/profit-centers/{id} | SCR-ORG-006 |
| API-ORG-036 | PUT | /api/v1/org/profit-centers/{id}/deactivate | SCR-ORG-006 |
| API-ORG-037 | PUT | /api/v1/org/profit-centers/{id}/activate | SCR-ORG-006 |
| API-ORG-038 | GET | /api/v1/org/profit-centers/{id} | SCR-ORG-006 |
| API-ORG-039 | POST | /api/v1/org/location-sites | SCR-ORG-007 |
| API-ORG-040 | GET | /api/v1/org/location-sites | SCR-ORG-007 |
| API-ORG-041 | PUT | /api/v1/org/location-sites/{id} | SCR-ORG-007 |
| API-ORG-042 | PUT | /api/v1/org/location-sites/{id}/deactivate | SCR-ORG-007 |
| API-ORG-043 | PUT | /api/v1/org/location-sites/{id}/activate | SCR-ORG-007 |
| API-ORG-044 | GET | /api/v1/org/location-sites/{id} | SCR-ORG-007 |

---

## PERMISSIONS

| PERM Name | Linked SCR-ID(s) |
|---|---|
| PERM_LEGAL_ENTITY_VIEW / CREATE / UPDATE / DELETE | SCR-ORG-001 |
| PERM_BRANCH_VIEW / CREATE / UPDATE / DELETE | SCR-ORG-002 |
| PERM_REGION_VIEW / CREATE / UPDATE / DELETE | SCR-ORG-003 |
| PERM_DEPARTMENT_VIEW / CREATE / UPDATE / DELETE | SCR-ORG-004 |
| PERM_COST_CENTER_VIEW / CREATE / UPDATE / DELETE | SCR-ORG-005 |
| PERM_PROFIT_CENTER_VIEW / CREATE / UPDATE / DELETE | SCR-ORG-006 |
| PERM_LOCATION_SITE_VIEW / CREATE / UPDATE / DELETE | SCR-ORG-007 |

Note: PERM_* generated automatically by Security Engine from PAGE_CODE (SEC-3) — not explicitly defined via INSERT statements in SRS.

---

## OQ LOG STATUS

| OQ-ID | Status | One-line topic | Escalation |
|---|---|---|---|
| OQ-001 | DEFERRED | Region deactivation impact on SOFT-READ consumer modules | XM-ESC-[CONSUMER] |

---
*End of registry-srs-ORG.md*
