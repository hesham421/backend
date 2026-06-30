# registry-security.md
════════════════════════════════════════
Module       : SECURITY
Version      : 2.2.0
Last Updated : 2026-06-28
Updated By   : Agent (PG Migration)
Status       : Complete
════════════════════════════════════════

---

## 1. ENTITIES & DB STRUCTURE

### 1.1 `USERS` → `UserAccount.java`

| Column      | Type                            | Nullable | Default | Constraints                    |
|-------------|---------------------------------|----------|---------|--------------------------------|
| ID          | BIGINT GENERATED ALWAYS AS IDENTITY | NOT NULL | — | PK → `USERS_PK`          |
| USERNAME    | VARCHAR(80)                     | NOT NULL | —       | UK: `UK_USERS_USERNAME`        |
| PASSWORD    | VARCHAR(200)                    | NOT NULL | —       | BCrypt hash                    |
| ENABLED     | SMALLINT                        | NOT NULL | 1       | CK: `CK_USERS_ENABLED` IN (0,1)|
| CREATED_AT  | TIMESTAMP                       | NOT NULL | —       | Audit                          |
| CREATED_BY  | VARCHAR(100)                    | NOT NULL | —       | Audit                          |
| UPDATED_AT  | TIMESTAMP                       | NULL     | —       | Audit                          |
| UPDATED_BY  | VARCHAR(100)                    | NULL     | —       | Audit                          |

- **Primary Key**: `ID` (`USERS_PK`)
- **Indexes**: `IDX_USERS_ENABLED`, `IDX_USERS_USERNAME`
- **FK Fields**: none (join table `USER_ROLES` links to `ROLES`)
- **Flag Fields**: `ENABLED` (SMALLINT → converted to Boolean via `BooleanNumberConverter`)
- **System Fields**: `CREATED_AT`, `CREATED_BY`, `UPDATED_AT`, `UPDATED_BY`
- **Java Entity**: `UserAccount extends AuditableEntity`
- **Relationships**: `@ManyToMany` → `ROLES` via join table `USER_ROLES`
- **⚠️ NAMING NOTE**: PK column is `ID` (not `ID_PK` per convention).

---

### 1.2 `ROLES` → `Role.java`

| Column      | Type                            | Nullable | Default | Constraints                   |
|-------------|---------------------------------|----------|---------|-------------------------------|
| ID          | BIGINT GENERATED ALWAYS AS IDENTITY | NOT NULL | — | PK → `ROLES_PK`          |
| NAME        | VARCHAR(60)                     | NOT NULL | —       | UK: `UK_ROLES_NAME`           |
| IS_ACTIVE   | SMALLINT                        | NOT NULL | 1       | CK: `CK_ROLES_ACTIVE` IN (0,1)|
| CREATED_AT  | TIMESTAMP                       | NOT NULL | —       | Audit                         |
| CREATED_BY  | VARCHAR(100)                    | NOT NULL | —       | Audit                         |
| UPDATED_AT  | TIMESTAMP                       | NULL     | —       | Audit                         |
| UPDATED_BY  | VARCHAR(100)                    | NULL     | —       | Audit                         |

- **Primary Key**: `ID` (`ROLES_PK`)
- **Indexes**: `IDX_ROLES_IS_ACTIVE`
- **Flag Fields**: `IS_ACTIVE` (SMALLINT → Boolean via `BooleanNumberConverter`)
- **System Fields**: `CREATED_AT`, `CREATED_BY`, `UPDATED_AT`, `UPDATED_BY`
- **Java Entity**: `Role extends AuditableEntity`
- **Java Field Mapping**:
  - `NAME` DB column → Java `roleName` field (legacy `getName()` deprecated alias)
  - `roleCode`, `description` are `@Transient` (not persisted)
- **Relationships**: `@ManyToMany` → `PERMISSIONS` via `ROLE_PERMISSIONS`
- **⚠️ NAMING NOTE**: PK column is `ID` (not `ID_PK` per convention). DB column is `NAME` while Java field is `roleName`.
- **⚠️ TRANSIENT CONFLICT**: `roleCode` and `description` exist in DTOs but are `@Transient` in entity — NOT stored in DB.

---

### 1.3 `SEC_PAGES` → `Page.java`

