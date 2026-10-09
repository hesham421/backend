# ADR-FIN-031 — Event business fields travel in the existing `fields` map; the mapping search joins the account

Module  : FIN     Version : v2     Stage raised : P3.1 (Backend Execution Plan)
Status  : ACCEPTED (non-breaking)
traces  : REQ-FIN-047, REQ-FIN-050, REQ-FIN-051, REQ-FIN-055, REQ-FIN-063, REQ-FIN-068, API-FIN-020, API-FIN-038, QR-FIN-050

## Context
1. ADR-FIN-013 names each business fact by a FIN_EVENT_BUSINESS_FIELD code, but no upstream artifact says where in the
   event request the value travels. The published `EventEntryBuildRequest` (api-docs-fin.md) already has a `fields`
   object of named string values. The unimplemented DIRECT mode used it to carry account codes, and nothing else reads it.
2. SCR-REQ-FIN-013 §B2 shows each mapping with its account's code and bilingual name. ENT-FIN-015 stores only the
   account's id (ADR-FIN-024). The engine's join governance requires an ADR for any join that fetches parent data.

## Decision
1. A business field's value is `fields[<FIN_EVENT_BUSINESS_FIELD code>]`, for example `fields.PAYMENT_METHOD`. If the key
   is absent or blank, the field is missing (RULE-FIN-021). No request field is added, and the key set grows as MDL data.
   Nothing else in `fields` is read by the rules engine, so an account code sent there is ignored (REQ-FIN-047).
2. QR-FIN-050 joins FIN_ACCOUNT on the mapping's account id and returns the account's code, nameAr and nameEn with each
   row. This is an intra-module, read-only projection. No lookup label is joined.

## Consequences
- The event contract stays additive: the published request shape is unchanged.
- The mapping screen needs no second call per row to label an account.
- Override: add a dedicated `businessFields` object to the request if a separate namespace is preferred. That would be a
  breaking change to the published request, so it would need a new ADR.
