# FRONTEND TEST PLAN — الحسابات العامة / Finance (General Ledger) (FIN)
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v1   Profile : erp   Track : frontend   Plan : test
Scope  : **module** — modules FIN (`gov.py run-standalone test-gen --module FIN`)
Sources: `_state/current-srs.md` (v1) · `_state/current-frontend-execution-plan.md` (v1) ·
         `_state/current-backend-execution-plan.md` (v1) · `_state/current-registry-srs.md` (v1) ·
         `_state/current-registry-exec-fe.md` (v1)
Framework : **agnostic** (`profile.stack.testing`). Every block below is the framework-agnostic
            TC form; no tool, annotation, fixture, selector or file layout is named anywhere in
            this plan. The consumer repo chooses its framework and turns each TC into a test.
REDUCED   : no — the frontend-execution-plan is present, so every step binds to a real `SCR-*`
            and its declared route.
TC ids    : TC-FIN-053 … TC-FIN-092 — the continuation of the one module sequence that
            `backend-test-plan-fin.md` opened at TC-FIN-001 and closed at TC-FIN-052. No id is
            reused and no id is reassigned to a different subject than the one it already carried.
Open ADRs : 1 raised by this run — ADR-FIN-009 (ACCEPTED, non-breaking). Applied and not
            re-derived: ADR-FIN-001 … ADR-FIN-008.
══════════════════════════════════════════════════════════════════

**Id order is AC order within this track.** Each TC below takes the next free id in AC order, so
TC-FIN-053 is AC-FIN-001's frontend case and TC-FIN-092 is AC-FIN-046's. The two SUBs group by
flow kind, so the ids are not monotonic inside a SUB; the traceability index is the address.

**Six ACs are out of this track, and none of them is a gap.** The engine's coverage rule is that
every `AC-FIN-*` carries ≥1 TC across the module; this track's denominator is the ACs that a
screen can exercise, which is 40 of the 46:

| AC | why it is not a frontend case | covered on the backend track by |
|---|---|---|
| AC-FIN-010, AC-FIN-011, AC-FIN-012, AC-FIN-013 | their When is "a canonical event arrives". API-FIN-020 is bound but drawn on no screen and constructed by no form — ADR-FIN-007, restated in F1-SCR-FIN-006 | TC-FIN-010, TC-FIN-011, TC-FIN-012, TC-FIN-013 |
| AC-FIN-044, AC-FIN-045 | onboarding, no screen — the P0.5 traceability matrix itself says so ("— (onboarding, no screen)") | TC-FIN-044, TC-FIN-045 |

Entries built from an event are still **seen and reversed** on SCR-FIN-006 like any other source;
what no screen does is construct one. ADR-FIN-009 records this denominator so no later reader
reads 40/40 as a silent narrowing of 46.

**What this track asserts and what it leaves to the server.** Where the frontend plan declares a
client-side pre-empt of a rule (F3), the TC asserts the pre-empt **and** the server's own refusal,
because the pre-empt is a convenience and the server remains authoritative. Where the plan routes
an error to a generic surface rather than a field, the TC says generic and does not invent a field
binding. No UI copy is invented anywhere: the only strings asserted are the SRS's own rule messages
and the Error Catalog's own rows, in both languages.

<!-- PHASE:TEST-PLAN-FE:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-003,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-014,REQ-FIN-015,REQ-FIN-016,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-022,REQ-FIN-023,REQ-FIN-024,REQ-FIN-025,REQ-FIN-026,REQ-FIN-027,REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,REQ-FIN-031,REQ-FIN-032,REQ-FIN-033,REQ-FIN-034,REQ-FIN-035,REQ-FIN-036,REQ-FIN-037,REQ-FIN-038,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-046,AC-FIN-001,AC-FIN-002,AC-FIN-003,AC-FIN-004,AC-FIN-005,AC-FIN-006,AC-FIN-007,AC-FIN-008,AC-FIN-009,AC-FIN-014,AC-FIN-015,AC-FIN-016,AC-FIN-017,AC-FIN-018,AC-FIN-019,AC-FIN-020,AC-FIN-021,AC-FIN-022,AC-FIN-023,AC-FIN-024,AC-FIN-025,AC-FIN-026,AC-FIN-027,AC-FIN-028,AC-FIN-029,AC-FIN-030,AC-FIN-031,AC-FIN-032,AC-FIN-033,AC-FIN-034,AC-FIN-035,AC-FIN-036,AC-FIN-037,AC-FIN-038,AC-FIN-039,AC-FIN-040,AC-FIN-041,AC-FIN-042,AC-FIN-043,AC-FIN-046 -->
## PHASE — TEST-PLAN-FE

TC count is 40, above the split threshold of 8, so this phase is split into the two groups the
engine names: per-screen flows, and the flows that only exist across screens.

<!-- SUB:UI-FLOWS:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-014,REQ-FIN-015,REQ-FIN-016,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-022,REQ-FIN-025,REQ-FIN-027,REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,REQ-FIN-031,REQ-FIN-032,REQ-FIN-033,REQ-FIN-034,REQ-FIN-035,REQ-FIN-036,REQ-FIN-037,REQ-FIN-038,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,AC-FIN-001,AC-FIN-002,AC-FIN-004,AC-FIN-005,AC-FIN-006,AC-FIN-007,AC-FIN-008,AC-FIN-009,AC-FIN-014,AC-FIN-015,AC-FIN-016,AC-FIN-017,AC-FIN-018,AC-FIN-019,AC-FIN-020,AC-FIN-021,AC-FIN-022,AC-FIN-025,AC-FIN-027,AC-FIN-028,AC-FIN-029,AC-FIN-030,AC-FIN-031,AC-FIN-032,AC-FIN-033,AC-FIN-034,AC-FIN-035,AC-FIN-036,AC-FIN-037,AC-FIN-038,AC-FIN-039,AC-FIN-040,AC-FIN-041,AC-FIN-042,AC-FIN-043 -->
### SUB — UI-FLOWS

One screen, one flow: search, create, the violation shown on the screen that caused it, the
state a row moves to, and the affordance a missing grant hides.

<!-- TC:TC-FIN-053:START traces=AC-FIN-001,REQ-FIN-001,SCR-FIN-001 -->
### TC-FIN-053 — an account is created from the chart screen
Derived from : AC-FIN-001  (REQ-FIN-001)
Exercises    : SCR-FIN-001 `/fin/accounts`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the effective menu the security module serves carries `FIN_ACCOUNTS` with its
               CREATE affordance; the `ACCOUNT_TYPE` and `DEBIT_CREDIT` option lists have loaded
               (UXD-FIN-001, UXD-FIN-002)
Steps        : 1. navigate to `/fin/accounts`
               2. open the entry side of the composite screen and fill code, both names, account
                  type, nature, optionally a parent, and the leaf flag
               3. submit
Expected     : the row is created and the list refreshes to include it — the screen's list cache
               is invalidated by the create, so no manual reload is needed. The type and nature
               selects offer the option lists MDL returns and submit **codes**, never labels.
               There is no `:id` route: the entry form hydrates from the search row (ADR-FIN-006)
Test data    : `ASSET` and `DEBIT` are seeded values; the code and names are placeholders
<!-- TC:TC-FIN-053:END -->

