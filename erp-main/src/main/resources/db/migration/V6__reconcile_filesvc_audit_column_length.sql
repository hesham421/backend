-- ============================================================================
-- Reconciliation Migration — DATABASE_RECONCILIATION_REPORT.md Item R-1
-- Source: governance/modules/FILESVC/P2/db-script.md BLOCK 2 (FILE_CATEGORY /
--         FILE_DOCUMENT column list) vs V3__file_service_schema_and_seed.sql.
--
-- FINDING (category B — EXISTS, DIFFERENT DEFINITION):
-- db-script.md specifies CREATED_BY/UPDATED_BY as VARCHAR(255) for both
-- FILE_CATEGORY and FILE_DOCUMENT (DBF-0009/0011/0024/0026, registry-db-
-- filesvc.md). V3 created both columns as VARCHAR(100) on both tables — the
-- only File Service deviation from db-script.md not already flagged in V3's
-- own header comment (which only documents the FILE_CONTENT nullability and
-- IS_ACTIVE_FL INTEGER-vs-SMALLINT deviations).
--
-- RESOLUTION: per the conflict-resolution priority in the governance
-- reconciliation task (approved Governance spec > existing Flyway
-- implementation), align to the governed VARCHAR(255). This is a pure
-- length-widening ALTER — additive, non-destructive, no data loss, no
-- rewrite of existing values, safe to run against a populated table.
--
-- NOTE FOR ARCHITECTURE AUTHORITY (not resolved here, flagged only):
-- AuditableEntity (erp-common-utils) declares CREATED_BY/UPDATED_BY at
-- length=100 for every entity in the codebase, and every other governed
-- module (ORG, SECURITY, and File Service before this migration) is
-- VARCHAR(100) in Flyway. NOTIFICATION (V5) is the only module whose
-- migration already used VARCHAR(255), matching its own db-script.md but
-- diverging from AuditableEntity's declared length and from every sibling
-- table. Both directions are harmless at runtime (ddl-auto=none in prod;
-- Flyway is the DDL authority; a wider column never rejects a shorter
-- value) but the three-way inconsistency (entity=100, ORG/SEC/pre-fix
-- FILE=100, NOTIFICATION=255) should be settled once, in one direction,
-- by a human decision — see DATABASE_RECONCILIATION_REPORT.md Section 6.
-- This migration only closes the one File Service vs its own governance
-- spec gap; it does not attempt to also narrow NOTIFICATION or widen
-- ORG/SECURITY, both of which would be separate, larger-blast-radius calls.
-- ============================================================================

BEGIN;

ALTER TABLE file_category ALTER COLUMN created_by TYPE VARCHAR(255);
ALTER TABLE file_category ALTER COLUMN updated_by TYPE VARCHAR(255);

ALTER TABLE file_document ALTER COLUMN created_by TYPE VARCHAR(255);
ALTER TABLE file_document ALTER COLUMN updated_by TYPE VARCHAR(255);

COMMIT;
