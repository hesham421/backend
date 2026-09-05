<!-- ═══════════════════════════════════════════════════════════ -->
<!-- SRS — وثيقة التحليل والمتطلبات                             -->
<!-- Governed by: SRS Governance Engine (Project 1)             -->
<!-- Compatible: PROJECT-2 | PROJECT-3 | PROJECT-4              -->
<!-- Structure : PART A (Module Foundation) + PART B (Screens)  -->
<!-- ═══════════════════════════════════════════════════════════ -->

# وثيقة التحليل (SRS)
## البيانات المرجعية | Master Data (MDM)

---

> ## ⓘ UPSTREAM CHANGE / AMENDMENT — v1.2 → v1.3 (2026-09-04)
> **نوع التعديل:** حسم OQ-MDM-002 فقط (مصدر تعداد `FILE_FILE_TYPE`) — §A2 governed-seed + OQ Log + توجيه P2. لا مساس بـ A3/A4/A6/PART B.
> **القرار:** بتفويض صريح من المعماري (Hesham, 2026-09-04: «افعل ما تراه مناسبًا، كل الصلاحيات، حتى تحل التعارض») — اعتُمِد التعداد المحكوم القائم في **`srs-FILE.md §A5`** (LOV-FILE-001 · lookupKey `FILE_FILE_TYPE`): **IMAGE · DOCUMENT · SPREADSHEET · ARCHIVE · OTHER**. خيار **zero-invention** (HR-1)، مؤكَّد من ثلاثة أرتيفاكتات متوافقة: srs-FILE §A5 · module-registry-FILE §LOVs OWNED · db-script-FILE (FILE_TYPE_ID / LOV-FILE-001).
> **لماذا لا امتداد جديد:** الحسم قرار مصدر-بيانات لبذرة MDM فقط؛ لا يمسّ أرتيفاكتات FILE/NOTIF (Core يبقى مجمّدًا). إنشاء Extension لهذا وحده = إفراط هيكلي يخالف «التعقيد المتوسط». يبقى الامتداد مبرَّرًا فقط لإعادة توجيه FILE/NOTIF لاستهلاك MDM (Stage C) — قرار P-DOMAIN منفصل، خارج نطاق هذا التعارض.
> **الأثر النازل:** توجيه P2 لإعادة ختم `DBS-MDM-001` BLOCK 8 يشمل الآن **المجموعات الأربع كلّها** (بما فيها FILE_FILE_TYPE). لا استثناء متبقٍّ.
> **النسخة السابقة:** `_backup/MDM__srs.md__2026-09-04-03.md` (backup-and-replace).

> ## ⓘ UPSTREAM CHANGE — v1.1 → v1.2 (2026-09-04)
> §A2 فقط: رفع استبعاد الـ Seeding (قرار معماري) — MDM يشحن القيم القياسية المملوكة لموديولات أخرى كـ seed محكوم، حرفيًا من مصادرها. النمط المعماري (API-only، بلا FK/XM) دون تغيير. Backup: `_backup/MDM__srs.md__2026-09-04-02.md`.

> ## ⓘ UPSTREAM CHANGE — v1.0 → v1.1 (2026-09-04)
> طبقة الشاشة فقط: دمج الشاشتين في شاشة مركّبة master-detail واحدة (SCR-MDM-001، PATTERN-1، `MDM_LOOKUP`). OQ-MDM-001 → RESOLVED. Backup: `_backup/MDM__srs.md__2026-09-04.md`.

---

# ══════════════════════════════════════════════════════════
# PART A — MODULE FOUNDATION
# Single source of truth — read once per module
# ══════════════════════════════════════════════════════════

---

## A1 — معلومات الوثيقة (Document Information)

| البند | القيمة |
|---|---|
| **اسم المشروع** | منصة ERP — الأساس المشترك (Domain: erp-core) |
| **الموديول** | البيانات المرجعية (Master Data) |
| **Feature Code** | MDM-001 |
| **Feature Type** | Reference / Master — كتالوج القوائم المرجعية للمنصة (LookupType MASTER 1—* LookupValue DETAIL) |
| **الطبقة / النوع** | L1 · Reference/Service · dep: CU (library) + SEC (audit SOFT) |
| **النسخة** | 1.3 (حُسِم OQ-MDM-002 — FILE_FILE_TYPE من srs-FILE §A5) |
| **التاريخ** | 2026-09-04 |
| **الحالة** | Draft |
| **Open Questions** | None — see OQ Log (OQ-MDM-001 · OQ-MDM-002 RESOLVED) |
| **Governed by** | SRS Governance Engine (Project 1) |
| **Deployment Surface** | Backend + Frontend — واجهات REST + شاشة إدارة مركّبة (React/TS/Vite) |

---

