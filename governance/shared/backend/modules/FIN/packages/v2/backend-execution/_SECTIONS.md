<!-- source: content outside every PHASE block (leading / between / trailing sections) -->
# BACKEND EXECUTION PLAN — الحسابات العامة / Finance (General Ledger) (FIN) — DELTA v2
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v2 (delta on v1 — change set CS-FIN-001, ADDITIVE)   Profile : erp   Dialect : postgresql16
Framework : spring-boot-java (profile.stack.backend.framework)
Inputs : srs, db-script, registry-srs, registry-db (all v2 current state, `_state/`); change-manifest CS-FIN-001
Open ADRs : 6 raised by this stage, all ACCEPTED (non-breaking) — decisions/FIN/ADR-FIN-029.md,
            decisions/FIN/ADR-FIN-030.md, decisions/FIN/ADR-FIN-031.md,
            decisions/FIN/ADR-FIN-040.md, decisions/FIN/ADR-FIN-041.md, decisions/FIN/ADR-FIN-042.md. Carried: ADR-FIN-001. Applied from
            upstream v2: ADR-FIN-010, ADR-FIN-011, ADR-FIN-012, ADR-FIN-013, ADR-FIN-014, ADR-FIN-021,
            ADR-FIN-023, ADR-FIN-024, ADR-FIN-025, ADR-FIN-026, ADR-FIN-027, ADR-FIN-028. BLOCKED: none.
Delta  : ADDED — four account-mapping endpoints (API-FIN-038 … API-FIN-041), twelve queries (QR-FIN-050 …
         QR-FIN-061), the entity blocks of ENT-FIN-015 and ENT-FIN-016.
         MODIFIED — API-FIN-011 and API-FIN-020 in substance (rule-line tags; mapped accounts, event dimensions,
         distinct rejections); XM-FIN-001 (scope); QR-FIN-015,
         QR-FIN-025, QR-FIN-026; the Concurrency line of API-FIN-014, API-FIN-024, API-FIN-025, API-FIN-026 and
         API-FIN-027, each now naming a PESSIMISTIC_WRITE guard (ADR-FIN-041, pass-1); every other v1 endpoint
         and entity block in annotation only (ADR-FIN-029 — no behaviour change).
         REMOVED — none. `FIN-422-MAPPING-UNSUPPORTED` is struck in the Error Catalog; a code is not an id.
══════════════════════════════════════════════════════════════════

Delta reading note. This file is the complete current plan, not a fragment. `gov.py state` folds a delta plan
by carrying only a phase's DIRECT children, so a SUB re-emitted with its v2 atoms alone would lose every v1 atom
inside it, and nothing outside a marker is carried at all (index, manifest, catalog, self-check). Every phase is
therefore re-emitted in full. Records restated unchanged (the v1 query definitions, XM-FIN-002) are byte-identical
to v1; every record that differs is listed MODIFIED in the version's change manifest. ADR-FIN-029 records why the
v1 blocks were brought up to the current P3.1 template in the same pass: the contracts that check that template
were added after v1 passed its gate, and a v2 gate reads the whole folded plan.

## PRE-GENERATION EXTRACTION — FIN v2 (working set; not part of the plan proper)

```
── FROM srs ──────────────────────────────────────────────────────────────
ENTITIES      16 — ENT-FIN-001..016. v2 ADDED ENT-FIN-015 AccountMapping and ENT-FIN-016
              RuleLineDimension (both config); MODIFIED ENT-FIN-010 RuleLine (DIRECT gone,
              accountBusinessFieldCode added, dimension tags hang off the line)
REQUIREMENTS  77 — REQ-FIN-001..077 (v2 ADDED 047..077; MODIFIED 045 — 15 lookup types)
RULES         28 — RULE-FIN-001..028 (v2 ADDED 018..028; MODIFIED 004 — both detections answer
              the duplicate error). RULE-FIN-015 keeps its DEFERRED data source
SCREENS       13 — SCR-REQ-FIN-001..013 (ADDED 013 Account mappings; MODIFIED 003 and 006)
PERMISSIONS   13 page codes, pattern PERM_<PAGE_CODE>_<ACTION>, gateway VIEW. v2 adds page
              FIN_ACCOUNT_MAPPINGS with VIEW / CREATE / UPDATE (deactivate under UPDATE, no DELETE)
LOOKUPS       15 FIN-owned keys, runtime-loaded from MDL. v2 ADDED FIN_EVENT_BUSINESS_FIELD
              (PAYMENT_METHOD, EXPENSE_TYPE_CODE, ORGANISATION_CODE, BRANCH_CODE) and
              FIN_DIMENSION_VALUE_SOURCE (CONSTANT, BUSINESS_FIELD); MODIFIED ACCOUNT_DERIVATION_TYPE
              (CONSTANT, MAPPING — DIRECT soft-retired) and PAYMENT_METHOD (CHEQUE seeded)
BUSINESS CODE JournalEntry.docNo — `JV-{fiscalYearCode}-{NNNNNN}`, FIN-local generator, unchanged.
              AccountMapping and RuleLineDimension carry no business code (SRS A3)
── FROM db-script ────────────────────────────────────────────────────────
TABLES        16 — v2 ADDED FIN_ACCOUNT_MAPPING and FIN_RULE_LINE_DIM; FIN_RULE_LINE altered
PK GENERATION strategy `sequence`: one SEQ_{TABLE} per table, the PK a plain BIGINT filled by the
              application. The 14 v1 sequences exist from migration V22; v2 BLOCK 1 creates
              SEQ_FIN_ACCOUNT_MAPPING and SEQ_FIN_RULE_LINE_DIM
COLUMNS       165 DBF (v2 ADDED 148..165; MODIFIED 041 traces, 101 value set, 102 nullability)
CONSTRAINTS   v2: PK_FIN_ACCOUNT_MAPPING, PK_FIN_RULE_LINE_DIM, UQ_FIN_RULE_LINE_DIM_LINE_DIM,
              CHK_FIN_RULE_LINE_DERIVATION_SPEC, CHK_FIN_RULE_LINE_DIM_VALUE_SPEC,
              FK_ACCOUNT_MAPPING_ACCOUNT, FK_RULE_LINE_DIM_LINE, FK_RULE_LINE_DIM_DIMENSION,
              FK_RULE_LINE_DIM_VALUE. Indexes: UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY (partial unique,
              WHERE is_active_fl), IDX_FIN_ACCOUNT_MAPPING_EVT_FLD_VAL, IDX_FIN_ACCOUNT_MAPPING_FIELD,
              IDX_FIN_ACCOUNT_MAPPING_ACCOUNT, IDX_FIN_RULE_LINE_DIM_DIMENSION, IDX_FIN_RULE_LINE_DIM_VALUE.
              v1's UQ_FIN_JOURNAL_ENTRY_EVENT_REF is unchanged; only the answer to its refusal changes
XM            XM-FIN-001 SOFT-READ → MDL, ACTIVE, MODIFIED (6 more columns, 15 keys);
              XM-FIN-002 RETIRED 2026-09-12 (id burned)
── FROM registries ───────────────────────────────────────────────────────
SHARED ENTITIES CONSUMED   LookupType / LookupValue (MDL — ENT-MDL-001, ENT-MDL-002) via XM-FIN-001.
                           SEC identity and FIN's self-registration into SEC stay platform
                           integration, not an XM row (ADR-FIN-001)
EXISTING LOOKUP KEYS       the 13 v1 keys are reused; the 2 new keys are FIN-owned, no duplicate
ID RANGES already used     API-FIN-001..037 · QR-FIN-001..049 — v2 continues at API-FIN-038 and QR-FIN-050
──────────────────────────────────────────────────────────────────────────
No row required §2A.3 extraction-failure handling.
```

## EXECUTION PLAN INDEX — FIN v2 — backend-execution-plan-fin.md
Profile: erp · dialect: postgresql16 · framework: spring-boot-java
Open ADRs: 6 new (ADR-FIN-029, ADR-FIN-030, ADR-FIN-031, ADR-FIN-040, ADR-FIN-041, ADR-FIN-042), none BLOCKED — decisions/FIN/

**ENTITY REGISTRY**
| ENT | Name | Table | Business code | Operations (security actions — the endpoints are in each entity block) |
|---|---|---|---|---|
| ENT-FIN-001 | Account | FIN_ACCOUNT | none | VIEW · CREATE · UPDATE (update, deactivate) |
| ENT-FIN-002 | Dimension | FIN_DIMENSION | none | VIEW · CREATE |
| ENT-FIN-003 | DimensionValue | FIN_DIMENSION_VALUE | none | VIEW · CREATE · UPDATE (deactivate) |
| ENT-FIN-004 | JournalEntry | FIN_JOURNAL_ENTRY | **docNo** (`JV-{fiscalYearCode}-{NNNNNN}`, FIN-local generator) | VIEW · CREATE (manual, event, recurring, allocation, reversal, year-end) |
| ENT-FIN-005 | JournalLine | FIN_JOURNAL_LINE | none | VIEW · CREATE (with its header) |
| ENT-FIN-006 | JournalLineDimension | FIN_JOURNAL_LINE_DIM | none | VIEW · CREATE (with its line) |
| ENT-FIN-007 | FiscalYear | FIN_FISCAL_YEAR | none | CREATE · UPDATE (year-end close) |
| ENT-FIN-008 | FiscalPeriod | FIN_FISCAL_PERIOD | none | VIEW · CREATE (with its year) · UPDATE (open, soft-close, hard-close) |
| ENT-FIN-009 | EventTypeRule | FIN_EVENT_TYPE_RULE | none | VIEW · CREATE · UPDATE (deactivate) |
| ENT-FIN-010 | RuleLine | FIN_RULE_LINE | none | CREATE (with its dimension tags) |
| ENT-FIN-011 | RecurringTemplate | FIN_RECURRING_TEMPLATE | none | VIEW · CREATE · UPDATE (run, deactivate) |
| ENT-FIN-012 | RecurringTemplateLine | FIN_RECURRING_TEMPLATE_LINE | none | VIEW · CREATE (with its template) |
| ENT-FIN-013 | AllocationRule | FIN_ALLOCATION_RULE | none | VIEW · CREATE · UPDATE (run, deactivate) |
| ENT-FIN-014 | AllocationTarget | FIN_ALLOCATION_TARGET | none | VIEW · CREATE (with its rule) |
| ENT-FIN-015 | AccountMapping (v2) | FIN_ACCOUNT_MAPPING | none | VIEW · CREATE · UPDATE (change account, deactivate) |
| ENT-FIN-016 | RuleLineDimension (v2) | FIN_RULE_LINE_DIM | none | CREATE (with its rule line) |

**FIELD REGISTRY** — the DB Alignment Manifest below is the single binding (one row per DBF, property,
language type, XM). Read-only in every request: each PK, each audit column, docNo, postedAt, closedBy/closedAt,
and every `createdAt`-only child row. The v2 key columns of an account mapping (eventTypeCode, businessFieldCode,
businessValue) are writable on create and read-only afterwards (ADR-FIN-024).

