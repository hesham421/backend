<!-- ═══════════════════════════════════════════════════════════ -->
<!-- SRS — وثيقة التحليل والمتطلبات                             -->
<!-- Governed by: SRS Governance Engine (Project 1)             -->
<!-- Compatible: PROJECT-2 | PROJECT-3 | PROJECT-4              -->
<!-- Structure : PART A (Module Foundation) + PART B (Screens)  -->
<!-- ═══════════════════════════════════════════════════════════ -->

# وثيقة التحليل (SRS)
## موديول التنظيم المؤسسي | Organization Module

---

# ══════════════════════════════════════════════════════════
# PART A — MODULE FOUNDATION
# Single source of truth — read once per module
# ══════════════════════════════════════════════════════════

---

## A1 — معلومات الوثيقة (Document Information)

| البند | القيمة |
|---|---|
| **اسم المشروع** | نظام إدارة الموارد الحكومية |
| **الموديول** | التنظيم المؤسسي — Organization |
| **Feature Code** | ORG-001 |
| **Feature Type** | Master Data — Core Foundation |
| **الطبقة** | Layer-1 — Foundation (L1-1) |
| **إعداد بواسطة** | SRS Governance Engine (Project 1) |
| **النسخة** | 1.0 |
| **التاريخ** | 2026-06-23 |
| **الحالة** | GOVERNED ✓ |
| **Open Questions** | 1 active (OQ-001) — see OQ Log |
| **Governed by** | SRS Governance Engine (Project 1) |
| **P0 Context** | module-registry-ORG.md / business-policies-ORG.md — REGENERATED 2026-06-23 |
| **Registry Status** | master-registry.md v2.7.2 — GOVERNED ✓ MODE 1.5 |

---

## A2 — السياق الوظيفي (Functional Context)

### ما يشمله هذا الموديول

> يشمل هذا الموديول تعريف وإدارة الهيكل التنظيمي المؤسسي الكامل للمنظومة الحكومية، ويغطي سبعة كيانات أساسية: الكيان القانوني، والفرع، والمنطقة الجغرافية/التشغيلية، والقسم، ومركز التكلفة، ومركز الربح، والموقع الجغرافي. تُعدّ هذه الكيانات السبعة الجذر التنظيمي لجميع الموديولات الأخرى في المنصة.

### ما لا يشمله هذا الموديول

> لا يشمل هذا الموديول: وحدة الأعمال (BusinessUnit — مؤجلة)، والدولة (Country — مملوكة لـ MasterData 1.4)، والمستودع (Warehouse — مملوك لـ Inventory 3.2). لا يتضمن الموديول أي منطق مالي، ولا آليات موافقة، ولا تدفقات عمل.

### وظيفة الموديول

> يُمكّن موديول التنظيم المؤسسي المستخدمين المخوّلين من إنشاء وإدارة الهيكل التنظيمي للمؤسسة بشكل هرمي وكامل الضبط، بدءاً من الكيان القانوني وصولاً إلى أدق مستويات التنظيم كالأقسام ومراكز التكلفة، مما يُشكّل الأساس البياني الذي تستند إليه جميع عمليات المنصة الأخرى.

### الوصف الوظيفي التفصيلي

> يُمثّل موديول التنظيم المؤسسي الركيزة التأسيسية للمنصة بأسرها. يبدأ الهيكل من **الكيان القانوني (LegalEntity)** الذي يُعرّف الوجود القانوني المستقل للمؤسسة أو أي جهة تابعة لها. تتفرع من كل كيان قانوني **فروع (Branches)** تمثّل الوحدات التشغيلية الرئيسية، وتُمثّل **المناطق (Regions)** تجميعاً جغرافياً أو تشغيلياً مرتبطاً بالكيان القانوني. تنتمي **الأقسام (Departments)** إلى الفروع وتُبنى كهيكل شجري متعدد المستويات يدعم التمييز بين عقد التجميع (SUMMARY) وعقد الإدخال (DETAIL). كذلك **مراكز التكلفة (CostCenters)** وهي كيانات مالية هرمية مرتبطة بالفروع، أما **مراكز الربح (ProfitCenters)** فترتبط بالكيان القانوني مباشرة بوصفها وحدات تقرير مالي. أخيراً، **المواقع الجغرافية (LocationSites)** تُعرّف المواقع الفيزيائية المرتبطة بالفروع وهي مصدر بيانات الموقع لموديول المخازن.
>
> جميع الكيانات السبعة تُنشأ بشكل محكوم: لكل كيان رمز أعمال تلقائي غير قابل للتعديل، ونمط تفعيل/تعطيل ناعم (isActiveFl) مع قواعد منع التعطيل عند وجود تبعيات نشطة. لا توجد دورة حياة معقدة أو محرك موافقة — الكيانات إما نشطة أو غير نشطة.

#### الوضع الحالي

| الخطوات | الجهة | ملاحظات |
|---|---|---|
| تعريف الكيان القانوني يدوياً | الإدارة العليا | غير رسمي |
| تعريف الفروع والأقسام يدوياً | إدارة الموارد | مبعثرة في ملفات منفصلة |
| لا يوجد نظام موحّد للهيكل التنظيمي | — | يُسبّب تعارضات في البيانات |

#### الصعوبات الحالية

| # | الصعوبة |
|---|---|
| 1 | غياب مصدر موحّد للهيكل التنظيمي يُسبّب بيانات متضاربة بين الإدارات |
| 2 | عدم وجود قواعد تحكم العلاقات الهرمية بين الكيانات |
| 3 | عدم قدرة النظام الحالي على منع تعطيل كيانات لها تبعيات نشطة |

#### النظام المقترح وفوائده

| # | الفائدة |
|---|---|
| 1 | مصدر حقيقة واحد للهيكل التنظيمي تستهلكه جميع الموديولات |
| 2 | رموز أعمال تلقائية وغير قابلة للتعديل تضمن ترتيباً موحّداً |
| 3 | قواعد منع التعطيل تحمي سلامة البيانات عبر المنصة كاملة |
| 4 | هياكل شجرية مُدارة للأقسام ومراكز التكلفة تدعم التقارير المالية الهرمية |

### ملاحظات عامة

- جميع الكيانات السبعة مُصنَّفة SHARED (مالك): تستهلكها جميع الموديولات الأخرى عبر HARD-FK أو SOFT-READ
- الموديول جذري (ROOT) — لا تبعيات خارجية صادرة
- ORG_REGION_TYPE كيان مرجعي (Reference Table) — ليس Lookup Detail — يدعم > 15 قيمة وقابل للتوسيع
- Department وCostCenter هياكل شجرية ذات مرجع ذاتي (self-reference)
- AQ-003 مؤجّل وغير مانع للمتابعة: يُحسم عند تشغيل أول موديول مستهلك لـ Region في MODE 1.5
- لا يوجد Workflow Engine — لا يُذكر ولا يُقترح (RULE-13)

---

## A3 — الكيانات والحقول (Entities & Fields)

> **قاعدة إلزامية:** هذا القسم هو المصدر الوحيد لتعريف الكيانات والحقول.
> PART B يُشير للكيانات بـ ENTITY-ID فقط — لا يُعيد تعريفها.
> جميع الكيانات مُصنَّفة SHARED (owner) — مستهلكة من جميع الموديولات اللاحقة.

---

### ENTITY-ORG-001 — LegalEntity (الكيان القانوني)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **جدول DB** | ORG_LEGAL_ENTITY — GOVERNED ✓ DBS-ORG-001 |
| **Business Code** | YES — Format: `LE-NNNNN` (توليد: NumberingEngine) |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | None — ROOT entity |

#### حقول الكيان

| اسم الحقل | نوع | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| legalEntityPk | NUMBER (PK) | نظام | — | رقم تسلسلي تلقائي |
| legalEntityCode | VARCHAR2(20) | نظام | — | يُنشأ تلقائياً — Read-Only بعد الحفظ — NumberingEngine |
| nameAr | VARCHAR2(200) | نعم | — | اسم الكيان بالعربي |
| nameEn | VARCHAR2(100) | نعم | — | اسم الكيان بالإنجليزي |
| entityTypeId | VARCHAR2(50) | نعم | LOV-ORG-001 | lookupKey: LEGAL_ENTITY_TYPE |
| isActiveFl | NUMBER(1) | نظام | 1 / 0 | 1=نشط، 0=غير نشط — Fl suffix إلزامي |
| createdBy | VARCHAR2(100) | نظام | — | AuditEntityListener — لا يُقبل في DTO |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener — لا يُقبل في DTO |
| updatedBy | VARCHAR2(100) | نظام | — | AuditEntityListener — لا يُقبل في DTO |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener — لا يُقبل في DTO |
| notes | VARCHAR2(2000) | لا | — | ملاحظات |

---

### ENTITY-ORG-002 — Branch (الفرع)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **جدول DB** | ORG_BRANCH — GOVERNED ✓ DBS-ORG-001 |
| **Business Code** | YES — Format: `BR-[LE]-NNNNN` (فريد ضمن الكيان القانوني) |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | يُنشأ تحت ENTITY-ORG-001 (legalEntityFk NOT NULL RESTRICT) |

#### حقول الكيان

| اسم الحقل | نوع | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| branchPk | NUMBER (PK) | نظام | — | رقم تسلسلي تلقائي |
| branchCode | VARCHAR2(20) | نظام | — | يُنشأ تلقائياً — Read-Only — فريد ضمن الكيان القانوني |
| nameAr | VARCHAR2(200) | نعم | — | اسم الفرع بالعربي |
| nameEn | VARCHAR2(100) | نعم | — | اسم الفرع بالإنجليزي |
| legalEntityFk | NUMBER (FK) | نعم | ENTITY-ORG-001 | NOT NULL — RESTRICT on deactivate |
| branchTypeId | VARCHAR2(50) | نعم | LOV-ORG-002 | lookupKey: BRANCH_TYPE |
| isActiveFl | NUMBER(1) | نظام | 1 / 0 | 1=نشط، 0=غير نشط |
| createdBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener |
| updatedBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener |
| notes | VARCHAR2(2000) | لا | — | ملاحظات |

