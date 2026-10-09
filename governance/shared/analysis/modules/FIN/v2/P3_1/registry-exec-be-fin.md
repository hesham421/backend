## REGISTRY — P3.1 — FIN v2
══════════════════════════════════════════════════════════════════
Change set : CS-FIN-001 (ADDITIVE), a delta on v1. This registry is the complete current P3.1 registry: the v1 rows
             are restated and the v2 rows marked, so the fold needs no v1 lookup.

ID RANGES
API-FIN-001 .. API-FIN-041 · QR-FIN-001 .. QR-FIN-061
v2 ADDED: API-FIN-038, API-FIN-039, API-FIN-040, API-FIN-041 · QR-FIN-050, QR-FIN-051, QR-FIN-052, QR-FIN-053,
QR-FIN-054, QR-FIN-055, QR-FIN-056, QR-FIN-057, QR-FIN-058, QR-FIN-059, QR-FIN-060, QR-FIN-061
v2 MODIFIED in substance: API-FIN-011, API-FIN-020, QR-FIN-015, QR-FIN-025, QR-FIN-026; every other v1 API block in
annotation only (ADR-FIN-029). Nothing renumbered, no sequence restarted.

API ids: API-FIN-001, API-FIN-002, API-FIN-003, API-FIN-004, API-FIN-005, API-FIN-006, API-FIN-007, API-FIN-008,
API-FIN-009, API-FIN-010, API-FIN-011, API-FIN-012, API-FIN-013, API-FIN-014, API-FIN-015, API-FIN-016, API-FIN-017,
API-FIN-018, API-FIN-019, API-FIN-020, API-FIN-021, API-FIN-022, API-FIN-023, API-FIN-024, API-FIN-025, API-FIN-026,
API-FIN-027, API-FIN-028, API-FIN-029, API-FIN-030, API-FIN-031, API-FIN-032, API-FIN-033, API-FIN-034, API-FIN-035,
API-FIN-036, API-FIN-037, API-FIN-038, API-FIN-039, API-FIN-040, API-FIN-041

QR ids: QR-FIN-001, QR-FIN-002, QR-FIN-003, QR-FIN-004, QR-FIN-005, QR-FIN-006, QR-FIN-007, QR-FIN-008, QR-FIN-009,
QR-FIN-010, QR-FIN-011, QR-FIN-012, QR-FIN-013, QR-FIN-014, QR-FIN-015, QR-FIN-016, QR-FIN-017, QR-FIN-018,
QR-FIN-019, QR-FIN-020, QR-FIN-021, QR-FIN-022, QR-FIN-023, QR-FIN-024, QR-FIN-025, QR-FIN-026, QR-FIN-027,
QR-FIN-028, QR-FIN-029, QR-FIN-030, QR-FIN-031, QR-FIN-032, QR-FIN-033, QR-FIN-034, QR-FIN-035, QR-FIN-036,
QR-FIN-037, QR-FIN-038, QR-FIN-039, QR-FIN-040, QR-FIN-041, QR-FIN-042, QR-FIN-043, QR-FIN-044, QR-FIN-045,
QR-FIN-046, QR-FIN-047, QR-FIN-048, QR-FIN-049, QR-FIN-050, QR-FIN-051, QR-FIN-052, QR-FIN-053, QR-FIN-054,
QR-FIN-055, QR-FIN-056, QR-FIN-057, QR-FIN-058, QR-FIN-059, QR-FIN-060, QR-FIN-061

History of the v1 ranges, kept: API-FIN-033 … API-FIN-037 and QR-FIN-045 … QR-FIN-049 were minted at ALIGN-BE on
2026-09-12 and reached this registry on 2026-09-17, when it was synchronized against the implemented backend.

