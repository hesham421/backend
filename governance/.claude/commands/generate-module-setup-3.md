# Generate Module Execution Setup

```
Lives at   : backend/governance/.claude/commands/generate-module-setup.md
             (a copy also lives at frontend/governance/.claude/commands/
             generate-module-setup.md — IDENTICAL content, same reason
             governance-tools/ is duplicated: Claude Code resolves slash
             commands from the CURRENT repo's .claude/commands/ folder,
             so a developer working in either repo needs it locally
             discoverable. Keep both copies in sync by hand.)
Invokes    : backend/governance/governance-tools/agent1_create_structure.py,
             agent2_archive.py, agent3_splitter.py — these Python tools
             live in ONE place only (backend/) and are invoked with
             --track backend|frontend regardless of which repo you're
             actually working in (see WORKSPACE-ARCHITECTURE-REFERENCE.md
             Section 2). This command file does not duplicate that
             tooling logic — it generates the SLASH COMMANDS
             (execute-backend.md / execute-frontend.md, etc.) a
             developer uses afterward to drive implementation.
```

## Your Task

Scan the governance repo for the specified module and track, and
generate three files:
1. `.claude/commands/execute-[track].md` — the slash command for
   implementation phase execution
2. `.claude/commands/execute-[track]-test.md` — the slash command for
   test phase execution, gated on execute-[track].md's phases
3. `execution-state.json` — current state tracker for both (location
   depends on track — see Step 2)

Both generated commands reference `TEST-EXECUTION-AGENT.md` for MCP
boundaries and the failure taxonomy — that file is shared across
modules, not regenerated per module.

---

## Input

```
$ARGUMENTS = MODULE TRACK

MODULE : module code, e.g. FIN, ORG
TRACK  : backend | frontend
```

If `TRACK` is missing or not exactly `backend`/`frontend`, stop and ask
— do not guess which track was intended.

