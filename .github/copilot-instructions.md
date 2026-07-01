# ERP Backend — GitHub Copilot Instructions

This is the **backend** repository. It contains only Java source code and tests.

## Governance Dependency

All AI skills, coding standards, architecture contracts, and execution protocols
are defined in the **governance-repo** repository.

**Before generating any code:**
1. Confirm `governance-repo` is available in the workspace.
2. Read `governance-repo/.github/copilot-instructions.md` — it contains the full skill routing table.
3. Load the required skill from `governance-repo/.github/skills/backend/<skill-name>/SKILL.md`.
4. Load architecture context from `governance-repo/.github/context/backend.md`.

If `governance-repo` is not available: **stop and report the missing dependency.**
Do NOT recreate governance content locally.

## This Repository

```
pom.xml                  ← Parent POM (multi-module Maven)
erp-common-utils/        ← Shared utilities, base classes, error codes
erp-security/            ← JWT auth, users, roles, permissions
erp-masterdata/          ← Lookup tables and master data
erp-org/                 ← Organizational structure
erp-finance-gl/          ← General ledger
erp-main/                ← Spring Boot entry point
Dockerfile               ← Multi-stage Maven → JRE image
docker/docker-compose.yml ← Local development Postgres
tests/                   ← Playwright API integration tests
```

## Quick Skill Reference

> Full skill files are in `governance-repo/.github/skills/backend/`

| Task | Skill |
|------|-------|
| Create Entity | `create-entity` |
| Create Repository | `create-repository` |
| Create DTOs | `create-dto` |
| Create Mapper | `create-mapper` |
| Create Service | `create-service` |
| Create Controller | `create-controller` |
| Validate contract | `enforce-backend-contract` |
| Validate feature | `validate-backend-feature` |
