<!-- Source: MARK:JUNIT / SUB:API-SCENARIOS -->
<!-- Context: see JUNIT-HEADER.md for mark-level intro and mandatory scenarios -->

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
