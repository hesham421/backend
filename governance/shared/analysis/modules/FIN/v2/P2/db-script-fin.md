# DATABASE — الحسابات العامة / Finance (General Ledger) (FIN) — DELTA v2
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v2 (delta on v1 — change set CS-FIN-001, ADDITIVE)   Dialect : postgresql16
Schema prefix : none
Identifier transformation : SRS logical field name (camelCase) → physical column name
  (snake_case). Applied to every identifier; no other spelling exists.
Types the §4.1 map lacks, declared once here : date → DATE · number (non-PK) → NUMERIC ·
  reference → BIGINT · lookup → VARCHAR(n) holding the MDL lookup CODE.
PK generation : sequence (profile.stack.db.pk_generation). Each PK is a plain BIGINT NOT NULL,
  populated by the application from its table's own SEQ_{TABLE} sequence. The 14 v1 sequences were
  created by migration V22. This delta creates the 2 sequences for its 2 new tables.
Date : 2026-09-23
Counts (current state, v1 + v2) : 16 tables · 165 DBF · 2 XM ids (1 live)
Delta : ADDED — 2 tables (FIN_ACCOUNT_MAPPING, FIN_RULE_LINE_DIM) · 18 DBF (DBF-FIN-148 … DBF-FIN-165)
        MODIFIED — DBF-FIN-041 (traces) · DBF-FIN-101 (value set) · DBF-FIN-102 (nullability)
        · the MDL SOFT-READ XM-FIN-001 (scope: + 6 columns, 15 keys)
        REMOVED — none (the DIRECT lookup value is soft-retired in MDL, not an id)
══════════════════════════════════════════════════════════════════

Delta reading note: the SQL in §3 is a **migration** for a database that already has v1 (V22 onward)
applied. It runs on top of v1 and is not a clean-schema script. §1 is the complete current matrix:
v1 rows are restated so the folded state still binds every RULE `Data source`. §2 is the complete
current XM register. The id definitions in §6/§7 are re-emitted only for ADDED and MODIFIED ids.
Every other v1 DBF / XM definition is carried from v1 by `gov.py state`.

Documentation correction carried in the restated matrix (no id modified, no DDL): v1's matrix typed
every PK as `GENERATED ALWAYS AS IDENTITY`. v1's own note says migration V22 built plain BIGINT PKs fed
by explicit `SEQ_<TABLE>` sequences, which matches profile.stack.db.pk_generation = `sequence`. The PK
rows below now state what V22 built. v1's shorthand audit rows ("DBF-FIN-019..022") are expanded
to one row per id, with the types v1's DDL declared.

## 1. DB FIELD TRACEABILITY MATRIX — FIN v2 (current state)

### Table FIN_ACCOUNT (ENT-FIN-001) — unchanged
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-001 | account_pk | BIGINT | ENT-FIN-001.accountPk (PK) | REQ-FIN-001 | NOT NULL | — (SEQ_FIN_ACCOUNT, application) |
| DBF-FIN-002 | code | VARCHAR(30) | ENT-FIN-001.code | REQ-FIN-001 | NOT NULL | — |
| DBF-FIN-003 | name_ar | VARCHAR(200) | ENT-FIN-001.nameAr | REQ-FIN-001 | NOT NULL | — |
| DBF-FIN-004 | name_en | VARCHAR(200) | ENT-FIN-001.nameEn | REQ-FIN-001 | NOT NULL | — |
| DBF-FIN-005 | account_type_code | VARCHAR(20) | ENT-FIN-001.accountTypeCode (A6 ACCOUNT_TYPE) | REQ-FIN-001 | NOT NULL | — |
| DBF-FIN-006 | nature_code | VARCHAR(10) | ENT-FIN-001.natureCode (A6 DEBIT_CREDIT) | REQ-FIN-001 | NOT NULL | — |
| DBF-FIN-007 | parent_account_id | BIGINT | ENT-FIN-001.parentAccountId (self) | REQ-FIN-001, REQ-FIN-002 | NULL | — |
| DBF-FIN-008 | is_leaf_fl | BOOLEAN | ENT-FIN-001.isLeafFl | REQ-FIN-002, REQ-FIN-019 | NOT NULL | TRUE |
| DBF-FIN-009 | is_active_fl | BOOLEAN | ENT-FIN-001.isActiveFl | REQ-FIN-003, REQ-FIN-019 | NOT NULL | TRUE |
| DBF-FIN-147 | is_retained_earnings_fl | BOOLEAN | ENT-FIN-001.isRetainedEarningsFl | REQ-FIN-036 | NOT NULL | FALSE — seeded as data, never through an API (v1 §1 note) |
| DBF-FIN-010 | created_by | VARCHAR(100) | ENT-FIN-001.createdBy · profile: entity_defaults.master (audit) | REQ-FIN-001 | NOT NULL | — |
| DBF-FIN-011 | created_at | TIMESTAMPTZ | ENT-FIN-001.createdAt · profile: entity_defaults.master (audit) | REQ-FIN-001 | NOT NULL | now() |
| DBF-FIN-012 | updated_by | VARCHAR(100) | ENT-FIN-001.updatedBy · profile: entity_defaults.master (audit) | REQ-FIN-001 | NULL | — |
| DBF-FIN-013 | updated_at | TIMESTAMPTZ | ENT-FIN-001.updatedAt · profile: entity_defaults.master (audit) | REQ-FIN-001 | NULL | — |

### Table FIN_DIMENSION (ENT-FIN-002) — unchanged
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-014 | dimension_pk | BIGINT | ENT-FIN-002.dimensionPk (PK) | REQ-FIN-004 | NOT NULL | — (SEQ_FIN_DIMENSION, application) |
| DBF-FIN-015 | code | VARCHAR(30) | ENT-FIN-002.code | REQ-FIN-004 | NOT NULL | — |
| DBF-FIN-016 | name_ar | VARCHAR(150) | ENT-FIN-002.nameAr | REQ-FIN-004 | NOT NULL | — |
| DBF-FIN-017 | name_en | VARCHAR(150) | ENT-FIN-002.nameEn | REQ-FIN-004 | NOT NULL | — |
| DBF-FIN-018 | is_active_fl | BOOLEAN | ENT-FIN-002.isActiveFl | REQ-FIN-004 | NOT NULL | TRUE |
| DBF-FIN-019 | created_by | VARCHAR(100) | ENT-FIN-002.createdBy · profile: entity_defaults.config (audit) | REQ-FIN-004 | NOT NULL | — |
| DBF-FIN-020 | created_at | TIMESTAMPTZ | ENT-FIN-002.createdAt · profile: entity_defaults.config (audit) | REQ-FIN-004 | NOT NULL | now() |
| DBF-FIN-021 | updated_by | VARCHAR(100) | ENT-FIN-002.updatedBy · profile: entity_defaults.config (audit) | REQ-FIN-004 | NULL | — |
| DBF-FIN-022 | updated_at | TIMESTAMPTZ | ENT-FIN-002.updatedAt · profile: entity_defaults.config (audit) | REQ-FIN-004 | NULL | — |

### Table FIN_DIMENSION_VALUE (ENT-FIN-003) — unchanged
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-023 | dimension_value_pk | BIGINT | ENT-FIN-003.dimensionValuePk (PK) | REQ-FIN-005 | NOT NULL | — (SEQ_FIN_DIMENSION_VALUE, application) |
| DBF-FIN-024 | dimension_id | BIGINT | ENT-FIN-003.dimensionId → FK ENT-FIN-002 | REQ-FIN-005 | NOT NULL | — |
| DBF-FIN-025 | code | VARCHAR(30) | ENT-FIN-003.code | REQ-FIN-005, REQ-FIN-006 | NOT NULL | — |
| DBF-FIN-026 | name_ar | VARCHAR(150) | ENT-FIN-003.nameAr | REQ-FIN-005 | NOT NULL | — |
| DBF-FIN-027 | name_en | VARCHAR(150) | ENT-FIN-003.nameEn | REQ-FIN-005 | NOT NULL | — |
| DBF-FIN-028 | sort_order | NUMERIC | ENT-FIN-003.sortOrder | REQ-FIN-005 | NOT NULL | 0 |
| DBF-FIN-029 | is_active_fl | BOOLEAN | ENT-FIN-003.isActiveFl | REQ-FIN-021 | NOT NULL | TRUE |
| DBF-FIN-030 | created_by | VARCHAR(100) | ENT-FIN-003.createdBy · profile: entity_defaults.lookup (audit) | REQ-FIN-005 | NOT NULL | — |
| DBF-FIN-031 | created_at | TIMESTAMPTZ | ENT-FIN-003.createdAt · profile: entity_defaults.lookup (audit) | REQ-FIN-005 | NOT NULL | now() |
| DBF-FIN-032 | updated_by | VARCHAR(100) | ENT-FIN-003.updatedBy · profile: entity_defaults.lookup (audit) | REQ-FIN-005 | NULL | — |
| DBF-FIN-033 | updated_at | TIMESTAMPTZ | ENT-FIN-003.updatedAt · profile: entity_defaults.lookup (audit) | REQ-FIN-005 | NULL | — |

