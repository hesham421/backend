# Process Generated Project Files — Autonomous Split Orchestrator (Cowork)

You are the single agent responsible for taking whatever governance artifact
files a person drops into a Downloads folder and driving them ALL THE WAY to
fully split, verified package files — automatically, end to end, using the real
tools, fixing what can be fixed deterministically, and stopping only for the
few things that genuinely need a human.

This workspace has two independent toolsets — `backend/governance/governance-tools/`
and `frontend/governance/governance-tools/` — each with its own
`config.py` (artifact filename lists + validation config), `agent1_create_structure.py`
(folder creation), `agent2_archive.py` (archiving), and `agent3_splitter.py`
(marker validation, deterministic safe-fix, and 5-stage splitting).

**Operating principle:** do all the deterministic, reversible work yourself and
run the whole pipeline to completion without narrating a stream of prompts. Only
stop for a real human decision: (1) overwriting existing archived work, (2) an
error the tools cannot safely auto-fix, or (3) a module code / target that
cannot be determined unambiguously. Everything else goes in the final report,
not in a question.

You never invent copy/split logic and never hand-edit marker content. The only
edits you make are (a) normalizing a file's NAME in staging, and (b) invoking
`agent3_splitter.py --fix-safe`, which performs deterministic marker repairs and
keeps the untouched original as `<file>.orig`. Both are recorded in the report.

## Fixed locations — DERIVED AT RUNTIME, never hardcoded

Work on any machine, for any user, repos checked out anywhere — never bake a
specific absolute path into this file (same portability law as
`orchestrate-module.md`). At the start of every run resolve, this run:

```
Backend root  : the backend/governance/ this command lives under — derive via
                git rev-parse --show-toplevel (+ /governance), or by walking up
                from this file's own location. BE_TOOLS = <backend root>/governance-tools.
Frontend root : the sibling frontend/governance/ next to the project root.
                FE_TOOLS = <frontend root>/governance-tools. If not found at the
                expected sibling, ASK for it — never guess or persist it.
Source        : ~/Downloads/project-files/ by default; if the person named a
                different drop folder, use that.
```

Use derived roots for THIS run only — never write them back into this file. The
source may hold SOME, ALL, or a MIX of both tracks' files — work with what's there.

---

## STEP 1 — Read the real filename lists from the tools themselves

Never hardcode which filenames belong to which track — read them from the
authoritative source so this stays correct if the tools change:

```bash
python3 -c "import sys; sys.path.insert(0, '$BE_TOOLS'); import config
for stage, files in config.ARTIFACT_FILES.items(): print(stage, files)"
python3 -c "import sys; sys.path.insert(0, '$FE_TOOLS'); import config
for stage, files in config.ARTIFACT_FILES.items(): print(stage, files)"
```

This yields the exact current templates (e.g. `srs.md`, `db-script.md`,
`backend-execution-plan.md`, `backend-test-plan.md` for backend). Some contain
`{mod}` — resolve against the module code (Step 3). The archivers match EXACT
filenames only — any leniency happens in Step 2.5, before the tools run.

---

## STEP 2 — Scan the source folder

```bash
ls -la <SOURCE>
```

Empty or missing → stop and say so. Compare what's present against both tracks'
lists from Step 1: exact BACKEND match → needs backend; exact FRONTEND match →
needs frontend; neither → carry into Step 2.5. A module can need BOTH tracks.

---

## STEP 2.5 — Auto-repair filenames that don't exactly match

Generated files sometimes pick up decoration the tools don't expect (`-ORG`,
`-v2`, `-FINAL`, wrong case). The archiver won't match these and will report the
correct file "not found." Since it's a name problem, fix it yourself:

