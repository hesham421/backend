# Prompt — Branch / organisation filter on the FIN financial statements

> Prepared 2026-10-09. Paste the **Backend** section into a session on `backend/`, and the
> **Frontend** section into a session on `frontend/`. Run the backend first, because the frontend
> depends on its query parameters.

## Why

The accountant must be able to read every report for one company branch:
**أفضل وسيط**, **الوسيط الذهبي** or **الوسائط الفضية**, or for all of them together.

What production FIN already has (checked 2026-10-09 on erp_db):

- **Dimensions:** `fin_dimension` has `ORG` and `BRANCH`.
  - `BRANCH` values: code `1` = افضل وسيط, `2` = الوسيط الذهبى, `3` = الوسائط الفضيه. These match the
    LOAN_SYS `ORGANIZATION_SUB_FK` numbers.
  - `ORG` has one value: `1` = شركه الوسائط.
- **Tagging:** all 364 journal lines carry a `BRANCH` and an `ORG` value in `fin_journal_line_dim`. This covers
  the opening entries and the event-generated ones. Untagged lines: 0.
- **Reports with the filter already:**
  - `GET /api/v1/fin/reports/account-ledger` (`dimensionId`, `dimensionValueId`);
  - `GET /api/v1/fin/reports/dimension`.
- **Reports without the filter:** trial balance, balance sheet and income statement have no dimension filter in
  the API or in the UI.
  - They all use `JournalLineRepository.aggregateAccountBalances` (QR-FIN-043) via
    `ReportService.balanceRows`.
  - Their UI features are `trial-balance`, `balance-sheet` and `income-statement`. None of them references a
    dimension.

---

## Backend prompt

```
Task: add an optional dimension filter (branch / organisation) to the three FIN financial
statements, the same way account-ledger already does it.

Read first:
- CLAUDE.md and governance/shared/platform/rules/GOVERNANCE-RULES.md (skill routing);
  load the build-* skills it routes to for controller / service / repository changes.
- src/main/java/com/erp/fin/controller/ReportController.java
- src/main/java/com/erp/fin/service/ReportService.java  (trialBalance, balanceSheet,
  incomeStatement, balanceRows, accountLedger as the reference)
- src/main/java/com/erp/fin/repository/JournalLineRepository.java
  (aggregateAccountBalances = QR-FIN-043; the account-ledger query already has the
  EXISTS-on-JournalLineDimension pattern to copy)

Change:
1. ReportController: add two optional query params to
   GET /reports/trial-balance, /reports/balance-sheet, /reports/income-statement:
     dimensionId (Long), dimensionValueId (Long)
   Same names, types and validation as /reports/account-ledger.
2. ReportService: pass them through balanceRows → aggregateAccountBalances.
   Validation exactly as account-ledger:
     - unknown dimensionId            → FIN-404-DIMENSION
     - dimensionValueId without dimensionId, or a value that does not belong to the
       dimension → the same error account-ledger raises (reuse it, do not invent a code)
   Echo dimensionId / dimensionValueId in the three response DTOs (TrialBalanceResponse,
   BalanceSheetResponse, IncomeStatementResponse) so the UI can show what was filtered.
3. JournalLineRepository.aggregateAccountBalances: add the two predicates, null-safe like
   the existing ones:
     AND (CAST(:dimensionValueId AS Long) IS NULL OR EXISTS (
          SELECT 1 FROM JournalLineDimension jv
           WHERE jv.journalLine = l AND jv.dimensionValue.dimensionValuePk = :dimensionValueId))
     AND (CAST(:dimensionId AS Long) IS NULL OR EXISTS (
          SELECT 1 FROM JournalLineDimension jd
           WHERE jd.journalLine = l AND jd.dimension.dimensionPk = :dimensionId))
   A filter on dimensionValueId alone is enough to select one branch; dimensionId alone
   keeps every line tagged with that dimension.
4. The trial balance "balanced" flag: a per-branch trial balance balances only if every
   entry's lines carry the same branch. Event entries do (one branch per event), and manual
   entries may not. Keep the flag, and document in the Javadoc that a filtered result may
   not balance, as the accountTypeCode slice already does.
5. Manual journal entries: in the create/update validation, warn or reject (follow the
   plan; if the plan is silent, record an api_doc_gaps row, do not invent) a line that has
   no BRANCH dimension, so manual entries do not fall out of branch reports.
6. OpenAPI annotations on the three endpoints for the new params.
7. Tests: for each report, one test with no filter (unchanged totals), one with
   dimensionValueId of branch 1 (only branch-1 lines), one unknown dimension (404).
   Use the existing ReportService / ReportController test classes.

Do not add a Flyway migration (no schema change). Do not touch the dimension report.
When done, record any plan gap with
  python3 scripts/gov-module.py record FIN api_doc_gaps --key "GET /api/v1/fin/reports/trial-balance" --resolution "OPEN — ..."
because the factory's API document for FIN does not declare these params yet.
```

## Frontend prompt

```
Task: add a "الفرع" (branch) filter to the FIN trial balance, balance sheet and income
statement screens in mxdashboard, using the new optional query params dimensionId and
dimensionValueId that the backend now accepts on
  GET /api/v1/fin/reports/trial-balance | balance-sheet | income-statement.

Read first: frontend/governance/CLAUDE.md and its skills; then
- src/modules/fin/features/account-ledger/  ← the reference: it already has the
  dimension + dimension-value pickers, the schema rule "a value needs its dimension", and
  passes them as query params. Reuse those components/hooks, do not duplicate them.
- src/modules/fin/features/trial-balance/   (types/trialBalance.types.ts, .schema.ts,
  components/trialBalanceOptions.ts, api/trialBalanceApi.ts, hooks/useTrialBalanceFacade.ts)
- src/modules/fin/features/balance-sheet/ and income-statement/ (same layout)

Change, in each of the three features:
1. Criteria type + zod schema: add optional dimensionId, dimensionValueId with the same
   rule as account-ledger (value requires dimension).
2. Criteria form: a branch picker. Default UX: one select labelled "الفرع" with
   "كل الفروع" + the BRANCH dimension's values (fetched from the dimensions API, never
   hard-coded). Selecting a branch sets dimensionId = BRANCH's id and dimensionValueId =
   the value. "كل الفروع" clears both.
3. Query keys: include both params so each branch caches separately.
4. Show the selected branch in the report header and in any export/print title.
5. Tests: schema tests (value without dimension rejected), facade test (params sent),
   one render test per screen with the branch select.
Arabic labels via the i18n files the feature already uses.
```

## Accept when

- `trial-balance?dimensionValueId=<branch 1 value id>` returns only أفضل وسيط balances.
- The three branch results add up to the unfiltered result, account by account.
  This holds while every line carries a branch, as all 364 do on 2026-10-09.
- The UI shows "الفرع" on the three screens, and the account ledger keeps working unchanged.
