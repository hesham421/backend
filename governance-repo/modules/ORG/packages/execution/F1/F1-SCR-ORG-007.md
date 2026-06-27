<!-- Source: PHASE:F1 / SUB:SCR-ORG-007 -->

## F1 — SCR-ORG-007 — إدارة المواقع الجغرافية

```
Pattern: COMPOSITE PATTERN-1

SEARCH MODEL — LocationSiteSearchModel:
  locationCode : string (LIKE)
  nameAr       : string (LIKE)
  branchFk     : number (EXACT)
  siteTypeId   : string (EXACT — LOV-ORG-006)
  isActiveFl   : number (default 1)
  page, size, sortBy, sortDir

ENTRY MODEL — LocationSiteFormModel:
  locationCode : string   READ-ONLY
  nameAr       : string   REQUIRED
  nameEn       : string   REQUIRED
  branchFk     : number   REQUIRED   — LOV active Branches
  siteTypeId   : string   REQUIRED   — LOV-ORG-006 (LOCATION_SITE_TYPE)
  notes        : string   OPTIONAL

ACTIONS: New/Edit/Deactivate (SRS B2 — no Reactivate defined for SCR-ORG-007)
```
