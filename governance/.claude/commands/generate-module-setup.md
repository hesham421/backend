# Generate Module Execution Setup

## Your Task

Scan the governance repo for the specified module and generate four files:
1. `.claude/commands/execute.md` — the slash command for implementation phase execution (CORE..INT-R, SEC, ALIGN backend-side; F1-F4 frontend-side)
2. `.claude/commands/execute-test.md` — the slash command for test phase execution (JUNIT / PLAYWRIGHT), gated on execute.md's phases
3. `governance/modules/[MODULE]/execution-state.json` — state tracker for backend-owned phases (CORE..INT-R, SEC, ALIGN) and JUNIT
4. `../frontend/governance/modules/[MODULE]/execution-state.json` — state tracker for frontend-owned phases (F1-F4) and PLAYWRIGHT, plus a read-only `align_status` mirrored from file #3

Per STRUCTURAL LAW (see `backend/CLAUDE.md`/`frontend/CLAUDE.md`), F1-F4 execution-phase content and PLAYWRIGHT test-phase content both live in `frontend/governance/`, never `backend/governance/` — everything else stays here. This applies to both the split package files AND `execution-state.json` itself, which is why there are now two state files instead of one.

Both generated commands reference `TEST-EXECUTION-AGENT.md`
for MCP boundaries and the failure taxonomy — that file is shared across
modules, not regenerated per module.

---

## Input

Module name: **$ARGUMENTS**

Governance repo path: `governance/modules/$ARGUMENTS/`

---

## Step 1 — Scan the governance repo structure

Run the following and capture the output. THREE separate scans are required
— F1-F4 execution phases and PLAYWRIGHT test content both live in
frontend/governance/ post-split (STRUCTURAL LAW: `backend/governance/` owns
CORE..INT-R/SEC/ALIGN + JUNIT; `frontend/governance/` owns F1-F4 +
PLAYWRIGHT only) — scans rooted in this repo will NEVER find either, even
when they genuinely exist:

```bash
# CORE..INT-R/SEC/ALIGN execution phases + JUNIT test scenarios — this repo (backend/governance/)
find governance/modules/$ARGUMENTS/packages/execution -maxdepth 1 -mindepth 1 -type d ! \( -name "F1" -o -name "F2" -o -name "F3" -o -name "F4" \) | sort
find governance/modules/$ARGUMENTS/packages/execution -type f -name "*.md" | grep -vE "/F[1-4]/" | sort
find governance/modules/$ARGUMENTS/packages/test/JUNIT -type f -name "*.md" | sort

# F1-F4 execution phases — sibling repo (frontend/governance/), NOT this repo
find ../frontend/governance/modules/$ARGUMENTS/packages/execution/F1 -type f -name "*.md" 2>/dev/null | sort
find ../frontend/governance/modules/$ARGUMENTS/packages/execution/F2 -type f -name "*.md" 2>/dev/null | sort
find ../frontend/governance/modules/$ARGUMENTS/packages/execution/F3 -type f -name "*.md" 2>/dev/null | sort
find ../frontend/governance/modules/$ARGUMENTS/packages/execution/F4 -type f -name "*.md" 2>/dev/null | sort

# PLAYWRIGHT test scenarios — sibling repo (frontend/governance/), NOT this repo
find ../frontend/governance/modules/$ARGUMENTS/packages/test/PLAYWRIGHT -type f -name "*.md" | sort
```

If a `../frontend/governance/modules/$ARGUMENTS/...` scan finds nothing,
that's a real "no F1-F4 / PLAYWRIGHT content for this module yet" result —
but only if you actually ran that scan. Never infer "no F1-F4" or "no
PLAYWRIGHT" from the backend-rooted scans alone; they cannot see either one
either way.

From the scan results:
- Identify all PHASES (top-level folders under `packages/execution/` in
  EITHER repo — CORE..INT-R/SEC/ALIGN here, F1-F4 in frontend/governance/)
