# ADR-FIN-014 — One distinct, catalogued period-not-open code covers every existing period whose state refuses posting; a date with no defined period is a configuration error, not this code
Status      : RESOLVED-IN-DIALOGUE
Stage       : P0        Module: FIN        Version: v2
Lane        : analysis-dialogue · round 3 · claude:opus
Decided     : 2026-09-23T09:50:31+00:00
Dialogue-key: 4d4b709e144f
traces      : POL-FIN-004, POL-FIN-031

## Decision
Accepted. The request text supports it: §4 asks for a code "distinct from validation and configuration errors". The consumer needs one signal that means "retry later". Separate codes per period state would be API detail for P3.1. A missing period is a calendar setup fault that opening a period won't fix, so it doesn't belong under a retry signal. Error-code names are set at P1/P3.1. Sources: change manifest §4, acceptance 6; domain-profile §7.1.
traces: POL-FIN-031, POL-FIN-004

Source      : governance-shared/analysis/modules/FIN/v2/_state/briefs/P0-round3.response3.md
