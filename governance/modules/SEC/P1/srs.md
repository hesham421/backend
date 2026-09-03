<!-- SRS — Governed by SRS Governance Engine (Project 1) | PART A + PART B -->

# وثيقة التحليل (SRS)
## الأمن والصلاحيات | Security (SEC)

---

# PART A — MODULE FOUNDATION

## A1 — معلومات الوثيقة (Document Information)

| البند | القيمة |
|---|---|
| **اسم المشروع** | منصة Foundation (Domain: ERP) |
| **الموديول** | الأمن والصلاحيات (Security) |
| **Feature Code** | SEC-001 |
| **Feature Type** | Master + Configuration (Auth/AuthZ Engine) |
| **الطبقة / النوع** | L1 · Engine · dep: CU |
| **النسخة** | 1.3 — CONTINUATION (updated from v1.2) |
| **التاريخ** | 2026-09-02 |
| **الحالة** | Draft |
| **Open Questions** | None — see OQ Log |
| **Governed by** | SRS Governance Engine (Project 1) |
| **Deployment Surface** | **Backend + Frontend** — واجهات REST + شاشات إدارة (React/TS/Vite) |
| **Direct Upstream** | prd-SEC.md **v2** (US-SEC-008..011) · domain-profile-ERP.md **v2** |

> **UPSTREAM CHANGE — SEC two-tier RBAC + internal SSO (domain-profile-ERP.md v2)**
> - **Triggered by:** prd-SEC.md **v2** (US-SEC-008..011) ← domain-profile-ERP.md **v2** §GOVERNING RULES.
> - **Amended here (v1.2 → v1.3):** +ENTITY-SEC-010 (Module), +ENTITY-SEC-011 (RoleModule join); +حقل `moduleFk` على ENTITY-SEC-004 (Page); +RULE-SEC-013 (Tier-1 grant → dashboard filter + prerequisite), +RULE-SEC-014 (derivation — no orphan screen permission); +SSO note (US-SEC-011); +API-SEC-017..020; +SCR-SEC-004 (SEC_MODULES) وتحديث SCR-SEC-002. كل المعرّفات السابقة محفوظة كما هي.
> - **Downstream must re-align:** **P2 (db-script-SEC)** ← أولاً (جدول Module + join + FK)، ثم **P2.5 (ui-ux-spec-SEC / flow-diagram-SEC)** يستهلك PRD+SRS، ثم **P3.1 (backend-execution-plan-SEC)**.

## A2 — السياق الوظيفي (Functional Context)

### ما يشمله هذا الموديول
> أساس مصادقة وتفويض قابل لإعادة الاستخدام: حسابات مستخدمين، أدوار، صلاحيات، سجل شاشات (Page/Screen Registry — مالك CORE-9)، جلسات JWT مع تجديد (Refresh)، نسيان كلمة المرور وتفعيل الحساب ذاتياً، **مع شاشات إدارة أمامية** لكل ذلك. المصدر: module-registry-SEC §SCOPE NOTE.
> **نموذج التفويض الموحّد (two-tier RBAC فوق SSO داخلي) — domain-profile-ERP.md v2:**
> - **Tier 1 — Role → Modules:** منح الموديل لدور يحكم ظهوره على الداشبورد (**مرشّح عرض فقط**، لا بوابة إنفاذ منفصلة وقت التشغيل)، وهو **شرط مسبق** لأي صلاحية شاشة داخله. (US-SEC-008, US-SEC-009)
> - **Tier 2 — Role → Screens:** الإنفاذ الفعلي على مستوى الشاشة عبر `PERM_<PAGE>_VIEW/CREATE/UPDATE/DELETE` — **CORE-9 دون تغيير**. (US-SEC-010)
> - **اشتقاق الشاشات من الموديل:** لا تُمنح صلاحية شاشة لدور ما لم يُمنَح الدور موديلها — **لا صلاحية شاشة يتيمة** (RULE-SEC-014). (US-SEC-010)
> - **SSO داخلي:** هوية/توكن داخلي واحد عبر كل موديولات المنصة، **للمصادقة فقط** (مَن أنت)، منفصل عن التفويض (ماذا تملك). (US-SEC-011) — انظر A7.

### ما لا يشمله هذا الموديول
> - **Branch/Organization DataScope** — مُسقَط (business-policies-SEC §SCOPE EXCEPTIONS).
> - **كيان UserProfile منفصل** — حقول الملف تُطوى في UserAccount (medium complexity).
> - **External identity federation (SSO خارجي)** — إضافة اختيارية لاحقة، **خارج النطاق الآن** (prd-SEC v2 §SCOPE EXCLUSIONS؛ domain-profile v2).
> - **Workflow Engine** (RULE-13 = OFF).

