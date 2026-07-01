# ERP Governance Repository

This is the **single source of truth** for all AI governance across the ERP platform.

All AI skills, coding standards, architecture rules, context documents, commands, and
execution protocols live here. No other repository may duplicate or redefine them.

> **Other repositories reference this one — they do not own governance.**
> `backend/CLAUDE.md`, `frontend/CLAUDE.md`, and `deploy/CLAUDE.md` contain only
> repository-local documentation and a pointer back to this file.

---

## Workspace Layout

See `WORKSPACE.md` for the full sibling-repository layout, ownership boundaries,
and expected developer workflow.

---

## Shared Governance

Skill routing, execution order, governance rules, and context references are
shared across every AI runtime and defined once in `GOVERNANCE-RULES.md`. Read
it before generating or modifying any code — do not restate its contents here.

---

## Phase Execution Protocol

> Applies when executing a governance phase from the execution plan.
> This section governs HOW to execute — the skill routing table above governs WHAT to use.

### Entry — before writing any code in a phase

1. Read `modules/[MODULE]/execution-state.json`
2. Confirm the requested phase matches `current_phase` in state
3. Read the phase index file: `modules/[MODULE]/packages/execution/[PHASE]/index.md`
4. Identify all subs inside the phase (in order)
5. For each sub, read its file completely before executing it

### Execution — per sub

1. Read sub file completely
2. Identify all tasks in the sub
3. Map each task to the skill routing table above
4. Read required skills from `.github/skills/`
5. Execute all tasks in order
6. Run `validate-backend-feature` or `validate-frontend-feature` after last task
7. Mark sub as COMPLETE in `modules/[MODULE]/execution-state.json`

### Blocked items — OQ / XM DEFERRED

- OQ-blocked task → skip, add to `execution-state.json` blocked list
  Mark in code: `// TODO: OQ-[ID] — pending resolution`
- XM DEFERRED → implement mock strategy
  Mark in code: `// TODO: XM-[MOD]-[N] DEFERRED — replace when READY`
- Continue remaining tasks — never stop the phase for a blocked item

### Exit — after all subs in phase complete

1. Mark phase as COMPLETE in `modules/[MODULE]/execution-state.json`
2. Set `current_phase` to next PENDING phase
3. Print execution report:
   ```
   PHASE [NAME] COMPLETE
   ─────────────────────────────
   Subs executed : [list]
   Blocked       : [OQ-IDs / none]
   XM Deferred   : [XM-IDs / none]
   Next phase    : [name] — awaiting your instruction
   ```

### Constraints (NON-NEGOTIABLE)

- NEVER skip a sub within a phase
- NEVER invent field or column names — always look up db-script.md
- NEVER copy QRC entries as production code — read intent, write implementation
- NEVER implement a blocked OQ item — mark and skip only
- NEVER advance to next phase without explicit instruction from user