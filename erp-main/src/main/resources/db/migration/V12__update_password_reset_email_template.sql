-- ============================================================================
-- PLAN-SEC-NOTIF-001 — Password Reset Email Enhancement (Security x Notification)
--
-- Replaces the bare "{{token}} (valid until {{expiresAt}})" copy V11 seeded for
-- PASSWORD_RESET_REQUESTED with a real bilingual HTML message: application
-- name, greeting, a clickable reset button, a human-readable expiry, a
-- "didn't request this" / do-not-share warning, and an optional support
-- contact line. The raw token now only ever appears embedded inside
-- {{resetUrl}} (URL-encoded), never as a bare standalone value.
--
-- HTML is safe to use now: EmailChannelSender sends real MIME/HTML (not
-- SimpleMailMessage — see EmailChannelSender.java), and notif_log.body_preview
-- is unbounded TEXT (V13), not the old VARCHAR(1000) that would have clipped
-- markup mid-tag. Table-based layout, inline CSS only, max-width ~600px —
-- standard transactional-email practice for client compatibility.
--
-- templateCode is left as PASSWORD_RESET_REQUESTED (the code erp-security's
-- NotificationClient already sends) rather than introducing a new code — no
-- reason to fork identity for a content-only change.
--
-- templateBodyAr and templateBodyEn are set to the IDENTICAL bilingual HTML
-- (EN section dir="ltr" then AR section dir="rtl") so the recipient sees
-- both languages regardless of which column
-- NotificationEventProcessor.resolveBody() picks based on their preferredLang.
--
-- All placeholder VALUES that can carry user-supplied text (recipient name,
-- support email, the composed reset URL) are HTML-escaped by
-- PasswordResetEmailContextBuilder before they ever reach this markup —
-- NotificationEventProcessor's {{placeholder}} substitution itself does not
-- escape, so the escaping has to happen at the source.
-- ============================================================================

BEGIN;

UPDATE notif_template
SET template_body_ar = $$<div style="font-family:Arial,Helvetica,sans-serif;max-width:600px;margin:0 auto;color:#1a1a1a;">
  <h2 style="margin:0 0 16px;">{{applicationName}}</h2>

  <div dir="ltr" lang="en" style="text-align:left;margin-bottom:24px;">
    <p>Hi {{recipientNameEn}},</p>
    <p>We received a request to reset your password. Click the button below to continue — this link expires on <strong>{{resetExpiryFormattedEn}}</strong>.</p>
    <p style="margin:24px 0;">
      <a href="{{resetUrl}}" style="background:#2563eb;color:#ffffff;padding:12px 20px;border-radius:6px;text-decoration:none;display:inline-block;">Reset Password</a>
    </p>
    <p>If you did not request this, you can safely ignore this email — your password will not change. Never share this link with anyone.</p>
  </div>

  <hr style="border:none;border-top:1px solid #e5e5e5;margin:24px 0;">

  <div dir="rtl" lang="ar" style="text-align:right;margin-bottom:24px;">
    <p>مرحباً {{recipientNameAr}}،</p>
    <p>وصلنا طلب لإعادة تعيين كلمة مرورك. اضغط على الزر أدناه للمتابعة — تنتهي صلاحية هذا الرابط في <strong>{{resetExpiryFormattedAr}}</strong>.</p>
    <p style="margin:24px 0;">
      <a href="{{resetUrl}}" style="background:#2563eb;color:#ffffff;padding:12px 20px;border-radius:6px;text-decoration:none;display:inline-block;">إعادة تعيين كلمة المرور</a>
    </p>
    <p>إذا لم تطلب ذلك، يمكنك تجاهل هذه الرسالة بأمان — لن تتغير كلمة مرورك. لا تشارك هذا الرابط مع أي شخص.</p>
  </div>

  <p style="font-size:12px;color:#666;margin-top:24px;">{{supportEmail}}</p>
</div>$$,
    template_body_en = $$<div style="font-family:Arial,Helvetica,sans-serif;max-width:600px;margin:0 auto;color:#1a1a1a;">
  <h2 style="margin:0 0 16px;">{{applicationName}}</h2>

  <div dir="ltr" lang="en" style="text-align:left;margin-bottom:24px;">
    <p>Hi {{recipientNameEn}},</p>
    <p>We received a request to reset your password. Click the button below to continue — this link expires on <strong>{{resetExpiryFormattedEn}}</strong>.</p>
    <p style="margin:24px 0;">
      <a href="{{resetUrl}}" style="background:#2563eb;color:#ffffff;padding:12px 20px;border-radius:6px;text-decoration:none;display:inline-block;">Reset Password</a>
    </p>
    <p>If you did not request this, you can safely ignore this email — your password will not change. Never share this link with anyone.</p>
  </div>

  <hr style="border:none;border-top:1px solid #e5e5e5;margin:24px 0;">

  <div dir="rtl" lang="ar" style="text-align:right;margin-bottom:24px;">
    <p>مرحباً {{recipientNameAr}}،</p>
    <p>وصلنا طلب لإعادة تعيين كلمة مرورك. اضغط على الزر أدناه للمتابعة — تنتهي صلاحية هذا الرابط في <strong>{{resetExpiryFormattedAr}}</strong>.</p>
    <p style="margin:24px 0;">
      <a href="{{resetUrl}}" style="background:#2563eb;color:#ffffff;padding:12px 20px;border-radius:6px;text-decoration:none;display:inline-block;">إعادة تعيين كلمة المرور</a>
    </p>
    <p>إذا لم تطلب ذلك، يمكنك تجاهل هذه الرسالة بأمان — لن تتغير كلمة مرورك. لا تشارك هذا الرابط مع أي شخص.</p>
  </div>

  <p style="font-size:12px;color:#666;margin-top:24px;">{{supportEmail}}</p>
</div>$$,
    updated_by = 'SYSTEM',
    updated_at = now()
WHERE template_code = 'PASSWORD_RESET_REQUESTED';

COMMIT;
