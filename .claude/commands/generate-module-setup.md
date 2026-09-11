# Generate Backend Module Setup

```
Lives at   : backend/.claude/commands/generate-module-setup.md, so it
             auto-loads as a Claude Code slash command
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

`execute-backend-test.md` is this module's test-verification command — it
drives TestSprite (this repo's sole adopted backend testing mechanism,
wired in `.mcp.json` as the `TestSprite` MCP server), producing one
coverage report — not regenerated per module.
The generated command is **fully self-contained**: it depends only on the
wired `TestSprite` MCP server and this module's own artifacts under
`governance/modules/[MODULE]/` — never on an external governance/mechanism
doc. Every rule it needs (bootstrap conditions, module scoping, archiving,
failure taxonomy) is written into the generated command itself, below.

---

## Input

```
$ARGUMENTS = MODULE
```

If missing, ask for it — do not guess.

**Module validation:** confirm a `governance/modules/[MODULE]/` folder
(or its version-suffixed variant, resolved in Step 0.5) exists on disk.
If it doesn't, stop with a plain "unknown module" message — do not guess
or fabricate a structure. This is the only validation this command
performs — there is no other precondition, because backend work has no
upstream gate to wait on.

---

## Step 0.5 — Resolve the module VERSION base (IFA-aware) — MANDATORY

A module that received an incremental feature via IFA has a current version
≥ 2, and ALL its artifacts (packages, execution-state, api-docs, generated
commands) live under a version-suffixed base — never over v1. Resolve the base
BEFORE scanning, directly from the filesystem:

```bash
ls -d governance/modules/$MODULE/v*/ 2>/dev/null | sort -t v -k2 -n | tail -1
```

Rule:
- No `vN` folder found → base = `governance/modules/$MODULE/`        (no suffix, v1)
- Highest `vN` folder found → base = `governance/modules/$MODULE/v$N/`

Call this resolved path `$MBASE`. Every `governance/modules/$MODULE/…` path in
the steps below means `$MBASE/…`. In particular, for a vN module:
- scan `$MBASE/packages/backend-execution` and `$MBASE/backend-test`
  (fallback `$MBASE/test_gen` — see Step 1's Test phase(s) section; NOT
  `$MBASE/packages/backend-test`, which this command no longer reads —
  that split output depended on governance-tools splitter tooling this
  project no longer relies on)
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
ls $MBASE/backend-test/backend-test-plan-*.md 2>/dev/null
ls $MBASE/test_gen/backend-test-plan-*.md 2>/dev/null
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

### Test phase(s) — scan generically, from the flat test-gen delivery — never assume a fixed shape

The test-gen stage delivers ONE flat, per-module plan file directly under
`$MBASE/` — `backend-test/backend-test-plan-<mod-lowercase>.md` (current
folder name) or, for a module not yet on that name, `test_gen/backend-test-plan-<mod-lowercase>.md`
(fallback — check both on the filesystem, never assume one without checking).
This command does **not** read `packages/backend-test/` — that split-folder
shape was produced by the governance-tools splitter (`agent3_splitter.py`),
which this project no longer relies on; the flat file is the sole source of
truth for test coverage now.

There is no per-phase subfolder and no per-sub file — detect phases and subs
from markers INSIDE that one file:

- PHASES = every `<!-- PHASE:<id>:START -->` … `<!-- PHASE:<id>:END -->`
  block found in the file (in practice, one: `TEST-PLAN-BE`) — its id is the
  `<id>` in the marker, never invented or renamed.
- For each PHASE block, SUBs = every `<!-- SUB:<id>:START -->` …
  `<!-- SUB:<id>:END -->` block nested inside it (e.g. `RULE-SCENARIOS`,
  `API-SCENARIOS`) — its id is the `<id>` in the marker.
- Each TC belongs to whichever SUB block contains its own
  `<!-- TC:TC-[MODULE]-<seq>:START -->` marker; this is also how STEP 0.1 of
  the generated `execute-backend-test.md` will later load the REQUIRED
  COVERAGE list.
- A separate cross-module/integration phase (historically named `INT-XM`) is
  detected the same way — as its own `<!-- PHASE:*:START -->` block — if the
  file contains one. If the file instead states outright that no such phase
  applies (e.g. "No `INT-XM` phase — SEC is ROOT"), there is none to add —
  do not fabricate an empty placeholder entry for it.
- Preserve marker order as found in the file for both phases and subs.
- Each test phase is gated by every backend EXECUTION phase that exists for
  this module (CORE … ALIGN-BE), unless the file's own header narrows it.
- `header_file` in `execution-state.json` (Step 2) is the path to this one
  flat file itself — there is no separate `*-HEADER.md` for a flat-file-sourced
  test phase.
- If neither `$MBASE/backend-test/` nor `$MBASE/test_gen/` yields a
  `backend-test-plan-*.md` file, `test_phases` is an empty array — there is
  nothing to record yet, and that is the correct, honest result (not a bug
  to work around).

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
  "test_phases": [
    {
      "id": "[TEST_PHASE_NAME]",
      "status": "PENDING",
      "gated_by_phases": ["CORE", "DATA-DOM", "SVC-API", "DOC", "INT-C", "INT-R", "SEC-BE", "ALIGN-BE"],
      "header_file": "[MBASE]/backend-test/backend-test-plan-<mod-lowercase>.md (or the test_gen/ fallback path actually used) — the flat file itself, since there is no separate *-HEADER.md",
      "subs": [
        { "id": "[SUB_NAME]", "status": "PENDING" }
      ]
    }
  ],
  "blocked": [],
  "deferred_xm": [],
  "api_doc_gaps": []
}
```