**API REGISTRY**
| API | Operation | Verb | Path | ENT | Traces (REQ) |
|---|---|---|---|---|---|
| API-FIN-001 | search accounts | POST | /api/v1/fin/accounts/search | ENT-FIN-001 | REQ-FIN-001 |
| API-FIN-002 | create account | POST | /api/v1/fin/accounts | ENT-FIN-001 | REQ-FIN-001, REQ-FIN-002 |
| API-FIN-003 | update account | PUT | /api/v1/fin/accounts/{id} | ENT-FIN-001 | REQ-FIN-002 |
| API-FIN-004 | deactivate account | PUT | /api/v1/fin/accounts/{id}/deactivate | ENT-FIN-001 | REQ-FIN-003 |
| API-FIN-005 | search dimensions | POST | /api/v1/fin/dimensions/search | ENT-FIN-002 | REQ-FIN-004 |
| API-FIN-006 | create dimension | POST | /api/v1/fin/dimensions | ENT-FIN-002 | REQ-FIN-004 |
| API-FIN-007 | create dimension value | POST | /api/v1/fin/dimensions/{id}/values | ENT-FIN-003 | REQ-FIN-005, REQ-FIN-006 |
| API-FIN-008 | search dimension values | POST | /api/v1/fin/dimensions/values/search | ENT-FIN-003 | REQ-FIN-005 |
| API-FIN-009 | search event-type rules | POST | /api/v1/fin/event-rules/search | ENT-FIN-009 | REQ-FIN-007 |
| API-FIN-010 | create event-type rule | POST | /api/v1/fin/event-rules | ENT-FIN-009 | REQ-FIN-007 |
| API-FIN-011 | add rule line (v2: with its dimension tags) | POST | /api/v1/fin/event-rules/{id}/lines | ENT-FIN-010, ENT-FIN-016 | REQ-FIN-008, REQ-FIN-009, REQ-FIN-048, REQ-FIN-049, REQ-FIN-062, REQ-FIN-064, REQ-FIN-065 |
| API-FIN-012 | search templates | POST | /api/v1/fin/recurring-templates/search | ENT-FIN-011 | REQ-FIN-022 |
| API-FIN-013 | create template | POST | /api/v1/fin/recurring-templates | ENT-FIN-011, ENT-FIN-012 | REQ-FIN-022 |
| API-FIN-014 | run template | POST | /api/v1/fin/recurring-templates/{id}/run | ENT-FIN-011 | REQ-FIN-023, REQ-FIN-024 |
| API-FIN-015 | search allocation rules | POST | /api/v1/fin/allocation-rules/search | ENT-FIN-013 | REQ-FIN-025 |
| API-FIN-016 | create allocation rule | POST | /api/v1/fin/allocation-rules | ENT-FIN-013, ENT-FIN-014 | REQ-FIN-025 |
| API-FIN-017 | run allocation rule | POST | /api/v1/fin/allocation-rules/{id}/run | ENT-FIN-013 | REQ-FIN-026 |
| API-FIN-018 | search journal entries | POST | /api/v1/fin/journal-entries/search | ENT-FIN-004 | REQ-FIN-027 |
| API-FIN-019 | create manual entry | POST | /api/v1/fin/journal-entries | ENT-FIN-004 | REQ-FIN-014, REQ-FIN-015, REQ-FIN-017..021 |
| API-FIN-020 | build event entry (system; v2: mapped accounts, event dimensions, distinct rejections) | POST | /api/v1/fin/journal-entries/from-event | ENT-FIN-004 | REQ-FIN-010..013, REQ-FIN-017..021, REQ-FIN-047, REQ-FIN-050..053, REQ-FIN-063, REQ-FIN-066..070, REQ-FIN-072..076 |
| API-FIN-021 | reverse entry | POST | /api/v1/fin/journal-entries/{id}/reverse | ENT-FIN-004 | REQ-FIN-028, REQ-FIN-029, REQ-FIN-030, REQ-FIN-077 |
| API-FIN-022 | read entry | GET | /api/v1/fin/journal-entries/{id} | ENT-FIN-004 | REQ-FIN-016, REQ-FIN-027, REQ-FIN-070 |
| API-FIN-023 | create fiscal year | POST | /api/v1/fin/fiscal-years | ENT-FIN-007 | REQ-FIN-031 |
| API-FIN-024 | open period | PATCH | /api/v1/fin/fiscal-periods/{id}/open | ENT-FIN-008 | REQ-FIN-032 |
| API-FIN-025 | soft-close period | PATCH | /api/v1/fin/fiscal-periods/{id}/soft-close | ENT-FIN-008 | REQ-FIN-033 |
| API-FIN-026 | hard-close period (approval) | PATCH | /api/v1/fin/fiscal-periods/{id}/hard-close | ENT-FIN-008 | REQ-FIN-034, REQ-FIN-035, REQ-FIN-037, REQ-FIN-038 |
| API-FIN-027 | run year-end close | POST | /api/v1/fin/fiscal-years/{id}/year-end-close | ENT-FIN-007 | REQ-FIN-036 |
| API-FIN-028 | account ledger | GET | /api/v1/fin/reports/account-ledger | ENT-FIN-005 | REQ-FIN-039, REQ-FIN-046, REQ-FIN-071 |
| API-FIN-029 | trial balance | GET | /api/v1/fin/reports/trial-balance | ENT-FIN-005 | REQ-FIN-040, REQ-FIN-046 |
| API-FIN-030 | balance sheet | GET | /api/v1/fin/reports/balance-sheet | ENT-FIN-005 | REQ-FIN-041, REQ-FIN-046 |
| API-FIN-031 | income statement | GET | /api/v1/fin/reports/income-statement | ENT-FIN-005 | REQ-FIN-042, REQ-FIN-046 |
| API-FIN-032 | dimension report | GET | /api/v1/fin/reports/dimension | ENT-FIN-005 | REQ-FIN-043, REQ-FIN-071 |
| API-FIN-033 | search fiscal periods | POST | /api/v1/fin/fiscal-periods/search | ENT-FIN-008 | REQ-FIN-031 |
| API-FIN-034 | deactivate event-type rule | PUT | /api/v1/fin/event-rules/{id}/deactivate | ENT-FIN-009 | REQ-FIN-007 |
| API-FIN-035 | deactivate dimension value | PUT | /api/v1/fin/dimensions/values/{id}/deactivate | ENT-FIN-003 | REQ-FIN-005, REQ-FIN-021 |
| API-FIN-036 | deactivate template | PUT | /api/v1/fin/recurring-templates/{id}/deactivate | ENT-FIN-011 | REQ-FIN-022 |
| API-FIN-037 | deactivate allocation rule | PUT | /api/v1/fin/allocation-rules/{id}/deactivate | ENT-FIN-013 | REQ-FIN-025 |
| API-FIN-038 | search account mappings (v2) | POST | /api/v1/fin/account-mappings/search | ENT-FIN-015 | REQ-FIN-055 |
| API-FIN-039 | create account mapping (v2) | POST | /api/v1/fin/account-mappings | ENT-FIN-015 | REQ-FIN-054, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061 |
| API-FIN-040 | update account mapping — the account only (v2) | PUT | /api/v1/fin/account-mappings/{id} | ENT-FIN-015 | REQ-FIN-056, REQ-FIN-058, REQ-FIN-060 |
| API-FIN-041 | deactivate account mapping (v2) | PUT | /api/v1/fin/account-mappings/{id}/deactivate | ENT-FIN-015 | REQ-FIN-057, REQ-FIN-058 |

**RULE REGISTRY** — scope, where each rule is enforced and the catalog row that carries its message. The ar/en
text of every v2 rule is inline in the endpoint that enforces it; every message is in the Error Catalog.
| RULE | Name | Scope | ar/en message | Enforced by | Catalog code |
|---|---|---|---|---|---|
| RULE-FIN-001 | no direct posting on a parent account | ENT-FIN-001 | ✓ / ✓ | API-FIN-002, API-FIN-003 | FIN-409-PARENT-NOT-LEAF-ELIGIBLE, FIN-409-HAS-CHILDREN |
| RULE-FIN-002 | unique code within a dimension | ENT-FIN-003 | ✓ / ✓ | API-FIN-007 | FIN-409-DIMVALUE-DUP |
| RULE-FIN-003 | exactly one remainder line / target | ENT-FIN-010, ENT-FIN-014 | ✓ / ✓ | API-FIN-011, API-FIN-016, API-FIN-017, API-FIN-020 | FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER |
| RULE-FIN-004 | duplicate event reference (MODIFIED v2 — both detections) | ENT-FIN-004 | ✓ / ✓ | API-FIN-020 | FIN-409-DUPLICATE-EVENT |
| RULE-FIN-005 | no active rule for the event type | ENT-FIN-009 | ✓ / ✓ | API-FIN-020 | FIN-404-NO-ACTIVE-RULE |
| RULE-FIN-006 | debits equal credits | ENT-FIN-004, ENT-FIN-005 | ✓ / ✓ | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 | FIN-409-UNBALANCED |
| RULE-FIN-007 | post only to leaf, active accounts (v2: also the mapped one) | ENT-FIN-001, ENT-FIN-005 | ✓ / ✓ | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 | FIN-409-NOT-POSTABLE-ACCOUNT |
| RULE-FIN-008 | period gate at post time | ENT-FIN-004, ENT-FIN-008 | ✓ / ✓ | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021 | FIN-409-PERIOD-NOT-OPEN |
| RULE-FIN-009 | dimension value validity (v2: also event-derived values) | ENT-FIN-006 | ✓ / ✓ | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 | FIN-409-INVALID-DIMENSION |
| RULE-FIN-010 | the remainder line absorbs the rounding difference | ENT-FIN-005, ENT-FIN-010, ENT-FIN-014 | ✓ / ✓ | API-FIN-017, API-FIN-020 | FIN-422-REMAINDER-NOT-POSITIVE |
| RULE-FIN-011 | reversal is exact and linked | ENT-FIN-004, ENT-FIN-005 | ✓ / ✓ (informational — raises no rejection) | API-FIN-021 | — |
| RULE-FIN-012 | reversal posts to the current period when the original's is closed | ENT-FIN-004, ENT-FIN-008 | ✓ / ✓ (informational) | API-FIN-021 | — (no open period at all answers FIN-409-PERIOD-NOT-OPEN) |
| RULE-FIN-013 | no reversal of a non-posted or already-reversed entry | ENT-FIN-004 | ✓ / ✓ | API-FIN-021, API-FIN-014 | FIN-409-NOT-POSTED, FIN-409-ALREADY-REVERSED |
| RULE-FIN-014 | a hard-closed period is never reopened | ENT-FIN-008 | ✓ / ✓ | API-FIN-024, API-FIN-026 | FIN-409-NOT-REOPENABLE |
| RULE-FIN-015 | close approval is a distinct permission | ENT-FIN-008 | ✓ / ✓ | DEFERRED (no declaration surface in v2) — satisfied by SEC's permission gate on API-FIN-026 and API-FIN-027 | — (a denial is FIN-403-FORBIDDEN) |
| RULE-FIN-016 | a posted entry is locked | ENT-FIN-004, ENT-FIN-005 | ✓ / ✓ | by omission — no update or delete path exists | — |
| RULE-FIN-017 | fiscal year, period and document date cohere | ENT-FIN-004, ENT-FIN-008 | ✓ / ✓ | API-FIN-019 | FIN-400-PERIOD-NOT-IN-YEAR, FIN-400-DOCDATE-OUTSIDE-PERIOD |
| RULE-FIN-018 | only CONSTANT and MAPPING derivations | ENT-FIN-010 | ✓ / ✓ | API-FIN-011 | FIN-422-UNSUPPORTED-DERIVATION |
| RULE-FIN-019 | the derivation spec matches its type | ENT-FIN-010 | ✓ / ✓ | API-FIN-011 | FIN-422-DERIVATION-SPEC |
| RULE-FIN-020 | the business value must be mapped | ENT-FIN-015, ENT-FIN-010 | ✓ / ✓ | API-FIN-020 | FIN-422-UNMAPPED-VALUE |
| RULE-FIN-021 | the event carries the business field the rule names | ENT-FIN-010, ENT-FIN-016 | ✓ / ✓ | API-FIN-020 | FIN-422-MISSING-BUSINESS-FIELD |
| RULE-FIN-022 | one active mapping per key | ENT-FIN-015 | ✓ / ✓ | API-FIN-039 | FIN-409-MAPPING-DUP |
| RULE-FIN-023 | a mapped account is an active leaf on save | ENT-FIN-015 | ✓ / ✓ | API-FIN-039, API-FIN-040 | FIN-422-MAPPED-ACCOUNT-INELIGIBLE |
| RULE-FIN-024 | a payment-method value is a defined code | ENT-FIN-015 | ✓ / ✓ | API-FIN-039 | FIN-422-INVALID-PAYMENT-METHOD |
| RULE-FIN-025 | one tag per dimension on a rule line | ENT-FIN-016 | ✓ / ✓ | API-FIN-011 | FIN-422-DIMENSION-TAG-DUP |
| RULE-FIN-026 | a dimension tag matches its value source | ENT-FIN-016 | ✓ / ✓ | API-FIN-011 | FIN-422-DIMENSION-TAG-SOURCE |
| RULE-FIN-027 | an event dimension value resolves to a defined value | ENT-FIN-016, ENT-FIN-003 | ✓ / ✓ | API-FIN-020 | FIN-422-UNRESOLVED-DIMENSION-VALUE |
| RULE-FIN-028 | the event date falls in a defined period | ENT-FIN-008 | ✓ / ✓ | API-FIN-020 | FIN-422-NO-DEFINED-PERIOD |

