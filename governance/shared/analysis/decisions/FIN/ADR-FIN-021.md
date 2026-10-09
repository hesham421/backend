# ADR-FIN-021 — US-FIN-025 includes the need that changing a mapping never alters an entry already posted
Status      : RESOLVED-IN-DIALOGUE
Stage       : P0.5        Module: FIN        Version: v2
Lane        : analysis-dialogue · round 3 · claude:opus
Decided     : 2026-09-23T10:01:54+00:00
Dialogue-key: 296fb32a3974
traces      : POL-FIN-013, POL-FIN-025, US-FIN-025

## Decision
US-FIN-025 said nothing about entries already posted when a mapping is changed or deactivated. The module registry's AUTO-DECISIONS say a mapping change applies only to later derivations. That rule rests on acceptance 7 (the v1 invariants are inherited) and on POL-FIN-013 (immutable audit trail). An administrator editing live mappings needs to know this. I wrote it as a need, not a mechanism: "I need to be sure that changing or deactivating a mapping does not change an entry already posted". I added POL-FIN-013 to the story's traces and the acceptance 7 source to its Source line. The traceability table row is updated to match.
traces: US-FIN-025, POL-FIN-025, POL-FIN-013

Source      : governance-shared/analysis/modules/FIN/v2/_state/briefs/P0.5-round3.response3.md
