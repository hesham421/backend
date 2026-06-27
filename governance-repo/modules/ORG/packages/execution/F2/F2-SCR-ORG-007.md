<!-- Source: PHASE:F2 / SUB:SCR-ORG-007 -->

## F2 — SCR-ORG-007 — LocationSite Screen

```
SCREEN INIT:
  1. Check PERM_LOCATION_SITE_VIEW
  2. Load LOV-ORG-006: GET /api/lookups/LOCATION_SITE_TYPE?active=true → siteTypeOptions
  3. Load active Branches → branchOptions
  4. Default search (isActiveFl=1)

F2-FACADE — SCR-ORG-007 — LocationSite Management
STATE: locationSiteList, selectedItem, isLoading, lastSearchRequest, siteTypeOptions, branchOptions
OPERATIONS: searchLocationSites / getLocationSiteById / createLocationSite / updateLocationSite / deactivateLocationSite
```