---

### ENTITY-ORG-003 — Region (المنطقة)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **جدول DB** | ORG_REGION — GOVERNED ✓ DBS-ORG-001 |
| **Business Code** | YES — Format: `RG-[LE]-NNNNN` (فريد ضمن الكيان القانوني) |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | يُنشأ تحت ENTITY-ORG-001 (legalEntityFk NOT NULL RESTRICT) |
| **ملاحظة** | AQ-003 مؤجّل — المستهلكون عبر SOFT-READ غير محددين بعد |

#### حقول الكيان

| اسم الحقل | نوع | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| regionPk | NUMBER (PK) | نظام | — | رقم تسلسلي تلقائي |
| regionCode | VARCHAR2(20) | نظام | — | يُنشأ تلقائياً — Read-Only — فريد ضمن الكيان القانوني |
| nameAr | VARCHAR2(200) | نعم | — | اسم المنطقة بالعربي |
| nameEn | VARCHAR2(100) | نعم | — | اسم المنطقة بالإنجليزي |
| legalEntityFk | NUMBER (FK) | نعم | ENTITY-ORG-001 | NOT NULL — RESTRICT on deactivate |
| regionTypeId | NUMBER (FK) | نعم | ENTITY-ORG-008 | FK إلى ORG_REGION_TYPE — Reference Table (ليس Lookup) |
| isActiveFl | NUMBER(1) | نظام | 1 / 0 | 1=نشط، 0=غير نشط |
| createdBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener |
| updatedBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener |
| notes | VARCHAR2(2000) | لا | — | ملاحظات |

---

### ENTITY-ORG-004 — Department (القسم)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **جدول DB** | ORG_DEPARTMENT — GOVERNED ✓ DBS-ORG-001 |
| **Business Code** | YES — Format: `DEP-[BR]-NNNNN` (فريد ضمن الفرع) |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | يُنشأ تحت ENTITY-ORG-002 (branchFk NOT NULL RESTRICT) |
| **هيكل** | شجري — self-reference (parentDepartmentFk NULLABLE) |

#### حقول الكيان

| اسم الحقل | نوع | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| departmentPk | NUMBER (PK) | نظام | — | رقم تسلسلي تلقائي |
| deptCode | VARCHAR2(20) | نظام | — | يُنشأ تلقائياً — Read-Only — فريد ضمن الفرع |
| nameAr | VARCHAR2(200) | نعم | — | اسم القسم بالعربي |
| nameEn | VARCHAR2(100) | نعم | — | اسم القسم بالإنجليزي |
| branchFk | NUMBER (FK) | نعم | ENTITY-ORG-002 | NOT NULL — RESTRICT on deactivate |
| parentDepartmentFk | NUMBER (FK) | لا | ENTITY-ORG-004 (self) | NULLABLE — يُحدد الأب في الشجرة |
| nodeTypeId | VARCHAR2(50) | نعم | LOV-ORG-003 | lookupKey: DEPARTMENT_NODE_TYPE — SUMMARY / DETAIL |
| isActiveFl | NUMBER(1) | نظام | 1 / 0 | 1=نشط، 0=غير نشط |
| createdBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener |
| updatedBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener |
| notes | VARCHAR2(2000) | لا | — | ملاحظات |

---

### ENTITY-ORG-005 — CostCenter (مركز التكلفة)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **جدول DB** | ORG_COST_CENTER — GOVERNED ✓ DBS-ORG-001 |
| **Business Code** | YES — Format: `CC-[BR]-NNNNN` (فريد ضمن الفرع) |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | يُنشأ تحت ENTITY-ORG-002 (branchFk NOT NULL RESTRICT) |
| **هيكل** | شجري — self-reference (parentCostCenterFk NULLABLE) |

#### حقول الكيان

| اسم الحقل | نوع | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| costCenterPk | NUMBER (PK) | نظام | — | رقم تسلسلي تلقائي |
| costCenterCode | VARCHAR2(20) | نظام | — | يُنشأ تلقائياً — Read-Only — فريد ضمن الفرع |
| nameAr | VARCHAR2(200) | نعم | — | اسم مركز التكلفة بالعربي |
| nameEn | VARCHAR2(100) | نعم | — | اسم مركز التكلفة بالإنجليزي |
| branchFk | NUMBER (FK) | نعم | ENTITY-ORG-002 | NOT NULL — RESTRICT on deactivate |
| parentCostCenterFk | NUMBER (FK) | لا | ENTITY-ORG-005 (self) | NULLABLE — يُحدد الأب في الشجرة |
| nodeTypeId | VARCHAR2(50) | نعم | LOV-ORG-004 | lookupKey: COST_CENTER_NODE_TYPE — SUMMARY / DETAIL |
| costCenterTypeId | VARCHAR2(50) | نعم | LOV-ORG-005 | lookupKey: COST_CENTER_TYPE — Direct / Indirect / Shared |
| isActiveFl | NUMBER(1) | نظام | 1 / 0 | 1=نشط، 0=غير نشط |
| createdBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener |
| updatedBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener |
| notes | VARCHAR2(2000) | لا | — | ملاحظات |

---

### ENTITY-ORG-006 — ProfitCenter (مركز الربح)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **جدول DB** | ORG_PROFIT_CENTER — GOVERNED ✓ DBS-ORG-001 |
| **Business Code** | YES — Format: `PC-[LE]-NNNNN` (فريد ضمن الكيان القانوني) |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | يُنشأ تحت ENTITY-ORG-001 (legalEntityFk NOT NULL RESTRICT) |
| **ملاحظة** | مرتبط بالكيان القانوني مباشرة (وحدة تقرير مالي — ليس بالفرع) |

#### حقول الكيان

| اسم الحقل | نوع | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| profitCenterPk | NUMBER (PK) | نظام | — | رقم تسلسلي تلقائي |
| profitCenterCode | VARCHAR2(20) | نظام | — | يُنشأ تلقائياً — Read-Only — فريد ضمن الكيان القانوني |
| nameAr | VARCHAR2(200) | نعم | — | اسم مركز الربح بالعربي |
| nameEn | VARCHAR2(100) | نعم | — | اسم مركز الربح بالإنجليزي |
| legalEntityFk | NUMBER (FK) | نعم | ENTITY-ORG-001 | NOT NULL — RESTRICT on deactivate |
| isActiveFl | NUMBER(1) | نظام | 1 / 0 | 1=نشط، 0=غير نشط |
| createdBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener |
| updatedBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener |
| notes | VARCHAR2(2000) | لا | — | ملاحظات |

---

### ENTITY-ORG-007 — LocationSite (الموقع الجغرافي)

| البند | القيمة |
|---|---|
| **النوع** | SHARED (owner: Organization) |
| **جدول DB** | ORG_LOCATION_SITE — GOVERNED ✓ DBS-ORG-001 |
| **Business Code** | YES — Format: `LS-[BR]-NNNNN` (فريد ضمن الفرع) |
| **العمليات** | Create, Read, Update, Deactivate |
| **Cross-Module** | يُنشأ تحت ENTITY-ORG-002 (branchFk NOT NULL RESTRICT) |
| **هيكل** | مسطّح (Flat) — لا هيكل شجري |

#### حقول الكيان

| اسم الحقل | نوع | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| locationSitePk | NUMBER (PK) | نظام | — | رقم تسلسلي تلقائي |
| locationCode | VARCHAR2(20) | نظام | — | يُنشأ تلقائياً — Read-Only — فريد ضمن الفرع |
| nameAr | VARCHAR2(200) | نعم | — | اسم الموقع بالعربي |
| nameEn | VARCHAR2(100) | نعم | — | اسم الموقع بالإنجليزي |
| branchFk | NUMBER (FK) | نعم | ENTITY-ORG-002 | NOT NULL — RESTRICT on deactivate |
| siteTypeId | VARCHAR2(50) | نعم | LOV-ORG-006 | lookupKey: LOCATION_SITE_TYPE |
| isActiveFl | NUMBER(1) | نظام | 1 / 0 | 1=نشط، 0=غير نشط |
| createdBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener |
| updatedBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener |
| notes | VARCHAR2(2000) | لا | — | ملاحظات |

---

### ENTITY-ORG-008 — RegionType (نوع المنطقة) — Reference Table

| البند | القيمة |
|---|---|
| **النوع** | PRIVATE — Reference Table (قابل للتوسيع بواسطة Admin) |
| **جدول DB** | ORG_REGION_TYPE — GOVERNED ✓ DBS-ORG-001 |
| **Business Code** | لا — Reference Table |
| **العمليات** | Create, Read, Update, Deactivate (Admin فقط) |
| **ملاحظة** | > 15 قيمة متوقعة → Reference Table وليس MD_LOOKUP_DETAIL |

#### حقول الكيان

| اسم الحقل | نوع | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| regionTypePk | NUMBER (PK) | نظام | — | رقم تسلسلي تلقائي |
| nameAr | VARCHAR2(200) | نعم | — | اسم نوع المنطقة بالعربي |
| nameEn | VARCHAR2(100) | نعم | — | اسم نوع المنطقة بالإنجليزي |
| isActiveFl | NUMBER(1) | نظام | 1 / 0 | 1=نشط، 0=غير نشط |
| createdBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener |
| updatedBy | VARCHAR2(100) | نظام | — | AuditEntityListener |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener |

**القيم الأولية (seed data):**

| nameAr | nameEn |
|---|---|
| جغرافي | GEOGRAPHIC |
| مبيعات | SALES |
| تشغيلي | OPERATIONAL |

---

## A4 — قواعد التحقق (Business Rules)

> **قاعدة إلزامية:** هذا القسم هو المصدر الوحيد لتعريف القواعد.
> PART B يُشير للقواعد بـ RULE-ID فقط — لا يُعيد تعريفها.
> رسائل الخطأ ثنائية اللغة (عربي + إنجليزي) إلزامية في جميع القواعد.

---

