# ADR-FIN-061 — Served endpoints that the api-docs publish without a contract id are cited by verb and path; the v1 plan's provisional API-FIN-038 is withdrawn
Status      : ACCEPTED
Stage       : P3.2        Module: FIN        Version: v2
Lane        : analysis
Decided     : 2026-09-23
traces      : REQ-FIN-005, REQ-FIN-007, REQ-FIN-022, REQ-FIN-025, REQ-FIN-031, SCR-FIN-002, SCR-FIN-003, SCR-FIN-004, SCR-FIN-005, SCR-FIN-006, SCR-FIN-007

## Context

The catalog in `_inputs/api-docs-fin.md` gives a contract id to 32 of the 38 served endpoints (API-FIN-001…032).
Its drift table reports the other six as "undeclared":

| Published endpoint | P3.1 v2 registry id | v1 frontend plan cited it as |
|---|---|---|
| `POST /api/v1/fin/fiscal-periods/search` | API-FIN-033 | API-FIN-033 (ADR-FIN-002 binding annex) |
| `PUT /api/v1/fin/event-rules/{id}/deactivate` | API-FIN-034 | API-FIN-034 (annex) |
| `PUT /api/v1/fin/dimensions/values/{id}/deactivate` | API-FIN-035 | API-FIN-035 (annex) |
| `PUT /api/v1/fin/recurring-templates/{id}/deactivate` | API-FIN-036 | API-FIN-036 (annex) |
| `PUT /api/v1/fin/allocation-rules/{id}/deactivate` | API-FIN-037 | API-FIN-037 (annex) |
| `POST /api/v1/fin/fiscal-years/search` | **none** | API-FIN-038, a provisional id (ADR-FIN-006) |

Two facts changed after v1:
- `gov.py analyze` C9.5 now accepts only ids that the **fetched api-docs** define. The companion binding annex is
  not an input it reads. As a result, the folded v1 plan fails six CRITICAL clauses before this stage writes anything.
- P3.1 v2 has minted **API-FIN-038 for the account-mapping search**. The v1 plan's provisional API-FIN-038 now
  names a different endpoint.

## Decision

The frontend plan cites each of these six endpoints by its published verb and path, and by no id that the api-docs do not
carry. All six are published and served, and the frontend uses all six exactly as in v1. The P3.1 ids for the first five
are kept in `registry-exec-fe-fin.md` (API COVERAGE) as a cross-reference only.

The fiscal-year search stays the source of SCR-FIN-007's Master pane and of the required `fiscalYearId`
inputs of SCR-FIN-010 and SCR-FIN-011. No id exists for it in the api-docs or in P3.1's registry. That is a gap in
P3.1's API REGISTRY (a served endpoint with no contract id), recorded here for P3.1. The frontend does not mint an id,
because it owns no `API-*`.

This ADR supersedes two things:
- ADR-FIN-002's binding annex, as a source of API ids.
- The sentence of ADR-FIN-006 that names the fiscal-year search "API-FIN-038". The rest of ADR-FIN-006 stands.

## Consequences

- The change is non-breaking. Only the naming differs, and every operation is published (engine §3.0, "naming diffs
  continue").
- When regenerated api-docs carry these ids, the path citations become ids in the same re-run that ADR-FIN-060
  requires. The binding does not change.
- P3.1 should number `POST /api/v1/fin/fiscal-years/search` in its next delta.

## Narrowed (2026-09-24, ADR-FIN-064)

The republished api-docs number the first five endpoints of the table above as API-FIN-033 … API-FIN-037,
and the frontend plan now cites them by id. Only `POST /api/v1/fin/fiscal-years/search` is still published
without a contract id, so it alone is still cited by verb and path.
