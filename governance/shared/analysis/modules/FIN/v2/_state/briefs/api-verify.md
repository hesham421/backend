# BRIEF — stage `api-verify` (API Verification (post-implementation)) · module FIN · v2 · profile `erp`

Lane `analysis` · implementer claude:opus · effort high · round 1

## Rules that bind this run
- Questions: **forbidden**. A `[QUESTION]` block is refused. Ambiguity → ADR in `analysis/decisions/FIN/` (`ADR-{MOD}-{seq:03d}.md`): non-breaking → continue; breaking → status BLOCKED and stop.
- Owns IDs: none — ID grammar `{prefix}-{MOD}-{seq}` (seq width 3); never re-number, never restart a sequence.
- Read only what this brief contains (generated current state); never open version folders yourself.
- Write exactly these files (complete files; this is a delta version, so only what changed):
- `governance-shared/analysis/modules/FIN/v2/api_verify/api-verify-fin.py`
- `governance-shared/analysis/modules/FIN/v2/api_verify/api-verify-report-fin.md`
- `governance-shared/analysis/modules/FIN/v2/change-manifest.md` — fill in its `## Per artifact` section for what you wrote (ADDED / MODIFIED / REMOVED / UNCHANGED); it is ONE file per version, at the version root, never in a stage folder.
- Respond with one `<<<FILE: <repo-relative path>>>> … <<<END FILE>>>` block per file when running through a command runner; when running as the operator, write the files directly.

## Contracts checked by `gov.py analyze` after this stage
- **C11** real API docs (+ manifest) → API verification (standalone): C11.1 exists {'input': 'api-docs'} [CRITICAL]; C11.2 exists {'artifact': 'test-execution-manifest'} [MINOR]; C11.3 exists {'artifact': 'api-verify-report'} [MAJOR]

---
# ENGINE
```
ENGINE        : api-verify — API Verification (post-implementation)   (STANDALONE — post-implementation)
LANE          : analysis · questions forbidden
MODULE        : FIN · v2 · profile erp (ERP Platform)
READS         : api-docs · test-execution-manifest?   ("?" = optional)
PRODUCES      : api-verify-fin.py · api-verify-report-fin.md
OWNS IDS      : none — it mints no ID
BOUNDARY      : runs OUTSIDE the factory boundary (analysis-only), never inside a pass
```

# API Verification (post-implementation) — engine reference

## 0. Position

This engine runs **after** the backend module has been implemented and its api-docs
published, on demand (`/api-verify`).
It sits outside the factory boundary: no pass includes it, no gate waits for it, and its
result never changes a line artifact. Its job is **translation, not derivation** — it turns
already-governed specifications into one runnable verification script that exercises the
real API in dependency order, plus a report of what it found.

It designs no test and invents no rule, message, dependency or data value. Anything not
traceable to an input document is skipped and named as skipped. Questions are
`forbidden`; a real blocker (an unresolvable dependency cycle, an ambiguous
payload) becomes an ADR per `factory.yaml → ambiguity` and the run stops there.

## 1. Inputs and tiers

| Input | Read from | Role |
|---|---|---|
| `api-docs` | `_inputs/api-docs-fin.md` (published at ``) | **mandatory** — endpoints, verbs, paths, request/response field tables, examples, error codes |
| `test-execution-manifest` | `_state/current-test-execution-manifest.md` (from `test-gen`, when `profile.stack.testing.manifest` is true) | optional — pre-computed dependency order, RULE → code → TC triples, entity CRUD checklist |
| base URL · credentials · auth endpoint | the run's arguments | optional — placeholders otherwise (§6) |

| Tier | Present | Generates |
|---|---|---|
| **Full** | api-docs + manifest | happy-path CRUD per entity **and** negative (rule-violation) tests; dependency order and triples read from the manifest — zero self-derivation, even if other artifacts are also attached |
| **Minimal** | api-docs only | happy-path CRUD only; FK order inferred from FK-typed fields and "parent …" descriptions in the request tables; the report states that negative cases were skipped and why |

State the tier at the start of the run. When the manifest is present, re-deriving what it
already holds is a boundary violation.

Multi-batch api-docs (a large module documented across several files): reconcile received
endpoints against the manifest's CRUD checklist (or the catalogue the api-docs carry),
report entity-level coverage briefly, never ask — when a batch trails off, generate what is
documented and list every entity without a documented create endpoint as *out of scope for
this run*; one function per entity lets a later batch append without a rewrite.

## 2. Stack conventions (from the profile — never assumed)

| Convention | Value | Use in the script |
|---|---|---|
| base path | `/api/v1/{module}/{resource}` | every path is an instance of it; the module segment is `fin` |
| verbs | `POST`=create · `GET`=read · `PUT`=update · `DELETE`=deactivate (soft) · `PATCH`=partial | operation type per endpoint (create / read / search / update / deactivate (soft) / partial) |
| response envelope | ApiResponse<T> | unwrap before asserting on data |
| paging envelope | Page<T> | search assertions read the page fields the api-docs document (page number, size, total, content list) — confirm names in the api-docs; an undocumented field is a documentation gap to flag, not an assertion |
| error envelope | LocalizedException → {code, messageAr, messageEn} | negative assertions check HTTP status + the runtime `code`; the runtime code format is the one the api-docs / manifest state — never the hyphenated governance ID |
| languages | ar, en (primary `ar`) | message presence asserted per language field the envelope carries; assertion text and report language = `ar` with `en` beside it |
| deactivate (soft) semantics | soft — active flag (suffix `Fl`) | cleanup strategy (§3 stage F) |
| permissions | `PERM_<PAGE_CODE>_<ACTION>`, gateway `VIEW` | a forbidden call asserts the forbidden status through the error envelope |

## 3. Processing pipeline

**A — Inventory.** Parse every documented endpoint: id (`API-*` when the docs carry it),
verb, path, entity (path segment), operation type (from verb + path suffix), request field
table (name, type, required, constraints, example), response shape.

**B — Dependency order.** Full tier: read the manifest's DEPENDENCY ORDER as-is. Minimal
tier: infer edges from FK-typed fields whose description names a parent, then sort
topologically; roots first. A cycle or an unresolvable edge → ADR, stop.

**C — Negative mapping.** Full tier: for each manifest triple (RULE → code → TC, HTTP): set up
the precondition the RULE describes, call the endpoint that must be blocked, assert the HTTP
status and the runtime code, tag the function with RULE/TC ids. Minimal tier: none — say so.
Informational-only RULEs are excluded (the manifest already does this).

**D — Assembly.** One fixed structure (§4), one `test_<entity>()` per entity in stage-B order,
FK ids threaded as parameters from the parent's return value — never hard-coded.

**E — Exploratory scenarios (bounded).** Only for fields with `required = yes` or a stated
constraint (length, numeric range): at-limit / one-over, omission, type mismatch, an
undocumented lookup value, CRUD idempotency (deactivate twice, activate active), invalid FK
reference. **Assert vs observe:** an outcome governed by a RULE + code is stage C's; any
other expected status is undocumented → executed as an *observation* (recorded, never
pass/fail, listed apart in the report). No documented source value → skip.

**F — Teardown.** Every created id is tracked per entity; cleanup runs in `finally`, in
reverse dependency order; a hard-delete endpoint is used only when the api-docs document
one, otherwise the deactivate (soft) endpoint is called and the report
states that records remain (deactivated) — never a silent "cleaned up".

**G — Problems report.** The report `api-verify-report-fin.md` lists only failures, bucketed: *likely real
bug* (a documented rejection did not happen, or an unexplained status), *test assumption
mismatch* (rejected, but with a status the manifest did not state — fix the expectation, not
the backend), *infrastructure* (connection / timeout — rerun). Ambiguous → *likely real bug*.

**H — Log correlation and database access (opt-in).** Log excerpts around a failing call are
attached as *approximate* unless a trace id exists. Database writes are two-tier: deleting
rows this run created (tracked ids only) is allowed for teardown; any other data fix is
emitted as *suggested SQL* for a human — never executed. All DB writes sit behind an
explicit opt-in flag (default off = print only), in a transaction, with separate credentials.

## 4. Script structure — `api-verify-fin.py`

The file extension of the produced artifact decides the language (`factory.standalone` →
`py`). Structure (a fixed skeleton the run extends, never redesigns):

```
# Intended for Dev/Test environments only.
config      : BASE_URL (argument or localhost placeholder) · credentials (placeholders unless given) · auth endpoint · DB opt-in flag
client      : thin HTTP wrapper (get / post / put / patch / delete as the api-docs need) that unwraps ApiResponse<T>
results     : TestResult / TestSuite records · run() for asserted calls · run_observation() for stage-E observations (separate bucket, never in totals)
helpers     : extract_token · extract_id · first id of a page (per Page<T>)
per entity  : test_<entity>(parent_ids…) → create → get-by-id (ok) → get-by-id (missing id → not found) → update → [stage C negatives] → [stage E observations] → deactivate → activate ; appends created ids
cleanup()   : stage F
report      : HTML/Markdown with pass/fail suites, an "observations" section, and the problems buckets → api-verify-report-fin.md
main()      : suites in stage-B order inside try/finally; exit non-zero on any asserted failure (observations never affect the exit code)
```
Every test function carries a traceability comment: `Covers: API-… ; Negative: RULE-… / <code> / TC-…`.

Payload rules: use the api-docs `example` values verbatim; a required field without an
example gets a clearly marked placeholder of the right type; lookup / enum values only as
seen in the docs; FK ids threaded, never literal.

## 5. Generation gate (silent; only failures are reported)

```
[ ] every entity in the api-docs has a test_<entity>() function
[ ] main() order = stage-B order — no forward FK reference
[ ] every payload value comes from an example or a marked placeholder
[ ] negatives only where RULE + code + TC resolve (Full tier) — none self-derived beside a present manifest
[ ] exploratory scenarios only on required/constrained fields; undocumented outcomes use run_observation()
[ ] every create appends its id; cleanup() in finally, reverse order; hard-delete used only if documented
[ ] runtime error codes in the format the docs/manifest state; page fields as documented
[ ] no invented credentials, business codes or lookup values; traceability comment on every function
```

## 6. Safety

Credentials are placeholders unless supplied in the run; the script targets only the
supplied base URL or a localhost default; DB writes are opt-in and scoped to ids this run
created; the report never presents an approximate log correlation as a confirmed root cause.

## 7. Boundaries

| Consumes (read-only) | Produces | Never |
|---|---|---|
| api-docs, the manifest, run arguments | `api-verify-fin.py`, `api-verify-report-fin.md` | an ID of any kind, a change to any line artifact, a gate verdict, a data fix against records it did not create |


---
# INPUTS (generated current state)

<<<INPUT: api-docs>>>
<!-- GENERATED by `gov.py fetch-inputs` — do not edit.
     source : backend/modules/FIN/api-docs
     files  : 11
     digest : c2d2842c56a6492e48f4dd645020e4684f43f139c137e2fb6c7a1091a867bef9
     Edits belong in the source; anything this factory adds belongs in
     the companion file, which this command never touches. -->

<!-- fetched-from: index.md -->
<!-- AUTO-GENERATED by api-doc-generator — do not edit manually -->
# FIN API Documentation

_OpenAPI definition_

API version: `v0`

## Servers

- http://localhost:7272

## Authentication

- **bearerAuth**: bearer (JWT) — Access token issued by POST /api/v1/sec/auth/login, sent as Authorization: Bearer <token>


## Common Response Envelope

Schema: `ApiResponse<T>`

| Field | Type | Required | Constraints | Description |
|---|---|---|---|---|
| success | boolean | No |  |  |
| data | object | No |  | Endpoint-specific payload — see each endpoint's Response section |
| error | ApiError | No |  | Present only when success=false |
| error.code | string | No |  |  |
| error.message | string | No |  |  |
| error.fieldErrors | array<FieldErrorItem> | No |  |  |
| error.fieldErrors[].field | string | No |  |  |
| error.fieldErrors[].message | string | No |  |  |
| timestamp | string (date-time) | No |  |  |

### What `error.fieldErrors[].field` carries

Read from `GlobalExceptionHandler.java`: the expression each handler passes to `FieldErrorItem.field(...)`. The same slot is filled differently per handler, so a client must not assume it is always a request field path — a row whose fallback branch is not a field path can put something else there.

| Handler | Exceptions | HTTP Status | Code | `field` holds | Source expression |
|---|---|---|---|---|---|
| handleLocalizedException | LocalizedException | the thrown Status's HTTP status | the thrown error code | `detail.field()` when `detail.field()` is non-null, otherwise `detail.errorCode()` | `detail.field() != null ? detail.field() : detail.errorCode()` |
| handleValidation | MethodArgumentNotValidException | 400 BAD_REQUEST | VALIDATION_ERROR | the value of `fe.getField()` | `fe.getField()` |
| handleRequestParameter | MissingServletRequestParameterException, MethodArgumentTypeMismatchException | 400 BAD_REQUEST | VALIDATION_ERROR | the value of `parameterName` | `parameterName` |

## Pagination Envelope

Schema: `Page<T>`

| Field | Type | Required | Constraints | Description |
|---|---|---|---|---|
| empty | boolean | No |  |  |
| first | boolean | No |  |  |
| last | boolean | No |  |  |
| number | integer (int32) | No |  |  |
| numberOfElements | integer (int32) | No |  |  |
| pageable | Pageable | No |  |  |
| size | integer (int32) | No |  |  |
| sort | Sort | No |  |  |
| totalElements | integer (int64) | No |  |  |
| totalPages | integer (int32) | No |  |  |

## Pagination Constraints

Source: `com/erp/common/search/PageableBuilder.java`

| Constraint | Value |
|---|---|
| Default page | 0 |
| Default size | 20 |
| Maximum size | 200 |

## Uniqueness Invariants

Enforced by the database on this module's own tables, read from each entity's `@Table(uniqueConstraints)` and from the Flyway migrations. Scope is the partial index's `WHERE` clause verbatim where one exists; a request that violates an invariant is refused.

| Table | Entity | Columns (entity field) | Scope | Constraint | Source |
|---|---|---|---|---|---|
| FIN_ACCOUNT | Account | CODE (code) | all rows | UQ_FIN_ACCOUNT_CODE | Account.java, V22__fin_schema.sql |
| FIN_ACCOUNT | Account | is_retained_earnings_fl (isRetainedEarningsFl) | WHERE is_retained_earnings_fl | UQ_FIN_ACCOUNT_RETAINED_EARNINGS | V23__fin_account_retained_earnings_flag.sql |
| FIN_ACCOUNT_MAPPING | AccountMapping | event_type_code (eventTypeCode), business_field_code (businessFieldCode), business_value (businessValue) | WHERE is_active_fl | UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY | V35__fin_v2_account_mapping_schema.sql |
| FIN_DIMENSION | Dimension | CODE (code) | all rows | UQ_FIN_DIMENSION_CODE | Dimension.java, V22__fin_schema.sql |
| FIN_DIMENSION_VALUE | DimensionValue | DIMENSION_ID (dimension), CODE (code) | all rows | UQ_FIN_DIMENSION_VALUE_DIM_CODE | DimensionValue.java, V22__fin_schema.sql |
| FIN_EVENT_TYPE_RULE | EventTypeRule | EVENT_TYPE_CODE (eventTypeCode) | all rows | UQ_FIN_EVENT_TYPE_RULE_CODE | EventTypeRule.java, V22__fin_schema.sql |
| FIN_FISCAL_YEAR | FiscalYear | CODE (code) | all rows | UQ_FIN_FISCAL_YEAR_CODE | FiscalYear.java, V22__fin_schema.sql |
| FIN_JOURNAL_ENTRY | JournalEntry | EVENT_REFERENCE (eventReference) | all rows | UQ_FIN_JOURNAL_ENTRY_EVENT_REF | JournalEntry.java, V22__fin_schema.sql |
| FIN_JOURNAL_ENTRY | JournalEntry | FISCAL_YEAR_ID (fiscalYear), DOC_NO (docNo) | all rows | UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO | JournalEntry.java, V22__fin_schema.sql |
| FIN_RULE_LINE_DIM | RuleLineDimension | RULE_LINE_ID (ruleLine), DIMENSION_ID (dimension) | all rows | UQ_FIN_RULE_LINE_DIM_LINE_DIM | RuleLineDimension.java, V35__fin_v2_account_mapping_schema.sql |

## Known Error Codes

A code's value is also its i18n message key, so each row carries the message the API actually answers with. `Message` is the base bundle (the fallback every locale resolves through); a `Message (<tag>)` column is that locale's own bundle. An em dash means that bundle has no entry for that code.

"Consumer note" is the error-code constant's own Javadoc, verbatim apart from markup — when the module's author documented what raises the code and what makes it stop being raised, that is what it says.

"Raised by" counts the endpoints whose controller → service → domain call walk reaches a throw site naming the code. An unbound code is thrown only from code no endpoint's walk reaches (a cross-module entry point, a job, or a call past the walk's depth) — it is listed, not attributed.

| Code | Value | Source | Message | Message (ar) | Status | HTTP Status | Raised by | Consumer note |
|---|---|---|---|---|---|---|---|---|
| FIN_409_DIMVALUE_DUP | `FIN-409-DIMVALUE-DUP` | exception/FinErrorCodes.java | This code is already used within this dimension | هذا الرمز مستخدم بالفعل ضمن هذا البُعد | ALREADY_EXISTS | 409 CONFLICT | 1 endpoint(s) — see their Business Responses | RULE-FIN-002 — a dimension value whose code already exists under the same dimension (QR-FIN-010; backed by UQ_FIN_DIMENSION_VALUE_DIM_CODE). API: API-FIN-007. HTTP 409. |
| FIN_409_RULE_DUP | `FIN-409-RULE-DUP` | exception/FinErrorCodes.java | An active rule already exists for this event type | يوجد بالفعل قاعدة نشطة لهذا النوع | ALREADY_EXISTS | 409 CONFLICT | 1 endpoint(s) — see their Business Responses | PLATFORM-STD (§6.4, one rule per event type, QR-FIN-014) — the event type already has an active rule (backed by UQ_FIN_EVENT_TYPE_RULE_CODE). API: API-FIN-010. HTTP 409. |
| FIN_409_REMAINDER_COUNT | `FIN-409-REMAINDER-COUNT` | exception/FinErrorCodes.java | Exactly one remainder line is required when any percentage distribution is present | يلزم تحديد سطر باقٍ واحد بالضبط عند وجود توزيع نسبي | CONFLICT | 409 CONFLICT | 4 endpoint(s) — see their Business Responses | RULE-FIN-003 — wrong remainder-line/target count: exactly one remainder is required whenever any sibling line (ENT-FIN-010) or target (ENT-FIN-014) uses percentage distribution (QR-FIN-016). APIs: API-FIN-011, API-FIN-016. HTTP 409. |
| FIN_409_PARENT_NOT_LEAF_ELIGIBLE | `FIN-409-PARENT-NOT-LEAF-ELIGIBLE` | exception/FinErrorCodes.java | An account with sub-accounts cannot accept direct posting | لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا | CONFLICT | 409 CONFLICT | 1 endpoint(s) — see their Business Responses | RULE-FIN-001 (create trigger) — a new sub-account was placed under a parent that is still marked as accepting direct posting; the parent must be demoted first (QR-FIN-006). API: API-FIN-002. HTTP 409. |
| FIN_409_HAS_CHILDREN | `FIN-409-HAS-CHILDREN` | exception/FinErrorCodes.java | An account with sub-accounts cannot accept direct posting | لا يمكن لحساب له حسابات فرعية أن يقبل ترحيلاً مباشرًا | CONFLICT | 409 CONFLICT | 1 endpoint(s) — see their Business Responses | RULE-FIN-001 (update trigger) — an account that already has sub-accounts cannot be marked as accepting direct posting (QR-FIN-006). APIs: API-FIN-002, API-FIN-003. HTTP 409. |
| FIN_409_NOT_POSTABLE_ACCOUNT | `FIN-409-NOT-POSTABLE-ACCOUNT` | exception/FinErrorCodes.java | The target account does not accept direct posting | الحساب المستهدف لا يقبل ترحيلاً مباشرًا |  |  | 1 endpoint(s) — see their Business Responses | RULE-FIN-007 — a posting line targets an account that is not a leaf or not active. APIs: API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017. HTTP 409. |
| FIN_409_NOT_REOPENABLE | `FIN-409-NOT-REOPENABLE` | exception/FinErrorCodes.java | The period is hard-closed and cannot be reopened | الفترة مغلقة إغلاقًا صارمًا ولا يمكن إعادة فتحها | CONFLICT | 409 CONFLICT | 2 endpoint(s) — see their Business Responses | RULE-FIN-014 — an attempt to reopen a Hard Closed (or Year-End Closed) period; both are terminal states per SRS A7 (QR-FIN-040). APIs: API-FIN-024, API-FIN-026. HTTP 409. |
| FIN_403_SOD_VIOLATION | `FIN-403-SOD-VIOLATION` | exception/FinErrorCodes.java | The close-approval permission is separate from the entry-creation permission | صلاحية اعتماد الإغلاق منفصلة عن صلاحية إنشاء القيود |  |  | no throw site naming it in module source | RULE-FIN-015 — STRUCK in the v2 Error Catalog: both throw sites were deleted on 2026-09-12 (recorded human decision); the rule is satisfied by the distinct PERM_FIN_PERIODS_CLOSE_APPROVE gate alone. The constant and its two bundle keys stay on record with no thrower until a session removes all three together. |
| FIN_409_UNBALANCED | `FIN-409-UNBALANCED` | exception/FinErrorCodes.java | The entry is unbalanced — total debits do not equal total credits | القيد غير متوازن — إجمالي المدين لا يساوي إجمالي الدائن |  |  | 6 endpoint(s) — see their Business Responses | RULE-FIN-006 — the entry's total debits do not equal its total credits (QR-FIN-029, POL-FIN-001). Thrown by JournalEntryDomain.assertBalanced(...). APIs: API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017. HTTP 409 (Status.CONFLICT). |
| FIN_409_PERIOD_NOT_OPEN | `FIN-409-PERIOD-NOT-OPEN` | exception/FinErrorCodes.java | The target period is not open | الفترة المستهدفة غير مفتوحة | CONFLICT | 409 CONFLICT | 6 endpoint(s) — see their Business Responses | RULE-FIN-008 — the target period is not Open at post time (QR-FIN-031, POL-FIN-004). Thrown by JournalEntryDomain.assertTargetPeriodOpen(...) and by JournalEntryDomain.resolveReversalPeriodPk(...) when RULE-FIN-012 finds no open period to receive a reversal. Retryable: the rejection clears once the target period is opened, and only a SOFT_CLOSE period can return to OPEN, so a retry needs an upper bound. APIs: API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021. HTTP 409. |
| FIN_409_INVALID_DIMENSION | `FIN-409-INVALID-DIMENSION` | exception/FinErrorCodes.java | The dimension value is invalid | قيمة البُعد غير صالحة | CONFLICT | 409 CONFLICT | 3 endpoint(s) — see their Business Responses | RULE-FIN-009 — a posting line cites a dimension value that does not belong to its stated dimension, or is inactive (QR-FIN-032). Thrown by DimensionValueDomain.assertUsableOnLine(...). APIs: API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017. HTTP 409. |
| FIN_409_DUPLICATE_EVENT | `FIN-409-DUPLICATE-EVENT` | exception/FinErrorCodes.java | An entry for this event reference has already been posted | تم بالفعل ترحيل قيد لهذا المرجع | CONFLICT | 409 CONFLICT | 6 endpoint(s) — see their Business Responses | RULE-FIN-004 — an entry has already been produced for this eventReference (QR-FIN-026, POL-FIN-012; backed by UQ_FIN_JOURNAL_ENTRY_EVENT_REF). Thrown by JournalEntryDomain.create(...); v2 also by FinPersistenceRefusalAdvisor when the store refuses a concurrent duplicate on that constraint (ADR-FIN-030, REQ-FIN-072). Not retryable: the entry is already posted, so the consumer acknowledges the message on this code. API: API-FIN-020. HTTP 409. |
| FIN_409_NOT_POSTED | `FIN-409-NOT-POSTED` | exception/FinErrorCodes.java | A non-posted entry cannot be reversed | لا يمكن عكس قيد غير مُرحَّل | CONFLICT | 409 CONFLICT | 2 endpoint(s) — see their Business Responses | RULE-FIN-013 — a reverse action was requested on an entry that is not POSTED (QR-FIN-035). Thrown by JournalEntryDomain.assertCanReverse(). API: API-FIN-021. HTTP 409. |
| FIN_409_INVALID_TRANSITION | `FIN-409-INVALID-TRANSITION` | exception/FinErrorCodes.java | This transition is not allowed from the current status | لا يمكن تنفيذ هذا الانتقال من الحالة الحالية | CONFLICT | 409 CONFLICT | 2 endpoint(s) — see their Business Responses | PLATFORM-STD — the row is not in the state this transition expects. The Error Catalog registers this row against the period soft-close transition; it does not cover RULE-FIN-016 (the posting lock), which is enforced by omission and throws nothing — see JournalEntryDomain's class javadoc. |
| FIN_404_ENTRY | `FIN-404-ENTRY` | exception/FinErrorCodes.java | Entry not found | القيد غير موجود | NOT_FOUND | 404 NOT_FOUND | 2 endpoint(s) — see their Business Responses | PLATFORM-STD — unknown journal entry id. The not-found code for the two read paths this sub's repository layer serves (QR-FIN-035's load-before-reverse and QR-FIN-037's read-one); raised by the service's orElseThrow, registered here with the rest of the posting core's codes. APIs: API-FIN-021, API-FIN-022. HTTP 404 (Status.NOT_FOUND). |
| FIN_400_INVALID_LOOKUP | `FIN-400-INVALID-LOOKUP` | exception/FinErrorCodes.java | The submitted value is not valid | القيمة المُدخلة غير صالحة | VALIDATION_ERROR | 400 BAD_REQUEST | 7 endpoint(s) — see their Business Responses | PLATFORM-STD (XM-FIN-001) — a submitted lookup-backed code is not an active value of its MDL lookup type, or the type key itself is unknown/inactive (MDL's own MDL-404-TYPE-KEY, translated at FIN's boundary, never surfaced raw). APIs: many. HTTP 400 (Status.VALIDATION_ERROR). |
| FIN_409_ACCOUNT_DUP | `FIN-409-ACCOUNT-DUP` | exception/FinErrorCodes.java | Account code already in use | رمز الحساب مستخدم بالفعل | ALREADY_EXISTS | 409 CONFLICT | 1 endpoint(s) — see their Business Responses | PLATFORM-STD — duplicate account code (QR-FIN-005, backed by UQ_FIN_ACCOUNT_CODE). API: API-FIN-002. HTTP 409 (Status.ALREADY_EXISTS). |
| FIN_404_ACCOUNT | `FIN-404-ACCOUNT` | exception/FinErrorCodes.java | Account not found | الحساب غير موجود | NOT_FOUND | 404 NOT_FOUND | 11 endpoint(s) — see their Business Responses | PLATFORM-STD — unknown account id. APIs: API-FIN-003, API-FIN-004, API-FIN-013, API-FIN-016, API-FIN-019, API-FIN-028. HTTP 404 (Status.NOT_FOUND). |
| FIN_409_DIMENSION_DUP | `FIN-409-DIMENSION-DUP` | exception/FinErrorCodes.java | Dimension code already in use | رمز البُعد مستخدم بالفعل | ALREADY_EXISTS | 409 CONFLICT | 1 endpoint(s) — see their Business Responses | PLATFORM-STD — duplicate dimension code (backed by UQ_FIN_DIMENSION_CODE). API: API-FIN-006. HTTP 409 (Status.ALREADY_EXISTS). |
| FIN_404_DIMENSION | `FIN-404-DIMENSION` | exception/FinErrorCodes.java | Dimension not found | البُعد غير موجود | NOT_FOUND | 404 NOT_FOUND | 4 endpoint(s) — see their Business Responses | PLATFORM-STD — unknown dimension id. APIs: API-FIN-007, API-FIN-008, API-FIN-019, API-FIN-032. HTTP 404. |
| FIN_404_DIMVALUE | `FIN-404-DIMVALUE` | exception/FinErrorCodes.java | Dimension value not found | قيمة البُعد غير موجودة | NOT_FOUND | 404 NOT_FOUND | 2 endpoint(s) — see their Business Responses | PLATFORM-STD — unknown dimension value id. Its own row rather than a reuse of FIN_404_DIMENSION: the catalog assigns a distinct 404 per entity, and raising the parent's code here would tell the client the DIMENSION was missing when the value was. Not to be confused with FIN_409_DIMVALUE_DUP, which is the duplicate-code scenario. API: API-FIN-035. HTTP 404 (Status.NOT_FOUND). |
| FIN_404_RULE | `FIN-404-RULE` | exception/FinErrorCodes.java | Rule not found | القاعدة غير موجودة | NOT_FOUND | 404 NOT_FOUND | 2 endpoint(s) — see their Business Responses | PLATFORM-STD — unknown event-type rule id. APIs: API-FIN-011, API-FIN-034. HTTP 404. |
| FIN_400_MISSING_FREQUENCY | `FIN-400-MISSING-FREQUENCY` | exception/FinErrorCodes.java | A frequency is required for a recurring template | يلزم تحديد التكرار للقالب المتكرر | VALIDATION_ERROR | 400 BAD_REQUEST | 1 endpoint(s) — see their Business Responses | PLATFORM-STD — a recurring template was submitted without a frequencyCode while its scheduleTypeCode is not REVERSING (SRS ENT-FIN-011: "yes (when scheduleTypeCode=RECURRING)"). A conditional-requiredness input check, not an SRS RULE. API: API-FIN-013. HTTP 400 (Status.VALIDATION_ERROR). |
| FIN_404_YEAR | `FIN-404-YEAR` | exception/FinErrorCodes.java | Fiscal year not found | السنة المالية غير موجودة | NOT_FOUND | 404 NOT_FOUND | 8 endpoint(s) — see their Business Responses | PLATFORM-STD — unknown fiscal year id. Registered here because API-FIN-019 resolves fiscalYearId before it can build an entry; the catalog row is shared with API-FIN-027. HTTP 404. |
| FIN_404_PERIOD | `FIN-404-PERIOD` | exception/FinErrorCodes.java | Period not found | الفترة غير موجودة | NOT_FOUND | 404 NOT_FOUND | 9 endpoint(s) — see their Business Responses | PLATFORM-STD — unknown period id. Registered here because API-FIN-019 resolves periodId before it can build an entry; the catalog row is shared with API-FIN-024/025/026. HTTP 404. |
| FIN_404_NO_ACTIVE_RULE | `FIN-404-NO-ACTIVE-RULE` | exception/FinErrorCodes.java | No active rule exists for this event type | لا توجد قاعدة نشطة لهذا النوع من الأحداث | NOT_FOUND | 404 NOT_FOUND | 1 endpoint(s) — see their Business Responses | RULE-FIN-005 — the submitted event's type has no active EventTypeRule (QR-FIN-027). A not-found precondition: without a rule there is nothing to build an entry from, so it is raised fail-fast by the service's orElseThrow before any other check runs. API: API-FIN-020. HTTP 404 (Status.NOT_FOUND). |
| FIN_404_ALLOCATION_RULE | `FIN-404-ALLOCATION-RULE` | exception/FinErrorCodes.java | Allocation rule not found | قاعدة التوزيع غير موجودة | NOT_FOUND | 404 NOT_FOUND | 2 endpoint(s) — see their Business Responses | PLATFORM-STD — unknown allocation rule id (QR-FIN-022). API: API-FIN-017. HTTP 404 (Status.NOT_FOUND). |
| FIN_409_YEAR_DUP | `FIN-409-YEAR-DUP` | exception/FinErrorCodes.java | Fiscal year code already in use | رمز السنة المالية مستخدم بالفعل | ALREADY_EXISTS | 409 CONFLICT | 1 endpoint(s) — see their Business Responses | PLATFORM-STD — duplicate fiscal year code (backed by UQ_FIN_FISCAL_YEAR_CODE). API: API-FIN-023. HTTP 409 (Status.ALREADY_EXISTS). |
| FIN_409_PERIODS_NOT_CLOSED | `FIN-409-PERIODS-NOT-CLOSED` | exception/FinErrorCodes.java | Every period must be hard-closed first | يجب إغلاق كل الفترات إغلاقًا صارمًا أولًا | CONFLICT | 409 CONFLICT | 1 endpoint(s) — see their Business Responses | PLATFORM-STD (§10.4 precondition) — year-end close was requested while at least one period of the fiscal year is not Hard Closed. Thrown by FiscalPeriodDomain.assertHardClosedForYearEnd(). API: API-FIN-027. HTTP 409 (Status.CONFLICT). |
| FIN_404_TEMPLATE | `FIN-404-TEMPLATE` | exception/FinErrorCodes.java | Recurring template not found | القالب المتكرر غير موجود | NOT_FOUND | 404 NOT_FOUND | 2 endpoint(s) — see their Business Responses | PLATFORM-STD — unknown recurring template id (ENT-FIN-011, QR-FIN-019). Its own row: the catalog assigns a distinct 404 per entity, and raising FIN-404-RULE here told the client that an event-type rule was missing. API: API-FIN-014. HTTP 404 (Status.NOT_FOUND). |
| FIN_400_INVALID_SORT | `FIN-400-INVALID-SORT` | exception/FinErrorCodes.java | Unrecognized sort field | حقل الترتيب غير معروف | VALIDATION_ERROR | 400 BAD_REQUEST | 10 endpoint(s) — see their Business Responses | PLATFORM-STD — the caller asked to sort by a field that is not on the searched entity's ALLOWED_SORT_FIELDS whitelist. Registered verbatim from the Error Catalog row (search API \| 400 \| unrecognized sort field). |
| FIN_409_ALREADY_REVERSED | `FIN-409-ALREADY-REVERSED` | exception/FinErrorCodes.java | This entry has already been reversed and cannot be reversed again | تم عكس هذا القيد بالفعل ولا يمكن عكسه مرة أخرى | CONFLICT | 409 CONFLICT | 2 endpoint(s) — see their Business Responses | RULE-FIN-013 — a reverse action was requested on a POSTED entry that already carries a reversal link (REVERSAL_ENTRY_ID, DBF-FIN-043). The second half of REQ-FIN-030's rationale ("prevents double-reversal", srs-fin.md line 682), which used to be enforced incidentally by the pre-classic VOID write and now needs its own code. Thrown by JournalEntryDomain.assertCanReverse(boolean). |
| FIN_422_REMAINDER_MARKER | `FIN-422-REMAINDER-MARKER` | exception/FinErrorCodes.java | The remainder marker disagrees with the line's own distribution or amount-source type | علامة سطر الباقي لا تتفق مع نوع التوزيع أو مصدر المبلغ لنفس السطر | BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT | 4 endpoint(s) — see their Business Responses | RULE-FIN-003 / RULE-FIN-010 — a rule line (ENT-FIN-010) or allocation target (ENT-FIN-014) whose isRemainderFl marker disagrees with its own amountSourceTypeCode / distributionTypeCode: either the line declares a REMAINDER source without being marked, or it is marked without declaring one. isRemainderFl (DBF-FIN-103 / DBF-FIN-141) is the schema's own marker and the single one both the RULE-FIN-003 guard and the distribution builder read; a disagreement makes "which line is the remainder" ambiguous, which is exactly what RULE-FIN-010 must never be. Thrown by EventTypeRuleDomain.assertRemainderLineSetValid(...) / AllocationRuleDomain.assertRemainderTargetSetValid(...) and, defensively, by their amount derivations. |
| FIN_422_REMAINDER_NOT_POSITIVE | `FIN-422-REMAINDER-NOT-POSITIVE` | exception/FinErrorCodes.java | The remainder line is computed as a difference and must be positive; the other lines already consume the full amount | سطر الباقي يُحسب كفرق ويجب أن يكون موجبًا؛ السطور الأخرى تستهلك المبلغ بالكامل | BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT | 2 endpoint(s) — see their Business Responses | RULE-FIN-010 — the remainder line's computed amount is not positive: the non-remainder lines on its own side already equal or exceed the opposing side's total, so there is no balancing residue for it to absorb. Previously this produced a negative or zero amount that died on CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE as an unlocalized DATA_INTEGRITY_VIOLATION. Thrown by EventTypeRuleDomain.remainderAmount(...). |
| FIN_422_INVALID_PERCENTAGE_VALUE | `FIN-422-INVALID-PERCENTAGE-VALUE` | exception/FinErrorCodes.java | This rule line's amount source value is not a valid percentage number | قيمة مصدر المبلغ لهذا السطر ليست نسبة مئوية صالحة | BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT | 1 endpoint(s) — see their Business Responses | Code review fix (correctness) — a rule line (ENT-FIN-010) with = PERCENTAGE whose amountSourceValue (DBF-FIN-104, free text) is not a well-formed decimal. Nothing upstream (the DTO, the create-time validation) constrains this column's format when the type is PERCENTAGE, so a bad value persisted once at rule-creation time previously surfaced as an unlocalized NumberFormatException — caught nowhere and falling through to a generic 500 — on every subsequent event-driven build for that event type. Thrown by EventTypeRuleDomain.sourcedAmount(...). |
| FIN_400_PERIOD_NOT_IN_YEAR | `FIN-400-PERIOD-NOT-IN-YEAR` | exception/FinErrorCodes.java | The selected period does not belong to the selected fiscal year | الفترة المحددة لا تتبع السنة المالية المحددة | VALIDATION_ERROR | 400 BAD_REQUEST | 1 endpoint(s) — see their Business Responses | RULE-FIN-017 — the submitted periodId belongs to a different fiscal year than the submitted fiscalYearId (DBF-FIN-076 makes the period's year a stored fact). Thrown by JournalEntryDomain.assertHeaderCoherent(...). |
| FIN_400_DOCDATE_OUTSIDE_PERIOD | `FIN-400-DOCDATE-OUTSIDE-PERIOD` | exception/FinErrorCodes.java | The document date falls outside the selected period | تاريخ المستند خارج نطاق الفترة المحددة | VALIDATION_ERROR | 400 BAD_REQUEST | 1 endpoint(s) — see their Business Responses | RULE-FIN-017 — the submitted docDate falls outside the submitted period's endDate] span (DBF-FIN-080/081, both NOT NULL). Thrown by JournalEntryDomain.assertHeaderCoherent(...). |
| FIN_403_FORBIDDEN | `FIN-403-FORBIDDEN` | exception/FinErrorCodes.java | You do not hold the Finance permission required for this operation | لا تملك صلاحية المالية المطلوبة لهذه العملية | FORBIDDEN | 403 FORBIDDEN | 42 endpoint(s) — see their Other Possible Responses, via FinForbiddenAdvisor | PLATFORM-STD — the caller holds no grant for the module/screen/action this endpoint requires, so the @PreAuthorize guard on the FIN service denied the call. Raised by FinForbiddenAdvisor, which catches Spring Security's AccessDeniedException on any com.erp.fin.service method and re-raises it as this code — never thrown by hand in a service. Distinct from FIN_403_SOD_VIOLATION, a struck code with no thrower. |
| FIN_409_NOT_ACTIVE | `FIN-409-NOT-ACTIVE` | exception/FinErrorCodes.java | This definition is deactivated and cannot be run | هذا التعريف غير نشط ولا يمكن تشغيله | CONFLICT | 409 CONFLICT | 2 endpoint(s) — see their Business Responses | RECORDED HUMAN DECISION (no RULE-FIN-* states it) — the definition row the caller asked to run exists but is deactivated (= false), so the run is refused and no journal entry is built or posted. Thrown by RecurringTemplateDomain.assertCanRun() and AllocationRuleDomain.assertCanRun(). |
| FIN_409_MAPPING_DUP | `FIN-409-MAPPING-DUP` | exception/FinErrorCodes.java | An active mapping already exists for this event type, business field and value | يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها | ALREADY_EXISTS | 409 CONFLICT | 1 endpoint(s) — see their Business Responses | RULE-FIN-022 — an active mapping already holds the (event type, business field, business value) key: by the pre-check (QR-FIN-052) or by the store's refusal on UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY, translated by FinPersistenceRefusalAdvisor (ADR-FIN-030). API: API-FIN-039. HTTP 409 (Status.CONFLICT). |
| FIN_422_UNSUPPORTED_DERIVATION | `FIN-422-UNSUPPORTED-DERIVATION` | exception/FinErrorCodes.java | Unsupported account derivation type; only constant or mapping is allowed | نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط | BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT | 1 endpoint(s) — see their Business Responses | RULE-FIN-018 — a rule line's accountDerivationTypeCode is not an active ACCOUNT_DERIVATION_TYPE value (DIRECT included, retired in MDL). Thrown by EventTypeRuleDomain.assertDerivationSupported(...). API: API-FIN-011. HTTP 422 (Status.BUSINESS_RULE_VIOLATION). |
| FIN_422_DERIVATION_SPEC | `FIN-422-DERIVATION-SPEC` | exception/FinErrorCodes.java | The account derivation spec does not match its type | مواصفة اشتقاق الحساب لا تطابق نوعه | BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT | 1 endpoint(s) — see their Business Responses | RULE-FIN-019 — a CONSTANT line without an account code or with a business field, or a MAPPING line without a business field or with an account code. Thrown by EventTypeRuleDomain.assertDerivationSpec(...). API: API-FIN-011. HTTP 422 (Status.BUSINESS_RULE_VIOLATION). |
| FIN_422_UNMAPPED_VALUE | `FIN-422-UNMAPPED-VALUE` | exception/FinErrorCodes.java | No active account mapping exists for the business value in the event | لا يوجد ربط حساب نشط للقيمة التجارية في الحدث | BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT | 1 endpoint(s) — see their Business Responses | RULE-FIN-020 — no ACTIVE account mapping for the rule's event type, the line's business field and the event's value (QR-FIN-055). Thrown by AccountMappingDomain.assertResolved(...). API: API-FIN-020. HTTP 422 (Status.BUSINESS_RULE_VIOLATION). |
| FIN_422_MISSING_BUSINESS_FIELD | `FIN-422-MISSING-BUSINESS-FIELD` | exception/FinErrorCodes.java | The event does not carry the business field the rule requires | الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة | BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT | 1 endpoint(s) — see their Business Responses | RULE-FIN-021 — the event does not carry the business field a MAPPING line or a BUSINESS_FIELD tag names. Thrown by EventTypeRuleDomain.assertFieldPresent(...). API: API-FIN-020. HTTP 422 (Status.BUSINESS_RULE_VIOLATION). |
| FIN_422_MAPPED_ACCOUNT_INELIGIBLE | `FIN-422-MAPPED-ACCOUNT-INELIGIBLE` | exception/FinErrorCodes.java | The mapped account must be an active leaf account | الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا | BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT | 2 endpoint(s) — see their Business Responses | RULE-FIN-023 — the mapping's account is not a leaf or is inactive when the mapping is saved (QR-FIN-060). Thrown by AccountMappingDomain.assertAccountEligible(...). APIs: API-FIN-039, API-FIN-040. HTTP 422 (Status.BUSINESS_RULE_VIOLATION). |
| FIN_422_INVALID_PAYMENT_METHOD | `FIN-422-INVALID-PAYMENT-METHOD` | exception/FinErrorCodes.java | The value is not a defined payment method | القيمة ليست طريقة دفع معرَّفة | BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT | 1 endpoint(s) — see their Business Responses | RULE-FIN-024 — businessFieldCode is PAYMENT_METHOD and businessValue is not an active PAYMENT_METHOD code. Thrown by AccountMappingDomain.assertBusinessValueDefined(...). API: API-FIN-039. HTTP 422 (Status.BUSINESS_RULE_VIOLATION). |
| FIN_422_DIMENSION_TAG_DUP | `FIN-422-DIMENSION-TAG-DUP` | exception/FinErrorCodes.java | A rule line cannot tag the same dimension twice | لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة | BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT | 1 endpoint(s) — see their Business Responses | RULE-FIN-025 — two tags of one submitted rule line name the same dimension. Thrown by EventTypeRuleDomain.assertTagsDistinct(...). API: API-FIN-011. HTTP 422 (Status.BUSINESS_RULE_VIOLATION). |
| FIN_422_DIMENSION_TAG_SOURCE | `FIN-422-DIMENSION-TAG-SOURCE` | exception/FinErrorCodes.java | The dimension tag does not match its value source | وسم البُعد لا يطابق مصدر قيمته | BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT | 1 endpoint(s) — see their Business Responses | RULE-FIN-026 — a CONSTANT tag without a value of its own dimension or with a business field, or a BUSINESS_FIELD tag without a business field or with a value. Thrown by EventTypeRuleDomain.assertTagMatchesSource(...). API: API-FIN-011. HTTP 422 (Status.BUSINESS_RULE_VIOLATION). |
| FIN_422_UNRESOLVED_DIMENSION_VALUE | `FIN-422-UNRESOLVED-DIMENSION-VALUE` | exception/FinErrorCodes.java | The dimension value in the event matches no defined value of this dimension | قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد | BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT | 1 endpoint(s) — see their Business Responses | RULE-FIN-027 — the event's value for a BUSINESS_FIELD tag equals the code of no dimension value of the tagged dimension (QR-FIN-058). Thrown by DimensionValueDomain.assertResolvedByCode(...). API: API-FIN-020. HTTP 422 (Status.BUSINESS_RULE_VIOLATION). |
| FIN_422_NO_DEFINED_PERIOD | `FIN-422-NO-DEFINED-PERIOD` | exception/FinErrorCodes.java | No accounting period is defined for the event date | لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث | BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT | 1 endpoint(s) — see their Business Responses | RULE-FIN-028 — no fiscal period's [startDate, endDate] covers the event's docDate (QR-FIN-059); a calendar-setup error, never the retryable period-not-open answer. Thrown by FiscalPeriodDomain.assertDateCovered(...). API: API-FIN-020. HTTP 422 (Status.BUSINESS_RULE_VIOLATION). |
| FIN_404_ACCOUNT_MAPPING | `FIN-404-ACCOUNT-MAPPING` | exception/FinErrorCodes.java | Account mapping not found | ربط الحساب غير موجود | NOT_FOUND | 404 NOT_FOUND | 2 endpoint(s) — see their Business Responses | PLATFORM-STD (ADR-FIN-030) — unknown account-mapping id in the path. APIs: API-FIN-040, API-FIN-041. HTTP 404 (Status.NOT_FOUND). |
| VALIDATION_ERROR | `VALIDATION_ERROR` | com/erp/common/web/GlobalExceptionHandler.java | Validation failed | فشل التحقق من البيانات |  | 400 BAD_REQUEST | shared handler (any endpoint) | — |
| METHOD_NOT_ALLOWED | `METHOD_NOT_ALLOWED` | com/erp/common/web/GlobalExceptionHandler.java | The HTTP method is not supported for this resource | طريقة الطلب غير مدعومة لهذا المورد |  | 405 METHOD_NOT_ALLOWED | shared handler (any endpoint) | — |
| DATA_INTEGRITY_VIOLATION | `DATA_INTEGRITY_VIOLATION` | com/erp/common/web/GlobalExceptionHandler.java | The request could not be completed because it violates a data constraint | تعذّر إتمام الطلب لأنه يخالف قيدًا على البيانات |  | 409 CONFLICT | shared handler (any endpoint) | — |
| ACCESS_DENIED | `ACCESS_DENIED` | com/erp/common/web/GlobalExceptionHandler.java | You do not have permission to perform this operation | ليس لديك صلاحية لتنفيذ هذه العملية |  | 403 FORBIDDEN | shared handler (any endpoint) | — |
| INTERNAL_ERROR | `INTERNAL_ERROR` | com/erp/common/web/GlobalExceptionHandler.java | An unexpected error occurred. Please try again later. | حدث خطأ غير متوقع. يرجى المحاولة لاحقاً. |  | 500 INTERNAL_SERVER_ERROR | shared handler (any endpoint) | — |

## Status -> HTTP Status Reference

Shared, module-independent mapping every business error code's `Status` resolves through (see each error code's own Status column above, when known).