## A2 — السياق الوظيفي (Functional Context)

### ما يشمله هذا الموديول

> كتالوج عام للقوائم المرجعية / البيانات المرجعية على مستوى المنصة، مبنيّ على كيانين في علاقة **master-detail**: **LookupType** (MASTER — فئة/نوع القائمة) و**LookupValue** (DETAIL — القيم الأعضاء تحت كل نوع، لا توجد قيمة بلا نوع أب). يوفّر المرجع الإلزامي الوحيد لأي قائمة قيم مشتركة (Lookup / Reference Data) يحتاجها أي موديول حالي أو مستقبلي في المنصة، ويُستهلَك عبر واجهة خدمة (API) لا عبر قراءة جدول مباشرة. المصدر: module-registry-MDM.md §SCOPE NOTE + domain-profile.md v3 MAIN COMPONENTS #5.

#### الزرع المحكوم للقيم القياسية (Governed Seed) — قرار معماري (Hesham, 2026-09-04)

> يشحن MDM — كـ **seed محكوم ضمن نطاق الموديول** — القيم القياسية للقوائم المرجعية المملوكة أصلًا لموديولات أخرى، بشرط أن تُؤخذ القيم **حرفيًا من الأرتيفاكت المُعتمد للموديول المالك** (قراءة read-only؛ لا اختراع). هذا يجعل BLOCK 8 في DBS-MDM-001 متوافقًا مع الطبقة 1 بدل CONDITIONAL. **النمط المعماري لا يتغيّر:** الاستهلاك يبقى API-only، بلا FK بين-موديولي وبلا XM — التعديل يخصّ «مصدر القيم» فقط.

> **نطاق القيم المحكومة (تُنقل حرفيًا، بمصادرها):**
>
> | type_code | المصدر المُعتمد (read-only) | القيم (حرفيًا) | الحالة |
> |---|---|---|---|
> | `NOTIF_CHANNEL` | srs-NOTIF.md §A5 (LOV-NOTIF-001) | EMAIL · SMS · WHATSAPP · PUSH · INTERNAL | ✓ مؤكَّد للزرع |
> | `NOTIF_STATUS` | srs-NOTIF.md §A5/§A6 (LOV-NOTIF-002) | PENDING · SENT · FAILED · CHANNEL_DISABLED | ✓ مؤكَّد للزرع |
> | `FILE_FILE_STATUS` | srs-FILE.md §A5/§A6 (LOV-FILE-002) | ACTIVE · ARCHIVED · DELETED | ✓ مؤكَّد للزرع |
> | `FILE_FILE_TYPE` | srs-FILE.md §A5 (LOV-FILE-001) | IMAGE · DOCUMENT · SPREADSHEET · ARCHIVE · OTHER | ✓ مؤكَّد للزرع (حُسِم OQ-MDM-002 v1.3) |
>
> **حسم OQ-MDM-002 (v1.3):** التعارض السابق (التوجيه افترض عدم وجود تعداد محكوم لـ FILE_FILE_TYPE) حُسِم باعتماد التعداد القائم فعلًا في `srs-FILE.md §A5` — خيار zero-invention مؤكَّد من ثلاثة أرتيفاكتات متوافقة، بتفويض صريح من المعماري.
>
> القيم المُثبَّتة أعلاه محتوى seed جديد مُستنَد إلى قراءة read-only لأرتيفاكت الموديول المالك، موسوم بالمصدر — وليست استنساخًا لأرتيفاكت الموديول المالك (RULE-3) ولا تغييرًا لملكيته. صياغة INSERT ملك P2 (BLOCK 8)؛ P1 يحكم النطاق والمصدر فقط.

### ما لا يشمله هذا الموديول

> - **الإعدادات وقيم التشغيل (Configuration):** تبقى AppConfiguration ملكًا لـ Common Utils — MDM بيانات مرجعية/lookup فقط (business-policies-MDM §SCOPE EXCEPTIONS).
> - **إعادة توجيه FILE/NOTIF لاستهلاك MDM (repoint / SOFT-READ):** تبقى تعديلًا حوكميًا منفصلًا على تلك الموديولات (INTEGRATION CANDIDATES / Stage C) — **منفصلة تمامًا** عن زرع القيم داخل MDM؛ ذلك الزرع لا يفرض على FILE/NOTIF أي تغيير ولا FK. (إن اعتُمِد repoint لاحقًا، يُوصى بحمله في Extension يُبقي Core مجمّدًا — قرار P-DOMAIN.)
> - **Workflow Engine:** لا يوجد (RULE-13 = OFF).
> - *(أُزيل في v1.2: بند «MDM لا يشحن قيمًا» — عُكِس إلى «الزرع المحكوم» أعلاه.)*

### وظيفة الموديول

