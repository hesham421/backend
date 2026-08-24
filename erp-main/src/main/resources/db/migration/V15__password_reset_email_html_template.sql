-- ============================================================================
-- Follow-up to V12/V14 (PLAN-SEC-NOTIF-001) — EmailChannelSender now sends
-- MimeMessage with helper.setText(bodyPreview, true), i.e. Content-Type: text/html.
-- V12's body was plain text with literal newlines and a "---" separator — valid
-- as plain text, but meaningless as HTML: a mail client renders it as HTML and
-- collapses the newlines, so the message shows as one run-on paragraph with no
-- visual separation between the two languages, and no dir="rtl"/"ltr" markup.
--
-- This migration replaces the body with real HTML: inline CSS (external
-- stylesheets/<style> blocks are unreliable across mail clients), a single-column
-- max-width layout for mobile, a clickable "Reset Password" button using the same
-- {{resetUrl}} placeholder, and two explicitly marked sections —
-- dir="ltr" lang="en" and dir="rtl" lang="ar" — so RTL Arabic text actually
-- renders right-to-left instead of inheriting the client's default direction.
--
-- Same placeholders as V12 (all wired via PasswordResetEmailContextBuilder,
-- unchanged): {{applicationName}}, {{recipientNameEn}}, {{recipientNameAr}},
-- {{resetUrl}}, {{resetExpiryFormattedEn}}, {{resetExpiryFormattedAr}},
-- {{supportEmail}}. templateBodyAr/templateBodyEn stay set to the IDENTICAL
-- bilingual HTML (same V12 convention) so the recipient sees both languages
-- regardless of which column NotificationEventProcessor.resolveBody() picks.
-- ============================================================================

BEGIN;

UPDATE notif_template
SET template_body_en = $$<div style="font-family:Arial,Helvetica,sans-serif;max-width:600px;margin:0 auto;padding:24px;background-color:#ffffff;color:#1f2937;">
  <div style="text-align:center;padding-bottom:16px;border-bottom:2px solid #2563eb;">
    <h1 style="margin:0;font-size:20px;color:#2563eb;">{{applicationName}}</h1>
  </div>
  <div dir="ltr" lang="en" style="padding:24px 0;text-align:left;">
    <h2 style="font-size:18px;margin:0 0 12px 0;color:#111827;">Password Reset Request</h2>
    <p style="font-size:14px;line-height:1.6;margin:0 0 12px 0;">Hello {{recipientNameEn}},</p>
    <p style="font-size:14px;line-height:1.6;margin:0 0 16px 0;">We received a request to reset your password. Click the button below to continue:</p>
    <div style="text-align:center;margin:24px 0;">
      <a href="{{resetUrl}}" style="background-color:#2563eb;color:#ffffff;text-decoration:none;padding:12px 28px;border-radius:6px;font-size:14px;font-weight:bold;display:inline-block;">Reset Password</a>
    </div>
    <p style="font-size:13px;line-height:1.6;margin:0 0 8px 0;color:#4b5563;">This link will expire on <strong>{{resetExpiryFormattedEn}}</strong>.</p>
    <p style="font-size:13px;line-height:1.6;margin:0;color:#6b7280;">If you did not request a password reset, please ignore this email — your password will remain unchanged. Never share this link with anyone.</p>
  </div>
  <hr style="border:none;border-top:1px solid #e5e7eb;margin:8px 0;">
  <div dir="rtl" lang="ar" style="padding:24px 0;text-align:right;">
    <h2 style="font-size:18px;margin:0 0 12px 0;color:#111827;">طلب إعادة تعيين كلمة المرور</h2>
    <p style="font-size:14px;line-height:1.8;margin:0 0 12px 0;">مرحباً {{recipientNameAr}}،</p>
    <p style="font-size:14px;line-height:1.8;margin:0 0 16px 0;">لقد تلقينا طلباً لإعادة تعيين كلمة المرور الخاصة بحسابك. اضغط على الزر أدناه للمتابعة:</p>
    <div style="text-align:center;margin:24px 0;">
      <a href="{{resetUrl}}" style="background-color:#2563eb;color:#ffffff;text-decoration:none;padding:12px 28px;border-radius:6px;font-size:14px;font-weight:bold;display:inline-block;">إعادة تعيين كلمة المرور</a>
    </div>
    <p style="font-size:13px;line-height:1.8;margin:0 0 8px 0;color:#4b5563;">ستنتهي صلاحية هذا الرابط في <strong>{{resetExpiryFormattedAr}}</strong>.</p>
    <p style="font-size:13px;line-height:1.8;margin:0;color:#6b7280;">إذا لم تطلب إعادة تعيين كلمة المرور، يمكنك تجاهل هذه الرسالة — لن تتغير كلمة مرورك. لا تشارك هذا الرابط مع أي شخص.</p>
  </div>
  <div style="text-align:center;padding-top:16px;border-top:1px solid #e5e7eb;margin-top:8px;">
    <p style="font-size:12px;color:#9ca3af;margin:0;">{{applicationName}} &middot; Support: {{supportEmail}}</p>
  </div>