| Status | HTTP Status |
|---|---|
| ALREADY_EXISTS | 409 CONFLICT |
| BUSINESS_RULE_VIOLATION | 422 UNPROCESSABLE_CONTENT |
| CONFLICT | 409 CONFLICT |
| CREATED | 201 CREATED |
| FORBIDDEN | 403 FORBIDDEN |
| INTERNAL_ERROR | 500 INTERNAL_SERVER_ERROR |
| NOT_FOUND | 404 NOT_FOUND |
| PAYLOAD_TOO_LARGE | 413 CONTENT_TOO_LARGE |
| SUCCESS | 200 OK |
| UNAUTHORIZED | 401 UNAUTHORIZED |
| UNSUPPORTED_MEDIA_TYPE | 415 UNSUPPORTED_MEDIA_TYPE |
| UPDATED | 200 OK |
| VALIDATION_ERROR | 400 BAD_REQUEST |

## Contract Traceability

Contract ids joined from `backend-execution-plan-fin.md` (API REGISTRY): **41 of 42** served endpoints carry one.

Resolve a contract id (`API-FIN-002`, ...) to a path **here** — the API column of the catalog below, and the `Contract ID` line of each endpoint. A planning document states the path that was proposed, not the one that is served.

### Drift — declared vs served

| Kind | API | Method | Path | What this means |
|---|---|---|---|---|
| undeclared | — | POST | `/api/v1/fin/fiscal-years/search` | implemented but absent from the API REGISTRY — it has no contract id any other artifact can refer to |

## API Catalog

### FIN Account Management

