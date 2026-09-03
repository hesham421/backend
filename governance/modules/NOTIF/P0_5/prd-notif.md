# PRD — Notification Service (NOTIF)
══════════════════════════════════════════════════════════════════
Module          : Notification Service (NOTIF prefix)
Source artifacts: platform-summary.md, module-registry-NOTIF.md,
                  business-policies-NOTIF.md
Status          : DRAFT — awaiting Reconciliation Gate (Project 2.5)
Open Questions  : None — see OQ Log
══════════════════════════════════════════════════════════════════

## USER STORIES

US-NOTIF-001
  Story    : كموديول مستهلِك، أحتاج إشعار المستخدم عبر قناة واحدة أو
             أكثر، ليصله ما يخصّه من أحداث.
  Priority : —
  Success metric : —
  Source   : module-registry-NOTIF.md §SCOPE NOTE (multi-channel dispatch);
             entity NotificationLog; business-policies-NOTIF.md §POLICY-CLI-01
  Status   : DRAFT

US-NOTIF-002
  Story    : كموديول مُرسِل، أحتاج اختيار القناة/القنوات لكل إشعار عبر
             channelHint، لتبقى منطقية اختيار القناة عند الموديول لا عند
             خدمة الإشعارات.
  Priority : —
  Success metric : —
  Source   : business-policies-NOTIF.md §POLICY-CLI-02 (sending module owns
             channel choice); module-registry-NOTIF.md §SCOPE NOTE (channelHint)
  Status   : DRAFT

US-NOTIF-003
  Story    : كأدمن، أحتاج قوالب رسائل ثنائية اللغة (عربي/إنجليزي) مع
             إمكانية إرفاق ملف اختياري، لتكون الإشعارات متّسقة ومترجمة.
  Priority : —
  Success metric : —
  Source   : module-registry-NOTIF.md §ENTITIES OWNED (NotificationTemplate —
             inline body_ar/en; optional file_id attachment)
  Status   : DRAFT

US-NOTIF-004
  Story    : كمشغّل، أحتاج تمكين/تعطيل كل قناة وحفظ إعدادات مزوّدها كبيانات،
             لتكون القنوات قابلة للتهيئة دون تعديل الكود.
  Priority : —
  Success metric : —
  Source   : module-registry-NOTIF.md §ENTITIES OWNED (NotificationChannelConfig
             — per-channel enable flag + provider config_json)
  Status   : DRAFT

US-NOTIF-005
  Story    : كمشغّل، أحتاج رؤية حالة كل إشعار ونتيجة تسليمه (أُرسل / فشل /
             القناة معطّلة)، لتتبّع مشاكل التسليم.
  Priority : —
  Success metric : —
  Source   : module-registry-NOTIF.md §ENTITIES OWNED (NotificationLog);
             §LOVs OWNED (NotificationStatus); business-policies-NOTIF.md
             §POLICY-CLI-03 (retry then fail)
  Status   : DRAFT

## STORIES EXCLUDED (justified)

  — NotificationChannel / NotificationStatus (LOVs) — قوائم قيم داعمة
    للقصص أعلاه، لا قصص مستقلة.
  — سياسة إعادة المحاولة الدقيقة (5 محاولات، backoff 1.5x) — تفصيل
    RULE-level يقرّره P1؛ الحاجة (رؤية النتيجة) مغطّاة في US-NOTIF-005.

## SCOPE EXCLUSIONS (خارج النطاق — من P0)

  — Apache Camel — البريد عبر Spring JavaMailSender مباشرةً.
  — RabbitMQ / وسيط رسائل خارجي — الإطلاق عبر أحداث Spring داخلية (CU).

## OPEN ITEMS (ambiguous, not yet a story)

  ? اختيار مزوّد SMS / WhatsApp / Push الملموس (Twilio / Unifonic /
    Meta Cloud API / Firebase) — مؤجّل كقرار تقني P3، غير حاجب لـ P0/P1؛
    شكل الجدول مستقل عن المزوّد والاعتمادات في config_json
    (business-policies-NOTIF §SCOPE EXCEPTIONS + platform-summary §OPEN ITEMS).

══════════════════════════════════════════════════════════════════
*End of prd-NOTIF.md*
*Next stage: Project 2.5 (UI/UX Design Engine) — requires this file
 AND srs.md together (CONTRACT-11). Does not gate Project 1.*
══════════════════════════════════════════════════════════════════
