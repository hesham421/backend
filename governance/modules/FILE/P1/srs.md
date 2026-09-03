<!-- SRS — Governed by SRS Governance Engine (Project 1) | PART A + PART B -->

# وثيقة التحليل (SRS)
## خدمة الملفات | File Service (FILE)

---

# PART A — MODULE FOUNDATION

## A1 — معلومات الوثيقة (Document Information)

| البند | القيمة |
|---|---|
| **اسم المشروع** | منصة Foundation (Domain: ERP) |
| **الموديول** | خدمة الملفات (File Service) |
| **Feature Code** | FILE-001 |
| **Feature Type** | Transactional (FileDocument) + Reference (FileCategory) |
| **الطبقة / النوع** | L1 · Service (Provider) · dep: CU, SEC |
| **النسخة** | 1.1 (أُضيفت PART B — Frontend) |
| **التاريخ** | 2026-09-02 |
| **الحالة** | Draft |
| **Open Questions** | None — see OQ Log |
| **Governed by** | SRS Governance Engine (Project 1) |
| **Deployment Surface** | **Backend + Frontend** — provider (@Service) + REST APIs + شاشات إدارة |

## A2 — السياق الوظيفي (Functional Context)

### ما يشمله هذا الموديول
> أساس تخزين ملفات قابل لإعادة الاستخدام: بايتات في PostgreSQL BYTEA مع بياناتها الوصفية، ووصول محدود المدة عبر روابط برموز مشفّرة AES/GCM (منفصلة عن JWT). نمط مزوّد (@Service) للموديولات المستهلِكة، **مع شاشات إدارة أمامية** للفئات ومستعرض الملفات. ملكية عامة (owner_id + owner_type + module_code). المصدر: module-registry-FILE §SCOPE NOTE.

### ما لا يشمله هذا الموديول
> معالجة/معاينة PDF (PDFBox) · وسيط رسائل / معالجة غير متزامنة · تخزين على نظام ملفات خارجي · التحقق من JWT ذاتياً (يثق بمرشّح Security) · Workflow Engine (RULE-13=OFF).

### وظيفة الموديول
> رفع/تخزين آمن لأي ملف، واسترجاع عبر رابط آمن محدود المدة، وتصنيف بحدود لكل فئة، وأرشفة/إزالة ضمن دورة حياة واضحة — عبر خدمة تُحقَن وشاشات إدارة.

### الوصف الوظيفي التفصيلي
> FileDocument يخزّن البايتات والبيانات؛ الملكية polymorphic (owner_id/type/module_code) — مرجع تطبيقي لا FK محوكَم. FileCategory يعرّف الأنواع والحدود لكل مستهلك. الوصول عبر رمز AES/GCM (~100د) لكل عملية. يثق FILE بمرشّح Security ولا يتحقق من JWT. مبدأ حاكم: تعقيد متوسط.

### ملاحظات عامة
- **قرار P1 (يحسم بند PRD):** الحذف = **soft-delete** (`fileStatusId=DELETED`؛ البايتات تُبقى ما لم تُعرَّف سياسة تطهير). المصدر: business-policies-FILE §SCOPE EXCEPTIONS + prd-FILE Open Item.
- **قرار P1:** FileDocument حركي **تقني** — لا `fiscal_year_id`/`period_id` (لا سياق محاسبي) — انحراف موثّق عن 5.4.2.
- **قرار P1:** لا Business Code (يُشار للملف بالمعرّف/الرمز الآمن — BC-RULE-0 غير منطبق).

## A3 — الكيانات والحقول (Entities & Fields)
*(POSTGRESQL_16 — audit مطوي: createdBy/At, updatedBy/At)*

### ENTITY-FILE-001 — FileDocument (المستند/الملف)
| PRIVATE · Transactional | BC: NO | module-registry-FILE; prd-FILE US-FILE-001/002/004 | Upload, Read/Download(token), List, Archive, Delete(soft) |

