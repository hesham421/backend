<!-- Source: PHASE:F2 / SUB:SCR-ORG-006 -->

## F2 — SCR-ORG-006 — ProfitCenter Screen

```
SCREEN INIT:
  1. Check PERM_PROFIT_CENTER_VIEW
  2. Load active LegalEntities → legalEntityOptions
  3. Default search (isActiveFl=1)

F2-FACADE — SCR-ORG-006 — ProfitCenter Management
STATE: profitCenterList, selectedItem, isLoading, lastSearchRequest, legalEntityOptions
OPERATIONS: searchProfitCenters / getProfitCenterById / createProfitCenter / updateProfitCenter / deactivateProfitCenter
```
