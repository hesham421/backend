<!-- Source: MARK:JUNIT / SUB:RULE-SCENARIOS -->


## PHASE:JUNIT — RULE-ORG-001..002 (LegalEntity Deactivation)

<!-- TC:TC-ORG-001:START -->
TC-ORG-001 — LegalEntity deactivation succeeds when no active Branches or ProfitCenters
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-005
RULE-ID      : RULE-ORG-001, RULE-ORG-002
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : A LegalEntity (legalEntityPk=1) exists with isActiveFl=1
                Zero active Branches reference it (isActiveFl=1 count=0)
                Zero active ProfitCenters reference it (isActiveFl=1 count=0)
                Authenticated user has PERM_LEGAL_ENTITY_DELETE
When          : DELETE /api/v1/org/legal-entities/1
Then          : HTTP 200 returned
                LegalEntity.isActiveFl = 0 in DB (soft delete only)
                Response contains messageAr + messageEn confirmation
                No physical row removed from ORG_LEGAL_ENTITY

ERR-ID        : —
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-001:END -->

<!-- TC:TC-ORG-002:START -->
TC-ORG-002 — LegalEntity deactivation blocked by active Branch (MANDATORY-J-6)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-005
RULE-ID      : RULE-ORG-001
SCR-ID       : SCR-ORG-001
ERR-ID       : ERR-0001
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : LegalEntity (legalEntityPk=1) with isActiveFl=1
                At least one Branch with LEGAL_ENTITY_FK=1 and isActiveFl=1 exists
                Authenticated user has PERM_LEGAL_ENTITY_DELETE
When          : DELETE /api/v1/org/legal-entities/1
Then          : HTTP 409 returned
                Response body: { messageAr: "لا يمكن تعطيل الكيان القانوني لوجود فروع نشطة مرتبطة به. يرجى تعطيل جميع الفروع أولاً.", messageEn: "Cannot deactivate Legal Entity: active branches exist. Please deactivate all branches first." }
                LegalEntity.isActiveFl remains 1 in DB (not modified)

ERR-ID        : ERR-0001
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-002:END -->

<!-- TC:TC-ORG-003:START -->
TC-ORG-003 — LegalEntity deactivation blocked by active ProfitCenter
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-005
RULE-ID      : RULE-ORG-002
SCR-ID       : SCR-ORG-001
ERR-ID       : ERR-0002
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : LegalEntity (legalEntityPk=2) with isActiveFl=1
                Zero active Branches
                At least one ProfitCenter with LEGAL_ENTITY_FK=2 and isActiveFl=1
                Authenticated user has PERM_LEGAL_ENTITY_DELETE
When          : DELETE /api/v1/org/legal-entities/2
Then          : HTTP 409 returned
                Response body: { messageAr: "لا يمكن تعطيل الكيان القانوني لوجود مراكز ربح نشطة مرتبطة به. يرجى تعطيل جميع مراكز الربح أولاً.", messageEn: "Cannot deactivate Legal Entity: active profit centers exist. Please deactivate all profit centers first." }
                LegalEntity.isActiveFl remains 1

ERR-ID        : ERR-0002
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-003:END -->

---

## PHASE:JUNIT — RULE-ORG-003..005 (Branch Deactivation)

<!-- TC:TC-ORG-004:START -->
TC-ORG-004 — Branch deactivation succeeds when no active children
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-011
RULE-ID      : RULE-ORG-003, RULE-ORG-004, RULE-ORG-005
SCR-ID       : SCR-ORG-002
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Branch (branchPk=10) with isActiveFl=1
                Zero active Departments, CostCenters, LocationSites referencing it
                Authenticated user has PERM_BRANCH_DELETE
When          : DELETE /api/v1/org/branches/10
Then          : HTTP 200 returned
                Branch.isActiveFl = 0 in DB

ERR-ID        : —
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-004:END -->

