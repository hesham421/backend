# Bootstrap Legacy Frontend

```
Lives at   : backend/governance/.claude/commands/bootstrap-legacy-frontend.md
             (a copy also lives at frontend/governance/.claude/commands/
             bootstrap-legacy-frontend.md — IDENTICAL content, same reason
             governance-tools/ is duplicated: Claude Code resolves slash
             commands from the CURRENT repo's .claude/commands/ folder,
             so a developer working in either repo needs it locally
             discoverable. Keep both copies in sync by hand.)
Invokes    : backend/governance/governance-tools/agent1_create_structure.py,
             agent2_archive.py, agent3_splitter.py — these Python tools
             live in ONE place only (backend/) and are invoked with
             --frontend-only / --track frontend regardless of which repo
             you're actually working in. This command does not duplicate
             that tooling logic — it is a conversational launcher, purpose-
             built for bootstrapping the FRONTEND-native structure of a
             Legacy Module Path module (see CLAUDE.md's "Legacy Module
             Path" section), with the precondition gate and documented-
             override mechanism from generate-module-setup-3.md applied
             up front, before any Agent 1/2/3 command is built or run.
```

## Your task

Help the user bootstrap the frontend-native structure (`P3_2`, `P3_5_FE`,
`packages/frontend-execution/*`, `packages/frontend-test/*`) for
one module, by walking Agent 1 → Agent 2 → Agent 3 in order, using only
the real `argparse` flags each script actually defines. Never modify a
script. Never guess a missing value — ask.

> There is NO `P4`/`P4_2` (pre-implementation audit) stage in this
> ecosystem — the audit gate was removed. P4 is not part of the structure,
> is never created, and is never required: if a `P4*` folder happens to be
> absent (the normal case), nothing here depends on it and nothing breaks.
> Should a legacy module still carry a leftover `P4*` folder, the tools
> ignore it — they only ever act on the stages their own `config.py` knows.

---

## STEP 0 — Gate Check (mandatory before building any command)

### 0.1 — Module name check (hard stop, no override path)

If `MODULE` is `SECURITY`, stop immediately, regardless of anything
else in this file. SECURITY is a PERMANENT EXCEPTION in `CLAUDE.md`'s
STRUCTURAL LAW — no frontend footprint under any circumstance. This is
a structural prohibition, not a precondition gate: it never accepts a
documented override, and this command must never build an
`--frontend-only` / `--track frontend` command for it.

### 0.2 — Backend structure exists?

```bash
ls backend/governance/modules/$MODULE/
```
If the module has no backend structure at all, stop — there is nothing
to bootstrap a frontend track onto yet. Run
`agent1_create_structure.py --module $MODULE` (backend pass) first.

### 0.3 — Frontend precondition gate

Identical gate and override mechanism as `generate-module-setup-3.md`'s
own "Precondition gate for `TRACK=frontend`" — reproduced here verbatim
so the two never diverge:

```
╔══════════════════════════════════════════════════════════════════╗
║   FRONTEND TRACK — PRECONDITION GATE                              ║
╠════════════════════════════════════╦═══════════════════════════════╣
║ GATE: BACKEND MODULE COMPLETE      ║ [Yes / No — STOP]              ║
║ confirmed for this module          ║                                ║
║ (real API Docs exist + UI/UX       ║                                ║
║  outputs human-approved + backend  ║                                ║
║  implementation 100% done)         ║                                ║
║ GATE: UI SHELL COMPLETE confirmed  ║ [Yes / No — STOP]              ║
║ frontend-execution-plan.md exists  ║ [Yes / No — STOP, Project 3.2 ║
║ with Gate ALIGN-FE ✓               ║  hasn't run yet]               ║
╚══════════════════════════════════════════════════════════════════╝
```
If any box is "No" or unconfirmed, this is not an automatic stop — offer
the documented-override path instead of a silent block:

```
This module doesn't meet the standard frontend precondition (e.g. a
legacy module with a working backend but no formal P2.5/P3.1 artifacts).

  1) Yes — I want a documented override (one question, then proceed immediately)
  2) No — stop here, I'll complete the standard flow first
```

If (2): stop, state exactly which precondition is missing, do not proceed.

If (1): ask exactly one follow-up question — a one-line reason for the
override — then immediately append a record to
`backend/governance/modules/[MODULE]/frontend-gate-overrides.json`
(create the file with an `"overrides": []` array if it doesn't exist
yet; otherwise append to the existing array, never overwrite it):
```json
{
  "date": "[actual current date]",
  "module": "[MODULE]",
  "gates_bypassed": ["list which of the two gates were No/unconfirmed"],
  "reason": "[the user's exact one-line answer]",
  "decided_by": "user (documented override — see CLAUDE.md STRUCTURAL LAW)"
}
```
Print a one-line confirmation that the override was logged, then
proceed straight to STEP 1 — do not re-ask or re-warn again this session.

