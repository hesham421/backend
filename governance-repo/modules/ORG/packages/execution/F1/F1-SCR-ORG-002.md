<!-- Source: PHASE:F1 / SUB:SCR-ORG-002 -->

## F1 — SCR-ORG-002 — إدارة الفروع

```
Pattern: COMPOSITE PATTERN-1

SEARCH MODEL — BranchSearchModel:
  branchCode    : string (LIKE)
  nameAr        : string (LIKE)
  legalEntityFk : number (EXACT — selected from active LegalEntities dropdown)
  branchTypeId  : string (EXACT — LOV-ORG-002)
  isActiveFl    : number (default 1)
  page, size, sortBy, sortDir

SEARCH RESULT COLUMNS: branchCode, nameAr, nameEn, legalEntityFk (display), branchTypeId (display), isActiveFl

ENTRY MODEL — BranchFormModel:
  branchCode    : string   READ-ONLY
  nameAr        : string   REQUIRED
  nameEn        : string   REQUIRED
  legalEntityFk : number   REQUIRED   — LOV from API-ORG-013 (active LegalEntities)
  branchTypeId  : string   REQUIRED   — LOV-ORG-002 (BRANCH_TYPE)
  notes         : string   OPTIONAL

ACTIONS: New/Edit/Deactivate/Reactivate (PERM_BRANCH_*)
```
