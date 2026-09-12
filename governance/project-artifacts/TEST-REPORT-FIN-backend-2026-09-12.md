# TEST REPORT — FIN (backend) — 2026-09-12

Mechanism: `api-verify` (`.claude/skills/api-verify/SKILL.md`), **Full tier** — TestSprite not
used (retired as this project's backend test mechanism).
Target: `http://localhost:7272` (Dev/Test, localhost — `api-verify-config.md` §4.1).
Generated script: `governance/modules/FIN/test-api/test_fin_apis.py`
Raw skill output: `governance/modules/FIN/test-api/fin_problems_report.md` (left untouched).

**This report covers two passes on 2026-09-12**: the verification run, then a fix pass the user
authorised on its findings, then a full re-run. Final result: **124 assertions, 0 failures.**
§7 records what was changed, what was deliberately NOT changed, and why.

> **Location note.** `/FIN/execute-backend-test` STEP 2 names `reports/TEST-REPORT-FIN-backend-*.md`.
> No `reports/` directory exists in this repo and no prior `TEST-REPORT-*` file has ever been
> written. `CLAUDE.md`'s STRUCTURAL LAW is binding and states that reporting / non-impacting
> markdown lives in `governance/project-artifacts/`, and that no new top-level content category
> may be created without explicit human confirmation. This report is therefore filed here.
> Creating `reports/` needs a human decision, not a silent one.

---

## 1. Coverage — governed plan ↔ api-verify (STEP 1.9)

Enumerated from `governance/modules/FIN/test_gen/backend-test-plan-fin.md`: **109** `TC-FIN-*`
markers, ids TC-FIN-001..109 with no gaps — RULE-SCENARIOS 34 · API-SCENARIOS 73 · INT-XM 2.
One is RETIRED (TC-FIN-091, plan line 1607), so **108 TCs are in force**.

**Coverage ratio: 100/108 TCs in force · 0 failures · 8 gaps · 1 retired (reported separately,
neither covered nor a gap).**
Executed: 124 assertions across 14 suites — **124 passed, 0 failed** — plus 2 stage-E
observations, which never affect totals.

XM coverage: **1/1**. FIN declares one XM today (XM-FIN-001 → MDL); TC-FIN-047 exercises it and
passes. XM-FIN-002 → SEC no longer exists, and TC-FIN-091, which existed only to drive it, is
RETIRED — recorded as RETIRED, never as a gap.

| TC | traces (AC/REQ/XM) | sub/phase | test suite ref | result |
|---|---|---|---|---|
| TC-FIN-002 | AC-FIN-002,REQ-FIN-002,API-FIN-003 | RULE-SCENARIOS | Account | PASS |
| TC-FIN-006 | AC-FIN-006,REQ-FIN-006,API-FIN-007 | RULE-SCENARIOS | DimensionValue | PASS |
| TC-FIN-009 | AC-FIN-009,REQ-FIN-009,API-FIN-011 | RULE-SCENARIOS | EventTypeRule | PASS |
| TC-FIN-011 | AC-FIN-011,REQ-FIN-011,API-FIN-020 | RULE-SCENARIOS | JournalEntry (from event) | PASS |
| TC-FIN-012 | AC-FIN-012,REQ-FIN-012,API-FIN-020 | RULE-SCENARIOS | EventTypeRule (remainder distribution) | PASS |
| TC-FIN-013 | AC-FIN-013,REQ-FIN-013,API-FIN-020 | RULE-SCENARIOS | JournalEntry (from event) | PASS |
| TC-FIN-017 | AC-FIN-017,REQ-FIN-017,API-FIN-019 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-018 | AC-FIN-018,REQ-FIN-018,API-FIN-019 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-019 | AC-FIN-019,REQ-FIN-019,API-FIN-019 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-020 | AC-FIN-020,REQ-FIN-020,API-FIN-019 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-021 | AC-FIN-021,REQ-FIN-021,API-FIN-019 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-028 | AC-FIN-028,REQ-FIN-028,API-FIN-021 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-029 | AC-FIN-029,REQ-FIN-029,API-FIN-021 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-030 | AC-FIN-030,REQ-FIN-030,API-FIN-021 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-048 | AC-FIN-030,REQ-FIN-030,API-FIN-021 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-035 | AC-FIN-035,REQ-FIN-035,API-FIN-024 | RULE-SCENARIOS | FiscalPeriod | PASS |
| TC-FIN-038 | AC-FIN-038,REQ-FIN-038,API-FIN-026 | RULE-SCENARIOS | ✗ none | **GAP** |
| TC-FIN-049 | AC-FIN-014,REQ-FIN-014,REQ-FIN-017,API-FIN-019 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-050 | AC-FIN-014,REQ-FIN-014,REQ-FIN-017,API-FIN-019 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-051 | AC-FIN-014,REQ-FIN-014,REQ-FIN-017,API-FIN-019 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-052 | AC-FIN-009,REQ-FIN-009,API-FIN-011 | RULE-SCENARIOS | EventTypeRule | PASS |
| TC-FIN-053 | AC-FIN-025,REQ-FIN-025,API-FIN-016 | RULE-SCENARIOS | AllocationRule | PASS |
| TC-FIN-054 | AC-FIN-012,REQ-FIN-012,API-FIN-020 | RULE-SCENARIOS | EventTypeRule (remainder distribution) | PASS |
| TC-FIN-055 | AC-FIN-012,REQ-FIN-012,API-FIN-020 | RULE-SCENARIOS | EventTypeRule (remainder distribution) | PASS |
| TC-FIN-056 | AC-FIN-036,REQ-FIN-036,API-FIN-027 | RULE-SCENARIOS | ✗ none | **GAP** |
| TC-FIN-057 | AC-FIN-002,REQ-FIN-002,API-FIN-003 | RULE-SCENARIOS | Account | PASS |
| TC-FIN-058 | AC-FIN-002,REQ-FIN-002,API-FIN-002 | RULE-SCENARIOS | Account | PASS |
| TC-FIN-059 | AC-FIN-034,REQ-FIN-034,REQ-FIN-038,API-FIN-026 | RULE-SCENARIOS | FiscalPeriod | PASS |
| TC-FIN-060 | AC-FIN-038,REQ-FIN-038,API-FIN-027 | RULE-SCENARIOS | ✗ none | **GAP** |
| TC-FIN-061 | AC-FIN-038,REQ-FIN-038,API-FIN-027 | RULE-SCENARIOS | ✗ none | **GAP** |
| TC-FIN-062 | AC-FIN-038,REQ-FIN-038,API-FIN-026 | RULE-SCENARIOS | FiscalPeriod | PASS |
| TC-FIN-092 | AC-FIN-013,REQ-FIN-013,REQ-FIN-007,API-FIN-034,API-FIN-020 | RULE-SCENARIOS | JournalEntry (from event) | PASS |
| TC-FIN-093 | AC-FIN-021,REQ-FIN-021,REQ-FIN-005,API-FIN-035,API-FIN-019 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-103 | AC-FIN-021,REQ-FIN-021,API-FIN-019 | RULE-SCENARIOS | JournalEntry | PASS |
| TC-FIN-001 | AC-FIN-001,REQ-FIN-001,API-FIN-002 | API-SCENARIOS | Account | PASS |
| TC-FIN-003 | AC-FIN-003,REQ-FIN-003,API-FIN-004 | API-SCENARIOS | JournalEntry | PASS |
| TC-FIN-004 | AC-FIN-004,REQ-FIN-004,API-FIN-006 | API-SCENARIOS | Dimension | PASS |
| TC-FIN-005 | AC-FIN-005,REQ-FIN-005,API-FIN-007 | API-SCENARIOS | DimensionValue | PASS |
| TC-FIN-007 | AC-FIN-007,REQ-FIN-007,API-FIN-010 | API-SCENARIOS | EventTypeRule | PASS |
| TC-FIN-008 | AC-FIN-008,REQ-FIN-008,API-FIN-011 | API-SCENARIOS | EventTypeRule | PASS |
| TC-FIN-010 | AC-FIN-010,REQ-FIN-010,API-FIN-020 | API-SCENARIOS | JournalEntry (from event) | PASS |
| TC-FIN-014 | AC-FIN-014,REQ-FIN-014,API-FIN-019 | API-SCENARIOS | JournalEntry | PASS |
| TC-FIN-015 | AC-FIN-015,REQ-FIN-015,API-FIN-019 | API-SCENARIOS | JournalEntry | PASS |
| TC-FIN-016 | AC-FIN-016,REQ-FIN-016,API-FIN-022 | API-SCENARIOS | JournalEntry | PASS |
| TC-FIN-022 | AC-FIN-022,REQ-FIN-022,API-FIN-013 | API-SCENARIOS | RecurringTemplate | PASS |
| TC-FIN-023 | AC-FIN-023,REQ-FIN-023,API-FIN-014 | API-SCENARIOS | RecurringTemplate | PASS |
| TC-FIN-024 | AC-FIN-024,REQ-FIN-024,API-FIN-014 | API-SCENARIOS | RecurringTemplate | PASS |
| TC-FIN-025 | AC-FIN-025,REQ-FIN-025,API-FIN-016 | API-SCENARIOS | AllocationRule | PASS |
| TC-FIN-026 | AC-FIN-026,REQ-FIN-026,API-FIN-017 | API-SCENARIOS | AllocationRule | PASS |
| TC-FIN-027 | AC-FIN-027,REQ-FIN-027,API-FIN-018 | API-SCENARIOS | JournalEntry | PASS |
| TC-FIN-031 | AC-FIN-031,REQ-FIN-031,API-FIN-023 | API-SCENARIOS | FiscalYear | PASS |
| TC-FIN-032 | AC-FIN-032,REQ-FIN-032,API-FIN-024 | API-SCENARIOS | FiscalPeriod | PASS |
| TC-FIN-033 | AC-FIN-033,REQ-FIN-033,API-FIN-025 | API-SCENARIOS | FiscalPeriod | PASS |
| TC-FIN-034 | AC-FIN-034,REQ-FIN-034,API-FIN-026 | API-SCENARIOS | FiscalPeriod | PASS |
| TC-FIN-036 | AC-FIN-036,REQ-FIN-036,API-FIN-027 | API-SCENARIOS | ✗ none | **GAP** |
| TC-FIN-037 | AC-FIN-037,REQ-FIN-037,API-FIN-026 | API-SCENARIOS | FiscalPeriod | PASS |
| TC-FIN-039 | AC-FIN-039,REQ-FIN-039,API-FIN-028 | API-SCENARIOS | Reports | PASS |
| TC-FIN-040 | AC-FIN-040,REQ-FIN-040,API-FIN-029 | API-SCENARIOS | Reports | PASS |
| TC-FIN-041 | AC-FIN-041,REQ-FIN-041,API-FIN-030 | API-SCENARIOS | Reports | PASS |
| TC-FIN-042 | AC-FIN-042,REQ-FIN-042,API-FIN-031 | API-SCENARIOS | Reports | PASS |
| TC-FIN-043 | AC-FIN-043,REQ-FIN-043,API-FIN-032 | API-SCENARIOS | Reports | PASS |
| TC-FIN-044 | AC-FIN-044,REQ-FIN-044,API-FIN-010 | API-SCENARIOS | ✗ none | **GAP** |
| TC-FIN-045 | AC-FIN-045,REQ-FIN-045,API-FIN-010 | API-SCENARIOS | PREFLIGHT (stage A0) | PASS |
| TC-FIN-046 | AC-FIN-046,REQ-FIN-046,API-FIN-030 | API-SCENARIOS | JournalEntry (from event) | PASS |
| TC-FIN-063 | AC-FIN-001,REQ-FIN-001,API-FIN-001 | API-SCENARIOS | Account | PASS |
| TC-FIN-064 | AC-FIN-001,REQ-FIN-001,API-FIN-001 | API-SCENARIOS | Dimension | PASS |
| TC-FIN-065 | AC-FIN-004,REQ-FIN-004,API-FIN-005 | API-SCENARIOS | Dimension | PASS |
| TC-FIN-066 | AC-FIN-005,REQ-FIN-005,API-FIN-008 | API-SCENARIOS | DimensionValue | PASS |
| TC-FIN-067 | AC-FIN-007,REQ-FIN-007,API-FIN-009 | API-SCENARIOS | EventTypeRule | PASS |
| TC-FIN-068 | AC-FIN-022,REQ-FIN-022,API-FIN-012 | API-SCENARIOS | RecurringTemplate | PASS |
| TC-FIN-069 | AC-FIN-025,REQ-FIN-025,API-FIN-015 | API-SCENARIOS | AllocationRule | PASS |
| TC-FIN-070 | AC-FIN-001,REQ-FIN-001,API-FIN-002 | API-SCENARIOS | Account | PASS |
| TC-FIN-071 | AC-FIN-002,REQ-FIN-002,REQ-FIN-003,API-FIN-003 | API-SCENARIOS | Account; Reports | PASS |
| TC-FIN-072 | AC-FIN-004,REQ-FIN-004,API-FIN-006 | API-SCENARIOS | Dimension | PASS |
| TC-FIN-073 | AC-FIN-007,REQ-FIN-007,API-FIN-010 | API-SCENARIOS | EventTypeRule | PASS |
| TC-FIN-074 | AC-FIN-008,REQ-FIN-008,API-FIN-011 | API-SCENARIOS | EventTypeRule | PASS |
| TC-FIN-075 | AC-FIN-022,REQ-FIN-022,API-FIN-013 | API-SCENARIOS | RecurringTemplate | PASS |
| TC-FIN-076 | AC-FIN-023,REQ-FIN-023,API-FIN-014 | API-SCENARIOS | RecurringTemplate | PASS |
| TC-FIN-077 | AC-FIN-026,REQ-FIN-026,API-FIN-017 | API-SCENARIOS | AllocationRule | PASS |
| TC-FIN-078 | AC-FIN-010,REQ-FIN-010,API-FIN-020 | API-SCENARIOS | EventTypeRule (remainder distribution) | PASS |
| TC-FIN-079 | AC-FIN-016,REQ-FIN-016,REQ-FIN-027,API-FIN-022 | API-SCENARIOS | JournalEntry | PASS |
| TC-FIN-080 | AC-FIN-031,REQ-FIN-031,API-FIN-023 | API-SCENARIOS | FiscalYear | PASS |
| TC-FIN-081 | AC-FIN-031,REQ-FIN-031,API-FIN-023 | API-SCENARIOS | FiscalYear | PASS |
| TC-FIN-082 | AC-FIN-032,REQ-FIN-032,API-FIN-024 | API-SCENARIOS | FiscalPeriod | PASS |
| TC-FIN-083 | AC-FIN-033,REQ-FIN-033,API-FIN-025 | API-SCENARIOS | FiscalPeriod | PASS |
| TC-FIN-084 | AC-FIN-036,REQ-FIN-036,API-FIN-027 | API-SCENARIOS | FiscalYear year-end close | PASS |
| TC-FIN-085 | AC-FIN-036,REQ-FIN-036,API-FIN-027 | API-SCENARIOS | FiscalYear year-end close | PASS |
| TC-FIN-086 | AC-FIN-036,REQ-FIN-036,API-FIN-027 | API-SCENARIOS | ✗ none | **GAP** |
| TC-FIN-087 | AC-FIN-014,REQ-FIN-014,API-FIN-019 | API-SCENARIOS | JournalEntry | PASS |
| TC-FIN-088 | AC-FIN-014,REQ-FIN-014,API-FIN-019 | API-SCENARIOS | JournalEntry | PASS |
| TC-FIN-089 | AC-FIN-043,REQ-FIN-043,API-FIN-032 | API-SCENARIOS | Reports | PASS |
| TC-FIN-090 | AC-FIN-045,REQ-FIN-045,API-FIN-010,XM-FIN-001 | API-SCENARIOS | PREFLIGHT (stage A0) | PASS |
| TC-FIN-094 | AC-FIN-031,REQ-FIN-031,API-FIN-033 | API-SCENARIOS | FiscalPeriod | PASS |
| TC-FIN-095 | AC-FIN-031,REQ-FIN-031,API-FIN-033 | API-SCENARIOS | FiscalPeriod | PASS |
| TC-FIN-096 | AC-FIN-007,REQ-FIN-007,API-FIN-034 | API-SCENARIOS | EventTypeRule; JournalEntry (from event) | PASS |
| TC-FIN-097 | AC-FIN-007,REQ-FIN-007,API-FIN-034,API-FIN-010 | API-SCENARIOS | JournalEntry (from event) | PASS |
| TC-FIN-098 | AC-FIN-005,REQ-FIN-005,API-FIN-035 | API-SCENARIOS | DimensionValue | PASS |
| TC-FIN-099 | AC-FIN-040,REQ-FIN-040,API-FIN-029 | API-SCENARIOS | Reports | PASS |
| TC-FIN-100 | AC-FIN-041,REQ-FIN-041,API-FIN-030 | API-SCENARIOS | Reports | PASS |
| TC-FIN-101 | AC-FIN-042,REQ-FIN-042,API-FIN-031 | API-SCENARIOS | Reports | PASS |
| TC-FIN-102 | AC-FIN-036,REQ-FIN-036,API-FIN-027,API-FIN-019 | API-SCENARIOS | ✗ none | **GAP** |
| TC-FIN-104 | AC-FIN-022,REQ-FIN-022,API-FIN-036 | API-SCENARIOS | RecurringTemplate | PASS |
| TC-FIN-105 | AC-FIN-025,REQ-FIN-025,API-FIN-037 | API-SCENARIOS | AllocationRule | PASS |
| TC-FIN-106 | AC-FIN-023,REQ-FIN-023,API-FIN-036,API-FIN-014 | API-SCENARIOS | RecurringTemplate | PASS |
| TC-FIN-107 | AC-FIN-026,REQ-FIN-026,API-FIN-037,API-FIN-017 | API-SCENARIOS | AllocationRule | PASS |
| TC-FIN-108 | AC-FIN-022,REQ-FIN-022,API-FIN-012,API-FIN-015,API-FIN-036,API-FIN-037 | API-SCENARIOS | Account | PASS |
| TC-FIN-109 | AC-FIN-010,REQ-FIN-010,API-FIN-020,API-FIN-011 | API-SCENARIOS | EventTypeRule (remainder distribution) | PASS |
| TC-FIN-047 | XM-FIN-001,REQ-FIN-001,API-FIN-002 | INT-XM | JournalEntry (from event) | PASS |
| TC-FIN-091 | REQ-FIN-037,REQ-FIN-038,API-FIN-026 | RETIRED | — | RETIRED |
### 1.1 The 8 coverage gaps, and why each is a gap

None of these is a silent drop; each is blocked by a stated boundary, not by effort.

| TC | What it needs | Why api-verify cannot reach it |
|---|---|---|
| TC-FIN-036 · 056 · 086 · 102 | API-FIN-027 year-end close | All four need a completed year-end close. That needs (a) every one of the year's 12 periods Hard Closed, and (b) an account marked `is_retained_earnings_fl`. The manifest states that flag is **"settable only as data"** — there is no API that sets it. `api-verify` may write to the DB only to delete rows it created (SKILL.md §3-H); any other data fix is emitted as suggested SQL for a human, never executed. So the close cannot be staged from the HTTP surface at all. **TC-FIN-086 is itself the test for that marker's absence being reported**, which makes the circularity explicit. |
| TC-FIN-038 · 060 · 061 | A caller holding a *different* permission set | These are separation-of-duties tests: they need a second user whose role grants entry-creation but not close-approval (038, 060) or both (061). SKILL.md §3-I lets a run grant **its own account** the module's documented permissions and nothing else — "creating a role, elevating a human user, touching a grant the run did not make is out of scope and stays out." The run therefore cannot construct the restricted principal these three require. Note TC-FIN-059 and TC-FIN-062, the *satisfied* halves of RULE-FIN-015, ARE covered and pass. |
| TC-FIN-044 | "FIN registers itself into SEC at onboarding" | There is no documented **read** endpoint for the SEC module registry. The aggregate OpenAPI exposes only `POST /api/v1/sec/registry/modules` (create) — no search or get. With no read surface, registration cannot be asserted without inventing one. Its MDL twin, TC-FIN-045 (13 FIN-owned lookup types), IS covered and passes, because MDL does publish `POST /api/v1/mdl/lookup-types/by-owner/search`. |

---

## 2. Failures and observations, classified

### 2.1 The one failing test — now FIXED (see §7)

| TC | Classification | Detail |
|---|---|---|
| TC-FIN-016 | `CONTRACT_BREAK` | `DELETE /api/v1/fin/journal-entries/{id}` returned **HTTP 500 / `INTERNAL_ERROR`**. The manifest's RULE-FIN-016 row states 404/405. **Now returns 405 with an `Allow: GET` header; TC-FIN-016 PASSES.** |

**The defect was real, and it was NOT in FIN.** The protection itself always held — no delete
route exists and no entry was ever deleted — but the status code was wrong. Root cause:
`com/erp/common/web/GlobalExceptionHandler.java` declared handlers only for
`LocalizedException`, `MethodArgumentNotValidException`, `HttpMessageNotReadableException`,
`DataIntegrityViolationException`, `AccessDeniedException` and a catch-all
`@ExceptionHandler(Exception.class)`. It does not extend `ResponseEntityExceptionHandler` and had
no handler for Spring's standard MVC exceptions, so `HttpRequestMethodNotSupportedException` fell
into the catch-all and was reported as 500.

The same root cause was observed a second time during the run, on a different module:
`GET /api/v1/mdl/lookups` without its required `type` parameter raises
`MissingServletRequestParameterException` and also returned **500** instead of 400. This was a
platform-wide defect affecting every module, not a FIN one — every malformed request that Spring
itself rejects was reported to clients as an internal server error. Both are fixed in §7.

### 2.2 Stage-E observations (recorded, never asserted — no artifact states an expected outcome)

1. **An unresolvable `FIELD` amount source degrades to zero, then trips a DB constraint.**
   `EventTypeRuleDomain.sourcedAmount` (lines 274-276) returns `BigDecimal.ZERO` when the named
   amount field is absent from the event, instead of raising. The zero-amount line then violates
   the Postgres check constraint `chk_fin_journal_line_amount_positive`, and the caller receives
   **409 / `DATA_INTEGRITY_VIOLATION`** — a raw persistence failure, not a `FIN-*` code.
   `api-verify-config.md` §3 requires the format `{MOD}-{http}[-{SLUG}]`, and the
   `gov-enforce-error-handling` skill requires every exception to be a `LocalizedException`
   carrying a registered module-specific code. Worth a decision: either the domain should raise
   a registered FIN code for an unresolvable amount source, or the silent-zero behaviour should
   be written down as intended. No artifact currently says either, so no rule was invented here.

2. **An unknown search FILTER field is silently ignored.** An unknown *sort* field is rejected
   with `FIN-400-INVALID-SORT` (TC-FIN-064, TC-FIN-095 — both pass). An unknown *filter* field is
   accepted and dropped, returning the unfiltered page. This bit this very run: a precondition
   step filtered MDL lookup types on `code` (the column is `key`), the filter was ignored, and
   the first unrelated row came back — so a value was nearly seeded into the wrong lookup type.
   Observed on MDL's shared `SpecBuilder` path, so it is platform-wide, not FIN-specific.

### 2.3 Documentation gap found while running

`POST /api/v1/fin/allocation-rules`'s generated Request Example shows a single target with
`distributionTypeCode: PERCENTAGE` and `isRemainderFl: false`. That payload is **invalid**:
`srs-fin.md` ENT-FIN-014 (line 296) requires "exactly one true per rule when any target is
PERCENTAGE [RULE-FIN-003, reused]", and the API correctly rejects it with
`FIN-409-REMAINDER-COUNT`. The example comes from the `@Schema(example=...)` annotations on
`AllocationRuleCreateRequest`/`AllocationTargetCreateRequest`, so the fix belongs in those
annotations, not in the generated docs (which must never be hand-edited). Classified as a
documentation defect, not an application defect — the rule behaves exactly as specified.
**Partially fixed in §7**: the `@Schema` examples now carry a valid PERCENTAGE + REMAINDER target
set, which is what Swagger UI and `/v3/api-docs` render. The generated *markdown* still shows a
single-element `targets` array, because `governance-tools/api-doc-generator`'s markdown renderer
always synthesises an array example as exactly one element from the nested DTO and never reads an
array-level example. Fixing that means editing `governance-tools/`, which was out of scope for the
fix pass — recorded here, not silently done.

### 2.4 Environment problems hit and resolved before the run (not FIN defects)

| Problem | Classification | Resolution |
|---|---|---|
| `POST /api/v1/sec/auth/login` returned 500, `NoClassDefFoundError: com.erp.sec.domain.UserDomain` | `ENVIRONMENT_FAILURE` | `target/classes` was being shared with the VSCode Java language server, which emptied `com/erp/sec/domain/` after the Maven build. `mvn clean compile` and a restart fixed it. Nothing to do with FIN. |
| FIN has no springdoc group | `MISSING_IMPLEMENTATION` | See §3 — **fixed in §7**. |

---

## 3. STEP 0.3 finding — FIN had no `GroupedOpenApi` group (FIXED in §7)

`src/main/java/com/erp/main/config/OpenApiConfig.java` (lines 34-76) declares groups for `sec`,
`cu`, `notif`, `file` and `mdl`. **There is no `finApi()` bean**, although FIN has 9 controllers
and 37 live operations under `/api/v1/fin`. The api-doc-generator aborted with
*"Could not identify a unique springdoc GroupedOpenApi group for module 'FIN'"*, and that file's
own Javadoc states the group id IS the generator's `--module` match key. FIN is therefore not
documentable through the standard path, and `governance/modules/FIN/api-docs/` had never been
generated before this run.

Classification: `MISSING_IMPLEMENTATION`. **Fixed in the §7 pass**: a `finApi()` bean was added,
`/v3/api-docs/fin` is now published alongside the other five groups, and the api-docs were
regenerated through the STANDARD path — `python3 generate.py --module FIN --function generate`,
no override, discovery resolving the group on its own (37 endpoints, 9 groups, 40 error codes).

Workaround used during the first pass, before the fix, so the run could proceed: the generator's own documented `--openapi` override
(README "Explicit overrides"), pointed at a copy of the live aggregate `/v3/api-docs` filtered
to the `/api/v1/fin` path prefix, with `--source`/`--common-source` given explicitly. The prefix
is not invented — it is the base path in `api-verify-config.md` §3 and in
`packages/backend-execution/DOC/DOC.md` line 7. Result: 37 endpoints, 9 entity groups, 40 error
codes written to `governance/modules/FIN/api-docs/`.

Related, minor: `DOC.md` line 7 says "32 endpoints under `/api/v1/fin`"; 37 are live. The
manifest's "ENDPOINTS ADDED AFTER THE ORIGINAL MANIFEST" section already accounts for
API-FIN-033..037, so the DOC count is simply stale, not wrong code.

---

## 4. Test-script defects found and corrected during the run

Per the standing instruction that a test failure is not automatically an application bug, each
first-pass failure was classified before anything was changed. Five were **test bugs** and were
fixed in the generated script; no application code was touched for any of them.

| First-pass failure | Classification | Correction |
|---|---|---|
| Event-type rule create → `FIN-400-INVALID-LOOKUP` | `TEST_STRUCTURE_FAILURE` | The precondition filtered MDL lookup types on `code`; the filterable field is `key` (`LookupTypeSearchRequest` javadoc). The ignored filter returned `NOTIF_CHANNEL`, so the event type was being seeded into the wrong lookup type. Now filtered on `key` and re-checked after the search. |
| From-event build → `409 DATA_INTEGRITY_VIOLATION` | `TEST_STRUCTURE_FAILURE` | Rule lines named the FIELD amount `baseAmount`, which is a top-level request field, not a member of the `amounts` map. Now uses `netAmount` with `amounts: {netAmount: …}`, matching the api-docs example. (The underlying degrade-to-zero behaviour is reported as observation §2.2-1.) |
| Rule line → `FIN-404-ACCOUNT` | `TEST_STRUCTURE_FAILURE` | `CONSTANT` derivation takes the account **code**, not the primary key — `EventTypeRuleDomain.java` line 47. The script passed the pk. |
| TC-FIN-049 → `FIN-404-YEAR` instead of `FIN-400-PERIOD-NOT-IN-YEAR` | `TEST_STRUCTURE_FAILURE` | The test submitted a nonexistent `fiscalYearId`, which can only ever raise 404. It now creates a **real** second fiscal year and submits year 1's period under year 2. Passes. |
| TC-FIN-013 expected `FIN-400-INVALID-LOOKUP` | `TEST_STRUCTURE_FAILURE` | The manifest's own RULE → CODE → TC row states RULE-FIN-005 → `FIN-404-NO-ACTIVE-RULE` → TC-FIN-013 ("never configured"). The application was right; the expectation was wrong. |
| TC-FIN-025 → `FIN-409-REMAINDER-COUNT` | `TEST_STRUCTURE_FAILURE` + doc defect | The payload was the api-docs example verbatim, which violates RULE-FIN-003 — see §2.3. The test now sends a valid PERCENTAGE + REMAINDER target set. |
| TC-FIN-026 → `FIN-404-PERIOD` | `DB_PRECONDITION` | `AllocationRuleService.java` lines 190-191 resolve the posting period from `LocalDate.now()`, and no fiscal period covered today. The script now ensures a fiscal year covering the current date exists, and says so. |

---

## 5. Surviving records and privileges

FIN documents **no hard-delete endpoint for any entity**, so this run's rows persist. They are
listed individually, per run, in `governance/modules/FIN/test-api/fin_problems_report.md`
("Surviving records"), never summarised as cleaned up.

- Deactivated at teardown (soft): AllocationRule, DimensionValue, RecurringTemplate,
  EventTypeRule, Account.
- **Left ACTIVE, no retirement surface exists**: Dimension (ENTITY CRUD CHECKLIST ENT-FIN-002
  shows "—" for both deactivate and activate), FiscalYear and its 12 generated FiscalPeriods
  (a fiscal year's only retirement is year-end close), and every JournalEntry — immutable by
  design under RULE-FIN-016.
- **Permanent residue, retired**: the `ACCOUNTING_EVENT_TYPE` lookup values this run added to
  MDL — a reference table other modules' rules read. V26 documents that this type is seeded
  empty on purpose and that the host must add its own values, so adding them is the documented
  step, not an invention. Each was retired via `DELETE /api/v1/mdl/lookup-values/{id}`, and the
  report carries the cleanup SQL for any that survive.
- Because fiscal years cannot be deleted, the script walks forward to a free year code rather
  than failing on a collision with an earlier run's leftovers.

**Privileges: none created, none revoked, none standing.** The bootstrap `admin` holds
SYS_ADMIN, which V24/V25/V30 already grant the FIN permission set to, so stage I was skipped
entirely and no grant journal was written.

---

## 6. Verdict

| Phase | Status | Basis |
|---|---|---|
| `TEST-PLAN-BE` | **PARTIAL** | 100 of its 107 in-force TCs are covered and **all pass — 0 failures**. Status is PARTIAL rather than COMPLETE solely because TC-FIN-036, 038, 044, 056, 060, 061, 086, 102 remain GAPS (§1.1) — structurally unreachable from the HTTP surface, not untested through neglect. Closing them needs a decision about how they should be covered, not more test code. |
| `INT-XM` | **COMPLETE** | Its only live TC, TC-FIN-047 (XM-FIN-001 → MDL), passes. TC-FIN-091 is RETIRED and contributes nothing. XM coverage 1/1. |

§2.1 and §3 were fixed in the §7 pass and verified by a full re-run. The items still open, each
needing a human decision that an agent may not take, are §2.2-1 (silent zero for an unresolvable
amount source), §2.2-2 (unknown search FILTER field silently ignored) and the markdown-renderer
half of §2.3. The first two are recorded in `execution-state.json`'s `api_doc_gaps[]` as
ESCALATED, NOT RESOLVED — see §7.2.

---

## 7. Fix pass (2026-09-12, authorised by the user after the first run)

Every change below went through a dispatched agent session that read
`gov-enforce-error-handling`, `gov-enforce-backend-contract` and `CLAUDE.md` in full first, and
ran `mvn -q -DskipTests compile` and `mvn -q -DskipTests test-compile` — both exited **0**. The
full suite was then re-run against the rebuilt, restarted app: **124 passed, 0 failed.**

### 7.1 Fixed

**(a) `GlobalExceptionHandler` no longer reports Spring's own MVC rejections as 500.**
Two targeted handlers were added (the class deliberately still does NOT extend
`ResponseEntityExceptionHandler` — that would change the envelope for every already-handled
case, a far wider blast radius than the defect warrants; the catch-all stays last and unchanged):

- `MissingServletRequestParameterException` + `MethodArgumentTypeMismatchException` → **400**,
  wire code `VALIDATION_ERROR`. This **reuses the already-registered code** and its existing
  bundle rows — no new code was introduced — and mirrors `handleValidation`'s shape by naming the
  offending parameter in `fieldErrors`. Verified live:
  `GET /api/v1/mdl/lookups` with no `type` now returns
  `400 / VALIDATION_ERROR / fieldErrors:[{field:"type", …}]`, and localizes
  (`Accept-Language: ar` → "فشل التحقق من البيانات").
- `HttpRequestMethodNotSupportedException` → **405**, wire code `METHOD_NOT_ALLOWED`, with the
  standard `Allow` header populated from `ex.getSupportedMethods()`. Verified live:
  `DELETE /api/v1/fin/journal-entries/{id}` → `405`, `Allow: GET`.

`METHOD_NOT_ALLOWED` is a **new platform-level error code** — added to `CommonErrorCodes` and to
**both** message bundles (`i18n/messages.properties`, `i18n/messages_ar.properties`). Because
governance forbids a later reader mistaking a decision for a requirement that was always there,
this comment sits verbatim above both the constant and the handler:

> *"Added 2026-09-12 by an explicit recorded human decision, not by any pre-existing requirement:
> no artifact registered a platform code for HTTP 405. Introduced so that an unsupported HTTP
> method reports 405 instead of the 500 the catch-all handler previously produced (found by the
> FIN api-verify run, TC-FIN-016)."*

Both handlers log at `warn`, not `error` — these are client errors; `log.error` stays for genuine
internals.

**(b) `finApi()` GroupedOpenApi bean added** to `OpenApiConfig`, mirroring the existing five
(group `fin`, displayName `FIN — Financials`, `packagesToScan("com.erp.fin.controller")`). The
agent checked the ambiguity warning in that file's own javadoc: `fin` appears in no other display
name and `FIN — Financials` contains none of the other group ids, so no `--module` match becomes
ambiguous. `/v3/api-docs/fin` is now published, and api-docs regenerate through the standard path
with no override.

**(c) Allocation-rule `@Schema` examples** on `AllocationRuleCreateRequest` /
`AllocationTargetCreateRequest` now show a **valid** target set — a PERCENTAGE target plus exactly
one REMAINDER target (which carries no `distributionValue`, per the same srs table) — and
`isRemainderFl`'s description states the RULE-FIN-003 constraint. Documentation annotations only:
no validation, no field, no default, no behaviour changed. See §2.3 for the half of this that
remains open in the markdown renderer.

### 7.2 Deliberately NOT fixed — specification silent, escalated instead

Governance forbids creating an error code, rule or status transition because it seems reasonable.
Both of these are genuinely unspecified, so no rule was invented; each is appended to
`governance/modules/FIN/execution-state.json` → `api_doc_gaps[]` as **"ESCALATED, NOT RESOLVED"**
with its `resolution` left OPEN. The pre-existing 36 gap entries are byte-identical — nothing was
deleted, rewritten, or retyped.

1. **Silent zero for an unresolvable `FIELD` amount source** (§2.2-1). `srs-fin.md:228` only
   requires `amountSourceValue` to be present unless the type is REMAINDER; it says nothing about
   the event failing to carry the named field at run time. The decision is between raising a new
   registered FIN code (a new error code — needs an explicit human decision) or declaring the
   silent-zero degrade intended and writing it into `srs-fin.md`, the Error Catalog and the plan.
2. **Unknown search FILTER field silently ignored** (§2.2-2). Symmetry with the sort-field
   behaviour argues for rejecting it, but that is a cross-module contract change affecting every
   search endpoint in every module, and no artifact specifies it. Out of FIN's scope to decide
   unilaterally; raised because a FIN run exposed it.

### 7.3 Artifacts updated in the same pass

- `governance/modules/FIN/api-docs/` — regenerated through the standard path after (b).
- `governance/modules/FIN/execution-state.json` — `test_phases[]` re-graded; two ESCALATED
  entries appended to `api_doc_gaps[]` (36 → 38, none modified or removed).
- This report.

No change was needed to `srs-fin.md`, `db-script-fin.md`, the execution plan, its mirrored
`packages/backend-execution/` blocks, the FIN Error Catalog, `packages/backend-test/`,
`test_gen/backend-test-plan-fin.md` or `state.json.atoms`: the TC set did not change, no FIN
error code was added or struck, no FIN endpoint or column changed, and the behaviour TC-FIN-016
asserts is exactly what the manifest already specified (404/405) — the code now simply delivers
it. `METHOD_NOT_ALLOWED` is a platform code in `com.erp.common`, not a `FIN-*` code, so it does
not belong in FIN's Error Catalog.