Rules:
- `test_phases` is an ARRAY — one object per real test-phase folder found in
  Step 1's generic scan (mirrors the main `phases[]` array's shape). A module
  with only a base test phase gets a one-element array; a module whose plan
  spans cross-module dependencies gets the base phase plus its integration
  phase(s) (e.g. `INT-XM`) as additional array elements.
- List only phases/subs actually found in Step 1 — never a fixed name.
- Each element's `gated_by_phases` lists only backend EXECUTION phases that
  exist for this module; `header_file` is that phase's `*-HEADER.md` if the scan
  found one, else `null`.
- `blocked`, `deferred_xm`, `api_doc_gaps` start empty.

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
3. Match each task to the applicable skill(s) in `.claude/skills/`
   (`build-*` to generate, `gov-*` to validate — skills self-declare what
   they apply to; consult the ones whose scope matches the task)
4. Read those skills from `.claude/skills/<skill>/SKILL.md` before writing
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

> **Self-contained.** This command needs only the `TestSprite` MCP server
> (wired in `.mcp.json`) and this module's own artifacts under
> `governance/modules/[MODULE]/`. Every rule it relies on is written below —
> it reads no external mechanism/governance doc, and never stops waiting on one.

## Usage
/[MODULE]/execute-backend-test

---

## STEP 0 — Plan Load, Gate Check + Assessment

### 0.1 — Load the delivered test-gen plan (the REQUIRED COVERAGE)
Before any TestSprite call, read every `TC-[MODULE]-<seq>` block out of this
module's flat test-gen plan file — `governance/modules/[MODULE]/backend-test/backend-test-plan-<mod-lowercase>.md`
(current location), falling back to `governance/modules/[MODULE]/test_gen/backend-test-plan-<mod-lowercase>.md`
if the former doesn't exist. This command does not read `packages/backend-test/`
— that split-folder shape depended on governance-tools splitter tooling this
project no longer relies on; the flat file is the sole source of truth. Across
every `<!-- PHASE:*:START -->` block the file contains (the base test phase
and any integration phase such as `INT-XM`, each nested `<!-- SUB:*:START -->`
block), extract per TC: its `TC-[MODULE]-<seq>` id, the `AC-*` / `XM-*` /
`UXD-*` it traces (from its `traces=` marker attribute / `Derived from` line),
and its one-line scenario. This list is the **REQUIRED COVERAGE** for this
run — it is what the system's own analysis says must be tested, independent
of whatever TestSprite later discovers from the code surface. If neither
location yields a `backend-test-plan-*.md` file, or the file holds no `TC-*`
block, STOP and report it — there is nothing governed to verify.