### وظيفة الموديول
> دخول آمن (JWT + Refresh)، وإدارة الحسابات والأدوار والصلاحيات (RBAC ثنائي الطبقة) عبر شاشات، وربط كل شاشة مُسجَّلة بصلاحياتها الأربع تلقائياً (CORE-9)، ومنح الموديولات للأدوار (Tier-1) لتصفية الداشبورد، وخدمة ذاتية لإعادة تعيين كلمة المرور وتفعيل الحساب.

### الوصف الوظيفي التفصيلي
> UserAccount هو كيان الهوية المشترك (SHARED owner = SEC، RULE-10) تقرؤه NOTIF وFILE قراءةً SOFT. الأدوار تُمنَح للمستخدمين (UserRole). التفويض طبقتان: **Tier-1** يمنح الدورَ موديولات (RoleModule) فتظهر على داشبورده وتصبح شرطاً مسبقاً؛ **Tier-2** يمنح الدورَ صلاحيات الشاشات داخل تلك الموديولات (RolePermission) والصلاحيات تُولَّد تلقائياً لكل Page وفق CORE-9. رموز إعادة التعيين/التفعيل تُنشأ داخل SEC ويُطلَق حدث CU يستمع إليه NOTIF — **SEC لا يستدعي NOTIF مباشرة** (اعتماده = CU فقط).

### ملاحظات عامة
- **قرار P1:** حالة الحساب عبر `userStatusId` (LOV-SEC-002) — دورة حياة (A6). القفل المؤقت عبر `failedLoginCount`+`lockedUntil` (ليس حالة lifecycle).
- **قرار P1:** القيم العددية للمصادقة افتراضية قياسية ⚠ — قابلة للربط Source: Client (business-policies-SEC).
- **قرار P1:** `permissionType` (VIEW/CREATE/UPDATE/DELETE) اصطلاح كود (CORE-9)، **لا LOV**.
- **قرار Architect (2026-09-02) — حسم OQ-SEC-001:** إلغاء تنشيط الحساب **مسموح** ولا يتعاقب على المستهلكين؛ المراجع التاريخية تُبقى؛ لا حاجز مرجعي مركزي؛ المستهلكون يمنعون العمليات الجديدة لحساب غير نشط (RULE-SEC-012 + RULE-NOTIF-007).
- **DRV (قرار نمذجة P1 — v1.3):** نُمذِج Tier-1 عبر **كيان Module صريح (ENTITY-SEC-010) + join RoleModule (ENTITY-SEC-011) + `moduleFk` على Page**، بدل إعادة استخدام هرمية SEC_PAGE (اعتبار الصفحة الجذر = موديل). السبب: (1) وفاء لمعيار domain-profile v2 الذي يجعل «Role→Modules» طبقةً أولى صريحة؛ (2) إنفاذ نظيف لقاعدة الاشتقاق (Page.moduleFk + RoleModule) دون تحميل دلالة مزدوجة على Page؛ (3) تعقيد متوسط — جدول مرجعي صغير + join + FK واحد، لا over-engineering؛ (4) استعلام داشبورد واضح لمجموعة الموديولات الممنوحة. البديل (إعادة استخدام هرمية الصفحات) مرفوض: يخلط الموديل بالشاشة ويعقّد الاشتقاق واستعلام الداشبورد.

## A3 — الكيانات والحقول (Entities & Fields)
*(POSTGRESQL_16 — حقول audit مطوية: createdBy/At, updatedBy/At عبر AuditEntityListener)*

### ENTITY-SEC-001 — UserAccount (حساب المستخدم)
| النوع | Business Code | المصدر | العمليات |
|---|---|---|---|
| SHARED (owner: SEC) · Master · يُستهلَك SOFT من NOTIF/FILE | NO — `username` مفتاح طبيعي (BC-RULE-2 غير منطبق) | module-registry-SEC; prd-SEC US-SEC-001/002 | Create, Read, Update, Deactivate, Activate, Login, Refresh, Logout |

