## REGISTRY — P2 — FIN v2
══════════════════════════════════════════════════════════════════
Change set : CS-FIN-001 (ADDITIVE), a delta on v1. This registry is the complete current P2
             registry: v1 rows are restated and v2 rows marked, so the merge into project-registry.md
             needs no v1 lookup.

Tables
| Table | ENT id | Kind | DBF range | v2 |
|---|---|---|---|---|
| FIN_ACCOUNT | ENT-FIN-001 | master | DBF-FIN-001 … DBF-FIN-013, DBF-FIN-147 | unchanged |
| FIN_DIMENSION | ENT-FIN-002 | config | DBF-FIN-014 … DBF-FIN-022 | unchanged |
| FIN_DIMENSION_VALUE | ENT-FIN-003 | lookup | DBF-FIN-023 … DBF-FIN-033 | unchanged |
| FIN_JOURNAL_ENTRY | ENT-FIN-004 | transactional | DBF-FIN-034 … DBF-FIN-050 | DBF-FIN-041 MODIFIED (traces only) |
| FIN_JOURNAL_LINE | ENT-FIN-005 | transactional | DBF-FIN-051 … DBF-FIN-060 | unchanged |
| FIN_JOURNAL_LINE_DIM | ENT-FIN-006 | transactional | DBF-FIN-061 … DBF-FIN-064 | unchanged (now also written for event-built lines) |
| FIN_FISCAL_YEAR | ENT-FIN-007 | master | DBF-FIN-065 … DBF-FIN-074 | unchanged |
| FIN_FISCAL_PERIOD | ENT-FIN-008 | master | DBF-FIN-075 … DBF-FIN-088 | unchanged |
| FIN_EVENT_TYPE_RULE | ENT-FIN-009 | config | DBF-FIN-089 … DBF-FIN-097 | unchanged |
| FIN_RULE_LINE | ENT-FIN-010 | config | DBF-FIN-098 … DBF-FIN-108, DBF-FIN-148 | MODIFIED — DBF-FIN-101, DBF-FIN-102 MODIFIED; DBF-FIN-148 ADDED; CHK_FIN_RULE_LINE_DERIVATION_SPEC |
| FIN_RECURRING_TEMPLATE | ENT-FIN-011 | config | DBF-FIN-109 … DBF-FIN-121 | unchanged |
| FIN_RECURRING_TEMPLATE_LINE | ENT-FIN-012 | config | DBF-FIN-122 … DBF-FIN-129 | unchanged |
| FIN_ALLOCATION_RULE | ENT-FIN-013 | config | DBF-FIN-130 … DBF-FIN-138 | unchanged |
| FIN_ALLOCATION_TARGET | ENT-FIN-014 | config | DBF-FIN-139 … DBF-FIN-146 | unchanged |
| FIN_ACCOUNT_MAPPING | ENT-FIN-015 | config | DBF-FIN-149 … DBF-FIN-158 | ADDED |
| FIN_RULE_LINE_DIM | ENT-FIN-016 | config | DBF-FIN-159 … DBF-FIN-165 | ADDED |

DBF ids (full detail in db-script-fin.md → §1): DBF-FIN-001, DBF-FIN-002, DBF-FIN-003,
DBF-FIN-004, DBF-FIN-005, DBF-FIN-006, DBF-FIN-007,
DBF-FIN-008, DBF-FIN-009, DBF-FIN-010, DBF-FIN-011, DBF-FIN-012, DBF-FIN-013, DBF-FIN-014,
DBF-FIN-015, DBF-FIN-016, DBF-FIN-017, DBF-FIN-018, DBF-FIN-019, DBF-FIN-020, DBF-FIN-021,
DBF-FIN-022, DBF-FIN-023, DBF-FIN-024, DBF-FIN-025, DBF-FIN-026, DBF-FIN-027, DBF-FIN-028,
DBF-FIN-029, DBF-FIN-030, DBF-FIN-031, DBF-FIN-032, DBF-FIN-033, DBF-FIN-034, DBF-FIN-035,
DBF-FIN-036, DBF-FIN-037, DBF-FIN-038, DBF-FIN-039, DBF-FIN-040, DBF-FIN-041, DBF-FIN-042,
DBF-FIN-043, DBF-FIN-044, DBF-FIN-045, DBF-FIN-046, DBF-FIN-047, DBF-FIN-048, DBF-FIN-049,
DBF-FIN-050, DBF-FIN-051, DBF-FIN-052, DBF-FIN-053, DBF-FIN-054, DBF-FIN-055, DBF-FIN-056,
DBF-FIN-057, DBF-FIN-058, DBF-FIN-059, DBF-FIN-060, DBF-FIN-061, DBF-FIN-062, DBF-FIN-063,
DBF-FIN-064, DBF-FIN-065, DBF-FIN-066, DBF-FIN-067, DBF-FIN-068, DBF-FIN-069, DBF-FIN-070,
DBF-FIN-071, DBF-FIN-072, DBF-FIN-073, DBF-FIN-074, DBF-FIN-075, DBF-FIN-076, DBF-FIN-077,
DBF-FIN-078, DBF-FIN-079, DBF-FIN-080, DBF-FIN-081, DBF-FIN-082, DBF-FIN-083, DBF-FIN-084,
DBF-FIN-085, DBF-FIN-086, DBF-FIN-087, DBF-FIN-088, DBF-FIN-089, DBF-FIN-090, DBF-FIN-091,
DBF-FIN-092, DBF-FIN-093, DBF-FIN-094, DBF-FIN-095, DBF-FIN-096, DBF-FIN-097, DBF-FIN-098,
DBF-FIN-099, DBF-FIN-100, DBF-FIN-101, DBF-FIN-102, DBF-FIN-103, DBF-FIN-104, DBF-FIN-105,
DBF-FIN-106, DBF-FIN-107, DBF-FIN-108, DBF-FIN-109, DBF-FIN-110, DBF-FIN-111, DBF-FIN-112,
DBF-FIN-113, DBF-FIN-114, DBF-FIN-115, DBF-FIN-116, DBF-FIN-117, DBF-FIN-118, DBF-FIN-119,
DBF-FIN-120, DBF-FIN-121, DBF-FIN-122, DBF-FIN-123, DBF-FIN-124, DBF-FIN-125, DBF-FIN-126,
DBF-FIN-127, DBF-FIN-128, DBF-FIN-129, DBF-FIN-130, DBF-FIN-131, DBF-FIN-132, DBF-FIN-133,
DBF-FIN-134, DBF-FIN-135, DBF-FIN-136, DBF-FIN-137, DBF-FIN-138, DBF-FIN-139, DBF-FIN-140,
DBF-FIN-141, DBF-FIN-142, DBF-FIN-143, DBF-FIN-144, DBF-FIN-145, DBF-FIN-146, DBF-FIN-147,
v2 ADDED: DBF-FIN-148, DBF-FIN-149, DBF-FIN-150, DBF-FIN-151, DBF-FIN-152, DBF-FIN-153, DBF-FIN-154,
DBF-FIN-155, DBF-FIN-156, DBF-FIN-157, DBF-FIN-158, DBF-FIN-159, DBF-FIN-160, DBF-FIN-161,
DBF-FIN-162, DBF-FIN-163, DBF-FIN-164, DBF-FIN-165
v2 MODIFIED: DBF-FIN-041, DBF-FIN-101, DBF-FIN-102