### 0.2 — Gate Check (MANDATORY)
Read `execution-state.json` → for each entry in `test_phases[]`, its
`gated_by_phases[]`. Confirm every listed backend execution phase has
`status == COMPLETE`. Empty list → that phase's gate passes automatically.

If not all complete:
```
══════════════════════════════════════════════════════
⛔ TEST GATE FAILED — [MODULE]
══════════════════════════════════════════════════════
Waiting on : [PHASE: status], ...
══════════════════════════════════════════════════════
```
STOP. Do not call any TestSprite tool.

### 0.3 — Confirm the app is reachable
`http://localhost:7272/actuator/health` (start it with `mvn spring-boot:run`
if it isn't running). Unreachable →
classify `ENVIRONMENT_FAILURE`, stop, report — do not proceed.

### 0.4 — Same assessment/confirmation pattern as execute-backend.md

---

## STEP 1 — Execution (after confirmation)

Pick the branch by whether this module already has archived tests:

### Branch A — RERUN
This module already has `.py` files under
`governance/modules/[MODULE]/testsprite/tests/` and the API surface hasn't
changed since. No TestSprite MCP tool call at all: run each archived file
directly (`python3 <path>`, never pytest — each file already calls its own
`test_*()` at the bottom) and record pass/fail per file.

### Branch B — NEW
No archived tests exist yet for this module, or the API surface changed
since the last archive. Run the TestSprite pipeline via the wired `TestSprite`
MCP server, calling its tools as the live server actually exposes them (verify
current tool names/params against the connected server before calling — do not
assume the names below never drift across a TestSprite MCP version bump):

1. **Housekeeping** — if any leftover, unarchived run is sitting in the repo-root
   `testsprite_tests/` working directory, archive it (Branch-B close-out below)
   before starting a new one; never let two runs' output mix.
2. `testsprite_bootstrap` — ONLY if `testsprite_tests/tmp/config.json`
   does not already exist (`type: backend`, `testScope: codebase`,
   `localPort: 7272`, `projectPath: <repo root>`).
3. `testsprite_generate_code_summary`
4. `testsprite_generate_standardized_prd`
5. `testsprite_generate_backend_test_plan` — (re)writes
   `testsprite_tests/testsprite_backend_test_plan.json`, spanning the
   WHOLE backend, not just this module.
6. **Module scoping (self-contained).** From that plan, select only the `TCnnn`
   entries whose endpoint path matches THIS module's own API path prefix(es).
   Discover the prefix(es) directly from this module's own artifacts — the exact
   `/api/v...` paths written in its `packages/backend-execution/SVC-API/` files
   (and its `api-docs/` if present) — never from an external table. Collect the
   matching ids; this is the module scoping step.
7. `testsprite_generate_code_and_execute` with `testIds` = exactly that
   filtered id list (never the full-plan default, which would drag every
   other module's scenarios into this module's run) — `projectName` /
   `projectPath` as usual, `serverMode` matching how the app was actually
   started (`production` only if it was built+started that way).
8. **Close out (self-contained archive).** `git mv` this module's `TCnnn_*.py`
   files into `governance/modules/[MODULE]/testsprite/tests/`, and the run's
   PRD/plan/report trio into `governance/modules/[MODULE]/testsprite/runs/<today>/`
   (create the folders if absent — everything for a module lives under its own
   `governance/modules/[MODULE]/testsprite/`). Leave the repo-root
   `testsprite_tests/` working directory clean afterward.

---

## STEP 1.9 — Coverage cross-check (governed plan ↔ TestSprite) — MANDATORY

This is the connective tissue between the delivered test-gen plan (STEP 0.1)
and TestSprite's own output. Without it the two id spaces (`TC-[MODULE]-<seq>`
vs TestSprite's `TCnnn`) stay permanently disconnected and TestSprite's
code-surface discovery silently becomes the only coverage that counts.

Map every REQUIRED-COVERAGE `TC-[MODULE]-<seq>` from STEP 0.1 to the TestSprite
`TCnnn` file(s) that actually exercise it — matched by endpoint + scenario, not
by number (the two numbering schemes are unrelated). Produce this table for the
report:

```
GOVERNED PLAN ↔ TESTSPRITE COVERAGE — [MODULE]
TC-[MODULE]-<seq>  │ traces (AC/XM/UXD) │ scenario        │ TestSprite TCnnn │ result
───────────────────┼────────────────────┼─────────────────┼──────────────────┼────────
TC-[MODULE]-001    │ AC-…               │ …               │ TC003            │ PASS
TC-[MODULE]-0NN    │ XM-… / UXD-…       │ …               │ ✗ none           │ GAP
```

- A delivered `TC-*` with NO matching TestSprite test is a **coverage gap** —
  list it prominently; it is never dropped silently.
- Integration `TC-*` (those tracing `XM-*` or `UXD-*`, from an `INT-XM` phase
  or the like) are checked here exactly like any other — a cross-module
  dependency with no exercising test is a gap, same as an uncovered `AC-*`.
- Record the coverage ratio: `<covered>/<total>` REQUIRED-COVERAGE TCs.

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
module-scoped digest, distinct from TestSprite's own raw report (which is
archived under `governance/modules/[MODULE]/testsprite/runs/<today>/`,
untouched). It MUST include the STEP 1.9 coverage table (governed plan ↔
TestSprite) and the coverage ratio, ABOVE the failure taxonomy — a green
taxonomy over an incomplete plan is not a pass. This report is complete once
the test/coverage section above is written.