### RULE-ORG-001 — منع تعطيل الكيان القانوني مع فروع نشطة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-001 |
| **Trigger** | عند طلب تعطيل Deactivate |
| **Statement** | The system MUST prevent deactivation of a LegalEntity when one or more active Branches reference it |
| **Message-AR** | لا يمكن تعطيل الكيان القانوني لوجود فروع نشطة مرتبطة به. يرجى تعطيل جميع الفروع أولاً. |
| **Message-EN** | Cannot deactivate Legal Entity: active branches exist. Please deactivate all branches first. |
| **Source** | module-registry-ORG.md — RULE-ORG-01 |

---

### RULE-ORG-002 — منع تعطيل الكيان القانوني مع مراكز ربح نشطة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-001 |
| **Trigger** | عند طلب تعطيل Deactivate |
| **Statement** | The system MUST prevent deactivation of a LegalEntity when one or more active ProfitCenters reference it |
| **Message-AR** | لا يمكن تعطيل الكيان القانوني لوجود مراكز ربح نشطة مرتبطة به. يرجى تعطيل جميع مراكز الربح أولاً. |
| **Message-EN** | Cannot deactivate Legal Entity: active profit centers exist. Please deactivate all profit centers first. |
| **Source** | module-registry-ORG.md — RULE-ORG-02 |

---

### RULE-ORG-003 — منع تعطيل الفرع مع أقسام نشطة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-002 |
| **Trigger** | عند طلب تعطيل Deactivate |
| **Statement** | The system MUST prevent deactivation of a Branch when one or more active Departments reference it |
| **Message-AR** | لا يمكن تعطيل الفرع لوجود أقسام نشطة مرتبطة به. يرجى تعطيل جميع الأقسام أولاً. |
| **Message-EN** | Cannot deactivate Branch: active departments exist. Please deactivate all departments first. |
| **Source** | module-registry-ORG.md — RULE-ORG-03 |

---

### RULE-ORG-004 — منع تعطيل الفرع مع مراكز تكلفة نشطة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-002 |
| **Trigger** | عند طلب تعطيل Deactivate |
| **Statement** | The system MUST prevent deactivation of a Branch when one or more active CostCenters reference it |
| **Message-AR** | لا يمكن تعطيل الفرع لوجود مراكز تكلفة نشطة مرتبطة به. يرجى تعطيل جميع مراكز التكلفة أولاً. |
| **Message-EN** | Cannot deactivate Branch: active cost centers exist. Please deactivate all cost centers first. |
| **Source** | module-registry-ORG.md — RULE-ORG-04 |

---

### RULE-ORG-005 — منع تعطيل الفرع مع مواقع جغرافية نشطة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-002 |
| **Trigger** | عند طلب تعطيل Deactivate |
| **Statement** | The system MUST prevent deactivation of a Branch when one or more active LocationSites reference it |
| **Message-AR** | لا يمكن تعطيل الفرع لوجود مواقع جغرافية نشطة مرتبطة به. يرجى تعطيل جميع المواقع أولاً. |
| **Message-EN** | Cannot deactivate Branch: active location sites exist. Please deactivate all location sites first. |
| **Source** | module-registry-ORG.md — RULE-ORG-05 |

---

### RULE-ORG-006 — منع تعطيل المنطقة مع فروع نشطة تشير إليها

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-003 |
| **Trigger** | عند طلب تعطيل Deactivate |
| **Statement** | The system MUST prevent deactivation of a Region when one or more active Branches reference it |
| **Message-AR** | لا يمكن تعطيل المنطقة لوجود فروع نشطة مرتبطة بها. يرجى إلغاء ربط الفروع أولاً. |
| **Message-EN** | Cannot deactivate Region: active branches reference it. Please unlink branches first. |
| **Source** | module-registry-ORG.md — RULE-ORG-06 |
| **ملاحظة** | يرتبط بـ OQ-001: تأثير التعطيل على مستهلكي SOFT-READ غير محدد |

---

### RULE-ORG-007 — منع المرجع الدائري في شجرة الأقسام

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-004 |
| **Trigger** | عند الحفظ (Create أو Update لـ parentDepartmentFk) |
| **Statement** | The system MUST prevent circular parent references in the Department tree — a Department may not be set as its own ancestor |
| **Message-AR** | لا يمكن تعيين هذا القسم أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري. |
| **Message-EN** | Cannot set this department as parent: circular reference detected in department hierarchy. |
| **Source** | module-registry-ORG.md — RULE-ORG-07 |

---

### RULE-ORG-008 — منع المرجع الدائري في شجرة مراكز التكلفة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-005 |
| **Trigger** | عند الحفظ (Create أو Update لـ parentCostCenterFk) |
| **Statement** | The system MUST prevent circular parent references in the CostCenter tree — a CostCenter may not be set as its own ancestor |
| **Message-AR** | لا يمكن تعيين مركز التكلفة هذا أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري. |
| **Message-EN** | Cannot set this cost center as parent: circular reference detected in cost center hierarchy. |
| **Source** | module-registry-ORG.md — RULE-ORG-08 |

---

### RULE-ORG-009 — منع ربط القسم SUMMARY بسجلات معاملات

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-004 |
| **Trigger** | عند محاولة ربط قسم بسجل معاملة (يُطبَّق من الموديولات المستهلكة) |
| **Statement** | The system MUST prevent assignment of a Department with nodeType=SUMMARY to any transactional record — only DETAIL departments may be directly assigned |
| **Message-AR** | لا يمكن ربط قسم تجميعي (SUMMARY) بسجل معاملة مباشرة. يرجى اختيار قسم تفصيلي (DETAIL). |
| **Message-EN** | Cannot assign a SUMMARY department to a transaction. Please select a DETAIL department. |
| **Source** | module-registry-ORG.md — RULE-ORG-09 |
| **ملاحظة** | يُطبَّق من الموديولات المستهلكة — موثَّق هنا كمصدر حقيقة واحد |

---

### RULE-ORG-010 — منع ربط مركز التكلفة SUMMARY بسجلات معاملات

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-005 |
| **Trigger** | عند محاولة ربط مركز تكلفة بسجل معاملة (يُطبَّق من الموديولات المستهلكة) |
| **Statement** | The system MUST prevent assignment of a CostCenter with nodeType=SUMMARY to any transactional record — only DETAIL cost centers may be directly assigned |
| **Message-AR** | لا يمكن ربط مركز تكلفة تجميعي (SUMMARY) بسجل معاملة مباشرة. يرجى اختيار مركز تكلفة تفصيلي (DETAIL). |
| **Message-EN** | Cannot assign a SUMMARY cost center to a transaction. Please select a DETAIL cost center. |
| **Source** | module-registry-ORG.md — RULE-ORG-10 |
| **ملاحظة** | يُطبَّق من الموديولات المستهلكة — موثَّق هنا كمصدر حقيقة واحد |

---

### RULE-ORG-011 — ثبات رموز الأعمال بعد الحفظ الأول

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-001, 002, 003, 004, 005, 006, 007 |
| **Trigger** | عند محاولة تعديل أي حقل Business Code |
| **Statement** | The system MUST prevent any modification to business codes (legalEntityCode, branchCode, regionCode, deptCode, costCenterCode, profitCenterCode, locationCode) after their initial creation — they are permanently immutable |
| **Message-AR** | رمز الأعمال لا يمكن تعديله بعد الإنشاء الأول — هذه القيمة ثابتة نهائياً. |
| **Message-EN** | Business code is immutable after creation and cannot be modified. |
| **Source** | module-registry-ORG.md — RULE-ORG-11 / master-registry Section 4 |
| **Test-Hint** | تحقق من أن حقل الرمز غير موجود في Update DTO بالكلية — لا مجرد validation |

---

### RULE-ORG-012 — فريد رموز الأعمال ضمن النطاق المحدد

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-001, 002, 003, 004, 005, 006, 007 |
| **Trigger** | عند الحفظ الأول (Create) |
| **Statement** | The system MUST ensure business codes are unique within their defined scope: LegalEntity globally, Branch per LegalEntity, Department per Branch, CostCenter per Branch, ProfitCenter per LegalEntity, Region per LegalEntity, LocationSite per Branch |
| **Message-AR** | رمز الأعمال المُنشأ تلقائياً موجود مسبقاً. يرجى المحاولة مجدداً. |
| **Message-EN** | Generated business code already exists. Please retry the operation. |
| **Source** | module-registry-ORG.md — RULE-ORG-12 |
| **ملاحظة** | يتولى NumberingEngine ضمان التفرد — هذه القاعدة تُوثّق السلوك المتوقع |

---

### RULE-ORG-013 — إلزامية اختيار الكيان القانوني عند إنشاء الفرع

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-002 |
| **Trigger** | عند الحفظ (Create) |
| **Statement** | The system MUST require a valid active LegalEntity reference before saving a new Branch |
| **Message-AR** | يرجى اختيار كيان قانوني نشط لربط الفرع به. |
| **Message-EN** | A valid active Legal Entity must be selected before saving a Branch. |
| **Source** | علاقة ENTITY-ORG-001 → ENTITY-ORG-002 (NOT NULL) |

---

### RULE-ORG-014 — إلزامية اختيار الفرع عند إنشاء القسم

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-004 |
| **Trigger** | عند الحفظ (Create) |
| **Statement** | The system MUST require a valid active Branch reference before saving a new Department |
| **Message-AR** | يرجى اختيار فرع نشط لربط القسم به. |
| **Message-EN** | A valid active Branch must be selected before saving a Department. |
| **Source** | علاقة ENTITY-ORG-002 → ENTITY-ORG-004 (NOT NULL) |

---

### RULE-ORG-015 — إلزامية اختيار الفرع عند إنشاء مركز التكلفة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-005 |
| **Trigger** | عند الحفظ (Create) |
| **Statement** | The system MUST require a valid active Branch reference before saving a new CostCenter |
| **Message-AR** | يرجى اختيار فرع نشط لربط مركز التكلفة به. |
| **Message-EN** | A valid active Branch must be selected before saving a CostCenter. |
| **Source** | علاقة ENTITY-ORG-002 → ENTITY-ORG-005 (NOT NULL) |

---

