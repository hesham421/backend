# BACKEND TEST PLAN — الحسابات العامة / Finance (General Ledger) (FIN)
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v1   Profile : erp   Scope : project (modules FIN, MDL, SEC)
Sources: srs-fin.md v1 · backend-execution-plan-fin.md v1 · registry-srs-fin.md v1 · registry-db-fin.md v1
Framework: agnostic. REDUCED: no. Open ADRs: 0 new (ADR-FIN-001 unaffected).
TC count: 89 (module scope) · 2 (integration — XM-FIN-001, FIN declares → MDL; XM-FIN-002, FIN declares → SEC)
Extended after ALIGN-BE with TC-FIN-049..091 (no existing TC id, marker or trace was changed).
══════════════════════════════════════════════════════════════════

<!-- PHASE:TEST-PLAN-BE:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-003,REQ-FIN-004,REQ-FIN-005,REQ-FIN-006,REQ-FIN-007,REQ-FIN-008,REQ-FIN-009,REQ-FIN-010,REQ-FIN-011,REQ-FIN-012,REQ-FIN-013,REQ-FIN-014,REQ-FIN-015,REQ-FIN-016,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-022,REQ-FIN-023,REQ-FIN-024,REQ-FIN-025,REQ-FIN-026,REQ-FIN-027,REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,REQ-FIN-031,REQ-FIN-032,REQ-FIN-033,REQ-FIN-034,REQ-FIN-035,REQ-FIN-036,REQ-FIN-037,REQ-FIN-038,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-044,REQ-FIN-045,REQ-FIN-046 -->

<!-- SUB:RULE-SCENARIOS:START traces=REQ-FIN-002,REQ-FIN-006,REQ-FIN-009,REQ-FIN-011,REQ-FIN-012,REQ-FIN-013,REQ-FIN-014,REQ-FIN-017,REQ-FIN-018,REQ-FIN-019,REQ-FIN-020,REQ-FIN-021,REQ-FIN-025,REQ-FIN-028,REQ-FIN-029,REQ-FIN-030,REQ-FIN-034,REQ-FIN-035,REQ-FIN-036,REQ-FIN-038 -->
### SUB — RULE-SCENARIOS (the 14 §12 must-honor points + SoD)

<!-- TC:TC-FIN-002:START traces=AC-FIN-002,REQ-FIN-002,API-FIN-003 -->
### TC-FIN-002 — reject direct-posting on an account with children
Derived from : AC-FIN-002 (REQ-FIN-002) · Exercises: API-FIN-003 PUT /api/v1/fin/accounts/{id}
Rule / code  : RULE-FIN-001 → FIN-409-HAS-CHILDREN
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an account with ≥1 child account
Steps        : 1. PUT {isLeafFl: true} on the parent
Expected     : 409 FIN-409-HAS-CHILDREN; isLeafFl unchanged
Test data    : parent account with 1 child
<!-- TC:TC-FIN-002:END -->

<!-- TC:TC-FIN-006:START traces=AC-FIN-006,REQ-FIN-006,API-FIN-007 -->
### TC-FIN-006 — reject a duplicate code within a dimension
Derived from : AC-FIN-006 (REQ-FIN-006) · Exercises: API-FIN-007 POST /api/v1/fin/dimensions/{id}/values
Rule / code  : RULE-FIN-002 → FIN-409-DIMVALUE-DUP
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a dimension already holding value code "NORTH"
Steps        : 1. POST a second value with code "NORTH" under the same dimension
Expected     : 409 FIN-409-DIMVALUE-DUP; no second row
Test data    : dimension "REGION", code "NORTH"
<!-- TC:TC-FIN-006:END -->

<!-- TC:TC-FIN-009:START traces=AC-FIN-009,REQ-FIN-009,API-FIN-011 -->
### TC-FIN-009 — reject an incorrect remainder-line count
Derived from : AC-FIN-009 (REQ-FIN-009) · Exercises: API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-003 → FIN-409-REMAINDER-COUNT
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a rule with 2 PERCENTAGE lines and 0 lines marked remainder
Steps        : 1. attempt to save the rule as-is
Expected     : 409 FIN-409-REMAINDER-COUNT until exactly one line is marked remainder
Test data    : 2 PERCENTAGE lines (30%, 70%), 0 remainder lines
<!-- TC:TC-FIN-009:END -->

<!-- TC:TC-FIN-011:START traces=AC-FIN-011,REQ-FIN-011,API-FIN-020 -->
### TC-FIN-011 — reject a duplicate event reference (§12.12 idempotency)
Derived from : AC-FIN-011 (REQ-FIN-011) · Exercises: API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-004 → FIN-409-DUPLICATE-EVENT
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a POSTED entry already exists with eventReference "EVT-1001"
Steps        : 1. submit another event with reference "EVT-1001"
Expected     : 409 FIN-409-DUPLICATE-EVENT; no second entry posted
Test data    : eventReference "EVT-1001" (already used)
<!-- TC:TC-FIN-011:END -->

<!-- TC:TC-FIN-012:START traces=AC-FIN-012,REQ-FIN-012,API-FIN-020 -->
### TC-FIN-012 — remainder line absorbs the rounding difference exactly (§12.6)
Derived from : AC-FIN-012 (REQ-FIN-012) · Exercises: API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-010 → (success-path computation, no error code)
Scenario     : HAPPY · data class BOUNDARY · language ALL
Preconditions: a rule distributing 100.00 as 33% + 33% + remainder
Steps        : 1. submit the triggering event with amount=100.00
Expected     : the two percentage lines total 66.00 (each rounded to the smallest currency
  unit); the remainder line is exactly 34.00; total debits = total credits = 100.00 exactly
Test data    : amount 100.00, split 33%/33%/remainder
<!-- TC:TC-FIN-012:END -->

<!-- TC:TC-FIN-013:START traces=AC-FIN-013,REQ-FIN-013,API-FIN-020 -->
### TC-FIN-013 — reject an event with no active rule
Derived from : AC-FIN-013 (REQ-FIN-013) · Exercises: API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-005 → FIN-404-NO-ACTIVE-RULE
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an event type with no active EventTypeRule
Steps        : 1. submit an event of that type
Expected     : 404 FIN-404-NO-ACTIVE-RULE; the failure is recorded for operator follow-up; no entry created
Test data    : eventType "NEVER_CONFIGURED"
<!-- TC:TC-FIN-013:END -->

<!-- TC:TC-FIN-017:START traces=AC-FIN-017,REQ-FIN-017,API-FIN-019 -->
### TC-FIN-017 — unified validation + direct posting, no separate approval (§8.1, §12 no per-entry approval)
Derived from : AC-FIN-017 (REQ-FIN-017) · Exercises: API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-016 → (success-path; entry locks immediately on post)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a balanced DRAFT that passes every check (RULE-FIN-006..009)
Steps        : 1. submit the entry — 2. immediately attempt to edit or delete it (no approval step exists between build and post)
Expected     : 1. statusCode=POSTED, postedAt set, in the same call/transaction as validation — 2. step 2 is rejected (no UPDATE/DELETE route on a POSTED row)
Test data    : a fully valid 2-line balanced manual entry
<!-- TC:TC-FIN-017:END -->

<!-- TC:TC-FIN-018:START traces=AC-FIN-018,REQ-FIN-018,API-FIN-019 -->
### TC-FIN-018 — reject an unbalanced entry (§12.1 debit=credit invariant)
Derived from : AC-FIN-018 (REQ-FIN-018) · Exercises: API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-006 → FIN-409-UNBALANCED
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: lines totaling debit 100.00, credit 99.99
Steps        : 1. submit the entry
Expected     : 409 FIN-409-UNBALANCED, imbalance amount shown; nothing posted (also re-run via API-FIN-020/014/017 — same RULE, same code, per Exercises union)
Test data    : debit 100.00 / credit 99.99
<!-- TC:TC-FIN-018:END -->

<!-- TC:TC-FIN-019:START traces=AC-FIN-019,REQ-FIN-019,API-FIN-019 -->
### TC-FIN-019 — reject a non-postable account (§12.3 leaf/active only)
Derived from : AC-FIN-019 (REQ-FIN-019) · Exercises: API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-007 → FIN-409-NOT-POSTABLE-ACCOUNT
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a line targeting a non-leaf (rollup) account
Steps        : 1. submit the entry
Expected     : 409 FIN-409-NOT-POSTABLE-ACCOUNT, offending line named; nothing posted
Test data    : a rollup account with isLeafFl=false
<!-- TC:TC-FIN-019:END -->

<!-- TC:TC-FIN-020:START traces=AC-FIN-020,REQ-FIN-020,API-FIN-019 -->
### TC-FIN-020 — reject a closed period at post time (§12.4 period gate at post, not build)
Derived from : AC-FIN-020 (REQ-FIN-020) · Exercises: API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-008 → FIN-409-PERIOD-NOT-OPEN
Scenario     : VIOLATION · data class EDGE · language ALL
Preconditions: an entry built while its period was Open, then the period is Hard Closed before posting completes
Steps        : 1. build the entry (period Open) — 2. hard-close the period — 3. complete posting
Expected     : 409 FIN-409-PERIOD-NOT-OPEN at step 3, citing the period's current state
Test data    : period transitioned OPEN→HARD_CLOSE between build and post
<!-- TC:TC-FIN-020:END -->

<!-- TC:TC-FIN-021:START traces=AC-FIN-021,REQ-FIN-021,API-FIN-019 -->
### TC-FIN-021 — reject an invalid dimension value (§12.11 dimensions as posting identity)
Derived from : AC-FIN-021 (REQ-FIN-021) · Exercises: API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-009 → FIN-409-INVALID-DIMENSION
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a line citing an inactive DimensionValue
Steps        : 1. submit the entry
Expected     : 409 FIN-409-INVALID-DIMENSION, offending line + dimension named
Test data    : an inactive dimension value
<!-- TC:TC-FIN-021:END -->

<!-- TC:TC-FIN-028:START traces=AC-FIN-028,REQ-FIN-028,API-FIN-021 -->
### TC-FIN-028 — reversal is exact and bidirectionally linked (§12.7)
Derived from : AC-FIN-028 (REQ-FIN-028) · Exercises: API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse
Rule / code  : RULE-FIN-011 → (success-path)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a POSTED entry with 3 lines
Steps        : 1. reverse it
Expected     : a new POSTED entry with the same 3 lines' amounts and opposite directions;
  the ORIGINAL STAYS POSTED (classic reversal — its statusCode is unchanged, never VOID);
  original.reversalEntryId = new.id and new.originalEntryId = original.id;
  every affected account's net balance across the two entries is zero
Test data    : 3-line posted entry
<!-- TC:TC-FIN-028:END -->

<!-- TC:TC-FIN-029:START traces=AC-FIN-029,REQ-FIN-029,API-FIN-021 -->
### TC-FIN-029 — reversal posts to the current period when the original's is closed
Derived from : AC-FIN-029 (REQ-FIN-029) · Exercises: API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse
Rule / code  : RULE-FIN-012 → (success-path, period substitution)
Scenario     : STATE · data class EDGE · language ALL
Preconditions: a POSTED entry whose period is now Hard Closed
Steps        : 1. reverse it
Expected     : the reversal posts into the current open period, not the closed one
Test data    : entry in a now-hard-closed period
<!-- TC:TC-FIN-029:END -->

