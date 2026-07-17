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
