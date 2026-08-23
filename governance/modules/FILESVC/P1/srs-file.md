<!-- ═══════════════════════════════════════════════════════════ -->
<!-- SRS — وثيقة التحليل والمتطلبات                             -->
<!-- Governed by: SRS Governance Engine (Project 1)             -->
<!-- Compatible: PROJECT-2 | PROJECT-3 | PROJECT-4              -->
<!-- Structure : PART A (Module Foundation) + PART B (Screens)  -->
<!-- ═══════════════════════════════════════════════════════════ -->

# وثيقة التحليل (SRS)
## خدمة الملفات | File Service

---

# ══════════════════════════════════════════════════════════
# PART A — MODULE FOUNDATION
# Single source of truth — read once per module
# ══════════════════════════════════════════════════════════

---

## A1 — معلومات الوثيقة (Document Information)

| البند | القيمة |
|---|---|
| **اسم المشروع** | نظام إدارة الموارد المؤسسية (ERP) |
| **الموديول** | File Service — خدمة الملفات |
| **Feature Code** | FILE-001 |
| **Feature Type** | **Engine / Foundation Module** — تصنيف غير قياسي (خارج الأنواع الأربعة في 5.1.2: Master/Transactional/Configuration/Reference). السبب: موديول محرك (Type: Engine — module-registry-file.md) يُستهلك من موديولات أخرى، لا سجل أعمال بحد ذاته. بنية FileDocument أقرب لـ Transactional (statusId lifecycle) لكن بدون fiscal_year/period (لا ينطبق على ملف). ERP-DEFAULT: تم تكييف قالب 5.4.2 بدلاً من تطبيقه حرفياً — موثّق أدناه A3. |
| **الإدارة / القسم** | البنية التحتية المشتركة (Layer-1 Foundation) |
| **إعداد بواسطة** | SRS Governance Engine (Project 1) |
| **النسخة** | 1.0 |
| **التاريخ** | 2026-07-11 |
| **الحالة** | Draft |
| **Open Questions** | None — see OQ Log |
| **Governed by** | SRS Governance Engine (Project 1) |
| **DB_TARGET** | POSTGRESQL_16 (master-registry §1 / business-policies-file.md) |
| **P0 Readiness** | READY ✓ — master-registry §15 (2026-07-11), لا AQ-IDs مفتوحة |

---

## A2 — السياق الوظيفي (Functional Context)

### ما يشمله هذا الموديول

> يوفّر File Service قدرة مركزية على مستوى المنصة لتخزين واسترجاع وحذف الملفات الثنائية
> (مرفقات، وثائق، صور) نيابةً عن أي موديول آخر (Procurement، Sales، Notification، Audit، ...).
> يخزّن الملف كمحتوى ثنائي (BYTEA) داخل نفس قاعدة بيانات المنصة — لا تخزين خارجي (filesystem)
> ولا اعتماد على خدمة منفصلة. يوفّر أيضاً آلية أمان خاصة لمشاركة روابط الرفع/التنزيل مع
> واجهات Angular وFlutter عبر Encrypted Token (مستقلة تماماً عن JWT الخاص بـ Security).

### ما لا يشمله هذا الموديول

> - لا يتحقق من هوية المستخدم (JWT) — هذه مسؤولية Security Module (1.2) حصراً.
> - لا يقرر أي موديول يحتاج أي فئة ملفات (FileCategory) — كل موديول مستهلك يقرر فئاته بنفسه.
> - لا معالجة محتوى الملفات (لا PDF parsing، لا OCR، لا معاينة) — تم استبعاد PDFBox نهائياً
>   (ARCH-REF RESOLUTION-03).
> - لا سلة محذوفات (Recycle Bin) — الحذف نهائي دائماً في هذه المرحلة.
> - لا تكامل عبر RabbitMQ — كل التكامل مع الموديولات الأخرى مباشر (Direct @Service injection).

### وظيفة الموديول

> يتيح File Service لأي موديول في المنصة رفع ملف وربطه بسجل أعمال معيّن (owner)، ثم استرجاعه
> أو حذفه لاحقاً، مع حماية الروابط عبر رمز مشفّر ذي صلاحية زمنية محدودة، دون أن يحتاج الموديول
> المستهلك لبناء أي منطق تخزين ملفات خاص به.

### الوصف الوظيفي التفصيلي

> عند حاجة موديول ما لرفع ملف (مثلاً مرفق أمر شراء)، يطلب أولاً Encrypted Upload Token من
> File Service (API-FILE-001)، ثم يستخدم هذا الرمز لرفع الملف الفعلي (API-FILE-002) عبر
> multipart/form-data. يكتشف File Service نوع الملف (MIME) من المحتوى نفسه — لا يثق بترويسة
> Content-Type القادمة من العميل — ويتحقق أن الحجم لا يتجاوز الحد المسموح (5MB افتراضياً، أو
> حد مخصص حسب FileCategory). عند الحاجة للتنزيل، يُطلب Encrypted Download Token ثم يُستخدم
> لاسترجاع المحتوى كـ binary stream (API-FILE-003). الحذف نهائي ومقيَّد بالمالك أو Admin فقط
> (API-FILE-004، عبر Encrypted Delete Token). كل رمز صالح لمدة 100 دقيقة افتراضياً، ولاستخدام
> واحد فقط لكل نية رفع.

