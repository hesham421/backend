# PHASE 6 — INT-R (Resolution / Readiness)
## Plan: PLAN-SEC-002 | Source: execution-plan-SEC-gaps.md → Section 6.1 (Status column)

---
## SESSION PROMPT — PASTE THIS FIRST

You are executing **Phase INT-R** of PLAN-SEC-002 for the Security
module, in this repository, using Claude Code.

FIRST: read `HANDOFF-PHASE-5-INT-C.md`. If missing or INT-C ✓ = fail —
STOP and ask me.

**Source of truth: `execution-plan-SEC-gaps.md` Section 6.1**, "Status"
column — both XM-SEC-001 and XM-SEC-002 are marked **READY** (Organization
module is GOVERNED ✓, DBS-ORG-001). This phase is intentionally small:
there is NO deferred/blocked XM dependency to resolve or workaround in
this plan — both cross-module FKs are live (applied in Phase 2's
migration per db-script-SEC-gaps.md BLOCK 5e).

TASK:
1. Confirm (do not assume) that the physical FK constraints
   `FK_SEC_USER_PROFILE_BRANCH` and `FK_SEC_ROLE_BRANCH_BRANCH` exist
   and are enforced in the actual database after Phase 2's migration ran.
2. Run an integration check: attempt to insert a SEC_USER_PROFILE row
   with a non-existent branchIdFk and confirm the DB rejects it (FK
   violation) — and confirm the Service layer (Phase 3) turns that into
   the RULE-SEC-034 / ERR-SEC-1034 LocalizedException rather than a raw
   DB error leaking to the client.
3. There is nothing else to "resolve" in this phase — if you find a
   genuine gap (e.g., the FK is missing, or ORG_BRANCH's table doesn't
   actually exist in this environment), STOP and report it as a blocker
   rather than improvising a workaround.

DEFINITION OF DONE:
- [ ] Both FK constraints confirmed live in the actual DB
- [ ] Invalid-branch insert attempt confirmed to surface as
      ERR-SEC-1034 (LocalizedException), not a raw DB exception
- [ ] No workaround/deferral needed — both XM-IDs confirmed READY in practice

Write `HANDOFF-PHASE-6-INT-R.md` using the template below.

---
## HANDOFF REPORT TEMPLATE

```
# HANDOFF — PHASE 6 (INT-R) — PLAN-SEC-002
FK_SEC_USER_PROFILE_BRANCH confirmed live: [yes/no]
FK_SEC_ROLE_BRANCH_BRANCH confirmed live: [yes/no]
Invalid-branch test result: [pass/fail + evidence]
Any blockers found (should be none): [list or "none"]
Ready for Phase 7 (F1)? [yes/no — note: CONTRACT ALIGNMENT GATE requires
  DOC ✓ (Phase 4) AND INT-C ✓ (Phase 5) — confirm both still hold]
```