### Table FIN_JOURNAL_ENTRY (ENT-FIN-004) — DBF-FIN-041 MODIFIED (traces only)
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-034 | journal_entry_pk | BIGINT | ENT-FIN-004.journalEntryPk (PK) | REQ-FIN-010, REQ-FIN-014 | NOT NULL | — (SEQ_FIN_JOURNAL_ENTRY, application) |
| DBF-FIN-035 | doc_no | VARCHAR(30) | ENT-FIN-004.docNo | REQ-FIN-010, REQ-FIN-014 | NOT NULL | — (FIN-local generator `JV-{fiscalYearCode}-{NNNNNN}`, application) |
| DBF-FIN-036 | doc_date | DATE | ENT-FIN-004.docDate | REQ-FIN-010, REQ-FIN-014 | NOT NULL | — |
| DBF-FIN-037 | fiscal_year_id | BIGINT | ENT-FIN-004.fiscalYearId → FK ENT-FIN-007 | REQ-FIN-010, REQ-FIN-014 | NOT NULL | — |
| DBF-FIN-038 | period_id | BIGINT | ENT-FIN-004.periodId → FK ENT-FIN-008 | REQ-FIN-020 | NOT NULL | — |
| DBF-FIN-039 | journal_type_code | VARCHAR(20) | ENT-FIN-004.journalTypeCode (A6 JOURNAL_TYPE) | REQ-FIN-010, REQ-FIN-014 | NOT NULL | — |
| DBF-FIN-040 | status_code | VARCHAR(10) | ENT-FIN-004.statusCode (A6 JOURNAL_STATUS) | REQ-FIN-017 | NOT NULL | 'DRAFT' |
| DBF-FIN-041 | event_reference | VARCHAR(100) | ENT-FIN-004.eventReference | REQ-FIN-010, REQ-FIN-011, REQ-FIN-072 (v2: its uniqueness refusal answers the duplicate-event error — RULE-FIN-004) | NULL | — |
| DBF-FIN-042 | original_entry_id | BIGINT | ENT-FIN-004.originalEntryId (self) | REQ-FIN-028 | NULL | — |
| DBF-FIN-043 | reversal_entry_id | BIGINT | ENT-FIN-004.reversalEntryId (self) | REQ-FIN-028 | NULL | — |
| DBF-FIN-044 | description_ar | TEXT | ENT-FIN-004.descriptionAr | REQ-FIN-014 | NULL | — |
| DBF-FIN-045 | description_en | TEXT | ENT-FIN-004.descriptionEn | REQ-FIN-014 | NULL | — |
| DBF-FIN-046 | posted_at | TIMESTAMPTZ | ENT-FIN-004.postedAt | REQ-FIN-017 | NULL | — |
| DBF-FIN-047 | created_by | VARCHAR(100) | ENT-FIN-004.createdBy · profile: entity_defaults.transactional (audit) | REQ-FIN-010, REQ-FIN-014 | NOT NULL | — |
| DBF-FIN-048 | created_at | TIMESTAMPTZ | ENT-FIN-004.createdAt · profile: entity_defaults.transactional (audit) | REQ-FIN-010, REQ-FIN-014 | NOT NULL | now() |
| DBF-FIN-049 | updated_by | VARCHAR(100) | ENT-FIN-004.updatedBy · profile: entity_defaults.transactional (audit) | REQ-FIN-016 | NULL | — |
| DBF-FIN-050 | updated_at | TIMESTAMPTZ | ENT-FIN-004.updatedAt · profile: entity_defaults.transactional (audit) | REQ-FIN-016 | NULL | — |

### Table FIN_JOURNAL_LINE (ENT-FIN-005) — unchanged
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-051 | journal_line_pk | BIGINT | ENT-FIN-005.journalLinePk (PK) | REQ-FIN-014 | NOT NULL | — (SEQ_FIN_JOURNAL_LINE, application) |
| DBF-FIN-052 | journal_entry_id | BIGINT | ENT-FIN-005.journalEntryId → FK ENT-FIN-004 | REQ-FIN-014 | NOT NULL | — |
| DBF-FIN-053 | line_no | NUMERIC | ENT-FIN-005.lineNo | REQ-FIN-014 | NOT NULL | — |
| DBF-FIN-054 | account_id | BIGINT | ENT-FIN-005.accountId → FK ENT-FIN-001 | REQ-FIN-019 | NOT NULL | — |
| DBF-FIN-055 | amount | NUMERIC(18,4) | ENT-FIN-005.amount | REQ-FIN-012, REQ-FIN-018 | NOT NULL | — |
| DBF-FIN-056 | direction_code | VARCHAR(10) | ENT-FIN-005.directionCode (A6 DEBIT_CREDIT) | REQ-FIN-018 | NOT NULL | — |
| DBF-FIN-057 | is_remainder_fl | BOOLEAN | ENT-FIN-005.isRemainderFl | REQ-FIN-012 | NOT NULL | FALSE |
| DBF-FIN-058 | description_ar | TEXT | ENT-FIN-005.descriptionAr | REQ-FIN-014 | NULL | — |
| DBF-FIN-059 | description_en | TEXT | ENT-FIN-005.descriptionEn | REQ-FIN-014 | NULL | — |
| DBF-FIN-060 | created_at | TIMESTAMPTZ | ENT-FIN-005.createdAt (audit) | REQ-FIN-014 | NOT NULL | now() |

### Table FIN_JOURNAL_LINE_DIM (ENT-FIN-006) — unchanged (v2: also written for event-built lines, same shape — REQ-FIN-070)
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-061 | journal_line_dim_pk | BIGINT | ENT-FIN-006.journalLineDimensionPk (PK) | REQ-FIN-021 | NOT NULL | — (SEQ_FIN_JOURNAL_LINE_DIM, application) |
| DBF-FIN-062 | journal_line_id | BIGINT | ENT-FIN-006.journalLineId → FK ENT-FIN-005 | REQ-FIN-021 | NOT NULL | — |
| DBF-FIN-063 | dimension_id | BIGINT | ENT-FIN-006.dimensionId → FK ENT-FIN-002 | REQ-FIN-021 | NOT NULL | — |
| DBF-FIN-064 | dimension_value_id | BIGINT | ENT-FIN-006.dimensionValueId → FK ENT-FIN-003 | REQ-FIN-021, REQ-FIN-043 | NOT NULL | — |

### Table FIN_FISCAL_YEAR (ENT-FIN-007) — unchanged
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-065 | fiscal_year_pk | BIGINT | ENT-FIN-007.fiscalYearPk (PK) | REQ-FIN-031 | NOT NULL | — (SEQ_FIN_FISCAL_YEAR, application) |
| DBF-FIN-066 | code | VARCHAR(10) | ENT-FIN-007.code | REQ-FIN-031 | NOT NULL | — |
| DBF-FIN-067 | start_date | DATE | ENT-FIN-007.startDate | REQ-FIN-031 | NOT NULL | — |
| DBF-FIN-068 | end_date | DATE | ENT-FIN-007.endDate | REQ-FIN-031 | NOT NULL | — |
| DBF-FIN-069 | status_code | VARCHAR(10) | ENT-FIN-007.statusCode (A6 FISCAL_YEAR_STATUS) | REQ-FIN-036 | NOT NULL | 'OPEN' |
| DBF-FIN-070 | is_active_fl | BOOLEAN | ENT-FIN-007.isActiveFl | REQ-FIN-031 | NOT NULL | TRUE |
| DBF-FIN-071 | created_by | VARCHAR(100) | ENT-FIN-007.createdBy · profile: entity_defaults.master (audit) | REQ-FIN-031 | NOT NULL | — |
| DBF-FIN-072 | created_at | TIMESTAMPTZ | ENT-FIN-007.createdAt · profile: entity_defaults.master (audit) | REQ-FIN-031 | NOT NULL | now() |
| DBF-FIN-073 | updated_by | VARCHAR(100) | ENT-FIN-007.updatedBy · profile: entity_defaults.master (audit) | REQ-FIN-031 | NULL | — |
| DBF-FIN-074 | updated_at | TIMESTAMPTZ | ENT-FIN-007.updatedAt · profile: entity_defaults.master (audit) | REQ-FIN-031 | NULL | — |

