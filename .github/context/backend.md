# Backend Architecture

## Layers

```
Controller → Service → Repository
```

| Layer | Responsibility |
|---|---|
| **Controller** | REST endpoint, `@Valid`, delegation only — ZERO business logic |
| **Service** | Orchestration, transactions, `ServiceResult<T>` |
| **Repository** | JPA data access — module-internal only, never shared |
| **Entity** | JPA model, extends `AuditableEntity`, internal only |
| **DTO** | Public API contract (`CreateRequest`, `UpdateRequest`, `Response`) |
| **Mapper** | Entity ↔ DTO, one `@Component` per entity |

## Implementation Order

```
Entity → Repository → DTOs → Mapper → Error Codes → Permissions → Service → Controller → Tests
```

## Key Contracts

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
