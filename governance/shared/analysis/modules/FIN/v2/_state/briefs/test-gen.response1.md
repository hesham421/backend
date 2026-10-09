I've realigned the FIN v2 test plans with the revised frontend plan (ADR-FIN-064 and ADR-FIN-065). `gov.py analyze --scope stage:test-gen` came back clean: 0 findings, 145 test cases traced, 83/83 acceptance criteria covered. Nothing is committed.

**Frontend test plan** (`frontend-test-plan-fin.md`)
- **Nothing is blocked any more.** The 13 cases that were waiting on the unpublished v2 endpoints (ADR-FIN-060, now superseded) now call the published ones: the four account-mapping endpoints API-FIN-038 to 041 and the new v2 fields on API-FIN-011. All "PENDING" notes are gone.
- **New routes.** Cases on the five screens with child records (SCR-FIN-002, 003, 004, 005, 007) now use the list / `/new` / `/:id` pages and the new drawer parameters (`?lineAction=create`, `?valueAction=create`, `?targetAction=create`).
- **No client-side permission gating.** Each "caller holds the … affordance" precondition now names the permission the server checks. TC-FIN-082, 084 and 086 no longer assert a hidden or disabled button: the button shows, and the server's refusal message is what gets tested.
- **Journal-line refusals.** TC-FIN-055, 063, 067 and 069 now expect the error once in the lines pane's refusal slot, not against a specific line. Where the account picker would never offer the bad account, the case checks that and then submits past it.
- **Two report fixes.** TC-FIN-090 sends the two income-statement period bounds independently. TC-FIN-092 drills from a balance-sheet group into the trial balance by account type.
- Nine v1 cases have unchanged content: 065, 066, 075, 077, 078, 087, 088, 089, 091.

**Backend test plan** (`backend-test-plan-fin.md`)
- TC-FIN-127 no longer says it fails: the republished error catalogue now has the period-not-open note.
- TC-FIN-019 and TC-FIN-021 still assert what AC-FIN-019 and AC-FIN-021 require, that the rejection names the offending line (and dimension). The published API-FIN-019 error names neither. These two halves will fail against the current build until the backend publishes that detail, and both cases now say so.

**Other files**
- **Test execution manifest:** the ADR-FIN-060 note is gone, and the fiscal-year search row now says only frontend cases use it.
- **ADR-FIN-066** (new): records why the cases were realigned and the choice to keep, not drop, the line-naming half of those two ACs.
- **Change manifest:** only the NOTE rows changed. ADDED and MODIFIED ids are unchanged, and no case was added, removed or renumbered.

**Decision for you:** the line-naming gap in TC-FIN-019 and TC-FIN-021 only closes when the backend puts the line number (and dimension) into the API-FIN-019 error body. That belongs in the next P3.1 delta.

The check caught one line in TC-FIN-082 that the tools would have misread as a new id definition (a wrapped line starting with `RULE-FIN-015 (`); I reflowed it before installing.

`_state/briefs/test-gen.md` was already modified before I started, and I left it alone.