### RULE-ORG-016 — توليد رموز الأعمال حصراً عبر NumberingEngine

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-001, 002, 003, 004, 005, 006, 007 |
| **Trigger** | عند الحفظ الأول (Create) |
| **Statement** | The system MUST generate all business codes exclusively through NumberingEngine — no module may implement its own numbering logic |
| **Message-AR** | — (قاعدة بنية — لا رسالة للمستخدم) |
| **Message-EN** | — (Architectural rule — no user-facing message) |
| **Source** | master-registry Section 8 — NUMBERING RULES / BC-RULE-6 |

---

### RULE-ORG-017 — منع القسم الأب من كونه قسماً غير نشط

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-004 |
| **Trigger** | عند الحفظ (Create أو Update لـ parentDepartmentFk) |
| **Statement** | The system MUST prevent assigning an inactive Department as the parent of another Department |
| **Message-AR** | لا يمكن تعيين قسم غير نشط أباً للقسم. يرجى اختيار قسم نشط. |
| **Message-EN** | Cannot set an inactive department as parent. Please select an active department. |
| **Source** | قاعدة سلامة الشجرة — Section M.1 |

---

### RULE-ORG-018 — منع مركز التكلفة الأب من كونه غير نشط

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-005 |
| **Trigger** | عند الحفظ (Create أو Update لـ parentCostCenterFk) |
| **Statement** | The system MUST prevent assigning an inactive CostCenter as the parent of another CostCenter |
| **Message-AR** | لا يمكن تعيين مركز تكلفة غير نشط أباً لمركز التكلفة. يرجى اختيار مركز تكلفة نشط. |
| **Message-EN** | Cannot set an inactive cost center as parent. Please select an active cost center. |
| **Source** | قاعدة سلامة الشجرة — Section M.1 |

---

### RULE-ORG-019 — إلزامية اختيار الكيان القانوني عند إنشاء المنطقة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-003 |
| **Trigger** | عند الحفظ (Create) |
| **Statement** | The system MUST require a valid active LegalEntity reference before saving a new Region |
| **Message-AR** | يرجى اختيار كيان قانوني نشط لربط المنطقة به. |
| **Message-EN** | A valid active Legal Entity must be selected before saving a Region. |
| **Source** | علاقة ENTITY-ORG-001 → ENTITY-ORG-003 (NOT NULL) |

---

### RULE-ORG-020 — إلزامية اختيار الكيان القانوني عند إنشاء مركز الربح

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-ORG-006 |
| **Trigger** | عند الحفظ (Create) |
| **Statement** | The system MUST require a valid active LegalEntity reference before saving a new ProfitCenter |
| **Message-AR** | يرجى اختيار كيان قانوني نشط لربط مركز الربح به. |
| **Message-EN** | A valid active Legal Entity must be selected before saving a ProfitCenter. |
| **Source** | علاقة ENTITY-ORG-001 → ENTITY-ORG-006 (NOT NULL) |

---

## A5 — قوائم القيم (LOV / Lookup)

> **قاعدة إلزامية:** هذا القسم هو المصدر الوحيد لتعريف LOVs.
> PART B يُشير للـ LOVs بـ LOV-ID أو lookupKey فقط — لا يُعيد تعريفها.
> الاستهلاك: `GET /api/lookups/{lookupKey}?active=true`

---

### LOV-ORG-001 — LEGAL_ENTITY_TYPE (نوع الكيان القانوني)

| البند | القيمة |
|---|---|
| **الحقل** | LegalEntity.entityTypeId |
| **ENTITY-ID** | ENTITY-ORG-001 |
| **نوع التحكم** | Dropdown (4 قيم ≤ 15) |
| **lookupKey** | LEGAL_ENTITY_TYPE |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Organization |
| **API الاستهلاك** | GET /api/lookups/LEGAL_ENTITY_TYPE?active=true |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| HEAD_OFFICE | المقر الرئيسي | Head Office |
| BRANCH_OFFICE | مكتب فرعي | Branch Office |
| SUBSIDIARY | شركة تابعة | Subsidiary |
| REPRESENTATIVE_OFFICE | مكتب تمثيل | Representative Office |

> ⚠ القيمة المُخزَّنة في LegalEntity.entityTypeId: code من MD_LOOKUP_DETAIL (مثال: "HEAD_OFFICE")

---

### LOV-ORG-002 — BRANCH_TYPE (نوع الفرع)

| البند | القيمة |
|---|---|
| **الحقل** | Branch.branchTypeId |
| **ENTITY-ID** | ENTITY-ORG-002 |
| **نوع التحكم** | Dropdown (4 قيم ≤ 15) |
| **lookupKey** | BRANCH_TYPE |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Organization |
| **API الاستهلاك** | GET /api/lookups/BRANCH_TYPE?active=true |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| MAIN_BRANCH | الفرع الرئيسي | Main Branch |
| SUB_BRANCH | فرع فرعي | Sub-Branch |
| OPERATIONS_BRANCH | فرع عمليات | Operations Branch |
| ADMIN_BRANCH | فرع إداري | Admin Branch |

> ⚠ القيمة المُخزَّنة في Branch.branchTypeId: code من MD_LOOKUP_DETAIL

---

### LOV-ORG-003 — DEPARTMENT_NODE_TYPE (نوع عقدة القسم)

| البند | القيمة |
|---|---|
| **الحقل** | Department.nodeTypeId |
| **ENTITY-ID** | ENTITY-ORG-004 |
| **نوع التحكم** | Dropdown (2 قيم ≤ 15) |
| **lookupKey** | DEPARTMENT_NODE_TYPE |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Organization |
| **API الاستهلاك** | GET /api/lookups/DEPARTMENT_NODE_TYPE?active=true |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| SUMMARY | تجميعي | Summary |
| DETAIL | تفصيلي | Detail |

---

### LOV-ORG-004 — COST_CENTER_NODE_TYPE (نوع عقدة مركز التكلفة)

| البند | القيمة |
|---|---|
| **الحقل** | CostCenter.nodeTypeId |
| **ENTITY-ID** | ENTITY-ORG-005 |
| **نوع التحكم** | Dropdown (2 قيم ≤ 15) |
| **lookupKey** | COST_CENTER_NODE_TYPE |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Organization |
| **API الاستهلاك** | GET /api/lookups/COST_CENTER_NODE_TYPE?active=true |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| SUMMARY | تجميعي | Summary |
| DETAIL | تفصيلي | Detail |

---

### LOV-ORG-005 — COST_CENTER_TYPE (نوع مركز التكلفة)

| البند | القيمة |
|---|---|
| **الحقل** | CostCenter.costCenterTypeId |
| **ENTITY-ID** | ENTITY-ORG-005 |
| **نوع التحكم** | Dropdown (3 قيم ≤ 15) |
| **lookupKey** | COST_CENTER_TYPE |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Organization |
| **API الاستهلاك** | GET /api/lookups/COST_CENTER_TYPE?active=true |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| DIRECT | مباشر | Direct |
| INDIRECT | غير مباشر | Indirect |
| SHARED | مشترك | Shared |

---

### LOV-ORG-006 — LOCATION_SITE_TYPE (نوع الموقع الجغرافي)

| البند | القيمة |
|---|---|
| **الحقل** | LocationSite.siteTypeId |
| **ENTITY-ID** | ENTITY-ORG-007 |
| **نوع التحكم** | Dropdown (5 قيم ≤ 15) |
| **lookupKey** | LOCATION_SITE_TYPE |
| **المصدر** | MD_LOOKUP_DETAIL |
| **المالك** | Organization |
| **API الاستهلاك** | GET /api/lookups/LOCATION_SITE_TYPE?active=true |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| OFFICE | مكتب | Office |
| WAREHOUSE | مستودع | Warehouse |
| FACTORY | مصنع | Factory |
| SITE | موقع | Site |
| RETAIL | منفذ بيع | Retail |

---

### LOV-ORG-007 — REGION_TYPE (Reference Table)

| البند | القيمة |
|---|---|
| **الحقل** | Region.regionTypeId |
| **ENTITY-ID** | ENTITY-ORG-003 |
| **نوع التحكم** | LOV من Reference Table (> 15 قيمة متوقعة) |
| **جدول DB** | ORG_REGION_TYPE (ENTITY-ORG-008) |
| **المالك** | Organization |
| **API الاستهلاك** | GET /api/v1/org/region-types?active=true |

> ⚠ ليس MD_LOOKUP_DETAIL — هذا Reference Table مستقل يُدار بواسطة Admin
> القيم الأولية: GEOGRAPHIC / SALES / OPERATIONAL — قابل للتوسيع

---

## A6 — دورة الحالة (Status Lifecycle)

> **حسب RULE-13 وSection M.1:** جميع الكيانات السبعة تستخدم isActiveFl حصراً.
> حالتان فقط: نشط (1) ↔ غير نشط (0).
> لا lifecycle معقد — لا workflow — لا approval flow.

```
[نشط — isActiveFl=1] ◄──── إعادة تفعيل (Reactivate)
         │
         │ تعطيل (Deactivate)
         │ [محكوم بـ RULE-ORG-001 حتى RULE-ORG-006]
         ▼
[غير نشط — isActiveFl=0]
```

> ينطبق على: ENTITY-ORG-001, 002, 003, 004, 005, 006, 007, 008
> منع التعطيل عند وجود تبعيات نشطة محكوم بـ RULE-ORG-001 إلى RULE-ORG-006.

---

## A7 — تبعيات الموديولات (Module Dependencies)

> **ملاحظة:** Organization هو الموديول الجذري (ROOT) — لا تبعيات خارجية صادرة.
> XM-IDs تُعيَّن في MODE 1.5 من الموديولات المستهلكة — ليس هنا.
> هذا الجدول يوضّح من يستهلك Organization (لأغراض التوثيق وإشعار ARCH-8).

### الموديولات المستهلكة لكيانات Organization

