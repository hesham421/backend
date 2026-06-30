<!-- Source: MARK:PLAYWRIGHT / SUB:MANDATORY-P -->

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