| الحقل | النوع | إلزامي | القيم/المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| userAccountPk | BIGINT (PK) | نظام | Sequence | — | المعرف | ID |
| username | VARCHAR(100) | نعم | UNIQUE | اسم الدخول | اسم المستخدم | Username |
| passwordHash | VARCHAR(255) | نظام | — | مُجزّأ فقط (RULE-SEC-004) | — | — |
| email | VARCHAR(255) | نعم | UNIQUE | — | البريد | Email |
| phone | VARCHAR(30) | لا | — | — | الهاتف | Phone |
| fullName | VARCHAR(200) | نعم | — | — | الاسم الكامل | Full Name |
| preferredLangId | VARCHAR(10) | نعم | LOV-SEC-001 | code (AR/EN) | اللغة المفضّلة | Preferred Language |
| userStatusId | VARCHAR(50) | نعم | LOV-SEC-002 | دورة حياة (A6) | حالة الحساب | Status |
| failedLoginCount | SMALLINT | نظام | 0 | عدّاد القفل | محاولات فاشلة | Failed Logins |
| lockedUntil | TIMESTAMP | لا | — | انتهاء القفل | مقفول حتى | Locked Until |
| isActiveFl | SMALLINT | نعم | 1/0 | ⚠ Fl | نشط | Active |

### ENTITY-SEC-002 — Role (الدور)
| PRIVATE · Master | Business Code: NO (`roleCode` مفتاح يختاره الأدمن) | module-registry-SEC; prd-SEC US-SEC-003 | Create, Read, Update, Deactivate, AssignToUser, **AssignModules (Tier-1)** |

| الحقل | النوع | إلزامي | القيم | Label-AR | Label-EN |
|---|---|---|---|---|---|
| rolePk | BIGINT (PK) | نظام | Sequence | المعرف | ID |
| roleCode | VARCHAR(50) | نعم | UNIQUE | رمز الدور | Role Code |
| nameAr | VARCHAR(200) | نعم | — | الاسم بالعربي | Name (AR) |
| nameEn | VARCHAR(100) | نعم | — | الاسم بالإنجليزي | Name (EN) |
| isActiveFl | SMALLINT | نعم | 1/0 | نشط | Active |

### ENTITY-SEC-003 — Permission (الصلاحية)
| PRIVATE · Config · تُولَّد تلقائياً لكل Page (CORE-9/SEC-3) | Business Code: NO | module-registry-SEC; CORE-9 | Auto-generate, Read, Grant/Revoke |

| الحقل | النوع | إلزامي | القيم | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| permissionPk | BIGINT (PK) | نظام | Sequence | — | المعرف | ID |
| permissionCode | VARCHAR(150) | نعم | UNIQUE | PERM_<PAGE>_<TYPE> (اصطلاح) | رمز الصلاحية | Permission Code |
| pageFk | BIGINT (FK) | نعم | ENTITY-SEC-004 | — | الصفحة | Page |
| permissionType | VARCHAR(20) | نعم | VIEW/CREATE/UPDATE/DELETE (اصطلاح، لا LOV) | — | النوع | Type |
| nameAr / nameEn | VARCHAR | نعم | — | — | الاسم | Name |
| isActiveFl | SMALLINT | نعم | 1/0 | ⚠ Fl | نشط | Active |

### ENTITY-SEC-004 — Page / Screen Registry (سجل الشاشات)
| PRIVATE · Config/Ref · مالك CORE-9 | Business Code: NO | module-registry-SEC; CORE-9 | Create, Read, Update, Deactivate |

| الحقل | النوع | إلزامي | القيم | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| pagePk | BIGINT (PK) | نظام | Sequence | — | المعرف | ID |
| pageCode | VARCHAR(100) | نعم | UNIQUE | مفتاح الشاشة | رمز الشاشة | Page Code |
| nameAr / nameEn | VARCHAR | نعم | — | — | الاسم | Name |
| **moduleFk** | **BIGINT (FK)** | **نعم** | **ENTITY-SEC-010** | **⟵ أُضيف في v1.3: الموديل المالك للصفحة — يُفعّل الاشتقاق (RULE-SEC-014)** | **الموديل** | **Module** |
| parentPageFk | BIGINT (FK) | لا | ENTITY-SEC-004 (self) | تسلسل الملاحة داخل الموديل | الصفحة الأب | Parent Page |
| isActiveFl | SMALLINT | نعم | 1/0 | ⚠ Fl | نشط | Active |

### ENTITY-SEC-005/006/007 — Tokens · Internal/PRIVATE
- **RefreshToken**: refreshTokenPk · userAccountFk→SEC-001 · token(مُجزّأ,UNIQUE) · expiresAt · revokedFl · createdAt — تدوير عند التجديد (RULE-SEC-006).
- **PasswordResetToken**: passwordResetTokenPk · userAccountFk · token · expiresAt · usedFl · createdAt — واحد نشط/مستخدم، أحادي الاستخدام (RULE-SEC-007).
- **AccountActivationToken**: accountActivationTokenPk · userAccountFk · token · expiresAt · usedFl · createdAt — واحد نشط/مستخدم (RULE-SEC-008).
> كيانات داخلية — بلا nameAr/nameEn، بلا business code، بلا شاشة إدارة.

