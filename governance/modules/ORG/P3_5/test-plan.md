<!-- PHASE:HEADER:START -->
# TEST PLAN — Organization & Cost Centers — PLAN-ID: PLAN-ORG-001 — MODE 2.5

```
Module         : Organization (ORG-001)
Source Plan    : execution-plan-org-001.md — ALIGN GATE PASSED ✓
Governed by    : Execution Plan Governance Engine (Project 3) v2 — Section 16
Derivation     : TP-SEC-1 (per RULE-ID) + TP-SEC-2 (per API-ID) + TP-SEC-3 (per SCR-ID) + TP-SEC-4 (Module INT)
Total TCs      : 61 (over-engineering guard applied — raw derivation exceeded 100+, reduced per guard rules; J-6/P-4 restored per 4A-005-003 finding)
JUNIT TCs      : 52 · PLAYWRIGHT TCs : 9
Generated      : 2026-06-30
```

DRV-ORG-017: 4A-005-003 remediation — MANDATORY-J-6 (Concurrent update conflict, TC-ORG-060) and MANDATORY-P-4 (Module Integration Flow UI, TC-ORG-061) were omitted from the initial Over-Engineering Guard reduction without a named TC-ID or DRV entry. Both are now restored as explicit, individually traceable TCs. The Over-Engineering Guard reduction rationale for all OTHER reductions (per-API happy-path consolidation, UI-FLOWS scope) remains documented in DRV-ORG-016 below — only J-6/P-4 were incorrectly dropped; this DRV-ID corrects that specific gap.

DRV-ORG-016: Over-Engineering Guard applied — raw per-API happy-path derivation (44 TCs) reduced to 3 representative non-duplicate-coverage TCs (Search, GetById, Tree), since Create/Update happy paths are already exercised by RULE-ID happy-path TCs (RULE-ORG-011..016 apply globally across all 7 entities). Per-screen UI-FLOWS reduced from 21 (3×7) to 2, focused on SCR-ORG-001 (primary/representative screen) plus the rule-violation flow folded into a single combined TC. MANDATORY-J reduced from 8 candidate scenarios to 7 (J-8 SQLi retained, J-2 empty-search retained, J-7 retained — duplicate idempotency scenario merged into INT-FLOW). Logged per Section 16.5 Over-Engineering Guard.
<!-- PHASE:HEADER:END -->

---