### Table FIN_FISCAL_PERIOD (ENT-FIN-008) — unchanged (v2: start_date/end_date also decide RULE-FIN-028's no-defined-period answer; status_code drives REQ-FIN-073)
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-075 | fiscal_period_pk | BIGINT | ENT-FIN-008.fiscalPeriodPk (PK) | REQ-FIN-031 | NOT NULL | — (SEQ_FIN_FISCAL_PERIOD, application) |
| DBF-FIN-076 | fiscal_year_id | BIGINT | ENT-FIN-008.fiscalYearId → FK ENT-FIN-007 | REQ-FIN-031 | NOT NULL | — |
| DBF-FIN-077 | period_no | NUMERIC | ENT-FIN-008.periodNo | REQ-FIN-031 | NOT NULL | — |
| DBF-FIN-078 | name_ar | VARCHAR(100) | ENT-FIN-008.nameAr | REQ-FIN-031 | NOT NULL | — |
| DBF-FIN-079 | name_en | VARCHAR(100) | ENT-FIN-008.nameEn | REQ-FIN-031 | NOT NULL | — |
| DBF-FIN-080 | start_date | DATE | ENT-FIN-008.startDate | REQ-FIN-031 | NOT NULL | — |
| DBF-FIN-081 | end_date | DATE | ENT-FIN-008.endDate | REQ-FIN-031 | NOT NULL | — |
| DBF-FIN-082 | status_code | VARCHAR(15) | ENT-FIN-008.statusCode (A6 PERIOD_STATE) | REQ-FIN-020, REQ-FIN-032, REQ-FIN-033, REQ-FIN-034, REQ-FIN-035 | NOT NULL | 'OPEN' |
| DBF-FIN-083 | closed_by | VARCHAR(100) | ENT-FIN-008.closedBy | REQ-FIN-037 | NULL | — |
| DBF-FIN-084 | closed_at | TIMESTAMPTZ | ENT-FIN-008.closedAt | REQ-FIN-037 | NULL | — |
| DBF-FIN-085 | created_by | VARCHAR(100) | ENT-FIN-008.createdBy · profile: entity_defaults.master (audit) | REQ-FIN-031 | NOT NULL | — |
| DBF-FIN-086 | created_at | TIMESTAMPTZ | ENT-FIN-008.createdAt · profile: entity_defaults.master (audit) | REQ-FIN-031 | NOT NULL | now() |
| DBF-FIN-087 | updated_by | VARCHAR(100) | ENT-FIN-008.updatedBy · profile: entity_defaults.master (audit) | REQ-FIN-031 | NULL | — |
| DBF-FIN-088 | updated_at | TIMESTAMPTZ | ENT-FIN-008.updatedAt · profile: entity_defaults.master (audit) | REQ-FIN-031 | NULL | — |

### Table FIN_EVENT_TYPE_RULE (ENT-FIN-009) — unchanged (v2: event_type_code is the rule's half of the RULE-FIN-020 mapping key)
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-089 | event_type_rule_pk | BIGINT | ENT-FIN-009.eventTypeRuleId (PK) | REQ-FIN-007 | NOT NULL | — (SEQ_FIN_EVENT_TYPE_RULE, application) |
| DBF-FIN-090 | event_type_code | VARCHAR(50) | ENT-FIN-009.eventTypeCode (A6 ACCOUNTING_EVENT_TYPE) | REQ-FIN-007, REQ-FIN-013 | NOT NULL | — |
| DBF-FIN-091 | name_ar | VARCHAR(150) | ENT-FIN-009.nameAr | REQ-FIN-007 | NOT NULL | — |
| DBF-FIN-092 | name_en | VARCHAR(150) | ENT-FIN-009.nameEn | REQ-FIN-007 | NOT NULL | — |
| DBF-FIN-093 | is_active_fl | BOOLEAN | ENT-FIN-009.isActiveFl | REQ-FIN-013 | NOT NULL | TRUE |
| DBF-FIN-094 | created_by | VARCHAR(100) | ENT-FIN-009.createdBy · profile: entity_defaults.config (audit) | REQ-FIN-007 | NOT NULL | — |
| DBF-FIN-095 | created_at | TIMESTAMPTZ | ENT-FIN-009.createdAt · profile: entity_defaults.config (audit) | REQ-FIN-007 | NOT NULL | now() |
| DBF-FIN-096 | updated_by | VARCHAR(100) | ENT-FIN-009.updatedBy · profile: entity_defaults.config (audit) | REQ-FIN-007 | NULL | — |
| DBF-FIN-097 | updated_at | TIMESTAMPTZ | ENT-FIN-009.updatedAt · profile: entity_defaults.config (audit) | REQ-FIN-007 | NULL | — |

### Table FIN_RULE_LINE (ENT-FIN-010) — MODIFIED v2: DBF-FIN-101, DBF-FIN-102 MODIFIED · DBF-FIN-148 ADDED
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-098 | rule_line_pk | BIGINT | ENT-FIN-010.ruleLineId (PK) | REQ-FIN-008 | NOT NULL | — (SEQ_FIN_RULE_LINE, application) |
| DBF-FIN-099 | event_type_rule_id | BIGINT | ENT-FIN-010.eventTypeRuleId → FK ENT-FIN-009 | REQ-FIN-008 | NOT NULL | — |
| DBF-FIN-100 | line_no | NUMERIC | ENT-FIN-010.lineNo | REQ-FIN-008 | NOT NULL | — |
| DBF-FIN-101 | account_derivation_type_code | VARCHAR(20) | ENT-FIN-010.accountDerivationTypeCode (A6 ACCOUNT_DERIVATION_TYPE — v2: CONSTANT / MAPPING only; DIRECT soft-retired in MDL, BLOCK 8) | REQ-FIN-008, REQ-FIN-048 | NOT NULL | — |
| DBF-FIN-102 | account_derivation_value | TEXT | ENT-FIN-010.accountDerivationValue (v2: the constant account code only; NULL when MAPPING — CHK_FIN_RULE_LINE_DERIVATION_SPEC) | REQ-FIN-008, REQ-FIN-047, REQ-FIN-049 | NULL (v2 — was NOT NULL) | — |
| DBF-FIN-148 | account_business_field_code | VARCHAR(50) | ENT-FIN-010.accountBusinessFieldCode (A6 FIN_EVENT_BUSINESS_FIELD; NULL when CONSTANT — CHK_FIN_RULE_LINE_DERIVATION_SPEC) | REQ-FIN-049, REQ-FIN-050, REQ-FIN-051 | NULL | — |
| DBF-FIN-103 | amount_source_type_code | VARCHAR(20) | ENT-FIN-010.amountSourceTypeCode (A6 AMOUNT_SOURCE_TYPE) | REQ-FIN-008 | NOT NULL | — |
| DBF-FIN-104 | amount_source_value | TEXT | ENT-FIN-010.amountSourceValue | REQ-FIN-008 | NULL | — |
| DBF-FIN-105 | direction_code | VARCHAR(10) | ENT-FIN-010.directionCode (A6 DEBIT_CREDIT) | REQ-FIN-008 | NOT NULL | — |
| DBF-FIN-106 | distribution_type_code | VARCHAR(15) | ENT-FIN-010.distributionTypeCode (A6 DISTRIBUTION_TYPE) | REQ-FIN-009, REQ-FIN-012 | NOT NULL | — |
| DBF-FIN-107 | is_remainder_fl | BOOLEAN | ENT-FIN-010.isRemainderFl | REQ-FIN-009, REQ-FIN-012 | NOT NULL | FALSE |
| DBF-FIN-108 | created_at | TIMESTAMPTZ | ENT-FIN-010.createdAt (audit) | REQ-FIN-008 | NOT NULL | now() |

Physical position: `account_business_field_code` is appended as the table's last column by
`ALTER TABLE … ADD COLUMN`, since PostgreSQL cannot insert a column mid-table. The matrix lists it
beside the field it pairs with, in SRS order.

### Table FIN_RECURRING_TEMPLATE (ENT-FIN-011) — unchanged
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-109 | recurring_template_pk | BIGINT | ENT-FIN-011.recurringTemplateId (PK) | REQ-FIN-022 | NOT NULL | — (SEQ_FIN_RECURRING_TEMPLATE, application) |
| DBF-FIN-110 | name_ar | VARCHAR(150) | ENT-FIN-011.nameAr | REQ-FIN-022 | NOT NULL | — |
| DBF-FIN-111 | name_en | VARCHAR(150) | ENT-FIN-011.nameEn | REQ-FIN-022 | NOT NULL | — |
| DBF-FIN-112 | schedule_type_code | VARCHAR(15) | ENT-FIN-011.scheduleTypeCode (A6 RECURRING_SCHEDULE_TYPE) | REQ-FIN-022, REQ-FIN-024 | NOT NULL | — |
| DBF-FIN-113 | frequency_code | VARCHAR(15) | ENT-FIN-011.frequencyCode (A6 RECURRING_FREQUENCY) | REQ-FIN-023 | NULL | — |
| DBF-FIN-114 | start_date | DATE | ENT-FIN-011.startDate | REQ-FIN-022 | NOT NULL | — |
| DBF-FIN-115 | next_run_date | DATE | ENT-FIN-011.nextRunDate | REQ-FIN-023 | NOT NULL | — |
| DBF-FIN-116 | end_date | DATE | ENT-FIN-011.endDate | REQ-FIN-022 | NULL | — |
| DBF-FIN-117 | is_active_fl | BOOLEAN | ENT-FIN-011.isActiveFl | REQ-FIN-022 | NOT NULL | TRUE |
| DBF-FIN-118 | created_by | VARCHAR(100) | ENT-FIN-011.createdBy · profile: entity_defaults.config (audit) | REQ-FIN-022 | NOT NULL | — |
| DBF-FIN-119 | created_at | TIMESTAMPTZ | ENT-FIN-011.createdAt · profile: entity_defaults.config (audit) | REQ-FIN-022 | NOT NULL | now() |
| DBF-FIN-120 | updated_by | VARCHAR(100) | ENT-FIN-011.updatedBy · profile: entity_defaults.config (audit) | REQ-FIN-022 | NULL | — |
| DBF-FIN-121 | updated_at | TIMESTAMPTZ | ENT-FIN-011.updatedAt · profile: entity_defaults.config (audit) | REQ-FIN-022 | NULL | — |

