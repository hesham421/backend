<!-- source: content outside every PHASE block (leading / between / trailing sections) -->
# BACKEND TEST PLAN — الحسابات العامة / Finance (General Ledger) (FIN)
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v2 (delta on v1 — change set CS-FIN-001)   Profile : erp   Track : backend   Plan : test
Scope  : **module** — modules FIN (`gov.py run-standalone test-gen --module FIN`)
Sources: `_state/current-srs.md` (v2) · `_state/current-backend-execution-plan.md` (v2) ·
         `_state/current-frontend-execution-plan.md` (v2) · `_state/current-registry-srs.md` (v2) ·
         `_state/current-registry-db.md` (v2) · `_state/current-registry-exec-fe.md` (v2) ·
         baseline `_state/current-backend-test-plan.md` (v1)
Framework : **agnostic** (`profile.stack.testing`). Every block below is the framework-agnostic
            TC form; no tool, annotation, fixture or file layout is named anywhere in this plan.
            The consumer repo chooses its framework and turns each TC into a test.
REDUCED   : no — the backend-execution-plan is present, so every step binds to a real `API-*`,
            the four v2 account-mapping endpoints API-FIN-038 … API-FIN-041 included.
TC ids    : v1 TC-FIN-001 … TC-FIN-052 on this track, the frontend holding TC-FIN-053 … TC-FIN-092;
            v2 continues the one shared module sequence at TC-FIN-093 … TC-FIN-129 on this track,
            and `frontend-test-plan-fin.md` continues at TC-FIN-130. No id is reused, none is
            renumbered, and no id is reassigned to a different subject than the one it carried.
Open ADRs : 3 raised by test-gen v2 — ADR-FIN-063 (first run), ADR-FIN-066 (second) and ADR-FIN-068
            (this run: host catalogue values created by the case that needs them, distinct values
            where cases need opposite states), all ACCEPTED, non-breaking. Applied and not re-derived: ADR-FIN-009 (v1 test-gen),
            ADR-FIN-010 … ADR-FIN-014, ADR-FIN-021, ADR-FIN-023 … ADR-FIN-031, ADR-FIN-037,
            ADR-FIN-039, ADR-FIN-040 … ADR-FIN-042, ADR-FIN-064, ADR-FIN-065. The first run cited
            ADR-FIN-060 as BLOCKED upstream; it is superseded by ADR-FIN-064.
══════════════════════════════════════════════════════════════════

**Delta v2 — what this file is.** A complete current plan, not a fragment. `gov.py state` folds a
marker plan by phase and carries back only a phase's missing *direct* children, so a SUB
re-emitted with its v2 atoms alone would lose every v1 atom inside it. The phase is therefore
re-emitted whole (ADR-FIN-063). Every v1 case whose content v2 leaves alone is restated
byte-identical.

- **ADDED** — thirty-seven cases, TC-FIN-093 … TC-FIN-129, one per v2 acceptance criterion
  AC-FIN-047 … AC-FIN-083 (TC = AC + 46). Every v2 AC is backend-exercisable, so this track's
  denominator stays the whole module.
- **MODIFIED** — five v1 cases whose content v2 made stale. TC-FIN-045 follows AC-FIN-045
  itself, which v2 modified (fifteen lookup types, DIRECT gone, CHEQUE seeded). TC-FIN-044 now
  counts thirteen screens and thirty actions. TC-FIN-008 no longer offers DIRECT as test data.
  TC-FIN-010 and TC-FIN-012 assert the 200 the published build-from-event contract returns,
  where v1 wrote 201. TC-FIN-049's text is unchanged, but its record runs on to the next SUB's
  START marker, whose `traces=` grew, so the tools read it as re-defined and it is listed
  MODIFIED too (ADR-FIN-063). The re-run of ADR-FIN-066 changes two more: the rollup-account
  and inactive-dimension cases, TC-FIN-019 and TC-FIN-021, now state that the "offending line
  is named" half of their ACs fails against the published error body, which names no line.
