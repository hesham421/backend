-- ============================================================================
-- Bug fix: erp-security's NotificationClient (AuthEventListener, AFTER_COMMIT)
-- dispatches ACCOUNT_ACTIVATION_REQUESTED / PASSWORD_RESET_REQUESTED events
-- to erp-notification with contextData={token, expiresAt}, but V5 created the
-- NOTIF_TEMPLATE schema/sequence without seeding any rows into it. With no
-- template for these codes (and no SYSTEM_DEFAULT fallback seeded either),
-- NotificationEventProcessor.resolveTemplate() always fell through to the
-- generic in-memory fallback ("You have a new notification") with no
-- {{token}} placeholder — so any real signup-activation or password-reset
-- email would render with no actual token/link in it.
--
-- This migration seeds exactly those two template rows so the existing
-- {{token}}/{{expiresAt}} substitution in
-- NotificationEventProcessor.renderBody() has real content to render.
--
-- DEVIATION NOTE: no governance/modules/NOTIFICATION doc specifies template
-- wording for these two codes (only the NOTIF_TEMPLATE table schema is
-- governed, not seed content) — this wording is new/ungoverned starter copy,
-- consistent in shape with V5's own seed rows, and may be revised by a future
-- governance/content pass.
-- ============================================================================

BEGIN;

INSERT INTO notif_template (notification_template_pk, template_code, template_name_ar, template_name_en,
    channel_type_id, module_code, template_body_ar, template_body_en, is_active_fl, created_by, created_at)
SELECT nextval('seq_notif_template'), 'ACCOUNT_ACTIVATION_REQUESTED', 'تفعيل الحساب', 'Account Activation',
    'EMAIL', 'SECURITY',
    'لتفعيل حسابك، استخدم الرمز التالي: {{token}} (صالح حتى {{expiresAt}})',
    'To activate your account, use this token: {{token}} (valid until {{expiresAt}})',
    1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM notif_template WHERE template_code = 'ACCOUNT_ACTIVATION_REQUESTED');

INSERT INTO notif_template (notification_template_pk, template_code, template_name_ar, template_name_en,
    channel_type_id, module_code, template_body_ar, template_body_en, is_active_fl, created_by, created_at)
SELECT nextval('seq_notif_template'), 'PASSWORD_RESET_REQUESTED', 'إعادة تعيين كلمة المرور', 'Password Reset',
    'EMAIL', 'SECURITY',
    'لإعادة تعيين كلمة المرور، استخدم الرمز التالي: {{token}} (صالح حتى {{expiresAt}})',
    'To reset your password, use this token: {{token}} (valid until {{expiresAt}})',
    1, 'SYSTEM', now()
WHERE NOT EXISTS (SELECT 1 FROM notif_template WHERE template_code = 'PASSWORD_RESET_REQUESTED');

COMMIT;
