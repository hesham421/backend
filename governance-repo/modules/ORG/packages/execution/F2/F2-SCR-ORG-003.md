<!-- Source: PHASE:F2 / SUB:SCR-ORG-003 -->

## F2 — SCR-ORG-003 — Region Screen

```
SCREEN INIT:
  1. Check PERM_REGION_VIEW
  2. Load active LegalEntities (for filter + entry LOV)
  3. Load RegionTypes: API-ORG-020 GET /api/v1/org/region-types?active=true → regionTypeOptions
  4. Execute default search (isActiveFl=1)

F2-FACADE — SCR-ORG-003 — Region Management
STATE: regionList, selectedItem, isLoading, lastSearchRequest, legalEntityOptions, regionTypeOptions
OPERATIONS: searchRegions / getRegionById / createRegion / updateRegion / deactivateRegion
            loadLegalEntityOptions / loadRegionTypeOptions
```