<!-- TC:TC-ORG-005:START -->
TC-ORG-005 — Branch deactivation blocked by active Department
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-011
RULE-ID      : RULE-ORG-003
SCR-ID       : SCR-ORG-002
ERR-ID       : ERR-0003
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : Branch (branchPk=10) with isActiveFl=1
                At least one Department with BRANCH_FK=10 and isActiveFl=1
When          : DELETE /api/v1/org/branches/10
Then          : HTTP 409 returned
                messageAr: "لا يمكن تعطيل الفرع لوجود أقسام نشطة مرتبطة به. يرجى تعطيل جميع الأقسام أولاً."
                messageEn: "Cannot deactivate Branch: active departments exist. Please deactivate all departments first."
                Branch.isActiveFl remains 1

ERR-ID        : ERR-0003
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-005:END -->

<!-- TC:TC-ORG-006:START -->
TC-ORG-006 — Branch deactivation blocked by active CostCenter
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-011
RULE-ID      : RULE-ORG-004
SCR-ID       : SCR-ORG-002
ERR-ID       : ERR-0004
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : Branch (branchPk=10) — zero Departments — at least one active CostCenter with BRANCH_FK=10
When          : DELETE /api/v1/org/branches/10
Then          : HTTP 409 returned
                messageAr: "لا يمكن تعطيل الفرع لوجود مراكز تكلفة نشطة مرتبطة به. يرجى تعطيل جميع مراكز التكلفة أولاً."
                messageEn: "Cannot deactivate Branch: active cost centers exist. Please deactivate all cost centers first."

ERR-ID        : ERR-0004
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-006:END -->

<!-- TC:TC-ORG-007:START -->
TC-ORG-007 — Branch deactivation blocked by active LocationSite
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-011
RULE-ID      : RULE-ORG-005
SCR-ID       : SCR-ORG-002
ERR-ID       : ERR-0005
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : Branch (branchPk=10) — zero Depts/CostCenters — at least one active LocationSite with BRANCH_FK=10
When          : DELETE /api/v1/org/branches/10
Then          : HTTP 409 returned
                messageAr: "لا يمكن تعطيل الفرع لوجود مواقع جغرافية نشطة مرتبطة به. يرجى تعطيل جميع المواقع أولاً."
                messageEn: "Cannot deactivate Branch: active location sites exist. Please deactivate all location sites first."

ERR-ID        : ERR-0005
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-007:END -->

---

## PHASE:JUNIT — RULE-ORG-006 (Region Deactivation)

<!-- TC:TC-ORG-008:START -->
TC-ORG-008 — Region deactivation succeeds when no active Branches reference it
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-018
RULE-ID      : RULE-ORG-006
SCR-ID       : SCR-ORG-003
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Region (regionPk=5) with isActiveFl=1
                Zero active Branches reference this Region
When          : DELETE /api/v1/org/regions/5
Then          : HTTP 200 returned
                Region.isActiveFl = 0 in DB
                OQ-001 note: full SOFT-READ consumer check deferred

ERR-ID        : —
Language      : BOTH
Test-Hint     : —
XM-impact     : OQ-001 — SOFT-READ consumer impact deferred
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-008:END -->

<!-- TC:TC-ORG-009:START -->
TC-ORG-009 — Region deactivation blocked by active Branch reference
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-018
RULE-ID      : RULE-ORG-006
SCR-ID       : SCR-ORG-003
ERR-ID       : ERR-0006
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : Region (regionPk=5) with isActiveFl=1
                At least one Branch with a reference to this Region and isActiveFl=1
When          : DELETE /api/v1/org/regions/5
Then          : HTTP 409 returned
                messageAr: "لا يمكن تعطيل المنطقة لوجود فروع نشطة مرتبطة بها. يرجى إلغاء ربط الفروع أولاً."
                messageEn: "Cannot deactivate Region: active branches reference it. Please unlink branches first."
                Region.isActiveFl remains 1

ERR-ID        : ERR-0006
Language      : BOTH
Test-Hint     : —
XM-impact     : OQ-001 — SOFT-READ consumer impact deferred
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-009:END -->

---

## PHASE:JUNIT — RULE-ORG-007 (Department Circular Reference)

