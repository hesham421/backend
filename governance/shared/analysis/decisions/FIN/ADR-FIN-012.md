# ADR-FIN-012 — Translating legacy organisation and branch codes into FIN dimension values is deferred; only constant and business-field sources are in scope
Status      : RESOLVED-IN-DIALOGUE
Stage       : P0        Module: FIN        Version: v2
Lane        : analysis-dialogue · round 3 · claude:opus
Decided     : 2026-09-23T09:50:31+00:00
Dialogue-key: ee3dddedf972
traces      : POL-FIN-026

## Decision
Accepted. You asked for exactly these two modes. Organisation and branch codes are business facts, so reading them directly stays within G6. Widening the mapping store to cover dimensions would add scope nobody requested. It becomes due when legacy codes turn out to differ from FIN's dimension value codes. Sources: change manifest §2; domain-profile G6.
traces: POL-FIN-026

Source      : governance-shared/analysis/modules/FIN/v2/_state/briefs/P0-round3.response3.md
