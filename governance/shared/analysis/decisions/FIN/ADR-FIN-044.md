# ADR-FIN-044 — ADR-FIN-037 is only partly applied, so the prior pass's G1 comes back as a repeated finding. AC-FIN-054, 055, 056, 057, 061 and 082 still carry the bare `paymentMethod` field instead of `fields.PAYMENT_METHOD`. A test built from them would receive FIN-422-MISSING-BUSINESS-FIELD instead of the message the AC asserts.
Status      : RESOLVED-IN-DIALOGUE
Stage       : P3.1        Module: FIN        Version: v2
Lane        : review-pass · round 2 · claude:opus
Decided     : 2026-09-23T11:40:34+00:00
Dialogue-key: 88844e4bdf97
traces      : AC-FIN-054, ADR-FIN-037

## Decision
(the dialogue recorded the title alone)

Source      : governance-shared/analysis/modules/FIN/v2/_state/briefs/gate-pass-1-round2.response2.md
