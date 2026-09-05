# /SEC/execute-backend (v2)

Execute the current phase for SEC v2 (delta CS-SEC-001) — with context safety check.

## Usage
/SEC/v2/execute-backend [PHASE]

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
PHASE ASSESSMENT — SEC v2 / [PHASE]
══════════════════════════════════════════════════════
Subs pending : [list, weight + task count each]
Plan         : [one pass / chunked — list chunks]
══════════════════════════════════════════════════════
Proceed? [waits for confirmation]
```

---

## STEP 1 — Execution (after confirmation)

### 1.0 — Read shared context once (before the per-sub loop)
- The phase's `[PHASE]-HEADER.md` under
  `governance/modules/SEC/v2/packages/backend-execution/[PHASE]/` if present —
  phase-level strategy, tables, and intro that the SUB files reference but
  don't repeat. SVC-API has `SVC-API-HEADER.md` — read once at the start of
  that phase.
- `governance/modules/SEC/v2/packages/backend-execution/_SECTIONS.md` if
  present — plan-level content that lives OUTSIDE every phase (Plan Index
  Delta, DB Alignment note, Registry Update Block, Agent Handoff Addendum).
  Read once for orientation; it is context, not a sub.
- This is a DELTA on top of the frozen v1 (`governance/modules/SEC/`,
  ALIGN-BE ✓) — CORE, DATA-DOM, DOC, INT-C, INT-R, SEC-BE are UNCHANGED and
  not part of this v2 execution; do not re-run or re-generate them.

### Per sub:
1. Read `governance/modules/SEC/v2/packages/backend-execution/[PHASE]/[SUB].md`
   completely (the SUB file is named by its phase-qualified label, e.g.
   `SVC-API-SESSION.md`)
2. Identify all tasks
3. Map each task to the skill routing table in `governance/GOVERNANCE-RULES.md`
4. Read required skills from `.claude/skills/` (`build-*` to generate, `gov-*` to validate)
5. Execute all tasks in order
6. Run the phase's validation skill after the last task
7. Mark sub COMPLETE in `governance/modules/SEC/v2/execution-state.json`

### Blocked items — OQ
OQ-blocked task → skip, add to `blocked[]`, mark in code:
`// TODO: OQ-[ID] — pending resolution`. Continue remaining tasks.

---

## STEP 2 — Session Report

Print phase/sub completed, tasks executed, blocked items, any
api_doc_gaps entries added.

---

## Weight Map — SEC v2

| Phase | Sub | Weight | Notes |
|---|---|---|---|
| SVC-API | SVC-API-SESSION | MEDIUM | 2 self-scoped, read-only APIs (API-SEC-021 `/auth/me`, API-SEC-022 `/me/menu`) spanning DTO + Repository + Service + Controller — no new entity, no new domain rule engine. ~9 tasks: 2 new response DTOs (MeResponse, MenuNodeResponse), 3 new repo queries (QR-SEC-0030/0031/0032; QR-SEC-0028 reused as-is), 2 service methods (AuthMeService.getSelf, MenuService.buildTree), 2 controller endpoints (MeController.me / .menu). Menu tree-building (RULE-SEC-017/018 orphan-branch labeling) is the one non-trivial piece of logic. |
| ALIGN-BE | ALIGN-BE | LIGHT | Delta traceability/consistency gate check only — no code artifact; already PASSED ✓ in the source plan, this sub is a re-verification, not new work. |

## Phase Map — SEC v2

```
SVC-API → ALIGN-BE
```

| Phase | Subs (filesystem order) |
|---|---|
| SVC-API | SVC-API-SESSION |
| ALIGN-BE | ALIGN-BE |

---

## Constraints (NON-NEGOTIABLE)

- NEVER skip STEP 0
- NEVER execute without confirmation after assessment
- NEVER invent field/column/route names — always look up `db-script-SEC.md`
  (v1.1, UNCHANGED — this delta introduces no DB change)
- NEVER implement a blocked OQ item — mark and skip only
- NEVER advance phase without explicit instruction
- NEVER touch CORE, DATA-DOM, DOC, INT-C, INT-R, or SEC-BE under this v2 run
  — they are UNCHANGED from v1 and out of this delta's scope
- ALWAYS update `governance/modules/SEC/v2/execution-state.json` after every sub