- For each PHASE, identify all SUBs (files inside the phase folder, excluding `index.md`)
- Preserve the exact order from the filesystem sort
- For each SUB file, read the first 40 lines and count the number of tasks

Expected phases (in strict execution order):
`CORE → DATA-DOM → SVC-API → DOC → INT-C → INT-R → F1 → F2 → F3 → SEC → ALIGN`
(F1-F4 physically live in `frontend/governance/`, but still execute in this
same overall sequence — the phase ORDER is one continuous pipeline even
though the FILES that implement it are split across two repos.)

Only include phases that actually exist in the scanned structure.

### Test phases (packages/test/) — same pattern, separate namespace, two repos

`packages/test/` has exactly two possible top-level folders: `JUNIT` (this
repo, backend/governance/) and `PLAYWRIGHT` (sibling repo,
frontend/governance/ — see the two-scan note above). Treat each as a
TEST-PHASE the same way an execution folder is treated as a PHASE:
- SUBs = every `.md` file inside, excluding `index.md`, `.gitkeep`, any
  `*-HEADER.md`, and any `MANDATORY-*.md`
  (e.g. JUNIT → `API-SCENARIOS`, `RULE-SCENARIOS`; PLAYWRIGHT → `INT-FLOW`, `UI-FLOWS`)
- `*-HEADER.md` and `MANDATORY-*.md` are NOT subs — they're shared context
  read once before generating any sub in that phase (conventions + the
  mandatory scenario list). Record their paths for execute-test.md to read.
- Only include a TEST-PHASE (JUNIT / PLAYWRIGHT) if its folder actually
  exists in the scan.

Each TEST-PHASE is gated by a fixed set of EXECUTION phases (only the ones
that actually exist for this module are used):
```
JUNIT      gated by → CORE, DATA-DOM, SVC-API, DOC, INT-C, INT-R
PLAYWRIGHT gated by → F1, F2, F3
```

### Weight classification (record for Weight Map in execute.md):

| Weight | Criteria |
|--------|----------|
| LIGHT  | < 5 tasks, single layer |
| MEDIUM | 5–10 tasks, 1–2 layers |
| HEAVY  | > 10 tasks, multi-layer (Entity+Repo+Service+Controller) |
| XL     | Full backend feature OR 3+ frontend screens in one sub |

Record the weight and estimated task count for every sub found.

---

## Step 2 — Generate TWO `execution-state.json` files

Per STRUCTURAL LAW, backend-owned phases (CORE..INT-R, SEC, ALIGN) and
JUNIT get their own state file in this repo; frontend-owned phases (F1-F4)
and PLAYWRIGHT get a SEPARATE state file in `frontend/governance/`. This
mirrors the same principle already applied to the package files themselves
— it is not optional, and it is not a reference copy of one shared file;
each file owns disjoint fields.

### Shared rules (both files)
- `module` = the module name from $ARGUMENTS
- `current_phase` = first non-`COMPLETE` phase in THAT FILE's own `phases[]`
  (each file only ever names phases it owns); if every phase in the file is
  `COMPLETE`, use the literal string `"COMPLETE"` with `current_sub: null`
  (this is an existing convention already used elsewhere in this pipeline,
  not a new invention)
- `current_sub` = first sub of `current_phase` (null if that phase has no subs)
- All phases/subs start as `PENDING`
- If a phase has only `index.md` and no sub files → `"subs": []`
- `blocked`, `deferred_xm`, and `api_doc_gaps` entries route by their own
  `phase` field: an entry whose `phase` names an F1-F4 phase goes in the
  frontend file; everything else (including entries with no `phase` field
  at all) goes in the backend file
- `test_phases` follows the exact same shape as `phases` — JUNIT lives in
  the backend file, PLAYWRIGHT lives in the frontend file, each with its own
  `gated_by_phases[]` / `header_file` / `mandatory_file`

