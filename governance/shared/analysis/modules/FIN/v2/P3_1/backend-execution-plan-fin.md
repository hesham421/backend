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

<!-- PHASE:CORE:START traces=REQ-FIN-017,REQ-FIN-045,REQ-FIN-047,REQ-FIN-072,REQ-FIN-075 -->
## PHASE 1 — CORE

**Layers**: controller → service → mapper → domain → repository (`profile.stack.backend.layers`).
Domain-behaviour placement: **domain classes** — one dedicated `<Entity>Domain` per entity — for every rule that
answers "is this operation allowed?" (`profile.conventions.domain_behaviour_placement: domain_classes`). The service
layer orchestrates only (load → delegate → persist → return) and holds no business-rule conditional of its own. How
many entities a rule reads is not a placement criterion: the service fetches those facts and passes them to the
domain class as plain arguments. The decision sits in the domain class; the entity keeps the plain state mutation
(`JournalEntryDomain.assertCanPost(...)` then `JournalEntry.post()`).

FIN domain classes and the rules each owns — v1, then the v2 additions:
- `AccountDomain` — RULE-FIN-001, RULE-FIN-007
- `DimensionValueDomain` — RULE-FIN-002, RULE-FIN-009; v2: RULE-FIN-027 (`assertResolvedByCode`)
- `EventTypeRuleDomain` — RULE-FIN-003, RULE-FIN-005, RULE-FIN-010; v2: RULE-FIN-018, RULE-FIN-019, RULE-FIN-021,
  RULE-FIN-025, RULE-FIN-026 (the rule line, its tags, and the event's business fields the line reads)
- `JournalEntryDomain` — RULE-FIN-004, RULE-FIN-006, RULE-FIN-008, RULE-FIN-011, RULE-FIN-012, RULE-FIN-013,
  RULE-FIN-016, RULE-FIN-017
- `FiscalPeriodDomain` — RULE-FIN-014; v2: RULE-FIN-028 (`assertDateCovered`)
- `FiscalYearDomain` — the year-end re-run guard (API-FIN-027)
- `AllocationRuleDomain`, `RecurringTemplateDomain` — RULE-FIN-003 and RULE-FIN-010 for allocation targets; the
  `assertCanRun()` gate of 2026-09-12 (a recorded human decision, not a RULE)
- v2 `AccountMappingDomain` (NEW, ENT-FIN-015) — RULE-FIN-020 (`assertResolved`), RULE-FIN-022 (`assertKeyFree`),
  and also RULE-FIN-023 (`assertAccountEligible`) and RULE-FIN-024 (`assertBusinessValueDefined`)

RULE-FIN-015 has no domain owner. Its data source is DEFERRED, and it is satisfied entirely by SEC's permission
gate: `@PreAuthorize(PERM_FIN_PERIODS_CLOSE_APPROVE)` on API-FIN-026 and API-FIN-027.

**Error signalling**: `LocalizedException → {code, messageAr, messageEn}`. Runtime code format, verbatim from
profile.stack.backend.api.error_code_format: `{MOD}-{http}[-{SLUG}]`, which for this module reads
`FIN-{http}[-{SLUG}]`. Every Error Catalog row is an instance of it, and its HTTP cell is one of the statuses the
platform emits (200, 201, 400, 401, 403, 404, 409, 413, 415, 422, 500).

**Store refusals (v2, ADR-FIN-030)**: a service never catches `DataIntegrityViolationException`. Two v2 rules need a
uniqueness refusal answered as a business error rather than as a server error: RULE-FIN-004 on
UQ_FIN_JOURNAL_ENTRY_EVENT_REF and RULE-FIN-022 on UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY. FIN therefore gains one
module-wide `FinPersistenceRefusalAdvisor`, placed exactly like the delivered `FinForbiddenAdvisor`: one
`DefaultPointcutAdvisor` over `com.erp.fin.service.*`, ordered outside the transaction advisor so a refusal raised at
flush or at commit passes through it, never a catch inside a service and never a feature `@ControllerAdvice`. It
reads the violated constraint's name and re-raises `LocalizedException(CONFLICT, …)` from a fixed registry:
UQ_FIN_JOURNAL_ENTRY_EVENT_REF → `FIN-409-DUPLICATE-EVENT`, UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY →
`FIN-409-MAPPING-DUP`. Any other constraint name is re-thrown unchanged. The two inserts concerned are flushed at
once, so the refusal happens inside the service call.

**Transaction scope**: `READ_ONLY` for every `FIND_*`/`EXISTS`/`AGGREGATE` query; `READ_WRITE` for every
`SAVE`/`UPDATE`. The build → validate → post sequence runs in ONE transaction per entry: either the whole entry
posts or nothing is written. That holds for every v2 event rejection too (SRS A7): no row exists for a rejected event.
Adding a rule line writes the line and its tags in one transaction.

**Search contract**: `{filters, page, size, sort}` in the request body, `Page<T>` in `ApiResponse<T>`, an empty
result is success. Allowed sort fields are the screen's own filter and result columns (SRS §B2), never widened.
Paging: server-side, size 20 by default, 200 at most [KB:erp-domain-standards §6].

**Audit fields**: `createdBy/createdAt/updatedBy/updatedAt` are filled by the platform, never by a request. On
FIN_FISCAL_PERIOD, `closedBy`/`closedAt` record the hard-close approver (API-FIN-026).

**Type mapping** (postgresql16 → Java, from profile.stack.db.syntax_map; column types only — PK generation is not a
type):

| postgresql16 | Java | Notes |
|---|---|---|
| BIGINT | Long | every PK — a plain column filled from its table's own `SEQ_{TABLE}` sequence, no identity clause — and every FK |
| VARCHAR(n) | String | every lookup-backed column holds the MDL code |
| TEXT | String | descriptions, rule-line values |
| BOOLEAN | Boolean | every `…Fl` flag |
| TIMESTAMPTZ | Instant | audit and system timestamps, stored UTC |
| DATE | LocalDate | document and period dates |
| NUMERIC(18,4) | BigDecimal | money [KB:erp-domain-standards §6] |
| NUMERIC | Integer | line, period and sort numbers |

**Lookup values**: every lookup-backed column is a plain `String` holding the code, validated in the service layer
against MDL (XM-FIN-001) before any write. v2: 15 FIN-owned keys. Two keys have a v2 rule of their own instead of the
generic `FIN-400-INVALID-LOOKUP`: ACCOUNT_DERIVATION_TYPE (RULE-FIN-018) and PAYMENT_METHOD when it is an account
mapping's business value (RULE-FIN-024).

**Event business fields (v2, ADR-FIN-031)**: the canonical event names each business fact by its
FIN_EVENT_BUSINESS_FIELD code, as a key of the request's existing `fields` map (for example
`fields.PAYMENT_METHOD = "CHEQUE"`, `fields.BRANCH_CODE = "BR-07"`). The rules engine reads a business field only
through that code: a MAPPING rule line names it in accountBusinessFieldCode, and a BUSINESS_FIELD tag names it in
businessFieldCode. Nothing in the payload is read as an account code (REQ-FIN-047).

**Numbering**: `docNo` format is `JV-{fiscalYearCode}-{NNNNNN}` — literal prefix `JV-`, the owning fiscal year's
`code` (DBF-FIN-066, VARCHAR(10)), `-`, then a zero-padded 6-digit counter starting at `000001`; e.g.
`JV-2026-000123`, at most 20 characters inside `doc_no VARCHAR(30)`. The counter is scoped per `fiscalYearId` and
restarts each year; one counter serves every journal type. `UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO (fiscal_year_id, doc_no)`
backs it in the store. A FIN-local generator in `com.erp.fin` produces it, deliberately not a shared `com.erp.common`
component. This is a recorded deviation from the profile's guidance that document numbers come from the platform
numbering engine: no such engine exists in this platform. The number is assigned once on create, never appears in a
request, and is present in every response. v2 adds no business code.

**Workflow engine**: forbidden — every lifecycle (JournalEntry, FiscalPeriod, FiscalYear) is a plain guarded
transition.

**Languages**: every name field and every catalog message exists in ar and en. v2 adds no stored name: an account
mapping is labelled by its account's `nameAr`/`nameEn`.

**Cross-module contract placement**: XM-FIN-001 (SOFT-READ → MDL) is FIN's service layer injecting MDL's published
in-process interface `com.erp.mdl.crossmodule.MdlLookupApi` and calling `readActiveValuesByKey(typeKey)`; it
consumes `LookupOptionView` (code, labelAr, labelEn, sortOrder), never an MDL entity. One deployable
(`profile.conventions.module_interface: in_process`): no HTTP client, no base path, no network failure path. FIN's
dependency on SEC for identity and authorization is the platform interceptor that SEC's and MDL's own plans declare,
cited here and not redeclared (ADR-FIN-001).

**Cross-cutting authorization**: every secured endpoint is gated before its method body runs by a `@PreAuthorize`
naming one `PERM_<PAGE_CODE>_<ACTION>` from `PermissionConstants`. A denial is re-raised as `FIN-403-FORBIDDEN` by
`FinForbiddenAdvisor`. RULE-FIN-015 is satisfied by the distinct `PERM_FIN_PERIODS_CLOSE_APPROVE` gate alone. The
earlier sentence here, which specified a FIN service-layer check that no single user holds both close-approval and
entry creation, was the origin of the SoD over-implementation removed on 2026-09-12; it is withdrawn and must not be
reinstated (SEC-BE).
<!-- PHASE:CORE:END -->

<!-- PHASE:DATA-DOM:START traces=REQ-FIN-001,REQ-FIN-010,REQ-FIN-014,REQ-FIN-031,REQ-FIN-049,REQ-FIN-054,REQ-FIN-062,REQ-FIN-070 -->
## PHASE 2 — DATA-DOM

Sixteen entities, grouped by role: `SUB:DATA-DOM-MASTER` (Account, FiscalYear, FiscalPeriod),
`SUB:DATA-DOM-TRANSACTIONAL` (JournalEntry, JournalLine, JournalLineDimension — the posting core) and
`SUB:DATA-DOM-LOOKUP` (FIN's own config and lookup-kind structural data, the two v2 entities among them). Every PK
is a plain BIGINT filled by the application from its table's own sequence (db-script header; profile
stack.db.pk_generation = `sequence`); v1's blocks said `GENERATED ALWAYS AS IDENTITY`, which migration V22 never built
and which the v2 db-script corrects — the bindings below name the sequence (ADR-FIN-029). Full RULE text for v1
rules is single-sourced in the SRS §A5 and in the Error Catalog; every v2 rule is stated in full in the block that
owns it and again in the endpoint that enforces it.

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

<!-- SUB:DATA-DOM-LOOKUP:START traces=REQ-FIN-004,REQ-FIN-007,REQ-FIN-022,REQ-FIN-025,REQ-FIN-049,REQ-FIN-054,REQ-FIN-062 -->
### SUB — DATA-DOM-LOOKUP (FIN's own config and lookup-kind structural data)

