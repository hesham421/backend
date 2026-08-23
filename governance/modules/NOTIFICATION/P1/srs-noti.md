<!-- ═══════════════════════════════════════════════════════════ -->
<!-- SRS — وثيقة التحليل والمتطلبات                             -->
<!-- Governed by: SRS Governance Engine (Project 1)             -->
<!-- Compatible: PROJECT-2 | PROJECT-3 | PROJECT-4              -->
<!-- Structure : PART A (Module Foundation) + PART B (Screens)  -->
<!-- ═══════════════════════════════════════════════════════════ -->

# وثيقة التحليل (SRS)
## خدمة الإشعارات | Notification Service

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
| **الموديول** | Notification Service — خدمة الإشعارات |
| **Feature Code** | NOTIF-001 |
| **Feature Type** | **Engine / Foundation Module** — تصنيف غير قياسي (نفس نمط File Service). مزيج من: كيان Log مُدار بالمحرك (NotificationLog — append-only، لا Business Code)، وكيانات Configuration/Master بسيطة (Template، ChannelConfig). موثَّق كانحراف عن 5.1.2/5.4.2 في A3 لكل كيان. |
| **الإدارة / القسم** | البنية التحتية المشتركة (Layer-1 Foundation) |
| **إعداد بواسطة** | SRS Governance Engine (Project 1) |
| **النسخة** | 1.0 |
| **التاريخ** | 2026-07-11 |
| **الحالة** | Draft |
| **Open Questions** | None — see OQ Log |
| **Governed by** | SRS Governance Engine (Project 1) |
| **DB_TARGET** | POSTGRESQL_16 (business-policies-notif.md) |
| **P0 Readiness** | PARTIALLY_READY ⚠️ — master-registry §15 (2026-07-11) — AQ-010/AQ-011 مفتوحة (اختيار SMS/WhatsApp provider) — **غير حاجبة لـ MODE 1** |

---

## A2 — السياق الوظيفي (Functional Context)

### ما يشمله هذا الموديول

> يوفّر Notification Service قناة إرسال موحّدة لكل الموديولات في المنصة عبر 5 قنوات
> (Email/SMS/WhatsApp/Push/Internal)، مع سجل كامل لكل إشعار مُرسَل، وإعادة محاولة تلقائية
> عند الفشل، وقوالب ثنائية اللغة (عربي/إنجليزي). القرار بشأن أي قناة يحتاجها حدث معيّن
> يعود بالكامل للموديول الناشر (Publishing Module) — لا منطق عمل داخل Notification نفسها.

### ما لا يشمله هذا الموديول

> - لا يقرر أي قناة يحتاج أي حدث — هذا قرار الموديول الناشر حصراً (AD-NOTIF-10).
> - لا يتحقق من JWT — مسؤولية Security Module (1.2) حصراً.
> - لا يخزّن ملفات القوالب فعلياً في Phase 1 — تخزين inline مؤقت (انظر أدناه).
> - لا Workflow/Approval Engine لإعادة المحاولة أو الحالة — سلوك Status Lifecycle قياسي فقط.

### وظيفة الموديول

> يتيح Notification Service لأي موديول نشر حدث إشعار (Event) يحتوي المستلم والقناة/القنوات
> المطلوبة والقالب والبيانات السياقية، فيتولى Notification Service الإرسال الفعلي عبر
> القناة/القنوات المطلوبة، مع تسجيل مستقل لكل قناة، وإعادة محاولة عند الفشل.

### الوصف الوظيفي التفصيلي

> عند نشر حدث NotificationEvent (عبر RabbitMQ للأحداث غير المتزامنة، أو Spring Events
> للأحداث ضمن نفس المعاملة)، يحتوي الحدث حقلاً channelHint يحدده الموديول الناشر — قناة
> واحدة، أو قائمة قنوات، أو "ALL". لكل قناة مطلوبة، يتحقق Notification Service من حالة
> تفعيلها (NotificationChannelConfig.isEnabledFl) بشكل مستقل، وينشئ صفاً مستقلاً في
> NotificationLog لكل قناة (fan-out) — قناة معطَّلة لا توقف باقي القنوات المطلوبة، بل
> تُسجَّل كـ CHANNEL_DISABLED فقط. عند فشل الإرسال الفعلي (SMTP/SMS/WhatsApp/Push)، يُعاد
> المحاولة حتى 5 مرات بتأخير تصاعدي (2s→3s→4.5s→6.75s)، وبعدها يُسجَّل الإشعار كـ FAILED
> دون إبلاغ الجهة الناشرة (Admin فقط يتابع من السجل). القوالب ثنائية اللغة، ولغة القالب
> المُرسَل تُحدَّد من تفضيل لغة المستلم في Security.

#### الوضع الحالي

> لا ينطبق — موديول تحتي جديد (Foundation) لا يستبدل عملية يدوية أو نظاماً قائماً.

#### الصعوبات الحالية

| # | الصعوبة |
|---|---|
| 1 | غياب قناة إرسال موحّدة يجبر كل موديول على بناء منطق إرسال (بريد/SMS/...) خاص به لو لم يوجد هذا الموديول. |
| 2 | صعوبة تتبّع نجاح/فشل الإشعارات عبر قنوات متعددة دون سجل مركزي. |

#### النظام المقترح وفوائده

| # | الفائدة |
|---|---|
| 1 | نقطة إرسال موحّدة لكل الموديولات — لا تكرار منطق قنوات الإرسال. |
| 2 | فصل واضح بين "من يقرر القناة" (الموديول الناشر) و"من يرسل فعلياً" (Notification) — لا تسرّب منطق عمل. |
| 3 | سجل تتبّع كامل لكل إشعار — نجاح/فشل/تعطيل — مفيد لـ Audit Service لاحقاً (SOFT-READ). |
| 4 | مرونة تفعيل/تعطيل قناة كاملة من الواجهة دون تعديل كود (`isEnabledFl`). |

### ملاحظات عامة

