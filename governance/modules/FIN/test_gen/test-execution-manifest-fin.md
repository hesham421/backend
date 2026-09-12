## TEST EXECUTION MANIFEST — FIN v1
══════════════════════════════════════════════════════════════════
Derived from: backend-test-plan-fin.md v1 (this run) · db-script-fin.md v1 (FK/XM) ·
srs-fin.md v1 (RULE messages). Extended with TC-FIN-049..091 after ALIGN-BE; no existing TC id
was renumbered. Endpoints API-FIN-026 and API-FIN-027 are KNOWN-BLOCKED on a fresh deployment —
see TC-FIN-059's precondition block (FIN_CLOSE_APPROVER assigned to a non-creator) and TC-FIN-086
(an account marked `is_retained_earnings_fl`, settable only as data).
══════════════════════════════════════════════════════════════════

## DEPENDENCY ORDER (topological entity build order)
1. Dimension (no FK)
2. Account (self-FK parent only)
3. FiscalYear (no FK)
4. EventTypeRule (no FK)
5. RecurringTemplate (no FK)
6. DimensionValue (FK → Dimension)
7. AllocationRule (FK → Account)
8. FiscalPeriod (FK → FiscalYear)
9. RuleLine (FK → EventTypeRule)
10. JournalEntry (FK → FiscalYear, FiscalPeriod, self)
11. RecurringTemplateLine (FK → RecurringTemplate, Account, DimensionValue)
12. AllocationTarget (FK → AllocationRule, Account, DimensionValue)
13. JournalLine (FK → JournalEntry, Account)
14. JournalLineDimension (FK → JournalLine, Dimension, DimensionValue)

## RULE → CODE → TC
| RULE | Catalog code | TC | HTTP | API |
|---|---|---|---|---|
| RULE-FIN-001 | FIN-409-HAS-CHILDREN / FIN-409-PARENT-NOT-LEAF-ELIGIBLE | TC-FIN-002, TC-FIN-058 (violated) · TC-FIN-057 (satisfied) | 409 / 200 | API-FIN-002, API-FIN-003 |
| RULE-FIN-002 | FIN-409-DIMVALUE-DUP | TC-FIN-006 | 409 | API-FIN-007 |
| RULE-FIN-003 | FIN-409-REMAINDER-COUNT | TC-FIN-009, TC-FIN-053 | 409 | API-FIN-011, API-FIN-016 |
| RULE-FIN-003 | FIN-422-REMAINDER-MARKER | TC-FIN-052, TC-FIN-053 | 422 | API-FIN-011, API-FIN-016, API-FIN-017, API-FIN-020 |
| RULE-FIN-004 | FIN-409-DUPLICATE-EVENT | TC-FIN-011 | 409 | API-FIN-020 |
| RULE-FIN-005 | FIN-404-NO-ACTIVE-RULE | TC-FIN-013 | 404 | API-FIN-020 |
| RULE-FIN-006 | FIN-409-UNBALANCED | TC-FIN-018 | 409 | API-FIN-019, 020, 014, 017 |
| RULE-FIN-007 | FIN-409-NOT-POSTABLE-ACCOUNT | TC-FIN-019 | 409 | API-FIN-019, 020, 014, 017 |
| RULE-FIN-008 | FIN-409-PERIOD-NOT-OPEN | TC-FIN-020 | 409 | API-FIN-019, 020, 014, 017 |
| RULE-FIN-008 | — (year-end CLOSING/OPENING exemption) | TC-FIN-056 | 201 | API-FIN-027 |
| RULE-FIN-009 | FIN-409-INVALID-DIMENSION | TC-FIN-021 | 409 | API-FIN-019, 020, 014, 017 |
| RULE-FIN-010 | — (success-path computation, per side) | TC-FIN-012, TC-FIN-026, TC-FIN-054 | 200/201 | API-FIN-020, 014, 017 |
| RULE-FIN-010 | FIN-422-REMAINDER-NOT-POSITIVE | TC-FIN-055 | 422 | API-FIN-017, API-FIN-020 |
| RULE-FIN-011 | — (success-path) | TC-FIN-028 | 201 | API-FIN-021 |
| RULE-FIN-012 | — (success-path) | TC-FIN-029 | 201 | API-FIN-021 |
| RULE-FIN-013 | FIN-409-NOT-POSTED | TC-FIN-030 | 409 | API-FIN-021 |
| RULE-FIN-013 | FIN-409-ALREADY-REVERSED | TC-FIN-048 | 409 | API-FIN-021 |
| RULE-FIN-014 | FIN-409-NOT-REOPENABLE | TC-FIN-035 | 409 | API-FIN-024 |
| RULE-FIN-015 | FIN-403-SOD-VIOLATION | TC-FIN-038 (creator only), TC-FIN-060 (nobody holds close-approval), TC-FIN-061 (one user holds both), TC-FIN-091 (SEC directory read fails) | 403 | API-FIN-026, API-FIN-027 |
| RULE-FIN-015 | — (satisfied path, FIN_CLOSE_APPROVER) | TC-FIN-059 | 200 | API-FIN-026 |
| (platform gateway) | — rendered as the platform `ACCESS_DENIED` body (catalog FIN-403-FORBIDDEN) | TC-FIN-062 | 403 | API-FIN-026 |
| RULE-FIN-016 | — (enforced by omission, no route) | TC-FIN-016, TC-FIN-017 | 404/405 | API-FIN-022 |
| RULE-FIN-017 | FIN-400-PERIOD-NOT-IN-YEAR | TC-FIN-049 | 400 | API-FIN-019 |
| RULE-FIN-017 | FIN-400-DOCDATE-OUTSIDE-PERIOD | TC-FIN-050 | 400 | API-FIN-019 |
| RULE-FIN-017 | — (satisfied, inclusive period bounds) | TC-FIN-051 | 201 | API-FIN-019 |

