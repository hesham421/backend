# ADR-FIN-023 — A MAPPING rule line names its business field in a dedicated lookup-backed field; the derivation value is the constant account code only

Module  : FIN     Version : v2     Stage raised : P1 (SRS)
Status  : ACCEPTED (non-breaking)
traces  : REQ-FIN-049, REQ-FIN-050, RULE-FIN-019, ENT-FIN-010, US-FIN-021, US-FIN-022

## Context
In v1, `RuleLine.accountDerivationValue` (ENT-FIN-010) is one free-text field whose meaning depends
on `accountDerivationTypeCode`: an account code for CONSTANT, an event field name for DIRECT, a
"mapping-set key" for MAPPING. v2 removes DIRECT (ADR-FIN-010) and makes MAPPING real. Under
MAPPING the line has to name the business field whose value in the event selects the account
(payment method, expense type code). Those names come from the FIN-owned lookup
`FIN_EVENT_BUSINESS_FIELD` (ADR-FIN-013). The profile rule is that a lookup-backed value lives in
a field bound to a lookup key and never in free text. The current overloaded text field breaks
that rule for MAPPING.

## Decision
ENT-FIN-010 gains `accountBusinessFieldCode`, a lookup field on `FIN_EVENT_BUSINESS_FIELD`. It is
required when the derivation type is MAPPING and empty when it is CONSTANT.
`accountDerivationValue` is narrowed to the constant account code. It is required when the type is
CONSTANT and empty when it is MAPPING. RULE-FIN-019 enforces both pairings. The "mapping-set key"
reading of v1 is dropped. The mapping store's key is (the rule's event type, the line's business
field, the event's value for that field), so there is no set to name.

## Consequences
- P2 adds one lookup-backed column to the rule-line table, and XM-FIN-001's key count grows with
  the new lookup type. P3.1 adds the field to the add-rule-line request.
- No v1 data is affected: no rule line was ever built with MAPPING, because v1 answered it with an
  unsupported error.
- Override: to keep a single field, store the `FIN_EVENT_BUSINESS_FIELD` code in
  `accountDerivationValue` and validate it against the lookup when the type is MAPPING.
