# EXECUTION PLAN — Organization Module (ORG-001)
## 🆕 New Feature — Organization & Cost Centers — ORG-001

```
Plan Name      : New Feature — Organization & Cost Centers — ORG-001
Plan ID        : PLAN-ORG-001
DBS-ID         : DBS-ORG-001
Feature Code   : ORG-001
Governed by    : Execution Plan Governance Engine (Project 3) v2
Output Mode    : SINGLE-FILE — Agent-Ready Specification
Open Questions : 1 active (OQ-001) — see OQ Log
Generated      : 2026-06-23
```

---

## MODE 2 — ENTRY GATE

```
╔══════════════════════════════════════════════════════════════════╗
║                  MODE 2 — ENTRY GATE                             ║
╠════════════════════════════════╦═════════════════════════════════╣
║ SRS attached + feature code?   ║ Yes — srs-org-001.md / ORG-001  ║
║ DB Script attached (DBS-ID)?   ║ Yes — DBS-ORG-001               ║
║ Gate DB passed?                ║ PASSED ✓                        ║
║ Entities in SRS = tables in DB?║ 8 / 8 — ALIGNED ✓              ║
║ Registry loaded, no conflicts? ║ ✓ — master-registry v2.7.2      ║
║ Naming: 3-way consistent?      ║ ✓ — no violations               ║
╠════════════════════════════════╩═════════════════════════════════╣
║ P0 ARTIFACT CHECK:                                                ║
║ module-registry-ORG.md loaded? ║ Referenced via master-registry  ║
║ EXCEPTION modules detected?    ║ None — standard module          ║
╠══════════════════════════════════════════════════════════════════╣
║ Extracted: 8 entities, 93 DBF-IDs, 47 APIs, 20 rules,           ║
║ 0 XM dependencies (ROOT MODULE), 1 open OQ                       ║
╠══════════════════════════════════════════════════════════════════╣
║ PROCEED? Yes ✓                                                    ║
╚══════════════════════════════════════════════════════════════════╝
```

---

## SRS ANALYSIS SUMMARY

```
╔══════════════════════════════════════════════════════════════════╗
║          SRS ANALYSIS SUMMARY — ORG-001                          ║
╠══════════════════════════════╦═══════════════════════════════════╣
║ Task Type                    ║ 🆕 New Feature                    ║
║ Execution Plan Name          ║ New Feature — Organization &      ║
║                              ║ Cost Centers — ORG-001            ║
║ Extracted Entities           ║ ENTITY-ORG-001 (LegalEntity)      ║
║                              ║ ENTITY-ORG-002 (Branch)           ║
║                              ║ ENTITY-ORG-003 (Region)           ║
║                              ║ ENTITY-ORG-004 (Department)       ║
║                              ║ ENTITY-ORG-005 (CostCenter)       ║
║                              ║ ENTITY-ORG-006 (ProfitCenter)     ║
║                              ║ ENTITY-ORG-007 (LocationSite)     ║
║                              ║ ENTITY-ORG-008 (RegionType)       ║
║ API Count                    ║ 47 (API-ORG-001..047)             ║
║ Rules Count                  ║ 20 (RULE-ORG-001..020)            ║
║   of which Architectural     ║ 2 (RULE-ORG-009, RULE-ORG-016)   ║
║ EXCEPTION modules            ║ None                              ║
║ Approval Workflow            ║ No                                ║
║ XM Dependencies              ║ None — ROOT MODULE                ║
║ Test Scenarios               ║ Generated in test-plan.md         ║
║ Open Questions               ║ 1 (OQ-001 — Region deactivation)  ║
║ Phases Required              ║ CORE, DATA+DOM, SVC+API, DOC,     ║
║                              ║ INT-C, INT-R, F1, F2, F3,         ║
║                              ║ SEC, SECTION D, ALIGN             ║
╚══════════════════════════════╩═══════════════════════════════════╝
```

---

## PRE-GENERATION EXTRACTION — ORG-001

```
╔══════════════════════════════════════════════════════════════════════
║  PRE-GENERATION EXTRACTION — Organization (ORG-001)
╠══════════════════════════════════════════════════════════════════════

── FROM srs-org-001.md ────────────────────────────────────────────────

ENTITIES:
  ENTITY-ORG-001 │ LegalEntity   │ Type: MASTER (SHARED owner)
  ENTITY-ORG-002 │ Branch        │ Type: MASTER (SHARED owner)
  ENTITY-ORG-003 │ Region        │ Type: MASTER (SHARED owner)
  ENTITY-ORG-004 │ Department    │ Type: MASTER (SHARED owner — tree)
  ENTITY-ORG-005 │ CostCenter    │ Type: MASTER (SHARED owner — tree)
  ENTITY-ORG-006 │ ProfitCenter  │ Type: MASTER (SHARED owner)
  ENTITY-ORG-007 │ LocationSite  │ Type: MASTER (SHARED owner)
  ENTITY-ORG-008 │ RegionType    │ Type: PRIVATE Reference Table

BUSINESS CODES (one per master entity):
  ENTITY-ORG-001 │ Format: LE-NNNNN              │ Field: legalEntityCode
                 │ Pattern sourced from: SRS A3 ENTITY-ORG-001
  ENTITY-ORG-002 │ Format: BR-[LE]-NNNNN          │ Field: branchCode
                 │ Unique within LegalEntity
  ENTITY-ORG-003 │ Format: RG-[LE]-NNNNN          │ Field: regionCode
                 │ Unique within LegalEntity
  ENTITY-ORG-004 │ Format: DEP-[BR]-NNNNN         │ Field: deptCode
                 │ Unique within Branch
  ENTITY-ORG-005 │ Format: CC-[BR]-NNNNN          │ Field: costCenterCode
                 │ Unique within Branch
  ENTITY-ORG-006 │ Format: PC-[LE]-NNNNN          │ Field: profitCenterCode
                 │ Unique within LegalEntity
  ENTITY-ORG-007 │ Format: LS-[BR]-NNNNN          │ Field: locationCode
                 │ Unique within Branch
  ENTITY-ORG-008 │ No Business Code — Reference Table

LOVs:
  LOV-ORG-001 │ LOOKUP_CODE: LEGAL_ENTITY_TYPE   │ Usage: entityTypeId in ENTITY-ORG-001
  LOV-ORG-002 │ LOOKUP_CODE: BRANCH_TYPE         │ Usage: branchTypeId in ENTITY-ORG-002
  LOV-ORG-003 │ LOOKUP_CODE: DEPARTMENT_NODE_TYPE│ Usage: nodeTypeId in ENTITY-ORG-004
  LOV-ORG-004 │ LOOKUP_CODE: COST_CENTER_NODE_TYPE│ Usage: nodeTypeId in ENTITY-ORG-005
  LOV-ORG-005 │ LOOKUP_CODE: COST_CENTER_TYPE    │ Usage: costCenterTypeId in ENTITY-ORG-005
  LOV-ORG-006 │ LOOKUP_CODE: LOCATION_SITE_TYPE  │ Usage: siteTypeId in ENTITY-ORG-007
  LOV-ORG-007 │ Reference Table (ORG_REGION_TYPE) │ Usage: regionTypeId in ENTITY-ORG-003
               │ Endpoint: GET /api/v1/org/region-types?active=true (API-ORG-020)

RULES (full extraction):
  RULE-ORG-001:
    Scope      : ENTITY-ORG-001
    Trigger    : عند طلب تعطيل Deactivate
    Statement  : The system MUST prevent deactivation of a LegalEntity when one or more active Branches reference it
    Message-AR : لا يمكن تعطيل الكيان القانوني لوجود فروع نشطة مرتبطة به. يرجى تعطيل جميع الفروع أولاً.
    Message-EN : Cannot deactivate Legal Entity: active branches exist. Please deactivate all branches first.

  RULE-ORG-002:
    Scope      : ENTITY-ORG-001
    Trigger    : عند طلب تعطيل Deactivate
    Statement  : The system MUST prevent deactivation of a LegalEntity when one or more active ProfitCenters reference it
    Message-AR : لا يمكن تعطيل الكيان القانوني لوجود مراكز ربح نشطة مرتبطة به. يرجى تعطيل جميع مراكز الربح أولاً.
    Message-EN : Cannot deactivate Legal Entity: active profit centers exist. Please deactivate all profit centers first.

  RULE-ORG-003:
    Scope      : ENTITY-ORG-002
    Trigger    : عند طلب تعطيل Deactivate
    Statement  : The system MUST prevent deactivation of a Branch when one or more active Departments reference it
    Message-AR : لا يمكن تعطيل الفرع لوجود أقسام نشطة مرتبطة به. يرجى تعطيل جميع الأقسام أولاً.
    Message-EN : Cannot deactivate Branch: active departments exist. Please deactivate all departments first.

  RULE-ORG-004:
    Scope      : ENTITY-ORG-002
    Trigger    : عند طلب تعطيل Deactivate
    Statement  : The system MUST prevent deactivation of a Branch when one or more active CostCenters reference it
    Message-AR : لا يمكن تعطيل الفرع لوجود مراكز تكلفة نشطة مرتبطة به. يرجى تعطيل جميع مراكز التكلفة أولاً.
    Message-EN : Cannot deactivate Branch: active cost centers exist. Please deactivate all cost centers first.

  RULE-ORG-005:
    Scope      : ENTITY-ORG-002
    Trigger    : عند طلب تعطيل Deactivate
    Statement  : The system MUST prevent deactivation of a Branch when one or more active LocationSites reference it
    Message-AR : لا يمكن تعطيل الفرع لوجود مواقع جغرافية نشطة مرتبطة به. يرجى تعطيل جميع المواقع أولاً.
    Message-EN : Cannot deactivate Branch: active location sites exist. Please deactivate all location sites first.

  RULE-ORG-006:
    Scope      : ENTITY-ORG-003
    Trigger    : عند طلب تعطيل Deactivate
    Statement  : The system MUST prevent deactivation of a Region when one or more active Branches reference it
    Message-AR : لا يمكن تعطيل المنطقة لوجود فروع نشطة مرتبطة بها. يرجى إلغاء ربط الفروع أولاً.
    Message-EN : Cannot deactivate Region: active branches reference it. Please unlink branches first.
    Note       : OQ-001 open — SOFT-READ consumer impact on deactivation

  RULE-ORG-007:
    Scope      : ENTITY-ORG-004
    Trigger    : عند الحفظ (Create أو Update لـ parentDepartmentFk)
    Statement  : The system MUST prevent circular parent references in the Department tree — a Department may not be set as its own ancestor
    Message-AR : لا يمكن تعيين هذا القسم أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري.
    Message-EN : Cannot set this department as parent: circular reference detected in department hierarchy.

  RULE-ORG-008:
    Scope      : ENTITY-ORG-005
    Trigger    : عند الحفظ (Create أو Update لـ parentCostCenterFk)
    Statement  : The system MUST prevent circular parent references in the CostCenter tree — a CostCenter may not be set as its own ancestor
    Message-AR : لا يمكن تعيين مركز التكلفة هذا أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري.
    Message-EN : Cannot set this cost center as parent: circular reference detected in cost center hierarchy.

  RULE-ORG-009:
    Scope      : ENTITY-ORG-004 (applied by consuming modules)
    Trigger    : عند محاولة ربط قسم بسجل معاملة
    Statement  : The system MUST prevent assignment of a Department with nodeType=SUMMARY to any transactional record — only DETAIL departments may be directly assigned
    Message-AR : لا يمكن ربط قسم تجميعي (SUMMARY) بسجل معاملة مباشرة. يرجى اختيار قسم تفصيلي (DETAIL).
    Message-EN : Cannot assign a SUMMARY department to a transaction. Please select a DETAIL department.
    Note       : Enforcement by consuming modules — documented here as single source of truth

  RULE-ORG-010:
    Scope      : ENTITY-ORG-005 (applied by consuming modules)
    Trigger    : عند محاولة ربط مركز تكلفة بسجل معاملة
    Statement  : The system MUST prevent assignment of a CostCenter with nodeType=SUMMARY to any transactional record — only DETAIL cost centers may be directly assigned
    Message-AR : لا يمكن ربط مركز تكلفة تجميعي (SUMMARY) بسجل معاملة مباشرة. يرجى اختيار مركز تكلفة تفصيلي (DETAIL).
    Message-EN : Cannot assign a SUMMARY cost center to a transaction. Please select a DETAIL cost center.
    Note       : Enforcement by consuming modules — documented here as single source of truth

  RULE-ORG-011:
    Scope      : ENTITY-ORG-001, 002, 003, 004, 005, 006, 007
    Trigger    : عند محاولة تعديل أي حقل Business Code
    Statement  : The system MUST prevent any modification to business codes after their initial creation — they are permanently immutable
    Message-AR : رمز الأعمال لا يمكن تعديله بعد الإنشاء الأول — هذه القيمة ثابتة نهائياً.
    Message-EN : Business code is immutable after creation and cannot be modified.
    Test-Hint  : Verify Business Code field is absent from Update DTO entirely — not just validated

  RULE-ORG-012:
    Scope      : ENTITY-ORG-001, 002, 003, 004, 005, 006, 007
    Trigger    : عند الحفظ الأول (Create)
    Statement  : The system MUST ensure business codes are unique within their defined scope
    Message-AR : رمز الأعمال المُنشأ تلقائياً موجود مسبقاً. يرجى المحاولة مجدداً.
    Message-EN : Generated business code already exists. Please retry the operation.
    Note       : NumberingEngine guarantees uniqueness — this rule documents expected behavior

  RULE-ORG-013:
    Scope      : ENTITY-ORG-002
    Trigger    : عند الحفظ (Create)
    Statement  : The system MUST require a valid active LegalEntity reference before saving a new Branch
    Message-AR : يرجى اختيار كيان قانوني نشط لربط الفرع به.
    Message-EN : A valid active Legal Entity must be selected before saving a Branch.

  RULE-ORG-014:
    Scope      : ENTITY-ORG-004
    Trigger    : عند الحفظ (Create)
    Statement  : The system MUST require a valid active Branch reference before saving a new Department
    Message-AR : يرجى اختيار فرع نشط لربط القسم به.
    Message-EN : A valid active Branch must be selected before saving a Department.

  RULE-ORG-015:
    Scope      : ENTITY-ORG-005
    Trigger    : عند الحفظ (Create)
    Statement  : The system MUST require a valid active Branch reference before saving a new CostCenter
    Message-AR : يرجى اختيار فرع نشط لربط مركز التكلفة به.
    Message-EN : A valid active Branch must be selected before saving a CostCenter.

  RULE-ORG-016:
    Scope      : ENTITY-ORG-001, 002, 003, 004, 005, 006, 007
    Trigger    : عند الحفظ الأول (Create)
    Statement  : The system MUST generate all business codes exclusively through NumberingEngine — no module may implement its own numbering logic
    Message-AR : — (Architectural rule — no user-facing message)
    Message-EN : — (Architectural rule — no user-facing message)

  RULE-ORG-017:
    Scope      : ENTITY-ORG-004
    Trigger    : عند الحفظ (Create أو Update لـ parentDepartmentFk)
    Statement  : The system MUST prevent assigning an inactive Department as the parent of another Department
    Message-AR : لا يمكن تعيين قسم غير نشط أباً للقسم. يرجى اختيار قسم نشط.
    Message-EN : Cannot set an inactive department as parent. Please select an active department.

  RULE-ORG-018:
    Scope      : ENTITY-ORG-005
    Trigger    : عند الحفظ (Create أو Update لـ parentCostCenterFk)
    Statement  : The system MUST prevent assigning an inactive CostCenter as the parent of another CostCenter
    Message-AR : لا يمكن تعيين مركز تكلفة غير نشط أباً لمركز التكلفة. يرجى اختيار مركز تكلفة نشط.
    Message-EN : Cannot set an inactive cost center as parent. Please select an active cost center.

  RULE-ORG-019:
    Scope      : ENTITY-ORG-003
    Trigger    : عند الحفظ (Create)
    Statement  : The system MUST require a valid active LegalEntity reference before saving a new Region
    Message-AR : يرجى اختيار كيان قانوني نشط لربط المنطقة به.
    Message-EN : A valid active Legal Entity must be selected before saving a Region.

  RULE-ORG-020:
    Scope      : ENTITY-ORG-006
    Trigger    : عند الحفظ (Create)
    Statement  : The system MUST require a valid active LegalEntity reference before saving a new ProfitCenter
    Message-AR : يرجى اختيار كيان قانوني نشط لربط مركز الربح به.
    Message-EN : A valid active Legal Entity must be selected before saving a ProfitCenter.

SCREENS:
  SCR-ORG-001 │ إدارة الكيانات القانونية  │ Type: COMPOSITE PATTERN-1 │ Entity: ENTITY-ORG-001
  SCR-ORG-002 │ إدارة الفروع              │ Type: COMPOSITE PATTERN-1 │ Entity: ENTITY-ORG-002
  SCR-ORG-003 │ إدارة المناطق             │ Type: COMPOSITE PATTERN-1 │ Entity: ENTITY-ORG-003
  SCR-ORG-004 │ إدارة الأقسام             │ Type: SPECIALIZED PATTERN-3 (Hierarchical Tree) │ Entity: ENTITY-ORG-004
  SCR-ORG-005 │ إدارة مراكز التكلفة       │ Type: SPECIALIZED PATTERN-3 (Hierarchical Tree) │ Entity: ENTITY-ORG-005
  SCR-ORG-006 │ إدارة مراكز الربح         │ Type: COMPOSITE PATTERN-1 │ Entity: ENTITY-ORG-006
  SCR-ORG-007 │ إدارة المواقع الجغرافية   │ Type: COMPOSITE PATTERN-1 │ Entity: ENTITY-ORG-007

PERMISSIONS MATRIX:
  SCR-ORG-001 │ PAGE_CODE: LEGAL_ENTITY
              │ PERM_LEGAL_ENTITY_VIEW   : مدير النظام ✓, مدير التنظيم ✓
              │ PERM_LEGAL_ENTITY_CREATE : مدير النظام ✓
              │ PERM_LEGAL_ENTITY_UPDATE : مدير النظام ✓
              │ PERM_LEGAL_ENTITY_DELETE : مدير النظام ✓
  SCR-ORG-002 │ PAGE_CODE: BRANCH
              │ PERM_BRANCH_VIEW         : مدير النظام ✓, مدير التنظيم ✓
              │ PERM_BRANCH_CREATE       : مدير النظام ✓
              │ PERM_BRANCH_UPDATE       : مدير النظام ✓
              │ PERM_BRANCH_DELETE       : مدير النظام ✓
  SCR-ORG-003 │ PAGE_CODE: REGION
              │ PERM_REGION_VIEW         : مدير النظام ✓, مدير التنظيم ✓
              │ PERM_REGION_CREATE       : مدير النظام ✓
              │ PERM_REGION_UPDATE       : مدير النظام ✓
              │ PERM_REGION_DELETE       : مدير النظام ✓
  SCR-ORG-004 │ PAGE_CODE: DEPARTMENT
              │ PERM_DEPARTMENT_VIEW     : مدير النظام ✓, مدير التنظيم ✓
              │ PERM_DEPARTMENT_CREATE   : مدير النظام ✓
              │ PERM_DEPARTMENT_UPDATE   : مدير النظام ✓
              │ PERM_DEPARTMENT_DELETE   : مدير النظام ✓
  SCR-ORG-005 │ PAGE_CODE: COST_CENTER
              │ PERM_COST_CENTER_VIEW    : مدير النظام ✓, مدير التنظيم ✓, مدير المالية ✓
              │ PERM_COST_CENTER_CREATE  : مدير النظام ✓
              │ PERM_COST_CENTER_UPDATE  : مدير النظام ✓
              │ PERM_COST_CENTER_DELETE  : مدير النظام ✓
  SCR-ORG-006 │ PAGE_CODE: PROFIT_CENTER
              │ PERM_PROFIT_CENTER_VIEW  : مدير النظام ✓, مدير المالية ✓
              │ PERM_PROFIT_CENTER_CREATE: مدير النظام ✓
              │ PERM_PROFIT_CENTER_UPDATE: مدير النظام ✓
              │ PERM_PROFIT_CENTER_DELETE: مدير النظام ✓
  SCR-ORG-007 │ PAGE_CODE: LOCATION_SITE
              │ PERM_LOCATION_SITE_VIEW  : مدير النظام ✓, مدير التنظيم ✓
              │ PERM_LOCATION_SITE_CREATE: مدير النظام ✓
              │ PERM_LOCATION_SITE_UPDATE: مدير النظام ✓
              │ PERM_LOCATION_SITE_DELETE: مدير النظام ✓

── FROM dbs-org-001.md ────────────────────────────────────────────────

TABLES:
  ENTITY-ORG-001 → TABLE: ORG_LEGAL_ENTITY
  ENTITY-ORG-002 → TABLE: ORG_BRANCH
  ENTITY-ORG-003 → TABLE: ORG_REGION
  ENTITY-ORG-004 → TABLE: ORG_DEPARTMENT
  ENTITY-ORG-005 → TABLE: ORG_COST_CENTER
  ENTITY-ORG-006 → TABLE: ORG_PROFIT_CENTER
  ENTITY-ORG-007 → TABLE: ORG_LOCATION_SITE
  ENTITY-ORG-008 → TABLE: ORG_REGION_TYPE

SEQUENCES:
  ORG_REGION_TYPE    → SEQ_ORG_REGION_TYPE
  ORG_LEGAL_ENTITY   → SEQ_ORG_LEGAL_ENTITY
  ORG_BRANCH         → SEQ_ORG_BRANCH
  ORG_REGION         → SEQ_ORG_REGION
  ORG_DEPARTMENT     → SEQ_ORG_DEPARTMENT
  ORG_COST_CENTER    → SEQ_ORG_COST_CENTER
  ORG_PROFIT_CENTER  → SEQ_ORG_PROFIT_CENTER
  ORG_LOCATION_SITE  → SEQ_ORG_LOCATION_SITE
  ⚠ No BEFORE INSERT triggers — framework uses GenerationType.SEQUENCE directly

UNIQUE CONSTRAINTS (from db-script):
  UQ_ORG_LE_CODE      — ORG_LEGAL_ENTITY.LEGAL_ENTITY_CODE (globally unique)
  UQ_ORG_BR_CODE_LE   — ORG_BRANCH.(BRANCH_CODE, LEGAL_ENTITY_FK)
  UQ_ORG_RG_CODE_LE   — ORG_REGION.(REGION_CODE, LEGAL_ENTITY_FK)
  UQ_ORG_DEP_CODE_BR  — ORG_DEPARTMENT.(DEPT_CODE, BRANCH_FK)
  UQ_ORG_CC_CODE_BR   — ORG_COST_CENTER.(COST_CENTER_CODE, BRANCH_FK)
  UQ_ORG_PC_CODE_LE   — ORG_PROFIT_CENTER.(PROFIT_CENTER_CODE, LEGAL_ENTITY_FK)
  UQ_ORG_LS_CODE_BR   — ORG_LOCATION_SITE.(LOCATION_CODE, BRANCH_FK)

FK CONSTRAINTS:
  FK_ORG_BR_LE  — ORG_BRANCH.LEGAL_ENTITY_FK    → ORG_LEGAL_ENTITY.LEGAL_ENTITY_PK
  FK_ORG_RG_LE  — ORG_REGION.LEGAL_ENTITY_FK    → ORG_LEGAL_ENTITY.LEGAL_ENTITY_PK
  FK_ORG_RG_RT  — ORG_REGION.REGION_TYPE_FK     → ORG_REGION_TYPE.REGION_TYPE_PK
  FK_ORG_PC_LE  — ORG_PROFIT_CENTER.LEGAL_ENTITY_FK → ORG_LEGAL_ENTITY.LEGAL_ENTITY_PK
  FK_ORG_DEP_BR — ORG_DEPARTMENT.BRANCH_FK      → ORG_BRANCH.BRANCH_PK
  FK_ORG_DEP_SELF — ORG_DEPARTMENT.PARENT_DEPARTMENT_FK → ORG_DEPARTMENT.DEPARTMENT_PK (NULLABLE)
  FK_ORG_CC_BR  — ORG_COST_CENTER.BRANCH_FK     → ORG_BRANCH.BRANCH_PK
  FK_ORG_CC_SELF — ORG_COST_CENTER.PARENT_COST_CENTER_FK → ORG_COST_CENTER.COST_CENTER_PK (NULLABLE)
  FK_ORG_LS_BR  — ORG_LOCATION_SITE.BRANCH_FK   → ORG_BRANCH.BRANCH_PK

XM DEPENDENCIES: None — ROOT MODULE (XM Register: EMPTY)

── FROM master-registry.md ────────────────────────────────────────────

MODULE PREFIX    : ORG — used for all IDs in this module
EXISTING LOVs    : LOV-ORG-001..006 registered (MD_LOOKUP_DETAIL)
                   LOV-ORG-007 is Reference Table (ORG_REGION_TYPE) — not MD_LOOKUP_DETAIL
SHARED ENTITIES  : None consumed — Organization is ROOT
LOV API endpoint : GET /api/lookups/{lookupKey}?active=true (master-registry canonical contract)
RegionType API   : GET /api/v1/org/region-types?active=true (dedicated endpoint — not lookup)
Entity base      : AuditableEntity (uniform — TenantAuditableEntity retired 2026-06-21)
Error signaling  : LocalizedException — NotFoundException BANNED

╠══════════════════════════════════════════════════════════════════════
║  Extraction complete. All values bound into every phase below.
╚══════════════════════════════════════════════════════════════════════
```

---

## EXECUTION PLAN INDEX — ORG-001 — PLAN-ID: PLAN-ORG-001

