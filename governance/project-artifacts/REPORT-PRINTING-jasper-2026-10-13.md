# Report printing (JasperReports): one export method for every screen

**Date:** 2026-10-13.
**Branches:** backend `feat/jasper-print`; frontend `feat/server-report-export`.
**Ported from:** the erp-core step-14 printing practice (newback/newfront), adapted to this monolith.

## 1. What changed

**Before:** every list or report screen exported in the browser. `TableExportMenu` read the rendered table
(only the rows on screen), wrote Excel with the `xlsx` package (known advisories) and "PDF" via the browser's
print dialog. The audit-log screen called `GET /sec/audit-log/export` but never delivered a file.

**After:** one server endpoint prints any screen as a real PDF or Excel file, and one shared frontend
control calls it.

```
POST /api/v1/reports/{code}/export?format=pdf|xlsx
Accept-Language: ar | en
{ "params": { ...the screen's current criteria... } }
→ 200 application/pdf | …spreadsheetml.sheet  (the file itself, Content-Disposition: attachment)
```

**Errors** (JSON envelope as usual):

| HTTP | Code | When |
|---|---|---|
| 404 | `REPORT_NOT_FOUND` | the report code does not exist |
| 400 | `PRINT_FORMAT_NOT_SUPPORTED` | the format is not `pdf` or `xlsx` |
| 400 | `REPORT_PARAM_INVALID` | a criterion is missing or malformed |
| 422 | `REPORT_EXPORT_TOO_LARGE` | the export has more than `app.print.max-rows` rows |
| 500 | `PRINT_GENERATION_FAILED` | the print engine failed |
| 401 / 403 | — | no token, or the user lacks the permission (as usual) |

## 1b. The five financial reports are designed templates with their own live SQL

Trial balance, income statement, balance sheet, account ledger and dimension report do **not** use the
generic table. Each is a designed JasperReports template, `reports/fin/<name>.jrxml`. Its SQL reads the
POSTED journal lines straight from the database on every print. There is no cache anywhere: FIN's
caching register is empty and nothing in the print path caches data. Only the compiled layout is reused.

**How a print runs:**

1. **Same gate.** `FinPrintCriteriaService` (`fin/service`) applies the exact permission (`@PreAuthorize`)
   and the exact criteria validation of the matching `ReportService` method, with the same error codes in
   the same order:
   - `FIN-404-PERIOD`, `FIN-404-YEAR`, `FIN-404-ACCOUNT`, `FIN-404-DIMENSION`, `FIN-404-DIMVALUE`;
   - `FIN-409-INVALID-DIMENSION`.

   It also resolves the names the printout states (period, year, branch, account).
2. **Same query.** The template's SQL is the SQL form of the repository's own query:
   - `aggregateAccountBalances` for the trial balance, balance sheet and income statement;
   - `findAccountLedgerLines` for the ledger;
   - `aggregateByAccountAndDimensionValue` for the dimension report.

   It applies the same POSTED gate, the same optional period/year/date/dimension filters (the same EXISTS
   narrowing) and the same ordering. The mapper's rules move into SQL unchanged: debit/credit balance,
   signed balance against the account's nature, the ledger's running balance (a window sum in the same
   order) and the income statement's revenue − expense.
3. **One template, two languages.** The template is designed left-to-right. For Arabic,
   `TemplatePrinter` mirrors it when it loads: positions and left/right alignments swap, and the Excel
   sheet is set right-to-left. Labels live in `reports/fin/i18n/fin(_ar).properties`; lookup codes print as
   their MDL labels, passed in as maps.

**What each printout contains:**
- company name and logo, title, a criteria line (e.g. "Period: November | Account type: all | Branch: …"),
  and printed by / printed at;
- the screen's columns, with group headers and group totals (account type) on the statements;
- grand totals, and the "balanced" line on the trial balance;
- for the balance sheet, total assets against total liabilities + equity;
- for the income statement, the net result as profit / loss / break-even;
- for the ledger, the closing balance;
- signature blocks (prepared / reviewed / approved), and "page X of Y".

**Excel:** one header row, then exactly the table's columns. Logo, printed by/at and signatures are PDF-only.