For every file that didn't match an exact artifact name:
1. **Identify it** from an unambiguous internal header (e.g. "SOFTWARE
   REQUIREMENTS SPECIFICATION", "BACKEND EXECUTION PLAN") and/or a module line.
2. **Confirm a plausible near-match** to exactly one template (same base with
   an added/removed suffix, case, or extension decoration).
3. If (1) and (2) hold and no other file already claims that exact target:
   **rename it in the source folder** to the exact expected name (resolving
   `{mod}`). Record every rename in the report under "Auto-fixed filenames".
4. If identity/target is ambiguous, or two files map to the same target: **do
   not guess** — ask, naming the specific uncertainty.
5. Files still matching neither track go on the report's "unrecognized" list.

---

## STEP 2.6 — Validate markers and auto-fix what's safe (BEFORE archiving)

For each execution-plan / test-plan file now present in the source (backend:
`backend-execution-plan.md`, `backend-test-plan.md`; frontend: its equivalents),
validate it with its OWN track's tool — catching marker problems while the file
is still an editable staging copy:

```bash
python3 "$BE_TOOLS/agent3_splitter.py" --validate-markers --file "<SOURCE>/backend-execution-plan.md"
```

Interpret the exit code and output:

- **Exit 0, no advisories** → clean. Proceed.
- **Exit 0 with THRESHOLD ADVISORIES** → non-blocking. A phase over its split
  threshold with no SUBs (or a never-split phase with SUBs) is a SEMANTIC call,
  NOT something to auto-restructure. Record each advisory in the report and
  proceed — do NOT add `--strict-thresholds` and do NOT invent SUB groupings.
- **Exit 1 (structural / semantic errors)** → attempt a deterministic repair:

  ```bash
  python3 "$BE_TOOLS/agent3_splitter.py" --fix-safe --file "<SOURCE>/backend-execution-plan.md"
  ```

  `--fix-safe` repairs ONLY the unambiguous, reversible classes — a phase-key
  separator typo (`SVC_API` → `SVC-API`) and un-qualified SUB labels
  (`SUB:CRUD` → `SUB:SVC-API-CRUD`) — writing `<file>.orig` as backup and
  re-validating. Read its exit code:
    - **Exit 0** → the file is now valid. Record every applied fix (from→to,
      line) in the report and proceed to archive the corrected file.
    - **Exit 1** → some issue is NOT safe to auto-fix (unmatched/unclosed/
      mismatched markers, duplicate IDs, orphan atomics, an ambiguous phase key
      like `DATADOM` with the hyphen missing entirely, a `+`/space in a key that
      breaks tokenization). **STOP for this file.** Report exactly what remains,
      with line numbers, and do NOT archive or split a file that still fails
      validation — that would push a broken plan downstream. Ask the person to
      fix the flagged lines (or confirm a specific correction), then re-run.

Use `$FE_TOOLS/agent3_splitter.py` for frontend files. If a track's tool does
not support `--validate-markers`/`--fix-safe`, fall back to letting Agent 3
Stage 1 validate during Step 4 and stop on the error it reports there.

Never hand-edit markers yourself: if `--fix-safe` can't resolve it, a human
decides. Never "fix" a threshold advisory by restructuring — it's advisory.

---

## STEP 3 — Determine the module code

Most filenames don't encode the module (`srs.md`, `db-script.md`,
`backend-execution-plan.md`); a few do (`module-registry-{mod}.md`,
`prd-{mod}.md`) — check those first. Otherwise read an unambiguous "Module: X
(CODE)" header consistent across every file present (legitimate when every
signal agrees). Use the same check to resolve `{mod}` in Steps 2.5/2.6. If it
still can't be pinned down — ask. Never proceed on a guess.

---

## STEP 4 — Run the real tools to completion, per track (AUTONOMOUS)

Only touch a track that had at least one matching file (after Step 2.5) AND
whose plan files passed Step 2.6 (clean, or cleaned by `--fix-safe`). Never run
frontend tools for a module with zero frontend files, or vice versa.

### 4.1 — Preview

```bash
cd "<track>/governance-tools"
python3 agent1_create_structure.py --module [MODULE] --dry-run
python3 agent2_archive.py --module [MODULE] --source "<SOURCE>" --dry-run
```

Note whether structure is new or exists, how many files will copy, and — from
the dry-run — whether any archive step would **overwrite an existing file**.

### 4.2 — Decide whether a human gate is needed

- **No overwrite, no unresolved error** → PROCEED AUTOMATICALLY. This command's
  job is to run the whole split without pestering; do not ask just to ask.
- **An archive step would overwrite existing archived work** → this is real
  prior-work loss. Ask ONE consolidated question per track, calling out exactly
  which files would be overwritten, before doing anything for that track. Only
  after an explicit yes do you pass `--force`.

### 4.3 — Execute end to end on a single authorization