<!-- TC:TC-FIN-054:START traces=AC-FIN-002,REQ-FIN-002,SCR-FIN-001 -->
### TC-FIN-054 — the leaf flag is refused on an account that has children
Derived from : AC-FIN-002  (REQ-FIN-002)
Exercises    : SCR-FIN-001 `/fin/accounts`
Rule / code  : RULE-FIN-001 → FIN-409-HAS-CHILDREN (409), shown inline
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an account with at least one child, visible in the loaded tree; the caller holds
               the UPDATE affordance
Steps        : 1. navigate to `/fin/accounts` and select the parent account
               2. observe the leaf-flag control
               3. submit the update with the leaf flag set, bypassing the client pre-empt
Expected     : step 2 → the leaf control is **disabled and forced false** while the account has
               ≥1 child in the loaded tree — the declared client pre-empt of RULE-FIN-001.
               step 3 → the server's refusal is shown **inline on the leaf-flag field**, carrying
               the catalog message in the caller's language —
               ar: «لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا» ·
               en: "An account with sub-accounts cannot accept direct posting".
               The pre-empt is a convenience; the server stays authoritative and the row is
               unchanged either way
Test data    : parent and child are placeholders
<!-- TC:TC-FIN-054:END -->

<!-- TC:TC-FIN-056:START traces=AC-FIN-004,REQ-FIN-004,SCR-FIN-002 -->
### TC-FIN-056 — a dimension is created on the master side of the screen
Derived from : AC-FIN-004  (REQ-FIN-004)
Exercises    : SCR-FIN-002 `/fin/dimensions`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the effective menu carries `FIN_DIMENSIONS` with its CREATE affordance
Steps        : 1. navigate to `/fin/dimensions`
               2. create a dimension with code `PROJECT` and both names
Expected     : the dimension appears in the master list as active, and selecting it opens its
               (empty) values panel. The screen is one composite master + detail with no `:id`
               route (ADR-FIN-006), and the master list cache is invalidated by the create
Test data    : `PROJECT` — ENT-FIN-002's own example code
<!-- TC:TC-FIN-056:END -->

<!-- TC:TC-FIN-057:START traces=AC-FIN-005,REQ-FIN-005,SCR-FIN-002 -->
### TC-FIN-057 — a value is added under the selected dimension
Derived from : AC-FIN-005  (REQ-FIN-005)
Exercises    : SCR-FIN-002 `/fin/dimensions`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the dimension `PROJECT` exists and is selected; it holds no value with the code
               about to be used; the caller holds the CREATE affordance
Steps        : 1. in the values panel, add a value with code `NORTH`, both names and a sort order
Expected     : the value appears in the detail list of that dimension and of no other — the
               values query is keyed by the selected dimension id, so selecting a different
               dimension shows a different set. The sort order is a required integer
Test data    : `NORTH` — the code AC-FIN-006 names; the sort order is a placeholder integer
<!-- TC:TC-FIN-057:END -->

<!-- TC:TC-FIN-058:START traces=AC-FIN-006,REQ-FIN-006,SCR-FIN-002 -->
### TC-FIN-058 — a duplicate value code is refused on the screen that entered it
Derived from : AC-FIN-006  (REQ-FIN-006)
Exercises    : SCR-FIN-002 `/fin/dimensions`
Rule / code  : RULE-FIN-002 → FIN-409-DIMVALUE-DUP (409), shown inline on the code field
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the dimension `PROJECT` already holds a value with code `NORTH`, and its values
               are loaded on screen
Steps        : 1. add a second value with code `NORTH` under the same dimension
               2. submit past the client-side uniqueness pre-check
Expected     : step 1 → the client-side uniqueness pre-check against the loaded values marks the
               code field before any request is sent.
               step 2 → the server answers and the message is shown **inline on the code field** —
               ar: «هذا الرمز مستخدم بالفعل ضمن هذا البُعد» ·
               en: "This code is already used within this dimension".
               The detail list still shows exactly one `NORTH`
Test data    : `NORTH` and `PROJECT` — the AC's and the entity's own values
<!-- TC:TC-FIN-058:END -->

<!-- TC:TC-FIN-059:START traces=AC-FIN-007,REQ-FIN-007,SCR-FIN-003 -->
### TC-FIN-059 — an event-type rule is created on the engine-rules screen
Derived from : AC-FIN-007  (REQ-FIN-007)
Exercises    : SCR-FIN-003 `/fin/rules`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the effective menu carries `FIN_RULES` with CREATE; the five option lists the
               screen needs have loaded (UXD-FIN-007, UXD-FIN-008, UXD-FIN-009, UXD-FIN-002,
               UXD-FIN-010); the chosen event type has no rule yet
Steps        : 1. navigate to `/fin/rules`
               2. create a rule with an event type and both names
Expected     : the rule appears in the master list as active and can be selected to reveal its
               (empty) lines panel. The event-type select offers the `ACCOUNTING_EVENT_TYPE`
               options MDL returns — which may legitimately be an empty list, the type being
               host-defined and seeding no value
Test data    : the event type code is a placeholder
<!-- TC:TC-FIN-059:END -->

<!-- TC:TC-FIN-060:START traces=AC-FIN-008,REQ-FIN-008,SCR-FIN-003 -->
### TC-FIN-060 — a rule line captures its three references as three controls
Derived from : AC-FIN-008  (REQ-FIN-008)
Exercises    : SCR-FIN-003 `/fin/rules`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a rule is selected; the caller holds the UPDATE affordance, which is what the
               add-line action follows
Steps        : 1. add a line, setting the account-derivation type and value, the amount-source
                  type and value, the direction and the distribution type
Expected     : the line is added to the selected rule's lines panel. The account derivation, the
               amount source and the direction are **three separate controls**, never fused: the
               amount-source value is required unless the amount-source type is `REMAINDER`, which
               is the one conditional the form declares. There is no line-delete affordance —
               none is published (ADR-FIN-006)
Test data    : `CONSTANT`, `FIELD`, `FIXED`, `DEBIT`, `REMAINDER` — seeded values (SRS A6)
<!-- TC:TC-FIN-060:END -->

<!-- TC:TC-FIN-061:START traces=AC-FIN-009,REQ-FIN-009,SCR-FIN-003 -->
### TC-FIN-061 — the remainder-line count is checked across the whole line set
Derived from : AC-FIN-009  (REQ-FIN-009)
Exercises    : SCR-FIN-003 `/fin/rules`
Rule / code  : RULE-FIN-003 → FIN-409-REMAINDER-COUNT (409), shown inline
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a selected rule whose loaded line set holds two `PERCENTAGE` lines and no line
               marked as the remainder
Steps        : 1. attempt to save the line set as it stands
               2. submit past the client cross-field check
Expected     : step 1 → the cross-field check runs **across the whole line list**, not per line:
               exactly one line may carry the remainder marker once any sibling is `PERCENTAGE`.
               step 2 → the server's message is shown inline —
               ar: «يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي» ·
               en: "Exactly one remainder line is required when any percentage distribution is
               present".
               The set stays unsaved until exactly one line is marked remainder, which is
               AC-FIN-009's own Then
Test data    : two percentage lines and zero remainder — AC-FIN-009's own counts
<!-- TC:TC-FIN-061:END -->

