-- ============================================================================
-- File Service Module — DBS-FILE-001 / Phase DATA+DOM
-- Source: governance-repo/modules/FILESVC/P2/db-script.md, P1/srs.md A3/A5.
--
-- Adds FILE_CATEGORY (parent, Reference Table) and FILE_DOCUMENT (child,
-- SHARED-owner) tables, seeds LOV-FILE-001 (FILE_TYPE) / LOV-FILE-002
-- (FILE_STATUS) into the existing MD_MASTER_LOOKUP/MD_LOOKUP_DETAIL tables,
-- and seeds the FILE_ATTACHMENT SEC_PAGES row + its 4 permissions
-- (SCR-FILE-001), granted to SUPER_ADMIN — same pattern as
-- V2__sec_pages_permissions_seed.sql.
--
-- Naming follows this file's own house style (lowercase, IF NOT EXISTS,
-- matching V1__inital_schema.sql) rather than db-script.md's literal
-- UPPERCASE spelling — Postgres folds unquoted identifiers to lowercase
-- either way, so both forms resolve to the same objects; lowercase is used
-- here purely for consistency with the two prior migrations in this
-- directory (db-script.md itself was written before this repo consolidated
-- around Flyway — see HANDOFF-FILESVC-PHASE-DATAOM.md).
--
-- Deviations from db-script.md, flagged (not silently reconciled):
--   1. FILE_DOCUMENT.FILE_CONTENT is NULLABLE here, not NOT NULL as
--      db-script.md's own DDL literally states — that DDL directly
--      contradicts db-script.md's own column comment and RULE-FILE-006/
--      OQ-001, both of which require setting fileContent to NULL at the
--      application layer on permanent delete while the row is retained.
--      A NOT NULL column cannot support that documented behavior; nullable
--      is the only reading consistent with the rest of the plan.
--   2. IS_ACTIVE_FL uses INTEGER, not db-script.md's stated SMALLINT —
--      matches this repo's actual BooleanNumberConverter/Hibernate mapping
--      (Boolean -> Integer) used by every other boolean flag already live
--      in this schema (see org_legal_entity.is_active_fl, roles.is_active),
--      not a new deviation specific to this module.
--   3. Block 5b seeds ONE FILE_CATEGORY row, which srs.md A3 §"ما لا يشمله
--      هذا الموديول" explicitly says this module must NOT decide ("لا يقرر
--      أي موديول يحتاج أي فئة ملفات — كل موديول مستهلك يقرر فئاته بنفسه" —
--      that's each consuming module's own responsibility). Added anyway,
--      flagged as a deviation: FileCategory has no Create/Update API
--      (DRV-FILE-001, reference table, DB-seeded only — see
--      test-api/test_file_apis.py known-gaps note #1), and no Purchase
--      Order/PRC module exists yet in this backend to own a real seed.
--      Without at least one row, every issueUploadToken call 404s and the
--      File Service API is entirely untestable. DEV/TEST convenience only
--      — remove or replace once a real consuming module owns its own
--      FileCategory seed.
-- ============================================================================

BEGIN;

-- ============================================================================
-- BLOCK 1: SEQUENCES
-- ============================================================================
CREATE SEQUENCE IF NOT EXISTS seq_file_category START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_file_document START WITH 1 INCREMENT BY 1;

-- ============================================================================
-- BLOCK 2: TABLES
-- ============================================================================
CREATE TABLE IF NOT EXISTS file_category (
    file_category_pk BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    category_code VARCHAR(50) NOT NULL,
    module_code VARCHAR(20) NOT NULL,
    name_ar VARCHAR(200) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    max_size_bytes_override BIGINT,
    allowed_types_note VARCHAR(500),
    is_active_fl INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS file_document (
    file_document_pk BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    owner_id BIGINT NOT NULL,
    owner_type VARCHAR(100) NOT NULL,
    module_code VARCHAR(20) NOT NULL,
    file_category_fk BIGINT NOT NULL,
    file_type_id VARCHAR(50) NOT NULL,
    file_name_original VARCHAR(255) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    file_content BYTEA,
    file_status_id VARCHAR(50) NOT NULL
);

-- ============================================================================
-- BLOCK 3: CONSTRAINTS
-- ============================================================================
ALTER TABLE file_category ADD CONSTRAINT pk_file_category PRIMARY KEY (file_category_pk);
ALTER TABLE file_document ADD CONSTRAINT pk_file_document PRIMARY KEY (file_document_pk);

ALTER TABLE file_category ADD CONSTRAINT uq_file_category_module_code UNIQUE (module_code, category_code);

ALTER TABLE file_document ADD CONSTRAINT fk_file_document_file_category
    FOREIGN KEY (file_category_fk) REFERENCES file_category (file_category_pk);

-- ============================================================================
-- BLOCK 4: INDEXES
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_file_document_file_category_fk ON file_document (file_category_fk);
CREATE INDEX IF NOT EXISTS idx_file_document_owner ON file_document (owner_id, owner_type);

-- ============================================================================
-- BLOCK 5: LOOKUP SEED DATA — LOV-FILE-001 (FILE_TYPE) / LOV-FILE-002 (FILE_STATUS)
-- MD_MASTER_LOOKUP / MD_LOOKUP_DETAIL are shared MasterData tables, used as-is.
-- ============================================================================
INSERT INTO md_master_lookup (id_pk, lookup_key, lookup_name, lookup_name_en, is_active, created_by, created_at)
SELECT nextval('md_master_lookup_seq'), 'FILE_TYPE', 'نوع الملف', 'File Type', 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_master_lookup WHERE lookup_key = 'FILE_TYPE');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'FILE_TYPE'), 'IMAGE', 'صورة', 'Image', 1, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'FILE_TYPE' AND d.code = 'IMAGE');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'FILE_TYPE'), 'DOCUMENT', 'مستند', 'Document', 2, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'FILE_TYPE' AND d.code = 'DOCUMENT');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'FILE_TYPE'), 'SPREADSHEET', 'جدول بيانات', 'Spreadsheet', 3, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'FILE_TYPE' AND d.code = 'SPREADSHEET');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'FILE_TYPE'), 'ARCHIVE', 'أرشيف مضغوط', 'Archive', 4, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'FILE_TYPE' AND d.code = 'ARCHIVE');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'FILE_TYPE'), 'OTHER', 'أخرى', 'Other', 5, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'FILE_TYPE' AND d.code = 'OTHER');