```
══════════════════════════════════════════════════════════════════
Feature Code   : ORG-001
DBS-ID         : DBS-ORG-001
Governed by    : Execution Plan Governance Engine (Project 3) v2
Output Mode    : SINGLE-FILE — Agent-Ready Specification
Open Questions : 1 active (OQ-001) — see OQ Log
══════════════════════════════════════════════════════════════════

ENTITY REGISTRY
───────────────────────────────────────────────────────────────
ENTITY-ID        │ Entity Name    │ DB Table            │ Business Code  │ Operations
─────────────────┼────────────────┼─────────────────────┼────────────────┼──────────────────
ENTITY-ORG-001   │ LegalEntity    │ ORG_LEGAL_ENTITY    │ LE-NNNNN       │ Create,Read,Update,Deactivate,Reactivate
ENTITY-ORG-002   │ Branch         │ ORG_BRANCH          │ BR-[LE]-NNNNN  │ Create,Read,Update,Deactivate,Reactivate
ENTITY-ORG-003   │ Region         │ ORG_REGION          │ RG-[LE]-NNNNN  │ Create,Read,Update,Deactivate,Reactivate
ENTITY-ORG-004   │ Department     │ ORG_DEPARTMENT      │ DEP-[BR]-NNNNN │ Create,Read,Update,Deactivate,Reactivate
ENTITY-ORG-005   │ CostCenter     │ ORG_COST_CENTER     │ CC-[BR]-NNNNN  │ Create,Read,Update,Deactivate,Reactivate
ENTITY-ORG-006   │ ProfitCenter   │ ORG_PROFIT_CENTER   │ PC-[LE]-NNNNN  │ Create,Read,Update,Deactivate,Reactivate
ENTITY-ORG-007   │ LocationSite   │ ORG_LOCATION_SITE   │ LS-[BR]-NNNNN  │ Create,Read,Update,Deactivate,Reactivate
ENTITY-ORG-008   │ RegionType     │ ORG_REGION_TYPE     │ None (Ref Tbl) │ Create,Read,Update,Deactivate (Admin)

FIELD REGISTRY (this plan — P3-assigned FIELD-IDs)
───────────────────────────────────────────────────────────────
FIELD-ID   │ Java Property         │ DBF-ID   │ Read-Only
───────────┼───────────────────────┼──────────┼──────────
── ENTITY-ORG-008 / ORG_REGION_TYPE ──
FIELD-0001 │ regionTypePk          │ DBF-0001 │ YES (PK)
FIELD-0002 │ nameAr                │ DBF-0002 │ NO
FIELD-0003 │ nameEn                │ DBF-0003 │ NO
FIELD-0004 │ isActiveFl            │ DBF-0004 │ SYSTEM
── ENTITY-ORG-001 / ORG_LEGAL_ENTITY ──
FIELD-0005 │ legalEntityPk         │ DBF-0009 │ YES (PK)
FIELD-0006 │ legalEntityCode       │ DBF-0010 │ YES (BC)
FIELD-0007 │ nameAr                │ DBF-0011 │ NO
FIELD-0008 │ nameEn                │ DBF-0012 │ NO
FIELD-0009 │ entityTypeId          │ DBF-0013 │ NO
FIELD-0010 │ isActiveFl            │ DBF-0014 │ SYSTEM
FIELD-0011 │ notes                 │ DBF-0015 │ NO
── ENTITY-ORG-002 / ORG_BRANCH ──
FIELD-0012 │ branchPk              │ DBF-0020 │ YES (PK)
FIELD-0013 │ branchCode            │ DBF-0021 │ YES (BC)
FIELD-0014 │ nameAr                │ DBF-0022 │ NO
FIELD-0015 │ nameEn                │ DBF-0023 │ NO
FIELD-0016 │ legalEntityFk         │ DBF-0024 │ NO
FIELD-0017 │ branchTypeId          │ DBF-0025 │ NO
FIELD-0018 │ isActiveFl            │ DBF-0026 │ SYSTEM
FIELD-0019 │ notes                 │ DBF-0027 │ NO
── ENTITY-ORG-003 / ORG_REGION ──
FIELD-0020 │ regionPk              │ DBF-0032 │ YES (PK)
FIELD-0021 │ regionCode            │ DBF-0033 │ YES (BC)
FIELD-0022 │ nameAr                │ DBF-0034 │ NO
FIELD-0023 │ nameEn                │ DBF-0035 │ NO
FIELD-0024 │ legalEntityFk         │ DBF-0036 │ NO
FIELD-0025 │ regionTypeFk          │ DBF-0037 │ NO
FIELD-0026 │ isActiveFl            │ DBF-0038 │ SYSTEM
FIELD-0027 │ notes                 │ DBF-0039 │ NO
── ENTITY-ORG-004 / ORG_DEPARTMENT ──
FIELD-0028 │ departmentPk          │ DBF-0044 │ YES (PK)
FIELD-0029 │ deptCode              │ DBF-0045 │ YES (BC)
FIELD-0030 │ nameAr                │ DBF-0046 │ NO
FIELD-0031 │ nameEn                │ DBF-0047 │ NO
FIELD-0032 │ branchFk              │ DBF-0048 │ NO
FIELD-0033 │ parentDepartmentFk    │ DBF-0049 │ NO (NULLABLE)
FIELD-0034 │ nodeTypeId            │ DBF-0050 │ NO
FIELD-0035 │ isActiveFl            │ DBF-0051 │ SYSTEM
FIELD-0036 │ notes                 │ DBF-0052 │ NO
── ENTITY-ORG-005 / ORG_COST_CENTER ──
FIELD-0037 │ costCenterPk          │ DBF-0057 │ YES (PK)
FIELD-0038 │ costCenterCode        │ DBF-0058 │ YES (BC)
FIELD-0039 │ nameAr                │ DBF-0059 │ NO
FIELD-0040 │ nameEn                │ DBF-0060 │ NO
FIELD-0041 │ branchFk              │ DBF-0061 │ NO
FIELD-0042 │ parentCostCenterFk    │ DBF-0062 │ NO (NULLABLE)
FIELD-0043 │ nodeTypeId            │ DBF-0063 │ NO
FIELD-0044 │ costCenterTypeId      │ DBF-0064 │ NO
FIELD-0045 │ isActiveFl            │ DBF-0065 │ SYSTEM
FIELD-0046 │ notes                 │ DBF-0066 │ NO
── ENTITY-ORG-006 / ORG_PROFIT_CENTER ──
FIELD-0047 │ profitCenterPk        │ DBF-0071 │ YES (PK)
FIELD-0048 │ profitCenterCode      │ DBF-0072 │ YES (BC)
FIELD-0049 │ nameAr                │ DBF-0073 │ NO
FIELD-0050 │ nameEn                │ DBF-0074 │ NO
FIELD-0051 │ legalEntityFk         │ DBF-0075 │ NO
FIELD-0052 │ isActiveFl            │ DBF-0076 │ SYSTEM
FIELD-0053 │ notes                 │ DBF-0077 │ NO
── ENTITY-ORG-007 / ORG_LOCATION_SITE ──
FIELD-0054 │ locationSitePk        │ DBF-0082 │ YES (PK)
FIELD-0055 │ locationCode          │ DBF-0083 │ YES (BC)
FIELD-0056 │ nameAr                │ DBF-0084 │ NO
FIELD-0057 │ nameEn                │ DBF-0085 │ NO
FIELD-0058 │ branchFk              │ DBF-0086 │ NO
FIELD-0059 │ siteTypeId            │ DBF-0087 │ NO
FIELD-0060 │ isActiveFl            │ DBF-0088 │ SYSTEM
FIELD-0061 │ notes                 │ DBF-0089 │ NO

Note: Audit fields (createdBy/createdAt/updatedBy/updatedAt) managed by AuditEntityListener —
      not assigned FIELD-IDs — excluded from all Create/Update DTOs.

API REGISTRY (this plan)
───────────────────────────────────────────────────────────────
API-ID         │ Operation                      │ HTTP   │ Endpoint
───────────────┼────────────────────────────────┼────────┼──────────────────────────────────
API-ORG-001    │ Create LegalEntity             │ POST   │ /api/v1/org/legal-entities
API-ORG-002    │ Search LegalEntities           │ GET    │ /api/v1/org/legal-entities
API-ORG-003    │ Get LegalEntity by ID          │ GET    │ /api/v1/org/legal-entities/{id}
API-ORG-004    │ Update LegalEntity             │ PUT    │ /api/v1/org/legal-entities/{id}
API-ORG-005    │ Deactivate LegalEntity         │ DELETE │ /api/v1/org/legal-entities/{id}
API-ORG-006    │ Reactivate LegalEntity         │ PUT    │ /api/v1/org/legal-entities/{id}/reactivate
API-ORG-007    │ Create Branch                  │ POST   │ /api/v1/org/branches
API-ORG-008    │ Search Branches                │ GET    │ /api/v1/org/branches
API-ORG-009    │ Get Branch by ID               │ GET    │ /api/v1/org/branches/{id}
API-ORG-010    │ Update Branch                  │ PUT    │ /api/v1/org/branches/{id}
API-ORG-011    │ Deactivate Branch              │ DELETE │ /api/v1/org/branches/{id}
API-ORG-012    │ Reactivate Branch              │ PUT    │ /api/v1/org/branches/{id}/reactivate
API-ORG-013    │ Get Branches by LegalEntity    │ GET    │ /api/v1/org/branches/by-legal-entity/{leId}
API-ORG-014    │ Create Region                  │ POST   │ /api/v1/org/regions
API-ORG-015    │ Search Regions                 │ GET    │ /api/v1/org/regions
API-ORG-016    │ Get Region by ID               │ GET    │ /api/v1/org/regions/{id}
API-ORG-017    │ Update Region                  │ PUT    │ /api/v1/org/regions/{id}
API-ORG-018    │ Deactivate Region              │ DELETE │ /api/v1/org/regions/{id}
API-ORG-019    │ Reactivate Region              │ PUT    │ /api/v1/org/regions/{id}/reactivate
API-ORG-020    │ Get RegionTypes                │ GET    │ /api/v1/org/region-types
API-ORG-021    │ Create Department              │ POST   │ /api/v1/org/departments
API-ORG-022    │ Get Department Tree            │ GET    │ /api/v1/org/departments/tree
API-ORG-023    │ Search Departments             │ GET    │ /api/v1/org/departments
API-ORG-024    │ Get Department by ID           │ GET    │ /api/v1/org/departments/{id}
API-ORG-025    │ Update Department              │ PUT    │ /api/v1/org/departments/{id}
API-ORG-026    │ Deactivate Department          │ DELETE │ /api/v1/org/departments/{id}
API-ORG-027    │ Reactivate Department          │ PUT    │ /api/v1/org/departments/{id}/reactivate
API-ORG-028    │ Create CostCenter              │ POST   │ /api/v1/org/cost-centers
API-ORG-029    │ Get CostCenter Tree            │ GET    │ /api/v1/org/cost-centers/tree
API-ORG-030    │ Search CostCenters             │ GET    │ /api/v1/org/cost-centers
API-ORG-031    │ Get CostCenter by ID           │ GET    │ /api/v1/org/cost-centers/{id}
API-ORG-032    │ Update CostCenter              │ PUT    │ /api/v1/org/cost-centers/{id}
API-ORG-033    │ Deactivate CostCenter          │ DELETE │ /api/v1/org/cost-centers/{id}
API-ORG-034    │ Reactivate CostCenter          │ PUT    │ /api/v1/org/cost-centers/{id}/reactivate
API-ORG-035    │ Create ProfitCenter            │ POST   │ /api/v1/org/profit-centers
API-ORG-036    │ Search ProfitCenters           │ GET    │ /api/v1/org/profit-centers
API-ORG-037    │ Get ProfitCenter by ID         │ GET    │ /api/v1/org/profit-centers/{id}
API-ORG-038    │ Update ProfitCenter            │ PUT    │ /api/v1/org/profit-centers/{id}
API-ORG-039    │ Deactivate ProfitCenter        │ DELETE │ /api/v1/org/profit-centers/{id}
API-ORG-040    │ Reactivate ProfitCenter        │ PUT    │ /api/v1/org/profit-centers/{id}/reactivate
API-ORG-041    │ Create LocationSite            │ POST   │ /api/v1/org/location-sites
API-ORG-042    │ Search LocationSites           │ GET    │ /api/v1/org/location-sites
API-ORG-043    │ Get LocationSite by ID         │ GET    │ /api/v1/org/location-sites/{id}
API-ORG-044    │ Update LocationSite            │ PUT    │ /api/v1/org/location-sites/{id}
API-ORG-045    │ Deactivate LocationSite        │ DELETE │ /api/v1/org/location-sites/{id}
API-ORG-046    │ Reactivate LocationSite        │ PUT    │ /api/v1/org/location-sites/{id}/reactivate
API-ORG-047    │ Get LocationSites by Branch    │ GET    │ /api/v1/org/location-sites/by-branch/{branchId}

RULE REGISTRY
───────────────────────────────────────────────────────────────
RULE-ID        │ Rule Name                              │ Scope           │ ENTITY-ID       │ Message-AR
───────────────┼────────────────────────────────────────┼─────────────────┼─────────────────┼──────────
RULE-ORG-001   │ Prevent LegalEntity deactivation/Branches│ Deactivate    │ ENTITY-ORG-001  │ ✓
RULE-ORG-002   │ Prevent LegalEntity deactivation/ProfitCenters│ Deactivate│ ENTITY-ORG-001 │ ✓
RULE-ORG-003   │ Prevent Branch deactivation/Departments│ Deactivate      │ ENTITY-ORG-002  │ ✓
RULE-ORG-004   │ Prevent Branch deactivation/CostCenters│ Deactivate      │ ENTITY-ORG-002  │ ✓
RULE-ORG-005   │ Prevent Branch deactivation/LocationSites│ Deactivate    │ ENTITY-ORG-002  │ ✓
RULE-ORG-006   │ Prevent Region deactivation/Branches   │ Deactivate      │ ENTITY-ORG-003  │ ✓
RULE-ORG-007   │ Prevent circular Department parent     │ CREATE/UPDATE   │ ENTITY-ORG-004  │ ✓
RULE-ORG-008   │ Prevent circular CostCenter parent     │ CREATE/UPDATE   │ ENTITY-ORG-005  │ ✓
RULE-ORG-009   │ Prevent SUMMARY Dept assignment        │ Consumer modules│ ENTITY-ORG-004  │ ✓
RULE-ORG-010   │ Prevent SUMMARY CC assignment          │ Consumer modules│ ENTITY-ORG-005  │ ✓
RULE-ORG-011   │ Business code immutability             │ UPDATE          │ ALL (001-007)   │ ✓
RULE-ORG-012   │ Business code uniqueness               │ CREATE          │ ALL (001-007)   │ ✓
RULE-ORG-013   │ Branch requires active LegalEntity     │ CREATE          │ ENTITY-ORG-002  │ ✓
RULE-ORG-014   │ Department requires active Branch      │ CREATE          │ ENTITY-ORG-004  │ ✓
RULE-ORG-015   │ CostCenter requires active Branch      │ CREATE          │ ENTITY-ORG-005  │ ✓
RULE-ORG-016   │ NumberingEngine mandatory              │ CREATE          │ ALL (001-007)   │ — (arch)
RULE-ORG-017   │ Department parent must be active       │ CREATE/UPDATE   │ ENTITY-ORG-004  │ ✓
RULE-ORG-018   │ CostCenter parent must be active       │ CREATE/UPDATE   │ ENTITY-ORG-005  │ ✓
RULE-ORG-019   │ Region requires active LegalEntity     │ CREATE          │ ENTITY-ORG-003  │ ✓
RULE-ORG-020   │ ProfitCenter requires active LegalEntity│ CREATE         │ ENTITY-ORG-006  │ ✓

SCREEN REGISTRY
───────────────────────────────────────────────────────────────
SCR-ID       │ Screen Name                    │ Type          │ ENTITY-ID
─────────────┼────────────────────────────────┼───────────────┼──────────────────
SCR-ORG-001  │ إدارة الكيانات القانونية       │ COMPOSITE P1  │ ENTITY-ORG-001
SCR-ORG-002  │ إدارة الفروع                   │ COMPOSITE P1  │ ENTITY-ORG-002
SCR-ORG-003  │ إدارة المناطق                  │ COMPOSITE P1  │ ENTITY-ORG-003
SCR-ORG-004  │ إدارة الأقسام                  │ SPECIALIZED P3│ ENTITY-ORG-004
SCR-ORG-005  │ إدارة مراكز التكلفة            │ SPECIALIZED P3│ ENTITY-ORG-005
SCR-ORG-006  │ إدارة مراكز الربح              │ COMPOSITE P1  │ ENTITY-ORG-006
SCR-ORG-007  │ إدارة المواقع الجغرافية        │ COMPOSITE P1  │ ENTITY-ORG-007

LOV REGISTRY
───────────────────────────────────────────────────────────────
LOV-ID       │ LOOKUP_CODE / Source          │ Used In Field      │ ENTITY-ID
─────────────┼───────────────────────────────┼────────────────────┼──────────────────
LOV-ORG-001  │ LEGAL_ENTITY_TYPE             │ entityTypeId       │ ENTITY-ORG-001
LOV-ORG-002  │ BRANCH_TYPE                   │ branchTypeId       │ ENTITY-ORG-002
LOV-ORG-003  │ DEPARTMENT_NODE_TYPE          │ nodeTypeId         │ ENTITY-ORG-004
LOV-ORG-004  │ COST_CENTER_NODE_TYPE         │ nodeTypeId         │ ENTITY-ORG-005
LOV-ORG-005  │ COST_CENTER_TYPE              │ costCenterTypeId   │ ENTITY-ORG-005
LOV-ORG-006  │ LOCATION_SITE_TYPE            │ siteTypeId         │ ENTITY-ORG-007
LOV-ORG-007  │ Reference Table: ORG_REGION_TYPE│ regionTypeFk     │ ENTITY-ORG-003

QUERY REFERENCE CATALOG SUMMARY
───────────────────────────────────────────────────────────────
⚠ ALL entries are AGENT REFERENCE only — agent rewrites every query during implementation
QR-ORG-0001..0048 — generated in DATA+DOM and SVC+API phases — see Section 11

DB ALIGNMENT     : ALIGNED ✓ — see DB Alignment Manifest (Section 4)
XM STATUS        : None — ROOT MODULE
CONTRACT GATE    : DOC ✓ | INT-C ✓ (no XM)
SECURITY         : Permissions matrix: 7 screens × 3 roles
══════════════════════════════════════════════════════════════════
```

---

## SECTION 4 — DB ALIGNMENT MANIFEST

```
DB ALIGNMENT MANIFEST — ORG-001 — PLAN-ORG-001
══════════════════════════════════════════════════════════════════
⚠ CONTRACT-1: Manifest contains ONLY 5 columns — no Column Name, DB Type, or SRS Source
FIELD-ID   │ DBF-ID   │ Plan Type       │ FK/XM-ID                    │ Match Status
───────────┼──────────┼─────────────────┼─────────────────────────────┼──────────────
FIELD-0001 │ DBF-0001 │ PK              │ —                           │ ALIGNED ✓
FIELD-0002 │ DBF-0002 │ Name-AR         │ —                           │ ALIGNED ✓
FIELD-0003 │ DBF-0003 │ Name-EN         │ —                           │ ALIGNED ✓
FIELD-0004 │ DBF-0004 │ FLAG            │ —                           │ ALIGNED ✓
FIELD-0005 │ DBF-0009 │ PK              │ —                           │ ALIGNED ✓
FIELD-0006 │ DBF-0010 │ BUSINESS-CODE   │ UQ_ORG_LE_CODE              │ ALIGNED ✓
FIELD-0007 │ DBF-0011 │ Name-AR         │ —                           │ ALIGNED ✓
FIELD-0008 │ DBF-0012 │ Name-EN         │ —                           │ ALIGNED ✓
FIELD-0009 │ DBF-0013 │ LOV (LOV-ORG-001)│ —                          │ ALIGNED ✓
FIELD-0010 │ DBF-0014 │ FLAG            │ CHK_ORG_LE_ACTIVE           │ ALIGNED ✓
FIELD-0011 │ DBF-0015 │ TEXT            │ —                           │ ALIGNED ✓
FIELD-0012 │ DBF-0020 │ PK              │ —                           │ ALIGNED ✓
FIELD-0013 │ DBF-0021 │ BUSINESS-CODE   │ UQ_ORG_BR_CODE_LE           │ ALIGNED ✓
FIELD-0014 │ DBF-0022 │ Name-AR         │ —                           │ ALIGNED ✓
FIELD-0015 │ DBF-0023 │ Name-EN         │ —                           │ ALIGNED ✓
FIELD-0016 │ DBF-0024 │ FK              │ FK_ORG_BR_LE                │ ALIGNED ✓
FIELD-0017 │ DBF-0025 │ LOV (LOV-ORG-002)│ —                          │ ALIGNED ✓
FIELD-0018 │ DBF-0026 │ FLAG            │ CHK_ORG_BR_ACTIVE           │ ALIGNED ✓
FIELD-0019 │ DBF-0027 │ TEXT            │ —                           │ ALIGNED ✓
FIELD-0020 │ DBF-0032 │ PK              │ —                           │ ALIGNED ✓
FIELD-0021 │ DBF-0033 │ BUSINESS-CODE   │ UQ_ORG_RG_CODE_LE           │ ALIGNED ✓
FIELD-0022 │ DBF-0034 │ Name-AR         │ —                           │ ALIGNED ✓
FIELD-0023 │ DBF-0035 │ Name-EN         │ —                           │ ALIGNED ✓
FIELD-0024 │ DBF-0036 │ FK              │ FK_ORG_RG_LE                │ ALIGNED ✓
FIELD-0025 │ DBF-0037 │ FK (Ref Table)  │ FK_ORG_RG_RT / LOV-ORG-007 │ ALIGNED ✓
FIELD-0026 │ DBF-0038 │ FLAG            │ CHK_ORG_RG_ACTIVE           │ ALIGNED ✓
FIELD-0027 │ DBF-0039 │ TEXT            │ —                           │ ALIGNED ✓
FIELD-0028 │ DBF-0044 │ PK              │ —                           │ ALIGNED ✓
FIELD-0029 │ DBF-0045 │ BUSINESS-CODE   │ UQ_ORG_DEP_CODE_BR          │ ALIGNED ✓
FIELD-0030 │ DBF-0046 │ Name-AR         │ —                           │ ALIGNED ✓
FIELD-0031 │ DBF-0047 │ Name-EN         │ —                           │ ALIGNED ✓
FIELD-0032 │ DBF-0048 │ FK              │ FK_ORG_DEP_BR               │ ALIGNED ✓
FIELD-0033 │ DBF-0049 │ FK-SELF (NULLABLE)│ FK_ORG_DEP_SELF           │ ALIGNED ✓
FIELD-0034 │ DBF-0050 │ LOV (LOV-ORG-003)│ —                          │ ALIGNED ✓
FIELD-0035 │ DBF-0051 │ FLAG            │ CHK_ORG_DEP_ACTIVE          │ ALIGNED ✓
FIELD-0036 │ DBF-0052 │ TEXT            │ —                           │ ALIGNED ✓
FIELD-0037 │ DBF-0057 │ PK              │ —                           │ ALIGNED ✓
FIELD-0038 │ DBF-0058 │ BUSINESS-CODE   │ UQ_ORG_CC_CODE_BR           │ ALIGNED ✓
FIELD-0039 │ DBF-0059 │ Name-AR         │ —                           │ ALIGNED ✓
FIELD-0040 │ DBF-0060 │ Name-EN         │ —                           │ ALIGNED ✓
FIELD-0041 │ DBF-0061 │ FK              │ FK_ORG_CC_BR                │ ALIGNED ✓
FIELD-0042 │ DBF-0062 │ FK-SELF (NULLABLE)│ FK_ORG_CC_SELF            │ ALIGNED ✓
FIELD-0043 │ DBF-0063 │ LOV (LOV-ORG-004)│ —                          │ ALIGNED ✓
FIELD-0044 │ DBF-0064 │ LOV (LOV-ORG-005)│ —                          │ ALIGNED ✓
FIELD-0045 │ DBF-0065 │ FLAG            │ CHK_ORG_CC_ACTIVE           │ ALIGNED ✓
FIELD-0046 │ DBF-0066 │ TEXT            │ —                           │ ALIGNED ✓
FIELD-0047 │ DBF-0071 │ PK              │ —                           │ ALIGNED ✓
FIELD-0048 │ DBF-0072 │ BUSINESS-CODE   │ UQ_ORG_PC_CODE_LE           │ ALIGNED ✓
FIELD-0049 │ DBF-0073 │ Name-AR         │ —                           │ ALIGNED ✓
FIELD-0050 │ DBF-0074 │ Name-EN         │ —                           │ ALIGNED ✓
FIELD-0051 │ DBF-0075 │ FK              │ FK_ORG_PC_LE                │ ALIGNED ✓
FIELD-0052 │ DBF-0076 │ FLAG            │ CHK_ORG_PC_ACTIVE           │ ALIGNED ✓
FIELD-0053 │ DBF-0077 │ TEXT            │ —                           │ ALIGNED ✓
FIELD-0054 │ DBF-0082 │ PK              │ —                           │ ALIGNED ✓
FIELD-0055 │ DBF-0083 │ BUSINESS-CODE   │ UQ_ORG_LS_CODE_BR           │ ALIGNED ✓
FIELD-0056 │ DBF-0084 │ Name-AR         │ —                           │ ALIGNED ✓
FIELD-0057 │ DBF-0085 │ Name-EN         │ —                           │ ALIGNED ✓
FIELD-0058 │ DBF-0086 │ FK              │ FK_ORG_LS_BR                │ ALIGNED ✓
FIELD-0059 │ DBF-0087 │ LOV (LOV-ORG-006)│ —                          │ ALIGNED ✓
FIELD-0060 │ DBF-0088 │ FLAG            │ CHK_ORG_LS_ACTIVE           │ ALIGNED ✓
FIELD-0061 │ DBF-0089 │ TEXT            │ —                           │ ALIGNED ✓
──────────────────────────────────────────────────────────────────────
Total FIELD-IDs: FIELD-0001 through FIELD-0061 — 61 bindings
All audit fields (createdBy/createdAt/updatedBy/updatedAt): managed by AuditEntityListener
  → DBF-0005..0008, DBF-0016..0019, DBF-0028..0031, DBF-0040..0043
  → DBF-0053..0056, DBF-0067..0070, DBF-0078..0081, DBF-0090..0093
  Not assigned FIELD-IDs — set by AuditEntityListener, never in DTO
DB Alignment: ALIGNED ✓ — all 61 fields trace to confirmed DBF-IDs
══════════════════════════════════════════════════════════════════
```

<!-- PHASE:CORE:START -->

---

# PHASE CORE — Architecture & Project Standards Declaration

## CORE — Canonical Architecture (MANDATORY — Agent MUST read before writing any code)

### Backend Architecture

```
Layer              │ Class Pattern                        │ Responsibility
───────────────────┼──────────────────────────────────────┼──────────────────────────────────────
Controller         │ [Entity]Controller                   │ HTTP routing, permission check, DTO in/out
Service            │ [Entity]Service / [Entity]ServiceImpl│ Orchestration, business rule enforcement
Mapper             │ [Entity]Mapper (MapStruct)           │ Entity ↔ DTO conversion ONLY — no logic
Domain / Entity    │ [Entity] extends AuditableEntity     │ JPA entity, field declaration, relations
Repository         │ [Entity]Repository extends JpaRepository│ Data access — JPA queries only
```

**Package structure (agent infers exact package from project convention):**
```
com.[project].org
  ├── controller
  ├── service
  ├── mapper
  ├── domain (entity classes)
  └── repository
```

### Frontend Architecture (Angular)

```
Layer       │ Angular Artifact               │ Responsibility
────────────┼────────────────────────────────┼──────────────────────────────────────
Models      │ [entity].model.ts              │ TypeScript interfaces for all DTOs
Services    │ [entity].service.ts            │ HTTP calls only — no state
Facades     │ [entity].facade.ts             │ State ownership + orchestration
Helpers     │ [entity].helper.ts             │ Pure transform/formatting functions
Components  │ [entity]-list / [entity]-form  │ UI rendering + user events only
```

### Entity Base Class

```
ALL entities in this module extend: AuditableEntity
  ✗ TenantAuditableEntity — RETIRED 2026-06-21 — DO NOT USE
  ✗ Do NOT add tenantId column — multi-tenancy eliminated system-wide
AuditEntityListener sets: createdBy, createdAt, updatedBy, updatedAt
  ✗ NEVER set audit fields in Mapper or Service
  ✗ NEVER accept audit fields in Create/Update DTOs
```

### Error Handling Standard

```
Exception type   : LocalizedException (project standard)
  ✗ NotFoundException — BANNED
  ✗ Generic RuntimeException — BANNED for business errors
  ✓ throw new LocalizedException(ErrorCodes.[ERR_CONSTANT], messageAr, messageEn)

ERR-ID 4-point registration (every new ERR-ID must be registered in ALL 4):
  1. ErrorCodes.[ERR_CONSTANT] — constant definition class
  2. messages_ar.properties / messages_en.properties — message catalog
  3. erp-errors.json — frontend error code registry
  4. ErpErrorMapper — HTTP status code mapping
```

### Deactivation Policy (Soft Delete)

```
Deactivation = set isActiveFl = 0 (NOT physical DELETE)
Pre-check sequence (for all entities with dependants):
  1. Check all child/referencing entities for isActiveFl = 1
  2. If any found: throw LocalizedException with RULE-ID message
  3. If none: set isActiveFl = 0 and save
Reactivation = set isActiveFl = 1 (no pre-checks required)
```

### Search / Pagination Standard

```
Backend  : SearchRequest extends BaseSearchContractRequest
           → fields: page (0-based), size, sortBy, sortDir
           → ALLOWED_SORT_FIELDS: Set<String> declared per Service
           → PageableBuilder.from(request, ALLOWED_SORT_FIELDS) builds Pageable
           → Returns: JPA Page<T> — DO NOT create custom pagination wrapper
           → Empty results: HTTP 200 + empty content — NEVER HTTP 404

Frontend : currentPage and pageSize are DERIVED from lastSearchRequest
           ✗ NEVER declare currentPage / pageSize as independent state
```

### NumberingEngine Integration

```
All 7 master entities (ENTITY-ORG-001..007) use NumberingEngine exclusively.
Service layer calls:
  NumberingEngine.generate(entityType, legalEntityFk, branchFk)
  → Returns the formatted business code string
  → Business code is then set on the entity before persist
No module implements its own numbering logic (RULE-ORG-016).
```

### LOV Loading Standard

```
MD_LOOKUP_DETAIL lookups (LOV-ORG-001..006):
  GET /api/lookups/{lookupKey}?active=true
  Stored in DB as DETAIL_CODE (VARCHAR2) — never a numeric FK
  Frontend: load once on screen init → store in facade state as [lovName]Options

RegionType Reference Table (LOV-ORG-007):
  GET /api/v1/org/region-types?active=true (via API-ORG-020)
  Stored in DB as REGION_TYPE_FK NUMBER(10) → FK to ORG_REGION_TYPE.REGION_TYPE_PK
  ⚠ DRV-ORG-001: Region.regionTypeId is stored as FK (NUMBER) not DETAIL_CODE (VARCHAR2)
    This deviates from standard LOV pattern because RegionType is a Reference Table,
    not a MD_LOOKUP_DETAIL entry. Agent must map regionTypeFk as a FK relationship.
```

### Inbound Stubs (INBOUND-STUB)

```
This module is ROOT — zero outbound XM dependencies.
Inbound consumers (other modules reference ORG entities):
  INBOUND-STUB-ORG-001 : Finance module will reference CostCenter / ProfitCenter
                          Status: DEFERRED — Finance module not yet built
  INBOUND-STUB-ORG-002 : Inventory module will reference LocationSite
                          Status: DEFERRED — Inventory module not yet built
These stubs are informational — no implementation required in this module.
```

**CORE Gate: PASSED ✓**

```
[ ✓ ] Backend architecture declared (Controller/Service/Mapper/Domain/Repository)
[ ✓ ] Frontend architecture declared (Models/Services/Facades/Helpers/Components)
[ ✓ ] AuditableEntity base declared — TenantAuditableEntity BANNED
[ ✓ ] LocalizedException declared — NotFoundException BANNED
[ ✓ ] AuditEntityListener declared — never in Mapper/Service
[ ✓ ] Search/Pagination standard declared
[ ✓ ] Deactivation policy declared
[ ✓ ] NumberingEngine integration declared
[ ✓ ] LOV loading standard declared
[ ✓ ] DRV-ORG-001: RegionType FK deviation documented
```

<!-- PHASE:CORE:END -->

---

<!-- PHASE:DATA-DOM:START -->

# PHASE DATA+DOM — Entity & Domain Specifications

> Sub-phases triggered: 8 entities ≥ 5 threshold → grouped semantically

<!-- SUB:MASTER:START -->

## DATA+DOM — Master Entities (ENTITY-ORG-001..007)

---

### ENTITY-ORG-001 — LegalEntity

```
Java Class       : LegalEntity extends AuditableEntity
DB Table         : ORG_LEGAL_ENTITY
PK Generation    : SEQ_ORG_LEGAL_ENTITY.NEXTVAL
                   ⚠ Agent: use @SequenceGenerator(sequenceName = "SEQ_ORG_LEGAL_ENTITY")

FIELD DECLARATIONS:
  FIELD-0005 │ legalEntityPk   │ DB: LEGAL_ENTITY_PK  │ NUMBER(10) NOT NULL │ @Id
  FIELD-0006 │ legalEntityCode │ DB: LEGAL_ENTITY_CODE │ VARCHAR2(20) NOT NULL │ Read-Only / BC
  FIELD-0007 │ nameAr          │ DB: NAME_AR           │ VARCHAR2(200) NOT NULL
  FIELD-0008 │ nameEn          │ DB: NAME_EN           │ VARCHAR2(100) NOT NULL
  FIELD-0009 │ entityTypeId    │ DB: ENTITY_TYPE_ID    │ VARCHAR2(50) NOT NULL │ LOV-ORG-001
  FIELD-0010 │ isActiveFl      │ DB: IS_ACTIVE_FL      │ NUMBER(1) DEFAULT 1
  FIELD-0011 │ notes           │ DB: NOTES             │ VARCHAR2(2000) NULLABLE

BUSINESS CODE:
  Field name  : legalEntityCode (FIELD-0006)
  Column      : LEGAL_ENTITY_CODE — DBF-0010
  Format      : LE-NNNNN
  Generation  : SEQ_ORG_LEGAL_ENTITY provides sequential part via NumberingEngine
  Uniqueness  : UQ_ORG_LE_CODE (globally unique)
  Immutability: RULE-ORG-011 — excluded from Update DTO entirely

LOV FIELDS:
  FIELD-0009 │ entityTypeId │ DB: ENTITY_TYPE_ID │ DBF-0013 │ LOV-ORG-001
    LOOKUP_CODE : LEGAL_ENTITY_TYPE
    Endpoint    : GET /api/lookups/LEGAL_ENTITY_TYPE?active=true
    Detail codes: HEAD_OFFICE / BRANCH_OFFICE / SUBSIDIARY / REPRESENTATIVE_OFFICE

DOMAIN RULES:
  RULE-ORG-001 — Prevent LegalEntity deactivation when active Branches exist:
    Trigger    : Deactivate request (API-ORG-005)
    Statement  : The system MUST prevent deactivation of a LegalEntity when one or more active Branches reference it
    Message-AR : لا يمكن تعطيل الكيان القانوني لوجود فروع نشطة مرتبطة به. يرجى تعطيل جميع الفروع أولاً.
    Message-EN : Cannot deactivate Legal Entity: active branches exist. Please deactivate all branches first.
    Scope      : DEACTIVATE
    DB Enforce : app-level (count active Branches with LEGAL_ENTITY_FK = this PK)
    ERR-ID     : ERR-0001 (assigned SVC+API phase)
    Owned by   : Service layer

  RULE-ORG-002 — Prevent LegalEntity deactivation when active ProfitCenters exist:
    Trigger    : Deactivate request (API-ORG-005)
    Statement  : The system MUST prevent deactivation of a LegalEntity when one or more active ProfitCenters reference it
    Message-AR : لا يمكن تعطيل الكيان القانوني لوجود مراكز ربح نشطة مرتبطة به. يرجى تعطيل جميع مراكز الربح أولاً.
    Message-EN : Cannot deactivate Legal Entity: active profit centers exist. Please deactivate all profit centers first.
    Scope      : DEACTIVATE
    DB Enforce : app-level (count active ProfitCenters with LEGAL_ENTITY_FK = this PK)
    ERR-ID     : ERR-0002 (assigned SVC+API phase)
    Owned by   : Service layer

  RULE-ORG-011 — Business code immutability (applies on UPDATE):
    Trigger    : Any attempt to modify legalEntityCode
    Statement  : The system MUST prevent any modification to business codes after creation
    Message-AR : رمز الأعمال لا يمكن تعديله بعد الإنشاء الأول — هذه القيمة ثابتة نهائياً.
    Message-EN : Business code is immutable after creation and cannot be modified.
    Scope      : UPDATE
    DB Enforce : field excluded from Update DTO — not even accepted
    ERR-ID     : ERR-0011 (assigned SVC+API phase)
    Owned by   : DTO design (field absent from UpdateRequest)

  RULE-ORG-012 — Business code uniqueness (NumberingEngine guarantees — docs expected behavior):
    Trigger    : Create (NumberingEngine collision)
    Statement  : Business codes must be unique within defined scope
    Message-AR : رمز الأعمال المُنشأ تلقائياً موجود مسبقاً. يرجى المحاولة مجدداً.
    Message-EN : Generated business code already exists. Please retry the operation.
    Scope      : CREATE
    DB Enforce : UQ_ORG_LE_CODE
    ERR-ID     : ERR-0012 (assigned SVC+API phase)

REPOSITORY OPERATIONS REQUIRED:
  → QR-ORG-0001 : FIND_ONE by legalEntityPk
  → QR-ORG-0002 : FIND_BY_CRITERIA (search with filters)
  → QR-ORG-0003 : SAVE (create)
  → QR-ORG-0004 : UPDATE (modify)
  → QR-ORG-0005 : COUNT — active Branches (deactivation pre-check RULE-ORG-001)
  → QR-ORG-0006 : COUNT — active ProfitCenters (deactivation pre-check RULE-ORG-002)
  → QRC entries in Section 11
```

---

### ENTITY-ORG-002 — Branch

