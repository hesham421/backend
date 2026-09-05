-- V5 — Master Data (MDM) security-registry seed
-- Source: governance/modules/MDM/packages/backend-execution/SEC-BE/SEC-BE.md (SECURITY SEED DATA REQUIREMENTS)
-- Seeds MDM into the SEC RBAC registry: MDM module, R1 role, MDM_LOOKUP page, 4 CORE-9 permissions, Tier-1 + Tier-2 grants.
-- All FKs resolved by natural key (never a hardcoded id). Surrogate PKs from the V2 sequences.
--
-- Role-code derivation note: the spec names R1 as "مدير البيانات المرجعية / Master Data Administrator"
--   but does NOT provide a role_code. It is created here with the convention-derived code MDM_ADMIN
--   (mirrors the short-uppercase SYS_ADMIN pattern seeded in V3).
-- Pre-existing dependency: R2 = SYS_ADMIN ('مدير النظام' / 'System Administrator') was already seeded by
--   V3 (SEC seed). It is NOT re-inserted here — it is referenced by natural key (ROLE_CODE = 'SYS_ADMIN').

-- 1. SEC_MODULE — module MDM (Tier-1 grantable unit)
INSERT INTO SEC_MODULE (ID, MODULE_CODE, NAME_AR, NAME_EN, IS_ACTIVE_FL, CREATED_BY, CREATED_AT)
VALUES (nextval('SEQ_SEC_MODULE'), 'MDM', 'البيانات المرجعية', 'Master Data', 1, 'SYSTEM', CURRENT_TIMESTAMP);

-- 2. SEC_ROLE — R1 (new): MDM_ADMIN (derived code). SYS_ADMIN already exists (V3) — not inserted here.
INSERT INTO SEC_ROLE (ID, ROLE_CODE, NAME_AR, NAME_EN, IS_ACTIVE_FL, CREATED_BY, CREATED_AT)
VALUES (nextval('SEQ_SEC_ROLE'), 'MDM_ADMIN', 'مدير البيانات المرجعية', 'Master Data Administrator', 1, 'SYSTEM', CURRENT_TIMESTAMP);

-- 3. SEC_PAGE — composite screen SCR-MDM-001, MODULE_FK -> MDM, no parent nav
INSERT INTO SEC_PAGE (ID, PAGE_CODE, NAME_AR, NAME_EN, MODULE_FK, PARENT_PAGE_FK, IS_ACTIVE_FL, CREATED_BY, CREATED_AT)
VALUES (nextval('SEQ_SEC_PAGE'), 'MDM_LOOKUP', 'إدارة القوائم المرجعية', 'Reference Data Lookup Management',
        (SELECT ID FROM SEC_MODULE WHERE MODULE_CODE = 'MDM'), NULL, 1, 'SYSTEM', CURRENT_TIMESTAMP);

-- 4. SEC_PERMISSION — 4 per page (CORE-9). code = PERM_<PAGE_CODE>_<TYPE>; name = <page name> - <TYPE>
--    (matches PermissionGenerationDomainService output so the seed equals runtime generation).
INSERT INTO SEC_PERMISSION (ID, PERMISSION_CODE, PERMISSION_TYPE, NAME_AR, NAME_EN, IS_ACTIVE_FL, PAGE_FK, CREATED_BY, CREATED_AT)
VALUES (nextval('SEQ_SEC_PERMISSION'), 'PERM_MDM_LOOKUP_VIEW', 'VIEW', 'إدارة القوائم المرجعية - VIEW', 'Reference Data Lookup Management - VIEW', 1,
        (SELECT ID FROM SEC_PAGE WHERE PAGE_CODE = 'MDM_LOOKUP'), 'SYSTEM', CURRENT_TIMESTAMP);
