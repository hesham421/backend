# /MDM/execute-backend-test

Execute test scenarios for MDM — only for what's actually complete.

> Read `TEST-EXECUTION-AGENT.md` first.

## Usage
/MDM/execute-backend-test

---

## STEP 0 — Gate Check + Assessment

### 0.1 — Gate Check (MANDATORY)
Read `execution-state.json` → `test_phase.gated_by_phases[]`. Confirm
every listed phase has `status == COMPLETE`.

Gated by: CORE, DATA-DOM, SVC-API, DOC, INT-C, INT-R, SEC-BE, ALIGN-BE.

If not all complete:
```
══════════════════════════════════════════════════════
⛔ TEST GATE FAILED — MDM
══════════════════════════════════════════════════════
Waiting on : [PHASE: status], ...
══════════════════════════════════════════════════════
```
STOP. Do not generate or run any test.

### 0.1b — Test plan availability check (MANDATORY, MDM-specific)
`packages/backend-test/` currently contains only `.gitkeep` — no
RULE-SCENARIOS / API-SCENARIOS sub files have been split for this module
yet (see `execution-state.json` → `api_doc_gaps[]`). Even if the gate
above passes, STOP here if `test_phase.subs` is still empty and report:

```
══════════════════════════════════════════════════════
⛔ NO TEST PLAN — MDM
══════════════════════════════════════════════════════
packages/backend-test/ has no split sub files yet.
Run the test-plan splitter for MDM before this command can proceed.
══════════════════════════════════════════════════════
```

### 0.2–0.4 — Same assessment/confirmation pattern as execute-backend.md

---

## STEP 1 — Execution (after confirmation)

### 1.0 — Read `header_file` once (if present)
`packages/backend-test/TEST-PLAN-BE-HEADER.md` — only if it exists.

### Per sub:
1. Read `packages/backend-test/[SUB].md` completely
2. Identify all scenarios
3. Generate: Spring Boot test class (`@SpringBootTest`/`@WebMvcTest` +
   `MockMvc`), file `src/test/java/.../[Scenario]Test.java`
4. Run: `mvn test -Dtest=[Class]` via bash. The `postgres` MCP server wired
   in `.mcp.json` (`postgres-mcp`, `--access-mode=restricted`, read-only) for
   any DB assertion.
5. Classify every failure/skip using the shared taxonomy
6. Update `execution-state.json`

---

## STEP 2 — Session Report

Write to `reports/TEST-REPORT-MDM-backend-[YYYY-MM-DD].md`. Any
`FAIL` → hand off to `AUTONOMOUS-FULLSTACK-FIXING-AGENT.md` — never fix here.

---

## Constraints (NON-NEGOTIABLE)

- NEVER run before the gate check passes
- NEVER run before a test plan exists (see STEP 0.1b)
- NEVER treat `*-HEADER.md` as a sub
- NEVER skip mandatory scenarios (the Mandatory-J TC blocks embedded inside
  the RULE-SCENARIOS / API-SCENARIOS sub files)
- NEVER modify application source code — report, don't fix
- NEVER run mutating SQL via the PostgreSQL MCP (read-only only)
- ALWAYS classify every failure/skip
- ALWAYS update execution-state.json after every sub
