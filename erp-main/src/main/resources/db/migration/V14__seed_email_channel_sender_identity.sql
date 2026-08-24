-- ============================================================================
-- PLAN-SEC-NOTIF-001 (follow-up enhancement) — configurable sender identity
-- (DRV-SEC-NOTIF-005). Seeds a display name for the EMAIL channel so outbound
-- mail shows "ERP System <account>" instead of a bare address.
--
-- Deliberately does NOT set "mailFrom" here: Gmail's free-tier SMTP relay
-- (spring.mail.username, see application-dev.properties) generally rejects
-- or silently rewrites a From *address* that isn't the authenticated account
-- or a verified "Send As" alias. mailFrom is left to be configured later,
-- by hand, once a verified sending domain/alias exists — EmailChannelSender
-- already reads it from this same config_json if present.
-- ============================================================================

BEGIN;

UPDATE notif_channel_config
SET config_json = '{"mailFromName": "ERP System"}',
    updated_by = 'SYSTEM',
    updated_at = now()
WHERE channel_type_id = 'EMAIL'
  AND config_json IS NULL;

COMMIT;