> يُمكّن مدير البيانات المرجعية — من **شاشة مركّبة واحدة** — من تعريف نوع قائمة مرجعية وصيانته، وإدارة قيمه الأعضاء في شبكة تفصيلية ضمن نفس الشاشة، بلغتين، بحيث تحصل كل الموديولات الأخرى على قوائمها المشتركة من مصدر واحد موحّد بدل تكرار قائمة محلّية لكل غرض.

### الوصف الوظيفي التفصيلي

> يُنشئ المدير **LookupType** بمُعرِّف نصّي طبيعي (typeCode مثل `FILE_FILE_TYPE`) واسمين (عربي/إنجليزي)، ويدير تحته **LookupValue**‌ات في شبكة تفصيلية، لكل قيمة رمز (valueCode مثل `IMAGE`) واسمان وترتيب عرض اختياري. الموديولات المستهلِكة لا تقرأ جداول MDM مباشرة؛ تطلب القيم عبر `GET /api/v1/mdm/lookups/{typeCode}?active=true` وتخزّن **الرمز (code)** كمرجع ليّن (SOFT) — لا مفتاحًا رقميًا. لأن المستهلكين يحملون الرمز، فإن رموز الأنواع والقيم **غير قابلة للتعديل بعد الإنشاء**، وتُدار دورة الاستبعاد عبر `isActiveFl` (تعطيل ناعم) لا الحذف الصلب، بلا أي تعاقب (cascade) على المستهلكين.

### ملاحظات عامة

- **قرار P1 (شكل الكيان):** الشكل **المسطّح (flat)** لكيان LookupValue — بلا `parentValueFk` هرمي — (تعقيد متوسط؛ قابل للتمديد لاحقًا بلا إعادة هيكلة).
- **قرار P1 (Business Code):** لا Business Code (BC-RULE-0 = NO). `typeCode`/`valueCode` مفاتيح طبيعية.
- **قرار P1 (طبقة الشاشة — v1.1):** شاشة مركّبة master-detail واحدة (SCR-MDM-001، PATTERN-1، `MDM_LOOKUP`) — CORE-9.
- **قرار معماري (v1.2):** رفع استبعاد الـ Seeding (§A2 «الزرع المحكوم»).
- **حسم (v1.3):** OQ-MDM-002 — FILE_FILE_TYPE من srs-FILE §A5 (zero-invention).
- **قرار P1 (التبعيات):** CU مكتبة؛ `createdBy` من SEC نمط تدقيق SOFT — لا XM ولا ARCH-8.

---

## A3 — الكيانات والحقول (Entities & Fields)

*(POSTGRESQL_16 — حقول audit مطوية: createdBy/At, updatedBy/At عبر AuditEntityListener)*

> **علاقة Master-Detail (صريحة):** `ENTITY-MDM-001 LookupType` (MASTER) —‹1—*›— `ENTITY-MDM-002 LookupValue` (DETAIL) عبر `LookupValue.lookupTypeFk` (NOT NULL). **لا وجود لصف تفصيلي بلا صف رئيسي.** القيد المرجعي (FK) والفهرس يُثبَّتان في P2.

---

### ENTITY-MDM-001 — LookupType (نوع القائمة المرجعية) — MASTER

| البند | القيمة |
|---|---|
| **النوع** | PRIVATE (يُستهلَك عبر API خدمة MDM — provider pattern — لا كجدول مشترك) |
| **Business Code** | NO — per BC-RULE-0 (5.5.5): `typeCode` مفتاح طبيعي، لا مُعرِّف خارجي المنشأ |
| **المصدر** | module-registry-MDM.md §ENTITIES OWNED; prd-MDM US-MDM-001 |
| **العمليات** | Create, Read, Update, Deactivate (soft) |
| **Cross-Module** | None — يُستهلَك عبر API |

#### حقول الكيان

| اسم الحقل | نوع البيانات | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| lookupTypePk | BIGINT (PK) | نظام | Sequence | رقم إنشائي تلقائي | المعرف | ID |
| typeCode | VARCHAR(50) | نعم | UNIQUE | مفتاح نصّي طبيعي — Read-Only بعد الإنشاء (RULE-MDM-002) | رمز النوع | Type Code |
| nameAr | VARCHAR(200) | نعم | — | — | الاسم بالعربي | Name (Arabic) |
| nameEn | VARCHAR(100) | نعم | — | — | الاسم بالإنجليزي | Name (English) |
| isActiveFl | SMALLINT | نظام | 1 / 0 | 1 = نشط — ⚠ Fl suffix إلزامي | نشط | Active |
| createdBy | VARCHAR(255) | نظام | — | AuditEntityListener — لا يُقبل في DTO | أنشئ بواسطة | Created By |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener — لا يُقبل في DTO | تاريخ الإنشاء | Created At |
| updatedBy | VARCHAR(255) | نظام | — | AuditEntityListener — لا يُقبل في DTO | عُدِّل بواسطة | Updated By |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener — لا يُقبل في DTO | تاريخ التعديل | Updated At |
| notes | VARCHAR(2000) | لا | — | — | ملاحظات | Notes |