| الموديول المستهلك | الكيانات المستهلكة | نوع الاعتمادية | ملاحظات |
|---|---|---|---|
| Security (1.2) | Branch | HARD-FK | SEC_ROLE_BRANCH (M:M) |
| MasterData (1.4) | LegalEntity, Branch | HARD-FK | — |
| CurrencyCalendar (1.5) | LegalEntity | HARD-FK | نطاق السنة المالية |
| NumberingEngine (1.6) | Branch | HARD-FK | نطاق الترقيم |
| FileService (1.10) | Branch | HARD-FK | نطاق البيانات |
| NotificationService (1.8) | Branch | HARD-FK | نطاق البيانات |
| AuditService (1.9) | Branch | HARD-FK | نطاق البيانات |
| جميع موديولات Layer-2 | Branch, CostCenter | HARD-FK | السياق التشغيلي |
| جميع موديولات Layer-3 | Branch, Department, CostCenter, ProfitCenter | HARD-FK | — |
| Inventory (3.2) | LocationSite | HARD-FK | سياق المستودع |
| Finance (3.4) | CostCenter, ProfitCenter | HARD-FK | — |
| TBD (مستهلكو Region) | Region | SOFT-READ | AQ-003 — مؤجل |

### ARCH-8 — Auto-raise OQ

> **تطبيقاً لـ ARCH-8:** Organization يملك APIs للتعطيل لجميع الكيانات السبعة.
> Region مصنّف SHARED (owner) وله مستهلكون عبر SOFT-READ غير محددين (AQ-003).
> OQ-001 مُرفَع تلقائياً — انظر OQ Log.

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

## SCR-ORG-001 — إدارة الكيانات القانونية

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-001 |
| **اسم الشاشة** | إدارة الكيانات القانونية |
| **UI Pattern** | PATTERN-1 — Search + Entry |
| **Pattern Reason** | كيان رئيسي بـ 7+ حقول + مُستهلَك من جميع الموديولات — يستوجب PATTERN-1 |
| **SCR-ID Scope** | ONE SCR-ID covers: Search + Entry (CORE-9) |
| **P3 Implication** | Two-screen UX navigation — P3 determines component names in F1 |
| **ENTITY-ID** | ENTITY-ORG-001 |
| **وظيفة الشاشة** | إنشاء وإدارة الكيانات القانونية للمؤسسة |
| **المستخدمون** | مدير النظام، مدير التنظيم |
| **الموضع في النظام** | التنظيم المؤسسي ← الكيانات القانونية |
| **روابط من** | القائمة الرئيسية — التنظيم المؤسسي |
| **روابط إلى** | إدارة الفروع (SCR-ORG-002)، مراكز الربح (SCR-ORG-006) |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| legalEntityCode | نص | لا | — | بحث جزئي |
| nameAr | نص | لا | — | بحث جزئي |
| nameEn | نص | لا | — | بحث جزئي |
| entityTypeId | قائمة منسدلة | لا | LOV-ORG-001 → A5 | lookupKey: LEGAL_ENTITY_TYPE |
| isActiveFl | قائمة منسدلة | لا | نشط / غير نشط | افتراضي: نشط |

#### أعمدة نتائج البحث

| العمود | المصدر |
|---|---|
| legalEntityCode | ENTITY-ORG-001 → A3 |
| nameAr | ENTITY-ORG-001 → A3 |
| nameEn | ENTITY-ORG-001 → A3 |
| entityTypeId (معروض) | LOV-ORG-001 → A5 |
| isActiveFl | ENTITY-ORG-001 → A3 |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_LEGAL_ENTITY_CREATE |
| Edit | عند تحديد سجل | PERM_LEGAL_ENTITY_UPDATE |
| Deactivate | عند تحديد سجل نشط | PERM_LEGAL_ENTITY_DELETE |
| Reactivate | عند تحديد سجل غير نشط | PERM_LEGAL_ENTITY_UPDATE |

#### قواعد البحث المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| RULE-ORG-011 | لا يُعرض حقل legalEntityCode للتعديل | ← see A4 |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| legalEntityCode | نص (Read-Only) | نظام | ENTITY-ORG-001 → A3 | يُنشأ تلقائياً — لا يُقبل في Create DTO |
| nameAr | نص | نعم | ENTITY-ORG-001 → A3 | |
| nameEn | نص | نعم | ENTITY-ORG-001 → A3 | |
| entityTypeId | قائمة منسدلة | نعم | LOV-ORG-001 → A5 | lookupKey: LEGAL_ENTITY_TYPE |
| isActiveFl | لا يُعرض في Create | — | — | يبدأ نشطاً تلقائياً |
| notes | نص | لا | ENTITY-ORG-001 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (جديد) | POST | RULE-ORG-012, RULE-ORG-016 — *(تفاصيل في A4)* |
| حفظ (تعديل) | PUT | RULE-ORG-011, RULE-ORG-012 — *(تفاصيل في A4)* |
| تعطيل | DELETE (soft) | RULE-ORG-001, RULE-ORG-002 — *(تفاصيل في A4)* |
| إعادة تفعيل | PUT (reactivate) | — |
| إلغاء | navigation back | — |

---

### B4 — الصلاحيات (Permissions)

> **CORE-9:** هذه الشاشة المركبة = SCR-ID واحد = صف واحد في SEC_PAGES.
> **SEC-3:** Security Engine يُنشئ PERM_* تلقائياً من PAGE_CODE — لا seed data هنا.
> **ملاحظة INF-ORG-01:** PERM_LEGAL_ENTITY_VIEW مُنشأ مسبقاً في SEC_PAGES — لا يُكرَّر.

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف/تعطيل (DELETE) |
|---|---|---|---|---|
| SCR-ORG-001 | مدير النظام، مدير التنظيم | مدير النظام | مدير النظام | مدير النظام |

**Security Engine Declaration:**
```
SEC_PAGES  : page_code = LEGAL_ENTITY, module = ORGANIZATION, parent_id_fk = [ORG_MENU]
PERMISSIONS: يُنشئها Security Engine × 4 تلقائياً من PAGE_CODE
```

---

### B5 — الواجهات البرمجية (Functional APIs)

> **Stack Rule (CORE-8):** جميع APIs تتبع Spring Boot REST conventions.
> مسار موحّد: `/api/v1/org/legal-entities`

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-001 | إنشاء كيان قانوني | POST | /api/v1/org/legal-entities | nameAr, nameEn, entityTypeId, notes? | LegalEntity كامل | RULE-ORG-012, RULE-ORG-016 |
| API-ORG-002 | بحث في الكيانات | GET | /api/v1/org/legal-entities | code?, nameAr?, entityTypeId?, isActiveFl?, page, size | قائمة LegalEntity | — |
| API-ORG-003 | جلب كيان بالمعرف | GET | /api/v1/org/legal-entities/{id} | legalEntityPk | LegalEntity كامل | — |
| API-ORG-004 | تعديل كيان قانوني | PUT | /api/v1/org/legal-entities/{id} | nameAr?, nameEn?, entityTypeId?, notes? | LegalEntity محدَّث | RULE-ORG-011 |
| API-ORG-005 | تعطيل كيان قانوني | DELETE | /api/v1/org/legal-entities/{id} | legalEntityPk | تأكيد | RULE-ORG-001, RULE-ORG-002 |
| API-ORG-006 | إعادة تفعيل كيان | PUT | /api/v1/org/legal-entities/{id}/reactivate | legalEntityPk | LegalEntity محدَّث | — |

---

## SCR-ORG-002 — إدارة الفروع

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-002 |
| **اسم الشاشة** | إدارة الفروع |
| **UI Pattern** | PATTERN-1 — Search + Entry |
| **Pattern Reason** | كيان رئيسي بـ 7+ حقول + مُستهلَك من جميع الموديولات — يستوجب PATTERN-1 |
| **SCR-ID Scope** | ONE SCR-ID covers: Search + Entry (CORE-9) |
| **P3 Implication** | Two-screen UX navigation — P3 determines component names in F1 |
| **ENTITY-ID** | ENTITY-ORG-002 |
| **وظيفة الشاشة** | إنشاء وإدارة الفروع ضمن الكيانات القانونية |
| **المستخدمون** | مدير النظام، مدير التنظيم |
| **الموضع في النظام** | التنظيم المؤسسي ← الفروع |
| **روابط من** | القائمة الرئيسية — التنظيم المؤسسي |
| **روابط إلى** | إدارة الأقسام (SCR-ORG-004)، مراكز التكلفة (SCR-ORG-005)، المواقع (SCR-ORG-007) |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر البحث

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| branchCode | نص | لا | — | بحث جزئي |
| nameAr | نص | لا | — | بحث جزئي |
| legalEntityFk | LOV | لا | ENTITY-ORG-001 → A3 | اختيار من قائمة الكيانات النشطة |
| branchTypeId | قائمة منسدلة | لا | LOV-ORG-002 → A5 | lookupKey: BRANCH_TYPE |
| isActiveFl | قائمة منسدلة | لا | نشط / غير نشط | افتراضي: نشط |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_BRANCH_CREATE |
| Edit | عند تحديد سجل | PERM_BRANCH_UPDATE |
| Deactivate | عند تحديد سجل نشط | PERM_BRANCH_DELETE |
| Reactivate | عند تحديد سجل غير نشط | PERM_BRANCH_UPDATE |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| branchCode | نص (Read-Only) | نظام | ENTITY-ORG-002 → A3 | يُنشأ تلقائياً |
| nameAr | نص | نعم | ENTITY-ORG-002 → A3 | |
| nameEn | نص | نعم | ENTITY-ORG-002 → A3 | |
| legalEntityFk | LOV | نعم | ENTITY-ORG-001 → A3 | اختيار الكيان القانوني (نشط فقط) |
| branchTypeId | قائمة منسدلة | نعم | LOV-ORG-002 → A5 | lookupKey: BRANCH_TYPE |
| notes | نص | لا | ENTITY-ORG-002 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (جديد) | POST | RULE-ORG-013, RULE-ORG-012, RULE-ORG-016 — *(تفاصيل في A4)* |
| حفظ (تعديل) | PUT | RULE-ORG-011 — *(تفاصيل في A4)* |
| تعطيل | DELETE (soft) | RULE-ORG-003, RULE-ORG-004, RULE-ORG-005 — *(تفاصيل في A4)* |
| إعادة تفعيل | PUT (reactivate) | — |

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف/تعطيل (DELETE) |
|---|---|---|---|---|
| SCR-ORG-002 | مدير النظام، مدير التنظيم | مدير النظام | مدير النظام | مدير النظام |

