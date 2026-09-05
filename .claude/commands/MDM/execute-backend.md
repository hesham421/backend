# /MDM/execute-backend

Execute the current phase for MDM — with context safety check.

## Usage
/MDM/execute-backend [PHASE]

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
PHASE ASSESSMENT — MDM / [PHASE]
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
  reference but don't repeat. (SVC-API has one: `SVC-API-HEADER.md`.)
- `packages/backend-execution/_SECTIONS.md` — plan-level content that lives
  OUTSIDE every phase (Plan Index, DB Alignment Manifest, Error Catalog,
  Agent Handoff Summary). Read once for orientation; it is context, not a sub.

### Per sub:
1. Read `packages/backend-execution/[PHASE]/[SUB].md` completely
   (the SUB file is named by its phase-qualified label, e.g. `SVC-API-CONSUMPTION.md`)
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

## Weight Map — MDM

| Phase | Sub | Weight | Notes |
|---|---|---|---|
| CORE | CORE | LIGHT | Architectural policy declarations only — no code |
| DATA-DOM | DATA-DOM | HEAVY | 2 entities (LookupType MASTER, LookupValue DETAIL), Entity+Domain layers, 14 fields total |
| SVC-API | SVC-API-CONSUMPTION | LIGHT | 1 API (API-MDM-011), thin Service+Controller |
| SVC-API | SVC-API-LOOKUP-TYPE | MEDIUM | 5 CRUD APIs (API-MDM-001..005), Service+Controller |
| SVC-API | SVC-API-LOOKUP-VALUE | MEDIUM | 5 CRUD APIs (API-MDM-006..010), Service+Controller |
| DOC | DOC | LIGHT | Contract summary / gate checklist, no code |
| INT-C | INT-C | LIGHT | Zero cross-module dependencies (pure provider) |
| INT-R | INT-R | LIGHT | No XM-IDs — nothing to activate |
| SEC-BE | SEC-BE | LIGHT | Seed data: 1 module grant, 1 page, 4 permissions |
| ALIGN-BE | ALIGN-BE | LIGHT | Traceability gate checklist only |

## Phase Map — MDM

```
CORE → DATA-DOM → SVC-API → DOC → INT-C → INT-R → SEC-BE → ALIGN-BE

CORE:      CORE
DATA-DOM:  DATA-DOM
SVC-API:   SVC-API-CONSUMPTION, SVC-API-LOOKUP-TYPE, SVC-API-LOOKUP-VALUE
DOC:       DOC
INT-C:     INT-C
INT-R:     INT-R
SEC-BE:    SEC-BE
ALIGN-BE:  ALIGN-BE
```

---

## Constraints (NON-NEGOTIABLE)

- NEVER skip STEP 0
- NEVER execute without confirmation after assessment
- NEVER invent field/column/route names — always look up db-script.md
- NEVER implement a blocked OQ item — mark and skip only
- NEVER advance phase without explicit instruction
- ALWAYS update execution-state.json after every sub
