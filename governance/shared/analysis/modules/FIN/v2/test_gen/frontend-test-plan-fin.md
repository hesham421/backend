# FRONTEND TEST PLAN — الحسابات العامة / Finance (General Ledger) (FIN)
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v2 (delta on v1 — change set CS-FIN-001)   Profile : erp   Track : frontend   Plan : test
Scope  : **module** — modules FIN (`gov.py run-standalone test-gen --module FIN`)
Sources: `_state/current-srs.md` (v2) · `_state/current-frontend-execution-plan.md` (v2) ·
         `_state/current-backend-execution-plan.md` (v2) · `_state/current-registry-srs.md` (v2) ·
         `_state/current-registry-exec-fe.md` (v2) · baseline `_state/current-frontend-test-plan.md` (v1)
Framework : **agnostic** (`profile.stack.testing`). Every block below is the framework-agnostic
            TC form; no tool, annotation, fixture, selector or file layout is named anywhere in
            this plan. The consumer repo chooses its framework and turns each TC into a test.
REDUCED   : no — the frontend-execution-plan is present, so every step binds to a real `SCR-*`
            and its declared route, the v2 screen SCR-FIN-013 included. Since ADR-FIN-064 every
            call a case makes is a published endpoint: the account-mapping calls and the v2
            members of the add-rule-line call no longer wait on publication.
TC ids    : v1 TC-FIN-053 … TC-FIN-092 on this track; v2 continues the one shared module
            sequence at TC-FIN-130 … TC-FIN-145, after `backend-test-plan-fin.md`'s
            TC-FIN-093 … TC-FIN-129. No id is reused, none is renumbered, and no id is reassigned
            to a different subject than the one it carried.
Open ADRs : 3 raised by test-gen v2 — ADR-FIN-063 (first run), ADR-FIN-066 (which realigns the
            cases with the revised frontend plan) and ADR-FIN-068 (this run: host catalogue values
            created by the case that needs them, distinct values where cases need opposite
            states), all ACCEPTED, non-breaking. The refusal texts the cases quote are bound in
            the frontend plan since ADR-FIN-067.
            Applied and not re-derived: ADR-FIN-009 (v1 test-gen), ADR-FIN-001 … ADR-FIN-008,
            ADR-FIN-024, ADR-FIN-025, ADR-FIN-039, ADR-FIN-061, ADR-FIN-062, ADR-FIN-064,
            ADR-FIN-065. The first run cited ADR-FIN-060 as BLOCKED; it is superseded by
            ADR-FIN-064.
══════════════════════════════════════════════════════════════════

**Delta v2 — what this file is.** A complete current plan, not a fragment. The reason is the one
the backend plan states: a marker plan folds by phase, so a SUB re-emitted with its v2 atoms
alone would lose its v1 atoms (ADR-FIN-063). Every v1 case whose content v2 leaves alone is
restated byte-identical.

- **ADDED** — sixteen cases, TC-FIN-130 … TC-FIN-145, one per v2 acceptance criterion a screen
  can exercise.
- **MODIFIED** — the v1 cases the v2 frontend plan made stale. The first v2 run changed four:
  the engine-rules cases TC-FIN-059, TC-FIN-060 and TC-FIN-061 for the one-line-per-submit form
  (ADR-FIN-062), and the fiscal-year case TC-FIN-079 for the fiscal-year search (ADR-FIN-061).
  This run (ADR-FIN-066) realigns the plan with the frontend plan as ADR-FIN-064 and
  ADR-FIN-065 revised it:
  - record pages: the five screens that own a child collection are now a list, a `/new` page
    and a `/:id` record page, so every case on SCR-FIN-002, 003, 004, 005 and 007 names its
    new route;
  - no client-side action gating: every affordance renders for a holder of the screen and the
    server's 403 decides, so each "holds the … affordance" precondition becomes the permission
    the server checks. The cases on hard-close, year-end close and the entry creator's close
    (TC-FIN-082, TC-FIN-084, TC-FIN-086) asserted a gate that no longer exists and are
    re-derived;
  - journal-entry line refusals: the published error body names no line, so the refusals of
    TC-FIN-055, TC-FIN-063, TC-FIN-067 and TC-FIN-069 are asserted in the lines pane's single
    refusal slot, never against a line;
  - the income statement sends its two period bounds independently (TC-FIN-090), and the
    balance sheet drills down from a group by its account type (TC-FIN-092).

  Unchanged in content: TC-FIN-065, TC-FIN-066, TC-FIN-075, TC-FIN-077, TC-FIN-078,
  TC-FIN-087, TC-FIN-088, TC-FIN-089 and TC-FIN-091. TC-FIN-091's record still runs on to the
  INT-FLOW START marker, whose `traces=` grew in the first v2 run, so it stays listed MODIFIED.
- **REMOVED** — none.

The change manifest lists **all 40** re-emitted v1 cases as MODIFIED, for the reason the backend
plan gives: the version check compares a marker atom's START line with the baseline's heading
record, so it reads a byte-identical case as re-defined. The cases whose content changed are the
ones named above; the nine listed as unchanged are word for word v1.

**Id order is AC order within this track.** In v1, TC-FIN-053 is AC-FIN-001's frontend case and
TC-FIN-092 is AC-FIN-046's. In v2, TC-FIN-130 … TC-FIN-145 follow the in-track v2 criteria in AC
order. The v2 cases **open** each SUB and the v1 cases follow, for the same record-text reason
the backend plan gives. The traceability index is the address.

**Twenty-seven ACs are out of this track, and none of them is a gap.** Every `AC-FIN-*` carries ≥1
TC across the module. This track's denominator is the ACs a screen can exercise: 40 of the 46 in
v1, and 16 of the 37 added in v2, so 56 of 83.

| AC | why it is not a frontend case | covered on the backend track by |
|---|---|---|
| AC-FIN-010, AC-FIN-011, AC-FIN-012, AC-FIN-013 | their When is "a canonical event arrives". API-FIN-020 is bound but drawn on no screen and constructed by no form — ADR-FIN-007, restated in F1-SCR-FIN-006 | TC-FIN-010, TC-FIN-011, TC-FIN-012, TC-FIN-013 |
| AC-FIN-044, AC-FIN-045 | onboarding, no screen — the P0.5 traceability matrix itself says so ("— (onboarding, no screen)"). AC-FIN-045 is MODIFIED in v2 and stays onboarding | TC-FIN-044, TC-FIN-045 |
| AC-FIN-047, AC-FIN-050 … AC-FIN-057, AC-FIN-067, AC-FIN-068, AC-FIN-071 … AC-FIN-074, AC-FIN-077 … AC-FIN-080, AC-FIN-082 (v2) | the same event path: the account a mapping resolves, the dimension a tag resolves, and every event rejection happen inside API-FIN-020, which no screen calls (ADR-FIN-007) | TC-FIN-093, TC-FIN-096 … TC-FIN-103, TC-FIN-113, TC-FIN-114, TC-FIN-117 … TC-FIN-120, TC-FIN-123 … TC-FIN-126, TC-FIN-128 |
| AC-FIN-081 (v2) | it asks what the published error catalogue says. That is a document the backend publishes, not a screen | TC-FIN-127 |

Entries built from an event are still **seen and reversed** on SCR-FIN-006 like any other source
(TC-FIN-143, TC-FIN-145). What no screen does is construct one. ADR-FIN-009 and ADR-FIN-063 record
this denominator, so that no later reader takes 56/56 for a silent narrowing of 83.

**What this track asserts and what it leaves to the server.** Where the frontend plan declares a
client-side pre-empt of a rule (F3), the TC asserts the pre-empt **and** the server's own refusal:
the pre-empt is a convenience, and the server stays authoritative. RULE-FIN-022 is the one v2
rule the plan explicitly does **not** pre-empt, and TC-FIN-137 asserts that too. Where the plan
routes an error to a generic surface rather than a field, the TC says generic and does not invent
a field binding. No UI copy is invented anywhere: the only strings asserted are the SRS's own
rule messages and the Error Catalog's own rows, in both languages. The deactivate confirmation on
SCR-FIN-013 has no stated text, so none is asserted. No case asserts an affordance hidden or
disabled for want of a grant: the plan gates none on the client, every affordance renders for a
holder of the screen, and the server's 403 is the control (ADR-FIN-065).

<!-- PHASE:TEST-PLAN-FE:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-003,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-014,REQ-FIN-015,REQ-FIN-016,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-022,REQ-FIN-023,REQ-FIN-024,REQ-FIN-025,REQ-FIN-026,REQ-FIN-027,REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,REQ-FIN-031,REQ-FIN-032,REQ-FIN-033,REQ-FIN-034,REQ-FIN-035,REQ-FIN-036,REQ-FIN-037,REQ-FIN-038,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-046,REQ-FIN-048,REQ-FIN-049,REQ-FIN-054,REQ-FIN-055,REQ-FIN-056,REQ-FIN-057,REQ-FIN-058,REQ-FIN-059,REQ-FIN-060,REQ-FIN-061,REQ-FIN-062,REQ-FIN-064,REQ-FIN-065,REQ-FIN-070,REQ-FIN-071,REQ-FIN-077,AC-FIN-001,AC-FIN-002,AC-FIN-003,AC-FIN-004,AC-FIN-005,AC-FIN-006,AC-FIN-007,AC-FIN-008,AC-FIN-009,AC-FIN-014,AC-FIN-015,AC-FIN-016,AC-FIN-017,AC-FIN-018,AC-FIN-019,AC-FIN-020,AC-FIN-021,AC-FIN-022,AC-FIN-023,AC-FIN-024,AC-FIN-025,AC-FIN-026,AC-FIN-027,AC-FIN-028,AC-FIN-029,AC-FIN-030,AC-FIN-031,AC-FIN-032,AC-FIN-033,AC-FIN-034,AC-FIN-035,AC-FIN-036,AC-FIN-037,AC-FIN-038,AC-FIN-039,AC-FIN-040,AC-FIN-041,AC-FIN-042,AC-FIN-043,AC-FIN-046,AC-FIN-048,AC-FIN-049,AC-FIN-058,AC-FIN-059,AC-FIN-060,AC-FIN-061,AC-FIN-062,AC-FIN-063,AC-FIN-064,AC-FIN-065,AC-FIN-066,AC-FIN-069,AC-FIN-070,AC-FIN-075,AC-FIN-076,AC-FIN-083 -->
## PHASE — TEST-PLAN-FE

