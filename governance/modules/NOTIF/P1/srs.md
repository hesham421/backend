<!-- SRS — Governed by SRS Governance Engine (Project 1) | PART A + PART B -->

# وثيقة التحليل (SRS)
## خدمة الإشعارات | Notification Service (NOTIF)

---

# PART A — MODULE FOUNDATION

## A1 — معلومات الوثيقة (Document Information)

| البند | القيمة |
|---|---|
| **اسم المشروع** | منصة Foundation (Domain: ERP) |
| **الموديول** | خدمة الإشعارات (Notification Service) |
| **Feature Code** | NOTIF-001 |
| **Feature Type** | Transactional (NotificationLog) + Configuration (Template, ChannelConfig) |
| **الطبقة / النوع** | L1 · Service · dep: CU, SEC, FILE |
| **النسخة** | 1.2 (حُسِم OQ-NOTIF-001 + تطبيق حسم OQ-SEC-001) |
| **التاريخ** | 2026-09-02 |
| **الحالة** | Draft |
| **Open Questions** | None (OQ-NOTIF-001 RESOLVED) — see OQ Log |
| **Governed by** | SRS Governance Engine (Project 1) |
| **Deployment Surface** | **Backend + Frontend** — service + in-process event listener + شاشات إدارة |

## A2 — السياق الوظيفي (Functional Context)

### ما يشمله هذا الموديول
> أساس إشعارات متعدد القنوات: خمس قنوات مبنية ومُفعّلة (EMAIL/SMS/WHATSAPP/PUSH/INTERNAL)، تصميم موحّد بمميّز `channel_type`، والموديول المُرسِل يختار القنوات عبر `channelHint`، وتبقى الخدمة محايدة وتتفرّع لصفّ سجل لكل قناة — **مع شاشات إدارة للقوالب والقنوات وسجل الإشعارات**. المصدر: module-registry-NOTIF §SCOPE NOTE.

### ما لا يشمله هذا الموديول
> منطق أعمال المُرسِل · وسيط رسائل خارجي (RabbitMQ — تُستخدَم أحداث CU) · إطار تكامل ثقيل (Camel — JavaMailSender مباشرة) · التحقق من JWT ذاتياً · نصّ القوالب في ملفات خارجية (inline؛ file_id للمرفقات) · Workflow Engine (RULE-13=OFF).

### وظيفة الموديول
> استقبال طلب إشعار (حدث CU أو API)، حلّه مقابل قالب ثنائي اللغة، التفرّع لصفوف سجل لكل قناة، الإرسال عبر مزوّد كل قناة مع إعادة محاولات محكومة، وتسجيل الحالة النهائية — مع شاشات لإدارة القوالب/القنوات ومتابعة السجل.

### الوصف الوظيفي التفصيلي
> يستهلك NOTIF هوية المستلِم (UserAccount) من Security SOFT، وخدمة الملفات (file_id) للمرفقات. القناة المعطّلة → CHANNEL_DISABLED دون محاولة. عند الفشل تُعاد المحاولة حتى 5 مرات (2ث، تراجع 1.5×) ثم FAILED. **المزوّد الفعلي لكل قناة قرار P3** (تصميم مستقل عن المزوّد — OQ-NOTIF-001 محسوم). تعقيد متوسط.

### ملاحظات عامة
- **قرار P1:** NotificationLog حركي **تقني** — لا `fiscal_year_id`/`period_id` — انحراف موثّق عن 5.4.2.
- **قرار P1:** لا Business Code (كلها داخلية — BC-RULE-0 غير منطبق).
- **قرار P1:** اعتماد المزوّد في `configJson` لا في الكود (reg §SCOPE).
- **قرار Architect (2026-09-02) — حسم OQ-NOTIF-001:** التصميم يبقى **مستقلاً عن المزوّد**؛ اختيار المزوّد الفعلي (SMTP/SMS/WhatsApp/Push) مُفوَّض إلى P3 دون أي أثر على الجداول أو الـ SRS.
- **قرار Architect (2026-09-02) — تطبيق حسم OQ-SEC-001:** المستلِم غير النشط لا يُرسَل إليه (RULE-NOTIF-007)؛ سجلات الإشعارات التاريخية تُبقى.

