# ERP Governance Rules

Shared governance content for every AI runtime operating on this platform
(Claude Code via `CLAUDE.md`, GitHub Copilot via `.github/copilot-instructions.md`,
and any future runtime). This is the single copy of the skill routing table,
execution order, context references, and governance rules — runtime files must
reference this document, not restate it.

---

## Governance Content Map

| Artifact | Path in this repository |
|----------|------------------------|
| Backend skills | `.github/skills/backend/` |
| Frontend skills | `.github/skills/frontend/` |
| DevOps / deploy skill | `.github/skills/devops/deploy/` |
| Backend architecture context | `.github/context/backend.md` |
| Frontend architecture context | `.github/context/frontend.md` |
| Master entity registry | `master-registry.md` |
| Modules registry | `modules-registry.json` |
| AI commands | `.claude/commands/` |
| Governance automation tools | `governance-tools/` |
| Module execution plans | `modules/` |

---

## Task → Skill Routing

Read the matching skill BEFORE generating or modifying any code.
Skill files are at `.github/skills/<category>/<skill-name>/SKILL.md`.

### Backend (code lives in `backend` repo)

| Task | Skill |
|------|-------|
| **Always first — contract validation** | `enforce-backend-contract` |
| Create / modify Entity | `create-entity` |
| Create / modify Repository | `create-repository` |
| Create / modify DTOs | `create-dto` |
| Create / modify Mapper | `create-mapper` |
| Create / modify Service | `create-service` |
| Create / modify Controller | `create-controller` |
| Review / validate backend code | `enforce-backend-contract` |
| Add / review caching | `enforce-caching-rules` |
| Add / review error handling | `enforce-error-handling` |
| Validate a complete feature | `validate-backend-feature` |

### Frontend (code lives in `frontend` repo)

> ERP rules always take precedence over Angular/skills guidance.

| Task | Skill |
|------|-------|
| Create / modify models / DTOs / FormMapper | `create-models` |
| Create / modify API service | `create-api-service` |
| Create / modify Facade | `create-facade` |
| Create / modify Routing | `create-routing` |
| Create / modify Components | `create-components` |
| Review frontend architecture | `enforce-frontend-architecture` |
| Review UI/UX & data display | `enforce-ui-ux` |
| Review design system & CSS | `enforce-design-system` |
| Review code reusability | `enforce-reusability` |
| Review permissions | `enforce-permissions` |
| Review state management | `enforce-state-management` |
| Validate a complete feature | `validate-frontend-feature` |

### DevOps (infrastructure lives in `deploy` repo)

| Task | Skill |
|------|-------|
| Dockerfiles / docker-compose / nginx / deployment | `deploy` |

---

## Execution Order

**Backend (strict):**
`enforce-backend-contract` → `create-entity` → `create-repository` → `create-dto` → `create-mapper` → `create-service` → `create-controller` → `validate-backend-feature`

> `create-entity` emits two artifacts in this one step when applicable: the JPA entity, and its
> Domain companion object (business rules) per `.github/context/domain-layer.md`. This does not
> add a step to the sequence above.

**Frontend (strict):**
`create-models` → `create-api-service` → `create-facade` → `create-routing` → `create-components` → `validate-frontend-feature`

---

## Governance Rules

- NEVER generate backend code without first reading the corresponding skill
- NEVER generate frontend code without first reading the corresponding skill
- NEVER duplicate governance content in backend, frontend, or deploy repositories
- When a task spans multiple layers, read ALL relevant skills
- After completing a feature, run the validation skill to verify compliance
- Reference existing implementations in the codebase as canonical examples
- `master-registry.md` is the single source of truth for all entities and rules
- Business-rule conditions (anything answering "is this operation allowed?") must be
  implemented on a dedicated Domain object created via `create()`/`from()` factory methods —
  never inlined in Service, Repository, Controller, Mapper, or the Entity. See
  `.github/context/domain-layer.md`. This is a Governance requirement, not a prescription of
  which Backend Skill produces the Domain object — that remains an implementation detail of
  the Backend Skills.

---

## Context Reference (read on demand)

- Backend architecture overview: `.github/context/backend.md`
- Domain Layer Guideline (Business Rule ownership): `.github/context/domain-layer.md`
- API Contract Guideline (response envelope, exception→HTTP mapping, error-code format): `.github/context/api-contract.md`
- Frontend architecture overview + navigation i18n keys: `.github/context/frontend.md`
- All detailed rules live in `.github/skills/`
