---
name: validate-backend-feature
description: "MASTER VALIDATION — runs ALL enforcement checks across a completed backend feature. Verifies execution order, file inventory, 85 layer-by-layer contract rules (including the Domain layer), cross-cutting validations (error handling, caching, security, immutability), and unit tests."
---

# Skill: validate-backend-feature

## Name
`validate-backend-feature`

## Description
**MASTER VALIDATION SKILL.** The final gatekeeper that runs ALL enforcement checks against a completed backend feature. This skill analyzes generated code across ALL layers, detects violations, reports missing layers, and ensures full implementation-contract compliance. It orchestrates all enforcement skills into a single comprehensive validation pass.

## When to Use
- After a developer or AI agent claims a backend feature is "complete"
- Before merging any backend feature branch
- As the final step before moving to frontend implementation
- During code review of any backend module
- When verifying execution-template phase ordering

## When NOT to Use
- During individual layer creation — use the specific `create-*` skill for that layer
- For partial features where not all layers exist yet — report missing layers rather than running full validation
- For frontend code — use `validate-frontend-feature` instead
- For deploy/infrastructure — use the `deploy` skill instead

## Responsibilities

- Verify ALL phases were executed in correct order (entity → repository → DTO → mapper → service → controller → tests)
- Run file inventory check to confirm all required files exist
- Execute all layer-by-layer contract checks (85 rules, including the Domain layer)
- Validate cross-cutting concerns: error handling, caching, security, immutability
- Verify unit test coverage for all service method scenarios

## Constraints

- MUST NOT generate or modify application code — this skill only validates
- MUST NOT accept partial features — all required phases must be present
- MUST NOT skip any validation stage — all stages are mandatory
- MUST NOT fix violations automatically — report them with specific skill references

## Output

- Comprehensive validation report with:
  - Execution order verification (pass/fail)
  - File inventory (present/missing per artifact)
  - Layer-by-layer contract compliance (77 checks)
  - Cross-cutting validation results
  - Unit test coverage assessment
  - Final verdict: APPROVED or REJECTED with reasons

---

## VALIDATION PIPELINE

### STAGE 0: Execution Order Verification

Verify that ALL phases were executed in the correct order:

```
[ ] Step 1.1 Entity       — File exists, extends AuditableEntity
[ ] Step 1.2 Repository   — File exists, extends JpaRepository + JpaSpecificationExecutor
[ ] Step 1.3 DTOs         — All 5-6 DTO files exist (Create, Update, Response, Search, Usage, Option)
[ ] Step 1.4 Mapper       — File exists, @Component annotated
[ ] Step 1.4.5 Domain     — Unconditional (see `.github/context/domain-layer.md`).
                              A dedicated <Entity>Domain class exists for every entity
                              whose Execution Plan RULE-IDs require Business Decision
                              ownership; it is a plain class (no Spring/JPA annotations),
                              constructed only via create()/from(); the Service delegates
                              business rule checks to it — never inline.
[ ] Step 1.5 Error Codes  — Constants registered in <Module>ErrorCodes
[ ] Step 1.6 Permissions  — 4 permissions in SecurityPermissions.java
[ ] Step 1.7 Service      — File exists, @Service annotated
[ ] Step 1.8 Controller   — File exists, @RestController annotated
[ ] Step 1.9 Unit Tests   — File exists, @ExtendWith(MockitoExtension.class)
```

> **If ANY step is missing → REJECT immediately. No partial features.**

---

### STAGE 1: File Inventory Check

For a feature named `<Entity>` in module `<module>`, verify ALL files exist:

```
erp-<module>/src/main/java/com/example/<module>/
├── entity/Md<Entity>.java                          [ ]
├── repository/<Entity>Repository.java               [ ]
├── dto/<Entity>CreateRequest.java                   [ ]
├── dto/<Entity>UpdateRequest.java                   [ ]
├── dto/<Entity>Response.java                        [ ]
├── dto/<Entity>SearchRequest.java                   [ ]
├── dto/<Entity>UsageResponse.java                   [ ]
├── dto/<Entity>OptionResponse.java (if dropdown)    [ ]
├── mapper/<Entity>Mapper.java                       [ ]
├── domain/<Entity>Domain.java                       [ ]  ← required whenever the
│                                                            entity has Business Rules
│                                                            needing Business Decision
│                                                            ownership (see domain-layer.md)
├── exception/<Module>ErrorCodes.java (updated)      [ ]
├── service/<Entity>Service.java                     [ ]
└── controller/<Entity>Controller.java               [ ]

erp-<module>/src/test/java/com/example/<module>/
├── domain/<Entity>DomainTest.java (if <Entity>Domain exists) [ ]
└── service/<Entity>ServiceTest.java                 [ ]

erp-security/src/main/java/.../
└── constants/SecurityPermissions.java (updated)     [ ]

erp-main/src/main/resources/i18n/
├── messages.properties (updated)                    [ ]
└── messages_ar.properties (updated)                 [ ]
```

---

### STAGE 2: Layer-by-Layer Contract Validation

Run each enforcement skill's full checklist:

#### 2.0 Domain Validation (7 checks from `enforce-backend-contract`)
- A.0.1 through A.0.7 — see [`domain-layer.md`](../../../context/domain-layer.md). Unconditional
  for every entity with Business Rules requiring Business Decision ownership.

#### 2.1 Entity Validation (19 checks from `enforce-backend-contract`)
- A.1.1 through A.1.19

> Note: A.1.1 (AuditableEntity) has a canonical exception for
> short-lived security/session artifacts (e.g., RefreshToken)
> with their own lifecycle fields (issuedAt/expiresAt/revoked).
> Verify exemption is intentionally declared in module Phase CORE
> before flagging as a violation.

#### 2.2 Repository Validation (9 checks)
- A.2.1 through A.2.9

#### 2.3 DTO Validation (13 checks)
- A.3.1 through A.3.13

#### 2.4 Mapper Validation (7 checks)
- A.4.1 through A.4.7

#### 2.5 Service Validation (18 checks)
- A.5.1 through A.5.18

#### 2.6 Controller Validation (12 checks)
- A.6.1 through A.6.12

---

### STAGE 3: Cross-Cutting Validations

#### 3.1 Error Handling (from `enforce-error-handling`; Status/HTTP mapping canonical in [`api-contract.md`](../../../context/api-contract.md) §2)
```
[ ] NO NotFoundException usage anywhere in module
[ ] ALL not-found → LocalizedException(Status.NOT_FOUND, ...)
[ ] ALL duplicates → LocalizedException(Status.ALREADY_EXISTS, ...)
[ ] ALL FK violations → LocalizedException(Status.CONFLICT, ...) (except delete — DIVE handled by GlobalExceptionHandler)
[ ] Delete does NOT try-catch DataIntegrityViolationException
[ ] Error codes registered in <Module>ErrorCodes.java
[ ] Error messages in messages.properties (EN)
[ ] Error messages in messages_ar.properties (AR)
```

#### 3.2 Caching (from `enforce-caching-rules`)
```
[ ] Entity is on approved cache list OR has NO caching annotations
[ ] If cached: @CacheEvict on ALL write methods
[ ] If cached: @Cacheable only on approved read methods
[ ] If cached: annotation order correct (Cache → Transaction → Security)
[ ] If NOT cached: ZERO caching annotations anywhere
```

#### 3.3 Security
```
[ ] 4 permissions defined: VIEW, CREATE, UPDATE, DELETE
[ ] SecurityPermissions constants use PERM_<ENTITY>_<ACTION> format
[ ] @PreAuthorize on EVERY service public method
[ ] Permission constants referenced (not hardcoded strings)
```

#### 3.4 Immutability
```
[ ] Natural keys identified and documented
[ ] UpdateRequest excludes natural keys and FK references
[ ] Mapper's updateEntityFromRequest() skips immutable fields
[ ] Service does NOT update immutable fields
```