---

### ENTITY-MDM-002 — LookupValue (قيمة القائمة المرجعية) — DETAIL

| البند | القيمة |
|---|---|
| **النوع** | PRIVATE (يُستهلَك عبر API خدمة MDM — provider pattern — لا كجدول مشترك) |
| **Business Code** | NO — per BC-RULE-0 (5.5.5): `valueCode` مفتاح طبيعي، وهو الرمز الذي يخزّنه المستهلكون |
| **المصدر** | module-registry-MDM.md §ENTITIES OWNED; prd-MDM US-MDM-002 |
| **العمليات** | Create, Read, Update, Deactivate (soft) |
| **Cross-Module** | None — يُستهلَك عبر API |

#### حقول الكيان

| اسم الحقل | نوع البيانات | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| lookupValuePk | BIGINT (PK) | نظام | Sequence | رقم إنشائي تلقائي | المعرف | ID |
| lookupTypeFk | BIGINT (FK) | نعم | ENTITY-MDM-001 | **FK → LookupType — DETAIL لا يوجد بلا MASTER (NOT NULL)** | نوع القائمة | Lookup Type |
| valueCode | VARCHAR(50) | نعم | UNIQUE ضمن lookupTypeFk | الرمز المُخزَّن لدى المستهلكين — Read-Only بعد الإنشاء (RULE-MDM-004) | رمز القيمة | Value Code |
| nameAr | VARCHAR(200) | نعم | — | — | الاسم بالعربي | Name (Arabic) |
| nameEn | VARCHAR(100) | نعم | — | — | الاسم بالإنجليزي | Name (English) |
| sortOrder | SMALLINT | لا | — | ترتيب العرض في القائمة (اختياري) | ترتيب العرض | Sort Order |
| isActiveFl | SMALLINT | نظام | 1 / 0 | 1 = نشط — ⚠ Fl suffix إلزامي | نشط | Active |
| createdBy | VARCHAR(255) | نظام | — | AuditEntityListener — لا يُقبل في DTO | أنشئ بواسطة | Created By |
| createdAt | TIMESTAMP | نظام | — | AuditEntityListener — لا يُقبل في DTO | تاريخ الإنشاء | Created At |
| updatedBy | VARCHAR(255) | نظام | — | AuditEntityListener — لا يُقبل في DTO | عُدِّل بواسطة | Updated By |
| updatedAt | TIMESTAMP | نظام | — | AuditEntityListener — لا يُقبل في DTO | تاريخ التعديل | Updated At |
| notes | VARCHAR(2000) | لا | — | — | ملاحظات | Notes |

> **قاعدة Label إلزامية:** كل حقل يحمل Label-AR وLabel-EN. **UNIQUE مركّب:** تفرُّد `valueCode` ضمن `lookupTypeFk` — يُثبَّت في P2 كـ `UNIQUE(type_id, value_code)`.

---

## A4 — قواعد التحقق (Business Rules)

> **قاعدة إلزامية:** هذا القسم هو المصدر الوحيد لتعريف القواعد. PART B يشير بالـ RULE-ID فقط.

---

### RULE-MDM-001 — تفرُّد رمز النوع

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-MDM-001 |
| **Trigger** | عند إنشاء أو تعديل LookupType |
| **Statement** | The system MUST prevent creating a LookupType whose typeCode already exists. |
| **Message-AR** | رمز النوع مستخدَم مسبقًا — اختر رمزًا فريدًا. |
| **Message-EN** | This type code already exists — choose a unique code. |
| **Source** | domain-profile.md v3 (LookupType كمفتاح فئة موحّد) |

### RULE-MDM-002 — عدم قابلية رمز النوع للتعديل

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-MDM-001 |
| **Trigger** | عند تعديل LookupType |
| **Statement** | The system MUST prevent modifying typeCode after creation. |
| **Message-AR** | لا يمكن تعديل رمز النوع بعد الإنشاء — القيمة مرجع تعتمد عليه موديولات أخرى. |
| **Message-EN** | Type code cannot be changed after creation — other modules reference it. |
| **Source** | domain-profile.md v3 (المستهلكون يخزّنون code كمرجع SOFT) |
| **Test-Hint** | تحقّق أن محاولة تغيير typeCode عبر PUT تُرفَض حتى مع صلاحية UPDATE |