#### الوضع الحالي

> لا ينطبق — موديول تحتي جديد (Foundation) لا يستبدل عملية يدوية أو نظاماً قائماً؛ هذه أول
> بنية موحّدة لتخزين الملفات على مستوى المنصة.

#### الصعوبات الحالية

| # | الصعوبة |
|---|---|
| 1 | غياب آلية موحّدة لتخزين المرفقات يجبر كل موديول مستقبلي على بناء منطقه الخاص لو لم يوجد هذا الموديول. |
| 2 | الحاجة لمشاركة روابط ملفات مع الواجهة الأمامية دون كشف مسار داخلي أو الاعتماد على JWT طويل الأمد في الروابط. |

#### النظام المقترح وفوائده

| # | الفائدة |
|---|---|
| 1 | نقطة واحدة موحّدة لتخزين واسترجاع الملفات لكل موديولات المنصة — لا تكرار منطق. |
| 2 | روابط رفع/تنزيل محمية بتشفير AES/GCM وصلاحية زمنية قصيرة — أمان أعلى من رابط ثابت. |
| 3 | فصل واضح بين مصادقة الجلسة (JWT — Security) وأمان الوصول للملف (Encrypted Token — File Service). |

### آلية الأمان الخاصة — Encrypted Token (ملاحظة معمارية إلزامية القراءة)

