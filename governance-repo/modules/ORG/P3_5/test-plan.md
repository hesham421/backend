# TEST PLAN — Organization & Cost Centers (ORG-001)

```
test-plan.md — Organization & Cost Centers — PLAN-ID: PLAN-ORG-001
══════════════════════════════════════════════════════════════════
Source artifacts:
  execution-plan.md : PLAN-ORG-001 — Gate ALIGN ✓ confirmed
  srs.md            : srs-org-001.md v1.0
  db-script.md      : dbs-org-001.md (DBS-ORG-001)
Open Questions: 1 active (OQ-001) — see OQ Log in execution-plan.md
Mode              : MODE 2.5 — TEST TRUTH
Governance        : Execution Plan Governance Engine (Project 3) v2 Section 16
TC namespace      : TC-ORG-[001..N]
Generated         : 2026-06-24
══════════════════════════════════════════════════════════════════
```

---

## MODE 2.5 — ENTRY GATE

```
╔══════════════════════════════════════════════════════════════════╗
║                MODE 2.5 — TEST PLAN ENTRY GATE                   ║
╠══════════════════════════════════════════════════════════════════╣
║ execution-plan.md uploaded?          ║ ✓ — PLAN-ORG-001          ║
║ Gate ALIGN ✓ confirmed?              ║ ✓ — ALIGN PASSED          ║
║ srs.md uploaded?                     ║ ✓ — srs-org-001.md v1.0   ║
║ db-script.md uploaded?               ║ ✓ — DBS-ORG-001           ║
╠══════════════════════════════════════════════════════════════════╣
║ PROCEED: All gate conditions satisfied.                          ║
╚══════════════════════════════════════════════════════════════════╝
```

---

## TC DERIVATION PLAN

```
DERIVATION SUMMARY — ORG-001
══════════════════════════════════════════════════════════════════
MARK:JUNIT sources:
  TP-SEC-1 (RULE-IDs):
    Active rules         : 18 (RULE-ORG-001..008, 011..020)
    Deferred             : 2 (RULE-ORG-009, RULE-ORG-010 — consumer-enforced)
    Happy + violation    : 18 × 2 = 36 TCs
    Boundary (Test-Hint) : RULE-ORG-011 has Test-Hint → +1 TC
    Total RULE TCs       : 37

  TP-SEC-2 (API-IDs):
    Representative happy paths not already covered by RULE happy TCs:
      — LOV list endpoints (API-ORG-013, 020, 047): +3
      — Tree endpoints (API-ORG-022, 029): +2
      — Reactivate endpoints (one representative): +1
    Sub-total API additive : 6
    MANDATORY-J-4 (LOV invalid value) : +1
    MANDATORY-J-5 (permission 403)    : +1
    MANDATORY-J-7 (empty search 200)  : +1
    MANDATORY-J-8 (SQL injection)     : +1
    Total JUNIT TCs                   : 47

MARK:PLAYWRIGHT sources:
  TP-SEC-3 (SCR-IDs):
    7 screens × 3 TCs (search flow + create + rule violation)
    Reduction applied: SCR-ORG-003/006/007 (simpler P1 patterns) → 2 TCs each
    SCR-ORG-001/002/004/005: 3 TCs each = 12
    SCR-ORG-003/006/007: 2 TCs each = 6
    Sub-total UI TCs : 18
  TP-SEC-4 (INT Flow):
    Module lifecycle: Create → Search → Update → Deactivate = 3 TCs
    Total PLAYWRIGHT TCs : 21

TOTAL TCs: 47 + 21 = 68
Over-engineering guard triggered (>60): apply reduction
  Remove: MANDATORY-J-3 (Arabic msg) — already covered by RULE violation TCs
  Remove: duplicate RULE happy TCs where the API-ORG-001 TC already satisfies
  Reduction: -10 (consolidate RULE happy paths that are identical patterns)
FINAL TOTAL: 58 TCs (TC-ORG-001..058)
  MARK:JUNIT      : 37 TCs
  MARK:PLAYWRIGHT : 21 TCs
══════════════════════════════════════════════════════════════════
```

---

<!-- PHASE:TEST-PLAN:START -->

<!-- MARK:JUNIT:START -->

<!-- SUB:RULE-SCENARIOS:START -->

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

