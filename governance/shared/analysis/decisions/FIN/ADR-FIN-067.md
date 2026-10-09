# ADR-FIN-067 — Frontend plan re-run for FIN v2: every screen binds what it must READ, the journal entry page takes the record-page shape, and every routed refusal carries its words
Status      : ACCEPTED (non-breaking)
Stage       : P3.2 (Frontend — UX Design + Execution Plan)        Module: FIN        Version: v2
Lane        : analysis
Decided     : 2026-09-24
traces      : SCR-FIN-001, SCR-FIN-003, SCR-FIN-004, SCR-FIN-005, SCR-FIN-006, SCR-FIN-008, SCR-FIN-009, SCR-FIN-010, SCR-FIN-011, SCR-FIN-012, SCR-FIN-013, REQ-FIN-017, REQ-FIN-022, REQ-FIN-025, REQ-FIN-039, REQ-FIN-040, REQ-FIN-041, REQ-FIN-042, REQ-FIN-043, REQ-FIN-060, RULE-FIN-017

## Context

The frontend built FIN v2 end to end on 2026-09-24: 54 subs, 56 governed test cases, 72 live tests.
It recorded twelve v2 items in `frontend/modules/FIN/execution-state.json → api_doc_gaps[]`. Every one
had passed gate pass-2 with analyze clean, because every one is a gap between what the plan
DECLARES and what a consumer must DO to build it. No shape or reference clause could see them.

The factory now checks each kind (C9.18–C9.23, C10.7–C10.9). Run against the plan the gate approved,
the checks found:

- **read-complete (16).** No option read was bound for the required report parameters: `accountId`
  (SCR-FIN-008), `fiscalYearId` (SCR-FIN-010, 011) and `dimensionId` (SCR-FIN-012). None was bound
  for the optional filters (`periodId`, `fromPeriodId`/`toPeriodId`, `dimensionId`/`dimensionValueId`)
  or for the account and dimension-value pickers of SCR-FIN-004 and 005.
- **label-read (14).** Ids were rendered with no label read by `<pk> IN`. Where labels existed, they
  came from the picker reads, which filter by `isActiveFl`/`isLeafFl` and cap at 200. That is TC-FIN-136,
  an app failure.
- **scoped-option (1).** F2-SCR-FIN-006-PERIODS-QUERY read periods unscoped and narrowed them
  client-side, so every year past the 200-row cap had no periods. That is TC-FIN-068, an app failure.
- **exact-drill (2).** The SCR-FIN-013 → SCR-FIN-001 drill-in seeded `code`, a LIKE-only field, so
  `1101` also matched `11010`.
- **composition-rule (1 + 13).** `/fin/journal-entries/new` is a route page, yet its lines were drawn
  as a summary row plus a `?panel=lines` Drawer. That is the drawer shape (`erp-drawer-zustand` §10),
  against the project's page rule (§11, `ui-master-detail-records`, owner decision 2026-09-21). No
  Composition line named its container.
- **msg-bound (42).** Refusals were routed to controls with no bound text, so every test that quotes
  the message disagreed with the screen by construction.

## Decision

1. **Option reads.** Every foreign key a screen sends has an OPTIONS or PICKER row reading its
   entity:
   - SCR-FIN-008 binds an account picker with no status filter (a deactivated account still has a
     ledger), a dimension read, and a value read scoped by `dimensionId`.
   - SCR-FIN-009 and 012 bind a year read that scopes their period read (the year is never sent,
     because neither report publishes one). SCR-FIN-012 also binds its dimension and value reads.
   - SCR-FIN-010 and 011 bind the fiscal-year read. SCR-FIN-011 also binds the chosen year's
     periods for both bounds.
   - SCR-FIN-004 and 005 bind the account picker and the dimension-then-value pickers.
2. **Label reads.** Every rendered foreign id resolves through a LABELS row of its own, by
   `<entity>Pk IN [ids]`, with no status filter:
   - SCR-FIN-001: parent accounts.
   - SCR-FIN-003: tag dimensions and tag values.
   - SCR-FIN-004 and 005: accounts and dimension values.
   - SCR-FIN-006: year, period, line accounts, line dimensions and values, and the reversal links.

   A dimension value's search is scoped by its dimension (`dimensionId` — the parent scope). The
   published recurring-template line and allocation target carry `dimensionValueId` without
   `dimensionId`, so their value labels are read once per dimension. The dimensions come from an
   unfiltered dimension read, whose count is small and disclosed by `hasMore`. No value is guessed.
   Point 8 raises the backend ask.
3. **Scoped periods.** SCR-FIN-006 reads the chosen year's periods with `fiscalYearId EQUALS`, and so
   does every other period option read. The unscoped read is gone.
4. **Exact drill-in.** SCR-FIN-013's account cell drills into SCR-FIN-001 with `accountPk EQUALS` the
   row's accountId. The chart of accounts accepts `accountPk` in its search filters, and the typed
   code stays a free-text filter.
5. **Containers.** Every Composition line opens with `container page | drawer`. SCR-FIN-006's `/new`
   is a page: the header form is inline and the lines pane sits beneath it. The pane carries the
   totals and is the refusal slot. One line is composed in a one-line Drawer (`?lineAction=create`),
   and the pickers stay second-level Drawers. The read view `/:id` renders the same pane read-only.
   This is the shape the frontend kept, and the plan now says so. §10 was never intended on a page,
   so no departure is recorded.
6. **Refusal texts.** Every routed code carries `text: catalogue <CODE>`, meaning the ar and en
   `Message` of its row in the api-docs' Known Error Codes. That is the text the server answers with.
   Where a RULE raises the code, that row is the SRS rule's own message. The codes whose text is not
   their RULE's message are bound to their own row. FIN-409-ALREADY-REVERSED is one: RULE-FIN-013's SRS
   message is the not-posted case.
7. **Navigation carrier.** The ledger's `journalEntryId` opens the entry. It is named on the
   Navigation line, not listed as a rendered field, because the row shows the entry by its docNo.
8. **Asks routed to the backend track, not this stage's to implement:**
   - `AllocationTargetResponse` and the recurring-template line response to carry `dimensionId`, or
     API-FIN-008 to accept `dimensionValuePk IN` without `dimensionId`. Live, API-FIN-008 answers
     FIN-404-DIMENSION without it, which is why point 2 fans out.
   - The generated JSON examples of API-FIN-001…004, 023 and 038…041 show `accountCode`/`code`
     unquoted, while their field tables say `string`. The generator must quote string examples.
   - `POST /api/v1/fin/fiscal-years/search` still has no contract id (Drift: undeclared). Register it.

## Consequences

- Stage analyze on P3.2 reports 0 critical, 0 major and 0 minor findings. C9.18 examines 25 subjects,
  C9.19 13, C9.20 17, C9.21 13 and C9.22 13. The same checks against the plan the gate approved flag
  every item listed under Context.
- The frontend's v2 items `F2-SCR-FIN-003/004/009/010`, `F2-SCR-FIN-001` (drill-in),
  `F2-SCR-FIN-006` (periods) and `F4-SCR-FIN-006` (composition) are answered in the plan. The
  dimension-value half of F2-SCR-FIN-004 is answered by the fan-out in point 2, and the backend ask
  stays open on the backend track. The implementer's resolutions (`accountPk` drill-in, pk-IN labels,
  §11 lines pane) are now the plan's own.
- Test generation must re-derive the cases that quoted texts the plan did not bind, and those that
  assume host catalogue values (ADR-FIN-068).