### Step 2a — Backend file
Create the file at:
`governance/modules/$ARGUMENTS/execution-state.json`

```json
{
  "module": "[MODULE]",
  "generated_at": "[today's date]",
  "current_phase": "[FIRST_BACKEND_PHASE]",
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
  "test_phases": [
    {
      "id": "JUNIT",
      "status": "PENDING",
      "gated_by_phases": ["CORE", "DATA-DOM", "SVC-API", "DOC", "INT-C", "INT-R"],
      "header_file": "packages/test/JUNIT/JUNIT-HEADER.md",
      "mandatory_file": "packages/test/JUNIT/MANDATORY-J.md",
      "subs": [
        { "id": "API-SCENARIOS", "status": "PENDING" },
        { "id": "RULE-SCENARIOS", "status": "PENDING" }
      ]
    }
  ],
  "blocked": [],
  "deferred_xm": [],
  "api_doc_gaps": []
}
```
`phases[]` here only ever contains CORE, DATA-DOM, SVC-API, DOC, INT-C,
INT-R, SEC, ALIGN — never F1-F4.

### Step 2b — Frontend file
Create the file at:
`../frontend/governance/modules/$ARGUMENTS/execution-state.json`
(a NEW file — this repo currently has none)

```json
{
  "module": "[MODULE]",
  "generated_at": "[today's date, same as the backend file]",
  "current_phase": "[FIRST_FRONTEND_PHASE, e.g. F1 — or \"COMPLETE\" if this module has no F1-F4 at all]",
  "current_sub": "[FIRST_SUB or null]",
  "api_docs_path": "../backend/governance/modules/[MODULE]/api-docs/",
  "align_status": "PENDING",
  "phases": [
    {
      "id": "F1",
      "status": "PENDING",
      "subs": [
        { "id": "[SUB_NAME]", "status": "PENDING" }
      ]
    }
  ],
  "test_phases": [
    {
      "id": "PLAYWRIGHT",
      "status": "PENDING",
      "gated_by_phases": ["F1", "F2", "F3"],
      "header_file": "packages/test/PLAYWRIGHT/PLAYWRIGHT-HEADER.md",
      "mandatory_file": "packages/test/PLAYWRIGHT/MANDATORY-P.md",
      "subs": [
        { "id": "INT-FLOW", "status": "PENDING" },
        { "id": "UI-FLOWS", "status": "PENDING" }
      ]
    }
  ],
  "blocked": [],
  "deferred_xm": [],
  "api_doc_gaps": []
}
```
`phases[]` here only ever contains F1, F2, F3, F4 (whichever this module
actually has — some modules have no F4). PLAYWRIGHT's `header_file` /
`mandatory_file` are relative to THIS file's own repo root (no
`../frontend/...` prefix needed here — they already live in this repo).

**`align_status`** is a MIRRORED, READ-ONLY field — copied from the backend
file's `ALIGN` phase status at generation time (`"PENDING"` for a brand-new
module, since ALIGN hasn't run yet). It exists so a frontend-only agent can
confirm ALIGN's gate result without opening `backend/`. Comment this
explicitly wherever it's set: **"mirrored from backend — do not hand-edit
here, source of truth is backend/governance/modules/[MODULE]/execution-state.json"**.
Whichever agent updates the backend file's ALIGN status must also update
this mirrored copy in the same session — it does not update itself.

### `test_phases[].gated_by_phases` rule
Only list phases that actually exist in this module's OWN `phases[]` (e.g. a
backend-only module has no F1/F2/F3, so PLAYWRIGHT would have an empty
`gated_by_phases: []` — treat an empty list as "always gated open", i.e.
nothing to wait for, since there's no frontend to test).

### `api_doc_gaps[]` entry format (populated only during F1/F2/F3 execution —
see execute.md STEP 1.5 — when a needed detail is not in api-docs at all).
Written into whichever file owns the phase named:
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

