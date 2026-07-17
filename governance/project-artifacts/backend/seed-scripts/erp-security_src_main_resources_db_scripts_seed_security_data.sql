-- Relocated from backend/erp-security/src/main/resources/db/scripts/seed_security_data.sql on 2026-07-06 as part of the non-functional artifact consolidation.
-- See governance-repo/project-artifacts/CONSOLIDATION-INVENTORY.md.
-- Synced 2026-07-08 to the column-naming/schema changes applied by
-- backend/erp-security/src/main/resources/db/scripts/001_security_schema_migration_and_seed.sql
-- (entity-specific PK column names + persisted ROLE_CODE/DESCRIPTION on ROLES).

-- ============================================================================
-- SECURITY MODULE — BOOTSTRAP SEED DATA (single admin account)
-- ============================================================================
-- Target   : PostgreSQL 17 (erp_db, schema: public)
-- Source   : governance-repo/modules/security-registry.md (v2.4.0) +
--            erp-security entities + SecurityPermissions.java, cross-checked
--            against the ACTUAL frontend routes in
--            /Users/ezzat/my project/frontend/src/app (see page list below).
-- Prereq   : Run AFTER 001_security_schema_migration_and_seed.sql (this seed
--            targets the post-migration column names — see below).
-- Idempotent: yes, but as a wipe-and-reseed — see DESTRUCTIVE warning below.
--
-- ⚠️ SCOPE: this seeds exactly ONE user (admin/admin) with ONE role that
-- holds every permission. It intentionally does NOT create the larger
-- multi-role/multi-user cast from the earlier draft of this script — that
-- was replaced per instruction to keep this to a single bootstrap admin.
--
-- ⚠️ DESTRUCTIVE: this script first DELETEs every existing row from all 7
-- tables below (child tables first, to satisfy FK constraints), then
-- re-inserts the bootstrap set — a full wipe-and-reseed, not a merge. ANY
-- data manually added to these tables (extra users, roles, pages,
-- permissions, refresh tokens) is destroyed, not just the rows this script
-- creates. The ON CONFLICT DO NOTHING on each INSERT is now just a safety
-- net (tables are empty going in) rather than the primary idempotency
-- mechanism.
--
-- ⚠️ IDENTIFIER CASE: entities declare @Column(name="USERS_PK")/@Table(name=
-- "USERS") in uppercase, but Hibernate emits these UNQUOTED, so Postgres
-- folds them to lowercase physical names (users, roles, sec_pages,
-- permissions, ...). Verified directly against the live schema via \d — this
-- script targets those lowercase names.
--
-- ⚠️ PK/FK COLUMN NAMES: 001_security_schema_migration_and_seed.sql renames
-- every PK column to its entity-specific name (USERS_PK, ROLES_PK,
-- PERMISSIONS_PK, REFRESH_TOKENS_PK, SEC_PAGES_PK — folded to users_pk,
-- roles_pk, permissions_pk, refresh_tokens_pk, sec_pages_pk) and every FK
-- column to the *_FK suffix convention (USER_ID_FK, ROLE_ID_FK, PERM_ID_FK —
-- folded to user_id_fk, role_id_fk, perm_id_fk). This script uses those
-- post-migration names; it will fail against a database that hasn't run that
-- migration yet.
--
-- ⚠️ ROLES.ROLE_CODE / ROLES.DESCRIPTION: previously @Transient in Role.java
-- (accepted by CreateRoleRequest/RoleDto but silently discarded). Now real,
-- persisted columns (ROLE_CODE is NOT NULL with a unique constraint) as of
-- 001_security_schema_migration_and_seed.sql — this seed sets ROLE_CODE
-- equal to NAME for SUPER_ADMIN, matching this module's create-role
-- convention of an uppercase code.
--
-- ⚠️ CREDENTIALS: username 'admin' / password 'admin' (BCrypt, strength 10,
-- $2y$ prefix — accepted by Spring's BCryptPasswordEncoder). This is a weak,
-- well-known credential pair suitable ONLY for local/dev bootstrap — never
-- run this script against a staging or production database.
-- ============================================================================

BEGIN;

-- ============================================================================
-- SECTION: SEC_PAGES — verified against real frontend routes
-- ============================================================================
-- Cross-checked route-by-route in frontend/src/app:
--   app-routing.module.ts            → mounts 'security', 'master-data', 'finance'
--   modules/security/security-routing.module.ts
--     /security/users           (data.permission = PERM_USER_VIEW)
--     /security/pages-registry  (data.permission = PERM_PAGE_VIEW/CREATE/UPDATE)
--     /security/role-access     (data.permission = PERM_ROLE_VIEW/CREATE/UPDATE)
--   modules/master-data/master-data-routing.module.ts
--     /master-data/master-lookups (data.permission = PERM_MASTER_LOOKUP_VIEW/CREATE/UPDATE)
--   modules/finance/finance-routing.module.ts
--     /finance/gl/accounts        (data.permission = PERM_GL_ACCOUNT_VIEW)
-- No other module (inventory, procurement, sales, hr, maintenance, reports)
-- has any permission-guarded route yet, and no ORG-module screens exist in
-- the frontend despite backend permission constants for them (REGION,
-- BRANCH, DEPARTMENT, ...) — those are backend-ready but not yet wired to a
-- screen, so they are intentionally NOT seeded as pages here.
-- Arabic/English names pulled verbatim from assets/i18n/ar.json + en.json
-- (PAGES.TITLE, MASTER_LOOKUPS.TITLE, ROLES.TITLE, DASHBOARD.* keys).

-- Delete child-to-parent to satisfy FK constraints. refresh_tokens is
-- included even though this script never inserts into it — it FKs to users
-- (user_id_fk NOT NULL, no cascade), so any existing tokens for the admin
-- user would block the users delete below.
DELETE FROM refresh_tokens;
DELETE FROM user_roles;
DELETE FROM role_permissions;
DELETE FROM users;
DELETE FROM permissions;
DELETE FROM roles;
DELETE FROM sec_pages;

INSERT INTO sec_pages (sec_pages_pk, page_code, name_ar, name_en, route, icon, module, parent_id_fk, display_order, is_active, description, created_at, created_by)
SELECT nextval('sec_pages_seq'), v.page_code, v.name_ar, v.name_en, v.route, v.icon, v.module, NULL, v.display_order, 1, v.description, TIMESTAMP '2025-11-03 08:00:00', 'SYSTEM'
FROM (VALUES
    ('USER',          'إدارة المستخدمين',      'User Management',   '/security/users',            'user',       'SECURITY',   1, 'إدارة مستخدمي النظام وحساباتهم'),
    ('ROLE',          'إدارة الأدوار',         'Role Management',   '/security/role-access',      'users',      'SECURITY',   2, 'إعداد الأدوار والتحكم في الوصول'),
    ('PAGE',          'إدارة الصفحات',         'Pages Management',  '/security/pages-registry',   'file-text',  'SECURITY',   3, 'إدارة صفحات النظام المسجلة'),
    ('MASTER_LOOKUP', 'قوائم البحث الرئيسية',  'Master Lookups',    '/master-data/master-lookups','database',   'MASTERDATA', 1, 'إدارة البيانات المرجعية وجداول البحث'),
    ('GL_ACCOUNT',    'حسابات الأستاذ العام',  'GL Accounts',       '/finance/gl/accounts',       'calculator', 'FINANCE',    1, 'إدارة دليل الحسابات')
) AS v(page_code, name_ar, name_en, route, icon, module, display_order, description)
ON CONFLICT (page_code) DO NOTHING;

-- ============================================================================
-- SECTION: PERMISSIONS
-- ============================================================================
-- Standard 4 (VIEW/CREATE/UPDATE/DELETE) per page, per business rule 11 —
-- matches the real constants in SecurityPermissions.java for these 5 groups.

INSERT INTO permissions (name, page_id_fk, permission_type, created_at, created_by)
SELECT v.name, pg.sec_pages_pk, v.ptype, TIMESTAMP '2025-11-03 08:05:00', 'SYSTEM'
FROM (VALUES
    ('PERM_USER_VIEW','USER','VIEW'), ('PERM_USER_CREATE','USER','CREATE'), ('PERM_USER_UPDATE','USER','UPDATE'), ('PERM_USER_DELETE','USER','DELETE'),
    ('PERM_ROLE_VIEW','ROLE','VIEW'), ('PERM_ROLE_CREATE','ROLE','CREATE'), ('PERM_ROLE_UPDATE','ROLE','UPDATE'), ('PERM_ROLE_DELETE','ROLE','DELETE'),
    ('PERM_PAGE_VIEW','PAGE','VIEW'), ('PERM_PAGE_CREATE','PAGE','CREATE'), ('PERM_PAGE_UPDATE','PAGE','UPDATE'), ('PERM_PAGE_DELETE','PAGE','DELETE'),
    ('PERM_MASTER_LOOKUP_VIEW','MASTER_LOOKUP','VIEW'), ('PERM_MASTER_LOOKUP_CREATE','MASTER_LOOKUP','CREATE'), ('PERM_MASTER_LOOKUP_UPDATE','MASTER_LOOKUP','UPDATE'), ('PERM_MASTER_LOOKUP_DELETE','MASTER_LOOKUP','DELETE'),
    ('PERM_GL_ACCOUNT_VIEW','GL_ACCOUNT','VIEW'), ('PERM_GL_ACCOUNT_CREATE','GL_ACCOUNT','CREATE'), ('PERM_GL_ACCOUNT_UPDATE','GL_ACCOUNT','UPDATE'), ('PERM_GL_ACCOUNT_DELETE','GL_ACCOUNT','DELETE')
) AS v(name, page_code, ptype)
JOIN sec_pages pg ON pg.page_code = v.page_code
ON CONFLICT (name) DO NOTHING;

-- System-level permission, not tied to any page.
INSERT INTO permissions (name, page_id_fk, permission_type, created_at, created_by)
VALUES ('PERM_SYSTEM_ADMIN', NULL, NULL, TIMESTAMP '2025-11-03 08:06:00', 'SYSTEM')
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- SECTION: ROLES
-- ============================================================================
-- Single role, holding every permission that exists. ROLE_CODE is required
-- (NOT NULL, UK_ROLES_ROLE_CODE) as of 001_security_schema_migration_and_seed.sql
-- — set equal to NAME, matching this module's create-role convention of an
-- uppercase code.

INSERT INTO roles (name, role_code, description, is_active, created_at, created_by)
VALUES ('SUPER_ADMIN', 'SUPER_ADMIN', 'Full system access - all permissions', 1, TIMESTAMP '2025-11-03 08:00:00', 'SYSTEM')
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- SECTION: ROLE_PERMISSIONS
-- ============================================================================

INSERT INTO role_permissions (role_id_fk, perm_id_fk)
SELECT r.roles_pk, p.permissions_pk
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- ============================================================================
-- SECTION: USERS
-- ============================================================================
-- Single bootstrap account. Password 'admin' hashed with BCrypt (see header
-- warning — dev/local only).

INSERT INTO users (username, password, enabled, created_at, created_by)
VALUES ('admin', '$2y$10$IRtwox0AP57TtJmAojWfKuoyCxgj.nqjevQuXm1.hDONGqrVLvnyG', 1, TIMESTAMP '2025-11-03 08:10:00', 'SYSTEM')
ON CONFLICT (username) DO NOTHING;

-- ============================================================================
-- SECTION: USER_ROLES
-- ============================================================================

INSERT INTO user_roles (user_id_fk, role_id_fk)
SELECT u.users_pk, r.roles_pk
FROM users u
JOIN roles r ON r.name = 'SUPER_ADMIN'
WHERE u.username = 'admin'
ON CONFLICT DO NOTHING;

COMMIT;

-- ============================================================================
-- SECTION: VERIFICATION
-- ============================================================================
SELECT 'roles' AS table_name, COUNT(*) AS row_count FROM roles
UNION ALL
SELECT 'sec_pages', COUNT(*) FROM sec_pages
UNION ALL
SELECT 'permissions', COUNT(*) FROM permissions
UNION ALL
SELECT 'role_permissions', COUNT(*) FROM role_permissions
UNION ALL
SELECT 'users', COUNT(*) FROM users
UNION ALL
SELECT 'user_roles', COUNT(*) FROM user_roles;
