# PHASE 1 — CORE
## Plan: PLAN-SEC-002 | Source: execution-plan-SEC-gaps.md → Section 2

---
## SESSION PROMPT — PASTE THIS FIRST

You are executing **Phase CORE** of PLAN-SEC-002 for the Security module,
in this repository, using Claude Code.

**Your single source of truth is the file `execution-plan-SEC-gaps.md`
in this repo (governed artifact — do not re-derive requirements from
memory or from any other document).** Also read `db-script-SEC-gaps.md`
and `security-registry.md` for cross-reference — both are cited by the
plan and are authoritative for naming/conventions.

Read `execution-plan-SEC-gaps.md` Section 2 ("PHASE CORE") in full, plus
the "⚠ GOVERNANCE EXCEPTION" box near the top and Section 0 (Entry Gate).

TASK: Do NOT write any code in this phase. This phase is a **setup and
confirmation phase only**:
1. Confirm Conflict #19 sign-off status with me (the human operator)
   before touching any file — if I have not explicitly confirmed
   sign-off in this conversation, STOP and ask me.
2. Locate and report the exact file paths for: the entity/domain layer
   where `AuditableEntity` lives, the existing Security entities
   (USERS, ROLES, PERMISSIONS, SEC_PAGES, REFRESH_TOKENS,
   USER_ROLES, ROLE_PERMISSIONS), `JwtService`/`JwtProperties`,
   `LoginRateLimitFilter`, `LocalizedException`, and the migration/DDL
   mechanism actually used in this repo (Flyway? plain SQL scripts?
   `ddl-auto`?).
3. Confirm DB_TARGET = POSTGRESQL_16 matches this repo's actual
   datasource config.
4. Report back a short inventory — do not assume, verify by reading
   the actual files.

DEFINITION OF DONE:
- [ ] Conflict #19 sign-off explicitly confirmed by the human operator
- [ ] File-path inventory of the items in step 2 reported
- [ ] DB_TARGET confirmed against actual repo config
- [ ] No code written yet

Write `HANDOFF-PHASE-1-CORE.md` at repo root using the template below.

---
## HANDOFF REPORT TEMPLATE (agent fills this at end of phase)

```
# HANDOFF — PHASE 1 (CORE) — PLAN-SEC-002
Conflict #19 sign-off confirmed: [yes/no + how]
AuditableEntity location: [path]
Existing Security entities locations: [paths]
JwtService / JwtProperties: [paths]
LoginRateLimitFilter: [path]
LocalizedException: [path]
Migration mechanism in this repo: [Flyway / SQL script / other — path]
DB_TARGET confirmed: [yes/no, actual value found]
Open questions / discrepancies found vs. execution-plan-SEC-gaps.md: [list or "none"]
Ready for Phase 2 (DATA+DOM)? [yes/no]
```