TC count is 40, above the split threshold of 8, so this phase is split into the two groups the
engine names: per-screen flows, and the flows that only exist across screens.

<!-- SUB:UI-FLOWS:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-014,REQ-FIN-015,REQ-FIN-016,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-022,REQ-FIN-025,REQ-FIN-027,REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,REQ-FIN-031,REQ-FIN-032,REQ-FIN-033,REQ-FIN-034,REQ-FIN-035,REQ-FIN-036,REQ-FIN-037,REQ-FIN-038,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-048,REQ-FIN-049,REQ-FIN-054,REQ-FIN-055,REQ-FIN-056,REQ-FIN-057,REQ-FIN-059,REQ-FIN-060,REQ-FIN-061,REQ-FIN-062,REQ-FIN-064,REQ-FIN-065,REQ-FIN-070,REQ-FIN-071,REQ-FIN-077,AC-FIN-001,AC-FIN-002,AC-FIN-004,AC-FIN-005,AC-FIN-006,AC-FIN-007,AC-FIN-008,AC-FIN-009,AC-FIN-014,AC-FIN-015,AC-FIN-016,AC-FIN-017,AC-FIN-018,AC-FIN-019,AC-FIN-020,AC-FIN-021,AC-FIN-022,AC-FIN-025,AC-FIN-027,AC-FIN-028,AC-FIN-029,AC-FIN-030,AC-FIN-031,AC-FIN-032,AC-FIN-033,AC-FIN-034,AC-FIN-035,AC-FIN-036,AC-FIN-037,AC-FIN-038,AC-FIN-039,AC-FIN-040,AC-FIN-041,AC-FIN-042,AC-FIN-043,AC-FIN-048,AC-FIN-049,AC-FIN-058,AC-FIN-059,AC-FIN-060,AC-FIN-061,AC-FIN-063,AC-FIN-064,AC-FIN-065,AC-FIN-066,AC-FIN-069,AC-FIN-070,AC-FIN-075,AC-FIN-076,AC-FIN-083 -->
### SUB — UI-FLOWS

One screen, one flow: search, create, the violation shown on the screen that caused it, the
state a row moves to, and the refusal the server gives a caller who lacks the grant.

**v2 cases open the SUB** (TC-FIN-130 … TC-FIN-145, the screen half of CS-FIN-001), and the v1
cases follow. The account-mapping screen SCR-FIN-013 and the v2 half of the engine-rules Line
entry on SCR-FIN-003 call endpoints the republished api-docs carry: the four account-mapping
endpoints API-FIN-038 … API-FIN-041, the v2 members of API-FIN-011, and all twelve v2
catalogue codes (ADR-FIN-064). No case waits on publication any longer. The messages asserted
are the SRS's own rule messages, in both languages.

<!-- TC:TC-FIN-130:START traces=AC-FIN-048,REQ-FIN-048,SCR-FIN-003 -->
### TC-FIN-130 — the Line entry never offers DIRECT, and the server refuses it anyway
Derived from : AC-FIN-048  (REQ-FIN-048)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the Line entry Drawer, `?lineAction=create`
Rule / code  : RULE-FIN-018 → FIN-422-UNSUPPORTED-DERIVATION (422), shown inline on the derivation
               type
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: `FIN_RULES` is in the caller's effective menu and the caller's role holds
               `PERM_FIN_RULES_UPDATE`, which the server checks; a rule's record page is open; MDL
               holds CONSTANT and MAPPING as the only active `ACCOUNT_DERIVATION_TYPE` values
               (UXD-FIN-008)
Steps        : 1. open the Line entry and open the account-derivation select
               2. submit a line whose derivation type is DIRECT, bypassing the select
Expected     : step 1 → the select offers exactly the list MDL returns, CONSTANT and MAPPING;
                  DIRECT is not offered and no value is hardcoded in the screen.
               step 2 → the server's refusal is shown inline on the derivation type —
                  ar: «نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط» ·
                  en: "Unsupported account derivation type; only constant or mapping is allowed".
                  No line is added to the rule
Test data    : DIRECT — AC-FIN-048's own value
<!-- TC:TC-FIN-130:END -->

<!-- TC:TC-FIN-131:START traces=AC-FIN-049,REQ-FIN-049,SCR-FIN-003 -->
### TC-FIN-131 — a MAPPING line is entered with a business field and no account
Derived from : AC-FIN-049  (REQ-FIN-049)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the Line entry Drawer, `?lineAction=create`
Rule / code  : RULE-FIN-019 → pre-empted on the form (the derivation spec matches its type)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the record page of an active rule for "EXPENSE_PAID" is open; the
               `FIN_EVENT_BUSINESS_FIELD` option list has loaded (UXD-FIN-013); the caller's role
               holds `PERM_FIN_RULES_UPDATE`
Host data    : ACCOUNTING_EVENT_TYPE EXPENSE_PAID (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. open the Line entry and choose derivation type MAPPING
               2. observe the account and business-field controls
               3. choose business field EXPENSE_TYPE_CODE, fill the amount-source, direction and
                  distribution fields, and save
Expected     : step 2 → the business-field control becomes required and the constant-account
                  control empty and not required: a MAPPING line carries a business field and no
                  account code, the RULE-FIN-019 pre-empt.
               step 3 → **one** call adds the line, which appears among the rule's lines with
                  business field EXPENSE_TYPE_CODE shown by its label in the caller's language
Test data    : EXPENSE_PAID and EXPENSE_TYPE_CODE — AC-FIN-049's own values
<!-- TC:TC-FIN-131:END -->

<!-- TC:TC-FIN-132:START traces=AC-FIN-058,REQ-FIN-054,SCR-FIN-013 -->
### TC-FIN-132 — an account mapping is created from the account-mappings screen
Derived from : AC-FIN-058  (REQ-FIN-054)
Exercises    : SCR-FIN-013 `/fin/account-mappings` — the Entry, `?entry=new`
Rule / code  : — (happy path; RULE-FIN-023 and RULE-FIN-024 are pre-empted by the account picker
               and the payment-method select)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_ACCOUNT_MAPPINGS` is in the caller's effective menu, and the caller's role
               holds `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`, which the server checks; the
               three option lists have loaded (UXD-FIN-007, UXD-FIN-013, UXD-FIN-015); no active
               mapping holds (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE"); account 1102 is an
               active leaf
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. navigate to `/fin/account-mappings` and open a new entry
               2. choose event type PAYMENT_RECEIVED and business field PAYMENT_METHOD, and
                  observe the business-value control
               3. choose CHEQUE; open the account picker (`?picker=account`) and pick 1102
               4. save
Expected     : step 2 → while the business field is PAYMENT_METHOD, the business value is a
                  select over the `PAYMENT_METHOD` option list, not free text.
               step 3 → the picker is its own view, a sibling of the Entry, offering active leaf
                  accounts only; it returns 1102 to the Entry's form.
               step 4 → **one** call creates the mapping, and the list refreshes to show it
                  active with the account's code and its name in the caller's language
Test data    : PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE" and 1102 — AC-FIN-058's own values
<!-- TC:TC-FIN-132:END -->

<!-- TC:TC-FIN-133:START traces=AC-FIN-059,REQ-FIN-055,SCR-FIN-013 -->
### TC-FIN-133 — filtering the mapping list returns exactly the matching rows
Derived from : AC-FIN-059  (REQ-FIN-055)
Exercises    : SCR-FIN-013 `/fin/account-mappings`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: 3 mappings for PAYMENT_RECEIVED (CASH active, CHEQUE active, TRANSFER_133 inactive)
               and 2 for EXPENSE_PAID; `FIN_ACCOUNT_MAPPINGS` is in the caller's effective menu
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) · ACCOUNTING_EVENT_TYPE EXPENSE_PAID
               (active) · PAYMENT_METHOD CASH (active) · PAYMENT_METHOD TRANSFER_133 (active; its
               mapping is saved, then deactivated) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. filter by event type PAYMENT_RECEIVED and active only
               2. copy the resulting URL, open it fresh, and compare
Expected     : step 1 → exactly 2 rows, CASH and CHEQUE, each showing its account's code and name.
               step 2 → the same two rows: the filters live in the route's search parameters and
                  the page and page size are part of the filter that keys the list, so paging
                  re-queries the server
Test data    : the 3 + 2 mappings and the filter pair — AC-FIN-059's own, the inactive row's payment
               method renamed for this case (ADR-FIN-068)
<!-- TC:TC-FIN-133:END -->

<!-- TC:TC-FIN-134:START traces=AC-FIN-060,REQ-FIN-056,SCR-FIN-013 -->
### TC-FIN-134 — only the account of an existing mapping can be changed
Derived from : AC-FIN-060  (REQ-FIN-056)
Exercises    : SCR-FIN-013 `/fin/account-mappings` — the Entry, `?entry=<row id>`
Rule / code  : RULE-FIN-023 → pre-empted by the account picker
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the mapping (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT") → account 5220; account 5225
               is an active leaf; the caller's role holds `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`
Host data    : ACCOUNTING_EVENT_TYPE EXPENSE_PAID (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. open that mapping's entry from its row
               2. observe the event type, business field and business value controls
               3. pick 5225 in the account picker and save
Expected     : step 2 → the three key fields render **read-only**.
               step 3 → one call sends the account alone, never the fixed key; the row then
                  shows account 5225 with its event type, business field and business value
                  unchanged
Test data    : 5220, 5225 and "RENT" — AC-FIN-060's own values
<!-- TC:TC-FIN-134:END -->

<!-- TC:TC-FIN-135:START traces=AC-FIN-061,REQ-FIN-057,SCR-FIN-013 -->
### TC-FIN-135 — a deactivated mapping leaves the active list
Derived from : AC-FIN-061  (REQ-FIN-057)
Exercises    : SCR-FIN-013 `/fin/account-mappings`
Rule / code  : — (state change; the event half of AC-FIN-061 is asserted by TC-FIN-107, no
               screen building an event)
Scenario     : STATE · data class VALID · language ALL
Preconditions: an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH") → account 1101; the
               caller's role holds `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) · PAYMENT_METHOD CASH (active) — each
               created through API-MDL-006 `POST /api/v1/mdl/lookup-types/{id}/values` on its key's type
               (SRS A6: host data, never a product seed — ADR-FIN-068)
