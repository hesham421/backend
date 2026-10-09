# Smoke run — pipeline review findings

Subject: module `NOTE` (Notes) · one entity · four operations · one screen.
Baseline measured before any change: `lint 0 critical · 0 major · 0 minor` ·
`237 passed, 1 skipped`.

## F-1 — `lint` reported a clean verdict over a freshness check that never ran
Where   : governance-tools/lint.py:334 (`except ImportError: pass`)
Expected: invariant 3 — a check that cannot see the answer says so. An
          unrunnable `C1-stale-render` must be reported, at a blocking rank.
Actual  : the import of `render` was wrapped in `try/except ImportError: pass`.
          Reproduced by stubbing the import and tampering a rendered block in
          README.md: a genuinely stale generated file was present and
          `lint.run()` returned `{'CRITICAL': 0, 'MAJOR': 0, 'MINOR': 0}` —
          0 findings. The clean verdict was produced by a check that examined
          nothing, which is the precise failure the invariant names.
Fix     : the `except` now appends `C1-render-unavailable` at
          `sev(RENDER_UNAVAILABLE_RANK)` (rank 1 — the same rank the check it
          replaces charges, so it blocks exactly where a stale render would)
          and states in its message that the verdict says nothing about
          freshness. The check was NOT weakened — it was made able to speak.
          Pinned by `tests/test_silent_success.py::
          test_lint_says_so_when_it_cannot_run_the_freshness_check`, in the
          suite that already owns this defect class.
Status  : FIXED

## F-2 — `lint` ⇄ `render` import cycle, held open by two lazy imports
Where   : governance-tools/lint.py:347 · governance-tools/render.py:254
Expected: the module graph is a DAG — a cycle means neither module can be
          changed alone.
Actual  : `lint` imported `render` (for `check_fresh`) and `render` imported
          `lint` (for `Finding` — one 10-line dataclass with zero
          dependencies, out of a 366-line checker). Both sides were function-
          local imports whose comments explained each other
          ("local import: lint imports render lazily too"), i.e. the cycle was
          known and worked around rather than cut.
Fix     : the same cut the `analyze`→`render` edge got: `findings.py`
          (31 lines, one dependency: `dataclasses`) now holds `Finding`;
          `lint` re-exports it, so no call site changed. `render` imports
          `findings` at module top and no longer imports a checker at all.
          Graph re-measured after the cut: no cycles.
Status  : FIXED