This override path is exactly how a Legacy Path module (ORG,
NOTIFICATION, FILESVC, MASTERDATA) reaches frontend bootstrap: it will
almost always answer "No" on GATE: UI SHELL COMPLETE (its
`frontend-execution-plan.md` may not exist under the current-model
naming), and the override records that plainly rather than blocking it.

### 0.4 — Frontend structure already exists for this module?

```bash
ls frontend/governance/modules/$MODULE/ 2>/dev/null || echo "not yet created"
```
If it already exists, tell the user plainly and ask whether they want
to proceed anyway (Agent 1 is idempotent and skips existing folders;
Agent 2 asks before overwriting; Agent 3 has its own `--status`/`--resume`
for re-entry) rather than assuming a fresh bootstrap is wanted.

---

## STEP 1 — Agent 1 (`--frontend-only`)

Ask, in order:
1. Confirm module code (already known from STEP 0).
2. Dry run first, or execute directly?

Build the exact command from real flags only:
```
--module / -m
--frontend-only
--dry-run
```
Show the full command before running, e.g.:
```
python3 agent1_create_structure.py --module ORG --frontend-only --dry-run
```
Ask: "Run this now? (yes/no)". Only after "yes" → execute via bash.
If dry-run was chosen, after showing the plan, ask separately: "Proceed
with the actual creation? (yes/no)" before running the same command
without `--dry-run`.

Per the script's own behavior, this step does NOT write
`manifest.json` or touch `modules-registry.json` — those stay
backend-owned, written by the backend (non-`--frontend-only`) pass,
which must already have run for this module (checked in STEP 0.2).

---

## STEP 2 — Agent 2 (`--track frontend`)

Ask, in order:
1. Path to the source folder containing the generated frontend artifact
   files (`frontend-execution-plan.md`, `frontend-test-plan.md`,
   registry files) — **always ask explicitly, never assume a path.**
2. Dry run first, or execute directly?
3. If the module was already archived (frontend track) before:
   overwrite existing files? (maps to `--force`)

Build the exact command from real flags only:
```
--module / -m
--track frontend
--source / -s
--dry-run
--force / -f
```
Show the full command before running, e.g.:
```
python3 agent2_archive.py --module ORG --track frontend --source ~/Desktop/ORG-frontend-files --dry-run
```
Ask: "Run this now? (yes/no)". Only after "yes" → execute via bash.
If dry-run was chosen, show the plan, then ask separately: "Proceed
with the actual copy? (yes/no)" before running without `--dry-run`.

---

## STEP 3 — Agent 3 (`--track frontend`) — optional

Ask whether the user wants to run the splitter now, or stop after
archiving (Agent 3 can always be run later, independently).

If yes, ask:
1. Full run — all 5 stages in sequence, approving each one as it goes
2. Single stage only — specify which (1–5)
3. Resume — continue from the next incomplete stage
4. Status only — just show stage completion, don't run anything

Build the exact command from real flags only:
```
--module / -m
--track frontend
--stage / -s     (1-5, only for single-stage mode)
--resume / -r
--status
--dry-run
```
Show the full command before running, e.g.:
```
python3 agent3_splitter.py --module ORG --track frontend
```
Explain clearly before running: "This will go through Stage 1 (Parse &
Plan) first. The script itself will pause and ask you to approve each
stage individually — I will relay each approval prompt to you as it
appears, and will NOT pre-approve any stage on your behalf."

Ask: "Start this now? (yes/no)". Only after "yes" → execute via bash,
and surface each stage's output and approval prompt to the user one at
a time. Never answer a stage's `[y/N]` prompt without the user's
explicit input for that specific stage — same rule as
`governance-tools-launcher.md`.

---

## STEP 4 — Final report

```
══════════════════════════════════════════════════════
LEGACY FRONTEND BOOTSTRAP — [MODULE]
══════════════════════════════════════════════════════
Precondition gate            : [passed cleanly / documented override logged]
Agent 1 (--frontend-only)    : [done / skipped — already existed]
Agent 2 (--track frontend)   : [N files copied, N skipped]
Agent 3 (--track frontend)   : [stage reached / not run this session]
Override record              : [none / logged — see frontend-gate-overrides.json]
══════════════════════════════════════════════════════
```

---

## Constraints (NON-NEGOTIABLE)

- NEVER invent a flag that isn't in a script's real `argparse` definition
- NEVER guess a module code, source path, or version — always ask
- NEVER build or run a frontend-scoped command for `MODULE=SECURITY` —
  that exception has no override path at all, documented or otherwise
- NEVER run `--frontend-only` / `--track frontend` without EITHER the
  precondition gate passing OR a documented override logged first —
  no silent bypass either way
- NEVER use a different override-log format than the one reproduced in
  STEP 0.3 — it must match `generate-module-setup-3.md`'s format
  exactly (same fields, same file path)
- NEVER modify `agent1_create_structure.py`, `agent2_archive.py`, or
  `agent3_splitter.py`
- For Agent 3, never auto-approve a stage's internal `[y/N]` prompt —
  that confirmation belongs to the user, not to you
- Always show the exact command before running it, and always require
  explicit "yes" before executing via bash
