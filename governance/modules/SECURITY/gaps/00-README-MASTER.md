# CLAUDE CODE EXECUTION KIT — PLAN-SEC-002
## Security Module — DataScope Extension + Self-Service Auth Gap Package

```
Source Plan   : execution-plan-SEC-gaps.md (PLAN-SEC-002, Gate Status: CONDITIONAL)
Governance    : Conflict #19 (master-registry.md Section 13) MUST be signed off
                by architecture authority BEFORE Phase 1 (CORE) is executed
                against the real codebase. This kit assumes that sign-off has
                happened OR that you are running this in a sandboxed/dry-run
                branch with explicit awareness of the exception being sought.
Execution     : 11 phases, ONE Claude Code session per phase (separate context)
Continuity    : Each phase produces a HANDOFF-PHASE-[N].md — the NEXT phase's
                session prompt tells the agent to read it first.
```

---

## ⚠ MANDATORY PRE-FLIGHT CHECK (do this once, before Phase 1)

Before opening the first Claude Code session, confirm ALL of the following:

1. [ ] Conflict #19 has been signed off by architecture authority (or you are
       knowingly proceeding without it, on a throwaway/sandbox branch).
2. [ ] The repository has `execution-plan-SEC-gaps.md`, `db-script-SEC-gaps.md`,
       `security-registry.md`, and `master-registry.md` accessible on disk
       (copy them into the repo, e.g. under `/docs/governance/`).
3. [ ] `DB_TARGET = POSTGRESQL_16` confirmed (per db-script-SEC-gaps.md).
4. [ ] You have a way to run the app + DB locally to verify each phase before
       moving to the next (migrations apply cleanly, app boots, tests green).

---

## PHASE EXECUTION ORDER (strict — do not reorder, do not skip)

| # | File | Phase | Depends on |
|---|---|---|---|
| 1 | 01-PHASE-CORE.md | CORE | Pre-flight check |
| 2 | 02-PHASE-DATA-DOM.md | DATA+DOM | Phase 1 handoff |
| 3 | 03-PHASE-SVC-API.md | SVC+API | Phase 2 handoff |
| 4 | 04-PHASE-DOC.md | DOC | Phase 3 handoff |
| 5 | 05-PHASE-INT-C.md | INT-C | Phase 4 handoff |
| 6 | 06-PHASE-INT-R.md | INT-R | Phase 5 handoff |
| 7 | 07-PHASE-F1.md | F1 (Screens) | Phase 6 handoff — CONTRACT ALIGNMENT GATE (DOC ✓ + INT-C ✓ required) |
| 8 | 08-PHASE-F2.md | F2 (Facades) | Phase 7 handoff |
| 9 | 09-PHASE-F3.md | F3 (Validators) | Phase 8 handoff |
| 10 | 10-PHASE-SEC.md | SEC (Permissions/Seed) | Phase 9 handoff |
| 11 | 11-PHASE-ALIGN.md | ALIGN (final self-check) | Phase 10 handoff |

After Phase 11: proceed to MODE 2.5 (test-plan.md generation) in the
Execution Plan Governance Engine project — NOT in Claude Code.

---

## HOW TO RUN EACH PHASE (repeat for every phase file)

1. Open a **new** Claude Code session, in the actual repo root.
2. Copy the entire **"SESSION PROMPT — PASTE THIS FIRST"** block from that
   phase's file and send it as your first message.
3. Let the agent work. Review its diff carefully (`بدقة عالية` — do not
   rubber-stamp; check column names against db-script-SEC-gaps.md verbatim,
   check ERR-IDs/messages against Section 4.2 of the plan verbatim).
4. When the agent reports it is done, explicitly ask it to write
   `HANDOFF-PHASE-[N]-[NAME].md` at the repo root, using the "Handoff Report
   Template" included at the bottom of that phase's file.
5. Verify the Definition of Done checklist for that phase before closing
   the session.
6. Start the next phase's session fresh — do NOT continue in the same
   context window (this is intentional: forces re-grounding in the
   governed artifacts instead of drifting on accumulated chat context).

---

## HIGH-PRECISION RULES THAT APPLY TO EVERY PHASE (بدقة عالية)

```
1. NEVER invent a column name. Every column MUST come verbatim from
   db-script-SEC-gaps.md. Security is an EXCEPTION module — AS-IS names
   only (USER_ID_FK, BRANCH_ID_FK, IS_ACTIVE_FL, etc.) — NOT the
   Pk/Fk/Fl convention used elsewhere in the ERP.
2. NEVER use NotFoundException — always LocalizedException (project
   standard, security-registry.md §7.2).
3. NEVER add a VERSION/optimistic-locking column — not used in this
   project.
4. NEVER set audit fields (createdBy/createdAt/updatedBy/updatedAt) in
   Mapper or Service code — AuditEntityListener does this.
5. Deactivation = isActiveFl=false, NEVER a physical DELETE — except
   the two token tables and DELETE /role-branches/{id}, which are
   explicitly specified as real deletes/consumption in the plan.
6. If anything in the codebase contradicts the plan (e.g. a column
   doesn't exist, an existing filter doesn't work as described), STOP
   and report the discrepancy — do NOT silently improvise. This is a
   governance requirement, not just good practice.
7. Every RULE-ID implemented must produce the EXACT Arabic + English
   message pair from Section 4.2 of execution-plan-SEC-gaps.md — do not
   paraphrase.
8. Stay inside this phase's scope ONLY. Do not "helpfully" start next
   phase's work early — that breaks the handoff/review discipline.
```
