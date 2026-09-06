<!-- SRS — Governed by SRS Governance Engine (Project 1) | PART A + PART B -->

# وثيقة التحليل (SRS)
## المرافق المشتركة | Common Utils (CU)

---

# PART A — MODULE FOUNDATION

## A1 — معلومات الوثيقة (Document Information)

| البند | القيمة |
|---|---|
| **اسم المشروع** | منصة Foundation — أصول برمجية أساس قابلة لإعادة الاستخدام (Domain: ERP) |
| **الموديول** | المرافق المشتركة (Common Utils) |
| **Feature Code** | CU-001 |
| **Feature Type** | Configuration (مكوّن أساس متقاطع — Cross-Cutting Library) |
| **الطبقة / النوع** | L1 · Cross-Cutting Foundation · ROOT |
| **إعداد بواسطة** | SRS Governance Engine (P1) — Architect: Hesham |
| **النسخة** | 1.0 |
| **التاريخ** | 2026-09-02 |
| **الحالة** | Draft |
| **Open Questions** | None — see OQ Log |
| **Governed by** | SRS Governance Engine (Project 1) |
| **Deployment Surface** | Backend-only — لا واجهة أمامية (قرار Architect 2026-09-02) |

## A2 — السياق الوظيفي (Functional Context)

### ما يشمله هذا الموديول
> Common Utils مكوّن أساس متقاطع (Cross-Cutting) — **مكتبة مشتركة** لا موديول أعمال — تعتمد عليه كل الموديولات ولا يعتمد على أيٍّ منها (ROOT). يجمّع خمس قدرات (المصدر: module-registry-CU §RESPONSIBILITIES):
> - **Specification / Filtering** — استعلام وفلترة ديناميكية (predicate builder). كود صرف — بلا كيان.
> - **Global Exceptions** — تسلسل استثناءات + معالج مركزي + شكل خطأ موحّد. كود صرف. (الـ Error Catalog/ERR-IDs ملك P3.)
> - **Bundle (i18n)** — رسائل AR/EN عبر resource bundles ملفّية. بلا كيان.
> - **Configuration** — مخزن إعدادات key/value وقت التشغيل (الكيان الوحيد: `AppConfiguration`).
> - **Events** — أحداث داخلية متزامنة (Spring ApplicationEvent). بلا كيان، بلا وسيط.

### ما لا يشمله هذا الموديول
> لا Workflow Engine (RULE-13=OFF) · لا وسيط رسائل خارجي · لا Error Catalog (ملك P3) · لا واجهة أمامية · لا MD_MASTER_LOOKUP مركزي في نطاق Foundation.

### وظيفة الموديول
> يمنح بقية الموديولات مرافق موحّدة (فلترة، استثناءات، i18n، إعدادات، أحداث) بأقل glue code. الـ Operator يضبط إعدادات المنصة key/value وقت التشغيل دون إعادة نشر.

### الوصف الوظيفي التفصيلي
> القدرات الأربع آليات كود بلا بيانات، تُستهلَك بالحقن. القدرة الوحيدة ذات بيانات مُخزَّنة هي Configuration: مخزن `AppConfiguration` key/value يقرأه أي موديول عبر `ConfigurationService`، ويُدار عبر REST APIs يستهلكها تطبيق إدارة لاحق. مبدأ حاكم: تعقيد متوسط (POLICY-CLI-01).

### ملاحظات عامة
- **قرار P1:** `AppConfiguration` key/value نقي — `configKey` هو الهوية — لذا **بلا `nameAr`/`nameEn`** (لا يناسبان key/value)، ويُكتفى بـ `notes`. المصدر: طبيعة الكيان + POLICY-CLI-01. *(انحراف موثّق — HR-1.)*
- **قرار P1:** لا `valueType` LOV (module-registry يتركها لـ P1؛ التعقيد المتوسط يرجّح تخزين نصي). النتيجة: CU يملك **صفر LOVs**. Override: يُضاف عند حاجة فعلية.
- **قرار P1:** i18n عبر resource bundles ملفّية (لا كيان MessageCatalog). Override: يُضاف عند طلب ترجمات محرّرة وقت التشغيل.

## A3 — الكيانات والحقول (Entities & Fields)

### ENTITY-CU-001 — AppConfiguration (إعدادات المنصة)

| البند | القيمة |
|---|---|
| **النوع** | PRIVATE — Configuration |
| **Business Code** | NO — BC-RULE-0 (كيان إعدادات داخلي) |
| **المصدر** | module-registry-CU §ENTITIES OWNED + §RESPONSIBILITIES; prd-CU US-CU-001 |
| **العمليات** | Create, Read, Update, Deactivate (soft) |
| **Cross-Module** | None (ROOT) |

#### حقول الكيان — (DB_TARGET = POSTGRESQL_16)

| اسم الحقل | نوع البيانات | إلزامي | القيم/المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| appConfigurationPk | BIGINT (PK) | نظام | Sequence | رقم تلقائي | المعرف | ID |
| configKey | VARCHAR(150) | نعم | UNIQUE | Read-Only بعد الإنشاء (RULE-CU-003) | مفتاح الإعداد | Config Key |
| configValue | TEXT | نعم | — | قيمة الإعداد (نص) | قيمة الإعداد | Config Value |
| notes | VARCHAR(2000) | لا | — | وصف اختياري | ملاحظات | Notes |
| isActiveFl | SMALLINT | نعم | 1/0 | ⚠ Fl إلزامي | نشط | Active |
| createdBy | VARCHAR(255) | نظام | — | AuditEntityListener | أنشئ بواسطة | Created By |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ الإنشاء | Created At |
| updatedBy | VARCHAR(255) | نظام | — | AuditEntityListener | عُدِّل بواسطة | Updated By |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ التعديل | Updated At |

