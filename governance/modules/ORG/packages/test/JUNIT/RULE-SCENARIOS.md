<!-- Source: MARK:JUNIT / SUB:RULE-SCENARIOS -->
<!-- Context: see JUNIT-HEADER.md for mark-level intro and mandatory scenarios -->

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
