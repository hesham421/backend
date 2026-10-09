# PLATFORM SUMMARY — منصة تخطيط موارد المؤسسات (ERP Platform) — DELTA FIN v2
══════════════════════════════════════════════════════════════════
Profile : erp   Domain profile : v1   Registry : v1.3.1
Delta   : FIN v2 فوق v1 — تُدرَج هنا العناصر المضافة (ADDED) والمعدَّلة (MODIFIED) والمحذوفة (REMOVED) فقط؛ كل ما لم يُذكر يبقى كما في v1 دون تغيير
Dialogue: analysis-dialogue (claude:opus ↔ claude:sonnet) — round 3 — CONVERGED؛ بانتظار تأكيد المستخدم النهائي
══════════════════════════════════════════════════════════════════

## OVERVIEW (MODIFIED — addendum)
يهدف الإصدار v2 من الحسابات العامة / Finance (General Ledger) (FIN) إلى تمكين FIN من اشتقاق
قيد اليومية / Journal Entry وترحيله / Posting انطلاقًا من الحدث المحاسبي القياسي / Canonical
accounting event الذي يُصدره نظام Oracle القديم، تمهيدًا لإيقاف المحاسبة في النظام القديم. يمرّ
هذا الانتقال بتشغيل متوازٍ (shadow mode) تُقارَن فيه قيود النظام القديم بقيود FIN سطرًا بسطر،
لذا يجب أن يحمل كل قيد مبني من حدث الحسابَ الصحيح وإسناد المؤسسة والفرع. الحد الحاكم لهذا الإصدار
هو القاعدة G6 نفسها: الحدث يصرّح بواقعة تجارية فقط، وFIN وحدها تقرر معناها المحاسبي؛ لا رمز حساب
ولا أي مفهوم محاسبي يعبر داخل حمولة الحدث. لا تتغير أي وحدة أخرى في المنصة بهذا الإصدار.

## MODULES (MODIFIED rows only)
| #   | Code | Module | Bounded context | Layer | Type | Depends on | Status |
|-----|------|--------|-----------------|-------|------|------------|--------|
| 2.2 | FIN | الحسابات العامة / Finance (General Ledger) | finance | L3 | transactional/reporting | SEC (HARD), MDL (HARD) — دون تغيير | EXISTING — دلتا v2 (Phase 2 توسّع السجل) |
Status: NEW (Phase 2 produces) · EXISTING (Phase 2 extends) · EXCEPTION (read as-is)
باقي الوحدات دون تغيير: SEC (1.2) وMDL (1.3) كما في v1؛ ORG وPRC وHR وINV وSLS وCTR تبقى RESERVED.
ثبات الترقيم: الرقم 2.2 المُسنَد لـ FIN في v1 لا يتغير؛ الطبقة والنوع دون تغيير.

## DEPENDENCY MAP (MODIFIED lines only)
Build order: دون تغيير — SEC → MDL → FIN [domain-profile §8 #5]
Key dependencies (السطور المعدَّلة فقط):
  FIN → HARD → SEC : النوع دون تغيير؛ يضيف v2 تسجيل شاشة إعداد جديدة واحدة في FIN (روابط الحسابات / Account mappings) وإجراءاتها كبيانات، لا كشيفرة [domain-profile G2؛ §6]
  FIN → HARD → MDL : النوع دون تغيير (الشكل الفيزيائي SOFT-READ وفق ADR-FIN-001)؛ يسجّل v2 نوعَي لوكب جديدين تملكهما FIN (FIN_EVENT_BUSINESS_FIELD، FIN_DIMENSION_VALUE_SOURCE) فيصبح عدد مفاتيح FIN في MDL خمسة عشر بدل ثلاثة عشر [domain-profile G4/G5]
  host (Oracle/ADF القديم) → EVENT → FIN : MODIFIED — المضيف الفعلي في v2 هو نظام Oracle القديم [domain-profile G10]؛ يحمل الحدث وقائع تجارية فقط (طريقة الدفع، رمز نوع المصروف، رمز المؤسسة، رمز الفرع) ولا يحمل رمز حساب ولا أي مفهوم محاسبي [domain-profile G6]

## DEFERRED (ADDED rows — not in scope for this version)
| Item | Reason / activation trigger |
|---|---|
| طابور قاعدة البيانات، خدمة المستهلك (consumer service)، وكل ما داخل نظام Oracle القديم | استبعاد صريح من المستخدم لهذا الإصدار؛ يوثَّق خارج FIN [domain-profile §1] |
| تعبئة روابط الحسابات الفعلية للترحيل (seeding) | بيانات ترحيل (migration data) تُنتَج بعد تسليم مخزن الربط |
| مصدر أحداث ثانٍ أو وسيط رسائل (broker) | يُضاف لاحقًا كمحوّل (adapter) إضافي دون إعادة بناء |
| مصادقة المستدعي الآلي (machine-caller authentication) للمستهلك | دلتا لوحدة SEC تُتابَع منفصلة، ليست جزءًا من هذا الإصدار |
| ربط قيم الأبعاد (ترجمة رموز المؤسسة/الفرع القديمة إلى قيم أبعاد FIN) | محسوم بالحوار — تأجيل؛ التفعيل: عند ثبوت اختلاف رموز النظام القديم عن رموز قيم الأبعاد في FIN |
| استبعادات v1 (تعدد العملات، تعدد الدفاتر والكيانات، المعاملات بين الشركات، الحسابات الإحصائية، التقويم متعدد الأنماط، المرفقات) | تبقى قائمة دون تغيير [domain-profile §1] |

## RESOLVED DECISIONS (this phase — ADDED)
| # | Point | Recommended | Confirmed by user | Sources |
|---|---|---|---|---|
| PS-v2-1 | تصنيف التغيير على مستوى المنصة | FIN وحدة EXISTING بدلتا v2 برقمها 2.2؛ لا وحدة جديدة ولا تغيير في الرقم أو الطبقة (L3) أو النوع (transactional/reporting) | نعم — منصوص في طلب التغيير | change manifest FIN v2 (Summary)؛ project-registry MODULE INDEX |
| PS-v2-2 | أنواع الاعتماديات بين الوحدات | دون تغيير (SEC: HARD، MDL: HARD، المضيف: EVENT فقط)؛ الإضافات تسجيل بيانات فقط | نعم — منصوص (التغيير محصور في FIN) | change manifest FIN v2 §1–§4؛ [KB:erp-domain-standards §5]؛ ADR-FIN-001 |

## OPEN ITEMS
None — platform scope fully determined. توصيات الوحدة الخمس محسومة بالحوار (الجولة 3) ومسجّلة في
module-registry-fin.md وbusiness-policies-fin.md؛ بانتظار تأكيد المستخدم النهائي على تلك التوصيات فقط
(وليس على نطاق المنصة، المحسوم بالكامل).

## NEXT STEP
Reply with a plain instruction to adjust, or with a module number to start Phase 2.
══════════════════════════════════════════════════════════════════
