# ADR-FIN-066 — Test generation re-run for FIN v2: the cases follow the frontend plan as ADR-FIN-064 and ADR-FIN-065 revised it, and a line-level AC half the published contract cannot meet is kept and stated as failing
Status      : ACCEPTED (non-breaking)
Stage       : test-gen (Test Generation, standalone)        Module: FIN        Version: v2
Lane        : test-gen
Decided     : 2026-09-24
traces      : AC-FIN-019, AC-FIN-021, AC-FIN-034, AC-FIN-036, AC-FIN-038, AC-FIN-042, AC-FIN-046, AC-FIN-081, REQ-FIN-019, REQ-FIN-021, REQ-FIN-038, REQ-FIN-075, SCR-FIN-002, SCR-FIN-003, SCR-FIN-004, SCR-FIN-005, SCR-FIN-006, SCR-FIN-007, SCR-FIN-010, SCR-FIN-011, SCR-FIN-013

## Context

The first v2 run of this stage (ADR-FIN-063) wrote its cases against the frontend plan of
2026-09-23, whose v2 surface was then unpublished (ADR-FIN-060, BLOCKED). Since then P3.2 has been
re-run twice:

- **ADR-FIN-064.** The republished api-docs carry CS-FIN-001's surface. ADR-FIN-060 is superseded,
  and so is the period-not-open note AC-FIN-081 asks for.
- **ADR-FIN-065.** The frontend build's consumer feedback is answered in the plan:
  - the five screens that own a child collection become a list, a `/new` page and a `/:id` record
    page;
  - no action is gated on the client, so every affordance renders for a holder of the screen and
    the server's 403 is the control;
  - the two journal-entry line refusals are bound to the lines pane's single refusal slot, because
    the published error body names no line;
  - the income-statement bounds are sent independently, and the balance sheet drills to the trial
    balance by the group's account type.

ADR-FIN-064 and ADR-FIN-065 both name this stage as the owner of the case alignment. The stage's
state therefore held cases that asserted:
- a hidden close-approve affordance (TC-FIN-082, TC-FIN-086) and a year-end close enabled only
  once every period is hard-closed (TC-FIN-084);
- routes the plan no longer draws (`?line=new`, a master list selected in place);
- a journal-line refusal shown against the offending line (TC-FIN-063, TC-FIN-067, TC-FIN-069);
- an income-statement range whose bounds travel together (TC-FIN-090);
- a catalogue case that fails until the backend republishes (TC-FIN-127);
- thirteen cases waiting on ADR-FIN-060.

One fact is not mechanical. AC-FIN-019 says the rejection names the offending line, and AC-FIN-021
says it names the offending line and dimension. The published error body of API-FIN-019 names
neither. ADR-FIN-065 §9 records that as a known limitation and a backend follow-up.

## Decision

1. **Every case follows the current frontend plan.** Routes are the plan's own; preconditions name
   the permission the server checks, never an affordance the client reads; each refusal is shown
   where the plan binds it (inline, generic, the lines pane's refusal slot, or the screen's
   forbidden message).

2. **No case asserts client-side gating.** TC-FIN-082, TC-FIN-084 and TC-FIN-086 are re-derived
   from their ACs. The action renders; the server's FIN-409-PERIODS-NOT-CLOSED,
   FIN-409-NOT-REOPENABLE or FIN-403-FORBIDDEN decides, and the case asserts that message on the
   screen. The separation of duty RULE-FIN-015 requires is asserted as server-enforced only.

3. **A picker's pre-empt is observed, and then bypassed.** The account pickers of SCR-FIN-006 list
   active leaf accounts only. A case whose AC needs a rollup or deactivated account on a line
   (TC-FIN-055, TC-FIN-063, TC-FIN-067) asserts that the picker does not offer it, then submits
   past it and asserts the server's refusal. This is the rule every earlier pre-empt case follows.

4. **The line-naming half of AC-FIN-019 and AC-FIN-021 is kept, not dropped or reworded.**
   - On the frontend track, TC-FIN-067 and TC-FIN-069 assert what the screen can show: the
     message once, in the lines pane's refusal slot, bound to no line.
   - On the backend track, TC-FIN-019 and TC-FIN-021 keep the AC's Then and state that this half
     is expected to fail against the build until the backend publishes the line, and the dimension
     too for the second. That is the follow-up ADR-FIN-065 records.
   - A test plan that silently dropped the half would hide a requirement the build does not meet.
     Rewording it would restate an AC, which this engine may not do.

5. **TC-FIN-127 reads a catalogue that now holds the note** (ADR-FIN-064) and is no longer stated
   as failing.

6. **Ids, counts and the change-manifest classes do not change.** No case is added or removed, and
   no id is reassigned. Every re-emitted v1 case stays MODIFIED, for the reason ADR-FIN-063 §(b)
   gives. The manifest's NOTE rows name the cases whose content changed:
   - backend: TC-FIN-019 and TC-FIN-021 (and the ADDED case TC-FIN-127);
   - frontend: every v1 case except TC-FIN-065, TC-FIN-066, TC-FIN-075, TC-FIN-077, TC-FIN-078,
     TC-FIN-087, TC-FIN-088, TC-FIN-089 and TC-FIN-091.

## Consequences

- Backend: 89 cases, AC 83/83, REQ 77/77, API 32/41, unchanged. Two cases, TC-FIN-019 and
  TC-FIN-021, are stated as partly failing against the build.
- Frontend: 56 cases, AC 56/56 in track, SCR 13/13, unchanged. None waits on publication.
- The test-execution manifest drops its ADR-FIN-060 note. Its ENT-FIN-007 row now says the
  unnumbered fiscal-year search is exercised on the frontend track only.
- Nothing in FIN's line artifacts changes. The line-level refusal remains the backend's to publish
  in P3.1's next delta. When it lands, the two backend cases pass as written and the two frontend
  cases can bind the refusal to its line.