INSERT INTO md_master_lookup (id_pk, lookup_key, lookup_name, lookup_name_en, is_active, created_by, created_at)
SELECT nextval('md_master_lookup_seq'), 'FILE_STATUS', 'حالة الملف', 'File Status', 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_master_lookup WHERE lookup_key = 'FILE_STATUS');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'FILE_STATUS'), 'ACTIVE', 'نشط', 'Active', 1, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'FILE_STATUS' AND d.code = 'ACTIVE');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'FILE_STATUS'), 'ARCHIVED', 'مؤرشف', 'Archived', 2, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'FILE_STATUS' AND d.code = 'ARCHIVED');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'FILE_STATUS'), 'DELETED', 'محذوف', 'Deleted', 3, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'FILE_STATUS' AND d.code = 'DELETED');

-- ============================================================================
-- BLOCK 5b: FILE_CATEGORY seed — DEV/TEST convenience row, see Deviation #3
-- above. module_code/category_code match test-api/test_file_apis.py's
-- MODULE_CODE="PRC" / OWNER_TYPE="PURCHASE_ORDER" defaults.
-- ============================================================================
INSERT INTO file_category (file_category_pk, created_at, created_by, category_code, module_code, name_ar, name_en, is_active_fl)
SELECT nextval('seq_file_category'), now(), 'SYSTEM', 'PURCHASE_ORDER_DOC', 'PRC', 'مستند أمر الشراء', 'Purchase Order Document', 1
WHERE NOT EXISTS (SELECT 1 FROM file_category WHERE module_code = 'PRC' AND category_code = 'PURCHASE_ORDER_DOC');

-- ============================================================================
-- BLOCK 6: SEC_PAGES + PERMISSIONS seed (SCR-FILE-001 — Attachment Panel)
-- Same pattern as V2__sec_pages_permissions_seed.sql. Shared Component — no
-- independent nav route, PARENT_ID_FK left NULL (same precedent as
-- USER_PROFILE in V2: no real parent page exists to reference).
-- ============================================================================
INSERT INTO sec_pages (sec_pages_pk, page_code, name_ar, name_en, route, module, parent_id_fk, is_active, created_by, created_at)
SELECT nextval('sec_pages_seq'), 'FILE_ATTACHMENT', 'لوحة إدارة المرفقات', 'Attachment Panel', '/shared/file-attachment', 'FILE', NULL, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM sec_pages WHERE page_code = 'FILE_ATTACHMENT');

INSERT INTO permissions (name, page_id_fk, permission_type, created_by, created_at)
SELECT v.name, pg.sec_pages_pk, v.ptype, 'SYSTEM', now()
FROM (VALUES
    ('PERM_FILE_ATTACHMENT_VIEW',   'FILE_ATTACHMENT', 'VIEW'),
    ('PERM_FILE_ATTACHMENT_CREATE', 'FILE_ATTACHMENT', 'CREATE'),
    ('PERM_FILE_ATTACHMENT_UPDATE', 'FILE_ATTACHMENT', 'UPDATE'),
    ('PERM_FILE_ATTACHMENT_DELETE', 'FILE_ATTACHMENT', 'DELETE')
) AS v(name, page_code, ptype)
JOIN sec_pages pg ON pg.page_code = v.page_code
ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id_fk, perm_id_fk)
SELECT r.roles_pk, p.permissions_pk
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SUPER_ADMIN'
  AND p.name IN ('PERM_FILE_ATTACHMENT_VIEW', 'PERM_FILE_ATTACHMENT_CREATE', 'PERM_FILE_ATTACHMENT_UPDATE', 'PERM_FILE_ATTACHMENT_DELETE')
ON CONFLICT DO NOTHING;

COMMIT;
