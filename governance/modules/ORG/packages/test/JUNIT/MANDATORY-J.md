<!-- Source: MARK:JUNIT / SUB:MANDATORY-J -->
<!-- Context: see JUNIT-HEADER.md for mark-level intro and mandatory scenarios -->

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
