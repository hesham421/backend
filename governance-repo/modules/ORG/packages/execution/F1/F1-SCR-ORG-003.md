<!-- Source: PHASE:F1 / SUB:SCR-ORG-003 -->

## F1 — SCR-ORG-003 — إدارة المناطق

```
Pattern: COMPOSITE PATTERN-1

SEARCH MODEL — RegionSearchModel:
  regionCode    : string (LIKE)
  nameAr        : string (LIKE)
  legalEntityFk : number (EXACT)
  regionTypeId  : number (EXACT — FK to ORG_REGION_TYPE)
  isActiveFl    : number (default 1)
  page, size, sortBy, sortDir

SEARCH RESULT COLUMNS: regionCode, nameAr, nameEn, legalEntityFk (display), regionTypeId (display name), isActiveFl

ENTRY MODEL — RegionFormModel:
  regionCode    : string   READ-ONLY
  nameAr        : string   REQUIRED
  nameEn        : string   REQUIRED
  legalEntityFk : number   REQUIRED   — LOV from API-ORG-013
  regionTypeId  : number   REQUIRED   — LOV from API-ORG-020 (RegionTypes Reference Table)
  notes         : string   OPTIONAL

ACTIONS: New/Edit/Deactivate/Reactivate (PERM_REGION_*)
⚠ No Reactivate button defined in SRS B2 for SCR-ORG-003 — DRV-ORG-006: derived from API-ORG-019 presence
```
