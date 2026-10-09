<!-- source: PHASE:CORE -->
<!-- traces: REQ-FIN-017, REQ-FIN-045, REQ-FIN-047, REQ-FIN-072, REQ-FIN-075 -->
<!-- PHASE:CORE:START traces=REQ-FIN-017,REQ-FIN-045,REQ-FIN-047,REQ-FIN-072,REQ-FIN-075 -->
## PHASE 1 — CORE

**Layers**: controller → service → mapper → domain → repository (`profile.stack.backend.layers`).
Domain-behaviour placement: **domain classes** — one dedicated `<Entity>Domain` per entity — for every rule that
answers "is this operation allowed?" (`profile.conventions.domain_behaviour_placement: domain_classes`). The service
layer orchestrates only (load → delegate → persist → return) and holds no business-rule conditional of its own. How
many entities a rule reads is not a placement criterion: the service fetches those facts and passes them to the
domain class as plain arguments. The decision sits in the domain class; the entity keeps the plain state mutation
(`JournalEntryDomain.assertCanPost(...)` then `JournalEntry.post()`).

FIN domain classes and the rules each owns — v1, then the v2 additions:
- `AccountDomain` — RULE-FIN-001, RULE-FIN-007
- `DimensionValueDomain` — RULE-FIN-002, RULE-FIN-009; v2: RULE-FIN-027 (`assertResolvedByCode`)
- `EventTypeRuleDomain` — RULE-FIN-003, RULE-FIN-005, RULE-FIN-010; v2: RULE-FIN-018, RULE-FIN-019, RULE-FIN-021,
  RULE-FIN-025, RULE-FIN-026 (the rule line, its tags, and the event's business fields the line reads)
- `JournalEntryDomain` — RULE-FIN-004, RULE-FIN-006, RULE-FIN-008, RULE-FIN-011, RULE-FIN-012, RULE-FIN-013,
  RULE-FIN-016, RULE-FIN-017
- `FiscalPeriodDomain` — RULE-FIN-014; v2: RULE-FIN-028 (`assertDateCovered`)
- `FiscalYearDomain` — the year-end re-run guard (API-FIN-027)
- `AllocationRuleDomain`, `RecurringTemplateDomain` — RULE-FIN-003 and RULE-FIN-010 for allocation targets; the
  `assertCanRun()` gate of 2026-09-12 (a recorded human decision, not a RULE)
- v2 `AccountMappingDomain` (NEW, ENT-FIN-015) — RULE-FIN-020 (`assertResolved`), RULE-FIN-022 (`assertKeyFree`),
  and also RULE-FIN-023 (`assertAccountEligible`) and RULE-FIN-024 (`assertBusinessValueDefined`)

RULE-FIN-015 has no domain owner. Its data source is DEFERRED, and it is satisfied entirely by SEC's permission
gate: `@PreAuthorize(PERM_FIN_PERIODS_CLOSE_APPROVE)` on API-FIN-026 and API-FIN-027.

**Error signalling**: `LocalizedException → {code, messageAr, messageEn}`. Runtime code format, verbatim from
profile.stack.backend.api.error_code_format: `{MOD}-{http}[-{SLUG}]`, which for this module reads
`FIN-{http}[-{SLUG}]`. Every Error Catalog row is an instance of it, and its HTTP cell is one of the statuses the
platform emits (200, 201, 400, 401, 403, 404, 409, 413, 415, 422, 500).

**Store refusals (v2, ADR-FIN-030)**: a service never catches `DataIntegrityViolationException`. Two v2 rules need a
uniqueness refusal answered as a business error rather than as a server error: RULE-FIN-004 on
UQ_FIN_JOURNAL_ENTRY_EVENT_REF and RULE-FIN-022 on UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY. FIN therefore gains one
module-wide `FinPersistenceRefusalAdvisor`, placed exactly like the delivered `FinForbiddenAdvisor`: one
`DefaultPointcutAdvisor` over `com.erp.fin.service.*`, ordered outside the transaction advisor so a refusal raised at
flush or at commit passes through it, never a catch inside a service and never a feature `@ControllerAdvice`. It
reads the violated constraint's name and re-raises `LocalizedException(CONFLICT, …)` from a fixed registry:
UQ_FIN_JOURNAL_ENTRY_EVENT_REF → `FIN-409-DUPLICATE-EVENT`, UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY →
`FIN-409-MAPPING-DUP`. Any other constraint name is re-thrown unchanged. The two inserts concerned are flushed at
once, so the refusal happens inside the service call.

**Transaction scope**: `READ_ONLY` for every `FIND_*`/`EXISTS`/`AGGREGATE` query; `READ_WRITE` for every
`SAVE`/`UPDATE`. The build → validate → post sequence runs in ONE transaction per entry: either the whole entry
posts or nothing is written. That holds for every v2 event rejection too (SRS A7): no row exists for a rejected event.
Adding a rule line writes the line and its tags in one transaction.

**Search contract**: `{filters, page, size, sort}` in the request body, `Page<T>` in `ApiResponse<T>`, an empty
result is success. Allowed sort fields are the screen's own filter and result columns (SRS §B2), never widened.
Paging: server-side, size 20 by default, 200 at most [KB:erp-domain-standards §6].

**Audit fields**: `createdBy/createdAt/updatedBy/updatedAt` are filled by the platform, never by a request. On
FIN_FISCAL_PERIOD, `closedBy`/`closedAt` record the hard-close approver (API-FIN-026).

**Type mapping** (postgresql16 → Java, from profile.stack.db.syntax_map; column types only — PK generation is not a
type):

| postgresql16 | Java | Notes |
|---|---|---|
| BIGINT | Long | every PK — a plain column filled from its table's own `SEQ_{TABLE}` sequence, no identity clause — and every FK |
| VARCHAR(n) | String | every lookup-backed column holds the MDL code |
| TEXT | String | descriptions, rule-line values |
| BOOLEAN | Boolean | every `…Fl` flag |
| TIMESTAMPTZ | Instant | audit and system timestamps, stored UTC |
| DATE | LocalDate | document and period dates |
| NUMERIC(18,4) | BigDecimal | money [KB:erp-domain-standards §6] |
| NUMERIC | Integer | line, period and sort numbers |

**Lookup values**: every lookup-backed column is a plain `String` holding the code, validated in the service layer
against MDL (XM-FIN-001) before any write. v2: 15 FIN-owned keys. Two keys have a v2 rule of their own instead of the
generic `FIN-400-INVALID-LOOKUP`: ACCOUNT_DERIVATION_TYPE (RULE-FIN-018) and PAYMENT_METHOD when it is an account
mapping's business value (RULE-FIN-024).

**Event business fields (v2, ADR-FIN-031)**: the canonical event names each business fact by its
FIN_EVENT_BUSINESS_FIELD code, as a key of the request's existing `fields` map (for example
`fields.PAYMENT_METHOD = "CHEQUE"`, `fields.BRANCH_CODE = "BR-07"`). The rules engine reads a business field only
through that code: a MAPPING rule line names it in accountBusinessFieldCode, and a BUSINESS_FIELD tag names it in
businessFieldCode. Nothing in the payload is read as an account code (REQ-FIN-047).

