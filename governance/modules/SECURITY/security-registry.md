# registry-security.md
════════════════════════════════════════
Module       : SECURITY
Version      : 2.5.0
Last Updated : 2026-07-09
Status       : Core AS-IS (Complete) + DataScope/Self-Service Auth Extension — Phase SVC+API COMPLETE ⚠️ (permission gating pending, governance conflicts OPEN)
════════════════════════════════════════

⚠️ Session Note (this update): reconciled this document directly against
`backend/erp-security/src/main/java/**` and
`governance-repo/HANDOFF-PHASE-3-SVC-API.md`. The DataScope extension
(`SEC_USER_PROFILE`, `SEC_ROLE_BRANCH`) and the Self-Service Auth gap
package (Sign Up, Account Activation, Forgot Password, Reset Password)
that v2.4.1 listed as "⚠️ DESIGNED — NOT YET IMPLEMENTED" (old §8) are now
**implemented in code** (entities, repositories, services, controllers,
DTOs, error codes, i18n messages — verified by direct source read, not by
re-trusting the prior upload). What is still genuinely outstanding is
tracked in §9. The governance status is **not** fully green: master-registry
Conflict #20 remains OPEN (Section 13) and Security's extension
scope is tracked there as `PARTIALLY_READY ⚠️, BLOCKED pending BLK-SEC-002`
— this document describes what the code does, not an authorization for it.

---

## 1. ENTITIES & DB STRUCTURE

> **Naming-convention note**: PK column names (`USERS_PK`, `ROLES_PK`, `PERMISSIONS_PK`, `REFRESH_TOKENS_PK`, `SEC_PAGES_PK`) and FK constraint names (`FK_*`) are both real and grep-able in `erp-security/src/main/java` (`@Column(name = ...)` and `@JoinColumn(foreignKey = @ForeignKey(name = ...))` respectively) — matching how `erp-org`/`erp-masterdata` already name things, and this project's own `create-entity` skill (rules A.1.2, A.1.15). **PK constraint names**, however, cannot be set via any JPA/Hibernate annotation (verified by decompiling `hibernate-core-7.2.0.Final.jar`: neither `PhysicalNamingStrategy` nor `ImplicitNamingStrategy` has a PK-constraint hook) — those are enforced only by the `pg_constraint`-driven blocks in `001_security_schema_migration_and_seed.sql` / `002_datascope_selfservice_auth_schema.sql`, and only take effect once a DBA runs them (`ddl-auto=none`, no Flyway wired up for `erp-security`). `CK_*` (check constraint) names exist for the two new token tables' `USED_FL` columns (`CHK_PASSWORD_RESET_TOKEN_USED_FL`, `CHK_ACCOUNT_ACTIVATION_TOKEN_USED_FL`, both in `002_datascope_selfservice_auth_schema.sql`) — no other `@Check` usage exists in this project.

### 1.1 `USERS` → `UserAccount.java`

| Column      | Type                            | Nullable | Default | Constraints                    |
|-------------|---------------------------------|----------|---------|--------------------------------|
| USERS_PK    | BIGINT GENERATED ALWAYS AS IDENTITY | NOT NULL | — | Primary Key             |
| USERNAME    | VARCHAR(80)                     | NOT NULL | —       | UK: `UK_USERS_USERNAME`        |
| PASSWORD    | VARCHAR(200)                    | NOT NULL | —       | BCrypt hash                    |
| EMAIL       | VARCHAR(150)                    | NULL     | —       | UK: `UK_USERS_EMAIL` — added by `002_datascope_selfservice_auth_schema.sql` (BLOCK 5b), pending DBA execution (see naming-convention note above). Nullable because pre-existing rows have no value to backfill. Added directly to USERS (not SEC_USER_PROFILE) because SEC_USER_PROFILE.BRANCH_ID_FK is NOT NULL, which would block self-registration (Sign Up) before a branch is assigned. |
| ENABLED     | SMALLINT                        | NOT NULL | 1       | Boolean flag (0/1)             |
| CREATED_AT  | TIMESTAMP                       | NOT NULL | —       | Audit                          |
| CREATED_BY  | VARCHAR(100)                    | NOT NULL | —       | Audit                          |
| UPDATED_AT  | TIMESTAMP                       | NULL     | —       | Audit                          |
| UPDATED_BY  | VARCHAR(100)                    | NULL     | —       | Audit                          |

- **Primary Key**: `USERS_PK` (column and constraint both use this name; renamed from `ID`) — column rename applied via `001_security_schema_migration_and_seed.sql`, executed by DBA; live in the database
- **Indexes**: `IDX_USERS_ENABLED`, `IDX_USERS_USERNAME`
- **FK Fields**: none (join table `USER_ROLES` links to `ROLES`)
- **Flag Fields**: `ENABLED` (SMALLINT → converted to Boolean via `BooleanNumberConverter`)
- **System Fields**: `CREATED_AT`, `CREATED_BY`, `UPDATED_AT`, `UPDATED_BY`
- **Java Entity**: `UserAccount extends AuditableEntity`
- **Relationships**: `@ManyToMany` → `ROLES` via join table `USER_ROLES` (`USER_ROLES.USER_ID_FK` → `USERS.USERS_PK`, `USER_ROLES.ROLE_ID_FK` → `ROLES.ROLES_PK`); `@OneToOne` (inverse side) → `SEC_USER_PROFILE` (§1.7)

---

### 1.2 `ROLES` → `Role.java`

| Column      | Type                            | Nullable | Default | Constraints                   |
|-------------|---------------------------------|----------|---------|--------------------------------|
| ROLES_PK    | BIGINT GENERATED ALWAYS AS IDENTITY | NOT NULL | — | Primary Key             |
| NAME        | VARCHAR(60)                     | NOT NULL | —       | UK: `UK_ROLES_NAME`           |
| ROLE_CODE   | VARCHAR(60)                     | NOT NULL | —       | UK: `UK_ROLES_ROLE_CODE`      |
| DESCRIPTION | VARCHAR(500)                    | NULL     | —       | —                             |
| IS_ACTIVE   | SMALLINT                        | NOT NULL | 1       | Boolean flag (0/1)            |
| CREATED_AT  | TIMESTAMP                       | NOT NULL | —       | Audit                         |
| CREATED_BY  | VARCHAR(100)                    | NOT NULL | —       | Audit                         |
| UPDATED_AT  | TIMESTAMP                       | NULL     | —       | Audit                         |
| UPDATED_BY  | VARCHAR(100)                    | NULL     | —       | Audit                         |

- **Primary Key**: `ROLES_PK` (column and constraint both use this name; renamed from `ID`) — column rename applied via `001_security_schema_migration_and_seed.sql`, executed by DBA; live in the database
- **Indexes**: `IDX_ROLES_IS_ACTIVE`
- **Flag Fields**: `IS_ACTIVE` (SMALLINT → Boolean via `BooleanNumberConverter`)
- **System Fields**: `CREATED_AT`, `CREATED_BY`, `UPDATED_AT`, `UPDATED_BY`
- **Java Entity**: `Role extends AuditableEntity`
- **Java Field Mapping**:
  - `NAME` DB column → Java `roleName` field (legacy `getName()` deprecated alias)
  - `ROLE_CODE` DB column → Java `roleCode` field — persisted (previously `@Transient`); backfilled from `NAME`, which historically held the uppercased code
  - `DESCRIPTION` DB column → Java `description` field — persisted (previously `@Transient`)
- **Relationships**: `@ManyToMany` → `PERMISSIONS` via `ROLE_PERMISSIONS` (`ROLE_PERMISSIONS.ROLE_ID_FK` → `ROLES.ROLES_PK`, `ROLE_PERMISSIONS.PERM_ID_FK` → `PERMISSIONS.PERMISSIONS_PK`); referenced (scalar FK, no JPA association) by `SEC_ROLE_BRANCH.ROLE_ID_FK` (§1.8)

---

### 1.3 `SEC_PAGES` → `Page.java`

| Column        | Type           | Nullable | Default | Constraints                       |
|---------------|----------------|----------|---------|-----------------------------------|
| SEC_PAGES_PK  | BIGINT         | NOT NULL | SEQ     | Primary Key, uses sequence `SEC_PAGES_SEQ` (sequence name real: `@SequenceGenerator(sequenceName = "SEC_PAGES_SEQ")`) |
| PAGE_CODE     | VARCHAR(50)    | NOT NULL | —       | UK: `UK_PAGES_CODE`               |
| NAME_AR       | VARCHAR(100)   | NOT NULL | —       | Arabic name                       |
| NAME_EN       | VARCHAR(100)   | NOT NULL | —       | English name                      |
| ROUTE         | VARCHAR(200)   | NOT NULL | —       | UK: `UK_PAGES_ROUTE`              |
| ICON          | VARCHAR(50)    | NULL     | —       | —                                 |
| MODULE        | VARCHAR(50)    | NULL     | —       | e.g., SECURITY, FINANCE, GL       |
| PARENT_ID_FK  | BIGINT         | NULL     | —       | Self-reference (hierarchy) — plain `@Column`, not a JPA relationship, so no `@ForeignKey` name applies |
| DISPLAY_ORDER | BIGINT         | NULL     | —       | Sort order                        |
| IS_ACTIVE     | SMALLINT       | NULL     | 1       | Boolean flag (0/1)                |
| DESCRIPTION   | VARCHAR(500)   | NULL     | —       | —                                 |
| CREATED_AT    | TIMESTAMP      | NOT NULL | —       | Audit                             |
| CREATED_BY    | VARCHAR(100)   | NOT NULL | —       | Audit                             |
| UPDATED_AT    | TIMESTAMP      | NULL     | —       | Audit                             |
| UPDATED_BY    | VARCHAR(100)   | NULL     | —       | Audit                             |

