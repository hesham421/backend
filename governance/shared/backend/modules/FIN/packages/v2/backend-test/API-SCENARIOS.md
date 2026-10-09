<!-- source: PHASE:TEST-PLAN-BE / SUB:API-SCENARIOS -->
<!-- context: TEST-PLAN-BE-HEADER.md — phase-level preamble -->
<!-- traces: AC-FIN-001, AC-FIN-004, AC-FIN-005, AC-FIN-007, AC-FIN-008, AC-FIN-010, AC-FIN-014, AC-FIN-022, AC-FIN-023, AC-FIN-024, AC-FIN-025, AC-FIN-027, AC-FIN-028, AC-FIN-031, AC-FIN-036, AC-FIN-037, AC-FIN-038, AC-FIN-039, AC-FIN-040, AC-FIN-041, AC-FIN-042, AC-FIN-043, AC-FIN-044, AC-FIN-045, AC-FIN-046, AC-FIN-047, AC-FIN-049, AC-FIN-050, AC-FIN-051, AC-FIN-052, AC-FIN-058, AC-FIN-059, AC-FIN-060, AC-FIN-066, AC-FIN-067, AC-FIN-068, AC-FIN-071, AC-FIN-075, AC-FIN-076, AC-FIN-081, AC-FIN-083, API-FIN-002, API-FIN-006, API-FIN-007, API-FIN-010, API-FIN-011, API-FIN-013, API-FIN-014, API-FIN-016, API-FIN-018, API-FIN-019, API-FIN-020, API-FIN-021, API-FIN-022, API-FIN-023, API-FIN-026, API-FIN-027, API-FIN-028, API-FIN-029, API-FIN-030, API-FIN-031, API-FIN-032, API-FIN-038, API-FIN-039, API-FIN-040, REQ-FIN-001, REQ-FIN-004, REQ-FIN-005, REQ-FIN-007, REQ-FIN-008, REQ-FIN-010, REQ-FIN-014, REQ-FIN-022, REQ-FIN-023, REQ-FIN-024, REQ-FIN-025, REQ-FIN-027, REQ-FIN-028, REQ-FIN-031, REQ-FIN-036, REQ-FIN-037, REQ-FIN-038, REQ-FIN-039, REQ-FIN-040, REQ-FIN-041, REQ-FIN-042, REQ-FIN-043, REQ-FIN-044, REQ-FIN-045, REQ-FIN-046, REQ-FIN-047, REQ-FIN-049, REQ-FIN-050, REQ-FIN-054, REQ-FIN-055, REQ-FIN-056, REQ-FIN-062, REQ-FIN-063, REQ-FIN-066, REQ-FIN-070, REQ-FIN-071, REQ-FIN-075, REQ-FIN-077 -->
<!-- SUB:API-SCENARIOS:START traces=REQ-FIN-001,REQ-FIN-004,REQ-FIN-005,REQ-FIN-007,REQ-FIN-008,REQ-FIN-010,REQ-FIN-014,REQ-FIN-022,REQ-FIN-023,REQ-FIN-024,REQ-FIN-025,REQ-FIN-027,REQ-FIN-028,REQ-FIN-031,REQ-FIN-036,REQ-FIN-037,REQ-FIN-038,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-044,REQ-FIN-045,REQ-FIN-046,REQ-FIN-047,REQ-FIN-049,REQ-FIN-050,REQ-FIN-054,REQ-FIN-055,REQ-FIN-056,REQ-FIN-062,REQ-FIN-063,REQ-FIN-066,REQ-FIN-070,REQ-FIN-071,REQ-FIN-075,REQ-FIN-077,AC-FIN-001,AC-FIN-004,AC-FIN-005,AC-FIN-007,AC-FIN-008,AC-FIN-010,AC-FIN-014,AC-FIN-022,AC-FIN-023,AC-FIN-024,AC-FIN-025,AC-FIN-027,AC-FIN-028,AC-FIN-031,AC-FIN-036,AC-FIN-037,AC-FIN-038,AC-FIN-039,AC-FIN-040,AC-FIN-041,AC-FIN-042,AC-FIN-043,AC-FIN-044,AC-FIN-045,AC-FIN-046,AC-FIN-047,AC-FIN-049,AC-FIN-050,AC-FIN-051,AC-FIN-052,AC-FIN-058,AC-FIN-059,AC-FIN-060,AC-FIN-066,AC-FIN-067,AC-FIN-068,AC-FIN-071,AC-FIN-075,AC-FIN-076,AC-FIN-081,AC-FIN-083 -->
### SUB — API-SCENARIOS

The ACs whose Then is an endpoint's own success shape: creations, runs, reports, paging and
empty results, and the two permission denials AC-FIN-038 states.

**v2 cases open the SUB** (TC-FIN-093 … TC-FIN-129, the endpoint-driven half of CS-FIN-001), and
the v1 cases follow unchanged. The mapping endpoints API-FIN-038 … API-FIN-041 are gated by the
three `PERM_FIN_ACCOUNT_MAPPINGS_*` permissions that the v2 FIN security migration registers and
grants to `SYS_ADMIN`; without that grant every mapping call answers FIN-403-FORBIDDEN, so the
grant is a precondition of every mapping case below. Where a mapping for a PAYMENT_METHOD value
other than CHEQUE is set up, that value must first exist as host data in MDL (RULE-FIN-024):
only CHEQUE is seeded.

<!-- TC:TC-FIN-093:START traces=AC-FIN-047,REQ-FIN-047,API-FIN-020 -->
### TC-FIN-093 — an account code carried in the event payload is never used
Derived from : AC-FIN-047  (REQ-FIN-047)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : — (success path; REQ-FIN-047 holds by construction, no code is raised)
Scenario     : HAPPY · data class ATTACK · language ALL
Preconditions: an active rule for event type PAYMENT_RECEIVED whose debit line is MAPPING on
               PAYMENT_METHOD and whose other line resolves a leaf, active account; an active
               mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102; accounts 1101
               and 1102 are both active leaves, so 1101 **could** be posted to; an Open period
               covering the event date; the caller is the event consumer's service principal
               holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3001`, type
                  PAYMENT_RECEIVED, `fields.PAYMENT_METHOD` = "CHEQUE" and an extra payload field
                  `accountCode` = "1101" — the backend plan names the `fields.accountCode` form
Expected     : 200 · the entry POSTED with its debit line on account 1102, and **no line of the
               entry carries account 1101**. The extra field is never read: nothing in the
               payload is treated as an account code
Test data    : `EVT-3001`, "CHEQUE", 1101 and 1102 — AC-FIN-047's own values; the amount is a
               placeholder
<!-- TC:TC-FIN-093:END -->

<!-- TC:TC-FIN-095:START traces=AC-FIN-049,REQ-FIN-049,API-FIN-011 -->
### TC-FIN-095 — a MAPPING rule line records the business field that selects its account
Derived from : AC-FIN-049  (REQ-FIN-049)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-019 → passes (the MAPPING half of the spec is complete)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an active EventTypeRule for "EXPENSE_PAID"; `FIN_EVENT_BUSINESS_FIELD` is seeded
               in MDL with its 4 values (db-script v2 BLOCK 8); a finance administrator holding
               `PERM_FIN_RULES_UPDATE`
Host data    : ACCOUNTING_EVENT_TYPE EXPENSE_PAID (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with
                  `accountDerivationTypeCode` = MAPPING, `accountBusinessFieldCode` =
                  EXPENSE_TYPE_CODE, **no** `accountDerivationValue`, and valid amount-source,
                  direction and distribution fields
Expected     : 201 · `RuleLineResponse` with `accountBusinessFieldCode` = EXPENSE_TYPE_CODE
               (DBF-FIN-148) and `accountDerivationValue` empty (DBF-FIN-102): the line names the
               business fact and the mapping store names the account
Test data    : EXPENSE_PAID and EXPENSE_TYPE_CODE — AC-FIN-049's own values; `FIELD`, `DEBIT`
               and `FIXED` are seeded values (SRS A6)
<!-- TC:TC-FIN-095:END -->

<!-- TC:TC-FIN-096:START traces=AC-FIN-050,REQ-FIN-050,API-FIN-020 -->
### TC-FIN-096 — a cheque payment resolves to the bank account through its mapping
Derived from : AC-FIN-050  (REQ-FIN-050)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-020 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: active mappings (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH") → account 1101 (cash)
               and (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102 (bank); a rule for
               PAYMENT_RECEIVED whose debit line is MAPPING on PAYMENT_METHOD and whose credit
               side resolves a leaf, active account; an Open period covering the event date
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) · PAYMENT_METHOD CASH (active) — each
               created through API-MDL-006 `POST /api/v1/mdl/lookup-types/{id}/values` on its key's type
               (SRS A6: host data, never a product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3002`, type
                  PAYMENT_RECEIVED, `fields.PAYMENT_METHOD` = "CHEQUE" and amount 500.00