**Security Engine Declaration:**
```
SEC_PAGES  : page_code = BRANCH, module = ORGANIZATION, parent_id_fk = [ORG_MENU]
PERMISSIONS: يُنشئها Security Engine × 4 تلقائياً
```

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-007 | إنشاء فرع | POST | /api/v1/org/branches | nameAr, nameEn, legalEntityFk, branchTypeId, notes? | Branch كامل | RULE-ORG-013, RULE-ORG-012, RULE-ORG-016 |
| API-ORG-008 | بحث في الفروع | GET | /api/v1/org/branches | code?, nameAr?, legalEntityFk?, branchTypeId?, isActiveFl?, page, size | قائمة Branch | — |
| API-ORG-009 | جلب فرع بالمعرف | GET | /api/v1/org/branches/{id} | branchPk | Branch كامل | — |
| API-ORG-010 | تعديل فرع | PUT | /api/v1/org/branches/{id} | nameAr?, nameEn?, branchTypeId?, notes? | Branch محدَّث | RULE-ORG-011 |
| API-ORG-011 | تعطيل فرع | DELETE | /api/v1/org/branches/{id} | branchPk | تأكيد | RULE-ORG-003, RULE-ORG-004, RULE-ORG-005 |
| API-ORG-012 | إعادة تفعيل فرع | PUT | /api/v1/org/branches/{id}/reactivate | branchPk | Branch محدَّث | — |
| API-ORG-013 | فروع حسب الكيان القانوني | GET | /api/v1/org/branches/by-legal-entity/{leId} | legalEntityFk, isActiveFl? | قائمة Branch | — |

---

## SCR-ORG-003 — إدارة المناطق

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-003 |
| **اسم الشاشة** | إدارة المناطق |
| **UI Pattern** | PATTERN-1 — Search + Entry |
| **Pattern Reason** | كيان رئيسي بـ 5+ حقول + LOV مرجعية — يستوجب PATTERN-1 |
| **SCR-ID Scope** | ONE SCR-ID covers: Search + Entry (CORE-9) |
| **P3 Implication** | Two-screen UX navigation — P3 determines component names in F1 |
| **ENTITY-ID** | ENTITY-ORG-003 |
| **وظيفة الشاشة** | إنشاء وإدارة المناطق الجغرافية والتشغيلية |
| **المستخدمون** | مدير النظام، مدير التنظيم |
| **الموضع في النظام** | التنظيم المؤسسي ← المناطق |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر البحث

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| regionCode | نص | لا | — | بحث جزئي |
| nameAr | نص | لا | — | بحث جزئي |
| legalEntityFk | LOV | لا | ENTITY-ORG-001 → A3 | |
| regionTypeId | LOV | لا | LOV-ORG-007 → A5 | GET /api/v1/org/region-types |
| isActiveFl | قائمة منسدلة | لا | نشط / غير نشط | افتراضي: نشط |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_REGION_CREATE |
| Edit | عند تحديد سجل | PERM_REGION_UPDATE |
| Deactivate | عند تحديد سجل نشط | PERM_REGION_DELETE |
| Reactivate | عند تحديد سجل غير نشط | PERM_REGION_UPDATE |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| regionCode | نص (Read-Only) | نظام | ENTITY-ORG-003 → A3 | يُنشأ تلقائياً |
| nameAr | نص | نعم | ENTITY-ORG-003 → A3 | |
| nameEn | نص | نعم | ENTITY-ORG-003 → A3 | |
| legalEntityFk | LOV | نعم | ENTITY-ORG-001 → A3 | نشط فقط |
| regionTypeId | LOV | نعم | LOV-ORG-007 → A5 | Reference Table |
| notes | نص | لا | ENTITY-ORG-003 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (جديد) | POST | RULE-ORG-019, RULE-ORG-012, RULE-ORG-016 — *(تفاصيل في A4)* |
| حفظ (تعديل) | PUT | RULE-ORG-011 — *(تفاصيل في A4)* |
| تعطيل | DELETE (soft) | RULE-ORG-006 — *(تفاصيل في A4)* |

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف/تعطيل (DELETE) |
|---|---|---|---|---|
| SCR-ORG-003 | مدير النظام، مدير التنظيم | مدير النظام | مدير النظام | مدير النظام |

**Security Engine Declaration:**
```
SEC_PAGES  : page_code = REGION, module = ORGANIZATION, parent_id_fk = [ORG_MENU]
PERMISSIONS: يُنشئها Security Engine × 4 تلقائياً
```

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-014 | إنشاء منطقة | POST | /api/v1/org/regions | nameAr, nameEn, legalEntityFk, regionTypeId, notes? | Region كامل | RULE-ORG-019, RULE-ORG-012, RULE-ORG-016 |
| API-ORG-015 | بحث في المناطق | GET | /api/v1/org/regions | code?, nameAr?, legalEntityFk?, regionTypeId?, isActiveFl?, page, size | قائمة Region | — |
| API-ORG-016 | جلب منطقة بالمعرف | GET | /api/v1/org/regions/{id} | regionPk | Region كامل | — |
| API-ORG-017 | تعديل منطقة | PUT | /api/v1/org/regions/{id} | nameAr?, nameEn?, regionTypeId?, notes? | Region محدَّث | RULE-ORG-011 |
| API-ORG-018 | تعطيل منطقة | DELETE | /api/v1/org/regions/{id} | regionPk | تأكيد | RULE-ORG-006 |
| API-ORG-019 | إعادة تفعيل منطقة | PUT | /api/v1/org/regions/{id}/reactivate | regionPk | Region محدَّث | — |
| API-ORG-020 | جلب قائمة أنواع المناطق | GET | /api/v1/org/region-types | isActiveFl? | قائمة RegionType | — |

---

## SCR-ORG-004 — إدارة الأقسام

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-004 |
| **اسم الشاشة** | إدارة الأقسام |
| **UI Pattern** | PATTERN-3 — Specialized (Hierarchical Tree) |
| **Pattern Reason** | كيان شجري ذو مرجع ذاتي — يستوجب عرضاً شجرياً متخصصاً |
| **SCR-ID Scope** | ONE SCR-ID covers: Tree Navigation + Entry (CORE-9) |
| **P3 Implication** | Specialized tree UI — P3 determines tree component and entry panel in F1 |
| **ENTITY-ID** | ENTITY-ORG-004 |
| **وظيفة الشاشة** | إنشاء وإدارة هيكل الأقسام الشجري ضمن الفروع |
| **المستخدمون** | مدير النظام، مدير التنظيم |
| **الموضع في النظام** | التنظيم المؤسسي ← الأقسام |

**Specialized Layout Description:**
| البند | القيمة |
|---|---|
| نوع الشاشة | Hierarchical Tree + Entry Panel |
| مبرر الاستثناء | الأقسام ذات هيكل شجري متعدد المستويات — PATTERN-1 لا يكفي |
| المكونات الخاصة | Tree Explorer (يسار) + Entry Form (يمين/modal) + SUMMARY/DETAIL visual indicator |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر تصفية الشجرة

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| branchFk | LOV | نعم | ENTITY-ORG-002 → A3 | تصفية الشجرة حسب الفرع |
| nameAr | نص | لا | — | بحث جزئي |
| nodeTypeId | قائمة منسدلة | لا | LOV-ORG-003 → A5 | SUMMARY / DETAIL |
| isActiveFl | قائمة منسدلة | لا | نشط / غير نشط | افتراضي: نشط |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_DEPARTMENT_CREATE |
| Edit | عند تحديد عقدة | PERM_DEPARTMENT_UPDATE |
| Deactivate | عند تحديد عقدة نشطة | PERM_DEPARTMENT_DELETE |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| deptCode | نص (Read-Only) | نظام | ENTITY-ORG-004 → A3 | يُنشأ تلقائياً |
| nameAr | نص | نعم | ENTITY-ORG-004 → A3 | |
| nameEn | نص | نعم | ENTITY-ORG-004 → A3 | |
| branchFk | LOV | نعم | ENTITY-ORG-002 → A3 | نشط فقط |
| parentDepartmentFk | LOV | لا | ENTITY-ORG-004 → A3 | نشط فقط — يُمنع الدائري (RULE-ORG-007) |
| nodeTypeId | قائمة منسدلة | نعم | LOV-ORG-003 → A5 | SUMMARY / DETAIL |
| notes | نص | لا | ENTITY-ORG-004 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (جديد) | POST | RULE-ORG-014, RULE-ORG-007, RULE-ORG-017, RULE-ORG-012, RULE-ORG-016 |
| حفظ (تعديل) | PUT | RULE-ORG-011, RULE-ORG-007, RULE-ORG-017 |
| تعطيل | DELETE (soft) | — (لا أبناء نشطة كقيد مُفترَض — يُوثَّق في P3) |

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف/تعطيل (DELETE) |
|---|---|---|---|---|
| SCR-ORG-004 | مدير النظام، مدير التنظيم | مدير النظام | مدير النظام | مدير النظام |

**Security Engine Declaration:**
```
SEC_PAGES  : page_code = DEPARTMENT, module = ORGANIZATION, parent_id_fk = [ORG_MENU]
PERMISSIONS: يُنشئها Security Engine × 4 تلقائياً
```

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-021 | إنشاء قسم | POST | /api/v1/org/departments | nameAr, nameEn, branchFk, nodeTypeId, parentDepartmentFk?, notes? | Department كامل | RULE-ORG-014, RULE-ORG-007, RULE-ORG-017, RULE-ORG-012, RULE-ORG-016 |
| API-ORG-022 | جلب شجرة الأقسام | GET | /api/v1/org/departments/tree | branchFk, isActiveFl? | Department Tree | — |
| API-ORG-023 | بحث في الأقسام | GET | /api/v1/org/departments | branchFk?, nameAr?, nodeTypeId?, isActiveFl?, page, size | قائمة Department | — |
| API-ORG-024 | جلب قسم بالمعرف | GET | /api/v1/org/departments/{id} | departmentPk | Department كامل | — |
| API-ORG-025 | تعديل قسم | PUT | /api/v1/org/departments/{id} | nameAr?, nameEn?, nodeTypeId?, parentDepartmentFk?, notes? | Department محدَّث | RULE-ORG-011, RULE-ORG-007, RULE-ORG-017 |
| API-ORG-026 | تعطيل قسم | DELETE | /api/v1/org/departments/{id} | departmentPk | تأكيد | — |
| API-ORG-027 | إعادة تفعيل قسم | PUT | /api/v1/org/departments/{id}/reactivate | departmentPk | Department محدَّث | — |