- **REMOVED** — none.

The change manifest lists **all 52** re-emitted v1 cases as MODIFIED, not only the six above.
The version check reads each case's START marker as a record of its own and compares it with the
baseline's heading record. A byte-identical marker atom therefore always reads as re-defined —
the same reason the P3.1 v2 plan lists API-FIN-001 … API-FIN-037 as MODIFIED. The content
changes are exactly the seven named above; TC-FIN-049 and the other forty-four are word for word
v1.

**Id order is AC order, not document order.** In v1, `TC-FIN-<seq>` maps 1:1 onto
`AC-FIN-<seq>` for the forty-six v1 criteria, and TC-FIN-047 … TC-FIN-052 extend six of them.
In v2, TC-FIN-093 … TC-FIN-129 map onto AC-FIN-047 … AC-FIN-083 in order. The v2 cases **open**
each SUB and the v1 cases follow: the tools compare a v1 record's full text, and a v2 case placed
after the last v1 case would change that record. The traceability index below is the address.

**Where the TCs come from, and where they deliberately stop.** Every TC below is derived from an
`AC-FIN-*`. The v1 boundary of ADR-FIN-009 holds unchanged: behaviour delivered on a recorded
decision or as an as-built narrowing gets no TC of its own. v2 adds five such paths, none of
which any AC states:

- the store-refusal translation of FIN-409-MAPPING-DUP under two simultaneous creates;
- FIN-404-ACCOUNT-MAPPING for an unknown mapping id;
- the trimming of `businessValue` on save and on event read (ADR-FIN-039);
- a blank, rather than absent, business field on the event;
- the RULE-FIN-019 mismatch (FIN-422-DERIVATION-SPEC), whose only AC, AC-FIN-049, is the case
  that passes.

They are named in the "catalog rows no TC exercises" list below and in ADR-FIN-063. If anyone
wants them covered, that is a gap on the requirements side, not a test-generation one.

**What is expected to fail against the build.** Nothing waits on publication: the republished
api-docs carry API-FIN-038 … API-FIN-041, the v2 members of API-FIN-011 and API-FIN-020, and the
period-not-open note AC-FIN-081 asks for (ADR-FIN-064), so the catalogue case, TC-FIN-127, reads
a document that holds it. Two v1 cases carry a half that the published contract cannot satisfy
yet. The rollup-account case, TC-FIN-019, and the inactive-dimension case, TC-FIN-021, assert
that the refusal names the offending line (and, for the second, the dimension). The published
error body of API-FIN-019 names neither, so that half fails until the backend publishes them —
the follow-up ADR-FIN-065 records. It is stated in both cases, and not dropped, because the AC
says it (ADR-FIN-066).



## TC TRACEABILITY INDEX

**AC → TC**