## ENTITY CRUD CHECKLIST
| ENT | create | read | search | update | deactivate (soft) | activate |
|---|---|---|---|---|---|---|
| ENT-FIN-001 Account | ✓ (API-FIN-002) | — | ✓ (API-FIN-001) | ✓ (API-FIN-003) | ✓ (API-FIN-004) | — |
| ENT-FIN-002 Dimension | ✓ (API-FIN-006) | — | ✓ (API-FIN-005) | — | — | — |
| ENT-FIN-003 DimensionValue | ✓ (API-FIN-007) | — | ✓ (API-FIN-008) | — | — | — |
| ENT-FIN-004 JournalEntry | ✓ (API-FIN-019, 020) | ✓ (API-FIN-022) | ✓ (API-FIN-018) | — (locked, RULE-FIN-016) | — (no VOID path — classic reversal leaves the original POSTED, API-FIN-021) | — |
| ENT-FIN-005 JournalLine | ✓ (with header) | ✓ (with header) | — | — | — | — |
| ENT-FIN-006 JournalLineDimension | ✓ (with line) | ✓ (with line) | — | — | — | — |
| ENT-FIN-007 FiscalYear | ✓ (API-FIN-023) | — | — | — | ✓ (statusCode=CLOSED, API-FIN-027) | — |
| ENT-FIN-008 FiscalPeriod | ✓ (with year) | — | — | ✓ (API-FIN-024/025/026, transitions) | — | ✓ (API-FIN-024, reopen) |
| ENT-FIN-009 EventTypeRule | ✓ (API-FIN-010) | — | ✓ (API-FIN-009) | — | — | — |
| ENT-FIN-010 RuleLine | ✓ (API-FIN-011) | — | — | — | — | — |
| ENT-FIN-011 RecurringTemplate | ✓ (API-FIN-013) | — | ✓ (API-FIN-012) | — | — | — |
| ENT-FIN-012 RecurringTemplateLine | ✓ (with template) | — | — | — | — | — |
| ENT-FIN-013 AllocationRule | ✓ (API-FIN-016) | — | ✓ (API-FIN-015) | — | — | — |
| ENT-FIN-014 AllocationTarget | ✓ (with rule) | — | — | — | — | — |

## CROSS-MODULE (this module's declared XM)
| XM | Target | TC | Status |
|---|---|---|---|
| XM-FIN-001 | MDL | TC-FIN-047, TC-FIN-090 | ACTIVE — covered |
| XM-FIN-002 | SEC | TC-FIN-091 (failure translation), TC-FIN-059/060/061 (the fact it supplies) | ACTIVE — covered |
══════════════════════════════════════════════════════════════════