Expected     : 200 · a POSTED entry, `journalTypeCode` EVENT_GENERATED, whose debit line is
               500.00 on account 1102. The account is stored on the line (DBF-FIN-054), not
               looked up again when the entry is read
Test data    : `EVT-3002`, "CASH", "CHEQUE", 1101, 1102 and 500.00 — AC-FIN-050's own values
<!-- TC:TC-FIN-096:END -->

<!-- TC:TC-FIN-097:START traces=AC-FIN-051,REQ-FIN-050,API-FIN-020 -->
### TC-FIN-097 — a cash payment resolves to the cash account through its mapping
Derived from : AC-FIN-051  (REQ-FIN-050)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-020 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the mappings and rule of AC-FIN-050; an Open period covering the event date
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) · PAYMENT_METHOD CASH (active) — each
               created through API-MDL-006 `POST /api/v1/mdl/lookup-types/{id}/values` on its key's type
               (SRS A6: host data, never a product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3003`, type
                  PAYMENT_RECEIVED, `fields.PAYMENT_METHOD` = "CASH" and amount 500.00
Expected     : 200 · a POSTED entry whose debit line is 500.00 on account 1101. Together with
               TC-FIN-096 it shows one rule line taking two different accounts from two business
               values, with no code change between them
Test data    : `EVT-3003`, "CASH", 1101 and 500.00 — AC-FIN-051's own values
<!-- TC:TC-FIN-097:END -->

<!-- TC:TC-FIN-098:START traces=AC-FIN-052,REQ-FIN-050,API-FIN-020 -->
### TC-FIN-098 — an expense type resolves to its own expense account
Derived from : AC-FIN-052  (REQ-FIN-050)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-020 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: active mappings (EXPENSE_PAID, EXPENSE_TYPE_CODE, "TRAVEL") → account 5210 and
               (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT") → account 5220; a rule for EXPENSE_PAID
               whose debit line is MAPPING on EXPENSE_TYPE_CODE; an Open period
Host data    : ACCOUNTING_EVENT_TYPE EXPENSE_PAID (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3004`, type
                  EXPENSE_PAID, `fields.EXPENSE_TYPE_CODE` = "RENT" and amount 1200.00
Expected     : 200 · a POSTED entry whose debit line is 1200.00 on account 5220, and not on 5210
Test data    : `EVT-3004`, "TRAVEL", "RENT", 5210, 5220 and 1200.00 — AC-FIN-052's own values
<!-- TC:TC-FIN-098:END -->

<!-- TC:TC-FIN-104:START traces=AC-FIN-058,REQ-FIN-054,API-FIN-039 -->
### TC-FIN-104 — an account mapping is created active
Derived from : AC-FIN-058  (REQ-FIN-054)
Exercises    : API-FIN-039 POST /api/v1/fin/account-mappings
Rule / code  : RULE-FIN-022, RULE-FIN-023, RULE-FIN-024 all pass
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: no active mapping for (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE"); account 1102
               is an active leaf; PAYMENT_RECEIVED is an `ACCOUNTING_EVENT_TYPE` value in MDL and
               CHEQUE an active PAYMENT_METHOD value; a finance administrator holding
               `PERM_FIN_ACCOUNT_MAPPINGS_CREATE` and the `PERM_FIN_ACCOUNT_MAPPINGS_VIEW` gateway
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/account-mappings` with `eventTypeCode` = PAYMENT_RECEIVED,
                  `businessFieldCode` = PAYMENT_METHOD, `businessValue` = "CHEQUE" and
                  `accountId` = 1102
Expected     : 201 · `AccountMappingResponse` with `isActiveFl` = true and `accountId` = 1102,
               the three key fields stored as submitted **codes**, and the account's code and
               bilingual name returned beside it. No name is stored on the mapping itself
Test data    : PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE" and 1102 — AC-FIN-058's own values
<!-- TC:TC-FIN-104:END -->

<!-- TC:TC-FIN-105:START traces=AC-FIN-059,REQ-FIN-055,API-FIN-038 -->
### TC-FIN-105 — a mapping search returns exactly the rows its filters match
Derived from : AC-FIN-059  (REQ-FIN-055)
Exercises    : API-FIN-038 POST /api/v1/fin/account-mappings/search
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: 3 mappings for PAYMENT_RECEIVED — CASH active, CHEQUE active, TRANSFER_105 inactive
               — and 2 for EXPENSE_PAID; a caller holding `PERM_FIN_ACCOUNT_MAPPINGS_VIEW`
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) · ACCOUNTING_EVENT_TYPE EXPENSE_PAID
               (active) · PAYMENT_METHOD CASH (active) · PAYMENT_METHOD TRANSFER_105 (active; its
               mapping is saved, then deactivated through API-FIN-041) — each created through
               API-MDL-006 `POST /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host
               data, never a product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/account-mappings/search` with filters `eventTypeCode` =
                  PAYMENT_RECEIVED and `isActiveFl` = true, plus page and size
Expected     : 200 · `Page<AccountMappingResponse>` with **exactly 2** rows, CASH and CHEQUE —
               neither the inactive TRANSFER_105 row nor any EXPENSE_PAID row. Each row carries its
               account's code, `nameAr` and `nameEn`. Paging is server-side and the page
               metadata reflects the filtered total
Test data    : the 3 + 2 mappings and the filter pair — AC-FIN-059's own, the inactive row's payment method
               renamed for this case (ADR-FIN-068); accounts are placeholders
<!-- TC:TC-FIN-105:END -->

<!-- TC:TC-FIN-106:START traces=AC-FIN-060,REQ-FIN-056,API-FIN-040 -->
### TC-FIN-106 — changing a mapping's account leaves its key untouched
Derived from : AC-FIN-060  (REQ-FIN-056)
Exercises    : API-FIN-040 PUT /api/v1/fin/account-mappings/{id}
Rule / code  : RULE-FIN-023 → passes (the new account is an active leaf)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the mapping (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT") → account 5220; account
               5225 is an active leaf; a finance administrator holding
               `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`
Host data    : ACCOUNTING_EVENT_TYPE EXPENSE_PAID (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. PUT `/api/v1/fin/account-mappings/{RENT id}` with a body carrying `accountId` =
                  5225 and nothing else
Expected     : 200 · the mapping with `accountId` = 5225, and its `eventTypeCode`,
               `businessFieldCode` and `businessValue` unchanged; `updatedBy` and `updatedAt` are
               set by the platform. The key is not an input of this endpoint at all
Test data    : 5220, 5225 and "RENT" — AC-FIN-060's own values
<!-- TC:TC-FIN-106:END -->

<!-- TC:TC-FIN-112:START traces=AC-FIN-066,REQ-FIN-062,API-FIN-011 -->
### TC-FIN-112 — a rule line is saved with its two dimension tags
Derived from : AC-FIN-066  (REQ-FIN-062)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-025, RULE-FIN-026 → pass
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: dimensions ORGANISATION, holding value "ORG-1", and BRANCH, both active; an
               EventTypeRule — PAYMENT_RECEIVED in the event cases that reuse this line;
               `FIN_DIMENSION_VALUE_SOURCE` is seeded in MDL; a finance administrator holding
               `PERM_FIN_RULES_UPDATE`
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with a valid line whose
                  `dimensionTags` are {ORGANISATION: CONSTANT "ORG-1"} and
                  {BRANCH: BUSINESS_FIELD BRANCH_CODE}
Expected     : 201 · the RuleLine and **2** RuleLineDimension rows, one per tag. The ORGANISATION
               tag carries `valueSourceCode` CONSTANT with the "ORG-1" value's id and no business
               field; the BRANCH tag carries BUSINESS_FIELD with `businessFieldCode` BRANCH_CODE
               and no value (DBF-FIN-160, DBF-FIN-161, DBF-FIN-164). The response returns the
               line with both tags
Test data    : ORGANISATION, "ORG-1", BRANCH and BRANCH_CODE — AC-FIN-066's own values
<!-- TC:TC-FIN-112:END -->

<!-- TC:TC-FIN-113:START traces=AC-FIN-067,REQ-FIN-063,API-FIN-020 -->
### TC-FIN-113 — a CONSTANT tag stamps its value on the event-built line
Derived from : AC-FIN-067  (REQ-FIN-063)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : — (success path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the rule line of AC-FIN-066 on the PAYMENT_RECEIVED rule, the rule balancing and
               its accounts leaf and active; BRANCH holds an active value "BR-07", without which
               RULE-FIN-027 would refuse the event (AC-FIN-068's own Given); an Open period
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3011` and
                  `fields.BRANCH_CODE` = "BR-07"
               2. GET the posted entry through API-FIN-022
Expected     : 200 · POSTED, and the journal line built from that rule line carries
               ORGANISATION = "ORG-1", taken from the tag and not from the event
Test data    : `EVT-3011`, "ORG-1" and "BR-07" — AC-FIN-067's own values. AC-FIN-068 names the
               same event, so on one dataset this TC and TC-FIN-114 read the same posting
<!-- TC:TC-FIN-113:END -->

<!-- TC:TC-FIN-114:START traces=AC-FIN-068,REQ-FIN-063,API-FIN-020 -->
### TC-FIN-114 — a BUSINESS_FIELD tag resolves its value by code from the event
Derived from : AC-FIN-068  (REQ-FIN-063)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-027 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the rule line of AC-FIN-066; BRANCH holds an active value with code "BR-07"; an
               Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3011` and
                  `fields.BRANCH_CODE` = "BR-07"
               2. GET the posted entry through API-FIN-022
Expected     : 200 · POSTED, and the journal line built from that rule line carries BRANCH = the
               DimensionValue whose code is "BR-07" — resolved by exact code **inside the BRANCH
               dimension**, never by a value of another dimension that happens to share the code
Test data    : `EVT-3011` and "BR-07" — AC-FIN-068's own values
<!-- TC:TC-FIN-114:END -->

<!-- TC:TC-FIN-117:START traces=AC-FIN-071,REQ-FIN-066,API-FIN-020 -->
### TC-FIN-117 — both resolved dimension values are validated and written on the line
Derived from : AC-FIN-071  (REQ-FIN-066)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-009 → passes for both values
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the rule line of AC-FIN-066; "ORG-1" and "BR-07" both active in their own
               dimensions; an Open period
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with reference `EVT-3012` and
                  `fields.BRANCH_CODE` = "BR-07"
               2. GET the posted entry through API-FIN-022
Expected     : 200 · the entry is POSTED with **2** JournalLineDimension rows on that line, one
               for ORGANISATION and one for BRANCH — the same validation a manual line receives,
               applied without a bypass
Test data    : `EVT-3012`, "ORG-1" and "BR-07" — AC-FIN-071's own values
<!-- TC:TC-FIN-117:END -->

<!-- TC:TC-FIN-121:START traces=AC-FIN-075,REQ-FIN-070,API-FIN-022 -->
### TC-FIN-121 — an event-built line and a manual line read back in one dimension shape
Derived from : AC-FIN-075  (REQ-FIN-070)
Exercises    : API-FIN-022 GET /api/v1/fin/journal-entries/{id}, with API-FIN-020 and
               API-FIN-019 as setup
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: entry A posted from event `EVT-3016` with BRANCH "BR-07" on its debit line
               (API-FIN-020), and manual entry B posted with BRANCH "BR-07" on its debit line
               (API-FIN-019); a caller holding `PERM_FIN_JOURNAL_ENTRIES_VIEW`
Steps        : 1. GET entry A
               2. GET entry B
Expected     : each debit line holds **exactly 1** JournalLineDimension row with dimension
               BRANCH and the same `dimensionValueId`. The two responses differ in
               `journalTypeCode` and `eventReference` only, never in the shape of their lines
Test data    : `EVT-3016` and "BR-07" — AC-FIN-075's own values
<!-- TC:TC-FIN-121:END -->

<!-- TC:TC-FIN-122:START traces=AC-FIN-076,REQ-FIN-071,API-FIN-032 -->
### TC-FIN-122 — the dimension report groups event-built and manual lines together
Derived from : AC-FIN-076  (REQ-FIN-071)
Exercises    : API-FIN-032 GET /api/v1/fin/reports/dimension
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the two entries of AC-FIN-075, each debiting account 5100 with 100.00 under
               BRANCH "BR-07"; a controller holding `PERM_FIN_DIMENSION_REPORTS_VIEW`
Steps        : 1. GET `/api/v1/fin/reports/dimension` with `dimensionId` = the BRANCH dimension
Expected     : 200 · **1** row for (5100, "BR-07") with a debit total of 200.00 — not two rows
               split by journal type. The dimension report is no longer empty for event-built
               entries
Test data    : 5100, 100.00, 200.00 and "BR-07" — AC-FIN-076's own values
<!-- TC:TC-FIN-122:END -->

<!-- TC:TC-FIN-127:START traces=AC-FIN-081,REQ-FIN-075,API-FIN-020 -->
### TC-FIN-127 — the published catalogue documents the period-not-open code once
Derived from : AC-FIN-081  (REQ-FIN-075)
Exercises    : the published API documentation of API-FIN-020 — its error catalogue, read as a
               document; no call is made
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN (409), the row under inspection
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: FIN's API documentation as the backend publishes it for v2
Steps        : 1. read the error catalogue of the published documentation
               2. count and read the rows naming FIN-409-PERIOD-NOT-OPEN
Expected     : the catalogue lists FIN-409-PERIOD-NOT-OPEN **exactly once**, with its message —
               ar: «الفترة المستهدفة غير مفتوحة» · en: "The target period is not open" — and the
               note that the rejection clears once the target period is opened. The copy
               republished for v2 carries both (ADR-FIN-064)
Test data    : none invented
<!-- TC:TC-FIN-127:END -->

<!-- TC:TC-FIN-129:START traces=AC-FIN-083,REQ-FIN-077,API-FIN-021,API-FIN-028,API-FIN-032 -->
### TC-FIN-129 — an entry posted through a mapping and tags reverses like any other
Derived from : AC-FIN-083  (REQ-FIN-077)
Exercises    : API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse, then
               API-FIN-028 GET /api/v1/fin/reports/account-ledger and
               API-FIN-032 GET /api/v1/fin/reports/dimension
Rule / code  : RULE-FIN-011 → success path · RULE-FIN-016 holds (the original is never modified)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: entry `JV-2026-000600`, POSTED from event `EVT-4006`, with a mapped debit account
               and BRANCH "BR-07" on its lines; its period still Open; an accountant holding
               `PERM_FIN_JOURNAL_ENTRIES_REVERSE`
Steps        : 1. POST `/api/v1/fin/journal-entries/{JV-2026-000600 id}/reverse`
               2. read the ledger of each account the entry touches, and the dimension report by
                  BRANCH, over the entry's date
Expected     : step 1 → 201 · a POSTED reversal whose lines carry the **same accounts, amounts and
                  dimension values** with the opposite direction; the original stays POSTED and
                  the two are linked through `originalEntryId` / `reversalEntryId`. No mapping is
                  consulted — the reversal reads the posted lines, never the rules that built them.
               step 2 → every account balance and every BRANCH balance of the pair nets to 0.00
Test data    : `JV-2026-000600`, `EVT-4006` and "BR-07" — AC-FIN-083's own values
<!-- TC:TC-FIN-129:END -->

<!-- TC:TC-FIN-001:START traces=AC-FIN-001,REQ-FIN-001,API-FIN-002 -->
### TC-FIN-001 — an account is created inside the chart hierarchy
Derived from : AC-FIN-001  (REQ-FIN-001)
Exercises    : API-FIN-002 POST /api/v1/fin/accounts
Rule / code  : — (happy path; RULE-FIN-001 is not triggered)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a finance administrator holding `PERM_FIN_ACCOUNTS_CREATE` (and the
               `PERM_FIN_ACCOUNTS_VIEW` gateway); MDL holds the `ACCOUNT_TYPE` and `DEBIT_CREDIT`
               types with their seeded values, so XM-FIN-001 can validate the two codes; the code
               submitted is not yet used
Steps        : 1. POST `/api/v1/fin/accounts` with a unique `code`, `nameAr`, `nameEn`,
                  `accountTypeCode`, `natureCode`, an optional `parentAccountId` and `isLeafFl`
Expected     : 201 · `AccountResponse` carrying every submitted field plus the system-set
               `accountPk` and the four audit columns. `accountTypeCode` and `natureCode` are
               stored as the submitted **codes** (DBF-FIN-005, DBF-FIN-006), never as numeric
               keys. A following API-FIN-001 search on that code returns exactly the new row
Test data    : `ASSET` and `DEBIT` are seeded `ACCOUNT_TYPE` / `DEBIT_CREDIT` values (SRS A6);
               the chart code and the two names are placeholders — the AC names none
<!-- TC:TC-FIN-001:END -->

<!-- TC:TC-FIN-004:START traces=AC-FIN-004,REQ-FIN-004,API-FIN-006 -->
### TC-FIN-004 — a dimension is created and is active
Derived from : AC-FIN-004  (REQ-FIN-004)
Exercises    : API-FIN-006 POST /api/v1/fin/dimensions
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a caller holding `PERM_FIN_DIMENSIONS_CREATE`; the code is not yet used
Steps        : 1. POST `/api/v1/fin/dimensions` with code `PROJECT` and both names
Expected     : 201 · `DimensionResponse` with `isActiveFl` (DBF-FIN-018) true — AC-FIN-004's
               "creates an active Dimension". The dimension is immediately available to carry
               values and to be cited on a journal line's dimension pair
Test data    : `PROJECT` — ENT-FIN-002's own example code; the names are placeholders
<!-- TC:TC-FIN-004:END -->

<!-- TC:TC-FIN-005:START traces=AC-FIN-005,REQ-FIN-005,API-FIN-007 -->
### TC-FIN-005 — a value is created under its dimension
Derived from : AC-FIN-005  (REQ-FIN-005)
Exercises    : API-FIN-007 POST /api/v1/fin/dimensions/{id}/values
Rule / code  : — (happy path; RULE-FIN-002 passes)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the dimension `PROJECT` exists and holds no value with the submitted code; a
               caller holding `PERM_FIN_DIMENSIONS_CREATE`
Steps        : 1. POST `/api/v1/fin/dimensions/{PROJECT id}/values` with code `NORTH`, both names
                  and a `sortOrder`
Expected     : 201 · `DimensionValueResponse` whose `dimensionId` (DBF-FIN-024) is the path id
               and whose `isActiveFl` is true. API-FIN-008 filtered on that dimension returns it
Test data    : `NORTH` — the code AC-FIN-006 names for this dimension; `sortOrder` is a
               placeholder integer
<!-- TC:TC-FIN-005:END -->

<!-- TC:TC-FIN-007:START traces=AC-FIN-007,REQ-FIN-007,API-FIN-010 -->
### TC-FIN-007 — an event type gets its single active rule
Derived from : AC-FIN-007  (REQ-FIN-007)
Exercises    : API-FIN-010 POST /api/v1/fin/event-rules
Rule / code  : — (happy path; the one-rule-per-type uniqueness passes)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: FIN's onboarding has completed, so the `ACCOUNTING_EVENT_TYPE` type is registered
               in MDL (REQ-FIN-045) and FIN's screens and actions are registered in SEC
               (REQ-FIN-044); the event type carries no existing rule; a caller holding
               `PERM_FIN_RULES_CREATE`
Steps        : 1. POST `/api/v1/fin/event-rules` with `eventTypeCode`, `nameAr`, `nameEn`
Expected     : 201 · `EventTypeRuleResponse` with `isActiveFl` (DBF-FIN-093) true — the single
               active rule for that type, which §6.4 allows exactly one of. A second create for
               the same type answers FIN-409-RULE-DUP
Test data    : the event type code is a placeholder — `ACCOUNTING_EVENT_TYPE` seeds no value
<!-- TC:TC-FIN-007:END -->

<!-- TC:TC-FIN-008:START traces=AC-FIN-008,REQ-FIN-008,API-FIN-011 -->
### TC-FIN-008 — a rule line records its three references separately
Derived from : AC-FIN-008  (REQ-FIN-008)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : — (happy path; RULE-FIN-003 passes, no percentage line being present;
               RULE-FIN-018 and RULE-FIN-019 pass for a complete CONSTANT line)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an EventTypeRule exists; a caller holding `PERM_FIN_RULES_UPDATE`; MDL holds the
               four lookup types the line's codes belong to; the constant account code names a
               leaf, active account
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with an account-derivation spec
                  (`accountDerivationTypeCode` = CONSTANT + `accountDerivationValue` = the
                  constant account code, and no `accountBusinessFieldCode`), an amount-source
                  spec (`amountSourceTypeCode` + `amountSourceValue`) and a `directionCode`
Expected     : 201 · `RuleLineResponse` in which the account derivation, the amount source and the
               direction are **three separate stored references** (DBF-FIN-101 … DBF-FIN-105), not
               one fused field — the deliberate §6.2 separation REQ-FIN-008 states. On this
               CONSTANT line `accountBusinessFieldCode` (DBF-FIN-148) is empty
Test data    : `CONSTANT` (`ACCOUNT_DERIVATION_TYPE` — since v2 its only other active value is
               MAPPING, and DIRECT is refused, TC-FIN-094), `FIELD` (`AMOUNT_SOURCE_TYPE`),
               `FIXED` (`DISTRIBUTION_TYPE`), `DEBIT` (`DEBIT_CREDIT`) — all seeded values (SRS
               A6); the account code and the source value are placeholders
<!-- TC:TC-FIN-008:END -->

<!-- TC:TC-FIN-010:START traces=AC-FIN-010,REQ-FIN-010,API-FIN-020 -->
### TC-FIN-010 — an arriving event is turned into an entry by its type's rule
Derived from : AC-FIN-010  (REQ-FIN-010)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : — (happy path; RULE-FIN-004 and RULE-FIN-005 both pass, then REQ-FIN-017's checks)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an event type with an active EventTypeRule whose lines derive leaf, active
               accounts and balance; an Open period covering the document date; the event's
               reference has produced no posted entry
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with a canonical event of that
                  type
Expected     : 200 · `ApiResponse<JournalEntryResponse>`, the status the published contract
               states. A DRAFT is built whose lines follow the rule — account derived per
               `accountDerivationTypeCode` (the constant code, or since v2 the account a mapping
               resolves), amount per `amountSourceTypeCode`, one `directionCode` per line — and
               the same call proceeds straight through validation to POSTED (REQ-FIN-017).
               `journalTypeCode` is EVENT_GENERATED and `eventReference` carries the event's own
               reference. No account number ever appears in the event payload: the rule derives
               it
Test data    : the event payload is opaque by POL-FIN-020; no field of it is invented here
<!-- TC:TC-FIN-010:END -->

<!-- TC:TC-FIN-014:START traces=AC-FIN-014,REQ-FIN-014,API-FIN-019 -->
### TC-FIN-014 — a manual entry follows exactly the same path as any other source
Derived from : AC-FIN-014  (REQ-FIN-014)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-017 passes (header coherent), then RULE-FIN-006 … RULE-FIN-009 pass
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a fiscal period that is Open and belongs to the submitted fiscal year, its
               `[startDate, endDate]` covering the document date; at least two balanced lines on
               leaf, active accounts; an accountant holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. POST `/api/v1/fin/journal-entries` with the header and its nested lines
Expected     : 201 · a DRAFT is built and proceeds to REQ-FIN-017 in the same call, so the
               response carries `statusCode` = POSTED. No separate approval step exists for the
               entry, and `docNo`, `statusCode` and `postedAt` are absent from the request and
               present in the response
Test data    : two lines — AC-FIN-014's own "at least two"; accounts and amounts are placeholders
<!-- TC:TC-FIN-014:END -->

<!-- TC:TC-FIN-022:START traces=AC-FIN-022,REQ-FIN-022,API-FIN-013 -->
### TC-FIN-022 — a recurring or reversing template is stored with its schedule
Derived from : AC-FIN-022  (REQ-FIN-022)
Exercises    : API-FIN-013 POST /api/v1/fin/recurring-templates
Rule / code  : — (happy path; the frequency-required condition is satisfied)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a caller holding `PERM_FIN_RECURRING_TEMPLATES_CREATE`; MDL holds
               `RECURRING_SCHEDULE_TYPE` and `RECURRING_FREQUENCY`
Steps        : 1. POST `/api/v1/fin/recurring-templates` with `scheduleTypeCode` = `RECURRING`,
                  a `frequencyCode`, a `startDate` and at least two balanced lines
Expected     : 201 · `RecurringTemplateResponse` with `isActiveFl` (DBF-FIN-117) true and
               `nextRunDate` (DBF-FIN-115) set to the submitted `startDate`, its lines stored with
               it in one request. A `RECURRING` template without a `frequencyCode` would answer
               FIN-400-MISSING-FREQUENCY; a `REVERSING` one needs none
Test data    : `RECURRING`, `REVERSING`, `MONTHLY` — seeded values (SRS A6); dates and amounts are
               placeholders
<!-- TC:TC-FIN-022:END -->

<!-- TC:TC-FIN-023:START traces=AC-FIN-023,REQ-FIN-023,API-FIN-014 -->
### TC-FIN-023 — a due recurring template posts one entry and advances its next run date
Derived from : AC-FIN-023  (REQ-FIN-023)
Exercises    : API-FIN-014 POST /api/v1/fin/recurring-templates/{id}/run
Rule / code  : RULE-FIN-006 … RULE-FIN-009 re-checked at run time
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an **active** recurring template whose `nextRunDate` is today — AC-FIN-023's own
               Given; an Open period covering that date; every template line's account still leaf
               and active; a caller holding `PERM_FIN_RECURRING_TEMPLATES_UPDATE`
Steps        : 1. POST `/api/v1/fin/recurring-templates/{id}/run`
Expected     : 201 · exactly **one** `JournalEntryResponse`, `journalTypeCode` = RECURRING and
               `statusCode` = POSTED, and the template's `nextRunDate` advanced per its
               `frequencyCode`. The lines are re-validated at run time even though they were
               checked at create time, because accounts and periods may have changed since
Test data    : none invented; "today" is AC-FIN-023's own
<!-- TC:TC-FIN-023:END -->

<!-- TC:TC-FIN-024:START traces=AC-FIN-024,REQ-FIN-024,API-FIN-014 -->
### TC-FIN-024 — a reversing template's entry is mirrored in the next period
Derived from : AC-FIN-024  (REQ-FIN-024)
Exercises    : API-FIN-014 POST /api/v1/fin/recurring-templates/{id}/run
Rule / code  : RULE-FIN-011 (the mirror) and RULE-FIN-012 (its period) — success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an active template whose `scheduleTypeCode` is `REVERSING`; period N is Open and
               period N+1 exists inside the same fiscal year
Steps        : 1. POST `/api/v1/fin/recurring-templates/{id}/run`
Expected     : 201 · the template's own entry posts in period N, and a **linked reversal** posts
               in period N+1 — same line amounts, opposite `directionCode`, the two rows carrying
               each other's ids in `originalEntryId` / `reversalEntryId`. Both entries are
               individually balanced, so the pair nets to zero across the two periods
Test data    : none invented; "period N / N+1" is AC-FIN-024's own wording
<!-- TC:TC-FIN-024:END -->

<!-- TC:TC-FIN-025:START traces=AC-FIN-025,REQ-FIN-025,API-FIN-016 -->
### TC-FIN-025 — an allocation rule is stored with its targets
Derived from : AC-FIN-025  (REQ-FIN-025)
Exercises    : API-FIN-016 POST /api/v1/fin/allocation-rules
Rule / code  : RULE-FIN-003 (reused over the target set) passes
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a source account exists; a caller holding `PERM_FIN_ALLOCATION_RULES_CREATE`; MDL
               holds `DISTRIBUTION_TYPE`
Steps        : 1. POST `/api/v1/fin/allocation-rules` with a `sourceAccountId` and at least one
                  target carrying a `distributionTypeCode`
Expected     : 201 · `AllocationRuleResponse` with `isActiveFl` (DBF-FIN-134) true and its targets
               stored in the same request, each recording how the source account's balance
               distributes to it
Test data    : `FIXED`, `PERCENTAGE`, `REMAINDER` — seeded `DISTRIBUTION_TYPE` values (SRS A6);
               account ids are placeholders
<!-- TC:TC-FIN-025:END -->

<!-- TC:TC-FIN-027:START traces=AC-FIN-027,REQ-FIN-027,API-FIN-018 -->
### TC-FIN-027 — a filtered search returns exactly the matching entries, unmodified
Derived from : AC-FIN-027  (REQ-FIN-027)
Exercises    : API-FIN-018 POST /api/v1/fin/journal-entries/search
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: entries exist across several `statusCode` values and several periods; a caller
               holding `PERM_FIN_JOURNAL_ENTRIES_VIEW`
Steps        : 1. POST `/api/v1/fin/journal-entries/search` with `periodId` and `statusCode` in
                  the body filters, plus page and size
Expected     : 200 · `Page<JournalEntryResponse>` holding **exactly** the entries matching both
               filters and no others, each returned unmodified — no field recomputed, no status
               changed by the read. Paging is server-side and the page metadata reflects the
               filtered total, not the table total
Test data    : the filter pair is AC-FIN-027's own ("filters by period and status"); ids are
               placeholders
<!-- TC:TC-FIN-027:END -->

<!-- TC:TC-FIN-028:START traces=AC-FIN-028,REQ-FIN-028,API-FIN-021 -->
### TC-FIN-028 — a reversal mirrors the original line for line and links both ways
Derived from : AC-FIN-028  (REQ-FIN-028)
Exercises    : API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse
Rule / code  : RULE-FIN-011 → success path, no distinct code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a POSTED entry with exactly three lines, whose own period is still Open; a caller
               holding `PERM_FIN_JOURNAL_ENTRIES_REVERSE`
Steps        : 1. POST `/api/v1/fin/journal-entries/{id}/reverse`
               2. GET both rows through API-FIN-022
Expected     : 201 · a new POSTED entry with `journalTypeCode` = REVERSAL carrying the **same
               three line amounts with opposite directions**. Both entries reference each other:
               the reversal's `originalEntryId` (DBF-FIN-042) and the original's `reversalEntryId`
               (DBF-FIN-043). The original **stays POSTED** — classic reversal, SRS A7 — so both
               rows remain visible to the POSTED-only reports and net to zero
Test data    : three lines — AC-FIN-028's own count
<!-- TC:TC-FIN-028:END -->

<!-- TC:TC-FIN-031:START traces=AC-FIN-031,REQ-FIN-031,API-FIN-023 -->
### TC-FIN-031 — a fiscal year is created with its periods, each Open
Derived from : AC-FIN-031  (REQ-FIN-031)
Exercises    : API-FIN-023 POST /api/v1/fin/fiscal-years
Rule / code  : — (happy path; the year-code uniqueness check passes)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a finance administrator holding `PERM_FIN_PERIODS_CREATE`; the year code is not
               yet used
Steps        : 1. POST `/api/v1/fin/fiscal-years` with code `2026`, a start date, an end date
                  spanning that whole calendar year, and `periodCount` = 12
Expected     : 201 · `FiscalYearResponse` with `statusCode` = OPEN **and its generated periods**,
               twelve of them, each `statusCode` = OPEN — AC-FIN-031's "each initially Open".
               Because the count is twelve over a whole year, each period is one calendar month
               and its `nameAr` / `nameEn` (DBF-FIN-078, DBF-FIN-079) are that month's own name in
               Arabic and English from the platform's locale data. `periodCount` is a transient
               input and is not a stored column
Test data    : `2026` — the fiscal year code the `docNo` example in the SRS itself uses; twelve is
               REQ-FIN-031's own stated count
<!-- TC:TC-FIN-031:END -->

<!-- TC:TC-FIN-036:START traces=AC-FIN-036,REQ-FIN-036,API-FIN-027 -->
### TC-FIN-036 — year-end close posts a balanced closing entry and the next year's opening entry
Derived from : AC-FIN-036  (REQ-FIN-036)
Exercises    : API-FIN-027 POST /api/v1/fin/fiscal-years/{id}/year-end-close
Rule / code  : RULE-FIN-006, RULE-FIN-007, RULE-FIN-009 apply to both generated entries;
               RULE-FIN-008 does **not** — the rule exempts these two by its own carve-out
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a fiscal year still `statusCode` = OPEN whose every period is HARD_CLOSE; exactly
               one account marked as Retained Earnings (DBF-FIN-147); a successor fiscal year
               whose `startDate` is the day after this year's `endDate`; an approver holding
               `PERM_FIN_PERIODS_CLOSE_APPROVE`
Steps        : 1. POST `/api/v1/fin/fiscal-years/{id}/year-end-close`
Expected     : 201 · both entries returned. The closing entry posts into the year's **last**
               period with result accounts closed to Retained Earnings; the opening entry is
               generated for the next year from the resulting balance-sheet balances. **Each is
               individually balanced** — AC-FIN-036's own Then. The fiscal year moves to CLOSED
               and its periods to YEAR_END_CLOSE. FIN-409-PERIOD-NOT-OPEN is never raised, and the
               Retained Earnings account is derived by the system, never supplied by the caller
Test data    : none invented; the two entries' composition is AC-FIN-036's own
<!-- TC:TC-FIN-036:END -->

<!-- TC:TC-FIN-037:START traces=AC-FIN-037,REQ-FIN-037,API-FIN-026 -->
### TC-FIN-037 — the close approval is recorded as an act of its own
Derived from : AC-FIN-037  (REQ-FIN-037)
Exercises    : API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close
Rule / code  : — (happy path; RULE-FIN-015's gate has already admitted the caller)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a period ready to hard-close (SOFT_CLOSE); entries exist inside that period whose
               `createdBy` (DBF-FIN-047) is some other principal; the approver holds
               `PERM_FIN_PERIODS_CLOSE_APPROVE`
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/hard-close`
               2. read back the period and any entry of that period
Expected     : `closedBy` (DBF-FIN-083) and `closedAt` (DBF-FIN-084) hold the approving principal
               and the moment of approval, and `closedBy` is **distinct from every entry's
               `createdBy` in that period** — AC-FIN-037's own Then. The approval is therefore
               legible as a separate act, not inferred from any entry's audit columns
Test data    : principals are placeholders; the distinctness is the fact the AC states
<!-- TC:TC-FIN-037:END -->

<!-- TC:TC-FIN-038:START traces=AC-FIN-038,REQ-FIN-038,API-FIN-026 -->
### TC-FIN-038 — entry-creation permission alone does not buy a period close
Derived from : AC-FIN-038  (REQ-FIN-038)
Exercises    : API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close
Rule / code  : RULE-FIN-015 → FIN-403-FORBIDDEN (403)
Scenario     : PERMISSION · data class ATTACK · language ALL
Preconditions: a role holding `PERM_FIN_JOURNAL_ENTRIES_CREATE` and **not**
               `PERM_FIN_PERIODS_CLOSE_APPROVE`; a period in SOFT_CLOSE, so nothing but the
               permission can refuse the call
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/hard-close` as that role's user
Expected     : 403 with code FIN-403-FORBIDDEN —
               ar: «لا تملك صلاحية المالية المطلوبة لهذه العملية» ·
               en: "You do not hold the Finance permission required for this operation".
               The denial comes from the permission gate — SEC's own mechanism, as AC-FIN-038
               words it — and the period's `statusCode` is unchanged.
               **Assert nothing else.** There is no global user-set-disjointness check: a caller
               who does hold `PERM_FIN_PERIODS_CLOSE_APPROVE` succeeds even when some other user
               in the system also holds the creation permission, and `FIN-403-SOD-VIOLATION` is
               struck as unreachable. A test asserting that older behaviour fails by design
Test data    : the two permission codes are the registry's own
<!-- TC:TC-FIN-038:END -->

<!-- TC:TC-FIN-039:START traces=AC-FIN-039,REQ-FIN-039,API-FIN-028 -->
### TC-FIN-039 — the account ledger is computed live from posted lines
Derived from : AC-FIN-039  (REQ-FIN-039)
Exercises    : API-FIN-028 GET /api/v1/fin/reports/account-ledger
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: one account with POSTED lines across two periods; a caller holding
               `PERM_FIN_ACCOUNT_LEDGER_VIEW`
Steps        : 1. GET `/api/v1/fin/reports/account-ledger` with that `accountId` and a date range
                  spanning both periods
               2. post one further entry touching the same account, then repeat step 1
Expected     : a running-balance list built from those POSTED lines at the moment of the call,
               each row linking to its `journalEntryId`. Step 2's repeat reflects the new line
               immediately — **no stored balance column exists anywhere** to fall out of sync,
               which is AC-FIN-039's own "never from a stored column". Lines of entries that are
               not POSTED never appear
Test data    : two periods — AC-FIN-039's own; ids are placeholders
<!-- TC:TC-FIN-039:END -->

<!-- TC:TC-FIN-040:START traces=AC-FIN-040,REQ-FIN-040,API-FIN-029 -->
### TC-FIN-040 — the trial balance always balances
Derived from : AC-FIN-040  (REQ-FIN-040)
Exercises    : API-FIN-029 GET /api/v1/fin/reports/trial-balance
Rule / code  : RULE-FIN-006 restated as a report-level guarantee — an invariant by construction,
               not a separate check
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: any set of POSTED entries in a period; a controller holding
               `PERM_FIN_TRIAL_BALANCE_VIEW`
Steps        : 1. GET `/api/v1/fin/reports/trial-balance` with that `periodId`
               2. GET it again with `periodId` omitted
Expected     : 200 · one row per account, and **total debit balances equal total credit balances
               exactly** in both calls — AC-FIN-040's own Then. The sums match because every
               contributing entry individually balanced, not because the report corrects
               anything. Omitting `periodId` is a legitimate "no period narrowing" request and
               stays a 200
Test data    : none invented
<!-- TC:TC-FIN-040:END -->

<!-- TC:TC-FIN-041:START traces=AC-FIN-041,REQ-FIN-041,API-FIN-030 -->
### TC-FIN-041 — the new year's opening balances equal the prior year's closing balances
Derived from : AC-FIN-041  (REQ-FIN-041)
Exercises    : API-FIN-030 GET /api/v1/fin/reports/balance-sheet
Rule / code  : — (read-only; continuity is guaranteed by REQ-FIN-036's opening entry, not
               re-validated at read time)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a completed year-end close (the TC-FIN-036 outcome); a controller holding
               `PERM_FIN_BALANCE_SHEET_VIEW`
Steps        : 1. GET `/api/v1/fin/reports/balance-sheet` for the closed year
               2. GET it for the new year
Expected     : for **every** balance-sheet account, the new year's opening balance equals the
               prior year's closing balance — AC-FIN-041's own Then. Balances are grouped
               ASSET / LIABILITY / EQUITY and computed live from POSTED lines, the sign following
               each account's `natureCode`
Test data    : none invented
<!-- TC:TC-FIN-041:END -->

<!-- TC:TC-FIN-042:START traces=AC-FIN-042,REQ-FIN-042,API-FIN-031 -->
### TC-FIN-042 — the income statement opens a new year at zero
Derived from : AC-FIN-042  (REQ-FIN-042)
Exercises    : API-FIN-031 GET /api/v1/fin/reports/income-statement
Rule / code  : — (read-only; the zero opening is guaranteed by REQ-FIN-036 closing result
               accounts to Retained Earnings)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a completed year-end close, and **no posting yet** in the new year; a controller
               holding `PERM_FIN_INCOME_STATEMENT_VIEW`
Steps        : 1. GET `/api/v1/fin/reports/income-statement` for the new fiscal year
Expected     : 200 · **every** revenue and expense account shows a zero balance — AC-FIN-042's
               own Then — while the balance-sheet accounts of TC-FIN-041 carry their brought-
               forward balances. The contrast between the two reports is the point: result
               accounts restart, balance-sheet accounts continue
Test data    : none invented
<!-- TC:TC-FIN-042:END -->

<!-- TC:TC-FIN-043:START traces=AC-FIN-043,REQ-FIN-043,API-FIN-032 -->
### TC-FIN-043 — a dimension report never merges two dimension values into one account row
Derived from : AC-FIN-043  (REQ-FIN-043)
Exercises    : API-FIN-032 GET /api/v1/fin/reports/dimension
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: POSTED lines on **one** account carrying two different `PROJECT` dimension values;
               a controller holding `PERM_FIN_DIMENSION_REPORTS_VIEW`
Steps        : 1. GET `/api/v1/fin/reports/dimension` with `dimensionId` = the `PROJECT` dimension
Expected     : 200 · **two separate rows** for that account, one per project value, not one
               combined row — AC-FIN-043's own Then. The aggregation key is the full
               account-and-dimension-value combination, never the base account alone when a
               dimension is in play
Test data    : the `PROJECT` dimension and two of its values — AC-FIN-043's own shape
<!-- TC:TC-FIN-043:END -->

<!-- TC:TC-FIN-044:START traces=AC-FIN-044,REQ-FIN-044 -->
### TC-FIN-044 — FIN registers itself into SEC as data
Derived from : AC-FIN-044  (REQ-FIN-044)
Exercises    : — deployment-time onboarding; FIN publishes no endpoint for it, so no `API-*` is
               cited (ADR-FIN-009)
Rule / code  : — (registration, no rule)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: SEC v1 is gated and reachable — AC-FIN-044's own Given
Steps        : 1. run FIN's onboarding once against a database that has none of its registry
                  rows, together with FIN's forward security migrations up to and including the
                  v2 one that registers FIN_ACCOUNT_MAPPINGS
               2. read back SEC's module, screen and action registries
Expected     : SEC holds **one** ModuleRegistry row for FIN (ENT-SEC-004), **one** ScreenRegistry
               row per FIN screen (ENT-SEC-005) — the thirteen page codes FIN_ACCOUNTS …
               FIN_DIMENSION_REPORTS and, since v2, FIN_ACCOUNT_MAPPINGS — and **one**
               ActionRegistry row per action (ENT-SEC-006), thirty in total, each named
               `PERM_<PAGE_CODE>_<ACTION>`. They include the two custom actions
               `PERM_FIN_JOURNAL_ENTRIES_REVERSE` and `PERM_FIN_PERIODS_CLOSE_APPROVE`, and the
               three v2 actions VIEW, CREATE and UPDATE of FIN_ACCOUNT_MAPPINGS, which are also
               granted to `SYS_ADMIN`; no FIN page has a DELETE action. Onboarding is idempotent
               at the deployment boundary: it runs once, not per request
Test data    : the thirteen page codes and the thirty actions are the backend plan's own
               bootstrap rows, not invented
<!-- TC:TC-FIN-044:END -->

<!-- TC:TC-FIN-045:START traces=AC-FIN-045,REQ-FIN-045 -->
### TC-FIN-045 — FIN registers its fifteen lookup types into MDL
Derived from : AC-FIN-045  (REQ-FIN-045) — MODIFIED in v2
Exercises    : — deployment-time onboarding and the v2 lookup migration (db-script v2 BLOCK 8);
               no `API-*` publishes either (ADR-FIN-009). The registration is made into MDL,
               which this module does not own
Rule / code  : — (registration, no rule)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: MDL is gated and reachable, and FIN v2 is deployed — AC-FIN-045's own Given
Steps        : 1. run FIN's onboarding and its v2 migration
               2. read MDL's lookup types filtered by owner module `FIN`, with their values
Expected     : MDL holds exactly **15** LookupType rows owned by FIN — the thirteen of v1
               (ACCOUNT_TYPE, DEBIT_CREDIT, PERIOD_STATE, FISCAL_YEAR_STATUS, JOURNAL_TYPE,
               JOURNAL_STATUS, ACCOUNTING_EVENT_TYPE, PAYMENT_METHOD, ACCOUNT_DERIVATION_TYPE,
               AMOUNT_SOURCE_TYPE, DISTRIBUTION_TYPE, RECURRING_SCHEDULE_TYPE, RECURRING_FREQUENCY)
               plus FIN_EVENT_BUSINESS_FIELD and FIN_DIMENSION_VALUE_SOURCE. Within them:
               ACCOUNT_DERIVATION_TYPE holds exactly **2** active values, CONSTANT and MAPPING;
               FIN_EVENT_BUSINESS_FIELD holds **4** values (PAYMENT_METHOD, EXPENSE_TYPE_CODE,
               ORGANISATION_CODE, BRANCH_CODE); FIN_DIMENSION_VALUE_SOURCE holds **2** values
               (CONSTANT, BUSINESS_FIELD); PAYMENT_METHOD holds the value CHEQUE. FIN consumes no
               lookup it does not own. ACCOUNTING_EVENT_TYPE still seeds no value — host-defined
               — and that is the registered state, not an incomplete one. Running the migration
               a second time changes nothing: the seed is idempotent
Test data    : the fifteen keys and the value counts are AC-FIN-045's own and SRS A6's own list
<!-- TC:TC-FIN-045:END -->

<!-- TC:TC-FIN-046:START traces=AC-FIN-046,REQ-FIN-046,API-FIN-030,API-FIN-029,API-FIN-028,API-FIN-022 -->
### TC-FIN-046 — a statement line drills all the way down to its source event
Derived from : AC-FIN-046  (REQ-FIN-046)
Exercises    : API-FIN-030 → API-FIN-029 → API-FIN-028 → API-FIN-022, in that order
Rule / code  : — (read-only chain)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: at least one balance-sheet line whose balance comes from an event-sourced entry;
               a controller holding the VIEW permission of each of the four report and entry
               screens
Steps        : 1. GET the balance sheet and take one statement line
               2. GET the trial balance and find that line's account row
               3. GET the account ledger for that account
               4. GET the originating entry by the id the ledger row carries
Expected     : each step's output carries the key the next step needs, so the chain
               statement line → trial balance row → account ledger → originating entry →
               `eventReference` completes without a gap. For an entry that is not event-sourced
               the last hop reads `manual`, `recurring` or `allocation` instead of a reference —
               AC-FIN-046's own alternative
Test data    : none invented; the chain is AC-FIN-046's own
<!-- TC:TC-FIN-046:END -->

<!-- TC:TC-FIN-050:START traces=AC-FIN-027,REQ-FIN-027,API-FIN-018 -->
### TC-FIN-050 — a filter matching nothing is a successful empty page
Derived from : AC-FIN-027  (REQ-FIN-027) — its "exactly the matching entries" read at zero
Exercises    : API-FIN-018 POST /api/v1/fin/journal-entries/search
Rule / code  : — (read-only)
Scenario     : BOUNDARY · data class EDGE · language ALL
Preconditions: entries exist, but none in the combination of period and status the filter names;
               a caller holding `PERM_FIN_JOURNAL_ENTRIES_VIEW`
Steps        : 1. POST `/api/v1/fin/journal-entries/search` with that filter pair
Expected     : 200 · an **empty** `Page<JournalEntryResponse>`. An empty result is success, not a
               404 and not an error — the module's own search contract. "Exactly the matching
               entries" is honoured when the matching set is empty, and no entry of a neighbouring
               period or status leaks into the page
Test data    : none invented
<!-- TC:TC-FIN-050:END -->

<!-- TC:TC-FIN-051:START traces=AC-FIN-031,REQ-FIN-031,API-FIN-023 -->
### TC-FIN-051 — a period count other than twelve falls back to an even day split
Derived from : AC-FIN-031  (REQ-FIN-031) — the twelve-period limit REQ-FIN-031 states
Exercises    : API-FIN-023 POST /api/v1/fin/fiscal-years
Rule / code  : — (happy path, the documented fallback branch)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: a finance administrator holding `PERM_FIN_PERIODS_CREATE`; a year code not yet used
Steps        : 1. POST `/api/v1/fin/fiscal-years` with a `periodCount` **other than 12**, or with
                  twelve over a span that is not a whole calendar year
Expected     : 201 · that many periods, each `statusCode` = OPEN, produced by the even day split:
               the days of the span divided into contiguous blocks, the first
               (totalDays mod periodCount) blocks one day longer, so **the last period ends on the
               year's end date and no day belongs to two periods**. Their names are «الفترة N» and
               "Period N", not month names — the calendar-month naming of TC-FIN-031 applies only
               at exactly twelve over a whole year
Test data    : the two naming patterns are REQ-FIN-031's own strings; the count is a placeholder
               on the other side of the stated limit
<!-- TC:TC-FIN-051:END -->

<!-- TC:TC-FIN-052:START traces=AC-FIN-038,REQ-FIN-038,API-FIN-027 -->
### TC-FIN-052 — the same permission gates the year-end close
Derived from : AC-FIN-038  (REQ-FIN-038) — its "period-close-approval action", on the second
               endpoint that carries it
Exercises    : API-FIN-027 POST /api/v1/fin/fiscal-years/{id}/year-end-close
Rule / code  : RULE-FIN-015 → FIN-403-FORBIDDEN (403)
Scenario     : PERMISSION · data class ATTACK · language ALL
Preconditions: a role holding `PERM_FIN_JOURNAL_ENTRIES_CREATE` and not
               `PERM_FIN_PERIODS_CLOSE_APPROVE`; a fiscal year that is OPEN with every period
               HARD_CLOSE, so the call would otherwise succeed
Steps        : 1. POST `/api/v1/fin/fiscal-years/{id}/year-end-close` as that role's user
Expected     : 403 with code FIN-403-FORBIDDEN —
               ar: «لا تملك صلاحية المالية المطلوبة لهذه العملية» ·
               en: "You do not hold the Finance permission required for this operation".
               The denial happens **before the method body runs**, so neither the successor year
               nor the Retained Earnings account is resolved and no entry is posted; the fiscal
               year stays OPEN. Assert the 403 at the gate, never from inside the service
Test data    : the two permission codes are the registry's own
<!-- TC:TC-FIN-052:END -->
<!-- SUB:API-SCENARIOS:END -->
