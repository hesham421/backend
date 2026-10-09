I've applied the REVISE findings directly to the repo files (this is a real git submodule, not a virtual pipeline), plus the required ADRs. Summary:

**`governance-shared/analysis/modules/FIN/v2/P1/srs-fin.md`**
- G1: ACs AC-FIN-047, 050–053, 067, 068, 071–074 now carry event business facts as `fields.<FIN_EVENT_BUSINESS_FIELD code>` (e.g. `fields.PAYMENT_METHOD = "CHEQUE"`) instead of bare camelCase fields; added a sentence to A2 stating the carrier.
- G4: added an A3 adaptation note for ENT-FIN-007/ENT-FIN-008 explaining why they lack name/isActiveFl (satisfies ERP-1).
- G5: ENT-FIN-015.businessValue note now states exact-code matching with trim on save/read.
- G8: AC-FIN-045's When now reads "FIN's onboarding and its v2 migration have run."
- G9: RULE-FIN-008 re-emitted (MODIFIED, wording only) — "may only run once" → "runs only after."
- Header counts, Decisions-applied table and ADR count updated (ADR-FIN-037…039).

**`governance-shared/analysis/modules/FIN/v2/P3_1/backend-execution-plan-fin.md`**
- G2: API-FIN-023 gains an unwanted-behaviour check rejecting overlapping fiscal years (RULE-FIN-029, new `FIN-409-YEAR-OVERLAP`, backed by a `btree_gist` EXCLUDE constraint), new QR-FIN-062, catalog row, rule registry row, and QR-FIN-059's assumption is now backed rather than implicit.
- G3: API-FIN-014, API-FIN-024/025/026, and API-FIN-027 now load their target row `PESSIMISTIC_WRITE` before their status checks, closing the concurrent-double-post/last-writer-wins gaps; Concurrency lines rewritten accordingly.
- Header Open-ADRs/Delta blocks updated.

**New ADRs** (sequence continued at 037, since 032–036 were already taken by an earlier review round): `ADR-FIN-037` (fields.* carrier + RULE-FIN-008 wording), `ADR-FIN-038` (ERP-1 adaptation), `ADR-FIN-039` (businessValue trim), `ADR-FIN-040` (fiscal-year overlap rule), `ADR-FIN-041` (concurrency guards).

G6 (analyzer format handling) and G7 (platform-findings registry row) are process/tooling-level and outside the file set I was given to write, so I left them unaddressed — flagging that rather than silently dropping them.