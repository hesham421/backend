---
name: create-entity
description: "Generates a JPA entity class following the canonical pattern, plus its Domain companion object (business rules) when the entity has Business Rules requiring Business Decision ownership. Phase 1, Step 1 — MUST be completed before any other backend artifact. Enforces AuditableEntity, @SuperBuilder, BooleanNumberConverter, @Formula counts, DB naming conventions, and Domain object construction via factory methods (create()/from())."
---

# Skill: create-entity

## Name
`create-entity`

## Description
Generates a JPA entity class for the ERP system following the canonical MasterLookup pattern. This is **Phase 1, Step 1** of the execution template and MUST be completed before any other backend artifact.

## When to Use
- When implementing a new feature/domain entity in any backend module
- When the execution template Phase 1, Step 1.1 is being started
- BEFORE creating repository, DTO, mapper, service, or controller

## When NOT to Use
- When the entity already exists — use the entity file directly
- When only modifying DTOs, mapper, service, or controller (no entity change needed)
- When adding a field to an existing entity (edit the entity file, do not re-run this skill from scratch)
- For frontend code, deploy config, or governance documents

## Variables (Must Be Defined First)

| Variable | Example | Description |
|----------|---------|-------------|
| `MODULE_NAME` | `masterdata` | Maven module suffix |
| `MODULE_PREFIX` | `MD` | DB table prefix |
| `ENTITY_NAME` | `Activity` | PascalCase Java class name |
| `ENTITY_TABLE` | `MD_ACTIVITY` | UPPER_SNAKE DB table name |
| `ENTITY_SEQ` | `MD_ACTIVITY_SEQ` | Sequence name |
| `PARENT_ENTITY` | *(optional)* | Parent entity if child |
| `PARENT_FK_COL` | *(optional)* | e.g., `ACTIVITY_ID_FK` |

## Responsibilities

- Generate a JPA entity class extending `AuditableEntity`
- Define DB schema: table name, columns, constraints, indexes, sequences
- Implement boolean fields with `BooleanNumberConverter`
- Add FK relationships (`@ManyToOne` LAZY) if child entity
- Add child collections (`@OneToMany`) and `@Formula` counts if parent entity
- Implement `@PrePersist`/`@PreUpdate` lifecycle hooks for key normalization
- Provide `activate()`/`deactivate()` helper methods — pure field mutation only, no guard logic
- Per `.github/context/domain-layer.md`: if the entity has Business Rules requiring Business
  Decision ownership (RULE-IDs from the Execution Plan that answer "is this allowed"), also
  generate the entity's Domain companion object — see "DOMAIN COMPANION OBJECT" below. If the
  entity has no such rules (e.g. a pure reference/lookup table), no Domain companion is required.

## Constraints

- MUST NOT generate repository, DTO, mapper, service, or controller code
- MUST NOT modify other existing entity files
- MUST NOT assume missing variable values — require them before generation
- MUST NOT apply uppercase normalization outside `@PrePersist`/`@PreUpdate`
- MUST NOT use `@Builder` — always `@SuperBuilder` due to `AuditableEntity` inheritance

## Output

- `erp-<MODULE>/src/main/java/com/example/<module>/entity/Md<Entity>.java`
- `erp-<MODULE>/src/main/java/com/example/<module>/domain/<Entity>Domain.java` — only when the
  entity has Business Rules requiring Business Decision ownership (see "DOMAIN COMPANION
  OBJECT" below)

---

## Steps

### 1. Create Entity File
- **Location:** `erp-<MODULE_NAME>/src/main/java/com/example/<module>/entity/Md<ENTITY_NAME>.java`

### 2. Class-Level Setup
```java
@Entity
@Table(name = "<ENTITY_TABLE>",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_<ENTITY_TABLE>_<FIELD>", columnNames = {"<FIELD>"})
    },
    indexes = {
        @Index(name = "IDX_<ENTITY_TABLE>_<COLUMN>", columnList = "<COLUMN>")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Md<ENTITY_NAME> extends AuditableEntity {
```

### 3. Primary Key
```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "<entity>_seq")
@SequenceGenerator(name = "<entity>_seq", sequenceName = "<ENTITY_SEQ>", allocationSize = 1)
@Column(name = "<ENTITY_PK_COLUMN>") // derive from db-script — e.g. LEGAL_ENTITY_PK, BRANCH_PK
private Long id;
```

### 4. Business Fields
```java
@NotBlank(message = "{validation.required}")
@Size(max = 50, message = "{validation.size}")
@Column(name = "<COLUMN_NAME>", length = 50, nullable = false)
private String fieldName;
```

### 5. Boolean Fields
```java
@Column(name = "IS_ACTIVE", nullable = false)
@Builder.Default
@Convert(converter = BooleanNumberConverter.class)
private Boolean isActive = Boolean.TRUE;
```

### 6. FK Relationships (if child entity)
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "<PARENT>_ID_FK", nullable = false,
    foreignKey = @ForeignKey(name = "FK_<ENTITY_TABLE>_<PARENT>"))
