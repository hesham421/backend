# STAGE 2 — GOVERNANCE TOOLS (Marker Packaging & Archival Layer)

```
Status        : IMPLEMENTED ✓
Depends on    : P3 Section 6.7 (Artifact Marker Protocol)
Scope         : Post-generation tooling — runs AFTER P0–P4 artifacts exist
Location      : governance-tools/ (Python scripts — local execution,
                 typically via Claude Code on macOS)
Relationship  : Independent of P0–P4 project instructions.
                 Reads their OUTPUT artifacts. Never modifies governance
                 logic, never regenerates content, never makes decisions
                 P0–P4 are responsible for.
```

This document exists so that any future edit to P0, P1, P2, P3, or P4 —
especially to Section 6.7 (Marker Protocol) or Section 16 (test-plan
structure) of P3 — accounts for what Stage 2 tooling expects and depends
on. A change to marker syntax, phase names, or threshold rules in P3
will silently break Stage 2 tooling unless this document is consulted.

---

## 1 — WHY STAGE 2 EXISTS

The five governance engines (P0–P4) produce text artifacts
(`platform-summary.md`, `srs.md`, `db-script.md`, `execution-plan.md`,
`test-plan.md`, `audit-report.md`). For a mid-complexity module these
artifacts can reach thousands of lines (a real `test-plan.md` for module
ORG measured 1264 lines containing 104 TCs).

Two problems emerge once a module's artifacts are finalized:

```
PROBLEM 1 — Repository organization
  Where do finalized artifacts live? How are they archived per module,
  per version, without manual file-juggling?

PROBLEM 2 — Context window cost for downstream agents
  An implementation agent (Claude Code) that only needs
  API-ORG-014 should not have to load an 8000-line execution-plan.md
  to find it.
```

Stage 2 solves both — without touching what P0–P4 generate. It is a
**packaging and archival layer**, not a content-generation layer.

---

## 2 — TWO-PART DESIGN

```
PART A — Marker Governance (lives INSIDE P3, Section 6.7)
  P3 embeds HTML comment markers into execution-plan.md and test-plan.md
  AT GENERATION TIME. This is a P3 responsibility, not a Stage 2 script.

PART B — Artifact Packaging & Archival (Stage 2 tooling — this document)
  Independent Python scripts that:
    1. Create the canonical folder structure for a module (Agent 1)
    2. Archive generated artifacts into that structure (Agent 2)
    3. Read the markers P3 embedded and split artifacts into smaller,
       addressable package files (Agent 3)
```

Stage 2 tooling is entirely dependent on Part A. If P3's marker syntax
changes, Stage 2 tooling must change in lockstep — they are coupled by
contract, not by accident.

---

## 3 — COMPONENTS

```
governance-tools/
├── config.py                    Shared configuration — single source of
│                                 truth for repo path, known modules,
│                                 folder structure, artifact filenames,
│                                 marker regex patterns, manifest schema.
├── marker_parser.py             Marker parsing engine. Reads HTML comment
│                                 markers, builds a nested tree, validates
│                                 structural rules (Section 6.7.6).
├── agent1_create_structure.py   Creates the folder structure for a module
│                                 (or a new version of an existing module).
├── agent2_archive.py            Copies generated P0–P4 artifact files from
│                                 a source folder into the canonical structure.
└── agent3_splitter.py           Staged, approve-gated splitter. Reads
                                  markers and groups content into package
                                  files for downstream agent consumption.
```

All five files are plain Python 3.10+, no external dependencies beyond
the standard library. They run locally (typically on macOS via Terminal,
Claude Code) — they are NOT Claude Projects and do
NOT call any LLM API. They are deterministic file operations only.

---

## 4 — DEPENDENCY ON P3 SECTION 6.7 (CRITICAL)

Stage 2 tooling's correctness depends entirely on P3 Section 6.7 being
followed exactly as specified when execution-plan.md and test-plan.md
are generated. The following P3 rules are hard dependencies:

```
DEPENDENCY                          WHERE IT'S USED IN STAGE 2
──────────────────────────────────────────────────────────────────────
Marker syntax:
  <!-- PHASE:{id}:START/END -->     marker_parser.py MARKERS regex
  <!-- MARK:{JUNIT|PLAYWRIGHT}      (config.py) — exact syntax match
       :START/END -->               required; any deviation breaks
  <!-- SUB:{id}:START/END -->       parsing silently or raises errors.
  <!-- API:{id}:START/END -->
  <!-- XM:{id}:START/END -->
  <!-- TC:{id}:START/END -->

Hierarchy rules (Section 6.7.6     marker_parser.py ALLOWED_PARENTS —
  Rule 2):                          used to validate nesting and reject
  execution-plan: PHASE→[SUB]→ATOM  malformed artifacts before any
  test-plan: PHASE→MARK→[SUB]→ATOM  splitting is attempted (Stage 1).

Canonical phase keys                agent3_splitter.py PHASE_FOLDER_MAP
  (Section 6.7.3):                  — maps each PHASE marker_id to a
  CORE, DATA-DOM, SVC-API, DOC,     package output folder. Adding a new
  INT-C, INT-R, F1, F2, F3, F4,     phase key in P3 requires adding it
  SEC, ALIGN                         here too.
  [F4 added — AMEND-P3-J, 2026-07 — Frontend Routing & Component
  Structure — positioned between F3 and SEC. CONFIRMED: agent3_splitter.py's
  PHASE_FOLDER_MAP has an F4 entry, and Stage 1-5 have been run end-to-end
  against FILESVC and NOTIFICATION (both post-amendment) with zero
  structural errors and zero content drift on Stage 5 verification.
  execution-state.json for both modules also carries an F4 phase entry
  (PENDING, between F3 and SEC) and PLAYWRIGHT's gated_by_phases includes
  F4 — see Section 10 checklist below.]

SUB-phase thresholds                Stage 1 plan display only references
  (Section 6.7.4):                  these as documentation — actual
  DATA+DOM ≥5 entities,             threshold ENFORCEMENT happens in P3
  SVC+API ≥8 APIs/≥6 methods,       at generation time, not in Stage 2.
  INT-C/R ≥5 XM-IDs,                Stage 2 simply reads whatever
  F1/F2/F3/F4 ≥5 screens,           structure P3 actually produced.
  MARK:JUNIT >12 TCs,
  MARK:PLAYWRIGHT >8 TCs

Atomic markers are addressing,      agent3_splitter.py Stage 2/3 — groups
NOT a file-splitting instruction    content at SUB/PHASE/MARK level,
  (Section 6.7.5, amended):         NEVER writes one file per API/XM/TC.
                                     This was a real bug found and fixed
                                     (see Section 8 — Lessons Learned).
```

**If P3 Section 6.7 is edited in the future** (new phase key, changed
threshold, changed marker syntax, new atomic marker type), this Stage 2
documentation and the corresponding code in `config.py` /
`marker_parser.py` / `agent3_splitter.py` MUST be updated together.
They are not independently versioned.

---

## 5 — WORKFLOW

```
STEP 0   Generate artifacts via P0 → P1 → P2 → P3 (Stage 1 + Stage 2.5)
         as usual, in claude.ai Projects. Markers are embedded by P3
         automatically per Section 6.7 — no manual step required.

STEP 1   Agent 1 — create structure
         python3 agent1_create_structure.py --module ORG
         → creates governance-repo/modules/ORG/{P0,P1,P2,P3,P3_5,P4,packages}/

STEP 2   Agent 2 — archive
         python3 agent2_archive.py --module ORG --source ~/Desktop/ORG-files
         → copies platform-summary.md, srs.md, db-script.md,
           execution-plan.md, test-plan.md, audit-report.md, registry
           files, and master-registry.md into the structure created
           in Step 1. Source filenames must match config.py's
           ARTIFACT_FILES canonical names exactly.

STEP 3   Agent 3 — split (5 stages, each requires explicit approval)
         python3 agent3_splitter.py --module ORG
         Stage 1: Parse & validate marker structure, show a plan
         Stage 2: Split execution-plan.md → packages/execution/
         Stage 3: Split test-plan.md      → packages/test/
         Stage 4: Generate index.md per package folder
         Stage 5: Verify completeness + content-hash integrity
                  (detects any drift, even a single corrupted line
                  inside a grouped file containing 40+ TCs)

STEP 4   Downstream agents (Claude Code) consume
         individual package files directly — e.g. open
         packages/test/JUNIT/RULE-SCENARIOS.md instead of the full
         1264-line test-plan.md.
```

