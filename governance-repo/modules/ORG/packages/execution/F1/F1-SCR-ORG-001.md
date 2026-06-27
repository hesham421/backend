<!-- Source: PHASE:F1 / SUB:SCR-ORG-001 -->

## F1 — SCR-ORG-001 — إدارة الكيانات القانونية

```
Pattern: COMPOSITE PATTERN-1 (Search + Entry — CORE-9: one SCR-ID, two UX views)

SEARCH MODEL — LegalEntitySearchModel:
  legalEntityCode : string  (filter — LIKE)
  nameAr          : string  (filter — LIKE)
  nameEn          : string  (filter — LIKE)
  entityTypeId    : string  (filter — EXACT — DETAIL_CODE from LOV-ORG-001)
  isActiveFl      : number  (filter — EXACT — default 1 in UI)
  page            : number  (pagination — 0-based)
  size            : number  (pagination)
  sortBy          : string  (sort — ALLOWED_SORT_FIELDS)
  sortDir         : string  (ASC/DESC)

SEARCH RESULT COLUMNS:
  legalEntityCode, nameAr, nameEn, entityTypeId (display label), isActiveFl

SEARCH ACTIONS:
  New        → navigate to Entry (create mode)     — requires PERM_LEGAL_ENTITY_CREATE
  Edit       → navigate to Entry (edit mode)       — requires PERM_LEGAL_ENTITY_UPDATE
  Deactivate → call API-ORG-005 (soft)             — requires PERM_LEGAL_ENTITY_DELETE (record active)
  Reactivate → call API-ORG-006                    — requires PERM_LEGAL_ENTITY_UPDATE (record inactive)

ENTRY MODEL — LegalEntityFormModel:
  legalEntityCode : string   READ-ONLY  — system-generated (F3-BC-RULE-1)
  nameAr          : string   REQUIRED
  nameEn          : string   REQUIRED
  entityTypeId    : string   REQUIRED   — LOV-ORG-001 dropdown
  notes           : string   OPTIONAL

ENTRY BUTTONS:
  حفظ (Save)      → POST (create) / PUT (update)
  تعطيل          → DELETE soft (edit mode, active record)
  إعادة تفعيل   → PUT reactivate (edit mode, inactive record)
  إلغاء          → navigate back to search
```