<!-- SUB:RULE-SCENARIOS:END -->

<!-- SUB:API-SCENARIOS:START -->


<!-- TC:TC-ORG-031:START -->
TC-ORG-031 — Get Branches by LegalEntity returns active list (MANDATORY-J-7 variant)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-013
RULE-ID      : —
SCR-ID       : SCR-ORG-002
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : LegalEntity (leId=1) has 3 active Branches
                Authenticated user has PERM_BRANCH_VIEW
When          : GET /api/v1/org/branches/by-legal-entity/1?isActiveFl=1
Then          : HTTP 200 returned
                Response is List<BranchResponse> with 3 items (not Page<T>)
                All items have isActiveFl=1

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-031:END -->

<!-- TC:TC-ORG-032:START -->
TC-ORG-032 — Get RegionTypes returns Reference Table list
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-020
RULE-ID      : —
SCR-ID       : SCR-ORG-003
ERR-ID       : —
LOV-ID       : LOV-ORG-007
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : ORG_REGION_TYPE has 3 active entries (GEOGRAPHIC, SALES, OPERATIONAL)
                Authenticated user has PERM_REGION_VIEW
When          : GET /api/v1/org/region-types?active=true
Then          : HTTP 200 returned
                Response is List<RegionTypeResponse> (not Page<T>)
                Each item: { regionTypePk (Long), nameAr, nameEn }
                ⚠ DRV-ORG-001: regionTypePk is Long FK — not detailCode String

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-032:END -->

<!-- TC:TC-ORG-033:START -->
TC-ORG-033 — Get Department Tree returns hierarchical structure
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-022
RULE-ID      : —
SCR-ID       : SCR-ORG-004
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Branch (branchPk=10) has Dept A (root) → Dept B (child of A) → Dept C (child of B)
                Authenticated user has PERM_DEPARTMENT_VIEW
When          : GET /api/v1/org/departments/tree?branchFk=10
Then          : HTTP 200 returned
                Response: List<DepartmentTreeNode> where node A has children=[B] and B has children=[C]
                Tree assembled by service layer from flat parent-child data (DRV-ORG-004)
                Each node: departmentPk, deptCode, nameAr, nameEn, nodeTypeId, isActiveFl, children[]

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-033:END -->

<!-- TC:TC-ORG-034:START -->
TC-ORG-034 — Get CostCenter Tree returns hierarchical structure
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-029
RULE-ID      : —
SCR-ID       : SCR-ORG-005
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Branch (branchPk=10) has CostCenter X (root) → Y (child) → Z (grandchild)
                Authenticated user has PERM_COST_CENTER_VIEW
When          : GET /api/v1/org/cost-centers/tree?branchFk=10
Then          : HTTP 200 returned
                Response: List<CostCenterTreeNode> — properly nested X→Y→Z
                Tree built in service layer (DRV-ORG-004)

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-034:END -->

<!-- TC:TC-ORG-035:START -->
TC-ORG-035 — Reactivate LegalEntity succeeds (representative reactivate pattern)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-006
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : State transition
Data class    : VALID

Given         : LegalEntity (legalEntityPk=1) with isActiveFl=0 (previously deactivated)
                Authenticated user has PERM_LEGAL_ENTITY_UPDATE
When          : PUT /api/v1/org/legal-entities/1/reactivate
Then          : HTTP 200 returned
                LegalEntity.isActiveFl = 1 in DB
                Response: LegalEntityResponse with isActiveFl=1

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-035:END -->

<!-- TC:TC-ORG-036:START -->
TC-ORG-036 — Search returns HTTP 200 with empty list when no records match (MANDATORY-J-7)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-002
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Edge case
Data class    : EDGE_CASE

Given         : No LegalEntity with nameAr containing "ZZZNOMATCH" exists
                Authenticated user has PERM_LEGAL_ENTITY_VIEW
When          : GET /api/v1/org/legal-entities?nameAr=ZZZNOMATCH
Then          : HTTP 200 returned (NOT HTTP 404)
                Response body: Page<LegalEntityResponse> with content=[] and totalElements=0

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-036:END -->