Every stage in Agent 3 is independently resumable (`--resume`,
`--status`, `--stage N`) and is approve-gated — no file is written
without explicit user confirmation at each stage.

---

## 6 — OUTPUT STRUCTURE

```
governance-repo/
├── master-registry.md                 (shared — not per-module)
├── modules-registry.json              (tracks all modules + versions)
└── modules/
    └── ORG/                            (v1 — or ORG/v2/, v3/... for
        │                                later versions of the same module)
        ├── manifest.json
        ├── P0/  P1/  P2/  P3/  P3_5/  P4/    (archived full artifacts —
        │                                       untouched, exactly as
        │                                       generated by P0–P4)
        └── packages/
            ├── execution/
            │   ├── CORE/CORE.md
            │   ├── SVC-API/SVC-API.md          (or split per SUB if the
            │   ├── DATA-DOM/...                 phase exceeded threshold)
            │   └── ...
            └── test/
                ├── JUNIT/
                │   ├── RULE-SCENARIOS.md        (grouped — many TCs,
                │   │                              one file, per Section
                │   │                              6.7.5 amendment)
                │   └── API-SCENARIOS.md
                └── PLAYWRIGHT/
                    ├── UI-FLOWS.md
                    └── INT-FLOW.md
```

Key principle: **package file count is proportional to module STRUCTURE
(phase/sub-phase count), never to element COUNT (number of APIs/TCs)**.
A module with 104 TCs produces 4 test package files, not 104.

---

## 7 — GUARANTEES

```
GUARANTEE                          MECHANISM
──────────────────────────────────────────────────────────────────────
No content rewriting               Stage 2/3 do pure copy/paste from
                                    parsed block.content — no LLM call,
                                    no text transformation, anywhere in
                                    the splitting path.

No content loss                    Stage 5 computes SHA-256 hash of every
                                    atomic block (API/XM/TC) as it exists
                                    in the archived source, and compares
                                    it against the same block re-parsed
                                    from inside its package file. Any
                                    mismatch — even one corrupted word
                                    inside a 46-TC grouped file — is
                                    reported with the exact TC-ID and file.

Structural validity enforced       marker_parser.py rejects (with line
before any write                   numbers and clear messages) any
                                    unmatched START/END, illegal nesting,
                                    or duplicate ID, BEFORE Stage 2/3
                                    write a single file.

No silent partial archiving        Agent 2 reports exactly which files
                                    were found/copied/skipped; manifest.json
                                    tracks archived_files and skipped_files
                                    explicitly.

Approve-gated, resumable           Every stage in Agent 3 requires [y/N]
                                    confirmation. State is persisted in
                                    packages/_agent3-state.json — a failed
                                    or cancelled run can resume from the
                                    next incomplete stage without redoing
                                    completed work.

Module/version scalability         New modules: --auto-register flag
                                    registers them into modules-registry.json
                                    without editing code. New versions of
                                    an existing module: --new-version
                                    creates v2/v3/... alongside v1 without
                                    overwriting it.
```

---

## 8 — LESSONS LEARNED (for future editors of P3 or Stage 2)

```
ISSUE FOUND                         RESOLUTION
──────────────────────────────────────────────────────────────────────
Initial Agent 3 implementation      P3 Section 6.7.5 was amended with an
wrote ONE FILE PER ATOMIC MARKER    explicit "Atomic Markers are
(API/XM/TC). For a real module      addressing, NOT a file-splitting
with 104 TCs this produced 104      instruction" rule. Agent 3 Stage 2/3
separate files — defeating the      were rewritten to group all atomic
entire purpose of reducing          content at the SUB (or PHASE/MARK if
context-window cost, and            no SUB) level into ONE file, with
contradicting the explicit          atomic markers remaining embedded
requirement that file count must    inside for in-file searchability.
not scale with element count.       Stage 5's verification logic was
                                     correspondingly rewritten to search
                                     for a marker's content INSIDE
                                     package files rather than expecting
                                     a 1:1 filename match.

argparse required=True on           Fixed: --module made optional with a
--module conflicted with the        manual required-check placed AFTER
standalone --list-modules flag      parse_args(), executed only when
in Agent 1 (argparse evaluates      --list-modules was not requested.
required fields before any code     This pattern should be followed for
runs, so --list-modules could       any future "standalone mode" flag
never be reached).                  added to any agent's CLI.

SUB markers were initially          Section 6.7.4 was corrected so SUB
described as unconditional          markers in test-plan.md follow the
inside MARK:PLAYWRIGHT in an        same threshold-triggered rule as
early draft of Section 6.7.4 —      every other phase: MARK:JUNIT >12
contradicting the general           TCs, MARK:PLAYWRIGHT >8 TCs — SUB
"semantic split, not arbitrary      omitted entirely below threshold.
split" principle agreed for the
whole Marker Protocol.
```