#### 3.5 Response Envelope (canonical definition in [`api-contract.md`](../../../context/api-contract.md) §1–2)
```
[ ] Service methods return ServiceResult<T> (except delete)
[ ] create() uses Status.CREATED
[ ] update()/activate()/deactivate() use Status.UPDATED
[ ] getById()/search()/getUsage() use default Status.SUCCESS
[ ] delete() returns void (no ServiceResult)
[ ] Controller uses operationCode.craftResponse() for non-delete
[ ] Controller uses @ResponseStatus(NO_CONTENT) for delete
[ ] Controller does NOT use @ResponseStatus(CREATED)
```

#### 3.6 Domain Delegation (unconditional — see `domain-layer.md`)

```
[ ] 3.6.1 — Business rule guards (deactivation checks, FK
             constraints, state transitions) are NOT inlined in
             service method bodies — delegated to the entity's
             <Entity>Domain object
[ ] 3.6.2 — Service body is orchestration-only: load → delegate
             → persist → return (no business if blocks remaining)
[ ] 3.6.3 — <Entity>Domain throws LocalizedException for rule
             violations (not the service)
[ ] 3.6.4 — <Entity>Domain does NOT import or call another
             module's service — cross-module (XM) data is resolved
             by the Service and passed in as a plain argument
```

---

### STAGE 4: Unit Test Validation

```
[ ] <Entity>DomainTest exists (only when a <Entity>Domain class is required) —
     plain unit test, NO mocks needed since Domain has no dependencies;
     asserts each guard method throws LocalizedException on violation and
     succeeds otherwise; asserts create()/from() reject invalid input
[ ] Test file exists at correct location
[ ] @ExtendWith(MockitoExtension.class) class annotation
[ ] @Mock on repository and mapper
[ ] @InjectMocks on service
[ ] Test: create_Success → asserts ServiceResult.isSuccess(), .getData(), .getStatusCode() == CREATED
[ ] Test: create_ShouldThrow_WhenDuplicate → asserts LocalizedException.class
[ ] Test: update_Success → asserts .getStatusCode() == UPDATED
[ ] Test: update_ShouldThrow_WhenNotFound → asserts LocalizedException.class
[ ] Test: getById_Success → asserts .isSuccess()
[ ] Test: getById_ShouldThrow_WhenNotFound → asserts LocalizedException.class
[ ] Test: activate_Success
[ ] Test: deactivate_Success
[ ] Test: deactivate_ShouldFail_WhenConstraints (if parent)
[ ] Test: delete_Success
[ ] Test: delete_ShouldFail_WhenHasChildren (if parent)
[ ] Test: delete_ShouldFail_WhenReferenced (if applicable)
[ ] ALL tests pass: mvn test -pl erp-<module>
```

---

### STAGE 5: Compilation & Test Execution

```
[ ] mvn clean compile -pl erp-<module> -am → SUCCESS (no errors)
[ ] mvn test -pl erp-<module> → ALL TESTS PASS
[ ] No compilation warnings related to the feature
```

---

## FINAL VERDICT

### Scoring

| Stage | Weight | Max Score |
|-------|--------|-----------|
| Stage 0: Execution Order | 10% | 10 points |
| Stage 1: File Inventory | 10% | 15 points |
| Stage 2: Layer Contracts | 40% | 85 points |
| Stage 3: Cross-Cutting | 25% | 22 points |
| Stage 4: Unit Tests | 10% | 15 points |
| Stage 5: Build/Test | 5% | 2 points |
| **TOTAL** | **100%** | **149 points** |

> Stage 0/1/2 totals include the Domain layer (1 execution-order step, 1 file, 7 contract
> checks) added by the Domain Layer Guideline — see `domain-layer.md`.

### Verdict Thresholds

