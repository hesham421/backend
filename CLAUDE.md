# ERP Backend

This is the **backend** repository. It contains Java source code, Maven build
configuration, API integration tests, and (as of the backend/frontend governance
split) its own internal copy of the AI governance content that applies to backend
work.

> **Governance lives inside this repository now.**
> All backend-relevant AI skills, coding standards, architecture rules, and
> execution protocols live in `governance/` (this repo's own subfolder). Do not
> look for a separate sibling `governance-repo/` repository — the pointer-based
> external-governance model has been replaced by an internal `governance/` copy.

---

## Internal Governance

Before generating any code:

1. Read `governance/CLAUDE.md` if present, otherwise treat this file plus
   `governance/GOVERNANCE-RULES.md` (if present) as canonical.
2. Load the required skill from `governance/.github/skills/backend/<skill-name>/SKILL.md`.
3. Load architecture context from `governance/.github/context/backend.md` (if present).
4. For a specific module's phase execution, read `governance/modules/[MODULE]/execution-state.json`
   and follow `governance/.claude/commands/execute-[module].md`.

If `governance/` is missing or looks incomplete for what you need:
- Stop implementation.
- Explain the missing dependency.
- Never invent governance content to fill the gap.

Note: a `governance/governance-shared/` placeholder folder exists, reserved for
a future git submodule carrying genuinely cross-repo shared governance content
(e.g. anything still needed by `frontend/`). It is intentionally empty — do not
treat it as a source of truth yet.

---

## Workspace Layout

This repository is one of several repositories that form the ERP platform. As of
the backend/frontend governance split, `backend/` and `frontend/` each carry
their own internal `governance/` copy rather than pointing at one external
sibling repository:

```
workspace/
  backend/          ← this repository (includes governance/)
  frontend/         ← includes its own governance/
  deploy/
  governance-repo/  ← original source-of-truth checkout; some content not yet
                       migrated still lives here only (see governance/governance-tools/
                       for tooling that was copied out, and governance-repo/ itself
                       for anything not yet classified/moved)
```

---

## Where to Find Governance

| Governance artifact | Location |
|---------------------|----------|
| Backend skills | `governance/.github/skills/backend/` |
| Backend architecture context | `governance/.github/context/backend.md` |
| Master entity registry | not yet migrated — still only in `governance-repo/master-registry.md` |
| AI commands | `governance/.claude/commands/` |
| Module execution state | `governance/modules/[MODULE]/execution-state.json` |
| Governance tooling (splitter, api-doc-generator, etc.) | `governance/governance-tools/` |
| Postgres MCP server | `governance/mcp-servers/postgres/` |
| SECURITY module (permanent exception) | `governance/modules/SECURITY/` |

---

## Repository Structure

```
pom.xml                  ← Parent POM (multi-module Maven project)
erp-common-utils/        ← Shared utilities, base classes, error codes
erp-security/            ← JWT auth, users, roles, permissions
erp-masterdata/          ← Lookup tables and master data
erp-org/                 ← Organizational structure
erp-finance-gl/          ← General ledger
erp-main/                ← Spring Boot entry point, assembles all modules
Dockerfile               ← Multi-stage Maven → JRE image
docker/
  docker-compose.yml     ← Local development database (Postgres)
tests/
  masterdata-api-test.ps1
  probe-failures.ps1
governance/               ← internal AI governance copy (see above)
playwright.config.ts     ← API integration test runner config
package.json             ← Playwright dev dependency only
.env.example             ← Environment variable template
```

---

## Running Locally

```bash
# Start local Postgres
docker compose -f docker/docker-compose.yml up -d

# Build and run the Spring Boot application
./mvnw spring-boot:run -pl erp-main

# API integration tests (requires backend running on localhost:7272)
npx playwright test

# Or run the PowerShell test runner directly
powershell -ExecutionPolicy Bypass -File tests/masterdata-api-test.ps1
```

Copy `.env.example` to `.env` and fill in values before running locally.

---

## STRUCTURAL LAW — DO NOT DEVIATE

> This section is binding, not a proposal. It formalizes the backend/frontend governance split that is already implemented and verified on disk — see workspace-root `ARCHITECTURE-OVERVIEW.md`, `backend/governance/README.md`, `frontend/governance/README.md`, and `GOVERNANCE-SPLIT-PHASE-2-COMPLETION.md`. It locks in what already exists; it does not redesign anything. This exact section also appears in `frontend/CLAUDE.md` — the two copies are identical and must stay that way.

### Ownership table

| Content type | Lives in | Never in |
|---|---|---|
| `CLAUDE.md`, `GOVERNANCE-RULES.md`, `WORKSPACE.md`, `master-registry.md`, `modules-registry.json`, `vision.md` | `backend/governance/` | `frontend/governance/` |
| P0-P4 planning docs (per module) | `backend/governance/modules/<MOD>/` | `frontend/governance/` — except the `P3_5/test-plan.md` reference copies noted below |
| `packages/execution/<PHASE>/` (CORE, DATA-DOM/DATAOM, SVC-API/SVCAPI, DOC, INT-C/INTC, INT-R/INTR, F1, F2, F3, F4, SEC, ALIGN) — including the F1-F4 frontend-*implementation* phase specs | `backend/governance/modules/<MOD>/packages/execution/` | `frontend/governance/` |
| `packages/test/JUNIT/` (JUnit test scenarios) | `backend/governance/modules/<MOD>/packages/test/JUNIT/` | `frontend/governance/` |
| `packages/test/PLAYWRIGHT/` (Playwright UI/E2E scenarios) | `frontend/governance/modules/<MOD>/packages/test/PLAYWRIGHT/` | `backend/governance/` — backend keeps none of these |
| `P3_5/test-plan.md` | Source of truth: `backend/governance/modules/<MOD>/P3_5/`. A marked `REFERENCE COPY` also exists at `frontend/governance/modules/<MOD>/P3_5/` for audit purposes only. | Nowhere else. Never hand-edit the frontend copy; never treat it as a second source of truth. |
| `execution-state.json` | `backend/governance/modules/<MOD>/` (and `SECURITY/gaps/`) | `frontend/governance/` — frontend has no copy |
| `governance-tools/` (`config.py`, `marker_parser.py`, `agent1_create_structure.py`, `agent2_archive.py`, `agent3_splitter.py`, `api-doc-generator/`) | `backend/governance/governance-tools/` only | `frontend/governance/` — never duplicated, never re-implemented |
| `.claude/commands/` (slash commands) | `backend/governance/.claude/commands/` only | `frontend/governance/` |
| `.github/skills/backend/`, `.github/skills/devops/` | `backend/governance/.github/skills/` | `frontend/governance/` |
| `.github/skills/frontend/` | `frontend/governance/.github/skills/` | `backend/governance/` |
| `mcp-servers/postgres/` | `backend/governance/mcp-servers/postgres/` only, wired via `backend/.mcp.json` | `frontend/governance/` — not duplicated (no frontend DB access use case) |
| `mcp-servers/playwright/` | Both: `backend/governance/mcp-servers/playwright/` (wired via `backend/.mcp.json`) AND an independent copy at `frontend/governance/mcp-servers/playwright/` (wired via `frontend/.mcp.json`). This is the one deliberate, established duplication pattern besides `P3_5`. The two copies do not auto-sync. | — |
| SECURITY module (all of it — "PERMANENT EXCEPTION") | `backend/governance/modules/SECURITY/` | `frontend/governance/` |
| `api-docs/` (auto-generated) | `backend/governance/modules/<MOD>/api-docs/` | `frontend/governance/` |
| `project-artifacts/` (backend + shared) | `backend/governance/project-artifacts/` | — |
| `project-artifacts/frontend/` | `frontend/governance/project-artifacts/frontend/` | `backend/governance/` |
| `governance-shared/` | Empty placeholder in both repos, reserved for a future git submodule | Do not put content in either copy without a separate, explicit human decision |
| `governance-repo/` (workspace root) | Superseded remnant only (`reports/` folder + an inert timestamped backup) | Not a source of truth for anything, in either repo |

### If you are about to do X, the answer is always Y

- **About to add a new execution-plan phase file, JUnit scenario, or slash command?** → `backend/governance/`. Never `frontend/governance/`.
- **About to add a new PLAYWRIGHT scenario or frontend skill?** → `frontend/governance/`. Never `backend/governance/`.
- **Found yourself wanting to copy a NEW file from `backend/` into `frontend/` (or vice versa) that isn't already an established reference-copy pattern (`P3_5/test-plan.md`, the Playwright MCP server)?** → STOP. This requires an explicit human decision, not silent duplication. Ask first.
- **About to regenerate PLAYWRIGHT content locally in `frontend/`?** → Not possible by design. Regeneration only happens via `backend/governance/governance-tools/agent3_splitter.py`; `frontend/` only re-copies the output afterward.
- **About to write to `governance-shared/` or initialize a submodule there?** → Forbidden until a separate, explicit human decision authorizes it.
- **About to edit `execution-state.json` via a script?** → Forbidden. It's hand-maintained only, per the agent phase-execution protocol — no script reads or writes it.
- **About to treat `governance-repo/` (the empty shell at the workspace root) as a source of truth for anything?** → Forbidden. It's a superseded remnant; only its git history has archival value.

### No new top-level content categories without explicit confirmation

Do not create a new folder type, a new cross-repo dependency, or a new "shared" location under either `governance/` tree without first producing a short written justification and getting explicit human confirmation — the same rigor the original split itself required (evidence-based analysis → dry-run plan → confirmation → execution). Never skip straight to implementation.

### Decision authority

This structure was deliberately designed and verified (see workspace-root ARCHITECTURE-OVERVIEW.md and GOVERNANCE-SPLIT-PHASE-2-COMPLETION.md) to keep backend/ and frontend/ independently workable without opening the other. Any deviation — new duplication, new cross-repo path reference, new shared location — requires the same rigor the original split required: evidence-based analysis, a dry-run plan, and explicit human confirmation. Silent structural drift is treated as a governance violation, not a convenience.
