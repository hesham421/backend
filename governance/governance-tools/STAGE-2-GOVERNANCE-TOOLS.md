# STAGE 2 — GOVERNANCE TOOLS (Marker Packaging & Archival Layer) — BACKEND

```
Status        : IMPLEMENTED ✓  (backend-only toolset)
Depends on    : PROJECT-3-REGISTRY.md Section 5.7 (Artifact Marker Protocol)
Scope         : Post-generation tooling — runs AFTER the backend analysis +
                 plans are released (P0 → P3_1 execution plan, P3_5_BE test
                 plan) and BEFORE the backend code is implemented.
Location      : backend/governance/governance-tools/ (Python 3.10+, stdlib
                 only — local execution, typically via Claude Code on macOS).
Relationship  : Independent of the governance engines. Reads their OUTPUT
                 artifacts. Never modifies governance logic, never regenerates
                 content, never makes a decision an engine is responsible for.
                 No LLM call anywhere — deterministic file operations only.
```

This toolset knows ONLY the backend. There is no `frontend` concept, no
`--track` flag, and no `P4`/audit concept anywhere — the frontend has its own
separate copy of these tools in `frontend/governance/governance-tools/`, and
the pre-implementation audit gate was removed. Agent 3 runs ONCE here (five
stages) for the backend; the frontend toolset runs its own separately.

This document is the contract companion to PROJECT-3-REGISTRY.md Section 5.7.
Any future edit to that section (marker syntax, phase keys, thresholds) must be
reflected here and in the code in lockstep — see the Impact Checklist at the end.

---

## 1 — WHY STAGE 2 EXISTS

The backend engines produce large text artifacts (`backend-execution-plan.md`
can reach thousands of lines). Stage 2 solves two problems without touching
what those engines generate:

```
PROBLEM 1 — Repository organization
  Where do finalized artifacts live? How are they archived per module,
  per version, without manual file-juggling?

PROBLEM 2 — Context-window cost for downstream agents
  An implementation agent (Claude Code) that only needs API-ORG-014 should
  not have to load the whole backend-execution-plan.md to find it.
```

It is a packaging and archival layer, not a content-generation layer.

---

## 2 — TWO-PART DESIGN

```
PART A — Marker Governance (lives INSIDE the generating engine)
  The engine embeds HTML-comment markers into backend-execution-plan.md at
  generation time (P3.1), and the Test Generation Engine does the same for
  backend-test-plan.md — both per PROJECT-3-REGISTRY.md Section 5.7. This is
  NOT a Stage 2 script.

PART B — Artifact Packaging & Archival (Stage 2 tooling — this document)
  Independent Python scripts that:
    1. Create the canonical backend folder structure for a module (Agent 1)
    2. Archive generated artifacts into that structure (Agent 2)
    3. Read the embedded markers and split artifacts into smaller,
       addressable package files (Agent 3)
```

Stage 2 is entirely dependent on Part A. If the marker syntax in Section 5.7
changes, Stage 2 must change in lockstep — coupled by contract, not accident.

---

## 3 — COMPONENTS

```
governance-tools/
├── config.py                    Single source of truth: repo path, modules,
│                                 folder structure, artifact filenames, package
│                                 structure, marker regexes, ALLOWED_PARENTS,
│                                 CANONICAL_PHASE_KEYS, manifest schema.
├── marker_parser.py             Marker parsing + validation engine. Builds a
│                                 nested tree, validates STRUCTURAL rules, and
│                                 (validate_semantics) SEMANTIC rules.
├── agent1_create_structure.py   Creates the backend folder structure (or a new
│                                 version) for a module.
├── agent2_archive.py            Copies generated artifacts from a source folder
│                                 into the canonical structure.
├── agent3_splitter.py           Staged, approve-gated splitter (5 stages) +
│                                 standalone --validate-markers mode.
└── tests/                       pytest suite — parser, semantic validator,
                                  full split pipeline, dry-run & force guards.
```

Marker hierarchy (every backend file): `PHASE → [SUB] → ATOM (API/XM/TC)`.
There is NO `MARK` level — `backend-test-plan.md` is JUnit-only by
construction, so the file itself is the tool boundary.

---

## 3A — VALIDATION: STRUCTURAL + SEMANTIC