<!-- TC:TC-ORG-010:START -->
TC-ORG-010 — Department created with valid parent (no circular reference)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-021
RULE-ID      : RULE-ORG-007
SCR-ID       : SCR-ORG-004
ERR-ID       : —
LOV-ID       : LOV-ORG-003
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Branch (branchPk=10) active
                Department A (departmentPk=100) exists under this Branch
                Department B to be created as child of A
When          : POST /api/v1/org/departments
                Body: { nameAr: "قسم ب", nameEn: "Dept B", branchFk: 10, parentDepartmentFk: 100, nodeTypeId: "DETAIL" }
Then          : HTTP 201 returned
                New Department created with parentDepartmentFk=100
                deptCode auto-generated (format: DEP-[BR]-NNNNN)
                No circular reference detected

ERR-ID        : —
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-010:END -->

<!-- TC:TC-ORG-011:START -->
TC-ORG-011 — Department update blocked when circular parent reference detected
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-025
RULE-ID      : RULE-ORG-007
SCR-ID       : SCR-ORG-004
ERR-ID       : ERR-0007
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : Department A (pk=100) is parent of Department B (pk=200)
                Department B is parent of Department C (pk=300)
                Authenticated user has PERM_DEPARTMENT_UPDATE
When          : PUT /api/v1/org/departments/100
                Body: { parentDepartmentFk: 300 }
                (attempting to set A's parent to C — which is A's own grandchild)
Then          : HTTP 409 returned
                messageAr: "لا يمكن تعيين هذا القسم أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري."
                messageEn: "Cannot set this department as parent: circular reference detected in department hierarchy."
                Department A.parentDepartmentFk not modified

ERR-ID        : ERR-0007
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-011:END -->

---

## PHASE:JUNIT — RULE-ORG-008 (CostCenter Circular Reference)

<!-- TC:TC-ORG-012:START -->
TC-ORG-012 — CostCenter created with valid parent (no circular reference)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-028
RULE-ID      : RULE-ORG-008
SCR-ID       : SCR-ORG-005
ERR-ID       : —
LOV-ID       : LOV-ORG-004, LOV-ORG-005
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Branch (branchPk=10) active
                CostCenter X (costCenterPk=50) exists
When          : POST /api/v1/org/cost-centers
                Body: { nameAr: "مركز ب", nameEn: "CC B", branchFk: 10, parentCostCenterFk: 50, nodeTypeId: "DETAIL", costCenterTypeId: "DIRECT" }
Then          : HTTP 201 returned
                New CostCenter created with parentCostCenterFk=50
                costCenterCode auto-generated

ERR-ID        : —
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-012:END -->

<!-- TC:TC-ORG-013:START -->
TC-ORG-013 — CostCenter update blocked on circular parent reference
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-032
RULE-ID      : RULE-ORG-008
SCR-ID       : SCR-ORG-005
ERR-ID       : ERR-0008
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : CostCenter X (pk=50) → child Y (pk=51) → child Z (pk=52)
When          : PUT /api/v1/org/cost-centers/50
                Body: { parentCostCenterFk: 52 }
Then          : HTTP 409 returned
                messageAr: "لا يمكن تعيين مركز التكلفة هذا أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري."
                messageEn: "Cannot set this cost center as parent: circular reference detected in cost center hierarchy."

ERR-ID        : ERR-0008
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-013:END -->

---

## PHASE:JUNIT — RULE-ORG-011 (Business Code Immutability)

<!-- TC:TC-ORG-014:START -->
TC-ORG-014 — Business Code absent from Update DTO (happy path)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-004
RULE-ID      : RULE-ORG-011
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Business Code
Data class    : VALID

Given         : LegalEntity (legalEntityPk=1, legalEntityCode="LE-00001") with isActiveFl=1
                Authenticated user has PERM_LEGAL_ENTITY_UPDATE
When          : PUT /api/v1/org/legal-entities/1
                Body: { nameAr: "اسم جديد", nameEn: "New Name" }
                (legalEntityCode NOT included in request body)
