# Backend Architecture

## Layers

```
Controller → Service → Repository
                 ↕
              Domain
```

Domain is not a hop the request passes through — it's a collaborator the Service consults
(constructed/loaded via factory method, asked a decision) before persisting through the
Entity/Repository as before.

| Layer | Responsibility |
|---|---|
| **Controller** | REST endpoint, `@Valid`, delegation only — ZERO business logic |
| **Service** | Orchestration, transactions, `ServiceResult<T>` — delegates business-rule decisions to Domain, never owns them |
| **Domain** | Owns Business Rules, decisions, validations, state transitions — plain class, no Spring/JPA/repository/DTO, constructed only via `create()`/`from()`. See [`domain-layer.md`](domain-layer.md) |
| **Repository** | JPA data access — module-internal only, never shared |
| **Entity** | JPA model, extends `AuditableEntity`, internal only — persistence shape, not business decisions |
| **DTO** | Public API contract (`CreateRequest`, `UpdateRequest`, `Response`) |
| **Mapper** | Entity ↔ DTO, one `@Component` per entity |

### Business Rule Ownership

Whenever code answers "is this operation allowed?", it belongs in the Domain — never inlined in
the Service, Repository, Controller, Mapper, or the Entity. **The Domain is the decision maker.
The Service is the orchestrator.** Full guideline: [`domain-layer.md`](domain-layer.md).

## Implementation Order

```
Entity → Repository → DTOs → Mapper → Error Codes → Permissions → Service → Controller → Tests
```

## Key Contracts

> The full public REST contract (envelope shape, exception→HTTP mapping, error-code format) is
> canonically defined once in [`api-contract.md`](api-contract.md) — the rows below are a
> summary, not a second copy.

| Concern | Rule |
|---|---|
| Response envelope | Service returns `ServiceResult<T>`; controller calls `operationCode.craftResponse()` |
| Error handling | `LocalizedException(Status, ErrorCode)` — never raw exceptions or `NotFoundException` |
| Transactions | `@Transactional` on writes, `@Transactional(readOnly = true)` on reads |
| Security | `@PreAuthorize` on every service method, constants from `SecurityPermissions.java` |
| PK strategy | `GenerationType.SEQUENCE` only — never `IDENTITY` or `AUTO` |
| Booleans | `BooleanNumberConverter`, stored as `NUMBER(1)`, column named `IS_<FIELD>` |
| Existence checks | `existsBy<Field>()` — never `findBy().isPresent()` |

> For detailed rules and examples, read the relevant skill from `.github/skills/backend/`.
