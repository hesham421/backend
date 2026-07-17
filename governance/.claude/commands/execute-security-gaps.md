# /project:execute-security-gaps

Execute the current phase of **PLAN-SEC-002** (Security module — DataScope
Extension + Self-Service Auth Gap Package) — with context safety check.

This command is a dedicated variant for `governance-repo/modules/SECURITY/gaps/`,
which does NOT use the standard `packages/execution/[PHASE]/[SUB].md` layout.
Instead each phase is a single flat file (`01-PHASE-CORE.md` … `11-PHASE-ALIGN.md`)
that already contains its own SESSION PROMPT, task list, Definition of Done,
and HANDOFF report template. Do not confuse this with `.claude/commands/execute.md`
(that one drives the ORG module and uses the standard packages/execution layout —
leave it untouched).

## Usage
/project:execute-security-gaps [PHASE]

---

## STEP 0 — Context Safety Assessment (MANDATORY before any execution)

### 0.1 — Read state
Read `governance-repo/modules/SECURITY/gaps/execution-state.json`.
Confirm the requested `[PHASE]` matches `current_phase` (or is a phase the
user is explicitly re-running).

### 0.2 — Look up phase weight from the Weight Map below
Each phase in this kit is a single unit (no independent subs) — weight was
estimated at setup time from its task count and layer span.

### 0.3 — Classify and decide chunking

| Phase Weight | Decision |
|--------------|----------|
| LIGHT        | ✅ SAFE — execute in one session |
| MEDIUM       | ✅ SAFE — execute in one session (verify context budget first) |
| HEAVY        | ⚠ CHUNK — split across layers/entities within the session if needed |
| XL           | ⚠ CHUNK — split by screen/route across sessions |

### 0.4 — CONTRACT ALIGNMENT GATE (F1, F2, F3 only)

This is this module's OWN gate (per `00-README-MASTER.md` and each phase
file's own "FIRST: read HANDOFF-PHASE-N" check) — it is not the generic
API-ALIGNMENT phase used by other modules. Before F1/F2/F3:

1. Read `execution-state.json`. Confirm phase `DOC` status is `COMPLETE`
   AND phase `INT-C` status is `COMPLETE`.
2. If either is not `COMPLETE`:

```
══════════════════════════════════════════════════════
⛔ CONTRACT ALIGNMENT GATE FAILED — PLAN-SEC-002
══════════════════════════════════════════════════════
DOC    : [status]
INT-C  : [status]

F1/F2/F3 are BLOCKED until both are COMPLETE (plan Section 12).
Action required:
  /project:execute-security-gaps DOC     (if not complete)
  /project:execute-security-gaps INT-C   (if not complete)
══════════════════════════════════════════════════════
```
   STOP here.
3. Otherwise proceed to 0.5.

### 0.5 — Print assessment and wait for confirmation

```
══════════════════════════════════════════════════════
EXECUTION ASSESSMENT: [PHASE] — PLAN-SEC-002 (SECURITY/gaps)
══════════════════════════════════════════════════════
Phase file    : governance-repo/modules/SECURITY/gaps/[NN]-PHASE-[NAME].md
Weight        : [LIGHT/MEDIUM/HEAVY/XL]
Predecessor   : HANDOFF-[PREV].md — Ready for this phase? [yes/no, from file]

Execution plan:
  → Read the phase file's SESSION PROMPT in full and execute it as written
  → Proceed? (confirm to start)
══════════════════════════════════════════════════════
```

WAIT for user confirmation. Do not execute before confirmation.

---

## STEP 1 — Execution (after confirmation)

1. Read `governance-repo/modules/SECURITY/gaps/[NN]-PHASE-[NAME].md` completely.
2. Follow its "FIRST: read HANDOFF-PHASE-[N-1]..." check yourself — if that
   handoff is missing or says not ready, STOP and report it, do not improvise.
3. Execute the SESSION PROMPT's TASK list in strict order, per its own rules
   (never invent columns, `LocalizedException` not `NotFoundException`,
   exact ERR-ID message pairs, etc. — see the HIGH-PRECISION RULES in
   `00-README-MASTER.md`, which apply to every phase).
4. **[F1/F2/F3 only] API Contract Resolution** — see STEP 1.5 below, for any
   task that calls a backend endpoint (own or Organization-module).
5. Run `validate-backend-feature` or `validate-frontend-feature` as applicable.
6. Write `HANDOFF-PHASE-[N]-[NAME].md` at the governance-repo root, using the
   template embedded in the phase file itself.
7. Update `execution-state.json`:
   - Mark the phase (and its single sub) COMPLETE
   - Set `current_phase`/`current_sub` to the next PENDING phase

### Blocked items — OQ / XM DEFERRED
- OQ-blocked → skip, add to `blocked[]` in state; code: `// TODO: OQ-[ID] — pending resolution`
- XM DEFERRED → implement mock, add to `deferred_xm[]` in state; code: `// TODO: XM-[MOD]-[N] DEFERRED — replace when READY`
- Never stop the session for a blocked item — continue remaining tasks, but
  DO stop if the phase file itself says to STOP (e.g. missing predecessor
  handoff, Conflict sign-off not confirmed) — those are explicit governance
  stop conditions, not ordinary blockers.

---

## STEP 1.5 — API Contract Resolution (F1, F2, F3 only)

Priority order for any endpoint contract (path, method, fields, types,
required/optional, validation, enums, error codes, permissions):

