# /orchestrate-module

Master orchestration protocol for executing ANY module's governance execution
pipeline, on EITHER side of this app — frontend (`packages/frontend-execution/`,
`execute-frontend.md`) or backend (`packages/backend-execution/`,
`execute-backend.md`) — for any module under `governance/modules/` (`ORG`,
`SECURITY`, `MASTERDATA`, `NOTIFICATION`, `FILESVC`, or any future one). This
command is module-agnostic AND side-agnostic — it wraps and adds a strict
session/safety discipline on top of that module's own per-side execute
command, without duplicating that command's module-specific phase/weight
content. Run it from within whichever repo (frontend or backend) you're
orchestrating; every path below is relative to that repo's root unless said
otherwise.

## Usage

```
/orchestrate-module [MODULE] [PHASE?]
```

- `MODULE` (required): e.g. `ORG`, `SECURITY`, `MASTERDATA`. Must have a
  `governance/modules/{MODULE}/` folder with `execution-state.json`,
  an execution-package folder (`packages/frontend-execution/` or
  `packages/backend-execution/`, whichever this repo/side has), `api-docs/`,
  and a per-module execute command under `.claude/commands/{MODULE}/`
  (`execute-frontend.md` on the frontend side, `execute-backend.md` or
  equivalent on the backend side — if this side keeps that command at the
  repo root instead of per-module, use that one). If any of this is missing
  or shaped differently than expected, STOP and ask — never guess a module's
  structure.
- `PHASE` (optional): if omitted, resume from `execution-state.json`'s
  `current_phase`/`current_sub` — this command ALWAYS resumes from the last
  completed point, it never restarts a module from scratch.

## Portability — never hardcode an absolute path

This file must keep working unchanged on any machine, for any user, with the
repos checked out anywhere. **Never write a specific machine's absolute path
into this file, into a dispatched agent's prompt from memory, or into any
config this command touches.** At the start of every run:

- Derive THIS side's repo root at runtime — e.g. `pwd` (if already inside it)
  or `git rev-parse --show-toplevel` — never assume a remembered path from an
  earlier session or a different machine.