**SCREEN REGISTRY** — one composite screen per SCR-REQ, one SEC_PAGES row each (SEC-BE).
| Screen | Page code | Type | ENT | Permission names |
|---|---|---|---|---|
| SCR-REQ-FIN-001 Chart of accounts | FIN_ACCOUNTS | Search + Entry | ENT-FIN-001 | PERM_FIN_ACCOUNTS_VIEW, PERM_FIN_ACCOUNTS_CREATE, PERM_FIN_ACCOUNTS_UPDATE |
| SCR-REQ-FIN-002 Dimensions | FIN_DIMENSIONS | Master + Detail | ENT-FIN-002, ENT-FIN-003 | PERM_FIN_DIMENSIONS_VIEW, PERM_FIN_DIMENSIONS_CREATE, PERM_FIN_DIMENSIONS_UPDATE |
| SCR-REQ-FIN-003 Engine rules (v2 MODIFIED) | FIN_RULES | Master + Detail | ENT-FIN-009, ENT-FIN-010, ENT-FIN-016 | PERM_FIN_RULES_VIEW, PERM_FIN_RULES_CREATE, PERM_FIN_RULES_UPDATE |
| SCR-REQ-FIN-004 Recurring templates | FIN_RECURRING_TEMPLATES | Master + Detail | ENT-FIN-011, ENT-FIN-012 | PERM_FIN_RECURRING_TEMPLATES_VIEW, PERM_FIN_RECURRING_TEMPLATES_CREATE, PERM_FIN_RECURRING_TEMPLATES_UPDATE |
| SCR-REQ-FIN-005 Allocation rules | FIN_ALLOCATION_RULES | Master + Detail | ENT-FIN-013, ENT-FIN-014 | PERM_FIN_ALLOCATION_RULES_VIEW, PERM_FIN_ALLOCATION_RULES_CREATE, PERM_FIN_ALLOCATION_RULES_UPDATE |
| SCR-REQ-FIN-006 Journal entries (v2 MODIFIED) | FIN_JOURNAL_ENTRIES | Search + Entry | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | PERM_FIN_JOURNAL_ENTRIES_VIEW, PERM_FIN_JOURNAL_ENTRIES_CREATE, PERM_FIN_JOURNAL_ENTRIES_REVERSE |
| SCR-REQ-FIN-007 Fiscal periods and years | FIN_PERIODS | Master + Detail | ENT-FIN-007, ENT-FIN-008 | PERM_FIN_PERIODS_VIEW, PERM_FIN_PERIODS_CREATE, PERM_FIN_PERIODS_UPDATE, PERM_FIN_PERIODS_CLOSE_APPROVE |
| SCR-REQ-FIN-008 Account ledger | FIN_ACCOUNT_LEDGER | Report | ENT-FIN-005 | PERM_FIN_ACCOUNT_LEDGER_VIEW |
| SCR-REQ-FIN-009 Trial balance | FIN_TRIAL_BALANCE | Report | ENT-FIN-005 | PERM_FIN_TRIAL_BALANCE_VIEW |
| SCR-REQ-FIN-010 Balance sheet | FIN_BALANCE_SHEET | Report | ENT-FIN-005 | PERM_FIN_BALANCE_SHEET_VIEW |
| SCR-REQ-FIN-011 Income statement | FIN_INCOME_STATEMENT | Report | ENT-FIN-005 | PERM_FIN_INCOME_STATEMENT_VIEW |
| SCR-REQ-FIN-012 Dimension reports | FIN_DIMENSION_REPORTS | Report | ENT-FIN-005, ENT-FIN-006 | PERM_FIN_DIMENSION_REPORTS_VIEW |
| SCR-REQ-FIN-013 Account mappings (v2 ADDED) | FIN_ACCOUNT_MAPPINGS | Search + Entry | ENT-FIN-015 | PERM_FIN_ACCOUNT_MAPPINGS_VIEW, PERM_FIN_ACCOUNT_MAPPINGS_CREATE, PERM_FIN_ACCOUNT_MAPPINGS_UPDATE |

**LOOKUP REGISTRY** — every value is runtime-loaded from MDL (XM-FIN-001); no API or field spec hardcodes one.
| Lookup key | Used in field (`DBF-*`) | `ENT-*` | Table owner module |
|---|---|---|---|
| ACCOUNT_TYPE | DBF-FIN-005 | ENT-FIN-001 | MDL |
| DEBIT_CREDIT | DBF-FIN-006, DBF-FIN-056, DBF-FIN-105, DBF-FIN-127 | ENT-FIN-001, ENT-FIN-005, ENT-FIN-010, ENT-FIN-012 | MDL |
| PERIOD_STATE | DBF-FIN-082 | ENT-FIN-008 | MDL |
| FISCAL_YEAR_STATUS | DBF-FIN-069 | ENT-FIN-007 | MDL |
| JOURNAL_TYPE | DBF-FIN-039 | ENT-FIN-004 | MDL |
| JOURNAL_STATUS | DBF-FIN-040 | ENT-FIN-004 | MDL |
| ACCOUNTING_EVENT_TYPE | DBF-FIN-090, DBF-FIN-150 | ENT-FIN-009, ENT-FIN-015 | MDL |
| PAYMENT_METHOD | DBF-FIN-152 (only when the business field is PAYMENT_METHOD) | ENT-FIN-015 | MDL |
| ACCOUNT_DERIVATION_TYPE | DBF-FIN-101 | ENT-FIN-010 | MDL |
| AMOUNT_SOURCE_TYPE | DBF-FIN-103 | ENT-FIN-010 | MDL |
| DISTRIBUTION_TYPE | DBF-FIN-106, DBF-FIN-144 | ENT-FIN-010, ENT-FIN-014 | MDL |
| RECURRING_SCHEDULE_TYPE | DBF-FIN-112 | ENT-FIN-011 | MDL |
| RECURRING_FREQUENCY | DBF-FIN-113 | ENT-FIN-011 | MDL |
| FIN_EVENT_BUSINESS_FIELD | DBF-FIN-148, DBF-FIN-151, DBF-FIN-161 | ENT-FIN-010, ENT-FIN-015, ENT-FIN-016 | MDL |
| FIN_DIMENSION_VALUE_SOURCE | DBF-FIN-160 | ENT-FIN-016 | MDL |

**QRC SUMMARY** — QR-FIN-001 … QR-FIN-061; v2 adds QR-FIN-050 … QR-FIN-061 (Query Reference Catalog below).

```
DB ALIGNMENT      the manifest below — aligned; v2 corrects three wrong DBF citations of v1 (ADR-FIN-029)
XM STATUS         0 deferred — XM-FIN-001 ACTIVE (scope widened in v2); XM-FIN-002 RETIRED
SECURITY          13 screens; grants to the SYS_ADMIN role plus the least-privilege FIN_CLOSE_APPROVER role
DBF ROWS          165
XM ROWS           2
API ROWS          41
QR ROWS           61
```

## DB Alignment Manifest — FIN v2
Canonical binding of plan properties to db-script fields. Column names, database types and SRS references are read
from the db-script by `DBF-*`, never repeated here. Status: ✓ aligned · ✗ type mismatch · ⏸ deferred XM. No row is
✗ or ⏸. XM-FIN-001 binds every lookup-backed column: the 16 of v1 plus 6 in v2 (DBF-FIN-148, DBF-FIN-150,
DBF-FIN-151, DBF-FIN-152, DBF-FIN-160, DBF-FIN-161). Every required column that no endpoint writes says so on its
row; the only one is DBF-FIN-147. Language types come from CORE's type-mapping table.

