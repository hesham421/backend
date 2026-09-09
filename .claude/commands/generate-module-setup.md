# Generate Backend Module Setup

```
Lives at   : backend/.claude/commands/generate-module-setup.md (moved out of
             backend/governance/.claude/commands/ on 2026-09-05 so this
             auto-loads as a Claude Code slash command — see CLAUDE.md's
             STRUCTURAL LAW ownership table)
Invokes    : backend/governance/governance-tools/agent1_create_structure.py,
             agent2_archive.py, agent3_splitter.py — these tools know
             ONLY the backend. There is no track concept here; this
             file and the tools it calls have no representation of
             "frontend" anywhere.
```

## Your Task

Scan this repo for the specified module and generate three files:
1. `.claude/commands/[MODULE]/execute-backend.md` — implementation phase execution
2. `.claude/commands/[MODULE]/execute-backend-test.md` — test phase execution,
   gated on execute-backend.md's phases
3. `execution-state.json` — state tracker for both

Each module gets its own `.claude/commands/[MODULE]/` folder — never write
back to the flat `.claude/commands/execute-backend.md` (no module name,
collides with every other module's setup, and silently overwrites whatever
module was generated last).

`execute-backend-test.md` is this module's combined verify command — it
drives TestSprite (this repo's sole adopted backend testing mechanism,
wired in `.mcp.json` as the `TestSprite` MCP server; see
`governance/testsprite/TESTSPRITE-GOVERNANCE.md` for the shared mechanism,
file-ownership rules, and failure taxonomy every generated test command
follows) AND `debate-review --local` code review, one run, one report —
not regenerated per module.

---

## Input

```
$ARGUMENTS = MODULE
```

If missing, ask for it — do not guess.

**Module validation:** confirm `MODULE` exists in
`governance-tools/config.py`'s module registry (backed by
`modules-registry.json`). If it doesn't, stop with a plain "unknown
module" message and ask whether to register it first via
`agent1_create_structure.py --auto-register`. This is the only
validation this command performs — there is no other precondition,
because backend work has no upstream gate to wait on.

---

## Step 0.5 — Resolve the module VERSION base (IFA-aware) — MANDATORY

A module that received an incremental feature via IFA has a current version
≥ 2, and ALL its artifacts (packages, execution-state, api-docs, generated
commands) live under a version-suffixed base — never over v1. Resolve the base
BEFORE scanning, exactly the way the tools do (`config.get_module_version_path`):

```bash
python3 -c "import sys; sys.path.insert(0,'governance/governance-tools'); \
import config; print(config.get_module_version_path('$MODULE'))"
```

Rule (mirror it if you resolve by hand):
- `current_version == 1` → base = `governance/modules/$MODULE/`        (no suffix)
- `current_version == N` (N ≥ 2) → base = `governance/modules/$MODULE/v$N/`

Call this resolved path `$MBASE`. Every `governance/modules/$MODULE/…` path in
the steps below means `$MBASE/…`. In particular, for a vN module:
- scan `$MBASE/packages/backend-execution` and `$MBASE/packages/backend-test`
- write `execution-state.json` to `$MBASE/execution-state.json`
- `api_docs_path` = `$MBASE/api-docs/`
- write the generated commands to `.claude/commands/[MODULE]/v$N/` (so the v1
  commands, still valid history, are never overwritten). For v1 keep the flat
  `.claude/commands/[MODULE]/`.

NEVER hardcode the un-suffixed `modules/$MODULE/` for a module whose
current_version is ≥ 2 — that would scan the frozen v1 packages and generate a
v1 command for a v2 delta.

---

## Step 1 — Scan the repo structure

```bash
find $MBASE/packages/backend-execution -type f -name "*.md" | sort
find $MBASE/packages/backend-test -type f -name "*.md" | sort
```

From the scan results:
- Identify all PHASES (top-level folders under `packages/backend-execution/`)
- For each PHASE, identify all SUBs = the `.md` files inside that phase folder,
  EXCLUDING `index.md`, `.gitkeep`, and any `[PHASE]-HEADER.md` (the HEADER is
  phase-level shared context — read once in execution STEP 1.0, never a sub)
- Ignore `packages/backend-execution/_SECTIONS.md` for phase/sub detection — it
  is a top-level FILE (plan content outside every phase), not a phase folder
- Preserve the exact filesystem sort order
- For each SUB file, read the first 40 lines and count the tasks

Expected phases, in strict order (only include ones actually present):
```
CORE → DATA-DOM → SVC-API → DOC → INT-C → INT-R → SEC-BE → ALIGN-BE
```

### Test phase (single phase — no MARK-level split)

