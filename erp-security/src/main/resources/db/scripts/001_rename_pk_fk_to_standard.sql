-- ============================================================================
-- Security Module — Rename PK/FK columns to standard convention (ID_PK / *_FK)
-- GAP: registry-security.md §8.5 — PK naming convention deviation
-- Run manually by DBA (psql / pgAdmin). Not applied automatically — no Flyway
-- migration runner is wired up for erp-security.
-- ============================================================================

ALTER TABLE USERS RENAME COLUMN ID TO ID_PK;

ALTER TABLE ROLES RENAME COLUMN ID TO ID_PK;

ALTER TABLE PERMISSIONS RENAME COLUMN ID TO ID_PK;

ALTER TABLE REFRESH_TOKENS RENAME COLUMN ID TO ID_PK;
ALTER TABLE REFRESH_TOKENS RENAME COLUMN USER_ID TO USER_ID_FK;

ALTER TABLE USER_ROLES RENAME COLUMN USER_ID TO USER_ID_FK;
ALTER TABLE USER_ROLES RENAME COLUMN ROLE_ID TO ROLE_ID_FK;

ALTER TABLE ROLE_PERMISSIONS RENAME COLUMN ROLE_ID TO ROLE_ID_FK;
ALTER TABLE ROLE_PERMISSIONS RENAME COLUMN PERM_ID TO PERM_ID_FK;

-- No change needed: PERMISSIONS.PAGE_ID_FK, SEC_PAGES.* (already compliant)
