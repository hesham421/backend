<!-- Source: PHASE:F2 / SUB:SCR-ORG-002 -->

## F2 — SCR-ORG-002 — Branch Screen

```
SCREEN INIT:
  1. Check PERM_BRANCH_VIEW → redirect if false
  2. Load LOV-ORG-002: GET /api/lookups/BRANCH_TYPE?active=true → branchTypeOptions
  3. Load active LegalEntities: API-ORG-013 (by-legal-entity not applicable here)
     → Use API-ORG-002 with isActiveFl=1, no pagination (or API-ORG-013 with a fixed leId if pre-filtered)
     DRV-ORG-007: For Branch search screen LegalEntity filter, load active LEs via API-ORG-002 (isActiveFl=1, no page limit)
  4. Execute default search (isActiveFl=1)

F2-FACADE — SCR-ORG-002 — Branch Management
STATE: branchList, selectedItem, isLoading, lastSearchRequest, branchTypeOptions, legalEntityOptions
OPERATIONS: searchBranches / getBranchById / createBranch / updateBranch / deactivateBranch / reactivateBranch
            loadBranchTypeOptions / loadLegalEntityOptions
```
