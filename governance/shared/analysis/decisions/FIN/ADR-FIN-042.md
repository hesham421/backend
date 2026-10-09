# ADR-FIN-042 — Two v1 read-decide-write races are recorded, not closed, by CS-FIN-001
Status      : ACCEPTED
Stage       : P3.1        Module: FIN        Version: v2
Lane        : operator (pass-1 gate escalation)
Decided     : 2026-09-23

## Decision
Two concurrency defects in v1 endpoints that v2 does not otherwise touch are stated here and left
unchanged in this change set:

1. **API-FIN-017 (run an allocation rule)** reads the source balance (QR-FIN-022, QR-FIN-043) and
   posts an entry for that amount with no guard. Two concurrent runs of one rule both read the same
   balance and both post, so a source of 1,000.00 can end at −1,000.00.
2. **API-FIN-003 (update account)** decides RULE-FIN-001 over the child set QR-FIN-006 reads, which
   a concurrent **API-FIN-002 (create account)** can extend; API-FIN-002 also flips the named
   parent's `isLeafFl` without a lock. The result can be an account that is both leaf and parent — a
   postable rollup that RULE-FIN-007 then accepts.

## Why
Both are real, and both are v1. CS-FIN-001 is four deltas on the accounting FIN derives from a
business event; allocation runs and account-tree maintenance are neither. The pass-1 revise already
placed `PESSIMISTIC_WRITE` guards on the endpoints v2 does touch (API-FIN-014, API-FIN-024,
API-FIN-025, API-FIN-026, API-FIN-027, ADR-FIN-041); extending that sweep to the rest of v1 would
make this change set a concurrency audit of the module.

The second defect is the more serious of the two, because a both-leaf-and-parent account is a state
the chart's own invariant says cannot exist, and RULE-FIN-007 — which v2 leans on for every mapped
account — trusts that invariant. v2 does not make it worse: it adds no path that creates accounts.

## Consequence
- Neither endpoint changes in v2. The Concurrency line of API-FIN-017, API-FIN-002 and API-FIN-003
  states its race, so a reader of the plan alone finds it without this ADR.
- Both are to be scheduled with the other v1 debt waived at the pass-1 gate, and the second should
  lead that list.
- If either is fixed before the legacy-accounting cutover, the fix is a v1 change set, not a v2 one.

Source : pass-1 gate escalation finding G3
Traces : API-FIN-002, API-FIN-003, API-FIN-017, RULE-FIN-001, RULE-FIN-007