Then          : HTTP 200 returned
                LegalEntity.legalEntityCode remains "LE-00001" unchanged in DB
                Response includes legalEntityCode="LE-00001" (unchanged)

ERR-ID        : —
Language      : BOTH
Test-Hint     : Verify Business Code field is absent from Update DTO entirely — not just validated
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-014:END -->

<!-- TC:TC-ORG-015:START -->
TC-ORG-015 — Business Code modification attempt rejected
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-004
RULE-ID      : RULE-ORG-011
SCR-ID       : SCR-ORG-001
ERR-ID       : ERR-0011
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : LegalEntity (legalEntityPk=1, legalEntityCode="LE-00001")
                Authenticated user has PERM_LEGAL_ENTITY_UPDATE
When          : PUT /api/v1/org/legal-entities/1
                Body includes legalEntityCode: "LE-99999" (attempt to override)
Then          : HTTP 400 returned
                messageAr: "رمز الأعمال لا يمكن تعديله بعد الإنشاء الأول — هذه القيمة ثابتة نهائياً."
                messageEn: "Business code is immutable after creation and cannot be modified."
                DB value unchanged

ERR-ID        : ERR-0011
Language      : BOTH
Test-Hint     : Verify Business Code field is absent from Update DTO entirely — not just validated
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-015:END -->

<!-- TC:TC-ORG-016:START -->
TC-ORG-016 — Business Code field verified absent from UpdateRequest DTO schema (Boundary / Test-Hint)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-004
RULE-ID      : RULE-ORG-011
SCR-ID       : SCR-ORG-001
ERR-ID       : ERR-0011
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Boundary
Data class    : BOUNDARY

Given         : LegalEntityUpdateRequest class definition
When          : Inspect DTO class fields (unit test — reflection or schema inspection)
Then          : legalEntityCode field is NOT present in LegalEntityUpdateRequest class
                Field cannot be deserialized from request body (Jackson ignores unknown OR schema blocks it)
                Sending legalEntityCode in body → field silently ignored OR HTTP 400
                In either case: DB value never modified by this field

ERR-ID        : ERR-0011
Language      : —
Test-Hint     : تحقق من أن حقل الرمز غير موجود في Update DTO بالكلية — لا مجرد validation
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-016:END -->

---

## PHASE:JUNIT — RULE-ORG-012 (Business Code Uniqueness + NumberingEngine)

<!-- TC:TC-ORG-017:START -->
TC-ORG-017 — Business Code auto-generated on create (MANDATORY-J-1)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-001
RULE-ID      : RULE-ORG-012, RULE-ORG-016
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : LOV-ORG-001
─────────────────────────────────────────────────────────────────
Scenario type : Business Code
Data class    : VALID

Given         : No LegalEntity with code "LE-00001" exists
                Authenticated user has PERM_LEGAL_ENTITY_CREATE
When          : POST /api/v1/org/legal-entities
                Body: { nameAr: "كيان قانوني اختبار", nameEn: "Test Legal Entity", entityTypeId: "HEAD_OFFICE" }
                (legalEntityCode NOT in request body)
Then          : HTTP 201 returned
                Response contains legalEntityCode in format "LE-NNNNN"
                legalEntityCode was NOT sent in request body (auto-generated by NumberingEngine)
                SEQ_ORG_LEGAL_ENTITY used for PK generation
                legalEntityCode stored in LEGAL_ENTITY_CODE column (UQ_ORG_LE_CODE enforced)

ERR-ID        : —
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-017:END -->

<!-- TC:TC-ORG-018:START -->
TC-ORG-018 — Business Code collision handled (NumberingEngine retry)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-001
RULE-ID      : RULE-ORG-012
SCR-ID       : SCR-ORG-001
ERR-ID       : ERR-0012
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : NumberingEngine generates a code that already exists in LEGAL_ENTITY_CODE
                (simulated by mocking NumberingEngine to return duplicate code)
When          : POST /api/v1/org/legal-entities
                Body: { nameAr: "كيان", nameEn: "Entity", entityTypeId: "HEAD_OFFICE" }