## Step 3 — Generate `.claude/commands/execute.md`

Create the file at:
`.claude/commands/execute.md`

### Content to generate:

The file must reference the exact phase names and sub names discovered in Step 1.
For each sub, record its estimated weight based on task count seen during scan:
- LIGHT  = < 5 tasks
- MEDIUM = 5–10 tasks
- HEAVY  = > 10 tasks or multi-layer (backend Entity+Repo+Service+Controller)
- XL     = full backend feature OR 3+ frontend screens in one sub

Generate the file with this structure:

```markdown
# /project:execute

Execute the current phase for the specified module — with context safety check.

## Usage
/project:execute [MODULE] [PHASE]

---

## STEP 0 — Context Safety Assessment (MANDATORY before any execution)

Before writing a single line of code, assess the execution load.

### 0.1 — Read state and identify scope

Path depends on the requested PHASE — backend and frontend phases now have
SEPARATE state files (STRUCTURAL LAW):
- **CORE, DATA-DOM, SVC-API, DOC, INT-C, INT-R, SEC, ALIGN** → read
  `governance/modules/[MODULE]/execution-state.json` (this repo)
- **F1, F2, F3, F4** → read
  `../frontend/governance/modules/[MODULE]/execution-state.json` (sibling repo)

Identify all PENDING subs in the requested phase, from whichever file owns it.

### 0.2 — Look up sub weights from the Weight Map below

Each sub's weight was estimated at setup time from task count.
Use the Weight Map to classify the phase total without re-reading files.

### 0.3 — Classify phase total and decide chunking

| Phase Total                | Decision |
|----------------------------|----------|
| 1–2 LIGHT subs             | ✅ SAFE — execute all in one session |
| 1 MEDIUM sub               | ✅ SAFE — execute in one session |
| 2+ MEDIUM subs             | ⚠ CHUNK — one sub per session |
| Any HEAVY sub              | ⚠ CHUNK — each HEAVY sub = one session |
| Any XL sub                 | ⚠ CHUNK — split XL into layers across sessions |

### 0.4 — Print assessment and wait for confirmation

```
══════════════════════════════════════════════════════
EXECUTION ASSESSMENT: [PHASE] — module: [MODULE]
══════════════════════════════════════════════════════
Pending subs  : [list from state]
Total weight  : [SAFE / CHUNK RECOMMENDED]

Sub weights:
  [SUB-ID] → [LIGHT/MEDIUM/HEAVY/XL]
  [SUB-ID] → [LIGHT/MEDIUM/HEAVY/XL]

Execution plan:
  [If SAFE]
  → All pending subs in one session ✅
  → Proceed? (confirm to start)

  [If CHUNK]
  → Session 1: [SUB-IDs]
  → Session 2: [SUB-IDs]
  → ...
  → Proceed with Session 1? (confirm to start)
══════════════════════════════════════════════════════
```

WAIT for user confirmation. Do not execute before confirmation.

---

## STEP 1 — Execution (after confirmation)

### Per sub:

1. Read sub file completely:
   - **CORE, DATA-DOM, SVC-API, DOC, INT-C, INT-R, SEC, ALIGN** →
     `governance/modules/[MODULE]/packages/execution/[PHASE]/[SUB].md`
     (this repo; if no sub → `[PHASE]/index.md`)
   - **F1, F2, F3, F4** →
     `../frontend/governance/modules/[MODULE]/packages/execution/[PHASE]/[SUB].md`
     (sibling repo; if no sub → `[PHASE]/index.md`)
2. Identify all tasks in the sub
2.5. **[Frontend phases F1/F2/F3 only] API Contract Resolution** — see STEP 1.5
   below. Resolve every endpoint contract this sub's tasks depend on BEFORE
   writing implementation code for those tasks.
3. Map each task to skill routing table in CLAUDE.md
4. Read required skills from `.github/skills/`
5. Execute all tasks in strict order
6. After last task → run `validate-backend-feature` or `validate-frontend-feature`
7. Update `execution-state.json` — the SAME file read in STEP 0.1 for this
   phase (backend file for CORE..INT-R/SEC/ALIGN, frontend file for F1-F4):
   - Mark sub as COMPLETE
   - Set `current_sub` to next PENDING sub in same phase
   - If no more subs → mark phase COMPLETE, set `current_phase` to next PENDING phase
     in THAT FILE's own phases[] (or `"COMPLETE"` if this was the last phase this
     file owns — the OTHER file's own current_phase is not touched by this step)
   - **If the phase just completed was ALIGN**: also update the mirrored
     `align_status` field in `../frontend/governance/modules/[MODULE]/execution-state.json`
     to match — this is the ONE field ALIGN's completion writes into the
     other repo's file. Never update any other field in the other file.

### Blocked items — OQ / XM DEFERRED:
- OQ-blocked → skip, add to `blocked[]` in state
  Write in code: `// TODO: OQ-[ID] — pending resolution`
