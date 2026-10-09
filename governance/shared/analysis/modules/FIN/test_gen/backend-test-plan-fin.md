# BACKEND TEST PLAN — الحسابات العامة / Finance (General Ledger) (FIN)
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v1   Profile : erp   Track : backend   Plan : test
Scope  : **module** — modules FIN (`gov.py run-standalone test-gen --module FIN`)
Sources: `_state/current-srs.md` (v1) · `_state/current-backend-execution-plan.md` (v1) ·
         `_state/current-frontend-execution-plan.md` (v1) · `_state/current-registry-srs.md` (v1) ·
         `_state/current-registry-db.md` (v1) · `_state/current-registry-exec-fe.md` (v1)
Framework : **agnostic** (`profile.stack.testing`). Every block below is the framework-agnostic
            TC form; no tool, annotation, fixture or file layout is named anywhere in this plan.
            The consumer repo chooses its framework and turns each TC into a test.
REDUCED   : no — the backend-execution-plan is present, so every step binds to a real `API-*`.
TC ids    : TC-FIN-001 … TC-FIN-052 — one continuous module sequence **shared** with
            `frontend-test-plan-fin.md`, which continues at TC-FIN-053. No id is reused and no
            id is reassigned to a different subject than the one it already carried.
Open ADRs : 1 raised by this run — ADR-FIN-009 (ACCEPTED, non-breaking). Applied and not
            re-derived: ADR-FIN-001 … ADR-FIN-008.
══════════════════════════════════════════════════════════════════

**Id order is AC order, not document order.** `TC-FIN-<seq>` maps 1:1 onto `AC-FIN-<seq>` for
all forty-six acceptance criteria; TC-FIN-047 … TC-FIN-052 are the six additional cases §3 rule 3
(a stated numeric limit) and §3's permission rule allow, each declared against the AC it extends.
The two SUBs group by scenario kind, so the ids are not monotonic inside a SUB — that is the
price of never renumbering, and the traceability index below is the address.

**Where the TCs come from, and where they deliberately stop.** Every TC below is derived from an
`AC-FIN-*`. FIN's artifacts also record behaviour that no `REQ-*`, `AC-*` or `RULE-*` states and
that was delivered on a recorded human decision or as an as-built narrowing:

- the `FIN-409-NOT-ACTIVE` run gate on API-FIN-014 / API-FIN-017 (backend-execution-plan, Error
  Catalog: "no RULE — recorded human decision, 2026-09-12");
- the four deactivate endpoints API-FIN-034 … API-FIN-037 as scenarios in their own right;
- the keying-id 404s on API-FIN-029 / API-FIN-030 / API-FIN-031 (`FIN-404-PERIOD`, `FIN-404-YEAR`);
- the dormant-year lineless CLOSING/OPENING entry (API-FIN-027, "reviewed and deliberately KEPT");
- the fiscal-period search API-FIN-033, which no `AC-*` asks for.

None of them gets a TC of its own here: fabricating one would invent an acceptance criterion this
engine does not own. They appear only where an AC's own Given needs them as setup — API-FIN-035
is how AC-FIN-021's inactive dimension value is reached at all, and API-FIN-033 is how a period id
is discovered. This boundary is recorded in **ADR-FIN-009**, not left unsaid; it is a
requirements-side gap if anyone wants those paths covered, not a test-generation one.

<!-- PHASE:TEST-PLAN-BE:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-003,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-010,REQ-FIN-011,REQ-FIN-012,REQ-FIN-013,REQ-FIN-014,REQ-FIN-015,REQ-FIN-016,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-022,REQ-FIN-023,REQ-FIN-024,REQ-FIN-025,REQ-FIN-026,REQ-FIN-027,REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,REQ-FIN-031,REQ-FIN-032,REQ-FIN-033,REQ-FIN-034,REQ-FIN-035,REQ-FIN-036,REQ-FIN-037,REQ-FIN-038,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-044,REQ-FIN-045,REQ-FIN-046,AC-FIN-001,AC-FIN-002,AC-FIN-003,AC-FIN-004,AC-FIN-005,AC-FIN-006,AC-FIN-007,AC-FIN-008,AC-FIN-009,AC-FIN-010,AC-FIN-011,AC-FIN-012,AC-FIN-013,AC-FIN-014,AC-FIN-015,AC-FIN-016,AC-FIN-017,AC-FIN-018,AC-FIN-019,AC-FIN-020,AC-FIN-021,AC-FIN-022,AC-FIN-023,AC-FIN-024,AC-FIN-025,AC-FIN-026,AC-FIN-027,AC-FIN-028,AC-FIN-029,AC-FIN-030,AC-FIN-031,AC-FIN-032,AC-FIN-033,AC-FIN-034,AC-FIN-035,AC-FIN-036,AC-FIN-037,AC-FIN-038,AC-FIN-039,AC-FIN-040,AC-FIN-041,AC-FIN-042,AC-FIN-043,AC-FIN-044,AC-FIN-045,AC-FIN-046 -->
## PHASE — TEST-PLAN-BE

TC count is 52, far above the split threshold of 12, so this phase is split into the two groups
the engine names: rule-driven scenarios (violations and state transitions) and endpoint-driven
scenarios (happy paths, permission, paging and empty results). Every TC sits inside one of them.

<!-- SUB:RULE-SCENARIOS:START traces=REQ-FIN-002,REQ-FIN-003,REQ-FIN-006,REQ-FIN-009,REQ-FIN-011,REQ-FIN-012,REQ-FIN-013,REQ-FIN-014,REQ-FIN-015,REQ-FIN-016,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-026,REQ-FIN-029,REQ-FIN-030,REQ-FIN-032,REQ-FIN-033,REQ-FIN-034,REQ-FIN-035,AC-FIN-002,AC-FIN-003,AC-FIN-006,AC-FIN-009,AC-FIN-011,AC-FIN-012,AC-FIN-013,AC-FIN-014,AC-FIN-015,AC-FIN-016,AC-FIN-017,AC-FIN-018,AC-FIN-019,AC-FIN-020,AC-FIN-021,AC-FIN-026,AC-FIN-029,AC-FIN-030,AC-FIN-032,AC-FIN-033,AC-FIN-034,AC-FIN-035 -->
### SUB — RULE-SCENARIOS

