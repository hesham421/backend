<!-- source: PHASE:DATA-DOM / SUB:DATA-DOM-MASTER -->
<!-- context: DATA-DOM-HEADER.md — phase-level preamble -->
<!-- traces: REQ-FIN-001, REQ-FIN-031, REQ-FIN-036, REQ-FIN-074 -->
<!-- SUB:DATA-DOM-MASTER:START traces=REQ-FIN-001,REQ-FIN-031,REQ-FIN-036,REQ-FIN-074 -->
### SUB — DATA-DOM-MASTER

#### ENT-FIN-001 — Account      kind: master
BINDINGS       table `FIN_ACCOUNT` · PK `accountPk` (DBF-FIN-001) · PK generation `sequence` → `SEQ_FIN_ACCOUNT` (V22) · db-script v2
BUSINESS CODE  none — `code` is a client-chosen chart-of-accounts code, unique by UQ_FIN_ACCOUNT_CODE
FIELDS         DBF-FIN-001..013 and DBF-FIN-147 (`isRetainedEarningsFl`, seeded as data — see API-FIN-027 and SEC-BE's bootstrap rows); see the DB Alignment Manifest
DTO MEMBERSHIP create excludes {accountPk, isActiveFl, isRetainedEarningsFl, audit}; update excludes {accountPk, code, accountTypeCode, natureCode, isActiveFl, isRetainedEarningsFl, audit}; the response includes every field
LOOKUP FIELDS  accountTypeCode → `ACCOUNT_TYPE`; natureCode → `DEBIT_CREDIT` — codes validated through XM-FIN-001, never a numeric FK
DOMAIN RULES
- RULE-FIN-001 (SRS §A5) — on update of the leaf flag · application layer (QR-FIN-006) · `AccountDomain`
- RULE-FIN-007 (SRS §A5) — on post · application layer (QR-FIN-030) · `AccountDomain`; v2 applies it unchanged to an account taken from a mapping (REQ-FIN-052)
- v2: an account an active mapping names must be an active leaf when the mapping is saved (RULE-FIN-023) — owned by `AccountMappingDomain`, reading this entity's flags
STATE MACHINE  `isActiveFl` binary — not applicable
CROSS-MODULE   accountTypeCode and natureCode touch XM-FIN-001
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-001, QR-FIN-002, QR-FIN-003, QR-FIN-004, QR-FIN-005, QR-FIN-006, QR-FIN-045; v2 QR-FIN-060

#### ENT-FIN-007 — FiscalYear      kind: master
BINDINGS       table `FIN_FISCAL_YEAR` · PK `fiscalYearPk` (DBF-FIN-065) · PK generation `sequence` → `SEQ_FIN_FISCAL_YEAR` (V22) · db-script v2
BUSINESS CODE  none
FIELDS         DBF-FIN-065..074; statusCode lookup-backed (XM-FIN-001)
DTO MEMBERSHIP create `{code, startDate, endDate, periodCount}` — periodCount is a transient input that drives period generation (REQ-FIN-031), never persisted; the response carries the year and its generated periods
LOOKUP FIELDS  statusCode → `FISCAL_YEAR_STATUS`
DOMAIN RULES
- a CLOSED year is never closed again — the year-end re-run guard, `FiscalYearDomain` (FIN-409-INVALID-TRANSITION); the rest of year-end close is orchestrated in API-FIN-027
STATE MACHINE  statusCode OPEN → CLOSED, set once by API-FIN-027 — binary (SRS A7)
CROSS-MODULE   statusCode touches XM-FIN-001
OPERATIONS     CREATE · UPDATE
REPOSITORY OPS → QR-FIN-038, QR-FIN-041, QR-FIN-047, QR-FIN-048

#### ENT-FIN-008 — FiscalPeriod      kind: master
BINDINGS       table `FIN_FISCAL_PERIOD` · PK `fiscalPeriodPk` (DBF-FIN-075) · PK generation `sequence` → `SEQ_FIN_FISCAL_PERIOD` (V22) · db-script v2
BUSINESS CODE  none
FIELDS         DBF-FIN-075..088; statusCode lookup-backed (XM-FIN-001)
DTO MEMBERSHIP no direct create — generated with the year (API-FIN-023); the transition endpoints take only `{id}`; the response includes every field
LOOKUP FIELDS  statusCode → `PERIOD_STATE`
DOMAIN RULES
- RULE-FIN-008 (SRS §A5) — period Open at post time, the year-end closing and opening entries exempt · application layer (QR-FIN-031) · `JournalEntryDomain`. v2: for an event this is the retryable rejection, published for the consumer (REQ-FIN-073, REQ-FIN-075)
- RULE-FIN-014 (SRS §A5) — a hard-closed period is never reopened · application layer (QR-FIN-040) · `FiscalPeriodDomain`
- RULE-FIN-015 (SRS §A5) — DEFERRED (no declaration surface in v2): its data source is SEC's permission matrix; the separate `PERM_FIN_PERIODS_CLOSE_APPROVE` gate on API-FIN-026 and API-FIN-027 satisfies it, with no FIN catalog row and no FIN query
- RULE-FIN-028 (v2) — trigger: on build (event-sourced entry). Statement: the system shall reject the event with the no-defined-period configuration error when no accounting period's start and end dates cover the event's date, and shall never answer this case with the period-not-open error. Message: ar "لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث" · en "No accounting period is defined for the event date". Scope: build · DB enforcement: application layer (QR-FIN-059 reads DBF-FIN-080 and DBF-FIN-081) · owner `FiscalPeriodDomain.assertDateCovered`
STATE MACHINE  statusCode (PERIOD_STATE) per SRS A7 — OPEN ⇄ SOFT_CLOSE → HARD_CLOSE → YEAR_END_CLOSE. v2: every state other than OPEN answers an event with FIN-409-PERIOD-NOT-OPEN; only SOFT_CLOSE can return to OPEN, so it is the case a consumer's retry resolves
CROSS-MODULE   statusCode touches XM-FIN-001
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-038, QR-FIN-039, QR-FIN-040, QR-FIN-049; v2 QR-FIN-059
<!-- SUB:DATA-DOM-MASTER:END -->
