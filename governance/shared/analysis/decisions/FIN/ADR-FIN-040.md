# ADR-FIN-040 — Overlapping fiscal years stay a v1 defect; CS-FIN-001 does not undertake the fix
Status      : ACCEPTED
Stage       : pass-1 revise        Module: FIN        Version: v2
Lane        : merge-review-notes (original) · operator (revised at the pass-1 escalation)
Decided     : 2026-09-23

## Decision
FIN does not stop two fiscal years from being created with overlapping date spans, and **v2 does not
change that**. The overlap rule, the `FIN-409-YEAR-OVERLAP` code, the `btree_gist` EXCLUDE constraint
and the pre-check query this ADR first proposed are withdrawn from this change set. The defect is
recorded as v1 debt for its own change set, alongside the other v1 gaps the pass-1 gate surfaced.

## Why
The gate's first review asked what backs QR-FIN-059's implicit "at most one period covers a date"
assumption. The answer — nothing does — is correct and worth fixing. But the fix is not this change
set's.

CS-FIN-001 is four deltas on the accounting FIN derives from a business event: account mapping,
dimension tags on rule-driven lines, the duplicate-event answer and the closed-period code. Fiscal
year creation is none of them. `API-FIN-023` is a v1 endpoint that v2 does not otherwise touch, and
the proposed fix reached across three stages — a `RULE` that P1 must mint, an extension and an
EXCLUDE constraint that P2 must create, a query and an error code in P3.1.

Taking it inside a revise produced exactly the incoherence the gate then reported: `RULE-FIN-029`
minted in P3.1 though `RULE` is P1's to own, and a constraint the plan called "the guarantee" that
`db-script` never created. A change set that quietly grows to cover what a reviewer noticed in
passing is how scope stops meaning anything.

## Consequence
- No new error code, no new store constraint, no new rule and no new query in v2.
- QR-FIN-059 keeps the v1 behaviour it has: zero rows answer `FIN-422-NO-DEFINED-PERIOD`. Its Null
  handling line now says outright that more than one row is possible and what happens then, so the
  defect is stated in the plan a reader actually reads, not only here.
- The overlap defect is recorded as consumer feedback against FIN, to be scheduled with the other
  v1 items waived at this gate.

Source : merge-review-notes gate pass-1 finding G2; pass-1 gate escalation finding G2
Traces : API-FIN-023, QR-FIN-059