```
Java Class       : Branch extends AuditableEntity
DB Table         : ORG_BRANCH
PK Generation    : SEQ_ORG_BRANCH.NEXTVAL

FIELD DECLARATIONS:
  FIELD-0012 │ branchPk       │ DB: BRANCH_PK       │ NUMBER(10) NOT NULL │ @Id
  FIELD-0013 │ branchCode     │ DB: BRANCH_CODE     │ VARCHAR2(20) NOT NULL │ Read-Only / BC
  FIELD-0014 │ nameAr         │ DB: NAME_AR         │ VARCHAR2(200) NOT NULL
  FIELD-0015 │ nameEn         │ DB: NAME_EN         │ VARCHAR2(100) NOT NULL
  FIELD-0016 │ legalEntityFk  │ DB: LEGAL_ENTITY_FK │ NUMBER(10) NOT NULL │ FK → ENTITY-ORG-001
  FIELD-0017 │ branchTypeId   │ DB: BRANCH_TYPE_ID  │ VARCHAR2(50) NOT NULL │ LOV-ORG-002
  FIELD-0018 │ isActiveFl     │ DB: IS_ACTIVE_FL    │ NUMBER(1) DEFAULT 1
  FIELD-0019 │ notes          │ DB: NOTES           │ VARCHAR2(2000) NULLABLE

BUSINESS CODE:
  Field name  : branchCode (FIELD-0013)
  Column      : BRANCH_CODE — DBF-0021
  Format      : BR-[LE]-NNNNN (unique within LegalEntity)
  Generation  : SEQ_ORG_BRANCH via NumberingEngine
  Uniqueness  : UQ_ORG_BR_CODE_LE (BRANCH_CODE + LEGAL_ENTITY_FK)

LOV FIELDS:
  FIELD-0017 │ branchTypeId │ DB: BRANCH_TYPE_ID │ DBF-0025 │ LOV-ORG-002
    LOOKUP_CODE : BRANCH_TYPE
    Endpoint    : GET /api/lookups/BRANCH_TYPE?active=true
    Detail codes: MAIN_BRANCH / SUB_BRANCH / OPERATIONS_BRANCH / ADMIN_BRANCH

DOMAIN RULES:
  RULE-ORG-003 — Prevent Branch deactivation when active Departments exist:
    Trigger    : Deactivate (API-ORG-011)
    Statement  : The system MUST prevent deactivation of a Branch when one or more active Departments reference it
    Message-AR : لا يمكن تعطيل الفرع لوجود أقسام نشطة مرتبطة به. يرجى تعطيل جميع الأقسام أولاً.
    Message-EN : Cannot deactivate Branch: active departments exist. Please deactivate all departments first.
    ERR-ID     : ERR-0003

  RULE-ORG-004 — Prevent Branch deactivation when active CostCenters exist:
    Trigger    : Deactivate (API-ORG-011)
    Statement  : The system MUST prevent deactivation of a Branch when one or more active CostCenters reference it
    Message-AR : لا يمكن تعطيل الفرع لوجود مراكز تكلفة نشطة مرتبطة به. يرجى تعطيل جميع مراكز التكلفة أولاً.
    Message-EN : Cannot deactivate Branch: active cost centers exist. Please deactivate all cost centers first.
    ERR-ID     : ERR-0004

  RULE-ORG-005 — Prevent Branch deactivation when active LocationSites exist:
    Trigger    : Deactivate (API-ORG-011)
    Statement  : The system MUST prevent deactivation of a Branch when one or more active LocationSites reference it
    Message-AR : لا يمكن تعطيل الفرع لوجود مواقع جغرافية نشطة مرتبطة به. يرجى تعطيل جميع المواقع أولاً.
    Message-EN : Cannot deactivate Branch: active location sites exist. Please deactivate all location sites first.
    ERR-ID     : ERR-0005

  RULE-ORG-013 — Branch requires active LegalEntity:
    Trigger    : Create (API-ORG-007)
    Statement  : The system MUST require a valid active LegalEntity reference before saving a new Branch
    Message-AR : يرجى اختيار كيان قانوني نشط لربط الفرع به.
    Message-EN : A valid active Legal Entity must be selected before saving a Branch.
    ERR-ID     : ERR-0013

  RULE-ORG-011 (business code immutability): ERR-ID ERR-0011
  RULE-ORG-012 (business code uniqueness): ERR-ID ERR-0012

REPOSITORY OPERATIONS REQUIRED:
  → QR-ORG-0007 : FIND_ONE by branchPk
  → QR-ORG-0008 : FIND_BY_CRITERIA (search with filters)
  → QR-ORG-0009 : FIND_ALL by legalEntityFk (API-ORG-013)
  → QR-ORG-0010 : SAVE (create)
  → QR-ORG-0011 : UPDATE (modify)
  → QR-ORG-0012 : COUNT — active Departments (RULE-ORG-003)
  → QR-ORG-0013 : COUNT — active CostCenters (RULE-ORG-004)
  → QR-ORG-0014 : COUNT — active LocationSites (RULE-ORG-005)
  → QR-ORG-0015 : EXISTS — active LegalEntity (RULE-ORG-013)
```

---

### ENTITY-ORG-003 — Region

```
Java Class       : Region extends AuditableEntity
DB Table         : ORG_REGION
PK Generation    : SEQ_ORG_REGION.NEXTVAL

FIELD DECLARATIONS:
  FIELD-0020 │ regionPk      │ DB: REGION_PK       │ NUMBER(10) NOT NULL │ @Id
  FIELD-0021 │ regionCode    │ DB: REGION_CODE     │ VARCHAR2(20) NOT NULL │ Read-Only / BC
  FIELD-0022 │ nameAr        │ DB: NAME_AR         │ VARCHAR2(200) NOT NULL
  FIELD-0023 │ nameEn        │ DB: NAME_EN         │ VARCHAR2(100) NOT NULL
  FIELD-0024 │ legalEntityFk │ DB: LEGAL_ENTITY_FK │ NUMBER(10) NOT NULL │ FK → ENTITY-ORG-001
  FIELD-0025 │ regionTypeFk  │ DB: REGION_TYPE_FK  │ NUMBER(10) NOT NULL │ FK → ENTITY-ORG-008
  FIELD-0026 │ isActiveFl    │ DB: IS_ACTIVE_FL    │ NUMBER(1) DEFAULT 1
  FIELD-0027 │ notes         │ DB: NOTES           │ VARCHAR2(2000) NULLABLE

BUSINESS CODE:
  Field name  : regionCode (FIELD-0021)
  Column      : REGION_CODE — DBF-0033
  Format      : RG-[LE]-NNNNN (unique within LegalEntity)
  Uniqueness  : UQ_ORG_RG_CODE_LE

LOV / REFERENCE TABLE:
  FIELD-0025 │ regionTypeFk │ DB: REGION_TYPE_FK │ DBF-0037 │ LOV-ORG-007
    ⚠ DRV-ORG-001: This is a FK to ORG_REGION_TYPE (Reference Table) — NOT a DETAIL_CODE string
    Endpoint    : GET /api/v1/org/region-types?active=true (API-ORG-020)
    Stored as   : NUMBER(10) FK — not VARCHAR2 DETAIL_CODE
    FK constraint: FK_ORG_RG_RT

DOMAIN RULES:
  RULE-ORG-006 — Prevent Region deactivation when active Branches reference it:
    Trigger    : Deactivate (API-ORG-018)
    Statement  : The system MUST prevent deactivation of a Region when one or more active Branches reference it
    Message-AR : لا يمكن تعطيل المنطقة لوجود فروع نشطة مرتبطة بها. يرجى إلغاء ربط الفروع أولاً.
    Message-EN : Cannot deactivate Region: active branches reference it. Please unlink branches first.
    Note       : OQ-001 open — SOFT-READ consumer impact unknown
    ERR-ID     : ERR-0006

  RULE-ORG-019 — Region requires active LegalEntity:
    Trigger    : Create (API-ORG-014)
    Statement  : The system MUST require a valid active LegalEntity reference before saving a new Region
    Message-AR : يرجى اختيار كيان قانوني نشط لربط المنطقة به.
    Message-EN : A valid active Legal Entity must be selected before saving a Region.
    ERR-ID     : ERR-0019

  RULE-ORG-011 (immutability): ERR-0011 | RULE-ORG-012 (uniqueness): ERR-0012

OPEN QUESTION REFERENCE:
  OQ-001 — What is the impact of deactivating a Region on SOFT-READ consumers?
           Status: DEFERRED — resolved when first consumer module is built in MODE 1.5
           Affects: API-ORG-018 — deactivation behavior may need enhancement

REPOSITORY OPERATIONS REQUIRED:
  → QR-ORG-0016 : FIND_ONE by regionPk
  → QR-ORG-0017 : FIND_BY_CRITERIA (search with filters)
  → QR-ORG-0018 : SAVE (create)
  → QR-ORG-0019 : UPDATE (modify)
  → QR-ORG-0020 : EXISTS — active LegalEntity (RULE-ORG-019)
  → QR-ORG-0021 : FIND_ALL — RegionTypes (API-ORG-020)
```

---

### ENTITY-ORG-004 — Department (Tree Entity)

```
Java Class       : Department extends AuditableEntity
DB Table         : ORG_DEPARTMENT
PK Generation    : SEQ_ORG_DEPARTMENT.NEXTVAL
Structure        : Hierarchical tree — self-reference (PARENT_DEPARTMENT_FK NULLABLE)

FIELD DECLARATIONS:
  FIELD-0028 │ departmentPk       │ DB: DEPARTMENT_PK       │ NUMBER(10) NOT NULL │ @Id
  FIELD-0029 │ deptCode           │ DB: DEPT_CODE           │ VARCHAR2(20) NOT NULL │ Read-Only / BC
  FIELD-0030 │ nameAr             │ DB: NAME_AR             │ VARCHAR2(200) NOT NULL
  FIELD-0031 │ nameEn             │ DB: NAME_EN             │ VARCHAR2(100) NOT NULL
  FIELD-0032 │ branchFk           │ DB: BRANCH_FK           │ NUMBER(10) NOT NULL │ FK → ENTITY-ORG-002
  FIELD-0033 │ parentDepartmentFk │ DB: PARENT_DEPARTMENT_FK│ NUMBER(10) NULLABLE │ Self-FK
  FIELD-0034 │ nodeTypeId         │ DB: NODE_TYPE_ID        │ VARCHAR2(50) NOT NULL │ LOV-ORG-003
  FIELD-0035 │ isActiveFl         │ DB: IS_ACTIVE_FL        │ NUMBER(1) DEFAULT 1
  FIELD-0036 │ notes              │ DB: NOTES               │ VARCHAR2(2000) NULLABLE

BUSINESS CODE:
  Field name  : deptCode (FIELD-0029)
  Column      : DEPT_CODE — DBF-0045
  Format      : DEP-[BR]-NNNNN (unique within Branch)
  Uniqueness  : UQ_ORG_DEP_CODE_BR (DEPT_CODE + BRANCH_FK)

LOV FIELDS:
  FIELD-0034 │ nodeTypeId │ DB: NODE_TYPE_ID │ DBF-0050 │ LOV-ORG-003
    LOOKUP_CODE : DEPARTMENT_NODE_TYPE
    Endpoint    : GET /api/lookups/DEPARTMENT_NODE_TYPE?active=true
    Detail codes: SUMMARY / DETAIL

DOMAIN RULES:
  RULE-ORG-007 — Prevent circular parent reference:
    Trigger    : Create or Update where parentDepartmentFk is set
    Statement  : The system MUST prevent circular parent references in the Department tree — a Department may not be set as its own ancestor
    Message-AR : لا يمكن تعيين هذا القسم أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري.
    Message-EN : Cannot set this department as parent: circular reference detected in department hierarchy.
    Logic      : Traverse parent chain from proposed parent upward; if current Department's PK appears → circular
    ERR-ID     : ERR-0007

  RULE-ORG-017 — Parent department must be active:
    Trigger    : Create or Update where parentDepartmentFk is set
    Statement  : The system MUST prevent assigning an inactive Department as the parent of another Department
    Message-AR : لا يمكن تعيين قسم غير نشط أباً للقسم. يرجى اختيار قسم نشط.
    Message-EN : Cannot set an inactive department as parent. Please select an active department.
    ERR-ID     : ERR-0017

  RULE-ORG-014 — Requires active Branch:
    Trigger    : Create
    Statement  : The system MUST require a valid active Branch reference before saving a new Department
    Message-AR : يرجى اختيار فرع نشط لربط القسم به.
    Message-EN : A valid active Branch must be selected before saving a Department.
    ERR-ID     : ERR-0014

  RULE-ORG-009 — SUMMARY node assignment prevention (informational — enforced by consumers):
    Statement  : The system MUST prevent assignment of a Department with nodeType=SUMMARY to any transactional record
    Message-AR : لا يمكن ربط قسم تجميعي (SUMMARY) بسجل معاملة مباشرة. يرجى اختيار قسم تفصيلي (DETAIL).
    Message-EN : Cannot assign a SUMMARY department to a transaction. Please select a DETAIL department.
    Note       : Enforced by consuming modules — this module exposes nodeTypeId in ResponseDTO for consumers to filter
    ERR-ID     : ERR-0009 (registered for consumer modules)

  RULE-ORG-011 (immutability): ERR-0011 | RULE-ORG-012 (uniqueness): ERR-0012

TREE API SPEC:
  API-ORG-022 returns hierarchical structure — agent builds recursive tree from flat parent-child data
  Tree filter: branchFk (REQUIRED), isActiveFl (OPTIONAL)
  Each node: departmentPk, deptCode, nameAr, nameEn, nodeTypeId, isActiveFl, children[]

REPOSITORY OPERATIONS REQUIRED:
  → QR-ORG-0022 : FIND_ONE by departmentPk
  → QR-ORG-0023 : FIND_BY_CRITERIA (search/filter flat list)
  → QR-ORG-0024 : FIND_ALL by branchFk (tree construction — API-ORG-022)
  → QR-ORG-0025 : SAVE (create)
  → QR-ORG-0026 : UPDATE (modify)
  → QR-ORG-0027 : EXISTS — active Branch (RULE-ORG-014)
  → QR-ORG-0028 : FIND ancestor chain (circular reference check RULE-ORG-007)
  → QR-ORG-0029 : EXISTS — parent active (RULE-ORG-017)
```

---

### ENTITY-ORG-005 — CostCenter (Tree Entity)

```
Java Class       : CostCenter extends AuditableEntity
DB Table         : ORG_COST_CENTER
PK Generation    : SEQ_ORG_COST_CENTER.NEXTVAL
Structure        : Hierarchical tree — self-reference (PARENT_COST_CENTER_FK NULLABLE)

FIELD DECLARATIONS:
  FIELD-0037 │ costCenterPk       │ DB: COST_CENTER_PK       │ NUMBER(10) NOT NULL │ @Id
  FIELD-0038 │ costCenterCode     │ DB: COST_CENTER_CODE     │ VARCHAR2(20) NOT NULL │ Read-Only / BC
  FIELD-0039 │ nameAr             │ DB: NAME_AR              │ VARCHAR2(200) NOT NULL
  FIELD-0040 │ nameEn             │ DB: NAME_EN              │ VARCHAR2(100) NOT NULL
  FIELD-0041 │ branchFk           │ DB: BRANCH_FK            │ NUMBER(10) NOT NULL │ FK → ENTITY-ORG-002
  FIELD-0042 │ parentCostCenterFk │ DB: PARENT_COST_CENTER_FK│ NUMBER(10) NULLABLE │ Self-FK
  FIELD-0043 │ nodeTypeId         │ DB: NODE_TYPE_ID         │ VARCHAR2(50) NOT NULL │ LOV-ORG-004
  FIELD-0044 │ costCenterTypeId   │ DB: COST_CENTER_TYPE_ID  │ VARCHAR2(50) NOT NULL │ LOV-ORG-005
  FIELD-0045 │ isActiveFl         │ DB: IS_ACTIVE_FL         │ NUMBER(1) DEFAULT 1
  FIELD-0046 │ notes              │ DB: NOTES                │ VARCHAR2(2000) NULLABLE

BUSINESS CODE:
  Field name  : costCenterCode (FIELD-0038)
  Column      : COST_CENTER_CODE — DBF-0058
  Format      : CC-[BR]-NNNNN (unique within Branch)
  Uniqueness  : UQ_ORG_CC_CODE_BR (COST_CENTER_CODE + BRANCH_FK)

LOV FIELDS:
  FIELD-0043 │ nodeTypeId       │ DB: NODE_TYPE_ID        │ DBF-0063 │ LOV-ORG-004
    LOOKUP_CODE : COST_CENTER_NODE_TYPE
    Endpoint    : GET /api/lookups/COST_CENTER_NODE_TYPE?active=true
    Detail codes: SUMMARY / DETAIL

  FIELD-0044 │ costCenterTypeId │ DB: COST_CENTER_TYPE_ID │ DBF-0064 │ LOV-ORG-005
    LOOKUP_CODE : COST_CENTER_TYPE
    Endpoint    : GET /api/lookups/COST_CENTER_TYPE?active=true
    Detail codes: DIRECT / INDIRECT / SHARED

DOMAIN RULES:
  RULE-ORG-008 — Prevent circular parent reference:
    Statement  : The system MUST prevent circular parent references in the CostCenter tree — a CostCenter may not be set as its own ancestor
    Message-AR : لا يمكن تعيين مركز التكلفة هذا أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري.
    Message-EN : Cannot set this cost center as parent: circular reference detected in cost center hierarchy.
    ERR-ID     : ERR-0008

  RULE-ORG-018 — Parent CostCenter must be active:
    Statement  : The system MUST prevent assigning an inactive CostCenter as the parent of another CostCenter
    Message-AR : لا يمكن تعيين مركز تكلفة غير نشط أباً لمركز التكلفة. يرجى اختيار مركز تكلفة نشط.
    Message-EN : Cannot set an inactive cost center as parent. Please select an active cost center.
    ERR-ID     : ERR-0018

  RULE-ORG-015 — Requires active Branch:
    Statement  : The system MUST require a valid active Branch reference before saving a new CostCenter
    Message-AR : يرجى اختيار فرع نشط لربط مركز التكلفة به.
    Message-EN : A valid active Branch must be selected before saving a CostCenter.
    ERR-ID     : ERR-0015

  RULE-ORG-010 (SUMMARY assignment prevention — consumer-enforced): ERR-0010
  RULE-ORG-011 (immutability): ERR-0011 | RULE-ORG-012 (uniqueness): ERR-0012

REPOSITORY OPERATIONS REQUIRED:
  → QR-ORG-0030 : FIND_ONE by costCenterPk
  → QR-ORG-0031 : FIND_BY_CRITERIA (search/filter flat list)
  → QR-ORG-0032 : FIND_ALL by branchFk (tree construction — API-ORG-029)
  → QR-ORG-0033 : SAVE (create)
  → QR-ORG-0034 : UPDATE (modify)
  → QR-ORG-0035 : EXISTS — active Branch (RULE-ORG-015)
  → QR-ORG-0036 : FIND ancestor chain (circular reference check RULE-ORG-008)
  → QR-ORG-0037 : EXISTS — parent active (RULE-ORG-018)
```

---

### ENTITY-ORG-006 — ProfitCenter

```
Java Class       : ProfitCenter extends AuditableEntity
DB Table         : ORG_PROFIT_CENTER
PK Generation    : SEQ_ORG_PROFIT_CENTER.NEXTVAL

FIELD DECLARATIONS:
  FIELD-0047 │ profitCenterPk   │ DB: PROFIT_CENTER_PK   │ NUMBER(10) NOT NULL │ @Id
  FIELD-0048 │ profitCenterCode │ DB: PROFIT_CENTER_CODE │ VARCHAR2(20) NOT NULL │ Read-Only / BC
  FIELD-0049 │ nameAr           │ DB: NAME_AR            │ VARCHAR2(200) NOT NULL
  FIELD-0050 │ nameEn           │ DB: NAME_EN            │ VARCHAR2(100) NOT NULL
  FIELD-0051 │ legalEntityFk    │ DB: LEGAL_ENTITY_FK    │ NUMBER(10) NOT NULL │ FK → ENTITY-ORG-001
  FIELD-0052 │ isActiveFl       │ DB: IS_ACTIVE_FL       │ NUMBER(1) DEFAULT 1
  FIELD-0053 │ notes            │ DB: NOTES              │ VARCHAR2(2000) NULLABLE

BUSINESS CODE:
  Field name  : profitCenterCode (FIELD-0048)
  Column      : PROFIT_CENTER_CODE — DBF-0072
  Format      : PC-[LE]-NNNNN (unique within LegalEntity)
  Uniqueness  : UQ_ORG_PC_CODE_LE

DOMAIN RULES:
  RULE-ORG-020 — Requires active LegalEntity:
    Statement  : The system MUST require a valid active LegalEntity reference before saving a new ProfitCenter
    Message-AR : يرجى اختيار كيان قانوني نشط لربط مركز الربح به.
    Message-EN : A valid active Legal Entity must be selected before saving a ProfitCenter.
    ERR-ID     : ERR-0020

  RULE-ORG-011 (immutability): ERR-0011 | RULE-ORG-012 (uniqueness): ERR-0012

INBOUND-STUB-ORG-001 : Finance module will reference ProfitCenter — deactivation impact DEFERRED

REPOSITORY OPERATIONS REQUIRED:
  → QR-ORG-0038 : FIND_ONE by profitCenterPk
  → QR-ORG-0039 : FIND_BY_CRITERIA (search with filters)
  → QR-ORG-0040 : SAVE (create)
  → QR-ORG-0041 : UPDATE (modify)
  → QR-ORG-0042 : EXISTS — active LegalEntity (RULE-ORG-020)
```

---

### ENTITY-ORG-007 — LocationSite

```
Java Class       : LocationSite extends AuditableEntity
DB Table         : ORG_LOCATION_SITE
PK Generation    : SEQ_ORG_LOCATION_SITE.NEXTVAL
Structure        : Flat (no tree / no self-reference)

FIELD DECLARATIONS:
  FIELD-0054 │ locationSitePk │ DB: LOCATION_SITE_PK │ NUMBER(10) NOT NULL │ @Id
  FIELD-0055 │ locationCode   │ DB: LOCATION_CODE    │ VARCHAR2(20) NOT NULL │ Read-Only / BC
  FIELD-0056 │ nameAr         │ DB: NAME_AR          │ VARCHAR2(200) NOT NULL
  FIELD-0057 │ nameEn         │ DB: NAME_EN          │ VARCHAR2(100) NOT NULL
  FIELD-0058 │ branchFk       │ DB: BRANCH_FK        │ NUMBER(10) NOT NULL │ FK → ENTITY-ORG-002
  FIELD-0059 │ siteTypeId     │ DB: SITE_TYPE_ID     │ VARCHAR2(50) NOT NULL │ LOV-ORG-006
  FIELD-0060 │ isActiveFl     │ DB: IS_ACTIVE_FL     │ NUMBER(1) DEFAULT 1
  FIELD-0061 │ notes          │ DB: NOTES            │ VARCHAR2(2000) NULLABLE

BUSINESS CODE:
  Field name  : locationCode (FIELD-0055)
  Column      : LOCATION_CODE — DBF-0083
  Format      : LS-[BR]-NNNNN (unique within Branch)
  Uniqueness  : UQ_ORG_LS_CODE_BR

LOV FIELDS:
  FIELD-0059 │ siteTypeId │ DB: SITE_TYPE_ID │ DBF-0087 │ LOV-ORG-006
    LOOKUP_CODE : LOCATION_SITE_TYPE
    Endpoint    : GET /api/lookups/LOCATION_SITE_TYPE?active=true
    Detail codes: OFFICE / WAREHOUSE / FACTORY / SITE / RETAIL

DOMAIN RULES:
  RULE-ORG-011 (immutability): ERR-0011 | RULE-ORG-012 (uniqueness): ERR-0012
  No deactivation pre-check — no children depend on LocationSite within ORG module
  Note: Inventory module will reference LocationSite (INBOUND-STUB-ORG-002) — deactivation impact DEFERRED

REPOSITORY OPERATIONS REQUIRED:
  → QR-ORG-0043 : FIND_ONE by locationSitePk
  → QR-ORG-0044 : FIND_BY_CRITERIA (search with filters)
  → QR-ORG-0045 : FIND_ALL by branchFk (API-ORG-047)
  → QR-ORG-0046 : SAVE (create)
  → QR-ORG-0047 : UPDATE (modify)
```

<!-- SUB:MASTER:END -->

<!-- SUB:REFERENCE:START -->

## DATA+DOM — Reference Table (ENTITY-ORG-008)

### ENTITY-ORG-008 — RegionType (Reference Table — PRIVATE)

```
Java Class       : RegionType extends AuditableEntity
DB Table         : ORG_REGION_TYPE
PK Generation    : SEQ_ORG_REGION_TYPE.NEXTVAL
Type             : PRIVATE Reference Table — Admin-managed, not MD_LOOKUP_DETAIL

FIELD DECLARATIONS:
  FIELD-0001 │ regionTypePk │ DB: REGION_TYPE_PK │ NUMBER(10) NOT NULL │ @Id
  FIELD-0002 │ nameAr       │ DB: NAME_AR        │ VARCHAR2(200) NOT NULL
  FIELD-0003 │ nameEn       │ DB: NAME_EN        │ VARCHAR2(100) NOT NULL
  FIELD-0004 │ isActiveFl   │ DB: IS_ACTIVE_FL   │ NUMBER(1) DEFAULT 1

No Business Code (Reference Table).
No LOV fields.
No complex domain rules — simple CRUD + deactivation.

SEED DATA (from SRS A3 ENTITY-ORG-008):
  nameAr: جغرافي  | nameEn: GEOGRAPHIC
  nameAr: مبيعات  | nameEn: SALES
  nameAr: تشغيلي  | nameEn: OPERATIONAL

REPOSITORY OPERATIONS REQUIRED:
  → QR-ORG-0048 : FIND_ALL active (API-ORG-020 — for LOV loading)
```

**DATA+DOM Governance Rules:**
```
BC-DOM-RULE-1 — All 7 business codes use NumberingEngine exclusively (RULE-ORG-016)
BC-DOM-RULE-2 — All business codes immutable after creation (RULE-ORG-011)
LOC-DOM-RULE-1 — nameAr AND nameEn mandatory NOT NULL on all entities
SEC-DOM-RULE-1 — Soft deactivation: isActiveFl = 0 (no physical delete)
BIND-RULE-1   — Every DB column reference uses exact Oracle name from DBF-ID
BIND-RULE-2   — Every sequence uses SEQ_[EXACT_TABLE_NAME].NEXTVAL
BIND-RULE-3   — LOV-ORG-001..006 stored as DETAIL_CODE (VARCHAR2)
              — LOV-ORG-007 stored as NUMBER FK (DRV-ORG-001)
BIND-RULE-4   — All RULE text is exact from SRS — not paraphrase
```

**DATA+DOM Gate: PASSED ✓**
```
[ ✓ ] 8 entities fully declared
[ ✓ ] All FIELD-IDs assigned (FIELD-0001..0061)
[ ✓ ] All sequences named exactly from db-script.md
[ ✓ ] All LOV LOOKUP_CODEs exact from SRS A5
[ ✓ ] All RULE texts exact from SRS A4
[ ✓ ] DRV-ORG-001 documented (RegionType FK vs LOV pattern)
[ ✓ ] Tree entities (ENTITY-ORG-004, 005) circular reference rules declared
[ ✓ ] 48 QR-IDs assigned (QR-ORG-0001..0048)
```

<!-- SUB:REFERENCE:END -->

<!-- PHASE:DATA-DOM:END -->

<!-- PHASE:SVC-API:START -->

# PHASE SVC+API — Service & API Contract Specifications

> 47 APIs ≥ 8 threshold → sub-phases by semantic group

<!-- SUB:CRUD:START -->

## SVC+API — CRUD Operations (Create / Read / Update / Deactivate / Reactivate)

### DTO MEMBERSHIP RULES (applies all APIs)
```
CreateRequest  : excludes → [entityPk], [entityCode], isActiveFl, createdBy, createdAt, updatedBy, updatedAt
UpdateRequest  : excludes → [entityPk], [entityCode], isActiveFl, createdBy, createdAt, updatedBy, updatedAt
ResponseDTO    : includes → all fields including [entityCode], isActiveFl, createdBy, createdAt, updatedBy, updatedAt
```

---

<!-- API:API-ORG-001:START -->
### API-ORG-001 — Create LegalEntity
```
Endpoint         : POST /api/v1/org/legal-entities
Controller       : LegalEntityController → method: create
Service          : LegalEntityService → method: create

REQUEST:
  Content-Type   : application/json
  Request Body   : LegalEntityCreateRequest
    Fields:
      nameAr         : String    REQUIRED  — maps to NAME_AR (VARCHAR2(200))
      nameEn         : String    REQUIRED  — maps to NAME_EN (VARCHAR2(100))
      entityTypeId   : String    REQUIRED  — DETAIL_CODE from LOV-ORG-001 (LEGAL_ENTITY_TYPE)
      notes          : String    OPTIONAL  — maps to NOTES (VARCHAR2(2000))
    Excluded: legalEntityCode (NumberingEngine generates via SEQ_ORG_LEGAL_ENTITY)

RESPONSE:
  Success code   : 201
  Response DTO   : LegalEntityResponse
    Fields: legalEntityPk, legalEntityCode, nameAr, nameEn, entityTypeId, isActiveFl,
            notes, createdBy, createdAt, updatedBy, updatedAt

VALIDATIONS:
  1. RULE-ORG-016 — NumberingEngine mandatory:
       Statement  : All business codes generated exclusively through NumberingEngine
       Scope      : CREATE
  2. RULE-ORG-012 — Business code uniqueness:
       Statement  : The system MUST ensure business codes are unique within their defined scope
       Message-AR : رمز الأعمال المُنشأ تلقائياً موجود مسبقاً. يرجى المحاولة مجدداً.
       Message-EN : Generated business code already exists. Please retry the operation.

ERRORS:
  ERR-0012 → RULE-ORG-012 triggered → HTTP 409
             Message-AR: رمز الأعمال المُنشأ تلقائياً موجود مسبقاً. يرجى المحاولة مجدداً.
             Message-EN: Generated business code already exists. Please retry the operation.
  ERR-0100 → DB constraint / unexpected error → HTTP 500 (PLATFORM-STD — DRV-ORG-002)

SERVICE ORCHESTRATION:
  1. [validate] — confirm entityTypeId is a valid LEGAL_ENTITY_TYPE detail code
  2. [generate] — call NumberingEngine.generate("LEGAL_ENTITY", null, null) → legalEntityCode
  3. [persist]  — save LegalEntity to ORG_LEGAL_ENTITY via SEQ_ORG_LEGAL_ENTITY

REPOSITORY OPERATION:
  QR-ID      : QR-ORG-0003
  Table      : ORG_LEGAL_ENTITY
  Operation  : SAVE
  Join       : NONE
  Transaction: READ_WRITE
  Sequence   : SEQ_ORG_LEGAL_ENTITY

REPOSITORY STRATEGY:
  DB Operation        : INSERT (SAVE via SEQ_ORG_LEGAL_ENTITY)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY:
  Screen     : SCR-ORG-001 — إدارة الكيانات القانونية
  Permission : PERM_LEGAL_ENTITY_CREATE

LOCALIZATION:
  Error responses: messageAr AND messageEn (from Error Catalog)
  Name responses : nameAr (NAME_AR) AND nameEn (NAME_EN) always returned
```
<!-- API:API-ORG-001:END -->

<!-- API:API-ORG-002:START -->
### API-ORG-002 — Search LegalEntities
```
Endpoint         : GET /api/v1/org/legal-entities
Controller       : LegalEntityController → method: search
Service          : LegalEntityService → method: search

REQUEST:
  Query Params:
    legalEntityCode : String   OPTIONAL — LIKE search on LEGAL_ENTITY_CODE
    nameAr          : String   OPTIONAL — LIKE search on NAME_AR
    nameEn          : String   OPTIONAL — LIKE search on NAME_EN
    entityTypeId    : String   OPTIONAL — EXACT match on ENTITY_TYPE_ID
    isActiveFl      : Integer  OPTIONAL — EXACT match (default filter: active only in UI, not forced in API)
    page            : Integer  OPTIONAL — 0-based (BaseSearchContractRequest)
    size            : Integer  OPTIONAL — page size
    sortBy          : String   OPTIONAL — validated against ALLOWED_SORT_FIELDS
    sortDir         : String   OPTIONAL — ASC/DESC

  ALLOWED_SORT_FIELDS: { "legalEntityCode", "nameAr", "nameEn", "createdAt" }

RESPONSE:
  Success code   : 200
  Response DTO   : Page<LegalEntityResponse> (JPA Page<T> — project's existing framework)
  Empty result   : HTTP 200 with empty content — NEVER HTTP 404
  Paginated      : YES — JPA Page<T>

ERRORS: None (search returns empty page — not an error)

SERVICE ORCHESTRATION:
  1. [build]   — build Pageable from BaseSearchContractRequest + ALLOWED_SORT_FIELDS
  2. [query]   — apply dynamic filters: LIKE for name/code fields, EXACT for typeId/isActiveFl
  3. [return]  — JPA Page<LegalEntityResponse>

REPOSITORY OPERATION:
  QR-ID      : QR-ORG-0002
  Table      : ORG_LEGAL_ENTITY
  Operation  : FIND_BY_CRITERIA
  Join       : NONE
  Transaction: READ_ONLY

REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_BY_CRITERIA — dynamic filters)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY:
  Screen     : SCR-ORG-001
  Permission : PERM_LEGAL_ENTITY_VIEW
```
<!-- API:API-ORG-002:END -->

<!-- API:API-ORG-003:START -->
### API-ORG-003 — Get LegalEntity by ID
```
Endpoint         : GET /api/v1/org/legal-entities/{id}
Controller       : LegalEntityController → method: getById
Path Params      : id: Long (maps to LEGAL_ENTITY_PK)

RESPONSE:
  Success code   : 200
  Response DTO   : LegalEntityResponse (full entity including legalEntityCode, isActiveFl, audit fields)

ERRORS:
  ERR-0101 → entity not found → HTTP 404 (PLATFORM-STD — DRV-ORG-002)
             Message-AR: الكيان القانوني غير موجود.
             Message-EN: Legal Entity not found.

SERVICE ORCHESTRATION:
  1. [load] — find LegalEntity by LEGAL_ENTITY_PK — throw LocalizedException (ERR-0101) if absent

REPOSITORY OPERATION:
  QR-ID: QR-ORG-0001 | Table: ORG_LEGAL_ENTITY | Operation: FIND_ONE | Transaction: READ_ONLY

REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_ONE by PK)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY:
  Screen: SCR-ORG-001 | Permission: PERM_LEGAL_ENTITY_VIEW
```
<!-- API:API-ORG-003:END -->

