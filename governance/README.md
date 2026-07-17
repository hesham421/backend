# backend/governance/

This is the backend-owned slice of the ERP platform's AI governance content, migrated here from the standalone `governance-repo` repository on 2026-07-17 (see the workspace root's `DEEP-DIVE-BACKEND-FRONTEND-SPLIT.md` and `ARCHITECTURE-OVERVIEW.md` for why and what moved). It holds almost everything: planning docs, execution phase specs (including the frontend-implementation phases F1-F4), JUnit test scenarios, the SECURITY module, and all governance tooling.

## Layout

```
governance/
  CLAUDE.md              ← canonical AI governance document (Phase Execution Protocol)
  GOVERNANCE-RULES.md     ← skill routing table, execution order
  WORKSPACE.md            ← historical layout doc — describes the OLD 4-repo sibling
                             model, not updated for the split (see the note below)
  master-registry.md      ← master entity/module registry
  modules-registry.json   ← dynamic module registry, read/written by governance-tools/config.py
  vision.md
  .gitignore              ← nested ignore rules (e.g. __pycache__, node_modules) for this subtree

  modules/                ← one folder per backend module
    FILESVC/
    NOTIFICATION/
    ORG/
    SECURITY/             ← unchanged internal structure ("permanent exception" module)
    each module contains (where applicable):
      P0/ P1/ P2/ P3/ P3_5/ P4/    ← planning stages
      packages/execution/<PHASE>/  ← split execution-plan.md, one folder per phase (incl. F1-F4)
      packages/test/JUNIT/         ← split test-plan.md, backend-executed scenarios
      api-docs/                    ← auto-generated from live backend source, do not hand-edit
      execution-state.json         ← current phase/sub status, hand-maintained by whichever
                                      AI agent is executing a phase (no script reads/writes this)
      manifest.json, packages/_agent3-state.json  ← bookkeeping from the original split tooling
      test-api/                    ← standalone Python scripts that hit a live running backend

  .claude/commands/       ← 9 slash commands (execute-<module>[-test].md, execute-security-gaps.md,
                             generate-module-setup.md, TEST-EXECUTION-AGENT.md)
  .github/
    copilot-instructions.md
    context/               ← architecture context docs (api-contract, backend, domain-layer, frontend)
    skills/backend/         ← 9 backend task skills
    skills/devops/          ← 1 deploy skill

  governance-tools/       ← the Python pipeline (see governance-tools/README.md)
    config.py               ← REPO_BASE_PATH points at this governance/ folder
    marker_parser.py
    agent1_create_structure.py
    agent2_archive.py
    agent3_splitter.py
    api-doc-generator/       ← generates modules/<MOD>/api-docs/ from live backend source + a
                                 running backend server (see its own README.md)

  mcp-servers/
    postgres/                ← Postgres MCP server; NOT how it's wired in — see below
    playwright/               ← Playwright MCP server; its config was MERGED into backend/.mcp.json.
                                 frontend/ does NOT vendor a copy of this — frontend/.mcp.json instead
                                 wires its own playwright entry via `npx @playwright/mcp@latest`,
                                 matching frontend/package.json's own pre-existing `mcp:playwright`
                                 script (see frontend/governance/README.md). A vendored copy was
                                 placed there briefly during the frontend-independence work, then
                                 reverted once it became clear frontend/ already had its own
                                 npx-based convention for this and vendoring it too would have just
                                 duplicated node_modules for no benefit.

  project-artifacts/       ← non-governance investigation reports / audits, backend + shared

  governance-shared/       ← EMPTY placeholder, reserved for a future git submodule.
                              Do not put content here manually; do not init a submodule here
                              without a deliberate, separate decision.
```

## Things that are NOT obvious from the file tree

- **The MCP servers are not wired via a `.mcp.json` inside this folder.** Claude Code reads MCP config from a repo root, not an arbitrary subfolder, so both `governance/mcp-servers/postgres/` and `governance/mcp-servers/playwright/` are referenced from `backend/.mcp.json` (one level up, at the actual repo root) via relative paths like `governance/mcp-servers/postgres/index.js`. `frontend/.mcp.json` also has its own `playwright` entry, but via `npx @playwright/mcp@latest` rather than a vendored local copy (postgres was not duplicated at all — frontend has no direct DB access use case today).
- **`execution-state.json` is hand-maintained, not generated.** No script in `governance-tools/` reads or writes it. It's edited directly by whichever AI agent is executing a phase, per the protocol in `CLAUDE.md`. Its schema also isn't uniform across modules — SECURITY's copy (`modules/SECURITY/gaps/execution-state.json`) has a different shape entirely (no `test_phases[]`, a `handoff` field per phase) since SECURITY never went through the standard P0-P4 pipeline. Note also: several modules' `execution-state.json` still has a stale `api_docs_path` value pointing at the old `governance-repo/modules/...` prefix instead of `governance/modules/...` — a leftover from before the migration that nothing validates or auto-fixes.
- **`WORKSPACE.md` is stale**, and now says so at the top of the file (a one-line banner was added rather than deleting it, since it still has some historical detail). It still describes the pre-split "four independent sibling repositories" model and was deliberately left as-is during the split (only `CLAUDE.md` files were rewritten). Don't trust its layout description — trust this README and `ARCHITECTURE-OVERVIEW.md` at the workspace root instead.
- **`packages/execution/F1-F4` living here, not in `frontend/`, is deliberate**, not an oversight — the split kept all of `packages/execution/` together with the tooling and slash commands that drive it. `frontend/governance/` only has the split-out PLAYWRIGHT test scenarios, frontend skills, and (as of the Phase 2 follow-up) a reference copy of each module's `P3_5/test-plan.md` plus its own local Playwright MCP server — not the phase specs themselves, and not the splitter tooling. See `frontend/governance/README.md`'s "PLAYWRIGHT re-verification" section for the exact boundary.
- **`api-doc-generator` needs a running backend, not just checked-out source.** It fetches live OpenAPI JSON over HTTP from the running Spring Boot app (auto-discovering the port), and separately reads Java source on disk for permissions/error-codes/pagination info that OpenAPI can't express. Both are needed for full output; `--openapi <file>` alone skips the network call but also skips those extra sections.
- **`governance-tools/config.py`'s `REPO_BASE_PATH` now points at this exact folder** (`backend/governance`). If this folder is ever moved again, that constant needs a matching update — it's a hardcoded absolute path, not derived from the file's own location.

## Running the tooling

```bash
cd backend/governance/governance-tools

# Regenerate api-docs/ for a module (needs backend running on its dev port, or --openapi <file>)
python3 api-doc-generator/generate.py --module ORG --function generate

# Check a module's split-tooling progress (does NOT touch execution-state.json)
python3 agent3_splitter.py --module ORG --status

# List known/registered modules
python3 agent1_create_structure.py --list-modules
```
