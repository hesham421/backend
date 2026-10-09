## REGISTRY — P1 — FIN v2
══════════════════════════════════════════════════════════════════
Change set : CS-FIN-001 (ADDITIVE) — delta on v1; this registry is the complete current P1 registry
             (v1 rows restated, v2 rows marked) so the merge into project-registry.md needs no v1 lookup.

Entities
| ENT id | Name (ar/en) | Kind | PRIVATE/SHARED | Status | v2 |
|---|---|---|---|---|---|
| ENT-FIN-001 | الحساب / Account | master | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-002 | البُعد / Dimension | config | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-003 | قيمة البُعد / DimensionValue | lookup | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-004 | رأس قيد اليومية / JournalEntry | transactional | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-005 | سطر قيد اليومية / JournalLine | transactional | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-006 | بُعد سطر القيد / JournalLineDimension | transactional | PRIVATE | REGISTERED | unchanged (now also written for event-built lines) |
| ENT-FIN-007 | السنة المالية / FiscalYear | master | PRIVATE | REGISTERED | MODIFIED v2 — label note only (ADR-FIN-038) |
| ENT-FIN-008 | الفترة المحاسبية / FiscalPeriod | master | PRIVATE | REGISTERED | MODIFIED v2 — label note only (ADR-FIN-038) |
| ENT-FIN-009 | قاعدة نوع الحدث / EventTypeRule | config | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-010 | سطر القاعدة / RuleLine | config | PRIVATE | REGISTERED | MODIFIED — DIRECT removed, accountBusinessFieldCode added |
| ENT-FIN-011 | قالب متكرر/عكسي / RecurringTemplate | config | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-012 | سطر القالب المتكرر / RecurringTemplateLine | config | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-013 | قاعدة توزيع / AllocationRule | config | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-014 | هدف التوزيع / AllocationTarget | config | PRIVATE | REGISTERED | unchanged |
| ENT-FIN-015 | ربط الحساب / AccountMapping | config | PRIVATE | REGISTERED | ADDED |
| ENT-FIN-016 | بُعد سطر القاعدة / RuleLineDimension | config | PRIVATE | REGISTERED | ADDED |

Consumed
| Consumed entity | Owner ENT id | Owner module | HARD-FK / SOFT-READ | v2 |
|---|---|---|---|---|
| User | ENT-SEC-001 | SEC | HARD-FK (principal string) | unchanged |
| ModuleRegistry | ENT-SEC-004 | SEC | HARD-FK | unchanged |
| ScreenRegistry | ENT-SEC-005 | SEC | HARD-FK | + FIN_ACCOUNT_MAPPINGS screen row |
| ActionRegistry | ENT-SEC-006 | SEC | HARD-FK | + its VIEW / CREATE / UPDATE action rows |
| LookupType | ENT-MDL-001 | MDL | HARD-FK | + 2 types (15 FIN-owned) |
| LookupValue | ENT-MDL-002 | MDL | HARD-FK | + FIN_EVENT_BUSINESS_FIELD (4), FIN_DIMENSION_VALUE_SOURCE (2), PAYMENT_METHOD CHEQUE; ACCOUNT_DERIVATION_TYPE DIRECT retired |

Lookups owned
| Key | ENT | Values count | v2 |
|---|---|---|---|
| ACCOUNT_TYPE | ENT-FIN-001 | 5 | unchanged |
| DEBIT_CREDIT | ENT-FIN-001, ENT-FIN-005, ENT-FIN-010, ENT-FIN-012 | 2 | unchanged |
| PERIOD_STATE | ENT-FIN-008 | 4 | unchanged |
| FISCAL_YEAR_STATUS | ENT-FIN-007 | 2 | unchanged |
| JOURNAL_TYPE | ENT-FIN-004 | 5 | unchanged |
| JOURNAL_STATUS | ENT-FIN-004 | 3 | unchanged |
| ACCOUNTING_EVENT_TYPE | ENT-FIN-009, ENT-FIN-015 | 0 (host-defined) | now also used by ENT-FIN-015 |
| PAYMENT_METHOD | ENT-FIN-015 (businessValue when the field is PAYMENT_METHOD) | 1 seeded (CHEQUE) + host-defined | MODIFIED |
| ACCOUNT_DERIVATION_TYPE | ENT-FIN-010 | 2 (CONSTANT, MAPPING) | MODIFIED — DIRECT removed |
| AMOUNT_SOURCE_TYPE | ENT-FIN-010 | 3 | unchanged |
| DISTRIBUTION_TYPE | ENT-FIN-010, ENT-FIN-014 | 3 | unchanged |
| RECURRING_SCHEDULE_TYPE | ENT-FIN-011 | 2 | unchanged |
| RECURRING_FREQUENCY | ENT-FIN-011 | 4 | unchanged |
| FIN_EVENT_BUSINESS_FIELD | ENT-FIN-010, ENT-FIN-015, ENT-FIN-016 | 4 | ADDED |
| FIN_DIMENSION_VALUE_SOURCE | ENT-FIN-016 | 2 | ADDED |

