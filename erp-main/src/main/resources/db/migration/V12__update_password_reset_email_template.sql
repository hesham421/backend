-- ============================================================================
-- PLAN-SEC-NOTIF-001 — Password Reset Email Enhancement (Security x Notification)
--
-- Replaces the bare "{{token}} (valid until {{expiresAt}})" copy V11 seeded for
-- PASSWORD_RESET_REQUESTED with a proper bilingual message: application name,
-- greeting, a clickable reset link (built by erp-security's
-- PasswordResetEmailContextBuilder — the raw token now only ever appears
-- embedded inside {{resetUrl}}, never as a bare standalone value), a
-- human-readable expiry, a "didn't request this" / do-not-share warning, and
-- an optional support contact line.
--
-- DEVIATION NOTE from the original content spec: EmailChannelSender sends
-- plain-text SimpleMailMessage (not MIME/HTML), and NotificationLog.body_preview
-- is capped at 1000 chars (V5 schema) with no conditional-placeholder support
-- in NotificationEventProcessor.renderBody() (plain {{key}} substitution only).
-- A full HTML table-layout email is therefore not achievable without changing
-- erp-notification, which is out of scope here — this is a concise plain-text
-- bilingual body instead, comfortably under the 1000-char cap after
-- substitution. templateCode is left as PASSWORD_RESET_REQUESTED (the code
-- erp-security's NotificationClient already sends) rather than introducing a
-- new code — no reason to fork identity for a content-only change.
--
-- templateBodyAr and templateBodyEn are set to the IDENTICAL bilingual text
-- (EN section then AR section) so the recipient sees both languages
-- regardless of which column NotificationEventProcessor.resolveBody() picks
-- based on their preferredLang.
-- ============================================================================

BEGIN;

UPDATE notif_template
SET template_body_ar = $${{applicationName}} - Password Reset
Hi {{recipientNameEn}}, click below to reset your password (expires {{resetExpiryFormattedEn}}):
{{resetUrl}}
Did not request this? Ignore this email - your password stays unchanged. Never share this link. Support: {{supportEmail}}
---
{{applicationName}} - إعادة تعيين كلمة المرور
مرحباً {{recipientNameAr}}، اضغط على الرابط أدناه لإعادة تعيين كلمة مرورك (تنتهي الصلاحية {{resetExpiryFormattedAr}}):
{{resetUrl}}
لم تطلب ذلك؟ تجاهل هذه الرسالة، لن تتغير كلمة مرورك. لا تشارك هذا الرابط مع أي شخص. الدعم: {{supportEmail}}$$,
    template_body_en = $${{applicationName}} - Password Reset
Hi {{recipientNameEn}}, click below to reset your password (expires {{resetExpiryFormattedEn}}):
{{resetUrl}}
Did not request this? Ignore this email - your password stays unchanged. Never share this link. Support: {{supportEmail}}
---
{{applicationName}} - إعادة تعيين كلمة المرور
مرحباً {{recipientNameAr}}، اضغط على الرابط أدناه لإعادة تعيين كلمة مرورك (تنتهي الصلاحية {{resetExpiryFormattedAr}}):
{{resetUrl}}
لم تطلب ذلك؟ تجاهل هذه الرسالة، لن تتغير كلمة مرورك. لا تشارك هذا الرابط مع أي شخص. الدعم: {{supportEmail}}$$,
    updated_by = 'SYSTEM',
    updated_at = now()
WHERE template_code = 'PASSWORD_RESET_REQUESTED';

COMMIT;