- XM DEFERRED → implement mock, add to `deferred_xm[]` in state
  Write in code: `// TODO: XM-[MOD]-[N] DEFERRED — replace when READY`
- Never stop the session for a blocked item — continue remaining tasks

---

## STEP 1.5 — API Contract Resolution (frontend phases F1/F2/F3 only)

This step runs for every task in F1/F2/F3 that calls or models a backend
endpoint, before that task's code is written. It does NOT apply to
CORE/DATA-DOM/SVC-API/DOC/INT-C/INT-R/SEC/ALIGN — those phases either
implement the backend itself or don't touch API contracts directly.

**Priority order for API contract facts (path, method, request/response
fields, types, required/optional, validation, enums, headers, error codes,
permissions):**

```
1st — api-docs/endpoints/<group>/<slug>.md   (real backend, generated)
2nd — backend source directly                (ONLY if api-docs doesn't have it)
      (controller / DTO / service under the module's backend source root)
3rd — never invent a field, type, path, or endpoint name, under any
      circumstance, regardless of what execution-plan.md implies
```

execution-plan.md (B2 APIs / INT Summary) tells you WHICH endpoint a task
calls and WHY (the flow/logic). api-docs tells you WHAT the endpoint's real
contract is. Both are needed — neither replaces the other.

### 1.5.1 — Resolve from api-docs (default and primary path)
1. From the task, identify the endpoint (method + path, or operationId)
   referenced in execution-plan.md.
2. Open the matching `api-docs/endpoints/<group>/<slug>.md`.
3. Extract the fields/contract details the task needs.
4. Implement the task against these values directly — api-docs is trusted
   as-is, no comparison against execution-plan.md and no conflict check
   here (that's covered separately by your own review process).

### 1.5.2 — Detail missing from api-docs entirely (fallback to backend)
Only when api-docs genuinely has no answer for what the task needs — matches
the tool's documented best-effort gaps (per-endpoint error responses,
undiscoverable enum/operator values, best-effort permissions, etc.) — go
directly to the backend source:
1. Locate the controller/DTO/service in the module's backend source tree.
2. Read only what's needed to resolve the specific missing detail — do not
   re-derive the whole contract from source when api-docs already covers it.
3. Implement against what you found.
4. Record it:
   ```
   // TODO: api_doc_gap MISSING_IN_DOCS — [detail] not in api-docs,
   // resolved from backend source: [path]
   ```
5. Add an entry to `api_doc_gaps[]`:
   `{ "type": "MISSING_IN_DOCS", "phase", "sub", "endpoint", "detail", "resolution": "resolved via backend source: <path>" }`
6. Continue execution — do not stop the session for this.

**Never skip straight to backend source as a shortcut** — 1.5.1 (api-docs)
is always tried first; 1.5.2 is a documented exception path, not a default.

---

## STEP 2 — Session completion report

