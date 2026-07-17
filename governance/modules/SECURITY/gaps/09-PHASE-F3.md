# PHASE 9 — F3 (Validators)
## Plan: PLAN-SEC-002 | Source: execution-plan-SEC-gaps.md → Section 7.3

---
## SESSION PROMPT — PASTE THIS FIRST

You are executing **Phase F3** of PLAN-SEC-002 for the Security module,
in this repository, using Claude Code.

FIRST: read `HANDOFF-PHASE-8-F2.md`. If missing or "Ready for Phase 9:
no" — STOP and ask me.

**Source of truth: `execution-plan-SEC-gaps.md` Section 7.3** (F3 —
Validators) in full, cross-checked against Section 1.3 (Rules) and
Section 4.2 (Error Catalog) — every message you show on screen MUST be
the exact Arabic/English pair from Section 4.2, no paraphrasing.

TASK — client-side validators mirroring server-side rules (server
remains source of truth; these are UX-layer only):
1. RULE-SEC-034 — branchIdFk required, dropdown sourced from ACTIVE
   branches only (already filtered by F2's facade).
2. RULE-SEC-035 — dataAccessLevel required (LOV-SEC-002 dropdown:
   BRANCH_ONLY / BRANCH_AND_CHILDREN / ALL).
3. RULE-SEC-036 — client-side pre-check for duplicate (role, branch)
   pair before submit, for UX only — do NOT treat this as authoritative;
   server's ERR-SEC-1036 remains the real gate.
4. RULE-SEC-040/041 — username/email format + required; do NOT add a
   client-side "check availability" call — no such endpoint exists in
   the plan, and inventing one would exceed scope.
5. RULE-SEC-038 (CRITICAL) — the Forgot Password form MUST show the
   IDENTICAL generic success message regardless of what the API
   response contains. Verify no code path branches UI behavior on
   whether the email existed — this would defeat the anti-enumeration
   rule at the UI layer even if the backend is correct.

DEFINITION OF DONE:
- [ ] All validators from Section 7.3 implemented
- [ ] Every displayed message matches Section 4.2 verbatim (AR + EN)
- [ ] RULE-SEC-038 UI-level verification done explicitly (describe how)
- [ ] No invented "check availability" endpoint calls

Write `HANDOFF-PHASE-9-F3.md` using the template below.

---
## HANDOFF REPORT TEMPLATE

```
# HANDOFF — PHASE 9 (F3) — PLAN-SEC-002
Validators implemented: [list, mapped to RULE-IDs]
Message-text cross-check vs Section 4.2: [confirmed exact / discrepancies: list]
RULE-SEC-038 UI-level verification: [describe test performed]
Ready for Phase 10 (SEC)? [yes/no]
```
