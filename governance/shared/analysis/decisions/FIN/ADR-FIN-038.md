# ADR-FIN-038 — ERP-1's master entity_defaults are recorded as adapted for ENT-FIN-007 FiscalYear and ENT-FIN-008 FiscalPeriod, matching the adaptation v2 already states for ENT-FIN-015/016
Status      : ACCEPTED
Stage       : pass-1 revise        Module: FIN        Version: v2
Lane        : merge-review-notes
Decided     : 2026-09-23

## Decision
SRS A3's preamble now records, for FiscalYear and FiscalPeriod, the same kind of adaptation of the
master `entity_defaults` that v2 already records for the two new config entities: FiscalYear's
`code` is its own bilingual-free label, so it carries no separate `nameAr`/`nameEn`; FiscalPeriod's
natural key is `periodNo` together with its `fiscalYearId`, and its lifecycle state lives in
`statusCode` (PERIOD_STATE), not in a master `isActiveFl`. No column is added, removed or retyped;
this is a documentation adaptation, not a schema change.

## Why
ERP-1 is evaluated on the whole folded P1 state, v1 included. ENT-FIN-007 has no `nameAr`/`nameEn`
and ENT-FIN-008 has no `code` and no `isActiveFl` — both pre-existing v1 facts. Left unexplained,
that reads as ERP-1 failing on two master entities. The adaptation the v2 preamble already
performs for ENT-FIN-015/016 is the correct treatment for these two as well: the profile's
`entity_defaults` are defaults, adaptable per entity where the entity's own key and lifecycle
already carry that meaning.

## Consequence
ERP-1 passes on the corrected reading. No DDL, no DBF change, no API change.

Source : merge-review-notes gate pass-1, finding G4
Traces : ENT-FIN-007, ENT-FIN-008