### Table FIN_RECURRING_TEMPLATE_LINE (ENT-FIN-012) — unchanged
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-122 | recurring_template_line_pk | BIGINT | ENT-FIN-012.recurringTemplateLineId (PK) | REQ-FIN-022 | NOT NULL | — (SEQ_FIN_RECURRING_TEMPLATE_LINE, application) |
| DBF-FIN-123 | recurring_template_id | BIGINT | ENT-FIN-012.recurringTemplateId → FK ENT-FIN-011 | REQ-FIN-022 | NOT NULL | — |
| DBF-FIN-124 | line_no | NUMERIC | ENT-FIN-012.lineNo | REQ-FIN-022 | NOT NULL | — |
| DBF-FIN-125 | account_id | BIGINT | ENT-FIN-012.accountId → FK ENT-FIN-001 | REQ-FIN-022 | NOT NULL | — |
| DBF-FIN-126 | amount | NUMERIC(18,4) | ENT-FIN-012.amount | REQ-FIN-022 | NOT NULL | — |
| DBF-FIN-127 | direction_code | VARCHAR(10) | ENT-FIN-012.directionCode (A6 DEBIT_CREDIT) | REQ-FIN-022 | NOT NULL | — |
| DBF-FIN-128 | dimension_value_id | BIGINT | ENT-FIN-012.dimensionValueId → FK ENT-FIN-003 | REQ-FIN-022 | NULL | — |
| DBF-FIN-129 | created_at | TIMESTAMPTZ | ENT-FIN-012.createdAt (audit) | REQ-FIN-022 | NOT NULL | now() |

### Table FIN_ALLOCATION_RULE (ENT-FIN-013) — unchanged
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-130 | allocation_rule_pk | BIGINT | ENT-FIN-013.allocationRuleId (PK) | REQ-FIN-025 | NOT NULL | — (SEQ_FIN_ALLOCATION_RULE, application) |
| DBF-FIN-131 | name_ar | VARCHAR(150) | ENT-FIN-013.nameAr | REQ-FIN-025 | NOT NULL | — |
| DBF-FIN-132 | name_en | VARCHAR(150) | ENT-FIN-013.nameEn | REQ-FIN-025 | NOT NULL | — |
| DBF-FIN-133 | source_account_id | BIGINT | ENT-FIN-013.sourceAccountId → FK ENT-FIN-001 | REQ-FIN-025, REQ-FIN-026 | NOT NULL | — |
| DBF-FIN-134 | is_active_fl | BOOLEAN | ENT-FIN-013.isActiveFl | REQ-FIN-025 | NOT NULL | TRUE |
| DBF-FIN-135 | created_by | VARCHAR(100) | ENT-FIN-013.createdBy · profile: entity_defaults.config (audit) | REQ-FIN-025 | NOT NULL | — |
| DBF-FIN-136 | created_at | TIMESTAMPTZ | ENT-FIN-013.createdAt · profile: entity_defaults.config (audit) | REQ-FIN-025 | NOT NULL | now() |
| DBF-FIN-137 | updated_by | VARCHAR(100) | ENT-FIN-013.updatedBy · profile: entity_defaults.config (audit) | REQ-FIN-025 | NULL | — |
| DBF-FIN-138 | updated_at | TIMESTAMPTZ | ENT-FIN-013.updatedAt · profile: entity_defaults.config (audit) | REQ-FIN-025 | NULL | — |

### Table FIN_ALLOCATION_TARGET (ENT-FIN-014) — unchanged
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-139 | allocation_target_pk | BIGINT | ENT-FIN-014.allocationTargetId (PK) | REQ-FIN-025 | NOT NULL | — (SEQ_FIN_ALLOCATION_TARGET, application) |
| DBF-FIN-140 | allocation_rule_id | BIGINT | ENT-FIN-014.allocationRuleId → FK ENT-FIN-013 | REQ-FIN-025 | NOT NULL | — |
| DBF-FIN-141 | line_no | NUMERIC | ENT-FIN-014.lineNo | REQ-FIN-025 | NOT NULL | — |
| DBF-FIN-142 | target_account_id | BIGINT | ENT-FIN-014.targetAccountId → FK ENT-FIN-001 | REQ-FIN-025 | NOT NULL | — |
| DBF-FIN-143 | dimension_value_id | BIGINT | ENT-FIN-014.dimensionValueId → FK ENT-FIN-003 | REQ-FIN-025 | NULL | — |
| DBF-FIN-144 | distribution_type_code | VARCHAR(15) | ENT-FIN-014.distributionTypeCode (A6 DISTRIBUTION_TYPE) | REQ-FIN-009, REQ-FIN-026 | NOT NULL | — |
| DBF-FIN-145 | distribution_value | NUMERIC(18,4) | ENT-FIN-014.distributionValue | REQ-FIN-026 | NULL | — |
| DBF-FIN-146 | is_remainder_fl | BOOLEAN | ENT-FIN-014.isRemainderFl | REQ-FIN-009, REQ-FIN-026 | NOT NULL | FALSE |

### Table FIN_ACCOUNT_MAPPING (ENT-FIN-015) — ADDED v2
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-149 | account_mapping_pk | BIGINT | ENT-FIN-015.accountMappingPk (PK) | REQ-FIN-054 | NOT NULL | — (SEQ_FIN_ACCOUNT_MAPPING, application) |
| DBF-FIN-150 | event_type_code | VARCHAR(50) | ENT-FIN-015.eventTypeCode (A6 ACCOUNTING_EVENT_TYPE) — fixed after create (ADR-FIN-024) | REQ-FIN-050, REQ-FIN-054, REQ-FIN-055, REQ-FIN-059 | NOT NULL | — |
| DBF-FIN-151 | business_field_code | VARCHAR(50) | ENT-FIN-015.businessFieldCode (A6 FIN_EVENT_BUSINESS_FIELD) — fixed after create | REQ-FIN-050, REQ-FIN-054, REQ-FIN-055, REQ-FIN-059, REQ-FIN-061 | NOT NULL | — |
| DBF-FIN-152 | business_value | VARCHAR(100) | ENT-FIN-015.businessValue — stored as a code; a PAYMENT_METHOD code when business_field_code = PAYMENT_METHOD (RULE-FIN-024); fixed after create | REQ-FIN-050, REQ-FIN-053, REQ-FIN-054, REQ-FIN-055, REQ-FIN-059, REQ-FIN-061 | NOT NULL | — |
| DBF-FIN-153 | account_id | BIGINT | ENT-FIN-015.accountId → FK ENT-FIN-001 — the only column an update changes | REQ-FIN-050, REQ-FIN-052, REQ-FIN-054, REQ-FIN-056, REQ-FIN-060 | NOT NULL | — |
| DBF-FIN-154 | is_active_fl | BOOLEAN | ENT-FIN-015.isActiveFl | REQ-FIN-053, REQ-FIN-055, REQ-FIN-057, REQ-FIN-059 | NOT NULL | TRUE |
| DBF-FIN-155 | created_by | VARCHAR(100) | ENT-FIN-015.createdBy · profile: entity_defaults.config (audit) | REQ-FIN-054 | NOT NULL | — |
| DBF-FIN-156 | created_at | TIMESTAMPTZ | ENT-FIN-015.createdAt · profile: entity_defaults.config (audit) | REQ-FIN-054 | NOT NULL | now() |
| DBF-FIN-157 | updated_by | VARCHAR(100) | ENT-FIN-015.updatedBy · profile: entity_defaults.config (audit) | REQ-FIN-056, REQ-FIN-057 | NULL | — |
| DBF-FIN-158 | updated_at | TIMESTAMPTZ | ENT-FIN-015.updatedAt · profile: entity_defaults.config (audit) | REQ-FIN-056, REQ-FIN-057 | NULL | — |

No column holds a display text for the mapping: the bilingual label is the referenced account's
`name_ar` / `name_en` (DBF-FIN-003 / DBF-FIN-004), per the SRS's adapted config defaults. No journal
line references a mapping. A line stores the resolved `account_id` (DBF-FIN-054), so a later change
to a mapping cannot reach an already-posted line (REQ-FIN-058). That holds by structure.

