# ERP Backend

This is the **backend** repository. It contains only Java source code, Maven build
configuration, and API integration tests.

> **Governance is external.**
> All AI skills, coding standards, architecture rules, and execution protocols
> live in the `governance-repo` repository. Do NOT recreate them here.

---

## External Dependency

This repository depends on **governance-repo**.

Before generating any code:

1. Confirm `governance-repo` is available in the workspace (see layout below).
2. Read `governance-repo/CLAUDE.md` — it is the canonical AI governance document.
3. Load the required skill from `governance-repo/.github/skills/backend/<skill-name>/SKILL.md`.
4. Load architecture context from `governance-repo/.github/context/backend.md`.

If `governance-repo` is missing:
- Stop implementation.
- Explain the missing dependency.
- Never recreate governance artifacts locally.
- Never duplicate governance files.

---

## Workspace Layout

This repository is one of four sibling repositories that form the ERP platform:

```
workspace/
  backend/          ← this repository
  frontend/
  deploy/
  governance-repo/
```

The repositories are independent — there is no monorepo root above them.

---

## Where to Find Governance

| Governance artifact | Location |
|---------------------|----------|
| Backend skills | `governance-repo/.github/skills/backend/` |
| Frontend skills | `governance-repo/.github/skills/frontend/` |
| Backend architecture context | `governance-repo/.github/context/backend.md` |
| Master entity registry | `governance-repo/master-registry.md` |
| AI commands | `governance-repo/.claude/commands/` |
| Task routing table | `governance-repo/CLAUDE.md` |
| Execution protocol | `governance-repo/CLAUDE.md` |

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