Then          : HTTP 409 returned
                messageAr: "رمز الأعمال المُنشأ تلقائياً موجود مسبقاً. يرجى المحاولة مجدداً."
                messageEn: "Generated business code already exists. Please retry the operation."

ERR-ID        : ERR-0012
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-018:END -->

---

## PHASE:JUNIT — RULE-ORG-013 (Branch requires active LegalEntity)

<!-- TC:TC-ORG-019:START -->
TC-ORG-019 — Branch created with valid active LegalEntity
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-007
RULE-ID      : RULE-ORG-013
SCR-ID       : SCR-ORG-002
ERR-ID       : —
LOV-ID       : LOV-ORG-002
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : LegalEntity (legalEntityPk=1) with isActiveFl=1
                Authenticated user has PERM_BRANCH_CREATE
When          : POST /api/v1/org/branches
                Body: { nameAr: "فرع رئيسي", nameEn: "Main Branch", legalEntityFk: 1, branchTypeId: "MAIN_BRANCH" }
Then          : HTTP 201 returned
                Branch created with branchCode auto-generated (format: BR-[LE]-NNNNN)
                Branch.legalEntityFk = 1 stored

ERR-ID        : —
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-019:END -->

<!-- TC:TC-ORG-020:START -->
TC-ORG-020 — Branch creation blocked when LegalEntity inactive
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-007
RULE-ID      : RULE-ORG-013
SCR-ID       : SCR-ORG-002
ERR-ID       : ERR-0013
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : LegalEntity (legalEntityPk=99) with isActiveFl=0
When          : POST /api/v1/org/branches
                Body: { nameAr: "فرع", nameEn: "Branch", legalEntityFk: 99, branchTypeId: "MAIN_BRANCH" }
Then          : HTTP 400 returned
                messageAr: "يرجى اختيار كيان قانوني نشط لربط الفرع به."
                messageEn: "A valid active Legal Entity must be selected before saving a Branch."
                No Branch row created in DB

ERR-ID        : ERR-0013
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-020:END -->

---

## PHASE:JUNIT — RULE-ORG-014 (Department requires active Branch)

<!-- TC:TC-ORG-021:START -->
TC-ORG-021 — Department creation blocked when Branch inactive
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-021
RULE-ID      : RULE-ORG-014
SCR-ID       : SCR-ORG-004
ERR-ID       : ERR-0014
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : Branch (branchPk=99) with isActiveFl=0
When          : POST /api/v1/org/departments
                Body: { nameAr: "قسم", nameEn: "Dept", branchFk: 99, nodeTypeId: "DETAIL" }
Then          : HTTP 400 returned
                messageAr: "يرجى اختيار فرع نشط لربط القسم به."
                messageEn: "A valid active Branch must be selected before saving a Department."

ERR-ID        : ERR-0014
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-021:END -->

---

## PHASE:JUNIT — RULE-ORG-015 (CostCenter requires active Branch)

<!-- TC:TC-ORG-022:START -->
TC-ORG-022 — CostCenter creation blocked when Branch inactive
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-028
RULE-ID      : RULE-ORG-015
SCR-ID       : SCR-ORG-005
ERR-ID       : ERR-0015
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : Branch (branchPk=99) with isActiveFl=0
When          : POST /api/v1/org/cost-centers
                Body: { nameAr: "مركز", nameEn: "CC", branchFk: 99, nodeTypeId: "DETAIL", costCenterTypeId: "DIRECT" }
Then          : HTTP 400 returned
                messageAr: "يرجى اختيار فرع نشط لربط مركز التكلفة به."
                messageEn: "A valid active Branch must be selected before saving a CostCenter."

ERR-ID        : ERR-0015
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-022:END -->

---

## PHASE:JUNIT — RULE-ORG-017 (Department parent must be active)

<!-- TC:TC-ORG-023:START -->
TC-ORG-023 — Department create succeeds with active parent
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-021
RULE-ID      : RULE-ORG-017
SCR-ID       : SCR-ORG-004
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Parent Department (departmentPk=100) with isActiveFl=1
                Branch (branchPk=10) active