</div>$$,
    template_body_ar = $$<div style="font-family:Arial,Helvetica,sans-serif;max-width:600px;margin:0 auto;padding:24px;background-color:#ffffff;color:#1f2937;">
  <div style="text-align:center;padding-bottom:16px;border-bottom:2px solid #2563eb;">
    <h1 style="margin:0;font-size:20px;color:#2563eb;">{{applicationName}}</h1>
  </div>
  <div dir="ltr" lang="en" style="padding:24px 0;text-align:left;">
    <h2 style="font-size:18px;margin:0 0 12px 0;color:#111827;">Password Reset Request</h2>
    <p style="font-size:14px;line-height:1.6;margin:0 0 12px 0;">Hello {{recipientNameEn}},</p>
    <p style="font-size:14px;line-height:1.6;margin:0 0 16px 0;">We received a request to reset your password. Click the button below to continue:</p>
    <div style="text-align:center;margin:24px 0;">
      <a href="{{resetUrl}}" style="background-color:#2563eb;color:#ffffff;text-decoration:none;padding:12px 28px;border-radius:6px;font-size:14px;font-weight:bold;display:inline-block;">Reset Password</a>
    </div>
    <p style="font-size:13px;line-height:1.6;margin:0 0 8px 0;color:#4b5563;">This link will expire on <strong>{{resetExpiryFormattedEn}}</strong>.</p>
    <p style="font-size:13px;line-height:1.6;margin:0;color:#6b7280;">If you did not request a password reset, please ignore this email — your password will remain unchanged. Never share this link with anyone.</p>
  </div>
  <hr style="border:none;border-top:1px solid #e5e7eb;margin:8px 0;">
  <div dir="rtl" lang="ar" style="padding:24px 0;text-align:right;">
    <h2 style="font-size:18px;margin:0 0 12px 0;color:#111827;">طلب إعادة تعيين كلمة المرور</h2>
    <p style="font-size:14px;line-height:1.8;margin:0 0 12px 0;">مرحباً {{recipientNameAr}}،</p>
    <p style="font-size:14px;line-height:1.8;margin:0 0 16px 0;">لقد تلقينا طلباً لإعادة تعيين كلمة المرور الخاصة بحسابك. اضغط على الزر أدناه للمتابعة:</p>
    <div style="text-align:center;margin:24px 0;">
      <a href="{{resetUrl}}" style="background-color:#2563eb;color:#ffffff;text-decoration:none;padding:12px 28px;border-radius:6px;font-size:14px;font-weight:bold;display:inline-block;">إعادة تعيين كلمة المرور</a>
    </div>
    <p style="font-size:13px;line-height:1.8;margin:0 0 8px 0;color:#4b5563;">ستنتهي صلاحية هذا الرابط في <strong>{{resetExpiryFormattedAr}}</strong>.</p>
    <p style="font-size:13px;line-height:1.8;margin:0;color:#6b7280;">إذا لم تطلب إعادة تعيين كلمة المرور، يمكنك تجاهل هذه الرسالة — لن تتغير كلمة مرورك. لا تشارك هذا الرابط مع أي شخص.</p>
  </div>
  <div style="text-align:center;padding-top:16px;border-top:1px solid #e5e7eb;margin-top:8px;">
    <p style="font-size:12px;color:#9ca3af;margin:0;">{{applicationName}} &middot; Support: {{supportEmail}}</p>
  </div>
</div>$$,
    updated_by = 'SYSTEM',
    updated_at = now()
WHERE template_code = 'PASSWORD_RESET_REQUESTED';

COMMIT;