<!-- TC:TC-FIN-030:START traces=AC-FIN-030,REQ-FIN-030,API-FIN-021 -->
### TC-FIN-030 — reject reversing a non-POSTED (DRAFT) entry
Derived from : AC-FIN-030 (REQ-FIN-030) · Exercises: API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse
Rule / code  : RULE-FIN-013 → FIN-409-NOT-POSTED
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an entry that is not POSTED — a DRAFT entry. NOT REACHABLE THROUGH ANY API:
  build, validate and post happen in one transaction and only the POSTED row is ever written
  (the entry is posted before the single save), so no endpoint leaves a DRAFT row behind; the
  row must be seeded directly into FIN_JOURNAL_ENTRY with statusCode=DRAFT for this case to
  exist. (Under classic reversal an already-reversed entry stays POSTED, so it is no longer a
  non-POSTED case; that half of RULE-FIN-013 is now its own guard and its own case, TC-FIN-048.)
Steps        : 1. attempt to reverse it
Expected     : 409 FIN-409-NOT-POSTED
Test data    : a DRAFT (unposted) entry
<!-- TC:TC-FIN-030:END -->

<!-- TC:TC-FIN-048:START traces=AC-FIN-030,REQ-FIN-030,API-FIN-021 -->
### TC-FIN-048 — reject reversing an entry that has already been reversed (double-reversal)
Derived from : AC-FIN-030 (REQ-FIN-030) · Exercises: API-FIN-021 POST /api/v1/fin/journal-entries/{id}/reverse
Rule / code  : RULE-FIN-013 -> FIN-409-ALREADY-REVERSED
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a POSTED entry that has already been reversed once — under classic reversal it
  STAYS POSTED and carries reversalEntryId pointing at its reversal (the linked-entry
  precondition that replaces the old, now unreachable statusCode=VOID one)
Steps        : 1. attempt to reverse the same original entry a second time
Expected     : 409 FIN-409-ALREADY-REVERSED; no second reversal entry is created, and every
  affected account's net balance across the original and its single reversal stays zero
  (a second mirror would leave a net effect of -(original))
Test data    : a posted entry already reversed once, with its reversal link set
<!-- TC:TC-FIN-048:END -->

<!-- TC:TC-FIN-035:START traces=AC-FIN-035,REQ-FIN-035,API-FIN-024 -->
### TC-FIN-035 — reject reopening a hard-closed period
Derived from : AC-FIN-035 (REQ-FIN-035) · Exercises: API-FIN-024 PATCH /api/v1/fin/fiscal-periods/{id}/open
Rule / code  : RULE-FIN-014 → FIN-409-NOT-REOPENABLE
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a Hard Closed period
Steps        : 1. attempt to reopen it
Expected     : 409 FIN-409-NOT-REOPENABLE
Test data    : a hard-closed period
<!-- TC:TC-FIN-035:END -->

<!-- TC:TC-FIN-038:START traces=AC-FIN-038,REQ-FIN-038,API-FIN-026 -->
### TC-FIN-038 — close-approval permission distinct from entry-creation permission (SoD)
Derived from : AC-FIN-038 (REQ-FIN-038) · Exercises: API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close
Rule / code  : RULE-FIN-015 → FIN-403-SOD-VIOLATION
Scenario     : PERMISSION · data class ATTACK · language ALL
Preconditions: a role holding only `PERM_FIN_JOURNAL_ENTRIES_CREATE` (no close-approve permission)
Steps        : 1. that role's user attempts hard-close
Expected     : 403 FIN-403-SOD-VIOLATION (or 403 FIN-403-FORBIDDEN if the permission itself is entirely absent — both paths tested)
Test data    : role with entry-creation only
<!-- TC:TC-FIN-038:END -->

<!-- TC:TC-FIN-049:START traces=AC-FIN-014,REQ-FIN-014,REQ-FIN-017,API-FIN-019 -->
### TC-FIN-049 — reject a period that does not belong to the submitted fiscal year
Derived from : AC-FIN-014 (REQ-FIN-014, REQ-FIN-017) · Exercises: API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-017 → FIN-400-PERIOD-NOT-IN-YEAR
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: two fiscal years, FY-A and FY-B, each with its own OPEN periods; an otherwise
  valid balanced 2-line manual payload
Steps        : 1. POST the entry with fiscalYearId = FY-B and periodId = an OPEN period of FY-A
Expected     : 400 FIN-400-PERIOD-NOT-IN-YEAR, ar "الفترة المحددة لا تتبع السنة المالية المحددة" /
  en "The selected period does not belong to the selected fiscal year"; fail-fast, so
  RULE-FIN-006/007/008/009 are never collected; nothing posted and no docNo consumed from either
  year's series
Test data    : fiscalYearId FY-B, periodId = FY-A period 1, docDate inside that period
<!-- TC:TC-FIN-049:END -->

<!-- TC:TC-FIN-050:START traces=AC-FIN-014,REQ-FIN-014,REQ-FIN-017,API-FIN-019 -->
### TC-FIN-050 — reject a document date outside the submitted period's span
Derived from : AC-FIN-014 (REQ-FIN-014, REQ-FIN-017) · Exercises: API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-017 → FIN-400-DOCDATE-OUTSIDE-PERIOD
Scenario     : VIOLATION · data class BOUNDARY · language ALL
Preconditions: an OPEN period spanning [startDate, endDate] inside its own fiscal year
Steps        : 1. POST the entry with docDate = endDate + 1 day — 2. repeat with docDate = startDate − 1 day
Expected     : both attempts 400 FIN-400-DOCDATE-OUTSIDE-PERIOD, ar "تاريخ المستند خارج نطاق الفترة المحددة" /
  en "The document date falls outside the selected period"; nothing posted
Test data    : period 2026-02-01..2026-02-28; docDate 2026-03-01, then 2026-01-31
<!-- TC:TC-FIN-050:END -->

<!-- TC:TC-FIN-051:START traces=AC-FIN-014,REQ-FIN-014,REQ-FIN-017,API-FIN-019 -->
### TC-FIN-051 — coherent header accepted at both period boundaries (RULE-FIN-017 satisfied)
Derived from : AC-FIN-014 (REQ-FIN-014, REQ-FIN-017) · Exercises: API-FIN-019 POST /api/v1/fin/journal-entries
Rule / code  : RULE-FIN-017 → (success path, no error code)
Scenario     : HAPPY · data class BOUNDARY · language ALL
Preconditions: an OPEN period belonging to the submitted fiscal year, spanning [startDate, endDate]
Steps        : 1. POST a balanced entry with docDate = startDate — 2. POST a second one with docDate = endDate
Expected     : both 201, statusCode=POSTED; the inclusive bounds are accepted (the rule rejects
  only dates strictly outside the span)
Test data    : period 2026-02-01..2026-02-28; docDate 2026-02-01, then 2026-02-28
<!-- TC:TC-FIN-051:END -->

<!-- TC:TC-FIN-052:START traces=AC-FIN-009,REQ-FIN-009,API-FIN-011 -->
### TC-FIN-052 — reject a rule line whose remainder marker disagrees with its own type code
Derived from : AC-FIN-009 (REQ-FIN-009) · Exercises: API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Rule / code  : RULE-FIN-003 → FIN-422-REMAINDER-MARKER
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an existing event-type rule; `isRemainderFl` (DBF-FIN-103) is the single marker the
  API-FIN-020 builder reads
Steps        : 1. POST a line with amountSourceTypeCode=REMAINDER and isRemainderFl=false —
  2. POST a line with isRemainderFl=true and distributionTypeCode=FIXED, amountSourceTypeCode=FIELD
Expected     : both attempts 422 FIN-422-REMAINDER-MARKER, ar "علامة سطر الباقي لا تتفق مع نوع التوزيع أو مصدر المبلغ لنفس السطر" /
  en "The remainder marker disagrees with the line's own distribution or amount-source type";
  no line stored, so the ambiguity can never reach the builder
Test data    : the two disagreeing line payloads above
<!-- TC:TC-FIN-052:END -->

<!-- TC:TC-FIN-053:START traces=AC-FIN-025,REQ-FIN-025,API-FIN-016 -->
### TC-FIN-053 — reject an allocation target set with the wrong remainder-target count
Derived from : AC-FIN-025 (REQ-FIN-025) · Exercises: API-FIN-016 POST /api/v1/fin/allocation-rules
Rule / code  : RULE-FIN-003 (target half, QR-FIN-016) → FIN-409-REMAINDER-COUNT; a marker/type
  disagreement on a target → FIN-422-REMAINDER-MARKER
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a source account; the rule is submitted with its targets in one call
Steps        : 1. POST a rule with 2 PERCENTAGE targets (40%/40%) and 0 targets marked remainder —
  2. POST a rule with 2 targets both marked isRemainderFl=true —
  3. POST a rule with one target isRemainderFl=true but distributionTypeCode=FIXED
Expected     : 1 and 2 → 409 FIN-409-REMAINDER-COUNT; 3 → 422 FIN-422-REMAINDER-MARKER; no rule
  and no target row persisted in any of the three
Test data    : source account 5000; targets as above
<!-- TC:TC-FIN-053:END -->

<!-- TC:TC-FIN-054:START traces=AC-FIN-012,REQ-FIN-012,API-FIN-020 -->
### TC-FIN-054 — the remainder is computed PER SIDE, never against every line
Derived from : AC-FIN-012 (REQ-FIN-012) · Exercises: API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Rule / code  : RULE-FIN-010 → (success-path computation; a side-blind total would produce a
  negative amount and die on CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE)
Scenario     : HAPPY · data class BOUNDARY · language ALL
Preconditions: an active rule with three lines — DEBIT/FIELD from the event's base amount,
  CREDIT/PERCENTAGE 60%, CREDIT/REMAINDER (isRemainderFl=true)
Steps        : 1. submit the triggering event with baseAmount = 1000.00
Expected     : 201, statusCode=POSTED; the credit percentage line is 600.0000 and the remainder
  line is 400.0000 — the difference between the OPPOSING side's total (debit 1000) and the
  remainder line's own side so far (credit 600), not 1000 − (1000 + 600); the entry balances
  D 1000.00 = C 600.00 + 400.00
Test data    : baseAmount 1000.00; lines DEBIT/FIELD, CREDIT/PERCENTAGE 60%, CREDIT/REMAINDER
<!-- TC:TC-FIN-054:END -->

<!-- TC:TC-FIN-055:START traces=AC-FIN-012,REQ-FIN-012,API-FIN-020 -->
### TC-FIN-055 — reject a distribution whose remainder computes to zero or less
Derived from : AC-FIN-012 (REQ-FIN-012) · Exercises: API-FIN-020 POST /api/v1/fin/journal-entries/from-event
  (same code on API-FIN-017 POST /api/v1/fin/allocation-rules/{id}/run)