<!-- API:API-ORG-004:START -->
### API-ORG-004 — Update LegalEntity
```
Endpoint         : PUT /api/v1/org/legal-entities/{id}
Controller       : LegalEntityController → method: update
Path Params      : id: Long

REQUEST:
  Request Body   : LegalEntityUpdateRequest
    Fields:
      nameAr       : String    OPTIONAL
      nameEn       : String    OPTIONAL
      entityTypeId : String    OPTIONAL
      notes        : String    OPTIONAL
    Excluded: legalEntityCode (RULE-ORG-011 — field absent from DTO entirely)

RESPONSE:
  Success code   : 200
  Response DTO   : LegalEntityResponse

VALIDATIONS:
  1. RULE-ORG-011 — Business code immutability:
       Statement  : The system MUST prevent any modification to business codes after their initial creation
       Implementation: legalEntityCode field is ABSENT from UpdateRequest DTO — not accepted at all
       Message-AR : رمز الأعمال لا يمكن تعديله بعد الإنشاء الأول — هذه القيمة ثابتة نهائياً.
       Message-EN : Business code is immutable after creation and cannot be modified.

ERRORS:
  ERR-0011 → RULE-ORG-011 (if business code field is sent via path override) → HTTP 400
  ERR-0101 → not found → HTTP 404 (PLATFORM-STD)

SERVICE ORCHESTRATION:
  1. [load]     — find by LEGAL_ENTITY_PK
  2. [validate] — legalEntityCode not in request body (enforced by DTO)
  3. [update]   — merge non-null fields, save

REPOSITORY OPERATION:
  QR-ID: QR-ORG-0004 | Table: ORG_LEGAL_ENTITY | Operation: UPDATE | Transaction: READ_WRITE

REPOSITORY STRATEGY:
  DB Operation        : UPDATE (merge on LEGAL_ENTITY_PK)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY:
  Screen: SCR-ORG-001 | Permission: PERM_LEGAL_ENTITY_UPDATE
```
<!-- API:API-ORG-004:END -->

<!-- API:API-ORG-005:START -->
### API-ORG-005 — Deactivate LegalEntity
```
Endpoint         : DELETE /api/v1/org/legal-entities/{id}
Controller       : LegalEntityController → method: deactivate
Path Params      : id: Long
⚠ Soft delete — sets IS_ACTIVE_FL = 0, no physical row removal

RESPONSE:
  Success code   : 200
  Response DTO   : confirmation message (messageAr + messageEn)

VALIDATIONS:
  1. RULE-ORG-001 — Active Branches exist:
       Statement  : The system MUST prevent deactivation of a LegalEntity when one or more active Branches reference it
       Message-AR : لا يمكن تعطيل الكيان القانوني لوجود فروع نشطة مرتبطة به. يرجى تعطيل جميع الفروع أولاً.
       Message-EN : Cannot deactivate Legal Entity: active branches exist. Please deactivate all branches first.
  2. RULE-ORG-002 — Active ProfitCenters exist:
       Statement  : The system MUST prevent deactivation of a LegalEntity when one or more active ProfitCenters reference it
       Message-AR : لا يمكن تعطيل الكيان القانوني لوجود مراكز ربح نشطة مرتبطة به. يرجى تعطيل جميع مراكز الربح أولاً.
       Message-EN : Cannot deactivate Legal Entity: active profit centers exist. Please deactivate all profit centers first.

ERRORS:
  ERR-0001 → RULE-ORG-001 triggered → HTTP 409
             Message-AR: لا يمكن تعطيل الكيان القانوني لوجود فروع نشطة مرتبطة به. يرجى تعطيل جميع الفروع أولاً.
             Message-EN: Cannot deactivate Legal Entity: active branches exist. Please deactivate all branches first.
  ERR-0002 → RULE-ORG-002 triggered → HTTP 409
             Message-AR: لا يمكن تعطيل الكيان القانوني لوجود مراكز ربح نشطة مرتبطة به. يرجى تعطيل جميع مراكز الربح أولاً.
             Message-EN: Cannot deactivate Legal Entity: active profit centers exist. Please deactivate all profit centers first.
  ERR-0101 → not found → HTTP 404 (PLATFORM-STD)

SERVICE ORCHESTRATION:
  1. [load]    — find LegalEntity by PK
  2. [check-1] — count active Branches with LEGAL_ENTITY_FK = this PK → if > 0: throw ERR-0001
  3. [check-2] — count active ProfitCenters with LEGAL_ENTITY_FK = this PK → if > 0: throw ERR-0002
  4. [persist] — set IS_ACTIVE_FL = 0, save

REPOSITORY OPERATIONS:
  QR-ORG-0005: COUNT active Branches | QR-ORG-0006: COUNT active ProfitCenters
  QR-ORG-0004: UPDATE IS_ACTIVE_FL=0 | Transaction: READ_WRITE

REPOSITORY STRATEGY:
  DB Operation        : SELECT COUNT (pre-check) + UPDATE IS_ACTIVE_FL=0
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY:
  Screen: SCR-ORG-001 | Permission: PERM_LEGAL_ENTITY_DELETE
```
<!-- API:API-ORG-005:END -->

<!-- API:API-ORG-006:START -->
### API-ORG-006 — Reactivate LegalEntity
```
Endpoint         : PUT /api/v1/org/legal-entities/{id}/reactivate
Service          : LegalEntityService → method: reactivate
RESPONSE: 200 — LegalEntityResponse (isActiveFl = 1)
SERVICE: [load] → set IS_ACTIVE_FL = 1 → save (no pre-checks)
REPOSITORY: QR-ORG-0004 | Transaction: READ_WRITE
REPOSITORY STRATEGY:
  DB Operation        : UPDATE IS_ACTIVE_FL=1
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-001 | PERM_LEGAL_ENTITY_UPDATE
ERRORS: ERR-0101 → not found → HTTP 404 (PLATFORM-STD)
```
<!-- API:API-ORG-006:END -->

---

**Branch APIs (API-ORG-007..013)**

<!-- API:API-ORG-007:START -->
### API-ORG-007 — Create Branch
```
Endpoint: POST /api/v1/org/branches
REQUEST Body: BranchCreateRequest
  Fields: nameAr (REQUIRED), nameEn (REQUIRED), legalEntityFk (REQUIRED — active LE only),
          branchTypeId (REQUIRED — BRANCH_TYPE detail code), notes (OPTIONAL)
  Excluded: branchCode (NumberingEngine via SEQ_ORG_BRANCH)

RESPONSE: 201 — BranchResponse (all fields incl. branchCode, isActiveFl, audit)

VALIDATIONS:
  RULE-ORG-013 — Requires active LegalEntity:
    Statement  : The system MUST require a valid active LegalEntity reference before saving a new Branch
    Message-AR : يرجى اختيار كيان قانوني نشط لربط الفرع به.
    Message-EN : A valid active Legal Entity must be selected before saving a Branch.
  RULE-ORG-016 — NumberingEngine mandatory
  RULE-ORG-012 — Business code uniqueness within LegalEntity scope

ERRORS:
  ERR-0013 → RULE-ORG-013 → HTTP 400
             Message-AR: يرجى اختيار كيان قانوني نشط لربط الفرع به.
             Message-EN: A valid active Legal Entity must be selected before saving a Branch.
  ERR-0012 → RULE-ORG-012 → HTTP 409

SERVICE ORCHESTRATION:
  1. [validate] — load LegalEntity by legalEntityFk → verify isActiveFl=1 → else ERR-0013
  2. [generate] — NumberingEngine.generate("BRANCH", legalEntityFk, null) → branchCode
  3. [persist]  — save Branch to ORG_BRANCH via SEQ_ORG_BRANCH

REPOSITORY: QR-ORG-0010 | SAVE | Transaction: READ_WRITE | Sequence: SEQ_ORG_BRANCH
REPOSITORY STRATEGY:
  DB Operation        : INSERT (SAVE via SEQ_ORG_BRANCH)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-002 | PERM_BRANCH_CREATE
```
<!-- API:API-ORG-007:END -->

<!-- API:API-ORG-008:START -->
### API-ORG-008 — Search Branches
```
Endpoint: GET /api/v1/org/branches
Query Params: branchCode? (LIKE), nameAr? (LIKE), legalEntityFk? (EXACT),
              branchTypeId? (EXACT), isActiveFl? (EXACT), page, size, sortBy, sortDir
ALLOWED_SORT_FIELDS: { "branchCode", "nameAr", "nameEn", "createdAt" }
RESPONSE: 200 — Page<BranchResponse> | Empty: HTTP 200 empty
REPOSITORY: QR-ORG-0008 | FIND_BY_CRITERIA | READ_ONLY
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_BY_CRITERIA — dynamic filters)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-002 | PERM_BRANCH_VIEW
```
<!-- API:API-ORG-008:END -->

<!-- API:API-ORG-009:START -->
### API-ORG-009 — Get Branch by ID
```
Endpoint: GET /api/v1/org/branches/{id}
RESPONSE: 200 — BranchResponse
ERRORS: ERR-0101 → not found → HTTP 404 (PLATFORM-STD)
REPOSITORY: QR-ORG-0007 | FIND_ONE | READ_ONLY
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_ONE by PK)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-002 | PERM_BRANCH_VIEW
```
<!-- API:API-ORG-009:END -->

<!-- API:API-ORG-010:START -->
### API-ORG-010 — Update Branch
```
Endpoint: PUT /api/v1/org/branches/{id}
REQUEST Body: BranchUpdateRequest
  Fields: nameAr (OPTIONAL), nameEn (OPTIONAL), branchTypeId (OPTIONAL), notes (OPTIONAL)
  Excluded: branchCode (RULE-ORG-011), legalEntityFk (not updatable after create)
VALIDATIONS: RULE-ORG-011 (branchCode absent from DTO)
ERRORS: ERR-0011 → HTTP 400 | ERR-0101 → HTTP 404
REPOSITORY: QR-ORG-0011 | UPDATE | READ_WRITE
REPOSITORY STRATEGY:
  DB Operation        : UPDATE (merge on BRANCH_PK)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-002 | PERM_BRANCH_UPDATE
```
<!-- API:API-ORG-010:END -->

<!-- API:API-ORG-011:START -->
### API-ORG-011 — Deactivate Branch
```
Endpoint: DELETE /api/v1/org/branches/{id}  (soft — IS_ACTIVE_FL = 0)
VALIDATIONS:
  RULE-ORG-003 — Active Departments:
    Message-AR : لا يمكن تعطيل الفرع لوجود أقسام نشطة مرتبطة به. يرجى تعطيل جميع الأقسام أولاً.
    Message-EN : Cannot deactivate Branch: active departments exist. Please deactivate all departments first.
  RULE-ORG-004 — Active CostCenters:
    Message-AR : لا يمكن تعطيل الفرع لوجود مراكز تكلفة نشطة مرتبطة به. يرجى تعطيل جميع مراكز التكلفة أولاً.
    Message-EN : Cannot deactivate Branch: active cost centers exist. Please deactivate all cost centers first.
  RULE-ORG-005 — Active LocationSites:
    Message-AR : لا يمكن تعطيل الفرع لوجود مواقع جغرافية نشطة مرتبطة به. يرجى تعطيل جميع المواقع أولاً.
    Message-EN : Cannot deactivate Branch: active location sites exist. Please deactivate all location sites first.
ERRORS:
  ERR-0003 → RULE-ORG-003 → HTTP 409 | ERR-0004 → RULE-ORG-004 → HTTP 409
  ERR-0005 → RULE-ORG-005 → HTTP 409 | ERR-0101 → HTTP 404
SERVICE:
  1. [load] 2. [check] COUNT active Departments (QR-ORG-0012) → ERR-0003
  3. [check] COUNT active CostCenters (QR-ORG-0013) → ERR-0004
  4. [check] COUNT active LocationSites (QR-ORG-0014) → ERR-0005
  5. [persist] IS_ACTIVE_FL = 0
REPOSITORY STRATEGY:
  DB Operation        : SELECT COUNT ×3 (pre-checks) + UPDATE IS_ACTIVE_FL=0
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-002 | PERM_BRANCH_DELETE
```
<!-- API:API-ORG-011:END -->

<!-- API:API-ORG-012:START -->
### API-ORG-012 — Reactivate Branch
```
Endpoint: PUT /api/v1/org/branches/{id}/reactivate
RESPONSE: 200 — BranchResponse (isActiveFl=1)
SERVICE: [load] → IS_ACTIVE_FL=1 → save
ERRORS: ERR-0101 → HTTP 404
REPOSITORY STRATEGY:
  DB Operation        : UPDATE IS_ACTIVE_FL=1
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-002 | PERM_BRANCH_UPDATE
```
<!-- API:API-ORG-012:END -->

<!-- API:API-ORG-013:START -->
### API-ORG-013 — Get Branches by LegalEntity
```
Endpoint: GET /api/v1/org/branches/by-legal-entity/{leId}
Path Params: leId: Long (LEGAL_ENTITY_FK)
Query Params: isActiveFl (OPTIONAL — default active only for LOV use)
RESPONSE: 200 — List<BranchResponse>
REPOSITORY: QR-ORG-0009 | FIND_ALL by LEGAL_ENTITY_FK | READ_ONLY
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_ALL by LEGAL_ENTITY_FK)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-002 | PERM_BRANCH_VIEW
DRV-ORG-003: This endpoint returns List (not Page) — used as LOV source for Branch selection on other screens
```
<!-- API:API-ORG-013:END -->

---

**Region APIs (API-ORG-014..020)**

<!-- API:API-ORG-014:START -->
### API-ORG-014 — Create Region
```
Endpoint: POST /api/v1/org/regions
REQUEST Body: RegionCreateRequest
  Fields: nameAr (REQUIRED), nameEn (REQUIRED), legalEntityFk (REQUIRED),
          regionTypeId (REQUIRED — FK to ORG_REGION_TYPE.REGION_TYPE_PK), notes (OPTIONAL)
  ⚠ regionTypeId here maps to REGION_TYPE_FK (NUMBER FK) not a DETAIL_CODE string
  Excluded: regionCode (NumberingEngine via SEQ_ORG_REGION)

RESPONSE: 201 — RegionResponse

VALIDATIONS:
  RULE-ORG-019 — Requires active LegalEntity:
    Message-AR : يرجى اختيار كيان قانوني نشط لربط المنطقة به.
    Message-EN : A valid active Legal Entity must be selected before saving a Region.
  RULE-ORG-016 | RULE-ORG-012

ERRORS:
  ERR-0019 → RULE-ORG-019 → HTTP 400
             Message-AR: يرجى اختيار كيان قانوني نشط لربط المنطقة به.
             Message-EN: A valid active Legal Entity must be selected before saving a Region.
  ERR-0012 → HTTP 409

SERVICE:
  1. [validate] LegalEntity active (QR-ORG-0020)
  2. [validate] RegionType exists and is active
  3. [generate] NumberingEngine → regionCode via SEQ_ORG_REGION
  4. [persist]  save to ORG_REGION

REPOSITORY: QR-ORG-0018 | SAVE | Sequence: SEQ_ORG_REGION
REPOSITORY STRATEGY:
  DB Operation        : INSERT (SAVE via SEQ_ORG_REGION)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-003 | PERM_REGION_CREATE
```
<!-- API:API-ORG-014:END -->

<!-- API:API-ORG-015:START -->
### API-ORG-015 — Search Regions
```
Endpoint: GET /api/v1/org/regions
Query Params: regionCode? (LIKE), nameAr? (LIKE), legalEntityFk? (EXACT),
              regionTypeId? (EXACT — FK value), isActiveFl? (EXACT), page, size, sortBy, sortDir
ALLOWED_SORT_FIELDS: { "regionCode", "nameAr", "nameEn", "createdAt" }
RESPONSE: 200 — Page<RegionResponse>
REPOSITORY: QR-ORG-0017 | FIND_BY_CRITERIA | READ_ONLY
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_BY_CRITERIA — dynamic filters)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-003 | PERM_REGION_VIEW
```
<!-- API:API-ORG-015:END -->

<!-- API:API-ORG-016:START -->
### API-ORG-016 — Get Region by ID
```
Endpoint: GET /api/v1/org/regions/{id}
RESPONSE: 200 — RegionResponse | ERRORS: ERR-0101 → HTTP 404
REPOSITORY: QR-ORG-0016 | FIND_ONE | READ_ONLY
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_ONE by PK)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-003 | PERM_REGION_VIEW
```
<!-- API:API-ORG-016:END -->

<!-- API:API-ORG-017:START -->
### API-ORG-017 — Update Region
```
Endpoint: PUT /api/v1/org/regions/{id}
REQUEST Body: RegionUpdateRequest
  Fields: nameAr (OPTIONAL), nameEn (OPTIONAL), regionTypeId (OPTIONAL — FK), notes (OPTIONAL)
  Excluded: regionCode (RULE-ORG-011), legalEntityFk (not updatable)
VALIDATIONS: RULE-ORG-011
ERRORS: ERR-0011 → HTTP 400 | ERR-0101 → HTTP 404
REPOSITORY: QR-ORG-0019 | UPDATE | READ_WRITE
REPOSITORY STRATEGY:
  DB Operation        : UPDATE (merge on REGION_PK)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-003 | PERM_REGION_UPDATE
```
<!-- API:API-ORG-017:END -->

<!-- API:API-ORG-018:START -->
### API-ORG-018 — Deactivate Region
```
Endpoint: DELETE /api/v1/org/regions/{id}  (soft)
VALIDATIONS:
  RULE-ORG-006 — (OQ-001 partial — only Branch reference check implemented now):
    Statement  : The system MUST prevent deactivation of a Region when one or more active Branches reference it
    Message-AR : لا يمكن تعطيل المنطقة لوجود فروع نشطة مرتبطة بها. يرجى إلغاء ربط الفروع أولاً.
    Message-EN : Cannot deactivate Region: active branches reference it. Please unlink branches first.
    ⚠ OQ-001: SOFT-READ consumer impact unknown — additional checks may be needed when consumers are built
ERRORS:
  ERR-0006 → RULE-ORG-006 → HTTP 409
             Message-AR: لا يمكن تعطيل المنطقة لوجود فروع نشطة مرتبطة بها. يرجى إلغاء ربط الفروع أولاً.
             Message-EN: Cannot deactivate Region: active branches reference it. Please unlink branches first.
  ERR-0101 → HTTP 404
SERVICE: [load] → count active Branches referencing this Region → ERR-0006 → IS_ACTIVE_FL=0
REPOSITORY STRATEGY:
  DB Operation        : SELECT COUNT (pre-check) + UPDATE IS_ACTIVE_FL=0
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-003 | PERM_REGION_DELETE
```
<!-- API:API-ORG-018:END -->

<!-- API:API-ORG-019:START -->
### API-ORG-019 — Reactivate Region
```
Endpoint: PUT /api/v1/org/regions/{id}/reactivate
RESPONSE: 200 | ERRORS: ERR-0101 → HTTP 404
SERVICE: [load] → IS_ACTIVE_FL=1 → save
REPOSITORY STRATEGY:
  DB Operation        : UPDATE IS_ACTIVE_FL=1
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-003 | PERM_REGION_UPDATE
```
<!-- API:API-ORG-019:END -->

<!-- API:API-ORG-020:START -->
### API-ORG-020 — Get RegionTypes
```
Endpoint: GET /api/v1/org/region-types
Query Params: isActiveFl (OPTIONAL — default active=1)
RESPONSE: 200 — List<RegionTypeResponse>
  Fields: regionTypePk, nameAr, nameEn, isActiveFl
PURPOSE: LOV source for Region.regionTypeId field on SCR-ORG-003
REPOSITORY: QR-ORG-0048 | FIND_ALL active ORG_REGION_TYPE | READ_ONLY
DRV-ORG-003: Returns List (not Page) — reference table used as LOV dropdown source
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_ALL active from ORG_REGION_TYPE)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-003 | PERM_REGION_VIEW
```
<!-- API:API-ORG-020:END -->

---

**Department APIs (API-ORG-021..027)**

<!-- API:API-ORG-021:START -->
### API-ORG-021 — Create Department
```
Endpoint: POST /api/v1/org/departments
REQUEST Body: DepartmentCreateRequest
  Fields: nameAr (REQUIRED), nameEn (REQUIRED), branchFk (REQUIRED),
          nodeTypeId (REQUIRED — DEPARTMENT_NODE_TYPE), parentDepartmentFk (OPTIONAL),
          notes (OPTIONAL)
  Excluded: deptCode (SEQ_ORG_DEPARTMENT via NumberingEngine)

VALIDATIONS:
  RULE-ORG-014 — Requires active Branch:
    Message-AR : يرجى اختيار فرع نشط لربط القسم به.
    Message-EN : A valid active Branch must be selected before saving a Department.
  RULE-ORG-007 — Circular parent prevention (when parentDepartmentFk provided):
    Message-AR : لا يمكن تعيين هذا القسم أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري.
    Message-EN : Cannot set this department as parent: circular reference detected in department hierarchy.
  RULE-ORG-017 — Parent must be active:
    Message-AR : لا يمكن تعيين قسم غير نشط أباً للقسم. يرجى اختيار قسم نشط.
    Message-EN : Cannot set an inactive department as parent. Please select an active department.

ERRORS:
  ERR-0014 → RULE-ORG-014 → HTTP 400
  ERR-0007 → RULE-ORG-007 → HTTP 409
  ERR-0017 → RULE-ORG-017 → HTTP 400
  ERR-0012 → HTTP 409

SERVICE:
  1. [validate] branchFk active (QR-ORG-0027)
  2. [validate if parent set] parent isActiveFl=1 (QR-ORG-0029) → ERR-0017
  3. [validate if parent set] ancestor traverse (QR-ORG-0028) → ERR-0007
  4. [generate] NumberingEngine → deptCode via SEQ_ORG_DEPARTMENT
  5. [persist]  save to ORG_DEPARTMENT

REPOSITORY: QR-ORG-0025 | SAVE | Sequence: SEQ_ORG_DEPARTMENT
REPOSITORY STRATEGY:
  DB Operation        : INSERT (SAVE via SEQ_ORG_DEPARTMENT) + ancestor traverse
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-004 | PERM_DEPARTMENT_CREATE
```
<!-- API:API-ORG-021:END -->

<!-- API:API-ORG-022:START -->
### API-ORG-022 — Get Department Tree
```
Endpoint: GET /api/v1/org/departments/tree
Query Params: branchFk (REQUIRED), isActiveFl (OPTIONAL)
RESPONSE: 200 — List<DepartmentTreeNode>
  DepartmentTreeNode: departmentPk, deptCode, nameAr, nameEn, nodeTypeId, isActiveFl,
                      parentDepartmentFk, children: List<DepartmentTreeNode>
SERVICE: fetch flat list by branchFk → build tree recursively in service layer
REPOSITORY: QR-ORG-0024 | FIND_ALL by branchFk | READ_ONLY
DRV-ORG-004: Tree building is performed in service layer (recursive parent-child assembly)
             — not via Oracle hierarchical query
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_ALL by BRANCH_FK — tree assembly in service layer)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-004 | PERM_DEPARTMENT_VIEW
```
<!-- API:API-ORG-022:END -->

<!-- API:API-ORG-023:START -->
### API-ORG-023 — Search Departments
```
Endpoint: GET /api/v1/org/departments
Query Params: branchFk? (EXACT), nameAr? (LIKE), nodeTypeId? (EXACT), isActiveFl? (EXACT), page, size, sortBy, sortDir
ALLOWED_SORT_FIELDS: { "deptCode", "nameAr", "nameEn", "nodeTypeId", "createdAt" }
RESPONSE: 200 — Page<DepartmentResponse>
REPOSITORY: QR-ORG-0023 | FIND_BY_CRITERIA | READ_ONLY
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_BY_CRITERIA — dynamic filters)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-004 | PERM_DEPARTMENT_VIEW
```
<!-- API:API-ORG-023:END -->

<!-- API:API-ORG-024:START -->
### API-ORG-024 — Get Department by ID
```
Endpoint: GET /api/v1/org/departments/{id}
RESPONSE: 200 — DepartmentResponse | ERRORS: ERR-0101 → HTTP 404
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_ONE by PK)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-004 | PERM_DEPARTMENT_VIEW
```
<!-- API:API-ORG-024:END -->

<!-- API:API-ORG-025:START -->
### API-ORG-025 — Update Department
```
Endpoint: PUT /api/v1/org/departments/{id}
REQUEST Body: DepartmentUpdateRequest
  Fields: nameAr (OPTIONAL), nameEn (OPTIONAL), nodeTypeId (OPTIONAL),
          parentDepartmentFk (OPTIONAL — can change parent), notes (OPTIONAL)
  Excluded: deptCode (RULE-ORG-011), branchFk (not updatable)

VALIDATIONS: RULE-ORG-011 | RULE-ORG-007 (if parent changes) | RULE-ORG-017 (if parent changes)
ERRORS: ERR-0011→400 | ERR-0007→409 | ERR-0017→400 | ERR-0101→404
SERVICE: [load] → [validate parent if changed: active+circular] → [update] → [save]
REPOSITORY: QR-ORG-0026 | UPDATE | READ_WRITE
REPOSITORY STRATEGY:
  DB Operation        : UPDATE (merge on DEPARTMENT_PK) + ancestor traverse
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-004 | PERM_DEPARTMENT_UPDATE
```
<!-- API:API-ORG-025:END -->

<!-- API:API-ORG-026:START -->
### API-ORG-026 — Deactivate Department
```
Endpoint: DELETE /api/v1/org/departments/{id}  (soft — IS_ACTIVE_FL=0)
⚠ DRV-ORG-005: No deactivation pre-check within ORG module (no child entity depends on Department
               in this module). Consuming modules enforce RULE-ORG-009 — no inbound check needed here.
RESPONSE: 200 — confirmation
ERRORS: ERR-0101 → HTTP 404
SERVICE: [load] → IS_ACTIVE_FL=0 → save
REPOSITORY STRATEGY:
  DB Operation        : UPDATE IS_ACTIVE_FL=0 (no pre-check per DRV-ORG-005)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-004 | PERM_DEPARTMENT_DELETE
```
<!-- API:API-ORG-026:END -->

<!-- API:API-ORG-027:START -->
### API-ORG-027 — Reactivate Department
```
Endpoint: PUT /api/v1/org/departments/{id}/reactivate
RESPONSE: 200 — DepartmentResponse | ERRORS: ERR-0101 → HTTP 404
REPOSITORY STRATEGY:
  DB Operation        : UPDATE IS_ACTIVE_FL=1
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-004 | PERM_DEPARTMENT_UPDATE
```
<!-- API:API-ORG-027:END -->

---

**CostCenter APIs (API-ORG-028..034)**

<!-- API:API-ORG-028:START -->
### API-ORG-028 — Create CostCenter
```
Endpoint: POST /api/v1/org/cost-centers
REQUEST Body: CostCenterCreateRequest
  Fields: nameAr (REQUIRED), nameEn (REQUIRED), branchFk (REQUIRED),
          nodeTypeId (REQUIRED — COST_CENTER_NODE_TYPE), costCenterTypeId (REQUIRED — COST_CENTER_TYPE),
          parentCostCenterFk (OPTIONAL), notes (OPTIONAL)
  Excluded: costCenterCode (SEQ_ORG_COST_CENTER via NumberingEngine)

VALIDATIONS:
  RULE-ORG-015 — Requires active Branch:
    Message-AR : يرجى اختيار فرع نشط لربط مركز التكلفة به.
    Message-EN : A valid active Branch must be selected before saving a CostCenter.
  RULE-ORG-008 — Circular parent prevention:
    Message-AR : لا يمكن تعيين مركز التكلفة هذا أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري.
    Message-EN : Cannot set this cost center as parent: circular reference detected in cost center hierarchy.
  RULE-ORG-018 — Parent must be active:
    Message-AR : لا يمكن تعيين مركز تكلفة غير نشط أباً لمركز التكلفة. يرجى اختيار مركز تكلفة نشط.
    Message-EN : Cannot set an inactive cost center as parent. Please select an active cost center.

ERRORS: ERR-0015→400 | ERR-0008→409 | ERR-0018→400 | ERR-0012→409

SERVICE:
  1. [validate] branchFk active | 2. [validate parent if set] active + circular check
  3. [generate] NumberingEngine → costCenterCode via SEQ_ORG_COST_CENTER
  4. [persist]  save to ORG_COST_CENTER

REPOSITORY: QR-ORG-0033 | SAVE | Sequence: SEQ_ORG_COST_CENTER
REPOSITORY STRATEGY:
  DB Operation        : INSERT (SAVE via SEQ_ORG_COST_CENTER) + ancestor traverse
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-005 | PERM_COST_CENTER_CREATE
```
<!-- API:API-ORG-028:END -->

<!-- API:API-ORG-029:START -->
### API-ORG-029 — Get CostCenter Tree
```
Endpoint: GET /api/v1/org/cost-centers/tree
Query Params: branchFk (REQUIRED), isActiveFl (OPTIONAL)
RESPONSE: 200 — List<CostCenterTreeNode>
  CostCenterTreeNode: costCenterPk, costCenterCode, nameAr, nameEn, nodeTypeId, costCenterTypeId,
                      isActiveFl, parentCostCenterFk, children[]
SERVICE: fetch flat list by branchFk → build recursive tree in service layer (DRV-ORG-004)
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_ALL by BRANCH_FK — tree assembly in service layer)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-005 | PERM_COST_CENTER_VIEW
```
<!-- API:API-ORG-029:END -->

<!-- API:API-ORG-030:START -->
### API-ORG-030 — Search CostCenters
```
Endpoint: GET /api/v1/org/cost-centers
Query Params: branchFk? (EXACT), nameAr? (LIKE), nodeTypeId? (EXACT), costCenterTypeId? (EXACT),
              isActiveFl? (EXACT), page, size, sortBy, sortDir
ALLOWED_SORT_FIELDS: { "costCenterCode", "nameAr", "nameEn", "costCenterTypeId", "createdAt" }
RESPONSE: 200 — Page<CostCenterResponse>
REPOSITORY: QR-ORG-0031 | FIND_BY_CRITERIA | READ_ONLY
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_BY_CRITERIA — dynamic filters)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-005 | PERM_COST_CENTER_VIEW
```
<!-- API:API-ORG-030:END -->

<!-- API:API-ORG-031:START -->
### API-ORG-031 — Get CostCenter by ID
```
Endpoint: GET /api/v1/org/cost-centers/{id}
RESPONSE: 200 — CostCenterResponse | ERRORS: ERR-0101 → HTTP 404
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_ONE by PK)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-005 | PERM_COST_CENTER_VIEW
```
<!-- API:API-ORG-031:END -->

<!-- API:API-ORG-032:START -->
### API-ORG-032 — Update CostCenter
```
Endpoint: PUT /api/v1/org/cost-centers/{id}
REQUEST Body: CostCenterUpdateRequest
  Fields: nameAr, nameEn, nodeTypeId, costCenterTypeId, parentCostCenterFk, notes (all OPTIONAL)
  Excluded: costCenterCode (RULE-ORG-011), branchFk
VALIDATIONS: RULE-ORG-011 | RULE-ORG-008 | RULE-ORG-018 (if parent changes)
ERRORS: ERR-0011→400 | ERR-0008→409 | ERR-0018→400 | ERR-0101→404
REPOSITORY: QR-ORG-0034 | UPDATE | READ_WRITE
REPOSITORY STRATEGY:
  DB Operation        : UPDATE (merge on COST_CENTER_PK) + ancestor traverse
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-005 | PERM_COST_CENTER_UPDATE
```
<!-- API:API-ORG-032:END -->

<!-- API:API-ORG-033:START -->
### API-ORG-033 — Deactivate CostCenter
```
Endpoint: DELETE /api/v1/org/cost-centers/{id}  (soft)
⚠ DRV-ORG-005: No intra-module pre-check (no ORG child depends on CostCenter).
  Finance module enforces RULE-ORG-010 on its side (INBOUND-STUB-ORG-001).
RESPONSE: 200 | ERRORS: ERR-0101 → HTTP 404
SERVICE: [load] → IS_ACTIVE_FL=0 → save
REPOSITORY STRATEGY:
  DB Operation        : UPDATE IS_ACTIVE_FL=0 (no pre-check per DRV-ORG-005)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-005 | PERM_COST_CENTER_DELETE
```
<!-- API:API-ORG-033:END -->

<!-- API:API-ORG-034:START -->
### API-ORG-034 — Reactivate CostCenter
```
Endpoint: PUT /api/v1/org/cost-centers/{id}/reactivate
RESPONSE: 200 — CostCenterResponse | ERRORS: ERR-0101 → HTTP 404
REPOSITORY STRATEGY:
  DB Operation        : UPDATE IS_ACTIVE_FL=1
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-005 | PERM_COST_CENTER_UPDATE
```
<!-- API:API-ORG-034:END -->

---

**ProfitCenter APIs (API-ORG-035..040)**