| AC | TC | AC | TC |
|---|---|---|---|
| AC-FIN-001 | TC-FIN-001 | AC-FIN-043 | TC-FIN-043 |
| AC-FIN-002 | TC-FIN-002 | AC-FIN-044 | TC-FIN-044 |
| AC-FIN-003 | TC-FIN-003 | AC-FIN-045 | TC-FIN-045 |
| AC-FIN-004 | TC-FIN-004 | AC-FIN-046 | TC-FIN-046 |
| AC-FIN-005 | TC-FIN-005 | AC-FIN-047 | TC-FIN-093 |
| AC-FIN-006 | TC-FIN-006 | AC-FIN-048 | TC-FIN-094 |
| AC-FIN-007 | TC-FIN-007 | AC-FIN-049 | TC-FIN-095 |
| AC-FIN-008 | TC-FIN-008 | AC-FIN-050 | TC-FIN-096 |
| AC-FIN-009 | TC-FIN-009, TC-FIN-047 | AC-FIN-051 | TC-FIN-097 |
| AC-FIN-010 | TC-FIN-010 | AC-FIN-052 | TC-FIN-098 |
| AC-FIN-011 | TC-FIN-011 | AC-FIN-053 | TC-FIN-099 |
| AC-FIN-012 | TC-FIN-012, TC-FIN-048 | AC-FIN-054 | TC-FIN-100 |
| AC-FIN-013 | TC-FIN-013 | AC-FIN-055 | TC-FIN-101 |
| AC-FIN-014 | TC-FIN-014, TC-FIN-049 | AC-FIN-056 | TC-FIN-102 |
| AC-FIN-015 | TC-FIN-015 | AC-FIN-057 | TC-FIN-103 |
| AC-FIN-016 | TC-FIN-016 | AC-FIN-058 | TC-FIN-104 |
| AC-FIN-017 | TC-FIN-017 | AC-FIN-059 | TC-FIN-105 |
| AC-FIN-018 | TC-FIN-018 | AC-FIN-060 | TC-FIN-106 |
| AC-FIN-019 | TC-FIN-019 | AC-FIN-061 | TC-FIN-107 |
| AC-FIN-020 | TC-FIN-020 | AC-FIN-062 | TC-FIN-108 |
| AC-FIN-021 | TC-FIN-021 | AC-FIN-063 | TC-FIN-109 |
| AC-FIN-022 | TC-FIN-022 | AC-FIN-064 | TC-FIN-110 |
| AC-FIN-023 | TC-FIN-023 | AC-FIN-065 | TC-FIN-111 |
| AC-FIN-024 | TC-FIN-024 | AC-FIN-066 | TC-FIN-112 |
| AC-FIN-025 | TC-FIN-025 | AC-FIN-067 | TC-FIN-113 |
| AC-FIN-026 | TC-FIN-026 | AC-FIN-068 | TC-FIN-114 |
| AC-FIN-027 | TC-FIN-027, TC-FIN-050 | AC-FIN-069 | TC-FIN-115 |
| AC-FIN-028 | TC-FIN-028 | AC-FIN-070 | TC-FIN-116 |
| AC-FIN-029 | TC-FIN-029 | AC-FIN-071 | TC-FIN-117 |
| AC-FIN-030 | TC-FIN-030 | AC-FIN-072 | TC-FIN-118 |
| AC-FIN-031 | TC-FIN-031, TC-FIN-051 | AC-FIN-073 | TC-FIN-119 |
| AC-FIN-032 | TC-FIN-032 | AC-FIN-074 | TC-FIN-120 |
| AC-FIN-033 | TC-FIN-033 | AC-FIN-075 | TC-FIN-121 |
| AC-FIN-034 | TC-FIN-034 | AC-FIN-076 | TC-FIN-122 |
| AC-FIN-035 | TC-FIN-035 | AC-FIN-077 | TC-FIN-123 |
| AC-FIN-036 | TC-FIN-036 | AC-FIN-078 | TC-FIN-124 |
| AC-FIN-037 | TC-FIN-037 | AC-FIN-079 | TC-FIN-125 |
| AC-FIN-038 | TC-FIN-038, TC-FIN-052 | AC-FIN-080 | TC-FIN-126 |
| AC-FIN-039 | TC-FIN-039 | AC-FIN-081 | TC-FIN-127 |
| AC-FIN-040 | TC-FIN-040 | AC-FIN-082 | TC-FIN-128 |
| AC-FIN-041 | TC-FIN-041 | AC-FIN-083 | TC-FIN-129 |
| AC-FIN-042 | TC-FIN-042 | — | — |

**REQ → TC** — for REQ-FIN-001 … REQ-FIN-046 each `REQ-FIN-<n>` carries the TC of its own
`AC-FIN-<n>`, plus the extensions in the first table. v2 requirements are not 1:1 with their
criteria, so the second table lists every one of them.

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