| DBF | ENT | plan property | plan type | XM | status |
|---|---|---|---|---|---|
| DBF-FIN-001 | ENT-FIN-001 | accountPk | Long | — | ✓ |
| DBF-FIN-002 | ENT-FIN-001 | code | String | — | ✓ |
| DBF-FIN-003 | ENT-FIN-001 | nameAr | String | — | ✓ |
| DBF-FIN-004 | ENT-FIN-001 | nameEn | String | — | ✓ |
| DBF-FIN-005 | ENT-FIN-001 | accountTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-006 | ENT-FIN-001 | natureCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-007 | ENT-FIN-001 | parentAccountId | Long | — | ✓ |
| DBF-FIN-008 | ENT-FIN-001 | isLeafFl | Boolean | — | ✓ |
| DBF-FIN-009 | ENT-FIN-001 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-147 | ENT-FIN-001 | isRetainedEarningsFl | Boolean | — | ✓ seeded as data, never through an API (db-script §1) — see SEC-BE's bootstrap rows |
| DBF-FIN-010 | ENT-FIN-001 | createdBy | String | — | ✓ |
| DBF-FIN-011 | ENT-FIN-001 | createdAt | Instant | — | ✓ |
| DBF-FIN-012 | ENT-FIN-001 | updatedBy | String | — | ✓ |
| DBF-FIN-013 | ENT-FIN-001 | updatedAt | Instant | — | ✓ |
| DBF-FIN-014 | ENT-FIN-002 | dimensionPk | Long | — | ✓ |
| DBF-FIN-015 | ENT-FIN-002 | code | String | — | ✓ |
| DBF-FIN-016 | ENT-FIN-002 | nameAr | String | — | ✓ |
| DBF-FIN-017 | ENT-FIN-002 | nameEn | String | — | ✓ |
| DBF-FIN-018 | ENT-FIN-002 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-019 | ENT-FIN-002 | createdBy | String | — | ✓ |
| DBF-FIN-020 | ENT-FIN-002 | createdAt | Instant | — | ✓ |
| DBF-FIN-021 | ENT-FIN-002 | updatedBy | String | — | ✓ |
| DBF-FIN-022 | ENT-FIN-002 | updatedAt | Instant | — | ✓ |
| DBF-FIN-023 | ENT-FIN-003 | dimensionValuePk | Long | — | ✓ |
| DBF-FIN-024 | ENT-FIN-003 | dimensionId | Long | — | ✓ |
| DBF-FIN-025 | ENT-FIN-003 | code | String | — | ✓ |
| DBF-FIN-026 | ENT-FIN-003 | nameAr | String | — | ✓ |
| DBF-FIN-027 | ENT-FIN-003 | nameEn | String | — | ✓ |
| DBF-FIN-028 | ENT-FIN-003 | sortOrder | Integer | — | ✓ |
| DBF-FIN-029 | ENT-FIN-003 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-030 | ENT-FIN-003 | createdBy | String | — | ✓ |
| DBF-FIN-031 | ENT-FIN-003 | createdAt | Instant | — | ✓ |
| DBF-FIN-032 | ENT-FIN-003 | updatedBy | String | — | ✓ |
| DBF-FIN-033 | ENT-FIN-003 | updatedAt | Instant | — | ✓ |
| DBF-FIN-034 | ENT-FIN-004 | journalEntryPk | Long | — | ✓ |
| DBF-FIN-035 | ENT-FIN-004 | docNo | String | — | ✓ |
| DBF-FIN-036 | ENT-FIN-004 | docDate | LocalDate | — | ✓ |
| DBF-FIN-037 | ENT-FIN-004 | fiscalYearId | Long | — | ✓ |
| DBF-FIN-038 | ENT-FIN-004 | periodId | Long | — | ✓ |
| DBF-FIN-039 | ENT-FIN-004 | journalTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-040 | ENT-FIN-004 | statusCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-041 | ENT-FIN-004 | eventReference | String | — | ✓ |
| DBF-FIN-042 | ENT-FIN-004 | originalEntryId | Long | — | ✓ |
| DBF-FIN-043 | ENT-FIN-004 | reversalEntryId | Long | — | ✓ |
| DBF-FIN-044 | ENT-FIN-004 | descriptionAr | String | — | ✓ |
| DBF-FIN-045 | ENT-FIN-004 | descriptionEn | String | — | ✓ |
| DBF-FIN-046 | ENT-FIN-004 | postedAt | Instant | — | ✓ |
| DBF-FIN-047 | ENT-FIN-004 | createdBy | String | — | ✓ |
| DBF-FIN-048 | ENT-FIN-004 | createdAt | Instant | — | ✓ |
| DBF-FIN-049 | ENT-FIN-004 | updatedBy | String | — | ✓ |
| DBF-FIN-050 | ENT-FIN-004 | updatedAt | Instant | — | ✓ |
| DBF-FIN-051 | ENT-FIN-005 | journalLinePk | Long | — | ✓ |
| DBF-FIN-052 | ENT-FIN-005 | journalEntryId | Long | — | ✓ |
| DBF-FIN-053 | ENT-FIN-005 | lineNo | Integer | — | ✓ |
| DBF-FIN-054 | ENT-FIN-005 | accountId | Long | — | ✓ |
| DBF-FIN-055 | ENT-FIN-005 | amount | BigDecimal | — | ✓ |
| DBF-FIN-056 | ENT-FIN-005 | directionCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-057 | ENT-FIN-005 | isRemainderFl | Boolean | — | ✓ |
| DBF-FIN-058 | ENT-FIN-005 | descriptionAr | String | — | ✓ |
| DBF-FIN-059 | ENT-FIN-005 | descriptionEn | String | — | ✓ |
| DBF-FIN-060 | ENT-FIN-005 | createdAt | Instant | — | ✓ |
| DBF-FIN-061 | ENT-FIN-006 | journalLineDimensionPk | Long | — | ✓ |
| DBF-FIN-062 | ENT-FIN-006 | journalLineId | Long | — | ✓ |
| DBF-FIN-063 | ENT-FIN-006 | dimensionId | Long | — | ✓ |
| DBF-FIN-064 | ENT-FIN-006 | dimensionValueId | Long | — | ✓ |
| DBF-FIN-065 | ENT-FIN-007 | fiscalYearPk | Long | — | ✓ |
| DBF-FIN-066 | ENT-FIN-007 | code | String | — | ✓ |
| DBF-FIN-067 | ENT-FIN-007 | startDate | LocalDate | — | ✓ |
| DBF-FIN-068 | ENT-FIN-007 | endDate | LocalDate | — | ✓ |
| DBF-FIN-069 | ENT-FIN-007 | statusCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-070 | ENT-FIN-007 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-071 | ENT-FIN-007 | createdBy | String | — | ✓ |
| DBF-FIN-072 | ENT-FIN-007 | createdAt | Instant | — | ✓ |
| DBF-FIN-073 | ENT-FIN-007 | updatedBy | String | — | ✓ |
| DBF-FIN-074 | ENT-FIN-007 | updatedAt | Instant | — | ✓ |
| DBF-FIN-075 | ENT-FIN-008 | fiscalPeriodPk | Long | — | ✓ |
| DBF-FIN-076 | ENT-FIN-008 | fiscalYearId | Long | — | ✓ |
| DBF-FIN-077 | ENT-FIN-008 | periodNo | Integer | — | ✓ |
| DBF-FIN-078 | ENT-FIN-008 | nameAr | String | — | ✓ |
| DBF-FIN-079 | ENT-FIN-008 | nameEn | String | — | ✓ |
| DBF-FIN-080 | ENT-FIN-008 | startDate | LocalDate | — | ✓ |
| DBF-FIN-081 | ENT-FIN-008 | endDate | LocalDate | — | ✓ |
| DBF-FIN-082 | ENT-FIN-008 | statusCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-083 | ENT-FIN-008 | closedBy | String | — | ✓ |
| DBF-FIN-084 | ENT-FIN-008 | closedAt | Instant | — | ✓ |
| DBF-FIN-085 | ENT-FIN-008 | createdBy | String | — | ✓ |
| DBF-FIN-086 | ENT-FIN-008 | createdAt | Instant | — | ✓ |
| DBF-FIN-087 | ENT-FIN-008 | updatedBy | String | — | ✓ |
| DBF-FIN-088 | ENT-FIN-008 | updatedAt | Instant | — | ✓ |
| DBF-FIN-089 | ENT-FIN-009 | eventTypeRulePk | Long | — | ✓ |
| DBF-FIN-090 | ENT-FIN-009 | eventTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-091 | ENT-FIN-009 | nameAr | String | — | ✓ |
| DBF-FIN-092 | ENT-FIN-009 | nameEn | String | — | ✓ |
| DBF-FIN-093 | ENT-FIN-009 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-094 | ENT-FIN-009 | createdBy | String | — | ✓ |
| DBF-FIN-095 | ENT-FIN-009 | createdAt | Instant | — | ✓ |
| DBF-FIN-096 | ENT-FIN-009 | updatedBy | String | — | ✓ |
| DBF-FIN-097 | ENT-FIN-009 | updatedAt | Instant | — | ✓ |
| DBF-FIN-098 | ENT-FIN-010 | ruleLinePk | Long | — | ✓ |
| DBF-FIN-099 | ENT-FIN-010 | eventTypeRuleId | Long | — | ✓ |
| DBF-FIN-100 | ENT-FIN-010 | lineNo | Integer | — | ✓ |
| DBF-FIN-101 | ENT-FIN-010 | accountDerivationTypeCode | String | XM-FIN-001 | ✓ (v2: CONSTANT / MAPPING only) |
| DBF-FIN-102 | ENT-FIN-010 | accountDerivationValue | String | — | ✓ (v2: NULL-able, CONSTANT lines only) |
| DBF-FIN-148 | ENT-FIN-010 | accountBusinessFieldCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-103 | ENT-FIN-010 | amountSourceTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-104 | ENT-FIN-010 | amountSourceValue | String | — | ✓ |
| DBF-FIN-105 | ENT-FIN-010 | directionCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-106 | ENT-FIN-010 | distributionTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-107 | ENT-FIN-010 | isRemainderFl | Boolean | — | ✓ |
| DBF-FIN-108 | ENT-FIN-010 | createdAt | Instant | — | ✓ |
| DBF-FIN-109 | ENT-FIN-011 | recurringTemplatePk | Long | — | ✓ |
| DBF-FIN-110 | ENT-FIN-011 | nameAr | String | — | ✓ |
| DBF-FIN-111 | ENT-FIN-011 | nameEn | String | — | ✓ |
| DBF-FIN-112 | ENT-FIN-011 | scheduleTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-113 | ENT-FIN-011 | frequencyCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-114 | ENT-FIN-011 | startDate | LocalDate | — | ✓ |
| DBF-FIN-115 | ENT-FIN-011 | nextRunDate | LocalDate | — | ✓ |
| DBF-FIN-116 | ENT-FIN-011 | endDate | LocalDate | — | ✓ |
| DBF-FIN-117 | ENT-FIN-011 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-118 | ENT-FIN-011 | createdBy | String | — | ✓ |
| DBF-FIN-119 | ENT-FIN-011 | createdAt | Instant | — | ✓ |
| DBF-FIN-120 | ENT-FIN-011 | updatedBy | String | — | ✓ |
| DBF-FIN-121 | ENT-FIN-011 | updatedAt | Instant | — | ✓ |
| DBF-FIN-122 | ENT-FIN-012 | recurringTemplateLinePk | Long | — | ✓ |
| DBF-FIN-123 | ENT-FIN-012 | recurringTemplateId | Long | — | ✓ |
| DBF-FIN-124 | ENT-FIN-012 | lineNo | Integer | — | ✓ |
| DBF-FIN-125 | ENT-FIN-012 | accountId | Long | — | ✓ |
| DBF-FIN-126 | ENT-FIN-012 | amount | BigDecimal | — | ✓ |
| DBF-FIN-127 | ENT-FIN-012 | directionCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-128 | ENT-FIN-012 | dimensionValueId | Long | — | ✓ |
| DBF-FIN-129 | ENT-FIN-012 | createdAt | Instant | — | ✓ |
| DBF-FIN-130 | ENT-FIN-013 | allocationRulePk | Long | — | ✓ |
| DBF-FIN-131 | ENT-FIN-013 | nameAr | String | — | ✓ |
| DBF-FIN-132 | ENT-FIN-013 | nameEn | String | — | ✓ |
| DBF-FIN-133 | ENT-FIN-013 | sourceAccountId | Long | — | ✓ |
| DBF-FIN-134 | ENT-FIN-013 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-135 | ENT-FIN-013 | createdBy | String | — | ✓ |
| DBF-FIN-136 | ENT-FIN-013 | createdAt | Instant | — | ✓ |
| DBF-FIN-137 | ENT-FIN-013 | updatedBy | String | — | ✓ |
| DBF-FIN-138 | ENT-FIN-013 | updatedAt | Instant | — | ✓ |
| DBF-FIN-139 | ENT-FIN-014 | allocationTargetPk | Long | — | ✓ |
| DBF-FIN-140 | ENT-FIN-014 | allocationRuleId | Long | — | ✓ |
| DBF-FIN-141 | ENT-FIN-014 | lineNo | Integer | — | ✓ |
| DBF-FIN-142 | ENT-FIN-014 | targetAccountId | Long | — | ✓ |
| DBF-FIN-143 | ENT-FIN-014 | dimensionValueId | Long | — | ✓ |
| DBF-FIN-144 | ENT-FIN-014 | distributionTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-145 | ENT-FIN-014 | distributionValue | BigDecimal | — | ✓ |
| DBF-FIN-146 | ENT-FIN-014 | isRemainderFl | Boolean | — | ✓ |
| DBF-FIN-149 | ENT-FIN-015 | accountMappingPk | Long | — | ✓ |
| DBF-FIN-150 | ENT-FIN-015 | eventTypeCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-151 | ENT-FIN-015 | businessFieldCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-152 | ENT-FIN-015 | businessValue | String | XM-FIN-001 | ✓ (XM-FIN-001 applies only when businessFieldCode = PAYMENT_METHOD — RULE-FIN-024) |
| DBF-FIN-153 | ENT-FIN-015 | accountId | Long | — | ✓ |
| DBF-FIN-154 | ENT-FIN-015 | isActiveFl | Boolean | — | ✓ |
| DBF-FIN-155 | ENT-FIN-015 | createdBy | String | — | ✓ |
| DBF-FIN-156 | ENT-FIN-015 | createdAt | Instant | — | ✓ |
| DBF-FIN-157 | ENT-FIN-015 | updatedBy | String | — | ✓ |
| DBF-FIN-158 | ENT-FIN-015 | updatedAt | Instant | — | ✓ |
| DBF-FIN-159 | ENT-FIN-016 | ruleLineDimensionPk | Long | — | ✓ |
| DBF-FIN-160 | ENT-FIN-016 | valueSourceCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-161 | ENT-FIN-016 | businessFieldCode | String | XM-FIN-001 | ✓ |
| DBF-FIN-162 | ENT-FIN-016 | ruleLineId | Long | — | ✓ |
| DBF-FIN-163 | ENT-FIN-016 | dimensionId | Long | — | ✓ |
| DBF-FIN-164 | ENT-FIN-016 | dimensionValueId | Long | — | ✓ |
| DBF-FIN-165 | ENT-FIN-016 | createdAt | Instant | — | ✓ |

## Query Reference Catalog (QR-FIN-*)

