<!-- backend-execution-plan.md — Governed by Execution Plan Governance Engine (Project 3.1 / PASS 1) -->

# BACKEND EXECUTION PLAN — Security (SEC)

## SECTION 0 — PLAN HEADER
══════════════════════════════════════════════════════════════════
Plan Name        : New Feature — Auth & RBAC Engine — Security — BE
Plan ID          : PLAN-SEC-001
Plan Status      : CONTINUATION — amended from initial pass (srs v1.2 → v1.3): two-tier RBAC + internal SSO
Task Type        : 🆕 New Feature (amended — Behavior/Feature Ext. via upstream change)
Feature Code     : SEC-001 (srs-SEC.md v1.3 — two-tier RBAC + internal SSO; OQ-SEC-001 RESOLVED)
Module           : Security (SEC) — L1 Engine — dep: CU — Backend + Frontend
Platform         : Foundation (Domain: ERP)
Truth Layer      : Layer 3.1 — Backend Execution Truth
DB_TARGET        : POSTGRESQL_16      BACKEND_STACK: SPRING_BOOT_JAVA
DBS-ID           : DBS-SEC-001 (db-script-SEC.md v1.1)
Direct Upstream  : srs-SEC.md v1.3 + db-script-SEC.md v1.1 (← prd-SEC v2 / domain-profile-ERP.md v2)
Output Mode      : SINGLE-FILE — Agent-Ready Specification
GOVERNANCE STATE : NORMAL (srs.md + db-script.md both PRESENT)
Open Questions   : None (OQ-SEC-001 RESOLVED by Architect — RULE-SEC-012; no new OQ in v1.3)
══════════════════════════════════════════════════════════════════

> **UPSTREAM CHANGE — SEC two-tier RBAC + internal SSO (domain-profile-ERP.md v2)**
> - **Triggered by :** srs-SEC.md **v1.3** (+ENTITY-SEC-010 Module, +ENTITY-SEC-011 RoleModule, `moduleFk` on ENTITY-SEC-004 Page, +RULE-SEC-013/014, +API-SEC-017..020, +SCR-SEC-004) and db-script-SEC.md **v1.1** (+SEC_MODULE, +SEC_ROLE_MODULE, +SEC_PAGE.MODULE_FK, DBF-0049..0056).
> - **Amended here (backend-execution-plan-SEC, PLAN-SEC-001) :** +ENTITY-SEC-010/011; +FIELD-0049..0056 (↔ DBF-0049..0056); +API-SEC-017..020; +RULE-SEC-013/014 (service-layer enforcement of Tier-1 prerequisite + no-orphan derivation); +ERR-0013/0014; +QR-SEC-0023..0029; +SCR-SEC-004 (SEC_MODULES) and updated SCR-SEC-002; SEC-BE seed (SEC_MODULE rows + SEC_PAGE.MODULE_FK + Tier-1 grant SEC_ROLE_MODULE(SYS_ADMIN,SEC)); ALIGN-BE re-run against v1.3/v1.1. **All prior IDs (ENTITY-SEC-001..009, FIELD-0001..0048, API-SEC-001..016, RULE-SEC-001..012, ERR-0001..0012, QR-SEC-0001..0022, SCR-SEC-001..003, LOV-SEC-001/002, DRV-001..005) preserved verbatim.**
> - **Downstream must re-align :** backend-test-plan.md + test-execution-manifest.md (regenerate — CONTRACT-13 / §16A) → Project 4.1 (Backend Audit Gate). (Frontend Tier-1/Tier-2 UI is PASS 2 / P3.2, gated on P2.5.)

---

## SECTION 1 — PLAN INDEX — SEC — PLAN-ID: PLAN-SEC-001
══════════════════════════════════════════════════════════════════

ENTITY REGISTRY
───────────────────────────────────────────────────────────────
ENTITY-ID       │ Entity Name              │ DB Table                        │ Business Code │ Operations
────────────────┼──────────────────────────┼─────────────────────────────────┼───────────────┼───────────
ENTITY-SEC-001  │ UserAccount (SHARED own) │ SEC_USER_ACCOUNT                │ NO (username) │ C,R,U,Deact,Act,Login,Refresh,Logout
ENTITY-SEC-002  │ Role                     │ SEC_ROLE                        │ NO (roleCode) │ CRUD, AssignToUser, AssignModules (Tier-1)
ENTITY-SEC-003  │ Permission               │ SEC_PERMISSION                  │ NO            │ Auto-gen, Read, Grant/Revoke (Tier-2)
ENTITY-SEC-004  │ Page (Screen Registry)   │ SEC_PAGE                        │ NO (pageCode) │ CRUD (CORE-9 owner) — +moduleFk (v1.3)
ENTITY-SEC-005  │ RefreshToken             │ SEC_REFRESH_TOKEN               │ NO (internal) │ create/rotate/revoke
ENTITY-SEC-006  │ PasswordResetToken       │ SEC_PASSWORD_RESET_TOKEN        │ NO (internal) │ create/consume
ENTITY-SEC-007  │ AccountActivationToken   │ SEC_ACCOUNT_ACTIVATION_TOKEN    │ NO (internal) │ create/consume
ENTITY-SEC-008  │ UserRole (join)          │ SEC_USER_ROLE                   │ NO (composite)│ assign/remove
ENTITY-SEC-009  │ RolePermission (join)    │ SEC_ROLE_PERMISSION             │ NO (composite)│ grant/revoke (Tier-2)
ENTITY-SEC-010  │ Module (Registry) ⟵ v1.3 │ SEC_MODULE                      │ NO (moduleCode)│ CRUD, Grant/Revoke to Role (Tier-1)
ENTITY-SEC-011  │ RoleModule (join) ⟵ v1.3 │ SEC_ROLE_MODULE                 │ NO (composite)│ grant/revoke module (Tier-1)

FIELD REGISTRY (FIELD-ID ↔ DBF-ID, 1:1, continuous across module)
───────────────────────────────────────────────────────────────
FIELD-0001..0011 → DBF-0001..0011 : SEC_USER_ACCOUNT (ID, USERNAME, PASSWORD_HASH, EMAIL, PHONE, FULL_NAME, PREFERRED_LANG_ID, USER_STATUS_ID, FAILED_LOGIN_COUNT, LOCKED_UNTIL, IS_ACTIVE_FL)
FIELD-0012..0016 → DBF-0012..0016 : SEC_ROLE (ID, ROLE_CODE, NAME_AR, NAME_EN, IS_ACTIVE_FL)
FIELD-0017..0023 → DBF-0017..0023 : SEC_PERMISSION (ID, PERMISSION_CODE, PERMISSION_TYPE, NAME_AR, NAME_EN, IS_ACTIVE_FL, PAGE_FK)
FIELD-0024..0029 → DBF-0024..0029 : SEC_PAGE (ID, PAGE_CODE, NAME_AR, NAME_EN, IS_ACTIVE_FL, PARENT_PAGE_FK)
FIELD-0030..0034 → DBF-0030..0034 : SEC_REFRESH_TOKEN (ID, TOKEN, EXPIRES_AT, REVOKED_FL, USER_ACCOUNT_FK)
FIELD-0035..0039 → DBF-0035..0039 : SEC_PASSWORD_RESET_TOKEN (ID, TOKEN, EXPIRES_AT, USED_FL, USER_ACCOUNT_FK)
FIELD-0040..0044 → DBF-0040..0044 : SEC_ACCOUNT_ACTIVATION_TOKEN (ID, TOKEN, EXPIRES_AT, USED_FL, USER_ACCOUNT_FK)
FIELD-0045..0046 → DBF-0045..0046 : SEC_USER_ROLE (USER_ACCOUNT_FK, ROLE_FK)
FIELD-0047..0048 → DBF-0047..0048 : SEC_ROLE_PERMISSION (ROLE_FK, PERMISSION_FK)
FIELD-0049       → DBF-0049       : SEC_PAGE.MODULE_FK (moduleFk → SEC_MODULE) ⟵ v1.3 (appended, not renumbered — mirrors db-script v1.1)
FIELD-0050..0054 → DBF-0050..0054 : SEC_MODULE (ID, MODULE_CODE, NAME_AR, NAME_EN, IS_ACTIVE_FL) ⟵ v1.3
FIELD-0055..0056 → DBF-0055..0056 : SEC_ROLE_MODULE (ROLE_FK, MODULE_FK) ⟵ v1.3
Audit fields (createdBy/At, updatedBy/At) on base tables: no DBF-ID (AuditEntityListener). Join tables (incl. SEC_ROLE_MODULE): no audit, no surrogate id.
FIELD-ID count: 56 (was 48) — 1:1 with DBF-0001..0056.

API REGISTRY
───────────────────────────────────────────────────────────────
API-SEC-001 Login POST /api/v1/security/auth/login | API-SEC-002 Refresh POST /auth/refresh
API-SEC-003 Logout POST /auth/logout | API-SEC-004 Forgot-pw POST /auth/forgot-password
API-SEC-005 Reset-pw POST /auth/reset-password | API-SEC-006 Activate POST /auth/activate
API-SEC-007 Create user POST /security/users | API-SEC-008 Search users GET /security/users
API-SEC-009 Update user PUT /security/users/{id} | API-SEC-010 Deactivate user DELETE /security/users/{id}
API-SEC-011 Roles CRUD /security/roles | API-SEC-012 Assign role POST /security/users/{id}/roles
API-SEC-013 Pages CRUD /security/pages | API-SEC-014 Permissions GET /security/permissions
API-SEC-015 Grant/Revoke perm POST|DELETE /security/roles/{id}/permissions | API-SEC-016 Lookups GET /security/lookups/{lookupKey}
API-SEC-017 Assign module→role (Tier-1) POST /security/roles/{id}/modules ⟵ v1.3 | API-SEC-018 Revoke module→role DELETE /security/roles/{id}/modules/{moduleId} ⟵ v1.3
API-SEC-019 Dashboard modules (current user) GET /security/me/modules ⟵ v1.3 | API-SEC-020 Modules CRUD /security/modules ⟵ v1.3