| REQ (v2) | AC | TC |
|---|---|---|
| REQ-FIN-047 | AC-FIN-047 | TC-FIN-093 |
| REQ-FIN-048 | AC-FIN-048 | TC-FIN-094 |
| REQ-FIN-049 | AC-FIN-049 | TC-FIN-095 |
| REQ-FIN-050 | AC-FIN-050, AC-FIN-051, AC-FIN-052 | TC-FIN-096, TC-FIN-097, TC-FIN-098 |
| REQ-FIN-051 | AC-FIN-053 | TC-FIN-099 |
| REQ-FIN-052 | AC-FIN-054, AC-FIN-055 | TC-FIN-100, TC-FIN-101 |
| REQ-FIN-053 | AC-FIN-056, AC-FIN-057 | TC-FIN-102, TC-FIN-103 |
| REQ-FIN-054 | AC-FIN-058 | TC-FIN-104 |
| REQ-FIN-055 | AC-FIN-059 | TC-FIN-105 |
| REQ-FIN-056 | AC-FIN-060 | TC-FIN-106 |
| REQ-FIN-057 | AC-FIN-061 | TC-FIN-107 |
| REQ-FIN-058 | AC-FIN-062 | TC-FIN-108 |
| REQ-FIN-059 | AC-FIN-063 | TC-FIN-109 |
| REQ-FIN-060 | AC-FIN-064 | TC-FIN-110 |
| REQ-FIN-061 | AC-FIN-065 | TC-FIN-111 |
| REQ-FIN-062 | AC-FIN-066 | TC-FIN-112 |
| REQ-FIN-063 | AC-FIN-067, AC-FIN-068 | TC-FIN-113, TC-FIN-114 |
| REQ-FIN-064 | AC-FIN-069 | TC-FIN-115 |
| REQ-FIN-065 | AC-FIN-070 | TC-FIN-116 |
| REQ-FIN-066 | AC-FIN-071 | TC-FIN-117 |
| REQ-FIN-067 | AC-FIN-072 | TC-FIN-118 |
| REQ-FIN-068 | AC-FIN-073 | TC-FIN-119 |
| REQ-FIN-069 | AC-FIN-074 | TC-FIN-120 |
| REQ-FIN-070 | AC-FIN-075 | TC-FIN-121 |
| REQ-FIN-071 | AC-FIN-076 | TC-FIN-122 |
| REQ-FIN-072 | AC-FIN-077, AC-FIN-078 | TC-FIN-123, TC-FIN-124 |
| REQ-FIN-073 | AC-FIN-079 | TC-FIN-125 |
| REQ-FIN-074 | AC-FIN-080 | TC-FIN-126 |
| REQ-FIN-075 | AC-FIN-081 | TC-FIN-127 |
| REQ-FIN-076 | AC-FIN-082 | TC-FIN-128 |
| REQ-FIN-077 | AC-FIN-083 | TC-FIN-129 |

**API → TC**