### Table FIN_RULE_LINE_DIM (ENT-FIN-016) — ADDED v2
| DBF id | Column | Type (postgresql16) | Traces (ENT.field) | Traces (REQ) | Nullable | Default |
|---|---|---|---|---|---|---|
| DBF-FIN-159 | rule_line_dimension_pk | BIGINT | ENT-FIN-016.ruleLineDimensionPk (PK) | REQ-FIN-062 | NOT NULL | — (SEQ_FIN_RULE_LINE_DIM, application) |
| DBF-FIN-160 | value_source_code | VARCHAR(50) | ENT-FIN-016.valueSourceCode (A6 FIN_DIMENSION_VALUE_SOURCE) | REQ-FIN-062, REQ-FIN-063, REQ-FIN-065 | NOT NULL | — |
| DBF-FIN-161 | business_field_code | VARCHAR(50) | ENT-FIN-016.businessFieldCode (A6 FIN_EVENT_BUSINESS_FIELD; NULL when CONSTANT — CHK_FIN_RULE_LINE_DIM_VALUE_SPEC) | REQ-FIN-062, REQ-FIN-063, REQ-FIN-065, REQ-FIN-068, REQ-FIN-069 | NULL | — |
| DBF-FIN-162 | rule_line_id | BIGINT | ENT-FIN-016.ruleLineId → FK ENT-FIN-010 | REQ-FIN-062, REQ-FIN-064 | NOT NULL | — |
| DBF-FIN-163 | dimension_id | BIGINT | ENT-FIN-016.dimensionId → FK ENT-FIN-002 | REQ-FIN-062, REQ-FIN-064, REQ-FIN-066 | NOT NULL | — |
| DBF-FIN-164 | dimension_value_id | BIGINT | ENT-FIN-016.dimensionValueId → FK ENT-FIN-003 (NULL when BUSINESS_FIELD — CHK_FIN_RULE_LINE_DIM_VALUE_SPEC) | REQ-FIN-062, REQ-FIN-063, REQ-FIN-065 | NULL | — |
| DBF-FIN-165 | created_at | TIMESTAMPTZ | ENT-FIN-016.createdAt (audit — written with the rule line) | REQ-FIN-062 | NOT NULL | now() |

No `is_active_fl`: a tag lives and retires with its rule line (ADR-FIN-025), which has no active
flag either. No `updated_*`: tags are never updated (SCR-REQ-FIN-003 §B4).

Total: 165 DBF ids across 16 tables (v1: 147 across 14 · v2 ADDED: 18 across 1 altered and 2 new tables).

## 2. XM REGISTER — FIN v2 (current state)

| XM id | Type | This table | Column / access | Target table | Target module | Traces (REQ) | Status |
|---|---|---|---|---|---|---|---|
| XM-FIN-001 | SOFT-READ | FIN_ACCOUNT, FIN_JOURNAL_ENTRY, FIN_JOURNAL_LINE, FIN_FISCAL_YEAR, FIN_FISCAL_PERIOD, FIN_EVENT_TYPE_RULE, FIN_RULE_LINE, FIN_RECURRING_TEMPLATE, FIN_ALLOCATION_TARGET, v2: FIN_ACCOUNT_MAPPING, FIN_RULE_LINE_DIM | application-level validation of every lookup-backed code column against `MDL_LOOKUP_VALUE`, joined through `MDL_LOOKUP_TYPE.key` = the FIN-owned key. v1 columns: account_type_code, nature_code, direction_code, journal_type_code, status_code, event_type_code, account_derivation_type_code, amount_source_type_code, distribution_type_code, schedule_type_code, frequency_code. v2 adds: FIN_RULE_LINE.account_business_field_code, FIN_ACCOUNT_MAPPING.event_type_code, FIN_ACCOUNT_MAPPING.business_field_code, FIN_ACCOUNT_MAPPING.business_value (only when business_field_code = PAYMENT_METHOD — RULE-FIN-024), FIN_RULE_LINE_DIM.value_source_code, FIN_RULE_LINE_DIM.business_field_code. Now over 15 FIN-owned keys | MDL_LOOKUP_VALUE (and MDL_LOOKUP_TYPE for the key join) | MDL | REQ-FIN-001, REQ-FIN-007, REQ-FIN-008, REQ-FIN-010, REQ-FIN-014, REQ-FIN-018, REQ-FIN-022, REQ-FIN-025, REQ-FIN-031, REQ-FIN-045, REQ-FIN-048, REQ-FIN-049, REQ-FIN-054, REQ-FIN-061, REQ-FIN-062 | ACTIVE (target MDL v1 gated) — MODIFIED v2 (scope widened; same target, same type) |
| ~~XM-FIN-002~~ | READ | (no table — service-to-service) | HISTORICAL. It was FIN's read of SEC's user→permission directory for a separation-of-duties fact, and was deleted on 2026-09-12 with `FinSeparationOfDutiesService` (v1 §2 carries the full record). Unchanged by v2 | SEC's grant tables (crossmodule interface only) | SEC | REQ-FIN-037, REQ-FIN-038 | RETIRED 2026-09-12 — id burned, never reused |

SRS A8 origin of XM-FIN-001: the "LookupType, LookupValue — ENT-MDL-001, ENT-MDL-002 — MDL" row. v2
adds 2 lookup types, the CHEQUE value and the DIRECT retirement there, "the physical shape stays the
v1 SOFT-READ lookup validation (ADR-FIN-001), now over 15 keys". v2 therefore modifies this XM and
assigns no new one. The other A8 rows are SEC's User, ModuleRegistry, ScreenRegistry and
ActionRegistry. v2 adds the FIN_ACCOUNT_MAPPINGS screen and its VIEW / CREATE / UPDATE action rows.
These stay what ADR-FIN-001 made them: platform-standard registration and identity, narrated by
P3.1. They are not XM rows, and no physical cross-module FK exists. The legacy Oracle host (A8 external
service) is reached by EVENT only, through an out-of-scope consumer. It gets no table access in
either direction, so it gets no XM.

### 2.1 SOFT-READ handling
```
-- XM-FIN-001 SOFT-READ — every FIN lookup-backed column reads MDL_LOOKUP_VALUE (joined by
-- MDL_LOOKUP_TYPE.key = the FIN-owned key, e.g. 'FIN_EVENT_BUSINESS_FIELD', and
-- MDL_LOOKUP_VALUE.code = the stored column value) from MDL without a physical FK: these
-- columns store plain VARCHAR codes validated at the application layer, per ADR-FIN-001.
-- Rationale: centralization per the MDL plan; FIN owns no lookup table (A6: "values are
-- runtime-loaded from MDL, never hardcoded").
-- Risk: a value deactivated in MDL after a FIN row already cites it is not retro-invalidated —
-- v2 relies on exactly that for DIRECT (deactivated, no FIN row cites it: ADR-FIN-010). A PAYMENT_METHOD
-- value deactivated in MDL leaves any mapping that stores it in place; that mapping can no longer
-- be created again (RULE-FIN-024 runs on create) but still resolves for events. Changes to
-- MDL_LOOKUP_VALUE require impact assessment on REQ-FIN-045, REQ-FIN-048, REQ-FIN-049,
-- REQ-FIN-054, REQ-FIN-061, REQ-FIN-062.
```

No DEFERRED FK. A SOFT-READ never gets a live constraint (§6), and MDL is already gated either way.

## 3. FULL_DATABASE_SCRIPT — v2 migration (applies on top of v1)