---

## SCR-ORG-005 — إدارة مراكز التكلفة

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-005 |
| **اسم الشاشة** | إدارة مراكز التكلفة |
| **UI Pattern** | PATTERN-3 — Specialized (Hierarchical Tree) |
| **Pattern Reason** | كيان شجري ذو مرجع ذاتي — يستوجب عرضاً شجرياً متخصصاً |
| **SCR-ID Scope** | ONE SCR-ID covers: Tree Navigation + Entry (CORE-9) |
| **P3 Implication** | Specialized tree UI — P3 determines tree component and entry panel in F1 |
| **ENTITY-ID** | ENTITY-ORG-005 |
| **وظيفة الشاشة** | إنشاء وإدارة هيكل مراكز التكلفة الشجري ضمن الفروع |
| **المستخدمون** | مدير النظام، مدير التنظيم، مدير المالية |
| **الموضع في النظام** | التنظيم المؤسسي ← مراكز التكلفة |

**Specialized Layout Description:**
| البند | القيمة |
|---|---|
| نوع الشاشة | Hierarchical Tree + Entry Panel |
| مبرر الاستثناء | مراكز التكلفة ذات هيكل شجري + حقل نوع إضافي (Direct/Indirect/Shared) |
| المكونات الخاصة | Tree Explorer + Entry Form + cost center type indicator |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر تصفية الشجرة

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| branchFk | LOV | نعم | ENTITY-ORG-002 → A3 | تصفية الشجرة حسب الفرع |
| nameAr | نص | لا | — | بحث جزئي |
| nodeTypeId | قائمة منسدلة | لا | LOV-ORG-004 → A5 | SUMMARY / DETAIL |
| costCenterTypeId | قائمة منسدلة | لا | LOV-ORG-005 → A5 | Direct / Indirect / Shared |
| isActiveFl | قائمة منسدلة | لا | نشط / غير نشط | |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_COST_CENTER_CREATE |
| Edit | عند تحديد عقدة | PERM_COST_CENTER_UPDATE |
| Deactivate | عند تحديد عقدة نشطة | PERM_COST_CENTER_DELETE |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| costCenterCode | نص (Read-Only) | نظام | ENTITY-ORG-005 → A3 | يُنشأ تلقائياً |
| nameAr | نص | نعم | ENTITY-ORG-005 → A3 | |
| nameEn | نص | نعم | ENTITY-ORG-005 → A3 | |
| branchFk | LOV | نعم | ENTITY-ORG-002 → A3 | نشط فقط |
| parentCostCenterFk | LOV | لا | ENTITY-ORG-005 → A3 | نشط فقط — يُمنع الدائري (RULE-ORG-008) |
| nodeTypeId | قائمة منسدلة | نعم | LOV-ORG-004 → A5 | SUMMARY / DETAIL |
| costCenterTypeId | قائمة منسدلة | نعم | LOV-ORG-005 → A5 | Direct / Indirect / Shared |
| notes | نص | لا | ENTITY-ORG-005 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (جديد) | POST | RULE-ORG-015, RULE-ORG-008, RULE-ORG-018, RULE-ORG-012, RULE-ORG-016 |
| حفظ (تعديل) | PUT | RULE-ORG-011, RULE-ORG-008, RULE-ORG-018 |
| تعطيل | DELETE (soft) | — |

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف/تعطيل (DELETE) |
|---|---|---|---|---|
| SCR-ORG-005 | مدير النظام، مدير التنظيم، مدير المالية | مدير النظام | مدير النظام | مدير النظام |

**Security Engine Declaration:**
```
SEC_PAGES  : page_code = COST_CENTER, module = ORGANIZATION, parent_id_fk = [ORG_MENU]
PERMISSIONS: يُنشئها Security Engine × 4 تلقائياً
```

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-028 | إنشاء مركز تكلفة | POST | /api/v1/org/cost-centers | nameAr, nameEn, branchFk, nodeTypeId, costCenterTypeId, parentCostCenterFk?, notes? | CostCenter كامل | RULE-ORG-015, RULE-ORG-008, RULE-ORG-018, RULE-ORG-012, RULE-ORG-016 |
| API-ORG-029 | جلب شجرة مراكز التكلفة | GET | /api/v1/org/cost-centers/tree | branchFk, isActiveFl? | CostCenter Tree | — |
| API-ORG-030 | بحث في مراكز التكلفة | GET | /api/v1/org/cost-centers | branchFk?, nameAr?, nodeTypeId?, costCenterTypeId?, isActiveFl?, page, size | قائمة CostCenter | — |
| API-ORG-031 | جلب مركز تكلفة بالمعرف | GET | /api/v1/org/cost-centers/{id} | costCenterPk | CostCenter كامل | — |
| API-ORG-032 | تعديل مركز تكلفة | PUT | /api/v1/org/cost-centers/{id} | nameAr?, nameEn?, nodeTypeId?, costCenterTypeId?, parentCostCenterFk?, notes? | CostCenter محدَّث | RULE-ORG-011, RULE-ORG-008, RULE-ORG-018 |
| API-ORG-033 | تعطيل مركز تكلفة | DELETE | /api/v1/org/cost-centers/{id} | costCenterPk | تأكيد | — |
| API-ORG-034 | إعادة تفعيل مركز تكلفة | PUT | /api/v1/org/cost-centers/{id}/reactivate | costCenterPk | CostCenter محدَّث | — |

---

## SCR-ORG-006 — إدارة مراكز الربح

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-006 |
| **اسم الشاشة** | إدارة مراكز الربح |
| **UI Pattern** | PATTERN-1 — Search + Entry |
| **Pattern Reason** | كيان رئيسي بـ 5+ حقول مرتبط بالكيان القانوني — يستوجب PATTERN-1 |
| **SCR-ID Scope** | ONE SCR-ID covers: Search + Entry (CORE-9) |
| **P3 Implication** | Two-screen UX navigation — P3 determines component names in F1 |
| **ENTITY-ID** | ENTITY-ORG-006 |
| **وظيفة الشاشة** | إنشاء وإدارة مراكز الربح ضمن الكيانات القانونية |
| **المستخدمون** | مدير النظام، مدير المالية |
| **الموضع في النظام** | التنظيم المؤسسي ← مراكز الربح |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر البحث

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| profitCenterCode | نص | لا | — | بحث جزئي |
| nameAr | نص | لا | — | بحث جزئي |
| legalEntityFk | LOV | لا | ENTITY-ORG-001 → A3 | |
| isActiveFl | قائمة منسدلة | لا | نشط / غير نشط | |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_PROFIT_CENTER_CREATE |
| Edit | عند تحديد سجل | PERM_PROFIT_CENTER_UPDATE |
| Deactivate | عند تحديد سجل نشط | PERM_PROFIT_CENTER_DELETE |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| profitCenterCode | نص (Read-Only) | نظام | ENTITY-ORG-006 → A3 | يُنشأ تلقائياً |
| nameAr | نص | نعم | ENTITY-ORG-006 → A3 | |
| nameEn | نص | نعم | ENTITY-ORG-006 → A3 | |
| legalEntityFk | LOV | نعم | ENTITY-ORG-001 → A3 | نشط فقط |
| notes | نص | لا | ENTITY-ORG-006 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (جديد) | POST | RULE-ORG-020, RULE-ORG-012, RULE-ORG-016 |
| حفظ (تعديل) | PUT | RULE-ORG-011 |
| تعطيل | DELETE (soft) | — |

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف/تعطيل (DELETE) |
|---|---|---|---|---|
| SCR-ORG-006 | مدير النظام، مدير المالية | مدير النظام | مدير النظام | مدير النظام |

**Security Engine Declaration:**
```
SEC_PAGES  : page_code = PROFIT_CENTER, module = ORGANIZATION, parent_id_fk = [ORG_MENU]
PERMISSIONS: يُنشئها Security Engine × 4 تلقائياً
```

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-035 | إنشاء مركز ربح | POST | /api/v1/org/profit-centers | nameAr, nameEn, legalEntityFk, notes? | ProfitCenter كامل | RULE-ORG-020, RULE-ORG-012, RULE-ORG-016 |
| API-ORG-036 | بحث في مراكز الربح | GET | /api/v1/org/profit-centers | code?, nameAr?, legalEntityFk?, isActiveFl?, page, size | قائمة ProfitCenter | — |
| API-ORG-037 | جلب مركز ربح بالمعرف | GET | /api/v1/org/profit-centers/{id} | profitCenterPk | ProfitCenter كامل | — |
| API-ORG-038 | تعديل مركز ربح | PUT | /api/v1/org/profit-centers/{id} | nameAr?, nameEn?, notes? | ProfitCenter محدَّث | RULE-ORG-011 |
| API-ORG-039 | تعطيل مركز ربح | DELETE | /api/v1/org/profit-centers/{id} | profitCenterPk | تأكيد | — |
| API-ORG-040 | إعادة تفعيل مركز ربح | PUT | /api/v1/org/profit-centers/{id}/reactivate | profitCenterPk | ProfitCenter محدَّث | — |

---