<!-- TC:TC-FIN-062:START traces=AC-FIN-014,REQ-FIN-014,SCR-FIN-006 -->
### TC-FIN-062 — a manual entry is composed and posted from its own route
Derived from : AC-FIN-014  (REQ-FIN-014)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : — (happy path; RULE-FIN-017's pairing is pre-empted by the filtered period select)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the effective menu carries `FIN_JOURNAL_ENTRIES` with its CREATE affordance; the
               fiscal-period list has loaded for the year/period pair; at least two leaf active
               accounts and an Open period exist
Steps        : 1. navigate to `/fin/journal-entries/new`
               2. pick a fiscal year, then a period from the select **filtered by that year**
               3. add two balanced lines and post
Expected     : the entry is created and the search list is invalidated so the new row appears.
               The period options are narrowed to the chosen year, which is the client pre-empt of
               RULE-FIN-017; the journal type is a fixed, non-editable `MANUAL` and is not offered
               as a select (ADR-FIN-008); the document number is never an input and appears only
               on the created row
Test data    : `MANUAL` — a seeded `JOURNAL_TYPE` value; two lines — AC-FIN-014's own minimum
<!-- TC:TC-FIN-062:END -->

<!-- TC:TC-FIN-063:START traces=AC-FIN-015,REQ-FIN-015,SCR-FIN-006 -->
### TC-FIN-063 — both validation failures are shown and nothing is posted
Derived from : AC-FIN-015  (REQ-FIN-015)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-006 → FIN-409-UNBALANCED · RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an entry form filled so that it is both unbalanced and targets a rollup account on
               one line; the year, period and document date are coherent so nothing else fails
               first
Steps        : 1. submit the entry from `/fin/journal-entries/new`
Expected     : **both** failures are surfaced together, not the first alone, each field-level
               where the offending line or field is named —
               FIN-409-UNBALANCED: ar «القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن» ·
               en "The entry is unbalanced — total debits do not equal total credits"; and
               FIN-409-NOT-POSTABLE-ACCOUNT: ar «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en "The target account does not accept direct posting".
               The form keeps the accountant's input, and the search list gains no row: nothing
               was posted
Test data    : the two failing conditions are AC-FIN-015's own count
<!-- TC:TC-FIN-063:END -->

<!-- TC:TC-FIN-064:START traces=AC-FIN-016,REQ-FIN-016,SCR-FIN-006 -->
### TC-FIN-064 — a posted entry offers no delete, only a reverse
Derived from : AC-FIN-016  (REQ-FIN-016)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : RULE-FIN-016 → no code: enforced by omission, and mirrored on the screen by the
               absence of the affordance
Scenario     : STATE · data class EDGE · language ALL
Preconditions: a POSTED entry; the caller holds `FIN_JOURNAL_ENTRIES` VIEW and the custom Reverse
               affordance
Steps        : 1. open `/fin/journal-entries/:id` for that entry
               2. enumerate the actions the row offers
Expected     : the row offers **Reverse and nothing that deletes or edits it**. No delete
               affordance exists anywhere on the screen for any row, in any permission
               combination — FIN publishes no delete endpoint, so drawing one would be an
               affordance with nothing behind it (ADR-FIN-006). The correction path a user is
               given is the reversal, which is AC-FIN-016's own Then
Test data    : none beyond the existing entry
<!-- TC:TC-FIN-064:END -->

<!-- TC:TC-FIN-065:START traces=AC-FIN-017,REQ-FIN-017,SCR-FIN-006 -->
### TC-FIN-065 — a posted entry is read-only and shows its posted state
Derived from : AC-FIN-017  (REQ-FIN-017)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : RULE-FIN-016 → no code (the lock is a fact about the surface, not a message)
Scenario     : STATE · data class VALID · language ALL
Preconditions: an entry just posted through TC-FIN-062's flow
Steps        : 1. open the entry's read route
               2. attempt to change any header or line field
Expected     : the status reads POSTED and the posted-at moment is shown; the header and the lines
               are rendered **read-only**, with no editable control and no save affordance, since
               there is no update endpoint to receive one. The status label resolves through the
               `JOURNAL_STATUS` option list (UXD-FIN-006) and is displayed in the caller's
               language, the code itself never being shown raw
Test data    : none invented
<!-- TC:TC-FIN-065:END -->

<!-- TC:TC-FIN-066:START traces=AC-FIN-018,REQ-FIN-018,SCR-FIN-006 -->
### TC-FIN-066 — live totals warn, and the server refuses, an entry a hundredth out
Derived from : AC-FIN-018  (REQ-FIN-018)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-006 → FIN-409-UNBALANCED (409), shown inline
Scenario     : VIOLATION · data class BOUNDARY · language ALL
Preconditions: an entry form with every line on a leaf, active account and an Open period, so the
               imbalance is the only failing check
Steps        : 1. enter debit lines totalling 100.00 and credit lines totalling 99.99
               2. observe the live debit/credit totals the screen keeps while entering
               3. submit
Expected     : step 2 → the totals are shown side by side and do not match, the screen's own echo
               of RULE-FIN-006.
               step 3 → the server refuses and the message is shown inline —
               ar: «القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن» ·
               en: "The entry is unbalanced — total debits do not equal total credits" —
               with the imbalance amount. The client echo never substitutes for the server check:
               submitting with the echo bypassed still fails
Test data    : 100.00 and 99.99 — AC-FIN-018's own values
<!-- TC:TC-FIN-066:END -->

<!-- TC:TC-FIN-067:START traces=AC-FIN-019,REQ-FIN-019,SCR-FIN-006 -->
### TC-FIN-067 — a rollup account on a line is reported against that line
Derived from : AC-FIN-019  (REQ-FIN-019)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409), shown inline
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a balanced entry form in which exactly one line targets a non-leaf (rollup) account
Steps        : 1. submit the entry
Expected     : the refusal is shown **against the offending line**, not as a screen-level banner,
               carrying —
               ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
               en: "The target account does not accept direct posting".
               The other lines keep their values so the accountant can correct one line and
               resubmit; nothing was posted
Test data    : account ids are placeholders; the rollup relation is the AC's own fact
<!-- TC:TC-FIN-067:END -->

<!-- TC:TC-FIN-068:START traces=AC-FIN-020,REQ-FIN-020,SCR-FIN-006 -->
### TC-FIN-068 — an entry composed against a period that has since closed is refused
Derived from : AC-FIN-020  (REQ-FIN-020)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN (409), shown inline
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: a period Open when the form is filled, and hard-closed from SCR-FIN-007 by an
               approver before the form is submitted
Steps        : 1. fill a balanced entry against that period and leave the form open
               2. hard-close the period from `/fin/periods` in another session
               3. submit the form
Expected     : the refusal names the period's **current** state —
               ar: «الفترة المستهدفة غير مفتوحة» · en: "The target period is not open" —
               even though the period was Open when the form was filled. A stale option list is
               not an authority: the check is the server's, at post time
Test data    : none invented
<!-- TC:TC-FIN-068:END -->