```
══════════════════════════════════════════════════════
SESSION COMPLETE
══════════════════════════════════════════════════════
Phase         : [PHASE]
Session       : [1 of N / final]
Subs executed : [list]
Blocked       : [OQ-IDs / none]
XM Deferred   : [XM-IDs / none]
API Doc Gaps  : [count of MISSING_IN_DOCS entries / none]
──────────────────────────────────────────────────────
[If more chunks remain:]
  Remaining   : [SUB-IDs]
  Next command: /project:execute [MODULE] [PHASE]
  (resumes from [next SUB-ID] automatically)

[If phase complete:]
  Phase status: ✓ COMPLETE
  Next phase  : [PHASE-NAME] — awaiting your instruction
══════════════════════════════════════════════════════
```

---

## Weight Map — [MODULE]

[Insert the actual weight map discovered in Step 1. Format:]

| Phase    | Sub              | Weight | Est. Tasks |
|----------|-----------------|--------|------------|
| CORE     | CORE            | [W]    | [N]        |
| DATA-DOM | DATA-DOM-MASTER | [W]    | [N]        |
| ...      | ...             | ...    | ...        |

---

## Phase Map — [MODULE]

[Insert the actual phase → subs map discovered in Step 1. Format:]

```
CORE        → CORE
DATA-DOM    → DATA-DOM-MASTER, DATA-DOM-REFERENCE
SVC-API     → SVC-API-CRUD, SVC-API-INT
...
```

---

## Constraints (NON-NEGOTIABLE)

- NEVER skip STEP 0 — assessment is mandatory before every execution
- NEVER execute without user confirmation after assessment
- NEVER skip a sub within the planned chunk
- NEVER invent field or column names — always look up db-script.md
- NEVER copy QRC entries as production code — read intent, write implementation
- NEVER implement a blocked OQ item — mark and skip only
- NEVER advance to next phase without explicit instruction from user
- ALWAYS update execution-state.json after every sub
- NEVER implement a frontend task that calls a backend endpoint without
  first checking api-docs (STEP 1.5.1) — treat it as trusted, no
  cross-check against execution-plan.md needed
- NEVER go directly to backend source for a frontend task's API contract
  unless the detail is confirmed absent from api-docs (STEP 1.5.2) —
  and always log that fallback in api_doc_gaps[]
```

---

## Step 3B — Generate `.claude/commands/execute-test.md`

Create the file at:
`.claude/commands/execute-test.md`

This mirrors `execute.md`'s structure exactly (same STEP 0 → STEP 1 → STEP 2
shape), applied to `test_phases` instead of `phases`. Reference
`TEST-EXECUTION-AGENT.md` for MCP boundaries and the failure
taxonomy — do not redefine them here.

Generate the file with this structure:

```markdown
# /project:execute-test

Execute test scenarios for a TEST-PHASE (JUNIT or PLAYWRIGHT) — only for
what the implementation has actually completed.

> Read `TEST-EXECUTION-AGENT.md` first — MCP boundaries and
> failure taxonomy used below come from there.

## Usage
/project:execute-test [MODULE] [JUNIT|PLAYWRIGHT]

---

## STEP 0 — Gate Check + Context Safety Assessment

### 0.1 — Gate Check (MANDATORY, before anything else)

`test_phases[]` entries now live in different files per STRUCTURAL LAW —
read the one that owns the requested TEST-PHASE:
- **JUNIT** → `governance/modules/[MODULE]/execution-state.json` (this repo)
- **PLAYWRIGHT** → `../frontend/governance/modules/[MODULE]/execution-state.json` (sibling repo)