- Derive the OTHER side's repo root (needed only for STEP 2's gap-resolution
  dispatches) by checking likely sibling locations relative to this repo
  first (e.g. a sibling directory next to this one, or a path recorded in
  this project's own config/README if it documents one); if it can't be
  found this way, ASK the user for it in this conversation. Either way, use
  it for this run only — do not persist it into this command file or any
  file this command writes.
- Every path a dispatched agent's prompt needs must be resolved fresh, this
  run, on this machine — never copy-pasted from a previous run's report or
  from this file's own examples.

---

## Role of the orchestrating session (this session, running this command)

**The orchestrating session never writes code, never edits governance specs,
and never touches the target project's source files directly.** Its only
jobs are:

1. Read state and spec files to brief each dispatch (read-only).
2. Dispatch exactly ONE Claude Code agent session (via the `Agent` tool) per
   **sub** (one screen/entity, or one backend-side unit of the same
   granularity — e.g. one entity's CRUD slice) — never per individual task
   line, and never a whole phase in one shot, REGARDLESS of what that
   module's own per-side execute command's weight table says. That file's
   STEP 0 weight-based chunking ("all LIGHT/MEDIUM → whole phase in one
   pass") is **overridden** by this standing rule: **one dispatched session
   per sub, always.** This is a deliberate context-safety and auditability
   choice, not a reflection of the sub's actual complexity.
3. Run execution **strictly sequentially**: dispatch sub N, wait for its
   agent to fully finish and report, do a lightweight verification pass,
   THEN dispatch sub N+1. Never dispatch two subs' agents in parallel, even
   when they look independent — later subs in a phase routinely depend on
   an earlier sub's output files (a cross-entity options hook, a shared
   picker), and some phases (e.g. a routing phase with a single
   `renderCurrentScreen()`/`App.tsx` switch) have every sub touching the
   same shared file.
4. Gate every **phase** transition on the user's explicit go-ahead. Before
   dispatching the first sub of a phase, print a phase assessment and wait:
   ```
   ══════════════════════════════════════════════════════
   PHASE ASSESSMENT — {MODULE} / {PHASE}
   ══════════════════════════════════════════════════════
   Subs pending : [list, one line each]
   Plan         : N separate Claude Code sessions (one per sub), sequential
   ══════════════════════════════════════════════════════
   Proceed?
   ```
   Never advance to the next phase without the user's explicit confirmation
   in this conversation, even if every sub in the current phase completed
   cleanly.
5. Resolve any `api_doc_gaps` a dispatched agent reports (see the dedicated
   protocol below) **before** letting the phase be considered done and before
   presenting the next phase's assessment. A phase is not "sound" — and this
   command does not move on — until every gap opened during it is resolved,
   not just recorded.
6. Communicate with dispatched agents in **English** (full technical detail).
   Communicate with the user in **Arabic**, concisely, in a way that helps
   them decide — not a narration of tool calls.

---

## STEP 0 — Locate module & resume point

1. Read `governance/modules/{MODULE}/execution-state.json`. Note
   `current_phase`, `current_sub`, and every phase's `status` and each sub's
   `status`.
2. If a `PHASE` argument was given, use it (but still resume from whatever
   subs in it are not yet `COMPLETE` — never re-run a `COMPLETE` sub).
   Otherwise use `current_phase`.
3. Read this side's own per-module execute command
   (`governance/.claude/commands/{MODULE}/execute-frontend.md` on the
   frontend side, the backend-side equivalent otherwise) for this module's
   own phase list, weight map, and any module-specific constraints
   (module-specific rules there still apply — this command only overrides
   the *session-granularity and phase-gating* behavior described above, not
   the module's substantive rules like which files never get touched or how
   errors route).
4. **Skill & CLAUDE.md orientation — mandatory, once per run, before the
   first phase assessment.** Read, in full:
   - This side's skills index: `governance/.github/skills/frontend/CLAUDE.md`
     (or the backend equivalent) — it names every `create-*`/`enforce-*`
     skill file that exists and when to use it. Do not proceed on the
     assumption that "matching real code precedent" already covers this —
     precedent tells you what the codebase currently does, not whether that
     pattern is the one the skills actually prescribe (precedent can itself
     be non-compliant; only reading the skill catches that).
   - `governance/CLAUDE.md` (repo-wide housekeeping rules — file placement,
     comment style, etc. — that apply regardless of which module/phase is
     being executed).
   - This side's binding architecture reference,
     `governance/.github/skills/frontend/references/architecture.md`, and
     `references/contract-rules.md` — `architecture.md` is the only thing
     that outranks a skill file when the two disagree; nothing else does,
     and a skill never yields to codebase precedent on its own say-so either
     (see STEP 1.1's skill-compliance bullet for what to do when they
     conflict).
   This is a one-time read for the run, not per-sub — STEP 1.1 below still
   requires identifying, per sub, exactly which of the indexed skill files
   that sub's specific work triggers.
5. Print the phase assessment (format above) and wait for confirmation
   before dispatching anything.

---

## STEP 1 — Per-sub dispatch protocol

For each `PENDING` sub in the confirmed phase, in the order listed in
`execution-state.json`:

### 1.1 — Orchestrator prep (read-only, in this session)

Before writing the dispatch prompt, read:
- **This command file itself (`governance/.claude/commands/
  orchestrate-module.md`), in full, again — before every single sub, not
  just once at the start of the run.** Long multi-sub, multi-phase runs are
  exactly where instruction drift happens: a rule added or sharpened mid-run
  (skill-compliance, a specific NEVER) is easy to keep following at sub 1
  and silently stop applying by sub 20, without ever consciously deciding
  to skip it — your own memory of "what this command says" degrades over a
  long session even when nothing about the file itself changed. This file
  on disk is authoritative over your recollection of it; re-read it fresh
  every time, don't rely on recall from earlier in the conversation.
- The phase's `*-HEADER.md` (phase-level contracts) and this sub's spec file
  under this side's execution-package folder (`packages/frontend-execution/{PHASE}/`
  or `packages/backend-execution/{PHASE}/`).
- The relevant `api-docs/endpoints/*.md` file(s) for this sub's entity —
  this is the **only** authoritative API contract; a spec file's HTTP
  method/path/shape is a plan, api-docs is ground truth, and reality (found
  during a prior sub or investigation) beats both if they conflict.
- **Skill compliance (mandatory — not satisfied by precedent-matching
  alone, and not skippable because "the pattern already exists in the
  codebase").**
  1. Cross-reference this sub's work type against the skills index read in
     STEP 0.4 and list, explicitly, in your own working notes, every skill
     file this sub triggers before writing the dispatch prompt — err toward
     listing more, not fewer. Non-exhaustive triggers: a facade-hook/query
     sub → `create-queries` + `create-api-client`; anything producing or
     touching a form → `create-forms`; anything touching a route, nav guard,
     or a permission-gated control (button, field, action) → `create-routing`
     + `enforce-permissions`; a new entity's types/schema → `create-models`;
     a new list/table/entry page → `create-components`; a delete/activate/
     deactivate handler → `create-confirm-actions`.
  2. Read every listed skill file **in full** — not skimmed, not assumed
     from its filename or one-line description.
  3. Precedent-matching (finding an already-COMPLETE sub or sibling module
     to mirror, per the next bullet) answers "what does the existing code
     look like"; this step answers "is that code — and the precedent you're
     about to copy — actually compliant." Both questions must be answered;
     neither substitutes for the other.
  4. If a skill's prescribed pattern conflicts with the real precedent you
     found, do NOT silently pick a side. Note the conflict and surface it to
     the user (alongside or before the phase assessment) rather than
     resolving it unilaterally — a real, consistent, codebase-wide
     divergence from a skill is common and often the right call, but it is
     the user's call to bless, not this orchestrator's to assume. The one
     exception: `governance/.github/skills/frontend/references/
     architecture.md` (read in STEP 0.4) outranks a skill file outright when
     the two disagree — that one conflict resolves itself in the
     architecture doc's favor without needing to ask.
- **The real code precedent to mirror.** Before inventing any structure,
  find an already-implemented analog and copy its exact shape:
  - An earlier, already-`COMPLETE` sub in this same module (closest analog
    by container/structure pattern — e.g. flat SIDE_DRAWER vs.
    self-referencing TREE_MASTER_DETAIL on the frontend; a flat entity vs. a
    self-referencing one on the backend).
  - Failing that, an already-built sibling module's equivalent feature —
    on the frontend, e.g. `src/roles/`, `src/permissions/`,
    `src/masterLookups/` are real, working implementations of this exact
    governance pipeline for a different module (same `hooks.ts`/`*Api.ts`/
    `*.schema.ts` shape, same query-key factory pattern, same facade-hook
    shape, same comment style referencing API-IDs); on the backend, the
    equivalent already-built module's entity/repository/service/
    controller/mapper/DTO set for a comparable entity.
  - Never assume a folder/file layout from the architecture doc's
    aspirational project-structure diagram alone if the *actual* codebase
    already diverges from it in a consistent way — real, consistent
    precedent in the checked-out code wins over the doc when they disagree.
- Whether this sub needs something an **earlier sub in this phase** already
  built (a cross-entity FK "options" hook or shared type on the frontend; a
  shared repository method, mapper, or lookup on the backend). If so,
  identify the exact file/symbol to reuse and instruct the dispatched agent
  to import it, not duplicate it — and, if it doesn't exist yet on the
  earlier sub's file, to ADD it there (additive only, one new export/method,
  nothing else in that file touched) rather than reinventing it locally.

### 1.2 — Dispatch (Agent tool, one sub, `run_in_background: false` while
strictly sequential)