<!-- PHASE:JUNIT:START -->
<!-- MARK:JUNIT:START -->
# MARK:JUNIT

  <!-- SUB:RULE-SCENARIOS:START -->
  ## SUB:RULE-SCENARIOS — RULE-ID derived TCs (41)

  <!-- TC:TC-ORG-001:START -->
  ### TC-ORG-001 — RULE-ORG-001 happy (Data-Class: VALID)
  Given: LegalEntity LE-00001 active, zero active Branches reference it
  When: PUT /api/v1/org/legal-entities/{id}/deactivate
  Then: HTTP 200, isActiveFl=0
  <!-- TC:TC-ORG-001:END -->

  <!-- TC:TC-ORG-002:START -->
  ### TC-ORG-002 — RULE-ORG-001 violation (Data-Class: INVALID — ERR-ORG-0005)
  Given: LegalEntity LE-00001 active, ≥1 active Branch references it
  When: PUT /api/v1/org/legal-entities/{id}/deactivate
  Then: HTTP 409, ERR-ORG-0005, Arabic+English message returned
  <!-- TC:TC-ORG-002:END -->

  <!-- TC:TC-ORG-003:START -->
  ### TC-ORG-003 — RULE-ORG-002 happy (Data-Class: VALID)
  Given: LegalEntity active, zero active ProfitCenters
  When: deactivate
  Then: HTTP 200
  <!-- TC:TC-ORG-003:END -->

  <!-- TC:TC-ORG-004:START -->
  ### TC-ORG-004 — RULE-ORG-002 violation (Data-Class: INVALID — ERR-ORG-0006)
  Given: LegalEntity active, ≥1 active ProfitCenter references it
  When: deactivate
  Then: HTTP 409, ERR-ORG-0006
  <!-- TC:TC-ORG-004:END -->

  <!-- TC:TC-ORG-005:START -->
  ### TC-ORG-005 — RULE-ORG-003 happy (Data-Class: VALID)
  Given: Branch active, zero active Departments
  When: PUT .../branches/{id}/deactivate
  Then: HTTP 200
  <!-- TC:TC-ORG-005:END -->

  <!-- TC:TC-ORG-006:START -->
  ### TC-ORG-006 — RULE-ORG-003 violation (Data-Class: INVALID — ERR-ORG-0007)
  Given: Branch active, ≥1 active Department references it
  When: deactivate Branch
  Then: HTTP 409, ERR-ORG-0007
  <!-- TC:TC-ORG-006:END -->

  <!-- TC:TC-ORG-007:START -->
  ### TC-ORG-007 — RULE-ORG-004 happy (Data-Class: VALID)
  Given: Branch active, zero active CostCenters
  When: deactivate Branch
  Then: HTTP 200
  <!-- TC:TC-ORG-007:END -->

  <!-- TC:TC-ORG-008:START -->
  ### TC-ORG-008 — RULE-ORG-004 violation (Data-Class: INVALID — ERR-ORG-0008)
  Given: Branch active, ≥1 active CostCenter references it
  When: deactivate Branch
  Then: HTTP 409, ERR-ORG-0008
  <!-- TC:TC-ORG-008:END -->

  <!-- TC:TC-ORG-009:START -->
  ### TC-ORG-009 — RULE-ORG-005 happy (Data-Class: VALID)
  Given: Branch active, zero active LocationSites
  When: deactivate Branch
  Then: HTTP 200
  <!-- TC:TC-ORG-009:END -->

  <!-- TC:TC-ORG-010:START -->
  ### TC-ORG-010 — RULE-ORG-005 violation (Data-Class: INVALID — ERR-ORG-0009)
  Given: Branch active, ≥1 active LocationSite references it
  When: deactivate Branch
  Then: HTTP 409, ERR-ORG-0009
  <!-- TC:TC-ORG-010:END -->

  <!-- TC:TC-ORG-011:START -->
  ### TC-ORG-011 — RULE-ORG-006 happy (Data-Class: VALID)
  Given: Region active, zero active Branches reference it
  When: PUT .../regions/{id}/deactivate
  Then: HTTP 200
  <!-- TC:TC-ORG-011:END -->

  <!-- TC:TC-ORG-012:START -->
  ### TC-ORG-012 — RULE-ORG-006 violation (Data-Class: INVALID — ERR-ORG-0010)
  Given: Region active, ≥1 active Branch references it
  When: deactivate Region
  Then: HTTP 409, ERR-ORG-0010
  <!-- TC:TC-ORG-012:END -->

  <!-- TC:TC-ORG-013:START -->
  ### TC-ORG-013 — RULE-ORG-006 boundary (Data-Class: BOUNDARY — Test-Hint in SRS A4)
  Given: Region has Branches but ALL are inactive (is_active_fl=0)
  When: deactivate Region
  Then: HTTP 200 — inactive-only relations do NOT block deactivation
  <!-- TC:TC-ORG-013:END -->

  <!-- TC:TC-ORG-014:START -->
  ### TC-ORG-014 — RULE-ORG-007 happy (Data-Class: VALID)
  Given: Department creation/update with parentDepartmentFk not creating a cycle
  When: POST/PUT department with valid parent
  Then: HTTP 200/201, tree integrity preserved
  <!-- TC:TC-ORG-014:END -->

  <!-- TC:TC-ORG-015:START -->
  ### TC-ORG-015 — RULE-ORG-007 violation (Data-Class: INVALID — ERR-ORG-0011)
  Given: Department A is ancestor of Department B
  When: attempt to set A.parentDepartmentFk = B (creates cycle)
  Then: HTTP 400, ERR-ORG-0011
  <!-- TC:TC-ORG-015:END -->

  <!-- TC:TC-ORG-016:START -->
  ### TC-ORG-016 — RULE-ORG-008 happy (Data-Class: VALID)
  Given: CostCenter parent assignment without cycle
  When: POST/PUT cost center
  Then: HTTP 200/201
  <!-- TC:TC-ORG-016:END -->

  <!-- TC:TC-ORG-017:START -->
  ### TC-ORG-017 — RULE-ORG-008 violation (Data-Class: INVALID — ERR-ORG-0012)
  Given: CostCenter A is ancestor of B
  When: set A.parentCostCenterFk = B
  Then: HTTP 400, ERR-ORG-0012
  <!-- TC:TC-ORG-017:END -->

  <!-- TC:TC-ORG-018:START -->
  ### TC-ORG-018 — RULE-ORG-009 happy (Data-Class: VALID — documentation scope)
  Given: Department nodeType=DETAIL assigned to a transactional record in a consuming module (simulated)
  When: assignment attempted
  Then: accepted — DETAIL departments permitted (enforcement lives in consuming module; this TC documents the contract)
  <!-- TC:TC-ORG-018:END -->

  <!-- TC:TC-ORG-019:START -->
  ### TC-ORG-019 — RULE-ORG-009 violation (Data-Class: INVALID — ERR-ORG-0018, contract documentation)
  Given: Department nodeType=SUMMARY
  When: assignment to transactional record attempted (consuming-module contract)
  Then: rejection contract documented — ERR-ORG-0018; actual enforcement out of ORG-001 scope
  <!-- TC:TC-ORG-019:END -->

  <!-- TC:TC-ORG-020:START -->
  ### TC-ORG-020 — RULE-ORG-010 happy (Data-Class: VALID — documentation scope)
  Given: CostCenter nodeType=DETAIL
  When: assignment to transactional record (consuming-module contract)
  Then: accepted
  <!-- TC:TC-ORG-020:END -->

  <!-- TC:TC-ORG-021:START -->
  ### TC-ORG-021 — RULE-ORG-010 violation (Data-Class: INVALID — ERR-ORG-0018)
  Given: CostCenter nodeType=SUMMARY
  When: assignment to transactional record (consuming-module contract)
  Then: rejection contract documented — ERR-ORG-0018
  <!-- TC:TC-ORG-021:END -->

  <!-- TC:TC-ORG-022:START -->
  ### TC-ORG-022 — RULE-ORG-011 happy (Data-Class: VALID)
  Given: existing LegalEntity record
  When: PUT update with nameAr/nameEn changed only, no code field in payload
  Then: HTTP 200, code unchanged
  <!-- TC:TC-ORG-022:END -->

  <!-- TC:TC-ORG-023:START -->
  ### TC-ORG-023 — RULE-ORG-011 violation (Data-Class: INVALID — ERR-ORG-0013)
  Given: existing record
  When: PUT update payload includes legalEntityCode field with a different value
  Then: HTTP 400, ERR-ORG-0013
  <!-- TC:TC-ORG-023:END -->

  <!-- TC:TC-ORG-024:START -->
  ### TC-ORG-024 — RULE-ORG-012 happy (Data-Class: VALID)
  Given: NumberingEngine available
  When: POST create LegalEntity
  Then: HTTP 201, unique code LE-NNNNN generated
  <!-- TC:TC-ORG-024:END -->

  <!-- TC:TC-ORG-025:START -->
  ### TC-ORG-025 — RULE-ORG-012 violation (Data-Class: INVALID — ERR-ORG-0002)
  Given: simulated NumberingEngine sequence conflict
  When: POST create
  Then: HTTP 409, ERR-ORG-0002
  <!-- TC:TC-ORG-025:END -->

  <!-- TC:TC-ORG-026:START -->
  ### TC-ORG-026 — RULE-ORG-013 happy (Data-Class: VALID)
  Given: POST create any of the 7 Business-Code entities
  When: record saved
  Then: code generated exclusively via NumberingEngine call — no module-local numbering logic invoked
  <!-- TC:TC-ORG-026:END -->

  <!-- TC:TC-ORG-027:START -->
  ### TC-ORG-027 — RULE-ORG-013 violation (Data-Class: INVALID — ERR-ORG-0002)
  Given: NumberingEngine unreachable (simulated outage)
  When: POST create
  Then: HTTP 5xx/409 surfaced as ERR-ORG-0002 class, no fallback local numbering occurs
  <!-- TC:TC-ORG-027:END -->

  <!-- TC:TC-ORG-028:START -->
  ### TC-ORG-028 — RULE-ORG-014 happy (Data-Class: VALID)
  Given: Update DTO schema excludes Business Code field entirely
  When: PUT update without code field
  Then: HTTP 200
  <!-- TC:TC-ORG-028:END -->

  <!-- TC:TC-ORG-029:START -->
  ### TC-ORG-029 — RULE-ORG-014 violation (Data-Class: INVALID — ERR-ORG-0003)
  Given: PUT update payload includes a code field
  When: request submitted
  Then: HTTP 400, ERR-ORG-0003
  <!-- TC:TC-ORG-029:END -->

  <!-- TC:TC-ORG-030:START -->
  ### TC-ORG-030 — RULE-ORG-015 happy (Data-Class: VALID)
  Given: nameAr/nameEn unique within parent scope
  When: POST create
  Then: HTTP 201
  <!-- TC:TC-ORG-030:END -->

  <!-- TC:TC-ORG-031:START -->
  ### TC-ORG-031 — RULE-ORG-015 violation (Data-Class: INVALID — ERR-ORG-0001)
  Given: an active record with same nameAr already exists in same parent scope
  When: POST create with duplicate nameAr
  Then: HTTP 400, ERR-ORG-0001
  <!-- TC:TC-ORG-031:END -->

  <!-- TC:TC-ORG-032:START -->
  ### TC-ORG-032 — RULE-ORG-016 happy (Data-Class: VALID)
  Given: Create/Update payload contains no audit fields
  When: request submitted
  Then: HTTP 200/201, audit fields populated by AuditEntityListener only
  <!-- TC:TC-ORG-032:END -->

  <!-- TC:TC-ORG-033:START -->
  ### TC-ORG-033 — RULE-ORG-016 violation (Data-Class: INVALID — ERR-ORG-0003)
  Given: payload includes createdBy/createdAt/updatedBy/updatedAt
  When: POST or PUT submitted
  Then: HTTP 400, ERR-ORG-0003
  <!-- TC:TC-ORG-033:END -->

  <!-- TC:TC-ORG-034:START -->
  ### TC-ORG-034 — RULE-ORG-017 happy (Data-Class: VALID)
  Given: Region with no consumer SOFT-READ references registered
  When: deactivate Region
  Then: HTTP 200, no warning surfaced
  <!-- TC:TC-ORG-034:END -->

  <!-- TC:TC-ORG-035:START -->
  ### TC-ORG-035 — RULE-ORG-017 violation (Data-Class: INVALID — ERR-ORG-0014, non-blocking warning)
  Given: Region referenced via SOFT-READ by another module (simulated)
  When: deactivate Region
  Then: HTTP 200 + ERR-ORG-0014 warning payload (non-blocking — see OQ-001)
  <!-- TC:TC-ORG-035:END -->

  <!-- TC:TC-ORG-036:START -->
  ### TC-ORG-036 — RULE-ORG-018 happy (Data-Class: VALID)
  Given: target LegalEntity is active
  When: POST create Branch under it
  Then: HTTP 201
  <!-- TC:TC-ORG-036:END -->

  <!-- TC:TC-ORG-037:START -->
  ### TC-ORG-037 — RULE-ORG-018 violation (Data-Class: INVALID — ERR-ORG-0015)
  Given: target LegalEntity is inactive
  When: POST create Branch under it
  Then: HTTP 400, ERR-ORG-0015
  <!-- TC:TC-ORG-037:END -->

  <!-- TC:TC-ORG-038:START -->
  ### TC-ORG-038 — RULE-ORG-019 happy (Data-Class: VALID)
  Given: target Branch is active
  When: POST create Department/CostCenter/LocationSite under it
  Then: HTTP 201
  <!-- TC:TC-ORG-038:END -->

  <!-- TC:TC-ORG-039:START -->
  ### TC-ORG-039 — RULE-ORG-019 violation (Data-Class: INVALID — ERR-ORG-0016)
  Given: target Branch is inactive
  When: POST create Department/CostCenter/LocationSite under it
  Then: HTTP 400, ERR-ORG-0016
  <!-- TC:TC-ORG-039:END -->

  <!-- TC:TC-ORG-040:START -->
  ### TC-ORG-040 — RULE-ORG-020 happy (Data-Class: VALID)
  Given: Department/CostCenter update payload omits nodeTypeId
  When: PUT update
  Then: HTTP 200, nodeTypeId unchanged
  <!-- TC:TC-ORG-040:END -->

  <!-- TC:TC-ORG-041:START -->
  ### TC-ORG-041 — RULE-ORG-020 violation (Data-Class: INVALID — ERR-ORG-0017)
  Given: existing Department/CostCenter record
  When: PUT update payload includes a different nodeTypeId
  Then: HTTP 400, ERR-ORG-0017
  <!-- TC:TC-ORG-041:END -->
  <!-- SUB:RULE-SCENARIOS:END -->

  <!-- SUB:API-SCENARIOS:START -->
  ## SUB:API-SCENARIOS — API-ID derived TCs (3 — non-duplicate coverage only)

  <!-- TC:TC-ORG-042:START -->
  ### TC-ORG-042 — API-ORG-002 happy (Data-Class: VALID — Search)
  Given: 5 LegalEntity records exist, 2 matching filter nameAr
  When: GET /api/v1/org/legal-entities?nameAr=...&page=0&size=10
  Then: HTTP 200, page content contains the 2 matching records, total=2
  <!-- TC:TC-ORG-042:END -->

  <!-- TC:TC-ORG-043:START -->
  ### TC-ORG-043 — API-ORG-012 happy (Data-Class: VALID — GetById)
  Given: Branch with known id exists
  When: GET /api/v1/org/branches/{id}
  Then: HTTP 200, full Branch payload returned including resolved legalEntityFk
  <!-- TC:TC-ORG-043:END -->

  <!-- TC:TC-ORG-044:START -->
  ### TC-ORG-044 — API-ORG-020 happy (Data-Class: VALID — Department Tree)
  Given: Branch has a 3-level Department hierarchy
  When: GET /api/v1/org/departments/tree?branchFk={id}
  Then: HTTP 200, nested tree structure with correct parent-child ordering and 3 depth levels
  <!-- TC:TC-ORG-044:END -->
  <!-- SUB:API-SCENARIOS:END -->

  <!-- SUB:MANDATORY-J:START -->
  ## MANDATORY-J Scenarios (7)

  <!-- TC:TC-ORG-045:START -->
  ### TC-ORG-045 — MANDATORY-J-1 Permission denied
  Given: user without PERM_ORG_LEGAL_ENTITY_CREATE
  When: POST /api/v1/org/legal-entities
  Then: HTTP 403
  <!-- TC:TC-ORG-045:END -->

  <!-- TC:TC-ORG-046:START -->
  ### TC-ORG-046 — MANDATORY-J-2 Empty search returns 200
  Given: search filters match zero records, across all 7 search endpoints
  When: GET .../{entity}?filters
  Then: HTTP 200 with empty content array — NOT HTTP 404 (applies to API-ORG-002,008,014,021,028,034,040)
  <!-- TC:TC-ORG-046:END -->

  <!-- TC:TC-ORG-047:START -->
  ### TC-ORG-047 — MANDATORY-J-3 GetById not found
  Given: id does not exist
  When: GET /api/v1/org/{entity}/{id}
  Then: HTTP 404, ERR-ORG-0004 (via LocalizedException, not NotFoundException)
  <!-- TC:TC-ORG-047:END -->

  <!-- TC:TC-ORG-048:START -->
  ### TC-ORG-048 — MANDATORY-J-4 Required field missing
  Given: POST payload missing required nameAr
  When: request submitted
  Then: HTTP 400, field-level validation error, bilingual message
  <!-- TC:TC-ORG-048:END -->

  <!-- TC:TC-ORG-049:START -->
  ### TC-ORG-049 — MANDATORY-J-5 Invalid LOV code rejected
  Given: entityTypeId = "INVALID_CODE" not in LOV-ORG-001
  When: POST create LegalEntity
  Then: HTTP 400, invalid LOV value rejected
  <!-- TC:TC-ORG-049:END -->

  <!-- TC:TC-ORG-050:START -->
  ### TC-ORG-050 — MANDATORY-J-7 Idempotent deactivate
  Given: record already inactive
  When: PUT .../deactivate called again
  Then: HTTP 200, no error, state remains inactive (idempotent)
  <!-- TC:TC-ORG-050:END -->

  <!-- TC:TC-ORG-051:START -->
  ### TC-ORG-051 — MANDATORY-J-8 SQL injection resistance
  Given: POST endpoint accepting string input
  When: nameAr = "test' OR '1'='1"
  Then: HTTP 400 OR value stored as literal string — DB not affected, no data leaked (Data class: ATTACK)
  <!-- TC:TC-ORG-051:END -->

  <!-- TC:TC-ORG-060:START -->
  ### TC-ORG-060 — MANDATORY-J-6 Concurrent update conflict
  Given: two clients fetch the same record (e.g. CostCenter) simultaneously
  When: both submit PUT update concurrently with different field values
  Then: both writes succeed sequentially (no optimistic-locking/version column exists per DRV-ORG-004) — last write wins; HTTP 200 returned to both callers; final DB state reflects the later-committed transaction
  <!-- TC:TC-ORG-060:END -->
  <!-- SUB:MANDATORY-J:END -->
  <!-- MARK:JUNIT:END -->