`packages/backend-test/` is JUnit-only by construction. Treat it as
ONE TEST-PHASE named `backend-test`:
- SUBs = every `.md` file inside, excluding `index.md`, `.gitkeep`, and any
  `*-HEADER.md` (the real subs are `RULE-SCENARIOS` / `API-SCENARIOS`, or a
  single whole-phase file when the plan was below the TC>12 threshold)
- `TEST-PLAN-BE-HEADER.md` (present only if the plan had a phase preamble)
  is shared context — read once, not a sub. The splitter does NOT emit any
  `MANDATORY-*.md` file: mandatory scenarios are TC blocks living inside the
  SUB files themselves.
- Gated by every backend phase that exists for this module

### Weight classification

| Weight | Criteria |
|--------|----------|
| LIGHT  | < 5 tasks, single layer |
| MEDIUM | 5–10 tasks, 1–2 layers |
| HEAVY  | > 10 tasks, multi-layer (Entity+Repo+Service+Controller) |
| XL     | Full feature in one sub |

Record weight and task count for every sub found.

---

## Step 2 — Generate `execution-state.json`

Location: `$MBASE/execution-state.json`  (resolved in Step 0.5 — v1 = no suffix, vN = /vN)

```json
{
  "module": "[MODULE]",
  "generated_at": "[today's date]",
  "current_phase": "[FIRST_PHASE]",
  "current_sub": "[FIRST_SUB or null]",
  "api_docs_path": "[MBASE]/api-docs/",
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
    "header_file": "packages/backend-test/TEST-PLAN-BE-HEADER.md",
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

Rules:
- List only phases actually found in Step 1
- `gated_by_phases` lists only phases that exist for this module
- `blocked`, `deferred_xm`, `api_doc_gaps` start empty

### `api_doc_gaps[]` entry format (populated during execution)
```json
{
  "type": "MISSING_IN_DOCS",
  "phase": "[PHASE]",
  "sub": "[SUB]",
  "endpoint": "[METHOD] [path]",
  "detail": "[what was missing]",
  "resolution": "resolved via backend source: <path>",
  "recorded_at": "[timestamp]"
}
```

---

## Step 3 — Generate `.claude/commands/[MODULE]/execute-backend.md`

```markdown
# /[MODULE]/execute-backend

Execute the current phase for [MODULE] — with context safety check.

## Usage
/[MODULE]/execute-backend [PHASE]

---

## STEP 0 — Context Safety Assessment (MANDATORY)

### 0.1 — Read state, identify PENDING subs in the requested phase
### 0.2 — Look up each sub's weight from the Weight Map below
### 0.3 — Classify and decide chunking

| Total weight in phase | Action |
|---|---|
| All LIGHT/MEDIUM | Execute the whole phase in one pass |
| Any HEAVY present | Chunk — one sub (or a few LIGHT subs) per pass |
| Any XL present | That sub alone is one full pass |

### 0.4 — Print assessment, wait for confirmation
```
══════════════════════════════════════════════════════
PHASE ASSESSMENT — [MODULE] / [PHASE]
══════════════════════════════════════════════════════
Subs pending : [list, weight + task count each]
Plan         : [one pass / chunked — list chunks]
══════════════════════════════════════════════════════
Proceed? [waits for confirmation]
```

---

## STEP 1 — Execution (after confirmation)

### 1.0 — Read shared context once (before the per-sub loop)
- The phase's `[PHASE]-HEADER.md` under `packages/backend-execution/[PHASE]/`
  if present — phase-level strategy, tables, and intro that the SUB files
  reference but don't repeat.
- `packages/backend-execution/_SECTIONS.md` if present — plan-level content
  that lives OUTSIDE every phase (Plan Index, DB Alignment Manifest, Error
  Catalog, Agent Handoff Summary). Read once for orientation; it is context,
  not a sub.

### Per sub:
1. Read `packages/backend-execution/[PHASE]/[SUB].md` completely
   (the SUB file is named by its phase-qualified label, e.g. `SVC-API-CRUD.md`)
2. Identify all tasks
3. Map each task to the skill routing table in `GOVERNANCE-RULES.md`
4. Read required skills from `.claude/skills/` (`build-*` to generate, `gov-*` to validate)
5. Execute all tasks in order
6. Run the phase's validation skill after the last task
7. Mark sub COMPLETE in `execution-state.json`

### Blocked items — OQ
OQ-blocked task → skip, add to `blocked[]`, mark in code:
`// TODO: OQ-[ID] — pending resolution`. Continue remaining tasks.

---

## STEP 2 — Session Report

Print phase/sub completed, tasks executed, blocked items, any
api_doc_gaps entries added.

---

## Weight Map — [MODULE]
[Insert actual weight map from Step 1]

## Phase Map — [MODULE]
[Insert actual phase → subs map from Step 1]

---

## Constraints (NON-NEGOTIABLE)

