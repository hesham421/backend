<!-- source: PHASE:TEST-PLAN-BE / SUB:API-SCENARIOS -->
<!-- context: TEST-PLAN-BE-HEADER.md — phase-level preamble -->
<!-- traces: AC-FIN-001, AC-FIN-004, AC-FIN-005, AC-FIN-007, AC-FIN-008, AC-FIN-010, AC-FIN-014, AC-FIN-022, AC-FIN-023, AC-FIN-024, AC-FIN-025, AC-FIN-027, AC-FIN-028, AC-FIN-031, AC-FIN-036, AC-FIN-037, AC-FIN-038, AC-FIN-039, AC-FIN-040, AC-FIN-041, AC-FIN-042, AC-FIN-043, AC-FIN-044, AC-FIN-045, AC-FIN-046, API-FIN-002, API-FIN-006, API-FIN-007, API-FIN-010, API-FIN-011, API-FIN-013, API-FIN-014, API-FIN-016, API-FIN-018, API-FIN-019, API-FIN-020, API-FIN-021, API-FIN-022, API-FIN-023, API-FIN-026, API-FIN-027, API-FIN-028, API-FIN-029, API-FIN-030, API-FIN-031, API-FIN-032, REQ-FIN-001, REQ-FIN-004, REQ-FIN-005, REQ-FIN-007, REQ-FIN-008, REQ-FIN-010, REQ-FIN-014, REQ-FIN-022, REQ-FIN-023, REQ-FIN-024, REQ-FIN-025, REQ-FIN-027, REQ-FIN-028, REQ-FIN-031, REQ-FIN-036, REQ-FIN-037, REQ-FIN-038, REQ-FIN-039, REQ-FIN-040, REQ-FIN-041, REQ-FIN-042, REQ-FIN-043, REQ-FIN-044, REQ-FIN-045, REQ-FIN-046 -->
<!-- SUB:API-SCENARIOS:START traces=REQ-FIN-001,REQ-FIN-004,REQ-FIN-005,REQ-FIN-007,REQ-FIN-008,REQ-FIN-010,REQ-FIN-014,REQ-FIN-022,REQ-FIN-023,REQ-FIN-024,REQ-FIN-025,REQ-FIN-027,REQ-FIN-028,REQ-FIN-031,REQ-FIN-036,REQ-FIN-037,REQ-FIN-038,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-044,REQ-FIN-045,REQ-FIN-046,AC-FIN-001,AC-FIN-004,AC-FIN-005,AC-FIN-007,AC-FIN-008,AC-FIN-010,AC-FIN-014,AC-FIN-022,AC-FIN-023,AC-FIN-024,AC-FIN-025,AC-FIN-027,AC-FIN-028,AC-FIN-031,AC-FIN-036,AC-FIN-037,AC-FIN-038,AC-FIN-039,AC-FIN-040,AC-FIN-041,AC-FIN-042,AC-FIN-043,AC-FIN-044,AC-FIN-045,AC-FIN-046 -->
### SUB — API-SCENARIOS

The ACs whose Then is an endpoint's own success shape: creations, runs, reports, paging and
empty results, and the two permission denials AC-FIN-038 states.

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
Rule / code  : — (happy path; RULE-FIN-003 passes, no percentage line being present)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an EventTypeRule exists; a caller holding `PERM_FIN_RULES_UPDATE`; MDL holds the
               four lookup types the line's codes belong to
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with an account-derivation spec
                  (`accountDerivationTypeCode` + `accountDerivationValue`), an amount-source spec
                  (`amountSourceTypeCode` + `amountSourceValue`) and a `directionCode`
Expected     : 201 · `RuleLineResponse` in which the account derivation, the amount source and the
               direction are **three separate stored references** (DBF-FIN-101 … DBF-FIN-105), not
               one fused field — the deliberate §6.2 separation REQ-FIN-008 states
Test data    : `CONSTANT` / `DIRECT` (`ACCOUNT_DERIVATION_TYPE`), `FIELD` (`AMOUNT_SOURCE_TYPE`),
               `FIXED` (`DISTRIBUTION_TYPE`), `DEBIT` (`DEBIT_CREDIT`) — all seeded values (SRS
               A6); the derivation and source values are placeholders
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
Expected     : 201 · a DRAFT is built whose lines follow the rule — account derived per
               `accountDerivationTypeCode`, amount per `amountSourceTypeCode`, one
               `directionCode` per line — and the same call proceeds straight through validation
               to POSTED (REQ-FIN-017). `journalTypeCode` is EVENT_GENERATED and `eventReference`
               carries the event's own reference. No account number ever appears in the event
               payload: the rule derives it
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
Exercises    : — deployment-time onboarding; FIN publishes no endpoint for it, so no `API-FIN-*`
               is cited (ADR-FIN-009)
Rule / code  : — (registration, no rule)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: SEC v1 is gated and reachable — AC-FIN-044's own Given
Steps        : 1. run FIN's onboarding once against a database that has none of its registry rows
               2. read back SEC's module, screen and action registries
Expected     : SEC holds **one** ModuleRegistry row for FIN (ENT-SEC-004), **one** ScreenRegistry
               row per FIN screen (ENT-SEC-005) — the twelve page codes FIN_ACCOUNTS …
               FIN_DIMENSION_REPORTS — and **one** ActionRegistry row per action (ENT-SEC-006),
               twenty-seven in total, each named `PERM_<PAGE_CODE>_<ACTION>` and including the two
               custom actions `PERM_FIN_JOURNAL_ENTRIES_REVERSE` and
               `PERM_FIN_PERIODS_CLOSE_APPROVE`. Onboarding is idempotent at the deployment
               boundary: it runs once, not per request
Test data    : the twelve page codes and the action count are the registry's own, not invented
<!-- TC:TC-FIN-044:END -->

<!-- TC:TC-FIN-045:START traces=AC-FIN-045,REQ-FIN-045 -->
### TC-FIN-045 — FIN registers its thirteen lookup types into MDL
Derived from : AC-FIN-045  (REQ-FIN-045)
Exercises    : — deployment-time onboarding; no `API-FIN-*` publishes it (ADR-FIN-009). The
               registration is made through MDL's own endpoint, which this module does not own
Rule / code  : — (registration, no rule)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: MDL v1 is gated and reachable — AC-FIN-045's own Given
Steps        : 1. run FIN's onboarding once
               2. read MDL's lookup types filtered by owner module `FIN`
Expected     : MDL holds exactly **13** LookupType rows owned by FIN — ACCOUNT_TYPE,
               DEBIT_CREDIT, PERIOD_STATE, FISCAL_YEAR_STATUS, JOURNAL_TYPE, JOURNAL_STATUS,
               ACCOUNTING_EVENT_TYPE, PAYMENT_METHOD, ACCOUNT_DERIVATION_TYPE, AMOUNT_SOURCE_TYPE,
               DISTRIBUTION_TYPE, RECURRING_SCHEDULE_TYPE, RECURRING_FREQUENCY. FIN consumes no
               lookup it does not own. Two of the thirteen seed no value at all
               (ACCOUNTING_EVENT_TYPE, PAYMENT_METHOD — host-specific), and that is the registered
               state, not an incomplete one
Test data    : the thirteen keys are SRS A6's own list
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