Find the `test_phases[]` entry matching `[JUNIT|PLAYWRIGHT]` in that file.
For every phase id in its `gated_by_phases[]`, confirm `phases[].status ==
COMPLETE` — but look up each gating phase's status in WHICHEVER file owns
that phase name, not necessarily the same file you just read: JUNIT's gating
phases (CORE, DATA-DOM, SVC-API, DOC, INT-C, INT-R) are all in the backend
file; PLAYWRIGHT's gating phases (F1, F2, F3) are all in the frontend file.
In practice this means: JUNIT's gate check reads one file throughout;
PLAYWRIGHT's gate check reads the frontend file for both the TEST-PHASE
entry itself AND every one of its gating phases (F1-F4 never appear in the
backend file at all, so there's no cross-file lookup needed for PLAYWRIGHT
specifically — this note exists so a future TEST-PHASE with mixed gating
phases doesn't get silently mishandled).

If `gated_by_phases` is empty → gate passes automatically (nothing to wait for).

If any gating phase is NOT complete:

```
══════════════════════════════════════════════════════
⛔ TEST GATE FAILED — [JUNIT|PLAYWRIGHT] — module: [MODULE]
══════════════════════════════════════════════════════
Waiting on : [PHASE-NAME: status], [PHASE-NAME: status], ...

[JUNIT|PLAYWRIGHT] cannot run until these implementation phases are COMPLETE.
══════════════════════════════════════════════════════
```
STOP here. Do not proceed to 0.2, do not generate or run any test.

### 0.2 — Read state and identify scope
Identify all PENDING subs in the requested TEST-PHASE.

### 0.3 — Look up sub weights
Same Weight Map convention as execute.md (LIGHT/MEDIUM/HEAVY/XL by scenario
count in each sub file, estimated at setup time).

### 0.4 — Classify total and decide chunking
Same table as execute.md STEP 0.3.

### 0.5 — Print assessment and wait for confirmation
Same format as execute.md STEP 0.5. WAIT for user confirmation.

---

## STEP 1 — Execution (after confirmation)

### 1.0 — Read shared context once per TEST-PHASE run
Before the first sub: read `header_file` (conventions to follow) and
`mandatory_file` (scenarios that are always required, regardless of what
the scenario subs contain) for this TEST-PHASE. Path depends on TEST-PHASE —
JUNIT and PLAYWRIGHT content live in different repos (STRUCTURAL LAW):
- **JUNIT** → `governance/modules/[MODULE]/packages/test/JUNIT/[FILE]`
  (this repo, backend/governance/ — unchanged)
- **PLAYWRIGHT** → `../frontend/governance/modules/[MODULE]/packages/test/PLAYWRIGHT/[FILE]`
  (sibling repo, frontend/governance/ — never backend/governance/)

### Per sub:

1. Read the sub file completely:
   - **JUNIT** → `governance/modules/[MODULE]/packages/test/JUNIT/[SUB].md`
   - **PLAYWRIGHT** → `../frontend/governance/modules/[MODULE]/packages/test/PLAYWRIGHT/[SUB].md`
2. Identify all scenarios in it
3. Generate test code for each scenario:
   - **JUNIT** → Spring Boot test class (`@SpringBootTest` / `@WebMvcTest` +
     `MockMvc`), file `src/test/java/.../[Scenario]Test.java`
   - **PLAYWRIGHT** → POM + spec file, following the exact conventions in
     TEST-EXECUTION-AGENT.md's NEW-mode conventions (Page Object Model, `data-testid` first,
     no `waitForTimeout`)
4. Run:
   - **JUNIT** → `mvn test -Dtest=[Class]` via bash (no MCP for JUnit).
     Use `oracle-sql` MCP (read-only) for any DB assertion the scenario needs.
   - **PLAYWRIGHT** → `playwright-mcp`, per the shared MCP execution order
     (oracle-sql precondition → playwright-mcp execute → oracle-sql confirm → screenshot on failure)
