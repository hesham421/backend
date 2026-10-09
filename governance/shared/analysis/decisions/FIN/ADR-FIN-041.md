# ADR-FIN-041 — Named pessimistic-write guards close the concurrency gaps ADR-FIN-029 carried forward on API-FIN-014, API-FIN-024/025/026 and API-FIN-027
Status      : ACCEPTED
Stage       : pass-1 revise        Module: FIN        Version: v2
Lane        : merge-review-notes
Decided     : 2026-09-23

## Decision
Four Concurrency lines, restated by ADR-FIN-029 as carrying no guard, now name one:
- **API-FIN-027 (run year-end close)** — the fiscal year row is loaded `PESSIMISTIC_WRITE` before
  the OPEN-status check (the same row the docNo allocation already locks later in the same method;
  the lock is simply taken at the top of the transaction instead of only at doc-numbering time).
- **API-FIN-014 (run template)** — the template row is loaded `PESSIMISTIC_WRITE` before
  `nextRunDate` is read, so two concurrent runs of one template serialize instead of both reading
  the same predecessor date and both posting.
- **API-FIN-024 / API-FIN-025 / API-FIN-026 (period open / soft-close / hard-close)** — the period
  row is loaded `PESSIMISTIC_WRITE` before the current-status check, so two concurrent transitions
  on one period serialize instead of racing last-writer-wins.

## Why
ADR-FIN-029 restated these five blocks unchanged and marked them MODIFIED-in-annotation-only, but
their own Concurrency lines say plainly that no guard exists. Two concurrent year-end closes can
both pass the OPEN check and each post a CLOSING/OPENING pair, corrupting Retained Earnings; two
concurrent runs of a recurring template can double-post; and two concurrent period transitions can
leave the period in whichever state applied last. "v1 as built, unchanged" is not a fix for a defect
that corrupts financial state, and CS-FIN-001 is additive — carrying a correctness gap forward
because touching it is out of the additive change's nominal scope is not an acceptable trade against
a doubled closing entry.

## Consequence
No entity, no DTO and no endpoint contract changes. Each affected service method's load step
changes from a plain `findById` to a `findById` under `PESSIMISTIC_WRITE` (Spring Data:
`@Lock(LockModeType.PESSIMISTIC_WRITE)` on the repository finder, or the JPA
`EntityManager.find(..., LockModeType.PESSIMISTIC_WRITE)` equivalent). The five Concurrency lines in
the backend execution plan are rewritten to name the guard instead of stating its absence.

Source : merge-review-notes gate pass-1, finding G3
Traces : API-FIN-014, API-FIN-024, API-FIN-025, API-FIN-026, API-FIN-027, ADR-FIN-029