Write a fully self-contained English prompt — the dispatched agent has no
memory of this conversation. It MUST include:

- **Project root(s)**: this side's repo absolute path (and the OTHER side's
  repo absolute path ONLY if this dispatch is a gap-resolution task per STEP
  2, never otherwise). State it's a git repo, no worktree needed, work on
  the current checkout.
- **What NOT to touch**: every other module/phase/sub's output files, any
  file from an earlier sub in this same phase (except the one specific
  additive change identified in 1.1, if any — name that file explicitly and
  say "the ONLY change allowed to this file is X"), any page/component (or,
  backend-side, any controller/route) file unless this phase's own spec
  explicitly requires wiring into it, and any pre-existing legacy/mock-data
  files unless this phase explicitly targets them.
- **The exact files to read first**, in full, before writing anything (the
  ones identified in 1.1).
- **The "don't build a competing implementation" check**: before writing a
  task's code, confirm whether the thing this sub needs already exists —
  on the frontend, a corresponding component/route in the UI Shell; on the
  backend, an existing entity/repository/controller/service for this
  resource. If it exists: confirm/integrate, modify the existing file, never
  create a competing new one. If genuinely absent, flag it as a gap in the
  report and implement it as an explicit, minimal addition — don't redesign
  around it.
- **API Contract Resolution rule**: `api-docs/` is authoritative and the
  ONLY source for wire contracts. A frontend-side agent must NEVER consult
  backend source, controllers, services, repositories, or governance beyond
  api-docs; a backend-side agent must NEVER consult frontend source or
  invent a contract detail the SRS/spec doesn't give it either — if a detail
  is confirmed absent or contradictory, it records an `api_doc_gaps[]` entry
  (exact shape given below) and continues with everything else; it does NOT
  try to resolve the gap itself. Only the orchestrating session (this one)
  escalates a gap into the other side's investigation, per STEP 2 below.