> كل عملية (رفع/تنزيل/حذف) تتطلب Encrypted Token (AES/GCM، IV 12-byte، GCM Tag 128-bit)
> مُضمَّن في مسار الـ URL — وليس في ترويسة Authorization. صلاحية الرمز 100 دقيقة (قابلة
> للإعداد)، والرمز صادر عن File Service نفسها (POLICY-CLI-03)، لا العميل. هذه آلية أمان
> على مستوى الوصول للـ API — **وليست قاعدة تحقق أعمال (Business Validation Rule)** — لذلك
> تفاصيل التشفير (خوارزمية AES/GCM، أطوال IV/Tag) لا تُوثَّق كـ RULE-ID في A4 (هذه تفاصيل
> تنفيذية تخص P3 — B3 Security Implementation). أما **السلوك الملاحظ من طرف المستخدم**
> الناتج عن هذه الآلية (انتهاء الصلاحية، رفض رمز مستخدَم مسبقاً، رفض رمز لا يطابق الإجراء)
> فهو موثَّق كقواعد أعمال قياسية (RULE-FILE-002/003/004 في A4) لأنه سلوك يُختبر ويُترجَم
> لرسالة تُعرض على المستخدم — هذا التمييز مصدره توجيه ARCH-REF-1.10 §P1 صراحةً.
>
> **استثناء صريح — POLICY-CLI-06 ("No JWT validation inside the module"):** هذه السياسة
> **مُعفاة عمداً** من أي RULE-ID في A4. السبب: هي قرار حدود معمارية (trust-boundary) يصف
> ما لا يفعله الموديول (لا يتحقق من JWT بنفسه — يثق بـ Security's filter chain) — ولا ينتج
> عنها أي سلوك عمل مُلاحَظ أو رسالة تُعرض على مستخدم نهائي يمكن اختبارها كـ RULE-ID (لا يوجد
> "طلب MUST يُرفَض" ناتج مباشرة عنها؛ الرفض الفعلي لطلب غير مُصادَق عليه هو من اختصاص
> Security's filter chain نفسه، خارج نطاق هذا الموديول تماماً). هذا الاستثناء يتبع نفس منطق
> استبعاد تفاصيل تشفير Encrypted Token أعلاه من A4 — الفرق أنه هنا مُصرَّح به بالاسم صراحة.

### ملاحظات عامة

- File Service لا تملك اعتماد بيانات (Data-FK) على أي موديول آخر — الاعتماد على Security
  هو Trust-Boundary فقط (JWT filter chain)، لا FK فعلي على أي جدول (module-registry-file.md).
- FILE_DOCUMENT كيان SHARED (owner) — مُستهلَك مستقبلاً من NotificationService (DEFERRED عبر
  RXE-NOTIF)، AuditService، وموديولات 3.x (NOT-YET-ASSIGNED جميعاً). لا XM-ID وارد فعلياً بعد.
- PDFBox مُستبعَد نهائياً (ليس مؤجَّلاً) — RESOLUTION-03، ARCH-REF-1.10.
- RabbitMQ مُستبعَد من المسار الأساسي — كل التكامل عبر استدعاء مباشر — RESOLUTION-04.

---

## A3 — الكيانات والحقول (Entities & Fields)

---

### ENTITY-FILE-001 — FileDocument

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner) — مستهلكون مستقبليون: NotificationService (HARD-FK, DEFERRED), AuditService (NOT-YET-ASSIGNED), موديولات 3.x (NOT-YET-ASSIGNED) |
| **Business Code** | **لا يوجد — انحراف موثَّق عن القالب المعياري (5.4.2)**. السبب: FileDocument سجل يُدار بواسطة المحرك (Engine-managed) وليس سجل أعمال يواجه المستخدم بحاجة لرمز عمل قابل للقراءة البشرية. المعرّف الوحيد هو fileDocumentPk (تسلسلي تلقائي). المصدر: ARCH-REF-1.10 لا يذكر Business Code لهذا الكيان في أي موضع؛ PK تسلسلي فقط (ADAPT-05). |
| **المصدر** | ARCH-REF-1.10-FILE-SERVICE.md §P2 (RESOLUTION-01، ADAPT-01، ADAPT-05) |
| **العمليات** | Create (Upload), Read (Download/List), Delete (نهائي) — **لا Update** (لا تعديل ملف موجود؛ الاستبدال = حذف + رفع جديد) |
| **Cross-Module** | None consumed — هذا الكيان مُصدَّر (owner) لا مستهلِك |

#### حقول الكيان

| اسم الحقل | نوع البيانات (*) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| fileDocumentPk | BIGINT (PK) | نظام | — | رقم إنشائي تلقائي | المعرف | ID |
| ownerId | BIGINT | نعم | — | **لا FK فعلي — مرجع متعدد الأشكال (polymorphic)**: يشير لسجل في جدول يُحدَّده ownerType، يختلف حسب الموديول المُنتِج. قرار موثَّق (ADAPT-05) | معرف الكيان المالك | Owner Entity ID |
| ownerType | VARCHAR(100) | نعم | نص حر — اسم كيان الموديول المُنتِج (مثال: PURCHASE_ORDER) — **ليس LOV محكوم** — نفس نمط NOTIF_LOG.reference_type | لا يُضاف كموديول جديد يتطلب لمس File Service | نوع الكيان المالك | Owner Entity Type |
| moduleCode | VARCHAR(20) | نعم | نص حر — كود الموديول المُنتِج (مثال: PRC، FIN) | — | كود الموديول | Module Code |
| fileCategoryFk | BIGINT (FK) | نعم | ENTITY-FILE-002 | مرتبط بـ FileCategory | فئة الملف | File Category |
| fileTypeId | VARCHAR(50) | نظام | LOV-FILE-001 | يُكتشف تلقائياً من محتوى الملف — RULE-FILE-005 | نوع الملف | File Type |
| fileNameOriginal | VARCHAR(255) | نعم | — | اسم الملف كما رفعه المستخدم | اسم الملف | File Name |
| mimeType | VARCHAR(100) | نظام | — | يُكتشف تلقائياً من المحتوى — لا يُعتمَد على ترويسة العميل — RULE-FILE-005 | نوع MIME | MIME Type |
| fileSizeBytes | BIGINT | نظام | — | محسوب عند الرفع، بالبايت | حجم الملف | File Size |
| fileContent | **Binary (BYTEA)** | نعم | — | ⚠ امتداد على جدول CORE-8 (لا يغطي أنواعاً ثنائية) — قرار موثَّق: RESOLUTION-01، business-policies-file.md "Platform Integration Notes". POSTGRESQL_16 حصراً — لا Large Objects | محتوى الملف | File Content |
| fileStatusId | VARCHAR(50) | نعم | LOV-FILE-002 | Status Lifecycle — 3 حالات — انظر A6. الحذف الفعلي (RULE-FILE-006) نهائي، وDELETED تشير لسجلات تم تطهير محتواها الثنائي مع بقاء الـ metadata | حالة الملف | File Status |
| createdBy | VARCHAR(255) | نظام | — | AuditEntityListener — لا يُقبل في DTO | أنشئ بواسطة | Created By |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener — لا يُقبل في DTO | تاريخ الإنشاء | Created At |
| updatedBy | VARCHAR(255) | نظام | — | AuditEntityListener — لا يُقبل في DTO | عُدِّل بواسطة | Updated By |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener — لا يُقبل في DTO | تاريخ التعديل | Updated At |

(*) نوع البيانات حسب DB_TARGET = POSTGRESQL_16 (CORE-8) — BIGINT/VARCHAR/TIMESTAMP/SMALLINT.
    **استثناء موثَّق:** fileContent يستخدم BYTEA — نوع غير مُدرَج في جدول CORE-8 القياسي؛
    هذا امتداد خاص بهذا الموديول فقط، مصدره قرار معماري صريح (RESOLUTION-01)، وليس اختراعاً
    من P1. لا notes field — غير مطلوب في ARCH-REF لهذا الكيان (لا invention beyond source، HR-1).

> **قاعدة Label إلزامية:** مطبَّقة على كل حقل أعلاه.

---

### ENTITY-FILE-002 — FileCategory

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner) — مستهلكون: كل موديول يحتاج تصنيف مستندات (Procurement/Sales/Notification/...) |
| **Business Code** | **لا — انحراف موثَّق**. categoryCode يُحدَّد يدوياً عند الإنشاء من قبل الموديول/الإدمن (نمط شبيه بـ lookupKey — ثابت بعد الإنشاء)، وليس رمزاً تسلسلياً مولَّداً آلياً حسب BC-RULE-2. مصنَّف كـ Reference Table (نفس نمط ORG_REGION_TYPE) لأن مجموع القيم عبر كل الموديولات سيتجاوز 15 (master-registry §6 Lookup Governance Rule). |
| **المصدر** | ARCH-REF-1.10-FILE-SERVICE.md §P2؛ module-registry-file.md AUTO-DECISIONS |
| **العمليات** | Create, Read, Update, Deactivate — يُدار بواسطة Admin (كل موديول مستهلك يُنشئ فئاته الخاصة دون لمس كود File Service) |
| **Cross-Module** | None consumed |

