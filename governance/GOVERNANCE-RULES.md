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

> Precedence when general React guidance (react.dev, community convention,
> an external library's docs) appears to conflict with a project rule: see
> `erp-priority-override`.

| Task | Skill |
|------|-------|
| Create / modify TS DTO types, Zod form schema, FormMapper | `create-models` |
| Create / modify the HTTP client / a feature's typed API module | `create-api-client` |
| Create / modify TanStack Query hooks (reads + mutations) | `create-queries` |
| Create / modify entry forms (React Hook Form + Zod) | `create-forms` |
| Create / modify feature UI (columns, list page, entry page) | `create-components` |
| Create / modify routing (route tree, guards, navigation model) | `create-routing` |
| Create / modify cross-cutting client state (Language/Auth Context) | `create-app-state` |
| Create / modify the auth/session layer (tokens, refresh, login/logout) | `create-auth-session` |
| Create / modify confirm handlers for destructive/state-changing actions | `create-confirm-actions` |
| Create / modify the error architecture (taxonomy, boundaries, mapping) | `create-error-handling` |
| Create / modify the test suite (Vitest, MSW, RTL, Playwright) | `create-tests` |
| Review frontend architecture | `enforce-frontend-architecture` |
| Review UI/UX, design tokens, i18n & accessibility | `enforce-ui-ux` |
| Review code reusability | `enforce-reusability` |
| Review permissions | `enforce-permissions` |
| Review state management | `enforce-state-management` |
| Review frontend security (tokens, XSS, CSRF, uploads, secrets) | `enforce-security` |
| Resolve conflicts with external React guidance | `erp-priority-override` |
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

**Frontend — foundation (build once, before any feature):**
`create-auth-session` → `create-error-handling` → `create-app-state`

**Frontend — per feature (strict):**
`create-models` → `create-api-client` → `create-queries` → `create-forms` → `create-components` → `create-routing` → `create-confirm-actions` → `create-tests` → `validate-frontend-feature`

> This order is read directly from each skill's own stated step number in
> its `SKILL.md` front matter (e.g. `create-models` = "Step 2.1",
> `create-queries` = "Step 2.3", `create-confirm-actions` = "Step 2.8") —
> not invented for this document. `create-routing` carries no step number
> of its own; it is placed after `create-components` because a route's
> lazy import needs the page component to already exist. The six
> `enforce-*` skills and `erp-priority-override` are on-demand review /
> precedence skills, not fixed points in the generation sequence.

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
- NEVER write banner/section-divider comments (`// ==== Section ====`, `// ─────...─────`,
  or any repeated-character line used to slice one file into visual sections). If a class has
  grown enough sections to need dividers, that is a signal to split the class, not to add ASCII
  art.
- NEVER write a Javadoc block longer than ~5 lines. No `@author` tags, no embedded usage
  examples, no restated "Architecture Rules" prose — that content belongs in
  `.github/context/` or a `project-artifacts/` doc, not repeated inside every class that
  touches the concept. A Javadoc comment states the one non-obvious thing a reader couldn't
  get from the method/class signature and name; if there isn't one, omit the Javadoc entirely.
- NEVER create or modify a JUnit test file (anything under `src/test/java/` or
  `packages/backend-test/`) unless the user explicitly asked for tests, or the dedicated
  `execute-backend-test` phase is what's currently running. Implementing or fixing a feature
  is not, on its own, a request for tests — `packages/backend-test/` is a separate, gated
  phase for a reason (see `generate-module-setup.md`'s Step 1).

---

## Context Reference (read on demand)

- Backend architecture overview: `.github/context/backend.md`
- Domain Layer Guideline (Business Rule ownership): `.github/context/domain-layer.md`
- API Contract Guideline (response envelope, exception→HTTP mapping, error-code format): `.github/context/api-contract.md`
- Frontend architecture overview + navigation i18n keys: `.github/context/frontend.md`
- All detailed rules live in `.github/skills/`