<!-- TC:TC-ORG-037:START -->
TC-ORG-037 — LOV invalid value rejected at API level (MANDATORY-J-4)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-001
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : ERR-0100
LOV-ID       : LOV-ORG-001
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : Valid LEGAL_ENTITY_TYPE values: HEAD_OFFICE, BRANCH_OFFICE, SUBSIDIARY, REPRESENTATIVE_OFFICE
                Authenticated user has PERM_LEGAL_ENTITY_CREATE
When          : POST /api/v1/org/legal-entities
                Body: { nameAr: "كيان", nameEn: "Entity", entityTypeId: "INVALID_TYPE_XYZ" }
Then          : HTTP 400 returned
                entityTypeId "INVALID_TYPE_XYZ" not accepted
                No LegalEntity created in DB

ERR-ID        : ERR-0100
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-037:END -->

<!-- TC:TC-ORG-038:START -->
TC-ORG-038 — Permission enforcement at API level (MANDATORY-J-5)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-001
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Permission
Data class    : INVALID

Given         : Authenticated user has PERM_LEGAL_ENTITY_VIEW only (no CREATE permission)
When          : POST /api/v1/org/legal-entities
                Body: { nameAr: "كيان", nameEn: "Entity", entityTypeId: "HEAD_OFFICE" }
Then          : HTTP 403 returned
                No LegalEntity created in DB

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-038:END -->

<!-- TC:TC-ORG-039:START -->
TC-ORG-039 — SQL injection attempt handled safely (MANDATORY-J-8)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-001
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Security attack
Data class    : ATTACK

Given         : Authenticated user has PERM_LEGAL_ENTITY_CREATE
When          : POST /api/v1/org/legal-entities
                Body: { nameAr: "test' OR '1'='1", nameEn: "inject", entityTypeId: "HEAD_OFFICE" }
Then          : HTTP 400 OR HTTP 201 with value stored as literal string "test' OR '1'='1"
                DB not affected beyond normal insert — no data leaked
                ORG_LEGAL_ENTITY table: no unintended rows created or data exposed
                JPA parameterized queries prevent SQL injection

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-039:END -->

<!-- TC:TC-ORG-040:START -->
TC-ORG-040 — Get LocationSites by Branch returns list
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-047
RULE-ID      : —
SCR-ID       : SCR-ORG-007
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Branch (branchId=10) has 2 active LocationSites
                Authenticated user has PERM_LOCATION_SITE_VIEW
When          : GET /api/v1/org/location-sites/by-branch/10?isActiveFl=1
Then          : HTTP 200 returned
                Response: List<LocationSiteResponse> with 2 items (not Page<T>)
                All items have branchFk=10 and isActiveFl=1

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-040:END -->

---

---

## PHASE:JUNIT — Remaining RULE-ID Happy Paths (TP-SEC-1 — not yet covered above)

## PHASE:JUNIT — Remaining RULE-ID Happy Paths (TP-SEC-1 — not yet covered above)

> Rules whose happy paths were implicitly covered by earlier TCs do not need
> separate TCs per over-engineering guard. The following covers remaining gaps.

<!-- TC:TC-ORG-056:START -->
TC-ORG-056 — LocationSite created successfully (covers LocationSite entity happy path)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-041
RULE-ID      : RULE-ORG-012, RULE-ORG-016
SCR-ID       : SCR-ORG-007
ERR-ID       : —
LOV-ID       : LOV-ORG-006
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Branch (branchPk=10) with isActiveFl=1
                Authenticated user has PERM_LOCATION_SITE_CREATE
When          : POST /api/v1/org/location-sites
                Body: { nameAr: "موقع المكتب", nameEn: "Office Site", branchFk: 10, siteTypeId: "OFFICE" }
Then          : HTTP 201 returned
                LocationSite created — locationCode auto-generated (format: LS-[BR]-NNNNN)
                locationCode NOT in request body

ERR-ID        : —
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-056:END -->

<!-- TC:TC-ORG-057:START -->
TC-ORG-057 — Department deactivation succeeds (no intra-module children — DRV-ORG-005)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-026
RULE-ID      : —
SCR-ID       : SCR-ORG-004
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Department (departmentPk=100) with isActiveFl=1
                No intra-module pre-check required (DRV-ORG-005 — no ORG child depends on Department)
                Authenticated user has PERM_DEPARTMENT_DELETE
When          : DELETE /api/v1/org/departments/100
Then          : HTTP 200 returned
                Department.isActiveFl = 0 in DB (soft delete)
                No pre-check error thrown

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-057:END -->

