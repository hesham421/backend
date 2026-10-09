<!-- source: PHASE:DATA-DOM — preamble before the first SUB -->
<!-- traces: REQ-FIN-001, REQ-FIN-010, REQ-FIN-014, REQ-FIN-031, REQ-FIN-049, REQ-FIN-054, REQ-FIN-062, REQ-FIN-070 -->
## PHASE 2 — DATA-DOM

Sixteen entities, grouped by role: `SUB:DATA-DOM-MASTER` (Account, FiscalYear, FiscalPeriod),
`SUB:DATA-DOM-TRANSACTIONAL` (JournalEntry, JournalLine, JournalLineDimension — the posting core) and
`SUB:DATA-DOM-LOOKUP` (FIN's own config and lookup-kind structural data, the two v2 entities among them). Every PK
is a plain BIGINT filled by the application from its table's own sequence (db-script header; profile
stack.db.pk_generation = `sequence`); v1's blocks said `GENERATED ALWAYS AS IDENTITY`, which migration V22 never built
and which the v2 db-script corrects — the bindings below name the sequence (ADR-FIN-029). Full RULE text for v1
rules is single-sourced in the SRS §A5 and in the Error Catalog; every v2 rule is stated in full in the block that
owns it and again in the endpoint that enforces it.