The ACs whose Then names a `RULE-*` violation or a state transition. Each one asserts the catalog
code and both message strings, or the stored state the rule governs.

<!-- TC:TC-FIN-002:START traces=AC-FIN-002,REQ-FIN-002,API-FIN-003 -->
### TC-FIN-002 — an account with children cannot be marked as accepting direct posting
Derived from : AC-FIN-002  (REQ-FIN-002)
Exercises    : API-FIN-003 PUT /api/v1/fin/accounts/{id}
Rule / code  : RULE-FIN-001 → FIN-409-HAS-CHILDREN (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a finance administrator holding `PERM_FIN_ACCOUNTS_UPDATE` (and the
               `PERM_FIN_ACCOUNTS_VIEW` gateway); an account `<parent>` (ENT-FIN-001) that has at
               least one child row whose `parentAccountId` (DBF-FIN-007) points at it
Steps        : 1. PUT `/api/v1/fin/accounts/{parent id}` with `isLeafFl` = true
Expected     : 409 · `LocalizedException → {code, messageAr, messageEn}` with code
               FIN-409-HAS-CHILDREN —
               ar: «لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا» ·
               en: "An account with sub-accounts cannot accept direct posting".
               The stored `isLeafFl` (DBF-FIN-008) is unchanged; a subsequent API-FIN-001 search
               returns the parent still non-leaf
Test data    : `<parent>` and `<child>` are placeholders — AC-FIN-002 names no code; the child
               relation is the only fact the AC states
<!-- TC:TC-FIN-002:END -->

<!-- TC:TC-FIN-003:START traces=AC-FIN-003,REQ-FIN-003,API-FIN-004 -->
### TC-FIN-003 — deactivating an account blocks every later posting to it
Derived from : AC-FIN-003  (REQ-FIN-003)
Exercises    : API-FIN-004 PUT /api/v1/fin/accounts/{id}/deactivate, then
               API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409), on the second half
Scenario     : STATE · data class VALID · language ALL
Preconditions: an active leaf account `<A>`; an Open period whose `[startDate, endDate]` covers
               the document date; a caller holding `PERM_FIN_ACCOUNTS_UPDATE` and
               `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. PUT `/api/v1/fin/accounts/{A}/deactivate`
               2. POST `/api/v1/fin/journal-entries` with a balanced two-line entry, one line
                  targeting `<A>`
Expected     : step 1 → 200 · `AccountResponse` with `isActiveFl` = false (DBF-FIN-009).
               step 2 → 409 with code FIN-409-NOT-POSTABLE-ACCOUNT —
               ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting".
               Nothing is written: REQ-FIN-015's one-transaction rule means no DRAFT survives
Test data    : `<A>` is a placeholder; the AC names no code
<!-- TC:TC-FIN-003:END -->

<!-- TC:TC-FIN-006:START traces=AC-FIN-006,REQ-FIN-006,API-FIN-007 -->
### TC-FIN-006 — a duplicate code under the same dimension is refused
Derived from : AC-FIN-006  (REQ-FIN-006)
Exercises    : API-FIN-007 POST /api/v1/fin/dimensions/{id}/values
Rule / code  : RULE-FIN-002 → FIN-409-DIMVALUE-DUP (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a caller holding `PERM_FIN_DIMENSIONS_CREATE`; a dimension `PROJECT`
               (ENT-FIN-002) already holding a value with code `NORTH`
Steps        : 1. POST `/api/v1/fin/dimensions/{PROJECT id}/values` with code `NORTH`, both
                  names and a sortOrder
Expected     : 409 with code FIN-409-DIMVALUE-DUP —
               ar: «هذا الرمز مستخدم بالفعل ضمن هذا البُعد» ·
               en: "This code is already used within this dimension".
               No second row is stored: API-FIN-008 filtered by that dimension returns exactly
               one `NORTH` value
Test data    : `NORTH` — AC-FIN-006's own value; `PROJECT` — ENT-FIN-002's own example code
<!-- TC:TC-FIN-006:END -->

<!-- TC:TC-FIN-009:START traces=AC-FIN-009,REQ-FIN-009,API-FIN-011 -->
### TC-FIN-009 — a percentage line set with no remainder line is refused
Derived from : AC-FIN-009  (REQ-FIN-009)
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-003 → FIN-409-REMAINDER-COUNT (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a caller holding `PERM_FIN_RULES_UPDATE`; an EventTypeRule (ENT-FIN-009) whose
               existing line set already carries one `PERCENTAGE`-distributed line and no line
               marked `isRemainderFl` (DBF-FIN-107)
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with a second line whose
                  `distributionTypeCode` is `PERCENTAGE` and whose `isRemainderFl` is false
Expected     : 409 with code FIN-409-REMAINDER-COUNT —
               ar: «يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي» ·
               en: "Exactly one remainder line is required when any percentage distribution is
               present".
               The rule's stored line set is unchanged, and stays unchanged until exactly one
               line is marked remainder — the AC's own wording
Test data    : `PERCENTAGE` — a seeded `DISTRIBUTION_TYPE` value (SRS A6); the two-line count is
               AC-FIN-009's own
<!-- TC:TC-FIN-009:END -->

<!-- TC:TC-FIN-011:START traces=AC-FIN-011,REQ-FIN-011,API-FIN-020 -->
### TC-FIN-011 — a repeated event reference never produces a second entry
Derived from : AC-FIN-011  (REQ-FIN-011)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-004 → FIN-409-DUPLICATE-EVENT (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a POSTED JournalEntry (ENT-FIN-004) whose `eventReference` (DBF-FIN-041) is
               `EVT-1001`; the event type still carries its active rule; the caller is the Event
               consumer's own service principal holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with a canonical event whose
                  reference is `EVT-1001`
Expected     : 409 with code FIN-409-DUPLICATE-EVENT —
               ar: «تم بالفعل ترحيل قيد لهذا المرجع» ·
               en: "An entry for this event reference has already been posted".
               API-FIN-018 filtered on that reference still returns exactly one entry; no second
               `docNo` is consumed from the fiscal-year series
Test data    : `EVT-1001` — AC-FIN-011's own value
<!-- TC:TC-FIN-011:END -->

<!-- TC:TC-FIN-012:START traces=AC-FIN-012,REQ-FIN-012,API-FIN-020 -->
### TC-FIN-012 — the remainder line absorbs the rounding difference exactly
Derived from : AC-FIN-012  (REQ-FIN-012)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-010 → (success path; no code is raised when the residue is positive)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active EventTypeRule whose lines distribute the event amount as 33% + 33% +
               remainder, exactly one line carrying `isRemainderFl` = true (DBF-FIN-107) and
               agreeing with its own `REMAINDER` type code; an Open period covering the document
               date; every derived account leaf and active
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with an event whose amount is
                  100.00
Expected     : 201 · `JournalEntryResponse` with `statusCode` = POSTED. The two percentage lines
               total 66.00, each rounded to the smallest currency unit, and the remainder line is
               exactly 34.00 (DBF-FIN-055, `NUMERIC(18,4)`), so total debits equal total credits
               to the smallest currency unit. The remainder amount is a difference, never a
               percentage — RULE-FIN-010's own statement
Test data    : 100.00 · 33% · 33% · 66.00 · 34.00 — all AC-FIN-012's own values
<!-- TC:TC-FIN-012:END -->

<!-- TC:TC-FIN-013:START traces=AC-FIN-013,REQ-FIN-013,API-FIN-020 -->
### TC-FIN-013 — an event whose type has no active rule is refused
Derived from : AC-FIN-013  (REQ-FIN-013)
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-005 → FIN-404-NO-ACTIVE-RULE (404)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an `ACCOUNTING_EVENT_TYPE` value registered in MDL for which FIN holds no active
               EventTypeRule row — reachable either because none was ever created or because the
               only one was retired through API-FIN-034
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with an event of that type
Expected     : 404 with code FIN-404-NO-ACTIVE-RULE —
               ar: «لا توجد قاعدة نشطة لهذا النوع من الأحداث» ·
               en: "No active rule exists for this event type".
               No entry is built and none is posted; the failure is recorded for operator
               follow-up, which is AC-FIN-013's own Then
Test data    : the event type is a placeholder — `ACCOUNTING_EVENT_TYPE` seeds no value (SRS A6:
               host-specific, added as data)
<!-- TC:TC-FIN-013:END -->

<!-- TC:TC-FIN-015:START traces=AC-FIN-015,REQ-FIN-015,API-FIN-019 -->
### TC-FIN-015 — two failing validations are both returned and nothing is posted
Derived from : AC-FIN-015  (REQ-FIN-015)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-006 and RULE-FIN-007 together → FIN-409-UNBALANCED,
               FIN-409-NOT-POSTABLE-ACCOUNT
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an accountant holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`; an Open period; one
               non-leaf (rollup) account among the submitted lines; the submitted `fiscalYearId`,
               `periodId` and `docDate` are coherent, so RULE-FIN-017 does not fail first