#### حقول الكيان

| اسم الحقل | نوع البيانات (*) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| fileCategoryPk | BIGINT (PK) | نظام | — | رقم إنشائي تلقائي | المعرف | ID |
| categoryCode | VARCHAR(50) | نعم | — | فريد ضمن نفس moduleCode — ثابت بعد الإنشاء (نمط lookupKey) | رمز الفئة | Category Code |
| moduleCode | VARCHAR(20) | نعم | نص حر | الموديول المالك لهذه الفئة (مثال: NOTIFICATION، PRC) | كود الموديول | Module Code |
| nameAr | VARCHAR(200) | نعم | — | الاسم بالعربي | الاسم بالعربي | Name (Arabic) |
| nameEn | VARCHAR(100) | نعم | — | الاسم بالإنجليزي | الاسم بالإنجليزي | Name (English) |
| maxSizeBytesOverride | BIGINT | لا | — | تجاوز اختياري للحد الافتراضي 5MB — RULE-FILE-001 | حد الحجم المخصص | Max Size Override |
| allowedTypesNote | VARCHAR(500) | لا | — | ملاحظة نصية حرة بأنواع الملفات المسموحة لهذه الفئة (اختياري — إرشادي وليس قيداً مُلزَماً) | الأنواع المسموحة | Allowed Types |
| isActiveFl | SMALLINT | نعم | 1 / 0 | 1 = نشط | نشط | Active |
| createdBy | VARCHAR(255) | نظام | — | AuditEntityListener | أنشئ بواسطة | Created By |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ الإنشاء | Created At |
| updatedBy | VARCHAR(255) | نظام | — | AuditEntityListener | عُدِّل بواسطة | Updated By |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ التعديل | Updated At |

(*) DB_TARGET = POSTGRESQL_16.

---

## A4 — قواعد التحقق (Business Rules)

> **قاعدة إلزامية:** هذا القسم هو المصدر الوحيد لتعريف القواعد. PART B يُشير إليها بـ RULE-ID فقط.
> **ملاحظة نطاق:** تفاصيل تشفير Encrypted Token (خوارزمية/IV/Tag) ليست هنا — انظر A2. القواعد
> أدناه تغطي فقط السلوك المُلاحَظ من المستخدم/العميل الناتج عن تلك الآلية.

---

### RULE-FILE-001 — حد حجم محتوى الملف

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-FILE-001 |
| **Trigger** | عند الرفع |
| **Statement** | The system MUST reject any uploaded file content exceeding 5MB (default) or the FileCategory-specific `maxSizeBytesOverride` when set, before persisting `fileContent`. |
| **Message-AR** | حجم الملف يتجاوز الحد المسموح به |
| **Message-EN** | File size exceeds the allowed limit |
| **Source** | POLICY-CLI-01 (business-policies-file.md — Client Policy) |
| **Test-Hint** | تحقّق من رفض ملف > 5MB بـ 400؛ وتحقّق من تطبيق `maxSizeBytesOverride` عند وجوده على FileCategory بدل الحد الافتراضي |

### RULE-FILE-002 — انتهاء صلاحية رابط العملية (Token TTL)

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-FILE-001 |
| **Trigger** | عند أي طلب رفع/تنزيل/حذف عبر رابط يحمل Token |
| **Statement** | The system MUST reject any file operation request whose Encrypted Token has exceeded its TTL (100 minutes default), returning 401 before reaching business logic. |
| **Message-AR** | انتهت صلاحية الرابط — يرجى طلب رابط جديد |
| **Message-EN** | This link has expired — please request a new one |
| **Source** | POLICY-CLI-02 |
| **Test-Hint** | طلب بعد انتهاء TTL → 401 |

### RULE-FILE-003 — رفض الرمز التالف أو غير المطابق للإجراء

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-FILE-001 |
| **Trigger** | عند أي طلب على /upload، /download، أو حذف عبر Token |
| **Statement** | The system MUST reject any request whose token is missing, tampered (GCM tag mismatch), was not issued by File Service's own token-issuing endpoint, or whose embedded action does not match the target endpoint, returning 401/403 before reaching business logic. |
| **Message-AR** | الرابط غير صالح |
| **Message-EN** | The link is invalid |
| **Source** | POLICY-CLI-02 + POLICY-CLI-03 ("Token generation is File Service's responsibility" — enforced here: a client-constructed token fails GCM authentication and is rejected by this same rule, since only File Service holds the signing key) |
| **Test-Hint** | رمز صادر لعملية download يُستخدَم على endpoint الحذف → 403؛ رمز مُركَّب يدوياً من العميل (وليس صادراً عبر API-FILE-001) → فشل GCM authentication → 401 |