### ENTITY-SEC-008/009 — Joins · INTERNAL/JOIN
- **UserRole**: userAccountFk→SEC-001 · roleFk→SEC-002 (مركّب، @JoinTable، بلا id/audit).
- **RolePermission**: roleFk→SEC-002 · permissionFk→SEC-003 (مركّب، @JoinTable، بلا id/audit) — **Tier-2** (يخضع لاشتقاق RULE-SEC-014).

### ⟵ ENTITY-SEC-010 — Module (سجل الموديولات) · جديد v1.3
| PRIVATE · Reference/Config · الوحدة الممنوحة في Tier-1 + وحدة عرض الداشبورد | Business Code: NO — BC-RULE-0 (كيان داخلي؛ `moduleCode` مفتاح) | prd-SEC US-SEC-008/009; domain-profile v2 | Create, Read, Update, Deactivate, Grant/Revoke to Role |

| الحقل | النوع | إلزامي | القيم | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| modulePk | BIGINT (PK) | نظام | Sequence | — | المعرف | ID |
| moduleCode | VARCHAR(50) | نعم | UNIQUE | مثل SEC/FILE/NOTIF/… | رمز الموديل | Module Code |
| nameAr | VARCHAR(200) | نعم | — | — | الاسم بالعربي | Name (AR) |
| nameEn | VARCHAR(100) | نعم | — | — | الاسم بالإنجليزي | Name (EN) |
| isActiveFl | SMALLINT | نعم | 1/0 | ⚠ Fl | نشط | Active |

### ⟵ ENTITY-SEC-011 — RoleModule (ربط دور×موديل) · INTERNAL/JOIN · جديد v1.3
- **RoleModule**: roleFk→SEC-002 · moduleFk→SEC-010 (مفتاح مركّب، @JoinTable، بلا id/audit) — **Tier-1 grant** (منح الموديل للدور). المصدر: prd-SEC US-SEC-008.

## A4 — قواعد التحقق (Business Rules)
> القيم ⚠ افتراضية قياسية — قابلة للربط Source: Client (business-policies-SEC).

| RULE-ID | Scope | Trigger | Statement | Message-AR | Message-EN | Source |
|---|---|---|---|---|---|---|
| RULE-SEC-001 | SEC-001 | إنشاء | prevent duplicate `username`. | اسم المستخدم مستخدَم مسبقاً. | Username already exists. | reg |
| RULE-SEC-002 | SEC-001 | إنشاء | require username, passwordHash, email, fullName. | حقول الحساب الأساسية إلزامية. | Core account fields required. | reg |
| RULE-SEC-003 | SEC-001 | كلمة المرور | enforce complexity (min ⚠8, letters+digits). | كلمة المرور لا تحقق التعقيد. | Password complexity not met. | biz-pol ⚠Client |
| RULE-SEC-004 | SEC-001 | حفظ | store password hashed only. | تُخزَّن مُجزّأة فقط. | Stored hashed only. | standard |
| RULE-SEC-005 | SEC-001 | Login | lock after ⚠5 failed logins. | قُفل بعد محاولات فاشلة. | Locked after failed logins. | biz-pol ⚠Client |
| RULE-SEC-006 | SEC-005 | Refresh | rotate refresh token; access TTL ⚠15m, refresh ⚠7d. | يُدوَّر رمز التجديد. | Refresh token rotated. | biz-pol ⚠Client |
| RULE-SEC-007 | SEC-006 | Forgot-pw | single active, single-use reset token, TTL ⚠60m. | رمز إعادة تعيين واحد فعّال. | Single active reset token. | biz-pol ⚠Client |
| RULE-SEC-008 | SEC-007 | Activation | single active, single-use activation token, TTL ⚠24h. | رمز تفعيل واحد فعّال. | Single active activation token. | biz-pol ⚠Client |
| RULE-SEC-009 | SEC-001 | Login | prevent login when `userStatusId` ≠ ACTIVE. | لا دخول لحساب غير نشط. | Login blocked for non-active. | reg |
| RULE-SEC-010 | SEC-002/003/004 | إنشاء | unique `roleCode`, `permissionCode`, `pageCode`. | الرموز فريدة. | Codes are unique. | integrity |
| RULE-SEC-011 | SEC-004→003 | تسجيل Page | auto-generate 4 permissions/Page (CORE-9); SRS declares Page+pageCode only. | تُولَّد أربع صلاحيات لكل شاشة. | Four permissions auto-generated per page. | CORE-9/SEC-3 |
| RULE-SEC-012 | SEC-001 | Deactivate | allow deactivating a UserAccount without cascading to SOFT consumers; history retained; reactivation permitted. | يُسمح بإلغاء التنشيط دون تعاقب؛ تُحفظ المراجع؛ يُسمح بإعادة التنشيط. | Deactivation allowed without cascade; history retained. | قرار Architect — يحسم OQ-SEC-001 |
| **RULE-SEC-013** | **SEC-011 (RoleModule)** | **Tier-1 grant / dashboard load** | **MUST treat a Role→Module grant as a dashboard DISPLAY FILTER (module visible only if granted) and as a PREREQUISITE for any screen permission within that module; no separate runtime enforcement gate at module level.** | **منح الموديل للدور يُظهره على الداشبورد وهو شرط مسبق لأي صلاحية شاشة داخله.** | **A module grant drives dashboard visibility and gates screen-permission grants within it.** | **prd-SEC US-SEC-008, US-SEC-009; domain-profile v2** |
| **RULE-SEC-014** | **SEC-009 (RolePermission) ↔ SEC-004.moduleFk** | **Grant screen permission** | **MUST NOT grant a Role a screen (Page) permission unless the Role is granted that Page's Module (RoleModule). No orphan screen permission.** | **لا تُمنح صلاحية شاشة لدور ما لم يُمنَح الدور موديل الشاشة — لا صلاحية شاشة يتيمة.** | **No screen permission may be granted unless the page's module is granted to the role.** | **prd-SEC US-SEC-010; domain-profile v2 (derivation)** |