When          : POST /api/v1/org/departments
                Body: { nameAr: "قسم فرعي", nameEn: "Sub Dept", branchFk: 10, parentDepartmentFk: 100, nodeTypeId: "DETAIL" }
Then          : HTTP 201 returned
                Department created with parentDepartmentFk=100

ERR-ID        : —
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-023:END -->

<!-- TC:TC-ORG-024:START -->
TC-ORG-024 — Department create blocked when parent is inactive
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-021
RULE-ID      : RULE-ORG-017
SCR-ID       : SCR-ORG-004
ERR-ID       : ERR-0017
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : Parent Department (departmentPk=100) with isActiveFl=0
When          : POST /api/v1/org/departments
                Body: { nameAr: "قسم", nameEn: "Dept", branchFk: 10, parentDepartmentFk: 100, nodeTypeId: "DETAIL" }
Then          : HTTP 400 returned
                messageAr: "لا يمكن تعيين قسم غير نشط أباً للقسم. يرجى اختيار قسم نشط."
                messageEn: "Cannot set an inactive department as parent. Please select an active department."

ERR-ID        : ERR-0017
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-024:END -->

---

## PHASE:JUNIT — RULE-ORG-018 (CostCenter parent must be active)

<!-- TC:TC-ORG-025:START -->
TC-ORG-025 — CostCenter create succeeds with active parent
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-028
RULE-ID      : RULE-ORG-018
SCR-ID       : SCR-ORG-005
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Parent CostCenter (costCenterPk=50) with isActiveFl=1
                Branch (branchPk=10) active
When          : POST /api/v1/org/cost-centers
                Body: { nameAr: "مركز فرعي", nameEn: "Sub CC", branchFk: 10, parentCostCenterFk: 50, nodeTypeId: "DETAIL", costCenterTypeId: "DIRECT" }
Then          : HTTP 201 returned
                CostCenter created with parentCostCenterFk=50

ERR-ID        : —
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-025:END -->

<!-- TC:TC-ORG-026:START -->
TC-ORG-026 — CostCenter create blocked when parent is inactive
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-028
RULE-ID      : RULE-ORG-018
SCR-ID       : SCR-ORG-005
ERR-ID       : ERR-0018
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : Parent CostCenter (costCenterPk=50) with isActiveFl=0
When          : POST /api/v1/org/cost-centers
                Body: { nameAr: "مركز", nameEn: "CC", branchFk: 10, parentCostCenterFk: 50, nodeTypeId: "DETAIL", costCenterTypeId: "DIRECT" }
Then          : HTTP 400 returned
                messageAr: "لا يمكن تعيين مركز تكلفة غير نشط أباً لمركز التكلفة. يرجى اختيار مركز تكلفة نشط."
                messageEn: "Cannot set an inactive cost center as parent. Please select an active cost center."

ERR-ID        : ERR-0018
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-026:END -->

---

## PHASE:JUNIT — RULE-ORG-019 (Region requires active LegalEntity)

<!-- TC:TC-ORG-027:START -->
TC-ORG-027 — Region created with valid active LegalEntity and RegionType
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-014
RULE-ID      : RULE-ORG-019
SCR-ID       : SCR-ORG-003
ERR-ID       : —
LOV-ID       : LOV-ORG-007
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : LegalEntity (legalEntityPk=1) with isActiveFl=1
                RegionType (regionTypePk=3) with isActiveFl=1
                Authenticated user has PERM_REGION_CREATE
When          : POST /api/v1/org/regions
                Body: { nameAr: "منطقة الشمال", nameEn: "North Region", legalEntityFk: 1, regionTypeId: 3 }
Then          : HTTP 201 returned
                Region created with regionCode auto-generated (format: RG-[LE]-NNNNN)
                Region.regionTypeFk=3 stored as FK NUMBER

ERR-ID        : —
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-027:END -->