### RULE-FILE-004 — الرمز أحادي الاستخدام

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-FILE-001 |
| **Trigger** | عند كل نية رفع جديدة |
| **Statement** | The system MUST issue a new Encrypted Token for every upload intent — a previously consumed token MUST NOT be accepted for a subsequent operation. |
| **Message-AR** | هذا الرابط مُستخدَم مسبقاً |
| **Message-EN** | This link has already been used |
| **Source** | POLICY-CLI-02 ("single-use, time-bound Encrypted Token") |
| **Test-Hint** | إعادة استخدام رمز رفع تم استهلاكه سابقاً → رفض |

### RULE-FILE-005 — اكتشاف نوع الملف من المحتوى فقط

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-FILE-001 |
| **Trigger** | عند الرفع |
| **Statement** | The system MUST determine the file's MIME type from the file content itself and MUST NOT rely on the client-supplied Content-Type header for any validation or classification decision. |
| **Message-AR** | نوع الملف يُحدَّد تلقائياً من محتواه |
| **Message-EN** | File type is automatically determined from its content |
| **Source** | POLICY-CLI-05 |
| **Test-Hint** | رفع ملف بترويسة Content-Type مزوَّرة → يُصنَّف حسب المحتوى الفعلي وليس الترويسة |

### RULE-FILE-006 — نهائية الحذف

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-FILE-001 |
| **Trigger** | عند الحذف |
| **Statement** | The system MUST permanently delete the file's content upon a valid delete request — no Recycle Bin or recovery mechanism exists in this phase. |
| **Message-AR** | سيتم حذف الملف نهائياً ولا يمكن استرجاعه |
| **Message-EN** | The file will be permanently deleted and cannot be recovered |
| **Source** | POLICY-CLI-04 |
| **Test-Hint** | — |

> **توضيح إلزامي (يحسم OQ-001):** "الحذف النهائي" هنا يعني تطهير `fileContent` (والحقول
> الثنائية المرتبطة) فقط — **لا يُحذَف صف FILE_DOCUMENT من الجدول فعلياً**. الصف ينتقل
> لحالة `fileStatusId = DELETED` (A6) ويبقى قائماً مع بياناته الوصفية (fileNameOriginal،
> ownerId/ownerType، audit trail) دون تعديل `fileDocumentPk`. هذا يحافظ تلقائياً على سلامة
> أي HARD-FK من موديول مستهلك (Notification/Audit/3.x) — لا orphaned reference ولا حاجة
> لأي RESTRICT/CASCADE على مستوى DB لهذا السبب. ذُكر مسبقاً في A3 (fileStatusId) وA6 —
> هذا السطر يجعله صريحاً كقاعدة تحقق رسمية.

### RULE-FILE-007 — تقييد الحذف بالمالك أو Admin

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-FILE-001 |
| **Trigger** | عند الحذف |
| **Statement** | The system MUST reject a delete request (403) from an actor who is neither the file's owning entity's authorized actor nor holding an Admin role. |
| **Message-AR** | غير مصرح لك بحذف هذا الملف |
| **Message-EN** | You are not authorized to delete this file |
| **Source** | POLICY-CLI-04 |
| **Test-Hint** | محاولة حذف ملف من entity/موديول مختلف عن المالك (وبدون دور Admin) → 403 |

---

## A5 — قوائم القيم (LOV / Lookup)

---

### LOV-FILE-001 — نوع الملف (FileType)

| البند | القيمة |
|---|---|
| **الحقل** | fileTypeId |
| **ENTITY-ID** | ENTITY-FILE-001 |
| **نوع التحكم** | Dropdown (≤15) — نظام (لا إدخال مستخدم، يُكتشف تلقائياً) |
| **lookupKey** | FILE_TYPE |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | File Service |
| **API الاستهلاك** | GET /api/lookups/FILE_TYPE?active=true |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| IMAGE | صورة | Image |
| DOCUMENT | مستند | Document |
| SPREADSHEET | جدول بيانات | Spreadsheet |
| ARCHIVE | أرشيف مضغوط | Archive |
| OTHER | أخرى | Other |

⚠ مؤكَّدة في master-registry §6 (Owner: FileService).

---

### LOV-FILE-002 — حالة الملف (FileStatus)

| البند | القيمة |
|---|---|
| **الحقل** | fileStatusId |
| **ENTITY-ID** | ENTITY-FILE-001 |
| **نوع التحكم** | Dropdown (≤15) — Status Lifecycle، ليس is_active_fl بسيط |
| **lookupKey** | FILE_STATUS |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | File Service |
| **API الاستهلاك** | GET /api/lookups/FILE_STATUS?active=true |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| ACTIVE | نشط | Active |
| ARCHIVED | مؤرشف | Archived |
| DELETED | محذوف | Deleted |

⚠ مؤكَّدة في master-registry §6 (Owner: FileService).

---

## A6 — دورة الحالة (Status Lifecycle)

> **الحالة 1 — Status Lifecycle فقط** (لا Workflow Engine — RULE-13). FileDocument لديها
> 3 حالات (> 2) لذا مخطط الانتقال إلزامي (SCR-5).

### STATUS LIFECYCLE — FileDocument

