# PRD — File Service (FILE)
══════════════════════════════════════════════════════════════════
Module          : File Service (FILE prefix)
Source artifacts: platform-summary.md, module-registry-FILE.md,
                  business-policies-FILE.md
Status          : DRAFT — awaiting Reconciliation Gate (Project 2.5)
Open Questions  : None — see OQ Log
══════════════════════════════════════════════════════════════════

## USER STORIES

US-FILE-001
  Story    : كمستخدم، أحتاج رفع ملف وتخزينه بأمان، ليُحفظ لاسترجاعه لاحقاً.
  Priority : —
  Success metric : —
  Source   : module-registry-FILE.md §ENTITIES OWNED (FileDocument);
             §SCOPE NOTE (bytes في PostgreSQL BYTEA)
  Status   : DRAFT

US-FILE-002
  Story    : كمستخدم، أحتاج الوصول إلى ملفي عبر رابط آمن محدود المدة
             (ينتهي بعد فترة)، ليبقى الوصول تحت التحكّم.
  Priority : —
  Success metric : —
  Source   : module-registry-FILE.md §SCOPE NOTE (AES/GCM encrypted URL token,
             TTL ~100 min); business-policies-FILE.md §POLICY-CLI-03
  Status   : DRAFT

US-FILE-003
  Story    : كأدمن/موديول مستهلِك، أحتاج تعريف فئات المستندات مع أنواعها
             وحدود حجمها، لتُتحقَّق عمليات الرفع حسب كل فئة.
  Priority : —
  Success metric : —
  Source   : module-registry-FILE.md §ENTITIES OWNED (FileCategory);
             business-policies-FILE.md §POLICY-CLI-01 / §POLICY-CLI-02
  Status   : DRAFT

US-FILE-004
  Story    : كموديول مستهلِك، أحتاج إرفاق ملفات بسجلاتي أياً كان الموديول
             (ملكية عامة owner_id/owner_type/module_code)، ليكون تخزين
             الملفات قابلاً لإعادة الاستخدام عبر المنصة.
  Priority : —
  Success metric : —
  Source   : module-registry-FILE.md §SCOPE NOTE (generic ownership,
             provider pattern)
  Status   : DRAFT

US-FILE-005
  Story    : كمستخدم، أحتاج أرشفة أو إزالة الملفات التي لم أعد أحتاجها،
             لإدارة دورة حياة ملفاتي.
  Priority : —
  Success metric : —
  Source   : module-registry-FILE.md §LOVs OWNED (FileStatus:
             ACTIVE/ARCHIVED/DELETED)
  Status   : DRAFT

## STORIES EXCLUDED (justified)

  — FileType / FileStatus (LOVs) — قوائم قيم داعمة للقصص أعلاه، لا قصص
    مستقلة.

## SCOPE EXCLUSIONS (خارج النطاق — من P0)

  — PDF processing/preview (PDFBox) — business-policies-FILE §SCOPE EXCEPTIONS.
  — Async file pipeline / message broker (RabbitMQ) — كل العمليات متزامنة.
  — External filesystem storage — البايتات في DB (BYTEA).

## OPEN ITEMS (ambiguous, not yet a story)

  ? دلالة الحذف (حذف دائم مقابل soft-delete عبر status=DELETED) — قرار
    مؤجّل لـ P1 (business-policies-FILE §SCOPE EXCEPTIONS)؛ الـ LOV
    يدعم الحالتين. لا يفصّل في PRD.

══════════════════════════════════════════════════════════════════
*End of prd-FILE.md*
*Next stage: Project 2.5 (UI/UX Design Engine) — requires this file
 AND srs.md together (CONTRACT-11). Does not gate Project 1.*
══════════════════════════════════════════════════════════════════
