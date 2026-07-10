-- ============================================================================
-- Security Module — Full schema migration + bootstrap seed data
-- Run manually by DBA (psql / pgAdmin). Not applied automatically — no Flyway
-- migration runner is wired up for erp-security. Safe to re-run (idempotent).
--
-- Order matters and is enforced by running this as ONE transaction:
--   STEP 1 — Drop legacy/unused tables FIRST (before anything references them)
--   STEP 2 — Rename PK/FK columns to the entity-specific standard convention
--   STEP 3 — Persist ROLE_CODE/DESCRIPTION on ROLES (previously @Transient)
--   STEP 4 — Bootstrap seed data (single admin user/role, real frontend pages)
-- ============================================================================

BEGIN;

-- ============================================================================
-- STEP 1 — Drop legacy/unused tables
-- ============================================================================
-- SEC_MENU_ITEM exists in some environments' schema history but is never read
-- or written by the application — MenuService builds menus dynamically from
-- SEC_PAGES + permissions. Confirmed zero code references before dropping.

DROP TABLE IF EXISTS SEC_MENU_ITEM;

-- ============================================================================
-- STEP 2 — Rename PK/FK columns to standard convention
-- ============================================================================
-- Convention: the PK COLUMN itself is named after its own table (USERS_PK,
-- ROLES_PK, PERMISSIONS_PK, REFRESH_TOKENS_PK, SEC_PAGES_PK) — matching how
-- erp-org/erp-masterdata already name PK columns, per this project's own
-- create-entity governance rule (A.1.2: PK column name must be entity-
-- specific, never generic "ID" or hardcoded "ID_PK").

ALTER TABLE USERS RENAME COLUMN ID TO USERS_PK;

ALTER TABLE ROLES RENAME COLUMN ID TO ROLES_PK;

ALTER TABLE PERMISSIONS RENAME COLUMN ID TO PERMISSIONS_PK;

ALTER TABLE REFRESH_TOKENS RENAME COLUMN ID TO REFRESH_TOKENS_PK;
ALTER TABLE REFRESH_TOKENS RENAME COLUMN USER_ID TO USER_ID_FK;

-- SEC_PAGES already used ID_PK (not a generic "ID") but that was still a
-- generic per-table name rather than entity-specific — rename it too.
ALTER TABLE SEC_PAGES RENAME COLUMN ID_PK TO SEC_PAGES_PK;

ALTER TABLE USER_ROLES RENAME COLUMN USER_ID TO USER_ID_FK;
ALTER TABLE USER_ROLES RENAME COLUMN ROLE_ID TO ROLE_ID_FK;

ALTER TABLE ROLE_PERMISSIONS RENAME COLUMN ROLE_ID TO ROLE_ID_FK;
ALTER TABLE ROLE_PERMISSIONS RENAME COLUMN PERM_ID TO PERM_ID_FK;

-- No change needed: PERMISSIONS.PAGE_ID_FK (FK column name, unaffected by the
-- referenced table's PK column being renamed — only Permission.java's
-- @JoinColumn(referencedColumnName = ...) changes, a Java-side concern).

-- Name the PK constraints to match the same per-table convention. Plain
-- JPA/Hibernate has no annotation to assign this name, so it was never
-- enforced anywhere; the live constraint may currently be unnamed, Postgres-
-- default-named (e.g. users_pkey), or already correct — this block finds
-- whatever it actually is and renames it only if it doesn't already match.
DO $$
DECLARE
    rec RECORD;
    current_pk_name text;
    actual_table regclass;
BEGIN
    FOR rec IN
        SELECT * FROM (VALUES
            ('USERS',            'USERS_PK'),
            ('ROLES',            'ROLES_PK'),
            ('PERMISSIONS',      'PERMISSIONS_PK'),
            ('REFRESH_TOKENS',   'REFRESH_TOKENS_PK'),
            ('SEC_PAGES',        'SEC_PAGES_PK'),
            ('USER_ROLES',       'USER_ROLES_PK'),
            ('ROLE_PERMISSIONS', 'ROLE_PERMISSIONS_PK')
        ) AS t(table_name, desired_pk_name)
    LOOP
        -- Casting text to regclass folds unquoted case (e.g. 'USERS' -> users
        -- table), but %I on the raw literal below would NOT — it preserves
        -- the literal's case verbatim, producing a broken quoted identifier
        -- like "USERS" when the real table is lowercase. Resolve the actual
        -- object once via regclass and reuse it (via %s, already correctly
        -- quoted/cased) instead of re-quoting the original literal.
        actual_table := rec.table_name::regclass;

        SELECT conname INTO current_pk_name
        FROM pg_constraint
        WHERE conrelid = actual_table AND contype = 'p';

        IF current_pk_name IS NOT NULL AND current_pk_name <> rec.desired_pk_name THEN
            EXECUTE format('ALTER TABLE %s RENAME CONSTRAINT %I TO %I',
                actual_table, current_pk_name, rec.desired_pk_name);
        END IF;
    END LOOP;
