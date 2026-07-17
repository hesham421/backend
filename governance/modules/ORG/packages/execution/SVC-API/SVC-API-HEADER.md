<!-- Source: PHASE:SVC-API / PREAMBLE (before first SUB) -->

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