- NEVER skip STEP 0
- NEVER execute without confirmation after assessment
- NEVER invent field/column/route names — always look up db-script.md
- NEVER implement a blocked OQ item — mark and skip only
- NEVER advance phase without explicit instruction
- ALWAYS update execution-state.json after every sub
```

---

## Step 3B — Generate `.claude/commands/[MODULE]/execute-backend-test.md`

TestSprite treats the whole backend as ONE flat surface — there is no
per-module bootstrap/PRD/plan. The generated command below scopes down to
this module only at the `testIds` step (Branch B) and at archive time; it
never regenerates or re-executes another module's coverage.

```markdown
# /[MODULE]/execute-backend-test

Execute TestSprite-based test scenarios for [MODULE] — only for what's
actually complete.

> Read `governance/testsprite/TESTSPRITE-GOVERNANCE.md` in full before
> doing anything else — it is the single source of truth for how
> TestSprite is used against this repo, and for where its output lives.

## Usage
/[MODULE]/execute-backend-test

---

## STEP 0 — Gate Check + Assessment

### 0.1 — Gate Check (MANDATORY)
Read `execution-state.json` → `test_phase.gated_by_phases[]`. Confirm
every listed phase has `status == COMPLETE`. Empty list → gate passes
automatically.

If not all complete:
```
══════════════════════════════════════════════════════
⛔ TEST GATE FAILED — [MODULE]
══════════════════════════════════════════════════════
Waiting on : [PHASE: status], ...
══════════════════════════════════════════════════════
```
STOP. Do not call any TestSprite tool.

### 0.2 — Confirm the app is reachable
`http://localhost:7272/actuator/health` (start it via `mvn spring-boot:run`
per `CLAUDE.md`'s "Running Locally" if it isn't running). Unreachable →
classify `ENVIRONMENT_FAILURE`, stop, report — do not proceed.

### 0.3–0.4 — Same assessment/confirmation pattern as execute-backend.md

---

## STEP 1 — Execution (after confirmation)

Pick the branch by whether this module already has archived tests:

### Branch A — RERUN
This module already has `.py` files under
`governance/modules/[MODULE]/testsprite/tests/` and the API surface hasn't
changed since. Follow `governance/testsprite/prompts/rerun-tests.md`'s
mechanism exactly — no TestSprite MCP tool call at all: run each archived
file directly (`python3 <path>`, never pytest — each file already calls
its own `test_*()` at the bottom) and record pass/fail per file.

### Branch B — NEW
No archived tests exist yet for this module, or the API surface changed
since the last archive. Follow `governance/testsprite/prompts/start-tests.md`'s
pipeline, calling the TestSprite MCP tools as the live `TestSprite` server
actually exposes them (verify current tool names/params against the
connected server before calling — do not assume the names below never
drift across a TestSprite MCP version bump):

1. Housekeeping per TESTSPRITE-GOVERNANCE.md §4 — archive any leftover,
   unarchived run sitting in `testsprite_tests/` before starting a new one.
2. `testsprite_bootstrap` — ONLY if `testsprite_tests/tmp/config.json`
   does not already exist (`type: backend`, `testScope: codebase`,
   `localPort: 7272`, `projectPath: <repo root>`).
3. `testsprite_generate_code_summary`
4. `testsprite_generate_standardized_prd`
5. `testsprite_generate_backend_test_plan` — (re)writes
   `testsprite_tests/testsprite_backend_test_plan.json`, spanning the
   WHOLE backend, not just this module.
6. From that plan, select only the `TCnnn` entries whose endpoint matches
   this module's prefix per TESTSPRITE-GOVERNANCE.md §3's table. Collect
   their ids — this is the module scoping step.
7. `testsprite_generate_code_and_execute` with `testIds` = exactly that
   filtered id list (never the full-plan default, which would drag every
   other module's scenarios into this module's run) — `projectName` /
   `projectPath` as usual, `serverMode` matching how the app was actually
   started (`production` only if it was built+started that way).
8. Close out per TESTSPRITE-GOVERNANCE.md §4 "After a run finishes":
   `git mv` this module's `TCnnn_*.py` files into
   `governance/modules/[MODULE]/testsprite/tests/`, and the
   PRD/plan/report trio into `governance/testsprite/runs/<today>-backend/`.

---

## STEP 2 — Classify and report

Classify every failure/skip using this taxonomy — tool-agnostic, describes
outcomes rather than any specific test framework:

