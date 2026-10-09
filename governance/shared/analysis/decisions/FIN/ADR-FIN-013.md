# ADR-FIN-013 — The event business-field names come from a FIN-owned lookup, FIN_EVENT_BUSINESS_FIELD, registered in MDL
Status      : RESOLVED-IN-DIALOGUE
Stage       : P0        Module: FIN        Version: v2
Lane        : analysis-dialogue · round 3 · claude:opus
Decided     : 2026-09-23T09:50:31+00:00
Dialogue-key: 76cf6c80e05b
traces      : POL-FIN-022, POL-FIN-026

## Decision
Accepted. G1 says anything that can change is defined data, and G4 and G5 put every coded list in MDL, owned by the module that registers it. With free text, a typo would only show up when an event fails to post. The initial values are the four you named: payment method, expense type code, organisation code and branch code. Sources: domain-profile G1, G4, G5; [KB:erp-domain-standards §2 rule 3].
traces: POL-FIN-022, POL-FIN-026

Source      : governance-shared/analysis/modules/FIN/v2/_state/briefs/P0-round3.response3.md
