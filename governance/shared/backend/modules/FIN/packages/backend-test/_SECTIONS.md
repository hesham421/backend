<!-- source: content outside every PHASE block (leading / between / trailing sections) -->
# BACKEND TEST PLAN — الحسابات العامة / Finance (General Ledger) (FIN)
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v1   Profile : erp   Track : backend   Plan : test
Scope  : **module** — modules FIN (`gov.py run-standalone test-gen --module FIN`)
Sources: `_state/current-srs.md` (v1) · `_state/current-backend-execution-plan.md` (v1) ·
         `_state/current-frontend-execution-plan.md` (v1) · `_state/current-registry-srs.md` (v1) ·
         `_state/current-registry-db.md` (v1) · `_state/current-registry-exec-fe.md` (v1)
Framework : **agnostic** (`profile.stack.testing`). Every block below is the framework-agnostic
            TC form; no tool, annotation, fixture or file layout is named anywhere in this plan.
            The consumer repo chooses its framework and turns each TC into a test.
REDUCED   : no — the backend-execution-plan is present, so every step binds to a real `API-*`.
TC ids    : TC-FIN-001 … TC-FIN-052 — one continuous module sequence **shared** with
            `frontend-test-plan-fin.md`, which continues at TC-FIN-053. No id is reused and no
            id is reassigned to a different subject than the one it already carried.
Open ADRs : 1 raised by this run — ADR-FIN-009 (ACCEPTED, non-breaking). Applied and not
            re-derived: ADR-FIN-001 … ADR-FIN-008.
══════════════════════════════════════════════════════════════════

**Id order is AC order, not document order.** `TC-FIN-<seq>` maps 1:1 onto `AC-FIN-<seq>` for
all forty-six acceptance criteria; TC-FIN-047 … TC-FIN-052 are the six additional cases §3 rule 3
(a stated numeric limit) and §3's permission rule allow, each declared against the AC it extends.
The two SUBs group by scenario kind, so the ids are not monotonic inside a SUB — that is the
price of never renumbering, and the traceability index below is the address.

**Where the TCs come from, and where they deliberately stop.** Every TC below is derived from an
`AC-FIN-*`. FIN's artifacts also record behaviour that no `REQ-*`, `AC-*` or `RULE-*` states and
that was delivered on a recorded human decision or as an as-built narrowing:

- the `FIN-409-NOT-ACTIVE` run gate on API-FIN-014 / API-FIN-017 (backend-execution-plan, Error
  Catalog: "no RULE — recorded human decision, 2026-09-12");
- the four deactivate endpoints API-FIN-034 … API-FIN-037 as scenarios in their own right;
- the keying-id 404s on API-FIN-029 / API-FIN-030 / API-FIN-031 (`FIN-404-PERIOD`, `FIN-404-YEAR`);
- the dormant-year lineless CLOSING/OPENING entry (API-FIN-027, "reviewed and deliberately KEPT");
- the fiscal-period search API-FIN-033, which no `AC-*` asks for.

None of them gets a TC of its own here: fabricating one would invent an acceptance criterion this
engine does not own. They appear only where an AC's own Given needs them as setup — API-FIN-035
is how AC-FIN-021's inactive dimension value is reached at all, and API-FIN-033 is how a period id
is discovered. This boundary is recorded in **ADR-FIN-009**, not left unsaid; it is a
requirements-side gap if anyone wants those paths covered, not a test-generation one.



## TC TRACEABILITY INDEX

**AC → TC**

