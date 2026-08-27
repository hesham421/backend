# Backend Governance

This is the **backend** repository's own copy of AI governance content —
skills, context, commands, registries, and execution state for backend work,
plus the single-source-of-truth routing rules that frontend also reads from
this repo (see [Sanctioned cross-repo reads](#sanctioned-cross-repo-reads)).

> Read the repository's top-level [`CLAUDE.md`](../CLAUDE.md) for the actual
> execution protocol — this file is a map of what lives where, not a
> restatement of the rules. Unlike `frontend/governance/CLAUDE.md`, this
> repo's execution protocol lives in the repo-root `CLAUDE.md`, not in a
> `governance/CLAUDE.md` — see the note at the bottom of this file.

---

## What lives here

| Content | Path |
|---|---|
| Single source of truth for skill routing (both backend and frontend tasks) | [`GOVERNANCE-RULES.md`](GOVERNANCE-RULES.md) |
| Backend skills | `.github/skills/backend/` |
| DevOps / deploy skill | `.github/skills/devops/` |
| Frontend skills (frontend code, but skills are frontend-repo-owned) | `frontend/governance/.github/skills/frontend/` — not here |
| Architecture context (backend, domain layer, API contract, frontend) | `.github/context/` |
| Master entity registry | `master-registry.md` |
| Modules registry | `modules-registry.json` |
| Module planning + execution artifacts | `modules/<MOD>/` |
| Governance automation tooling (independent, backend-only — NOT synced with frontend's copy) | `governance-tools/` |
| AI commands (generated setup, launcher prompts) | `.claude/commands/` |
| Postgres MCP server | `mcp-servers/postgres/` |
| Reporting / non-impacting markdown (audits, investigation notes) | `project-artifacts/` |
| Reserved for a future cross-repo shared-content submodule (do not populate) | `governance-shared/` |

`vision.md` also lives at this level, backend-owned per STRUCTURAL LAW in the
root `CLAUDE.md`.

---

## Sanctioned cross-repo reads

`frontend/governance/README.md` documents that the frontend repo reads exactly
two things out of here directly from disk, never writing to either and never
keeping its own copy: `modules-registry.json`, and each module's
`modules/<MOD>/api-docs/`. This repo does not read anything out of
`frontend/governance/` in return — the dependency is one-directional, per the
root `CLAUDE.md`'s STRUCTURAL LAW.

---

## About `governance/CLAUDE.md`

`frontend/governance/CLAUDE.md` exists because the frontend repo has no
repo-root `CLAUDE.md` of its own. This repo already has a comprehensive
repo-root `CLAUDE.md` covering the same ground (Internal Governance, Phase
Execution Protocol, Constraints, STRUCTURAL LAW). Adding a second, separate
`governance/CLAUDE.md` here would duplicate that content and create a new
two-file drift risk within a single repo, so this pass deliberately did not
create one — see `project-artifacts/` for the governance-drift report that
made this call explicit. If a future change makes the split genuinely
necessary, that is itself a structural decision requiring the same explicit
human confirmation STRUCTURAL LAW demands for any other new duplication.