| Score | Verdict | Action |
|-------|---------|--------|
| 149/149 (100%) | ✅ **APPROVED** | Proceed to frontend |
| 142-148 (95%+) | ⚠️ **APPROVED WITH NOTES** | Minor issues, document and proceed |
| 119-141 (80%+) | 🔶 **CONDITIONAL** | Fix issues before proceeding |
| < 119 (< 80%) | ❌ **REJECTED** | Major rework required |

### Automatic Rejection (regardless of score)

The feature is **IMMEDIATELY REJECTED** if any of these are found:

- `NotFoundException` used anywhere
- Service method without `@PreAuthorize`
- Service returning raw entity outside module
- `GenerationType.IDENTITY` or `AUTO`
- Repository injected in another module
- Business logic in controller
- Missing `AuditableEntity` extension
- `@Builder` instead of `@SuperBuilder` on entity
- Dead-code repository methods with no caller in any service
- Entity helper methods iterating/filtering lazy `@OneToMany` collections
- Child mapper `toEntity()` without parent entity FK parameter
- Business-rule condition inlined in a Service method instead of delegated to `<Entity>Domain`
- `<Entity>Domain` annotated with `@Component`, `@Service`, or `@Entity`, or accessing a Repository

---

## Validation Report Template

```
# Backend Feature Validation Report

## Feature: [Feature Name]
## Module: erp-[module]
## Entity: Md[Entity]
## Date: [Date]
## Validator: AI Governance System

---

## STAGE 0: Execution Order
| Step | Artifact | Status |
|------|----------|--------|
| 1.1 | Entity | ✅/❌ |
| 1.2 | Repository | ✅/❌ |
| 1.3 | DTOs | ✅/❌ |
| 1.4 | Mapper | ✅/❌ |
| 1.4.5 | Domain | ✅/❌ |
| 1.5 | Error Codes | ✅/❌ |
| 1.6 | Permissions | ✅/❌ |
| 1.7 | Service | ✅/❌ |
| 1.8 | Controller | ✅/❌ |
| 1.9 | Unit Tests | ✅/❌ |

## STAGE 1: File Inventory
[x/15] files present

## STAGE 2: Layer Contracts
| Layer | Checks | Passed | Failed |
|-------|--------|--------|--------|
| Domain | 7 | ? | ? |
| Entity | 19 | ? | ? |
| Repository | 9 | ? | ? |
| DTO | 13 | ? | ? |
| Mapper | 7 | ? | ? |
| Service | 18 | ? | ? |
| Controller | 12 | ? | ? |

## STAGE 3: Cross-Cutting
| Area | Checks | Passed | Failed |
|------|--------|--------|--------|
| Error Handling | 7 | ? | ? |
| Caching | 5 | ? | ? |
| Security | 4 | ? | ? |
| Immutability | 4 | ? | ? |
| Response Envelope | 8 | ? | ? |
| Common-Utils Reuse | 8 | ? | ? |

## STAGE 4: Unit Tests
[x/15] test cases verified

## STAGE 5: Build
- Compile: ✅/❌
- Tests: ✅/❌ ([x] passed, [y] failed)

---

## VIOLATIONS FOUND
1. [Rule ID] — [Description] — [Location] — [Severity]

## SCORE: [X] / 149 ([Y]%)

## VERDICT: APPROVED / APPROVED WITH NOTES / CONDITIONAL / REJECTED

## REQUIRED FIXES (if not approved):
1. [Fix description]
```

---

### erp-common-utils Compliance (CU.1–CU.8)

Run `.github/skills/backend/enforce-backend-contract/SKILL.md` for full erp-common-utils compliance validation (CU.1–CU.8). A feature that fails any CU check is NOT compliant regardless of its layer scores.

---

## RELATED SKILLS

| Skill | Purpose |
|-------|---------|
| `enforce-backend-contract` | Detailed 77-check architectural validation across all layers |
| `enforce-error-handling` | 27-check error handling compliance with `LocalizedException` and `Status` |
| `enforce-caching-rules` | 35-check caching eligibility and annotation rules |
