# /SEC/execute-backend-test (v2)

Execute test scenarios for SEC v2 (delta CS-SEC-001) — only for what's actually complete.

> Read `TEST-EXECUTION-AGENT.md` first.

## Usage
/SEC/v2/execute-backend-test

---

## STEP 0 — Gate Check + Assessment

### 0.1 — Gate Check (MANDATORY)
Read `governance/modules/SEC/v2/execution-state.json` →
`test_phase.gated_by_phases[]`. Confirm every listed phase has
`status == COMPLETE`. Empty list → gate passes automatically.

Gated by: SVC-API, ALIGN-BE (only the phases this v2 delta touches — CORE,
DATA-DOM, DOC, INT-C, INT-R, SEC-BE are UNCHANGED from v1 and already
COMPLETE there; they are not re-gated here).

If not all complete:
```
══════════════════════════════════════════════════════
⛔ TEST GATE FAILED — SEC v2
══════════════════════════════════════════════════════
Waiting on : [PHASE: status], ...
══════════════════════════════════════════════════════
```
STOP. Do not generate or run any test.

### 0.2–0.4 — Same assessment/confirmation pattern as execute-backend.md

---

## STEP 1 — Execution (after confirmation)

### 1.0 — Read `header_file` once (if present)
`packages/backend-test/TEST-PLAN-BE-HEADER.md` — not present yet as of this
generation (no `backend-test-plan.md` archived for this delta yet). Skip
until it exists; re-check when `backend-test-plan.md` + `test-execution-manifest.md`
are regenerated per the source plan's Agent Handoff Addendum.

### Per sub:
1. Read `governance/modules/SEC/v2/packages/backend-test/[SUB].md` completely
2. Identify all scenarios
3. Generate: Spring Boot test class (`@SpringBootTest`/`@WebMvcTest` +
   `MockMvc`), file `src/test/java/.../[Scenario]Test.java`
4. Run: `mvn test -Dtest=[Class]` via bash. The `postgres` MCP server wired
   in `.mcp.json` (`postgres-mcp`, `--access-mode=restricted`, read-only) for
   any DB assertion.
5. Classify every failure/skip using the shared taxonomy
6. Update `governance/modules/SEC/v2/execution-state.json`

---

## STEP 2 — Session Report

Write to `reports/TEST-REPORT-SEC-v2-backend-[YYYY-MM-DD].md`. Any
`FAIL` → hand off to `AUTONOMOUS-FULLSTACK-FIXING-AGENT.md` — never fix here.

---

## Test Sub Map — SEC v2

| Sub | Scenarios | Notes |
|---|---|---|
| RULE-SCENARIOS | TBD | Not yet archived — `governance/modules/SEC/v2/packages/backend-test/` is currently empty. Regenerate `backend-test-plan.md` + `test-execution-manifest.md` for CS-SEC-001 (RULE-SEC-015..018) and re-run `process-project-files` / `agent3_splitter.py --stage 3` before this sub can execute. |
| API-SCENARIOS | TBD | Same — covers API-SEC-021/API-SEC-022 once the test plan delta is archived and split. |

---

## Constraints (NON-NEGOTIABLE)

- NEVER run before the gate check passes
- NEVER treat `*-HEADER.md` as a sub
- NEVER skip mandatory scenarios (the Mandatory-J TC blocks embedded inside
  the RULE-SCENARIOS / API-SCENARIOS sub files)
- NEVER modify application source code — report, don't fix
- NEVER run mutating SQL via the PostgreSQL MCP (read-only only)
- ALWAYS classify every failure/skip
- ALWAYS update `governance/modules/SEC/v2/execution-state.json` after every sub
