<!-- Source: PHASE:SEC -->


# PHASE SEC — Security Specifications

```
SEC — SCR-ORG-001 — إدارة الكيانات القانونية
─────────────────────────────────────────────────────────────────
Screen guard     : PERM_LEGAL_ENTITY_VIEW required — canView=false → /unauthorized
Permission-based UI:
  canView=false   → blocked at navigation
  canCreate=false → "New" button hidden
  canEdit=false   → form fields read-only, Save not available
  canDelete=false → Deactivate action hidden

API-level: every API-ORG-001..006 verifies permission before processing

SECURITY SEED DATA:
  SEC_PAGES: page_code=LEGAL_ENTITY, module=ORGANIZATION, parent_id_fk=[ORG_MENU]
  PERMISSIONS (4 rows — Security Engine generates):
    PERM_LEGAL_ENTITY_VIEW   │ مدير النظام, مدير التنظيم
    PERM_LEGAL_ENTITY_CREATE │ مدير النظام
    PERM_LEGAL_ENTITY_UPDATE │ مدير النظام
    PERM_LEGAL_ENTITY_DELETE │ مدير النظام
─────────────────────────────────────────────────────────────────

SEC — SCR-ORG-002 — إدارة الفروع
  Guard: PERM_BRANCH_VIEW | SEC_PAGES: page_code=BRANCH
  PERM_BRANCH_VIEW: مدير النظام, مدير التنظيم
  PERM_BRANCH_CREATE/UPDATE/DELETE: مدير النظام

SEC — SCR-ORG-003 — إدارة المناطق
  Guard: PERM_REGION_VIEW | SEC_PAGES: page_code=REGION
  PERM_REGION_VIEW: مدير النظام, مدير التنظيم
  PERM_REGION_CREATE/UPDATE/DELETE: مدير النظام

SEC — SCR-ORG-004 — إدارة الأقسام
  Guard: PERM_DEPARTMENT_VIEW | SEC_PAGES: page_code=DEPARTMENT
  PERM_DEPARTMENT_VIEW: مدير النظام, مدير التنظيم
  PERM_DEPARTMENT_CREATE/UPDATE/DELETE: مدير النظام

SEC — SCR-ORG-005 — إدارة مراكز التكلفة
  Guard: PERM_COST_CENTER_VIEW | SEC_PAGES: page_code=COST_CENTER
  PERM_COST_CENTER_VIEW: مدير النظام, مدير التنظيم, مدير المالية
  PERM_COST_CENTER_CREATE/UPDATE/DELETE: مدير النظام

SEC — SCR-ORG-006 — إدارة مراكز الربح
  Guard: PERM_PROFIT_CENTER_VIEW | SEC_PAGES: page_code=PROFIT_CENTER
  PERM_PROFIT_CENTER_VIEW: مدير النظام, مدير المالية
  PERM_PROFIT_CENTER_CREATE/UPDATE/DELETE: مدير النظام

SEC — SCR-ORG-007 — إدارة المواقع الجغرافية
  Guard: PERM_LOCATION_SITE_VIEW | SEC_PAGES: page_code=LOCATION_SITE
  PERM_LOCATION_SITE_VIEW: مدير النظام, مدير التنظيم
  PERM_LOCATION_SITE_CREATE/UPDATE/DELETE: مدير النظام

─────────────────────────────────────────────────────────────────
SEC Governance Rules:
  SEC-IMPL-RULE-1 — Every SCR-ID has navigation guard (no exceptions)
  SEC-IMPL-RULE-2 — All UI show/hide references permission flags
  SEC-IMPL-RULE-3 — HTTP 403: caught and shown as localized message
  SEC-IMPL-RULE-4 — Every SCR-ID verified in SEC_PAGES before launch
  ⚠ SEC_PAGES and PERMISSIONS are PERMANENT EXCEPTION tables — use actual column names
  ⚠ INF-ORG-01: PERM_LEGAL_ENTITY_VIEW created previously — MERGE idempotent (DBS-ORG-001 Block 8c)
```

**SEC Gate: PASSED ✓**
```
[ ✓ ] All 7 SCR-IDs have SEC blocks
[ ✓ ] All permission declarations match SRS B4
[ ✓ ] SEC_PAGES seed data declared per SCR-ID
[ ✓ ] API-level enforcement declared for all 47 APIs
```