Steps        : 1. invoke Deactivate on that row and confirm
               2. filter the list by active only, then by inactive only
Expected     : step 1 → Deactivate is a confirmed row action with its own single call, never a
                  field of the Entry; the row reads inactive. There is **no Delete affordance**
                  anywhere on the screen: a mapping is only ever deactivated.
               step 2 → the mapping is gone from the active results and present in the inactive
                  ones
Test data    : "CASH" and 1101 — AC-FIN-061's own values
<!-- TC:TC-FIN-135:END -->

<!-- TC:TC-FIN-137:START traces=AC-FIN-063,REQ-FIN-059,SCR-FIN-013 -->
### TC-FIN-137 — a duplicate active mapping is refused on the key fields
Derived from : AC-FIN-063  (REQ-FIN-059)
Exercises    : SCR-FIN-013 `/fin/account-mappings` — the Entry, `?entry=new`
Rule / code  : RULE-FIN-022 → FIN-409-MAPPING-DUP (409), shown inline on the three key fields
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102;
               account 1103 is an active leaf; the caller's role holds
               `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. create a second mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → 1103 and
                  save
Expected     : the form sends the request — RULE-FIN-022 is **not** pre-empted client-side, the
               loaded page possibly not holding the conflicting row — and the server's refusal is
               shown inline on the event type, business field and business value —
               ar: «يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها» ·
               en: "An active mapping already exists for this event type, business field and
               value".
               The list still shows exactly one active mapping for that key
Test data    : 1102, 1103 and "CHEQUE" — AC-FIN-063's own values
<!-- TC:TC-FIN-137:END -->

<!-- TC:TC-FIN-138:START traces=AC-FIN-064,REQ-FIN-060,SCR-FIN-013 -->
### TC-FIN-138 — a parent account is neither offered nor accepted for a mapping
Derived from : AC-FIN-064  (REQ-FIN-060)
Exercises    : SCR-FIN-013 `/fin/account-mappings` — the account picker, `?picker=account`
Rule / code  : RULE-FIN-023 → FIN-422-MAPPED-ACCOUNT-INELIGIBLE (422), shown inline on the account
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: account 1100 is a parent (rollup) account with `isLeafFl` false; the caller's role
               holds `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`
Steps        : 1. open a new entry and open the account picker, searching for 1100
               2. submit a mapping whose account is 1100, bypassing the picker
Expected     : step 1 → 1100 is not offered: the picker lists active leaf accounts only.
               step 2 → the server's refusal is shown inline on the account field —
                  ar: «الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا» ·
                  en: "The mapped account must be an active leaf account" —
                  and no row is added to the list
Test data    : 1100 — AC-FIN-064's own value; the key is a placeholder
<!-- TC:TC-FIN-138:END -->

<!-- TC:TC-FIN-139:START traces=AC-FIN-065,REQ-FIN-061,SCR-FIN-013 -->
### TC-FIN-139 — a misspelt payment method cannot be entered, and is refused if sent
Derived from : AC-FIN-065  (REQ-FIN-061)
Exercises    : SCR-FIN-013 `/fin/account-mappings` — the Entry, `?entry=new`
Rule / code  : RULE-FIN-024 → FIN-422-INVALID-PAYMENT-METHOD (422), shown inline on the business
               value
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: PAYMENT_METHOD includes CASH and CHEQUE as active values (UXD-FIN-015); the caller's
               role holds `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`
Host data    : ACCOUNTING_EVENT_TYPE PAYMENT_RECEIVED (active) · PAYMENT_METHOD CASH (active) — each
               created through API-MDL-006 `POST /api/v1/mdl/lookup-types/{id}/values` on its key's type
               (SRS A6: host data, never a product seed — ADR-FIN-068)
Steps        : 1. choose business field PAYMENT_METHOD and try to type "CHEQEU" as the value
               2. submit (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQEU"), bypassing the select
Expected     : step 1 → the value is chosen from a select of the active PAYMENT_METHOD values MDL
                  returns, CASH and CHEQUE among them, and "CHEQEU" is not one; free text is not
                  accepted.
               step 2 → the server's refusal is shown inline on the business value —
                  ar: «القيمة ليست طريقة دفع معرَّفة» ·
                  en: "The value is not a defined payment method" —
                  and no row is added
Test data    : "CHEQEU" — AC-FIN-065's own misspelling
<!-- TC:TC-FIN-139:END -->

<!-- TC:TC-FIN-140:START traces=AC-FIN-066,REQ-FIN-062,SCR-FIN-003 -->
### TC-FIN-140 — a rule line is saved with its dimension tags in one submit
Derived from : AC-FIN-066  (REQ-FIN-062)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the Line entry `?lineAction=create` and its tag
               editor `?picker=tags`
Rule / code  : RULE-FIN-025, RULE-FIN-026 → pre-empted in the tag editor
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: dimensions ORGANISATION (value "ORG-1") and BRANCH, both active; the
               `FIN_DIMENSION_VALUE_SOURCE` and `FIN_EVENT_BUSINESS_FIELD` option lists have
               loaded (UXD-FIN-014, UXD-FIN-013); a rule's record page is open; the caller's role
               holds `PERM_FIN_RULES_UPDATE`
Steps        : 1. fill a line, then open its tag editor from the tags summary row
               2. add {ORGANISATION: CONSTANT "ORG-1"} — picking the value in the dimension-value
                  picker — and {BRANCH: BUSINESS_FIELD BRANCH_CODE}, and return to the line
               3. save the line
Expected     : step 2 → the tag editor is its own view, a sibling of the Line entry, and returns
                  the tag list to it; the summary row reads two tags. A CONSTANT tag asks for a
                  value and no business field, a BUSINESS_FIELD tag the reverse.
               step 3 → **one** call carries the line together with both tags, never a line
                  save followed by a tag save. The line appears among the rule's lines with
                  its two tags
Test data    : ORGANISATION, "ORG-1", BRANCH and BRANCH_CODE — AC-FIN-066's own values
<!-- TC:TC-FIN-140:END -->

<!-- TC:TC-FIN-141:START traces=AC-FIN-069,REQ-FIN-064,SCR-FIN-003 -->
### TC-FIN-141 — a second tag for the same dimension is stopped in the tag editor
Derived from : AC-FIN-069  (REQ-FIN-064)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the tag editor, `?picker=tags`
Rule / code  : RULE-FIN-025 → FIN-422-DIMENSION-TAG-DUP (422), shown inline on the tags summary row
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a rule's record page is open; BRANCH holds "BR-01"; the caller's role holds
               `PERM_FIN_RULES_UPDATE`
Steps        : 1. in the tag editor add {BRANCH: CONSTANT "BR-01"}, then a second tag on BRANCH
               2. submit the line with both tags, bypassing the editor's check
Expected     : step 1 → the editor refuses the second BRANCH tag: the dimension is unique across
                  a line's tags, the RULE-FIN-025 pre-empt.
               step 2 → the server's refusal is shown inline on the tags summary row —
                  ar: «لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة» ·
                  en: "A rule line cannot tag the same dimension twice" —
                  and no line is added
Test data    : BRANCH, "BR-01" and BRANCH_CODE — AC-FIN-069's own values
<!-- TC:TC-FIN-141:END -->

<!-- TC:TC-FIN-142:START traces=AC-FIN-070,REQ-FIN-065,SCR-FIN-003 -->
### TC-FIN-142 — a CONSTANT tag can only take a value of its own dimension
Derived from : AC-FIN-070  (REQ-FIN-065)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the tag editor and its dimension-value picker
               `?picker=dimension-value`
Rule / code  : RULE-FIN-026 → FIN-422-DIMENSION-TAG-SOURCE (422), shown inline on the tags summary
               row
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: dimensions BRANCH and ORGANISATION, "ORG-1" being a value of ORGANISATION; the
               caller's role holds `PERM_FIN_RULES_UPDATE`; a rule's record page is open
Steps        : 1. add a CONSTANT tag on BRANCH and open its value picker, looking for "ORG-1"
               2. submit the line tagged {BRANCH: CONSTANT "ORG-1"}, bypassing the picker
Expected     : step 1 → "ORG-1" is not offered: the picker is scoped to the tag's own dimension
                  and to active values.
               step 2 → the server's refusal is shown inline on the tags summary row —
                  ar: «وسم البُعد لا يطابق مصدر قيمته» ·
                  en: "The dimension tag does not match its value source" —
                  and no line is added
Test data    : BRANCH, ORGANISATION and "ORG-1" — AC-FIN-070's own values
<!-- TC:TC-FIN-142:END -->

<!-- TC:TC-FIN-143:START traces=AC-FIN-075,REQ-FIN-070,SCR-FIN-006 -->
### TC-FIN-143 — an event-built entry's dimensions display exactly as a manual entry's
Derived from : AC-FIN-075  (REQ-FIN-070)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: entry A posted from event `EVT-3016` with BRANCH "BR-07" on its debit line, and
               manual entry B posted with BRANCH "BR-07" on its debit line; the caller holds
               `FIN_JOURNAL_ENTRIES` VIEW
Steps        : 1. open entry A's read route
               2. open entry B's read route
Expected     : both debit lines show exactly one dimension, BRANCH, with the value "BR-07" by its
               name and code, drawn by the same line component. The entries differ on screen only
               in their journal type — EVENT_GENERATED against MANUAL, labelled from UXD-FIN-005 —
               and in A's event reference
Test data    : `EVT-3016` and "BR-07" — AC-FIN-075's own values
<!-- TC:TC-FIN-143:END -->

<!-- TC:TC-FIN-144:START traces=AC-FIN-076,REQ-FIN-071,SCR-FIN-012 -->
### TC-FIN-144 — the dimension report screen shows one row for both entries
Derived from : AC-FIN-076  (REQ-FIN-071)
Exercises    : SCR-FIN-012 `/fin/reports/dimension`
Rule / code  : — (read-only)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the two entries of AC-FIN-075, each debiting account 5100 with 100.00 under
               BRANCH "BR-07"; the caller holds `FIN_DIMENSION_REPORTS`
Steps        : 1. run the report by the BRANCH dimension
Expected     : one row for (5100, "BR-07") with a debit total of 200.00. The screen applies no
               journal-type filter, so event-built and manual lines are counted together
Test data    : 5100, 100.00, 200.00 and "BR-07" — AC-FIN-076's own values
<!-- TC:TC-FIN-144:END -->

<!-- TC:TC-FIN-145:START traces=AC-FIN-083,REQ-FIN-077,SCR-FIN-006 -->
### TC-FIN-145 — an event-built entry is reversed from the journal screen like any other
Derived from : AC-FIN-083  (REQ-FIN-077)
Exercises    : SCR-FIN-006 `/fin/journal-entries/:id`
Rule / code  : RULE-FIN-011 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: entry `JV-2026-000600`, posted from event `EVT-4006` with a mapped debit account
               and BRANCH "BR-07" on its lines; the caller's role holds the custom
               `PERM_FIN_JOURNAL_ENTRIES_REVERSE`
Steps        : 1. open the entry and invoke Reverse, confirming
               2. open the created reversal
Expected     : step 1 → Reverse is offered on this EVENT_GENERATED entry exactly as on a manual
                  one; one call posts the reversal.
               step 2 → its lines show the same accounts, amounts and BRANCH value with the
                  opposite direction, each row showing the other's link, and the original still
                  reads POSTED
Test data    : `JV-2026-000600`, `EVT-4006` and "BR-07" — AC-FIN-083's own values
<!-- TC:TC-FIN-145:END -->

<!-- TC:TC-FIN-053:START traces=AC-FIN-001,REQ-FIN-001,SCR-FIN-001 -->
### TC-FIN-053 — an account is created from the chart screen
Derived from : AC-FIN-001  (REQ-FIN-001)
Exercises    : SCR-FIN-001 `/fin/accounts`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the effective menu the security module serves carries `FIN_ACCOUNTS`, and the
               caller's role holds `PERM_FIN_ACCOUNTS_CREATE`, which the server checks — the
               affordance renders for every holder of the screen (ADR-FIN-065); the
               `ACCOUNT_TYPE` and `DEBIT_CREDIT` option lists have loaded (UXD-FIN-001, UXD-FIN-002)
Steps        : 1. navigate to `/fin/accounts`
               2. open the entry side of the composite screen and fill code, both names, account
                  type, nature, optionally a parent through the parent-account picker
                  (`?picker=parent`), and the leaf flag
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
Preconditions: an account with at least one child, visible in the loaded tree; the caller's role
               holds `PERM_FIN_ACCOUNTS_UPDATE`
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
### TC-FIN-056 — a dimension is created on its `/new` page and opens as a record page
Derived from : AC-FIN-004  (REQ-FIN-004)
Exercises    : SCR-FIN-002 `/fin/dimensions/new` → `/fin/dimensions/:dimensionId`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_DIMENSIONS` is in the caller's effective menu, and the caller's role holds
               `PERM_FIN_DIMENSIONS_CREATE`, which the server checks