## SCR-ORG-007 — إدارة المواقع الجغرافية

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-ORG-007 |
| **اسم الشاشة** | إدارة المواقع الجغرافية |
| **UI Pattern** | PATTERN-1 — Search + Entry |
| **Pattern Reason** | كيان رئيسي مسطّح بـ 5+ حقول — يستوجب PATTERN-1 |
| **SCR-ID Scope** | ONE SCR-ID covers: Search + Entry (CORE-9) |
| **P3 Implication** | Two-screen UX navigation — P3 determines component names in F1 |
| **ENTITY-ID** | ENTITY-ORG-007 |
| **وظيفة الشاشة** | إنشاء وإدارة المواقع الجغرافية ضمن الفروع |
| **المستخدمون** | مدير النظام، مدير التنظيم |
| **الموضع في النظام** | التنظيم المؤسسي ← المواقع الجغرافية |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر البحث

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| locationCode | نص | لا | — | بحث جزئي |
| nameAr | نص | لا | — | بحث جزئي |
| branchFk | LOV | لا | ENTITY-ORG-002 → A3 | |
| siteTypeId | قائمة منسدلة | لا | LOV-ORG-006 → A5 | lookupKey: LOCATION_SITE_TYPE |
| isActiveFl | قائمة منسدلة | لا | نشط / غير نشط | |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_LOCATION_SITE_CREATE |
| Edit | عند تحديد سجل | PERM_LOCATION_SITE_UPDATE |
| Deactivate | عند تحديد سجل نشط | PERM_LOCATION_SITE_DELETE |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| locationCode | نص (Read-Only) | نظام | ENTITY-ORG-007 → A3 | يُنشأ تلقائياً |
| nameAr | نص | نعم | ENTITY-ORG-007 → A3 | |
| nameEn | نص | نعم | ENTITY-ORG-007 → A3 | |
| branchFk | LOV | نعم | ENTITY-ORG-002 → A3 | نشط فقط |
| siteTypeId | قائمة منسدلة | نعم | LOV-ORG-006 → A5 | lookupKey: LOCATION_SITE_TYPE |
| notes | نص | لا | ENTITY-ORG-007 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ (جديد) | POST | RULE-ORG-012, RULE-ORG-016 |
| حفظ (تعديل) | PUT | RULE-ORG-011 |
| تعطيل | DELETE (soft) | — |

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف/تعطيل (DELETE) |
|---|---|---|---|---|
| SCR-ORG-007 | مدير النظام، مدير التنظيم | مدير النظام | مدير النظام | مدير النظام |

**Security Engine Declaration:**
```
SEC_PAGES  : page_code = LOCATION_SITE, module = ORGANIZATION, parent_id_fk = [ORG_MENU]
PERMISSIONS: يُنشئها Security Engine × 4 تلقائياً
```

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-ORG-041 | إنشاء موقع جغرافي | POST | /api/v1/org/location-sites | nameAr, nameEn, branchFk, siteTypeId, notes? | LocationSite كامل | RULE-ORG-012, RULE-ORG-016 |
| API-ORG-042 | بحث في المواقع | GET | /api/v1/org/location-sites | code?, nameAr?, branchFk?, siteTypeId?, isActiveFl?, page, size | قائمة LocationSite | — |
| API-ORG-043 | جلب موقع بالمعرف | GET | /api/v1/org/location-sites/{id} | locationSitePk | LocationSite كامل | — |
| API-ORG-044 | تعديل موقع جغرافي | PUT | /api/v1/org/location-sites/{id} | nameAr?, nameEn?, siteTypeId?, notes? | LocationSite محدَّث | RULE-ORG-011 |
| API-ORG-045 | تعطيل موقع جغرافي | DELETE | /api/v1/org/location-sites/{id} | locationSitePk | تأكيد | — |
| API-ORG-046 | إعادة تفعيل موقع | PUT | /api/v1/org/location-sites/{id}/reactivate | locationSitePk | LocationSite محدَّث | — |
| API-ORG-047 | مواقع حسب الفرع | GET | /api/v1/org/location-sites/by-branch/{branchId} | branchFk, isActiveFl? | قائمة LocationSite | — |

---

# ══════════════════════════════════════════════════════════
# STANDALONE — بعد PART B
# ══════════════════════════════════════════════════════════

---

## Permissions Summary & Registry Update

> **ملاحظة:** هذا الجدول aggregate view مُجمَّع من B4 sections.
> B4 هو المصدر — هذا الجدول للمراجعة الإجمالية (P4 CHECK-10).

| الشاشة | PAGE_CODE | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف (DELETE) |
|---|---|---|---|---|---|
| SCR-ORG-001 — الكيانات القانونية | LEGAL_ENTITY | مدير النظام، مدير التنظيم | مدير النظام | مدير النظام | مدير النظام |
| SCR-ORG-002 — الفروع | BRANCH | مدير النظام، مدير التنظيم | مدير النظام | مدير النظام | مدير النظام |
| SCR-ORG-003 — المناطق | REGION | مدير النظام، مدير التنظيم | مدير النظام | مدير النظام | مدير النظام |
| SCR-ORG-004 — الأقسام | DEPARTMENT | مدير النظام، مدير التنظيم | مدير النظام | مدير النظام | مدير النظام |
| SCR-ORG-005 — مراكز التكلفة | COST_CENTER | مدير النظام، مدير التنظيم، مدير المالية | مدير النظام | مدير النظام | مدير النظام |
| SCR-ORG-006 — مراكز الربح | PROFIT_CENTER | مدير النظام، مدير المالية | مدير النظام | مدير النظام | مدير النظام |
| SCR-ORG-007 — المواقع الجغرافية | LOCATION_SITE | مدير النظام، مدير التنظيم | مدير النظام | مدير النظام | مدير النظام |

> **CORE-9:** كل شاشة = SCR-ID واحد = صف واحد في SEC_PAGES.
> **SEC-3:** Security Engine يُنشئ 4 أذونات تلقائياً لكل PAGE_CODE.
> **INF-ORG-01:** PERM_LEGAL_ENTITY_VIEW مُنشأ مسبقاً في security-registry — لا يُكرَّر.

---

### Registry Update — MODE 1

```
## REGISTRY UPDATE — 2026-06-23
────────────────────────────────────────────────────────────────
Source Mode    : MODE 1 — SRS Governance Engine
Feature Code   : ORG-001
SRS Version    : 1.0
P0 Context     : module-registry-ORG.md v2026-06-23 (REGENERATED)
DBS-ID         : DBS-ORG-001 (prior — GOVERNED ✓ MODE 1.5)
Plan ID        : PLAN-ORG-001 (prior — GOVERNED ✓)
────────────────────────────────────────────────────────────────
Entities       : ENTITY-ORG-001 (LegalEntity — SHARED owner)
                 ENTITY-ORG-002 (Branch — SHARED owner)
                 ENTITY-ORG-003 (Region — SHARED owner)
                 ENTITY-ORG-004 (Department — SHARED owner)
                 ENTITY-ORG-005 (CostCenter — SHARED owner)
                 ENTITY-ORG-006 (ProfitCenter — SHARED owner)
                 ENTITY-ORG-007 (LocationSite — SHARED owner)
                 ENTITY-ORG-008 (RegionType — PRIVATE Reference Table)
DB Tables      : ORG_LEGAL_ENTITY / ORG_BRANCH / ORG_REGION /
                 ORG_DEPARTMENT / ORG_COST_CENTER / ORG_PROFIT_CENTER /
                 ORG_LOCATION_SITE / ORG_REGION_TYPE
                 (all GOVERNED ✓ DBS-ORG-001 — no new tables)
Screens        : SCR-ORG-001 through SCR-ORG-007 (7 screens)
LOVs           : LOV-ORG-001 (LEGAL_ENTITY_TYPE)
                 LOV-ORG-002 (BRANCH_TYPE)
                 LOV-ORG-003 (DEPARTMENT_NODE_TYPE)
                 LOV-ORG-004 (COST_CENTER_NODE_TYPE)
                 LOV-ORG-005 (COST_CENTER_TYPE)
                 LOV-ORG-006 (LOCATION_SITE_TYPE)
                 LOV-ORG-007 (REGION_TYPE — Reference Table)
Rules          : RULE-ORG-001 through RULE-ORG-020 (20 rules)
APIs           : API-ORG-001 through API-ORG-047 (47 APIs)
XM-IDs Open   : — (Organization is ROOT — no outbound XM dependencies)
OQ-IDs Open   : OQ-001 (DEFERRED — see OQ Log)
Gate Status    : PASSED ✓
Next Action    : MODE 4A — Governance Audit Engine (P4)
────────────────────────────────────────────────────────────────
```

---

## OQ Log — سجل الأسئلة المفتوحة

```
## OPEN QUESTIONS LOG — Organization (ORG-001) — 2026-06-23
─────────────────────────────────────────────────────────────────────────────────────────
OQ-ID  │ Question                                         │ Status   │ Raised   │ Resolved │ Escalation
───────┼──────────────────────────────────────────────────┼──────────┼──────────┼──────────┼────────────────
OQ-001 │ ما هو تأثير إلغاء تفعيل Region على الموديولات   │ DEFERRED │ MODE 1   │ —        │ XM-ESC-TBD
       │ المستهلكة عبر SOFT-READ؟ هل يُمنع الإلغاء؟      │          │ (ARCH-8) │          │ (AQ-003)
       │ هل يُبلَّغ المستهلكون؟                           │          │          │          │
─────────────────────────────────────────────────────────────────────────────────────────
Source : ARCH-8 auto-raise — SOFT-READ consumer impact on Region deactivation
Status : DEFERRED — يُحسم عند تشغيل أول موديول مستهلك لـ Region في MODE 1.5
Affects: ENTITY-ORG-003 — API-ORG-018 (Deactivate Region)
AQ-003 : OQ-001 مُصعَّد إلى master-registry Section 14 كـ AQ-003 (DEFERRED — non-blocking)
─────────────────────────────────────────────────────────────────────────────────────────
```

---

*نهاية الوثيقة | End of srs-org-001.md*
*Governed by: SRS Governance Engine (Project 1)*
*Feature Code: ORG-001 | Version: 1.0 | Date: 2026-06-23*
*Structure: PART A (Module Foundation — A1–A7) + PART B (7 Screen Specifications) + Standalone*
*Entities: 8 (7 SHARED owner + 1 PRIVATE Reference Table)*
*Rules: 20 | LOVs: 7 | Screens: 7 | APIs: 47*
*Next Mode: MODE 4A — Governance Audit Engine (Project 4)*
*ROOT MODULE — zero outbound XM dependencies*
