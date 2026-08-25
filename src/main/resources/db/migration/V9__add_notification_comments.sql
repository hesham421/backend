-- ============================================================================
-- Reconciliation Migration — DATABASE_RECONCILIATION_REPORT.md Section 6
-- Source: governance/modules/NOTIFICATION/P2/db-script.md BLOCK 4 (COMMENTS).
--
-- FINDING (category GOVERNANCE_METADATA_ONLY): db-script.md defines 41
-- COMMENT ON TABLE/COLUMN statements for NOTIF_LOG/NOTIF_TEMPLATE/
-- NOTIF_CHANNEL_CONFIG. V5 applied the schema but not this documentation
-- metadata. Schema itself is already EXACT_MATCH — this migration adds ONLY
-- the missing metadata, verbatim.
--
-- The NOTIF_TEMPLATE.FILE_FK comment below documents the column's current,
-- correct, unconstrained-and-deferred state (XM-NOTIF-001) — adding this
-- comment does not enable the deferred FK itself (still commented out,
-- unblocked only by RXE-NOTIF per Section 5/9 of the report).
-- ============================================================================

BEGIN;

COMMENT ON TABLE notif_log IS 'SHARED (owner) — append-only delivery log, one row per (event, channel) fan-out. ENTITY-NOTIF-001. No Update/Delete — status transitions only.';
COMMENT ON COLUMN notif_log.notification_log_pk IS 'Primary key — auto-generated, PK population handled by application framework.';
COMMENT ON COLUMN notif_log.recipient_id IS 'FK → Security USERS.USERS_PK (PERMANENT EXCEPTION column name) — not usersFk.';
COMMENT ON COLUMN notif_log.notification_type_id IS 'Channel used for this row — LOV-NOTIF-001 (lookupKey: NOTIFICATION_CHANNEL). One independent row per requested channel — RULE-NOTIF-003.';
COMMENT ON COLUMN notif_log.template_code IS 'Natural-key logical reference to NOTIF_TEMPLATE.TEMPLATE_CODE — no physical FK (graceful fallback per RULE-NOTIF-006).';
COMMENT ON COLUMN notif_log.subject IS 'Notification subject (primarily Email channel).';
COMMENT ON COLUMN notif_log.body_preview IS 'Short preview of the sent content.';
COMMENT ON COLUMN notif_log.notification_status_id IS 'Status Lifecycle (4 states: PENDING/SENT/FAILED/CHANNEL_DISABLED) — LOV-NOTIF-002 (lookupKey: NOTIFICATION_STATUS).';
COMMENT ON COLUMN notif_log.retry_count IS 'Delivery retry attempts, default 0, ceiling 5 — RULE-NOTIF-004. SMALLINT per governance note (Section 2).';
COMMENT ON COLUMN notif_log.sent_at IS 'Actual send timestamp — null until sent.';
COMMENT ON COLUMN notif_log.module_code IS 'Publishing module code.';
COMMENT ON COLUMN notif_log.reference_id IS 'Polymorphic reference to the related business record — no physical FK; same pattern as FILE_DOCUMENT.OWNER_ID.';
COMMENT ON COLUMN notif_log.reference_type IS 'Related entity type name from the publishing module — free text, not a governed lookup.';
COMMENT ON COLUMN notif_log.created_by IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN notif_log.created_at IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN notif_log.updated_by IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN notif_log.updated_at IS 'Audit — populated by AuditEntityListener.';

COMMENT ON TABLE notif_template IS 'PRIVATE — bilingual notification templates. ENTITY-NOTIF-002. Consumes SHARED ENTITY-FILE-001 (DEFERRED — XM-NOTIF-001).';
COMMENT ON COLUMN notif_template.notification_template_pk IS 'Primary key — auto-generated, PK population handled by application framework.';
COMMENT ON COLUMN notif_template.template_code IS 'Unique template code, immutable after creation — RULE-NOTIF-007.';
COMMENT ON COLUMN notif_template.template_name_ar IS 'Template display name — Arabic.';
COMMENT ON COLUMN notif_template.template_name_en IS 'Template display name — English.';
COMMENT ON COLUMN notif_template.channel_type_id IS 'Target channel for this template — LOV-NOTIF-001 (lookupKey: NOTIFICATION_CHANNEL).';
COMMENT ON COLUMN notif_template.module_code IS 'Owning module code for this template.';
COMMENT ON COLUMN notif_template.template_body_ar IS 'Template body, Arabic — Phase-1 inline storage (RESOLUTION-02). Supports placeholders. Retained permanently as fallback after File Service migration.';
COMMENT ON COLUMN notif_template.template_body_en IS 'Template body, English — Phase-1 inline storage (RESOLUTION-02). Retained permanently as fallback after File Service migration.';
COMMENT ON COLUMN notif_template.file_fk IS 'DEFERRED FK to FILE_DOCUMENT (File Service) — XM-NOTIF-001. NULLABLE, unused in Phase 1 — activated on RXE-NOTIF receipt without changing TEMPLATE_CODE.';
COMMENT ON COLUMN notif_template.is_active_fl IS 'Active flag — 1 = active, 0 = inactive.';
COMMENT ON COLUMN notif_template.created_by IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN notif_template.created_at IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN notif_template.updated_by IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN notif_template.updated_at IS 'Audit — populated by AuditEntityListener.';

COMMENT ON TABLE notif_channel_config IS 'PRIVATE (Configuration) — one fixed row per channel (5 seed rows), toggled by Admin. ENTITY-NOTIF-003. No Create/Delete from the user.';
COMMENT ON COLUMN notif_channel_config.notification_channel_config_pk IS 'Primary key — auto-generated, PK population handled by application framework.';
COMMENT ON COLUMN notif_channel_config.channel_type_id IS 'Channel this config row governs — LOV-NOTIF-001 (lookupKey: NOTIFICATION_CHANNEL). Unique — one row per channel.';
COMMENT ON COLUMN notif_channel_config.is_enabled_fl IS 'Whether this channel is currently enabled — RULE-NOTIF-005. Default 1 for all 5 channels, Phase 1 — no channel deferred.';
COMMENT ON COLUMN notif_channel_config.config_json IS 'Provider-specific adapter configuration (e.g. SMS/WhatsApp provider credentials, AQ-010/AQ-011) — free text, interpreted by the application.';
COMMENT ON COLUMN notif_channel_config.created_by IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN notif_channel_config.created_at IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN notif_channel_config.updated_by IS 'Audit — populated by AuditEntityListener.';
COMMENT ON COLUMN notif_channel_config.updated_at IS 'Audit — populated by AuditEntityListener.';

COMMIT;
