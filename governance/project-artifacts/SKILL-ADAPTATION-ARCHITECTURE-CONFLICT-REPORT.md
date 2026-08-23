# Skill Adaptation Request — Architecture Conflict Report

Produced **2026-08-22** in response to a request to adapt the ten backend
`SKILL.md` files (real module/package/class names substituted in) and inject
three new architecture constraints — a module facade interface, a
RabbitMQ-backed 3-layer event isolation pipeline, and a `MessagingConfig`
constants class — into every skill touching cross-module calls, events, or
messaging.

No skill files were modified for this request. Step 1 discovery (below)
surfaced a direct conflict between what the three requested constraints
assume and what the codebase actually does, so this report was produced
first, per the human operator's instruction, instead of drafting adapted
skill content premised on infrastructure that doesn't exist.

---

## 1. What Exists

### Modules (Maven, assembled into one `erp-main` deployable)

| Module | Root package | Notes |
|---|---|---|
| `erp-common-utils` | `com.example.erp.common` (+ legacy `com.erp.common.search`) | Shared base classes, error codes, web/audit helpers |
| `erp-security` | `com.example.security` | Auth, users, roles, permissions |
| `erp-org` | `com.example.erp.org` | Org structure (branch, dept, cost center, etc.) |
| `erp-masterdata` | `com.example.masterdata` | Lookup tables |
| `erp-notification` | `com.example.erp.notification` | Notification dispatch |
| `erp-file` | `com.example.erp.file` | File upload/download |
| `erp-finance-gl` | — | POM shell only, no Java yet |
| `erp-main` | `com.erp.main` | Spring Boot entry point |

### Cross-module calls — no facade interfaces

There is no `XModule` interface anywhere in the codebase (`grep -r "interface.*Module\b"` — zero hits). The real, deliberate pattern is a **same-JVM REST self-call**: each caller module defines a `RestTemplate`-based `*Client` class (`OrgBranchClient`, `MasterDataLookupClient`, `SecUserProfileClient`, `SecurityUserClient`), wired through a per-module `InternalApiClientConfig` bean. This is not incidental — `OrgBranchClient`'s javadoc explains it directly:

> "erp-security has no Maven dependency on erp-org, so this is a same-JVM HTTP self-call to `GET /api/v1/org/branches/{id}`... rather than a shared JPA object graph."

Both `InternalApiClientConfig` copies (security, notification) cross-reference each other in javadoc and explicitly note they're named distinctly to avoid Spring bean collisions in the shared `erp-main` context — this is a maintained, intentional convention, not leftover scaffolding.

### Events — no 3-layer isolation, no broker

All event usage found (`AccountActivationRequestedEvent`, `PasswordResetRequestedEvent` in `erp-security`; `NotificationRequestedEvent`, `NotificationLogPersistedEvent` in `erp-notification`) goes through plain Spring `ApplicationEventPublisher.publishEvent(...)`. There is:

- No Spring Modulith dependency (`grep -r "modulith"` across all `pom.xml` — zero hits) and no `@ApplicationModuleListener` anywhere.
- No `EventPublisher`/`MessagePublisher` port abstraction — `ApplicationEventPublisher` is injected and called directly from services (`AuthService`, `NotificationEventProcessor`).
- No `RabbitTemplate`, no `amqp` dependency, no message broker of any kind (`grep -r "RabbitTemplate\|RabbitMQ\|amqp"` — zero hits).
- No `MessagingConfig` class, and therefore no queue/exchange/routing-key constants anywhere to centralize.

### Skill files found (all backend-relevant)

`governance/.github/skills/backend/`: `create-controller`, `create-dto`, `create-entity`, `create-mapper`, `create-repository`, `create-service`, `enforce-backend-contract`, `enforce-caching-rules`, `enforce-error-handling`, `validate-backend-feature`.

---

## 2. Conflict With Requested Constraints