**Precondition gate for `TRACK=frontend`** (check before anything else):
```
╔══════════════════════════════════════════════════════════════════╗
║   FRONTEND TRACK — PRECONDITION GATE                              ║
╠════════════════════════════════════╦═══════════════════════════════╣
║ GATE: BACKEND MODULE COMPLETE      ║ [Yes / No — STOP]              ║
║ confirmed for this module          ║                                ║
║ (real API Docs exist + UI/UX       ║                                ║
║  outputs human-approved + backend  ║                                ║
║  implementation 100% done)         ║                                ║
║ GATE: UI SHELL COMPLETE confirmed  ║ [Yes / No — STOP]              ║
║ frontend-execution-plan.md exists  ║ [Yes / No — STOP, Project 3.2 ║
║ with Gate ALIGN-FE ✓               ║  hasn't run yet]               ║
╚══════════════════════════════════════════════════════════════════╝
```
If any box is "No" or unconfirmed, state plainly which precondition is
missing and stop. `TRACK=backend` has no such precondition — it only
requires `backend-execution-plan.md` to exist with Gate ALIGN-BE ✓
(checked naturally in Step 1's scan).

---

## Step 1 — Scan the governance repo structure

Both repos are siblings — `backend/governance/` and
`frontend/governance/`. Resolve paths relative to wherever this
command's repo root actually is; the examples below assume you're
running from `backend/governance/` (adjust the relative prefix if
running from `frontend/governance/` instead — the target paths are the
same, only the relative `../` prefix flips).

```bash
# TRACK=backend — scan this repo only
find backend/governance/modules/$MODULE/packages/backend-execution -type f -name "*.md" | sort
find backend/governance/modules/$MODULE/packages/backend-test -type f -name "*.md" | sort

# TRACK=frontend — scan the frontend repo (natively generated there,
# never routed from backend — see WORKSPACE-ARCHITECTURE-REFERENCE.md
# Section 6)
find frontend/governance/modules/$MODULE/packages/frontend-execution -type f -name "*.md" | sort
find frontend/governance/modules/$MODULE/packages/frontend-test -type f -name "*.md" | sort
```

From the scan results:
- Identify all PHASES (top-level folders under `packages/[track]-execution/`)
- For each PHASE, identify all SUBs (files inside the phase folder,
  excluding `index.md`)
- Preserve the exact order from the filesystem sort
- For each SUB file, read the first 40 lines and count the number of tasks

Expected phases, in strict execution order (only include phases that
actually exist in the scan):
```
TRACK=backend  : CORE → DATA-DOM → SVC-API → DOC → INT-C → INT-R → SEC-BE → ALIGN-BE
TRACK=frontend : F1 → F2 → F3 → F4 → SEC-FE → ALIGN-FE
```

### Test phase (single phase per track — no MARK-level split)

`packages/[track]-test/` is the tool boundary itself — `backend-test`
is JUnit-only, `frontend-test` is Playwright-only, by construction (see
`PROJECT-3-REGISTRY.md` Section 5.7.4). Treat it as ONE TEST-PHASE:
- SUBs = every `.md` file inside, excluding `index.md`, `.gitkeep`, any
  `*-HEADER.md`, and any `MANDATORY-*.md`
  (backend: `RULE-SCENARIOS`, `API-SCENARIOS` — frontend: `UI-FLOWS`, `INT-FLOW`)
- `*-HEADER.md` and `MANDATORY-*.md` are NOT subs — shared context read
  once before generating any sub in that phase. Record their paths for
  `execute-[track]-test.md` to read.
- Gated by ALL phases that exist for this module in that track:
  ```
  backend-test  gated by → CORE, DATA-DOM, SVC-API, DOC, INT-C, INT-R, SEC-BE, ALIGN-BE
  frontend-test gated by → F1, F2, F3, F4, SEC-FE, ALIGN-FE
  (only the phases that actually exist for this module)
  ```

### Weight classification (record for Weight Map in execute-[track].md)

| Weight | Criteria |
|--------|----------|
| LIGHT  | < 5 tasks, single layer/component |
| MEDIUM | 5–10 tasks, 1–2 layers/components |
| HEAVY  | > 10 tasks, multi-layer (backend: Entity+Repo+Service+Controller — frontend: Models+Hooks+Validation+Routing) |
| XL     | Full backend feature OR 3+ frontend screens in one sub |

Record the weight and estimated task count for every sub found.

---

## Step 2 — Generate `execution-state.json`

```
TRACK=backend  → backend/governance/modules/$MODULE/execution-state.json
TRACK=frontend → frontend/governance/modules/$MODULE/execution-state.json
                 (a DIFFERENT file from backend's — never merged, never
                 synced automatically — see WORKSPACE-ARCHITECTURE-
                 REFERENCE.md Section 3)
```

### Rules
- `module` = the module name from `$MODULE`
- `track` = `backend` or `frontend`
- `current_phase` = first phase found in scan (PENDING or IN_PROGRESS)
- `current_sub` = first sub of the first phase (null if phase has no subs)
- All phases and subs start `PENDING`
- If a phase has only `index.md` and no sub files → `"subs": []`
- `blocked`, `deferred_xm`, and `api_doc_gaps` start as empty arrays
  (`deferred_xm` is meaningful for `TRACK=backend` only — XM-IDs are
  exclusively a backend concern; leave it `[]` and unused for frontend)
- `test_phase` (singular — one entry, not an array) follows the same
  shape as a phase entry, plus `gated_by_phases`, `header_file`, `mandatory_file`

### Format — TRACK=backend
```json
{
  "module": "[MODULE]",
  "track": "backend",
  "generated_at": "[today's date]",
  "current_phase": "[FIRST_PHASE]",
  "current_sub": "[FIRST_SUB or null]",
  "api_docs_path": "governance/modules/[MODULE]/api-docs/",
  "phases": [
    {
      "id": "[PHASE_NAME]",
      "status": "PENDING",
      "subs": [
        { "id": "[SUB_NAME]", "status": "PENDING" }
      ]
    }
  ],
  "test_phase": {
    "id": "backend-test",
    "status": "PENDING",
    "gated_by_phases": ["CORE", "DATA-DOM", "SVC-API", "DOC", "INT-C", "INT-R", "SEC-BE", "ALIGN-BE"],
    "header_file": "packages/backend-test/RULE-SCENARIOS-HEADER.md",
    "mandatory_file": "packages/backend-test/MANDATORY-J.md",
    "subs": [
      { "id": "RULE-SCENARIOS", "status": "PENDING" },
      { "id": "API-SCENARIOS", "status": "PENDING" }
    ]
  },
  "blocked": [],
  "deferred_xm": [],
  "api_doc_gaps": []
}
```

### Format — TRACK=frontend
```json
{
  "module": "[MODULE]",
  "track": "frontend",
  "generated_at": "[today's date]",
  "backend_module_complete_confirmed": true,
  "ui_shell_complete_confirmed": true,
  "current_phase": "[FIRST_PHASE]",
  "current_sub": "[FIRST_SUB or null]",
  "api_docs_path": "../backend/governance/modules/[MODULE]/api-docs/",
  "phases": [
    {
      "id": "[PHASE_NAME]",
      "status": "PENDING",
      "subs": [
        { "id": "[SUB_NAME]", "status": "PENDING" }
      ]
    }
  ],
  "test_phase": {
    "id": "frontend-test",
    "status": "PENDING",
    "gated_by_phases": ["F1", "F2", "F3", "F4", "SEC-FE", "ALIGN-FE"],
    "header_file": "packages/frontend-test/UI-FLOWS-HEADER.md",
    "mandatory_file": "packages/frontend-test/MANDATORY-P.md",
    "subs": [
      { "id": "UI-FLOWS", "status": "PENDING" },
      { "id": "INT-FLOW", "status": "PENDING" }
    ]
  },
  "blocked": [],
  "api_doc_gaps": []
}
```

The two `*_confirmed` booleans in the frontend format record that this
command's own precondition gate (top of this file) actually passed —
a record, not a cache to trust blindly on a much later re-read.

### `test_phase.gated_by_phases` rule
List only phases that actually exist in this module's `phases[]`. An
empty list means "always gated open" — nothing to wait for.

### `api_doc_gaps[]` entry format (populated during execution, when a
needed detail is not in api-docs at all — see execute-[track].md STEP 1.5):
```json
{
  "type": "MISSING_IN_DOCS",
  "phase": "[PHASE]",
  "sub": "[SUB]",
  "endpoint": "[METHOD] [path]",
  "detail": "[what was missing from api-docs]",
  "resolution": "resolved via backend source: <controller/dto/service path>",
  "recorded_at": "[timestamp]"
}
```