## A3 — الكيانات والحقول (Entities & Fields)
*(POSTGRESQL_16 — audit مطوي: createdBy/At, updatedBy/At)*

### ENTITY-NOTIF-001 — NotificationLog (سجل الإشعار)
| PRIVATE · Transactional (صف/قناة/حدث) | BC: NO | module-registry-NOTIF; prd-NOTIF US-NOTIF-001/002 | Create(fan-out), Read/Query, Retry(internal) |
| Cross-Module: recipientId → UserAccount (SEC) SOFT-READ |

| الحقل | النوع | إلزامي | القيم/المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| notificationLogPk | BIGINT (PK) | نظام | Sequence | — | المعرف | ID |
| recipientId | BIGINT | نعم | UserAccount (SEC) SOFT | هوية المستلِم | المستلِم | Recipient |
| channelTypeId | VARCHAR(20) | نعم | LOV-NOTIF-001 | code | القناة | Channel |
| notificationStatusId | VARCHAR(30) | نعم | LOV-NOTIF-002 | دورة الحياة (A6) | الحالة | Status |
| templateFk | BIGINT (FK) | نعم | ENTITY-NOTIF-002 | القالب | القالب | Template |
| moduleCode | VARCHAR(50) | نعم | — | الموديول المُرسِل | رمز الموديول | Module Code |
| referenceId | BIGINT | لا | — | مرجع الكيان المصدر | معرّف المرجع | Reference ID |
| referenceType | VARCHAR(100) | لا | — | نوعه | نوع المرجع | Reference Type |
| retryCount | SMALLINT | نظام | 0 | RULE-NOTIF-002 | عدد المحاولات | Retry Count |
| errorMessage | TEXT | لا | — | سبب الفشل | رسالة الخطأ | Error Message |
| sentAt | TIMESTAMP | لا | — | وقت الإرسال | تاريخ الإرسال | Sent At |

### ENTITY-NOTIF-002 — NotificationTemplate (قالب الإشعار)
| PRIVATE · Config/Master | BC: NO | module-registry-NOTIF; prd-NOTIF US-NOTIF-003 | Create, Read, Update, Deactivate |

| الحقل | النوع | إلزامي | القيم | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| notificationTemplatePk | BIGINT (PK) | نظام | Sequence | — | المعرف | ID |
| templateCode | VARCHAR(80) | نعم | UNIQUE | مفتاح القالب | رمز القالب | Template Code |
| nameAr / nameEn | VARCHAR | نعم | — | — | الاسم | Name |
| subjectAr / subjectEn | VARCHAR(300) | لا | — | لعنوان البريد | العنوان | Subject |
| bodyAr | TEXT | نعم | — | نص عربي | المتن (عربي) | Body (AR) |
| bodyEn | TEXT | نعم | — | نص إنجليزي | المتن (إنجليزي) | Body (EN) |
| attachmentFileId | BIGINT | لا | File Service (API) | مرفق اختياري | مرفق | Attachment |
| isActiveFl | SMALLINT | نعم | 1/0 | ⚠ Fl | نشط | Active |

### ENTITY-NOTIF-003 — NotificationChannelConfig (تهيئة القناة)
| PRIVATE · Config | BC: NO | module-registry-NOTIF; prd-NOTIF US-NOTIF-004 | Create, Read, Update(enable/disable), Deactivate |

| الحقل | النوع | إلزامي | القيم | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| notificationChannelConfigPk | BIGINT (PK) | نظام | Sequence | — | المعرف | ID |
| channelTypeId | VARCHAR(20) | نعم | LOV-NOTIF-001 · UNIQUE | قناة فريدة | القناة | Channel |
| isEnabledFl | SMALLINT | نعم | 1/0 | تفعيل وقت التشغيل | مُفعّلة | Enabled |
| configJson | TEXT | لا | — | اعتماد المزوّد (JSON) — المزوّد الفعلي قرار P3 | تهيئة المزوّد | Provider Config |

