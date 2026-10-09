<!-- source: PHASE:DATA-DOM / SUB:DATA-DOM-TRANSACTIONAL -->
<!-- context: DATA-DOM-HEADER.md — phase-level preamble -->
<!-- traces: REQ-FIN-010, REQ-FIN-014, REQ-FIN-017, REQ-FIN-028, REQ-FIN-070, REQ-FIN-072 -->
<!-- SUB:DATA-DOM-TRANSACTIONAL:START traces=REQ-FIN-010,REQ-FIN-014,REQ-FIN-017,REQ-FIN-028,REQ-FIN-070,REQ-FIN-072 -->
### SUB — DATA-DOM-TRANSACTIONAL

#### ENT-FIN-004 — JournalEntry      kind: transactional
BINDINGS       table `FIN_JOURNAL_ENTRY` · PK `journalEntryPk` (DBF-FIN-034) · PK generation `sequence` → `SEQ_FIN_JOURNAL_ENTRY` (V22) · db-script v2
BUSINESS CODE  **docNo** · column DBF-FIN-035 · format `JV-{fiscalYearCode}-{NNNNNN}` (e.g. `JV-2026-000123`), counter per fiscalYearId, one counter for every journal type · uniqueness UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO · generation source: the FIN-local generator in `com.erp.fin` (CORE — Numbering), invoked on create under the fiscal-year row lock; never in a request, always in a response
FIELDS         DBF-FIN-034..050; journalTypeCode and statusCode lookup-backed (XM-FIN-001)
DTO MEMBERSHIP manual create `{docDate, fiscalYearId, periodId, descriptionAr, descriptionEn, lines: [...]}`; event build = `EventEntryBuildRequest` (API-FIN-020 — eventReference, eventTypeCode, docDate, baseAmount, amounts, fields, descriptions); no update endpoint (RULE-FIN-016); the response carries every field with its lines
LOOKUP FIELDS  journalTypeCode → `JOURNAL_TYPE`; statusCode → `JOURNAL_STATUS`
DOMAIN RULES
- RULE-FIN-004 (MODIFIED v2) — trigger: on create (event-sourced entry), by the pre-check before building and again when the store's uniqueness guarantee on eventReference refuses a concurrent second write. Statement: the system shall reject building an entry for an event reference that has already produced a posted entry, and shall answer that rejection with the duplicate-event error whichever of the two detections catches the repeat; a uniqueness refusal from the store is never surfaced as a generic data-integrity or server error. Message: ar "تم بالفعل ترحيل قيد لهذا المرجع" · en "An entry for this event reference has already been posted". DB enforcement: UQ_FIN_JOURNAL_ENTRY_EVENT_REF, its refusal translated by FinPersistenceRefusalAdvisor (ADR-FIN-030) · owner `JournalEntryDomain` (pre-check, QR-FIN-026)
- RULE-FIN-005, RULE-FIN-006, RULE-FIN-008, RULE-FIN-011, RULE-FIN-012, RULE-FIN-013, RULE-FIN-016, RULE-FIN-017 (SRS §A5) — as in v1: QR-FIN-027, QR-FIN-029, QR-FIN-031, QR-FIN-034, QR-FIN-036, QR-FIN-035, repository omission, `JournalEntryDomain.assertHeaderCoherent`
- concurrency of RULE-FIN-013's already-reversed half — PESSIMISTIC_WRITE load of the original's header (API-FIN-021)
STATE MACHINE  statusCode (JOURNAL_STATUS) per SRS A7 — DRAFT → POSTED, POSTED terminal and locked at once; classic reversal leaves the original POSTED and links the mirror through originalEntryId / reversalEntryId (DBF-FIN-042, DBF-FIN-043); no path writes VOID. v2: every event rejection is raised before any row is written
CROSS-MODULE   journalTypeCode and statusCode touch XM-FIN-001
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → QR-FIN-023 … QR-FIN-037 (the posting pipeline, reversal and read); v2 QR-FIN-025 and QR-FIN-026 modified

#### ENT-FIN-005 — JournalLine      kind: transactional
BINDINGS       table `FIN_JOURNAL_LINE` · PK `journalLinePk` (DBF-FIN-051) · PK generation `sequence` → `SEQ_FIN_JOURNAL_LINE` (V22) · db-script v2
FIELDS         DBF-FIN-051..060; directionCode lookup-backed (XM-FIN-001); amount positive by CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE (POL-FIN-005)
DTO MEMBERSHIP written only nested in an entry's create or build request, never updated on its own (locked with its header)
DOMAIN RULES
- RULE-FIN-007, RULE-FIN-010 (SRS §A5) — QR-FIN-030, QR-FIN-028. v2: an event-built line's accountId is the rule line's constant account or the account its mapping resolves (REQ-FIN-047, REQ-FIN-050); it is STORED on the line, so a later mapping change cannot reach it (REQ-FIN-058)
CROSS-MODULE   directionCode touches XM-FIN-001
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → written by QR-FIN-024, QR-FIN-025, QR-FIN-034; read by QR-FIN-037, QR-FIN-042, QR-FIN-043, QR-FIN-044

#### ENT-FIN-006 — JournalLineDimension      kind: transactional
BINDINGS       table `FIN_JOURNAL_LINE_DIM` · PK `journalLineDimensionPk` (DBF-FIN-061) · PK generation `sequence` → `SEQ_FIN_JOURNAL_LINE_DIM` (V22) · db-script v2
FIELDS         DBF-FIN-061..064
DTO MEMBERSHIP nested under each line of a manual entry (0..N per line); for an event-built line, one row per resolved dimension tag, written by the engine (v2)
DOMAIN RULES
- RULE-FIN-009 (SRS §A5) — QR-FIN-032. v2: applied unchanged to a value the engine resolved from the event (REQ-FIN-066, REQ-FIN-067)
- v2 REQ-FIN-070: an event-built line records each dimension value in exactly this shape, one row per dimension, the same as a manual line — no second shape, no separate table, so every report reads one form (REQ-FIN-071)
CROSS-MODULE   none
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → written by QR-FIN-024, QR-FIN-025, QR-FIN-034; read by QR-FIN-037, QR-FIN-044
<!-- SUB:DATA-DOM-TRANSACTIONAL:END -->