```
1st — governance-repo/modules/SECURITY/api-docs/endpoints/<group>/<slug>.md
      (real backend, generated) — also check api-docs for Organization-module
      endpoints this plan consumes (API-ORG-008, API-ORG-012)
2nd — backend source directly (ONLY if api-docs doesn't have it) — the
      controller/DTO/service under the module's backend source root
3rd — never invent a field, type, path, or endpoint name, regardless of
      what execution-plan-SEC-gaps.md implies
```

Note: `api-docs` was regenerated 2026-07-10 (after Phase 3 / SVC-API landed
2026-07-09) and includes API-SEC-032..043 — see
`endpoints/security-datascope-role-branches/` and
`endpoints/security-datascope-user-profiles/`. Treat `api-docs` as the
complete, trusted contract source for the SECURITY module; it is no longer
expected to be missing these endpoints. The MISSING_IN_DOCS fallback below
still applies to genuine per-detail gaps (error responses, enum values,
etc.), not to whole endpoints.

- Resolve from api-docs first — implement the task against these values
  directly. api-docs is trusted as-is, no comparison against
  execution-plan-SEC-gaps.md and no conflict check here.
- If api-docs is missing/stale for an endpoint → resolve from backend source,
  record: `// TODO: api_doc_gap MISSING_IN_DOCS — [detail] not in api-docs, resolved from backend source: [path]`
  and add `{ "type": "MISSING_IN_DOCS", "phase", "endpoint", "detail", "resolution": "resolved via backend source: <path>" }` to `api_doc_gaps[]`.
- Continue execution — do not stop the session for this.

---

## STEP 2 — Session completion report

```
══════════════════════════════════════════════════════
SESSION COMPLETE — PLAN-SEC-002 (SECURITY/gaps)
══════════════════════════════════════════════════════
Phase           : [PHASE]
Handoff written : HANDOFF-PHASE-[N]-[NAME].md
Blocked         : [OQ-IDs / none]
XM Deferred     : [XM-IDs / none]
API Doc Gaps    : [count of MISSING_IN_DOCS entries / none]
──────────────────────────────────────────────────────
Phase status  : ✓ COMPLETE
Next phase    : [PHASE-NAME] — awaiting your instruction
══════════════════════════════════════════════════════
```

---

## Weight Map — SECURITY/gaps (PLAN-SEC-002)

| Phase    | File                    | Weight | Est. Tasks | Status   |
|----------|-------------------------|--------|------------|----------|
| CORE     | 01-PHASE-CORE.md        | LIGHT  | 4          | COMPLETE |
| DATA-DOM | 02-PHASE-DATA-DOM.md    | HEAVY  | 4 (4 entities + full migration BLOCK 1-8) | COMPLETE |
| SVC-API  | 03-PHASE-SVC-API.md     | HEAVY  | 12 API-IDs, Entity+Repo+Service+Controller | COMPLETE |
| DOC      | 04-PHASE-DOC.md         | LIGHT  | 3          | PENDING (current) |
| INT-C    | 05-PHASE-INT-C.md       | LIGHT  | 4 (verification/doc only) | PENDING |
| INT-R    | 06-PHASE-INT-R.md       | LIGHT  | 3 (verification only) | PENDING |
| F1       | 07-PHASE-F1.md          | XL     | 5 tasks, 4 screens/sub-tabs in one file | PENDING |
| F2       | 08-PHASE-F2.md          | MEDIUM | 4 facades, facade+wiring layers | PENDING |
| F3       | 09-PHASE-F3.md          | MEDIUM | 5 validators | PENDING |
| SEC      | 10-PHASE-SEC.md         | HEAVY  | 5 tasks, backend (permissions/JWT/rate-limit) + frontend (UI hiding) | PENDING |
| ALIGN    | 11-PHASE-ALIGN.md       | LIGHT  | 6 audit checks, no code | PENDING |

---

## Phase Map — SECURITY/gaps (PLAN-SEC-002)

```
CORE      → CORE
DATA-DOM  → DATA-DOM
SVC-API   → SVC-API
DOC       → DOC
INT-C     → INT-C
INT-R     → INT-R
F1        → F1
F2        → F2
F3        → F3
SEC       → SEC
ALIGN     → ALIGN
```

---

## Constraints (NON-NEGOTIABLE)

- NEVER skip STEP 0 — assessment is mandatory before every execution
- NEVER execute without user confirmation after assessment
- NEVER invent field or column names — always look up `db-script-SEC-gaps.md`
- NEVER use `NotFoundException` — always `LocalizedException`
- NEVER implement a blocked OQ item — mark and skip only
- NEVER advance to next phase without explicit instruction from user
- ALWAYS update `execution-state.json` after every phase
- NEVER start F1/F2/F3 unless DOC and INT-C are both COMPLETE (STEP 0.4)
- NEVER implement a frontend task that calls a backend endpoint without
  first checking api-docs (STEP 1.5) — treat it as trusted, no cross-check
  against execution-plan-SEC-gaps.md needed
- NEVER go directly to backend source for a contract unless api-docs is
  confirmed missing/stale for that endpoint — and always log that fallback
- Conflict #19 sign-off already confirmed (see Phase 1 handoff) — do not
  re-litigate it; Conflict #20/BLK-SEC-002 remains OPEN (technical
  dependency-cycle issue) — expected, not a new discrepancy, until a real
  resolution is implemented