private Md<PARENT_ENTITY> parentEntity;
```

### 7. Parent → Child Collection (if parent entity)
```java
@OneToMany(mappedBy = "<childField>", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
private List<Md<CHILD_ENTITY>> children = new ArrayList<>();

@Formula("(SELECT COUNT(*) FROM <CHILD_TABLE> c WHERE c.<PARENT>_ID_FK = <ENTITY_PK_COLUMN>)")
private Integer childCount;
```

### 8. Lifecycle Hooks
```java
@PrePersist
protected void onCreate() {
    if (isActive == null) {
        isActive = Boolean.TRUE;
    }
    if (naturalKey != null) {
        naturalKey = naturalKey.toUpperCase();
    }
}

@PreUpdate
protected void onUpdate() {
    if (naturalKey != null) {
        naturalKey = naturalKey.toUpperCase();
    }
}
```

### 9. Helper Methods
```java
public void activate() {
    this.isActive = Boolean.TRUE;
}

public void deactivate() {
    this.isActive = Boolean.FALSE;
}
```

---

## SHARED LAYER MANDATE (`erp-common-utils`)

Before creating a new entity, verify the following shared resources from `erp-common-utils` are consumed — do NOT reinvent:

| # | Requirement | Shared Class | Package |
|---|-------------|-------------|--------|
| SH.1 | MUST extend `AuditableEntity` for audit fields. Exception: short-lived security/session artifacts (e.g., RefreshToken) with their own lifecycle fields (issuedAt, expiresAt, revoked) are NOT required to extend AuditableEntity. Declare in Phase CORE: "[EntityName]: Session artifact — does not extend AuditableEntity." | `AuditableEntity` | `com.example.erp.common.domain` |
| SH.2 | MUST use `BooleanNumberConverter` for all boolean columns (SMALLINT) | `BooleanNumberConverter` | `com.example.erp.common.converter` |
| SH.3 | Audit fields auto-populated by `AuditEntityListener` — do NOT set manually | `AuditEntityListener` | `com.example.erp.common.audit` |
| SH.4 | Use `@SuperBuilder` due to `AuditableEntity` inheritance — NEVER `@Builder` | — | Lombok |

**Rules:**
- NEVER create a custom audit base class — use `AuditableEntity`
- NEVER add a TENANT_ID column — the system is single-tenant
- NEVER create a custom boolean converter — use `BooleanNumberConverter` (or `BooleanCharYNConverter` for CHAR(1) columns)
- NEVER set `createdAt/createdBy/updatedAt/updatedBy` manually — `AuditEntityListener` handles it

> **Cross-reference:** After creating the entity, run [`enforce-backend-contract`](../enforce-backend-contract/SKILL.md) to verify compliance.

---

## DOMAIN COMPANION OBJECT (Business Rules)

> Full guideline: [`domain-layer.md`](../../../context/domain-layer.md). This section applies
> the guideline specifically to entity generation — it does not restate it in full.

The entity file above is persistence-only. Any Business Rule traceable to a Plan RULE-ID that
answers **"is this operation allowed?"** — deactivation guards, code-immutability enforcement,
uniqueness preconditions the entity alone can decide, state-transition validation, cycle
prevention — does not belong on the entity. It belongs on a separate, dedicated Domain object.

### Package — check for a naming collision before generating

Default output package: `com.example.<module>.domain.<Entity>Domain`. Before generating into
that package, check the module's Phase CORE for an existing, conflicting use of `<module>.domain`
(for example, the ORG module's `CORE.md` already assigns `org.domain` to its JPA entities, not to
business-rule classes — that module is not to be touched by this default). If the module's Phase
CORE already assigns `<module>.domain` to a different concern, stop and confirm the intended
package with the user before generating, rather than silently placing Domain classes and Entities
in a package whose name contradicts what one of them actually is.

### Rules (STRICT — mirrors `enforce-backend-contract` LAYER 0)

| Rule ID | Rule | MUST |
|---------|------|------|
| A.0.1 | A dedicated `<Entity>Domain` class exists for every entity whose Plan RULE-IDs answer "is this operation allowed?" | YES |
| A.0.2 | `<Entity>Domain` carries no Spring or JPA annotations (no `@Component`, `@Service`, `@Entity`, `@Table`, `@Transactional`) | YES |
| A.0.3 | `<Entity>Domain` does not access a Repository or the database under any circumstance | YES |
| A.0.4 | `<Entity>Domain` throws `LocalizedException` for all business rule violations | YES |
| A.0.5 | `<Entity>Domain` is constructed only via static factories `create(...)` / `from(...)` — no public constructor used from outside the class | YES |
| A.0.6 | `<Entity>Domain` does not import or call another module's service — cross-module data is resolved by the Service and passed in | YES |
| A.0.7 | At most one Domain object per entity; a Domain Service is introduced only per the Domain Service Policy in `domain-layer.md`, never one per entity | YES |

### Responsibilities

- One `<Entity>Domain` class per entity that has such rules — plain Java, no annotations
- Static factory `create(...)` — runs construction-time Business Validations, returns a valid
  instance or throws `LocalizedException`
- Static factory `from(<Entity> entity)` — reconstructs a Domain view over an already-persisted
  entity, for evaluating a rule before a mutation (e.g. before deactivation)
- Guard/decision methods that take primitives or the `<Entity>` itself as arguments (counts,
  flags, sibling data already fetched by the Service) and throw `LocalizedException` on a rule
  violation
- Division of labor on state transitions: the Domain object **decides** ("may this entity be
  deactivated given these counts"); the Entity's existing `activate()`/`deactivate()` methods
  still **execute** the field mutation; the Service calls the Domain object first, then the
  Entity

### Constraints

- MUST NOT carry `@Entity`, `@Table`, `@Component`, `@Service`, `@Transactional`, or any other
  Spring/JPA annotation
- MUST NOT access a Repository or the database
- MUST NOT perform DTO mapping, call a REST endpoint, or call another module's service
- MUST NOT expose a public constructor usable from outside the class — `create(...)`/`from(...)`
  are the only entry points
- MUST NOT be generated as one Domain Service per entity — per the Domain Service Policy in
  `domain-layer.md`, default to no Domain Service at all; a Domain Service is introduced only
  when a rule spans multiple entities or repositories, and is a separate, explicitly-justified
  artifact, not a rename of this class

### Violations (MUST NOT)

- ❌ `<Entity>Domain` annotated with `@Component`/`@Service`/`@Entity`
- ❌ `<Entity>Domain` field or constructor referencing a Repository
- ❌ Public no-arg or all-args constructor usable outside the class instead of `create()`/`from()`
- ❌ A Business Rule left inline in the Service instead of expressed here (see `create-service`)
- ❌ Skipping this section for an entity that has RULE-ID guards in the Execution Plan

### Example

> Status choice for guard methods below follows [`api-contract.md`](../../../context/api-contract.md)
> §2: "cannot deactivate/delete because active/child records exist" is `Status.CONFLICT` (409) —
> `Status.BUSINESS_RULE_VIOLATION` (422) is reserved for invariant violations that are not "a
> specific referencing record exists" (invalid state transitions, cycle detection).

```java
public final class OrgLegalEntityDomain {

    private final String legalEntityCode;
    private final boolean active;

    private OrgLegalEntityDomain(String legalEntityCode, boolean active) {
        this.legalEntityCode = legalEntityCode;
        this.active = active;
    }

    public static OrgLegalEntityDomain create(String generatedCode) {
        if (generatedCode == null || generatedCode.isBlank()) {
            throw new LocalizedException(Status.VALIDATION_ERROR, OrgErrorCodes.CODE_REQUIRED);
        }
        return new OrgLegalEntityDomain(generatedCode, true);
    }

    public static OrgLegalEntityDomain from(OrgLegalEntity entity) {
        return new OrgLegalEntityDomain(entity.getLegalEntityCode(), Boolean.TRUE.equals(entity.getIsActiveFl()));
    }

    // RULE-ORG-001 / RULE-ORG-002 — decision only, Service performs entity.deactivate() after this returns
    public void assertCanDeactivate(long activeBranchCount, long activeProfitCenterCount) {
        if (activeBranchCount > 0) {
            throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.LE_HAS_ACTIVE_BRANCHES);
        }
        if (activeProfitCenterCount > 0) {
            throw new LocalizedException(Status.CONFLICT, OrgErrorCodes.LE_HAS_ACTIVE_PROFIT_CENTERS);
        }
    }
}
```

---

## Rules (STRICT — from implementation-contract.md)

| Rule ID | Rule | MUST |
|---------|------|------|
| A.1.1 | Extends `AuditableEntity` | YES — except short-lived security/session artifacts (e.g., RefreshToken) with own lifecycle (issuedAt/expiresAt/revoked). Verify exemption is intentional before flagging. |
| A.1.2 | PK `@Column` name derived from db-script — entity-specific (e.g. `LEGAL_ENTITY_PK`, `BRANCH_PK`) — NEVER generic `"ID"` or hardcoded `"ID_PK"` | YES |
| A.1.3 | PK uses `GenerationType.SEQUENCE` with explicit `@SequenceGenerator` | YES |
| A.1.4 | `allocationSize = 1` on `@SequenceGenerator` | YES |
| A.1.5 | FK columns end with `_ID_FK` | YES |
| A.1.6 | Booleans stored via `BooleanNumberConverter` | YES |
| A.1.7 | Boolean default: `@Builder.Default Boolean isActive = Boolean.TRUE` — `@Builder.Default` is compatible with `@SuperBuilder` | YES |
| A.1.8 | Every `@ManyToOne` uses `fetch = FetchType.LAZY` | YES |
| A.1.9 | `@OneToMany` uses `cascade = ALL, orphanRemoval = false, fetch = LAZY` | YES |
| A.1.10 | Uses `@SuperBuilder` (NOT `@Builder`) due to `AuditableEntity` | YES |
| A.1.11 | Table name: UPPER_SNAKE_CASE with module prefix | YES |
| A.1.12 | `@UniqueConstraint` and `@Index` declared in `@Table` | YES |
| A.1.13 | Unique constraints: `UK_<TABLE>_<DESC>` | YES |
| A.1.14 | Indexes: `IDX_<TABLE>_<COLUMN>` | YES |
| A.1.15 | FK constraints: `FK_<TABLE>_<REF>` via `@ForeignKey(name)` | YES |
| A.1.16 | Use `@Formula` for computed counts, NOT collection `.size()` | YES |
| A.1.17 | `@PrePersist` is the SOLE canonical location for uppercase normalization | YES |
| A.1.18 | Entity has `activate()` and `deactivate()` helpers | YES |
| A.1.19 | No helper methods that iterate/filter lazy `@OneToMany` collections — use repository count queries | YES |

---

## Violations (MUST NOT)

- ❌ `@Builder` instead of `@SuperBuilder`
- ❌ `GenerationType.IDENTITY` or `GenerationType.AUTO`
- ❌ `allocationSize` != 1
- ❌ Generic `@Column(name = "ID")` or hardcoded `@Column(name = "ID_PK")` — must use the entity-specific PK column name from db-script
- ❌ FK columns without `_ID_FK` suffix
- ❌ `boolean` primitive for boolean fields — must be `Boolean` wrapper
- ❌ `fetch = FetchType.EAGER` on relationships
- ❌ `orphanRemoval = true` without explicit governance approval
- ❌ Uppercase normalization in mapper or service
- ❌ `entity.setIsActive(true)` in service — must use `activate()` / `deactivate()`
- ❌ Defining `createdAt`/`createdBy` directly (they come from `AuditableEntity`)
- ❌ Using `entity.getChildren().size()` instead of `@Formula`
- ❌ Entity helper methods that iterate/filter lazy collections (e.g., `hasActiveDetails()` using `stream().anyMatch()`)
- ❌ Lowercase or camelCase table names

---

## Example (Real ERP — MasterLookup)

```java
@Entity
@Table(name = "MD_MASTER_LOOKUP",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_MD_MASTER_LOOKUP_KEY", columnNames = {"LOOKUP_KEY"})
    },
    indexes = {
        @Index(name = "IDX_MD_MASTER_LOOKUP_ACTIVE", columnList = "IS_ACTIVE")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class MdMasterLookup extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "master_lookup_seq")
    @SequenceGenerator(name = "master_lookup_seq", sequenceName = "MD_MASTER_LOOKUP_SEQ", allocationSize = 1)
    @Column(name = "MASTER_LOOKUP_PK") // entity-specific — from db-script
    private Long id;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "LOOKUP_KEY", length = 50, nullable = false)
    private String lookupKey;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "DESCRIPTION_EN", length = 200, nullable = false)
    private String descriptionEn;

    @Size(max = 200, message = "{validation.size}")
    @Column(name = "DESCRIPTION_AR", length = 200)
    private String descriptionAr;

    @Column(name = "IS_ACTIVE", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActive = Boolean.TRUE;

    @OneToMany(mappedBy = "masterLookup", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    private List<MdLookupDetail> lookupDetails = new ArrayList<>();

    @Formula("(SELECT COUNT(*) FROM MD_LOOKUP_DETAIL d WHERE d.MASTER_LOOKUP_ID_FK = MASTER_LOOKUP_PK)")
    private Integer detailCount;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) isActive = Boolean.TRUE;
        if (lookupKey != null) lookupKey = lookupKey.toUpperCase();
    }

    @PreUpdate
    protected void onUpdate() {
        if (lookupKey != null) lookupKey = lookupKey.toUpperCase();
    }

    public void activate() { this.isActive = Boolean.TRUE; }
    public void deactivate() { this.isActive = Boolean.FALSE; }
}
```