## F-3 — `render.check_fresh` spelled a severity name as a literal
Where   : governance-tools/render.py:258
Expected: C1/C2 — the severity vocabulary and its order live in
          factory.yaml → `analyze.severities`; a checker charges by RANK.
          `lint.py` states this in its own imports ("lint charges by rank,
          never by a name spelled here") and every other checker obeys it.
Actual  : `Finding("MAJOR", "C1-stale-render", …)` — the string `MAJOR` typed
          into code. Narrowing or reordering `analyze.severities` would leave
          this one finding charged at a name that may no longer be declared,
          and `severity_rank()` sorts an undeclared severity after every
          declared one, so it would silently stop blocking.
          `lint.code_paths` does not catch it: that scan looks for stage ids,
          phase keys and ID prefixes, not severity names.
Fix     : `sev(STALE_RENDER_RANK)` with the rank named once as a module
          constant and documented. Found by inspection, not by a check — the
          gap in `scan_code_literals` is itself recorded as F-4.
Status  : FIXED

## F-4 — the no-hardcode scan does not cover severity names
Where   : governance-tools/lint.py `scan_code_literals` · factory.yaml `lint.code_paths`
Expected: C2 says engines, docs, reviewers and tools reference config keys,
          "never literals — no stage id, phase key, ID prefix, stack name,
          path, model name or domain word". `analyze.severities` is a
          factory fact of exactly the same kind, and it decides what blocks.
Actual  : the code scan enumerates stage ids / phase keys / ID prefixes only.
          F-3 was a severity name sitting in `governance-tools/` — inside
          `lint.code_paths` — for as long as that file has existed, and lint
          passed `0 critical · 0 major · 0 minor` over it every run.
Fix     : OPEN — deliberately not fixed here, because the two defensible
          options change different things and neither is groundable in the
          repo as written:
          (a) add `analyze.severities` to the code-literal scan. Cheap, but it
              would fire on `toolkit/common.py`, which must name them to
              resolve a rank, so it needs an owner-exemption concept that does
              not exist yet;
          (b) extend `lint.profile_term_sources` with a factory-side twin
              (`factory_term_sources`) so any factory.yaml value can be
              declared scannable as data. Larger, and the right shape, but it
              is a new config surface and C5 says variability is declared in
              `_schema.yaml` — which today validates profiles only.
          Recorded rather than guessed at. F-3's instance is fixed either way.
Status  : OPEN

## F-5 — the backend generator writes `api_docs_path` into the backend repo
Where   : backend/.claude/commands/generate-module-setup.md:91 and :190
Expected: invariant 1 — api-docs exist in exactly one place. The ownership
          table (GOVERNANCE-SHARED-DESIGN.md §3) gives `backend/modules/*/
          api-docs/` in the SHARED repo one writer, and factory.yaml
          `repos.backend.publishes.api-docs` resolves against
          `reads_from: shared`. So a generated `api_docs_path` must read
          `governance/shared/backend/modules/{MOD}/api-docs/`.
Actual  : both lines derived it from `$MBASE`, which Step 0.5 of the same file
          resolves to `governance/modules/$MODULE/` (or `…/v$N/`) — a path
          INSIDE the backend repo. Every module generated by this command
          would declare a second api-docs location, and a vN module would
          declare a per-version one, where api-docs are derived from the
          running app and have no version at all.
          The contradiction is INSIDE this one file (C3): its own STEP 0.3
          and test-phase input list (:408, :448) already name
          `governance/shared/backend/modules/[MODULE]/api-docs/`. The
          frontend twin, `generate-frontend-module-setup.md:218`, has always
          written the shared path — the backend generator was the only
          outlier, which is why the shipped `execution-state.json` files
          carry the correct shared path: they were corrected BY HAND after
          generation. That hand-correction is the cost this defect was
          charging, once per module, invisibly.
Fix     : both lines now name the shared path directly and no longer derive
          from `$MBASE`, with the reason stated inline (one copy; not
          version-suffixed). Verified by re-reading the generated
          `execution-state.json` for NOTE after `/generate-module-setup`.
Status  : FIXED

## F-6 — two writers target one `execution-state.json`
Where   : governance-tools/gov.py:755 (`cmd_deliver`) vs
          backend/.claude/commands/generate-module-setup.md:182 (Step 2)
Expected: GOVERNANCE-SHARED-DESIGN.md §1 — "one writer per path, no
          exception" — and §3's ownership table, which gives
          `*/execution-state.json` exactly one writer: **the factory**, via
          `gov.py deliver`. factory.yaml agrees:
          `delivery.execution_state.schema` is annotated "generated by
          `gov.py deliver`, never hand-written".
Actual  : both tools resolve to the SAME path.
          `cmd_deliver` writes `dest / "execution-state.json"` where
          `dest = <backend>/governance/modules/{MOD}`; the generator's Step 2
          writes `$MBASE/execution-state.json`, and Step 0.5 resolves
          `$MBASE` to `governance/modules/$MODULE/`. Identical.
          The schemas are disjoint, so this is a replacement, not a merge.
          Factory fields: module, version, track, profile,
          markers_schema_version, packages, phases, traceability, analyze,
          gate, generated_at. Generator fields: module, generated_at,
          current_phase, current_sub, api_docs_path, phases, test_phases,
          blocked, deferred_xm, api_doc_gaps.
          Measured on all three delivered modules — every one carries the
          GENERATOR's key set and none of `version`, `track`, `profile`,
          `packages`, `traceability`, `analyze`, `gate`:
            SEC/FIN/MDL -> [api_doc_gaps, api_docs_path, blocked,
            current_phase, current_sub, deferred_xm, generated_at, module,
            phases, test_phases]
          What is lost is precisely the delivery's audit trail: which gate
          verdict and which analyze counts the module passed, and which
          toolchain built it. The pipeline REQUIRES the overwriting command
          to run (orchestrate refuses a module without it), so the loss is
          not a mishap — it is the designed order of operations.
          NOT yet proven as a live overwrite: SEC/FIN/MDL predate this
          `cmd_deliver` (their file's first git revision is already the
          generator's schema, in the repo's `first` commit), so for them the
          factory record never existed rather than being destroyed. NOTE is
          the first module where both tools run in sequence — verified below
          in F-6a once `deliver` and `/generate-module-setup` have both run.
Fix     : OPEN — two defensible repairs, and the choice is not groundable in
          the repo as written:
          (a) STRUCTURAL: the generator writes its own file (runtime progress
              is a different concern from the delivery record), restoring one
              writer per path literally. Correct, but it renames a file that
              `orchestrate-module.md` and every generated per-module command
              in TWO repos reads, so it cannot be done from the factory side
              alone.
          (b) PRESERVING: the generator reads the delivered file and carries
              the factory-owned block forward under its own key, adding only
              runtime fields. Restores the audit trail today and is
              reversible, but leaves two writers on one path — the letter of
              §1 still broken, the consequence fixed.
          Recorded with both, per the standing rule, rather than guessed at.
Status  : OPEN

## F-7 — `shared-pointer-fresh` is promised as CRITICAL and does not exist
Where   : GOVERNANCE-SHARED-DESIGN.md §6 and §8 vs governance-tools/gov.py:673
Expected: §8 names "building on stale api-docs without knowing" the biggest
          risk of the whole design and gives its mitigation as
          `shared-pointer-fresh` **(CRITICAL)**. §6 lists it, `partition-writer`
          and `api-docs-reachable` as the checks the factory's two-sided
          awareness buys.
Actual  : none of the three exists anywhere in the repo — grepped across the
          factory outside `history/` and `_archive-v5/`, the only hits are
          the four lines of the design document that promise them.
          What stands in for the first is an advisory print in `cmd_sync`:
            if behind != "0": _say("BEHIND — a consumer pinning this commit
                                    is building on stale inputs …")
            return OK
          — a CRITICAL mitigation implemented as a message with exit code 0.
          `cmd_fetch_inputs` is where it would matter, and it never consults
          it: it reads the shared checkout's working tree at whatever commit
          it happens to be on, with `--pull` optional, and folds it into
          `_inputs/` without a word about freshness. A stale fold is
          therefore indistinguishable from a current one at the one command
          that turns a pointer into an input.
          Likewise the stale consumer pointers `cmd_sync` collects into
          `stale` are printed and never reach the exit code.
Fix     : OPEN — not fixed, and deliberately not fixed by making `sync` exit
          non-zero, which would be the weakening-shaped move in reverse:
          `sync` is documented as a reporter (`sync --push` is the writer),
          and changing its exit code silently breaks any caller that treats
          0 as "ran". The check belongs where the risk is — a freshness gate
          inside `cmd_fetch_inputs`, which already returns BLOCKED for a
          missing input and would return it for a stale one. That is a new
          check, not a repair of an existing one, and the severity it should
          carry (§8 says CRITICAL; `analyze.blocking` makes MAJOR block too)
          is a policy call the documents do not settle.
Status  : OPEN

## F-8 — the human-approval gate approved a PRD that did not exist
Where   : governance-tools/gov.py:437 `approve()` · :452 `_artifact_shas()`
Expected: CONSTITUTION.md §2 — this gate is where "the user approves the PRD
          **file itself**". The approval record carries `artifact_sha`, a
          field whose whole purpose is to bind the decision to the bytes
          approved.
Actual  : with `erp/modules/NOTE/P0_5/prd-note.md` deleted,
            gov.py approve prd-approval -m NOTE -v 1 --by smoke-run
          printed `approved 'prd-approval' for NOTE v1 by smoke-run`, exited
          0, and wrote a record whose `artifact_sha` was `{}`.
          `_artifact_shas()` collects a hash only `if p.exists()`, and
          `approve()` never looks at what came back — so the one field that
          could have noticed recorded the emptiness and told nobody. The gate
          the Constitution calls a human decision point certified nothing,
          and the pass continued into P1 on that certificate.
Fix     : `approve()` now compares the shas it collected against the
          `produces` list of the gate's `after` stage and returns BLOCKED,
          naming the missing artifact and the path it looked at, when any is
          absent. Nothing was softened: the happy path still approves and now
          provably binds the record to the file
          (`artifact_sha: {'prd': '2bbcf3d33900d215…'}`). Values all come
          from `CFG.gate()` / `CFG.stage().produces`; no gate id, stage id or
          filename is spelled. Pinned by
          `test_the_human_approval_gate_refuses_to_approve_an_absent_artifact`.
Status  : FIXED

## F-9 — only the last dialogue round was ingested; the artifact survived by luck
Where   : governance-tools/dispatch.py:344 (`res.written = ingest(previous)`)
Expected: a dialogue stage's artifacts are what the run produced across its
          rounds. The documented runner contract is that the response carries
          the work as `<<<FILE: path>>>` blocks and `dispatch.ingest()` writes
          them.
Actual  : `ingest()` was called on `previous` — the LAST round — only, so
          every earlier round's blocks were discarded. The triggering shape is
          not exotic, it is the NORMAL one for a converging dialogue: the
          final round is a self-review that argues about the artifact instead
          of re-emitting it. P0.5 of this run was exactly that —
          `P0.5.response1.md` carries one `<<<FILE: …/prd-note.md>>>` block,
          `P0.5-round2.response2.md` carries none and only
          `<!-- CONVERGED -->` — and the orchestrator duly reported
          `dispatched P0.5: 2 round(s), converged=True, wrote 0 file(s)`
          while committing a PRD.
          The PRD existed only because the runner is `claude -p
          --permission-mode bypassPermissions`, an agent holding write tools,
          which had saved the file itself. It said so in the response:
          "Round 2 — written to `erp/modules/NOTE/P0_5/prd-note.md`
          (as operator, not a file block)." Under the contract as documented —
          a runner that only answers text — this stage would have produced
          nothing at all.
Fix     : `dispatch()` now ingests every round, oldest first, so a file
          emitted once survives the rounds that do not mention it. Verified
          against this run's own recorded responses: ingesting the last round
          alone recovers `[]`; ingesting both recovers `prd-note.md`.
Status  : FIXED

## F-10 — the shipped artifact is not reconstructible from the archived run
Where   : same two response files as F-9
Expected: a dialogue's briefs and responses are archived under
          `_state/briefs/` so a run can be re-derived and audited.
Actual  : found while fixing F-9, and it is why that fix needed a second
          half. Round 2's amendments — a `القصة :` line per story, `Status :
          DRAFT`, the corrected trace on US-NOTE-001 — are in the artifact on
          disk and in **no file block anywhere**: the round-1 block is the
          pre-amendment draft (11756 bytes vs the shipped 15186; `القصة`
          appears 7 times on disk and 0 times in the block). The archived
          responses therefore do not contain the delivered artifact, and a
          naive replay of them REPLACES it with a superseded draft — which
          the first version of the F-9 fix did, silently.
Fix     : partially, and the remainder is OPEN.
          FIXED half — `ingest()` no longer overwrites a target whose mtime is
          newer than the response carrying the block and whose content
          differs: a response cannot supersede a write that happened after it.
          Verified both directions: with the shipped artifact present nothing
          is ingested and its 15186 bytes are preserved; with it absent the
          round-1 block is recovered. Pinned by two tests
          (`test_a_file_emitted_in_an_earlier_round_is_not_discarded`,
          `test_a_block_never_overwrites_a_newer_out_of_band_write`).
          OPEN half — the provenance gap itself. The runner contract asks for
          file blocks; the runners actually dispatched to hold write tools and
          may legitimately not use blocks. Two ways to close it, neither
          groundable in the documents as written:
          (a) enforce the contract — dispatch refuses a round that changed a
              declared artifact on disk without emitting its block. Honest,
              but it makes the factory police a CLI it does not own;
          (b) drop the contract for agent runners and record a content hash of
              each declared artifact per round instead, so provenance is a
              digest chain rather than a transcript.
Status  : OPEN

## F-11 — `_state/` was added to and never pruned, so `analyze` read a dead copy
Where   : governance-tools/state.py:189 `build_state` · reported via
          governance-tools/analyze.py (C4.1 `exists`)
Expected: invariant 2 — a derivation is deterministic, a function of its
          sources. `factory.yaml` says `versioning.current_state: generated`
          and "engines read `_state` only".
Actual  : `build_state` wrote a `current-*` for every artifact it found and
          removed none for artifacts it did not. Deleting `prd-note.md` and
          re-running `gov.py state` printed `missing ['prd', …]` — it KNEW —
          and left `_state/current-prd.md` in place. `analyze`, which reads
          `_state`, then examined the dead copy and reported
          `0 critical · 0 major · 0 minor · CLEAN`, where with the copy gone
          it correctly reports `[CRITICAL] C4.1 (exists) prd — 'prd' is
          missing or empty` and BLOCKED. Two tools looking at one module,
          disagreeing, with only the quieter one believed.
          `versioning.delta_only: true` makes this routine rather than
          exotic: a vN whose change manifest REMOVES an artifact would keep
          passing the existence clause off v1's snapshot.
          Worth recording that the surrounding machinery is SOUND and was not
          the defect: `C4.1 exists` fires correctly, and the vacuous-clause
          reporter does its job, printing "1 clause(s) examined nothing
          (C4.4)" with a paragraph on how to read it. Invariant 3 is well
          served here — the input to it was stale.
Fix     : `build_state` now prunes any `current-*` in the state dir that is
          not among the copies this run wrote, and reports them in
          `StateReport.pruned` and in `state.json`. The glob comes from
          `naming.current_state_file`, so no filename is spelled. Pinned by
          `test_a_derived_state_copy_does_not_outlive_its_artifact`.
Status  : FIXED

## F-12 — a consumer repo must silently share its track's name
Where   : governance-tools/gov.py:739, 740, 855, 962, 966, 1017
Expected: an extension point should be config only, and the config should say
          what it requires. Adding a third consumer repo is one of the five
          extension questions this review is asked to answer.
Actual  : six call sites index the repo table with a TRACK name —
          `CFG.repos[track]`, `CFG.repo_checkout(track)` — so a track and its
          consumer repo must carry the identical key. Nothing declares that
          and nothing checks it. Measured on the live config:
            tracks : ['backend', 'frontend']
            repos  : ['backend', 'frontend', 'shared']
            tracks with no repo of the same name: []
            repos that are not tracks           : ['shared']
          A track whose repo key differs fails with an unhandled
          `KeyError: 'mobile'` rather than a message naming the two tables
          that disagree — and the profile already ships
          `stack.mobile.framework`, so a third track is a live prospect, not
          a hypothetical.
          This is NOT a C2 violation: no literal is typed, the value is read
          from config. It is an undeclared invariant between two config
          tables, which `profiles/_schema.yaml` cannot catch because C5 scopes
          it to profiles — `factory.yaml`'s own structure is validated by
          nothing.
Fix     : OPEN. The one-line repair (a lint rule asserting every
          `tracks.<k>` has a `repos.<k>`) is easy but picks a winner between
          two readings the documents do not settle: either a track IS a
          consumer repo and the tables should be merged, or a track MAY name
          its repo (`tracks.<k>.repo`, defaulting to `<k>`), which is the more
          flexible shape and the larger change. Recorded with both rather
          than guessed at.
Status  : OPEN

## F-13 — `run-pass` could not resume, and the pass is designed to stop
Where   : governance-tools/gov.py:343 `run_pass` (`for sid in p["stages"]`)
Expected: `gates.prd-approval` has `blocks: [P1]`, and pass 1's stages are
          `[P0, P0.5, P1, P2, P3.1]`. So EVERY module's pass 1 halts in the
          middle of itself at a human-approval gate, and re-invoking
          `run-pass 1` after the approval is the documented way forward —
          the smoke prompt's own command list does exactly that. Resuming is
          the normal path, not an edge case.
Actual  : `run_pass` looped over `p["stages"]` unconditionally, with no
          completion check anywhere in it or in `run_stage`. The resume
          re-dispatched P0 from scratch. Observed live: after
          `approve prd-approval`, the second `run-pass 1` rewrote
          `_state/briefs/P0.md` at 16:10:15 and left all three P0 artifacts
          modified in `git status` before it was stopped.
          Two costs, the second serious:
          · the two dialogue stages are the most expensive in the pipeline
            (~4 opus dispatches) and were paid again for no new information;
          · it rewrites the PRD the human has just approved. The approval
            record binds `artifact_sha` to those exact bytes (F-8), so a
            resume silently leaves an approval pointing at content that no
            longer exists — the gate would still read as passed.
Fix     : `run_pass` now skips a stage whose non-optional `produces` are all
          present and non-empty, printing what it skipped and how to redo it.
          `run-stage <id>` stays unconditional — that is how a single stage is
          re-run — and `--redo` restores the old whole-pass behaviour.
          Nothing is spelled: the artifacts come from `CFG.stage().produces`.
          Verified on the live run: the resume printed
            skipped P0: already produced platform-summary, module-registry,
                        business-policies
            skipped P0.5: already produced prd
          and the approved PRD's md5 was unchanged.
          LIMITATION, stated rather than left to be discovered: the skip rule
          is "the declared artifacts exist", not "they are still current". Edit
          the SRS by hand and re-run the pass, and P2/P3.1 are skipped though
          they should re-run. `state.is_fresh()` cannot close this — it
          compares one module-wide `inputs_mtime` against the newest file under
          the version root, and every stage commit moves that, so wiring it in
          would make `is_fresh` false on every resume and skip nothing at all.
          A per-stage freshness stamp (each stage recording the digests of the
          inputs it consumed) is the real answer and is a larger change than
          this defect warranted. `--redo` is the escape hatch meanwhile.
Status  : FIXED

## F-14 — the track's phase list is restated in both consumer generators
Where   : backend/.claude/commands/generate-module-setup.md:129 and :211
          frontend/.claude/commands/generate-frontend-module-setup.md:168 and :232
Expected: the phase vocabulary is a PROFILE fact
          (`profile.tracks.<track>.plans.<plan>.phases`, C1). Adding a phase to
          a track should be a profile edit plus the phase's own content.
          `GOVERNANCE-SHARED-DESIGN.md` §7 lists both generators under "does
          not change" and credits C1 with keeping the blast radius small.
Actual   : each generator restates the whole ordered list twice — once as
          "Expected phases, in strict order" and once as the `gated_by_phases`
          array of the test phase (`grep -c` returns 2 in each file).
          The first is partly defensive: the surrounding text says to derive
          phases from the delivered package folders and "only include ones
          actually present", so the list reads as a sanity ordering. The
          second is not — `gated_by_phases` is written verbatim into
          `execution-state.json` and decides which phases must be COMPLETE
          before the test phase may run. A phase added to the profile and
          delivered in the package would be scanned into `phases` by the
          generator and silently ABSENT from `gated_by_phases`, so the test
          phase would run without it.
          Measured extension cost for "add a phase to a track": 1 profile file
          + 2 consumer files in 2 repos, of which the consumer half is invisible
          to `gov.py lint` — `lint.scan_paths` covers the factory only, so
          nothing in the factory can see these two files drift.
Fix     : OPEN. The clean repair is for `gated_by_phases` to be derived rather
          than typed: `gov.py deliver` already writes `phases` into the
          execution-state it delivers (`delivery.execution_state.schema`), so
          the generator could read the delivered list instead of restating it.
          That is blocked on F-6 — the generator currently OVERWRITES that
          delivered file rather than reading it, so the data it needs is the
          data it destroys. Recorded together; F-6 is the one to settle first.
Status  : OPEN

## F-15 — a second api-docs copy lived in the factory, and it had drifted
Where   : factory/erp/modules/{SEC,FIN,MDL}/api-docs/ (25 files, git-tracked)
Expected: invariant 1 — api-docs exist in exactly one place. The ownership
          table (§3) gives `backend/modules/*/api-docs/` one writer (the
          backend) and one location (the shared repo). §2 states the point
          outright: one source, not two, so drift is structurally impossible
          rather than merely visible.
Actual   : three full `api-docs/` trees — an `index.md` plus an `endpoints/`
          folder each — were committed inside the factory's own module
          folders, left over from before the shared repo existed.
          They had already drifted, which is the argument for one copy made
          concrete rather than hypothetically:
            diff erp/modules/SEC/api-docs governance-shared/backend/modules/SEC/api-docs
          reports all eight endpoint files and the index differing. The
          factory-side copy is the OLDER one — it lacks the
          `Contract ID: API-SEC-###` lines the published copy carries
          (index 9811 bytes vs 10785), so a contract id resolved against it
          would have resolved to nothing. FIN and MDL were still
          byte-identical: the drift was silent and partial, which is the state
          a second copy decays into rather than an accident.
          Nothing read them — `fetch-inputs` resolves
          `repos.backend.publishes.api-docs` against `reads_from: shared`,
          which is `factory/governance-shared/backend/modules/{MOD}/api-docs`,
          confirmed by resolving the path through `CFG` directly. So this was
          a dead second copy, which is the kind that drifts unnoticed longest.
Fix     : removed (`git rm -r`). Verified after: no `api-docs` directory
          remains anywhere in the factory outside the shared submodule, and
          `fetch-inputs -m SEC` reports `unchanged` twice in a row.
Status  : FIXED

## F-16 — a fourth, unpinned clone of the shared repo sits beside the three
Where   : <workspace>/governance-shared/ (workspace root)
Expected: §4 — the shared repo reaches each of the three repositories as a
          PINNED submodule, and the pin is what makes a delivered version
          provable: "any release knows what it was built on". Three consumers,
          three pointers, one commit.
Actual   : there are four checkouts, not three. The factory, backend and
          frontend each mount it correctly as a submodule, all three pinned to
          `7df6f10`. Beside them sits a plain clone at the workspace root that
          is a submodule of nothing and is pinned by nothing.
          It is at `7df6f10` too, so nothing is wrong today, and that is
          precisely the problem: it is the one copy no `git submodule status`
          will ever report as `+` (differs from its pinned commit) — the check
          §8 names as the mitigation for the biggest risk, and the one
          `cmd_sync` collects into `stale`. A commit made there is invisible to
          every freshness signal the design has.
          Its working tree is already dirty (five untracked `.DS_Store` files),
          which is evidence it is a place someone works, not a build artifact.
Fix     : WONTFIX — not mine to delete. It lies outside all three repositories,
          so removing it is a workspace decision, and it may be the clone used
          to push (the submodules are `shallow = true`, which makes pushing
          from them awkward — a plausible reason it exists). Recorded so the
          decision is made rather than inherited. If it IS the push clone, the
          honest fix is to say so in `GOVERNANCE-SHARED-DESIGN.md` §4, which
          today describes three checkouts and knows nothing of a fourth.
Status  : WONTFIX

## F-17 — the file-block contract is honoured inconsistently, so `wrote N` means nothing
Where   : governance-tools/dispatch.py `ingest()` / `run_round()` · the
          `GOV_RUNNER_CMD` contract the smoke prompt specifies
Expected: "The brief already tells each implementer to answer with
          `<<<FILE: path>>> … <<<END FILE>>>` blocks; `dispatch.ingest()`
          writes them." On that contract, `len(res.written)` is the count of
          what the stage produced, and the archived response is the record of
          it.
Actual   : measured across this run's four responses —
            P0.response1.md           FILE-blocks=3   30127 bytes
            P0-round2.response2.md    FILE-blocks=3   36592 bytes
            P0.5.response1.md         FILE-blocks=1   19366 bytes
            P1.response1.md           FILE-blocks=0    1991 bytes
          P0 honoured the contract in both rounds. P0.5 honoured it in round 1
          and abandoned it in round 2 ("written … as operator, not a file
          block"). P1 never used it at all: a 1991-byte response, while
          `srs-note.md` and `registry-srs-note.md` both landed on disk and
          analyzed clean. The orchestrator reported
          `dispatched P1: 1 round(s), converged=True, wrote 0 file(s)`.
          So this is NOT a dialogue-only effect (F-9) and not one implementer's
          quirk: the runner is an agent holding write tools, and whether it
          uses blocks varies by stage. Two consequences:
          · `wrote N file(s)` is not a signal. It reads as "this stage
            produced nothing" and printed exactly that over a committed PRD
            and a committed SRS. An operator watching that line cannot
            distinguish a stage that produced nothing from one that produced
            everything.
          · the provenance gap of F-10 is total for P1, not partial: the SRS
            exists in no archived response, so the run cannot be re-derived
            from what it archived.
          What DOES catch a genuinely empty stage is the artifact-existence
          clause C4.1 downstream, which is now reliable (F-11). The dispatch
          line is the part that lies; the checker is sound.
Fix     : OPEN, and deliberately not patched by making the message cosmetic.
          `wrote N` should report what the STAGE produced, not what `ingest`
          happened to write — i.e. `_complete_stage` already knows the
          declared artifacts and could report their presence and whether each
          changed. That is the honest signal and it is a small change; it is
          left OPEN only because it overlaps F-10's choice (enforce the block
          contract, or drop it for agent runners and record per-artifact
          digests). Settle F-10 first and this follows from it — patching the
          message alone would paper over the same gap in a different place.
Status  : OPEN

## F-18 — the P2 engine's canonical DBF form is one the ID model cannot read
Where   : engines/P2/references/ENGINE.md §2 (:79) and §7 (:265)
          vs governance-tools/idmodel.py:24 `_HEAD` / `records()`
          surfaced as: C6.5 `registry-agree`, C6.6 `orphans`
Expected: a plan that follows its own engine template exactly should satisfy
          the contracts that engine's stage is checked against. "If a check
          fires wrongly, the check is the defect."
Actual   : P2 blocked NOTE with 10 MAJOR — nine `DBF-NOTE-001..009`
          "registered in `registry-db` but not defined in `db-script`", plus
          `ENT-NOTE-001 is referenced by 0 of ['DBF']`. The db-script is not
          wrong. It carries all nine exactly where §2 puts them:
            | DBF-NOTE-001 | note_pk | BIGINT | ENT-NOTE-001.notePk … |
          §2 calls that table "the single canonical source of `DBF` → column →
          type → SRS origin", and §7's six-section output structure for the
          db-script contains no definition list at all — a matrix, an XM
          register, the SQL, decisions, registry content. The template asks
          for a table and nothing else.
          `idmodel._HEAD` only accepts an id at the START of a line (optionally
          `#`-headed or `**`-bolded) followed by `— - : (` or EOL. A table row
          starts with `|`, and a `COMMENT ON COLUMN … 'DBF-NOTE-001 …'` line
          starts with `COMMENT`. So `defined_ids()` sees zero DBF definitions
          in a db-script written to spec, and `registry-agree` — whose
          artifact side deliberately requires a DEFINITION while the registry
          side accepts a mere reference — charges every one.
          WHY THE SHIPPED MODULES PASS, which is the part that matters: SEC
          and MDL carry an EXTRA section the engine never asks for —
          `**DBF-MDL-001** — MDL_LOOKUP_TYPE.lookup_type_pk [ENT-MDL-001,
          REQ-MDL-001]`, 104 such lines in SEC, from line 188 in MDL. That
          bold form does match `_HEAD`. So the suite has been passing on a
          convention that exists only in the authored artifacts, never in the
          template — and NOTE, the first module generated strictly from the
          template, is the first to expose it. This is a latent defect the
          existing corpus was hiding, not a regression.
Fix     : OPEN. I implemented the obvious repair — teach `records()` that a
          table row whose FIRST cell is an id defines it — and MEASURED it
          before keeping it. It is wrong, and the measurement is the reason:
            SEC   62 → 96 MAJOR
            MDL   30 → 67 MAJOR
            NOTE  34 → 61 MAJOR
          with new mass `C6.2`/`C6.3`/`C9.4` `traces` and `C6.6` `orphans`
          findings. The cause is structural, not a regex detail: a `Record`'s
          body is the block under its definition and its traces come from a
          `Traces:` line in that body, whereas a table row carries its traces
          in COLUMNS. Recognising the row without also reading its columns
          makes every newly-defined id an untraced one. Reverted; the four
          modules' counts return to baseline and the suite is 242 passed.
          The two coherent repairs:
          (a) teach `records()` to parse a definition table properly — read the
              header row, learn which columns are traces, populate
              `Record.traces` from them. Architecturally indicated: §2 says the
              matrix IS the canonical source, so the model should be able to
              read it. Needs the column semantics declared as config (C1), not
              typed in the parser, which is the part that makes it real work
              rather than a patch.
          (b) add a definition list to the engine's §7 output structure, so the
              instructed form matches the enforced one — which is what SEC and
              MDL already do in practice. Cheap and immediately correct, but it
              duplicates the matrix that §2 calls "the single canonical
              source", which is the kind of second copy C4 exists to refuse.
          I did not force the choice. It changes either a shared parser used by
          every atom and artifact, or a rule the corpus has been quietly
          violating — and the documents do not settle which.
Status  : OPEN

### F-18 — how the run was unblocked (scaffolding, NOT the fix)
NOTE's own `db-script-note.md` now carries a `## 1a. DB FIELD DEFINITIONS`
section — nine `**DBF-NOTE-00n** — NOTE_NOTE.<column> [ENT-NOTE-001]` lines
each with a `Traces:` line — which is the same shape SEC and MDL already ship.
P2 then analyzes `0 critical · 0 major · 0 minor · CLEAN · 1 clause(s) examined
nothing (C6.3)`, and C6.3 is the XM-traces clause, correctly empty for a module
that declares no cross-module dependency.
No check was weakened and no row was softened: the artifact was made to carry
what the checker requires, exactly as every shipped module does. The engine
still does not ask for that section, so the next module generated from the
template will block in the same place. That is what F-18 stays OPEN for.
Also noted while doing it: the db-script carries the nine ids in TWO tables
(the §2 matrix at line 21 and the §10 registry-content table at line 203) and
neither was a definition — so the artifact already duplicated the data twice
before this third listing was added, which is an argument for repair (a).

## EXTENSION POINTS — measured, not assumed

| To add … | Files outside its own artifacts | Where the literal lives |
|---|---|---|
| a module (`NOTE` was one) | **2** — `profiles/erp.yaml` (`vocabulary.module_prefixes`), `README.md` (regenerated by `gov.py render`, never hand-edited) | the profile. Correct home. |
| a phase to a track | **3** — the profile, plus `generate-module-setup.md` and `generate-frontend-module-setup.md`, which each restate the ordered phase list twice (F-14) | the profile owns it; the two consumer copies have no home in config and are invisible to `gov.py lint` |
| a contract clause | **2** — `shared/ARTIFACT-CONTRACTS.md` frontmatter (the clause as data) and the check function in `analyze.py` | none. Both are "the thing itself". A clause naming an unimplemented check is REPORTED, not skipped. |
| a third consumer repo | **1** — `factory.yaml` (`repos.<k>` + `tracks.<k>`), plus the profile's `tracks.<k>.plans` | none typed, but the repo key must silently equal the track name (F-12) |
| a second profile | **1** — `profiles/<id>.yaml`, then `gov.py render` | none. No `erp` literal survives outside generated blocks; verified by grep + `lint` clean. |

Adding a module cost exactly **two** files, one of them regenerated. That is the
bar the brief set ("config only, plus the thing itself") and the factory meets
it. C1 is doing the work it was built to do, and the §12 claim that the layers
are sound survives contact with a real extension.

Everything else changed during this review was a DEFECT REPAIR the run exposed,
not a cost of extending:
  governance-tools/{findings.py (new), lint.py, render.py, dispatch.py,
                    state.py, gov.py, tests/test_silent_success.py}
  erp/modules/{SEC,FIN,MDL}/api-docs/**            (deleted — F-15)
  backend/.claude/commands/generate-module-setup.md (F-5)
Counting those as extension cost would be the wrong lesson: they were already
broken, and adding one small module is simply what made them visible.

## COUPLING — measured before and after

The graph over `governance-tools/` (ten modules plus the `toolkit` package):

    analyze   -> config contracts idmodel state toolkit
    contracts -> config
    dispatch  -> config contracts idmodel state
    gov       -> analyze config dispatch idmodel lint render state toolkit
    idmodel   -> config
    lint      -> config findings render toolkit
    render    -> config contracts findings toolkit
    state     -> config idmodel toolkit
    toolkit   -> config

One cycle existed and was cut (F-2): `lint -> render` (for `check_fresh`) and
`render -> lint` (for `Finding`), each a function-local import whose comment
explained the other. `render` needed ONE ten-line dataclass with zero
dependencies out of a 366-line checker — the same shape as the `analyze ->
render` edge that `contracts.py` had already cut, so it got the same treatment:
`findings.py`, 31 lines, one dependency, re-exported from `lint` so no call
site changed. Re-measured after: **no cycles, the graph is a DAG.**

No other edge has that shape. The ones worth naming as examined and LEFT:
  `dispatch -> contracts`  one symbol (`contracts_from_doc`) — but `contracts`
      is already the 39-line thin module; there is nothing left to cut.
  `lint -> render`         now one-directional, and it is a checker calling the
      thing it checks the freshness of. That is the real dependency.
  `gov -> everything`      gov is the orchestrator; breadth is its job, and
      every edge is a handful of named symbols, not a god-object reach.

## WHAT HELD — the things a review should say plainly when they work

Recorded because "no finding" is only credible if the check that would have
produced one is named and was actually run.

**Invariant 2 — determinism. HELD, both halves.**
`fetch-inputs -m SEC -v 1` reports `unchanged` on the FIRST run as well as the
second, which is the stronger result: the committed `_inputs/api-docs-sec.md`
is byte-identical to a fresh fold of the published folder, so no hand-edit has
crept in. `gov.py state -m SEC -v 1` twice leaves `git status` empty.

**Known condition 1 — RESOLVED, and not the way the brief allowed for.**
The brief said a brand-new module SHOULD carry an `Entity` line on every `API-*`
block, and that its absence would be a finding about the engine. NOTE carries it:
12 `Entity : ENT-NOTE-001` lines against 10 `API` blocks. The P3.1 engine does
instruct it (`ENGINE.md:376`, with the reason at :403). So the engine is right
and the gap is confined to the older modules, exactly as the brief's own
disclaimer described — no finding. SEC's `C7.23` still reports "20 of 27 `API`
blocks name no `ENT` … the demand each `SCR-REQ` declares stays unchecked",
which is the check SAYING IT CANNOT SEE THE ANSWER rather than guessing.
Invariant 3, working.
CORRECTION to the above, recorded rather than quietly edited: the `Entity` line
was the only half of this that held. Carrying it did NOT make `C7.23` able to
judge NOTE — the clause still examined 0 subjects, for an unrelated reason that
took instrumenting it to find, and that is F-19. The engine's half of known
condition 1 is sound; the checker's half was not, and is now. After F-19,
`C7.23` examines 6 subjects for NOTE and returns a real verdict.

**Empty-but-present phases — CORRECT, and legible as such.**
NOTE declares no `XM`, and all eight backend phases are present in the plan.
`INT-C` opens with "**Empty by construction, present by requirement**", an empty
table with an em-dash row, and a paragraph per SEC entity explaining why each is
not an `XM`. A phase that ran and found nothing is unmistakable from one that
was skipped — which was the thing the brief asked me to be unable to tell apart.

**The contract set itself — CLEAN.** 12 contracts, 94 clauses, audited
programmatically: zero clauses naming a check `analyze` does not implement, zero
implemented checks no clause uses, zero clauses charged at a severity
`analyze.severities` does not declare. And an unknown check is a FINDING, not a
skip (`analyze.py:1867`) — the vacuous-clause reporter names every clause that
examined nothing and explains how to read it.

**The ambiguity rule — WORKING.** Six ADRs under `erp/decisions/NOTE/`, all
`ACCEPTED — non-breaking; the pass continued`, none BLOCKED. No stage wrote
"STOP and ask the user", and no `[QUESTION]` survived a questions-forbidden
stage.

**Scope discipline — HELD.** The brief fixed the module at one entity, four
operations, one screen, zero cross-module dependencies, and said growth beyond
that was itself a finding. Measured: 1 `ENT`, 1 `SCR-REQ`, 18 `REQ`, 9 `DBF`,
0 `XM`. Nothing inflated it.

## F-19 — a clause reported "examined nothing" when it had failed to READ its subject
Where   : governance-tools/analyze.py:1250 (`_c_operation_resolves`, the
          `verbatim` branch) · surfaced as C7.23 on NOTE's P3.1
Expected: invariant 3. `analyze`'s own vacuous-clause paragraph sets the test:
          "Confirm each is empty by nature and not because the check failed to
          find its subject." Those two cases must be distinguishable.
Actual   : P3.1 analyzed `CLEAN · 3 clause(s) examined nothing (C7.23, C7.5,
          C7.5b)`. C7.5/C7.5b are XM clauses and NOTE has no XM — empty by
          nature. C7.23 was NOT: its subject was right there.
          The SRS's `SCR-REQ-NOTE-001` block carries both lines the clause
          needs — `Entities : ENT-NOTE-001` and
          `Operations   : search · list · create · read · update · deactivate`
          — and instrumenting the clause confirmed it resolved the subject and
          read the line: `stated='search · list · create · read · update ·
          deactivate'`. Then:
            words = re.split(r"[,;/]", stated)   → ONE token, the whole line
            names = {w for w in words if w.isalpha()}  → empty (spaces, `·`)
          so `for a in names:` never ran, `seen` stayed 0, and the clause
          reported itself vacuous. The operation vocabulary is read `verbatim`
          precisely so the factory never has to anticipate a project's words —
          but the SEPARATOR between those words was the literal `[,;/]` in the
          checker, a factory-side constant with no home in config, and `·` is
          what the engines render as their own list separator throughout (the
          P3.1 template alone uses it on a dozen lines). An author reaching for
          it is following the house style.
          Two defects, then: a C1/C2 literal, and a check that answered "nothing
          to examine" when the truthful answer was "I could not read this".
Fix     : both, and the separator is declared where `plan_vocabulary`'s own
          rationale says it belongs — "the labels the plans use on the lines the
          checks read; the engine renders these same values, so neither side can
          drift into looking for a line the other never writes".
          · `profiles/erp.yaml` → `plan_vocabulary.operation_separators:
            [",", ";", "/", "·"]`, and the optional field added to
            `profiles/_schema.yaml`;
          · C7.20 and C7.23 pass it as `separators:
            plan_vocabulary.operation_separators`;
          · `analyze` builds the split class from it, falling back to the old
            set when the field is absent (C5 — absent optional field means the
            behaviour is not applied, not that the clause refuses to run);
          · and when a non-empty line yields no readable operation, the clause
            now RAISES A FINDING naming the line and the legal separators,
            instead of counting zero.
          The engine renders the separator list beside the `Entity`-line rule,
          so the author is told. That last part was NOT my idea — the suite's
          `test_every_label_a_clause_reads_is_one_the_engine_actually_renders`
          failed the moment I added a `plan_vocabulary.*` address no engine
          rendered, which is exactly the drift it exists to catch. The guard
          worked on a live change.
          Verified: C7.23 for NOTE goes from `0 ⚠ nothing` to `6` subjects
          examined with a real verdict; SEC (62), FIN (166) and MDL (30) are
          byte-identical to baseline; lint `0 · 0 · 0`; 242 passed, 1 skipped.
Status  : FIXED

## F-20 — a schema category was added and NO gate in the factory could open again
Where   : shared/REGISTRY-SCHEMA.md:38 (added by commit 31d82fa) vs
          erp/project-registry.md — surfaced as C2.2 `registry-agree`
Expected: `C2.2` requires the project registry to map every category
          `REGISTRY-SCHEMA.md` declares. A gate opens only when analyze is
          `clean` (`gates.*.requires_analyze`), so this clause is load-bearing
          for every gate in the factory.
Actual   : gate 1 for NOTE closed with
            [MAJOR] C2.2 (registry-agree) project-registry —
                    registry does not map categories ['CAT-10']
            GATE CLOSED: analyze is not clean
          `CAT-10 platform findings` was added to `REGISTRY-SCHEMA.md` by
          commit `31d82fa` ("fix(registry): a channel for findings that belong
          to no module"). `erp/project-registry.md` was never given the
          matching section, and its category map still ended at CAT-9 under a
          line reading `Uncovered: none` — a claim that had become false.
          This is NOT caused by adding NOTE, and it is not NOTE's to fix: the
          registry is a platform artifact. Confirmed platform-wide by running
          the same scope against the shipped modules — SEC and MDL both report
          the identical C2.2 finding at `gate:pass-1`. So since `31d82fa`, NO
          module's gate in this factory could open. SEC, MDL and FIN each
          recorded `GATE pass-1: APPROVE` on 2026-09-10, before that commit, so
          nothing had re-run a gate since and nothing noticed.
          The shape is worth naming: a change to a SHARED schema silently
          invalidated a derived platform artifact, and the only thing that
          could see it was a clause that fires at a gate nobody had re-run.
          The check behaved perfectly — it is the reason this is a finding at
          all rather than a mystery.
Fix     : added the `## PLATFORM FINDINGS` section to `erp/project-registry.md`
          with the CAT-10 row shape REGISTRY-SCHEMA §4 specifies (finding ·
          evidence · found by · belongs to · status), an empty table, `Open
          platform findings: 0`, and the category-map row that makes
          `Uncovered: none` true again. This is what P-1 would have produced;
          it was written by hand rather than by re-running P-1, because
          re-dispatching the registry builder would regenerate a platform
          artifact three shipped modules depend on in order to add one section.
          Verified: NOTE's `gate:pass-1` scope goes to
          `0 critical · 0 major · 0 minor · CLEAN`, and C2.2 no longer fires
          for SEC or MDL either. lint `0 · 0 · 0`; 242 passed, 1 skipped.
Status  : FIXED

## F-21 — the gate declares two reviewer lanes and dispatches neither
Where   : governance-tools/gov.py:390 `gate()` — the only stage-like command
          that never calls `dp.dispatch()`
Expected: `factory.yaml` gives each review gate
          `lanes: [review-per-engine, review-holistic]`, and defines both in
          `lanes:` with `implementers: ["claude:sonnet"]`, `effort: medium`,
          `read_only: true`. `dispatch.py` supports read-only lanes end to
          end — `run_round()` takes `read_only` and substitutes
          `{read_only_flag}` as `--read-only` in `GOV_RUNNER_CMD`, documented
          in its own module docstring. Both halves exist.
Actual   : nothing connects them. `gate()` writes the brief and returns
          AWAITING, naming the lanes in a MESSAGE — "(lanes
          `review-per-engine`, `review-holistic`, read-only reviewers)" —
          while dispatching neither, under any runner. Grepping
          `governance-tools/` for those lane ids returns exactly that one
          `_say()` line. `lane.read_only` is read in `dispatch.py` (for
          ordinary stages, none of which set it) and in `render.py:98` (to
          print a table), so the flag that exists FOR the reviewer lanes is
          used by everything except them.
          The consequence is not that a human decides — a human deciding is
          correct and is what CONSTITUTION §2 requires. It is that the human
          decides with NO SCORECARD, because the thing that produces one was
          never run. `review.rubric`, `review.scale`, `review.pass_threshold`
          and `review.verdicts` are all declared and all wait on a JSON file
          the factory never asks anyone to generate.
Fix     : OPEN, and deliberately not "make the gate auto-approve" — that would
          delete a human decision point, which is the one move this review is
          forbidden. The additive repair is narrow: `gate()` dispatches its
          lanes (read-only) to produce the scorecard, then STILL returns
          AWAITING so the human accepts or rejects it. That preserves the gate
          and removes the only manual step in an otherwise unattended pipeline.
          Left OPEN because it is a capability the factory declares and has
          never had, not a regression — adding it is a design decision about
          how much of a human gate may be pre-computed, and the documents say
          the human ACCEPTS a verdict without saying who must produce it.
          For this run I acted as the operator the message addresses, ran the
          brief through the reviewer lane's own model by hand, and completed
          the gate with the JSON — which is the workflow as it actually stands.
Status  : OPEN

## F-22 — the self-check's COVERAGE prose goes stale and no machine check sees it
Where   : erp/modules/NOTE/P3_1/backend-execution-plan-note.md, PHASE 8
          ALIGN-BE · found by the gate reviewer (finding G1), not by `analyze`
Expected: the ALIGN block states the run's coverage. `profile.self_check` says
          the verdict is the ORCHESTRATOR's to write "from the analyze report
          (gov.py), the author leaves it alone", and `verdict-agrees`
          (C7.15/C9.12) guards it so a plan cannot claim fewer findings than
          the machine produced.
Actual   : that guarantee covers the verdict LINE only. `gov.py:145` stamps
          `RESULT: <token> — <n> findings` via `analyze.render_verdict()`, and
          `_c_verdict_agrees` checks the COUNT on that line. The surrounding
          COVERAGE narrative — which clauses examined nothing, and why — is
          author prose that nothing writes and nothing checks.
          It was already wrong at the gate. The plan's COVERAGE row named
          "C7.23, C7.5, C7.5b — the three clauses the analyze report lists as
          having examined nothing" and spent a paragraph re-explaining the
          C7.23 separator defect. The analyze report actually bound to the gate
          lists **C6.3, C7.5, C7.5b**: C7.23 now examines 6 subjects.
          The cause is worth stating plainly because it is mine: the plan was
          generated BEFORE I fixed F-19, and my fix changed the analyze result
          underneath it. So the plan honestly described the world when it was
          written, and the ONE clause that is genuinely vacuous this run —
          C6.3 — is named nowhere in the artifact, while a defect that no
          longer reproduces is explained at length.
          This is the exact hazard the vacuous-clause paragraph warns about,
          one level up: a reader is told which clauses enforced nothing, by a
          sentence with nothing behind it.
          The gate REVIEWER caught it and `analyze` could not — which is a
          point in the gate's favour, and the clearest argument in this run for
          why the review lane is worth dispatching (F-21).
Fix     : OPEN. Two shapes, and the choice is not mine to force:
          (a) the orchestrator stamps the coverage list too, the way it already
              stamps the verdict line — `AnalyzeReport.vacuous()` is exactly
              the list needed, so this is small. It makes more of the ALIGN
              block generated and less of it authored, which is the direction
              `self_check` already points;
          (b) a contract clause guards the authored list against the report the
              same way `verdict-agrees` guards the count — the author keeps
              writing the reasoning, and a stale list becomes a finding.
          (a) is cheaper, (b) preserves the author's judgement about WHY a
          clause is empty, which is the part worth keeping. `gov.py analyze
          --all-modules` (the `reanalyze` command, documented "sweep after a
          rules change") re-derives the reports but re-stamps no prose, so
          neither is available today.
Status  : OPEN

## GATE 1 — recorded outcome
`gov.py gate 1 -m NOTE -v 1 --complete --result …` → **REVISE**, exit 1.
Scores: unambiguous 3 · verifiable 3 · complete 3 · consistent 2 · singular 3 ·
feasible 3 · traceable 2 — every attribute at or above `review.pass_threshold`
(2), so the verdict was the reviewer's own judgement, not the automatic
downgrade. One MAJOR finding (G1), recorded above as F-22. All five ERP extra
checks PASS; all six ADRs reviewed and confirmed accurate; all three vacuous
clauses confirmed empty by nature rather than by check failure.
The gate did its job: it blocked on something no machine clause could see.


---

# MIGRATION — governance into the shared repo

Continues the numbering above. Run against `MIGRATE-TO-SHARED-PROMPT.md`.

## F-23 — the consumers had been amending their delivered copies for months
Where   : backend/governance/modules/{SEC,FIN,MDL}/**
          frontend/governance/modules/{SEC,FIN,MDL}/**
Expected: `gov.py deliver` copies the factory's artifacts into a consumer repo.
          The consumer READS them. `GOVERNANCE-SHARED-DESIGN.md` §1 — "one
          writer per path, no exception" — gives every one of those paths
          exactly one writer: the factory.
Actual  : measured before deleting anything, because the migration's own rule
          is to verify content byte-identical first. It was not identical in
          either direction:
            lines present ONLY in a consumer copy : 550
            lines present ONLY in the factory's   : 1956
          across 40 files in 6 module trees. Not drift from a stale copy — the
          backend EDITED its copy, deliberately, and committed the edits with
          reasons. `6b8df42 feat: Enhance SEC module with cross-module read
          capabilities` added a whole requirement to the delivered SRS:
            REQ-SEC-034 — Cross-module read of a user's contact details
            Source : AMENDMENT 2026-09-11
          plus three `Note:` blocks recording operative decisions taken at
          implementation ("the account is created with an unusable random
          secret", "N = 10 most recent AuditLogEntry rows"). None of it ever
          reached the factory. The factory's plan for SEC did not describe what
          the backend had built, and nothing anywhere could see that.
          The 1956 in the other direction are mostly real too: the factory's
          later `Data source:` lines and search-endpoint rows, and three
          `frontend-test-plan-*.md` that the consumers hold as 27-line stubs
          and the factory as the finished 200-to-570-line plans.
          A copy in either direction would have destroyed one side.
Fix     : FIXED — resolved as the three-way merge it actually is. Base is each
          file's FIRST revision in the consumer repo, which is the delivered
          copy; ours is the factory tree, theirs the consumer's.
            16 files merged with no conflict at all
            14 conflicted — every one of them the two sides making the SAME
               change in different words (`Page\<UserResponse\>` against
               "paginated list of UserResponse"), or the consumer recording
               something the factory could not know (the 500 path is owned by
               the shared GlobalExceptionHandler, so it carries no
               module-scoped code)
            20 derived files (manifest/state/verification .json) left alone —
               the factory regenerates them
          Conflicts resolved toward the consumer: it is the side that built it.
          Verified after, not assumed — every atom id appearing anywhere in the
          consumer copies resolves in the shared tree: SEC 341, FIN 540,
          MDL 119, and no file was written carrying a conflict marker.
          The merge also honoured the consumer's DELETIONS, which is the part a
          copy would have got wrong in the other direction: seven traceability
          and search-endpoint rows the backend removed stayed removed, because
          they are in the base.
Status  : FIXED
Note    : this is the finding that justifies the whole migration. Three copies
          kept in step by a command drifted for months in both directions and
          no check could see it, because no check compared them. One copy
          cannot drift from itself.

## F-24 — `_commit` would have staged a pointer and saved nothing
Where   : governance-tools/gov.py::_commit
Expected: a stage commits its own output.
Actual  : `_commit` ran `git add` in the factory root over paths that, after
          the flip, live in a submodule. Git stages the submodule POINTER for
          such a path, not the content — and then commits cleanly. A stage
          would have reported `OK: P1 committed abc1234` having saved none of
          the artifact it produced. That is the worst shape a failure can take:
          a success message over nothing.
Fix     : FIXED — `_owning_checkout()` resolves the repo per path from the
          declared checkouts (innermost wins, so a submodule beats the parent
          containing it), `_commit` groups by owner and commits in each, and
          advances the parent's pointer in the same operation so a factory
          commit records the shared commit its artifacts went into.
          Guarded by test_commit_routing.py; three of its four tests fail if
          the old behaviour is restored, checked by restoring it.
Status  : FIXED

## F-25 — `git commit` with no pathspec takes the whole index
Where   : governance-tools/gov.py::_commit_in
Expected: a commit contains what the caller named.
Actual  : found by probing F-24's fix rather than by reading it. `git commit
          -m msg` commits everything staged, so a probe that wrote one file
          produced a commit containing that file AND 36 deletions staged out of
          band by the migration, under a message describing only the file.
          The old `_commit` had the same hole; it mattered less with one repo.
Fix     : FIXED — every git call in `_commit_in` is pathspec-limited. Proved by
          staging an unrelated file in the shared repo and watching it stay out
          of the commit.
Status  : FIXED

## F-26 — `{profile_id}` escaped into a filesystem path, and the error lied
Where   : governance-tools/config.py::fmt · factory.yaml repos.*.publishes
Expected: a declared path resolves before it is used.
Actual  : `paths` resolved `{profile_id}`; `CFG.fmt` did not. So
          `repos.backend.publishes.api-docs` reached the filesystem as
          `…/governance-shared/{profile_id}/modules/SEC/api-docs`, found
          nothing, and `fetch-inputs` reported:
            GATE CLOSED — missing inputs: api-docs ← …/{profile_id}/…
          over a directory holding eleven files. A closed gate is the right
          answer to a missing input and the wrong answer to an unresolved
          template, and nothing in the message distinguishes them.
          Found by RUNNING the pipeline after the migration, not by reading it.
Fix     : FIXED — resolved in `CFG.fmt`, where every other token is resolved,
          so any declared value may carry it; an explicit `profile_id=` still
          wins. Guarded by walking every declared `publishes` and `partitions`
          template and asserting none survives formatting with the token
          intact.
Status  : FIXED

## F-27 — repointing a path can invert the check that guards it
Where   : backend/.github/workflows/governance-shared.yml
          frontend/.github/workflows/governance-shared.yml
Expected: the guard means "no local api-docs copy has reappeared in this repo".
Actual  : the migration rewrote `governance/modules/...` to
          `governance/shared/erp/modules/...` across 25 consumer files. The
          guard's own path went with it, and its meaning became "fail if
          api-docs exist in the shared repo" — the location that is now
          correct. It would have failed every run, for being right.
          A mechanical path rewrite is safe for a path that is read and unsafe
          for a path that is ASSERTED ABSENT. Nothing distinguishes the two
          syntactically.
Fix     : FIXED — both workflows rewritten around what they should check: every
          `*_path` in this repo's execution state resolves; the check examined
          something rather than passing over an empty glob; and no governance
          directory has reappeared outside the submodule. Both directions of
          the reappearance guard proven before committing.
Status  : FIXED

## F-28 — a commit made in a submodule is abandoned by the next update
Where   : the working procedure, not a file
Expected: committing in the submodule and pushing is how the shared repo is
          written.
Actual  : `git submodule update --remote` leaves the checkout on a COMMIT, not
          a branch. A commit made there is on a detached HEAD, and the next
          `submodule update` moves away from it — leaving a real commit that
          nothing references. It happened here: `809a22c platform rules leave
          the repo the other repos were reaching into` was committed, orphaned,
          and the subsequent `git push origin HEAD:main` was a silent no-op
          because HEAD was no longer that commit.
Fix     : FIXED for this instance — recovered from the reflog and cherry-picked.
          Recorded in `GOVERNANCE-SHARED-DESIGN.md` §4 as the rule it implies:
          `git checkout main` inside the submodule before any write.
Status  : FIXED
Note    : a `gov.py` guard that refuses to write into a detached submodule
          would close this mechanically. Not built — recorded so the choice is
          made rather than inherited.

## F-29 — `FIN/execute-backend-test` names a plan that was never generated
Where   : backend/.claude/commands/FIN/execute-backend-test.md:20
Expected: a generated command names files that exist.
Actual  : it reads every `TC-FIN-<seq>` block out of
          `…/FIN/test_gen/backend-test-plan-fin.md`. That file does not exist
          and never did — FIN's `test_gen/` holds only
          `frontend-test-plan-fin.md` and the execution manifest. The split
          package `packages/backend-test/` exists, so the plan was split from
          something that is not there under that name.
          Pre-existing; surfaced by this migration's check that every literal
          path in a consumer command resolves. It is the ONLY one of roughly
          two hundred that does not.
Fix     : OPEN — either the plan is generated, or the command reads the split
          package it actually has. Which is right depends on whether FIN's
          backend test plan was ever authored, and the repo does not say.
Status  : OPEN

## PRIOR FINDINGS THIS MIGRATION CLOSED

- **F-6** two writers on one `execution-state.json` — CLOSED structurally, not
  by picking a winner. The file carried two things. Everything `gov.py deliver`
  wrote into it already lived in a tracked file nobody overwrites
  (`manifest.json`, `_state/gate-pass-*.md`, `_state/analyze-*.md`); what
  remained is execution progress, which belongs to the track. `deliver` is
  retired and each track now writes its own
  `modules/{MOD}/{backend,frontend}/execution-state.json`. Two files, two
  writers, no contention.
- **F-12** a consumer repo key had to silently equal its track name — CLOSED.
  The finding left the repair open between merging the two tables and letting a
  track name its repo. `repos.shared` settles it: it is a repo and not a track,
  so they cannot merge. `tracks.<t>.repo` states the link and
  `lint.scan_config` checks it — factory.yaml's own structure was validated by
  nothing, since C5 covers profiles only. Verified the check fires on all four
  disagreements it claims to catch.
- **F-16** a fourth unpinned clone of the shared repo — CLOSED. Recorded as
  WONTFIX ("not mine to delete"); it proved its own case twice in one session.
  A write went to it instead of the submodule and looked like it had not
  happened; a reset meant for the submodule landed on it and dropped a commit.
  No `git submodule status` reports a checkout that is not a submodule.
  Removed after verifying it held nothing unpushed. `shallow = true` went with
  it from all three mounts: it was right while the consumers only read, and
  wrong once they write — a writer pushes, and a pusher needs history.
- **F-14** the phase list restated in both consumer generators — NOT closed.
  Step 6 touched those files for their paths, not their phase vocabulary. It
  remains OPEN, unchanged.

## MIGRATION — summary

Steps completed : all 8
Repos cleaned   : factory 631 → 0 governance files · backend 411 removed ·
                  frontend 225 removed. Each now holds code, tools, its own
                  reports, and a submodule pointer. `governance/.gitignore` is
                  the only file left under either consumer's `governance/` that
                  is not a tool, a report, or the mount.
Shared repo     : 892 tracked files · 7 modules (CU FILE FIN MDL NOTE NOTIF
                  SEC) · 233 commits · partitions platform, factory, api-docs,
                  backend, frontend
deliver         : retired. 266 lines of command and helper, the `delivery:`
                  config block, `naming.delivery_branch`, `CFG.delivery`,
                  `CFG.delivery_branch`, the `verify-delivery` command file,
                  and `deliver` from both passes' `then:` lists. Nothing read
                  any of it after the flip — checked before deleting.
Findings        : F-23 … F-29 — 6 FIXED · 1 OPEN (F-29)
Prior findings  : F-6, F-12, F-16 closed · F-14 untouched and still OPEN
Baseline        : lint 0/0/0 · 250 passed, 1 skipped (was 242/1 at the start;
                  +8 from the guards this migration added)
Move fidelity   : 631/631 blob hashes identical to the source, 89 commits
                  readable by path (`git log erp/<file>` follows a file's real
                  history, which `git subtree add` alone does not give), and
                  the derived `_state` caches rebuild 64/68 byte-identical —
                  the other four differ only in their own timestamps.
Verdict         : yes. Each repo knows only where to read and where to write,
                  and both are the same place. The factory writes
                  `governance-shared/`; the backend writes `api-docs/` and
                  `backend/` inside it; the frontend writes `frontend/`. Proven
                  by running the pipeline, not by reading it: `fetch-inputs`
                  folds 11, 10 and 4 api-doc files for SEC, FIN and MDL out of
                  the backend's partition into the factory's inputs, both
                  inside the shared repo, with nothing copied between
                  repositories at any point.

---

# REVIEW 3 — factory, flow, boundaries (F-30 …)

## F-30 — `sync` diagnoses a consumer that mounts nothing as a stale pointer
Where   : governance-tools/gov.py:776 (printer) · :741 (producer)
Expected: `sync` "reports per-partition presence and stale consumer pointers".
          The three states are distinct repairs: a submodule never initialised
          (`git submodule update --init`), one on a commit other than the pinned
          one (bump the pointer), and a repo whose `.gitmodules` does not name
          the shared repo at all (neither command helps).
Actual  : the producer pushed a prose string for the third state, and the
          printer re-derived the diagnosis from the row's first character:
          `'not initialised' if line.startswith('-') else 'differs from its
          pinned commit'`. Anything not starting with `-` read as `+`. Forced
          with a decoy checkout via `GOV_BACKEND_CHECKOUT`:
            backend: submodule differs from its pinned commit
                     — MOUNTS NOTHING — .gitmodules does not name the shared repo
          A reader is told to bump a pointer that does not exist.
Fix     : FIXED — each row now carries its own diagnosis as a third element;
          the printer states it rather than guessing. Re-verified all three:
          mounts-nothing → "mounts the shared repo nowhere"; uninitialised →
          "not initialised"; both real consumers → 0 stale rows.
Status  : FIXED

## F-31 — `render` re-joined a path key to the root, ignoring `paths.external`
Where   : governance-tools/render.py:36
Expected: §1's lever — every artifact path funnels through `config.dir()`, and
          `paths.external` alone decides which keys resolve against the shared
          checkout: "Which is which is declared in `paths.external`, never
          decided in this function."
Actual  : `jinja2.FileSystemLoader(str(cfg.root / cfg.paths["templates"]))` —
          the key is read, then joined to the factory root by hand, so the
          `external` declaration is not consulted. Proven by declaring
          `templates` external at runtime: `config.dir("templates")` moved to
          `<factory>/governance-shared/governance-tools/templates` while the
          loader stayed at `<factory>/governance-tools/templates`. Latent
          today — `templates` is not external — but the funnel had a hole in it.
Fix     : FIXED — `cfg.dir("templates")`. Re-verified: the loader now follows
          the declaration in both directions. lint 0/0/0, 262 passed.
Status  : FIXED

## F-32 — the feedback waiver records a status it never reads
Where   : governance-tools/gov.py:878 (written) · :892 (read)
Expected: "`waive-feedback` is pinned to the items it saw." The record stores
          pairs — `[key, status]` — which states that the status is part of what
          was waived.
Actual  : `covered = {k for k, _ in waiver.get("waived", [])}` discards the
          status. The waiver is pinned to the item's identity only. Proven on a
          copy of the shared checkout (`GOV_SHARED_CHECKOUT`):
            - waive MDL → blockers 0
            - append a NEW gap → blockers 1  ← the gate closes again, correctly
            - flip the WAIVED item's `resolution` from `UNRESOLVED` (parsed
              `UNRECOGNISED`) to `OPEN` → blockers 0
          So the item waived while the factory "cannot tell whether this is
          still owed" stays waived once the consumer confirms it IS still owed.
          The stored status is a value nothing reads (condition 1).
          The headline behaviour is sound: the waiver is NOT an off switch, and
          a gap recorded after it does close the gate. Only the status is dead.
Fix     : OPEN — either pin on `(key, status)`, so a change of status re-closes
          the gate and the stored half is load-bearing; or drop the status from
          the record and store keys, so nothing implies it was considered. The
          repo cannot settle it: both readings of "the items it saw" are
          defensible, and which is right is a policy question about whether a
          waiver covers an item or an item-in-a-state.
Status  : OPEN

## F-33 — the whole `versioning` block is declared and read by nothing
Where   : factory.yaml `versioning:` (4 keys) · config.py:255
Expected: "A declared value nothing reads is dead config and should be deleted,
          not kept 'for later'."
Actual  : `cfg.versioning` — the accessor — has zero callers. Every mention of
          `versioning.authority` and `versioning.delta_only` in the tools is
          inside a comment or docstring citing the block as justification
          (config.py:401, publications.py:74, state.py:197); `current_state` and
          `freeze_previous` appear nowhere in code or templates at all. Changing
          any of the four changes nothing. Two further keys are equally dead:
          `naming.module_code` and `publications.*.derived_from`.
          `shared/VERSIONING.md` tells a reader `versioning.freeze_previous`
          governs tag-freezing, which no tool consults — so this is not merely
          unread config, it is documented behaviour with no implementation
          behind it.
          (Checked against false positives: `ids.ears.patterns.*` IS read, via
          `render.block_ears` iterating `.items()`; `markers.rules.split_unit`
          IS read, by the P3.1 engine template. Both survived the first pass of
          this scan and were cleared individually.)
Fix     : OPEN — either delete the six keys and correct `shared/VERSIONING.md`
          and `shared/GOVERNANCE-CORE.md`, or implement the behaviour the docs
          already promise. Deleting silently would make two shipped documents
          wrong; implementing silently would change tagging. Not a reviewer's
          call.
Status  : OPEN

## F-34 — the hardcode check is narrower than the rule it enforces
Where   : governance-tools/lint.py:279-300 (`scan_code_literals`)
Expected: C1 as the constitution states it — no stage id, phase key, ID prefix,
          **path, profile name, repo key, branch** or vocabulary term typed in
          Python.
Actual  : the scanner compares literals against `stage_ids | phase_keys` and
          `prefixes` only; its own docstring says so. Probed by injecting
          literals into `findings.py` and running `lint`:
            'ui-shell'          → CRITICAL (removed concept)   ✓
            'INT-XM', 'P0'      → MAJOR (code-literal)          ✓
            'spring-boot-java', 'SEC_PAGES', 'postgresql16'
                                → MAJOR (profile-literal)       ✓
            'erp'               → NOT CAUGHT                    ✗
          The profile NAME — the one term the consumers' CI, CLAUDE.md and
          commands all go out of their way not to type — is the one the factory's
          own check cannot see. No such literal exists in the tools today, so
          this is a gap in the check, not a live violation. Path, repo-key and
          branch literals are likewise unchecked, and several exist:
          gov.py:1076 (`"_archive-v5"`, `"history"`), gov.py:1215
          (`"factory.yaml"`), lint.py:309 (`"commands"`), render.py:219
          (`"README.md"`), analyze.py:469 (`"REGISTRY-SCHEMA.md"`).
          Note also line 287: `scan_code_literals` exempts `lint.py` from its own
          scan. Re-ran the scanner's logic against `lint.py` with the exemption
          removed: 0 literals hidden today, so the exemption conceals nothing
          now — but it is an unbounded carve-out for the one file no other check
          covers.
Fix     : OPEN — the profile name is cheap to add (`profile.id`) and would have
          caught nothing so far. Paths are not: a literal like `"README.md"` is a
          filename, not a factory fact, and a naive path rule would fire on all
          of them. The repo has to decide which of C1's eight categories the
          check is actually claiming before the check can be widened honestly.
          Narrowing the CONSTITUTION's wording to match the check would also
          settle it, and is the smaller change.
Status  : OPEN

## F-35 — a consumer command types the authoritative phase list it also publishes
Where   : frontend/.claude/commands/orchestrate-module.md:43 ·
          backend/.claude/commands/orchestrate-module.md (same shape)
Expected: the file's own Step 0 — "resolve where governance lives (never type
          it) … The profile folder is the factory's to name; spelling it here
          makes a second profile an edit to this file." Consumers must resolve
          published facts from `platform/profile-summary.json`.
Actual  : the command applies that principle to the profile (`jq -r
          .paths.modules "$SUMMARY"`) and then breaks it for the phase list:
            Frontend execution phases, in order: `F1 → F2 → F3 → F4 → SEC-FE → ALIGN-FE`
          Those keys are published at
          `tracks.frontend.plans.exec.phases[].key`. Compared the typed list
          against the published one for both tracks: they AGREE today (frontend
          6/6, backend 8/8), so this is duplication, not drift — but nothing
          checks that they agree, and the list has already changed once (F4 was
          added to the frontend plan). `ALIGN-FE` is further used as an
          authoritative trigger at :518 and :547 ("when the phase just closed is
          `ALIGN-FE`"), which a runtime acts on. The narrative mentions of phase
          keys elsewhere in the file are illustrative and are not counted here.
Fix     : OPEN — either resolve the list from `$SUMMARY` at Step 0 alongside
          `$MODULES`, or add a guard to each consumer's CI comparing the typed
          list to the published one. The first removes the duplicate; the second
          keeps the prose readable and fails when it lies. Adding a new CI check
          is an extension, so it is not made here.
Status  : OPEN

## F-36 — generated consumer commands carry no marker, no freshness check, and have been hand-edited
Where   : backend/.claude/commands/{CU,FILE,FIN,MDL,NOTIF,SEC}/*.md
Expected: per-module commands are resolved output — "Check they were
          regenerated, not hand-edited." The factory's own generated files carry
          `lint.generated_marker` ("GENERATED by `gov.py render` … do not edit by
          hand") and `lint` fails on `C1-stale-render` when one drifts.
Actual  : none of the 12 per-module commands carries any marker (`head -5 | grep
          -ci generated` → 0 for all 12), and no check anywhere compares them to
          what `/generate-module-setup` would produce. They have in fact been
          edited by hand during implementation:
            git show ddd5d02 -- .claude/commands/FIN/execute-backend-test.md
            → 21 insertions, 13 deletions, inside a commit titled
              "feat(FiscalPeriod): Implement search functionality for fiscal periods"
          The edit rewrote the required-coverage totals (46+1 TCs → 101+2) and
          added a line telling the next reader not to trust the file:
            "Do not trust these id lists over the file: they are a convenience,
             and the plan has grown more than once. Enumerate the
             `TC:TC-FIN-*:START` markers actually present and use that."
          That sentence is the finding: a human hit stale generated output,
          could not regenerate it, and wrote a warning into the artifact instead.
Fix     : OPEN — either the generator stamps the marker and a consumer CI step
          fails when a command differs from a regeneration, or these files are
          reclassified as authored-once scaffolding that implementation owns, and
          the claim that they are generated output is dropped. Today they are
          treated as both.
Status  : OPEN

## F-37 — `CODEOWNERS` names one owner for all four paths, so it enforces nothing
Where   : governance-shared/CODEOWNERS · GOVERNANCE-SHARED-DESIGN.md §3
Expected: §3 states the mechanism and the table it enforces:
            *                                   @factory-maintainers
            /{profile_id}/modules/*/api-docs/   @backend-maintainers
            /{profile_id}/modules/*/backend/    @backend-maintainers
            /{profile_id}/modules/*/frontend/   @frontend-maintainers
          and the file's own header: "a write outside an owned path is refused at
          review". §4 concedes git cannot enforce visibility, and rests write
          enforcement on "التقسيم + `CODEOWNERS`".
Actual  : every rule in the shipped file resolves to the same principal:
            *                          @hesham421
            /erp/modules/*/api-docs/   @hesham421
            /erp/modules/*/backend/    @hesham421
            /erp/modules/*/frontend/   @hesham421
          "Most specific wins" decides nothing when all four are one owner: no
          write is ever outside an owned path, so the rule cannot fire in either
          direction. This is a check that cannot be made to fail.
          Second defect in the same file: it spells `erp` where the design writes
          `{profile_id}`. The second-profile test otherwise PASSES — I created
          `profiles/demo.yaml`, ran `GOV_PROFILE=demo gov.py lint` (which caught
          the id/stem mismatch correctly) and `render_all(write=False)`, and 12
          generated files regenerate with no code edit. But a real second profile
          would need `CODEOWNERS` edited, and that file is outside
          `factory/profiles/`. The `{profile_id}/…` partitions are the one place
          the design's own path variable was flattened to a literal.
Fix     : OPEN — the ownership split cannot be enforced by one account. Either
          three teams exist and the file names them, or §3 and §4 stop claiming
          `CODEOWNERS` enforces the partition and name the consumers' CI guards
          as the real mechanism (they do bite — proven below). The profile
          literal is separately fixable only if GitHub's path syntax is given a
          generated file, since `CODEOWNERS` cannot read `profile-summary.json`.
Status  : OPEN

## F-38 — an implemented module is "not declared", against the repo's own RESERVED rule
Where   : governance-tools/analyze.py:790 · :877 (`known`) ·
          shared/GOVERNANCE-CORE.md:58 · profiles/erp.yaml `vocabulary.module_prefixes`
Expected: `shared/GOVERNANCE-CORE.md` states the policy outright — "a module
          named during a run but not yet listed here is used as RESERVED (never
          blocked) and the list is extended later — no stage invents a code, but
          no stage waits on the profile either". And the module set has a
          declared authority: `versioning.authority: filesystem`, which is why
          `CFG.modules()` sweeps the filesystem rather than reading a list.
Actual  : `xref-resolve` builds its universe from one side only —
          `known = set(CFG.profile.vocabulary["module_prefixes"])` — and raises
          at blocking severity when a cross-module id names anything outside it:
          "`XM-NOTIF-001` names module `NOTIF`, which the profile's module
          registry does not declare". The two sources disagree in both
          directions:
            implemented but NOT in the registry : CU, FILE, NOTIF
            in the registry but NOT implemented : CTR, HR, INV, ORG, PRC, SLS
          NOTIF is a real module with a full partition, api-docs and execution
          state — `sync` counts it among the 7 — and a reference to it is
          CRITICAL. Live today: 2 such findings, and this one is new since the
          stored report (see F-39).
Fix     : OPEN — either `known` is the union of the registry and the filesystem
          (the RESERVED rule as written, and consistent with
          `versioning.authority`), or the registry is the authority and the
          RESERVED paragraph is deleted and the six unimplemented codes justified.
          The repo cannot settle it because it currently asserts both: one
          document says an unlisted module is never blocked, and one check
          blocks on exactly that. Not fixed here — changing `known` changes
          analyze verdicts for every module, which is a gate-affecting decision.
Status  : OPEN

## F-39 — the stored `analyze` report had drifted far from what the tool produces
Where   : governance-shared/erp/modules/SEC/_state/analyze-all.{md,json}
Expected: `_state/analyze-*.md` is generated state, and it carries the same
          "GENERATED … do not edit by hand" marker as every rendered file.
          `lint` fails on `C1-stale-render` when a rendered file drifts.
Actual  : re-running `gov.py analyze -m SEC` — the only write this review made
          to the real shared repo, and a normal side effect of the command —
          rewrote the report from **1 critical · 61 major · 0 minor** to
          **2 critical · 76 major · 1 minor**: 188 inserted lines, 36 deleted.
          The committed artifact understated SEC's blocking findings by 15
          majors and one critical. Nothing detects this: `C1-stale-render`
          covers `render`'s targets, not `analyze`'s, so a stale analyze report
          is indistinguishable from a current one until someone re-runs it.
Fix     : OPEN — either analyze reports get the same freshness check as rendered
          files (they are equally derived, and the marker already claims they
          are), or they are timestamped as point-in-time run records and no
          longer read as current state. The gate reads them, so the first is the
          safer reading — but adding that check is an extension, not a repair,
          so it is left here. The re-run report itself is left in place: it is
          the accurate one.
Status  : OPEN

## REVIEW — factory, flow, boundaries

Scope       : RAN — `lint` (before and after every change), the full suite,
              `sync` (all three consumer states forced via a decoy checkout on
              `GOV_BACKEND_CHECKOUT`), `feedback`, `waive-feedback` (on a copy
              of the shared checkout via `GOV_SHARED_CHECKOUT`), `verify-split`
              on four SEC track/plan pairs, `analyze -m SEC`, `render_all(
              write=False)` under a second profile, the import graph by AST,
              `scan_config` and `scan_code_literals` by deliberate injection,
              `_commit_in` pathspec-limiting and the detached-HEAD guard in a
              throwaway repo, and both consumers' CI guard steps line by line in
              both directions. READ ONLY — `run-pass`, `gate`, `split`,
              `publish`, `approve`, `archive`, and the P-stage engines. No pass
              was regenerated. One write reached the real shared repo:
              `analyze -m SEC` rewrites its own report as a normal side effect,
              and what it rewrote is F-39.
Arbitrary   : 7 — `versioning.{authority,delta_only,current_state,
              freeze_previous}` (the accessor has zero callers),
              `naming.module_code`, `publications.*.derived_from`, and the
              `status` half of every waiver row. Two candidates cleared as false
              positives: `ids.ears.patterns.*` and `markers.rules.split_unit`.
Coupling    : cycles 0 · modules that cannot load alone: none — all 16 import
              clean in isolation. `config` and `findings` depend on nothing;
              `contracts`, `idmodel`, `publications` on `config` alone, as
              claimed. `lint` importing `publications` and `render` is registry
              lookup and delegation, not the checker-imports-its-subject shape:
              `findings.py` exists precisely to keep that edge acyclic.
Hardcode    : factory 6 path literals (gov.py:1076 ×2, gov.py:1215, lint.py:309,
              render.py:219, analyze.py:469), none of which the check covers —
              F-34. consumers 2: the phase list in both `orchestrate-module`
              commands (F-35) and `erp` in `CODEOWNERS` (F-37). Everything else
              the consumers touch resolves from `platform/profile-summary.json`;
              the `erp` spellings in their CLAUDE.md and commands are all either
              `# e.g.` comments or explicitly labelled "the live value today",
              which is descriptive, not authoritative.
Flow        : none found in the tools. `fetch-inputs` folds 11 / 10 / 4 api-doc
              files for SEC / FIN / MDL — re-measured, exact — and both its
              source and destination resolve INSIDE the shared checkout, so
              nothing is copied between repositories; `reads_from: shared` is
              what makes that true, and removing it would silently reintroduce
              cross-repo copying. The step that would break first is not a tool:
              it is the next `/generate-module-setup FIN`, which would overwrite
              the hand-corrected coverage totals in
              `FIN/execute-backend-test.md` and restore 46+1 TCs over the
              corrected 101+2, with nothing to detect it (F-36).
Boundaries  : no write outside a partition found — including the placeholder
              forms; every `[MODULE]`/`$MODULE`/`{MODULE}` path in the consumer
              commands lands under that repo's own partition or under a
              factory-owned path it only reads. One CODEOWNERS/design mismatch,
              and the design is the half that is right (F-37). Both consumers'
              CI guards are byte-identical modulo the track name and both bite
              in both directions: the path-resolution step caught an injected
              missing path and an injected empty one, and the reappearance guard
              fired on a planted `governance/modules` and went quiet when it was
              removed. The `seen == 0` branch is real, not decorative — it
              examines 9 `*_path` keys across 9 files today.
Checks      : 13 made to fail on purpose — `scan_code_literals` (removed concept,
              stage id, phase key, 3 profile vocabulary terms), `scan_config`
              (all 5 of its invariants, one more than the 4 F-12 claimed),
              the publication-builder check (both the missing and the
              unregistered branch), `_commit_in` pathspec limiting (a file staged
              out of band did NOT ride along), the detached-HEAD guard, `sync`
              stale detection (2 of 3 states; the third was the defect),
              both consumers' CI guards, and `C5-profile`'s id/stem rule.
              `verify-split` needed no forcing — it is failing on real drift now
              and passes clean on the other three SEC tracks, which is both
              directions. 1 that could NOT be made to fail: `CODEOWNERS`, and
              that is F-37 — with one principal on all four rules there is no
              input that makes it fire.
Findings    : F-30 … F-39 — 2 FIXED · 8 OPEN
Known state : all four rows still say what they say. F-29 still names
              `test_gen/backend-test-plan-fin.md`, which still does not exist
              (FIN's `test_gen/` holds only the frontend plan and the execution
              manifest). SEC backend/test still reports exactly 2 missing and 3
              drifted blocks. `analyze` still returns MAJOR on SEC (C7.19 ×4,
              C7.21, C7.22, C7.23, C7.4, C7.6 ×2, C8.4) — content defects in the
              plans, not tool defects. 38 unanswered feedback items, re-measured
              exactly, across FIN, NOTIF, CU, FILE, MDL, SEC; the status
              vocabulary does return `UNRECOGNISED` rather than guessing, and
              the eleven ways `resolution` was being written are visible as
              `BINDING`, `THE`, `NOT`, `REPLACE`, `VALIDATE`, `ABSENT`,
              `UNRESOLVED`, `IMPLEMENTATION`, `BUILD-CREATE-SERVICE` — the
              parser reads the first word of prose and refuses to bucket it.
Baseline    : lint 0/0/0 · 262 passed, 1 skipped — measured first, and identical
              after both fixes.
Verdict     : yes for the factory and the flow; qualified for the boundaries.
              Each repo does know only where to read and where to write: the
              lever holds (§1's two named non-escapes are the only root-relative
              builds left that matter, and the third one I found in `render` is
              now closed), the import graph is acyclic, the shared checkout is
              the single home, and nothing crosses a repo boundary at any step I
              ran. What is NOT proven is the enforcement of that boundary
              against a party that does not want to respect it: `CODEOWNERS`
              cannot fire, so the consumers' CI guards are the only mechanism
              actually standing, and they check that governance has not come
              BACK into a consumer — not that a consumer stayed out of a
              partition it does not own. The load-bearing property §1 names —
              that the shared checkout sits INSIDE the factory root — is still
              undeclared in code, and `dispatch.ingest()` and
              `splitter._clean_previous()` still hold only because of it. It
              should be checked: it is one assertion, and it is the assumption
              every root-relative path in the toolkit rests on.
