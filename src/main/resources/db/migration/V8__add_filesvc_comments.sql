-- ============================================================================
-- Reconciliation Migration — DATABASE_RECONCILIATION_REPORT.md Section 6
-- Source: governance/modules/FILESVC/P2/db-script.md BLOCK 4 (COMMENTS).
--
-- FINDING (category GOVERNANCE_METADATA_ONLY): db-script.md defines 29
-- COMMENT ON TABLE/COLUMN statements for FILE_CATEGORY/FILE_DOCUMENT. V3
-- applied the schema but not this documentation metadata. Schema itself is
-- already EXACT_MATCH (module for the length conflict fixed in V6) — this
-- migration adds ONLY the missing metadata, verbatim.
-- ============================================================================

BEGIN;

COMMENT ON TABLE file_category IS 'Reference Table — business document-category taxonomy, module_code-scoped, extensible by Admin. ENTITY-FILE-002.';
COMMENT ON COLUMN file_category.file_category_pk IS 'Primary key — auto-generated, PK population handled by application framework.';
COMMENT ON COLUMN file_category.category_code IS 'Category code — unique within MODULE_CODE, immutable after creation (lookupKey-like pattern).';
COMMENT ON COLUMN file_category.module_code IS 'Owning module code for this category (e.g. NOTIFICATION, PRC) — free text, not a governed lookup.';
COMMENT ON COLUMN file_category.name_ar IS 'Category display name — Arabic.';
COMMENT ON COLUMN file_category.name_en IS 'Category display name — English.';
COMMENT ON COLUMN file_category.max_size_bytes_override IS 'Optional per-category override of the default 5MB content size limit — RULE-FILE-001.';
COMMENT ON COLUMN file_category.allowed_types_note IS 'Free-text advisory note of allowed file types for this category — not an enforced constraint.';
COMMENT ON COLUMN file_category.is_active_fl IS 'Active flag — 1 = active, 0 = inactive.';
COMMENT ON COLUMN file_category.created_by IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN file_category.created_at IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN file_category.updated_by IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN file_category.updated_at IS 'Audit — populated by AuditEntityListener.';

COMMENT ON TABLE file_document IS 'SHARED (owner) — central binary file storage for all platform modules. ENTITY-FILE-001. No Update operation — replacement is delete+re-upload.';
COMMENT ON COLUMN file_document.file_document_pk IS 'Primary key — auto-generated, PK population handled by application framework.';
COMMENT ON COLUMN file_document.owner_id IS 'Polymorphic reference to the owning business record — no physical FK; target table determined by OWNER_TYPE (ADAPT-05).';
COMMENT ON COLUMN file_document.owner_type IS 'Owning entity type name from the producing module (e.g. PURCHASE_ORDER) — free text, not a governed lookup.';
COMMENT ON COLUMN file_document.module_code IS 'Producing module code.';
COMMENT ON COLUMN file_document.file_category_fk IS 'FK to FILE_CATEGORY — intra-module.';
COMMENT ON COLUMN file_document.file_type_id IS 'System-detected technical file type — LOV-FILE-001 (lookupKey: FILE_TYPE) — RULE-FILE-005.';
COMMENT ON COLUMN file_document.file_name_original IS 'Original file name as uploaded by the user.';
COMMENT ON COLUMN file_document.mime_type IS 'MIME type detected server-side from file content — never trusts client Content-Type header — RULE-FILE-005.';
COMMENT ON COLUMN file_document.file_size_bytes IS 'File content size in bytes, computed at upload time.';
COMMENT ON COLUMN file_document.file_content IS 'Binary file content (BYTEA) — RESOLUTION-01, extends CORE-8. Purged (set to NULL at app layer) on permanent delete while the row is retained — see FILE_STATUS_ID.';
COMMENT ON COLUMN file_document.file_status_id IS 'Status Lifecycle (3 states: ACTIVE/ARCHIVED/DELETED) — LOV-FILE-002 (lookupKey: FILE_STATUS). DELETED = content purged, metadata + audit trail retained, so any consumer HARD-FK stays valid — resolves OQ-001.';
COMMENT ON COLUMN file_document.created_by IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN file_document.created_at IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN file_document.updated_by IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN file_document.updated_at IS 'Audit — populated by AuditEntityListener.';

COMMIT;