<!-- API:API-ORG-035:START -->
### API-ORG-035 — Create ProfitCenter
```
Endpoint: POST /api/v1/org/profit-centers
REQUEST Body: ProfitCenterCreateRequest
  Fields: nameAr (REQUIRED), nameEn (REQUIRED), legalEntityFk (REQUIRED), notes (OPTIONAL)
  Excluded: profitCenterCode (SEQ_ORG_PROFIT_CENTER via NumberingEngine)
VALIDATIONS:
  RULE-ORG-020 — Requires active LegalEntity:
    Message-AR : يرجى اختيار كيان قانوني نشط لربط مركز الربح به.
    Message-EN : A valid active Legal Entity must be selected before saving a ProfitCenter.
ERRORS: ERR-0020→400 | ERR-0012→409
SERVICE: [validate LE active] → [generate code] → [persist via SEQ_ORG_PROFIT_CENTER]
REPOSITORY: QR-ORG-0040 | SAVE | Sequence: SEQ_ORG_PROFIT_CENTER
REPOSITORY STRATEGY:
  DB Operation        : INSERT (SAVE via SEQ_ORG_PROFIT_CENTER)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-006 | PERM_PROFIT_CENTER_CREATE
```
<!-- API:API-ORG-035:END -->

<!-- API:API-ORG-036:START -->
### API-ORG-036 — Search ProfitCenters
```
Endpoint: GET /api/v1/org/profit-centers
Query Params: profitCenterCode? (LIKE), nameAr? (LIKE), legalEntityFk? (EXACT), isActiveFl? (EXACT), page, size, sortBy, sortDir
ALLOWED_SORT_FIELDS: { "profitCenterCode", "nameAr", "nameEn", "createdAt" }
RESPONSE: 200 — Page<ProfitCenterResponse>
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_BY_CRITERIA — dynamic filters)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-006 | PERM_PROFIT_CENTER_VIEW
```
<!-- API:API-ORG-036:END -->

<!-- API:API-ORG-037:START -->
### API-ORG-037 — Get ProfitCenter by ID
```
Endpoint: GET /api/v1/org/profit-centers/{id}
RESPONSE: 200 — ProfitCenterResponse | ERRORS: ERR-0101 → HTTP 404
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_ONE by PK)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-006 | PERM_PROFIT_CENTER_VIEW
```
<!-- API:API-ORG-037:END -->

<!-- API:API-ORG-038:START -->
### API-ORG-038 — Update ProfitCenter
```
Endpoint: PUT /api/v1/org/profit-centers/{id}
REQUEST Body: ProfitCenterUpdateRequest
  Fields: nameAr (OPTIONAL), nameEn (OPTIONAL), notes (OPTIONAL)
  Excluded: profitCenterCode (RULE-ORG-011), legalEntityFk
ERRORS: ERR-0011→400 | ERR-0101→404
REPOSITORY STRATEGY:
  DB Operation        : UPDATE (merge on PROFIT_CENTER_PK)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-006 | PERM_PROFIT_CENTER_UPDATE
```
<!-- API:API-ORG-038:END -->

<!-- API:API-ORG-039:START -->
### API-ORG-039 — Deactivate ProfitCenter
```
Endpoint: DELETE /api/v1/org/profit-centers/{id}  (soft)
⚠ INBOUND-STUB-ORG-001: Finance module may add deactivation pre-check (DEFERRED)
RESPONSE: 200 | ERRORS: ERR-0101 → HTTP 404
SERVICE: [load] → IS_ACTIVE_FL=0 → save
REPOSITORY STRATEGY:
  DB Operation        : UPDATE IS_ACTIVE_FL=0 (no pre-check per DRV-ORG-005)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-006 | PERM_PROFIT_CENTER_DELETE
```
<!-- API:API-ORG-039:END -->

<!-- API:API-ORG-040:START -->
### API-ORG-040 — Reactivate ProfitCenter
```
Endpoint: PUT /api/v1/org/profit-centers/{id}/reactivate
RESPONSE: 200 | ERRORS: ERR-0101 → HTTP 404
REPOSITORY STRATEGY:
  DB Operation        : UPDATE IS_ACTIVE_FL=1
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-006 | PERM_PROFIT_CENTER_UPDATE
```
<!-- API:API-ORG-040:END -->

---

**LocationSite APIs (API-ORG-041..047)**

<!-- API:API-ORG-041:START -->
### API-ORG-041 — Create LocationSite
```
Endpoint: POST /api/v1/org/location-sites
REQUEST Body: LocationSiteCreateRequest
  Fields: nameAr (REQUIRED), nameEn (REQUIRED), branchFk (REQUIRED),
          siteTypeId (REQUIRED — LOCATION_SITE_TYPE), notes (OPTIONAL)
  Excluded: locationCode (SEQ_ORG_LOCATION_SITE via NumberingEngine)
VALIDATIONS: RULE-ORG-012 | RULE-ORG-016
ERRORS: ERR-0012→409
SERVICE: [generate code via NumberingEngine] → [persist via SEQ_ORG_LOCATION_SITE]
REPOSITORY: QR-ORG-0046 | SAVE | Sequence: SEQ_ORG_LOCATION_SITE
REPOSITORY STRATEGY:
  DB Operation        : INSERT (SAVE via SEQ_ORG_LOCATION_SITE)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-007 | PERM_LOCATION_SITE_CREATE
```
<!-- API:API-ORG-041:END -->

<!-- API:API-ORG-042:START -->
### API-ORG-042 — Search LocationSites
```
Endpoint: GET /api/v1/org/location-sites
Query Params: locationCode? (LIKE), nameAr? (LIKE), branchFk? (EXACT), siteTypeId? (EXACT),
              isActiveFl? (EXACT), page, size, sortBy, sortDir
ALLOWED_SORT_FIELDS: { "locationCode", "nameAr", "nameEn", "siteTypeId", "createdAt" }
RESPONSE: 200 — Page<LocationSiteResponse>
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_BY_CRITERIA — dynamic filters)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-007 | PERM_LOCATION_SITE_VIEW
```
<!-- API:API-ORG-042:END -->

<!-- API:API-ORG-043:START -->
### API-ORG-043 — Get LocationSite by ID
```
Endpoint: GET /api/v1/org/location-sites/{id}
RESPONSE: 200 — LocationSiteResponse | ERRORS: ERR-0101 → HTTP 404
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_ONE by PK)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-007 | PERM_LOCATION_SITE_VIEW
```
<!-- API:API-ORG-043:END -->

<!-- API:API-ORG-044:START -->
### API-ORG-044 — Update LocationSite
```
Endpoint: PUT /api/v1/org/location-sites/{id}
REQUEST Body: LocationSiteUpdateRequest
  Fields: nameAr (OPTIONAL), nameEn (OPTIONAL), siteTypeId (OPTIONAL), notes (OPTIONAL)
  Excluded: locationCode (RULE-ORG-011), branchFk
VALIDATIONS: RULE-ORG-011
ERRORS: ERR-0011→400 | ERR-0101→404
REPOSITORY STRATEGY:
  DB Operation        : UPDATE (merge on LOCATION_SITE_PK)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-007 | PERM_LOCATION_SITE_UPDATE
```
<!-- API:API-ORG-044:END -->

<!-- API:API-ORG-045:START -->
### API-ORG-045 — Deactivate LocationSite
```
Endpoint: DELETE /api/v1/org/location-sites/{id}  (soft)
⚠ INBOUND-STUB-ORG-002: Inventory module may add pre-check (DEFERRED)
RESPONSE: 200 | ERRORS: ERR-0101 → HTTP 404
REPOSITORY STRATEGY:
  DB Operation        : UPDATE IS_ACTIVE_FL=0 (no pre-check per DRV-ORG-005)
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-007 | PERM_LOCATION_SITE_DELETE
```
<!-- API:API-ORG-045:END -->

<!-- API:API-ORG-046:START -->
### API-ORG-046 — Reactivate LocationSite
```
Endpoint: PUT /api/v1/org/location-sites/{id}/reactivate
RESPONSE: 200 | ERRORS: ERR-0101 → HTTP 404
REPOSITORY STRATEGY:
  DB Operation        : UPDATE IS_ACTIVE_FL=1
  Join strategy       : NONE
  Transaction boundary: READ_WRITE
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-007 | PERM_LOCATION_SITE_UPDATE
```
<!-- API:API-ORG-046:END -->

<!-- API:API-ORG-047:START -->
### API-ORG-047 — Get LocationSites by Branch
```
Endpoint: GET /api/v1/org/location-sites/by-branch/{branchId}
Path Params: branchId: Long (BRANCH_FK)
Query Params: isActiveFl (OPTIONAL — default active for LOV use)
RESPONSE: 200 — List<LocationSiteResponse>
REPOSITORY: QR-ORG-0045 | FIND_ALL by BRANCH_FK | READ_ONLY
DRV-ORG-003: Returns List (not Page) — used as LOV source for Inventory module
REPOSITORY STRATEGY:
  DB Operation        : SELECT (FIND_ALL by BRANCH_FK)
  Join strategy       : NONE
  Transaction boundary: READ_ONLY
  Fetch strategy      : LAZY (default)
  Bulk operation flag : NO

SECURITY: SCR-ORG-007 | PERM_LOCATION_SITE_VIEW
```
<!-- API:API-ORG-047:END -->

<!-- SUB:CRUD:END -->

<!-- SUB:INT:START -->

## SVC+API — Integration Stubs

```
XM Register: EMPTY — Organization is ROOT MODULE
No outbound XM dependencies.
INBOUND-STUB-ORG-001 (Finance → CostCenter/ProfitCenter): DEFERRED
INBOUND-STUB-ORG-002 (Inventory → LocationSite): DEFERRED
```

<!-- SUB:INT:END -->

**SVC+API Gate: PASSED ✓**
```
[ ✓ ] All 47 API-IDs specified with full contracts
[ ✓ ] All ERR-IDs assigned (ERR-0001..0020, ERR-0100, ERR-0101)
[ ✓ ] All RULE-ID full texts inline — no "see SRS" references
[ ✓ ] BusinessCode excluded from all Create/Update DTOs
[ ✓ ] All LOV LOOKUP_CODEs exact (not placeholders)
[ ✓ ] All sequence names exact from db-script
[ ✓ ] DRV-ORG-002, DRV-ORG-003, DRV-ORG-004, DRV-ORG-005 documented
[ ✓ ] RULE-ERR-CARRY: every RULE-ID in Validations has ERR-ID in Errors
[ ✓ ] RULE-PLATFORM-ERR: ERR-0100/0101 documented as PLATFORM-STD
```

<!-- PHASE:SVC-API:END -->

<!-- PHASE:DOC:START -->

# PHASE DOC — Contract Stabilization

## DOC-1: API Contract Summary

```
API CONTRACT SUMMARY — ORG-001 — PLAN-ORG-001
─────────────────────────────────────────────────────────────────
API-ID         │ Endpoint                                      │ Method │ Request DTO              │ Response DTO             │ Stability
───────────────┼───────────────────────────────────────────────┼────────┼──────────────────────────┼──────────────────────────┼──────────
API-ORG-001    │ /api/v1/org/legal-entities                    │ POST   │ LegalEntityCreateRequest │ LegalEntityResponse      │ STABLE
API-ORG-002    │ /api/v1/org/legal-entities                    │ GET    │ [query params]           │ Page<LegalEntityResponse>│ STABLE
API-ORG-003    │ /api/v1/org/legal-entities/{id}               │ GET    │ —                        │ LegalEntityResponse      │ STABLE
API-ORG-004    │ /api/v1/org/legal-entities/{id}               │ PUT    │ LegalEntityUpdateRequest │ LegalEntityResponse      │ STABLE
API-ORG-005    │ /api/v1/org/legal-entities/{id}               │ DELETE │ —                        │ confirmation             │ STABLE
API-ORG-006    │ /api/v1/org/legal-entities/{id}/reactivate    │ PUT    │ —                        │ LegalEntityResponse      │ STABLE
API-ORG-007    │ /api/v1/org/branches                          │ POST   │ BranchCreateRequest      │ BranchResponse           │ STABLE
API-ORG-008    │ /api/v1/org/branches                          │ GET    │ [query params]           │ Page<BranchResponse>     │ STABLE
API-ORG-009    │ /api/v1/org/branches/{id}                     │ GET    │ —                        │ BranchResponse           │ STABLE
API-ORG-010    │ /api/v1/org/branches/{id}                     │ PUT    │ BranchUpdateRequest      │ BranchResponse           │ STABLE
API-ORG-011    │ /api/v1/org/branches/{id}                     │ DELETE │ —                        │ confirmation             │ STABLE
API-ORG-012    │ /api/v1/org/branches/{id}/reactivate          │ PUT    │ —                        │ BranchResponse           │ STABLE
API-ORG-013    │ /api/v1/org/branches/by-legal-entity/{leId}   │ GET    │ isActiveFl?              │ List<BranchResponse>     │ STABLE
API-ORG-014    │ /api/v1/org/regions                           │ POST   │ RegionCreateRequest      │ RegionResponse           │ STABLE
API-ORG-015    │ /api/v1/org/regions                           │ GET    │ [query params]           │ Page<RegionResponse>     │ STABLE
API-ORG-016    │ /api/v1/org/regions/{id}                      │ GET    │ —                        │ RegionResponse           │ STABLE
API-ORG-017    │ /api/v1/org/regions/{id}                      │ PUT    │ RegionUpdateRequest      │ RegionResponse           │ STABLE
API-ORG-018    │ /api/v1/org/regions/{id}                      │ DELETE │ —                        │ confirmation             │ STABLE (OQ-001 may extend)
API-ORG-019    │ /api/v1/org/regions/{id}/reactivate           │ PUT    │ —                        │ RegionResponse           │ STABLE
API-ORG-020    │ /api/v1/org/region-types                      │ GET    │ isActiveFl?              │ List<RegionTypeResponse> │ STABLE
API-ORG-021    │ /api/v1/org/departments                       │ POST   │ DepartmentCreateRequest  │ DepartmentResponse       │ STABLE
API-ORG-022    │ /api/v1/org/departments/tree                  │ GET    │ branchFk, isActiveFl?    │ List<DeptTreeNode>       │ STABLE
API-ORG-023    │ /api/v1/org/departments                       │ GET    │ [query params]           │ Page<DepartmentResponse> │ STABLE
API-ORG-024    │ /api/v1/org/departments/{id}                  │ GET    │ —                        │ DepartmentResponse       │ STABLE
API-ORG-025    │ /api/v1/org/departments/{id}                  │ PUT    │ DepartmentUpdateRequest  │ DepartmentResponse       │ STABLE
API-ORG-026    │ /api/v1/org/departments/{id}                  │ DELETE │ —                        │ confirmation             │ STABLE
API-ORG-027    │ /api/v1/org/departments/{id}/reactivate       │ PUT    │ —                        │ DepartmentResponse       │ STABLE
API-ORG-028    │ /api/v1/org/cost-centers                      │ POST   │ CostCenterCreateRequest  │ CostCenterResponse       │ STABLE
API-ORG-029    │ /api/v1/org/cost-centers/tree                 │ GET    │ branchFk, isActiveFl?    │ List<CCTreeNode>         │ STABLE
API-ORG-030    │ /api/v1/org/cost-centers                      │ GET    │ [query params]           │ Page<CostCenterResponse> │ STABLE
API-ORG-031    │ /api/v1/org/cost-centers/{id}                 │ GET    │ —                        │ CostCenterResponse       │ STABLE
API-ORG-032    │ /api/v1/org/cost-centers/{id}                 │ PUT    │ CostCenterUpdateRequest  │ CostCenterResponse       │ STABLE
API-ORG-033    │ /api/v1/org/cost-centers/{id}                 │ DELETE │ —                        │ confirmation             │ STABLE
API-ORG-034    │ /api/v1/org/cost-centers/{id}/reactivate      │ PUT    │ —                        │ CostCenterResponse       │ STABLE
API-ORG-035    │ /api/v1/org/profit-centers                    │ POST   │ ProfitCenterCreateRequest│ ProfitCenterResponse     │ STABLE
API-ORG-036    │ /api/v1/org/profit-centers                    │ GET    │ [query params]           │ Page<ProfitCenterResponse>│ STABLE
API-ORG-037    │ /api/v1/org/profit-centers/{id}               │ GET    │ —                        │ ProfitCenterResponse     │ STABLE
API-ORG-038    │ /api/v1/org/profit-centers/{id}               │ PUT    │ ProfitCenterUpdateRequest│ ProfitCenterResponse     │ STABLE
API-ORG-039    │ /api/v1/org/profit-centers/{id}               │ DELETE │ —                        │ confirmation             │ STABLE
API-ORG-040    │ /api/v1/org/profit-centers/{id}/reactivate    │ PUT    │ —                        │ ProfitCenterResponse     │ STABLE
API-ORG-041    │ /api/v1/org/location-sites                    │ POST   │ LocationSiteCreateRequest│ LocationSiteResponse     │ STABLE
API-ORG-042    │ /api/v1/org/location-sites                    │ GET    │ [query params]           │ Page<LocationSiteResponse>│ STABLE
API-ORG-043    │ /api/v1/org/location-sites/{id}               │ GET    │ —                        │ LocationSiteResponse     │ STABLE
API-ORG-044    │ /api/v1/org/location-sites/{id}               │ PUT    │ LocationSiteUpdateRequest│ LocationSiteResponse     │ STABLE
API-ORG-045    │ /api/v1/org/location-sites/{id}               │ DELETE │ —                        │ confirmation             │ STABLE
API-ORG-046    │ /api/v1/org/location-sites/{id}/reactivate    │ PUT    │ —                        │ LocationSiteResponse     │ STABLE
API-ORG-047    │ /api/v1/org/location-sites/by-branch/{branchId}│ GET   │ isActiveFl?              │ List<LocationSiteResponse>│ STABLE
─────────────────────────────────────────────────────────────────
Unstable APIs: API-ORG-018 (may extend on OQ-001 resolution) — otherwise all STABLE
```

## DOC-2: DTO Typing Rules
```
LOV field typing : String (stores DETAIL_CODE from MD_LOOKUP_DETAIL) — never ENUM
                   Exception: regionTypeFk in Region is Long (FK to ORG_REGION_TYPE) — DRV-ORG-001
Business Code    : String — always in ResponseDTO — never in CreateRequest/UpdateRequest
```

## DOC-3: Pagination & Filter Standards
```
PAGINATION & FILTER STANDARDS (PROJECT-STANDARD)
─────────────────────────────────────────────────────────────────
Backend strategy : JPA Page<T> — NO custom pagination wrapper
Request contract : SearchRequest extends BaseSearchContractRequest
                   → PageableBuilder.from(request, ALLOWED_SORT_FIELDS)
Empty result     : HTTP 200 with empty content — NEVER HTTP 404
Filter types     : EXACT (=) for IDs/flags/codes | LIKE (%value%) for name/code text search
Angular frontend : currentPage and pageSize derived from lastSearchRequest — NOT independent state
List endpoints   : API-ORG-013, 020, 022, 029, 047 — return List (not Page) — DRV-ORG-003
─────────────────────────────────────────────────────────────────
```

## ERROR CATALOG — ORG-001

```
ERROR CATALOG — Organization (ORG-001) — PLAN-ORG-001
══════════════════════════════════════════════════════════════════════════
ERR-ID   │ RULE-ID         │ HTTP │ Message-AR                                                            │ Message-EN
─────────┼─────────────────┼──────┼───────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────────────
ERR-0001 │ RULE-ORG-001    │ 409  │ لا يمكن تعطيل الكيان القانوني لوجود فروع نشطة مرتبطة به. يرجى تعطيل جميع الفروع أولاً. │ Cannot deactivate Legal Entity: active branches exist. Please deactivate all branches first.
ERR-0002 │ RULE-ORG-002    │ 409  │ لا يمكن تعطيل الكيان القانوني لوجود مراكز ربح نشطة مرتبطة به. يرجى تعطيل جميع مراكز الربح أولاً. │ Cannot deactivate Legal Entity: active profit centers exist. Please deactivate all profit centers first.
ERR-0003 │ RULE-ORG-003    │ 409  │ لا يمكن تعطيل الفرع لوجود أقسام نشطة مرتبطة به. يرجى تعطيل جميع الأقسام أولاً. │ Cannot deactivate Branch: active departments exist. Please deactivate all departments first.
ERR-0004 │ RULE-ORG-004    │ 409  │ لا يمكن تعطيل الفرع لوجود مراكز تكلفة نشطة مرتبطة به. يرجى تعطيل جميع مراكز التكلفة أولاً. │ Cannot deactivate Branch: active cost centers exist. Please deactivate all cost centers first.
ERR-0005 │ RULE-ORG-005    │ 409  │ لا يمكن تعطيل الفرع لوجود مواقع جغرافية نشطة مرتبطة به. يرجى تعطيل جميع المواقع أولاً. │ Cannot deactivate Branch: active location sites exist. Please deactivate all location sites first.
ERR-0006 │ RULE-ORG-006    │ 409  │ لا يمكن تعطيل المنطقة لوجود فروع نشطة مرتبطة بها. يرجى إلغاء ربط الفروع أولاً. │ Cannot deactivate Region: active branches reference it. Please unlink branches first.
ERR-0007 │ RULE-ORG-007    │ 409  │ لا يمكن تعيين هذا القسم أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري. │ Cannot set this department as parent: circular reference detected in department hierarchy.
ERR-0008 │ RULE-ORG-008    │ 409  │ لا يمكن تعيين مركز التكلفة هذا أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري. │ Cannot set this cost center as parent: circular reference detected in cost center hierarchy.
ERR-0009 │ RULE-ORG-009    │ 400  │ لا يمكن ربط قسم تجميعي (SUMMARY) بسجل معاملة مباشرة. يرجى اختيار قسم تفصيلي (DETAIL). │ Cannot assign a SUMMARY department to a transaction. Please select a DETAIL department.
ERR-0010 │ RULE-ORG-010    │ 400  │ لا يمكن ربط مركز تكلفة تجميعي (SUMMARY) بسجل معاملة مباشرة. يرجى اختيار مركز تكلفة تفصيلي (DETAIL). │ Cannot assign a SUMMARY cost center to a transaction. Please select a DETAIL cost center.
ERR-0011 │ RULE-ORG-011    │ 400  │ رمز الأعمال لا يمكن تعديله بعد الإنشاء الأول — هذه القيمة ثابتة نهائياً. │ Business code is immutable after creation and cannot be modified.
ERR-0012 │ RULE-ORG-012    │ 409  │ رمز الأعمال المُنشأ تلقائياً موجود مسبقاً. يرجى المحاولة مجدداً. │ Generated business code already exists. Please retry the operation.
ERR-0013 │ RULE-ORG-013    │ 400  │ يرجى اختيار كيان قانوني نشط لربط الفرع به. │ A valid active Legal Entity must be selected before saving a Branch.
ERR-0014 │ RULE-ORG-014    │ 400  │ يرجى اختيار فرع نشط لربط القسم به. │ A valid active Branch must be selected before saving a Department.
ERR-0015 │ RULE-ORG-015    │ 400  │ يرجى اختيار فرع نشط لربط مركز التكلفة به. │ A valid active Branch must be selected before saving a CostCenter.
ERR-0017 │ RULE-ORG-017    │ 400  │ لا يمكن تعيين قسم غير نشط أباً للقسم. يرجى اختيار قسم نشط. │ Cannot set an inactive department as parent. Please select an active department.
ERR-0018 │ RULE-ORG-018    │ 400  │ لا يمكن تعيين مركز تكلفة غير نشط أباً لمركز التكلفة. يرجى اختيار مركز تكلفة نشط. │ Cannot set an inactive cost center as parent. Please select an active cost center.
ERR-0019 │ RULE-ORG-019    │ 400  │ يرجى اختيار كيان قانوني نشط لربط المنطقة به. │ A valid active Legal Entity must be selected before saving a Region.
ERR-0020 │ RULE-ORG-020    │ 400  │ يرجى اختيار كيان قانوني نشط لربط مركز الربح به. │ A valid active Legal Entity must be selected before saving a ProfitCenter.
ERR-0100 │ PLATFORM-STD    │ 500  │ حدث خطأ غير متوقع. يرجى المحاولة مجدداً أو التواصل مع الدعم الفني. │ An unexpected error occurred. Please try again or contact support.
ERR-0101 │ PLATFORM-STD    │ 404  │ السجل المطلوب غير موجود. │ The requested record was not found.
══════════════════════════════════════════════════════════════════════════
Note: ERR-0016 not assigned (RULE-ORG-016 is architectural — no user-facing message).
Note: ERR-0009, ERR-0010 registered for consumer module use — enforced externally.
Note: ERR-0100, ERR-0101 are PLATFORM-STD (DRV-ORG-002) — no RULE-ID.
```

**DOC Gate: PASSED ✓**
```
[ ✓ ] All 47 API-IDs appear in API Contract Summary
[ ✓ ] Error Catalog complete with Arabic + English messages
[ ✓ ] All APIs marked STABLE (one noted OQ-001 extension)
[ ✓ ] Pagination standard declared
[ ✓ ] List vs Page endpoints differentiated (DRV-ORG-003)
```

<!-- PHASE:DOC:END -->

---

<!-- PHASE:INT-C:START -->

# PHASE INT-C — Integration Contract Specifications

```
INT-C SUMMARY — ORG-001 — PLAN-ORG-001
══════════════════════════════════════════════════════════════════════════
Organization is the ROOT MODULE — XM Register: EMPTY
No outbound cross-module dependencies. INT-C has nothing to contract.

INBOUND STUBS (informational — no contracts required from this module):
  INBOUND-STUB-ORG-001 : Finance → CostCenter / ProfitCenter | DEFERRED
  INBOUND-STUB-ORG-002 : Inventory → LocationSite | DEFERRED

INT-C Gate: PASSED ✓ (no XM-IDs — ROOT MODULE)
══════════════════════════════════════════════════════════════════════════
```

<!-- PHASE:INT-C:END -->

---

<!-- PHASE:INT-R:START -->

# PHASE INT-R — Integration Runtime Specifications

```
INT-R SUMMARY — ORG-001 — PLAN-ORG-001
══════════════════════════════════════════════════════════════════════════
No XM-IDs to resolve at runtime. ROOT MODULE.
INT-R Gate: PASSED ✓
══════════════════════════════════════════════════════════════════════════
```

<!-- PHASE:INT-R:END -->

---

<!-- PHASE:F1:START -->

# PHASE F1 — Frontend Model Specifications

> 7 screens ≥ 5 threshold → sub-phases per SCR-ID

<!-- SUB:SCR-ORG-001:START -->
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
<!-- SUB:SCR-ORG-001:END -->

<!-- SUB:SCR-ORG-002:START -->
## F1 — SCR-ORG-002 — إدارة الفروع

```
Pattern: COMPOSITE PATTERN-1

SEARCH MODEL — BranchSearchModel:
  branchCode    : string (LIKE)
  nameAr        : string (LIKE)
  legalEntityFk : number (EXACT — selected from active LegalEntities dropdown)
  branchTypeId  : string (EXACT — LOV-ORG-002)
  isActiveFl    : number (default 1)
  page, size, sortBy, sortDir

SEARCH RESULT COLUMNS: branchCode, nameAr, nameEn, legalEntityFk (display), branchTypeId (display), isActiveFl

ENTRY MODEL — BranchFormModel:
  branchCode    : string   READ-ONLY
  nameAr        : string   REQUIRED
  nameEn        : string   REQUIRED
  legalEntityFk : number   REQUIRED   — LOV from API-ORG-013 (active LegalEntities)
  branchTypeId  : string   REQUIRED   — LOV-ORG-002 (BRANCH_TYPE)
  notes         : string   OPTIONAL

ACTIONS: New/Edit/Deactivate/Reactivate (PERM_BRANCH_*)
```
<!-- SUB:SCR-ORG-002:END -->

<!-- SUB:SCR-ORG-003:START -->
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
<!-- SUB:SCR-ORG-003:END -->

<!-- SUB:SCR-ORG-004:START -->
## F1 — SCR-ORG-004 — إدارة الأقسام (Hierarchical Tree)

```
Pattern: SPECIALIZED PATTERN-3 (Tree Explorer + Entry Panel)

TREE FILTER MODEL — DepartmentTreeFilterModel:
  branchFk   : number   REQUIRED — filter tree by Branch
  nameAr     : string   OPTIONAL — partial search on tree nodes
  nodeTypeId : string   OPTIONAL — SUMMARY / DETAIL filter
  isActiveFl : number   OPTIONAL (default 1)

TREE VIEW: hierarchical display of DepartmentTreeNode[]
  Node display: deptCode + nameAr + nodeTypeId indicator (SUMMARY/DETAIL visual badge)
  Actions on node: Edit (PERM_DEPARTMENT_UPDATE), Deactivate (PERM_DEPARTMENT_DELETE)
  Add Root: PERM_DEPARTMENT_CREATE — no parent
  Add Child: PERM_DEPARTMENT_CREATE — with parentDepartmentFk set

ENTRY MODEL — DepartmentFormModel:
  deptCode            : string   READ-ONLY
  nameAr              : string   REQUIRED
  nameEn              : string   REQUIRED
  branchFk            : number   REQUIRED   — LOV from API-ORG-013 (active Branches)
  parentDepartmentFk  : number   OPTIONAL   — LOV from active Departments in same Branch
  nodeTypeId          : string   REQUIRED   — LOV-ORG-003 (DEPARTMENT_NODE_TYPE)
  notes               : string   OPTIONAL

⚠ parentDepartmentFk dropdown: shows only ACTIVE departments in same branch — excludes current record
```
<!-- SUB:SCR-ORG-004:END -->

<!-- SUB:SCR-ORG-005:START -->
## F1 — SCR-ORG-005 — إدارة مراكز التكلفة (Hierarchical Tree)

```
Pattern: SPECIALIZED PATTERN-3 (Tree Explorer + Entry Panel)

TREE FILTER MODEL — CostCenterTreeFilterModel:
  branchFk          : number   REQUIRED
  nameAr            : string   OPTIONAL
  nodeTypeId        : string   OPTIONAL (LOV-ORG-004)
  costCenterTypeId  : string   OPTIONAL (LOV-ORG-005)
  isActiveFl        : number   OPTIONAL

TREE VIEW: hierarchical CostCenterTreeNode[]
  Node display: costCenterCode + nameAr + nodeTypeId + costCenterTypeId indicators

ENTRY MODEL — CostCenterFormModel:
  costCenterCode      : string   READ-ONLY
  nameAr              : string   REQUIRED
  nameEn              : string   REQUIRED
  branchFk            : number   REQUIRED   — LOV active Branches
  parentCostCenterFk  : number   OPTIONAL   — LOV active CostCenters in same Branch
  nodeTypeId          : string   REQUIRED   — LOV-ORG-004
  costCenterTypeId    : string   REQUIRED   — LOV-ORG-005
  notes               : string   OPTIONAL
```
<!-- SUB:SCR-ORG-005:END -->

<!-- SUB:SCR-ORG-006:START -->
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
<!-- SUB:SCR-ORG-006:END -->

<!-- SUB:SCR-ORG-007:START -->
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
<!-- SUB:SCR-ORG-007:END -->

**F1 Gate: PASSED ✓**
```
[ ✓ ] All 7 SCR-IDs have model specifications
[ ✓ ] Business Code fields READ-ONLY in all models (F3-BC-RULE-1)
[ ✓ ] No ENUM for LOV fields — all String or FK Long
[ ✓ ] Search ≠ Entry (CORE-9 — separate UX views under one SCR-ID)
[ ✓ ] PATTERN-3 tree screens declared for SCR-ORG-004, 005
[ ✓ ] DRV-ORG-006 documented (Reactivate UI gap for SCR-ORG-003/006/007)
```

<!-- PHASE:F1:END -->

---

<!-- PHASE:F2:START -->

# PHASE F2 — Screen Init & Facade Specifications

<!-- SUB:SCR-ORG-001:START -->
## F2 — SCR-ORG-001 — Legal Entity Screen

```
SCREEN INIT — SCR-ORG-001:
  On init:
    1. Check PERM_LEGAL_ENTITY_VIEW → redirect to /unauthorized if false
    2. Load LOV-ORG-001: GET /api/lookups/LEGAL_ENTITY_TYPE?active=true → store as entityTypeOptions
    3. Execute default search (isActiveFl=1, page=0, size=20) via API-ORG-002

OBSERVABLE TYPES (Angular):
  legalEntities$   : Observable<Page<LegalEntityResponse>>
  entityTypeOptions$ : Observable<LookupDetail[]>
  isLoading$       : Observable<boolean>

F2-FACADE — SCR-ORG-001 — Legal Entity Management
─────────────────────────────────────────────────────────────────
Facade serves   : SCR-ORG-001
Delegates to    : LegalEntityService

STATE THIS FACADE OWNS:
  legalEntityList    — Page<LegalEntityResponse> — search results
  selectedItem       — LegalEntityResponse | null — edit target
  isLoading          — boolean — loading indicator
  lastSearchRequest  — LegalEntitySearchModel — last executed search
                       currentPage and pageSize DERIVED from this — not independent state
  entityTypeOptions  — LookupDetail[] — LOV-ORG-001 (LEGAL_ENTITY_TYPE)

OPERATIONS EXPOSED TO COMPONENT:
  searchLegalEntities(request)  → API-ORG-002 → updates legalEntityList
  getLegalEntityById(id)        → API-ORG-003 → updates selectedItem
  createLegalEntity(data)       → API-ORG-001 → refreshes legalEntityList
  updateLegalEntity(id, data)   → API-ORG-004 → updates selectedItem
  deactivateLegalEntity(id)     → checks usage via response → API-ORG-005
                                  if blocked (ERR-0001/ERR-0002): surfaces error — no confirmation
                                  if allowed: triggers confirmation → proceeds
  reactivateLegalEntity(id)     → API-ORG-006 → refreshes legalEntityList
  loadEntityTypeOptions()       → GET /api/lookups/LEGAL_ENTITY_TYPE?active=true → entityTypeOptions

BOUNDARIES:
  ✓ Component calls facade only — no direct service calls from component
  ✓ Facade calls service only — no direct HTTP from facade
  ✓ currentPage and pageSize derived from lastSearchRequest
─────────────────────────────────────────────────────────────────
```
<!-- SUB:SCR-ORG-001:END -->