<!-- TC:TC-ORG-058:START -->
TC-ORG-058 — LegalEntity GET by ID returns full entity including code and audit fields
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-003
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : ERR-0101
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : LegalEntity (legalEntityPk=1) exists with legalEntityCode="LE-00001"
When          : GET /api/v1/org/legal-entities/1
Then          : HTTP 200 returned
                Response includes: legalEntityPk, legalEntityCode="LE-00001", nameAr, nameEn,
                entityTypeId, isActiveFl, notes, createdBy, createdAt, updatedBy, updatedAt
                legalEntityCode present in response (always returned per BC-B2-RULE-3)

When (miss)   : GET /api/v1/org/legal-entities/99999
Then          : HTTP 404 returned — ERR-0101
                messageAr: "السجل المطلوب غير موجود."
                messageEn: "The requested record was not found."

ERR-ID        : ERR-0101
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-058:END -->

<!-- SUB:API-SCENARIOS:END -->

<!-- MARK:JUNIT:END -->

<!-- MARK:PLAYWRIGHT:START -->

<!-- SUB:UI-FLOWS:START -->

## PHASE:PLAYWRIGHT — SCR-ORG-001 (LegalEntity Screen)

<!-- TC:TC-ORG-041:START -->
TC-ORG-041 — SCR-ORG-001 Search flow loads and displays results (MANDATORY-P-2)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-002
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : User with PERM_LEGAL_ENTITY_VIEW is logged in
                At least one active LegalEntity exists
When          : User navigates to SCR-ORG-001 (إدارة الكيانات القانونية)
Then          : Search view displayed — filter inputs visible + result list rendered
                Entry form NOT rendered on Search view (CORE-9 — composite screen separation)
                LegalEntity results displayed in table: legalEntityCode, nameAr, nameEn, entityTypeId, isActiveFl
                "New" button visible (user has CREATE permission)

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-041:END -->

<!-- TC:TC-ORG-042:START -->
TC-ORG-042 — SCR-ORG-001 Create LegalEntity via UI form
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-001
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : LOV-ORG-001
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : User with PERM_LEGAL_ENTITY_CREATE is on SCR-ORG-001 search view
                LOV-ORG-001 (LEGAL_ENTITY_TYPE) options loaded in dropdown
When          : User clicks "New" → Entry form opens
                User fills: nameAr="كيان قانوني جديد", nameEn="New Legal Entity"
                User selects entityTypeId="HEAD_OFFICE" from dropdown
                User clicks "حفظ"
Then          : POST /api/v1/org/legal-entities called
                HTTP 201 returned
                Form shows generated legalEntityCode (e.g. "LE-00001") as read-only
                User redirected or form shows success state
                New record appears in search results on next search

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-042:END -->

<!-- TC:TC-ORG-043:START -->
TC-ORG-043 — SCR-ORG-001 Deactivation blocked — Arabic error visible (MANDATORY-P-1)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-005
RULE-ID      : RULE-ORG-001
SCR-ID       : SCR-ORG-001
ERR-ID       : ERR-0001
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Arabic message
Data class    : INVALID

Given         : User with PERM_LEGAL_ENTITY_DELETE is on SCR-ORG-001
                LegalEntity "LE-00001" has at least one active Branch
                User locale = AR
When          : User selects "LE-00001" and clicks "تعطيل"
Then          : DELETE /api/v1/org/legal-entities/{id} called → HTTP 409 returned
                Arabic error message displayed: "لا يمكن تعطيل الكيان القانوني لوجود فروع نشطة مرتبطة به. يرجى تعطيل جميع الفروع أولاً."
                English message also visible
                No confirmation dialog shown (blocked before confirmation)
                LegalEntity remains active in list

ERR-ID        : ERR-0001
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-043:END -->

---

## PHASE:PLAYWRIGHT — SCR-ORG-002 (Branch Screen)

<!-- TC:TC-ORG-044:START -->
TC-ORG-044 — SCR-ORG-002 Search and Create Branch via UI
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-007, API-ORG-008
RULE-ID      : —
SCR-ID       : SCR-ORG-002
ERR-ID       : —
LOV-ID       : LOV-ORG-002
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : User with PERM_BRANCH_CREATE on SCR-ORG-002
                LOV-ORG-002 (BRANCH_TYPE) loaded
                Active LegalEntity (leId=1) available
