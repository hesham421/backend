-- ============================================================================
-- Notification Module — SEC_PAGES route fix
--
-- V5__notification_schema_and_seed.sql seeded routes that don't match the
-- actual Angular routes registered in
-- frontend/src/app/modules/notification/notification-routing.module.ts
-- (F4-RULE-1: absolute top-level paths, not nested under /notifications/*):
--
--   NOTIFICATION_INBOX           '/notifications/inbox'         -> '/notifications'
--   NOTIFICATION_TEMPLATE        '/notifications/templates'     -> '/notification-templates'
--   NOTIFICATION_CHANNEL_CONFIG  '/notifications/channel-config'-> '/notification-channel-configs'
--
-- Same class of bug as V7 (FILE_ATTACHMENT) — a sidebar entry whose route
-- doesn't resolve to any real Angular route, so clicking it fails silently.
-- ============================================================================

BEGIN;

UPDATE sec_pages SET route = '/notifications', updated_by = 'SYSTEM', updated_at = now()
WHERE page_code = 'NOTIFICATION_INBOX';

UPDATE sec_pages SET route = '/notification-templates', updated_by = 'SYSTEM', updated_at = now()
WHERE page_code = 'NOTIFICATION_TEMPLATE';

UPDATE sec_pages SET route = '/notification-channel-configs', updated_by = 'SYSTEM', updated_at = now()
WHERE page_code = 'NOTIFICATION_CHANNEL_CONFIG';

COMMIT;