## A4 — قواعد التحقق (Business Rules)

### RULE-CU-001 — تفرّد مفتاح الإعداد
| Scope | Trigger | Statement | Message-AR | Message-EN | Source |
|---|---|---|---|---|---|
| ENTITY-CU-001 | عند الإنشاء | The system MUST prevent creating an AppConfiguration when `configKey` already exists. | مفتاح الإعداد موجود مسبقاً — اختر مفتاحاً فريداً. | Configuration key already exists — choose a unique key. | module-registry-CU §Configuration |

### RULE-CU-002 — الحقول الإلزامية
| Scope | Trigger | Statement | Message-AR | Message-EN | Source |
|---|---|---|---|---|---|
| ENTITY-CU-001 | حفظ/تعديل | The system MUST require `configKey` and `configValue` before saving. | مفتاح الإعداد وقيمته إلزاميان. | Config key and value are required. | طبيعة الكيان (key/value) |

### RULE-CU-003 — ثبات المفتاح بعد الإنشاء
| Scope | Trigger | Statement | Message-AR | Message-EN | Source |
|---|---|---|---|---|---|
| ENTITY-CU-001 | تعديل | The system MUST prevent modifying `configKey` after creation. | لا يمكن تعديل مفتاح الإعداد بعد إنشائه. | Config key cannot be changed after creation. | قرار تصميم P1 (POLICY-CLI-02) ⚠ lower-certainty |

## A5 — قوائم القيم (LOV / Lookup)
> **None.** CU لا يملك LOVs (module-registry §LOVs OWNED = none). لا MD_MASTER_LOOKUP مركزي في domain الأساس.

## A6 — دورة الحالة (Status Lifecycle)
> **لا ينطبق.** `isActiveFl` فقط (حالتان) — دون حدّ SCR-5. لا statusId، لا Workflow (RULE-13).

## A7 — تبعيات الموديولات (Module Dependencies)
> CU **ROOT** — لا يعتمد ولا يستهلك. **لا XM candidates.**
> مُعتمِدون عليه (معلوماتي): SEC/FILE/NOTIF جميعها USES مكتبة CU — علاقة مكتبة لا XM (master-registry §8).

---

# PART B — SCREEN SPECIFICATIONS
> **لا شاشات — موديول Backend-only.** CU مكتبة تُستهلَك برمجياً؛ لا واجهة خاصة به (قرار Architect 2026-09-02). إدارة `AppConfiguration` عبر REST APIs يستهلكها تطبيق إدارة لاحق خارج نطاق CU. **لا SCR-IDs، لا SEC_PAGES، CORE-9 لا ينطبق.**

---

# MODULE-LEVEL FUNCTIONAL APIs (Backend-only)
> STACK-1: `/api/v1/[module]/[resource]` — Spring Boot/Java. أنواع POSTGRESQL_16.

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-CU-001 | إنشاء | POST | /api/v1/common/configurations | configKey, configValue, notes? | AppConfiguration | RULE-CU-001, 002 |
| API-CU-002 | بحث/قائمة | GET | /api/v1/common/configurations | configKey?, isActiveFl?, page, size | قائمة | — |
| API-CU-003 | تعديل | PUT | /api/v1/common/configurations/{key} | configValue, notes?, isActiveFl? | محدَّث | RULE-CU-002, 003 |
| API-CU-004 | إلغاء (soft) | DELETE | /api/v1/common/configurations/{key} | configKey | تأكيد | — |
| API-CU-005 | جلب بالمفتاح | GET | /api/v1/common/configurations/{key} | configKey | AppConfiguration | — |

> **Internal (in-process):** `ConfigurationService.getValue(configKey)` — قراءة داخلية للموديولات الأخرى. جزء من مكتبة CU.

---

# STANDALONE

## Permissions Summary & Registry Update
> لا صفحات ولا صلاحيات CORE-9 (Backend-only بلا شاشات). التفويض على REST APIs يُطبَّق على مستوى الـ API.

### Registry Update — MODE 1
```
Source Mode  : MODE 1 | Feature Code: CU-001
New Entities : ENTITY-CU-001 (AppConfiguration — PRIVATE, Configuration)
New Lookups  : — | New APIs: API-CU-001→005 (+ internal ConfigurationService.getValue)
XM-IDs Open  : — (ROOT) | OQ-IDs Open: None
Gate Status  : PASSED ✓ | Next: MODE 1.5 (Project 2)
```
> لمشرف السجل: master-registry §10 → CU·P1=✓، وأسند ENTITY-CU-001 في §5.

## OQ Log
```
OPEN QUESTIONS LOG — Common Utils (CU) — 2026-09-02
— None — لا أسئلة مفتوحة
```

---
*End of srs-CU.md | Feature Code: CU-001 | v1.0 | Backend-only | Next: MODE 1.5*
