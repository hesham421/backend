# Skills: backend

Skill pack for the ERP backend (Spring Boot / JPA). This directory is the
home for every backend skill — read this file first when working under
`.claude/skills/`. Moved here 2026-08-31 from
`governance/.github/skills/backend/` so these skills auto-load via the
Skill tool in every session; `governance/GOVERNANCE-RULES.md` remains the
authoritative routing table for which skill to use and when.

## Build skills (Phase 1, per entity — in order)

| Step | Skill | Use when |
|---|---|---|
| 1.1 | [create-entity](create-entity/SKILL.md) | Starting any new backend artifact — JPA entity + Domain companion object. Must be done first. |
| 1.2 | [create-repository](create-repository/SKILL.md) | After the entity exists — JpaRepository + JpaSpecificationExecutor interface |
| 1.3 | [create-dto](create-dto/SKILL.md) | After the repository — CreateRequest, UpdateRequest, Response, SearchRequest, UsageResponse, OptionResponse |
| 1.4 | [create-mapper](create-mapper/SKILL.md) | After DTOs, before the service — entity-to-DTO `@Component` mapper |
| 1.7 | [create-service](create-service/SKILL.md) | Application orchestration: CRUD, search, activate/deactivate, transactions, caching |
| 1.8 | [create-controller](create-controller/SKILL.md) | Final step — thin `@RestController` delegating all logic to the service |

## Enforcement skills (review)

| Skill | Use when |
|---|---|
| [enforce-backend-contract](enforce-backend-contract/SKILL.md) | After any backend code generation or review — validates all 85 contract rules across 7 layers |
| [enforce-error-handling](enforce-error-handling/SKILL.md) | Reviewing exception handling — rejects raw exceptions, enforces `LocalizedException` + registered error codes |
| [enforce-caching-rules](enforce-caching-rules/SKILL.md) | Reviewing `@Cacheable`/`@CacheEvict` usage against the approved entity list |
| [validate-backend-feature](validate-backend-feature/SKILL.md) | Final review of a completed feature — runs all enforcement checks end to end |

## Adding a new skill

1. Create `<skill-name>/SKILL.md` with `name` and `description` frontmatter.
2. Add a row to the appropriate table above.
3. If the skill changes one of the 85 contract rules, update `enforce-backend-contract` accordingly.