---

## Step 3 — Generate `.claude/commands/execute-[track].md`

Create at: `.claude/commands/execute-[track].md`
(e.g. `execute-backend.md` or `execute-frontend.md`)

The file must reference the exact phase names and sub names discovered
in Step 1, with each sub's estimated weight from the scan.

```markdown
# /project:execute-[track]

Execute the current phase for the specified module — with context safety check.

## Usage
/project:execute-[track] [MODULE] [PHASE]

---

## STEP 0 — Context Safety Assessment (MANDATORY before any execution)

Before writing a single line of code, assess the execution load.

### 0.1 — Read state and identify scope
Read `execution-state.json` (this repo's copy — see Step 2 above).
Identify all PENDING subs in the requested phase.

### 0.2 — Look up sub weights from the Weight Map below
Each sub's weight was estimated at setup time from task count.

### 0.3 — Classify total and decide chunking
| Total weight in phase | Action |
|---|---|
| All LIGHT/MEDIUM | Execute the whole phase in one pass |
| Any HEAVY present | Chunk — one sub (or a few LIGHT subs) per pass |
| Any XL present | That sub alone is one full pass — never combine with another sub |

### 0.4 — Print assessment and wait for confirmation
```
══════════════════════════════════════════════════════
PHASE ASSESSMENT — [MODULE] / [PHASE]  (track: [track])
══════════════════════════════════════════════════════
Subs pending : [list, with weight + task count each]
Plan         : [one pass / chunked into N passes — list the chunks]
══════════════════════════════════════════════════════
Proceed with [first chunk description]? [waits for user confirmation]
```

---

## STEP 1 — Execution (after confirmation)

### Per sub:
1. Read the sub file completely:
   `packages/[track]-execution/[PHASE]/[SUB].md`
2. Identify all tasks in the sub
[TRACK=frontend only:]
2.5. **UI Shell reference check** — before writing any task's code,
     confirm whether a corresponding component/route already exists in
     the UI Shell. If it exists: this phase CONFIRMS/INTEGRATES with
     it — modify the existing file, never create a competing new one.
     If genuinely absent: flag it in the session report as a Shell gap,
     implement it as an explicit addition.
3. **API Contract Resolution** [TRACK=frontend, phases F1/F2/F3 only]:
   check `api_docs_path` first (STEP 1.5.1) — treat it as trusted.
   Only fall back to backend source if the detail is confirmed absent
   from api-docs (STEP 1.5.2) — and log that fallback in `api_doc_gaps[]`.
4. Map each task to the skill routing table in `GOVERNANCE-RULES.md`
5. Read required skills from `.github/skills/[track]/`
6. Execute all tasks in order
7. Run the phase's validation skill after the last task
8. Mark sub COMPLETE in `execution-state.json`

### Blocked items — OQ
OQ-blocked task → skip, add to `blocked[]`.
Mark in code: `// TODO: OQ-[ID] — pending resolution`
Continue remaining tasks — never stop the phase for a blocked item.

