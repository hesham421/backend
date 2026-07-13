-- ============================================================================
-- Security Module — DataScope extension + Self-Service Auth gap package
-- PLAN-SEC-002 / DBS-SEC-001 — Phase SEC (Section 8.2 Security Seed Data Task)
-- Run manually by DBA (psql / pgAdmin). Not applied automatically — no Flyway
-- migration runner is wired up for erp-security. Safe to re-run (idempotent
-- via ON CONFLICT DO NOTHING, additive only — no DELETE like script 001's
-- bootstrap step).
--
-- Source: governance-repo/modules/SECURITY/gaps/execution-plan-SEC-gaps.md
--         Section 8.1 (Permissions Matrix) + 8.2 (Security Seed Data Task).
--
-- Adds SEC_PAGES row for SCR-SEC-006 (User Profiles / Data Scope) and its
-- 3 permission records: VIEW / CREATE / UPDATE — deliberately NO DELETE
-- (profiles deactivate via isActiveFl through UPDATE, never DELETE, per
-- Section 8.1). Inserted directly via SQL rather than through PageService's
-- createPage() API: that service unconditionally generates all 4 CRUD
-- permissions (PageService.createPermissionRecords, no flag to suppress
-- DELETE) — flagged as a real gap vs. this plan's "3 permissions, no DELETE"
-- requirement in HANDOFF-PHASE-10-SEC.md. Direct SQL sidesteps it cleanly
-- without a PageService code change that's out of this phase's scope.
--
-- PARENT_ID_FK left NULL: queried the live sec_pages table (2026-07-10) —
-- every existing row (USER/ROLE/PAGE/PERMISSION, all module=SECURITY) has
-- PARENT_ID_FK NULL. The "الأمان" (Security) sidebar group referenced in the
-- plan is a frontend i18n label keyed off MODULE='SECURITY', not an actual
-- SEC_PAGES row — there is no real parent page to reference. Flagged as a
-- discrepancy vs. the plan's literal "parent = existing 'الأمان' group page"
-- wording in HANDOFF-PHASE-10-SEC.md; following existing data (NULL parent,
-- MODULE grouping) rather than inventing a parent page.
--
-- Also does NOT re-seed DATA_ACCESS_LEVEL lookup values (LOV-SEC-002) —
-- confirmed already present and active (ALL / BRANCH_AND_CHILDREN /
-- BRANCH_ONLY) via script 002 BLOCK 8, verified live 2026-07-10.
-- ============================================================================

BEGIN;

-- ============================================================================
-- BLOCK 1: SEC_PAGES row (SCR-SEC-006 — User Profiles / Data Scope)
-- ============================================================================
INSERT INTO SEC_PAGES (SEC_PAGES_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, ICON, MODULE, PARENT_ID_FK, DISPLAY_ORDER, IS_ACTIVE, DESCRIPTION, CREATED_AT, CREATED_BY)
SELECT nextval('SEC_PAGES_SEQ'), 'USER_PROFILE', 'ملفات المستخدمين / نطاق البيانات', 'User Profiles / Data Scope',
       '/security/user-profiles', 'user-check', 'SECURITY', NULL, 5, 1,
       'إدارة ملفات تعريف المستخدمين وإسناد الفروع - User profile / branch assignment management (PLAN-SEC-002 SCR-SEC-006)',
       now(), 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM SEC_PAGES WHERE PAGE_CODE = 'USER_PROFILE');

-- ============================================================================
-- BLOCK 2: Permissions — VIEW / CREATE / UPDATE only (no DELETE — Section 8.1)
-- ============================================================================
INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_AT, CREATED_BY)
SELECT v.name, pg.SEC_PAGES_PK, v.ptype, now(), 'SYSTEM'
FROM (VALUES
    ('PERM_USER_PROFILE_VIEW',   'USER_PROFILE', 'VIEW'),
    ('PERM_USER_PROFILE_CREATE', 'USER_PROFILE', 'CREATE'),
    ('PERM_USER_PROFILE_UPDATE', 'USER_PROFILE', 'UPDATE')
) AS v(name, page_code, ptype)
JOIN SEC_PAGES pg ON pg.PAGE_CODE = v.page_code
ON CONFLICT (NAME) DO NOTHING;

-- ============================================================================
-- BLOCK 3: Grant the 3 new permissions to SUPER_ADMIN
-- ============================================================================
-- Targeted grant, NOT a full ROLES x PERMISSIONS re-seed — script 001's
-- bootstrap CROSS JOIN only ran once at initial setup (and first wiped
-- ROLE_PERMISSIONS); permissions added afterward (this + the Phase 3
-- PERM_BRANCH_VIEW grant, see HANDOFF-PHASE-3-SVC-API.md) must be granted
-- explicitly or SUPER_ADMIN silently does not receive them.
INSERT INTO ROLE_PERMISSIONS (ROLE_ID_FK, PERM_ID_FK)
SELECT r.ROLES_PK, p.PERMISSIONS_PK
FROM ROLES r
CROSS JOIN PERMISSIONS p
WHERE r.NAME = 'SUPER_ADMIN'
  AND p.NAME IN ('PERM_USER_PROFILE_VIEW', 'PERM_USER_PROFILE_CREATE', 'PERM_USER_PROFILE_UPDATE')
ON CONFLICT DO NOTHING;

COMMIT;

-- ============================================================================
-- VERIFICATION (read-only, run after COMMIT)
-- ============================================================================
SELECT sp.page_code, p.name, p.permission_type
FROM SEC_PAGES sp
JOIN PERMISSIONS p ON p.PAGE_ID_FK = sp.SEC_PAGES_PK
WHERE sp.PAGE_CODE = 'USER_PROFILE'
ORDER BY p.permission_type;