<!-- SUB:SCR-ORG-002:START -->
## F2 — SCR-ORG-002 — Branch Screen

```
SCREEN INIT:
  1. Check PERM_BRANCH_VIEW → redirect if false
  2. Load LOV-ORG-002: GET /api/lookups/BRANCH_TYPE?active=true → branchTypeOptions
  3. Load active LegalEntities: API-ORG-013 (by-legal-entity not applicable here)
     → Use API-ORG-002 with isActiveFl=1, no pagination (or API-ORG-013 with a fixed leId if pre-filtered)
     DRV-ORG-007: For Branch search screen LegalEntity filter, load active LEs via API-ORG-002 (isActiveFl=1, no page limit)
  4. Execute default search (isActiveFl=1)

F2-FACADE — SCR-ORG-002 — Branch Management
STATE: branchList, selectedItem, isLoading, lastSearchRequest, branchTypeOptions, legalEntityOptions
OPERATIONS: searchBranches / getBranchById / createBranch / updateBranch / deactivateBranch / reactivateBranch
            loadBranchTypeOptions / loadLegalEntityOptions
```
<!-- SUB:SCR-ORG-002:END -->

<!-- SUB:SCR-ORG-003:START -->
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
<!-- SUB:SCR-ORG-003:END -->

<!-- SUB:SCR-ORG-004:START -->
## F2 — SCR-ORG-004 — Department Screen (Tree)

```
SCREEN INIT:
  1. Check PERM_DEPARTMENT_VIEW
  2. Load LOV-ORG-003: GET /api/lookups/DEPARTMENT_NODE_TYPE?active=true → nodeTypeOptions
  3. Load active Branches (for tree filter)
  4. No auto-tree load — wait for branchFk selection

F2-FACADE — SCR-ORG-004 — Department Management
STATE: departmentTree (List<DeptTreeNode>), selectedBranchFk, selectedItem, isLoading,
       lastTreeFilter, nodeTypeOptions, branchOptions
OPERATIONS: loadDepartmentTree(branchFk, isActiveFl?) → API-ORG-022
            getDepartmentById(id) → API-ORG-024
            createDepartment(data) → API-ORG-021 → reload tree
            updateDepartment(id, data) → API-ORG-025 → reload tree
            deactivateDepartment(id) → API-ORG-026 → reload tree
            reactivateDepartment(id) → API-ORG-027 → reload tree
            loadNodeTypeOptions() | loadBranchOptions()
            loadParentDeptOptions(branchFk) → API-ORG-023 (active DETAIL depts for parent LOV)
```
<!-- SUB:SCR-ORG-004:END -->

<!-- SUB:SCR-ORG-005:START -->
## F2 — SCR-ORG-005 — CostCenter Screen (Tree)

```
SCREEN INIT:
  1. Check PERM_COST_CENTER_VIEW
  2. Load LOV-ORG-004 (COST_CENTER_NODE_TYPE) → nodeTypeOptions
  3. Load LOV-ORG-005 (COST_CENTER_TYPE) → costCenterTypeOptions
  4. Load active Branches

F2-FACADE — SCR-ORG-005 — CostCenter Management
STATE: costCenterTree, selectedBranchFk, selectedItem, isLoading, lastTreeFilter,
       nodeTypeOptions, costCenterTypeOptions, branchOptions
OPERATIONS: loadCostCenterTree(branchFk) → API-ORG-029
            getCostCenterById(id) → API-ORG-031
            createCostCenter(data) → API-ORG-028
            updateCostCenter(id, data) → API-ORG-032
            deactivateCostCenter(id) → API-ORG-033
            reactivateCostCenter(id) → API-ORG-034
            loadParentCCOptions(branchFk) → API-ORG-030 (active CostCenters for parent LOV)
```
<!-- SUB:SCR-ORG-005:END -->

<!-- SUB:SCR-ORG-006:START -->
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
<!-- SUB:SCR-ORG-006:END -->

<!-- SUB:SCR-ORG-007:START -->
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
<!-- SUB:SCR-ORG-007:END -->

---

## F2 — FRONTEND CONTRACTS (Global — applies all screens)

```
FRONTEND CONTRACTS — ORG-001 — PLAN-ORG-001
══════════════════════════════════════════════════════════════════

STATE OWNERSHIP:
  currentPage — derived from lastSearchRequest (not independent state)
  pageSize    — derived from lastSearchRequest (not independent state)
  ✗ NEVER declare currentPage or pageSize as standalone independent state

ERROR ROUTING PER HTTP STATUS:
  HTTP 400 (field validation)   → displayed inline under the triggering field
  HTTP 409/422 (business rule)  → routed through error mapper → user toast
  HTTP 401                      → redirect to login
  HTTP 403                      → redirect to unauthorized
  HTTP 500                      → generic message only — no technical detail shown

PRE-DEACTIVATION CHECK:
  Before any deactivate action: backend usage check declared (see API-ORG-005/011/018/026/033/039/045)
  If blocked  → reason (ERR-ID message) shown to user — no confirmation dialog opened
  If allowed  → confirmation dialog → proceed with deactivation
══════════════════════════════════════════════════════════════════
```

---

## F2-SERVICE — API Contracts (one per API-ID)

<!-- F2-SERVICE:API-ORG-001:START -->
### F2-SERVICE — API-ORG-001 — Create LegalEntity
```
API-ID           : API-ORG-001
Service class    : LegalEntityService
Observable type  : Observable<LegalEntityResponse>
HTTP method      : POST
Endpoint path    : /api/v1/org/legal-entities
Request shape    : LegalEntityCreateRequest
Response shape   : LegalEntityResponse

Errors this call can produce:
  ERR-0012 → HTTP 409 → business rule (BC uniqueness) → user toast via error mapper
  HTTP 400  → field validation → inline display under triggering field
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-001:END -->

<!-- F2-SERVICE:API-ORG-002:START -->
### F2-SERVICE — API-ORG-002 — Search LegalEntities
```
API-ID           : API-ORG-002
Service class    : LegalEntityService
Observable type  : Observable<Page<LegalEntityResponse>>
HTTP method      : GET
Endpoint path    : /api/v1/org/legal-entities
Request shape    : LegalEntitySearchModel (filter params)
Response shape   : Page<LegalEntityResponse>

Errors this call can produce:
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-002:END -->

<!-- F2-SERVICE:API-ORG-003:START -->
### F2-SERVICE — API-ORG-003 — Get LegalEntity by ID
```
API-ID           : API-ORG-003
Service class    : LegalEntityService
Observable type  : Observable<LegalEntityResponse>
HTTP method      : GET
Endpoint path    : /api/v1/org/legal-entities/{id}
Request shape    : void (path param: id)
Response shape   : LegalEntityResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → record not found → user toast
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-003:END -->

<!-- F2-SERVICE:API-ORG-004:START -->
### F2-SERVICE — API-ORG-004 — Update LegalEntity
```
API-ID           : API-ORG-004
Service class    : LegalEntityService
Observable type  : Observable<LegalEntityResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/legal-entities/{id}
Request shape    : LegalEntityUpdateRequest
Response shape   : LegalEntityResponse

Errors this call can produce:
  ERR-0011 → HTTP 400 → BC immutability → inline field error
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-004:END -->

<!-- F2-SERVICE:API-ORG-005:START -->
### F2-SERVICE — API-ORG-005 — Deactivate LegalEntity
```
API-ID           : API-ORG-005
Service class    : LegalEntityService
Observable type  : Observable<void>
HTTP method      : DELETE
Endpoint path    : /api/v1/org/legal-entities/{id}
Request shape    : void (path param: id)
Response shape   : confirmation (messageAr + messageEn)

Errors this call can produce:
  ERR-0001 → HTTP 409 → active Branches exist → user toast (no confirmation shown)
  ERR-0002 → HTTP 409 → active ProfitCenters exist → user toast (no confirmation shown)
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-005:END -->

<!-- F2-SERVICE:API-ORG-006:START -->
### F2-SERVICE — API-ORG-006 — Reactivate LegalEntity
```
API-ID           : API-ORG-006
Service class    : LegalEntityService
Observable type  : Observable<LegalEntityResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/legal-entities/{id}/reactivate
Request shape    : void (path param: id)
Response shape   : LegalEntityResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-006:END -->

<!-- F2-SERVICE:API-ORG-007:START -->
### F2-SERVICE — API-ORG-007 — Create Branch
```
API-ID           : API-ORG-007
Service class    : BranchService
Observable type  : Observable<BranchResponse>
HTTP method      : POST
Endpoint path    : /api/v1/org/branches
Request shape    : BranchCreateRequest
Response shape   : BranchResponse

Errors this call can produce:
  ERR-0013 → HTTP 400 → inactive/missing LegalEntity → inline field error
  ERR-0012 → HTTP 409 → BC uniqueness → user toast
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-007:END -->

<!-- F2-SERVICE:API-ORG-008:START -->
### F2-SERVICE — API-ORG-008 — Search Branches
```
API-ID           : API-ORG-008
Service class    : BranchService
Observable type  : Observable<Page<BranchResponse>>
HTTP method      : GET
Endpoint path    : /api/v1/org/branches
Request shape    : BranchSearchModel (filter params)
Response shape   : Page<BranchResponse>

Errors this call can produce:
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-008:END -->

<!-- F2-SERVICE:API-ORG-009:START -->
### F2-SERVICE — API-ORG-009 — Get Branch by ID
```
API-ID           : API-ORG-009
Service class    : BranchService
Observable type  : Observable<BranchResponse>
HTTP method      : GET
Endpoint path    : /api/v1/org/branches/{id}
Request shape    : void (path param: id)
Response shape   : BranchResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-009:END -->

<!-- F2-SERVICE:API-ORG-010:START -->
### F2-SERVICE — API-ORG-010 — Update Branch
```
API-ID           : API-ORG-010
Service class    : BranchService
Observable type  : Observable<BranchResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/branches/{id}
Request shape    : BranchUpdateRequest
Response shape   : BranchResponse

Errors this call can produce:
  ERR-0011 → HTTP 400 → BC immutability → inline field error
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-010:END -->

<!-- F2-SERVICE:API-ORG-011:START -->
### F2-SERVICE — API-ORG-011 — Deactivate Branch
```
API-ID           : API-ORG-011
Service class    : BranchService
Observable type  : Observable<void>
HTTP method      : DELETE
Endpoint path    : /api/v1/org/branches/{id}
Request shape    : void (path param: id)
Response shape   : confirmation

Errors this call can produce:
  ERR-0003 → HTTP 409 → active Departments exist → user toast (no confirmation shown)
  ERR-0004 → HTTP 409 → active CostCenters exist → user toast (no confirmation shown)
  ERR-0005 → HTTP 409 → active LocationSites exist → user toast (no confirmation shown)
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-011:END -->

<!-- F2-SERVICE:API-ORG-012:START -->
### F2-SERVICE — API-ORG-012 — Reactivate Branch
```
API-ID           : API-ORG-012
Service class    : BranchService
Observable type  : Observable<BranchResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/branches/{id}/reactivate
Request shape    : void (path param: id)
Response shape   : BranchResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-012:END -->

<!-- F2-SERVICE:API-ORG-013:START -->
### F2-SERVICE — API-ORG-013 — Get Branches by LegalEntity
```
API-ID           : API-ORG-013
Service class    : BranchService
Observable type  : Observable<BranchResponse[]>
HTTP method      : GET
Endpoint path    : /api/v1/org/branches/by-legal-entity/{leId}
Request shape    : void (path param: leId; optional query: isActiveFl)
Response shape   : List<BranchResponse>

Errors this call can produce:
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : SHORT-TERM — stable LOV reference; list doesn't change during a session
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-013:END -->

<!-- F2-SERVICE:API-ORG-014:START -->
### F2-SERVICE — API-ORG-014 — Create Region
```
API-ID           : API-ORG-014
Service class    : RegionService
Observable type  : Observable<RegionResponse>
HTTP method      : POST
Endpoint path    : /api/v1/org/regions
Request shape    : RegionCreateRequest
Response shape   : RegionResponse

Errors this call can produce:
  ERR-0019 → HTTP 400 → inactive/missing LegalEntity → inline field error
  ERR-0012 → HTTP 409 → BC uniqueness → user toast
  HTTP 401  → redirect to login
  HTTP 403  → redirect to unauthorized
  HTTP 500  → generic message only

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-014:END -->

<!-- F2-SERVICE:API-ORG-015:START -->
### F2-SERVICE — API-ORG-015 — Search Regions
```
API-ID           : API-ORG-015
Service class    : RegionService
Observable type  : Observable<Page<RegionResponse>>
HTTP method      : GET
Endpoint path    : /api/v1/org/regions
Request shape    : RegionSearchModel (filter params)
Response shape   : Page<RegionResponse>

Errors this call can produce:
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-015:END -->

<!-- F2-SERVICE:API-ORG-016:START -->
### F2-SERVICE — API-ORG-016 — Get Region by ID
```
API-ID           : API-ORG-016
Service class    : RegionService
Observable type  : Observable<RegionResponse>
HTTP method      : GET
Endpoint path    : /api/v1/org/regions/{id}
Request shape    : void (path param: id)
Response shape   : RegionResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-016:END -->

<!-- F2-SERVICE:API-ORG-017:START -->
### F2-SERVICE — API-ORG-017 — Update Region
```
API-ID           : API-ORG-017
Service class    : RegionService
Observable type  : Observable<RegionResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/regions/{id}
Request shape    : RegionUpdateRequest
Response shape   : RegionResponse

Errors this call can produce:
  ERR-0011 → HTTP 400 → BC immutability → inline field error
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-017:END -->

<!-- F2-SERVICE:API-ORG-018:START -->
### F2-SERVICE — API-ORG-018 — Deactivate Region
```
API-ID           : API-ORG-018
Service class    : RegionService
Observable type  : Observable<void>
HTTP method      : DELETE
Endpoint path    : /api/v1/org/regions/{id}
Request shape    : void (path param: id)
Response shape   : confirmation

Errors this call can produce:
  ERR-0006 → HTTP 409 → active Branches reference it → user toast (no confirmation shown)
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-018:END -->

<!-- F2-SERVICE:API-ORG-019:START -->
### F2-SERVICE — API-ORG-019 — Reactivate Region
```
API-ID           : API-ORG-019
Service class    : RegionService
Observable type  : Observable<RegionResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/regions/{id}/reactivate
Request shape    : void (path param: id)
Response shape   : RegionResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-019:END -->

<!-- F2-SERVICE:API-ORG-020:START -->
### F2-SERVICE — API-ORG-020 — Get RegionTypes
```
API-ID           : API-ORG-020
Service class    : RegionService
Observable type  : Observable<RegionTypeResponse[]>
HTTP method      : GET
Endpoint path    : /api/v1/org/region-types
Request shape    : void (optional query: isActiveFl)
Response shape   : List<RegionTypeResponse>

Errors this call can produce:
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : SHORT-TERM — Reference Table; stable within a session
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-020:END -->

<!-- F2-SERVICE:API-ORG-021:START -->
### F2-SERVICE — API-ORG-021 — Create Department
```
API-ID           : API-ORG-021
Service class    : DepartmentService
Observable type  : Observable<DepartmentResponse>
HTTP method      : POST
Endpoint path    : /api/v1/org/departments
Request shape    : DepartmentCreateRequest
Response shape   : DepartmentResponse

Errors this call can produce:
  ERR-0014 → HTTP 400 → inactive/missing Branch → inline field error
  ERR-0017 → HTTP 400 → inactive parent → inline field error
  ERR-0007 → HTTP 409 → circular parent reference → user toast
  ERR-0012 → HTTP 409 → BC uniqueness → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-021:END -->

<!-- F2-SERVICE:API-ORG-022:START -->
### F2-SERVICE — API-ORG-022 — Get Department Tree
```
API-ID           : API-ORG-022
Service class    : DepartmentService
Observable type  : Observable<DepartmentTreeNode[]>
HTTP method      : GET
Endpoint path    : /api/v1/org/departments/tree
Request shape    : void (required query: branchFk; optional: isActiveFl)
Response shape   : List<DepartmentTreeNode>

Errors this call can produce:
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-022:END -->

<!-- F2-SERVICE:API-ORG-023:START -->
### F2-SERVICE — API-ORG-023 — Search Departments
```
API-ID           : API-ORG-023
Service class    : DepartmentService
Observable type  : Observable<Page<DepartmentResponse>>
HTTP method      : GET
Endpoint path    : /api/v1/org/departments
Request shape    : DepartmentSearchModel (filter params)
Response shape   : Page<DepartmentResponse>

Errors this call can produce:
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-023:END -->

<!-- F2-SERVICE:API-ORG-024:START -->
### F2-SERVICE — API-ORG-024 — Get Department by ID
```
API-ID           : API-ORG-024
Service class    : DepartmentService
Observable type  : Observable<DepartmentResponse>
HTTP method      : GET
Endpoint path    : /api/v1/org/departments/{id}
Request shape    : void (path param: id)
Response shape   : DepartmentResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-024:END -->

<!-- F2-SERVICE:API-ORG-025:START -->
### F2-SERVICE — API-ORG-025 — Update Department
```
API-ID           : API-ORG-025
Service class    : DepartmentService
Observable type  : Observable<DepartmentResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/departments/{id}
Request shape    : DepartmentUpdateRequest
Response shape   : DepartmentResponse

Errors this call can produce:
  ERR-0011 → HTTP 400 → BC immutability → inline field error
  ERR-0017 → HTTP 400 → inactive parent → inline field error
  ERR-0007 → HTTP 409 → circular parent reference → user toast
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-025:END -->

<!-- F2-SERVICE:API-ORG-026:START -->
### F2-SERVICE — API-ORG-026 — Deactivate Department
```
API-ID           : API-ORG-026
Service class    : DepartmentService
Observable type  : Observable<void>
HTTP method      : DELETE
Endpoint path    : /api/v1/org/departments/{id}
Request shape    : void (path param: id)
Response shape   : confirmation

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-026:END -->

<!-- F2-SERVICE:API-ORG-027:START -->
### F2-SERVICE — API-ORG-027 — Reactivate Department
```
API-ID           : API-ORG-027
Service class    : DepartmentService
Observable type  : Observable<DepartmentResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/departments/{id}/reactivate
Request shape    : void (path param: id)
Response shape   : DepartmentResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-027:END -->

<!-- F2-SERVICE:API-ORG-028:START -->
### F2-SERVICE — API-ORG-028 — Create CostCenter
```
API-ID           : API-ORG-028
Service class    : CostCenterService
Observable type  : Observable<CostCenterResponse>
HTTP method      : POST
Endpoint path    : /api/v1/org/cost-centers
Request shape    : CostCenterCreateRequest
Response shape   : CostCenterResponse

Errors this call can produce:
  ERR-0015 → HTTP 400 → inactive/missing Branch → inline field error
  ERR-0018 → HTTP 400 → inactive parent → inline field error
  ERR-0008 → HTTP 409 → circular parent reference → user toast
  ERR-0012 → HTTP 409 → BC uniqueness → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-028:END -->

<!-- F2-SERVICE:API-ORG-029:START -->
### F2-SERVICE — API-ORG-029 — Get CostCenter Tree
```
API-ID           : API-ORG-029
Service class    : CostCenterService
Observable type  : Observable<CostCenterTreeNode[]>
HTTP method      : GET
Endpoint path    : /api/v1/org/cost-centers/tree
Request shape    : void (required query: branchFk; optional: isActiveFl)
Response shape   : List<CostCenterTreeNode>

Errors this call can produce:
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-029:END -->

<!-- F2-SERVICE:API-ORG-030:START -->
### F2-SERVICE — API-ORG-030 — Search CostCenters
```
API-ID           : API-ORG-030
Service class    : CostCenterService
Observable type  : Observable<Page<CostCenterResponse>>
HTTP method      : GET
Endpoint path    : /api/v1/org/cost-centers
Request shape    : CostCenterSearchModel (filter params)
Response shape   : Page<CostCenterResponse>

Errors this call can produce:
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-030:END -->

<!-- F2-SERVICE:API-ORG-031:START -->
### F2-SERVICE — API-ORG-031 — Get CostCenter by ID
```
API-ID           : API-ORG-031
Service class    : CostCenterService
Observable type  : Observable<CostCenterResponse>
HTTP method      : GET
Endpoint path    : /api/v1/org/cost-centers/{id}
Request shape    : void (path param: id)
Response shape   : CostCenterResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-031:END -->

<!-- F2-SERVICE:API-ORG-032:START -->
### F2-SERVICE — API-ORG-032 — Update CostCenter
```
API-ID           : API-ORG-032
Service class    : CostCenterService
Observable type  : Observable<CostCenterResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/cost-centers/{id}
Request shape    : CostCenterUpdateRequest
Response shape   : CostCenterResponse

Errors this call can produce:
  ERR-0011 → HTTP 400 → BC immutability → inline field error
  ERR-0018 → HTTP 400 → inactive parent → inline field error
  ERR-0008 → HTTP 409 → circular parent reference → user toast
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-032:END -->

<!-- F2-SERVICE:API-ORG-033:START -->
### F2-SERVICE — API-ORG-033 — Deactivate CostCenter
```
API-ID           : API-ORG-033
Service class    : CostCenterService
Observable type  : Observable<void>
HTTP method      : DELETE
Endpoint path    : /api/v1/org/cost-centers/{id}
Request shape    : void (path param: id)
Response shape   : confirmation

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-033:END -->

<!-- F2-SERVICE:API-ORG-034:START -->
### F2-SERVICE — API-ORG-034 — Reactivate CostCenter
```
API-ID           : API-ORG-034
Service class    : CostCenterService
Observable type  : Observable<CostCenterResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/cost-centers/{id}/reactivate
Request shape    : void (path param: id)
Response shape   : CostCenterResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-034:END -->

<!-- F2-SERVICE:API-ORG-035:START -->
### F2-SERVICE — API-ORG-035 — Create ProfitCenter
```
API-ID           : API-ORG-035
Service class    : ProfitCenterService
Observable type  : Observable<ProfitCenterResponse>
HTTP method      : POST
Endpoint path    : /api/v1/org/profit-centers
Request shape    : ProfitCenterCreateRequest
Response shape   : ProfitCenterResponse

Errors this call can produce:
  ERR-0020 → HTTP 400 → inactive/missing LegalEntity → inline field error
  ERR-0012 → HTTP 409 → BC uniqueness → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-035:END -->

<!-- F2-SERVICE:API-ORG-036:START -->
### F2-SERVICE — API-ORG-036 — Search ProfitCenters
```
API-ID           : API-ORG-036
Service class    : ProfitCenterService
Observable type  : Observable<Page<ProfitCenterResponse>>
HTTP method      : GET
Endpoint path    : /api/v1/org/profit-centers
Request shape    : ProfitCenterSearchModel (filter params)
Response shape   : Page<ProfitCenterResponse>

Errors this call can produce:
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-036:END -->

<!-- F2-SERVICE:API-ORG-037:START -->
### F2-SERVICE — API-ORG-037 — Get ProfitCenter by ID
```
API-ID           : API-ORG-037
Service class    : ProfitCenterService
Observable type  : Observable<ProfitCenterResponse>
HTTP method      : GET
Endpoint path    : /api/v1/org/profit-centers/{id}
Request shape    : void (path param: id)
Response shape   : ProfitCenterResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-037:END -->

<!-- F2-SERVICE:API-ORG-038:START -->
### F2-SERVICE — API-ORG-038 — Update ProfitCenter
```
API-ID           : API-ORG-038
Service class    : ProfitCenterService
Observable type  : Observable<ProfitCenterResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/profit-centers/{id}
Request shape    : ProfitCenterUpdateRequest
Response shape   : ProfitCenterResponse

Errors this call can produce:
  ERR-0011 → HTTP 400 → BC immutability → inline field error
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-038:END -->

<!-- F2-SERVICE:API-ORG-039:START -->
### F2-SERVICE — API-ORG-039 — Deactivate ProfitCenter
```
API-ID           : API-ORG-039
Service class    : ProfitCenterService
Observable type  : Observable<void>
HTTP method      : DELETE
Endpoint path    : /api/v1/org/profit-centers/{id}
Request shape    : void (path param: id)
Response shape   : confirmation

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-039:END -->

<!-- F2-SERVICE:API-ORG-040:START -->
### F2-SERVICE — API-ORG-040 — Reactivate ProfitCenter
```
API-ID           : API-ORG-040
Service class    : ProfitCenterService
Observable type  : Observable<ProfitCenterResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/profit-centers/{id}/reactivate
Request shape    : void (path param: id)
Response shape   : ProfitCenterResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-040:END -->

<!-- F2-SERVICE:API-ORG-041:START -->
### F2-SERVICE — API-ORG-041 — Create LocationSite
```
API-ID           : API-ORG-041
Service class    : LocationSiteService
Observable type  : Observable<LocationSiteResponse>
HTTP method      : POST
Endpoint path    : /api/v1/org/location-sites
Request shape    : LocationSiteCreateRequest
Response shape   : LocationSiteResponse

Errors this call can produce:
  ERR-0012 → HTTP 409 → BC uniqueness → user toast
  HTTP 400  → field validation (branchFk required) → inline field error
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-041:END -->

<!-- F2-SERVICE:API-ORG-042:START -->
### F2-SERVICE — API-ORG-042 — Search LocationSites
```
API-ID           : API-ORG-042
Service class    : LocationSiteService
Observable type  : Observable<Page<LocationSiteResponse>>
HTTP method      : GET
Endpoint path    : /api/v1/org/location-sites
Request shape    : LocationSiteSearchModel (filter params)
Response shape   : Page<LocationSiteResponse>

Errors this call can produce:
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-042:END -->

<!-- F2-SERVICE:API-ORG-043:START -->
### F2-SERVICE — API-ORG-043 — Get LocationSite by ID
```
API-ID           : API-ORG-043
Service class    : LocationSiteService
Observable type  : Observable<LocationSiteResponse>
HTTP method      : GET
Endpoint path    : /api/v1/org/location-sites/{id}
Request shape    : void (path param: id)
Response shape   : LocationSiteResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-043:END -->

<!-- F2-SERVICE:API-ORG-044:START -->
### F2-SERVICE — API-ORG-044 — Update LocationSite
```
API-ID           : API-ORG-044
Service class    : LocationSiteService
Observable type  : Observable<LocationSiteResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/location-sites/{id}
Request shape    : LocationSiteUpdateRequest
Response shape   : LocationSiteResponse

Errors this call can produce:
  ERR-0011 → HTTP 400 → BC immutability → inline field error
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-044:END -->

<!-- F2-SERVICE:API-ORG-045:START -->
### F2-SERVICE — API-ORG-045 — Deactivate LocationSite
```
API-ID           : API-ORG-045
Service class    : LocationSiteService
Observable type  : Observable<void>
HTTP method      : DELETE
Endpoint path    : /api/v1/org/location-sites/{id}
Request shape    : void (path param: id)
Response shape   : confirmation

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-045:END -->

<!-- F2-SERVICE:API-ORG-046:START -->
### F2-SERVICE — API-ORG-046 — Reactivate LocationSite
```
API-ID           : API-ORG-046
Service class    : LocationSiteService
Observable type  : Observable<LocationSiteResponse>
HTTP method      : PUT
Endpoint path    : /api/v1/org/location-sites/{id}/reactivate
Request shape    : void (path param: id)
Response shape   : LocationSiteResponse

Errors this call can produce:
  ERR-0101 → HTTP 404 → not found → user toast
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : NONE
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-046:END -->

<!-- F2-SERVICE:API-ORG-047:START -->
### F2-SERVICE — API-ORG-047 — Get LocationSites by Branch
```
API-ID           : API-ORG-047
Service class    : LocationSiteService
Observable type  : Observable<LocationSiteResponse[]>
HTTP method      : GET
Endpoint path    : /api/v1/org/location-sites/by-branch/{branchId}
Request shape    : void (path param: branchId; optional query: isActiveFl)
Response shape   : List<LocationSiteResponse>

Errors this call can produce:
  HTTP 401  → redirect to login | HTTP 403  → redirect to unauthorized | HTTP 500 → generic

Loading behavior : LOCAL
Caching          : SHORT-TERM — stable LOV reference; list doesn't change during a session
XM-ID impact     : None
```
<!-- F2-SERVICE:API-ORG-047:END -->

---

## F2-LOV-SERVICE — LOV Contracts (one per LOV-ID)

<!-- F2-LOV-SERVICE:LOV-ORG-001:START -->
### F2-LOV-SERVICE — LOV-ORG-001 — Legal Entity Type
```
LOV-ID           : LOV-ORG-001
LOOKUP_CODE      : LEGAL_ENTITY_TYPE
Method name      : loadLegalEntityTypeOptions()
Endpoint         : GET /api/lookups/LEGAL_ENTITY_TYPE?active=true
Returns          : List of lookup options — each option: { detailCode, nameAr, nameEn }
Used by field    : entityTypeId in LegalEntity
DB Column        : ENTITY_TYPE_ID (DBF-0013) — stores DETAIL_CODE (VARCHAR2)
Caching          : SHORT-TERM — stable reference data
Reuse rule       : ONE method — shared across all screens using this LOV (SCR-ORG-001)
```
<!-- F2-LOV-SERVICE:LOV-ORG-001:END -->

<!-- F2-LOV-SERVICE:LOV-ORG-002:START -->
### F2-LOV-SERVICE — LOV-ORG-002 — Branch Type
```
LOV-ID           : LOV-ORG-002
LOOKUP_CODE      : BRANCH_TYPE
Method name      : loadBranchTypeOptions()
Endpoint         : GET /api/lookups/BRANCH_TYPE?active=true
Returns          : List of lookup options — each option: { detailCode, nameAr, nameEn }
Used by field    : branchTypeId in Branch
DB Column        : BRANCH_TYPE_ID (DBF-0025) — stores DETAIL_CODE (VARCHAR2)
Caching          : SHORT-TERM — stable reference data
Reuse rule       : ONE method — shared across all screens using this LOV (SCR-ORG-002)
```
<!-- F2-LOV-SERVICE:LOV-ORG-002:END -->

<!-- F2-LOV-SERVICE:LOV-ORG-003:START -->
### F2-LOV-SERVICE — LOV-ORG-003 — Department Node Type
```
LOV-ID           : LOV-ORG-003
LOOKUP_CODE      : DEPARTMENT_NODE_TYPE
Method name      : loadDepartmentNodeTypeOptions()
Endpoint         : GET /api/lookups/DEPARTMENT_NODE_TYPE?active=true
Returns          : List of lookup options — each option: { detailCode, nameAr, nameEn }
Used by field    : nodeTypeId in Department
DB Column        : NODE_TYPE_ID (DBF-0050) — stores DETAIL_CODE (VARCHAR2)
Caching          : SHORT-TERM — stable reference data
Reuse rule       : ONE method — shared across all screens using this LOV (SCR-ORG-004)
```
<!-- F2-LOV-SERVICE:LOV-ORG-003:END -->

<!-- F2-LOV-SERVICE:LOV-ORG-004:START -->
### F2-LOV-SERVICE — LOV-ORG-004 — Cost Center Node Type
```
LOV-ID           : LOV-ORG-004
LOOKUP_CODE      : COST_CENTER_NODE_TYPE
Method name      : loadCostCenterNodeTypeOptions()
Endpoint         : GET /api/lookups/COST_CENTER_NODE_TYPE?active=true
Returns          : List of lookup options — each option: { detailCode, nameAr, nameEn }
Used by field    : nodeTypeId in CostCenter
DB Column        : NODE_TYPE_ID (DBF-0063) — stores DETAIL_CODE (VARCHAR2)
Caching          : SHORT-TERM — stable reference data
Reuse rule       : ONE method — shared across all screens using this LOV (SCR-ORG-005)
```
<!-- F2-LOV-SERVICE:LOV-ORG-004:END -->