[TRACK=frontend only:]
Never write an XM-related TODO in frontend code — XM-IDs are
exclusively a backend concern (CORE-5 RULE-6). If a task seems to need
one, stop and flag it instead of implementing it.

---

## STEP 2 — Session Report

Print the same-shaped execution report used throughout this ecosystem
(phase/sub completed, tasks executed, any blocked items, any
api_doc_gaps entries added).

---

## Weight Map — [MODULE] (track: [track])

[Insert the actual weight map discovered in Step 1]

| Phase    | Sub              | Weight | Est. Tasks |
|----------|-----------------|--------|------------|
| ...      | ...             | ...    | ...        |

---

## Phase Map — [MODULE] (track: [track])

[Insert the actual phase → subs map discovered in Step 1]

---

## Constraints (NON-NEGOTIABLE)

- NEVER skip STEP 0 — assessment is mandatory before every execution
- NEVER execute without user confirmation after assessment
- NEVER skip a sub within the planned chunk
- NEVER invent field, column, route, or component names — always look
  up the source artifact (db-script.md for backend, F1-F4 blocks for frontend)
- NEVER implement a blocked OQ item — mark and skip only
- NEVER advance to next phase without explicit instruction from user
- ALWAYS update execution-state.json after every sub
- [TRACK=frontend] NEVER redesign a component/route that already
  exists in the UI Shell — confirm/integrate per STEP 1's check
- [TRACK=frontend] NEVER go to backend source for an API contract
  detail unless confirmed absent from api-docs — always log the
  fallback in api_doc_gaps[]
```

---

## Step 3B — Generate `.claude/commands/execute-[track]-test.md`

Create at: `.claude/commands/execute-[track]-test.md`

Mirrors `execute-[track].md`'s structure (STEP 0 → STEP 1 → STEP 2),
applied to `test_phase` instead of `phases`. Reference
`TEST-EXECUTION-AGENT.md` for MCP boundaries and the failure
taxonomy — do not redefine them here.

```markdown
# /project:execute-[track]-test

Execute test scenarios for [MODULE] — only for what implementation has
actually completed.

> Read `TEST-EXECUTION-AGENT.md` first — MCP boundaries and
> failure taxonomy used below come from there.

## Usage
/project:execute-[track]-test [MODULE]

---

## STEP 0 — Gate Check + Context Safety Assessment

### 0.1 — Gate Check (MANDATORY, before anything else)
Read `execution-state.json` → `test_phase.gated_by_phases[]`.
Confirm every listed phase has `phases[].status == COMPLETE`.
If `gated_by_phases` is empty → gate passes automatically.