- Notification Service **لا تستنتج القناة** من نوع الحدث أو module_code — هذا حاجز معماري
  صريح (AD-NOTIF-10) لمنع تسرّب منطق عمل الموديولات الناشرة إلى موديول محايد.
- التكامل مع الموديولات الناشرة: RabbitMQ (`erp.notification.exchange` / `erp.notification.queue`
  / routing key `notification.send`) للأحداث غير المتزامنة، أو Spring Events للأحداث
  المتزامنة ضمن نفس المعاملة — **ليس API-ID** (انظر ملاحظة B5 أدناه).
- قوالب الإشعارات مُخزَّنة inline في Phase 1 (`templateBodyAr`/`templateBodyEn`) — الاعتماد
  على File Service (1.10) **DEFERRED** وليس ملغياً؛ الآن بعد اعتماد srs-file-001.md، أصبح
  `ENTITY-FILE-001` (FileDocument) مرجعاً قانونياً فعلياً (انظر A7). الترقية تتبع آلية
  RXE-NOTIF القياسية (CONTRACT-8) عند تفعيل DBS-ID لـ File Service — **ليست بروتوكولاً
  مخصصاً**.
- AQ-010 (مزوّد SMS) وAQ-011 (مزوّد WhatsApp) مفتوحتان في master-registry §14 — قرارات
  تقنية تخص P3 لاحقاً (تفاصيل provider تعيش في `NotificationChannelConfig.configJson`،
  لا في بنية الجدول) — **لا تؤثر على بنية SRS هذه ولم تُرفَع كـ OQ** (ليست سؤالاً وظيفياً).
- لا Workflow Engine لإعادة المحاولة (RULE-13) — Retry سلوك Status Lifecycle قياسي، ليس
  خطوات موافقة.

### استثناء صريح — POLICY-CLI-06 ("No JWT validation inside the module")

> هذه السياسة **مُعفاة عمداً** من أي RULE-ID في A4، بنفس المنطق المُطبَّق في srs-file-001.md
> A2 لـ POLICY-CLI-06 هناك. السبب: قرار حدود معمارية (trust-boundary) — Notification
> Service لا تتحقق من JWT بنفسها، بل تثق بـ Security's filter chain (ADAPT-NOTIF-03) —
> ولا ينتج عنها سلوك عمل مُلاحَظ أو رسالة قابلة للاختبار كـ RULE-ID داخل هذا الموديول (رفض
> الطلب غير المُصادَق عليه يحدث في Security's filter chain، خارج نطاق Notification تماماً).

---

## A3 — الكيانات والحقول (Entities & Fields)

---

### ENTITY-NOTIF-001 — NotificationLog

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner) — مستهلك مستقبلي: AuditService (SOFT-READ, NOT-YET-ASSIGNED — module-registry-notif.md) |
| **Business Code** | **لا يوجد — انحراف موثَّق**. كيان Engine-managed append-only (module-registry-notif.md AUTO-DECISIONS: "NOTIF_LOG is append-only") — سجل نظام لا سجل أعمال يحتاج رمزاً بشرياً. نفس نمط الانحراف الموثَّق في ENTITY-FILE-001 (srs-file-001.md). |
| **المصدر** | ARCH-REF-1.8-NOTIFICATION-SERVICE.md §P2 (AD-NOTIF-04، RESOLUTION-01) |
| **العمليات** | Create (عبر النظام فقط عند الإرسال)، Read، **Update محدود جداً** (فقط `isReadFl`/`readAt` — API-NOTIF-005) — لا Delete — append-only بخلاف علم القراءة، الانتقال بين حالات `notificationStatusId` فقط (status/retry_count) |
| **Cross-Module** | None consumed — هذا الكيان مُصدَّر (owner) |

#### حقول الكيان

| اسم الحقل | نوع البيانات (*) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| notificationLogPk | BIGINT (PK) | نظام | — | رقم إنشائي تلقائي | المعرف | ID |
| recipientId | BIGINT | نعم | Security USERS_PK | **لا FK بالاسم القياسي** — يشير فعلياً لعمود `USERS_PK` (PERMANENT EXCEPTION — master-registry §4)، ليس `usersFk` | معرف المستلم | Recipient ID |
| notificationTypeId | VARCHAR(20) | نعم | LOV-NOTIF-001 | القناة المُستخدَمة لهذا الصف (صف مستقل لكل قناة — RULE-NOTIF-003) | نوع الإشعار | Notification Type |
| templateCode | VARCHAR(50) | نعم | ENTITY-NOTIF-002 → A3 | **مرجع منطقي بالكود الطبيعي (natural key)** — ليس FK رقمي (لا يُسمَّى templateFk) | رمز القالب | Template Code |
| subject | VARCHAR(500) | لا | — | موضوع الإشعار (قناة Email بشكل أساسي) | الموضوع | Subject |
| bodyPreview | VARCHAR(1000) | لا | — | معاينة مختصرة للمحتوى المُرسَل | معاينة المحتوى | Body Preview |
| notificationStatusId | VARCHAR(20) | نعم | LOV-NOTIF-002 | Status Lifecycle — 4 حالات — انظر A6 | حالة الإشعار | Notification Status |
| retryCount | NUMERIC | نظام | — | افتراضي 0 — RULE-NOTIF-004 | عدد المحاولات | Retry Count |
| sentAt | TIMESTAMP | لا | — | وقت الإرسال الفعلي (يبقى فارغاً إن لم يُرسَل بعد) | وقت الإرسال | Sent At |
| moduleCode | VARCHAR(20) | نعم | نص حر | الموديول الناشر للحدث | كود الموديول | Module Code |
| referenceId | BIGINT | لا | — | **لا FK فعلي — مرجع متعدد الأشكال (polymorphic)**، نفس نمط `ownerId` في File Service | معرف الكيان المرتبط | Reference ID |
| referenceType | VARCHAR(50) | لا | نص حر | اسم الكيان في الموديول الناشر — **ليس LOV محكوم** | نوع الكيان المرتبط | Reference Type |
| isReadFl | SMALLINT | نعم | 1 / 0 | افتراضي 0 — يدعم API-NOTIF-004 (غير مقروء) وAPI-NOTIF-005 (تحديد كمقروء)؛ ⚠ حقل قابل للتعديل الوحيد رغم append-only العام للكيان (تحديث حالة القراءة فقط، لا تعديل لبيانات الإشعار نفسه) | مقروء | Read |
| readAt | TIMESTAMP | لا | — | وقت التحديد كمقروء — يبقى فارغاً حتى `isReadFl = 1` | وقت القراءة | Read At |
| createdBy | VARCHAR(255) | نظام | — | AuditEntityListener | أنشئ بواسطة | Created By |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ الإنشاء | Created At |
| updatedBy | VARCHAR(255) | نظام | — | AuditEntityListener | عُدِّل بواسطة | Updated By |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ التعديل | Updated At |