| Column        | Type           | Nullable | Default | Constraints                       |
|---------------|----------------|----------|---------|-----------------------------------|
| ID_PK         | BIGINT         | NOT NULL | SEQ     | PK → `SEC_PAGES_PK` (uses `SEC_PAGES_SEQ`) |
| PAGE_CODE     | VARCHAR(50)    | NOT NULL | —       | UK: `UK_PAGES_CODE`               |
| NAME_AR       | VARCHAR(100)   | NOT NULL | —       | Arabic name                       |
| NAME_EN       | VARCHAR(100)   | NOT NULL | —       | English name                      |
| ROUTE         | VARCHAR(200)   | NOT NULL | —       | UK: `UK_PAGES_ROUTE`              |
| ICON          | VARCHAR(50)    | NULL     | —       | —                                 |
| MODULE        | VARCHAR(50)    | NULL     | —       | e.g., SECURITY, FINANCE, GL       |
| PARENT_ID_FK  | BIGINT         | NULL     | —       | Self-reference FK (hierarchy)     |
| DISPLAY_ORDER | BIGINT         | NULL     | —       | Sort order                        |
| IS_ACTIVE     | SMALLINT       | NULL     | 1       | CK: `CK_PAGES_ACTIVE` IN (0,1)   |
| DESCRIPTION   | VARCHAR(500)   | NULL     | —       | —                                 |
| CREATED_AT    | TIMESTAMP      | NOT NULL | —       | Audit                             |
| CREATED_BY    | VARCHAR(100)   | NOT NULL | —       | Audit                             |
| UPDATED_AT    | TIMESTAMP      | NULL     | —       | Audit                             |
| UPDATED_BY    | VARCHAR(100)   | NULL     | —       | Audit                             |

- **Primary Key**: `ID_PK` (`SEC_PAGES_PK`) — uses sequence `SEC_PAGES_SEQ`
- **FK Fields**: `PARENT_ID_FK` (self-reference to `SEC_PAGES.ID_PK`)
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
| ID              | BIGINT GENERATED ALWAYS AS IDENTITY | NOT NULL | — | PK → `PERMISSIONS_PK`    |
| NAME            | VARCHAR(150)                    | NOT NULL | —       | UK: `UK_PERMS_NAME`            |
| PAGE_ID_FK      | BIGINT                          | NULL     | —       | FK → `SEC_PAGES.ID_PK` (`FK_PERMS_PAGE`) |
| PERMISSION_TYPE | VARCHAR(20)                     | NULL     | —       | Enum: VIEW, CREATE, UPDATE, DELETE |
| CREATED_AT      | TIMESTAMP                       | NOT NULL | —       | Audit                          |
| CREATED_BY      | VARCHAR(100)                    | NOT NULL | —       | Audit                          |
| UPDATED_AT      | TIMESTAMP                       | NULL     | —       | Audit                          |
| UPDATED_BY      | VARCHAR(100)                    | NULL     | —       | Audit                          |

- **Primary Key**: `ID` (`PERMISSIONS_PK`)
- **FK Fields**: `PAGE_ID_FK` → `SEC_PAGES(ID_PK)` (nullable for system permissions)
- **Indexes**: `IDX_PERMS_NAME`, `IDX_PERMS_PAGE_FK`, `IDX_PERMS_TYPE`
- **Naming Pattern**: `PERM_<PAGE_CODE>_<TYPE>` (e.g., `PERM_USER_VIEW`, `PERM_ROLE_CREATE`)
- **Java Entity**: `Permission extends AuditableEntity`
- **Enum**: `PermissionType` {VIEW, CREATE, UPDATE, DELETE}

---

### 1.5 `REFRESH_TOKENS` → `RefreshToken.java`

| Column     | Type                            | Nullable | Default | Constraints                          |
|------------|---------------------------------|----------|---------|--------------------------------------|
| ID         | BIGINT GENERATED ALWAYS AS IDENTITY | NOT NULL | — | PK → `REFRESH_TOKENS_PK`        |
| JTI        | VARCHAR(64)                     | NOT NULL | —       | UK: `UK_REFRESH_TOKENS_JTI`         |
| USER_ID    | BIGINT                          | NOT NULL | —       | FK → `USERS.ID` (`FK_RT_USER`)      |
| CREATED_AT | TIMESTAMP                       | NOT NULL | —       | Auto via `@CreationTimestamp`        |
| EXPIRES_AT | TIMESTAMP                       | NOT NULL | —       | Expiry timestamp                     |
| REVOKED    | SMALLINT                        | NOT NULL | 0       | CK: `CK_RT_REVOKED` IN (0,1)        |