<!-- TC:TC-ORG-028:START -->
TC-ORG-028 — Region creation blocked when LegalEntity inactive
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-014
RULE-ID      : RULE-ORG-019
SCR-ID       : SCR-ORG-003
ERR-ID       : ERR-0019
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : LegalEntity (legalEntityPk=99) with isActiveFl=0
When          : POST /api/v1/org/regions
                Body: { nameAr: "منطقة", nameEn: "Region", legalEntityFk: 99, regionTypeId: 3 }
Then          : HTTP 400 returned
                messageAr: "يرجى اختيار كيان قانوني نشط لربط المنطقة به."
                messageEn: "A valid active Legal Entity must be selected before saving a Region."

ERR-ID        : ERR-0019
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-028:END -->

---

## PHASE:JUNIT — RULE-ORG-020 (ProfitCenter requires active LegalEntity)

<!-- TC:TC-ORG-029:START -->
TC-ORG-029 — ProfitCenter created with valid active LegalEntity
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-035
RULE-ID      : RULE-ORG-020
SCR-ID       : SCR-ORG-006
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : LegalEntity (legalEntityPk=1) with isActiveFl=1
                Authenticated user has PERM_PROFIT_CENTER_CREATE
When          : POST /api/v1/org/profit-centers
                Body: { nameAr: "مركز ربح رئيسي", nameEn: "Main Profit Center", legalEntityFk: 1 }
Then          : HTTP 201 returned
                ProfitCenter created with profitCenterCode auto-generated (format: PC-[LE]-NNNNN)

ERR-ID        : —
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-029:END -->

<!-- TC:TC-ORG-030:START -->
TC-ORG-030 — ProfitCenter creation blocked when LegalEntity inactive
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-035
RULE-ID      : RULE-ORG-020
SCR-ID       : SCR-ORG-006
ERR-ID       : ERR-0020
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : LegalEntity (legalEntityPk=99) with isActiveFl=0
When          : POST /api/v1/org/profit-centers
                Body: { nameAr: "مركز", nameEn: "PC", legalEntityFk: 99 }
Then          : HTTP 400 returned
                messageAr: "يرجى اختيار كيان قانوني نشط لربط مركز الربح به."
                messageEn: "A valid active Legal Entity must be selected before saving a ProfitCenter."

ERR-ID        : ERR-0020
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-030:END -->

---

## PHASE:JUNIT — API-ID Representative Scenarios (TP-SEC-2 additive)


<!-- TC:TC-ORG-059:START -->
TC-ORG-059 — Business Code immutability: field absent at DTO schema level (MANDATORY-J-2)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-004
RULE-ID      : RULE-ORG-011
SCR-ID       : SCR-ORG-001
ERR-ID       : ERR-0011
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Business Code
Data class    : INVALID

Given         : LegalEntity (legalEntityPk=1, legalEntityCode="LE-00001") with isActiveFl=1
                Authenticated user has PERM_LEGAL_ENTITY_UPDATE
When          : PUT /api/v1/org/legal-entities/1
                Request body contains legalEntityCode: "LE-CHANGED"
                (attempting to modify Business Code via update endpoint)
Then          : Business Code modification must be impossible — either:
                (a) HTTP 400 returned with ERR-0011
                    messageAr: "رمز الأعمال لا يمكن تعديله بعد الإنشاء الأول — هذه القيمة ثابتة نهائياً."
                    messageEn: "Business code is immutable after creation and cannot be modified."
                OR (b) HTTP 200 returned and legalEntityCode in DB is still "LE-00001" — field silently ignored
                IN BOTH CASES: legalEntityCode in DB must be "LE-00001" (unchanged)
                ⚠ MANDATORY-J-2 distinguishes this from TC-ORG-015 (which tests the error message)
                   This TC verifies the invariant at the DB level regardless of HTTP response code
                Test-Hint: verify legalEntityCode was NOT sent in the request body in the first place
                           per RULE-ORG-011 — field should be structurally absent from UpdateRequest DTO

ERR-ID        : ERR-0011
Language      : BOTH
Test-Hint     : تحقق من أن حقل الرمز غير موجود في Update DTO بالكلية — لا مجرد validation
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-059:END -->