| API | TC |
|---|---|
| API-FIN-001 | TC-FIN-001, TC-FIN-002 (the unchanged-state assertion) |
| API-FIN-002 | TC-FIN-001 · TC-FIN-100 (setup: the child that makes a mapped account a parent) |
| API-FIN-003 | TC-FIN-002 |
| API-FIN-004 | TC-FIN-003 · TC-FIN-101 (setup: the mapped account deactivated) |
| API-FIN-005 | — no TC: no `AC-*` states a dimension search (ADR-FIN-009) |
| API-FIN-006 | TC-FIN-004 |
| API-FIN-007 | TC-FIN-005, TC-FIN-006 |
| API-FIN-008 | TC-FIN-005, TC-FIN-006 (the one-row assertion) |
| API-FIN-009 | — no TC: no `AC-*` states a rule search (ADR-FIN-009) |
| API-FIN-010 | TC-FIN-007 |
| API-FIN-011 | TC-FIN-008, TC-FIN-009, TC-FIN-047 · v2: TC-FIN-094, TC-FIN-095, TC-FIN-112, TC-FIN-115, TC-FIN-116 |
| API-FIN-012 | — no TC: no `AC-*` states a template search (ADR-FIN-009) |
| API-FIN-013 | TC-FIN-022 |
| API-FIN-014 | TC-FIN-023, TC-FIN-024 |
| API-FIN-015 | — no TC: no `AC-*` states an allocation-rule search (ADR-FIN-009) |
| API-FIN-016 | TC-FIN-025 |
| API-FIN-017 | TC-FIN-026 |
| API-FIN-018 | TC-FIN-027, TC-FIN-050, TC-FIN-011 (the one-entry assertion) |
| API-FIN-019 | TC-FIN-003, TC-FIN-014, TC-FIN-015, TC-FIN-017, TC-FIN-018, TC-FIN-019, TC-FIN-020, TC-FIN-021, TC-FIN-033, TC-FIN-049 · TC-FIN-121 (setup: manual entry B) |
| API-FIN-020 | TC-FIN-010, TC-FIN-011, TC-FIN-012, TC-FIN-013, TC-FIN-048 · v2: TC-FIN-093, TC-FIN-096 … TC-FIN-103, TC-FIN-107, TC-FIN-113, TC-FIN-114, TC-FIN-117 … TC-FIN-120, TC-FIN-123 … TC-FIN-128 |
| API-FIN-021 | TC-FIN-016, TC-FIN-028, TC-FIN-029, TC-FIN-030 · TC-FIN-129 |
| API-FIN-022 | TC-FIN-016, TC-FIN-017, TC-FIN-028, TC-FIN-046 · TC-FIN-108, TC-FIN-113, TC-FIN-114, TC-FIN-117, TC-FIN-121 |
| API-FIN-023 | TC-FIN-031, TC-FIN-051 |
| API-FIN-024 | TC-FIN-032, TC-FIN-035 |
| API-FIN-025 | TC-FIN-020, TC-FIN-033 |
| API-FIN-026 | TC-FIN-020, TC-FIN-034, TC-FIN-037, TC-FIN-038 |
| API-FIN-027 | TC-FIN-036, TC-FIN-052 |
| API-FIN-028 | TC-FIN-039, TC-FIN-046 · TC-FIN-108, TC-FIN-129 |
| API-FIN-029 | TC-FIN-040, TC-FIN-046 |
| API-FIN-030 | TC-FIN-041, TC-FIN-046 |
| API-FIN-031 | TC-FIN-042 |
| API-FIN-032 | TC-FIN-043 · TC-FIN-122, TC-FIN-129 |
| API-FIN-033 | — no TC: no `AC-*` states a fiscal-period search (ADR-FIN-009); it is cited only as setup |
| API-FIN-034 | — no TC of its own; cited as TC-FIN-013's alternative precondition (ADR-FIN-009) |
| API-FIN-035 | — no TC of its own; cited as the only precondition of TC-FIN-021 and TC-FIN-118 (ADR-FIN-009) |
| API-FIN-036 | — no TC: no `AC-*` states a template deactivate (ADR-FIN-009) |
| API-FIN-037 | — no TC: no `AC-*` states an allocation-rule deactivate (ADR-FIN-009) |
| API-FIN-038 | TC-FIN-105 · TC-FIN-109 (the one-active-row assertion) |
| API-FIN-039 | TC-FIN-104, TC-FIN-109, TC-FIN-110, TC-FIN-111 |
| API-FIN-040 | TC-FIN-106, TC-FIN-108 |
| API-FIN-041 | TC-FIN-107 · TC-FIN-103 (setup: the inactive mapping) |

**RULE / catalog code → TC**