RULE REGISTRY : RULE-SEC-001..014 (all Message-AR defined). RULE-SEC-003/005/006/007/008 carry ⚠ Client-Policy defaults (business-policies-SEC).
  ⟵ v1.3: RULE-SEC-013 (Tier-1 grant = dashboard DISPLAY FILTER + prerequisite; no separate module-level runtime gate), RULE-SEC-014 (derivation — no orphan screen permission; enforced service-layer per db-script v1.1 note).

SCREEN REGISTRY (CORE-9 — one SCR-ID = one SEC_PAGE row)
───────────────────────────────────────────────────────────────
SCR-SEC-001 │ User Management       │ COMPOSITE (PATTERN-2 SIDE_DRAWER) │ ENTITY-SEC-001 │ page_code SEC_USERS (module SEC)
SCR-SEC-002 │ Roles·Modules·Perms ⟵v1.3│ COMPOSITE (PATTERN-2 SIDE_DRAWER) │ ENTITY-SEC-002 (+SEC-011 Tier-1, +SEC-009/003 Tier-2) │ page_code SEC_ROLES (module SEC)
SCR-SEC-003 │ Page Registry         │ COMPOSITE (PATTERN-2 SIDE_DRAWER) │ ENTITY-SEC-004 │ page_code SEC_PAGE_REGISTRY (module SEC)
SCR-SEC-004 │ Module Registry ⟵ v1.3│ COMPOSITE (PATTERN-2 SIDE_DRAWER) │ ENTITY-SEC-010 │ page_code SEC_MODULES (module SEC)
Public (pre-auth, NO SEC_PAGE row): Login, Forgot/Reset Password, Activate.
Dashboard module-filter (Tier-1, US-SEC-009): platform-shell behavior via API-SEC-019 — NOT a CRUD screen (P2.5 details the flow).

LOV REGISTRY (SEC-local, runtime-loaded codes — no MD_MASTER_LOOKUP)
───────────────────────────────────────────────────────────────
LOV-SEC-001 │ SEC_PREFERRED_LANG │ preferredLangId (SEC_USER_ACCOUNT) │ AR, EN
LOV-SEC-002 │ SEC_USER_STATUS    │ userStatusId (SEC_USER_ACCOUNT)    │ PENDING_ACTIVATION, ACTIVE, INACTIVE
Note: permissionType (VIEW/CREATE/UPDATE/DELETE) is a CORE-9 code convention with a DB CHECK — NOT a LOV.
Note (v1.3): **Module is a REFERENCE entity (ENTITY-SEC-010), NOT a LOV** — no new LOV added (srs-SEC A5).

QRC SUMMARY : QR-SEC-0001..0029 (see SECTION B). ⚠ AGENT REFERENCE only. (+QR-SEC-0023..0029 for Tier-1 module CRUD/grant + derivation checks.)

DB ALIGNMENT : SECTION 2 — ALIGNED ✓ (56 FIELD↔DBF; was 48)
XM STATUS    : None outbound. SEC OWNS SHARED UserAccount; inbound SOFT-READ from FILE (XM-FILE-001) & NOTIF (XM-NOTIF-001) registered on consumer side. **Tier-1 (Module/RoleModule) is INTRA-SEC — no new XM (db-script v1.1 §3).**
SECURITY     : 4 admin screens × 4 CORE-9 permissions (all SYS_ADMIN) + public auth endpoints. Tier-1 prerequisite: SYS_ADMIN granted SEC module (SEC_ROLE_MODULE) so its screen permissions are valid (RULE-SEC-014).
══════════════════════════════════════════════════════════════════

---

## SECTION 2 — DB ALIGNMENT MANIFEST — SEC — DBS-ID: DBS-SEC-001
══════════════════════════════════════════════════════════════════
FIELD-ID       │ DBF-ID        │ Plan Type    │ FK/XM-ID                 │ Match
───────────────┼───────────────┼──────────────┼──────────────────────────┼──────
FIELD-0001..11 │ DBF-0001..11  │ (per column) │ —                        │ ✓
FIELD-0017     │ DBF-0017      │ Long         │ —                        │ ✓
FIELD-0023     │ DBF-0023      │ Long         │ FK PAGE_FK→SEC_PAGE      │ ✓
FIELD-0029     │ DBF-0029      │ Long         │ FK self PARENT_PAGE_FK   │ ✓
FIELD-0034     │ DBF-0034      │ Long         │ FK USER_ACCOUNT_FK       │ ✓
FIELD-0039     │ DBF-0039      │ Long         │ FK USER_ACCOUNT_FK       │ ✓
FIELD-0044     │ DBF-0044      │ Long         │ FK USER_ACCOUNT_FK       │ ✓
FIELD-0045..46 │ DBF-0045..46  │ Long         │ FK (join SEC_USER_ROLE)  │ ✓
FIELD-0047..48 │ DBF-0047..48  │ Long         │ FK (join SEC_ROLE_PERMISSION)│ ✓
FIELD-0049     │ DBF-0049      │ Long         │ FK MODULE_FK→SEC_MODULE (NOT NULL) ⟵v1.3│ ✓
FIELD-0050..54 │ DBF-0050..54  │ (per column) │ — (SEC_MODULE base) ⟵v1.3│ ✓
FIELD-0055..56 │ DBF-0055..56  │ Long         │ FK (join SEC_ROLE_MODULE: ROLE_FK→SEC_ROLE, MODULE_FK→SEC_MODULE) ⟵v1.3│ ✓
All remaining FIELD-IDs align 1:1 to their DBF-ID (see FIELD REGISTRY). No type mismatch. 56/56 aligned.
══════════════════════════════════════════════════════════════════
Legend: ✓ aligned | ✗ mismatch | ⏸ XM deferred. CONTRACT-1: manifest carries FIELD-ID/DBF-ID/Type/FK/Status only.
All FK columns are INTRA-module (no cross-module HARD-FK). Audit columns: no DBF-ID.

---

## SECTION 3 — OPEN QUESTIONS LOG (continuation)
══════════════════════════════════════════════════════════════════
OQ-SEC-001 │ Deactivation impact of SHARED UserAccount on SOFT consumers │ RESOLVED (Architect, 2026-09-02)
  Resolution: deactivation allowed, no cascade, history retained, reactivation permitted.
  Implemented by RULE-SEC-012 (SEC) + RULE-NOTIF-007 (NOTIF). No open questions remain.
(v1.3) No new OQ — two-tier RBAC + SSO fully derived from srs-SEC v1.3 / db-script-SEC v1.1 (prd-SEC v2 / domain-profile v2).
══════════════════════════════════════════════════════════════════

---

## SECTION 4 — DERIVATION LOG
══════════════════════════════════════════════════════════════════
DRV-001 │ ERR-0011 invalid credentials (login)     │ PLATFORM │ Standard auth 401 — not a business RULE
DRV-002 │ ERR-0012 NOT_FOUND (user/role/page)      │ PLATFORM │ Standard 404 on get/update/deactivate by id
DRV-003 │ EAGER fetch of roles on login            │ CRIT-2   │ RULE-SEC-009 needs status + roles to build the JWT authorities in one load
DRV-004 │ QR-SEC EXISTS checks (username/email/codes)│ CRIT-2 │ RULE-SEC-001 & RULE-SEC-010 require pre-insert uniqueness checks
DRV-005 │ Token hashing at rest                    │ CRIT-2   │ RULE-SEC-004 hashing principle extended to refresh/reset/activation tokens
DRV-006 │ RULE-SEC-014 pre-grant EXISTS check (QR-SEC-0027) ⟵v1.3 │ CRIT-2 │ "MUST NOT grant screen permission unless page's module granted" is a pre-insert precondition (srs-SEC A4 RULE-SEC-014; db-script v1.1 note: invariant not declaratively expressible in PostgreSQL → enforced service-layer). Mirrors DRV-004 pattern.
DRV-007 │ API-SEC-018 revoke-module semantics = BLOCK on dependents ⟵v1.3 │ CRIT-2 │ srs-SEC binds API-SEC-018 to RULE-SEC-013+014 but does not specify block-vs-cascade. P3.1 chooses BLOCK (ERR-0014) when the role still holds screen permissions in that module — explicit, non-destructive, preserves the no-orphan invariant (vs. a silent destructive cascade). Admin must remove the module's screen permissions first.
DRV-008 │ ERR-0013 HTTP 422 / ERR-0014 HTTP 409 ⟵v1.3 │ PLATFORM │ Consistent with existing mapping: business precondition violation → 422 (cf. ERR-0003); state conflict on revoke → 409 (cf. ERR-0009).
══════════════════════════════════════════════════════════════════

---

<!-- PHASE:CORE:START -->
## PHASE CORE — Architectural Policies
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓ (re-passed v1.3)

CANONICAL ARCHITECTURE (backend layers): controller/ service/ mapper/ domain/ repository/ entity/ dto/ exception/ config/
Domain behavior placement: separate classes in domain/ (auth flows, token rotation, permission auto-generation, **Tier-1 module grants + Tier-2 derivation enforcement (RULE-SEC-013/014)** are non-trivial domain logic warranting dedicated domain services — e.g. AuthDomainService, PermissionGenerationDomainService, **AuthorizationGrantDomainService**).

