---
name: enforce-backend-contract
description: "GOVERNANCE ENFORCER — validates generated backend code against ALL 85 contract rules across 7 layers (Domain, Entity, Repository, DTO, Mapper, Service, Controller). Rejects any violation. Use after ANY backend code generation or review."
---

# Skill: enforce-backend-contract

## Name
`enforce-backend-contract`

## Description
**GOVERNANCE ENFORCER.** Analyzes generated backend code and rejects ANY violation of the implementation contract. This skill is the architectural gatekeeper — it does NOT generate code, it VALIDATES and BLOCKS non-compliant code.

## When to Use
- After ANY backend code generation (entity, repository, DTO, mapper, service, controller)
- As a mandatory post-generation validation step
- When reviewing pull requests or code changes in backend modules
- When an AI agent or developer claims a feature is "complete"

## When NOT to Use
- During code generation (this skill validates only — it never generates code)
- For frontend code — use `enforce-frontend-architecture` instead
- For deploy/infrastructure code — use the `deploy` skill instead
- When validating only a single cross-cutting concern — use the specific enforce skill (`enforce-caching-rules`, `enforce-error-handling`) instead

## Responsibilities

- Validate generated backend code against ALL 85 contract rules across 7 layers (Domain, Entity, Repository, DTO, Mapper, Service, Controller)
- Mark each check as PASS or VIOLATION
- Reject any non-compliant code with specific violation references

## Constraints

- MUST NOT generate or modify application code — this skill only validates
- MUST NOT fix violations automatically — report them for the appropriate create-* skill to fix
- MUST NOT validate layers outside the backend module scope
- MUST NOT skip any check — ALL 85 rules must be evaluated

## Output

- Compliance report with PASS/VIOLATION per check across all 6 layers
- Specific violation descriptions with rule IDs for any failures

---

## Enforcement Checklist

### LAYER 0: Domain Layer

> Full guideline: [`domain-layer.md`](../../../context/domain-layer.md). This check is
> **unconditional** — it applies to every entity with Business Rules requiring Business
> Decision ownership, regardless of what any module's Phase CORE does or doesn't declare.

```
[ ] A.0.1 — A dedicated <Entity>Domain class exists for every entity whose Execution Plan
             RULE-IDs answer "is this operation allowed?" (deactivation guards, code-
             immutability, cycle prevention, state-transition checks, etc.)
[ ] A.0.2 — <Entity>Domain carries NO Spring or JPA annotations
             (no @Component, @Service, @Entity, @Table, @Transactional)
[ ] A.0.3 — <Entity>Domain does NOT access a Repository or the database, under any
             circumstance, including constructor injection — all data it needs is passed
             in as plain arguments by the Service
[ ] A.0.4 — <Entity>Domain throws LocalizedException for all
             business rule violations — never raw RuntimeException
[ ] A.0.5 — <Entity>Domain is constructed ONLY via static factory methods
             (create(...), from(...)) — no public constructor used from outside the class
[ ] A.0.6 — <Entity>Domain does NOT import or call another module's service — cross-module
             (XM) data is resolved by the Service and passed in as a plain argument
[ ] A.0.7 — At most one Domain object per entity; a Domain Service exists only when a rule
             genuinely spans multiple entities/repositories — flag "one Domain Service per
             entity" as an over-application per the Domain Service Policy
```

### LAYER 1: Entity Contract Enforcement

Run EVERY check. Mark ✅ PASS or ❌ VIOLATION.

