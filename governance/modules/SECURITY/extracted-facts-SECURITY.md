# extracted-facts-SECURITY.md

Reverse-engineered from the ALREADY-IMPLEMENTED `erp-security` module
(`erp-security/src/main/java/com/example/security/...`) plus its migration
scripts (`erp-security/src/main/resources/db/scripts/*.sql`) and the shared
base classes it depends on in `erp-common-utils`. Every claim below traces to
a specific file read during this extraction. Nothing here was invented to
fill a gap — gaps are called out explicitly in each section's "Gaps /
Ambiguous" subsection.

**Module scope note (per the extraction agent's own instructions):** SECURITY
is governed in this ecosystem as an EXCEPTION module — consumed by other
modules via SOFT-READ/HARD-FK-style declarations, never re-implemented. This
file therefore documents SECURITY's boundary and public interface facts, and
does NOT attempt to retrofit it into the standard P0→P1→P2→P3.1 per-module
template used for ordinary domain modules. This matches what the code itself
shows: SECURITY is the one module every other backend module (ORG, FINANCE-GL,
MASTERDATA, and by inference FILESVC/NOTIFICATION) is expected to depend on
for auth/authorization, while SECURITY itself has no compile-time Maven
dependency on any of them — it reaches ORG and MASTERDATA only via same-JVM
internal HTTP calls (see LAYER 0).

---

## SECTION LAYER 0 — Platform/Architecture Facts

### Module boundary
- Package root: `com.example.security` (note: different base package than the
  Maven artifact/module name `erp-security` and the repo's other modules,
  which appear to use `com.example.erp.<module>` — `erp-common-utils` base
  package is `com.example.erp.common`. This module's package root omits
  `.erp`.)
- Maven artifact: `erp-security` (`erp-security/pom.xml`), packaging `jar`,
  parent `com.erp:erp-system`. Built as a library consumed by `erp-main`
  (modular monolith) — `spring-boot-maven-plugin` repackaging is explicitly
  `<skip>true</skip>` "used as a library in erp-main ... Do not repackage
  into an executable boot jar."
- Declared Maven dependencies (from `pom.xml`): `erp-common-utils`,
  `spring-boot-starter-web`, `spring-boot-starter-security`,
  `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`,
  `spring-boot-starter-cache`, `spring-boot-starter-data-redis`,
  `postgresql` (runtime), `bucket4j-core` (rate limiting), `jjwt-api` /
  `jjwt-impl` / `jjwt-jackson` (JWT), `lombok`, `slf4j-api`,
  `springdoc-openapi-starter-webmvc-ui`. Test-only: `spring-boot-starter-test`,
  `spring-security-test`.
- No Maven dependency on `erp-org` or `erp-masterdata` — confirmed by
  `pom.xml` dependency list AND explicitly stated in code comments in
  `OrgBranchClient.java` and `MasterDataLookupClient.java`.
- Classes/files scanned: 114 main `src/main/java` files — 1 application
  class, `dto/` 41, `entity/` 10, `service/` 10, `repository/` 9,
  `controller/` 8, `config/` 7 (+ 7 in `config/properties/`), `security/` 7,
  `mapper/` 5, `client/` 4, `event/` 2, `constants/` 1, `exception/` 1,
  `scheduler/` 1 — + 7 test files, plus 3 SQL migration scripts under
  `src/main/resources/db/scripts/`.

### Detected entities (candidate list — package/class names only, not
### yet confirmed as governed ENTITY-IDs)
- `UserAccount` → `entity/UserAccount.java` (table `USERS`)
- `Role` → `entity/Role.java` (table `ROLES`)
- `Permission` → `entity/Permission.java` (table `PERMISSIONS`)
- `Page` → `entity/Page.java` (table `SEC_PAGES`)
- `RefreshToken` → `entity/RefreshToken.java` (table `REFRESH_TOKENS`)
- `SecRoleBranch` → `entity/SecRoleBranch.java` (table `SEC_ROLE_BRANCH`,
  composite key, code comment tags it `ENTITY-SEC-010`)
- `SecRoleBranchId` → `entity/SecRoleBranchId.java` (`@IdClass` companion,
  not itself an entity)
- `SecUserProfile` → `entity/SecUserProfile.java` (table `SEC_USER_PROFILE`,
  code comment tags it `ENTITY-SEC-009`)
- `AccountActivationToken` → `entity/AccountActivationToken.java` (table
  `ACCOUNT_ACTIVATION_TOKEN`, code comment tags it `ENTITY-SEC-012`)
- `PasswordResetToken` → `entity/PasswordResetToken.java` (table
  `PASSWORD_RESET_TOKEN`, code comment tags it `ENTITY-SEC-011`)

Join tables (no `@Entity` class — pure `@JoinTable` mappings): `USER_ROLES`
(UserAccount↔Role), `ROLE_PERMISSIONS` (Role↔Permission).

Dropped/legacy table found only in migration history, not in code:
`SEC_MENU_ITEM` — `DROP TABLE IF EXISTS SEC_MENU_ITEM` in
`001_security_schema_migration_and_seed.sql`, with the comment "exists in
some environments' schema history but is never read or written by the
application — MenuService builds menus dynamically from SEC_PAGES +
permissions."

### Detected cross-module dependencies (found via imports/injected beans
### from other modules' packages)
- Depends on: **ORG** (specifically `ORG_BRANCH`) — evidence:
  `client/OrgBranchClient.java`, which calls
  `GET /api/v1/org/branches/{id}` over `RestTemplate` (same-JVM HTTP, not a
  Maven/JPA dependency, since erp-security has no compile-time dependency on
  erp-org). Migration script `002_datascope_selfservice_auth_schema.sql`
  BLOCK 5e adds real DB-level FKs `FK_SEC_USER_PROFILE_BRANCH` and
  `FK_SEC_ROLE_BRANCH_BRANCH` referencing `ORG_BRANCH(BRANCH_PK)`, tagged
  `XM-SEC-001` / `XM-SEC-002` in code comments, both noted as "READY, target
  GOVERNED" in the SQL header.
  - Looks like: **HARD-FK** — the DB migration script literally adds FK
    constraints to `ORG_BRANCH`, even though there's no JPA object-graph
    association (deliberately, per `SecUserProfile.java`'s javadoc: "no JPA
    association to OrgBranch: erp-security has no Maven dependency on
    erp-org"). So structurally this is DB-HARD-FK + code-level SOFT-READ
    (validated via REST call, not JPA).
- Depends on: **MASTERDATA** (specifically `MD_MASTER_LOOKUP` /
  `MD_LOOKUP_DETAIL`) — evidence: `client/MasterDataLookupClient.java`,
  which calls `GET /api/lookups/{lookupCode}` to validate the
  `DATA_ACCESS_LEVEL` LOV. Migration script BLOCK 8 seeds
  `MD_MASTER_LOOKUP`/`MD_LOOKUP_DETAIL` rows directly (shared system tables,
  "never recreated here — seed INSERTs only").
  - Looks like: **SOFT-READ** — no FK constraint to these tables exists;
    validation happens entirely via the internal REST call at the Service
    layer.
- No detected dependency on **FILESVC** or **NOTIFICATION** modules in code.
  Password-reset/account-activation flows publish Spring
  `ApplicationEvent`s (`AccountActivationRequestedEvent`,
  `PasswordResetRequestedEvent` in `event/`) rather than calling a
  notification service directly — `AuthService.java` comments this as
  "RULE-SEC-031 — publish event instead of calling NotificationService
  directly." No listener/consumer of these events was found inside
  `erp-security` itself (consistent with the intent that some other module,
  presumably NOTIFICATION, listens for them — not confirmed from this
  module's code alone).
- Internal self-call pattern: both `OrgBranchClient` and
  `MasterDataLookupClient` call `http://localhost:${server.port:7272}/...`
  and forward the caller's own inbound `Authorization` header verbatim — code
  comments in both classes flag this as a deliberate stopgap ("no
  service-to-service credential exists in this codebase yet").

### Detected LOV/lookup candidates (enums, or foreign-key-to-lookup-table
### patterns)
- `PermissionType` (Java enum: `VIEW`, `CREATE`, `UPDATE`, `DELETE`) →
  `dto/PermissionType.java`. Not a DB lookup table — persisted directly as
  `PERMISSIONS.PERMISSION_TYPE` via `@Enumerated(EnumType.STRING)`.
- `DATA_ACCESS_LEVEL` (code comments tag it `LOV-SEC-002`) → backed by
  `MD_MASTER_LOOKUP`/`MD_LOOKUP_DETAIL` (MASTERDATA module tables, seeded by
  this module's own migration script). Valid codes found in seed data:
  `BRANCH_ONLY`, `BRANCH_AND_CHILDREN`, `ALL`. Stored as a plain
  `VARCHAR(30)` on `SEC_ROLE_BRANCH.DATA_ACCESS_LEVEL` — validated at the
  Service layer via `MasterDataLookupClient`, not a DB check constraint.
- `PREFERRED_LANG` on `SecUserProfile` — plain nullable `VARCHAR(10)`,
  explicit code comment: "OQ-004 — no LOV domain governed yet for preferred
  language."

### Ambiguous / could not determine
- Whether FILESVC or NOTIFICATION actually listens for
  `AccountActivationRequestedEvent` / `PasswordResetRequestedEvent` cannot be
  determined from `erp-security`'s code alone (the listener, if any, lives
  outside this module).
- Package-root naming (`com.example.security` vs. the `com.example.erp.*`
  convention seen in `erp-common-utils`) is inconsistent with what the
  parent CLAUDE.md implies about naming; not explained by any in-code
  comment found.
- `erp-security`'s own base package omits `erp.` — a real P0 session should
  confirm whether this is intentional or historical drift.

---

## SECTION LAYER 0.5 — Product Intent Facts

### Inferred user stories (every story traces to a real controller method)
- "As an unauthenticated user, I need to log in with a username/password and
  receive a JWT access token (plus a refresh-token cookie)" — Source:
  `AuthController.login()` at `controller/AuthController.java:36`
- "As a client app, I need a single login call that also returns my full
  user info (roles, permissions) instead of just a token" — Source:
  `AuthController.loginWithToken()` at
  `controller/AuthController.java:47`
- "As a logged-in user, I need to refresh my access token using my refresh
  cookie without re-entering credentials" — Source:
  `AuthController.refresh()` at `controller/AuthController.java:57`
- "As a logged-in user, I need to log out and have my tokens invalidated" —
  Source: `AuthController.logout()` at `controller/AuthController.java:68`
- "As a prospective user, I need to self-register an account that starts
  disabled until I confirm it" — Source: `AuthController.signup()` at
  `controller/AuthController.java:78`, description explicitly cites
  "RULE-SEC-030, RULE-SEC-040, RULE-SEC-041"
- "As a self-registered user, I need to activate my account via an emailed
  token link" — Source: `AuthController.activate()` at
  `controller/AuthController.java:87`
- "As a user who forgot my password, I need to request a reset without
  revealing whether my email exists in the system" — Source:
  `AuthController.forgotPassword()` at `controller/AuthController.java:97`,
  description explicitly cites "anti-enumeration"
- "As a user with a valid reset token, I need to set a new password" —
  Source: `AuthController.resetPassword()` at
  `controller/AuthController.java:107`
- "As an administrator, I need to create, list, search, update, and delete
  user accounts and assign roles to them" — Source: `UserController`
  (`create`, `all`, `search`, `assignRoles`, `getUserRoles`, `delete`,
  `update`) at `controller/UserController.java`
- "As an administrator, I need to create, search, update, and delete roles,
  and toggle their active status" — Source: `RoleController`
  (`createRole`..`toggleRoleActive`) at `controller/RoleController.java`
- "As an administrator, I need to assign UI pages to a role with specific
  CRUD permissions, view a role's page/permission matrix, remove a page from
  a role, and copy one role's page permissions onto another" — Source:
  `RoleController` (`getRolePages`, `addPageToRole`, `syncRolePages`,
  `removePageFromRole`, `copyFromRole`) at
  `controller/RoleController.java:143-218`
- "As an administrator, I need to register new UI pages/screens (which
  auto-generates their 4 CRUD permission records), and update, deactivate,
  or reactivate them" — Source: `PageController`
  (`createPage`..`reactivatePage`) at `controller/PageController.java`
- "As an administrator, I need to create/search/update raw permission
  records directly" — Source: `PermissionController` at
  `controller/PermissionController.java`
- "As a logged-in user, I need a menu tree built from only the pages I have
  VIEW permission for" — Source: `MenuController.getUserMenu()` at
  `controller/MenuController.java:43`
- "As an administrator, I need to view any specific user's menu for
  debugging permission issues" — Source:
  `MenuController.getUserMenuById()` at `controller/MenuController.java:59`
- "As an administrator, I need to scope a role's data access to specific
  branches with a data-access level (branch-only / branch-and-children /
  all)" — Source: `SecRoleBranchController` (`create`..`delete`) at
  `controller/SecRoleBranchController.java`, description cites
  "RULE-SEC-035, RULE-SEC-036"
- "As an administrator, I need to attach a branch/profile (full name,
  preferred language, employee link) to a user account" — Source:
  `SecUserProfileController` (`create`..`update`) at
  `controller/SecUserProfileController.java`, description cites
  "RULE-SEC-034"

### Inferred priorities
- NONE — code does not encode business priority; requires human input.

### Explicit caveat (always include verbatim):
This is a REVERSE-inferred product-intent section. It describes what
the code currently DOES, not what was originally INTENDED. Treat every
"user story" here as a hypothesis to confirm with a human familiar with
the module's original business requirements — not as a confirmed PRD.

---

## SECTION LAYER 1 — Functional/SRS Facts

### Entities found

#### UserAccount → candidate ENTITY-ID: ENTITY-SEC-[N] (provisional)
  Fields:
    `id` : Long → PK, `IDENTITY` generation, column `USERS_PK`
    `username` : String → not null, length 80, unique (`UK_USERS_USERNAME`)
    `email` : String → nullable, length 150, unique (`UK_USERS_EMAIL`,
      added in migration 002). Code comment tags field `FIELD-SEC-0032`.
    `password` : String → not null, length 200 (BCrypt hash storage)
    `enabled` : Boolean → not null, default `TRUE`, converted via
      `BooleanNumberConverter` (DB stores NUMBER(1)/SMALLINT 0/1)
  Relationships:
    `roles` : Set<Role> → `@ManyToMany` via join table `USER_ROLES`
      (`USER_ID_FK`→`USERS_PK`, `ROLE_ID_FK`→`ROLES_PK`), lazy fetch

#### Role → candidate ENTITY-ID: ENTITY-SEC-[N] (provisional)
  Governance tag in code: "Governance: BE-REQ-ROLEACCESS-001, Contract:
  role-access.contract.md."
  Fields:
    `id` : Long → PK, `IDENTITY`, column `ROLES_PK`
    `roleName` (mapped from column `NAME`) : String → not null, length 60,
      unique (`UK_ROLES_NAME`)
    `roleCode` : String → not null, length 60, unique
      (`UK_ROLES_ROLE_CODE`) — added by migration 001 STEP 3 (previously
      `@Transient`, silently discarded before that)
    `description` : String → nullable, length 500 — also newly persisted by
      migration 001 STEP 3
    `active` (mapped from column `IS_ACTIVE`) : Boolean → not null, default
      `TRUE`, `BooleanNumberConverter`. Getter deliberately NOT named
      `isActive()` — code comment: "to avoid Hibernate interpreting it as a
      boolean property accessor and creating a phantom 'ACTIVE' column
      mapping."
  Relationships:
    `permissions` : Set<Permission> → `@ManyToMany` via join table
      `ROLE_PERMISSIONS` (`ROLE_ID_FK`→`ROLES_PK`,
      `PERM_ID_FK`→`PERMISSIONS_PK`), lazy fetch, `@JsonIgnore`
  Deprecated compatibility: `getName()`/`setName()` alias `roleName` — code
  comment: "Legacy compatibility - maps to roleCode for existing code" (the
  comment's own wording is inconsistent with the implementation, which
  actually aliases `roleName`, not `roleCode` — flagged as-is, not resolved).

#### Permission → candidate ENTITY-ID: ENTITY-SEC-[N] (provisional)
  Fields:
    `id` : Long → PK, `IDENTITY`, column `PERMISSIONS_PK`
    `name` : String → not null, length 150, unique (`UK_PERMS_NAME`).
      Convention documented in code: `PERM_<PAGE_CODE>_<TYPE>`
    `permissionType` : PermissionType enum (`VIEW`/`CREATE`/`UPDATE`/
      `DELETE`) → `@Enumerated(STRING)`, column `PERMISSION_TYPE`,
      length 20, nullable (null for system permissions)
  Relationships:
    `page` : Page → `@ManyToOne` lazy, column `PAGE_ID_FK`, nullable ("System
      permissions (not linked to pages) have PAGE_ID_FK = null")

#### Page → candidate ENTITY-ID: ENTITY-SEC-[N] (provisional)
  Table `SEC_PAGES`. Code comment: "Pages are the DETAIL in the RBAC model,
  with Roles as MASTER. ... Each Page auto-generates 4 CRUD permissions."
  Fields:
    `id` : Long → PK, `SEQUENCE` (`SEC_PAGES_SEQ`), column `SEC_PAGES_PK`
    `pageCode` : String → not null, length 50, unique (`UK_PAGES_CODE`),
      uppercased + trimmed in `@PrePersist`/`@PreUpdate`
    `nameAr` : String → not null, length 100
    `nameEn` : String → not null, length 100
    `route` : String → not null, length 200, unique (`UK_PAGES_ROUTE`)
    `icon` : String → nullable, length 50
    `module` : String → nullable, length 50 (e.g. SECURITY, FINANCE)
    `parentId` : Long → nullable, plain scalar (no `@JoinColumn`/FK) for
      hierarchical structure
    `displayOrder` : Integer → nullable
    `active` : Boolean → nullable-column-but-code-defaulted `true`,
      `BooleanNumberConverter`
    `description` : String → nullable, length 500

#### SecRoleBranch → ENTITY-SEC-010 (per code comment)
  Table `SEC_ROLE_BRANCH`, composite PK via `@IdClass(SecRoleBranchId)`.
  Fields:
    `roleIdFk` : Long → PK part 1, FK to `ROLES.ROLES_PK`
    `branchIdFk` : Long → PK part 2, FK to `ORG_BRANCH.BRANCH_PK`
      (cross-module)
    `dataAccessLevel` : String → not null, length 30. Code comment: "LOV-SEC-002
      — validated against MD_LOOKUP_DETAIL codes at the Service layer ...;
      not enforced by a DB check constraint here."
    `isActiveFl` : Boolean → not null, default `TRUE`, `BooleanNumberConverter`
  No navigable JPA association to `Role`/`OrgBranch` — code comment:
  "no existing composite-key precedent in this module to follow otherwise,
  and RI is enforced at the DB level."

#### SecUserProfile → ENTITY-SEC-009 (per code comment)
  Table `SEC_USER_PROFILE`. 1:1 extension of `UserAccount` via shared PK
  (`@MapsId`).
  Fields:
    `userIdFk` : Long → PK, also FK to `USERS.USERS_PK` (shared PK, not a
      surrogate)
    `branchIdFk` : Long → not null, FK to `ORG_BRANCH.BRANCH_PK`
      (cross-module, enforced at DB layer only — "no JPA association to
      OrgBranch")
    `fullNameAr` : String → nullable, length 200
    `fullNameEn` : String → nullable, length 100
    `preferredLang` : String → nullable, length 10 (no governed LOV yet —
      OQ-004)
    `employeeIdFk` : Long → nullable, unconstrained (no HR module governed
      yet — OQ-005)
    `isActiveFl` : Boolean → not null, default `TRUE`, `BooleanNumberConverter`
  Relationships:
    `user` : UserAccount → `@OneToOne` lazy, `@MapsId`
  Implements `Persistable<Long>` — code comment explains this is required
  because `userIdFk` is manually assigned (never null even when new),
  otherwise Spring Data's `isNew()` misfires and calls `merge()` instead of
  `persist()`, throwing a Hibernate `AssertionFailure`.

#### RefreshToken (not `AuditableEntity` — plain audit fields)
  Table `REFRESH_TOKENS`.
  Fields:
    `id` : Long → PK, `IDENTITY`, column `REFRESH_TOKENS_PK`
    `jti` : String → not null, unique, length 64
    `createdAt` : Instant → `@CreationTimestamp`, not null, not updatable
    `expiresAt` : Instant → not null
    `revoked` : Boolean → not null, default `FALSE`, `BooleanNumberConverter`
  Relationships:
    `user` : UserAccount → `@ManyToOne` lazy, not null, column `USER_ID_FK`

#### AccountActivationToken → ENTITY-SEC-012 (per code comment)
  Table `ACCOUNT_ACTIVATION_TOKEN`. Not `AuditableEntity` — code comment:
  "modeled on RefreshToken's plain-audit-field style ... consistent with
  db-script-SEC-gaps.md BLOCK 3 giving this table only CREATED_AT/
  EXPIRES_AT, no CREATED_BY/UPDATED_AT/UPDATED_BY."
  Fields:
    `tokenPk` : Long → PK, `SEQUENCE` (`ACCOUNT_ACTIVATION_TOKEN_SEQ`)
    `token` : String → not null, length 64, unique
    `createdAt` : Instant → `@CreationTimestamp`, not null
    `expiresAt` : Instant → not null
    `usedFl` : Boolean → not null, default `FALSE`, `BooleanNumberConverter`
  Relationships:
    `user` : UserAccount → `@ManyToOne` lazy, not null

#### PasswordResetToken → ENTITY-SEC-011 (per code comment)
  Table `PASSWORD_RESET_TOKEN`. Same plain-audit-field style as
  `AccountActivationToken`. Field shape identical (`tokenPk`, `token`,
  `createdAt`, `expiresAt`, `usedFl`), FK to `UserAccount`.

### Business rules found
- RULE-SEC-030: self-registered accounts MUST start disabled
  (`enabled=false`). Evidence: `AuthService.signup()`,
  `service/AuthService.java:324`. Enforced at: Service layer.
- RULE-SEC-031: password-reset/account-activation notifications are
  triggered via published `ApplicationEvent`s, not a direct
  `NotificationService` call. Evidence: `AuthService.signup()` line 337-338,
  `AuthService.forgotPassword()` line 399. Enforced at: Service layer
  (architectural convention, not a runtime guard).
- RULE-SEC-032: account activation / password reset requires a valid,
  unused, non-expired token. Evidence: `AuthService.activateAccount()` lines
  356-361, `AuthService.resetPassword()` lines 419-424. Enforced at: Service
  layer.
- RULE-SEC-033: activation/reset tokens are marked used immediately on
  success and rejected if already used. Evidence: same methods as above,
  `token.setUsed(true)` calls. Enforced at: Service layer.
- RULE-SEC-034: `SEC_USER_PROFILE.branchIdFk` must reference an existing,
  active `ORG_BRANCH` row (validated via cross-module HTTP call, not JPA).
  Evidence: `SecUserProfileService.create()`/`update()` calling
  `orgBranchClient.assertActiveBranch()`, `client/OrgBranchClient.java:46`.
  Enforced at: Service layer (via `OrgBranchClient`).
- RULE-SEC-035: `SEC_ROLE_BRANCH.dataAccessLevel` is required and must be a
  valid, active `LOV-SEC-002` code. Evidence:
  `SecRoleBranchService.assertValidDataAccessLevel()`,
  `service/SecRoleBranchService.java:143-148`; blank check throws
  `SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED` before even calling the
  lookup client. Enforced at: Service layer.
- RULE-SEC-036: no duplicate `(roleIdFk, branchIdFk)` assignment in
  `SEC_ROLE_BRANCH`. Evidence:
  `SecRoleBranchService.create()` line 68 (`existsByRoleIdFkAndBranchIdFk`
  check) AND the composite PK itself (DB-level enforcement per
  `SecRoleBranchId.java` javadoc). Enforced at: Service layer (pre-check) +
  DB layer (composite PK, belt-and-suspenders).
- RULE-SEC-037: JWT `allowedBranches[]` claim is derived from the user's
  active `SEC_ROLE_BRANCH` assignments across their active roles, with an
  `"ALL"` sentinel collapsing an unbounded branch list into one element.
  Evidence: `AuthService.resolveAllowedBranches()`,
  `service/AuthService.java:225-244`.
- RULE-SEC-038 (anti-enumeration): `forgotPassword` always responds
  identically regardless of whether the email exists. Evidence:
  `AuthService.forgotPassword()` lines 384-404, code comment: "response is
  identical whether or not the email existed; nothing beyond this point may
  branch on the Optional above."
- RULE-SEC-039: issuing a new password-reset token invalidates any prior
  unexpired token for the same user. Evidence:
  `AuthService.forgotPassword()` lines 385-388.
- RULE-SEC-040/041: signup username/email must be globally unique. Evidence:
  `AuthService.signup()` lines 313-317
  (`SIGNUP_USERNAME_ALREADY_EXISTS`/`SIGNUP_EMAIL_ALREADY_EXISTS`),
  `UserAccountRepository.existsByEmailIgnoreCase()`.
- Role-page RBAC rules (governance tag "BE-REQ-ROLEACCESS-001"), all
  enforced in `RoleAccessService.java`:
  - VIEW permission is ALWAYS auto-added when a Page is assigned to a Role
    (lines 112-117, 169).
  - VIEW is not independently removable — removing a page removes VIEW +
    all CRUD together (lines 265-274); attempting to add a page already
    assigned (has VIEW) throws `PAGE_ALREADY_ASSIGNED_TO_ROLE` (lines 94-100).
  - CRUD permission values in requests are restricted to
    `CREATE`/`UPDATE`/`DELETE` only — anything else throws
    `INVALID_PERMISSION_TYPE` (lines 104-108, 172-176).
  - `syncRolePages` is a full-replace of a role's page-scoped permissions;
    system-level permissions (no page FK) are left untouched (lines 150-213).
  - `copyPermissionsFromRole` copies only page-scoped permissions
    (`PAGE_ID_FK IS NOT NULL`); a role's system-level permissions (e.g.
    `PERM_SYSTEM_ADMIN`) are never overwritten by a copy; copying from a
    role with zero page-scoped permissions throws `NO_PERMISSIONS_TO_COPY`;
    self-copy (same role ID both sides) throws `INVALID_OPERATION` (lines
    295-348).
- Page rules, enforced in `PageService.java`:
  - `pageCode` must match `^[A-Z0-9_]+$` and be 2-50 chars
    (`createPage()` lines 106-113).
  - `route` must start with `/` and match `^/[a-zA-Z0-9/_-]+$`
    (`validateRouteFormat()` lines 463-469).
  - `pageCode` and `route` must each be unique (lines 116-127).
  - A page's `parentId`, if given, must reference an existing page, and on
    update cannot equal the page's own ID (self-reference guard, lines
    186-193).
  - Every Page creation auto-generates exactly 4 Permission records
    (`VIEW`/`CREATE`/`UPDATE`/`DELETE`) named `PERM_<PAGE_CODE>_<TYPE>`
    (`createPermissionRecords()` lines 383-418) — this is a general rule,
    but code comment in migration script `003_sec_pages_permissions_seed.sql`
    flags a documented GAP: for `SCR-SEC-006` (User Profile page) the
    business requirement was only 3 permissions (no DELETE — profiles
    deactivate via `isActiveFl`/UPDATE, never DELETE), and this had to be
    worked around with direct SQL because `PageService` has no flag to
    suppress DELETE generation.
- Role rules, enforced in `RoleService.java`:
  - `roleCode` and `roleName` must each be unique (`createRole()` lines
    73-83).
  - `roleCode` is immutable after creation — `updateRole()` never touches it
    (lines 156-174, explicit code comment).
  - A role with existing user assignments cannot be deleted — `deleteRole()`
    line 194 (`roleRepo.hasUserAssignments(id)`) throws `ROLE_IN_USE`
    (409 Conflict).
- User rules, enforced in `UserService.java`:
  - `username` must be unique (case-insensitive) both on create (line 62)
    and update (lines 235-241).
  - A user with active refresh tokens cannot be deleted — `deleteUser()`
    lines 200-207 (`refreshTokenRepo.countByUser_Id`) throws
    `USER_HAS_ACTIVE_REFRESH_TOKENS` (409 Conflict).
  - New users created via `POST /api/users` are auto-assigned the
    `ROLE_USER` role if it exists (`createUser()` lines 72-74) — silently
    skipped if that role is absent (no error thrown).
- Login rate limiting: an in-memory bucket keyed by `ip|username` blocks
  further attempts after a configured max, with a configured lockout window
  — `security/LoginRateLimiterService.java`. Explicit code comment flags
  this as NOT safe for horizontal scaling ("must move to Redis ... if the
  backend is ever horizontally scaled").
- Refresh-token rotation: every `login`/`refresh` call revokes the prior
  refresh token and issues a new JTI — `AuthService.refresh()` lines
  134-136.
- Scheduled cleanup deletes (a) all expired refresh tokens regardless of
  revoked status, and (b) revoked refresh tokens older than a configured
  retention window (using `CREATED_AT` as the age reference, since there's
  no separate "revoked at" column) — `scheduler/RefreshTokenCleanupJob.java`.

### APIs found

#### POST /api/auth/login → AuthController.login()
  Request DTO: `AuthRequest` (username, password)
  Response DTO: `AuthResponse` (access token, 900s expiry hardcoded in
    controller — not read from `JwtProperties` at this call site)
  Auth required: none (public — `/api/auth/**` permitAll in
    `SecurityConfig`)
  Errors handled: authentication failure surfaces via Spring Security's
    `AuthenticationManager` (not a custom `LocalizedException` at this call
    site); `USER_NOT_FOUND` if the authenticated principal's entity lookup
    fails post-auth.

#### POST /api/auth/login-token → AuthController.loginWithToken()
  Response DTO: `UserInfo` (access + refresh tokens, expiries, full
    `UserDto`)
  Auth required: none

#### POST /api/auth/refresh → AuthController.refresh()
  Response DTO: `AuthResponse`
  Auth required: none (reads refresh cookie)
  Errors handled: `NO_REFRESH_COOKIE`, `REFRESH_REVOKED`,
    `REFRESH_EXPIRED_OR_REVOKED`, `USER_NOT_FOUND`

#### POST /api/auth/logout → AuthController.logout()
  Response: 204 No Content
  Auth required: none; explicitly idempotent — code comment: "even if the
    refresh cookie is missing/expired, we still clear client-side session
    cookies and return success."

#### POST /api/auth/signup → AuthController.signup()
  Request DTO: `SignupRequest` (username, email, password — bean-validated:
    `@NotBlank`, `@Size(3,80)` / `@Email` + `@Size(max=150)` /
    `@Size(6,120)`)
  Response DTO: `SignupResponse`
  Auth required: none
  Errors handled: `SIGNUP_USERNAME_ALREADY_EXISTS`,
    `SIGNUP_EMAIL_ALREADY_EXISTS`

#### POST /api/auth/signup/activate → AuthController.activate()
  Request DTO: `ActivateAccountRequest`
  Errors handled: `ACTIVATION_TOKEN_INVALID_OR_EXPIRED`, `TOKEN_ALREADY_USED`

#### POST /api/auth/forgot-password → AuthController.forgotPassword()
  Request DTO: `ForgotPasswordRequest`
  Response: always 200 OK regardless of outcome (anti-enumeration)

#### POST /api/auth/reset-password → AuthController.resetPassword()
  Request DTO: `ResetPasswordRequest`
  Errors handled: `RESET_TOKEN_INVALID_OR_EXPIRED`, `TOKEN_ALREADY_USED`

#### POST /api/users → UserController.create()
  Request DTO: `CreateUserRequest` (username `@Size(3,80)`, password
    `@Size(6,120)`, both `@NotBlank`)
  Response DTO: `UserDto`
  Auth required: `PERM_USER_CREATE` (enforced at Service layer via
    `@PreAuthorize`, not on the controller — see LAYER 3.1)
  Errors handled: `USERNAME_ALREADY_EXISTS`

#### GET /api/users → UserController.all()
  Response: `Page<UserDto>`. Allowed sort fields (code-enforced whitelist):
    `id, username, enabled, createdAt`
  Auth required: `PERM_USER_VIEW`

#### POST /api/users/search → UserController.search()
  Dynamic filter/sort/paginate. Same allowed-field whitelist as above.
  Auth required: `PERM_USER_VIEW`

#### PUT /api/users/{userId}/roles → UserController.assignRoles()
  Full-replace role assignment. Auth required: `PERM_USER_UPDATE` (constant
  `USER_MANAGE_ROLES` is literally aliased to the same string as
  `USER_UPDATE` in `SecurityPermissions.java` — not a distinct permission).
  Errors handled: `USER_NOT_FOUND`, `ROLE_NOT_FOUND`

#### GET /api/users/{userId}/roles → UserController.getUserRoles()
  Auth required: `PERM_USER_VIEW`

#### DELETE /api/users/{userId} → UserController.delete()
  Response: 204 No Content. Auth required: `PERM_USER_DELETE`
  Errors handled: `USER_NOT_FOUND`, `USER_HAS_ACTIVE_REFRESH_TOKENS` (409)

#### PUT /api/users/{userId} → UserController.update()
  All fields optional (partial update). Auth required: `PERM_USER_UPDATE`
  Errors handled: `USER_NOT_FOUND`, `USERNAME_ALREADY_EXISTS`,
    `ROLE_NOT_FOUND`

#### Role endpoints (`RoleController`, base `/api/roles`)
  `POST /` createRole — `PERM_ROLE_CREATE`, errors: `DUPLICATE_ROLE_CODE`,
    `DUPLICATE_ROLE_NAME`
  `POST /search` searchRoles — `PERM_ROLE_VIEW`, allowed filter field:
    `roleName`; allowed sort fields: `id, roleName`
  `GET /{roleId}` getRoleById — `PERM_ROLE_VIEW`, errors: `ROLE_NOT_FOUND`
  `PUT /{roleId}` updateRole — `PERM_ROLE_UPDATE` (roleCode immutable),
    errors: `ROLE_NOT_FOUND`, `DUPLICATE_ROLE_NAME`
  `DELETE /{roleId}` deleteRole — `PERM_ROLE_DELETE`, 204, errors:
    `ROLE_NOT_FOUND`, `ROLE_IN_USE` (409)
  `PUT /{roleId}/toggle-active` toggleRoleActive — `PERM_ROLE_UPDATE`
  `GET /{roleId}/pages` getRolePages — `PERM_ROLE_VIEW`
  `POST /{roleId}/pages` addPageToRole — `PERM_ROLE_UPDATE`, errors:
    `ROLE_NOT_FOUND`, `PAGE_NOT_FOUND_BY_CODE`, `PAGE_ALREADY_ASSIGNED_TO_ROLE`,
    `INVALID_PERMISSION_TYPE`, `PERMISSIONS_NOT_FOUND`
  `PUT /{roleId}/pages` syncRolePages (full replace) — `PERM_ROLE_UPDATE`
  `DELETE /{roleId}/pages/{pageCode}` removePageFromRole —
    `PERM_ROLE_UPDATE`, 204, errors: `PAGE_NOT_ASSIGNED_TO_ROLE`
  `POST /{roleId}/copy-from/{sourceRoleId}` copyFromRole —
    `PERM_ROLE_UPDATE`, errors: `INVALID_OPERATION`, `NO_PERMISSIONS_TO_COPY`

#### Permission endpoints (`PermissionController`, base `/api/permissions`)
  `POST /` create — `@PreAuthorize` **on the controller method itself**
    (`PERMISSION_CREATE`) — the one controller in this module that deviates
    from the "controllers never carry @PreAuthorize" convention documented
    elsewhere (see LAYER 3.1 Gaps).
  `POST /search` searchPermissions — controller-level `@PreAuthorize`
    (`PERMISSION_VIEW`). Allowed filter fields: `name, module`; sort:
    `id, name, module, createdAt, updatedAt`
  `PUT /{id}` update — no `@PreAuthorize` found on this method (neither
    controller nor, per `PermissionService.updatePermission()`, is one
    visible in the excerpt read — flagged as a possible gap, not confirmed
    absent since only the controller was fully read for this endpoint).
    Errors: `PERMISSION_NOT_FOUND`, `PERMISSION_ALREADY_EXISTS`

#### Page endpoints (`PageController`, base `/api/pages`)
  `POST /` createPage — no `@PreAuthorize` visible on the controller method
    (flagged — every other write endpoint in this controller has one; this
    is the odd one out. `PageService.createPage()` itself does carry
    `@PreAuthorize(PAGE_CREATE)`, so it is still enforced, just not visible
    at the controller.)
  `POST /search` searchPages — `PAGE_VIEW`
  `GET /active` getActivePages — `PAGE_VIEW`
  `GET /{id}` getPageById — `PAGE_VIEW`
  `PUT /{id}` updatePage — no `@PreAuthorize` on controller (same pattern as
    createPage; `PageService.updatePage()` does have `@PreAuthorize(PAGE_UPDATE)`)
  `PUT /{id}/deactivate` deactivatePage — `PAGE_DELETE` (controller-level)
  `PUT /{id}/reactivate` reactivatePage — `PAGE_UPDATE` (controller-level)

#### Menu endpoints (`MenuController`, base `/api/menu`)
  `GET /user-menu` getUserMenu — no `@PreAuthorize` (any authenticated user;
    JWT filter chain requires authentication globally except `/api/auth/**`)
  `GET /user-menu/{userId}` getUserMenuById — `@PreAuthorize` on controller,
    `USER_VIEW`

#### SecRoleBranch endpoints (`SecRoleBranchController`, base
  `/api/v1/security/role-branches`) — thin controller, NO `@PreAuthorize`
  anywhere on the controller by design (code comment: "controllers never
  carry @PreAuthorize" — A.5.2 service-contract convention). All 5 CRUD +
  search operations gated inside `SecRoleBranchService` reusing existing
  `PERM_ROLE_*` permissions (no new permission set introduced for this
  sub-resource).
  `POST /` create — errors: `ROLE_NOT_FOUND`,
    `SEC_ROLE_BRANCH_DUPLICATE_ASSIGNMENT` (409),
    `SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED`
  `GET /` list (paginated)
  `POST /search` search
  `GET /{roleId}/{branchId}` getById — errors: `SEC_ROLE_BRANCH_NOT_FOUND`
  `PUT /{roleId}/{branchId}` update
  `DELETE /{roleId}/{branchId}` delete — 204

#### SecUserProfile endpoints (`SecUserProfileController`, base
  `/api/v1/security/user-profiles`) — same thin-controller pattern, all
  logic + `@PreAuthorize` in `SecUserProfileService`, reusing dedicated
  `PERM_USER_PROFILE_*` permissions (VIEW/CREATE/UPDATE only — deliberately
  no DELETE permission constant, code comment: "profiles deactivate via
  isActiveFl through UPDATE, never DELETE").
  `POST /` create — errors: `SEC_USER_PROFILE_ALREADY_EXISTS`,
    `USER_NOT_FOUND`, `SEC_USER_PROFILE_BRANCH_INACTIVE`
  `GET /` list
  `POST /search` search
  `GET /{userId}` getById — errors: `SEC_USER_PROFILE_NOT_FOUND`
  `PUT /{userId}` update

### Status lifecycle found (if any)
- `UserAccount.enabled` (Boolean, no dedicated enum): `false` (signup) →
  `true` (activation, `AuthService.activateAccount()`); also directly
  settable by an admin via `UpdateUserRequest.enabled()`. No other states.
- `Role.active` / `Page.active` / `SecRoleBranch.isActiveFl` /
  `SecUserProfile.isActiveFl`: simple Boolean active/inactive toggles, no
  intermediate states — transitions are direct `true ↔ false` flips via
  dedicated activate()/deactivate() entity methods or service methods
  (`RoleService.toggleRoleActive()`, `PageService.deactivatePage()`/
  `reactivatePage()`), no guard conditions found beyond existence checks.
  Exception: `Role.active` is documented in `RoleService.toggleRoleActive()`
  as currently NOT actually persisting reliably — code comment: "Currently
  the 'active' field is @Transient (not persisted) ... After DB migration
  (adding IS_ACTIVE column), remove this note" — but the `Role` entity DOES
  now map `IS_ACTIVE` to a real column (per migration 001 STEP 2/`Role.java`
  `@Column(name="IS_ACTIVE")`), so this comment/TODO appears STALE relative
  to the current entity mapping. Flagged as a doc/code mismatch, not
  resolved here.
- `AccountActivationToken.usedFl` / `PasswordResetToken.usedFl`: `false` →
  `true`, one-way, single-use, set on successful consumption.
- `RefreshToken.revoked`: `false` → `true`, one-way, set on logout or on
  rotation (a new refresh issued at `/refresh` revokes the old one).

### Permissions found
All `PERM_*` constants live in `constants/SecurityPermissions.java`. Ones
actually referenced by `@PreAuthorize` in this module's own controllers/
services (module-relevant subset — the constants file also defines many
permissions for OTHER modules, e.g. `GL_*`, `MASTER_LOOKUP_*`,
`LEGAL_ENTITY_*`, evidently centralized here for reuse across the whole
`erp-main` assembly):
- `PERM_USER_VIEW` / `_CREATE` / `_UPDATE` / `_DELETE` → `UserController`/
  `UserService` endpoints. `PERM_USER_MANAGE_ROLES` constant literally
  equals `PERM_USER_UPDATE` (not a distinct value).
- `PERM_ROLE_VIEW` / `_CREATE` / `_UPDATE` / `_DELETE` → `RoleController`/
  `RoleService`/`RoleAccessService`, and reused as-is for
  `SecRoleBranchController`'s endpoints (no new permission set).
- `PERM_PERMISSION_VIEW` / `_CREATE` / `_UPDATE` / `_DELETE` →
  `PermissionController`/`PermissionService` (`_DELETE` constant exists but
  no delete endpoint was found using it).
- `PERM_PAGE_VIEW` / `_CREATE` / `_UPDATE` / `_DELETE` → `PageController`/
  `PageService`.
- `PERM_MENU_VIEW` / `_CREATE` / `_UPDATE` / `_DELETE` → defined but NOT
  referenced by any `@PreAuthorize` found in `MenuController`/`MenuService`
  during this extraction (flagged as a gap below).
- `PERM_USER_PROFILE_VIEW` / `_CREATE` / `_UPDATE` (no `_DELETE` constant —
  deliberate, per code comment "SCR-SEC-006 — no DELETE constant") →
  `SecUserProfileController`/`SecUserProfileService`.
- `PERM_SYSTEM_ADMIN` → seeded as a system-level (non-page) permission
  record; not observed being checked by any `@PreAuthorize` in this module's
  code (likely a superuser catch-all consumed elsewhere or just granted to
  `SUPER_ADMIN`).

### Gaps / things a real SRS needs that code cannot answer
- `PageController.createPage()`/`updatePage()` have no visible
  `@PreAuthorize` at the controller layer (unlike every sibling endpoint in
  the same controller) — enforcement still exists at the Service layer
  (`PageService`), so this is a consistency/readability gap, not a security
  hole, but a real SRS reconciliation should confirm this is intentional.
- `PermissionController.update()` — no `@PreAuthorize` visible on the
  controller method (the create/search siblings do have it); whether
  `PermissionService.updatePermission()` enforces it was not independently
  confirmed in this pass (the method body shown had no `@PreAuthorize`
  either) — potential authorization gap, needs verification.
- `PERM_MENU_*` constants are defined but appear unused by
  `MenuController`/`MenuService` — either dead permission constants or
  enforcement lives somewhere not found in this pass.
- `Role.getName()`/`setName()` deprecated-alias javadoc text ("maps to
  roleCode") doesn't match its actual implementation (aliases `roleName`) —
  a real SRS write-up should not propagate this doc error.
- `RoleService.toggleRoleActive()`'s code comment claiming `active` is
  `@Transient`/non-persisted appears stale versus the current `Role.java`
  entity (which does map `IS_ACTIVE` to a real column) — needs a human to
  confirm current persisted behavior, not just trust either comment.
- No confirmed downstream consumer of `AccountActivationRequestedEvent` /
  `PasswordResetRequestedEvent` was found inside this module — an SRS pass
  should trace this to whichever module actually sends the email/notification.

---

## SECTION LAYER 2 — Structural/DB Facts
Source: migration scripts (`001_security_schema_migration_and_seed.sql`,
`002_datascope_selfservice_auth_schema.sql`,
`003_sec_pages_permissions_seed.sql`) cross-checked against `@Entity`/
`@Table`/`@Column` annotations. Scripts are explicitly NOT run by an
automated migration tool — header comment on all three: "Run manually by
DBA (psql / pgAdmin). Not applied automatically — no Flyway migration runner
is wired up for erp-security." Target DB confirmed Postgres (scripts use
Postgres-specific `nextval('SEQ')`/`regclass`/`DO $$` blocks; one script
explicitly notes translating from an Oracle-dialect source document).

### Tables found

#### USERS
  Columns:
    `USERS_PK` : BIGINT (IDENTITY) not null — PK, renamed from generic `ID`
      by migration 001 (per this project's own governance rule cited in the
      script: "PK column name must be entity-specific, never generic 'ID'")
    `USERNAME` : VARCHAR(80) not null
    `EMAIL` : VARCHAR(150) nullable — added by migration 002 (did not exist
      before; script explicitly flags this as a deviation from its own
      source spec, confirmed by a human sign-off referenced as "Conflict
      #19")
    `PASSWORD` : VARCHAR(200) not null
    `ENABLED` : SMALLINT/NUMBER(1) not null, default true (via converter)
    `CREATED_AT`/`CREATED_BY`/`UPDATED_AT`/`UPDATED_BY` : standard audit
      columns (from `AuditableEntity`)
  Primary key: `USERS_PK`, constraint name `USERS_PK`
  Foreign keys: none (parent table)
  Indexes/unique constraints: `UK_USERS_USERNAME` (USERNAME), `UK_USERS_EMAIL`
    (EMAIL, added migration 002), `IDX_USERS_ENABLED`, `IDX_USERS_USERNAME`
  Audit columns present: yes, standard names.

#### ROLES
  Columns: `ROLES_PK` (BIGINT IDENTITY, PK, renamed from `ID`), `NAME`
    VARCHAR(60) not null, `ROLE_CODE` VARCHAR(60) not null (added migration
    001 STEP 3, backfilled from `NAME`), `DESCRIPTION` VARCHAR(500) nullable
    (added migration 001 STEP 3), `IS_ACTIVE` SMALLINT not null default true,
    + standard audit columns.
  Primary key: `ROLES_PK`
  Unique constraints: `UK_ROLES_NAME` (NAME), `UK_ROLES_ROLE_CODE`
    (ROLE_CODE, added migration 001)
  Indexes: `IDX_ROLES_IS_ACTIVE`

#### PERMISSIONS
  Columns: `PERMISSIONS_PK` (BIGINT IDENTITY, PK, renamed from `ID`), `NAME`
    VARCHAR(150) not null, `PAGE_ID_FK` BIGINT nullable,
    `PERMISSION_TYPE` VARCHAR(20) nullable, + audit columns.
  Primary key: `PERMISSIONS_PK`
  Foreign keys: `PAGE_ID_FK` → `SEC_PAGES.SEC_PAGES_PK` (`FK_PERMS_PAGE`)
  Unique constraints: `UK_PERMS_NAME` (NAME)
  Indexes: `IDX_PERMS_NAME`, `IDX_PERMS_PAGE_FK`, `IDX_PERMS_TYPE`

#### SEC_PAGES
  Columns: `SEC_PAGES_PK` (BIGINT, SEQUENCE `SEC_PAGES_SEQ`, PK — renamed
    from `ID_PK`), `PAGE_CODE` VARCHAR(50) not null, `NAME_AR` VARCHAR(100)
    not null, `NAME_EN` VARCHAR(100) not null, `ROUTE` VARCHAR(200) not null,
    `ICON` VARCHAR(50) nullable, `MODULE` VARCHAR(50) nullable,
    `PARENT_ID_FK` BIGINT nullable (unconstrained — no actual FK declared),
    `DISPLAY_ORDER` INTEGER nullable, `IS_ACTIVE` SMALLINT nullable,
    `DESCRIPTION` VARCHAR(500) nullable, + audit columns.
  Primary key: `SEC_PAGES_PK`
  Unique constraints: `UK_PAGES_CODE` (PAGE_CODE), `UK_PAGES_ROUTE` (ROUTE)
  Indexes: `IDX_PAGES_MODULE`, `IDX_PAGES_ACTIVE`

#### REFRESH_TOKENS
  Columns: `REFRESH_TOKENS_PK` (BIGINT IDENTITY, PK, renamed from `ID`),
    `JTI` VARCHAR(64) not null unique, `USER_ID_FK` BIGINT not null (renamed
    from `USER_ID`), `CREATED_AT` TIMESTAMP not null, `EXPIRES_AT` TIMESTAMP
    not null, `REVOKED` SMALLINT not null default false.
  Primary key: `REFRESH_TOKENS_PK`
  Foreign keys: `USER_ID_FK` → `USERS.USERS_PK` (`FK_RT_USER`)
  Audit columns present: NO — only `CREATED_AT`/`EXPIRES_AT`, no
    CREATED_BY/UPDATED_AT/UPDATED_BY (not `AuditableEntity`).

#### USER_ROLES (pure join table, no `@Entity`)
  Columns: `USER_ID_FK` (renamed from `USER_ID`), `ROLE_ID_FK` (renamed from
    `ROLE_ID`)
  Foreign keys: `FK_UR_USER` → `USERS.USERS_PK`, `FK_UR_ROLE` →
    `ROLES.ROLES_PK`

#### ROLE_PERMISSIONS (pure join table)
  Columns: `ROLE_ID_FK` (renamed from `ROLE_ID`), `PERM_ID_FK` (renamed from
    `PERM_ID`)
  Foreign keys: `FK_RP_ROLE` → `ROLES.ROLES_PK`, `FK_RP_PERM` →
    `PERMISSIONS.PERMISSIONS_PK`

#### SEC_USER_PROFILE (ENTITY-SEC-009)
  Columns: `USER_ID_FK` BIGINT not null (PK, shared with `USERS`),
    `BRANCH_ID_FK` BIGINT not null, `FULL_NAME_AR` VARCHAR(200),
    `FULL_NAME_EN` VARCHAR(100), `PREFERRED_LANG` VARCHAR(10),
    `EMPLOYEE_ID_FK` BIGINT nullable unconstrained, `IS_ACTIVE_FL` SMALLINT
    default 1 not null, + full standard audit columns (CREATED_BY/
    CREATED_AT/UPDATED_BY/UPDATED_AT, all present, CREATED_BY/CREATED_AT
    not null).
  Primary key: `PK_SEC_USER_PROFILE` on `USER_ID_FK`
  Foreign keys: `FK_SEC_USER_PROFILE_USER` → `USERS.USERS_PK` (intra-module),
    `FK_SEC_USER_PROFILE_BRANCH` → `ORG_BRANCH.BRANCH_PK` (cross-module,
    XM-SEC-001)
  Indexes: `IDX_SEC_USER_PROFILE_BRANCH`, `IDX_SEC_USER_PROFILE_EMPLOYEE`

#### SEC_ROLE_BRANCH (ENTITY-SEC-010)
  Columns: `ROLE_ID_FK` BIGINT not null, `BRANCH_ID_FK` BIGINT not null,
    `DATA_ACCESS_LEVEL` VARCHAR(30) not null, `IS_ACTIVE_FL` SMALLINT
    default 1 not null, + standard audit columns.
  Primary key: `PK_SEC_ROLE_BRANCH` composite (`ROLE_ID_FK`, `BRANCH_ID_FK`)
  Foreign keys: `FK_SEC_ROLE_BRANCH_ROLE` → `ROLES.ROLES_PK` (intra-module),
    `FK_SEC_ROLE_BRANCH_BRANCH` → `ORG_BRANCH.BRANCH_PK` (cross-module,
    XM-SEC-002)
  Indexes: `IDX_SEC_ROLE_BRANCH_BRANCH`

#### PASSWORD_RESET_TOKEN (ENTITY-SEC-011)
  Columns: `TOKEN_PK` BIGINT (SEQUENCE `PASSWORD_RESET_TOKEN_SEQ`, PK),
    `TOKEN` VARCHAR(64) not null unique, `USER_ID_FK` BIGINT not null,
    `CREATED_AT` TIMESTAMP not null, `EXPIRES_AT` TIMESTAMP not null,
    `USED_FL` SMALLINT default 0 not null (`CHK_PASSWORD_RESET_TOKEN_USED_FL`
    check IN (0,1)).
  Primary key: `PK_PASSWORD_RESET_TOKEN`
  Foreign keys: `FK_PASSWORD_RESET_TOKEN_USER` → `USERS.USERS_PK`
  Indexes: `IDX_PASSWORD_RESET_TOKEN_USER`, `IDX_PASSWORD_RESET_TOKEN_EXPIRES`
  Audit columns present: NO — CREATED_AT/EXPIRES_AT only.

#### ACCOUNT_ACTIVATION_TOKEN (ENTITY-SEC-012)
  Same shape as `PASSWORD_RESET_TOKEN` (own sequence, own FK/check
  constraint names, own indexes). Audit columns present: NO.

### Lookup tables found
#### MD_MASTER_LOOKUP / MD_LOOKUP_DETAIL — referenced by:
  `SEC_ROLE_BRANCH.DATA_ACCESS_LEVEL` (application-level validation only, no
  DB FK). These tables are owned by MASTERDATA, not created here; this
  module's migration 002 BLOCK 8 only INSERTs seed rows into them
  (`LOOKUP_KEY = 'DATA_ACCESS_LEVEL'`, 3 detail codes:
  `BRANCH_ONLY` / `BRANCH_AND_CHILDREN` / `ALL`). Migration script's own
  header flags that the source spec's INSERT lists omitted
  CREATED_AT/CREATED_BY (NOT NULL on both tables) and the applied script had
  to add them to make the seed actually insertable — a deviation, called out
  explicitly, not a silent fix.

### Deviations from this ecosystem's naming conventions (flag, don't fix)
- `RefreshToken`, `PasswordResetToken`, `AccountActivationToken` do NOT
  extend `AuditableEntity` and have no `CREATED_BY`/`UPDATED_AT`/`UPDATED_BY`
  columns — only `CREATED_AT`/`EXPIRES_AT`. Explicitly acknowledged in code
  comments as a deliberate, documented exception ("Infrastructure table, no
  soft-delete"), not an oversight — but still a deviation from the
  `AuditableEntity` pattern every other entity in this module follows.
  `RefreshToken` additionally uses `@CreationTimestamp` directly rather than
  `AuditEntityListener`.
  `Role.getActiveStatus()` / `Page.getActiveStatus()` are deliberately NOT
  named `isActive()` to dodge a Hibernate property-accessor collision — a
  real naming-convention document should note this as a required pattern
  for any future Boolean-flag entity in this module, not just an isolated
  workaround.
- `SEC_PAGES.PARENT_ID_FK` has no actual DB foreign-key constraint despite
  the `_FK` suffix implying one — it's a plain nullable `BIGINT`
  self-reference, unconstrained at the DB layer.
- `SEC_USER_PROFILE.EMPLOYEE_ID_FK` — same pattern: `_FK`-suffixed column
  name with zero DB constraint, explicitly deferred pending a not-yet-
  governed HR module (migration 002's own BLOCK 11 comment: "pending HR
  module governance (OQ-005, no XM-ID yet)").
- Migration 001 itself is a record of this module's own historical
  convention drift: original `USERS.ID`, `ROLES.ID`, etc. had to be
  renamed to entity-specific PK names (`USERS_PK`, `ROLES_PK`, ...) to
  match "how erp-org/erp-masterdata already name PK columns" — meaning the
  original schema violated the project's own PK-naming rule and had to be
  corrected after the fact, live, via `ALTER TABLE ... RENAME COLUMN`.
- Migration 001's STEP 4 (bootstrap seed) is explicitly DESTRUCTIVE — it
  `DELETE`s every row from 7 tables (REFRESH_TOKENS, USER_ROLES,
  ROLE_PERMISSIONS, USERS, PERMISSIONS, ROLES, SEC_PAGES) before reseeding,
  with an explicit `⚠️ DESTRUCTIVE` warning in the script header. Bootstrap
  credentials are the well-known `admin`/`admin` (BCrypt-hashed), explicitly
  flagged "local/dev bootstrap — never run this step against a staging or
  production database."

---

## SECTION LAYER 3.1 — Backend Execution Facts

### Architectural pattern actually used
- Layering: Controller → Service → Repository, standard for most
  controllers (`UserController`, `RoleController`, `PageController`,
  `PermissionController`, `MenuController`). **Exception**:
  `SecRoleBranchController` and `SecUserProfileController` are explicitly
  "thin controllers" — code comment states the convention here is that
  `@PreAuthorize` NEVER appears on these two controllers, only inside their
  respective Services ("per this codebase's A.5.2 service-contract
  convention (governance-repo enforce-backend-contract skill)"). The other,
  older controllers do NOT consistently follow this — some have
  controller-level `@PreAuthorize` (`PermissionController`,
  `MenuController.getUserMenuById()`, most of `PageController`) and some
  don't (`PageController.createPage()`/`updatePage()`, `UserController`
  entirely relies on Service-layer `@PreAuthorize` — code comment: "User
  Controller - Authorization handled at service layer (Rule 19.1)"). This is
  an inconsistent-but-real pattern across the module's history, not a single
  clean convention.
- Domain logic placement: mostly Service layer (validation, uniqueness
  checks, cross-module calls). Some domain logic lives directly on entities
  via `@PrePersist`/`@PreUpdate` lifecycle callbacks — e.g. `Page` normalizes
  `pageCode` to uppercase/trimmed on both create and update; `SecRoleBranch`/
  `SecUserProfile` default `isActiveFl` to true on `@PrePersist` if null.

### Repository Strategy patterns found (per repository/query method)
- `UserAccountRepository.findByUsernameWithRoles()` /
  `findByIdWithRoles()` → `LEFT JOIN FETCH` JPQL to avoid N+1 when loading a
  user with roles+permissions (explicit anti-N+1 comment in code).
- `RoleRepository.findByIdWithPermissions()` → `LEFT JOIN FETCH` role→
  permissions→page in one query, same anti-N+1 rationale.
- `RoleRepository.findByFilters()` → hand-written JPQL with nullable
  parameter pattern (`:search IS NULL OR ...`) for optional filtering,
  `@Transactional(readOnly = true)` on the calling service methods.
- `RoleRepository.hasUserAssignments()` → JPQL `COUNT(...) > 0` existence
  check used as a pre-delete guard (returns primitive boolean, not an
  entity fetch).
- Most list/search endpoints use a generic `SpecBuilder`/`SearchRequest`/
  `SetAllowedFields` pattern (from `erp-common-utils`'s `com.erp.common.search`
  package) to build a `Specification<T>` from a whitelisted field set, then
  `JpaSpecificationExecutor.findAll(spec, pageable)` — this is the dominant,
  repeated pattern across `UserService`, `RoleService`, `PageService`,
  `PermissionService`, `SecRoleBranchService`, `SecUserProfileService`.
- `@Transactional` boundaries: write operations use plain `@Transactional`;
  read operations consistently use `@Transactional(readOnly = true)`
  (observed in every list/get/search method across all services).
- Sort-field whitelisting: every service defines a static
  `ALLOWED_*_SORT_FIELDS`/`ALLOWED_*_SEARCH_FIELDS` `Set<String>` and passes
  it through `PageableValidator.validateSortFields()` or
  `PageableBuilder.from()` — a consistent, repeated defensive pattern
  against arbitrary sort-injection (code comments tag this "Rule 17.3" in
  several services).

### Error handling pattern
- Exception hierarchy: single `LocalizedException extends RuntimeException`
  (in `erp-common-utils`), carrying a domain `StatusCode` (e.g.
  `Status.NOT_FOUND`, `Status.ALREADY_EXISTS`, `Status.CONFLICT`,
  `Status.BAD_REQUEST`) + an i18n `messageKey` + `args` for message
  interpolation. No SECURITY-specific exception subclasses were found — the
  module reuses the common one exclusively, differentiated only by its own
  `SecurityErrorCodes` string constants passed as `messageKey`.
- HTTP status mapping: NOT via `@ExceptionHandler`/`@ResponseStatus` inside
  `erp-security` itself — `LocalizedException` javadoc states the
  authoritative mapping happens in a `GlobalExceptionHandler` (not found in
  this module; presumably lives in `erp-main` or `erp-common-utils`), via
  `OperationCode.toHttpStatus(getStatusCode(), ...)`. The `HttpStatus`
  field on `LocalizedException` itself is documented as "a best-effort
  default ... NOT the authoritative response status."
- Message pattern: dual Arabic/English support confirmed at multiple
  levels — `@Operation(description = "...")` Swagger text is bilingual
  throughout controllers (Arabic + English in the same string); `Page`
  entity has separate `nameAr`/`nameEn` columns; `SecurityErrorCodes`
  javadoc references `erp-main/src/main/resources/i18n/messages.properties`
  and `messages_ar.properties` as the required destination for every error
  code's translated message (not verified to exist from this module's code
  alone — that file lives outside `erp-security`).
- Error-code naming rule stated in code: "Rule 31.3: Error codes must follow
  UPPERCASE_SNAKE_CASE format. Pattern: SEC_<ENTITY>_<ERROR_DESCRIPTION>" —
  though in practice many existing codes (`USER_NOT_FOUND`,
  `ROLE_NOT_FOUND`, etc.) don't carry the `SEC_` prefix; only the newer
  DataScope/self-service codes consistently do
  (`SEC_USER_PROFILE_NOT_FOUND`, etc.) — a real naming-convention doc should
  flag this as inconsistent legacy-vs-new naming, not a hard rule
  universally followed.

### Security implementation pattern
- Stateless JWT auth: `SecurityConfig` configures
  `SessionCreationPolicy.STATELESS`, disables CSRF, wires
  `JwtAuthenticationFilter` + `LoginRateLimitFilter` before
  `UsernamePasswordAuthenticationFilter`. Two filter chains: one
  `@Order(1)` chain permits `/actuator/health` unconditionally (Docker
  healthcheck, no auth/JWT filter at all); the main `@Order(2)` chain
  permits `/api/auth/**` + Swagger/OpenAPI paths, requires authentication
  for everything else.
- `@EnableMethodSecurity` is active — most authorization is enforced via
  `@PreAuthorize("hasAuthority(...)")` at the Service layer (see
  Architectural pattern note on inconsistency between controller-level and
  service-level placement).
- `JwtAuthenticationFilter` explicitly skips `/actuator/health` and
  `/api/auth/**` (`shouldNotFilter()`), extracts `userId` from the token
  (not username, "more efficient") and loads the user via
  `CustomUserDetailsService.loadUserById()`, silently swallowing any
  malformed/invalid token (`catch (Exception ex) { log.debug(...) }`) and
  simply not authenticating — no 401 thrown directly by the filter itself;
  downstream Spring Security's exception handling (`CustomAuthenticationEntryPoint`)
  produces the actual 401 response.
- Passwords stored as BCrypt hashes (via `PasswordEncoder`, confirmed by
  seed data comment "$2y$10$... BCrypt").
- `AuthService.login()`/`loginWithUserInfo()` both contain a diagnostic
  block that looks up the user pre-authentication purely to log
  `[AUTH-DIAG]` hash-prefix + bcrypt-match info (raw password itself is
  explicitly NOT logged, comment cites "OWASP A09"). This diagnostic
  lookup is a duplicate of the authentication that `authManager.authenticate()`
  performs immediately after — flagged as a real, observable inefficiency/
  smell (executes the user lookup + password comparison twice per login
  call), not a security bug.
- Login rate limiting: `LoginRateLimiterService` (in-memory `bucket4j`
  buckets keyed by `ip|username`) + `LoginRateLimitFilter`
  (`RATE_LIMIT_LOGIN_EXCEEDED` error). Explicitly documented as not
  horizontally-scalable in its current form.
- CORS: configured centrally in `SecurityConfig.corsConfigurationSource()`,
  driven by `CorsProperties`; wildcard origin handling switches to
  `allowedOriginPatterns` instead of `allowedOrigins` specifically to remain
  compatible with `allowCredentials(true)`.

### Existing test coverage (as behavior evidence, not for governance TC-IDs)
- `PageControllerTest` (168 lines) → integration-style tests asserting
  `@PreAuthorize` enforcement per endpoint: 200 with the right authority,
  403 without it, 401 when unauthenticated — covers `searchPages`,
  `getActivePages`, `getPageById`, `deactivatePage`, `reactivatePage`.
  (Notably does NOT test `createPage`/`updatePage` — consistent with those
  two lacking controller-level `@PreAuthorize`, per the Gap noted in LAYER 1.)
- `PermissionControllerTest` → same 200/403/401 pattern for
  `createPermission` and `searchPermissions`.
- `RoleServiceTest` (84 lines) → `createRole` persists roleCode+description
  and round-trips through the DTO; duplicate roleCode throws
  `DUPLICATE_ROLE_CODE`.
- `RoleAccessServiceCopyPermissionsTest` → verifies `copyPermissionsFromRole`
  copies only page-scoped permissions and leaves target's system-level
  permissions untouched; verifies `NO_PERMISSIONS_TO_COPY` when source has
  none.
- `LoginRateLimiterServiceTest` → 6th attempt within the window is blocked
  for the same key; different IP/username pairs are not cross-throttled.
- `LoginRateLimitFilterTest` → first attempt passes through with the
  request body still readable downstream (validates the
  `CachedBodyHttpServletRequest` wrapper); second attempt within window for
  same IP+username returns 429; a different username on the same IP is not
  throttled by the other key's attempts.
- `RefreshTokenCleanupJobTest` → cleanup deletes expired tokens regardless
  of revoked status; deletes revoked tokens older than the configured
  retention window.
- No test files were found for: `AuthService` (login/refresh/logout/signup/
  activation/password-reset flows), `UserService`, `PageService` (beyond the
  controller-level auth tests), `SecRoleBranchService`,
  `SecUserProfileService`, `JwtService`, `MenuService`,
  `CustomUserDetailsService`, `OrgBranchClient`/`MasterDataLookupClient`.
  This is a substantial test-coverage gap on the module's most business-
  rule-dense code (all of `AuthService`'s RULE-SEC-03x logic in particular).

### Gaps for a real execution plan
- Cross-module calls (`OrgBranchClient`, `MasterDataLookupClient`) forward
  the caller's own `Authorization` header verbatim rather than using a
  dedicated service-to-service credential — both classes' own javadoc flags
  this as a stopgap ("no service-to-service credential exists in this
  codebase yet"). A real execution plan should treat this as a known,
  self-acknowledged gap, not a stable pattern to build further on.
  `OrgBranchClient`'s javadoc additionally notes a "known BRANCH_VIEW
  cross-module permission gap" that can cause the internal call to fail
  with 403, which the client treats conservatively as "branch not
  usable" rather than surfacing the real cause.
- `PageService.createPermissionRecords()` unconditionally generates 4 CRUD
  permissions with no way to request fewer — the DataScope gap package's
  own migration script (003) had to route around this via direct SQL for
  the `USER_PROFILE` page's 3-permission requirement, and the code comment
  explicitly calls this out as a genuine implementation gap versus that
  plan's requirement, not a resolved design decision.
- `SecRoleBranch`/`SecUserProfile` deliberately have no navigable JPA
  association to their cross-module FK targets (`ORG_BRANCH`) — RI is
  DB-only; if `erp-org`'s `ORG_BRANCH` table/PK name (`BRANCH_PK`) ever
  changes, nothing in `erp-security`'s compiled code would catch it, only
  the DB FK constraint would (and only at write time).
- `EMPLOYEE_ID_FK` on `SEC_USER_PROFILE` is fully unconstrained pending an
  ungoverned HR module — explicitly flagged in the migration script's own
  "BLOCK 11: DEFERRED FK PATCH BLOCKS" section as future work with "no
  XM-ID yet."
- No Flyway/Liquibase automation exists for this module's schema — all
  three migration scripts are manual, DBA-run, and explicitly documented as
  such. Any execution plan involving new DB structure must account for this
  manual-apply reality rather than assuming CI-driven migrations.