**Proof of equality** (test record, §6):
- An automated comparison read each of the five screen endpoints and the printed XLSX for 30 criteria
  combinations: periods, account types, branch value / branch dimension, year with cut-off date, period
  ranges, date ranges, accounts.
- It compared every row, every amount and every total, then six invalid-criteria cases (same HTTP status,
  same error code).
- **251/251 checks equal.** The data held a reversal pair (both counted, as on screen) and a DRAFT entry
  (excluded by both).

### The journal and the chart of accounts are templates too

| Report | Template | Contents |
|---|---|---|
| `FIN_JOURNAL_ENTRIES`, the journal book (journal entries screen) | `fin/journal-entries` | Every entry is one block, kept on one page: number, date, type, status, description, reference, and "reverses entry / reversed by". Under it: its lines (line, account, description, dimensions such as branch and organisation, debit, credit) and the entry total. At the end: entry count, grand totals and signatures |
| `FIN_JOURNAL_VOUCHER`, a single entry (button "Print entry" on the entry page) | the same template, one entry | as above |
| `FIN_ACCOUNTS`, the chart of accounts | `fin/chart-of-accounts` | the tree in the screen's order, names indented by level, type, nature, parent, accepts posting, status |

**Which records are printed:**
- **Journal book, chart of accounts.** Taken from the screen's own `search` (same filters, same sort, same
  permission). The template then reads those entries with **all** their lines, or those accounts, live by
  SQL.
- **Voucher.** Gated by `JournalEntryService.read`: same permission, same `FIN-404-ENTRY`.

**Proof of equality:**
- Against the screens' search and read endpoints:
  - 5 journal filter sets (all, period with sort, date range with status, type, drafts only);
  - every entry printed as a voucher;
  - 2 chart filters;
  - an unknown entry.
- It compared:
  - for the journal book and the vouchers: the same entries in the same order, and every line's account,
    debit and credit;
  - for the chart: the same accounts in tree order;
  - for the unknown entry: the same error.
- **117/117 equal.**

### Every FIN screen prints from a template

All 14 FIN report codes are designed templates. The master-data lists print as organized reports:
- fiscal years with their periods;
- dimensions with their values;
- recurring templates and engine rules as blocks with their lines;
- account mappings with each account's code and name.

Each prints exactly the records the screen's own `search` returns, in the same order, read live by SQL.
The comparison against the screens' search endpoints (records and order) gave **8/8 equal**, plus every
rule's line count. Only the SEC and MDL screens still use the generic table.

## 2. Why it does not change any logic

- **Same service and filters.** Each report reads through the service method its screen already uses: the
  `ReportService` method for the five FIN reports, otherwise the module's `search(...)` (or `browseByOwner`
  for the type registry). The data, filters, validation and `@PreAuthorize` permission are exactly the
  screen's.
- **No schema or permission change:**
  - no new permission, no migration, no change to any existing entity, DTO, service or controller;
  - who can export = who can view the screen.
- **List screens send their own search body.** They pass the body they already send to `/search`
  (`filters`, `sortField`, `sortDirection`), minus paging. The provider pages through it 200 rows at a time,
  the search endpoints' own maximum. The printout therefore holds every matching row, not just the visible
  page.
- **Audit-log CSV left as is.** The old `GET /api/v1/sec/audit-log/export` endpoint is untouched, so the API
  contract does not change. The UI now prints `SEC_AUDIT_LOG` instead.

## 3. Backend layout