(*) DB_TARGET = POSTGRESQL_16 (CORE-8). لا notes field — غير مطلوب في ARCH-REF (HR-1).

---

### ENTITY-NOTIF-002 — NotificationTemplate

| البند | القيمة |
|---|---|
| **النوع** | PRIVATE (Phase 1) |
| **Business Code** | **لا — انحراف موثَّق**. `templateCode` يُحدَّد يدوياً عند الإنشاء (نمط شبيه بـ lookupKey — ثابت بعد الإنشاء، RULE-NOTIF-007)، وليس رمزاً تسلسلياً تلقائياً حسب BC-RULE-2. نفس نمط `FileCategory.categoryCode`. |
| **المصدر** | ARCH-REF-1.8-NOTIFICATION-SERVICE.md §P2 (RESOLUTION-02، AD-NOTIF-05 المُعدَّل) |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | **Consumes SHARED ENTITY-FILE-001 (FileDocument) — owner: File Service** — انظر تفصيل كامل في A7 |

#### حقول الكيان

| اسم الحقل | نوع البيانات (*) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| notificationTemplatePk | BIGINT (PK) | نظام | — | رقم إنشائي تلقائي | المعرف | ID |
| templateCode | VARCHAR(50) | نعم | — | فريد، ثابت بعد الإنشاء — RULE-NOTIF-007 | رمز القالب | Template Code |
| templateNameAr | VARCHAR(200) | نعم | — | — | الاسم بالعربي | Name (Arabic) |
| templateNameEn | VARCHAR(200) | نعم | — | — | الاسم بالإنجليزي | Name (English) |
| channelTypeId | VARCHAR(20) | نعم | LOV-NOTIF-001 | القناة المستهدفة لهذا القالب | القناة | Channel |
| moduleCode | VARCHAR(20) | نعم | نص حر | الموديول المالك لهذا القالب | كود الموديول | Module Code |
| templateBodyAr | TEXT | نعم | — | **Phase 1 — تخزين inline** (RESOLUTION-02). يحتوي Placeholders مثل `{{recipientName}}` | نص القالب (عربي) | Template Body (Arabic) |
| templateBodyEn | TEXT | نعم | — | **Phase 1 — تخزين inline** — نفس المحتوى بالإنجليزي | نص القالب (إنجليزي) | Template Body (English) |
| fileFk | BIGINT (FK) | لا | ENTITY-FILE-001 | **NULLABLE — DEFERRED — غير مُستخدَم في Phase 1.** يُفعَّل عند RXE-NOTIF (CONTRACT-8)، دون تغيير `templateCode` (العقد الخارجي ثابت) | ملف القالب | Template File |
| isActiveFl | SMALLINT | نعم | 1 / 0 | 1 = نشط | نشط | Active |
| createdBy | VARCHAR(255) | نظام | — | AuditEntityListener | أنشئ بواسطة | Created By |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ الإنشاء | Created At |
| updatedBy | VARCHAR(255) | نظام | — | AuditEntityListener | عُدِّل بواسطة | Updated By |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ التعديل | Updated At |

(*) DB_TARGET = POSTGRESQL_16. `templateBodyAr/En` → TEXT (يقابل CLOB في Oracle، CORE-8).

---

### ENTITY-NOTIF-003 — NotificationChannelConfig

| البند | القيمة |
|---|---|
| **النوع** | PRIVATE (Configuration) |
| **Business Code** | لا ينطبق — كيان Configuration بحت، لا سجل أعمال، لا lifecycle (تكييف قالب REFERENCE/CONFIG من 5.4.2) |
| **المصدر** | ARCH-REF-1.8-NOTIFICATION-SERVICE.md §P2 (RESOLUTION-01) |
| **العمليات** | Read, Update (فقط — 5 صفوف مزروعة seed، لا Create/Delete من المستخدم) |
| **Cross-Module** | None |

#### حقول الكيان

| اسم الحقل | نوع البيانات (*) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| notificationChannelConfigPk | BIGINT (PK) | نظام | — | رقم إنشائي تلقائي | المعرف | ID |
| channelTypeId | VARCHAR(20) | نعم | LOV-NOTIF-001 | فريد — صف واحد لكل قناة (Seed: 5 صفوف) | القناة | Channel |
| isEnabledFl | SMALLINT | نعم | 1 / 0 | افتراضي 1 لكل القنوات الخمس (2026-07-11 — لا تأجيل) | مفعّلة | Enabled |
| configJson | TEXT | لا | — | إعدادات إضافية خاصة بمزوّد القناة (adapter pattern — provider credentials لاحقاً، مثال: AQ-010/AQ-011) | إعدادات إضافية | Additional Config |
| createdBy | VARCHAR(255) | نظام | — | AuditEntityListener | أنشئ بواسطة | Created By |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ الإنشاء | Created At |
| updatedBy | VARCHAR(255) | نظام | — | AuditEntityListener | عُدِّل بواسطة | Updated By |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ التعديل | Updated At |

(*) DB_TARGET = POSTGRESQL_16. `configJson` → TEXT (لا JSON native type في جدول CORE-8 — يُخزَّن كنص، يُفسَّر بالتطبيق، نفس نمط `config_json` في ARCH-REF §P2).

---

## A4 — قواعد التحقق (Business Rules)