<!-- PHASE:JUNIT:END -->

---

<!-- PHASE:PLAYWRIGHT:START -->
<!-- MARK:PLAYWRIGHT:START -->
# MARK:PLAYWRIGHT

  <!-- SUB:UI-FLOWS:START -->
  ## SUB:UI-FLOWS (2 — representative screen: SCR-ORG-001)

  <!-- TC:TC-ORG-052:START -->
  ### TC-ORG-052 — SCR-ORG-001 happy UI flow (search + view results)
  Given: user with PERM_ORG_LEGAL_ENTITY_VIEW on SCR-ORG-001
  When: user opens screen, enters search filter, clicks Search
  Then: result list renders matching LegalEntity rows; Entry form NOT shown on Search view (MANDATORY-P-2)
  <!-- TC:TC-ORG-052:END -->

  <!-- TC:TC-ORG-053:START -->
  ### TC-ORG-053 — SCR-ORG-001 create via UI + rule violation on screen
  Given: user with PERM_ORG_LEGAL_ENTITY_CREATE navigates to Entry view
  When: user submits form with duplicate nameAr (RULE-ORG-015)
  Then: Arabic error message displayed inline on field, English message also visible (MANDATORY-P-1); on valid resubmit, record created and visible in Search
  <!-- TC:TC-ORG-053:END -->
  <!-- SUB:UI-FLOWS:END -->

  <!-- SUB:INT-FLOW:START -->
  ## SUB:INT-FLOW — Module lifecycle (3 — max per governance)

  <!-- TC:TC-ORG-054:START -->
  ### TC-ORG-054 — Create → Search (verify appears)
  Given: clean module state
  When: user creates a LegalEntity then searches for it
  Then: newly created record appears in active search results
  <!-- TC:TC-ORG-054:END -->

  <!-- TC:TC-ORG-055:START -->
  ### TC-ORG-055 — Update → Search (verify updated)
  Given: existing LegalEntity
  When: user updates nameEn then searches
  Then: search results reflect updated nameEn
  <!-- TC:TC-ORG-055:END -->

  <!-- TC:TC-ORG-056:START -->
  ### TC-ORG-056 — Deactivate → Search (verify removed from active list)
  Given: existing LegalEntity with no blocking children
  When: user deactivates it then searches with isActiveFl=true filter
  Then: deactivated record no longer appears in active search results
  <!-- TC:TC-ORG-056:END -->
  <!-- SUB:INT-FLOW:END -->

  <!-- SUB:MANDATORY-P:START -->
  ## MANDATORY-P Scenarios (3)

  <!-- TC:TC-ORG-057:START -->
  ### TC-ORG-057 — MANDATORY-P-2 Composite Screen UX separation (CORE-9)
  Given: SCR-ORG-002 (Branches) opened
  Then: Search view shows filter inputs + result list only; Entry form not rendered on Search view; Entry accessible via navigation action only
  <!-- TC:TC-ORG-057:END -->

  <!-- TC:TC-ORG-058:START -->
  ### TC-ORG-058 — MANDATORY-P-3 Permission enforcement (UI level)
  Given: user without PERM_ORG_BRANCH_CREATE
  When: navigates to SCR-ORG-002
  Then: Add/New button not visible on screen
  <!-- TC:TC-ORG-058:END -->

  <!-- TC:TC-ORG-059:START -->
  ### TC-ORG-059 — MANDATORY-P-1 Arabic error visible across screens (cross-screen spot-check)
  Given: user locale = AR, form open on SCR-ORG-004 (Departments)
  When: user submits form triggering RULE-ORG-007 (circular reference)
  Then: Arabic error message displayed inline on field, English message also visible
  <!-- TC:TC-ORG-059:END -->

  <!-- TC:TC-ORG-061:START -->
  ### TC-ORG-061 — MANDATORY-P-4 Module Integration Flow (UI)
  Given: clean module state, user with full ORG permissions
  When: user performs via UI: Create LegalEntity → Search (verify appears) → Update → Deactivate
  Then: each step reflects correct state on screen; deactivated record no longer appears in active search results (one scenario for the module — not per screen)
  <!-- TC:TC-ORG-061:END -->
  <!-- SUB:MANDATORY-P:END -->
  <!-- MARK:PLAYWRIGHT:END -->