- **Primary Key**: `SEC_PAGES_PK` (column and constraint both use this name; renamed from the generic `ID_PK`) — uses sequence `SEC_PAGES_SEQ`; column rename applied via `001_security_schema_migration_and_seed.sql`, executed by DBA; live in the database
- **FK Fields**: `PARENT_ID_FK` (self-reference to `SEC_PAGES.SEC_PAGES_PK`) — stored as a raw `Long` via `@Column`, not a `@ManyToOne`/`@JoinColumn` relationship
- **Flag Fields**: `IS_ACTIVE` (NUMBER(1) → Boolean)
- **Indexes**: `IDX_PAGES_MODULE`, `IDX_PAGES_ACTIVE`, `IDX_PAGES_PARENT`
- **Java Entity**: `Page extends AuditableEntity`
- **`@PrePersist`**: Normalizes `pageCode` to UPPERCASE, sets `active = true` if null
- **`@PreUpdate`**: Normalizes `pageCode` to UPPERCASE
- **NOTE**: `getActiveStatus()` used instead of `isActive()` to avoid Hibernate phantom column conflict

---

### 1.4 `PERMISSIONS` → `Permission.java`

| Column          | Type                            | Nullable | Default | Constraints                    |
|-----------------|---------------------------------|----------|---------|--------------------------------|
| PERMISSIONS_PK  | BIGINT GENERATED ALWAYS AS IDENTITY | NOT NULL | — | Primary Key              |
| NAME            | VARCHAR(150)                    | NOT NULL | —       | UK: `UK_PERMS_NAME`            |
| PAGE_ID_FK      | BIGINT                          | NULL     | —       | FK → `SEC_PAGES.SEC_PAGES_PK` (`FK_PERMS_PAGE`) |
| PERMISSION_TYPE | VARCHAR(20)                     | NULL     | —       | Enum: VIEW, CREATE, UPDATE, DELETE |
| CREATED_AT      | TIMESTAMP                       | NOT NULL | —       | Audit                          |
| CREATED_BY      | VARCHAR(100)                    | NOT NULL | —       | Audit                          |
| UPDATED_AT      | TIMESTAMP                       | NULL     | —       | Audit                          |
| UPDATED_BY      | VARCHAR(100)                    | NULL     | —       | Audit                          |

- **Primary Key**: `PERMISSIONS_PK` (column and constraint both use this name; renamed from `ID`) — column rename applied via `001_security_schema_migration_and_seed.sql`, executed by DBA; live in the database
- **FK Fields**: `PAGE_ID_FK` → `SEC_PAGES(SEC_PAGES_PK)` (nullable for system permissions), named `FK_PERMS_PAGE` via `@JoinColumn(foreignKey = @ForeignKey(name = ...))`
- **Indexes**: `IDX_PERMS_NAME`, `IDX_PERMS_PAGE_FK`, `IDX_PERMS_TYPE`
- **Naming Pattern**: `PERM_<PAGE_CODE>_<TYPE>` (e.g., `PERM_USER_VIEW`, `PERM_ROLE_CREATE`)
- **Java Entity**: `Permission extends AuditableEntity`
- **Enum**: `PermissionType` {VIEW, CREATE, UPDATE, DELETE}

---

### 1.5 `REFRESH_TOKENS` → `RefreshToken.java`

| Column            | Type                            | Nullable | Default | Constraints                          |
|-------------------|---------------------------------|----------|---------|--------------------------------------|
| REFRESH_TOKENS_PK | BIGINT GENERATED ALWAYS AS IDENTITY | NOT NULL | — | Primary Key                     |
| JTI               | VARCHAR(64)                     | NOT NULL | —       | UK: `UK_REFRESH_TOKENS_JTI`         |
| USER_ID_FK        | BIGINT                          | NOT NULL | —       | FK → `USERS.USERS_PK` (`FK_RT_USER`)|
| CREATED_AT        | TIMESTAMP                       | NOT NULL | —       | Auto via `@CreationTimestamp`        |
| EXPIRES_AT        | TIMESTAMP                       | NOT NULL | —       | Expiry timestamp                     |
| REVOKED           | SMALLINT                        | NOT NULL | 0       | Boolean flag (0/1)                   |

- **Primary Key**: `REFRESH_TOKENS_PK` (column and constraint both use this name; renamed from `ID`) — column rename applied via `001_security_schema_migration_and_seed.sql`, executed by DBA; live in the database
- **FK Fields**: `USER_ID_FK` → `USERS(USERS_PK)`, named `FK_RT_USER` via `@JoinColumn(foreignKey = @ForeignKey(name = ...))` (column renamed from `USER_ID`)
- **Flag Fields**: `REVOKED` (SMALLINT → Boolean)
- **JTI**: UUID used as refresh token identifier (stored in cookie)

---

### 1.6 Join Tables

#### `USER_ROLES`
| Column     | Type   | Constraints                                  |
|------------|--------|----------------------------------------------|
| USER_ID_FK | BIGINT | FK → `USERS(USERS_PK)` (`FK_UR_USER`) + PK  |
| ROLE_ID_FK | BIGINT | FK → `ROLES(ROLES_PK)` (`FK_UR_ROLE`) + PK  |

PK: `USER_ROLES_PK (USER_ID_FK, ROLE_ID_FK)` — composite PK constraint, not annotatable in JPA; enforced via `001_security_schema_migration_and_seed.sql`, executed by DBA; live in the database. FK names are real (`@JoinColumn(foreignKey = @ForeignKey(name = ...))`); columns renamed from `USER_ID`/`ROLE_ID`.

#### `ROLE_PERMISSIONS`
| Column     | Type   | Constraints                                      |
|------------|--------|--------------------------------------------------|
| ROLE_ID_FK | BIGINT | FK → `ROLES(ROLES_PK)` (`FK_RP_ROLE`) + PK       |
| PERM_ID_FK | BIGINT | FK → `PERMISSIONS(PERMISSIONS_PK)` (`FK_RP_PERM`) + PK |

PK: `ROLE_PERMISSIONS_PK (ROLE_ID_FK, PERM_ID_FK)` — same treatment as `USER_ROLES` above. FK names are real; columns renamed from `ROLE_ID`/`PERM_ID`.

---

### 1.7 `SEC_USER_PROFILE` → `SecUserProfile.java` — ✅ IMPLEMENTED (was §8.1 DESIGNED in v2.4.1)

1:1 profile/branch-assignment extension of `USERS`. Source: `entity/SecUserProfile.java`, schema in `002_datascope_selfservice_auth_schema.sql` (BLOCK 3/5).

| Column           | Type          | Nullable | Default | Constraints                                       |
|------------------|---------------|----------|---------|------------------------------------------------------|
| USER_ID_FK       | BIGINT        | NOT NULL | —       | PK **and** FK → `USERS(USERS_PK)` (`FK_SEC_USER_PROFILE_USER`) — shared 1:1 PK via `@MapsId`, not a separate surrogate PK |
| BRANCH_ID_FK     | BIGINT        | NOT NULL | —       | FK → `ORG_BRANCH(BRANCH_PK)` (`FK_SEC_USER_PROFILE_BRANCH`) — HARD-FK, cross-module |
| FULL_NAME_AR     | VARCHAR(200)  | NULL     | —       | —                                                     |
| FULL_NAME_EN     | VARCHAR(100)  | NULL     | —       | —                                                     |
| PREFERRED_LANG   | VARCHAR(10)   | NULL     | —       | No LOV domain governed yet (OQ-004, still OPEN) — plain string |
| EMPLOYEE_ID_FK   | BIGINT        | NULL     | —       | Unconstrained — no HR module governed yet (OQ-005, still OPEN); deliberately no `@JoinColumn`/FK |
| IS_ACTIVE_FL     | SMALLINT      | NOT NULL | 1       | Boolean flag (0/1)                                    |
| CREATED_AT/BY, UPDATED_AT/BY | —  | —        | —       | Standard `AuditableEntity` audit fields                |

- **Primary Key**: `PK_SEC_USER_PROFILE (USER_ID_FK)` — composite-free, shared with `USERS`
- **Indexes**: `IDX_SEC_USER_PROFILE_BRANCH`, `IDX_SEC_USER_PROFILE_EMPLOYEE`
- **Java Entity**: `SecUserProfile extends AuditableEntity`; `branchIdFk` is a plain scalar `Long`, **not** a JPA association — `erp-security` has no Maven dependency on `erp-org`, so referential integrity to `ORG_BRANCH` is DB-level only (`FK_SEC_USER_PROFILE_BRANCH`); the "branch must exist and be active" business rule (RULE-SEC-034) is enforced at the Service layer via an HTTP call (`OrgBranchClient`, see §7.2)
- **Repository**: `SecUserProfileRepository` (plain `JpaRepository<SecUserProfile, Long>` + `JpaSpecificationExecutor`)

---

### 1.8 `SEC_ROLE_BRANCH` → `SecRoleBranch.java` — ✅ IMPLEMENTED (was §8.2 DESIGNED in v2.4.1)

Role branch scope for DataScope. Source: `entity/SecRoleBranch.java` + `entity/SecRoleBranchId.java`, schema in `002_datascope_selfservice_auth_schema.sql` (BLOCK 3/5).

