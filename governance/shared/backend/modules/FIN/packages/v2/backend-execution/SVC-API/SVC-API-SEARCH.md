<!-- source: PHASE:SVC-API / SUB:SVC-API-SEARCH -->
<!-- context: SVC-API-HEADER.md — phase-level preamble -->
<!-- traces: DBF-FIN-002, DBF-FIN-003, DBF-FIN-004, DBF-FIN-005, DBF-FIN-006, DBF-FIN-015, DBF-FIN-016, DBF-FIN-017, DBF-FIN-024, DBF-FIN-025, DBF-FIN-026, DBF-FIN-027, DBF-FIN-034, DBF-FIN-035, DBF-FIN-036, DBF-FIN-040, DBF-FIN-041, DBF-FIN-054, DBF-FIN-055, DBF-FIN-056, DBF-FIN-063, DBF-FIN-064, DBF-FIN-076, DBF-FIN-077, DBF-FIN-080, DBF-FIN-081, DBF-FIN-082, DBF-FIN-090, DBF-FIN-091, DBF-FIN-092, DBF-FIN-110, DBF-FIN-111, DBF-FIN-112, DBF-FIN-131, DBF-FIN-132, DBF-FIN-133, DBF-FIN-150, DBF-FIN-151, DBF-FIN-152, DBF-FIN-153, DBF-FIN-154, REQ-FIN-001, REQ-FIN-004, REQ-FIN-005, REQ-FIN-007, REQ-FIN-016, REQ-FIN-022, REQ-FIN-025, REQ-FIN-027, REQ-FIN-031, REQ-FIN-039, REQ-FIN-040, REQ-FIN-041, REQ-FIN-042, REQ-FIN-043, REQ-FIN-046, REQ-FIN-055, REQ-FIN-070, REQ-FIN-071 -->
<!-- SUB:SVC-API-SEARCH:START traces=REQ-FIN-001,REQ-FIN-004,REQ-FIN-007,REQ-FIN-022,REQ-FIN-027,REQ-FIN-031,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-055,REQ-FIN-071 -->
### SUB — SVC-API-SEARCH (read-only)

<!-- API:API-FIN-001:START traces=REQ-FIN-001,DBF-FIN-002,DBF-FIN-003,DBF-FIN-004,DBF-FIN-005 -->
### API-FIN-001 — search accounts
Entity       : ENT-FIN-001 · operation VIEW (search)
Endpoint: POST /api/v1/fin/accounts/search · Layers: `AccountController.search`→`AccountService.search`
Request: body `AccountSearchRequest` — `code`(LIKE), `nameAr/nameEn`(LIKE), `accountTypeCode`(EXACT), `isActiveFl`(EXACT), paging
Response: 200 · `Page<AccountResponse>` · `ApiResponse<...>`
Validations: none (read-only) · Errors: `FIN-500`
Orchestration: load (QR-FIN-001) → map → return · Repository: QR-FIN-001 · join NONE · READ_ONLY
Security: screen FIN_ACCOUNTS · `PERM_FIN_ACCOUNTS_VIEW` · Localization: nameAr/nameEn returned
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-001:END -->

<!-- API:API-FIN-005:START traces=REQ-FIN-004,DBF-FIN-015,DBF-FIN-016,DBF-FIN-017 -->
### API-FIN-005 — search dimensions
Entity       : ENT-FIN-002 · operation VIEW (search)
Endpoint: POST /api/v1/fin/dimensions/search · Layers: `DimensionController.search`→`DimensionService.search`
Request: body `DimensionSearchRequest` — `code`(LIKE), paging · Response: 200 · `Page<DimensionResponse>`
Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-007 → map → return · Repository: QR-FIN-007 · join NONE · READ_ONLY
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-005:END -->