#### ENT-FIN-002 — Dimension      kind: config
BINDINGS       table `FIN_DIMENSION` · PK `dimensionPk` (DBF-FIN-014) · PK generation `sequence` → `SEQ_FIN_DIMENSION` (V22) · db-script v2
FIELDS         DBF-FIN-014..022 · DTO: create `{code, nameAr, nameEn}`; no update and, by decision, no deactivate (SRS SCR-REQ-FIN-002 §B4)
DOMAIN RULES   none scoped alone; code unique by UQ_FIN_DIMENSION_CODE (QR-FIN-046)
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → QR-FIN-007, QR-FIN-008, QR-FIN-046; v2 QR-FIN-061 (a tag's dimension)

#### ENT-FIN-003 — DimensionValue      kind: lookup
BINDINGS       table `FIN_DIMENSION_VALUE` · PK `dimensionValuePk` (DBF-FIN-023) · PK generation `sequence` → `SEQ_FIN_DIMENSION_VALUE` (V22) · db-script v2
FIELDS         DBF-FIN-023..033 — the profile's lookup defaults (code, nameAr, nameEn, sortOrder, isActiveFl) plus PK, FK and audit
DOMAIN RULES
- RULE-FIN-002 (SRS §A5) — QR-FIN-010 · UQ_FIN_DIMENSION_VALUE_DIM_CODE
- RULE-FIN-027 (v2) — trigger: on build (event-sourced entry, per BUSINESS_FIELD dimension tag). Statement: the system shall reject the event with the unresolvable-dimension-value error when the event's value for a tag's business field equals the code of no dimension value of the tagged dimension. Message: ar "قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد" · en "The dimension value in the event matches no defined value of this dimension". Resolution is by exact code within the dimension (ADR-FIN-026), served by UQ_FIN_DIMENSION_VALUE_DIM_CODE · application layer (QR-FIN-058) · owner `DimensionValueDomain.assertResolvedByCode`. A match that is inactive is RULE-FIN-009's rejection, not this one
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-009, QR-FIN-010, QR-FIN-011; v2 QR-FIN-058, QR-FIN-061

#### ENT-FIN-009 — EventTypeRule      kind: config
BINDINGS       table `FIN_EVENT_TYPE_RULE` · PK `eventTypeRulePk` (DBF-FIN-089) · PK generation `sequence` → `SEQ_FIN_EVENT_TYPE_RULE` (V22) · db-script v2
FIELDS         DBF-FIN-089..097; eventTypeCode lookup-backed (XM-FIN-001). v2: its eventTypeCode is the rule's half of every account-mapping key (RULE-FIN-020)
DOMAIN RULES   one rule per event type — UQ_FIN_EVENT_TYPE_RULE_CODE and QR-FIN-014; RULE-FIN-005 (QR-FIN-027)
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-012, QR-FIN-013, QR-FIN-014, QR-FIN-027

#### ENT-FIN-010 — RuleLine      kind: config   (MODIFIED v2)
BINDINGS       table `FIN_RULE_LINE` · PK `ruleLinePk` (DBF-FIN-098) · PK generation `sequence` → `SEQ_FIN_RULE_LINE` (V22) · db-script v2
FIELDS         DBF-FIN-098..108 and DBF-FIN-148. v2: accountDerivationTypeCode (DBF-FIN-101) is CONSTANT or MAPPING; accountDerivationValue (DBF-FIN-102) is NULL-able and holds only the constant account code; accountBusinessFieldCode (DBF-FIN-148) names the business field a MAPPING line reads
DTO MEMBERSHIP create request (API-FIN-011): every business field plus `dimensionTags[]` (ENT-FIN-016); excluded {ruleLinePk, lineNo — assigned, createdAt}; no update, no by-id read, no delete (SRS SCR-REQ-FIN-003 §B4); the response carries the line with its tags
LOOKUP FIELDS  accountDerivationTypeCode → `ACCOUNT_DERIVATION_TYPE`; accountBusinessFieldCode → `FIN_EVENT_BUSINESS_FIELD`; amountSourceTypeCode → `AMOUNT_SOURCE_TYPE`; directionCode → `DEBIT_CREDIT`; distributionTypeCode → `DISTRIBUTION_TYPE`
DOMAIN RULES
- RULE-FIN-003 (SRS §A5) — exactly one remainder line once the set is a compound or percentage distribution; `isRemainderFl` (DBF-FIN-107) is the single marker and must agree with the line's own REMAINDER type code · QR-FIN-016 · `EventTypeRuleDomain`
- RULE-FIN-018 (v2) — trigger: on create (rule line). Statement: the system shall reject a rule line whose account derivation type is not an active ACCOUNT_DERIVATION_TYPE value, so that CONSTANT and MAPPING are the only accepted derivations and no derivation reads an account code from the event. Message: ar "نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط" · en "Unsupported account derivation type; only constant or mapping is allowed". Scope CREATE · application layer, membership in MDL's active values (XM-FIN-001) · `EventTypeRuleDomain.assertDerivationSupported`
- RULE-FIN-019 (v2) — trigger: on create (rule line). Statement: the system shall require a CONSTANT rule line to carry an account code in accountDerivationValue and no business field, and a MAPPING rule line to carry a business field in accountBusinessFieldCode and no account code. Message: ar "مواصفة اشتقاق الحساب لا تطابق نوعه" · en "The account derivation spec does not match its type". Scope CREATE · DB: CHK_FIN_RULE_LINE_DERIVATION_SPEC (the exclusive-or), application: which half the type needs · `EventTypeRuleDomain.assertDerivationSpec`
- RULE-FIN-021 (v2) — the MAPPING half, checked when an event is built (full text in ENT-FIN-016)
STATE MACHINE  none — the line lives and retires with its rule
CROSS-MODULE   DBF-FIN-101, DBF-FIN-103, DBF-FIN-105, DBF-FIN-106 and v2 DBF-FIN-148 touch XM-FIN-001
OPERATIONS     CREATE
REPOSITORY OPS → QR-FIN-015 (MODIFIED), QR-FIN-016; read by QR-FIN-025 and QR-FIN-028 at build time

#### ENT-FIN-011 — RecurringTemplate      kind: config
BINDINGS       table `FIN_RECURRING_TEMPLATE` · PK `recurringTemplatePk` (DBF-FIN-109) · PK generation `sequence` → `SEQ_FIN_RECURRING_TEMPLATE` (V22) · db-script v2
FIELDS         DBF-FIN-109..121; scheduleTypeCode and frequencyCode lookup-backed (XM-FIN-001)
DOMAIN RULES   none scoped alone; its run posts through the shared pipeline (RULE-FIN-006 … RULE-FIN-010, cited on API-FIN-014) and `RecurringTemplateDomain.assertCanRun()` refuses a deactivated template (recorded human decision, not a RULE)
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-017, QR-FIN-018, QR-FIN-019

#### ENT-FIN-012 — RecurringTemplateLine      kind: config
BINDINGS       table `FIN_RECURRING_TEMPLATE_LINE` · PK `recurringTemplateLinePk` (DBF-FIN-122) · PK generation `sequence` → `SEQ_FIN_RECURRING_TEMPLATE_LINE` (V22) · db-script v2
FIELDS         DBF-FIN-122..129; directionCode lookup-backed (XM-FIN-001); amount positive by CHK_FIN_RECURRING_TPL_LINE_AMOUNT_POS
DOMAIN RULES   none scoped alone
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → written by QR-FIN-018; read by QR-FIN-019 at run time

#### ENT-FIN-013 — AllocationRule      kind: config
BINDINGS       table `FIN_ALLOCATION_RULE` · PK `allocationRulePk` (DBF-FIN-130) · PK generation `sequence` → `SEQ_FIN_ALLOCATION_RULE` (V22) · db-script v2
FIELDS         DBF-FIN-130..138
DOMAIN RULES   none scoped alone; `AllocationRuleDomain.assertCanRun()` refuses a deactivated rule (recorded human decision, not a RULE)
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-020, QR-FIN-021, QR-FIN-022

#### ENT-FIN-014 — AllocationTarget      kind: config
BINDINGS       table `FIN_ALLOCATION_TARGET` · PK `allocationTargetPk` (DBF-FIN-139) · PK generation `sequence` → `SEQ_FIN_ALLOCATION_TARGET` (V22) · db-script v2
FIELDS         DBF-FIN-139..146; distributionTypeCode lookup-backed (XM-FIN-001)
DOMAIN RULES
- RULE-FIN-003 (SRS §A5, reused) — exactly one remainder target; `isRemainderFl` (DBF-FIN-146 — v1 cited DBF-FIN-141, the lineNo column, a slip corrected here) is the single marker the API-FIN-017 builder reads · QR-FIN-016 · `AllocationRuleDomain`
OPERATIONS     VIEW · CREATE
REPOSITORY OPS → written by QR-FIN-021; read by QR-FIN-022 at run time

#### ENT-FIN-015 — AccountMapping      kind: config   (ADDED v2)
BINDINGS       table `FIN_ACCOUNT_MAPPING` · PK `accountMappingPk` (DBF-FIN-149) · PK generation `sequence` → `SEQ_FIN_ACCOUNT_MAPPING` (db-script v2 BLOCK 1) · db-script v2
BUSINESS CODE  none — a mapping is identified by its key (eventTypeCode, businessFieldCode, businessValue); nothing outside the system quotes it
DEFAULT FIELDS config defaults adapted (SRS A3): `key` → the three-column natural key, unique among ACTIVE rows; `valueAr`/`valueEn` → not stored, the label is the referenced account's nameAr/nameEn; `isActiveFl` kept; four audit fields
FIELDS         DBF-FIN-149..158 — see the DB Alignment Manifest
DTO MEMBERSHIP create `{eventTypeCode, businessFieldCode, businessValue, accountId}`; update `{accountId}` only — the key is fixed after create (ADR-FIN-024); excluded everywhere {accountMappingPk, isActiveFl, audit}; the response carries every field plus the account's code, nameAr and nameEn (QR-FIN-050)
LOOKUP FIELDS  eventTypeCode → `ACCOUNTING_EVENT_TYPE`; businessFieldCode → `FIN_EVENT_BUSINESS_FIELD`; businessValue → `PAYMENT_METHOD` only when businessFieldCode = PAYMENT_METHOD, otherwise the host's code stored as-is — every one a stored code, never a numeric FK
DOMAIN RULES
- RULE-FIN-020 — trigger: on build (event-sourced entry, per MAPPING rule line). Statement: the system shall reject the event with the dedicated unmapped-value error when no active account mapping exists for the rule's event type, the rule line's business field and the event's value for that field; it never substitutes a default account. Message: ar "لا يوجد ربط حساب نشط للقيمة التجارية في الحدث" · en "No active account mapping exists for the business value in the event". Scope build · application layer (QR-FIN-055) · `AccountMappingDomain.assertResolved`. An inactive mapping counts as none
- RULE-FIN-022 — trigger: on create (account mapping). Statement: the system shall reject an account mapping whose event type, business field and business value already have an active mapping. Message: ar "يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها" · en "An active mapping already exists for this event type, business field and value". Scope CREATE · DB: partial unique index UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY; application: QR-FIN-052 · `AccountMappingDomain.assertKeyFree`
- RULE-FIN-023 — trigger: on create / update (account mapping). Statement: the system shall reject an account mapping whose account is not a leaf account or is inactive. Message: ar "الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا" · en "The mapped account must be an active leaf account". Scope CREATE, UPDATE · application layer (QR-FIN-060) · `AccountMappingDomain.assertAccountEligible`. Does not replace RULE-FIN-007 at post time
- RULE-FIN-024 — trigger: on create (account mapping). Statement: the system shall reject an account mapping for the PAYMENT_METHOD business field whose business value is not an active PAYMENT_METHOD code. Message: ar "القيمة ليست طريقة دفع معرَّفة" · en "The value is not a defined payment method". Scope CREATE · application layer, membership in MDL's active PAYMENT_METHOD values (XM-FIN-001) · `AccountMappingDomain.assertBusinessValueDefined`
STATE MACHINE  `isActiveFl` binary; soft deactivation, no reactivation — a deactivated key is restored by creating a new mapping (ADR-FIN-024)
CROSS-MODULE   eventTypeCode, businessFieldCode and (conditionally) businessValue touch XM-FIN-001
OPERATIONS     VIEW · CREATE · UPDATE
REPOSITORY OPS → QR-FIN-050, QR-FIN-051, QR-FIN-052, QR-FIN-053, QR-FIN-054, QR-FIN-055

#### ENT-FIN-016 — RuleLineDimension      kind: config   (ADDED v2)
BINDINGS       table `FIN_RULE_LINE_DIM` · PK `ruleLineDimensionPk` (DBF-FIN-159) · PK generation `sequence` → `SEQ_FIN_RULE_LINE_DIM` (db-script v2 BLOCK 1) · db-script v2
BUSINESS CODE  none
DEFAULT FIELDS config defaults adapted (SRS A3): `key` → (ruleLineId, dimensionId), one tag per dimension per line; no stored names (labels come from ENT-FIN-002 / ENT-FIN-003); no `isActiveFl` and no update audit — a tag lives and retires with its rule line (ADR-FIN-025); `createdAt` only
FIELDS         DBF-FIN-159..165 — see the DB Alignment Manifest
DTO MEMBERSHIP submitted only inside API-FIN-011 as `dimensionTags[]`: {dimensionId, valueSourceCode, dimensionValueId (CONSTANT), businessFieldCode (BUSINESS_FIELD)}; excluded {ruleLineDimensionPk, ruleLineId, createdAt}; no endpoint of its own
LOOKUP FIELDS  valueSourceCode → `FIN_DIMENSION_VALUE_SOURCE`; businessFieldCode → `FIN_EVENT_BUSINESS_FIELD`
DOMAIN RULES
- RULE-FIN-025 — trigger: on create (rule line with its dimension tags). Statement: the system shall reject a rule line that carries more than one dimension tag for the same dimension. Message: ar "لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة" · en "A rule line cannot tag the same dimension twice". Scope CREATE · DB: UQ_FIN_RULE_LINE_DIM_LINE_DIM; application: over the submitted list · `EventTypeRuleDomain.assertTagsDistinct`
- RULE-FIN-026 — trigger: on create (rule line with its dimension tags). Statement: the system shall require a CONSTANT dimension tag to carry a dimension value that belongs to the tagged dimension and no business field, and a BUSINESS_FIELD dimension tag to carry a business field and no dimension value. Message: ar "وسم البُعد لا يطابق مصدر قيمته" · en "The dimension tag does not match its value source". Scope CREATE · DB: CHK_FIN_RULE_LINE_DIM_VALUE_SPEC (the exclusive-or); application: which half the source needs, and that a CONSTANT value belongs to the dimension (QR-FIN-061) · `EventTypeRuleDomain.assertTagMatchesSource`
- RULE-FIN-021 — trigger: on build (event-sourced entry, per MAPPING rule line and per BUSINESS_FIELD dimension tag). Statement: the system shall reject the event with the missing-business-field error when the event does not carry the business field named by a MAPPING rule line or by a BUSINESS_FIELD dimension tag. Message: ar "الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة" · en "The event does not carry the business field the rule requires". Scope build · application layer (reads DBF-FIN-148 or DBF-FIN-161 against the event's `fields`) · `EventTypeRuleDomain.assertFieldPresent`
- RULE-FIN-027 — resolution of a BUSINESS_FIELD tag's value (full text in ENT-FIN-003)
STATE MACHINE  none
CROSS-MODULE   valueSourceCode and businessFieldCode touch XM-FIN-001
OPERATIONS     CREATE
REPOSITORY OPS → QR-FIN-056, QR-FIN-057, QR-FIN-061
<!-- SUB:DATA-DOM-LOOKUP:END -->
<!-- PHASE:DATA-DOM:END -->

<!-- PHASE:SVC-API:START traces=REQ-FIN-001,REQ-FIN-010,REQ-FIN-014,REQ-FIN-017,REQ-FIN-028,REQ-FIN-036,REQ-FIN-050,REQ-FIN-054,REQ-FIN-062,REQ-FIN-072 -->
## PHASE 3 — SVC-API

API count = 41 (≥ 8) → split, grouped SEARCH / CRUD / INT. Every block names its entity and operation on its
`Entity` line, the columns it writes on its `Request` and `Orchestration` lines, and what stops two simultaneous
calls on its `Concurrency` line. For the 35 unchanged v1 endpoints those three lines are the v2 addition
(ADR-FIN-029); the rest of each block is v1 as delivered. API-FIN-011 and API-FIN-020 are rewritten, and
API-FIN-038 … API-FIN-041 are new.

<!-- SUB:SVC-API-SEARCH:START traces=REQ-FIN-001,REQ-FIN-004,REQ-FIN-007,REQ-FIN-022,REQ-FIN-027,REQ-FIN-031,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-055,REQ-FIN-071 -->
### SUB — SVC-API-SEARCH (read-only)

<!-- API:API-FIN-001:START traces=REQ-FIN-001,DBF-FIN-002,DBF-FIN-003,DBF-FIN-004,DBF-FIN-005 -->
### API-FIN-001 — search accounts
Entity       : ENT-FIN-001 · operation VIEW (search)
Endpoint: POST /api/v1/fin/accounts/search · Layers: `AccountController.search`→`AccountService.search`
Request: body `AccountSearchRequest` — `code`(LIKE), `nameAr/nameEn`(LIKE), `accountTypeCode`(EXACT), `isActiveFl`(EXACT), paging
Response: 200 · `Page<AccountResponse>` · `ApiResponse<...>`
Validations: none (read-only) · Errors: `FIN-500`
Orchestration: load (QR-FIN-001) → map → return · Repository: QR-FIN-001 · join NONE · READ_ONLY
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_VIEW` · Localization: nameAr/nameEn returned
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-001:END -->

<!-- API:API-FIN-005:START traces=REQ-FIN-004,DBF-FIN-015,DBF-FIN-016,DBF-FIN-017 -->
### API-FIN-005 — search dimensions
Entity       : ENT-FIN-002 · operation VIEW (search)
Endpoint: POST /api/v1/fin/dimensions/search · Layers: `DimensionController.search`→`DimensionService.search`
Request: body `DimensionSearchRequest` — `code`(LIKE), paging · Response: 200 · `Page<DimensionResponse>`
Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-007 → map → return · Repository: QR-FIN-007 · join NONE · READ_ONLY
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-005:END -->

<!-- API:API-FIN-008:START traces=REQ-FIN-005,DBF-FIN-024,DBF-FIN-025,DBF-FIN-026,DBF-FIN-027 -->
### API-FIN-008 — search dimension values
Entity       : ENT-FIN-003 · operation VIEW (search)
Endpoint: POST /api/v1/fin/dimensions/values/search · Layers: `DimensionController.searchDimensionValues`→`DimensionValueService.search`
Request: body `DimensionValueSearchRequest` — `dimensionId`(EXACT, required) carried in the body filters and read by the child parent-id extractor (never a path variable); `code`(LIKE), paging · Response: 200 · `Page<DimensionValueResponse>`
Validations: none · Errors: `FIN-404-DIMENSION`
Orchestration: QR-FIN-011 → map → return · Repository: QR-FIN-011 · join NONE · READ_ONLY
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-008:END -->

<!-- API:API-FIN-009:START traces=REQ-FIN-007,DBF-FIN-090,DBF-FIN-091,DBF-FIN-092 -->
### API-FIN-009 — search event-type rules
Entity       : ENT-FIN-009 · operation VIEW (search)
Endpoint: POST /api/v1/fin/event-rules/search · Layers: `EventTypeRuleController.search`→`EventTypeRuleService.search`
Request: body `EventTypeRuleSearchRequest` — `eventTypeCode`(EXACT), `isActiveFl`(EXACT), paging · Response: 200 · `Page<EventTypeRuleResponse>`
Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-012 → map → return · Repository: QR-FIN-012 · join NONE · READ_ONLY
Security: screen FIN_RULES · `PERM_FIN_RULES_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-009:END -->

<!-- API:API-FIN-012:START traces=REQ-FIN-022,DBF-FIN-110,DBF-FIN-111,DBF-FIN-112 -->
### API-FIN-012 — search templates
Entity       : ENT-FIN-011, ENT-FIN-012 · operation VIEW (search — each template is returned with its lines)
Endpoint: POST /api/v1/fin/recurring-templates/search · Layers: `RecurringTemplateController.search`→`RecurringTemplateService.search`
Request: body `RecurringTemplateSearchRequest` — `nameAr/nameEn`(LIKE), `scheduleTypeCode`(EXACT), `isActiveFl`(EXACT), paging
Response: 200 · `Page<RecurringTemplateResponse>` · Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-017 → map → return · Repository: QR-FIN-017 · join NONE · READ_ONLY
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-012:END -->

<!-- API:API-FIN-015:START traces=REQ-FIN-025,DBF-FIN-131,DBF-FIN-132,DBF-FIN-133 -->
### API-FIN-015 — search allocation rules
Entity       : ENT-FIN-013, ENT-FIN-014 · operation VIEW (search — each rule is returned with its targets)
Endpoint: POST /api/v1/fin/allocation-rules/search · Layers: `AllocationRuleController.search`→`AllocationRuleService.search`
Request: body `AllocationRuleSearchRequest` — `nameAr/nameEn`(LIKE), `sourceAccountId`(EXACT), `isActiveFl`(EXACT), paging
Response: 200 · `Page<AllocationRuleResponse>` · Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-020 → map → return · Repository: QR-FIN-020 · join NONE · READ_ONLY
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-015:END -->

<!-- API:API-FIN-018:START traces=REQ-FIN-027,DBF-FIN-035,DBF-FIN-036,DBF-FIN-040 -->
### API-FIN-018 — search journal entries
Entity       : ENT-FIN-004 · operation VIEW (search)
Endpoint: POST /api/v1/fin/journal-entries/search · Layers: `JournalEntryController.search`→`JournalEntryService.search`
Request: body `JournalEntrySearchRequest` — `docNo`(LIKE), `docDate`(DATE_RANGE), `periodId`(EXACT), `statusCode`(EXACT), `journalTypeCode`(EXACT), paging
Response: 200 · `Page<JournalEntryResponse>` · Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-023 → map → return (REQ-FIN-027: unmodified) · Repository: QR-FIN-023 · join NONE · READ_ONLY
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_VIEW` · Localization: descriptionAr/En
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-018:END -->

<!-- API:API-FIN-022:START traces=REQ-FIN-016,REQ-FIN-027,DBF-FIN-034,DBF-FIN-041,REQ-FIN-070 -->
### API-FIN-022 — read entry
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 · operation VIEW (read one entry with its lines and their dimensions)
Endpoint: GET /api/v1/fin/journal-entries/{id} · Layers: `JournalEntryController.read`→`JournalEntryService.read`
Request: path `id` · Response: 200 · `JournalEntryResponse` with nested lines and their dimensions
Validations: none · Errors: `FIN-404-ENTRY`
Orchestration: QR-FIN-037 → map → return · Repository: QR-FIN-037 · join intra-module (entry→line→line-dim) · READ_ONLY
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_VIEW` · Localization: descriptionAr/En
Concurrency  : NONE — read-only; nothing is allocated or written
v2: an event-built line returns its dimension values as JournalLineDimension rows in exactly the form a manual line does (REQ-FIN-070, AC-FIN-075). Nothing changes in this endpoint, which already returns the rows it finds.
<!-- API:API-FIN-022:END -->

<!-- API:API-FIN-028:START traces=REQ-FIN-039,REQ-FIN-046,DBF-FIN-054,DBF-FIN-055,DBF-FIN-056,REQ-FIN-071 -->
### API-FIN-028 — account ledger
Entity       : ENT-FIN-005 · operation VIEW (read — account ledger report)
Endpoint: GET /api/v1/fin/reports/account-ledger · Layers: `ReportController.accountLedger`→`ReportService.accountLedger`
Request: `accountId`(EXACT), date range, dimension filters · Response: 200 · running-balance list, each row linking journalEntryId
Validations: none · Errors: `FIN-404-ACCOUNT`
Orchestration: (v2: event-built and manual lines are read alike — REQ-FIN-071) QR-FIN-042 (POSTED lines only, live) → compute running balance → return (POL-FIN-009)
Repository: QR-FIN-042 · join intra-module (line→entry for docDate/status) · READ_ONLY
Security: screen FIN_ACCOUNT_LEDGER · `PERM_FIN_ACCOUNT_LEDGER_VIEW` · Localization: n/a
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-028:END -->

<!-- API:API-FIN-029:START traces=REQ-FIN-040,REQ-FIN-046,DBF-FIN-005,DBF-FIN-006,DBF-FIN-055 -->
### API-FIN-029 — trial balance
Entity       : ENT-FIN-005 · operation VIEW (read — trial balance report)
Endpoint: GET /api/v1/fin/reports/trial-balance · Layers: `ReportController.trialBalance`→`ReportService.trialBalance`
Request: `periodId`(EXACT), `accountTypeCode`(EXACT) · Response: 200 · one row per account (debit/credit balance, sign per natureCode — POL-FIN-002)
Validations: RULE-FIN-006 restated as a report-level guarantee (POL-FIN-008: the sums always match because every contributing entry individually balanced — no separate check needed, an invariant by construction). `periodId` is an OPTIONAL narrowing, validated ONLY when supplied: omitting it means "no period narrowing" and stays a 200 (AC-FIN-040's happy path) — it is NOT mandatory
Errors: `FIN-404-PERIOD` (supplied `periodId` does not resolve), `FIN-500`
Orchestration: QR-FIN-043 (POSTED lines only, live, grouped by account) → apply nature sign → return
Repository: QR-FIN-043 · join intra-module · READ_ONLY
Security: screen FIN_TRIAL_BALANCE · `PERM_FIN_TRIAL_BALANCE_VIEW` · Localization: nameAr/nameEn per account
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-029:END -->

<!-- API:API-FIN-030:START traces=REQ-FIN-041,REQ-FIN-046,DBF-FIN-005,DBF-FIN-055 -->
### API-FIN-030 — balance sheet
Entity       : ENT-FIN-005 · operation VIEW (read — balance sheet report)
Endpoint: GET /api/v1/fin/reports/balance-sheet · Layers: `ReportController.balanceSheet`→`ReportService.balanceSheet`
Request: `fiscalYearId`(EXACT), `asOfDate` · Response: 200 · grouped ASSET/LIABILITY/EQUITY balances
Validations: none (continuity itself is guaranteed by REQ-FIN-036's opening-entry generation, not re-validated at read time). The REQUIRED `fiscalYearId` is resolved FIRST, before any aggregation
Errors: `FIN-404-YEAR` (unknown `fiscalYearId` — previously a silent 200 carrying an all-zero statement), `FIN-500`
Orchestration: QR-FIN-043 (accountTypeCode IN ASSET,LIABILITY,EQUITY) → group → return
Repository: QR-FIN-043 · join intra-module · READ_ONLY
Security: screen FIN_BALANCE_SHEET · `PERM_FIN_BALANCE_SHEET_VIEW` · Localization: nameAr/nameEn per account
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-030:END -->

<!-- API:API-FIN-031:START traces=REQ-FIN-042,REQ-FIN-046,DBF-FIN-005,DBF-FIN-055 -->
### API-FIN-031 — income statement
Entity       : ENT-FIN-005 · operation VIEW (read — income statement report)
Endpoint: GET /api/v1/fin/reports/income-statement · Layers: `ReportController.incomeStatement`→`ReportService.incomeStatement`
Request: `fiscalYearId`(EXACT), period range · Response: 200 · grouped REVENUE/EXPENSE balances
Validations: none (zero-opening is guaranteed by REQ-FIN-036 closing result accounts to Retained Earnings, not re-validated at read time). The REQUIRED `fiscalYearId` is resolved FIRST; the two OPTIONAL period bounds `fromPeriodId`/`toPeriodId` are validated only when supplied, as they already were
Errors: `FIN-404-YEAR` (unknown `fiscalYearId`), `FIN-404-PERIOD` (supplied period bound does not resolve — unchanged), `FIN-500`
Orchestration: QR-FIN-043 (accountTypeCode IN REVENUE,EXPENSE, scoped to the year/period range) → group → return
Repository: QR-FIN-043 · join intra-module · READ_ONLY
Security: screen FIN_INCOME_STATEMENT · `PERM_FIN_INCOME_STATEMENT_VIEW` · Localization: nameAr/nameEn per account
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-031:END -->

<!-- API:API-FIN-032:START traces=REQ-FIN-043,DBF-FIN-063,DBF-FIN-064,DBF-FIN-055,REQ-FIN-071 -->
### API-FIN-032 — dimension report
Entity       : ENT-FIN-005, ENT-FIN-006 · operation VIEW (read — dimension report)
Endpoint: GET /api/v1/fin/reports/dimension · Layers: `ReportController.dimensionReport`→`ReportService.dimensionReport`
Request: `dimensionId`(EXACT), `dimensionValueId`(EXACT), `periodId`(EXACT) · Response: 200 · one row per account+dimension-value combination (POL-FIN-011)
Validations: none · Errors: `FIN-404-DIMENSION`
Orchestration: (v2: event-built lines carry their JournalLineDimension rows in the v1 shape, so this grouping covers them unchanged — REQ-FIN-071, AC-FIN-076) QR-FIN-044 (group by account + dimension value, never base account alone) → return
Repository: QR-FIN-044 · join intra-module (line→line-dim→dimension-value) · READ_ONLY
Security: screen FIN_DIMENSION_REPORTS · `PERM_FIN_DIMENSION_REPORTS_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-032:END -->

<!-- API:API-FIN-033:START traces=REQ-FIN-031,DBF-FIN-076,DBF-FIN-077,DBF-FIN-080,DBF-FIN-081,DBF-FIN-082 -->
### API-FIN-033 — search fiscal periods
Entity       : ENT-FIN-008 · operation VIEW (search)
Endpoint: POST /api/v1/fin/fiscal-periods/search · Layers: `FiscalPeriodController.search`→`FiscalPeriodService.search`
Request: body `FiscalPeriodSearchRequest` — `fiscalYearId`(EXACT, **OPTIONAL**, DBF-FIN-076) carried in the body filters and read by the child parent-id extractor (never a path variable); `statusCode`(EXACT, DBF-FIN-082), paging/sort · Response: 200 · `Page<FiscalPeriodResponse>`
Validations: none (read-only) · Errors: `FIN-400-INVALID-SORT`, `FIN-500`
Orchestration: build the generic specification from the remaining filters → AND in an explicit join predicate on `fiscalYear.fiscalYearPk` ONLY when `fiscalYearId` is present → page → map → return
Repository: `FiscalPeriodRepository` via `JpaSpecificationExecutor.findAll(Specification, Pageable)` — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join intra-module (period→year, only when the parent filter is supplied) · READ_ONLY
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_VIEW` · Localization: nameAr/nameEn per period
Parent id OPTIONAL — deliberate divergence from API-FIN-008, which rejects a missing `dimensionId` with `FIN-404-DIMENSION`. SCR-REQ-FIN-007 §B2 makes both filters EXACT but neither mandatory, and the endpoint exists precisely so a client that did NOT create the fiscal year in the same session can discover a period id: requiring the year id first would leave that client with no way in. Same shape API-FIN-018 already uses for its optional `periodId`.
Why it exists: `JournalEntryCreateRequest` requires `fiscalYearId` + `periodId` (API-FIN-019) and API-FIN-029/030/031 require a period or year id, yet before this endpoint no API returned a fiscal period except API-FIN-023's create response. `PERM_FIN_PERIODS_VIEW` was already a V24 registry row and already granted by V25/V27, so **no migration was needed** — only the matching `PermissionConstants` constant was added.
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-033:END -->

<!-- API:API-FIN-038:START traces=REQ-FIN-055,DBF-FIN-150,DBF-FIN-151,DBF-FIN-152,DBF-FIN-153,DBF-FIN-154 -->
### API-FIN-038 — search account mappings
Entity       : ENT-FIN-015 · operation VIEW (search)
Endpoint     : POST /api/v1/fin/account-mappings/search   verb: POST — FIN's house style for a search with a request body (SRS SCR-REQ-FIN-013 §B5)
Layers       : `AccountMappingController.search` → `AccountMappingService.search`
Request      : body `AccountMappingSearchRequest` — filters eventTypeCode (EXACT), businessFieldCode (EXACT), businessValue (LIKE), accountId (EXACT), isActiveFl (EXACT), all optional; `page`, `size` (20 by default, 200 at most), `sort` limited to those five result columns (SRS §B2); nothing is written
Filters      : eventTypeCode → DBF-FIN-150 · businessFieldCode → DBF-FIN-151 · businessValue → DBF-FIN-152 · accountId → DBF-FIN-153 · isActiveFl → DBF-FIN-154
Response     : 200 · `ApiResponse<Page<AccountMappingResponse>>` — accountMappingPk, eventTypeCode, businessFieldCode, businessValue, accountId, accountCode, accountNameAr, accountNameEn, isActiveFl and the audit fields; an empty page is success, never "not found" (AC-FIN-059)
Validations  : the search contract only — a sort field outside the five → FIN-400-INVALID-SORT
Errors       : FIN-400-INVALID-SORT, FIN-403-FORBIDDEN, FIN-500
Orchestration: build the specification from the filters supplied → page (QR-FIN-050) → map; the account's code and names come from the joined account row (ADR-FIN-031) → return
Repository   : QR-FIN-050 · FIND_BY_CRITERIA · join FIN_ACCOUNT for the display columns (ADR-FIN-031) · READ_ONLY
Concurrency  : NONE — read-only
Security     : screen FIN_ACCOUNT_MAPPINGS · `PERM_FIN_ACCOUNT_MAPPINGS_VIEW` (the gateway), checked by `@PreAuthorize` before processing
Localization : the account's nameAr and nameEn are both returned; lookup values travel as codes and the frontend labels them from MDL
<!-- API:API-FIN-038:END -->

**404 on a keying id, as built (API-FIN-029/030/031).** FIN's house style already 404s on the id
that keys a report — API-FIN-028 answers `FIN-404-ACCOUNT`, API-FIN-032 answers
`FIN-404-DIMENSION`. API-FIN-030 and API-FIN-031 now do the same for their REQUIRED `fiscalYearId`
(`FIN-404-YEAR`), and API-FIN-029 for its `periodId` when — and only when — one is supplied
(`FIN-404-PERIOD`). API-FIN-031 was internally contradictory before: 404 on an unknown period bound
and 200 on an unknown year, in the same request. No new error code was introduced; both codes were
already registered in `FinErrorCodes` and both i18n bundles.
<!-- SUB:SVC-API-SEARCH:END -->

<!-- SUB:SVC-API-CRUD:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-014,REQ-FIN-022,REQ-FIN-025,REQ-FIN-048,REQ-FIN-049,REQ-FIN-054,REQ-FIN-056,REQ-FIN-057,REQ-FIN-062 -->
### SUB — SVC-API-CRUD

<!-- API:API-FIN-002:START traces=REQ-FIN-001,REQ-FIN-002,DBF-FIN-002,DBF-FIN-005,DBF-FIN-006,DBF-FIN-007,DBF-FIN-008 -->
### API-FIN-002 — create account
Entity       : ENT-FIN-001 · operation CREATE
Endpoint: POST /api/v1/fin/accounts · Layers: `AccountController.create`→`AccountService.create`
Request: (writes DBF-FIN-002 code · DBF-FIN-003 nameAr · DBF-FIN-004 nameEn · DBF-FIN-005 accountTypeCode · DBF-FIN-006 natureCode · DBF-FIN-007 parentAccountId · DBF-FIN-008 isLeafFl) `{code, nameAr, nameEn, accountTypeCode, natureCode, parentAccountId?, isLeafFl}`
Response: 201 · `AccountResponse`
Validations: RULE-FIN-001 (leaf requires no children — only relevant if `parentAccountId`
is later given children; at create this only matters when the same call sets a parent
that itself must remain non-leaf — QR-FIN-006 checks the *parent*, not the new row);
uniqueness of code (QR-FIN-005); accountTypeCode/natureCode validated via XM-FIN-001
Errors: `FIN-409-ACCOUNT-DUP`, `FIN-409-PARENT-NOT-LEAF-ELIGIBLE`, `FIN-400-INVALID-LOOKUP`
Orchestration: (sets DBF-FIN-009 isActiveFl=TRUE on insert; flips the parent's DBF-FIN-008 isLeafFl to FALSE) validate lookups (XM-FIN-001) → validate uniqueness (QR-FIN-005) → if
parentAccountId given, flip the parent's isLeafFl to false via RULE-FIN-001's inverse
effect (a parent gaining its first child can no longer itself be a leaf) → persist (QR-FIN-002) → return
Repository: QR-FIN-002, QR-FIN-005, QR-FIN-006 · join NONE · READ_WRITE
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_CREATE` · Localization: nameAr/nameEn required
Concurrency  : the pre-check is the friendly path; UQ_FIN_ACCOUNT_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_ACCOUNT_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-ACCOUNT-DUP — v1 behaviour as built, recorded here and not changed by v2. Creating a child also flips the named parent's isLeafFl without a lock, racing a concurrent API-FIN-003 that is deciding RULE-FIN-001 over the same child set; the pair can leave an account both leaf and parent, which RULE-FIN-007 would then accept as postable. v1 as built; stated here, not changed by v2 (ADR-FIN-042)
<!-- API:API-FIN-002:END -->

<!-- API:API-FIN-003:START traces=REQ-FIN-002,DBF-FIN-003,DBF-FIN-004,DBF-FIN-008 -->
### API-FIN-003 — update account
Entity       : ENT-FIN-001 · operation UPDATE
Endpoint: PUT /api/v1/fin/accounts/{id} · Layers: `AccountController.update`→`AccountService.update`
Request: (writes DBF-FIN-003 nameAr · DBF-FIN-004 nameEn · DBF-FIN-008 isLeafFl) `{nameAr, nameEn, isLeafFl}` — excludes {accountPk, code, accountTypeCode, natureCode, isActiveFl, audit}
Response: 200 · `AccountResponse`
Validations: RULE-FIN-002-equivalent — RULE-FIN-001 (full text: DATA-DOM §ENT-FIN-001) —
`isLeafFl=true` rejected if the account has any child (QR-FIN-006)
Errors: `FIN-409-HAS-CHILDREN`, `FIN-404-ACCOUNT`
Orchestration: load → check RULE-FIN-001 if isLeafFl changing to true (QR-FIN-006) → update (QR-FIN-003) → return
Repository: QR-FIN-003, QR-FIN-006 · join NONE · READ_WRITE
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_UPDATE` · Localization: both name fields updatable
Concurrency  : RULE-FIN-001 is decided on the child set QR-FIN-006 reads, which a concurrent API-FIN-002 naming this account as parent can change; no lock is specified (v1 as built) — recorded, not changed by v2. The same race can leave the account both leaf and parent, a state RULE-FIN-007 accepts as postable (ADR-FIN-042)
<!-- API:API-FIN-003:END -->

<!-- API:API-FIN-004:START traces=REQ-FIN-003,DBF-FIN-009 -->
### API-FIN-004 — deactivate account
Entity       : ENT-FIN-001 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/accounts/{id}/deactivate · Layers: `AccountController.deactivate`→`AccountService.deactivate`
Request: path `id`, no body · Response: 200 · `AccountResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-ACCOUNT`
Orchestration: (sets DBF-FIN-009 isActiveFl=FALSE) load → `Account.deactivate()` → persist (QR-FIN-004) → return (REQ-FIN-019
subsequently rejects any posting to it)
Repository: QR-FIN-004 · join NONE · READ_WRITE
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_UPDATE` · Localization: n/a
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-004:END -->

<!-- API:API-FIN-006:START traces=REQ-FIN-004,DBF-FIN-015,DBF-FIN-016,DBF-FIN-017 -->
### API-FIN-006 — create dimension
Entity       : ENT-FIN-002 · operation CREATE
Endpoint: POST /api/v1/fin/dimensions · Layers: `DimensionController.create`→`DimensionService.create`
Request: (writes DBF-FIN-015 code · DBF-FIN-016 nameAr · DBF-FIN-017 nameEn) `{code, nameAr, nameEn}` · Response: 201 · `DimensionResponse`
Validations: uniqueness of code (QR-FIN-046, DB `UQ_FIN_DIMENSION_CODE` friendly-checked at service layer)
Errors: `FIN-409-DIMENSION-DUP`
Orchestration: (sets DBF-FIN-018 isActiveFl=TRUE on insert) validate → persist (QR-FIN-008) → return · Repository: QR-FIN-008 · join NONE · READ_WRITE
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_CREATE` · Localization: nameAr/nameEn required
Concurrency  : the pre-check is the friendly path; UQ_FIN_DIMENSION_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_DIMENSION_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-DIMENSION-DUP — v1 behaviour as built, recorded here and not changed by v2
<!-- API:API-FIN-006:END -->

<!-- API:API-FIN-007:START traces=REQ-FIN-005,REQ-FIN-006,DBF-FIN-024,DBF-FIN-025,DBF-FIN-028 -->
### API-FIN-007 — create dimension value
Entity       : ENT-FIN-003 · operation CREATE
Endpoint: POST /api/v1/fin/dimensions/{id}/values · Layers: `DimensionController.createDimensionValue`→`DimensionValueService.create`
Request: (writes DBF-FIN-024 dimensionId from the path · DBF-FIN-025 code · DBF-FIN-026 nameAr · DBF-FIN-027 nameEn · DBF-FIN-028 sortOrder) path `id` (dimensionId); body `{code, nameAr, nameEn, sortOrder}`
Response: 201 · `DimensionValueResponse`
Validations: RULE-FIN-002 (full text: DATA-DOM §ENT-FIN-003) — code unique within the
dimension (QR-FIN-010)
Errors: `FIN-409-DIMVALUE-DUP`, `FIN-404-DIMENSION`
Orchestration: (sets DBF-FIN-029 isActiveFl=TRUE on insert) validate dimension exists → check RULE-FIN-002 (QR-FIN-010) → persist (QR-FIN-009) → return
Repository: QR-FIN-009, QR-FIN-010 · join NONE · READ_WRITE
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_CREATE` · Localization: nameAr/nameEn required
Concurrency  : the pre-check is the friendly path; UQ_FIN_DIMENSION_VALUE_DIM_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_DIMENSION_VALUE_DIM_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-DIMVALUE-DUP — v1 behaviour as built, recorded here and not changed by v2
<!-- API:API-FIN-007:END -->

<!-- API:API-FIN-035:START traces=REQ-FIN-005,REQ-FIN-021,DBF-FIN-029 -->
### API-FIN-035 — deactivate dimension value
Entity       : ENT-FIN-003 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/dimensions/values/{id}/deactivate · Layers: `DimensionController.deactivateDimensionValue`→`DimensionValueService.deactivate`
Request: path `id` (dimensionValuePk), no body · Response: 200 · `DimensionValueResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-DIMVALUE`
Orchestration: (sets DBF-FIN-029 isActiveFl=FALSE) load → `DimensionValue.deactivate()` (the entity's own helper, never a direct field assignment) → persist → return. Effect: `DimensionValueDomain.checkUsableOnLine` reads exactly this flag (DBF-FIN-029), so every posting path citing the value afterwards answers `FIN-409-INVALID-DIMENSION` (RULE-FIN-009 / REQ-FIN-021, via API-FIN-019, 020, 014, 017)
Repository: `DimensionValueRepository.findById` + `save` — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_UPDATE` · Localization: n/a
New in this delivery: `FIN-404-DIMVALUE` (`FinErrorCodes.FIN_404_DIMVALUE`, added to BOTH i18n bundles) and `PERM_FIN_DIMENSIONS_UPDATE`. This is the first UPDATE-class endpoint on FIN_DIMENSIONS, so migration `V28__fin_dimensions_update_action.sql` registers the `FIN_DIMENSIONS / UPDATE` action row AND explicitly grants it to `SYS_ADMIN` — V25 grants by a `SELECT` over the registry and has already run, so a later row would otherwise be registered-but-ungrantable.
Why it exists: RULE-FIN-009 / REQ-FIN-021 reject a journal line citing an INACTIVE dimension value and `DimensionValueDomain` implements that check, but nothing could set the flag false, so the branch was unreachable and untestable. Deliberately NOT built (a decision, not a backlog item): a deactivate on the PARENT `Dimension` — no REQ/AC/RULE requires one and `Dimension.isActiveFl` (DBF-FIN-018) drives no behaviour. No `activate` counterpart either, matching the delivered `AccountService.deactivate` precedent.
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-035:END -->

<!-- API:API-FIN-010:START traces=REQ-FIN-007,REQ-FIN-044,REQ-FIN-045,DBF-FIN-090,DBF-FIN-091,DBF-FIN-092 -->
### API-FIN-010 — create event-type rule
Entity       : ENT-FIN-009 · operation CREATE
Endpoint: POST /api/v1/fin/event-rules · Layers: `EventTypeRuleController.create`→`EventTypeRuleService.create`
Request: (writes DBF-FIN-090 eventTypeCode · DBF-FIN-091 nameAr · DBF-FIN-092 nameEn) `{eventTypeCode, nameAr, nameEn}` · Response: 201 · `EventTypeRuleResponse`
Validations: eventTypeCode validated via XM-FIN-001; uniqueness of one-active-rule-per-type (QR-FIN-014)
Errors: `FIN-409-RULE-DUP`, `FIN-400-INVALID-LOOKUP`
Orchestration: (sets DBF-FIN-093 isActiveFl=TRUE on insert) validate lookup → check uniqueness (QR-FIN-014) → persist (QR-FIN-013) → return
Repository: QR-FIN-013, QR-FIN-014 · join NONE · READ_WRITE
Security: screen FIN_RULES · `PERM_FIN_RULES_CREATE` · Localization: nameAr/nameEn required
Precondition: this endpoint is only reachable once FIN's onboarding has completed —
FIN registered as a module with its screens/actions into SEC (REQ-FIN-044) and FIN's lookup types (13 in v1, 15 since v2),
including ACCOUNTING_EVENT_TYPE, registered into MDL (REQ-FIN-045); both run
once at deployment, not per request.
Concurrency  : the pre-check is the friendly path; UQ_FIN_EVENT_TYPE_RULE_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_EVENT_TYPE_RULE_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-RULE-DUP — v1 behaviour as built, recorded here and not changed by v2
<!-- API:API-FIN-010:END -->

<!-- API:API-FIN-011:START traces=REQ-FIN-008,REQ-FIN-009,REQ-FIN-048,REQ-FIN-049,REQ-FIN-062,REQ-FIN-064,REQ-FIN-065,DBF-FIN-099,DBF-FIN-101,DBF-FIN-102,DBF-FIN-148,DBF-FIN-103,DBF-FIN-105,DBF-FIN-106,DBF-FIN-107,DBF-FIN-160,DBF-FIN-161,DBF-FIN-163,DBF-FIN-164 -->
### API-FIN-011 — add rule line (MODIFIED v2 — MAPPING business field, dimension tags)
Entity       : ENT-FIN-010, ENT-FIN-016 · operation CREATE (a line and its tags, gated as an update of the rule)
Endpoint     : POST /api/v1/fin/event-rules/{id}/lines   verb: POST
Layers       : `EventTypeRuleController.createRuleLine` → `RuleLineService.create`; every decision in `EventTypeRuleDomain`
Request      : path `id` → DBF-FIN-099 eventTypeRuleId; body `RuleLineCreateRequest` — `accountDerivationTypeCode` DBF-FIN-101 (String, required) · `accountDerivationValue` DBF-FIN-102 (String, CONSTANT only: the constant account code) · `accountBusinessFieldCode` DBF-FIN-148 (String, MAPPING only) · `amountSourceTypeCode` DBF-FIN-103 (required) · `amountSourceValue` DBF-FIN-104 (required unless REMAINDER) · `directionCode` DBF-FIN-105 (required) · `distributionTypeCode` DBF-FIN-106 (required) · `isRemainderFl` DBF-FIN-107 (Boolean, default false) · `dimensionTags[]`, each {`dimensionId` DBF-FIN-163 (required) · `valueSourceCode` DBF-FIN-160 (required) · `dimensionValueId` DBF-FIN-164 (CONSTANT only) · `businessFieldCode` DBF-FIN-161 (BUSINESS_FIELD only)}; excluded: ruleLinePk, lineNo, ruleLineDimensionPk, ruleLineId, createdAt
Response     : 201 · `ApiResponse<RuleLineResponse>` — the line with its `dimensionTags[]`
Validations  : in this order, inside one transaction; nothing is written if any fails
- lookup codes amountSourceTypeCode, directionCode, distributionTypeCode, every valueSourceCode and every business-field code (FIN_EVENT_BUSINESS_FIELD) through XM-FIN-001 → FIN-400-INVALID-LOOKUP
- RULE-FIN-018 — trigger: on create (rule line). Statement: reject a rule line whose account derivation type is not an active ACCOUNT_DERIVATION_TYPE value, so that CONSTANT and MAPPING are the only accepted derivations and no derivation reads an account code from the event. Message: ar "نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط" · en "Unsupported account derivation type; only constant or mapping is allowed" → FIN-422-UNSUPPORTED-DERIVATION. Membership is read from MDL (`readActiveValuesByKey("ACCOUNT_DERIVATION_TYPE")`, XM-FIN-001); DIRECT is deactivated there by db-script BLOCK 8, so a DIRECT line fails this rule and not the generic lookup check (AC-FIN-048)
- RULE-FIN-019 — trigger: on create (rule line). Statement: require a CONSTANT rule line to carry an account code in accountDerivationValue and no business field, and a MAPPING rule line to carry a business field in accountBusinessFieldCode and no account code. Message: ar "مواصفة اشتقاق الحساب لا تطابق نوعه" · en "The account derivation spec does not match its type" → FIN-422-DERIVATION-SPEC. CHK_FIN_RULE_LINE_DERIVATION_SPEC holds the exclusive-or in the store; the domain check answers first (AC-FIN-049)
- RULE-FIN-003 (v1, SRS §A5) — exactly one remainder line once the set is a compound or percentage distribution, and `isRemainderFl` (DBF-FIN-107) agrees with the line's own REMAINDER type code → FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER (QR-FIN-016). v1 cited the marker as DBF-FIN-103; that column is amountSourceTypeCode — corrected here
- RULE-FIN-025 — trigger: on create (rule line with its dimension tags). Statement: reject a rule line that carries more than one dimension tag for the same dimension. Message: ar "لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة" · en "A rule line cannot tag the same dimension twice" → FIN-422-DIMENSION-TAG-DUP, checked over the submitted list (AC-FIN-069); UQ_FIN_RULE_LINE_DIM_LINE_DIM is the store's guarantee
- RULE-FIN-026 — trigger: on create (rule line with its dimension tags). Statement: require a CONSTANT dimension tag to carry a dimension value that belongs to the tagged dimension and no business field, and a BUSINESS_FIELD dimension tag to carry a business field and no dimension value. Message: ar "وسم البُعد لا يطابق مصدر قيمته" · en "The dimension tag does not match its value source" → FIN-422-DIMENSION-TAG-SOURCE (AC-FIN-070: a CONSTANT BRANCH tag citing an ORGANISATION value). An unknown dimension answers FIN-404-DIMENSION and an unknown value FIN-404-DIMVALUE (QR-FIN-061)
Errors       : FIN-404-RULE, FIN-400-INVALID-LOOKUP, FIN-422-UNSUPPORTED-DERIVATION, FIN-422-DERIVATION-SPEC, FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER, FIN-404-DIMENSION, FIN-404-DIMVALUE, FIN-422-DIMENSION-TAG-DUP, FIN-422-DIMENSION-TAG-SOURCE, FIN-403-FORBIDDEN, FIN-500
Orchestration: (sets DBF-FIN-100 lineNo = the rule's highest lineNo + 1 · each tag's DBF-FIN-162 ruleLineId from the saved line) load the rule under a row lock (FIN-404-RULE) → validate the lookup codes (XM-FIN-001) → `assertDerivationSupported` (RULE-FIN-018) → `assertDerivationSpec` (RULE-FIN-019) → RULE-FIN-003 over the rule's existing lines plus the new one (QR-FIN-016) → load each tag's dimension and CONSTANT value (QR-FIN-061) → `assertTagsDistinct` (RULE-FIN-025) → `assertTagMatchesSource` (RULE-FIN-026) → persist the line (QR-FIN-015) and its tags (QR-FIN-056) → return the line with its tags
Repository   : QR-FIN-016, QR-FIN-061, QR-FIN-015, QR-FIN-056 · join NONE · READ_WRITE (one transaction)
Concurrency  : lineNo is allocated from a read (the rule's highest lineNo + 1), and RULE-FIN-003 decides on the sibling set this call then extends. Two simultaneous additions to one rule are serialized by a PESSIMISTIC_WRITE load of the parent FIN_EVENT_TYPE_RULE row before either is read. FIN_RULE_LINE has no unique constraint on (event_type_rule_id, line_no), so that lock is the only guard
Security     : screen FIN_RULES · `PERM_FIN_RULES_UPDATE` — adding a line, with its tags, is an update of the rule (SRS SCR-REQ-FIN-003 §B4; tags add no action, ADR-FIN-025) — checked before processing
Localization : every message ar + en
<!-- API:API-FIN-011:END -->

<!-- API:API-FIN-034:START traces=REQ-FIN-007,DBF-FIN-093 -->
### API-FIN-034 — deactivate event-type rule
Entity       : ENT-FIN-009 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/event-rules/{id}/deactivate · Layers: `EventTypeRuleController.deactivate`→`EventTypeRuleService.deactivate`
Request: path `id`, no body · Response: 200 · `EventTypeRuleResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-RULE`
Orchestration: (sets DBF-FIN-093 isActiveFl=FALSE) load → clear the active flag (DBF-FIN-093) → persist → return
Repository: `EventTypeRuleRepository.findById` + `save` — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_RULES · `PERM_FIN_RULES_UPDATE` (pre-existing — no new constant, no migration) · Localization: n/a
Why it exists: until this endpoint landed no rule could ever be retired, so `FIN-404-NO-ACTIVE-RULE` (RULE-FIN-005, API-FIN-020) was unreachable. **Stated limitation**, recorded in the service's own javadoc: deactivating does NOT free the event type for a replacement rule, because `EventTypeRuleService.create` guards uniqueness with `existsByEventTypeCode`, which is not scoped to the active flag. No `activate` counterpart, and no rule-line delete — ENT-FIN-010 carries no active-flag column and FIN publishes no `DELETE` endpoint on any screen (a deliberate v1 exclusion, see srs-fin.md SCR-REQ-FIN-003 §B4).
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-034:END -->

<!-- API:API-FIN-013:START traces=REQ-FIN-022,DBF-FIN-112,DBF-FIN-113,DBF-FIN-114,DBF-FIN-115 -->
### API-FIN-013 — create template
Entity       : ENT-FIN-011, ENT-FIN-012 · operation CREATE
Endpoint: POST /api/v1/fin/recurring-templates · Layers: `RecurringTemplateController.create`→`RecurringTemplateService.create`
Request: (writes DBF-FIN-110 nameAr · DBF-FIN-111 nameEn · DBF-FIN-112 scheduleTypeCode · DBF-FIN-113 frequencyCode · DBF-FIN-114 startDate · DBF-FIN-116 endDate; per line DBF-FIN-125 accountId · DBF-FIN-126 amount · DBF-FIN-127 directionCode · DBF-FIN-128 dimensionValueId) `{nameAr, nameEn, scheduleTypeCode, frequencyCode?, startDate, endDate?, lines: [...]}`
Response: 201 · `RecurringTemplateResponse`
Validations: scheduleTypeCode/frequencyCode/line directionCode validated via XM-FIN-001;
frequencyCode required unless scheduleTypeCode=REVERSING
Errors: `FIN-400-MISSING-FREQUENCY`, `FIN-400-INVALID-LOOKUP`
Orchestration: (sets DBF-FIN-115 nextRunDate=startDate · DBF-FIN-117 isActiveFl=TRUE · per line DBF-FIN-123 recurringTemplateId and DBF-FIN-124 lineNo in submission order) validate → set nextRunDate=startDate → persist template+lines (QR-FIN-018) → return
Repository: QR-FIN-018 · join NONE · READ_WRITE
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_CREATE` · Localization: nameAr/nameEn required
Concurrency  : NONE — nothing unique is allocated: every PK comes from its table's own sequence and lineNo is the position in this one request
<!-- API:API-FIN-013:END -->

<!-- API:API-FIN-036:START traces=REQ-FIN-022,DBF-FIN-117 -->
### API-FIN-036 — deactivate template
Entity       : ENT-FIN-011 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/recurring-templates/{id}/deactivate · Layers: `RecurringTemplateController.deactivate`→`RecurringTemplateService.deactivate`
Request: path `id`, no body · Response: 200 · `RecurringTemplateResponse` (isActiveFl=false)
Validations: none beyond existence · Errors: `FIN-404-TEMPLATE`
Orchestration: (sets DBF-FIN-117 isActiveFl=FALSE) load → `RecurringTemplate.deactivate()` (the entity's own helper, never a direct field assignment) → persist → re-read the template's lines and hand them to the mapper → return. The line re-read is not decoration: `RecurringTemplateResponse.lineCount` is derived from the list the mapper is handed, so passing an empty list would misreport the aggregate as having no lines. It is orchestration (load → map), not a rule.
Repository: `RecurringTemplateRepository.findById` + `save`, plus `RecurringTemplateLineRepository.findByRecurringTemplatePk` for the response's lines — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_UPDATE` (pre-existing — V24 seeds the `FIN_RECURRING_TEMPLATES / UPDATE` action row and V25's blanket Tier-3 grant already covers it, so no new constant, no new error code and no migration) · Localization: n/a
**DEFECT CLOSED 2026-09-12 — deactivate now DOES stop the run.** This paragraph used to report an OPEN DEFECT: `RecurringTemplateService.run` (API-FIN-014) ignored `isActiveFl`, so a deactivated template still ran and still posted. That is no longer true. `RecurringTemplateDomain.assertCanRun()` — a NEW Domain companion for ENT-FIN-011 — is called by `RecurringTemplateService.run` immediately after the row is loaded and before anything else is resolved, and refuses a deactivated template with `FIN-409-NOT-ACTIVE` (`Status.CONFLICT` → HTTP 409; ar "هذا التعريف غير نشط ولا يمكن تشغيله", en "This definition is deactivated and cannot be run"). The screen's 404 was deliberately not reused: the template does exist. **This gate is a RECORDED HUMAN DECISION, not spec.** No RULE-FIN-* states it, and AC-FIN-023 is written "Given an active recurring template" without stating any outcome for an inactive one — a later reader must not mistake the gate for a requirement that was always there. What remains OPEN on this screen is only that a template cannot be UPDATED after creation (next paragraph). Recorded identically at srs-fin.md SCR-REQ-FIN-004 §B4 and §"Access summary".
Why it exists: srs-fin.md SCR-REQ-FIN-004 §B4 recorded the absence of a template deactivate as an OPEN DEFECT rather than a scope decision — `IS_ACTIVE_FL` is NOT NULL, the entity's `activate()`/`deactivate()` shipped with zero callers, B2 advertises an `isActiveFl(EXACT)` filter over a column nothing could set to FALSE, and AC-FIN-023 is written "Given an active recurring template", presupposing a state nothing could produce. Deliberately NOT built, and NOT taken by this endpoint: a template `update` — a correct update must decide the fate of the template's existing child lines, which is a design question no REQ, AC or RULE answers, so it remains the still-open half of the same gap. No `activate` counterpart either; FIN ships none for any entity.
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-036:END -->

<!-- API:API-FIN-016:START traces=REQ-FIN-025,DBF-FIN-133,DBF-FIN-142,DBF-FIN-144 -->
### API-FIN-016 — create allocation rule
Entity       : ENT-FIN-013, ENT-FIN-014 · operation CREATE
Endpoint: POST /api/v1/fin/allocation-rules · Layers: `AllocationRuleController.create`→`AllocationRuleService.create`
Request: (writes DBF-FIN-131 nameAr · DBF-FIN-132 nameEn · DBF-FIN-133 sourceAccountId; per target DBF-FIN-142 targetAccountId · DBF-FIN-143 dimensionValueId · DBF-FIN-144 distributionTypeCode · DBF-FIN-145 distributionValue · DBF-FIN-146 isRemainderFl) `{nameAr, nameEn, sourceAccountId, targets: [...]}`
Response: 201 · `AllocationRuleResponse`
Validations: RULE-FIN-003 (reused) — exactly one remainder target when any sibling is
PERCENTAGE or any target is already marked remainder (QR-FIN-016, reused), and each target's
`isRemainderFl` (DBF-FIN-146) must agree with its own `distributionTypeCode`;
distributionTypeCode validated via XM-FIN-001
Errors: `FIN-409-REMAINDER-COUNT`, `FIN-422-REMAINDER-MARKER`, `FIN-404-ACCOUNT`, `FIN-400-INVALID-LOOKUP`
Orchestration: (sets DBF-FIN-134 isActiveFl=TRUE · per target DBF-FIN-140 allocationRuleId and DBF-FIN-141 lineNo in submission order) validate → check RULE-FIN-003 across targets (QR-FIN-016) → persist rule+targets (QR-FIN-021) → return
Repository: QR-FIN-021, QR-FIN-016 · join NONE · READ_WRITE
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_CREATE` · Localization: nameAr/nameEn required
Concurrency  : NONE — nothing unique is allocated: every PK comes from its table's own sequence and lineNo is the position in this one request
<!-- API:API-FIN-016:END -->

<!-- API:API-FIN-037:START traces=REQ-FIN-025,DBF-FIN-134 -->
### API-FIN-037 — deactivate allocation rule
Entity       : ENT-FIN-013 · operation UPDATE (deactivate)
Endpoint: PUT /api/v1/fin/allocation-rules/{id}/deactivate · Layers: `AllocationRuleController.deactivate`→`AllocationRuleService.deactivate`
Request: path `id`, no body · Response: 200 · `AllocationRuleResponse` (isActiveFl=false)
Validations: none beyond existence — RULE-FIN-003, the one rule `AllocationRuleDomain` owns, governs the remainder-target SET at create and run time and says nothing about the active flag · Errors: `FIN-404-ALLOCATION-RULE`
Orchestration: (sets DBF-FIN-134 isActiveFl=FALSE) load → `AllocationRule.deactivate()` (the entity's own helper, never a direct field assignment) → persist → re-read the rule's targets and hand them to the mapper → return. The target re-read is not decoration: `AllocationRuleResponse.targetCount` is derived from the list the mapper is handed, so passing an empty list would misreport the aggregate as having no targets. It is orchestration (load → map), not a rule.
Repository: `AllocationRuleRepository.findById` + `save`, plus `AllocationTargetRepository.findByAllocationRulePk` for the response's targets — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join NONE · READ_WRITE
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_UPDATE` (pre-existing — V24 seeds the `FIN_ALLOCATION_RULES / UPDATE` action row and V25's blanket Tier-3 grant already covers it, so no new constant, no new error code and no migration) · Localization: n/a
**DEFECT CLOSED 2026-09-12 — deactivate now DOES stop the run.** This paragraph used to report an OPEN DEFECT: `AllocationRuleService.run` (API-FIN-017) ignored `isActiveFl`, so a deactivated rule still ran and still posted, and `AllocationRuleDomain.isActive()` had no caller. That is no longer true. `AllocationRuleDomain.assertCanRun()` is called by `AllocationRuleService.run` immediately after the row is loaded and before the target set is even fetched, and refuses a deactivated rule with `FIN-409-NOT-ACTIVE` (`Status.CONFLICT` → HTTP 409; ar "هذا التعريف غير نشط ولا يمكن تشغيله", en "This definition is deactivated and cannot be run"). The screen's 404 was deliberately not reused: the rule does exist. **This gate is a RECORDED HUMAN DECISION, not spec** — no RULE-FIN-* states it, and a later reader must not mistake it for a requirement that was always there. What remains OPEN on this screen is only that a rule cannot be UPDATED after creation (next paragraph). Recorded identically at srs-fin.md SCR-REQ-FIN-005 §B4 and §"Access summary".
Why it exists: srs-fin.md SCR-REQ-FIN-005 §B4 recorded the absence of a rule deactivate as an OPEN DEFECT rather than a scope decision — `IS_ACTIVE_FL` is NOT NULL, the entity's `activate()`/`deactivate()` shipped with zero callers, `AllocationRuleDomain.isActive()` was dead code, and B2 advertises an `isActiveFl(EXACT)` filter over a column nothing could set to FALSE. Deliberately NOT built, and NOT taken by this endpoint: a rule `update` — a correct update must decide the fate of the rule's existing targets (and therefore of RULE-FIN-003's remainder-target set), which is a design question no REQ, AC or RULE answers, so it remains the still-open half of the same gap. No `activate` counterpart either; FIN ships none for any entity.
Concurrency  : NONE — flips one flag on the row it loads; nothing is allocated or decided on a read
<!-- API:API-FIN-037:END -->

<!-- API:API-FIN-019:START traces=REQ-FIN-014,REQ-FIN-015,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,DBF-FIN-036,DBF-FIN-037,DBF-FIN-038,DBF-FIN-044 -->
### API-FIN-019 — create manual entry
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 · operation CREATE
Endpoint: POST /api/v1/fin/journal-entries · Layers: `JournalEntryController.createManual`→`JournalEntryService.createManual`
Request: (writes DBF-FIN-036 docDate · DBF-FIN-037 fiscalYearId · DBF-FIN-038 periodId · DBF-FIN-044 descriptionAr · DBF-FIN-045 descriptionEn; per line DBF-FIN-054 accountId · DBF-FIN-055 amount · DBF-FIN-056 directionCode · DBF-FIN-058 descriptionAr · DBF-FIN-059 descriptionEn; per line dimension DBF-FIN-063 dimensionId · DBF-FIN-064 dimensionValueId) `{docDate, fiscalYearId, periodId, journalTypeCode="MANUAL", descriptionAr,
descriptionEn, lines: [{accountId, amount, directionCode, descriptionAr?, descriptionEn?,
dimensions: [{dimensionId, dimensionValueId}]}]}` — excludes {journalEntryPk, docNo,
statusCode, postedAt, audit}
Response: 201 · `JournalEntryResponse` (statusCode=POSTED on success)
Validations: RULE-FIN-006 (debit=credit, QR-FIN-029), RULE-FIN-007 (leaf/active accounts,
QR-FIN-030), RULE-FIN-008 (period open, QR-FIN-031), RULE-FIN-009 (dimension valid,
QR-FIN-032) — every failure returned together (REQ-FIN-015), nothing posts if any fails.
Header coherence (RULE-FIN-017) runs FIRST and fail-fast: the submitted `periodId` must
belong to the submitted `fiscalYearId` (DBF-FIN-076) and `docDate` must fall inside that
period's [startDate, endDate] (DBF-FIN-080/081) — an incoherent triple makes RULE-FIN-008's
own period gate meaningless, lets the entry take a docNo from the wrong year's series, and
corrupts every period-scoped report and the year-end close. This is the only API that needs
it: every system-generated entry derives the three facts from one another
Errors: `FIN-400-PERIOD-NOT-IN-YEAR`, `FIN-400-DOCDATE-OUTSIDE-PERIOD`, `FIN-409-UNBALANCED`,
`FIN-409-NOT-POSTABLE-ACCOUNT`, `FIN-409-PERIOD-NOT-OPEN`, `FIN-409-INVALID-DIMENSION`
Numbering: `docNo` = `JV-{fiscalYearCode}-{NNNNNN}` — literal prefix `JV-`, the owning
fiscal year's `code` (DBF-FIN-066, VARCHAR(10)), `-`, then a zero-padded 6-digit counter
starting at `000001` (e.g. `JV-2026-000123`; worst case 20 chars, inside
`doc_no VARCHAR(30)`). Counter scoped per `fiscalYearId`, restarting at `000001` each
fiscal year — one counter for all journal types, never segmented by `journalTypeCode`;
`UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO (fiscal_year_id, doc_no)` backs uniqueness at the
database level. Produced by a FIN-local generator in `com.erp.fin`, deliberately NOT a
shared `com.erp.common` component (no platform numbering engine exists in this repo);
implemented in this phase. Assigned once on create, immutable thereafter — excluded from
every create/update request DTO, present only in responses.
Orchestration: (sets DBF-FIN-035 docNo · DBF-FIN-039 journalTypeCode=MANUAL · DBF-FIN-040 statusCode DRAFT→POSTED · DBF-FIN-046 postedAt · per line DBF-FIN-052 journalEntryId, DBF-FIN-053 lineNo and DBF-FIN-057 isRemainderFl=FALSE · per line dimension DBF-FIN-062 journalLineId) resolve fiscal year UNDER A ROW LOCK (`SELECT ... FOR UPDATE` on
FIN_FISCAL_YEAR, the docNo series' allocation lock) → resolve period → check RULE-FIN-017 →
generate docNo (FIN-local generator) → build DRAFT (QR-FIN-024) → validate
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
Concurrency  : the docNo series — allocated from a read of the fiscal year's last number and then written — is guarded by the fiscal-year row lock taken first in the Orchestration line; UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO stays an unreachable backstop
<!-- API:API-FIN-019:END -->

<!-- API:API-FIN-039:START traces=REQ-FIN-054,REQ-FIN-059,REQ-FIN-060,REQ-FIN-061,DBF-FIN-150,DBF-FIN-151,DBF-FIN-152,DBF-FIN-153,DBF-FIN-154 -->
### API-FIN-039 — create account mapping
Entity       : ENT-FIN-015 · operation CREATE
Endpoint     : POST /api/v1/fin/account-mappings   verb: POST
Layers       : `AccountMappingController.create` → `AccountMappingService.create`; every decision in `AccountMappingDomain`
Request      : body `AccountMappingCreateRequest` — `eventTypeCode` DBF-FIN-150 (String, required, ACCOUNTING_EVENT_TYPE) · `businessFieldCode` DBF-FIN-151 (String, required, FIN_EVENT_BUSINESS_FIELD) · `businessValue` DBF-FIN-152 (String, required, ≤ 100, a code stored as-is) · `accountId` DBF-FIN-153 (Long, required, FK_ACCOUNT_MAPPING_ACCOUNT); excluded: accountMappingPk (from SEQ_FIN_ACCOUNT_MAPPING), isActiveFl, audit fields
Response     : 201 · `ApiResponse<AccountMappingResponse>` (isActiveFl = true)
Validations  : in this order
- lookup codes eventTypeCode (ACCOUNTING_EVENT_TYPE) and businessFieldCode (FIN_EVENT_BUSINESS_FIELD) through XM-FIN-001 → FIN-400-INVALID-LOOKUP
- RULE-FIN-024 — trigger: on create (account mapping). Statement: reject an account mapping for the PAYMENT_METHOD business field whose business value is not an active PAYMENT_METHOD code. Message: ar "القيمة ليست طريقة دفع معرَّفة" · en "The value is not a defined payment method" → FIN-422-INVALID-PAYMENT-METHOD. Only when businessFieldCode = PAYMENT_METHOD; membership in `readActiveValuesByKey("PAYMENT_METHOD")` (XM-FIN-001; AC-FIN-065)
- RULE-FIN-023 — trigger: on create / update (account mapping). Statement: reject an account mapping whose account is not a leaf account or is inactive. Message: ar "الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا" · en "The mapped account must be an active leaf account" → FIN-422-MAPPED-ACCOUNT-INELIGIBLE (QR-FIN-060; AC-FIN-064). An unknown accountId → FIN-404-ACCOUNT
- RULE-FIN-022 — trigger: on create (account mapping). Statement: reject an account mapping whose event type, business field and business value already have an active mapping. Message: ar "يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها" · en "An active mapping already exists for this event type, business field and value" → FIN-409-MAPPING-DUP (QR-FIN-052; AC-FIN-063)
Errors       : FIN-400-INVALID-LOOKUP, FIN-422-INVALID-PAYMENT-METHOD, FIN-404-ACCOUNT, FIN-422-MAPPED-ACCOUNT-INELIGIBLE, FIN-409-MAPPING-DUP, FIN-403-FORBIDDEN, FIN-500
Orchestration: (sets DBF-FIN-154 isActiveFl=TRUE; the PK is drawn from SEQ_FIN_ACCOUNT_MAPPING) validate the two lookup codes (XM-FIN-001) → when businessFieldCode = PAYMENT_METHOD, hand MDL's active PAYMENT_METHOD codes to `assertBusinessValueDefined` (RULE-FIN-024) → load the account (QR-FIN-060) and hand its leaf and active flags to `assertAccountEligible` (RULE-FIN-023) → look for an active mapping on the key (QR-FIN-052) and hand the answer to `assertKeyFree` (RULE-FIN-022) → persist, flushed (QR-FIN-051) → return (AC-FIN-058)
Repository   : QR-FIN-060, QR-FIN-052, QR-FIN-051 · join NONE · READ_WRITE
Concurrency  : two simultaneous creates for one key can both pass QR-FIN-052. The partial unique index UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY (one ACTIVE row per event type, business field and value) makes the second impossible: its flush is refused and FinPersistenceRefusalAdvisor answers FIN-409-MAPPING-DUP, the same answer the pre-check gives (ADR-FIN-030). The pre-check is the friendly path and the index is the guarantee
Security     : screen FIN_ACCOUNT_MAPPINGS · `PERM_FIN_ACCOUNT_MAPPINGS_CREATE`, checked before processing (VIEW is the gateway)
Localization : every message ar + en; no name is stored — the mapping is labelled by its account's nameAr/nameEn
<!-- API:API-FIN-039:END -->

<!-- API:API-FIN-040:START traces=REQ-FIN-056,REQ-FIN-058,REQ-FIN-060,DBF-FIN-149,DBF-FIN-153 -->
### API-FIN-040 — update account mapping (the account only)
Entity       : ENT-FIN-015 · operation UPDATE (change the account)
Endpoint     : PUT /api/v1/fin/account-mappings/{id}   verb: PUT
Layers       : `AccountMappingController.update` → `AccountMappingService.update`
Request      : path `id` → DBF-FIN-149 accountMappingPk; body `AccountMappingUpdateRequest` — `accountId` DBF-FIN-153 (Long, required) and nothing else. eventTypeCode, businessFieldCode and businessValue are fixed after create and are not in the body (ADR-FIN-024); to move a mapping to another key, deactivate it and create a new one
Response     : 200 · `ApiResponse<AccountMappingResponse>`
Validations  :
- RULE-FIN-023 — trigger: on update (account mapping); statement and message as in API-FIN-039 → FIN-422-MAPPED-ACCOUNT-INELIGIBLE (QR-FIN-060); an unknown accountId → FIN-404-ACCOUNT
- no active-state gate: the SRS states none, and a deactivated mapping resolves nothing whatever its account (RULE-FIN-020)
Errors       : FIN-404-ACCOUNT-MAPPING, FIN-404-ACCOUNT, FIN-422-MAPPED-ACCOUNT-INELIGIBLE, FIN-403-FORBIDDEN, FIN-500
Orchestration: (sets DBF-FIN-153 accountId only; updatedBy and updatedAt are filled by the platform) load the mapping (FIN-404-ACCOUNT-MAPPING) → load the account (QR-FIN-060) → `AccountMappingDomain.assertAccountEligible` (RULE-FIN-023) → update (QR-FIN-053) → return (AC-FIN-060). REQ-FIN-058 holds by structure: a posted line stores its resolved accountId and no line references a mapping, so this write cannot reach a posted entry (AC-FIN-062)
Repository   : QR-FIN-060, QR-FIN-053 · join NONE · READ_WRITE
Concurrency  : NONE for the mapping — it sets one column from the request and allocates nothing; two concurrent changes end last-writer-wins, each validated against its own account. The account's eligibility is read and not locked, so an account deactivated a moment later is caught at post time by RULE-FIN-007 (REQ-FIN-052)
Security     : screen FIN_ACCOUNT_MAPPINGS · `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`, checked before processing
Localization : every message ar + en
<!-- API:API-FIN-040:END -->

<!-- API:API-FIN-041:START traces=REQ-FIN-057,REQ-FIN-058,DBF-FIN-149,DBF-FIN-154 -->
### API-FIN-041 — deactivate account mapping
Entity       : ENT-FIN-015 · operation UPDATE (deactivate)
Endpoint     : PUT /api/v1/fin/account-mappings/{id}/deactivate   verb: PUT — FIN's house style: a soft deactivate under UPDATE, no DELETE endpoint (SRS SCR-REQ-FIN-013 §B4/§B5)
Layers       : `AccountMappingController.deactivate` → `AccountMappingService.deactivate`
Request      : path `id` → DBF-FIN-149 accountMappingPk, no body
Response     : 200 · `ApiResponse<AccountMappingResponse>` (isActiveFl = false)
Validations  : none beyond existence; deactivating a mapping that is already inactive is not an error (the API-FIN-004 precedent)
Errors       : FIN-404-ACCOUNT-MAPPING, FIN-403-FORBIDDEN, FIN-500
Orchestration: (sets DBF-FIN-154 isActiveFl=FALSE) load (FIN-404-ACCOUNT-MAPPING) → `AccountMapping.deactivate()` → persist (QR-FIN-054) → return. From the next event on QR-FIN-055 no longer finds the mapping, so an event that needs it answers FIN-422-UNMAPPED-VALUE (REQ-FIN-057, AC-FIN-061); posted lines keep their account (REQ-FIN-058). No `activate` counterpart (ADR-FIN-024)
Repository   : QR-FIN-054 · join NONE · READ_WRITE
Concurrency  : NONE — flips one flag; nothing is allocated or decided on a read
Security     : screen FIN_ACCOUNT_MAPPINGS · `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE`, checked before processing
Localization : n/a
<!-- API:API-FIN-041:END -->
<!-- SUB:SVC-API-CRUD:END -->

<!-- SUB:SVC-API-INT:START traces=REQ-FIN-010,REQ-FIN-023,REQ-FIN-024,REQ-FIN-026,REQ-FIN-028,REQ-FIN-031,REQ-FIN-034,REQ-FIN-036,REQ-FIN-037,REQ-FIN-050,REQ-FIN-063,REQ-FIN-072,REQ-FIN-073 -->
### SUB — SVC-API-INT (posting-pipeline orchestration and period-control actions)

<!-- API:API-FIN-020:START traces=REQ-FIN-010,REQ-FIN-011,REQ-FIN-012,REQ-FIN-013,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-047,REQ-FIN-050,REQ-FIN-051,REQ-FIN-052,REQ-FIN-053,REQ-FIN-063,REQ-FIN-066,REQ-FIN-067,REQ-FIN-068,REQ-FIN-069,REQ-FIN-070,REQ-FIN-072,REQ-FIN-073,REQ-FIN-074,REQ-FIN-075,REQ-FIN-076,DBF-FIN-041,DBF-FIN-090,DBF-FIN-102,DBF-FIN-148,DBF-FIN-153,DBF-FIN-160,DBF-FIN-161,DBF-FIN-164,DBF-FIN-063,DBF-FIN-064 -->
### API-FIN-020 — build event entry (system; MODIFIED v2 — mapped accounts, event dimensions, distinct rejections)
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 · operation CREATE (build and post); reads ENT-FIN-009, ENT-FIN-010, ENT-FIN-015, ENT-FIN-016, ENT-FIN-003, ENT-FIN-008
Endpoint     : POST /api/v1/fin/journal-entries/from-event   verb: POST — a system call with no screen affordance (SRS SCR-REQ-FIN-006)
Layers       : `JournalEntryController.buildFromEvent` → `EventEntryService.build` → `JournalPostingService.buildValidateAndPost`
Request      : body `EventEntryBuildRequest` (as published in api-docs-fin.md) — `eventReference` → DBF-FIN-041 (required, ≤ 100) · `eventTypeCode` (required; selects the active rule by DBF-FIN-090) · `docDate` → DBF-FIN-036 (required; selects the period by DBF-FIN-080/081) · `baseAmount` and `amounts{}` (the amount sources the rule lines name) · `fields{}` — the event's business facts, keyed by FIN_EVENT_BUSINESS_FIELD code (PAYMENT_METHOD, EXPENSE_TYPE_CODE, ORGANISATION_CODE, BRANCH_CODE), string values (ADR-FIN-031) · `descriptionAr` → DBF-FIN-044 · `descriptionEn` → DBF-FIN-045. No payload value is ever read as an account code: an extra `fields.accountCode` is simply never looked at (REQ-FIN-047, AC-FIN-047)
Response     : 200 · `ApiResponse<JournalEntryResponse>` (statusCode POSTED, lines with their dimensions) — as published; a rejection writes no row (SRS A7)
Validations  : in build order; the first failure rejects the whole event and nothing is written
- RULE-FIN-004 (MODIFIED v2) — trigger: on create (event-sourced entry), by the pre-check before building and again when the store's uniqueness guarantee on eventReference refuses a concurrent second write. Statement: reject building an entry for an event reference that has already produced a posted entry, and answer with this rule's duplicate-event error whichever detection catches the repeat; a uniqueness refusal from the store is never surfaced as a generic data-integrity or server error. Message: ar "تم بالفعل ترحيل قيد لهذا المرجع" · en "An entry for this event reference has already been posted" → FIN-409-DUPLICATE-EVENT (pre-check QR-FIN-026; the store's refusal — see Concurrency)
- RULE-FIN-005 (v1, SRS §A5) — no active rule for the event type → FIN-404-NO-ACTIVE-RULE (QR-FIN-027)
- RULE-FIN-028 — trigger: on build (event-sourced entry). Statement: reject the event with the no-defined-period configuration error when no accounting period's start and end dates cover the event's date, and never answer this case with the period-not-open error. Message: ar "لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث" · en "No accounting period is defined for the event date" → FIN-422-NO-DEFINED-PERIOD (QR-FIN-059 finds no covering period; AC-FIN-080)
- RULE-FIN-003 (v1, SRS §A5) — single remainder marker over the stored line set (QR-FIN-016) → FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER
- RULE-FIN-021 — trigger: on build (event-sourced entry, per MAPPING rule line and per BUSINESS_FIELD dimension tag). Statement: reject the event with the missing-business-field error when the event does not carry the business field named by a MAPPING rule line or by a BUSINESS_FIELD dimension tag. Message: ar "الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة" · en "The event does not carry the business field the rule requires" → FIN-422-MISSING-BUSINESS-FIELD (the named key is absent from `fields{}` or blank; AC-FIN-053, AC-FIN-073)
- RULE-FIN-020 — trigger: on build (event-sourced entry, per MAPPING rule line). Statement: reject the event with the dedicated unmapped-value error when no active account mapping exists for the rule's event type, the rule line's business field and the event's value for that field; never substitute a default account. Message: ar "لا يوجد ربط حساب نشط للقيمة التجارية في الحدث" · en "No active account mapping exists for the business value in the event" → FIN-422-UNMAPPED-VALUE (QR-FIN-055 finds no active row; an inactive mapping counts as none — AC-FIN-056, AC-FIN-057)
- RULE-FIN-027 — trigger: on build (event-sourced entry, per BUSINESS_FIELD dimension tag). Statement: reject the event with the unresolvable-dimension-value error when the event's value for a tag's business field equals the code of no dimension value of the tagged dimension. Message: ar "قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد" · en "The dimension value in the event matches no defined value of this dimension" → FIN-422-UNRESOLVED-DIMENSION-VALUE (QR-FIN-058, exact code within the dimension — ADR-FIN-026; AC-FIN-074). A match that is inactive is RULE-FIN-009's rejection, below
- RULE-FIN-010 (v1, SRS §A5) — the remainder line, computed last per posting side, must come out positive → FIN-422-REMAINDER-NOT-POSITIVE (QR-FIN-028); a MAPPING remainder line computes exactly like any other (AC-FIN-082)
- RULE-FIN-006, RULE-FIN-007, RULE-FIN-008, RULE-FIN-009 (v1, SRS §A5 — applied unchanged to the new path, REQ-FIN-076): debits equal credits (QR-FIN-029) → FIN-409-UNBALANCED; every line's account — constant or mapped — is a leaf, active and postable (QR-FIN-030) → FIN-409-NOT-POSTABLE-ACCOUNT, ar "الحساب المستهدف لا يقبل ترحيلاً مباشرًا" · en "The target account does not accept direct posting" (REQ-FIN-052, AC-FIN-054, AC-FIN-055); the covering period is Open (QR-FIN-031) → FIN-409-PERIOD-NOT-OPEN, ar "الفترة المستهدفة غير مفتوحة" · en "The target period is not open" — the retryable rejection (REQ-FIN-073, AC-FIN-079); every dimension value — the tag's constant one or the one resolved from the event — belongs to its dimension and is active (QR-FIN-032) → FIN-409-INVALID-DIMENSION, ar "قيمة البُعد غير صالحة" · en "The dimension value is invalid" (REQ-FIN-066, REQ-FIN-067, AC-FIN-072)
Errors       : FIN-409-DUPLICATE-EVENT, FIN-404-NO-ACTIVE-RULE, FIN-422-NO-DEFINED-PERIOD, FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER, FIN-422-MISSING-BUSINESS-FIELD, FIN-422-UNMAPPED-VALUE, FIN-422-UNRESOLVED-DIMENSION-VALUE, FIN-404-ACCOUNT (a CONSTANT line's account code resolves to no account), FIN-422-REMAINDER-NOT-POSITIVE, FIN-409-UNBALANCED, FIN-409-NOT-POSTABLE-ACCOUNT, FIN-409-PERIOD-NOT-OPEN, FIN-409-INVALID-DIMENSION, FIN-404-YEAR, FIN-403-FORBIDDEN, FIN-500. Retired: FIN-422-MAPPING-UNSUPPORTED — MAPPING is implemented (ADR-FIN-027). No longer raised here: FIN-404-PERIOD for a date no period covers, which is FIN-422-NO-DEFINED-PERIOD now (ADR-FIN-030)
Orchestration: (sets DBF-FIN-035 docNo · DBF-FIN-039 journalTypeCode=EVENT_GENERATED · DBF-FIN-040 statusCode DRAFT→POSTED · DBF-FIN-046 postedAt · DBF-FIN-037 fiscalYearId and DBF-FIN-038 periodId from the covering period · per line DBF-FIN-052, DBF-FIN-053, DBF-FIN-054 accountId (constant or mapped), DBF-FIN-055, DBF-FIN-056, DBF-FIN-057 · per resolved tag one JournalLineDimension row DBF-FIN-062, DBF-FIN-063, DBF-FIN-064 — the shape a manual line writes, REQ-FIN-070)
  1. pre-check RULE-FIN-004 (QR-FIN-026) → 2. resolve the active rule (QR-FIN-027) → 3. resolve the period covering docDate (QR-FIN-059); none → `FiscalPeriodDomain.assertDateCovered` answers RULE-FIN-028. This event path resolves its period itself, so the shared pipeline's FIN-404-PERIOD stays the answer only for API-FIN-014 and API-FIN-017 → 4. load the rule's lines (QR-FIN-025, read half) and their tags (QR-FIN-057); re-check RULE-FIN-003 (QR-FIN-016)
  5. per line, the account: CONSTANT → the account whose code is the line's accountDerivationValue; MAPPING → the value `fields[accountBusinessFieldCode]` (`EventTypeRuleDomain.assertFieldPresent`, RULE-FIN-021) → the active mapping for (the rule's eventTypeCode, the line's business field, that value) (QR-FIN-055, `AccountMappingDomain.assertResolved`, RULE-FIN-020) → its accountId
  6. per line, the amount from its amount source; the remainder line last, per side (QR-FIN-028) → 7. per tag, the dimension value: CONSTANT → the tag's dimensionValueId; BUSINESS_FIELD → the value `fields[businessFieldCode]` (RULE-FIN-021) → the DimensionValue with that code in the tagged dimension (QR-FIN-058, `DimensionValueDomain.assertResolvedByCode`, RULE-FIN-027)
  8. validate RULE-FIN-006 / 007 / 008 / 009 (QR-FIN-029 … QR-FIN-032) → 9. allocate docNo under the fiscal-year row lock (as API-FIN-019) → 10. persist header, lines and line dimensions with the header flushed (QR-FIN-025) → post (QR-FIN-033) → return
Repository   : QR-FIN-026, QR-FIN-027, QR-FIN-059, QR-FIN-025, QR-FIN-057, QR-FIN-016, QR-FIN-055, QR-FIN-028, QR-FIN-058, QR-FIN-029, QR-FIN-030, QR-FIN-031, QR-FIN-032, QR-FIN-033 · join NONE · READ_WRITE (one transaction, build through post; a rejection rolls back every row)
Concurrency  : two simultaneous events with one reference (AC-FIN-078) can both pass the QR-FIN-026 pre-check. UQ_FIN_JOURNAL_ENTRY_EVENT_REF is the guarantee: the header insert is flushed inside the transaction, the store refuses the second, and FinPersistenceRefusalAdvisor (CORE, ADR-FIN-030) translates a refusal naming that constraint into FIN-409-DUPLICATE-EVENT with RULE-FIN-004's message. Exactly one entry posts and the other caller gets the duplicate error, never FIN-500 (REQ-FIN-072). The docNo series stays guarded by the fiscal-year row lock
Security     : system-to-system call under the event consumer's own service principal, gated like any authenticated caller · `PERM_FIN_JOURNAL_ENTRIES_CREATE`. Machine-caller authentication is a separate SEC change set (CS-FIN-001, out of scope)
Localization : every rejection carries its ar + en message; descriptionAr/descriptionEn are stored as sent
<!-- API:API-FIN-020:END -->

<!-- API:API-FIN-014:START traces=REQ-FIN-023,REQ-FIN-024,DBF-FIN-115,DBF-FIN-112 -->
### API-FIN-014 — run template
Entity       : ENT-FIN-011, ENT-FIN-004, ENT-FIN-005 · operation UPDATE (run — posts a RECURRING entry)
Endpoint: POST /api/v1/fin/recurring-templates/{id}/run · Layers: `RecurringTemplateController.run`→`RecurringTemplateService.run`
Request: path `id` (or invoked by an internal scheduler with no path — the endpoint is the
same either way) · Response: 201 · `JournalEntryResponse`
Validations: the template must still be ACTIVE — `RecurringTemplateDomain.assertCanRun()`
(the NEW Domain companion for ENT-FIN-011) runs FIRST, immediately after the row is loaded and
before the period is resolved, and refuses a deactivated template with `FIN-409-NOT-ACTIVE`;
**no RULE-FIN-* states this gate — it rests on a recorded human decision of 2026-09-12, not on
stated requirement.** Then the same RULE-FIN-006/007/008/009 checks as API-FIN-019 (the
template's own lines were already balance-checked at API-FIN-013 create time, but re-validated
here since accounts/periods may have changed since)
Errors: same as API-FIN-019, plus `FIN-404-TEMPLATE` (unknown recurring template id) and
`FIN-409-NOT-ACTIVE` (the template exists but is deactivated)
Orchestration: (sets DBF-FIN-115 nextRunDate, advanced per frequency; the posted entry's DBF-FIN-035 docNo, DBF-FIN-039 journalTypeCode=RECURRING and DBF-FIN-040 statusCode as in API-FIN-019) load template (QR-FIN-019) → assert it is active → build entry from its lines (journalTypeCode=
RECURRING) → validate + post (QR-FIN-029..033) → advance nextRunDate per frequencyCode →
if scheduleTypeCode=REVERSING: also build and post the linked reversal in the next period
(REQ-FIN-024, reusing RULE-FIN-011/012 via QR-FIN-034/036) → return
Repository: QR-FIN-019, QR-FIN-024(-style build), QR-FIN-029..033, QR-FIN-034, QR-FIN-036
· join NONE · READ_WRITE
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_UPDATE` (running
a template is modeled as an update-class custom action) · Localization: n/a
Concurrency  : the template row is loaded PESSIMISTIC_WRITE before nextRunDate is read, so two concurrent runs of one template serialize instead of both reading the same predecessor date and both posting (ADR-FIN-041); the posted entry's docNo is separately allocated under the fiscal-year row lock as in API-FIN-019
<!-- API:API-FIN-014:END -->

<!-- API:API-FIN-017:START traces=REQ-FIN-026,DBF-FIN-133,DBF-FIN-145,DBF-FIN-146 -->
### API-FIN-017 — run allocation rule
Entity       : ENT-FIN-013, ENT-FIN-004, ENT-FIN-005 · operation UPDATE (run — posts an ALLOCATION entry)
Endpoint: POST /api/v1/fin/allocation-rules/{id}/run · Layers: `AllocationRuleController.run`→`AllocationRuleService.run`
Request: path `id` · Response: 201 · `JournalEntryResponse`
Validations: the rule must still be ACTIVE — `AllocationRuleDomain.assertCanRun()` runs FIRST,
immediately after the row is loaded and before the target set is even fetched, and refuses a
deactivated rule with `FIN-409-NOT-ACTIVE`; **no RULE-FIN-* states this gate — it rests on a
recorded human decision of 2026-09-12, not on stated requirement.** Then RULE-FIN-003 (single
remainder marker over the stored targets, QR-FIN-016) and RULE-FIN-010 (per-side remainder
guarantee, QR-FIN-028, reused) then RULE-FIN-006/007/008/009
Errors: same family as API-FIN-019, plus `FIN-404-ALLOCATION-RULE`, `FIN-409-NOT-ACTIVE`,
`FIN-409-REMAINDER-COUNT`, `FIN-422-REMAINDER-MARKER`, `FIN-422-REMAINDER-NOT-POSITIVE`
Orchestration: (sets the posted entry's DBF-FIN-035 docNo, DBF-FIN-039 journalTypeCode=ALLOCATION and DBF-FIN-040 statusCode as in API-FIN-019) load rule → assert it is active → load targets + current source-account balance (QR-FIN-022) →
distribute per target's distributionTypeCode, the target marked `isRemainderFl`
(DBF-FIN-146, the single marker — not `distributionTypeCode`) absorbing the rounding
difference between the source line's side and the targets' own side (QR-FIN-028) → build
entry (journalTypeCode=ALLOCATION) → validate + post (QR-FIN-029..033) → return
Repository: QR-FIN-022, QR-FIN-028, QR-FIN-029..033 · join NONE · READ_WRITE
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_UPDATE` · Localization: n/a
Concurrency  : the posted entry's docNo is allocated under the same fiscal-year row lock as API-FIN-019 (JournalPostingService); nothing else is allocated. The source balance this run reads (QR-FIN-022, QR-FIN-043) is read without a lock and the entry is posted for that amount, so two concurrent runs of one rule both read the same balance and both post — a source of 1,000.00 can end at −1,000.00. v1 as built; stated here, not changed by v2 (ADR-FIN-042)
<!-- API:API-FIN-017:END -->

<!-- API:API-FIN-021:START traces=REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,DBF-FIN-042,DBF-FIN-043,REQ-FIN-077 -->
### API-FIN-021 — reverse entry
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 · operation CREATE (reverse — posts the mirror entry; custom action)
Endpoint: POST /api/v1/fin/journal-entries/{id}/reverse · Layers: `JournalEntryController.reverse`→`JournalEntryService.reverse`
Request: path `id` · Response: 201 · `JournalEntryResponse` (the new reversal entry)
Validations: RULE-FIN-013 (must be POSTED and not already reversed, QR-FIN-035); RULE-FIN-012
(period substitution if original's is closed, QR-FIN-036)
Errors: `FIN-409-NOT-POSTED`, `FIN-409-ALREADY-REVERSED`, `FIN-404-ENTRY`
Orchestration: (sets DBF-FIN-042 originalEntryId on the reversal · DBF-FIN-043 reversalEntryId on the original · the reversal's DBF-FIN-039 journalTypeCode=REVERSAL) load original UNDER A ROW LOCK (`SELECT ... FOR UPDATE` on the
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
Concurrency  : the already-reversed guard is a read-then-write, made atomic by the PESSIMISTIC_WRITE load of the original's header in the Orchestration line; the mirror's docNo is allocated under the fiscal-year row lock
v2: an entry posted through an account mapping or dimension tags reverses exactly like any other. The mirror copies each line's stored accountId and its JournalLineDimension rows with the opposite direction, the original stays POSTED, and every account and dimension balance nets to zero (REQ-FIN-077, AC-FIN-083). No mapping is consulted: a reversal reads the posted lines, never the rules or mappings that built them.
<!-- API:API-FIN-021:END -->

<!-- API:API-FIN-023:START traces=REQ-FIN-031,DBF-FIN-066,DBF-FIN-067,DBF-FIN-068,DBF-FIN-077 -->
### API-FIN-023 — create fiscal year
Entity       : ENT-FIN-007, ENT-FIN-008 · operation CREATE
Endpoint: POST /api/v1/fin/fiscal-years · Layers: `FiscalYearController.create`→`FiscalYearService.create`
Request: (writes DBF-FIN-066 code · DBF-FIN-067 startDate · DBF-FIN-068 endDate) `{code, startDate, endDate, periodCount}` · Response: 201 · `FiscalYearResponse` with its generated periods
Validations: uniqueness of code (QR-FIN-047)
Errors: `FIN-409-YEAR-DUP`
Orchestration: (sets DBF-FIN-069 statusCode=OPEN · DBF-FIN-070 isActiveFl=TRUE; per generated period DBF-FIN-076 fiscalYearId · DBF-FIN-077 periodNo · DBF-FIN-078 nameAr · DBF-FIN-079 nameEn · DBF-FIN-080 startDate · DBF-FIN-081 endDate · DBF-FIN-082 statusCode=OPEN) validate uniqueness → persist year (statusCode=OPEN) + generate periodCount periods,
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
Concurrency  : the code pre-check is the friendly path; UQ_FIN_FISCAL_YEAR_CODE is the guarantee. Two simultaneous requests can both pass the pre-check, and the store then refuses the second. UQ_FIN_FISCAL_YEAR_CODE is not registered with FinPersistenceRefusalAdvisor (ADR-FIN-030 registers only the two v2 constraints), so that refusal still surfaces as the platform's generic error instead of FIN-409-YEAR-DUP — v1 behaviour as built, recorded here and not changed by v2.
<!-- API:API-FIN-023:END -->

<!-- API:API-FIN-024:START traces=REQ-FIN-032,DBF-FIN-082 -->
### API-FIN-024 — open period
Entity       : ENT-FIN-008 · operation UPDATE (open)
Endpoint: PATCH /api/v1/fin/fiscal-periods/{id}/open · Layers: `FiscalPeriodController.open`→`FiscalPeriodService.open`
Request: path `id` · Response: 200 · `FiscalPeriodResponse`
Validations: current state must be SOFT_CLOSE (§10.2 "re-openable" — Hard Closed is not, RULE-FIN-014)
Errors: `FIN-409-NOT-REOPENABLE`, `FIN-404-PERIOD`
Orchestration: (sets DBF-FIN-082 statusCode=OPEN) load the period PESSIMISTIC_WRITE → check RULE-FIN-014 (QR-FIN-040) → transition (QR-FIN-039) → return
Repository: QR-FIN-039, QR-FIN-040 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_UPDATE` · Localization: n/a
Concurrency  : the period row is loaded PESSIMISTIC_WRITE before the current-status check, so two concurrent transitions of one period serialize instead of racing last-writer-wins (ADR-FIN-041)
<!-- API:API-FIN-024:END -->

<!-- API:API-FIN-025:START traces=REQ-FIN-033,DBF-FIN-082 -->
### API-FIN-025 — soft-close period
Entity       : ENT-FIN-008 · operation UPDATE (soft-close)
Endpoint: PATCH /api/v1/fin/fiscal-periods/{id}/soft-close · Layers: `FiscalPeriodController.softClose`→`FiscalPeriodService.softClose`
Request: path `id` · Response: 200 · `FiscalPeriodResponse`
Validations: current state must be OPEN · Errors: `FIN-409-INVALID-TRANSITION`, `FIN-404-PERIOD`
Orchestration: (sets DBF-FIN-082 statusCode=SOFT_CLOSE) load the period PESSIMISTIC_WRITE → transition (QR-FIN-039) → return · Repository: QR-FIN-039 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_UPDATE` · Localization: n/a
Concurrency  : the period row is loaded PESSIMISTIC_WRITE before the current-status check, so two concurrent transitions of one period serialize instead of racing last-writer-wins (ADR-FIN-041)
<!-- API:API-FIN-025:END -->

<!-- API:API-FIN-026:START traces=REQ-FIN-034,REQ-FIN-035,REQ-FIN-037,REQ-FIN-038,DBF-FIN-082,DBF-FIN-083,DBF-FIN-084 -->
### API-FIN-026 — hard-close period (approval)
Entity       : ENT-FIN-008 · operation UPDATE (hard-close — custom close-approve action)
Endpoint: PATCH /api/v1/fin/fiscal-periods/{id}/hard-close · Layers: `FiscalPeriodController.hardClose`→`FiscalPeriodService.hardClose`
Request: path `id` · Response: 200 · `FiscalPeriodResponse`
Validations: RULE-FIN-014 (not already Hard Closed, QR-FIN-040); RULE-FIN-015 (full text:
DATA-DOM §ENT-FIN-008) — caller must hold `PERM_FIN_PERIODS_CLOSE_APPROVE`, a permission
distinct from `PERM_FIN_JOURNAL_ENTRIES_CREATE`. **That gate is the WHOLE of the rule**: the
`@PreAuthorize` on `FiscalPeriodService.hardClose`, enforced by SEC's own mechanism, exactly as
RULE-FIN-015 states it and AC-FIN-038 describes it. There is NO service-layer SoD read — the
one that existed was deleted on 2026-09-12 as an over-implementation (it refused the close for
every caller whenever any single user held both codes); see the Error Catalog's struck
`FIN-403-SOD-VIOLATION` row and retired XM-FIN-002
Errors: `FIN-409-NOT-REOPENABLE`(reused message context), `FIN-404-PERIOD`
Orchestration: (sets DBF-FIN-082 statusCode=HARD_CLOSE · DBF-FIN-083 closedBy · DBF-FIN-084 closedAt) load the period PESSIMISTIC_WRITE → check RULE-FIN-014 (QR-FIN-040)
→ transition, set closedBy/closedAt to the approving principal (QR-FIN-039) → return
Repository: QR-FIN-039, QR-FIN-040 · join NONE · READ_WRITE
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_CLOSE_APPROVE` (custom — the distinct permission RULE-FIN-015 requires; since V30 it is also granted to SYS_ADMIN) · Localization: n/a
Concurrency  : the period row is loaded PESSIMISTIC_WRITE before the current-status check, so two concurrent transitions of one period serialize instead of racing last-writer-wins (ADR-FIN-041)
<!-- API:API-FIN-026:END -->

<!-- API:API-FIN-027:START traces=REQ-FIN-036,DBF-FIN-069,DBF-FIN-034 -->
### API-FIN-027 — run year-end close
Entity       : ENT-FIN-007, ENT-FIN-008, ENT-FIN-004 · operation UPDATE (year-end close; creates the CLOSING and OPENING entries)
Endpoint: POST /api/v1/fin/fiscal-years/{id}/year-end-close · Layers: `FiscalYearController.yearEndClose`→`FiscalYearService.yearEndClose`
Request: path `id` · Response: 201 · `{closingEntry: JournalEntryResponse, openingEntry: JournalEntryResponse}`
Validations, IN THIS ORDER (the order is part of the contract — a test asserting 409 on this
endpoint must not be defeated by a missing successor year or Retained Earnings
account): (1) the fiscal year's own statusCode must
still be OPEN — a re-run against an already-CLOSED year is rejected with
`FIN-409-INVALID-TRANSITION`, a stated rule on ENT-FIN-007's state machine rather than the
incidental `FIN-409-PERIODS-NOT-CLOSED` the first run's own YEAR_END_CLOSE transitions would
otherwise raise; (2) every period of the year must be HARD_CLOSE (§10.4 precondition), 409.
Only then are the successor year and the Retained Earnings account resolved.
RULE-FIN-015 is no longer a numbered step here: it is enforced entirely by this endpoint's
`@PreAuthorize(PERM_FIN_PERIODS_CLOSE_APPROVE)` gate, which rejects before the method body
runs. The step that used to come first — a service-layer SoD read answering 403 — was deleted
on 2026-09-12 as an over-implementation; see the Error Catalog's struck `FIN-403-SOD-VIOLATION`
row. **A test written against the old contract and asserting 403 from inside this method will
now fail, and should be retargeted at the `@PreAuthorize` gate.**
Errors: `FIN-409-INVALID-TRANSITION` (year already closed), `FIN-409-PERIODS-NOT-CLOSED`, `FIN-404-YEAR` (unknown year id, or no adjacent successor year), `FIN-404-ACCOUNT` (no account marked as Retained Earnings)
Orchestration: (sets DBF-FIN-069 statusCode=CLOSED · DBF-FIN-082 statusCode=YEAR_END_CLOSE on every period · the two entries' DBF-FIN-039 journalTypeCode=CLOSING/OPENING) load the fiscal year PESSIMISTIC_WRITE → verify the year is still OPEN → verify all periods Hard
Closed → resolve the successor year (QR-FIN-048) and the Retained Earnings account
(QR-FIN-045) → compute closing
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
Concurrency  : the fiscal year row is loaded PESSIMISTIC_WRITE at the top of the transaction, before the OPEN-status check, so two concurrent runs of year-end close on one year serialize instead of both passing the OPEN check and each posting a CLOSING/OPENING pair (ADR-FIN-041); the same lock backs the docNo allocation later in the same method
<!-- API:API-FIN-027:END -->
<!-- SUB:SVC-API-INT:END -->
<!-- PHASE:SVC-API:END -->

<!-- PHASE:DOC:START traces=REQ-FIN-017,REQ-FIN-072,REQ-FIN-073,REQ-FIN-074,REQ-FIN-075 -->
## PHASE 4 — DOC

**API contract summary** (backend self-check only; the frontend binds to the real `api-docs-fin.md` published after
implementation): 41 endpoints under `/api/v1/fin`, one per API-FIN-001 … API-FIN-041, listed with verb and path in
the API Registry of the Plan Index — not restated here.

**DTO typing constraints**: every lookup-backed field is a `String` holding the code, never a Java enum —
accountTypeCode, natureCode, directionCode, journalTypeCode, statusCode, eventTypeCode, accountDerivationTypeCode,
amountSourceTypeCode, distributionTypeCode, scheduleTypeCode, frequencyCode, and in v2 accountBusinessFieldCode,
businessFieldCode, valueSourceCode and (for the PAYMENT_METHOD field) businessValue. `docNo` never appears in a
request body and always appears in a response.

**Pagination and filter standard**: `{filters, page, size, sort}` in the body, `Page<T>` in `ApiResponse<T>`, as
SEC's and MDL's own CORE phases define it.

**Event rejection contract for the consumer (REQ-FIN-075, ADR-FIN-027, ADR-FIN-030)**. The event consumer branches on
the full code of an API-FIN-020 rejection, never on the HTTP status alone: several unrelated rejections share 409.
Each code appears in the Error Catalog exactly once, with its ar/en message, and the published api-docs carry the
same three classes:

| Class | Code | HTTP | Consumer action |
|---|---|---|---|
| already posted | FIN-409-DUPLICATE-EVENT | 409 | acknowledge the message: the event posted the first time, whether the pre-check or the store's uniqueness guarantee caught the repeat (REQ-FIN-072) |
| retry later | FIN-409-PERIOD-NOT-OPEN | 409 | wait and retry: the rejection clears once the target period is opened. Only a SOFT_CLOSE period can return to OPEN; a HARD_CLOSE or YEAR_END_CLOSE period never will, so a retry policy needs an upper bound (REQ-FIN-073) |
| needs a fix | every other code API-FIN-020 lists — among them FIN-422-UNMAPPED-VALUE (add a mapping), FIN-422-MISSING-BUSINESS-FIELD (fix the producer), FIN-422-UNRESOLVED-DIMENSION-VALUE (define the dimension value), FIN-422-NO-DEFINED-PERIOD (define the calendar) | 404 / 409 / 422 | mark the message failed; each code names the party that fixes it (ADR-FIN-027) |

**Event payload** (ADR-FIN-031): business facts travel in the request's `fields{}` map, keyed by
FIN_EVENT_BUSINESS_FIELD codes (PAYMENT_METHOD, EXPENSE_TYPE_CODE, ORGANISATION_CODE, BRANCH_CODE); the key set
grows as MDL data, not as a new request field. An account code is never read from the payload.
<!-- PHASE:DOC:END -->

<!-- PHASE:INT-C:START traces=REQ-FIN-001,REQ-FIN-045,REQ-FIN-048,REQ-FIN-054,REQ-FIN-061,REQ-FIN-062 -->
## PHASE 5 — INT-C (cross-module consume)

One live `XM-*` row, below the split threshold (1 < 5), so no SUB. XM-FIN-002 stays below as a retired, struck
historical block; it is not a live dependency. v2 mints no XM: the six new lookup-backed columns widen XM-FIN-001,
and the new screen's registration into SEC is platform integration under ADR-FIN-001, like every v1 screen.

| XM | Classification | Target | Interface (in_process — one deployable) | What the target publishes | Status |
|---|---|---|---|---|---|
| XM-FIN-001 | SOFT-READ | MDL — ENT-MDL-001, ENT-MDL-002 | injected `com.erp.mdl.crossmodule.MdlLookupApi` | `readActiveValuesByKey(typeKey)` → `List<LookupOptionView>` | ACTIVE (MODIFIED v2 — 15 keys) |
| XM-FIN-002 | READ (historical) | SEC | none — the consuming service was deleted | — | RETIRED 2026-09-12 |

<!-- XM:XM-FIN-001:START traces=REQ-FIN-001,REQ-FIN-007,REQ-FIN-008,REQ-FIN-010,REQ-FIN-014,REQ-FIN-018,REQ-FIN-022,REQ-FIN-025,REQ-FIN-031,REQ-FIN-045,REQ-FIN-048,REQ-FIN-049,REQ-FIN-054,REQ-FIN-061,REQ-FIN-062 -->
### XM-FIN-001 — validate and read lookup-backed codes against MDL (MODIFIED v2 — 15 keys, 6 more columns)
Target        : MDL · ENT-MDL-001 (LookupType), ENT-MDL-002 (LookupValue) · classification SOFT-READ
Interface     : in-process (profile.conventions.module_interface = in_process) — FIN's services inject MDL's published
                `com.erp.mdl.crossmodule.MdlLookupApi` and call `readActiveValuesByKey(typeKey)`, consuming
                `LookupOptionView` (code, labelAr, labelEn, sortOrder) and never an MDL entity. One deployable: no
                HTTP client, no base path, no timeout, no network error path
Contract      : data required = the submitted code is an active value of the named FIN-owned key. The v1 columns
                are unchanged; v2 adds six: the rule line's accountBusinessFieldCode, the mapping's eventTypeCode,
                businessFieldCode and — only when businessFieldCode is PAYMENT_METHOD — businessValue, and the tag's
                valueSourceCode and businessFieldCode (DBF-FIN-148, DBF-FIN-150, DBF-FIN-151, DBF-FIN-152,
                DBF-FIN-160, DBF-FIN-161). Fallback if absent = reject with FIN-400-INVALID-LOOKUP, except
                ACCOUNT_DERIVATION_TYPE (RULE-FIN-018 → FIN-422-UNSUPPORTED-DERIVATION) and PAYMENT_METHOD as a
                business value (RULE-FIN-024 → FIN-422-INVALID-PAYMENT-METHOD). MDL's own not-found for an unknown
                key is caught and answered as FIN-400-INVALID-LOOKUP; MDL's code never leaks out of FIN's API.
                Retry: none (same process). Idempotency: read-only
Blocks        : none DEFERRED — MDL v1 is gated, the row is ACTIVE. v2 depends on db-script BLOCK 8 (2 new keys, 6
                values, CHEQUE, DIRECT retired) having run before API-FIN-011 or API-FIN-039 is first called —
                listed with its producer in SEC-BE's bootstrap rows. Accepted risk, unchanged: a value deactivated
                in MDL after a FIN row cites it is not retro-invalidated (db-script §2.1)
<!-- XM:XM-FIN-001:END -->

FIN's dependency on SEC for identity and authorization — the principal on every request, the `@PreAuthorize` gate,
and FIN's own registration of its module, screens and actions into SEC, the account-mappings screen included —
remains platform integration and not an `XM` row (ADR-FIN-001, carried from P2). Since 2026-09-12 it is the only
relationship FIN has with SEC: XM-FIN-002 was registered for FIN's read of SEC's data about other users, and that
read was deleted with the service that made it.
<!-- PHASE:INT-C:END -->

<!-- PHASE:INT-R:START traces=REQ-FIN-001,REQ-FIN-045 -->
## PHASE 6 — INT-R (cross-module resolve)

| XM | Status | Workaround (if not READY / ACTIVE) |
|---|---|---|
| XM-FIN-001 | ACTIVE | not applicable — MDL is gated. The v2 widening needs no MDL change: MDL receives FIN's new keys and values as data (db-script BLOCK 8) |
| XM-FIN-002 | RETIRED 2026-09-12 | not applicable — historical; the service that consumed it was deleted |

No DEFERRED row exists. The next module to consume FIN is outside this batch; the inbound stub
`XM-INBOUND-STUB-3` names FIN as its eventual target, the formal id to be assigned by that module's own P2. The
legacy Oracle host of CS-FIN-001 reaches FIN only through the event endpoint API-FIN-020, via a consumer that is out
of scope; it has no table access in either direction and so no XM (db-script §2).
<!-- PHASE:INT-R:END -->

<!-- PHASE:SEC-BE:START traces=REQ-FIN-038,REQ-FIN-044,REQ-FIN-045,REQ-FIN-054 -->
## PHASE 7 — SEC-BE (security, backend half)

Permission matrix — one row per composite screen; a `✓` cell names the endpoints it serves and the permission for
that action. FIN publishes no `DELETE` endpoint, so the DELETE column is empty everywhere.

| Screen (page code) | ENT | VIEW | CREATE | UPDATE | DELETE | Custom |
|---|---|---|---|---|---|---|
| FIN_ACCOUNTS | ENT-FIN-001 | ✓ `PERM_FIN_ACCOUNTS_VIEW` (API-FIN-001) | ✓ `PERM_FIN_ACCOUNTS_CREATE` (API-FIN-002) | ✓ `PERM_FIN_ACCOUNTS_UPDATE` (API-FIN-003; API-FIN-004 deactivate) | — | — |
| FIN_DIMENSIONS | ENT-FIN-002, ENT-FIN-003 | ✓ `PERM_FIN_DIMENSIONS_VIEW` (API-FIN-005, API-FIN-008) | ✓ `PERM_FIN_DIMENSIONS_CREATE` (API-FIN-006, API-FIN-007) | ✓ `PERM_FIN_DIMENSIONS_UPDATE` (API-FIN-035, deactivate a dimension value — V28) | — | — |
| FIN_RULES | ENT-FIN-009, ENT-FIN-010, ENT-FIN-016 | ✓ `PERM_FIN_RULES_VIEW` (API-FIN-009) | ✓ `PERM_FIN_RULES_CREATE` (API-FIN-010) | ✓ `PERM_FIN_RULES_UPDATE` (API-FIN-011, add a line with its dimension tags; API-FIN-034, deactivate a rule) | — | — |
| FIN_RECURRING_TEMPLATES | ENT-FIN-011, ENT-FIN-012 | ✓ `PERM_FIN_RECURRING_TEMPLATES_VIEW` (API-FIN-012) | ✓ `PERM_FIN_RECURRING_TEMPLATES_CREATE` (API-FIN-013) | ✓ `PERM_FIN_RECURRING_TEMPLATES_UPDATE` (API-FIN-014, run; API-FIN-036, deactivate) | — | — |
| FIN_ALLOCATION_RULES | ENT-FIN-013, ENT-FIN-014 | ✓ `PERM_FIN_ALLOCATION_RULES_VIEW` (API-FIN-015) | ✓ `PERM_FIN_ALLOCATION_RULES_CREATE` (API-FIN-016) | ✓ `PERM_FIN_ALLOCATION_RULES_UPDATE` (API-FIN-017, run; API-FIN-037, deactivate) | — | — |
| FIN_JOURNAL_ENTRIES | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | ✓ `PERM_FIN_JOURNAL_ENTRIES_VIEW` (API-FIN-018, API-FIN-022) | ✓ `PERM_FIN_JOURNAL_ENTRIES_CREATE` (API-FIN-019, API-FIN-020) | — | — | Reverse `PERM_FIN_JOURNAL_ENTRIES_REVERSE` (API-FIN-021) |
| FIN_PERIODS | ENT-FIN-007, ENT-FIN-008 | ✓ `PERM_FIN_PERIODS_VIEW` (API-FIN-033, and the gateway) | ✓ `PERM_FIN_PERIODS_CREATE` (API-FIN-023) | ✓ `PERM_FIN_PERIODS_UPDATE` (API-FIN-024, API-FIN-025) | — | Close-approve `PERM_FIN_PERIODS_CLOSE_APPROVE` (API-FIN-026, API-FIN-027 — the distinct permission RULE-FIN-015 requires) |
| FIN_ACCOUNT_LEDGER | ENT-FIN-005 | ✓ `PERM_FIN_ACCOUNT_LEDGER_VIEW` (API-FIN-028) | — | — | — | — |
| FIN_TRIAL_BALANCE | ENT-FIN-005 | ✓ `PERM_FIN_TRIAL_BALANCE_VIEW` (API-FIN-029) | — | — | — | — |
| FIN_BALANCE_SHEET | ENT-FIN-005 | ✓ `PERM_FIN_BALANCE_SHEET_VIEW` (API-FIN-030) | — | — | — | — |
| FIN_INCOME_STATEMENT | ENT-FIN-005 | ✓ `PERM_FIN_INCOME_STATEMENT_VIEW` (API-FIN-031) | — | — | — | — |
| FIN_DIMENSION_REPORTS | ENT-FIN-005, ENT-FIN-006 | ✓ `PERM_FIN_DIMENSION_REPORTS_VIEW` (API-FIN-032) | — | — | — | — |
| FIN_ACCOUNT_MAPPINGS (v2) | ENT-FIN-015 | ✓ `PERM_FIN_ACCOUNT_MAPPINGS_VIEW` (API-FIN-038) | ✓ `PERM_FIN_ACCOUNT_MAPPINGS_CREATE` (API-FIN-039) | ✓ `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE` (API-FIN-040, change the account; API-FIN-041, deactivate) | — | — |

**Every API verifies its permission before processing.** Each service method carries `@PreAuthorize` naming the
cell's permission from `PermissionConstants`. Services hold no hard-coded role check. API-FIN-020 is a system call:
the event consumer's service principal must hold `PERM_FIN_JOURNAL_ENTRIES_CREATE`; how a machine caller
authenticates is SEC's separate change set (CS-FIN-001, out of scope).

**v2 — FIN_ACCOUNT_MAPPINGS.** One new SEC_PAGES row (page code FIN_ACCOUNT_MAPPINGS, parent FIN, name ar
"روابط الحسابات" / en "Account mappings") and three action rows, VIEW (the gateway), CREATE and UPDATE, following
`PERM_<PAGE_CODE>_<ACTION>`. `PermissionConstants` gains the three constants. Deactivate is modelled under UPDATE,
exactly as FIN_ACCOUNTS models API-FIN-004; there is no DELETE action for this page. They are delivered by a new
forward FIN migration — the next free Flyway version after V30 — that registers the page and its three actions AND
grants all three explicitly to SYS_ADMIN, following V28. The explicit grant is required: V25 granted SYS_ADMIN its FIN
actions with a `SELECT` over the action registry that has already run everywhere, so Flyway will never re-evaluate
it for a row inserted later. Registering without granting would leave every mapping endpoint answering
FIN-403-FORBIDDEN to every caller, the administrator included. The SRS role "finance administrator" is assigned
these permissions through SEC's role screens; it is not a FIN seed. `FIN_CLOSE_APPROVER` (V27) receives nothing new.

**v2 — FIN_RULES.** Dimension tags travel inside the add-rule-line submission and add no action (ADR-FIN-025):
API-FIN-011 keeps `PERM_FIN_RULES_UPDATE`, so no new constant, no new row and no migration.

**The DELETE column is empty by design.** Deactivation is `PUT /{id}/deactivate` under the screen's UPDATE
permission everywhere — API-FIN-004, 034, 035, 036, 037 and, in v2, API-FIN-041 — and no FIN screen has a
registered delete permission (V24, V28 and the v2 migration seed none). No deactivate has an `activate`
counterpart, following the delivered `AccountService.deactivate` precedent.

**FIN_DIMENSIONS / UPDATE — the one cell V24 did not seed** (as built). `PERM_FIN_DIMENSIONS_UPDATE` is registered by
`V28__fin_dimensions_update_action.sql`, which also grants it explicitly to SYS_ADMIN, for the V25 reason above. The
VIEW gateway on the same screen was already registered and granted by V24/V25.

**FIN_RULES, FIN_RECURRING_TEMPLATES and FIN_ALLOCATION_RULES / UPDATE — one permission, two endpoints each** (as
built). API-FIN-034, API-FIN-036 and API-FIN-037 reuse `PERM_FIN_RULES_UPDATE`,
`PERM_FIN_RECURRING_TEMPLATES_UPDATE` and `PERM_FIN_ALLOCATION_RULES_UPDATE`, seeded by V24 and granted by V25's
Tier-3 statement, so none needed a new constant, error code or migration.

**Deactivating a template or an allocation rule stops its run — closed 2026-09-12, by decision, not by rule.**
`RecurringTemplateDomain.assertCanRun()` (API-FIN-014) and `AllocationRuleDomain.assertCanRun()` (API-FIN-017) refuse
a deactivated definition with FIN-409-NOT-ACTIVE. No RULE-FIN-* states this gate; it rests on a recorded human
decision and must not be cited as pre-existing spec. What stays open on those two screens is only that neither can be
updated after creation (SRS SCR-REQ-FIN-004 §B4, SCR-REQ-FIN-005 §B4).

**FIN_PERIODS / VIEW — a gateway that also has an endpoint** (as built). `PERM_FIN_PERIODS_VIEW` is load-bearing:
`MenuService.effectiveAuthorityCodes()` keeps a granted permission only if the same screen also carries a granted
VIEW, so V27's `FIN_CLOSE_APPROVER` must hold it for `PERM_FIN_PERIODS_CLOSE_APPROVE` to reach the caller. Since
API-FIN-033 it also gates the fiscal-period search; the constant was added with that endpoint, and no migration was
needed.

**SoD enforcement (RULE-FIN-015, POL-FIN-016) — as rewritten 2026-09-12, unchanged by v2.** The requirement, read at
source: the period-close approval is gated by a permission distinct from journal-entry creation, enforced through the
Security module, and the rule's `Data source` is DEFERRED — FIN has no permission entity to read. The delivered
`@PreAuthorize(PermissionConstants.PERM_FIN_PERIODS_CLOSE_APPROVE)` on `FiscalPeriodService.hardClose` (API-FIN-026)
and `FiscalYearService.yearEndClose` (API-FIN-027) satisfies it in full: it is a different code from
`PERM_FIN_JOURNAL_ENTRIES_CREATE`, and SEC's mechanism enforces it (`JwtAuthenticationFilter` builds the caller's
authorities from `MenuService`, which reads the grant table). Keeping the two permissions on different roles remains
sound administration, and `FIN_CLOSE_APPROVER` (V27) is how a deployment expresses it — a guideline, not a check in FIN
code.

**What this section used to specify, and must not specify again.** Earlier revisions said FIN's service layer also
checks at close time that no single user holds both permissions. That global user-set disjointness rule is stronger
than anything the SRS states. It was implemented as `FinSeparationOfDutiesService` reading SEC's user directory and
`FiscalPeriodDomain.assertCanHardClose` throwing `FIN-403-SOD-VIOLATION` whenever any user in the system held both —
refusing the close for every caller. Both were deleted on 2026-09-12 by recorded human decision. **No FIN service
reads SEC's user directory, and none may be specified here.** XM-FIN-002 is retired and FIN-403-SOD-VIOLATION is
struck as unreachable in the Error Catalog.

**Who can close, as seeded.** V25 had withheld `PERM_FIN_PERIODS_CLOSE_APPROVE` from SYS_ADMIN only to avoid tripping
the removed check; `V30__fin_sys_admin_close_approve_grant.sql` grants it, so the bootstrap `admin` user can hard-close
a period and run a year-end close. V27's `FIN_CLOSE_APPROVER` role (the `PERM_FIN_PERIODS_VIEW` gateway plus
`PERM_FIN_PERIODS_CLOSE_APPROVE`, and by construction no entry creation) stays valid as the least-privilege option.
V27's header text and V25's describe the removed mechanism as live; both are applied and immutable, so this paragraph
is the correction: there is no `resolveFacts()`, no `assertCanHardClose` and no thrower of `FIN-403-SOD-VIOLATION`
anywhere.

**Gateway**: every non-VIEW permission requires VIEW on the same screen first — SEC's own interceptor convention, not
a FIN-owned rule.

**Forbidden responses**: a `@PreAuthorize` denial on any `com.erp.fin.service.*` method is caught by
`com.erp.fin.security.FinForbiddenAdvisor` (one `DefaultPointcutAdvisor` at `Ordered.HIGHEST_PRECEDENCE`, never a
feature `@ControllerAdvice`) and re-raised as `LocalizedException(Status.FORBIDDEN, FinErrorCodes.FIN_403_FORBIDDEN)`,
so FIN-403-FORBIDDEN reaches the wire with both ar and en messages. Two boundaries hold, both read in source: a
denial raised by the Spring Security filter chain before any FIN service is entered still answers the platform's own
forbidden code — unreachable for FIN today, because the platform `SecurityConfig` authorizes with
`.anyRequest().authenticated()` and declares no FIN URL rule; and FIN-403-SOD-VIOLATION never was an access denial
and now has no throw site. The v2 mapping endpoints inherit this unchanged.

### BOOTSTRAP DATA — FIN v2
Every row a fresh deployment needs before a FIN endpoint can succeed, each with who produces it. Lookup tables belong
to MDL, yet the keys are FIN's, so FIN seeds them. Registering a permission is not a grant: every permission names the
role it is granted to (`profile.conventions.security_model.grant_target` = role).

```
LOOKUP KEY  ACCOUNT_TYPE               │ seed source: FIN onboarding into MDL (REQ-FIN-045, delivered in v1) — 5 values
LOOKUP KEY  DEBIT_CREDIT               │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 2 values
LOOKUP KEY  PERIOD_STATE               │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 4 values
LOOKUP KEY  FISCAL_YEAR_STATUS         │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 2 values
LOOKUP KEY  JOURNAL_TYPE               │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 5 values, plus CLOSING and OPENING added as data at v1 ALIGN-BE
LOOKUP KEY  JOURNAL_STATUS             │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 3 values
LOOKUP KEY  ACCOUNTING_EVENT_TYPE      │ seed source: the type by FIN onboarding (v1); its values are host data, added in MDL by the deploying organization before its first rule or mapping (SRS A6 — none seeded)
LOOKUP KEY  PAYMENT_METHOD             │ seed source: the type by FIN onboarding (v1); CHEQUE by db-script v2 BLOCK 8 (idempotent SQL into MDL's tables, ADR-FIN-028); every other method is host data
LOOKUP KEY  ACCOUNT_DERIVATION_TYPE    │ seed source: the type, CONSTANT and MAPPING by FIN onboarding (v1); DIRECT deactivated by db-script v2 BLOCK 8
LOOKUP KEY  AMOUNT_SOURCE_TYPE         │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 3 values
LOOKUP KEY  DISTRIBUTION_TYPE          │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 3 values
LOOKUP KEY  RECURRING_SCHEDULE_TYPE    │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 2 values
LOOKUP KEY  RECURRING_FREQUENCY        │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 4 values
LOOKUP KEY  FIN_EVENT_BUSINESS_FIELD   │ seed source: db-script v2 BLOCK 8 — the type and 4 values (ADR-FIN-013)
LOOKUP KEY  FIN_DIMENSION_VALUE_SOURCE │ seed source: db-script v2 BLOCK 8 — the type and 2 values (ADR-FIN-010)
PERMISSION  PERM_FIN_ACCOUNTS_VIEW                │ grant target: role SYS_ADMIN — registered by V24, granted by V25
PERMISSION  PERM_FIN_ACCOUNTS_CREATE              │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ACCOUNTS_UPDATE              │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_DIMENSIONS_VIEW              │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_DIMENSIONS_CREATE            │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_DIMENSIONS_UPDATE            │ grant target: role SYS_ADMIN — registered and granted by V28
PERMISSION  PERM_FIN_RULES_VIEW                   │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RULES_CREATE                 │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RULES_UPDATE                 │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RECURRING_TEMPLATES_VIEW     │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RECURRING_TEMPLATES_CREATE   │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RECURRING_TEMPLATES_UPDATE   │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ALLOCATION_RULES_VIEW        │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ALLOCATION_RULES_CREATE      │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ALLOCATION_RULES_UPDATE      │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_JOURNAL_ENTRIES_VIEW         │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_JOURNAL_ENTRIES_CREATE       │ grant target: role SYS_ADMIN — V24, V25; the event consumer's service principal needs a role holding it (SEC change set)
PERMISSION  PERM_FIN_JOURNAL_ENTRIES_REVERSE      │ grant target: role SYS_ADMIN — V24 (custom action), V25
PERMISSION  PERM_FIN_PERIODS_VIEW                 │ grant target: roles SYS_ADMIN (V25) and FIN_CLOSE_APPROVER (V27) — V24 registers it
PERMISSION  PERM_FIN_PERIODS_CREATE               │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_PERIODS_UPDATE               │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_PERIODS_CLOSE_APPROVE        │ grant target: roles SYS_ADMIN (V30) and FIN_CLOSE_APPROVER (V27) — V24 registers it
PERMISSION  PERM_FIN_ACCOUNT_LEDGER_VIEW          │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_TRIAL_BALANCE_VIEW           │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_BALANCE_SHEET_VIEW           │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_INCOME_STATEMENT_VIEW        │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_DIMENSION_REPORTS_VIEW       │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ACCOUNT_MAPPINGS_VIEW        │ grant target: role SYS_ADMIN — registered and granted by the v2 FIN security migration (after V30)
PERMISSION  PERM_FIN_ACCOUNT_MAPPINGS_CREATE      │ grant target: role SYS_ADMIN — the v2 FIN security migration
PERMISSION  PERM_FIN_ACCOUNT_MAPPINGS_UPDATE      │ grant target: role SYS_ADMIN — the v2 FIN security migration
REQUIRED    FIN_ACCOUNT.is_retained_earnings_fl  │ seed source: marked on exactly one account as data by the deploying organization before the first year-end close (API-FIN-027 answers FIN-404-ACCOUNT otherwise); never written by an API
DATA        FIN_ACCOUNT_MAPPING rows            │ seed source: the migration-data load of CS-FIN-001, produced once the store exists (out of scope); until then every MAPPING line answers FIN-422-UNMAPPED-VALUE
```
<!-- PHASE:SEC-BE:END -->

<!-- PHASE:ALIGN-BE:START traces=REQ-FIN-017,REQ-FIN-076 -->
## PHASE 8 — ALIGN-BE

The self-check of this plan. Each row names the `gov.py analyze` check that backs it, and its mark is that check's
result in the stage report (`_state/analyze-stage-P3.1.md`), copied rather than judged. A clause that examined nothing
is written as such, never ✓. The RESULT line is written by the orchestrator.

The one ✗: C7.12 flags the id of SEC's User entity, cited in the retired XM-FIN-002 block of INT-C that
`gov.py state` carries unchanged from v1. The citation is correct history. It fails to resolve because
SEC's current version (v2) has no artifacts yet, so SEC publishes no id at all to resolve it against.

```
ALIGN — FIN v2
row               backing check        assertion                                                                  mark
TRACEABILITY      traces               every PHASE/SUB/atom block carries traces=, and every API traces to its     ✓ (C7.2: 57 blocks · C7.3: 41 API)
                                       REQ and its DBF
COVERED           orphans              every REQ is covered by ≥1 API or DBF                                      ✓ (C7.6: 77 REQ)
BINDING (§2A)     value-agreement      every DBF names the same physical column here as the db-script declares   ✓ (C7.10)
MANIFEST (§4)     count-agrees         every total this plan states equals the rows it heads                      ✓ (C7.18: 4 totals)
WRITERS           required-writer      every required column is written by an endpoint, or the row states why    ✓ (C7.19: 82 columns)
QRC (§5)          orphans              every catalogued query is reached by ≥1 API                                ✓ (C7.22: 61 QR)
API (R3)          code-format          every catalog code is an instance of the declared format and carries a    ✓ (C7.11)
                                       status the platform can emit
RULE INPUTS       data-source          every RULE enforced at runtime names where the data it READS comes from,  ✓ (C6.9)
                                       or is deferred
CROSS-MODULE      registry-agree       every registered XM is placed here, and every XM minted here is           ✓ (C7.5, C7.5b: 2 XM)
                                       back-registered
FOREIGN IDS       xref-resolve         every id of another module cited here is defined in that module's own     ✗ (C7.12: 1)
                                       registry
BOOTSTRAP DATA    bootstrap-complete   every lookup key and every permission has a row naming who produces it    ✓ (C7.21: 45 names)
SECURITY (R7)     operation-resolves   every declared entity operation resolves to an API, and every marked      ✓ (C7.20: 65)
                                       matrix cell names its API and its permission
DEMAND (SRS)      operation-resolves   every operation an SRS screen names is built by an API, or the plan       ✓ (C7.23: 30)
                                       states why it is not
DECISIONS         refs-exist           every ADR this plan cites exists on disk in analysis/decisions/FIN/        ✓ (C7.13)
PATHS             paths-resolve        every path the generated manifest and execution state emit resolves to    ✓ (C7.14)
                                       something that exists
COVERAGE          (the report)         clauses the report lists as having examined nothing: none
RESULT            PASSED ✓ — 0 findings
```

**Coverage — ENT/DBF → phases → QR → XM.** Every ENT-FIN-001 … ENT-FIN-016 has exactly one DATA-DOM block and at least
one SVC-API endpoint; all 165 DBF ids have one manifest row; XM-FIN-001 binds the 22 lookup-backed columns.
| ENT | DBF | Endpoints | Queries | XM |
|---|---|---|---|---|
| ENT-FIN-001 | DBF-FIN-001..013, DBF-FIN-147 | API-FIN-001 … 004 | QR-FIN-001 … 006, QR-FIN-045, QR-FIN-060 | XM-FIN-001 |
| ENT-FIN-002 | DBF-FIN-014..022 | API-FIN-005, 006 | QR-FIN-007, QR-FIN-008, QR-FIN-046, QR-FIN-061 | — |
| ENT-FIN-003 | DBF-FIN-023..033 | API-FIN-007, 008, 035 | QR-FIN-009 … 011, QR-FIN-058, QR-FIN-061 | — |
| ENT-FIN-004 | DBF-FIN-034..050 | API-FIN-018 … 022 | QR-FIN-023 … 037 | XM-FIN-001 |
| ENT-FIN-005 | DBF-FIN-051..060 | API-FIN-019, 020, 022, 028 … 032 | QR-FIN-024, 025, 028 … 030, 034, 037, 042 … 044 | XM-FIN-001 |
| ENT-FIN-006 | DBF-FIN-061..064 | API-FIN-019, 020, 022, 032 | QR-FIN-024, 025, 032, 034, 037, 044 | — |
| ENT-FIN-007 | DBF-FIN-065..074 | API-FIN-023, 027 | QR-FIN-038, 041, 047, 048 | XM-FIN-001 |
| ENT-FIN-008 | DBF-FIN-075..088 | API-FIN-023 … 026, 033 | QR-FIN-038 … 040, 049, QR-FIN-059 | XM-FIN-001 |
| ENT-FIN-009 | DBF-FIN-089..097 | API-FIN-009, 010, 034 | QR-FIN-012 … 014, 027 | XM-FIN-001 |
| ENT-FIN-010 | DBF-FIN-098..108, DBF-FIN-148 | API-FIN-011 | QR-FIN-015, 016 | XM-FIN-001 |
| ENT-FIN-011 | DBF-FIN-109..121 | API-FIN-012 … 014, 036 | QR-FIN-017 … 019 | XM-FIN-001 |
| ENT-FIN-012 | DBF-FIN-122..129 | API-FIN-012, 013 | QR-FIN-018, 019 | XM-FIN-001 |
| ENT-FIN-013 | DBF-FIN-130..138 | API-FIN-015 … 017, 037 | QR-FIN-020 … 022 | — |
| ENT-FIN-014 | DBF-FIN-139..146 | API-FIN-015, 016 | QR-FIN-016, 021, 022 | XM-FIN-001 |
| ENT-FIN-015 | DBF-FIN-149..158 | API-FIN-038 … 041; read by API-FIN-020 | QR-FIN-050 … 055 | XM-FIN-001 |
| ENT-FIN-016 | DBF-FIN-159..165 | API-FIN-011; read by API-FIN-020 | QR-FIN-056, 057, 061 | XM-FIN-001 |

**Coverage — RULE → API → catalog code**: the Rule Registry of the Plan Index carries it, one row per RULE-FIN-001 …
RULE-FIN-028; every rule that raises a user-facing rejection has exactly one Error Catalog row per code.

**Coverage — XM → status → blocks → workaround**: XM-FIN-001 → ACTIVE → blocks nothing → no workaround; XM-FIN-002 →
RETIRED → blocks nothing → not applicable.
<!-- PHASE:ALIGN-BE:END -->

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
