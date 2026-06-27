# REGISTRY EXTRACT — registry-srs-ORG.md
══════════════════════════════════════════════════════════════════
Module          : Organization (ORG)
Source artifact : srs-org-001.md
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4 audit
══════════════════════════════════════════════════════════════════

---

## HEADER

| Field             | Value                                      |
|-------------------|--------------------------------------------|
| Module Name       | Organization — موديول التنظيم المؤسسي      |
| Module Prefix     | ORG                                        |
| OQ Count          | 1 active (OQ-001)                          |
| Governance Status | GOVERNED ✓                                 |

---

## ENTITIES

| ENTITY-ID      | Entity Name   | Type    |
|----------------|---------------|---------|
| ENTITY-ORG-001 | LegalEntity   | PRIVATE |
| ENTITY-ORG-002 | Branch        | PRIVATE |
| ENTITY-ORG-003 | Region        | SHARED  |
| ENTITY-ORG-004 | Department    | PRIVATE |
| ENTITY-ORG-005 | CostCenter    | PRIVATE |
| ENTITY-ORG-006 | ProfitCenter  | PRIVATE |
| ENTITY-ORG-007 | LocationSite  | PRIVATE |
| ENTITY-ORG-008 | RegionType    | PRIVATE |

---

## RULES

| RULE-ID       | Short Title                                      | Test-Hint                                                                                              |
|---------------|--------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| RULE-ORG-001  | No deactivate LegalEntity with active Branches   | —                                                                                                      |
| RULE-ORG-002  | No deactivate LegalEntity with active ProfitCenters | —                                                                                                   |
| RULE-ORG-003  | No deactivate Branch with active Departments     | —                                                                                                      |
| RULE-ORG-004  | No deactivate Branch with active CostCenters     | —                                                                                                      |
| RULE-ORG-005  | No deactivate Branch with active LocationSites   | —                                                                                                      |
| RULE-ORG-006  | No deactivate Region with active Branches        | —                                                                                                      |
| RULE-ORG-007  | No circular ref in Department tree               | —                                                                                                      |
| RULE-ORG-008  | No circular ref in CostCenter tree               | —                                                                                                      |
| RULE-ORG-009  | No SUMMARY Department on transactions            | —                                                                                                      |
| RULE-ORG-010  | No SUMMARY CostCenter on transactions            | —                                                                                                      |
| RULE-ORG-011  | Business codes immutable after first save        | تحقق من أن حقل الرمز غير موجود في Update DTO بالكلية — لا مجرد validation                            |
| RULE-ORG-012  | Business codes unique within defined scope       | —                                                                                                      |
| RULE-ORG-013  | Branch creation requires active LegalEntity      | —                                                                                                      |
| RULE-ORG-014  | Department creation requires active Branch       | —                                                                                                      |
| RULE-ORG-015  | CostCenter creation requires active Branch       | —                                                                                                      |
| RULE-ORG-016  | Business codes via NumberingEngine only          | —                                                                                                      |
| RULE-ORG-017  | Parent Department must be active                 | —                                                                                                      |
| RULE-ORG-018  | Parent CostCenter must be active                 | —                                                                                                      |
| RULE-ORG-019  | Region creation requires active LegalEntity      | —                                                                                                      |
| RULE-ORG-020  | ProfitCenter creation requires active LegalEntity| —                                                                                                      |

---

## LOVs

| LOV-ID      | LOV Name                  |
|-------------|---------------------------|
| LOV-ORG-001 | LEGAL_ENTITY_TYPE         |
| LOV-ORG-002 | BRANCH_TYPE               |
| LOV-ORG-003 | DEPARTMENT_NODE_TYPE      |
| LOV-ORG-004 | COST_CENTER_NODE_TYPE     |
| LOV-ORG-005 | COST_CENTER_TYPE          |
| LOV-ORG-006 | LOCATION_SITE_TYPE        |
| LOV-ORG-007 | REGION_TYPE (Reference Table) |

---

## LIFECYCLE STATES

All 8 entities (ENTITY-ORG-001 through ENTITY-ORG-008) use isActiveFl exclusively.
States: نشط (isActiveFl=1), غير نشط (isActiveFl=0) — no complex lifecycle.

---

## DEPENDENCIES

Note: Organization is the ROOT module — no outbound dependencies.
Table below documents inbound consumers (informational — per A7).