<!-- API:API-FIN-008:START traces=REQ-FIN-005,DBF-FIN-024,DBF-FIN-025,DBF-FIN-026,DBF-FIN-027 -->
### API-FIN-008 — search dimension values
Entity       : ENT-FIN-003 · operation VIEW (search)
Endpoint: POST /api/v1/fin/dimensions/values/search · Layers: `DimensionController.searchDimensionValues`→`DimensionValueService.search`
Request: body `DimensionValueSearchRequest` — `dimensionId`(EXACT, required) carried in the body filters and read by the child parent-id extractor (never a path variable); `code`(LIKE), paging · Response: 200 · `Page<DimensionValueResponse>`
Validations: none · Errors: `FIN-404-DIMENSION`
Orchestration: QR-FIN-011 → map → return · Repository: QR-FIN-011 · join NONE · READ_ONLY
Security: screen FIN_DIMENSIONS · `PERM_FIN_DIMENSIONS_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-008:END -->

<!-- API:API-FIN-009:START traces=REQ-FIN-007,DBF-FIN-090,DBF-FIN-091,DBF-FIN-092 -->
### API-FIN-009 — search event-type rules
Entity       : ENT-FIN-009 · operation VIEW (search)
Endpoint: POST /api/v1/fin/event-rules/search · Layers: `EventTypeRuleController.search`→`EventTypeRuleService.search`
Request: body `EventTypeRuleSearchRequest` — `eventTypeCode`(EXACT), `isActiveFl`(EXACT), paging · Response: 200 · `Page<EventTypeRuleResponse>`
Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-012 → map → return · Repository: QR-FIN-012 · join NONE · READ_ONLY
Security: screen FIN_RULES · `PERM_FIN_RULES_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-009:END -->

<!-- API:API-FIN-012:START traces=REQ-FIN-022,DBF-FIN-110,DBF-FIN-111,DBF-FIN-112 -->
### API-FIN-012 — search templates
Entity       : ENT-FIN-011, ENT-FIN-012 · operation VIEW (search — each template is returned with its lines)
Endpoint: POST /api/v1/fin/recurring-templates/search · Layers: `RecurringTemplateController.search`→`RecurringTemplateService.search`
Request: body `RecurringTemplateSearchRequest` — `nameAr/nameEn`(LIKE), `scheduleTypeCode`(EXACT), `isActiveFl`(EXACT), paging
Response: 200 · `Page<RecurringTemplateResponse>` · Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-017 → map → return · Repository: QR-FIN-017 · join NONE · READ_ONLY
Security: screen FIN_RECURRING_TEMPLATES · `PERM_FIN_RECURRING_TEMPLATES_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-012:END -->

<!-- API:API-FIN-015:START traces=REQ-FIN-025,DBF-FIN-131,DBF-FIN-132,DBF-FIN-133 -->
### API-FIN-015 — search allocation rules
Entity       : ENT-FIN-013, ENT-FIN-014 · operation VIEW (search — each rule is returned with its targets)
Endpoint: POST /api/v1/fin/allocation-rules/search · Layers: `AllocationRuleController.search`→`AllocationRuleService.search`
Request: body `AllocationRuleSearchRequest` — `nameAr/nameEn`(LIKE), `sourceAccountId`(EXACT), `isActiveFl`(EXACT), paging
Response: 200 · `Page<AllocationRuleResponse>` · Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-020 → map → return · Repository: QR-FIN-020 · join NONE · READ_ONLY
Security: screen FIN_ALLOCATION_RULES · `PERM_FIN_ALLOCATION_RULES_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-015:END -->

