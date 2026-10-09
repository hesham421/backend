# TEST EXECUTION MANIFEST — الحسابات العامة / Finance (General Ledger) (FIN)
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v1   Profile : erp   Scope : module
Emitted because `profile.stack.testing.manifest` is ON. A **derived view** for the `api-verify`
standalone: it introduces no id, defines nothing, gates nothing, and every cell below is an
`ID` or a value one of the cited artifacts already states.
Derived from : `backend-test-plan-fin.md` (this run) · `_state/current-backend-execution-plan.md`
               (v1) · `_state/current-srs.md` (v1) · `_state/current-registry-db.md` (v1)
Regenerate whenever the backend execution plan or the backend test plan changes — a stale
manifest is a defect.
══════════════════════════════════════════════════════════════════

## DEPENDENCY ORDER

The order in which entities must be built for any TC of this module to run. It is read off the FK
and XM relations of the backend plan and the "must belong to" rules, and nothing else.

```
0.  MDL · LookupType + LookupValue for FIN's 13 owned keys      (ENT-MDL-001/002, XM-FIN-001)
      ── not this module's data. Every lookup-backed column is validated by membership in the
         list MDL returns, so no FIN row carrying a code can be written before the keys exist.
         REQ-FIN-045 / TC-FIN-045 is the registration itself; every later TC assumes it done.
1.  SEC · ModuleRegistry, ScreenRegistry, ActionRegistry rows for FIN   (ENT-SEC-004/005/006)
      ── every endpoint below is gated, so no TC has a caller until FIN's 12 screens and 27
         actions are registered and granted. REQ-FIN-044 / TC-FIN-044 is that registration.
2.  ENT-FIN-001 · Account            (FIN_ACCOUNT)              ← API-FIN-002
      ── self-referencing through parentAccountId (DBF-FIN-007): roots before children, and a
         parent stops being a leaf as soon as it gains one (RULE-FIN-001). At most one row
         carries isRetainedEarningsFl (DBF-FIN-147), which API-FIN-027 derives rather than
         receives — TC-FIN-036 needs it present.
3.  ENT-FIN-002 · Dimension          (FIN_DIMENSION)            ← API-FIN-006
4.  ENT-FIN-003 · DimensionValue     (FIN_DIMENSION_VALUE)      ← API-FIN-007
      ── depends on 3: the code must be unique within its dimension (RULE-FIN-002), and the
         parent id arrives as the path id. Only API-FIN-035 can clear its active flag, which is
         TC-FIN-021's and TC-FIN-069's one setup step.
5.  ENT-FIN-007 · FiscalYear         (FIN_FISCAL_YEAR)          ← API-FIN-023
      ── TC-FIN-036 additionally needs a SUCCESSOR year whose startDate is the day after this
         year's endDate: the adjacency is derived, ENT-FIN-007 declaring no successor column.
6.  ENT-FIN-008 · FiscalPeriod       (FIN_FISCAL_PERIOD)        ← API-FIN-023 (generated with 5)
      ── never created directly; generated with its year, each period OPEN. Transitions run
         OPEN ⇄ SOFT_CLOSE → HARD_CLOSE → YEAR_END_CLOSE (API-FIN-024/025/026, then 027).
7.  ENT-FIN-009 · EventTypeRule      (FIN_EVENT_TYPE_RULE)      ← API-FIN-010
8.  ENT-FIN-010 · RuleLine           (FIN_RULE_LINE)            ← API-FIN-011
      ── depends on 7 (path id) and on 2 for any CONSTANT-derived account. RULE-FIN-003 is
         evaluated over the whole sibling set, so lines must be added in an order that never
         leaves a percentage set without exactly one remainder line at save time.
9.  ENT-FIN-011 · RecurringTemplate  (FIN_RECURRING_TEMPLATE)   ← API-FIN-013
10. ENT-FIN-012 · RecurringTemplateLine                         ← API-FIN-013 (same request)
      ── depends on 9, on 2 (accountId) and optionally on 4 (dimensionValueId).
11. ENT-FIN-013 · AllocationRule     (FIN_ALLOCATION_RULE)      ← API-FIN-016
12. ENT-FIN-014 · AllocationTarget   (FIN_ALLOCATION_TARGET)    ← API-FIN-016 (same request)
      ── depends on 11 and on 2 (sourceAccountId, targetAccountId); RULE-FIN-003 again.
13. ENT-FIN-004 · JournalEntry       (FIN_JOURNAL_ENTRY)        ← API-FIN-019, 020, and generated
                                                                   by 014, 017, 021, 027
14. ENT-FIN-005 · JournalLine        (FIN_JOURNAL_LINE)         ← written with 13
15. ENT-FIN-006 · JournalLineDimension                          ← written with 14
      ── 13–15 are one atomic write and depend on everything above: leaf active accounts (2),
         an Open period belonging to the submitted year (5, 6), valid active dimension values
         (3, 4), and — for an event-built entry — an active rule with its lines (7, 8). The
         docNo counter is scoped per fiscalYearId and restarts at 000001 each year, so a test
         asserting a document number must know which year's series it is drawing from.
```

