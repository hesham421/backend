-- ============================================================================
-- Notification Module — DBS-NOTIF-001 / Phase DATA+DOM
-- Source: governance-repo/modules/NOTIFICATION/P2/db-script.md BLOCK 1-8,
--         P1/srs.md.
--
-- Adds NOTIF_LOG (SHARED-owner, append-only), NOTIF_TEMPLATE (PRIVATE) and
-- NOTIF_CHANNEL_CONFIG (PRIVATE/Configuration) tables, seeds LOV-NOTIF-001
-- (NOTIFICATION_CHANNEL) / LOV-NOTIF-002 (NOTIFICATION_STATUS) into the
-- existing MD_MASTER_LOOKUP/MD_LOOKUP_DETAIL tables, and seeds
-- NOTIF_CHANNEL_CONFIG's own 5 fixed rows (one per channel, all enabled
-- Phase 1 — module-registry-notif.md AUTO-DECISIONS, final 2026-07-11).
--
-- Naming follows this file's own house style (lowercase, IF NOT EXISTS)
-- rather than db-script.md's literal UPPERCASE spelling — same reasoning as
-- V3__file_service_schema_and_seed.sql.
--
-- SCOPE NOTE: db-script.md also contains a "SECURITY SEED — SEC_PAGES +
-- PERMISSIONS" section (NOTIFICATION_INBOX/SETTINGS/TEMPLATE pages +
-- PERM_NOTIFICATION_* permissions) after its own BLOCK 11. That section is
-- deliberately NOT applied here — DATAOM.md (this phase's own governing
-- sub-file) scopes this phase to ENTITY-NOTIF-001/002/003 + QR-NOTIF-001/
-- 002/003 only, and unlike File Service (whose FILE_ATTACHMENT page is a
-- single shared component seeded in its own DATAOM migration), NOTIFICATION
-- has its own dedicated later SEC phase ("Permission matrix across the same
-- 3 screens", Weight Map: XL) that owns this seeding — see
-- HANDOFF-NOTIFICATION-PHASE-DATAOM.md.
--
-- Deviations from db-script.md, flagged (not silently reconciled):
--   1. IS_ACTIVE_FL / IS_ENABLED_FL use INTEGER, not db-script.md's stated
--      SMALLINT — matches this repo's actual BooleanNumberConverter mapping
--      (Boolean -> Integer) used by every other boolean flag already live in
--      this schema (org_legal_entity.is_active_fl, file_category.is_active_fl),
--      same non-new deviation already flagged in V3.
--   2. RETRY_COUNT keeps db-script.md's literal SMALLINT — this one is a
--      deliberate CORE.md/DRV-NOTIF-001 call (a count, not a flag), not
--      subject to the _FL/BooleanNumberConverter convention above.
--   3. CREATED_AT/UPDATED_AT/SENT_AT use TIMESTAMP (not TIMESTAMPTZ) —
--      matches AuditableEntity's existing @JdbcTypeCode(SqlTypes.TIMESTAMP)
--      mapping for createdAt/updatedAt on every other entity in this schema.
-- ============================================================================

BEGIN;

-- ============================================================================
-- BLOCK 1: SEQUENCES
-- ============================================================================
CREATE SEQUENCE IF NOT EXISTS seq_notif_log START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_notif_template START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS seq_notif_channel_config START WITH 1 INCREMENT BY 1;

-- ============================================================================
-- BLOCK 2: TABLES
-- (No intra-module FK dependencies — TEMPLATE_CODE on NOTIF_LOG is a
--  natural-key reference, not a physical FK.)
-- ============================================================================
CREATE TABLE IF NOT EXISTS notif_log (
    notification_log_pk BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    recipient_id BIGINT NOT NULL,
    notification_type_id VARCHAR(20) NOT NULL,
    template_code VARCHAR(50) NOT NULL,
    subject VARCHAR(500),
    body_preview VARCHAR(1000),
    notification_status_id VARCHAR(20) NOT NULL,
    retry_count SMALLINT NOT NULL DEFAULT 0,
    sent_at TIMESTAMP,
    module_code VARCHAR(20) NOT NULL,
    reference_id BIGINT,
    reference_type VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS notif_template (
    notification_template_pk BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    template_code VARCHAR(50) NOT NULL,
    template_name_ar VARCHAR(200) NOT NULL,
    template_name_en VARCHAR(200) NOT NULL,
    channel_type_id VARCHAR(20) NOT NULL,
    module_code VARCHAR(20) NOT NULL,
    template_body_ar TEXT NOT NULL,
    template_body_en TEXT NOT NULL,
    file_fk BIGINT,
    is_active_fl INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS notif_channel_config (
    notification_channel_config_pk BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    channel_type_id VARCHAR(20) NOT NULL,
    is_enabled_fl INTEGER NOT NULL DEFAULT 1,
    config_json TEXT
);

-- ============================================================================
-- BLOCK 3: CONSTRAINTS
-- ============================================================================
ALTER TABLE notif_log ADD CONSTRAINT pk_notif_log PRIMARY KEY (notification_log_pk);
ALTER TABLE notif_template ADD CONSTRAINT pk_notif_template PRIMARY KEY (notification_template_pk);
ALTER TABLE notif_channel_config ADD CONSTRAINT pk_notif_channel_config PRIMARY KEY (notification_channel_config_pk);

ALTER TABLE notif_template ADD CONSTRAINT uq_notif_template_code UNIQUE (template_code);
ALTER TABLE notif_channel_config ADD CONSTRAINT uq_notif_channel_config_type UNIQUE (channel_type_id);

-- Cross-module FK — Security PERMANENT EXCEPTION (live, USERS is a
-- pre-existing AS-IS dependency, not gated by this module's own DBS-IDs).
ALTER TABLE notif_log ADD CONSTRAINT fk_notif_log_users
    FOREIGN KEY (recipient_id) REFERENCES users (users_pk);

-- ============================================================================
-- BLOCK 4: INDEXES
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_notif_log_recipient ON notif_log (recipient_id);
CREATE INDEX IF NOT EXISTS idx_notif_log_status ON notif_log (notification_status_id);
CREATE INDEX IF NOT EXISTS idx_notif_log_type ON notif_log (notification_type_id);

CREATE INDEX IF NOT EXISTS idx_notif_template_file_fk ON notif_template (file_fk);
CREATE INDEX IF NOT EXISTS idx_notif_template_channel ON notif_template (channel_type_id);
CREATE INDEX IF NOT EXISTS idx_notif_template_module ON notif_template (module_code);

-- (notif_channel_config.channel_type_id already indexed via uq_notif_channel_config_type)

-- ============================================================================
-- BLOCK 5: LOOKUP SEED DATA — LOV-NOTIF-001 (NOTIFICATION_CHANNEL) /
-- LOV-NOTIF-002 (NOTIFICATION_STATUS). MD_MASTER_LOOKUP / MD_LOOKUP_DETAIL
-- are shared MasterData tables, used as-is.
-- ============================================================================
INSERT INTO md_master_lookup (id_pk, lookup_key, lookup_name, lookup_name_en, is_active, created_by, created_at)
SELECT nextval('md_master_lookup_seq'), 'NOTIFICATION_CHANNEL', 'قناة الإشعار', 'Notification Channel', 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_master_lookup WHERE lookup_key = 'NOTIFICATION_CHANNEL');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'NOTIFICATION_CHANNEL'), 'EMAIL', 'بريد إلكتروني', 'Email', 1, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'NOTIFICATION_CHANNEL' AND d.code = 'EMAIL');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'NOTIFICATION_CHANNEL'), 'SMS', 'رسالة نصية', 'SMS', 2, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'NOTIFICATION_CHANNEL' AND d.code = 'SMS');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'NOTIFICATION_CHANNEL'), 'WHATSAPP', 'واتساب', 'WhatsApp', 3, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'NOTIFICATION_CHANNEL' AND d.code = 'WHATSAPP');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'NOTIFICATION_CHANNEL'), 'PUSH', 'إشعار فوري (تطبيق)', 'Push', 4, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'NOTIFICATION_CHANNEL' AND d.code = 'PUSH');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'NOTIFICATION_CHANNEL'), 'INTERNAL', 'إشعار داخلي', 'Internal', 5, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'NOTIFICATION_CHANNEL' AND d.code = 'INTERNAL');

