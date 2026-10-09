# ADR-FIN-060 — The fetched api-docs publish FIN's v1 as-built surface only: CS-FIN-001's account-mapping endpoints and the v2 rule-line request are not published, so SCR-FIN-013 has no endpoint to bind
Status      : SUPERSEDED by ADR-FIN-064 (was BLOCKED, 2026-09-23)
Stage       : P3.2        Module: FIN        Version: v2
Lane        : analysis
Decided     : 2026-09-23
traces      : REQ-FIN-048, REQ-FIN-049, REQ-FIN-054, REQ-FIN-055, REQ-FIN-056, REQ-FIN-057, REQ-FIN-058, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061, REQ-FIN-062, REQ-FIN-064, REQ-FIN-065, SCR-FIN-003, SCR-FIN-013

## Context

The frontend plan binds to the **real** API surface, `_inputs/api-docs-fin.md`, published by the
backend repo after implementation. The engine names it the only source for endpoint shape (P3.2 engine §0, §3.0).
The copy fetched for this run is from backend commit `7706fd851d91a04faf7edc8c8924de14fa52820b`,
fetched at 2026-09-23T12:00:56Z. It describes the v1 as-built backend. Measured against the approved v2 SRS and
the P3.1 v2 registry, it lacks the following:

| # | What CS-FIN-001 needs on a screen | What the fetched api-docs publish |
|---|---|---|
| 1 | Account-mapping search, create, change-account and deactivate (SCR-REQ-FIN-013 §B5; REQ-FIN-054…061). P3.1 declares them as four endpoints under `/api/v1/fin/account-mappings`. | No `account-mappings` endpoint of any kind. |
| 2 | A rule line that names its business field and carries its dimension tags (REQ-FIN-049, REQ-FIN-062; RULE-FIN-019 says a MAPPING line carries **no** account code). | API-FIN-011 publishes `RuleLineCreateRequest` without a business-field member or a tag array, and makes `accountDerivationValue` **required** on every line. |
| 3 | The rule's Detail pane shows a line's business field and tags. | `RuleLineResponse` and `EventTypeRuleResponse` carry neither. |
| 4 | The v2 rejections a user can meet on these two screens: RULE-FIN-018, 019, 022, 023, 024, 025 and 026. | The Known Error Codes table carries none of the twelve v2 codes. It still publishes `FIN-422-MAPPING-UNSUPPORTED`, which P3.1 v2 retires. |

Engine §3.0 settles how this is classified: "every REQ that needs an endpoint has one (missing/renamed → ADR —
naming diffs continue, **a missing core operation is breaking**)". Account-mapping maintenance is CS-FIN-001's
acceptance 3 and the whole purpose of SCR-REQ-FIN-013, so it is a core operation. Row 2 also contradicts an
upstream artifact outright: the published request cannot express a MAPPING line as RULE-FIN-019 defines one.

## Decision

The stage stops, and this ADR is BLOCKED.

The four stage artifacts are still written in full. The design can then be reviewed at the human decision
point, and the re-run needs no new design work:

- **Part A stands whole.** The flow diagram, the ui-ux-spec, SCR-FIN-013 and UXD-FIN-013…015 depend only on the
  SRS and the PRD (P3.2 engine §2).
- **Part B binds only what is published.** Every published endpoint is cited by its published id, or by
  its published verb and path where it has no id (ADR-FIN-061). Every binding owned by the unpublished surface is
  written `PENDING ADR-FIN-060`. No API id, DTO member name, length limit or catalog code is taken from the
  backend plan's contract draft into a binding. Where the plan names a v2 catalog code, it names it only as
  "P3.1 catalogue, not yet published".

## Consequences

To unblock, in this order:
1. Implement the FIN v2 backend package (`backend/modules/FIN/packages/v2/backend-execution`), then regenerate
   and publish `backend/modules/FIN/api-docs`.
2. Run `gov.py fetch-inputs` for FIN v2, so `_inputs/api-docs-fin.md` describes the v2 surface.
3. Re-run P3.2 on FIN v2. Each `PENDING ADR-FIN-060` becomes the published id, DTO and catalog code. The
   path-only citations of ADR-FIN-061 become ids wherever the regenerated api-docs carry them. The re-run
   raises the ADR that supersedes this one.

`gate:pass-2` must not open on this version of the plan. Nothing in this ADR is the frontend's to decide.
The missing surface is the backend's publication, and the frontend design already waits on it.

## Superseded (2026-09-24)

The backend published CS-FIN-001's surface, and the re-fetched api-docs (backend commit `4cbe044…`) carry
all four gaps above. The re-run of P3.2 bound every `PENDING ADR-FIN-060` to its published id, DTO and code.
See ADR-FIN-064. This ADR no longer stops the stage.
