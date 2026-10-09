# ADR-FIN-026 — A business-field dimension value resolves by code within the tagged dimension

Module  : FIN     Version : v2     Stage raised : P1 (SRS)
Status  : ACCEPTED (non-breaking)
traces  : REQ-FIN-063, REQ-FIN-069, RULE-FIN-027, ENT-FIN-003, ENT-FIN-016, US-FIN-026, US-FIN-028

## Context
When a dimension tag's source is BUSINESS_FIELD, the event carries a text value, for example the
branch code `BR-07`. The journal line needs a DimensionValue (ENT-FIN-003). ADR-FIN-012 defers any
translation table between legacy codes and FIN values. That leaves one decision open: how the
event's text becomes a FIN dimension value.

## Decision
The event's value resolves to the DimensionValue of the tagged dimension whose `code` equals it.
The comparison is exact. `DimensionValue.code` is unique within its dimension (RULE-FIN-002), so at
most one row can match. No match means the value cannot be resolved (RULE-FIN-027, REQ-FIN-069),
and the event is rejected (POL-FIN-028). The system never falls back to a default, and it never
matches on a name. A match that is inactive is rejected by the existing dimension check
(RULE-FIN-009, REQ-FIN-067).

## Consequences
- For the parallel run, FIN's organisation and branch dimension values need to carry the legacy
  codes as their `code`. That is migration data and outside this version. If the codes turn out to
  differ, the deferred US-FIN-033 is the activation path.
- The lookup has to find a DimensionValue by (dimension, code), and P2 needs an index on that pair.
- Override: case-insensitive matching, or the deferred translation table (US-FIN-033).
