# ADR-FIN-010 — DIRECT account derivation is removed outright, not deprecated or restricted; dimension values use their own source list (constant or business field)
Status      : RESOLVED-IN-DIALOGUE
Stage       : P0        Module: FIN        Version: v2
Lane        : analysis-dialogue · round 3 · claude:opus
Decided     : 2026-09-23T09:50:31+00:00
Dialogue-key: c5212de91644
traces      : POL-FIN-014, POL-FIN-020, POL-FIN-021, POL-FIN-022, POL-FIN-026

## Decision
Accepted from round 1 (option A); round 2 agreed. DIRECT is the only way an account code could come in through the event payload. That breaks domain-profile G6, POL-FIN-014, POL-FIN-020 and v1's own rule "Account numbers in event payloads — never", so v1 never had a legitimate use for it. Deprecating it would protect nothing: v1 was never delivered (project-registry pipeline status: "deliver BLOCKED"), so no configuration data uses DIRECT. Restricting it would leave one value that is allowed for dimensions but forbidden for accounts, and every later stage would have to handle that ambiguity. `ACCOUNT_DERIVATION_TYPE` keeps CONSTANT and MAPPING only, and dimensions use `FIN_DIMENSION_VALUE_SOURCE`. Sources: change manifest §1; domain-profile G6, G1.
traces: POL-FIN-021, POL-FIN-022, POL-FIN-026

Source      : governance-shared/analysis/modules/FIN/v2/_state/briefs/P0-round3.response3.md