Steps        : 1. POST `/api/v1/fin/journal-entries` with an entry that is both unbalanced and
                  targets the rollup account on one line
Expected     : the response carries **both** failures, not the first one only —
               FIN-409-UNBALANCED (ar: «القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن» ·
               en: "The entry is unbalanced — total debits do not equal total credits") and
               FIN-409-NOT-POSTABLE-ACCOUNT (ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting").
               Nothing is written at all: the whole build-through-post sequence is one
               transaction, so no DRAFT row and no `docNo` survive the failed attempt
Test data    : the two failing conditions are AC-FIN-015's own count; account ids are placeholders
<!-- TC:TC-FIN-015:END -->

<!-- TC:TC-FIN-016:START traces=AC-FIN-016,REQ-FIN-016,API-FIN-022 -->
### TC-FIN-016 — a POSTED entry cannot be deleted; only a reversal changes its effect
Derived from : AC-FIN-016  (REQ-FIN-016)
Exercises    : API-FIN-022 GET /api/v1/fin/journal-entries/{id} (the surface assertion) ·
               API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse (the only mutation)
Rule / code  : RULE-FIN-016 → no catalog code: the rule is enforced by omission, there being no
               UPDATE and no DELETE mapping on a POSTED row anywhere in the module
Scenario     : STATE · data class EDGE · language ALL
Preconditions: a POSTED JournalEntry; a caller holding `PERM_FIN_JOURNAL_ENTRIES_VIEW` and
               `PERM_FIN_JOURNAL_ENTRIES_REVERSE`
Steps        : 1. enumerate the mappings published on `/api/v1/fin/journal-entries` and on
                  `/api/v1/fin/journal-entries/{id}`
               2. issue a DELETE against `/api/v1/fin/journal-entries/{id}`
               3. GET `/api/v1/fin/journal-entries/{id}`
Expected     : step 1 → the published set is search, create, from-event, reverse and read, and no
                  other; FIN publishes no DELETE endpoint on any screen.
               step 2 → refused by the framework's own routing before any FIN code runs, so **no
                  FIN catalog code is emitted** and none is asserted here.
               step 3 → the entry is byte-identical to before, still `statusCode` = POSTED.
               The only path that changes its effect is API-FIN-021 (REQ-FIN-028), whose own
               effect is a new linked entry, never a mutation of this row
Test data    : none beyond the existing POSTED entry
<!-- TC:TC-FIN-016:END -->

<!-- TC:TC-FIN-017:START traces=AC-FIN-017,REQ-FIN-017,API-FIN-019 -->
### TC-FIN-017 — a DRAFT that passes every check posts directly and locks
Derived from : AC-FIN-017  (REQ-FIN-017)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-016 → no code (the lock is enforced by omission) · RULE-FIN-006,
               RULE-FIN-007, RULE-FIN-008, RULE-FIN-009 all pass
Scenario     : STATE · data class VALID · language ALL
Preconditions: an Open period belonging to the submitted fiscal year and covering the document
               date; two or more balanced lines on leaf, active accounts; every cited dimension
               value active and belonging to its stated dimension
Steps        : 1. POST `/api/v1/fin/journal-entries` with that entry
               2. GET the created entry through API-FIN-022
Expected     : 201 · `statusCode` = POSTED and `postedAt` (DBF-FIN-046) set, with **no separate
               human approval step** between validation and posting — REQ-FIN-017's whole point.
               A `docNo` of the form `JV-{fiscalYearCode}-{NNNNNN}` is present in the response and
               was never present in the request. Step 2 returns the same POSTED row: the entry is
               fully locked, no edit and no delete mapping exists for it
Test data    : `JV-2026-000123` is the format example the SRS itself gives for fiscal year `2026`
<!-- TC:TC-FIN-017:END -->

<!-- TC:TC-FIN-018:START traces=AC-FIN-018,REQ-FIN-018,API-FIN-019 -->
### TC-FIN-018 — an entry out of balance by the smallest currency unit is refused
Derived from : AC-FIN-018  (REQ-FIN-018)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-006 → FIN-409-UNBALANCED (409)
Scenario     : VIOLATION · data class BOUNDARY · language ALL
Preconditions: an Open period; every line on a leaf, active account, so the imbalance is the only
               failing check
Steps        : 1. POST `/api/v1/fin/journal-entries` with debit lines totalling 100.00 and credit
                  lines totalling 99.99
Expected     : 409 with code FIN-409-UNBALANCED —
               ar: «القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن» ·
               en: "The entry is unbalanced — total debits do not equal total credits".
               The imbalance amount is shown, which is AC-FIN-018's own Then. Nothing is posted
               and nothing is stored
Test data    : 100.00 and 99.99 — AC-FIN-018's own values, one hundredth apart, which is the
               smallest currency unit RULE-FIN-006 measures to
<!-- TC:TC-FIN-018:END -->

<!-- TC:TC-FIN-019:START traces=AC-FIN-019,REQ-FIN-019,API-FIN-019 -->
### TC-FIN-019 — a line targeting a rollup account is refused, naming the line
Derived from : AC-FIN-019  (REQ-FIN-019)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an Open period; a balanced line set in which exactly one line targets a non-leaf
               (rollup) account — `isLeafFl` false (DBF-FIN-008) — every other line being leaf and
               active
Steps        : 1. POST `/api/v1/fin/journal-entries` with that entry
Expected     : 409 with code FIN-409-NOT-POSTABLE-ACCOUNT —
               ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting".
               The offending line is named, which is AC-FIN-019's own Then. Nothing is posted
Test data    : account ids are placeholders; the rollup relation is the fact the AC states
<!-- TC:TC-FIN-019:END -->

<!-- TC:TC-FIN-020:START traces=AC-FIN-020,REQ-FIN-020,API-FIN-019 -->
### TC-FIN-020 — the period gate is read at post time, not at build time
Derived from : AC-FIN-020  (REQ-FIN-020)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries, with API-FIN-026 in between
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN (409)
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: a period that is OPEN when the entry is composed; an approver holding
               `PERM_FIN_PERIODS_CLOSE_APPROVE`, so the period can be moved to HARD_CLOSE between
               composition and submission (OPEN → SOFT_CLOSE → HARD_CLOSE, SRS A7)
Steps        : 1. compose a balanced entry against that period while it is still Open
               2. soft-close then hard-close the period (API-FIN-025, API-FIN-026)
               3. POST `/api/v1/fin/journal-entries` with the entry composed in step 1
Expected     : 409 with code FIN-409-PERIOD-NOT-OPEN —
               ar: «الفترة المستهدفة غير مفتوحة» ·
               en: "The target period is not open".
               The rejection cites the period's **current** state, which is AC-FIN-020's own Then,
               and holds even though the period was Open when the entry was built
Test data    : none invented; the state sequence is the SRS A7 lifecycle
<!-- TC:TC-FIN-020:END -->

<!-- TC:TC-FIN-021:START traces=AC-FIN-021,REQ-FIN-021,API-FIN-019 -->
### TC-FIN-021 — a line citing an inactive dimension value is refused
Derived from : AC-FIN-021  (REQ-FIN-021)
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-009 → FIN-409-INVALID-DIMENSION (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an Open period and a balanced, leaf-account line set; one DimensionValue
               (ENT-FIN-003) whose `isActiveFl` (DBF-FIN-029) is false. That state is reachable
               only through API-FIN-035, the sole endpoint that clears the flag — the setup
               dependency ADR-FIN-009 records
Steps        : 1. PUT `/api/v1/fin/dimensions/values/{id}/deactivate`
               2. POST `/api/v1/fin/journal-entries` with a line citing that dimension value
Expected     : step 1 → 200 with `isActiveFl` = false.
               step 2 → 409 with code FIN-409-INVALID-DIMENSION —
               ar: «قيمة البُعد غير صالحة» · en: "The dimension value is invalid".
               The offending line and dimension are named, which is AC-FIN-021's own Then
Test data    : a value under `PROJECT`; ids are placeholders
<!-- TC:TC-FIN-021:END -->

<!-- TC:TC-FIN-026:START traces=AC-FIN-026,REQ-FIN-026,API-FIN-017 -->
### TC-FIN-026 — an allocation run distributes the source balance to the last unit
Derived from : AC-FIN-026  (REQ-FIN-026)
Exercises    : API-FIN-017 POST /api/v1/fin/allocation-rules/{id}/run
Rule / code  : RULE-FIN-010 (reused for allocations) → success path, no code raised ·
               RULE-FIN-003 governs the target set
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active AllocationRule (ENT-FIN-013) with two `PERCENTAGE` targets and one
               `REMAINDER` target, exactly one target carrying `isRemainderFl` = true
               (DBF-FIN-146) in agreement with its own `distributionTypeCode`; the source
               account's current balance is 1,000.00; an Open period; an accountant holding
               `PERM_FIN_ALLOCATION_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/allocation-rules/{id}/run`
Expected     : 201 · one `JournalEntryResponse` with `journalTypeCode` = ALLOCATION and
               `statusCode` = POSTED, whose target lines sum **exactly** to 1,000.00, the
               remainder target absorbing the rounding difference. The entry balances, so
               RULE-FIN-006 does not fire
Test data    : 1,000.00 and the two-percentage-plus-one-remainder shape — AC-FIN-026's own
<!-- TC:TC-FIN-026:END -->

<!-- TC:TC-FIN-029:START traces=AC-FIN-029,REQ-FIN-029,API-FIN-021 -->
### TC-FIN-029 — a reversal of an entry in a closed period posts into the current open period
Derived from : AC-FIN-029  (REQ-FIN-029)
Exercises    : API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse
Rule / code  : RULE-FIN-012 → success path, no distinct code (the period substitution is the
               rule's effect, not a refusal)
Scenario     : STATE · data class EDGE · language ALL
Preconditions: a POSTED entry whose own period is now HARD_CLOSE; a different period that is
               currently OPEN; a caller holding `PERM_FIN_JOURNAL_ENTRIES_REVERSE`
Steps        : 1. POST `/api/v1/fin/journal-entries/{id}/reverse`
Expected     : 201 · the reversal entry's `periodId` (DBF-FIN-038) is the **current open**
               period, not the original's closed one — AC-FIN-029's own Then. The mirrored lines
               carry the same amounts with opposite `directionCode`, and both rows are linked
               through `originalEntryId` / `reversalEntryId`. FIN-409-PERIOD-NOT-OPEN is **not**
               raised, because the reversal does not target the closed period
Test data    : none invented
<!-- TC:TC-FIN-029:END -->

<!-- TC:TC-FIN-030:START traces=AC-FIN-030,REQ-FIN-030,API-FIN-021 -->
### TC-FIN-030 — an entry that already carries a reversal cannot be reversed again
Derived from : AC-FIN-030  (REQ-FIN-030)
Exercises    : API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse
Rule / code  : RULE-FIN-013 → FIN-409-ALREADY-REVERSED (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an entry reversed once already. Per SRS A7 it stays POSTED — classic reversal
               writes no VOID anywhere — and carries a `reversalEntryId` (DBF-FIN-043) link to
               its mirror
Steps        : 1. POST `/api/v1/fin/journal-entries/{id}/reverse` on the same original a second
                  time
Expected     : 409 with code FIN-409-ALREADY-REVERSED —
               ar: «تم عكس هذا القيد بالفعل ولا يمكن عكسه مرة أخرى» ·
               en: "This entry has already been reversed and cannot be reversed again".
               No second mirror is posted, so the pair's net ledger effect stays zero rather than
               becoming −(original)
Test data    : none invented
<!-- TC:TC-FIN-030:END -->

<!-- TC:TC-FIN-032:START traces=AC-FIN-032,REQ-FIN-032,API-FIN-024 -->
### TC-FIN-032 — a soft-closed period reopens
Derived from : AC-FIN-032  (REQ-FIN-032)
Exercises    : API-FIN-024 PATCH /api/v1/fin/fiscal-periods/{id}/open
Rule / code  : RULE-FIN-014 → not triggered here (the period is not Hard Closed)
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period whose `statusCode` (DBF-FIN-082) is SOFT_CLOSE — the one state §10.2
               calls re-openable; an administrator holding `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/open`
Expected     : 200 · `FiscalPeriodResponse` with `statusCode` = OPEN, the SOFT_CLOSE → OPEN
               transition of SRS A7. Normal posting into it succeeds afterwards, so RULE-FIN-008
               no longer refuses it
Test data    : the `PERIOD_STATE` values OPEN and SOFT_CLOSE — seeded lookup values (SRS A6)
<!-- TC:TC-FIN-032:END -->

<!-- TC:TC-FIN-033:START traces=AC-FIN-033,REQ-FIN-033,API-FIN-025 -->
### TC-FIN-033 — soft-closing a period blocks normal posting into it
Derived from : AC-FIN-033  (REQ-FIN-033)
Exercises    : API-FIN-025 PATCH /api/v1/fin/fiscal-periods/{id}/soft-close
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN (409), on the posting attempt that follows
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period currently OPEN; an administrator holding `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/soft-close`
               2. POST a balanced manual entry against that period (API-FIN-019)
Expected     : step 1 → 200 with `statusCode` = SOFT_CLOSE (OPEN → SOFT_CLOSE, SRS A7).
               step 2 → 409 with code FIN-409-PERIOD-NOT-OPEN —
               ar: «الفترة المستهدفة غير مفتوحة» · en: "The target period is not open" —
               which is AC-FIN-033's own "REQ-FIN-020 rejects normal postings thereafter"
Test data    : none invented
<!-- TC:TC-FIN-033:END -->

<!-- TC:TC-FIN-034:START traces=AC-FIN-034,REQ-FIN-034,API-FIN-026 -->
### TC-FIN-034 — hard-closing a period is permanent
Derived from : AC-FIN-034  (REQ-FIN-034)
Exercises    : API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close
Rule / code  : RULE-FIN-015 (the distinct permission gates the call) · RULE-FIN-014 (the
               terminal state it creates)
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period whose `statusCode` is SOFT_CLOSE; an authorized approver holding
               `PERM_FIN_PERIODS_CLOSE_APPROVE` together with the `PERM_FIN_PERIODS_VIEW` gateway
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/hard-close`
Expected     : 200 · `statusCode` = HARD_CLOSE, with `closedBy` (DBF-FIN-083) and `closedAt`
               (DBF-FIN-084) set. The state is terminal: TC-FIN-035 shows the reopen refusal, and
               posting into it is refused by RULE-FIN-008
Test data    : none invented
<!-- TC:TC-FIN-034:END -->

<!-- TC:TC-FIN-035:START traces=AC-FIN-035,REQ-FIN-035,API-FIN-024 -->
### TC-FIN-035 — a hard-closed period never reopens
Derived from : AC-FIN-035  (REQ-FIN-035)
Exercises    : API-FIN-024 PATCH /api/v1/fin/fiscal-periods/{id}/open
Rule / code  : RULE-FIN-014 → FIN-409-NOT-REOPENABLE (409)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a period whose `statusCode` is HARD_CLOSE; an administrator holding
               `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. PATCH `/api/v1/fin/fiscal-periods/{id}/open`
Expected     : 409 with code FIN-409-NOT-REOPENABLE —
               ar: «الفترة مغلقة إغلاقًا صارمًا ولا يمكن إعادة فتحها» ·
               en: "The period is hard-closed and cannot be reopened".
               The stored `statusCode` is unchanged
Test data    : none invented
<!-- TC:TC-FIN-035:END -->

<!-- TC:TC-FIN-047:START traces=AC-FIN-009,REQ-FIN-009,API-FIN-011 -->
### TC-FIN-047 — two remainder lines are refused as surely as none
Derived from : AC-FIN-009  (REQ-FIN-009) — the "exactly one" limit its RULE states
Exercises    : API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-003 → FIN-409-REMAINDER-COUNT (409)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an EventTypeRule whose line set already carries one `PERCENTAGE` line and one line
               marked `isRemainderFl` = true; a caller holding `PERM_FIN_RULES_UPDATE`
Steps        : 1. POST `/api/v1/fin/event-rules/{rule id}/lines` with a **second** line marked
                  `isRemainderFl` = true
Expected     : 409 with code FIN-409-REMAINDER-COUNT —
               ar: «يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي» ·
               en: "Exactly one remainder line is required when any percentage distribution is
               present".
               This is the upper side of the same numeric limit TC-FIN-009 tests from below:
               RULE-FIN-003 requires exactly one, so zero and two fail alike. Which line is the
               remainder decides every other line's amount under RULE-FIN-010, so an ambiguous set
               must never be stored
Test data    : the line counts come from RULE-FIN-003's own "exactly one"
<!-- TC:TC-FIN-047:END -->

<!-- TC:TC-FIN-048:START traces=AC-FIN-012,REQ-FIN-012,API-FIN-020 -->
### TC-FIN-048 — a remainder that computes to zero or less is refused
Derived from : AC-FIN-012  (REQ-FIN-012) — the positivity limit its RULE states
Exercises    : API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-010 → FIN-422-REMAINDER-NOT-POSITIVE (422)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active EventTypeRule whose non-remainder lines on the remainder line's own side
               already equal or exceed the total carried by the opposing side, leaving no residue
               for the remainder line to absorb; exactly one line marked `isRemainderFl`
Steps        : 1. POST `/api/v1/fin/journal-entries/from-event` with an event of that type
Expected     : 422 with code FIN-422-REMAINDER-NOT-POSITIVE —
               ar: «سطر الباقي يُحسب كفرق ويجب أن يكون موجبًا؛ السطور الأخرى تستهلك المبلغ بالكامل» ·
               en: "The remainder line is computed as a difference and must be positive; the
               other lines already consume the full amount".
               Nothing is posted. The refusal happens in the rule engine, before
               `CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE` could ever be reached — a negative amount is
               never offered to the database
Test data    : no figures are invented; the shape ("other lines already equal or exceed the
               opposing side") is RULE-FIN-010's own wording
<!-- TC:TC-FIN-048:END -->

<!-- TC:TC-FIN-049:START traces=AC-FIN-014,REQ-FIN-014,API-FIN-019 -->
### TC-FIN-049 — a manual entry with no lines never reaches the rules
Derived from : AC-FIN-014  (REQ-FIN-014) — the "at least two balanced lines" limit it states
Exercises    : API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : — (no FIN catalog code: the refusal is the platform validation envelope, before
               any RULE-FIN-* is evaluated)
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an accountant holding `PERM_FIN_JOURNAL_ENTRIES_CREATE`; a coherent
               fiscalYearId / periodId / docDate triple, so nothing else can fail first
Steps        : 1. POST `/api/v1/fin/journal-entries` with a well-formed header and an **empty**
                  `lines` array
Expected     : the request is refused at the DTO boundary — `JournalEntryCreateRequest.lines`
               carries `@NotEmpty` — and answers the platform `VALIDATION_ERROR` envelope in the
               caller's language, not a FIN code. No entry row is written and no `docNo` is drawn
               from the fiscal-year series. This is the boundary the backend plan names when it
               says the lineless entry shape "exists solely on the internal year-end path"
               (API-FIN-027) and cannot be reached through this DTO
Test data    : the empty line set is the boundary of AC-FIN-014's "at least two"
<!-- TC:TC-FIN-049:END -->
<!-- SUB:RULE-SCENARIOS:END -->

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
<!-- PHASE:TEST-PLAN-BE:END -->

## TC TRACEABILITY INDEX

**AC → TC**

| AC | TC | AC | TC |
|---|---|---|---|
| AC-FIN-001 | TC-FIN-001 | AC-FIN-024 | TC-FIN-024 |
| AC-FIN-002 | TC-FIN-002 | AC-FIN-025 | TC-FIN-025 |
| AC-FIN-003 | TC-FIN-003 | AC-FIN-026 | TC-FIN-026 |
| AC-FIN-004 | TC-FIN-004 | AC-FIN-027 | TC-FIN-027, TC-FIN-050 |
| AC-FIN-005 | TC-FIN-005 | AC-FIN-028 | TC-FIN-028 |
| AC-FIN-006 | TC-FIN-006 | AC-FIN-029 | TC-FIN-029 |
| AC-FIN-007 | TC-FIN-007 | AC-FIN-030 | TC-FIN-030 |
| AC-FIN-008 | TC-FIN-008 | AC-FIN-031 | TC-FIN-031, TC-FIN-051 |
| AC-FIN-009 | TC-FIN-009, TC-FIN-047 | AC-FIN-032 | TC-FIN-032 |
| AC-FIN-010 | TC-FIN-010 | AC-FIN-033 | TC-FIN-033 |
| AC-FIN-011 | TC-FIN-011 | AC-FIN-034 | TC-FIN-034 |
| AC-FIN-012 | TC-FIN-012, TC-FIN-048 | AC-FIN-035 | TC-FIN-035 |
| AC-FIN-013 | TC-FIN-013 | AC-FIN-036 | TC-FIN-036 |
| AC-FIN-014 | TC-FIN-014, TC-FIN-049 | AC-FIN-037 | TC-FIN-037 |
| AC-FIN-015 | TC-FIN-015 | AC-FIN-038 | TC-FIN-038, TC-FIN-052 |
| AC-FIN-016 | TC-FIN-016 | AC-FIN-039 | TC-FIN-039 |
| AC-FIN-017 | TC-FIN-017 | AC-FIN-040 | TC-FIN-040 |
| AC-FIN-018 | TC-FIN-018 | AC-FIN-041 | TC-FIN-041 |
| AC-FIN-019 | TC-FIN-019 | AC-FIN-042 | TC-FIN-042 |
| AC-FIN-020 | TC-FIN-020 | AC-FIN-043 | TC-FIN-043 |
| AC-FIN-021 | TC-FIN-021 | AC-FIN-044 | TC-FIN-044 |
| AC-FIN-022 | TC-FIN-022 | AC-FIN-045 | TC-FIN-045 |
| AC-FIN-023 | TC-FIN-023 | AC-FIN-046 | TC-FIN-046 |

**REQ → TC** — each `REQ-FIN-<n>` carries the TC of its own `AC-FIN-<n>`, plus:

| REQ | TC |
|---|---|
| REQ-FIN-009 | TC-FIN-009, TC-FIN-047 |
| REQ-FIN-012 | TC-FIN-012, TC-FIN-048 |
| REQ-FIN-014 | TC-FIN-014, TC-FIN-049 |
| REQ-FIN-027 | TC-FIN-027, TC-FIN-050 |
| REQ-FIN-031 | TC-FIN-031, TC-FIN-051 |
| REQ-FIN-038 | TC-FIN-038, TC-FIN-052 |
| REQ-FIN-003 | TC-FIN-003 (and RULE-FIN-007's effect, asserted there) |
| REQ-FIN-017 | TC-FIN-017 (the transition) · TC-FIN-010, TC-FIN-014, TC-FIN-023, TC-FIN-026, TC-FIN-036 (the same unified path from every other source) |
| REQ-FIN-046 | TC-FIN-046 (the chain) · TC-FIN-039, TC-FIN-040, TC-FIN-041, TC-FIN-042 (each hop's own report) |

**API → TC**

| API | TC |
|---|---|
| API-FIN-001 | TC-FIN-001, TC-FIN-002 (the unchanged-state assertion) |
| API-FIN-002 | TC-FIN-001 |
| API-FIN-003 | TC-FIN-002 |
| API-FIN-004 | TC-FIN-003 |
| API-FIN-005 | — no TC: no `AC-*` states a dimension search (ADR-FIN-009) |
| API-FIN-006 | TC-FIN-004 |
| API-FIN-007 | TC-FIN-005, TC-FIN-006 |
| API-FIN-008 | TC-FIN-005, TC-FIN-006 (the one-row assertion) |
| API-FIN-009 | — no TC: no `AC-*` states a rule search (ADR-FIN-009) |
| API-FIN-010 | TC-FIN-007 |
| API-FIN-011 | TC-FIN-008, TC-FIN-009, TC-FIN-047 |
| API-FIN-012 | — no TC: no `AC-*` states a template search (ADR-FIN-009) |
| API-FIN-013 | TC-FIN-022 |
| API-FIN-014 | TC-FIN-023, TC-FIN-024 |
| API-FIN-015 | — no TC: no `AC-*` states an allocation-rule search (ADR-FIN-009) |
| API-FIN-016 | TC-FIN-025 |
| API-FIN-017 | TC-FIN-026 |
| API-FIN-018 | TC-FIN-027, TC-FIN-050, TC-FIN-011 (the one-entry assertion) |
| API-FIN-019 | TC-FIN-003, TC-FIN-014, TC-FIN-015, TC-FIN-017, TC-FIN-018, TC-FIN-019, TC-FIN-020, TC-FIN-021, TC-FIN-033, TC-FIN-049 |
| API-FIN-020 | TC-FIN-010, TC-FIN-011, TC-FIN-012, TC-FIN-013, TC-FIN-048 |
| API-FIN-021 | TC-FIN-016, TC-FIN-028, TC-FIN-029, TC-FIN-030 |
| API-FIN-022 | TC-FIN-016, TC-FIN-017, TC-FIN-028, TC-FIN-046 |
| API-FIN-023 | TC-FIN-031, TC-FIN-051 |
| API-FIN-024 | TC-FIN-032, TC-FIN-035 |
| API-FIN-025 | TC-FIN-020, TC-FIN-033 |
| API-FIN-026 | TC-FIN-020, TC-FIN-034, TC-FIN-037, TC-FIN-038 |
| API-FIN-027 | TC-FIN-036, TC-FIN-052 |
| API-FIN-028 | TC-FIN-039, TC-FIN-046 |
| API-FIN-029 | TC-FIN-040, TC-FIN-046 |
| API-FIN-030 | TC-FIN-041, TC-FIN-046 |
| API-FIN-031 | TC-FIN-042 |
| API-FIN-032 | TC-FIN-043 |
| API-FIN-033 | — no TC: no `AC-*` states a fiscal-period search (ADR-FIN-009); it is cited only as setup |
| API-FIN-034 | — no TC of its own; cited as TC-FIN-013's alternative precondition (ADR-FIN-009) |
| API-FIN-035 | — no TC of its own; cited as TC-FIN-021's only precondition (ADR-FIN-009) |
| API-FIN-036 | — no TC: no `AC-*` states a template deactivate (ADR-FIN-009) |
| API-FIN-037 | — no TC: no `AC-*` states an allocation-rule deactivate (ADR-FIN-009) |

**RULE / catalog code → TC**

| RULE | code | TC |
|---|---|---|
| RULE-FIN-001 | FIN-409-HAS-CHILDREN (409) | TC-FIN-002 |
| RULE-FIN-001 | FIN-409-PARENT-NOT-LEAF-ELIGIBLE (409) | — no TC: no `AC-*` states the create-path variant |
| RULE-FIN-002 | FIN-409-DIMVALUE-DUP (409) | TC-FIN-006 |
| RULE-FIN-003 | FIN-409-REMAINDER-COUNT (409) | TC-FIN-009, TC-FIN-047 |
| RULE-FIN-003 | FIN-422-REMAINDER-MARKER (422) | — no TC: no `AC-*` states marker disagreement |
| RULE-FIN-004 | FIN-409-DUPLICATE-EVENT (409) | TC-FIN-011 |
| RULE-FIN-005 | FIN-404-NO-ACTIVE-RULE (404) | TC-FIN-013 |
| RULE-FIN-006 | FIN-409-UNBALANCED (409) | TC-FIN-015, TC-FIN-018 · TC-FIN-040 (the report invariant) |
| RULE-FIN-007 | FIN-409-NOT-POSTABLE-ACCOUNT (409) | TC-FIN-003, TC-FIN-015, TC-FIN-019 |
| RULE-FIN-008 | FIN-409-PERIOD-NOT-OPEN (409) | TC-FIN-020, TC-FIN-033 · TC-FIN-036 (the carve-out, asserted as not raised) |
| RULE-FIN-009 | FIN-409-INVALID-DIMENSION (409) | TC-FIN-021 |
| RULE-FIN-010 | — (success path) | TC-FIN-012, TC-FIN-026 |
| RULE-FIN-010 | FIN-422-REMAINDER-NOT-POSITIVE (422) | TC-FIN-048 |
| RULE-FIN-011 | — (success path, no distinct code) | TC-FIN-028, TC-FIN-024 |
| RULE-FIN-012 | — (success path, no distinct code) | TC-FIN-029, TC-FIN-024 |
| RULE-FIN-013 | FIN-409-ALREADY-REVERSED (409) | TC-FIN-030 |
| RULE-FIN-013 | FIN-409-NOT-POSTED (409) | — no TC: AC-FIN-030's Given is the already-reversed entry, not a DRAFT, and DRAFT is never a stored browsable state (SRS A7) |
| RULE-FIN-014 | FIN-409-NOT-REOPENABLE (409) | TC-FIN-035 · TC-FIN-032, TC-FIN-034 (the states around it) |
| RULE-FIN-015 | FIN-403-FORBIDDEN (403) | TC-FIN-038, TC-FIN-052 |
| RULE-FIN-016 | — (enforced by omission, no code) | TC-FIN-016, TC-FIN-017 |
| RULE-FIN-017 | FIN-400-PERIOD-NOT-IN-YEAR, FIN-400-DOCDATE-OUTSIDE-PERIOD (400) | — no TC: no `AC-*` states the incoherent triple; RULE-FIN-017 was added at SVC-API and carries no AC of its own (ADR-FIN-009) |

**Catalog rows no TC exercises**, stated rather than left unsaid: FIN-409-ACCOUNT-DUP,
FIN-409-DIMENSION-DUP, FIN-409-RULE-DUP, FIN-409-YEAR-DUP, FIN-400-INVALID-LOOKUP,
FIN-400-MISSING-FREQUENCY, FIN-400-INVALID-SORT, FIN-422-MAPPING-UNSUPPORTED,
FIN-422-REMAINDER-MARKER, FIN-422-INVALID-PERCENTAGE-VALUE, FIN-409-NOT-ACTIVE,
FIN-409-INVALID-TRANSITION, FIN-409-PERIODS-NOT-CLOSED, FIN-400-PERIOD-NOT-IN-YEAR,
FIN-400-DOCDATE-OUTSIDE-PERIOD, the five `FIN-404-*` not-found rows raised outside an AC's own
path, and FIN-500. No `AC-FIN-*` states any of them, and this engine derives from the ACs alone;
fabricating a TC for a code no acceptance criterion asserts is the over-engineering §3 rule 6
forbids. They are a requirements-side gap if anyone wants them covered, not a test-generation one
— ADR-FIN-009 records the boundary. `FIN-403-SOD-VIOLATION` is not in that list and must not be
tested at all: it is struck as unreachable, and TC-FIN-038 says so explicitly.

## COVERAGE

```
AC  covered   46/46   ✓ 0 gaps   (every AC-FIN-001…046 carries ≥1 TC on this track)
REQ covered   46/46   ✓ 0 gaps
API covered   26/37   — 11 endpoints carry no TC, each named in the API → TC index with the
                        reason; none is an AC left uncovered (ADR-FIN-009)
TC count      52      ✓ 1.13× the AC count — well under the ~2× over-engineering guard
Integration   n/a     scope = module: no INT-XM phase is emitted and none is owed (§2 rule 3).
                      XM-FIN-001 (SOFT-READ → MDL) is cited only as a precondition on the
                      lookup-validated creates, never asserted as a cross-module scenario
```
══════════════════════════════════════════════════════════════════