XM index
| XM id | Type | From | To | Status | v2 |
|---|---|---|---|---|---|
| XM-FIN-001 | SOFT-READ | FIN | MDL | ACTIVE | MODIFIED — scope widened to the 6 v2 lookup-backed columns and 15 keys; same target, same type |
| ~~XM-FIN-002~~ | READ | FIN | SEC | RETIRED 2026-09-12 (see db-script-fin.md §2) | unchanged |

Lookups
| Key | Seeded values count | Owner | v2 |
|---|---|---|---|
| ACCOUNT_TYPE | 5 | FIN | unchanged |
| DEBIT_CREDIT | 2 | FIN | unchanged |
| PERIOD_STATE | 4 | FIN | unchanged |
| FISCAL_YEAR_STATUS | 2 | FIN | unchanged |
| JOURNAL_TYPE | 5 | FIN | unchanged |
| JOURNAL_STATUS | 3 | FIN | unchanged |
| ACCOUNTING_EVENT_TYPE | 0 (host-defined) | FIN | unchanged; now also used by FIN_ACCOUNT_MAPPING.event_type_code |
| PAYMENT_METHOD | 1 (CHEQUE) + host-defined | FIN | MODIFIED — CHEQUE seeded (BLOCK 8) |
| ACCOUNT_DERIVATION_TYPE | 2 active (CONSTANT, MAPPING); DIRECT deactivated | FIN | MODIFIED — DIRECT soft-retired (BLOCK 8) |
| AMOUNT_SOURCE_TYPE | 3 | FIN | unchanged |
| DISTRIBUTION_TYPE | 3 | FIN | unchanged |
| RECURRING_SCHEDULE_TYPE | 2 | FIN | unchanged |
| RECURRING_FREQUENCY | 4 | FIN | unchanged |
| FIN_EVENT_BUSINESS_FIELD | 4 | FIN | ADDED (BLOCK 8) |
| FIN_DIMENSION_VALUE_SOURCE | 2 | FIN | ADDED (BLOCK 8) |
The 13 v1 keys were seeded through MDL's API at onboarding (REQ-FIN-045). The v2 changes also have
an idempotent SQL seed block against MDL_LOOKUP_TYPE / MDL_LOOKUP_VALUE (db-script BLOCK 8,
ADR-FIN-028). The tables are MDL's; the keys, and so the seed, are FIN's.

Sequences
Last DBF: DBF-FIN-165 · Last XM: XM-FIN-002 (retired 2026-09-12; the id is burned, not reused. The
live XM set is XM-FIN-001 alone.)
Physical sequences ADDED: SEQ_FIN_ACCOUNT_MAPPING, SEQ_FIN_RULE_LINE_DIM (16 per-table sequences in all)

Decisions
ADR-FIN-001 (ACCEPTED, non-breaking, v1, unchanged) — see analysis/decisions/FIN/ADR-FIN-001.md
ADR-FIN-028 (ACCEPTED, non-breaking, raised by P2 v2) — see analysis/decisions/FIN/ADR-FIN-028.md
BLOCKED: none.

Event
"P2 completed: FIN v2 — 16 tables (+2), 165 DBF (+18 ADDED, 3 MODIFIED), 2 XM ids (XM-FIN-001 MODIFIED; 1 live); migration on v1; 1 ADR"

Cascade
No registry XM row anywhere in the platform targets FIN with status DEFERRED, so there is nothing
to resolve. XM-FIN-001 stays ACTIVE, because MDL v1 is gated. Its v2 widening needs no MDL-side
change: MDL receives FIN's new keys as data. v2 adds no XM towards SEC. Per ADR-FIN-001, the
FIN_ACCOUNT_MAPPINGS screen and its actions are registered as data through SEC's onboarding, not
as an XM.
══════════════════════════════════════════════════════════════════
