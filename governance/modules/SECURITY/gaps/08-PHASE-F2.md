# PHASE 8 — F2 (Facades)
## Plan: PLAN-SEC-002 | Source: execution-plan-SEC-gaps.md → Section 7.2

---
## SESSION PROMPT — PASTE THIS FIRST

You are executing **Phase F2** of PLAN-SEC-002 for the Security module,
in this repository, using Claude Code.

FIRST: read `HANDOFF-PHASE-7-F1.md`. If missing or "Ready for Phase 8:
no" — STOP and ask me.

**Source of truth: `execution-plan-SEC-gaps.md` Section 7.2** (F2 —
Facade / State Ownership) in full, plus Section 4.1 (API Register) for
exact endpoint bindings.

TASK — implement the 4 facades exactly as specified, wired to the
screens from Phase 7:
1. `UserProfileFacade` — search()→API-SEC-033, load(id)→API-SEC-035,
   create()→API-SEC-032, update(id)→API-SEC-034. Branch dropdown
   sourced via API-ORG-008 (active branches).
2. `RoleBranchFacade` — search(roleId)→API-SEC-037, assign()→API-SEC-036,
   update(id)→API-SEC-038, remove(id)→API-SEC-039.
3. `SignUpFacade` — submit()→API-SEC-040, activate(token)→API-SEC-041.
4. `ForgotPasswordFacade` — requestReset()→API-SEC-042, reset(token)→API-SEC-043.

Each Facade sits between its Component and the corresponding Angular
Service (per the repo's existing Models/Services/Facades/Helpers/
Components architecture, if that convention exists — verify against
an existing Security-adjacent screen's facade if one exists, and follow
the SAME derived-signals pagination pattern for search results).

DEFINITION OF DONE:
- [ ] 4 facades implemented, each API-ID correctly bound (no undocumented
      endpoint usage — only the endpoints listed above, per API CONTRACT
      COMPLETENESS rule)
- [ ] Facades wired into Phase 7's screen shells (screens now show real data)
- [ ] Pagination pattern consistent with the rest of the app

Write `HANDOFF-PHASE-8-F2.md` using the template below.

---
## HANDOFF REPORT TEMPLATE

```
# HANDOFF — PHASE 8 (F2) — PLAN-SEC-002
Facades created: [list + paths + API-IDs bound]
Pagination pattern used: [describe, confirm consistency]
Any undocumented endpoint usage (should be none): [list or "none"]
Ready for Phase 9 (F3)? [yes/no]
```