## A5 — قوائم القيم (LOV / Lookup)
> لا MD_MASTER_LOOKUP مركزي — قوائم **محلية** لـ SEC. القيمة = code، runtime-loaded (LOV-3)، لا ENUMs (LOV-4). *(لا LOV جديد في v1.3 — Module كيان مرجعي وليس LOV.)*

**LOV-SEC-001 — preferredLang** (`preferredLangId`, ENTITY-SEC-001, Dropdown, `SEC_PREFERRED_LANG`): `AR`=العربية/Arabic · `EN`=الإنجليزية/English.

**LOV-SEC-002 — userStatus** (`userStatusId`, ENTITY-SEC-001, Dropdown, `SEC_USER_STATUS`): `PENDING_ACTIVATION`=بانتظار التفعيل · `ACTIVE`=نشط · `INACTIVE`=غير نشط.

## A6 — دورة الحالة (Status Lifecycle) — UserAccount
```
[PENDING_ACTIVATION] ──تفعيل──► [ACTIVE] ✓ ⇄ [INACTIVE] ✗  (إلغاء/إعادة تنشيط — RULE-SEC-012)
```
> القفل المؤقت (lockedUntil) ليس حالة lifecycle (RULE-SEC-005). RULE-13 — لا Workflow. *(لا تغيير دورة حياة في v1.3.)*

## A7 — تبعيات الموديولات (Module Dependencies)
> لا كيان مُستهلَك — SEC مالك الهوية. اعتماده الوحيد: **USES مكتبة CU**. إشعارات إعادة التعيين/التفعيل عبر **CU Event** يستمع إليه NOTIF (لا استدعاء مباشر).
> **SHARED (owner):** UserAccount (SEC-001) — مستهلكوه NOTIF/FILE (SOFT-READ). سلوك إلغاء التنشيط محسوم (RULE-SEC-012).
> **SSO داخلي (US-SEC-011):** SEC هو سلطة المصادقة الوحيدة للمنصة؛ توكن JWT داخلي واحد يعمل عبر كل الموديولات (FILE/NOTIF يثقان بمرشّح SEC — سلطة JWT واحدة). هذا **هو** الـ SSO الداخلي: مصادقة فقط (مَن أنت)، منفصلة عن التفويض ثنائي الطبقة (Tier-1/Tier-2). **لا federation خارجي الآن** (خارج النطاق — prd-SEC v2). *(تأكيد للتصميم القائم — بلا كيان/جدول جديد للمصادقة.)*

---

# ══════════════════════════════════════════════════════════
# PART B — SCREEN SPECIFICATIONS (Frontend: React/TS/Vite)
# ══════════════════════════════════════════════════════════

> **شاشات عامة (خارج نموذج الصلاحيات — لا SEC_PAGES rows):** Login، Forgot/Reset Password، Activate — شاشات ما قبل المصادقة (public). الإدارية المُقيَّدة أدناه.
> **سلوك الداشبورد (Tier-1 — US-SEC-009):** غلاف المنصة يعرض فقط الموديولات الممنوحة لدور المستخدم (مرشّح عرض)، عبر `API-SEC-019`. هذا سلوك على مستوى غلاف المنصة، لا شاشة CRUD مستقلة لـ SEC — التدفق التفصيلي يُفصّله P2.5.