> **ملاحظة نطاق:** لا RULE-ID لتفاصيل RabbitMQ/Camel التنفيذية (تلك تخص P3). القواعد أدناه
> تغطي السلوك المُلاحَظ من الموديول الناشر أو المستخدم النهائي فقط.

---

### RULE-NOTIF-001 — اكتمال عقد الحدث (Event Contract)

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-NOTIF-001 |
| **Trigger** | عند نشر NotificationEvent |
| **Statement** | The system MUST reject any NotificationEvent missing `recipientId`, `channelHint`, `templateCode`, `contextData`, or `priority`. |
| **Message-AR** | بيانات الحدث غير مكتملة |
| **Message-EN** | Notification event data is incomplete |
| **Source** | POLICY-CLI-01 |
| **Test-Hint** | نشر event بـ templateCode غير موجود → رسالة خطأ عربية؛ نشر event بدون channelHint → رفض |

### RULE-NOTIF-002 — ملكية قرار القناة (لا استنتاج داخلي)

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-NOTIF-001 |
| **Trigger** | عند استقبال أي NotificationEvent |
| **Statement** | The system MUST NOT infer or hardcode which channel(s) an event requires based on event type or `moduleCode` — `channelHint` MUST be supplied explicitly by the publishing module as a single channel, a list, or `"ALL"`. |
| **Message-AR** | — (قاعدة تصميم داخلية، لا رسالة رفض للمستخدم — القناة دائماً صريحة من الناشر) |
| **Message-EN** | — |
| **Source** | POLICY-CLI-02 / AD-NOTIF-10 |
| **Test-Hint** | لا جدول/إعداد داخل Notification يربط `module_code` بقناة معينة ضمنياً — تحقّق غيابه |

### RULE-NOTIF-003 — استقلالية كل قناة (Fan-Out)

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-NOTIF-001 |
| **Trigger** | عند معالجة `channelHint` متعدد القيم |
| **Statement** | For each channel requested in `channelHint`, the system MUST create an independent NotificationLog entry and evaluate that channel's enabled status independently — a disabled channel MUST NOT block delivery on the other requested channels. |
| **Message-AR** | — (سلوك نظامي، ينعكس في السجل لا برسالة مباشرة) |
| **Message-EN** | — |
| **Source** | POLICY-CLI-02 / AD-NOTIF-10 |
| **Test-Hint** | `channelHint=['SMS','WHATSAPP']` وSMS معطَّلة → WhatsApp يُرسَل بنجاح، SMS يُسجَّل CHANNEL_DISABLED، لا فشل كامل للحدث |

### RULE-NOTIF-004 — سياسة إعادة المحاولة عند الفشل

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-NOTIF-001 |
| **Trigger** | عند فشل إرسال عبر القناة |
| **Statement** | The system MUST retry a failed delivery up to 5 times with exponential backoff (2s → 3s → 4.5s → 6.75s), then mark the notification FAILED in NotificationLog without notifying the original sender. |
| **Message-AR** | — (لا يُبلَّغ المُرسِل تلقائياً — Admin يتابع من السجل) |
| **Message-EN** | — |
| **Source** | POLICY-CLI-03 / AD-NOTIF-01 |
| **Test-Hint** | محاكاة فشل SMTP → التحقق من `retryCount` في NotificationLog يصل 5 قبل FAILED |

### RULE-NOTIF-005 — معالجة القناة المعطَّلة (بدون خطأ للمُرسِل)

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-NOTIF-001 |
| **Trigger** | عند محاولة إرسال على قناة `isEnabledFl = 0` |
| **Statement** | The system MUST NOT raise an error to the sending module when a target channel is disabled — it MUST log the event as `CHANNEL_DISABLED` in NotificationLog instead. |
| **Message-AR** | — (لا خطأ يُرجَع للناشر — تسجيل داخلي فقط) |
| **Message-EN** | — |
| **Source** | POLICY-CLI-04 |
| **Test-Hint** | تعطيل قناة SMS ثم نشر حدث بها → NotificationLog يُسجَّل CHANNEL_DISABLED، لا استثناء يُرمى للناشر |

### RULE-NOTIF-006 — إلزامية ثنائية اللغة للقالب + Fallback

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-NOTIF-002 |
| **Trigger** | عند إنشاء/تعديل قالب، وعند طلب قالب للإرسال |
| **Statement** | Every NotificationTemplate MUST have both an Arabic and an English body (`templateBodyAr`, `templateBodyEn`); template language MUST be resolved from the recipient's user language preference (Security); a missing `templateCode` MUST fall back to a default template rather than fail the send. |
| **Message-AR** | يجب توفير نص القالب بالعربي والإنجليزي معاً |
| **Message-EN** | The template body must be provided in both Arabic and English |
| **Source** | POLICY-CLI-05 |
| **Test-Hint** | طلب قالب برمز غير موجود → استخدام قالب افتراضي بدل فشل الإرسال |

### RULE-NOTIF-007 — فرادة وثبات رمز القالب

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-NOTIF-002 |
| **Trigger** | عند الإنشاء / عند التعديل |
| **Statement** | The system MUST prevent creating a NotificationTemplate whose `templateCode` duplicates an existing template, and MUST reject any attempt to modify `templateCode` after creation. |
| **Message-AR** | رمز القالب مستخدَم مسبقاً أو غير قابل للتعديل |
| **Message-EN** | Template code already exists or cannot be modified |
| **Source** | ERP-DEFAULT — platform-standards.md Section M (نمط فرادة/ثبات الرموز المُعرَّفة يدوياً، بنفس منطق BC-RULE-4 وثبات lookupKey — راجع PROJECT-1 §5.5.6 LOV-2) — Override: بنية العمود UNIQUE مؤكَّدة أيضاً في ARCH-REF-1.8 §P2 |
| **Test-Hint** | محاولة تعديل `templateCode` على قالب موجود → رفض |

