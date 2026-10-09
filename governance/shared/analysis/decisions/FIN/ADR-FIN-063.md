# ADR-FIN-063 — Test generation for FIN v2: complete re-emission, the in-track denominator, cases bound to an unpublished surface, and five stale v1 cases
Status      : ACCEPTED (non-breaking)
Stage       : test-gen (Test Generation, standalone)        Module: FIN        Version: v2
Lane        : test-gen
Decided     : 2026-09-23
traces      : AC-FIN-045, AC-FIN-049, AC-FIN-081, REQ-FIN-045, REQ-FIN-075, SCR-FIN-003, SCR-FIN-013

## Context

CS-FIN-001 adds thirty-seven acceptance criteria (AC-FIN-047 … AC-FIN-083), modifies AC-FIN-045,
and adds one screen (SCR-FIN-013). The test-gen engine forbids questions, so every choice below
that was not mechanical had to be settled here. ADR-FIN-009 (v1) stands unchanged: TCs derive
from ACs alone, the frontend denominator is the set of ACs a screen can exercise, and onboarding
ACs carry an empty `Exercises` binding. This ADR extends it to v2.

Five facts forced a decision.

**(a) A delta test plan cannot be a fragment.** The engine says to emit only ADDED, MODIFIED and
REMOVED cases. But `gov.py state` folds a marker plan by phase, carrying back only a phase's
missing **direct** children, which here are the SUBs `RULE-SCENARIOS` / `API-SCENARIOS` and
`UI-FLOWS` / `INT-FLOW`. A v2 SUB holding only its v2 atoms would replace the v1 SUB of the same
label, and every v1 case inside it would vanish from `_state/current-*-test-plan.md`. The profile
fixes the SUB labels, so the v2 cases cannot live in SUBs of their own. The P3.1 and P3.2 v2
plans met the same constraint and re-emitted in full (ADR-FIN-029).

**(b) The version check reads a byte-identical marker atom as re-defined.** `ids-continue`
(C12.3) keeps one baseline text per id — the heading record, the last one it reads. It then
compares every record of the new file against that text, the TC's START-marker record included,
which is a separate, one-line record. So a v1 case restated word for word is still reported as
"re-defined … but not listed as MODIFIED". In the same way, the last v1 case before a SUB whose
START `traces=` grew has a heading record that runs on to that marker, so its text differs even
though its content does not: TC-FIN-049 on the backend, TC-FIN-091 on the frontend.

**(c) Twenty-one v2 ACs have no screen.** Their When is an event arriving — the mapped account,
the resolved dimension, and every event rejection happen inside API-FIN-020, which no screen
calls (ADR-FIN-007). AC-FIN-081 asks what a published document says.

**(d) The frontend's v2 surface is designed but not callable.** SCR-FIN-013 and the v2 half of
SCR-FIN-003's Line entry are drawn in full — route, guard, facade, forms. Their calls, the v2
members of API-FIN-011 and the twelve v2 catalogue codes are not in the fetched api-docs. That is
ADR-FIN-060, BLOCKED at P3.2. The backend plan, by contrast, declares all of them.

**(e) Five v1 cases went stale without their ACs changing, and one AC changed.** AC-FIN-045 now
says fifteen lookup types. TC-FIN-044 counted twelve screens and twenty-seven actions, where v2
has thirteen and thirty. TC-FIN-008 offered DIRECT as test data, which RULE-FIN-018 now refuses.
TC-FIN-010 and TC-FIN-012 asserted 201 on the build-from-event endpoint, whose published
contract answers 200. On the frontend, TC-FIN-059, TC-FIN-060 and TC-FIN-061 described v1's
engine-rules form, which is replaced by the one-line-per-submit form of ADR-FIN-062. TC-FIN-079
said no fiscal-year search exists, which ADR-FIN-061 superseded.

## Decision

1. **Both test plans are re-emitted complete.** v1 cases that v2 leaves alone are restated
   byte-identical. v2 cases **open** each SUB, so that the last v1 case of the phase keeps its
   record text. The test-execution manifest carries no id and is regenerated in full.