<!-- PHASE:PLAYWRIGHT:END -->

---

<!-- PHASE:TRACEABILITY:START -->
# TC TRACEABILITY INDEX — Organization (ORG-001)
══════════════════════════════════════════════════════════════════

## MARK:JUNIT
──────────────────────────────────────────────────────────────────
RULE-ID → TC-IDs:
RULE-ORG-001 → TC-ORG-001 (happy) | TC-ORG-002 (violation)
RULE-ORG-002 → TC-ORG-003 (happy) | TC-ORG-004 (violation)
RULE-ORG-003 → TC-ORG-005 (happy) | TC-ORG-006 (violation)
RULE-ORG-004 → TC-ORG-007 (happy) | TC-ORG-008 (violation)
RULE-ORG-005 → TC-ORG-009 (happy) | TC-ORG-010 (violation)
RULE-ORG-006 → TC-ORG-011 (happy) | TC-ORG-012 (violation) | TC-ORG-013 (boundary — Test-Hint)
RULE-ORG-007 → TC-ORG-014 (happy) | TC-ORG-015 (violation)
RULE-ORG-008 → TC-ORG-016 (happy) | TC-ORG-017 (violation)
RULE-ORG-009 → TC-ORG-018 (happy) | TC-ORG-019 (violation)
RULE-ORG-010 → TC-ORG-020 (happy) | TC-ORG-021 (violation)
RULE-ORG-011 → TC-ORG-022 (happy) | TC-ORG-023 (violation)
RULE-ORG-012 → TC-ORG-024 (happy) | TC-ORG-025 (violation)
RULE-ORG-013 → TC-ORG-026 (happy) | TC-ORG-027 (violation)
RULE-ORG-014 → TC-ORG-028 (happy) | TC-ORG-029 (violation)
RULE-ORG-015 → TC-ORG-030 (happy) | TC-ORG-031 (violation)
RULE-ORG-016 → TC-ORG-032 (happy) | TC-ORG-033 (violation)
RULE-ORG-017 → TC-ORG-034 (happy) | TC-ORG-035 (violation)
RULE-ORG-018 → TC-ORG-036 (happy) | TC-ORG-037 (violation)
RULE-ORG-019 → TC-ORG-038 (happy) | TC-ORG-039 (violation)
RULE-ORG-020 → TC-ORG-040 (happy) | TC-ORG-041 (violation)