```
[ACTIVE] ───────(أرشفة)────────► [ARCHIVED]
   │                                   │
   │                                   │
   └──────────(حذف نهائي)──────────────┴──────► [DELETED] ✗ (نهائي — لا رجوع)
```

> ملاحظة: DELETED هنا حالة سجلّية (يبقى الـ metadata + audit trail) بعد تطهير المحتوى
> الثنائي فعلياً — التمييز موثَّق في module-registry-file.md. حذف المحتوى نفسه نهائي وفوري
> (RULE-FILE-006) — لا خطوات موافقة، لا أدوار، لا محرك workflow.

---

## A7 — تبعيات الموديولات (Module Dependencies)

### الكيانات المُستهلَكة من موديولات أخرى

| الكيان | ENTITY-ID (canonical) | الموديول المالك | نوع الاعتمادية | XM Candidate |
|---|---|---|---|---|
| — | — | — | — | لا يوجد — File Service لا تستهلك أي كيان SHARED من موديول آخر (module-registry-file.md) |

> اعتماد Security (1.2) هو Trust-Boundary فقط (JWT filter chain للـ token-issuing endpoint) —
> **ليس** اعتماد بيانات (لا FK فعلي على أي جدول SEC_*). لا XM Candidate يُصاغ لهذا الاعتماد.

### الخدمات والتكاملات الخارجية

*(لا يوجد — لا تكامل خارجي لهذا الموديول)*

### ملاحظة إلزامية لأي موديول مستهلك مستقبلي (SHARED consumer of ENTITY-FILE-001)

> عند استهلاك FILE_DOCUMENT عبر HARD-FK (مثال: NotificationService عند تفعيل RXE-NOTIF)،
> يجب على الموديول المستهلك التحقق من `fileStatusId` عند العرض/الاسترجاع، وليس افتراض توفر
> `fileContent` دائماً. حالة `DELETED` تعني أن الصف موجود (المرجع FK صالح) لكن المحتوى
> الثنائي مُطهَّر — يجب عرض رسالة مناسبة ("الملف لم يعد متاحاً") بدل خطأ FK. هذا يحسم
> OQ-001 (انظر OQ Log وRULE-FILE-006) — لا حاجة لمنع الحذف عند وجود مستهلك مرتبط.

---

# ══════════════════════════════════════════════════════════
# PART B — SCREEN SPECIFICATIONS
# ══════════════════════════════════════════════════════════

---

## SCR-FILE-001 — لوحة إدارة المرفقات

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-FILE-001 |
| **اسم الشاشة** | لوحة إدارة المرفقات |
| **UI Pattern** | PATTERN-2 — Inline / Modal |
| **Pattern Reason** | مكوّن مُضمَّن (widget) يُستهلَك داخل شاشات موديولات أخرى (مثال: شاشة أمر الشراء تُضمِّن لوحة المرفقات) — لا مسار تنقّل مستقل خاص به. حقول قليلة (رفع + قائمة + تنزيل/حذف). متوافق مع F1 في ARCH-REF (FileUploadComponent/FileDownloadService كمكوّنات Angular قابلة لإعادة الاستخدام). |
| **SCR-ID Scope** | ONE SCR-ID covers: عرض القائمة + الرفع + التنزيل + الحذف (كلها ضمن نفس اللوحة — UNIFIED) |
| **P3 Implication** | Single embeddable component/modal — P3 يحدد اسم الـ Component الفعلي في F1 |
| **ENTITY-ID** | ENTITY-FILE-001 (+ ENTITY-FILE-002 للاختيار فقط، لا تحرير) |
| **وظيفة الشاشة** | تتيح لأي شاشة أخرى في المنصة عرض/رفع/تنزيل/حذف الملفات المرتبطة بسجل معيّن (ownerId/ownerType) |
| **المستخدمون** | أي مستخدم يملك صلاحية على الشاشة المضيفة + PERM_FILE_* المطلوبة |
| **الموضع في النظام** | File Service ← Shared Component ← يُستدعى من شاشات الموديولات الأخرى (لا مسار قائمة رئيسية مستقل) |
| **روابط من** | أي شاشة تحتاج مرفقات (Procurement PO، Sales Order، ...) |
| **روابط إلى** | لا يوجد |

#### UI Structure Decision (P2-RULE-4 — إلزامي لـ PATTERN-2)

| البند | القيمة |
|---|---|
| **Data Size** | Small (≤8 حقول إدخال فعلية للمستخدم: اختيار الملف + FileCategory) |
| **Interaction** | Inline Panel (مُضمَّن داخل شاشة أخرى) — ليس Modal منفصل بالضرورة، القرار النهائي لـ P3/F1 |
| **Pattern** | PATTERN-2 |
| **Reason** | لا حاجة لملاحة Search+Entry منفصلة لعملية رفع/عرض/حذف بسيطة مرتبطة بسجل مضيف واحد |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الرفع (جزء من اللوحة)

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| الملف | File Picker | نعم | — | multipart — الحجم والنوع يُتحقَّقان بعد الرفع (RULE-FILE-001, 005) |
| fileCategoryFk | قائمة منسدلة | نعم | ENTITY-FILE-002 → A3 | تُفلتَر حسب moduleCode للشاشة المضيفة |
| ownerId / ownerType / moduleCode | مخفي (سياقي) | نظام | تُمرَّر من الشاشة المضيفة | لا يُدخلها المستخدم يدوياً |

