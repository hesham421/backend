# ERP Frontend — GitHub Copilot Instructions

This is the **frontend** repository. It contains only Angular source code.

## Governance Dependency

All AI skills, coding standards, architecture contracts, and execution protocols
are defined in the **governance-repo** repository.

**Before generating any code:**
1. Confirm `governance-repo` is available in the workspace.
2. Read `governance-repo/.github/copilot-instructions.md` — it contains the full skill routing table.
3. Load the required skill from `governance-repo/.github/skills/frontend/<skill-name>/SKILL.md`.
4. Load architecture context from `governance-repo/.github/context/frontend.md`.

If `governance-repo` is not available: **stop and report the missing dependency.**
Do NOT recreate governance content locally.

## This Repository

```
angular.json / package.json / tsconfig.json
Dockerfile           ← Multi-stage Node → Nginx image
nginx.conf           ← Nginx config (used by Dockerfile Stage 2)
src/
  app/
    core/
    layout/
    modules/         ← Feature modules live here
    shared/
    theme/
  assets/i18n/       ← en.json and ar.json translation files
```

## Quick Skill Reference

> Full skill files are in `governance-repo/.github/skills/frontend/`

| Task | Skill |
|------|-------|
| Create TypeScript models | `create-models` |
| Create API service | `create-api-service` |
| Create Facade (state) | `create-facade` |
| Create Routing | `create-routing` |
| Create Components | `create-components` |
| Validate architecture | `enforce-frontend-architecture` |
| Validate feature | `validate-frontend-feature` |

## Architecture Rules

> ERP rules always take precedence over Angular conventions. These rules are
> owned by the skills below — read the skill for current detail, do not rely
> on a restatement here.

| Concern | Canonical skill |
|---------|-----------------|
| State management (signals vs. `BehaviorSubject`) | `enforce-state-management` |
| Component/architecture contracts (`standalone`, `OnPush`) | `enforce-frontend-architecture` |
| Facade / API service provider scoping | `create-facade`, `create-api-service` |

Build output: `dist/n-erp-system/` (no `/browser` subdirectory) — a repository
build fact, not a governance rule.