Any future change to Section 6.7 of P3 should be cross-checked against
this lessons-learned table to avoid reintroducing a previously-fixed
class of bug.

---

## 9 — WHAT STAGE 2 DOES **NOT** DO

```
✗ Does not generate, rewrite, summarize, or validate governance CONTENT
  (that is exclusively P0–P4's responsibility).
✗ Does not enforce SUB-phase thresholds — it reads whatever P3 already
  produced. If P3 fails to apply a threshold correctly, Stage 2 will
  faithfully package the (possibly non-compliant) result as-is; it is
  not a substitute for P4's CHECK-4 structural audit.
✗ Does not call any LLM API — purely deterministic file/text operations.
✗ Does not replace P4 (Governance Audit Engine). P4 audits CONTENT
  correctness and cross-artifact alignment; Stage 2 only verifies that
  packaging preserved content byte-for-byte.
✗ Does not modify execution-plan.md, test-plan.md, or any other archived
  source artifact — archived copies in P0–P4 folders are read-only
  inputs to Agent 3.
```

---

## 10 — IMPACT CHECKLIST FOR FUTURE EDITS

Before changing any of the following in P0–P4, check this box:

```
[ ] Changing P3 Section 6.7 marker syntax
      → update marker_parser.py MARKERS regex + config.py MARKERS

[ ] Adding/renaming a PHASE key in P3 (e.g. new phase beyond CORE,
    DATA-DOM, SVC-API, DOC, INT-C, INT-R, F1, F2, F3, F4, SEC, ALIGN)
      → update agent3_splitter.py PHASE_FOLDER_MAP
      [DONE for F4 — AMEND-P3-J, Frontend Routing & Component Structure,
      positioned between F3 and SEC. PHASE_FOLDER_MAP carries a confirmed
      F4 entry; Stage 1-5 verified end-to-end against FILESVC and
      NOTIFICATION 2026-07-15. Note: source artifacts must use the plain
      `<!-- PHASE:F4:START/END -->` marker (no "MARK:" prefix) — both
      modules initially had every phase marker mistakenly written as
      `<!-- MARK:PHASE:{id}:START/END -->` while drafting the F4 section,
      which the parser silently fails to recognize (0 phase blocks found,
      no error raised). Caught and corrected before splitting. ORG's
      execution-plan.md already has an F4 phase from an earlier session
      but its execution-state.json/PHASE_FOLDER_MAP alignment has not been
      re-verified against this amendment — check before running Agent 3
      on ORG again.]

[ ] Changing SUB-phase thresholds in Section 6.7.4
      → update this document's Section 4 dependency table
        (no code change needed — Stage 2 reads structure, doesn't
        enforce thresholds — but documentation must stay accurate)

[ ] Changing test-plan.md MARK values beyond JUNIT/PLAYWRIGHT
      → update marker_parser.py MARKERS["mark"] regex + config.py

[ ] Adding a new atomic marker type beyond API/XM/TC
      → update marker_parser.py ALLOWED_PARENTS + agent3_splitter.py
        grouping logic (apply the same "group at container level,
        not per-element" principle from Section 7/8 of this document)

[ ] Changing P0–P4 canonical artifact filenames
      → update config.py ARTIFACT_FILES

[ ] Adding a new module
      → no code change needed — use --auto-register, or add to
        config.py KNOWN_MODULES for a permanent static entry
```

---

*End of STAGE-2-GOVERNANCE-TOOLS.md*
*Companion document to PROJECT-3-EXECUTION-PLAN-GOVERNANCE-ENGINE.md
Section 6.7 (Artifact Marker Protocol).*
*Any edit to Section 6.7 or Section 16 of P3 should be cross-checked
against this document before being finalized.*