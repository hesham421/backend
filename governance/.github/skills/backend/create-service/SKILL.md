---
name: create-service
description: "Generates the @Service for application orchestration: CRUD, search, activate, deactivate, getUsage. Owns transactions, security, caching, logging. Delegates business rules to domain/ classes. Phase 1, Step 1.7. Enforces @PreAuthorize, @Transactional, ServiceResult<T>, LocalizedException, SpecBuilder + PageableBuilder, ALLOWED_SORT_FIELDS."
---

# Skill: create-service

## Name
`create-service`

## Description
Generates the service class for an ERP feature. This is **Phase 1, Step 1.7** of the execution template. The service is the ORCHESTRATION layer — it loads data, delegates business rules to domain/ classes, manages transactions, enforces security, and persists results. It does NOT own business rules directly.

## When to Use
- After `create-entity`, `create-repository`, `create-dto`, `create-mapper`, and error codes are complete (Steps 1.1–1.5)
- When Phase 1, Step 1.7 of the execution template is being executed
- BEFORE creating controller

## When NOT to Use
- Before entity, repository, DTOs, mapper, error codes, and `SecurityPermissions` constants are all defined
- When the service already exists and only one method needs modification (edit directly)
- For any layer other than the service (orchestration) layer
- For frontend state management — use `create-facade` instead

## Prerequisites
- Entity with `activate()`/`deactivate()` helpers
- Repository with `existsBy*`, count queries
- All DTOs (Create, Update, Response, Search, Usage)
- Mapper with `toEntity()`, `updateEntityFromRequest()`, `toResponse()`, `toUsageResponse()`
- Error codes registered in `<Module>ErrorCodes`
- Permissions registered in `SecurityPermissions`

## Responsibilities

- Generate service class with all CRUD operations: `create`, `getById`, `update`, `delete`, `activate`, `deactivate`, `search`, `getUsage`
- Apply `@PreAuthorize` on every public method using `SecurityPermissions` constants
- Apply `@Transactional` on write methods and `@Transactional(readOnly = true)` on reads
- Apply `@CacheEvict` on writes and `@Cacheable` on reads if entity is cache-eligible
- Return `ServiceResult<T>` from all methods except `delete()` (returns void)
- Throw `LocalizedException` with error codes from `<Module>ErrorCodes` for all error scenarios
- Validate sort fields against `ALLOWED_SORT_FIELDS` whitelist
- Use `SpecBuilder` + `PageableBuilder` for search operations

## Constraints

- MUST NOT generate entity, repository, DTO, mapper, or controller code
- MUST NOT modify other service files unless explicitly requested
- MUST NOT assume missing error codes or permissions — they must be defined before service creation
- MUST NOT hardcode error messages — use `<Module>ErrorCodes` constants only
- MUST NOT catch `DataIntegrityViolationException` — let `GlobalExceptionHandler` handle it
- MUST NOT set `isActive` directly — use entity’s `activate()`/`deactivate()` helpers

## Output

- Single file: `erp-<MODULE>/src/main/java/com/example/<module>/service/<Entity>Service.java`

---

## Steps

## Domain Delegation Rule

Full guideline: [`domain-layer.md`](../../../context/domain-layer.md).