2. **The change manifest lists every re-emitted v1 case as MODIFIED**: TC-FIN-001 … TC-FIN-052
   on the backend and TC-FIN-053 … TC-FIN-092 on the frontend. This is forced by (b), and it
   follows the precedent of the P3.1 plan, which lists API-FIN-001 … API-FIN-037. Each plan's
   header names the cases whose content actually changed, and the manifest carries the same
   list as a note:
   - backend: TC-FIN-008, TC-FIN-010, TC-FIN-012, TC-FIN-044, TC-FIN-045;
   - frontend: TC-FIN-059, TC-FIN-060, TC-FIN-061, TC-FIN-079.

   TC-FIN-049 and TC-FIN-091 are named separately, as record-boundary changes only.

3. **v2 ids continue the one shared sequence.** The backend track takes TC-FIN-093 …
   TC-FIN-129, one case per v2 AC in AC order (TC = AC + 46). The frontend track takes TC-FIN-130
   … TC-FIN-145, one per in-track v2 AC in AC order. No boundary, permission or extra case is
   added. No v2 AC states a numeric limit that its own case does not already test (AC-FIN-082 is
   the boundary), and none is a permission AC.

4. **The frontend denominator becomes 56 of 83.** That is v1's 40 plus the sixteen v2 ACs a
   screen can exercise: AC-FIN-048, 049, 058 … 066, 069, 070, 075, 076 and 083. The twenty-seven
   outside it are listed in the frontend header, each beside the backend case that covers it.
   Module-wide coverage is 83/83.

5. **Cases bound to the unpublished surface are written now, against the drawn screen.** These
   are TC-FIN-130 … TC-FIN-142. Each states in its Preconditions that the call it needs must be
   published (ADR-FIN-060). They assert the SRS messages character-perfect in both languages,
   and they name each code as the backend plan's catalogue does. The frontend coverage block
   counts them as waiting. This delays when they run; it narrows nothing they assert, and it is
   not a derivation blocker. Nothing on the backend track waits.

6. **AC-FIN-081 gets a document case.** TC-FIN-127 is bound to API-FIN-020's published error
   catalogue, read as a document with no call. It states that the copy fetched for P3.2 lacks
   the message and note the AC asks for, so it fails until the backend republishes.

7. **As-built v2 behaviour that no AC states gets no case**, extending ADR-FIN-009 §2:
   - the store-refusal half of FIN-409-MAPPING-DUP;
   - FIN-404-ACCOUNT-MAPPING;
   - the blank-value half of RULE-FIN-021;
   - the trimming of `businessValue` (ADR-FIN-039);
   - FIN-422-DERIVATION-SPEC. RULE-FIN-019's only AC, AC-FIN-049, is the passing case.

   Each is named in the plans' "catalog rows no TC exercises" lists.
   FIN-422-MAPPING-UNSUPPORTED is struck in v2 (ADR-FIN-027) and is treated like
   FIN-403-SOD-VIOLATION: not a gap, and not to be tested.

8. **The five stale backend cases and four stale frontend cases are modified** to their v2 truth,
   each change sourced from the v2 SRS, backend plan or frontend plan. AC-FIN-045's case follows
   the AC's own new text.

## Consequences

- Backend: 89 cases (52 + 37), AC 83/83, REQ 77/77, API 32/41. The nine endpoints without a
  case are the same nine v1 had. v1's printed 26/37 was a miscount of its own table, which
  counts 28, and v2 prints the table's figure.
- Frontend: 56 cases (40 + 16), AC 56/56 in track, SCR 13/13. Thirteen cases wait for
  ADR-FIN-060.
- The fold of `_state/current-*-test-plan.md` carries nothing from v1: every v1 child is present
  in the delta.
- Nothing in FIN's line artifacts changes. No REQ, AC, RULE, API, screen or catalogue row is
  added, removed or reworded. The gaps named in decision 7 are for P1 to close, if anyone wants
  them covered.
- A later factory fix to `ids-continue` — comparing a marker record with the baseline's marker
  record — would let a later delta list only the cases whose content changed.