| Column             | Type       | Nullable | Default | Constraints                                              |
|--------------------|------------|----------|---------|---------------------------------------------------------|
| ROLE_ID_FK         | BIGINT     | NOT NULL | —       | FK → `ROLES(ROLES_PK)` (`FK_SEC_ROLE_BRANCH_ROLE`) — composite PK part 1 |
| BRANCH_ID_FK       | BIGINT     | NOT NULL | —       | FK → `ORG_BRANCH(BRANCH_PK)` (`FK_SEC_ROLE_BRANCH_BRANCH`) — HARD-FK, cross-module — composite PK part 2 |
| DATA_ACCESS_LEVEL  | VARCHAR(30)| NOT NULL | —       | LOV: `BRANCH_ONLY` / `BRANCH_AND_CHILDREN` / `ALL` (MD_LOOKUP_DETAIL, `LOOKUP_KEY = 'DATA_ACCESS_LEVEL'`), validated at Service layer via `MasterDataLookupClient`, not a DB check constraint |
| IS_ACTIVE_FL       | SMALLINT   | NOT NULL | 1       | Boolean flag (0/1)                                       |
| CREATED_AT/BY, UPDATED_AT/BY | — | —        | —       | Standard `AuditableEntity` audit fields                   |

- **Primary Key**: `PK_SEC_ROLE_BRANCH (ROLE_ID_FK, BRANCH_ID_FK)` — composite, via `@IdClass(SecRoleBranchId.class)`
- **Indexes**: `IDX_SEC_ROLE_BRANCH_BRANCH`
- **Java Entity**: `SecRoleBranch extends AuditableEntity`; both FK fields are plain scalar `Long`s (same no-JPA-association pattern as §1.7, same cross-module Maven-dependency reason)
- **Repository**: `SecRoleBranchRepository` — adds `existsByRoleIdFkAndBranchIdFk()` to surface a clean `409` (`SEC_ROLE_BRANCH_DUPLICATE_ASSIGNMENT`) instead of a raw constraint-violation stack trace
- **Unique constraint**: `(ROLE_ID_FK, BRANCH_ID_FK)` enforced by the composite PK itself (RULE-SEC-036)

---

### 1.9 `PASSWORD_RESET_TOKEN` → `PasswordResetToken.java` — ✅ IMPLEMENTED (was §8.5 PROPOSED in v2.4.1)

Single-use password reset token, modeled on `RefreshToken`'s plain-audit-field style (**not** `AuditableEntity`) — only `CREATED_AT`/`EXPIRES_AT`, no `CREATED_BY`/`UPDATED_AT`/`UPDATED_BY`.

| Column     | Type         | Nullable | Default | Constraints                                    |
|------------|--------------|----------|---------|-------------------------------------------------|
| TOKEN_PK   | BIGINT       | NOT NULL | SEQ     | Primary Key, sequence `PASSWORD_RESET_TOKEN_SEQ` |
| TOKEN      | VARCHAR(64)  | NOT NULL | —       | UK: `UK_PASSWORD_RESET_TOKEN_TOKEN` — UUID string |
| USER_ID_FK | BIGINT       | NOT NULL | —       | FK → `USERS(USERS_PK)` (`FK_PASSWORD_RESET_TOKEN_USER`) |
| CREATED_AT | TIMESTAMP    | NOT NULL | —       | Auto via `@CreationTimestamp`                    |
| EXPIRES_AT | TIMESTAMP    | NOT NULL | —       | TTL: `erp.security.self-service-token.reset-expiration-seconds` (default 3600 = 1h) |
| USED_FL    | SMALLINT     | NOT NULL | 0       | Boolean flag (0/1); `CHK_PASSWORD_RESET_TOKEN_USED_FL` |

- **Indexes**: `IDX_PASSWORD_RESET_TOKEN_USER`, `IDX_PASSWORD_RESET_TOKEN_EXPIRES`
- **Java Entity**: `PasswordResetToken` (plain `@Data @Builder`, no `AuditableEntity`)
- **Repository**: `PasswordResetTokenRepository` — `findByToken()`, `findByUser_IdAndUsedFlFalseAndExpiresAtAfter()` (RULE-SEC-039)

---

### 1.10 `ACCOUNT_ACTIVATION_TOKEN` → `AccountActivationToken.java` — ✅ IMPLEMENTED (was §8.6 PROPOSED in v2.4.1)

Single-use self-registration activation token. Same plain-audit-field style as §1.9.

| Column     | Type         | Nullable | Default | Constraints                                    |
|------------|--------------|----------|---------|-------------------------------------------------|
| TOKEN_PK   | BIGINT       | NOT NULL | SEQ     | Primary Key, sequence `ACCOUNT_ACTIVATION_TOKEN_SEQ` |
| TOKEN      | VARCHAR(64)  | NOT NULL | —       | UK: `UK_ACCOUNT_ACTIVATION_TOKEN_TOKEN` — UUID string |
| USER_ID_FK | BIGINT       | NOT NULL | —       | FK → `USERS(USERS_PK)` (`FK_ACCOUNT_ACTIVATION_TOKEN_USER`) |
| CREATED_AT | TIMESTAMP    | NOT NULL | —       | Auto via `@CreationTimestamp`                    |
| EXPIRES_AT | TIMESTAMP    | NOT NULL | —       | TTL: `erp.security.self-service-token.activation-expiration-seconds` (default 86400 = 24h) |
| USED_FL    | SMALLINT     | NOT NULL | 0       | Boolean flag (0/1); `CHK_ACCOUNT_ACTIVATION_TOKEN_USED_FL` |

- **Indexes**: `IDX_ACCT_ACTIVATION_TOKEN_USER`, `IDX_ACCT_ACTIVATION_TOKEN_EXPIRES`
- **Java Entity**: `AccountActivationToken` (plain `@Data @Builder`, no `AuditableEntity`)
- **Repository**: `AccountActivationTokenRepository` — `findByToken()`

---

## 2. IMPLEMENTED APIs

### 2.1 Authentication → `/api/auth`

| Method | Path                    | Description                                               |
|--------|-------------------------|-------------------------------------------------------------|
| POST   | `/api/auth/login`       | Authenticate user → returns `accessToken` + refresh cookie |
| POST   | `/api/auth/login-token` | Authenticate → returns `UserInfo` (tokens + roles/perms)  |
| POST   | `/api/auth/refresh`     | Refresh access token using `refresh_token` cookie         |
| POST   | `/api/auth/logout`      | Revoke refresh token + clear cookie                       |

**POST `/api/auth/login`**
- Request: `{ "username": "string", "password": "string" }`
- Response: `{ "accessToken": "string", "expiresIn": 900 }`
- Side effect: Sets `refresh_token` HttpOnly cookie
- Validation: `@NotBlank` on username and password
- Rate-limited: see §5.8. Exceeding the limit → `429` `RATE_LIMIT_LOGIN_EXCEEDED` with a `Retry-After` header (seconds)

**POST `/api/auth/login-token`**
- Request: same as login
- Response: `UserInfo` record: `{ accessToken, expiresIn, refreshToken, refreshExpiresIn, userId, username, enabled, roles[], permissions[] }`

**POST `/api/auth/refresh`**
- Request: no body — reads `refresh_token` cookie
- Response: `{ "accessToken": "string", "expiresIn": 900 }`
- Side effect: Old JTI revoked, new refresh cookie issued (token rotation)

**POST `/api/auth/logout`**
- No body required
- Response: `204 No Content`
- Behavior: Idempotent — succeeds even if cookie is missing

---

### 2.2 User Management → `/api/users`

| Method | Path                        | Permission Required       |
|--------|-----------------------------|---------------------------|
| POST   | `/api/users`                | `PERM_USER_CREATE`        |
| GET    | `/api/users`                | `PERM_USER_VIEW`          |
| POST   | `/api/users/search`         | `PERM_USER_VIEW`          |
| PUT    | `/api/users/{userId}/roles` | `PERM_USER_UPDATE`        |
| GET    | `/api/users/{userId}/roles` | `PERM_USER_VIEW`          |
| DELETE | `/api/users/{userId}`       | `PERM_USER_DELETE`        |
| PUT    | `/api/users/{userId}`       | `PERM_USER_UPDATE`        |

**POST `/api/users`** → Create User
- Request: `CreateUserRequest { username (3–80 chars, NotBlank), password (6–120 chars, NotBlank) }` — no `email` field (admin-created users only; email is signup-only, see §2.7)
- Response: `ApiResponse<UserDto>`
- Auto-assigns `ROLE_USER` if it exists
- Default `enabled = true`

**GET `/api/users`** → List Users (Paginated)
- Query params: `page`, `size` (default 20), `sort` (e.g., `username,asc`)
- Pagination enforced via `PageableUtils.enforceConstraints()`
- Allowed sort fields: `id`, `username`, `enabled`, `createdAt`

**POST `/api/users/search`** → Dynamic Search
- Request: `UserSearchContractRequest { filters[], page, size, sortBy, sortDir }`
- Allowed filter fields: `id`, `username`, `enabled`, `createdAt`
- Operators: EQ, NE, GT, GE, LT, LE, LIKE, IN, IS_NULL, IS_NOT_NULL, BETWEEN

**PUT `/api/users/{userId}/roles`** → Assign Roles (Full Replace)
- Request: `AssignRolesRequest { roleNames: Set<String> }`
- Replaces ALL existing roles

**DELETE `/api/users/{userId}`** → Delete User
- Blocked if user has active refresh tokens (`USER_HAS_ACTIVE_REFRESH_TOKENS`)
- Response: `204 No Content`

---

### 2.3 Role Management → `/api/roles`

