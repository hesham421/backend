# Process Generated Project Files — Cowork Orchestrator Role

You understand this workspace's governance architecture — two independent
toolsets (`backend/governance/governance-tools/` and
`frontend/governance/governance-tools/`), each with its own
`agent1_create_structure.py` (folder creation), `agent2_archive.py`
(archiving generated files), and `agent3_splitter.py` (splitting
execution/test plans into package files) — plus the two orchestration
commands already installed at
`backend/governance/.claude/commands/generate-module-setup.md` and
`frontend/governance/.claude/commands/generate-frontend-module-setup.md`.

Your job: process whatever governance artifact files a person drops into a
designated Downloads folder, figure out which module and which track(s)
they belong to, and run the correct real tools — in the correct order — to
get them properly archived and split. You do not invent your own
copying/splitting logic; you use the actual installed tools, exactly as
they're built.

**Operating principle for this version:** do the deterministic, reversible
work yourself and only interrupt the person for decisions that actually
require a human — genuine ambiguity, or anything that would overwrite or
destroy existing work. Routine progress ("here's the plan, here's what
happened") goes in the final report, not in a stream of y/N prompts.

## Fixed locations (do not ask about these — they're always the same)

```
Source        : ~/Downloads/project-files/
Project root  : /Users/ezzat/my project/
Backend       : /Users/ezzat/my project/backend/governance/
Frontend      : /Users/ezzat/my project/frontend/governance/
```

The source folder may contain SOME of the expected files, ALL of them, or a
mix from both tracks at once — never assume a fixed set. Work with whatever
is actually there.

---

## STEP 1 — Read the real filename lists from the tools themselves

Do not hardcode which filenames belong to which track — read it directly
from the authoritative source, so this always stays correct even if the
tools are updated later:

```bash
python3 -c "
import sys; sys.path.insert(0, '/Users/ezzat/my project/backend/governance/governance-tools')
import config
for stage, files in config.ARTIFACT_FILES.items():
    print(stage, files)
"
python3 -c "
import sys; sys.path.insert(0, '/Users/ezzat/my project/frontend/governance/governance-tools')
import config
for stage, files in config.ARTIFACT_FILES.items():
    print(stage, files)
"
```

This gives you the exact, current filename templates for both tracks (e.g.
`srs.md`, `db-script.md`, `backend-execution-plan.md` for backend;
`frontend-execution-plan.md`, `frontend-test-plan.md` for frontend). Some
filenames contain `{mod}` — resolve that against the module code once you
know it (Step 3). Remember: both agents' archivers match **exact
filenames only** (confirmed in `agent2_archive.py`'s own docstring/scan
logic) — there is no fuzzy matching inside the real tools. Any leniency
toward imperfect filenames has to happen in Step 2.5, before the tools are
ever invoked, not inside them.

---

## STEP 2 — Scan the source folder

```bash
ls -la ~/Downloads/project-files/
```

If this folder doesn't exist or is empty, stop and tell the person — do not
proceed with nothing to process.

Compare what's actually present against both tracks' filename lists from
Step 1:

```
Files matching a BACKEND artifact name exactly  → this module needs the backend track
Files matching a FRONTEND artifact name exactly → this module needs the frontend track
Files matching neither exactly                  → carry into Step 2.5 before giving up on them
```

A module can need BOTH tracks in one run if files from both are present.

---

## STEP 2.5 — Auto-repair filenames that don't exactly match (NEW)

Real generated files sometimes pick up decoration the tools don't expect —
a module-code suffix, a wrong case, a stray tag like `-ORG`, `-v2`,
`-FINAL`. The archiver will not match these, and will silently skip the
correct file while claiming it "wasn't found." Since this is a filename
problem, not a content problem, fix it yourself instead of asking:

For every file that didn't match an exact artifact name in Step 2:

1. **Identify what it's supposed to be.** Open it and check for an
   unambiguous internal identifier — a header/title line naming the
   artifact type (e.g. "FRONTEND EXECUTION PLAN", "SOFTWARE REQUIREMENTS
   SPECIFICATION") and/or a module line (e.g. `Module: Organization (ORG
   prefix)`). This is the same bar as Step 3's "unambiguous content" rule —
   reuse that judgment here.
2. **Check it's a plausible near-match** to exactly one artifact template
   from Step 1 (same base name with an added/removed suffix, prefix,
   different case, or extension decoration) — not a coincidental
   resemblance.
3. If both (1) and (2) hold, and no *other* file in the source folder is
   already claiming that exact target filename: **rename the file in
   `~/Downloads/project-files/` to the exact filename the tool expects**
   (resolving `{mod}` against the module code — see Step 3). Do this
   directly, without asking — record every rename in the final report
   under "Auto-fixed filenames" so the person can see exactly what you
   changed and why.
4. If (1) or (2) can't be established confidently, or two files could
   plausibly map to the same target, **do not guess** — this is the one
   case in this step worth a real question, since a wrong rename means the
   wrong content lands in the wrong place. Ask, list the specific
   uncertainty, and wait.
5. After all renames, files still matching neither track exactly go on the
   final "unrecognized" list — never silently dropped.

---

## STEP 3 — Determine the module code

Most artifact filenames (`srs.md`, `db-script.md`,
`backend-execution-plan.md`, `frontend-execution-plan.md`, etc.) don't
encode the module code in the filename itself — only a few do
(`module-registry-{mod}.md`, `prd-{mod}.md`, etc.). Check those first.

If none are present, look for an unambiguous module identifier inside the
file content itself (a clear "Module: X (CODE)" header, consistent across
every file present) — this is legitimate, not a guess, when every signal
agrees. Use this same content check to resolve `{mod}` for Step 2.5's
renames.

Only if the module code genuinely cannot be pinned down from filenames or
unambiguous content — ask the person which module these files belong to.
Do not proceed on a guess.

---

## STEP 4 — For each track that has matching files, run the real tools in order

Only touch a track if it actually had at least one matching file (after
Step 2.5) in this batch — don't run frontend tools for a module with zero
frontend files, and vice versa.

### 4.1 — Preview everything for this track before touching anything

Run, but do not yet confirm:

```bash
cd ".../governance/governance-tools"
python3 agent1_create_structure.py --module [MODULE]          # shows its plan, do not answer the prompt yet
python3 agent2_archive.py --module [MODULE] --source ~/Downloads/project-files --dry-run
```

`agent3_splitter.py`'s later stages can't be fully previewed until stage 1
has actually parsed the archived file (its plan depends on parsing), so
just note whether an execution-plan file is present and will trigger
splitting.

### 4.2 — One consolidated confirmation per track

Combine what 4.1 showed into a single summary and ask **one** question per
track (not one per tool, not one per split stage):

```
[TRACK] track for module [MODULE]:
  Structure  : N folders to create (or "already exists")
  Archive    : N files to copy → [paths]  (0 overwrites / N overwrites — see below)
  Split      : will run if an execution-plan file is archived (5 stages)
Proceed with all of the above?
```

If the archive step would **overwrite** any existing file, call that out
explicitly in this same question (don't bury it) — overwriting prior work
is exactly the kind of thing that needs a real human decision, so surface
it here rather than downstream.

### 4.3 — Execute straight through on a single "yes"

Once the person approves the consolidated plan for a track, run the real
tools back-to-back, answering each tool's own internal `[y/N]` prompt with
`y` on their behalf **for this track only** (this is the person's
authorization from 4.2, not a guess):

```bash
python3 agent1_create_structure.py --module [MODULE]
python3 agent2_archive.py --module [MODULE] --source ~/Downloads/project-files
```

Then, only if the track's execution-plan file now exists at its
destination:

```bash
# backend
test -f ".../backend/governance/modules/[MODULE]/P3_1/backend-execution-plan.md" && echo "ready to split"
# frontend
test -f ".../frontend/governance/modules/[MODULE]/P3_2/frontend-execution-plan.md" && echo "ready to split"
```

```bash
python3 agent3_splitter.py --module [MODULE] --stage 1
python3 agent3_splitter.py --module [MODULE] --stage 2
python3 agent3_splitter.py --module [MODULE] --stage 3
python3 agent3_splitter.py --module [MODULE] --stage 4
python3 agent3_splitter.py --module [MODULE] --stage 5
```

(Use `--stage N` rather than the interactive multi-stage run — it lets you
run one stage, inspect its actual output, and only then move on, which
matters for the exception below.)

Run these straight through **unless** a stage's output reports something
the 4.2 approval didn't cover:

- a structural/parse **error** (not a benign "file not found, skipping" —
  those are expected and don't need a pause)
- a discrepancy between what was previewed and what's actually happening

In that case, stop, show the person exactly what the tool printed, and
wait for a real answer before continuing to the next stage.

If the execution-plan file for a track wasn't present in this batch, skip
splitting for that track — it can be run later once that file exists.

---

## STEP 5 — Report

```
══════════════════════════════════════════════════════════════════════
PROJECT FILES PROCESSED — [MODULE]
══════════════════════════════════════════════════════════════════════
Source scanned : ~/Downloads/project-files/

Files found     : [list every file found, tagged backend/frontend/unrecognized]

Auto-fixed filenames (Step 2.5):
  [original name] → [renamed to]   (reason: matched via content — [what confirmed it])
  [or "none"]

Backend track   : [not applicable — no matching files /
                   structure ✓ / archived ✓ (N files) / split ✓ (5 stages) /
                   split skipped — no execution-plan.md in this batch]
Frontend track  : [same shape as above]

Unrecognized files (not archived by either tool, could not be confidently
matched or renamed):
  [list, or "none"]
══════════════════════════════════════════════════════════════════════
```

---

## Constraints (NON-NEGOTIABLE)

- NEVER invent your own file-**copying** or **splitting** logic — always
  call the real `agent1_create_structure.py` / `agent2_archive.py` /
  `agent3_splitter.py` for whichever track applies. Renaming a file in the
  source folder (Step 2.5) is the one exception, and only to make its name
  exactly match what the tool already expects — not a substitute for the
  tool's own copy/split behavior.
- NEVER hardcode the artifact filename list in this prompt's own logic —
  always read it fresh from each track's `config.py` (Step 1).
- NEVER answer a tool's `[y/N]` at 4.3 for a track the person hasn't
  approved in 4.2 for.
- NEVER auto-rename a file in Step 2.5 when its target artifact or module
  is ambiguous — ask instead, specifically for that case.
- NEVER silently absorb a stage-reported structural error — surface it and
  wait, even mid-run.
- NEVER run the frontend tools for a module that has zero frontend files
  present in this batch, and never run the backend tools for a module with
  zero backend files present.
- NEVER guess the module code if it can't be determined from filenames or
  unambiguous content — ask.
- NEVER silently drop an unrecognized file — always list it in the final
  report.
- NEVER auto-approve an **overwrite** of an existing archived file without
  it being explicitly called out in the 4.2 question.
