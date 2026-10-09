# ADR-FIN-011 — A dimension value that a rule line declares, if missing from the event or failing validation, rejects the event and nothing is posted
Status      : RESOLVED-IN-DIALOGUE
Stage       : P0        Module: FIN        Version: v2
Lane        : analysis-dialogue · round 3 · claude:opus
Decided     : 2026-09-23T09:50:31+00:00
Dialogue-key: 4727e93a6e36
traces      : POL-FIN-024, POL-FIN-027, POL-FIN-028

## Decision
Accepted, and upgraded from a recommendation to CONFIRMED. The request text requires it: §2 says "same discipline as accounts" and "existing validation is not bypassed", and §1 says "never a default, never a guess". Posting the line without the dimension would produce entries with no branch, which is exactly the parallel-run mismatch this change set is meant to prevent. A rule line that declares no dimension still produces a line without one, the same as a manual line. Sources: change manifest §1, §2, acceptance 4.
traces: POL-FIN-027, POL-FIN-028, POL-FIN-024

Source      : governance-shared/analysis/modules/FIN/v2/_state/briefs/P0-round3.response3.md
