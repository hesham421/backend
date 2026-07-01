# ERP Deploy — GitHub Copilot Instructions

This is the **deploy** repository. It contains only Docker Compose orchestration
and deployment scripts.

## Governance Dependency

All AI skills, coding standards, and deployment architecture rules are defined
in the **governance-repo** repository.

**Before modifying deployment configuration:**
1. Confirm `governance-repo` is available in the workspace.
2. Read `governance-repo/.github/copilot-instructions.md`.
3. Load the deploy skill from `governance-repo/.github/skills/devops/deploy/SKILL.md`.

If `governance-repo` is not available: **stop and report the missing dependency.**

## This Repository

```
docker-compose.yml   ← Production orchestration
deploy.sh            ← Git pull → build → restart
.env.example         ← Environment variable template
.env                 ← Live secrets (never committed)
README.md            ← Deployment runbook
```

## Repository Facts

- Backend image: `context: ../backend`, `dockerfile: Dockerfile`
- Frontend image: `context: ../frontend`, `dockerfile: Dockerfile`

## Architecture Rules

> Deployment architecture (network mode, reverse-proxy topology, deployment
> model) is owned by the skill below — read it for current detail, do not rely
> on a restatement here.

| Concern | Canonical skill |
|---------|-----------------|
| Network mode, Nginx proxy topology, Git-pull deployment model | `devops/deploy` |