ENTITIES / TABLES bound
All 16 entities, ENT-FIN-001 … ENT-FIN-016, are bound to their db-script tables and all 165 DBF ids, through DBF-FIN-165,
carry a DB Alignment Manifest row. v2 adds FIN_ACCOUNT_MAPPING (ENT-FIN-015, SEQ_FIN_ACCOUNT_MAPPING) and
FIN_RULE_LINE_DIM (ENT-FIN-016, SEQ_FIN_RULE_LINE_DIM). The v1 bindings now name each table's sequence instead of an
identity clause. DBF-FIN-147 is seeded as data, never written by an API.
Lookups: 15 FIN-owned keys, all runtime-loaded from MDL. Reused: the 13 v1 keys. New in v2: FIN_EVENT_BUSINESS_FIELD,
FIN_DIMENSION_VALUE_SOURCE. v2 also changes ACCOUNT_DERIVATION_TYPE (DIRECT retired) and PAYMENT_METHOD (CHEQUE
seeded). v1 ALIGN-BE had already added CLOSING and OPENING to JOURNAL_TYPE as data.

XM STATUS
open: none · deferred: none · active: XM-FIN-001 (SOFT-READ → MDL, ACTIVE; MODIFIED v2 — six more columns, 15 keys).
XM-FIN-002 (READ → SEC) RETIRED 2026-09-12 with `FinSeparationOfDutiesService`; the id is burned. v2 mints no XM.

CATALOG
53 rows: 41 from v1 (40 live codes plus the struck, never-declared service-unavailable row) and 12 added in v2, of
which 11 carry a RULE-FIN-018 … RULE-FIN-028 message and one, FIN-404-ACCOUNT-MAPPING, is PLATFORM-STD. Struck and
unreachable: FIN-403-SOD-VIOLATION (since 2026-09-12) and FIN-422-MAPPING-UNSUPPORTED (retired in v2). Rules without a
message: none — every rule that produces a user-facing message has a full ar/en pair.
Published for the event consumer (REQ-FIN-075): FIN-409-DUPLICATE-EVENT (acknowledge), FIN-409-PERIOD-NOT-OPEN
(wait and retry), every other API-FIN-020 code (fail, needs a fix).

ALIGN
The verdict is stamped by the orchestrator from the analyze report into the ALIGN block of backend-execution-plan-fin.md
(PHASE 8, ALIGN-BE); this registry carries no hand-counted verdict. Fixed in v2: three wrong DBF citations of v1
(the remainder marker cited as DBF-FIN-103 and DBF-FIN-141 instead of DBF-FIN-107 and DBF-FIN-146), and the v1 PK
bindings that named an identity clause no migration built.

ADRs
decisions/FIN/ADR-FIN-029.md — v2 plan restated in full, v1 blocks brought to the current template (ACCEPTED, non-breaking)
decisions/FIN/ADR-FIN-030.md — v2 error codes, store-refusal translation, FIN-404-ACCOUNT-MAPPING (ACCEPTED, non-breaking)
decisions/FIN/ADR-FIN-031.md — event business fields in `fields{}`; mapping search joins the account (ACCEPTED, non-breaking)
Carried: decisions/FIN/ADR-FIN-001.md (ACCEPTED). BLOCKED: none.

TRACEABILITY
REQ covered by ≥1 API/DBF: 77/77 — every REQ-FIN-001 … REQ-FIN-077 is in the traces of at least one API block or one
DBF record of db-script-fin.md. v2 requirements with no DBF trace are carried by API traces: REQ-FIN-047, REQ-FIN-058,
REQ-FIN-067, REQ-FIN-070, REQ-FIN-071, REQ-FIN-073, REQ-FIN-074, REQ-FIN-075, REQ-FIN-076, REQ-FIN-077. Orphan REQ: none.
Rules RULE-FIN-001 … RULE-FIN-017 as in v1; RULE-FIN-018 … RULE-FIN-028 each enforced by one endpoint and one
catalog row.

Event
"P3.1 completed: FIN v2 — 41 API (+4), 61 QR (+12), 165 DBF bound, 53 catalog rows (+12, 1 retired), 1 active XM
(XM-FIN-001 widened), 3 ADRs (ADR-FIN-029 … ADR-FIN-031); API-FIN-011 and API-FIN-020 modified; v1 blocks annotated"
══════════════════════════════════════════════════════════════════