### RULE-NOTIF-008 — استراتيجية تخزين القالب واحتياط الاسترجاع (Fallback)

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-NOTIF-002 |
| **Trigger** | عند إنشاء قالب (Phase 1) / عند طلب محتوى القالب للإرسال (بعد ترقية File Service مستقبلاً) |
| **Statement** | In Phase 1, the system MUST store the template body inline (`templateBodyAr`/`templateBodyEn`). After a future migration to File Service (via RXE-NOTIF — see A7), the system MUST attempt retrieval via `fileFk` first and MUST fall back to the inline body columns if File Service is transiently unavailable — `templateBodyAr`/`templateBodyEn` MUST NOT be deleted upon migration. |
| **Message-AR** | — (سلوك احتياطي داخلي — لا رسالة تُعرض على المستخدم؛ الإرسال ينجح بصمت عبر أي من المصدرين) |
| **Message-EN** | — |
| **Source** | POLICY-CLI-07 |
| **Test-Hint** | بعد الترقية (fileFk مُعبَّأ)، محاكاة انقطاع File Service مؤقت عند الإرسال → يجب أن يُستخدَم `templateBodyAr`/`En` بدل فشل الإرسال بالكامل؛ التحقق من بقاء `templateBodyAr`/`En` غير محذوفة بعد نجاح الترقية |

---

## A5 — قوائم القيم (LOV / Lookup)

---

### LOV-NOTIF-001 — قناة الإشعار (NotificationChannel)

| البند | القيمة |
|---|---|
| **الحقل** | notificationTypeId (NotificationLog) / channelTypeId (Template, ChannelConfig) |
| **ENTITY-ID** | ENTITY-NOTIF-001, ENTITY-NOTIF-002, ENTITY-NOTIF-003 |
| **نوع التحكم** | Dropdown (≤15) |
| **lookupKey** | NOTIFICATION_CHANNEL |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Notification Service |
| **API الاستهلاك** | GET /api/lookups/NOTIFICATION_CHANNEL?active=true |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| EMAIL | بريد إلكتروني | Email |
| SMS | رسالة نصية | SMS |
| WHATSAPP | واتساب | WhatsApp |
| PUSH | إشعار فوري (تطبيق) | Push |
| INTERNAL | إشعار داخلي | Internal |

⚠ مؤكَّدة في master-registry §6 (Owner: NotificationService) — 5 قيم، قرار نهائي 2026-07-11 (لا تأجيل لأي قناة).

---

### LOV-NOTIF-002 — حالة الإشعار (NotificationStatus)

| البند | القيمة |
|---|---|
| **الحقل** | notificationStatusId |
| **ENTITY-ID** | ENTITY-NOTIF-001 |
| **نوع التحكم** | Dropdown (≤15) — Status Lifecycle |
| **lookupKey** | NOTIFICATION_STATUS |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Notification Service |
| **API الاستهلاك** | GET /api/lookups/NOTIFICATION_STATUS?active=true |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| PENDING | قيد الانتظار | Pending |
| SENT | تم الإرسال | Sent |
| FAILED | فشل | Failed |
| CHANNEL_DISABLED | القناة معطَّلة | Channel Disabled |

⚠ مؤكَّدة في master-registry §6 (Owner: NotificationService).

---

## A6 — دورة الحالة (Status Lifecycle)

> **الحالة 1 — Status Lifecycle فقط** (لا Workflow Engine — RULE-13). NotificationLog لديها
> 4 حالات (> 2) لذا مخطط الانتقال إلزامي (SCR-5). append-only — لا انتقال خارج الحالات
> النهائية الثلاث (module-registry-notif.md AUTO-DECISIONS).

### STATUS LIFECYCLE — NotificationLog

```
                    ┌───(نجاح الإرسال)───► [SENT] ✓  (نهائية)
                    │
   [PENDING] ───────┼───(فشل بعد 5 محاولات — RULE-NOTIF-004)───► [FAILED] ✗  (نهائية)
                    │
                    └───(القناة معطَّلة وقت الإرسال — RULE-NOTIF-005)───► [CHANNEL_DISABLED] ⏸  (نهائية)
```

> لا رجوع من أي من الحالات الثلاث النهائية إلى PENDING — كل صف NotificationLog يُسجَّل مرة
> واحدة لكل قناة (RULE-NOTIF-003) وينتقل لحالة نهائية واحدة فقط. لا خطوات موافقة، لا أدوار،
> لا محرك workflow.

---

## A7 — تبعيات الموديولات (Module Dependencies)

### الكيانات المُستهلَكة من موديولات أخرى

| الكيان | ENTITY-ID (canonical) | الموديول المالك | نوع الاعتمادية | XM Candidate |
|---|---|---|---|---|
| FileDocument | **ENTITY-FILE-001** (srs-file-001.md — قانوني فعلياً) | File Service | HARD-FK — **DEFERRED** (Phase 1 workaround: تخزين inline في `templateBodyAr`/`templateBodyEn`، RESOLUTION-02) | نعم → `XM-NOTIF-[N]` في MODE 1.5، يُفعَّل عند `RXE-NOTIF-[SEQ]` (CONTRACT-8) عند تفعيل DBS-ID لـ File Service |
| USERS | — (PERMANENT EXCEPTION — لا ENTITY-ID محكوم، master-registry §4) | Security (EXCEPTION-status) | HARD-FK — `recipientId` → `USERS_PK` (الاسم الفعلي) | لا — Security EXCEPTION، يُستهلك AS-IS، لا XM رسمي يُصاغ لموديول EXCEPTION |

> **ملاحظة CONTRACT-7:** الصف الأول أعلاه يتبع الصيغة القياسية الكاملة الآن (`Consumes SHARED
> ENTITY-FILE-001 (FileDocument) — owner: File Service — Dependency type: HARD-FK (DEFERRED)
> — XM candidate: Yes`) — كان هذا غير ممكن قبل اعتماد srs-file-001.md (انظر مناقشة الترتيب
> السابقة). الصف الثاني (Security) خارج نطاق CONTRACT-7 لأن Security PERMANENT EXCEPTION،
> لا نظام ENTITY-ID محكوم لها.

### الخدمات والتكاملات الخارجية

