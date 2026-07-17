# API Contract Guideline

> Read this before generating or reviewing any backend code that produces or handles an HTTP
> response. This document is **Governance**, not a generation Skill — it defines WHAT the public
> REST contract is and WHERE it is implemented. It does not define which Skill generates or
> validates a given feature's use of it; that remains an implementation detail of the Backend
> Skills (see "Scope" below), the same relationship `domain-layer.md` has to Business Rules.

## Purpose

The public REST contract — response envelope shape, exception→HTTP mapping, HTTP status
semantics — is shared infrastructure consumed by every controller and service in every module.
Its purpose is to give that infrastructure **one canonical definition** instead of letting each
module, or each skill, restate a fragment of it independently.

**This document is the decision maker for "what does the contract say?" The generators
(`create-controller`, `create-dto`) and validators (`enforce-backend-contract`,
`enforce-error-handling`) are the enforcers of it.**

## Scope — what this document owns

- The `ApiResponse<T>` envelope shape and when it is applied
- The `Status` → HTTP status mapping (`OperationCodeImpl`) and the exception → `Status` mapping
  (`GlobalExceptionHandler`)
- Error code **format** conventions and where a module is allowed to deviate
- Which shared classes are the single source of truth for each concern, so no module or skill
  reinvents a competing version

## What this document does NOT own