| AC | TC | AC | TC |
|---|---|---|---|
| AC-FIN-001 | TC-FIN-001 | AC-FIN-024 | TC-FIN-024 |
| AC-FIN-002 | TC-FIN-002 | AC-FIN-025 | TC-FIN-025 |
| AC-FIN-003 | TC-FIN-003 | AC-FIN-026 | TC-FIN-026 |
| AC-FIN-004 | TC-FIN-004 | AC-FIN-027 | TC-FIN-027, TC-FIN-050 |
| AC-FIN-005 | TC-FIN-005 | AC-FIN-028 | TC-FIN-028 |
| AC-FIN-006 | TC-FIN-006 | AC-FIN-029 | TC-FIN-029 |
| AC-FIN-007 | TC-FIN-007 | AC-FIN-030 | TC-FIN-030 |
| AC-FIN-008 | TC-FIN-008 | AC-FIN-031 | TC-FIN-031, TC-FIN-051 |
| AC-FIN-009 | TC-FIN-009, TC-FIN-047 | AC-FIN-032 | TC-FIN-032 |
| AC-FIN-010 | TC-FIN-010 | AC-FIN-033 | TC-FIN-033 |
| AC-FIN-011 | TC-FIN-011 | AC-FIN-034 | TC-FIN-034 |
| AC-FIN-012 | TC-FIN-012, TC-FIN-048 | AC-FIN-035 | TC-FIN-035 |
| AC-FIN-013 | TC-FIN-013 | AC-FIN-036 | TC-FIN-036 |
| AC-FIN-014 | TC-FIN-014, TC-FIN-049 | AC-FIN-037 | TC-FIN-037 |
| AC-FIN-015 | TC-FIN-015 | AC-FIN-038 | TC-FIN-038, TC-FIN-052 |
| AC-FIN-016 | TC-FIN-016 | AC-FIN-039 | TC-FIN-039 |
| AC-FIN-017 | TC-FIN-017 | AC-FIN-040 | TC-FIN-040 |
| AC-FIN-018 | TC-FIN-018 | AC-FIN-041 | TC-FIN-041 |
| AC-FIN-019 | TC-FIN-019 | AC-FIN-042 | TC-FIN-042 |
| AC-FIN-020 | TC-FIN-020 | AC-FIN-043 | TC-FIN-043 |
| AC-FIN-021 | TC-FIN-021 | AC-FIN-044 | TC-FIN-044 |
| AC-FIN-022 | TC-FIN-022 | AC-FIN-045 | TC-FIN-045 |
| AC-FIN-023 | TC-FIN-023 | AC-FIN-046 | TC-FIN-046 |

**REQ → TC** — each `REQ-FIN-<n>` carries the TC of its own `AC-FIN-<n>`, plus:

| REQ | TC |
|---|---|
| REQ-FIN-009 | TC-FIN-009, TC-FIN-047 |
| REQ-FIN-012 | TC-FIN-012, TC-FIN-048 |
| REQ-FIN-014 | TC-FIN-014, TC-FIN-049 |
| REQ-FIN-027 | TC-FIN-027, TC-FIN-050 |
| REQ-FIN-031 | TC-FIN-031, TC-FIN-051 |
| REQ-FIN-038 | TC-FIN-038, TC-FIN-052 |
| REQ-FIN-003 | TC-FIN-003 (and RULE-FIN-007's effect, asserted there) |
| REQ-FIN-017 | TC-FIN-017 (the transition) · TC-FIN-010, TC-FIN-014, TC-FIN-023, TC-FIN-026, TC-FIN-036 (the same unified path from every other source) |
| REQ-FIN-046 | TC-FIN-046 (the chain) · TC-FIN-039, TC-FIN-040, TC-FIN-041, TC-FIN-042 (each hop's own report) |

**API → TC**

| API | TC |
|---|---|
| API-FIN-001 | TC-FIN-001, TC-FIN-002 (the unchanged-state assertion) |
| API-FIN-002 | TC-FIN-001 |
| API-FIN-003 | TC-FIN-002 |
| API-FIN-004 | TC-FIN-003 |
| API-FIN-005 | — no TC: no `AC-*` states a dimension search (ADR-FIN-009) |
| API-FIN-006 | TC-FIN-004 |
| API-FIN-007 | TC-FIN-005, TC-FIN-006 |
| API-FIN-008 | TC-FIN-005, TC-FIN-006 (the one-row assertion) |
| API-FIN-009 | — no TC: no `AC-*` states a rule search (ADR-FIN-009) |
| API-FIN-010 | TC-FIN-007 |
| API-FIN-011 | TC-FIN-008, TC-FIN-009, TC-FIN-047 |
| API-FIN-012 | — no TC: no `AC-*` states a template search (ADR-FIN-009) |
| API-FIN-013 | TC-FIN-022 |
| API-FIN-014 | TC-FIN-023, TC-FIN-024 |
| API-FIN-015 | — no TC: no `AC-*` states an allocation-rule search (ADR-FIN-009) |
| API-FIN-016 | TC-FIN-025 |
| API-FIN-017 | TC-FIN-026 |
| API-FIN-018 | TC-FIN-027, TC-FIN-050, TC-FIN-011 (the one-entry assertion) |
| API-FIN-019 | TC-FIN-003, TC-FIN-014, TC-FIN-015, TC-FIN-017, TC-FIN-018, TC-FIN-019, TC-FIN-020, TC-FIN-021, TC-FIN-033, TC-FIN-049 |
| API-FIN-020 | TC-FIN-010, TC-FIN-011, TC-FIN-012, TC-FIN-013, TC-FIN-048 |
| API-FIN-021 | TC-FIN-016, TC-FIN-028, TC-FIN-029, TC-FIN-030 |
| API-FIN-022 | TC-FIN-016, TC-FIN-017, TC-FIN-028, TC-FIN-046 |
| API-FIN-023 | TC-FIN-031, TC-FIN-051 |
| API-FIN-024 | TC-FIN-032, TC-FIN-035 |
| API-FIN-025 | TC-FIN-020, TC-FIN-033 |
| API-FIN-026 | TC-FIN-020, TC-FIN-034, TC-FIN-037, TC-FIN-038 |
| API-FIN-027 | TC-FIN-036, TC-FIN-052 |
| API-FIN-028 | TC-FIN-039, TC-FIN-046 |
| API-FIN-029 | TC-FIN-040, TC-FIN-046 |
| API-FIN-030 | TC-FIN-041, TC-FIN-046 |
| API-FIN-031 | TC-FIN-042 |
| API-FIN-032 | TC-FIN-043 |
| API-FIN-033 | — no TC: no `AC-*` states a fiscal-period search (ADR-FIN-009); it is cited only as setup |
| API-FIN-034 | — no TC of its own; cited as TC-FIN-013's alternative precondition (ADR-FIN-009) |
| API-FIN-035 | — no TC of its own; cited as TC-FIN-021's only precondition (ADR-FIN-009) |
| API-FIN-036 | — no TC: no `AC-*` states a template deactivate (ADR-FIN-009) |
| API-FIN-037 | — no TC: no `AC-*` states an allocation-rule deactivate (ADR-FIN-009) |

**RULE / catalog code → TC**

| RULE | code | TC |
|---|---|---|
| RULE-FIN-001 | FIN-409-HAS-CHILDREN (409) | TC-FIN-002 |
| RULE-FIN-001 | FIN-409-PARENT-NOT-LEAF-ELIGIBLE (409) | — no TC: no `AC-*` states the create-path variant |
| RULE-FIN-002 | FIN-409-DIMVALUE-DUP (409) | TC-FIN-006 |
| RULE-FIN-003 | FIN-409-REMAINDER-COUNT (409) | TC-FIN-009, TC-FIN-047 |
| RULE-FIN-003 | FIN-422-REMAINDER-MARKER (422) | — no TC: no `AC-*` states marker disagreement |
| RULE-FIN-004 | FIN-409-DUPLICATE-EVENT (409) | TC-FIN-011 |
| RULE-FIN-005 | FIN-404-NO-ACTIVE-RULE (404) | TC-FIN-013 |
| RULE-FIN-006 | FIN-409-UNBALANCED (409) | TC-FIN-015, TC-FIN-018 · TC-FIN-040 (the report invariant) |
| RULE-FIN-007 | FIN-409-NOT-POSTABLE-ACCOUNT (409) | TC-FIN-003, TC-FIN-015, TC-FIN-019 |
| RULE-FIN-008 | FIN-409-PERIOD-NOT-OPEN (409) | TC-FIN-020, TC-FIN-033 · TC-FIN-036 (the carve-out, asserted as not raised) |
| RULE-FIN-009 | FIN-409-INVALID-DIMENSION (409) | TC-FIN-021 |
| RULE-FIN-010 | — (success path) | TC-FIN-012, TC-FIN-026 |
| RULE-FIN-010 | FIN-422-REMAINDER-NOT-POSITIVE (422) | TC-FIN-048 |
| RULE-FIN-011 | — (success path, no distinct code) | TC-FIN-028, TC-FIN-024 |
| RULE-FIN-012 | — (success path, no distinct code) | TC-FIN-029, TC-FIN-024 |
| RULE-FIN-013 | FIN-409-ALREADY-REVERSED (409) | TC-FIN-030 |
| RULE-FIN-013 | FIN-409-NOT-POSTED (409) | — no TC: AC-FIN-030's Given is the already-reversed entry, not a DRAFT, and DRAFT is never a stored browsable state (SRS A7) |
| RULE-FIN-014 | FIN-409-NOT-REOPENABLE (409) | TC-FIN-035 · TC-FIN-032, TC-FIN-034 (the states around it) |
| RULE-FIN-015 | FIN-403-FORBIDDEN (403) | TC-FIN-038, TC-FIN-052 |
| RULE-FIN-016 | — (enforced by omission, no code) | TC-FIN-016, TC-FIN-017 |
| RULE-FIN-017 | FIN-400-PERIOD-NOT-IN-YEAR, FIN-400-DOCDATE-OUTSIDE-PERIOD (400) | — no TC: no `AC-*` states the incoherent triple; RULE-FIN-017 was added at SVC-API and carries no AC of its own (ADR-FIN-009) |

**Catalog rows no TC exercises**, stated rather than left unsaid: FIN-409-ACCOUNT-DUP,
FIN-409-DIMENSION-DUP, FIN-409-RULE-DUP, FIN-409-YEAR-DUP, FIN-400-INVALID-LOOKUP,
FIN-400-MISSING-FREQUENCY, FIN-400-INVALID-SORT, FIN-422-MAPPING-UNSUPPORTED,
FIN-422-REMAINDER-MARKER, FIN-422-INVALID-PERCENTAGE-VALUE, FIN-409-NOT-ACTIVE,
FIN-409-INVALID-TRANSITION, FIN-409-PERIODS-NOT-CLOSED, FIN-400-PERIOD-NOT-IN-YEAR,
FIN-400-DOCDATE-OUTSIDE-PERIOD, the five `FIN-404-*` not-found rows raised outside an AC's own
path, and FIN-500. No `AC-FIN-*` states any of them, and this engine derives from the ACs alone;
fabricating a TC for a code no acceptance criterion asserts is the over-engineering §3 rule 6
forbids. They are a requirements-side gap if anyone wants them covered, not a test-generation one
— ADR-FIN-009 records the boundary. `FIN-403-SOD-VIOLATION` is not in that list and must not be
tested at all: it is struck as unreachable, and TC-FIN-038 says so explicitly.

## COVERAGE

```
AC  covered   46/46   ✓ 0 gaps   (every AC-FIN-001…046 carries ≥1 TC on this track)
REQ covered   46/46   ✓ 0 gaps
API covered   26/37   — 11 endpoints carry no TC, each named in the API → TC index with the
                        reason; none is an AC left uncovered (ADR-FIN-009)
TC count      52      ✓ 1.13× the AC count — well under the ~2× over-engineering guard
Integration   n/a     scope = module: no INT-XM phase is emitted and none is owed (§2 rule 3).
                      XM-FIN-001 (SOFT-READ → MDL) is cited only as a precondition on the
                      lookup-validated creates, never asserted as a cross-module scenario
```
══════════════════════════════════════════════════════════════════