Rule / code  : RULE-FIN-010 → FIN-422-REMAINDER-NOT-POSITIVE
Scenario     : VIOLATION · data class BOUNDARY · language ALL
Preconditions: an active rule whose non-remainder lines on the remainder's own side already
  equal the opposing side's total — DEBIT/FIELD 1000, CREDIT/PERCENTAGE 100%, CREDIT/REMAINDER
Steps        : 1. submit the triggering event with baseAmount = 1000.00
Expected     : 422 FIN-422-REMAINDER-NOT-POSITIVE, ar "سطر الباقي يُحسب كفرق ويجب أن يكون موجبًا؛ السطور الأخرى تستهلك المبلغ بالكامل" /
  en "The remainder line is computed as a difference and must be positive; the other lines
  already consume the full amount" — never an unlocalized DATA_INTEGRITY_VIOLATION from
  CHK_FIN_JOURNAL_LINE_AMOUNT_POSITIVE; nothing posted
Test data    : baseAmount 1000.00; CREDIT/PERCENTAGE 100% + CREDIT/REMAINDER
<!-- TC:TC-FIN-055:END -->

<!-- TC:TC-FIN-056:START traces=AC-FIN-036,REQ-FIN-036,API-FIN-027 -->
### TC-FIN-056 — RULE-FIN-008's year-end exemption: CLOSING/OPENING post into non-Open periods
Derived from : AC-FIN-036 (REQ-FIN-036) · Exercises: API-FIN-027 POST /api/v1/fin/fiscal-years/{id}/year-end-close
Rule / code  : RULE-FIN-008 (year-end carve-out) → (success path); RULE-FIN-006/007/009 still
  apply to both generated entries in full
Scenario     : STATE · data class EDGE · language ALL
Preconditions: KNOWN-BLOCKED ENDPOINT — see TC-FIN-059's precondition block; all of it must hold:
  a user holding FIN_CLOSE_APPROVER and no role carrying PERM_FIN_JOURNAL_ENTRIES_CREATE, an
  account with is_retained_earnings_fl = TRUE (set as data — no endpoint sets it), every period
  of the year HARD_CLOSE, and an adjacent successor year whose startDate = this year's endDate + 1
Steps        : 1. run year-end close as that approver
Expected     : 201; the CLOSING entry posts into the year's LAST period even though that period is
  HARD_CLOSE, and the OPENING entry posts into the successor year's first period — neither is
  rejected with FIN-409-PERIOD-NOT-OPEN; a manual entry (API-FIN-019) carrying
  journalTypeCode="CLOSING" into the same period is still rejected 409 FIN-409-PERIOD-NOT-OPEN,
  so the exemption cannot be bought by a caller-supplied journal type
Test data    : a fully hard-closed year with posted result-account activity
<!-- TC:TC-FIN-056:END -->

<!-- TC:TC-FIN-057:START traces=AC-FIN-002,REQ-FIN-002,API-FIN-003 -->
### TC-FIN-057 — marking a childless account as a leaf is accepted (RULE-FIN-001 satisfied)
Derived from : AC-FIN-002 (REQ-FIN-002) · Exercises: API-FIN-003 PUT /api/v1/fin/accounts/{id}
Rule / code  : RULE-FIN-001 → (success path, no error code)
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an account with zero child accounts and isLeafFl=false
Steps        : 1. PUT {nameAr, nameEn, isLeafFl: true}
Expected     : 200 `AccountResponse` with isLeafFl=true; the account then accepts a posting
  (the mirror of TC-FIN-019)
Test data    : a childless rollup account
<!-- TC:TC-FIN-057:END -->

<!-- TC:TC-FIN-058:START traces=AC-FIN-002,REQ-FIN-002,API-FIN-002 -->
### TC-FIN-058 — reject a child under a parent that is still marked as accepting direct posting
Derived from : AC-FIN-002 (REQ-FIN-002) · Exercises: API-FIN-002 POST /api/v1/fin/accounts
Rule / code  : RULE-FIN-001 (create trigger, QR-FIN-006) → FIN-409-PARENT-NOT-LEAF-ELIGIBLE
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an existing account with isLeafFl=true and no children
Steps        : 1. POST a new account naming it as parentAccountId — 2. demote the parent through
  API-FIN-003 (isLeafFl=false) — 3. repeat step 1
Expected     : 1. 409 FIN-409-PARENT-NOT-LEAF-ELIGIBLE and no child row created — the parent's
  isLeafFl is NEVER flipped automatically — 3. 201, the child is created once the parent has been
  demoted explicitly
Test data    : parent "1000" isLeafFl=true; child "1001"
<!-- TC:TC-FIN-058:END -->

<!-- TC:TC-FIN-059:START traces=AC-FIN-034,REQ-FIN-034,REQ-FIN-038,API-FIN-026 -->
### TC-FIN-059 — hard-close succeeds for a close-approver who creates no entries (RULE-FIN-015 satisfied)
Derived from : AC-FIN-034 (REQ-FIN-034, REQ-FIN-038) · Exercises: API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close
Rule / code  : RULE-FIN-015 → (success path); XM-FIN-002 supplies the two user sets
Scenario     : PERMISSION · data class VALID · language ALL
Preconditions: KNOWN-BLOCKED ENDPOINT — this cannot succeed on a fresh deployment and the setup is
  part of the scenario. V27 mints the role FIN_CLOSE_APPROVER (module FIN, screen FIN_PERIODS,
  exactly PERM_FIN_PERIODS_VIEW + PERM_FIN_PERIODS_CLOSE_APPROVE) but assigns it to NO user, and
  the bootstrap `admin` holds SYS_ADMIN, which V25 granted PERM_FIN_JOURNAL_ENTRIES_CREATE. The
  test must therefore create a second SEC user whose ROLE UNION carries FIN_CLOSE_APPROVER and no
  role granting PERM_FIN_JOURNAL_ENTRIES_CREATE, and assign it through SEC's own role
  administration. Also required: a SOFT_CLOSE period whose entries were created by `admin`
Steps        : 1. hard-close the period as that second user
Expected     : 200, statusCode=HARD_CLOSE, closedBy = that user and closedAt set; with the setup
  omitted the same call answers 403 FIN-403-SOD-VIOLATION (TC-FIN-060), which is the fresh-deployment
  behaviour, not a defect
Test data    : user "fin_closer" holding only FIN_CLOSE_APPROVER; a soft-closed period
<!-- TC:TC-FIN-059:END -->

<!-- TC:TC-FIN-060:START traces=AC-FIN-038,REQ-FIN-038,API-FIN-026 -->
### TC-FIN-060 — close fails when NOBODY holds close-approval (fresh deployment)
Derived from : AC-FIN-038 (REQ-FIN-038) · Exercises: API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close
Rule / code  : RULE-FIN-015 → FIN-403-SOD-VIOLATION
Scenario     : PERMISSION · data class EDGE · language ALL
Preconditions: the delivered state — FIN_CLOSE_APPROVER exists (V27) but is assigned to no user,
  so the set of users holding PERM_FIN_PERIODS_CLOSE_APPROVE is empty
Steps        : 1. a SOFT_CLOSE period is hard-closed by any authenticated principal
Expected     : 403 FIN-403-SOD-VIOLATION, ar "صلاحية اعتماد الإغلاق منفصلة عن صلاحية إنشاء القيود" /
  en "The close-approval permission is separate from the entry-creation permission"; the period
  stays SOFT_CLOSE. An empty approver set fails the rule exactly as an overlapping one does —
  documented in V27's header, not a defect
Test data    : untouched V27 state, no SEC_USER_ROLE row for FIN_CLOSE_APPROVER
<!-- TC:TC-FIN-060:END -->

<!-- TC:TC-FIN-061:START traces=AC-FIN-038,REQ-FIN-038,API-FIN-027 -->
### TC-FIN-061 — close fails when ONE user holds both permissions across their role union
Derived from : AC-FIN-038 (REQ-FIN-038) · Exercises: API-FIN-027 POST /api/v1/fin/fiscal-years/{id}/year-end-close
  (same code and same guard on API-FIN-026)
Rule / code  : RULE-FIN-015 → FIN-403-SOD-VIOLATION; the fact is read through XM-FIN-002
Scenario     : PERMISSION · data class ATTACK · language ALL
Preconditions: FIN_CLOSE_APPROVER assigned to the bootstrap `admin`, who already holds SYS_ADMIN
  and therefore PERM_FIN_JOURNAL_ENTRIES_CREATE — the two user sets now intersect
Steps        : 1. run year-end close as `admin`
Expected     : 403 FIN-403-SOD-VIOLATION, raised BEFORE the year's status, the all-periods check,
  the successor year and the Retained Earnings lookup (the validation order is part of the
  contract), so the answer is 403 and never FIN-404-YEAR or FIN-404-ACCOUNT; nothing posted and
  the year stays OPEN
Test data    : `admin` holding SYS_ADMIN + FIN_CLOSE_APPROVER
<!-- TC:TC-FIN-061:END -->

<!-- TC:TC-FIN-062:START traces=AC-FIN-038,REQ-FIN-038,API-FIN-026 -->
### TC-FIN-062 — the gateway rule resolves the two-word action CLOSE_APPROVE
Derived from : AC-FIN-038 (REQ-FIN-038) · Exercises: API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close
Rule / code  : platform gateway convention (every non-VIEW permission needs a granted VIEW on the
  SAME SCREEN) → 403 rendered as the platform `ACCESS_DENIED` envelope, the catalog's
  FIN-403-FORBIDDEN row
Scenario     : PERMISSION · data class ATTACK · language ALL
Preconditions: a role granted PERM_FIN_PERIODS_CLOSE_APPROVE on screen FIN_PERIODS but WITHOUT the
  screen's PERM_FIN_PERIODS_VIEW gateway row; and, for the passing half, FIN_CLOSE_APPROVER, which
  holds both
