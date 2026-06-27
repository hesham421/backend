<!-- Source: PHASE:F3 / SUB:SCR-ORG-003 -->

## F3 — SCR-ORG-003 Validation Rules

```
F3-BC-RULE-1 : regionCode — READ-ONLY

F3-VALIDATION — RULE-ORG-019 — LegalEntity required:
  Field: legalEntityFk | ERR-ID: ERR-0019
  Message-AR: يرجى اختيار كيان قانوني نشط لربط المنطقة به.

F3-VALIDATION — regionTypeId (Reference Table FK):
  Field          : regionTypeId | DB: REGION_TYPE_FK | DBF-0037
  Validation type: REQUIRED + LOV_VALID
  LOV source     : loadRegionTypeOptions() → API-ORG-020 (GET /api/v1/org/region-types?active=true)
  ⚠ DRV-ORG-001: stored as NUMBER (FK) not DETAIL_CODE — frontend sends Long value
  When evaluated : ON_SUBMIT

F3-VALIDATION — RULE-ORG-006 on Deactivate: surface ERR-0006
  Message-AR: لا يمكن تعطيل المنطقة لوجود فروع نشطة مرتبطة بها. يرجى إلغاء ربط الفروع أولاً.
```
