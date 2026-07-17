&lt;!-- ═══════════════════════════════════════════════════════════ --&gt;
&lt;!-- SRS — وثيقة التحليل والمتطلبات                             --&gt;
&lt;!-- Governed by: SRS Governance Engine (Project 1)             --&gt;
&lt;!-- Compatible: PROJECT-2 | PROJECT-3 | PROJECT-4              --&gt;
&lt;!-- Structure : PART A (Module Foundation) + PART B (Screens)  --&gt;
&lt;!-- DB_TARGET : POSTGRESQL_16                                   --&gt;
&lt;!-- ═══════════════════════════════════════════════════════════ --&gt;

# وثيقة التحليل (SRS)
## موديول الهيكل التنظيمي | Organization Module

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
| **الموديول** | الهيكل التنظيمي — Organization |
| **Feature Code** | ORG-001 |
| **Feature Type** | Master Data (Core — Foundation) |
| **الطبقة** | LAYER-1 — Foundation (L1-1) |
| **إعداد بواسطة** | SRS Governance Engine (Project 1) |
| **النسخة** | 1.0 |
| **التاريخ** | 2026-06-28 |
| **الحالة** | GOVERNED ✓ MODE 2 — ALIGN GATE PASSED ✓ |
| **DB_TARGET** | POSTGRESQL_16 |
| **Open Questions** | 1 active (OQ-001 — DEFERRED) — see OQ Log |
| **Governed by** | SRS Governance Engine (Project 1) |

---

## A2 — السياق الوظيفي (Functional Context)

### ما يشمله هذا الموديول

> يشمل هذا الموديول تعريف وإدارة الهيكل التنظيمي الكامل للمنشأة، ويتضمن: الكيانات القانونية، الفروع، المناطق، الأقسام (بهيكل شجري)، مراكز التكلفة (بهيكل شجري)، مراكز الربح، ومواقع العمل. وهو الموديول الجذري (ROOT MODULE) الذي تعتمد عليه جميع الموديولات الأخرى في المنصة.

### ما لا يشمله هذا الموديول

> - لا يشمل إدارة الموظفين أو الموارد البشرية
> - لا يشمل إدارة المستودعات التشغيلية (تملكها وحدة Inventory 3.2؛ LocationSite هو المفهوم المادي L1 فقط)
> - لا يشمل وحدة الأعمال BusinessUnit (مؤجلة — غير مسجلة في master-registry Section 5)
> - لا يشمل إدارة الدول — تملكها MasterData (1.4)
> - لا يشمل أي منطق مالي أو محاسبي
> - لا يطبّق Row-Level Security بذاته — مفوَّض بالكامل لموديول Security

### وظيفة الموديول

> يُمكّن هذا الموديول المستخدمين المخوّلين من إنشاء وإدارة الهيكل التنظيمي للمنشأة بالكامل عبر تسلسل هرمي متماسك يبدأ بالكيان القانوني وينتهي بأدق وحدات التشغيل، مما يوفر البنية التحتية المرجعية اللازمة لجميع عمليات النظام.

### الوصف الوظيفي التفصيلي

> يُنشئ المستخدم أولاً الكيانات القانونية (LegalEntity) التي تمثل الشركات أو المؤسسات القانونية المسجلة رسمياً. تحت كل كيان قانوني تُنشأ الفروع (Branch) التي تمثل التواجد الجغرافي أو التشغيلي. تُعرَّف المناطق (Region) مرتبطةً بالكيان القانوني لتوفير تجميع جغرافي أو مبيعاتي. تحت كل فرع تُبنى هياكل الأقسام (Department) بشكل شجري يدعم مستويات متعددة مع تمييز بين عُقد الملخص (SUMMARY) وعُقد التفصيل (DETAIL). بالمثل تُبنى مراكز التكلفة (CostCenter) بهيكل شجري تحت الفرع. مراكز الربح (ProfitCenter) ترتبط بالكيان القانوني مباشرة. وأخيراً تُعرَّف مواقع العمل (LocationSite) تحت كل فرع لتمثيل المواقع المادية. جميع الكيانات تحمل رموز أعمال (Business Codes) غير قابلة للتعديل بعد الحفظ، وجميعها تخضع للتدقيق الكامل عبر AuditEntityListener.

#### الوضع الحالي

| الخطوات | الجهة | ملاحظات |
|---|---|---|
| إنشاء الكيان القانوني | مسؤول النظام | أول خطوة في بناء الهيكل |
| إنشاء الفروع والمناطق | مسؤول التنظيم | تعتمد على الكيان القانوني |
| بناء هيكل الأقسام | مسؤول التنظيم | هيكل شجري متعدد المستويات |
| بناء مراكز التكلفة والربح | مسؤول التنظيم | مرتبطة بالفرع أو الكيان |
| تعريف مواقع العمل | مسؤول التنظيم | مرتبطة بالفرع |

#### الصعوبات الحالية

| # | الصعوبة |
|---|---|
| 1 | غياب مرجعية تنظيمية موحدة يجعل كل موديول يُعرّف كياناته الخاصة مما يُفضي إلى تضارب البيانات |
| 2 | صعوبة تطبيق الصلاحيات على مستوى الفرع بدون كيان مرجعي مركزي |
| 3 | تكرار بيانات الهيكل التنظيمي في أنظمة متعددة يُعيق الاتساق |

#### النظام المقترح وفوائده

| # | الفائدة |
|---|---|
| 1 | مرجعية تنظيمية واحدة تُستهلك من كل موديولات المنصة عبر HARD-FK موثّق |
| 2 | تطبيق DataScope مركزي على مستوى الفرع عبر موديول Security |
| 3 | رموز أعمال ثابتة وغير قابلة للتغيير تضمن سلامة التتبع التاريخي |
| 4 | هيكل شجري مُدار بقواعد صارمة يمنع الإدخالات الخاطئة (الدوائر، إسناد SUMMARY) |

### ملاحظات عامة