Run the real tools back to back (the person's Step-4.2 go — explicit for an
overwrite, implicit otherwise — authorizes answering each tool's own `[y/N]`
with `y` for THIS track):

```bash
python3 agent1_create_structure.py --module [MODULE]
python3 agent2_archive.py --module [MODULE] --source "<SOURCE>"    # add --force ONLY if approved in 4.2
```

Then, only if the track's execution-plan file archived successfully:

```bash
test -f "<track root>/modules/[MODULE]/<P3_1|P3_2>/<backend|frontend>-execution-plan.md" && echo ready
python3 agent3_splitter.py --module [MODULE] --stage 1
python3 agent3_splitter.py --module [MODULE] --stage 2
python3 agent3_splitter.py --module [MODULE] --stage 3
python3 agent3_splitter.py --module [MODULE] --stage 4
python3 agent3_splitter.py --module [MODULE] --stage 5
```

Run straight through. Stage 1 re-validates (structural + semantic, blocking;
thresholds advisory) — because Step 2.6 already validated/fixed the staging
copy, Stage 1 should pass; if it somehow reports a NEW blocking error, stop and
report it (do not push past a blocking error). A missing OPTIONAL file is not an
error: if only an execution plan was in this batch and no test plan, Stage 3
prints "not found — skipping" and the run still completes — that is expected,
not a failure. There is no P4/audit stage anywhere; its absence is normal and
breaks nothing.

Stage 2 also captures any content outside all phases as
`packages/<...-execution>/_SECTIONS.md` — note it in the report if produced.

If the execution-plan file wasn't in this batch, skip splitting for the track;
it can run later once that file exists.

---

## STEP 5 — Report

```
══════════════════════════════════════════════════════════════════════
PROJECT FILES PROCESSED — [MODULE]
══════════════════════════════════════════════════════════════════════
Source scanned : <SOURCE>

Files found     : [every file, tagged backend / frontend / unrecognized]

Auto-fixed filenames (2.5):
  [original] → [renamed]   (matched via: [what confirmed it])   | or "none"

Marker auto-fixes (2.6, via --fix-safe):
  [file]: PHASE:[from]→[to] (line N); SUB:[from]→[to] (line N)   | or "none"
  Backups kept: [file].orig ...                                  | or "none"

Threshold advisories (non-blocking, left as-is):
  [file]: PHASE:[key] over threshold, unsplit — verify intent    | or "none"

Backend track   : [n/a — no files / structure ✓ / archived ✓ (N, M overwritten)
                   / split ✓ (5 stages) / _SECTIONS.md captured
                   / split skipped — no execution plan this batch]
Frontend track  : [same shape]

Stopped for a human (if any):
  [file/line]: [exact unfixable issue from --fix-safe / Stage 1, and what's
               needed]                                            | or "none"

Unrecognized files (not archived, not confidently matched):
  [list]                                                          | or "none"
══════════════════════════════════════════════════════════════════════
```

---

## Constraints (NON-NEGOTIABLE)

- NEVER invent your own copy/split logic — always call the real
  `agent1_create_structure.py` / `agent2_archive.py` / `agent3_splitter.py`.
- The ONLY edits permitted are: renaming a file in the source folder (2.5) to
  match an exact expected name, and running `agent3_splitter.py --fix-safe`
  (2.6), which itself only performs deterministic, backed-up marker repairs.
  NEVER hand-edit marker syntax or ANY substantive content.
- NEVER archive or split a plan file that still fails validation after
  `--fix-safe` — stop and report; a broken plan must not go downstream.
- NEVER "fix" a threshold advisory by inventing SUB groupings, and NEVER add
  `--strict-thresholds` on the person's behalf — thresholds are a semantic call.
- NEVER hardcode an absolute path — derive roots at runtime (Step 0).
- NEVER read the filename list from anywhere but each track's live `config.py`.
- NEVER auto-approve an OVERWRITE of existing archived work — it must be called
  out and approved in 4.2 before `--force` is used.
- NEVER run one track's tools for a module with zero files for that track.
- NEVER guess the module code — ask if it can't be determined unambiguously.
- NEVER treat a missing OPTIONAL artifact (no test plan yet, no P4) as an error
  — it is a graceful skip.
- NEVER silently drop an unrecognized file — list it in the report.