INSERT INTO md_master_lookup (id_pk, lookup_key, lookup_name, lookup_name_en, is_active, created_by, created_at)
SELECT nextval('md_master_lookup_seq'), 'NOTIFICATION_STATUS', 'حالة الإشعار', 'Notification Status', 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_master_lookup WHERE lookup_key = 'NOTIFICATION_STATUS');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'NOTIFICATION_STATUS'), 'PENDING', 'قيد الانتظار', 'Pending', 1, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'NOTIFICATION_STATUS' AND d.code = 'PENDING');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'NOTIFICATION_STATUS'), 'SENT', 'تم الإرسال', 'Sent', 2, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'NOTIFICATION_STATUS' AND d.code = 'SENT');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'NOTIFICATION_STATUS'), 'FAILED', 'فشل', 'Failed', 3, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'NOTIFICATION_STATUS' AND d.code = 'FAILED');

INSERT INTO md_lookup_detail (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('md_lookup_detail_seq'), (SELECT id_pk FROM md_master_lookup WHERE lookup_key = 'NOTIFICATION_STATUS'), 'CHANNEL_DISABLED', 'القناة معطَّلة', 'Channel Disabled', 4, 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM md_lookup_detail d JOIN md_master_lookup m ON m.id_pk = d.master_lookup_id_fk WHERE m.lookup_key = 'NOTIFICATION_STATUS' AND d.code = 'CHANNEL_DISABLED');

