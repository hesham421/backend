<!-- Source: MARK:PLAYWRIGHT / SUB:INT-FLOW -->

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