Lookups consumed
none.

Screens
| SCR-REQ id | Name (ar/en) | Page code | v2 |
|---|---|---|---|
| SCR-REQ-FIN-001 | شجرة الحسابات / Chart of accounts | FIN_ACCOUNTS | unchanged |
| SCR-REQ-FIN-002 | تعريف الأبعاد وقيمها / Dimension definition & values | FIN_DIMENSIONS | unchanged |
| SCR-REQ-FIN-003 | قواعد المحرك / Engine rules | FIN_RULES | MODIFIED |
| SCR-REQ-FIN-004 | قوالب متكررة/عكسية / Recurring/reversing templates | FIN_RECURRING_TEMPLATES | unchanged |
| SCR-REQ-FIN-005 | قواعد التوزيع / Allocation rules | FIN_ALLOCATION_RULES | unchanged |
| SCR-REQ-FIN-006 | قيود اليومية / Journal entries | FIN_JOURNAL_ENTRIES | MODIFIED |
| SCR-REQ-FIN-007 | الفترات والسنوات المالية / Fiscal periods & years | FIN_PERIODS | unchanged |
| SCR-REQ-FIN-008 | دفتر الحساب / Account ledger | FIN_ACCOUNT_LEDGER | unchanged |
| SCR-REQ-FIN-009 | ميزان المراجعة / Trial balance | FIN_TRIAL_BALANCE | unchanged |
| SCR-REQ-FIN-010 | الميزانية العمومية / Balance sheet | FIN_BALANCE_SHEET | unchanged |
| SCR-REQ-FIN-011 | قائمة الدخل / Income statement | FIN_INCOME_STATEMENT | unchanged |
| SCR-REQ-FIN-012 | تقارير الأبعاد / Dimension reports | FIN_DIMENSION_REPORTS | unchanged |
| SCR-REQ-FIN-013 | روابط الحسابات / Account mappings | FIN_ACCOUNT_MAPPINGS | ADDED |

Requirements
REQ count: 77 · AC count: 83 · RULE count: 28 · ENT count: 16 · SCR-REQ count: 13
Last sequence per atom: REQ: 077 · AC: 083 · ENT: 016 · RULE: 028 · SCR-REQ: 013
v2 delta: ADDED REQ 31 · AC 37 · RULE 11 · ENT 2 · SCR-REQ 1; MODIFIED ENT 1 · REQ 1 · AC 1 · RULE 1 · SCR-REQ 2

REQ ids (full text in srs-fin.md → A4): REQ-FIN-001, REQ-FIN-002, REQ-FIN-003, REQ-FIN-004,
REQ-FIN-005, REQ-FIN-006, REQ-FIN-007, REQ-FIN-008, REQ-FIN-009, REQ-FIN-010, REQ-FIN-011,
REQ-FIN-012, REQ-FIN-013, REQ-FIN-014, REQ-FIN-015, REQ-FIN-016, REQ-FIN-017, REQ-FIN-018,
REQ-FIN-019, REQ-FIN-020, REQ-FIN-021, REQ-FIN-022, REQ-FIN-023, REQ-FIN-024, REQ-FIN-025,
REQ-FIN-026, REQ-FIN-027, REQ-FIN-028, REQ-FIN-029, REQ-FIN-030, REQ-FIN-031, REQ-FIN-032,
REQ-FIN-033, REQ-FIN-034, REQ-FIN-035, REQ-FIN-036, REQ-FIN-037, REQ-FIN-038, REQ-FIN-039,
REQ-FIN-040, REQ-FIN-041, REQ-FIN-042, REQ-FIN-043, REQ-FIN-044, REQ-FIN-045 (MODIFIED v2), REQ-FIN-046,
v2 ADDED: REQ-FIN-047, REQ-FIN-048, REQ-FIN-049, REQ-FIN-050, REQ-FIN-051, REQ-FIN-052, REQ-FIN-053,
REQ-FIN-054, REQ-FIN-055, REQ-FIN-056, REQ-FIN-057, REQ-FIN-058, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061,
REQ-FIN-062, REQ-FIN-063, REQ-FIN-064, REQ-FIN-065, REQ-FIN-066, REQ-FIN-067, REQ-FIN-068, REQ-FIN-069,
REQ-FIN-070, REQ-FIN-071, REQ-FIN-072, REQ-FIN-073, REQ-FIN-074, REQ-FIN-075, REQ-FIN-076, REQ-FIN-077