| RULE | code | TC |
|---|---|---|
| RULE-FIN-001 | FIN-409-HAS-CHILDREN (409) | TC-FIN-002 |
| RULE-FIN-001 | FIN-409-PARENT-NOT-LEAF-ELIGIBLE (409) | — no TC: no `AC-*` states the create-path variant |
| RULE-FIN-002 | FIN-409-DIMVALUE-DUP (409) | TC-FIN-006 |
| RULE-FIN-003 | FIN-409-REMAINDER-COUNT (409) | TC-FIN-009, TC-FIN-047 |
| RULE-FIN-003 | FIN-422-REMAINDER-MARKER (422) | — no TC: no `AC-*` states marker disagreement |
| RULE-FIN-004 | FIN-409-DUPLICATE-EVENT (409) | TC-FIN-011 · v2: TC-FIN-123 (the pre-check), TC-FIN-124 (the store's refusal) |
| RULE-FIN-005 | FIN-404-NO-ACTIVE-RULE (404) | TC-FIN-013 |
| RULE-FIN-006 | FIN-409-UNBALANCED (409) | TC-FIN-015, TC-FIN-018 · TC-FIN-040 (the report invariant) |
| RULE-FIN-007 | FIN-409-NOT-POSTABLE-ACCOUNT (409) | TC-FIN-003, TC-FIN-015, TC-FIN-019 · v2: TC-FIN-100, TC-FIN-101 (a mapped account) |
| RULE-FIN-008 | FIN-409-PERIOD-NOT-OPEN (409) | TC-FIN-020, TC-FIN-033 · TC-FIN-036 (the carve-out, asserted as not raised) · v2: TC-FIN-125 (the event path), TC-FIN-127 (its published row) |
| RULE-FIN-009 | FIN-409-INVALID-DIMENSION (409) | TC-FIN-021 · v2: TC-FIN-118 (an event-derived value) · TC-FIN-117 (passes) |
| RULE-FIN-010 | — (success path) | TC-FIN-012, TC-FIN-026 · v2: TC-FIN-128 (a MAPPING remainder line) |
| RULE-FIN-010 | FIN-422-REMAINDER-NOT-POSITIVE (422) | TC-FIN-048 |
| RULE-FIN-011 | — (success path, no distinct code) | TC-FIN-028, TC-FIN-024 · v2: TC-FIN-129 |
| RULE-FIN-012 | — (success path, no distinct code) | TC-FIN-029, TC-FIN-024 |
| RULE-FIN-013 | FIN-409-ALREADY-REVERSED (409) | TC-FIN-030 |
| RULE-FIN-013 | FIN-409-NOT-POSTED (409) | — no TC: AC-FIN-030's Given is the already-reversed entry, not a DRAFT, and DRAFT is never a stored browsable state (SRS A7) |
| RULE-FIN-014 | FIN-409-NOT-REOPENABLE (409) | TC-FIN-035 · TC-FIN-032, TC-FIN-034 (the states around it) |
| RULE-FIN-015 | FIN-403-FORBIDDEN (403) | TC-FIN-038, TC-FIN-052 |
| RULE-FIN-016 | — (enforced by omission, no code) | TC-FIN-016, TC-FIN-017 · v2: TC-FIN-129 (the original of a new-path entry stays untouched) |
| RULE-FIN-017 | FIN-400-PERIOD-NOT-IN-YEAR, FIN-400-DOCDATE-OUTSIDE-PERIOD (400) | — no TC: no `AC-*` states the incoherent triple; RULE-FIN-017 was added at SVC-API and carries no AC of its own (ADR-FIN-009) |
| RULE-FIN-018 | FIN-422-UNSUPPORTED-DERIVATION (422) | TC-FIN-094 |
| RULE-FIN-019 | FIN-422-DERIVATION-SPEC (422) | — no TC raises it: AC-FIN-049 is the passing case, TC-FIN-095, and no `AC-*` states a mismatched spec (ADR-FIN-063) |
| RULE-FIN-020 | FIN-422-UNMAPPED-VALUE (422) | TC-FIN-102, TC-FIN-103, TC-FIN-107 · TC-FIN-096, TC-FIN-097, TC-FIN-098 (resolves) |
| RULE-FIN-021 | FIN-422-MISSING-BUSINESS-FIELD (422) | TC-FIN-099 (a MAPPING line's field), TC-FIN-119 (a tag's field) |
| RULE-FIN-022 | FIN-409-MAPPING-DUP (409) | TC-FIN-109 |
| RULE-FIN-023 | FIN-422-MAPPED-ACCOUNT-INELIGIBLE (422) | TC-FIN-110 |
| RULE-FIN-024 | FIN-422-INVALID-PAYMENT-METHOD (422) | TC-FIN-111 |
| RULE-FIN-025 | FIN-422-DIMENSION-TAG-DUP (422) | TC-FIN-115 |
| RULE-FIN-026 | FIN-422-DIMENSION-TAG-SOURCE (422) | TC-FIN-116 |
| RULE-FIN-027 | FIN-422-UNRESOLVED-DIMENSION-VALUE (422) | TC-FIN-120 · TC-FIN-114 (resolves) |
| RULE-FIN-028 | FIN-422-NO-DEFINED-PERIOD (422) | TC-FIN-126 |

**Catalog rows no TC exercises**, stated rather than left unsaid: FIN-409-ACCOUNT-DUP,
FIN-409-DIMENSION-DUP, FIN-409-RULE-DUP, FIN-409-YEAR-DUP, FIN-400-INVALID-LOOKUP,
FIN-400-MISSING-FREQUENCY, FIN-400-INVALID-SORT, FIN-422-REMAINDER-MARKER,
FIN-422-DERIVATION-SPEC, FIN-409-NOT-ACTIVE, FIN-409-INVALID-TRANSITION,
FIN-409-PERIODS-NOT-CLOSED, FIN-400-PERIOD-NOT-IN-YEAR, FIN-400-DOCDATE-OUTSIDE-PERIOD,
FIN-404-ACCOUNT-MAPPING, the `FIN-404-*` not-found rows raised outside an AC's own path, and
FIN-500. No `AC-FIN-*` states any of them, and this engine derives from the ACs alone. Writing a
TC for a code no acceptance criterion asserts is the over-engineering §3 rule 6 forbids. They are
a requirements-side gap if anyone wants them covered, not a test-generation one — ADR-FIN-009 and
ADR-FIN-063 record the boundary. Two rows are not gaps and must not be tested: FIN-403-SOD-VIOLATION
is struck as unreachable, and TC-FIN-038 says so; FIN-422-MAPPING-UNSUPPORTED is struck in v2,
MAPPING being implemented (ADR-FIN-027), and no path raises it.

## COVERAGE

```
AC  covered   83/83   ✓ 0 gaps   (every AC-FIN-001…083 carries ≥1 TC on this track; v2 adds 37,
                        AC-FIN-047…083, and AC-FIN-045 keeps TC-FIN-045, modified to its v2 text)
REQ covered   77/77   ✓ 0 gaps   (REQ-FIN-047…077 are new in v2)
API covered   32/41   — 9 endpoints carry no TC, each named in the API → TC index with the reason;
                        none is an AC left uncovered (ADR-FIN-009). The four v2 endpoints
                        API-FIN-038…041 are all covered. (v1 printed 26/37 over a table that
                        counts 28/37; the figure here is the table's own.)
TC count      89      ✓ 1.07× the AC count (52 v1 + 37 v2) — well under the ~2× guard
Integration   n/a     scope = module: no INT-XM phase is emitted and none is owed (§2 rule 3).
                      The one live XM, XM-FIN-001 (SOFT-READ → MDL, widened in v2 to 15 keys),
                      is cited only as a precondition on the lookup-validated writes, never
                      asserted as a cross-module scenario
```
══════════════════════════════════════════════════════════════════