## A4 — قواعد التحقق (Business Rules)
| RULE-ID | Scope | Trigger | Statement | Message-AR | Message-EN | Source |
|---|---|---|---|---|---|---|
| RULE-NOTIF-001 | NOTIF-001 | Dispatch | fan out one log per requested channel (channelHint); no business routing inside. | سجل لكل قناة مطلوبة. | One log per requested channel. | POLICY-CLI-02 + reg |
| RULE-NOTIF-002 | NOTIF-001 | Failure | retry ≤5 (2s, ×1.5), then FAILED. | إعادة حتى ٥ ثم فشل. | Retry ≤5 then FAILED. | biz-pol POLICY-CLI-03 |
| RULE-NOTIF-003 | NOTIF-001 | Dispatch | disabled channel → CHANNEL_DISABLED, no retry. | القناة المعطّلة تُسجَّل دون محاولة. | Disabled channel logged, not retried. | biz-pol POLICY-CLI-03 |
| RULE-NOTIF-004 | NOTIF-002 | Compose | bilingual templates; attachment via File `file_id`. | قوالب ثنائية اللغة والمرفق عبر الملفات. | Bilingual templates; File attachment. | reg §AUTO |
| RULE-NOTIF-005 | NOTIF-001 | Access | delegate auth to Security filter. | — (معماري) | — (architecture) | reg §AUTO |
| RULE-NOTIF-006 | NOTIF-002/003 | إنشاء | unique `templateCode`; unique channel/config. | رمز القالب والقناة فريدان. | Template code & channel unique. | integrity |
| **RULE-NOTIF-007** | NOTIF-001 | Dispatch | MUST NOT dispatch to a recipient whose UserAccount is inactive (skip); historical logs retained. | لا يُرسَل إشعار لمستلِم حسابه غير نشط؛ تُبقى السجلات التاريخية. | Must not dispatch to an inactive recipient; history retained. | **حسم OQ-SEC-001 (consumer-side)** |

## A5 — قوائم القيم (LOV / Lookup)
> لا MD_MASTER_LOOKUP مركزي — قوائم **محلية** لـ NOTIF. القيمة = code، runtime-loaded، لا ENUMs.

**LOV-NOTIF-001 — NotificationChannel** (`channelTypeId`, NOTIF-001/003, Dropdown, `NOTIF_CHANNEL`): EMAIL=بريد · SMS=رسالة نصية · WHATSAPP=واتساب · PUSH=إشعار فوري · INTERNAL=داخلي.

**LOV-NOTIF-002 — NotificationStatus** (`notificationStatusId`, NOTIF-001, Dropdown, `NOTIF_STATUS`): PENDING=قيد الانتظار · SENT=مُرسَل · FAILED=فشل · CHANNEL_DISABLED=القناة معطّلة.

## A6 — دورة الحالة (Status Lifecycle) — NotificationLog
```
[PENDING] ──ناجح──► [SENT] ✓ | ──فشل بعد المحاولات──► [FAILED] ✗ | ──قناة معطّلة──► [CHANNEL_DISABLED] ⊘
```
> RULE-13 — لا Workflow.

## A7 — تبعيات الموديولات (Module Dependencies)
### الكيانات المُستهلَكة
| الكيان | ENTITY-ID | المالك | الاعتمادية | XM Candidate |
|---|---|---|---|---|
| UserAccount | ENTITY-SEC-001 | SEC | SOFT-READ (المستلِم) | نعم → XM-NOTIF-N (MODE 1.5) |
> سلوك المستلِم غير النشط محسوم: RULE-NOTIF-007 (لا إرسال؛ تُبقى السجلات) — تطبيق حسم OQ-SEC-001.

### الخدمات والتكاملات
| CU: Events/config/exceptions — USES(library) · SEC: هوية المستلِم — SOFT · FILE: مرفقات (file_id) — SOFT/service (XM candidate) · مزوّدو القنوات: عبر configJson — المزوّد الفعلي قرار P3 (OQ-NOTIF-001 محسوم) |
> master-registry §8: «NOTIF → SEC : SOFT»، «NOTIF → FILE : SOFT/service».