#### عرض القائمة (جزء من اللوحة)

| العمود | المصدر |
|---|---|
| fileNameOriginal | ENTITY-FILE-001 → A3 |
| fileCategoryFk (الاسم) | ENTITY-FILE-002 → A3 |
| fileTypeId | LOV-FILE-001 → A5 |
| fileSizeBytes | ENTITY-FILE-001 → A3 |
| createdAt | ENTITY-FILE-001 → A3 |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| رفع | POST (عبر Token) | RULE-FILE-001, RULE-FILE-002, RULE-FILE-003, RULE-FILE-005 — *(تفاصيل A4؛ RULE-FILE-004 يُطبَّق عند إصدار الرمز — API-FILE-001 — وليس عند الرفع نفسه، انظر B5)* |
| تنزيل | GET (عبر Token) | RULE-FILE-002, RULE-FILE-003 |
| حذف | DELETE (عبر Token) | RULE-FILE-002, RULE-FILE-003, RULE-FILE-006, RULE-FILE-007 |

---

### B4 — الصلاحيات (Permissions)

> **CORE-9:** شاشة مركبة موحّدة = SCR-ID واحد = صف واحد في SEC_PAGES.
> **ملاحظة خاصة (RULE-FILE-007):** حذف الملف يتطلب PERM_FILE_DELETE **بالإضافة** لتحقّق
> ملكية (ownerId/ownerType مطابق) أو دور Admin — تحقّق مركّب (صلاحية + قاعدة عمل)، وليس
> صلاحية وحدها.

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف (DELETE) | تصدير |
|---|---|---|---|---|---|
| SCR-FILE-001 | R1 | R1 | — (لا استخدام تشغيلي — لا مفهوم تعديل ملف) | R1 + شرط الملكية/Admin | — |

> R1 = أي مستخدم بصلاحية على الشاشة المضيفة. PERM_FILE_UPDATE يُولَّد تلقائياً (CORE-9)
> لكنه غير مُستخدَم وظيفياً في هذه المرحلة (لا Update API لملف موجود).

**Security Seed Data:**
```
SEC_PAGES  : INSERT — page_code = FILE_ATTACHMENT, parent_id_fk = NULL (Shared Component — لا قائمة تنقّل مستقلة)
PERMISSIONS: INSERT × 4 — PERM_FILE_ATTACHMENT_VIEW / CREATE / UPDATE / DELETE
```

---

### B5 — الواجهات البرمجية (Functional APIs)

> **⚠ انحراف موثَّق عن STACK-1:** الـ APIs التالية (002/003/004) لا تتبع نمط
> `/api/v1/[module]/[resource]` القياسي — تستخدم نمط `/upload/{token}` مباشرةً حسب تصميم
> Encrypted Token (POLICY-CLI-02 — الرمز مُضمَّن في مسار الـ URL نفسه، وليس كـ query param
> على مسار REST قياسي). القرار مصدره ARCH-REF-1.10 AD-FILE-02/03 — موثَّق صراحة وليس
> اختراعاً. الـ HTTP methods وResponse envelope القياسي (ApiResponse<T>) لا يزالان مُطبَّقين.

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-FILE-001 | إصدار رمز رفع | POST | /api/v1/files/upload-token | ownerId, ownerType, moduleCode, fileCategoryFk | Encrypted Token (نص) | RULE-FILE-004 |
| API-FILE-002 | رفع الملف الفعلي | POST | /upload/{encryptedToken} | multipart/form-data (الملف) | تأكيد رفع + fileDocumentPk | RULE-FILE-001, RULE-FILE-002, RULE-FILE-003, RULE-FILE-005 |
| API-FILE-003 | تنزيل الملف | GET | /download/{encryptedToken} | — | binary stream + Content-Type + Content-Disposition | RULE-FILE-002, RULE-FILE-003 |
| API-FILE-004 | حذف الملف | DELETE | /{encryptedToken} | — | تأكيد حذف | RULE-FILE-002, RULE-FILE-003, RULE-FILE-006, RULE-FILE-007 |
| API-FILE-005 | قائمة ملفات سجل معيّن | GET | /api/v1/files/{ownerId} | ownerId, ownerType?, page, size | قائمة FileDocument | — |

---

---

# ══════════════════════════════════════════════════════════
# STANDALONE — بعد PART B
# ══════════════════════════════════════════════════════════

---

## Permissions Summary & Registry Update

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف (DELETE) | تصدير |
|---|---|---|---|---|---|
| SCR-FILE-001 (لوحة المرفقات) | R1 | R1 | — | R1 + ملكية/Admin | — |

> R1 = أي مستخدم بصلاحية على الشاشة المضيفة (لا دور خاص بـ File Service على مستوى المنصة)

---

### Registry Update — MODE 1