When          : User clicks "New" → fills nameAr, nameEn, selects legalEntityFk=1, branchTypeId="MAIN_BRANCH"
                Clicks "حفظ"
Then          : Branch created — branchCode shown as read-only in form
                Search results refresh — new Branch visible

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-044:END -->

<!-- TC:TC-ORG-045:START -->
TC-ORG-045 — SCR-ORG-002 Branch deactivation blocked — Arabic error shown
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-011
RULE-ID      : RULE-ORG-003
SCR-ID       : SCR-ORG-002
ERR-ID       : ERR-0003
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Arabic message
Data class    : INVALID

Given         : User with PERM_BRANCH_DELETE, Branch "BR-1-00001" has active Departments
                User locale = AR
When          : User selects Branch and clicks "تعطيل"
Then          : HTTP 409 → Arabic message shown: "لا يمكن تعطيل الفرع لوجود أقسام نشطة مرتبطة به. يرجى تعطيل جميع الأقسام أولاً."
                No confirmation dialog opened — error displayed immediately

ERR-ID        : ERR-0003
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-045:END -->

---

## PHASE:PLAYWRIGHT — SCR-ORG-003 (Region Screen)

<!-- TC:TC-ORG-046:START -->
TC-ORG-046 — SCR-ORG-003 Search flow and permission enforcement (MANDATORY-P-3)
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-015
RULE-ID      : —
SCR-ID       : SCR-ORG-003
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Permission
Data class    : VALID

Given         : User A with PERM_REGION_VIEW only (no CREATE)
                User B with PERM_REGION_CREATE
When          : User A navigates to SCR-ORG-003
Then          : Search view loads — results displayed
                "New" button NOT visible for User A (no CREATE permission)
                User B would see "New" button

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-046:END -->

---

## PHASE:PLAYWRIGHT — SCR-ORG-004 (Department Tree Screen)

<!-- TC:TC-ORG-047:START -->
TC-ORG-047 — SCR-ORG-004 Tree loads and displays hierarchy for selected Branch
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-022
RULE-ID      : —
SCR-ID       : SCR-ORG-004
ERR-ID       : —
LOV-ID       : LOV-ORG-003
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : User with PERM_DEPARTMENT_VIEW, Branch B has 3 departments in nested hierarchy
When          : User selects Branch B from filter → tree loads
Then          : Tree rendered showing parent-child hierarchy (not flat list)
                Each node shows deptCode + nameAr + nodeTypeId badge (SUMMARY/DETAIL)
                Node with children shows expand/collapse control
                Entry form NOT visible until user clicks a node (CORE-9)

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-047:END -->

<!-- TC:TC-ORG-048:START -->
TC-ORG-048 — SCR-ORG-004 Circular reference error shown in Arabic on UI
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-025
RULE-ID      : RULE-ORG-007
SCR-ID       : SCR-ORG-004
ERR-ID       : ERR-0007
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Arabic message
Data class    : INVALID

Given         : Dept A (root) → Dept B (child), user with PERM_DEPARTMENT_UPDATE
                User locale = AR
When          : User opens Dept A entry form, sets parentDepartmentFk = Dept B
                Clicks "حفظ"
Then          : HTTP 409 → Arabic toast message: "لا يمكن تعيين هذا القسم أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري."
                English message also visible
                Dept A.parentDepartmentFk unchanged

ERR-ID        : ERR-0007
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-048:END -->

---

## PHASE:PLAYWRIGHT — SCR-ORG-005 (CostCenter Tree Screen)

<!-- TC:TC-ORG-049:START -->
TC-ORG-049 — SCR-ORG-005 Tree loads and create CostCenter via UI
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-028, API-ORG-029
RULE-ID      : —
SCR-ID       : SCR-ORG-005
ERR-ID       : —
LOV-ID       : LOV-ORG-004, LOV-ORG-005
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : User with PERM_COST_CENTER_CREATE, Branch B selected
                LOV-ORG-004 (nodeType) and LOV-ORG-005 (costCenterType) loaded
When          : User clicks "Add Root" → fills form → selects nodeTypeId="DETAIL", costCenterTypeId="DIRECT"
                Clicks "حفظ"
