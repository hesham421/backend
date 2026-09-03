# PRD — Common Utils (CU)
══════════════════════════════════════════════════════════════════
Module          : Common Utils (CU prefix)
Source artifacts: platform-summary.md, module-registry-CU.md,
                  business-policies-CU.md
Status          : DRAFT — awaiting Reconciliation Gate (Project 2.5)
Open Questions  : None — see OQ Log
══════════════════════════════════════════════════════════════════

## USER STORIES

US-CU-001
  Story    : كمشغّل للمنصة، أحتاج ضبط إعدادات المنصة وقت التشغيل
             كمدخلات key/value دون إعادة نشر، ليبقى النظام قابلاً للتهيئة.
  Priority : —
  Success metric : —
  Source   : module-registry-CU.md §ENTITIES OWNED (AppConfiguration)
             + §RESPONSIBILITIES (Configuration);
             business-policies-CU.md §POLICY-CLI-02 (Configurable)
  Status   : DRAFT

## STORIES EXCLUDED (لا NEED نهائي متتبَّع — بنية تحتية code)

  — Specification / Filtering — آلية استعلام/predicate مشتركة؛ pure
    code، لا كيان، لا حاجة مستخدم مستقلة.
  — Global Exceptions — بنية معالجة أخطاء؛ الـ Error Catalog / ERR-IDs
    مُلك P3، ليست حاجة مستخدم في CU.
  — Events (in-process bus) — آلية نشر/استماع داخلية؛ لا سطح مستخدم.
  — Bundle (i18n AR/EN) — آلية حلّ رسائل (resource bundles ملفّية)؛
    بنية تحتية، متّسقة مع الثلاثة أعلاه. الرسائل المترجمة تظهر عبر
    الموديول المستهلِك، لا كميزة منتج في CU.

## DESIGN CONSTRAINTS (سياق فقط — ليست قصصاً)

  سياسات تصميم شاملة من P0، تُطبَّق على كل موديولات Foundation؛
  P1 يحوّلها لـ RULE-IDs حيث تصبح قابلة للإنفاذ.
  — POLICY-CLI-01 : تعقيد متوسط — أبسط حل يفي بالمتطلب؛ لا إفراط هيكلة.
  — POLICY-CLI-02 : Reusable + Configurable + Integrable + Composable.
  — POLICY-CLI-03 : بناء كامل مستقل — لا أجزاء مؤجّلة داخل Foundation.
  Source: business-policies-CU.md §CLIENT-SPECIFIC POLICIES.

## OPEN ITEMS (ambiguous, not yet a story)

  None — الغموضان السابقان حُسِما (2026-09-01): i18n صُنّف بنية تحتية
  (مستبعَد)، وسياسات التصميم حُمِلت كقيود لا كقصص.

══════════════════════════════════════════════════════════════════
*End of prd-CU.md*
*Next stage: Project 2.5 (UI/UX Design Engine) — requires this file
 AND srs.md together (CONTRACT-11). Does not gate Project 1.*
══════════════════════════════════════════════════════════════════