### RULE-MDM-003 — تفرُّد رمز القيمة ضمن النوع

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-MDM-002 |
| **Trigger** | عند إنشاء أو تعديل LookupValue |
| **Statement** | The system MUST prevent creating a LookupValue whose valueCode already exists under the same lookupTypeFk. |
| **Message-AR** | رمز القيمة مستخدَم مسبقًا ضمن هذا النوع — اختر رمزًا فريدًا. |
| **Message-EN** | This value code already exists under this type — choose a unique code. |
| **Source** | domain-profile.md v3 (LookupValue أعضاء تحت LookupType) |
| **Test-Hint** | تحقّق أن التفرُّد ضمن النوع فقط — نفس الرمز مسموح تحت نوع آخر |

### RULE-MDM-004 — عدم قابلية رمز القيمة للتعديل

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-MDM-002 |
| **Trigger** | عند تعديل LookupValue |
| **Statement** | The system MUST prevent modifying valueCode after creation. |
| **Message-AR** | لا يمكن تعديل رمز القيمة بعد الإنشاء — القيمة مرجع تخزّنه موديولات أخرى. |
| **Message-EN** | Value code cannot be changed after creation — other modules store it. |
| **Source** | domain-profile.md v3 (المستهلكون يخزّنون code كمرجع SOFT) |

### RULE-MDM-005 — إلزامية الاسمين

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-MDM-001, ENTITY-MDM-002 |
| **Trigger** | عند الحفظ (إنشاء/تعديل) |
| **Statement** | The system MUST require both nameAr and nameEn before saving a LookupType or a LookupValue. |
| **Message-AR** | الاسم بالعربي والاسم بالإنجليزي كلاهما إلزامي. |
| **Message-EN** | Both the Arabic name and the English name are required. |
| **Source** | Language Rule (المنصة ثنائية اللغة) + قواعد التسمية المعيارية (5.4.2) |

### RULE-MDM-006 — تعطيل ناعم بلا حذف صلب للبيانات المستخدَمة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-MDM-001, ENTITY-MDM-002 |
| **Trigger** | عند طلب استبعاد نوع أو قيمة |
| **Statement** | The system MUST deactivate reference data via isActiveFl (soft) rather than hard-delete, and MUST prevent hard-deleting a LookupType that still has LookupValues; deactivation cascades no change to external consumers (they hold codes as SOFT references). |
| **Message-AR** | يُستبعَد العنصر بالتعطيل لا بالحذف؛ لا يمكن حذف نوع يحتوي قيمًا. |
| **Message-EN** | Items are deactivated, not deleted; a type that still has values cannot be hard-deleted. |
| **Source** | domain-profile.md v3 (مرجع إلزامي للمستهلكين) + ERP reference-data default |
| **Test-Hint** | تحقّق أن تعطيل نوع/قيمة لا يحذف صفوفًا ولا يؤثّر على مراجع المستهلكين المخزّنة |

---

## A5 — قوائم القيم (LOV / Lookup)

> **لا يوجد LOV-ID مملوك أو مُستهلَك لهذا الموديول.**
> MDM هو **مزوّد** آلية القوائم المرجعية للمنصة: كيانا MDM نفسهما (LookupType/LookupValue) هما آلية القوائم المشتركة. القيمة المُخزَّنة لدى المستهلكين هي **code** (LOV-6)، وتُقرأ عبر `GET /api/v1/mdm/lookups/{typeCode}?active=true`.
> **ملاحظة:** «الزرع المحكوم» (§A2) لا يُنشئ LOV-ID جديدًا لـ MDM — هو محتوى بيانات (صفوف) يُثبَّت كـ seed في P2 (BLOCK 8)، بقيم حرفية من مصادرها.

---

## A6 — دورة الحالة (Status Lifecycle)

> **غير منطبق (SCR-5).** لا حقل `statusId` بدورة حياة متعددة — الاستبعاد عبر `isActiveFl` فقط. لا Approval Flow (RULE-13 = OFF).

---

## A7 — تبعيات الموديولات (Module Dependencies)

> هذا القسم يحدد XM Candidates. التصنيف الرسمي (XM-IDs) يتم في MODE 1.5.

### الكيانات المُستهلَكة من موديولات أخرى

> **لا يوجد.** هوية `createdBy` تُقرأ من Security كنمط التدقيق القياسي (SOFT، بلا FK) — وليست XM candidate. لا ARCH-8 auto-raise.

### الخدمات والتكاملات الخارجية

> **لا يوجد تكامل خارجي.** التبعية الوحيدة على مستوى المكتبة هي **Common Utils**.
> **ملاحظة تكامل (خارج نطاق هذا الـ SRS):** FILE/NOTIF مرشّحان لاستهلاك MDM كـ SOFT-READ بعد الزرع — تعديلات حوكمية منفصلة على تلك الموديولات (اتجاه الاعتماد: هم يعتمدون على MDM). **زرع القيم داخل MDM (§A2) لا ينشئ اعتمادًا لـ MDM على أحد.**