- **Primary Key**: `ID` (`REFRESH_TOKENS_PK`)
- **FK Fields**: `USER_ID` → `USERS(ID)`
- **Flag Fields**: `REVOKED` (SMALLINT → Boolean)
- **JTI**: UUID used as refresh token identifier (stored in cookie)

---

### 1.6 Join Tables

#### `USER_ROLES`
| Column  | Type   | Constraints                                  |
|---------|--------|----------------------------------------------|
| USER_ID | BIGINT | FK → `USERS(ID)` (`FK_UR_USER`) + PK        |
| ROLE_ID | BIGINT | FK → `ROLES(ID)` (`FK_UR_ROLE`) + PK        |

PK: `USER_ROLES_PK (USER_ID, ROLE_ID)`

#### `ROLE_PERMISSIONS`
| Column  | Type   | Constraints                                      |
|---------|--------|--------------------------------------------------|
| ROLE_ID | BIGINT | FK → `ROLES(ID)` (`FK_RP_ROLE`) + PK            |
| PERM_ID | BIGINT | FK → `PERMISSIONS(ID)` (`FK_RP_PERM`) + PK      |

PK: `ROLE_PERMISSIONS_PK (ROLE_ID, PERM_ID)`

---

### 1.7 `SEC_MENU_ITEM` (Legacy — Deprecated)

Table exists in DB schema (`V0__full_schema_create.sql`) but is **NOT actively used** by the application. The `MenuService` now builds menus dynamically from `SEC_PAGES` + permissions. The `SEC_MENU_ITEM` table is a legacy artifact.

---

## 2. IMPLEMENTED APIs

### 2.1 Authentication → `/api/auth`

| Method | Path                    | Description                                               |
|--------|-------------------------|-----------------------------------------------------------|
| POST   | `/api/auth/login`       | Authenticate user → returns `accessToken` + refresh cookie |
| POST   | `/api/auth/login-token` | Authenticate → returns `UserInfo` (tokens + roles/perms)  |
| POST   | `/api/auth/refresh`     | Refresh access token using `refresh_token` cookie         |
| POST   | `/api/auth/logout`      | Revoke refresh token + clear cookie                       |

**POST `/api/auth/login`**
- Request: `{ "username": "string", "password": "string" }`
- Response: `{ "accessToken": "string", "expiresIn": 900 }`
- Side effect: Sets `refresh_token` HttpOnly cookie
- Validation: `@NotBlank` on username and password

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
- Request: `CreateUserRequest { username (3–80 chars, NotBlank), password (6–120 chars, NotBlank) }`
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
|--------|------------------------------------|------------------------|
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

**POST `/api/roles`** → Create Role
- Request: `CreateRoleRequest { roleCode (uppercase pattern ^[A-Z][A-Z0-9_]*$), roleName, description, active }`
- `roleCode` is stored as `NAME` in DB (normalized to UPPERCASE)
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

---

### 2.4 Pages Management → `/api/pages`

| Method | Path                         | Permission Required    |
|--------|------------------------------|------------------------|
| POST   | `/api/pages`                 | `PERM_PAGE_CREATE`    |
| POST   | `/api/pages/search`          | `PERM_PAGE_VIEW` ✓    |
| GET    | `/api/pages/active`          | `PERM_PAGE_VIEW` ✓    |
| GET    | `/api/pages/{id}`            | `PERM_PAGE_VIEW` ✓    |
| PUT    | `/api/pages/{id}`            | `PERM_PAGE_UPDATE`    |
| PUT    | `/api/pages/{id}/deactivate` | `PERM_PAGE_DELETE` ✓  |
| PUT    | `/api/pages/{id}/reactivate` | `PERM_PAGE_UPDATE` ✓  |

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
|--------|---------------------------|----------------------------------|
| POST   | `/api/permissions`        | `PERM_PERMISSION_CREATE` ✓       |
| POST   | `/api/permissions/search` | `PERM_PERMISSION_VIEW` ✓         |

Allowed filter fields: `name`, `module`
Allowed sort fields: `id`, `name`, `module`, `createdAt`, `updatedAt`

