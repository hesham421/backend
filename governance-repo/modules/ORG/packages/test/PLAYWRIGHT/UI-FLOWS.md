<!-- Source: MARK:PLAYWRIGHT / SUB:UI-FLOWS -->


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

