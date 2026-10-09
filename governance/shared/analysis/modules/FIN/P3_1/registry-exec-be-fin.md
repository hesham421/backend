## REGISTRY — P3.1 — FIN v1
══════════════════════════════════════════════════════════════════

ID RANGES
API-FIN-001 .. API-FIN-037 · QR-FIN-001 .. QR-FIN-049

Synchronized 2026-09-17 against the implemented backend
(`backend/src/main/java/com/erp/fin`) and `_inputs/api-docs-binding-fin.md`: five API ids
and five QR ids were minted at ALIGN-BE on 2026-09-12 (the four deactivate endpoints plus
the fiscal-period search, and the query ids their services use) but never reached this
registry — ADR-FIN-002 records the gap on the frontend side; this update closes it here,
at the source.

API ids: API-FIN-001, API-FIN-002, API-FIN-003, API-FIN-004, API-FIN-005, API-FIN-006,
API-FIN-007, API-FIN-008, API-FIN-009, API-FIN-010, API-FIN-011, API-FIN-012, API-FIN-013,
API-FIN-014, API-FIN-015, API-FIN-016, API-FIN-017, API-FIN-018, API-FIN-019, API-FIN-020,
API-FIN-021, API-FIN-022, API-FIN-023, API-FIN-024, API-FIN-025, API-FIN-026, API-FIN-027,
API-FIN-028, API-FIN-029, API-FIN-030, API-FIN-031, API-FIN-032, API-FIN-033, API-FIN-034,
API-FIN-035, API-FIN-036, API-FIN-037

QR ids: QR-FIN-001, QR-FIN-002, QR-FIN-003, QR-FIN-004, QR-FIN-005, QR-FIN-006, QR-FIN-007,
QR-FIN-008, QR-FIN-009, QR-FIN-010, QR-FIN-011, QR-FIN-012, QR-FIN-013, QR-FIN-014,
QR-FIN-015, QR-FIN-016, QR-FIN-017, QR-FIN-018, QR-FIN-019, QR-FIN-020, QR-FIN-021,
QR-FIN-022, QR-FIN-023, QR-FIN-024, QR-FIN-025, QR-FIN-026, QR-FIN-027, QR-FIN-028,
QR-FIN-029, QR-FIN-030, QR-FIN-031, QR-FIN-032, QR-FIN-033, QR-FIN-034, QR-FIN-035,
QR-FIN-036, QR-FIN-037, QR-FIN-038, QR-FIN-039, QR-FIN-040, QR-FIN-041, QR-FIN-042,
QR-FIN-043, QR-FIN-044, QR-FIN-045, QR-FIN-046, QR-FIN-047, QR-FIN-048, QR-FIN-049

ENTITIES / TABLES bound
All 14 ENT-FIN-001..014 bound to their db-script tables (147 DBF ids — DBF-FIN-147,
FIN_ACCOUNT.is_retained_earnings_fl, added by migration V23 after this registry's original
147→146 count; see db-script-fin.md §1 total note); lookups: 13 FIN-owned keys registered
into MDL (2 additional values, CLOSING/OPENING, added to JOURNAL_TYPE this stage —
data-only, non-breaking).

XM STATUS
open: none · deferred: none · active: XM-FIN-001 (SOFT-READ → MDL, target already gated).
XM-FIN-002 (READ → SEC user directory) was assigned at ALIGN-BE and RETIRED 2026-09-12 with
the deletion of `FinSeparationOfDutiesService` (RULE-FIN-015 is satisfied in full by the
`PERM_FIN_PERIODS_CLOSE_APPROVE` gate alone).

CATALOG
41 error-catalog rows (three-way verified 2026-09-12 against `FinErrorCodes.java`,
`messages.properties` and `messages_ar.properties`: 40/40/40 live codes, plus the struck
never-declared-service-unavailable row kept visible for its history). Rules without a
message: none — every RULE-FIN-001..017 that produces a user-facing message has a full
ar/en pair. `FIN-403-SOD-VIOLATION` is struck (unreachable, both throw sites deleted) but
its constant and both bundle entries remain in the build, live but unthrown — recorded as
an open question for a human, not this stage's to resolve.

ALIGN
See backend-execution-plan-fin.md → PHASE 8 (ALIGN-BE) for the current, line-numbered
self-check narrative (API/DBF/SECURITY/SCOPE/SRS-OPERATIONS/DEACTIVATE-RUN/MIRROR-DIVERGENCE
rows) — this line no longer hand-carries a verdict number so it cannot drift from that
narrative the way the API/QR ranges above just had to be corrected.

ADRs
erp/decisions/FIN/ADR-FIN-001.md (ACCEPTED, carried from P2; no new ADR this stage).

TRACEABILITY
REQ covered by ≥1 API/DBF: 46/46 (every REQ-FIN-001..046 appears in ≥1 API block's
traces= or ≥1 DBF record's traces in db-script-fin.md). Orphan REQ: none.

Event
"P3.1 registry synchronized 2026-09-17: FIN v1 — 37 API, 49 QR, 147 DBF, 41 catalog rows,
1 active XM (XM-FIN-002 retired) — corrected against the implemented backend after the
range drifted behind five ALIGN-BE-minted ids (API-FIN-033..037, QR-FIN-045..049)"
══════════════════════════════════════════════════════════════════