```
[ ] A.1.1  — Entity extends AuditableEntity (NOT defining createdAt/createdBy directly).
             Exception: short-lived security/session artifacts
             (e.g., RefreshToken) with their own lifecycle
             (issuedAt/expiresAt/revoked) are exempt — verify
             exemption is intentional before flagging as violation.
[ ] A.1.2  — PK @Column name matches the entity's PK column defined in db-script (entity-specific,
             e.g. LEGAL_ENTITY_PK, BRANCH_PK) — NOT a generic "ID" or hardcoded "ID_PK"
[ ] A.1.3  — PK uses GenerationType.SEQUENCE with @SequenceGenerator
[ ] A.1.4  — allocationSize = 1 on @SequenceGenerator
[ ] A.1.5  — FK columns end with _ID_FK suffix
[ ] A.1.6  — Booleans use BooleanNumberConverter (SMALLINT) or BooleanCharYNConverter (CHAR(1)) — based on db-script column type
[ ] A.1.7  — Boolean default: @Builder.Default Boolean isActive = Boolean.TRUE
[ ] A.1.8  — Every @ManyToOne uses fetch = FetchType.LAZY
[ ] A.1.9  — @OneToMany uses cascade = ALL, orphanRemoval = false, fetch = LAZY
[ ] A.1.10 — Uses @SuperBuilder (NOT @Builder)
[ ] A.1.11 — Table name is UPPER_SNAKE_CASE with module prefix
[ ] A.1.12 — @UniqueConstraint and @Index declared in @Table
[ ] A.1.13 — Unique constraints named UK_<TABLE>_<DESC>
[ ] A.1.14 — Indexes named IDX_<TABLE>_<COLUMN>
[ ] A.1.15 — FK constraints named FK_<TABLE>_<REF>
[ ] A.1.16 — Uses @Formula for counts (NOT collection.size())
[ ] A.1.17 — @PrePersist is sole location for uppercase normalization
[ ] A.1.18 — Entity has activate() and deactivate() helpers
[ ] A.1.19 — No helper methods that iterate/filter lazy @OneToMany collections
```

### LAYER 2: Repository Contract Enforcement

```
[ ] A.2.1 — Extends JpaRepository AND JpaSpecificationExecutor
[ ] A.2.2 — Has @Repository annotation
[ ] A.2.3 — NOT injected outside its own module
[ ] A.2.4 — Existence checks use existsBy<Field>()
[ ] A.2.5 — Update uniqueness uses existsBy<Field>AndIdNot() ONLY if field is mutable
[ ] A.2.6 — Child queries use JOIN FETCH
[ ] A.2.7 — Count queries use JPQL @Query("SELECT COUNT()")
[ ] A.2.8 — Projections used for read-only multi-table queries
[ ] A.2.9 — No dead code — every repository method has at least one caller in the service
```

### LAYER 3: DTO Contract Enforcement

```
[ ] A.3.1  — All DTOs use @Data @Builder @NoArgsConstructor @AllArgsConstructor
[ ] A.3.2  — Class-level @Schema(description = "English - Arabic")
[ ] A.3.3  — Each field has @Schema(description, example)
[ ] A.3.4  — Validation messages use i18n keys: "{validation.required}"
[ ] A.3.5  — CreateRequest excludes id and audit fields
[ ] A.3.6  — UpdateRequest excludes immutable fields
[ ] A.3.7  — Response includes ALL fields + audit + computed counts
[ ] A.3.8  — Audit timestamps: @JsonFormat with "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
[ ] A.3.9  — SearchRequest extends BaseSearchContractRequest
[ ] A.3.10 — Child SearchRequest overrides toCommonSearchRequest()
[ ] A.3.11 — Child SearchRequest has parent ID extractor
[ ] A.3.12 — UsageResponse has canDelete/canDeactivate + reason
[ ] A.3.13 — OptionResponse is slim (no audit fields)
```

### LAYER 4: Mapper Contract Enforcement

```
[ ] A.4.1 — One @Component mapper per entity
[ ] A.4.2 — Child toEntity() accepts parent entity as FK parameter (compile-time safety)
[ ] A.4.3 — updateEntityFromRequest() returns void
[ ] A.4.4 — updateEntityFromRequest() skips immutable fields
[ ] A.4.5 — toResponse() uses Boolean.TRUE.equals() for booleans
[ ] A.4.6 — All methods handle null input
[ ] A.4.7 — toUsageResponse() computes eligibility from counts
```

### LAYER 5: Service Contract Enforcement

