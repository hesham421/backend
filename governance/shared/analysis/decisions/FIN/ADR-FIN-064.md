# ADR-FIN-064 — The republished api-docs carry CS-FIN-001's surface, so every binding ADR-FIN-060 left pending is bound to its published id, DTO and code; ADR-FIN-060 is superseded and ADR-FIN-061 narrows to the fiscal-year search
Status      : ACCEPTED (non-breaking)
Stage       : P3.2        Module: FIN        Version: v2
Lane        : analysis
Decided     : 2026-09-24
traces      : REQ-FIN-005, REQ-FIN-007, REQ-FIN-022, REQ-FIN-025, REQ-FIN-031, REQ-FIN-048, REQ-FIN-049, REQ-FIN-054, REQ-FIN-055, REQ-FIN-056, REQ-FIN-057, REQ-FIN-058, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061, REQ-FIN-062, REQ-FIN-064, REQ-FIN-065, REQ-FIN-075, SCR-FIN-002, SCR-FIN-003, SCR-FIN-004, SCR-FIN-005, SCR-FIN-006, SCR-FIN-007, SCR-FIN-013

## Context

The previous run of this stage (2026-09-23) stopped on ADR-FIN-060 (BLOCKED). The api-docs fetched then described
FIN's v1 as-built surface: no account-mapping endpoint, no v2 member on the add-rule-line request or response, none
of the v2 catalogue codes, and a contract id on only 32 of the served endpoints. ADR-FIN-060 named the way out:
implement the v2 backend, republish the api-docs, fetch them, and re-run P3.2.

That has happened. `_inputs/api-docs-fin.md` was re-fetched on 2026-09-23T20:36Z from backend commit `4cbe044…`.
Measured against the four gaps ADR-FIN-060 listed:

| # | ADR-FIN-060 gap | Republished api-docs |
|---|---|---|
| 1 | No account-mapping endpoint | Search, create, change account and deactivate are published as API-FIN-038, API-FIN-039, API-FIN-040 and API-FIN-041, gated by the page's VIEW, CREATE and UPDATE permissions. The search response carries the mapped account's code and names. |
| 2 | Add-rule-line request cannot express a MAPPING line | API-FIN-011's request carries `accountBusinessFieldCode` and a `dimensionTags` array, and `accountDerivationValue` is no longer required. |
| 3 | Rule and line responses carry no business field or tags | Every rule and line response carries both. |
| 4 | No v2 catalogue code; the retired MAPPING-unsupported code still listed | All twelve v2 codes are published with ar/en messages. The retired code is gone. The period-not-open code is listed once, with its message and the note that the rejection clears once the period is opened (AC-FIN-081). |

The catalog also now numbers the five endpoints ADR-FIN-061 cited by path (API-FIN-033 … API-FIN-037), matching
the P3.1 v2 registry. Its drift table reports one served endpoint without a contract id:
`POST /api/v1/fin/fiscal-years/search`, which P3.1's registry has never numbered.

## Decision

1. Every binding that read `PENDING ADR-FIN-060` is bound to the published id, DTO member and catalogue code. The
   plan cites nothing from the backend plan's contract draft.
2. ADR-FIN-060 is superseded. Its status changes from BLOCKED to SUPERSEDED. The stage no longer stops on it.
3. ADR-FIN-061 narrows. The five endpoints the api-docs now number are cited by id. Only the fiscal-year search is
   still cited by verb and path, for the reason ADR-FIN-061 gives.
4. The design does not change. Part A stood whole in the previous run. The only spec edits are the endpoint each
   screen names and two error states the republished catalogue now documents: FIN-404-DIMENSION on the dimension
   report and the not-found codes on account mappings.
5. The Line entry's Save is no longer disabled for a MAPPING or tagged line. SCR-FIN-013's calls are bound.

## Consequences

- Non-breaking. No operation, field, rule or permission is added or removed. The screens now call endpoints that
  exist.
- Every screen-facing REQ is bound to a published endpoint. The plan's UNMAPPED list is empty apart from the one
  path citation.
- The v2 test plans (ADR-FIN-063) bound some cases to the then-unpublished surface. They were written before this
  binding and should be checked against it at their next run. Test generation owns that decision, not this stage.
- P3.1 should still number `POST /api/v1/fin/fiscal-years/search` in its next delta (ADR-FIN-061).
