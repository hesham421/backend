<!-- Source: content OUTSIDE all PHASE markers (trailing / between-phase sections — e.g. Plan Index, DB Alignment Manifest, Error Catalog, Agent Handoff Summary) -->

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