```sql
-- ════════════════════════════════════════════════════════════════
-- FIN v2 — General Ledger module — PostgreSQL 16 — MIGRATION on v1 (CS-FIN-001)
-- Identifier transformation: camelCase (SRS) -> snake_case (DB)
-- Precondition: FIN v1 objects exist (migrations V22 onward); MDL v1 objects exist.
-- ════════════════════════════════════════════════════════════════

-- BLOCK 1 — SEQUENCES (one per NEW table; the 14 v1 sequences exist from V22)
CREATE SEQUENCE SEQ_FIN_ACCOUNT_MAPPING START WITH 1 INCREMENT BY 1 CACHE 1 NO CYCLE;
CREATE SEQUENCE SEQ_FIN_RULE_LINE_DIM START WITH 1 INCREMENT BY 1 CACHE 1 NO CYCLE;

-- BLOCK 2 — PARENT TABLES
-- No new parent table. FIN_RULE_LINE (v1), parent of FIN_RULE_LINE_DIM, is altered here:
-- ENT-FIN-010 MODIFIED — the MAPPING business field is added, and the derivation value
-- becomes optional because a MAPPING line carries no account code (RULE-FIN-019, ADR-FIN-023).
ALTER TABLE FIN_RULE_LINE ADD COLUMN account_business_field_code VARCHAR(50);
ALTER TABLE FIN_RULE_LINE ALTER COLUMN account_derivation_value DROP NOT NULL;

-- BLOCK 3 — CHILD TABLES (parents FIN_ACCOUNT, FIN_RULE_LINE, FIN_DIMENSION,
-- FIN_DIMENSION_VALUE already exist from v1)

CREATE TABLE FIN_ACCOUNT_MAPPING (
  account_mapping_pk   BIGINT        NOT NULL,
  event_type_code      VARCHAR(50)   NOT NULL,
  business_field_code  VARCHAR(50)   NOT NULL,
  business_value       VARCHAR(100)  NOT NULL,
  account_id           BIGINT        NOT NULL,
  is_active_fl         BOOLEAN       NOT NULL DEFAULT TRUE,
  created_by           VARCHAR(100)  NOT NULL,
  created_at           TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_by           VARCHAR(100),
  updated_at           TIMESTAMPTZ
);

CREATE TABLE FIN_RULE_LINE_DIM (
  rule_line_dimension_pk  BIGINT        NOT NULL,
  value_source_code       VARCHAR(50)   NOT NULL,
  business_field_code     VARCHAR(50),
  rule_line_id            BIGINT        NOT NULL,
  dimension_id            BIGINT        NOT NULL,
  dimension_value_id      BIGINT,
  created_at              TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- BLOCK 4 — COMMENTS (every new table and column, plus the re-described v1 columns)

COMMENT ON TABLE FIN_RULE_LINE IS 'ENT-FIN-010 RuleLine — PRIVATE; [DBF-FIN-098..108, DBF-FIN-148]';
COMMENT ON COLUMN FIN_RULE_LINE.account_derivation_type_code IS 'DBF-FIN-101 — lookup ACCOUNT_DERIVATION_TYPE (XM-FIN-001): CONSTANT or MAPPING only; DIRECT retired (RULE-FIN-018, ADR-FIN-010)';
COMMENT ON COLUMN FIN_RULE_LINE.account_derivation_value IS 'DBF-FIN-102 — the constant account code when CONSTANT; NULL when MAPPING (RULE-FIN-019, CHK_FIN_RULE_LINE_DERIVATION_SPEC)';
COMMENT ON COLUMN FIN_RULE_LINE.account_business_field_code IS 'DBF-FIN-148 — lookup FIN_EVENT_BUSINESS_FIELD (XM-FIN-001): the business field whose event value selects the account through FIN_ACCOUNT_MAPPING when MAPPING; NULL when CONSTANT (RULE-FIN-019)';

COMMENT ON COLUMN FIN_JOURNAL_ENTRY.event_reference IS 'DBF-FIN-041 — RULE-FIN-004 idempotency (UQ_FIN_JOURNAL_ENTRY_EVENT_REF); a violation of that constraint is answered as the duplicate-event error, never a generic error (REQ-FIN-072)';

COMMENT ON TABLE FIN_ACCOUNT_MAPPING IS 'ENT-FIN-015 AccountMapping — PRIVATE config, soft-deleted via is_active_fl; one active row per (event_type_code, business_field_code, business_value) — RULE-FIN-022; [DBF-FIN-149..158]';
COMMENT ON COLUMN FIN_ACCOUNT_MAPPING.account_mapping_pk IS 'DBF-FIN-149 — primary key, drawn by the application from SEQ_FIN_ACCOUNT_MAPPING';
COMMENT ON COLUMN FIN_ACCOUNT_MAPPING.event_type_code IS 'DBF-FIN-150 — lookup ACCOUNTING_EVENT_TYPE (XM-FIN-001); fixed after create (ADR-FIN-024)';
COMMENT ON COLUMN FIN_ACCOUNT_MAPPING.business_field_code IS 'DBF-FIN-151 — lookup FIN_EVENT_BUSINESS_FIELD (XM-FIN-001); fixed after create';
COMMENT ON COLUMN FIN_ACCOUNT_MAPPING.business_value IS 'DBF-FIN-152 — business value stored as a code; an active PAYMENT_METHOD code when business_field_code = PAYMENT_METHOD (RULE-FIN-024, XM-FIN-001); fixed after create';
COMMENT ON COLUMN FIN_ACCOUNT_MAPPING.account_id IS 'DBF-FIN-153 — resolved account; active leaf on save (RULE-FIN-023) and again at post (RULE-FIN-007), application layer';
COMMENT ON COLUMN FIN_ACCOUNT_MAPPING.is_active_fl IS 'DBF-FIN-154 — an inactive mapping resolves nothing (RULE-FIN-020)';
COMMENT ON COLUMN FIN_ACCOUNT_MAPPING.created_by IS 'DBF-FIN-155';
COMMENT ON COLUMN FIN_ACCOUNT_MAPPING.created_at IS 'DBF-FIN-156';
COMMENT ON COLUMN FIN_ACCOUNT_MAPPING.updated_by IS 'DBF-FIN-157';
COMMENT ON COLUMN FIN_ACCOUNT_MAPPING.updated_at IS 'DBF-FIN-158';

COMMENT ON TABLE FIN_RULE_LINE_DIM IS 'ENT-FIN-016 RuleLineDimension — PRIVATE config; follows its rule line (ADR-FIN-025); one tag per dimension per line — RULE-FIN-025; [DBF-FIN-159..165]';
COMMENT ON COLUMN FIN_RULE_LINE_DIM.rule_line_dimension_pk IS 'DBF-FIN-159 — primary key, drawn by the application from SEQ_FIN_RULE_LINE_DIM';
COMMENT ON COLUMN FIN_RULE_LINE_DIM.value_source_code IS 'DBF-FIN-160 — lookup FIN_DIMENSION_VALUE_SOURCE (XM-FIN-001): CONSTANT or BUSINESS_FIELD';
COMMENT ON COLUMN FIN_RULE_LINE_DIM.business_field_code IS 'DBF-FIN-161 — lookup FIN_EVENT_BUSINESS_FIELD (XM-FIN-001) when BUSINESS_FIELD; NULL when CONSTANT (RULE-FIN-026); the event value resolves by FIN_DIMENSION_VALUE.code within dimension_id (ADR-FIN-026, RULE-FIN-027)';
COMMENT ON COLUMN FIN_RULE_LINE_DIM.rule_line_id IS 'DBF-FIN-162';
COMMENT ON COLUMN FIN_RULE_LINE_DIM.dimension_id IS 'DBF-FIN-163 — RULE-FIN-025 (UQ_FIN_RULE_LINE_DIM_LINE_DIM)';
COMMENT ON COLUMN FIN_RULE_LINE_DIM.dimension_value_id IS 'DBF-FIN-164 — constant value when CONSTANT, must belong to dimension_id (RULE-FIN-026, application layer); NULL when BUSINESS_FIELD';
COMMENT ON COLUMN FIN_RULE_LINE_DIM.created_at IS 'DBF-FIN-165';

-- BLOCK 5 — CONSTRAINTS

-- 5a PK
ALTER TABLE FIN_ACCOUNT_MAPPING  ADD CONSTRAINT PK_FIN_ACCOUNT_MAPPING  PRIMARY KEY (account_mapping_pk);
ALTER TABLE FIN_RULE_LINE_DIM    ADD CONSTRAINT PK_FIN_RULE_LINE_DIM    PRIMARY KEY (rule_line_dimension_pk);

-- 5b UNIQUE
ALTER TABLE FIN_RULE_LINE_DIM    ADD CONSTRAINT UQ_FIN_RULE_LINE_DIM_LINE_DIM  UNIQUE (rule_line_id, dimension_id);   -- RULE-FIN-025
-- RULE-FIN-022 (one ACTIVE mapping per key) is a partial unique index, BLOCK 7 (ADR-FIN-028).
-- RULE-FIN-004 (MODIFIED): UQ_FIN_JOURNAL_ENTRY_EVENT_REF (v1) is unchanged; only its violation's answer changes.

-- 5c CHECK — exclusive-or, no lookup code named (ADR-FIN-028; v1 DEFAULT: value sets live in MDL)
ALTER TABLE FIN_RULE_LINE        ADD CONSTRAINT CHK_FIN_RULE_LINE_DERIVATION_SPEC
  CHECK ((account_derivation_value IS NULL) <> (account_business_field_code IS NULL));   -- RULE-FIN-019
ALTER TABLE FIN_RULE_LINE_DIM    ADD CONSTRAINT CHK_FIN_RULE_LINE_DIM_VALUE_SPEC
  CHECK ((dimension_value_id IS NULL) <> (business_field_code IS NULL));                  -- RULE-FIN-026

-- 5d intra-module FK (parent PKs exist from v1)
ALTER TABLE FIN_ACCOUNT_MAPPING  ADD CONSTRAINT FK_ACCOUNT_MAPPING_ACCOUNT    FOREIGN KEY (account_id) REFERENCES FIN_ACCOUNT (account_pk);
ALTER TABLE FIN_RULE_LINE_DIM    ADD CONSTRAINT FK_RULE_LINE_DIM_LINE         FOREIGN KEY (rule_line_id) REFERENCES FIN_RULE_LINE (rule_line_pk);
ALTER TABLE FIN_RULE_LINE_DIM    ADD CONSTRAINT FK_RULE_LINE_DIM_DIMENSION    FOREIGN KEY (dimension_id) REFERENCES FIN_DIMENSION (dimension_pk);
ALTER TABLE FIN_RULE_LINE_DIM    ADD CONSTRAINT FK_RULE_LINE_DIM_VALUE        FOREIGN KEY (dimension_value_id) REFERENCES FIN_DIMENSION_VALUE (dimension_value_pk);

-- BLOCK 6 — TRIGGERS
-- none: every v2 RULE is enforced at the application layer (it needs the event payload, MDL
-- or another row) or structurally above (RULE-FIN-019, RULE-FIN-025, RULE-FIN-026's
-- exclusive-or) and in BLOCK 7 (RULE-FIN-022). No PK-population trigger.

-- BLOCK 7 — INDEXES (non-PK; every new FK column + every SCR-REQ-FIN-013 filter column)
CREATE UNIQUE INDEX UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY  ON FIN_ACCOUNT_MAPPING (event_type_code, business_field_code, business_value) WHERE is_active_fl;   -- RULE-FIN-022
CREATE INDEX IDX_FIN_ACCOUNT_MAPPING_EVT_FLD_VAL       ON FIN_ACCOUNT_MAPPING (event_type_code, business_field_code, business_value);   -- search incl. inactive rows (REQ-FIN-055)
CREATE INDEX IDX_FIN_ACCOUNT_MAPPING_FIELD             ON FIN_ACCOUNT_MAPPING (business_field_code);   -- businessFieldCode filter alone
CREATE INDEX IDX_FIN_ACCOUNT_MAPPING_ACCOUNT           ON FIN_ACCOUNT_MAPPING (account_id);            -- FK + accountId filter
CREATE INDEX IDX_FIN_RULE_LINE_DIM_DIMENSION           ON FIN_RULE_LINE_DIM (dimension_id);            -- FK
CREATE INDEX IDX_FIN_RULE_LINE_DIM_VALUE               ON FIN_RULE_LINE_DIM (dimension_value_id);      -- FK
-- FIN_RULE_LINE_DIM.rule_line_id (FK) is the leading column of UQ_FIN_RULE_LINE_DIM_LINE_DIM,
-- whose implicit index serves it — never duplicated.

-- BLOCK 8 — LOOKUP SEED DATA (tables owned by MDL; keys owned by FIN — so FIN seeds them)
-- Target tables: MDL_LOOKUP_TYPE, MDL_LOOKUP_VALUE (MDL module, created by MDL's own script).
-- Only the v2 changes are seeded here. The 11 unchanged v1 keys stay as v1 left them.
-- Idempotent: every statement is guarded against MDL's unique keys (UQ_MDL_LOOKUP_TYPE_KEY,
-- UQ_MDL_LOOKUP_VALUE_TYPE_CODE), so it coexists with FIN's API onboarding (REQ-FIN-045).
-- PKs are drawn from MDL's own sequences, as MDL's application does.

-- FIN_EVENT_BUSINESS_FIELD — ADDED (ADR-FIN-013; SRS A6)
INSERT INTO MDL_LOOKUP_TYPE (lookup_type_pk, key, owner_module_code, name_ar, name_en, is_active_fl, created_by, created_at)
SELECT nextval('SEQ_MDL_LOOKUP_TYPE'), 'FIN_EVENT_BUSINESS_FIELD', 'FIN', 'الحقل التجاري للحدث', 'Event business field', TRUE, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM MDL_LOOKUP_TYPE WHERE key = 'FIN_EVENT_BUSINESS_FIELD');

INSERT INTO MDL_LOOKUP_VALUE (lookup_value_pk, lookup_type_id, code, name_ar, name_en, sort_order, is_active_fl, created_by, created_at)
SELECT nextval('SEQ_MDL_LOOKUP_VALUE'), t.lookup_type_pk, s.code, s.name_ar, s.name_en, s.sort_order, TRUE, 'SYSTEM', now()
FROM MDL_LOOKUP_TYPE t
CROSS JOIN (VALUES
  ('PAYMENT_METHOD',    'طريقة الدفع',     'Payment method',    1),
  ('EXPENSE_TYPE_CODE', 'رمز نوع المصروف', 'Expense type code', 2),
  ('ORGANISATION_CODE', 'رمز المؤسسة',     'Organisation code', 3),
  ('BRANCH_CODE',       'رمز الفرع',       'Branch code',       4)
) AS s (code, name_ar, name_en, sort_order)
WHERE t.key = 'FIN_EVENT_BUSINESS_FIELD'
  AND NOT EXISTS (SELECT 1 FROM MDL_LOOKUP_VALUE v WHERE v.lookup_type_id = t.lookup_type_pk AND v.code = s.code);

-- FIN_DIMENSION_VALUE_SOURCE — ADDED (ADR-FIN-010; SRS A6)
INSERT INTO MDL_LOOKUP_TYPE (lookup_type_pk, key, owner_module_code, name_ar, name_en, is_active_fl, created_by, created_at)
SELECT nextval('SEQ_MDL_LOOKUP_TYPE'), 'FIN_DIMENSION_VALUE_SOURCE', 'FIN', 'مصدر قيمة البُعد', 'Dimension value source', TRUE, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM MDL_LOOKUP_TYPE WHERE key = 'FIN_DIMENSION_VALUE_SOURCE');

INSERT INTO MDL_LOOKUP_VALUE (lookup_value_pk, lookup_type_id, code, name_ar, name_en, sort_order, is_active_fl, created_by, created_at)
SELECT nextval('SEQ_MDL_LOOKUP_VALUE'), t.lookup_type_pk, s.code, s.name_ar, s.name_en, s.sort_order, TRUE, 'SYSTEM', now()
FROM MDL_LOOKUP_TYPE t
CROSS JOIN (VALUES
  ('CONSTANT',       'ثابت',       'Constant',       1),
  ('BUSINESS_FIELD', 'حقل تجاري', 'Business field', 2)
) AS s (code, name_ar, name_en, sort_order)
WHERE t.key = 'FIN_DIMENSION_VALUE_SOURCE'
  AND NOT EXISTS (SELECT 1 FROM MDL_LOOKUP_VALUE v WHERE v.lookup_type_id = t.lookup_type_pk AND v.code = s.code);

-- PAYMENT_METHOD — MODIFIED: CHEQUE seeded (named by the user; SRS A6). Other methods are host data.
INSERT INTO MDL_LOOKUP_VALUE (lookup_value_pk, lookup_type_id, code, name_ar, name_en, sort_order, is_active_fl, created_by, created_at)
SELECT nextval('SEQ_MDL_LOOKUP_VALUE'), t.lookup_type_pk, 'CHEQUE', 'شيك', 'Cheque', 1, TRUE, 'SYSTEM', now()
FROM MDL_LOOKUP_TYPE t
WHERE t.key = 'PAYMENT_METHOD'
  AND NOT EXISTS (SELECT 1 FROM MDL_LOOKUP_VALUE v WHERE v.lookup_type_id = t.lookup_type_pk AND v.code = 'CHEQUE');

-- ACCOUNT_DERIVATION_TYPE — MODIFIED: DIRECT retired (ADR-FIN-010). Soft retire, per the
-- soft-delete default, so exactly CONSTANT and MAPPING stay active (AC-FIN-045).
-- No FIN_RULE_LINE row cites DIRECT (ADR-FIN-010: v1 never delivered it), so nothing is converted.
UPDATE MDL_LOOKUP_VALUE v
   SET is_active_fl = FALSE, updated_by = 'SYSTEM', updated_at = now()
  FROM MDL_LOOKUP_TYPE t
 WHERE v.lookup_type_id = t.lookup_type_pk
   AND t.key = 'ACCOUNT_DERIVATION_TYPE'
   AND v.code = 'DIRECT'
   AND v.is_active_fl;
COMMIT;

-- BLOCK 9 — VIEWS
-- none required by the v2 SRS (reports stay computed queries, POL-FIN-009; event-built lines
-- reach them through FIN_JOURNAL_LINE_DIM in the v1 shape — REQ-FIN-070, REQ-FIN-071).

-- BLOCK 10 — FUNCTIONS / PROCEDURES
-- none required by the v2 SRS.

-- BLOCK 11 — DEFERRED FK PATCH BLOCKS
-- none: FIN's one live cross-module dependency (XM-FIN-001) is SOFT-READ, never a deferred
-- HARD-FK, and MDL is already gated regardless.
```

