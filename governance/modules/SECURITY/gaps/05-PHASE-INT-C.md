# PHASE 5 — INT-C (Contract)
## Plan: PLAN-SEC-002 | Source: execution-plan-SEC-gaps.md → Section 6.1, 6.2

---
## SESSION PROMPT — PASTE THIS FIRST

You are executing **Phase INT-C** of PLAN-SEC-002 for the Security
module, in this repository, using Claude Code.

FIRST: read `HANDOFF-PHASE-4-DOC.md`. If missing or DOC ✓ gate = fail —
STOP (per the plan's Section 12: "CONTRACT ALIGNMENT GATE — DOC ✓ must
pass — if it fails, F1/F2/F3 are BLOCKED", and INT-C sits before that
gate so it must also be sound).

**Source of truth: `execution-plan-SEC-gaps.md` Section 6.1** (Outbound
XM Register — XM-SEC-001, XM-SEC-002) and **Section 6.2** (event-based
integration for RULE-SEC-031, already implemented in Phase 3 — this
phase VERIFIES and formalizes the contract, it does not re-implement).

TASK:
1. Verify SEC_USER_PROFILE.branchIdFk and SEC_ROLE_BRANCH.branchIdFk
   correctly call into the Organization module's branch-validation path
   (API-ORG-012 GET /api/v1/org/branches/{id} for active-branch check
   per RULE-SEC-034, API-ORG-008 GET /api/v1/org/branches for the F1
   dropdown source, to be wired in Phase F1/F2) — confirm the FK
   constraint from Phase 2 plus the Service-layer active-check from
   Phase 3 together fully satisfy RULE-SEC-034.
2. Verify the RULE-SEC-031 event contract from Phase 3: confirm event
   payloads carry exactly `userIdFk, token, expiresAt` as specified,
   and that NO direct compile-time dependency from Security to
   NotificationService was introduced.
3. Document both contracts (Organization branch lookup + the two
   domain events) in whatever internal integration-contract doc/comment
   convention this repo already uses (if none exists, add a short
   `docs/integrations/SEC-INT-CONTRACTS.md` describing them).
4. Do NOT modify Organization module code — Security only CONSUMES it.

DEFINITION OF DONE:
- [ ] XM-SEC-001 and XM-SEC-002 contracts verified against actual
      Organization module endpoints (confirm they exist and behave as
      expected — do not assume)
- [ ] Event contract (RULE-SEC-031) verified: no direct
      NotificationService dependency
- [ ] Integration contracts documented somewhere in the repo
- [ ] INT-C ✓ gate condition met

Write `HANDOFF-PHASE-5-INT-C.md` using the template below.

---
## HANDOFF REPORT TEMPLATE

```
# HANDOFF — PHASE 5 (INT-C) — PLAN-SEC-002
XM-SEC-001 contract verified against: [actual endpoint/path found]
XM-SEC-002 contract verified against: [actual endpoint/path found]
Event contract verified — no direct NotificationService coupling: [yes/no]
Integration contract doc location: [path]
INT-C ✓ gate: [pass/fail — if fail, describe blocker]
Ready for Phase 6 (INT-R)? [yes/no]
```