- Business Rules and business decisions — that is `domain-layer.md` ("is this operation
  allowed?"). This document only owns how an already-decided outcome is serialized onto the
  wire.
- Per-module error code **registration** (which constants exist in `<Module>ErrorCodes`,
  whether `messages.properties`/`messages_ar.properties` have matching entries) — that stays with
  `enforce-error-handling`, unchanged.
- Per-feature Controller-layer generation (`@Operation`, `@Valid`, endpoint shapes) — that stays
  with `create-controller`, unchanged.

---

## 1. The Envelope (single source of truth: `ApiResponse<T>` / `ApiError`, `erp-common-utils`)

Every JSON response body on the platform is one shape:

```json
{
  "success": true,
  "message": "OK",
  "data": { "...": "..." },
  "error": null,
  "timestamp": "2026-07-04T09:24:18.913Z",
  "correlationId": "9fd928f0-fe86-4d44-9179-df4ffdce1183"
}
```

On failure, `data` is `null` and `error` is populated:

```json
{
  "success": false,
  "message": "Cannot deactivate Branch: active departments exist",
  "data": null,
  "error": {
    "code": "ERR_ORG_0007",
    "details": "Cannot deactivate Branch: active departments exist",
    "fieldErrors": null,
    "path": "/api/v1/org/branches/21/deactivate",
    "timestamp": "2026-07-04T05:24:19.523401Z"
  },
  "timestamp": "2026-07-04T05:24:19.523401Z",
  "correlationId": "a601935f-85e9-45ea-ad00-d1d7931e072f"
}
```

**Rules:**
- No module, controller, or feature may define its own response envelope, error DTO, or
  success/failure wrapper. `ApiResponse<T>` / `ApiError` / `FieldErrorItem` in
  `com.example.erp.common.web` are the only ones that exist.
- A controller never constructs `ApiResponse` by hand for a `ServiceResult`-backed endpoint —
  `OperationCode.craftResponse(result)` is the only translation point (`create-controller`
  SH.1). Plain (non-`ServiceResult`) return values are wrapped automatically by
  `ApiResponseWrapper` — a controller returning a bare DTO does not bypass the envelope.
- `204 No Content` (delete) is the one documented case with **no body at all** — `null` is
  preserved, not wrapped.

## 2. Exception → HTTP Mapping (single source of truth: `OperationCodeImpl` + `GlobalExceptionHandler`)

`Status` (`com.example.erp.common.domain.status.Status`) is the **only** vocabulary the Service
and Domain layers may use to describe an outcome. It carries no HTTP knowledge. `OperationCodeImpl`
is the **only** place a `Status`/`StatusCategory` is translated into an `HttpStatus`.

Canonical mapping (from `OperationCodeImpl`, current as of this writing):

| Status | HTTP | Status | HTTP |
|---|---|---|---|
| `SUCCESS`, `OK` | 200 | `NOT_FOUND` family | 404 |
| `CREATED` | 201 | `BUSINESS_RULE_VIOLATION`, `OPERATION_NOT_ALLOWED`, `INVALID_STATE` | 422 |
| `UPDATED`, `DELETED` | 200 | `PRECONDITION_FAILED` | 412 |
| `VALIDATION_ERROR` family (client-error category) | 400 | `PRECONDITION_VIOLATION` | 400 |
| `METHOD_NOT_ALLOWED` | 405 | `DUPLICATE`, `ALREADY_EXISTS`, `DB_CONSTRAINT_VIOLATION`, `CONCURRENT_MODIFICATION` | 409 |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | `UNAUTHORIZED`, `INVALID_CREDENTIALS`, `TOKEN_EXPIRED` | 401 |
| `INTERNAL_ERROR`, `DB_ERROR` | 500 | `FORBIDDEN`, `ACCESS_DENIED` | 403 |
| `SERVICE_UNAVAILABLE` | 503 | `TIMEOUT` | 504 |

Framework-level exceptions not raised by application code are pre-mapped once, centrally, in
`GlobalExceptionHandler` — no module may add its own `@ExceptionHandler`/`@RestControllerAdvice`
for these:

| Exception | HTTP | `error.code` |
|---|---|---|
| `MethodArgumentNotValidException`, `ConstraintViolationException`, `BindException` | 400 | `VALIDATION_ERROR` / `BINDING_ERROR` (+ `fieldErrors`) |
| `HttpMessageNotReadableException` | 400 | `INVALID_JSON` |
| `MissingServletRequestParameterException` | 400 | `MISSING_PARAMETER` |
| `MethodArgumentTypeMismatchException` | 400 | `TYPE_MISMATCH` |
| `AuthenticationException` / `BadCredentialsException` | 401 | `UNAUTHORIZED` / `INVALID_CREDENTIALS` |
| `AccessDeniedException` | 403 | `FORBIDDEN` |
| `NoHandlerFoundException` | 404 | `ENDPOINT_NOT_FOUND` |
| `DataIntegrityViolationException` | 409 | `DB_CONSTRAINT_VIOLATION` |
| `DataAccessException` | 500 | `DB_ERROR` |
| `LocalizedException` / `BusinessException` | via `OperationCode.toHttpStatus(statusCode, ...)` above | `ex.getMessageKey()` / `ex.getCode()` |
| Anything else (`Exception`) | 500 | `INTERNAL_ERROR` |

**Known resolution — deactivation/deletion-blocked-by-children guards:** the Domain layer
(`<Entity>Domain.assertCanDeactivate(...)`, per `domain-layer.md`) must throw
`LocalizedException(Status.CONFLICT, ...)` for "cannot deactivate/delete because active/child
records exist" scenarios — **not** `Status.BUSINESS_RULE_VIOLATION`. This resolves an ambiguity
between two skill documents that currently disagree (`create-entity`'s illustrative
`assertCanDeactivate` example uses `BUSINESS_RULE_VIOLATION`, while `enforce-error-handling`'s
own Status-usage table prescribes `CONFLICT` for the identical scenario) — `CONFLICT` (409) is
canonical because it is what `enforce-error-handling` already documents and what deployed `erp-org`
behavior actually returns for this scenario. `BUSINESS_RULE_VIOLATION` (422) remains correct for
genuine invariant violations that are not "a specific referencing record exists" (e.g. invalid
state transitions, cycle detection).

## 3. Error Code Format

Two coexisting, both-valid formats currently exist in the codebase; a module picks **one** and is
internally consistent:

- **Semantic form** (`<ENTITY>_<SCENARIO>`, e.g. `MASTER_LOOKUP_KEY_DUPLICATE`) — the default
  documented by `enforce-error-handling` and used by `erp-masterdata`.
- **Registry form** (`ERR_<MODULE>_<NNNN>`, e.g. `ERR_ORG_0007`) — used by `erp-org`, tracing
  each code back to a numbered entry in that module's execution-plan Error Catalog.

**Rule:** a new module follows the semantic form by default (it is self-describing without a
lookup table). A module may use the registry form only if it already has one (`erp-org`) — do
not mix both formats within a single module's `<Module>ErrorCodes` class. Either form still
resolves through the same `LocalizedException(Status, ErrorCode, ...args)` /
`<Module>ErrorCodes` / `messages.properties`+`messages_ar.properties` pipeline documented by
`enforce-error-handling` — the format choice does not change that pipeline.

## 4. Deprecated Surfaces

- `NotFoundException` (`com.example.erp.common.exception`) is `@Deprecated` and still has a
  live handler in `GlobalExceptionHandler` for backward compatibility only. New code must never
  use it — `enforce-error-handling`'s "NotFoundException is NOT USED. EVER." rule is unchanged
  and unaffected by the handler's continued existence.

---

## Relationship to the Backend Skills

```
api-contract.md        → Contract Definition   (this guideline)
create-controller       → Generates a controller that CONSUMES the contract (craftResponse, envelope)
create-dto               → Generates DTOs whose shapes CONSUME the contract (Response/error shape)
enforce-backend-contract → Validates ONE feature's Controller layer against this contract (LAYER 6, CU.3/6/7/8)
enforce-error-handling  → Validates ONE module's exception usage against the Status/error-code taxonomy defined here
```

This document does not generate or validate anything itself. It exists so that when
`create-controller`, `create-dto`, `enforce-backend-contract`, and `enforce-error-handling` each
need to state "what the envelope/mapping/format is," they point here instead of restating their
own copy — the same relationship `domain-layer.md` has to `create-entity`, `create-service`, and
`enforce-backend-contract` LAYER 0.

## Scope — what this document does and does not decide

This guideline states an architectural fact and a small number of canonical resolutions:

> The public REST contract (envelope, exception→HTTP mapping, error-code format) has exactly one
> definition, given above, and every module and skill defers to it instead of restating or
> re-deciding it independently.

It does **not** prescribe which Backend Skill generates or validates a given feature's compliance
with it — that remains an implementation detail of the Backend Skills (see
`.github/skills/backend/`), governed the same way `domain-layer.md` governs Business Rule
placement without prescribing which skill produces the Domain object.

## Golden Rule

The contract has one definition. Every module consumes it — none of them re-decides it.