END $$;

-- ============================================================================
-- STEP 3 — Persist ROLE_CODE and DESCRIPTION on ROLES
-- ============================================================================
-- Previously @Transient in Role.java — accepted by CreateRoleRequest/RoleDto
-- but silently discarded. NAME historically held the uppercased role code
-- (RoleService.createRole() conflated the two), so it's the correct backfill
-- source for any rows that already exist at this point.

ALTER TABLE ROLES ADD COLUMN IF NOT EXISTS ROLE_CODE VARCHAR(60);
ALTER TABLE ROLES ADD COLUMN IF NOT EXISTS DESCRIPTION VARCHAR(500);

UPDATE ROLES SET ROLE_CODE = NAME WHERE ROLE_CODE IS NULL;

ALTER TABLE ROLES ALTER COLUMN ROLE_CODE SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'UK_ROLES_ROLE_CODE'
    ) THEN
        ALTER TABLE ROLES ADD CONSTRAINT UK_ROLES_ROLE_CODE UNIQUE (ROLE_CODE);
    END IF;
END $$;

-- ============================================================================
-- STEP 4 — Bootstrap seed data (single admin account)
-- ============================================================================
-- Seeds exactly ONE user (admin/admin) with ONE role holding every
-- permission, plus the SEC_PAGES/PERMISSIONS rows for the pages that are
-- actually wired up in the frontend today (security/users, security/role-
-- access, security/pages-registry, master-data/master-lookups,
-- finance/gl/accounts).
--
-- ⚠️ DESTRUCTIVE: this step first DELETEs every existing row from all 7
-- tables below (child tables first, to satisfy FK constraints), then
-- re-inserts the bootstrap set — a full wipe-and-reseed, not a merge. ANY
-- data manually added to these tables (extra users, roles, pages,
-- permissions, refresh tokens) is destroyed, not just the rows this script
-- creates. The ON CONFLICT DO NOTHING on each INSERT is now just a safety
-- net (tables are empty going in) rather than the primary idempotency
-- mechanism.
--
-- ⚠️ CREDENTIALS: username 'admin' / password 'admin' (BCrypt). Weak,
-- well-known credentials suitable ONLY for local/dev bootstrap — never run
-- this step against a staging or production database.

-- Delete child-to-parent to satisfy FK constraints. REFRESH_TOKENS is
-- included even though this step never inserts into it — it FKs to USERS
-- (USER_ID_FK NOT NULL, no cascade), so any existing tokens for the admin
-- user would block the USERS delete below.
DELETE FROM REFRESH_TOKENS;
DELETE FROM USER_ROLES;
DELETE FROM ROLE_PERMISSIONS;
DELETE FROM USERS;
DELETE FROM PERMISSIONS;
DELETE FROM ROLES;
DELETE FROM SEC_PAGES;

INSERT INTO SEC_PAGES (SEC_PAGES_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, ICON, MODULE, PARENT_ID_FK, DISPLAY_ORDER, IS_ACTIVE, DESCRIPTION, CREATED_AT, CREATED_BY)
SELECT nextval('SEC_PAGES_SEQ'), v.page_code, v.name_ar, v.name_en, v.route, v.icon, v.module, NULL, v.display_order, 1, v.description, TIMESTAMP '2025-11-03 08:00:00', 'SYSTEM'
FROM (VALUES
    ('USER',          'إدارة المستخدمين',      'User Management',   '/security/users',            'user',       'SECURITY',   1, 'إدارة مستخدمي النظام وحساباتهم'),
    ('ROLE',          'إدارة الأدوار',         'Role Management',   '/security/role-access',      'users',      'SECURITY',   2, 'إعداد الأدوار والتحكم في الوصول'),
    ('PAGE',          'إدارة الصفحات',         'Pages Management',  '/security/pages-registry',   'file-text',  'SECURITY',   3, 'إدارة صفحات النظام المسجلة'),
    ('MASTER_LOOKUP', 'قوائم البحث الرئيسية',  'Master Lookups',    '/master-data/master-lookups','database',   'MASTERDATA', 1, 'إدارة البيانات المرجعية وجداول البحث'),
    ('GL_ACCOUNT',    'حسابات الأستاذ العام',  'GL Accounts',       '/finance/gl/accounts',       'calculator', 'FINANCE',    1, 'إدارة دليل الحسابات')
) AS v(page_code, name_ar, name_en, route, icon, module, display_order, description)
ON CONFLICT (PAGE_CODE) DO NOTHING;