> Logical specification only, never executable code. The implementer rewrites every entry with the real entity
> classes and the project's query strategy. The v1 entries stand as delivered; the v2 entries and the three
> modified ones carry the full §5 form below the summary table.

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
| QR-FIN-015 | SAVE | API-FIN-011 | ENT-FIN-010 | create | add rule line — MODIFIED v2: also writes accountBusinessFieldCode; accountDerivationValue stays empty on a MAPPING line |
| QR-FIN-016 | EXISTS | API-FIN-011, API-FIN-016 | ENT-FIN-010, ENT-FIN-014 | RULE-FIN-003 | exactly one remainder line/target when a percentage distribution exists (scope widened here to match the id definition below and the code) |
| QR-FIN-017 | FIND_BY_CRITERIA | API-FIN-012 | ENT-FIN-011 | search | search templates |
| QR-FIN-018 | SAVE | API-FIN-013 | ENT-FIN-011, ENT-FIN-012 | create | create template with lines |
| QR-FIN-019 | FIND_ONE | API-FIN-014 | ENT-FIN-011 | run | load the template to run, by id (inherited `findById`; no due-date selection query exists — FIN exposes no scheduler endpoint) |
| QR-FIN-020 | FIND_BY_CRITERIA | API-FIN-015 | ENT-FIN-013 | search | search allocation rules |
| QR-FIN-021 | SAVE | API-FIN-016 | ENT-FIN-013, ENT-FIN-014 | create | create allocation rule with targets |
| QR-FIN-022 | FIND_ONE | API-FIN-017 | ENT-FIN-013 | run | load allocation rule + current source balance (via QR-FIN-043 — the shared balance aggregation; the earlier citation of QR-FIN-040, the hard-closed-period check, was wrong) |
| QR-FIN-023 | FIND_BY_CRITERIA | API-FIN-018 | ENT-FIN-004 | search | search journal entries |
| QR-FIN-024 | SAVE | API-FIN-019 | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | create | build manual entry (DRAFT) |
| QR-FIN-025 | SAVE | API-FIN-020 | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | create | build event entry from its rule — MODIFIED v2: each line's account is the constant or the mapped one, each resolved tag writes a JournalLineDimension row (REQ-FIN-070), the header insert is flushed (RULE-FIN-004) |
| QR-FIN-026 | EXISTS | API-FIN-020 | ENT-FIN-004 | RULE-FIN-004 | duplicate eventReference — MODIFIED v2: the friendly pre-check; UQ_FIN_JOURNAL_ENTRY_EVENT_REF is the guarantee |
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
| QR-FIN-050 | FIND_BY_CRITERIA | API-FIN-038 | ENT-FIN-015 | search | search account mappings, with the account's display columns (ADR-FIN-031) |
| QR-FIN-051 | SAVE | API-FIN-039 | ENT-FIN-015 | create | create account mapping |
| QR-FIN-052 | EXISTS | API-FIN-039 | ENT-FIN-015 | RULE-FIN-022 | an active mapping already holds the key |
| QR-FIN-053 | UPDATE | API-FIN-040 | ENT-FIN-015 | update | change a mapping's account |
| QR-FIN-054 | UPDATE | API-FIN-041 | ENT-FIN-015 | deactivate | deactivate a mapping |
| QR-FIN-055 | FIND_ONE | API-FIN-020 | ENT-FIN-015 | RULE-FIN-020 | the active mapping for (event type, business field, value) |
| QR-FIN-056 | SAVE | API-FIN-011 | ENT-FIN-016 | create | a rule line's dimension tags, with the line |
| QR-FIN-057 | FIND_BY_CRITERIA | API-FIN-020 | ENT-FIN-016 | read | the tags of the active rule's lines |
| QR-FIN-058 | FIND_ONE | API-FIN-020 | ENT-FIN-003 | RULE-FIN-027 | the dimension value with a given code in a given dimension |
| QR-FIN-059 | FIND_ONE | API-FIN-020 | ENT-FIN-008 | RULE-FIN-028 | the fiscal period whose span covers a date |
| QR-FIN-060 | FIND_ONE | API-FIN-039, API-FIN-040 | ENT-FIN-001 | RULE-FIN-023 | the account a mapping names, with its leaf and active flags |
| QR-FIN-061 | FIND_ONE | API-FIN-011 | ENT-FIN-002, ENT-FIN-003 | RULE-FIN-026 | a tag's dimension and its CONSTANT value, by id |

#### QR-FIN-015 — add rule line (MODIFIED v2)
Phase        : DATA-DOM (ENT-FIN-010)
API          : API-FIN-011 — add rule line
Entity       : ENT-FIN-010
Operation    : SAVE
Intent       : insert one rule line; v2 also writes accountBusinessFieldCode, and accountDerivationValue stays empty on a MAPPING line
Logical spec : INSERT INTO FIN_RULE_LINE (rule_line_pk ← nextval SEQ_FIN_RULE_LINE, event_type_rule_id, line_no, account_derivation_type_code, account_derivation_value, account_business_field_code, amount_source_type_code, amount_source_value, direction_code, distribution_type_code, is_remainder_fl)
Join         : NONE
Transaction  : READ_WRITE — the same transaction as the line's tags (QR-FIN-056)
Locking      : the parent FIN_EVENT_TYPE_RULE row is loaded PESSIMISTIC_WRITE before line_no is read (API-FIN-011 Concurrency)
Pagination   : NO
Filters      : —
Result shape : full entity
Null handling: account_derivation_value NULL on MAPPING, account_business_field_code NULL on CONSTANT (CHK_FIN_RULE_LINE_DERIVATION_SPEC)

