# BACKEND EXECUTION PLAN — الحسابات العامة / Finance (General Ledger) (FIN)
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v1   Profile : erp   Dialect : postgresql16
Framework : spring-boot-java (profile.stack.backend.framework)
Inputs : srs (v1, PRD-approved), db-script (v1), registry-srs (v1), registry-db (v1)
Open ADRs : 1 — erp/decisions/FIN/ (ADR-FIN-001, carried from P2; no new ADR this stage)
══════════════════════════════════════════════════════════════════

## PRE-GENERATION EXTRACTION — FIN v1 (working set; not part of the plan proper)

```
── FROM srs ──────────────────────────────────────────────────────────────
ENTITIES      14 — ENT-FIN-001..014 (master/config/lookup/transactional per SRS A3)
REQUIREMENTS  46 — REQ-FIN-001..046, each with 1 AC-FIN-*
RULES         17 — RULE-FIN-001..017 (RULE-FIN-017, fiscal-year/period/docDate coherence,
              added during SVC-API and carried back into srs-fin.md §A5), all 14 §12
              must-honor points covered (see ALIGN)
SCREENS       12 — SCR-REQ-FIN-001..012
PERMISSIONS   12 secured page codes + PERM_<PAGE_CODE>_<ACTION>, gateway VIEW, plus the
              custom `PERM_FIN_PERIODS_CLOSE_APPROVE` (RULE-FIN-015 SoD)
LOOKUPS       13 keys, all FIN-owned, registered into MDL (SRS A6) — none CHECK-constrained
              locally (unlike SEC's ADR-SEC-001; MDL is already gated)
BUSINESS CODE JournalEntry.docNo — system-generated `JV-{fiscalYearCode}-{NNNNNN}`
              (e.g. JV-2026-000123) by a FIN-local generator in com.erp.fin; counter
              scoped per fiscalYearId, restarts at 000001 each fiscal year, single
              counter across all journal types; unique per fiscalYearId
              (UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO), read-only after create
── FROM db-script ────────────────────────────────────────────────────────
TABLES        14 tables, FIN_ACCOUNT … FIN_ALLOCATION_TARGET
PK GENERATION every table: an explicit `SEQ_<TABLE>` sequence (V22 BLOCK 1) — a deliberate,
              documented deviation from db-script's `GENERATED ALWAYS AS IDENTITY`, required by
              build-create-entity A.1.3/A.1.4 and matching SEC/MDL/CU/NOTIF/FILE
COLUMNS       147 DBF-FIN-001..147
CONSTRAINTS   PK_*, UQ_*, CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE (POL-FIN-005), FK_*; INDEXES IDX_*
XM            2 — XM-FIN-001 SOFT-READ → MDL_LOOKUP_VALUE, status ACTIVE; XM-FIN-002
              READ → SEC user directory (SecUserDirectoryApi), status ACTIVE — assigned at
              ALIGN-BE, after SEC-BE introduced the RULE-FIN-015 read
── FROM registries ───────────────────────────────────────────────────────
SHARED ENTITIES CONSUMED   LookupType/LookupValue (MDL) via XM-FIN-001; SEC's user→permission
                           read model (SecUserDirectoryApi) via XM-FIN-002; SEC's identity/
                           authorization for every request, and FIN's self-registration into
                           SEC, consumed as platform-standard integration, not an XM row
                           (ADR-FIN-001 — unchanged)
EXISTING LOOKUP KEYS        none reused — all 13 are new, FIN-owned
ID RANGES already used      API: none yet · QR: none yet
──────────────────────────────────────────────────────────────────────────
No row required §2A.3 extraction-failure handling.
```

## EXECUTION PLAN INDEX — FIN v1 — backend-execution-plan-fin.md
Profile: erp · dialect: postgresql16 · framework: spring-boot-java
Open ADRs: 1 — decisions/FIN/ (ADR-FIN-001, non-breaking, carried from P2)

**ENTITY REGISTRY**
| ENT | Name | Table | Business code | Operations |
|---|---|---|---|---|
| ENT-FIN-001 | Account | FIN_ACCOUNT | none | create, read, search, update, deactivate |
| ENT-FIN-002 | Dimension | FIN_DIMENSION | none | create, read, search, deactivate |
| ENT-FIN-003 | DimensionValue | FIN_DIMENSION_VALUE | none | create, read, search, deactivate |
| ENT-FIN-004 | JournalEntry | FIN_JOURNAL_ENTRY | **docNo** (`JV-{fiscalYearCode}-{NNNNNN}`, FIN-local generator) | create (4 sources), read, search, reverse |
| ENT-FIN-005 | JournalLine | FIN_JOURNAL_LINE | none | create (with header), read |
| ENT-FIN-006 | JournalLineDimension | FIN_JOURNAL_LINE_DIM | none | create (with line), read |
| ENT-FIN-007 | FiscalYear | FIN_FISCAL_YEAR | none | create, read, search |
| ENT-FIN-008 | FiscalPeriod | FIN_FISCAL_PERIOD | none | create, read, search, open, soft-close, hard-close |
| ENT-FIN-009 | EventTypeRule | FIN_EVENT_TYPE_RULE | none | create, read, search, update, deactivate |
| ENT-FIN-010 | RuleLine | FIN_RULE_LINE | none | create, read, update, delete |
| ENT-FIN-011 | RecurringTemplate | FIN_RECURRING_TEMPLATE | none | create, read, search, update, deactivate |
| ENT-FIN-012 | RecurringTemplateLine | FIN_RECURRING_TEMPLATE_LINE | none | create, read, update, delete |
| ENT-FIN-013 | AllocationRule | FIN_ALLOCATION_RULE | none | create, read, search, update, deactivate |
| ENT-FIN-014 | AllocationTarget | FIN_ALLOCATION_TARGET | none | create, read, update, delete |

**FIELD REGISTRY** — see DB Alignment Manifest below (147 rows; property = camelCase of the
db-script column, per the same 1:1 transformation used for SEC/MDL — not restated as a
separate lighter table here given the row count, per this stage's own economy: the
Manifest already carries property/type/status, and read-only is uniformly: **Yes** for
every PK, every audit column, every system-timestamp column (postedAt, closedAt/closedBy,
createdAt-only child rows); **No** for every business-input field named in each entity's
SRS A3 "Required" column. The DB Alignment Manifest is the single canonical binding.

**API REGISTRY**
| API | Operation | Verb | Path | Traces (REQ) |
|---|---|---|---|---|
| API-FIN-001 | search accounts | POST | /api/v1/fin/accounts/search | REQ-FIN-001 |
| API-FIN-002 | create account | POST | /api/v1/fin/accounts | REQ-FIN-001, REQ-FIN-002 |
| API-FIN-003 | update account | PUT | /api/v1/fin/accounts/{id} | REQ-FIN-002 |
| API-FIN-004 | deactivate account | PUT | /api/v1/fin/accounts/{id}/deactivate | REQ-FIN-003 |
| API-FIN-005 | search dimensions | POST | /api/v1/fin/dimensions/search | REQ-FIN-004 |
| API-FIN-006 | create dimension | POST | /api/v1/fin/dimensions | REQ-FIN-004 |
| API-FIN-007 | create dimension value | POST | /api/v1/fin/dimensions/{id}/values | REQ-FIN-005, REQ-FIN-006 |
| API-FIN-008 | search dimension values | POST | /api/v1/fin/dimensions/values/search | REQ-FIN-005 |
| API-FIN-009 | search event-type rules | POST | /api/v1/fin/event-rules/search | REQ-FIN-007 |
| API-FIN-010 | create event-type rule | POST | /api/v1/fin/event-rules | REQ-FIN-007 |
| API-FIN-011 | add rule line | POST | /api/v1/fin/event-rules/{id}/lines | REQ-FIN-008, REQ-FIN-009 |
| API-FIN-012 | search templates | POST | /api/v1/fin/recurring-templates/search | REQ-FIN-022 |
| API-FIN-013 | create template | POST | /api/v1/fin/recurring-templates | REQ-FIN-022 |
| API-FIN-014 | run template | POST | /api/v1/fin/recurring-templates/{id}/run | REQ-FIN-023, REQ-FIN-024 |
| API-FIN-015 | search allocation rules | POST | /api/v1/fin/allocation-rules/search | REQ-FIN-025 |
| API-FIN-016 | create allocation rule | POST | /api/v1/fin/allocation-rules | REQ-FIN-025 |
| API-FIN-017 | run allocation rule | POST | /api/v1/fin/allocation-rules/{id}/run | REQ-FIN-026 |
| API-FIN-018 | search journal entries | POST | /api/v1/fin/journal-entries/search | REQ-FIN-027 |
| API-FIN-019 | create manual entry | POST | /api/v1/fin/journal-entries | REQ-FIN-014, REQ-FIN-015, REQ-FIN-017, REQ-FIN-018..021 |
| API-FIN-020 | build event entry (system) | POST | /api/v1/fin/journal-entries/from-event | REQ-FIN-010..013, REQ-FIN-017..021 |
| API-FIN-021 | reverse entry | POST | /api/v1/fin/journal-entries/{id}/reverse | REQ-FIN-028, REQ-FIN-029, REQ-FIN-030 |
| API-FIN-022 | read entry | GET | /api/v1/fin/journal-entries/{id} | REQ-FIN-016, REQ-FIN-027 |
| API-FIN-023 | create fiscal year | POST | /api/v1/fin/fiscal-years | REQ-FIN-031 |
| API-FIN-024 | open period | PATCH | /api/v1/fin/fiscal-periods/{id}/open | REQ-FIN-032 |
| API-FIN-025 | soft-close period | PATCH | /api/v1/fin/fiscal-periods/{id}/soft-close | REQ-FIN-033 |
| API-FIN-026 | hard-close period (approval) | PATCH | /api/v1/fin/fiscal-periods/{id}/hard-close | REQ-FIN-034, REQ-FIN-035, REQ-FIN-037, REQ-FIN-038 |
| API-FIN-027 | run year-end close | POST | /api/v1/fin/fiscal-years/{id}/year-end-close | REQ-FIN-036 |
| API-FIN-028 | account ledger | GET | /api/v1/fin/reports/account-ledger | REQ-FIN-039, REQ-FIN-046 |
| API-FIN-029 | trial balance | GET | /api/v1/fin/reports/trial-balance | REQ-FIN-040, REQ-FIN-046 |
| API-FIN-030 | balance sheet | GET | /api/v1/fin/reports/balance-sheet | REQ-FIN-041, REQ-FIN-046 |
| API-FIN-031 | income statement | GET | /api/v1/fin/reports/income-statement | REQ-FIN-042, REQ-FIN-046 |
| API-FIN-032 | dimension report | GET | /api/v1/fin/reports/dimension | REQ-FIN-043 |

**RULE REGISTRY** — all 16 SRS rules; full text: srs-fin.md §A5, cited by id in each API's
Validations line below (never restated in full here — single-source rule).

**SCREEN REGISTRY** — 12 secured screens; see SEC-BE (Phase 7) below.

**LOOKUP REGISTRY** — 13 keys; see SRS A6 (not restated).

**QRC SUMMARY** — 49 QR ids, QR-FIN-001..049 (045-049 assigned at ALIGN-BE to queries that
were implemented but uncatalogued) — see Query Reference Catalog below.

**DB ALIGNMENT** — see manifest below — ALIGNED ✓ / issues: 0
**XM STATUS** — 2 (XM-FIN-001, SOFT-READ → MDL, ACTIVE; XM-FIN-002, READ → SEC, ACTIVE)
**SECURITY** — 12 secured screens, data-driven role grants + 1 custom SoD-gated permission

## DB Alignment Manifest — FIN v1
All 147 rows: **status ✓ (aligned)**; XM populated only on lookup-backed columns (16
columns across 10 tables touch XM-FIN-001, noted individually below — a SOFT-READ, never
blocking; the count read 12/8 until ALIGN-BE recounted it against V22's 16 XM-FIN-001
`COMMENT ON COLUMN` lines). XM-FIN-002 binds no column — it is a service-to-service read of
SEC's user directory, not a lookup-backed value — so it carries no row in this manifest.

| DBF | ENT | property | type | XM |
|---|---|---|---|---|
| DBF-FIN-001 | ENT-FIN-001 | accountPk | Long | — |
| DBF-FIN-002 | ENT-FIN-001 | code | String | — |
| DBF-FIN-003 | ENT-FIN-001 | nameAr | String | — |
| DBF-FIN-004 | ENT-FIN-001 | nameEn | String | — |
| DBF-FIN-005 | ENT-FIN-001 | accountTypeCode | String | XM-FIN-001 |
| DBF-FIN-006 | ENT-FIN-001 | natureCode | String | XM-FIN-001 |
| DBF-FIN-007 | ENT-FIN-001 | parentAccountId | Long | — |
| DBF-FIN-008 | ENT-FIN-001 | isLeafFl | Boolean | — |
| DBF-FIN-009 | ENT-FIN-001 | isActiveFl | Boolean | — |
| DBF-FIN-010 | ENT-FIN-001 | createdBy | String | — |
| DBF-FIN-011 | ENT-FIN-001 | createdAt | Instant | — |
| DBF-FIN-012 | ENT-FIN-001 | updatedBy | String | — |
| DBF-FIN-013 | ENT-FIN-001 | updatedAt | Instant | — |
| DBF-FIN-014 | ENT-FIN-002 | dimensionPk | Long | — |
| DBF-FIN-015 | ENT-FIN-002 | code | String | — |
| DBF-FIN-016 | ENT-FIN-002 | nameAr | String | — |
| DBF-FIN-017 | ENT-FIN-002 | nameEn | String | — |
| DBF-FIN-018 | ENT-FIN-002 | isActiveFl | Boolean | — |
| DBF-FIN-019 | ENT-FIN-002 | createdBy | String | — |
| DBF-FIN-020 | ENT-FIN-002 | createdAt | Instant | — |
| DBF-FIN-021 | ENT-FIN-002 | updatedBy | String | — |
| DBF-FIN-022 | ENT-FIN-002 | updatedAt | Instant | — |
| DBF-FIN-023 | ENT-FIN-003 | dimensionValuePk | Long | — |
| DBF-FIN-024 | ENT-FIN-003 | dimensionId | Long | — |
| DBF-FIN-025 | ENT-FIN-003 | code | String | — |
| DBF-FIN-026 | ENT-FIN-003 | nameAr | String | — |
| DBF-FIN-027 | ENT-FIN-003 | nameEn | String | — |
| DBF-FIN-028 | ENT-FIN-003 | sortOrder | Integer | — |
| DBF-FIN-029 | ENT-FIN-003 | isActiveFl | Boolean | — |
| DBF-FIN-030 | ENT-FIN-003 | createdBy | String | — |
| DBF-FIN-031 | ENT-FIN-003 | createdAt | Instant | — |
| DBF-FIN-032 | ENT-FIN-003 | updatedBy | String | — |
| DBF-FIN-033 | ENT-FIN-003 | updatedAt | Instant | — |
| DBF-FIN-034 | ENT-FIN-004 | journalEntryPk | Long | — |
| DBF-FIN-035 | ENT-FIN-004 | docNo | String | — |
| DBF-FIN-036 | ENT-FIN-004 | docDate | LocalDate | — |
| DBF-FIN-037 | ENT-FIN-004 | fiscalYearId | Long | — |
| DBF-FIN-038 | ENT-FIN-004 | periodId | Long | — |
| DBF-FIN-039 | ENT-FIN-004 | journalTypeCode | String | XM-FIN-001 |
| DBF-FIN-040 | ENT-FIN-004 | statusCode | String | XM-FIN-001 |
| DBF-FIN-041 | ENT-FIN-004 | eventReference | String | — |
| DBF-FIN-042 | ENT-FIN-004 | originalEntryId | Long | — |
| DBF-FIN-043 | ENT-FIN-004 | reversalEntryId | Long | — |
| DBF-FIN-044 | ENT-FIN-004 | descriptionAr | String | — |
| DBF-FIN-045 | ENT-FIN-004 | descriptionEn | String | — |
| DBF-FIN-046 | ENT-FIN-004 | postedAt | Instant | — |
| DBF-FIN-047 | ENT-FIN-004 | createdBy | String | — |
| DBF-FIN-048 | ENT-FIN-004 | createdAt | Instant | — |
| DBF-FIN-049 | ENT-FIN-004 | updatedBy | String | — |
| DBF-FIN-050 | ENT-FIN-004 | updatedAt | Instant | — |
| DBF-FIN-051 | ENT-FIN-005 | journalLinePk | Long | — |
| DBF-FIN-052 | ENT-FIN-005 | journalEntryId | Long | — |
| DBF-FIN-053 | ENT-FIN-005 | lineNo | Integer | — |
| DBF-FIN-054 | ENT-FIN-005 | accountId | Long | — |
| DBF-FIN-055 | ENT-FIN-005 | amount | BigDecimal | — |
| DBF-FIN-056 | ENT-FIN-005 | directionCode | String | XM-FIN-001 |
| DBF-FIN-057 | ENT-FIN-005 | isRemainderFl | Boolean | — |
| DBF-FIN-058 | ENT-FIN-005 | descriptionAr | String | — |
| DBF-FIN-059 | ENT-FIN-005 | descriptionEn | String | — |
| DBF-FIN-060 | ENT-FIN-005 | createdAt | Instant | — |
| DBF-FIN-061 | ENT-FIN-006 | journalLineDimensionPk | Long | — |
| DBF-FIN-062 | ENT-FIN-006 | journalLineId | Long | — |
| DBF-FIN-063 | ENT-FIN-006 | dimensionId | Long | — |
| DBF-FIN-064 | ENT-FIN-006 | dimensionValueId | Long | — |
| DBF-FIN-065 | ENT-FIN-007 | fiscalYearPk | Long | — |
| DBF-FIN-066 | ENT-FIN-007 | code | String | — |
| DBF-FIN-067 | ENT-FIN-007 | startDate | LocalDate | — |
| DBF-FIN-068 | ENT-FIN-007 | endDate | LocalDate | — |
| DBF-FIN-069 | ENT-FIN-007 | statusCode | String | XM-FIN-001 |
| DBF-FIN-070 | ENT-FIN-007 | isActiveFl | Boolean | — |
| DBF-FIN-071 | ENT-FIN-007 | createdBy | String | — |
| DBF-FIN-072 | ENT-FIN-007 | createdAt | Instant | — |
| DBF-FIN-073 | ENT-FIN-007 | updatedBy | String | — |
| DBF-FIN-074 | ENT-FIN-007 | updatedAt | Instant | — |
| DBF-FIN-075 | ENT-FIN-008 | fiscalPeriodPk | Long | — |
| DBF-FIN-076 | ENT-FIN-008 | fiscalYearId | Long | — |
| DBF-FIN-077 | ENT-FIN-008 | periodNo | Integer | — |
| DBF-FIN-078 | ENT-FIN-008 | nameAr | String | — |
| DBF-FIN-079 | ENT-FIN-008 | nameEn | String | — |
| DBF-FIN-080 | ENT-FIN-008 | startDate | LocalDate | — |
| DBF-FIN-081 | ENT-FIN-008 | endDate | LocalDate | — |
| DBF-FIN-082 | ENT-FIN-008 | statusCode | String | XM-FIN-001 |
| DBF-FIN-083 | ENT-FIN-008 | closedBy | String | — |
| DBF-FIN-084 | ENT-FIN-008 | closedAt | Instant | — |
| DBF-FIN-085 | ENT-FIN-008 | createdBy | String | — |
| DBF-FIN-086 | ENT-FIN-008 | createdAt | Instant | — |
| DBF-FIN-087 | ENT-FIN-008 | updatedBy | String | — |
| DBF-FIN-088 | ENT-FIN-008 | updatedAt | Instant | — |
| DBF-FIN-089 | ENT-FIN-009 | eventTypeRulePk | Long | — |
| DBF-FIN-090 | ENT-FIN-009 | eventTypeCode | String | XM-FIN-001 |
| DBF-FIN-091 | ENT-FIN-009 | nameAr | String | — |
| DBF-FIN-092 | ENT-FIN-009 | nameEn | String | — |
| DBF-FIN-093 | ENT-FIN-009 | isActiveFl | Boolean | — |
| DBF-FIN-094 | ENT-FIN-009 | createdBy | String | — |
| DBF-FIN-095 | ENT-FIN-009 | createdAt | Instant | — |
| DBF-FIN-096 | ENT-FIN-009 | updatedBy | String | — |
| DBF-FIN-097 | ENT-FIN-009 | updatedAt | Instant | — |
| DBF-FIN-098 | ENT-FIN-010 | ruleLinePk | Long | — |
| DBF-FIN-099 | ENT-FIN-010 | eventTypeRuleId | Long | — |
| DBF-FIN-100 | ENT-FIN-010 | lineNo | Integer | — |
| DBF-FIN-101 | ENT-FIN-010 | accountDerivationTypeCode | String | XM-FIN-001 |
| DBF-FIN-102 | ENT-FIN-010 | accountDerivationValue | String | — |
| DBF-FIN-103 | ENT-FIN-010 | amountSourceTypeCode | String | XM-FIN-001 |
| DBF-FIN-104 | ENT-FIN-010 | amountSourceValue | String | — |
| DBF-FIN-105 | ENT-FIN-010 | directionCode | String | XM-FIN-001 |
| DBF-FIN-106 | ENT-FIN-010 | distributionTypeCode | String | XM-FIN-001 |
| DBF-FIN-107 | ENT-FIN-010 | isRemainderFl | Boolean | — |
| DBF-FIN-108 | ENT-FIN-010 | createdAt | Instant | — |
| DBF-FIN-109 | ENT-FIN-011 | recurringTemplatePk | Long | — |
| DBF-FIN-110 | ENT-FIN-011 | nameAr | String | — |
| DBF-FIN-111 | ENT-FIN-011 | nameEn | String | — |
| DBF-FIN-112 | ENT-FIN-011 | scheduleTypeCode | String | XM-FIN-001 |
| DBF-FIN-113 | ENT-FIN-011 | frequencyCode | String | XM-FIN-001 |
| DBF-FIN-114 | ENT-FIN-011 | startDate | LocalDate | — |
| DBF-FIN-115 | ENT-FIN-011 | nextRunDate | LocalDate | — |
| DBF-FIN-116 | ENT-FIN-011 | endDate | LocalDate | — |
| DBF-FIN-117 | ENT-FIN-011 | isActiveFl | Boolean | — |
| DBF-FIN-118 | ENT-FIN-011 | createdBy | String | — |
| DBF-FIN-119 | ENT-FIN-011 | createdAt | Instant | — |
| DBF-FIN-120 | ENT-FIN-011 | updatedBy | String | — |
| DBF-FIN-121 | ENT-FIN-011 | updatedAt | Instant | — |
| DBF-FIN-122 | ENT-FIN-012 | recurringTemplateLinePk | Long | — |
| DBF-FIN-123 | ENT-FIN-012 | recurringTemplateId | Long | — |
| DBF-FIN-124 | ENT-FIN-012 | lineNo | Integer | — |
| DBF-FIN-125 | ENT-FIN-012 | accountId | Long | — |
| DBF-FIN-126 | ENT-FIN-012 | amount | BigDecimal | — |
| DBF-FIN-127 | ENT-FIN-012 | directionCode | String | XM-FIN-001 |
| DBF-FIN-128 | ENT-FIN-012 | dimensionValueId | Long | — |
| DBF-FIN-129 | ENT-FIN-012 | createdAt | Instant | — |
| DBF-FIN-130 | ENT-FIN-013 | allocationRulePk | Long | — |
| DBF-FIN-131 | ENT-FIN-013 | nameAr | String | — |
| DBF-FIN-132 | ENT-FIN-013 | nameEn | String | — |
| DBF-FIN-133 | ENT-FIN-013 | sourceAccountId | Long | — |
| DBF-FIN-134 | ENT-FIN-013 | isActiveFl | Boolean | — |
| DBF-FIN-135 | ENT-FIN-013 | createdBy | String | — |
| DBF-FIN-136 | ENT-FIN-013 | createdAt | Instant | — |
| DBF-FIN-137 | ENT-FIN-013 | updatedBy | String | — |
| DBF-FIN-138 | ENT-FIN-013 | updatedAt | Instant | — |
| DBF-FIN-139 | ENT-FIN-014 | allocationTargetPk | Long | — |
| DBF-FIN-140 | ENT-FIN-014 | allocationRuleId | Long | — |
| DBF-FIN-141 | ENT-FIN-014 | lineNo | Integer | — |
| DBF-FIN-142 | ENT-FIN-014 | targetAccountId | Long | — |
| DBF-FIN-143 | ENT-FIN-014 | dimensionValueId | Long | — |
| DBF-FIN-144 | ENT-FIN-014 | distributionTypeCode | String | XM-FIN-001 |
| DBF-FIN-145 | ENT-FIN-014 | distributionValue | BigDecimal | — |
| DBF-FIN-146 | ENT-FIN-014 | isRemainderFl | Boolean | — |
| DBF-FIN-147 | ENT-FIN-001 | isRetainedEarningsFl | Boolean | — |

## Query Reference Catalog (QR-FIN-*)

> Logical specification only — never executable code.

| QR | Operation | API | Entity | Kind | Intent |
|---|---|---|---|---|---|
| QR-FIN-001 | FIND_BY_CRITERIA | API-FIN-001 | ENT-FIN-001 | search | search accounts |
| QR-FIN-002 | SAVE | API-FIN-002 | ENT-FIN-001 | create | create account |
| QR-FIN-003 | UPDATE | API-FIN-003 | ENT-FIN-001 | update | update account |
| QR-FIN-004 | UPDATE | API-FIN-004 | ENT-FIN-001 | deactivate | deactivate account |
| QR-FIN-005 | EXISTS | API-FIN-002 | ENT-FIN-001 | uniqueness | account code unique |
| QR-FIN-006 | EXISTS | API-FIN-002, API-FIN-003 | ENT-FIN-001 | RULE-FIN-001 | account has no children before marking leaf |
| QR-FIN-007 | FIND_BY_CRITERIA | API-FIN-005 | ENT-FIN-002 | search | search dimensions |
| QR-FIN-008 | SAVE | API-FIN-006 | ENT-FIN-002 | create | create dimension |
| QR-FIN-009 | SAVE | API-FIN-007 | ENT-FIN-003 | create | create dimension value |
| QR-FIN-010 | EXISTS | API-FIN-007 | ENT-FIN-003 | RULE-FIN-002 | dimension value code unique within dimension |
| QR-FIN-011 | FIND_BY_CRITERIA | API-FIN-008 | ENT-FIN-003 | search | search dimension values |
| QR-FIN-012 | FIND_BY_CRITERIA | API-FIN-009 | ENT-FIN-009 | search | search event-type rules |
| QR-FIN-013 | SAVE | API-FIN-010 | ENT-FIN-009 | create | create event-type rule |
| QR-FIN-014 | EXISTS | API-FIN-010 | ENT-FIN-009 | uniqueness | one active rule per event type |
| QR-FIN-015 | SAVE | API-FIN-011 | ENT-FIN-010 | create | add rule line |
| QR-FIN-016 | EXISTS | API-FIN-011, API-FIN-016 | ENT-FIN-010, ENT-FIN-014 | RULE-FIN-003 | exactly one remainder line/target when a percentage distribution exists (scope widened here to match the id definition below and the code) |
| QR-FIN-017 | FIND_BY_CRITERIA | API-FIN-012 | ENT-FIN-011 | search | search templates |
| QR-FIN-018 | SAVE | API-FIN-013 | ENT-FIN-011, ENT-FIN-012 | create | create template with lines |
| QR-FIN-019 | FIND_ONE | API-FIN-014 | ENT-FIN-011 | run | load the template to run, by id (inherited `findById`; no due-date selection query exists — FIN exposes no scheduler endpoint) |
| QR-FIN-020 | FIND_BY_CRITERIA | API-FIN-015 | ENT-FIN-013 | search | search allocation rules |
| QR-FIN-021 | SAVE | API-FIN-016 | ENT-FIN-013, ENT-FIN-014 | create | create allocation rule with targets |
| QR-FIN-022 | FIND_ONE | API-FIN-017 | ENT-FIN-013 | run | load allocation rule + current source balance (via QR-FIN-043 — the shared balance aggregation; the earlier citation of QR-FIN-040, the hard-closed-period check, was wrong) |
| QR-FIN-023 | FIND_BY_CRITERIA | API-FIN-018 | ENT-FIN-004 | search | search journal entries |
| QR-FIN-024 | SAVE | API-FIN-019 | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | create | build manual entry (DRAFT) |
| QR-FIN-025 | SAVE | API-FIN-020 | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | create | build event entry (DRAFT) from rule |
| QR-FIN-026 | EXISTS | API-FIN-020 | ENT-FIN-004 | RULE-FIN-004 | duplicate eventReference |
| QR-FIN-027 | EXISTS | API-FIN-020 | ENT-FIN-009 | RULE-FIN-005 | active rule exists for event type |
| QR-FIN-028 | AGGREGATE | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017 | ENT-FIN-005 | RULE-FIN-010 | compute remainder-line amount for a compound/percentage distribution |
| QR-FIN-029 | EXISTS | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017 | ENT-FIN-005 | RULE-FIN-006 | debits = credits |
| QR-FIN-030 | EXISTS | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017 | ENT-FIN-001 | RULE-FIN-007 | every line's account is leaf + active |
| QR-FIN-031 | EXISTS | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017 | ENT-FIN-008 | RULE-FIN-008 | entry's period is Open at post time (not applied to API-FIN-027's CLOSING/OPENING entries — exempted by RULE-FIN-008 itself) |
| QR-FIN-032 | EXISTS | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017 | ENT-FIN-006 | RULE-FIN-009 | every dimension value valid + active |
| QR-FIN-033 | UPDATE | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017 | ENT-FIN-004 | post | flip DRAFT → POSTED, set postedAt (RULE-FIN-016 lock takes effect) |
| QR-FIN-034 | SAVE | API-FIN-021 | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | create | build reversal entry (RULE-FIN-011) |
| QR-FIN-035 | EXISTS | API-FIN-021, 014 | ENT-FIN-004 | RULE-FIN-013 | original entry is POSTED and not already reversed |
| QR-FIN-036 | FIND_ONE | API-FIN-021 | ENT-FIN-008 | RULE-FIN-012 | current open period, if original's is closed |
| QR-FIN-037 | FIND_ONE | API-FIN-022 | ENT-FIN-004 | read | read one entry with lines |
| QR-FIN-038 | SAVE | API-FIN-023 | ENT-FIN-007, ENT-FIN-008 | create | create fiscal year + periods |
| QR-FIN-039 | UPDATE | API-FIN-024, API-FIN-025, API-FIN-026 | ENT-FIN-008 | transition | open / soft-close / hard-close a period |
| QR-FIN-040 | EXISTS | API-FIN-026 | ENT-FIN-008 | RULE-FIN-014 | period not already Hard Closed before reopen attempts |
| QR-FIN-041 | AGGREGATE | API-FIN-027 | ENT-FIN-004, ENT-FIN-005 | year-end | compute closing balances, build closing + opening entries |
| QR-FIN-042 | FIND_BY_CRITERIA | API-FIN-028 | ENT-FIN-005 | report | account ledger — POSTED lines for one account, oldest first; the running balance is accumulated by the service, deliberately not returned by the query |
| QR-FIN-043 | AGGREGATE | API-FIN-029, API-FIN-030, API-FIN-031 | ENT-FIN-005 | report | trial balance / balance sheet / income statement account balances (shared aggregation, filtered per report by accountTypeCode) |
| QR-FIN-044 | AGGREGATE | API-FIN-032 | ENT-FIN-005, ENT-FIN-006 | report | dimension report (account × dimension value) |
| QR-FIN-045 | FIND_ONE | API-FIN-027 | ENT-FIN-001 | year-end | the single Retained Earnings account (DBF-FIN-147), lowest pk first |
| QR-FIN-046 | EXISTS | API-FIN-006 | ENT-FIN-002 | uniqueness | dimension code unique |
| QR-FIN-047 | EXISTS | API-FIN-023 | ENT-FIN-007 | uniqueness | fiscal year code unique |
| QR-FIN-048 | FIND_ONE | API-FIN-027 | ENT-FIN-007 | year-end | the next fiscal year, found by its startDate = this year's endDate + 1 day (the opening entry's target) |
| QR-FIN-049 | FIND_BY_CRITERIA | API-FIN-023, API-FIN-027, API-FIN-014 | ENT-FIN-008 | read | every period of one fiscal year, JOIN FETCH on the year, ordered by periodNo |