-- Standard 4 (VIEW/CREATE/UPDATE/DELETE) per page, matching the real
-- constants in SecurityPermissions.java for these 5 groups.
INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_AT, CREATED_BY)
SELECT v.name, pg.SEC_PAGES_PK, v.ptype, TIMESTAMP '2025-11-03 08:05:00', 'SYSTEM'
FROM (VALUES
    ('PERM_USER_VIEW','USER','VIEW'), ('PERM_USER_CREATE','USER','CREATE'), ('PERM_USER_UPDATE','USER','UPDATE'), ('PERM_USER_DELETE','USER','DELETE'),
    ('PERM_ROLE_VIEW','ROLE','VIEW'), ('PERM_ROLE_CREATE','ROLE','CREATE'), ('PERM_ROLE_UPDATE','ROLE','UPDATE'), ('PERM_ROLE_DELETE','ROLE','DELETE'),
    ('PERM_PAGE_VIEW','PAGE','VIEW'), ('PERM_PAGE_CREATE','PAGE','CREATE'), ('PERM_PAGE_UPDATE','PAGE','UPDATE'), ('PERM_PAGE_DELETE','PAGE','DELETE'),
    ('PERM_MASTER_LOOKUP_VIEW','MASTER_LOOKUP','VIEW'), ('PERM_MASTER_LOOKUP_CREATE','MASTER_LOOKUP','CREATE'), ('PERM_MASTER_LOOKUP_UPDATE','MASTER_LOOKUP','UPDATE'), ('PERM_MASTER_LOOKUP_DELETE','MASTER_LOOKUP','DELETE'),
    ('PERM_GL_ACCOUNT_VIEW','GL_ACCOUNT','VIEW'), ('PERM_GL_ACCOUNT_CREATE','GL_ACCOUNT','CREATE'), ('PERM_GL_ACCOUNT_UPDATE','GL_ACCOUNT','UPDATE'), ('PERM_GL_ACCOUNT_DELETE','GL_ACCOUNT','DELETE')
) AS v(name, page_code, ptype)
JOIN SEC_PAGES pg ON pg.PAGE_CODE = v.page_code
ON CONFLICT (NAME) DO NOTHING;

-- System-level permission, not tied to any page.
INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_AT, CREATED_BY)
VALUES ('PERM_SYSTEM_ADMIN', NULL, NULL, TIMESTAMP '2025-11-03 08:06:00', 'SYSTEM')
ON CONFLICT (NAME) DO NOTHING;

-- Single role, holding every permission that exists. ROLE_CODE is required
-- (NOT NULL as of STEP 3) — set equal to NAME, matching this module's
-- create-role convention of an uppercase code.
INSERT INTO ROLES (NAME, ROLE_CODE, DESCRIPTION, IS_ACTIVE, CREATED_AT, CREATED_BY)
VALUES ('SUPER_ADMIN', 'SUPER_ADMIN', 'Full system access - all permissions', 1, TIMESTAMP '2025-11-03 08:00:00', 'SYSTEM')
ON CONFLICT (NAME) DO NOTHING;

INSERT INTO ROLE_PERMISSIONS (ROLE_ID_FK, PERM_ID_FK)
SELECT r.ROLES_PK, p.PERMISSIONS_PK
FROM ROLES r
CROSS JOIN PERMISSIONS p
WHERE r.NAME = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

-- Password 'admin' hashed with BCrypt (see header warning — dev/local only).
INSERT INTO USERS (USERNAME, PASSWORD, ENABLED, CREATED_AT, CREATED_BY)
VALUES ('admin', '$2y$10$IRtwox0AP57TtJmAojWfKuoyCxgj.nqjevQuXm1.hDONGqrVLvnyG', 1, TIMESTAMP '2025-11-03 08:10:00', 'SYSTEM')
ON CONFLICT (USERNAME) DO NOTHING;

INSERT INTO USER_ROLES (USER_ID_FK, ROLE_ID_FK)
SELECT u.USERS_PK, r.ROLES_PK
FROM USERS u
JOIN ROLES r ON r.NAME = 'SUPER_ADMIN'
WHERE u.USERNAME = 'admin'
ON CONFLICT DO NOTHING;

COMMIT;

-- ============================================================================
-- VERIFICATION (read-only, run after COMMIT)
-- ============================================================================
SELECT 'roles' AS table_name, COUNT(*) AS row_count FROM ROLES
UNION ALL
SELECT 'sec_pages', COUNT(*) FROM SEC_PAGES
UNION ALL
SELECT 'permissions', COUNT(*) FROM PERMISSIONS
UNION ALL
SELECT 'role_permissions', COUNT(*) FROM ROLE_PERMISSIONS
UNION ALL
SELECT 'users', COUNT(*) FROM USERS
UNION ALL
SELECT 'user_roles', COUNT(*) FROM USER_ROLES;