PROJECT-STANDARD CONSTRAINTS:
  Entity base       : AuditableEntity on all **8** base tables (audit via AuditEntityListener) — SEC_USER_ACCOUNT, SEC_ROLE, **SEC_MODULE (v1.3)**, SEC_PERMISSION, SEC_PAGE, SEC_REFRESH_TOKEN, SEC_PASSWORD_RESET_TOKEN, SEC_ACCOUNT_ACTIVATION_TOKEN.
  Exception (declared): SEC_REFRESH_TOKEN / SEC_PASSWORD_RESET_TOKEN /
    SEC_ACCOUNT_ACTIVATION_TOKEN are session artifacts with their own lifecycle
    (expiresAt/usedFl/revokedFl) — still extend AuditableEntity here since db-script
    defines the four audit columns on them; treat as standard audited entities.
  Join tables SEC_USER_ROLE / SEC_ROLE_PERMISSION / **SEC_ROLE_MODULE (v1.3)**: composite PK, NO audit, NO surrogate id.
  ✗ orgUnitId never in any DTO. TenantAuditableEntity retired (no multi-tenancy).
  Error signaling   : LocalizedException — NotFoundException BANNED.
  Error catalog     : every ERR-ID registered 4× (ErrorCodes + messages.properties + i18n JSON + ErpErrorMapperService).
  Search contract   : SearchRequest extends BaseSearchContractRequest; ALLOWED_SORT_FIELDS per search.
  Deactivation      : isActiveFl=false (record preserved). RULE-SEC-012: no cascade to SOFT consumers.
  Security          : passwords & all tokens stored hashed only (RULE-SEC-004, DRV-005). JWT access + rotating refresh.
  Authorization (v1.3): **two-tier RBAC** — Tier-1 Role→Module (SEC_ROLE_MODULE) drives dashboard visibility + is a prerequisite (RULE-SEC-013); Tier-2 Role→Screen (SEC_ROLE_PERMISSION) is the real enforcement via CORE-9 PERM_<PAGE>_<TYPE>. Derivation (RULE-SEC-014, no orphan screen permission) enforced in the service layer (db-script v1.1: not a declarative DB constraint). No separate module-level runtime gate.
  Internal SSO (v1.3): SEC is the single platform auth authority; one internal JWT valid across all modules; auth-only (identity), **separate from the two-tier authorization**. No new entity/table (confirmation of existing design; srs-SEC A7). No external federation now.
  i18n / events     : CU library — SEC publishes CU ApplicationEvents (reset/activation requested); NOTIF listens. SEC never calls NOTIF directly (srs-SEC A7).

TYPE MAPPING (POSTGRESQL_16): BIGINT→Long · VARCHAR(N)→String · SMALLINT(_FL)→Boolean · TIMESTAMP→LocalDateTime.

MODULE-SPECIFIC NOTES:
  - CORE-9 ownership: SEC owns SEC_PAGE (screen registry) and SEC_PERMISSION. Permissions are auto-generated
    4-per-page (PERM_<PAGE_CODE>_<VIEW|CREATE|UPDATE|DELETE>) — RULE-SEC-011.
  - **(v1.3) Every SEC_PAGE belongs to a module (moduleFk NOT NULL → SEC_MODULE).** Granting a page's permissions to a role is valid only if the role is granted that page's module (RULE-SEC-014).
  - LOV values are runtime codes (no ENUM, no lookup table); label resolution via API-SEC-016 / CU i18n. **Module is a reference table, not a LOV.**
  - permissionType uses DB CHECK (VIEW/CREATE/UPDATE/DELETE) — fixed convention, not a runtime LOV.
  - No Workflow Engine (RULE-13 = OFF). Temporary account lock (lockedUntil) is NOT a lifecycle status.
─────────────────────────────────────────────────────────────────
<!-- PHASE:CORE:END -->

<!-- PHASE:DATA-DOM:START -->
## PHASE DATA+DOM — Entity & Domain Specifications
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓ (re-passed v1.3)
(**11** entities ≥ 5 → SUB split by semantic group: IDENTITY / RBAC / TOKENS)

  <!-- SUB:DATA-DOM-IDENTITY:START -->
### ENTITY-SEC-001 — UserAccount (SHARED — owner SEC)
  DB Table: SEC_USER_ACCOUNT | PK: ID | Sequence: SEQ_SEC_USER_ACCOUNT | DBS-SEC-001
  BUSINESS CODE: NONE (username is the natural key).
  SHARED: consumed SOFT-READ by FILE (XM-FILE-001) & NOTIF (XM-NOTIF-001). Owner-only writes.
  FIELDS (FIELD-0001..0011 → DBF-0001..0011):
   FIELD-0001 userAccountPk ID DBF-0001 BIGINT PK SEQ_SEC_USER_ACCOUNT | المعرف / ID
   FIELD-0002 username USERNAME DBF-0002 VARCHAR(100) UNIQUE UQ_..._USERNAME Read-only after create | اسم المستخدم / Username
   FIELD-0003 passwordHash PASSWORD_HASH DBF-0003 VARCHAR(255) System — never in any DTO (RULE-SEC-004) | — / —
   FIELD-0004 email EMAIL DBF-0004 VARCHAR(255) UNIQUE UQ_..._EMAIL | البريد / Email
   FIELD-0005 phone PHONE DBF-0005 VARCHAR(30) nullable | الهاتف / Phone
   FIELD-0006 fullName FULL_NAME DBF-0006 VARCHAR(200) NOT NULL | الاسم الكامل / Full Name
   FIELD-0007 preferredLangId PREFERRED_LANG_ID DBF-0007 VARCHAR(10) LOV-SEC-001 code | اللغة المفضّلة / Preferred Language
   FIELD-0008 userStatusId USER_STATUS_ID DBF-0008 VARCHAR(50) LOV-SEC-002 code (lifecycle A6) | حالة الحساب / Status
   FIELD-0009 failedLoginCount FAILED_LOGIN_COUNT DBF-0009 SMALLINT System (RULE-SEC-005) | محاولات فاشلة / Failed Logins
   FIELD-0010 lockedUntil LOCKED_UNTIL DBF-0010 TIMESTAMP nullable | مقفول حتى / Locked Until
   FIELD-0011 isActiveFl IS_ACTIVE_FL DBF-0011 SMALLINT DEFAULT 1 · CHK IN(0,1) | نشط / Active
  DTO: CreateRequest{username,email,phone,fullName,preferredLangId} (password via activation/reset flow, not create body);
       UpdateRequest{email,phone,fullName,preferredLangId,userStatusId,isActiveFl} (username immutable; passwordHash never);
       ResponseDTO excludes passwordHash always.
  STATE MACHINE (userStatusId — LOV-SEC-002): PENDING_ACTIVATION → ACTIVE ⇄ INACTIVE (RULE-SEC-012 reactivation). Initial: PENDING_ACTIVATION.
  DOMAIN RULES: RULE-SEC-001 (unique username), RULE-SEC-002 (required fields), RULE-SEC-003 (password complexity),
   RULE-SEC-004 (store hashed only), RULE-SEC-005 (lock after N failed), RULE-SEC-009 (block login when status≠ACTIVE),
   RULE-SEC-012 (deactivation allowed, no cascade, history retained, reactivation permitted). Full text in Error Catalog + SVC+API.
  XM: SHARED-owner; no outbound XM. QR: QR-SEC-0001..0006.
  <!-- SUB:DATA-DOM-IDENTITY:END -->

  <!-- SUB:DATA-DOM-RBAC:START -->
### ENTITY-SEC-002 — Role
  Table SEC_ROLE | PK ID | SEQ_SEC_ROLE. FIELDS 0012-0016 (roleCode UNIQUE, nameAr, nameEn, isActiveFl).
  Rules: RULE-SEC-010 (unique roleCode). BC: none (roleCode natural key).
  **(v1.3) Tier-1 op AssignModules → writes SEC_ROLE_MODULE (ENTITY-SEC-011) via API-SEC-017/018; Tier-2 grants (SEC_ROLE_PERMISSION) gated by RULE-SEC-014.** QR-SEC-0007..0010.
### ENTITY-SEC-010 — Module (Registry) ⟵ v1.3  [Tier-1 grantable unit + dashboard display unit]
  Table SEC_MODULE | PK ID | SEQ_SEC_MODULE. FIELDS 0050-0054 → DBF-0050..0054 (ID, moduleCode UNIQUE VARCHAR(50), nameAr, nameEn, isActiveFl).
  BUSINESS CODE: NONE (moduleCode natural key — e.g. SEC/FILE/NOTIF/CU). BC-RULE-0 not applied.
  DTO: ModuleCreateRequest{moduleCode(create-only, immutable after),nameAr,nameEn,isActiveFl}; ModuleUpdateRequest{nameAr,nameEn,isActiveFl}; ModuleResponse{modulePk,moduleCode,nameAr,nameEn,isActiveFl}.
  Rules: RULE-SEC-010 (unique moduleCode). Module is the Tier-1 grantable unit (RULE-SEC-013). QR-SEC-0023..0025.
### ENTITY-SEC-011 — RoleModule (join, Tier-1 grant) ⟵ v1.3
  Table SEC_ROLE_MODULE — composite PK (ROLE_FK, MODULE_FK), no surrogate id, no audit. FIELDS 0055-0056 → DBF-0055..0056.
  Semantics: presence of a (role,module) row = the role is granted the module (RULE-SEC-013 → dashboard visibility + prerequisite for Tier-2). QR-SEC-0026 (grant/revoke), QR-SEC-0027 (EXISTS check).
