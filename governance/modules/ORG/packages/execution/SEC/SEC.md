<!-- Source: PHASE:SEC -->

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