### 3.1 Self-verification (§7.2)
```
SYNTAX        ✓ S-1 every statement terminated; no trailing comma; every referenced object is created here
                or exists from v1 / MDL v1 (FIN_ACCOUNT, FIN_RULE_LINE, FIN_DIMENSION, FIN_DIMENSION_VALUE,
                FIN_JOURNAL_ENTRY, MDL_LOOKUP_TYPE, MDL_LOOKUP_VALUE, SEQ_MDL_LOOKUP_TYPE, SEQ_MDL_LOOKUP_VALUE)
              ✓ S-2 types: BIGINT, VARCHAR(n), BOOLEAN, TIMESTAMPTZ (+ v1's TEXT) — all in §4.1 / header
              ✓ S-3 constraints in ALTER TABLE form, named per §3; FKs after their parent PKs
              ✓ S-4 no PK trigger, no sequence DEFAULT on a PK column
              ✓ S-5 seed INSERTs carry column lists; no 'NULL' string; COMMIT after the DML
              ✓ S-6 n/a (no deferred FK) · ✓ S-7 no schema prefix anywhere · ✓ S-8 no placeholders
PK STRATEGY   ✓ 2 new tables → 2 SEQ_{TABLE} in BLOCK 1; PKs plain BIGINT NOT NULL; no identity clause
ORDER         ✓ sequences → altered parent → new children → comments → PK → UQ → CHK → FK → indexes → seed → COMMIT
COMPLETENESS  ✓ ENT-FIN-015, ENT-FIN-016 → tables; every SRS field → a DBF column; ENT-FIN-010's new field → DBF-FIN-148
              ✓ every v2 lookup change (2 types, 6 values, CHEQUE, DIRECT retirement) in BLOCK 8
              ✓ every new FK column and every SCR-REQ-FIN-013 filter column indexed (is_active_fl excepted, as in v1)
              ✓ shared lookup tables not re-created (MDL's)
MIGRATION     ✓ existing FIN_RULE_LINE rows satisfy CHK_FIN_RULE_LINE_DERIVATION_SPEC (value NOT NULL
                until now, new column NULL); no v1 object dropped or renamed
TRACE         ✓ every ADDED/MODIFIED DBF → ENT.field + REQ; XM-FIN-001 → REQ + SRS A8 LookupType/LookupValue row
```

