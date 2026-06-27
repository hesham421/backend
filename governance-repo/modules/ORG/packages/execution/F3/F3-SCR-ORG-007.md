<!-- Source: PHASE:F3 / SUB:SCR-ORG-007 -->

## F3 — SCR-ORG-007 Validation Rules

```
F3-BC-RULE-1 : locationCode — READ-ONLY

F3-VALIDATION — branchFk: REQUIRED (no specific RULE-ORG for LocationSite branchFk in SRS — FK constraint)
  DRV-ORG-008: branchFk required per NOT NULL DB constraint — no explicit RULE-ID for LocationSite branchFk
               Validation: REQUIRED on form + server-side DB constraint

F3-VALIDATION — siteTypeId:
  LOV-ID: LOV-ORG-006 | LOOKUP_CODE: LOCATION_SITE_TYPE
  Endpoint: GET /api/lookups/LOCATION_SITE_TYPE?active=true
  Validation: REQUIRED + LOV_VALID

F3-VALIDATION — nameAr/nameEn: REQUIRED + LENGTH constraints
```