5. Classify every failure/skip using the shared taxonomy — never invent a new code
6. Update `execution-state.json` — the SAME file read in STEP 0.1 for this
   TEST-PHASE (backend file for JUNIT, frontend file for PLAYWRIGHT):
   - Mark sub as COMPLETE (or FAILED if any scenario in it failed and isn't recoverable)
   - Set next PENDING sub in the same TEST-PHASE
   - If no more subs → mark TEST-PHASE COMPLETE

---

## STEP 2 — Session Report

Write to `reports/TEST-REPORT-[MODULE]-[JUNIT|PLAYWRIGHT]-[YYYY-MM-DD].md`,
same shape as TEST-EXECUTION-AGENT.md's TEST-REPORT template (Summary / Success /
Fail / Skipped / DB Validation Summary / Action Required), using the
unified taxonomy codes throughout.

If any scenario ended `FAIL`, hand the report path to
`AUTONOMOUS-FULLSTACK-FIXING-AGENT.md` — do not attempt to fix anything here.

---

## Constraints (NON-NEGOTIABLE)

- NEVER run a TEST-PHASE whose gate check (STEP 0.1) hasn't passed
- NEVER treat `*-HEADER.md` or `MANDATORY-*.md` as a sub — read once as context
- NEVER skip the MANDATORY scenarios — they run every time the phase runs
- NEVER modify application source code — a scenario failure is reported,
  not fixed, here (that's the Autonomous Fullstack Fixing Agent's job)
- NEVER run mutating SQL via oracle-sql — read-only, always
- ALWAYS classify every failure/skip with a taxonomy code
- ALWAYS update execution-state.json after every sub
```

---

## Step 4 — Verify and report

After generating all files (two `execution-state.json` files, `execute.md`,
`execute-test.md`), print:

```
══════════════════════════════════════════════════════
MODULE SETUP COMPLETE: [MODULE]
══════════════════════════════════════════════════════
execution-state.json (backend)   ✓  governance/modules/[MODULE]/
                                     — CORE..INT-R, SEC, ALIGN, JUNIT
execution-state.json (frontend)  ✓  ../frontend/governance/modules/[MODULE]/
                                     — F1-F4, PLAYWRIGHT, align_status mirror
execute.md            ✓  .claude/commands/
execute-test.md       ✓  .claude/commands/

Phases detected       : [count]  (backend: [N], frontend F1-F4: [N])
Total subs detected   : [count]
F1/F2/F3 wired to api-docs (STEP 1.5) : ✓
api_docs_path         : governance/modules/[MODULE]/api-docs/
  (generated manually beforehand via api-doc-generator's
   `python3 generate.py --module [MODULE] --function generate` —
   this setup script does not check for it; execute.md's STEP 1.5
   reads it directly during F1/F2/F3, falling back to backend source
   only when a detail is missing from it. The frontend execution-state.json
   also carries this same api_docs_path, qualified as
   ../backend/governance/modules/[MODULE]/api-docs/, since api-docs stays
   backend-generated even though F1-F4 execution now runs from the
   frontend-owned state file.)

Test phases detected  : [JUNIT ✓ / PLAYWRIGHT ✓ / neither found]
  JUNIT      gated by : [phases found, e.g. CORE, DATA-DOM, SVC-API...] (backend file)
  PLAYWRIGHT gated by : [phases found, e.g. F1, F2, F3] (frontend file)

Weight map:
  [PHASE] / [SUB]  → [LIGHT/MEDIUM/HEAVY/XL]  ([N] tasks)
  [PHASE] / [SUB]  → [LIGHT/MEDIUM/HEAVY/XL]  ([N] tasks)
  ...

Heavy phases (require chunking):
  [PHASE] → [reason]   (or "none — all phases safe")

To start execution:
  /project:execute [MODULE] [FIRST_PHASE]
  → Assessment will be shown before any code is written

To run tests once implementation phases are COMPLETE:
  /project:execute-test [MODULE] [JUNIT|PLAYWRIGHT]
  → Gate check runs first — blocks if gating phases aren't COMPLETE yet
══════════════════════════════════════════════════════
```