| Code | Meaning |
|---|---|
| `TEST_STRUCTURE_FAILURE` | Broken test script itself — not an app bug |
| `DB_PRECONDITION` | Required seed/lookup/master data missing |
| `ENVIRONMENT_FAILURE` | MCP, server, or config unreachable/broken |
| `DEPENDENCY_FAILURE` | Skipped/failed because an upstream TC failed |
| `MISSING_IMPLEMENTATION` | Endpoint or feature not built yet |
| `AUTH_FAILURE` | Login / session / token issue |
| `VALIDATION_FAILURE` | Backend rejected input that should have been valid |
| `SERVER_ERROR` | 5xx from backend |
| `CONTRACT_BREAK` | Response shape no longer matches the documented contract |
| `API_REGRESSION` | API behavior changed vs. expected |
| `DATA_INTEGRITY_ISSUE` | API step reported success but DB state is wrong |
| `BUSINESS_LOGIC_ISSUE` | A functional/business rule behaves incorrectly |

Every failed/skipped test gets exactly one code. Never invent a new one —
if nothing fits, use `ENVIRONMENT_FAILURE` and explain why in the detail.

Write `reports/TEST-REPORT-[MODULE]-backend-[YYYY-MM-DD].md` — a
module-scoped digest, distinct from TestSprite's own raw report (which
stays archived under `governance/testsprite/runs/` per TESTSPRITE-GOVERNANCE.md
§2, untouched). This file stays open for STEP 3 to append to — do not
treat it as closed once the test section is written.

Any `FAIL` → hand off to `AUTONOMOUS-FULLSTACK-FIXING-AGENT.md` — never fix
here.

---

## STEP 3 — Code review (`debate-review --local`)

Run `debate-review` against this module's accumulated working-tree diff —
this is the same command invocation as testing, one run covers both jobs:
```bash
node "<debate-review skill dir>/scripts/review-pr.mjs" --local [--base <ref>]
```
Per the skill's own documentation, `--local` reviews local
uncommitted/branch changes directly — no PR needs to exist yet. It
internally coordinates two lanes (a main-reviewer pass, then a
debate-reviewer pass that tries to knock the main pass's findings down or
add its own, with the main reviewer making the final call) and returns one
consolidated finding set — this command does not dispatch those two lanes
itself, the skill owns that internally.

Append the findings to the **same** report file from STEP 2, as a clearly
separate section — never merged into the test taxonomy table above, since
these are a different kind of finding:

```markdown
## Code Review (debate-review --local)
P0: [n]  P1: [n]  P2: [n]

| Severity | File | Summary |
|---|---|---|
| P0 | ... | ... |
```

`babysit-pr` is a separate, later follow-up — it only operates on a live
PR, so it does not run here. Once this module's branch is actually pushed
and a PR exists, run it there to close out review threads; until then,
these findings need addressing manually.

---

## Constraints (NON-NEGOTIABLE)

- NEVER run before the gate check passes
- NEVER call a TestSprite MCP tool in Branch A (RERUN) — direct `python3`
  execution of the already-archived files only
- NEVER call the bootstrap tool when `testsprite_tests/tmp/config.json`
  already exists
- NEVER skip TESTSPRITE-GOVERNANCE.md §4's housekeeping/archiving steps
- NEVER modify application source code — report, don't fix
- NEVER hand-edit an archived `.py` test file except under
  TESTSPRITE-GOVERNANCE.md §5's keep-in-sync exception
- NEVER run `debate-review` before the STEP 1/STEP 2 test run finishes —
  the report file's test section must exist before the review section is
  appended
- NEVER overwrite the STEP 2 test section when appending STEP 3's review
  section — append, don't replace
- ALWAYS classify every failure/skip
- ALWAYS update execution-state.json after every sub
```

---

## Step 4 — Verify and report

```
══════════════════════════════════════════════════════
BACKEND MODULE SETUP COMPLETE: [MODULE]
══════════════════════════════════════════════════════
execution-state.json      ✓  [MBASE]/  (v1 = modules/[MODULE]/, vN = modules/[MODULE]/vN/)
execute-backend.md        ✓  .claude/commands/[MODULE]/
execute-backend-test.md   ✓  .claude/commands/[MODULE]/

Phases detected       : [count]
Total subs detected   : [count]
Test phase detected   : backend-test [✓ / not found]
  gated by : [phases found]

Weight map:
  [PHASE] / [SUB]  → [WEIGHT]  ([N] tasks)

Heavy phases (require chunking): [list or "none"]

To start execution:
  /[MODULE]/execute-backend [FIRST_PHASE]

To verify (test + review) once implementation is COMPLETE:
  /[MODULE]/execute-backend-test
══════════════════════════════════════════════════════
```

---

## Constraints (this command itself — NON-NEGOTIABLE)

- NEVER run without MODULE specified
- NEVER invent a phase, sub, or file path not found in Step 1's scan
- NEVER reach into `frontend/governance/` for anything — this command
  and the tools it calls have no concept of a frontend track at all
- NEVER write a specific machine's absolute path into this file, into a
  generated command, or into `execution-state.json`. Derive the backend
  repo root at runtime (`git rev-parse --show-toplevel`, or by walking up
  from this file's location) — never a remembered path from an earlier
  session or machine.