| Requested constraint | Assumes | Actually exists | Verdict |
|---|---|---|---|
| **1 — Module Facade**: cross-module calls only through an `XModule` interface | An in-process facade interface per module, injected as a Spring bean | REST self-calls over HTTP through `*Client` + `RestTemplate`, explicitly chosen *because* modules have no Maven dependency on each other | **Contradicts existing, documented pattern.** Injecting this would tell future code generation to build interfaces that don't exist and that the current design deliberately avoided. |
| **2 — Event Isolation (3 layers)**: internal event → `@ApplicationModuleListener` → `MessagePublisher` port → `RabbitTemplate` | Spring Modulith + a message broker already in the stack | Plain `ApplicationEventPublisher.publishEvent()`, no Modulith, no broker, no port abstraction | **Contradicts existing pattern and requires new infrastructure** (Modulith dependency + RabbitMQ) not present in `pom.xml` or `docker-compose.yml`. |
| **3 — Messaging constants in `erp-common`**: queue/exchange names as constants in `MessagingConfig` | A `MessagingConfig` class and queue-based messaging already exist | No such class, no messaging layer at all | **Nothing to formalize** — the class this constraint would centralize constants in doesn't exist because there's no messaging to have constants for. |

All three constraints describe a microservices/message-driven target architecture. This codebase is a single-JVM modular monolith (one Spring Boot app, one port, cross-module calls via loopback HTTP) with in-process events. The constraints aren't a refinement of what's there — they assume a different deployment model.

---

## 3. Recommendation

**Do not inject the three constraints as-is.** Writing them into `enforce-backend-contract`, `enforce-caching-rules`, `enforce-error-handling`, and `validate-backend-feature` would make those skills — which other AI sessions treat as binding rules — instruct future code generation to build a facade-interface layer and RabbitMQ messaging pipeline that contradict the pattern already implemented, documented in javadoc, and (per `OrgBranchClient`'s comments) already reasoned through for this specific deployment topology. Per this repo's own `governance/CLAUDE.md` and root `CLAUDE.md`, that's exactly the kind of invented/contradictory governance content and undeclared cross-cutting architecture change that requires an explicit, separate human decision before it goes anywhere — not something to fold into a skill-adaptation pass.

**Two paths forward, both legitimate — pick one:**

1. **Formalize what's actually there.** Adapt the ten skills with real names (as originally requested) and, wherever they touch cross-module calls or events, codify the *existing* patterns as the enforceable rule instead of the three requested constraints:
   - Cross-module calls: must go through a `*Client` class + module-scoped `InternalApiClientConfig`/`RestTemplate` bean, hitting the target module's own REST API — never a direct import of another module's internal service/repository/entity class.
   - Events: must go through `ApplicationEventPublisher`, with dedicated event classes per trigger (mirroring `AccountActivationRequestedEvent`-style naming) and listeners kept in the owning module.
   - Skip constraint 3 entirely — there's no messaging layer to have constants for.

   This is the lower-risk option: it makes the skills accurately enforce what the codebase already does, with zero architecture change.

2. **Treat this as a genuine target-architecture decision.** If the intent is to actually move this platform toward message-driven, facade-isolated modules (e.g., in preparation for splitting modules into separate services later), that's a legitimate direction — but it's a real infrastructure change (new Modulith + RabbitMQ dependencies, new config, a migration plan for existing `*Client`/`ApplicationEventPublisher` call sites) that should be scoped and confirmed as its own initiative before any skill file states it as a binding rule. Writing the rule into governance ahead of the infrastructure would make every subsequent AI-generated feature either violate the new skill or invent partial, inconsistent messaging scaffolding to satisfy it.

My recommendation is **option 1** unless there's a specific, already-decided reason (e.g., a scaling requirement, an eventual service-split plan) driving option 2 — in which case that reason is worth stating explicitly so the skill adaptation and the infrastructure work can be sequenced correctly instead of the skill getting ahead of the code.

Awaiting direction on which path to take before drafting the adapted `SKILL.md` content.