```
## REGISTRY UPDATE — 2026-07-11
────────────────────────────────────────────────────────────────
Source Mode    : MODE 1
Feature Code   : FILE-001
DBS-ID         : —
Plan ID        : —
────────────────────────────────────────────────────────────────
New Entities   : ENTITY-FILE-001 (SHARED-owner), ENTITY-FILE-002 (SHARED-owner)
New Tables     : —  (P2 scope)
New Lookups    : FILE_TYPE, FILE_STATUS  (كلاهما مؤكَّد سابقاً في master-registry §6)
New APIs       : API-FILE-001 through API-FILE-005
XM-IDs Open    : — (لا استهلاك صادر من هذا الموديول)
OQ-IDs Open    : — (OQ-001 RESOLVED هذه الجلسة)
Gate Status    : PASSED ✓
Next Action    : Trigger MODE 1.5 — Database Governance Engine (FileService)
────────────────────────────────────────────────────────────────
```

---

## OQ Log — سجل الأسئلة المفتوحة

```
## OPEN QUESTIONS LOG — File Service — 2026-07-11
─────────────────────────────────────────────────────────────────────────────
OQ-ID  │ Question                                          │ Status   │ Raised │ Resolved │ Escalation
───────┼────────────────────────────────────────────────────┼──────────┼────────┼──────────┼───────────
OQ-001 │ ما تأثير حذف FileDocument (نهائي — RULE-FILE-006) │ RESOLVED │ MODE 1 │ MODE 1   │ LOCAL
       │ على الموديولات المستهلكة عبر HARD-FK (Notification │          │        │          │
       │ DEFERRED، Audit، 3.x) عند تفعيل XM لاحقاً؟ هل يُمنع │          │        │          │
       │ الحذف إن وُجد مستهلك مرتبط فعلياً، أم يُترك التعامل │          │        │          │
       │ مع المرجع اليتيم (orphaned reference) للموديول      │          │        │          │
       │ المستهلك نفسه؟                                      │          │        │          │
─────────────────────────────────────────────────────────────────────────────
```

> **مصدر الرفع:** ARCH-8 (auto-raise) — Section 5.5.3 PROJECT-1: كل كيان SHARED (owner) يملك
> API حذف يُرفَع له OQ تلقائياً.
>
> **القرار (RESOLVED — MODE 1، نفس الجلسة):** "الحذف النهائي" (RULE-FILE-006) يُطهِّر
> `fileContent` فقط — صف FILE_DOCUMENT لا يُحذَف فعلياً، بل ينتقل لحالة `DELETED` (A6)
> ويبقى قائماً بكامل بياناته الوصفية. بذلك يبقى أي HARD-FK من مستهلك مستقبلي **دائماً
> صالحاً** — لا orphaned reference، ولا حاجة لمنع الحذف أو RESTRICT/CASCADE على مستوى DB.
> المسؤولية المقابلة تقع على الموديول المستهلك: التحقق من `fileStatusId` عند العرض وعدم
> افتراض توفر المحتوى دائماً (تفصيل كامل في A7 وRULE-FILE-006). لا حاجة لانتظار أول موديول
> مستهلك فعلي — القرار مُشتَق مباشرةً من تصميم Status Lifecycle الموثَّق في A6/A3، وليس
> بحاجة لمدخل خارجي إضافي (Zero-Question Protocol Step 3 — مُستنتَج من التصميم الموثَّق
> ذاته داخل هذه الوثيقة).

---

## MODULE GOVERNANCE INDEX — File Service

```
══════════════════════════════════════════════════════════════════
Feature Code     : FILE-001
Last Updated     : 2026-07-11 by MODE 1
LAST-VERIFIED    : 2026-07-11
VERIFIED-BY      : MODE 1 gate
GOVERNANCE-STATE : FULL
══════════════════════════════════════════════════════════════════

PIPELINE STATUS
───────────────────────────────────────────────────────────────
Stage            │ Mode    │ Status      │ Gate Result
─────────────────┼─────────┼─────────────┼──────────────
Functional Truth │ MODE 1  │ ✓ COMPLETE  │ PASSED ✓
Structural Truth │ MODE 1.5│ —           │ —
Execution Truth  │ MODE 2  │ —           │ —
───────────────────────────────────────────────────────────────

ATTACHED ARTIFACTS
───────────────────────────────────────────────────────────────
srs.md            : ✓ Feature Code FILE-001
db-script.md      : —
execution-plan.md : —
OQ Log            : 0 open (OQ-001 RESOLVED — MODE 1)
───────────────────────────────────────────────────────────────

EXECUTION STATE
───────────────────────────────────────────────────────────────
Current Phase     : MODE 1 COMPLETE
Next Safe Action  : Upload srs-file-001.md to PROJECT-2 → trigger MODE 1.5
                     (DB_TARGET = POSTGRESQL_16 — يُذكَر عند بدء الجلسة)
Execution Readiness: READY — كل بوابات MODE 1 اجتازت
───────────────────────────────────────────────────────────────
```

---
*نهاية الوثيقة | End of srs.md*
*Governed by: SRS Governance Engine (Project 1)*
*Feature Code: FILE-001 | Version: 1.0*
*Structure: PART A (Module Foundation) + PART B (Screen Specifications)*
*Next Mode: MODE 1.5 — Database Governance Engine (Project 2) — DB_TARGET = POSTGRESQL_16*