Any `FAIL` or coverage GAP → report it here with its taxonomy code and STOP;
this command never fixes source itself. Fixing is a separate, deliberate step
the user runs afterward — do not auto-invoke any fixing agent from here.

### 2.1 — Update `execution-state.json` `test_phases[]` (MANDATORY)
For each entry in `test_phases[]`, set its status from the STEP 1.9 result:
- `COMPLETE` only when EVERY `TC-*` under that phase (STEP 0.1) has a passing
  TestSprite counterpart (STEP 1.9).
- `PARTIAL` when some pass but at least one `TC-*` is a gap or a fail — attach
  the gap/fail `TC-*` list to the entry.
- `PENDING` if the phase never ran.
Scope the edit to `test_phases[]` (and, if a real doc gap surfaced, one
`api_doc_gaps[]` append in the canonical shape) — touch nothing else. The
TestSprite run MUST leave `test_phases[]` reflecting exactly what it verified.

---

## Constraints (NON-NEGOTIABLE)

- NEVER run before the gate check passes
- NEVER call a TestSprite MCP tool in Branch A (RERUN) — direct `python3`
  execution of the already-archived files only
- NEVER call the bootstrap tool when `testsprite_tests/tmp/config.json`
  already exists
- NEVER skip STEP 1's housekeeping/archiving steps
- NEVER modify application source code — report, don't fix
- NEVER hand-edit an archived `.py` test file, EXCEPT the one sanctioned case:
  when backend code an archived test already covers changed (endpoint path,
  request/response fields, status/error codes, auth), update that test's
  payload/assertions to match rather than leave it silently broken
- ALWAYS classify every failure/skip
- ALWAYS load the governed `TC-*` plan (STEP 0.1) and emit the STEP 1.9
  coverage table before considering any test phase complete
- ALWAYS update `execution-state.json` `test_phases[]` status per STEP 2.1
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
Test phases detected  : [list of test-phase folders found, e.g. base + INT-XM / "none"]
  gated by : [phases found]

Weight map:
  [PHASE] / [SUB]  → [WEIGHT]  ([N] tasks)

Heavy phases (require chunking): [list or "none"]

To start execution:
  /[MODULE]/execute-backend [FIRST_PHASE]

To verify (test) once implementation is COMPLETE:
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