Steps        : 1. navigate to `/fin/dimensions/new` and observe the values section
               2. create a dimension with code `PROJECT` and both names
               3. open `/fin/dimensions`, then open the record page's URL in a fresh session
Expected     : step 1 → the values section is locked and says why: values are added under a
                  saved dimension.
               step 2 → one call creates the dimension, and the page moves to
                  `/fin/dimensions/:dimensionId` with the id from the create response: the header
                  in a collapsible section, the (empty) values beneath it.
               step 3 → the list shows the dimension as active, and the cold deep link renders
                  the same record page. It reads through the published dimension search filtered
                  by `dimensionPk`, never through an invented by-id endpoint (ADR-FIN-065)
Test data    : `PROJECT` — ENT-FIN-002's own example code
<!-- TC:TC-FIN-056:END -->

<!-- TC:TC-FIN-057:START traces=AC-FIN-005,REQ-FIN-005,SCR-FIN-002 -->
### TC-FIN-057 — a value is added under the dimension whose record page is open
Derived from : AC-FIN-005  (REQ-FIN-005)
Exercises    : SCR-FIN-002 `/fin/dimensions/:dimensionId` — the Value entry Drawer,
               `?valueAction=create`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: the dimension `PROJECT` exists and its record page is open; it holds no value with
               the code about to be used; the caller's role holds `PERM_FIN_DIMENSIONS_CREATE`
Steps        : 1. open the Value entry and add a value with code `NORTH`, both names and a sort
                  order
Expected     : the value appears among that dimension's values and under no other dimension —
               the values query is keyed by the record page's dimension id, so another
               dimension's record page shows a different set. The sort order is a required
               integer
Test data    : `NORTH` — the code AC-FIN-006 names; the sort order is a placeholder integer
<!-- TC:TC-FIN-057:END -->

<!-- TC:TC-FIN-058:START traces=AC-FIN-006,REQ-FIN-006,SCR-FIN-002 -->
### TC-FIN-058 — a duplicate value code is refused on the screen that entered it
Derived from : AC-FIN-006  (REQ-FIN-006)
Exercises    : SCR-FIN-002 `/fin/dimensions/:dimensionId` — the Value entry Drawer,
               `?valueAction=create`