---

# ══════════════════════════════════════════════════════════
# PART B — SCREEN SPECIFICATIONS
# One block per SCR-ID — self-contained for P3 execution
# References PART A by ID — never redefines artifacts
# ══════════════════════════════════════════════════════════

---

## SCR-MDM-001 — إدارة القوائم المرجعية (Composite Master-Detail)

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-MDM-001 |
| **اسم الشاشة** | إدارة القوائم المرجعية |
| **UI Pattern** | PATTERN-1 — Composite (Master + Detail) |
| **Pattern Reason** | رأس (LookupType) + شبكة سطور تفصيلية متكررة (LookupValue) تحت نفس الرأس — شاشة مركّبة، SCR-ID واحد (5.8.2 / CORE-9) |
| **SCR-ID Scope** | ONE SCR-ID covers: Search (قائمة الأنواع) + Master form + Detail grid (القيم) — CORE-9 |
| **Container Pattern** | FULL_PAGE — P3 يحدد أسماء المكوّنات في F1 |
| **ENTITY-ID** | ENTITY-MDM-001 (Master) + ENTITY-MDM-002 (Detail) |
| **page_code** | MDM_LOOKUP |
| **وظيفة الشاشة** | اختيار/إنشاء نوع قائمة، وإدارة قيمه في شبكة تفصيلية ضمن نفس الشاشة |
| **المستخدمون** | R1 (مدير البيانات المرجعية), R2 (مدير النظام) |
| **الموضع في النظام** | البيانات المرجعية ← القوائم المرجعية |
| **روابط من** | قائمة موديول البيانات المرجعية |
| **روابط إلى** | — |

---

### B2 — مواصفة البحث (Search Specification)

> *(الجزء Master من الشاشة المركّبة — قائمة LookupTypes)*

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| typeCode | نص | لا | — | بحث بالرمز |
| nameAr / nameEn | نص | لا | — | بحث بالاسم |
| isActiveFl | قائمة (نشط/غير نشط) | لا | 1 / 0 | فلتر الحالة |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New (نوع جديد) | دائماً | PERM_MDM_LOOKUP_CREATE |
| Edit (فتح نوع) | عند تحديد نوع | PERM_MDM_LOOKUP_UPDATE |
| Deactivate | عند تحديد نوع | PERM_MDM_LOOKUP_DELETE |
| Export | دائماً | PERM_MDM_LOOKUP_VIEW |

#### قواعد البحث المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| RULE-MDM-006 | استبعاد المعطَّلة افتراضيًا في العرض | ← see A4 |

---

### B3 — مواصفة الإدخال (Input Specification)

> *(الشاشة المركّبة: قسم Master لـ LookupType + شبكة Detail لـ LookupValue)*

#### قسم الرأس (Master) — LookupType

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| typeCode | نص | نعم | ENTITY-MDM-001 → A3 | Read-Only بعد الإنشاء (RULE-MDM-002) |
| nameAr | نص | نعم | ENTITY-MDM-001 → A3 | |
| nameEn | نص | نعم | ENTITY-MDM-001 → A3 | |
| isActiveFl | مفتاح (نشط) | نظام | ENTITY-MDM-001 → A3 | التعطيل الناعم (RULE-MDM-006) |
| notes | نص | لا | ENTITY-MDM-001 → A3 | |

#### شبكة التفصيل (Detail grid) — LookupValue (تحت النوع المحدَّد)

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| valueCode | نص | نعم | ENTITY-MDM-002 → A3 | Read-Only بعد الإنشاء (RULE-MDM-004) |
| nameAr | نص | نعم | ENTITY-MDM-002 → A3 | |
| nameEn | نص | نعم | ENTITY-MDM-002 → A3 | |
| sortOrder | رقم | لا | ENTITY-MDM-002 → A3 | ترتيب العرض |
| isActiveFl | مفتاح (نشط) | نظام | ENTITY-MDM-002 → A3 | التعطيل الناعم (RULE-MDM-006) |
| (إضافة/تعديل/تعطيل صف) | إجراء صف | — | — | `lookupTypeFk` يُملأ تلقائيًا من الرأس المحدَّد |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ | POST / PUT (رأس + تفاصيل) | RULE-MDM-001..005 — *(تفاصيل في A4)* |
| إلغاء | navigation back | — |
| تعطيل | DELETE (soft) | RULE-MDM-006 — *(تفاصيل في A4)* |

