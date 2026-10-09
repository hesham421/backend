# FIN v2 — api-verify problems report

Generated: 2026-09-24 · RUN_ID `195569` · tier **Full** (api-docs + test-execution-manifest-fin.md both present)
Target: `http://localhost:7272` (Dev/Test — localhost, per api-verify-config.md §4.1)

**126 passed · 0 failed** across 21 suites · 2 skipped (named below, never counted as passes) · 1 observation(s), which never affect the totals.

## Suites

| Suite | Passed | Failed |
|---|---|---|
| PREFLIGHT (stage A0) | 17 | 0 |
| Registration (SEC + MDL) | 3 | 0 |
| Dimension | 3 | 0 |
| Account | 8 | 0 |
| FiscalYear | 3 | 0 |
| FiscalPeriod | 6 | 0 |
| DimensionValue | 5 | 0 |
| AccountMapping (v2) | 8 | 0 |
| EventTypeRule + RuleLine (v2) | 11 | 0 |
| JournalEntry (manual) | 12 | 0 |
| JournalEntry (reversal) | 3 | 0 |
| JournalEntry (from event) | 8 | 0 |
| Event entry — MAPPING derivation (v2) | 10 | 0 |
| Event entry — dimension tags (v2) | 8 | 0 |
| Event entry — distribution / remainder | 3 | 0 |
| RecurringTemplate | 4 | 0 |
| AllocationRule | 4 | 0 |
| FiscalYear year-end close | 2 | 0 |
| Reports | 7 | 0 |
| Published catalogue | 1 | 0 |
| Permission gate (RULE-FIN-015) | 0 | 0 |

## Failures

_None._

## Preconditions (stage A0)

- PAYMENT_METHOD value TRANSFER exists but is INACTIVE in MDL, and MDL publishes no activate endpoint for a lookup value. TC-FIN-105's literal fixture (an inactive TRANSFER mapping) cannot be built; the case falls back to an EXPENSE_TYPE_CODE-keyed inactive third row, which demonstrates the same exclusion. Suggested SQL for a human, NOT executed: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = TRUE WHERE CODE = 'TRANSFER' AND LOOKUP_TYPE_ID = (SELECT LOOKUP_TYPE_PK FROM MDL_LOOKUP_TYPE WHERE KEY = 'PAYMENT_METHOD');

## Skipped — named, never counted as passes

- TC-FIN-038 — entry-creation permission alone does not buy a period close — not exercisable by api-verify (needs a principal WITHOUT PERM_FIN_PERIODS_CLOSE_APPROVE; minting a SEC user/role is outside SKILL.md §3-I). COVERED in the JUnit track: FinSoDCoverageIntegrationTest#hardClose_isDeniedForAPrincipalHoldingOnlyTheEntryCreationPermission
- TC-FIN-052 — the same permission gates the year-end close — same reason as TC-FIN-038. COVERED in the JUnit track: FinSoDCoverageIntegrationTest#yearEndClose_isDeniedForAPrincipalHoldingOnlyTheEntryCreationPermission

## Observations (stage E — not pass/fail)

- **stage E — a second hard-close on an already hard-closed period** — HTTP 409 / FIN-409-NOT-REOPENABLE (terminal state upheld; which of FIN-409-INVALID-TRANSITION or FIN-409-NOT-REOPENABLE is owed is undocumented)

## Surviving records

- JournalEntry: 26 entr(ies) [358, 359, 360, 361, 362, 363, 367, 368, 369, 370, 371, 372, 373, 374, 375, 376, 377, 378, 379, 380, 381, 382, 383, 384, 385, 386] — immutable by design (RULE-FIN-016, no delete or void route); left behind POSTED.
- AccountMapping: [48, 49, 50, 51, 52, 53, 54, 55, 56, 57] — deactivated (soft); FIN documents no hard delete.
- AllocationRule: [10] — deactivated (soft); FIN documents no hard delete.
- DimensionValue: [100, 101, 102, 103, 104, 105] — deactivated (soft); FIN documents no hard delete.
- RecurringTemplate: [11, 12] — deactivated (soft); FIN documents no hard delete.
- EventTypeRule: [110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121] — deactivated (soft); FIN documents no hard delete.
- Account: [1033, 1034, 1035, 1036, 1037, 1038, 1039, 1040, 1041, 1042] — deactivated (soft); FIN documents no hard delete.
- FiscalYear [287, 288, 289, 290, 291] and its 40 period(s) — no deactivate or delete endpoint exists (ENTITY CRUD CHECKLIST: FiscalYear's only retirement is year-end close); left behind.
- Dimension: [69, 70] — no deactivate endpoint exists (ENTITY CRUD CHECKLIST ENT-FIN-002 shows '—'); left behind ACTIVE.

### Permanent residue (reference tables other modules' rules read)

- MDL lookup value 155 (ACCOUNTING_EVENT_TYPE, run 195569) — retired via DELETE /api/v1/mdl/lookup-values/155 -> HTTP 200. Cleanup SQL if still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 155;
- MDL lookup value 156 (ACCOUNTING_EVENT_TYPE, run 195569) — retired via DELETE /api/v1/mdl/lookup-values/156 -> HTTP 200. Cleanup SQL if still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 156;
- MDL lookup value 157 (ACCOUNTING_EVENT_TYPE, run 195569) — retired via DELETE /api/v1/mdl/lookup-values/157 -> HTTP 200. Cleanup SQL if still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 157;
- MDL lookup value 158 (ACCOUNTING_EVENT_TYPE, run 195569) — retired via DELETE /api/v1/mdl/lookup-values/158 -> HTTP 200. Cleanup SQL if still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 158;
- MDL lookup value 159 (ACCOUNTING_EVENT_TYPE, run 195569) — retired via DELETE /api/v1/mdl/lookup-values/159 -> HTTP 200. Cleanup SQL if still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 159;
- MDL lookup value 160 (ACCOUNTING_EVENT_TYPE, run 195569) — retired via DELETE /api/v1/mdl/lookup-values/160 -> HTTP 200. Cleanup SQL if still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 160;
- MDL lookup value 161 (ACCOUNTING_EVENT_TYPE, run 195569) — retired via DELETE /api/v1/mdl/lookup-values/161 -> HTTP 200. Cleanup SQL if still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 161;
- MDL lookup value 162 (ACCOUNTING_EVENT_TYPE, run 195569) — retired via DELETE /api/v1/mdl/lookup-values/162 -> HTTP 200. Cleanup SQL if still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 162;
- MDL lookup value 163 (ACCOUNTING_EVENT_TYPE, run 195569) — retired via DELETE /api/v1/mdl/lookup-values/163 -> HTTP 200. Cleanup SQL if still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 163;
- MDL lookup value 164 (ACCOUNTING_EVENT_TYPE, run 195569) — retired via DELETE /api/v1/mdl/lookup-values/164 -> HTTP 200. Cleanup SQL if still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 164;
- MDL lookup value 165 (ACCOUNTING_EVENT_TYPE, run 195569) — retired via DELETE /api/v1/mdl/lookup-values/165 -> HTTP 200. Cleanup SQL if still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 165;
- MDL lookup value 166 (ACCOUNTING_EVENT_TYPE, run 195569) — retired via DELETE /api/v1/mdl/lookup-values/166 -> HTTP 200. Cleanup SQL if still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 166;

## Privileges

No permission grant was created or revoked by this run. The bootstrap `admin` holds SYS_ADMIN, which V24/V25/V30 and the v2 migration V37 already grant the FIN permission set to, so stage I was skipped entirely and no grant journal was written. No standing privilege was left behind.