| Method | Path                               | Permission Required    |
|--------|-------------------------------------|-------------------------|
| POST   | `/api/roles`                       | `PERM_ROLE_CREATE`    |
| POST   | `/api/roles/search`                | `PERM_ROLE_VIEW`      |
| GET    | `/api/roles/{roleId}`              | `PERM_ROLE_VIEW`      |
| PUT    | `/api/roles/{roleId}`              | `PERM_ROLE_UPDATE`    |
| DELETE | `/api/roles/{roleId}`              | `PERM_ROLE_DELETE`    |
| PUT    | `/api/roles/{roleId}/toggle-active`| `PERM_ROLE_UPDATE`    |
| GET    | `/api/roles/{roleId}/pages`        | `PERM_ROLE_VIEW`      |
| POST   | `/api/roles/{roleId}/pages`        | `PERM_ROLE_UPDATE`    |
| PUT    | `/api/roles/{roleId}/pages`        | `PERM_ROLE_UPDATE`    |
| DELETE | `/api/roles/{roleId}/pages/{pageCode}` | `PERM_ROLE_UPDATE` |
| POST   | `/api/roles/{roleId}/copy-from/{sourceRoleId}` | `PERM_ROLE_UPDATE` |

**POST `/api/roles`** → Create Role
- Request: `CreateRoleRequest { roleCode (uppercase pattern ^[A-Z][A-Z0-9_]*$), roleName, description, active }`
- `roleCode` is normalized to UPPERCASE and persisted to its own `ROLE_CODE` column; `roleName` and `description` are persisted as given to `NAME`/`DESCRIPTION`
- Duplicate `roleCode` → `409` `DUPLICATE_ROLE_CODE`; duplicate `roleName` → `409` `DUPLICATE_ROLE_NAME`
- Response: `ApiResponse<RoleDto>`

**PUT `/api/roles/{roleId}/toggle-active`**
- Request: `ToggleRoleActiveRequest { active: boolean }`

**GET `/api/roles/{roleId}/pages`** → Get Role Pages Matrix
- Response: `RolePagesMatrixResponse { roleId, roleName, assignments[{ pageCode, pageName, pageNameAr, permissions[] }] }`
- VIEW permission is excluded from `permissions[]` (implicit)

**POST `/api/roles/{roleId}/pages`** → Add Page to Role
- Request: `AddPageToRoleRequest { pageCode, permissions[] }` (permissions: CREATE/UPDATE/DELETE only)
- VIEW always auto-added
- Returns `409` if page already assigned to role

**PUT `/api/roles/{roleId}/pages`** → Sync Pages (Full Replace)
- Request: `SyncRolePagesRequest { assignments[{ pageCode, permissions[] }] }`
- Empty array removes all page access
- Replaces all current page-related permissions (identified by `PERM_` prefix)

**DELETE `/api/roles/{roleId}/pages/{pageCode}`** → Remove Page from Role
- Removes VIEW + all CRUD permissions for that page from the role

**POST `/api/roles/{roleId}/copy-from/{sourceRoleId}`** → Copy Permissions From Role
- `roleId` = target role, `sourceRoleId` = source role
- **Scope (page-scoped only, not full-role copy)**: reads source role's `ROLE_PERMISSIONS` filtered to `Permission.isPagePermission()` (i.e. joined `PERMISSIONS.PAGE_ID_FK IS NOT NULL`); Full-Replaces the target's page-scoped `ROLE_PERMISSIONS` rows with that set. The target's system-level permissions (`PAGE_ID_FK IS NULL`, e.g. `PERM_SYSTEM_ADMIN`) are left untouched — deliberate privilege-escalation guard (Rule 25).
- `targetRoleId == sourceRoleId` → `400 Bad Request` (`INVALID_OPERATION`)
- Source has zero page-scoped permissions → `409 Conflict` (`NO_PERMISSIONS_TO_COPY`) — Rule 24
- Response: `CopyPermissionsResponse { roleId, roleName, copiedFrom { roleId, roleName }, assignments[] }`
- Service: `RoleAccessService.copyPermissionsFromRole()`

---

### 2.4 Pages Management → `/api/pages`

| Method | Path                         | Permission Required    |
|--------|------------------------------|--------------------------|
| POST   | `/api/pages`                 | `PERM_PAGE_CREATE`    |
| POST   | `/api/pages/search`          | `PERM_PAGE_VIEW`      |
| GET    | `/api/pages/active`          | `PERM_PAGE_VIEW`      |
| GET    | `/api/pages/{id}`            | `PERM_PAGE_VIEW`      |
| PUT    | `/api/pages/{id}`            | `PERM_PAGE_UPDATE`    |
| PUT    | `/api/pages/{id}/deactivate` | `PERM_PAGE_DELETE`    |
| PUT    | `/api/pages/{id}/reactivate` | `PERM_PAGE_UPDATE`    |

**POST `/api/pages`** → Create Page
- Request: `CreatePageRequest { pageCode, nameAr, nameEn, route, icon, module, parentId, displayOrder, active, description }`
- Auto-generates 4 permission records: `PERM_<CODE>_VIEW`, `PERM_<CODE>_CREATE`, `PERM_<CODE>_UPDATE`, `PERM_<CODE>_DELETE`
- Validations: pageCode uppercase alphanumeric+underscore, length 2–50, route starts with `/`, no spaces
- Response: `PageResponse { ...fields..., permissionKeys: Map<String,String> }`

**GET `/api/pages/active`** → Get Active Pages
- No pagination; returns full list of active pages for dropdowns

**PUT `/api/pages/{id}/deactivate`** → Soft Delete
- Sets `IS_ACTIVE = 0`

**PUT `/api/pages/{id}/reactivate`** → Reactivate
- Sets `IS_ACTIVE = 1`

---

### 2.5 Permissions Management → `/api/permissions`

| Method | Path                      | Permission Required              |
|--------|---------------------------|-----------------------------------|
| POST   | `/api/permissions`        | `PERM_PERMISSION_CREATE`         |
| POST   | `/api/permissions/search` | `PERM_PERMISSION_VIEW`           |

Allowed filter fields: `name`, `module`
Allowed sort fields: `id`, `name`, `module`, `createdAt`, `updatedAt`

---

### 2.6 Menu → `/api/menu`

| Method | Path                         | Permission Required   |
|--------|------------------------------|--------------------------|
| GET    | `/api/menu/user-menu`        | (authenticated only)  |
| GET    | `/api/menu/user-menu/{userId}` | `PERM_USER_VIEW`   |

**GET `/api/menu/user-menu`**
- Builds dynamic menu tree (`parent → children`) from `SEC_PAGES`
- Filters pages where user has `PERM_*_VIEW` permission (resolved via `PAGE_ID_FK`)
- Only `IS_ACTIVE = 1` pages included
- Response: `List<MenuItemDto>` (tree structure)

---

### 2.7 Self-Service Auth → `/api/auth` — ✅ IMPLEMENTED (was §8.5/§8.6 PROPOSED in v2.4.1)

All four endpoints below share `AuthController`/`SecurityConfig`'s existing `/api/auth/**` `permitAll()` matcher — no `SecurityConfig` change was needed to add them. `POST /login` and `POST /signup` are additionally rate-limited (§5.8).

| Method | Path                        | Permission Required   |
|--------|------------------------------|------------------------|
| POST   | `/api/auth/signup`           | Public (`permitAll`)  |
| POST   | `/api/auth/signup/activate`  | Public (`permitAll`)  |
| POST   | `/api/auth/forgot-password`  | Public (`permitAll`)  |
| POST   | `/api/auth/reset-password`   | Public (`permitAll`)  |

**POST `/api/auth/signup`** → Self-Registration (RULE-SEC-030/040/041)
- Request: `SignupRequest { username (3–80), email (valid, ≤150), password (6–120) }`
- Rejects duplicate username → `409` `SIGNUP_USERNAME_ALREADY_EXISTS` (RULE-SEC-040); duplicate email → `409` `SIGNUP_EMAIL_ALREADY_EXISTS` (RULE-SEC-041) — checked against `USERS.EMAIL`/`UK_USERS_EMAIL`, distinct from `USERNAME_ALREADY_EXISTS` used by admin-facing `CreateUserRequest` (different message text, not reused)
- Creates `UserAccount` with `ENABLED = false` (RULE-SEC-030 — self-registered accounts MUST start disabled)
- Issues an `AccountActivationToken` (UUID, TTL `erp.security.self-service-token.activation-expiration-seconds`, default 24h) and publishes `AccountActivationRequestedEvent` (RULE-SEC-031 — publishes an event instead of calling a NotificationService directly; no subscriber exists yet, NotificationService is not implemented)
- Response: `SignupResponse { userId, username, enabled }` — no tokens issued (account is disabled)
- Service: `AuthService.signup()`

**POST `/api/auth/signup/activate`** → Account Activation (RULE-SEC-032/033)
- Request: `ActivateAccountRequest { token }`
- Validates token exists, unused, unexpired → `400` `ACTIVATION_TOKEN_INVALID_OR_EXPIRED`, or `400` `TOKEN_ALREADY_USED`
- Marks token used, sets `UserAccount.enabled = true`
- Response: `200 OK`, empty body
- Service: `AuthService.activateAccount()`

**POST `/api/auth/forgot-password`** → Forgot Password (RULE-SEC-038/039)
- Request: `ForgotPasswordRequest { email }`
- **Anti-enumeration (RULE-SEC-038)**: response is `200 OK` with an empty body **regardless of whether the email exists** — the method has exactly one exit path; the `Optional` lookup only drives internal side effects (token issuance, event publish), never the HTTP response
- If the email exists: invalidates any prior unexpired `PasswordResetToken` for that user (RULE-SEC-039), issues a new one (UUID, TTL `erp.security.self-service-token.reset-expiration-seconds`, default 1h), publishes `PasswordResetRequestedEvent` (RULE-SEC-031, same event-based pattern as signup)
- Service: `AuthService.forgotPassword()`