#### قواعد الإدخال المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| RULE-MDM-001 | تفرُّد typeCode (Master) | ← see A4 |
| RULE-MDM-002 | منع تعديل typeCode (Master) | ← see A4 |
| RULE-MDM-003 | تفرُّد valueCode ضمن النوع (Detail) | ← see A4 |
| RULE-MDM-004 | منع تعديل valueCode (Detail) | ← see A4 |
| RULE-MDM-005 | إلزامية الاسمين (Master + Detail) | ← see A4 |
| RULE-MDM-006 | تعطيل ناعم / منع حذف نوع بقيم | ← see A4 |

---

### B4 — الصلاحيات (Permissions)

> **CORE-9:** هذه الشاشة المركّبة = SCR-ID واحد = صف واحد في SEC_PAGES.

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف/تعطيل (DELETE) | تصدير |
|---|---|---|---|---|---|
| SCR-MDM-001 | R1, R2 | R1, R2 | R1, R2 | R1, R2 | R1, R2 |

> R1 = مدير البيانات المرجعية (Master Data Administrator) | R2 = مدير النظام (System Administrator)
> VIEW = gateway يمنح الوصول للرأس والتفاصيل معًا.

**Security Seed Data:**
```
SEC_PAGES  : INSERT — page_code = MDM_LOOKUP, parent = قائمة «البيانات المرجعية»
PERMISSIONS: تُولَّد بواسطة Security Engine (4 لكل صفحة) — SRS لا يُعدِّد PERM_* (SEC-3)
```

---

### B5 — الواجهات البرمجية (Functional APIs)

> **Stack Rule (STACK-1):** مسار موحّد `/api/v1/mdm/[resource]` — REST (Spring Boot).

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-MDM-001 | إنشاء نوع (Master) | POST | /api/v1/mdm/lookup-types | typeCode, nameAr, nameEn, notes? | LookupType كامل | RULE-MDM-001, RULE-MDM-005 |
| API-MDM-002 | قائمة الأنواع (Master search) | GET | /api/v1/mdm/lookup-types | nameAr?, isActiveFl?, page, size | قائمة LookupType | — |
| API-MDM-003 | تعديل نوع (Master) | PUT | /api/v1/mdm/lookup-types/{id} | nameAr, nameEn, notes? | LookupType محدَّث | RULE-MDM-002, RULE-MDM-005 |
| API-MDM-004 | تعطيل نوع (Master, soft) | DELETE | /api/v1/mdm/lookup-types/{id} | lookupTypePk | تأكيد | RULE-MDM-006 |
| API-MDM-005 | جلب نوع بالمعرّف | GET | /api/v1/mdm/lookup-types/{id} | lookupTypePk | LookupType كامل | — |
| API-MDM-006 | إضافة قيمة (Detail) | POST | /api/v1/mdm/lookup-types/{typeId}/values | valueCode, nameAr, nameEn, sortOrder? | LookupValue كامل | RULE-MDM-003, RULE-MDM-005 |
| API-MDM-007 | قائمة قيم النوع (Detail) | GET | /api/v1/mdm/lookup-types/{typeId}/values | isActiveFl?, page, size | قائمة LookupValue | — |
| API-MDM-008 | تعديل قيمة (Detail) | PUT | /api/v1/mdm/lookup-values/{id} | nameAr, nameEn, sortOrder? | LookupValue محدَّثة | RULE-MDM-004, RULE-MDM-005 |
| API-MDM-009 | تعطيل قيمة (Detail, soft) | DELETE | /api/v1/mdm/lookup-values/{id} | lookupValuePk | تأكيد | RULE-MDM-006 |
| API-MDM-010 | جلب قيمة بالمعرّف | GET | /api/v1/mdm/lookup-values/{id} | lookupValuePk | LookupValue كامل | — |
| API-MDM-011 | استهلاك القائمة (منصّي) | GET | /api/v1/mdm/lookups/{typeCode} | typeCode, active=true | قيم النوع النشطة (code + name) | — |

> **API-MDM-011** نقطة الاستهلاك على مستوى المنصّة (US-MDM-003): قراءة لأي مُصادَق عليه، تُرجِع الرمز (code). مسار Detail المُعشَّش `/lookup-types/{typeId}/values` يعزّز «لا قيمة بلا نوع».

---

# ══════════════════════════════════════════════════════════
# STANDALONE — بعد PART B
# ══════════════════════════════════════════════════════════

---

## Permissions Summary & Registry Update

> **CORE-9 COMPOSITE SCREEN RULE:** الشاشة المركّبة master-detail = SCR-ID واحد — VIEW هو الـ gateway.

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف/تعطيل (DELETE) | تصدير |
|---|---|---|---|---|---|
| SCR-MDM-001 (القوائم المرجعية — Master+Detail) | R1, R2 | R1, R2 | R1, R2 | R1, R2 | R1, R2 |