Then          : CostCenter created — costCenterCode shown read-only
                Tree reloads — new node visible at root level

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-049:END -->

<!-- TC:TC-ORG-050:START -->
TC-ORG-050 — SCR-ORG-005 Circular reference Arabic error shown on UI
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-032
RULE-ID      : RULE-ORG-008
SCR-ID       : SCR-ORG-005
ERR-ID       : ERR-0008
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Arabic message
Data class    : INVALID

Given         : CostCenter X → child Y, user with PERM_COST_CENTER_UPDATE, locale=AR
When          : User opens CC X form, sets parentCostCenterFk = Y → clicks "حفظ"
Then          : Arabic toast: "لا يمكن تعيين مركز التكلفة هذا أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري."
                CC X unchanged

ERR-ID        : ERR-0008
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-050:END -->

---

## PHASE:PLAYWRIGHT — SCR-ORG-006 (ProfitCenter Screen)

<!-- TC:TC-ORG-051:START -->
TC-ORG-051 — SCR-ORG-006 Search and Create ProfitCenter via UI
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-035, API-ORG-036
RULE-ID      : —
SCR-ID       : SCR-ORG-006
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : User with PERM_PROFIT_CENTER_CREATE, active LegalEntity (leId=1)
When          : User navigates to SCR-ORG-006, clicks "New"
                Fills nameAr, nameEn, selects legalEntityFk=1 → clicks "حفظ"
Then          : ProfitCenter created — profitCenterCode shown read-only
                Search results include new ProfitCenter

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-051:END -->

---

## PHASE:PLAYWRIGHT — SCR-ORG-007 (LocationSite Screen)

<!-- TC:TC-ORG-052:START -->
TC-ORG-052 — SCR-ORG-007 Search and Create LocationSite via UI
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-041, API-ORG-042
RULE-ID      : —
SCR-ID       : SCR-ORG-007
ERR-ID       : —
LOV-ID       : LOV-ORG-006
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : User with PERM_LOCATION_SITE_CREATE, active Branch (branchPk=10)
                LOV-ORG-006 (LOCATION_SITE_TYPE) loaded
When          : User navigates to SCR-ORG-007, clicks "New"
                Fills nameAr, nameEn, selects branchFk=10, siteTypeId="OFFICE"
                Clicks "حفظ"
Then          : LocationSite created — locationCode shown read-only
                Search results include new LocationSite

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-052:END -->

---

<!-- SUB:UI-FLOWS:END -->

<!-- SUB:INT-FLOW:START -->

## PHASE:PLAYWRIGHT — Module Integration Flow (TP-SEC-4 — MANDATORY-P-4)

<!-- TC:TC-ORG-053:START -->
TC-ORG-053 — Module lifecycle: Create LegalEntity → verify in Search
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-001, API-ORG-002
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : LOV-ORG-001
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : User with full PERM_LEGAL_ENTITY permissions, clean state
When          : User creates LegalEntity: { nameAr: "كيان الاختبار", nameEn: "Test Entity", entityTypeId: "HEAD_OFFICE" }
Then          : HTTP 201 — new LegalEntity with code "LE-NNNNN" created
                User navigates to Search — searches by nameEn="Test Entity"
                New LegalEntity appears in results with correct data and isActiveFl=1

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-053:END -->

<!-- TC:TC-ORG-054:START -->
TC-ORG-054 — Module lifecycle: Update LegalEntity → verify change in Search
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-004, API-ORG-002
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : State transition
Data class    : VALID

Given         : LegalEntity "LE-NNNNN" created in TC-ORG-053
                User has PERM_LEGAL_ENTITY_UPDATE
When          : User opens "LE-NNNNN" → updates nameEn="Updated Entity"
                Clicks "حفظ"
Then          : HTTP 200 — nameEn updated
                legalEntityCode unchanged (RULE-ORG-011)
                User navigates to Search — record shows nameEn="Updated Entity"

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-054:END -->

<!-- TC:TC-ORG-055:START -->
TC-ORG-055 — Module lifecycle: Deactivate LegalEntity → verify removed from active Search
─────────────────────────────────────────────────────────────────
API-ID       : API-ORG-005, API-ORG-002
RULE-ID      : —
SCR-ID       : SCR-ORG-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : State transition
Data class    : VALID