| Type        | Target ENTITY-ID                      | Target Module          | XM Candidate |
|-------------|---------------------------------------|------------------------|--------------|
| HARD-FK     | Branch                                | Security (1.2)         | No           |
| HARD-FK     | LegalEntity, Branch                   | MasterData (1.4)       | No           |
| HARD-FK     | LegalEntity                           | CurrencyCalendar (1.5) | No           |
| HARD-FK     | Branch                                | NumberingEngine (1.6)  | No           |
| HARD-FK     | Branch                                | FileService (1.10)     | No           |
| HARD-FK     | Branch                                | NotificationService (1.8) | No        |
| HARD-FK     | Branch                                | AuditService (1.9)     | No           |
| HARD-FK     | Branch, CostCenter                    | All Layer-2 modules    | No           |
| HARD-FK     | Branch, Department, CostCenter, ProfitCenter | All Layer-3 modules | No       |
| HARD-FK     | LocationSite                          | Inventory (3.2)        | No           |
| HARD-FK     | CostCenter, ProfitCenter              | Finance (3.4)          | No           |
| SOFT-READ   | Region                                | TBD (AQ-003 — deferred)| Yes          |

---

## SCREENS

| SCR-ID      | page_code      | Screen Name                    | Pattern                              |
|-------------|----------------|--------------------------------|--------------------------------------|
| SCR-ORG-001 | LEGAL_ENTITY   | إدارة الكيانات القانونية       | PATTERN-1 — Search + Entry           |
| SCR-ORG-002 | BRANCH         | إدارة الفروع                   | PATTERN-1 — Search + Entry           |
| SCR-ORG-003 | REGION         | إدارة المناطق                  | PATTERN-1 — Search + Entry           |
| SCR-ORG-004 | DEPARTMENT     | إدارة الأقسام                  | PATTERN-3 — Specialized (Hierarchical Tree) |
| SCR-ORG-005 | COST_CENTER    | إدارة مراكز التكلفة            | PATTERN-3 — Specialized (Hierarchical Tree) |
| SCR-ORG-006 | PROFIT_CENTER  | إدارة مراكز الربح              | PATTERN-1 — Search + Entry           |
| SCR-ORG-007 | LOCATION_SITE  | إدارة المواقع الجغرافية        | PATTERN-1 — Search + Entry           |

---

## APIs

| API-ID      | Method | Endpoint                                          | Owning SCR-ID |
|-------------|--------|---------------------------------------------------|---------------|
| API-ORG-001 | POST   | /api/v1/org/legal-entities                        | SCR-ORG-001   |
| API-ORG-002 | GET    | /api/v1/org/legal-entities                        | SCR-ORG-001   |
| API-ORG-003 | GET    | /api/v1/org/legal-entities/{id}                   | SCR-ORG-001   |
| API-ORG-004 | PUT    | /api/v1/org/legal-entities/{id}                   | SCR-ORG-001   |
| API-ORG-005 | DELETE | /api/v1/org/legal-entities/{id}                   | SCR-ORG-001   |
| API-ORG-006 | PUT    | /api/v1/org/legal-entities/{id}/reactivate        | SCR-ORG-001   |
| API-ORG-007 | POST   | /api/v1/org/branches                              | SCR-ORG-002   |
| API-ORG-008 | GET    | /api/v1/org/branches                              | SCR-ORG-002   |
| API-ORG-009 | GET    | /api/v1/org/branches/{id}                         | SCR-ORG-002   |
| API-ORG-010 | PUT    | /api/v1/org/branches/{id}                         | SCR-ORG-002   |
| API-ORG-011 | DELETE | /api/v1/org/branches/{id}                         | SCR-ORG-002   |
| API-ORG-012 | PUT    | /api/v1/org/branches/{id}/reactivate              | SCR-ORG-002   |
| API-ORG-013 | GET    | /api/v1/org/branches/by-legal-entity/{leId}       | SCR-ORG-002   |
| API-ORG-014 | POST   | /api/v1/org/regions                               | SCR-ORG-003   |
| API-ORG-015 | GET    | /api/v1/org/regions                               | SCR-ORG-003   |
| API-ORG-016 | GET    | /api/v1/org/regions/{id}                          | SCR-ORG-003   |
| API-ORG-017 | PUT    | /api/v1/org/regions/{id}                          | SCR-ORG-003   |
| API-ORG-018 | DELETE | /api/v1/org/regions/{id}                          | SCR-ORG-003   |
| API-ORG-019 | PUT    | /api/v1/org/regions/{id}/reactivate               | SCR-ORG-003   |
| API-ORG-020 | GET    | /api/v1/org/region-types                          | SCR-ORG-003   |
| API-ORG-021 | POST   | /api/v1/org/departments                           | SCR-ORG-004   |
| API-ORG-022 | GET    | /api/v1/org/departments/tree                      | SCR-ORG-004   |
| API-ORG-023 | GET    | /api/v1/org/departments                           | SCR-ORG-004   |
| API-ORG-024 | GET    | /api/v1/org/departments/{id}                      | SCR-ORG-004   |
| API-ORG-025 | PUT    | /api/v1/org/departments/{id}                      | SCR-ORG-004   |
| API-ORG-026 | DELETE | /api/v1/org/departments/{id}                      | SCR-ORG-004   |
| API-ORG-027 | PUT    | /api/v1/org/departments/{id}/reactivate           | SCR-ORG-004   |
| API-ORG-028 | POST   | /api/v1/org/cost-centers                          | SCR-ORG-005   |
| API-ORG-029 | GET    | /api/v1/org/cost-centers/tree                     | SCR-ORG-005   |
| API-ORG-030 | GET    | /api/v1/org/cost-centers                          | SCR-ORG-005   |
| API-ORG-031 | GET    | /api/v1/org/cost-centers/{id}                     | SCR-ORG-005   |
| API-ORG-032 | PUT    | /api/v1/org/cost-centers/{id}                     | SCR-ORG-005   |
| API-ORG-033 | DELETE | /api/v1/org/cost-centers/{id}                     | SCR-ORG-005   |
| API-ORG-034 | PUT    | /api/v1/org/cost-centers/{id}/reactivate          | SCR-ORG-005   |
| API-ORG-035 | POST   | /api/v1/org/profit-centers                        | SCR-ORG-006   |
| API-ORG-036 | GET    | /api/v1/org/profit-centers                        | SCR-ORG-006   |
| API-ORG-037 | GET    | /api/v1/org/profit-centers/{id}                   | SCR-ORG-006   |
| API-ORG-038 | PUT    | /api/v1/org/profit-centers/{id}                   | SCR-ORG-006   |
| API-ORG-039 | DELETE | /api/v1/org/profit-centers/{id}                   | SCR-ORG-006   |
| API-ORG-040 | PUT    | /api/v1/org/profit-centers/{id}/reactivate        | SCR-ORG-006   |
| API-ORG-041 | POST   | /api/v1/org/location-sites                        | SCR-ORG-007   |
| API-ORG-042 | GET    | /api/v1/org/location-sites                        | SCR-ORG-007   |
| API-ORG-043 | GET    | /api/v1/org/location-sites/{id}                   | SCR-ORG-007   |
| API-ORG-044 | PUT    | /api/v1/org/location-sites/{id}                   | SCR-ORG-007   |
| API-ORG-045 | DELETE | /api/v1/org/location-sites/{id}                   | SCR-ORG-007   |
| API-ORG-046 | PUT    | /api/v1/org/location-sites/{id}/reactivate        | SCR-ORG-007   |
| API-ORG-047 | GET    | /api/v1/org/location-sites/by-branch/{branchId}   | SCR-ORG-007   |