| الخدمة | الغرض | نوع التكامل |
|---|---|---|
| Firebase Cloud Messaging (FCM) | إرسال Push Notifications لتطبيق Flutter | REST API (Firebase Admin SDK) |
| SMTP (مزوّد بريد) | إرسال قناة Email عبر Apache Camel | Direct (SMTP) |
| SMS Provider (TBD — AQ-010) | إرسال قناة SMS | REST API — provider محدَّد لاحقاً عبر `configJson` |
| WhatsApp Business API (TBD — AQ-011) | إرسال قناة WhatsApp | REST API — provider محدَّد لاحقاً عبر `configJson` |

---

# ══════════════════════════════════════════════════════════
# PART B — SCREEN SPECIFICATIONS
# ══════════════════════════════════════════════════════════

---

## SCR-NOTIF-001 — لوحة إشعاراتي (Notification Bell + History)

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-NOTIF-001 |
| **اسم الشاشة** | لوحة إشعاراتي |
| **UI Pattern** | PATTERN-3 — Specialized |
| **Pattern Reason** | تفاعل بصري غير قياسي: جرس إشعارات (Dropdown مصغَّر) + صفحة سجل كاملة — ليس Search+Entry تقليدياً ولا Inline/Modal بسيط (ARCH-REF F1: NotificationBellComponent + NotificationHistoryPage) |
| **SCR-ID Scope** | ONE SCR-ID يغطي: الجرس المصغَّر + صفحة السجل الكاملة (وحدة منطقية واحدة) |
| **P3 Implication** | مكوّنان مترابطان (Bell dropdown + History page) — P3 يحدد أسماء الـ Components في F1 |
| **ENTITY-ID** | ENTITY-NOTIF-001 |
| **وظيفة الشاشة** | عرض عدد الإشعارات غير المقروءة، فتح قائمة منسدلة، تصفح السجل الكامل مع فلاتر، تحديد كمقروء |
| **المستخدمون** | كل مستخدم مسجَّل (يرى إشعاراته الخاصة فقط) |
| **الموضع في النظام** | مكوّن عام في الـ Header — متاح من كل شاشة |
| **روابط من** | Header العام للنظام |
| **روابط إلى** | الشاشة/الكيان المرتبط بالإشعار (عبر referenceId/referenceType — تنقّل سياقي) |

#### Specialized Layout Description (P3-RULE-1 — إلزامي لـ PATTERN-3)

| البند | القيمة |
|---|---|
| **نوع الشاشة الخاصة** | Bell Dropdown + History List |
| **مبرر الاستثناء** | لا Entry فعلي (القراءة فقط + إجراء "تحديد كمقروء") — لا يلائم PATTERN-1/2 |
| **المكونات الخاصة** | عداد غير مقروء، قائمة منسدلة سريعة، صفحة سجل بفلاتر (نوع/تاريخ/حالة) |

---

### B3 — مواصفة الإدخال (Input Specification)

*(لا Entry فعلي — القسم التالي يصف عرض/إجراءات فقط)*

#### عرض القائمة

| العمود | المصدر |
|---|---|
| notificationTypeId | LOV-NOTIF-001 → A5 |
| subject / bodyPreview | ENTITY-NOTIF-001 → A3 |
| notificationStatusId | LOV-NOTIF-002 → A5 |
| sentAt / createdAt | ENTITY-NOTIF-001 → A3 |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| تحديد كمقروء | PUT | — |
| فلترة (نوع/تاريخ/حالة) | GET | — |

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف (DELETE) | تصدير |
|---|---|---|---|---|---|
| SCR-NOTIF-001 | R1 (كل مستخدم — إشعاراته فقط) | — (لا إنشاء يدوي) | R1 (تحديد كمقروء فقط) | — | — |

> PERM_NOTIFICATION_INBOX_CREATE/DELETE يُولَّدان تلقائياً (CORE-9) لكن غير مُستخدَمين وظيفياً.

**Security Seed Data:**
```
SEC_PAGES  : INSERT — page_code = NOTIFICATION_INBOX, parent_id_fk = NULL (Header component)
PERMISSIONS: INSERT × 4 — PERM_NOTIFICATION_INBOX_VIEW / CREATE / UPDATE / DELETE
```

---

### B5 — الواجهات البرمجية (Functional APIs)

> **ملاحظة نطاق:** الإرسال الفعلي (API-NOTIF-001/002) لا يرتبط بشاشة معيّنة — يُستدعى من
> موديولات أخرى مباشرة أو عبر RabbitMQ، وليس من هذه الشاشة. مُدرَج هنا للاكتمال فقط (لا زر
> UI يستدعيه مباشرة).

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-NOTIF-001 | إرسال فوري (نظامي) | POST | /api/v1/notifications/send | recipientId, channelHint, templateCode, contextData, priority | تأكيد | RULE-NOTIF-001, RULE-NOTIF-002, RULE-NOTIF-003 |
| API-NOTIF-002 | جدولة إشعار (نظامي) | POST | /api/v1/notifications/schedule | نفس مدخلات API-NOTIF-001 + scheduledAt | تأكيد | RULE-NOTIF-001, RULE-NOTIF-002 |
| API-NOTIF-003 | تاريخ إشعارات المستخدم | GET | /api/v1/notifications/history | recipientId (اختياري — الافتراضي: المستخدم الحالي؛ Admin قد يستعلم عن آخرين حسب الصلاحية), type?, status?, page, size | قائمة NotificationLog | — |
| API-NOTIF-004 | الإشعارات غير المقروءة | GET | /api/v1/notifications/unread | recipientId (ضمنياً — المستخدم الحالي) | عدد `isReadFl=0` + قائمة مختصرة | — |
| API-NOTIF-005 | تحديد كمقروء | PUT | /api/v1/notifications/{id}/read | notificationLogPk | تأكيد — يُحدِّث `isReadFl=1`, `readAt=now()` | — |

---

---