- **OQ-blocked items**: skip, note in the report, and only write
  `// TODO: OQ-[ID] — pending resolution` in code if the spec explicitly
  names that OQ ID for that exact field/behavior — never invent one.
- **XM-ID prohibition**: never write an XM-ID reference anywhere in this
  side's code; if one seems needed, stop and flag it instead.
- **No parallel/competing mechanism for an owned responsibility** — only
  whatever this side's architecture doc (or, absent one, its own consistent
  real precedent) names as the one owner for each responsibility (e.g., on
  the frontend, TanStack Query + a single `http` client wrapper, no new
  state library, no axios/raw fetch; on the backend, the established
  repository/service/mapper layering, no bypassing it with ad-hoc queries).
- **Skill compliance instruction**: name the exact skill file(s) identified
  in 1.1, by path, and instruct the agent to read each one in full before
  writing any code, and to check its work against that skill's own
  "Verify before finishing" / violations list where the skill has one. A
  dispatch prompt that never names a skill file means that step was skipped,
  not that no skill applied.
- **Validation step**: whatever check actually verifies this phase's kind of
  change (typecheck at minimum; a build or the phase's own validation skill
  if one applies) — run it and report the result, don't claim success
  without running it.
- **execution-state.json update, precisely scoped**: set only this sub's
  `status` to `"COMPLETE"`; advance top-level `current_sub` only if it still
  equals this sub's id; if this is the LAST sub in the phase, also set the
  phase's own `status` to `"COMPLETE"` and advance `current_phase` /
  `current_sub` to the next phase's first sub. If (and only if) the agent
  found and needs to record a new gap, it may also append ONE entry to the
  top-level `api_doc_gaps[]` array in this exact shape:
  ```json
  {"phase": "...", "sub": "...", "field": "...", "description": "...", "resolution": "blocked pending frontend API contract clarification"}
  ```
  Nothing else in that file may be touched. Append, never overwrite existing
  entries.