Rule / code  : RULE-FIN-002 → FIN-409-DIMVALUE-DUP (409), shown inline on the code field
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: the dimension `PROJECT` already holds a value with code `NORTH`; its record page
               is open, so its values are loaded
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
### TC-FIN-059 — an event-type rule is created on its `/new` page, header alone
Derived from : AC-FIN-007  (REQ-FIN-007)
Exercises    : SCR-FIN-003 `/fin/rules/new` → `/fin/rules/:ruleId`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_RULES` is in the caller's effective menu and the caller's role holds
               `PERM_FIN_RULES_CREATE`; the seven option lists the screen needs have loaded
               (UXD-FIN-007, UXD-FIN-008, UXD-FIN-009, UXD-FIN-002, UXD-FIN-010 and, since v2,
               the two of UXD-FIN-013 and UXD-FIN-014); the chosen event type has no rule yet
Steps        : 1. navigate to `/fin/rules/new` and observe the lines section
               2. create a rule with an event type and both names
Expected     : step 1 → the lines section is locked and says why: lines are added to a saved
                  rule.
               step 2 → the rule header is saved **alone**, in one call, and the page moves to
                  `/fin/rules/:ruleId` with the id from the create response; the rule reads
                  active and has no lines yet. Lines are added afterwards one per submit, never
                  chained behind the rule's save (ADR-FIN-062). The event-type select offers the
                  `ACCOUNTING_EVENT_TYPE` options MDL returns — which may legitimately be an
                  empty list, the type being host-defined and seeding no value
Test data    : the event type code is a placeholder
<!-- TC:TC-FIN-059:END -->

<!-- TC:TC-FIN-060:START traces=AC-FIN-008,REQ-FIN-008,SCR-FIN-003 -->
### TC-FIN-060 — a rule line captures its three references as three controls
Derived from : AC-FIN-008  (REQ-FIN-008)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the Line entry Drawer, `?lineAction=create`
Rule / code  : — (happy path; RULE-FIN-019 is pre-empted on the form)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a rule's record page is open; the caller's role holds `PERM_FIN_RULES_UPDATE`,
               the permission adding a line needs
Steps        : 1. open the Line entry and choose derivation type CONSTANT
               2. pick the constant account in the account picker (`?picker=account`)
               3. set the amount-source type and value, the direction and the distribution type,
                  and save
Expected     : the line is added to the rule's lines by **one** call. The account derivation, the
               amount source and the direction are **three separate controls**, never fused. The
               picker is a sibling view offering active leaf accounts and returns the chosen
               account's code as the derivation value; the business-field control stays empty
               and not required on a CONSTANT line. The amount-source value is required unless
               the amount-source type is `REMAINDER`. There is no line-delete affordance — none
               is published (ADR-FIN-006)
Test data    : `CONSTANT`, `FIELD`, `FIXED`, `DEBIT`, `REMAINDER` — seeded values (SRS A6)
<!-- TC:TC-FIN-060:END -->

<!-- TC:TC-FIN-061:START traces=AC-FIN-009,REQ-FIN-009,SCR-FIN-003 -->
### TC-FIN-061 — the remainder-line count is enforced across the whole line set
Derived from : AC-FIN-009  (REQ-FIN-009)
Exercises    : SCR-FIN-003 `/fin/rules/:ruleId` — the Line entry Drawer, `?lineAction=create`
Rule / code  : RULE-FIN-003 → FIN-409-REMAINDER-COUNT (409), shown inline on the Line entry's
               remainder flag
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a rule's record page whose loaded lines already hold one `PERCENTAGE` line and no
               line marked as the remainder; the caller's role holds `PERM_FIN_RULES_UPDATE`
Steps        : 1. add a second `PERCENTAGE` line, not marked as the remainder, and save
               2. on a rule whose loaded lines already include one marked as the remainder, try
                  to add a further line also marked as the remainder
Expected     : step 1 → lines arrive one per submit (ADR-FIN-062), so "exactly one remainder
                  once any line is PERCENTAGE" is the server's check: its message is shown
                  inline on the remainder flag —
                  ar: «يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي» ·
                  en: "Exactly one remainder line is required when any percentage distribution is
                  present" —
                  and the line is not added.
               step 2 → the form's cross-field check, counting the loaded lines and this one,
                  refuses a second remainder marker before any request is sent.
               The rule stays unsaved in that shape until exactly one line is marked remainder,
               which is AC-FIN-009's own Then
Test data    : two percentage lines and zero remainder — AC-FIN-009's own counts
<!-- TC:TC-FIN-061:END -->

<!-- TC:TC-FIN-062:START traces=AC-FIN-014,REQ-FIN-014,SCR-FIN-006 -->
### TC-FIN-062 — a manual entry is composed and posted from its own route
Derived from : AC-FIN-014  (REQ-FIN-014)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : — (happy path; RULE-FIN-017's pairing is pre-empted by the year-filtered period
               select)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_JOURNAL_ENTRIES` is in the caller's effective menu and the caller's role holds
               `PERM_FIN_JOURNAL_ENTRIES_CREATE`; the fiscal-year and fiscal-period option lists
               have loaded; at least two leaf active accounts and an Open period exist
Steps        : 1. navigate to `/fin/journal-entries/new`
               2. pick a fiscal year from the year select, then a period from the select
                  **narrowed to that year**
               3. open the lines Drawer (`?panel=lines`), add two balanced lines through its
                  account picker, return to the entry and post
Expected     : the entry is created in one call and the search list is invalidated so the new row
               appears. The year select lists the fiscal years the facade publishes, by year
               code, and says so when that list is capped at the option read's maximum rather
               than cutting it silently. The period options are narrowed to the chosen year —
               the client pre-empt of RULE-FIN-017, the server staying the authority. The lines
               summary row shows the line count and the running debit and credit totals. The
               journal type is a fixed, non-editable `MANUAL` and is not offered as a select
               (ADR-FIN-008); the document number is never an input and appears only on the
               created row
Test data    : `MANUAL` — a seeded `JOURNAL_TYPE` value; two lines — AC-FIN-014's own minimum
<!-- TC:TC-FIN-062:END -->

<!-- TC:TC-FIN-063:START traces=AC-FIN-015,REQ-FIN-015,SCR-FIN-006 -->
### TC-FIN-063 — both validation failures are shown and nothing is posted
Derived from : AC-FIN-015  (REQ-FIN-015)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-006 → FIN-409-UNBALANCED · RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a non-leaf (rollup) account exists; the year, period and document date are
               coherent so nothing else fails first; the caller's role holds
               `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. on `/fin/journal-entries/new`, submit an entry that is both unbalanced and
                  targets the rollup account on one line, bypassing the account picker, which
                  offers leaf accounts only
Expected     : **both** failures are surfaced together, not the first alone. The imbalance is
               shown inline on the header field it concerns —
               ar «القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن» ·
               en "The entry is unbalanced — total debits do not equal total credits".
               The account refusal is shown once, in the lines pane's refusal slot, bound to no
               line because the published error body names none (ADR-FIN-065) —
               ar «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
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
Preconditions: a POSTED entry; `FIN_JOURNAL_ENTRIES` is in the caller's effective menu — Reverse
               renders for every holder of the screen, the server checking
               `PERM_FIN_JOURNAL_ENTRIES_REVERSE` (ADR-FIN-065)
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
### TC-FIN-067 — a rollup account on a line is refused, in the lines pane
Derived from : AC-FIN-019  (REQ-FIN-019)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409), shown in the lines pane's
               refusal slot
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a non-leaf (rollup) account exists; an Open period; the caller's role holds
               `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. in the lines Drawer, open the account picker and search for the rollup account
               2. submit a balanced entry in which exactly one line targets the rollup account,
                  bypassing the picker
Expected     : step 1 → the rollup account is not offered: the picker lists active leaf accounts
                  only.
               step 2 → the refusal is shown **once, in the lines pane's refusal slot** —
                  ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
                  en: "The target account does not accept direct posting".
                  It is bound to no line: the published error body names none, and the screen
                  never guesses one. The "naming the offending line" half of AC-FIN-019 is
                  asserted on the backend track, by TC-FIN-019, as a known limitation of the
                  published contract (ADR-FIN-065, ADR-FIN-066). The lines keep their values
                  and nothing was posted
Test data    : account ids are placeholders; the rollup relation is the AC's own fact
<!-- TC:TC-FIN-067:END -->

<!-- TC:TC-FIN-068:START traces=AC-FIN-020,REQ-FIN-020,SCR-FIN-006 -->
### TC-FIN-068 — an entry composed against a period that has since closed is refused
Derived from : AC-FIN-020  (REQ-FIN-020)
Exercises    : SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN (409), shown inline
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: a period Open when the form is filled, and hard-closed on SCR-FIN-007 by an
               approver before the form is submitted
Steps        : 1. fill a balanced entry against that period and leave the form open
               2. in another session, soft-close then hard-close the period on its year's
                  record page, `/fin/periods/:fiscalYearId`
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
Rule / code  : RULE-FIN-009 → FIN-409-INVALID-DIMENSION (409), shown in the lines pane's refusal
               slot
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a dimension value deactivated on its dimension's record page — the only way the
               inactive state is reachable at all (ADR-FIN-009); the caller's role holds
               `PERM_FIN_DIMENSIONS_UPDATE` and `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. on `/fin/dimensions/:dimensionId` for `PROJECT`, deactivate one of its values
               2. on `/fin/journal-entries/new`, add a line citing that dimension value and submit
Expected     : step 1 → the value reads inactive. The deactivate row action renders for every
                  holder of the screen, and the parent dimension itself offers no deactivate —
                  none is published.
               step 2 → the refusal is shown once, in the lines pane's refusal slot —
                  ar: «قيمة البُعد غير صالحة» · en: "The dimension value is invalid" —
                  bound to no line and naming no dimension, because the published error body
                  carries neither. The "naming the offending line and dimension" half of
                  AC-FIN-021 is asserted on the backend track, by TC-FIN-021 (ADR-FIN-065,
                  ADR-FIN-066). Nothing is posted
Test data    : `PROJECT` and one of its values
<!-- TC:TC-FIN-069:END -->

<!-- TC:TC-FIN-070:START traces=AC-FIN-022,REQ-FIN-022,SCR-FIN-004 -->
### TC-FIN-070 — a template is created with its schedule and lines in one submit
Derived from : AC-FIN-022  (REQ-FIN-022)
Exercises    : SCR-FIN-004 `/fin/recurring-templates/new` → `/fin/recurring-templates/:templateId`
Rule / code  : — (happy path; the frequency conditional is pre-empted on the form)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_RECURRING_TEMPLATES` is in the caller's effective menu and the caller's role
               holds `PERM_FIN_RECURRING_TEMPLATES_CREATE`; the schedule-type and frequency option
               lists have loaded (UXD-FIN-011, UXD-FIN-012)
Steps        : 1. on `/fin/recurring-templates/new`, choose schedule type `RECURRING` and observe
                  the frequency control
               2. choose `REVERSING` and observe it again
               3. return to `RECURRING`, give a frequency and a start date, compose at least two
                  balanced lines one at a time in the Line entry Drawer (`?lineAction=create`),
                  and submit
Expected     : the frequency is **required only when the schedule type is `RECURRING`** — the
               declared cross-field rule, which pre-empts FIN-400-MISSING-FREQUENCY — and is not
               required for `REVERSING`. The line set's debit = credit balance is checked before
               submit as a convenience, the server re-checking at run time. **One** call creates
               the template with its lines, and the page moves to the template's record page with
               the id from the create response, header and lines read-only. The record page
               offers Run and Deactivate and **no Edit affordance**: no update endpoint is
               published (ADR-FIN-006)
Test data    : `RECURRING`, `REVERSING`, `MONTHLY` — seeded values (SRS A6)
<!-- TC:TC-FIN-070:END -->