---

## SCR-SEC-001 — إدارة المستخدمين (User Management)
### B1
| SCR-ID | UI Pattern | Pattern Reason | Scope | Container | ENTITY | page_code |
|---|---|---|---|---|---|---|
| SCR-SEC-001 | PATTERN-2 | كيان بلا سطور/هرمية (5.8.2) | ONE (Unified, CORE-9) | SIDE_DRAWER | ENTITY-SEC-001 | `SEC_USERS` (module: SEC) |

### B3
- قائمة/فلاتر: username, email, `userStatusId` (LOV-SEC-002), isActiveFl.
- تحرير (Drawer): username, email, phone, fullName, `preferredLangId`, `userStatusId`, isActiveFl → A3.
- القواعد: حفظ → RULE-SEC-001,002,003,004 · إلغاء تنشيط → RULE-SEC-009, RULE-SEC-012.

### B4 (CORE-9 / SEC-3) — `page_code=SEC_USERS`
> Security Engine يولّد الصلاحيات الأربع تلقائياً. لا seed لـ PERM_*. (Tier-1: صلاحيات هذه الشاشة قابلة للمنح فقط لدور مُنِح موديل SEC — RULE-SEC-014.)

| الشاشة | VIEW | CREATE | UPDATE | DELETE |
|---|---|---|---|---|
| SCR-SEC-001 | SYS_ADMIN | SYS_ADMIN | SYS_ADMIN | SYS_ADMIN |

### B5
API-SEC-007/008/009/010/012/016.

---

## SCR-SEC-002 — إدارة الأدوار والصلاحيات (Roles · Modules · Permissions)  ⟵ محدَّثة v1.3
### B1
| SCR-SEC-002 | PATTERN-2 (SIDE_DRAWER) | Role بسيط + منح موديولات (Tier-1) + مُنتقي صلاحيات (Tier-2) | ONE (CORE-9) | ENTITY-SEC-002 (+SEC-011 RoleModule Tier-1، +SEC-009/003 Tier-2) | page_code `SEC_ROLES` (module: SEC) |

### B3
- قائمة/فلاتر: roleCode, nameAr, isActiveFl.
- تحرير (Drawer):
  - roleCode, nameAr, nameEn, isActiveFl (ENTITY-SEC-002).
  - **Tier-1 — منح الموديولات:** مُنتقي موديولات (كتابة RoleModule ENTITY-SEC-011؛ قراءة Module ENTITY-SEC-010) → RULE-SEC-013.
  - **Tier-2 — منح صلاحيات الشاشات:** مُنتقي صلاحيات **مُقيَّد بالموديولات الممنوحة فقط** (قراءة ENTITY-SEC-003، كتابة RolePermission ENTITY-SEC-009) → **RULE-SEC-014** (لا صلاحية شاشة يتيمة).
- القواعد: حفظ → RULE-SEC-010 · منح موديل → RULE-SEC-013 · منح صلاحية شاشة → RULE-SEC-014 · توليد الصلاحيات → RULE-SEC-011.
- *(تفاصيل تعشيق «الصلاحيات تحت الموديولات الممنوحة» في الواجهة → يُفصّلها P2.5.)*

### B4 (CORE-9 / SEC-3) — `page_code=SEC_ROLES`
| SCR-SEC-002 | VIEW SYS_ADMIN | CREATE SYS_ADMIN | UPDATE SYS_ADMIN | DELETE SYS_ADMIN |

### B5
API-SEC-011 (CRUD roles), **API-SEC-017 (assign module→role), API-SEC-018 (revoke module→role)**, API-SEC-014 (list permissions), API-SEC-015 (grant/revoke permission), API-SEC-016 (lookups).

---

## SCR-SEC-003 — سجل الشاشات (Screen/Page Registry)
### B1
| SCR-SEC-003 | PATTERN-2 (SIDE_DRAWER) | قائمة + مُحدِّد «الموديل» و«الصفحة الأب». **بديل PATTERN-3 (Tree)** يتطلب موافقة Architect (P3-RULE-2)؛ الافتراضي PATTERN-2 | ONE (CORE-9) | ENTITY-SEC-004 | page_code `SEC_PAGE_REGISTRY` (module: SEC) |

### B3
- قائمة/فلاتر: pageCode, nameAr, **moduleFk**, parentPageFk, isActiveFl.
- تحرير (Drawer): pageCode, nameAr, nameEn, **moduleFk (اختيار من ENTITY-SEC-010 — إلزامي)**, parentPageFk (self), isActiveFl → A3.
- القواعد: حفظ → RULE-SEC-010 · تسجيل صفحة → RULE-SEC-011 (توليد الصلاحيات) · انتماء الصفحة لموديل يُفعّل الاشتقاق → RULE-SEC-014.

