-- ============================================================================
-- PLAN-SEC-NOTIF-001 (follow-up enhancement) — the EMAIL channel now sends real
-- HTML (EmailChannelSender switched from SimpleMailMessage to a MIME/HTML
-- message), and notif_log.body_preview IS the content actually emailed, not
-- a separate log-only preview. The old VARCHAR(1000) cap (V5) truncated
-- rendered HTML mid-tag. Widen to unbounded TEXT, matching notif_template's
-- template_body_ar/en columns.
-- ============================================================================

BEGIN;

ALTER TABLE notif_log ALTER COLUMN body_preview TYPE TEXT;

COMMIT;
