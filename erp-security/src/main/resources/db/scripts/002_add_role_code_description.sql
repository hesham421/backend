-- ============================================================================
-- Security Module — Persist ROLE_CODE and DESCRIPTION on ROLES
-- GAP: registry-security.md §8.2 — roleCode/description were @Transient
-- Run manually by DBA (psql / pgAdmin), AFTER 001_rename_pk_fk_to_standard.sql.
-- ============================================================================

ALTER TABLE ROLES ADD COLUMN ROLE_CODE VARCHAR(60);
ALTER TABLE ROLES ADD COLUMN DESCRIPTION VARCHAR(500);

-- Backfill: historically NAME was populated with the uppercased role code
-- (see RoleService.createRole() prior to this change), so it is the correct
-- source value for the one-time backfill.
UPDATE ROLES SET ROLE_CODE = NAME WHERE ROLE_CODE IS NULL;

ALTER TABLE ROLES ALTER COLUMN ROLE_CODE SET NOT NULL;
ALTER TABLE ROLES ADD CONSTRAINT UK_ROLES_ROLE_CODE UNIQUE (ROLE_CODE);