| الحقل | النوع | إلزامي | القيم/المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| fileDocumentPk | BIGINT (PK) | نظام | Sequence | — | المعرف | ID |
| ownerId | BIGINT | نعم | — | polymorphic تطبيقي | معرّف المالك | Owner ID |
| ownerType | VARCHAR(100) | نعم | — | نوع الكيان المالك | نوع المالك | Owner Type |
| moduleCode | VARCHAR(50) | نعم | — | الموديول المالك | رمز الموديول | Module Code |
| fileName | VARCHAR(255) | نعم | — | — | اسم الملف | File Name |
| contentType | VARCHAR(150) | نظام | MIME auto-detect | لا يُوثَق من العميل (RULE-FILE-002) | نوع المحتوى | Content Type |
| fileSize | BIGINT | نظام | — | بايت | الحجم | Size |
| fileContent | BYTEA | نعم | — | بايتات الملف | المحتوى | Content |
| fileTypeId | VARCHAR(50) | نعم | LOV-FILE-001 | code | نوع الملف | File Type |
| fileStatusId | VARCHAR(50) | نعم | LOV-FILE-002 | دورة الحياة (A6) | الحالة | Status |
| fileCategoryFk | BIGINT (FK) | لا | ENTITY-FILE-002 | الفئة والحدود | الفئة | Category |

### ENTITY-FILE-002 — FileCategory (فئة المستند)
| PRIVATE · Reference | BC: NO | module-registry-FILE; biz-pol-FILE; prd-FILE US-FILE-003 | Create, Read, Update, Deactivate |

| الحقل | النوع | إلزامي | القيم | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| fileCategoryPk | BIGINT (PK) | نظام | Sequence | — | المعرف | ID |
| categoryCode | VARCHAR(50) | نعم | UNIQUE | — | رمز الفئة | Category Code |
| nameAr / nameEn | VARCHAR | نعم | — | — | الاسم | Name |
| maxSizeBytes | BIGINT | لا | افتراضي RULE-FILE-001 | تجاوز الحد لكل فئة | الحد الأقصى | Max Size |
| allowedContentTypes | TEXT | لا | — | تجاوز الأنواع لكل فئة | الأنواع المسموحة | Allowed Types |
| isActiveFl | SMALLINT | نعم | 1/0 | ⚠ Fl | نشط | Active |

## A4 — قواعد التحقق (Business Rules)
| RULE-ID | Scope | Trigger | Statement | Message-AR | Message-EN | Source |
|---|---|---|---|---|---|---|
| RULE-FILE-001 | FILE-001 | Upload | content ≤ 5MB, request ≤ 10MB (overridable/category). | حجم الملف يتجاوز المسموح. | File exceeds allowed size. | biz-pol POLICY-CLI-01 |
| RULE-FILE-002 | FILE-001 | Upload | auto-detect MIME (not client header); restrict to accepted types. | نوع الملف غير مسموح. | File type not allowed. | biz-pol POLICY-CLI-02 |
| RULE-FILE-003 | FILE-001 | Up/Download | require fresh AES/GCM token; invalid after ~100m; single-use. | رابط الوصول غير صالح/منتهٍ. | Access link invalid/expired. | biz-pol POLICY-CLI-03 |
| RULE-FILE-004 | FILE-001 | Access | delegate auth to Security filter (no self JWT). | — (معماري) | — (architecture) | reg §AUTO |
| RULE-FILE-005 | FILE-001 | Upload | require ownerId, ownerType, moduleCode. | بيانات الملكية إلزامية. | Ownership fields required. | reg §SCOPE |
| RULE-FILE-006 | FILE-001 | Delete | soft delete (`fileStatusId=DELETED`); bytes retained by default. | حذف منطقي دون إزالة فيزيائية. | Soft delete; bytes retained. | P1 — يحسم prd-FILE |
| RULE-FILE-007 | FILE-002 | إنشاء | unique `categoryCode`. | رمز الفئة مستخدَم مسبقاً. | Category code exists. | integrity |

## A5 — قوائم القيم (LOV / Lookup)
> لا MD_MASTER_LOOKUP مركزي — قوائم **محلية** لـ FILE. القيمة = code، runtime-loaded، لا ENUMs.

**LOV-FILE-001 — FileType** (`fileTypeId`, FILE-001, Dropdown, `FILE_FILE_TYPE`): IMAGE=صورة · DOCUMENT=مستند · SPREADSHEET=جدول · ARCHIVE=أرشيف · OTHER=أخرى.

**LOV-FILE-002 — FileStatus** (`fileStatusId`, FILE-001, Dropdown, `FILE_FILE_STATUS`): ACTIVE=نشط · ARCHIVED=مؤرشف · DELETED=محذوف.