Steps        : 1. call hard-close as the gateway-less role's user — 2. call it as a FIN_CLOSE_APPROVER
  holder (TC-FIN-059's setup)
Expected     : 1. the authority is stripped during resolution and the call answers 403 with the
  platform body {code: "ACCESS_DENIED"}, an English message and no ar/en localization (the known
  open ERROR ENVELOPE finding — assert that body, not a FIN code) — 2. the authority survives and
  the call reaches the RULE-FIN-015 check, proving the gateway is matched against the screen's own
  registry rows and not by splitting the code at its last underscore (which would demand a
  non-existent PERM_FIN_PERIODS_CLOSE_VIEW)
Test data    : one role with CLOSE_APPROVE only; FIN_CLOSE_APPROVER for the contrast
<!-- TC:TC-FIN-062:END -->
<!-- SUB:RULE-SCENARIOS:END -->

<!-- SUB:API-SCENARIOS:START traces=REQ-FIN-001,REQ-FIN-002,REQ-FIN-003,REQ-FIN-004,REQ-FIN-005,REQ-FIN-007,REQ-FIN-008,REQ-FIN-010,REQ-FIN-014,REQ-FIN-015,REQ-FIN-016,REQ-FIN-022,REQ-FIN-023,REQ-FIN-024,REQ-FIN-025,REQ-FIN-026,REQ-FIN-027,REQ-FIN-031,REQ-FIN-032,REQ-FIN-033,REQ-FIN-034,REQ-FIN-036,REQ-FIN-037,REQ-FIN-039,REQ-FIN-040,REQ-FIN-041,REQ-FIN-042,REQ-FIN-043,REQ-FIN-044,REQ-FIN-045,REQ-FIN-046 -->
### SUB — API-SCENARIOS

<!-- TC:TC-FIN-001:START traces=AC-FIN-001,REQ-FIN-001,API-FIN-002 -->
### TC-FIN-001 — create an account
Derived from : AC-FIN-001 (REQ-FIN-001) · Exercises: API-FIN-002 POST /api/v1/fin/accounts
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: unique code; registered owner-consistent accountTypeCode/natureCode
Steps        : 1. POST {code, nameAr, nameEn, accountTypeCode: "ASSET", natureCode: "DEBIT"}
Expected     : 201; account created
Test data    : code "1000", ASSET/DEBIT
<!-- TC:TC-FIN-001:END -->

<!-- TC:TC-FIN-003:START traces=AC-FIN-003,REQ-FIN-003,API-FIN-004 -->
### TC-FIN-003 — deactivate an account blocks future posting
Derived from : AC-FIN-003 (REQ-FIN-003) · Exercises: API-FIN-004 PUT /api/v1/fin/accounts/{id}/deactivate
Scenario     : STATE · data class VALID · language ALL
Preconditions: an active account
Steps        : 1. PUT /{id}/deactivate (no body) — 2. attempt to post to it (TC-FIN-019 pattern)
Expected     : 1. 200 `AccountResponse`, isActiveFl=false — 2. rejected (FIN-409-NOT-POSTABLE-ACCOUNT)
Test data    : a leaf account with no postings yet
<!-- TC:TC-FIN-003:END -->

<!-- TC:TC-FIN-004:START traces=AC-FIN-004,REQ-FIN-004,API-FIN-006 -->
### TC-FIN-004 — create a dimension
Derived from : AC-FIN-004 (REQ-FIN-004) · Exercises: API-FIN-006 POST /api/v1/fin/dimensions
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: unique code
Steps        : 1. POST {code, nameAr, nameEn}
Expected     : 201; active Dimension created
Test data    : code "PROJECT"
<!-- TC:TC-FIN-004:END -->

<!-- TC:TC-FIN-005:START traces=AC-FIN-005,REQ-FIN-005,API-FIN-007 -->
### TC-FIN-005 — create a dimension value
Derived from : AC-FIN-005 (REQ-FIN-005) · Exercises: API-FIN-007 POST /api/v1/fin/dimensions/{id}/values
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a code not yet used under the dimension
Steps        : 1. POST {code, nameAr, nameEn, sortOrder}
Expected     : 201; DimensionValue created
Test data    : dimension "PROJECT", code "P100"
<!-- TC:TC-FIN-005:END -->

<!-- TC:TC-FIN-007:START traces=AC-FIN-007,REQ-FIN-007,API-FIN-010 -->
### TC-FIN-007 — create an event-type rule
Derived from : AC-FIN-007 (REQ-FIN-007) · Exercises: API-FIN-010 POST /api/v1/fin/event-rules
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an event type with no existing rule
Steps        : 1. POST {eventTypeCode, nameAr, nameEn}
Expected     : 201; active EventTypeRule created
Test data    : eventTypeCode "INVOICE_PAID"
<!-- TC:TC-FIN-007:END -->

<!-- TC:TC-FIN-008:START traces=AC-FIN-008,REQ-FIN-008,API-FIN-011 -->
### TC-FIN-008 — add a rule line
Derived from : AC-FIN-008 (REQ-FIN-008) · Exercises: API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an existing rule
Steps        : 1. POST {accountDerivationTypeCode, accountDerivationValue, amountSourceTypeCode, directionCode, distributionTypeCode}
Expected     : 201; RuleLine created
Test data    : CONSTANT derivation, FIELD amount source, DEBIT
<!-- TC:TC-FIN-008:END -->

<!-- TC:TC-FIN-010:START traces=AC-FIN-010,REQ-FIN-010,API-FIN-020 -->
### TC-FIN-010 — build an entry from an event using its rule
Derived from : AC-FIN-010 (REQ-FIN-010) · Exercises: API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an event type with an active rule
Steps        : 1. submit a valid event of that type
Expected     : a DRAFT is built per the rule, then proceeds through TC-FIN-017's validate+post
Test data    : event type "INVOICE_PAID", one matching payload
<!-- TC:TC-FIN-010:END -->

<!-- TC:TC-FIN-014:START traces=AC-FIN-014,REQ-FIN-014,API-FIN-019 -->
### TC-FIN-014 — create a manual journal entry
Derived from : AC-FIN-014 (REQ-FIN-014) · Exercises: API-FIN-019 POST /api/v1/fin/journal-entries
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: ≥2 balanced lines, leaf active accounts, an Open period
Steps        : 1. submit the entry
Expected     : 201, statusCode=POSTED, docNo generated
Test data    : 2-line balanced entry, 100.00 debit/credit
<!-- TC:TC-FIN-014:END -->

<!-- TC:TC-FIN-015:START traces=AC-FIN-015,REQ-FIN-015,API-FIN-019 -->
### TC-FIN-015 — every validation failure returned together, nothing posts
Derived from : AC-FIN-015 (REQ-FIN-015) · Exercises: API-FIN-019 POST /api/v1/fin/journal-entries
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an entry that is both unbalanced AND targets a non-leaf account
Steps        : 1. submit it
Expected     : both FIN-409-UNBALANCED and FIN-409-NOT-POSTABLE-ACCOUNT returned together; nothing posted
Test data    : unbalanced entry + rollup-account line
<!-- TC:TC-FIN-015:END -->

<!-- TC:TC-FIN-016:START traces=AC-FIN-016,REQ-FIN-016,API-FIN-022 -->
### TC-FIN-016 — a posted entry can never be hard-deleted (§12.13 immutable audit trail)
Derived from : AC-FIN-016 (REQ-FIN-016) · Exercises: (no DELETE route exists on /journal-entries/{id})
Scenario     : VIOLATION · data class ATTACK · language ALL
Preconditions: a POSTED entry
Steps        : 1. attempt any delete operation against it (direct call, since no UI/API path offers one)
Expected     : 404/405 (no route) — the record is retrievable unchanged via API-FIN-022 afterward
Test data    : any posted entry
<!-- TC:TC-FIN-016:END -->

<!-- TC:TC-FIN-022:START traces=AC-FIN-022,REQ-FIN-022,API-FIN-013 -->
### TC-FIN-022 — create a recurring/reversing template
Derived from : AC-FIN-022 (REQ-FIN-022) · Exercises: API-FIN-013 POST /api/v1/fin/recurring-templates
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a schedule type, frequency (if recurring), ≥2 balanced lines
Steps        : 1. POST the template
Expected     : 201; active RecurringTemplate created
Test data    : scheduleTypeCode RECURRING, frequencyCode MONTHLY
<!-- TC:TC-FIN-022:END -->

<!-- TC:TC-FIN-023:START traces=AC-FIN-023,REQ-FIN-023,API-FIN-014 -->
### TC-FIN-023 — run a recurring template on schedule
Derived from : AC-FIN-023 (REQ-FIN-023) · Exercises: API-FIN-014 POST /api/v1/fin/recurring-templates/{id}/run
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: an active template with nextRunDate = today
Steps        : 1. run it
Expected     : one entry built and posted from the template; nextRunDate advances per frequency
Test data    : monthly template due today
<!-- TC:TC-FIN-023:END -->

<!-- TC:TC-FIN-024:START traces=AC-FIN-024,REQ-FIN-024,API-FIN-014 -->
### TC-FIN-024 — automatic reversal in the next period for a reversing template
Derived from : AC-FIN-024 (REQ-FIN-024) · Exercises: API-FIN-014 POST /api/v1/fin/recurring-templates/{id}/run
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a REVERSING-type template posts in period N
Steps        : 1. run the template — 2. observe period N+1
Expected     : a linked reversal posts in period N+1 automatically; the period-N entry
  stays POSTED (classic reversal), so the accrual and its reversal net to zero across N/N+1
Test data    : reversing template, accrual scenario
<!-- TC:TC-FIN-024:END -->

<!-- TC:TC-FIN-025:START traces=AC-FIN-025,REQ-FIN-025,API-FIN-016 -->
### TC-FIN-025 — create an allocation rule
Derived from : AC-FIN-025 (REQ-FIN-025) · Exercises: API-FIN-016 POST /api/v1/fin/allocation-rules
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a source account, ≥1 target with a distribution type
Steps        : 1. POST the rule
Expected     : 201; active AllocationRule created
Test data    : 1 source account, 2 targets (60%/remainder)
<!-- TC:TC-FIN-025:END -->

<!-- TC:TC-FIN-026:START traces=AC-FIN-026,REQ-FIN-026,API-FIN-017 -->
### TC-FIN-026 — run an allocation rule with exact remainder distribution
Derived from : AC-FIN-026 (REQ-FIN-026) · Exercises: API-FIN-017 POST /api/v1/fin/allocation-rules/{id}/run
Scenario     : HAPPY · data class BOUNDARY · language ALL
Preconditions: 2 PERCENTAGE targets + 1 REMAINDER target; source balance 1,000.00
Steps        : 1. run the rule
Expected     : one posted entry whose target lines sum exactly to 1,000.00, remainder target absorbing rounding
Test data    : source balance 1,000.00, 40%/40%/remainder
<!-- TC:TC-FIN-026:END -->

<!-- TC:TC-FIN-027:START traces=AC-FIN-027,REQ-FIN-027,API-FIN-018 -->
### TC-FIN-027 — search journal entries without altering them
Derived from : AC-FIN-027 (REQ-FIN-027) · Exercises: API-FIN-018 POST /api/v1/fin/journal-entries/search
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: entries across several statuses/periods
Steps        : 1. POST /search with `JournalEntrySearchRequest` filtering period + status
Expected     : 200; exactly the matching entries, unmodified
Test data    : filter statusCode=POSTED
<!-- TC:TC-FIN-027:END -->

<!-- TC:TC-FIN-031:START traces=AC-FIN-031,REQ-FIN-031,API-FIN-023 -->
### TC-FIN-031 — create a fiscal year with its periods
Derived from : AC-FIN-031 (REQ-FIN-031) · Exercises: API-FIN-023 POST /api/v1/fin/fiscal-years
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: unique year code
Steps        : 1. POST {code, startDate, endDate, periodCount: 12}
Expected     : 201; year + 12 Open periods created
Test data    : code "2027", 12 monthly periods
<!-- TC:TC-FIN-031:END -->

<!-- TC:TC-FIN-032:START traces=AC-FIN-032,REQ-FIN-032,API-FIN-024 -->
### TC-FIN-032 — open (reopen) a soft-closed period
Derived from : AC-FIN-032 (REQ-FIN-032) · Exercises: API-FIN-024 PATCH /api/v1/fin/fiscal-periods/{id}/open
Scenario     : STATE · data class VALID · language ALL
Preconditions: a Soft Closed period
Steps        : 1. PATCH to open
Expected     : 200; statusCode=OPEN
Test data    : a soft-closed period
<!-- TC:TC-FIN-032:END -->

<!-- TC:TC-FIN-033:START traces=AC-FIN-033,REQ-FIN-033,API-FIN-025 -->
### TC-FIN-033 — soft-close an open period
Derived from : AC-FIN-033 (REQ-FIN-033) · Exercises: API-FIN-025 PATCH /api/v1/fin/fiscal-periods/{id}/soft-close
Scenario     : STATE · data class VALID · language ALL
Preconditions: an Open period
Steps        : 1. PATCH to soft-close
Expected     : 200; statusCode=SOFT_CLOSE; normal postings thereafter rejected (TC-FIN-020-style)
Test data    : an open period
<!-- TC:TC-FIN-033:END -->

<!-- TC:TC-FIN-034:START traces=AC-FIN-034,REQ-FIN-034,API-FIN-026 -->
### TC-FIN-034 — hard-close a soft-closed period (approval)
Derived from : AC-FIN-034 (REQ-FIN-034) · Exercises: API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close
Scenario     : STATE · data class VALID · language ALL
Preconditions: a Soft Closed period; approver holds PERM_FIN_PERIODS_CLOSE_APPROVE (distinct role)
Steps        : 1. PATCH to hard-close
Expected     : 200; statusCode=HARD_CLOSE; closedBy/closedAt set; permanently not reopenable
Test data    : soft-closed period, valid approver
<!-- TC:TC-FIN-034:END -->

<!-- TC:TC-FIN-036:START traces=AC-FIN-036,REQ-FIN-036,API-FIN-027 -->
### TC-FIN-036 — run year-end close (§12.10 opening-balance continuity)
Derived from : AC-FIN-036 (REQ-FIN-036) · Exercises: API-FIN-027 POST /api/v1/fin/fiscal-years/{id}/year-end-close
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: every period of the year Hard Closed
Steps        : 1. run year-end close
Expected     : a balanced closing entry (result accounts → Retained Earnings) and a
  balanced opening entry for next year, generated from the resulting balances
Test data    : fully hard-closed fiscal year with posted activity
<!-- TC:TC-FIN-036:END -->

<!-- TC:TC-FIN-037:START traces=AC-FIN-037,REQ-FIN-037,API-FIN-026 -->
### TC-FIN-037 — period-close approval recorded distinctly from entry creation
Derived from : AC-FIN-037 (REQ-FIN-037) · Exercises: API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a period ready to hard-close, entries in it created by a different principal
Steps        : 1. hard-close it
Expected     : closedBy/closedAt = the approver's principal/moment, distinct from any entry's createdBy in that period
Test data    : entries created by user A; close approved by user B
<!-- TC:TC-FIN-037:END -->

<!-- TC:TC-FIN-039:START traces=AC-FIN-039,REQ-FIN-039,API-FIN-028 -->
### TC-FIN-039 — account ledger derived live (§12.9 balances recomputed, never stored)
Derived from : AC-FIN-039 (REQ-FIN-039) · Exercises: API-FIN-028 GET /api/v1/fin/reports/account-ledger
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: posted lines across 2 periods for one account
Steps        : 1. GET the ledger for the account/range — 2. post one more entry — 3. GET again
Expected     : running balance correct both times; step 3 reflects the new posting immediately (no caching)
Test data    : one account, postings across 2 periods
<!-- TC:TC-FIN-039:END -->

<!-- TC:TC-FIN-040:START traces=AC-FIN-040,REQ-FIN-040,API-FIN-029 -->
### TC-FIN-040 — trial balance always balances (§12.8)
Derived from : AC-FIN-040 (REQ-FIN-040) · Exercises: API-FIN-029 GET /api/v1/fin/reports/trial-balance
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: any set of POSTED entries in a period
Steps        : 1. GET the trial balance
Expected     : total debit balances = total credit balances, exactly, by construction
Test data    : a period with ≥5 posted entries of mixed account types
<!-- TC:TC-FIN-040:END -->

<!-- TC:TC-FIN-041:START traces=AC-FIN-041,REQ-FIN-041,API-FIN-030 -->
### TC-FIN-041 — balance sheet continuity across year-end
Derived from : AC-FIN-041 (REQ-FIN-041) · Exercises: API-FIN-030 GET /api/v1/fin/reports/balance-sheet
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a completed year-end close (TC-FIN-036)
Steps        : 1. GET the new year's balance sheet at its very first moment
Expected     : opening balances equal the prior year's closing balances for every balance-sheet account
Test data    : post-year-end-close state
<!-- TC:TC-FIN-041:END -->

<!-- TC:TC-FIN-042:START traces=AC-FIN-042,REQ-FIN-042,API-FIN-031 -->
### TC-FIN-042 — income statement opens at zero each year
Derived from : AC-FIN-042 (REQ-FIN-042) · Exercises: API-FIN-031 GET /api/v1/fin/reports/income-statement
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a completed year-end close, no postings yet in the new year
Steps        : 1. GET the new year's income statement
Expected     : every revenue/expense account shows a zero balance
Test data    : post-year-end-close state, new year, zero activity
<!-- TC:TC-FIN-042:END -->

<!-- TC:TC-FIN-043:START traces=AC-FIN-043,REQ-FIN-043,API-FIN-032 -->
### TC-FIN-043 — dimension report never collapses distinct dimension values (§12.11)
Derived from : AC-FIN-043 (REQ-FIN-043) · Exercises: API-FIN-032 GET /api/v1/fin/reports/dimension
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: one account posted under 2 different PROJECT dimension values
Steps        : 1. GET the dimension report by PROJECT
Expected     : two separate rows for that account, one per project — never one combined row
Test data    : account posted under PROJECT=P1 and PROJECT=P2
<!-- TC:TC-FIN-043:END -->

<!-- TC:TC-FIN-044:START traces=AC-FIN-044,REQ-FIN-044,API-FIN-010 -->
### TC-FIN-044 — FIN registers itself into SEC at onboarding
Derived from : AC-FIN-044 (REQ-FIN-044) · Exercises: (onboarding call to SEC's API-SEC-018/019/020 — no dedicated FIN endpoint)
Scenario     : INTEGRATION · data class VALID · language ALL
Preconditions: SEC v1 gated and reachable; FIN deploying for the first time
Steps        : 1. run FIN's onboarding
Expected     : SEC records 1 ModuleRegistry row for FIN, 1 ScreenRegistry row per FIN screen (12), 1 ActionRegistry row per action
Test data    : fresh FIN deployment against a live SEC
<!-- TC:TC-FIN-044:END -->

<!-- TC:TC-FIN-045:START traces=AC-FIN-045,REQ-FIN-045,API-FIN-010 -->
### TC-FIN-045 — FIN registers its lookup types into MDL at onboarding
Derived from : AC-FIN-045 (REQ-FIN-045) · Exercises: (onboarding call to MDL's API-MDL-002/006 — no dedicated FIN endpoint)
Scenario     : INTEGRATION · data class VALID · language ALL
Preconditions: MDL v1 gated and reachable; FIN deploying for the first time
Steps        : 1. run FIN's onboarding
Expected     : MDL records 13 LookupType rows owned by FIN (module-registry-fin.md → LOOKUPS OWNED)
Test data    : fresh FIN deployment against a live MDL
<!-- TC:TC-FIN-045:END -->

<!-- TC:TC-FIN-046:START traces=AC-FIN-046,REQ-FIN-046,API-FIN-030 -->
### TC-FIN-046 — drill down from a statement line to its source event
Derived from : AC-FIN-046 (REQ-FIN-046) · Exercises: API-FIN-030 (entry point) → API-FIN-029 → API-FIN-028 → API-FIN-022
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: a balance-sheet line backed by a known event-sourced entry
Steps        : 1. drill from the balance-sheet line → trial balance row → account ledger → the originating entry → its eventReference
Expected     : each hop resolves to the correct downstream record; the terminal entry shows the original eventReference (or "manual"/"recurring"/"allocation" if not event-sourced)
Test data    : one event-sourced posted entry contributing to a known balance-sheet line
<!-- TC:TC-FIN-046:END -->

<!-- TC:TC-FIN-063:START traces=AC-FIN-001,REQ-FIN-001,API-FIN-001 -->
### TC-FIN-063 — search accounts
Derived from : AC-FIN-001 (REQ-FIN-001) · Exercises: API-FIN-001 POST /api/v1/fin/accounts/search
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: several accounts of mixed accountTypeCode and isActiveFl
Steps        : 1. POST `AccountSearchRequest` {code LIKE "10", accountTypeCode "ASSET", isActiveFl true, paging}
Expected     : 200 `Page<AccountResponse>` containing exactly the matching rows, nameAr/nameEn returned, nothing modified
Test data    : accounts 1000/1001 ASSET active, 4000 REVENUE active, 1002 ASSET inactive
<!-- TC:TC-FIN-063:END -->

<!-- TC:TC-FIN-064:START traces=AC-FIN-001,REQ-FIN-001,API-FIN-001 -->
### TC-FIN-064 — an unrecognized sort field is rejected on every search endpoint
Derived from : AC-FIN-001 (REQ-FIN-001) · Exercises: API-FIN-001 (representative of API-FIN-005, 008, 009, 012, 015, 018 — the same shared search builder)
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: none beyond an authenticated caller holding the screen's VIEW permission
Steps        : 1. POST a search whose paging sort names a field not on the request's allowed list
Expected     : 400 FIN-400-INVALID-SORT, ar "حقل الترتيب غير معروف" / en "Unrecognized sort field"; no page returned
Test data    : sort "dropTable"
<!-- TC:TC-FIN-064:END -->

<!-- TC:TC-FIN-065:START traces=AC-FIN-004,REQ-FIN-004,API-FIN-005 -->
### TC-FIN-065 — search dimensions
Derived from : AC-FIN-004 (REQ-FIN-004) · Exercises: API-FIN-005 POST /api/v1/fin/dimensions/search
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: ≥2 dimensions
Steps        : 1. POST `DimensionSearchRequest` {code LIKE "PRO", paging}
Expected     : 200 `Page<DimensionResponse>`, only the matching dimensions
Test data    : dimensions "PROJECT", "REGION"
<!-- TC:TC-FIN-065:END -->

<!-- TC:TC-FIN-066:START traces=AC-FIN-005,REQ-FIN-005,API-FIN-008 -->
### TC-FIN-066 — search dimension values, and reject an unknown parent dimension
Derived from : AC-FIN-005 (REQ-FIN-005) · Exercises: API-FIN-008 POST /api/v1/fin/dimensions/values/search
Scenario     : HAPPY + VIOLATION · data class VALID/INVALID · language ALL
Preconditions: dimension "PROJECT" with ≥2 values; dimensionId is carried in the body filters, never as a path variable
Steps        : 1. POST {dimensionId = PROJECT, code LIKE "P1", paging} — 2. POST the same with a dimensionId that does not exist
Expected     : 1. 200 `Page<DimensionValueResponse>`, only that dimension's matching values —
  2. 404 FIN-404-DIMENSION, ar "البُعد غير موجود" / en "Dimension not found"
Test data    : PROJECT values P100/P200; unknown dimensionId 999999
<!-- TC:TC-FIN-066:END -->

<!-- TC:TC-FIN-067:START traces=AC-FIN-007,REQ-FIN-007,API-FIN-009 -->
### TC-FIN-067 — search event-type rules
Derived from : AC-FIN-007 (REQ-FIN-007) · Exercises: API-FIN-009 POST /api/v1/fin/event-rules/search
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: ≥1 active and ≥1 inactive rule
Steps        : 1. POST {eventTypeCode EXACT, isActiveFl true, paging}
Expected     : 200 `Page<EventTypeRuleResponse>` with only the active rule for that event type
Test data    : eventTypeCode "INVOICE_PAID"
<!-- TC:TC-FIN-067:END -->

<!-- TC:TC-FIN-068:START traces=AC-FIN-022,REQ-FIN-022,API-FIN-012 -->
### TC-FIN-068 — search recurring templates
Derived from : AC-FIN-022 (REQ-FIN-022) · Exercises: API-FIN-012 POST /api/v1/fin/recurring-templates/search
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: one RECURRING and one REVERSING template
Steps        : 1. POST {scheduleTypeCode "REVERSING", isActiveFl true, paging}
Expected     : 200 `Page<RecurringTemplateResponse>` with only the reversing template
Test data    : two templates as above
<!-- TC:TC-FIN-068:END -->

<!-- TC:TC-FIN-069:START traces=AC-FIN-025,REQ-FIN-025,API-FIN-015 -->
### TC-FIN-069 — search allocation rules
Derived from : AC-FIN-025 (REQ-FIN-025) · Exercises: API-FIN-015 POST /api/v1/fin/allocation-rules/search
Scenario     : HAPPY · data class VALID · language ALL
Preconditions: ≥2 allocation rules with different source accounts
Steps        : 1. POST {sourceAccountId EXACT, isActiveFl true, paging}
Expected     : 200 `Page<AllocationRuleResponse>` with only that source account's rules
Test data    : two rules, one per source account
<!-- TC:TC-FIN-069:END -->

<!-- TC:TC-FIN-070:START traces=AC-FIN-001,REQ-FIN-001,API-FIN-002 -->
### TC-FIN-070 — reject a duplicate account code
Derived from : AC-FIN-001 (REQ-FIN-001) · Exercises: API-FIN-002 POST /api/v1/fin/accounts
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: account code "1000" already exists
Steps        : 1. POST a second account with code "1000"
Expected     : 409 FIN-409-ACCOUNT-DUP, ar "رمز الحساب مستخدم بالفعل" / en "Account code already in use"; no second row
Test data    : code "1000"
<!-- TC:TC-FIN-070:END -->

<!-- TC:TC-FIN-071:START traces=AC-FIN-002,REQ-FIN-002,REQ-FIN-003,API-FIN-003 -->
### TC-FIN-071 — unknown account id on update, deactivate and ledger
Derived from : AC-FIN-002 (REQ-FIN-002, REQ-FIN-003) · Exercises: API-FIN-003 PUT /api/v1/fin/accounts/{id}, API-FIN-004 PUT /{id}/deactivate, API-FIN-028 GET /reports/account-ledger
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an id that matches no account row
Steps        : 1. PUT the update — 2. PUT the deactivate — 3. GET the account ledger for it
Expected     : all three 404 FIN-404-ACCOUNT, ar "الحساب غير موجود" / en "Account not found"
Test data    : accountId 999999
<!-- TC:TC-FIN-071:END -->

<!-- TC:TC-FIN-072:START traces=AC-FIN-004,REQ-FIN-004,API-FIN-006 -->
### TC-FIN-072 — reject a duplicate dimension code
Derived from : AC-FIN-004 (REQ-FIN-004) · Exercises: API-FIN-006 POST /api/v1/fin/dimensions
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: dimension "PROJECT" exists
Steps        : 1. POST a second dimension with code "PROJECT"
Expected     : 409 FIN-409-DIMENSION-DUP, ar "رمز البُعد مستخدم بالفعل" / en "Dimension code already in use"; no second row
Test data    : code "PROJECT"
<!-- TC:TC-FIN-072:END -->

<!-- TC:TC-FIN-073:START traces=AC-FIN-007,REQ-FIN-007,API-FIN-010 -->
### TC-FIN-073 — reject a second active rule for the same event type
Derived from : AC-FIN-007 (REQ-FIN-007) · Exercises: API-FIN-010 POST /api/v1/fin/event-rules
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an active rule already exists for eventTypeCode "INVOICE_PAID" (whose value must
  first be added to MDL's ACCOUNTING_EVENT_TYPE — see TC-FIN-090)
Steps        : 1. POST another rule for the same event type
Expected     : 409 FIN-409-RULE-DUP, ar "يوجد بالفعل قاعدة نشطة لهذا النوع" / en "An active rule already exists for this event type"
Test data    : eventTypeCode "INVOICE_PAID"
<!-- TC:TC-FIN-073:END -->

<!-- TC:TC-FIN-074:START traces=AC-FIN-008,REQ-FIN-008,API-FIN-011 -->
### TC-FIN-074 — unknown event-type rule id when adding a line
Derived from : AC-FIN-008 (REQ-FIN-008) · Exercises: API-FIN-011 POST /api/v1/fin/event-rules/{id}/lines
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an id that matches no EventTypeRule row
Steps        : 1. POST a valid line payload under that id
Expected     : 404 FIN-404-RULE, ar "القاعدة غير موجودة" / en "Rule not found" — never FIN-404-TEMPLATE
Test data    : ruleId 999999
<!-- TC:TC-FIN-074:END -->

<!-- TC:TC-FIN-075:START traces=AC-FIN-022,REQ-FIN-022,API-FIN-013 -->
### TC-FIN-075 — a recurring template without a frequency is rejected
Derived from : AC-FIN-022 (REQ-FIN-022) · Exercises: API-FIN-013 POST /api/v1/fin/recurring-templates
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: none
Steps        : 1. POST a template with scheduleTypeCode=RECURRING and no frequencyCode —
  2. POST the same with scheduleTypeCode=REVERSING and no frequencyCode
Expected     : 1. 400 FIN-400-MISSING-FREQUENCY, ar "يلزم تحديد التكرار للقالب المتكرر" /
  en "A frequency is required for a recurring template" — 2. 201, a reversing template needs none
Test data    : the two payloads above
<!-- TC:TC-FIN-075:END -->

<!-- TC:TC-FIN-076:START traces=AC-FIN-023,REQ-FIN-023,API-FIN-014 -->
### TC-FIN-076 — unknown recurring template id on run
Derived from : AC-FIN-023 (REQ-FIN-023) · Exercises: API-FIN-014 POST /api/v1/fin/recurring-templates/{id}/run
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an id that matches no RecurringTemplate row
Steps        : 1. run it
Expected     : 404 FIN-404-TEMPLATE, ar "القالب المتكرر غير موجود" / en "Recurring template not found" —
  the entity named is the template, never FIN-404-RULE
Test data    : templateId 999999
<!-- TC:TC-FIN-076:END -->

<!-- TC:TC-FIN-077:START traces=AC-FIN-026,REQ-FIN-026,API-FIN-017 -->
### TC-FIN-077 — unknown allocation rule id on run
Derived from : AC-FIN-026 (REQ-FIN-026) · Exercises: API-FIN-017 POST /api/v1/fin/allocation-rules/{id}/run
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an id that matches no AllocationRule row
Steps        : 1. run it
Expected     : 404 FIN-404-ALLOCATION-RULE, ar "قاعدة التوزيع غير موجودة" / en "Allocation rule not found"
Test data    : allocationRuleId 999999
<!-- TC:TC-FIN-077:END -->

<!-- TC:TC-FIN-078:START traces=AC-FIN-010,REQ-FIN-010,API-FIN-020 -->
### TC-FIN-078 — MAPPING account derivation fails loudly
Derived from : AC-FIN-010 (REQ-FIN-010) · Exercises: API-FIN-020 POST /api/v1/fin/journal-entries/from-event
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an active rule holding one line with accountDerivationTypeCode=MAPPING (a stored
  configuration; db-script-fin.md declares no mapping store)
Steps        : 1. submit a triggering event of that type — 2. repeat with the line switched to
  CONSTANT, and again with DIRECT
Expected     : 1. 422 FIN-422-MAPPING-UNSUPPORTED, ar "يستخدم هذا السطر اشتقاق الحساب عبر جدول المطابقة وهو غير متاح حاليًا" /
  en "This rule line uses mapping-based account derivation, which is not available yet"; nothing
  posted and no account silently resolved — 2. both CONSTANT and DIRECT still build and post normally
Test data    : one MAPPING line, then the CONSTANT/DIRECT variants
<!-- TC:TC-FIN-078:END -->

<!-- TC:TC-FIN-079:START traces=AC-FIN-016,REQ-FIN-016,REQ-FIN-027,API-FIN-022 -->
### TC-FIN-079 — read one entry with its lines, and reject an unknown entry id
Derived from : AC-FIN-016 (REQ-FIN-016, REQ-FIN-027) · Exercises: API-FIN-022 GET /api/v1/fin/journal-entries/{id}
Scenario     : HAPPY + VIOLATION · data class VALID/INVALID · language ALL
Preconditions: a POSTED entry with 2 lines, one carrying a dimension tag
Steps        : 1. GET it — 2. GET an id that matches no entry — 3. GET the same unknown id on
  API-FIN-021 reverse
Expected     : 1. 200 `JournalEntryResponse` with docNo, statusCode=POSTED, nested lines and their
  dimensions — 2 and 3. 404 FIN-404-ENTRY, ar "القيد غير موجود" / en "Entry not found"
Test data    : a posted 2-line entry; entryId 999999
<!-- TC:TC-FIN-079:END -->

<!-- TC:TC-FIN-080:START traces=AC-FIN-031,REQ-FIN-031,API-FIN-023 -->
### TC-FIN-080 — reject a duplicate fiscal year code
Derived from : AC-FIN-031 (REQ-FIN-031) · Exercises: API-FIN-023 POST /api/v1/fin/fiscal-years
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: fiscal year code "2027" exists
Steps        : 1. POST a second year with code "2027"
Expected     : 409 FIN-409-YEAR-DUP, ar "رمز السنة المالية مستخدم بالفعل" / en "Fiscal year code already in use";
  no year and no periods generated
Test data    : code "2027"
<!-- TC:TC-FIN-080:END -->

<!-- TC:TC-FIN-081:START traces=AC-FIN-031,REQ-FIN-031,API-FIN-023 -->
### TC-FIN-081 — twelve periods are calendar months; any other count falls back to an even day split
Derived from : AC-FIN-031 (REQ-FIN-031) · Exercises: API-FIN-023 POST /api/v1/fin/fiscal-years
Scenario     : HAPPY · data class BOUNDARY · language ALL
Preconditions: none
Steps        : 1. POST {code "2027", startDate 2027-01-01, endDate 2027-12-31, periodCount 12} —
  2. POST {code "2028Q", startDate 2028-01-01, endDate 2028-12-31, periodCount 4} —
  3. POST a 12-period year whose span does not start on the first of a month
Expected     : 1. 201 with 12 OPEN periods, period N spanning the 1st to the LAST day of the Nth
  calendar month (period 2 = 2027-02-01..2027-02-28, not a 31-day block), nameAr/nameEn = that
  month's own Arabic/English name from the platform's locale data — 2 and 3. the even-day-split
  fallback: contiguous blocks, the first (totalDays mod periodCount) one day longer, the last
  period ending exactly on the year's endDate, named "الفترة N" / "Period N"; no day belongs to two periods
Test data    : the three payloads above
<!-- TC:TC-FIN-081:END -->

<!-- TC:TC-FIN-082:START traces=AC-FIN-032,REQ-FIN-032,API-FIN-024 -->
### TC-FIN-082 — unknown period id on every period transition
Derived from : AC-FIN-032 (REQ-FIN-032) · Exercises: API-FIN-024 open, API-FIN-025 soft-close, API-FIN-026 hard-close
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an id that matches no FiscalPeriod row
Steps        : 1. PATCH /{id}/open — 2. PATCH /{id}/soft-close — 3. PATCH /{id}/hard-close
Expected     : all three 404 FIN-404-PERIOD, ar "الفترة غير موجودة" / en "Period not found"
Test data    : periodId 999999
<!-- TC:TC-FIN-082:END -->

<!-- TC:TC-FIN-083:START traces=AC-FIN-033,REQ-FIN-033,API-FIN-025 -->
### TC-FIN-083 — reject soft-closing a period that is not Open
Derived from : AC-FIN-033 (REQ-FIN-033) · Exercises: API-FIN-025 PATCH /api/v1/fin/fiscal-periods/{id}/soft-close
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: a period already in SOFT_CLOSE, and another in HARD_CLOSE
Steps        : 1. soft-close the SOFT_CLOSE one — 2. soft-close the HARD_CLOSE one
Expected     : both 409 FIN-409-INVALID-TRANSITION, ar "لا يمكن تنفيذ هذا الانتقال من الحالة الحالية" /
  en "This transition is not allowed from the current status"; neither period's statusCode changes
Test data    : one soft-closed and one hard-closed period
<!-- TC:TC-FIN-083:END -->

<!-- TC:TC-FIN-084:START traces=AC-FIN-036,REQ-FIN-036,API-FIN-027 -->
### TC-FIN-084 — year-end close refused while any period is not Hard Closed
Derived from : AC-FIN-036 (REQ-FIN-036) · Exercises: API-FIN-027 POST /api/v1/fin/fiscal-years/{id}/year-end-close
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: TC-FIN-059's close-approver setup (otherwise the call stops at 403 first); an OPEN
  fiscal year with 11 HARD_CLOSE periods and 1 still SOFT_CLOSE
Steps        : 1. run year-end close
Expected     : 409 FIN-409-PERIODS-NOT-CLOSED, ar "يجب إغلاق كل الفترات إغلاقًا صارمًا أولًا" /
  en "Every period must be hard-closed first"; nothing posted, the year stays OPEN
Test data    : one period left soft-closed
<!-- TC:TC-FIN-084:END -->

<!-- TC:TC-FIN-085:START traces=AC-FIN-036,REQ-FIN-036,API-FIN-027 -->
### TC-FIN-085 — year-end close is not re-runnable, and needs a real year and successor
Derived from : AC-FIN-036 (REQ-FIN-036) · Exercises: API-FIN-027 POST /api/v1/fin/fiscal-years/{id}/year-end-close
Scenario     : STATE · data class EDGE · language ALL
Preconditions: TC-FIN-059's close-approver setup; a year already closed by TC-FIN-056
Steps        : 1. run year-end close again on that CLOSED year — 2. run it on an id matching no
  fiscal year — 3. run it on an eligible year for which NO year exists whose startDate =
  this year's endDate + 1 day
Expected     : 1. 409 FIN-409-INVALID-TRANSITION (the year's own status guard, never the misleading
  FIN-409-PERIODS-NOT-CLOSED the first run's YEAR_END_CLOSE transitions would otherwise raise) —
  2 and 3. 404 FIN-404-YEAR, ar "السنة المالية غير موجودة" / en "Fiscal year not found"; the
  successor is resolved by date adjacency only (a DERIVED decision — no successor column exists)
Test data    : a CLOSED year; yearId 999999; an eligible year with no adjacent successor
<!-- TC:TC-FIN-085:END -->

<!-- TC:TC-FIN-086:START traces=AC-FIN-036,REQ-FIN-036,API-FIN-027 -->
### TC-FIN-086 — Retained Earnings is resolved from the marker, and its absence is reported
Derived from : AC-FIN-036 (REQ-FIN-036) · Exercises: API-FIN-027 POST /api/v1/fin/fiscal-years/{id}/year-end-close
Scenario     : STATE · data class EDGE · language ALL
Preconditions: TC-FIN-059's close-approver setup; an eligible year with a successor. The marker
  FIN_ACCOUNT.is_retained_earnings_fl (DBF-FIN-147) is READ-ONLY on every endpoint — API-FIN-002/003
  cannot set it — so it must be set as data directly, and the partial unique index
  UQ_FIN_ACCOUNT_RETAINED_EARNINGS caps it at one marked account
Steps        : 1. run year-end close with NO account marked — 2. mark one EQUITY account and re-run —
  3. attempt to set a second account's marker directly in the database
Expected     : 1. 404 FIN-404-ACCOUNT (no invented code) and nothing posted — 2. 201; every
  result account's balance closes into the marked account, which is the only one credited/debited
  by the CLOSING entry, and the result accounts stand at zero afterwards — 3. the index rejects the
  second marked row, so the account can never be ambiguous
Test data    : EQUITY account "3900" marked as Retained Earnings
<!-- TC:TC-FIN-086:END -->

<!-- TC:TC-FIN-087:START traces=AC-FIN-014,REQ-FIN-014,API-FIN-019 -->
### TC-FIN-087 — docNo format, per-fiscal-year counter and immutability
Derived from : AC-FIN-014 (REQ-FIN-014) · Exercises: API-FIN-019 POST /api/v1/fin/journal-entries, read back through API-FIN-022
Scenario     : HAPPY · data class BOUNDARY · language ALL
Preconditions: two fiscal years, codes "2026" and "2027", both with OPEN periods
Steps        : 1. post the first entry of FY2026 — 2. post two more in FY2026, one of them through
  API-FIN-020 (event) and one through API-FIN-021 (a reversal) — 3. post the first entry of FY2027 —
  4. attempt to submit docNo in a create payload
Expected     : 1. docNo = "JV-2026-000001" — 2. "JV-2026-000002" and "JV-2026-000003": ONE counter
  per fiscal year across every journal type, never segmented by journalTypeCode — 3. "JV-2027-000001",
  the counter restarts per year — 4. the field is not part of any create/update request contract, so
  a submitted docNo is ignored and the generated value is returned; the value never changes on any
  later read
Test data    : years "2026"/"2027"
<!-- TC:TC-FIN-087:END -->

<!-- TC:TC-FIN-088:START traces=AC-FIN-014,REQ-FIN-014,API-FIN-019 -->
### TC-FIN-088 — concurrent creates in one fiscal year never collide on docNo
Derived from : AC-FIN-014 (REQ-FIN-014) · Exercises: API-FIN-019 POST /api/v1/fin/journal-entries
Scenario     : HAPPY · data class EDGE · language ALL
Preconditions: one fiscal year with an OPEN period; docNo allocation is serialized by a
  PESSIMISTIC_WRITE lock on the owning FIN_FISCAL_YEAR row, held to commit
Steps        : 1. issue N concurrent valid create requests against the same fiscal year (N ≥ 10)
Expected     : all N succeed with 201 and N DISTINCT sequential docNos; no request answers an
  unlocalized 409 DATA_INTEGRITY_VIOLATION from UQ_FIN_JOURNAL_ENTRY_YEAR_DOCNO, which stays an
  unreachable backstop
Test data    : 10 identical balanced 2-line payloads fired in parallel
<!-- TC:TC-FIN-088:END -->

<!-- TC:TC-FIN-089:START traces=AC-FIN-043,REQ-FIN-043,API-FIN-032 -->
### TC-FIN-089 — dimension report rejects an unknown dimension
Derived from : AC-FIN-043 (REQ-FIN-043) · Exercises: API-FIN-032 GET /api/v1/fin/reports/dimension
Scenario     : VIOLATION · data class INVALID · language ALL
Preconditions: an id that matches no Dimension row
Steps        : 1. GET the dimension report for it
Expected     : 404 FIN-404-DIMENSION, ar "البُعد غير موجود" / en "Dimension not found"; no rows returned
Test data    : dimensionId 999999
<!-- TC:TC-FIN-089:END -->

<!-- TC:TC-FIN-090:START traces=AC-FIN-045,REQ-FIN-045,API-FIN-010,XM-FIN-001 -->
### TC-FIN-090 — the lookup seed: 13 FIN-owned types, 36 values, two types deliberately empty
Derived from : AC-FIN-045 (REQ-FIN-045) · Exercises: API-FIN-010 POST /api/v1/fin/event-rules (the
  one create blocked by an empty type) and API-FIN-002 (the representative validated create)
Scenario     : INTEGRATION · data class EDGE · language ALL
Preconditions: a fresh database with the FIN lookup seed applied; FIN's module row exists in SEC
Steps        : 1. read MDL's lookup types owned by FIN — 2. POST an event-type rule with any
  eventTypeCode — 3. add one ACCOUNTING_EVENT_TYPE value through MDL's lookup administration and
  repeat step 2 — 4. POST an account with accountTypeCode "ASSET"
Expected     : 1. exactly 13 FIN-owned types and 36 active values; ACCOUNTING_EVENT_TYPE and
  PAYMENT_METHOD exist as TYPES with ZERO values on purpose (no artifact enumerates any), so they
  return an empty active-value list rather than MDL's type-not-found — 2. 400 FIN-400-INVALID-LOOKUP,
  because no active value exists yet: adding the host's own event types is a documented deployment
  step, not a defect — 3. 201 — 4. 201, every other lookup-backed create works out of the box
Test data    : the seeded 13 keys; one host-supplied ACCOUNTING_EVENT_TYPE value
<!-- TC:TC-FIN-090:END -->
<!-- SUB:API-SCENARIOS:END -->
<!-- PHASE:TEST-PLAN-BE:END -->

<!-- PHASE:INT-XM:START traces=REQ-FIN-001,REQ-FIN-037,REQ-FIN-038,XM-FIN-001,XM-FIN-002 -->
FIN declares two XM: XM-FIN-001 (SOFT-READ → MDL's lookup values) and XM-FIN-002 (READ → SEC's
user→permission directory, registered at ALIGN-BE for the RULE-FIN-015 fact). MDL and SEC are
both in the current selection, so both are real linking atoms.

<!-- TC:TC-FIN-047:START traces=XM-FIN-001,REQ-FIN-001,API-FIN-002 -->
### TC-FIN-047 — MDL lookup-type not-found is translated into a FIN error code
Derived from : XM-FIN-001 (REQ-FIN-001) · Exercises: API-FIN-002 POST /api/v1/fin/accounts (representative of every lookup-validating API)
Rule / code  : XM-FIN-001 (SOFT-READ) → FIN-400-INVALID-LOOKUP (a defined error, never a 500/unhandled state, never MDL's raw code)
Scenario     : INTEGRATION · data class EDGE · language ALL
Preconditions: MDL's `ACCOUNT_TYPE` LookupType is absent or deactivated, so the injected `MdlLookupApi.readActiveValuesByKey("ACCOUNT_TYPE")` throws `LocalizedException(NOT_FOUND, MDL_404_TYPE_KEY)`. (XM-FIN-001 is in-process Spring injection inside the single deployable — there is no network hop, so the former "MDL unreachable / times out" precondition is not reachable and has been replaced by the real in-process failure mode.)
Steps        : 1. POST a new account (accountTypeCode requires MDL validation) while that lookup type is unavailable
Expected     : the request fails with FIN-400-INVALID-LOOKUP, message ar "القيمة المُدخلة غير صالحة" / en "The submitted value is not valid" — never a raw crash, and MDL's `MDL_404_TYPE_KEY` never surfaces to the caller
Test data    : any account payload; MDL's ACCOUNT_TYPE lookup type deactivated (or the MdlLookupApi test double configured to throw MDL_404_TYPE_KEY)
<!-- TC:TC-FIN-047:END -->

<!-- TC:TC-FIN-091:START traces=XM-FIN-002,REQ-FIN-037,REQ-FIN-038,API-FIN-026 -->
### TC-FIN-091 — a failed SEC directory read refuses the close instead of approving it
Derived from : XM-FIN-002 (REQ-FIN-037, REQ-FIN-038) · Exercises: API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close (same translation on API-FIN-027)
Rule / code  : XM-FIN-002 (READ) → FIN-403-SOD-VIOLATION — never a 500, and never SEC's own code
Scenario     : INTEGRATION · data class EDGE · language ALL
Preconditions: the RULE-FIN-015 fact comes from SEC's
  `SecUserDirectoryApi.findUserIdsHoldingPermission`, called twice (for
  PERM_FIN_PERIODS_CLOSE_APPROVE and PERM_FIN_JOURNAL_ENTRIES_CREATE). XM-FIN-002 is in-process
  Spring injection inside the single deployable — there is no network hop, so the failure mode to
  drive is the injected directory throwing, not a timeout: configure the `SecUserDirectoryApi` test
  double to raise on the first call. Also set up TC-FIN-059's approver, so the call would otherwise
  succeed and the 403 can only come from the failed read
Steps        : 1. hard-close a SOFT_CLOSE period while the directory read fails
Expected     : 403 FIN-403-SOD-VIOLATION with FIN's own ar/en messages; the period stays SOFT_CLOSE.
  The fallback is never "continue without SEC" — an unverified separation-of-duties fact refuses
  the close rather than approving it
Test data    : `SecUserDirectoryApi` double throwing on findUserIdsHoldingPermission; a soft-closed period
<!-- TC:TC-FIN-091:END -->
<!-- PHASE:INT-XM:END -->

## TC TRACEABILITY INDEX
| AC | TC | REQ | API | RULE/code | XM |
|---|---|---|---|---|---|
| AC-FIN-001…046 | TC-FIN-001…046 (1:1) | REQ-FIN-001…046 (1:1) | see each TC's Exercises line | see each TC's Rule/code line | — |
| — | TC-FIN-047 | REQ-FIN-001 | API-FIN-002 (representative) | — | XM-FIN-001 |
| AC-FIN-030 | TC-FIN-048 | REQ-FIN-030 | API-FIN-021 | RULE-FIN-013 / FIN-409-ALREADY-REVERSED | — |
| AC-FIN-014 | TC-FIN-049, 050, 051 | REQ-FIN-014, REQ-FIN-017 | API-FIN-019 | RULE-FIN-017 / FIN-400-PERIOD-NOT-IN-YEAR, FIN-400-DOCDATE-OUTSIDE-PERIOD | — |
| AC-FIN-009 | TC-FIN-052 | REQ-FIN-009 | API-FIN-011 | RULE-FIN-003 / FIN-422-REMAINDER-MARKER | — |
| AC-FIN-025 | TC-FIN-053, 069 | REQ-FIN-025 | API-FIN-015, API-FIN-016 | RULE-FIN-003 / FIN-409-REMAINDER-COUNT, FIN-422-REMAINDER-MARKER | — |
| AC-FIN-012 | TC-FIN-054, 055 | REQ-FIN-012 | API-FIN-020 | RULE-FIN-010 / FIN-422-REMAINDER-NOT-POSITIVE | — |
| AC-FIN-036 | TC-FIN-056, 084, 085, 086 | REQ-FIN-036 | API-FIN-027 | RULE-FIN-008 year-end exemption / FIN-409-PERIODS-NOT-CLOSED, FIN-409-INVALID-TRANSITION, FIN-404-YEAR, FIN-404-ACCOUNT | — |
| AC-FIN-002 | TC-FIN-057, 058, 071 | REQ-FIN-002, REQ-FIN-003 | API-FIN-002, 003, 004, 028 | RULE-FIN-001 / FIN-409-PARENT-NOT-LEAF-ELIGIBLE, FIN-404-ACCOUNT | — |
| AC-FIN-034 | TC-FIN-059 | REQ-FIN-034, REQ-FIN-038 | API-FIN-026 | RULE-FIN-015 (satisfied path) | XM-FIN-002 |
| AC-FIN-038 | TC-FIN-060, 061, 062 | REQ-FIN-038 | API-FIN-026, 027 | RULE-FIN-015 / FIN-403-SOD-VIOLATION; gateway / platform ACCESS_DENIED (catalog FIN-403-FORBIDDEN) | XM-FIN-002 |
| AC-FIN-001 | TC-FIN-063, 064, 070 | REQ-FIN-001 | API-FIN-001, 002 | FIN-400-INVALID-SORT, FIN-409-ACCOUNT-DUP | — |
| AC-FIN-004 | TC-FIN-065, 072 | REQ-FIN-004 | API-FIN-005, 006 | FIN-409-DIMENSION-DUP | — |
| AC-FIN-005 | TC-FIN-066 | REQ-FIN-005 | API-FIN-008 | FIN-404-DIMENSION | — |
| AC-FIN-007 | TC-FIN-067, 073 | REQ-FIN-007 | API-FIN-009, 010 | FIN-409-RULE-DUP | — |
| AC-FIN-022 | TC-FIN-068, 075 | REQ-FIN-022 | API-FIN-012, 013 | FIN-400-MISSING-FREQUENCY | — |
| AC-FIN-008 | TC-FIN-074 | REQ-FIN-008 | API-FIN-011 | FIN-404-RULE | — |
| AC-FIN-023 | TC-FIN-076 | REQ-FIN-023 | API-FIN-014 | FIN-404-TEMPLATE | — |
| AC-FIN-026 | TC-FIN-077 | REQ-FIN-026 | API-FIN-017 | FIN-404-ALLOCATION-RULE | — |
| AC-FIN-010 | TC-FIN-078 | REQ-FIN-010 | API-FIN-020 | FIN-422-MAPPING-UNSUPPORTED | — |
| AC-FIN-016 | TC-FIN-079 | REQ-FIN-016, REQ-FIN-027 | API-FIN-021, 022 | FIN-404-ENTRY | — |
| AC-FIN-031 | TC-FIN-080, 081 | REQ-FIN-031 | API-FIN-023 | FIN-409-YEAR-DUP; calendar-month period generation | — |
| AC-FIN-032 | TC-FIN-082 | REQ-FIN-032 | API-FIN-024, 025, 026 | FIN-404-PERIOD | — |
| AC-FIN-033 | TC-FIN-083 | REQ-FIN-033 | API-FIN-025 | FIN-409-INVALID-TRANSITION | — |
| AC-FIN-014 | TC-FIN-087, 088 | REQ-FIN-014 | API-FIN-019, 022 | docNo `JV-{fiscalYearCode}-{NNNNNN}` (business code, no error row) | — |
| AC-FIN-043 | TC-FIN-089 | REQ-FIN-043 | API-FIN-032 | FIN-404-DIMENSION | — |
| AC-FIN-045 | TC-FIN-090 | REQ-FIN-045 | API-FIN-010, 002 | FIN-400-INVALID-LOOKUP | XM-FIN-001 |
| — | TC-FIN-091 | REQ-FIN-037, REQ-FIN-038 | API-FIN-026 | FIN-403-SOD-VIOLATION (failure translation) | XM-FIN-002 |

## COVERAGE
AC covered 46/46 (0 gaps) · REQ covered 46/46 · API covered 32/32, each now with a happy path
AND its principal failure — except API-FIN-029/030/031 (trial balance, balance sheet, income
statement), whose SVC-API-SEARCH Errors line names only FIN-500: they declare no reachable
failure of their own, so none was invented · every selected-module
(AC-FIN-030 carries two TCs — TC-FIN-030 for the non-POSTED half of RULE-FIN-013 and
TC-FIN-048 for its double-reversal half; the 1:1 row above is otherwise unchanged.)
XM covered 2/2 (XM-FIN-001 → TC-FIN-047 and TC-FIN-090; XM-FIN-002 → TC-FIN-091, including the
failure translation into FIN-403-SOD-VIOLATION).
RULE covered 17/17, each with both paths, except where the rule has no violated path by
construction: RULE-FIN-011 and RULE-FIN-012 are success-path only (TC-FIN-028/029) and
RULE-FIN-016 is enforced by omission — there is no route to violate, which TC-FIN-016/017
assert. RULE-FIN-017 is covered by TC-FIN-049/050 (violated) and TC-FIN-051 (satisfied, at both
inclusive period bounds); RULE-FIN-008's year-end exemption by TC-FIN-056; RULE-FIN-010's
per-side computation by TC-FIN-054 and its non-positive residue by TC-FIN-055; RULE-FIN-015 by
TC-FIN-059 (satisfied), TC-FIN-038/060/061 (violated: creator-only, nobody holding
close-approval, one user holding both).
ERROR CODES: all 36 registered `FinErrorCodes` constants are reachable by at least one scenario.
Three catalog rows are deliberately NOT given a scenario and are not constants: FIN-503 is struck
(XM-FIN-001 is in-process injection, so no 503 producer exists), FIN-500 is the infrastructure
fallthrough with no sanctioned way to provoke it from the API surface, and FIN-403-FORBIDDEN never
reaches the wire as a FIN code — TC-FIN-062 asserts the platform `ACCESS_DENIED` body instead,
which is the open ERROR ENVELOPE finding, not a FIN scenario gap. All 14 of the plan's §12 must-honor
points are individually exercised: 1→TC-018, 2→TC-040 (sign presentation, checked
structurally by the report), 3→TC-019, 4→TC-020, 5→TC-018/all amount fields (CHK
constraint, exercised implicitly by every posting TC), 6→TC-012/026, 7→TC-028, 8→TC-040,
9→TC-039/040/041/042/043 (all live-derived), 10→TC-036/041, 11→TC-021/043, 12→TC-011,
13→TC-016, 14→(no TC — a design-time constraint verified by code review, not a runtime scenario).
══════════════════════════════════════════════════════════════════
