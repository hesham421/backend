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

> **v2.1 — check the module's governance model first.** Read
> `governance-tools/config.py`'s `get_governance_model(mod)` (or just
> check `modules-registry.json` for that module's `"governance_model"`
> field) before assuming file paths below. Existing modules (ORG,
> NOTIFICATION, FILESVC) are `"v1"` — paths are exactly as documented
> below (`packages/execution/`, one combined execution-plan.md). Any
> module registered from now on is `"v2"` by default — paths become
> `packages/backend-execution/` (this repo) and
> `packages/frontend-execution/` (frontend repo, natively — not routed
> from here), and the frontend pass additionally requires the UI Shell
> step below (Section "UI Shell Implementation Protocol") to have
> completed BEFORE its own execution-plan phases begin.

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

### UI Shell Implementation Protocol (v2.1, NEW — frontend only, v2-model modules only)

> Applies ONLY between UI/UX mockup approval and the frontend
> execution-plan phases (F1-F4), and ONLY for `"v2"` governance-model
> modules. v1 modules (ORG, NOTIFICATION, FILESVC) do not have this
> step — their F1-F4 phases were designed directly, as originally
> documented above.

1. Read the approved `flow-diagram.md`, `ui-ux-spec.md`, and
   `visual-mockups/` (Project 2.5 outputs) for the module.
2. Implement the UI Shell for real, in the frontend repo:
   - Real Angular components, routing, and layout/styling matching
     the approved mockups **visually**
   - NO real API calls, NO service integration, NO Facade wiring yet
   - Static or dummy data is acceptable for display purposes only
3. Do NOT read or apply any backend skill, execution-plan phase, or
   API contract during this step — this is presentational/structural
   work only, deliberately decoupled from the integration work that
   comes later via F1-F4.
4. Stop and request human review once the Shell is implemented —
   this is a mandatory sign-off (`GATE: UI SHELL COMPLETE`), separate
   from the mockup approval itself. Do not proceed to F1-F4 without it.
5. Once approved, the frontend execution-plan (F1-F4) phases begin —
   see the Entry/Execution/Exit protocol above, applied to
   `frontend-execution-plan.md`'s phases. F1 CONFIRMS the models
   already used in this Shell against the real API Docs (correcting
   mismatches, not inventing new models the Shell doesn't need); F4
   DOCUMENTS the Shell's existing routes/components and adds only
   missing integration wiring (guards, service calls) as explicit,
   flagged additions — neither phase redesigns what this step built.

Full rationale: see `CONTRACT-12` and `RULE-13` in
`shared-artifact-contracts.md` / `shared-governance-rules.md` (loaded
in the governance Claude Projects, not duplicated here).

### Constraints (NON-NEGOTIABLE)

- NEVER skip a sub within a phase
- NEVER invent field or column names — always look up db-script.md
- NEVER copy QRC entries as production code — read intent, write implementation
- NEVER implement a blocked OQ item — mark and skip only
- NEVER advance to next phase without explicit instruction from user
- (v2.1, v2-model modules only) NEVER generate F1-F4 phase content
  before `GATE: UI SHELL COMPLETE` is confirmed — the Shell must exist
  and be human-approved first
- (v2.1) NEVER redesign component/routing structure that already
  exists in an approved UI Shell during F1/F4 — confirm/document it,
  flag genuine gaps as additions, don't silently rewrite it