AC ids (full text in srs-fin.md → A4): AC-FIN-001, AC-FIN-002, AC-FIN-003,
AC-FIN-004, AC-FIN-005, AC-FIN-006, AC-FIN-007, AC-FIN-008, AC-FIN-009, AC-FIN-010,
AC-FIN-011, AC-FIN-012, AC-FIN-013, AC-FIN-014, AC-FIN-015, AC-FIN-016, AC-FIN-017,
AC-FIN-018, AC-FIN-019, AC-FIN-020, AC-FIN-021, AC-FIN-022, AC-FIN-023, AC-FIN-024,
AC-FIN-025, AC-FIN-026, AC-FIN-027, AC-FIN-028, AC-FIN-029, AC-FIN-030, AC-FIN-031,
AC-FIN-032, AC-FIN-033, AC-FIN-034, AC-FIN-035, AC-FIN-036, AC-FIN-037, AC-FIN-038,
AC-FIN-039, AC-FIN-040, AC-FIN-041, AC-FIN-042, AC-FIN-043, AC-FIN-044, AC-FIN-045 (MODIFIED v2),
AC-FIN-046,
v2 ADDED: AC-FIN-047, AC-FIN-048, AC-FIN-049, AC-FIN-050, AC-FIN-051, AC-FIN-052, AC-FIN-053,
AC-FIN-054, AC-FIN-055, AC-FIN-056, AC-FIN-057, AC-FIN-058, AC-FIN-059, AC-FIN-060, AC-FIN-061,
AC-FIN-062, AC-FIN-063, AC-FIN-064, AC-FIN-065, AC-FIN-066, AC-FIN-067, AC-FIN-068, AC-FIN-069,
AC-FIN-070, AC-FIN-071, AC-FIN-072, AC-FIN-073, AC-FIN-074, AC-FIN-075, AC-FIN-076, AC-FIN-077,
AC-FIN-078, AC-FIN-079, AC-FIN-080, AC-FIN-081, AC-FIN-082, AC-FIN-083

RULE ids (full text in srs-fin.md → A5): RULE-FIN-001, RULE-FIN-002, RULE-FIN-003,
RULE-FIN-004 (MODIFIED v2), RULE-FIN-005, RULE-FIN-006, RULE-FIN-007, RULE-FIN-008 (MODIFIED v2 — wording,
ADR-FIN-037), RULE-FIN-009,
RULE-FIN-010, RULE-FIN-011, RULE-FIN-012, RULE-FIN-013, RULE-FIN-014, RULE-FIN-015,
RULE-FIN-016, RULE-FIN-017,
v2 ADDED: RULE-FIN-018, RULE-FIN-019, RULE-FIN-020, RULE-FIN-021, RULE-FIN-022, RULE-FIN-023,
RULE-FIN-024, RULE-FIN-025, RULE-FIN-026, RULE-FIN-027, RULE-FIN-028

Decisions
ADR ids raised by P1 v2: ADR-FIN-023, ADR-FIN-024, ADR-FIN-025, ADR-FIN-026, ADR-FIN-027 and, at the pass-1
revision, ADR-FIN-037, ADR-FIN-038, ADR-FIN-039 — eight, all ACCEPTED (non-breaking).
BLOCKED: none.

Event
"P1 completed: FIN v2 — +2 entities (16), +31 requirements (77), +37 acceptance criteria (83), +11 rules (28), +1 screen requirement (13), 5 ADRs; MODIFIED ENT-FIN-010, REQ-FIN-045, AC-FIN-045, RULE-FIN-004, SCR-REQ-FIN-003, SCR-REQ-FIN-006"
══════════════════════════════════════════════════════════════════