Two layers, both blocking:

```
STRUCTURAL (marker_parser.parse_file) — marker well-formedness:
  ✓ Every START has a matching END; no unclosed/unmatched/mismatched markers
  ✓ Legal nesting only (PHASE → SUB → ATOM); no cross-nesting
  ✓ No duplicate marker_id within a kind, across the whole document
    (this is what makes phase-qualified SUB labels mandatory — a bare
     SUB:CRUD repeated under two phases collides; SUB:SVC-API-CRUD does not)

SEMANTIC (marker_parser.validate_semantics) — governance-contract validity:
  ✓ Every PHASE key is one of the file's CANONICAL keys (config.CANONICAL_
    PHASE_KEYS) — a non-canonical key (e.g. "DATADOM" missing the hyphen)
    is rejected here instead of being silently skipped by the splitter.
  ✓ Every SUB label is phase-qualified {PHASE-KEY}-{LABEL} (AMEND-P3-N),
    EXCEPT in backend-test-plan.md where RULE-SCENARIOS / API-SCENARIOS are
    bare by design (single phase → no collision possible).
  ✓ No orphan atomic: an API/XM/TC directly under a PHASE that also has SUB
    children would never be written to a package file — rejected here.

THRESHOLD (config.PHASE_SPLIT_THRESHOLDS) — AUTO, but FLEXIBLE:
  A phase whose countable atomic marker reaches its split trigger (Section
  5.7.4) but carries no SUB — or a never-split phase that carries a SUB — is
  reported. This is ADVISORY by default (a non-blocking warning: the split
  decision is ultimately semantic) and becomes BLOCKING only with
  --strict-thresholds. Countable triggers only: SVC-API (APIs ≥ 8), INT-C /
  INT-R (XMs ≥ 5), TEST-PLAN-BE (TCs > 12). DATA-DOM's entity trigger is not
  marker-countable and is left to the generating engine.
```

Standalone usage (no module structure required), the mandatory closing step
right after any file is generated:

```
python3 agent3_splitter.py --validate-markers --file backend-execution-plan.md
python3 agent3_splitter.py --validate-markers --file backend-test-plan.md
```

Exit 0 + "marker structure valid" on success; exit 1 + a line-numbered list of
every structural/semantic violation on failure. Treat any non-zero exit as
blocking.

Deterministic self-repair (safe subset only):

```
python3 agent3_splitter.py --fix-safe --file backend-execution-plan.md
```

`--fix-safe` repairs ONLY unambiguous, reversible marker faults — a phase-key
separator typo (SVC_API → SVC-API) and un-qualified SUB labels
(SUB:CRUD → SUB:SVC-API-CRUD) — writing the untouched original to <file>.orig
and re-validating. It NEVER touches content and NEVER attempts anything needing
judgment (unmatched/unclosed markers, duplicate IDs, orphan atomics, an
ambiguous key like DATADOM, threshold restructuring); those are reported for a
human and it exits 1. This is what lets the Cowork orchestrator
(process-project-files.md) self-heal safe faults automatically and stop only on
the rest.

---

## 4 — WORKFLOW (single backend run)