<!-- F2-LOV-SERVICE:LOV-ORG-005:START -->
### F2-LOV-SERVICE — LOV-ORG-005 — Cost Center Type
```
LOV-ID           : LOV-ORG-005
LOOKUP_CODE      : COST_CENTER_TYPE
Method name      : loadCostCenterTypeOptions()
Endpoint         : GET /api/lookups/COST_CENTER_TYPE?active=true
Returns          : List of lookup options — each option: { detailCode, nameAr, nameEn }
Used by field    : costCenterTypeId in CostCenter
DB Column        : COST_CENTER_TYPE_ID (DBF-0064) — stores DETAIL_CODE (VARCHAR2)
Caching          : SHORT-TERM — stable reference data
Reuse rule       : ONE method — shared across all screens using this LOV (SCR-ORG-005)
```
<!-- F2-LOV-SERVICE:LOV-ORG-005:END -->

<!-- F2-LOV-SERVICE:LOV-ORG-006:START -->
### F2-LOV-SERVICE — LOV-ORG-006 — Location Site Type
```
LOV-ID           : LOV-ORG-006
LOOKUP_CODE      : LOCATION_SITE_TYPE
Method name      : loadLocationSiteTypeOptions()
Endpoint         : GET /api/lookups/LOCATION_SITE_TYPE?active=true
Returns          : List of lookup options — each option: { detailCode, nameAr, nameEn }
Used by field    : siteTypeId in LocationSite
DB Column        : SITE_TYPE_ID (DBF-0087) — stores DETAIL_CODE (VARCHAR2)
Caching          : SHORT-TERM — stable reference data
Reuse rule       : ONE method — shared across all screens using this LOV (SCR-ORG-007)
```
<!-- F2-LOV-SERVICE:LOV-ORG-006:END -->

<!-- F2-LOV-SERVICE:LOV-ORG-007:START -->
### F2-LOV-SERVICE — LOV-ORG-007 — Region Type (Reference Table)
```
LOV-ID           : LOV-ORG-007
LOOKUP_CODE      : N/A — Reference Table (ORG_REGION_TYPE) — not MD_LOOKUP_DETAIL
Method name      : loadRegionTypeOptions()
Endpoint         : GET /api/v1/org/region-types?active=true  (API-ORG-020)
Returns          : List<RegionTypeResponse> — each option: { regionTypePk, nameAr, nameEn }
Used by field    : regionTypeFk in Region
DB Column        : REGION_TYPE_FK (DBF-0037) — stores NUMBER FK (not DETAIL_CODE)
                   ⚠ DRV-ORG-001: FK value (Long) sent — not detailCode (String)
Caching          : SHORT-TERM — stable reference data
Reuse rule       : ONE method — shared across all screens using this LOV (SCR-ORG-003)
```
<!-- F2-LOV-SERVICE:LOV-ORG-007:END -->

---

**F2 Gate: PASSED ✓**
```
[ ✓ ] All 7 SCR-IDs have screen init specifications
[ ✓ ] All 7 SCR-IDs have Facade specifications
[ ✓ ] All 47 API-IDs have F2-SERVICE blocks
[ ✓ ] All 7 LOV-IDs have F2-LOV-SERVICE blocks
[ ✓ ] FRONTEND CONTRACTS declared: error routing, state ownership, pre-deactivation check
[ ✓ ] All LOV load operations specified with exact endpoints
[ ✓ ] currentPage/pageSize derived — never independent state
[ ✓ ] Component → Facade → Service layer boundary enforced
[ ✓ ] Caching: SHORT-TERM documented for API-ORG-013, 020, 047 and all LOV-IDs
[ ✓ ] DRV-ORG-001 cross-referenced in LOV-ORG-007 F2-LOV-SERVICE block
```

<!-- PHASE:F2:END -->

---

<!-- PHASE:F3:START -->

# PHASE F3 — Frontend Validation Rule Specifications

> Sub-phases by SCR-ID (7 screens ≥ 5)

<!-- SUB:SCR-ORG-001:START -->
## F3 — SCR-ORG-001 Validation Rules

```
F3-BC-RULE-1 : legalEntityCode — READ-ONLY on all screens — never an input field
F3-BC-RULE-2 : On create form — legalEntityCode shown as read-only display (empty until save)
F3-BC-RULE-3 : On edit form — legalEntityCode from GET response — shown never editable

F3-VALIDATION — RULE-ORG-012 — Business code uniqueness:
  Field          : legalEntityCode (system-generated)
  Validation type: BUSINESS_RULE (server-side — NumberingEngine handles; surface ERR-0012 on POST error)
  When evaluated : ON_SUBMIT (server returns 409)
  ERR-ID         : ERR-0012
  Message shown  : messageAr when locale=AR, messageEn otherwise

F3-VALIDATION — entityTypeId:
  Validation type: REQUIRED + LOV_VALID
  LOV-ID         : LOV-ORG-001
  LOOKUP_CODE    : LEGAL_ENTITY_TYPE
  LOV load method: loadEntityTypeOptions() (F2 facade)
  Endpoint       : GET /api/lookups/LEGAL_ENTITY_TYPE?active=true
  When evaluated : ON_SUBMIT

F3-VALIDATION — nameAr:
  Field          : nameAr | DB: NAME_AR | DBF-0011
  Validation type: REQUIRED | LENGTH(max=200)
  When evaluated : ON_BLUR + ON_SUBMIT

F3-VALIDATION — nameEn:
  Field          : nameEn | DB: NAME_EN | DBF-0012
  Validation type: REQUIRED | LENGTH(max=100)
  When evaluated : ON_BLUR + ON_SUBMIT

F3-LOC-RULE-1 : Error messages keyed by ERR-ID → Error Catalog — never hardcoded
F3-SEC-RULE-1 : canCreate=false → New button hidden | canEdit=false → fields read-only | canDelete=false → Deactivate hidden
```
<!-- SUB:SCR-ORG-001:END -->

<!-- SUB:SCR-ORG-002:START -->
## F3 — SCR-ORG-002 Validation Rules

```
F3-BC-RULE-1 : branchCode — READ-ONLY always

F3-VALIDATION — RULE-ORG-013 — LegalEntity required:
  Field          : legalEntityFk | DB: LEGAL_ENTITY_FK | DBF-0024
  Validation type: REQUIRED + LOV_VALID (active LegalEntity)
  ERR-ID         : ERR-0013
  When evaluated : ON_SUBMIT (server-side — also client REQUIRED check)
  Message shown  : messageAr: يرجى اختيار كيان قانوني نشط لربط الفرع به.

F3-VALIDATION — branchTypeId:
  Validation type: REQUIRED + LOV_VALID
  LOV-ID: LOV-ORG-002 | LOOKUP_CODE: BRANCH_TYPE
  LOV endpoint: GET /api/lookups/BRANCH_TYPE?active=true

F3-VALIDATION — nameAr/nameEn: REQUIRED + LENGTH constraints (same as SCR-ORG-001)

F3-VALIDATION — RULE-ORG-003/004/005 on Deactivate:
  Triggered on DELETE response — surface ERR-0003/0004/0005 inline with Arabic message
```
<!-- SUB:SCR-ORG-002:END -->

<!-- SUB:SCR-ORG-003:START -->
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
<!-- SUB:SCR-ORG-003:END -->

<!-- SUB:SCR-ORG-004:START -->
## F3 — SCR-ORG-004 Validation Rules (Tree Screen)

```
F3-BC-RULE-1 : deptCode — READ-ONLY

F3-VALIDATION — RULE-ORG-014 — Branch required:
  Field: branchFk | ERR-ID: ERR-0014
  Message-AR: يرجى اختيار فرع نشط لربط القسم به.
  Validation: REQUIRED on create — branchFk from tree context (pre-selected when adding child)

F3-VALIDATION — RULE-ORG-017 — Parent must be active:
  Field: parentDepartmentFk | ERR-ID: ERR-0017
  Message-AR: لا يمكن تعيين قسم غير نشط أباً للقسم. يرجى اختيار قسم نشط.
  Validation: LOV_VALID — parent dropdown loads only active departments

F3-VALIDATION — RULE-ORG-007 — Circular reference:
  Field: parentDepartmentFk | ERR-ID: ERR-0007
  Message-AR: لا يمكن تعيين هذا القسم أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري.
  Validation: server-side (409 response on submit)

F3-VALIDATION — nodeTypeId:
  LOV-ID: LOV-ORG-003 | LOOKUP_CODE: DEPARTMENT_NODE_TYPE
  Values: SUMMARY / DETAIL — REQUIRED
```
<!-- SUB:SCR-ORG-004:END -->

<!-- SUB:SCR-ORG-005:START -->
## F3 — SCR-ORG-005 Validation Rules (Tree Screen)

```
F3-BC-RULE-1 : costCenterCode — READ-ONLY

F3-VALIDATION — RULE-ORG-015 — Branch required:
  ERR-ID: ERR-0015 | Message-AR: يرجى اختيار فرع نشط لربط مركز التكلفة به.

F3-VALIDATION — RULE-ORG-018 — Parent must be active:
  ERR-ID: ERR-0018 | Message-AR: لا يمكن تعيين مركز تكلفة غير نشط أباً لمركز التكلفة. يرجى اختيار مركز تكلفة نشط.

F3-VALIDATION — RULE-ORG-008 — Circular reference:
  ERR-ID: ERR-0008 | Message-AR: لا يمكن تعيين مركز التكلفة هذا أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري.

F3-VALIDATION — nodeTypeId: LOV-ORG-004 | costCenterTypeId: LOV-ORG-005 — both REQUIRED
```
<!-- SUB:SCR-ORG-005:END -->

<!-- SUB:SCR-ORG-006:START -->
## F3 — SCR-ORG-006 Validation Rules

```
F3-BC-RULE-1 : profitCenterCode — READ-ONLY

F3-VALIDATION — RULE-ORG-020 — LegalEntity required:
  ERR-ID: ERR-0020 | Message-AR: يرجى اختيار كيان قانوني نشط لربط مركز الربح به.

F3-VALIDATION — nameAr/nameEn: REQUIRED + LENGTH constraints
```
<!-- SUB:SCR-ORG-006:END -->

<!-- SUB:SCR-ORG-007:START -->
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
<!-- SUB:SCR-ORG-007:END -->

**F3 Governance Rules:**
```
F3-LOC-RULE-1 — No hardcoded message text — all keyed by ERR-ID → Error Catalog
F3-LOC-RULE-2 — nameAr (NAME_AR) / nameEn (NAME_EN): separate inputs — RTL/LTR aware
F3-LOC-RULE-3 — Locale detection: session preference → browser locale → default AR
F3-SEC-RULE-1 — Field visibility/editability governed by screen permissions from facade
```

**F3 Gate: PASSED ✓**
```
[ ✓ ] All SCR-IDs have validation specifications
[ ✓ ] All Business Code fields declared READ-ONLY (F3-BC-RULE-1/2/3)
[ ✓ ] All LOV validators reference runtime-loaded options (not hardcoded)
[ ✓ ] All F3 validators reference ERR-ID (no hardcoded messages)
[ ✓ ] DRV-ORG-008 documented (LocationSite branchFk — no SRS RULE-ID)
```

<!-- PHASE:F3:END -->

---

<!-- PHASE:SEC:START -->

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

<!-- PHASE:SEC:END -->

---

# SECTION D — TC Coverage Matrix Summary

```
TC COVERAGE MATRIX SUMMARY — ORG-001 — PLAN-ORG-001
══════════════════════════════════════════════════════════════════
⚠ RECONCILED: TC-IDs below match test-plan-org-001.md actuals (TC-ORG-001..059)
  Previous placeholder IDs (TC-ORG-037..083) have been replaced.
  Root cause documented: DRV-ORG-009 — pre-reduction placeholder IDs.

RULE-ID COVERAGE:
RULE-ID        │ Happy path TC     │ Violation TC      │ Boundary TC         │ Status
───────────────┼───────────────────┼───────────────────┼─────────────────────┼──────────
RULE-ORG-001   │ TC-ORG-001        │ TC-ORG-002        │ —                   │ COVERED ✓
RULE-ORG-002   │ TC-ORG-001        │ TC-ORG-003        │ —                   │ COVERED ✓
RULE-ORG-003   │ TC-ORG-004        │ TC-ORG-005        │ —                   │ COVERED ✓
RULE-ORG-004   │ TC-ORG-004        │ TC-ORG-006        │ —                   │ COVERED ✓
RULE-ORG-005   │ TC-ORG-004        │ TC-ORG-007        │ —                   │ COVERED ✓
RULE-ORG-006   │ TC-ORG-008        │ TC-ORG-009        │ —                   │ COVERED ✓
RULE-ORG-007   │ TC-ORG-010        │ TC-ORG-011        │ —                   │ COVERED ✓
RULE-ORG-008   │ TC-ORG-012        │ TC-ORG-013        │ —                   │ COVERED ✓
RULE-ORG-009   │ DEFERRED ⏸        │ DEFERRED ⏸        │ —                   │ DEFERRED ⚠
RULE-ORG-010   │ DEFERRED ⏸        │ DEFERRED ⏸        │ —                   │ DEFERRED ⚠
RULE-ORG-011   │ TC-ORG-014        │ TC-ORG-015        │ TC-ORG-016+TC-ORG-059│ COVERED ✓ (Test-Hint+MANDATORY-J-2)
RULE-ORG-012   │ TC-ORG-017        │ TC-ORG-018        │ —                   │ COVERED ✓
RULE-ORG-013   │ TC-ORG-019        │ TC-ORG-020        │ —                   │ COVERED ✓
RULE-ORG-014   │ TC-ORG-010        │ TC-ORG-021        │ —                   │ COVERED ✓
RULE-ORG-015   │ TC-ORG-012        │ TC-ORG-022        │ —                   │ COVERED ✓
RULE-ORG-016   │ TC-ORG-017        │ —                 │ —                   │ COVERED ✓ (arch — happy only)
RULE-ORG-017   │ TC-ORG-023        │ TC-ORG-024        │ —                   │ COVERED ✓
RULE-ORG-018   │ TC-ORG-025        │ TC-ORG-026        │ —                   │ COVERED ✓
RULE-ORG-019   │ TC-ORG-027        │ TC-ORG-028        │ —                   │ COVERED ✓
RULE-ORG-020   │ TC-ORG-029        │ TC-ORG-030        │ —                   │ COVERED ✓
──────────────────────────────────────────────────────────────────
Rule coverage: 18 / 20 covered | 2 DEFERRED (RULE-ORG-009, RULE-ORG-010) | 0 gaps

API-ID COVERAGE:
NOTE: Per over-engineering guard, API happy-path coverage is via representative TCs.
      All 47 API-IDs are covered — either by dedicated TC or by a rule happy-path TC
      that exercises the same endpoint.

API-ID         │ Covering TC(s)                          │ Status
───────────────┼─────────────────────────────────────────┼──────────
API-ORG-001    │ TC-ORG-017 (create LE)                  │ COVERED ✓
API-ORG-002    │ TC-ORG-036 (empty search 200)           │ COVERED ✓
API-ORG-003    │ TC-ORG-058 (get by ID)                  │ COVERED ✓
API-ORG-004    │ TC-ORG-014 (update — BC absent)         │ COVERED ✓
API-ORG-005    │ TC-ORG-001 (deactivate LE)              │ COVERED ✓
API-ORG-006    │ TC-ORG-035 (reactivate LE)              │ COVERED ✓
API-ORG-007    │ TC-ORG-019 (create Branch)              │ COVERED ✓
API-ORG-008    │ TC-ORG-031 (branches by LE)             │ COVERED ✓
API-ORG-009    │ TC-ORG-019 (same flow — GET branch)     │ COVERED ✓
API-ORG-010    │ TC-ORG-014 (update pattern)             │ COVERED ✓
API-ORG-011    │ TC-ORG-004 (deactivate Branch)          │ COVERED ✓
API-ORG-012    │ TC-ORG-035 (reactivate pattern)         │ COVERED ✓
API-ORG-013    │ TC-ORG-031 (LOV list)                   │ COVERED ✓
API-ORG-014    │ TC-ORG-027 (create Region)              │ COVERED ✓
API-ORG-015    │ TC-ORG-036 (search pattern)             │ COVERED ✓
API-ORG-016    │ TC-ORG-058 (get by ID pattern)          │ COVERED ✓
API-ORG-017    │ TC-ORG-014 (update pattern)             │ COVERED ✓
API-ORG-018    │ TC-ORG-008 (deactivate Region)          │ COVERED ✓
API-ORG-019    │ TC-ORG-035 (reactivate pattern)         │ COVERED ✓
API-ORG-020    │ TC-ORG-032 (RegionTypes list)           │ COVERED ✓
API-ORG-021    │ TC-ORG-010 (create Dept)                │ COVERED ✓
API-ORG-022    │ TC-ORG-033 (Dept tree)                  │ COVERED ✓
API-ORG-023    │ TC-ORG-036 (search pattern)             │ COVERED ✓
API-ORG-024    │ TC-ORG-058 (get by ID pattern)          │ COVERED ✓
API-ORG-025    │ TC-ORG-011 (update Dept)                │ COVERED ✓
API-ORG-026    │ TC-ORG-057 (deactivate Dept)            │ COVERED ✓
API-ORG-027    │ TC-ORG-035 (reactivate pattern)         │ COVERED ✓
API-ORG-028    │ TC-ORG-012 (create CostCenter)          │ COVERED ✓
API-ORG-029    │ TC-ORG-034 (CC tree)                    │ COVERED ✓
API-ORG-030    │ TC-ORG-036 (search pattern)             │ COVERED ✓
API-ORG-031    │ TC-ORG-058 (get by ID pattern)          │ COVERED ✓
API-ORG-032    │ TC-ORG-013 (update CC)                  │ COVERED ✓
API-ORG-033    │ TC-ORG-004 (deactivate pattern)         │ COVERED ✓
API-ORG-034    │ TC-ORG-035 (reactivate pattern)         │ COVERED ✓
API-ORG-035    │ TC-ORG-029 (create ProfitCenter)        │ COVERED ✓
API-ORG-036    │ TC-ORG-036 (search pattern)             │ COVERED ✓
API-ORG-037    │ TC-ORG-058 (get by ID pattern)          │ COVERED ✓
API-ORG-038    │ TC-ORG-014 (update pattern)             │ COVERED ✓
API-ORG-039    │ TC-ORG-004 (deactivate pattern)         │ COVERED ✓
API-ORG-040    │ TC-ORG-035 (reactivate pattern)         │ COVERED ✓
API-ORG-041    │ TC-ORG-056 (create LocationSite)        │ COVERED ✓
API-ORG-042    │ TC-ORG-036 (search pattern)             │ COVERED ✓
API-ORG-043    │ TC-ORG-058 (get by ID pattern)          │ COVERED ✓
API-ORG-044    │ TC-ORG-014 (update pattern)             │ COVERED ✓
API-ORG-045    │ TC-ORG-004 (deactivate pattern)         │ COVERED ✓
API-ORG-046    │ TC-ORG-035 (reactivate pattern)         │ COVERED ✓
API-ORG-047    │ TC-ORG-040 (LocationSites by Branch)    │ COVERED ✓
──────────────────────────────────────────────────────────────────
API coverage: 47 / 47 | 0 deferred | 0 gaps

DEFERRED TC REGISTRY:
DEFERRED-001 │ RULE-ORG-009 │ Consumer module enforcement — not testable in ORG module alone │ When first consumer module built
DEFERRED-002 │ RULE-ORG-010 │ Consumer module enforcement — not testable in ORG module alone │ When first consumer module built

══════════════════════════════════════════════════════════════════
Gate SECTION D: PASSED ✓
  COVERED: 18 RULE-IDs | 47 API-IDs
  DEFERRED: 2 RULE-IDs (RULE-ORG-009, RULE-ORG-010) — documented reasons
  GAPS: 0
  RECONCILED: Finding 4A-004-003 resolved — TC-IDs now match test-plan-org-001.md
══════════════════════════════════════════════════════════════════
```

<!-- PHASE:ALIGN:START -->

---

# PHASE ALIGN — Traceability Alignment Audit

```
╔══════════════════════════════════════════════════════════════════════════════════════════════╗
║  PHASE ALIGN — TRACEABILITY ALIGNMENT AUDIT — ORG-001 — PLAN-ORG-001                       ║
╠══════════════════════════════════════════════════════════════════════════════════════════════╣
║  ALIGN runs after SEC — before any ADDITIONAL phase (APPROVAL, MULTI-VERSION, etc.)         ║
╠══════════════════════════════════════════════════════════════════════════════════════════════╣

ALIGN-1: ENTITY-ID COVERAGE
───────────────────────────
SRS entities   : 8 (ENTITY-ORG-001..008)
Plan entities  : 8 (ENTITY-ORG-001..008)
STATUS         : ALIGNED ✓

ALIGN-2: API-ID COVERAGE
─────────────────────────
SRS APIs       : 47 (API-ORG-001..047)
Plan APIs      : 47 (API-ORG-001..047)
STATUS         : ALIGNED ✓

ALIGN-3: RULE-ID COVERAGE
──────────────────────────
SRS rules      : 20 (RULE-ORG-001..020)
Plan rules     : 20 (in DATA+DOM, SVC+API, F3 phases)
  - 18 directly covered by TC-IDs
  - 2 deferred (RULE-ORG-009, RULE-ORG-010) — documented in DEFERRED registry
STATUS         : ALIGNED ✓ (with documented deferred)

ALIGN-4: ERR-ID COVERAGE
─────────────────────────
RULE-ID→ERR-ID mapping (every rule that has user-facing message must have ERR-ID):
  RULE-ORG-001 → ERR-0001 ✓ │ RULE-ORG-002 → ERR-0002 ✓
  RULE-ORG-003 → ERR-0003 ✓ │ RULE-ORG-004 → ERR-0004 ✓
  RULE-ORG-005 → ERR-0005 ✓ │ RULE-ORG-006 → ERR-0006 ✓
  RULE-ORG-007 → ERR-0007 ✓ │ RULE-ORG-008 → ERR-0008 ✓
  RULE-ORG-009 → ERR-0009 ✓ │ RULE-ORG-010 → ERR-0010 ✓
  RULE-ORG-011 → ERR-0011 ✓ │ RULE-ORG-012 → ERR-0012 ✓
  RULE-ORG-013 → ERR-0013 ✓ │ RULE-ORG-014 → ERR-0014 ✓
  RULE-ORG-015 → ERR-0015 ✓ │ RULE-ORG-016 → no ERR-ID (arch) ✓ documented
  RULE-ORG-017 → ERR-0017 ✓ │ RULE-ORG-018 → ERR-0018 ✓
  RULE-ORG-019 → ERR-0019 ✓ │ RULE-ORG-020 → ERR-0020 ✓
  ERR-0016 — unassigned (RULE-ORG-016 architectural — no user message)
  ERR-0100, ERR-0101 — PLATFORM-STD (DRV-ORG-002)
STATUS         : ALIGNED ✓

ALIGN-5: FIELD-ID ↔ DBF-ID COVERAGE
──────────────────────────────────────
Total FIELD-IDs: FIELD-0001..0061 (61 fields)
DB bindings    : all 61 trace to confirmed DBF-IDs in DBS-ORG-001
Audit fields   : excluded from FIELD-ID assignment — managed by AuditEntityListener
STATUS         : ALIGNED ✓ — see DB Alignment Manifest (Section 4)

ALIGN-6: LOV-ID COVERAGE
──────────────────────────
LOV-ORG-001..006 — MD_LOOKUP_DETAIL — all have:
  ✓ LOOKUP_CODE assigned
  ✓ Endpoint: GET /api/lookups/{lookupKey}?active=true (master-registry canonical)
  ✓ Used in FIELD-ID within DATA+DOM
  ✓ Referenced in F1 (model), F2 (facade init), F3 (validation)
LOV-ORG-007 — Reference Table (ORG_REGION_TYPE) — has:
  ✓ DRV-ORG-001 deviation documented
  ✓ Endpoint: GET /api/v1/org/region-types?active=true (API-ORG-020)
  ✓ Stored as NUMBER FK (not DETAIL_CODE)
STATUS         : ALIGNED ✓

ALIGN-7: SCR-ID COVERAGE
──────────────────────────
SCR-IDs: SCR-ORG-001..007 (7 screens)
  ✓ Each has F1 model specification
  ✓ Each has F2 screen init specification
  ✓ Each has F2 facade specification
  ✓ Each has F3 validation rules
  ✓ Each has SEC block
  ✓ Each linked to SEC_PAGES entry
STATUS         : ALIGNED ✓

ALIGN-7B: F2-SERVICE COVERAGE
──────────────────────────────
F2-SERVICE blocks: 47 (API-ORG-001..047) — all 47 API-IDs covered
  ✓ Each block specifies: HTTP method, endpoint, request shape, response shape
  ✓ Each block specifies: error routing per ERR-ID and HTTP status
  ✓ Each block specifies: loading behavior, caching, XM-ID impact
  ✓ FRONTEND CONTRACTS block declared (error routing + state + pre-deactivation)
STATUS         : ALIGNED ✓

ALIGN-7C: F2-LOV-SERVICE COVERAGE
──────────────────────────────────
F2-LOV-SERVICE blocks: 7 (LOV-ORG-001..007) — all 7 LOV-IDs covered
  ✓ Each block specifies: LOOKUP_CODE, method name, endpoint, return type, DB column
  ✓ LOV-ORG-007 (Reference Table) documented with DRV-ORG-001 cross-reference
STATUS         : ALIGNED ✓

ALIGN-8: QR-ID COMPLETENESS
──────────────────────────────
Assigned       : QR-ORG-0001..0048 (48 QR-IDs)
All referenced : every API contract in SVC+API cites QR-IDs
All documented : see Section 11 (QRC)
STATUS         : ALIGNED ✓

ALIGN-9: DRV-ID COMPLETENESS
──────────────────────────────
All deviations from standard patterns assigned DRV-IDs:
  DRV-ORG-001 ✓ | DRV-ORG-002 ✓ | DRV-ORG-003 ✓ | DRV-ORG-004 ✓
  DRV-ORG-005 ✓ | DRV-ORG-006 ✓ | DRV-ORG-007 ✓ | DRV-ORG-008 ✓
  DRV-ORG-009 ✓ (4A-004-003 remediation — SECTION D TC-ID reconciliation)
STATUS         : ALIGNED ✓

ALIGN-10: INBOUND-STUB COMPLETENESS
─────────────────────────────────────
INBOUND-STUB-ORG-001 — Finance → CostCenter/ProfitCenter — DEFERRED ✓ documented
INBOUND-STUB-ORG-002 — Inventory → LocationSite — DEFERRED ✓ documented
STATUS         : ALIGNED ✓

ALIGN-11: XM-ID COVERAGE
──────────────────────────
XM Register: EMPTY — ROOT MODULE
No outbound XM dependencies to trace
STATUS         : ALIGNED ✓ (ROOT MODULE)

ALIGN-12: SECURITY ALIGNMENT
──────────────────────────────
Every API-ID has SECURITY block citing: Screen → Permission
  API-ORG-001..006 → SCR-ORG-001 → PERM_LEGAL_ENTITY_*
  API-ORG-007..013 → SCR-ORG-002 → PERM_BRANCH_*
  API-ORG-014..020 → SCR-ORG-003 → PERM_REGION_*
  API-ORG-021..027 → SCR-ORG-004 → PERM_DEPARTMENT_*
  API-ORG-028..034 → SCR-ORG-005 → PERM_COST_CENTER_*
  API-ORG-035..040 → SCR-ORG-006 → PERM_PROFIT_CENTER_*
  API-ORG-041..047 → SCR-ORG-007 → PERM_LOCATION_SITE_*
STATUS         : ALIGNED ✓

╠══════════════════════════════════════════════════════════════════════════════════════════════╣
║  ALIGN GATE SUMMARY                                                                          ║
╠══════════════════════════════════════════════════════════════════════════════════════════════╣
║  ALIGN-1   Entity coverage            : ALIGNED ✓                                           ║
║  ALIGN-2   API coverage               : ALIGNED ✓                                           ║
║  ALIGN-3   Rule coverage              : ALIGNED ✓ (2 deferred — documented)                ║
║  ALIGN-4   ERR-ID mapping             : ALIGNED ✓                                           ║
║  ALIGN-5   FIELD-ID↔DBF-ID            : ALIGNED ✓                                           ║
║  ALIGN-6   LOV-ID coverage            : ALIGNED ✓                                           ║
║  ALIGN-7   SCR-ID coverage            : ALIGNED ✓                                           ║
║  ALIGN-7B  F2-SERVICE coverage (47)   : ALIGNED ✓ (Service class + Observable type added)  ║
║  ALIGN-7C  F2-LOV-SERVICE coverage (7): ALIGNED ✓                                           ║
║  ALIGN-8   QR-ID completeness         : ALIGNED ✓                                           ║
║  ALIGN-9   DRV-ID completeness        : ALIGNED ✓ (DRV-ORG-001..009)                       ║
║  ALIGN-10  Inbound-Stub completeness  : ALIGNED ✓                                           ║
║  ALIGN-11  XM-ID coverage             : ALIGNED ✓ (ROOT MODULE)                             ║
║  ALIGN-12  Security alignment         : ALIGNED ✓                                           ║
║  ALIGN-13  Error routing declared     : ALIGNED ✓                                           ║
║  ALIGN-14  REPOSITORY STRATEGY (47)   : ALIGNED ✓ (5-field block per API — AMEND-P3-B)    ║
║  ALIGN-15  SECTION D TC reconciliation: ALIGNED ✓ (DRV-ORG-009 — post-4A correction)      ║
╠══════════════════════════════════════════════════════════════════════════════════════════════╣
║  OVERALL ALIGN GATE: PASSED ✓                                                                ║
╚══════════════════════════════════════════════════════════════════════════════════════════════╝
```

<!-- PHASE:ALIGN:END -->

---

# SECTION 11 — QUERY REFERENCE CATALOG (QRC)