<!-- TC:TC-FIN-073:START traces=AC-FIN-025,REQ-FIN-025,SCR-FIN-005 -->
### TC-FIN-073 — an allocation rule is created with its targets
Derived from : AC-FIN-025  (REQ-FIN-025)
Exercises    : SCR-FIN-005 `/fin/allocation-rules/new` → `/fin/allocation-rules/:allocationRuleId`
Rule / code  : RULE-FIN-003 → pre-empted across the target set; FIN-409-REMAINDER-COUNT (409) if
               the pre-empt is bypassed
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_ALLOCATION_RULES` is in the caller's effective menu and the caller's role
               holds `PERM_FIN_ALLOCATION_RULES_CREATE`; the `DISTRIBUTION_TYPE` option list has
               loaded (UXD-FIN-010)
Steps        : 1. on `/fin/allocation-rules/new`, give a source account and compose targets one
                  at a time in the Target entry Drawer (`?targetAction=create`)
               2. with two `PERCENTAGE` targets and none marked as the remainder, try to submit
               3. mark exactly one target as the remainder and submit
               4. submit the step 2 set with the form's check bypassed
Expected     : step 2 → the form's cross-field check refuses the set before any request: exactly
                  one target must carry the remainder mark when any target is `PERCENTAGE`, and
                  the whole set is known because it travels in the one create.
               step 3 → **one** call stores the rule with its targets, and the page moves to the
                  rule's record page with the targets read-only; there is no Edit affordance, no
                  update endpoint being published.
               step 4 → the server's refusal is shown inline —
                  ar: «يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي» ·
                  en: "Exactly one remainder line is required when any percentage distribution is
                  present".
               A target's distribution value is required unless its type is `REMAINDER`
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
Preconditions: a POSTED entry with three lines; the caller's role holds the custom
               `PERM_FIN_JOURNAL_ENTRIES_REVERSE`, which is independent of CREATE
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
### TC-FIN-079 — creating a year opens its record page with its periods, each Open
Derived from : AC-FIN-031  (REQ-FIN-031)
Exercises    : SCR-FIN-007 `/fin/periods/new` → `/fin/periods/:fiscalYearId`
Rule / code  : — (happy path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: `FIN_PERIODS` is in the caller's effective menu and the caller's role holds
               `PERM_FIN_PERIODS_CREATE`; the `PERIOD_STATE` and `FISCAL_YEAR_STATUS` option lists
               have loaded (UXD-FIN-003, UXD-FIN-004)
Steps        : 1. navigate to `/fin/periods/new`
               2. create a year with a code, a start date, an end date after it, and a period
                  count
               3. return to `/fin/periods`
Expected     : step 2 → one call creates the year and the page moves to
                  `/fin/periods/:fiscalYearId` with the id from the create response: the year
                  header in a collapsible section, its generated periods beneath it, **every
                  period reading OPEN** — AC-FIN-031's own Then. The end date must be later than
                  the start date and the period count must be a positive integer, both checked on
                  the form.
               step 3 → the year is listed. The list and the record page's header come from the
                  published fiscal-year search, cited by path (ADR-FIN-061); the periods come
                  from API-FIN-033 scoped to the year. The creation invalidates both, so no
                  reload is needed
Test data    : `2026` and a twelve-period year; the dates are placeholders
<!-- TC:TC-FIN-079:END -->

<!-- TC:TC-FIN-080:START traces=AC-FIN-032,REQ-FIN-032,SCR-FIN-007 -->
### TC-FIN-080 — a soft-closed period is reopened from its row
Derived from : AC-FIN-032  (REQ-FIN-032)
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
Rule / code  : — (happy path)
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period row reading SOFT_CLOSE on its year's record page; the caller's role holds
               `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. invoke Open on that row
Expected     : the row's state becomes OPEN and the period list is invalidated so the change is
               visible at once. The row action takes no input beyond the row itself — the
               affordance is the whole form
Test data    : none invented
<!-- TC:TC-FIN-080:END -->