-- ============================================================================
-- BLOCK 6: NOTIF_CHANNEL_CONFIG SEED DATA (module's own table, not a lookup)
-- 5 fixed rows, one per channel — all enabled Phase 1 (final decision
-- 2026-07-11, module-registry-notif.md AUTO-DECISIONS).
-- ============================================================================
INSERT INTO notif_channel_config (notification_channel_config_pk, channel_type_id, is_enabled_fl, created_by, created_at)
SELECT nextval('seq_notif_channel_config'), 'EMAIL', 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM notif_channel_config WHERE channel_type_id = 'EMAIL');

INSERT INTO notif_channel_config (notification_channel_config_pk, channel_type_id, is_enabled_fl, created_by, created_at)
SELECT nextval('seq_notif_channel_config'), 'SMS', 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM notif_channel_config WHERE channel_type_id = 'SMS');

INSERT INTO notif_channel_config (notification_channel_config_pk, channel_type_id, is_enabled_fl, created_by, created_at)
SELECT nextval('seq_notif_channel_config'), 'WHATSAPP', 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM notif_channel_config WHERE channel_type_id = 'WHATSAPP');

INSERT INTO notif_channel_config (notification_channel_config_pk, channel_type_id, is_enabled_fl, created_by, created_at)
SELECT nextval('seq_notif_channel_config'), 'PUSH', 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM notif_channel_config WHERE channel_type_id = 'PUSH');

INSERT INTO notif_channel_config (notification_channel_config_pk, channel_type_id, is_enabled_fl, created_by, created_at)
SELECT nextval('seq_notif_channel_config'), 'INTERNAL', 1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM notif_channel_config WHERE channel_type_id = 'INTERNAL');

-- ============================================================================
-- BLOCK 7: DEFERRED FK (DO NOT UNCOMMENT UNTIL FILE SERVICE'S MIGRATION IS
-- TRIGGERED VIA RXE-NOTIF — XM-NOTIF-001, see INT-C/INT-R)
-- ============================================================================
-- ALTER TABLE notif_template
--     ADD CONSTRAINT fk_notif_template_file_document
--     FOREIGN KEY (file_fk) REFERENCES file_document (file_document_pk);

COMMIT;