**POST `/api/auth/reset-password`** → Reset Password (RULE-SEC-032/033)
- Request: `ResetPasswordRequest { token, newPassword (6–120) }`
- Same token validation as activation → `400` `RESET_TOKEN_INVALID_OR_EXPIRED`, or `400` `TOKEN_ALREADY_USED`
- Marks token used, updates `UserAccount.password` (BCrypt-encoded)
- Response: `200 OK`, empty body
- Service: `AuthService.resetPassword()`

---

### 2.8 DataScope Management → `/api/v1/security/user-profiles`, `/api/v1/security/role-branches` — ✅ IMPLEMENTED (was §8.4 PROPOSED in v2.4.1)

⚠️ **Not yet permission-gated** — both controllers have no `@PreAuthorize`, deliberately (per `HANDOFF-PHASE-3-SVC-API.md`: adding `SecurityPermissions` constants + `@PreAuthorize` is explicitly Phase SEC's job, not this phase's). The paths are **not** in `SecurityConfig`'s `permitAll` list, so `anyRequest().authenticated()` still applies — any authenticated user (regardless of role/permission) can call them today.

**`SecUserProfileController` → `/api/v1/security/user-profiles`**

| Method | Path                | Permission Required   |
|--------|---------------------|------------------------|
| POST   | ``                  | Authenticated only    |
| GET    | ``                  | Authenticated only    |
| POST   | `/search`           | Authenticated only    |
| GET    | `/{userId}`         | Authenticated only    |
| PUT    | `/{userId}`         | Authenticated only    |

- **POST `` → Create** — `CreateSecUserProfileRequest { userIdFk, branchIdFk, fullNameAr?, fullNameEn?, preferredLang?, employeeIdFk? }`. Rejects if a profile already exists for `userIdFk` → `409` `SEC_USER_PROFILE_ALREADY_EXISTS`; `404` `USER_NOT_FOUND` if the user doesn't exist; `400` `SEC_USER_PROFILE_BRANCH_INACTIVE` if `branchIdFk` doesn't resolve to an active branch (RULE-SEC-034, via `OrgBranchClient`, see §7.2)
- **GET `` → List (Paginated)** — default sort `userIdFk,asc`; allowed sort fields: `userIdFk`, `branchIdFk`, `isActiveFl`, `createdAt`
- **POST `/search` → Dynamic Search** — `SecUserProfileSearchContractRequest extends BaseSearchContractRequest`; same allowed fields as List
- **GET `/{userId}` → Get by ID** — `404` `SEC_USER_PROFILE_NOT_FOUND` if absent
- **PUT `/{userId}` → Update** — `UpdateSecUserProfileRequest { branchIdFk, fullNameAr?, fullNameEn?, preferredLang?, employeeIdFk? }`; re-validates branch is active (RULE-SEC-034) on every update
- No DELETE endpoint
- Service: `SecUserProfileService`

**`SecRoleBranchController` → `/api/v1/security/role-branches`**

| Method | Path                          | Permission Required   |
|--------|-------------------------------|------------------------|
| POST   | ``                            | Authenticated only    |
| GET    | ``                            | Authenticated only    |
| POST   | `/search`                     | Authenticated only    |
| GET    | `/{roleId}/{branchId}`        | Authenticated only    |
| PUT    | `/{roleId}/{branchId}`        | Authenticated only    |
| DELETE | `/{roleId}/{branchId}`        | Authenticated only    |

- **POST `` → Create** — `CreateSecRoleBranchRequest { roleIdFk, branchIdFk, dataAccessLevel }`. `404` `ROLE_NOT_FOUND` if role doesn't exist; `409` `SEC_ROLE_BRANCH_DUPLICATE_ASSIGNMENT` if `(roleIdFk, branchIdFk)` already exists (RULE-SEC-036); `400` `SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED` if `dataAccessLevel` is blank or not a valid `LOV-SEC-002` code (RULE-SEC-035, via `MasterDataLookupClient`, see §7.2)
- **GET `` → List (Paginated)** — default sort `roleIdFk,asc`; allowed sort fields: `roleIdFk`, `branchIdFk`, `dataAccessLevel`, `isActiveFl`, `createdAt`
- **POST `/search` → Dynamic Search** — `SecRoleBranchSearchContractRequest extends BaseSearchContractRequest`; same allowed fields as List
- **GET `/{roleId}/{branchId}` → Get by composite key** — `404` `SEC_ROLE_BRANCH_NOT_FOUND` if absent
- **PUT `/{roleId}/{branchId}` → Update** — `UpdateSecRoleBranchRequest { dataAccessLevel }`; re-validates against `LOV-SEC-002` (RULE-SEC-035)
- **DELETE `/{roleId}/{branchId}`** → `204 No Content`
- Path shape uses `{roleId}/{branchId}` rather than a single `{id}`: `SEC_ROLE_BRANCH` has no surrogate PK (composite key), so a single `{id}` would require inventing a non-existent column
- Service: `SecRoleBranchService`

---

## 3. BUSINESS RULES & VALIDATIONS

1. **The system MUST reject login if username/password combination is invalid** — Spring Security `DaoAuthenticationProvider` + BCrypt verification.

2. **The system MUST generate a UUID-based JTI for each refresh token** — Stored in `REFRESH_TOKENS.JTI` and sent in `refresh_token` HttpOnly cookie.

3. **The system MUST revoke the old refresh token and issue a new one on every refresh call** — Token rotation pattern. Old JTI set `REVOKED = 1`.

4. **The system MUST reject refresh if JTI is revoked or expired** — Throws `REFRESH_EXPIRED_OR_REVOKED` or `REFRESH_REVOKED`.

5. **The system MUST make logout idempotent** — Does not fail if refresh cookie is missing or already revoked.

6. **The system MUST prevent duplicate usernames globally** — DB: `UK_USERS_USERNAME`. Service: explicit pre-check → `USERNAME_ALREADY_EXISTS`.

7. **The system MUST prevent deleting a user who has active refresh tokens** — Pre-check: `countByUser_Id()` → `USER_HAS_ACTIVE_REFRESH_TOKENS`.

8. **The system MUST auto-assign `ROLE_USER` to newly created users** — if `ROLE_USER` exists.

9. **The system MUST prevent duplicate role names globally** — DB: `UK_ROLES_NAME`. Service: explicit pre-check → `DUPLICATE_ROLE_NAME`.

9a. **The system MUST prevent duplicate role codes globally** — DB: `UK_ROLES_ROLE_CODE`. Service: explicit pre-check → `DUPLICATE_ROLE_CODE`.

10. **The system MUST prevent deleting a role that is assigned to users** — Pre-check: `hasUserAssignments()` → `ROLE_IN_USE` (Status 409 Conflict).

11. **The system MUST auto-generate 4 permission records (VIEW/CREATE/UPDATE/DELETE) when a new page is created** — Pattern: `PERM_<PAGE_CODE>_VIEW`, etc.

12. **The system MUST auto-add VIEW permission when assigning a page to a role** — VIEW is implicit and non-removable while page is assigned.

13. **The system MUST prevent assigning a page to a role if already assigned** — Check: VIEW permission for that page already in role → `PAGE_ALREADY_ASSIGNED_TO_ROLE`.

14. **The system MUST only accept CREATE/UPDATE/DELETE as optional permissions in page assignment** — `VIEW` is disallowed in the request body (auto-added server-side). Invalid types → `INVALID_PERMISSION_TYPE`.

15. **The system MUST normalize `pageCode` to UPPERCASE on create and update** — Both `@PrePersist` / `@PreUpdate` and service layer.

16. **The system MUST reject `pageCode` with invalid format** — Only `^[A-Z0-9_]+$` allowed, 2–50 characters.

17. **The system MUST reject route that does not start with `/`** — Validated in `PageService.createPage()` and `updatePage()`.

18. **The system MUST reject route that contains spaces** — Regex: `^/[a-zA-Z0-9/_-]+$`.

19. **The system MUST prevent duplicate routes globally** — DB: `UK_PAGES_ROUTE`. Service: explicit pre-check → `DUPLICATE_ROUTE`.

20. **The system MUST validate parent page exists before creating a child page** — `PARENT_PAGE_NOT_FOUND` if `parentId` is provided but not found.

21. **The system MUST build the user menu from `SEC_PAGES` filtered by `VIEW` permissions via `PAGE_ID_FK`** — Not via string pattern extraction; uses FK for resilience against page code renaming.

22. **The system MUST enforce pagination size limits** — `PageableUtils.enforceConstraints()` applied in `UserController.all()`.

23. **The system MUST validate sort field whitelists** — `PageableValidator.validateSortFields()` to prevent injection via sort params.

24. **The system MUST reject Copy Permissions if the source role has zero page-scoped permissions** — `RoleAccessService.copyPermissionsFromRole()` → `NO_PERMISSIONS_TO_COPY` (Status `CONFLICT` → HTTP 409, same pattern as `ROLE_IN_USE`).

25. **The system MUST only copy page-scoped permissions (`PAGE_ID_FK IS NOT NULL`) in Copy Permissions, never system-level permissions** — `Permission.isPagePermission()` filters both the source read and the target's existing-row deletion; the target's pre-existing system-level permissions are preserved. Privilege-escalation guard.

26. **The system MUST purge expired refresh tokens periodically** — `RefreshTokenCleanupJob` (`@Scheduled`, cron `erp.security.token-cleanup.cron`, default daily 03:00) calls `RefreshTokenRepository.deleteByExpiresAtBefore(now())`.