---

## PERMISSIONS

| PERM Name                   | Linked SCR-ID(s) |
|-----------------------------|------------------|
| PERM_LEGAL_ENTITY_VIEW      | SCR-ORG-001      |
| PERM_LEGAL_ENTITY_CREATE    | SCR-ORG-001      |
| PERM_LEGAL_ENTITY_UPDATE    | SCR-ORG-001      |
| PERM_LEGAL_ENTITY_DELETE    | SCR-ORG-001      |
| PERM_BRANCH_VIEW            | SCR-ORG-002      |
| PERM_BRANCH_CREATE          | SCR-ORG-002      |
| PERM_BRANCH_UPDATE          | SCR-ORG-002      |
| PERM_BRANCH_DELETE          | SCR-ORG-002      |
| PERM_REGION_VIEW            | SCR-ORG-003      |
| PERM_REGION_CREATE          | SCR-ORG-003      |
| PERM_REGION_UPDATE          | SCR-ORG-003      |
| PERM_REGION_DELETE          | SCR-ORG-003      |
| PERM_DEPARTMENT_VIEW        | SCR-ORG-004      |
| PERM_DEPARTMENT_CREATE      | SCR-ORG-004      |
| PERM_DEPARTMENT_UPDATE      | SCR-ORG-004      |
| PERM_DEPARTMENT_DELETE      | SCR-ORG-004      |
| PERM_COST_CENTER_VIEW       | SCR-ORG-005      |
| PERM_COST_CENTER_CREATE     | SCR-ORG-005      |
| PERM_COST_CENTER_UPDATE     | SCR-ORG-005      |
| PERM_COST_CENTER_DELETE     | SCR-ORG-005      |
| PERM_PROFIT_CENTER_VIEW     | SCR-ORG-006      |
| PERM_PROFIT_CENTER_CREATE   | SCR-ORG-006      |
| PERM_PROFIT_CENTER_UPDATE   | SCR-ORG-006      |
| PERM_PROFIT_CENTER_DELETE   | SCR-ORG-006      |
| PERM_LOCATION_SITE_VIEW     | SCR-ORG-007      |
| PERM_LOCATION_SITE_CREATE   | SCR-ORG-007      |
| PERM_LOCATION_SITE_UPDATE   | SCR-ORG-007      |
| PERM_LOCATION_SITE_DELETE   | SCR-ORG-007      |

---

## OQ LOG STATUS

| OQ-ID  | Status   | One-line Topic                                        | Escalation  |
|--------|----------|-------------------------------------------------------|-------------|
| OQ-001 | DEFERRED | Region deactivation impact on SOFT-READ consumers     | XM-ESC-TBD (AQ-003 → master-registry Section 14) |
