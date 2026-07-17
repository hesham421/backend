# /project:execute-test

Execute test scenarios for a TEST-PHASE (JUNIT or PLAYWRIGHT) — only for
what the implementation has actually completed.

> Read `TEST-EXECUTION-AGENT.md` first — MCP boundaries and
> failure taxonomy used below come from there.

## Usage
/project:execute-test FILESVC [JUNIT|PLAYWRIGHT]

---

## STEP 0 — Gate Check + Context Safety Assessment

### 0.1 — Gate Check (MANDATORY, before anything else)

Read `execution-state.json` → find `test_phases[]` entry matching `[JUNIT|PLAYWRIGHT]`.
For every phase id in its `gated_by_phases[]`, confirm `phases[].status == COMPLETE`.

- JUNIT      gated by → CORE, DATAOM, SVCAPI, DOC, INTC, INTR
- PLAYWRIGHT gated by → F1, F2, F3

If any gating phase is NOT complete:

```
══════════════════════════════════════════════════════
⛔ TEST GATE FAILED — [JUNIT|PLAYWRIGHT] — module: FILESVC
══════════════════════════════════════════════════════
Waiting on : [PHASE-NAME: status], [PHASE-NAME: status], ...

[JUNIT|PLAYWRIGHT] cannot run until these implementation phases are COMPLETE.
══════════════════════════════════════════════════════
```
STOP here. Do not proceed to 0.2, do not generate or run any test.

### 0.2 — Read state and identify scope
Identify all PENDING subs in the requested TEST-PHASE.

- JUNIT subs      → RULE-SCENARIOS, API-SCENARIOS
- PLAYWRIGHT subs → PLAYWRIGHT (single bundled file — FILESVC's test-plan.md
  had only 6 PLAYWRIGHT TCs, below the >8 SUB-split threshold, so Agent 3
  wrote one file with no HEADER or UI-FLOWS/INT-FLOW breakdown)

### 0.3 — Look up sub weights
| TEST-PHASE | Sub             | Weight | TC count |
|------------|-----------------|--------|----------|
| JUNIT      | RULE-SCENARIOS  | HEAVY  | 14 |
| JUNIT      | API-SCENARIOS   | HEAVY  | 11 |
| PLAYWRIGHT | PLAYWRIGHT      | MEDIUM | 6 |

### 0.4 — Classify total and decide chunking
Same table as execute-filesvc.md STEP 0.3. Both JUNIT subs are HEAVY →
CHUNK, one sub per session. PLAYWRIGHT's single sub is MEDIUM → SAFE, one
session.

### 0.5 — Print assessment and wait for confirmation
Same format as execute-filesvc.md STEP 0.4. WAIT for user confirmation.

---

## STEP 1 — Execution (after confirmation)

### 1.0 — Read shared context once per TEST-PHASE run
Before the first sub: read `header_file` for JUNIT
(`packages/test/JUNIT/JUNIT-HEADER.md`). PLAYWRIGHT has no separate header
file for FILESVC — read the single `PLAYWRIGHT.md` sub in full instead.

### Per sub:

1. Read the sub file completely: `packages/test/[JUNIT|PLAYWRIGHT]/[SUB].md`
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
6. Update `execution-state.json`:
   - Mark sub as COMPLETE (or FAILED if any scenario in it failed and isn't recoverable)
   - Set next PENDING sub in the same TEST-PHASE
   - If no more subs → mark TEST-PHASE COMPLETE

---

## STEP 2 — Session Report

Write to `reports/TEST-REPORT-FILESVC-[JUNIT|PLAYWRIGHT]-[YYYY-MM-DD].md`,
same shape as TEST-EXECUTION-AGENT.md's TEST-REPORT template (Summary / Success /
Fail / Skipped / DB Validation Summary / Action Required), using the
unified taxonomy codes throughout.

If any scenario ended `FAIL`, hand the report path to
`AUTONOMOUS-FULLSTACK-FIXING-AGENT.md` — do not attempt to fix anything here.

---

## Constraints (NON-NEGOTIABLE)

- NEVER run a TEST-PHASE whose gate check (STEP 0.1) hasn't passed
- NEVER treat `*-HEADER.md` as a sub — read once as context
- NEVER modify application source code — a scenario failure is reported,
  not fixed, here (that's the Autonomous Fullstack Fixing Agent's job)
- NEVER run mutating SQL via oracle-sql — read-only, always
- ALWAYS classify every failure/skip with a taxonomy code
- ALWAYS update execution-state.json after every sub