**Numbering**: `docNo` format is `JV-{fiscalYearCode}-{NNNNNN}` — literal prefix `JV-`, the owning fiscal year's
`code` (DBF-FIN-066, VARCHAR(10)), `-`, then a zero-padded 6-digit counter starting at `000001`; e.g.
`JV-2026-000123`, at most 20 characters inside `doc_no VARCHAR(30)`. The counter is scoped per `fiscalYearId` and
restarts each year; one counter serves every journal type. `UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO (fiscal_year_id, doc_no)`
backs it in the store. A FIN-local generator in `com.erp.fin` produces it, deliberately not a shared `com.erp.common`
component. This is a recorded deviation from the profile's guidance that document numbers come from the platform
numbering engine: no such engine exists in this platform. The number is assigned once on create, never appears in a
request, and is present in every response. v2 adds no business code.

**Workflow engine**: forbidden — every lifecycle (JournalEntry, FiscalPeriod, FiscalYear) is a plain guarded
transition.

**Languages**: every name field and every catalog message exists in ar and en. v2 adds no stored name: an account
mapping is labelled by its account's `nameAr`/`nameEn`.

**Cross-module contract placement**: XM-FIN-001 (SOFT-READ → MDL) is FIN's service layer injecting MDL's published
in-process interface `com.erp.mdl.crossmodule.MdlLookupApi` and calling `readActiveValuesByKey(typeKey)`; it
consumes `LookupOptionView` (code, labelAr, labelEn, sortOrder), never an MDL entity. One deployable
(`profile.conventions.module_interface: in_process`): no HTTP client, no base path, no network failure path. FIN's
dependency on SEC for identity and authorization is the platform interceptor that SEC's and MDL's own plans declare,
cited here and not redeclared (ADR-FIN-001).

**Cross-cutting authorization**: every secured endpoint is gated before its method body runs by a `@PreAuthorize`
naming one `PERM_<PAGE_CODE>_<ACTION>` from `PermissionConstants`. A denial is re-raised as `FIN-403-FORBIDDEN` by
`FinForbiddenAdvisor`. RULE-FIN-015 is satisfied by the distinct `PERM_FIN_PERIODS_CLOSE_APPROVE` gate alone. The
earlier sentence here, which specified a FIN service-layer check that no single user holds both close-approval and
entry creation, was the origin of the SoD over-implementation removed on 2026-09-12; it is withdrawn and must not be
reinstated (SEC-BE).
<!-- PHASE:CORE:END -->