- **No `git commit`/`git push`** — leave changes in the working tree.
- **Required report-back format** (cap the word count so it stays
  scannable): sub completed y/n, files created/changed (exact paths, and an
  explicit confirmation of what was NOT touched if there was any risk of
  ambiguity), field/contract discrepancies found vs. the spec and how
  resolved, any Shell gaps flagged, any `api_doc_gaps` added, any OQ-blocked
  items, validation result, **skill compliance** (which skill file(s) it
  checked against, and for each: compliant, or a named, justified deviation
  — not silence), and exact scope of the `execution-state.json` edit.

### 1.3 — Orchestrator verification (after the agent reports, before
dispatching the next sub)

Do this yourself, directly, in this session (it's read-only inspection, not
"execution" in the sense STEP 1 agents do):
- `git status --short` in the target repo — confirm the changed-files list
  matches exactly what the agent claimed, nothing more.
- Re-run the validation command yourself (e.g. `tsc --noEmit`) if cheap
  enough, or at least read the agent's own run output critically.
- Re-read the relevant slice of `execution-state.json` to confirm the status
  update is scoped exactly as instructed (no other phase/sub/gap entry
  disturbed).
- Confirm the agent's report actually names the skill file(s) it checked
  against (per STEP 1.1/1.2) and states compliant-or-deviation-with-reason
  for each — a report silent on this means the skill step was skipped, not
  that no skill applied; do not accept the sub as done on a report like
  that. Send it back (below) instead.
- If anything is off, send a follow-up message to the same agent (by its
  `agentId`, via `SendMessage`) to fix it in place rather than silently
  patching it yourself — keep every code change attributable to a dispatched
  Claude Code session, per the standing instruction that this orchestrating
  session does not write code itself.

---

## STEP 2 — Gap-resolution protocol (the ONE case that reaches into the other side)

Trigger: a dispatched agent's report includes a new `api_doc_gaps` entry, or
you (the orchestrator) notice one still open from a prior sub.