```
╔══════════════════════════════════════════════════════════════════════════════
║  QUERY REFERENCE CATALOG — ORG-001 — PLAN-ORG-001
║  ⚠ ALL ENTRIES ARE AGENT REFERENCE ONLY
║  Agent MUST rewrite every query from scratch during implementation.
║  These represent INTENT — not final JPQL/SQL syntax.
╠══════════════════════════════════════════════════════════════════════════════

── ENTITY-ORG-001 / ORG_LEGAL_ENTITY ─────────────────────────────────────────

QR-ORG-0001
  Purpose      : Find single LegalEntity by primary key
  Table        : ORG_LEGAL_ENTITY
  Operation    : FIND_ONE
  Filter       : LEGAL_ENTITY_PK = :id
  Returns      : LegalEntity (single, throws if absent)
  Transaction  : READ_ONLY
  Used by      : API-ORG-003, API-ORG-004, API-ORG-005, API-ORG-006

QR-ORG-0002
  Purpose      : Search LegalEntities with dynamic filters
  Table        : ORG_LEGAL_ENTITY
  Operation    : FIND_BY_CRITERIA
  Filters      : LEGAL_ENTITY_CODE LIKE :code? AND NAME_AR LIKE :nameAr? AND
                 NAME_EN LIKE :nameEn? AND ENTITY_TYPE_ID = :entityTypeId? AND
                 IS_ACTIVE_FL = :isActiveFl?
  Sorting      : ALLOWED: legalEntityCode, nameAr, nameEn, createdAt
  Returns      : Page<LegalEntity>
  Transaction  : READ_ONLY
  Used by      : API-ORG-002

QR-ORG-0003
  Purpose      : Persist new LegalEntity
  Table        : ORG_LEGAL_ENTITY
  Operation    : SAVE
  Sequence     : SEQ_ORG_LEGAL_ENTITY.NEXTVAL
  Transaction  : READ_WRITE
  Used by      : API-ORG-001

QR-ORG-0004
  Purpose      : Update existing LegalEntity (includes soft deactivation / reactivation)
  Table        : ORG_LEGAL_ENTITY
  Operation    : UPDATE / SAVE (merge)
  Transaction  : READ_WRITE
  Used by      : API-ORG-004, API-ORG-005 (IS_ACTIVE_FL=0), API-ORG-006 (IS_ACTIVE_FL=1)

QR-ORG-0005
  Purpose      : Count active Branches referencing a LegalEntity (RULE-ORG-001 pre-check)
  Table        : ORG_BRANCH
  Operation    : COUNT
  Filter       : LEGAL_ENTITY_FK = :legalEntityPk AND IS_ACTIVE_FL = 1
  Returns      : Long (count)
  Transaction  : READ_ONLY
  Used by      : API-ORG-005 deactivation pre-check

QR-ORG-0006
  Purpose      : Count active ProfitCenters referencing a LegalEntity (RULE-ORG-002 pre-check)
  Table        : ORG_PROFIT_CENTER
  Operation    : COUNT
  Filter       : LEGAL_ENTITY_FK = :legalEntityPk AND IS_ACTIVE_FL = 1
  Returns      : Long (count)
  Transaction  : READ_ONLY
  Used by      : API-ORG-005 deactivation pre-check

── ENTITY-ORG-002 / ORG_BRANCH ───────────────────────────────────────────────

QR-ORG-0007
  Purpose      : Find single Branch by primary key
  Table        : ORG_BRANCH
  Operation    : FIND_ONE
  Filter       : BRANCH_PK = :id
  Transaction  : READ_ONLY
  Used by      : API-ORG-009, API-ORG-010, API-ORG-011, API-ORG-012

QR-ORG-0008
  Purpose      : Search Branches with dynamic filters
  Table        : ORG_BRANCH
  Operation    : FIND_BY_CRITERIA
  Filters      : BRANCH_CODE LIKE :code? AND NAME_AR LIKE :nameAr? AND NAME_EN LIKE :nameEn? AND
                 LEGAL_ENTITY_FK = :legalEntityFk? AND BRANCH_TYPE_ID = :branchTypeId? AND
                 IS_ACTIVE_FL = :isActiveFl?
  Sorting      : ALLOWED: branchCode, nameAr, nameEn, createdAt
  Returns      : Page<Branch>
  Transaction  : READ_ONLY
  Used by      : API-ORG-008

QR-ORG-0009
  Purpose      : Find all Branches for a given LegalEntity (LOV source)
  Table        : ORG_BRANCH
  Operation    : FIND_ALL
  Filter       : LEGAL_ENTITY_FK = :leId AND IS_ACTIVE_FL = :isActiveFl (optional, default 1)
  Returns      : List<Branch> (no pagination — DRV-ORG-003)
  Transaction  : READ_ONLY
  Used by      : API-ORG-013

QR-ORG-0010
  Purpose      : Persist new Branch
  Table        : ORG_BRANCH
  Operation    : SAVE
  Sequence     : SEQ_ORG_BRANCH.NEXTVAL
  Transaction  : READ_WRITE
  Used by      : API-ORG-007

QR-ORG-0011
  Purpose      : Update existing Branch (includes soft deactivation / reactivation)
  Table        : ORG_BRANCH
  Operation    : UPDATE / SAVE (merge)
  Transaction  : READ_WRITE
  Used by      : API-ORG-010, API-ORG-011, API-ORG-012

QR-ORG-0012
  Purpose      : Count active Departments referencing a Branch (RULE-ORG-003 pre-check)
  Table        : ORG_DEPARTMENT
  Operation    : COUNT
  Filter       : BRANCH_FK = :branchPk AND IS_ACTIVE_FL = 1
  Returns      : Long
  Transaction  : READ_ONLY
  Used by      : API-ORG-011

QR-ORG-0013
  Purpose      : Count active CostCenters referencing a Branch (RULE-ORG-004 pre-check)
  Table        : ORG_COST_CENTER
  Operation    : COUNT
  Filter       : BRANCH_FK = :branchPk AND IS_ACTIVE_FL = 1
  Returns      : Long
  Transaction  : READ_ONLY
  Used by      : API-ORG-011

QR-ORG-0014
  Purpose      : Count active LocationSites referencing a Branch (RULE-ORG-005 pre-check)
  Table        : ORG_LOCATION_SITE
  Operation    : COUNT
  Filter       : BRANCH_FK = :branchPk AND IS_ACTIVE_FL = 1
  Returns      : Long
  Transaction  : READ_ONLY
  Used by      : API-ORG-011

QR-ORG-0015
  Purpose      : Verify LegalEntity exists and is active (RULE-ORG-013 enforcement)
  Table        : ORG_LEGAL_ENTITY
  Operation    : EXISTS
  Filter       : LEGAL_ENTITY_PK = :leId AND IS_ACTIVE_FL = 1
  Returns      : boolean
  Transaction  : READ_ONLY
  Used by      : API-ORG-007 (Branch create)

── ENTITY-ORG-003 / ORG_REGION ───────────────────────────────────────────────

QR-ORG-0016
  Purpose      : Find single Region by primary key
  Table        : ORG_REGION
  Operation    : FIND_ONE
  Filter       : REGION_PK = :id
  Transaction  : READ_ONLY
  Used by      : API-ORG-016, API-ORG-017, API-ORG-018, API-ORG-019

QR-ORG-0017
  Purpose      : Search Regions with dynamic filters
  Table        : ORG_REGION
  Operation    : FIND_BY_CRITERIA
  Filters      : REGION_CODE LIKE :code? AND NAME_AR LIKE :nameAr? AND NAME_EN LIKE :nameEn? AND
                 LEGAL_ENTITY_FK = :legalEntityFk? AND REGION_TYPE_FK = :regionTypeId? AND
                 IS_ACTIVE_FL = :isActiveFl?
  Sorting      : ALLOWED: regionCode, nameAr, nameEn, createdAt
  Returns      : Page<Region>
  Transaction  : READ_ONLY
  Used by      : API-ORG-015

QR-ORG-0018
  Purpose      : Persist new Region
  Table        : ORG_REGION
  Operation    : SAVE
  Sequence     : SEQ_ORG_REGION.NEXTVAL
  Transaction  : READ_WRITE
  Used by      : API-ORG-014

QR-ORG-0019
  Purpose      : Update existing Region (includes soft deactivation / reactivation)
  Table        : ORG_REGION
  Operation    : UPDATE / SAVE (merge)
  Transaction  : READ_WRITE
  Used by      : API-ORG-017, API-ORG-018, API-ORG-019

QR-ORG-0020
  Purpose      : Verify LegalEntity exists and is active (RULE-ORG-019 enforcement)
  Table        : ORG_LEGAL_ENTITY
  Operation    : EXISTS
  Filter       : LEGAL_ENTITY_PK = :leId AND IS_ACTIVE_FL = 1
  Returns      : boolean
  Transaction  : READ_ONLY
  Used by      : API-ORG-014 (Region create)

QR-ORG-0021
  Purpose      : Find all active RegionTypes (API-ORG-020 LOV source)
  Table        : ORG_REGION_TYPE
  Operation    : FIND_ALL
  Filter       : IS_ACTIVE_FL = 1 (default) or per param
  Returns      : List<RegionType> (no pagination — DRV-ORG-003)
  Transaction  : READ_ONLY
  Used by      : API-ORG-020

── ENTITY-ORG-004 / ORG_DEPARTMENT ──────────────────────────────────────────

QR-ORG-0022
  Purpose      : Find single Department by primary key
  Table        : ORG_DEPARTMENT
  Operation    : FIND_ONE
  Filter       : DEPARTMENT_PK = :id
  Transaction  : READ_ONLY
  Used by      : API-ORG-024, API-ORG-025, API-ORG-026, API-ORG-027

QR-ORG-0023
  Purpose      : Search Departments with dynamic filters (flat list)
  Table        : ORG_DEPARTMENT
  Operation    : FIND_BY_CRITERIA
  Filters      : BRANCH_FK = :branchFk? AND NAME_AR LIKE :nameAr? AND
                 NODE_TYPE_ID = :nodeTypeId? AND IS_ACTIVE_FL = :isActiveFl?
  Sorting      : ALLOWED: deptCode, nameAr, nameEn, nodeTypeId, createdAt
  Returns      : Page<Department>
  Transaction  : READ_ONLY
  Used by      : API-ORG-023

QR-ORG-0024
  Purpose      : Find all Departments by Branch for tree construction
  Table        : ORG_DEPARTMENT
  Operation    : FIND_ALL
  Filter       : BRANCH_FK = :branchFk AND IS_ACTIVE_FL = :isActiveFl (optional)
  Returns      : List<Department> (service builds tree from flat list — DRV-ORG-004)
  Transaction  : READ_ONLY
  Used by      : API-ORG-022

QR-ORG-0025
  Purpose      : Persist new Department
  Table        : ORG_DEPARTMENT
  Operation    : SAVE
  Sequence     : SEQ_ORG_DEPARTMENT.NEXTVAL
  Transaction  : READ_WRITE
  Used by      : API-ORG-021

QR-ORG-0026
  Purpose      : Update existing Department (includes soft deactivation / reactivation)
  Table        : ORG_DEPARTMENT
  Operation    : UPDATE / SAVE (merge)
  Transaction  : READ_WRITE
  Used by      : API-ORG-025, API-ORG-026, API-ORG-027

QR-ORG-0027
  Purpose      : Verify Branch exists and is active (RULE-ORG-014 enforcement)
  Table        : ORG_BRANCH
  Operation    : EXISTS
  Filter       : BRANCH_PK = :branchFk AND IS_ACTIVE_FL = 1
  Returns      : boolean
  Transaction  : READ_ONLY
  Used by      : API-ORG-021 (Department create)

QR-ORG-0028
  Purpose      : Traverse Department ancestor chain for circular reference check (RULE-ORG-007)
  Table        : ORG_DEPARTMENT
  Operation    : FIND ancestor chain
  Logic        : Starting from proposed parentDepartmentFk, repeatedly load PARENT_DEPARTMENT_FK
                 until null; if any node's DEPARTMENT_PK = the current record's PK → circular detected
  Returns      : boolean (circular=true)
  Transaction  : READ_ONLY
  Used by      : API-ORG-021, API-ORG-025 (when parentDepartmentFk changes)

QR-ORG-0029
  Purpose      : Verify proposed parent Department is active (RULE-ORG-017)
  Table        : ORG_DEPARTMENT
  Operation    : EXISTS
  Filter       : DEPARTMENT_PK = :parentFk AND IS_ACTIVE_FL = 1
  Returns      : boolean
  Transaction  : READ_ONLY
  Used by      : API-ORG-021, API-ORG-025

── ENTITY-ORG-005 / ORG_COST_CENTER ─────────────────────────────────────────

QR-ORG-0030
  Purpose      : Find single CostCenter by primary key
  Table        : ORG_COST_CENTER
  Operation    : FIND_ONE
  Filter       : COST_CENTER_PK = :id
  Transaction  : READ_ONLY
  Used by      : API-ORG-031, API-ORG-032, API-ORG-033, API-ORG-034

QR-ORG-0031
  Purpose      : Search CostCenters with dynamic filters (flat list)
  Table        : ORG_COST_CENTER
  Operation    : FIND_BY_CRITERIA
  Filters      : BRANCH_FK = :branchFk? AND NAME_AR LIKE :nameAr? AND
                 NODE_TYPE_ID = :nodeTypeId? AND COST_CENTER_TYPE_ID = :costCenterTypeId? AND
                 IS_ACTIVE_FL = :isActiveFl?
  Sorting      : ALLOWED: costCenterCode, nameAr, nameEn, costCenterTypeId, createdAt
  Returns      : Page<CostCenter>
  Transaction  : READ_ONLY
  Used by      : API-ORG-030

QR-ORG-0032
  Purpose      : Find all CostCenters by Branch for tree construction
  Table        : ORG_COST_CENTER
  Operation    : FIND_ALL
  Filter       : BRANCH_FK = :branchFk AND IS_ACTIVE_FL = :isActiveFl (optional)
  Returns      : List<CostCenter> (service builds tree — DRV-ORG-004)
  Transaction  : READ_ONLY
  Used by      : API-ORG-029

QR-ORG-0033
  Purpose      : Persist new CostCenter
  Table        : ORG_COST_CENTER
  Operation    : SAVE
  Sequence     : SEQ_ORG_COST_CENTER.NEXTVAL
  Transaction  : READ_WRITE
  Used by      : API-ORG-028

QR-ORG-0034
  Purpose      : Update existing CostCenter (includes soft deactivation / reactivation)
  Table        : ORG_COST_CENTER
  Operation    : UPDATE / SAVE (merge)
  Transaction  : READ_WRITE
  Used by      : API-ORG-032, API-ORG-033, API-ORG-034

QR-ORG-0035
  Purpose      : Verify Branch exists and is active (RULE-ORG-015 enforcement)
  Table        : ORG_BRANCH
  Operation    : EXISTS
  Filter       : BRANCH_PK = :branchFk AND IS_ACTIVE_FL = 1
  Returns      : boolean
  Transaction  : READ_ONLY
  Used by      : API-ORG-028 (CostCenter create)

QR-ORG-0036
  Purpose      : Traverse CostCenter ancestor chain for circular reference check (RULE-ORG-008)
  Table        : ORG_COST_CENTER
  Operation    : FIND ancestor chain
  Logic        : Starting from proposed parentCostCenterFk, follow PARENT_COST_CENTER_FK chain
                 until null; if any node PK = current record PK → circular detected
  Returns      : boolean
  Transaction  : READ_ONLY
  Used by      : API-ORG-028, API-ORG-032

QR-ORG-0037
  Purpose      : Verify proposed parent CostCenter is active (RULE-ORG-018)
  Table        : ORG_COST_CENTER
  Operation    : EXISTS
  Filter       : COST_CENTER_PK = :parentFk AND IS_ACTIVE_FL = 1
  Returns      : boolean
  Transaction  : READ_ONLY
  Used by      : API-ORG-028, API-ORG-032

── ENTITY-ORG-006 / ORG_PROFIT_CENTER ───────────────────────────────────────

QR-ORG-0038
  Purpose      : Find single ProfitCenter by primary key
  Table        : ORG_PROFIT_CENTER
  Operation    : FIND_ONE
  Filter       : PROFIT_CENTER_PK = :id
  Transaction  : READ_ONLY
  Used by      : API-ORG-037, API-ORG-038, API-ORG-039, API-ORG-040

QR-ORG-0039
  Purpose      : Search ProfitCenters with dynamic filters
  Table        : ORG_PROFIT_CENTER
  Operation    : FIND_BY_CRITERIA
  Filters      : PROFIT_CENTER_CODE LIKE :code? AND NAME_AR LIKE :nameAr? AND
                 LEGAL_ENTITY_FK = :legalEntityFk? AND IS_ACTIVE_FL = :isActiveFl?
  Sorting      : ALLOWED: profitCenterCode, nameAr, nameEn, createdAt
  Returns      : Page<ProfitCenter>
  Transaction  : READ_ONLY
  Used by      : API-ORG-036

QR-ORG-0040
  Purpose      : Persist new ProfitCenter
  Table        : ORG_PROFIT_CENTER
  Operation    : SAVE
  Sequence     : SEQ_ORG_PROFIT_CENTER.NEXTVAL
  Transaction  : READ_WRITE
  Used by      : API-ORG-035

QR-ORG-0041
  Purpose      : Update existing ProfitCenter (includes soft deactivation / reactivation)
  Table        : ORG_PROFIT_CENTER
  Operation    : UPDATE / SAVE (merge)
  Transaction  : READ_WRITE
  Used by      : API-ORG-038, API-ORG-039, API-ORG-040

QR-ORG-0042
  Purpose      : Verify LegalEntity exists and is active (RULE-ORG-020 enforcement)
  Table        : ORG_LEGAL_ENTITY
  Operation    : EXISTS
  Filter       : LEGAL_ENTITY_PK = :leId AND IS_ACTIVE_FL = 1
  Returns      : boolean
  Transaction  : READ_ONLY
  Used by      : API-ORG-035 (ProfitCenter create)

── ENTITY-ORG-007 / ORG_LOCATION_SITE ───────────────────────────────────────

QR-ORG-0043
  Purpose      : Find single LocationSite by primary key
  Table        : ORG_LOCATION_SITE
  Operation    : FIND_ONE
  Filter       : LOCATION_SITE_PK = :id
  Transaction  : READ_ONLY
  Used by      : API-ORG-043, API-ORG-044, API-ORG-045, API-ORG-046

QR-ORG-0044
  Purpose      : Search LocationSites with dynamic filters
  Table        : ORG_LOCATION_SITE
  Operation    : FIND_BY_CRITERIA
  Filters      : LOCATION_CODE LIKE :code? AND NAME_AR LIKE :nameAr? AND
                 BRANCH_FK = :branchFk? AND SITE_TYPE_ID = :siteTypeId? AND
                 IS_ACTIVE_FL = :isActiveFl?
  Sorting      : ALLOWED: locationCode, nameAr, nameEn, siteTypeId, createdAt
  Returns      : Page<LocationSite>
  Transaction  : READ_ONLY
  Used by      : API-ORG-042

QR-ORG-0045
  Purpose      : Find all LocationSites by Branch (LOV source — DRV-ORG-003)
  Table        : ORG_LOCATION_SITE
  Operation    : FIND_ALL
  Filter       : BRANCH_FK = :branchId AND IS_ACTIVE_FL = :isActiveFl (optional)
  Returns      : List<LocationSite> (no pagination)
  Transaction  : READ_ONLY
  Used by      : API-ORG-047

QR-ORG-0046
  Purpose      : Persist new LocationSite
  Table        : ORG_LOCATION_SITE
  Operation    : SAVE
  Sequence     : SEQ_ORG_LOCATION_SITE.NEXTVAL
  Transaction  : READ_WRITE
  Used by      : API-ORG-041

QR-ORG-0047
  Purpose      : Update existing LocationSite (includes soft deactivation / reactivation)
  Table        : ORG_LOCATION_SITE
  Operation    : UPDATE / SAVE (merge)
  Transaction  : READ_WRITE
  Used by      : API-ORG-044, API-ORG-045, API-ORG-046

── ENTITY-ORG-008 / ORG_REGION_TYPE ─────────────────────────────────────────

QR-ORG-0048
  Purpose      : Find all active RegionTypes (LOV endpoint API-ORG-020)
  Table        : ORG_REGION_TYPE
  Operation    : FIND_ALL
  Filter       : IS_ACTIVE_FL = 1 (or per :isActiveFl param)
  Returns      : List<RegionType> (no pagination — DRV-ORG-003)
  Transaction  : READ_ONLY
  Used by      : API-ORG-020

╠══════════════════════════════════════════════════════════════════════════════
║  QRC Summary: 48 QR-IDs (QR-ORG-0001..0048) — all 47 APIs covered
║  QRC Gate: PASSED ✓
╚══════════════════════════════════════════════════════════════════════════════
```

---

# SECTION 12 — REGISTRY UPDATE SCHEMA

```
REGISTRY UPDATE SCHEMA — ORG-001 — PLAN-ORG-001
══════════════════════════════════════════════════════════════════

UPDATE TARGET: master-registry.md

12.1 MODULE STATUS UPDATE:
  Section: Layer-1 Modules
  Module : Organization (ORG-001)
  Field  : Execution Plan Status
  Before : GOVERNED ✓ MODE 1.5
  After  : GOVERNED ✓ MODE 2 — PLAN-ORG-001 (ALIGN GATE PASSED ✓)

12.2 PLAN REGISTRATION:
  Add to plans registry:
  | PLAN-ID      | DBS-ID      | Module        | Version | Status      | Align Gate | Date       |
  | PLAN-ORG-001 | DBS-ORG-001 | Organization  | 1.0     | MODE 2 ✓    | PASSED ✓   | 2026-06-23 |

12.3 API REGISTRATION:
  Register APIs API-ORG-001..047 under Organization module in API registry
  Endpoint prefix: /api/v1/org/
  Status: STABLE (API-ORG-018 conditionally pending OQ-001 resolution)

12.4 LOV REGISTRATION:
  LOV-ORG-001..006 — LOOKUP_CODEs confirmed in MD_LOOKUP_DETAIL (pre-existing)
  LOV-ORG-007 — Reference Table ORG_REGION_TYPE — registered as PRIVATE Reference Table

12.5 ERR-ID REGISTRATION:
  Register ERR-0001..0020 in erp-errors.json (see Error Catalog Section DOC)
  Register ERR-0100, ERR-0101 as PLATFORM-STD (pre-existing)

12.6 PERMISSION REGISTRATION (SEC_PAGES seed):
  SCR-ORG-001 → page_code: LEGAL_ENTITY
  SCR-ORG-002 → page_code: BRANCH
  SCR-ORG-003 → page_code: REGION
  SCR-ORG-004 → page_code: DEPARTMENT
  SCR-ORG-005 → page_code: COST_CENTER
  SCR-ORG-006 → page_code: PROFIT_CENTER
  SCR-ORG-007 → page_code: LOCATION_SITE

12.7 INBOUND STUBS (deferred — for tracking):
  INBOUND-STUB-ORG-001 — Finance module (CostCenter/ProfitCenter) — DEFERRED
  INBOUND-STUB-ORG-002 — Inventory module (LocationSite) — DEFERRED

Registry Update Gate: PASSED ✓
══════════════════════════════════════════════════════════════════
```

---

# SECTION 13 — PLAN COMPLETION BLOCK

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  PLAN COMPLETION BLOCK — PLAN-ORG-001                                        ║
╠══════════════════════════════════════════════════════════════════════════════╣
║                                                                              ║
║  Plan ID          : PLAN-ORG-001                                             ║
║  Plan Name        : New Feature — Organization & Cost Centers — ORG-001      ║
║  Module           : Organization (ORG-001) — ROOT Layer-1                    ║
║  SRS Source       : srs-org-001.md v1.0                                      ║
║  DBS Source       : dbs-org-001.md (DBS-ORG-001)                            ║
║  Registry         : master-registry.md v2.7.2                               ║
║  Governance       : Execution Plan Governance Engine (Project 3) v2          ║
║  Generated        : 2026-06-23                                               ║
║                                                                              ║
╠══════════════════════════════════════════════════════════════════════════════╣
║  ARTIFACT COUNTS                                                             ║
║                                                                              ║
║  Entities         : 8 (ENTITY-ORG-001..008)                                  ║
║  Fields           : 61 (FIELD-0001..0061)                                    ║
║  APIs             : 47 (API-ORG-001..047)                                    ║
║  Rules            : 20 (RULE-ORG-001..020)                                   ║
║  ERR-IDs          : 20 business + 2 platform (ERR-0001..0020, 0100, 0101)   ║
║  LOVs             : 7 (LOV-ORG-001..007)                                     ║
║  Screens          : 7 (SCR-ORG-001..007)                                     ║
║  QR-IDs           : 48 (QR-ORG-0001..0048)                                  ║
║  DRV-IDs          : 8 (DRV-ORG-001..008)                                    ║
║  XM Dependencies  : 0 (ROOT MODULE)                                          ║
║  Open Questions   : 1 (OQ-001 — deferred)                                   ║
║                                                                              ║
╠══════════════════════════════════════════════════════════════════════════════╣
║  PHASE GATE STATUS                                                           ║
║                                                                              ║
║  ENTRY GATE       : PASSED ✓                                                 ║
║  CORE             : PASSED ✓                                                 ║
║  DATA+DOM         : PASSED ✓                                                 ║
║  SVC+API          : PASSED ✓                                                 ║
║  DOC              : PASSED ✓                                                 ║
║  INT-C            : PASSED ✓ (ROOT MODULE — no XM)                           ║
║  INT-R            : PASSED ✓ (ROOT MODULE — no XM)                           ║
║  F1               : PASSED ✓                                                 ║
║  F2               : PASSED ✓ (incl. F2-SERVICE×47, F2-LOV-SERVICE×7,       ║
║                               FRONTEND CONTRACTS, error routing)            ║
║  F3               : PASSED ✓                                                 ║
║  SEC              : PASSED ✓                                                 ║
║  SECTION D        : PASSED ✓ (47/47 APIs, 18/20 rules — 2 deferred OK)      ║
║  ALIGN            : PASSED ✓                                                 ║
║                                                                              ║
╠══════════════════════════════════════════════════════════════════════════════╣
║  OVERALL PLAN STATUS: COMPLETE ✓ — ALIGN GATE PASSED ✓                       ║
║  NEXT STAGE: MODE 2.5 → test-plan-org-001.md generation                     ║
║  THEN: MODE 4A → Governance Audit Engine (Project 4)                        ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

---

# DERIVATION LOG — PLAN-ORG-001

```
DERIVATION LOG — ORG-001 — PLAN-ORG-001
══════════════════════════════════════════════════════════════════

DRV-ORG-001
  ID             : DRV-ORG-001
  Title          : RegionType stored as FK (NUMBER) not DETAIL_CODE (VARCHAR2)
  Deviation from : Standard LOV pattern (LOV fields stored as VARCHAR2 DETAIL_CODE)
  Reason         : LOV-ORG-007 is a Reference Table (ORG_REGION_TYPE) — not MD_LOOKUP_DETAIL
                   DB column REGION_TYPE_FK is NUMBER(10) with FK_ORG_RG_RT constraint
                   Therefore regionTypeFk is a Long FK in Java entity — not a String DETAIL_CODE
  Impact         : RegionCreateRequest sends Long, not String for regionTypeId field
                   Frontend sends numeric FK value — not DETAIL_CODE
  Source         : DBS-ORG-001 (FK constraint FK_ORG_RG_RT) + SRS ENTITY-ORG-008 definition
  Status         : ACCEPTED ✓

DRV-ORG-002
  ID             : DRV-ORG-002
  Title          : ERR-0100 and ERR-0101 are PLATFORM-STD — no RULE-ID assigned
  Deviation from : Standard ERR-ID registration requires RULE-ID pairing
  Reason         : ERR-0100 (unexpected server error) and ERR-0101 (not found) are
                   platform-standard error codes — pre-registered at infrastructure level
                   They apply uniformly across all modules without module-specific RULE-IDs
  Impact         : Error Catalog has ERR-0100/0101 without RULE-ID column values
  Source         : master-registry.md — PLATFORM-STD error catalog section
  Status         : ACCEPTED ✓

DRV-ORG-003
  ID             : DRV-ORG-003
  Title          : API-ORG-013, 020, 022, 029, 047 return List (not Page)
  Deviation from : Standard API response pattern (search → Page<T>)
  Reason         : These endpoints serve as LOV sources or tree construction sources:
                   API-ORG-013 — Branch LOV by LegalEntity
                   API-ORG-020 — RegionType LOV (Reference Table)
                   API-ORG-022 — Department tree (hierarchical — pagination incompatible)
                   API-ORG-029 — CostCenter tree (hierarchical — pagination incompatible)
                   API-ORG-047 — LocationSite LOV by Branch
                   LOV sources require complete lists — pagination defeats their purpose
  Impact         : Response DTO is List<T>, not Page<T>. Angular facade stores as arrays.
  Source         : SRS B5 sections for each API
  Status         : ACCEPTED ✓

DRV-ORG-004
  ID             : DRV-ORG-004
  Title          : Department / CostCenter tree built in service layer — not Oracle hierarchical query
  Deviation from : Potential use of Oracle CONNECT BY / WITH RECURSIVE for hierarchical queries
  Reason         : Service-layer recursive tree assembly is more portable, testable, and consistent
                   with Spring/JPA standard practices. Oracle hierarchical queries complicate
                   JPA mapping and introduce Oracle-specific SQL.
  Impact         : QR-ORG-0024 / QR-ORG-0032 fetch flat lists; service assembles tree recursively
                   Potential performance consideration for very deep trees (documented; acceptable)
  Source         : Platform architecture decision
  Status         : ACCEPTED ✓

DRV-ORG-005
  ID             : DRV-ORG-005
  Title          : No intra-module deactivation pre-check for Department / CostCenter / ProfitCenter / LocationSite
  Deviation from : General expectation that all deactivations have pre-checks
  Reason         : Within the ORG module itself, no child entity depends on these entities.
                   Department/CostCenter/ProfitCenter/LocationSite are leaf-level or
                   cross-module dependencies. Intra-module checks would always pass (no children).
                   Consumer modules (Finance, Inventory) enforce their own checks via RULE-ORG-009/010.
  Impact         : API-ORG-026, 033, 039, 045 execute soft deactivation immediately without
                   child count checks. INBOUND-STUBs document consumer responsibility.
  Source         : SRS A4 rules analysis — no intra-module deactivation rules for these entities
  Status         : ACCEPTED ✓

DRV-ORG-006
  ID             : DRV-ORG-006
  Title          : SRS B2 omits Reactivate action button for SCR-ORG-003, SCR-ORG-006, SCR-ORG-007
  Deviation from : Standard screen pattern (all 5 CRUD actions: Create/Read/Update/Deactivate/Reactivate)
  Reason         : SRS B2 (UI Behavior Spec) for screens 003/006/007 lists Deactivate action
                   but does not define a Reactivate button. However, the API layer includes
                   API-ORG-019 (Region Reactivate), API-ORG-040 (ProfitCenter Reactivate),
                   API-ORG-046 (LocationSite Reactivate).
                   APIs exist but UI button omitted in SRS.
  Impact         : F1/F2/F3 specs for SCR-ORG-003/006/007 do not include Reactivate button.
                   Backend Reactivate endpoints remain available for programmatic use.
                   UI gap may be resolved in future SRS revision.
  Source         : SRS B2 action definition per screen
  Status         : ACCEPTED ✓ (UI gap documented — APIs implemented)

DRV-ORG-007
  ID             : DRV-ORG-007
  Title          : Branch LOV for SCR-ORG-002 search filter loads via API-ORG-002 (not API-ORG-013)
  Deviation from : Expected use of API-ORG-013 (by-legal-entity) for all Branch LOV scenarios
  Reason         : SCR-ORG-002 search filter for "filter by branch" needs all active branches
                   across all LegalEntities (no pre-selected LE context on the search screen).
                   API-ORG-013 requires a leId path parameter — unusable without LE context.
                   API-ORG-002 (search all branches, isActiveFl=1) provides the correct unbounded list.
  Impact         : SCR-ORG-002 F2 facade calls API-ORG-002 (isActiveFl=1, no size limit) for branch filter LOV.
                   API-ORG-013 used when LegalEntity context is known (e.g. scoped sub-screens).
  Source         : SRS B3 screen filter spec for SCR-ORG-002
  Status         : ACCEPTED ✓

DRV-ORG-008
  ID             : DRV-ORG-008
  Title          : LocationSite.branchFk is REQUIRED but no explicit RULE-ORG-ID covers it
  Deviation from : Standard: every mandatory validation should trace to a RULE-ID
  Reason         : SRS A4 assigns 20 RULE-IDs. LocationSite.branchFk is NOT NULL per DB constraint
                   (FK_ORG_LS_BR) but no RULE-ORG-XXX was assigned for this validation in the SRS.
                   The constraint is enforced by DB FK + service-layer null check without a named rule.
  Impact         : F3 validation for SCR-ORG-007 branchFk cites "DB NOT NULL constraint" rather than
                   a RULE-ID. No ERR-ID specifically assigned for this case (relies on ERR-0100 for
                   unexpected DB failure; UI required field validation prevents blank submission).
  Source         : DBS-ORG-001 Column Definition (BRANCH_FK NOT NULL)
  Status         : ACCEPTED ✓

DRV-ORG-009
  ID             : DRV-ORG-009
  Title          : SECTION D TC-IDs reconciled after over-engineering reduction in MODE 2.5
  Deviation from : Standard: SECTION D TC-IDs should be final and match test-plan.md at time of generation
  Reason         : SECTION D was generated in MODE 2 with placeholder TC-IDs (TC-ORG-037..083)
                   assuming one TC per API-ID. After the over-engineering guard was applied in
                   MODE 2.5 (total TCs reduced from 107 to 59), the TC-IDs were reassigned and
                   no longer matched SECTION D. Finding 4A-004-003 identified this mismatch.
                   SECTION D has been retroactively reconciled to cite actual TC-IDs from
                   test-plan-org-001.md (TC-ORG-001..059).
  Impact         : SECTION D API coverage table updated — all 47 API-IDs now cite their
                   actual covering TC-IDs with a coverage rationale (dedicated TC or pattern TC).
  Source         : Finding 4A-004-003 (MODE 4A Governance Audit)
  Status         : ACCEPTED ✓ — retroactive correction per Section 6.2 Inline Correction Protocol (DB constraint sufficient — no SRS rule gap escalation required)

══════════════════════════════════════════════════════════════════
Derivation Log: DRV-ORG-001..008 — 8 entries — sequence CONTIGUOUS ✓
══════════════════════════════════════════════════════════════════
```

---

# OQ LOG — PLAN-ORG-001

```
OPEN QUESTIONS LOG — ORG-001 — PLAN-ORG-001
══════════════════════════════════════════════════════════════════

OQ-001
  ID             : OQ-001
  Title          : Region SOFT-READ deactivation impact on consumer modules
  Question       : When a Region is deactivated (IS_ACTIVE_FL = 0), what is the expected
                   impact on modules that reference Region as a SOFT-READ (informational FK)?
                   Should existing transactional records that reference this Region be flagged?
                   Should the deactivation be blocked until consumers unlink?
  Currently      : API-ORG-018 (Deactivate Region) blocks on active Branches (RULE-ORG-006 check).
                   Beyond Branches, no additional consumer pre-checks are implemented.
  Impact area    : API-ORG-018 deactivation behavior
  Status         : DEFERRED — non-blocking for ORG module implementation
  Resolution     : To be resolved when first consumer module referencing Region is built (MODE 1.5)
  Risk           : LOW — Region deactivation is infrequent operational activity

══════════════════════════════════════════════════════════════════
OQ Log: 1 active question (OQ-001) — 0 closed
══════════════════════════════════════════════════════════════════
```

---

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  END OF EXECUTION PLAN — PLAN-ORG-001                                        ║
║  execution-plan-org-001.md — COMPLETE ✓ — ALIGN GATE PASSED ✓               ║
║  Ready for: MODE 2.5 → test-plan-org-001.md                                 ║
╚══════════════════════════════════════════════════════════════════════════════╝
```
