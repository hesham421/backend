# /project:execute

Execute the current phase for the specified module — with context safety check.

## Usage
/project:execute [MODULE] [PHASE]

---

## STEP 0 — Context Safety Assessment (MANDATORY before any execution)

Before writing a single line of code, assess the execution load.

### 0.1 — Read state and identify scope

Read `governance-repo/modules/[MODULE]/execution-state.json`
Identify all PENDING subs in the requested phase.

### 0.2 — Look up sub weights from the Weight Map below

Each sub's weight was estimated at setup time from task count.
Use the Weight Map to classify the phase total without re-reading files.

### 0.3 — Classify phase total and decide chunking

| Phase Total                | Decision |
|----------------------------|----------|
| 1–2 LIGHT subs             | ✅ SAFE — execute all in one session |
| 1 MEDIUM sub                | ✅ SAFE — execute in one session |
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
   `governance-repo/modules/[MODULE]/packages/execution/[PHASE]/[SUB].md`
   (if no sub → `[PHASE]/index.md`)
2. Identify all tasks in the sub
2.5. **[Frontend phases F1/F2/F3 only] API Contract Resolution** — see STEP 1.5
   below. Resolve every endpoint contract this sub's tasks depend on BEFORE
   writing implementation code for those tasks.
3. Map each task to skill routing table in CLAUDE.md
4. Read required skills from `.github/skills/`
5. Execute all tasks in strict order
6. After last task → run `validate-backend-feature` or `validate-frontend-feature`
7. Update `execution-state.json`:
   - Mark sub as COMPLETE
   - Set `current_sub` to next PENDING sub in same phase
   - If no more subs → mark phase COMPLETE, set `current_phase` to next PENDING phase

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

## Weight Map — ORG

| Phase    | Sub                     | Weight | Est. Tasks |
|----------|--------------------------|--------|------------|
| CORE     | CORE                     | HEAVY  | ~15 (backend + frontend package/policy setup, 8 entities) |
| DATA-DOM | DATA-DOM-CORE-ENTITIES   | HEAVY  | ~28 (7 entities × fields/rules/guards/QR) |
| DATA-DOM | DATA-DOM-HEADER          | LIGHT  | 2 |
| SVC-API  | SVC-API-CRUD             | XL     | 44 (full CRUD API surface, 7 entities) |
| SVC-API  | SVC-API-HEADER           | HEAVY  | 42 (per-API repo/txn/fetch/bulk strategy audit) |
| SVC-API  | SVC-API-TREE             | MEDIUM | 2 (multi-layer: Repository CTE + Service assembly + Controller) |
| DOC      | DOC                      | LIGHT  | 3 |
| INT-C    | INT-C                    | LIGHT  | 4 (inbound XM stub declarations) |
| INT-R    | INT-R                    | LIGHT  | 1 |
| F1       | F1-HEADER                | LIGHT  | 1 |
| F1       | F1-SCR-ORG-001           | LIGHT  | 1 |
| F1       | F1-SCR-ORG-002           | LIGHT  | 1 |
| F1       | F1-SCR-ORG-003           | LIGHT  | 1 |
| F1       | F1-SCR-ORG-004           | LIGHT  | 1 |
| F1       | F1-SCR-ORG-005           | LIGHT  | 1 |
| F1       | F1-SCR-ORG-006           | LIGHT  | 1 |
| F1       | F1-SCR-ORG-007           | LIGHT  | 1 |
| F2       | F2-HEADER                | LIGHT  | 1 |
| F2       | F2-SCR-ORG-001           | MEDIUM | ~8 (Service: 6 methods + Facade: 4 ops) |
| F2       | F2-SCR-ORG-002           | MEDIUM | ~8 |
| F2       | F2-SCR-ORG-003           | MEDIUM | ~8 |
| F2       | F2-SCR-ORG-004           | MEDIUM | ~8 |
| F2       | F2-SCR-ORG-005           | MEDIUM | ~8 |
| F2       | F2-SCR-ORG-006           | MEDIUM | ~8 |
| F2       | F2-SCR-ORG-007           | MEDIUM | ~8 |
| F3       | F3-HEADER                | LIGHT  | 1 |
| F3       | F3-SCR-ORG-001           | LIGHT  | ~3 |
| F3       | F3-SCR-ORG-002           | LIGHT  | ~3 |
| F3       | F3-SCR-ORG-003           | LIGHT  | ~3 |
| F3       | F3-SCR-ORG-004           | LIGHT  | ~3 |
| F3       | F3-SCR-ORG-005           | LIGHT  | ~3 |
| F3       | F3-SCR-ORG-006           | LIGHT  | ~3 |
| F3       | F3-SCR-ORG-007           | LIGHT  | ~3 |
| SEC      | SEC                      | MEDIUM | 7 (permission matrix rows × backend+frontend) |
| ALIGN    | ALIGN                    | LIGHT  | 4 (gate coverage tables) |

---

## Phase Map — ORG

```
CORE        → CORE
DATA-DOM    → DATA-DOM-CORE-ENTITIES, DATA-DOM-HEADER
SVC-API     → SVC-API-CRUD, SVC-API-HEADER, SVC-API-TREE
DOC         → DOC
INT-C       → INT-C
INT-R       → INT-R
F1          → F1-HEADER, F1-SCR-ORG-001, F1-SCR-ORG-002, F1-SCR-ORG-003, F1-SCR-ORG-004, F1-SCR-ORG-005, F1-SCR-ORG-006, F1-SCR-ORG-007
F2          → F2-HEADER, F2-SCR-ORG-001, F2-SCR-ORG-002, F2-SCR-ORG-003, F2-SCR-ORG-004, F2-SCR-ORG-005, F2-SCR-ORG-006, F2-SCR-ORG-007
F3          → F3-HEADER, F3-SCR-ORG-001, F3-SCR-ORG-002, F3-SCR-ORG-003, F3-SCR-ORG-004, F3-SCR-ORG-005, F3-SCR-ORG-006, F3-SCR-ORG-007
SEC         → SEC
ALIGN       → ALIGN
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