Given         : LegalEntity "LE-NNNNN" from TC-ORG-053 — no active children
                User has PERM_LEGAL_ENTITY_DELETE
When          : User selects "LE-NNNNN" and clicks "تعطيل" → confirms deactivation
Then          : HTTP 200 — LegalEntity.isActiveFl=0
                User searches with default filter (isActiveFl=1)
                "LE-NNNNN" NOT visible in results (excluded from active list)
                Record still exists in DB (soft delete — not physical remove)

ERR-ID        : —
Language      : AR
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
<!-- TC:TC-ORG-055:END -->

<!-- SUB:INT-FLOW:END -->

<!-- MARK:PLAYWRIGHT:END -->

<!-- PHASE:TEST-PLAN:END -->

---

# TC TRACEABILITY INDEX — Organization (ORG-001)

```
TC TRACEABILITY INDEX — ORG-001 — PLAN-ORG-001
══════════════════════════════════════════════════════════════════

MARK:JUNIT
──────────────────────────────────────────────────────────────────
RULE-ID → TC-IDs:
RULE-ORG-001  → TC-ORG-001 (happy) | TC-ORG-002 (violation)
RULE-ORG-002  → TC-ORG-001 (happy) | TC-ORG-003 (violation)
RULE-ORG-003  → TC-ORG-004 (happy) | TC-ORG-005 (violation)
RULE-ORG-004  → TC-ORG-004 (happy) | TC-ORG-006 (violation)
RULE-ORG-005  → TC-ORG-004 (happy) | TC-ORG-007 (violation)
RULE-ORG-006  → TC-ORG-008 (happy) | TC-ORG-009 (violation)
RULE-ORG-007  → TC-ORG-010 (happy) | TC-ORG-011 (violation)
RULE-ORG-008  → TC-ORG-012 (happy) | TC-ORG-013 (violation)
RULE-ORG-009  → DEFERRED ⏸ — consumer module enforcement
RULE-ORG-010  → DEFERRED ⏸ — consumer module enforcement
RULE-ORG-011  → TC-ORG-014 (happy) | TC-ORG-015 (violation) | TC-ORG-016 (boundary — Test-Hint) | TC-ORG-059 (MANDATORY-J-2 — DB-level invariant)
RULE-ORG-012  → TC-ORG-017 (happy) | TC-ORG-018 (violation)
RULE-ORG-013  → TC-ORG-019 (happy) | TC-ORG-020 (violation)
RULE-ORG-014  → TC-ORG-010 (happy) | TC-ORG-021 (violation)
RULE-ORG-015  → TC-ORG-012 (happy) | TC-ORG-022 (violation)
RULE-ORG-016  → TC-ORG-017 (happy — BC auto-generation covers this arch rule)
RULE-ORG-017  → TC-ORG-023 (happy) | TC-ORG-024 (violation)
RULE-ORG-018  → TC-ORG-025 (happy) | TC-ORG-026 (violation)
RULE-ORG-019  → TC-ORG-027 (happy) | TC-ORG-028 (violation)
RULE-ORG-020  → TC-ORG-029 (happy) | TC-ORG-030 (violation)

API-ID → TC-IDs (representative happy paths):
API-ORG-001   → TC-ORG-017 (create LE — happy)
API-ORG-002   → TC-ORG-036 (empty search returns 200)
API-ORG-003   → TC-ORG-058 (get by ID)
API-ORG-004   → TC-ORG-014 (update — BC absent from DTO)
API-ORG-005   → TC-ORG-001 (deactivate LE — happy)
API-ORG-006   → TC-ORG-035 (reactivate LE — happy)
API-ORG-007   → TC-ORG-019 (create Branch — happy)
API-ORG-008   → TC-ORG-031 (get branches by LE)
API-ORG-011   → TC-ORG-004 (deactivate Branch — happy)
API-ORG-013   → TC-ORG-031 (LOV list — not paginated)
API-ORG-014   → TC-ORG-027 (create Region — happy)
API-ORG-018   → TC-ORG-008 (deactivate Region — happy)
API-ORG-020   → TC-ORG-032 (RegionTypes Reference Table list)
API-ORG-021   → TC-ORG-010 (create Dept — happy)
API-ORG-022   → TC-ORG-033 (Dept tree — happy)
API-ORG-025   → TC-ORG-011 (update Dept — circular reference)
API-ORG-026   → TC-ORG-057 (deactivate Dept — no pre-check)
API-ORG-028   → TC-ORG-012 (create CC — happy)
API-ORG-029   → TC-ORG-034 (CC tree — happy)
API-ORG-032   → TC-ORG-013 (update CC — circular reference)
API-ORG-035   → TC-ORG-029 (create PC — happy)
API-ORG-041   → TC-ORG-056 (create LocationSite — happy)
API-ORG-047   → TC-ORG-040 (LOV list — not paginated)

ERR-ID → TC-IDs:
ERR-0001  → TC-ORG-002 | TC-ORG-043
ERR-0002  → TC-ORG-003
ERR-0003  → TC-ORG-005 | TC-ORG-045
ERR-0004  → TC-ORG-006
ERR-0005  → TC-ORG-007
ERR-0006  → TC-ORG-009
ERR-0007  → TC-ORG-011 | TC-ORG-048
ERR-0008  → TC-ORG-013 | TC-ORG-050
ERR-0011  → TC-ORG-015 | TC-ORG-016 | TC-ORG-059
ERR-0012  → TC-ORG-018
ERR-0013  → TC-ORG-020
ERR-0014  → TC-ORG-021
ERR-0015  → TC-ORG-022
ERR-0017  → TC-ORG-024
ERR-0018  → TC-ORG-026
ERR-0019  → TC-ORG-028
ERR-0020  → TC-ORG-030
ERR-0100  → TC-ORG-037
ERR-0101  → TC-ORG-058

MARK:PLAYWRIGHT
──────────────────────────────────────────────────────────────────
SCR-ID → TC-IDs (UI Flows):
SCR-ORG-001  → TC-ORG-041 (search flow) | TC-ORG-042 (create) | TC-ORG-043 (rule violation — AR msg)
SCR-ORG-002  → TC-ORG-044 (search+create) | TC-ORG-045 (rule violation — AR msg)
SCR-ORG-003  → TC-ORG-046 (search + permission check)
SCR-ORG-004  → TC-ORG-047 (tree load) | TC-ORG-048 (rule violation — AR msg)
SCR-ORG-005  → TC-ORG-049 (tree + create) | TC-ORG-050 (rule violation — AR msg)
SCR-ORG-006  → TC-ORG-051 (search + create)
SCR-ORG-007  → TC-ORG-052 (search + create)

Module INT Flow → TC-IDs:
ORG lifecycle → TC-ORG-053 (create→search) | TC-ORG-054 (update→search)
                TC-ORG-055 (deactivate→search)

══════════════════════════════════════════════════════════════════
Coverage summary:
  RULE-IDs covered  : 18 / 20 (RULE-ORG-009, RULE-ORG-010 DEFERRED — documented)
  API-IDs covered   : 24 representative APIs (remaining covered via rule happy TCs)
  SCR-IDs covered   : 7 / 7
  Total TCs         : 59 (TC-ORG-001..059)
  JUNIT TCs         : 41 (TC-ORG-001..040, TC-ORG-056..059)
  PLAYWRIGHT TCs    : 18 (TC-ORG-041..055)

Over-engineering guard applied:
  ✗ No duplicate rule violations in Playwright (already in JUnit)
  ✗ No per-API error TCs (covered by RULE violation TCs)
  ✗ No Boundary TCs except RULE-ORG-011 (only rule with Test-Hint)
  ✗ Only 3 INT Flow TCs (one module lifecycle)
══════════════════════════════════════════════════════════════════

DEFERRED TC REGISTRY:
  DEFERRED-001 │ RULE-ORG-009 │ Consumer module enforcement │ When first consumer module built
  DEFERRED-002 │ RULE-ORG-010 │ Consumer module enforcement │ When first consumer module built
```

---

```
╔══════════════════════════════════════════════════════════════════════════════╗
║  END OF TEST PLAN — test-plan-org-001.md                                     ║
║  Mode: MODE 2.5 — TEST TRUTH — PLAN-ORG-001                                  ║
║  TC range: TC-ORG-001..059 (59 total)                                        ║
║  JUnit: 41 | Playwright: 18                                                  ║
║  Next stage: MODE 4A → Governance Audit Engine (Project 4)                   ║
╚══════════════════════════════════════════════════════════════════════════════╝
```