> R1 = مدير البيانات المرجعية | R2 = مدير النظام

---

### Registry Update — MODE 1

```
## REGISTRY UPDATE — 2026-09-04 (v1.3 — OQ-MDM-002 resolved)
────────────────────────────────────────────────────────────────
Source Mode    : MODE 1 (P1 — SRS Governance Engine) — AMENDMENT
Module         : Master Data (MDM) — Feature Code MDM-001
────────────────────────────────────────────────────────────────
Change         : OQ-MDM-002 RESOLVED — FILE_FILE_TYPE governed source =
                 srs-FILE §A5 (LOV-FILE-001): IMAGE·DOCUMENT·SPREADSHEET·
                 ARCHIVE·OTHER. Zero-invention; Architect-delegated.
Governed seed  : NOTIF_CHANNEL · NOTIF_STATUS · FILE_FILE_STATUS ·
  scope          FILE_FILE_TYPE — ALL 4 sets ✓ confirmed (literal from source).
Entities/Rules : unchanged (ENTITY-MDM-001/002; RULE-MDM-001..006).
Screens/APIs   : unchanged (SCR-MDM-001; API-MDM-001..011).
OQ-IDs Open    : None ✓ (OQ-MDM-001, OQ-MDM-002 both RESOLVED)
Gate Status    : PASSED ✓ (amendment)
────────────────────────────────────────────────────────────────
## DOWNSTREAM DIRECTIVE → P2 (Database Governance Engine)
Re-seal DBS-MDM-001 BLOCK 8 seed: CONDITIONAL → ADOPTABLE (Layer-1
  aligned) for ALL FOUR sets — NOTIF_CHANNEL, NOTIF_STATUS,
  FILE_FILE_STATUS, FILE_FILE_TYPE — using the exact values from their
  source artifacts (read-only). No pending exclusion remains.
Re-cite source as srs-MDM v1.3 (current db-script header cites v1.0).
P2/Maintainer note: the only db-script-MDM copy found is in
  _backup/MDM__db-script.md__2026-09-04.md — confirm/restore the live
  artifact at its P2-DB path before re-sealing.
────────────────────────────────────────────────────────────────
## REGISTRY EVENT LOG — 2026-09-04
- EVENT: SRS AMENDMENT (MDM) v1.2 → v1.3 — OQ-MDM-002 RESOLVED
  (FILE_FILE_TYPE source = srs-FILE §A5, zero-invention, Architect-
  delegated). Governed seed now covers all 4 sets. Backup:
  _backup/MDM__srs.md__2026-09-04-03.md. Ripple: DBS-MDM-001 BLOCK 8
  re-seal directive updated to include FILE_FILE_TYPE.

> يطبّق مسؤول السجل البشري هذه البلوكات على project-registry.md. لا مساس
> بـ DBF/DBS/XM (ملك P2). Core (FILE/NOTIF) لم يُمسّ في هذا التعديل.
```

---

## OQ Log — سجل الأسئلة المفتوحة

```
## OPEN QUESTIONS LOG — Master Data (MDM) — 2026-09-04
─────────────────────────────────────────────────────────────────────
OQ-ID      │ Question                                   │ Status   │ Raised    │ Resolved   │ Escalation
───────────┼────────────────────────────────────────────┼──────────┼───────────┼────────────┼──────────────
OQ-MDM-001 │ سطح الإدارة (شاشة مركّبة مقابل تزويد نشر)  │ RESOLVED │ MODE 1    │ MODE1 v1.1 │ LOCAL
OQ-MDM-002 │ مصدر تعداد FILE_FILE_TYPE للزرع            │ RESOLVED │ MODE1 v1.2│ MODE1 v1.3 │ Architect-
           │ الحسم: اعتُمِد تعداد srs-FILE §A5           │          │           │            │ delegated
           │ (IMAGE/DOCUMENT/SPREADSHEET/ARCHIVE/OTHER) │          │           │            │
           │ — zero-invention، مؤكَّد من 3 أرتيفاكتات.   │          │           │            │
─────────────────────────────────────────────────────────────────────
لا أسئلة مفتوحة. كل مجموعات الزرع الأربع مؤكَّدة من مصادرها الحيّة (read-only).
```

---

*End of srs.md — Master Data (MDM), P1 (SRS Governance Engine), v1.3, 2026-09-04.*
*v1.3: OQ-MDM-002 resolved (FILE_FILE_TYPE ← srs-FILE §A5, zero-invention). No open questions. Prev: _backup/MDM__srs.md__2026-09-04-03.md.*
*Downstream: P2 re-seal DBS-MDM-001 BLOCK 8 for all 4 seed sets. Core (FILE/NOTIF) untouched. Next stage otherwise: MODE 1.5.*
