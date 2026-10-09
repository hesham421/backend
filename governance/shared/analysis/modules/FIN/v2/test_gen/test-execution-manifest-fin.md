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
         EXPENSE_PAID in the v2 cases) and every PAYMENT_METHOD value but CHEQUE are site data.
         Each case that needs one creates it through API-MDL-006 (its `Host data` line), never
         a product seed; a case that needs a value in its own state uses its own (TRANSFER_102,
         TRANSFER_105, TRANSFER_133, CASH_103 — ADR-FIN-068). A mapping for a PAYMENT_METHOD
         value that MDL lacks is refused by RULE-FIN-024.
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
