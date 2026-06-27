<!-- Source: PHASE:DATA-DOM / SUB:MASTER -->


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

