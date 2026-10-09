# ADR-FIN-022 — The US-FIN-032 traces field stays on one line so the record parses whole
Status      : RESOLVED-IN-DIALOGUE
Stage       : P0.5        Module: FIN        Version: v2
Lane        : analysis-dialogue · round 3 · claude:opus
Decided     : 2026-09-23T10:01:54+00:00
Dialogue-key: cdea286bfe91
traces      : US-FIN-032

## Decision
In round 2's file, the wrapped continuation of US-FIN-032's Traces line started with a POL id. That is the idmodel record-truncation pattern: the record can end early and lose its Source and Status. I joined the traces onto one line. No content changed.
traces: US-FIN-032

Source      : governance-shared/analysis/modules/FIN/v2/_state/briefs/P0.5-round3.response3.md
