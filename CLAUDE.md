# ERP Backend

This is the **backend** repository. It contains Java source code, Maven build
configuration, API integration tests, and its own copy of the AI governance
content that applies to backend work.

> **Governance lives inside this repository.**
> All backend-relevant AI skills, coding standards, architecture rules, and
> execution protocols live in `governance/` (this repo's own subfolder).

---

## Internal Governance

Before generating any code:

1. Read `governance/CLAUDE.md` if present, otherwise treat this file plus
   `governance/GOVERNANCE-RULES.md` (if present) as canonical.
2. Load the required skill from `governance/.github/skills/backend/<skill-name>/SKILL.md`.
3. Load architecture context from `governance/.github/context/backend.md` (if present).
4. For a specific module's phase execution, read
   `governance/modules/[MODULE]/execution-state.json` and follow
   `governance/.claude/commands/execute-backend.md`
   (or `execute-backend-test.md` for the test phase — see
   `.claude/commands/generate-module-setup.md` for how these get generated).

If `governance/` is missing or looks incomplete for what you need:
- Stop implementation.
- Explain the missing dependency.
- Never invent governance content to fill the gap.

`governance/governance-shared/` is an empty placeholder, reserved for a
future git submodule carrying genuinely cross-repo shared content. It is
intentionally empty — do not treat it as a source of truth yet, and do not
put content there without a separate, explicit human decision.

---

## Workspace Layout

This repository is one of two that form the ERP platform. `backend/` and
`frontend/` each carry their own internal `governance/` copy:

```
workspace/
  backend/          ← this repository (includes governance/)
  frontend/         ← includes its own governance/
  deploy/
```

There is no external sibling governance repository — everything a session
in this repo needs lives inside `backend/governance/`.

---

## Where to Find Governance

| Governance artifact | Location |
|---------------------|----------|
| Backend skills | `governance/.github/skills/backend/` |
| Backend architecture context | `governance/.github/context/backend.md` |
| Master entity registry | `governance/master-registry.md` |
| Modules registry | `governance/modules-registry.json` |
| AI commands | `governance/.claude/commands/` |
| Module execution state | `governance/modules/[MODULE]/execution-state.json` |
| Governance tooling (splitter, api-doc-generator, etc.) | `governance/governance-tools/` |
| Postgres MCP server | `governance/mcp-servers/postgres/` |
| Playwright MCP server (backend-side copy, for API integration tests) | `governance/mcp-servers/playwright/` |
| SECURITY module (permanent exception) | `governance/modules/SECURITY/` |
| Reporting / non-impacting markdown (see below) | `governance/project-artifacts/` |

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

**Requires JDK 25.** `pom.xml` pins `java.version`/`maven.compiler.release` to 25 and a
`maven-enforcer-plugin` rule fails the build immediately (at `validate`, before any compilation)
under any other JDK, with a message naming the JDK actually in use. Point `JAVA_HOME` at a JDK 25
install before running any `mvn`/`./mvnw` command below.

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

## Reporting / Non-Impacting Markdown Files — ALWAYS under `project-artifacts/`

Any markdown file that is purely informational — a report, an investigation
note, a design-decision writeup, an audit summary — and does not itself
drive generation, execution, or another agent's behavior, belongs under:

```
governance/project-artifacts/
```

Never at the root of `governance/`, never inside `modules/<MOD>/`, and
never inside `.claude/commands/`. This keeps the folders that ARE read by
agents (`modules/`, `.claude/commands/`, `governance-tools/`) free of files
that exist only for a human to read later. If you're about to create a new
`.md` file and it doesn't fit one of the canonical categories in the
ownership table below, it almost certainly belongs in
`project-artifacts/` — check there before inventing a new top-level location.

---

## STRUCTURAL LAW — DO NOT DEVIATE

> This section is binding, not a proposal. It formalizes the backend/frontend
> governance split. This exact section also appears in `frontend/CLAUDE.md` —
> the two copies are identical and must stay that way.

### Ownership table

| Content type | Lives in | Never in |
|---|---|---|
| `CLAUDE.md`, `GOVERNANCE-RULES.md`, `WORKSPACE.md`, `master-registry.md`, `modules-registry.json`, `vision.md` | `backend/governance/` | `frontend/governance/` |
| P0, P0.5, P1, P2, P2.5 (text only — flow-diagram.md, ui-ux-spec.md), P3.1, P3.5_BE, P4.1 planning docs (per module) | `backend/governance/modules/<MOD>/` | `frontend/governance/` |
| P2.5 mockups (`visual-mockups/`, rendered via Claude Design) | `frontend/governance/modules/<MOD>/P2_5-mockups/` | `backend/governance/` — this is the one P2.5 artifact type that lives in frontend, since a developer building the UI Shell needs it right there |
| `packages/backend-execution/<PHASE>/` (CORE, DATA-DOM, SVC-API, DOC, INT-C, INT-R, SEC-BE, ALIGN-BE) | `backend/governance/modules/<MOD>/packages/backend-execution/` | `frontend/governance/` |
| `packages/backend-test/` (JUnit scenarios) | `backend/governance/modules/<MOD>/packages/backend-test/` | `frontend/governance/` |
| `packages/frontend-execution/<PHASE>/` (F1, F2, F3, F4, SEC-FE, ALIGN-FE) | `frontend/governance/modules/<MOD>/packages/frontend-execution/` | `backend/governance/` — backend keeps none of these |
| `packages/frontend-test/` (Playwright UI/E2E scenarios) | `frontend/governance/modules/<MOD>/packages/frontend-test/` | `backend/governance/` — backend keeps none of these |
| `P3_2/frontend-execution-plan.md`, `P3_5_FE/frontend-test-plan.md`, `P4_2/` | Natively generated in `frontend/governance/modules/<MOD>/` — never a copy of anything backend-owned | `backend/governance/` |
| `execution-state.json` | Two SEPARATE files, one per repo: `backend/governance/modules/<MOD>/` (and `SECURITY/gaps/`) AND `frontend/governance/modules/<MOD>/` — never merged, never synced automatically | — |
| `governance-tools/` (`config.py`, `marker_parser.py`, `agent1_create_structure.py`, `agent2_archive.py`, `agent3_splitter.py`, `api-doc-generator/`) | **Identical copy in BOTH repos** — `backend/governance/governance-tools/` and `frontend/governance/governance-tools/`. Both copies resolve the same two repo roots via hardcoded absolute paths in `config.py` — `--track backend`/`--track frontend` work identically from either copy. No automatic sync; keep both copies byte-identical by hand. | — |
| `.claude/commands/generate-module-setup.md` | **Identical copy in BOTH repos**, same reason as `governance-tools/` — Claude Code resolves slash commands from the current repo's own `.claude/commands/` folder | — |
| `.claude/commands/execute-backend.md`, `execute-backend-test.md` (generated output, not templates) | `backend/governance/.claude/commands/` only | `frontend/governance/` |
| `.claude/commands/execute-frontend.md`, `execute-frontend-test.md` (generated output, not templates) | `frontend/governance/.claude/commands/` only | `backend/governance/` |
| `.github/skills/backend/`, `.github/skills/devops/` | `backend/governance/.github/skills/` | `frontend/governance/` |
| `.github/skills/frontend/` | `frontend/governance/.github/skills/` | `backend/governance/` |
| `mcp-servers/postgres/` | `backend/governance/mcp-servers/postgres/` only, wired via `backend/.mcp.json` | `frontend/governance/` — no frontend DB access use case |
| `mcp-servers/playwright/` | Both: `backend/governance/mcp-servers/playwright/` (API integration tests, wired via `backend/.mcp.json`) AND an independent copy at `frontend/governance/mcp-servers/playwright/` (wired via `frontend/.mcp.json`). Same manual-sync caveat as `governance-tools/`. | — |
| SECURITY module (all of it — "PERMANENT EXCEPTION") | `backend/governance/modules/SECURITY/` | `frontend/governance/` |
| `api-docs/` (auto-generated) | `backend/governance/modules/<MOD>/api-docs/` | `frontend/governance/` — frontend reads this via the sanctioned cross-repo path, never gets its own copy |
| Reporting / non-impacting markdown | `backend/governance/project-artifacts/` (this repo's own reports) and `frontend/governance/project-artifacts/frontend/` (frontend's own) | Root of either `governance/` tree, or inside `modules/`/`.claude/commands/` |
| `governance-shared/` | Empty placeholder in both repos, reserved for a future git submodule | Do not put content in either copy without a separate, explicit human decision |

### If you are about to do X, the answer is always Y

- **About to add a new backend execution-plan phase file, JUnit scenario, or a generated `execute-backend*.md` command?** → `backend/governance/`. Never `frontend/governance/`.
- **About to add a new Playwright scenario, frontend skill, or a generated `execute-frontend*.md` command?** → `frontend/governance/`. Never `backend/governance/`.
- **About to edit `governance-tools/*.py` or `.claude/commands/generate-module-setup.md`?** → Edit it, then apply the IDENTICAL change to the other repo's copy by hand. Never let the two drift.
- **About to write a report, investigation note, or audit writeup?** → `governance/project-artifacts/`. Never the root of `governance/`, never inside `modules/`.
- **Found yourself wanting to copy a NEW file from `backend/` into `frontend/` (or vice versa) that isn't already an established dual-copy pattern (`governance-tools/`, `generate-module-setup.md`, the Playwright MCP server)?** → STOP. This requires an explicit human decision, not silent duplication. Ask first.
- **About to regenerate frontend package content (`packages/frontend-execution/`, `packages/frontend-test/`) from this repo?** → Not possible by design. `agent3_splitter.py --track frontend` only runs meaningfully from within `frontend/`'s own artifacts — this repo never holds frontend package output at all, so there's nothing here to regenerate from.
- **About to write to `governance-shared/` or initialize a submodule there?** → Forbidden until a separate, explicit human decision authorizes it.
- **About to edit `execution-state.json` via a script?** → Forbidden. It's hand-maintained only, per the agent phase-execution protocol — no script reads or writes it.

### No new top-level content categories without explicit confirmation

Do not create a new folder type, a new cross-repo dependency, or a new
"shared" location under either `governance/` tree without first producing
a short written justification and getting explicit human confirmation.
Never skip straight to implementation.

### Decision authority

This structure was deliberately designed to keep `backend/` and `frontend/`
independently workable without opening the other. Any deviation — new
duplication, new cross-repo path reference, new shared location — requires
evidence-based analysis, a dry-run plan, and explicit human confirmation.
Silent structural drift is treated as a governance violation, not a
convenience.