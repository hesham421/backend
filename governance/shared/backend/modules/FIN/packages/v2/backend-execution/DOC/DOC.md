<!-- source: PHASE:DOC -->
<!-- traces: REQ-FIN-017, REQ-FIN-072, REQ-FIN-073, REQ-FIN-074, REQ-FIN-075 -->
<!-- PHASE:DOC:START traces=REQ-FIN-017,REQ-FIN-072,REQ-FIN-073,REQ-FIN-074,REQ-FIN-075 -->
## PHASE 4 — DOC

**API contract summary** (backend self-check only; the frontend binds to the real `api-docs-fin.md` published after
implementation): 41 endpoints under `/api/v1/fin`, one per API-FIN-001 … API-FIN-041, listed with verb and path in
the API Registry of the Plan Index — not restated here.

**DTO typing constraints**: every lookup-backed field is a `String` holding the code, never a Java enum —
accountTypeCode, natureCode, directionCode, journalTypeCode, statusCode, eventTypeCode, accountDerivationTypeCode,
amountSourceTypeCode, distributionTypeCode, scheduleTypeCode, frequencyCode, and in v2 accountBusinessFieldCode,
businessFieldCode, valueSourceCode and (for the PAYMENT_METHOD field) businessValue. `docNo` never appears in a
request body and always appears in a response.

**Pagination and filter standard**: `{filters, page, size, sort}` in the body, `Page<T>` in `ApiResponse<T>`, as
SEC's and MDL's own CORE phases define it.

**Event rejection contract for the consumer (REQ-FIN-075, ADR-FIN-027, ADR-FIN-030)**. The event consumer branches on
the full code of an API-FIN-020 rejection, never on the HTTP status alone: several unrelated rejections share 409.
Each code appears in the Error Catalog exactly once, with its ar/en message, and the published api-docs carry the
same three classes:

| Class | Code | HTTP | Consumer action |
|---|---|---|---|
| already posted | FIN-409-DUPLICATE-EVENT | 409 | acknowledge the message: the event posted the first time, whether the pre-check or the store's uniqueness guarantee caught the repeat (REQ-FIN-072) |
| retry later | FIN-409-PERIOD-NOT-OPEN | 409 | wait and retry: the rejection clears once the target period is opened. Only a SOFT_CLOSE period can return to OPEN; a HARD_CLOSE or YEAR_END_CLOSE period never will, so a retry policy needs an upper bound (REQ-FIN-073) |
| needs a fix | every other code API-FIN-020 lists — among them FIN-422-UNMAPPED-VALUE (add a mapping), FIN-422-MISSING-BUSINESS-FIELD (fix the producer), FIN-422-UNRESOLVED-DIMENSION-VALUE (define the dimension value), FIN-422-NO-DEFINED-PERIOD (define the calendar) | 404 / 409 / 422 | mark the message failed; each code names the party that fixes it (ADR-FIN-027) |

**Event payload** (ADR-FIN-031): business facts travel in the request's `fields{}` map, keyed by
FIN_EVENT_BUSINESS_FIELD codes (PAYMENT_METHOD, EXPENSE_TYPE_CODE, ORGANISATION_CODE, BRANCH_CODE); the key set
grows as MDL data, not as a new request field. An account code is never read from the payload.
<!-- PHASE:DOC:END -->