## A6 — دورة الحالة (Status Lifecycle) — FileDocument
```
[ACTIVE] ──أرشفة──► [ARCHIVED] ──حذف──► [DELETED] ✗   ([ACTIVE] ──حذف soft──► [DELETED])
```
> RULE-13 — لا Workflow.

## A7 — تبعيات الموديولات (Module Dependencies)
### الكيانات المُستهلَكة
> لا كيان محوكَم: owner_id/type مرجع polymorphic (reg §SHARED CONSUMED = none).
### الخدمات والتكاملات
| الخدمة | الغرض | نوع التكامل |
|---|---|---|
| Security (SEC) | مرشّح المصادقة؛ createdBy | SOFT — XM candidate SOFT-READ (MODE 1.5) |
| Common Utils (CU) | exceptions/config/events/filtering | USES (library) |
> master-registry §8: «FILE → SEC : SOFT».

---

# ══════════════════════════════════════════════════════════
# PART B — SCREEN SPECIFICATIONS (Frontend: React/TS/Vite)
# ══════════════════════════════════════════════════════════
> ملاحظة: رفع الملفات عملياً يُدمَج داخل شاشة الموديول المالك (سياقياً)؛ الشاشتان أدناه إداريتان (فئات + مستعرض/إدارة).

---

## SCR-FILE-001 — إدارة فئات الملفات (File Categories)

### B1 — تعريف الشاشة
| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-FILE-001 · **UI Pattern** PATTERN-2 (SIDE_DRAWER) |
| **Pattern Reason** | FileCategory كيان مرجعي بسيط — لا سطور متكررة ولا هرمية (5.8.2 → PATTERN-2) |
| **SCR-ID Scope** | ONE SCR-ID — Unified — CORE-9 |
| **ENTITY-ID** | ENTITY-FILE-002 |
| **page_code** | `FILE_CATEGORIES` · parent nav: خدمة الملفات |

### B3 — مواصفة الإدخال
- **قائمة + فلاتر:** categoryCode, nameAr, isActiveFl.
- **تحرير (Drawer):** categoryCode, nameAr, nameEn, maxSizeBytes, allowedContentTypes, isActiveFl → A3.
- **القواعد:** حفظ → RULE-FILE-007 (وحدود الفئة تُغذّي RULE-FILE-001/002).

### B4 — الصلاحيات (CORE-9 / SEC-3)
> `page_code = FILE_CATEGORIES` — Security Engine يولّد الصلاحيات الأربع تلقائياً. لا seed لـ PERM_* (SEC-3).

| الشاشة | VIEW | CREATE | UPDATE | DELETE |
|---|---|---|---|---|
| SCR-FILE-001 | FILE_ADMIN | FILE_ADMIN | FILE_ADMIN | FILE_ADMIN |
> FILE_ADMIN = دور إدارة الملفات الأساس؛ المنح بيانات (تُدار عبر SEC / SCR-SEC-002).

### B5 — الواجهات المستخدَمة
API-FILE-007 (CRUD categories), API-FILE-008 (lookups).

---

## SCR-FILE-002 — مستعرض الملفات (File Browser / Management)

### B1 — تعريف الشاشة
| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-FILE-002 · **UI Pattern** PATTERN-2 (SIDE_DRAWER) |
| **Pattern Reason** | قائمة بحث + Drawer تفاصيل (بيانات وصفية/تنزيل/أرشفة) — لا سطور محسوبة ولا هرمية |
| **SCR-ID Scope** | ONE SCR-ID — Unified — CORE-9 |
| **ENTITY-ID** | ENTITY-FILE-001 |
| **page_code** | `FILE_BROWSER` · parent nav: خدمة الملفات |

### B3 — مواصفة الإدخال
- **قائمة + فلاتر:** fileName, moduleCode, ownerType/ownerId, `fileTypeId` (LOV-FILE-001), `fileStatusId` (LOV-FILE-002).
- **Drawer (تفاصيل/إجراءات):** عرض البيانات الوصفية (read-only) + تنزيل عبر رمز آمن + أرشفة/حذف. (المحتوى fileContent لا يُعرَض كحقل — يُنزَّل عبر الرابط الآمن.)
- **القواعد:** تنزيل → RULE-FILE-003, 004 · حذف/أرشفة → RULE-FILE-006 · العرض يخضع لملكية RULE-FILE-005.

