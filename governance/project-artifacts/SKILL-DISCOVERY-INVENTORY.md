# Skill Discovery Inventory — Search for Misplaced AI-Agent Skills

Working record of a search across `frontend/` and `backend/` for content that is
actually AI-agent guidance/reference material (a "skill") sitting outside
`governance-repo/.github/skills/`, performed **2026-07-07**. This is a narrower,
different-in-kind task from the `CONSOLIDATION-INVENTORY.md` pass (which handled
non-functional `.md`/`.sql` housekeeping docs) — this pass is specifically about
skill-shaped content.

Scope excluded: `node_modules/`, `dist/`, `.git/`, `build/`, `.angular/`, `target/`,
`backend/tests/.venv/` (untracked, confirmed via prior inventory), and
`design-system/avelynq-source/` + `design-system/avelynq-extensions/` in
`frontend/` — already identified as skill-shaped in the prior consolidation pass
and explicitly out of scope for this task (see "Known bundle, not touched" below).

## Method

Searched both repos for each Step-1 signal from the task brief:
- `find -iname "SKILL.md"` (and case variants)
- `find -iname "*.prompt.md"` and `.d.ts` spec-file pairs
- All `*.md`/`*.txt`/README files, read for agent-directed framing
  ("read this before generating", "for an AI agent", YAML frontmatter with
  `name:`/`description:`/`user-invocable:`)
- Directories named `*skill*`, `*prompt*`, `*guidance*`, `*spec*`, `*convention*`

## Results

### `frontend/` — no new candidates

| Path | Signal checked | Verdict |
|------|-----------------|---------|
| `frontend/CLAUDE.md` | Contains `---` horizontal rules (matched frontmatter grep) and agent-directed language ("Before generating any code...") | **Kept as-is** — this is the repo's live router/pointer to `governance-repo`, not a skill itself. It has no `name:`/`description:` frontmatter and its entire content is "go look in governance-repo." Correctly placed per the existing convention (mirrors `backend/CLAUDE.md`, `governance-repo/CLAUDE.md`). |
| `frontend/.github/copilot-instructions.md` | Same agent-directed language, same router role | **Kept as-is** — same reasoning as above. |
| `design-system/` (everything else) | N/A | Only contains `avelynq-source/` and `avelynq-extensions/` — no other subfolders. Nothing new here. |
| Rest of `frontend/` (`src/app/**`, etc.) | `find`/`grep` swept whole tree | No `SKILL.md`, no `.prompt.md`, no agent-directed READMEs, no matching directory names outside the already-known AVELYNQ bundle. |

### `backend/` — no new candidates

| Path | Signal checked | Verdict |
|------|-----------------|---------|
| `backend/CLAUDE.md` | Router language | **Kept as-is** — same router pattern as frontend's. |
| `backend/.github/copilot-instructions.md` | Router language | **Kept as-is** — same router pattern. |
| `backend/erp-security/README.md` | Only other `.md` in the repo (excluding router files and vendored `.venv`/`pip` license files) | **Kept as-is — ordinary developer documentation.** Read in full: describes the module's features, setup, API endpoints, DB schema, contribution guidelines in a normal "here's what this module does" register. No agent-directed framing, no frontmatter, not written as instructions for an AI to follow. |
| Rest of `backend/` | `find`/`grep` swept whole tree (excluding `tests/.venv`, `target/`) | No `SKILL.md`, no `.prompt.md`/`.d.ts` spec pairs, no matching directory names. |

**Conclusion: zero new misplaced skills found.** Nothing was relocated in this
pass — see "Verdict per candidate" in the final report for the one line item
that needed a decision (below).

## Known bundle, not touched (important discrepancy found)

This task's brief stated the AVELYNQ bundle "has already been relocated ... in
a prior task — do not redo that." That is **not accurate**, and is logged here
for the record:

- `frontend/design-system/avelynq-source/` and `avelynq-extensions/` are still
  fully present in `frontend` (SKILL.md, all `.prompt.md`/`.d.ts` component pairs,
  tokens, guidelines, assets — ~100 files, untouched).
- `governance-repo/.github/skills/frontend/avelynq-design-system/` exists only
  as an **empty directory skeleton** (subfolders `assets/`, `components/`,
  `extensions/`, `guidelines/`, `tokens/`, `ui_kits/`, `uploads/` — zero files
  inside, untracked by git, absent from `git status`/log/reflog/stash in either
  repo).
- The actual last relevant commit in `frontend`
  (`8ebb881 "Move non-functional AVELYNQ migration audits and one-off docs to
  governance-repo"`) explicitly says the bundle was **"deliberately left in
  place pending a human decision"** — see `CONSOLIDATION-INVENTORY.md`'s
  `## Needs Your Decision` section for the full reasoning (it's cited by path,
  in prose comments, from ~20 shipped components; moving it would sever those
  references).

**This session's instruction**: when this discrepancy was surfaced, the human
operator chose to leave the bundle untouched and proceed only with the new
discovery task (rather than complete the deferred relocation now). So:

- No relocation was performed for `avelynq-source`/`avelynq-extensions` in this
  session either.
- The empty skeleton directory under
  `governance-repo/.github/skills/frontend/avelynq-design-system/` was left
  as found (not deleted, not populated) — it wasn't created by this session,
  and its origin is unknown (likely a stalled/interrupted attempt at the
  deferred move from some other session).
- The conflict-check re-verification the task brief asked for
  ("re-confirm coverage for `avelynq-design-system`") **could not be
  meaningfully performed** — there is no relocated content at that path to
  check for conflicts against. That check remains blocked on the same human
  decision `CONSOLIDATION-INVENTORY.md` already flagged.

**Follow-up for a human**: the original decision in `CONSOLIDATION-INVENTORY.md`
(`## Needs Your Decision`) is still open and is now the actual blocker for
ever completing this bundle's relocation and conflict-check.
