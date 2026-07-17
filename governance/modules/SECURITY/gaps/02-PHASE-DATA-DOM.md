# PHASE 2 — DATA+DOM
## Plan: PLAN-SEC-002 | Source: execution-plan-SEC-gaps.md → Section 1.1, 1.2, 1.4, 3

---
## SESSION PROMPT — PASTE THIS FIRST

You are executing **Phase DATA+DOM** of PLAN-SEC-002 for the Security
module, in this repository, using Claude Code.

FIRST: read `HANDOFF-PHASE-1-CORE.md` at repo root. If it does not exist,
or says Conflict #19 sign-off is not confirmed, or says "Ready for Phase 2:
no" — STOP and tell me instead of proceeding.

**Your single source of truth is `execution-plan-SEC-gaps.md`.** Read:
- Section 1.1 (Entities), 1.2 (LOVs), 1.4 (FIELD-ID Register)
- Section 3 (PHASE DATA+DOM — Domain Model Specifications) in full
Also read `db-script-SEC-gaps.md` BLOCK 1-8 for exact DDL (table/column
names, types, constraints, indexes, lookup seed values) — every column
you create in JPA entities MUST match that DDL byte-for-byte.

TASK — implement ONLY the data/domain layer:
1. Create JPA entities for: SEC_USER_PROFILE, SEC_ROLE_BRANCH,
   PASSWORD_RESET_TOKEN, ACCOUNT_ACTIVATION_TOKEN — exactly per Section
   3's specs (shared-PK pattern for SEC_USER_PROFILE, composite key for
   SEC_ROLE_BRANCH, no FK annotation on employeeIdFk per OQ-005).
2. Add the DB migration (in whatever mechanism you found in
   HANDOFF-PHASE-1-CORE.md) reproducing db-script-SEC-gaps.md BLOCK 1-8
   verbatim (tables, constraints, indexes, lookup seed INSERTs) —
   INCLUDING the `ALTER TABLE USERS ADD CONSTRAINT UK_USERS_EMAIL` from
   BLOCK 5b, since Conflict #19 sign-off was already confirmed in Phase 1.
3. Do NOT create repositories, services, or controllers yet — that is
   Phase SVC+API.
4. Do NOT invent any column not listed in db-script-SEC-gaps.md.

DEFINITION OF DONE:
- [ ] 4 new JPA entities compile, map 1:1 to db-script-SEC-gaps.md columns
- [ ] Migration applies cleanly on a fresh DB (verify by running it)
- [ ] UK_USERS_EMAIL constraint added to USERS
- [ ] DATA_ACCESS_LEVEL lookup seeded (3 values, per BLOCK 8)
- [ ] No repository/service/controller code written this phase

Write `HANDOFF-PHASE-2-DATA-DOM.md` using the template below.

---
## HANDOFF REPORT TEMPLATE

```
# HANDOFF — PHASE 2 (DATA+DOM) — PLAN-SEC-002
Entities created: [list classes + paths]
Migration file(s): [path(s)]
Migration verified applying cleanly: [yes/no — how verified]
UK_USERS_EMAIL applied: [yes/no]
Lookup seed applied: [yes/no]
Deviations from db-script-SEC-gaps.md (should be none — list or "none"):
Ready for Phase 3 (SVC+API)? [yes/no]
```
