# ADR-FIN-045 — ADR-FIN-040 exists only in P3.1. RULE-FIN-029 was minted by P3.1, and no SRS rule, REQ, AC or registry-srs row backs it. The db-script creates no EXCL_FIN_FISCAL_YEAR_DATERANGE and no btree_gist extension, even though API-FIN-023 names that constraint as its guarantee. The CORE advisor registry still lists two constraints. Round 1's claim that this was "all present and consistent" is overturned.
Status      : RESOLVED-IN-DIALOGUE
Stage       : P3.1        Module: FIN        Version: v2
Lane        : review-pass · round 2 · claude:opus
Decided     : 2026-09-23T11:40:34+00:00
Dialogue-key: a266b6a58db2
traces      : ADR-FIN-040, API-FIN-023, RULE-FIN-029

## Decision
(the dialogue recorded the title alone)

Source      : governance-shared/analysis/modules/FIN/v2/_state/briefs/gate-pass-1-round2.response2.md