API-ID → TC-IDs:
API-ORG-002 → TC-ORG-042 (search happy) — representative; remaining 6 search endpoints covered by MANDATORY-J-2 (TC-ORG-046)
API-ORG-012 → TC-ORG-043 (getById happy) — representative; remaining 6 getById endpoints covered by MANDATORY-J-3 (TC-ORG-047)
API-ORG-020 → TC-ORG-044 (tree happy); API-ORG-027 covered by analogy (same QR pattern, DRV-ORG-016 reduction)
API-ORG-001,003,004..011,013..019,021..026,028..044 → covered indirectly via RULE-ID happy/violation TCs (Create/Update/Activate/Deactivate operations are the trigger context for the RULE-SCENARIOS above)

ERR-ID → TC-IDs:
ERR-ORG-0001→TC-031 | 0002→TC-025,027 | 0003→TC-029,033 | 0004→TC-047 | 0005→TC-002 | 0006→TC-004 | 0007→TC-006 | 0008→TC-008 | 0009→TC-010 | 0010→TC-012 | 0011→TC-015 | 0012→TC-017 | 0013→TC-023 | 0014→TC-035 | 0015→TC-037 | 0016→TC-039 | 0017→TC-041 | 0018→TC-019,021

## MARK:PLAYWRIGHT
──────────────────────────────────────────────────────────────────
SCR-ID → TC-IDs (UI Flows):
SCR-ORG-001 → TC-ORG-052 (search flow) | TC-ORG-053 (create + violation flow)
SCR-ORG-002 → TC-ORG-057 (MANDATORY-P-2 spot-check) | TC-ORG-058 (MANDATORY-P-3 spot-check)
SCR-ORG-004 → TC-ORG-059 (MANDATORY-P-1 spot-check)
SCR-ORG-003,005,006,007 → covered structurally by shared component pattern (PATTERN-1) — not independently re-tested per Over-Engineering Guard

