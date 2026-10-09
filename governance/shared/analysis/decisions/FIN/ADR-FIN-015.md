# ADR-FIN-015 — US sequence continues after US-FIN-019, not from US-FIN-001
Status      : RESOLVED-IN-DIALOGUE
Stage       : P0.5        Module: FIN        Version: v2
Lane        : analysis-dialogue · round 2 · claude:sonnet
Decided     : 2026-09-23T10:01:54+00:00
Dialogue-key: 51ad77982e07
traces      : US-FIN-001, US-FIN-019, US-FIN-020

## Decision
Round 1's recommendation A was conditional on whether FIN v1 had already claimed US ids. It has: `governance-shared/analysis/modules/FIN/v2/_state/current-prd.md` (the stage's own artifact, which delta mode requires this stage to consult for numbering) holds FIN v1's US-FIN-001…019. Drafting this delta at 001 would silently redefine 14 of those ids — exactly what `analyze.py`'s `ids-continue` check exists to catch, and a direct violation of the SEQUENCE RULE ("never reused"). I renumbered the whole draft to **US-FIN-020…033** and re-ran `gov.py analyze -m FIN -v 2`: no `ids-continue` finding, no C4 finding beyond the expected "gate not yet approved". I did not read any v1 story content — only the id count — to keep this dialogue's reasoning independent of borrowed scope.
Traces: US-FIN-020…033

Source      : governance-shared/analysis/modules/FIN/v2/_state/briefs/P0.5-round2.response2.md
