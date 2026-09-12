# FIN — api-verify problems report

Generated: 2026-09-12 · RUN_ID `223696` · tier **Full** (api-docs + test-execution-manifest present)
Target: `http://localhost:7272` (Dev/Test — localhost, per api-verify-config.md §4.1)

**124 passed · 0 failed** across 14 suites · 2 observation(s), which never affect the totals.

## Suites

| Suite | Passed | Failed |
|---|---|---|
| PREFLIGHT (stage A0) | 14 | 0 |
| Dimension | 4 | 0 |
| Account | 12 | 0 |
| FiscalYear | 5 | 0 |
| FiscalPeriod | 12 | 0 |
| EventTypeRule | 9 | 0 |
| DimensionValue | 5 | 0 |
| JournalEntry | 23 | 0 |
| JournalEntry (from event) | 7 | 0 |
| EventTypeRule (remainder distribution) | 5 | 0 |
| RecurringTemplate | 8 | 0 |
| AllocationRule | 7 | 0 |
| Reports | 11 | 0 |
| FiscalYear year-end close | 2 | 0 |

## Failures

_None._

## Preconditions (stage A0)

_All preconditions satisfied._

## Observations (stage E — not pass/fail)

- **stage E — an unknown search FILTER field is silently ignored (MDL)** — unknown filter field -> HTTP 200, totalElements=35 (silently ignored rather than rejected; an unknown SORT field is rejected with FIN-400-INVALID-SORT)
- **stage E — a FIELD amount source the event does not carry** — amount field absent from the event -> HTTP 409 / DATA_INTEGRITY_VIOLATION (a raw persistence failure, not a FIN-* code)

## Surviving records

- JournalEntry: 17 entr(ies) [274, 275, 276, 277, 279, 282, 278, 281, 280, 283, 284, 285, 286, 288, 289, 290, 292] — immutable by design (RULE-FIN-016, no delete or void route); left behind POSTED.
- AllocationRule: [12] — deactivated (soft); FIN documents no hard delete.
- DimensionValue: [31, 32, 33] — deactivated (soft); FIN documents no hard delete.
- RecurringTemplate: [18, 19] — deactivated (soft); FIN documents no hard delete.
- EventTypeRule: [36, 37, 38, 39, 40] — deactivated (soft); FIN documents no hard delete.
- Account: [190, 191, 192, 193, 194] — deactivated (soft); FIN documents no hard delete.
- FiscalYear [126, 127] and its 24 period(s) — no deactivate or delete endpoint exists (ENTITY CRUD CHECKLIST: FiscalYear's only retirement is year-end close); left behind.
- Dimension: [25, 26] — no deactivate endpoint exists (ENTITY CRUD CHECKLIST ENT-FIN-002 shows '—'); left behind ACTIVE.

### Permanent residue (reference tables other modules' rules read)

- MDL lookup value 200 (ACCOUNTING_EVENT_TYPE / SALES_INVOICE_223696) — retired via DELETE /api/v1/mdl/lookup-values/200 -> HTTP 200. Cleanup SQL if it is still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 200;
- MDL lookup value 201 (ACCOUNTING_EVENT_TYPE / SALES_INVOICE_223696) — retired via DELETE /api/v1/mdl/lookup-values/201 -> HTTP 200. Cleanup SQL if it is still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 201;
- MDL lookup value 202 (ACCOUNTING_EVENT_TYPE / SALES_INVOICE_223696) — retired via DELETE /api/v1/mdl/lookup-values/202 -> HTTP 200. Cleanup SQL if it is still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 202;
- MDL lookup value 203 (ACCOUNTING_EVENT_TYPE / SALES_INVOICE_223696) — retired via DELETE /api/v1/mdl/lookup-values/203 -> HTTP 200. Cleanup SQL if it is still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 203;
- MDL lookup value 204 (ACCOUNTING_EVENT_TYPE / SALES_INVOICE_223696) — retired via DELETE /api/v1/mdl/lookup-values/204 -> HTTP 200. Cleanup SQL if it is still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 204;

## Privileges

No permission grant was created or revoked by this run. The bootstrap `admin` holds SYS_ADMIN, which V24/V25/V30 already grant the FIN permission set to, so stage I was skipped entirely and no grant journal was written. No standing privilege was left behind.