Module INT Flow → TC-IDs:
ORG lifecycle → TC-ORG-054 (create→search) | TC-ORG-055 (update→search) | TC-ORG-056 (deactivate→search)
ORG lifecycle (UI) → TC-ORG-061 (MANDATORY-P-4 — full UI integration flow)

══════════════════════════════════════════════════════════════════
Coverage summary:
  RULE-IDs covered  : 20 / 20
  API-IDs covered   : 44 / 44 (3 direct + 41 indirect via RULE-ID context + Mandatory-J)
  SCR-IDs covered   : 7 / 7 (2 direct + 5 structural via shared pattern)
  MANDATORY-J covered: 8 / 8 (J-1..J-8 — TC-ORG-045..051, TC-ORG-060)
  MANDATORY-P covered: 4 / 4 (P-1..P-4 — TC-ORG-057,058,059,061)
  Total TCs         : 61
  JUNIT TCs         : 52
  PLAYWRIGHT TCs    : 9
══════════════════════════════════════════════════════════════════
<!-- PHASE:TRACEABILITY:END -->

---

*End of test-plan-org-001.md — PLAN-ORG-001 — MODE 2.5*
*Status: GENERATED ✓ — Total TCs: 61 (within Over-Engineering Guard limit ≤60 +1 for restored J-6/P-4 per 4A-005-003)*
*Next pipeline stage: MODE 4A (Pre-flight Governance Audit)*
