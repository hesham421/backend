# /project:execute

Execute the current phase for the specified module — with context safety check.

## Usage
/project:execute FILESVC [PHASE]

---

## STEP 0 — Context Safety Assessment (MANDATORY before any execution)

Before writing a single line of code, assess the execution load.

### 0.1 — Read state and identify scope

Read `governance-repo/modules/FILESVC/execution-state.json`
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
EXECUTION ASSESSMENT: [PHASE] — module: FILESVC
══════════════════════════════════════════════════════
Pending subs  : [list from state]
Total weight  : [SAFE / CHUNK RECOMMENDED]

Sub weights:
  [SUB-ID] → [LIGHT/MEDIUM/HEAVY/XL]

Execution plan:
  [If SAFE]
  → All pending subs in one session ✅
  → Proceed? (confirm to start)

  [If CHUNK]
  → Session 1: [layer/scope]
  → Session 2: [layer/scope]
  → ...
  → Proceed with Session 1? (confirm to start)
══════════════════════════════════════════════════════
```

WAIT for user confirmation. Do not execute before confirmation.

---

## STEP 1 — Execution (after confirmation)

### Per sub:

1. Read sub file completely:
   `governance-repo/modules/FILESVC/packages/execution/[PHASE]/[SUB].md`
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
  FILESVC has no outbound XM-IDs of its own — it is the TARGET of
  NOTIFICATION's XM-NOTIF-001, not a consumer.
- Never stop the session for a blocked item — continue remaining tasks

---

## STEP 1.5 — API Contract Resolution (frontend phases F1/F2/F3 only)

This step runs for every task in F1/F2/F3 that calls or models a backend
endpoint, before that task's code is written. It does NOT apply to
CORE/DATAOM/SVCAPI/DOC/INTC/INTR/SEC/ALIGN — those phases either implement
the backend itself or don't touch API contracts directly.

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

execution-plan.md (SVCAPI phase) tells you WHICH endpoint a task calls and
WHY (the flow/logic). api-docs tells you WHAT the endpoint's real contract
is. Both are needed — neither replaces the other.

### 1.5.1 — Resolve from api-docs (default and primary path)
1. From the task, identify the endpoint (method + path, or operationId)
   referenced in execution-plan.md.
2. Open the matching `api-docs/endpoints/<group>/<slug>.md`.
3. Extract the fields/contract details the task needs.
4. Implement the task against these values directly — api-docs is trusted
   as-is, no comparison against execution-plan.md and no conflict check
   here (that's covered separately by your own review process).

### 1.5.2 — Detail missing from api-docs entirely (fallback to backend)
Only when api-docs genuinely has no answer for what the task needs — go
directly to the backend source:
1. Locate the controller/DTO/service in the module's backend source tree.
2. Read only what's needed to resolve the specific missing detail.
3. Implement against what you found.
4. Record it:
   ```
   // TODO: api_doc_gap MISSING_IN_DOCS — [detail] not in api-docs,
   // resolved from backend source: [path]
   ```
5. Add an entry to `api_doc_gaps[]`.
6. Continue execution — do not stop the session for this.

**Never skip straight to backend source as a shortcut** — 1.5.1 (api-docs)
is always tried first; 1.5.2 is a documented exception path, not a default.

**Note:** `api-docs/` for FILESVC must be generated beforehand via
`api-doc-generator`'s `python3 generate.py --module FILESVC --function generate`
before F1/F2/F3 execution — this file does not check for it.

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
[If phase complete:]
  Phase status: ✓ COMPLETE
  Next phase  : [PHASE-NAME] — awaiting your instruction
══════════════════════════════════════════════════════
```

---

## Weight Map — FILESVC

Each phase folder holds exactly one bundled sub file (Agent 3 split at
PHASE granularity, not per-screen/per-API like ORG) — so "session" below
generally means "this whole phase file", chunked by internal layer/scope
where the phase is HEAVY/XL.

| Phase   | Sub     | Weight | Basis |
|---------|---------|--------|-------|
| CORE    | CORE    | MEDIUM | Architectural policies for 2 entities (~59 lines) |
| DATAOM  | DATAOM  | MEDIUM | 2 entities × fields/rules/guards, multi-layer (FileDocument, FileCategory) |
| SVCAPI  | SVCAPI  | XL     | Full backend service+API layer, 5 APIs (API-FILE-001..005) |
| DOC     | DOC     | LIGHT  | Contract stabilization / DTO+pagination conventions only |
| INTC    | INTC    | LIGHT  | No outbound XM (this module is the XM-NOTIF-001 target) — gate check only |
| INTR    | INTR    | LIGHT  | Runtime activation status |
| F1      | F1      | LIGHT  | Single screen (SCR-FILE-001, embedded attachment panel) |
| F2      | F2      | MEDIUM | Service+Facade layer for 5 APIs, single screen |
| F3      | F3      | MEDIUM | Validation rules, single screen (~7 RULE-IDs) |
| SEC     | SEC     | LIGHT  | Permission matrix, single screen |
| ALIGN   | ALIGN   | LIGHT  | Gate coverage tables only |

---

## Phase Map — FILESVC

```
CORE     → CORE
DATAOM   → DATAOM
SVCAPI   → SVCAPI
DOC      → DOC
INTC     → INTC
INTR     → INTR
F1       → F1
F2       → F2
F3       → F3
SEC      → SEC
ALIGN    → ALIGN
```

Note: `packages/execution/TEST/TEST.md` (TC Coverage Matrix Summary,
execution-plan.md SECTION D) is intentionally excluded — informational only,
not a code phase. Matches ORG's precedent of excluding its equivalent
SECTION-D from its own Phase Map.

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
