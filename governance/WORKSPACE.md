# ERP Workspace

This document is the official convention for how the four ERP repositories relate
to one another. It is referenced by both `CLAUDE.md` and `.github/copilot-instructions.md`
in this repository, and by the `CLAUDE.md` / `.github/copilot-instructions.md` files in
`backend`, `frontend`, and `deploy`.

---

## Expected Layout

The platform is made of four sibling repositories, checked out at the same level:

```
workspace/
  backend/          ← Java / Spring Boot source code
  frontend/         ← Angular source code
  deploy/           ← Docker Compose orchestration and deployment scripts
  governance-repo/  ← AI governance: skills, context, commands, registries
```

The repositories are independent — there is no monorepo root above them. Each
repository is expected to be cloned as a direct sibling of the other three so
that relative references such as `governance-repo/CLAUDE.md` resolve correctly
from any of them.

---

## Repository Relationships

| Repository | Owns | Depends on |
|---|---|---|
| `governance-repo` | AI governance: skills, context, commands, registries, execution plans | Nothing — it is the root of the dependency graph |
| `backend` | Java / Spring Boot source code, Maven build, API tests | `governance-repo` (governance only) |
| `frontend` | Angular source code, build config, Nginx config | `governance-repo` (governance only) |
| `deploy` | Docker Compose orchestration, deployment scripts, environment templates | `governance-repo` (governance only); builds `backend` and `frontend` images from their sibling directories |

`backend`, `frontend`, and `deploy` do not depend on each other's AI configuration.
Each depends only on `governance-repo` for governance, and `deploy` additionally
depends on `backend`/`frontend` at the infrastructure level (build contexts), not
at the AI-governance level.

---

## Governance Ownership

`governance-repo` is the single source of truth for:

- Claude Runtime (`.claude/`)
- GitHub Copilot Runtime entry point (`.github/copilot-instructions.md`)
- Skills (`.github/skills/`)
- Context (`.github/context/`)
- Governance rules and skill routing (`GOVERNANCE-RULES.md`)
- Entity/registry governance (`master-registry.md`, `modules-registry.json`)
- Module execution plans (`modules/`)

No other repository may redefine or duplicate this content. `backend`, `frontend`,
and `deploy` each contain repository-local documentation only, plus a pointer back
to `governance-repo`.

---

## Claude Runtime Ownership

Exactly one Claude runtime exists for the platform: `governance-repo/.claude/`.
It owns all slash commands (`.claude/commands/`) and is the only `.claude/`
directory in the workspace. `backend`, `frontend`, and `deploy` do not have their
own `.claude/` directories.

## Copilot Runtime Ownership

Each repository has its own `.github/copilot-instructions.md`, since GitHub
Copilot loads this file per-repository. In `backend`, `frontend`, and `deploy`,
this file is a thin entry point: it states the repository's own scope, points to
`governance-repo` for shared governance, and lists only genuinely repository-local
facts (its own directory structure, build output paths, etc.). In `governance-repo`,
this file is the Copilot-facing entry point into shared governance, and references
`GOVERNANCE-RULES.md` rather than restating it.

---

## Required Repositories

All four repositories must be present as siblings for AI-assisted development to
work as designed:

- `governance-repo` is always required — every other repository depends on it.
- `backend` and `frontend` are required for application development.
- `deploy` is required for building or running a deployment; it also requires
  `backend` and `frontend` to be present as siblings, since its Docker Compose
  build contexts point at `../backend` and `../frontend`.

---

## Expected Developer Workflow

1. Clone all four repositories as siblings under one parent folder.
2. Application code changes happen in `backend` or `frontend` only.
3. Before generating or modifying code, the AI agent reads its own repository's
   `CLAUDE.md` or `.github/copilot-instructions.md`, which points to
   `governance-repo` for the applicable skill, context file, and this document.
4. Deployment changes happen in `deploy`; the deploy skill in `governance-repo`
   governs Dockerfile/Compose/Nginx conventions.
5. Governance changes (new skills, updated rules, registry updates) happen only
   in `governance-repo`, never by editing the pointer files in the other three
   repositories.
