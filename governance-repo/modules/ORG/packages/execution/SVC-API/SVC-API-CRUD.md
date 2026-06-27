<!-- Source: PHASE:SVC-API / SUB:CRUD -->


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