### B4 (CORE-9 / SEC-3) — `page_code=SEC_PAGE_REGISTRY`
| SCR-SEC-003 | VIEW SYS_ADMIN | CREATE SYS_ADMIN | UPDATE SYS_ADMIN | DELETE SYS_ADMIN |

### B5
API-SEC-013 (CRUD pages), API-SEC-014, API-SEC-016, **API-SEC-020 (modules lookup)**.

---

## ⟵ SCR-SEC-004 — إدارة الموديولات (Module Registry)  ·  جديد v1.3
### B1
| SCR-ID | UI Pattern | Pattern Reason | Scope | Container | ENTITY | page_code |
|---|---|---|---|---|---|---|
| SCR-SEC-004 | PATTERN-2 | Module كيان مرجعي بسيط — لا سطور/هرمية (5.8.2 → PATTERN-2) | ONE (Unified, CORE-9) | SIDE_DRAWER | ENTITY-SEC-010 | `SEC_MODULES` (module: SEC) |

### B3
- قائمة/فلاتر: moduleCode, nameAr, isActiveFl.
- تحرير (Drawer): moduleCode, nameAr, nameEn, isActiveFl → A3 (ENTITY-SEC-010).
- القواعد: حفظ → تفرّد `moduleCode` (RULE-SEC-010 نمط التفرّد المطبَّق على الرموز) — الموديل هو الوحدة الممنوحة في Tier-1 (RULE-SEC-013).

### B4 (CORE-9 / SEC-3) — `page_code=SEC_MODULES`
> Security Engine يولّد الصلاحيات الأربع تلقائياً. لا seed لـ PERM_*.

| الشاشة | VIEW | CREATE | UPDATE | DELETE |
|---|---|---|---|---|
| SCR-SEC-004 | SYS_ADMIN | SYS_ADMIN | SYS_ADMIN | SYS_ADMIN |

### B5
API-SEC-020 (CRUD modules), API-SEC-016 (lookups).

---

# MODULE-LEVEL FUNCTIONAL APIs
> STACK-1: `/api/v1/security/[resource]`. POSTGRESQL_16.

| API-ID | العملية | HTTP | المسار | RULE-IDs |
|---|---|---|---|---|
| API-SEC-001 | دخول | POST | /api/v1/security/auth/login | RULE-SEC-005, 009 |
| API-SEC-002 | تجديد | POST | /api/v1/security/auth/refresh | RULE-SEC-006 |
| API-SEC-003 | خروج | POST | /api/v1/security/auth/logout | RULE-SEC-006 |
| API-SEC-004 | طلب إعادة تعيين | POST | /api/v1/security/auth/forgot-password | RULE-SEC-007 |
| API-SEC-005 | إعادة تعيين | POST | /api/v1/security/auth/reset-password | RULE-SEC-003, 007 |
| API-SEC-006 | تفعيل | POST | /api/v1/security/auth/activate | RULE-SEC-008, 009 |
| API-SEC-007 | إنشاء مستخدم | POST | /api/v1/security/users | RULE-SEC-001, 002, 003, 004 |
| API-SEC-008 | بحث مستخدمين | GET | /api/v1/security/users | — |
| API-SEC-009 | تعديل مستخدم | PUT | /api/v1/security/users/{id} | RULE-SEC-001 |
| API-SEC-010 | إلغاء تنشيط | DELETE | /api/v1/security/users/{id} | RULE-SEC-012 |
| API-SEC-011 | CRUD أدوار | POST/GET/PUT/DELETE | /api/v1/security/roles | RULE-SEC-010 |
| API-SEC-012 | إسناد دور لمستخدم | POST | /api/v1/security/users/{id}/roles | — |
| API-SEC-013 | CRUD صفحات | POST/GET/PUT/DELETE | /api/v1/security/pages | RULE-SEC-010, 011 |
| API-SEC-014 | صلاحيات | GET | /api/v1/security/permissions | RULE-SEC-011 |
| API-SEC-015 | منح/سحب صلاحية لدور | POST/DELETE | /api/v1/security/roles/{id}/permissions | RULE-SEC-014 |
| API-SEC-016 | قوائم القيم | GET | /api/v1/security/lookups/{lookupKey} | — |
| **API-SEC-017** | **منح موديل لدور (Tier-1)** | **POST** | **/api/v1/security/roles/{id}/modules** | **RULE-SEC-013** |
| **API-SEC-018** | **سحب موديل من دور** | **DELETE** | **/api/v1/security/roles/{id}/modules/{moduleId}** | **RULE-SEC-013, 014** |
| **API-SEC-019** | **موديولات الداشبورد للمستخدم الحالي** | **GET** | **/api/v1/security/me/modules** | **RULE-SEC-013** |
| **API-SEC-020** | **CRUD الموديولات (سجل الموديولات)** | **POST/GET/PUT/DELETE** | **/api/v1/security/modules** | **RULE-SEC-010** |