---

# ══════════════════════════════════════════════════════════
# PART B — SCREEN SPECIFICATIONS (Frontend: React/TS/Vite)
# ══════════════════════════════════════════════════════════

---

## SCR-NOTIF-001 — إدارة قوالب الإشعارات (Templates)
### B1
| SCR-NOTIF-001 | PATTERN-2 (SIDE_DRAWER) | كيان config بسيط (5.8.2) | ONE (CORE-9) | ENTITY-NOTIF-002 | page_code `NOTIF_TEMPLATES` (parent: الإشعارات) |

### B3
- قائمة/فلاتر: templateCode, nameAr, isActiveFl.
- تحرير (Drawer): templateCode, nameAr, nameEn, subjectAr, subjectEn, bodyAr, bodyEn, attachmentFileId (عبر خدمة الملفات), isActiveFl → A3.
- القواعد: حفظ → RULE-NOTIF-004, 006.

### B4 (CORE-9 / SEC-3) — `page_code=NOTIF_TEMPLATES`
> Security Engine يولّد الصلاحيات الأربع تلقائياً. لا seed لـ PERM_*.

| SCR-NOTIF-001 | VIEW NOTIF_ADMIN | CREATE NOTIF_ADMIN | UPDATE NOTIF_ADMIN | DELETE NOTIF_ADMIN |

### B5
API-NOTIF-004, 006.

---

## SCR-NOTIF-002 — تهيئة القنوات (Channel Configuration)
### B1
| SCR-NOTIF-002 | PATTERN-2 (SIDE_DRAWER) | كيان config (تفعيل + JSON) | ONE (CORE-9) | ENTITY-NOTIF-003 | page_code `NOTIF_CHANNELS` (parent: الإشعارات) |

### B3
- قائمة/فلاتر: channelTypeId (LOV-NOTIF-001), isEnabledFl.
- تحرير (Drawer): channelTypeId, isEnabledFl, configJson → A3.
- القواعد: حفظ → RULE-NOTIF-006 · التعطيل → RULE-NOTIF-003.

### B4 (CORE-9 / SEC-3) — `page_code=NOTIF_CHANNELS`
| SCR-NOTIF-002 | VIEW NOTIF_ADMIN | CREATE NOTIF_ADMIN | UPDATE NOTIF_ADMIN | DELETE NOTIF_ADMIN |

### B5
API-NOTIF-005, 006.

---

## SCR-NOTIF-003 — سجل الإشعارات (Notification Log — Read-only)
### B1
| SCR-NOTIF-003 | PATTERN-2 (SIDE_DRAWER) | سجل عرض فقط — قائمة + Drawer تفاصيل | ONE (CORE-9) | ENTITY-NOTIF-001 | page_code `NOTIF_LOG` (parent: الإشعارات) |

### B3
- قائمة/فلاتر: recipientId, moduleCode, `channelTypeId` (LOV-NOTIF-001), `notificationStatusId` (LOV-NOTIF-002), referenceType, نطاق تاريخ (sentAt).
- Drawer (read-only): كل الحقول + errorMessage + retryCount → A3.
- ملاحظة: السجل نظامي — الشاشة **عرض فقط**.

### B4 (CORE-9 / SEC-3) — `page_code=NOTIF_LOG`
> توليد تلقائي للصلاحيات الأربع؛ عملياً VIEW فقط (سجل نظامي). لا seed لـ PERM_*.

| SCR-NOTIF-003 | VIEW NOTIF_ADMIN | CREATE — | UPDATE — | DELETE — |

### B5
API-NOTIF-002, 003, 006.

---

# MODULE-LEVEL FUNCTIONAL APIs
> STACK-1: `/api/v1/notifications/...`. POSTGRESQL_16.