```
STEP 0    Generate backend artifacts via the engines in claude.ai Projects:
          P0 → [P0.5] → P1 → P2 → [P2.5] → P3.1 (backend-execution-plan.md).
          backend-test-plan.md is generated by the Test Generation Engine
          (after ALIGN-BE ✓). Markers are embedded at generation time.

STEP 0.5  Validate each file immediately (see 3A):
          python3 agent3_splitter.py --validate-markers --file <file>
          Fix any reported error and re-run before archiving.

STEP 1    Agent 1 — create structure
          python3 agent1_create_structure.py --module ORG
          → creates modules/ORG/{P0,P0_5,P1,P2,P2_5,P3_1,P3_5_BE}/ and
            packages/{backend-execution/<phase-folders>, backend-test/}.

STEP 2    Agent 2 — archive artifacts
          python3 agent2_archive.py --module ORG --source ~/Desktop/ORG-files
          → copies platform-summary.md, [prd-org.md], srs.md, db-script.md,
            flow-diagram.md, ui-ux-spec.md, backend-execution-plan.md,
            backend-test-plan.md, test-execution-manifest.md, and the P-REG
            registry-*.md files. Existing files are KEPT unless --force.
          (Agent 2 auto-creates the structure if Agent 1 was not run first.)

STEP 3    Agent 3 — split (5 stages, each approve-gated, resumable)
          python3 agent3_splitter.py --module ORG
          Stage 1: Parse + validate (structural AND semantic), show a plan
          Stage 2: Split backend-execution-plan.md → packages/backend-execution/
                    (one folder per phase; SUB files named by their already
                     phase-qualified label; unmarked trailing content captured
                     as packages/backend-execution/_SECTIONS.md)
          Stage 3: Split backend-test-plan.md → packages/backend-test/
                    (flat files: RULE-SCENARIOS.md / API-SCENARIOS.md, or one
                     whole-phase file below the TC>12 threshold)
          Stage 4: Generate index.md per package folder
          Stage 5: Verify — SHA-256 hash of every API/XM/TC block in the
                    archived source vs the same block re-parsed from its
                    package file. Any mismatch names the exact ID and file.

STEP 4    Downstream agent (Claude Code) implements the backend, consuming
          individual package files directly — e.g. open
          packages/backend-execution/SVC-API/SVC-API-CRUD.md instead of the
          full backend-execution-plan.md.
```

Every Agent 3 stage requires `[y/N]` confirmation and is resumable
(`--resume`, `--status`, `--stage N`, `--dry-run`). State persists in
`packages/_agent3-state.json`.

---

## 5 — PACKAGE LAYOUT (proportional to STRUCTURE, not element COUNT)

```
modules/ORG/packages/
├── backend-execution/
│   ├── CORE/           CORE.md
│   ├── DATA-DOM/       DATA-DOM-<group>.md ...
│   ├── SVC-API/        SVC-API-CRUD.md, SVC-API-SEARCH.md, [SVC-API-HEADER.md]
│   ├── DOC/            DOC.md
│   ├── INT-C/          INT-C-<module>.md ...
│   ├── INT-R/          INT-R-<module>.md ...
│   ├── SEC-BE/         SEC-BE.md
│   ├── ALIGN-BE/       ALIGN-BE.md
│   ├── _SECTIONS.md    (content outside all phases — Plan Index, Error
│   │                    Catalog, Agent Handoff Summary, etc. — if present)
│   └── index.md
└── backend-test/
    ├── RULE-SCENARIOS.md    (or TEST-PLAN-BE.md whole-phase if TCs ≤ 12)
    ├── API-SCENARIOS.md
    └── index.md
```

Key principle: a module with 104 TCs still produces a handful of package
files — file count tracks phase/sub-phase count, NEVER the number of
APIs/TCs. Atomic markers stay embedded inside each grouped file for in-file
search; they are addressing, not a split instruction.

---

## 6 — GUARANTEES

```
No content rewriting     Stage 2/3 do pure copy from parsed block content —
                          no LLM, no text transformation, anywhere.
No content loss          Stage 5 SHA-256 cross-checks every atomic block.
                          Content outside all phases is captured as _SECTIONS.md
                          rather than dropped.
Validity before write    marker_parser rejects structural AND semantic faults,
                          with line numbers, BEFORE Stage 2/3 write anything.
No silent phase skip      A non-canonical PHASE key is a blocking error, not a
                          soft skip that drops the phase.
Truthful archival        Agent 2 keeps existing files unless --force, and says
                          so; it reports found / copied / kept / skipped exactly.
Approve-gated, resumable  Every Agent 3 stage confirms [y/N]; state persists.
Dry-run is read-only      Agent 1 --dry-run never registers a module or writes
                          any file (FINDING-22a).
Absent stages never break There is no P4/audit stage anywhere in the model
                          (BACKEND_STAGES = P0..P3_5_BE only). A missing
                          optional artifact is a graceful skip, not an error —
                          e.g. no backend-test-plan.md yet → Stage 3 skips and
                          the run still completes. A leftover P4* folder from a
                          legacy module is simply ignored: the tools only ever
                          act on stages their own config.py knows.
```

---

## 7 — TEST SUITE

```
python3 -m pytest tests/            (from governance-tools/)
```

