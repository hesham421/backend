-- Relocated from backend/erp-security/src/main/resources/db/scripts/seed_security_data.sql on 2026-07-06 as part of the non-functional artifact consolidation.
-- See governance-repo/project-artifacts/CONSOLIDATION-INVENTORY.md.

-- ============================================================================
-- SECURITY MODULE — BOOTSTRAP SEED DATA (single admin account)
-- ============================================================================
-- Target   : PostgreSQL 17 (erp_db, schema: public)
-- Source   : governance-repo/modules/security-registry.md (v2.2.0) +
--            erp-security entities + SecurityPermissions.java, cross-checked
--            against the ACTUAL frontend routes in
--            /Users/ezzat/my project/frontend/src/app (see page list below).
-- Idempotent: yes — every INSERT is keyed on the table's real unique
--             constraint via ON CONFLICT DO NOTHING; safe to re-run.
--
-- ⚠️ SCOPE: this seeds exactly ONE user (admin/admin) with ONE role that
-- holds every permission. It intentionally does NOT create the larger
-- multi-role/multi-user cast from the earlier draft of this script — that
-- was replaced per instruction to keep this to a single bootstrap admin.
--
-- ⚠️ IDENTIFIER CASE: entities declare @Column(name="ID")/@Table(name="USERS")
-- in uppercase, but Hibernate emits these UNQUOTED, so Postgres folds them to
-- lowercase physical names (users, roles, sec_pages, permissions, ...).
-- Verified directly against the live schema via \d — this script targets
-- those lowercase names.
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

INSERT INTO sec_pages (id_pk, page_code, name_ar, name_en, route, icon, module, parent_id_fk, display_order, is_active, description, created_at, created_by)
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
SELECT v.name, pg.id_pk, v.ptype, TIMESTAMP '2025-11-03 08:05:00', 'SYSTEM'
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
-- Single role, holding every permission that exists.

INSERT INTO roles (name, is_active, created_at, created_by)
VALUES ('SUPER_ADMIN', 1, TIMESTAMP '2025-11-03 08:00:00', 'SYSTEM')
ON CONFLICT (name) DO NOTHING;

-- ============================================================================
-- SECTION: ROLE_PERMISSIONS
-- ============================================================================

INSERT INTO role_permissions (role_id, perm_id)
SELECT r.id, p.id
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

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
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