| API-ID | العملية | HTTP | المسار | RULE-IDs |
|---|---|---|---|---|
| API-NOTIF-001 | إرسال/توزيع | POST | /api/v1/notifications/dispatch | RULE-NOTIF-001, 002, 003, 004, 007 |
| API-NOTIF-002 | استعلام السجل | GET | /api/v1/notifications/logs | — |
| API-NOTIF-003 | سجل بالمعرّف | GET | /api/v1/notifications/logs/{id} | — |
| API-NOTIF-004 | CRUD القوالب | POST/GET/PUT/DELETE | /api/v1/notifications/templates | RULE-NOTIF-004, 006 |
| API-NOTIF-005 | CRUD/تفعيل القنوات | POST/GET/PUT/DELETE | /api/v1/notifications/channels | RULE-NOTIF-003, 006 |
| API-NOTIF-006 | قوائم القيم | GET | /api/v1/notifications/lookups/{lookupKey} | — |

> **Event listener (in-process):** يستمع لـ `NotificationEvent` عبر CU Events (مثال: أحداث SEC لإعادة التعيين/التفعيل).

---

# STANDALONE

## Permissions Summary & Registry Update
> CORE-9: كل شاشة = SCR-ID واحد = صف SEC_PAGES واحد. Security Engine يولّد الصلاحيات الأربع لكل page_code (لا seed أسماء PERM_* — SEC-3).

| الشاشة (page_code) | VIEW | CREATE | UPDATE | DELETE |
|---|---|---|---|---|
| SCR-NOTIF-001 (NOTIF_TEMPLATES) | NOTIF_ADMIN | NOTIF_ADMIN | NOTIF_ADMIN | NOTIF_ADMIN |
| SCR-NOTIF-002 (NOTIF_CHANNELS) | NOTIF_ADMIN | NOTIF_ADMIN | NOTIF_ADMIN | NOTIF_ADMIN |
| SCR-NOTIF-003 (NOTIF_LOG) | NOTIF_ADMIN | — | — | — |

### Registry Update — MODE 1
```
Source Mode  : MODE 1 | Feature Code: NOTIF-001 | v1.2 (OQ-NOTIF-001 resolved; OQ-SEC-001 applied)
New Entities : NOTIF-001 NotificationLog, NOTIF-002 NotificationTemplate, NOTIF-003 NotificationChannelConfig — PRIVATE
New Lookups  : NOTIF_CHANNEL, NOTIF_STATUS — local
New Screens  : SCR-NOTIF-001 (NOTIF_TEMPLATES), SCR-NOTIF-002 (NOTIF_CHANNELS), SCR-NOTIF-003 (NOTIF_LOG)
New Rules    : +RULE-NOTIF-007 (no dispatch to inactive recipient)
New APIs     : API-NOTIF-001 → API-NOTIF-006 (+ NotificationEvent listener)
XM-IDs Open  : NOTIF → SEC (SOFT UserAccount), NOTIF → FILE (SOFT/service) — MODE 1.5
OQ-IDs Open  : None ✓ (OQ-NOTIF-001 RESOLVED)
Gate Status  : PASSED ✓ | Next: MODE 1.5 (Project 2)
```
> لمشرف السجل: master-registry §10 → NOTIF·P1=✓؛ §5 NOTIF-001/002/003؛ §6 UserAccount SOFT؛ SEC_PAGES: NOTIF_TEMPLATES/NOTIF_CHANNELS/NOTIF_LOG.

## OQ Log
```
OPEN QUESTIONS LOG — Notification Service (NOTIF) — 2026-09-02
OQ-NOTIF-001 │ اعتماد مزوّد فعلي لكل قناة (SMS/WhatsApp/Push) │ RESOLVED │ MODE 1 │ 2026-09-02 │ P3-TECH
   القرار (Architect): التصميم مستقل عن المزوّد؛ اختيار المزوّد مُفوَّض إلى P3 (بلا أثر على الجداول/الـ SRS).
(مرجعي) OQ-SEC-001 (SEC) │ حُسِم — طُبِّق هنا عبر RULE-NOTIF-007 (لا إرسال لمستلِم غير نشط؛ تُبقى السجلات).
```

---
*End of srs-NOTIF.md | NOTIF-001 | v1.2 | Backend + Frontend | OQ-NOTIF-001 RESOLVED | Next: MODE 1.5*