Covers: structural parsing (unclosed / unmatched / illegal nesting /
duplicate id), the semantic validators (canonical keys, SUB qualification,
orphan atomics, test-plan exemption, no-SUB validity), the full Agent 3
split pipeline (correct SUB filenames, _SECTIONS capture, flat test files,
hash verify), the Agent 1 dry-run registry guard, and the Agent 2 force
behavior. All are hermetic (tmp dirs / isolated registry) — they never touch
the real backend/governance tree.

---

## 8 — AMENDMENT RECORD — AMEND-P3-O (backend Stage-2 maturation)

Resolved in this amendment (all verified by the test suite):

```
ID          FINDING                                    RESOLUTION
────────────────────────────────────────────────────────────────────────────
C1          Agent 3 Stage 2 re-prefixed already-       Filename = the SUB's own
            phase-qualified SUB labels, producing      (already qualified) label.
            SVC-API-SVC-API-CRUD.md.
FINDING-19  backend-test SUBs pre-created as FOLDERS    config: backend-test is a
            but written by Agent 3 as flat FILES —      container only; files land
            the folders were always dead.              flat inside it.
FINDING-22a Agent 1 --dry-run + --auto-register wrote   Dry-run validates
            the registry (and shared copy) despite     read-only; registration
            "no changes".                              deferred to a live run.
C4          Content outside all phases (Plan Index,     Captured as _SECTIONS.md;
            Error Catalog, Agent Handoff Summary)      dead SECTIONS folder removed.
            was silently dropped by the splitter.
M2          Non-canonical PHASE key / un-qualified      New semantic validator
            SUB / orphan atomic passed validation      (blocking) in Stage 1 and
            and caused silent loss.                    --validate-markers.
M4          Agent 2 --force was inert; the "won't       Existing files kept unless
            overwrite without --force" note was false. --force; messages truthful.
M1          ALLOWED_PARENTS defined twice (config +     Single source in config;
            parser) — drift hazard.                    parser imports it.
(parser)    Tokenizer captured only the FIRST marker    finditer across all
            per line — a latent bug on any shared line. patterns, ordered by column.
M7          No tests for the tooling.                   pytest suite added (tests/).
M3          No auto threshold check — an over-threshold   config.PHASE_SPLIT_
            unsplit phase passed unnoticed.              THRESHOLDS + advisory
                                                          check (flexible; strict
                                                          via --strict-thresholds).
D1/D2       STAGE-2 doc + AGENTS-GUIDE described the    Both rewritten to match the
            pre-split (P0–P4, MARK) world.             current backend-only reality.
```

---

## 9 — WHAT STAGE 2 DOES NOT DO

```
✗ Does not generate, rewrite, summarize, or validate governance CONTENT.
✗ Does not HARD-block on SUB-phase thresholds by default — a phase over its
  trigger but unsplit is an ADVISORY (the split is a semantic call). Use
  --strict-thresholds to make it blocking. It always enforces that whatever
  SUBs exist are well-formed and phase-qualified.
✗ Does not call any LLM API.
✗ Does not modify any archived source artifact — archived copies are
  read-only inputs to Agent 3.
```

---

## 10 — IMPACT CHECKLIST FOR FUTURE EDITS

```
[ ] Change marker syntax in PROJECT-3-REGISTRY.md §5.7
      → config.MARKERS + tests
[ ] Add/rename a backend PHASE key
      → config.CANONICAL_PHASE_KEYS + config.PACKAGES_STRUCTURE
        + agent3_splitter.PHASE_FOLDER_MAP + tests
[ ] Change SUB-phase thresholds
      → config.PHASE_SPLIT_THRESHOLDS (one row per phase) + tests. Advisory
        by default; --strict-thresholds makes them blocking.
[ ] Change canonical artifact filenames / registry filename
      → config.ARTIFACT_FILES (+ CANONICAL_PHASE_KEYS keys if a plan file
        is renamed)
[ ] Add a new atomic marker type beyond API/XM/TC
      → config.MARKERS + config.ALLOWED_PARENTS + agent3 grouping + the
        semantic orphan check + tests
[ ] Add a module
      → no code change — use --auto-register (or add to config.KNOWN_MODULES)
```

*End of STAGE-2-GOVERNANCE-TOOLS.md — backend-only companion to*
*PROJECT-3-REGISTRY.md Section 5.7. Keep code and this doc in lockstep.*