Join governance: every posting-pipeline QR (024, 025, 028-033) is intra-module only
(FIN_JOURNAL_ENTRY/LINE/LINE_DIM/ACCOUNT/FISCAL_PERIOD, all owned here); the one
cross-module read (QR for lookup-code validation, folded into QR-FIN-002/013/015/024/025/
etc.'s "Validations" step, per XM-FIN-001) is a separate in-process `MdlLookupApi` call,
never a SQL join. No
QR joins to resolve a lookup label — every lookup-backed column returns its code; the
frontend resolves the display label via MDL.

---

<!-- PHASE:CORE:START traces=REQ-FIN-017 -->
## PHASE 1 — CORE

**Layers**: controller → service → mapper → domain → repository (`profile.stack.backend.layers`).
Domain-behaviour placement: **domain classes** — one dedicated `<Entity>Domain` per entity —
for every rule that answers "is this operation allowed?"
(`profile.conventions.domain_behaviour_placement: domain_classes`). The service layer is
never a placement: it orchestrates only (load → delegate → persist → return) and holds no
business-rule conditional of its own. How many entities or tables a rule reads is NOT a
placement criterion — the service fetches those facts and passes them into the domain class
as plain arguments, so a multi-entity rule sits in exactly the same place as a single-entity
one. The decision moves to the domain class while the entity keeps the plain state mutation:
`JournalEntryDomain.assertCanPost(...)` then `JournalEntry.post()`;
`FiscalPeriodDomain.assertCanHardClose(...)` then `FiscalPeriod.hardClose()`. FIN domain
classes: `AccountDomain` (RULE-FIN-001, 007), `DimensionValueDomain` (RULE-FIN-002, 009),
`EventTypeRuleDomain` (RULE-FIN-003, 005, 010), `JournalEntryDomain` (RULE-FIN-004, 006,
008, 011, 012, 013, 016), `FiscalPeriodDomain` (RULE-FIN-014, 015), `AllocationRuleDomain`
(RULE-FIN-003, 010 as they apply to allocation targets).

**Error signalling**: `LocalizedException → {code, messageAr, messageEn}`; runtime code
format `FIN-{http}[-{SLUG}]`.

**Transaction scope**: `READ_ONLY` for every `FIND_*`/`EXISTS`/`AGGREGATE` QR; `READ_WRITE`
for every `SAVE`/`UPDATE` QR. The build→validate→post sequence (QR-FIN-024/025 through
QR-FIN-033) runs in ONE transaction per entry — either the whole entry posts, or nothing is
written (no partially-built DRAFT survives a failed validation, per REQ-FIN-015/018-021).

**Search contract**: `{filters, page, size, sort}`, `Page<T>`, empty result = success.

**Audit fields**: `createdBy/createdAt/updatedBy/updatedAt` framework-filled; on
FIN_JOURNAL_ENTRY, `createdBy` records the entry-creator principal used by RULE-FIN-015's
distinct-permission check; `closedBy` on FIN_FISCAL_PERIOD records the period-close
approver principal, from a different principal-permission pair by construction.

**Type mapping** (postgresql16 → Java): identical table to SEC's/MDL's own Phase 1 CORE
(`GENERATED ALWAYS AS IDENTITY`→Long, `VARCHAR(n)`→String, `BOOLEAN`→Boolean,
`TIMESTAMPTZ`→Instant, `TEXT`→String, `DATE`→LocalDate, `NUMERIC(18,4)`→BigDecimal,
bare `NUMERIC` (line/period numbers, sort orders)→Integer, per the same governance note
MDL's plan already stated for its own `sort_order`).

**Lookup values**: all 13 FIN-owned lookup-backed columns are plain `String` holding the
code; validated at the service layer against MDL (XM-FIN-001) before any write — an
invalid code is rejected with a catalog error before it reaches the database.

**Numbering**: `docNo` format is `JV-{fiscalYearCode}-{NNNNNN}` — literal prefix `JV-`, the
owning fiscal year's `code` (DBF-FIN-066, VARCHAR(10)), `-`, then a zero-padded 6-digit
counter starting at `000001`; e.g. `JV-2026-000123` (worst case 20 chars, inside
`doc_no VARCHAR(30)`). The counter is scoped per `fiscalYearId` and restarts at `000001`
for each new fiscal year — one counter for all journal types, never segmented by
`journalTypeCode`; `UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO (fiscal_year_id, doc_no)` backs the
uniqueness guarantee at the database level. Generated by a FIN-local generator in
`com.erp.fin`, deliberately NOT a shared `com.erp.common` component: no other module needs
document numbering today, so a general shared interface would be speculative — promote it
to `common` only if and when a second real consumer appears. This is a conscious, recorded
deviation from the profile's standing guidance that numbering "comes from the platform
numbering engine; never generated in a module" — that guidance presumes an engine which
does not exist in this repo, making literal compliance impossible. Assigned once on create,
immutable thereafter — excluded entirely from every create/update request DTO, present
only in responses.

**Workflow engine**: forbidden — every lifecycle below (JournalEntry, FiscalPeriod) is a
plain guarded transition, never a workflow definition.

**Languages**: every name field and catalog message present in ar + en.

**Cross-module contract placement**: XM-FIN-001 (SOFT-READ → MDL) is implemented in FIN's
service layer by injecting MDL's published cross-module interface
`com.erp.mdl.crossmodule.MdlLookupApi` directly — in-process Spring interface injection
within the single deployable, never a loopback HTTP call to API-MDL-011. FIN calls
`readActiveValuesByKey(String typeKey)` and consumes the narrow read-model record
`com.erp.mdl.crossmodule.LookupOptionView` (code, labelAr, labelEn, sortOrder) — never a
`LookupValue` entity or an MDL-internal DTO; a submitted code is validated by membership in
that returned list. Same pattern as the delivered `NotificationLookupService` and
`FileLookupService`. FIN's own dependency on SEC for identity/authorization uses the identical
CORE-interceptor mechanism SEC's and MDL's own plans already declare — not redeclared here,
only cited (ADR-FIN-001).

**Cross-cutting authorization**: every secured API below is gated by the platform's CORE
interceptor (module/screen/action grant check) before its controller method runs, exactly
as SEC's own Phase 1 CORE describes; `PERM_FIN_PERIODS_CLOSE_APPROVE` (API-FIN-026/027) is
additionally required to be held by a role distinct from `PERM_FIN_JOURNAL_ENTRIES_CREATE`
(RULE-FIN-015) — this distinctness check itself runs in FIN's own service layer by reading
the two roles' user sets through SEC's role/grant read APIs, since SEC's interceptor alone
only proves "this caller holds permission X," not "no user holding X also holds Y."
<!-- PHASE:CORE:END -->

<!-- PHASE:DATA-DOM:START traces=REQ-FIN-001,REQ-FIN-010,REQ-FIN-014,REQ-FIN-031 -->
## PHASE 2 — DATA-DOM

Entity count is 14 (≥ threshold) — grouped below under `SUB:DATA-DOM-MASTER` (Account,
FiscalYear, FiscalPeriod — master-like reference data), `SUB:DATA-DOM-TRANSACTIONAL`
(JournalEntry, JournalLine, JournalLineDimension — the posting core), and
`SUB:DATA-DOM-LOOKUP` (Dimension, DimensionValue, EventTypeRule, RuleLine,
RecurringTemplate, RecurringTemplateLine, AllocationRule, AllocationTarget — FIN's own
config/lookup-kind structural data, grouped together here since the engine's three-way
split is by role, not literally the `lookup` kind alone).

<!-- SUB:DATA-DOM-MASTER:START traces=REQ-FIN-001,REQ-FIN-031 -->
### SUB — DATA-DOM-MASTER

#### ENT-FIN-001 — Account      kind: master
BINDINGS: table `FIN_ACCOUNT` · PK `accountPk` (DBF-FIN-001) · PK generation `GENERATED ALWAYS AS IDENTITY`
BUSINESS CODE: none (§3.3 test: no — `code` is client-chosen, not numbering-engine-generated)
FIELDS: DBF-FIN-001..013 + DBF-FIN-147 (`isRetainedEarningsFl`, REQ-FIN-036 — see API-FIN-027) — see DB Alignment Manifest; `accountTypeCode`/`natureCode` are
lookup-backed (XM-FIN-001).
DTO MEMBERSHIP: create-request excludes {accountPk, isActiveFl, audit}; update-request
excludes {accountPk, code, isActiveFl, audit} (code immutable, matching MDL's own `key`
precedent); response includes all.
LOOKUP FIELDS: `accountTypeCode`→`ACCOUNT_TYPE`, `natureCode`→`DEBIT_CREDIT`, both validated
in the service layer via injected `MdlLookupApi.readActiveValuesByKey(<key>)` (XM-FIN-001) —
stored as code, never a numeric FK.
DOMAIN RULES: **RULE-FIN-001** (full text: srs-fin.md §A5) — Scope ENT-FIN-001 · Trigger:
on update (`isLeafFl`) · DB enforcement: application layer (QR-FIN-006) · owner layer:
service.
STATE MACHINE: `isActiveFl` binary only — not applicable.
CROSS-MODULE: `accountTypeCode`/`natureCode` touch XM-FIN-001.
REPOSITORY OPS → QR-FIN-001, QR-FIN-002, QR-FIN-003, QR-FIN-004, QR-FIN-005, QR-FIN-006,
QR-FIN-045 (FIND_ONE the Retained Earnings account, DBF-FIN-147 — API-FIN-027).

#### ENT-FIN-007 — FiscalYear      kind: master
BINDINGS: table `FIN_FISCAL_YEAR` · PK `fiscalYearPk` (DBF-FIN-065) · PK generation `GENERATED ALWAYS AS IDENTITY`
BUSINESS CODE: none
FIELDS: DBF-FIN-065..074 — see DB Alignment Manifest; `statusCode` lookup-backed (XM-FIN-001).
DTO MEMBERSHIP: create-request `{code, startDate, endDate, periodCount}` (periodCount is a
transient input driving REQ-FIN-031's period-generation, not a persisted column); response
includes all persisted fields plus the generated periods.
DOMAIN RULES: **a CLOSED year is never closed again** — the year-end close's own re-run
guard, scoped to `statusCode` alone and therefore owned by `FiscalYearDomain` (ALIGN-BE;
`FIN-409-INVALID-TRANSITION`, reusing the catalog's existing invalid-transition row). Amends
this block's earlier "none scoped alone": the rest of year-end close (REQ-FIN-036) is still
orchestrated at the API layer over both FiscalYear and FiscalPeriod — see API-FIN-027 — and
this remains the entity's ONLY Domain object (A.0.7).
STATE MACHINE: `statusCode` (FISCAL_YEAR_STATUS) — OPEN→CLOSED, set once by REQ-FIN-036 —
binary, not applicable for a diagram (SRS A7).
CROSS-MODULE: `statusCode` touches XM-FIN-001.
REPOSITORY OPS → QR-FIN-038 (SAVE, with periods), QR-FIN-041 (AGGREGATE, year-end),
QR-FIN-047 (EXISTS, code unique), QR-FIN-048 (FIND_ONE, next year by startDate).

#### ENT-FIN-008 — FiscalPeriod      kind: master
BINDINGS: table `FIN_FISCAL_PERIOD` · PK `fiscalPeriodPk` (DBF-FIN-075) · PK generation `GENERATED ALWAYS AS IDENTITY`
BUSINESS CODE: none
FIELDS: DBF-FIN-075..088 — see DB Alignment Manifest; `statusCode` lookup-backed (XM-FIN-001).
DTO MEMBERSHIP: no direct create (generated with the year, REQ-FIN-031); transition
endpoints only (open/soft-close/hard-close) take just `{id}`; response includes all.
DOMAIN RULES:
**RULE-FIN-008** (srs-fin.md §A5) — period-open-at-post-time, except the year-end closing and
opening entries, which the rule itself exempts (REQ-FIN-036 / API-FIN-027) — DB enforcement:
application layer (QR-FIN-031) · owner layer: service.
**RULE-FIN-014** (srs-fin.md §A5) — reject reopening a Hard Closed period — DB enforcement:
application layer (QR-FIN-040) · owner layer: service.
**RULE-FIN-015** (srs-fin.md §A5) — close-approval permission distinct from entry-creation
permission — DB enforcement: application layer (service, reading SEC role/grant data,
Phase 1 CORE) · owner layer: service.
STATE MACHINE: `statusCode` (PERIOD_STATE) per SRS A7 — OPEN⇄SOFT_CLOSE→HARD_CLOSE→YEAR_END_CLOSE.
CROSS-MODULE: `statusCode` touches XM-FIN-001; the SoD check (RULE-FIN-015) reads SEC role
data (not a formal XM row — platform-standard integration, ADR-FIN-001).
REPOSITORY OPS → QR-FIN-038 (SAVE, with year), QR-FIN-039 (UPDATE, transitions), QR-FIN-040 (EXISTS),
QR-FIN-049 (FIND_BY_CRITERIA, every period of one year).
<!-- SUB:DATA-DOM-MASTER:END -->

<!-- SUB:DATA-DOM-TRANSACTIONAL:START traces=REQ-FIN-010,REQ-FIN-014,REQ-FIN-017,REQ-FIN-028 -->
### SUB — DATA-DOM-TRANSACTIONAL

#### ENT-FIN-004 — JournalEntry      kind: transactional
BINDINGS: table `FIN_JOURNAL_ENTRY` · PK `journalEntryPk` (DBF-FIN-034) · PK generation `GENERATED ALWAYS AS IDENTITY`
BUSINESS CODE: **docNo** · column `doc_no` (DBF-FIN-035) · format:
`JV-{fiscalYearCode}-{NNNNNN}` (e.g. `JV-2026-000123`), counter scoped per `fiscalYearId`
and restarting at `000001` each fiscal year, single counter across all journal types
(`UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO`) · generation source: a FIN-local generator in
`com.erp.fin` (not `com.erp.common` — see CORE Numbering), invoked at create, before the
first save — excluded from every create/update request body, always present in responses.
FIELDS: DBF-FIN-034..050 — see DB Alignment Manifest; `journalTypeCode`/`statusCode`
lookup-backed (XM-FIN-001).
DTO MEMBERSHIP: manual-create request `{docDate, fiscalYearId, periodId, descriptionAr,
descriptionEn, lines: [...]}`; event-build request = the canonical event payload (opaque
to this DTO description — shape owned by the out-of-scope Event consumer, POL-FIN-020);
no update endpoint exists once POSTED (RULE-FIN-016); response includes all fields
including nested lines.
LOOKUP FIELDS: `journalTypeCode`→`JOURNAL_TYPE`, `statusCode`→`JOURNAL_STATUS`, both via
XM-FIN-001.
DOMAIN RULES:
**RULE-FIN-004** — duplicate eventReference rejected — QR-FIN-026 — service.
**RULE-FIN-005** — no active rule for event type — QR-FIN-027 — service.
**RULE-FIN-006** — debit=credit invariant — QR-FIN-029 — service (POL-FIN-001).
**RULE-FIN-008** — period open at post, except the year-end closing/opening entries the rule
itself exempts (REQ-FIN-036 / API-FIN-027) — QR-FIN-031 — service (POL-FIN-004).
**RULE-FIN-011** — reversal exact and linked — QR-FIN-034 — service (POL-FIN-007).
**RULE-FIN-012** — reversal posts to current period if original's closed — QR-FIN-036 — service.
**RULE-FIN-013** — reject reverse of non-POSTED, and reject reversing an entry that already
carries a reversal link (double-reversal) — QR-FIN-035 — service.
**RULE-FIN-016** — lock after posting — enforced by omission (no UPDATE/DELETE mapping on
a POSTED row in the repository layer at all) — service/repository.
**RULE-FIN-017** — the submitted fiscalYearId, periodId and docDate must describe one
accounting context (the period belongs to that year, DBF-FIN-076; the date falls inside the
period, DBF-FIN-080/081) — API-FIN-019 only, since every system-generated entry derives the
three from one another — `JournalEntryDomain.assertHeaderCoherent(...)` (ALIGN-BE;
`FIN-400-PERIOD-NOT-IN-YEAR`, `FIN-400-DOCDATE-OUTSIDE-PERIOD`).
**RULE-FIN-013's second half under concurrency** — the "already reversed" read-then-write is
made atomic by a PESSIMISTIC_WRITE load of the original's header before the guard runs
(ALIGN-BE); no FIN entity carries `@Version`, so without it two concurrent reversals both
post a mirror.
(Full text of every RULE above: srs-fin.md §A5 — not restated here per the single-source rule.)
STATE MACHINE: `statusCode` (JOURNAL_STATUS) per SRS A7 — DRAFT→POSTED (RULE-FIN-016 locks
immediately); POSTED is terminal. Classic reversal (RULE-FIN-011): reversing an entry leaves the
original POSTED and posts an equal, opposite mirror entry, the two linked through
`originalEntryId`/`reversalEntryId` (DBF-FIN-042/043) — net ledger effect zero. No path sets VOID.
CROSS-MODULE: `journalTypeCode`/`statusCode` touch XM-FIN-001.
REPOSITORY OPS → QR-FIN-023 through QR-FIN-037 (the full posting pipeline + reversal + read).

#### ENT-FIN-005 — JournalLine      kind: transactional
BINDINGS: table `FIN_JOURNAL_LINE` · PK `journalLinePk` (DBF-FIN-051) · PK generation `GENERATED ALWAYS AS IDENTITY`
FIELDS: DBF-FIN-051..060 — see DB Alignment Manifest; `directionCode` lookup-backed
(XM-FIN-001); `amount` DB-CHECK'd positive (`CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE`, POL-FIN-005).
DTO MEMBERSHIP: written only as a nested array inside the entry create/build request; no
standalone line endpoint; never independently updated (locked with its header).
DOMAIN RULES: **RULE-FIN-007** (leaf/active account) — QR-FIN-030 — service;
**RULE-FIN-010** (remainder-line rounding) — QR-FIN-028 — service.
CROSS-MODULE: `directionCode` touches XM-FIN-001.
REPOSITORY OPS → written as part of QR-FIN-024/025/034 (SAVE, header+lines in one
transaction); read via QR-FIN-037, QR-FIN-042 (ledger), QR-FIN-043 (statements).

#### ENT-FIN-006 — JournalLineDimension      kind: transactional
BINDINGS: table `FIN_JOURNAL_LINE_DIM` · PK `journalLineDimensionPk` (DBF-FIN-061) · PK generation `GENERATED ALWAYS AS IDENTITY`
FIELDS: DBF-FIN-061..064 — see DB Alignment Manifest.
DTO MEMBERSHIP: nested under each line in the entry create/build request (0..N per line).
DOMAIN RULES: **RULE-FIN-009** (dimension value validity) — QR-FIN-032 — service.
CROSS-MODULE: none.
REPOSITORY OPS → written as part of QR-FIN-024/025/034; read via QR-FIN-044 (dimension report).
<!-- SUB:DATA-DOM-TRANSACTIONAL:END -->

<!-- SUB:DATA-DOM-LOOKUP:START traces=REQ-FIN-004,REQ-FIN-007,REQ-FIN-022,REQ-FIN-025 -->
### SUB — DATA-DOM-LOOKUP (FIN's own config/lookup-kind structural data)

#### ENT-FIN-002 — Dimension      kind: config
BINDINGS: table `FIN_DIMENSION` · PK `dimensionPk` (DBF-FIN-014) · PK generation `GENERATED ALWAYS AS IDENTITY`
FIELDS: DBF-FIN-014..022. DTO: create `{code, nameAr, nameEn}`; no update endpoint (name
edits go through a future v2 if needed — not named in the plan, so not built). DOMAIN
RULES: none scoped alone. REPOSITORY OPS → QR-FIN-007, QR-FIN-008, QR-FIN-046 (EXISTS, code unique).

#### ENT-FIN-003 — DimensionValue      kind: lookup
BINDINGS: table `FIN_DIMENSION_VALUE` · PK `dimensionValuePk` (DBF-FIN-023) · PK generation `GENERATED ALWAYS AS IDENTITY`
FIELDS: DBF-FIN-023..033 — matches `profile.conventions.entity_defaults.lookup` exactly
(code, nameAr, nameEn, sortOrder, isActiveFl) plus PK/FK/audit. DOMAIN RULES:
**RULE-FIN-002** (duplicate code within dimension) — QR-FIN-010 — service + DB
(`UQ_FIN_DIMENSION_VALUE_DIM_CODE`). REPOSITORY OPS → QR-FIN-009, QR-FIN-010, QR-FIN-011.

#### ENT-FIN-009 — EventTypeRule      kind: config
BINDINGS: table `FIN_EVENT_TYPE_RULE` · PK `eventTypeRulePk` (DBF-FIN-089) · PK generation `GENERATED ALWAYS AS IDENTITY`
FIELDS: DBF-FIN-089..097; `eventTypeCode` lookup-backed (XM-FIN-001). DOMAIN RULES: one
active rule per event type — DB (`UQ_FIN_EVENT_TYPE_RULE_CODE`) + QR-FIN-014. REPOSITORY
OPS → QR-FIN-012, QR-FIN-013, QR-FIN-014, QR-FIN-027 (reused at post time).

#### ENT-FIN-010 — RuleLine      kind: config
BINDINGS: table `FIN_RULE_LINE` · PK `ruleLinePk` (DBF-FIN-098) · PK generation `GENERATED ALWAYS AS IDENTITY`
FIELDS: DBF-FIN-098..108; `accountDerivationTypeCode`/`amountSourceTypeCode`/
`directionCode`/`distributionTypeCode` all lookup-backed (XM-FIN-001). DOMAIN RULES:
**RULE-FIN-003** (exactly one remainder line whenever the line set is a compound or
percentage distribution — any sibling PERCENTAGE-distributed, or any line already marked
remainder — plus marker agreement: `isRemainderFl` (DBF-FIN-103) is the SINGLE remainder
marker, the one both this guard and the API-FIN-020 builder read, and must agree with the
line's own REMAINDER type code, `FIN-422-REMAINDER-MARKER`) — QR-FIN-016 —
`EventTypeRuleDomain`. REPOSITORY OPS → QR-FIN-015, QR-FIN-016; read as part of QR-FIN-025/028 at
event-entry build time.

#### ENT-FIN-011 — RecurringTemplate      kind: config
BINDINGS: table `FIN_RECURRING_TEMPLATE` · PK `recurringTemplatePk` (DBF-FIN-109) · PK generation `GENERATED ALWAYS AS IDENTITY`
FIELDS: DBF-FIN-109..121; `scheduleTypeCode`/`frequencyCode` lookup-backed (XM-FIN-001).
DOMAIN RULES: none scoped alone (its run posts through the same shared pipeline as any
entry — RULE-FIN-006 through RULE-FIN-010, cited on API-FIN-014). REPOSITORY OPS →
QR-FIN-017, QR-FIN-018, QR-FIN-019.

#### ENT-FIN-012 — RecurringTemplateLine      kind: config
BINDINGS: table `FIN_RECURRING_TEMPLATE_LINE` · PK `recurringTemplateLinePk` (DBF-FIN-122) · PK generation `GENERATED ALWAYS AS IDENTITY`
FIELDS: DBF-FIN-122..129; `directionCode` lookup-backed (XM-FIN-001); `amount` DB-CHECK'd
positive. DOMAIN RULES: none scoped alone. REPOSITORY OPS → written with QR-FIN-018; read
by QR-FIN-019 at run time.

#### ENT-FIN-013 — AllocationRule      kind: config
BINDINGS: table `FIN_ALLOCATION_RULE` · PK `allocationRulePk` (DBF-FIN-130) · PK generation `GENERATED ALWAYS AS IDENTITY`
FIELDS: DBF-FIN-130..138. DOMAIN RULES: none scoped alone. REPOSITORY OPS → QR-FIN-020,
QR-FIN-021, QR-FIN-022.

#### ENT-FIN-014 — AllocationTarget      kind: config
BINDINGS: table `FIN_ALLOCATION_TARGET` · PK `allocationTargetPk` (DBF-FIN-139) · PK generation `GENERATED ALWAYS AS IDENTITY`
FIELDS: DBF-FIN-139..146; `distributionTypeCode` lookup-backed (XM-FIN-001). DOMAIN RULES:
**RULE-FIN-003** (reused — exactly one remainder target whenever the target set is a
compound or percentage distribution, plus marker agreement between `isRemainderFl`
(DBF-FIN-141, the SINGLE marker the API-FIN-017 builder reads) and `distributionTypeCode`,
`FIN-422-REMAINDER-MARKER`) — QR-FIN-016 (reused) — `AllocationRuleDomain`. REPOSITORY OPS → written with QR-FIN-021; read by
QR-FIN-022 at run time.
<!-- SUB:DATA-DOM-LOOKUP:END -->
<!-- PHASE:DATA-DOM:END -->

<!-- PHASE:SVC-API:START traces=REQ-FIN-001,REQ-FIN-010,REQ-FIN-014,REQ-FIN-017,REQ-FIN-028,REQ-FIN-036 -->
## PHASE 3 — SVC-API

API count = 32 ≥ 8 → split by threshold, grouped CRUD / SEARCH / INT.

<!-- SUB:SVC-API-SEARCH:START traces=REQ-FIN-001,REQ-FIN-004,REQ-FIN-007,REQ-FIN-022,REQ-FIN-027,REQ-FIN-031,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043 -->
### SUB — SVC-API-SEARCH (read-only)

<!-- API:API-FIN-001:START traces=REQ-FIN-001,DBF-FIN-002,DBF-FIN-003,DBF-FIN-004,DBF-FIN-005 -->
### API-FIN-001 — search accounts
Endpoint: POST /api/v1/fin/accounts/search · Layers: `AccountController.search`→`AccountService.search`
Request: body `AccountSearchRequest` — `code`(LIKE), `nameAr/nameEn`(LIKE), `accountTypeCode`(EXACT), `isActiveFl`(EXACT), paging
Response: 200 · `Page<AccountResponse>` · `ApiResponse<...>`
Validations: none (read-only) · Errors: `FIN-500`
Orchestration: load (QR-FIN-001) → map → return · Repository: QR-FIN-001 · join NONE · READ_ONLY
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_VIEW` · Localization: nameAr/nameEn returned
<!-- API:API-FIN-001:END -->

<!-- API:API-FIN-005:START traces=REQ-FIN-004,DBF-FIN-015,DBF-FIN-016,DBF-FIN-017 -->
### API-FIN-005 — search dimensions
Endpoint: POST /api/v1/fin/dimensions/search · Layers: `DimensionController.search`→`DimensionService.search`
Request: body `DimensionSearchRequest` — `code`(LIKE), paging · Response: 200 · `Page<DimensionResponse>`
Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-007 → map → return · Repository: QR-FIN-007 · join NONE · READ_ONLY
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_VIEW` · Localization: nameAr/nameEn
<!-- API:API-FIN-005:END -->

<!-- API:API-FIN-008:START traces=REQ-FIN-005,DBF-FIN-024,DBF-FIN-025,DBF-FIN-026,DBF-FIN-027 -->
### API-FIN-008 — search dimension values
Endpoint: POST /api/v1/fin/dimensions/values/search · Layers: `DimensionController.searchDimensionValues`→`DimensionValueService.search`
Request: body `DimensionValueSearchRequest` — `dimensionId`(EXACT, required) carried in the body filters and read by the child parent-id extractor (never a path variable); `code`(LIKE), paging · Response: 200 · `Page<DimensionValueResponse>`
Validations: none · Errors: `FIN-404-DIMENSION`
Orchestration: QR-FIN-011 → map → return · Repository: QR-FIN-011 · join NONE · READ_ONLY
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_VIEW` · Localization: nameAr/nameEn
<!-- API:API-FIN-008:END -->

<!-- API:API-FIN-009:START traces=REQ-FIN-007,DBF-FIN-090,DBF-FIN-091,DBF-FIN-092 -->
### API-FIN-009 — search event-type rules
Endpoint: POST /api/v1/fin/event-rules/search · Layers: `EventTypeRuleController.search`→`EventTypeRuleService.search`
Request: body `EventTypeRuleSearchRequest` — `eventTypeCode`(EXACT), `isActiveFl`(EXACT), paging · Response: 200 · `Page<EventTypeRuleResponse>`
Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-012 → map → return · Repository: QR-FIN-012 · join NONE · READ_ONLY
Security: screen FIN_RULES · `PERM_FIN_RULES_VIEW` · Localization: nameAr/nameEn
<!-- API:API-FIN-009:END -->

<!-- API:API-FIN-012:START traces=REQ-FIN-022,DBF-FIN-110,DBF-FIN-111,DBF-FIN-112 -->
### API-FIN-012 — search templates
Endpoint: POST /api/v1/fin/recurring-templates/search · Layers: `RecurringTemplateController.search`→`RecurringTemplateService.search`
Request: body `RecurringTemplateSearchRequest` — `nameAr/nameEn`(LIKE), `scheduleTypeCode`(EXACT), `isActiveFl`(EXACT), paging
Response: 200 · `Page<RecurringTemplateResponse>` · Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-017 → map → return · Repository: QR-FIN-017 · join NONE · READ_ONLY
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_VIEW` · Localization: nameAr/nameEn
<!-- API:API-FIN-012:END -->

<!-- API:API-FIN-015:START traces=REQ-FIN-025,DBF-FIN-131,DBF-FIN-132,DBF-FIN-133 -->
### API-FIN-015 — search allocation rules
Endpoint: POST /api/v1/fin/allocation-rules/search · Layers: `AllocationRuleController.search`→`AllocationRuleService.search`
Request: body `AllocationRuleSearchRequest` — `nameAr/nameEn`(LIKE), `sourceAccountId`(EXACT), `isActiveFl`(EXACT), paging
Response: 200 · `Page<AllocationRuleResponse>` · Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-020 → map → return · Repository: QR-FIN-020 · join NONE · READ_ONLY
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_VIEW` · Localization: nameAr/nameEn
<!-- API:API-FIN-015:END -->

<!-- API:API-FIN-018:START traces=REQ-FIN-027,DBF-FIN-035,DBF-FIN-036,DBF-FIN-040 -->
### API-FIN-018 — search journal entries
Endpoint: POST /api/v1/fin/journal-entries/search · Layers: `JournalEntryController.search`→`JournalEntryService.search`
Request: body `JournalEntrySearchRequest` — `docNo`(LIKE), `docDate`(DATE_RANGE), `periodId`(EXACT), `statusCode`(EXACT), `journalTypeCode`(EXACT), paging
Response: 200 · `Page<JournalEntryResponse>` · Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-023 → map → return (REQ-FIN-027: unmodified) · Repository: QR-FIN-023 · join NONE · READ_ONLY
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_VIEW` · Localization: descriptionAr/En
<!-- API:API-FIN-018:END -->

<!-- API:API-FIN-022:START traces=REQ-FIN-016,REQ-FIN-027,DBF-FIN-034,DBF-FIN-041 -->
### API-FIN-022 — read entry
Endpoint: GET /api/v1/fin/journal-entries/{id} · Layers: `JournalEntryController.read`→`JournalEntryService.read`
Request: path `id` · Response: 200 · `JournalEntryResponse` with nested lines and their dimensions
Validations: none · Errors: `FIN-404-ENTRY`
Orchestration: QR-FIN-037 → map → return · Repository: QR-FIN-037 · join intra-module (entry→line→line-dim) · READ_ONLY
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_VIEW` · Localization: descriptionAr/En
<!-- API:API-FIN-022:END -->

<!-- API:API-FIN-028:START traces=REQ-FIN-039,REQ-FIN-046,DBF-FIN-054,DBF-FIN-055,DBF-FIN-056 -->
### API-FIN-028 — account ledger
Endpoint: GET /api/v1/fin/reports/account-ledger · Layers: `ReportController.accountLedger`→`ReportService.accountLedger`
Request: `accountId`(EXACT), date range, dimension filters · Response: 200 · running-balance list, each row linking journalEntryId
Validations: none · Errors: `FIN-404-ACCOUNT`
Orchestration: QR-FIN-042 (POSTED lines only, live) → compute running balance → return (POL-FIN-009)
Repository: QR-FIN-042 · join intra-module (line→entry for docDate/status) · READ_ONLY
Security: screen FIN_ACCOUNT_LEDGER · `PERM_FIN_ACCOUNT_LEDGER_VIEW` · Localization: n/a
<!-- API:API-FIN-028:END -->

<!-- API:API-FIN-029:START traces=REQ-FIN-040,REQ-FIN-046,DBF-FIN-005,DBF-FIN-006,DBF-FIN-055 -->
### API-FIN-029 — trial balance
Endpoint: GET /api/v1/fin/reports/trial-balance · Layers: `ReportController.trialBalance`→`ReportService.trialBalance`
Request: `periodId`(EXACT), `accountTypeCode`(EXACT) · Response: 200 · one row per account (debit/credit balance, sign per natureCode — POL-FIN-002)
Validations: RULE-FIN-006 restated as a report-level guarantee (POL-FIN-008: the sums always match because every contributing entry individually balanced — no separate check needed, an invariant by construction). `periodId` is an OPTIONAL narrowing, validated ONLY when supplied: omitting it means "no period narrowing" and stays a 200 (AC-FIN-040's happy path) — it is NOT mandatory
Errors: `FIN-404-PERIOD` (supplied `periodId` does not resolve), `FIN-500`
Orchestration: QR-FIN-043 (POSTED lines only, live, grouped by account) → apply nature sign → return
Repository: QR-FIN-043 · join intra-module · READ_ONLY
Security: screen FIN_TRIAL_BALANCE · `PERM_FIN_TRIAL_BALANCE_VIEW` · Localization: nameAr/nameEn per account
<!-- API:API-FIN-029:END -->

<!-- API:API-FIN-030:START traces=REQ-FIN-041,REQ-FIN-046,DBF-FIN-005,DBF-FIN-055 -->
### API-FIN-030 — balance sheet
Endpoint: GET /api/v1/fin/reports/balance-sheet · Layers: `ReportController.balanceSheet`→`ReportService.balanceSheet`
Request: `fiscalYearId`(EXACT), `asOfDate` · Response: 200 · grouped ASSET/LIABILITY/EQUITY balances
Validations: none (continuity itself is guaranteed by REQ-FIN-036's opening-entry generation, not re-validated at read time). The REQUIRED `fiscalYearId` is resolved FIRST, before any aggregation
Errors: `FIN-404-YEAR` (unknown `fiscalYearId` — previously a silent 200 carrying an all-zero statement), `FIN-500`
Orchestration: QR-FIN-043 (accountTypeCode IN ASSET,LIABILITY,EQUITY) → group → return
Repository: QR-FIN-043 · join intra-module · READ_ONLY
Security: screen FIN_BALANCE_SHEET · `PERM_FIN_BALANCE_SHEET_VIEW` · Localization: nameAr/nameEn per account
<!-- API:API-FIN-030:END -->

<!-- API:API-FIN-031:START traces=REQ-FIN-042,REQ-FIN-046,DBF-FIN-005,DBF-FIN-055 -->
### API-FIN-031 — income statement
Endpoint: GET /api/v1/fin/reports/income-statement · Layers: `ReportController.incomeStatement`→`ReportService.incomeStatement`
Request: `fiscalYearId`(EXACT), period range · Response: 200 · grouped REVENUE/EXPENSE balances
Validations: none (zero-opening is guaranteed by REQ-FIN-036 closing result accounts to Retained Earnings, not re-validated at read time). The REQUIRED `fiscalYearId` is resolved FIRST; the two OPTIONAL period bounds `fromPeriodId`/`toPeriodId` are validated only when supplied, as they already were
Errors: `FIN-404-YEAR` (unknown `fiscalYearId`), `FIN-404-PERIOD` (supplied period bound does not resolve — unchanged), `FIN-500`
Orchestration: QR-FIN-043 (accountTypeCode IN REVENUE,EXPENSE, scoped to the year/period range) → group → return
Repository: QR-FIN-043 · join intra-module · READ_ONLY
Security: screen FIN_INCOME_STATEMENT · `PERM_FIN_INCOME_STATEMENT_VIEW` · Localization: nameAr/nameEn per account
<!-- API:API-FIN-031:END -->

<!-- API:API-FIN-032:START traces=REQ-FIN-043,DBF-FIN-063,DBF-FIN-064,DBF-FIN-055 -->
### API-FIN-032 — dimension report
Endpoint: GET /api/v1/fin/reports/dimension · Layers: `ReportController.dimensionReport`→`ReportService.dimensionReport`
Request: `dimensionId`(EXACT), `dimensionValueId`(EXACT), `periodId`(EXACT) · Response: 200 · one row per account+dimension-value combination (POL-FIN-011)
Validations: none · Errors: `FIN-404-DIMENSION`
Orchestration: QR-FIN-044 (group by account + dimension value, never base account alone) → return
Repository: QR-FIN-044 · join intra-module (line→line-dim→dimension-value) · READ_ONLY
Security: screen FIN_DIMENSION_REPORTS · `PERM_FIN_DIMENSION_REPORTS_VIEW` · Localization: nameAr/nameEn
<!-- API:API-FIN-032:END -->

<!-- API:API-FIN-033:START traces=REQ-FIN-031,DBF-FIN-076,DBF-FIN-077,DBF-FIN-080,DBF-FIN-081,DBF-FIN-082 -->
### API-FIN-033 — search fiscal periods
Endpoint: POST /api/v1/fin/fiscal-periods/search · Layers: `FiscalPeriodController.search`→`FiscalPeriodService.search`
Request: body `FiscalPeriodSearchRequest` — `fiscalYearId`(EXACT, **OPTIONAL**, DBF-FIN-076) carried in the body filters and read by the child parent-id extractor (never a path variable); `statusCode`(EXACT, DBF-FIN-082), paging/sort · Response: 200 · `Page<FiscalPeriodResponse>`
Validations: none (read-only) · Errors: `FIN-400-INVALID-SORT`, `FIN-500`
Orchestration: build the generic specification from the remaining filters → AND in an explicit join predicate on `fiscalYear.fiscalYearPk` ONLY when `fiscalYearId` is present → page → map → return
Repository: `FiscalPeriodRepository` via `JpaSpecificationExecutor.findAll(Specification, Pageable)` — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join intra-module (period→year, only when the parent filter is supplied) · READ_ONLY
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_VIEW` · Localization: nameAr/nameEn per period
Parent id OPTIONAL — deliberate divergence from API-FIN-008, which rejects a missing `dimensionId` with `FIN-404-DIMENSION`. SCR-REQ-FIN-007 §B2 makes both filters EXACT but neither mandatory, and the endpoint exists precisely so a client that did NOT create the fiscal year in the same session can discover a period id: requiring the year id first would leave that client with no way in. Same shape API-FIN-018 already uses for its optional `periodId`.
Why it exists: `JournalEntryCreateRequest` requires `fiscalYearId` + `periodId` (API-FIN-019) and API-FIN-029/030/031 require a period or year id, yet before this endpoint no API returned a fiscal period except API-FIN-023's create response. `PERM_FIN_PERIODS_VIEW` was already a V24 registry row and already granted by V25/V27, so **no migration was needed** — only the matching `PermissionConstants` constant was added.
<!-- API:API-FIN-033:END -->

**404 on a keying id, as built (API-FIN-029/030/031).** FIN's house style already 404s on the id
that keys a report — API-FIN-028 answers `FIN-404-ACCOUNT`, API-FIN-032 answers
`FIN-404-DIMENSION`. API-FIN-030 and API-FIN-031 now do the same for their REQUIRED `fiscalYearId`
(`FIN-404-YEAR`), and API-FIN-029 for its `periodId` when — and only when — one is supplied
(`FIN-404-PERIOD`). API-FIN-031 was internally contradictory before: 404 on an unknown period bound
and 200 on an unknown year, in the same request. No new error code was introduced; both codes were
already registered in `FinErrorCodes` and both i18n bundles.
<!-- SUB:SVC-API-SEARCH:END -->

<!-- SUB:SVC-API-CRUD:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-014,REQ-FIN-022,REQ-FIN-025 -->
### SUB — SVC-API-CRUD

<!-- API:API-FIN-002:START traces=REQ-FIN-001,REQ-FIN-002,DBF-FIN-002,DBF-FIN-005,DBF-FIN-006,DBF-FIN-007,DBF-FIN-008 -->
### API-FIN-002 — create account
Endpoint: POST /api/v1/fin/accounts · Layers: `AccountController.create`→`AccountService.create`
Request: `{code, nameAr, nameEn, accountTypeCode, natureCode, parentAccountId?, isLeafFl}`
Response: 201 · `AccountResponse`
Validations: RULE-FIN-001 (leaf requires no children — only relevant if `parentAccountId`
is later given children; at create this only matters when the same call sets a parent
that itself must remain non-leaf — QR-FIN-006 checks the *parent*, not the new row);
uniqueness of code (QR-FIN-005); accountTypeCode/natureCode validated via XM-FIN-001
Errors: `FIN-409-ACCOUNT-DUP`, `FIN-409-PARENT-NOT-LEAF-ELIGIBLE`, `FIN-400-INVALID-LOOKUP`
Orchestration: validate lookups (XM-FIN-001) → validate uniqueness (QR-FIN-005) → if
parentAccountId given, flip the parent's isLeafFl to false via RULE-FIN-001's inverse
effect (a parent gaining its first child can no longer itself be a leaf) → persist (QR-FIN-002) → return
Repository: QR-FIN-002, QR-FIN-005, QR-FIN-006 · join NONE · READ_WRITE
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_CREATE` · Localization: nameAr/nameEn required
<!-- API:API-FIN-002:END -->

<!-- API:API-FIN-003:START traces=REQ-FIN-002,DBF-FIN-003,DBF-FIN-004,DBF-FIN-008 -->
### API-FIN-003 — update account
Endpoint: PUT /api/v1/fin/accounts/{id} · Layers: `AccountController.update`→`AccountService.update`
Request: `{nameAr, nameEn, isLeafFl}` — excludes {accountPk, code, accountTypeCode, natureCode, isActiveFl, audit}
Response: 200 · `AccountResponse`
Validations: RULE-FIN-002-equivalent — RULE-FIN-001 (full text: DATA-DOM §ENT-FIN-001) —
`isLeafFl=true` rejected if the account has any child (QR-FIN-006)
Errors: `FIN-409-HAS-CHILDREN`, `FIN-404-ACCOUNT`
Orchestration: load → check RULE-FIN-001 if isLeafFl changing to true (QR-FIN-006) → update (QR-FIN-003) → return
Repository: QR-FIN-003, QR-FIN-006 · join NONE · READ_WRITE
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_UPDATE` · Localization: both name fields updatable
<!-- API:API-FIN-003:END -->

<!-- API:API-FIN-004:START traces=REQ-FIN-003,DBF-FIN-009 -->
### API-FIN-004 — deactivate account
Endpoint: PUT /api/v1/fin/accounts/{id}/deactivate · Layers: `AccountController.deactivate`→`AccountService.deactivate`
Request: path `id`, no body · Response: 200 · `AccountResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-ACCOUNT`
Orchestration: load → `Account.deactivate()` → persist (QR-FIN-004) → return (REQ-FIN-019
subsequently rejects any posting to it)
Repository: QR-FIN-004 · join NONE · READ_WRITE
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_UPDATE` · Localization: n/a
<!-- API:API-FIN-004:END -->

<!-- API:API-FIN-006:START traces=REQ-FIN-004,DBF-FIN-015,DBF-FIN-016,DBF-FIN-017 -->
### API-FIN-006 — create dimension
Endpoint: POST /api/v1/fin/dimensions · Layers: `DimensionController.create`→`DimensionService.create`
Request: `{code, nameAr, nameEn}` · Response: 201 · `DimensionResponse`
Validations: uniqueness of code (DB `UQ_FIN_DIMENSION_CODE`, checked friendly at service layer)
Errors: `FIN-409-DIMENSION-DUP`
Orchestration: validate → persist (QR-FIN-008) → return · Repository: QR-FIN-008 · join NONE · READ_WRITE
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_CREATE` · Localization: nameAr/nameEn required
<!-- API:API-FIN-006:END -->

<!-- API:API-FIN-007:START traces=REQ-FIN-005,REQ-FIN-006,DBF-FIN-024,DBF-FIN-025,DBF-FIN-028 -->
### API-FIN-007 — create dimension value
Endpoint: POST /api/v1/fin/dimensions/{id}/values · Layers: `DimensionController.createDimensionValue`→`DimensionValueService.create`
Request: path `id` (dimensionId); body `{code, nameAr, nameEn, sortOrder}`
Response: 201 · `DimensionValueResponse`
Validations: RULE-FIN-002 (full text: DATA-DOM §ENT-FIN-003) — code unique within the
dimension (QR-FIN-010)
Errors: `FIN-409-DIMVALUE-DUP`, `FIN-404-DIMENSION`
Orchestration: validate dimension exists → check RULE-FIN-002 (QR-FIN-010) → persist (QR-FIN-009) → return
Repository: QR-FIN-009, QR-FIN-010 · join NONE · READ_WRITE
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_CREATE` · Localization: nameAr/nameEn required
<!-- API:API-FIN-007:END -->

<!-- API:API-FIN-035:START traces=REQ-FIN-005,REQ-FIN-021,DBF-FIN-029 -->
### API-FIN-035 — deactivate dimension value
Endpoint: PUT /api/v1/fin/dimensions/values/{id}/deactivate · Layers: `DimensionController.deactivateDimensionValue`→`DimensionValueService.deactivate`
Request: path `id` (dimensionValuePk), no body · Response: 200 · `DimensionValueResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-DIMVALUE`
Orchestration: load → `DimensionValue.deactivate()` (the entity's own helper, never a direct field assignment) → persist → return. Effect: `DimensionValueDomain.checkUsableOnLine` reads exactly this flag (DBF-FIN-029), so every posting path citing the value afterwards answers `FIN-409-INVALID-DIMENSION` (RULE-FIN-009 / REQ-FIN-021, via API-FIN-019, 020, 014, 017)
Repository: `DimensionValueRepository.findById` + `save` — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_UPDATE` · Localization: n/a
New in this delivery: `FIN-404-DIMVALUE` (`FinErrorCodes.FIN_404_DIMVALUE`, added to BOTH i18n bundles) and `PERM_FIN_DIMENSIONS_UPDATE`. This is the first UPDATE-class endpoint on FIN_DIMENSIONS, so migration `V28__fin_dimensions_update_action.sql` registers the `FIN_DIMENSIONS / UPDATE` action row AND explicitly grants it to `SYS_ADMIN` — V25 grants by a `SELECT` over the registry and has already run, so a later row would otherwise be registered-but-ungrantable.
Why it exists: RULE-FIN-009 / REQ-FIN-021 reject a journal line citing an INACTIVE dimension value and `DimensionValueDomain` implements that check, but nothing could set the flag false, so the branch was unreachable and untestable. Deliberately NOT built (a decision, not a backlog item): a deactivate on the PARENT `Dimension` — no REQ/AC/RULE requires one and `Dimension.isActiveFl` (DBF-FIN-018) drives no behaviour. No `activate` counterpart either, matching the delivered `AccountService.deactivate` precedent.
<!-- API:API-FIN-035:END -->

<!-- API:API-FIN-010:START traces=REQ-FIN-007,REQ-FIN-044,REQ-FIN-045,DBF-FIN-090,DBF-FIN-091,DBF-FIN-092 -->
### API-FIN-010 — create event-type rule
Endpoint: POST /api/v1/fin/event-rules · Layers: `EventTypeRuleController.create`→`EventTypeRuleService.create`
Request: `{eventTypeCode, nameAr, nameEn}` · Response: 201 · `EventTypeRuleResponse`
Validations: eventTypeCode validated via XM-FIN-001; uniqueness of one-active-rule-per-type (QR-FIN-014)
Errors: `FIN-409-RULE-DUP`, `FIN-400-INVALID-LOOKUP`
Orchestration: validate lookup → check uniqueness (QR-FIN-014) → persist (QR-FIN-013) → return
Repository: QR-FIN-013, QR-FIN-014 · join NONE · READ_WRITE
Security: screen FIN_RULES · `PERM_FIN_RULES_CREATE` · Localization: nameAr/nameEn required
Precondition: this endpoint is only reachable once FIN's onboarding has completed —
FIN registered as a module with its screens/actions into SEC (REQ-FIN-044) and FIN's 13
lookup types, including ACCOUNTING_EVENT_TYPE, registered into MDL (REQ-FIN-045); both run
once at deployment, not per request.
<!-- API:API-FIN-010:END -->

<!-- API:API-FIN-011:START traces=REQ-FIN-008,REQ-FIN-009,DBF-FIN-101,DBF-FIN-103,DBF-FIN-105,DBF-FIN-106,DBF-FIN-107 -->
### API-FIN-011 — add rule line
Endpoint: POST /api/v1/fin/event-rules/{id}/lines · Layers: `EventTypeRuleController.createRuleLine`→`RuleLineService.create`
Request: path `id` (eventTypeRuleId); body `{accountDerivationTypeCode, accountDerivationValue,
amountSourceTypeCode, amountSourceValue?, directionCode, distributionTypeCode, isRemainderFl}`
Response: 201 · `RuleLineResponse`
Validations: RULE-FIN-003 (full text: DATA-DOM §ENT-FIN-010) — exactly one remainder line
once the line set is a compound or percentage distribution, i.e. any sibling line is
PERCENTAGE-distributed OR any line is already marked remainder (QR-FIN-016); and each line's
`isRemainderFl` marker (DBF-FIN-103) must agree with its own REMAINDER type code, since that
marker is the single one the API-FIN-020 builder reads; all four lookup codes validated via
XM-FIN-001
Errors: `FIN-409-REMAINDER-COUNT`, `FIN-422-REMAINDER-MARKER`, `FIN-404-RULE`, `FIN-400-INVALID-LOOKUP`
Orchestration: validate lookups → check RULE-FIN-003 across the rule's existing + new line
(QR-FIN-016) → persist (QR-FIN-015) → return
Repository: QR-FIN-015, QR-FIN-016 · join NONE · READ_WRITE
Security: screen FIN_RULES · `PERM_FIN_RULES_UPDATE` · Localization: n/a
<!-- API:API-FIN-011:END -->

<!-- API:API-FIN-034:START traces=REQ-FIN-007,DBF-FIN-093 -->
### API-FIN-034 — deactivate event-type rule
Endpoint: PUT /api/v1/fin/event-rules/{id}/deactivate · Layers: `EventTypeRuleController.deactivate`→`EventTypeRuleService.deactivate`
Request: path `id`, no body · Response: 200 · `EventTypeRuleResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-RULE`
Orchestration: load → clear the active flag (DBF-FIN-093) → persist → return
Repository: `EventTypeRuleRepository.findById` + `save` — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_RULES · `PERM_FIN_RULES_UPDATE` (pre-existing — no new constant, no migration) · Localization: n/a
Why it exists: until this endpoint landed no rule could ever be retired, so `FIN-404-NO-ACTIVE-RULE` (RULE-FIN-005, API-FIN-020) was unreachable. **Stated limitation**, recorded in the service's own javadoc: deactivating does NOT free the event type for a replacement rule, because `EventTypeRuleService.create` guards uniqueness with `existsByEventTypeCode`, which is not scoped to the active flag. No `activate` counterpart, and no rule-line delete — ENT-FIN-010 carries no active-flag column and FIN publishes no `DELETE` endpoint on any screen (a deliberate v1 exclusion, see srs-fin.md SCR-REQ-FIN-003 §B4).
<!-- API:API-FIN-034:END -->

<!-- API:API-FIN-013:START traces=REQ-FIN-022,DBF-FIN-112,DBF-FIN-113,DBF-FIN-114,DBF-FIN-115 -->
### API-FIN-013 — create template
Endpoint: POST /api/v1/fin/recurring-templates · Layers: `RecurringTemplateController.create`→`RecurringTemplateService.create`
Request: `{nameAr, nameEn, scheduleTypeCode, frequencyCode?, startDate, endDate?, lines: [...]}`
Response: 201 · `RecurringTemplateResponse`
Validations: scheduleTypeCode/frequencyCode/line directionCode validated via XM-FIN-001;
frequencyCode required unless scheduleTypeCode=REVERSING
Errors: `FIN-400-MISSING-FREQUENCY`, `FIN-400-INVALID-LOOKUP`
Orchestration: validate → set nextRunDate=startDate → persist template+lines (QR-FIN-018) → return
Repository: QR-FIN-018 · join NONE · READ_WRITE
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_CREATE` · Localization: nameAr/nameEn required
<!-- API:API-FIN-013:END -->

<!-- API:API-FIN-016:START traces=REQ-FIN-025,DBF-FIN-133,DBF-FIN-142,DBF-FIN-144 -->
### API-FIN-016 — create allocation rule
Endpoint: POST /api/v1/fin/allocation-rules · Layers: `AllocationRuleController.create`→`AllocationRuleService.create`
Request: `{nameAr, nameEn, sourceAccountId, targets: [...]}`
Response: 201 · `AllocationRuleResponse`
Validations: RULE-FIN-003 (reused) — exactly one remainder target when any sibling is
PERCENTAGE or any target is already marked remainder (QR-FIN-016, reused), and each target's
`isRemainderFl` (DBF-FIN-141) must agree with its own `distributionTypeCode`;
distributionTypeCode validated via XM-FIN-001
Errors: `FIN-409-REMAINDER-COUNT`, `FIN-422-REMAINDER-MARKER`, `FIN-404-ACCOUNT`, `FIN-400-INVALID-LOOKUP`
Orchestration: validate → check RULE-FIN-003 across targets (QR-FIN-016) → persist rule+targets (QR-FIN-021) → return
Repository: QR-FIN-021, QR-FIN-016 · join NONE · READ_WRITE
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_CREATE` · Localization: nameAr/nameEn required
<!-- API:API-FIN-016:END -->

<!-- API:API-FIN-019:START traces=REQ-FIN-014,REQ-FIN-015,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,DBF-FIN-036,DBF-FIN-037,DBF-FIN-038,DBF-FIN-044 -->
### API-FIN-019 — create manual entry
Endpoint: POST /api/v1/fin/journal-entries · Layers: `JournalEntryController.createManual`→`JournalEntryService.createManual`
Request: `{docDate, fiscalYearId, periodId, journalTypeCode="MANUAL", descriptionAr,
descriptionEn, lines: [{accountId, amount, directionCode, descriptionAr?, descriptionEn?,
dimensions: [{dimensionId, dimensionValueId}]}]}` — excludes {journalEntryPk, docNo,
statusCode, postedAt, audit}
Response: 201 · `JournalEntryResponse` (statusCode=POSTED on success)
Validations: RULE-FIN-006 (debit=credit, QR-FIN-029), RULE-FIN-007 (leaf/active accounts,
QR-FIN-030), RULE-FIN-008 (period open, QR-FIN-031), RULE-FIN-009 (dimension valid,
QR-FIN-032) — every failure returned together (REQ-FIN-015), nothing posts if any fails.
RULE-FIN-017 (header coherence) runs FIRST and fail-fast: the submitted `periodId` must
belong to the submitted `fiscalYearId` (DBF-FIN-076) and `docDate` must fall inside that
period's [startDate, endDate] (DBF-FIN-080/081) — an incoherent triple makes RULE-FIN-008's
own period gate meaningless, lets the entry take a docNo from the wrong year's series, and
corrupts every period-scoped report and the year-end close. This is the only API that needs
it: every system-generated entry derives the three facts from one another
Errors: `FIN-400-PERIOD-NOT-IN-YEAR`, `FIN-400-DOCDATE-OUTSIDE-PERIOD`, `FIN-409-UNBALANCED`,
`FIN-409-NOT-POSTABLE-ACCOUNT`, `FIN-409-PERIOD-NOT-OPEN`, `FIN-409-INVALID-DIMENSION`
Orchestration: resolve fiscal year UNDER A ROW LOCK (`SELECT ... FOR UPDATE` on
FIN_FISCAL_YEAR, the docNo series' allocation lock) → resolve period → check RULE-FIN-017 →
generate docNo (FIN-local generator, `JV-{fiscalYearCode}-{NNNNNN}`) → build DRAFT (QR-FIN-024) → validate
(QR-FIN-029..032) → on success: post (QR-FIN-033); on failure: discard the whole attempt
(one transaction, REQ-FIN-015) → return. The lock is what makes the per-fiscal-year counter
safe under concurrency: two simultaneous creates can no longer observe the same predecessor,
so `UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO` stays an unreachable backstop instead of surfacing as an
unlocalized data-integrity 409. A sequence was rejected (the counter restarts per year and the
schema declares none) and so was catch-and-retry (a service may not catch
DataIntegrityViolationException)
Repository: QR-FIN-024, QR-FIN-029, QR-FIN-030, QR-FIN-031, QR-FIN-032, QR-FIN-033 · join
NONE · READ_WRITE (one transaction, build-through-post)
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_CREATE` · Localization: descriptionAr/En
<!-- API:API-FIN-019:END -->
<!-- SUB:SVC-API-CRUD:END -->

<!-- SUB:SVC-API-INT:START traces=REQ-FIN-010,REQ-FIN-023,REQ-FIN-024,REQ-FIN-026,REQ-FIN-028,REQ-FIN-031,REQ-FIN-034,REQ-FIN-036,REQ-FIN-037 -->
### SUB — SVC-API-INT (posting-pipeline orchestration and period-control actions)

<!-- API:API-FIN-020:START traces=REQ-FIN-010,REQ-FIN-011,REQ-FIN-012,REQ-FIN-013,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,DBF-FIN-041,DBF-FIN-090,DBF-FIN-106,DBF-FIN-107 -->
### API-FIN-020 — build event entry (system)
Endpoint: POST /api/v1/fin/journal-entries/from-event · Layers: `JournalEntryController.buildFromEvent`→`EventEntryService.build`
Request: the canonical accounting event payload (opaque shape, out-of-scope Event consumer, POL-FIN-020)
Response: 201 · `JournalEntryResponse` (statusCode=POSTED) — or a rejection recorded for operator follow-up (REQ-FIN-013)
Validations: RULE-FIN-004 (duplicate eventReference, QR-FIN-026), RULE-FIN-005 (active
rule exists, QR-FIN-027), RULE-FIN-003 (the single-remainder guarantee RULE-FIN-010 depends
on, re-checked over the stored line set, QR-FIN-016), RULE-FIN-010 (PER-SIDE remainder
difference, QR-FIN-028), then the same RULE-FIN-006/007/008/009 checks as API-FIN-019
(QR-FIN-029..032)
Errors: `FIN-409-DUPLICATE-EVENT`, `FIN-404-NO-ACTIVE-RULE`, `FIN-409-REMAINDER-COUNT`,
`FIN-422-REMAINDER-MARKER`, `FIN-422-REMAINDER-NOT-POSITIVE`, `FIN-409-UNBALANCED`,
`FIN-409-NOT-POSTABLE-ACCOUNT`, `FIN-409-PERIOD-NOT-OPEN`, `FIN-409-INVALID-DIMENSION`,
`FIN-422-MAPPING-UNSUPPORTED` (a rule line whose accountDerivationTypeCode is MAPPING — the
derivation fails loudly, since db-script-fin.md declares no mapping store; CONSTANT and
DIRECT are unaffected)
Orchestration: check RULE-FIN-004 (QR-FIN-026) → resolve active rule (QR-FIN-027) →
generate docNo (under the fiscal-year allocation lock, as API-FIN-019) → build lines from
the rule against the event's fields, computing the remainder line last as the difference
between the total already carried by the OPPOSING posting side and the total already carried
by the remainder line's own side (QR-FIN-028, QR-FIN-025) — a side-blind total would subtract
debit and credit lines alike from the base amount and produce a negative amount that dies on
CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE; a remainder that computes to zero or less is
`FIN-422-REMAINDER-NOT-POSITIVE`. Which line is the remainder is read from `isRemainderFl`
alone (DBF-FIN-103), the same marker RULE-FIN-003's guard counts → validate (QR-FIN-029..032)
→ post (QR-FIN-033) → return
Repository: QR-FIN-025, QR-FIN-026, QR-FIN-027, QR-FIN-028, QR-FIN-029..033 · join NONE ·
READ_WRITE (one transaction, build-through-post)
Security: system-to-system call (the Event consumer's own service principal), gated by
the same interceptor as any authenticated caller · `PERM_FIN_JOURNAL_ENTRIES_CREATE`
Localization: n/a (no free-text description supplied by an event)
<!-- API:API-FIN-020:END -->

<!-- API:API-FIN-014:START traces=REQ-FIN-023,REQ-FIN-024,DBF-FIN-115,DBF-FIN-112 -->
### API-FIN-014 — run template
Endpoint: POST /api/v1/fin/recurring-templates/{id}/run · Layers: `RecurringTemplateController.run`→`RecurringTemplateService.run`
Request: path `id` (or invoked by an internal scheduler with no path — the endpoint is the
same either way) · Response: 201 · `JournalEntryResponse`
Validations: same RULE-FIN-006/007/008/009 checks as API-FIN-019 (the template's own lines
were already balance-checked at API-FIN-013 create time, but re-validated here since
accounts/periods may have changed since)
Errors: same as API-FIN-019, plus `FIN-404-TEMPLATE` (unknown recurring template id)
Orchestration: load template (QR-FIN-019) → build entry from its lines (journalTypeCode=
RECURRING) → validate + post (QR-FIN-029..033) → advance nextRunDate per frequencyCode →
if scheduleTypeCode=REVERSING: also build and post the linked reversal in the next period
(REQ-FIN-024, reusing RULE-FIN-011/012 via QR-FIN-034/036) → return
Repository: QR-FIN-019, QR-FIN-024(-style build), QR-FIN-029..033, QR-FIN-034, QR-FIN-036
· join NONE · READ_WRITE
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_UPDATE` (running
a template is modeled as an update-class custom action) · Localization: n/a
<!-- API:API-FIN-014:END -->

<!-- API:API-FIN-017:START traces=REQ-FIN-026,DBF-FIN-133,DBF-FIN-145,DBF-FIN-146 -->
### API-FIN-017 — run allocation rule
Endpoint: POST /api/v1/fin/allocation-rules/{id}/run · Layers: `AllocationRuleController.run`→`AllocationRuleService.run`
Request: path `id` · Response: 201 · `JournalEntryResponse`
Validations: RULE-FIN-003 (single remainder marker over the stored targets, QR-FIN-016) and
RULE-FIN-010 (per-side remainder guarantee, QR-FIN-028, reused) then RULE-FIN-006/007/008/009
Errors: same family as API-FIN-019, plus `FIN-404-ALLOCATION-RULE`,
`FIN-409-REMAINDER-COUNT`, `FIN-422-REMAINDER-MARKER`, `FIN-422-REMAINDER-NOT-POSITIVE`
Orchestration: load rule + targets + current source-account balance (QR-FIN-022) →
distribute per target's distributionTypeCode, the target marked `isRemainderFl`
(DBF-FIN-141, the single marker — not `distributionTypeCode`) absorbing the rounding
difference between the source line's side and the targets' own side (QR-FIN-028) → build
entry (journalTypeCode=ALLOCATION) → validate + post (QR-FIN-029..033) → return
Repository: QR-FIN-022, QR-FIN-028, QR-FIN-029..033 · join NONE · READ_WRITE
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_UPDATE` · Localization: n/a
<!-- API:API-FIN-017:END -->

<!-- API:API-FIN-021:START traces=REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,DBF-FIN-042,DBF-FIN-043 -->
### API-FIN-021 — reverse entry
Endpoint: POST /api/v1/fin/journal-entries/{id}/reverse · Layers: `JournalEntryController.reverse`→`JournalEntryService.reverse`
Request: path `id` · Response: 201 · `JournalEntryResponse` (the new reversal entry)
Validations: RULE-FIN-013 (must be POSTED and not already reversed, QR-FIN-035); RULE-FIN-012
(period substitution if original's is closed, QR-FIN-036)
Errors: `FIN-409-NOT-POSTED`, `FIN-409-ALREADY-REVERSED`, `FIN-404-ENTRY`
Orchestration: load original UNDER A ROW LOCK (`SELECT ... FOR UPDATE` on the
FIN_JOURNAL_ENTRY header — no FIN entity carries `@Version`, so without it two concurrent
reversals both read reversalEntryId as null, both post a mirror, and the second save
overwrites the first link, leaving one orphaned mirror and a net effect of −(original)) →
check RULE-FIN-013 (QR-FIN-035) → resolve posting period
(original's if Open, else the current open period per RULE-FIN-012, QR-FIN-036) → build
mirrored lines with opposite directions, same amounts (RULE-FIN-011, QR-FIN-034) →
validate + post (QR-FIN-029..033, journalTypeCode=REVERSAL) → set originalEntryId/
reversalEntryId on both rows (bidirectional link) → return. Classic reversal: the original
STAYS POSTED — the only write back to it is the reversalEntryId link (DBF-FIN-043); its
statusCode is never modified, so both entries are seen by the POSTED-only report queries and
the pair nets to zero (SRS A7). Because the original stays POSTED, the double-reversal half of
RULE-FIN-013 is an explicit guard on the reversal link: a second reverse on the same entry is
rejected with `FIN-409-ALREADY-REVERSED` (AC-FIN-030), which would otherwise post a second
mirror and leave a net effect of −(original).
Repository: QR-FIN-034, QR-FIN-035, QR-FIN-036, QR-FIN-029..033 · join NONE · READ_WRITE
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_REVERSE` (custom action) · Localization: n/a
<!-- API:API-FIN-021:END -->

<!-- API:API-FIN-023:START traces=REQ-FIN-031,DBF-FIN-066,DBF-FIN-067,DBF-FIN-068,DBF-FIN-077 -->
### API-FIN-023 — create fiscal year
Endpoint: POST /api/v1/fin/fiscal-years · Layers: `FiscalYearController.create`→`FiscalYearService.create`
Request: `{code, startDate, endDate, periodCount}` · Response: 201 · `FiscalYearResponse` with its generated periods
Validations: uniqueness of code
Errors: `FIN-409-YEAR-DUP`
Orchestration: validate → persist year (statusCode=OPEN) + generate periodCount periods,
each statusCode=OPEN (QR-FIN-038) → return
Period spans and names (governed, not implicit): when `periodCount` = 12 and
[startDate, endDate] spans one whole calendar year, each generated period IS a calendar
month — period N runs from the 1st to the last day of the Nth month of the span — and its
NAME_AR / NAME_EN (DBF-FIN-078/079, both NOT NULL) are that month's own name in Arabic and
English, taken from the platform's locale data (`java.time.Month` + CLDR), never a
hardcoded string table. For any other `periodCount`, or a span that is not a whole year,
the fallback is the even split: the days of [startDate, endDate] divided into periodCount
contiguous blocks, the first (totalDays mod periodCount) blocks one day longer, so the last
period always ends on the year's endDate and no day belongs to two periods; those periods
are named "الفترة N" / "Period N".
Repository: QR-FIN-038 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_CREATE` · Localization: n/a
<!-- API:API-FIN-023:END -->

<!-- API:API-FIN-024:START traces=REQ-FIN-032,DBF-FIN-082 -->
### API-FIN-024 — open period
Endpoint: PATCH /api/v1/fin/fiscal-periods/{id}/open · Layers: `FiscalPeriodController.open`→`FiscalPeriodService.open`
Request: path `id` · Response: 200 · `FiscalPeriodResponse`
Validations: current state must be SOFT_CLOSE (§10.2 "re-openable" — Hard Closed is not, RULE-FIN-014)
Errors: `FIN-409-NOT-REOPENABLE`, `FIN-404-PERIOD`
Orchestration: load → check RULE-FIN-014 (QR-FIN-040) → transition (QR-FIN-039) → return
Repository: QR-FIN-039, QR-FIN-040 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_UPDATE` · Localization: n/a
<!-- API:API-FIN-024:END -->

<!-- API:API-FIN-025:START traces=REQ-FIN-033,DBF-FIN-082 -->
### API-FIN-025 — soft-close period
Endpoint: PATCH /api/v1/fin/fiscal-periods/{id}/soft-close · Layers: `FiscalPeriodController.softClose`→`FiscalPeriodService.softClose`
Request: path `id` · Response: 200 · `FiscalPeriodResponse`
Validations: current state must be OPEN · Errors: `FIN-409-INVALID-TRANSITION`, `FIN-404-PERIOD`
Orchestration: load → transition (QR-FIN-039) → return · Repository: QR-FIN-039 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_UPDATE` · Localization: n/a
<!-- API:API-FIN-025:END -->

<!-- API:API-FIN-026:START traces=REQ-FIN-034,REQ-FIN-035,REQ-FIN-037,REQ-FIN-038,DBF-FIN-082,DBF-FIN-083,DBF-FIN-084 -->
### API-FIN-026 — hard-close period (approval)
Endpoint: PATCH /api/v1/fin/fiscal-periods/{id}/hard-close · Layers: `FiscalPeriodController.hardClose`→`FiscalPeriodService.hardClose`
Request: path `id` · Response: 200 · `FiscalPeriodResponse`
Validations: RULE-FIN-014 (not already Hard Closed, QR-FIN-040); RULE-FIN-015 (full text:
DATA-DOM §ENT-FIN-008) — caller must hold `PERM_FIN_PERIODS_CLOSE_APPROVE`, a permission
distinct from `PERM_FIN_JOURNAL_ENTRIES_CREATE` (checked by the CORE interceptor plus the
service-layer distinctness read, Phase 1 CORE)
Errors: `FIN-409-NOT-REOPENABLE`(reused message context), `FIN-403-SOD-VIOLATION`, `FIN-404-PERIOD`
Orchestration: load → check RULE-FIN-014 (QR-FIN-040) → check RULE-FIN-015 (SEC role read)
→ transition, set closedBy/closedAt to the approving principal (QR-FIN-039) → return
Repository: QR-FIN-039, QR-FIN-040 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_CLOSE_APPROVE` (custom, SoD-gated) · Localization: n/a
<!-- API:API-FIN-026:END -->

<!-- API:API-FIN-027:START traces=REQ-FIN-036,DBF-FIN-069,DBF-FIN-034 -->
### API-FIN-027 — run year-end close
Endpoint: POST /api/v1/fin/fiscal-years/{id}/year-end-close · Layers: `FiscalYearController.yearEndClose`→`FiscalYearService.yearEndClose`
Request: path `id` · Response: 201 · `{closingEntry: JournalEntryResponse, openingEntry: JournalEntryResponse}`
Validations, IN THIS ORDER (the order is part of the contract — a test asserting 403 or 409
on this endpoint must not be defeated by a missing successor year or Retained Earnings
account): (1) RULE-FIN-015, the SoD read, 403; (2) the fiscal year's own statusCode must
still be OPEN — a re-run against an already-CLOSED year is rejected with
`FIN-409-INVALID-TRANSITION`, a stated rule on ENT-FIN-007's state machine rather than the
incidental `FIN-409-PERIODS-NOT-CLOSED` the first run's own YEAR_END_CLOSE transitions would
otherwise raise; (3) every period of the year must be HARD_CLOSE (§10.4 precondition), 409.
Only then are the successor year and the Retained Earnings account resolved
Errors: `FIN-403-SOD-VIOLATION`, `FIN-409-INVALID-TRANSITION` (year already closed), `FIN-409-PERIODS-NOT-CLOSED`, `FIN-404-YEAR` (unknown year id, or no adjacent successor year), `FIN-404-ACCOUNT` (no account marked as Retained Earnings)
Orchestration: read the SoD facts → verify the year is still OPEN → verify all periods Hard
Closed → resolve the successor year and the Retained Earnings account → compute closing
balances (QR-FIN-041) →
build + post a closing entry into the year's LAST period (result accounts → Retained
Earnings, journalTypeCode=CLOSING). The Retained Earnings account is DERIVED BY THE SYSTEM
from FIN_ACCOUNT.is_retained_earnings_fl (DBF-FIN-147, at most one row TRUE — partial unique
index UQ_FIN_ACCOUNT_RETAINED_EARNINGS) and is never supplied by the caller; no marked
account is `FIN-404-ACCOUNT`
→ build + post the next year's opening entry from the resulting balance-sheet balances
(journalTypeCode=OPENING, QR-FIN-041, POL-FIN-010) — CLOSING/OPENING are two values added
to the JOURNAL_TYPE lookup this stage (see Decisions Applied — a data-only, non-breaking
extension, not a new REQ/RULE) → mark FiscalYear statusCode=CLOSED, periods
statusCode=YEAR_END_CLOSE → return
Repository: QR-FIN-041, QR-FIN-029, QR-FIN-030, QR-FIN-032, QR-FIN-033 (both generated
entries go through the same validated posting pipeline) · QR-FIN-031 is deliberately NOT
applied to either of them: RULE-FIN-008 itself exempts the year-end closing and opening
entries, since this API's own all-periods-Hard-Closed precondition leaves no Open period for
the closing entry to post into. RULE-FIN-006/007/009 apply to both in full · the successor
fiscal year receiving the opening entry is resolved by DATE ADJACENCY (the year whose
startDate is the day after this year's endDate) — a DERIVED decision, recorded as such in
execution-state.json because ENT-FIN-007 declares no successor column; no such year is
`FIN-404-YEAR` · join intra-module · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_CLOSE_APPROVE` · Localization: n/a
DORMANT YEAR — a reviewed decision, not an accident. For a fiscal year in which no result account
carries a non-zero balance, `FiscalYearService.closingLines` contributes no line at all (each
result account with `net().signum() == 0` is skipped, and the Retained Earnings absorbing line is
added only when the running `resultTotal` is itself non-zero), and symmetrically
`openingLines` contributes none when no balance-sheet account carries a non-zero balance. The run
therefore posts a CLOSING (and OPENING) journal entry with an EMPTY line set, and that entry still
consumes a `docNo`: `JournalPostingService.buildValidateAndPost` allocates the number from the
locked fiscal-year series before it validates, and writes the entry unconditionally.
This was reviewed and deliberately KEPT. `JournalEntryDomain.checkBalanced` sums debits and
credits and returns empty when they compare equal, so 0 = 0 passes — a lineless entry literally
satisfies AC-FIN-036's "both entries individually balanced". Omitting the entry would make
AC-FIN-036's "posts a closing entry" false, and refusing the close outright would invent a rule no
artifact states.
Reachable only from here. `JournalEntryCreateRequest.lines` carries `@NotEmpty`, so API-FIN-019
rejects a lineless entry at the DTO boundary. This shape exists solely on the internal year-end
path, which builds its lines itself and never passes through that DTO.
<!-- API:API-FIN-027:END -->
<!-- SUB:SVC-API-INT:END -->
<!-- PHASE:SVC-API:END -->

<!-- PHASE:DOC:START traces=REQ-FIN-017 -->
## PHASE 4 — DOC

**API contract summary** (backend self-check only; the frontend stage binds to the real
`api-docs-fin.md` published after implementation): 32 endpoints under `/api/v1/fin`, one
row per API-FIN-001..032 (path/verb/request/response per the API Registry table in the
Plan Index above — not restated a third time here, single-source rule).

**DTO typing constraints**: every lookup-backed field (`accountTypeCode`, `natureCode`,
`directionCode`, `journalTypeCode`, `statusCode`, `eventTypeCode`,
`accountDerivationTypeCode`, `amountSourceTypeCode`, `distributionTypeCode`,
`scheduleTypeCode`, `frequencyCode`) is `String`, never a Java enum; `docNo` never appears
in any create/update request body, always in every response.

**Pagination + filter standard**: identical to SEC's/MDL's own Phase 1 CORE.
<!-- PHASE:DOC:END -->

<!-- PHASE:INT-C:START traces=REQ-FIN-001 -->
## PHASE 5 — INT-C (cross-module consume)

Two `XM-*` rows (XM-FIN-001, XM-FIN-002), below the split threshold (2 < 5) — no SUB opened.
XM-FIN-002 was assigned at ALIGN-BE: SEC-BE landed after this phase was written and added a
second, genuinely distinct cross-module consumption (see its block and the ADR-FIN-001 note
below).

<!-- XM:XM-FIN-001:START traces=REQ-FIN-001,REQ-FIN-007,REQ-FIN-008,REQ-FIN-010,REQ-FIN-014,REQ-FIN-018,REQ-FIN-022,REQ-FIN-025,REQ-FIN-031 -->
### XM-FIN-001 — validate/read lookup-backed codes against MDL
Target        : MDL · ENT-MDL-001/002 (LookupType/LookupValue) · classification SOFT-READ
Interface     : in-process Spring injection — FIN's service layer injects
`com.erp.mdl.crossmodule.MdlLookupApi` and calls `readActiveValuesByKey(typeKey)` for every
one of FIN's 13 owned keys, at the point each lookup-backed field is written or offered as a select-list
Contract      : data required = the submitted code exists as an active value under the
named type, checked by membership in the returned `List<LookupOptionView>` (code, labelAr,
labelEn, sortOrder); fallback if absent = reject with `FIN-400-INVALID-LOOKUP`; an unknown/
inactive `typeKey` raises MDL's `LocalizedException(NOT_FOUND, MDL_404_TYPE_KEY)`, which FIN
catches and translates to `FIN-400-INVALID-LOOKUP` (MDL's code never leaks out of FIN's API);
retry = none (same-process call, no network hop); idempotency
= read-only, naturally idempotent
Blocks        : none DEFERRED — MDL v1 is already gated (pass-1 APPROVE); ACTIVE from the
moment FIN v1 is created
<!-- XM:XM-FIN-001:END -->

<!-- XM:XM-FIN-002:START traces=REQ-FIN-037,REQ-FIN-038 -->
### XM-FIN-002 — read SEC's user→permission directory for the RULE-FIN-015 SoD fact
Target        : SEC · ENT-SEC-001 (User) + SEC's role/permission grant tables · classification READ
Interface     : in-process Spring injection — `FinSeparationOfDutiesService` injects
`com.erp.sec.crossmodule.SecUserDirectoryApi` and calls
`findUserIdsHoldingPermission(permissionCode)` twice, for
`PERM_FIN_PERIODS_CLOSE_APPROVE` and `PERM_FIN_JOURNAL_ENTRIES_CREATE`, on every API-FIN-026
hard-close and API-FIN-027 year-end close. Never HTTP.
Contract      : data required = the set of user ids holding each permission, across each user's
role union; FIN derives two booleans from them (`closeApprovePermissionHeld`,
`entryCreatePermissionShared`) and hands them to `FiscalPeriodDomain.assertCanHardClose`, which
owns the decision; fallback if absent = a failed or refused directory read is caught and
re-raised as `FIN-403-SOD-VIOLATION` — the close is REFUSED, never approved on an unverified
fact; retry = none (same-process call); idempotency = read-only, naturally idempotent
Blocks        : none DEFERRED — SEC is already gated; ACTIVE from the moment SEC-BE landed
<!-- XM:XM-FIN-002:END -->

FIN's dependency on SEC for identity/authorization (the principal on every request, the
`@PreAuthorize` gate, and FIN's own self-registration of its module/screens/actions into SEC)
remains NOT a formal `XM` row — ADR-FIN-001 (carried from P2), unchanged. XM-FIN-002 is a
different thing and is registered separately: it is not the platform's ambient authorization of
the caller but FIN's own business logic reading SEC's data *about other users* — a named FIN
service calling a named SEC `crossmodule` interface method, whose returned rows are an input to
a FIN business rule (RULE-FIN-015) and whose absence has a defined, FIN-owned failure code. That
is exactly the shape ADR-FIN-001 excludes only the ambient case from, and exactly the shape
XM-FIN-001 already has for MDL. Registering it costs nothing and makes FIN's dependency on
SEC's read model visible upstream; leaving it unregistered would hide a real consumption behind
an ADR written about a different one.
<!-- PHASE:INT-C:END -->

<!-- PHASE:INT-R:START traces=REQ-FIN-001 -->
## PHASE 6 — INT-R (cross-module resolve)

| XM | Status | Workaround (if not READY/ACTIVE) |
|---|---|---|
| XM-FIN-001 | ACTIVE | not applicable — target already gated |
| XM-FIN-002 | ACTIVE | not applicable — target already gated (SEC); a failed directory read is refused as `FIN-403-SOD-VIOLATION`, never bypassed |

No DEFERRED row exists. FIN is the third and last module of this batch
(GENERATION-INSTRUCTIONS.md §3); the next module to consume FIN (first candidate: PRC or
any future Tier-2/3 module needing accounting integration) is out of this batch's scope —
`XM-INBOUND-STUB-3` names FIN as the eventual target, formal id assigned by that module's
own P2 when it exists.
<!-- PHASE:INT-R:END -->

<!-- PHASE:SEC-BE:START traces=REQ-FIN-038,REQ-FIN-044 -->
## PHASE 7 — SEC-BE (security, backend half)

| Screen (page code) | VIEW | CREATE | UPDATE | DELETE | Custom |
|---|---|---|---|---|---|
| FIN_ACCOUNTS | ✓ (API-FIN-001) | ✓ (API-FIN-002) | ✓ (API-FIN-003, and API-FIN-004 deactivate) | — | — |
| FIN_DIMENSIONS | ✓ (API-FIN-005,008) | ✓ (API-FIN-006,007) | ✓ (API-FIN-035, deactivate a dimension VALUE — `PERM_FIN_DIMENSIONS_UPDATE`, added by V28) | — | — |
| FIN_RULES | ✓ (API-FIN-009) | ✓ (API-FIN-010) | ✓ (API-FIN-011, add line; and API-FIN-034, deactivate rule) | — | — |
| FIN_RECURRING_TEMPLATES | ✓ (API-FIN-012) | ✓ (API-FIN-013) | ✓ (API-FIN-014, run) | — | — |
| FIN_ALLOCATION_RULES | ✓ (API-FIN-015) | ✓ (API-FIN-016) | ✓ (API-FIN-017, run) | — | — |
| FIN_JOURNAL_ENTRIES | ✓ (API-FIN-018,022) | ✓ (API-FIN-019,020) | — | — | Reverse (`PERM_FIN_JOURNAL_ENTRIES_REVERSE`, API-FIN-021) |
| FIN_PERIODS | ✓ (API-FIN-033, search periods — and still the gateway, see below) | ✓ (API-FIN-023, year) | ✓ (API-FIN-024,025) | — | Close-approve (`PERM_FIN_PERIODS_CLOSE_APPROVE`, API-FIN-026,027 — RULE-FIN-015 SoD) |
| FIN_ACCOUNT_LEDGER | ✓ (API-FIN-028) | — | — | — | — |
| FIN_TRIAL_BALANCE | ✓ (API-FIN-029) | — | — | — | — |
| FIN_BALANCE_SHEET | ✓ (API-FIN-030) | — | — | — | — |
| FIN_INCOME_STATEMENT | ✓ (API-FIN-031) | — | — | — | — |
| FIN_DIMENSION_REPORTS | ✓ (API-FIN-032) | — | — | — | — |

**DELETE column — deliberately empty everywhere.** FIN exposes no `DELETE` endpoint at all.
Deactivation is `PUT /{id}/deactivate` gated by the screen's UPDATE permission (see
`AccountService.deactivate`, `@PreAuthorize` on `PERM_FIN_ACCOUNTS_UPDATE`), exactly as
MDL_LOOKUPS models it, and neither V24 nor V28 seeds a `PERM_FIN_*_DELETE` row for any FIN screen.
The FIN_ACCOUNTS row above read "✓ deactivate (API-FIN-004)" under DELETE until ALIGN-BE moved it
to UPDATE; inventing DELETE rows here would create permanently-unreferenced registry data. The two
later deactivates follow the same modelling: API-FIN-034 (event-type rule) under FIN_RULES/UPDATE
and API-FIN-035 (dimension value) under FIN_DIMENSIONS/UPDATE. There are three deactivate
endpoints in FIN and no `activate` anywhere — API-FIN-034 and API-FIN-035 each deliberately omit a
counterpart, following the delivered `AccountService.deactivate` precedent.

**FIN_DIMENSIONS / UPDATE — the one cell V24 did not seed.** `PERM_FIN_DIMENSIONS_UPDATE` is
declared in `PermissionConstants` and registered by `V28__fin_dimensions_update_action.sql`, which
also grants it explicitly to `SYS_ADMIN`. The explicit grant is not optional: V25 grants SYS_ADMIN
its FIN actions with a `SELECT` over `SEC_ACTION_REG` and has already run everywhere, so Flyway
will never re-evaluate it against a row inserted later — registering without granting is exactly
the MDL failure V19/V21 had to repair. Tiers 1 and 2 need nothing new (V25 already grants SYS_ADMIN
the FIN module row and every FIN screen, FIN_DIMENSIONS included), and the RULE-SEC-007 gateway
holds because V24/V25 already registered and granted `PERM_FIN_DIMENSIONS_VIEW` on the same screen.
`FIN_CLOSE_APPROVER` (V27) deliberately gets nothing from V28 — its grants are scoped to
FIN_PERIODS and two permission codes.

**FIN_RULES / UPDATE — one permission, two endpoints.** API-FIN-034 reuses the pre-existing
`PERM_FIN_RULES_UPDATE` that V24 already seeds and V25 already granted, so it needed no new
constant, no new error code and no migration.

**FIN_PERIODS / VIEW — a gateway that now also has an endpoint.** `PERM_FIN_PERIODS_VIEW` is a
real V24 action row and is load-bearing: `MenuService.effectiveAuthorityCodes()` keeps a granted
permission only if the same screen also carries a granted gateway (VIEW) action, so V27's
FIN_CLOSE_APPROVER role must hold it for `PERM_FIN_PERIODS_CLOSE_APPROVE` to survive into the
caller's authorities. That much is unchanged.

What HAS changed: the row is no longer constant-less and no longer endpoint-less. API-FIN-033
(`POST /api/v1/fin/fiscal-periods/search`, `FiscalPeriodService.search`) is gated on
`PERM_FIN_PERIODS_VIEW`, and the matching `PermissionConstants` constant was added with it. No
migration was needed — the action row was already registered by V24 and already granted by V25 and
V27. This section previously recorded the opposite state ("no `PermissionConstants` constant,
because FIN publishes no fiscal-period read endpoint … §B5 and the API registry define no search
API"); that was accurate before API-FIN-033 and is superseded now. srs-fin.md SCR-REQ-FIN-007 §B5
carries the endpoint row.

**Seed data** (REQ-FIN-044): 12 SEC_PAGES rows registered via SEC's screen-registration
endpoint at FIN onboarding; one action row per action above via SEC's action-registration
endpoint, following `PERM_<PAGE_CODE>_<ACTION>` — including the two custom actions
(`PERM_FIN_JOURNAL_ENTRIES_REVERSE`, `PERM_FIN_PERIODS_CLOSE_APPROVE`). Delivered as migrations
V24 (registry) and V25 (SYS_ADMIN grants); V27 adds the dedicated `FIN_CLOSE_APPROVER` role; V28
adds the one later action row, `FIN_DIMENSIONS / UPDATE`, together with its own explicit SYS_ADMIN
grant.

**SoD enforcement (RULE-FIN-015, POL-FIN-016)**: `PERM_FIN_PERIODS_CLOSE_APPROVE` and
`PERM_FIN_JOURNAL_ENTRIES_CREATE` must never be held by the same role by platform
convention (an administrative guideline enforced by role design, not a database
constraint — SEC's RBAC model grants permissions per role, and FIN's service layer
additionally checks at hard-close/year-end-close time that no single *user* holds both,
per role union, Phase 1 CORE). The role that holds close-approval is
`FIN_CLOSE_APPROVER`, minted by V27: module grant FIN, screen grant FIN_PERIODS only, and
exactly two action grants (`PERM_FIN_PERIODS_VIEW` gateway + `PERM_FIN_PERIODS_CLOSE_APPROVE`),
so it structurally cannot carry entry creation. It is assigned to NO user by V27 on purpose —
the assignee must be someone who holds no role carrying `PERM_FIN_JOURNAL_ENTRIES_CREATE`, which
excludes the bootstrap `admin` (SYS_ADMIN), and that is an environment-specific administrative
act rather than schema. Until such an assignment exists, API-FIN-026 and API-FIN-027 answer
`FIN-403-SOD-VIOLATION` (nobody holds close-approval is a failure just as surely as one user
holding both). The SEC read behind this check is XM-FIN-002.

**Gateway**: every non-VIEW permission requires VIEW on the same screen first (platform
convention, SEC's own interceptor — not restated as a FIN-owned RULE).

**Forbidden responses**: `FIN-403-SOD-VIOLATION` maps through the `LocalizedException`
envelope, carrying a registered FIN code and both ar/en messages. `FIN-403-FORBIDDEN` still does
NOT, and that half is unchanged: an authorization failure at `@PreAuthorize` is rendered by
`com.erp.common.web.GlobalExceptionHandler.handleAccessDenied`, which emits the platform code
`ACCESS_DENIED`; `FIN-403-FORBIDDEN` is in no `FinErrorCodes` constant and in neither i18n bundle.
It is the catalog's *name* for that platform response, not a FIN code, and **FIN still has no
`FIN-403-FORBIDDEN` on the wire**.

What DID change, platform-wide: the 403 body is now localized. `handleAccessDenied` resolves its
message through the same `resolveMessage(...)` helper and `MessageSource` every other handler uses,
keyed on a new `CommonErrorCodes.ACCESS_DENIED` constant, and `ACCESS_DENIED` was added to BOTH
`messages.properties` and `messages_ar.properties`. The wire `code` is unchanged and the English
text is byte-identical to the string that was hardcoded before, so only Arabic callers observe any
difference. The earlier description of this response as carrying "a hardcoded English message,
bypassing `MessageSource`" is therefore no longer accurate; the separate point — that this is a
platform response and not a FIN code — still stands, as does the fact that routing it through
`LocalizedException` would change every module's 403 envelope and remains a platform decision.
<!-- PHASE:SEC-BE:END -->

<!-- PHASE:ALIGN-BE:START traces=REQ-FIN-017 -->
## PHASE 8 — ALIGN-BE

See Alignment self-check (ALIGN) below.
<!-- PHASE:ALIGN-BE:END -->

## Error Catalog — FIN v1

Envelope: `LocalizedException → {code, messageAr, messageEn}`. Runtime code format: `FIN-{http}[-{SLUG}]`.

| code | RULE / PLATFORM-STD | API | HTTP | trigger | message-AR | message-EN |
|---|---|---|---|---|---|---|
| FIN-409-ACCOUNT-DUP | PLATFORM-STD | API-FIN-002 | 409 | duplicate account code | رمز الحساب مستخدم بالفعل | Account code already in use |
| FIN-409-PARENT-NOT-LEAF-ELIGIBLE | RULE-FIN-001 | API-FIN-002 | 409 | parent account cannot remain a leaf | لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا | An account with sub-accounts cannot accept direct posting |
| FIN-400-INVALID-LOOKUP | PLATFORM-STD (XM-FIN-001) | many | 400 | submitted code not found in MDL | القيمة المُدخلة غير صالحة | The submitted value is not valid |
| FIN-409-HAS-CHILDREN | RULE-FIN-001 | API-FIN-003 | 409 | marking a parent as leaf | لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا | An account with sub-accounts cannot accept direct posting |
| FIN-404-ACCOUNT | PLATFORM-STD | API-FIN-003, 004, 028 | 404 | unknown account id | الحساب غير موجود | Account not found |
| FIN-409-DIMENSION-DUP | PLATFORM-STD | API-FIN-006 | 409 | duplicate dimension code | رمز البُعد مستخدم بالفعل | Dimension code already in use |
| FIN-409-DIMVALUE-DUP | RULE-FIN-002 | API-FIN-007 | 409 | duplicate code within dimension | هذا الرمز مستخدم بالفعل ضمن هذا البُعد | This code is already used within this dimension |
| FIN-404-DIMENSION | PLATFORM-STD | API-FIN-007, 008, 032 | 404 | unknown dimension id | البُعد غير موجود | Dimension not found |
| FIN-404-DIMVALUE | PLATFORM-STD | API-FIN-035 | 404 | unknown dimension-value id | قيمة البُعد غير موجودة | Dimension value not found |
| FIN-409-RULE-DUP | PLATFORM-STD (§6.4) | API-FIN-010 | 409 | event type already has an active rule | يوجد بالفعل قاعدة نشطة لهذا النوع | An active rule already exists for this event type |
| FIN-409-REMAINDER-COUNT | RULE-FIN-003 | API-FIN-011, 016 | 409 | wrong remainder-line/target count | يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي | Exactly one remainder line is required when any percentage distribution is present |
| FIN-404-RULE | PLATFORM-STD | API-FIN-011, 034 | 404 | unknown event-type rule id | القاعدة غير موجودة | Rule not found |
| FIN-400-MISSING-FREQUENCY | PLATFORM-STD | API-FIN-013 | 400 | recurring template with no frequency | يلزم تحديد التكرار للقالب المتكرر | A frequency is required for a recurring template |
| FIN-404-TEMPLATE | PLATFORM-STD | API-FIN-014 | 404 | unknown recurring template id | القالب المتكرر غير موجود | Recurring template not found |
| FIN-404-ALLOCATION-RULE | PLATFORM-STD | API-FIN-017 | 404 | unknown allocation rule id | قاعدة التوزيع غير موجودة | Allocation rule not found |
| FIN-409-UNBALANCED | RULE-FIN-006 | API-FIN-019, 020, 014, 017 | 409 | debits ≠ credits | القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن | The entry is unbalanced — total debits do not equal total credits |
| FIN-409-NOT-POSTABLE-ACCOUNT | RULE-FIN-007 | API-FIN-019, 020, 014, 017 | 409 | non-leaf/inactive account | الحساب المستهدف لا يقبل ترحيلاً مباشرًا | The target account does not accept direct posting |
| FIN-409-PERIOD-NOT-OPEN | RULE-FIN-008 | API-FIN-019, 020, 014, 017 | 409 | period not Open at post | الفترة المستهدفة غير مفتوحة | The target period is not open |
| FIN-409-INVALID-DIMENSION | RULE-FIN-009 | API-FIN-019, 020, 014, 017 | 409 | invalid/inactive dimension value | قيمة البُعد غير صالحة | The dimension value is invalid |
| FIN-409-DUPLICATE-EVENT | RULE-FIN-004 | API-FIN-020 | 409 | repeated eventReference | تم بالفعل ترحيل قيد لهذا المرجع | An entry for this event reference has already been posted |
| FIN-404-NO-ACTIVE-RULE | RULE-FIN-005 | API-FIN-020 | 404 | no active rule for event type | لا توجد قاعدة نشطة لهذا النوع من الأحداث | No active rule exists for this event type |
| FIN-422-MAPPING-UNSUPPORTED | PLATFORM-STD | API-FIN-020 | 422 | rule line with accountDerivationTypeCode=MAPPING, for which no mapping store is declared in db-script-fin.md | يستخدم هذا السطر اشتقاق الحساب عبر جدول المطابقة وهو غير متاح حاليًا | This rule line uses mapping-based account derivation, which is not available yet |
| FIN-409-NOT-POSTED | RULE-FIN-013 | API-FIN-021 | 409 | reversing a non-posted entry | لا يمكن عكس قيد غير مُرحَّل | A non-posted entry cannot be reversed |
| FIN-409-ALREADY-REVERSED | RULE-FIN-013 | API-FIN-021, 014 | 409 | reversing an entry that already carries a reversal link | تم عكس هذا القيد بالفعل ولا يمكن عكسه مرة أخرى | This entry has already been reversed and cannot be reversed again |
| FIN-404-ENTRY | PLATFORM-STD | API-FIN-021, 022 | 404 | unknown entry id | القيد غير موجود | Entry not found |
| FIN-409-YEAR-DUP | PLATFORM-STD | API-FIN-023 | 409 | duplicate fiscal year code | رمز السنة المالية مستخدم بالفعل | Fiscal year code already in use |
| FIN-409-NOT-REOPENABLE | RULE-FIN-014 | API-FIN-024, 026 | 409 | period Hard Closed | الفترة مغلقة إغلاقًا صارمًا ولا يمكن إعادة فتحها | The period is hard-closed and cannot be reopened |
| FIN-409-INVALID-TRANSITION | PLATFORM-STD | API-FIN-025, 027 | 409 | period not in the expected state; fiscal year already CLOSED when year-end close is re-run | لا يمكن تنفيذ هذا الانتقال من الحالة الحالية | This transition is not allowed from the current status |
| FIN-403-SOD-VIOLATION | RULE-FIN-015 | API-FIN-026, 027 | 403 | close-approver also holds entry-creation permission | صلاحية اعتماد الإغلاق منفصلة عن صلاحية إنشاء القيود | The close-approval permission is separate from the entry-creation permission |
| FIN-404-PERIOD | PLATFORM-STD | API-FIN-024, 025, 026, 029, 031 | 404 | unknown period id — on API-FIN-029 only when the OPTIONAL `periodId` is actually supplied (omitting it stays a 200); API-FIN-031's `fromPeriodId`/`toPeriodId` already raised it before the report-404 change and are unchanged | الفترة غير موجودة | Period not found |
| FIN-409-PERIODS-NOT-CLOSED | PLATFORM-STD (§10.4 precondition) | API-FIN-027 | 409 | not every period Hard Closed | يجب إغلاق كل الفترات إغلاقًا صارمًا أولًا | Every period must be hard-closed first |
| FIN-404-YEAR | PLATFORM-STD | API-FIN-027, 030, 031 | 404 | unknown fiscal year id — on API-FIN-030 and API-FIN-031 the REQUIRED `fiscalYearId` is resolved first and raises this instead of the silent all-zero 200 those reports previously returned | السنة المالية غير موجودة | Fiscal year not found |
| FIN-422-REMAINDER-MARKER | RULE-FIN-003, RULE-FIN-010 | API-FIN-011, 016, 017, 020 | 422 | `isRemainderFl` (DBF-FIN-103/141) disagrees with the line's/target's own REMAINDER type code, so which line is the remainder is ambiguous | علامة سطر الباقي لا تتفق مع نوع التوزيع أو مصدر المبلغ لنفس السطر | The remainder marker disagrees with the line's own distribution or amount-source type |
| FIN-422-REMAINDER-NOT-POSITIVE | RULE-FIN-010 | API-FIN-017, 020 | 422 | the remainder line's per-side difference is zero or negative — the other lines on its side already equal or exceed the opposing side (POL-FIN-005) | سطر الباقي يُحسب كفرق ويجب أن يكون موجبًا؛ السطور الأخرى تستهلك المبلغ بالكامل | The remainder line is computed as a difference and must be positive; the other lines already consume the full amount |
| FIN-400-PERIOD-NOT-IN-YEAR | RULE-FIN-017 | API-FIN-019 | 400 | submitted `periodId` belongs to a different fiscal year than the submitted `fiscalYearId` (DBF-FIN-076) | الفترة المحددة لا تتبع السنة المالية المحددة | The selected period does not belong to the selected fiscal year |
| FIN-400-DOCDATE-OUTSIDE-PERIOD | RULE-FIN-017 | API-FIN-019 | 400 | submitted `docDate` falls outside the submitted period's `[startDate, endDate]` span (DBF-FIN-080/081) | تاريخ المستند خارج نطاق الفترة المحددة | The document date falls outside the selected period |
| FIN-403-FORBIDDEN | PLATFORM-STD (CORE interceptor) | every secured API | 403 | missing module/screen/action grant — **rendered on the wire as the platform `ACCESS_DENIED` envelope, not as this code**: `GlobalExceptionHandler.handleAccessDenied` builds an `ApiError` whose code is the `CommonErrorCodes.ACCESS_DENIED` constant. The message is now LOCALIZED — it is resolved through the same `resolveMessage(...)`/`MessageSource` path as every other handler, and `ACCESS_DENIED` was added to both i18n bundles; the wire `code` is unchanged and the English text is byte-identical to the string previously hardcoded, so only Arabic callers see a difference. (This row previously said the message was hardcoded English, bypassing `MessageSource`; that is no longer true.) This row remains the catalog's name for that response, not a `FinErrorCodes` constant, and `FIN-403-FORBIDDEN` itself is in neither i18n bundle. Platform-wide (SEC/MDL/CU/NOTIF/FILE identical); routing it through `LocalizedException` as a FIN code is still a platform decision, recorded as an open ALIGN-BE finding | غير مصرح بهذا الإجراء (غير مُفعَّل — الرد الفعلي هو مغلف `ACCESS_DENIED` المنصّي، وهو الآن مُترجَم في اللغتين) | You are not authorized to perform this action (not wired — the actual response is the platform `ACCESS_DENIED` body, now localized in both languages) |
| FIN-400-INVALID-SORT | PLATFORM-STD | every search API | 400 | unrecognized sort field | حقل الترتيب غير معروف | Unrecognized sort field |
| ~~FIN-503~~ STRUCK | PLATFORM-STD (MDL unreachable) | — | — | unreachable: XM-FIN-001 is in-process `MdlLookupApi` injection, so there is no network hop to fail (and `Status` has no SERVICE_UNAVAILABLE); an invalid code raises `FIN-400-INVALID-LOOKUP`, anything else falls through to `FIN-500` | — | — |
| FIN-500 | PLATFORM-STD (infrastructure) | any | 500 | unhandled server error | حدث خطأ في الخادم | A server error occurred |

Every PLATFORM-STD row follows SEC's ADR-SEC-002 umbrella convention, cited (not re-derived).

## Alignment self-check (ALIGN) — FIN v1

```
RE-RUN AT ALIGN-BE, against the delivered code — every line below states what was actually
verified after implementation, not what was intended before it. The pre-implementation form of
this block read "PASSED ✓ — 0 findings"; that was false and is superseded here.

TRACEABILITY      ✓ re-verified. REQ-FIN-001..046 and DBF-FIN-001..147 all defined upstream; every PHASE/SUB/atom carries traces=. Three id sets grew after this block was first written and are now reflected everywhere: RULE-FIN-017 (srs §A5), QR-FIN-045..049 (ALIGN-BE, for queries already implemented), XM-FIN-002 (ALIGN-BE). No id was renumbered.
BINDING (§2A)     ✓ re-verified. No placeholder; every column cites a DBF; every RULE message present in ar+en. docNo is `JV-{fiscalYearCode}-{NNNNNN}`, counter per fiscalYearId, generated by a FIN-local generator in com.erp.fin (the "platform numbering engine" the pre-implementation text named does not exist and never did).
MANIFEST (§4)     ✓ after correction. All 147 DBF listed, only the mandated columns. Corrected here: the XM count read "12 columns across 8 tables" and is 16 across 10 (V22 carries 16 XM-FIN-001 COMMENT ON COLUMN lines); five rows named a `_pk` column `...Id` (DBF-FIN-098/109/122/130/139) — one of them, DBF-FIN-130, colliding with DBF-FIN-140's real FK of the same name — and now match the entity. RESIDUAL, not corrected: db-script-fin.md's 14 matrix rows still describe `GENERATED ALWAYS AS IDENTITY` PKs where V22 deliberately built SEQUENCE PKs (deviation justified at V22:15-22 and noted in this plan's extraction block); 20 FK rows are modelled as scalar Long where the entity uses @ManyToOne. The third residual is now HALF-CLOSED: AuditableEntity.createdBy/updatedBy declared `length = 255` against the VARCHAR(100) of every FIN audit column and were narrowed to 100 today (read at AuditableEntity.java:26 and :32). The PHYSICAL schema was NOT changed and is still split — measured by grepping `(created_by|updated_by) +VARCHAR` across src/main/resources/db/migration/ and discarding the eight V2 tables that V14__drop_legacy_security_schema.sql drops: 16 LIVE tables at VARCHAR(100) (SEC 5 · MDL 2 · FIN 9) and 6 at VARCHAR(255) (CU_APP_CONFIGURATION V1:25/27; NOTIF_TEMPLATE V6:30/32; NOTIF_CHANNEL_CONFIG V6:41/43; NOTIF_LOG V6:62/64; FILE_CATEGORY V8:26/28; FILE_DOCUMENT V8:47/49), which is exactly the 22 classes `grep -rl "extends AuditableEntity"` returns. Platform-wide, so still not FIN's to settle — written up in governance/project-artifacts/platform-audit-widths-and-error-localization.md.
QRC (§5)          ✓ after correction. Both directions re-run. Forward: all 49 QR ids have a real implementation; no join resolves a lookup label (zero MDL joins in any FIN @Query). Backward: four repository methods had ZERO call sites (contract rule A.2.9) and were DELETED at ALIGN-BE — FiscalPeriodRepository.existsByFiscalPeriodPkAndStatusCode, JournalEntryRepository.existsByJournalEntryPkAndStatusCode, JournalLineRepository.countByJournalEntryPk, RecurringTemplateRepository.findByIsActiveFlAndNextRunDateLessThanEqual; the first two claimed to be the "cheap pre-check" for QR-FIN-040/035, which are in fact decided by FiscalPeriodDomain.assertCanReopen and JournalEntryDomain.assertCanReverse on the loaded entity, with no query at all. Five real, called queries had no QR id and now carry QR-FIN-045..049. Three catalog cross-references corrected (QR-FIN-022 cited QR-FIN-040 for a balance, now QR-FIN-043; QR-FIN-016's table scope now matches its id definition and the code; QR-FIN-042 no longer claims to return a running balance).
API (R3)          ✓ re-measured against the controllers today, NOT carried over. 35 endpoints — counted by listing every `@GetMapping/@PostMapping/@PutMapping/@PatchMapping/@DeleteMapping` in each of the nine files under src/main/java/com/erp/fin/controller/ and summing: Account 4, AllocationRule 3, Dimension 5, EventTypeRule 4, FiscalPeriod 4, FiscalYear 2, JournalEntry 5, RecurringTemplate 3, Report 5 — covering exactly API-FIN-001..035 (the previous revision of this line said "32 endpoints, exactly API-FIN-001..032"; three endpoints were delivered after it was written). Shapes unchanged: every search is POST /<resource>/search, every deactivate is a PUT on the resource's own id path (`/{id}/deactivate`, or `/values/{id}/deactivate` for the dimension-value child), the three period transitions are PATCH, child endpoints sit on the parent's controller, and there is still NO @DeleteMapping and no activate anywhere in FIN (grep returns zero of each). Create/update requests exclude PK/audit/system fields (docNo, statusCode, postedAt); docNo appears only in responses. Every RULE in a Validations line has a catalog row. 37 error codes, measured three ways and diffed pairwise with zero orphans in either direction: 37 `FIN-*` string constants in FinErrorCodes.java, 37 keys matching `^FIN-` in messages.properties, 37 in messages_ar.properties — 37/37/37 (the previous revision said 36/36/36; FIN-404-DIMVALUE was added today at FinErrorCodes.java:188, messages.properties:154, messages_ar.properties:151). The catalog's three remaining rows are still deliberately not constants — FIN-503 is struck, FIN-500 is the infrastructure fallthrough, and FIN-403-FORBIDDEN is the platform ACCESS_DENIED envelope (see ERROR ENVELOPE below).
CROSS-MODULE      ✓ after correction. 2 XM, 2 placed, 0 mismatched, both ACTIVE: XM-FIN-001 (SOFT-READ → MDL_LOOKUP_VALUE) and XM-FIN-002 (READ → SEC's SecUserDirectoryApi), the latter registered at ALIGN-BE — the line previously read "1 XM ... 1 placed", which stopped being true the moment SEC-BE added the RULE-FIN-015 directory read. Boundary itself verified clean: FIN's only non-FIN imports under src/main/java/com/erp/fin are com.erp.mdl.crossmodule.{MdlLookupApi, LookupOptionView} and com.erp.sec.crossmodule.SecUserDirectoryApi; consumption is in-process Spring interface injection, never HTTP; CrossModuleBoundaryArchTest passes. Inbound stub XM-INBOUND-STUB-3 notation unchanged.
SECURITY (R7)     ✓ ORIGINAL FINDING CLOSED, re-measured. 27 PERM_FIN_* constants (counted with `grep -cE 'String PERM_FIN_[A-Z_]+ *=' PermissionConstants.java`) against 27 registered action rows — 26 seeded by V24 (counted by extracting the `INSERT INTO SEC_ACTION_REG ... JOIN SEC_SCREEN_REG` range and counting its `('FIN_...', 'ACTION', ...)` VALUES rows) plus the 27th added by the new V28__fin_dimensions_update_action.sql. Synthesising `'PERM_' || page_code || '_' || action_code` from those 27 rows and diffing the sorted set against the sorted constant list gives an EXACT 27/27 match, no orphan in either direction; each of the 27 was then checked individually and every one is referenced from a service under com.erp.fin. The open half is gone: PERM_FIN_PERIODS_VIEW, previously "the one of 26 rows with no PermissionConstants constant", is now declared at PermissionConstants.java:248 and consumed by FiscalPeriodService.java:176's @PreAuthorize behind API-FIN-033 (POST /api/v1/fin/fiscal-periods/search, FiscalPeriodController.java:68) — the fiscal-period read endpoint whose absence was the whole cause of the finding. No migration was needed for it: the registry row and its V25/V27 grants already existed. PERM_FIN_DIMENSIONS_UPDATE (PermissionConstants.java:97, DimensionValueService.java:100) is the genuinely new one, which is why V28 both registers it AND grants it to SYS_ADMIN explicitly — V25 grants by a SELECT over the registry and has already run, so it can never see a row inserted later. V25/V27 behaviour otherwise unchanged; FIN_CLOSE_APPROVER deliberately gets nothing from V28. Corrected earlier and still standing: the FIN_ACCOUNTS ✓ sits on UPDATE, not DELETE.
CORE (R1)         ✓ re-verified. Layers, domain placement, error signalling (`FIN-{http}[-{SLUG}]`) and type mapping all as declared. 7 Domain classes carry the rule decisions; services delegate.
DECISIONS         ✓ ADR-FIN-001 (carried from P2) cited and still correct as written — see INT-C for why the SecUserDirectoryApi read is nevertheless a formal XM row. DEFAULTs landed since: JOURNAL_TYPE gains CLOSING/OPENING (data-only); docNo format and FIN-local generator ownership; classic reversal (the original stays POSTED and is linked to its reversal; a second reversal is rejected; nothing writes VOID); RULE-FIN-008 exempts API-FIN-027's year-end CLOSING/OPENING entries; MAPPING account derivation is rejected with FIN-422-MAPPING-UNSUPPORTED; a twelve-period fiscal year generates calendar months. No BLOCKED ADR.
ACCOUNTING §12    ✓ all 14 must-honor points still traced and re-verified: (1) RULE-FIN-006 · (2) POL-FIN-002/API-FIN-029 sign presentation · (3) RULE-FIN-007 · (4) RULE-FIN-008, now with its explicit carve-out for API-FIN-027's year-end CLOSING/OPENING entries · (5) CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE + directionCode · (6) RULE-FIN-003/010, plus FIN-422-REMAINDER-MARKER and FIN-422-REMAINDER-NOT-POSITIVE · (7) RULE-FIN-011, now CLASSIC reversal: the original stays POSTED, reversalEntryId/originalEntryId link the pair, a second reversal is rejected with FIN-409-ALREADY-REVERSED, net ledger effect zero, and no path writes VOID · (8) API-FIN-029 (balances by construction) · (9) every report QR reads POSTED lines live, no stored balance column anywhere in db-script-fin.md · (10) REQ-FIN-036/RULE for continuity · (11) RULE-FIN-009 + QR-FIN-044 dimension grouping · (12) RULE-FIN-004 · (13) RULE-FIN-016 lock + no DELETE mapping anywhere (FIN publishes no DELETE endpoint at all) · (14) POL-FIN-014, no host-specific branch anywhere in this plan
ERROR ENVELOPE    ⚠ PARTIALLY CLOSED — 1 residue STILL OPEN, platform-wide. Closed half, read at GlobalExceptionHandler.java:90-96: handleAccessDenied no longer hardcodes anything — it builds `.code(CommonErrorCodes.ACCESS_DENIED)` and `.message(resolveMessage(CommonErrorCodes.ACCESS_DENIED, null))`, the same MessageSource path (:109-115) every LocalizedException already used, and ACCESS_DENIED now exists in BOTH bundles (messages.properties:16, messages_ar.properties:13) plus as a constant at CommonErrorCodes.java:11. The English is byte-identical to the string it replaced — "You do not have permission to perform this operation", confirmed by reading the diff — so only Arabic callers see a change. The previous revision of this line said the handler used "a hardcoded English message, bypassing MessageSource"; that is no longer true and has been removed rather than left standing. OPEN RESIDUE, unchanged and deliberate: the wire `code` is still ACCESS_DENIED and is still NOT FIN-403-FORBIDDEN. Re-verified today — FIN-403-FORBIDDEN appears in no FinErrorCodes constant and in neither i18n bundle, so FIN's 403 remains the platform envelope, not a FIN code. Routing AccessDeniedException through LocalizedException as a FIN code would change every module's 403 body and is still a platform decision nobody has taken.
SCHEMA COMMENT    ⚠ STILL OPEN — re-verified today, unchanged, and expected to stay open. Read at V22__fin_schema.sql:240: `COMMENT ON TABLE FIN_ACCOUNT IS 'ENT-FIN-001 Account — PRIVATE; [DBF-FIN-001..013]';`. Read at db-script-fin.md:447: the same statement, but `[DBF-FIN-001..013, DBF-FIN-147]`. V23 added only the COLUMN comment (V23__fin_account_retained_earnings_flag.sql:25) and V22 is applied and immutable, so the live database's table comment still omits DBF-FIN-147 until someone decides a forward migration is worth writing purely for a comment. Nothing delivered today touches it; do not mark this closed without pointing at the migration that closed it.
SCOPE vs SRS      ✓ ORIGINAL FINDING CLOSED — by a SPLIT decision, half built and half deliberately excluded, both now recorded in srs-fin.md. BUILT: API-FIN-035, `PUT /api/v1/fin/dimensions/values/{id}/deactivate` (DimensionController.java:80 on base path /api/v1/fin/dimensions), gated on the new PERM_FIN_DIMENSIONS_UPDATE (DimensionValueService.java:100), raising the new FIN-404-DIMVALUE (DimensionValueService.java:106), registered and granted by V28; and API-FIN-034, `PUT /api/v1/fin/event-rules/{id}/deactivate` (EventTypeRuleController.java:66), on the PRE-EXISTING PERM_FIN_RULES_UPDATE (EventTypeRuleService.java:100) and the PRE-EXISTING FIN-404-RULE (FinErrorCodes.java:194) — so it needed no migration and no new code. NOT BUILT, deliberately, and now v1 DECISIONS rather than gaps: a deactivate on the PARENT Dimension (srs-fin.md:1154 and the reasoning at :1179 — no REQ, AC or RULE asks for one) and a rule-line DELETE (srs-fin.md:1212 and :1243, and the summary at :1578 — ENT-FIN-010 carries no active-flag column and FIN publishes no DELETE endpoint on any screen). The requirement text was narrowed WITH its reason attached, not silently deleted. A DIFFERENT, newly-surfaced SRS/code divergence is recorded below as SRS OPERATIONS — it is not this finding returning.
DOC QUOTATION     ⚠ NEW OPEN FINDING — a FABRICATED quotation inside an applied, immutable migration. V24__fin_security_seed.sql:52-53 asserts that srs-fin.md's B4 "Actions:" lines for FIN_DIMENSIONS, FIN_RULES, FIN_RECURRING_TEMPLATES and FIN_ALLOCATION_RULES `say "DELETE (deactivate ..., modeled as UPDATE)"`. Measured: `grep -c 'DELETE (deactivate' governance/modules/FIN/P1/srs-fin.md` returns 0 and `grep -c 'modeled as UPDATE' governance/modules/FIN/P1/srs-fin.md` returns 0 — that string occurs nowhere in srs-fin.md, in any spelling. What those four lines ACTUALLY read, opened at srs-fin.md:1166, :1227, :1270 and :1296, is `Actions: VIEW, CREATE, UPDATE` (two of them with a parenthetical about "run"), and none of the four mentions DELETE at all. The phrase's real home is MDL: `(deactivate only, modeled as UPDATE — no hard-delete endpoint exists)` at modules/MDL/packages/backend-execution/SEC-BE/SEC-BE.md:8 and modules/MDL/P3_1/backend-execution-plan-mdl.md:509 — so it was copied across modules, not read out of FIN's SRS. V24 is applied and MUST NOT be edited, so this cannot be corrected at source; it is recorded here and in execution-state.json precisely so no future session re-quotes it as srs text. Note the CONCLUSION V24 draws (seed no PERM_FIN_*_DELETE rows) is independently correct and is NOT in dispute — only the evidence it cites for it.
ERROR CAT COVER   ⚠ NEW OPEN FINDING. Two Error Catalog rows under-list the APIs that raise their code, measured by grepping `FinErrorCodes.FIN_404_YEAR` and `FinErrorCodes.FIN_404_PERIOD` across src/main/java/com/erp/fin/ and reading every hit. FIN-404-YEAR's API column names API-FIN-027, 030, 031; it is ALSO thrown at JournalEntryService.java:171 (createManual — API-FIN-019), JournalPostingService.java:152 (the posting path behind API-FIN-019/020/021) and FiscalYearService.java:364 (successorOf) and :371 (findOrThrow — API-FIN-023/027). FIN-404-PERIOD's API column names API-FIN-024, 025, 026, 029, 031; it is ALSO thrown at RecurringTemplateService.java:284 (template run — API-FIN-014), JournalEntryService.java:174 (API-FIN-019), JournalPostingService.java:208 and FiscalYearService.java:314 and :322 (year-end close — API-FIN-027). The two catalog rows sit OUTSIDE this self-check block, in territory three earlier sessions already aligned, so this session deliberately did not edit them — it records the divergence instead.
SRS OPERATIONS    ⚠ NEW OPEN FINDING, and distinct from the CLOSED SCOPE vs SRS one above. Several B1 "Operations" lines still name operations no controller publishes — verified by listing every `@*Mapping` in src/main/java/com/erp/fin/controller/ and comparing. srs-fin.md:1128 (SCR-REQ-FIN-001, Chart of accounts) lists `read`: AccountController publishes create, update, deactivate and search only — no `GET /{id}` — and the screen's own B5 table (srs-fin.md:1145-1148) has no read row either, so the B1 line is the sole claim. srs-fin.md:1258 (SCR-REQ-FIN-004) lists `read, update, deactivate` for the template and `create, read, update, delete` for the line: RecurringTemplateController publishes exactly POST create (API-FIN-013), POST /{id}/run (API-FIN-014) and POST /search (API-FIN-012). srs-fin.md:1283 (SCR-REQ-FIN-005) lists the same set for rule and target: AllocationRuleController publishes exactly POST create (API-FIN-016), POST /{id}/run (API-FIN-017) and POST /search (API-FIN-015). A probable fourth, same shape, flagged rather than asserted: srs-fin.md:1339 (SCR-REQ-FIN-007) lists `create (year), search, read` — FiscalYearController publishes only POST create and POST /{id}/year-end-close, and the search that now exists (API-FIN-033) is over PERIODS, not years. SCR-REQ-FIN-006 is NOT affected — JournalEntryController does publish `GET /{id}` (API-FIN-022) — and neither is SCR-REQ-FIN-008, whose `read` is the account-ledger report. srs-fin.md is OUTSIDE this session's scope and was not edited; recorded so a human can either narrow the requirements or schedule the endpoints.
PLATFORM I18N     ⚠ NEW OPEN FINDING, platform-wide, and one half of it needs a HUMAN to pick wording. Read at GlobalExceptionHandler.java:54-105: three handlers still build hardcoded English. INTERNAL_ERROR (:103-104) hardcodes `"An unexpected error occurred"`, yet BOTH bundles already carry an INTERNAL_ERROR entry that nothing resolves — messages.properties:15 `An unexpected error occurred. Please try again later.` and messages_ar.properties:12 — and the two English strings DIFFER, so simply routing it through resolveMessage would silently change the wire text for every existing English caller. That wording conflict is a human call, not a mechanical fix. VALIDATION_ERROR (:63 `"Validation failed"` and :74 `"The request body is malformed or does not match the expected structure"`) and DATA_INTEGRITY_VIOLATION (:84) have NO bundle entry in either language — `grep '^VALIDATION_ERROR='` and `grep '^DATA_INTEGRITY_VIOLATION='` return nothing in both files — so localizing those needs new keys minted first. Only LocalizedException (:32-46) and, since today, AccessDeniedException (:90-96) resolve through MessageSource. Written up in governance/project-artifacts/platform-audit-widths-and-error-localization.md.
RESULT            PASSED WITH FINDINGS — 6 open, 0 blocking, 0 unverified lines. Of the four findings the previous revision carried: SECURITY is CLOSED (PERM_FIN_PERIODS_VIEW now has both a constant and the endpoint that justifies it — API-FIN-033); SCOPE vs SRS is CLOSED (split decision — API-FIN-034/035 built, parent-Dimension deactivate and rule-line delete recorded in srs-fin.md as reasoned v1 exclusions); ERROR ENVELOPE is PARTIALLY CLOSED with its residue named precisely (the 403 message is localized through MessageSource, but the wire code is still the platform ACCESS_DENIED and FIN-403-FORBIDDEN still never reaches the wire); SCHEMA COMMENT is UNTOUCHED and still OPEN (V22 is applied and immutable). Four findings were ADDED by this session, each verified against the real artifact before being written: DOC QUOTATION, ERROR CAT COVER, SRS OPERATIONS, PLATFORM I18N. The six open are therefore ERROR ENVELOPE (residue), SCHEMA COMMENT, DOC QUOTATION, ERROR CAT COVER, SRS OPERATIONS, PLATFORM I18N. Every count on every line above was re-measured from the real artifact by this session and each line states how it was measured; nothing was carried forward from the previous revision unre-counted. This block must NOT be returned to "PASSED ✓ — 0 findings", and its open count must not be lowered, while any finding above still stands — closing one means naming, on its own line, the artifact that closed it.
```

**Coverage — ENT/DBF → phases → QR → XM**: every ENT-FIN-001..014 appears in exactly one
DATA-DOM entity block with ≥1 QR cited under REPOSITORY OPS; every DBF-FIN-001..147
appears in the DB Alignment Manifest and its owning entity's block; XM-FIN-001 appears in
INT-C, INT-R and every lookup-backed field's manifest row; XM-FIN-002 appears in INT-C and
INT-R and binds no manifest row (no column carries it).

**Coverage — RULE → API → catalog code**: RULE-FIN-001→API-FIN-002/003→
FIN-409-PARENT-NOT-LEAF-ELIGIBLE/FIN-409-HAS-CHILDREN · RULE-FIN-002→API-FIN-007→
FIN-409-DIMVALUE-DUP · RULE-FIN-003→API-FIN-011/016→FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER ·
RULE-FIN-004→API-FIN-020→FIN-409-DUPLICATE-EVENT · RULE-FIN-005→API-FIN-020→
FIN-404-NO-ACTIVE-RULE · RULE-FIN-006→API-FIN-019/020/014/017→FIN-409-UNBALANCED ·
RULE-FIN-007→(same APIs)→FIN-409-NOT-POSTABLE-ACCOUNT · RULE-FIN-008→(same)→
FIN-409-PERIOD-NOT-OPEN · RULE-FIN-009→(same)→FIN-409-INVALID-DIMENSION ·
RULE-FIN-010→API-FIN-020/014/017→FIN-422-REMAINDER-NOT-POSITIVE, FIN-422-REMAINDER-MARKER (the per-side computation itself is success-path; these two reject a distribution that leaves no positive residue, and a line whose remainder marker is ambiguous) ·
RULE-FIN-011→API-FIN-021→(no distinct code — success-path build) ·
RULE-FIN-012→API-FIN-021→(no distinct code — success-path period substitution) ·
RULE-FIN-013→API-FIN-021/014→FIN-409-NOT-POSTED, FIN-409-ALREADY-REVERSED ·
RULE-FIN-014→API-FIN-024/026→
FIN-409-NOT-REOPENABLE · RULE-FIN-015→API-FIN-026/027→FIN-403-SOD-VIOLATION ·
RULE-FIN-016→(every posted-entry endpoint)→(enforced by omission, no code needed — no
UPDATE/DELETE mapping exists on a POSTED row) ·
RULE-FIN-017→API-FIN-019→FIN-400-PERIOD-NOT-IN-YEAR, FIN-400-DOCDATE-OUTSIDE-PERIOD.

**Coverage — XM → status → blocks → workaround**: XM-FIN-001 → ACTIVE → blocks none → no
workaround needed. XM-FIN-002 → ACTIVE → blocks none → fallback is not "continue without SEC":
a failed directory read raises `FIN-403-SOD-VIOLATION`, so the close is refused rather than
approved on an unverified fact.

## QR id definitions (cross-reference index — full detail in Query Reference Catalog above)
**QR-FIN-001** — FIND_BY_CRITERIA search accounts [ENT-FIN-001, API-FIN-001]
**QR-FIN-002** — SAVE create account [ENT-FIN-001, API-FIN-002]
**QR-FIN-003** — UPDATE update account [ENT-FIN-001, API-FIN-003]
**QR-FIN-004** — UPDATE deactivate account [ENT-FIN-001, API-FIN-004]
**QR-FIN-005** — EXISTS account code unique [ENT-FIN-001, API-FIN-002]
**QR-FIN-006** — EXISTS account has no children (RULE-FIN-001) [ENT-FIN-001, API-FIN-002, API-FIN-003]
**QR-FIN-007** — FIND_BY_CRITERIA search dimensions [ENT-FIN-002, API-FIN-005]
**QR-FIN-008** — SAVE create dimension [ENT-FIN-002, API-FIN-006]
**QR-FIN-009** — SAVE create dimension value [ENT-FIN-003, API-FIN-007]
**QR-FIN-010** — EXISTS dimension value code unique within dimension (RULE-FIN-002) [ENT-FIN-003, API-FIN-007]
**QR-FIN-011** — FIND_BY_CRITERIA search dimension values [ENT-FIN-003, API-FIN-008]
**QR-FIN-012** — FIND_BY_CRITERIA search event-type rules [ENT-FIN-009, API-FIN-009]
**QR-FIN-013** — SAVE create event-type rule [ENT-FIN-009, API-FIN-010]
**QR-FIN-014** — EXISTS one active rule per event type [ENT-FIN-009, API-FIN-010]
**QR-FIN-015** — SAVE add rule line [ENT-FIN-010, API-FIN-011]
**QR-FIN-016** — EXISTS remainder-line/target count (RULE-FIN-003) [ENT-FIN-010, ENT-FIN-014, API-FIN-011, API-FIN-016]
**QR-FIN-017** — FIND_BY_CRITERIA search templates [ENT-FIN-011, API-FIN-012]
**QR-FIN-018** — SAVE create template with lines [ENT-FIN-011, ENT-FIN-012, API-FIN-013]
**QR-FIN-019** — FIND_ONE load the template to run, by id [ENT-FIN-011, API-FIN-014]
**QR-FIN-020** — FIND_BY_CRITERIA search allocation rules [ENT-FIN-013, API-FIN-015]
**QR-FIN-021** — SAVE create allocation rule with targets [ENT-FIN-013, ENT-FIN-014, API-FIN-016]
**QR-FIN-022** — FIND_ONE load allocation rule + source balance [ENT-FIN-013, API-FIN-017]
**QR-FIN-023** — FIND_BY_CRITERIA search journal entries [ENT-FIN-004, API-FIN-018]
**QR-FIN-024** — SAVE build manual entry (DRAFT) [ENT-FIN-004, ENT-FIN-005, ENT-FIN-006, API-FIN-019]
**QR-FIN-025** — SAVE build event entry (DRAFT) [ENT-FIN-004, ENT-FIN-005, ENT-FIN-006, API-FIN-020]
**QR-FIN-026** — EXISTS duplicate eventReference (RULE-FIN-004) [ENT-FIN-004, API-FIN-020]
**QR-FIN-027** — EXISTS active rule for event type (RULE-FIN-005) [ENT-FIN-009, API-FIN-020]
**QR-FIN-028** — AGGREGATE compute remainder-line amount (RULE-FIN-010) [ENT-FIN-005, API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017]
**QR-FIN-029** — EXISTS debits=credits (RULE-FIN-006) [ENT-FIN-005, API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017]
**QR-FIN-030** — EXISTS every line account leaf+active (RULE-FIN-007) [ENT-FIN-001, API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017]
**QR-FIN-031** — EXISTS period open at post (RULE-FIN-008) [ENT-FIN-008, API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017]
**QR-FIN-032** — EXISTS every dimension value valid+active (RULE-FIN-009) [ENT-FIN-006, API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017]
**QR-FIN-033** — UPDATE post entry DRAFT→POSTED [ENT-FIN-004, API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017]
**QR-FIN-034** — SAVE build reversal entry (RULE-FIN-011) [ENT-FIN-004, ENT-FIN-005, ENT-FIN-006, API-FIN-021]
**QR-FIN-035** — EXISTS original entry is POSTED and not already reversed (RULE-FIN-013) [ENT-FIN-004, API-FIN-021, API-FIN-014]
**QR-FIN-036** — FIND_ONE current open period for reversal (RULE-FIN-012) [ENT-FIN-008, API-FIN-021]
**QR-FIN-037** — FIND_ONE read one entry with lines [ENT-FIN-004, API-FIN-022]
**QR-FIN-038** — SAVE create fiscal year + periods [ENT-FIN-007, ENT-FIN-008, API-FIN-023]
**QR-FIN-039** — UPDATE open/soft-close/hard-close period [ENT-FIN-008, API-FIN-024, API-FIN-025, API-FIN-026]
**QR-FIN-040** — EXISTS period not already Hard Closed (RULE-FIN-014) [ENT-FIN-008, API-FIN-024, API-FIN-026]
**QR-FIN-041** — AGGREGATE year-end closing/opening balances [ENT-FIN-004, ENT-FIN-005, API-FIN-027]
**QR-FIN-042** — FIND_BY_CRITERIA account ledger POSTED lines; running balance accumulated in the service [ENT-FIN-005, API-FIN-028]
**QR-FIN-043** — AGGREGATE trial balance / balance sheet / income statement account balances [ENT-FIN-005, API-FIN-029, API-FIN-030, API-FIN-031]
**QR-FIN-044** — AGGREGATE dimension report [ENT-FIN-005, ENT-FIN-006, API-FIN-032]
**QR-FIN-045** — FIND_ONE the Retained Earnings account (DBF-FIN-147) [ENT-FIN-001, API-FIN-027]
**QR-FIN-046** — EXISTS dimension code unique [ENT-FIN-002, API-FIN-006]
**QR-FIN-047** — EXISTS fiscal year code unique [ENT-FIN-007, API-FIN-023]
**QR-FIN-048** — FIND_ONE next fiscal year by startDate [ENT-FIN-007, API-FIN-027]
**QR-FIN-049** — FIND_BY_CRITERIA every period of one fiscal year [ENT-FIN-008, API-FIN-023, API-FIN-027, API-FIN-014]

## XM id definitions
**XM-FIN-001** — SOFT-READ every FIN lookup-backed column → MDL_LOOKUP_VALUE [REQ-FIN-001, REQ-FIN-007, REQ-FIN-008, REQ-FIN-010, REQ-FIN-014, REQ-FIN-018, REQ-FIN-022, REQ-FIN-025, REQ-FIN-031]
**XM-FIN-002** — READ SEC's user→permission directory (`SecUserDirectoryApi.findUserIdsHoldingPermission`) for the RULE-FIN-015 separation-of-duties fact [REQ-FIN-037, REQ-FIN-038]

## Registry content
See `registry-exec-be-fin.md`.
══════════════════════════════════════════════════════════════════
