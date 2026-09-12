<!-- source: content outside every PHASE block (leading / between / trailing sections) -->
# BACKEND TEST PLAN — الحسابات العامة / Finance (General Ledger) (FIN)
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v1   Profile : erp   Scope : project (modules FIN, MDL, SEC)
Sources: srs-fin.md v1 · backend-execution-plan-fin.md v1 · registry-srs-fin.md v1 · registry-db-fin.md v1
Framework: agnostic. REDUCED: no. Open ADRs: 0 new (ADR-FIN-001 unaffected).
TC count: 101 (module scope) · 2 (integration — XM-FIN-001, FIN declares → MDL; XM-FIN-002, FIN declares → SEC)
Extended after ALIGN-BE with TC-FIN-049..091, then with TC-FIN-092..103 for the API-FIN-033/034/035
delivery, the report-404 change and RULE-FIN-009's previously uncovered wrong-dimension branch (no
existing TC id, marker or trace was changed; TC-FIN-062's Expected was rewritten in place for the
now-localized ACCESS_DENIED body).
══════════════════════════════════════════════════════════════════





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
| AC-FIN-013 | TC-FIN-092 | REQ-FIN-013, REQ-FIN-007 | API-FIN-034, API-FIN-020 | RULE-FIN-005 / FIN-404-NO-ACTIVE-RULE (reachable via deactivation) | — |
| AC-FIN-021 | TC-FIN-093 | REQ-FIN-021, REQ-FIN-005 | API-FIN-035, API-FIN-019 | RULE-FIN-009 / FIN-409-INVALID-DIMENSION (inactive-value branch) | — |
| AC-FIN-031 | TC-FIN-094, 095 | REQ-FIN-031 | API-FIN-033 | FIN-400-INVALID-SORT; platform ACCESS_DENIED | — |
| AC-FIN-007 | TC-FIN-096, 097 | REQ-FIN-007 | API-FIN-034, API-FIN-010 | FIN-404-RULE; FIN-409-RULE-DUP (deactivate does not free the event type) | — |
| AC-FIN-005 | TC-FIN-098 | REQ-FIN-005 | API-FIN-035 | FIN-404-DIMVALUE | — |
| AC-FIN-040 | TC-FIN-099 | REQ-FIN-040 | API-FIN-029 | FIN-404-PERIOD (only when periodId is supplied) | — |
| AC-FIN-041 | TC-FIN-100 | REQ-FIN-041 | API-FIN-030 | FIN-404-YEAR | — |
| AC-FIN-042 | TC-FIN-101 | REQ-FIN-042 | API-FIN-031 | FIN-404-YEAR, FIN-404-PERIOD | — |
| AC-FIN-036 | TC-FIN-102 | REQ-FIN-036 | API-FIN-027, API-FIN-019 | dormant close: empty line sets, RULE-FIN-006 reads 0 = 0 as balanced; VALIDATION_ERROR on `lines: []` | — |
| AC-FIN-021 | TC-FIN-103 | REQ-FIN-021 | API-FIN-019 | RULE-FIN-009 / FIN-409-INVALID-DIMENSION (wrong-dimension branch) | — |

## COVERAGE
AC covered 46/46 (0 gaps) · REQ covered 46/46 · API covered 35/35, each with a happy path AND its
principal failure. The three endpoints added since the previous count are API-FIN-033 (period
search — TC-FIN-094 happy, TC-FIN-095 FIN-400-INVALID-SORT and the 403), API-FIN-034 (deactivate
event-type rule — TC-FIN-096 happy and FIN-404-RULE, TC-FIN-097 the duplicate-code limitation) and
API-FIN-035 (deactivate dimension value — TC-FIN-098 happy and FIN-404-DIMVALUE).
SUPERSEDED, and stated plainly because this file previously claimed the opposite: API-FIN-029/030/031
no longer "declare no reachable failure of their own, so none was invented". The report service now
resolves the keying id first, so API-FIN-030 and API-FIN-031 answer FIN-404-YEAR on an unknown
REQUIRED fiscalYearId (TC-FIN-100, TC-FIN-101) instead of a silent 200 all-zero report, and
API-FIN-029 answers FIN-404-PERIOD on an unknown periodId ONLY when one is supplied — omitting it
stays a valid 200 across all periods (TC-FIN-099), so a scenario asserting 404 on an omitted
periodId would be wrong · every selected-module
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
close-approval, one user holding both). RULE-FIN-005's violated path is now covered twice, from two
different starting states: TC-FIN-013 from an event type that never had a rule, and TC-FIN-092 from
one whose rule was retired through API-FIN-034 — a transition nothing could stage through the API
before that endpoint existed. RULE-FIN-009 is now covered on BOTH of its
conditions, which it was not before: the inactive branch by TC-FIN-021 (stated) and TC-FIN-093
(arranged end to end through API-FIN-035, rather than by writing FIN_DIMENSION_VALUE.IS_ACTIVE_FL
as data), and the wrong-dimension branch by TC-FIN-103 — a real hole until now, since the guard is
a single `if` over both conditions and nothing exercised the second one. Both branches answer the
same FIN-409-INVALID-DIMENSION by design, so the pair is distinguished by its fixture, never by the
response.
ERROR CODES: all 37 registered `FinErrorCodes` constants are reachable by at least one scenario
(36 before this delivery; FIN-404-DIMVALUE is the one added, covered by TC-FIN-098 and never
conflated with the parent's FIN-404-DIMENSION). Both bundles carry exactly 37 `FIN-*` keys, matching
the constant count.
Three catalog rows are deliberately NOT given a scenario and are not constants: FIN-503 is struck
(XM-FIN-001 is in-process injection, so no 503 producer exists), FIN-500 is the infrastructure
fallthrough with no sanctioned way to provoke it from the API surface, and FIN-403-FORBIDDEN never
reaches the wire as a FIN code — TC-FIN-062 asserts the platform `ACCESS_DENIED` body instead,
which is deliberate platform behaviour, not a FIN scenario gap. The localization half of the old
ERROR ENVELOPE finding is CLOSED: the shared handler now resolves that body's message through
MessageSource and `ACCESS_DENIED` is registered in both bundles, so TC-FIN-062 expects an ar/en
message. The wire `code` is unchanged — still `ACCESS_DENIED`, never FIN-403-FORBIDDEN — and the
English text is byte-identical to before, so only Arabic callers see any change. All 14 of the plan's §12 must-honor
points are individually exercised: 1→TC-018, 2→TC-040 (sign presentation, checked
structurally by the report), 3→TC-019, 4→TC-020, 5→TC-018/all amount fields (CHK
constraint, exercised implicitly by every posting TC), 6→TC-012/026, 7→TC-028, 8→TC-040,
9→TC-039/040/041/042/043 (all live-derived), 10→TC-036/041, 11→TC-021/043, 12→TC-011,
13→TC-016, 14→(no TC — a design-time constraint verified by code review, not a runtime scenario).
══════════════════════════════════════════════════════════════════