27. **The system MUST purge revoked refresh tokens older than a configured retention window** — same `RefreshTokenCleanupJob` run, `RefreshTokenRepository.deleteByRevokedTrueAndCreatedAtBefore(cutoff)`, cutoff = now − `erp.security.token-cleanup.revoked-retention-days` (default 30). `REFRESH_TOKENS` has no separate "revoked at" timestamp, so `CREATED_AT` is used as the age reference.

28. **The system MUST rate-limit login attempts by IP+username** — `LoginRateLimitFilter` (§5.8). Exceeding `erp.security.rate-limit.login.max-attempts` within `window-seconds` blocks that IP+username pair for `lockout-seconds` → `429` `RATE_LIMIT_LOGIN_EXCEEDED`.

29. **⚠️ RULE-SEC-029 — undefined gap, not implemented under this ID.** `execution-plan-SEC-gaps.md` Section 4.1 cites RULE-SEC-029 against API-SEC-040 (Sign Up), but Section 1.3's rule catalog only defines RULE-SEC-030 onward — RULE-SEC-029's content was never specified anywhere in the governing plan. `SignupRequest` carries standard `@NotBlank`/`@Size`/`@Email` validation as a placeholder; no rule-specific behavior was implemented against this ID. Flagged for the plan author, not silently dropped.

30. **The system MUST create self-registered accounts as disabled (`ENABLED = false`)** — RULE-SEC-030. `AuthService.signup()`; activation (rule 32) is required before login is possible.

31. **The system MUST publish domain events instead of calling NotificationService directly for account activation and password reset** — RULE-SEC-031. `AccountActivationRequestedEvent` / `PasswordResetRequestedEvent` via Spring's `ApplicationEventPublisher` — the first event-publishing mechanism anywhere in this codebase. No subscriber exists yet (NotificationService is not implemented); this only covers the publish side. Resolves the Security↔NotificationService dependency conflict at the code level, but see Conflict #20 (§9) for the unresolved *governance* status of that dependency.

32. **The system MUST require a valid, unused, non-expired token before activating an account or resetting a password** — RULE-SEC-032. `AuthService.activateAccount()` / `resetPassword()` → `400` `ACTIVATION_TOKEN_INVALID_OR_EXPIRED` / `RESET_TOKEN_INVALID_OR_EXPIRED` if missing/expired.

33. **The system MUST reject an already-used activation/reset token and mark a token used immediately on successful consumption** — RULE-SEC-033 → `400` `TOKEN_ALREADY_USED` (shared error code for both flows).

34. **The system MUST reject `SEC_USER_PROFILE` create/update if the referenced `BRANCH_ID_FK` does not exist or is not active** — RULE-SEC-034. `SecUserProfileService` calls `OrgBranchClient.assertActiveBranch()` → `400` `SEC_USER_PROFILE_BRANCH_INACTIVE`.

35. **The system MUST require `SEC_ROLE_BRANCH.DATA_ACCESS_LEVEL` to be a valid `LOV-SEC-002` code** — RULE-SEC-035. `SecRoleBranchService` calls `MasterDataLookupClient.assertValidDataAccessLevel()` against `MD_LOOKUP_DETAIL` (`LOOKUP_KEY = 'DATA_ACCESS_LEVEL'`) → `400` `SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED` if blank or not one of `BRANCH_ONLY`/`BRANCH_AND_CHILDREN`/`ALL`.

36. **The system MUST prevent duplicate `(ROLE_ID_FK, BRANCH_ID_FK)` assignments in `SEC_ROLE_BRANCH`** — RULE-SEC-036. Composite PK at the DB level; Service pre-check (`existsByRoleIdFkAndBranchIdFk()`) → `409` `SEC_ROLE_BRANCH_DUPLICATE_ASSIGNMENT` for a clean error instead of a raw constraint violation.

37. **⚠️ RULE-SEC-037 — not found in code.** No corresponding behavior, constant, or comment located anywhere in `erp-security`. Not confirmed implemented; not confirmed what it covers. Flagged for governance follow-up, same as rule 29.

38. **The system MUST respond identically to Forgot Password requests whether or not the email exists (anti-enumeration)** — RULE-SEC-038. `AuthService.forgotPassword()` has exactly one exit path — the internal `Optional` lookup drives only side effects, never the HTTP response/status.

39. **The system MUST invalidate any prior unexpired password reset token for a user when issuing a new one** — RULE-SEC-039. `PasswordResetTokenRepository.findByUser_IdAndUsedFlFalseAndExpiresAtAfter()` + bulk-mark-used before saving the new token.