| API | Method | Path | Summary | Doc |
|---|---|---|---|---|
| API-FIN-002 | POST | `/api/v1/fin/accounts` | Create account | [create_5](endpoints/fin-account-management.md#post-apiv1finaccounts) |
| API-FIN-001 | POST | `/api/v1/fin/accounts/search` | Search accounts | [search_7](endpoints/fin-account-management.md#post-apiv1finaccountssearch) |
| API-FIN-003 | PUT | `/api/v1/fin/accounts/{id}` | Update account | [update](endpoints/fin-account-management.md#put-apiv1finaccountsid) |
| API-FIN-004 | PUT | `/api/v1/fin/accounts/{id}/deactivate` | Deactivate account | [deactivate_3](endpoints/fin-account-management.md#put-apiv1finaccountsiddeactivate) |

### FIN Account Mapping Management

| API | Method | Path | Summary | Doc |
|---|---|---|---|---|
| API-FIN-039 | POST | `/api/v1/fin/account-mappings` | Create account mapping | [create_6](endpoints/fin-account-mapping-management.md#post-apiv1finaccount-mappings) |
| API-FIN-038 | POST | `/api/v1/fin/account-mappings/search` | Search account mappings | [search_8](endpoints/fin-account-mapping-management.md#post-apiv1finaccount-mappingssearch) |
| API-FIN-040 | PUT | `/api/v1/fin/account-mappings/{id}` | Update account mapping (the account only) | [update_1](endpoints/fin-account-mapping-management.md#put-apiv1finaccount-mappingsid) |
| API-FIN-041 | PUT | `/api/v1/fin/account-mappings/{id}/deactivate` | Deactivate account mapping | [deactivate_4](endpoints/fin-account-mapping-management.md#put-apiv1finaccount-mappingsiddeactivate) |

### FIN Allocation Rule Management

| API | Method | Path | Summary | Doc |
|---|---|---|---|---|
| API-FIN-016 | POST | `/api/v1/fin/allocation-rules` | Create allocation rule | [create_4](endpoints/fin-allocation-rule-management.md#post-apiv1finallocation-rules) |
| API-FIN-015 | POST | `/api/v1/fin/allocation-rules/search` | Search allocation rules | [search_6](endpoints/fin-allocation-rule-management.md#post-apiv1finallocation-rulessearch) |
| API-FIN-037 | PUT | `/api/v1/fin/allocation-rules/{id}/deactivate` | Deactivate allocation rule | [deactivate_2](endpoints/fin-allocation-rule-management.md#put-apiv1finallocation-rulesiddeactivate) |
| API-FIN-017 | POST | `/api/v1/fin/allocation-rules/{id}/run` | Run an allocation rule | [run_1](endpoints/fin-allocation-rule-management.md#post-apiv1finallocation-rulesidrun) |

### FIN Dimension Management

| API | Method | Path | Summary | Doc |
|---|---|---|---|---|
| API-FIN-006 | POST | `/api/v1/fin/dimensions` | Create dimension | [create_3](endpoints/fin-dimension-management.md#post-apiv1findimensions) |
| API-FIN-005 | POST | `/api/v1/fin/dimensions/search` | Search dimensions | [search_5](endpoints/fin-dimension-management.md#post-apiv1findimensionssearch) |
| API-FIN-008 | POST | `/api/v1/fin/dimensions/values/search` | Search dimension values | [searchDimensionValues](endpoints/fin-dimension-management.md#post-apiv1findimensionsvaluessearch) |
| API-FIN-035 | PUT | `/api/v1/fin/dimensions/values/{id}/deactivate` | Deactivate dimension value | [deactivateDimensionValue](endpoints/fin-dimension-management.md#put-apiv1findimensionsvaluesiddeactivate) |
| API-FIN-007 | POST | `/api/v1/fin/dimensions/{id}/values` | Create dimension value | [createDimensionValue](endpoints/fin-dimension-management.md#post-apiv1findimensionsidvalues) |

### FIN Event Rule Management

| API | Method | Path | Summary | Doc |
|---|---|---|---|---|
| API-FIN-010 | POST | `/api/v1/fin/event-rules` | Create event-type rule | [create_2](endpoints/fin-event-rule-management.md#post-apiv1finevent-rules) |
| API-FIN-009 | POST | `/api/v1/fin/event-rules/search` | Search event-type rules | [search_4](endpoints/fin-event-rule-management.md#post-apiv1finevent-rulessearch) |
| API-FIN-034 | PUT | `/api/v1/fin/event-rules/{id}/deactivate` | Deactivate event-type rule | [deactivate_1](endpoints/fin-event-rule-management.md#put-apiv1finevent-rulesiddeactivate) |
| API-FIN-011 | POST | `/api/v1/fin/event-rules/{id}/lines` | Add rule line | [createRuleLine](endpoints/fin-event-rule-management.md#post-apiv1finevent-rulesidlines) |

### FIN Financial Reports

| API | Method | Path | Summary | Doc |
|---|---|---|---|---|
| API-FIN-028 | GET | `/api/v1/fin/reports/account-ledger` | Account ledger | [accountLedger](endpoints/fin-financial-reports.md#get-apiv1finreportsaccount-ledger) |
| API-FIN-030 | GET | `/api/v1/fin/reports/balance-sheet` | Balance sheet | [balanceSheet](endpoints/fin-financial-reports.md#get-apiv1finreportsbalance-sheet) |
| API-FIN-032 | GET | `/api/v1/fin/reports/dimension` | Dimension report | [dimensionReport](endpoints/fin-financial-reports.md#get-apiv1finreportsdimension) |
| API-FIN-031 | GET | `/api/v1/fin/reports/income-statement` | Income statement | [incomeStatement](endpoints/fin-financial-reports.md#get-apiv1finreportsincome-statement) |
| API-FIN-029 | GET | `/api/v1/fin/reports/trial-balance` | Trial balance | [trialBalance](endpoints/fin-financial-reports.md#get-apiv1finreportstrial-balance) |

### FIN Fiscal Period Management

| API | Method | Path | Summary | Doc |
|---|---|---|---|---|
| API-FIN-033 | POST | `/api/v1/fin/fiscal-periods/search` | Search fiscal periods | [search_3](endpoints/fin-fiscal-period-management.md#post-apiv1finfiscal-periodssearch) |
| API-FIN-026 | PATCH | `/api/v1/fin/fiscal-periods/{id}/hard-close` | Hard-close a fiscal period (approval) | [hardClose](endpoints/fin-fiscal-period-management.md#patch-apiv1finfiscal-periodsidhard-close) |
| API-FIN-024 | PATCH | `/api/v1/fin/fiscal-periods/{id}/open` | Open a fiscal period | [open](endpoints/fin-fiscal-period-management.md#patch-apiv1finfiscal-periodsidopen) |
| API-FIN-025 | PATCH | `/api/v1/fin/fiscal-periods/{id}/soft-close` | Soft-close a fiscal period | [softClose](endpoints/fin-fiscal-period-management.md#patch-apiv1finfiscal-periodsidsoft-close) |

### FIN Fiscal Year Management

| API | Method | Path | Summary | Doc |
|---|---|---|---|---|
| API-FIN-023 | POST | `/api/v1/fin/fiscal-years` | Create fiscal year and generate its periods | [create_1](endpoints/fin-fiscal-year-management.md#post-apiv1finfiscal-years) |
| — | POST | `/api/v1/fin/fiscal-years/search` | Search fiscal years | [search_2](endpoints/fin-fiscal-year-management.md#post-apiv1finfiscal-yearssearch) |
| API-FIN-027 | POST | `/api/v1/fin/fiscal-years/{id}/year-end-close` | Run year-end close | [yearEndClose](endpoints/fin-fiscal-year-management.md#post-apiv1finfiscal-yearsidyear-end-close) |

### FIN Journal Entry Management

| API | Method | Path | Summary | Doc |
|---|---|---|---|---|
| API-FIN-019 | POST | `/api/v1/fin/journal-entries` | Create manual journal entry | [createManual](endpoints/fin-journal-entry-management.md#post-apiv1finjournal-entries) |
| API-FIN-020 | POST | `/api/v1/fin/journal-entries/from-event` | Build and post a journal entry from an accounting event | [buildFromEvent](endpoints/fin-journal-entry-management.md#post-apiv1finjournal-entriesfrom-event) |
| API-FIN-018 | POST | `/api/v1/fin/journal-entries/search` | Search journal entries | [search_1](endpoints/fin-journal-entry-management.md#post-apiv1finjournal-entriessearch) |
| API-FIN-022 | GET | `/api/v1/fin/journal-entries/{id}` | Read a journal entry with its lines | [read](endpoints/fin-journal-entry-management.md#get-apiv1finjournal-entriesid) |
| API-FIN-021 | POST | `/api/v1/fin/journal-entries/{id}/reverse` | Reverse a posted journal entry | [reverse](endpoints/fin-journal-entry-management.md#post-apiv1finjournal-entriesidreverse) |

### FIN Recurring Template Management

| API | Method | Path | Summary | Doc |
|---|---|---|---|---|
| API-FIN-013 | POST | `/api/v1/fin/recurring-templates` | Create recurring template | [create](endpoints/fin-recurring-template-management.md#post-apiv1finrecurring-templates) |
| API-FIN-012 | POST | `/api/v1/fin/recurring-templates/search` | Search recurring templates | [search](endpoints/fin-recurring-template-management.md#post-apiv1finrecurring-templatessearch) |
| API-FIN-036 | PUT | `/api/v1/fin/recurring-templates/{id}/deactivate` | Deactivate recurring template | [deactivate](endpoints/fin-recurring-template-management.md#put-apiv1finrecurring-templatesiddeactivate) |
| API-FIN-014 | POST | `/api/v1/fin/recurring-templates/{id}/run` | Run a recurring template | [run](endpoints/fin-recurring-template-management.md#post-apiv1finrecurring-templatesidrun) |

<!-- fetched-from: endpoints/fin-account-management.md -->
<!-- AUTO-GENERATED by api-doc-generator — do not edit manually -->
# FIN Account Management

**Endpoints in this file:**

- `API-FIN-002` — [POST /api/v1/fin/accounts](#post-apiv1finaccounts)
- `API-FIN-001` — [POST /api/v1/fin/accounts/search](#post-apiv1finaccountssearch)
- `API-FIN-003` — [PUT /api/v1/fin/accounts/{id}](#put-apiv1finaccountsid)
- `API-FIN-004` — [PUT /api/v1/fin/accounts/{id}/deactivate](#put-apiv1finaccountsiddeactivate)

## POST /api/v1/fin/accounts

**Create account**

إنشاء حساب في دليل الحسابات

Contract ID: `API-FIN-002`

Operation ID: `create_5`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_ACCOUNTS_CREATE (found on service:AccountService)

### Request Body

Schema: `AccountCreateRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| code | string | Yes | maxLength: 30 | Unique account code - رمز الحساب الفريد | 1101 |
| nameAr | string | Yes | maxLength: 200 | Name (Arabic) - الاسم بالعربية | النقدية بالصندوق |
| nameEn | string | Yes | maxLength: 200 | Name (English) - الاسم بالإنجليزية | Cash on hand |
| accountTypeCode | string | Yes | maxLength: 20 | Account type code, ACCOUNT_TYPE lookup - نوع الحساب | ASSET |
| natureCode | string | Yes | maxLength: 10 | Normal balance side, DEBIT_CREDIT lookup - طبيعة الحساب | DEBIT |
| parentAccountId | integer (int64) | No |  | Parent account id, omitted for a root account - معرّف الحساب الأب | 10 |
| isLeafFl | boolean | No |  | Accepts direct posting - يقبل الترحيل المباشر | true |

**Request Example**

```json
{
  "code": 1101,
  "nameAr": "النقدية بالصندوق",
  "nameEn": "Cash on hand",
  "accountTypeCode": "ASSET",
  "natureCode": "DEBIT",
  "parentAccountId": 10,
  "isLeafFl": true
}
```

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `AccountService.create`.

Shape: `AccountResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| accountPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| code | string | No |  | Unique account code - رمز الحساب الفريد | 1101 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | النقدية بالصندوق |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Cash on hand |
| accountTypeCode | string | No |  | Account type code, ACCOUNT_TYPE lookup - نوع الحساب | ASSET |
| natureCode | string | No |  | Normal balance side, DEBIT_CREDIT lookup - طبيعة الحساب | DEBIT |
| parentAccountId | integer (int64) | No |  | Parent account id - معرّف الحساب الأب | 10 |
| isLeafFl | boolean | No |  | Accepts direct posting - يقبل الترحيل المباشر | true |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| isRetainedEarningsFl | boolean | No |  | Marks the single Retained Earnings account used by year-end close; set once by the data seed (V34__fin_account_retained_earnings_seed.sql, DBF-FIN-147), never settable through the account APIs by decision - يُحدِّد حساب الأرباح المُبقاة الوحيد المستخدم في إقفال نهاية السنة | false |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "accountPk": 1,
  "code": 1101,
  "nameAr": "النقدية بالصندوق",
  "nameEn": "Cash on hand",
  "accountTypeCode": "ASSET",
  "natureCode": "DEBIT",
  "parentAccountId": 10,
  "isLeafFl": true,
  "isActiveFl": true,
  "isRetainedEarningsFl": false
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `AccountController.create`, `AccountService.create`, `FinLookupValidationService.assertValidCode`, `AccountService.resolveParent`, `AccountDomain.create`, `AccountMapper.toEntity`, `AccountMapper.toResponse`, `new AccountDomain()`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-LOOKUP` | FIN_400_INVALID_LOOKUP | FinLookupValidationService.assertValidCode |
| 404 NOT_FOUND | `FIN-404-ACCOUNT` | FIN_404_ACCOUNT | AccountService.resolveParent |
| 409 CONFLICT | `FIN-409-ACCOUNT-DUP` | FIN_409_ACCOUNT_DUP | AccountService.create |
| 409 CONFLICT | `FIN-409-PARENT-NOT-LEAF-ELIGIBLE` | FIN_409_PARENT_NOT_LEAF_ELIGIBLE | AccountDomain.create |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## POST /api/v1/fin/accounts/search

**Search accounts**

بحث في دليل الحسابات

Contract ID: `API-FIN-001`

Operation ID: `search_7`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_ACCOUNTS_VIEW (found on service:AccountService)

### Request Body

Schema: `AccountSearchRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| filters | array<SearchFilter> | No |  | Filter criteria. Supported fields: code (LIKE), nameAr / nameEn (LIKE), accountTypeCode (EQUALS, IN - ACCOUNT_TYPE), natureCode (EQUALS - DEBIT_CREDIT), parentAccountId (EQUALS), isLeafFl (EQUALS), isActiveFl (EQUALS), accountPk (EQUALS, IN), createdAt (comparison operators). Any other field, or an operator a field does not list, is rejected as 400 VALIDATION_ERROR naming it - معايير التصفية |  |
| filters[].field | string | No |  |  |  |
| filters[].operator | string | No | enum: EQUALS, NOT_EQUALS, LIKE, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, IN |  |  |
| filters[].value | object | No |  |  |  |
| sortField | string | No |  | Sort field. Supported: accountPk, code, nameAr, nameEn, accountTypeCode, natureCode, isLeafFl, isActiveFl, createdAt. Any other value is rejected as 400 FIN-400-INVALID-SORT - حقل الترتيب |  |
| sortDirection | string | No | enum: ASC, DESC | Sort direction - اتجاه الترتيب |  |
| page | integer (int32) | No |  | Page number, zero-based - رقم الصفحة | 0 |
| size | integer (int32) | No |  | Page size - حجم الصفحة | 20 |

**Request Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "page": 0,
  "size": 20
}
```

### Response `200` — OK

Shape: `paginated list of AccountResponse (see Pagination Envelope in index.md)`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| accountPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| code | string | No |  | Unique account code - رمز الحساب الفريد | 1101 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | النقدية بالصندوق |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Cash on hand |
| accountTypeCode | string | No |  | Account type code, ACCOUNT_TYPE lookup - نوع الحساب | ASSET |
| natureCode | string | No |  | Normal balance side, DEBIT_CREDIT lookup - طبيعة الحساب | DEBIT |
| parentAccountId | integer (int64) | No |  | Parent account id - معرّف الحساب الأب | 10 |
| isLeafFl | boolean | No |  | Accepts direct posting - يقبل الترحيل المباشر | true |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| isRetainedEarningsFl | boolean | No |  | Marks the single Retained Earnings account used by year-end close; set once by the data seed (V34__fin_account_retained_earnings_seed.sql, DBF-FIN-147), never settable through the account APIs by decision - يُحدِّد حساب الأرباح المُبقاة الوحيد المستخدم في إقفال نهاية السنة | false |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "accountPk": 1,
  "code": 1101,
  "nameAr": "النقدية بالصندوق",
  "nameEn": "Cash on hand",
  "accountTypeCode": "ASSET",
  "natureCode": "DEBIT",
  "parentAccountId": 10,
  "isLeafFl": true,
  "isActiveFl": true,
  "isRetainedEarningsFl": false
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `AccountController.search`, `AccountService.search`, `FinSearchSupport.assertSortAllowed`, `AccountSearchRequest.getSortField`, `AccountSearchRequest.toCommonSearchRequest`, `FinSearchSupport.temporalFieldConverter`, `AccountSearchRequest.getParentAccountId`, `AccountMapper.toResponse`, `FinSearchSupport.temporalFieldConverter`, `FinSearchSupport.parseLocalDate`, `FinSearchSupport.parseInstant`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-SORT` | FIN_400_INVALID_SORT | FinSearchSupport.assertSortAllowed |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseInstant |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseLocalDate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## PUT /api/v1/fin/accounts/{id}

**Update account**

تعديل حساب

Contract ID: `API-FIN-003`

Operation ID: `update`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_ACCOUNTS_UPDATE (found on service:AccountService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Request Body

Schema: `AccountUpdateRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| nameAr | string | Yes | maxLength: 200 | Name (Arabic) - الاسم بالعربية | النقدية بالصندوق |
| nameEn | string | Yes | maxLength: 200 | Name (English) - الاسم بالإنجليزية | Cash on hand |
| isLeafFl | boolean | Yes |  | Accepts direct posting - يقبل الترحيل المباشر | true |

**Request Example**

```json
{
  "nameAr": "النقدية بالصندوق",
  "nameEn": "Cash on hand",
  "isLeafFl": true
}
```

### Response `200` — OK

Shape: `AccountResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| accountPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| code | string | No |  | Unique account code - رمز الحساب الفريد | 1101 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | النقدية بالصندوق |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Cash on hand |
| accountTypeCode | string | No |  | Account type code, ACCOUNT_TYPE lookup - نوع الحساب | ASSET |
| natureCode | string | No |  | Normal balance side, DEBIT_CREDIT lookup - طبيعة الحساب | DEBIT |
| parentAccountId | integer (int64) | No |  | Parent account id - معرّف الحساب الأب | 10 |
| isLeafFl | boolean | No |  | Accepts direct posting - يقبل الترحيل المباشر | true |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| isRetainedEarningsFl | boolean | No |  | Marks the single Retained Earnings account used by year-end close; set once by the data seed (V34__fin_account_retained_earnings_seed.sql, DBF-FIN-147), never settable through the account APIs by decision - يُحدِّد حساب الأرباح المُبقاة الوحيد المستخدم في إقفال نهاية السنة | false |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "accountPk": 1,
  "code": 1101,
  "nameAr": "النقدية بالصندوق",
  "nameEn": "Cash on hand",
  "accountTypeCode": "ASSET",
  "natureCode": "DEBIT",
  "parentAccountId": 10,
  "isLeafFl": true,
  "isActiveFl": true,
  "isRetainedEarningsFl": false
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `AccountController.update`, `AccountService.update`, `AccountDomain.from`, `AccountMapper.updateEntityFromRequest`, `AccountMapper.toResponse`, `AccountDomain.assertLeafFlagChangeAllowed`, `new AccountDomain()`, `AccountDomain.assertCanBeMarkedLeaf`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-ACCOUNT` | FIN_404_ACCOUNT | AccountService.update |
| 409 CONFLICT | `FIN-409-HAS-CHILDREN` | FIN_409_HAS_CHILDREN | AccountDomain.assertCanBeMarkedLeaf |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## PUT /api/v1/fin/accounts/{id}/deactivate

**Deactivate account**

إلغاء تفعيل حساب

Contract ID: `API-FIN-004`

Operation ID: `deactivate_3`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_ACCOUNTS_UPDATE (found on service:AccountService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `200` — OK

Shape: `AccountResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| accountPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| code | string | No |  | Unique account code - رمز الحساب الفريد | 1101 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | النقدية بالصندوق |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Cash on hand |
| accountTypeCode | string | No |  | Account type code, ACCOUNT_TYPE lookup - نوع الحساب | ASSET |
| natureCode | string | No |  | Normal balance side, DEBIT_CREDIT lookup - طبيعة الحساب | DEBIT |
| parentAccountId | integer (int64) | No |  | Parent account id - معرّف الحساب الأب | 10 |
| isLeafFl | boolean | No |  | Accepts direct posting - يقبل الترحيل المباشر | true |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| isRetainedEarningsFl | boolean | No |  | Marks the single Retained Earnings account used by year-end close; set once by the data seed (V34__fin_account_retained_earnings_seed.sql, DBF-FIN-147), never settable through the account APIs by decision - يُحدِّد حساب الأرباح المُبقاة الوحيد المستخدم في إقفال نهاية السنة | false |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "accountPk": 1,
  "code": 1101,
  "nameAr": "النقدية بالصندوق",
  "nameEn": "Cash on hand",
  "accountTypeCode": "ASSET",
  "natureCode": "DEBIT",
  "parentAccountId": 10,
  "isLeafFl": true,
  "isActiveFl": true,
  "isRetainedEarningsFl": false
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `AccountController.deactivate`, `AccountService.deactivate`, `Account.deactivate`, `AccountMapper.toResponse`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-ACCOUNT` | FIN_404_ACCOUNT | AccountService.deactivate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

<!-- fetched-from: endpoints/fin-account-mapping-management.md -->
<!-- AUTO-GENERATED by api-doc-generator — do not edit manually -->
# FIN Account Mapping Management

**Endpoints in this file:**

- `API-FIN-039` — [POST /api/v1/fin/account-mappings](#post-apiv1finaccount-mappings)
- `API-FIN-038` — [POST /api/v1/fin/account-mappings/search](#post-apiv1finaccount-mappingssearch)
- `API-FIN-040` — [PUT /api/v1/fin/account-mappings/{id}](#put-apiv1finaccount-mappingsid)
- `API-FIN-041` — [PUT /api/v1/fin/account-mappings/{id}/deactivate](#put-apiv1finaccount-mappingsiddeactivate)

## POST /api/v1/fin/account-mappings

**Create account mapping**

إنشاء ربط حساب

Contract ID: `API-FIN-039`

Operation ID: `create_6`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_ACCOUNT_MAPPINGS_CREATE (found on service:AccountMappingService)

### Request Body

Schema: `AccountMappingCreateRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| eventTypeCode | string | Yes | maxLength: 50 | Event type, ACCOUNTING_EVENT_TYPE lookup - نوع الحدث | CASH_PAYMENT |
| businessFieldCode | string | Yes | maxLength: 50 | Business field, FIN_EVENT_BUSINESS_FIELD lookup - الحقل التجاري | PAYMENT_METHOD |
| businessValue | string | Yes | maxLength: 100 | Business value stored as a code - القيمة التجارية | CHEQUE |
| accountId | integer (int64) | Yes |  | Mapped account id, an active leaf account - معرّف الحساب | 12 |

**Request Example**

```json
{
  "eventTypeCode": "CASH_PAYMENT",
  "businessFieldCode": "PAYMENT_METHOD",
  "businessValue": "CHEQUE",
  "accountId": 12
}
```

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `AccountMappingService.create`.

Shape: `AccountMappingResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| accountMappingPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| eventTypeCode | string | No |  | Event type, ACCOUNTING_EVENT_TYPE lookup - نوع الحدث | CASH_PAYMENT |
| businessFieldCode | string | No |  | Business field, FIN_EVENT_BUSINESS_FIELD lookup - الحقل التجاري | PAYMENT_METHOD |
| businessValue | string | No |  | Business value stored as a code - القيمة التجارية | CHEQUE |
| accountId | integer (int64) | No |  | Mapped account id - معرّف الحساب | 12 |
| accountCode | string | No |  | Mapped account code - رمز الحساب | 1101 |
| accountNameAr | string | No |  | Mapped account name (Arabic) - اسم الحساب بالعربية | النقدية بالصندوق |
| accountNameEn | string | No |  | Mapped account name (English) - اسم الحساب بالإنجليزية | Cash on hand |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "accountMappingPk": 1,
  "eventTypeCode": "CASH_PAYMENT",
  "businessFieldCode": "PAYMENT_METHOD",
  "businessValue": "CHEQUE",
  "accountId": 12,
  "accountCode": 1101,
  "accountNameAr": "النقدية بالصندوق",
  "accountNameEn": "Cash on hand",
  "isActiveFl": true
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `AccountMappingController.create`, `AccountMappingService.create`, `FinLookupValidationService.assertValidCode`, `AccountMappingDomain.assertBusinessValueDefined`, `FinLookupValidationService.isActiveCode`, `AccountMappingService.resolveAccount`, `AccountMappingDomain.assertAccountEligible`, `AccountMappingDomain.create`, `AccountMappingMapper.toEntity`, `AccountMappingMapper.toResponse`, `AccountMappingDomain.assertKeyFree`, `new AccountMappingDomain()`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-LOOKUP` | FIN_400_INVALID_LOOKUP | FinLookupValidationService.assertValidCode |
| 404 NOT_FOUND | `FIN-404-ACCOUNT` | FIN_404_ACCOUNT | AccountMappingService.resolveAccount |
| 409 CONFLICT | `FIN-409-MAPPING-DUP` | FIN_409_MAPPING_DUP | AccountMappingDomain.assertKeyFree |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-INVALID-PAYMENT-METHOD` | FIN_422_INVALID_PAYMENT_METHOD | AccountMappingDomain.assertBusinessValueDefined |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-MAPPED-ACCOUNT-INELIGIBLE` | FIN_422_MAPPED_ACCOUNT_INELIGIBLE | AccountMappingDomain.assertAccountEligible |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## POST /api/v1/fin/account-mappings/search

**Search account mappings**

البحث في روابط الحسابات

Contract ID: `API-FIN-038`

Operation ID: `search_8`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_ACCOUNT_MAPPINGS_VIEW (found on service:AccountMappingService)

### Request Body

Schema: `AccountMappingSearchRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| filters | array<SearchFilter> | No |  | Filter criteria. Supported fields: eventTypeCode (EQUALS, IN - ACCOUNTING_EVENT_TYPE), businessFieldCode (EQUALS, IN - FIN_EVENT_BUSINESS_FIELD), businessValue (LIKE), accountId (EQUALS), isActiveFl (EQUALS), accountMappingPk (EQUALS, IN), createdAt (comparison operators). Any other field, or an operator a field does not list, is rejected as 400 VALIDATION_ERROR naming it - معايير التصفية |  |
| filters[].field | string | No |  |  |  |
| filters[].operator | string | No | enum: EQUALS, NOT_EQUALS, LIKE, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, IN |  |  |
| filters[].value | object | No |  |  |  |
| sortField | string | No |  | Sort field. Supported: accountMappingPk, eventTypeCode, businessFieldCode, businessValue, isActiveFl, createdAt. Any other value is rejected as 400 FIN-400-INVALID-SORT - حقل الترتيب |  |
| sortDirection | string | No | enum: ASC, DESC | Sort direction - اتجاه الترتيب |  |
| page | integer (int32) | No |  | Page number, zero-based - رقم الصفحة | 0 |
| size | integer (int32) | No |  | Page size - حجم الصفحة | 20 |

**Request Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "page": 0,
  "size": 20
}
```

### Response `200` — OK

Shape: `paginated list of AccountMappingResponse (see Pagination Envelope in index.md)`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| accountMappingPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| eventTypeCode | string | No |  | Event type, ACCOUNTING_EVENT_TYPE lookup - نوع الحدث | CASH_PAYMENT |
| businessFieldCode | string | No |  | Business field, FIN_EVENT_BUSINESS_FIELD lookup - الحقل التجاري | PAYMENT_METHOD |
| businessValue | string | No |  | Business value stored as a code - القيمة التجارية | CHEQUE |
| accountId | integer (int64) | No |  | Mapped account id - معرّف الحساب | 12 |
| accountCode | string | No |  | Mapped account code - رمز الحساب | 1101 |
| accountNameAr | string | No |  | Mapped account name (Arabic) - اسم الحساب بالعربية | النقدية بالصندوق |
| accountNameEn | string | No |  | Mapped account name (English) - اسم الحساب بالإنجليزية | Cash on hand |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "accountMappingPk": 1,
  "eventTypeCode": "CASH_PAYMENT",
  "businessFieldCode": "PAYMENT_METHOD",
  "businessValue": "CHEQUE",
  "accountId": 12,
  "accountCode": 1101,
  "accountNameAr": "النقدية بالصندوق",
  "accountNameEn": "Cash on hand",
  "isActiveFl": true
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `AccountMappingController.search`, `AccountMappingService.search`, `FinSearchSupport.assertSortAllowed`, `AccountMappingSearchRequest.getSortField`, `AccountMappingSearchRequest.toCommonSearchRequest`, `FinSearchSupport.temporalFieldConverter`, `AccountMappingSearchRequest.getAccountId`, `AccountMappingMapper.toResponse`, `FinSearchSupport.temporalFieldConverter`, `FinSearchSupport.parseLocalDate`, `FinSearchSupport.parseInstant`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-SORT` | FIN_400_INVALID_SORT | FinSearchSupport.assertSortAllowed |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseInstant |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseLocalDate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## PUT /api/v1/fin/account-mappings/{id}

**Update account mapping (the account only)**

تغيير حساب الربط

Contract ID: `API-FIN-040`

Operation ID: `update_1`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_ACCOUNT_MAPPINGS_UPDATE (found on service:AccountMappingService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Request Body

Schema: `AccountMappingUpdateRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| accountId | integer (int64) | Yes |  | Mapped account id, an active leaf account - معرّف الحساب | 12 |

**Request Example**

```json
{
  "accountId": 12
}
```

### Response `200` — OK

Shape: `AccountMappingResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| accountMappingPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| eventTypeCode | string | No |  | Event type, ACCOUNTING_EVENT_TYPE lookup - نوع الحدث | CASH_PAYMENT |
| businessFieldCode | string | No |  | Business field, FIN_EVENT_BUSINESS_FIELD lookup - الحقل التجاري | PAYMENT_METHOD |
| businessValue | string | No |  | Business value stored as a code - القيمة التجارية | CHEQUE |
| accountId | integer (int64) | No |  | Mapped account id - معرّف الحساب | 12 |
| accountCode | string | No |  | Mapped account code - رمز الحساب | 1101 |
| accountNameAr | string | No |  | Mapped account name (Arabic) - اسم الحساب بالعربية | النقدية بالصندوق |
| accountNameEn | string | No |  | Mapped account name (English) - اسم الحساب بالإنجليزية | Cash on hand |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "accountMappingPk": 1,
  "eventTypeCode": "CASH_PAYMENT",
  "businessFieldCode": "PAYMENT_METHOD",
  "businessValue": "CHEQUE",
  "accountId": 12,
  "accountCode": 1101,
  "accountNameAr": "النقدية بالصندوق",
  "accountNameEn": "Cash on hand",
  "isActiveFl": true
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `AccountMappingController.update`, `AccountMappingService.update`, `AccountMappingService.resolveAccount`, `AccountMappingDomain.assertAccountEligible`, `AccountMappingMapper.updateEntityFromRequest`, `AccountMappingMapper.toResponse`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-ACCOUNT` | FIN_404_ACCOUNT | AccountMappingService.resolveAccount |
| 404 NOT_FOUND | `FIN-404-ACCOUNT-MAPPING` | FIN_404_ACCOUNT_MAPPING | AccountMappingService.update |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-MAPPED-ACCOUNT-INELIGIBLE` | FIN_422_MAPPED_ACCOUNT_INELIGIBLE | AccountMappingDomain.assertAccountEligible |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## PUT /api/v1/fin/account-mappings/{id}/deactivate

**Deactivate account mapping**

إلغاء تفعيل ربط حساب

Contract ID: `API-FIN-041`

Operation ID: `deactivate_4`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_ACCOUNT_MAPPINGS_UPDATE (found on service:AccountMappingService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `200` — OK

Shape: `AccountMappingResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| accountMappingPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| eventTypeCode | string | No |  | Event type, ACCOUNTING_EVENT_TYPE lookup - نوع الحدث | CASH_PAYMENT |
| businessFieldCode | string | No |  | Business field, FIN_EVENT_BUSINESS_FIELD lookup - الحقل التجاري | PAYMENT_METHOD |
| businessValue | string | No |  | Business value stored as a code - القيمة التجارية | CHEQUE |
| accountId | integer (int64) | No |  | Mapped account id - معرّف الحساب | 12 |
| accountCode | string | No |  | Mapped account code - رمز الحساب | 1101 |
| accountNameAr | string | No |  | Mapped account name (Arabic) - اسم الحساب بالعربية | النقدية بالصندوق |
| accountNameEn | string | No |  | Mapped account name (English) - اسم الحساب بالإنجليزية | Cash on hand |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "accountMappingPk": 1,
  "eventTypeCode": "CASH_PAYMENT",
  "businessFieldCode": "PAYMENT_METHOD",
  "businessValue": "CHEQUE",
  "accountId": 12,
  "accountCode": 1101,
  "accountNameAr": "النقدية بالصندوق",
  "accountNameEn": "Cash on hand",
  "isActiveFl": true
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `AccountMappingController.deactivate`, `AccountMappingService.deactivate`, `AccountMapping.deactivate`, `AccountMappingMapper.toResponse`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-ACCOUNT-MAPPING` | FIN_404_ACCOUNT_MAPPING | AccountMappingService.deactivate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

<!-- fetched-from: endpoints/fin-allocation-rule-management.md -->
<!-- AUTO-GENERATED by api-doc-generator — do not edit manually -->
# FIN Allocation Rule Management

**Endpoints in this file:**

- `API-FIN-016` — [POST /api/v1/fin/allocation-rules](#post-apiv1finallocation-rules)
- `API-FIN-015` — [POST /api/v1/fin/allocation-rules/search](#post-apiv1finallocation-rulessearch)
- `API-FIN-037` — [PUT /api/v1/fin/allocation-rules/{id}/deactivate](#put-apiv1finallocation-rulesiddeactivate)
- `API-FIN-017` — [POST /api/v1/fin/allocation-rules/{id}/run](#post-apiv1finallocation-rulesidrun)

## POST /api/v1/fin/allocation-rules

**Create allocation rule**

إنشاء قاعدة توزيع تكلفة

Contract ID: `API-FIN-016`

Operation ID: `create_4`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_ALLOCATION_RULES_CREATE (found on service:AllocationRuleService)

### Request Body

Schema: `AllocationRuleCreateRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| nameAr | string | Yes | maxLength: 150 | Name (Arabic) - الاسم بالعربية | توزيع مصاريف الإدارة |
| nameEn | string | Yes | maxLength: 150 | Name (English) - الاسم بالإنجليزية | Administrative expense allocation |
| sourceAccountId | integer (int64) | Yes |  | Source account id - معرّف الحساب المصدر | 31 |
| targets | array<AllocationTargetCreateRequest> | Yes |  | Allocation targets — when any target uses PERCENTAGE the set must also carry exactly one REMAINDER target (ENT-FIN-014, RULE-FIN-003 reused) - أهداف التوزيع — One entry of a rule's target set — the field examples below describe a single PERCENTAGE entry; the complete, valid payload is on AllocationRuleCreateRequest - هدف قاعدة التوزيع | [{"targetAccountId": 21, "dimensionValueId": 5, "distributionTypeCode": "PERCENTAGE", "distributionValue": 25.0, "isRemainderFl": false}, {"targetAccountId": 22, "distributionTypeCode": "REMAINDER", "isRemainderFl": true}] |
| targets[].targetAccountId | integer (int64) | Yes |  | Target account id - معرّف الحساب المستهدف | 21 |
| targets[].dimensionValueId | integer (int64) | No |  | Optional dimension value id - معرّف قيمة البُعد | 5 |
| targets[].distributionTypeCode | string | Yes | maxLength: 15 | Distribution type, DISTRIBUTION_TYPE lookup - نوع التوزيع | PERCENTAGE |
| targets[].distributionValue | number | No |  | Distribution value — the percentage or fixed amount; omitted for the remainder target - قيمة التوزيع | 25.0 |
| targets[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution — exactly one target per rule must set this true when any target is PERCENTAGE, and it must be the target whose distributionTypeCode is REMAINDER (ENT-FIN-014, RULE-FIN-003 reused) - هدف الباقي | false |

**Request Example**

```json
{
  "nameAr": "توزيع مصاريف الإدارة",
  "nameEn": "Administrative expense allocation",
  "sourceAccountId": 31,
  "targets": [
    {
      "targetAccountId": 21,
      "dimensionValueId": 5,
      "distributionTypeCode": "PERCENTAGE",
      "distributionValue": 25.0,
      "isRemainderFl": false
    },
    {
      "targetAccountId": 22,
      "distributionTypeCode": "REMAINDER",
      "isRemainderFl": true
    }
  ]
}
```

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `AllocationRuleService.create`.

Shape: `AllocationRuleResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| allocationRulePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | توزيع مصاريف الإدارة |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Administrative expense allocation |
| sourceAccountId | integer (int64) | No |  | Source account id - معرّف الحساب المصدر | 31 |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| targetCount | integer (int32) | No |  | Number of allocation targets - عدد الأهداف | 3 |
| targets | array<AllocationTargetResponse> | No |  | Allocation targets - أهداف التوزيع — Allocation rule target - هدف قاعدة التوزيع |  |
| targets[].allocationTargetPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| targets[].allocationRuleId | integer (int64) | No |  | Parent allocation rule id - معرّف قاعدة التوزيع الأب | 1 |
| targets[].lineNo | integer (int32) | No |  | Target position within the rule - رقم السطر | 1 |
| targets[].targetAccountId | integer (int64) | No |  | Target account id - معرّف الحساب المستهدف | 21 |
| targets[].dimensionValueId | integer (int64) | No |  | Optional dimension value id - معرّف قيمة البُعد | 5 |
| targets[].distributionTypeCode | string | No |  | Distribution type, DISTRIBUTION_TYPE lookup - نوع التوزيع | PERCENTAGE |
| targets[].distributionValue | number | No |  | Distribution value - قيمة التوزيع | 25.0 |
| targets[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - هدف الباقي | false |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "allocationRulePk": 1,
  "nameAr": "توزيع مصاريف الإدارة",
  "nameEn": "Administrative expense allocation",
  "sourceAccountId": 31,
  "isActiveFl": true,
  "targetCount": 3,
  "targets": [
    {
      "allocationTargetPk": 1,
      "allocationRuleId": 1,
      "lineNo": 1,
      "targetAccountId": 21,
      "dimensionValueId": 5,
      "distributionTypeCode": "PERCENTAGE",
      "distributionValue": 25.0,
      "isRemainderFl": false
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `AllocationRuleController.create`, `AllocationRuleService.create`, `AllocationRuleService.resolveAccount`, `FinLookupValidationService.assertValidCode`, `AllocationRuleMapper.toEntity`, `AllocationTargetMapper.toEntity`, `AllocationRuleService.resolveDimensionValue`, `AllocationRuleDomain.create`, `AllocationRuleMapper.toResponse`, `AllocationTargetMapper.toResponse`, `AllocationRuleDomain.assertRemainderTargetSetValid`, `new AllocationRuleDomain()`, `AllocationRuleDomain.isRemainderTarget`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-LOOKUP` | FIN_400_INVALID_LOOKUP | FinLookupValidationService.assertValidCode |
| 404 NOT_FOUND | `FIN-404-ACCOUNT` | FIN_404_ACCOUNT | AllocationRuleService.resolveAccount |
| 409 CONFLICT | `FIN-409-INVALID-DIMENSION` | FIN_409_INVALID_DIMENSION | AllocationRuleService.resolveDimensionValue |
| 409 CONFLICT | `FIN-409-REMAINDER-COUNT` | FIN_409_REMAINDER_COUNT | AllocationRuleDomain.assertRemainderTargetSetValid |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-REMAINDER-MARKER` | FIN_422_REMAINDER_MARKER | AllocationRuleDomain.assertRemainderTargetSetValid |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## POST /api/v1/fin/allocation-rules/search

**Search allocation rules**

بحث في قواعد التوزيع

Contract ID: `API-FIN-015`

Operation ID: `search_6`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_ALLOCATION_RULES_VIEW (found on service:AllocationRuleService)

### Request Body

Schema: `AllocationRuleSearchRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| filters | array<SearchFilter> | No |  | Filter criteria. Supported fields: nameAr / nameEn (LIKE), sourceAccountId (EQUALS), isActiveFl (EQUALS), allocationRulePk (EQUALS, IN), createdAt (comparison operators). Any other field, or an operator a field does not list, is rejected as 400 VALIDATION_ERROR naming it - معايير التصفية |  |
| filters[].field | string | No |  |  |  |
| filters[].operator | string | No | enum: EQUALS, NOT_EQUALS, LIKE, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, IN |  |  |
| filters[].value | object | No |  |  |  |
| sortField | string | No |  | Sort field. Supported: allocationRulePk, nameAr, nameEn, isActiveFl, createdAt. Any other value is rejected as 400 FIN-400-INVALID-SORT - حقل الترتيب |  |
| sortDirection | string | No | enum: ASC, DESC | Sort direction - اتجاه الترتيب |  |
| page | integer (int32) | No |  | Page number, zero-based - رقم الصفحة | 0 |
| size | integer (int32) | No |  | Page size - حجم الصفحة | 20 |

**Request Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "page": 0,
  "size": 20
}
```

### Response `200` — OK

Shape: `paginated list of AllocationRuleResponse (see Pagination Envelope in index.md)`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| allocationRulePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | توزيع مصاريف الإدارة |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Administrative expense allocation |
| sourceAccountId | integer (int64) | No |  | Source account id - معرّف الحساب المصدر | 31 |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| targetCount | integer (int32) | No |  | Number of allocation targets - عدد الأهداف | 3 |
| targets | array<AllocationTargetResponse> | No |  | Allocation targets - أهداف التوزيع — Allocation rule target - هدف قاعدة التوزيع |  |
| targets[].allocationTargetPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| targets[].allocationRuleId | integer (int64) | No |  | Parent allocation rule id - معرّف قاعدة التوزيع الأب | 1 |
| targets[].lineNo | integer (int32) | No |  | Target position within the rule - رقم السطر | 1 |
| targets[].targetAccountId | integer (int64) | No |  | Target account id - معرّف الحساب المستهدف | 21 |
| targets[].dimensionValueId | integer (int64) | No |  | Optional dimension value id - معرّف قيمة البُعد | 5 |
| targets[].distributionTypeCode | string | No |  | Distribution type, DISTRIBUTION_TYPE lookup - نوع التوزيع | PERCENTAGE |
| targets[].distributionValue | number | No |  | Distribution value - قيمة التوزيع | 25.0 |
| targets[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - هدف الباقي | false |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "allocationRulePk": 1,
  "nameAr": "توزيع مصاريف الإدارة",
  "nameEn": "Administrative expense allocation",
  "sourceAccountId": 31,
  "isActiveFl": true,
  "targetCount": 3,
  "targets": [
    {
      "allocationTargetPk": 1,
      "allocationRuleId": 1,
      "lineNo": 1,
      "targetAccountId": 21,
      "dimensionValueId": 5,
      "distributionTypeCode": "PERCENTAGE",
      "distributionValue": 25.0,
      "isRemainderFl": false
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `AllocationRuleController.search`, `AllocationRuleService.search`, `FinSearchSupport.assertSortAllowed`, `AllocationRuleSearchRequest.getSortField`, `AllocationRuleSearchRequest.toCommonSearchRequest`, `FinSearchSupport.temporalFieldConverter`, `AllocationRuleSearchRequest.getSourceAccountId`, `AllocationRuleMapper.toResponse`, `AllocationTargetMapper.toResponse`, `FinSearchSupport.temporalFieldConverter`, `FinSearchSupport.parseLocalDate`, `FinSearchSupport.parseInstant`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-SORT` | FIN_400_INVALID_SORT | FinSearchSupport.assertSortAllowed |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseInstant |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseLocalDate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## PUT /api/v1/fin/allocation-rules/{id}/deactivate

**Deactivate allocation rule**

إلغاء تفعيل قاعدة توزيع تكلفة

Contract ID: `API-FIN-037`

Operation ID: `deactivate_2`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_ALLOCATION_RULES_UPDATE (found on service:AllocationRuleService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `200` — OK

Shape: `AllocationRuleResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| allocationRulePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | توزيع مصاريف الإدارة |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Administrative expense allocation |
| sourceAccountId | integer (int64) | No |  | Source account id - معرّف الحساب المصدر | 31 |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| targetCount | integer (int32) | No |  | Number of allocation targets - عدد الأهداف | 3 |
| targets | array<AllocationTargetResponse> | No |  | Allocation targets - أهداف التوزيع — Allocation rule target - هدف قاعدة التوزيع |  |
| targets[].allocationTargetPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| targets[].allocationRuleId | integer (int64) | No |  | Parent allocation rule id - معرّف قاعدة التوزيع الأب | 1 |
| targets[].lineNo | integer (int32) | No |  | Target position within the rule - رقم السطر | 1 |
| targets[].targetAccountId | integer (int64) | No |  | Target account id - معرّف الحساب المستهدف | 21 |
| targets[].dimensionValueId | integer (int64) | No |  | Optional dimension value id - معرّف قيمة البُعد | 5 |
| targets[].distributionTypeCode | string | No |  | Distribution type, DISTRIBUTION_TYPE lookup - نوع التوزيع | PERCENTAGE |
| targets[].distributionValue | number | No |  | Distribution value - قيمة التوزيع | 25.0 |
| targets[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - هدف الباقي | false |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "allocationRulePk": 1,
  "nameAr": "توزيع مصاريف الإدارة",
  "nameEn": "Administrative expense allocation",
  "sourceAccountId": 31,
  "isActiveFl": true,
  "targetCount": 3,
  "targets": [
    {
      "allocationTargetPk": 1,
      "allocationRuleId": 1,
      "lineNo": 1,
      "targetAccountId": 21,
      "dimensionValueId": 5,
      "distributionTypeCode": "PERCENTAGE",
      "distributionValue": 25.0,
      "isRemainderFl": false
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `AllocationRuleController.deactivate`, `AllocationRuleService.deactivate`, `AllocationRule.deactivate`, `AllocationRuleMapper.toResponse`, `AllocationTargetMapper.toResponse`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-ALLOCATION-RULE` | FIN_404_ALLOCATION_RULE | AllocationRuleService.deactivate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

## POST /api/v1/fin/allocation-rules/{id}/run

**Run an allocation rule**

تشغيل قاعدة توزيع تكلفة

Contract ID: `API-FIN-017`

Operation ID: `run_1`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_ALLOCATION_RULES_UPDATE (found on service:AllocationRuleService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `AllocationRuleService.run`.

Shape: `JournalEntryResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| journalEntryPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| docNo | string | No |  | System-generated document number - رقم المستند | JV-2026-000123 |
| docDate | string (date) | No |  | Document date - تاريخ المستند | 2026-01-31 |
| fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periodId | integer (int64) | No |  | Target fiscal period id - معرّف الفترة المالية | 1 |
| journalTypeCode | string | No |  | Journal type, JOURNAL_TYPE lookup - نوع اليومية | MANUAL |
| statusCode | string | No |  | Status, JOURNAL_STATUS lookup - الحالة | POSTED |
| eventReference | string | No |  | Source event reference, for event-built entries - مرجع الحدث | INV-2026-0001 |
| originalEntryId | integer (int64) | No |  | Original entry id, when this entry is a reversal - معرّف القيد الأصلي | 7 |
| reversalEntryId | integer (int64) | No |  | Reversal entry id, when this entry has been reversed - معرّف قيد العكس | 9 |
| descriptionAr | string | No |  | Entry description (Arabic) - وصف القيد بالعربية | قيد إهلاك يناير |
| descriptionEn | string | No |  | Entry description (English) - وصف القيد بالإنجليزية | January depreciation entry |
| postedAt | string (date-time) | No |  | Posting timestamp - تاريخ الترحيل |  |
| lineCount | integer (int32) | No |  | Number of entry lines - عدد السطور | 2 |
| lines | array<JournalLineResponse> | No |  | Entry lines - سطور القيد — Journal entry line - سطر قيد يومية |  |
| lines[].journalLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].journalEntryId | integer (int64) | No |  | Parent journal entry id - معرّف القيد الأب | 1 |
| lines[].lineNo | integer (int32) | No |  | Line position within the entry - رقم السطر | 1 |
| lines[].accountId | integer (int64) | No |  | Target account id - معرّف الحساب | 12 |
| lines[].amount | number | No |  | Line amount, always positive - المبلغ | 1500.0 |
| lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| lines[].descriptionAr | string | No |  | Line description (Arabic) - وصف السطر بالعربية | إهلاك |
| lines[].descriptionEn | string | No |  | Line description (English) - وصف السطر بالإنجليزية | Depreciation |
| lines[].dimensions | array<JournalLineDimensionResponse> | No |  | Analysis dimension tags - الأبعاد التحليلية — Journal line dimension tag - بُعد تحليلي لسطر القيد |  |
| lines[].dimensions[].journalLineDimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].dimensions[].journalLineId | integer (int64) | No |  | Parent journal line id - معرّف سطر القيد الأب | 1 |
| lines[].dimensions[].dimensionId | integer (int64) | No |  | Dimension id - معرّف البُعد | 1 |
| lines[].dimensions[].dimensionValueId | integer (int64) | No |  | Dimension value id - معرّف قيمة البُعد | 5 |
| lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "journalEntryPk": 1,
  "docNo": "JV-2026-000123",
  "docDate": "2026-01-31",
  "fiscalYearId": 1,
  "periodId": 1,
  "journalTypeCode": "MANUAL",
  "statusCode": "POSTED",
  "eventReference": "INV-2026-0001",
  "originalEntryId": 7,
  "reversalEntryId": 9,
  "descriptionAr": "قيد إهلاك يناير",
  "descriptionEn": "January depreciation entry",
  "lineCount": 2,
  "lines": [
    {
      "journalLinePk": 1,
      "journalEntryId": 1,
      "lineNo": 1,
      "accountId": 12,
      "amount": 1500.0,
      "directionCode": "DEBIT",
      "isRemainderFl": false,
      "descriptionAr": "إهلاك",
      "descriptionEn": "Depreciation",
      "dimensions": [
        {
          "journalLineDimensionPk": 1,
          "journalLineId": 1,
          "dimensionId": 1,
          "dimensionValueId": 5
        }
      ]
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `AllocationRuleController.run`, `AllocationRuleService.run`, `AllocationRuleDomain.from`, `AllocationRuleDomain.assertCanRun`, `AllocationRuleDomain.assertRemainderTargetSetValid`, `AllocationRuleService.balanceOf`, `JournalPostingService.resolvePeriodContaining`, `JournalPostingService.buildValidateAndPost`, `AllocationRuleService.allocationLines`, `JournalEntryMapper.toResponse`, `JournalEntryMapper.toLineResponse`, `new AllocationRuleDomain()`, `AllocationRuleDomain.isRemainderTarget`, `AccountDomain.from`, `DimensionValueDomain.from`, `JournalDocNoGenerator.next`, `JournalPostingService.highestDocNoOf`, `JournalEntryMapper.toEntity`, `JournalEntryDomain.create`, `JournalEntryDomain.checkBalanced`, `JournalEntryDomain.checkTargetPeriodOpenForGeneratedEntry`, `JournalEntry.post`, `JournalEntryDomain.directionOfNet`, `JournalEntryDomain.reversalDirectionOf`, `AllocationRuleDomain.targetAmount`, `AllocationRuleService.toTargetLine`, `EventTypeRuleDomain.remainderAmount`, `JournalEntryMapper.toLineDimensionResponse`, `new AccountDomain()`, `new DimensionValueDomain()`, `JournalDocNoGenerator.format`, `JournalDocNoGenerator.counterOf`, `JournalEntryMapper.toBuiltLineEntity`, `new JournalEntryDomain()`, `JournalEntryDomain.isYearEndGenerated`, `JournalEntryDomain.checkTargetPeriodOpen`, `JournalEntryMapper.BuiltDimension`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-ALLOCATION-RULE` | FIN_404_ALLOCATION_RULE | AllocationRuleService.run |
| 404 NOT_FOUND | `FIN-404-PERIOD` | FIN_404_PERIOD | JournalPostingService.resolvePeriodContaining |
| 404 NOT_FOUND | `FIN-404-YEAR` | FIN_404_YEAR | JournalPostingService.buildValidateAndPost |
| 409 CONFLICT | `FIN-409-DUPLICATE-EVENT` | FIN_409_DUPLICATE_EVENT | JournalEntryDomain.create |
| 409 CONFLICT | `FIN-409-NOT-ACTIVE` | FIN_409_NOT_ACTIVE | AllocationRuleDomain.assertCanRun |
| 409 CONFLICT | `FIN-409-PERIOD-NOT-OPEN` | FIN_409_PERIOD_NOT_OPEN | JournalEntryDomain.checkTargetPeriodOpen — as a `fieldErrors[]` entry |
| 409 CONFLICT | `FIN-409-REMAINDER-COUNT` | FIN_409_REMAINDER_COUNT | AllocationRuleDomain.assertRemainderTargetSetValid |
| 409 CONFLICT | `FIN-409-UNBALANCED` | FIN_409_UNBALANCED | JournalEntryDomain.checkBalanced — as a `fieldErrors[]` entry |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-REMAINDER-MARKER` | FIN_422_REMAINDER_MARKER | AllocationRuleDomain.assertRemainderTargetSetValid |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-REMAINDER-MARKER` | FIN_422_REMAINDER_MARKER | AllocationRuleDomain.targetAmount |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-REMAINDER-NOT-POSITIVE` | FIN_422_REMAINDER_NOT_POSITIVE | EventTypeRuleDomain.remainderAmount |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

<!-- fetched-from: endpoints/fin-dimension-management.md -->
<!-- AUTO-GENERATED by api-doc-generator — do not edit manually -->
# FIN Dimension Management

**Endpoints in this file:**

- `API-FIN-006` — [POST /api/v1/fin/dimensions](#post-apiv1findimensions)
- `API-FIN-005` — [POST /api/v1/fin/dimensions/search](#post-apiv1findimensionssearch)
- `API-FIN-008` — [POST /api/v1/fin/dimensions/values/search](#post-apiv1findimensionsvaluessearch)
- `API-FIN-035` — [PUT /api/v1/fin/dimensions/values/{id}/deactivate](#put-apiv1findimensionsvaluesiddeactivate)
- `API-FIN-007` — [POST /api/v1/fin/dimensions/{id}/values](#post-apiv1findimensionsidvalues)

## POST /api/v1/fin/dimensions

**Create dimension**

إنشاء بُعد تحليلي

Contract ID: `API-FIN-006`

Operation ID: `create_3`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_DIMENSIONS_CREATE (found on service:DimensionService)

### Request Body

Schema: `DimensionCreateRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| code | string | Yes | maxLength: 30 | Unique dimension code - رمز البُعد الفريد | COST_CENTER |
| nameAr | string | Yes | maxLength: 150 | Name (Arabic) - الاسم بالعربية | مركز التكلفة |
| nameEn | string | Yes | maxLength: 150 | Name (English) - الاسم بالإنجليزية | Cost centre |

**Request Example**

```json
{
  "code": "COST_CENTER",
  "nameAr": "مركز التكلفة",
  "nameEn": "Cost centre"
}
```

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `DimensionService.create`.

Shape: `DimensionResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| dimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| code | string | No |  | Unique dimension code - رمز البُعد الفريد | COST_CENTER |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | مركز التكلفة |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Cost centre |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "dimensionPk": 1,
  "code": "COST_CENTER",
  "nameAr": "مركز التكلفة",
  "nameEn": "Cost centre",
  "isActiveFl": true
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `DimensionController.create`, `DimensionService.create`, `DimensionMapper.toEntity`, `DimensionMapper.toResponse`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 409 CONFLICT | `FIN-409-DIMENSION-DUP` | FIN_409_DIMENSION_DUP | DimensionService.create |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## POST /api/v1/fin/dimensions/search

**Search dimensions**

بحث في الأبعاد التحليلية

Contract ID: `API-FIN-005`

Operation ID: `search_5`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_DIMENSIONS_VIEW (found on service:DimensionService)

### Request Body

Schema: `DimensionSearchRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| filters | array<SearchFilter> | No |  | Filter criteria. Supported fields: code (LIKE), nameAr / nameEn (LIKE), isActiveFl (EQUALS), dimensionPk (EQUALS, IN), createdAt (comparison operators). Any other field, or an operator a field does not list, is rejected as 400 VALIDATION_ERROR naming it - معايير التصفية |  |
| filters[].field | string | No |  |  |  |
| filters[].operator | string | No | enum: EQUALS, NOT_EQUALS, LIKE, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, IN |  |  |
| filters[].value | object | No |  |  |  |
| sortField | string | No |  | Sort field. Supported: dimensionPk, code, nameAr, nameEn, isActiveFl, createdAt. Any other value is rejected as 400 FIN-400-INVALID-SORT - حقل الترتيب |  |
| sortDirection | string | No | enum: ASC, DESC | Sort direction - اتجاه الترتيب |  |
| page | integer (int32) | No |  | Page number, zero-based - رقم الصفحة | 0 |
| size | integer (int32) | No |  | Page size - حجم الصفحة | 20 |

**Request Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "page": 0,
  "size": 20
}
```

### Response `200` — OK

Shape: `paginated list of DimensionResponse (see Pagination Envelope in index.md)`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| dimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| code | string | No |  | Unique dimension code - رمز البُعد الفريد | COST_CENTER |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | مركز التكلفة |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Cost centre |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "dimensionPk": 1,
  "code": "COST_CENTER",
  "nameAr": "مركز التكلفة",
  "nameEn": "Cost centre",
  "isActiveFl": true
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `DimensionController.search`, `DimensionService.search`, `FinSearchSupport.assertSortAllowed`, `DimensionSearchRequest.getSortField`, `FinSearchSupport.temporalFieldConverter`, `DimensionMapper.toResponse`, `FinSearchSupport.temporalFieldConverter`, `FinSearchSupport.parseLocalDate`, `FinSearchSupport.parseInstant`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-SORT` | FIN_400_INVALID_SORT | FinSearchSupport.assertSortAllowed |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseInstant |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseLocalDate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## POST /api/v1/fin/dimensions/values/search

**Search dimension values**

بحث في قيم البُعد التحليلي — معرّف البُعد يُرسَل ضمن مرشِّحات الطلب

Contract ID: `API-FIN-008`

Operation ID: `searchDimensionValues`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_DIMENSIONS_VIEW (found on service:DimensionValueService)

### Request Body

Schema: `DimensionValueSearchRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| filters | array<SearchFilter> | No |  | Filter criteria. Supported fields: dimensionId (EQUALS - the parent scope), code (LIKE), nameAr / nameEn (LIKE), sortOrder (EQUALS, comparison operators), isActiveFl (EQUALS), dimensionValuePk (EQUALS, IN), createdAt (comparison operators). Any other field, or an operator a field does not list, is rejected as 400 VALIDATION_ERROR naming it - معايير التصفية |  |
| filters[].field | string | No |  |  |  |
| filters[].operator | string | No | enum: EQUALS, NOT_EQUALS, LIKE, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, IN |  |  |
| filters[].value | object | No |  |  |  |
| sortField | string | No |  | Sort field. Supported: dimensionValuePk, code, nameAr, nameEn, sortOrder, isActiveFl, createdAt. Any other value is rejected as 400 FIN-400-INVALID-SORT - حقل الترتيب |  |
| sortDirection | string | No | enum: ASC, DESC | Sort direction - اتجاه الترتيب |  |
| page | integer (int32) | No |  | Page number, zero-based - رقم الصفحة | 0 |
| size | integer (int32) | No |  | Page size - حجم الصفحة | 20 |

**Request Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "page": 0,
  "size": 20
}
```

### Response `200` — OK

Shape: `paginated list of DimensionValueResponse (see Pagination Envelope in index.md)`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| dimensionValuePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| dimensionId | integer (int64) | No |  | Parent dimension id - معرّف البُعد الأب | 1 |
| code | string | No |  | Unique code within the parent dimension - الرمز الفريد ضمن البُعد | CC-100 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | الإدارة المالية |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Finance department |
| sortOrder | integer (int32) | No |  | Display sort order - ترتيب العرض | 10 |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "dimensionValuePk": 1,
  "dimensionId": 1,
  "code": "CC-100",
  "nameAr": "الإدارة المالية",
  "nameEn": "Finance department",
  "sortOrder": 10,
  "isActiveFl": true
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `DimensionController.searchDimensionValues`, `DimensionValueService.search`, `DimensionValueSearchRequest.getDimensionId`, `FinSearchSupport.assertSortAllowed`, `DimensionValueSearchRequest.getSortField`, `DimensionValueSearchRequest.toCommonSearchRequest`, `FinSearchSupport.temporalFieldConverter`, `DimensionValueMapper.toResponse`, `FinSearchSupport.temporalFieldConverter`, `FinSearchSupport.parseLocalDate`, `FinSearchSupport.parseInstant`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-SORT` | FIN_400_INVALID_SORT | FinSearchSupport.assertSortAllowed |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseInstant |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseLocalDate |
| 404 NOT_FOUND | `FIN-404-DIMENSION` | FIN_404_DIMENSION | DimensionValueService.search |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## PUT /api/v1/fin/dimensions/values/{id}/deactivate

**Deactivate dimension value**

إلغاء تفعيل قيمة ضمن بُعد تحليلي

Contract ID: `API-FIN-035`

Operation ID: `deactivateDimensionValue`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_DIMENSIONS_UPDATE (found on service:DimensionValueService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `200` — OK

Shape: `DimensionValueResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| dimensionValuePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| dimensionId | integer (int64) | No |  | Parent dimension id - معرّف البُعد الأب | 1 |
| code | string | No |  | Unique code within the parent dimension - الرمز الفريد ضمن البُعد | CC-100 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | الإدارة المالية |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Finance department |
| sortOrder | integer (int32) | No |  | Display sort order - ترتيب العرض | 10 |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "dimensionValuePk": 1,
  "dimensionId": 1,
  "code": "CC-100",
  "nameAr": "الإدارة المالية",
  "nameEn": "Finance department",
  "sortOrder": 10,
  "isActiveFl": true
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `DimensionController.deactivateDimensionValue`, `DimensionValueService.deactivate`, `DimensionValue.deactivate`, `DimensionValueMapper.toResponse`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-DIMVALUE` | FIN_404_DIMVALUE | DimensionValueService.deactivate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

## POST /api/v1/fin/dimensions/{id}/values

**Create dimension value**

إنشاء قيمة ضمن بُعد تحليلي

Contract ID: `API-FIN-007`

Operation ID: `createDimensionValue`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_DIMENSIONS_CREATE (found on service:DimensionValueService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Request Body

Schema: `DimensionValueCreateRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| code | string | Yes | maxLength: 30 | Unique code within the parent dimension - الرمز الفريد ضمن البُعد | CC-100 |
| nameAr | string | Yes | maxLength: 150 | Name (Arabic) - الاسم بالعربية | الإدارة المالية |
| nameEn | string | Yes | maxLength: 150 | Name (English) - الاسم بالإنجليزية | Finance department |
| sortOrder | integer (int32) | Yes |  | Display sort order - ترتيب العرض | 10 |

**Request Example**

```json
{
  "code": "CC-100",
  "nameAr": "الإدارة المالية",
  "nameEn": "Finance department",
  "sortOrder": 10
}
```

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `DimensionValueService.create`.

Shape: `DimensionValueResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| dimensionValuePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| dimensionId | integer (int64) | No |  | Parent dimension id - معرّف البُعد الأب | 1 |
| code | string | No |  | Unique code within the parent dimension - الرمز الفريد ضمن البُعد | CC-100 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | الإدارة المالية |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Finance department |
| sortOrder | integer (int32) | No |  | Display sort order - ترتيب العرض | 10 |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "dimensionValuePk": 1,
  "dimensionId": 1,
  "code": "CC-100",
  "nameAr": "الإدارة المالية",
  "nameEn": "Finance department",
  "sortOrder": 10,
  "isActiveFl": true
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `DimensionController.createDimensionValue`, `DimensionValueService.create`, `DimensionValueDomain.create`, `DimensionValueMapper.toEntity`, `DimensionValueMapper.toResponse`, `new DimensionValueDomain()`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-DIMENSION` | FIN_404_DIMENSION | DimensionValueService.create |
| 409 CONFLICT | `FIN-409-DIMVALUE-DUP` | FIN_409_DIMVALUE_DUP | DimensionValueDomain.create |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

<!-- fetched-from: endpoints/fin-event-rule-management.md -->
<!-- AUTO-GENERATED by api-doc-generator — do not edit manually -->
# FIN Event Rule Management

**Endpoints in this file:**

- `API-FIN-010` — [POST /api/v1/fin/event-rules](#post-apiv1finevent-rules)
- `API-FIN-009` — [POST /api/v1/fin/event-rules/search](#post-apiv1finevent-rulessearch)
- `API-FIN-034` — [PUT /api/v1/fin/event-rules/{id}/deactivate](#put-apiv1finevent-rulesiddeactivate)
- `API-FIN-011` — [POST /api/v1/fin/event-rules/{id}/lines](#post-apiv1finevent-rulesidlines)

## POST /api/v1/fin/event-rules

**Create event-type rule**

إنشاء قاعدة نوع حدث محاسبي

Contract ID: `API-FIN-010`

Operation ID: `create_2`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_RULES_CREATE (found on service:EventTypeRuleService)

### Request Body

Schema: `EventTypeRuleCreateRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| eventTypeCode | string | Yes | maxLength: 50 | Event type code, ACCOUNTING_EVENT_TYPE lookup - رمز نوع الحدث | SALES_INVOICE |
| nameAr | string | Yes | maxLength: 150 | Name (Arabic) - الاسم بالعربية | فاتورة مبيعات |
| nameEn | string | Yes | maxLength: 150 | Name (English) - الاسم بالإنجليزية | Sales invoice |

**Request Example**

```json
{
  "eventTypeCode": "SALES_INVOICE",
  "nameAr": "فاتورة مبيعات",
  "nameEn": "Sales invoice"
}
```

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `EventTypeRuleService.create`.

Shape: `EventTypeRuleResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| eventTypeRulePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| eventTypeCode | string | No |  | Event type code, ACCOUNTING_EVENT_TYPE lookup - رمز نوع الحدث | SALES_INVOICE |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | فاتورة مبيعات |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Sales invoice |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| lineCount | integer (int32) | No |  | Number of rule lines - عدد سطور القاعدة | 2 |
| lines | array<RuleLineResponse> | No |  | Rule lines, ordered by lineNo - سطور القاعدة مرتبة حسب رقم السطر — Event-type rule line - سطر قاعدة نوع الحدث |  |
| lines[].ruleLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].eventTypeRuleId | integer (int64) | No |  | Parent event-type rule id - معرّف القاعدة الأب | 1 |
| lines[].lineNo | integer (int32) | No |  | Line position within the rule - رقم السطر | 1 |
| lines[].accountDerivationTypeCode | string | No |  | Account derivation type, ACCOUNT_DERIVATION_TYPE lookup - نوع اشتقاق الحساب | CONSTANT |
| lines[].accountDerivationValue | string | No |  | Constant account code, CONSTANT only - رمز الحساب الثابت | 1101 |
| lines[].accountBusinessFieldCode | string | No |  | Business field selecting the account, FIN_EVENT_BUSINESS_FIELD lookup, MAPPING only - الحقل التجاري لربط الحساب | PAYMENT_METHOD |
| lines[].amountSourceTypeCode | string | No |  | Amount source type, AMOUNT_SOURCE_TYPE lookup - نوع مصدر المبلغ | FIELD |
| lines[].amountSourceValue | string | No |  | Amount source value - قيمة مصدر المبلغ | netAmount |
| lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].distributionTypeCode | string | No |  | Distribution type, DISTRIBUTION_TYPE lookup - نوع التوزيع | FIXED |
| lines[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| lines[].dimensionTags | array<RuleLineDimensionTagResponse> | No |  | The line's dimension tags - وسوم الأبعاد على السطر — Rule line dimension tag - وسم بُعد على سطر القاعدة |  |
| lines[].dimensionTags[].ruleLineDimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].dimensionTags[].ruleLineId | integer (int64) | No |  | Owning rule line id - معرّف سطر القاعدة | 1 |
| lines[].dimensionTags[].dimensionId | integer (int64) | No |  | Tagged dimension id - معرّف البُعد الموسوم | 1 |
| lines[].dimensionTags[].valueSourceCode | string | No |  | Value source, FIN_DIMENSION_VALUE_SOURCE lookup - مصدر القيمة | CONSTANT |
| lines[].dimensionTags[].dimensionValueId | integer (int64) | No |  | Constant dimension value id, CONSTANT only - معرّف القيمة الثابتة | 7 |
| lines[].dimensionTags[].businessFieldCode | string | No |  | Business field, FIN_EVENT_BUSINESS_FIELD lookup, BUSINESS_FIELD only - الحقل التجاري | BRANCH_CODE |
| lines[].dimensionTags[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "eventTypeRulePk": 1,
  "eventTypeCode": "SALES_INVOICE",
  "nameAr": "فاتورة مبيعات",
  "nameEn": "Sales invoice",
  "isActiveFl": true,
  "lineCount": 2,
  "lines": [
    {
      "ruleLinePk": 1,
      "eventTypeRuleId": 1,
      "lineNo": 1,
      "accountDerivationTypeCode": "CONSTANT",
      "accountDerivationValue": 1101,
      "accountBusinessFieldCode": "PAYMENT_METHOD",
      "amountSourceTypeCode": "FIELD",
      "amountSourceValue": "netAmount",
      "directionCode": "DEBIT",
      "distributionTypeCode": "FIXED",
      "isRemainderFl": false,
      "dimensionTags": [
        {
          "ruleLineDimensionPk": 1,
          "ruleLineId": 1,
          "dimensionId": 1,
          "valueSourceCode": "CONSTANT",
          "dimensionValueId": 7,
          "businessFieldCode": "BRANCH_CODE"
        }
      ]
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `EventTypeRuleController.create`, `EventTypeRuleService.create`, `FinLookupValidationService.assertValidCode`, `EventTypeRuleDomain.create`, `EventTypeRuleMapper.toEntity`, `EventTypeRuleMapper.toResponse`, `new EventTypeRuleDomain()`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-LOOKUP` | FIN_400_INVALID_LOOKUP | FinLookupValidationService.assertValidCode |
| 409 CONFLICT | `FIN-409-RULE-DUP` | FIN_409_RULE_DUP | EventTypeRuleDomain.create |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## POST /api/v1/fin/event-rules/search

**Search event-type rules**

بحث في قواعد أنواع الأحداث

Contract ID: `API-FIN-009`

Operation ID: `search_4`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_RULES_VIEW (found on service:EventTypeRuleService)

### Request Body

Schema: `EventTypeRuleSearchRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| filters | array<SearchFilter> | No |  | Filter criteria. Supported fields: eventTypeCode (EQUALS, IN - ACCOUNTING_EVENT_TYPE), nameAr / nameEn (LIKE), isActiveFl (EQUALS), eventTypeRulePk (EQUALS, IN), createdAt (comparison operators). Any other field, or an operator a field does not list, is rejected as 400 VALIDATION_ERROR naming it - معايير التصفية |  |
| filters[].field | string | No |  |  |  |
| filters[].operator | string | No | enum: EQUALS, NOT_EQUALS, LIKE, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, IN |  |  |
| filters[].value | object | No |  |  |  |
| sortField | string | No |  | Sort field. Supported: eventTypeRulePk, eventTypeCode, nameAr, nameEn, isActiveFl, createdAt. Any other value is rejected as 400 FIN-400-INVALID-SORT - حقل الترتيب |  |
| sortDirection | string | No | enum: ASC, DESC | Sort direction - اتجاه الترتيب |  |
| page | integer (int32) | No |  | Page number, zero-based - رقم الصفحة | 0 |
| size | integer (int32) | No |  | Page size - حجم الصفحة | 20 |

**Request Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "page": 0,
  "size": 20
}
```

### Response `200` — OK

Shape: `paginated list of EventTypeRuleResponse (see Pagination Envelope in index.md)`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| eventTypeRulePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| eventTypeCode | string | No |  | Event type code, ACCOUNTING_EVENT_TYPE lookup - رمز نوع الحدث | SALES_INVOICE |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | فاتورة مبيعات |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Sales invoice |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| lineCount | integer (int32) | No |  | Number of rule lines - عدد سطور القاعدة | 2 |
| lines | array<RuleLineResponse> | No |  | Rule lines, ordered by lineNo - سطور القاعدة مرتبة حسب رقم السطر — Event-type rule line - سطر قاعدة نوع الحدث |  |
| lines[].ruleLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].eventTypeRuleId | integer (int64) | No |  | Parent event-type rule id - معرّف القاعدة الأب | 1 |
| lines[].lineNo | integer (int32) | No |  | Line position within the rule - رقم السطر | 1 |
| lines[].accountDerivationTypeCode | string | No |  | Account derivation type, ACCOUNT_DERIVATION_TYPE lookup - نوع اشتقاق الحساب | CONSTANT |
| lines[].accountDerivationValue | string | No |  | Constant account code, CONSTANT only - رمز الحساب الثابت | 1101 |
| lines[].accountBusinessFieldCode | string | No |  | Business field selecting the account, FIN_EVENT_BUSINESS_FIELD lookup, MAPPING only - الحقل التجاري لربط الحساب | PAYMENT_METHOD |
| lines[].amountSourceTypeCode | string | No |  | Amount source type, AMOUNT_SOURCE_TYPE lookup - نوع مصدر المبلغ | FIELD |
| lines[].amountSourceValue | string | No |  | Amount source value - قيمة مصدر المبلغ | netAmount |
| lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].distributionTypeCode | string | No |  | Distribution type, DISTRIBUTION_TYPE lookup - نوع التوزيع | FIXED |
| lines[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| lines[].dimensionTags | array<RuleLineDimensionTagResponse> | No |  | The line's dimension tags - وسوم الأبعاد على السطر — Rule line dimension tag - وسم بُعد على سطر القاعدة |  |
| lines[].dimensionTags[].ruleLineDimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].dimensionTags[].ruleLineId | integer (int64) | No |  | Owning rule line id - معرّف سطر القاعدة | 1 |
| lines[].dimensionTags[].dimensionId | integer (int64) | No |  | Tagged dimension id - معرّف البُعد الموسوم | 1 |
| lines[].dimensionTags[].valueSourceCode | string | No |  | Value source, FIN_DIMENSION_VALUE_SOURCE lookup - مصدر القيمة | CONSTANT |
| lines[].dimensionTags[].dimensionValueId | integer (int64) | No |  | Constant dimension value id, CONSTANT only - معرّف القيمة الثابتة | 7 |
| lines[].dimensionTags[].businessFieldCode | string | No |  | Business field, FIN_EVENT_BUSINESS_FIELD lookup, BUSINESS_FIELD only - الحقل التجاري | BRANCH_CODE |
| lines[].dimensionTags[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "eventTypeRulePk": 1,
  "eventTypeCode": "SALES_INVOICE",
  "nameAr": "فاتورة مبيعات",
  "nameEn": "Sales invoice",
  "isActiveFl": true,
  "lineCount": 2,
  "lines": [
    {
      "ruleLinePk": 1,
      "eventTypeRuleId": 1,
      "lineNo": 1,
      "accountDerivationTypeCode": "CONSTANT",
      "accountDerivationValue": 1101,
      "accountBusinessFieldCode": "PAYMENT_METHOD",
      "amountSourceTypeCode": "FIELD",
      "amountSourceValue": "netAmount",
      "directionCode": "DEBIT",
      "distributionTypeCode": "FIXED",
      "isRemainderFl": false,
      "dimensionTags": [
        {
          "ruleLineDimensionPk": 1,
          "ruleLineId": 1,
          "dimensionId": 1,
          "valueSourceCode": "CONSTANT",
          "dimensionValueId": 7,
          "businessFieldCode": "BRANCH_CODE"
        }
      ]
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `EventTypeRuleController.search`, `EventTypeRuleService.search`, `FinSearchSupport.assertSortAllowed`, `EventTypeRuleSearchRequest.getSortField`, `FinSearchSupport.temporalFieldConverter`, `EventTypeRuleService.tagsByLine`, `RuleLineMapper.toResponse`, `EventTypeRuleMapper.toResponse`, `FinSearchSupport.temporalFieldConverter`, `RuleLineMapper.toTagResponse`, `FinSearchSupport.parseLocalDate`, `FinSearchSupport.parseInstant`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-SORT` | FIN_400_INVALID_SORT | FinSearchSupport.assertSortAllowed |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseInstant |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseLocalDate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## PUT /api/v1/fin/event-rules/{id}/deactivate

**Deactivate event-type rule**

إلغاء تفعيل قاعدة نوع حدث محاسبي

Contract ID: `API-FIN-034`

Operation ID: `deactivate_1`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_RULES_UPDATE (found on service:EventTypeRuleService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `200` — OK

Shape: `EventTypeRuleResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| eventTypeRulePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| eventTypeCode | string | No |  | Event type code, ACCOUNTING_EVENT_TYPE lookup - رمز نوع الحدث | SALES_INVOICE |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | فاتورة مبيعات |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Sales invoice |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| lineCount | integer (int32) | No |  | Number of rule lines - عدد سطور القاعدة | 2 |
| lines | array<RuleLineResponse> | No |  | Rule lines, ordered by lineNo - سطور القاعدة مرتبة حسب رقم السطر — Event-type rule line - سطر قاعدة نوع الحدث |  |
| lines[].ruleLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].eventTypeRuleId | integer (int64) | No |  | Parent event-type rule id - معرّف القاعدة الأب | 1 |
| lines[].lineNo | integer (int32) | No |  | Line position within the rule - رقم السطر | 1 |
| lines[].accountDerivationTypeCode | string | No |  | Account derivation type, ACCOUNT_DERIVATION_TYPE lookup - نوع اشتقاق الحساب | CONSTANT |
| lines[].accountDerivationValue | string | No |  | Constant account code, CONSTANT only - رمز الحساب الثابت | 1101 |
| lines[].accountBusinessFieldCode | string | No |  | Business field selecting the account, FIN_EVENT_BUSINESS_FIELD lookup, MAPPING only - الحقل التجاري لربط الحساب | PAYMENT_METHOD |
| lines[].amountSourceTypeCode | string | No |  | Amount source type, AMOUNT_SOURCE_TYPE lookup - نوع مصدر المبلغ | FIELD |
| lines[].amountSourceValue | string | No |  | Amount source value - قيمة مصدر المبلغ | netAmount |
| lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].distributionTypeCode | string | No |  | Distribution type, DISTRIBUTION_TYPE lookup - نوع التوزيع | FIXED |
| lines[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| lines[].dimensionTags | array<RuleLineDimensionTagResponse> | No |  | The line's dimension tags - وسوم الأبعاد على السطر — Rule line dimension tag - وسم بُعد على سطر القاعدة |  |
| lines[].dimensionTags[].ruleLineDimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].dimensionTags[].ruleLineId | integer (int64) | No |  | Owning rule line id - معرّف سطر القاعدة | 1 |
| lines[].dimensionTags[].dimensionId | integer (int64) | No |  | Tagged dimension id - معرّف البُعد الموسوم | 1 |
| lines[].dimensionTags[].valueSourceCode | string | No |  | Value source, FIN_DIMENSION_VALUE_SOURCE lookup - مصدر القيمة | CONSTANT |
| lines[].dimensionTags[].dimensionValueId | integer (int64) | No |  | Constant dimension value id, CONSTANT only - معرّف القيمة الثابتة | 7 |
| lines[].dimensionTags[].businessFieldCode | string | No |  | Business field, FIN_EVENT_BUSINESS_FIELD lookup, BUSINESS_FIELD only - الحقل التجاري | BRANCH_CODE |
| lines[].dimensionTags[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "eventTypeRulePk": 1,
  "eventTypeCode": "SALES_INVOICE",
  "nameAr": "فاتورة مبيعات",
  "nameEn": "Sales invoice",
  "isActiveFl": true,
  "lineCount": 2,
  "lines": [
    {
      "ruleLinePk": 1,
      "eventTypeRuleId": 1,
      "lineNo": 1,
      "accountDerivationTypeCode": "CONSTANT",
      "accountDerivationValue": 1101,
      "accountBusinessFieldCode": "PAYMENT_METHOD",
      "amountSourceTypeCode": "FIELD",
      "amountSourceValue": "netAmount",
      "directionCode": "DEBIT",
      "distributionTypeCode": "FIXED",
      "isRemainderFl": false,
      "dimensionTags": [
        {
          "ruleLineDimensionPk": 1,
          "ruleLineId": 1,
          "dimensionId": 1,
          "valueSourceCode": "CONSTANT",
          "dimensionValueId": 7,
          "businessFieldCode": "BRANCH_CODE"
        }
      ]
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `EventTypeRuleController.deactivate`, `EventTypeRuleService.deactivate`, `EventTypeRule.deactivate`, `EventTypeRuleMapper.toResponse`, `EventTypeRuleService.linesOf`, `EventTypeRuleService.tagsByLine`, `RuleLineMapper.toResponse`, `RuleLineMapper.toTagResponse`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-RULE` | FIN_404_RULE | EventTypeRuleService.deactivate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

## POST /api/v1/fin/event-rules/{id}/lines

**Add rule line**

إضافة سطر إلى قاعدة نوع الحدث

Contract ID: `API-FIN-011`

Operation ID: `createRuleLine`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_RULES_UPDATE (found on service:RuleLineService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Request Body

Schema: `RuleLineCreateRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| accountDerivationTypeCode | string | Yes | maxLength: 20 | Account derivation type, ACCOUNT_DERIVATION_TYPE lookup - نوع اشتقاق الحساب | CONSTANT |
| accountDerivationValue | string | No |  | Constant account code, CONSTANT only - رمز الحساب الثابت | 1101 |
| accountBusinessFieldCode | string | No | maxLength: 50 | Business field selecting the account, FIN_EVENT_BUSINESS_FIELD lookup, MAPPING only - الحقل التجاري لربط الحساب | PAYMENT_METHOD |
| amountSourceTypeCode | string | Yes | maxLength: 20 | Amount source type, AMOUNT_SOURCE_TYPE lookup - نوع مصدر المبلغ | FIELD |
| amountSourceValue | string | No |  | Amount source value - قيمة مصدر المبلغ | netAmount |
| directionCode | string | Yes | maxLength: 10 | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| distributionTypeCode | string | Yes | maxLength: 15 | Distribution type, DISTRIBUTION_TYPE lookup - نوع التوزيع | FIXED |
| isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| dimensionTags | array<RuleLineDimensionTagRequest> | No |  | Dimension tags submitted with the line - وسوم الأبعاد على السطر — Dimension tag submitted with a rule line - وسم بُعد مُرسَل مع سطر القاعدة |  |
| dimensionTags[].dimensionId | integer (int64) | Yes |  | Tagged dimension id - معرّف البُعد الموسوم | 1 |
| dimensionTags[].valueSourceCode | string | Yes | maxLength: 50 | Value source, FIN_DIMENSION_VALUE_SOURCE lookup - مصدر القيمة | CONSTANT |
| dimensionTags[].dimensionValueId | integer (int64) | No |  | Constant dimension value id, CONSTANT only - معرّف القيمة الثابتة | 7 |
| dimensionTags[].businessFieldCode | string | No | maxLength: 50 | Business field, FIN_EVENT_BUSINESS_FIELD lookup, BUSINESS_FIELD only - الحقل التجاري | BRANCH_CODE |

**Request Example**

```json
{
  "accountDerivationTypeCode": "CONSTANT",
  "accountDerivationValue": 1101,
  "accountBusinessFieldCode": "PAYMENT_METHOD",
  "amountSourceTypeCode": "FIELD",
  "amountSourceValue": "netAmount",
  "directionCode": "DEBIT",
  "distributionTypeCode": "FIXED",
  "isRemainderFl": false,
  "dimensionTags": [
    {
      "dimensionId": 1,
      "valueSourceCode": "CONSTANT",
      "dimensionValueId": 7,
      "businessFieldCode": "BRANCH_CODE"
    }
  ]
}
```

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `RuleLineService.create`.

Shape: `RuleLineResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| ruleLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| eventTypeRuleId | integer (int64) | No |  | Parent event-type rule id - معرّف القاعدة الأب | 1 |
| lineNo | integer (int32) | No |  | Line position within the rule - رقم السطر | 1 |
| accountDerivationTypeCode | string | No |  | Account derivation type, ACCOUNT_DERIVATION_TYPE lookup - نوع اشتقاق الحساب | CONSTANT |
| accountDerivationValue | string | No |  | Constant account code, CONSTANT only - رمز الحساب الثابت | 1101 |
| accountBusinessFieldCode | string | No |  | Business field selecting the account, FIN_EVENT_BUSINESS_FIELD lookup, MAPPING only - الحقل التجاري لربط الحساب | PAYMENT_METHOD |
| amountSourceTypeCode | string | No |  | Amount source type, AMOUNT_SOURCE_TYPE lookup - نوع مصدر المبلغ | FIELD |
| amountSourceValue | string | No |  | Amount source value - قيمة مصدر المبلغ | netAmount |
| directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| distributionTypeCode | string | No |  | Distribution type, DISTRIBUTION_TYPE lookup - نوع التوزيع | FIXED |
| isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| dimensionTags | array<RuleLineDimensionTagResponse> | No |  | The line's dimension tags - وسوم الأبعاد على السطر — Rule line dimension tag - وسم بُعد على سطر القاعدة |  |
| dimensionTags[].ruleLineDimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| dimensionTags[].ruleLineId | integer (int64) | No |  | Owning rule line id - معرّف سطر القاعدة | 1 |
| dimensionTags[].dimensionId | integer (int64) | No |  | Tagged dimension id - معرّف البُعد الموسوم | 1 |
| dimensionTags[].valueSourceCode | string | No |  | Value source, FIN_DIMENSION_VALUE_SOURCE lookup - مصدر القيمة | CONSTANT |
| dimensionTags[].dimensionValueId | integer (int64) | No |  | Constant dimension value id, CONSTANT only - معرّف القيمة الثابتة | 7 |
| dimensionTags[].businessFieldCode | string | No |  | Business field, FIN_EVENT_BUSINESS_FIELD lookup, BUSINESS_FIELD only - الحقل التجاري | BRANCH_CODE |
| dimensionTags[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "ruleLinePk": 1,
  "eventTypeRuleId": 1,
  "lineNo": 1,
  "accountDerivationTypeCode": "CONSTANT",
  "accountDerivationValue": 1101,
  "accountBusinessFieldCode": "PAYMENT_METHOD",
  "amountSourceTypeCode": "FIELD",
  "amountSourceValue": "netAmount",
  "directionCode": "DEBIT",
  "distributionTypeCode": "FIXED",
  "isRemainderFl": false,
  "dimensionTags": [
    {
      "ruleLineDimensionPk": 1,
      "ruleLineId": 1,
      "dimensionId": 1,
      "valueSourceCode": "CONSTANT",
      "dimensionValueId": 7,
      "businessFieldCode": "BRANCH_CODE"
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `EventTypeRuleController.createRuleLine`, `RuleLineService.create`, `FinLookupValidationService.assertValidCode`, `FinLookupValidationService.assertValidCodeIfPresent`, `EventTypeRuleDomain.assertDerivationSupported`, `FinLookupValidationService.isActiveCode`, `EventTypeRuleDomain.assertDerivationSpec`, `RuleLineMapper.toEntity`, `EventTypeRuleDomain.from`, `RuleLineService.ResolvedTag`, `RuleLineService.resolveDimension`, `RuleLineService.resolveDimensionValue`, `EventTypeRuleDomain.assertTagsDistinct`, `EventTypeRuleDomain.assertTagMatchesSource`, `RuleLineMapper.toTagEntity`, `RuleLineMapper.toResponse`, `EventTypeRuleDomain.assertRemainderLineSetValid`, `EventTypeRuleDomain.hasText`, `new EventTypeRuleDomain()`, `RuleLineMapper.toTagResponse`, `EventTypeRuleDomain.isRemainderLine`, `EventTypeRuleDomain.declaresRemainder`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-LOOKUP` | FIN_400_INVALID_LOOKUP | FinLookupValidationService.assertValidCode |
| 404 NOT_FOUND | `FIN-404-DIMENSION` | FIN_404_DIMENSION | RuleLineService.resolveDimension |
| 404 NOT_FOUND | `FIN-404-DIMVALUE` | FIN_404_DIMVALUE | RuleLineService.resolveDimensionValue |
| 404 NOT_FOUND | `FIN-404-RULE` | FIN_404_RULE | RuleLineService.create |
| 409 CONFLICT | `FIN-409-REMAINDER-COUNT` | FIN_409_REMAINDER_COUNT | EventTypeRuleDomain.assertRemainderLineSetValid |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-DERIVATION-SPEC` | FIN_422_DERIVATION_SPEC | EventTypeRuleDomain.assertDerivationSpec |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-DIMENSION-TAG-DUP` | FIN_422_DIMENSION_TAG_DUP | EventTypeRuleDomain.assertTagsDistinct |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-DIMENSION-TAG-SOURCE` | FIN_422_DIMENSION_TAG_SOURCE | EventTypeRuleDomain.assertTagMatchesSource |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-REMAINDER-MARKER` | FIN_422_REMAINDER_MARKER | EventTypeRuleDomain.assertRemainderLineSetValid |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-UNSUPPORTED-DERIVATION` | FIN_422_UNSUPPORTED_DERIVATION | EventTypeRuleDomain.assertDerivationSpec |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-UNSUPPORTED-DERIVATION` | FIN_422_UNSUPPORTED_DERIVATION | EventTypeRuleDomain.assertDerivationSupported |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

<!-- fetched-from: endpoints/fin-financial-reports.md -->
<!-- AUTO-GENERATED by api-doc-generator — do not edit manually -->
# FIN Financial Reports

**Endpoints in this file:**

- `API-FIN-028` — [GET /api/v1/fin/reports/account-ledger](#get-apiv1finreportsaccount-ledger)
- `API-FIN-030` — [GET /api/v1/fin/reports/balance-sheet](#get-apiv1finreportsbalance-sheet)
- `API-FIN-032` — [GET /api/v1/fin/reports/dimension](#get-apiv1finreportsdimension)
- `API-FIN-031` — [GET /api/v1/fin/reports/income-statement](#get-apiv1finreportsincome-statement)
- `API-FIN-029` — [GET /api/v1/fin/reports/trial-balance](#get-apiv1finreportstrial-balance)

## GET /api/v1/fin/reports/account-ledger

**Account ledger**

دفتر الحساب المُشتق حيًا من القيود المُرحَّلة

Contract ID: `API-FIN-028`

Operation ID: `accountLedger`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_ACCOUNT_LEDGER_VIEW (found on service:ReportService)

### Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| accountId | integer | Yes |  |
| fromDate | string | No |  |
| toDate | string | No |  |
| dimensionId | integer | No |  |
| dimensionValueId | integer | No |  |

### Response `200` — OK

Shape: `AccountLedgerResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| accountId | integer (int64) | No |  | Account id - معرّف الحساب | 5 |
| accountCode | string | No |  | Account code - رمز الحساب | 1101 |
| accountNameAr | string | No |  | Account name (Arabic) - اسم الحساب بالعربية | النقدية بالصندوق |
| accountNameEn | string | No |  | Account name (English) - اسم الحساب بالإنجليزية | Cash on hand |
| accountTypeCode | string | No |  | Account type, ACCOUNT_TYPE lookup - نوع الحساب | ASSET |
| natureCode | string | No |  | Normal balance side, DEBIT_CREDIT lookup - طبيعة الحساب | DEBIT |
| fromDate | string (date) | No |  | Range start applied to docDate, null when unbounded - بداية المدى | 2026-01-01 |
| toDate | string (date) | No |  | Range end applied to docDate, null when unbounded - نهاية المدى | 2026-01-31 |
| dimensionId | integer (int64) | No |  | Dimension filter applied, null when none - البُعد المُرشَّح | 2 |
| dimensionValueId | integer (int64) | No |  | Dimension-value filter applied, null when none - قيمة البُعد المُرشَّحة | 7 |
| debitTotal | number | No |  | Total of DEBIT lines in range - إجمالي المدين | 5000.0 |
| creditTotal | number | No |  | Total of CREDIT lines in range - إجمالي الدائن | 1800.0 |
| closingBalance | number | No |  | Closing balance, signed per POL-FIN-002 - الرصيد الختامي | 3200.0 |
| rows | array<AccountLedgerRowResponse> | No |  | Ledger rows, oldest first - سطور الدفتر — Account ledger row - سطر في دفتر الحساب |  |
| rows[].journalEntryId | integer (int64) | No |  | Originating journal entry id, for drill-down (REQ-FIN-046) - معرّف القيد المصدر | 12 |
| rows[].docNo | string | No |  | Journal entry document number - رقم مستند القيد | JV-2026-000123 |
| rows[].docDate | string (date) | No |  | Journal entry document date - تاريخ مستند القيد | 2026-01-31 |
| rows[].journalTypeCode | string | No |  | Journal type, JOURNAL_TYPE lookup - نوع اليومية | MANUAL |
| rows[].eventReference | string | No |  | Source event reference, when the entry was event-built - مرجع الحدث | INV-2026-0001 |
| rows[].journalLineId | integer (int64) | No |  | Journal line id - معرّف سطر القيد | 44 |
| rows[].lineNo | integer (int32) | No |  | Line number inside the entry - رقم السطر داخل القيد | 1 |
| rows[].amount | number | No |  | Line amount, always positive (POL-FIN-005) - المبلغ | 1500.0 |
| rows[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| rows[].signedAmount | number | No |  | Amount signed against the account's nature (POL-FIN-002) - المبلغ بإشارة طبيعة الحساب | 1500.0 |
| rows[].runningBalance | number | No |  | Running balance after this line, signed per POL-FIN-002 - الرصيد الجاري بعد هذا السطر | 3200.0 |
| rows[].descriptionAr | string | No |  | Line description (Arabic) - وصف السطر بالعربية | إيجار يناير |
| rows[].descriptionEn | string | No |  | Line description (English) - وصف السطر بالإنجليزية | January rent |

**Response Example**

```json
{
  "accountId": 5,
  "accountCode": 1101,
  "accountNameAr": "النقدية بالصندوق",
  "accountNameEn": "Cash on hand",
  "accountTypeCode": "ASSET",
  "natureCode": "DEBIT",
  "fromDate": "2026-01-01",
  "toDate": "2026-01-31",
  "dimensionId": 2,
  "dimensionValueId": 7,
  "debitTotal": 5000.0,
  "creditTotal": 1800.0,
  "closingBalance": 3200.0,
  "rows": [
    {
      "journalEntryId": 12,
      "docNo": "JV-2026-000123",
      "docDate": "2026-01-31",
      "journalTypeCode": "MANUAL",
      "eventReference": "INV-2026-0001",
      "journalLineId": 44,
      "lineNo": 1,
      "amount": 1500.0,
      "directionCode": "DEBIT",
      "signedAmount": 1500.0,
      "runningBalance": 3200.0,
      "descriptionAr": "إيجار يناير",
      "descriptionEn": "January rent"
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `ReportController.accountLedger`, `ReportService.accountLedger`, `ReportMapper.toAccountLedger`, `ReportMapper.signedAgainstNature`, `ReportMapper.zeroIfNull`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-ACCOUNT` | FIN_404_ACCOUNT | ReportService.accountLedger |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

## GET /api/v1/fin/reports/balance-sheet

**Balance sheet**

الميزانية العمومية

Contract ID: `API-FIN-030`

Operation ID: `balanceSheet`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_BALANCE_SHEET_VIEW (found on service:ReportService)

### Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| fiscalYearId | integer | Yes |  |
| asOfDate | string | No |  |

### Response `200` — OK

Shape: `BalanceSheetResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| fiscalYearId | integer (int64) | No |  | Fiscal year the statement is scoped to - السنة المالية | 1 |
| asOfDate | string (date) | No |  | Cut-off applied to docDate, null when unbounded - تاريخ الإقفال | 2026-12-31 |
| groups | array<AccountBalanceGroupResponse> | No |  | Groups: ASSET, LIABILITY, EQUITY - المجموعات — Statement group by account type - مجموعة القائمة حسب نوع الحساب |  |
| groups[].accountTypeCode | string | No |  | Account type, ACCOUNT_TYPE lookup - نوع الحساب | ASSET |
| groups[].rows | array<AccountBalanceRowResponse> | No |  | Accounts in this group, ordered by code - حسابات المجموعة — Live-derived account balance row - سطر رصيد حساب مُشتق حيًا |  |
| groups[].rows[].accountId | integer (int64) | No |  | Account id, for drill-down (REQ-FIN-046) - معرّف الحساب | 5 |
| groups[].rows[].accountCode | string | No |  | Account code - رمز الحساب | 1101 |
| groups[].rows[].accountNameAr | string | No |  | Account name (Arabic) - اسم الحساب بالعربية | النقدية بالصندوق |
| groups[].rows[].accountNameEn | string | No |  | Account name (English) - اسم الحساب بالإنجليزية | Cash on hand |
| groups[].rows[].accountTypeCode | string | No |  | Account type, ACCOUNT_TYPE lookup - نوع الحساب | ASSET |
| groups[].rows[].natureCode | string | No |  | Normal balance side, DEBIT_CREDIT lookup - طبيعة الحساب | DEBIT |
| groups[].rows[].debitTotal | number | No |  | Sum of DEBIT line amounts - إجمالي المدين | 5000.0 |
| groups[].rows[].creditTotal | number | No |  | Sum of CREDIT line amounts - إجمالي الدائن | 1800.0 |
| groups[].rows[].debitBalance | number | No |  | Balance shown on the debit column, zero otherwise (POL-FIN-002) - الرصيد المدين | 3200.0 |
| groups[].rows[].creditBalance | number | No |  | Balance shown on the credit column, zero otherwise (POL-FIN-002) - الرصيد الدائن | 0.0 |
| groups[].rows[].signedBalance | number | No |  | Net balance signed against the account's nature (POL-FIN-002) - الرصيد الصافي بإشارة طبيعة الحساب | 3200.0 |
| groups[].groupTotal | number | No |  | Sum of the group's signed balances (POL-FIN-002) - إجمالي المجموعة | 42000.0 |

**Response Example**

```json
{
  "fiscalYearId": 1,
  "asOfDate": "2026-12-31",
  "groups": [
    {
      "accountTypeCode": "ASSET",
      "rows": [
        {
          "accountId": 5,
          "accountCode": 1101,
          "accountNameAr": "النقدية بالصندوق",
          "accountNameEn": "Cash on hand",
          "accountTypeCode": "ASSET",
          "natureCode": "DEBIT",
          "debitTotal": 5000.0,
          "creditTotal": 1800.0,
          "debitBalance": 3200.0,
          "creditBalance": 0.0,
          "signedBalance": 3200.0
        }
      ],
      "groupTotal": 42000.0
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `ReportController.balanceSheet`, `ReportService.balanceSheet`, `ReportService.assertFiscalYearExists`, `ReportService.balanceRows`, `ReportService.groupsOf`, `ReportMapper.toAccountBalanceRow`, `ReportMapper.toGroup`, `ReportMapper.zeroIfNull`, `JournalEntryDomain.directionOfNet`, `ReportMapper.signedAgainstNature`, `ReportMapper.sumSignedBalances`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-YEAR` | FIN_404_YEAR | ReportService.assertFiscalYearExists |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

## GET /api/v1/fin/reports/dimension

**Dimension report**

تقرير الأبعاد حسب تركيبة الحساب وقيمة البُعد

Contract ID: `API-FIN-032`

Operation ID: `dimensionReport`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_DIMENSION_REPORTS_VIEW (found on service:ReportService)

### Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| dimensionId | integer | Yes |  |
| dimensionValueId | integer | No |  |
| periodId | integer | No |  |

### Response `200` — OK

Shape: `DimensionReportResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| dimensionId | integer (int64) | No |  | Dimension the report is grouped by - البُعد | 2 |
| dimensionValueId | integer (int64) | No |  | Dimension value filter applied, null when all values - قيمة البُعد المُرشَّحة | 7 |
| periodId | integer (int64) | No |  | Fiscal period filter applied, null when unbounded - الفترة المُرشَّحة | 3 |
| rows | array<DimensionReportRowResponse> | No |  | Rows, one per account and dimension value - السطور — Dimension report row - سطر تقرير الأبعاد |  |
| rows[].accountId | integer (int64) | No |  | Account id - معرّف الحساب | 5 |
| rows[].accountCode | string | No |  | Account code - رمز الحساب | 5201 |
| rows[].accountNameAr | string | No |  | Account name (Arabic) - اسم الحساب بالعربية | مصروف إيجار |
| rows[].accountNameEn | string | No |  | Account name (English) - اسم الحساب بالإنجليزية | Rent expense |
| rows[].natureCode | string | No |  | Normal balance side, DEBIT_CREDIT lookup - طبيعة الحساب | DEBIT |
| rows[].dimensionId | integer (int64) | No |  | Dimension id - معرّف البُعد | 2 |
| rows[].dimensionValueId | integer (int64) | No |  | Dimension value id - معرّف قيمة البُعد | 7 |
| rows[].dimensionValueCode | string | No |  | Dimension value code - رمز قيمة البُعد | PRJ-A |
| rows[].dimensionValueNameAr | string | No |  | Dimension value name (Arabic) - اسم قيمة البُعد بالعربية | مشروع أ |
| rows[].dimensionValueNameEn | string | No |  | Dimension value name (English) - اسم قيمة البُعد بالإنجليزية | Project A |
| rows[].debitTotal | number | No |  | Sum of DEBIT line amounts - إجمالي المدين | 5000.0 |
| rows[].creditTotal | number | No |  | Sum of CREDIT line amounts - إجمالي الدائن | 0.0 |
| rows[].signedBalance | number | No |  | Net balance signed against the account's nature (POL-FIN-002) - الرصيد الصافي | 5000.0 |

**Response Example**

```json
{
  "dimensionId": 2,
  "dimensionValueId": 7,
  "periodId": 3,
  "rows": [
    {
      "accountId": 5,
      "accountCode": 5201,
      "accountNameAr": "مصروف إيجار",
      "accountNameEn": "Rent expense",
      "natureCode": "DEBIT",
      "dimensionId": 2,
      "dimensionValueId": 7,
      "dimensionValueCode": "PRJ-A",
      "dimensionValueNameAr": "مشروع أ",
      "dimensionValueNameEn": "Project A",
      "debitTotal": 5000.0,
      "creditTotal": 0.0,
      "signedBalance": 5000.0
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `ReportController.dimensionReport`, `ReportService.dimensionReport`, `ReportMapper.toDimensionReportRow`, `ReportMapper.zeroIfNull`, `ReportMapper.signedAgainstNature`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-DIMENSION` | FIN_404_DIMENSION | ReportService.dimensionReport |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

## GET /api/v1/fin/reports/income-statement

**Income statement**

قائمة الدخل

Contract ID: `API-FIN-031`

Operation ID: `incomeStatement`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_INCOME_STATEMENT_VIEW (found on service:ReportService)

### Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| fiscalYearId | integer | Yes |  |
| fromPeriodId | integer | No |  |
| toPeriodId | integer | No |  |

### Response `200` — OK

Shape: `IncomeStatementResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| fiscalYearId | integer (int64) | No |  | Fiscal year the statement is scoped to - السنة المالية | 1 |
| fromPeriodId | integer (int64) | No |  | First period of the requested range, null when unbounded - أول فترة في المدى | 1 |
| toPeriodId | integer (int64) | No |  | Last period of the requested range, null when unbounded - آخر فترة في المدى | 6 |
| fromDate | string (date) | No |  | docDate lower bound derived from fromPeriodId - بداية المدى | 2026-01-01 |
| toDate | string (date) | No |  | docDate upper bound derived from toPeriodId - نهاية المدى | 2026-06-30 |
| groups | array<AccountBalanceGroupResponse> | No |  | Groups: REVENUE, EXPENSE - المجموعات — Statement group by account type - مجموعة القائمة حسب نوع الحساب |  |
| groups[].accountTypeCode | string | No |  | Account type, ACCOUNT_TYPE lookup - نوع الحساب | ASSET |
| groups[].rows | array<AccountBalanceRowResponse> | No |  | Accounts in this group, ordered by code - حسابات المجموعة — Live-derived account balance row - سطر رصيد حساب مُشتق حيًا |  |
| groups[].rows[].accountId | integer (int64) | No |  | Account id, for drill-down (REQ-FIN-046) - معرّف الحساب | 5 |
| groups[].rows[].accountCode | string | No |  | Account code - رمز الحساب | 1101 |
| groups[].rows[].accountNameAr | string | No |  | Account name (Arabic) - اسم الحساب بالعربية | النقدية بالصندوق |
| groups[].rows[].accountNameEn | string | No |  | Account name (English) - اسم الحساب بالإنجليزية | Cash on hand |
| groups[].rows[].accountTypeCode | string | No |  | Account type, ACCOUNT_TYPE lookup - نوع الحساب | ASSET |
| groups[].rows[].natureCode | string | No |  | Normal balance side, DEBIT_CREDIT lookup - طبيعة الحساب | DEBIT |
| groups[].rows[].debitTotal | number | No |  | Sum of DEBIT line amounts - إجمالي المدين | 5000.0 |
| groups[].rows[].creditTotal | number | No |  | Sum of CREDIT line amounts - إجمالي الدائن | 1800.0 |
| groups[].rows[].debitBalance | number | No |  | Balance shown on the debit column, zero otherwise (POL-FIN-002) - الرصيد المدين | 3200.0 |
| groups[].rows[].creditBalance | number | No |  | Balance shown on the credit column, zero otherwise (POL-FIN-002) - الرصيد الدائن | 0.0 |
| groups[].rows[].signedBalance | number | No |  | Net balance signed against the account's nature (POL-FIN-002) - الرصيد الصافي بإشارة طبيعة الحساب | 3200.0 |
| groups[].groupTotal | number | No |  | Sum of the group's signed balances (POL-FIN-002) - إجمالي المجموعة | 42000.0 |
| netResult | number | No |  | Revenue total less expense total, both signed per POL-FIN-002 - صافي النتيجة | 8400.0 |

**Response Example**

```json
{
  "fiscalYearId": 1,
  "fromPeriodId": 1,
  "toPeriodId": 6,
  "fromDate": "2026-01-01",
  "toDate": "2026-06-30",
  "groups": [
    {
      "accountTypeCode": "ASSET",
      "rows": [
        {
          "accountId": 5,
          "accountCode": 1101,
          "accountNameAr": "النقدية بالصندوق",
          "accountNameEn": "Cash on hand",
          "accountTypeCode": "ASSET",
          "natureCode": "DEBIT",
          "debitTotal": 5000.0,
          "creditTotal": 1800.0,
          "debitBalance": 3200.0,
          "creditBalance": 0.0,
          "signedBalance": 3200.0
        }
      ],
      "groupTotal": 42000.0
    }
  ],
  "netResult": 8400.0
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `ReportController.incomeStatement`, `ReportService.incomeStatement`, `ReportService.assertFiscalYearExists`, `ReportService.periodOf`, `ReportService.balanceRows`, `ReportService.groupsOf`, `ReportMapper.toAccountBalanceRow`, `ReportMapper.toGroup`, `ReportMapper.zeroIfNull`, `JournalEntryDomain.directionOfNet`, `ReportMapper.signedAgainstNature`, `ReportMapper.sumSignedBalances`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-PERIOD` | FIN_404_PERIOD | ReportService.periodOf |
| 404 NOT_FOUND | `FIN-404-YEAR` | FIN_404_YEAR | ReportService.assertFiscalYearExists |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

## GET /api/v1/fin/reports/trial-balance

**Trial balance**

ميزان المراجعة المُشتق حيًا

Contract ID: `API-FIN-029`

Operation ID: `trialBalance`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_TRIAL_BALANCE_VIEW (found on service:ReportService)

### Query Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| periodId | integer | No |  |
| accountTypeCode | string | No |  |

### Response `200` — OK

Shape: `TrialBalanceResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| periodId | integer (int64) | No |  | Fiscal period filter applied, null when unbounded - الفترة المُرشَّحة | 3 |
| accountTypeCode | string | No |  | Account-type filter applied, null when all types - النوع المُرشَّح | ASSET |
| rows | array<AccountBalanceRowResponse> | No |  | Account rows, ordered by account code - سطور الحسابات — Live-derived account balance row - سطر رصيد حساب مُشتق حيًا |  |
| rows[].accountId | integer (int64) | No |  | Account id, for drill-down (REQ-FIN-046) - معرّف الحساب | 5 |
| rows[].accountCode | string | No |  | Account code - رمز الحساب | 1101 |
| rows[].accountNameAr | string | No |  | Account name (Arabic) - اسم الحساب بالعربية | النقدية بالصندوق |
| rows[].accountNameEn | string | No |  | Account name (English) - اسم الحساب بالإنجليزية | Cash on hand |
| rows[].accountTypeCode | string | No |  | Account type, ACCOUNT_TYPE lookup - نوع الحساب | ASSET |
| rows[].natureCode | string | No |  | Normal balance side, DEBIT_CREDIT lookup - طبيعة الحساب | DEBIT |
| rows[].debitTotal | number | No |  | Sum of DEBIT line amounts - إجمالي المدين | 5000.0 |
| rows[].creditTotal | number | No |  | Sum of CREDIT line amounts - إجمالي الدائن | 1800.0 |
| rows[].debitBalance | number | No |  | Balance shown on the debit column, zero otherwise (POL-FIN-002) - الرصيد المدين | 3200.0 |
| rows[].creditBalance | number | No |  | Balance shown on the credit column, zero otherwise (POL-FIN-002) - الرصيد الدائن | 0.0 |
| rows[].signedBalance | number | No |  | Net balance signed against the account's nature (POL-FIN-002) - الرصيد الصافي بإشارة طبيعة الحساب | 3200.0 |
| totalDebitBalance | number | No |  | Total of the debit-balance column - إجمالي الأرصدة المدينة | 12500.0 |
| totalCreditBalance | number | No |  | Total of the credit-balance column - إجمالي الأرصدة الدائنة | 12500.0 |
| balanced | boolean | No |  | Whether the two column totals match, an invariant by construction (POL-FIN-008) - هل يتوازن الميزان | true |

**Response Example**

```json
{
  "periodId": 3,
  "accountTypeCode": "ASSET",
  "rows": [
    {
      "accountId": 5,
      "accountCode": 1101,
      "accountNameAr": "النقدية بالصندوق",
      "accountNameEn": "Cash on hand",
      "accountTypeCode": "ASSET",
      "natureCode": "DEBIT",
      "debitTotal": 5000.0,
      "creditTotal": 1800.0,
      "debitBalance": 3200.0,
      "creditBalance": 0.0,
      "signedBalance": 3200.0
    }
  ],
  "totalDebitBalance": 12500.0,
  "totalCreditBalance": 12500.0,
  "balanced": true
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `ReportController.trialBalance`, `ReportService.trialBalance`, `ReportService.periodOf`, `ReportService.balanceRows`, `ReportMapper.sumDebitBalances`, `ReportMapper.sumCreditBalances`, `ReportMapper.toAccountBalanceRow`, `ReportMapper.zeroIfNull`, `JournalEntryDomain.directionOfNet`, `ReportMapper.signedAgainstNature`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-PERIOD` | FIN_404_PERIOD | ReportService.periodOf |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

<!-- fetched-from: endpoints/fin-fiscal-period-management.md -->
<!-- AUTO-GENERATED by api-doc-generator — do not edit manually -->
# FIN Fiscal Period Management

**Endpoints in this file:**

- `API-FIN-033` — [POST /api/v1/fin/fiscal-periods/search](#post-apiv1finfiscal-periodssearch)
- `API-FIN-026` — [PATCH /api/v1/fin/fiscal-periods/{id}/hard-close](#patch-apiv1finfiscal-periodsidhard-close)
- `API-FIN-024` — [PATCH /api/v1/fin/fiscal-periods/{id}/open](#patch-apiv1finfiscal-periodsidopen)
- `API-FIN-025` — [PATCH /api/v1/fin/fiscal-periods/{id}/soft-close](#patch-apiv1finfiscal-periodsidsoft-close)

## POST /api/v1/fin/fiscal-periods/search

**Search fiscal periods**

البحث في الفترات المحاسبية

Contract ID: `API-FIN-033`

Operation ID: `search_3`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_PERIODS_VIEW (found on service:FiscalPeriodService)

### Request Body

Schema: `FiscalPeriodSearchRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| filters | array<SearchFilter> | No |  | Filter criteria. Supported fields: fiscalYearId (EQUALS - the parent scope), statusCode (EQUALS, IN - PERIOD_STATE), periodNo (EQUALS, comparison operators), nameAr / nameEn (LIKE), startDate / endDate (EQUALS, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL - ISO yyyy-MM-dd), fiscalPeriodPk (EQUALS, IN), createdAt (comparison operators). Any other field, or an operator a field does not list, is rejected as 400 VALIDATION_ERROR naming it - معايير التصفية |  |
| filters[].field | string | No |  |  |  |
| filters[].operator | string | No | enum: EQUALS, NOT_EQUALS, LIKE, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, IN |  |  |
| filters[].value | object | No |  |  |  |
| sortField | string | No |  | Sort field. Supported: fiscalPeriodPk, periodNo, nameAr, nameEn, startDate, endDate, statusCode, createdAt. Any other value is rejected as 400 FIN-400-INVALID-SORT - حقل الترتيب |  |
| sortDirection | string | No | enum: ASC, DESC | Sort direction - اتجاه الترتيب |  |
| page | integer (int32) | No |  | Page number, zero-based - رقم الصفحة | 0 |
| size | integer (int32) | No |  | Page size - حجم الصفحة | 20 |

**Request Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "page": 0,
  "size": 20
}
```

### Response `200` — OK

Shape: `paginated list of FiscalPeriodResponse (see Pagination Envelope in index.md)`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| fiscalPeriodPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periodNo | integer (int32) | No |  | Period number within the year - رقم الفترة | 1 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | الفترة 1 |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Period 1 |
| startDate | string (date) | No |  | First day of the period - أول أيام الفترة | 2026-01-01 |
| endDate | string (date) | No |  | Last day of the period - آخر أيام الفترة | 2026-01-31 |
| statusCode | string | No |  | Status, PERIOD_STATE lookup - حالة الفترة | OPEN |
| closedBy | string | No |  | Close-approval principal - معتمد الإغلاق | finance.approver |
| closedAt | string (date-time) | No |  | Close-approval moment - وقت اعتماد الإغلاق |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "fiscalPeriodPk": 1,
  "fiscalYearId": 1,
  "periodNo": 1,
  "nameAr": "الفترة 1",
  "nameEn": "Period 1",
  "startDate": "2026-01-01",
  "endDate": "2026-01-31",
  "statusCode": "OPEN",
  "closedBy": "finance.approver"
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `FiscalPeriodController.search`, `FiscalPeriodService.search`, `FinSearchSupport.assertSortAllowed`, `FiscalPeriodSearchRequest.getSortField`, `FiscalPeriodSearchRequest.toCommonSearchRequest`, `FinSearchSupport.temporalFieldConverter`, `FiscalPeriodSearchRequest.getFiscalYearId`, `FiscalPeriodMapper.toResponse`, `FinSearchSupport.temporalFieldConverter`, `FinSearchSupport.parseLocalDate`, `FinSearchSupport.parseInstant`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-SORT` | FIN_400_INVALID_SORT | FinSearchSupport.assertSortAllowed |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseInstant |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseLocalDate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## PATCH /api/v1/fin/fiscal-periods/{id}/hard-close

**Hard-close a fiscal period (approval)**

إغلاق صارم لفترة محاسبية باعتماد

Contract ID: `API-FIN-026`

Operation ID: `hardClose`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_PERIODS_CLOSE_APPROVE (found on service:FiscalPeriodService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `200` — OK

Shape: `FiscalPeriodResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| fiscalPeriodPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periodNo | integer (int32) | No |  | Period number within the year - رقم الفترة | 1 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | الفترة 1 |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Period 1 |
| startDate | string (date) | No |  | First day of the period - أول أيام الفترة | 2026-01-01 |
| endDate | string (date) | No |  | Last day of the period - آخر أيام الفترة | 2026-01-31 |
| statusCode | string | No |  | Status, PERIOD_STATE lookup - حالة الفترة | OPEN |
| closedBy | string | No |  | Close-approval principal - معتمد الإغلاق | finance.approver |
| closedAt | string (date-time) | No |  | Close-approval moment - وقت اعتماد الإغلاق |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "fiscalPeriodPk": 1,
  "fiscalYearId": 1,
  "periodNo": 1,
  "nameAr": "الفترة 1",
  "nameEn": "Period 1",
  "startDate": "2026-01-01",
  "endDate": "2026-01-31",
  "statusCode": "OPEN",
  "closedBy": "finance.approver"
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `FiscalPeriodController.hardClose`, `FiscalPeriodService.hardClose`, `FiscalPeriodService.lockOrThrow`, `FiscalPeriodDomain.from`, `FiscalPeriod.hardClose`, `FiscalPeriodMapper.toResponse`, `FiscalPeriodDomain.assertCanReopen`, `new FiscalPeriodDomain()`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-PERIOD` | FIN_404_PERIOD | FiscalPeriodService.lockOrThrow |
| 409 CONFLICT | `FIN-409-NOT-REOPENABLE` | FIN_409_NOT_REOPENABLE | FiscalPeriodDomain.assertCanReopen |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

## PATCH /api/v1/fin/fiscal-periods/{id}/open

**Open a fiscal period**

فتح فترة محاسبية

Contract ID: `API-FIN-024`

Operation ID: `open`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_PERIODS_UPDATE (found on service:FiscalPeriodService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `200` — OK

Shape: `FiscalPeriodResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| fiscalPeriodPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periodNo | integer (int32) | No |  | Period number within the year - رقم الفترة | 1 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | الفترة 1 |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Period 1 |
| startDate | string (date) | No |  | First day of the period - أول أيام الفترة | 2026-01-01 |
| endDate | string (date) | No |  | Last day of the period - آخر أيام الفترة | 2026-01-31 |
| statusCode | string | No |  | Status, PERIOD_STATE lookup - حالة الفترة | OPEN |
| closedBy | string | No |  | Close-approval principal - معتمد الإغلاق | finance.approver |
| closedAt | string (date-time) | No |  | Close-approval moment - وقت اعتماد الإغلاق |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "fiscalPeriodPk": 1,
  "fiscalYearId": 1,
  "periodNo": 1,
  "nameAr": "الفترة 1",
  "nameEn": "Period 1",
  "startDate": "2026-01-01",
  "endDate": "2026-01-31",
  "statusCode": "OPEN",
  "closedBy": "finance.approver"
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `FiscalPeriodController.open`, `FiscalPeriodService.open`, `FiscalPeriodService.lockOrThrow`, `FiscalPeriodDomain.from`, `FiscalPeriod.open`, `FiscalPeriodMapper.toResponse`, `FiscalPeriodDomain.assertCanReopen`, `new FiscalPeriodDomain()`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-PERIOD` | FIN_404_PERIOD | FiscalPeriodService.lockOrThrow |
| 409 CONFLICT | `FIN-409-NOT-REOPENABLE` | FIN_409_NOT_REOPENABLE | FiscalPeriodDomain.assertCanReopen |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

## PATCH /api/v1/fin/fiscal-periods/{id}/soft-close

**Soft-close a fiscal period**

إغلاق ناعم لفترة محاسبية

Contract ID: `API-FIN-025`

Operation ID: `softClose`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_PERIODS_UPDATE (found on service:FiscalPeriodService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `200` — OK

Shape: `FiscalPeriodResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| fiscalPeriodPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periodNo | integer (int32) | No |  | Period number within the year - رقم الفترة | 1 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | الفترة 1 |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Period 1 |
| startDate | string (date) | No |  | First day of the period - أول أيام الفترة | 2026-01-01 |
| endDate | string (date) | No |  | Last day of the period - آخر أيام الفترة | 2026-01-31 |
| statusCode | string | No |  | Status, PERIOD_STATE lookup - حالة الفترة | OPEN |
| closedBy | string | No |  | Close-approval principal - معتمد الإغلاق | finance.approver |
| closedAt | string (date-time) | No |  | Close-approval moment - وقت اعتماد الإغلاق |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "fiscalPeriodPk": 1,
  "fiscalYearId": 1,
  "periodNo": 1,
  "nameAr": "الفترة 1",
  "nameEn": "Period 1",
  "startDate": "2026-01-01",
  "endDate": "2026-01-31",
  "statusCode": "OPEN",
  "closedBy": "finance.approver"
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `FiscalPeriodController.softClose`, `FiscalPeriodService.softClose`, `FiscalPeriodService.lockOrThrow`, `FiscalPeriodDomain.from`, `FiscalPeriod.softClose`, `FiscalPeriodMapper.toResponse`, `FiscalPeriodDomain.assertCanSoftClose`, `new FiscalPeriodDomain()`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-PERIOD` | FIN_404_PERIOD | FiscalPeriodService.lockOrThrow |
| 409 CONFLICT | `FIN-409-INVALID-TRANSITION` | FIN_409_INVALID_TRANSITION | FiscalPeriodDomain.assertCanSoftClose |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

<!-- fetched-from: endpoints/fin-fiscal-year-management.md -->
<!-- AUTO-GENERATED by api-doc-generator — do not edit manually -->
# FIN Fiscal Year Management

**Endpoints in this file:**

- `API-FIN-023` — [POST /api/v1/fin/fiscal-years](#post-apiv1finfiscal-years)
- [POST /api/v1/fin/fiscal-years/search](#post-apiv1finfiscal-yearssearch)
- `API-FIN-027` — [POST /api/v1/fin/fiscal-years/{id}/year-end-close](#post-apiv1finfiscal-yearsidyear-end-close)

## POST /api/v1/fin/fiscal-years

**Create fiscal year and generate its periods**

إنشاء سنة مالية وتوليد فتراتها

Contract ID: `API-FIN-023`

Operation ID: `create_1`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_PERIODS_CREATE (found on service:FiscalYearService)

### Request Body

Schema: `FiscalYearCreateRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| code | string | Yes | maxLength: 10 | Fiscal year code - رمز السنة المالية | 2026 |
| startDate | string (date) | Yes |  | First day of the fiscal year - أول أيام السنة المالية | 2026-01-01 |
| endDate | string (date) | Yes |  | Last day of the fiscal year - آخر أيام السنة المالية | 2026-12-31 |
| periodCount | integer (int32) | Yes |  | Number of periods to generate, each initially Open - عدد الفترات | 12 |

**Request Example**

```json
{
  "code": 2026,
  "startDate": "2026-01-01",
  "endDate": "2026-12-31",
  "periodCount": 12
}
```

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `FiscalYearService.create`.

Shape: `FiscalYearResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| fiscalYearPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| code | string | No |  | Fiscal year code - رمز السنة المالية | 2026 |
| startDate | string (date) | No |  | First day of the fiscal year - أول أيام السنة المالية | 2026-01-01 |
| endDate | string (date) | No |  | Last day of the fiscal year - آخر أيام السنة المالية | 2026-12-31 |
| statusCode | string | No |  | Status, FISCAL_YEAR_STATUS lookup - حالة السنة | OPEN |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| periodCount | integer (int32) | No |  | Number of periods - عدد الفترات | 12 |
| periods | array<FiscalPeriodResponse> | No |  | Generated periods. Populated by the create response only; ALWAYS EMPTY on a search row, where the period set is read through the fiscal-period search scoped by fiscalYearId - الفترات المولَّدة، تُملأ في استجابة الإنشاء فقط وتكون فارغة دائمًا في صف البحث — Fiscal period - الفترة المحاسبية |  |
| periods[].fiscalPeriodPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| periods[].fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periods[].periodNo | integer (int32) | No |  | Period number within the year - رقم الفترة | 1 |
| periods[].nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | الفترة 1 |
| periods[].nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Period 1 |
| periods[].startDate | string (date) | No |  | First day of the period - أول أيام الفترة | 2026-01-01 |
| periods[].endDate | string (date) | No |  | Last day of the period - آخر أيام الفترة | 2026-01-31 |
| periods[].statusCode | string | No |  | Status, PERIOD_STATE lookup - حالة الفترة | OPEN |
| periods[].closedBy | string | No |  | Close-approval principal - معتمد الإغلاق | finance.approver |
| periods[].closedAt | string (date-time) | No |  | Close-approval moment - وقت اعتماد الإغلاق |  |
| periods[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| periods[].createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| periods[].updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| periods[].updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "fiscalYearPk": 1,
  "code": 2026,
  "startDate": "2026-01-01",
  "endDate": "2026-12-31",
  "statusCode": "OPEN",
  "isActiveFl": true,
  "periodCount": 12,
  "periods": [
    {
      "fiscalPeriodPk": 1,
      "fiscalYearId": 1,
      "periodNo": 1,
      "nameAr": "الفترة 1",
      "nameEn": "Period 1",
      "startDate": "2026-01-01",
      "endDate": "2026-01-31",
      "statusCode": "OPEN",
      "closedBy": "finance.approver"
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `FiscalYearController.create`, `FiscalYearService.create`, `FiscalYearMapper.toEntity`, `FiscalYearMapper.toPeriodEntity`, `FiscalYearMapper.toResponse`, `FiscalPeriodMapper.toResponse`, `FiscalPeriodDomain.generatedPeriod`, `FiscalPeriodDomain.isWholeYearInCalendarMonths`, `FiscalPeriodDomain.GeneratedPeriod`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 409 CONFLICT | `FIN-409-YEAR-DUP` | FIN_409_YEAR_DUP | FiscalYearService.create |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## POST /api/v1/fin/fiscal-years/search

**Search fiscal years**

بحث في السنوات المالية

Operation ID: `search_2`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_PERIODS_VIEW (found on service:FiscalYearService)

### Request Body

Schema: `FiscalYearSearchRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| filters | array<SearchFilter> | No |  | Filter criteria. Supported fields: code (LIKE), statusCode (EQUALS, IN - FISCAL_YEAR_STATUS), startDate / endDate (EQUALS, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL - ISO yyyy-MM-dd), isActiveFl (EQUALS), fiscalYearPk (EQUALS, IN), createdAt (EQUALS, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL). Any other field, or an operator a field does not list, is rejected as 400 VALIDATION_ERROR naming it - معايير التصفية |  |
| filters[].field | string | No |  |  |  |
| filters[].operator | string | No | enum: EQUALS, NOT_EQUALS, LIKE, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, IN |  |  |
| filters[].value | object | No |  |  |  |
| sortField | string | No |  | Sort field. Supported: fiscalYearPk, code, startDate, endDate, statusCode, isActiveFl, createdAt. Any other value is rejected as 400 FIN-400-INVALID-SORT - حقل الترتيب |  |
| sortDirection | string | No | enum: ASC, DESC | Sort direction - اتجاه الترتيب |  |
| page | integer (int32) | No |  | Page number, zero-based - رقم الصفحة | 0 |
| size | integer (int32) | No |  | Page size - حجم الصفحة | 20 |

**Request Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "page": 0,
  "size": 20
}
```

### Response `200` — OK

Shape: `paginated list of FiscalYearResponse (see Pagination Envelope in index.md)`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| fiscalYearPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| code | string | No |  | Fiscal year code - رمز السنة المالية | 2026 |
| startDate | string (date) | No |  | First day of the fiscal year - أول أيام السنة المالية | 2026-01-01 |
| endDate | string (date) | No |  | Last day of the fiscal year - آخر أيام السنة المالية | 2026-12-31 |
| statusCode | string | No |  | Status, FISCAL_YEAR_STATUS lookup - حالة السنة | OPEN |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| periodCount | integer (int32) | No |  | Number of periods - عدد الفترات | 12 |
| periods | array<FiscalPeriodResponse> | No |  | Generated periods. Populated by the create response only; ALWAYS EMPTY on a search row, where the period set is read through the fiscal-period search scoped by fiscalYearId - الفترات المولَّدة، تُملأ في استجابة الإنشاء فقط وتكون فارغة دائمًا في صف البحث — Fiscal period - الفترة المحاسبية |  |
| periods[].fiscalPeriodPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| periods[].fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periods[].periodNo | integer (int32) | No |  | Period number within the year - رقم الفترة | 1 |
| periods[].nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | الفترة 1 |
| periods[].nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Period 1 |
| periods[].startDate | string (date) | No |  | First day of the period - أول أيام الفترة | 2026-01-01 |
| periods[].endDate | string (date) | No |  | Last day of the period - آخر أيام الفترة | 2026-01-31 |
| periods[].statusCode | string | No |  | Status, PERIOD_STATE lookup - حالة الفترة | OPEN |
| periods[].closedBy | string | No |  | Close-approval principal - معتمد الإغلاق | finance.approver |
| periods[].closedAt | string (date-time) | No |  | Close-approval moment - وقت اعتماد الإغلاق |  |
| periods[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| periods[].createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| periods[].updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| periods[].updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "fiscalYearPk": 1,
  "code": 2026,
  "startDate": "2026-01-01",
  "endDate": "2026-12-31",
  "statusCode": "OPEN",
  "isActiveFl": true,
  "periodCount": 12,
  "periods": [
    {
      "fiscalPeriodPk": 1,
      "fiscalYearId": 1,
      "periodNo": 1,
      "nameAr": "الفترة 1",
      "nameEn": "Period 1",
      "startDate": "2026-01-01",
      "endDate": "2026-01-31",
      "statusCode": "OPEN",
      "closedBy": "finance.approver"
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `FiscalYearController.search`, `FiscalYearService.search`, `FinSearchSupport.assertSortAllowed`, `FiscalYearSearchRequest.getSortField`, `FinSearchSupport.temporalFieldConverter`, `FiscalYearMapper.toSummaryResponse`, `FinSearchSupport.temporalFieldConverter`, `FiscalYearMapper.toResponse`, `FinSearchSupport.parseLocalDate`, `FinSearchSupport.parseInstant`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-SORT` | FIN_400_INVALID_SORT | FinSearchSupport.assertSortAllowed |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseInstant |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseLocalDate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## POST /api/v1/fin/fiscal-years/{id}/year-end-close

**Run year-end close**

تشغيل إقفال نهاية السنة وتوليد قيدي الإقفال والافتتاح

Contract ID: `API-FIN-027`

Operation ID: `yearEndClose`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_PERIODS_CLOSE_APPROVE (found on service:FiscalYearService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `FiscalYearService.yearEndClose`.

Shape: `YearEndCloseResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| closingEntry | JournalEntryResponse | No |  | Journal entry - قيد يومية |  |
| closingEntry.journalEntryPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| closingEntry.docNo | string | No |  | System-generated document number - رقم المستند | JV-2026-000123 |
| closingEntry.docDate | string (date) | No |  | Document date - تاريخ المستند | 2026-01-31 |
| closingEntry.fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| closingEntry.periodId | integer (int64) | No |  | Target fiscal period id - معرّف الفترة المالية | 1 |
| closingEntry.journalTypeCode | string | No |  | Journal type, JOURNAL_TYPE lookup - نوع اليومية | MANUAL |
| closingEntry.statusCode | string | No |  | Status, JOURNAL_STATUS lookup - الحالة | POSTED |
| closingEntry.eventReference | string | No |  | Source event reference, for event-built entries - مرجع الحدث | INV-2026-0001 |
| closingEntry.originalEntryId | integer (int64) | No |  | Original entry id, when this entry is a reversal - معرّف القيد الأصلي | 7 |
| closingEntry.reversalEntryId | integer (int64) | No |  | Reversal entry id, when this entry has been reversed - معرّف قيد العكس | 9 |
| closingEntry.descriptionAr | string | No |  | Entry description (Arabic) - وصف القيد بالعربية | قيد إهلاك يناير |
| closingEntry.descriptionEn | string | No |  | Entry description (English) - وصف القيد بالإنجليزية | January depreciation entry |
| closingEntry.postedAt | string (date-time) | No |  | Posting timestamp - تاريخ الترحيل |  |
| closingEntry.lineCount | integer (int32) | No |  | Number of entry lines - عدد السطور | 2 |
| closingEntry.lines | array<JournalLineResponse> | No |  | Entry lines - سطور القيد — Journal entry line - سطر قيد يومية |  |
| closingEntry.lines[].journalLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| closingEntry.lines[].journalEntryId | integer (int64) | No |  | Parent journal entry id - معرّف القيد الأب | 1 |
| closingEntry.lines[].lineNo | integer (int32) | No |  | Line position within the entry - رقم السطر | 1 |
| closingEntry.lines[].accountId | integer (int64) | No |  | Target account id - معرّف الحساب | 12 |
| closingEntry.lines[].amount | number | No |  | Line amount, always positive - المبلغ | 1500.0 |
| closingEntry.lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| closingEntry.lines[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| closingEntry.lines[].descriptionAr | string | No |  | Line description (Arabic) - وصف السطر بالعربية | إهلاك |
| closingEntry.lines[].descriptionEn | string | No |  | Line description (English) - وصف السطر بالإنجليزية | Depreciation |
| closingEntry.lines[].dimensions | array<JournalLineDimensionResponse> | No |  | Analysis dimension tags - الأبعاد التحليلية — Journal line dimension tag - بُعد تحليلي لسطر القيد |  |
| closingEntry.lines[].dimensions[].journalLineDimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| closingEntry.lines[].dimensions[].journalLineId | integer (int64) | No |  | Parent journal line id - معرّف سطر القيد الأب | 1 |
| closingEntry.lines[].dimensions[].dimensionId | integer (int64) | No |  | Dimension id - معرّف البُعد | 1 |
| closingEntry.lines[].dimensions[].dimensionValueId | integer (int64) | No |  | Dimension value id - معرّف قيمة البُعد | 5 |
| closingEntry.lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| closingEntry.createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| closingEntry.createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| closingEntry.updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| closingEntry.updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |
| openingEntry | JournalEntryResponse | No |  | Journal entry - قيد يومية |  |
| openingEntry.journalEntryPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| openingEntry.docNo | string | No |  | System-generated document number - رقم المستند | JV-2026-000123 |
| openingEntry.docDate | string (date) | No |  | Document date - تاريخ المستند | 2026-01-31 |
| openingEntry.fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| openingEntry.periodId | integer (int64) | No |  | Target fiscal period id - معرّف الفترة المالية | 1 |
| openingEntry.journalTypeCode | string | No |  | Journal type, JOURNAL_TYPE lookup - نوع اليومية | MANUAL |
| openingEntry.statusCode | string | No |  | Status, JOURNAL_STATUS lookup - الحالة | POSTED |
| openingEntry.eventReference | string | No |  | Source event reference, for event-built entries - مرجع الحدث | INV-2026-0001 |
| openingEntry.originalEntryId | integer (int64) | No |  | Original entry id, when this entry is a reversal - معرّف القيد الأصلي | 7 |
| openingEntry.reversalEntryId | integer (int64) | No |  | Reversal entry id, when this entry has been reversed - معرّف قيد العكس | 9 |
| openingEntry.descriptionAr | string | No |  | Entry description (Arabic) - وصف القيد بالعربية | قيد إهلاك يناير |
| openingEntry.descriptionEn | string | No |  | Entry description (English) - وصف القيد بالإنجليزية | January depreciation entry |
| openingEntry.postedAt | string (date-time) | No |  | Posting timestamp - تاريخ الترحيل |  |
| openingEntry.lineCount | integer (int32) | No |  | Number of entry lines - عدد السطور | 2 |
| openingEntry.lines | array<JournalLineResponse> | No |  | Entry lines - سطور القيد — Journal entry line - سطر قيد يومية |  |
| openingEntry.lines[].journalLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| openingEntry.lines[].journalEntryId | integer (int64) | No |  | Parent journal entry id - معرّف القيد الأب | 1 |
| openingEntry.lines[].lineNo | integer (int32) | No |  | Line position within the entry - رقم السطر | 1 |
| openingEntry.lines[].accountId | integer (int64) | No |  | Target account id - معرّف الحساب | 12 |
| openingEntry.lines[].amount | number | No |  | Line amount, always positive - المبلغ | 1500.0 |
| openingEntry.lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| openingEntry.lines[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| openingEntry.lines[].descriptionAr | string | No |  | Line description (Arabic) - وصف السطر بالعربية | إهلاك |
| openingEntry.lines[].descriptionEn | string | No |  | Line description (English) - وصف السطر بالإنجليزية | Depreciation |
| openingEntry.lines[].dimensions | array<JournalLineDimensionResponse> | No |  | Analysis dimension tags - الأبعاد التحليلية — Journal line dimension tag - بُعد تحليلي لسطر القيد |  |
| openingEntry.lines[].dimensions[].journalLineDimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| openingEntry.lines[].dimensions[].journalLineId | integer (int64) | No |  | Parent journal line id - معرّف سطر القيد الأب | 1 |
| openingEntry.lines[].dimensions[].dimensionId | integer (int64) | No |  | Dimension id - معرّف البُعد | 1 |
| openingEntry.lines[].dimensions[].dimensionValueId | integer (int64) | No |  | Dimension value id - معرّف قيمة البُعد | 5 |
| openingEntry.lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| openingEntry.createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| openingEntry.createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| openingEntry.updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| openingEntry.updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "closingEntry": {
    "journalEntryPk": 1,
    "docNo": "JV-2026-000123",
    "docDate": "2026-01-31",
    "fiscalYearId": 1,
    "periodId": 1,
    "journalTypeCode": "MANUAL",
    "statusCode": "POSTED",
    "eventReference": "INV-2026-0001",
    "originalEntryId": 7,
    "reversalEntryId": 9,
    "descriptionAr": "قيد إهلاك يناير",
    "descriptionEn": "January depreciation entry",
    "lineCount": 2,
    "lines": [
      {
        "journalLinePk": 1,
        "journalEntryId": 1,
        "lineNo": 1,
        "accountId": 12,
        "amount": 1500.0,
        "directionCode": "DEBIT",
        "isRemainderFl": false,
        "descriptionAr": "إهلاك",
        "descriptionEn": "Depreciation",
        "dimensions": [
          {
            "journalLineDimensionPk": 1,
            "journalLineId": 1,
            "dimensionId": 1,
            "dimensionValueId": 5
          }
        ]
      }
    ]
  },
  "openingEntry": {
    "journalEntryPk": 1,
    "docNo": "JV-2026-000123",
    "docDate": "2026-01-31",
    "fiscalYearId": 1,
    "periodId": 1,
    "journalTypeCode": "MANUAL",
    "statusCode": "POSTED",
    "eventReference": "INV-2026-0001",
    "originalEntryId": 7,
    "reversalEntryId": 9,
    "descriptionAr": "قيد إهلاك يناير",
    "descriptionEn": "January depreciation entry",
    "lineCount": 2,
    "lines": [
      {
        "journalLinePk": 1,
        "journalEntryId": 1,
        "lineNo": 1,
        "accountId": 12,
        "amount": 1500.0,
        "directionCode": "DEBIT",
        "isRemainderFl": false,
        "descriptionAr": "إهلاك",
        "descriptionEn": "Depreciation",
        "dimensions": [
          {
            "journalLineDimensionPk": 1,
            "journalLineId": 1,
            "dimensionId": 1,
            "dimensionValueId": 5
          }
        ]
      }
    ]
  }
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `FiscalYearController.yearEndClose`, `FiscalYearService.yearEndClose`, `FiscalYearService.lockOrThrow`, `FiscalYearDomain.from`, `FiscalPeriodDomain.from`, `FiscalYearService.successorOf`, `FiscalYearService.retainedEarningsAccount`, `JournalPostingService.buildValidateAndPost`, `FiscalYearService.lastPeriodOf`, `FiscalYearService.closingLines`, `FiscalYearService.firstPeriodOf`, `FiscalYearService.openingLines`, `FiscalPeriod.yearEndClose`, `FiscalYear.close`, `FiscalYearService.toEntryResponse`, `FiscalYearDomain.assertCanYearEndClose`, `FiscalPeriodDomain.assertHardClosedForYearEnd`, `new FiscalYearDomain()`, `new FiscalPeriodDomain()`, `AccountDomain.from`, `DimensionValueDomain.from`, `JournalDocNoGenerator.next`, `JournalPostingService.highestDocNoOf`, `JournalEntryMapper.toEntity`, `JournalEntryDomain.create`, `JournalEntryDomain.checkBalanced`, `JournalEntryDomain.checkTargetPeriodOpenForGeneratedEntry`, `JournalEntry.post`, `FiscalYearService.balancesOf`, `AccountDomain.isResultAccountType`, `JournalEntryDomain.reversalDirectionOf`, `JournalEntryDomain.directionOfNet`, `JournalEntryMapper.toResponse`, `JournalEntryMapper.toLineResponse`, `new AccountDomain()`, `new DimensionValueDomain()`, `JournalDocNoGenerator.format`, `JournalDocNoGenerator.counterOf`, `JournalEntryMapper.toBuiltLineEntity`, `new JournalEntryDomain()`, `JournalEntryDomain.isYearEndGenerated`, `JournalEntryDomain.checkTargetPeriodOpen`, `FiscalYearService.AccountBalance`, `JournalEntryMapper.toLineDimensionResponse`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-ACCOUNT` | FIN_404_ACCOUNT | FiscalYearService.retainedEarningsAccount |
| 404 NOT_FOUND | `FIN-404-PERIOD` | FIN_404_PERIOD | FiscalYearService.firstPeriodOf |
| 404 NOT_FOUND | `FIN-404-PERIOD` | FIN_404_PERIOD | FiscalYearService.lastPeriodOf |
| 404 NOT_FOUND | `FIN-404-YEAR` | FIN_404_YEAR | FiscalYearService.lockOrThrow |
| 404 NOT_FOUND | `FIN-404-YEAR` | FIN_404_YEAR | FiscalYearService.successorOf |
| 404 NOT_FOUND | `FIN-404-YEAR` | FIN_404_YEAR | JournalPostingService.buildValidateAndPost |
| 409 CONFLICT | `FIN-409-DUPLICATE-EVENT` | FIN_409_DUPLICATE_EVENT | JournalEntryDomain.create |
| 409 CONFLICT | `FIN-409-INVALID-TRANSITION` | FIN_409_INVALID_TRANSITION | FiscalYearDomain.assertCanYearEndClose |
| 409 CONFLICT | `FIN-409-PERIODS-NOT-CLOSED` | FIN_409_PERIODS_NOT_CLOSED | FiscalPeriodDomain.assertHardClosedForYearEnd |
| 409 CONFLICT | `FIN-409-PERIOD-NOT-OPEN` | FIN_409_PERIOD_NOT_OPEN | JournalEntryDomain.checkTargetPeriodOpen — as a `fieldErrors[]` entry |
| 409 CONFLICT | `FIN-409-UNBALANCED` | FIN_409_UNBALANCED | JournalEntryDomain.checkBalanced — as a `fieldErrors[]` entry |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

<!-- fetched-from: endpoints/fin-journal-entry-management.md -->
<!-- AUTO-GENERATED by api-doc-generator — do not edit manually -->
# FIN Journal Entry Management

**Endpoints in this file:**

- `API-FIN-019` — [POST /api/v1/fin/journal-entries](#post-apiv1finjournal-entries)
- `API-FIN-020` — [POST /api/v1/fin/journal-entries/from-event](#post-apiv1finjournal-entriesfrom-event)
- `API-FIN-018` — [POST /api/v1/fin/journal-entries/search](#post-apiv1finjournal-entriessearch)
- `API-FIN-022` — [GET /api/v1/fin/journal-entries/{id}](#get-apiv1finjournal-entriesid)
- `API-FIN-021` — [POST /api/v1/fin/journal-entries/{id}/reverse](#post-apiv1finjournal-entriesidreverse)

## POST /api/v1/fin/journal-entries

**Create manual journal entry**

إنشاء وترحيل قيد يومية يدوي

Contract ID: `API-FIN-019`

Operation ID: `createManual`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_JOURNAL_ENTRIES_CREATE (found on service:JournalEntryService)

### Request Body

Schema: `JournalEntryCreateRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| docDate | string (date) | Yes |  | Document date - تاريخ المستند | 2026-01-31 |
| fiscalYearId | integer (int64) | Yes |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periodId | integer (int64) | Yes |  | Target fiscal period id - معرّف الفترة المالية | 1 |
| journalTypeCode | string | Yes | maxLength: 20 | Journal type, JOURNAL_TYPE lookup - نوع اليومية | MANUAL |
| descriptionAr | string | No |  | Entry description (Arabic) - وصف القيد بالعربية | قيد إهلاك يناير |
| descriptionEn | string | No |  | Entry description (English) - وصف القيد بالإنجليزية | January depreciation entry |
| lines | array<JournalLineCreateRequest> | Yes |  | Entry lines - سطور القيد — Journal entry line - سطر قيد يومية |  |
| lines[].accountId | integer (int64) | Yes |  | Target account id - معرّف الحساب | 12 |
| lines[].amount | number | Yes |  | Line amount, always positive - المبلغ | 1500.0 |
| lines[].directionCode | string | Yes | maxLength: 10 | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].descriptionAr | string | No |  | Line description (Arabic) - وصف السطر بالعربية | إهلاك |
| lines[].descriptionEn | string | No |  | Line description (English) - وصف السطر بالإنجليزية | Depreciation |
| lines[].dimensions | array<JournalLineDimensionCreateRequest> | No |  | Analysis dimension tags - الأبعاد التحليلية — Journal line dimension tag - بُعد تحليلي لسطر القيد |  |
| lines[].dimensions[].dimensionId | integer (int64) | Yes |  | Dimension id the value is claimed to belong to - معرّف البُعد | 1 |
| lines[].dimensions[].dimensionValueId | integer (int64) | Yes |  | Dimension value id - معرّف قيمة البُعد | 5 |

**Request Example**

```json
{
  "docDate": "2026-01-31",
  "fiscalYearId": 1,
  "periodId": 1,
  "journalTypeCode": "MANUAL",
  "descriptionAr": "قيد إهلاك يناير",
  "descriptionEn": "January depreciation entry",
  "lines": [
    {
      "accountId": 12,
      "amount": 1500.0,
      "directionCode": "DEBIT",
      "descriptionAr": "إهلاك",
      "descriptionEn": "Depreciation",
      "dimensions": [
        {
          "dimensionId": 1,
          "dimensionValueId": 5
        }
      ]
    }
  ]
}
```

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `JournalEntryService.createManual`.

Shape: `JournalEntryResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| journalEntryPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| docNo | string | No |  | System-generated document number - رقم المستند | JV-2026-000123 |
| docDate | string (date) | No |  | Document date - تاريخ المستند | 2026-01-31 |
| fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periodId | integer (int64) | No |  | Target fiscal period id - معرّف الفترة المالية | 1 |
| journalTypeCode | string | No |  | Journal type, JOURNAL_TYPE lookup - نوع اليومية | MANUAL |
| statusCode | string | No |  | Status, JOURNAL_STATUS lookup - الحالة | POSTED |
| eventReference | string | No |  | Source event reference, for event-built entries - مرجع الحدث | INV-2026-0001 |
| originalEntryId | integer (int64) | No |  | Original entry id, when this entry is a reversal - معرّف القيد الأصلي | 7 |
| reversalEntryId | integer (int64) | No |  | Reversal entry id, when this entry has been reversed - معرّف قيد العكس | 9 |
| descriptionAr | string | No |  | Entry description (Arabic) - وصف القيد بالعربية | قيد إهلاك يناير |
| descriptionEn | string | No |  | Entry description (English) - وصف القيد بالإنجليزية | January depreciation entry |
| postedAt | string (date-time) | No |  | Posting timestamp - تاريخ الترحيل |  |
| lineCount | integer (int32) | No |  | Number of entry lines - عدد السطور | 2 |
| lines | array<JournalLineResponse> | No |  | Entry lines - سطور القيد — Journal entry line - سطر قيد يومية |  |
| lines[].journalLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].journalEntryId | integer (int64) | No |  | Parent journal entry id - معرّف القيد الأب | 1 |
| lines[].lineNo | integer (int32) | No |  | Line position within the entry - رقم السطر | 1 |
| lines[].accountId | integer (int64) | No |  | Target account id - معرّف الحساب | 12 |
| lines[].amount | number | No |  | Line amount, always positive - المبلغ | 1500.0 |
| lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| lines[].descriptionAr | string | No |  | Line description (Arabic) - وصف السطر بالعربية | إهلاك |
| lines[].descriptionEn | string | No |  | Line description (English) - وصف السطر بالإنجليزية | Depreciation |
| lines[].dimensions | array<JournalLineDimensionResponse> | No |  | Analysis dimension tags - الأبعاد التحليلية — Journal line dimension tag - بُعد تحليلي لسطر القيد |  |
| lines[].dimensions[].journalLineDimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].dimensions[].journalLineId | integer (int64) | No |  | Parent journal line id - معرّف سطر القيد الأب | 1 |
| lines[].dimensions[].dimensionId | integer (int64) | No |  | Dimension id - معرّف البُعد | 1 |
| lines[].dimensions[].dimensionValueId | integer (int64) | No |  | Dimension value id - معرّف قيمة البُعد | 5 |
| lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "journalEntryPk": 1,
  "docNo": "JV-2026-000123",
  "docDate": "2026-01-31",
  "fiscalYearId": 1,
  "periodId": 1,
  "journalTypeCode": "MANUAL",
  "statusCode": "POSTED",
  "eventReference": "INV-2026-0001",
  "originalEntryId": 7,
  "reversalEntryId": 9,
  "descriptionAr": "قيد إهلاك يناير",
  "descriptionEn": "January depreciation entry",
  "lineCount": 2,
  "lines": [
    {
      "journalLinePk": 1,
      "journalEntryId": 1,
      "lineNo": 1,
      "accountId": 12,
      "amount": 1500.0,
      "directionCode": "DEBIT",
      "isRemainderFl": false,
      "descriptionAr": "إهلاك",
      "descriptionEn": "Depreciation",
      "dimensions": [
        {
          "journalLineDimensionPk": 1,
          "journalLineId": 1,
          "dimensionId": 1,
          "dimensionValueId": 5
        }
      ]
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `JournalEntryController.createManual`, `JournalEntryService.createManual`, `JournalEntryDomain.assertHeaderCoherent`, `FinLookupValidationService.assertValidCode`, `JournalEntryService.resolveLines`, `JournalDocNoGenerator.next`, `JournalEntryService.highestDocNoOf`, `JournalEntryMapper.toEntity`, `JournalEntryDomain.create`, `JournalEntryDomain.checkBalanced`, `JournalEntryDomain.checkTargetPeriodOpen`, `JournalEntry.post`, `JournalEntryMapper.toResponse`, `JournalEntryMapper.toLineResponse`, `AccountDomain.from`, `JournalEntryService.resolveDimensions`, `AccountDomain.checkPostable`, `JournalDocNoGenerator.format`, `JournalDocNoGenerator.counterOf`, `JournalEntryMapper.toLineEntity`, `new JournalEntryDomain()`, `JournalEntryMapper.toLineDimensionResponse`, `new AccountDomain()`, `DimensionValueDomain.from`, `DimensionValueDomain.checkUsableOnLine`, `new DimensionValueDomain()`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-DOCDATE-OUTSIDE-PERIOD` | FIN_400_DOCDATE_OUTSIDE_PERIOD | JournalEntryDomain.assertHeaderCoherent |
| 400 BAD_REQUEST | `FIN-400-INVALID-LOOKUP` | FIN_400_INVALID_LOOKUP | FinLookupValidationService.assertValidCode |
| 400 BAD_REQUEST | `FIN-400-PERIOD-NOT-IN-YEAR` | FIN_400_PERIOD_NOT_IN_YEAR | JournalEntryDomain.assertHeaderCoherent |
| 404 NOT_FOUND | `FIN-404-ACCOUNT` | FIN_404_ACCOUNT | JournalEntryService.resolveLines |
| 404 NOT_FOUND | `FIN-404-PERIOD` | FIN_404_PERIOD | JournalEntryService.createManual |
| 404 NOT_FOUND | `FIN-404-YEAR` | FIN_404_YEAR | JournalEntryService.createManual |
| 409 CONFLICT | `FIN-409-DUPLICATE-EVENT` | FIN_409_DUPLICATE_EVENT | JournalEntryDomain.create |
| 409 CONFLICT | `FIN-409-INVALID-DIMENSION` | FIN_409_INVALID_DIMENSION | DimensionValueDomain.checkUsableOnLine — as a `fieldErrors[]` entry |
| 409 CONFLICT | `FIN-409-INVALID-DIMENSION` | FIN_409_INVALID_DIMENSION | JournalEntryService.resolveDimensions |
| 409 CONFLICT | `FIN-409-NOT-POSTABLE-ACCOUNT` | FIN_409_NOT_POSTABLE_ACCOUNT | AccountDomain.checkPostable — as a `fieldErrors[]` entry |
| 409 CONFLICT | `FIN-409-PERIOD-NOT-OPEN` | FIN_409_PERIOD_NOT_OPEN | JournalEntryDomain.checkTargetPeriodOpen — as a `fieldErrors[]` entry |
| 409 CONFLICT | `FIN-409-UNBALANCED` | FIN_409_UNBALANCED | JournalEntryDomain.checkBalanced — as a `fieldErrors[]` entry |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## POST /api/v1/fin/journal-entries/from-event

**Build and post a journal entry from an accounting event**

بناء وترحيل قيد يومية من حدث محاسبي. The consumer branches on the full error code, never on the HTTP status alone. FIN-409-DUPLICATE-EVENT: already posted, acknowledge the message. FIN-409-PERIOD-NOT-OPEN: retry later, the rejection clears once the target period is opened (only a SOFT_CLOSE period can return to OPEN, so a retry policy needs an upper bound). Every other code listed below: needs a fix, mark the message failed (FIN-422-UNMAPPED-VALUE add a mapping, FIN-422-MISSING-BUSINESS-FIELD fix the producer, FIN-422-UNRESOLVED-DIMENSION-VALUE define the dimension value, FIN-422-NO-DEFINED-PERIOD define the calendar).

Contract ID: `API-FIN-020`

Operation ID: `buildFromEvent`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_JOURNAL_ENTRIES_CREATE (found on service:EventEntryService)

### Request Body

Schema: `EventEntryBuildRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| eventReference | string | Yes | maxLength: 100 | Event idempotency reference - المرجع الفريد للحدث | SALES-INV-2026-000811 |
| eventTypeCode | string | Yes | maxLength: 50 | Event type, ACCOUNTING_EVENT_TYPE lookup - نوع الحدث المحاسبي | SALES_INVOICE |
| docDate | string (date) | Yes |  | Document date; selects the target fiscal period - تاريخ المستند | 2026-01-31 |
| baseAmount | number | Yes |  | Event base amount the rule distributes - المبلغ الأساسي للحدث | 1000.0 |
| amounts | object | No |  | Named amount fields of the event - حقول المبالغ المسماة للحدث |  |
| fields | object | No |  | Business facts of the event, keyed by FIN_EVENT_BUSINESS_FIELD code (PAYMENT_METHOD, EXPENSE_TYPE_CODE, ORGANISATION_CODE, BRANCH_CODE); the key set grows as MDL data, not as a new request field. No account code is read from the payload - الحقائق التجارية للحدث مفتاحها رمز FIN_EVENT_BUSINESS_FIELD |  |
| descriptionAr | string | No |  | Entry description (Arabic) - وصف القيد بالعربية | فاتورة مبيعات |
| descriptionEn | string | No |  | Entry description (English) - وصف القيد بالإنجليزية | Sales invoice |

**Request Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "eventReference": "SALES-INV-2026-000811",
  "eventTypeCode": "SALES_INVOICE",
  "docDate": "2026-01-31",
  "baseAmount": 1000.0,
  "descriptionAr": "فاتورة مبيعات",
  "descriptionEn": "Sales invoice"
}
```

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `EventEntryService.build`.

Shape: `JournalEntryResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| journalEntryPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| docNo | string | No |  | System-generated document number - رقم المستند | JV-2026-000123 |
| docDate | string (date) | No |  | Document date - تاريخ المستند | 2026-01-31 |
| fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periodId | integer (int64) | No |  | Target fiscal period id - معرّف الفترة المالية | 1 |
| journalTypeCode | string | No |  | Journal type, JOURNAL_TYPE lookup - نوع اليومية | MANUAL |
| statusCode | string | No |  | Status, JOURNAL_STATUS lookup - الحالة | POSTED |
| eventReference | string | No |  | Source event reference, for event-built entries - مرجع الحدث | INV-2026-0001 |
| originalEntryId | integer (int64) | No |  | Original entry id, when this entry is a reversal - معرّف القيد الأصلي | 7 |
| reversalEntryId | integer (int64) | No |  | Reversal entry id, when this entry has been reversed - معرّف قيد العكس | 9 |
| descriptionAr | string | No |  | Entry description (Arabic) - وصف القيد بالعربية | قيد إهلاك يناير |
| descriptionEn | string | No |  | Entry description (English) - وصف القيد بالإنجليزية | January depreciation entry |
| postedAt | string (date-time) | No |  | Posting timestamp - تاريخ الترحيل |  |
| lineCount | integer (int32) | No |  | Number of entry lines - عدد السطور | 2 |
| lines | array<JournalLineResponse> | No |  | Entry lines - سطور القيد — Journal entry line - سطر قيد يومية |  |
| lines[].journalLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].journalEntryId | integer (int64) | No |  | Parent journal entry id - معرّف القيد الأب | 1 |
| lines[].lineNo | integer (int32) | No |  | Line position within the entry - رقم السطر | 1 |
| lines[].accountId | integer (int64) | No |  | Target account id - معرّف الحساب | 12 |
| lines[].amount | number | No |  | Line amount, always positive - المبلغ | 1500.0 |
| lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| lines[].descriptionAr | string | No |  | Line description (Arabic) - وصف السطر بالعربية | إهلاك |
| lines[].descriptionEn | string | No |  | Line description (English) - وصف السطر بالإنجليزية | Depreciation |
| lines[].dimensions | array<JournalLineDimensionResponse> | No |  | Analysis dimension tags - الأبعاد التحليلية — Journal line dimension tag - بُعد تحليلي لسطر القيد |  |
| lines[].dimensions[].journalLineDimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].dimensions[].journalLineId | integer (int64) | No |  | Parent journal line id - معرّف سطر القيد الأب | 1 |
| lines[].dimensions[].dimensionId | integer (int64) | No |  | Dimension id - معرّف البُعد | 1 |
| lines[].dimensions[].dimensionValueId | integer (int64) | No |  | Dimension value id - معرّف قيمة البُعد | 5 |
| lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "journalEntryPk": 1,
  "docNo": "JV-2026-000123",
  "docDate": "2026-01-31",
  "fiscalYearId": 1,
  "periodId": 1,
  "journalTypeCode": "MANUAL",
  "statusCode": "POSTED",
  "eventReference": "INV-2026-0001",
  "originalEntryId": 7,
  "reversalEntryId": 9,
  "descriptionAr": "قيد إهلاك يناير",
  "descriptionEn": "January depreciation entry",
  "lineCount": 2,
  "lines": [
    {
      "journalLinePk": 1,
      "journalEntryId": 1,
      "lineNo": 1,
      "accountId": 12,
      "amount": 1500.0,
      "directionCode": "DEBIT",
      "isRemainderFl": false,
      "descriptionAr": "إهلاك",
      "descriptionEn": "Depreciation",
      "dimensions": [
        {
          "journalLineDimensionPk": 1,
          "journalLineId": 1,
          "dimensionId": 1,
          "dimensionValueId": 5
        }
      ]
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `JournalEntryController.buildFromEvent`, `EventEntryService.build`, `JournalEntryDomain.create`, `FiscalPeriodDomain.assertDateCovered`, `EventTypeRuleDomain.from`, `EventEntryService.tagsByLine`, `JournalPostingService.buildValidateAndPost`, `EventEntryService.buildLines`, `JournalEntryMapper.toResponse`, `EventTypeRuleDomain.assertRemainderLineSetValid`, `JournalEntryMapper.toLineResponse`, `new JournalEntryDomain()`, `FiscalPeriodDomain.from`, `new EventTypeRuleDomain()`, `AccountDomain.from`, `DimensionValueDomain.from`, `JournalDocNoGenerator.next`, `JournalPostingService.highestDocNoOf`, `JournalEntryMapper.toEntity`, `JournalEntryDomain.checkBalanced`, `JournalEntryDomain.checkTargetPeriodOpenForGeneratedEntry`, `JournalEntry.post`, `EventEntryService.resolveAccount`, `EventTypeRuleDomain.isRemainderLine`, `EventTypeRuleDomain.sourcedAmount`, `EventTypeRuleDomain.remainderAmount`, `EventEntryService.resolveDimensions`, `EventTypeRuleDomain.declaresRemainder`, `JournalEntryMapper.toLineDimensionResponse`, `new FiscalPeriodDomain()`, `new AccountDomain()`, `new DimensionValueDomain()`, `JournalDocNoGenerator.format`, `JournalDocNoGenerator.counterOf`, `JournalEntryMapper.toBuiltLineEntity`, `JournalEntryDomain.isYearEndGenerated`, `JournalEntryDomain.checkTargetPeriodOpen`, `EventTypeRuleDomain.isMappingDerivation`, `EventEntryService.resolveAccountByCode`, `EventTypeRuleDomain.assertFieldPresent`, `AccountMappingDomain.assertResolved`, `EventEntryService.resolveDimensionValue`, `EventTypeRuleDomain.hasText`, `AccountMappingDomain.from`, `AccountMappingDomain.isActive`, `EventTypeRuleDomain.isBusinessFieldSource`, `DimensionValueDomain.assertResolvedByCode`, `new AccountMappingDomain()`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-ACCOUNT` | FIN_404_ACCOUNT | EventEntryService.resolveAccountByCode |
| 404 NOT_FOUND | `FIN-404-NO-ACTIVE-RULE` | FIN_404_NO_ACTIVE_RULE | EventEntryService.build |
| 404 NOT_FOUND | `FIN-404-YEAR` | FIN_404_YEAR | JournalPostingService.buildValidateAndPost |
| 409 CONFLICT | `FIN-409-DUPLICATE-EVENT` | FIN_409_DUPLICATE_EVENT | JournalEntryDomain.create |
| 409 CONFLICT | `FIN-409-PERIOD-NOT-OPEN` | FIN_409_PERIOD_NOT_OPEN | JournalEntryDomain.checkTargetPeriodOpen — as a `fieldErrors[]` entry |
| 409 CONFLICT | `FIN-409-REMAINDER-COUNT` | FIN_409_REMAINDER_COUNT | EventTypeRuleDomain.assertRemainderLineSetValid |
| 409 CONFLICT | `FIN-409-UNBALANCED` | FIN_409_UNBALANCED | JournalEntryDomain.checkBalanced — as a `fieldErrors[]` entry |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-INVALID-PERCENTAGE-VALUE` | FIN_422_INVALID_PERCENTAGE_VALUE | EventTypeRuleDomain.sourcedAmount |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-MISSING-BUSINESS-FIELD` | FIN_422_MISSING_BUSINESS_FIELD | EventTypeRuleDomain.assertFieldPresent |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-NO-DEFINED-PERIOD` | FIN_422_NO_DEFINED_PERIOD | FiscalPeriodDomain.assertDateCovered |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-REMAINDER-MARKER` | FIN_422_REMAINDER_MARKER | EventTypeRuleDomain.assertRemainderLineSetValid |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-REMAINDER-MARKER` | FIN_422_REMAINDER_MARKER | EventTypeRuleDomain.sourcedAmount |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-REMAINDER-NOT-POSITIVE` | FIN_422_REMAINDER_NOT_POSITIVE | EventTypeRuleDomain.remainderAmount |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-UNMAPPED-VALUE` | FIN_422_UNMAPPED_VALUE | AccountMappingDomain.assertResolved |
| 422 UNPROCESSABLE_CONTENT | `FIN-422-UNRESOLVED-DIMENSION-VALUE` | FIN_422_UNRESOLVED_DIMENSION_VALUE | DimensionValueDomain.assertResolvedByCode |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## POST /api/v1/fin/journal-entries/search

**Search journal entries**

بحث في قيود اليومية

Contract ID: `API-FIN-018`

Operation ID: `search_1`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_JOURNAL_ENTRIES_VIEW (found on service:JournalEntryService)

### Request Body

Schema: `JournalEntrySearchRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| filters | array<SearchFilter> | No |  | Filter criteria. Supported fields: docNo (LIKE), docDate (EQUALS, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL - ISO yyyy-MM-dd, send both bounds for a range), periodId (EQUALS), fiscalYearId (EQUALS), statusCode (EQUALS, IN - JOURNAL_STATUS), journalTypeCode (EQUALS, IN - JOURNAL_TYPE), eventReference (EQUALS), journalEntryPk (EQUALS, IN), postedAt / createdAt (comparison operators). Any other field, or an operator a field does not list, is rejected as 400 VALIDATION_ERROR naming it - معايير التصفية |  |
| filters[].field | string | No |  |  |  |
| filters[].operator | string | No | enum: EQUALS, NOT_EQUALS, LIKE, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, IN |  |  |
| filters[].value | object | No |  |  |  |
| sortField | string | No |  | Sort field. Supported: journalEntryPk, docNo, docDate, journalTypeCode, statusCode, eventReference, postedAt, createdAt. Any other value is rejected as 400 FIN-400-INVALID-SORT - حقل الترتيب |  |
| sortDirection | string | No | enum: ASC, DESC | Sort direction - اتجاه الترتيب |  |
| page | integer (int32) | No |  | Page number, zero-based - رقم الصفحة | 0 |
| size | integer (int32) | No |  | Page size - حجم الصفحة | 20 |

**Request Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "page": 0,
  "size": 20
}
```

### Response `200` — OK

Shape: `paginated list of JournalEntryResponse (see Pagination Envelope in index.md)`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| journalEntryPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| docNo | string | No |  | System-generated document number - رقم المستند | JV-2026-000123 |
| docDate | string (date) | No |  | Document date - تاريخ المستند | 2026-01-31 |
| fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periodId | integer (int64) | No |  | Target fiscal period id - معرّف الفترة المالية | 1 |
| journalTypeCode | string | No |  | Journal type, JOURNAL_TYPE lookup - نوع اليومية | MANUAL |
| statusCode | string | No |  | Status, JOURNAL_STATUS lookup - الحالة | POSTED |
| eventReference | string | No |  | Source event reference, for event-built entries - مرجع الحدث | INV-2026-0001 |
| originalEntryId | integer (int64) | No |  | Original entry id, when this entry is a reversal - معرّف القيد الأصلي | 7 |
| reversalEntryId | integer (int64) | No |  | Reversal entry id, when this entry has been reversed - معرّف قيد العكس | 9 |
| descriptionAr | string | No |  | Entry description (Arabic) - وصف القيد بالعربية | قيد إهلاك يناير |
| descriptionEn | string | No |  | Entry description (English) - وصف القيد بالإنجليزية | January depreciation entry |
| postedAt | string (date-time) | No |  | Posting timestamp - تاريخ الترحيل |  |
| lineCount | integer (int32) | No |  | Number of entry lines - عدد السطور | 2 |
| lines | array<JournalLineResponse> | No |  | Entry lines - سطور القيد — Journal entry line - سطر قيد يومية |  |
| lines[].journalLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].journalEntryId | integer (int64) | No |  | Parent journal entry id - معرّف القيد الأب | 1 |
| lines[].lineNo | integer (int32) | No |  | Line position within the entry - رقم السطر | 1 |
| lines[].accountId | integer (int64) | No |  | Target account id - معرّف الحساب | 12 |
| lines[].amount | number | No |  | Line amount, always positive - المبلغ | 1500.0 |
| lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| lines[].descriptionAr | string | No |  | Line description (Arabic) - وصف السطر بالعربية | إهلاك |
| lines[].descriptionEn | string | No |  | Line description (English) - وصف السطر بالإنجليزية | Depreciation |
| lines[].dimensions | array<JournalLineDimensionResponse> | No |  | Analysis dimension tags - الأبعاد التحليلية — Journal line dimension tag - بُعد تحليلي لسطر القيد |  |
| lines[].dimensions[].journalLineDimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].dimensions[].journalLineId | integer (int64) | No |  | Parent journal line id - معرّف سطر القيد الأب | 1 |
| lines[].dimensions[].dimensionId | integer (int64) | No |  | Dimension id - معرّف البُعد | 1 |
| lines[].dimensions[].dimensionValueId | integer (int64) | No |  | Dimension value id - معرّف قيمة البُعد | 5 |
| lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "journalEntryPk": 1,
  "docNo": "JV-2026-000123",
  "docDate": "2026-01-31",
  "fiscalYearId": 1,
  "periodId": 1,
  "journalTypeCode": "MANUAL",
  "statusCode": "POSTED",
  "eventReference": "INV-2026-0001",
  "originalEntryId": 7,
  "reversalEntryId": 9,
  "descriptionAr": "قيد إهلاك يناير",
  "descriptionEn": "January depreciation entry",
  "lineCount": 2,
  "lines": [
    {
      "journalLinePk": 1,
      "journalEntryId": 1,
      "lineNo": 1,
      "accountId": 12,
      "amount": 1500.0,
      "directionCode": "DEBIT",
      "isRemainderFl": false,
      "descriptionAr": "إهلاك",
      "descriptionEn": "Depreciation",
      "dimensions": [
        {
          "journalLineDimensionPk": 1,
          "journalLineId": 1,
          "dimensionId": 1,
          "dimensionValueId": 5
        }
      ]
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `JournalEntryController.search`, `JournalEntryService.search`, `FinSearchSupport.assertSortAllowed`, `JournalEntrySearchRequest.getSortField`, `JournalEntrySearchRequest.toCommonSearchRequest`, `FinSearchSupport.temporalFieldConverter`, `JournalEntrySearchRequest.getPeriodId`, `JournalEntrySearchRequest.getFiscalYearId`, `JournalEntryMapper.toSummaryResponse`, `FinSearchSupport.parseLocalDate`, `FinSearchSupport.parseInstant`, `JournalEntryMapper.toResponse`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-SORT` | FIN_400_INVALID_SORT | FinSearchSupport.assertSortAllowed |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseInstant |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseLocalDate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## GET /api/v1/fin/journal-entries/{id}

**Read a journal entry with its lines**

عرض قيد يومية مع سطوره وأبعاده

Contract ID: `API-FIN-022`

Operation ID: `read`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_JOURNAL_ENTRIES_VIEW (found on service:JournalEntryService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `200` — OK

Shape: `JournalEntryResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| journalEntryPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| docNo | string | No |  | System-generated document number - رقم المستند | JV-2026-000123 |
| docDate | string (date) | No |  | Document date - تاريخ المستند | 2026-01-31 |
| fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periodId | integer (int64) | No |  | Target fiscal period id - معرّف الفترة المالية | 1 |
| journalTypeCode | string | No |  | Journal type, JOURNAL_TYPE lookup - نوع اليومية | MANUAL |
| statusCode | string | No |  | Status, JOURNAL_STATUS lookup - الحالة | POSTED |
| eventReference | string | No |  | Source event reference, for event-built entries - مرجع الحدث | INV-2026-0001 |
| originalEntryId | integer (int64) | No |  | Original entry id, when this entry is a reversal - معرّف القيد الأصلي | 7 |
| reversalEntryId | integer (int64) | No |  | Reversal entry id, when this entry has been reversed - معرّف قيد العكس | 9 |
| descriptionAr | string | No |  | Entry description (Arabic) - وصف القيد بالعربية | قيد إهلاك يناير |
| descriptionEn | string | No |  | Entry description (English) - وصف القيد بالإنجليزية | January depreciation entry |
| postedAt | string (date-time) | No |  | Posting timestamp - تاريخ الترحيل |  |
| lineCount | integer (int32) | No |  | Number of entry lines - عدد السطور | 2 |
| lines | array<JournalLineResponse> | No |  | Entry lines - سطور القيد — Journal entry line - سطر قيد يومية |  |
| lines[].journalLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].journalEntryId | integer (int64) | No |  | Parent journal entry id - معرّف القيد الأب | 1 |
| lines[].lineNo | integer (int32) | No |  | Line position within the entry - رقم السطر | 1 |
| lines[].accountId | integer (int64) | No |  | Target account id - معرّف الحساب | 12 |
| lines[].amount | number | No |  | Line amount, always positive - المبلغ | 1500.0 |
| lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| lines[].descriptionAr | string | No |  | Line description (Arabic) - وصف السطر بالعربية | إهلاك |
| lines[].descriptionEn | string | No |  | Line description (English) - وصف السطر بالإنجليزية | Depreciation |
| lines[].dimensions | array<JournalLineDimensionResponse> | No |  | Analysis dimension tags - الأبعاد التحليلية — Journal line dimension tag - بُعد تحليلي لسطر القيد |  |
| lines[].dimensions[].journalLineDimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].dimensions[].journalLineId | integer (int64) | No |  | Parent journal line id - معرّف سطر القيد الأب | 1 |
| lines[].dimensions[].dimensionId | integer (int64) | No |  | Dimension id - معرّف البُعد | 1 |
| lines[].dimensions[].dimensionValueId | integer (int64) | No |  | Dimension value id - معرّف قيمة البُعد | 5 |
| lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "journalEntryPk": 1,
  "docNo": "JV-2026-000123",
  "docDate": "2026-01-31",
  "fiscalYearId": 1,
  "periodId": 1,
  "journalTypeCode": "MANUAL",
  "statusCode": "POSTED",
  "eventReference": "INV-2026-0001",
  "originalEntryId": 7,
  "reversalEntryId": 9,
  "descriptionAr": "قيد إهلاك يناير",
  "descriptionEn": "January depreciation entry",
  "lineCount": 2,
  "lines": [
    {
      "journalLinePk": 1,
      "journalEntryId": 1,
      "lineNo": 1,
      "accountId": 12,
      "amount": 1500.0,
      "directionCode": "DEBIT",
      "isRemainderFl": false,
      "descriptionAr": "إهلاك",
      "descriptionEn": "Depreciation",
      "dimensions": [
        {
          "journalLineDimensionPk": 1,
          "journalLineId": 1,
          "dimensionId": 1,
          "dimensionValueId": 5
        }
      ]
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `JournalEntryController.read`, `JournalEntryService.read`, `JournalEntryMapper.toResponse`, `JournalEntryMapper.toLineResponse`, `JournalEntryMapper.toLineDimensionResponse`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-ENTRY` | FIN_404_ENTRY | JournalEntryService.read |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

## POST /api/v1/fin/journal-entries/{id}/reverse

**Reverse a posted journal entry**

عكس قيد يومية مُرحَّل

Contract ID: `API-FIN-021`

Operation ID: `reverse`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_JOURNAL_ENTRIES_REVERSE (found on service:JournalEntryService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `JournalEntryService.reverse`.

Shape: `JournalEntryResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| journalEntryPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| docNo | string | No |  | System-generated document number - رقم المستند | JV-2026-000123 |
| docDate | string (date) | No |  | Document date - تاريخ المستند | 2026-01-31 |
| fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periodId | integer (int64) | No |  | Target fiscal period id - معرّف الفترة المالية | 1 |
| journalTypeCode | string | No |  | Journal type, JOURNAL_TYPE lookup - نوع اليومية | MANUAL |
| statusCode | string | No |  | Status, JOURNAL_STATUS lookup - الحالة | POSTED |
| eventReference | string | No |  | Source event reference, for event-built entries - مرجع الحدث | INV-2026-0001 |
| originalEntryId | integer (int64) | No |  | Original entry id, when this entry is a reversal - معرّف القيد الأصلي | 7 |
| reversalEntryId | integer (int64) | No |  | Reversal entry id, when this entry has been reversed - معرّف قيد العكس | 9 |
| descriptionAr | string | No |  | Entry description (Arabic) - وصف القيد بالعربية | قيد إهلاك يناير |
| descriptionEn | string | No |  | Entry description (English) - وصف القيد بالإنجليزية | January depreciation entry |
| postedAt | string (date-time) | No |  | Posting timestamp - تاريخ الترحيل |  |
| lineCount | integer (int32) | No |  | Number of entry lines - عدد السطور | 2 |
| lines | array<JournalLineResponse> | No |  | Entry lines - سطور القيد — Journal entry line - سطر قيد يومية |  |
| lines[].journalLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].journalEntryId | integer (int64) | No |  | Parent journal entry id - معرّف القيد الأب | 1 |
| lines[].lineNo | integer (int32) | No |  | Line position within the entry - رقم السطر | 1 |
| lines[].accountId | integer (int64) | No |  | Target account id - معرّف الحساب | 12 |
| lines[].amount | number | No |  | Line amount, always positive - المبلغ | 1500.0 |
| lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| lines[].descriptionAr | string | No |  | Line description (Arabic) - وصف السطر بالعربية | إهلاك |
| lines[].descriptionEn | string | No |  | Line description (English) - وصف السطر بالإنجليزية | Depreciation |
| lines[].dimensions | array<JournalLineDimensionResponse> | No |  | Analysis dimension tags - الأبعاد التحليلية — Journal line dimension tag - بُعد تحليلي لسطر القيد |  |
| lines[].dimensions[].journalLineDimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].dimensions[].journalLineId | integer (int64) | No |  | Parent journal line id - معرّف سطر القيد الأب | 1 |
| lines[].dimensions[].dimensionId | integer (int64) | No |  | Dimension id - معرّف البُعد | 1 |
| lines[].dimensions[].dimensionValueId | integer (int64) | No |  | Dimension value id - معرّف قيمة البُعد | 5 |
| lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "journalEntryPk": 1,
  "docNo": "JV-2026-000123",
  "docDate": "2026-01-31",
  "fiscalYearId": 1,
  "periodId": 1,
  "journalTypeCode": "MANUAL",
  "statusCode": "POSTED",
  "eventReference": "INV-2026-0001",
  "originalEntryId": 7,
  "reversalEntryId": 9,
  "descriptionAr": "قيد إهلاك يناير",
  "descriptionEn": "January depreciation entry",
  "lineCount": 2,
  "lines": [
    {
      "journalLinePk": 1,
      "journalEntryId": 1,
      "lineNo": 1,
      "accountId": 12,
      "amount": 1500.0,
      "directionCode": "DEBIT",
      "isRemainderFl": false,
      "descriptionAr": "إهلاك",
      "descriptionEn": "Depreciation",
      "dimensions": [
        {
          "journalLineDimensionPk": 1,
          "journalLineId": 1,
          "dimensionId": 1,
          "dimensionValueId": 5
        }
      ]
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `JournalEntryController.reverse`, `JournalEntryService.reverse`, `JournalEntryDomain.from`, `JournalPostingService.findCurrentOpenPeriod`, `JournalEntryDomain.resolveReversalPeriodPk`, `JournalPostingService.buildValidateAndPost`, `JournalEntryService.mirroredLines`, `JournalEntry.linkOriginal`, `JournalEntry.linkReversal`, `JournalEntryMapper.toResponse`, `JournalEntryDomain.assertCanReverse`, `JournalEntryMapper.toLineResponse`, `new JournalEntryDomain()`, `AccountDomain.from`, `DimensionValueDomain.from`, `JournalDocNoGenerator.next`, `JournalPostingService.highestDocNoOf`, `JournalEntryMapper.toEntity`, `JournalEntryDomain.create`, `JournalEntryDomain.checkBalanced`, `JournalEntryDomain.checkTargetPeriodOpenForGeneratedEntry`, `JournalEntry.post`, `JournalEntryMapper.BuiltDimension`, `JournalEntryMapper.BuiltLine`, `JournalEntryDomain.reversalDirectionOf`, `JournalEntryMapper.toLineDimensionResponse`, `new AccountDomain()`, `new DimensionValueDomain()`, `JournalDocNoGenerator.format`, `JournalDocNoGenerator.counterOf`, `JournalEntryMapper.toBuiltLineEntity`, `JournalEntryDomain.isYearEndGenerated`, `JournalEntryDomain.checkTargetPeriodOpen`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-ENTRY` | FIN_404_ENTRY | JournalEntryService.reverse |
| 404 NOT_FOUND | `FIN-404-YEAR` | FIN_404_YEAR | JournalPostingService.buildValidateAndPost |
| 409 CONFLICT | `FIN-409-ALREADY-REVERSED` | FIN_409_ALREADY_REVERSED | JournalEntryDomain.assertCanReverse |
| 409 CONFLICT | `FIN-409-DUPLICATE-EVENT` | FIN_409_DUPLICATE_EVENT | JournalEntryDomain.create |
| 409 CONFLICT | `FIN-409-NOT-POSTED` | FIN_409_NOT_POSTED | JournalEntryDomain.assertCanReverse |
| 409 CONFLICT | `FIN-409-PERIOD-NOT-OPEN` | FIN_409_PERIOD_NOT_OPEN | JournalEntryDomain.checkTargetPeriodOpen — as a `fieldErrors[]` entry |
| 409 CONFLICT | `FIN-409-PERIOD-NOT-OPEN` | FIN_409_PERIOD_NOT_OPEN | JournalEntryDomain.resolveReversalPeriodPk |
| 409 CONFLICT | `FIN-409-UNBALANCED` | FIN_409_UNBALANCED | JournalEntryDomain.checkBalanced — as a `fieldErrors[]` entry |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

<!-- fetched-from: endpoints/fin-recurring-template-management.md -->
<!-- AUTO-GENERATED by api-doc-generator — do not edit manually -->
# FIN Recurring Template Management

**Endpoints in this file:**

- `API-FIN-013` — [POST /api/v1/fin/recurring-templates](#post-apiv1finrecurring-templates)
- `API-FIN-012` — [POST /api/v1/fin/recurring-templates/search](#post-apiv1finrecurring-templatessearch)
- `API-FIN-036` — [PUT /api/v1/fin/recurring-templates/{id}/deactivate](#put-apiv1finrecurring-templatesiddeactivate)
- `API-FIN-014` — [POST /api/v1/fin/recurring-templates/{id}/run](#post-apiv1finrecurring-templatesidrun)

## POST /api/v1/fin/recurring-templates

**Create recurring template**

إنشاء قالب قيد متكرر

Contract ID: `API-FIN-013`

Operation ID: `create`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_RECURRING_TEMPLATES_CREATE (found on service:RecurringTemplateService)

### Request Body

Schema: `RecurringTemplateCreateRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| nameAr | string | Yes | maxLength: 150 | Name (Arabic) - الاسم بالعربية | إهلاك شهري |
| nameEn | string | Yes | maxLength: 150 | Name (English) - الاسم بالإنجليزية | Monthly depreciation |
| scheduleTypeCode | string | Yes | maxLength: 15 | Schedule type, RECURRING_SCHEDULE_TYPE lookup - نوع الجدولة | RECURRING |
| frequencyCode | string | No | maxLength: 15 | Frequency, RECURRING_FREQUENCY lookup; required unless the schedule type is REVERSING - التكرار | MONTHLY |
| startDate | string (date) | Yes |  | First run date - تاريخ البداية | 2026-01-31 |
| endDate | string (date) | No |  | Last run date, open-ended when omitted - تاريخ النهاية | 2026-12-31 |
| lines | array<RecurringTemplateLineCreateRequest> | Yes |  | Template lines - سطور القالب — Recurring template line - سطر قالب متكرر |  |
| lines[].accountId | integer (int64) | Yes |  | Target account id - معرّف الحساب | 12 |
| lines[].amount | number | Yes |  | Line amount, always positive - المبلغ | 1500.0 |
| lines[].directionCode | string | Yes | maxLength: 10 | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].dimensionValueId | integer (int64) | No |  | Optional dimension value id - معرّف قيمة البُعد | 5 |

**Request Example**

```json
{
  "nameAr": "إهلاك شهري",
  "nameEn": "Monthly depreciation",
  "scheduleTypeCode": "RECURRING",
  "frequencyCode": "MONTHLY",
  "startDate": "2026-01-31",
  "endDate": "2026-12-31",
  "lines": [
    {
      "accountId": 12,
      "amount": 1500.0,
      "directionCode": "DEBIT",
      "dimensionValueId": 5
    }
  ]
}
```

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `RecurringTemplateService.create`.

Shape: `RecurringTemplateResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| recurringTemplatePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | إهلاك شهري |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Monthly depreciation |
| scheduleTypeCode | string | No |  | Schedule type, RECURRING_SCHEDULE_TYPE lookup - نوع الجدولة | RECURRING |
| frequencyCode | string | No |  | Frequency, RECURRING_FREQUENCY lookup - التكرار | MONTHLY |
| startDate | string (date) | No |  | First run date - تاريخ البداية | 2026-01-31 |
| nextRunDate | string (date) | No |  | Next scheduled run date - تاريخ التشغيل التالي | 2026-01-31 |
| endDate | string (date) | No |  | Last run date - تاريخ النهاية | 2026-12-31 |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| lineCount | integer (int32) | No |  | Number of template lines - عدد السطور | 2 |
| lines | array<RecurringTemplateLineResponse> | No |  | Template lines - سطور القالب — Recurring template line - سطر قالب متكرر |  |
| lines[].recurringTemplateLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].recurringTemplateId | integer (int64) | No |  | Parent template id - معرّف القالب الأب | 1 |
| lines[].lineNo | integer (int32) | No |  | Line position within the template - رقم السطر | 1 |
| lines[].accountId | integer (int64) | No |  | Target account id - معرّف الحساب | 12 |
| lines[].amount | number | No |  | Line amount, always positive - المبلغ | 1500.0 |
| lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].dimensionValueId | integer (int64) | No |  | Optional dimension value id - معرّف قيمة البُعد | 5 |
| lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "recurringTemplatePk": 1,
  "nameAr": "إهلاك شهري",
  "nameEn": "Monthly depreciation",
  "scheduleTypeCode": "RECURRING",
  "frequencyCode": "MONTHLY",
  "startDate": "2026-01-31",
  "nextRunDate": "2026-01-31",
  "endDate": "2026-12-31",
  "isActiveFl": true,
  "lineCount": 2,
  "lines": [
    {
      "recurringTemplateLinePk": 1,
      "recurringTemplateId": 1,
      "lineNo": 1,
      "accountId": 12,
      "amount": 1500.0,
      "directionCode": "DEBIT",
      "dimensionValueId": 5
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `RecurringTemplateController.create`, `RecurringTemplateService.create`, `FinLookupValidationService.assertValidCode`, `FinLookupValidationService.assertValidCodeIfPresent`, `RecurringTemplateMapper.toEntity`, `RecurringTemplateLineMapper.toEntity`, `RecurringTemplateService.resolveAccount`, `RecurringTemplateService.resolveDimensionValue`, `RecurringTemplateMapper.toResponse`, `RecurringTemplateLineMapper.toResponse`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-LOOKUP` | FIN_400_INVALID_LOOKUP | FinLookupValidationService.assertValidCode |
| 400 BAD_REQUEST | `FIN-400-MISSING-FREQUENCY` | FIN_400_MISSING_FREQUENCY | RecurringTemplateService.create |
| 404 NOT_FOUND | `FIN-404-ACCOUNT` | FIN_404_ACCOUNT | RecurringTemplateService.resolveAccount |
| 409 CONFLICT | `FIN-409-INVALID-DIMENSION` | FIN_409_INVALID_DIMENSION | RecurringTemplateService.resolveDimensionValue |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## POST /api/v1/fin/recurring-templates/search

**Search recurring templates**

بحث في القوالب المتكررة

Contract ID: `API-FIN-012`

Operation ID: `search`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_RECURRING_TEMPLATES_VIEW (found on service:RecurringTemplateService)

### Request Body

Schema: `RecurringTemplateSearchRequest` (application/json)

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| filters | array<SearchFilter> | No |  | Filter criteria. Supported fields: nameAr / nameEn (LIKE), scheduleTypeCode (EQUALS - RECURRING_SCHEDULE_TYPE), frequencyCode (EQUALS - RECURRING_FREQUENCY), startDate / nextRunDate / endDate (EQUALS, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL - ISO yyyy-MM-dd), isActiveFl (EQUALS), recurringTemplatePk (EQUALS, IN), createdAt (comparison operators). Any other field, or an operator a field does not list, is rejected as 400 VALIDATION_ERROR naming it - معايير التصفية |  |
| filters[].field | string | No |  |  |  |
| filters[].operator | string | No | enum: EQUALS, NOT_EQUALS, LIKE, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, IN |  |  |
| filters[].value | object | No |  |  |  |
| sortField | string | No |  | Sort field. Supported: recurringTemplatePk, nameAr, nameEn, scheduleTypeCode, frequencyCode, startDate, nextRunDate, endDate, isActiveFl, createdAt. Any other value is rejected as 400 FIN-400-INVALID-SORT - حقل الترتيب |  |
| sortDirection | string | No | enum: ASC, DESC | Sort direction - اتجاه الترتيب |  |
| page | integer (int32) | No |  | Page number, zero-based - رقم الصفحة | 0 |
| size | integer (int32) | No |  | Page size - حجم الصفحة | 20 |

**Request Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "page": 0,
  "size": 20
}
```

### Response `200` — OK

Shape: `paginated list of RecurringTemplateResponse (see Pagination Envelope in index.md)`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| recurringTemplatePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | إهلاك شهري |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Monthly depreciation |
| scheduleTypeCode | string | No |  | Schedule type, RECURRING_SCHEDULE_TYPE lookup - نوع الجدولة | RECURRING |
| frequencyCode | string | No |  | Frequency, RECURRING_FREQUENCY lookup - التكرار | MONTHLY |
| startDate | string (date) | No |  | First run date - تاريخ البداية | 2026-01-31 |
| nextRunDate | string (date) | No |  | Next scheduled run date - تاريخ التشغيل التالي | 2026-01-31 |
| endDate | string (date) | No |  | Last run date - تاريخ النهاية | 2026-12-31 |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| lineCount | integer (int32) | No |  | Number of template lines - عدد السطور | 2 |
| lines | array<RecurringTemplateLineResponse> | No |  | Template lines - سطور القالب — Recurring template line - سطر قالب متكرر |  |
| lines[].recurringTemplateLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].recurringTemplateId | integer (int64) | No |  | Parent template id - معرّف القالب الأب | 1 |
| lines[].lineNo | integer (int32) | No |  | Line position within the template - رقم السطر | 1 |
| lines[].accountId | integer (int64) | No |  | Target account id - معرّف الحساب | 12 |
| lines[].amount | number | No |  | Line amount, always positive - المبلغ | 1500.0 |
| lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].dimensionValueId | integer (int64) | No |  | Optional dimension value id - معرّف قيمة البُعد | 5 |
| lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "recurringTemplatePk": 1,
  "nameAr": "إهلاك شهري",
  "nameEn": "Monthly depreciation",
  "scheduleTypeCode": "RECURRING",
  "frequencyCode": "MONTHLY",
  "startDate": "2026-01-31",
  "nextRunDate": "2026-01-31",
  "endDate": "2026-12-31",
  "isActiveFl": true,
  "lineCount": 2,
  "lines": [
    {
      "recurringTemplateLinePk": 1,
      "recurringTemplateId": 1,
      "lineNo": 1,
      "accountId": 12,
      "amount": 1500.0,
      "directionCode": "DEBIT",
      "dimensionValueId": 5
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `RecurringTemplateController.search`, `RecurringTemplateService.search`, `FinSearchSupport.assertSortAllowed`, `RecurringTemplateSearchRequest.getSortField`, `FinSearchSupport.temporalFieldConverter`, `RecurringTemplateMapper.toResponse`, `RecurringTemplateLineMapper.toResponse`, `FinSearchSupport.temporalFieldConverter`, `FinSearchSupport.parseLocalDate`, `FinSearchSupport.parseInstant`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 400 BAD_REQUEST | `FIN-400-INVALID-SORT` | FIN_400_INVALID_SORT | FinSearchSupport.assertSortAllowed |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseInstant |
| 400 BAD_REQUEST | `VALIDATION_ERROR` | VALIDATION_ERROR | FinSearchSupport.parseLocalDate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |
| 400 BAD_REQUEST | VALIDATION_ERROR | Endpoint accepts a JSON request body; GlobalExceptionHandler maps a malformed or invalid body (HttpMessageNotReadableException / MethodArgumentNotValidException) to this status. |

## PUT /api/v1/fin/recurring-templates/{id}/deactivate

**Deactivate recurring template**

إلغاء تفعيل قالب قيد متكرر

Contract ID: `API-FIN-036`

Operation ID: `deactivate`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_RECURRING_TEMPLATES_UPDATE (found on service:RecurringTemplateService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `200` — OK

Shape: `RecurringTemplateResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| recurringTemplatePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| nameAr | string | No |  | Name (Arabic) - الاسم بالعربية | إهلاك شهري |
| nameEn | string | No |  | Name (English) - الاسم بالإنجليزية | Monthly depreciation |
| scheduleTypeCode | string | No |  | Schedule type, RECURRING_SCHEDULE_TYPE lookup - نوع الجدولة | RECURRING |
| frequencyCode | string | No |  | Frequency, RECURRING_FREQUENCY lookup - التكرار | MONTHLY |
| startDate | string (date) | No |  | First run date - تاريخ البداية | 2026-01-31 |
| nextRunDate | string (date) | No |  | Next scheduled run date - تاريخ التشغيل التالي | 2026-01-31 |
| endDate | string (date) | No |  | Last run date - تاريخ النهاية | 2026-12-31 |
| isActiveFl | boolean | No |  | Active status - حالة التفعيل | true |
| lineCount | integer (int32) | No |  | Number of template lines - عدد السطور | 2 |
| lines | array<RecurringTemplateLineResponse> | No |  | Template lines - سطور القالب — Recurring template line - سطر قالب متكرر |  |
| lines[].recurringTemplateLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].recurringTemplateId | integer (int64) | No |  | Parent template id - معرّف القالب الأب | 1 |
| lines[].lineNo | integer (int32) | No |  | Line position within the template - رقم السطر | 1 |
| lines[].accountId | integer (int64) | No |  | Target account id - معرّف الحساب | 12 |
| lines[].amount | number | No |  | Line amount, always positive - المبلغ | 1500.0 |
| lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].dimensionValueId | integer (int64) | No |  | Optional dimension value id - معرّف قيمة البُعد | 5 |
| lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "recurringTemplatePk": 1,
  "nameAr": "إهلاك شهري",
  "nameEn": "Monthly depreciation",
  "scheduleTypeCode": "RECURRING",
  "frequencyCode": "MONTHLY",
  "startDate": "2026-01-31",
  "nextRunDate": "2026-01-31",
  "endDate": "2026-12-31",
  "isActiveFl": true,
  "lineCount": 2,
  "lines": [
    {
      "recurringTemplateLinePk": 1,
      "recurringTemplateId": 1,
      "lineNo": 1,
      "accountId": 12,
      "amount": 1500.0,
      "directionCode": "DEBIT",
      "dimensionValueId": 5
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `RecurringTemplateController.deactivate`, `RecurringTemplateService.deactivate`, `RecurringTemplate.deactivate`, `RecurringTemplateMapper.toResponse`, `RecurringTemplateLineMapper.toResponse`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-TEMPLATE` | FIN_404_TEMPLATE | RecurringTemplateService.deactivate |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

## POST /api/v1/fin/recurring-templates/{id}/run

**Run a recurring template**

تشغيل قالب قيد متكرر

Contract ID: `API-FIN-014`

Operation ID: `run`

**Authentication**

Required (bearerAuth).

**Required permission(s)**: PERM_FIN_RECURRING_TEMPLATES_UPDATE (found on service:RecurringTemplateService)

### Path Parameters

| Name | Type | Required | Description |
|---|---|---|---|
| id | integer | Yes |  |

### Response `201` — Created

Status read from source: `Status.CREATED` returned by `RecurringTemplateService.run`.

Shape: `JournalEntryResponse`

| Field | Type | Required | Constraints | Description | Example |
|---|---|---|---|---|---|
| journalEntryPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| docNo | string | No |  | System-generated document number - رقم المستند | JV-2026-000123 |
| docDate | string (date) | No |  | Document date - تاريخ المستند | 2026-01-31 |
| fiscalYearId | integer (int64) | No |  | Owning fiscal year id - معرّف السنة المالية | 1 |
| periodId | integer (int64) | No |  | Target fiscal period id - معرّف الفترة المالية | 1 |
| journalTypeCode | string | No |  | Journal type, JOURNAL_TYPE lookup - نوع اليومية | MANUAL |
| statusCode | string | No |  | Status, JOURNAL_STATUS lookup - الحالة | POSTED |
| eventReference | string | No |  | Source event reference, for event-built entries - مرجع الحدث | INV-2026-0001 |
| originalEntryId | integer (int64) | No |  | Original entry id, when this entry is a reversal - معرّف القيد الأصلي | 7 |
| reversalEntryId | integer (int64) | No |  | Reversal entry id, when this entry has been reversed - معرّف قيد العكس | 9 |
| descriptionAr | string | No |  | Entry description (Arabic) - وصف القيد بالعربية | قيد إهلاك يناير |
| descriptionEn | string | No |  | Entry description (English) - وصف القيد بالإنجليزية | January depreciation entry |
| postedAt | string (date-time) | No |  | Posting timestamp - تاريخ الترحيل |  |
| lineCount | integer (int32) | No |  | Number of entry lines - عدد السطور | 2 |
| lines | array<JournalLineResponse> | No |  | Entry lines - سطور القيد — Journal entry line - سطر قيد يومية |  |
| lines[].journalLinePk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].journalEntryId | integer (int64) | No |  | Parent journal entry id - معرّف القيد الأب | 1 |
| lines[].lineNo | integer (int32) | No |  | Line position within the entry - رقم السطر | 1 |
| lines[].accountId | integer (int64) | No |  | Target account id - معرّف الحساب | 12 |
| lines[].amount | number | No |  | Line amount, always positive - المبلغ | 1500.0 |
| lines[].directionCode | string | No |  | Direction, DEBIT_CREDIT lookup - الاتجاه | DEBIT |
| lines[].isRemainderFl | boolean | No |  | Carries the remainder of a percentage distribution - سطر الباقي | false |
| lines[].descriptionAr | string | No |  | Line description (Arabic) - وصف السطر بالعربية | إهلاك |
| lines[].descriptionEn | string | No |  | Line description (English) - وصف السطر بالإنجليزية | Depreciation |
| lines[].dimensions | array<JournalLineDimensionResponse> | No |  | Analysis dimension tags - الأبعاد التحليلية — Journal line dimension tag - بُعد تحليلي لسطر القيد |  |
| lines[].dimensions[].journalLineDimensionPk | integer (int64) | No |  | Unique identifier - المعرف الفريد | 1 |
| lines[].dimensions[].journalLineId | integer (int64) | No |  | Parent journal line id - معرّف سطر القيد الأب | 1 |
| lines[].dimensions[].dimensionId | integer (int64) | No |  | Dimension id - معرّف البُعد | 1 |
| lines[].dimensions[].dimensionValueId | integer (int64) | No |  | Dimension value id - معرّف قيمة البُعد | 5 |
| lines[].createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdAt | string (date-time) | No |  | Created timestamp - تاريخ الإنشاء |  |
| createdBy | string | No |  | Created by - أنشئ بواسطة |  |
| updatedAt | string (date-time) | No |  | Updated timestamp - تاريخ التحديث |  |
| updatedBy | string | No |  | Updated by - حُدّث بواسطة |  |

**Response Example**

_(partial — only fields with a documented example are shown)_

```json
{
  "journalEntryPk": 1,
  "docNo": "JV-2026-000123",
  "docDate": "2026-01-31",
  "fiscalYearId": 1,
  "periodId": 1,
  "journalTypeCode": "MANUAL",
  "statusCode": "POSTED",
  "eventReference": "INV-2026-0001",
  "originalEntryId": 7,
  "reversalEntryId": 9,
  "descriptionAr": "قيد إهلاك يناير",
  "descriptionEn": "January depreciation entry",
  "lineCount": 2,
  "lines": [
    {
      "journalLinePk": 1,
      "journalEntryId": 1,
      "lineNo": 1,
      "accountId": 12,
      "amount": 1500.0,
      "directionCode": "DEBIT",
      "isRemainderFl": false,
      "descriptionAr": "إهلاك",
      "descriptionEn": "Depreciation",
      "dimensions": [
        {
          "journalLineDimensionPk": 1,
          "journalLineId": 1,
          "dimensionId": 1,
          "dimensionValueId": 5
        }
      ]
    }
  ]
}
```

### Business Responses

Raised by this endpoint's own rules. Each row cites the throw site it was read from (walked `RecurringTemplateController.run`, `RecurringTemplateService.run`, `RecurringTemplateDomain.from`, `JournalPostingService.resolvePeriodContaining`, `RecurringTemplateService.dimensionTagsOf`, `JournalPostingService.buildValidateAndPost`, `RecurringTemplateMapper.nextRunDateAfter`, `RecurringTemplateService.postReversalInNextPeriod`, `JournalEntryMapper.toResponse`, `RecurringTemplateDomain.assertCanRun`, `JournalEntryMapper.toLineResponse`, `new RecurringTemplateDomain()`, `JournalEntryMapper.BuiltDimension`, `AccountDomain.from`, `DimensionValueDomain.from`, `JournalDocNoGenerator.next`, `JournalPostingService.highestDocNoOf`, `JournalEntryMapper.toEntity`, `JournalEntryDomain.create`, `JournalEntryDomain.checkBalanced`, `JournalEntryDomain.checkTargetPeriodOpenForGeneratedEntry`, `JournalEntry.post`, `JournalEntryDomain.from`, `RecurringTemplateService.nextPeriodAfter`, `JournalEntryDomain.reversalDirectionOf`, `JournalEntry.linkOriginal`, `JournalEntryDomain.assertCanReverse`, `JournalEntryMapper.toLineDimensionResponse`, `new AccountDomain()`, `new DimensionValueDomain()`, `JournalDocNoGenerator.format`, `JournalDocNoGenerator.counterOf`, `JournalEntryMapper.toBuiltLineEntity`, `new JournalEntryDomain()`, `JournalEntryDomain.isYearEndGenerated`, `JournalEntryDomain.checkTargetPeriodOpen`).

| HTTP Status | Code | Constant | Raised at |
|---|---|---|---|
| 404 NOT_FOUND | `FIN-404-PERIOD` | FIN_404_PERIOD | JournalPostingService.resolvePeriodContaining |
| 404 NOT_FOUND | `FIN-404-PERIOD` | FIN_404_PERIOD | RecurringTemplateService.nextPeriodAfter |
| 404 NOT_FOUND | `FIN-404-TEMPLATE` | FIN_404_TEMPLATE | RecurringTemplateService.run |
| 404 NOT_FOUND | `FIN-404-YEAR` | FIN_404_YEAR | JournalPostingService.buildValidateAndPost |
| 409 CONFLICT | `FIN-409-ALREADY-REVERSED` | FIN_409_ALREADY_REVERSED | JournalEntryDomain.assertCanReverse |
| 409 CONFLICT | `FIN-409-DUPLICATE-EVENT` | FIN_409_DUPLICATE_EVENT | JournalEntryDomain.create |
| 409 CONFLICT | `FIN-409-NOT-ACTIVE` | FIN_409_NOT_ACTIVE | RecurringTemplateDomain.assertCanRun |
| 409 CONFLICT | `FIN-409-NOT-POSTED` | FIN_409_NOT_POSTED | JournalEntryDomain.assertCanReverse |
| 409 CONFLICT | `FIN-409-PERIOD-NOT-OPEN` | FIN_409_PERIOD_NOT_OPEN | JournalEntryDomain.checkTargetPeriodOpen — as a `fieldErrors[]` entry |
| 409 CONFLICT | `FIN-409-UNBALANCED` | FIN_409_UNBALANCED | JournalEntryDomain.checkBalanced — as a `fieldErrors[]` entry |

### Other Possible Responses

Structurally guaranteed by this endpoint's own shape (auth requirement, permission check, request body) combined with the shared framework's exception handling — not specific business errors.

| HTTP Status | Code | Why |
|---|---|---|
| 401 UNAUTHORIZED | SEC_401_INVALID_CREDENTIALS (`SEC-401-INVALID-CREDENTIALS`) | Endpoint requires authentication; an unauthenticated call never reaches the controller — SecSecurityErrorHandler.commence, wired as SecurityConfig's authenticationEntryPoint, writes this code. |
| 403 FORBIDDEN | FIN_403_FORBIDDEN (`FIN-403-FORBIDDEN`) | An authorization check was found for this endpoint (@PreAuthorize/@Secured); FinForbiddenAdvisor wraps `com.erp.fin.service.*`, catches AccessDeniedException and re-raises it as this module code, so the shared ACCESS_DENIED handler is not reached. |

<<<END INPUT>>>

<<<INPUT: test-execution-manifest>>>
# TEST EXECUTION MANIFEST — الحسابات العامة / Finance (General Ledger) (FIN)
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v2 (delta on v1 — change set CS-FIN-001)   Profile : erp   Scope : module
Emitted because `profile.stack.testing.manifest` is ON. A **derived view** for the `api-verify`
standalone: it introduces no id, defines nothing, gates nothing, and every cell below is an
`ID` or a value one of the cited artifacts already states.
Derived from : `backend-test-plan-fin.md` v2 (this run) · `_state/current-backend-execution-plan.md`
               (v2) · `_state/current-srs.md` (v2) · `_state/current-registry-db.md` (v2)
Regenerate whenever the backend execution plan or the backend test plan changes — a stale
manifest is a defect. v2 regenerates it in full: it carries no id to fold by, so a partial
manifest would drop every v1 row.
══════════════════════════════════════════════════════════════════

## DEPENDENCY ORDER

The order in which entities must be built for any TC of this module to run. It is read off the FK
and XM relations of the backend plan and the "must belong to" rules, and nothing else.

```
0.  MDL · LookupType + LookupValue for FIN's 15 owned keys      (ENT-MDL-001/002, XM-FIN-001)
      ── not this module's data. Every lookup-backed column is validated by membership in the
         list MDL returns, so no FIN row carrying a code can be written before the keys exist.
         REQ-FIN-045 / TC-FIN-045 is the registration: the 13 v1 keys by onboarding, then
         db-script v2 BLOCK 8 adds FIN_EVENT_BUSINESS_FIELD (4 values) and
         FIN_DIMENSION_VALUE_SOURCE (2), seeds CHEQUE into PAYMENT_METHOD and deactivates
         DIRECT in ACCOUNT_DERIVATION_TYPE. BLOCK 8 must run before API-FIN-011 or API-FIN-039 is
         first called. Host data comes on top: ACCOUNTING_EVENT_TYPE values (PAYMENT_RECEIVED,
         EXPENSE_PAID in the v2 cases) and every PAYMENT_METHOD value but CHEQUE (CASH,
         TRANSFER) are added in MDL by the deploying organisation — a mapping for a
         PAYMENT_METHOD value that MDL lacks is refused by RULE-FIN-024.
1.  SEC · ModuleRegistry, ScreenRegistry, ActionRegistry rows for FIN   (ENT-SEC-004/005/006)
      ── every endpoint below is gated, so no TC has a caller until FIN's 13 screens and 30
         actions are registered and granted. REQ-FIN-044 / TC-FIN-044 is that registration.
         The three FIN_ACCOUNT_MAPPINGS actions come from the v2 FIN security migration, which
         also grants them to SYS_ADMIN; without that grant every mapping case answers
         FIN-403-FORBIDDEN. The event consumer's service principal needs a role holding
         PERM_FIN_JOURNAL_ENTRIES_CREATE for every API-FIN-020 case.
2.  ENT-FIN-001 · Account            (FIN_ACCOUNT)              ← API-FIN-002
      ── self-referencing through parentAccountId (DBF-FIN-007): roots before children, and a
         parent stops being a leaf as soon as it gains one (RULE-FIN-001). At most one row
         carries isRetainedEarningsFl (DBF-FIN-147), which API-FIN-027 derives rather than
         receives — TC-FIN-036 needs it present.
3.  ENT-FIN-002 · Dimension          (FIN_DIMENSION)            ← API-FIN-006
4.  ENT-FIN-003 · DimensionValue     (FIN_DIMENSION_VALUE)      ← API-FIN-007
      ── depends on 3: the code must be unique within its dimension (RULE-FIN-002), and the
         parent id arrives as the path id. Only API-FIN-035 can clear its active flag, which is
         the one setup step of TC-FIN-021, TC-FIN-069 and TC-FIN-118. The v2 event cases resolve
         a value by its code within its dimension (ADR-FIN-026), so ORGANISATION "ORG-1" and
         BRANCH "BR-07" must exist before any tagged event posts.
5.  ENT-FIN-007 · FiscalYear         (FIN_FISCAL_YEAR)          ← API-FIN-023
      ── TC-FIN-036 additionally needs a SUCCESSOR year whose startDate is the day after this
         year's endDate: the adjacency is derived, ENT-FIN-007 declaring no successor column.
6.  ENT-FIN-008 · FiscalPeriod       (FIN_FISCAL_PERIOD)        ← API-FIN-023 (generated with 5)
      ── never created directly; generated with its year, each period OPEN. Transitions run
         OPEN ⇄ SOFT_CLOSE → HARD_CLOSE → YEAR_END_CLOSE (API-FIN-024/025/026, then 027).
         TC-FIN-126 needs a date that NO period covers; TC-FIN-125 a covering period in
         SOFT_CLOSE.
7.  ENT-FIN-009 · EventTypeRule      (FIN_EVENT_TYPE_RULE)      ← API-FIN-010
8.  ENT-FIN-010 · RuleLine           (FIN_RULE_LINE)            ← API-FIN-011
      ── depends on 7 (path id) and on 2 for any CONSTANT-derived account. RULE-FIN-003 is
         evaluated over the whole sibling set, so lines must be added in an order that never
         leaves a percentage set without exactly one remainder line at save time. v2: a line is
         CONSTANT (an account code, no business field) or MAPPING (a business field, no code),
         RULE-FIN-018 and RULE-FIN-019.
9.  ENT-FIN-016 · RuleLineDimension  (FIN_RULE_LINE_DIM)        ← API-FIN-011 (same request, v2)
      ── written in one transaction with its line, never alone; depends on 8, on 3 (the tagged
         dimension) and, for a CONSTANT tag, on 4 (a value of that same dimension,
         RULE-FIN-026). One tag per dimension per line (RULE-FIN-025).
10. ENT-FIN-015 · AccountMapping     (FIN_ACCOUNT_MAPPING)      ← API-FIN-039 (v2)
      ── depends on 2 (an active leaf on save, RULE-FIN-023) and on step 0 (the event type,
         the business field and, for PAYMENT_METHOD, the value, RULE-FIN-024). It is
         independent of 7–9 in the store — no FK ties a mapping to a rule — but an event whose
         MAPPING line reads it posts only once both exist. One active row per key
         (UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY). An account's later loss of leaf or active status
         is caught at post (TC-FIN-100, TC-FIN-101), not re-checked on the mapping.
11. ENT-FIN-011 · RecurringTemplate  (FIN_RECURRING_TEMPLATE)   ← API-FIN-013
12. ENT-FIN-012 · RecurringTemplateLine                         ← API-FIN-013 (same request)
      ── depends on 11, on 2 (accountId) and optionally on 4 (dimensionValueId).
13. ENT-FIN-013 · AllocationRule     (FIN_ALLOCATION_RULE)      ← API-FIN-016
14. ENT-FIN-014 · AllocationTarget   (FIN_ALLOCATION_TARGET)    ← API-FIN-016 (same request)
      ── depends on 13 and on 2 (sourceAccountId, targetAccountId); RULE-FIN-003 again.
15. ENT-FIN-004 · JournalEntry       (FIN_JOURNAL_ENTRY)        ← API-FIN-019, 020, and generated
                                                                   by 014, 017, 021, 027
16. ENT-FIN-005 · JournalLine        (FIN_JOURNAL_LINE)         ← written with 15
17. ENT-FIN-006 · JournalLineDimension                          ← written with 16
      ── 15–17 are one atomic write and depend on everything above: leaf active accounts (2),
         an Open period belonging to the submitted year (5, 6), valid active dimension values
         (3, 4), and — for an event-built entry — an active rule with its lines and tags
         (7, 8, 9) plus an active mapping for every MAPPING line (10). An event-built line
         writes its dimensions in exactly the manual shape (REQ-FIN-070) and stores the account
         it resolved, so a later mapping change never reaches it (TC-FIN-108). The docNo
         counter is scoped per fiscalYearId and restarts at 000001 each year, so a test
         asserting a document number must know which year's series it is drawing from. A
         rejected event writes nothing at all (SRS A7).
```

Nothing is seeded outside these steps: every required column is written by an endpoint or by the
platform. Steps 0 and 1 are the only rows FIN does not create through an endpoint of its own.
CS-FIN-001's own migration data — the real account mappings — is out of scope; until it is
loaded, every MAPPING line answers FIN-422-UNMAPPED-VALUE, and each v2 case sets up the mappings
it needs.

## RULE → CODE → TC

Runtime code format `FIN-{http}[-{SLUG}]`; the envelope is
`LocalizedException → {code, messageAr, messageEn}`. Rows are the rules that raise a catalog code;
the rules whose effect is a success path or an omission are listed after the table rather than
given empty cells. Frontend cases are marked.

| RULE | catalog code | TC | HTTP | API |
|---|---|---|---|---|
| RULE-FIN-001 | FIN-409-HAS-CHILDREN | TC-FIN-002 · TC-FIN-054 (frontend) | 409 | API-FIN-003 |
| RULE-FIN-001 | FIN-409-PARENT-NOT-LEAF-ELIGIBLE | — no TC (no AC states the create-path variant) | 409 | API-FIN-002 |
| RULE-FIN-002 | FIN-409-DIMVALUE-DUP | TC-FIN-006 · TC-FIN-058 (frontend) | 409 | API-FIN-007 |
| RULE-FIN-003 | FIN-409-REMAINDER-COUNT | TC-FIN-009, TC-FIN-047 · TC-FIN-061, TC-FIN-073 (frontend) | 409 | API-FIN-011, API-FIN-016, API-FIN-017, API-FIN-020 |
| RULE-FIN-003 | FIN-422-REMAINDER-MARKER | — no TC (no AC states marker disagreement) | 422 | API-FIN-011, API-FIN-016, API-FIN-017, API-FIN-020 |
| RULE-FIN-004 | FIN-409-DUPLICATE-EVENT | TC-FIN-011, TC-FIN-123 (the pre-check), TC-FIN-124 (the store's refusal under concurrency) | 409 | API-FIN-020 |
| RULE-FIN-005 | FIN-404-NO-ACTIVE-RULE | TC-FIN-013 | 404 | API-FIN-020 |
| RULE-FIN-006 | FIN-409-UNBALANCED | TC-FIN-015, TC-FIN-018 · TC-FIN-063, TC-FIN-066 (frontend) | 409 | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 |
| RULE-FIN-007 | FIN-409-NOT-POSTABLE-ACCOUNT | TC-FIN-003, TC-FIN-015, TC-FIN-019, TC-FIN-100, TC-FIN-101 (a mapped account) · TC-FIN-055, TC-FIN-063, TC-FIN-067 (frontend) | 409 | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 |
| RULE-FIN-008 | FIN-409-PERIOD-NOT-OPEN | TC-FIN-020, TC-FIN-033, TC-FIN-125 (the event path — the consumer's retry signal), TC-FIN-127 (its published row) · TC-FIN-068 (frontend) | 409 | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021 |
| RULE-FIN-008 | — (carve-out: the year-end CLOSING/OPENING entries are exempt, asserted as **not** raised) | TC-FIN-036 | — | API-FIN-027 |
| RULE-FIN-009 | FIN-409-INVALID-DIMENSION | TC-FIN-021, TC-FIN-118 (an event-derived value) · TC-FIN-069 (frontend) | 409 | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 |
| RULE-FIN-010 | FIN-422-REMAINDER-NOT-POSITIVE | TC-FIN-048 | 422 | API-FIN-017, API-FIN-020 |
| RULE-FIN-013 | FIN-409-ALREADY-REVERSED | TC-FIN-030 · TC-FIN-078 (frontend) | 409 | API-FIN-021 |
| RULE-FIN-013 | FIN-409-NOT-POSTED | — no TC (AC-FIN-030's Given is the already-reversed entry; DRAFT is never a stored browsable state) | 409 | API-FIN-021, API-FIN-014 |
| RULE-FIN-014 | FIN-409-NOT-REOPENABLE | TC-FIN-035 · TC-FIN-083 (frontend) | 409 | API-FIN-024, API-FIN-026 |
| RULE-FIN-015 | FIN-403-FORBIDDEN | TC-FIN-038, TC-FIN-052 · TC-FIN-086 (frontend) | 403 | API-FIN-026, API-FIN-027 |
| RULE-FIN-017 | FIN-400-PERIOD-NOT-IN-YEAR, FIN-400-DOCDATE-OUTSIDE-PERIOD | — no TC (the rule was added at SVC-API and carries no AC of its own) | 400 | API-FIN-019 |
| RULE-FIN-018 | FIN-422-UNSUPPORTED-DERIVATION | TC-FIN-094 · TC-FIN-130 (frontend) | 422 | API-FIN-011 |
| RULE-FIN-019 | FIN-422-DERIVATION-SPEC | — no TC raises it (AC-FIN-049 is the passing case, TC-FIN-095; TC-FIN-131 the form pre-empt) | 422 | API-FIN-011 |
| RULE-FIN-020 | FIN-422-UNMAPPED-VALUE | TC-FIN-102, TC-FIN-103, TC-FIN-107 | 422 | API-FIN-020 |
| RULE-FIN-021 | FIN-422-MISSING-BUSINESS-FIELD | TC-FIN-099 (a MAPPING line), TC-FIN-119 (a dimension tag) | 422 | API-FIN-020 |
| RULE-FIN-022 | FIN-409-MAPPING-DUP | TC-FIN-109 · TC-FIN-137 (frontend) | 409 | API-FIN-039 |
| RULE-FIN-023 | FIN-422-MAPPED-ACCOUNT-INELIGIBLE | TC-FIN-110 · TC-FIN-138 (frontend) | 422 | API-FIN-039, API-FIN-040 |
| RULE-FIN-024 | FIN-422-INVALID-PAYMENT-METHOD | TC-FIN-111 · TC-FIN-139 (frontend) | 422 | API-FIN-039 |
| RULE-FIN-025 | FIN-422-DIMENSION-TAG-DUP | TC-FIN-115 · TC-FIN-141 (frontend) | 422 | API-FIN-011 |
| RULE-FIN-026 | FIN-422-DIMENSION-TAG-SOURCE | TC-FIN-116 · TC-FIN-142 (frontend) | 422 | API-FIN-011 |
| RULE-FIN-027 | FIN-422-UNRESOLVED-DIMENSION-VALUE | TC-FIN-120 | 422 | API-FIN-020 |
| RULE-FIN-028 | FIN-422-NO-DEFINED-PERIOD | TC-FIN-126 | 422 | API-FIN-020 |

**Rules that raise no code, and why** — they are enforced, not informational, so they are named
rather than dropped: RULE-FIN-010's own computation (the per-side remainder difference) is a
success path, asserted by TC-FIN-012, TC-FIN-026 and, on a MAPPING remainder line, TC-FIN-128;
the exact mirrored reversal of RULE-FIN-011 and its period substitution under RULE-FIN-012 are
success paths, asserted by TC-FIN-028, TC-FIN-029, TC-FIN-024 and, for a new-path entry, TC-FIN-129;
RULE-FIN-016 is enforced by omission — no update and no delete mapping exists on a POSTED row
anywhere in the module — and is asserted by TC-FIN-016 and TC-FIN-017; RULE-FIN-020 and
RULE-FIN-027 also have success paths, the resolved mapping of TC-FIN-096 … TC-FIN-098 and the
resolved dimension value of TC-FIN-114. The three messages the SRS gives RULE-FIN-010,
RULE-FIN-011 and RULE-FIN-012 are the only genuinely informational strings in the module and no
TC asserts them as a response body.

**The event consumer's three classes** (backend plan, DOC phase), for `api-verify`: already
posted — FIN-409-DUPLICATE-EVENT, acknowledge (TC-FIN-123, TC-FIN-124); retry later —
FIN-409-PERIOD-NOT-OPEN, wait and retry (TC-FIN-125); needs a fix — every other API-FIN-020 code,
the four v2 ones among them (TC-FIN-099, TC-FIN-102, TC-FIN-120, TC-FIN-126). TC-FIN-125 and
TC-FIN-126 together assert that a date with no period never answers the retry code.

**Catalog rows no TC exercises**, listed so `api-verify` does not read this table as complete
coverage of the catalog: FIN-409-ACCOUNT-DUP, FIN-409-DIMENSION-DUP, FIN-409-RULE-DUP,
FIN-409-YEAR-DUP, FIN-400-INVALID-LOOKUP, FIN-400-MISSING-FREQUENCY, FIN-400-INVALID-SORT,
FIN-422-INVALID-PERCENTAGE-VALUE, FIN-409-NOT-ACTIVE, FIN-409-INVALID-TRANSITION,
FIN-409-PERIODS-NOT-CLOSED, FIN-422-DERIVATION-SPEC, FIN-404-ACCOUNT-MAPPING, the `FIN-404-*`
not-found rows raised outside an AC's own path, and FIN-500. No `AC-FIN-*` states any of them;
the absence is a requirements-side gap, not a test-generation one — ADR-FIN-009, ADR-FIN-063.

**Two rows are not gaps and must not be tested**: `FIN-403-SOD-VIOLATION`, struck as unreachable
— its two throw sites were deleted and the global user-set-disjointness behaviour it signalled is
not what RULE-FIN-015 asks for, so TC-FIN-038 asserts the permission gate and explicitly nothing
about disjointness; and `FIN-422-MAPPING-UNSUPPORTED`, struck in v2 because MAPPING is
implemented (ADR-FIN-027) — no path raises it.

## ENTITY CRUD CHECKLIST

Each cell is ✓ only where an `API-*` of the backend plan serves it; a `—` carries its reason.
`deactivate` is the soft semantics the profile declares: no endpoint of this module removes a row.

| ENT | create | read (by id) | search | update | deactivate (soft) | activate |
|---|---|---|---|---|---|---|
| ENT-FIN-001 · Account | ✓ API-FIN-002 | — deliberate v1 exclusion: the Entry form hydrates from the search row | ✓ API-FIN-001 | ✓ API-FIN-003 | ✓ API-FIN-004 | — none in FIN, following the delivered deactivate precedent |
| ENT-FIN-002 · Dimension | ✓ API-FIN-006 | — none published | ✓ API-FIN-005 | — none published | — deliberate: no REQ/AC/RULE needs it and the flag drives no behaviour | — none in FIN |
| ENT-FIN-003 · DimensionValue | ✓ API-FIN-007 | — none published | ✓ API-FIN-008 | — none published | ✓ API-FIN-035 | — none in FIN |
| ENT-FIN-004 · JournalEntry | ✓ API-FIN-019, API-FIN-020 (and generated by API-FIN-014, 017, 021, 027) | ✓ API-FIN-022 | ✓ API-FIN-018 | — RULE-FIN-016: a POSTED entry is locked; correction is a reversal | — no soft-delete flag on this entity | — n/a |
| ENT-FIN-005 · JournalLine | ✓ with its header | ✓ nested in API-FIN-022; read by API-FIN-028, 029, 030, 031 | — no standalone line search | — never edited independently of its header | — n/a | — n/a |
| ENT-FIN-006 · JournalLineDimension | ✓ with its line — for an event-built line, one row per resolved tag (v2) | ✓ nested in API-FIN-022; read by API-FIN-032 | — none | — none | — n/a | — n/a |
| ENT-FIN-007 · FiscalYear | ✓ API-FIN-023 | — none published: the create response already carries the year with its periods | — **KNOWN GAP** in the backend plan: its API registry holds no fiscal-year search. The fetched api-docs publish `POST /api/v1/fin/fiscal-years/search` without a contract id, and the frontend cites it by path (ADR-FIN-061). No backend TC exercises it, no AC stating a search; on the frontend it feeds the year list and record page TC-FIN-079 walks through, and the year selects | ✓ statusCode only, set once by API-FIN-027 | — no soft-delete flag | — n/a |
| ENT-FIN-008 · FiscalPeriod | ✓ generated with its year by API-FIN-023 | — none published: API-FIN-033 returns the full row | ✓ API-FIN-033 | ✓ API-FIN-024, API-FIN-025, API-FIN-026 (the three state transitions) | — no soft-delete flag; HARD_CLOSE is the terminal state instead | — RULE-FIN-014: a hard-closed period never reopens |
| ENT-FIN-009 · EventTypeRule | ✓ API-FIN-010 | — none published | ✓ API-FIN-009 | — none published | ✓ API-FIN-034 | — none in FIN |
| ENT-FIN-010 · RuleLine | ✓ API-FIN-011 (v2: with its tags) | ✓ embedded in the rule's own response | — none | — none published | — the entity carries no active flag | — n/a |
| ENT-FIN-011 · RecurringTemplate | ✓ API-FIN-013 | ✓ the search response carries the full aggregate, lines included | ✓ API-FIN-012 | — **OPEN GAP**: no update endpoint; a wrong template can only be retired and replaced | ✓ API-FIN-036 | — none in FIN |
| ENT-FIN-012 · RecurringTemplateLine | ✓ with its template | ✓ in the aggregate | — none | — none | — n/a | — n/a |
| ENT-FIN-013 · AllocationRule | ✓ API-FIN-016 | ✓ the search response carries the full aggregate, targets included | ✓ API-FIN-015 | — **OPEN GAP**: no update endpoint; same shape as ENT-FIN-011 | ✓ API-FIN-037 | — none in FIN |
| ENT-FIN-014 · AllocationTarget | ✓ with its rule | ✓ in the aggregate | — none | — none | — n/a | — n/a |
| ENT-FIN-015 · AccountMapping (v2) | ✓ API-FIN-039 | — none: the SRS names no by-id read and the Entry hydrates from the search row | ✓ API-FIN-038 | ✓ API-FIN-040 — the account only; the key is fixed after create (ADR-FIN-024) | ✓ API-FIN-041 | — none: a deactivated key is restored by creating a new mapping (ADR-FIN-024) |
| ENT-FIN-016 · RuleLineDimension (v2) | ✓ with its rule line, API-FIN-011 | ✓ embedded in the line's response | — none | — none: a tag lives and retires with its line (ADR-FIN-025) | — no active flag | — n/a |

**Custom actions, which no CRUD column holds**: reverse an entry (API-FIN-021, gated by its own
`PERM_FIN_JOURNAL_ENTRIES_REVERSE`, and reversing an event-built entry exactly like any other);
run a template (API-FIN-014) and run an allocation rule (API-FIN-017), both modelled as
update-class actions; hard-close a period (API-FIN-026) and run the year-end close (API-FIN-027),
both gated by `PERM_FIN_PERIODS_CLOSE_APPROVE` — the distinct permission RULE-FIN-015 requires;
build an entry from an event (API-FIN-020), a system call under the consumer's service principal.

**No DELETE column appears above** because FIN publishes no `DELETE` endpoint on any screen and
no `PERM_FIN_*_DELETE` row is registered — v2's FIN_ACCOUNT_MAPPINGS included. Every `—` in the
table is a recorded decision or a recorded gap, not an untested endpoint.
══════════════════════════════════════════════════════════════════

<<<END INPUT>>>

<<<INPUT: change-manifest>>>
# CHANGE MANIFEST — CS-FIN-001
Module       : FIN      Version: v2      Baseline: v1
Change type  : ADDITIVE
Summary      : Make FIN able to derive and post a journal entry from a business event emitted by the legacy Oracle system, so that legacy accounting can be retired. Four deltas on v1, none of which reopens a v1 invariant. (1) MAPPING account derivation: a FIN-owned, screen-managed mapping store that turns a business value carried by the event into an account code, replacing the unimplemented MAPPING mode; an unmapped value is a hard error and the entry does not post. (2) Dimension tags on rule-driven lines: a rule line carries dimension values derived from the event, each either a constant or a named business field, validated by FIN's existing dimension rules, so event-built and manually-built lines have the same dimension shape. (3) A duplicate event reference must always answer the duplicate-event error, whether the pre-check or the unique constraint caught it. (4) A posting attempt into a non-open period must answer its own distinct, catalogued and documented error code. The governing boundary for all four: no account code and no accounting concept of any kind may travel inside an event payload; the event states a business fact and FIN alone decides what it means in accounting terms.

## Scope detail (the raised request, restated for this change set)

### 1. MAPPING account derivation — blocking

A rule line derives its account today by CONSTANT (the value is the account code, fixed per line) or
by DIRECT (the value names an event field carrying the account code). MAPPING is declared but throws
an unsupported error. CONSTANT cannot vary; DIRECT breaks the boundary because it puts an account
code in the payload. Two legacy behaviours choose the account by a business value and fit neither:

- Cash versus bank. Legacy posts to the cash account and swaps it for the bank account when the
  payment was by cheque. The event carries the payment method as a business fact and FIN must decide
  which account that means. Eleven of the sixteen live legacy branches need this.
- Expense account per expense type. The event carries an expense type code and the account depends
  on it.

Requested: a FIN-owned mapping store, managed as data through a FIN screen like every other
configuration in the module, holding per entry the event type it serves, the business field being
mapped (payment method, expense type code), the business source value, the account code it resolves
to, an active flag and audit fields per FIN's existing conventions. Behaviour: when a rule line's
derivation is MAPPING, the engine reads the named business field from the event, resolves it through
the store, and validates the resulting account exactly as it validates a CONSTANT one — leaf, active,
postable. An unmapped value is an error and no entry posts; never a default, never a guess. A
dedicated error code is needed and FIN's analysis owns its name.

Also to settle in this change set: DIRECT is the only remaining way an account code can enter through
a payload, and once MAPPING exists it has no legitimate use in this architecture. Review it
explicitly and record the decision as an ADR — remove, deprecate, or restrict. Leaving it unexamined
is not acceptable.

### 2. Dimension tags on rule-driven lines — blocking

FIN v1 has dimensions on the chart of accounts and reports by dimension, but lines built by the rules
engine from an event carry no dimension values at all. Legacy stamps organisation and branch on every
journal entry. Without an equivalent, every event-sourced entry loses its branch and organisation
attribution, the parallel-run comparison between legacy and FIN diverges on every row (which makes
shadow-mode verification worthless), and FIN's own dimension reports are empty for exactly the
entries that matter most.

Requested: a rule line must be able to carry dimension values derived from the event, using the same
discipline as accounts — per dimension the value is either a constant or the content of a named
business field in the event (organisation code, branch code). The resolved value is validated against
FIN's existing dimension rules (belongs to that dimension, active); the existing validation is not
bypassed. Event-built and manually-built lines must end up with the same dimension shape so that
ledger and dimension reports treat them identically.

### 3. Return the duplicate-event error reliably — small

FIN checks the event reference for a duplicate and then inserts: check-then-act. When the same event
arrives twice at once the unique constraint catches the second, but it surfaces as a generic
data-integrity failure in the 500 class rather than as the duplicate-event error. For the consumer a
duplicate is success, not failure — it means the event was already posted and the message can be
acknowledged. A generic 500 looks like an outage: the consumer retries, the message lands in the
dead-letter queue, and someone reconciles by hand an event that posted correctly the first time.

Requested: a duplicate event reference must always answer the duplicate-event error, whether it was
caught by the pre-check or by the unique constraint.

### 4. Confirm the closed-period response — small

The consumer must treat "the period is closed" differently from every other rejection: it is an
operational state that resolves itself when someone opens the period, so the event should wait and be
retried rather than be marked failed.

Requested: confirm FIN answers a posting attempt into a non-open period with its own distinct,
catalogued error code, distinct from validation and configuration errors, and that the code is
documented for the consumer to depend on. If it does not exist, add it.

## Acceptance for this change set

1. A rule line resolves its account from a business value in the event, with no account code anywhere
   in the payload.
2. An unmapped business value produces a clear error and no posted entry.
3. Mappings are added and changed as data through a FIN screen, never in code.
4. Rule-driven lines carry validated dimension values derived from event fields.
5. A duplicate event reference always answers the duplicate-event error.
6. A closed-period rejection has its own distinct, documented code.
7. Every FIN v1 invariant still holds for entries produced through the new path — debits equal
   credits, leaf and active accounts only, the period gate at post time, positive amounts with
   direction, the single remainder line, duplicate rejection, derived balances, immutable audit
   trail. These are inherited, not reopened.

## Out of scope for this change set

- The in-database queue, the consumer service, and everything inside the legacy Oracle system.
- Seeding the actual account mappings for the migration: that is migration data, produced once the
  mapping store exists.
- A second event source or a broker: the consumer is built so adding one later is an added adapter,
  not a rebuild.
- The v1 exclusions stand unchanged — multi-currency, multi-ledger and multi-entity, intercompany,
  statistical accounts, multi-pattern calendar, attachments.
- The machine-caller authentication needed by the consumer is a SEC delta, tracked separately, not
  part of this change set.

## Per artifact

srs:
  ADDED    : ENT-FIN-015, ENT-FIN-016, REQ-FIN-047, REQ-FIN-048, REQ-FIN-049, REQ-FIN-050, REQ-FIN-051, REQ-FIN-052, REQ-FIN-053, REQ-FIN-054, REQ-FIN-055, REQ-FIN-056, REQ-FIN-057, REQ-FIN-058, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061, REQ-FIN-062, REQ-FIN-063, REQ-FIN-064, REQ-FIN-065, REQ-FIN-066, REQ-FIN-067, REQ-FIN-068, REQ-FIN-069, REQ-FIN-070, REQ-FIN-071, REQ-FIN-072, REQ-FIN-073, REQ-FIN-074, REQ-FIN-075, REQ-FIN-076, REQ-FIN-077, AC-FIN-047, AC-FIN-048, AC-FIN-049, AC-FIN-050, AC-FIN-051, AC-FIN-052, AC-FIN-053, AC-FIN-054, AC-FIN-055, AC-FIN-056, AC-FIN-057, AC-FIN-058, AC-FIN-059, AC-FIN-060, AC-FIN-061, AC-FIN-062, AC-FIN-063, AC-FIN-064, AC-FIN-065, AC-FIN-066, AC-FIN-067, AC-FIN-068, AC-FIN-069, AC-FIN-070, AC-FIN-071, AC-FIN-072, AC-FIN-073, AC-FIN-074, AC-FIN-075, AC-FIN-076, AC-FIN-077, AC-FIN-078, AC-FIN-079, AC-FIN-080, AC-FIN-081, AC-FIN-082, AC-FIN-083, RULE-FIN-018, RULE-FIN-019, RULE-FIN-020, RULE-FIN-021, RULE-FIN-022, RULE-FIN-023, RULE-FIN-024, RULE-FIN-025, RULE-FIN-026, RULE-FIN-027, RULE-FIN-028, SCR-REQ-FIN-013
  MODIFIED : ENT-FIN-010, REQ-FIN-045, AC-FIN-045, RULE-FIN-004, SCR-REQ-FIN-003, SCR-REQ-FIN-006, RULE-FIN-008
  REMOVED  : none
  UNCHANGED: every other v1 ENT / REQ / AC / RULE / SCR-REQ record (carried from v1 by gov.py state); A1, A2, A3 preamble, A6, A7, A8 and STANDALONE are restated in full (they carry no id to fold by)
registry-srs:
  MODIFIED : all
db-script:
  ADDED    : DBF-FIN-148, DBF-FIN-149, DBF-FIN-150, DBF-FIN-151, DBF-FIN-152, DBF-FIN-153, DBF-FIN-154, DBF-FIN-155, DBF-FIN-156, DBF-FIN-157, DBF-FIN-158, DBF-FIN-159, DBF-FIN-160, DBF-FIN-161, DBF-FIN-162, DBF-FIN-163, DBF-FIN-164, DBF-FIN-165
  MODIFIED : DBF-FIN-041, DBF-FIN-101, DBF-FIN-102, XM-FIN-001
  REMOVED  : none
  UNCHANGED: every other v1 DBF / XM definition (carried from v1 by gov.py state); the field matrix (§1) and the XM register (§2) are restated in full as current state; the SQL (§3) is a migration on v1
registry-db:
  MODIFIED : all
backend-execution-plan:
  ADDED    : API-FIN-038, API-FIN-039, API-FIN-040, API-FIN-041, QR-FIN-050, QR-FIN-051, QR-FIN-052, QR-FIN-053, QR-FIN-054, QR-FIN-055, QR-FIN-056, QR-FIN-057, QR-FIN-058, QR-FIN-059, QR-FIN-060, QR-FIN-061, ENT-FIN-015, ENT-FIN-016
  MODIFIED : API-FIN-001, API-FIN-002, API-FIN-003, API-FIN-004, API-FIN-005, API-FIN-006, API-FIN-007, API-FIN-008, API-FIN-009, API-FIN-010, API-FIN-011, API-FIN-012, API-FIN-013, API-FIN-014, API-FIN-015, API-FIN-016, API-FIN-017, API-FIN-018, API-FIN-019, API-FIN-020, API-FIN-021, API-FIN-022, API-FIN-023, API-FIN-024, API-FIN-025, API-FIN-026, API-FIN-027, API-FIN-028, API-FIN-029, API-FIN-030, API-FIN-031, API-FIN-032, API-FIN-033, API-FIN-034, API-FIN-035, API-FIN-036, API-FIN-037, QR-FIN-015, QR-FIN-025, QR-FIN-026, ENT-FIN-001, ENT-FIN-002, ENT-FIN-003, ENT-FIN-004, ENT-FIN-005, ENT-FIN-006, ENT-FIN-007, ENT-FIN-008, ENT-FIN-009, ENT-FIN-010, ENT-FIN-011, ENT-FIN-012, ENT-FIN-013, ENT-FIN-014, XM-FIN-001
  REMOVED  : none
  UNCHANGED: QR-FIN-001, QR-FIN-002, QR-FIN-003, QR-FIN-004, QR-FIN-005, QR-FIN-006, QR-FIN-007, QR-FIN-008, QR-FIN-009, QR-FIN-010, QR-FIN-011, QR-FIN-012, QR-FIN-013, QR-FIN-014, QR-FIN-016, QR-FIN-017, QR-FIN-018, QR-FIN-019, QR-FIN-020, QR-FIN-021, QR-FIN-022, QR-FIN-023, QR-FIN-024, QR-FIN-027, QR-FIN-028, QR-FIN-029, QR-FIN-030, QR-FIN-031, QR-FIN-032, QR-FIN-033, QR-FIN-034, QR-FIN-035, QR-FIN-036, QR-FIN-037, QR-FIN-038, QR-FIN-039, QR-FIN-040, QR-FIN-041, QR-FIN-042, QR-FIN-043, QR-FIN-044, QR-FIN-045, QR-FIN-046, QR-FIN-047, QR-FIN-048, QR-FIN-049 (restated byte-identical); XM-FIN-002 (carried by gov.py state)
registry-exec-be:
  MODIFIED : all
flow-diagram:
  MODIFIED : all
  REMOVED  : none
ui-ux-spec:
  ADDED    : SCR-FIN-013, UXD-FIN-013, UXD-FIN-014, UXD-FIN-015
  MODIFIED : SCR-FIN-001, SCR-FIN-002, SCR-FIN-003, SCR-FIN-004, SCR-FIN-005, SCR-FIN-006, SCR-FIN-007, SCR-FIN-008, SCR-FIN-009, SCR-FIN-010, SCR-FIN-011, SCR-FIN-012, UXD-FIN-007
  REMOVED  : none
  UNCHANGED: UXD-FIN-001, UXD-FIN-002, UXD-FIN-003, UXD-FIN-004, UXD-FIN-005, UXD-FIN-006, UXD-FIN-008, UXD-FIN-009, UXD-FIN-010, UXD-FIN-011, UXD-FIN-012 (restated byte-identical)
frontend-execution-plan:
  MODIFIED : all
  REMOVED  : none
registry-exec-fe:
  MODIFIED : all
backend-test-plan:
  ADDED    : TC-FIN-093, TC-FIN-094, TC-FIN-095, TC-FIN-096, TC-FIN-097, TC-FIN-098, TC-FIN-099, TC-FIN-100, TC-FIN-101, TC-FIN-102, TC-FIN-103, TC-FIN-104, TC-FIN-105, TC-FIN-106, TC-FIN-107, TC-FIN-108, TC-FIN-109, TC-FIN-110, TC-FIN-111, TC-FIN-112, TC-FIN-113, TC-FIN-114, TC-FIN-115, TC-FIN-116, TC-FIN-117, TC-FIN-118, TC-FIN-119, TC-FIN-120, TC-FIN-121, TC-FIN-122, TC-FIN-123, TC-FIN-124, TC-FIN-125, TC-FIN-126, TC-FIN-127, TC-FIN-128, TC-FIN-129
  MODIFIED : TC-FIN-001, TC-FIN-002, TC-FIN-003, TC-FIN-004, TC-FIN-005, TC-FIN-006, TC-FIN-007, TC-FIN-008, TC-FIN-009, TC-FIN-010, TC-FIN-011, TC-FIN-012, TC-FIN-013, TC-FIN-014, TC-FIN-015, TC-FIN-016, TC-FIN-017, TC-FIN-018, TC-FIN-019, TC-FIN-020, TC-FIN-021, TC-FIN-022, TC-FIN-023, TC-FIN-024, TC-FIN-025, TC-FIN-026, TC-FIN-027, TC-FIN-028, TC-FIN-029, TC-FIN-030, TC-FIN-031, TC-FIN-032, TC-FIN-033, TC-FIN-034, TC-FIN-035, TC-FIN-036, TC-FIN-037, TC-FIN-038, TC-FIN-039, TC-FIN-040, TC-FIN-041, TC-FIN-042, TC-FIN-043, TC-FIN-044, TC-FIN-045, TC-FIN-046, TC-FIN-047, TC-FIN-048, TC-FIN-049, TC-FIN-050, TC-FIN-051, TC-FIN-052
  REMOVED  : none
  NOTE     : content changed in TC-FIN-008, TC-FIN-010, TC-FIN-012, TC-FIN-044 and TC-FIN-045 (first run, ADR-FIN-063) and in TC-FIN-019 and TC-FIN-021 (re-run, ADR-FIN-066: the line-naming half of their ACs is stated as failing against the published error body); TC-FIN-049 changes at its record boundary (the next SUB's START traces grew); the other v1 cases are restated word for word and listed MODIFIED because ids-continue compares each START-marker record with the baseline heading record (ADR-FIN-063); of the ADDED cases the re-run changed TC-FIN-127, whose catalogue is now republished (ADR-FIN-064)
frontend-test-plan:
  ADDED    : TC-FIN-130, TC-FIN-131, TC-FIN-132, TC-FIN-133, TC-FIN-134, TC-FIN-135, TC-FIN-136, TC-FIN-137, TC-FIN-138, TC-FIN-139, TC-FIN-140, TC-FIN-141, TC-FIN-142, TC-FIN-143, TC-FIN-144, TC-FIN-145
  MODIFIED : TC-FIN-053, TC-FIN-054, TC-FIN-055, TC-FIN-056, TC-FIN-057, TC-FIN-058, TC-FIN-059, TC-FIN-060, TC-FIN-061, TC-FIN-062, TC-FIN-063, TC-FIN-064, TC-FIN-065, TC-FIN-066, TC-FIN-067, TC-FIN-068, TC-FIN-069, TC-FIN-070, TC-FIN-071, TC-FIN-072, TC-FIN-073, TC-FIN-074, TC-FIN-075, TC-FIN-076, TC-FIN-077, TC-FIN-078, TC-FIN-079, TC-FIN-080, TC-FIN-081, TC-FIN-082, TC-FIN-083, TC-FIN-084, TC-FIN-085, TC-FIN-086, TC-FIN-087, TC-FIN-088, TC-FIN-089, TC-FIN-090, TC-FIN-091, TC-FIN-092
  REMOVED  : none
  NOTE     : content unchanged only in TC-FIN-065, TC-FIN-066, TC-FIN-075, TC-FIN-077, TC-FIN-078, TC-FIN-087, TC-FIN-088, TC-FIN-089 and TC-FIN-091 (TC-FIN-091 changes at its record boundary, the INT-FLOW START traces having grown); every other v1 case changed — TC-FIN-059, TC-FIN-060, TC-FIN-061 and TC-FIN-079 in the first run (ADR-FIN-063), the rest in the re-run that aligns the cases with the frontend plan as ADR-FIN-064 and ADR-FIN-065 revised it (record pages, no client-side action gating, line refusals in the lines pane, independent income-statement bounds, group drill-down — ADR-FIN-066); the ADDED cases TC-FIN-130 … TC-FIN-142 and TC-FIN-145 no longer wait on publication
test-execution-manifest:
  MODIFIED : all
  REMOVED  : none

<<<END INPUT>>>

---
# KNOWLEDGE (profile primary sources — cite as [KB:<file> §n])

<<<KB: profiles/erp/knowledge/erp-domain-standards.md>>>
# ERP Domain Standards — knowledge base for the `erp` profile

```
Profile   : erp            (profiles/erp.yaml → knowledge.files)
Role      : PRIMARY SOURCE the engines may cite (domain-profile, P0, P1, P2, P3.x)
            when they resolve an ambiguity themselves (factory.yaml → ambiguity).
Replaces  : the former "platform-standards.md Section M" that engines referenced
            but that never existed in the factory.
Rule      : a citation to this file is written as [KB:erp-domain-standards §n].
```

## §1 Module tiers
| Tier | Purpose | Typical modules |
|---|---|---|
| Tier 0 — Foundation | must exist before any business module | Organization (ORG), Security (SEC), Master Data Lookup (MDL) |
| Tier 1 — Core business | first revenue/cost flows | Procurement (PRC), Finance (FIN), Inventory (INV) |
| Tier 2 — Extended business | depends on Tier 1 | Sales (SLS), Contracts (CTR), Human Resources (HR) |

A module may only declare a HARD-FK XM towards a module of the same or a lower tier.

## §2 Entity kinds and defaults
Entity kinds and their default fields are declared in `profiles/erp.yaml → conventions.entity_defaults`.
Rules the engines apply on top:
1. Every master entity is bilingual (`nameAr`, `nameEn`) and soft-deletable (`isActiveFl`).
2. Transactional documents are period-bound (`fiscalYearId`, `periodId`) and status-driven (`statusCode` from a lookup).
3. Lookups are owned by MDL; a module never stores a lookup's display text, only its code.
4. No entity generates its own document numbers — the platform numbering engine does.

## §3 Business-policy conventions (P0 → POL-*)
- A policy is a single, testable sentence in EARS form (see factory.yaml → ids.ears).
- Policies that cross modules are declared once, in the owning (lower-tier) module, and referenced by code elsewhere.
- Fiscal policies (period locking, posting rules) belong to FIN; approval-limit policies belong to the module that owns the document.

## §4 Screens, security and permissions
- Composite screens: Search + Entry (or Master + Detail, Wizard) = ONE `SCR-*` and ONE `SEC_PAGES` row.
- Permission pattern and gateway action: `profiles/erp.yaml → conventions.security_model`.
- Backend: one controller per composite screen; authorization per method (gateway action on reads; CREATE/UPDATE/DELETE on mutations).
- Frontend: one lazily-loaded chunk per composite screen; Search↔Entry via route params.

## §5 Cross-module dependencies (XM)
- `HARD-FK`: a physical foreign key to another module's table — allowed only downward in tier.
- `SOFT-READ`: a read-only lookup by code — allowed in any direction.
- Every XM cites the `REQ-*` that needs it; the consuming module owns the XM record.

## §6 Defaults an engine may assume without asking (after PRD approval)
| Question | Default |
|---|---|
| Soft delete vs hard delete | soft (`isActiveFl`) |
| Audit trail | the four audit fields on every table |
| Paging | server-side, page size 20, max 200 |
| Search | server-side filter on code/name (both languages) |
| Money | `NUMERIC(18,4)`, currency code from MDL |
| Dates | `TIMESTAMPTZ`, stored UTC, displayed in tenant timezone |

Anything not covered here becomes an ADR (`decisions/<MOD>/`) per the ambiguity rule.

<<<END KB>>>