Nothing is seeded outside these steps: every required column is written by an endpoint or by the
platform. Steps 0 and 1 are the only rows FIN does not create through an endpoint of its own.

## RULE → CODE → TC

Runtime code format `FIN-{http}[-{SLUG}]`; the envelope is
`LocalizedException → {code, messageAr, messageEn}`. Rows are the rules that raise a catalog code;
the rules whose effect is a success path or an omission are listed after the table rather than
given empty cells.

| RULE | catalog code | TC | HTTP | API |
|---|---|---|---|---|
| RULE-FIN-001 | FIN-409-HAS-CHILDREN | TC-FIN-002 · TC-FIN-054 (frontend) | 409 | API-FIN-003 |
| RULE-FIN-001 | FIN-409-PARENT-NOT-LEAF-ELIGIBLE | — no TC (no AC states the create-path variant) | 409 | API-FIN-002 |
| RULE-FIN-002 | FIN-409-DIMVALUE-DUP | TC-FIN-006 · TC-FIN-058 (frontend) | 409 | API-FIN-007 |
| RULE-FIN-003 | FIN-409-REMAINDER-COUNT | TC-FIN-009, TC-FIN-047 · TC-FIN-061, TC-FIN-073 (frontend) | 409 | API-FIN-011, API-FIN-016, API-FIN-017, API-FIN-020 |
| RULE-FIN-003 | FIN-422-REMAINDER-MARKER | — no TC (no AC states marker disagreement) | 422 | API-FIN-011, API-FIN-016, API-FIN-017, API-FIN-020 |
| RULE-FIN-004 | FIN-409-DUPLICATE-EVENT | TC-FIN-011 | 409 | API-FIN-020 |
| RULE-FIN-005 | FIN-404-NO-ACTIVE-RULE | TC-FIN-013 | 404 | API-FIN-020 |
| RULE-FIN-006 | FIN-409-UNBALANCED | TC-FIN-015, TC-FIN-018 · TC-FIN-063, TC-FIN-066 (frontend) | 409 | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 |
| RULE-FIN-007 | FIN-409-NOT-POSTABLE-ACCOUNT | TC-FIN-003, TC-FIN-015, TC-FIN-019 · TC-FIN-055, TC-FIN-063, TC-FIN-067 (frontend) | 409 | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 |
| RULE-FIN-008 | FIN-409-PERIOD-NOT-OPEN | TC-FIN-020, TC-FIN-033 · TC-FIN-068 (frontend) | 409 | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021 |
| RULE-FIN-008 | — (carve-out: the year-end CLOSING/OPENING entries are exempt, asserted as **not** raised) | TC-FIN-036 | — | API-FIN-027 |
| RULE-FIN-009 | FIN-409-INVALID-DIMENSION | TC-FIN-021 · TC-FIN-069 (frontend) | 409 | API-FIN-019, API-FIN-020, API-FIN-014, API-FIN-017, API-FIN-021, API-FIN-027 |
| RULE-FIN-010 | FIN-422-REMAINDER-NOT-POSITIVE | TC-FIN-048 | 422 | API-FIN-017, API-FIN-020 |
| RULE-FIN-013 | FIN-409-ALREADY-REVERSED | TC-FIN-030 · TC-FIN-078 (frontend) | 409 | API-FIN-021 |
| RULE-FIN-013 | FIN-409-NOT-POSTED | — no TC (AC-FIN-030's Given is the already-reversed entry; DRAFT is never a stored browsable state) | 409 | API-FIN-021, API-FIN-014 |
| RULE-FIN-014 | FIN-409-NOT-REOPENABLE | TC-FIN-035 · TC-FIN-083 (frontend) | 409 | API-FIN-024, API-FIN-026 |
| RULE-FIN-015 | FIN-403-FORBIDDEN | TC-FIN-038, TC-FIN-052 · TC-FIN-086 (frontend) | 403 | API-FIN-026, API-FIN-027 |
| RULE-FIN-017 | FIN-400-PERIOD-NOT-IN-YEAR, FIN-400-DOCDATE-OUTSIDE-PERIOD | — no TC (the rule was added at SVC-API and carries no AC of its own) | 400 | API-FIN-019 |

**Rules that raise no code, and why** — they are enforced, not informational, so they are named
rather than dropped: RULE-FIN-010's own computation (the per-side remainder difference) is a
success path, asserted by TC-FIN-012 and TC-FIN-026; RULE-FIN-011 (the exact mirrored reversal)
and RULE-FIN-012 (its period substitution) are success paths, asserted by TC-FIN-028, TC-FIN-029
and TC-FIN-024; RULE-FIN-016 is enforced by omission — no update and no delete mapping exists on
a POSTED row anywhere in the module — and is asserted by TC-FIN-016 and TC-FIN-017. The three
messages the SRS gives RULE-FIN-010, RULE-FIN-011 and RULE-FIN-012 are the only genuinely
informational strings in the module and no TC asserts them as a response body.

**Catalog rows no TC exercises**, listed so `api-verify` does not read this table as complete
coverage of the catalog: FIN-409-ACCOUNT-DUP, FIN-409-DIMENSION-DUP, FIN-409-RULE-DUP,
FIN-409-YEAR-DUP, FIN-400-INVALID-LOOKUP, FIN-400-MISSING-FREQUENCY, FIN-400-INVALID-SORT,
FIN-422-MAPPING-UNSUPPORTED, FIN-422-INVALID-PERCENTAGE-VALUE, FIN-409-NOT-ACTIVE,
FIN-409-INVALID-TRANSITION, FIN-409-PERIODS-NOT-CLOSED, the five `FIN-404-*` not-found rows
raised outside an AC's own path, and FIN-500. No `AC-FIN-*` states any of them; the absence is a
requirements-side gap, not a test-generation one — ADR-FIN-009.

**One row is not a gap and must not be tested**: `FIN-403-SOD-VIOLATION`, struck as unreachable.
Its two throw sites were deleted, and the global user-set-disjointness behaviour it signalled is
not what RULE-FIN-015 asks for. TC-FIN-038 asserts the permission gate and explicitly asserts
nothing about user-set disjointness.

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
| ENT-FIN-006 · JournalLineDimension | ✓ with its line | ✓ nested in API-FIN-022; read by API-FIN-032 | — none | — none | — n/a | — n/a |
| ENT-FIN-007 · FiscalYear | ✓ API-FIN-023 | — none published: the create response already carries the year with its periods | — **KNOWN GAP**: no fiscal-year search exists; year ids are discovered from a period row's own year id | ✓ statusCode only, set once by API-FIN-027 | — no soft-delete flag | — n/a |
| ENT-FIN-008 · FiscalPeriod | ✓ generated with its year by API-FIN-023 | — none published: API-FIN-033 returns the full row | ✓ API-FIN-033 | ✓ API-FIN-024, API-FIN-025, API-FIN-026 (the three state transitions) | — no soft-delete flag; HARD_CLOSE is the terminal state instead | — RULE-FIN-014: a hard-closed period never reopens |
| ENT-FIN-009 · EventTypeRule | ✓ API-FIN-010 | — none published | ✓ API-FIN-009 | — none published | ✓ API-FIN-034 | — none in FIN |
| ENT-FIN-010 · RuleLine | ✓ API-FIN-011 | ✓ embedded in the rule's own response | — none | — none published | — the entity carries no active flag | — n/a |
| ENT-FIN-011 · RecurringTemplate | ✓ API-FIN-013 | ✓ the search response carries the full aggregate, lines included | ✓ API-FIN-012 | — **OPEN GAP**: no update endpoint; a wrong template can only be retired and replaced | ✓ API-FIN-036 | — none in FIN |
| ENT-FIN-012 · RecurringTemplateLine | ✓ with its template | ✓ in the aggregate | — none | — none | — n/a | — n/a |
| ENT-FIN-013 · AllocationRule | ✓ API-FIN-016 | ✓ the search response carries the full aggregate, targets included | ✓ API-FIN-015 | — **OPEN GAP**: no update endpoint; same shape as ENT-FIN-011 | ✓ API-FIN-037 | — none in FIN |
| ENT-FIN-014 · AllocationTarget | ✓ with its rule | ✓ in the aggregate | — none | — none | — n/a | — n/a |

**Custom actions, which no CRUD column holds**: reverse an entry (API-FIN-021, gated by its own
`PERM_FIN_JOURNAL_ENTRIES_REVERSE`); run a template (API-FIN-014) and run an allocation rule
(API-FIN-017), both modelled as update-class actions; hard-close a period (API-FIN-026) and run
the year-end close (API-FIN-027), both gated by `PERM_FIN_PERIODS_CLOSE_APPROVE` — the distinct
permission RULE-FIN-015 requires.

**No DELETE column appears above** because FIN publishes no `DELETE` endpoint on any screen and
no `PERM_FIN_*_DELETE` row is registered. Every `—` in the table is a recorded decision or a
recorded gap, not an untested endpoint.
══════════════════════════════════════════════════════════════════