<!-- API:API-FIN-018:START traces=REQ-FIN-027,DBF-FIN-035,DBF-FIN-036,DBF-FIN-040 -->
### API-FIN-018 — search journal entries
Entity       : ENT-FIN-004 · operation VIEW (search)
Endpoint: POST /api/v1/fin/journal-entries/search · Layers: `JournalEntryController.search`→`JournalEntryService.search`
Request: body `JournalEntrySearchRequest` — `docNo`(LIKE), `docDate`(DATE_RANGE), `periodId`(EXACT), `statusCode`(EXACT), `journalTypeCode`(EXACT), paging
Response: 200 · `Page<JournalEntryResponse>` · Validations: none · Errors: `FIN-500`
Orchestration: QR-FIN-023 → map → return (REQ-FIN-027: unmodified) · Repository: QR-FIN-023 · join NONE · READ_ONLY
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_VIEW` · Localization: descriptionAr/En
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-018:END -->

<!-- API:API-FIN-022:START traces=REQ-FIN-016,REQ-FIN-027,DBF-FIN-034,DBF-FIN-041,REQ-FIN-070 -->
### API-FIN-022 — read entry
Entity       : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 · operation VIEW (read one entry with its lines and their dimensions)
Endpoint: GET /api/v1/fin/journal-entries/{id} · Layers: `JournalEntryController.read`→`JournalEntryService.read`
Request: path `id` · Response: 200 · `JournalEntryResponse` with nested lines and their dimensions
Validations: none · Errors: `FIN-404-ENTRY`
Orchestration: QR-FIN-037 → map → return · Repository: QR-FIN-037 · join intra-module (entry→line→line-dim) · READ_ONLY
Security: screen FIN_JOURNAL_ENTRIES · `PERM_FIN_JOURNAL_ENTRIES_VIEW` · Localization: descriptionAr/En
Concurrency  : NONE — read-only; nothing is allocated or written
v2: an event-built line returns its dimension values as JournalLineDimension rows in exactly the form a manual line does (REQ-FIN-070, AC-FIN-075). Nothing changes in this endpoint, which already returns the rows it finds.
<!-- API:API-FIN-022:END -->

<!-- API:API-FIN-028:START traces=REQ-FIN-039,REQ-FIN-046,DBF-FIN-054,DBF-FIN-055,DBF-FIN-056,REQ-FIN-071 -->
### API-FIN-028 — account ledger
Entity       : ENT-FIN-005 · operation VIEW (read — account ledger report)
Endpoint: GET /api/v1/fin/reports/account-ledger · Layers: `ReportController.accountLedger`→`ReportService.accountLedger`
Request: `accountId`(EXACT), date range, dimension filters · Response: 200 · running-balance list, each row linking journalEntryId
Validations: none · Errors: `FIN-404-ACCOUNT`
Orchestration: (v2: event-built and manual lines are read alike — REQ-FIN-071) QR-FIN-042 (POSTED lines only, live) → compute running balance → return (POL-FIN-009)
Repository: QR-FIN-042 · join intra-module (line→entry for docDate/status) · READ_ONLY
Security: screen FIN_ACCOUNT_LEDGER · `PERM_FIN_ACCOUNT_LEDGER_VIEW` · Localization: n/a
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-028:END -->

<!-- API:API-FIN-029:START traces=REQ-FIN-040,REQ-FIN-046,DBF-FIN-005,DBF-FIN-006,DBF-FIN-055 -->
### API-FIN-029 — trial balance
Entity       : ENT-FIN-005 · operation VIEW (read — trial balance report)
Endpoint: GET /api/v1/fin/reports/trial-balance · Layers: `ReportController.trialBalance`→`ReportService.trialBalance`
Request: `periodId`(EXACT), `accountTypeCode`(EXACT) · Response: 200 · one row per account (debit/credit balance, sign per natureCode — POL-FIN-002)
Validations: RULE-FIN-006 restated as a report-level guarantee (POL-FIN-008: the sums always match because every contributing entry individually balanced — no separate check needed, an invariant by construction). `periodId` is an OPTIONAL narrowing, validated ONLY when supplied: omitting it means "no period narrowing" and stays a 200 (AC-FIN-040's happy path) — it is NOT mandatory
Errors: `FIN-404-PERIOD` (supplied `periodId` does not resolve), `FIN-500`
Orchestration: QR-FIN-043 (POSTED lines only, live, grouped by account) → apply nature sign → return
Repository: QR-FIN-043 · join intra-module · READ_ONLY
Security: screen FIN_TRIAL_BALANCE · `PERM_FIN_TRIAL_BALANCE_VIEW` · Localization: nameAr/nameEn per account
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-029:END -->

<!-- API:API-FIN-030:START traces=REQ-FIN-041,REQ-FIN-046,DBF-FIN-005,DBF-FIN-055 -->
### API-FIN-030 — balance sheet
Entity       : ENT-FIN-005 · operation VIEW (read — balance sheet report)
Endpoint: GET /api/v1/fin/reports/balance-sheet · Layers: `ReportController.balanceSheet`→`ReportService.balanceSheet`
Request: `fiscalYearId`(EXACT), `asOfDate` · Response: 200 · grouped ASSET/LIABILITY/EQUITY balances
Validations: none (continuity itself is guaranteed by REQ-FIN-036's opening-entry generation, not re-validated at read time). The REQUIRED `fiscalYearId` is resolved FIRST, before any aggregation
Errors: `FIN-404-YEAR` (unknown `fiscalYearId` — previously a silent 200 carrying an all-zero statement), `FIN-500`
Orchestration: QR-FIN-043 (accountTypeCode IN ASSET,LIABILITY,EQUITY) → group → return
Repository: QR-FIN-043 · join intra-module · READ_ONLY
Security: screen FIN_BALANCE_SHEET · `PERM_FIN_BALANCE_SHEET_VIEW` · Localization: nameAr/nameEn per account
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-030:END -->

<!-- API:API-FIN-031:START traces=REQ-FIN-042,REQ-FIN-046,DBF-FIN-005,DBF-FIN-055 -->
### API-FIN-031 — income statement
Entity       : ENT-FIN-005 · operation VIEW (read — income statement report)
Endpoint: GET /api/v1/fin/reports/income-statement · Layers: `ReportController.incomeStatement`→`ReportService.incomeStatement`
Request: `fiscalYearId`(EXACT), period range · Response: 200 · grouped REVENUE/EXPENSE balances
Validations: none (zero-opening is guaranteed by REQ-FIN-036 closing result accounts to Retained Earnings, not re-validated at read time). The REQUIRED `fiscalYearId` is resolved FIRST; the two OPTIONAL period bounds `fromPeriodId`/`toPeriodId` are validated only when supplied, as they already were
Errors: `FIN-404-YEAR` (unknown `fiscalYearId`), `FIN-404-PERIOD` (supplied period bound does not resolve — unchanged), `FIN-500`
Orchestration: QR-FIN-043 (accountTypeCode IN REVENUE,EXPENSE, scoped to the year/period range) → group → return
Repository: QR-FIN-043 · join intra-module · READ_ONLY
Security: screen FIN_INCOME_STATEMENT · `PERM_FIN_INCOME_STATEMENT_VIEW` · Localization: nameAr/nameEn per account
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-031:END -->

<!-- API:API-FIN-032:START traces=REQ-FIN-043,DBF-FIN-063,DBF-FIN-064,DBF-FIN-055,REQ-FIN-071 -->
### API-FIN-032 — dimension report
Entity       : ENT-FIN-005, ENT-FIN-006 · operation VIEW (read — dimension report)
Endpoint: GET /api/v1/fin/reports/dimension · Layers: `ReportController.dimensionReport`→`ReportService.dimensionReport`
Request: `dimensionId`(EXACT), `dimensionValueId`(EXACT), `periodId`(EXACT) · Response: 200 · one row per account+dimension-value combination (POL-FIN-011)
Validations: none · Errors: `FIN-404-DIMENSION`
Orchestration: (v2: event-built lines carry their JournalLineDimension rows in the v1 shape, so this grouping covers them unchanged — REQ-FIN-071, AC-FIN-076) QR-FIN-044 (group by account + dimension value, never base account alone) → return
Repository: QR-FIN-044 · join intra-module (line→line-dim→dimension-value) · READ_ONLY
Security: screen FIN_DIMENSION_REPORTS · `PERM_FIN_DIMENSION_REPORTS_VIEW` · Localization: nameAr/nameEn
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-032:END -->

<!-- API:API-FIN-033:START traces=REQ-FIN-031,DBF-FIN-076,DBF-FIN-077,DBF-FIN-080,DBF-FIN-081,DBF-FIN-082 -->
### API-FIN-033 — search fiscal periods
Entity       : ENT-FIN-008 · operation VIEW (search)
Endpoint: POST /api/v1/fin/fiscal-periods/search · Layers: `FiscalPeriodController.search`→`FiscalPeriodService.search`
Request: body `FiscalPeriodSearchRequest` — `fiscalYearId`(EXACT, **OPTIONAL**, DBF-FIN-076) carried in the body filters and read by the child parent-id extractor (never a path variable); `statusCode`(EXACT, DBF-FIN-082), paging/sort · Response: 200 · `Page<FiscalPeriodResponse>`
Validations: none (read-only) · Errors: `FIN-400-INVALID-SORT`, `FIN-500`
Orchestration: build the generic specification from the remaining filters → AND in an explicit join predicate on `fiscalYear.fiscalYearPk` ONLY when `fiscalYearId` is present → page → map → return
Repository: `FiscalPeriodRepository` via `JpaSpecificationExecutor.findAll(Specification, Pageable)` — no QR id is assigned; the Query Reference Catalog closes at QR-FIN-049 and extending it is a catalog-level change left to ALIGN · join intra-module (period→year, only when the parent filter is supplied) · READ_ONLY
Security: screen FIN_PERIODS · `PERM_FIN_PERIODS_VIEW` · Localization: nameAr/nameEn per period
Parent id OPTIONAL — deliberate divergence from API-FIN-008, which rejects a missing `dimensionId` with `FIN-404-DIMENSION`. SCR-REQ-FIN-007 §B2 makes both filters EXACT but neither mandatory, and the endpoint exists precisely so a client that did NOT create the fiscal year in the same session can discover a period id: requiring the year id first would leave that client with no way in. Same shape API-FIN-018 already uses for its optional `periodId`.
Why it exists: `JournalEntryCreateRequest` requires `fiscalYearId` + `periodId` (API-FIN-019) and API-FIN-029/030/031 require a period or year id, yet before this endpoint no API returned a fiscal period except API-FIN-023's create response. `PERM_FIN_PERIODS_VIEW` was already a V24 registry row and already granted by V25/V27, so **no migration was needed** — only the matching `PermissionConstants` constant was added.
Concurrency  : NONE — read-only; nothing is allocated or written
<!-- API:API-FIN-033:END -->

<!-- API:API-FIN-038:START traces=REQ-FIN-055,DBF-FIN-150,DBF-FIN-151,DBF-FIN-152,DBF-FIN-153,DBF-FIN-154 -->
### API-FIN-038 — search account mappings
Entity       : ENT-FIN-015 · operation VIEW (search)
Endpoint     : POST /api/v1/fin/account-mappings/search   verb: POST — FIN's house style for a search with a request body (SRS SCR-REQ-FIN-013 §B5)
Layers       : `AccountMappingController.search` → `AccountMappingService.search`
Request      : body `AccountMappingSearchRequest` — filters eventTypeCode (EXACT), businessFieldCode (EXACT), businessValue (LIKE), accountId (EXACT), isActiveFl (EXACT), all optional; `page`, `size` (20 by default, 200 at most), `sort` limited to those five result columns (SRS §B2); nothing is written
Filters      : eventTypeCode → DBF-FIN-150 · businessFieldCode → DBF-FIN-151 · businessValue → DBF-FIN-152 · accountId → DBF-FIN-153 · isActiveFl → DBF-FIN-154
Response     : 200 · `ApiResponse<Page<AccountMappingResponse>>` — accountMappingPk, eventTypeCode, businessFieldCode, businessValue, accountId, accountCode, accountNameAr, accountNameEn, isActiveFl and the audit fields; an empty page is success, never "not found" (AC-FIN-059)
Validations  : the search contract only — a sort field outside the five → FIN-400-INVALID-SORT
Errors       : FIN-400-INVALID-SORT, FIN-403-FORBIDDEN, FIN-500
Orchestration: build the specification from the filters supplied → page (QR-FIN-050) → map; the account's code and names come from the joined account row (ADR-FIN-031) → return
Repository   : QR-FIN-050 · FIND_BY_CRITERIA · join FIN_ACCOUNT for the display columns (ADR-FIN-031) · READ_ONLY
Concurrency  : NONE — read-only
Security     : screen FIN_ACCOUNT_MAPPINGS · `PERM_FIN_ACCOUNT_MAPPINGS_VIEW` (the gateway), checked by `@PreAuthorize` before processing
Localization : the account's nameAr and nameEn are both returned; lookup values travel as codes and the frontend labels them from MDL
<!-- API:API-FIN-038:END -->

**404 on a keying id, as built (API-FIN-029/030/031).** FIN's house style already 404s on the id
that keys a report — API-FIN-028 answers `FIN-404-ACCOUNT`, API-FIN-032 answers
`FIN-404-DIMENSION`. API-FIN-030 and API-FIN-031 now do the same for their REQUIRED `fiscalYearId`
(`FIN-404-YEAR`), and API-FIN-029 for its `periodId` when — and only when — one is supplied
(`FIN-404-PERIOD`). API-FIN-031 was internally contradictory before: 404 on an unknown period bound
and 200 on an unknown year, in the same request. No new error code was introduced; both codes were
already registered in `FinErrorCodes` and both i18n bundles.
<!-- SUB:SVC-API-SEARCH:END -->
