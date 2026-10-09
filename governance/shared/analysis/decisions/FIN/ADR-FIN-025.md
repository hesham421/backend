# ADR-FIN-025 — Dimension tags are submitted with their rule line and follow its lifecycle

Module  : FIN     Version : v2     Stage raised : P1 (SRS)
Status  : ACCEPTED (non-breaking)
traces  : ENT-FIN-016, REQ-FIN-062, SCR-REQ-FIN-003, US-FIN-026

## Context
US-FIN-026 needs a rule line to carry dimension values derived from the event (RuleLineDimension,
ENT-FIN-016). The stories say how a tag is declared and how it is resolved. No story says how a
tag is changed or removed afterwards. In v1 the rule line itself (ENT-FIN-010) is add-only. The
as-built record at SCR-REQ-FIN-003 §B4 lists no update and no delete for a line, and FIN publishes
no `DELETE` endpoint on any screen.

## Decision
A rule line's dimension tags are part of the same submission that adds the line (REQ-FIN-062). They
are created and read with the line and are never edited or deleted on their own. This follows the
v1 lifecycle of the rule line exactly. A separately managed tag would be the only independently
editable child of an add-only parent.

## Consequences
- No new endpoint and no new permission are needed. The tags travel inside the existing add-rule-line
  operation, which `PERM_FIN_RULES_UPDATE` gates (SCR-REQ-FIN-003 §B4).
- Inherited limitation, stated rather than hidden: a line tagged wrongly can only be retired by
  deactivating its rule. The v1 note on SCR-REQ-FIN-003 §B4 says deactivation does not free the
  event type for a replacement rule. That limitation is v1's, is unchanged here, and is recorded
  as a known gap.
- `crud-covered` reports ENT-FIN-016 as having no update or delete requirement. That is expected
  under this decision.
- Override: publish add-tag and remove-tag operations on an existing rule line, gated by
  `PERM_FIN_RULES_UPDATE`, together with an update and delete REQ for ENT-FIN-016.
