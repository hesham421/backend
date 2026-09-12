# FIN — api-verify problems report

Generated: 2026-09-12 · RUN_ID `217494` · tier **Full** (api-docs + test-execution-manifest present)
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

- **stage E — an unknown search FILTER field is silently ignored (MDL)** — unknown filter field -> HTTP 200, totalElements=17 (silently ignored rather than rejected; an unknown SORT field is rejected with FIN-400-INVALID-SORT)
- **stage E — a FIELD amount source the event does not carry** — amount field absent from the event -> HTTP 409 / DATA_INTEGRITY_VIOLATION (a raw persistence failure, not a FIN-* code)

## Surviving records

- JournalEntry: 17 entr(ies) [89, 90, 91, 97, 92, 94, 95, 93, 96, 98, 99, 100, 101, 103, 104, 105, 107] — immutable by design (RULE-FIN-016, no delete or void route); left behind POSTED.
- AllocationRule: [9] — deactivated (soft); FIN documents no hard delete.
- DimensionValue: [22, 23, 24] — deactivated (soft); FIN documents no hard delete.
- RecurringTemplate: [12, 13] — deactivated (soft); FIN documents no hard delete.
- EventTypeRule: [21, 22, 23, 24, 25] — deactivated (soft); FIN documents no hard delete.
- Account: [39, 40, 41, 42, 43] — deactivated (soft); FIN documents no hard delete.
- FiscalYear [16, 17] and its 24 period(s) — no deactivate or delete endpoint exists (ENTITY CRUD CHECKLIST: FiscalYear's only retirement is year-end close); left behind.
- Dimension: [19, 20] — no deactivate endpoint exists (ENTITY CRUD CHECKLIST ENT-FIN-002 shows '—'); left behind ACTIVE.

### Permanent residue (reference tables other modules' rules read)

- MDL lookup value 149 (ACCOUNTING_EVENT_TYPE / SALES_INVOICE_217494) — retired via DELETE /api/v1/mdl/lookup-values/149 -> HTTP 200. Cleanup SQL if it is still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 149;
- MDL lookup value 150 (ACCOUNTING_EVENT_TYPE / SALES_INVOICE_217494) — retired via DELETE /api/v1/mdl/lookup-values/150 -> HTTP 200. Cleanup SQL if it is still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 150;
- MDL lookup value 151 (ACCOUNTING_EVENT_TYPE / SALES_INVOICE_217494) — retired via DELETE /api/v1/mdl/lookup-values/151 -> HTTP 200. Cleanup SQL if it is still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 151;
- MDL lookup value 152 (ACCOUNTING_EVENT_TYPE / SALES_INVOICE_217494) — retired via DELETE /api/v1/mdl/lookup-values/152 -> HTTP 200. Cleanup SQL if it is still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 152;
- MDL lookup value 153 (ACCOUNTING_EVENT_TYPE / SALES_INVOICE_217494) — retired via DELETE /api/v1/mdl/lookup-values/153 -> HTTP 200. Cleanup SQL if it is still active: UPDATE MDL_LOOKUP_VALUE SET IS_ACTIVE_FL = FALSE WHERE LOOKUP_VALUE_PK = 153;

## Privileges

No permission grant was created or revoked by this run. The bootstrap `admin` holds SYS_ADMIN, which V24/V25/V30 already grant the FIN permission set to, so stage I was skipped entirely and no grant journal was written. No standing privilege was left behind.