Before implementing any conditional in the service, apply the Decision Test: does this code
answer "is this operation allowed?" If yes, it MUST be expressed as a call to the entity's
Domain companion object (`<Entity>Domain`, produced by `create-entity` — see its "DOMAIN
COMPANION OBJECT" section), never as an inline `if` in the service method body. This applies
unconditionally, regardless of what any module's Phase CORE does or doesn't say.

- Fetch whatever data the rule needs via the Repository (counts, flags, sibling records).
- Build or load the Domain object via its factory: `<Entity>Domain.create(...)` for new
  instances, `<Entity>Domain.from(entity)` for evaluating a rule against an existing one.
- Call its decision method. It throws `LocalizedException` on violation.
- On success, call the Entity's own mutation method (e.g. `entity.deactivate()`) and persist.

Cross-module (XM) data needed for a decision is resolved by the Service — call the other
module's service, obtain the result, and pass it into the Domain object's method as a plain
argument. The Domain object itself never imports or calls another module's service.

The service MUST NOT own business rule conditions (if blocks that
enforce business invariants) directly in its method bodies.

## Cross-Module Interface Contracts (XM)
If this service exposes an interface consumed by other modules:
- The interface MUST be declared in service/ alongside the
  implementation — NOT in a standalone api/ package.
- Consuming modules import from:
  com.example.<module>.service.<InterfaceName>
- MUST NOT create a dedicated api/ package for this purpose.

### 1. Create Service File
- **Location:** `erp-<MODULE_NAME>/src/main/java/com/example/<module>/service/<ENTITY_NAME>Service.java`

### 2. Class Declaration
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class <ENTITY_NAME>Service {

    private final <ENTITY_NAME>Repository repository;
    private final <ENTITY_NAME>Mapper mapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "fieldName", "isActive", "createdAt"
    );
```

### 3. create() Method
```java
@CacheEvict(cacheNames = "<cacheName>", allEntries = true) // ONLY if entity is cache-eligible
@Transactional
@PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).<ENTITY>_CREATE)")
public ServiceResult<<ENTITY>Response> create(<ENTITY>CreateRequest request) {
    log.info("Creating <Entity> with key: {}", request.getKey());

    // 1. Fetch the data the rule needs — repository access stays in the Service
    boolean keyTaken = repository.existsByKey(request.getKey().toUpperCase());

    // 2. Delegate the decision to the Domain object — throws LocalizedException
    //    (e.g. <Module>ErrorCodes.<ENTITY>_KEY_DUPLICATE) on violation. See
    //    create-entity's "DOMAIN COMPANION OBJECT" section and domain-layer.md.
    <ENTITY>Domain.create(request.getKey(), keyTaken);

    // 3. Map to entity (FK relationships set AFTER mapping)
    Md<ENTITY> entity = mapper.toEntity(request);
    // entity.setParent(parentEntity); // If child entity

    // 4. Save
    Md<ENTITY> saved = repository.save(entity);
    log.info("Created <Entity> with ID: {}", saved.getId());

    // 5. Return ServiceResult with Status.CREATED
    return ServiceResult.success(mapper.toResponse(saved), Status.CREATED);
}
```

### 4. update() Method
```java
@CacheEvict(cacheNames = "<cacheName>", allEntries = true) // ONLY if cache-eligible
@Transactional
@PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).<ENTITY>_UPDATE)")
public ServiceResult<<ENTITY>Response> update(Long id, <ENTITY>UpdateRequest request) {
    log.info("Updating <Entity> ID: {}", id);

    // 1. Find or throw
    Md<ENTITY> entity = repository.findById(id)
        .orElseThrow(() -> new LocalizedException(
            Status.NOT_FOUND, <Module>ErrorCodes.<ENTITY>_NOT_FOUND, id));

    // 2. Fetch data any update-time rule needs — e.g. a uniqueness re-check
    //    excluding this id (existsBy<Field>AndIdNot — ONLY if the field is
    //    mutable on update, see create-repository A.2.5)
    boolean keyTakenByAnother = repository.existsByKeyAndIdNot(request.getKey().toUpperCase(), id);

    // 3. Delegate the decision to the Domain object
    <ENTITY>Domain.from(entity).assertCanRename(keyTakenByAnother);

    // 4. Update via mapper (immutable fields NOT changed)
    mapper.updateEntityFromRequest(entity, request);

    // 5. Save
    Md<ENTITY> saved = repository.save(entity);
    log.info("Updated <Entity> ID: {}", saved.getId());

    // 6. Return ServiceResult with Status.UPDATED
    return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
}
```

### 5. getById() Method
```java
@Transactional(readOnly = true)
@PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).<ENTITY>_VIEW)")
public ServiceResult<<ENTITY>Response> getById(Long id) {
    log.debug("Fetching <Entity> ID: {}", id);

    Md<ENTITY> entity = repository.findById(id)
        .orElseThrow(() -> new LocalizedException(
            Status.NOT_FOUND, <Module>ErrorCodes.<ENTITY>_NOT_FOUND, id));

    return ServiceResult.success(mapper.toResponse(entity));
}
```

### 6. search() Method
```java
@Transactional(readOnly = true)
@PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).<ENTITY>_VIEW)")
public ServiceResult<Page<<ENTITY>Response>> search(<ENTITY>SearchRequest searchRequest) {
    log.debug("Searching <Entity>s");

    SearchRequest commonRequest = searchRequest.toCommonSearchRequest();

    SetAllowedFields allowedFields = new SetAllowedFields(ALLOWED_SORT_FIELDS);
    Specification<Md<ENTITY>> spec = SpecBuilder.build(commonRequest, allowedFields, DefaultFieldValueConverter.INSTANCE);
    Pageable pageable = PageableBuilder.from(commonRequest, ALLOWED_SORT_FIELDS);

    Page<Md<ENTITY>> page = repository.findAll(spec, pageable);
    Page<<ENTITY>Response> responsePage = page.map(mapper::toResponse);

    return ServiceResult.success(responsePage);
}
```

### 7. activate() Method
```java
@CacheEvict(cacheNames = "<cacheName>", allEntries = true) // ONLY if cache-eligible
@Transactional
@PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).<ENTITY>_UPDATE)")
public ServiceResult<<ENTITY>Response> activate(Long id) {
    log.info("Activating <Entity> ID: {}", id);

    Md<ENTITY> entity = repository.findById(id)
        .orElseThrow(() -> new LocalizedException(
            Status.NOT_FOUND, <Module>ErrorCodes.<ENTITY>_NOT_FOUND, id));

    entity.activate();

    Md<ENTITY> saved = repository.save(entity);
    log.info("Activated <Entity> ID: {}", saved.getId());

    return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
}
```

### 8. deactivate() Method
```java
@CacheEvict(cacheNames = "<cacheName>", allEntries = true) // ONLY if cache-eligible
@Transactional
@PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).<ENTITY>_UPDATE)")
public ServiceResult<<ENTITY>Response> deactivate(Long id) {
    log.info("Deactivating <Entity> ID: {}", id);

    Md<ENTITY> entity = repository.findById(id)
        .orElseThrow(() -> new LocalizedException(
            Status.NOT_FOUND, <Module>ErrorCodes.<ENTITY>_NOT_FOUND, id));

    // Fetch data the rule needs, then delegate the decision to the Domain object
    long activeChildren = repository.countActiveChildren(id); // example — actual counts per entity's RULE-IDs
    <ENTITY>Domain.from(entity).assertCanDeactivate(activeChildren);

    entity.deactivate();

    Md<ENTITY> saved = repository.save(entity);
    log.info("Deactivated <Entity> ID: {}", saved.getId());

    return ServiceResult.success(mapper.toResponse(saved), Status.UPDATED);
}
```

### 9. delete() Method
```java
@CacheEvict(cacheNames = "<cacheName>", allEntries = true) // ONLY if cache-eligible
@Transactional
@PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).<ENTITY>_DELETE)")
public void delete(Long id) {
    log.info("Deleting <Entity> ID: {}", id);

    Md<ENTITY> entity = repository.findById(id)
        .orElseThrow(() -> new LocalizedException(
            Status.NOT_FOUND, <Module>ErrorCodes.<ENTITY>_NOT_FOUND, id));

    // Check references (if parent)
    long childCount = repository.countChildren(id);
    if (childCount > 0) {
        throw new LocalizedException(Status.CONFLICT,
            <Module>ErrorCodes.<ENTITY>_CHILDREN_EXIST, id);
    }

    // No try-catch — DataIntegrityViolationException propagates to GlobalExceptionHandler
    repository.delete(entity);
    log.info("Deleted <Entity> ID: {}", id);
}
```

### 10. getUsage() Method
```java
@Transactional(readOnly = true)
@PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).<ENTITY>_VIEW)")
public ServiceResult<<ENTITY>UsageResponse> getUsage(Long id) {
    log.debug("Fetching usage for <Entity> ID: {}", id);

    Md<ENTITY> entity = repository.findById(id)
        .orElseThrow(() -> new LocalizedException(
            Status.NOT_FOUND, <Module>ErrorCodes.<ENTITY>_NOT_FOUND, id));

    long childCount = repository.countChildren(id);

    return ServiceResult.success(mapper.toUsageResponse(entity, childCount));
}
```

---

## SHARED LAYER MANDATE (`erp-common-utils`)

> Full contract definition (Status→HTTP mapping, exception taxonomy): [`api-contract.md`](../../../context/api-contract.md).
> SH.2/SH.3 below are this skill's consumption checklist, not a second copy of the contract.

Before creating a service, verify the following shared resources from `erp-common-utils` are consumed — do NOT reinvent:

| # | Requirement | Shared Class | Package |
|---|-------------|-------------|--------|
| SH.1 | Return type: `ServiceResult<T>` for all methods except `delete()` | `ServiceResult` | `com.example.erp.common.domain.status` |
| SH.2 | Status codes: `Status.CREATED`, `Status.UPDATED`, `Status.SUCCESS` | `Status` | `com.example.erp.common.domain.status` |
| SH.3 | Errors: `LocalizedException(Status, ErrorCode, ...args)` for ALL errors | `LocalizedException` | `com.example.erp.common.exception` |
| SH.4 | Search: `SpecBuilder.build()` for dynamic JPA specifications | `SpecBuilder` | `com.erp.common.search` |
| SH.5 | Pagination: `PageableBuilder.from()` with sort field whitelist | `PageableBuilder` | `com.erp.common.search` |
| SH.6 | Sort validation: `SetAllowedFields` for `ALLOWED_SORT_FIELDS` whitelist | `SetAllowedFields` | `com.erp.common.search` |
| SH.7 | Security context: `SecurityContextHelper` for current user (`getUsernameOrSystem()`, `isAuthenticated()`) | `SecurityContextHelper` | `com.example.erp.common.util` |
| SH.8 | Validation utilities: `ValidationUtils` for common validations | `ValidationUtils` | `com.example.erp.common.util` |

**Rules:**
- NEVER throw raw `RuntimeException` — use `LocalizedException`
- NEVER use deprecated `NotFoundException` — use `LocalizedException(Status.NOT_FOUND, ...)`
- NEVER build `Specification` or `Pageable` manually — use `SpecBuilder` and `PageableBuilder`
- NEVER create a custom result wrapper — use `ServiceResult<T>`
- NEVER hardcode error messages — use `<Module>ErrorCodes` constants with `LocalizedException`
- NEVER catch `DataIntegrityViolationException` — let `GlobalExceptionHandler` from common-utils handle it

> **Cross-reference:** After creating the service, run [`enforce-error-handling`](../enforce-error-handling/SKILL.md) and [`enforce-backend-contract`](../enforce-backend-contract/SKILL.md).

---

## Rules (STRICT — from implementation-contract.md)

| Rule ID | Rule | MUST |
|---------|------|------|
| A.5.2 | `@PreAuthorize` with `SecurityPermissions` constant on EVERY public method | YES |
| A.5.3 | `@Transactional` on every write method | YES |
| A.5.4 | `@Transactional(readOnly = true)` on every read method | YES |
| A.5.5 | `@CacheEvict(allEntries = true)` on writes for cached entities | YES |
| A.5.6 | `ALLOWED_SORT_FIELDS` as `private static final Set<String>` | YES |
| A.5.7 | Search uses `SpecBuilder.build()` + `PageableBuilder.from()` | YES |
| A.5.8 | Return `ServiceResult<T>` — never raw DTOs or entities. Exception: `delete()` stays `void` | YES |
| A.5.9 | `create()` → validate → map → save → `ServiceResult.success(dto, Status.CREATED)` | YES |
| A.5.10 | `update()` → find → throw `LocalizedException(NOT_FOUND)` → map → save → `ServiceResult.success(dto, Status.UPDATED)` | YES |
| A.5.11 | `delete()` → find → check refs → delete (no try-catch — DIVE handled by `GlobalExceptionHandler`) | YES |
| A.5.12 | `activate()` → find → `entity.activate()` → save · `deactivate()` → find → validate constraints (via domain) → `entity.deactivate()` → save | YES |
| A.5.13 | Error codes from `<Module>ErrorCodes` constants, NEVER inline strings | YES |
| A.5.14 | `log.info()` for writes, `log.debug()` for reads | YES |
| A.5.15 | ALL exceptions are `LocalizedException(Status, ErrorCode, ...args)` — `NotFoundException` is **NOT USED** | YES |
| A.5.16 | Child service search requires non-null parent ID | YES |
| A.5.17 | Child service search uses explicit `Specification` JOIN | YES |

### Annotation Order (CRITICAL)

**Cached read method:**
```
@Cacheable(cacheNames = "...", key = "#id")     // 1st
@Transactional(readOnly = true)                  // 2nd
@PreAuthorize("hasAuthority(...)")               // 3rd
```

**Cached write method:**
```
@CacheEvict(cacheNames = "...", allEntries = true) // 1st
@Transactional                                      // 2nd
@PreAuthorize("hasAuthority(...)")                  // 3rd
```

---

## Violations (MUST NOT)

- ❌ Throwing `NotFoundException` — use `LocalizedException(Status.NOT_FOUND, ...)`
- ❌ Throwing raw `RuntimeException` or any non-`LocalizedException`
- ❌ Returning raw DTO or entity from service (must wrap in `ServiceResult<T>`)
- ❌ Missing `@PreAuthorize` on any public method
- ❌ Hardcoded permission strings — must use `SecurityPermissions` constants
- ❌ Missing `@Transactional` on write methods
- ❌ Missing `readOnly = true` on read methods
- ❌ Accepting arbitrary sort fields — must validate against `ALLOWED_SORT_FIELDS`
- ❌ Manual specification/page construction — use `SpecBuilder` + `PageableBuilder`
- ❌ Inline error message strings — must use `<Module>ErrorCodes` constants
- ❌ `entity.setIsActive(true/false)` — must use `entity.activate()` / `entity.deactivate()`
- ❌ Delete without reference check before attempting
- ❌ Try-catch for `DataIntegrityViolationException` in delete — it propagates to `GlobalExceptionHandler`
- ❌ Using `log.info()` for read operations (use `log.debug()`)
- ❌ Missing cache eviction on writes for cached entities
- ❌ A business-rule condition (`if` enforcing an invariant) implemented inline in a service
  method instead of delegated to the entity's `<Entity>Domain` object — see
  [`domain-layer.md`](../../../context/domain-layer.md)

---

## Example (Real ERP — MasterLookupService.create)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class MasterLookupService {

    private final MasterLookupRepository masterLookupRepository;
    private final MasterLookupMapper masterLookupMapper;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "lookupKey", "descriptionEn", "descriptionAr", "isActive", "createdAt", "detailCount"
    );

    @CacheEvict(cacheNames = "lookupValues", allEntries = true)
    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).MASTER_LOOKUP_CREATE)")
    public ServiceResult<MasterLookupResponse> create(MasterLookupCreateRequest request) {
        log.info("Creating MasterLookup with key: {}", request.getLookupKey());

        if (masterLookupRepository.existsByLookupKey(request.getLookupKey().toUpperCase())) {
            throw new LocalizedException(Status.ALREADY_EXISTS,
                MasterDataErrorCodes.MASTER_LOOKUP_KEY_DUPLICATE, request.getLookupKey());
        }

        MdMasterLookup entity = masterLookupMapper.toEntity(request);
        MdMasterLookup saved = masterLookupRepository.save(entity);

        log.info("Created MasterLookup ID: {}", saved.getId());
        return ServiceResult.success(masterLookupMapper.toResponse(saved), Status.CREATED);
    }
}
```