---

# STANDALONE

## Permissions Summary & Registry Update
> CORE-9: كل شاشة = SCR-ID واحد = صف SEC_PAGES واحد. Security Engine يولّد الصلاحيات الأربع لكل page_code (لا seed أسماء PERM_* — SEC-3). كل صفحة تنتمي لموديل (moduleFk)؛ منح صلاحياتها لدور مشروط بمنح موديلها (Tier-1 → RULE-SEC-014).

| الشاشة (page_code) | Module | VIEW | CREATE | UPDATE | DELETE |
|---|---|---|---|---|---|
| SCR-SEC-001 (SEC_USERS) | SEC | SYS_ADMIN | SYS_ADMIN | SYS_ADMIN | SYS_ADMIN |
| SCR-SEC-002 (SEC_ROLES) | SEC | SYS_ADMIN | SYS_ADMIN | SYS_ADMIN | SYS_ADMIN |
| SCR-SEC-003 (SEC_PAGE_REGISTRY) | SEC | SYS_ADMIN | SYS_ADMIN | SYS_ADMIN | SYS_ADMIN |
| SCR-SEC-004 (SEC_MODULES) | SEC | SYS_ADMIN | SYS_ADMIN | SYS_ADMIN | SYS_ADMIN |
> شاشات عامة (Login/Forgot/Reset/Activate): بلا صف SEC_PAGES (public).

### Registry Update — MODE 1 (Amendment v1.3)
```
Source Mode  : MODE 1 (amendment) | Feature Code: SEC-001 | v1.3 (two-tier RBAC + internal SSO)
Upstream     : prd-SEC v2 (US-SEC-008..011) ← domain-profile-ERP.md v2
New Entities : +ENTITY-SEC-010 (Module — Reference), +ENTITY-SEC-011 (RoleModule — join)
Modified     : ENTITY-SEC-004 (Page) +moduleFk (FK→SEC-010)
New Rules    : +RULE-SEC-013 (Tier-1 grant → dashboard filter + prerequisite),
               +RULE-SEC-014 (derivation — no orphan screen permission)
SSO          : note only (US-SEC-011) — single internal identity/token, auth-only, no external federation
New Screens  : +SCR-SEC-004 (SEC_MODULES); updated SCR-SEC-002 (Tier-1 module assignment + derivation)
New APIs     : +API-SEC-017 (assign module), +API-SEC-018 (revoke module),
               +API-SEC-019 (dashboard modules), +API-SEC-020 (CRUD modules)
Preserved    : ALL prior IDs (SEC-001..009, RULE-SEC-001..012, API-SEC-001..016, SCR-SEC-001..003, LOV-SEC-001/002)
XM-IDs Open  : — (no new cross-module dependency; Tier-1 is intra-SEC)
OQ-IDs Open  : None
Gate Status  : PASSED ✓
Downstream   : P2 (db-script-SEC) → P2.5 (ui-ux-SEC) → P3.1 (backend-plan-SEC)
```
> لمشرف السجل: عند مرور P2 — أضِف جدول `SEC_MODULE` + join `SEC_ROLE_MODULE` + عمود `MODULE_ID` على `SEC_PAGE`؛ سجّل SEC_PAGES: SEC_MODULES.

## OQ Log
```
OPEN QUESTIONS LOG — Security (SEC) — 2026-09-02
OQ-SEC-001 │ أثر إلغاء تنشيط UserAccount (ARCH-8) │ RESOLVED │ MODE 1 │ 2026-09-02 │ XM-ESC-SEC
   القرار: RULE-SEC-012 (SEC) + RULE-NOTIF-007 (NOTIF).
(v1.3) لا أسئلة مفتوحة جديدة — تغيير two-tier RBAC + SSO مُستمَد بالكامل من prd-SEC v2 / domain-profile v2.
```

---
*End of srs-SEC.md | SEC-001 | v1.3 | Backend + Frontend | two-tier RBAC + internal SSO | Upstream: prd-SEC v2 / domain-profile v2*
*Downstream re-align: P2 (DB) → P2.5 (UI/UX) → P3.1 (Backend Plan)*