| Where | What |
|---|---|
| `common/print/` | the engine and the SPI (shared, module-agnostic) |
| `ReportProvider` | `code()`, `titleAr()`, `titleEn()`, `run(ReportParams)`; one Spring bean per report |
| `ReportRegistry` | collects the providers; a duplicate code fails the startup |
| `ReportParams` | typed criteria (`text`, `longValue`, `requiredLong`, `date`), the search body (`filters()`, `sortField()`, `sortDirection()`), `allPages(...)` with the row cap, `localized(ar, en)` |
| `TemplateReportProvider` / `TemplatePrinter` | designed `.jrxml` reports that fill from their own SQL (read-only transaction on the app's connection), mirrored for Arabic, labels from `reports/<folder>/i18n` |
| `TabularReportLayout` | the runtime Jasper layout, no `.jrxml`:<br>• header: logo, company name, title (+ optional `subtitle` criterion), printed by and at<br>• localized column headers and typed cells<br>• totals row<br>• "page X of Y"<br>• mirrored right-to-left for Arabic<br>• compiled once per report, columns and language |
| `PrintExporters` | PDF (Arabic shaped, font embedded); XLSX (typed cells, right-to-left sheet, one header row) |
| `ReportPrintService` / `ReportExportController` | the one endpoint; logs every print: `PRINT report=… format=… rows=… lang=… user=… ms=…` |
| `<module>/report/` | the providers, each in its own module and calling only its own module's services (ArchUnit: green) |
| `resources/reports/` | the font extension "ERP Arabic" (IBM Plex Sans Arabic, OFL); common labels ar/en; `common/logo.png` |

**Dependencies:** `net.sf.jasperreports:jasperreports`, `-pdf` and `-jdt` 7.0.8. `-jdt` compiles the layouts on
the JRE-only Docker image.

**Settings** (`application.properties`, each overridable by an environment variable):

| Property | Variable | Default |
|---|---|---|
| `app.print.company-name-ar` | `PRINT_COMPANY_NAME_AR` | الوسيط الذهبي |
| `app.print.company-name-en` | `PRINT_COMPANY_NAME_EN` | Al Waseet Al Thahabi |
| `app.print.logo-path` | `PRINT_LOGO_PATH` | `classpath:reports/common/logo.png`, the app icon |
| `app.print.max-rows` | `PRINT_MAX_ROWS` | `10000` |

### Adding a report

1. Add a `@Component` implementing `ReportProvider` in `com.erp.<module>.report`.
2. Read through the module's existing service method.
3. Map each item to `ReportResult.row(...)` with `ReportColumn`s copied from the screen's table.
4. On the frontend, place `<ReportExportMenu reportCode="…" params={…} />` on the screen.

## 4. Report catalogue (22)

| Code | Screen | Reads through |
|---|---|---|
| `FIN_TRIAL_BALANCE` | Trial balance | **template** `fin/trial-balance` (live SQL); gate `FinPrintCriteriaService.trialBalance` |
| `FIN_INCOME_STATEMENT` | Income statement | **template** `fin/income-statement` (live SQL); gate `FinPrintCriteriaService.incomeStatement` |
| `FIN_BALANCE_SHEET` | Balance sheet | **template** `fin/balance-sheet` (live SQL); gate `FinPrintCriteriaService.balanceSheet` |
| `FIN_ACCOUNT_LEDGER` | Account ledger | **template** `fin/account-ledger` (live SQL); gate `FinPrintCriteriaService.accountLedger` |
| `FIN_DIMENSION_REPORTS` | Dimension reports | **template** `fin/dimension-report` (live SQL); gate `FinPrintCriteriaService.dimensionReport` |
| `FIN_JOURNAL_ENTRIES` | Journal entries (journal book) | **template** `fin/journal-entries`; entries from `JournalEntryService.search` |
| `FIN_JOURNAL_VOUCHER` | Journal entry page (one entry) | **template** `fin/journal-entries`; gate `JournalEntryService.read` |
| `FIN_ACCOUNTS` | Chart of accounts (tree order, indented) | **template** `fin/chart-of-accounts`; accounts from `AccountService.search` |
| `FIN_PERIODS` | Fiscal years (each year with all its periods) | **template** `fin/fiscal-years`; years from `FiscalYearService.search` |
| `FIN_FISCAL_PERIODS` | Fiscal periods pane | **template** `fin/fiscal-periods`; periods from `FiscalPeriodService.search` |
| `FIN_ACCOUNT_MAPPINGS` | Account mappings (with account code and name) | **template** `fin/account-mappings`; mappings from `AccountMappingService.search` |
| `FIN_RECURRING_TEMPLATES` | Recurring templates (each with its lines and totals) | **template** `fin/recurring-templates`; templates from `RecurringTemplateService.search` |
| `FIN_DIMENSIONS` | Dimensions (each with all its values) | **template** `fin/dimensions`; dimensions from `DimensionService.search` |
| `FIN_DIMENSION_VALUES` | Dimension values pane | **template** `fin/dimension-values`; values from `DimensionValueService.search` |
| `FIN_RULES` | Posting engine rules (each with its lines) | **template** `fin/engine-rules`; rules from `EventTypeRuleService.search` |
| `SEC_USERS` | Users | `UserService.search` |
| `SEC_PENDING_SIGNUPS` | Pending sign-ups | `SignupRequestService.search` |
| `SEC_SESSIONS` | Active sessions | `SessionService.search` |
| `SEC_AUDIT_LOG` | Audit log | `AuditLogService.search` |
| `MDL_LOOKUP_TYPES` | Generic lookups: types | `LookupTypeService.search` |
| `MDL_LOOKUP_VALUES` | Generic lookups: values | `LookupValueService.search` |
| `MDL_TYPE_REGISTRY` | Type registry (all owner groups) | `LookupTypeService.browseByOwner` |

**Labels:**
- Lookup codes print as their MDL labels in the print language, as on screen (`FinReportLabels`).
- Active/inactive and yes/no print the screens' own text (FIN template labels, `MdlReportLabels`).

## 5. Decisions and deviations

- **Engine package.** The engine lives in `common/print`, a new sub-package of the existing shared
  `common` package. No new module was created, and `CrossModuleBoundaryArchTest` needs no change.
- **Label helpers.** The helpers are `@Component`s in each module's `report` package, not `@Service`s.
  They read lookup labels through `MdlLookupApi`, the cross-module API, and hold no business rules.
- **Shared page codes.** Two screens share a page code (users/pending, the two lookup panes), so they got
  distinct report codes: `SEC_PENDING_SIGNUPS`, and `MDL_LOOKUP_TYPES` / `MDL_LOOKUP_VALUES`.
- **Print logging.** A print is logged, not written to `SEC_AUDIT_LOG`. That table's check constraint
  allows only SEC event types, and a migration was out of scope.
- **Chart of accounts.** The chart is printed in full, every account matching the screen's filters, in
  the screen's tree order with a parent column. The screen itself renders the tree from one loaded page.
- **Client-side column sort.** The header sort only reorders the loaded page in the browser and is never
  sent to the server, so it is not printed. The printout follows the search body's sort.
- **Spring property encoding.** Spring reads `application.properties` as ISO-8859-1, so the Arabic company
  name is written as `\u` escapes.

## 6. Test record

| Level | Result |
|---|---|
| `mvn verify`, run on a scratch copy against a throwaway clone of `erp_db` (`erp_eslam_print`) | **83/83**, including `CrossModuleBoundaryArchTest` 2/2 |
| Every report × {ar, en} × {pdf, xlsx} over HTTP | **84/84 → 200** |
| FIN templates against the screens' own endpoints (30 criteria sets, 6 error cases) | **251/251 equal** (rows, amounts, totals, balanced flag, net result, closing balance, error status and code) |
| FIN master-data templates against the screens' search (records and order; engine-rule line counts) | **8/8 equal** |
| Journal book, vouchers and chart of accounts against the screens' search/read | **117/117 equal** (entries and order, every line's account/debit/credit, entry totals, tree order, unknown-entry error) |
| Browser test: journal entries screen and entry page × {pdf, xlsx} | **4/4**: the journal export sent exactly the screen's search criteria; the voucher sent the open entry |
| Browser test, five FIN screens × {pdf, xlsx}, Arabic UI, criteria in the address | **10/10**: the export sent exactly the criteria the screen read with, and the file downloaded |
| Visual check of the PDFs | right-to-left mirroring, Arabic shaping, logo, company, totals, page X of Y |
| Error codes | 404 `REPORT_NOT_FOUND`; 400 `PRINT_FORMAT_NOT_SUPPORTED`; 401 without a token |
| Frontend | `tsc` and `oxlint` clean; vitest **1366/1366**; `npm audit --omit=dev` **0** (`xlsx` removed) |
| Browser test (Playwright, Arabic UI) | trial balance PDF, chart of accounts Excel, audit log PDF and users Excel each downloaded a file and showed the success toast |

**Fixed during testing:**
- the Arabic company name printed garbled (the property encoding above);
- a long totals label overflowed its cell. It now spans every column before the first total, and the row
  grows to fit.

**Environment:**
- `erp_db` was not touched. The live tests ran on a throwaway copy, which Flyway took to V49, with a
  temporary test user. The copy is to be dropped after the review.