- **ROOT MODULE**: ORG-001 لا يملك أي XM dependencies صادرة — صفر تبعيات خارجية
- **AQ-003 DEFERRED (غير مانع)**: تأثير إلغاء تفعيل Region على المستهلكين عبر SOFT-READ — يُحل تلقائياً عند تشغيل أول موديول مستهلك في MODE 1.5
- **TENANT_ID**: أُزيل من النظام بالكامل بتاريخ 2026-06-21 (Conflict #17 CLOSED) — لا يوجد أي سياق متعدد المستأجرين
- **DB_TARGET = POSTGRESQL_16**: جميع أنواع البيانات في هذه الوثيقة تتبع PG syntax وفق CORE-8
- **ORG_REGION_TYPE**: جدول مرجعي منفصل (> 15 قيمة — Reference Table) — ليس MD_LOOKUP_DETAIL — يُستهلك عبر FK مباشر

---

## A3 — الكيانات والحقول (Entities & Fields)

> **DB_TARGET = POSTGRESQL_16**: جميع أنواع البيانات تتبع:
> BIGINT (PKs/FKs) | VARCHAR(N) | SMALLINT DEFAULT 1 (flags) | TIMESTAMP (audit) | NUMERIC(N,N) (decimal)

---

### ENTITY-ORG-001 — الكيان القانوني (LegalEntity)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **DB Table** | ORG_LEGAL_ENTITY |
| **Business Code** | YES — Format: `LE-NNNNN` (global unique) |
| **المصدر** | master-registry v2.7.4 Section 5 + module-registry-ORG.md |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | مستهلَك من: Security (1.2), MasterData (1.4), CurrencyCalendar (1.5), ProfitCenter, Region, جميع Layer-3 |

#### حقول الكيان

| اسم الحقل (PG snake_case) | نوع البيانات | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| legal_entity_pk | BIGINT (PK) | نظام | SEQ: ORG_LEGAL_ENTITY_SEQ | رقم إنشائي تلقائي | المعرف | ID |
| legal_entity_code | VARCHAR(20) | نظام | NumberingEngine | يُنشأ تلقائياً — Read-Only بعد الحفظ — LE-NNNNN | الرمز | Code |
| name_ar | VARCHAR(200) | نعم | — | الاسم بالعربي | الاسم بالعربي | Name (Arabic) |
| name_en | VARCHAR(100) | نعم | — | الاسم بالإنجليزي | الاسم بالإنجليزي | Name (English) |
| entity_type_id | VARCHAR(50) | نعم | LOV-ORG-001 | lookupKey: LEGAL_ENTITY_TYPE — يُخزَّن code | نوع الكيان | Entity Type |
| is_active_fl | SMALLINT | نظام | DEFAULT 1 | 1=نشط، 0=غير نشط | نشط | Active |
| created_by | VARCHAR(255) | نظام | — | AuditEntityListener | أنشئ بواسطة | Created By |
| created_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ الإنشاء | Created At |
| updated_by | VARCHAR(255) | نظام | — | AuditEntityListener | عُدِّل بواسطة | Updated By |
| updated_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ التعديل | Updated At |
| notes | VARCHAR(2000) | لا | — | ملاحظات | ملاحظات | Notes |

---

### ENTITY-ORG-002 — الفرع (Branch)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **DB Table** | ORG_BRANCH |
| **Business Code** | YES — Format: `BR-[LE_CODE]-NNNNN` (unique per LegalEntity) |
| **المصدر** | master-registry v2.7.4 Section 5 + module-registry-ORG.md |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | حدود DataScope الرئيسية — مستهلَك من جميع الموديولات |

#### حقول الكيان

| اسم الحقل (PG snake_case) | نوع البيانات | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| branch_pk | BIGINT (PK) | نظام | SEQ: ORG_BRANCH_SEQ | رقم إنشائي تلقائي | المعرف | ID |
| branch_code | VARCHAR(20) | نظام | NumberingEngine | يُنشأ تلقائياً — Read-Only — فريد ضمن الكيان القانوني | الرمز | Code |
| name_ar | VARCHAR(200) | نعم | — | الاسم بالعربي | الاسم بالعربي | Name (Arabic) |
| name_en | VARCHAR(100) | نعم | — | الاسم بالإنجليزي | الاسم بالإنجليزي | Name (English) |
| legal_entity_fk | BIGINT (FK) | نعم | ENTITY-ORG-001 → ORG_LEGAL_ENTITY | NOT NULL — RESTRICT | الكيان القانوني | Legal Entity |
| branch_type_id | VARCHAR(50) | نعم | LOV-ORG-002 | lookupKey: BRANCH_TYPE — يُخزَّن code | نوع الفرع | Branch Type |
| is_active_fl | SMALLINT | نظام | DEFAULT 1 | 1=نشط، 0=غير نشط | نشط | Active |
| created_by | VARCHAR(255) | نظام | — | AuditEntityListener | أنشئ بواسطة | Created By |
| created_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ الإنشاء | Created At |
| updated_by | VARCHAR(255) | نظام | — | AuditEntityListener | عُدِّل بواسطة | Updated By |
| updated_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ التعديل | Updated At |
| notes | VARCHAR(2000) | لا | — | ملاحظات | ملاحظات | Notes |

---

### ENTITY-ORG-003 — المنطقة (Region)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **DB Table** | ORG_REGION |
| **Business Code** | YES — Format: `RG-[LE_CODE]-NNNNN` (unique per LegalEntity) |
| **المصدر** | master-registry v2.7.4 Section 5 + module-registry-ORG.md |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | SOFT-READ محتمل من موديولات أخرى — AQ-003 DEFERRED |

#### حقول الكيان

| اسم الحقل (PG snake_case) | نوع البيانات | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| region_pk | BIGINT (PK) | نظام | SEQ: ORG_REGION_SEQ | رقم إنشائي تلقائي | المعرف | ID |
| region_code | VARCHAR(20) | نظام | NumberingEngine | يُنشأ تلقائياً — Read-Only — فريد ضمن الكيان القانوني | الرمز | Code |
| name_ar | VARCHAR(200) | نعم | — | الاسم بالعربي | الاسم بالعربي | Name (Arabic) |
| name_en | VARCHAR(100) | نعم | — | الاسم بالإنجليزي | الاسم بالإنجليزي | Name (English) |
| legal_entity_fk | BIGINT (FK) | نعم | ENTITY-ORG-001 → ORG_LEGAL_ENTITY | NOT NULL — RESTRICT | الكيان القانوني | Legal Entity |
| region_type_id_fk | BIGINT (FK) | نعم | ENTITY-ORG-008 → ORG_REGION_TYPE | NOT NULL — RESTRICT | نوع المنطقة | Region Type |
| is_active_fl | SMALLINT | نظام | DEFAULT 1 | 1=نشط، 0=غير نشط | نشط | Active |
| created_by | VARCHAR(255) | نظام | — | AuditEntityListener | أنشئ بواسطة | Created By |
| created_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ الإنشاء | Created At |
| updated_by | VARCHAR(255) | نظام | — | AuditEntityListener | عُدِّل بواسطة | Updated By |
| updated_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ التعديل | Updated At |
| notes | VARCHAR(2000) | لا | — | ملاحظات | ملاحظات | Notes |

---

### ENTITY-ORG-004 — القسم (Department)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **DB Table** | ORG_DEPARTMENT |
| **Business Code** | YES — Format: `DEP-[BR_CODE]-NNNNN` (unique per Branch) |
| **المصدر** | master-registry v2.7.4 Section 5 + module-registry-ORG.md |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | مستهلَك من جميع Layer-3 modules عبر HARD-FK |
| **الهيكل** | شجري — self-reference (parent_department_fk NULLABLE) |

#### حقول الكيان

| اسم الحقل (PG snake_case) | نوع البيانات | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| department_pk | BIGINT (PK) | نظام | SEQ: ORG_DEPARTMENT_SEQ | رقم إنشائي تلقائي | المعرف | ID |
| department_code | VARCHAR(20) | نظام | NumberingEngine | يُنشأ تلقائياً — Read-Only — فريد ضمن الفرع | الرمز | Code |
| name_ar | VARCHAR(200) | نعم | — | الاسم بالعربي | الاسم بالعربي | Name (Arabic) |
| name_en | VARCHAR(100) | نعم | — | الاسم بالإنجليزي | الاسم بالإنجليزي | Name (English) |
| branch_fk | BIGINT (FK) | نعم | ENTITY-ORG-002 → ORG_BRANCH | NOT NULL — RESTRICT | الفرع | Branch |
| parent_department_fk | BIGINT (FK) | لا | ENTITY-ORG-004 → ORG_DEPARTMENT (self) | NULLABLE — RESTRICT | القسم الأب | Parent Department |
| node_type_id | VARCHAR(50) | نعم | LOV-ORG-003 | lookupKey: DEPARTMENT_NODE_TYPE — SUMMARY / DETAIL | نوع العقدة | Node Type |
| is_active_fl | SMALLINT | نظام | DEFAULT 1 | 1=نشط، 0=غير نشط | نشط | Active |
| created_by | VARCHAR(255) | نظام | — | AuditEntityListener | أنشئ بواسطة | Created By |
| created_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ الإنشاء | Created At |
| updated_by | VARCHAR(255) | نظام | — | AuditEntityListener | عُدِّل بواسطة | Updated By |
| updated_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ التعديل | Updated At |
| notes | VARCHAR(2000) | لا | — | ملاحظات | ملاحظات | Notes |

---

### ENTITY-ORG-005 — مركز التكلفة (CostCenter)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **DB Table** | ORG_COST_CENTER |
| **Business Code** | YES — Format: `CC-[BR_CODE]-NNNNN` (unique per Branch) |
| **المصدر** | master-registry v2.7.4 Section 5 + module-registry-ORG.md |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | مستهلَك من Layer-2 و Layer-3 جميعاً عبر HARD-FK — Finance (3.4) مستهلك رئيسي |
| **الهيكل** | شجري — self-reference (parent_cost_center_fk NULLABLE) |

#### حقول الكيان

| اسم الحقل (PG snake_case) | نوع البيانات | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| cost_center_pk | BIGINT (PK) | نظام | SEQ: ORG_COST_CENTER_SEQ | رقم إنشائي تلقائي | المعرف | ID |
| cost_center_code | VARCHAR(20) | نظام | NumberingEngine | يُنشأ تلقائياً — Read-Only — فريد ضمن الفرع | الرمز | Code |
| name_ar | VARCHAR(200) | نعم | — | الاسم بالعربي | الاسم بالعربي | Name (Arabic) |
| name_en | VARCHAR(100) | نعم | — | الاسم بالإنجليزي | الاسم بالإنجليزي | Name (English) |
| branch_fk | BIGINT (FK) | نعم | ENTITY-ORG-002 → ORG_BRANCH | NOT NULL — RESTRICT | الفرع | Branch |
| parent_cost_center_fk | BIGINT (FK) | لا | ENTITY-ORG-005 → ORG_COST_CENTER (self) | NULLABLE — RESTRICT | مركز التكلفة الأب | Parent Cost Center |
| node_type_id | VARCHAR(50) | نعم | LOV-ORG-004 | lookupKey: COST_CENTER_NODE_TYPE — SUMMARY / DETAIL | نوع العقدة | Node Type |
| cost_center_type_id | VARCHAR(50) | نعم | LOV-ORG-005 | lookupKey: COST_CENTER_TYPE — Direct / Indirect / Shared | نوع مركز التكلفة | Cost Center Type |
| is_active_fl | SMALLINT | نظام | DEFAULT 1 | 1=نشط، 0=غير نشط | نشط | Active |
| created_by | VARCHAR(255) | نظام | — | AuditEntityListener | أنشئ بواسطة | Created By |
| created_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ الإنشاء | Created At |
| updated_by | VARCHAR(255) | نظام | — | AuditEntityListener | عُدِّل بواسطة | Updated By |
| updated_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ التعديل | Updated At |
| notes | VARCHAR(2000) | لا | — | ملاحظات | ملاحظات | Notes |

---

### ENTITY-ORG-006 — مركز الربح (ProfitCenter)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **DB Table** | ORG_PROFIT_CENTER |
| **Business Code** | YES — Format: `PC-[LE_CODE]-NNNNN` (unique per LegalEntity) |
| **المصدر** | master-registry v2.7.4 Section 5 + module-registry-ORG.md |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | مستهلَك من Finance (3.4) وLayer-3 جميعاً عبر HARD-FK |
| **ملاحظة معمارية** | ProfitCenter تملكه Organization — Finance يستهلكه عبر HARD-FK (قرار CLOSED) |

#### حقول الكيان

| اسم الحقل (PG snake_case) | نوع البيانات | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| profit_center_pk | BIGINT (PK) | نظام | SEQ: ORG_PROFIT_CENTER_SEQ | رقم إنشائي تلقائي | المعرف | ID |
| profit_center_code | VARCHAR(20) | نظام | NumberingEngine | يُنشأ تلقائياً — Read-Only — فريد ضمن الكيان القانوني | الرمز | Code |
| name_ar | VARCHAR(200) | نعم | — | الاسم بالعربي | الاسم بالعربي | Name (Arabic) |
| name_en | VARCHAR(100) | نعم | — | الاسم بالإنجليزي | الاسم بالإنجليزي | Name (English) |
| legal_entity_fk | BIGINT (FK) | نعم | ENTITY-ORG-001 → ORG_LEGAL_ENTITY | NOT NULL — RESTRICT | الكيان القانوني | Legal Entity |
| is_active_fl | SMALLINT | نظام | DEFAULT 1 | 1=نشط، 0=غير نشط | نشط | Active |
| created_by | VARCHAR(255) | نظام | — | AuditEntityListener | أنشئ بواسطة | Created By |
| created_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ الإنشاء | Created At |
| updated_by | VARCHAR(255) | نظام | — | AuditEntityListener | عُدِّل بواسطة | Updated By |
| updated_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ التعديل | Updated At |
| notes | VARCHAR(2000) | لا | — | ملاحظات | ملاحظات | Notes |

---

### ENTITY-ORG-007 — موقع العمل (LocationSite)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **DB Table** | ORG_LOCATION_SITE |
| **Business Code** | YES — Format: `LS-[BR_CODE]-NNNNN` (unique per Branch) |
| **المصدر** | master-registry v2.7.4 Section 5 + module-registry-ORG.md |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | مستهلَك من Inventory (3.2) عبر HARD-FK — مفهوم الموقع المادي L1 |

#### حقول الكيان

| اسم الحقل (PG snake_case) | نوع البيانات | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| location_site_pk | BIGINT (PK) | نظام | SEQ: ORG_LOCATION_SITE_SEQ | رقم إنشائي تلقائي | المعرف | ID |
| location_site_code | VARCHAR(20) | نظام | NumberingEngine | يُنشأ تلقائياً — Read-Only — فريد ضمن الفرع | الرمز | Code |
| name_ar | VARCHAR(200) | نعم | — | الاسم بالعربي | الاسم بالعربي | Name (Arabic) |
| name_en | VARCHAR(100) | نعم | — | الاسم بالإنجليزي | الاسم بالإنجليزي | Name (English) |
| branch_fk | BIGINT (FK) | نعم | ENTITY-ORG-002 → ORG_BRANCH | NOT NULL — RESTRICT | الفرع | Branch |
| site_type_id | VARCHAR(50) | نعم | LOV-ORG-006 | lookupKey: LOCATION_SITE_TYPE — يُخزَّن code | نوع الموقع | Site Type |
| is_active_fl | SMALLINT | نظام | DEFAULT 1 | 1=نشط، 0=غير نشط | نشط | Active |
| created_by | VARCHAR(255) | نظام | — | AuditEntityListener | أنشئ بواسطة | Created By |
| created_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ الإنشاء | Created At |
| updated_by | VARCHAR(255) | نظام | — | AuditEntityListener | عُدِّل بواسطة | Updated By |
| updated_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ التعديل | Updated At |
| notes | VARCHAR(2000) | لا | — | ملاحظات | ملاحظات | Notes |

---

### ENTITY-ORG-008 — نوع المنطقة (RegionType) — PRIVATE Reference Table

| البند | القيمة |
|---|---|
| **النوع** | PRIVATE — Reference Table (مملوك بالكامل لـ Organization) |
| **DB Table** | ORG_REGION_TYPE |
| **Business Code** | لا — Reference Table |
| **المصدر** | master-registry v2.7.4 Section 5 + business-policies-ORG.md |
| **العمليات** | Create, Read, Update (Admin only) |
| **ملاحظة** | > 15 قيمة محتملة → Reference Table مستقل (ليس MD_LOOKUP_DETAIL) — قابل للتوسيع |

#### حقول الكيان

| اسم الحقل (PG snake_case) | نوع البيانات | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| region_type_pk | BIGINT (PK) | نظام | SEQ: ORG_REGION_TYPE_SEQ | رقم إنشائي تلقائي | المعرف | ID |
| region_type_code | VARCHAR(30) | نعم | — | فريد — GEOGRAPHIC / SALES / OPERATIONAL ... | الرمز | Code |
| name_ar | VARCHAR(200) | نعم | — | الاسم بالعربي | الاسم بالعربي | Name (Arabic) |
| name_en | VARCHAR(100) | نعم | — | الاسم بالإنجليزي | الاسم بالإنجليزي | Name (English) |
| is_active_fl | SMALLINT | نظام | DEFAULT 1 | 1=نشط، 0=غير نشط | نشط | Active |
| created_by | VARCHAR(255) | نظام | — | AuditEntityListener | أنشئ بواسطة | Created By |
| created_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ الإنشاء | Created At |
| updated_by | VARCHAR(255) | نظام | — | AuditEntityListener | عُدِّل بواسطة | Updated By |
| updated_at | TIMESTAMP | نظام | — | AuditEntityListener | تاريخ التعديل | Updated At |

---

## A4 — قواعد التحقق (Business Rules)

> **قاعدة إلزامية:** هذا القسم هو المصدر الوحيد لتعريف القواعد.
> PART B يُشير للقواعد بـ RULE-ID فقط — لا يُعيد تعريفها.

---

### RULE-ORG-001 — منع إلغاء تفعيل الكيان القانوني — فروع نشطة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-001 |
| **Trigger** | عند محاولة إلغاء تفعيل Deactivate LegalEntity |
| **Statement** | The system MUST prevent deactivation of a LegalEntity when one or more active Branches reference it |
| **Message-AR** | لا يمكن إلغاء تفعيل الكيان القانوني لوجود فروع نشطة مرتبطة به |
| **Message-EN** | Cannot deactivate Legal Entity: active branches exist |
| **Source** | RULE-ORG-01 (module-registry-ORG.md) |

---

### RULE-ORG-002 — منع إلغاء تفعيل الكيان القانوني — مراكز ربح نشطة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-001 |
| **Trigger** | عند محاولة إلغاء تفعيل Deactivate LegalEntity |
| **Statement** | The system MUST prevent deactivation of a LegalEntity when one or more active ProfitCenters reference it |
| **Message-AR** | لا يمكن إلغاء تفعيل الكيان القانوني لوجود مراكز ربح نشطة مرتبطة به |
| **Message-EN** | Cannot deactivate Legal Entity: active profit centers exist |
| **Source** | RULE-ORG-02 (module-registry-ORG.md) |

---

### RULE-ORG-003 — منع إلغاء تفعيل الفرع — أقسام نشطة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-002 |
| **Trigger** | عند محاولة إلغاء تفعيل Deactivate Branch |
| **Statement** | The system MUST prevent deactivation of a Branch when one or more active Departments reference it |
| **Message-AR** | لا يمكن إلغاء تفعيل الفرع لوجود أقسام نشطة مرتبطة به |
| **Message-EN** | Cannot deactivate Branch: active departments exist |
| **Source** | RULE-ORG-03 (module-registry-ORG.md) |

---

### RULE-ORG-004 — منع إلغاء تفعيل الفرع — مراكز تكلفة نشطة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-002 |
| **Trigger** | عند محاولة إلغاء تفعيل Deactivate Branch |
| **Statement** | The system MUST prevent deactivation of a Branch when one or more active CostCenters reference it |
| **Message-AR** | لا يمكن إلغاء تفعيل الفرع لوجود مراكز تكلفة نشطة مرتبطة به |
| **Message-EN** | Cannot deactivate Branch: active cost centers exist |
| **Source** | RULE-ORG-04 (module-registry-ORG.md) |

---

### RULE-ORG-005 — منع إلغاء تفعيل الفرع — مواقع عمل نشطة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-002 |
| **Trigger** | عند محاولة إلغاء تفعيل Deactivate Branch |
| **Statement** | The system MUST prevent deactivation of a Branch when one or more active LocationSites reference it |
| **Message-AR** | لا يمكن إلغاء تفعيل الفرع لوجود مواقع عمل نشطة مرتبطة به |
| **Message-EN** | Cannot deactivate Branch: active location sites exist |
| **Source** | RULE-ORG-05 (module-registry-ORG.md) |

---

### RULE-ORG-006 — منع إلغاء تفعيل المنطقة — فروع مرتبطة نشطة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-003 |
| **Trigger** | عند محاولة إلغاء تفعيل Deactivate Region |
| **Statement** | The system MUST prevent deactivation of a Region when one or more active Branches reference it |
| **Message-AR** | لا يمكن إلغاء تفعيل المنطقة لوجود فروع نشطة مرتبطة بها |
| **Message-EN** | Cannot deactivate Region: active branches reference it |
| **Source** | RULE-ORG-06 (module-registry-ORG.md) |
| **Test-Hint** | تحقق من الفروع النشطة فقط (is_active_fl=1) — لا يُمنع إذا كانت الفروع المرتبطة غير نشطة |

---

### RULE-ORG-007 — منع الدورة في هيكل الأقسام

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-004 |
| **Trigger** | عند إنشاء أو تعديل parent_department_fk |
| **Statement** | The system MUST prevent assignment of a parent Department that would create a circular reference in the Department tree |
| **Message-AR** | لا يمكن تعيين هذا القسم كقسم أب — سيؤدي إلى دورة في هيكل الأقسام |
| **Message-EN** | Cannot set parent department: circular reference detected |
| **Source** | RULE-ORG-07 (module-registry-ORG.md) |

---

### RULE-ORG-008 — منع الدورة في هيكل مراكز التكلفة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-005 |
| **Trigger** | عند إنشاء أو تعديل parent_cost_center_fk |
| **Statement** | The system MUST prevent assignment of a parent CostCenter that would create a circular reference in the CostCenter tree |
| **Message-AR** | لا يمكن تعيين مركز التكلفة هذا كأب — سيؤدي إلى دورة في هيكل مراكز التكلفة |
| **Message-EN** | Cannot set parent cost center: circular reference detected |
| **Source** | RULE-ORG-08 (module-registry-ORG.md) |

---

### RULE-ORG-009 — منع استخدام قسم SUMMARY في السجلات التشغيلية

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-004 |
| **Trigger** | عند محاولة ربط قسم بسجل تشغيلي في أي موديول مستهلك |
| **Statement** | The system MUST prevent direct assignment of a SUMMARY-type Department to any transactional record |
| **Message-AR** | لا يمكن استخدام قسم من نوع (ملخص) في السجلات التشغيلية — يُسمح فقط بأقسام من نوع (تفصيل) |
| **Message-EN** | Cannot assign a SUMMARY department to transactional records — only DETAIL departments are permitted |
| **Source** | RULE-ORG-09 (module-registry-ORG.md) |
| **Test-Hint** | هذه القاعدة مُطبَّقة في طبقة التطبيق لدى الموديولات المستهلكة — ليس في Organization مباشرة |

---

### RULE-ORG-010 — منع استخدام مركز تكلفة SUMMARY في السجلات التشغيلية

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-005 |
| **Trigger** | عند محاولة ربط مركز تكلفة بسجل تشغيلي في أي موديول مستهلك |
| **Statement** | The system MUST prevent direct assignment of a SUMMARY-type CostCenter to any transactional record |
| **Message-AR** | لا يمكن استخدام مركز تكلفة من نوع (ملخص) في السجلات التشغيلية — يُسمح فقط بمراكز من نوع (تفصيل) |
| **Message-EN** | Cannot assign a SUMMARY cost center to transactional records — only DETAIL cost centers are permitted |
| **Source** | RULE-ORG-10 (module-registry-ORG.md) |
| **Test-Hint** | هذه القاعدة مُطبَّقة في طبقة التطبيق لدى الموديولات المستهلكة — ليس في Organization مباشرة |

---

### RULE-ORG-011 — ثبات رمز الأعمال بعد الحفظ

| البند | القيمة |
|---|---|
| **Scope** | جميع الكيانات: ENTITY-ORG-001 إلى ENTITY-ORG-007 |
| **Trigger** | عند محاولة تعديل حقل الرمز (Business Code) لأي كيان |
| **Statement** | The system MUST prevent modification of the Business Code field after the record has been saved for the first time |
| **Message-AR** | رمز الأعمال لا يمكن تعديله بعد الحفظ الأول — هذا الحقل محمي ونهائي |
| **Message-EN** | Business Code is immutable after first save and cannot be modified |
| **Source** | RULE-ORG-11 / BC-RULE-4 (module-registry-ORG.md + Section 5.5.5) |

---

### RULE-ORG-012 — فريدية رمز الأعمال ضمن النطاق المحدد

| البند | القيمة |
|---|---|
| **Scope** | جميع الكيانات: ENTITY-ORG-001 إلى ENTITY-ORG-007 |
| **Trigger** | عند إنشاء كيان جديد — NumberingEngine يتحقق أوتوماتياً |
| **Statement** | The system MUST ensure the Business Code generated by NumberingEngine is globally unique within its defined scope (LegalEntity: global; Branch/Department/CostCenter/LocationSite: per LegalEntity or Branch; ProfitCenter/Region: per LegalEntity) |
| **Message-AR** | تعذّر إنشاء رمز الأعمال — تعارض في التسلسل. يرجى المحاولة مرة أخرى |
| **Message-EN** | Business Code generation failed due to sequence conflict. Please retry |
| **Source** | RULE-ORG-12 (module-registry-ORG.md) |

---

### RULE-ORG-013 — توليد رمز الأعمال عبر NumberingEngine حصراً

| البند | القيمة |
|---|---|
| **Scope** | جميع الكيانات: ENTITY-ORG-001 إلى ENTITY-ORG-007 |
| **Trigger** | عند الحفظ الأول لأي كيان |
| **Statement** | The system MUST generate the Business Code exclusively through NumberingEngine — no module may implement its own numbering logic |
| **Message-AR** | يجب إنشاء رمز الأعمال عبر محرك الترقيم المركزي فقط |
| **Message-EN** | Business Code must be generated via NumberingEngine only |
| **Source** | BC-RULE-6 / RULE-ORG-016 (Section 5.5.5 + module-registry-ORG.md) |

---

### RULE-ORG-014 — منع تعديل الرمز في عملية التحديث

| البند | القيمة |
|---|---|
| **Scope** | جميع الكيانات: ENTITY-ORG-001 إلى ENTITY-ORG-007 |
| **Trigger** | عند استقبال طلب PUT — فحص الحقول المُرسَلة |
| **Statement** | The system MUST reject any Update request that includes the Business Code field in its payload — the field must be excluded from Update DTOs |
| **Message-AR** | رمز الأعمال لا يُقبل ضمن طلبات التعديل |
| **Message-EN** | Business Code field is not accepted in update requests |
| **Source** | BC-RULE-4 (Section 5.5.5) |

---

### RULE-ORG-015 — فريدية الاسم ضمن النطاق الأب

| البند | القيمة |
|---|---|
| **Scope** | جميع الكيانات: ENTITY-ORG-001 إلى ENTITY-ORG-007 |
| **Trigger** | عند الإنشاء أو التعديل |
| **Statement** | The system MUST prevent saving a record whose name_ar or name_en duplicates an existing active record within the same parent scope (e.g., same LegalEntity for Branches, same Branch for Departments) |
| **Message-AR** | الاسم مُستخدم مسبقاً ضمن نفس النطاق — يرجى اختيار اسم مختلف |
| **Message-EN** | Name already exists within the same parent scope — please choose a different name |
| **Source** | ARCH-5 (Section 5.5.3) |

---

### RULE-ORG-016 — منع تعديل حقول Audit

| البند | القيمة |
|---|---|
| **Scope** | جميع الكيانات: ENTITY-ORG-001 إلى ENTITY-ORG-007 |
| **Trigger** | عند استقبال أي طلب Create أو Update |
| **Statement** | The system MUST reject any request payload that includes audit fields (created_by, created_at, updated_by, updated_at) — these are populated exclusively by AuditEntityListener |
| **Message-AR** | حقول التدقيق لا تُقبل من المستخدم — يملؤها النظام تلقائياً |
| **Message-EN** | Audit fields are not accepted in request payloads — populated by system only |
| **Source** | Section 5.4.2 — AuditEntityListener pattern |

---

### RULE-ORG-017 — منع إلغاء تفعيل الفرع لوجود region_fk

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-003 |
| **Trigger** | عند محاولة إلغاء تفعيل Region |
| **Statement** | The system MUST check for consuming modules referencing an active Region record via SOFT-READ before allowing deactivation, and must surface appropriate warnings if any are detected |
| **Message-AR** | تحذير: المنطقة مُستخدمة من موديولات أخرى — تأكد من مراجعة الأثر قبل إلغاء التفعيل |
| **Message-EN** | Warning: Region is referenced by other modules — review impact before deactivating |
| **Source** | ARCH-8 auto-raise — AQ-003 / OQ-001 |

---

### RULE-ORG-018 — فرع يجب أن ينتمي لكيان قانوني نشط

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-002 |
| **Trigger** | عند إنشاء Branch جديد |
| **Statement** | The system MUST prevent creation of a Branch under an inactive LegalEntity |
| **Message-AR** | لا يمكن إنشاء فرع تحت كيان قانوني غير نشط |
| **Message-EN** | Cannot create a Branch under an inactive Legal Entity |
| **Source** | Business integrity rule — ARCH-4 |

---

### RULE-ORG-019 — قسم / مركز تكلفة / موقع تحت فرع نشط فقط

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-004, ENTITY-ORG-005, ENTITY-ORG-007 |
| **Trigger** | عند إنشاء Department أو CostCenter أو LocationSite |
| **Statement** | The system MUST prevent creation of a Department, CostCenter, or LocationSite under an inactive Branch |
| **Message-AR** | لا يمكن إنشاء قسم أو مركز تكلفة أو موقع عمل تحت فرع غير نشط |
| **Message-EN** | Cannot create organizational unit under an inactive Branch |
| **Source** | Business integrity rule — ARCH-4 |

---

### RULE-ORG-020 — حقل node_type_id ثابت بعد الحفظ للأقسام

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-004, ENTITY-ORG-005 |
| **Trigger** | عند محاولة تعديل node_type_id بعد الحفظ الأول |
| **Statement** | The system MUST prevent modification of the node_type_id (SUMMARY / DETAIL) after a Department or CostCenter record has been saved, as this would corrupt the tree integrity |
| **Message-AR** | لا يمكن تغيير نوع العقدة (ملخص/تفصيل) بعد الحفظ |
| **Message-EN** | Node type (SUMMARY/DETAIL) cannot be changed after initial save |
| **Source** | Tree integrity rule — ARCH-4 |

---

## A5 — قوائم القيم (LOV / Lookup)

> **قاعدة إلزامية:** هذا القسم هو المصدر الوحيد لتعريف LOVs.
> PART B يُشير للـ LOVs بـ LOV-ID أو lookupKey فقط — لا يُعيد تعريفها.
> **جميع LOVs تُستهلك عبر:** GET /api/lookups/{lookup_key}?active=true
> **القيمة المُخزَّنة:** code من MD_LOOKUP_DETAIL (ليس id)

---

### LOV-ORG-001 — نوع الكيان القانوني

| البند | القيمة |
|---|---|
| **الحقل** | ENTITY-ORG-001.entity_type_id |
| **نوع التحكم** | Dropdown (≤ 15 — قيم ثابتة محدودة) |
| **lookupKey** | LEGAL_ENTITY_TYPE |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Organization |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| HEAD_OFFICE | المقر الرئيسي | Head Office |
| BRANCH_OFFICE | مكتب فرعي | Branch Office |
| SUBSIDIARY | شركة تابعة | Subsidiary |
| REPRESENTATIVE_OFFICE | مكتب تمثيلي | Representative Office |

---

### LOV-ORG-002 — نوع الفرع

| البند | القيمة |
|---|---|
| **الحقل** | ENTITY-ORG-002.branch_type_id |
| **نوع التحكم** | Dropdown (≤ 15) |
| **lookupKey** | BRANCH_TYPE |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Organization |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| MAIN_BRANCH | فرع رئيسي | Main Branch |
| SUB_BRANCH | فرع فرعي | Sub-Branch |
| OPERATIONS_BRANCH | فرع العمليات | Operations Branch |
| ADMIN_BRANCH | فرع إداري | Admin Branch |

---

### LOV-ORG-003 — نوع عقدة القسم

| البند | القيمة |
|---|---|
| **الحقل** | ENTITY-ORG-004.node_type_id |
| **نوع التحكم** | Dropdown (2 قيم ثابتة) |
| **lookupKey** | DEPARTMENT_NODE_TYPE |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Organization |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| SUMMARY | ملخص | Summary |
| DETAIL | تفصيل | Detail |

---

### LOV-ORG-004 — نوع عقدة مركز التكلفة

| البند | القيمة |
|---|---|
| **الحقل** | ENTITY-ORG-005.node_type_id |
| **نوع التحكم** | Dropdown (2 قيم ثابتة) |
| **lookupKey** | COST_CENTER_NODE_TYPE |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Organization |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| SUMMARY | ملخص | Summary |
| DETAIL | تفصيل | Detail |

---

### LOV-ORG-005 — نوع مركز التكلفة

| البند | القيمة |
|---|---|
| **الحقل** | ENTITY-ORG-005.cost_center_type_id |
| **نوع التحكم** | Dropdown (≤ 15) |
| **lookupKey** | COST_CENTER_TYPE |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Organization |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| DIRECT | مباشر | Direct |
| INDIRECT | غير مباشر | Indirect |
| SHARED | مشترك | Shared |

---

### LOV-ORG-006 — نوع موقع العمل

| البند | القيمة |
|---|---|
| **الحقل** | ENTITY-ORG-007.site_type_id |
| **نوع التحكم** | Dropdown (≤ 15) |
| **lookupKey** | LOCATION_SITE_TYPE |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Organization |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| OFFICE | مكتب | Office |
| WAREHOUSE | مستودع | Warehouse |
| FACTORY | مصنع | Factory |
| SITE | موقع | Site |
| RETAIL | متجر | Retail |

---

## A6 — دورة الحالة (Status Lifecycle)

> جميع كيانات ORG-001 (ENTITY-ORG-001 إلى ENTITY-ORG-007) تستخدم:
> **is_active_fl** فقط — حالتان: نشط (1) ↔ غير نشط (0)
> لا workflow — لا approval flow — لا محطات وسيطة
> إلغاء التفعيل محكوم بقواعد RULE-ORG-001 إلى RULE-ORG-006

```
[نشط: is_active_fl=1] ◄──────────────► [غير نشط: is_active_fl=0]
       ▲                                         │
       └────── إعادة التفعيل (Update PUT) ────────┘

قيود إلغاء التفعيل (مُطبَّقة بطبقة الخدمة):
  LegalEntity  : مقيّد إذا وجدت Branches أو ProfitCenters نشطة
  Branch       : مقيّد إذا وجدت Departments أو CostCenters أو LocationSites نشطة
  Region       : مقيّد إذا وجدت Branches نشطة مرتبطة
  Department   : مقيّد إذا وجدت أقسام فرعية نشطة
  CostCenter   : مقيّد إذا وجدت مراكز تكلفة فرعية نشطة
  ProfitCenter : بدون قيود تبعية داخلية
  LocationSite : بدون قيود تبعية داخلية
```

---

## A7 — تبعيات الموديولات (Module Dependencies)

> ORG-001 هو ROOT MODULE — لا تبعيات صادرة.
> التبعيات الواردة (من موديولات أخرى) موثّقة في module-registry-ORG.md OUTGOING.

### الكيانات المُستهلَكة من موديولات أخرى

| الكيان | نوع الاعتمادية | XM Candidate |
|---|---|---|
| لا توجد — Organization لا تستهلك كيانات خارجية | — | — |

### الخدمات والتكاملات الخارجية

| الخدمة | الغرض | نوع التكامل |
|---|---|---|
| NumberingEngine (1.6) | توليد Business Codes لجميع كيانات ORG | REST API (POST /api/numbering/generate) |
| Security Engine | SEC_PAGES seeding — توليد PERM_* تلقائياً من PAGE_CODE | SEC-3 Declaration |
| AuditEntityListener | ملء حقول الـ Audit تلقائياً عند الحفظ | Spring Listener (داخلي) |

---

# ══════════════════════════════════════════════════════════
# PART B — SCREEN SPECIFICATIONS
# One block per SCR-ID — self-contained for P3 execution
# References PART A by ID — never redefines artifacts
# ══════════════════════════════════════════════════════════

> **قاعدة PART B الإلزامية:**
> كل block يشير لـ PART A بالـ ID فقط.
> أي إعادة كتابة لتفاصيل entity أو rule أو LOV داخل PART B = انتهاك Single Source of Truth.

---

## SCR-ORG-001 — الكيانات القانونية

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-001 |
| **اسم الشاشة** | إدارة الكيانات القانونية |
| **UI Pattern** | PATTERN-1 — Search + Entry |
| **Pattern Reason** | حقول > 8 — كيان رئيسي يُستهلك من جميع الموديولات |
| **SCR-ID Scope** | ONE SCR-ID covers: Search + Entry (CORE-9) |
| **P3 Implication** | Two-screen UX navigation — P3 determines component names in F1 |
| **ENTITY-ID** | ENTITY-ORG-001 |
| **وظيفة الشاشة** | إنشاء وإدارة الكيانات القانونية المسجلة للمنشأة |
| **المستخدمون** | مسؤول النظام، مسؤول التنظيم |
| **الموضع في النظام** | الهيكل التنظيمي ← الكيانات القانونية |
| **روابط من** | القائمة الرئيسية — الهيكل التنظيمي |
| **روابط إلى** | شاشة الفروع (SCR-ORG-002) |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| legal_entity_code | نص | لا | — | بحث يساوي |
| name_ar | نص | لا | — | بحث يحتوي |
| name_en | نص | لا | — | بحث يحتوي |
| entity_type_id | قائمة منسدلة | لا | LOV-ORG-001 → A5 | lookupKey: LEGAL_ENTITY_TYPE |
| is_active_fl | قائمة منسدلة | لا | نشط / غير نشط | القيم: 1 / 0 |

#### نتائج البحث (أعمدة الجدول)

| اسم العمود | Label-AR | Label-EN |
|---|---|---|
| legal_entity_code | الرمز | Code |
| name_ar | الاسم بالعربي | Name (Arabic) |
| name_en | الاسم بالإنجليزي | Name (English) |
| entity_type_id | نوع الكيان | Entity Type |
| is_active_fl | الحالة | Status |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_LEGAL_ENTITY_CREATE |
| Edit | عند تحديد سجل | PERM_LEGAL_ENTITY_UPDATE |
| Deactivate | عند تحديد سجل نشط | PERM_LEGAL_ENTITY_DELETE |
| Activate | عند تحديد سجل غير نشط | PERM_LEGAL_ENTITY_UPDATE |
| Export | دائماً | PERM_LEGAL_ENTITY_VIEW |

#### قواعد البحث المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| — | لا قواعد بحث خاصة | — |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| legal_entity_code | نص (Read-Only) | نظام | ENTITY-ORG-001 → A3 | يُنشأ تلقائياً عند الحفظ — لا يظهر في Create |
| name_ar | نص | نعم | ENTITY-ORG-001 → A3 | |
| name_en | نص | نعم | ENTITY-ORG-001 → A3 | |
| entity_type_id | قائمة منسدلة | نعم | LOV-ORG-001 → A5 | lookupKey: LEGAL_ENTITY_TYPE |
| notes | نص طويل | لا | ENTITY-ORG-001 → A3 | |
| is_active_fl | عرض فقط | نظام | ENTITY-ORG-001 → A3 | يُدار عبر أزرار Activate/Deactivate |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (Create) | POST | RULE-ORG-012, RULE-ORG-013, RULE-ORG-015, RULE-ORG-016 |
| حفظ (Update) | PUT | RULE-ORG-011, RULE-ORG-014, RULE-ORG-015, RULE-ORG-016 |
| إلغاء تفعيل | PUT (is_active_fl=0) | RULE-ORG-001, RULE-ORG-002 |
| تفعيل | PUT (is_active_fl=1) | — |
| إلغاء | navigation back | — |

---

### B4 — الصلاحيات (Permissions)

> **SEC-3 Declaration:** Security Engine يولّد PERM_* تلقائياً من PAGE_CODE.
> SRS تُعلن PAGE_CODE فقط — لا تُدرج INSERT statements صريحة.

| PAGE_CODE | PERM_VIEW | PERM_CREATE | PERM_UPDATE | PERM_DELETE |
|---|---|---|---|---|
| LEGAL_ENTITY | مسؤول النظام، مسؤول التنظيم | مسؤول النظام | مسؤول النظام، مسؤول التنظيم | مسؤول النظام |

> VIEW = gateway: يمنح الوصول للبحث والإدخال (read mode)
> SEC_PAGES: page_code=LEGAL_ENTITY, module=ORGANIZATION, parent_id_fk=[ORG_PARENT]

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-001 | إنشاء كيان قانوني | POST | /api/v1/org/legal-entities | name_ar, name_en, entity_type_id, notes? | LegalEntity كامل | RULE-ORG-012, 013, 015, 016 |
| API-ORG-002 | بحث الكيانات | GET | /api/v1/org/legal-entities | legal_entity_code?, name_ar?, name_en?, entity_type_id?, is_active_fl?, page, size | قائمة LegalEntity | — |
| API-ORG-003 | تعديل كيان قانوني | PUT | /api/v1/org/legal-entities/{id} | name_ar?, name_en?, entity_type_id?, notes? | LegalEntity محدَّث | RULE-ORG-011, 014, 015, 016 |
| API-ORG-004 | إلغاء تفعيل | PUT | /api/v1/org/legal-entities/{id}/deactivate | legal_entity_pk | تأكيد | RULE-ORG-001, 002 |
| API-ORG-005 | تفعيل | PUT | /api/v1/org/legal-entities/{id}/activate | legal_entity_pk | تأكيد | — |
| API-ORG-006 | جلب بالمعرف | GET | /api/v1/org/legal-entities/{id} | legal_entity_pk | LegalEntity كامل | — |

---

## SCR-ORG-002 — الفروع

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-002 |
| **اسم الشاشة** | إدارة الفروع |
| **UI Pattern** | PATTERN-1 — Search + Entry |
| **Pattern Reason** | حقول > 8 — تعتمد على LegalEntity — حدود DataScope الرئيسية |
| **SCR-ID Scope** | ONE SCR-ID covers: Search + Entry (CORE-9) |
| **P3 Implication** | Two-screen UX navigation — P3 determines component names in F1 |
| **ENTITY-ID** | ENTITY-ORG-002 |
| **وظيفة الشاشة** | إنشاء وإدارة فروع المنشأة المرتبطة بالكيانات القانونية |
| **المستخدمون** | مسؤول النظام، مسؤول التنظيم |
| **الموضع في النظام** | الهيكل التنظيمي ← الفروع |
| **روابط من** | الكيانات القانونية (SCR-ORG-001) |
| **روابط إلى** | شاشة الأقسام (SCR-ORG-004)، مراكز التكلفة (SCR-ORG-005)، مواقع العمل (SCR-ORG-007) |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| branch_code | نص | لا | — | بحث يساوي |
| name_ar | نص | لا | — | بحث يحتوي |
| name_en | نص | لا | — | بحث يحتوي |
| legal_entity_fk | LOV | لا | ENTITY-ORG-001 → API-ORG-002 | اختيار كيان قانوني |
| branch_type_id | قائمة منسدلة | لا | LOV-ORG-002 → A5 | lookupKey: BRANCH_TYPE |
| is_active_fl | قائمة منسدلة | لا | نشط / غير نشط | القيم: 1 / 0 |

#### نتائج البحث (أعمدة الجدول)

| اسم العمود | Label-AR | Label-EN |
|---|---|---|
| branch_code | الرمز | Code |
| name_ar | الاسم بالعربي | Name (Arabic) |
| name_en | الاسم بالإنجليزي | Name (English) |
| legal_entity_fk | الكيان القانوني | Legal Entity |
| branch_type_id | نوع الفرع | Branch Type |
| is_active_fl | الحالة | Status |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_BRANCH_CREATE |
| Edit | عند تحديد سجل | PERM_BRANCH_UPDATE |
| Deactivate | عند تحديد سجل نشط | PERM_BRANCH_DELETE |
| Activate | عند تحديد سجل غير نشط | PERM_BRANCH_UPDATE |
| Export | دائماً | PERM_BRANCH_VIEW |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| branch_code | نص (Read-Only) | نظام | ENTITY-ORG-002 → A3 | يُنشأ تلقائياً |
| name_ar | نص | نعم | ENTITY-ORG-002 → A3 | |
| name_en | نص | نعم | ENTITY-ORG-002 → A3 | |
| legal_entity_fk | LOV | نعم | ENTITY-ORG-001 → A3 | يُقيَّد بالكيانات النشطة فقط |
| branch_type_id | قائمة منسدلة | نعم | LOV-ORG-002 → A5 | lookupKey: BRANCH_TYPE |
| notes | نص طويل | لا | ENTITY-ORG-002 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (Create) | POST | RULE-ORG-012, 013, 015, 016, 018 |
| حفظ (Update) | PUT | RULE-ORG-011, 014, 015, 016 |
| إلغاء تفعيل | PUT | RULE-ORG-003, 004, 005 |
| تفعيل | PUT | — |

---

### B4 — الصلاحيات (Permissions)

| PAGE_CODE | PERM_VIEW | PERM_CREATE | PERM_UPDATE | PERM_DELETE |
|---|---|---|---|---|
| BRANCH | مسؤول النظام، مسؤول التنظيم | مسؤول النظام | مسؤول النظام، مسؤول التنظيم | مسؤول النظام |

> SEC_PAGES: page_code=BRANCH, module=ORGANIZATION

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-007 | إنشاء فرع | POST | /api/v1/org/branches | name_ar, name_en, legal_entity_fk, branch_type_id, notes? | Branch كامل | RULE-ORG-012, 013, 015, 016, 018 |
| API-ORG-008 | بحث الفروع | GET | /api/v1/org/branches | branch_code?, name_ar?, legal_entity_fk?, branch_type_id?, is_active_fl?, page, size | قائمة Branch | — |
| API-ORG-009 | تعديل فرع | PUT | /api/v1/org/branches/{id} | name_ar?, name_en?, branch_type_id?, notes? | Branch محدَّث | RULE-ORG-011, 014, 015, 016 |
| API-ORG-010 | إلغاء تفعيل فرع | PUT | /api/v1/org/branches/{id}/deactivate | branch_pk | تأكيد | RULE-ORG-003, 004, 005 |
| API-ORG-011 | تفعيل فرع | PUT | /api/v1/org/branches/{id}/activate | branch_pk | تأكيد | — |
| API-ORG-012 | جلب بالمعرف | GET | /api/v1/org/branches/{id} | branch_pk | Branch كامل | — |

---

## SCR-ORG-003 — المناطق

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-003 |
| **اسم الشاشة** | إدارة المناطق |
| **UI Pattern** | PATTERN-1 — Search + Entry |
| **Pattern Reason** | حقول > 8 — تعتمد على LegalEntity و RegionType |
| **SCR-ID Scope** | ONE SCR-ID covers: Search + Entry (CORE-9) |
| **P3 Implication** | Two-screen UX navigation — P3 determines component names in F1 |
| **ENTITY-ID** | ENTITY-ORG-003 |
| **وظيفة الشاشة** | إنشاء وإدارة المناطق الجغرافية أو المبيعاتية المرتبطة بالكيانات القانونية |
| **المستخدمون** | مسؤول النظام، مسؤول التنظيم |
| **الموضع في النظام** | الهيكل التنظيمي ← المناطق |
| **روابط من** | القائمة الرئيسية — الهيكل التنظيمي |
| **روابط إلى** | — |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| region_code | نص | لا | — | بحث يساوي |
| name_ar | نص | لا | — | بحث يحتوي |
| name_en | نص | لا | — | بحث يحتوي |
| legal_entity_fk | LOV | لا | ENTITY-ORG-001 | اختيار كيان قانوني |
| region_type_id_fk | LOV | لا | ENTITY-ORG-008 | اختيار نوع المنطقة |
| is_active_fl | قائمة منسدلة | لا | نشط / غير نشط | القيم: 1 / 0 |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_REGION_CREATE |
| Edit | عند تحديد سجل | PERM_REGION_UPDATE |
| Deactivate | عند تحديد سجل نشط | PERM_REGION_DELETE |
| Activate | عند تحديد سجل غير نشط | PERM_REGION_UPDATE |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| region_code | نص (Read-Only) | نظام | ENTITY-ORG-003 → A3 | يُنشأ تلقائياً |
| name_ar | نص | نعم | ENTITY-ORG-003 → A3 | |
| name_en | نص | نعم | ENTITY-ORG-003 → A3 | |
| legal_entity_fk | LOV | نعم | ENTITY-ORG-001 → A3 | الكيانات النشطة فقط |
| region_type_id_fk | LOV | نعم | ENTITY-ORG-008 → A3 | أنواع المناطق النشطة |
| notes | نص طويل | لا | ENTITY-ORG-003 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (Create) | POST | RULE-ORG-012, 013, 015, 016 |
| حفظ (Update) | PUT | RULE-ORG-011, 014, 015, 016 |
| إلغاء تفعيل | PUT | RULE-ORG-006, 017 |
| تفعيل | PUT | — |

---

### B4 — الصلاحيات (Permissions)

| PAGE_CODE | PERM_VIEW | PERM_CREATE | PERM_UPDATE | PERM_DELETE |
|---|---|---|---|---|
| REGION | مسؤول النظام، مسؤول التنظيم | مسؤول النظام | مسؤول النظام، مسؤول التنظيم | مسؤول النظام |

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-013 | إنشاء منطقة | POST | /api/v1/org/regions | name_ar, name_en, legal_entity_fk, region_type_id_fk, notes? | Region كامل | RULE-ORG-012, 013, 015, 016 |
| API-ORG-014 | بحث المناطق | GET | /api/v1/org/regions | region_code?, name_ar?, legal_entity_fk?, region_type_id_fk?, is_active_fl?, page, size | قائمة Region | — |
| API-ORG-015 | تعديل منطقة | PUT | /api/v1/org/regions/{id} | name_ar?, name_en?, region_type_id_fk?, notes? | Region محدَّث | RULE-ORG-011, 014, 015, 016 |
| API-ORG-016 | إلغاء تفعيل منطقة | PUT | /api/v1/org/regions/{id}/deactivate | region_pk | تأكيد | RULE-ORG-006, 017 |
| API-ORG-017 | تفعيل منطقة | PUT | /api/v1/org/regions/{id}/activate | region_pk | تأكيد | — |
| API-ORG-018 | جلب بالمعرف | GET | /api/v1/org/regions/{id} | region_pk | Region كامل | — |

---

## SCR-ORG-004 — الأقسام

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-004 |
| **اسم الشاشة** | إدارة الأقسام |
| **UI Pattern** | PATTERN-3 — Specialized (Tree Hierarchy) |
| **Pattern Reason** | هيكل شجري متعدد المستويات — self-reference — يستلزم عرضاً هرمياً خاصاً |
| **SCR-ID Scope** | ONE SCR-ID — Specialized Tree Layout |
| **P3 Implication** | Specialized tree-view component + standard entry panel — P3 defines layout in F1 |
| **ENTITY-ID** | ENTITY-ORG-004 |
| **وظيفة الشاشة** | إنشاء وإدارة هيكل الأقسام الهرمي تحت كل فرع |
| **المستخدمون** | مسؤول النظام، مسؤول التنظيم |
| **الموضع في النظام** | الهيكل التنظيمي ← الأقسام |
| **روابط من** | الفروع (SCR-ORG-002) |

**Specialized Layout Description:**

| نوع الشاشة الخاصة | Tree Hierarchy |
|---|---|
| مبرر الاستثناء | الأقسام تملك self-reference متعدد المستويات — قائمة مسطحة غير كافية للتعبير عن الهيكل |
| المكونات الخاصة | Tree Panel (عرض هرمي بالتوسيع/الطي) + Entry Panel (تفاصيل العقدة المحددة) |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر التصفية في Tree View

| اسم الحقل | نوع الحقل | إلزامي | ملاحظات |
|---|---|---|---|
| branch_fk | LOV | نعم | فلتر إلزامي لعرض شجرة فرع محدد |
| name_ar / name_en | نص | لا | بحث داخل الشجرة المعروضة |
| node_type_id | قائمة منسدلة | لا | LOV-ORG-003 → A5 |
| is_active_fl | قائمة منسدلة | لا | نشط / غير نشط |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New (Root) | دائماً | PERM_DEPARTMENT_CREATE |
| New (Child) | عند تحديد عقدة أب | PERM_DEPARTMENT_CREATE |
| Edit | عند تحديد عقدة | PERM_DEPARTMENT_UPDATE |
| Deactivate | عند تحديد عقدة نشطة | PERM_DEPARTMENT_DELETE |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول Entry Panel

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| department_code | نص (Read-Only) | نظام | ENTITY-ORG-004 → A3 | يُنشأ تلقائياً |
| name_ar | نص | نعم | ENTITY-ORG-004 → A3 | |
| name_en | نص | نعم | ENTITY-ORG-004 → A3 | |
| branch_fk | LOV | نعم | ENTITY-ORG-002 → A3 | الفروع النشطة فقط |
| parent_department_fk | LOV (من الشجرة) | لا | ENTITY-ORG-004 → A3 | يُعبَّأ تلقائياً من العقدة المحددة |
| node_type_id | قائمة منسدلة | نعم | LOV-ORG-003 → A5 | محمي بعد الحفظ — RULE-ORG-020 |
| notes | نص طويل | لا | ENTITY-ORG-004 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (Create) | POST | RULE-ORG-007, 012, 013, 015, 016, 019 |
| حفظ (Update) | PUT | RULE-ORG-011, 014, 015, 016, 020 |
| إلغاء تفعيل | PUT | — |

---

### B4 — الصلاحيات (Permissions)

| PAGE_CODE | PERM_VIEW | PERM_CREATE | PERM_UPDATE | PERM_DELETE |
|---|---|---|---|---|
| DEPARTMENT | مسؤول النظام، مسؤول التنظيم | مسؤول النظام | مسؤول النظام، مسؤول التنظيم | مسؤول النظام |

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-019 | إنشاء قسم | POST | /api/v1/org/departments | name_ar, name_en, branch_fk, parent_department_fk?, node_type_id, notes? | Department كامل | RULE-ORG-007, 012, 013, 015, 016, 019 |
| API-ORG-020 | جلب شجرة أقسام | GET | /api/v1/org/departments/tree | branch_fk, is_active_fl? | هيكل شجري كامل | — |
| API-ORG-021 | بحث الأقسام (مسطّح) | GET | /api/v1/org/departments | branch_fk?, name_ar?, node_type_id?, is_active_fl?, page, size | قائمة Department | — |
| API-ORG-022 | تعديل قسم | PUT | /api/v1/org/departments/{id} | name_ar?, name_en?, parent_department_fk?, notes? | Department محدَّث | RULE-ORG-007, 011, 014, 015, 016, 020 |
| API-ORG-023 | إلغاء تفعيل قسم | PUT | /api/v1/org/departments/{id}/deactivate | department_pk | تأكيد | — |
| API-ORG-024 | تفعيل قسم | PUT | /api/v1/org/departments/{id}/activate | department_pk | تأكيد | — |
| API-ORG-025 | جلب بالمعرف | GET | /api/v1/org/departments/{id} | department_pk | Department كامل | — |

---

## SCR-ORG-005 — مراكز التكلفة

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-005 |
| **اسم الشاشة** | إدارة مراكز التكلفة |
| **UI Pattern** | PATTERN-3 — Specialized (Tree Hierarchy) |
| **Pattern Reason** | هيكل شجري متعدد المستويات — self-reference — مماثل لهيكل الأقسام |
| **SCR-ID Scope** | ONE SCR-ID — Specialized Tree Layout |
| **P3 Implication** | Specialized tree-view component — P3 defines layout in F1 |
| **ENTITY-ID** | ENTITY-ORG-005 |
| **وظيفة الشاشة** | إنشاء وإدارة هيكل مراكز التكلفة الهرمي تحت كل فرع |
| **المستخدمون** | مسؤول النظام، مسؤول المالية، مسؤول التنظيم |
| **الموضع في النظام** | الهيكل التنظيمي ← مراكز التكلفة |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر التصفية في Tree View

| اسم الحقل | نوع الحقل | إلزامي | ملاحظات |
|---|---|---|---|
| branch_fk | LOV | نعم | فلتر إلزامي |
| name_ar / name_en | نص | لا | بحث داخل الشجرة |
| node_type_id | قائمة منسدلة | لا | LOV-ORG-004 → A5 |
| cost_center_type_id | قائمة منسدلة | لا | LOV-ORG-005 → A5 |
| is_active_fl | قائمة منسدلة | لا | نشط / غير نشط |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول Entry Panel

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| cost_center_code | نص (Read-Only) | نظام | ENTITY-ORG-005 → A3 | يُنشأ تلقائياً |
| name_ar | نص | نعم | ENTITY-ORG-005 → A3 | |
| name_en | نص | نعم | ENTITY-ORG-005 → A3 | |
| branch_fk | LOV | نعم | ENTITY-ORG-002 → A3 | الفروع النشطة فقط |
| parent_cost_center_fk | LOV (من الشجرة) | لا | ENTITY-ORG-005 → A3 | |
| node_type_id | قائمة منسدلة | نعم | LOV-ORG-004 → A5 | محمي بعد الحفظ — RULE-ORG-020 |
| cost_center_type_id | قائمة منسدلة | نعم | LOV-ORG-005 → A5 | lookupKey: COST_CENTER_TYPE |
| notes | نص طويل | لا | ENTITY-ORG-005 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (Create) | POST | RULE-ORG-008, 012, 013, 015, 016, 019 |
| حفظ (Update) | PUT | RULE-ORG-011, 014, 015, 016, 020 |
| إلغاء تفعيل | PUT | — |

---

### B4 — الصلاحيات (Permissions)

| PAGE_CODE | PERM_VIEW | PERM_CREATE | PERM_UPDATE | PERM_DELETE |
|---|---|---|---|---|
| COST_CENTER | مسؤول النظام، مسؤول المالية، مسؤول التنظيم | مسؤول النظام، مسؤول المالية | مسؤول النظام، مسؤول المالية | مسؤول النظام |

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-026 | إنشاء مركز تكلفة | POST | /api/v1/org/cost-centers | name_ar, name_en, branch_fk, parent_cost_center_fk?, node_type_id, cost_center_type_id, notes? | CostCenter كامل | RULE-ORG-008, 012, 013, 015, 016, 019 |
| API-ORG-027 | جلب شجرة مراكز التكلفة | GET | /api/v1/org/cost-centers/tree | branch_fk, is_active_fl? | هيكل شجري كامل | — |
| API-ORG-028 | بحث مراكز التكلفة | GET | /api/v1/org/cost-centers | branch_fk?, name_ar?, node_type_id?, cost_center_type_id?, is_active_fl?, page, size | قائمة CostCenter | — |
| API-ORG-029 | تعديل مركز تكلفة | PUT | /api/v1/org/cost-centers/{id} | name_ar?, name_en?, parent_cost_center_fk?, cost_center_type_id?, notes? | CostCenter محدَّث | RULE-ORG-008, 011, 014, 015, 016, 020 |
| API-ORG-030 | إلغاء تفعيل | PUT | /api/v1/org/cost-centers/{id}/deactivate | cost_center_pk | تأكيد | — |
| API-ORG-031 | تفعيل | PUT | /api/v1/org/cost-centers/{id}/activate | cost_center_pk | تأكيد | — |
| API-ORG-032 | جلب بالمعرف | GET | /api/v1/org/cost-centers/{id} | cost_center_pk | CostCenter كامل | — |

---

## SCR-ORG-006 — مراكز الربح

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-006 |
| **اسم الشاشة** | إدارة مراكز الربح |
| **UI Pattern** | PATTERN-1 — Search + Entry |
| **Pattern Reason** | حقول > 8 — كيان مسطّح بدون self-reference — مرتبط بالكيان القانوني |
| **SCR-ID Scope** | ONE SCR-ID covers: Search + Entry (CORE-9) |
| **P3 Implication** | Two-screen UX navigation — P3 determines component names in F1 |
| **ENTITY-ID** | ENTITY-ORG-006 |
| **وظيفة الشاشة** | إنشاء وإدارة مراكز الربح المرتبطة بالكيانات القانونية |
| **المستخدمون** | مسؤول النظام، مسؤول المالية، مسؤول التنظيم |
| **الموضع في النظام** | الهيكل التنظيمي ← مراكز الربح |
| **ملاحظة معمارية** | ProfitCenter مملوك لـ Organization — Finance يستهلكه عبر HARD-FK |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| profit_center_code | نص | لا | — | بحث يساوي |
| name_ar | نص | لا | — | بحث يحتوي |
| name_en | نص | لا | — | بحث يحتوي |
| legal_entity_fk | LOV | لا | ENTITY-ORG-001 | اختيار كيان قانوني |
| is_active_fl | قائمة منسدلة | لا | نشط / غير نشط | القيم: 1 / 0 |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_PROFIT_CENTER_CREATE |
| Edit | عند تحديد سجل | PERM_PROFIT_CENTER_UPDATE |
| Deactivate | عند تحديد سجل نشط | PERM_PROFIT_CENTER_DELETE |
| Activate | عند تحديد سجل غير نشط | PERM_PROFIT_CENTER_UPDATE |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| profit_center_code | نص (Read-Only) | نظام | ENTITY-ORG-006 → A3 | يُنشأ تلقائياً |
| name_ar | نص | نعم | ENTITY-ORG-006 → A3 | |
| name_en | نص | نعم | ENTITY-ORG-006 → A3 | |
| legal_entity_fk | LOV | نعم | ENTITY-ORG-001 → A3 | الكيانات النشطة فقط |
| notes | نص طويل | لا | ENTITY-ORG-006 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (Create) | POST | RULE-ORG-012, 013, 015, 016 |
| حفظ (Update) | PUT | RULE-ORG-011, 014, 015, 016 |
| إلغاء تفعيل | PUT | RULE-ORG-002 (cascade check via LegalEntity) |
| تفعيل | PUT | — |

---

### B4 — الصلاحيات (Permissions)

| PAGE_CODE | PERM_VIEW | PERM_CREATE | PERM_UPDATE | PERM_DELETE |
|---|---|---|---|---|
| PROFIT_CENTER | مسؤول النظام، مسؤول المالية، مسؤول التنظيم | مسؤول النظام، مسؤول المالية | مسؤول النظام، مسؤول المالية | مسؤول النظام |

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-033 | إنشاء مركز ربح | POST | /api/v1/org/profit-centers | name_ar, name_en, legal_entity_fk, notes? | ProfitCenter كامل | RULE-ORG-012, 013, 015, 016 |
| API-ORG-034 | بحث مراكز الربح | GET | /api/v1/org/profit-centers | profit_center_code?, name_ar?, legal_entity_fk?, is_active_fl?, page, size | قائمة ProfitCenter | — |
| API-ORG-035 | تعديل مركز ربح | PUT | /api/v1/org/profit-centers/{id} | name_ar?, name_en?, notes? | ProfitCenter محدَّث | RULE-ORG-011, 014, 015, 016 |
| API-ORG-036 | إلغاء تفعيل | PUT | /api/v1/org/profit-centers/{id}/deactivate | profit_center_pk | تأكيد | — |
| API-ORG-037 | تفعيل | PUT | /api/v1/org/profit-centers/{id}/activate | profit_center_pk | تأكيد | — |
| API-ORG-038 | جلب بالمعرف | GET | /api/v1/org/profit-centers/{id} | profit_center_pk | ProfitCenter كامل | — |

---

## SCR-ORG-007 — مواقع العمل

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-007 |
| **اسم الشاشة** | إدارة مواقع العمل |
| **UI Pattern** | PATTERN-1 — Search + Entry |
| **Pattern Reason** | حقول > 8 — كيان مسطّح مرتبط بالفرع |
| **SCR-ID Scope** | ONE SCR-ID covers: Search + Entry (CORE-9) |
| **P3 Implication** | Two-screen UX navigation — P3 determines component names in F1 |
| **ENTITY-ID** | ENTITY-ORG-007 |
| **وظيفة الشاشة** | إنشاء وإدارة المواقع المادية للعمل المرتبطة بالفروع |
| **المستخدمون** | مسؤول النظام، مسؤول التنظيم |
| **الموضع في النظام** | الهيكل التنظيمي ← مواقع العمل |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| location_site_code | نص | لا | — | بحث يساوي |
| name_ar | نص | لا | — | بحث يحتوي |
| name_en | نص | لا | — | بحث يحتوي |
| branch_fk | LOV | لا | ENTITY-ORG-002 | اختيار فرع |
| site_type_id | قائمة منسدلة | لا | LOV-ORG-006 → A5 | lookupKey: LOCATION_SITE_TYPE |
| is_active_fl | قائمة منسدلة | لا | نشط / غير نشط | القيم: 1 / 0 |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_LOCATION_SITE_CREATE |
| Edit | عند تحديد سجل | PERM_LOCATION_SITE_UPDATE |
| Deactivate | عند تحديد سجل نشط | PERM_LOCATION_SITE_DELETE |
| Activate | عند تحديد سجل غير نشط | PERM_LOCATION_SITE_UPDATE |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| location_site_code | نص (Read-Only) | نظام | ENTITY-ORG-007 → A3 | يُنشأ تلقائياً |
| name_ar | نص | نعم | ENTITY-ORG-007 → A3 | |
| name_en | نص | نعم | ENTITY-ORG-007 → A3 | |
| branch_fk | LOV | نعم | ENTITY-ORG-002 → A3 | الفروع النشطة فقط |
| site_type_id | قائمة منسدلة | نعم | LOV-ORG-006 → A5 | lookupKey: LOCATION_SITE_TYPE |
| notes | نص طويل | لا | ENTITY-ORG-007 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (Create) | POST | RULE-ORG-012, 013, 015, 016, 019 |
| حفظ (Update) | PUT | RULE-ORG-011, 014, 015, 016 |
| إلغاء تفعيل | PUT | RULE-ORG-005 (cascade check via Branch) |

---

### B4 — الصلاحيات (Permissions)

| PAGE_CODE | PERM_VIEW | PERM_CREATE | PERM_UPDATE | PERM_DELETE |
|---|---|---|---|---|
| LOCATION_SITE | مسؤول النظام، مسؤول التنظيم | مسؤول النظام | مسؤول النظام، مسؤول التنظيم | مسؤول النظام |

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-039 | إنشاء موقع عمل | POST | /api/v1/org/location-sites | name_ar, name_en, branch_fk, site_type_id, notes? | LocationSite كامل | RULE-ORG-012, 013, 015, 016, 019 |
| API-ORG-040 | بحث مواقع العمل | GET | /api/v1/org/location-sites | location_site_code?, name_ar?, branch_fk?, site_type_id?, is_active_fl?, page, size | قائمة LocationSite | — |
| API-ORG-041 | تعديل موقع عمل | PUT | /api/v1/org/location-sites/{id} | name_ar?, name_en?, site_type_id?, notes? | LocationSite محدَّث | RULE-ORG-011, 014, 015, 016 |
| API-ORG-042 | إلغاء تفعيل | PUT | /api/v1/org/location-sites/{id}/deactivate | location_site_pk | تأكيد | RULE-ORG-005 |
| API-ORG-043 | تفعيل | PUT | /api/v1/org/location-sites/{id}/activate | location_site_pk | تأكيد | — |
| API-ORG-044 | جلب بالمعرف | GET | /api/v1/org/location-sites/{id} | location_site_pk | LocationSite كامل | — |

---

# ══════════════════════════════════════════════════════════
# STANDALONE — بعد PART B
# ══════════════════════════════════════════════════════════

---

## Permissions Summary & Registry Update

> هذا الجدول aggregate view مُجمَّع من B4 sections.
> B4 هو المصدر — هذا الجدول للمراجعة الإجمالية (P4 CHECK-10).

> **SEC-3 Rule:** Security Engine يولّد PERM_* تلقائياً من PAGE_CODE.
> SRS لا تُدرج INSERT statements صريحة لـ PERMISSIONS — Security Engine هو المُولِّد.

| الشاشة (SCR-ID) | PAGE_CODE | PERM_VIEW | PERM_CREATE | PERM_UPDATE | PERM_DELETE |
|---|---|---|---|---|---|
| SCR-ORG-001 — الكيانات القانونية | LEGAL_ENTITY | مسؤول النظام، مسؤول التنظيم | مسؤول النظام | مسؤول النظام، مسؤول التنظيم | مسؤول النظام |
| SCR-ORG-002 — الفروع | BRANCH | مسؤول النظام، مسؤول التنظيم | مسؤول النظام | مسؤول النظام، مسؤول التنظيم | مسؤول النظام |
| SCR-ORG-003 — المناطق | REGION | مسؤول النظام، مسؤول التنظيم | مسؤول النظام | مسؤول النظام، مسؤول التنظيم | مسؤول النظام |
| SCR-ORG-004 — الأقسام | DEPARTMENT | مسؤول النظام، مسؤول التنظيم | مسؤول النظام | مسؤول النظام، مسؤول التنظيم | مسؤول النظام |
| SCR-ORG-005 — مراكز التكلفة | COST_CENTER | مسؤول النظام، مسؤول المالية، مسؤول التنظيم | مسؤول النظام، مسؤول المالية | مسؤول النظام، مسؤول المالية | مسؤول النظام |
| SCR-ORG-006 — مراكز الربح | PROFIT_CENTER | مسؤول النظام، مسؤول المالية، مسؤول التنظيم | مسؤول النظام، مسؤول المالية | مسؤول النظام، مسؤول المالية | مسؤول النظام |
| SCR-ORG-007 — مواقع العمل | LOCATION_SITE | مسؤول النظام، مسؤول التنظيم | مسؤول النظام | مسؤول النظام، مسؤول التنظيم | مسؤول النظام |

> VIEW = gateway: يمنح الوصول للبحث والإدخال (read mode) — لا يمكن الوصول لأي شاشة بدونه
> SEC_PAGES seeding: module = ORGANIZATION لجميع الصفحات

---

### Registry Update — MODE 1

```
## REGISTRY UPDATE — 2026-06-28
────────────────────────────────────────────────────────────────
Source Mode    : MODE 1 (SRS Regeneration — DB_TARGET=POSTGRESQL_16)
Feature Code   : ORG-001
DBS-ID         : DBS-ORG-001 (GOVERNED ✓ — PG syntax)
Plan ID        : PLAN-ORG-001 (GOVERNED ✓ — ALIGN GATE PASSED)
────────────────────────────────────────────────────────────────
Entities       : ENTITY-ORG-001..008 (7 SHARED + 1 PRIVATE Ref Table)
DB Tables      : ORG_LEGAL_ENTITY, ORG_BRANCH, ORG_REGION,
                 ORG_DEPARTMENT, ORG_COST_CENTER, ORG_PROFIT_CENTER,
                 ORG_LOCATION_SITE, ORG_REGION_TYPE
Lookups        : LEGAL_ENTITY_TYPE, BRANCH_TYPE, DEPARTMENT_NODE_TYPE,
                 COST_CENTER_NODE_TYPE, COST_CENTER_TYPE, LOCATION_SITE_TYPE
SCR-IDs        : SCR-ORG-001 through SCR-ORG-007 (7 screens)
APIs           : API-ORG-001 through API-ORG-044 (44 APIs)
RULE-IDs       : RULE-ORG-001 through RULE-ORG-020 (20 rules)
LOV-IDs        : LOV-ORG-001 through LOV-ORG-006 (6 LOVs)
XM-IDs Open   : None — ROOT MODULE — zero outbound XM dependencies
OQ-IDs Open   : OQ-001 (DEFERRED — AQ-003)
Gate Status    : GOVERNED ✓ MODE 2 — ALIGN GATE PASSED ✓
Next Action    : MODE 2.5 → test-plan-org-001.md
────────────────────────────────────────────────────────────────
DB_TARGET change note:
  This SRS regenerated with DB_TARGET=POSTGRESQL_16.
  All field type notations updated: VARCHAR2→VARCHAR, NUMBER(1)→SMALLINT,
  NUMBER(10)/NUMBER→BIGINT, CLOB→TEXT.
  DBS-ORG-001 must be confirmed as PG-aligned (per registry-update-blocks-ORG.md
  note: "PG migration 2026-06-28"). If DBS-ORG-001 still uses Oracle syntax,
  a separate P2 amendment session is required.
────────────────────────────────────────────────────────────────
```

---

## OQ Log — سجل الأسئلة المفتوحة

```
## OPEN QUESTIONS LOG — Organization (ORG-001) — 2026-06-28
─────────────────────────────────────────────────────────────────────
OQ-ID  │ Question                                     │ Status   │ Raised   │ Resolved │ Escalation
───────┼──────────────────────────────────────────────┼──────────┼──────────┼──────────┼──────────────────
OQ-001 │ ما هو تأثير إلغاء تفعيل Region على          │ DEFERRED │ MODE 1.5 │ —        │ XM-ESC-[CONSUMER]
       │ الموديولات المستهلكة عبر SOFT-READ؟          │          │          │          │
       │ هل يُمنع الإلغاء؟ هل يُبلَّغ المستهلكون؟   │          │          │          │
       │ Source: ARCH-8 auto-raise                     │          │          │          │
       │ Affects: ENTITY-ORG-003 — API-ORG-016         │          │          │          │
       │ Note: AQ-003 in master-registry Section 14    │          │          │          │
       │ Non-blocking — resolves when first consumer   │          │          │          │
       │ module runs MODE 1.5                          │          │          │          │
─────────────────────────────────────────────────────────────────────
Total: 1 active (DEFERRED — non-blocking)
```

---

*نهاية الوثيقة | End of srs-org-001.md*
*Governed by: SRS Governance Engine (Project 1)*
*Feature Code: ORG-001 | Version: 1.0 | DB_TARGET: POSTGRESQL_16*
*Structure: PART A (8 Entities | 20 Rules | 6 LOVs) + PART B (7 Screens | 44 APIs)*
*Pipeline: GOVERNED ✓ MODE 2 — ALIGN GATE PASSED ✓*
*Next Mode: MODE 2.5 — Test Plan Generation*