```
[ ] A.5.1  — @Service @RequiredArgsConstructor @Slf4j
[ ] A.5.2  — @PreAuthorize on EVERY public method
[ ] A.5.3  — @Transactional on every write
[ ] A.5.4  — @Transactional(readOnly = true) on every read
[ ] A.5.5  — @CacheEvict on writes (if cached entity)
[ ] A.5.6  — ALLOWED_SORT_FIELDS whitelist
[ ] A.5.7  — Search uses SpecBuilder + PageableBuilder
[ ] A.5.8  — Returns ServiceResult<T> (delete() is void)
[ ] A.5.9  — create() → validate → map → save → ServiceResult(CREATED)
[ ] A.5.10 — update() → find → LocalizedException(NOT_FOUND) → update → ServiceResult(UPDATED)
[ ] A.5.11 — delete() → find → check refs → delete (no try-catch — DIVE handled by GlobalExceptionHandler)
[ ] A.5.12 — activate() → find → entity.activate() → save
             deactivate() → find → validate constraints → entity.deactivate() → save
[ ] A.5.13 — Error codes from <Module>ErrorCodes constants
[ ] A.5.14 — log.info() for writes, log.debug() for reads
[ ] A.5.15 — ALL exceptions are LocalizedException — NotFoundException NOT USED
[ ] A.5.16 — Child search requires non-null parent ID
[ ] A.5.17 — Child search uses Specification JOIN
[ ] A.5.18 — Business rule checks (deactivation guards, FK constraints,
             state transitions, domain invariants) are delegated to the
             entity's <Entity>Domain object — NOT inlined directly
             in service method bodies, and NOT left on the Entity itself
             beyond a trivial field mutation. Service body is orchestration-only.
             See `domain-layer.md`. Automatic rejection trigger — see table above.
```

### LAYER 6: Controller Contract Enforcement

> Full contract definition (envelope shape, exception→HTTP mapping this layer checks against):
> [`api-contract.md`](../../../context/api-contract.md).

```
[ ] A.6.1  — @RestController @RequestMapping @RequiredArgsConstructor
[ ] A.6.2  — @Tag with Arabic + English description
[ ] A.6.3  — Injects ONLY service(s) + OperationCode
[ ] A.6.4  — Non-delete: operationCode.craftResponse()
[ ] A.6.5  — Delete: @ResponseStatus(NO_CONTENT) + void
[ ] A.6.6  — Search uses POST /search with @RequestBody — NOT GET with @ModelAttribute
[ ] A.6.7  — Activation: separate PUT /{id}/activate and PUT /{id}/deactivate endpoints
             — NOT a single toggle-active endpoint
[ ] A.6.8  — Usage: GET /{id}/usage
[ ] A.6.9  — Child endpoints under same controller
[ ] A.6.10 — @Operation on every method
[ ] A.6.11 — @Valid @RequestBody on all DTOs
[ ] A.6.12 — ZERO business logic
```

---

## Violation Response Template

When a violation is detected, respond with:

```
❌ VIOLATION DETECTED

Rule: [Rule ID] — [Rule description]
Location: [File:Line]
Found: [What was found]
Expected: [What should be there]
Severity: CRITICAL / HIGH / MEDIUM

Fix: [Exact code correction]
```

---

## Automatic Rejection Triggers

> The envelope/exception-mapping rows below check consumption of the contract canonically defined
> in [`api-contract.md`](../../../context/api-contract.md) — that document owns the mapping itself.

The following patterns trigger IMMEDIATE rejection — no exceptions:

