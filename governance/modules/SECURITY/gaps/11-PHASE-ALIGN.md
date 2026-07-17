# PHASE 11 — ALIGN (Final Self-Check)
## Plan: PLAN-SEC-002 | Source: execution-plan-SEC-gaps.md → Section 12 (+ 9, 10, 11, 13)

---
## SESSION PROMPT — PASTE THIS FIRST

You are executing the final **Phase ALIGN** of PLAN-SEC-002 for the
Security module, in this repository, using Claude Code.

FIRST: read `HANDOFF-PHASE-10-SEC.md`. If missing or "Ready for Phase
11: no" — STOP and ask me. Also read ALL previous handoff files
(`HANDOFF-PHASE-1-CORE.md` through `HANDOFF-PHASE-10-SEC.md`) — this
phase audits the ENTIRE build against the plan, not just the last step.

**Source of truth: `execution-plan-SEC-gaps.md` Section 12** (GATE
ALIGN table) — walk it row by row against the real repository. Also
re-check Section 9 (Derivation Log — confirm every DRV-ID decision was
actually followed as recorded in the handoffs), Section 10 (OQ Log —
confirm none of the "non-blocking" OQs were accidentally treated as
resolved), and Section 11 (TC Coverage Matrix Summary — do NOT generate
test-plan.md here, that is MODE 2.5 in a different tool/project; just
confirm nothing in this build would make that coverage claim false).

TASK — produce a single ALIGN audit report, NOT new feature code:
1. For each row in Section 12's gate table (CORE, DATA+DOM, SVC+API,
   DOC, INT-C, INT-R, F1, F2, F3, SEC), mark ✓ or ✗ against what was
   ACTUALLY built (verified by reading code/running it), not against
   what the handoffs merely claim.
2. Confirm zero invented columns anywhere (final grep pass: diff every
   entity's `@Column` names against db-script-SEC-gaps.md).
3. Confirm zero `NotFoundException` usages introduced in the new code.
4. Confirm every ERR-ID from Section 4.2 is reachable and produces the
   exact message pair.
5. Confirm Conflict #19 status is still correctly represented — do NOT
   remove or "clean up" any governance references/comments pointing to
   it; they must remain until architecture authority formally closes it.
6. List any ✗ found — do not mark ALIGN ✓ if any row fails.

DEFINITION OF DONE:
- [ ] Full Section 12 gate table re-verified against real code, not assumptions
- [ ] Zero invented columns confirmed
- [ ] Zero NotFoundException confirmed
- [ ] All ERR-IDs verified reachable with exact messages
- [ ] Governance/Conflict #19 references intact in code comments/docs
- [ ] Final verdict: ALIGN ✓ (all rows pass) or ALIGN ✗ (list every failing row)

Write `HANDOFF-PHASE-11-ALIGN.md` using the template below. This is the
LAST file in this kit — after this, the human operator hands the whole
build off to MODE 2.5 (test-plan.md) and MODE 4A (pre-flight audit) in
the Execution Plan Governance Engine, outside Claude Code.

---
## HANDOFF REPORT TEMPLATE

```
# HANDOFF — PHASE 11 (ALIGN) — PLAN-SEC-002 — FINAL
Gate table verification (✓/✗ per row, with evidence):
  CORE:      [✓/✗ — evidence]
  DATA+DOM:  [✓/✗ — evidence]
  SVC+API:   [✓/✗ — evidence]
  DOC:       [✓/✗ — evidence]
  INT-C:     [✓/✗ — evidence]
  INT-R:     [✓/✗ — evidence]
  F1:        [✓/✗ — evidence]
  F2:        [✓/✗ — evidence]
  F3:        [✓/✗ — evidence]
  SEC:       [✓/✗ — evidence]
Invented-column grep result: [clean / list violations]
NotFoundException grep result: [clean / list violations]
ERR-ID reachability confirmed: [list each ERR-ID, pass/fail]
Conflict #19 references intact: [yes/no]
FINAL VERDICT: [ALIGN ✓ — ready for MODE 2.5/4A | ALIGN ✗ — blockers: list]
```
