<!-- Source: PHASE:F1 / SUB:SCR-ORG-006 -->

## F1 — SCR-ORG-006 — إدارة مراكز الربح

```
Pattern: COMPOSITE PATTERN-1

SEARCH MODEL — ProfitCenterSearchModel:
  profitCenterCode : string (LIKE)
  nameAr           : string (LIKE)
  legalEntityFk    : number (EXACT)
  isActiveFl       : number (default 1)
  page, size, sortBy, sortDir

ENTRY MODEL — ProfitCenterFormModel:
  profitCenterCode : string   READ-ONLY
  nameAr           : string   REQUIRED
  nameEn           : string   REQUIRED
  legalEntityFk    : number   REQUIRED   — LOV active LegalEntities
  notes            : string   OPTIONAL

ACTIONS: New/Edit/Deactivate (no Reactivate in SRS B2 for SCR-ORG-006)
⚠ DRV-ORG-006: API-ORG-040 exists (Reactivate) but SRS B2 omits Reactivate action — no button in UI
```