40. **The system MUST prevent duplicate usernames globally at Sign Up** — RULE-SEC-040 → `409` `SIGNUP_USERNAME_ALREADY_EXISTS` (distinct error code/message from admin-facing rule 6's `USERNAME_ALREADY_EXISTS` — same DB constraint `UK_USERS_USERNAME`, different messaging per flow).

41. **The system MUST prevent duplicate emails globally at Sign Up** — RULE-SEC-041 → `409` `SIGNUP_EMAIL_ALREADY_EXISTS`. DB: `UK_USERS_EMAIL` (`002_datascope_selfservice_auth_schema.sql`, pending DBA execution, see §1.1).

---

## 4. LOOKUP & REFERENCE USAGE

- **`DATA_ACCESS_LEVEL` (LOV-SEC-002)** — the module's first live `MD_MASTER_LOOKUP`/`MD_LOOKUP_DETAIL` consumption: `SEC_ROLE_BRANCH.DATA_ACCESS_LEVEL` is validated against it via `MasterDataLookupClient` (§7.2), values seeded by `002_datascope_selfservice_auth_schema.sql` BLOCK 8: `BRANCH_ONLY`, `BRANCH_AND_CHILDREN`, `ALL`.
- `PermissionType` is an in-module enum (VIEW, CREATE, UPDATE, DELETE) — not a DB lookup.
- Module grouping (`MODULE` field on `SEC_PAGES`) is a free-text string (e.g., SECURITY, FINANCE, GL) — no FK to a lookup table.
- ⚠️ NOT FOUND: No LOV table backing the `MODULE` field — it is a raw string.

---

## 5. SECURITY & PERMISSIONS

### 5.1 Defined Permissions (from `SecurityPermissions.java`)

| Group         | Permission Constant       | Value                     |
|---------------|---------------------------|---------------------------|
| User          | `USER_VIEW`               | `PERM_USER_VIEW`          |
| User          | `USER_CREATE`             | `PERM_USER_CREATE`        |
| User          | `USER_UPDATE`             | `PERM_USER_UPDATE`        |
| User          | `USER_DELETE`             | `PERM_USER_DELETE`        |
| User          | `USER_MANAGE_ROLES`       | `PERM_USER_UPDATE`        |
| Role          | `ROLE_VIEW`               | `PERM_ROLE_VIEW`          |
| Role          | `ROLE_CREATE`             | `PERM_ROLE_CREATE`        |
| Role          | `ROLE_UPDATE`             | `PERM_ROLE_UPDATE`        |
| Role          | `ROLE_DELETE`             | `PERM_ROLE_DELETE`        |
| Permission    | `PERMISSION_VIEW`         | `PERM_PERMISSION_VIEW`    |
| Permission    | `PERMISSION_CREATE`       | `PERM_PERMISSION_CREATE`  |
| Permission    | `PERMISSION_UPDATE`       | `PERM_PERMISSION_UPDATE`  |
| Permission    | `PERMISSION_DELETE`       | `PERM_PERMISSION_DELETE`  |
| Menu          | `MENU_VIEW`               | `PERM_MENU_VIEW`          |
| Menu          | `MENU_CREATE`             | `PERM_MENU_CREATE`        |
| Menu          | `MENU_UPDATE`             | `PERM_MENU_UPDATE`        |
| Menu          | `MENU_DELETE`             | `PERM_MENU_DELETE`        |
| Page          | `PAGE_VIEW`               | `PERM_PAGE_VIEW`          |
| Page          | `PAGE_CREATE`             | `PERM_PAGE_CREATE`        |
| Page          | `PAGE_UPDATE`             | `PERM_PAGE_UPDATE`        |
| Page          | `PAGE_DELETE`             | `PERM_PAGE_DELETE`        |
| Master Lookup | `MASTER_LOOKUP_VIEW`      | `PERM_MASTER_LOOKUP_VIEW` |
| Lookup Detail | `LOOKUP_DETAIL_VIEW`      | `PERM_LOOKUP_DETAIL_VIEW` |
| GL Account    | `GL_ACCOUNT_VIEW`         | `PERM_GL_ACCOUNT_VIEW`    |
| GL Rule       | `GL_RULE_VIEW`            | `PERM_GL_RULE_VIEW`       |
| GL Journal    | `GL_JOURNAL_VIEW`         | `PERM_GL_JOURNAL_VIEW`    |
| GL Journal    | `GL_JOURNAL_APPROVE`      | `PERM_GL_JOURNAL_APPROVE` |
| GL Journal    | `GL_JOURNAL_POST`         | `PERM_GL_JOURNAL_POST`    |
| GL Journal    | `GL_JOURNAL_REVERSE`      | `PERM_GL_JOURNAL_REVERSE` |
| GL Journal    | `GL_JOURNAL_CANCEL`       | `PERM_GL_JOURNAL_CANCEL`  |
| GL Posting    | `GL_POSTING_VIEW`         | `PERM_GL_POSTING_VIEW`    |
| Legal Entity  | `LEGAL_ENTITY_VIEW`       | `PERM_LEGAL_ENTITY_VIEW`  |
| Region        | `REGION_VIEW`             | `PERM_REGION_VIEW`        |
| Branch        | `BRANCH_VIEW`             | `PERM_BRANCH_VIEW`        |
| Department    | `DEPARTMENT_VIEW`         | `PERM_DEPARTMENT_VIEW`    |
| Cost Center   | `COST_CENTER_VIEW`        | `PERM_COST_CENTER_VIEW`   |
| Profit Center | `PROFIT_CENTER_VIEW`      | `PERM_PROFIT_CENTER_VIEW` |
| Location Site | `LOCATION_SITE_VIEW`      | `PERM_LOCATION_SITE_VIEW` |
| Region Type   | `REGION_TYPE_VIEW`        | `PERM_REGION_TYPE_VIEW`   |
| System Admin  | `SYSTEM_ADMIN`            | `PERM_SYSTEM_ADMIN`       |

*(Full CRUD constant sets — VIEW/CREATE/UPDATE/DELETE — exist for most groups above; only VIEW is tabulated where all four follow the same `PERM_<GROUP>_<TYPE>` pattern.)*

⚠️ **No permission constants exist yet for the DataScope endpoints** (`SEC_USER_PROFILE`, `SEC_ROLE_BRANCH`) — see §2.8 and §9. Adding e.g. `USER_PROFILE_VIEW`/`ROLE_BRANCH_UPDATE` and wiring `@PreAuthorize` is explicitly out of scope for Phase SVC+API and belongs to Phase SEC.

### 5.2 Authorization Enforcement

- **Method-level security**: `@EnableMethodSecurity` active on `SecurityConfig`
- **`@PreAuthorize`**: Applied in service layer for core CRUD (Rule 19.1 — thin controllers). **Not yet applied** to `SecUserProfileService`/`SecRoleBranchService` (§2.8, §9)
- **JWT Filter**: `JwtAuthenticationFilter extends OncePerRequestFilter`
  - Extracts `Bearer` token from `Authorization` header
  - Extracts `userId` and `authorities` from token claims
  - Loads `UserDetails` via `CustomUserDetailsService.loadUserById()` (uses ID, not username for performance)
  - Populates `SecurityContextHolder`
- **Public endpoints** (no auth required): `/api/auth/**` (includes signup/activate/forgot-password/reset-password, §2.7), `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health`
- **Session**: `STATELESS` — no server-side sessions

### 5.3 Token Configuration (`JwtProperties`)

| Property                                       | Description                               | Default  |
|------------------------------------------------|--------------------------------------------|----------|
| `erp.security.jwt.secret`                      | HS256 signing key (≥ 256 bits / 32 chars) | required |
| `erp.security.jwt.access-expiration-seconds`   | Access token TTL                          | 3600     |
| `erp.security.jwt.refresh-expiration-seconds`  | Refresh token TTL                         | 604800   |

**Access Token Claims**: `sub` (username), `authorities[]`, `userId` — **still no `allowedBranches[]` claim** (confirmed directly against `JwtService`/`JwtProperties` this session; DataScope branch-scoping exists only as CRUD data (§1.7/1.8), not yet enforced at token/query time — see §9).
**Refresh Token Claims**: `sub` (username), `jti` (UUID)

### 5.4 Cookie Configuration (`CookieProperties`)

- Cookie name: `refresh_token`
- `HttpOnly`, `Secure` (configurable)
- `SameSite`: STRICT / LAX / NONE (configurable)
- Domain and path configurable via `erp.security.cookie.*`

### 5.5 CORS Configuration (`CorsProperties`)

- Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
- Credentials: `true`
- Allowed origins: configurable via `erp.security.cors.allowed-origins`

### 5.6 Cache Configuration

- `@EnableCaching` is active in both `RedisCacheConfig.java` and `ErpMainApplication.java` (see §7.4 — two independent `@SpringBootApplication` entry points, both with caching enabled).
- **Currently zero active `@Cacheable` methods in the module.** `PermissionService`'s `@CacheEvict`s on `permissionByName`/`permissionsList` are eviction-only with no populate-side `@Cacheable` — currently inert no-ops. `UserService`'s USERS-derived data is deliberately never cached (freshness/security policy).
- `application-prod.properties` and `application.properties` both set `spring.cache.type=redis`. Redis runs as its own container in `deploy/docker-compose.yml`, with the backend's startup gated on its healthcheck.

### 5.7 Scheduled Jobs

- **`RefreshTokenCleanupJob`** (`com.example.security.scheduler`) — single `@Scheduled` job covering both Rule 26 (expired) and Rule 27 (revoked) token purges in one run.
  - `erp.security.token-cleanup.cron` — cron expression, default `0 0 3 * * *` (daily 03:00)
  - `erp.security.token-cleanup.revoked-retention-days` — retention window in days, default `30`
  - Both bound via `RefreshTokenCleanupProperties` (`@ConfigurationProperties(prefix = "erp.security.token-cleanup")`, registered in `SecurityPropertiesConfig`)
  - Requires `@EnableScheduling` — present on both `ErpMainApplication` and `SecurityOracleJwtApplication` (§7.4)
  - ⚠️ **Not extended to `PASSWORD_RESET_TOKEN`/`ACCOUNT_ACTIVATION_TOKEN`** (§1.9/1.10) — those tables have no scheduled purge job; expired/used rows accumulate indefinitely. Not tracked by any rule in §3. Flagged in §9.

### 5.8 Login Rate Limiting

- **`LoginRateLimitFilter`** (`com.example.security.security`, `OncePerRequestFilter`) — registered in `SecurityConfig` via `addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class)`, ahead of `JwtAuthenticationFilter`
  - Matches `POST /api/auth/login` and `POST /api/auth/signup` (exact literal path match via `Set.of(...)`, now that `/signup` exists — confirmed both are covered)
  - Reads the request body via `CachedBodyHttpServletRequest` (caches bytes so the `@RequestBody` argument resolver downstream can still read it) to extract `username`; IP taken from `X-Forwarded-For` (first entry) falling back to `getRemoteAddr()`
  - Rate-limit key: `"<ip>|<username-lowercased>"` — never IP alone, so one endpoint cannot be used to lock out unrelated accounts, and vice versa
  - Enforcement: `LoginRateLimiterService`, in-memory `Bucket4j` bucket per key (`com.bucket4j:bucket4j-core:8.10.1`) sized `max-attempts` per `window-seconds`; exceeding it additionally sets an explicit lockout of `lockout-seconds` (separate from the bucket's own refill) tracked in a side `ConcurrentHashMap`
  - **Not shared across instances** — acceptable for the current single-container deployment (`deploy/docker-compose.yml`). Must move to Redis if the backend is ever horizontally scaled.
  - On block: `429`, `Retry-After` header (seconds remaining), bilingual body via `LocalizationService` + the standard `ApiResponse`/`ApiError` envelope (message key `RATE_LIMIT_LOGIN_EXCEEDED`) — resolved directly from the `Accept-Language` header (not `LocaleContextHolder`, since this filter can run before Spring MVC's locale resolution is established)
  - ⚠️ **`/api/auth/forgot-password` is NOT rate-limited** — only `/login` and `/signup` are in `PROTECTED_PATHS`. Forgot Password's anti-enumeration behavior (rule 38) makes brute-forcing it less directly useful than login, but it is unthrottled today. Flagged in §9.
- **`LoginRateLimitProperties`** (`@ConfigurationProperties(prefix = "erp.security.rate-limit.login")`): `max-attempts` (default 5), `window-seconds` (default 60), `lockout-seconds` (default 300)
- Frontend: `auth-login.component.ts` reads `Retry-After` on a `429` and disables the submit button with a live countdown (`AUTH.RETRY_IN_SECONDS`); does not auto-retry

---

## 6. APPROVAL WORKFLOW

⚠️ NOT FOUND — No approval workflow implemented in the Security module. All operations are direct CRUD without multi-step approval states.

---

## 7. CROSS-MODULE DEPENDENCIES

### 7.1 Security Module PROVIDES to Other Modules

- **`SecurityPermissions.java`** — Used by ALL modules as the single source of truth for permission constant strings in `@PreAuthorize` annotations
- **JWT infrastructure** — All modules rely on the access token issued by `/api/auth/login`

### 7.2 Security Module USES from Other Modules

- **`erp-common-utils`** module:
  - `AuditableEntity` — base entity superclass for all security entities
  - `BooleanNumberConverter` — NUMBER(1) → Boolean
  - `SecurityContextHelper` — getting current user from security context (`getUsernameOrSystem()`, `isAuthenticated()`)
  - `LocalizedException` — localized error throwing
  - `ServiceResult` / `Status` — service response wrapping
  - `ApiResponse` / `OperationCode` — REST response building
  - `CookieUtils` / `SameSite` — cookie management
  - `PageableUtils` / `PageableValidator` — pagination enforcement
  - `SpecBuilder` / `SetAllowedFields` / `DefaultFieldValueConverter` / `PageableBuilder` — dynamic search
- **`ORG_BRANCH` (erp-org), via HTTP, not a Maven dependency** — `OrgBranchClient` calls `GET /api/v1/org/branches/{id}` (API-ORG-012) as a same-JVM `RestTemplate` self-call (`http://localhost:${server.port}/...`) to validate branch existence/active-status for RULE-SEC-034. First `RestTemplate`/HTTP-client usage anywhere in the backend (`config/InternalApiClientConfig.java`, 3s connect / 5s read timeout). ⚠️ Forwards the **caller's own incoming `Authorization` header** to the internal call, since no service-to-service credential exists in this codebase — this means creating/updating a `SEC_USER_PROFILE` implicitly requires the calling user to also hold Organization's `BRANCH_VIEW` permission, a coupling not captured by any documented RBAC design. Flagged for architecture review (§9).
- **`MD_MASTER_LOOKUP`/`MD_LOOKUP_DETAIL` (erp-masterdata), via HTTP, same pattern** — `MasterDataLookupClient` calls `GET /api/lookups/DATA_ACCESS_LEVEL` to validate `SEC_ROLE_BRANCH.DATA_ACCESS_LEVEL` (RULE-SEC-035, LOV-SEC-002), same same-JVM self-call + forwarded-auth-header pattern as `OrgBranchClient`.

### 7.3 SEC_PAGES Referenced by Other Modules

- Other modules (GL, Finance, etc.) seed their pages into `SEC_PAGES` as well — the Security module owns the `SEC_PAGES` table structure

### 7.4 Deployment Entry Points

There are **two** independent `@SpringBootApplication` classes that can boot the security module — this matters for any config decision that lives on the application class (`@EnableCaching`, `@EnableScheduling`, component scan, autoconfig excludes, etc.):

- **`com.erp.main.ErpMainApplication`** (`erp-main` module) — the **real production entry point**. Aggregates security + masterdata + finance-gl + org into one context, all APIs on port 7272. `backend/CLAUDE.md` confirms this is what actually ships.
- **`com.example.security.SecurityOracleJwtApplication`** (`erp-security` module) — a standalone/dev bootstrap for running the security module in isolation (also port 7272 standalone per its own Javadoc). Not used in the assembled production deployment; `erp-security` is packaged as a plain `jar`, not run directly.

Any `@Enable*` annotation, exclude, or app-level config added for this module must be applied to **`ErpMainApplication`** to take effect in production. `SecurityOracleJwtApplication` should be kept consistent for standalone-mode parity, but is not a substitute.

---

## 8. GOVERNANCE STATUS OF THE DATASCOPE / SELF-SERVICE AUTH EXTENSION

The entities, endpoints, and rules described in §1.7–1.10, §2.7–2.8, and rules 29–41 above are **implemented in code** (verified this session by direct source read of `erp-security/src/main/java/**`), but their **governance authorization is not clean**:

- `master-registry.md` Section 13, **Conflict #20 (OPEN) / BLK-SEC-002**: a two-way dependency cycle between Security and NotificationService (Security would depend on NotificationService for Forgot Password/Sign Up emails; NotificationService already depends on Security). Rule 31 (event-publish pattern) is this codebase's resolution *at the implementation level*, but the registry conflict itself remains open — Section 7's dependency matrix was never updated to reflect it either way.
- `master-registry.md` Section 15: Security's row is split — core (`USERS`/`ROLES`/`PERMISSIONS`/`SEC_PAGES`/`REFRESH_TOKENS`) is `EXCEPTION ⚠️` (unaffected), but the extension scope (everything in §1.7–1.10/§2.7–2.8 here) is tracked separately as `PARTIALLY_READY ⚠️, BLOCKED pending BLK-SEC-002`.
- `master-registry.md` Section 14, **AQ-006/AQ-007 (OPEN)**: registry version-mismatch questions (this document's own version history has gaps — see §10) that predate and are independent of this session's reconciliation.

None of the above blocks were introduced or resolved by this update — they are carried forward as-is from `master-registry.md` and restated here so a reader of this file alone has the full picture without cross-referencing.

---

## 9. WHAT IS MISSING OR INCOMPLETE

1. **⚠️ No LOV table backing `SEC_PAGES.MODULE`** — raw string field, no FK to a lookup table.
2. **⚠️ `SEC_MENU_ITEM` legacy table** — present but unused; superseded by dynamic menu-building from `SEC_PAGES` (§2.6/§7.3 pattern). *(carried forward — not re-verified this session)*
3. ✅ CLOSED: rate limiting on `/api/auth/login` + `/api/auth/signup` — `LoginRateLimitFilter` + Bucket4j (§5.8, rule 28).
4. ✅ CLOSED: scheduled cleanup job for expired/revoked refresh tokens — `RefreshTokenCleanupJob` (§5.7, rules 26–27).
5. ✅ CLOSED: copy-permissions endpoint — `POST /api/roles/{roleId}/copy-from/{sourceRoleId}` (§2.3, rules 24–25).
6. ✅ **CLOSED**: PK column renames — `001_security_schema_migration_and_seed.sql` executed by DBA; live in the database.
7. ✅ **CLOSED as of this update**: `SEC_USER_PROFILE` — implemented (§1.7, §2.8).
8. ✅ **CLOSED as of this update**: `SEC_ROLE_BRANCH` — implemented (§1.8, §2.8).
9. **⚠️ STILL NOT IMPLEMENTED — JWT `allowedBranches[]` claim** (§5.3). DataScope data (`SEC_USER_PROFILE`/`SEC_ROLE_BRANCH`) is now CRUD-manageable but is not yet consumed anywhere at query/authorization time — no code enforces branch-scoped data access based on it. This is the largest remaining gap in the DataScope feature as a whole.
10. ✅ **CLOSED as of this update**: 10 DataScope CRUD endpoints (5 on `user-profiles`, 6 on `role-branches` — more than the 8 originally scoped, since Search was added alongside List per existing controller convention) — implemented (§2.8). **Not permission-gated yet** — see item 13 below.
11. ✅ **CLOSED as of this update**: Forgot Password — implemented (§2.7, rules 38–39).
12. ✅ **CLOSED as of this update**: Sign Up — implemented (§2.7, rules 30, 40–41).
13. **⚠️ NEW — DataScope endpoints have no permission gate.** `SecUserProfileController`/`SecRoleBranchController` require only a valid JWT (any authenticated user), not a specific `SecurityPermissions` constant. Explicitly deferred to Phase SEC (§5.1, §5.2, §2.8).
14. **⚠️ NEW — RULE-SEC-029 undefined** (rule 29) and **RULE-SEC-037 not found in code** (rule 37) — two rule-ID gaps in the governing plan itself, flagged rather than guessed at.
15. **⚠️ NEW — `PASSWORD_RESET_TOKEN`/`ACCOUNT_ACTIVATION_TOKEN` have no cleanup job** (§5.7) — unlike `REFRESH_TOKENS`, expired/used rows are never purged.
16. **⚠️ NEW — `/api/auth/forgot-password` is not rate-limited** (§5.8) — only `/login` and `/signup` are protected by `LoginRateLimitFilter`.
17. **⚠️ NEW — cross-module HTTP clients forward the caller's own `Authorization` header** (`OrgBranchClient`, `MasterDataLookupClient`, §7.2) instead of using a dedicated service-to-service credential — flagged by the implementer for architecture review, not this document's own finding, but confirmed present in code.
18. **⚠️ Governance authorization gaps remain OPEN** — Conflict #20/BLK-SEC-002, AQ-006, AQ-007 (see §8 for the full explanation).
19. **⚠️ Cycle 2 findings** — tracked separately in `implementation-gaps-SEC-cycle2.md` (not duplicated here; not re-verified this session).

---

## 10. CHANGE LOG

| Version | Date       | Change                                                                                                                                                                                                | By |
|---------|------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----|
| 2.2.0   | 2026-06-28 | Oracle → PostgreSQL migration: column types updated (NUMBER→BIGINT etc.). No Java entity changes required. | Agent (PG Migration) |
| 2.3.0   | 2026-07-07 | DataScope Extension documented: `SEC_USER_PROFILE`, `SEC_ROLE_BRANCH`, JWT `allowedBranches[]`, 8 planned API endpoints — all ⚠️ DESIGNED-NOT-IMPLEMENTED. | Agent (DataScope Extension) |
| 2.4.0   | 2026-07-08 | User-supplied re-scan of actual code: rate limiting (`LoginRateLimitFilter`), scheduled cleanup (`RefreshTokenCleanupJob`), copy-permissions endpoint all now IMPLEMENTED. PK columns renamed (pending DBA execution). `Role.roleCode`/`description` now persisted. | User re-scan + Agent reconciliation |
| 2.4.1   | 2026-07-09 | Fixed internal inconsistency: `EMAIL` referenced in §8.5 prose as added to `USERS` but missing from §1.1's table definition; duplicated in §8.1 `SEC_USER_PROFILE`. Added `EMAIL` to §1.1, removed duplicate from §8.1. Data-consistency fix only. | Agent (consistency fix) |
| 2.5.0   | 2026-07-09 | **Full reconciliation against source code** (`erp-security/src/main/java/**`) and `HANDOFF-PHASE-3-SVC-API.md`, superseding the prior "DESIGNED — NOT YET IMPLEMENTED" status: `SEC_USER_PROFILE`, `SEC_ROLE_BRANCH`, `PASSWORD_RESET_TOKEN`, `ACCOUNT_ACTIVATION_TOKEN` entities now documented as implemented (§1.7–1.10); Sign Up/Activate/Forgot-Password/Reset-Password (§2.7) and DataScope CRUD APIs (§2.8) now documented as implemented; added rules 29–41 (§3), including two flagged gaps (RULE-SEC-029 undefined, RULE-SEC-037 not found); added new cross-module HTTP-client dependency section (§7.2: `OrgBranchClient`, `MasterDataLookupClient`); added §8 summarizing the still-OPEN governance conflicts (#19, #20/BLK-SEC-002, AQ-006, AQ-007) so this document doesn't imply authorization it doesn't have; §9 rewritten to close items 7/8/10/11/12 and add newly-observed gaps (no permission gate on DataScope endpoints, no cleanup job for the two new token tables, forgot-password not rate-limited, forwarded-Authorization-header pattern, rule-ID gaps). Superseded the old "Upload Note" (v2.4.0/v2.4.1 session artifact, no longer relevant). | Agent (source-code reconciliation) |

---
*End of registry-security.md*