<!-- TC:TC-FIN-069:START traces=AC-FIN-021,REQ-FIN-021,SCR-FIN-006 -->
### TC-FIN-069 — a line citing an inactive dimension value is refused
Derived from : AC-FIN-021  (REQ-FIN-021)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`, with SCR-FIN-002 as setup
Rule / code  : RULE-FIN-009 → FIN-409-INVALID-DIMENSION (409), shown inline
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a dimension value that has been deactivated from `/fin/dimensions` — the only way
               the inactive state is reachable at all (ADR-FIN-009)
Steps        : 1. on `/fin/dimensions`, deactivate a value of `PROJECT`
               2. on `/fin/journal-entries/new`, add a line citing that dimension value and submit
Expected     : the refusal is shown against the offending line and names the dimension —
               ar: «قيمة البُعد غير صالحة» · en: "The dimension value is invalid".
               Nothing is posted. The deactivate action on SCR-FIN-002 follows the screen's UPDATE
               affordance, and the parent dimension itself offers no deactivate — none is
               published
Test data    : `PROJECT` and one of its values
<!-- TC:TC-FIN-069:END -->

<!-- TC:TC-FIN-070:START traces=AC-FIN-022,REQ-FIN-022,SCR-FIN-004 -->
### TC-FIN-070 — a template is created with its schedule and lines in one submit
Derived from : AC-FIN-022  (REQ-FIN-022)
Exercises    : SCR-FIN-004 `/fin/recurring-templates`
Rule / code  : — (happy path; the frequency conditional is pre-empted on the form)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the effective menu carries `FIN_RECURRING_TEMPLATES` with CREATE; the schedule-type
               and frequency option lists have loaded (UXD-FIN-011, UXD-FIN-012)
Steps        : 1. choose schedule type `RECURRING` and observe the frequency control
               2. choose `REVERSING` and observe it again
               3. return to `RECURRING`, give a frequency, a start date and at least two balanced
                  lines, and submit
Expected     : the frequency is **required only when the schedule type is `RECURRING`** — the
               declared cross-field rule, which pre-empts FIN-400-MISSING-FREQUENCY — and is not
               required for `REVERSING`. The line set's debit = credit balance is checked before
               submit as a convenience, the server re-checking at run time. The created template
               appears in the list with its lines, and the screen offers **no Edit affordance**:
               no update endpoint is published (ADR-FIN-006)
Test data    : `RECURRING`, `REVERSING`, `MONTHLY` — seeded values (SRS A6)
<!-- TC:TC-FIN-070:END -->

<!-- TC:TC-FIN-073:START traces=AC-FIN-025,REQ-FIN-025,SCR-FIN-005 -->
### TC-FIN-073 — an allocation rule is created with its targets
Derived from : AC-FIN-025  (REQ-FIN-025)
Exercises    : SCR-FIN-005 `/fin/allocation-rules`
Rule / code  : RULE-FIN-003 → FIN-409-REMAINDER-COUNT (409) when the target set breaks it
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the effective menu carries `FIN_ALLOCATION_RULES` with CREATE; the
               `DISTRIBUTION_TYPE` option list has loaded (UXD-FIN-010)
Steps        : 1. create a rule with a source account and at least one target carrying a
                  distribution type
               2. add a second `PERCENTAGE` target without marking any target as the remainder,
                  and submit
Expected     : step 1 → the rule and its targets are stored in one submit and appear in the list
                  with the targets included.
               step 2 → the same cross-field remainder check as the engine-rules screen fires,
                  and the server's refusal is shown inline —
                  ar: «يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي» ·
                  en: "Exactly one remainder line is required when any percentage distribution is
                  present".
               A target's distribution value is required unless its type is `REMAINDER`. The
               screen offers no Edit affordance — no update endpoint is published
Test data    : `PERCENTAGE`, `REMAINDER`, `FIXED` — seeded values
<!-- TC:TC-FIN-073:END -->

<!-- TC:TC-FIN-075:START traces=AC-FIN-027,REQ-FIN-027,SCR-FIN-006 -->
### TC-FIN-075 — filtering the journal list returns exactly the matching rows
Derived from : AC-FIN-027  (REQ-FIN-027)
Exercises    : SCR-FIN-006 `/fin/journal-entries`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: entries exist across several statuses and periods; the caller holds
               `FIN_JOURNAL_ENTRIES` VIEW
Steps        : 1. navigate to `/fin/journal-entries`
               2. filter by period and by status
               3. page through the result
Expected     : exactly the matching entries are listed, unmodified, and the page and page size
               are part of the filter that keys the list — so paging re-queries the server rather
               than slicing a client-side array, and returning to a previous page does not
               re-order or re-shape the rows. The status and journal-type filters offer the
               `JOURNAL_STATUS` and `JOURNAL_TYPE` option lists (UXD-FIN-006, UXD-FIN-005)
Test data    : the filter pair is AC-FIN-027's own
<!-- TC:TC-FIN-075:END -->

<!-- TC:TC-FIN-076:START traces=AC-FIN-028,REQ-FIN-028,SCR-FIN-006 -->
### TC-FIN-076 — Reverse posts a mirrored entry and both rows point at each other
Derived from : AC-FIN-028  (REQ-FIN-028)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : RULE-FIN-011 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a POSTED entry with three lines; the caller holds the custom Reverse affordance,
               which is independent of CREATE
Steps        : 1. open the entry and invoke Reverse
               2. open the created reversal, then return to the original
Expected     : a new entry is posted with the same three line amounts and opposite directions, and
               each row shows the other's link. **The original still reads POSTED** — it is not
               voided — so both appear in the list and the pair nets to zero. The list is
               invalidated, so the reversal appears without a manual reload
Test data    : three lines — AC-FIN-028's own count
<!-- TC:TC-FIN-076:END -->

<!-- TC:TC-FIN-077:START traces=AC-FIN-029,REQ-FIN-029,SCR-FIN-006 -->
### TC-FIN-077 — reversing an entry of a closed period lands in the current open period
Derived from : AC-FIN-029  (REQ-FIN-029)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : RULE-FIN-012 → success path, no code
Scenario     : STATE · data class EDGE · language ALL
Preconditions: a POSTED entry whose own period is now hard-closed, and a different period that is
               currently Open
Steps        : 1. open the entry and invoke Reverse
Expected     : the reversal is created and its period, shown on the created row, is the **current
               open** period rather than the original's closed one. The action is not blocked and
               no period-not-open message appears: the substitution is the rule's effect, not a
               refusal, and the user is never asked to choose the period
Test data    : none invented
<!-- TC:TC-FIN-077:END -->

<!-- TC:TC-FIN-078:START traces=AC-FIN-030,REQ-FIN-030,SCR-FIN-006 -->
### TC-FIN-078 — a second Reverse on the same entry is refused
Derived from : AC-FIN-030  (REQ-FIN-030)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : RULE-FIN-013 → FIN-409-ALREADY-REVERSED (409), shown on the screen's generic
               surface (the plan routes reverse failures there, not to a field)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an entry already reversed once, still POSTED and carrying its reversal link
Steps        : 1. open that entry and invoke Reverse again
Expected     : the action is refused and the message is shown —
               ar: «تم عكس هذا القيد بالفعل ولا يمكن عكسه مرة أخرى» ·
               en: "This entry has already been reversed and cannot be reversed again".
               No second mirror appears in the list. The message is shown generically rather than
               bound to a field, there being no field to bind it to
Test data    : none invented
<!-- TC:TC-FIN-078:END -->

<!-- TC:TC-FIN-079:START traces=AC-FIN-031,REQ-FIN-031,SCR-FIN-007 -->
### TC-FIN-079 — creating a year draws its periods as the detail rows
Derived from : AC-FIN-031  (REQ-FIN-031)
Exercises    : SCR-FIN-007 `/fin/periods`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the effective menu carries `FIN_PERIODS` with its CREATE affordance; the
               `PERIOD_STATE` and `FISCAL_YEAR_STATUS` option lists have loaded (UXD-FIN-003,
               UXD-FIN-004)
Steps        : 1. navigate to `/fin/periods`
               2. create a year with a code, a start date, an end date after it, and a period
                  count
Expected     : the year and its generated periods are shown, **every period reading OPEN** —
               AC-FIN-031's own Then. The end date must be later than the start date and the
               period count must be a positive integer, both checked on the form. The year list
               itself is derived from the period rows' own year id (ADR-FIN-006): there is no
               fiscal-year search endpoint and the screen draws no route for one
Test data    : `2026` and a twelve-period year; the dates are placeholders
<!-- TC:TC-FIN-079:END -->

<!-- TC:TC-FIN-080:START traces=AC-FIN-032,REQ-FIN-032,SCR-FIN-007 -->
### TC-FIN-080 — a soft-closed period is reopened from its row
Derived from : AC-FIN-032  (REQ-FIN-032)
Exercises    : SCR-FIN-007 `/fin/periods`
Rule / code  : — (happy path)
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period row reading SOFT_CLOSE; the caller holds the UPDATE affordance
Steps        : 1. invoke Open on that row
Expected     : the row's state becomes OPEN and the period list is invalidated so the change is
               visible at once. The row action takes no input beyond the row itself — the
               affordance is the whole form
Test data    : none invented
<!-- TC:TC-FIN-080:END -->

<!-- TC:TC-FIN-081:START traces=AC-FIN-033,REQ-FIN-033,SCR-FIN-007 -->
### TC-FIN-081 — soft-closing a period is visible on its row
Derived from : AC-FIN-033  (REQ-FIN-033)
Exercises    : SCR-FIN-007 `/fin/periods`
Rule / code  : FIN-409-INVALID-TRANSITION (409) when the row is not Open
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period row reading OPEN; the caller holds the UPDATE affordance
Steps        : 1. invoke Soft-close on that row
               2. invoke Soft-close again on the now soft-closed row
Expected     : step 1 → the row reads SOFT_CLOSE.
               step 2 → the server refuses the second transition —
                  ar: «لا يمكن تنفيذ هذا الانتقال من الحالة الحالية» ·
                  en: "This transition is not allowed from the current status" —
                  shown on the screen's generic surface. Posting into the period is refused from
                  SCR-FIN-006 thereafter, which is AC-FIN-033's own consequence
Test data    : none invented
<!-- TC:TC-FIN-081:END -->

<!-- TC:TC-FIN-082:START traces=AC-FIN-034,REQ-FIN-034,SCR-FIN-007 -->
### TC-FIN-082 — hard-close is the approver's action and is permanent
Derived from : AC-FIN-034  (REQ-FIN-034)
Exercises    : SCR-FIN-007 `/fin/periods`
Rule / code  : RULE-FIN-015 (the gate) · RULE-FIN-014 (the terminal state)
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period row reading SOFT_CLOSE; the caller's effective menu carries the custom
               Close-approve flag
Steps        : 1. invoke Hard-close on that row
               2. look for an Open affordance on the row afterwards
Expected     : step 1 → the row reads HARD_CLOSE and shows who closed it and when.
               step 2 → reopening is no longer available, and invoking it anyway is refused
                  (TC-FIN-083). The hard-close affordance is rendered from the Close-approve
                  presence flag alone, never from a client-composed permission string
Test data    : none invented
<!-- TC:TC-FIN-082:END -->

<!-- TC:TC-FIN-083:START traces=AC-FIN-035,REQ-FIN-035,SCR-FIN-007 -->
### TC-FIN-083 — reopening a hard-closed period is refused on the screen
Derived from : AC-FIN-035  (REQ-FIN-035)
Exercises    : SCR-FIN-007 `/fin/periods`
Rule / code  : RULE-FIN-014 → FIN-409-NOT-REOPENABLE (409), shown on the generic surface
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a period row reading HARD_CLOSE; the caller holds the UPDATE affordance
Steps        : 1. invoke Open on that row
Expected     : refused, with —
               ar: «الفترة مغلقة إغلاقًا صارمًا ولا يمكن إعادة فتحها» ·
               en: "The period is hard-closed and cannot be reopened".
               The row's state is unchanged. The client never hides the rule behind a disabled
               control alone: the server is the authority, and this message is what a user sees
Test data    : none invented
<!-- TC:TC-FIN-083:END -->

<!-- TC:TC-FIN-084:START traces=AC-FIN-036,REQ-FIN-036,SCR-FIN-007 -->
### TC-FIN-084 — year-end close is offered only once every period is hard-closed
Derived from : AC-FIN-036  (REQ-FIN-036)
Exercises    : SCR-FIN-007 `/fin/periods`
Rule / code  : FIN-409-PERIODS-NOT-CLOSED (409), shown inline, when it is invoked too early
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a selected year; the caller's menu carries the Close-approve flag
Steps        : 1. with at least one period not HARD_CLOSE, observe the Run-year-end-close
                  affordance
               2. hard-close the remaining periods, then invoke it
Expected     : step 1 → the affordance is **enabled only once every period of the selected year
                  reads HARD_CLOSE**; invoking it regardless is refused with —
                  ar: «يجب إغلاق كل الفترات إغلاقًا صارمًا أولًا» ·
                  en: "Every period must be hard-closed first".
               step 2 → the close runs and posts the closing and opening entries, so the journal
                  list is invalidated too and both new entries are visible on SCR-FIN-006
Test data    : none invented
<!-- TC:TC-FIN-084:END -->

<!-- TC:TC-FIN-085:START traces=AC-FIN-037,REQ-FIN-037,SCR-FIN-007 -->
### TC-FIN-085 — the approving principal and moment are shown on the closed row
Derived from : AC-FIN-037  (REQ-FIN-037)
Exercises    : SCR-FIN-007 `/fin/periods`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a period just hard-closed by an approver, containing entries created by a
               different principal
Steps        : 1. read the closed period's row
Expected     : the row shows **who** closed it and **when**, distinct from the creator recorded on
               any entry of that period. The approval is legible on the screen as an act of its
               own rather than inferred from an entry's audit data — AC-FIN-037's own point
Test data    : principals are placeholders
<!-- TC:TC-FIN-085:END -->

<!-- TC:TC-FIN-086:START traces=AC-FIN-038,REQ-FIN-038,SCR-FIN-007 -->
### TC-FIN-086 — an entry creator never gets the close-approve affordance
Derived from : AC-FIN-038  (REQ-FIN-038)
Exercises    : SCR-FIN-007 `/fin/periods`
Rule / code  : RULE-FIN-015 → FIN-403-FORBIDDEN (403) if the call is made anyway
Scenario     : PERMISSION · data class ATTACK · language ALL
Preconditions: a user whose role holds the journal-entry-creation permission and **not** the
               close-approve one; the `FIN_PERIODS` page is in the effective menu with VIEW, so
               the screen itself is reachable
Steps        : 1. navigate to `/fin/periods`
               2. inspect each period row's actions
               3. issue the hard-close request directly, bypassing the hidden affordance
Expected     : step 2 → Open and Soft-close may be offered per grant, but **hard-close and
                  year-end-close are not**: their affordance follows the Close-approve presence
                  flag, which this user does not carry.
               step 3 → the server denies it —
                  ar: «لا تملك صلاحية المالية المطلوبة لهذه العملية» ·
                  en: "You do not hold the Finance permission required for this operation" —
                  and the response routes to the unauthorized surface. The separation of duty is
                  **server-enforced**; the hidden affordance is not the control, and the client
                  runs no separation-of-duty check of its own
Test data    : the two permission codes are the registry's own
<!-- TC:TC-FIN-086:END -->

<!-- TC:TC-FIN-087:START traces=AC-FIN-039,REQ-FIN-039,SCR-FIN-008 -->
### TC-FIN-087 — the account ledger screen shows a live running balance
Derived from : AC-FIN-039  (REQ-FIN-039)
Exercises    : SCR-FIN-008 `/fin/reports/account-ledger`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an account with posted lines across two periods; the caller holds
               `FIN_ACCOUNT_LEDGER` in the effective menu
Steps        : 1. navigate to `/fin/reports/account-ledger`
               2. set the account and a date range covering both periods
               3. copy the resulting URL, open it fresh, and compare
Expected     : the rows carry a running balance computed at request time, each linking to its
               entry. The filter state is mirrored into the route's search parameters, so step 3
               reproduces the same filtered report from the URL alone — the screen is a full page
               with no entry sub-view (ADR-FIN-003). The account filter is required; a date range
               and a dimension pair are optional, the dimension value requiring its dimension
Test data    : two periods — AC-FIN-039's own; ids are placeholders
<!-- TC:TC-FIN-087:END -->

<!-- TC:TC-FIN-088:START traces=AC-FIN-040,REQ-FIN-040,SCR-FIN-009 -->
### TC-FIN-088 — the trial balance screen shows equal debit and credit totals
Derived from : AC-FIN-040  (REQ-FIN-040)
Exercises    : SCR-FIN-009 `/fin/reports/trial-balance`
Rule / code  : — (read-only; the equality is an invariant by construction)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: posted entries exist in a period; the caller holds `FIN_TRIAL_BALANCE`
Steps        : 1. navigate to `/fin/reports/trial-balance`
               2. run it with a period, then with the period filter cleared
Expected     : one row per account, and the **total debit balances equal the total credit
               balances** in both runs. Both filters are optional, so clearing the period is a
               legitimate request and not an error state. Account names are shown in the caller's
               language, and the account-type filter offers the `ACCOUNT_TYPE` option list
               (UXD-FIN-001)
Test data    : none invented
<!-- TC:TC-FIN-088:END -->

<!-- TC:TC-FIN-089:START traces=AC-FIN-041,REQ-FIN-041,SCR-FIN-010 -->
### TC-FIN-089 — the balance sheet carries the prior year's closing balances forward
Derived from : AC-FIN-041  (REQ-FIN-041)
Exercises    : SCR-FIN-010 `/fin/reports/balance-sheet`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a completed year-end close; the caller holds `FIN_BALANCE_SHEET`
Steps        : 1. run the report for the closed year
               2. run it for the new year and compare, account by account
Expected     : the new year's opening balances equal the prior year's closing balances for every
               balance-sheet account, the figures grouped into asset, liability and equity
               sections with their totals. The fiscal year is a **required** filter, so the screen
               does not issue a request until one is chosen, and an as-of date narrows it
               optionally
Test data    : none invented
<!-- TC:TC-FIN-089:END -->

<!-- TC:TC-FIN-090:START traces=AC-FIN-042,REQ-FIN-042,SCR-FIN-011 -->
### TC-FIN-090 — the income statement opens the new year at zero
Derived from : AC-FIN-042  (REQ-FIN-042)
Exercises    : SCR-FIN-011 `/fin/reports/income-statement`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a completed year-end close and **no posting yet** in the new year; the caller holds
               `FIN_INCOME_STATEMENT`
Steps        : 1. run the report for the new fiscal year, before any posting
Expected     : every revenue and expense account shows a zero balance, grouped into revenue and
               expense sections with their totals — while TC-FIN-089's balance sheet, run for the
               same year, carries its brought-forward balances. The fiscal year is required; the
               two period bounds are optional and travel together as a range
Test data    : none invented
<!-- TC:TC-FIN-090:END -->

<!-- TC:TC-FIN-091:START traces=AC-FIN-043,REQ-FIN-043,SCR-FIN-012 -->
### TC-FIN-091 — the dimension report shows one row per account and dimension value
Derived from : AC-FIN-043  (REQ-FIN-043)
Exercises    : SCR-FIN-012 `/fin/reports/dimension`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: postings on one account under two different `PROJECT` dimension values; the caller
               holds `FIN_DIMENSION_REPORTS`
Steps        : 1. navigate to `/fin/reports/dimension` and run it by the `PROJECT` dimension
Expected     : **two separate rows** for that account, one per project value, never one combined
               row — AC-FIN-043's own Then. The dimension filter is required; a dimension value
               and a period narrow it optionally. Filter state is mirrored into the route's search
               parameters, so the run is linkable
Test data    : `PROJECT` and two of its values — AC-FIN-043's own shape
<!-- TC:TC-FIN-091:END -->
<!-- SUB:UI-FLOWS:END -->

<!-- SUB:INT-FLOW:START traces=REQ-FIN-003,REQ-FIN-023,REQ-FIN-024,REQ-FIN-026,REQ-FIN-046,AC-FIN-003,AC-FIN-023,AC-FIN-024,AC-FIN-026,AC-FIN-046 -->
### SUB — INT-FLOW

The flows that do not fit on one screen: a definition retired here changes what is accepted
there, a run here posts an entry that is read there, and a report drills into the entry behind it.

<!-- TC:TC-FIN-055:START traces=AC-FIN-003,REQ-FIN-003,SCR-FIN-001,SCR-FIN-006 -->
### TC-FIN-055 — deactivating an account on one screen closes it on another
Derived from : AC-FIN-003  (REQ-FIN-003)
Exercises    : SCR-FIN-001 `/fin/accounts` → SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409), on the second screen
Scenario     : STATE · data class VALID · language ALL
Preconditions: an active leaf account used on entries today; an Open period; the caller holds the
               UPDATE affordance on the chart screen and CREATE on the journal screen
Steps        : 1. on `/fin/accounts`, deactivate the account, confirming the usage prompt the
                  screen raises before the call
               2. filter the chart list by active only, and look for the account
               3. on `/fin/journal-entries/new`, compose a balanced entry with a line on it
Expected     : step 1 → the row is deactivated; the confirmation is a prompt, not a rule check —
                  the parent-child case is the server's to refuse and is never pre-empted here.
               step 2 → the account is gone from the active results.
               step 3 → the post is refused with —
                  ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
                  en: "The target account does not accept direct posting".
               This is the full lifecycle AC-FIN-003 describes: create, search, deactivate, gone
               from active results, and rejected by REQ-FIN-019 thereafter
Test data    : the account is a placeholder
<!-- TC:TC-FIN-055:END -->

<!-- TC:TC-FIN-071:START traces=AC-FIN-023,REQ-FIN-023,SCR-FIN-004,SCR-FIN-006 -->
### TC-FIN-071 — running a template posts one entry, visible on the journal screen
Derived from : AC-FIN-023  (REQ-FIN-023)
Exercises    : SCR-FIN-004 `/fin/recurring-templates` → SCR-FIN-006 `/fin/journal-entries`
Rule / code  : RULE-FIN-006 … RULE-FIN-009 re-checked at run time, surfaced generically
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an **active** recurring template whose next run date is today; an Open period; the
               caller holds the UPDATE affordance that Run follows
Steps        : 1. on `/fin/recurring-templates`, invoke Run on that template
               2. navigate to `/fin/journal-entries` and filter for the new row
Expected     : step 1 → exactly **one** entry is posted and the template's next run date advances
                  per its frequency. The run is a global-loading action, not a local one: it posts
                  a real entry, and both the template list and the journal list are invalidated.
               step 2 → the entry is listed with journal type `RECURRING` and status POSTED.
               Run-time failures (an account since deactivated, a period since closed) surface on
               the generic error surface of the template screen, the entry being server-built with
               no form field to bind a message to
Test data    : none invented; "today" is AC-FIN-023's own
<!-- TC:TC-FIN-071:END -->

<!-- TC:TC-FIN-072:START traces=AC-FIN-024,REQ-FIN-024,SCR-FIN-004,SCR-FIN-006 -->
### TC-FIN-072 — a reversing template's pair is visible across the two periods
Derived from : AC-FIN-024  (REQ-FIN-024)
Exercises    : SCR-FIN-004 `/fin/recurring-templates` → SCR-FIN-006 `/fin/journal-entries`
Rule / code  : RULE-FIN-011, RULE-FIN-012 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an active template whose schedule type is `REVERSING`; period N Open and period
               N+1 existing in the same year
Steps        : 1. invoke Run on the template
               2. on `/fin/journal-entries`, filter by period N, then by period N+1
Expected     : period N shows the template's own entry and period N+1 shows its **linked
               reversal**, equal amounts with opposite directions, each row showing the other's
               link. Opening either from the list reaches the same pair, so the relationship is
               navigable from the screen and not only implied by the amounts
Test data    : `REVERSING` — a seeded value; "period N / N+1" is AC-FIN-024's own wording
<!-- TC:TC-FIN-072:END -->

<!-- TC:TC-FIN-074:START traces=AC-FIN-026,REQ-FIN-026,SCR-FIN-005,SCR-FIN-006 -->
### TC-FIN-074 — an allocation run posts one entry that sums to the source balance
Derived from : AC-FIN-026  (REQ-FIN-026)
Exercises    : SCR-FIN-005 `/fin/allocation-rules` → SCR-FIN-006 `/fin/journal-entries`
Rule / code  : RULE-FIN-010 → success path, no code; RULE-FIN-003 governs the target set
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active allocation rule with two `PERCENTAGE` targets and one `REMAINDER` target,
               its source account balance 1,000.00; an Open period; the caller holds the UPDATE
               affordance that Run follows
Steps        : 1. on `/fin/allocation-rules`, invoke Run
               2. on `/fin/journal-entries`, open the created entry
Expected     : step 1 → the run is a global-loading action and invalidates both the rule list and
                  the journal list.
               step 2 → one entry with journal type `ALLOCATION` and status POSTED, whose target
                  lines sum **exactly** to 1,000.00, the remainder target carrying the rounding
                  difference. The screen displays the amounts as returned and computes no
                  distribution of its own
Test data    : 1,000.00 and the two-percentage-plus-one-remainder shape — AC-FIN-026's own
<!-- TC:TC-FIN-074:END -->

<!-- TC:TC-FIN-092:START traces=AC-FIN-046,REQ-FIN-046,SCR-FIN-010,SCR-FIN-009,SCR-FIN-008,SCR-FIN-006 -->
### TC-FIN-092 — a statement line is followed down to the entry behind it
Derived from : AC-FIN-046  (REQ-FIN-046)
Exercises    : SCR-FIN-010 `/fin/reports/balance-sheet` → SCR-FIN-009
               `/fin/reports/trial-balance` → SCR-FIN-008 `/fin/reports/account-ledger` →
               SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : — (read-only chain)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a balance-sheet line whose balance comes from an event-sourced entry; the caller's
               effective menu carries all four page codes
Steps        : 1. open the balance sheet and drill into a statement line
               2. from the trial-balance row reached, drill into the account
               3. from the ledger row reached, open the originating entry
Expected     : each hop lands on the next screen **already filtered** — the drill-in value is
               carried in the destination's route search parameters, so every intermediate step is
               linkable and the back navigation returns to the filtered view rather than a blank
               report. The final screen shows the entry with its lines and its event reference, or
               reads `manual`, `recurring` or `allocation` where the entry is not event-sourced.
               A user whose menu lacks one of the four page codes is redirected to unauthorized at
               that hop rather than shown an empty screen
Test data    : none invented; the chain is AC-FIN-046's own
<!-- TC:TC-FIN-092:END -->
<!-- SUB:INT-FLOW:END -->
<!-- PHASE:TEST-PLAN-FE:END -->

## TC TRACEABILITY INDEX

**AC → TC**

| AC | TC | AC | TC |
|---|---|---|---|
| AC-FIN-001 | TC-FIN-053 | AC-FIN-026 | TC-FIN-074 |
| AC-FIN-002 | TC-FIN-054 | AC-FIN-027 | TC-FIN-075 |
| AC-FIN-003 | TC-FIN-055 | AC-FIN-028 | TC-FIN-076 |
| AC-FIN-004 | TC-FIN-056 | AC-FIN-029 | TC-FIN-077 |
| AC-FIN-005 | TC-FIN-057 | AC-FIN-030 | TC-FIN-078 |
| AC-FIN-006 | TC-FIN-058 | AC-FIN-031 | TC-FIN-079 |
| AC-FIN-007 | TC-FIN-059 | AC-FIN-032 | TC-FIN-080 |
| AC-FIN-008 | TC-FIN-060 | AC-FIN-033 | TC-FIN-081 |
| AC-FIN-009 | TC-FIN-061 | AC-FIN-034 | TC-FIN-082 |
| AC-FIN-014 | TC-FIN-062 | AC-FIN-035 | TC-FIN-083 |
| AC-FIN-015 | TC-FIN-063 | AC-FIN-036 | TC-FIN-084 |
| AC-FIN-016 | TC-FIN-064 | AC-FIN-037 | TC-FIN-085 |
| AC-FIN-017 | TC-FIN-065 | AC-FIN-038 | TC-FIN-086 |
| AC-FIN-018 | TC-FIN-066 | AC-FIN-039 | TC-FIN-087 |
| AC-FIN-019 | TC-FIN-067 | AC-FIN-040 | TC-FIN-088 |
| AC-FIN-020 | TC-FIN-068 | AC-FIN-041 | TC-FIN-089 |
| AC-FIN-021 | TC-FIN-069 | AC-FIN-042 | TC-FIN-090 |
| AC-FIN-022 | TC-FIN-070 | AC-FIN-043 | TC-FIN-091 |
| AC-FIN-023 | TC-FIN-071 | AC-FIN-046 | TC-FIN-092 |
| AC-FIN-024 | TC-FIN-072 | AC-FIN-010 … 013, 044, 045 | out of track — see the header table |
| AC-FIN-025 | TC-FIN-073 | — | — |

**REQ → TC** — each `REQ-FIN-<n>` carries the TC of its own `AC-FIN-<n>`, plus:

| REQ | TC |
|---|---|
| REQ-FIN-017 | TC-FIN-065 (the locked, posted surface) · TC-FIN-062, TC-FIN-071, TC-FIN-074 (the same unified path seen from three screens) |
| REQ-FIN-019 | TC-FIN-067 · TC-FIN-055 (the same refusal reached through the chart screen) |
| REQ-FIN-021 | TC-FIN-069 (which needs SCR-FIN-002's deactivate as its only setup) |
| REQ-FIN-036 | TC-FIN-084 · TC-FIN-089, TC-FIN-090 (the two reports that read its result) |
| REQ-FIN-046 | TC-FIN-092 (the chain) · TC-FIN-087, TC-FIN-088, TC-FIN-089 (each hop's own screen) |

**SCR → TC**

| SCR | TC |
|---|---|
| SCR-FIN-001 | TC-FIN-053, TC-FIN-054, TC-FIN-055 |
| SCR-FIN-002 | TC-FIN-056, TC-FIN-057, TC-FIN-058, TC-FIN-069 (setup) |
| SCR-FIN-003 | TC-FIN-059, TC-FIN-060, TC-FIN-061 |
| SCR-FIN-004 | TC-FIN-070, TC-FIN-071, TC-FIN-072 |
| SCR-FIN-005 | TC-FIN-073, TC-FIN-074 |
| SCR-FIN-006 | TC-FIN-062, TC-FIN-063, TC-FIN-064, TC-FIN-065, TC-FIN-066, TC-FIN-067, TC-FIN-068, TC-FIN-069, TC-FIN-075, TC-FIN-076, TC-FIN-077, TC-FIN-078, TC-FIN-055, TC-FIN-071, TC-FIN-072, TC-FIN-074, TC-FIN-092 |
| SCR-FIN-007 | TC-FIN-079, TC-FIN-080, TC-FIN-081, TC-FIN-082, TC-FIN-083, TC-FIN-084, TC-FIN-085, TC-FIN-086, TC-FIN-068 (setup) |
| SCR-FIN-008 | TC-FIN-087, TC-FIN-092 |
| SCR-FIN-009 | TC-FIN-088, TC-FIN-092 |
| SCR-FIN-010 | TC-FIN-089, TC-FIN-092 |
| SCR-FIN-011 | TC-FIN-090 |
| SCR-FIN-012 | TC-FIN-091 |

**RULE / catalog code → TC**

| RULE | code | TC |
|---|---|---|
| RULE-FIN-001 | FIN-409-HAS-CHILDREN (409) | TC-FIN-054 |
| RULE-FIN-002 | FIN-409-DIMVALUE-DUP (409) | TC-FIN-058 |
| RULE-FIN-003 | FIN-409-REMAINDER-COUNT (409) | TC-FIN-061, TC-FIN-073 |
| RULE-FIN-006 | FIN-409-UNBALANCED (409) | TC-FIN-063, TC-FIN-066 |
| RULE-FIN-007 | FIN-409-NOT-POSTABLE-ACCOUNT (409) | TC-FIN-055, TC-FIN-063, TC-FIN-067 |
| RULE-FIN-008 | FIN-409-PERIOD-NOT-OPEN (409) | TC-FIN-068 |
| RULE-FIN-009 | FIN-409-INVALID-DIMENSION (409) | TC-FIN-069 |
| RULE-FIN-010 | — (success path) | TC-FIN-074 |
| RULE-FIN-011, RULE-FIN-012 | — (success path) | TC-FIN-072, TC-FIN-076, TC-FIN-077 |
| RULE-FIN-013 | FIN-409-ALREADY-REVERSED (409) | TC-FIN-078 |
| RULE-FIN-014 | FIN-409-NOT-REOPENABLE (409) | TC-FIN-083 |
| RULE-FIN-015 | FIN-403-FORBIDDEN (403) | TC-FIN-086 |
| RULE-FIN-016 | — (enforced by omission; on screen, by the absent affordance) | TC-FIN-064, TC-FIN-065 |
| RULE-FIN-017 | — (pre-empted by the year-filtered period select; the 400s are the server's) | TC-FIN-062 |
| — (not a rule) | FIN-409-INVALID-TRANSITION (409) | TC-FIN-081 |
| — (not a rule) | FIN-409-PERIODS-NOT-CLOSED (409) | TC-FIN-084 |
| — (not a rule) | FIN-400-MISSING-FREQUENCY, pre-empted on the form | TC-FIN-070 |

**Catalog rows no TC on this track exercises**, stated rather than left unsaid: the uniqueness
rows FIN-409-ACCOUNT-DUP, FIN-409-DIMENSION-DUP, FIN-409-RULE-DUP and FIN-409-YEAR-DUP, the
lookup and sort validation rows, FIN-422-MAPPING-UNSUPPORTED, FIN-422-REMAINDER-MARKER,
FIN-422-REMAINDER-NOT-POSITIVE, FIN-422-INVALID-PERCENTAGE-VALUE, FIN-409-NOT-ACTIVE, the
`FIN-404-*` not-found rows, FIN-409-NOT-POSTED, the two RULE-FIN-017 400s and FIN-500. No
`AC-FIN-*` states any of them; fabricating a screen case for a code no acceptance criterion
asserts is the over-engineering §3 rule 6 forbids, and ADR-FIN-009 records the boundary.
`FIN-403-SOD-VIOLATION` is not among them and must not be tested: it is struck as unreachable,
and no client-side separation-of-duty check exists to assert.

## COVERAGE

```
AC  covered   40/40   ✓ 0 gaps on this track's denominator — the ACs a screen can exercise.
                      The 6 outside it (AC-FIN-010…013, 044, 045) are not gaps: each is covered
                      on the backend track, and the header table names the TC that covers it.
                      Module-wide the figure is 46/46.
REQ covered   40/40   ✓ 0 gaps on the same denominator (REQ-FIN-010…013, 044, 045 excluded)
SCR covered   12/12   ✓ 0 gaps — every SCR-FIN-001…012 is named by ≥1 TC's Exercises line
TC count      40      ✓ 1.00× the in-track AC count — well under the ~2× over-engineering guard
Integration   n/a     scope = module: no INT-UXD phase is emitted and none is owed (§2 rule 3).
                      UXD-FIN-001 … UXD-FIN-012 are cited as the option lists a control loads,
                      never asserted as cross-module scenarios — every one of them is owned by
                      MDL, which is not in this run's selection
```
══════════════════════════════════════════════════════════════════
