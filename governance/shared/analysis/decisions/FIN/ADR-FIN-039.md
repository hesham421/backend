# ADR-FIN-039 — AccountMapping.businessValue matches by exact code, trimmed on save and on event read, for every business field, not PAYMENT_METHOD alone
Status      : ACCEPTED
Stage       : pass-1 revise        Module: FIN        Version: v2
Lane        : merge-review-notes
Decided     : 2026-09-23

## Decision
ENT-FIN-015.businessValue's field note now states the matching rule explicitly: compared by exact
code, with leading and trailing blanks trimmed both when the mapping is saved and when the event's
field value is read at build time (API-FIN-020). The trim applies uniformly, including to a
non-PAYMENT_METHOD business field such as EXPENSE_TYPE_CODE, which is otherwise stored and matched
as-is.

## Why
QR-FIN-052 and QR-FIN-055 already say "three key columns EXACT" and ADR-FIN-026 sets the identical
rule for dimension values, so exact matching was always the intended behaviour; the SRS simply never
stated it. Without the trim, a stray leading/trailing space typed by an administrator on save, or
present in the host system's event payload, silently produces a mapping that never resolves — the
event is rejected with FIN-422-UNMAPPED-VALUE for a fact that a human would call a match.

## Consequence
No schema change (businessValue stays VARCHAR(100), stored and compared as a plain string). The
comparison and the trim are application-layer behaviour in `AccountMappingDomain` (save) and
`EventEntryService` (build-time read of `fields[...]`), consistent with how every other lookup-backed
FIN comparison already works.

Source : merge-review-notes gate pass-1, finding G5 (downgraded from a prior round's MAJOR to MINOR)
Traces : ENT-FIN-015.businessValue, RULE-FIN-020, QR-FIN-052, QR-FIN-055