<!-- TC:TC-FIN-081:START traces=AC-FIN-033,REQ-FIN-033,SCR-FIN-007 -->
### TC-FIN-081 — soft-closing a period is visible on its row
Derived from : AC-FIN-033  (REQ-FIN-033)
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
Rule / code  : FIN-409-INVALID-TRANSITION (409) when the row is not Open
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period row reading OPEN on its year's record page; the caller's role holds
               `PERM_FIN_PERIODS_UPDATE`
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
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
Rule / code  : RULE-FIN-015 (the server's gate) · RULE-FIN-014 (the terminal state)
Scenario     : STATE · data class VALID · language ALL
Preconditions: a period row reading SOFT_CLOSE on its year's record page; the caller's role holds
               `PERM_FIN_PERIODS_CLOSE_APPROVE`
Steps        : 1. invoke Hard-close on that row and confirm
               2. observe the row's actions afterwards
Expected     : step 1 → the row reads HARD_CLOSE and shows who closed it and when.
               step 2 → the transitions still render, as every affordance does for a holder of
                  the screen whatever the period's state, but the state is terminal: invoking
                  Open is refused by the server (TC-FIN-083). No affordance is rendered from a
                  close-approve flag or any other grant read on the client; the server alone
                  enforces RULE-FIN-015 (ADR-FIN-065)
Test data    : none invented
<!-- TC:TC-FIN-082:END -->

<!-- TC:TC-FIN-083:START traces=AC-FIN-035,REQ-FIN-035,SCR-FIN-007 -->
### TC-FIN-083 — reopening a hard-closed period is refused on the screen
Derived from : AC-FIN-035  (REQ-FIN-035)
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
Rule / code  : RULE-FIN-014 → FIN-409-NOT-REOPENABLE (409), shown on the generic surface
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a period row reading HARD_CLOSE on its year's record page; the caller's role holds
               `PERM_FIN_PERIODS_UPDATE`
Steps        : 1. invoke Open on that row
Expected     : refused, with —
               ar: «الفترة مغلقة إغلاقًا صارمًا ولا يمكن إعادة فتحها» ·
               en: "The period is hard-closed and cannot be reopened".
               The row's state is unchanged. The Open action is neither hidden nor disabled on a
               hard-closed row: the server is the authority, and this message is what a user
               sees (ADR-FIN-065)
Test data    : none invented
<!-- TC:TC-FIN-083:END -->

<!-- TC:TC-FIN-084:START traces=AC-FIN-036,REQ-FIN-036,SCR-FIN-007 -->
### TC-FIN-084 — year-end close is refused until every period is hard-closed, then runs
Derived from : AC-FIN-036  (REQ-FIN-036)
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
Rule / code  : FIN-409-PERIODS-NOT-CLOSED (409), shown inline, when it is invoked too early
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a year's record page is open; the caller's role holds
               `PERM_FIN_PERIODS_CLOSE_APPROVE`
Steps        : 1. with at least one period not HARD_CLOSE, observe Run-year-end-close, then
                  invoke it
               2. hard-close the remaining periods, then invoke it again
Expected     : step 1 → the action renders enabled, with a readiness hint while not every period
                  reads HARD_CLOSE; the server decides, refusing with —
                  ar: «يجب إغلاق كل الفترات إغلاقًا صارمًا أولًا» ·
                  en: "Every period must be hard-closed first" —
                  shown inline, and nothing is posted (ADR-FIN-065).
               step 2 → the close runs and posts the closing and opening entries, so the journal
                  list is invalidated too and both new entries are visible on SCR-FIN-006
Test data    : none invented
<!-- TC:TC-FIN-084:END -->

<!-- TC:TC-FIN-085:START traces=AC-FIN-037,REQ-FIN-037,SCR-FIN-007 -->
### TC-FIN-085 — the approving principal and moment are shown on the closed row
Derived from : AC-FIN-037  (REQ-FIN-037)
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
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
### TC-FIN-086 — an entry creator's hard-close is refused by the server
Derived from : AC-FIN-038  (REQ-FIN-038)
Exercises    : SCR-FIN-007 `/fin/periods/:fiscalYearId`
Rule / code  : RULE-FIN-015 → FIN-403-FORBIDDEN (403), shown as the catalogue message on the screen
Scenario     : PERMISSION · data class ATTACK · language ALL
Preconditions: a user whose role holds the journal-entry-creation permission and **not** the
               close-approve one; `FIN_PERIODS` is in the effective menu with VIEW, so the screen
               is reachable; a period row reads SOFT_CLOSE
Steps        : 1. open the year's record page and inspect the period row's actions
               2. invoke Hard-close on that row
Expected     : step 1 → Hard-close and Run-year-end-close render like every other action: no
                  endpoint publishes the close-approve grant to the client, so the screen cannot
                  hide them and does not (ADR-FIN-065).
               step 2 → the server denies the call, and its message is shown on the screen in
                  the caller's language —
                  ar: «لا تملك صلاحية المالية المطلوبة لهذه العملية» ·
                  en: "You do not hold the Finance permission required for this operation" —
                  never as an empty result. The row still reads SOFT_CLOSE. The separation of
                  duty is **server-enforced**; the client runs no separation-of-duty check and no
                  permission check of its own
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
               same year, carries its brought-forward balances. The fiscal year is required. The
               two period bounds are each optional on their own: one set without the other is a
               legal open-ended range, sent as it stands and never coupled to the other
               (ADR-FIN-065)
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

<!-- SUB:INT-FLOW:START traces=REQ-FIN-003,REQ-FIN-023,REQ-FIN-024,REQ-FIN-026,REQ-FIN-046,REQ-FIN-058,AC-FIN-003,AC-FIN-023,AC-FIN-024,AC-FIN-026,AC-FIN-046,AC-FIN-062 -->
### SUB — INT-FLOW

The flows that do not fit on one screen: a definition retired here changes what is accepted
there, a run here posts an entry that is read there, and a report drills into the entry behind it.

The v2 case opens the SUB (TC-FIN-136) and the v1 cases follow unchanged.

<!-- TC:TC-FIN-136:START traces=AC-FIN-062,REQ-FIN-058,SCR-FIN-013,SCR-FIN-006,SCR-FIN-008 -->
### TC-FIN-136 — changing a mapping's account leaves posted entries as they were
Derived from : AC-FIN-062  (REQ-FIN-058)
Exercises    : SCR-FIN-013 `/fin/account-mappings` → SCR-FIN-006 `/fin/journal-entries/:id` →
               SCR-FIN-008 `/fin/reports/account-ledger`
Rule / code  : — (no rule fires; a posted line stores its account)
Scenario     : STATE · data class VALID · language ALL
Preconditions: entry `JV-2026-000500`, posted from event `EVT-3010` with its debit line on
               account 5220 through the mapping (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT");
               account 5225 is an active leaf; the caller's menu carries the three page codes,
               and the caller's role holds `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`
Host data    : ACCOUNTING_EVENT_TYPE EXPENSE_PAID (active) — each created through API-MDL-006 `POST
               /api/v1/mdl/lookup-types/{id}/values` on its key's type (SRS A6: host data, never a
               product seed — ADR-FIN-068)
Steps        : 1. on `/fin/account-mappings`, change the RENT mapping's account to 5225
               2. open `JV-2026-000500` on `/fin/journal-entries/:id`
               3. open the account ledger for 5220 over the entry's date
Expected     : step 1 → the mapping row shows 5225. The change invalidates the mapping list and
                  nothing else — no journal cache is touched, because a mapping change never
                  alters a posted entry.
               step 2 → the debit line still shows account 5220.
               step 3 → the ledger of 5220 still lists that line
Test data    : `JV-2026-000500`, `EVT-3010`, 5220, 5225 and "RENT" — AC-FIN-062's own values
<!-- TC:TC-FIN-136:END -->

<!-- TC:TC-FIN-055:START traces=AC-FIN-003,REQ-FIN-003,SCR-FIN-001,SCR-FIN-006 -->
### TC-FIN-055 — deactivating an account on one screen closes it on another
Derived from : AC-FIN-003  (REQ-FIN-003)
Exercises    : SCR-FIN-001 `/fin/accounts` → SCR-FIN-006 `/fin/journal-entries/new`
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT (409), on the second screen, shown in
               the lines pane's refusal slot
Scenario     : STATE · data class VALID · language ALL
Preconditions: an active leaf account used on entries today; an Open period; the caller's role
               holds `PERM_FIN_ACCOUNTS_UPDATE` and `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Steps        : 1. on `/fin/accounts`, deactivate the account, confirming the usage prompt the
                  screen raises before the call
               2. filter the chart list by active only, and look for the account
               3. on `/fin/journal-entries/new`, open the lines Drawer's account picker and
                  search for the account
               4. submit a balanced entry with a line on it, bypassing the picker
Expected     : step 1 → the row is deactivated; the confirmation is a prompt, not a rule check —
                  the parent-child case is the server's to refuse and is never pre-empted here.
               step 2 → the account is gone from the active results.
               step 3 → the account is not offered: the picker lists active leaf accounts only.
               step 4 → the post is refused, and the message is shown once, in the lines pane's
                  refusal slot —
                  ar: «الحساب المستهدف لا يقبل ترحيلاً مباشرًا» ·
                  en: "The target account does not accept direct posting".
                  The published error body names no line, so the screen binds the message to
                  none (ADR-FIN-065).
               This is the full lifecycle AC-FIN-003 describes: create, search, deactivate, gone
               from active results, and rejected by REQ-FIN-019 thereafter
Test data    : the account is a placeholder
<!-- TC:TC-FIN-055:END -->

<!-- TC:TC-FIN-071:START traces=AC-FIN-023,REQ-FIN-023,SCR-FIN-004,SCR-FIN-006 -->
### TC-FIN-071 — running a template posts one entry, visible on the journal screen
Derived from : AC-FIN-023  (REQ-FIN-023)
Exercises    : SCR-FIN-004 `/fin/recurring-templates/:templateId` → SCR-FIN-006
               `/fin/journal-entries`
Rule / code  : RULE-FIN-006 … RULE-FIN-009 re-checked at run time, surfaced generically
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an **active** recurring template whose next run date is today; an Open period; the
               caller's role holds `PERM_FIN_RECURRING_TEMPLATES_UPDATE`, the permission Run needs
Steps        : 1. on the template's record page, invoke Run and confirm
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
Exercises    : SCR-FIN-004 `/fin/recurring-templates/:templateId` → SCR-FIN-006
               `/fin/journal-entries`
Rule / code  : RULE-FIN-011, RULE-FIN-012 → success path, no code
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an active template whose schedule type is `REVERSING`; period N Open and period
               N+1 existing in the same year
Steps        : 1. on the template's record page, invoke Run and confirm
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
Exercises    : SCR-FIN-005 `/fin/allocation-rules/:allocationRuleId` → SCR-FIN-006
               `/fin/journal-entries`
Rule / code  : RULE-FIN-010 → success path, no code; RULE-FIN-003 governs the target set
Scenario     : BOUNDARY · data class BOUNDARY · language ALL
Preconditions: an active allocation rule with two `PERCENTAGE` targets and one `REMAINDER` target,
               its source account balance 1,000.00; an Open period; the caller's role holds
               `PERM_FIN_ALLOCATION_RULES_UPDATE`, the permission Run needs
Steps        : 1. on the rule's record page, invoke Run and confirm
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
Steps        : 1. open the balance sheet and drill down from the group that holds the line
               2. on the trial balance reached, find the line's account row and drill into it
               3. from the ledger row reached, open the originating entry
Expected     : step 1 → the trial balance opens filtered by the group's account type, carried as
                  `accountTypeCode` in its route search parameters — the one filter the trial
                  balance accepts that the statement holds; no account and no period is carried
                  (ADR-FIN-065).
               step 2 → the account ledger opens already filtered by that account.
               step 3 → the entry shows its lines and its event reference, or reads `manual`,
                  `recurring` or `allocation` where the entry is not event-sourced.
               Every hop's filter lives in the destination's route search parameters, so every
               intermediate step is linkable and back navigation returns to the filtered view
               rather than a blank report. A user whose menu lacks one of the four page codes is
               redirected to unauthorized at that hop rather than shown an empty screen
Test data    : none invented; the chain is AC-FIN-046's own
<!-- TC:TC-FIN-092:END -->
<!-- SUB:INT-FLOW:END -->
<!-- PHASE:TEST-PLAN-FE:END -->

## TC TRACEABILITY INDEX

**AC → TC** (in-track criteria only; the 27 others are in the header table)

| AC | TC | AC | TC |
|---|---|---|---|
| AC-FIN-001 | TC-FIN-053 | AC-FIN-033 | TC-FIN-081 |
| AC-FIN-002 | TC-FIN-054 | AC-FIN-034 | TC-FIN-082 |
| AC-FIN-003 | TC-FIN-055 | AC-FIN-035 | TC-FIN-083 |
| AC-FIN-004 | TC-FIN-056 | AC-FIN-036 | TC-FIN-084 |
| AC-FIN-005 | TC-FIN-057 | AC-FIN-037 | TC-FIN-085 |
| AC-FIN-006 | TC-FIN-058 | AC-FIN-038 | TC-FIN-086 |
| AC-FIN-007 | TC-FIN-059 | AC-FIN-039 | TC-FIN-087 |
| AC-FIN-008 | TC-FIN-060 | AC-FIN-040 | TC-FIN-088 |
| AC-FIN-009 | TC-FIN-061 | AC-FIN-041 | TC-FIN-089 |
| AC-FIN-014 | TC-FIN-062 | AC-FIN-042 | TC-FIN-090 |
| AC-FIN-015 | TC-FIN-063 | AC-FIN-043 | TC-FIN-091 |
| AC-FIN-016 | TC-FIN-064 | AC-FIN-046 | TC-FIN-092 |
| AC-FIN-017 | TC-FIN-065 | AC-FIN-048 | TC-FIN-130 |
| AC-FIN-018 | TC-FIN-066 | AC-FIN-049 | TC-FIN-131 |
| AC-FIN-019 | TC-FIN-067 | AC-FIN-058 | TC-FIN-132 |
| AC-FIN-020 | TC-FIN-068 | AC-FIN-059 | TC-FIN-133 |
| AC-FIN-021 | TC-FIN-069 | AC-FIN-060 | TC-FIN-134 |
| AC-FIN-022 | TC-FIN-070 | AC-FIN-061 | TC-FIN-135 |
| AC-FIN-023 | TC-FIN-071 | AC-FIN-062 | TC-FIN-136 |
| AC-FIN-024 | TC-FIN-072 | AC-FIN-063 | TC-FIN-137 |
| AC-FIN-025 | TC-FIN-073 | AC-FIN-064 | TC-FIN-138 |
| AC-FIN-026 | TC-FIN-074 | AC-FIN-065 | TC-FIN-139 |
| AC-FIN-027 | TC-FIN-075 | AC-FIN-066 | TC-FIN-140 |
| AC-FIN-028 | TC-FIN-076 | AC-FIN-069 | TC-FIN-141 |
| AC-FIN-029 | TC-FIN-077 | AC-FIN-070 | TC-FIN-142 |
| AC-FIN-030 | TC-FIN-078 | AC-FIN-075 | TC-FIN-143 |
| AC-FIN-031 | TC-FIN-079 | AC-FIN-076 | TC-FIN-144 |
| AC-FIN-032 | TC-FIN-080 | AC-FIN-083 | TC-FIN-145 |

**REQ → TC** — for the v1 requirements each `REQ-FIN-<n>` carries the TC of its own `AC-FIN-<n>`,
plus the extensions in the first table. The v2 in-track requirements are listed in full in the
second.

| REQ | TC |
|---|---|
| REQ-FIN-017 | TC-FIN-065 (the locked, posted surface) · TC-FIN-062, TC-FIN-071, TC-FIN-074 (the same unified path seen from three screens) |
| REQ-FIN-019 | TC-FIN-067 · TC-FIN-055 (the same refusal reached through the chart screen) |
| REQ-FIN-021 | TC-FIN-069 (which needs SCR-FIN-002's deactivate as its only setup) |
| REQ-FIN-036 | TC-FIN-084 · TC-FIN-089, TC-FIN-090 (the two reports that read its result) |
| REQ-FIN-046 | TC-FIN-092 (the chain) · TC-FIN-087, TC-FIN-088, TC-FIN-089 (each hop's own screen) |

| REQ (v2) | AC | TC |
|---|---|---|
| REQ-FIN-048 | AC-FIN-048 | TC-FIN-130 |
| REQ-FIN-049 | AC-FIN-049 | TC-FIN-131 |
| REQ-FIN-054 | AC-FIN-058 | TC-FIN-132 |
| REQ-FIN-055 | AC-FIN-059 | TC-FIN-133 |
| REQ-FIN-056 | AC-FIN-060 | TC-FIN-134 |
| REQ-FIN-057 | AC-FIN-061 | TC-FIN-135 |
| REQ-FIN-058 | AC-FIN-062 | TC-FIN-136 |
| REQ-FIN-059 | AC-FIN-063 | TC-FIN-137 |
| REQ-FIN-060 | AC-FIN-064 | TC-FIN-138 |
| REQ-FIN-061 | AC-FIN-065 | TC-FIN-139 |
| REQ-FIN-062 | AC-FIN-066 | TC-FIN-140 |
| REQ-FIN-064 | AC-FIN-069 | TC-FIN-141 |
| REQ-FIN-065 | AC-FIN-070 | TC-FIN-142 |
| REQ-FIN-070 | AC-FIN-075 | TC-FIN-143 |
| REQ-FIN-071 | AC-FIN-076 | TC-FIN-144 |
| REQ-FIN-077 | AC-FIN-083 | TC-FIN-145 |

**SCR → TC**

| SCR | TC |
|---|---|
| SCR-FIN-001 | TC-FIN-053, TC-FIN-054, TC-FIN-055 |
| SCR-FIN-002 | TC-FIN-056, TC-FIN-057, TC-FIN-058, TC-FIN-069 (setup) |
| SCR-FIN-003 | TC-FIN-059, TC-FIN-060, TC-FIN-061 · v2: TC-FIN-130, TC-FIN-131, TC-FIN-140, TC-FIN-141, TC-FIN-142 |
| SCR-FIN-004 | TC-FIN-070, TC-FIN-071, TC-FIN-072 |
| SCR-FIN-005 | TC-FIN-073, TC-FIN-074 |
| SCR-FIN-006 | TC-FIN-062, TC-FIN-063, TC-FIN-064, TC-FIN-065, TC-FIN-066, TC-FIN-067, TC-FIN-068, TC-FIN-069, TC-FIN-075, TC-FIN-076, TC-FIN-077, TC-FIN-078, TC-FIN-055, TC-FIN-071, TC-FIN-072, TC-FIN-074, TC-FIN-092 · v2: TC-FIN-136, TC-FIN-143, TC-FIN-145 |
| SCR-FIN-007 | TC-FIN-079, TC-FIN-080, TC-FIN-081, TC-FIN-082, TC-FIN-083, TC-FIN-084, TC-FIN-085, TC-FIN-086, TC-FIN-068 (setup) |
| SCR-FIN-008 | TC-FIN-087, TC-FIN-092 · v2: TC-FIN-136 |
| SCR-FIN-009 | TC-FIN-088, TC-FIN-092 |
| SCR-FIN-010 | TC-FIN-089, TC-FIN-092 |
| SCR-FIN-011 | TC-FIN-090 |
| SCR-FIN-012 | TC-FIN-091 · v2: TC-FIN-144 |
| SCR-FIN-013 | TC-FIN-132, TC-FIN-133, TC-FIN-134, TC-FIN-135, TC-FIN-136, TC-FIN-137, TC-FIN-138, TC-FIN-139 (v2) |

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
| RULE-FIN-011, RULE-FIN-012 | — (success path) | TC-FIN-072, TC-FIN-076, TC-FIN-077 · v2: TC-FIN-145 |
| RULE-FIN-013 | FIN-409-ALREADY-REVERSED (409) | TC-FIN-078 |
| RULE-FIN-014 | FIN-409-NOT-REOPENABLE (409) | TC-FIN-083 |
| RULE-FIN-015 | FIN-403-FORBIDDEN (403) | TC-FIN-086 |
| RULE-FIN-016 | — (enforced by omission; on screen, by the absent affordance) | TC-FIN-064, TC-FIN-065 |
| RULE-FIN-017 | — (pre-empted by the year-filtered period select; the 400s are the server's) | TC-FIN-062 |
| RULE-FIN-018 | FIN-422-UNSUPPORTED-DERIVATION (422) | TC-FIN-130 |
| RULE-FIN-019 | — (pre-empted on the Line entry; FIN-422-DERIVATION-SPEC is the server's) | TC-FIN-131, TC-FIN-060 |
| RULE-FIN-022 | FIN-409-MAPPING-DUP (409) — not pre-empted | TC-FIN-137 |
| RULE-FIN-023 | FIN-422-MAPPED-ACCOUNT-INELIGIBLE (422) | TC-FIN-138 · TC-FIN-132, TC-FIN-134 (the picker pre-empt) |
| RULE-FIN-024 | FIN-422-INVALID-PAYMENT-METHOD (422) | TC-FIN-139 · TC-FIN-132 (the select pre-empt) |
| RULE-FIN-025 | FIN-422-DIMENSION-TAG-DUP (422) | TC-FIN-141 · TC-FIN-140 (passes) |
| RULE-FIN-026 | FIN-422-DIMENSION-TAG-SOURCE (422) | TC-FIN-142 · TC-FIN-140 (passes) |
| — (not a rule) | FIN-409-INVALID-TRANSITION (409) | TC-FIN-081 |
| — (not a rule) | FIN-409-PERIODS-NOT-CLOSED (409) | TC-FIN-084 |
| — (not a rule) | FIN-400-MISSING-FREQUENCY, pre-empted on the form | TC-FIN-070 |

**Catalog rows no TC on this track exercises**, stated rather than left unsaid: the uniqueness
rows FIN-409-ACCOUNT-DUP, FIN-409-DIMENSION-DUP, FIN-409-RULE-DUP and FIN-409-YEAR-DUP, the
lookup and sort validation rows, FIN-422-REMAINDER-MARKER, FIN-422-REMAINDER-NOT-POSITIVE,
FIN-422-INVALID-PERCENTAGE-VALUE, FIN-409-NOT-ACTIVE, the `FIN-404-*` not-found rows
(FIN-404-ACCOUNT-MAPPING among them), FIN-409-NOT-POSTED, the two RULE-FIN-017 400s,
FIN-422-DERIVATION-SPEC and FIN-500. The event-path codes of RULE-FIN-020, RULE-FIN-021,
RULE-FIN-027 and RULE-FIN-028 are not on this track at all, no screen calling API-FIN-020; the
backend track covers each. No in-track `AC-FIN-*` states any of these rows. Writing a screen case
for a code no acceptance criterion asserts is the over-engineering §3 rule 6 forbids, and
ADR-FIN-009 and ADR-FIN-063 record the boundary. Two rows must not be tested:
FIN-403-SOD-VIOLATION is struck as unreachable and no client-side separation-of-duty check exists
to assert; FIN-422-MAPPING-UNSUPPORTED is retired in v2 and gone from the published catalogue
(ADR-FIN-027, ADR-FIN-064), so no screen can reach it.

## COVERAGE

```
AC  covered   56/56   ✓ 0 gaps on this track's denominator — the ACs a screen can exercise
                      (40 v1 + 16 v2). The 27 outside it are not gaps: each is covered on the
                      backend track, and the header table names the TC that covers it.
                      Module-wide the figure is 83/83.
REQ covered   56/56   ✓ 0 gaps on the same denominator (REQ-FIN-010…013, 044, 045, 047,
                      050…053, 063, 066…069, 072…076 excluded — the same event and onboarding
                      paths)
SCR covered   13/13   ✓ 0 gaps — every SCR-FIN-001…013 is named by ≥1 TC's Exercises line
TC count      56      ✓ 1.00× the in-track AC count — well under the ~2× over-engineering guard
Waiting       0/56    ADR-FIN-064 bound every v2 call, member and code to the republished
                      api-docs, so no case waits on publication. TC-FIN-143 … TC-FIN-145 still
                      need fixtures that only the v2 build-from-event path produces
Integration   n/a     scope = module: no INT-UXD phase is emitted and none is owed (§2 rule 3).
                      UXD-FIN-001 … UXD-FIN-015 are cited as the option lists a control loads,
                      never asserted as cross-module scenarios — every one of them is owned by
                      MDL, which is not in this run's selection
```
══════════════════════════════════════════════════════════════════