## SCR-NOTIF-002 — إدارة قوالب الإشعارات

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-NOTIF-002 |
| **اسم الشاشة** | إدارة قوالب الإشعارات |
| **UI Pattern** | PATTERN-1 — Search + Entry |
| **Pattern Reason** | حقول كثيرة (> 8: رمز، اسم AR/EN، قناة، موديول، نص AR/EN، ملف مؤجَّل، حالة) — يلائم Search+Entry (5.8.2) |
| **SCR-ID Scope** | ONE SCR-ID يغطي: Search + Entry (CORE-9) |
| **P3 Implication** | Two-screen UX navigation — P3 يحدد أسماء الـ Components في F1 |
| **ENTITY-ID** | ENTITY-NOTIF-002 |
| **وظيفة الشاشة** | إدارة قوالب الإشعارات (إنشاء/تعديل/تعطيل) — Admin فقط |
| **المستخدمون** | Admin |
| **الموضع في النظام** | Notification Service ← الإعدادات ← قوالب الإشعارات |
| **روابط من** | قائمة إعدادات Notification |
| **روابط إلى** | لا يوجد |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| templateCode | نص | لا | — | |
| channelTypeId | قائمة منسدلة | لا | LOV-NOTIF-001 | lookupKey: NOTIFICATION_CHANNEL |
| moduleCode | نص | لا | — | |
| isActiveFl | قائمة منسدلة | لا | 1/0 | |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_NOTIFICATION_TEMPLATE_CREATE |
| Edit | عند تحديد سجل | PERM_NOTIFICATION_TEMPLATE_UPDATE |
| Deactivate | عند تحديد سجل | PERM_NOTIFICATION_TEMPLATE_DELETE |

#### قواعد البحث المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| RULE-NOTIF-007 | فرادة templateCode | ← see A4 |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| templateCode | نص | نعم | ENTITY-NOTIF-002 → A3 | غير قابل للتعديل بعد الإنشاء — RULE-NOTIF-007 |
| templateNameAr / templateNameEn | نص | نعم | ENTITY-NOTIF-002 → A3 | |
| channelTypeId | قائمة منسدلة | نعم | LOV-NOTIF-001 → A5 | |
| moduleCode | نص | نعم | ENTITY-NOTIF-002 → A3 | |
| templateBodyAr / templateBodyEn | نص طويل (Rich Text/Placeholder editor) | نعم | ENTITY-NOTIF-002 → A3 | يدعم Placeholders — RULE-NOTIF-006 |
| isActiveFl | Toggle | نعم | ENTITY-NOTIF-002 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ | POST / PUT | RULE-NOTIF-006, RULE-NOTIF-007 — *(تفاصيل في A4)* |
| إلغاء | navigation back | — |
| تعطيل | PUT (deactivate) | — |

#### قواعد الإدخال المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| RULE-NOTIF-006 | عند الحفظ — يجب توفر نص AR وEN معاً | ← see A4 |
| RULE-NOTIF-007 | عند الحفظ — فرادة/ثبات templateCode | ← see A4 |

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف (DELETE) | تصدير |
|---|---|---|---|---|---|
| SCR-NOTIF-002 | R2 (Admin) | R2 | R2 | R2 | R2 |

> R2 = Admin

**Security Seed Data:**
```
SEC_PAGES  : INSERT — page_code = NOTIFICATION_TEMPLATE, parent_id_fk = [NOTIFICATION_SETTINGS]
PERMISSIONS: INSERT × 4 — PERM_NOTIFICATION_TEMPLATE_VIEW / CREATE / UPDATE / DELETE
```

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-NOTIF-006 | بحث/قائمة القوالب | GET | /api/v1/notifications/templates | templateCode?, channelTypeId?, moduleCode?, isActiveFl?, page, size | قائمة NotificationTemplate | — |
| API-NOTIF-007 | إنشاء قالب | POST | /api/v1/notifications/templates | templateCode, templateNameAr/En, channelTypeId, moduleCode, templateBodyAr/En | القالب كاملاً | RULE-NOTIF-006, RULE-NOTIF-007 |
| API-NOTIF-008 | تعديل قالب | PUT | /api/v1/notifications/templates/{id} | الحقول المعدَّلة (عدا templateCode) | القالب محدَّثاً | RULE-NOTIF-006, RULE-NOTIF-007 |
| API-NOTIF-009 | تعطيل قالب | PUT | /api/v1/notifications/templates/{id}/deactivate | notificationTemplatePk | تأكيد | — |
| API-NOTIF-010 | جلب بالمعرّف | GET | /api/v1/notifications/templates/{id} | notificationTemplatePk | القالب كاملاً | — |

---

---

## SCR-NOTIF-003 — إعدادات قنوات الإشعار

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-NOTIF-003 |
| **اسم الشاشة** | إعدادات قنوات الإشعار |
| **UI Pattern** | PATTERN-2 — Inline / Modal |
| **Pattern Reason** | شاشة توجّل بسيطة (≤8 حقول فعلياً: القناة + مفعّلة + إعدادات إضافية) — 5 صفوف ثابتة (Seed)، لا Search منفصل مبرَّر |
| **SCR-ID Scope** | ONE SCR-ID — UNIFIED |
| **P3 Implication** | شاشة واحدة بقائمة توجّل inline — P3 يحدد آلية التحرير في F1 |
| **ENTITY-ID** | ENTITY-NOTIF-003 |
| **وظيفة الشاشة** | تفعيل/تعطيل كل قناة وضبط إعداداتها الإضافية (provider config) |
| **المستخدمون** | Admin |
| **الموضع في النظام** | Notification Service ← الإعدادات ← قنوات الإشعار |
| **روابط من** | قائمة إعدادات Notification |
| **روابط إلى** | لا يوجد |

#### UI Structure Decision (P2-RULE-4 — إلزامي لـ PATTERN-2)

