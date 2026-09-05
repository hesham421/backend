# project-artifacts/

The single home for **reporting / non-impacting markdown** in this backend repo
— audits, investigation notes, decision records, and analysis writeups that
document *why* something is the way it is, but do not themselves drive code
generation or the governance pipeline.

Per the repo-root `CLAUDE.md` STRUCTURAL LAW: a report/investigation/decision
note goes here (flat), never at the root of `governance/`, never inside
`modules/` or `.claude/commands/`. Check here before inventing a new top-level
location for a document of that kind.

Current contents:
- `INTERFACE-VS-REST-AND-POM-STRUCTURE-RECOMMENDATION.md` — the rationale for the
  single-deployable, package-by-feature Maven layout (referenced by `pom.xml`
  and `Dockerfile`).
- `MDM-DEACTIVATE-INVARIANT-GAP.md` — OPEN governance decision: value `create`
  (DRV-004, existence-only) can strand an active value under an inactive type,
  which RULE-MDM-006's deactivate guard alone cannot prevent. No code changed;
  awaiting a spec decision.