INSERT INTO SEC_PERMISSION (ID, PERMISSION_CODE, PERMISSION_TYPE, NAME_AR, NAME_EN, IS_ACTIVE_FL, PAGE_FK, CREATED_BY, CREATED_AT)
VALUES (nextval('SEQ_SEC_PERMISSION'), 'PERM_MDM_LOOKUP_CREATE', 'CREATE', 'إدارة القوائم المرجعية - CREATE', 'Reference Data Lookup Management - CREATE', 1,
        (SELECT ID FROM SEC_PAGE WHERE PAGE_CODE = 'MDM_LOOKUP'), 'SYSTEM', CURRENT_TIMESTAMP);
INSERT INTO SEC_PERMISSION (ID, PERMISSION_CODE, PERMISSION_TYPE, NAME_AR, NAME_EN, IS_ACTIVE_FL, PAGE_FK, CREATED_BY, CREATED_AT)
VALUES (nextval('SEQ_SEC_PERMISSION'), 'PERM_MDM_LOOKUP_UPDATE', 'UPDATE', 'إدارة القوائم المرجعية - UPDATE', 'Reference Data Lookup Management - UPDATE', 1,
        (SELECT ID FROM SEC_PAGE WHERE PAGE_CODE = 'MDM_LOOKUP'), 'SYSTEM', CURRENT_TIMESTAMP);
INSERT INTO SEC_PERMISSION (ID, PERMISSION_CODE, PERMISSION_TYPE, NAME_AR, NAME_EN, IS_ACTIVE_FL, PAGE_FK, CREATED_BY, CREATED_AT)
VALUES (nextval('SEQ_SEC_PERMISSION'), 'PERM_MDM_LOOKUP_DELETE', 'DELETE', 'إدارة القوائم المرجعية - DELETE', 'Reference Data Lookup Management - DELETE', 1,
        (SELECT ID FROM SEC_PAGE WHERE PAGE_CODE = 'MDM_LOOKUP'), 'SYSTEM', CURRENT_TIMESTAMP);

-- 5. SEC_ROLE_MODULE — Tier-1 grants (MDM_ADMIN -> MDM, SYS_ADMIN -> MDM).
--    MUST precede Tier-2 grants (RULE-SEC-014 prerequisite: no orphan screen permission).
INSERT INTO SEC_ROLE_MODULE (ROLE_FK, MODULE_FK)
VALUES ((SELECT ID FROM SEC_ROLE WHERE ROLE_CODE = 'MDM_ADMIN'),
        (SELECT ID FROM SEC_MODULE WHERE MODULE_CODE = 'MDM'));
INSERT INTO SEC_ROLE_MODULE (ROLE_FK, MODULE_FK)
VALUES ((SELECT ID FROM SEC_ROLE WHERE ROLE_CODE = 'SYS_ADMIN'),
        (SELECT ID FROM SEC_MODULE WHERE MODULE_CODE = 'MDM'));

-- 6. SEC_ROLE_PERMISSION — Tier-2: grant all 4 MDM_LOOKUP permissions to both MDM_ADMIN and SYS_ADMIN (R1, R2).
INSERT INTO SEC_ROLE_PERMISSION (ROLE_FK, PERMISSION_FK)
SELECT (SELECT ID FROM SEC_ROLE WHERE ROLE_CODE = 'MDM_ADMIN'), p.ID
FROM SEC_PERMISSION p
WHERE p.PERMISSION_CODE IN (
  'PERM_MDM_LOOKUP_VIEW', 'PERM_MDM_LOOKUP_CREATE', 'PERM_MDM_LOOKUP_UPDATE', 'PERM_MDM_LOOKUP_DELETE'
);
INSERT INTO SEC_ROLE_PERMISSION (ROLE_FK, PERMISSION_FK)
SELECT (SELECT ID FROM SEC_ROLE WHERE ROLE_CODE = 'SYS_ADMIN'), p.ID
FROM SEC_PERMISSION p
WHERE p.PERMISSION_CODE IN (
  'PERM_MDM_LOOKUP_VIEW', 'PERM_MDM_LOOKUP_CREATE', 'PERM_MDM_LOOKUP_UPDATE', 'PERM_MDM_LOOKUP_DELETE'
);