#### QR-FIN-025 — build event entry from its rule (MODIFIED v2)
Phase        : SVC-API (SVC-API-INT)
API          : API-FIN-020 — build event entry
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006
Operation    : SAVE (read half: the rule's lines)
Intent       : read the active rule's lines in lineNo order, then insert the header, its lines and one JournalLineDimension row per resolved tag
Logical spec : SELECT … FROM FIN_RULE_LINE WHERE event_type_rule_id = :ruleId ORDER BY line_no; INSERT FIN_JOURNAL_ENTRY (flushed at once, so a duplicate event_reference is refused inside the transaction); INSERT FIN_JOURNAL_LINE per built line; INSERT FIN_JOURNAL_LINE_DIM per resolved tag
Join         : NONE
Transaction  : READ_WRITE — one transaction from the duplicate pre-check to the post (QR-FIN-033)
Locking      : the fiscal-year row lock guards the docNo series (as API-FIN-019); UQ_FIN_JOURNAL_ENTRY_EVENT_REF decides a concurrent duplicate (QR-FIN-026)
Pagination   : NO
Filters      : —
Result shape : full entity with lines and line dimensions
Null handling: event_reference is set on every event-built header; description_ar/description_en as sent

#### QR-FIN-026 — duplicate event reference (MODIFIED v2)
Phase        : SVC-API (SVC-API-INT)
API          : API-FIN-020 — build event entry
Entity       : ENT-FIN-004
Operation    : EXISTS
Intent       : has this event reference already produced an entry? The friendly half of RULE-FIN-004
Logical spec : SELECT EXISTS (SELECT 1 FROM FIN_JOURNAL_ENTRY WHERE event_reference = :eventReference)
Join         : NONE
Transaction  : READ_ONLY inside the build transaction
Locking      : none on the read. The read decides and the write follows, so two concurrent callers both pass it; UQ_FIN_JOURNAL_ENTRY_EVENT_REF refuses the second insert and FinPersistenceRefusalAdvisor answers FIN-409-DUPLICATE-EVENT (ADR-FIN-030)
Pagination   : NO
Filters      : event_reference EXACT
Result shape : boolean
Null handling: never called with a null reference — eventReference is required on the event request

#### QR-FIN-050 — search account mappings
Phase        : SVC-API (SVC-API-SEARCH)
API          : API-FIN-038 — search account mappings
Entity       : ENT-FIN-015
Operation    : FIND_BY_CRITERIA
Intent       : the mappings matching the screen's filters, each with the display data of its account
Logical spec : SELECT m.*, a.code, a.name_ar, a.name_en FROM FIN_ACCOUNT_MAPPING m JOIN FIN_ACCOUNT a ON a.account_pk = m.account_id WHERE [m.event_type_code = :eventTypeCode] AND [m.business_field_code = :businessFieldCode] AND [m.business_value LIKE :businessValue] AND [m.account_id = :accountId] AND [m.is_active_fl = :isActiveFl] ORDER BY :sort page :page size :size
Join         : required — FIN_ACCOUNT, for the account code and bilingual name the result columns show (ADR-FIN-031)
Transaction  : READ_ONLY
Locking      : NONE
Pagination   : YES (Page<T>) — default size 20, max 200
Filters      : eventTypeCode EXACT · businessFieldCode EXACT · businessValue LIKE · accountId EXACT · isActiveFl EXACT
Result shape : projection — the mapping's columns plus accountCode, accountNameAr, accountNameEn
Null handling: every filter optional; an empty page is success

#### QR-FIN-051 — create account mapping
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-039 — create account mapping
Entity       : ENT-FIN-015
Operation    : SAVE
Intent       : insert one active mapping
Logical spec : INSERT INTO FIN_ACCOUNT_MAPPING (account_mapping_pk ← nextval SEQ_FIN_ACCOUNT_MAPPING, event_type_code, business_field_code, business_value, account_id, is_active_fl = TRUE, created_by, created_at) — flushed at once
Join         : NONE
Transaction  : READ_WRITE
Locking      : UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY refuses a concurrent second active row for the same key; the refusal is answered as FIN-409-MAPPING-DUP (ADR-FIN-030)
Pagination   : NO
Filters      : —
Result shape : full entity
Null handling: no nullable business column; updated_by/updated_at NULL until the first change

#### QR-FIN-052 — an active mapping already holds the key
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-039 — create account mapping
Entity       : ENT-FIN-015
Operation    : EXISTS
Intent       : RULE-FIN-022 — is there an active mapping for this (event type, business field, business value)?
Logical spec : SELECT EXISTS (SELECT 1 FROM FIN_ACCOUNT_MAPPING WHERE event_type_code = :e AND business_field_code = :f AND business_value = :v AND is_active_fl)
Join         : NONE
Transaction  : READ_ONLY inside the create transaction
Locking      : none on the read; the partial unique index is the guarantee (QR-FIN-051)
Pagination   : NO
Filters      : three key columns EXACT, is_active_fl TRUE
Result shape : boolean
Null handling: all three key values are required on the request

#### QR-FIN-053 — change a mapping's account
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-040 — update account mapping
Entity       : ENT-FIN-015
Operation    : UPDATE
Intent       : replace the account of one mapping and nothing else (ADR-FIN-024)
Logical spec : UPDATE FIN_ACCOUNT_MAPPING SET account_id = :accountId, updated_by, updated_at WHERE account_mapping_pk = :id
Join         : NONE
Transaction  : READ_WRITE
Locking      : NONE — last writer wins between two concurrent changes, each validated against its own account
Pagination   : NO
Filters      : account_mapping_pk EXACT
Result shape : full entity
Null handling: —

#### QR-FIN-054 — deactivate a mapping
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-041 — deactivate account mapping
Entity       : ENT-FIN-015
Operation    : UPDATE
Intent       : soft-delete: the mapping stops resolving (RULE-FIN-020) and keeps its row for the audit trail
Logical spec : UPDATE FIN_ACCOUNT_MAPPING SET is_active_fl = FALSE, updated_by, updated_at WHERE account_mapping_pk = :id
Join         : NONE
Transaction  : READ_WRITE
Locking      : NONE
Pagination   : NO
Filters      : account_mapping_pk EXACT
Result shape : full entity
Null handling: —

#### QR-FIN-055 — the active mapping for an event's business value
Phase        : SVC-API (SVC-API-INT)
API          : API-FIN-020 — build event entry
Entity       : ENT-FIN-015
Operation    : FIND_ONE
Intent       : RULE-FIN-020 — the account a MAPPING line resolves to; none means the event is rejected, never a default
Logical spec : SELECT account_id FROM FIN_ACCOUNT_MAPPING WHERE event_type_code = :ruleEventType AND business_field_code = :lineField AND business_value = :eventValue AND is_active_fl
Join         : NONE — the account itself is then validated by QR-FIN-030 with every other line
Transaction  : READ_ONLY inside the build transaction
Locking      : NONE — a mapping changed after this read governs later events only (REQ-FIN-058)
Pagination   : NO
Filters      : three key columns EXACT, is_active_fl TRUE (served by UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY)
Result shape : projection account_id
Null handling: zero rows → FIN-422-UNMAPPED-VALUE; at most one row by the partial unique index

#### QR-FIN-056 — save a rule line's dimension tags
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-011 — add rule line
Entity       : ENT-FIN-016
Operation    : SAVE
Intent       : insert the submitted tags of the line just saved (ADR-FIN-025)
Logical spec : INSERT INTO FIN_RULE_LINE_DIM (rule_line_dimension_pk ← nextval SEQ_FIN_RULE_LINE_DIM, value_source_code, business_field_code, rule_line_id, dimension_id, dimension_value_id) per tag
Join         : NONE
Transaction  : READ_WRITE — the line's transaction (QR-FIN-015)
Locking      : NONE — the rows hang off a line no one else can see yet; UQ_FIN_RULE_LINE_DIM_LINE_DIM backs RULE-FIN-025
Pagination   : NO
Filters      : —
Result shape : full entities
Null handling: dimension_value_id NULL on BUSINESS_FIELD, business_field_code NULL on CONSTANT (CHK_FIN_RULE_LINE_DIM_VALUE_SPEC)

#### QR-FIN-057 — the tags of a rule's lines
Phase        : SVC-API (SVC-API-INT)
API          : API-FIN-020 — build event entry
Entity       : ENT-FIN-016
Operation    : FIND_BY_CRITERIA
Intent       : every dimension tag of the lines of the active rule, in one read
Logical spec : SELECT … FROM FIN_RULE_LINE_DIM WHERE rule_line_id IN (:lineIds)
Join         : NONE
Transaction  : READ_ONLY inside the build transaction
Locking      : NONE
Pagination   : NO
Filters      : rule_line_id SET
Result shape : full entities, grouped by rule line in the service
Null handling: a line with no tag builds a journal line with no dimension (v1 behaviour)

#### QR-FIN-058 — dimension value by code within a dimension
Phase        : SVC-API (SVC-API-INT)
API          : API-FIN-020 — build event entry
Entity       : ENT-FIN-003
Operation    : FIND_ONE
Intent       : RULE-FIN-027 — the DimensionValue whose code equals the event's value, inside the tagged dimension (ADR-FIN-026)
Logical spec : SELECT dimension_value_pk, is_active_fl FROM FIN_DIMENSION_VALUE WHERE dimension_id = :dimensionId AND code = :eventValue
Join         : NONE
Transaction  : READ_ONLY inside the build transaction
Locking      : NONE
Pagination   : NO
Filters      : dimension_id EXACT, code EXACT (served by UQ_FIN_DIMENSION_VALUE_DIM_CODE)
Result shape : projection
Null handling: zero rows → FIN-422-UNRESOLVED-DIMENSION-VALUE; an inactive match is left to RULE-FIN-009 (QR-FIN-032)

#### QR-FIN-059 — the fiscal period covering a date
Phase        : SVC-API (SVC-API-INT)
API          : API-FIN-020 — build event entry
Entity       : ENT-FIN-008
Operation    : FIND_ONE
Intent       : RULE-FIN-028 against RULE-FIN-008 — does any period cover the event date, and which one
Logical spec : SELECT fiscal_period_pk, fiscal_year_id, status_code FROM FIN_FISCAL_PERIOD WHERE :docDate BETWEEN start_date AND end_date
Join         : NONE
Transaction  : READ_ONLY inside the build transaction
Locking      : NONE here; the period state is read again at post by QR-FIN-031
Pagination   : NO
Filters      : docDate within [start_date, end_date]
Result shape : projection
Null handling: zero rows → FIN-422-NO-DEFINED-PERIOD (never FIN-409-PERIOD-NOT-OPEN). More than one row is possible: nothing stops two fiscal years from being created with overlapping spans, and this FIND_ONE then either fails or returns one non-deterministically. v1 defect, stated not hidden; CS-FIN-001 does not undertake it (ADR-FIN-040)

#### QR-FIN-060 — the account a mapping names
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-039 — create account mapping; API-FIN-040 — update account mapping
Entity       : ENT-FIN-001
Operation    : FIND_ONE
Intent       : RULE-FIN-023 — the account's leaf and active flags, read on save
Logical spec : SELECT account_pk, is_leaf_fl, is_active_fl FROM FIN_ACCOUNT WHERE account_pk = :accountId
Join         : NONE
Transaction  : READ_ONLY inside the write transaction
Locking      : NONE — an account deactivated after the save is still caught at post by RULE-FIN-007 (REQ-FIN-052)
Pagination   : NO
Filters      : account_pk EXACT
Result shape : projection
Null handling: zero rows → FIN-404-ACCOUNT

#### QR-FIN-061 — a tag's dimension and constant value, by id
Phase        : SVC-API (SVC-API-CRUD)
API          : API-FIN-011 — add rule line
Entity       : ENT-FIN-002, ENT-FIN-003
Operation    : FIND_ONE
Intent       : RULE-FIN-026 — the tagged dimension exists, and a CONSTANT tag's value belongs to it
Logical spec : SELECT dimension_pk FROM FIN_DIMENSION WHERE dimension_pk = :dimensionId; SELECT dimension_value_pk, dimension_id FROM FIN_DIMENSION_VALUE WHERE dimension_value_pk = :dimensionValueId
Join         : NONE
Transaction  : READ_ONLY inside the line's transaction
Locking      : NONE
Pagination   : NO
Filters      : primary keys EXACT
Result shape : projection
Null handling: unknown dimension → FIN-404-DIMENSION; unknown value → FIN-404-DIMVALUE; a value of another dimension → FIN-422-DIMENSION-TAG-SOURCE

Join governance. Every posting-pipeline query stays intra-module. The one cross-module read is the lookup
validation of XM-FIN-001, an in-process `MdlLookupApi` call and never a SQL join. No query joins to resolve a lookup
label: every lookup-backed column returns its code and the frontend labels it from MDL. v2 adds exactly one join
for parent data, QR-FIN-050's FIN_ACCOUNT display columns (ADR-FIN-031).

---

















## Error Catalog — FIN v2

Envelope: `LocalizedException → {code, messageAr, messageEn}`. Runtime code format: `FIN-{http}[-{SLUG}]`. The one
location of every code; downstream stages cite the code and never restate the text. v2 edits nine v1 rows (each says
what changed), strikes FIN-422-MAPPING-UNSUPPORTED and adds twelve rows at the end.

| code | RULE / PLATFORM-STD | API | HTTP | trigger | message-AR | message-EN |
|---|---|---|---|---|---|---|
| FIN-409-ACCOUNT-DUP | PLATFORM-STD | API-FIN-002 | 409 | duplicate account code | رمز الحساب مستخدم بالفعل | Account code already in use |
| FIN-409-PARENT-NOT-LEAF-ELIGIBLE | RULE-FIN-001 | API-FIN-002 | 409 | parent account cannot remain a leaf | لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا | An account with sub-accounts cannot accept direct posting |
| FIN-400-INVALID-LOOKUP | PLATFORM-STD (XM-FIN-001) | many | 400 | submitted code not found in MDL | القيمة المُدخلة غير صالحة | The submitted value is not valid |
| FIN-409-HAS-CHILDREN | RULE-FIN-001 | API-FIN-003 | 409 | marking a parent as leaf | لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا | An account with sub-accounts cannot accept direct posting |
| FIN-404-ACCOUNT | PLATFORM-STD | API-FIN-002, 003, 004, 013, 016, 019, 020, 027, 028, 039, 040 | 404 | unknown account id — every path that resolves an account FK raises it: API-FIN-002's `parentAccountId` (AccountService.java:168, reached only from `create`), a template line's `accountId` (API-FIN-013, RecurringTemplateService.java:304), an allocation rule's source and target accounts (API-FIN-016, AllocationRuleService.java:278), a manual entry line's `accountId` (API-FIN-019, JournalEntryService.java:349), the account code a CONSTANT rule line names (API-FIN-020, EventEntryService.java:193) and API-FIN-027's Retained Earnings account, where the reachable failure is "none marked" rather than an unknown id (FiscalYearService.java:347). v2: the `accountId` of an account mapping on create and on update (API-FIN-039, API-FIN-040, QR-FIN-060) | الحساب غير موجود | Account not found |
| FIN-409-DIMENSION-DUP | PLATFORM-STD | API-FIN-006 | 409 | duplicate dimension code | رمز البُعد مستخدم بالفعل | Dimension code already in use |
| FIN-409-DIMVALUE-DUP | RULE-FIN-002 | API-FIN-007 | 409 | duplicate code within dimension | هذا الرمز مستخدم بالفعل ضمن هذا البُعد | This code is already used within this dimension |
| FIN-404-DIMENSION | PLATFORM-STD | API-FIN-007, 008, 011, 032 | 404 | unknown dimension id — v2: also a dimension tag's `dimensionId` on API-FIN-011 (QR-FIN-061) | البُعد غير موجود | Dimension not found |
| FIN-404-DIMVALUE | PLATFORM-STD | API-FIN-011, 035 | 404 | unknown dimension-value id — v2: also a CONSTANT tag's `dimensionValueId` on API-FIN-011 (QR-FIN-061) | قيمة البُعد غير موجودة | Dimension value not found |
| FIN-409-RULE-DUP | PLATFORM-STD (§6.4) | API-FIN-010 | 409 | event type already has an active rule | يوجد بالفعل قاعدة نشطة لهذا النوع | An active rule already exists for this event type |
| FIN-409-REMAINDER-COUNT | RULE-FIN-003 | API-FIN-011, 016, 017, 020 | 409 | wrong remainder-line/target count — both guards are re-run on the consuming path, not only at create: the rule-line set is re-validated when an event entry is built (API-FIN-020, EventEntryService.java:104 → EventTypeRuleDomain.java:177) and the target set when an allocation rule is run (API-FIN-017, AllocationRuleService.java:179 → AllocationRuleDomain.java:122) | يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي | Exactly one remainder line is required when any percentage distribution is present |
| FIN-404-RULE | PLATFORM-STD | API-FIN-011, 034 | 404 | unknown event-type rule id | القاعدة غير موجودة | Rule not found |
| FIN-400-MISSING-FREQUENCY | PLATFORM-STD | API-FIN-013 | 400 | recurring template with no frequency | يلزم تحديد التكرار للقالب المتكرر | A frequency is required for a recurring template |
| FIN-404-TEMPLATE | PLATFORM-STD | API-FIN-014, 036 | 404 | unknown recurring template id — raised by `RecurringTemplateService.run` (API-FIN-014) and, since the template deactivate landed, by `RecurringTemplateService.deactivate` (API-FIN-036) resolving the same path id. API-FIN-036 raises it ONLY for an id that does not exist; deactivating an already-inactive template is not an error, and there is deliberately no "exists but is inactive" code — see the API-FIN-036 block in SVC-API-CRUD | القالب المتكرر غير موجود | Recurring template not found |
| FIN-404-ALLOCATION-RULE | PLATFORM-STD | API-FIN-017, 037 | 404 | unknown allocation rule id — raised by `AllocationRuleService.run` (API-FIN-017) and, since the rule deactivate landed, by `AllocationRuleService.deactivate` (API-FIN-037) resolving the same path id. API-FIN-037 raises it ONLY for an id that does not exist; deactivating an already-inactive rule is not an error, and there is deliberately no "exists but is inactive" code — see the API-FIN-037 block in SVC-API-CRUD | قاعدة التوزيع غير موجودة | Allocation rule not found |
| FIN-409-NOT-ACTIVE | (no RULE — recorded human decision, 2026-09-12) | API-FIN-014, 017 | 409 | running a DEACTIVATED recurring template (API-FIN-014) or a deactivated allocation rule (API-FIN-017). Thrown by `RecurringTemplateDomain.assertCanRun()` (a NEW Domain companion for ENT-FIN-011) and `AllocationRuleDomain.assertCanRun()`, each the first thing its service's `run` does after loading the row and before any period, line or target is resolved, so a retired definition posts nothing at all. `Status.CONFLICT` → HTTP 409, consistent with every other FIN refusal whose cause is the target row's own state (FIN-409-NOT-POSTED, FIN-409-NOT-REOPENABLE, FIN-409-PERIOD-NOT-OPEN, FIN-409-INVALID-TRANSITION); the screen's own 404 was NOT reused because the row does exist. **NO RULE-FIN-* STATES THIS GATE.** It closes the "deactivate does not stop the run" OPEN DEFECT by decision, not by implementing a requirement that was always there — do not cite it as pre-existing spec | هذا التعريف غير نشط ولا يمكن تشغيله | This definition is deactivated and cannot be run |
| FIN-409-UNBALANCED | RULE-FIN-006 | API-FIN-019, 020, 014, 017, 021, 027 | 409 | debits ≠ credits — checked once in JournalEntryService.createManual (API-FIN-019) and once in JournalPostingService.buildValidateAndPost (JournalPostingService.java:163), which is the single posting pipeline for API-FIN-014, 017, 020 AND for API-FIN-021's reversal and API-FIN-027's closing/opening pair; the last two were missing from this row | القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن | The entry is unbalanced — total debits do not equal total credits |
| FIN-409-NOT-POSTABLE-ACCOUNT | RULE-FIN-007 | API-FIN-019, 020, 014, 017, 021, 027 | 409 | non-leaf/inactive account — AccountDomain.checkPostable runs per line in JournalEntryService.createManual (API-FIN-019, :350) and in JournalPostingService.buildValidateAndPost (:141), so it also covers API-FIN-021's reversal and API-FIN-027's closing/opening pair. v2: on API-FIN-020 it applies unchanged to an account taken from an account mapping — a mapped account that has since gained a child or been deactivated rejects the event (REQ-FIN-052, AC-FIN-054, AC-FIN-055) | الحساب المستهدف لا يقبل ترحيلاً مباشرًا | The target account does not accept direct posting |
| FIN-409-PERIOD-NOT-OPEN | RULE-FIN-008 | API-FIN-019, 020, 014, 017, 021 | 409 | period not Open at post — API-FIN-021 raises it twice over: from the shared gate in JournalPostingService.buildValidateAndPost (:164) and, before that, when the original's period is closed and the ledger has no open period to receive the reversal (JournalEntryDomain.java:329). API-FIN-027 is deliberately NOT listed: RULE-FIN-008 exempts the year-end CLOSING/OPENING types (JournalEntryDomain.checkTargetPeriodOpenForGeneratedEntry, :236). v2 (REQ-FIN-073, REQ-FIN-075): on API-FIN-020 this is the answer for every EXISTING period whose state is not OPEN, distinct from every validation and configuration code, and it is the one rejection that clears once the target period is opened — published to the event consumer as its wait-and-retry signal (DOC phase). A date no period covers is FIN-422-NO-DEFINED-PERIOD, never this code | الفترة المستهدفة غير مفتوحة | The target period is not open |
| FIN-409-INVALID-DIMENSION | RULE-FIN-009 | API-FIN-013, 014, 016, 017, 019, 020, 021, 027 | 409 | invalid/inactive dimension value — RULE-FIN-009 (DimensionValueDomain.checkUsableOnLine) runs in JournalEntryService.createManual (API-FIN-019, :376) and in JournalPostingService.buildValidateAndPost (:144), so it also covers API-FIN-021 and API-FIN-027. API-FIN-013 and API-FIN-016 raise the SAME code for a different reason — plain FK resolution of a submitted `dimensionValueId` that does not exist (RecurringTemplateService.java:318, AllocationRuleService.java:291) — which is why they belong in this row even though no rule is evaluated there. v2: on API-FIN-020 it also covers a value resolved from the event or taken from a CONSTANT tag that is inactive or not of the tagged dimension (REQ-FIN-066, REQ-FIN-067, AC-FIN-072) | قيمة البُعد غير صالحة | The dimension value is invalid |
| FIN-409-DUPLICATE-EVENT | RULE-FIN-004 | API-FIN-020 | 409 | repeated eventReference — MODIFIED v2: raised by the pre-check (QR-FIN-026) AND by the store's refusal on UQ_FIN_JOURNAL_ENTRY_EVENT_REF when two events with one reference arrive together, translated by FinPersistenceRefusalAdvisor (ADR-FIN-030); a duplicate is never answered as a generic data-integrity or server error (REQ-FIN-072, AC-FIN-077, AC-FIN-078). Published to the event consumer as "already posted — acknowledge" (DOC phase) | تم بالفعل ترحيل قيد لهذا المرجع | An entry for this event reference has already been posted |
| FIN-404-NO-ACTIVE-RULE | RULE-FIN-005 | API-FIN-020 | 404 | no active rule for event type | لا توجد قاعدة نشطة لهذا النوع من الأحداث | No active rule exists for this event type |
| ~~FIN-422-MAPPING-UNSUPPORTED~~ STRUCK | — (retired v2) | — | — | RETIRED in v2 — MAPPING derivation is implemented (REQ-FIN-050, API-FIN-020) and DIRECT is removed (ADR-FIN-010), so the cause this code named no longer exists (ADR-FIN-027, ADR-FIN-030). Struck rather than deleted, following this table's precedent for unreachable codes. The constant `FinErrorCodes.FIN_422_MAPPING_UNSUPPORTED` and its two bundle entries are to be removed with the MAPPING implementation; no v2 path raises it | — | — |
| FIN-409-NOT-POSTED | RULE-FIN-013 | API-FIN-021, 014 | 409 | reversing a non-posted entry — raised by the same JournalEntryDomain.assertCanReverse guard as FIN-409-ALREADY-REVERSED (JournalEntryDomain.java:284), so a REVERSING template run reaches it too (API-FIN-014, RecurringTemplateService.java:249) | لا يمكن عكس قيد غير مُرحَّل | A non-posted entry cannot be reversed |
| FIN-409-ALREADY-REVERSED | RULE-FIN-013 | API-FIN-021, 014 | 409 | reversing an entry that already carries a reversal link | تم عكس هذا القيد بالفعل ولا يمكن عكسه مرة أخرى | This entry has already been reversed and cannot be reversed again |
| FIN-404-ENTRY | PLATFORM-STD | API-FIN-021, 022 | 404 | unknown entry id | القيد غير موجود | Entry not found |
| FIN-409-YEAR-DUP | PLATFORM-STD | API-FIN-023 | 409 | duplicate fiscal year code | رمز السنة المالية مستخدم بالفعل | Fiscal year code already in use |
| FIN-409-NOT-REOPENABLE | RULE-FIN-014 | API-FIN-024, 026 | 409 | period Hard Closed | الفترة مغلقة إغلاقًا صارمًا ولا يمكن إعادة فتحها | The period is hard-closed and cannot be reopened |
| FIN-409-INVALID-TRANSITION | PLATFORM-STD | API-FIN-025, 027 | 409 | period not in the expected state; fiscal year already CLOSED when year-end close is re-run | لا يمكن تنفيذ هذا الانتقال من الحالة الحالية | This transition is not allowed from the current status |
| ~~FIN-403-SOD-VIOLATION~~ STRUCK | RULE-FIN-015 | — | — | UNREACHABLE since 2026-09-12 — both throw sites are gone. It was raised when the close-approver's permission set was not globally disjoint from the entry-creation permission's: `FinSeparationOfDutiesService` read SEC's user directory (XM-FIN-002, now retired) and `FiscalPeriodDomain.assertCanHardClose(...)` threw. Both were DELETED by recorded human decision as an OVER-IMPLEMENTATION: they refused the close for EVERY caller whenever ANY single user in the system held both `PERM_FIN_PERIODS_CLOSE_APPROVE` and `PERM_FIN_JOURNAL_ENTRIES_CREATE`, and no REQ, AC or RULE states that. RULE-FIN-015 (srs-fin.md:1026-1033) asks only for a DISTINCT permission "enforced through the Security module" and its `Data source` line reads DEFERRED — no FIN-side fact to read — so the rule is satisfied in full by the delivered `@PreAuthorize(PERM_FIN_PERIODS_CLOSE_APPROVE)` gate on API-FIN-026 and API-FIN-027, which is untouched. STRUCK RATHER THAN DELETED, following the never-declared-service-unavailable row's precedent in this same table (an unreachable code is struck, kept visible, and given no API/HTTP cells). ONE HONEST DIFFERENCE FROM THAT PRECEDENT: that other row's code never existed in code at all, whereas the constant `FinErrorCodes.FIN_403_SOD_VIOLATION` and BOTH bundle entries (`messages.properties`, `messages_ar.properties`) were deliberately left in place by the code session, so the string is still in the build with no thrower — the message columns below are therefore the real, still-present bundle text, not a historical quotation. A session that removes the constant and the two bundle keys should delete this row's message cells with them | صلاحية اعتماد الإغلاق منفصلة عن صلاحية إنشاء القيود | The close-approval permission is separate from the entry-creation permission |
| FIN-404-PERIOD | PLATFORM-STD | API-FIN-014, 017, 019, 024, 025, 026, 027, 029, 031 | 404 | unknown period id — on API-FIN-029 only when the OPTIONAL `periodId` is actually supplied (omitting it stays a 200); API-FIN-031's `fromPeriodId`/`toPeriodId` raise it when supplied and unknown. The sites were verified in source and are NOT all "unknown id" failures: API-FIN-024/025/026 resolve the path id (FiscalPeriodService.java:204); API-FIN-019 resolves the submitted `periodId` (JournalEntryService.java:174); API-FIN-014 and 017 carry no period id and raise it when NO period's `[startDate, endDate]` covers the run date (JournalPostingService.java:208); API-FIN-014 also raises it when a REVERSING template's period has no successor inside the same fiscal year (RecurringTemplateService.java:284); API-FIN-027 raises it when the closing year has no last period or the successor year no first period (FiscalYearService.java:314, :322). v2: API-FIN-020 no longer raises it — an event date no period covers is FIN-422-NO-DEFINED-PERIOD (RULE-FIN-028, ADR-FIN-030) | الفترة غير موجودة | Period not found |
| FIN-409-PERIODS-NOT-CLOSED | PLATFORM-STD (§10.4 precondition) | API-FIN-027 | 409 | not every period Hard Closed | يجب إغلاق كل الفترات إغلاقًا صارمًا أولًا | Every period must be hard-closed first |
| FIN-404-YEAR | PLATFORM-STD | API-FIN-014, 017, 019, 020, 021, 027, 030, 031 | 404 | unknown fiscal year id — on API-FIN-030 and API-FIN-031 the REQUIRED `fiscalYearId` is resolved first and raises this instead of the silent all-zero 200 those reports previously returned. The five ids added here were verified in source: API-FIN-019 resolves the submitted `fiscalYearId` under the docNo row lock (JournalEntryService.java:171), and every system-generated posting re-resolves the year under that same lock inside JournalPostingService.buildValidateAndPost (:152) — the single pipeline behind API-FIN-014, 017, 020, 021 and 027. API-FIN-027 raises it from two further sites of its own: an absent adjacent successor year (FiscalYearService.java:364) and an unknown path id (:371). API-FIN-023 does NOT raise it — FiscalYearService.create resolves no year | السنة المالية غير موجودة | Fiscal year not found |
| FIN-422-REMAINDER-MARKER | RULE-FIN-003, RULE-FIN-010 | API-FIN-011, 016, 017, 020 | 422 | `isRemainderFl` (DBF-FIN-107 on a rule line, DBF-FIN-146 on an allocation target — v1 cited DBF-FIN-103 and DBF-FIN-141, the amount-source and lineNo columns, corrected in v2) disagrees with the line's/target's own REMAINDER type code, so which line is the remainder is ambiguous | علامة سطر الباقي لا تتفق مع نوع التوزيع أو مصدر المبلغ لنفس السطر | The remainder marker disagrees with the line's own distribution or amount-source type |
| FIN-422-REMAINDER-NOT-POSITIVE | RULE-FIN-010 | API-FIN-017, 020 | 422 | the remainder line's per-side difference is zero or negative — the other lines on its side already equal or exceed the opposing side (POL-FIN-005) | سطر الباقي يُحسب كفرق ويجب أن يكون موجبًا؛ السطور الأخرى تستهلك المبلغ بالكامل | The remainder line is computed as a difference and must be positive; the other lines already consume the full amount |
| FIN-400-PERIOD-NOT-IN-YEAR | RULE-FIN-017 | API-FIN-019 | 400 | submitted `periodId` belongs to a different fiscal year than the submitted `fiscalYearId` (DBF-FIN-076) | الفترة المحددة لا تتبع السنة المالية المحددة | The selected period does not belong to the selected fiscal year |
| FIN-400-DOCDATE-OUTSIDE-PERIOD | RULE-FIN-017 | API-FIN-019 | 400 | submitted `docDate` falls outside the submitted period's `[startDate, endDate]` span (DBF-FIN-080/081) | تاريخ المستند خارج نطاق الفترة المحددة | The document date falls outside the selected period |
| FIN-403-FORBIDDEN | PLATFORM-STD | every secured API (i.e. every `@PreAuthorize` on a `com.erp.fin.service.*` method — API-FIN-036 and API-FIN-037 included) | 403 | missing module/screen/action grant — **this code now REACHES THE WIRE as itself.** `com.erp.fin.security.FinForbiddenAdvisor` catches the `AccessDeniedException` raised by `@PreAuthorize` on any `com.erp.fin.service.*` method and re-raises it as `LocalizedException(Status.FORBIDDEN, FinErrorCodes.FIN_403_FORBIDDEN)`, which `GlobalExceptionHandler` renders like every other FIN error. It IS a `FinErrorCodes` constant (`FIN_403_FORBIDDEN`) and it IS in BOTH i18n bundles (`messages.properties`, `messages_ar.properties`). **Every earlier revision of this row said the opposite** — that FIN's 403 was rendered as the platform `ACCESS_DENIED` envelope, that this row was merely the catalog's *name* for that response, that the code was in no `FinErrorCodes` constant and in neither bundle, and that FIN had no `FIN-403-FORBIDDEN` on the wire. All of that is superseded and none of it is true any more. TWO BOUNDARIES, each read in source: (1) a denial raised by the Spring Security **filter chain**, before any FIN service is entered, never reaches an AOP proxy or `GlobalExceptionHandler` — it is written by the chain's `accessDeniedHandler` and still answers `SEC-403-FORBIDDEN`. That path is currently UNREACHABLE for FIN, because `SecurityConfig` (`com.erp.main.config`) authorizes with `.anyRequest().authenticated()` and declares no FIN authority rule, so every FIN permission denial is a `@PreAuthorize` denial on a FIN service and does pass through the advisor; adding a URL-level authority rule for a FIN path would change that. (2) `FIN-403-SOD-VIOLATION` is unaffected — it is a RULE-FIN-015 business failure thrown by hand as a `LocalizedException`, never an `AccessDeniedException`, so the advisor never sees it. Also note `CommonErrorCodes.ACCESS_DENIED` still exists and is still what `GlobalExceptionHandler.handleAccessDenied` emits for modules that have no forbidden advisor of their own; FIN is simply no longer one of them | لا تملك صلاحية المالية المطلوبة لهذه العملية | You do not hold the Finance permission required for this operation |
| FIN-400-INVALID-SORT | PLATFORM-STD | every search API | 400 | unrecognized sort field | حقل الترتيب غير معروف | Unrecognized sort field |
| ~~FIN service-unavailable (never declared)~~ STRUCK | PLATFORM-STD (MDL unreachable) | — | — | this row's code was originally shaped `{MOD}-{http}` with http 503 — never a declared platform status (`stack.backend.api.http_statuses` has no entry for it) and never a `FinErrorCodes` constant. Struck rather than spelled out here, so the row's own label no longer reads as a live catalog code. Genuinely unreachable: XM-FIN-001 is in-process `MdlLookupApi` injection, so there is no network hop to fail; an invalid code raises `FIN-400-INVALID-LOOKUP`, anything else falls through to `FIN-500` | — | — |
| FIN-500 | PLATFORM-STD (infrastructure) | any | 500 | unhandled server error | حدث خطأ في الخادم | A server error occurred |
| FIN-422-UNSUPPORTED-DERIVATION | RULE-FIN-018 | API-FIN-011 | 422 | ADDED v2 — a rule line's accountDerivationTypeCode is not an active ACCOUNT_DERIVATION_TYPE value (DIRECT included, retired in MDL) — AC-FIN-048 | نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط | Unsupported account derivation type; only constant or mapping is allowed |
| FIN-422-DERIVATION-SPEC | RULE-FIN-019 | API-FIN-011 | 422 | ADDED v2 — a CONSTANT line without an account code or with a business field, or a MAPPING line without a business field or with an account code | مواصفة اشتقاق الحساب لا تطابق نوعه | The account derivation spec does not match its type |
| FIN-422-UNMAPPED-VALUE | RULE-FIN-020 | API-FIN-020 | 422 | ADDED v2 — no ACTIVE account mapping for the rule's event type, the line's business field and the event's value; a configuration gap fixed by adding a mapping (AC-FIN-056, AC-FIN-057, AC-FIN-061) | لا يوجد ربط حساب نشط للقيمة التجارية في الحدث | No active account mapping exists for the business value in the event |
| FIN-422-MISSING-BUSINESS-FIELD | RULE-FIN-021 | API-FIN-020 | 422 | ADDED v2 — the event does not carry the business field a MAPPING line or a BUSINESS_FIELD tag names; a payload gap fixed at the producer (AC-FIN-053, AC-FIN-073) | الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة | The event does not carry the business field the rule requires |
| FIN-409-MAPPING-DUP | RULE-FIN-022 | API-FIN-039 | 409 | ADDED v2 — an active mapping already holds the (event type, business field, business value) key: by the pre-check QR-FIN-052, or by the store's refusal on UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY translated by FinPersistenceRefusalAdvisor (ADR-FIN-030) — AC-FIN-063 | يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها | An active mapping already exists for this event type, business field and value |
| FIN-422-MAPPED-ACCOUNT-INELIGIBLE | RULE-FIN-023 | API-FIN-039, 040 | 422 | ADDED v2 — the mapping's account is not a leaf or is inactive when the mapping is saved (AC-FIN-064) | الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا | The mapped account must be an active leaf account |
| FIN-422-INVALID-PAYMENT-METHOD | RULE-FIN-024 | API-FIN-039 | 422 | ADDED v2 — businessFieldCode is PAYMENT_METHOD and businessValue is not an active PAYMENT_METHOD code (AC-FIN-065) | القيمة ليست طريقة دفع معرَّفة | The value is not a defined payment method |
| FIN-422-DIMENSION-TAG-DUP | RULE-FIN-025 | API-FIN-011 | 422 | ADDED v2 — two tags of one submitted rule line name the same dimension (AC-FIN-069) | لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة | A rule line cannot tag the same dimension twice |
| FIN-422-DIMENSION-TAG-SOURCE | RULE-FIN-026 | API-FIN-011 | 422 | ADDED v2 — a CONSTANT tag without a value of its own dimension or with a business field, or a BUSINESS_FIELD tag without a business field or with a value (AC-FIN-070) | وسم البُعد لا يطابق مصدر قيمته | The dimension tag does not match its value source |
| FIN-422-UNRESOLVED-DIMENSION-VALUE | RULE-FIN-027 | API-FIN-020 | 422 | ADDED v2 — the event's value for a BUSINESS_FIELD tag equals the code of no dimension value of the tagged dimension; fixed by defining the value (AC-FIN-074) | قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد | The dimension value in the event matches no defined value of this dimension |
| FIN-422-NO-DEFINED-PERIOD | RULE-FIN-028 | API-FIN-020 | 422 | ADDED v2 — no fiscal period's [startDate, endDate] covers the event's docDate; a calendar-setup error, never the retryable period-not-open answer (AC-FIN-080) | لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث | No accounting period is defined for the event date |
| FIN-404-ACCOUNT-MAPPING | PLATFORM-STD (ADR-FIN-030) | API-FIN-040, 041 | 404 | ADDED v2 — unknown account-mapping id in the path | ربط الحساب غير موجود | Account mapping not found |

Every PLATFORM-STD row follows the platform's umbrella convention for infrastructure errors, which SEC established
at its own P3.1 and FIN cites without re-deriving. The v2 PLATFORM-STD row, FIN-404-ACCOUNT-MAPPING, is recorded in
ADR-FIN-030.

## QR id definitions — ADDED in v2
**QR-FIN-050** — FIND_BY_CRITERIA search account mappings, with the account's display columns [ENT-FIN-015, API-FIN-038]
**QR-FIN-051** — SAVE create account mapping [ENT-FIN-015, API-FIN-039]
**QR-FIN-052** — EXISTS an active mapping holds the key (RULE-FIN-022) [ENT-FIN-015, API-FIN-039]
**QR-FIN-053** — UPDATE change a mapping's account [ENT-FIN-015, API-FIN-040]
**QR-FIN-054** — UPDATE deactivate a mapping [ENT-FIN-015, API-FIN-041]
**QR-FIN-055** — FIND_ONE the active mapping for an event's business value (RULE-FIN-020) [ENT-FIN-015, API-FIN-020]
**QR-FIN-056** — SAVE a rule line's dimension tags [ENT-FIN-016, API-FIN-011]
**QR-FIN-057** — FIND_BY_CRITERIA the tags of the active rule's lines [ENT-FIN-016, API-FIN-020]
**QR-FIN-058** — FIND_ONE dimension value by code within a dimension (RULE-FIN-027) [ENT-FIN-003, API-FIN-020]
**QR-FIN-059** — FIND_ONE the fiscal period covering a date (RULE-FIN-028) [ENT-FIN-008, API-FIN-020]
**QR-FIN-060** — FIND_ONE the account a mapping names (RULE-FIN-023) [ENT-FIN-001, API-FIN-039, API-FIN-040]
**QR-FIN-061** — FIND_ONE a tag's dimension and CONSTANT value (RULE-FIN-026) [ENT-FIN-002, ENT-FIN-003, API-FIN-011]

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
**QR-FIN-015** — SAVE add rule line; v2 also writes accountBusinessFieldCode [ENT-FIN-010, API-FIN-011]
**QR-FIN-016** — EXISTS remainder-line/target count (RULE-FIN-003) [ENT-FIN-010, ENT-FIN-014, API-FIN-011, API-FIN-016]
**QR-FIN-017** — FIND_BY_CRITERIA search templates [ENT-FIN-011, API-FIN-012]
**QR-FIN-018** — SAVE create template with lines [ENT-FIN-011, ENT-FIN-012, API-FIN-013]
**QR-FIN-019** — FIND_ONE load the template to run, by id [ENT-FIN-011, API-FIN-014]
**QR-FIN-020** — FIND_BY_CRITERIA search allocation rules [ENT-FIN-013, API-FIN-015]
**QR-FIN-021** — SAVE create allocation rule with targets [ENT-FIN-013, ENT-FIN-014, API-FIN-016]
**QR-FIN-022** — FIND_ONE load allocation rule + source balance [ENT-FIN-013, API-FIN-017]
**QR-FIN-023** — FIND_BY_CRITERIA search journal entries [ENT-FIN-004, API-FIN-018]
**QR-FIN-024** — SAVE build manual entry (DRAFT) [ENT-FIN-004, ENT-FIN-005, ENT-FIN-006, API-FIN-019]
**QR-FIN-025** — SAVE build event entry (DRAFT) from the rule; v2 lines carry the constant or mapped account and their resolved dimensions [ENT-FIN-004, ENT-FIN-005, ENT-FIN-006, API-FIN-020]
**QR-FIN-026** — EXISTS duplicate eventReference (RULE-FIN-004); v2 the friendly pre-check, UQ_FIN_JOURNAL_ENTRY_EVENT_REF the guarantee [ENT-FIN-004, API-FIN-020]
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
**XM-FIN-001** — SOFT-READ every FIN lookup-backed column → MDL_LOOKUP_VALUE; MODIFIED v2: six more columns, 15 FIN-owned keys [REQ-FIN-001, REQ-FIN-007, REQ-FIN-008, REQ-FIN-010, REQ-FIN-014, REQ-FIN-018, REQ-FIN-022, REQ-FIN-025, REQ-FIN-031, REQ-FIN-045, REQ-FIN-048, REQ-FIN-049, REQ-FIN-054, REQ-FIN-061, REQ-FIN-062]
**~~XM-FIN-002~~** — RETIRED 2026-09-12, id burned and not reused. Was: READ SEC's user→permission directory (`SecUserDirectoryApi.findUserIdsHoldingPermission`) for a global user-set-disjointness check that RULE-FIN-015 does not require; retired when `FinSeparationOfDutiesService` was deleted [REQ-FIN-037, REQ-FIN-038]

## Registry content
See `registry-exec-be-fin.md`.
══════════════════════════════════════════════════════════════════
