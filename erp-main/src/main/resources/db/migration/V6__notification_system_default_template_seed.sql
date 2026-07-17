-- ============================================================================
-- Notification Module — SYSTEM_DEFAULT template seed (DRV-NOTIF-002)
--
-- NotificationEventProcessor.resolveTemplate() probes DEFAULT_TEMPLATE_CODE
-- ("SYSTEM_DEFAULT") after an unmatched/inactive templateCode, before falling
-- back to a transient in-memory NotificationTemplate (never persisted, logged
-- as a warning each time). No governed artifact defined this row's content
-- until now — this migration seeds it with exactly the same field values
-- transientFallbackTemplate() uses, so a real send resolves to a persisted
-- template instead of the in-memory phantom (see execution-state.json's
-- drv_notif_002_application note: "revisit if a real default template is
-- ever seeded").
-- ============================================================================

BEGIN;

INSERT INTO notif_template (
    notification_template_pk, template_code, template_name_ar, template_name_en,
    channel_type_id, module_code, template_body_ar, template_body_en,
    is_active_fl, created_by, created_at
)
SELECT
    nextval('seq_notif_template'), 'SYSTEM_DEFAULT', 'إشعار', 'Notification',
    'INTERNAL', 'NOTIFICATION', 'لديك إشعار جديد', 'You have a new notification',
    1, 'SYSTEM', now()
WHERE NOT EXISTS (
    SELECT 1 FROM notif_template WHERE template_code = 'SYSTEM_DEFAULT'
);

COMMIT;