If any gating phase is NOT complete:
```
══════════════════════════════════════════════════════
⛔ TEST GATE FAILED — [MODULE] (track: [track])
══════════════════════════════════════════════════════
Waiting on : [PHASE-NAME: status], ...
══════════════════════════════════════════════════════
```
STOP here. Do not proceed, do not generate or run any test.

### 0.2 — 0.5 — Same assessment/confirmation pattern as execute-[track].md

---

## STEP 1 — Execution (after confirmation)

### 1.0 — Read shared context once
Read `header_file` and `mandatory_file` from `test_phase` before the
first sub.

### Per sub:
1. Read `packages/[track]-test/[SUB].md` completely
2. Identify all scenarios in it
3. Generate test code:
   - **backend**  → Spring Boot test class (`@SpringBootTest`/`@WebMvcTest`
     + `MockMvc`), file `src/test/java/.../[Scenario]Test.java`
   - **frontend** → POM + spec file, per TEST-EXECUTION-AGENT.md's
     conventions (Page Object Model, `data-testid` first, no `waitForTimeout`)
4. Run:
   - **backend**  → `mvn test -Dtest=[Class]` via bash. `oracle-sql`
     MCP (read-only) for any DB assertion.
   - **frontend** → `playwright-mcp`, per the shared MCP execution
     order (oracle-sql precondition → playwright-mcp execute →
     oracle-sql confirm → screenshot on failure)
5. Classify every failure/skip using the shared taxonomy
6. Update `execution-state.json`: mark sub COMPLETE/FAILED, advance to
   next PENDING sub, mark `test_phase` COMPLETE when all subs are done

---

## STEP 2 — Session Report

Write to `reports/TEST-REPORT-[MODULE]-[track]-[YYYY-MM-DD].md`, same
shape as TEST-EXECUTION-AGENT.md's template. Any `FAIL` → hand off to
`AUTONOMOUS-FULLSTACK-FIXING-AGENT.md` — never fix here.

---

## Constraints (NON-NEGOTIABLE)

- NEVER run before the gate check (STEP 0.1) passes
- NEVER treat `*-HEADER.md`/`MANDATORY-*.md` as a sub
- NEVER skip MANDATORY scenarios
- NEVER modify application source code — report failures, don't fix them
- NEVER run mutating SQL via oracle-sql — read-only, always
- ALWAYS classify every failure/skip with a taxonomy code
- ALWAYS update execution-state.json after every sub
```

---

## Step 4 — Verify and report

```
══════════════════════════════════════════════════════
MODULE SETUP COMPLETE: [MODULE]  (track: [track])
══════════════════════════════════════════════════════
execution-state.json     ✓  [repo]/governance/modules/[MODULE]/
execute-[track].md       ✓  .claude/commands/
execute-[track]-test.md  ✓  .claude/commands/

Phases detected       : [count]
Total subs detected   : [count]
[TRACK=frontend] API contract resolution wired (STEP 1.5) : ✓

Test phase detected   : [track]-test [✓ / not found]
  gated by : [phases found]

Weight map:
  [PHASE] / [SUB]  → [LIGHT/MEDIUM/HEAVY/XL]  ([N] tasks)
  ...

Heavy phases (require chunking):
  [PHASE] → [reason]   (or "none — all phases safe")

To start execution:
  /project:execute-[track] [MODULE] [FIRST_PHASE]

To run tests once implementation phases are COMPLETE:
  /project:execute-[track]-test [MODULE]
══════════════════════════════════════════════════════
```

---

## Constraints (this command itself — NON-NEGOTIABLE)

- NEVER run without both MODULE and TRACK specified
- NEVER run TRACK=frontend without the precondition gate (top of this
  file) passing — all three checks are mandatory, not advisory
- NEVER scan or write across repos — TRACK=backend stays entirely in
  `backend/governance/`, TRACK=frontend stays entirely in
  `frontend/governance/` (except the two sanctioned reads:
  `api_docs_path` and, if needed, `modules-registry.json` — see
  WORKSPACE-ARCHITECTURE-REFERENCE.md Section 5)
- NEVER invent a phase, sub, or file path not found in Step 1's actual scan