**This is the single exception to "never consult the other side's source."**
Whatever the cause — a naming mismatch, a genuinely missing field, a wrong
assumed path/param — it must be run to ground and the right fix applied
before the current phase is considered safe to build on. Do not carry an
unresolved gap into the next phase. (If you're orchestrating the backend side
and the gap is instead about a frontend expectation, mirror this same
investigate → root-cause → fix → document loop pointed at the frontend repo
instead — the direction reverses, the discipline doesn't.)

1. **Investigate** (read-only): dispatch a `subagent_type: Explore` agent
   into the **other side's** repo (its absolute path — ask the user once per
   session if you don't already have it recorded) with a precise,
   self-contained prompt: what field/endpoint is in question, what this
   side's api-docs currently claim, and a request to determine ONE of:
   - **(A) Naming/shape mismatch** — the data exists under a different real
     name/path; cite the exact file/class/field.
   - **(B) Mapping gap** — the underlying data is available (e.g. via an
     already-loaded relation) but never wired into the response DTO; cite
     the entity field and the mapper/service method that fails to set it.
   - **(C) Genuinely absent** — no such data exists anywhere in the backend
     for this entity; explain why.
   Also ask it to confirm whether an analogous sibling entity's equivalent
   field is real in the backend (establishes whether this is a one-off gap
   or a documented, intentional absence).
2. **Act on the finding**:
   - (A) or (C) confirmed-as-designed: correct this side's understanding
     (api-docs wording, code comment) — no change to the other side's code.
   - (B) mapping gap: dispatch a scoped, additive fix agent into the OTHER
     side's repo (`general-purpose`, needs write access) that adds ONLY the
     missing wiring (e.g., on a backend gap: one field on the response DTO +
     one line in the mapper; on a frontend gap: the equivalent minimal
     addition), mirroring the exact pattern of a sibling entity that already
     does it correctly. Hard-constrain it: no other files, no schema/
     migration changes, no `git commit`/`push`, verify with a build/compile
     step.
3. **Close the loop on this side**: dispatch (or resume) a small follow-up
   agent to update this side's `api-docs/endpoints/*.md` file(s) to document
   the now-confirmed-real field/contract, and to change the gap's
   `resolution` field in `execution-state.json` from "blocked pending
   frontend API contract clarification" (or the backend-side equivalent
   wording) to a factual note of what was found and fixed — keep the entry
   as a historical record, never delete it. Also sweep for any stale code
   comment elsewhere that still describes the field as an open gap (e.g. a
   sibling entity's comment that referenced "the analogous gap on X/Y") and
   update it too.
4. Only once every gap opened in the current phase reads as resolved may you
   present that phase as done and move to the next phase's STEP 0 assessment.

---

## STEP 3 — Phase closure and hand-back to the user

When the last sub in a phase completes and every gap is resolved:
1. Do your own final sweep: `git status --short`, full validation run,
   `execution-state.json` phase/sub statuses, and the `api_doc_gaps` array's
   resolutions.
2. Report to the user in Arabic, concisely: what got built, any corrections
   applied, any gaps found-and-fixed (with the real root cause, not just
   "resolved"), any non-blocking issues worth flagging for later (a stale UI
   filter, a deferred picker, etc.) — help them decide, don't just narrate.
3. Print the next phase's assessment and wait for explicit confirmation
   before dispatching anything in it.

---

## Constraints (non-negotiable, apply across every sub and every module)

- NEVER skip the phase-assessment confirmation gate, and never advance a
  phase without the user's explicit instruction in this conversation —
  regardless of how clean every sub's report looked.
- NEVER dispatch more than one sub's agent at a time, even for LIGHT subs,
  even when they look independent.
- NEVER let a dispatched agent touch the other side's source — only the
  orchestrator escalates across sides, and only via STEP 2, and only for a
  confirmed `api_doc_gaps` entry.
- NEVER invent a route path, component/entity name, field, endpoint, or
  permission code — trace every value to a real spec block or real api-docs
  entry; raise a gap or an OQ instead of guessing.
- NEVER redesign a component/route/entity/repository that already exists.
- NEVER write an XM-ID reference in this side's code.
- NEVER dispatch a sub without first identifying (against the skills index
  read in STEP 0.4) and reading, in full, every skill file that sub's work
  type triggers. Matching real code precedent is necessary but not
  sufficient — precedent can itself be non-compliant with a skill, and only
  an actual skill read catches that. This was skipped for the entirety of a
  frontend-side ORG-module run (2026-08-29): 30 subs dispatched across
  F1-ALIGN-FE with zero skill files read, discovered only when the user
  asked directly. Concretely found afterward: `create-forms` (R.8.1,
  "useState per field" is a listed rejection trigger) was violated by every
  entity form — RHF+zodResolver was never wired, so the schemas built
  earlier in the pipeline sat unused; and `enforce-permissions`' Layer-3
  requirement (`can()` as the first statement inside a save/deactivate/
  activate handler, not just hiding the triggering button) was never
  implemented anywhere in the module. That incident was on the frontend
  side, but the discipline it exposed is side-agnostic — apply it here too,
  don't wait for a backend-side repeat to take it seriously.
- ALWAYS update `execution-state.json` after every sub, scoped exactly as
  described — never let two subs' status updates land in the same dispatch.
- ALWAYS keep every code change attributable to a dispatched Claude Code
  session; this orchestrating session reads, briefs, verifies, and reports —
  it does not edit source itself.
- ALWAYS re-read this command file itself, in full, immediately before every
  sub's STEP 1.1 prep — never rely on your memory of it from earlier in the
  same conversation, no matter how many subs deep the run is. This file
  changes as gaps in it are found (see the skill-compliance rules above,
  themselves added after exactly this kind of drift went unnoticed for 30
  subs); a stale in-context memory of an older version defeats the point of
  fixing it here.