### B4 — الصلاحيات (CORE-9 / SEC-3)
> `page_code = FILE_BROWSER` — توليد تلقائي للصلاحيات الأربع. لا seed لـ PERM_*.

| الشاشة | VIEW | CREATE | UPDATE | DELETE |
|---|---|---|---|---|
| SCR-FILE-002 | FILE_ADMIN | — (الرفع سياقي في الموديول المالك) | FILE_ADMIN (أرشفة) | FILE_ADMIN (حذف soft) |
> CREATE عبر هذه الشاشة اختياري — الرفع الأساسي يتم في شاشة الموديول المالك عبر خدمة FILE.

### B5 — الواجهات المستخدَمة
API-FILE-002 (access-token), API-FILE-003 (download), API-FILE-004 (metadata), API-FILE-005 (list by owner), API-FILE-006 (archive/delete), API-FILE-008 (lookups). *(الرفع: API-FILE-001.)*

---

# MODULE-LEVEL FUNCTIONAL APIs
> STACK-1: `/api/v1/files/...`. POSTGRESQL_16.

| API-ID | العملية | HTTP | المسار | RULE-IDs |
|---|---|---|---|---|
| API-FILE-001 | رفع ملف | POST | /api/v1/files | RULE-FILE-001, 002, 005 |
| API-FILE-002 | رمز وصول | POST | /api/v1/files/{id}/access-token | RULE-FILE-003 |
| API-FILE-003 | تنزيل | GET | /api/v1/files/download?token= | RULE-FILE-003, 004 |
| API-FILE-004 | بيانات وصفية | GET | /api/v1/files/{id} | RULE-FILE-004 |
| API-FILE-005 | قائمة حسب المالك | GET | /api/v1/files?ownerId=&ownerType=&moduleCode= | RULE-FILE-004 |
| API-FILE-006 | أرشفة/حذف (soft) | DELETE | /api/v1/files/{id} | RULE-FILE-006 |
| API-FILE-007 | CRUD الفئات | POST/GET/PUT/DELETE | /api/v1/files/categories | RULE-FILE-007 |
| API-FILE-008 | قوائم القيم | GET | /api/v1/files/lookups/{lookupKey} | — |

> **Provider (in-process):** `FileService` (store / retrieve / issueAccessToken) — يُحقَن في NOTIF والموديولات المستقبلية.

---

# STANDALONE

## Permissions Summary & Registry Update
> CORE-9: كل شاشة = SCR-ID واحد = صف SEC_PAGES واحد. Security Engine يولّد الصلاحيات الأربع لكل page_code (لا seed أسماء PERM_* — SEC-3).

| الشاشة (page_code) | VIEW | CREATE | UPDATE | DELETE |
|---|---|---|---|---|
| SCR-FILE-001 (FILE_CATEGORIES) | FILE_ADMIN | FILE_ADMIN | FILE_ADMIN | FILE_ADMIN |
| SCR-FILE-002 (FILE_BROWSER) | FILE_ADMIN | — | FILE_ADMIN | FILE_ADMIN |

### Registry Update — MODE 1
```
Source Mode  : MODE 1 | Feature Code: FILE-001 | v1.1 (+Frontend)
New Entities : FILE-001 FileDocument (Transactional), FILE-002 FileCategory (Reference) — PRIVATE
New Lookups  : FILE_FILE_TYPE, FILE_FILE_STATUS — local
New Screens  : SCR-FILE-001 (FILE_CATEGORIES), SCR-FILE-002 (FILE_BROWSER)
New APIs     : API-FILE-001 → API-FILE-008 (+ provider FileService)
XM-IDs Open  : FILE → SEC (SOFT-READ candidate) — MODE 1.5
OQ-IDs Open  : None (delete-semantics resolved — RULE-FILE-006)
Gate Status  : PASSED ✓ | Next: MODE 1.5 (Project 2)
```
> لمشرف السجل: master-registry §10 → FILE·P1=✓؛ §5 FILE-001/002؛ سجّل SEC_PAGES: FILE_CATEGORIES/FILE_BROWSER.

## OQ Log
```
OPEN QUESTIONS LOG — File Service (FILE) — 2026-09-02
— None — بند حذف الملفات حُسِم في RULE-FILE-006 (soft-delete).
```

---
*End of srs-FILE.md | FILE-001 | v1.1 | Backend + Frontend | Next: MODE 1.5*
