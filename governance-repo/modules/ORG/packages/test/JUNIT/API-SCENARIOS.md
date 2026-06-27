<!-- Source: MARK:JUNIT / SUB:API-SCENARIOS -->



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

