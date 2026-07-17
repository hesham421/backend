# PHASE 4 — DOC
## Plan: PLAN-SEC-002 | Source: execution-plan-SEC-gaps.md → Section 5

---
## SESSION PROMPT — PASTE THIS FIRST

You are executing **Phase DOC** of PLAN-SEC-002 for the Security module,
in this repository, using Claude Code.

FIRST: read `HANDOFF-PHASE-3-SVC-API.md`. If missing or "Ready for Phase
4: no" — STOP and ask me.

**Source of truth: `execution-plan-SEC-gaps.md` Section 5** (PHASE DOC),
cross-checked against Section 1.4 (FIELD-ID Register) and Section 4.1
(API Register). Also check how existing Security endpoints are
documented today (Swagger/OpenAPI annotations on existing controllers)
and follow the SAME pattern — do not introduce a new documentation
convention for this module alone.

TASK:
1. Add OpenAPI/Swagger documentation (or whatever mechanism the
   existing controllers use) for all 12 new endpoints.
2. Every request/response schema must expose exactly the FIELD-IDs
   listed in Section 1.4 for that entity — no more, no fewer fields.
3. Group the new endpoints as described in Section 4.1's "CONTRACT
   NOTES" (a "Security — DataScope" group for API-SEC-032..039, and
   extend the existing "Security — Auth" group for API-SEC-040..043)
   — unless the repo's existing grouping convention differs, in which
   case follow the repo's convention and note the deviation.

DEFINITION OF DONE:
- [ ] All 12 endpoints documented, matching the FIELD-ID Register exactly
- [ ] Docs render correctly (verify via Swagger UI or equivalent if repo has one)
- [ ] No extra/missing fields vs. Section 1.4

Write `HANDOFF-PHASE-4-DOC.md` using the template below.

---
## HANDOFF REPORT TEMPLATE

```
# HANDOFF — PHASE 4 (DOC) — PLAN-SEC-002
Documentation mechanism used: [Swagger annotations / other + path]
Endpoints documented: [list 12, confirm each]
Field-list cross-check vs Section 1.4: [confirmed / discrepancies: list]
DOC ✓ gate (per plan Section 12 ALIGN table): [pass/fail]
Ready for Phase 5 (INT-C)? [yes/no]
```