## 4. DECISIONS APPLIED

| DEFAULT / ADR | What | Source | Override / status |
|---|---|---|---|
| ADR-FIN-028 | Names FIN_ACCOUNT_MAPPING / FIN_RULE_LINE_DIM; VARCHAR(50) for MDL codes, VARCHAR(100) for business_value; RULE-FIN-019 / RULE-FIN-026 as exclusive-or CHECKs naming no lookup code; RULE-FIN-022 as a partial unique index; idempotent BLOCK 8 seed into MDL tables; no data conversion | this stage — analysis/decisions/FIN/ADR-FIN-028.md | ACCEPTED (non-breaking) |
| ADR-FIN-001 (unchanged) | SEC/MDL dependencies are not XM rows except the one SOFT-READ; v2's new screen registration and lookup keys follow it | v1 P2 | ACCEPTED (non-breaking) — XM-FIN-001 widened, no new XM |
| ADR-FIN-010 (applied) | DIRECT removed → soft-retired in MDL (BLOCK 8); dimension tags use FIN_DIMENSION_VALUE_SOURCE | P0 v2 | RESOLVED-IN-DIALOGUE |
| ADR-FIN-013 (applied) | Business fields named by FIN_EVENT_BUSINESS_FIELD → the lookup-backed columns DBF-FIN-148, 151, 161 | P0 v2 | RESOLVED-IN-DIALOGUE |
| ADR-FIN-023 (applied) | MAPPING line names its field in account_business_field_code; account_derivation_value = constant account code only → DBF-FIN-102 NULL-able | P1 v2 | ACCEPTED |
| ADR-FIN-024 (applied) | Mapping references its account by id (FK to FIN_ACCOUNT); key columns fixed after create (application) | P1 v2 | ACCEPTED |
| ADR-FIN-025 (applied) | Tags follow their rule line → FIN_RULE_LINE_DIM carries no active flag and no update audit | P1 v2 | ACCEPTED |
| ADR-FIN-026 (applied) | BUSINESS_FIELD tag resolves by FIN_DIMENSION_VALUE.code within dimension_id — served by v1's UQ_FIN_DIMENSION_VALUE_DIM_CODE | P1 v2 | ACCEPTED |
| ADR-FIN-027 (applied) | Duplicate / period-not-open codes unchanged → no DDL; UQ_FIN_JOURNAL_ENTRY_EVENT_REF stays the store's uniqueness guarantee | P1 v2 | ACCEPTED |
| DEFAULT (v1, kept) | Lookup-backed columns are plain VARCHAR codes validated against MDL (XM-FIN-001), never CHECK-enumerated | v1 P2 | non-breaking |
| DEFAULT (v2) | AccountMapping soft-deleted via is_active_fl, never hard-deleted | [KB:erp-domain-standards §6] | hard delete with audit record |
| DEFAULT (v2) | The four audit columns on FIN_ACCOUNT_MAPPING (config kind); only created_at on FIN_RULE_LINE_DIM, as the SRS lists | [KB:erp-domain-standards §6]; SRS A3 | — |
| DEFAULT (v2) | Seed rows written with created_by = 'SYSTEM' (a principal string, never a numeric FK) | §3 AUDIT COLUMNS | any platform bootstrap principal |

No BLOCKED ADR — the pass was not stopped.

## 5. REGISTRY CONTENT
See `registry-db-fin.md`.

## 6. DBF id definitions — ADDED and MODIFIED in v2 (cross-reference index; `[traces]` = ENT + REQ)
**DBF-FIN-041** — FIN_JOURNAL_ENTRY.event_reference, MODIFIED v2 (traces: + REQ-FIN-072; no DDL) [ENT-FIN-004, REQ-FIN-010, REQ-FIN-011, REQ-FIN-072]
**DBF-FIN-101** — FIN_RULE_LINE.account_derivation_type_code, MODIFIED v2 (value set CONSTANT / MAPPING) [ENT-FIN-010, REQ-FIN-008, REQ-FIN-048]
**DBF-FIN-102** — FIN_RULE_LINE.account_derivation_value, MODIFIED v2 (NULL-able; constant account code only) [ENT-FIN-010, REQ-FIN-008, REQ-FIN-047, REQ-FIN-049]
**DBF-FIN-148** — FIN_RULE_LINE.account_business_field_code, ADDED v2 [ENT-FIN-010, REQ-FIN-049, REQ-FIN-050, REQ-FIN-051]
**DBF-FIN-149** — FIN_ACCOUNT_MAPPING.account_mapping_pk, ADDED v2 [ENT-FIN-015, REQ-FIN-054]
**DBF-FIN-150** — FIN_ACCOUNT_MAPPING.event_type_code, ADDED v2 [ENT-FIN-015, REQ-FIN-050, REQ-FIN-054, REQ-FIN-055, REQ-FIN-059]
**DBF-FIN-151** — FIN_ACCOUNT_MAPPING.business_field_code, ADDED v2 [ENT-FIN-015, REQ-FIN-050, REQ-FIN-054, REQ-FIN-055, REQ-FIN-059, REQ-FIN-061]
**DBF-FIN-152** — FIN_ACCOUNT_MAPPING.business_value, ADDED v2 [ENT-FIN-015, REQ-FIN-050, REQ-FIN-053, REQ-FIN-054, REQ-FIN-055, REQ-FIN-059, REQ-FIN-061]
**DBF-FIN-153** — FIN_ACCOUNT_MAPPING.account_id, ADDED v2 [ENT-FIN-015, ENT-FIN-001, REQ-FIN-050, REQ-FIN-052, REQ-FIN-054, REQ-FIN-056, REQ-FIN-060]
**DBF-FIN-154** — FIN_ACCOUNT_MAPPING.is_active_fl, ADDED v2 [ENT-FIN-015, REQ-FIN-053, REQ-FIN-055, REQ-FIN-057, REQ-FIN-059]
**DBF-FIN-155** — FIN_ACCOUNT_MAPPING.created_by, ADDED v2 [ENT-FIN-015, REQ-FIN-054]
**DBF-FIN-156** — FIN_ACCOUNT_MAPPING.created_at, ADDED v2 [ENT-FIN-015, REQ-FIN-054]
**DBF-FIN-157** — FIN_ACCOUNT_MAPPING.updated_by, ADDED v2 [ENT-FIN-015, REQ-FIN-056, REQ-FIN-057]
**DBF-FIN-158** — FIN_ACCOUNT_MAPPING.updated_at, ADDED v2 [ENT-FIN-015, REQ-FIN-056, REQ-FIN-057]
**DBF-FIN-159** — FIN_RULE_LINE_DIM.rule_line_dimension_pk, ADDED v2 [ENT-FIN-016, REQ-FIN-062]
**DBF-FIN-160** — FIN_RULE_LINE_DIM.value_source_code, ADDED v2 [ENT-FIN-016, REQ-FIN-062, REQ-FIN-063, REQ-FIN-065]
**DBF-FIN-161** — FIN_RULE_LINE_DIM.business_field_code, ADDED v2 [ENT-FIN-016, REQ-FIN-062, REQ-FIN-063, REQ-FIN-065, REQ-FIN-068, REQ-FIN-069]
**DBF-FIN-162** — FIN_RULE_LINE_DIM.rule_line_id, ADDED v2 [ENT-FIN-016, ENT-FIN-010, REQ-FIN-062, REQ-FIN-064]
**DBF-FIN-163** — FIN_RULE_LINE_DIM.dimension_id, ADDED v2 [ENT-FIN-016, ENT-FIN-002, REQ-FIN-062, REQ-FIN-064, REQ-FIN-066]
**DBF-FIN-164** — FIN_RULE_LINE_DIM.dimension_value_id, ADDED v2 [ENT-FIN-016, ENT-FIN-003, REQ-FIN-062, REQ-FIN-063, REQ-FIN-065]
**DBF-FIN-165** — FIN_RULE_LINE_DIM.created_at, ADDED v2 [ENT-FIN-016, REQ-FIN-062]

## 7. XM id definitions — MODIFIED in v2
**XM-FIN-001** — SOFT-READ every FIN lookup-backed column (v2: + FIN_RULE_LINE.account_business_field_code, FIN_ACCOUNT_MAPPING.event_type_code / business_field_code / business_value, FIN_RULE_LINE_DIM.value_source_code / business_field_code; 15 keys) → MDL_LOOKUP_VALUE, MODIFIED v2 [REQ-FIN-001, REQ-FIN-007, REQ-FIN-008, REQ-FIN-010, REQ-FIN-014, REQ-FIN-018, REQ-FIN-022, REQ-FIN-025, REQ-FIN-031, REQ-FIN-045, REQ-FIN-048, REQ-FIN-049, REQ-FIN-054, REQ-FIN-061, REQ-FIN-062]
══════════════════════════════════════════════════════════════════
