<!-- Source: MARK:PLAYWRIGHT / SUB:UI-FLOWS -->

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
