# ADR-FIN-046 — ADR-FIN-041 is too narrow. API-FIN-017 reads the source balance and then posts it with no guard, so two concurrent runs both allocate the same 1,000.00. API-FIN-002 and API-FIN-003 decide the leaf flag from the child set with no lock, so a parent account can end up both leaf and parent, which makes a rollup account postable. The "v1 as built" wording is not acceptable for these races, for the same reason ADR-FIN-041 already gives.
Status      : RESOLVED-IN-DIALOGUE
Stage       : P3.1        Module: FIN        Version: v2
Lane        : review-pass · round 2 · claude:opus
Decided     : 2026-09-23T11:40:34+00:00
Dialogue-key: cd99dfa44afc
traces      : ADR-FIN-041, API-FIN-002, API-FIN-003, API-FIN-017

## Decision
(the dialogue recorded the title alone)

Source      : governance-shared/analysis/modules/FIN/v2/_state/briefs/gate-pass-1-round2.response2.md