---

### 2.6 Menu → `/api/menu`

| Method | Path                         | Permission Required   |
|--------|------------------------------|-----------------------|
| GET    | `/api/menu/user-menu`        | (authenticated only)  |
| GET    | `/api/menu/user-menu/{userId}` | `PERM_USER_VIEW`   |

**GET `/api/menu/user-menu`**
- Builds dynamic menu tree (`parent → children`) from `SEC_PAGES`
- Filters pages where user has `PERM_*_VIEW` permission (resolved via `PAGE_ID_FK`)
- Only `IS_ACTIVE = 1` pages included
- Response: `List<MenuItemDto>` (tree structure)

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

9. **The system MUST prevent duplicate role names globally** — DB: `UK_ROLES_NAME`. Service: explicit pre-check → `DUPLICATE_ROLE_CODE`.

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

---

## 4. LOOKUP & REFERENCE USAGE

- **No external Lookup tables (MD_MASTER_LOOKUP / MD_LOOKUP_DETAIL) used** within the security module.
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
| Activity      | `ACTIVITY_VIEW`           | `PERM_ACTIVITY_VIEW`      |
| GL Account    | `GL_ACCOUNT_VIEW`         | `PERM_GL_ACCOUNT_VIEW`    |
| GL Rule       | `GL_RULE_VIEW`            | `PERM_GL_RULE_VIEW`       |
| GL Journal    | `GL_JOURNAL_VIEW`         | `PERM_GL_JOURNAL_VIEW`    |
| GL Journal    | `GL_JOURNAL_APPROVE`      | `PERM_GL_JOURNAL_APPROVE` |
| GL Journal    | `GL_JOURNAL_POST`         | `PERM_GL_JOURNAL_POST`    |
| GL Journal    | `GL_JOURNAL_REVERSE`      | `PERM_GL_JOURNAL_REVERSE` |
| GL Journal    | `GL_JOURNAL_CANCEL`       | `PERM_GL_JOURNAL_CANCEL`  |
| GL Posting    | `GL_POSTING_VIEW`         | `PERM_GL_POSTING_VIEW`    |
| Legal Entity  | `LEGAL_ENTITY_VIEW`       | `PERM_LEGAL_ENTITY_VIEW`  |
| System Admin  | `SYSTEM_ADMIN`            | `PERM_SYSTEM_ADMIN`       |

### 5.2 Authorization Enforcement

- **Method-level security**: `@EnableMethodSecurity` active on `SecurityConfig`
- **`@PreAuthorize`**: Applied in service layer (Rule 19.1 — thin controllers)
- **JWT Filter**: `JwtAuthenticationFilter extends OncePerRequestFilter`
  - Extracts `Bearer` token from `Authorization` header
  - Extracts `userId` and `authorities` from token claims
  - Loads `UserDetails` via `CustomUserDetailsService.loadUserById()` (uses ID, not username for performance)
  - Populates `SecurityContextHolder`
- **Public endpoints** (no auth required): `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health`
- **Session**: `STATELESS` — no server-side sessions

### 5.3 Token Configuration (`JwtProperties`)

| Property                                       | Description                               | Default  |
|------------------------------------------------|-------------------------------------------|----------|
| `erp.security.jwt.secret`                      | HS256 signing key (≥ 256 bits / 32 chars) | required |
| `erp.security.jwt.access-expiration-seconds`   | Access token TTL                          | 3600     |
| `erp.security.jwt.refresh-expiration-seconds`  | Refresh token TTL                         | 604800   |

**Access Token Claims**: `sub` (username), `authorities[]`, `userId`
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

### 7.3 SEC_PAGES Referenced by Other Modules

- Other modules (GL, Finance, etc.) seed their pages into `SEC_PAGES` as well — the Security module owns the `SEC_PAGES` table structure

---

## 8. WHAT IS MISSING OR INCOMPLETE

1. **⚠️ Redis caching is DISABLED** — `@EnableCaching` is commented out in `RedisCacheConfig`. All `@Cacheable`, `@CachePut`, `@CacheEvict` annotations in services are also commented out. Cache infrastructure is configured but not active. *(DEFERRED — infrastructure decision)*