| Pattern | Rejection Reason |
|---------|-----------------|
| `new NotFoundException(...)` | MUST use `LocalizedException(Status.NOT_FOUND, ...)` |
| `throw new RuntimeException(...)` | MUST use `LocalizedException` |
| Service method without `@PreAuthorize` | Security breach — ALL methods need authorization |
| Service returning raw DTO (not `ServiceResult`) | Must wrap in `ServiceResult<T>` |
| `GenerationType.IDENTITY` or `AUTO` | Must use `GenerationType.SEQUENCE` |
| `@Builder` annotation on entity class (instead of `@SuperBuilder`) | Breaks `AuditableEntity` inheritance — `@Builder.Default` on fields is allowed with `@SuperBuilder` |
| Repository injected outside its module | Cross-module violation |
| Controller with business logic | Thin controller violation |
| Controller injecting repository | Layer violation |
| `@ResponseStatus(CREATED)` on POST | Handled by ServiceResult mapping |
| Entity without `AuditableEntity` | Missing audit trail — EXCEPT short-lived security/session artifacts (e.g., RefreshToken) with their own lifecycle (issuedAt/expiresAt/revoked). Verify exemption is declared in Phase CORE before rejecting. |
| Boolean without `BooleanNumberConverter` | PostgreSQL SMALLINT convention breach |
| Mapper doing `.toUpperCase()` | Canonical violation — entity @PrePersist handles it |
| `entity.setIsActive(true)` in service | Must use `activate()`/`deactivate()` |
| Business-rule `if` (enforcing an invariant) inlined in a Service method | Must delegate to `<Entity>Domain` — see `domain-layer.md` (A.5.18) |
| `<Entity>Domain` annotated with `@Component`, `@Service`, or `@Entity` | Domain must be a plain class — see `domain-layer.md` (A.0.2) |
| `<Entity>Domain` with a Repository field or constructor parameter | Domain must never access persistence — see `domain-layer.md` (A.0.3) |

---

## Enforcement Report Format

```
## Backend Contract Enforcement Report

### Feature: [Feature Name]
### Module: [Module Name]
### Date: [Date]

| Layer | Checks | Passed | Failed | Status |
|-------|--------|--------|--------|--------|
| Domain | 7 | ? | ? | ✅/❌ |
| Entity | 19 | ? | ? | ✅/❌ |
| Repository | 9 | ? | ? | ✅/❌ |
| DTO | 13 | ? | ? | ✅/❌ |
| Mapper | 7 | ? | ? | ✅/❌ |
| Service | 18 | ? | ? | ✅/❌ |
| Controller | 12 | ? | ? | ✅/❌ |
| **TOTAL** | **85** | **?** | **?** | **?** |

### Violations Found:
1. [Violation details]
2. [Violation details]

### Verdict: APPROVED / REJECTED
```

---

## `erp-common-utils` CONSUMPTION CHECKS

> CU.3 and CU.6–CU.8 check consumption of the contract canonically defined in
> [`api-contract.md`](../../../context/api-contract.md) — that document is the source of truth
> for the envelope/mapping shape itself; this table only checks that a feature consumes it
> correctly.

When running this enforcement, also verify shared code from `erp-common-utils` is consumed:

| # | Check | Expected | Violation |
|---|-------|----------|----------|
| CU.1 | Entity extends `AuditableEntity` | `extends AuditableEntity` | Custom audit base class |
| CU.2 | Boolean columns use `BooleanNumberConverter` (SMALLINT) or `BooleanCharYNConverter` (CHAR(1)) | `@Convert(converter = BooleanNumberConverter.class)` | Custom boolean converter |
| CU.3 | Service returns `ServiceResult<T>` | All non-delete methods return `ServiceResult` | Custom result wrapper |
| CU.4 | Errors use `LocalizedException` | `throw new LocalizedException(...)` | Raw `RuntimeException` or `NotFoundException` |
| CU.5 | Search uses `SpecBuilder` + `PageableBuilder` | `SpecBuilder.build()` + `PageableBuilder.from()` | Manual `Specification` or `Pageable` construction |
| CU.6 | Controller uses `OperationCode.craftResponse()` | `operationCode.craftResponse(result)` | Custom response wrapping |
| CU.7 | No duplicate of `GlobalExceptionHandler` | Zero exception `@ControllerAdvice` in feature module | Per-module exception handler |
| CU.8 | No duplicate of `ApiResponse` | Zero custom response envelope in feature module | Per-module response envelope class |

---

## RELATED SKILLS

| Skill | Purpose |
|-------|---------|
| `enforce-error-handling` | Deep dive into error handling compliance: `LocalizedException`, `Status`, error codes |
| `enforce-caching-rules` | Validates caching eligibility and annotation rules |
| `validate-backend-feature` | Master validation across all layers with scoring |