### ENTITY-SEC-004 — Page (Screen Registry — CORE-9 owner)
  Table SEC_PAGE | PK ID | SEQ_SEC_PAGE. FIELDS 0024-0029 (pageCode UNIQUE, nameAr, nameEn, isActiveFl, parentPageFk self-FK) **+ FIELD-0049 moduleFk (DBF-0049, BIGINT FK→SEC_MODULE, NOT NULL) ⟵ v1.3**.
  DTO: PageCreate/UpdateRequest gains **moduleFk (required)**. moduleFk is the basis of the RULE-SEC-014 derivation.
  Rules: RULE-SEC-010 (unique pageCode), RULE-SEC-011 (registering a page auto-generates 4 permissions), **RULE-SEC-014 (page's module gates who may hold its screen permissions)**. QR-SEC-0011..0014.
### ENTITY-SEC-003 — Permission (auto-generated per Page, CORE-9)
  Table SEC_PERMISSION | PK ID | SEQ_SEC_PERMISSION. FIELDS 0017-0023 (permissionCode UNIQUE, permissionType CHECK VIEW/CREATE/UPDATE/DELETE, nameAr, nameEn, isActiveFl, pageFk→SEC_PAGE).
  Rules: RULE-SEC-010 (unique permissionCode), RULE-SEC-011 (generation). Never client-created directly. **(v1.3) The permission's owning module = SEC_PAGE(pageFk).moduleFk — used by RULE-SEC-014.** QR-SEC-0015..0016.
### ENTITY-SEC-008 — UserRole (join) Table SEC_USER_ROLE — composite PK (USER_ACCOUNT_FK, ROLE_FK), no audit. QR-SEC-0017 (assign/remove).
### ENTITY-SEC-009 — RolePermission (join, Tier-2) Table SEC_ROLE_PERMISSION — composite PK (ROLE_FK, PERMISSION_FK), no audit. **Grant subject to RULE-SEC-014 (no orphan screen permission).** QR-SEC-0018 (grant/revoke), QR-SEC-0029 (dependents check for revoke-module).
  <!-- SUB:DATA-DOM-RBAC:END -->

  <!-- SUB:DATA-DOM-TOKENS:START -->
### ENTITY-SEC-005 — RefreshToken Table SEC_REFRESH_TOKEN | SEQ_SEC_REFRESH_TOKEN. FIELDS 0030-0034 (token UNIQUE+hashed, expiresAt, revokedFl, userAccountFk→SEC_USER_ACCOUNT).
  Rules: RULE-SEC-006 (rotate on refresh; access TTL ⚠15m, refresh ⚠7d). QR-SEC-0019.
### ENTITY-SEC-006 — PasswordResetToken Table SEC_PASSWORD_RESET_TOKEN | SEQ_SEC_PASSWORD_RESET_TOKEN. FIELDS 0035-0039 (token UNIQUE+hashed, expiresAt, usedFl, userAccountFk).
  Rules: RULE-SEC-007 (single active, single-use, TTL ⚠60m). QR-SEC-0020.
### ENTITY-SEC-007 — AccountActivationToken Table SEC_ACCOUNT_ACTIVATION_TOKEN | SEQ_SEC_ACCOUNT_ACTIVATION_TOKEN. FIELDS 0040-0044 (token UNIQUE+hashed, expiresAt, usedFl, userAccountFk).
  Rules: RULE-SEC-008 (single active, single-use, TTL ⚠24h). QR-SEC-0021. Internal entities — no nameAr/nameEn, no screen.
  <!-- SUB:DATA-DOM-TOKENS:END -->

DATA+DOM Governance: BIND-RULE-1/2/3/4 applied — every column/sequence/LOOKUP_CODE/RULE text exact from srs-SEC v1.3 / db-script-SEC v1.1. New entities/fields (ENTITY-SEC-010/011, moduleFk) bound to DBF-0049..0056 verbatim.
─────────────────────────────────────────────────────────────────
<!-- PHASE:DATA-DOM:END -->

<!-- PHASE:SVC-API:START -->
## PHASE SVC+API — Service & API Contract Specifications
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓ (re-passed v1.3)
(**20** APIs ≥ 8 → SUB split: AUTH / USERS / RBAC / MODULES / LOOKUP; atomic API markers applied)

  <!-- SUB:SVC-API-AUTH:START -->
<!-- API:API-SEC-001:START -->
### API-SEC-001 — Login
POST /api/v1/security/auth/login | Controller AuthController.login → AuthService.login
REQUEST LoginRequest{username, password} | RESPONSE 200 TokenResponse{accessToken, refreshToken, expiresIn}
VALIDATIONS: RULE-SEC-009 (block login when userStatusId≠ACTIVE — Message-AR: لا دخول لحساب غير نشط.);
  RULE-SEC-005 (lock after ⚠5 failed logins — Message-AR: قُفل بعد محاولات فاشلة.)
ERRORS: ERR-0011 → invalid credentials → 401; ERR-0004 → RULE-SEC-005 account locked → 423; ERR-0008 → RULE-SEC-009 non-active → 403
ORCHESTRATION: load user by username (QR-SEC-0002, EAGER roles DRV-003) → verify hash → on fail increment failedLoginCount / lock (RULE-SEC-005) → check status ACTIVE (RULE-SEC-009) → issue JWT + create refresh token (rotate).
  Note (SSO): the issued JWT is the single internal platform identity token; FILE/NOTIF trust SEC's JWT authority (auth-only; authorization stays per-module Tier-2).
REPO: QR-SEC-0002 FIND_ONE + QR-SEC-0019 SAVE(refresh) — READ_WRITE
SECURITY: public (pre-auth endpoint).
<!-- API:API-SEC-001:END -->
<!-- API:API-SEC-002:START -->
### API-SEC-002 — Refresh token
POST /auth/refresh | AuthController.refresh → AuthService.refresh
REQUEST {refreshToken} | RESPONSE 200 TokenResponse (new access + rotated refresh)
VALIDATIONS: RULE-SEC-006 (rotate refresh token; reject revoked/expired — Message-AR: يُدوَّر رمز التجديد.)
ERRORS: ERR-0005 → RULE-SEC-006 invalid/expired/revoked refresh → 401
ORCHESTRATION: hash-lookup token (QR-SEC-0019) → validate not revoked & not expired → revoke old, issue new (rotation).
REPO: QR-SEC-0019 — READ_WRITE | SECURITY: public (holds valid refresh token).
<!-- API:API-SEC-002:END -->
<!-- API:API-SEC-003:START -->
### API-SEC-003 — Logout
POST /auth/logout | AuthController.logout → AuthService.logout
REQUEST {refreshToken} | RESPONSE 204
VALIDATIONS: RULE-SEC-006 (revoke refresh token on logout)
ERRORS: ERR-0005 → RULE-SEC-006 token invalid → 401 (idempotent: already-revoked returns 204)
REPO: QR-SEC-0019 UPDATE revokedFl=1 — READ_WRITE | SECURITY: authenticated.
<!-- API:API-SEC-003:END -->
<!-- API:API-SEC-004:START -->
### API-SEC-004 — Forgot password (request reset)
POST /auth/forgot-password | AuthController.forgotPassword → AuthService.requestReset
REQUEST {email} | RESPONSE 202 (always neutral — no account enumeration)
VALIDATIONS: RULE-SEC-007 (single active, single-use reset token, TTL ⚠60m)
ERRORS: none surfaced to caller (neutral response); internal errors mapped platform-standard.
ORCHESTRATION: find active user by email → invalidate prior active reset tokens → create reset token (QR-SEC-0020) → publish CU event (NOTIF listens). SEC never calls NOTIF directly.
REPO: QR-SEC-0020 SAVE — READ_WRITE | SECURITY: public.
<!-- API:API-SEC-004:END -->
<!-- API:API-SEC-005:START -->
### API-SEC-005 — Reset password
POST /auth/reset-password | AuthController.resetPassword → AuthService.resetPassword
REQUEST {token, newPassword} | RESPONSE 200
VALIDATIONS: RULE-SEC-007 (valid, unused, unexpired token); RULE-SEC-003 (password complexity min ⚠8 letters+digits — Message-AR: كلمة المرور لا تحقق التعقيد.)
ERRORS: ERR-0006 → RULE-SEC-007 invalid/expired/used reset token → 400; ERR-0003 → RULE-SEC-003 complexity → 422
ORCHESTRATION: validate token (QR-SEC-0020) → enforce complexity (RULE-SEC-003) → set new passwordHash (RULE-SEC-004) → mark token usedFl=1.
REPO: QR-SEC-0020 UPDATE + QR-SEC-0004 UPDATE(user) — READ_WRITE | SECURITY: public (holds valid token).
<!-- API:API-SEC-005:END -->
<!-- API:API-SEC-006:START -->
### API-SEC-006 — Activate account
POST /auth/activate | AuthController.activate → AuthService.activate
REQUEST {token, newPassword?} | RESPONSE 200
VALIDATIONS: RULE-SEC-008 (valid, unused, unexpired activation token); RULE-SEC-009 (moves status PENDING_ACTIVATION→ACTIVE)
ERRORS: ERR-0007 → RULE-SEC-008 invalid/expired/used activation token → 400; ERR-0003 → RULE-SEC-003 complexity (if password set) → 422
ORCHESTRATION: validate token (QR-SEC-0021) → set userStatusId=ACTIVE → set password if provided → mark token usedFl=1.
REPO: QR-SEC-0021 UPDATE + QR-SEC-0004 UPDATE(user) — READ_WRITE | SECURITY: public.
<!-- API:API-SEC-006:END -->
  <!-- SUB:SVC-API-AUTH:END -->

  <!-- SUB:SVC-API-USERS:START -->
<!-- API:API-SEC-007:START -->
### API-SEC-007 — Create user
POST /api/v1/security/users | UserController.create → UserService.create
REQUEST UserCreateRequest{username, email, phone?, fullName, preferredLangId} | RESPONSE 201 UserResponse (no passwordHash)
VALIDATIONS: RULE-SEC-002 (required username,email,fullName — Message-AR: حقول الحساب الأساسية إلزامية.);
  RULE-SEC-001 (unique username — Message-AR: اسم المستخدم مستخدَم مسبقاً.)
ERRORS: ERR-0002 → RULE-SEC-002 → 400; ERR-0001 → RULE-SEC-001 dup username → 409; ERR-0010 → dup email → 409
ORCHESTRATION: validate required → EXISTS username (QR-SEC-0005) & email (QR-SEC-0006) → create with userStatusId=PENDING_ACTIVATION → issue activation token (QR-SEC-0021) → publish CU event.
REPO: QR-SEC-0001 SAVE — READ_WRITE — Sequence SEQ_SEC_USER_ACCOUNT
SECURITY: SCR-SEC-001 CREATE (PERM_SEC_USERS_CREATE).
<!-- API:API-SEC-007:END -->
<!-- API:API-SEC-008:START -->
### API-SEC-008 — Search users
GET /api/v1/security/users | UserController.search → UserService.search
REQUEST params: username?(LIKE), email?(LIKE), userStatusId?(EXACT), isActiveFl?(EXACT), page,size,sortBy,sortDir; ALLOWED_SORT_FIELDS={username,email,userStatusId,createdAt}
RESPONSE 200 Page<UserResponse> (empty → 200 [], never 404) | ERRORS: none
REPO: QR-SEC-0003 FIND_BY_CRITERIA — READ_ONLY — Join NONE | SECURITY: SCR-SEC-001 VIEW.
<!-- API:API-SEC-008:END -->
<!-- API:API-SEC-009:START -->
### API-SEC-009 — Update user
PUT /api/v1/security/users/{id} | UserController.update → UserService.update
REQUEST UserUpdateRequest{email,phone?,fullName,preferredLangId,userStatusId,isActiveFl} (username immutable)
RESPONSE 200 UserResponse
VALIDATIONS: RULE-SEC-001 (email uniqueness on change)
ERRORS: ERR-0012 → NOT_FOUND → 404; ERR-0010 → dup email → 409
REPO: QR-SEC-0001 FIND_ONE + QR-SEC-0004 UPDATE — READ_WRITE | SECURITY: SCR-SEC-001 UPDATE.
<!-- API:API-SEC-009:END -->
<!-- API:API-SEC-010:START -->
### API-SEC-010 — Deactivate user (soft)
DELETE /api/v1/security/users/{id} | UserController.deactivate → UserService.deactivate
RESPONSE 200/204
VALIDATIONS: RULE-SEC-012 (allow deactivation; NO cascade to SOFT consumers; history retained; reactivation permitted — Message-AR: يُسمح بإلغاء تنشيط الحساب دون تعاقب؛ تُحفظ المراجع التاريخية ويُسمح بإعادة التنشيط.)
ERRORS: ERR-0012 → NOT_FOUND → 404
ORCHESTRATION: load (QR-SEC-0001) → set isActiveFl=0 & userStatusId=INACTIVE (QR-SEC-0004). No cascade. Consumers (NOTIF RULE-NOTIF-007) block NEW ops at their layer.
REPO: QR-SEC-0004 — READ_WRITE | SECURITY: SCR-SEC-001 DELETE.
<!-- API:API-SEC-010:END -->
<!-- API:API-SEC-012:START -->
### API-SEC-012 — Assign role to user
POST /api/v1/security/users/{id}/roles | UserController.assignRole → UserRoleService.assign
REQUEST {roleId} | RESPONSE 200
VALIDATIONS: existence of user & role (idempotent insert into SEC_USER_ROLE)
ERRORS: ERR-0012 → NOT_FOUND (user or role) → 404
REPO: QR-SEC-0017 SAVE(join) — READ_WRITE | SECURITY: SCR-SEC-001 UPDATE.
<!-- API:API-SEC-012:END -->
  <!-- SUB:SVC-API-USERS:END -->

  <!-- SUB:SVC-API-RBAC:START -->
<!-- API:API-SEC-011:START -->
### API-SEC-011 — Roles CRUD
POST/GET/PUT/DELETE /api/v1/security/roles(/{id}) | RoleController → RoleService
REQUEST RoleCreate/UpdateRequest{roleCode(create-only, immutable after),nameAr,nameEn,isActiveFl}
RESPONSE 201/200 RoleResponse; search → Page<RoleResponse>
VALIDATIONS: RULE-SEC-010 (unique roleCode — Message-AR: الرموز فريدة.); RULE-SEC-002-equiv (nameAr,nameEn required — LOC)
ERRORS: ERR-0009 → RULE-SEC-010 dup code → 409; ERR-0012 → NOT_FOUND → 404
REPO: QR-SEC-0007..0010 (find_one/search/save/update, EXISTS roleCode) — mixed | ALLOWED_SORT_FIELDS={roleCode,nameAr,createdAt}
SECURITY: SCR-SEC-002 (VIEW/CREATE/UPDATE/DELETE).
<!-- API:API-SEC-011:END -->
<!-- API:API-SEC-013:START -->
### API-SEC-013 — Pages CRUD (Screen Registry, CORE-9 owner)
POST/GET/PUT/DELETE /api/v1/security/pages(/{id}) | PageController → PageService
REQUEST PageCreate/UpdateRequest{pageCode(create-only),nameAr,nameEn,**moduleFk (required, → SEC_MODULE)**,parentPageFk?,isActiveFl}
VALIDATIONS: RULE-SEC-010 (unique pageCode); RULE-SEC-011 (on CREATE, auto-generate 4 permissions PERM_<pageCode>_VIEW/CREATE/UPDATE/DELETE — Message-AR: تُولَّد أربع صلاحيات لكل شاشة.); **moduleFk existence (→ SEC_MODULE, active) — required so RULE-SEC-014 can resolve the page's owning module.**
ERRORS: ERR-0009 → RULE-SEC-010 dup pageCode → 409; ERR-0012 → NOT_FOUND (page or moduleFk) → 404
ORCHESTRATION (create): validate moduleFk (QR-SEC-0024) → save page with moduleFk (QR-SEC-0013) → PermissionGenerationDomainService creates 4 SEC_PERMISSION rows (QR-SEC-0016) — RULE-SEC-011.
REPO: QR-SEC-0011..0014 + QR-SEC-0016 + QR-SEC-0024(module existence) — READ_WRITE | ALLOWED_SORT_FIELDS={pageCode,nameAr,moduleFk,createdAt}
SECURITY: SCR-SEC-003 (VIEW/CREATE/UPDATE/DELETE).
<!-- API:API-SEC-013:END -->
<!-- API:API-SEC-014:START -->
### API-SEC-014 — List permissions
GET /api/v1/security/permissions | PermissionController.search → PermissionService.search
REQUEST params: pageFk?(EXACT), permissionType?(EXACT), **moduleFk?(EXACT — via page, for Tier-2 picker scoped to granted modules)**, page,size | RESPONSE 200 Page<PermissionResponse>
VALIDATIONS: RULE-SEC-011 (permissions are system-generated; read-only listing) | ERRORS: none (empty → 200 [])
REPO: QR-SEC-0015 FIND_BY_CRITERIA — READ_ONLY | SECURITY: SCR-SEC-002 VIEW.
<!-- API:API-SEC-014:END -->
<!-- API:API-SEC-015:START -->
### API-SEC-015 — Grant / revoke permission to role (Tier-2)
POST|DELETE /api/v1/security/roles/{id}/permissions | RoleController.grant/revoke → RolePermissionService
REQUEST {permissionId} | RESPONSE 200
VALIDATIONS: existence of role & permission (idempotent join write on SEC_ROLE_PERMISSION);
  **RULE-SEC-014 (grant only): the role MUST hold the module of this permission's page (SEC_PERMISSION→SEC_PAGE.moduleFk) in SEC_ROLE_MODULE — no orphan screen permission — Message-AR: لا تُمنح صلاحية شاشة لدور ما لم يُمنَح الدور موديل الشاشة.**
ERRORS: ERR-0012 → NOT_FOUND (role or permission) → 404; **ERR-0013 → RULE-SEC-014 module-not-granted → 422 (grant only)**
ORCHESTRATION (grant): resolve permission → its page → page.moduleFk → **EXISTS SEC_ROLE_MODULE(roleId, moduleFk) (QR-SEC-0027); if absent → ERR-0013** → else idempotent insert (QR-SEC-0018). (revoke): idempotent delete (QR-SEC-0018) — no derivation check on revoke of a single permission.
REPO: QR-SEC-0018 SAVE/DELETE(join) + QR-SEC-0027(EXISTS grant) — READ_WRITE | SECURITY: SCR-SEC-002 UPDATE.
<!-- API:API-SEC-015:END -->
  <!-- SUB:SVC-API-RBAC:END -->

  <!-- SUB:SVC-API-MODULES:START -->   ⟵ v1.3 (Tier-1 module registry + grants)
<!-- API:API-SEC-020:START -->
### API-SEC-020 — Modules CRUD (Module Registry) ⟵ v1.3
POST/GET/PUT/DELETE /api/v1/security/modules(/{id}) | ModuleController → ModuleService
REQUEST ModuleCreate/UpdateRequest{moduleCode(create-only, immutable after),nameAr,nameEn,isActiveFl}
RESPONSE 201/200 ModuleResponse; search → Page<ModuleResponse>
VALIDATIONS: RULE-SEC-010 (unique moduleCode — Message-AR: الرموز فريدة.); nameAr,nameEn required
ERRORS: ERR-0009 → RULE-SEC-010 dup moduleCode → 409; ERR-0012 → NOT_FOUND → 404
REPO: QR-SEC-0023 SAVE(SEQ_SEC_MODULE) + QR-SEC-0024 find/search + QR-SEC-0025 update/EXISTS moduleCode — mixed | ALLOWED_SORT_FIELDS={moduleCode,nameAr,createdAt}
SECURITY: SCR-SEC-004 (VIEW/CREATE/UPDATE/DELETE).
<!-- API:API-SEC-020:END -->
<!-- API:API-SEC-017:START -->
### API-SEC-017 — Assign module to role (Tier-1 grant) ⟵ v1.3
POST /api/v1/security/roles/{id}/modules | RoleController.assignModule → AuthorizationGrantDomainService.grantModule
REQUEST {moduleId} | RESPONSE 200
VALIDATIONS: existence of role & module (idempotent insert into SEC_ROLE_MODULE); RULE-SEC-013 (grant = dashboard display filter + prerequisite — Message-AR: منح الموديل للدور يُظهره على الداشبورد وهو شرط مسبق لأي صلاحية شاشة داخله.)
ERRORS: ERR-0012 → NOT_FOUND (role or module) → 404
ORCHESTRATION: validate role & module active → idempotent insert SEC_ROLE_MODULE (QR-SEC-0026). No module-level runtime gate created (display filter only).
REPO: QR-SEC-0026 SAVE(join) — READ_WRITE | SECURITY: SCR-SEC-002 UPDATE.
<!-- API:API-SEC-017:END -->
<!-- API:API-SEC-018:START -->
### API-SEC-018 — Revoke module from role ⟵ v1.3
DELETE /api/v1/security/roles/{id}/modules/{moduleId} | RoleController.revokeModule → AuthorizationGrantDomainService.revokeModule
RESPONSE 200/204
VALIDATIONS: RULE-SEC-013/014 — revoke MUST preserve the no-orphan invariant. **DRV-007: BLOCK if the role still holds any screen permission for a page in this module** (would leave orphan Tier-2 grants).
ERRORS: ERR-0012 → NOT_FOUND (grant/role/module) → 404; **ERR-0014 → RULE-SEC-014 revoke would orphan screen permissions → 409 — Message-AR: لا يمكن سحب الموديل: الدور لا يزال يملك صلاحيات شاشات داخله.**
ORCHESTRATION: **EXISTS SEC_ROLE_PERMISSION for role within this module (join SEC_ROLE_PERMISSION→SEC_PERMISSION→SEC_PAGE.moduleFk) (QR-SEC-0029); if present → ERR-0014** → else delete SEC_ROLE_MODULE row (QR-SEC-0026). Admin removes the module's screen permissions first.
REPO: QR-SEC-0026 DELETE(join) + QR-SEC-0029(dependents EXISTS) — READ_WRITE | SECURITY: SCR-SEC-002 UPDATE.
<!-- API:API-SEC-018:END -->
<!-- API:API-SEC-019:START -->
### API-SEC-019 — Dashboard modules (current user) ⟵ v1.3
GET /api/v1/security/me/modules | MeController.modules → DashboardService.grantedModules
REQUEST (none — principal from JWT) | RESPONSE 200 [ModuleResponse] (distinct active modules granted to any of the caller's roles; empty → 200 [])
VALIDATIONS: RULE-SEC-013 (returns only granted modules — the dashboard DISPLAY FILTER)
ERRORS: none (authenticated principal always resolvable; empty list is valid)
ORCHESTRATION: from JWT principal → user roles (SEC_USER_ROLE) → SEC_ROLE_MODULE → distinct active SEC_MODULE (QR-SEC-0028).
REPO: QR-SEC-0028 FIND granted modules for user — READ_ONLY | SECURITY: authenticated (self-scoped; no screen permission required).
<!-- API:API-SEC-019:END -->
  <!-- SUB:SVC-API-MODULES:END -->

  <!-- SUB:SVC-API-LOOKUP:START -->
<!-- API:API-SEC-016:START -->
### API-SEC-016 — Lookups (LOV values)
GET /api/v1/security/lookups/{lookupKey} | LookupController.get → LookupService.get
REQUEST path lookupKey ∈ {SEC_PREFERRED_LANG, SEC_USER_STATUS} | RESPONSE 200 [{code,labelAr,labelEn}]
VALIDATIONS: none (runtime-loaded codes) | ERRORS: ERR-0012 → unknown lookupKey → 404
BINDING: LOV-SEC-001 SEC_PREFERRED_LANG (AR,EN); LOV-SEC-002 SEC_USER_STATUS (PENDING_ACTIVATION,ACTIVE,INACTIVE).
Note (v1.3): Module is NOT a lookupKey — modules are served by API-SEC-020 (CRUD) / API-SEC-019 (dashboard), being a reference entity not a LOV.
REPO: QR-SEC-0022 (runtime lookup resolution — no lookup table; resolved from CU i18n / code registry) — READ_ONLY
SECURITY: authenticated.
<!-- API:API-SEC-016:END -->
  <!-- SUB:SVC-API-LOOKUP:END -->

API Governance: RULE-ERR-CARRY ✓; RULE-PLATFORM-ERR ✓ (ERR-0011/0012 = PLATFORM-STD, DRV-001/002); LOC ✓ (AR+EN on every error). RULE-SEC-004 & RULE-SEC-011 are internal domain behaviors (no user-facing ERR). **RULE-SEC-013 realized via API-SEC-017/019 (display filter, no error path); RULE-SEC-014 realized via API-SEC-015 grant-check (ERR-0013) + API-SEC-018 revoke-block (ERR-0014).**
─────────────────────────────────────────────────────────────────
<!-- PHASE:SVC-API:END -->

<!-- PHASE:DOC:START -->
## PHASE DOC — Contract Stabilization (INTERNAL-ONLY, v2.0)
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓ (re-passed v1.3)
DOC-1 API Contract Summary: API-SEC-001..020 all STABLE (auth endpoints public; user/role/page/permission/module/lookup authenticated; API-SEC-019 self-scoped).
DOC-2 DTO Typing: LOV fields (preferredLangId, userStatusId) = String code (never ENUM); passwordHash never in any DTO; no Business Code. **moduleFk = Long (FK); ModuleResponse.moduleCode = String.**
DOC-3 Pagination: JPA Page<T>; SearchRequest extends BaseSearchContractRequest; empty → 200; filters username/email LIKE, status/isActiveFl/moduleFk EXACT.
DOC GATE: PASSED ✓ ⚠ INTERNAL-ONLY — PASS 2 gates on real API Docs (CONTRACT-12).
─────────────────────────────────────────────────────────────────
<!-- PHASE:DOC:END -->

<!-- PHASE:INT-C:START -->
## PHASE INT-C — Integration Contract Specifications
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓ (re-passed v1.3)

## INT-C SUMMARY — SEC — PLAN-ID: PLAN-SEC-001
XM-ID │ Classification │ Target │ Interface │ Contract Status
──────┼────────────────┼────────┼───────────┼────────────────
(none outbound — SEC has no cross-module dependencies of its own — db-script-SEC §3. **Tier-1 Module/RoleModule is INTRA-SEC — introduces no XM.**)

INBOUND XM STUB NOTATION (SEC is the SOURCE/ROOT of the SHARED identity entity):
  XM-INBOUND-STUB-1
    Consumer module : FILE — SOFT-READ of SEC_USER_ACCOUNT (auth filter + created_by)
    XM-ID assignment : XM-FILE-001 (assigned by FILE, its own P2/P3.1) — NOT-YET-CONSUMED here
    Status : NOT-YET-ASSIGNED-BY-SEC (SEC never assigns consumer XM-IDs)
  XM-INBOUND-STUB-2
    Consumer module : NOTIF — SOFT-READ of SEC_USER_ACCOUNT (recipient identity)
    XM-ID assignment : XM-NOTIF-001 (assigned by NOTIF)
    Status : NOT-YET-ASSIGNED-BY-SEC
  Event contract: SEC publishes CU ApplicationEvents (PasswordResetRequested, AccountActivationRequested).
  NOTIF subscribes. SEC never calls NOTIF directly (srs-SEC A7). RULE-SEC-012 governs deactivation semantics for consumers.
  SSO note (v1.3): the single internal JWT authority is consumed by FILE/NOTIF as trusted auth — this is authentication trust, not an XM data dependency; no XM-ID.

INT-C GATE CHECK: [✓] all XM from DB Register accounted (0 outbound) [✓] no XM invented [✓] inbound stubs use INBOUND-STUB notation [✓] Tier-1 intra-SEC — no new XM [✓] Open RXEs none
INT-C Gate: PASSED ✓
─────────────────────────────────────────────────────────────────
<!-- PHASE:INT-C:END -->

<!-- PHASE:INT-R:START -->
## PHASE INT-R — Runtime Activation Status
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓ (re-passed v1.3)
## INT-R STATUS — SEC — PLAN-ID: PLAN-SEC-001
XM-ID │ Status │ Workaround
──────┼────────┼───────────
(none — no outbound runtime dependencies. Inbound consumers resolve at their own INT-R.)
Event dispatch: CU in-process synchronous ApplicationEvent — READY (CU library available, ROOT built first).
─────────────────────────────────────────────────────────────────
<!-- PHASE:INT-R:END -->

<!-- PHASE:SEC-BE:START -->
## PHASE SEC-BE — Backend Security Specifications
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓ (re-passed v1.3)

CORE-9 — one SCR-ID = one SEC_PAGE row = 4 permissions. Permissions auto-generated (RULE-SEC-011), NOT seeded by name.
**Two-tier (v1.3): Tier-1 = SEC_ROLE_MODULE grant (dashboard filter + prerequisite, RULE-SEC-013); Tier-2 = SEC_ROLE_PERMISSION @PreAuthorize (real enforcement, unchanged CORE-9). Derivation RULE-SEC-014 enforced service-layer (AuthorizationGrantDomainService).**

### SEC-BE — SCR-SEC-001 — User Management (page_code SEC_USERS · module SEC)
  API enforcement: API-SEC-007 CREATE, 008 VIEW, 009 UPDATE, 010 DELETE, 012 UPDATE → @PreAuthorize per CORE-9 permission.
  Roles: VIEW/CREATE/UPDATE/DELETE → SYS_ADMIN (all).
### SEC-BE — SCR-SEC-002 — Roles·Modules·Permissions (page_code SEC_ROLES · module SEC) ⟵ updated v1.3
  API enforcement: API-SEC-011 (CRUD→VIEW/CREATE/UPDATE/DELETE), 014 VIEW, 015 UPDATE, **017 UPDATE (assign module), 018 UPDATE (revoke module)**. Roles: SYS_ADMIN.
### SEC-BE — SCR-SEC-003 — Page Registry (page_code SEC_PAGE_REGISTRY · module SEC)
  API enforcement: API-SEC-013 (CRUD→VIEW/CREATE/UPDATE/DELETE). Roles: SYS_ADMIN.
### SEC-BE — SCR-SEC-004 — Module Registry (page_code SEC_MODULES · module SEC) ⟵ v1.3
  API enforcement: API-SEC-020 (CRUD→VIEW/CREATE/UPDATE/DELETE). Roles: SYS_ADMIN.
Self-scoped (no screen permission): API-SEC-019 (/me/modules) — authenticated principal only.
Public (pre-auth, NO enforcement / NO SEC_PAGE row): API-SEC-001..006.

SECURITY SEED DATA REQUIREMENTS (SEC owns SEC_MODULE / SEC_PAGE / SEC_PERMISSION / SEC_ROLE_MODULE):
  SEC_MODULE rows (v1.3): at minimum module **SEC** (owns the four admin pages). Foundation platform modules FILE, NOTIF, CU may be seeded here as the Tier-1 registry as they come online (each module's pages set moduleFk accordingly). moduleCode UNIQUE.
  SEC_PAGE rows: SEC_USERS, SEC_ROLES, SEC_PAGE_REGISTRY, **SEC_MODULES** — **all with MODULE_FK → SEC module (NOT NULL)** (parent nav: Security).
  SEC_PERMISSION rows: 4 per page auto-generated at page registration (RULE-SEC-011) —
    PERM_SEC_USERS_{VIEW,CREATE,UPDATE,DELETE}, PERM_SEC_ROLES_{...}, PERM_SEC_PAGE_REGISTRY_{...}, **PERM_SEC_MODULES_{...}**.
  Tier-1 grant (SEC_ROLE_MODULE) (v1.3): **SEC_ROLE_MODULE(SYS_ADMIN, SEC)** — REQUIRED so SYS_ADMIN's screen-permission grants below satisfy RULE-SEC-014 (no orphan). Seed BEFORE the Tier-2 grants.
  Role grants (SEC_ROLE_PERMISSION): all 16 SEC screen permissions above → SYS_ADMIN (valid because SYS_ADMIN holds the SEC module grant).
SEC-BE Rules: SEC-IMPL-RULE-1 (every SCR API enforces permission), SEC-IMPL-RULE-3 (403 via LocalizedException), SEC-IMPL-RULE-4 (every SCR-ID present in SEC_PAGE), **SEC-IMPL-RULE-5 (v1.3): a Tier-2 grant is only written if the role holds the page's module (RULE-SEC-014); SEC-IMPL-RULE-6 (v1.3): dashboard modules come from SEC_ROLE_MODULE, never from screen permissions.**
─────────────────────────────────────────────────────────────────
<!-- PHASE:SEC-BE:END -->

<!-- PHASE:ALIGN-BE:START -->
## PHASE ALIGN-BE — Backend Internal Self-Consistency Gate (auto-run)
─────────────────────────────────────────────────────────────────
## ALIGN-BE GATE — SEC — PLAN-ID: PLAN-SEC-001 (re-run against srs-SEC v1.3 + db-script-SEC v1.1)
Traceability: all FIELD/API/RULE/ERR/QR-IDs appear in Plan Index ✓ | Derivation Log complete (DRV-001..008) ✓ | DB field coverage 56/56 ✓
Upstream realignment: Feature Code cites srs v1.3 ✓ | DBS cites db-script v1.1 ✓ | every v1.3 element traces to srs v1.3 / db-script v1.1 (ENTITY-SEC-010/011, moduleFk, RULE-SEC-013/014, API-SEC-017..020) ✓ | nothing invented beyond upstream ✓
Business Code: N/A (SEC owns no business codes; natural keys incl. moduleCode) ✓
Localization: all **14** RULE-IDs have Message-AR ✓ | all **14** error responses AR+EN ✓
Security: every screen-serving API-ID has permission declared ✓ | SCR-SEC-001/002/003/**004** have SEC-BE blocks ✓ | CORE-9 (1 SCR=1 page=4 perms) ✓ | Tier-1 seed SEC_ROLE_MODULE(SYS_ADMIN,SEC) present so no orphan Tier-2 grant (RULE-SEC-014) ✓ | API-SEC-019 self-scoped (no perm) ✓
QRC: every DB-op API has QR-ID ✓ | agent-reference labels ✓ | no ENUM for LOV ✓ | Module is reference not LOV ✓ | no join to lookups ✓ | exact sequence names on SAVE (incl. SEQ_SEC_MODULE) ✓
Derivation enforcement: RULE-SEC-013 → display filter via API-SEC-019, no module runtime gate ✓ | RULE-SEC-014 → grant pre-check (QR-SEC-0027/ERR-0013) + revoke-block (QR-SEC-0029/ERR-0014), consistent with db-script v1.1 "enforced app-layer" note ✓
TEST-BE: SECTION D present, updated to 14 rules / 20 APIs ✓ | no GAP without DEFERRED ✓
Artifact binding: no placeholders ✓ | RULE text inline ✓ | every column→DBF-ID (0001..0056) ✓ | Message-AR exact ✓ | Manifest CONTRACT-1 ✓
Plan completeness: CORE architecture ✓ | domain placement ✓ | no orgUnitId in DTO ✓ | no audit in Create/Update ✓ | LocalizedException ✓ | ERR 4-registration ✓ | ALLOWED_SORT_FIELDS per search ✓ | empty search→200 ✓ | RULE-SEC-012 deactivation (no cascade) ✓ | passwordHash never in DTO ✓
CROSS-MODULE: no outbound XM; Tier-1 intra-SEC; inbound stubs INBOUND-STUB ✓ | RULE-SEC-011 permission generation ✓
ID PRESERVATION: ENTITY-SEC-001..009, FIELD-0001..0048, API-SEC-001..016, RULE-SEC-001..012, ERR-0001..0012, QR-SEC-0001..0022, SCR-SEC-001..003, LOV-SEC-001/002, DRV-001..005 unchanged; only appends (no renumber/reuse) ✓
═══════════════════════════════════════════════════════════════════
ALIGN-BE GATE RESULT: PASSED ✓ | Auto-correction: None
═══════════════════════════════════════════════════════════════════
─────────────────────────────────────────────────────────────────
<!-- PHASE:ALIGN-BE:END -->

---

## SECTION A — ERROR CATALOG (canonical)
══════════════════════════════════════════════════════════════════════════════════
ERR-ID   │ RULE-ID      │ API-ID          │ HTTP │ Trigger                     │ Message-AR                                │ Message-EN
─────────┼──────────────┼─────────────────┼──────┼─────────────────────────────┼───────────────────────────────────────────┼──────────────────────────
ERR-0001 │ RULE-SEC-001 │ API-SEC-007     │ 409  │ Duplicate username          │ اسم المستخدم مستخدَم مسبقاً.               │ Username already exists.
ERR-0002 │ RULE-SEC-002 │ API-SEC-007     │ 400  │ Missing core account fields │ حقول الحساب الأساسية إلزامية.              │ Core account fields required.
ERR-0003 │ RULE-SEC-003 │ API-SEC-005/006 │ 422  │ Password complexity         │ كلمة المرور لا تحقق التعقيد.               │ Password complexity not met.
ERR-0004 │ RULE-SEC-005 │ API-SEC-001     │ 423  │ Account locked (failed logins)│ قُفل بعد محاولات فاشلة.                   │ Locked after failed logins.
ERR-0005 │ RULE-SEC-006 │ API-SEC-002/003 │ 401  │ Invalid/expired/revoked refresh│ يُدوَّر رمز التجديد.                      │ Refresh token invalid or rotated.
ERR-0006 │ RULE-SEC-007 │ API-SEC-005     │ 400  │ Invalid/used/expired reset tok │ رمز إعادة تعيين واحد فعّال.               │ Reset token invalid or already used.
ERR-0007 │ RULE-SEC-008 │ API-SEC-006     │ 400  │ Invalid/used/expired activation│ رمز تفعيل واحد فعّال.                     │ Activation token invalid or already used.
ERR-0008 │ RULE-SEC-009 │ API-SEC-001     │ 403  │ Login on non-active account │ لا دخول لحساب غير نشط.                     │ Login blocked for non-active account.
ERR-0009 │ RULE-SEC-010 │ API-SEC-011/013/**020** │ 409 │ Duplicate role/permission/page/**module** code │ الرموز فريدة.               │ Code must be unique.
ERR-0010 │ RULE-SEC-001 │ API-SEC-007/009 │ 409  │ Duplicate email             │ البريد مستخدَم مسبقاً.                     │ Email already exists.
ERR-0011 │ PLATFORM-STD │ API-SEC-001     │ 401  │ Invalid credentials         │ بيانات الدخول غير صحيحة.                   │ Invalid credentials.
ERR-0012 │ PLATFORM-STD │ API-SEC-009/010/012/013/015/016/**017/018/019/020** │ 404 │ Resource not found │ العنصر غير موجود.        │ Resource not found.
ERR-0013 │ RULE-SEC-014 │ API-SEC-015     │ 422  │ ⟵v1.3 Grant screen permission whose module is not granted to the role │ لا تُمنح صلاحية شاشة لدور ما لم يُمنَح الدور موديل الشاشة. │ Screen permission requires the role to hold the page's module.
ERR-0014 │ RULE-SEC-014 │ API-SEC-018     │ 409  │ ⟵v1.3 Revoke module while role still holds screen permissions within it │ لا يمكن سحب الموديل: الدور لا يزال يملك صلاحيات شاشات داخله. │ Cannot revoke module: role still holds screen permissions within it.
══════════════════════════════════════════════════════════════════════════════════
Total Errors: **14** (ERR-0011/0012 = PLATFORM-STD, DRV-001/002; ERR-0013/0014 = RULE-SEC-014 derivation, DRV-006/007/008). RULE-SEC-004/011/012/013 are internal/permissive — no user-facing ERR (RULE-SEC-013 is a display filter, not an error path).
Every ERR-ID registered in 4 places (ErrorCodes + messages.properties + i18n JSON + ErpErrorMapperService). ERR-0013/0014 to be added to all four (amendment).

---

## SECTION B — QUERY REFERENCE CATALOG (agent reference)
══════════════════════════════════════════════════════════════════
⚠ AGENT REFERENCE ONLY — rewrite every query using actual JPA entity/field names.
QR-SEC-0001 SAVE user (SEQ_SEC_USER_ACCOUNT) — READ_WRITE
QR-SEC-0002 FIND_ONE user by USERNAME (EAGER roles, DRV-003) — READ_ONLY
QR-SEC-0003 FIND_BY_CRITERIA users (username/email LIKE, status/active EXACT) — READ_ONLY, paged
QR-SEC-0004 UPDATE user — READ_WRITE
QR-SEC-0005 EXISTS username (RULE-SEC-001) — READ_ONLY
QR-SEC-0006 EXISTS email (RULE-SEC-001) — READ_ONLY
QR-SEC-0007 FIND_ONE role by ID — READ_ONLY
QR-SEC-0008 FIND_BY_CRITERIA roles — READ_ONLY, paged
QR-SEC-0009 SAVE role (SEQ_SEC_ROLE) — READ_WRITE
QR-SEC-0010 UPDATE role / EXISTS roleCode — READ_WRITE/READ_ONLY
QR-SEC-0011 FIND_ONE page by ID — READ_ONLY
QR-SEC-0012 FIND_BY_CRITERIA pages (incl. moduleFk EXACT) — READ_ONLY, paged
QR-SEC-0013 SAVE page (SEQ_SEC_PAGE; with MODULE_FK) — READ_WRITE
QR-SEC-0014 UPDATE page / EXISTS pageCode — READ_WRITE/READ_ONLY
QR-SEC-0015 FIND_BY_CRITERIA permissions (pageFk, permissionType, moduleFk via page) — READ_ONLY, paged
QR-SEC-0016 SAVE permission × 4 per page (SEQ_SEC_PERMISSION) — RULE-SEC-011 generation — READ_WRITE
QR-SEC-0017 SAVE/DELETE SEC_USER_ROLE join (composite key) — READ_WRITE
QR-SEC-0018 SAVE/DELETE SEC_ROLE_PERMISSION join (composite key) — READ_WRITE
QR-SEC-0019 SAVE/UPDATE refresh token; FIND by hashed TOKEN; rotate/revoke (SEQ_SEC_REFRESH_TOKEN) — READ_WRITE
QR-SEC-0020 SAVE/UPDATE reset token; invalidate prior active; consume (SEQ_SEC_PASSWORD_RESET_TOKEN) — READ_WRITE
QR-SEC-0021 SAVE/UPDATE activation token; consume (SEQ_SEC_ACCOUNT_ACTIVATION_TOKEN) — READ_WRITE
QR-SEC-0022 Lookup resolution for {lookupKey} (runtime codes; no lookup table) — READ_ONLY
QR-SEC-0023 SAVE module (SEQ_SEC_MODULE) — READ_WRITE ⟵ v1.3
QR-SEC-0024 FIND_ONE / FIND_BY_CRITERIA modules; EXISTS active module by ID (page.moduleFk validation) — READ_ONLY, paged ⟵ v1.3
QR-SEC-0025 UPDATE module / EXISTS moduleCode (RULE-SEC-010) — READ_WRITE/READ_ONLY ⟵ v1.3
QR-SEC-0026 SAVE/DELETE SEC_ROLE_MODULE join (composite key) — Tier-1 grant/revoke — READ_WRITE ⟵ v1.3
QR-SEC-0027 EXISTS SEC_ROLE_MODULE(roleId, moduleId) — resolves module from a permission's page (SEC_PERMISSION→SEC_PAGE.MODULE_FK); RULE-SEC-014 grant pre-check — READ_ONLY ⟵ v1.3
QR-SEC-0028 FIND distinct active modules granted to a user (SEC_USER_ROLE→SEC_ROLE_MODULE→SEC_MODULE); dashboard (API-SEC-019) — READ_ONLY ⟵ v1.3
QR-SEC-0029 EXISTS any SEC_ROLE_PERMISSION for a role within a module (SEC_ROLE_PERMISSION→SEC_PERMISSION→SEC_PAGE.MODULE_FK); RULE-SEC-014 revoke-block — READ_ONLY ⟵ v1.3
Join governance: NEVER join to a lookups table (there is none). Permitted joins (v1.3): SEC_PERMISSION→SEC_PAGE (module resolution), SEC_ROLE_PERMISSION→SEC_PERMISSION→SEC_PAGE, SEC_USER_ROLE→SEC_ROLE_MODULE→SEC_MODULE — all for RULE-SEC-013/014 and dashboard, per DRV-006.
══════════════════════════════════════════════════════════════════

---

## SECTION C — REGISTRY UPDATE BLOCK
══════════════════════════════════════════════════════════════════
## REGISTRY UPDATE — 2026-09-02 (Amendment — SEC two-tier RBAC/SSO)
Source: Project 3.1 PASS 1 (Backend) | Feature Code SEC-001 | DBS-SEC-001 | Plan PLAN-SEC-001 (amended)
Upstream : srs-SEC v1.3 + db-script-SEC v1.1 (← prd-SEC v2 / domain-profile-ERP.md v2)
New Entities (in-plan): ENTITY-SEC-010 (Module), ENTITY-SEC-011 (RoleModule) — already registered in master-registry §5 by P2 reconciliation.
New Fields: FIELD-0049..0056 (↔ DBF-0049..0056) — 56 total.
New APIs: API-SEC-017 (assign module), API-SEC-018 (revoke module), API-SEC-019 (dashboard modules), API-SEC-020 (modules CRUD) — API total 20.
New Rules: RULE-SEC-013 (Tier-1 grant → dashboard filter + prerequisite), RULE-SEC-014 (derivation — no orphan screen permission) — rule total 14.
New Errors: ERR-0013 (422), ERR-0014 (409) — error total 14. Extended: ERR-0009 (+module code), ERR-0012 (+API-SEC-017/018/019/020).
New QR-IDs: QR-SEC-0023..0029 — QR total 29.
New Screen: SCR-SEC-004 (SEC_MODULES); updated SCR-SEC-002 (Tier-1 assignment + Tier-2 derivation).
New DRV: DRV-006/007/008.
XM-IDs Open: None outbound (Tier-1 intra-SEC; inbound XM-FILE-001/XM-NOTIF-001 owned by consumers).
OQ-IDs Open: None.
Gate Status: ALIGN-BE PASSED ✓ | Next: regenerate backend-test-plan.md + test-execution-manifest.md (CONTRACT-13 / §16A), then Project 4.1 → Pipeline Grid SEC · P3.1 = done.
Registry Event Log line to append (for human registry maintainer — P3.1 does not write master-registry.md directly):
  | 2026-09-02 | P3.1 amendment — backend-execution-plan-SEC.md (PLAN-SEC-001): SEC two-tier RBAC/SSO per srs-SEC v1.3 + db-script-SEC v1.1; +API-SEC-017..020, +RULE-SEC-013/014, +ERR-0013/0014, +QR-SEC-0023..0029, +FIELD-0049..0056, +SCR-SEC-004; ALIGN-BE ✓. Downstream: regenerate backend-test-plan + manifest → P4.1 | P3.1 |
══════════════════════════════════════════════════════════════════

---

## SECTION D — TC COVERAGE MATRIX SUMMARY (backend)
══════════════════════════════════════════════════════════════════
⚠ Full TC blocks (Given/When/Then) live in backend-test-plan.md — this is the summary consumed by ALIGN-BE. **backend-test-plan.md + test-execution-manifest.md are now STALE and MUST be regenerated in the same/next session before P4.1 (CONTRACT-13 / §16A regeneration rule).**
RULE-ID COVERAGE:
RULE-SEC-001 │ TC-BE-SEC-001 │ TC-BE-SEC-002 │ COVERED ✓
RULE-SEC-002 │ TC-BE-SEC-003 │ TC-BE-SEC-004 │ COVERED ✓
RULE-SEC-003 │ TC-BE-SEC-005 │ TC-BE-SEC-006 │ COVERED ✓
RULE-SEC-004 │ TC-BE-SEC-007 │ — │ COVERED ✓ (hash-only assertion; no violation path)
RULE-SEC-005 │ TC-BE-SEC-008 │ TC-BE-SEC-009 │ COVERED ✓
RULE-SEC-006 │ TC-BE-SEC-010 │ TC-BE-SEC-011 │ COVERED ✓
RULE-SEC-007 │ TC-BE-SEC-012 │ TC-BE-SEC-013 │ COVERED ✓
RULE-SEC-008 │ TC-BE-SEC-014 │ TC-BE-SEC-015 │ COVERED ✓
RULE-SEC-009 │ TC-BE-SEC-016 │ TC-BE-SEC-017 │ COVERED ✓
RULE-SEC-010 │ TC-BE-SEC-018 │ TC-BE-SEC-019 │ COVERED ✓
RULE-SEC-011 │ TC-BE-SEC-020 │ — │ COVERED ✓ (generation assertion)
RULE-SEC-012 │ TC-BE-SEC-021 │ — │ COVERED ✓ (no-cascade assertion)
RULE-SEC-013 │ TC-BE-SEC-038 │ — │ COVERED ✓ ⟵v1.3 (dashboard returns only granted modules; display filter — no violation path)
RULE-SEC-014 │ TC-BE-SEC-039 (happy: grant with module) │ TC-BE-SEC-040 (violation: grant w/o module → ERR-0013) │ COVERED ✓ ⟵v1.3
             │ TC-BE-SEC-041 (violation: revoke module w/ dependents → ERR-0014) │ │ COVERED ✓ ⟵v1.3
Rule coverage: 14/14 — 0 gaps.
API-ID COVERAGE: API-SEC-001..016 each ≥1 happy-path TC (TC-BE-SEC-022..037); API-SEC-017 (TC-BE-SEC-042), API-SEC-018 (TC-BE-SEC-043), API-SEC-019 (TC-BE-SEC-044), API-SEC-020 (TC-BE-SEC-045) ⟵v1.3 — 20/20 covered.
Total backend TCs: 45 (was 37; +8 for 2 new rules incl. 2 violation paths + 4 new API happy paths). No Boundary TCs added (no Test-Hint/threshold triggers) — within TP-SEC-1/2 derivation, over-engineering guard respected.
DEFERRED TC REGISTRY: (none)
══════════════════════════════════════════════════════════════════
Gate SECTION D: PASSED ✓

---

## AGENT HANDOFF SUMMARY (BACKEND) — not a phase
Agent-ready. Rewrite all QRC from scratch; apply RULE-SEC-001..014 in domain/; store passwords & tokens hashed;
auto-generate 4 permissions per page (RULE-SEC-011); every page belongs to a module (moduleFk NOT NULL).
Two-tier RBAC (v1.3): Tier-1 = grant module to role (API-SEC-017/018 → SEC_ROLE_MODULE) → dashboard filter via API-SEC-019 (RULE-SEC-013); Tier-2 = grant screen permission (API-SEC-015 → SEC_ROLE_PERMISSION) ONLY if the role holds the page's module (RULE-SEC-014 → QR-SEC-0027/ERR-0013); revoke module BLOCKS if screen permissions remain in it (QR-SEC-0029/ERR-0014). Enforce CORE-9 @PreAuthorize per screen (Tier-2 real enforcement); the module grant is a display filter + prerequisite, NOT a runtime gate. Single internal JWT = SSO (auth-only). Seed SEC module + SEC_ROLE_MODULE(SYS_ADMIN,SEC) before Tier-2 grants. Publish CU events for reset/activation (never call NOTIF directly); RULE-SEC-012 deactivation = no cascade. Regenerate backend-test-plan + test-execution-manifest before P4.1; run api-doc-generator before PASS 2.

*End of backend-execution-plan.md — SEC — PLAN-SEC-001 — v1.3 re-align (two-tier RBAC + internal SSO) — ALIGN-BE ✓*
*Upstream: srs-SEC v1.3 + db-script-SEC v1.1 · Downstream: backend-test-plan/manifest regen → P4.1*