2. **⚠️ `roleCode` and `description` are `@Transient`** in `Role.java` — They appear in `CreateRoleRequest` and `RoleDto` but are not persisted to DB. Only `roleName` is stored. This creates an API contract vs. DB mismatch. *(PERMANENT EXCEPTION — AS-IS)*

3. **⚠️ `SEC_MENU_ITEM` table** exists in DB schema but is never used by `MenuService`. It is a legacy artifact. The menu is now built from `SEC_PAGES`. *(DEFERRED)*

4. **⚠️ `RoleController` copy-permissions endpoint** referenced by `CopyPermissionsResponse` DTO and `NO_PERMISSIONS_TO_COPY` error code — no controller method implementing this feature was found. *(DEFERRED)*

5. **⚠️ PK naming convention deviation** — `USERS.ID`, `ROLES.ID`, `PERMISSIONS.ID`, `REFRESH_TOKENS.ID` use `ID` instead of the project convention `ID_PK`. Only `SEC_PAGES` follows the `ID_PK` convention. *(PERMANENT EXCEPTION — AS-IS)*

6. **⚠️ No rate limiting** on `/api/auth/login` — no protection against brute-force attacks. *(DEFERRED — infrastructure concern)*

7. **⚠️ No cleanup job** for expired `REFRESH_TOKENS` rows — `RefreshTokenRepository.deleteByExpiresAtBefore()` exists but no `@Scheduled` task using it was found. *(DEFERRED — operational concern)*

---

## 9. CHANGE LOG

| Version | Date       | Change Summary                                              | Updated By     |
|---------|------------|-------------------------------------------------------------|----------------|
| 1.0.0   | 2026-04-11 | Initial extraction — full scan of erp-security module       | GitHub Copilot |
| 2.0.0   | 2026-06-21 | Multi-tenancy removal — TENANT_ID eliminated system-wide    | Claude Code    |
| 2.1.0   | 2026-06-26 | GAP-SEC-01/02/03 closed: @PreAuthorize added to PageController (5 endpoints) and PermissionController (2 endpoints). PERMISSION_UPDATE constant confirmed present. Tests added for 401/403/200 scenarios on all affected endpoints. | Agent (Gap Closure) |
| 2.2.0   | 2026-06-28 | Oracle → PostgreSQL migration: column types updated in registry (NUMBER→BIGINT, NUMBER(IDENTITY)→BIGINT GENERATED ALWAYS AS IDENTITY, VARCHAR2→VARCHAR, NUMBER(1)→SMALLINT). No Java entity changes were required — all types were already PostgreSQL-compatible. | Agent (PG Migration) |

### v2.0.0 — Multi-Tenancy Removal Detail

**Deleted classes** (7 files from `erp-common-utils`):
- `TenantContext` — ThreadLocal holder
- `TenantHelper` — static helper with `requireTenant()`
- `TenantEntityListener` — JPA `@PrePersist`/`@PreUpdate` listener
- `TenantScoped` — interface with `getTenantId()`/`setTenantId()`
- `TenantAuditableEntity` — `@MappedSuperclass` with `TENANT_ID` column
- `TenantLoggingFilter` — MDC logging filter
- `TenantProperties` — `@ConfigurationProperties` for `X-Tenant-ID` header

**Entity changes**: All 5 security entities now extend `AuditableEntity` directly. `TENANT_ID` column removed. Unique constraints renamed to single-column (no tenant scope):
- `UK_USERS_TENANT_USERNAME` → `UK_USERS_USERNAME (USERNAME)`
- `UK_ROLES_TENANT_NAME` → `UK_ROLES_NAME (NAME)`
- `UK_PAGES_TENANT_CODE` → `UK_PAGES_CODE (PAGE_CODE)`
- `UK_PAGES_TENANT_ROUTE` → `UK_PAGES_ROUTE (ROUTE)`
- `UK_PERMS_TENANT_NAME` → `UK_PERMS_NAME (NAME)`

**JWT changes**: `tenant` claim removed from both access and refresh tokens. `JwtProperties.tenantClaim` removed. `JwtService.extractTenant()` removed.

**Service changes**: All `TenantHelper.requireTenant()` call sites removed (38 total across 7 services). All repository calls switched to non-tenant equivalents.

**DB migration**: `remove_tenant_id.sql` script at `erp-security/src/main/resources/db/scripts/` drops TENANT_ID columns and adds new single-column unique constraints. Must be run manually by DBA.