| البند | القيمة |
|---|---|
| **Data Size** | Small (≤8 حقول) |
| **Interaction** | Inline toggle list — تعديل مباشر بدون Modal منفصل لكل صف |
| **Pattern** | PATTERN-2 |
| **Reason** | 5 صفوف ثابتة (Seed) — لا حاجة لملاحة Search+Entry |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة التوجّل

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| channelTypeId | نص (Read-Only) | نظام | ENTITY-NOTIF-003 → A3 | 5 صفوف ثابتة — لا إنشاء/حذف |
| isEnabledFl | Toggle | نعم | ENTITY-NOTIF-003 → A3 | RULE-NOTIF-005 |
| configJson | نص طويل (JSON editor) | لا | ENTITY-NOTIF-003 → A3 | إعدادات مزوّد القناة |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ | PUT | — |

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف (DELETE) | تصدير |
|---|---|---|---|---|---|
| SCR-NOTIF-003 | R2 (Admin) | — | R2 | — | — |

> R2 = Admin. PERM_NOTIFICATION_CHANNEL_CONFIG_CREATE/DELETE يُولَّدان تلقائياً (CORE-9) لكن غير مُستخدَمين (لا إنشاء/حذف — 5 صفوف ثابتة).

**Security Seed Data:**
```
SEC_PAGES  : INSERT — page_code = NOTIFICATION_CHANNEL_CONFIG, parent_id_fk = [NOTIFICATION_SETTINGS]
PERMISSIONS: INSERT × 4 — PERM_NOTIFICATION_CHANNEL_CONFIG_VIEW / CREATE / UPDATE / DELETE
```

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-NOTIF-011 | قائمة إعدادات القنوات | GET | /api/v1/notifications/channel-configs | — | قائمة NotificationChannelConfig (5 صفوف) | — |
| API-NOTIF-012 | تعديل إعداد قناة | PUT | /api/v1/notifications/channel-configs/{id} | isEnabledFl, configJson | الإعداد محدَّثاً | RULE-NOTIF-005 |

---

---

# ══════════════════════════════════════════════════════════
# STANDALONE — بعد PART B
# ══════════════════════════════════════════════════════════

---

## Permissions Summary & Registry Update

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف (DELETE) | تصدير |
|---|---|---|---|---|---|
| SCR-NOTIF-001 (لوحة إشعاراتي) | R1 | — | R1 | — | — |
| SCR-NOTIF-002 (قوالب الإشعارات) | R2 | R2 | R2 | R2 | — |
| SCR-NOTIF-003 (إعدادات القنوات) | R2 | — | R2 | — | — |

> R1 = أي مستخدم مسجَّل (إشعاراته فقط) | R2 = Admin

---

### Registry Update — MODE 1

```
## REGISTRY UPDATE — 2026-07-11
────────────────────────────────────────────────────────────────
Source Mode    : MODE 1
Feature Code   : NOTIF-001
DBS-ID         : —
Plan ID        : —
────────────────────────────────────────────────────────────────
New Entities   : ENTITY-NOTIF-001 (SHARED-owner), ENTITY-NOTIF-002 (PRIVATE),
                 ENTITY-NOTIF-003 (PRIVATE)
New Tables     : — (P2 scope)
New Lookups    : NOTIFICATION_CHANNEL, NOTIFICATION_STATUS (مؤكَّدتان سابقاً — master-registry §6)
New APIs       : API-NOTIF-001 through API-NOTIF-012
XM-IDs Open    : XM-NOTIF-[TBD] → ENTITY-FILE-001 (DEFERRED — يُصاغ رسمياً في MODE 1.5)
OQ-IDs Open    : None
Gate Status    : PASSED ✓
Next Action    : Trigger MODE 1.5 — Database Governance Engine (NotificationService)
────────────────────────────────────────────────────────────────
```

---

## OQ Log — سجل الأسئلة المفتوحة

```
## OPEN QUESTIONS LOG — Notification Service — 2026-07-11
─────────────────────────────────────────────────────────────────────
OQ-ID  │ Question        │ Status   │ Raised  │ Resolved │ Escalation
───────┼─────────────────┼──────────┼─────────┼──────────┼───────────────
—      │ None             │ —        │ —       │ —        │ —
─────────────────────────────────────────────────────────────────────
```

> صفر أسئلة مفتوحة — كل القرارات مصدرها ARCH-REF-1.8/business-policies-notif.md المُعتمَدة
> بالفعل. AQ-010/AQ-011 (مزوّد SMS/WhatsApp) هي أسئلة P0-level تقنية، مُتتبَّعة في
> master-registry §14 — ليست OQ-IDs في نطاق هذه الوثيقة (لا تؤثر على البنية الوظيفية).
> ARCH-8 (auto-raise) لا ينطبق هنا — لا كيان SHARED (owner) في هذا الموديول يملك API حذف
> (NotificationLog كيان SHARED لكن append-only بلا Delete API).

---

## MODULE GOVERNANCE INDEX — Notification Service

```
══════════════════════════════════════════════════════════════════
Feature Code     : NOTIF-001
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
srs.md            : ✓ Feature Code NOTIF-001
db-script.md      : —
execution-plan.md : —
OQ Log            : 0 open
───────────────────────────────────────────────────────────────

OPEN DEPENDENCIES
───────────────────────────────────────────────────────────────
Open XM-IDs      : XM-NOTIF-[TBD] → ENTITY-FILE-001 (DEFERRED — RXE-NOTIF trigger pending)
Open OQ-IDs      : None
Pending Findings : —
───────────────────────────────────────────────────────────────

EXECUTION STATE
───────────────────────────────────────────────────────────────
Current Phase     : MODE 1 COMPLETE
Next Safe Action  : Upload srs-notif-001.md to PROJECT-2 → trigger MODE 1.5
                     (DB_TARGET = POSTGRESQL_16)
Execution Readiness: READY — كل بوابات MODE 1 اجتازت
───────────────────────────────────────────────────────────────
```

---
*نهاية الوثيقة | End of srs.md*
*Governed by: SRS Governance Engine (Project 1)*
*Feature Code: NOTIF-001 | Version: 1.0*
*Structure: PART A (Module Foundation) + PART B (Screen Specifications)*
*Next Mode: MODE 1.5 — Database Governance Engine (Project 2) — DB_TARGET = POSTGRESQL_16*
