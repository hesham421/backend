-- ============================================================================
-- FileCategory seed for MASTERDATA module — lets the embedded attachment panel
-- (SCR-FILE-001) wired into MasterLookupEntryComponent have a real category
-- to upload against. Same pattern as V3's PRC/PURCHASE_ORDER_DOC seed, the
-- only other FileCategory row that existed before this.
-- ============================================================================

BEGIN;

INSERT INTO file_category (
    file_category_pk, category_code, module_code, name_ar, name_en,
    is_active_fl, created_by, created_at
)
SELECT
    nextval('seq_file_category'), 'MASTER_LOOKUP_DOC', 'MASTERDATA',
    'مستند قائمة البحث', 'Master Lookup Document',
    1, 'SYSTEM', now()
WHERE NOT EXISTS (
    SELECT 1 FROM file_category WHERE category_code = 'MASTER_LOOKUP_DOC' AND module_code = 'MASTERDATA'
);

COMMIT;
