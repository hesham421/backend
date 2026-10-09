# API ID BINDING — FIN

> Written by this factory (P3.2 reconciliation, ADR-FIN-002), NOT published by the
> backend. It lived inside the consolidated api-docs file, where the first automated
> fetch would have erased it. It sits beside that file now: `gov.py fetch-inputs`
> regenerates the api-docs and never touches this one.

## API ID BINDING — added by P3.2 reconciliation (ADR-FIN-002)

The published generator emits no governance `API-*` id. This annex binds each
`API-FIN-*` of `backend-execution-plan-fin.md` to the endpoint this document actually
publishes, matched on verb + path. No endpoint is invented and no id is re-numbered.
Six of the 38 — API-FIN-033..038 — are not yet in `registry-exec-be-fin.md`'s
`API-FIN-001..032` range (ADR-FIN-002 records that gap as P3.1's to fix); all five are
bound below because all five are published and are exactly what the SRS Part B tables and
`backend-execution-plan-fin.md` already name.

| API id | Operation | Published verb · path | Bound |
|---|---|---|---|
| API-FIN-001 | search accounts | POST `/api/v1/fin/accounts/search` | ✓ |
| API-FIN-002 | create account | POST `/api/v1/fin/accounts` | ✓ |
| API-FIN-003 | update account | PUT `/api/v1/fin/accounts/{id}` | ✓ |
| API-FIN-004 | deactivate account | PUT `/api/v1/fin/accounts/{id}/deactivate` | ✓ |
| API-FIN-005 | search dimensions | POST `/api/v1/fin/dimensions/search` | ✓ |
| API-FIN-006 | create dimension | POST `/api/v1/fin/dimensions` | ✓ |
| API-FIN-007 | create dimension value | POST `/api/v1/fin/dimensions/{id}/values` | ✓ |
| API-FIN-008 | search dimension values | POST `/api/v1/fin/dimensions/values/search` | ✓ |
| API-FIN-009 | search event-type rules | POST `/api/v1/fin/event-rules/search` | ✓ |
| API-FIN-010 | create event-type rule | POST `/api/v1/fin/event-rules` | ✓ |
| API-FIN-011 | add rule line | POST `/api/v1/fin/event-rules/{id}/lines` | ✓ |
| API-FIN-012 | search recurring templates | POST `/api/v1/fin/recurring-templates/search` | ✓ |
| API-FIN-013 | create recurring template | POST `/api/v1/fin/recurring-templates` | ✓ |
| API-FIN-014 | run recurring template | POST `/api/v1/fin/recurring-templates/{id}/run` | ✓ |
| API-FIN-015 | search allocation rules | POST `/api/v1/fin/allocation-rules/search` | ✓ |
| API-FIN-016 | create allocation rule | POST `/api/v1/fin/allocation-rules` | ✓ |
| API-FIN-017 | run allocation rule | POST `/api/v1/fin/allocation-rules/{id}/run` | ✓ |
| API-FIN-018 | search journal entries | POST `/api/v1/fin/journal-entries/search` | ✓ |
| API-FIN-019 | create manual journal entry | POST `/api/v1/fin/journal-entries` | ✓ |
| API-FIN-020 | build journal entry from event (system) | POST `/api/v1/fin/journal-entries/from-event` | ✓ |
| API-FIN-021 | reverse journal entry | POST `/api/v1/fin/journal-entries/{id}/reverse` | ✓ |
| API-FIN-022 | read journal entry | GET `/api/v1/fin/journal-entries/{id}` | ✓ |
| API-FIN-023 | create fiscal year | POST `/api/v1/fin/fiscal-years` | ✓ |
| API-FIN-024 | open fiscal period | PATCH `/api/v1/fin/fiscal-periods/{id}/open` | ✓ |
| API-FIN-025 | soft-close fiscal period | PATCH `/api/v1/fin/fiscal-periods/{id}/soft-close` | ✓ |
| API-FIN-026 | hard-close fiscal period (approval) | PATCH `/api/v1/fin/fiscal-periods/{id}/hard-close` | ✓ |
| API-FIN-027 | run year-end close | POST `/api/v1/fin/fiscal-years/{id}/year-end-close` | ✓ |
| API-FIN-028 | account ledger | GET `/api/v1/fin/reports/account-ledger` | ✓ |
| API-FIN-029 | trial balance | GET `/api/v1/fin/reports/trial-balance` | ✓ |
| API-FIN-030 | balance sheet | GET `/api/v1/fin/reports/balance-sheet` | ✓ |
| API-FIN-031 | income statement | GET `/api/v1/fin/reports/income-statement` | ✓ |
| API-FIN-032 | dimension report | GET `/api/v1/fin/reports/dimension` | ✓ |
| API-FIN-033 | search fiscal periods | POST `/api/v1/fin/fiscal-periods/search` | ✓ (not yet in registry-exec-be range — ADR-FIN-002) |
| API-FIN-034 | deactivate event-type rule | PUT `/api/v1/fin/event-rules/{id}/deactivate` | ✓ (not yet in registry-exec-be range — ADR-FIN-002) |
| API-FIN-035 | deactivate dimension value | PUT `/api/v1/fin/dimensions/values/{id}/deactivate` | ✓ (not yet in registry-exec-be range — ADR-FIN-002) |
| API-FIN-036 | deactivate recurring template | PUT `/api/v1/fin/recurring-templates/{id}/deactivate` | ✓ (not yet in registry-exec-be range — ADR-FIN-002) |
| API-FIN-037 | deactivate allocation rule | PUT `/api/v1/fin/allocation-rules/{id}/deactivate` | ✓ (not yet in registry-exec-be range — ADR-FIN-002) |
| API-FIN-038 | search fiscal years | POST `/api/v1/fin/fiscal-years/search` | ✓ (published 2026-09-19; id provisional until the factory issues it) |

Coverage: 38 planned ids ↔ 38 published endpoints — no unbound id, no undocumented
endpoint. Operations the SRS names that this document publishes NO endpoint for are
listed in ADR-FIN-006; they are omitted from the frontend, never faked.
