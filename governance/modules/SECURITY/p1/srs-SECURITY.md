<!-- ═══════════════════════════════════════════════════════════ -->
<!-- SRS — وثيقة التحليل والمتطلبات                             -->
<!-- Governed by: SRS Governance Engine (Project 1)             -->
<!-- Compatible: PROJECT-2 | PROJECT-3 | PROJECT-4              -->
<!-- Structure : PART A (Module Foundation) + PART B (Screens)  -->
<!-- ✓ AMENDED 2026-07-22 — master-registry.md (v2.10.0) SUPPLIED.        -->
<!--   GOVERNANCE REDUCED (RULE-7) is LIFTED — registry checks re-run   -->
<!--   against Sections 4/5/6/7/8/10/13/14/15. Findings applied below   -->
<!--   as targeted amendments (not a full regeneration — RULE-5 single  -->
<!--   entry gate; no re-derivation of unaffected content). See the new -->
<!--   "MASTER-REGISTRY ALIGNMENT REVIEW — 2026-07-22" block after the  -->
<!--   Entry Gate for the full diff. Two NEW conflicts were discovered  -->
<!--   during this reconciliation (DataScope level values; cross-module -->
<!--   FK mechanism) that master-registry.md's own Conflict Log (Sec.   -->
<!--   13) does not yet cover — raised as OQ-006/OQ-007 per RULE-4      -->
<!--   (this engine raises, it does not silently resolve).             -->
<!-- ⚠ EXCEPTION MODULE — Security was implemented directly       -->
<!--   (Layer 4.1) without the forward P0→P1→P2→P3.1 pipeline.   -->
<!--   Per platform-standards-2.md M.A.4 (cited by module-        -->
<!--   registry-SECURITY-2.md), Security is the canonical         -->
<!--   EXCEPTION module — this SRS documents it AS-IS. No         -->
<!--   content below is a design recommendation; every ENTITY,    -->
<!--   RULE, LOV, and API traces to a source artifact (HR-1 — No  -->
<!--   Invention Beyond Source Artifacts). Deviations from        -->
<!--   standard governance conventions (BC-RULE, LOV-RULE, SCR-   -->
<!--   RULE, ARCH-RULE) found in the as-built code are documented -->
<!--   explicitly as deviations, not silently normalized away.    -->
<!-- ═══════════════════════════════════════════════════════════ -->

# وثيقة التحليل (SRS)
## الأمان والمستخدمون (Security Model + Users) | SECURITY

---

## 🔍 REGISTRY PRE-CHECK (Section 5.1.1)

```
Does this feature's entity already exist?    → YES — module ALREADY
                                                 IMPLEMENTED (Layer 4.1).
                                                 This SRS reverse-documents
                                                 it; it does not create it.
Does a similar Lookup table exist?           → MD_MASTER_LOOKUP /
                                                 MD_LOOKUP_DETAIL (MasterData,
                                                 1.4) — reused, not duplicated.
Does the module exist in the registry?       → YES — master-registry.md
                                                 Section 3/15: Security
                                                 (1.2) = "Active ⚠️
                                                 EXCEPTION". CORE scope
                                                 EXCEPTION ⚠️, EXTENSION
                                                 scope (DataScope +
                                                 Forgot Password + Sign
                                                 Up) PARTIALLY_READY ⚠️.
Any naming conflicts detected?               → NO NEW naming conflicts.
                                                 Section 4 PERMANENT
                                                 EXCEPTION block confirms
                                                 exactly the column names
                                                 used in A3 (USERS_PK,
                                                 ROLES_PK, IS_ACTIVE,
                                                 ENABLED, REVOKED,
                                                 PERMISSION_TYPE-as-VARCHAR-
                                                 enum). TWO NEW non-naming
                                                 conflicts found — see
                                                 OQ-006 / OQ-007 below.
master-registry Section 13 (Conflict Log)    → Conflicts #1/#3 (Security
                                                 PERMANENT EXCEPTION),
                                                 #4 (no TENANT_ID), #9
                                                 (DataScope centralized in
                                                 Security), #20 (Security↔
                                                 Notification cycle,
                                                 CLOSED — confirms RULE-
                                                 SEC-031's event pattern
                                                 is accepted architecture)
                                                 all reviewed — no
                                                 CLOSED conflict contradicts
                                                 this SRS. No OPEN conflict
                                                 targets Security.
master-registry Section 14 (Open Questions)  → AQ-006 / AQ-007 (registry
                                                 version-citation mismatch
                                                 for SEC_USER_PROFILE /
                                                 SEC_ROLE_BRANCH, non-
                                                 blocking) apply directly
                                                 to ENTITY-SEC-009/010 —
                                                 cross-referenced in A3/OQ
                                                 Log below.
```

## 📋 MODE 1 ENTRY GATE (Section 5.3)

| Check | Result |
|---|---|
| prd-SECURITY.md attached? (v2.1 HARD GATE) | ✓ — attached (bootstrap/reverse-reconstructed, per its own notice) |
| Business requirements provided? | ✓ — extracted-facts-SECURITY.md, rbac.md, users-datascope.md, auth-menu.md |
| Module identified? | SECURITY |
| moduleRegistry.md (module-registry-SECURITY-2.md) loaded? | ✓ |
| master-registry.md loaded? | ✓ **v2.10.0 — supplied 2026-07-22. GOVERNANCE REDUCED LIFTED.** |
| Prior srs.md attached? | ✓ — this engine's own prior output (2026-07-22), treated as amendment base |
| Prior OQ Log attached? | ✓ — embedded in prior srs.md (OQ-001 to OQ-005) |
| P0 Status (master-registry Sec 15) | **Security: EXCEPTION ⚠️ (core) / PARTIALLY_READY ⚠️ (extension scope — SEC_USER_PROFILE/SEC_ROLE_BRANCH/Forgot Password/Sign Up, unblocked, Conflict #20 CLOSED). Open AQ-006, AQ-007 (non-blocking).** → proceed |
| **PROCEED?** | **Yes — AMENDMENT MODE, registry-verified. No longer GOVERNANCE REDUCED.** |

## 📋 INFERRED VALUES (Section 5.1.2)

```
Feature Code        : SEC-001
Feature Type        : Hybrid — Engine (1.2) + Master Data (1.3), per
                      module-registry-SECURITY-2.md M.8. Contains no
                      Transactional entities (no document-numbered,
                      period-scoped records) — all entities are either
                      system/master (Users, Roles, Permissions, Pages)
                      or internal (tokens, join tables).
                      Reason: module-registry-SECURITY-2.md explicit
                      "Type: Engine + Master Data" declaration.
Requires Approval   : No — RULE-13 confirmed: no workflow/approval flow
                      found anywhere in extracted-facts-SECURITY.md.
                      All state is simple boolean toggle (see A6).
Has Financial Impact: No — Security has no monetary fields anywhere in
                      the extracted entity set.
```
*(Proceeding — module already exists, values are descriptive of the AS-BUILT system, not a design proposal requiring confirmation.)*

**DB_TARGET (CORE-8): POSTGRESQL_16** — confirmed independently by master-registry.md ("system-wide PG migration 2026-06-28", Section 4/5/9) in addition to module-registry-SECURITY-2.md.

---

## 🔎 MASTER-REGISTRY ALIGNMENT REVIEW — 2026-07-22

> master-registry.md (v2.10.0) was supplied after the initial SRS was issued. Per RULE-1 (Artifact Authoritative Order) this registry is now read and reconciled against the prior srs.md. Results below; the entity/rule/LOV/screen tables in PART A/B are amended in place where a finding required a change — everything not listed here was checked and found consistent, and is unchanged.

**✓ CONFIRMED — no changes needed:**
| # | Item | Master-registry source | Prior srs.md | Result |
|---|---|---|---|---|
| 1 | Column naming | Sec. 4 PERMANENT EXCEPTION table: `USERS_PK/ROLES_PK/PERMISSIONS_PK/REFRESH_TOKENS_PK/SEC_PAGES_PK`, `USER_ID_FK/ROLE_ID_FK/PERM_ID_FK/PAGE_ID_FK/PARENT_ID_FK`, flags `ENABLED/IS_ACTIVE/REVOKED`, dropdown `PERMISSION_TYPE` (VARCHAR enum) | A3 entity tables | **MATCH** — no correction needed |
| 2 | Core entity set | Sec. 5 "LAYER-1 Foundation Entities": User/Role/Permission/Page/RefreshToken/UserRole/RolePermission — exactly 7 rows for Security, all `⚠️ Active` | ENTITY-SEC-001..007 | **MATCH** |
| 3 | ORG_BRANCH is the correct external target | module-registry-org.md "OUTGOING — WHO CONSUMES ORGANIZATION": `Security (1.2) │ HARD-FK │ ORG_BRANCH — DataScope / SEC_ROLE_BRANCH` | A7 XM candidates | **MATCH** — canonical ID added: **ENTITY-ORG-002** (Branch, table `ORG_BRANCH`, GOVERNED ✓ MODE 2) |
| 4 | Security→Notification is accepted architecture | Conflict #20 CLOSED: Event-Based, not HARD-FK, "same pattern every other module uses" | RULE-SEC-031 | **MATCH** — strengthens RULE-SEC-031's Source; OQ-003 narrowed (see below) |
| 5 | Single-tenant, no TENANT_ID | Conflict #4 CLOSED | (not previously stated) | **MATCH** — added as a note under A2 |

**⚠ AMENDED — split governance state for the DataScope/self-service entities:**

Master-registry Section 15 does **not** treat `SEC_USER_PROFILE` / `SEC_ROLE_BRANCH` / Forgot-Password / Sign-Up as part of Security's CORE PERMANENT EXCEPTION. They are Security's **EXTENSION scope**, tracked separately as **PARTIALLY_READY ⚠️** (new development under an EXCEPTION-status module), with **AQ-006** and **AQ-007** OPEN (non-blocking — a registry version-citation mismatch: the canonical P0 file is cited there as `registry-security.md v2.4.1`, not the `module-registry-SECURITY-2.md` supplied to this session — **NOT VERIFIABLE THIS SESSION** whether the two are the same content under a different filename; flagged, not assumed). **Amendment applied:** ENTITY-SEC-009, ENTITY-SEC-010 (and by the same extension scope, ENTITY-SEC-011/012, which implement Forgot-Password/Sign-Up) are now labeled in A3 as **EXTENSION scope — PARTIALLY_READY ⚠️ (master-registry Sec. 15), not CORE EXCEPTION**, distinct from ENTITY-SEC-001..007/006/007 which remain full EXCEPTION AS-IS. This does not change any field, rule, or API content — only the governance-state label.

**🆕 NEW — two conflicts surfaced by this reconciliation, not yet in master-registry Section 13, raised as OQ-006/OQ-007 (RULE-4 — this engine raises, never silently resolves):**

| Finding | Master-registry says | AS-BUILT Security code says | 
|---|---|---|
| DataScope level values | Section 8 DATA SCOPE RULES: `DataScope levels: Platform / LegalEntity / Branch / Department` | LOV-SEC-002 seed data (extracted-facts-SECURITY.md): `BRANCH_ONLY / BRANCH_AND_CHILDREN / ALL` — different granularity, different names, no Platform/LegalEntity/Department levels at all | 
| Cross-module FK mechanism | module-registry-org.md line "Cross-module FK naming follows CORE-8 SOFT-READ rule — cross-module FKs are validated at service layer, **not DB-level**" | `SEC_USER_PROFILE`/`SEC_ROLE_BRANCH` carry real DB-level FK constraints to `ORG_BRANCH` (`FK_SEC_USER_PROFILE_BRANCH`, `FK_SEC_ROLE_BRANCH_BRANCH`) in addition to service-layer validation | 

Neither finding is covered by Conflict #1/#3 (those are naming-only exceptions). Both are new — see **OQ-006** and **OQ-007** in the OQ Log.

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
| **الموديول** | الأمان والمستخدمون (Security Model + Users) |
| **Feature Code** | SEC-001 |
| **Feature Type** | Engine (1.2) + Master Data (1.3) — hybrid, EXCEPTION module |
| **الإدارة / القسم** | البنية التحتية / الأمان (Platform / Security) |
| **إعداد بواسطة** | SRS Governance Engine (Project 1) — من إعادة هندسة الكود الفعلي |
| **النسخة** | 1.2 (v1.0: 2026-07-22 توليد أولي؛ v1.1: نفس اليوم، مراجعة master-registry.md + تحقيق كودي + إغلاق 13 OQ؛ **v1.2: 2026-08-23، تحديث كامل من backend.zip + business-policies-SECURITY.md/module-registry-SECURITY.md/prd-SECURITY-2.md المُحدَّثة**) |
| **التاريخ** | 2026-07-22 (v1.0) → **2026-08-23 (آخر تحديث)** |
| **الحالة** | Draft — **EXCEPTION (core) / PARTIALLY_READY (extension scope) / AS-IS** — registry-verified + code-investigated + **مُحدَّثة من كود مصدر حقيقي (backend.zip) 2026-08-23** |
| **Open Questions** | 16 إجمالاً عبر ثلاث جولات — **14 CLOSED، 2 OPEN** (OQ-014: لا تنظيف لرموز التفعيل/الاستعادة؛ **OQ-015: DataScope claim غير مُستهلَك — الأهم**) — انظر OQ Log |
| **Governed by** | SRS Governance Engine (Project 1) |
| **Provenance** | REVERSE-ENGINEERED from erp-security (as-built). **v1.2 مصادر**: `backend.zip` (كود مصدر فعلي، تحقُّق مباشر — الأعلى سلطة عند التعارض)، `business-policies-SECURITY.md` (نسخة 2026-08-22، تحمل RULE-SEC-031 مُحدَّثة + POLICY-CLI-AUTH-04 جديدة)، `module-registry-SECURITY.md` (نسخة P0 RETROFIT جديدة، **تحلّ محل** `module-registry-SECURITY-2.md` المُستخدَمة في v1.0/v1.1)، `prd-SECURITY-2.md` (**تحلّ محل** `prd-SECURITY.md`، تضيف US-SEC-018). NOT a forward P0→P1 design. See header notice. |

---

## A2 — السياق الوظيفي (Functional Context)

### ما يشمله هذا الموديول

> يوثّق هذا الموديول الطبقة الأمنية الأساسية للنظام كما هي منفَّذة فعلياً (Layer 4.1 — Backend Runtime): إدارة حسابات المستخدمين (UserAccount)، الأدوار (Role)، الصلاحيات (Permission)، شاشات النظام (Page/SEC_PAGES) وربطها بالصلاحيات (RBAC)، دورة حياة الدخول عبر JWT (تسجيل دخول، تحديث، خروج، تدوير refresh token)، التسجيل الذاتي وتفعيل الحساب، استعادة كلمة المرور، بناء قائمة التنقّل (Menu) ديناميكياً من صلاحيات VIEW، ونطاق البيانات الجغرافي/التنظيمي (DataScope) عبر SEC_ROLE_BRANCH وSEC_USER_PROFILE.

### ما لا يشمله هذا الموديول

> لا يشمل: منطق العمل الخاص بموديولات أخرى (Organization، MasterData، الموارد البشرية)؛ لا يشمل محرك workflow أو موافقات (RULE-13)؛ لا يشمل إرسال الإشعارات فعلياً (ينشر Events فقط — RULE-SEC-031)؛ لا يشمل تصميم DDL أو XM-IDs الرسمية (ملك Project 2)؛ لا يشمل خطط التنفيذ (ملك Project 3).

### وظيفة الموديول

> يمكّن هذا الموديول أي مستخدم من تسجيل الدخول بأمان والوصول فقط لما يملك صلاحية عرضه، ويمكّن المسؤول (Admin) من إدارة المستخدمين والأدوار والصلاحيات وشاشات النظام ونطاق البيانات لكل دور، دون الحاجة لأي تدخل برمجي مباشر.

### الوصف الوظيفي التفصيلي

> النظام مبني على JWT عديم الحالة (Stateless) — لا جلسات على الخادم. عند تسجيل الدخول يُصدَر access token قصير الأجل ويُخزَّن refresh token في cookie مع تدوير إلزامي عند كل استخدام (POLICY-CLI-AUTH-02). يمكن للمستخدم الجديد التسجيل الذاتي؛ يبدأ الحساب معطّلاً (RULE-SEC-030) حتى يُفعَّل عبر رمز يُرسَل بالبريد (عبر Event — RULE-SEC-031؛ **مؤكَّد بالتحقيق البرمجي 2026-08-23**: يوجد الآن Listener فعلي (`AuthEventListener`) يستدعي `NotificationClient` الذي يرسل فعلياً لموديول الإشعارات — انظر RULE-SEC-053 لآلية المصادقة الخاصة بهذا النداء). نموذج الصلاحيات (RBAC) قائم على: كل Page عند إنشائها تُنشئ تلقائياً 4 صلاحيات (VIEW/CREATE/UPDATE/DELETE)؛ عند إسناد Page لدور، تُضاف VIEW تلقائياً ولا يمكن إزالتها بمعزل عن باقي الصلاحيات (POLICY-CLI-RBAC-01). نطاق البيانات (DataScope) يُحدَّد بربط الدور بفرع تنظيمي (ORG_BRANCH) مع مستوى وصول (BRANCH_ONLY / BRANCH_AND_CHILDREN / ALL)، وتُشتَق قائمة الفروع المسموحة كـ claim داخل الـ JWT (RULE-SEC-037) — **لكن ⚠ مؤكَّد 2026-08-23: لا شيء في الكود يستهلك هذا الـ claim فعلياً لتقييد الوصول للبيانات** (بحث شامل في كامل `erp-security` عن أي استهلاك لـ `allowedBranches` خارج توليده لم يُظهر شيئاً) — الـ claim يُصدَر لكنه غير مُفعَّل، وهذه أكبر فجوة متبقية في ميزة DataScope ككل. مرفوعة كـ **OQ-015**.

#### الوضع الحالي

| الخطوات | الجهة | ملاحظات |
|---|---|---|
| الموديول منفَّذ فعلياً في الإنتاج (erp-security) | فريق التطوير | لم يمرّ بمسار الحوكمة الأمامي (P0→P1) |
| هذه الوثيقة أول توثيق حوكمة رسمي له | SRS Governance Engine | إعادة هندسة من الكود، وليست تصميماً جديداً |

#### الصعوبات الحالية

| # | الصعوبة |
|---|---|
| 1 | لا يوجد Business Code معياري على أي كيان رئيسي (UserAccount/Role/Page) — يُستخدم مفتاح عمل طبيعي بدلاً منه (username/roleCode/pageCode)، وهذا يخالف BC-RULE-1/2 القياسية (موثَّق كانحراف، لا كتوصية تصحيح). |
| 2 | Rate limiting مبني في الذاكرة (in-memory) وغير آمن للتوسّع الأفقي — موثَّق صراحة في الكود نفسه (POLICY-CLI-AUTH-01). ✓ مُصحَّح 2026-08-23: يغطي فعلياً 4 نقاط (login/signup/forgot-password/reset-password) لا نقطة واحدة كما وُثِّق أول مرة — انظر RULE-SEC-050. |
| 3 | استدعاءات cross-module لـ Organization/MasterData (OrgBranchClient / MasterDataLookupClient) تمرّر Authorization header للمستخدم الحالي حرفياً — **قرار Architect نهائي (OQ-004 CLOSED)**: يبقى كما هو. Notification وحدها لها آلية مختلفة الآن (حساب خدمة — RULE-SEC-053، جديدة 2026-08-23). |
| 4 | تسمية حزمة الكود الأساسية (`com.example.security`) لا تتبع نمط باقي الموديولات (`com.example.erp.*`) — لا يزال غير مفسَّر (AQ-SEC-002 لا يزال OPEN في module-registry-SECURITY.md اعتباراً من 2026-08-23). |
| 5 | تغطية اختبارات غير مكتملة على أكثر الخدمات كثافة بقواعد الأعمال (`AuthService` بالكامل) — تحسَّنت جزئياً (اختبارات أُضيفت لـ Permission/UserProfile أثناء إصلاح OQ-005/OQ-010)، لا تزال ناقصة إجمالاً. |
| 6 **[جديد 2026-08-23]** | JWT `allowedBranches[]` claim (RULE-SEC-037) **يُصدَر لكن لا يُستهلَك في أي مكان بالكود** — DataScope غير مُفعَّل فعلياً عند الوصول للبيانات. أكبر فجوة متبقية في الميزة — مرفوعة كـ **OQ-015**. |
| 7 **[جديد 2026-08-23]** | لا مهمة تنظيف دورية لـ `PASSWORD_RESET_TOKEN`/`ACCOUNT_ACTIVATION_TOKEN` (بعكس `REFRESH_TOKENS`) — مرفوعة كـ **OQ-014**. |
| 8 **[جديد 2026-08-23]** | ترقيم سكربتات الهجرة يحتوي فجوة: `001`، `002`، ثم `004`، `005` — **لا يوجد `003`** في backend.zip. غير محلول، غير حرج (moduleregistry-SECURITY.md يُثبِّت نفس الملاحظة دون تفسير). |
| 9 **[جديد 2026-08-23]** | تعارض توثيقي: `security-registry.md` (ضمن backend.zip، `governance/modules/SECURITY/`) يذكر Conflict #20 في master-registry.md كـ **OPEN**، بينما هذه الوثيقة وثَّقت سابقاً (بعد قراءة مباشرة لـ master-registry.md بتاريخ 2026-07-22) أنه **CLOSED**. لا يمكن حسمه بدون نسخة حالية من master-registry.md — مرفوع كـ **OQ-016**. |

#### النظام المقترح وفوائده

> *(لا ينطبق — هذا توثيق AS-IS لنظام قائم فعلاً، وليس اقتراحاً لنظام جديد. أي "فائدة" هنا هي فائدة توثيق الحوكمة نفسها.)*

| # | الفائدة |
|---|---|
| 1 | إتاحة نقطة مرجعية حوكمية واحدة (ENTITY/RULE/LOV/SCR/API-IDs) لموديول كان بلا أي توثيق حوكمي سابق. |
| 2 | كشف صريح لكل الانحرافات عن المعايير القياسية (BC-RULE، LOV-RULE، تسمية الحقول) بدل إخفائها. |
| 3 | تمكين Project 2 (DB Engine) من قراءة بنية البيانات الفعلية دون إعادة اكتشافها من الكود مباشرة. |

### ملاحظات عامة

- هذا الموديول **EXCEPTION** بقرار معماري (module-registry-SECURITY-2.md، عبر platform-standards-2.md M.A.4) — لا يُعاد بناؤه، بل يُستهلَك AS-IS من قِبل باقي الموديولات.
- أسماء الأعمدة الفعلية لكيانات الأمان (USERS/ROLES/PERMISSIONS/SEC_PAGES/REFRESH_TOKENS) هي **PERMANENT EXCEPTION** عن قواعد التسمية القياسية (Section 5.4.2) — تُستخدم الأسماء كما وُجدت في الكود (مثال: `IS_ACTIVE` بدل `isActiveFl`، `USERS_PK` بدل `entityPk`) حيثما وُجدت، مع الإبقاء على التسمية القياسية حيث يتطابق الكود فعلاً معها (مثال: `isActiveFl` على `SecRoleBranch`/`SecUserProfile` وهما فعلاً مسمّاة هكذا في الكود).
- خمس هويات ENTITY-SEC كانت "provisional" فقط في تعليقات الكود (UserAccount، Role، Permission، Page) — هذه الوثيقة تُسنِد لها هويات ENTITY-ID نهائية الآن (ملكية P1 لهذا الـ namespace)، دون تغيير أي شيء في تصنيف P0 السابق (لا يوجد P0 سابق أصلاً لهذا الموديول).
- عناصر قرار Architect نهائية (2026-07-22، OQ-001/OQ-002 CLOSED): `PREFERRED_LANG` يبقى نصاً حراً بقرار — لن يُحوَّل لـ LOV. `EMPLOYEE_ID_FK` يبقى بلا FK بقرار — لا يوجد موديول HR الآن، يُعاد فتح الملف تلقائياً فقط عند بدء حوكمة HR.
- ✓ مؤكَّد من master-registry.md (Conflict #4، CLOSED): النظام أحادي المستأجر (single-tenant) بشكل دائم — لا يوجد عمود `TENANT_ID` بتاتاً، ولا يوجد كيان DataScope منفصل. يطابق غياب أي حقل tenant في كيانات هذا الموديول.

---

## A3 — الكيانات والحقول (Entities & Fields)

> ⚠ **PERMANENT EXCEPTION مطبَّقة على كل الكيانات في هذا القسم** (Section 5.4.2): كيانات الأمان تستخدم أسماء الأعمدة الفعلية من الكود، وليس اصطلاح `entityPk`/`isActiveFl` القياسي، حيثما اختلفا.
> ⚠ **لا يوجد Business Code معياري** على أي كيان رئيسي هنا (انحراف موثَّق عن BC-RULE-1/2 — انظر ملاحظة تحت كل كيان).

---

### ENTITY-SEC-001 — حساب المستخدم (UserAccount)

| البند | القيمة |
|---|---|
| **الجدول** | USERS |
| **النوع** | PRIVATE |
| **Business Code** | **NOT IMPLEMENTED** — انحراف عن BC-RULE-1/2. المفتاح الطبيعي هو `username` (فريد، يُدخله المستخدم عند التسجيل، وليس مُولَّداً تلقائياً). |
| **المصدر** | AuthService.signup()/UserService — extracted-facts-SECURITY.md Layer 1 |
| **العمليات** | Create (Admin أو Self-signup), Read, Update, Delete (مقيَّد — RULE-SEC-049), Search |
| **Cross-Module** | None مؤكَّد — لا يوجد دليل على استهلاك SHARED لهذا الكيان من موديول آخر في المستندات المرفقة (NOT VERIFIABLE THIS SESSION بدون master-registry) |

#### حقول الكيان

| اسم الحقل الفعلي | نوع البيانات (Postgres) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| `id` (عمود `USERS_PK`) | BIGINT (PK, IDENTITY) | نظام | — | مُولَّد تلقائياً | المعرف | ID |
| `username` | VARCHAR(80) | نعم | — | فريد (`UK_USERS_USERNAME`) — RULE-SEC-049 | اسم المستخدم | Username |
| `email` | VARCHAR(150) | لا | — | فريد (`UK_USERS_EMAIL`) — RULE-SEC-041 | البريد الإلكتروني | Email |
| `password` | VARCHAR(200) | نعم | — | تُخزَّن كـ BCrypt hash فقط — لا تُعرَض أبداً | كلمة المرور | Password |
| `enabled` | SMALLINT (Boolean via converter) | نعم | true/false | افتراضي `TRUE`؛ `FALSE` عند التسجيل الذاتي (RULE-SEC-030) | مُفعَّل | Enabled |

> **قاعدة Label إلزامية مطبَّقة أعلاه.** لا توجد حقول `createdBy/createdAt/updatedBy/updatedAt` مؤكَّدة على `UserAccount` نفسها في extracted-facts-SECURITY.md (غير موثَّقة صراحة لهذا الكيان تحديداً) — **OQ لم يُرفع** لأن الأثر غير حرج على السرد الوظيفي (Zero-Question Protocol الخطوة الرابعة تفشل بلا platform-standards.md مرفق هذه الجلسة، لكن التأثير غير حاسم).

> **صف بيانات خاص [جديد 2026-08-23]**: صف واحد إضافي مزروع بواسطة `005_notification_service_account_seed.sql` — `username = 'svc-notification'`، **بلا أي صف في `USER_ROLES`** (صلاحيات صفرية عمداً)، `password` = BCrypt hash لقيمة عشوائية غير مُسجَّلة (لا يُتوقَّع دخول فعلي به). ليس مستخدماً بشرياً — حساب خدمة داخلي يُستخدَم حصراً بواسطة `NotificationClient` (RULE-SEC-053) لإصدار JWT لنداءات Notification المُطلَقة من تدفقات مجهولة الهوية. **لا يظهر في شاشة إدارة المستخدمين (SCR-SEC-002) كحالة خاصة موثَّقة** — يظهر كسجل مستخدم عادي بلا أدوار؛ إخفاؤه أو تمييزه في الواجهة قرار منتج مستقبلي، خارج نطاق AS-IS.

---

### ENTITY-SEC-002 — الدور (Role)

| البند | القيمة |
|---|---|
| **الجدول** | ROLES |
| **النوع** | PRIVATE |
| **Business Code** | **NOT IMPLEMENTED كـ BC-RULE قياسي** — المفتاح الطبيعي `roleCode`: يُدخله المستخدم (Admin) عند الإنشاء، وليس مُولَّداً تلقائياً، لكنه **غير قابل للتعديل بعد الإنشاء** (POLICY-CLI-ROLE-01) — يطابق BC-RULE-4 (read-only) لكن يخالف BC-RULE-2 (auto-generated). |
| **المصدر** | RoleService.java — Governance tag في الكود: "BE-REQ-ROLEACCESS-001" |
| **العمليات** | Create, Read, Update (roleCode ثابت), Delete (مقيَّد إن كانت مُسنَدة لمستخدمين — RULE-SEC-048)، Activate/Deactivate (مساران منفصلان — ✓ مُصحَّحة 2026-08-23، انظر SCR-SEC-003) |
| **Cross-Module** | None مؤكَّد |

#### حقول الكيان

| اسم الحقل الفعلي | نوع البيانات (Postgres) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| `id` (عمود `ROLES_PK`) | BIGINT (PK, IDENTITY) | نظام | — | مُولَّد تلقائياً | المعرف | ID |
| `roleName` (عمود `NAME`) | VARCHAR(60) | نعم | — | فريد (`UK_ROLES_NAME`) | اسم الدور | Role Name |
| `roleCode` | VARCHAR(60) | نعم | — | فريد (`UK_ROLES_ROLE_CODE`)، **غير قابل للتعديل بعد الإنشاء** | رمز الدور | Role Code |
| `description` | VARCHAR(500) | لا | — | — | الوصف | Description |
| `active` (عمود `IS_ACTIVE`) | SMALLINT (Boolean) | نعم | true/false | افتراضي `TRUE` | نشط | Active |

> ⚠ **PERMANENT EXCEPTION**: العمود الفعلي `IS_ACTIVE` وليس `isActiveFl` — التسمية القياسية لا تنطبق هنا (كيان أمان).
> ملاحظة توثيقية (AS-IS، غير محلولة هنا): تعليق كود قديم في `RoleService` يشير إلى أن `active` كان `@Transient` وغير محفوظ — لكن `Role.java` الحالي يُخزِّن `IS_ACTIVE` فعلياً كعمود حقيقي. تعارض توثيق/كود قديم، مُثبَت هنا كما هو دون حل.

---

### ENTITY-SEC-003 — الصلاحية (Permission)

| البند | القيمة |
|---|---|
| **الجدول** | PERMISSIONS |
| **النوع** | PRIVATE |
| **Business Code** | لا ينطبق — كيان صلاحيات نظامي، لا مفهوم Business Code له |
| **المصدر** | PageService.createPermissionRecords(), PermissionController |
| **العمليات** | Create (تلقائي عبر Page، أو يدوي عبر PermissionController)، Read, Update |
| **Cross-Module** | None |

#### حقول الكيان

| اسم الحقل الفعلي | نوع البيانات (Postgres) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| `id` (عمود `PERMISSIONS_PK`) | BIGINT (PK, IDENTITY) | نظام | — | مُولَّد تلقائياً | المعرف | ID |
| `name` | VARCHAR(150) | نعم | — | فريد (`UK_PERMS_NAME`)، النمط `PERM_<PAGE_CODE>_<TYPE>` | اسم الصلاحية | Permission Name |
| `permissionType` | VARCHAR(20) | لا | LOV-SEC-001 (انظر انحراف أدناه) | `NULL` للصلاحيات النظامية غير المرتبطة بشاشة | نوع الصلاحية | Permission Type |
| `page` (عمود `PAGE_ID_FK`) | BIGINT (FK) | لا | ENTITY-SEC-004 | `NULL` للصلاحيات النظامية | الشاشة المرتبطة | Related Page |

> ⚠ **انحراف موثَّق (LOV-4)**: `permissionType` مُخزَّن كـ Java enum عبر `@Enumerated(STRING)`، وليس مقروءاً من `MD_LOOKUP_DETAIL` كما تتطلب LOV-1/LOV-4 القياسية. مُوثَّق AS-IS في LOV-SEC-001 (A5) — غير مُعاد تصميمه هنا.

---

### ENTITY-SEC-004 — الشاشة (Page)

| البند | القيمة |
|---|---|
| **الجدول** | SEC_PAGES |
| **النوع** | PRIVATE |
| **Business Code** | **NOT IMPLEMENTED كـ BC-RULE قياسي** — المفتاح الطبيعي `pageCode` (يُدخله Admin، غير مُولَّد تلقائياً؛ يُحوَّل uppercase+trim تلقائياً عند الحفظ). |
| **المصدر** | PageService.java — "Pages هي الـ DETAIL في نموذج RBAC، والأدوار هي الـ MASTER" |
| **العمليات** | Create (يُولِّد 4 صلاحيات تلقائياً — RULE-SEC-047)، Read, Update, Deactivate, Reactivate |
| **Cross-Module** | None مؤكَّد — حقل `module` نصّي تصنيفي فقط (مثال: SECURITY، FINANCE) وليس FK حقيقياً لأي جدول موديول آخر |

#### حقول الكيان

| اسم الحقل الفعلي | نوع البيانات (Postgres) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| `id` (عمود `SEC_PAGES_PK`) | BIGINT (PK, SEQUENCE `SEC_PAGES_SEQ`) | نظام | — | — | المعرف | ID |
| `pageCode` | VARCHAR(50) | نعم | نمط `^[A-Z0-9_]+$`، 2-50 حرف — RULE-SEC-046 | فريد (`UK_PAGES_CODE`) | رمز الشاشة | Page Code |
| `nameAr` | VARCHAR(100) | نعم | — | — | الاسم بالعربي | Name (Arabic) |
| `nameEn` | VARCHAR(100) | نعم | — | — | الاسم بالإنجليزي | Name (English) |
| `route` | VARCHAR(200) | نعم | نمط `^/[a-zA-Z0-9/_-]+$` — RULE-SEC-046 | فريد (`UK_PAGES_ROUTE`) | المسار | Route |
| `icon` | VARCHAR(50) | لا | — | — | الأيقونة | Icon |
| `module` | VARCHAR(50) | لا | — | تصنيفي نصّي (مثال: SECURITY، FINANCE) | الموديول | Module |
| `parentId` | BIGINT | لا | ENTITY-SEC-004 (ذاتي) | لا يجوز أن يشير لنفسه — RULE-SEC-046 | الشاشة الأب | Parent Page |
| `displayOrder` | INTEGER | لا | — | — | ترتيب العرض | Display Order |
| `active` | SMALLINT (Boolean) | لا (افتراضي كودي `true`) | true/false | — | نشط | Active |
| `description` | VARCHAR(500) | لا | — | — | الوصف | Description |

---

### ENTITY-SEC-005 — رمز التحديث (RefreshToken)

| البند | القيمة |
|---|---|
| **الجدول** | REFRESH_TOKENS |
| **النوع** | PRIVATE |
| **Business Code** | لا ينطبق |
| **المصدر** | extracted-facts-SECURITY.md — لا يحمل ENTITY-SEC-ID في الكود؛ **هذه الوثيقة تُسنِد الهوية الآن** (ملكية P1 لـ namespace الـ ENTITY-ID) |
| **العمليات** | Create (عند login/refresh)، Revoke، Scheduled Cleanup |
| **Cross-Module** | None |

#### حقول الكيان

| اسم الحقل الفعلي | نوع البيانات (Postgres) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| `id` (عمود `REFRESH_TOKENS_PK`) | BIGINT (PK, IDENTITY) | نظام | — | — | المعرف | ID |
| `jti` | VARCHAR(64) | نعم | — | فريد | معرّف الرمز (JTI) | Token JTI |
| `user` (عمود `USER_ID_FK`) | BIGINT (FK) | نعم | ENTITY-SEC-001 | — | المستخدم | User |
| `createdAt` | TIMESTAMP | نظام | — | `@CreationTimestamp`، غير قابل للتحديث | تاريخ الإنشاء | Created At |
| `expiresAt` | TIMESTAMP | نعم | — | — | تاريخ الانتهاء | Expires At |
| `revoked` | SMALLINT (Boolean) | نعم | افتراضي `false` | ينتقل لـ `true` عند logout أو عند تدوير رمز جديد | مُلغى | Revoked |

> ملاحظة: لا توجد أعمدة `updatedBy/updatedAt` — الكيان يتبع نمط "plain audit fields" (`createdAt` فقط)، وليس `AuditableEntity` الكامل، كما هو موثَّق في extracted-facts-SECURITY.md.

---

### ENTITY-SEC-006 — ربط المستخدم بالدور (USER_ROLES) — [INTERNAL / JOIN]

| البند | القيمة |
|---|---|
| **الجدول** | USER_ROLES |
| **النوع** | INTERNAL / JOIN — بدون `@Entity` مستقل، `@JoinTable` فقط بين ENTITY-SEC-001 و ENTITY-SEC-002 |
| **الحقول** | `USER_ID_FK` → ENTITY-SEC-001، `ROLE_ID_FK` → ENTITY-SEC-002 (PK مركّب) |
| **ملاحظة** | لا حقول تدقيق (audit fields) — يطابق نمط "INTERNAL/JOIN ENTITY" القياسي في Section 5.4.2 |

---

### ENTITY-SEC-007 — ربط الدور بالصلاحية (ROLE_PERMISSIONS) — [INTERNAL / JOIN]

| البند | القيمة |
|---|---|
| **الجدول** | ROLE_PERMISSIONS |
| **النوع** | INTERNAL / JOIN — `@JoinTable` بين ENTITY-SEC-002 و ENTITY-SEC-003 |
| **الحقول** | `ROLE_ID_FK` → ENTITY-SEC-002، `PERM_ID_FK` → ENTITY-SEC-003 (PK مركّب) |
| **ملاحظة** | لا حقول تدقيق |

---

### ENTITY-SEC-009 — الملف التعريفي للمستخدم (SecUserProfile)

| البند | القيمة |
|---|---|
| **⚠ حالة الحوكمة** | **EXTENSION scope — PARTIALLY_READY ⚠️** (master-registry.md Section 15) — **ليست** جزءاً من CORE EXCEPTION الدائم لـ Security؛ تطوير جديد تحت موديول بحالة EXCEPTION. AQ-006/AQ-007 مفتوحتان (غير حاجبتين — تعارض توثيقي في رقم إصدار الملف المرجعي، غير معماري) |
| **الجدول** | SEC_USER_PROFILE |
| **النوع** | PRIVATE — **SHARED (Consumer)** لـ Branch (ENTITY-ORG-002، جدول `ORG_BRANCH`، مالكها Organization 1.1) |
| **Business Code** | لا ينطبق — امتداد 1:1 لـ UserAccount عبر مفتاح مشترك |
| **المصدر** | ENTITY-SEC-009 مؤكَّد في تعليق الكود |
| **العمليات** | Create, Read, Update (لا يوجد Delete — التعطيل عبر `isActiveFl`/UPDATE فقط) |
| **Cross-Module** | Consumes SHARED ORG_BRANCH (Organization 1.1) — نوع الاعتمادية: **HARD-FK (DB) + SOFT-READ (Service، عبر OrgBranchClient)** — مرشَّح XM candidate: **XM-SEC-001** (يُصنَّف رسمياً في MODE 1.5) |

#### حقول الكيان

| اسم الحقل الفعلي | نوع البيانات (Postgres) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| `userIdFk` | BIGINT (PK، مشترك مع USERS، `@MapsId`) | نظام | ENTITY-SEC-001 | ليس PK توليدياً — مُسنَد يدوياً | المستخدم | User |
| `branchIdFk` | BIGINT (FK) | نعم | ORG_BRANCH (خارجي) | يُتحقَّق عبر OrgBranchClient REST — RULE-SEC-034 | الفرع | Branch |
| `fullNameAr` | VARCHAR(200) | لا | — | — | الاسم الكامل بالعربي | Full Name (Arabic) |
| `fullNameEn` | VARCHAR(100) | لا | — | — | الاسم الكامل بالإنجليزي | Full Name (English) |
| `preferredLang` | VARCHAR(10) | لا | **نص حر بقرار نهائي — OQ-001 CLOSED** | يبقى نصاً حراً — قرار Architect 2026-07-22، لن يُحوَّل لـ LOV | اللغة المفضلة | Preferred Language |
| `employeeIdFk` | BIGINT | لا | **غير مقيَّد بقرار نهائي — OQ-002 CLOSED** | لا يوجد موديول HR حالياً — يبقى بلا FK حتى تُحوكَم HR | معرّف الموظف | Employee ID |
| `isActiveFl` | SMALLINT (Boolean) | نعم | افتراضي `true` | تسمية قياسية (تطابق الاصطلاح العام، وليست استثناءً) | نشط | Active |

---

### ENTITY-SEC-010 — نطاق بيانات الدور بالفرع (SecRoleBranch)

| البند | القيمة |
|---|---|
| **⚠ حالة الحوكمة** | **EXTENSION scope — PARTIALLY_READY ⚠️** (master-registry.md Section 15) — نفس ملاحظة ENTITY-SEC-009 أعلاه |
| **الجدول** | SEC_ROLE_BRANCH |
| **النوع** | PRIVATE — **SHARED (Consumer)** لـ Branch (ENTITY-ORG-002) |
| **Business Code** | لا ينطبق |
| **المصدر** | ENTITY-SEC-010 مؤكَّد في تعليق الكود |
| **العمليات** | Create, Read, Update, Delete |
| **Cross-Module** | Consumes SHARED ORG_BRANCH — HARD-FK (DB) + SOFT-READ (Service) — **XM-SEC-002** (MODE 1.5). كذلك يستهلك `DATA_ACCESS_LEVEL` من MasterData (MD_MASTER_LOOKUP) — SOFT-READ فقط، بلا XM-ID مؤكَّد بعد. |

#### حقول الكيان

| اسم الحقل الفعلي | نوع البيانات (Postgres) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| `roleIdFk` | BIGINT (PK جزء 1، FK) | نعم | ENTITY-SEC-002 | — | الدور | Role |
| `branchIdFk` | BIGINT (PK جزء 2، FK) | نعم | ORG_BRANCH (خارجي) | — | الفرع | Branch |
| `dataAccessLevel` | VARCHAR(30) | نعم | LOV-SEC-002 | يُتحقَّق عبر MasterDataLookupClient — RULE-SEC-035 | مستوى الوصول للبيانات | Data Access Level |
| `isActiveFl` | SMALLINT (Boolean) | نعم | افتراضي `true` | — | نشط | Active |

> **PK مركّب** (`roleIdFk` + `branchIdFk`) — لا تكرار مسموح لنفس الزوج (RULE-SEC-036: تحقُّق مزدوج على مستوى الخدمة + مستوى الـ DB).

---

### ENTITY-SEC-011 — رمز استعادة كلمة المرور (PasswordResetToken)

| البند | القيمة |
|---|---|
| **الجدول** | PASSWORD_RESET_TOKEN |
| **النوع** | PRIVATE |
| **Business Code** | لا ينطبق |
| **المصدر** | ENTITY-SEC-011 مؤكَّد في تعليق الكود |
| **العمليات** | Create (forgot-password)، Consume (reset-password) |
| **Cross-Module** | None |

#### حقول الكيان

| اسم الحقل الفعلي | نوع البيانات (Postgres) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| `tokenPk` | BIGINT (PK، SEQUENCE) | نظام | — | — | المعرف | ID |
| `token` | VARCHAR(64) | نعم | — | فريد | الرمز | Token |
| `user` (FK) | BIGINT | نعم | ENTITY-SEC-001 | — | المستخدم | User |
| `createdAt` | TIMESTAMP | نظام | — | `@CreationTimestamp` | تاريخ الإنشاء | Created At |
| `expiresAt` | TIMESTAMP | نعم | — | — | تاريخ الانتهاء | Expires At |
| `usedFl` | SMALLINT (Boolean) | نعم | افتراضي `false` | أحادي الاتجاه false→true — RULE-SEC-033 | مُستخدَم | Used |

> عند صدور رمز جديد، أي رمز سابق غير منتهٍ لنفس المستخدم يُلغى (RULE-SEC-039).

---

### ENTITY-SEC-012 — رمز تفعيل الحساب (AccountActivationToken)

| البند | القيمة |
|---|---|
| **الجدول** | ACCOUNT_ACTIVATION_TOKEN |
| **النوع** | PRIVATE |
| **Business Code** | لا ينطبق |
| **المصدر** | ENTITY-SEC-012 مؤكَّد في تعليق الكود |
| **العمليات** | Create (signup)، Consume (activate) |
| **Cross-Module** | None |

#### حقول الكيان

| اسم الحقل الفعلي | نوع البيانات (Postgres) | إلزامي | القيم / المصدر | ملاحظات | Label-AR | Label-EN |
|---|---|---|---|---|---|---|
| `tokenPk` | BIGINT (PK، SEQUENCE) | نظام | — | — | المعرف | ID |
| `token` | VARCHAR(64) | نعم | — | فريد | الرمز | Token |
| `user` (FK) | BIGINT | نعم | ENTITY-SEC-001 | — | المستخدم | User |
| `createdAt` | TIMESTAMP | نظام | — | `@CreationTimestamp` | تاريخ الإنشاء | Created At |
| `expiresAt` | TIMESTAMP | نعم | — | — | تاريخ الانتهاء | Expires At |
| `usedFl` | SMALLINT (Boolean) | نعم | افتراضي `false` | أحادي الاتجاه — RULE-SEC-033 | مُستخدَم | Used |

---

> **ملاحظة توثيقية عامة (A3):** جدول `SEC_MENU_ITEM` أُسقِط أثناء الهجرة (`DROP TABLE IF EXISTS`) — غير مُستخدَم بتاتاً من التطبيق الحالي (`MenuService` يبني القوائم ديناميكياً من `SEC_PAGES` + الصلاحيات). لا يُعامَل ككيان هنا، ولا يُعاد إحياؤه — تسجيلاً لتاريخ الهجرة فقط.

---

## A4 — قواعد التحقق (Business Rules)

> **قاعدة إلزامية:** هذا القسم هو المصدر الوحيد لتعريف القواعد. PART B يُشير للقواعد بـ RULE-ID فقط.
> القواعد RULE-SEC-030 حتى RULE-SEC-041 محتفَظ بأرقامها كما وُجدت مُسنَدة بالفعل في business-policies-SECURITY.md. القواعد RULE-SEC-042 حتى RULE-SEC-052 هويّات جديدة أسنَدَتها هذه الوثيقة (ملكية P1) لبنود كانت موسومة `POLICY-SEC-*` في نسخة سابقة من business-policies-SECURITY.md — **أُعيدت تسميتها لاحقاً في المصدر إلى `POLICY-CLI-*`** (تصحيح توثيقي 2026-08-22: `POLICY-SEC` لم يكن اصطلاحاً مسجَّلاً أصلاً؛ `POLICY-CLI-*` يطابق الاصطلاح المُستخدَم فعلياً في business-policies-org.md/-filesvc.md/-notification.md) — **تغيير تسمية فقط، لا أثر وظيفي**؛ RULE-SEC-ID الخاصة بكل بند هنا لم تتغيّر. RULE-SEC-053 (جديدة، 2026-08-23) لبند `POLICY-CLI-AUTH-04` المُضاف حديثاً.

---

### RULE-SEC-030 — تعطيل الحساب المسجَّل ذاتياً افتراضياً

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-001 |
| **Trigger** | عند التسجيل الذاتي (signup) |
| **Statement** | The system MUST create self-registered accounts with `enabled = false` until activated |
| **Message-AR** | حسابك قيد التفعيل — يرجى تأكيد بريدك الإلكتروني أولاً |
| **Message-EN** | Your account is pending activation — please confirm your email first |
| **Source** | AuthService.signup(), service/AuthService.java:324 |

### RULE-SEC-031 — الإشعارات عبر أحداث لا نداءً مباشراً

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-001, ENTITY-SEC-011, ENTITY-SEC-012 |
| **Trigger** | عند signup أو forgot-password |
| **Statement** | The system MUST publish an ApplicationEvent (AccountActivationRequestedEvent / PasswordResetRequestedEvent) instead of calling a Notification service directly |
| **Message-AR** | — (قاعدة معمارية داخلية، لا تظهر رسالة للمستخدم) |
| **Message-EN** | — (internal architectural rule, no user-facing message) |
| **Source** | AuthService.java lines 337-338, 399 — Rationale: architectural decoupling. **✅ GAP-SEC-02 RE-CLOSED (2026-08-23, direct backend.zip verification — SUPERSEDES the 2026-07-22 finding below):** a real listener now exists — `AuthEventListener` (`erp-security`, `@TransactionalEventListener(phase = AFTER_COMMIT)`) reacts to both events and calls `NotificationClient`, which `POST`s to erp-notification's `/api/v1/notifications/send`. Confirmed by direct source read: `service/AuthEventListener.java` (24 lines, both handlers present), `client/NotificationClient.java` (mints a real JWT for a dedicated service account — see new **RULE-SEC-053** below). **⚠ Governance history note (HR-1 disclosure, not silently overwritten):** the 2026-07-22 code investigation in this same document's history exhaustively searched 9 branches and found NO listener anywhere, and closed OQ-003 on that basis with high confidence. That finding was correct *for the code as it existed then* — `AuthEventListener`/`NotificationClient`/the `svc-notification` service account did not exist at that point. The code has since been extended (new files, new migration `005_notification_service_account_seed.sql`) to add this integration. Both investigations were accurate for their respective snapshots; this is not a contradiction, it is the module evolving. See OQ-003 entry in the OQ Log for the full before/after record — do not delete the prior finding, per this document's own conflict-log discipline. |

### RULE-SEC-032 — صلاحية رمز التفعيل/الاستعادة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-011, ENTITY-SEC-012 |
| **Trigger** | عند activate أو reset-password |
| **Statement** | The system MUST reject activation/reset if the token is invalid, expired, or already used |
| **Message-AR** | الرمز غير صالح أو منتهي الصلاحية |
| **Message-EN** | Token is invalid or has expired |
| **Source** | AuthService.activateAccount() lines 356-361, AuthService.resetPassword() lines 419-424 |

### RULE-SEC-033 — الرمز يُستخدَم مرة واحدة فقط

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-011, ENTITY-SEC-012 |
| **Trigger** | فوراً عند نجاح الاستهلاك |
| **Statement** | The system MUST mark the token as used immediately on success and MUST reject any further use of the same token |
| **Message-AR** | هذا الرمز مُستخدَم مسبقاً |
| **Message-EN** | This token has already been used |
| **Source** | نفس المصادر أعلاه، `token.setUsed(true)` |

### RULE-SEC-034 — التحقق من الفرع النشط (Cross-Module)

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-009 |
| **Trigger** | عند إنشاء أو تعديل SecUserProfile |
| **Statement** | The system MUST validate that `branchIdFk` references an existing, active ORG_BRANCH row via cross-module call before saving |
| **Message-AR** | الفرع المحدَّد غير موجود أو غير نشط |
| **Message-EN** | Selected branch does not exist or is not active |
| **Source** | SecUserProfileService.create()/update(), OrgBranchClient.java:46 |

### RULE-SEC-035 — إلزامية وصحة مستوى الوصول للبيانات

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-010 |
| **Trigger** | عند إنشاء أو تعديل SecRoleBranch |
| **Statement** | The system MUST require `dataAccessLevel` and MUST validate it is an active LOV-SEC-002 code |
| **Message-AR** | مستوى الوصول للبيانات إلزامي ويجب أن يكون قيمة معتمَدة |
| **Message-EN** | Data access level is required and must be a valid, active value |
| **Source** | SecRoleBranchService.assertValidDataAccessLevel(), service/SecRoleBranchService.java:143-148 |

### RULE-SEC-036 — منع تكرار إسناد الدور-الفرع

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-010 |
| **Trigger** | عند الإنشاء |
| **Statement** | The system MUST prevent duplicate (roleIdFk, branchIdFk) assignments |
| **Message-AR** | هذا الفرع مُسنَد بالفعل لهذا الدور |
| **Message-EN** | This branch is already assigned to this role |
| **Source** | SecRoleBranchService.create() line 68 + composite PK (SecRoleBranchId.java) |

### RULE-SEC-037 — اشتقاق الفروع المسموحة داخل JWT

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-001, ENTITY-SEC-010 |
| **Trigger** | عند login / refresh (إصدار JWT) |
| **Statement** | The system MUST derive the `allowedBranches[]` JWT claim from the user's active SEC_ROLE_BRANCH assignments across active roles, collapsing an unbounded list to a single "ALL" sentinel where applicable |
| **Message-AR** | — (قاعدة داخلية، لا رسالة مستخدم) |
| **Message-EN** | — (internal rule, no user-facing message) |
| **Source** | AuthService.resolveAllowedBranches(), service/AuthService.java:225-244 |

### RULE-SEC-038 — منع تعداد البريد الإلكتروني (Anti-Enumeration)

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-001, ENTITY-SEC-011 |
| **Trigger** | عند forgot-password |
| **Statement** | The system MUST return an identical response regardless of whether the submitted email exists |
| **Message-AR** | إذا كان بريدك مسجَّلاً لدينا، ستصلك رسالة استعادة كلمة المرور |
| **Message-EN** | If your email is registered, you will receive a password reset message |
| **Source** | AuthService.forgotPassword() lines 384-404 |

### RULE-SEC-039 — إلغاء الرموز السابقة عند إصدار رمز جديد

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-011 |
| **Trigger** | عند forgot-password (إصدار رمز جديد) |
| **Statement** | The system MUST invalidate any prior unexpired reset token for the same user when issuing a new one |
| **Message-AR** | — (سلوك داخلي شفاف للمستخدم) |
| **Message-EN** | — (internal behavior, transparent to the user) |
| **Source** | AuthService.forgotPassword() lines 385-388 |

### RULE-SEC-040 — تفرُّد اسم المستخدم عند التسجيل الذاتي

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-001 |
| **Trigger** | عند signup |
| **Statement** | The system MUST require globally unique username on signup |
| **Message-AR** | اسم المستخدم مستخدَم بالفعل |
| **Message-EN** | Username already exists |
| **Source** | AuthService.signup() lines 313-317 (SIGNUP_USERNAME_ALREADY_EXISTS) |

### RULE-SEC-041 — تفرُّد البريد الإلكتروني عند التسجيل الذاتي

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-001 |
| **Trigger** | عند signup |
| **Statement** | The system MUST require globally unique email on signup |
| **Message-AR** | البريد الإلكتروني مستخدَم بالفعل |
| **Message-EN** | Email already exists |
| **Source** | AuthService.signup() lines 313-317 (SIGNUP_EMAIL_ALREADY_EXISTS), UserAccountRepository.existsByEmailIgnoreCase() |

### RULE-SEC-042 — VIEW تلقائية وغير قابلة للإزالة المنفردة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-002, ENTITY-SEC-003, ENTITY-SEC-004 |
| **Trigger** | عند إسناد أو إزالة Page لدور |
| **Statement** | The system MUST auto-add VIEW permission whenever a Page is assigned to a Role, and MUST NOT allow VIEW to be removed independently of the full CRUD set for that page |
| **Message-AR** | صلاحية العرض تُضاف تلقائياً ولا يمكن إزالتها بمفردها |
| **Message-EN** | VIEW permission is added automatically and cannot be removed independently |
| **Source** | POLICY-CLI-RBAC-01 — RoleAccessService.java lines 112-117, 169, 265-274 |

### RULE-SEC-043 — تقييد قيم صلاحيات CRUD في إسناد الصفحات

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-002, ENTITY-SEC-003 |
| **Trigger** | عند إسناد/مزامنة صلاحيات صفحة لدور |
| **Statement** | The system MUST restrict permission values in role-page assignment requests to CREATE, UPDATE, DELETE only |
| **Message-AR** | نوع الصلاحية غير صالح |
| **Message-EN** | Invalid permission type |
| **Source** | POLICY-CLI-RBAC-02 — RoleAccessService.java lines 104-108, 172-176 (INVALID_PERMISSION_TYPE) |

### RULE-SEC-044 — الاستبدال الكامل عند مزامنة صفحات الدور

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-002 |
| **Trigger** | عند syncRolePages |
| **Statement** | The system MUST fully replace the role's page-scoped permissions on sync while leaving system-level permissions (no page FK) untouched |
| **Message-AR** | — (سلوك داخلي) |
| **Message-EN** | — (internal behavior) |
| **Source** | POLICY-CLI-RBAC-03 — RoleAccessService.java lines 150-213 |

### RULE-SEC-045 — قواعد نسخ صلاحيات الدور

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-002 |
| **Trigger** | عند copyPermissionsFromRole |
| **Statement** | The system MUST copy only page-scoped permissions from the source role, MUST NOT overwrite the target role's system-level permissions, MUST reject copying from a role with zero page-scoped permissions, and MUST reject self-copy |
| **Message-AR** | لا توجد صلاحيات لنسخها من هذا الدور / لا يمكن النسخ من نفس الدور |
| **Message-EN** | No permissions to copy from this role / Cannot copy from the same role |
| **Source** | POLICY-CLI-RBAC-04 — RoleAccessService.java lines 295-348 (NO_PERMISSIONS_TO_COPY / INVALID_OPERATION) |

### RULE-SEC-046 — تنسيق وتفرُّد رمز/مسار الشاشة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-004 |
| **Trigger** | عند إنشاء أو تعديل Page |
| **Statement** | The system MUST require `pageCode` to match `^[A-Z0-9_]+$` (2-50 chars) and `route` to start with `/` and match `^/[a-zA-Z0-9/_-]+$`; both MUST be unique; `parentId`, if given, MUST reference an existing page and MUST NOT self-reference |
| **Message-AR** | رمز أو مسار الشاشة غير صالح، أو مستخدَم بالفعل، أو الشاشة الأب غير صحيحة |
| **Message-EN** | Invalid or duplicate page code/route, or invalid parent page |
| **Source** | POLICY-CLI-PAGE-01 — PageService.java lines 106-113, 463-469, 116-127, 186-193 |

### RULE-SEC-047 — توليد 4 صلاحيات تلقائياً عند إنشاء شاشة

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-003, ENTITY-SEC-004 |
| **Trigger** | عند إنشاء Page جديدة |
| **Statement** | The system MUST auto-generate exactly 4 Permission records (VIEW/CREATE/UPDATE/DELETE) named `PERM_<PAGE_CODE>_<TYPE>` for every new Page |
| **Message-AR** | — (سلوك داخلي، لا رسالة مستخدم) |
| **Message-EN** | — (internal behavior, no user-facing message) |
| **Source** | POLICY-CLI-PAGE-02 — PageService.java lines 383-418. **استثناء موثَّق فعلياً في الإنتاج:** SCR-SEC-006 (شاشة الملف التعريفي) تتطلب 3 صلاحيات فقط (بلا DELETE)، ونُفِّذ عبر SQL مباشر لعدم وجود خيار لتعطيل توليد DELETE في PageService — GAP-SEC-03، لا يُصحَّح هنا (توثيق AS-IS). |

### RULE-SEC-048 — تفرُّد وثبات رمز الدور، وحماية الدور المُستخدَم من الحذف

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-002 |
| **Trigger** | عند الإنشاء/التعديل/الحذف |
| **Statement** | The system MUST require unique `roleCode` and `roleName`, MUST treat `roleCode` as immutable after creation, and MUST prevent deletion of a role that has existing user assignments |
| **Message-AR** | رمز أو اسم الدور مستخدَم بالفعل / لا يمكن حذف دور له مستخدمون مُسنَدون |
| **Message-EN** | Role code or name already exists / Cannot delete a role with assigned users |
| **Source** | POLICY-CLI-ROLE-01 — RoleService.java lines 73-83, 156-174, 194 (ROLE_IN_USE, 409) |

### RULE-SEC-049 — تفرُّد اسم المستخدم، وحماية المستخدم صاحب الجلسات النشطة من الحذف، والإسناد التلقائي لدور المستخدم الافتراضي

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-001, ENTITY-SEC-005 |
| **Trigger** | عند الإنشاء/التعديل/الحذف عبر واجهة الإدارة |
| **Statement** | The system MUST require unique username (case-insensitive) on create and update, MUST prevent deletion of a user with active refresh tokens, and MUST auto-assign the default ROLE_USER role on creation if it exists (silently skipped otherwise) |
| **Message-AR** | اسم المستخدم مستخدَم بالفعل / لا يمكن حذف مستخدم لديه جلسات نشطة |
| **Message-EN** | Username already exists / Cannot delete a user with active sessions |
| **Source** | POLICY-CLI-USER-01 — UserService.java lines 62, 235-241, 200-207, 72-74 (USER_HAS_ACTIVE_REFRESH_TOKENS, 409) |

### RULE-SEC-050 — تحديد المحاولات على نقاط المصادقة الحسّاسة (Login/Signup/Forgot-Password/Reset-Password)

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-001, ENTITY-SEC-011 |
| **Trigger** | عند كل محاولة POST على `/api/auth/login`، `/api/auth/signup`، `/api/auth/forgot-password`، أو `/api/auth/reset-password` |
| **Statement** | The system MUST block further attempts for the same `ip\|identifier` key (identifier = username for login/signup, email for forgot-password, reset token for reset-password) after a configured maximum within a configured lockout window |
| **Message-AR** | تجاوزت الحد المسموح من المحاولات — حاول لاحقاً |
| **Message-EN** | Too many attempts — please try again later |
| **Source** | POLICY-CLI-AUTH-01 — `LoginRateLimitFilter.java` (`PROTECTED_PATH_IDENTIFIER_FIELD`, يُطبَّق على المسارات الأربعة أعلاه) + `LoginRateLimiterService.java` (Bucket4j، `ConcurrentHashMap` بالذاكرة). ✓ **مُصحَّحة 2026-08-23 (تحقُّق مباشر من backend.zip)**: النطاق أوسع مما وُثِّق سابقاً — كان يُذكَر "تسجيل الدخول" فقط، والواقع أن التغطية تشمل الأربعة نقاط مذكورة أعلاه بالفعل. **قيد معروف موثَّق في الكود ولا يزال قائماً:** غير آمن للتوسّع الأفقي (in-memory `ConcurrentHashMap`، ليس Redis رغم توفّر `spring-boot-starter-data-redis`) — يتطلب الانتقال لـ Redis عند أي توسّع أفقي (GAP-SEC-04)، غير مُصحَّح هنا. |
| **⚠ فجوة متبقية (مؤكَّدة 2026-08-23)** | جدول تدوير الرموز (`PASSWORD_RESET_TOKEN`/`ACCOUNT_ACTIVATION_TOKEN`) **بلا أي مهمة تنظيف دورية** — بعكس `REFRESH_TOKENS` (انظر RULE-SEC-052). لا `@Scheduled` أو Job مماثل مؤكَّد بالبحث المباشر في الكود. مرفوعة كـ **OQ-014**. |


### RULE-SEC-051 — تدوير رمز التحديث

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-005 |
| **Trigger** | عند كل login أو refresh |
| **Statement** | The system MUST revoke the prior refresh token and issue a new JTI on every login/refresh call |
| **Message-AR** | — (سلوك داخلي) |
| **Message-EN** | — (internal behavior) |
| **Source** | POLICY-CLI-AUTH-02 — AuthService.refresh() lines 134-136 |

### RULE-SEC-052 — التنظيف الدوري لرموز التحديث

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-005 |
| **Trigger** | مجدوَل (Scheduler) |
| **Statement** | The system MUST delete all expired refresh tokens regardless of revoked status, and MUST delete revoked refresh tokens older than a configured retention window |
| **Message-AR** | — (مهمة نظامية مجدولة، لا تفاعل مستخدم) |
| **Message-EN** | — (scheduled system job, no user interaction) |
| **Source** | POLICY-CLI-AUTH-03 — scheduler/RefreshTokenCleanupJob.java |
| **⚠ ملاحظة نطاق (مؤكَّدة 2026-08-23)** | هذه المهمة **خاصة بـ `REFRESH_TOKENS` فقط**. لا يوجد أي مهمة مماثلة لـ `PASSWORD_RESET_TOKEN`/`ACCOUNT_ACTIVATION_TOKEN` (ENTITY-SEC-011/012) — بحث مباشر في الكود (`grep -r "@Scheduled"`) لم يُظهر أي Job آخر. مرفوعة كـ **OQ-014**. |

### RULE-SEC-053 — مصادقة النداءات العابرة للموديولات من التدفقات المجهولة عبر حساب خدمة مخصَّص [جديد 2026-08-23]

| البند | القيمة |
|---|---|
| **Scope** | ENTITY-SEC-001, ENTITY-SEC-005 (يُصدِر access token عبر نفس مسار الكود المُستخدَم لتسجيل الدخول الحقيقي) |
| **Trigger** | عند استدعاء `NotificationClient` من `AuthEventListener` (أي: عند signup أو forgot-password — تدفقات مجهولة الهوية بلا JWT للمستخدم أصلاً) |
| **Statement** | The system MUST authenticate cross-module calls triggered by anonymous flows (which have no caller JWT to forward) via a dedicated, roleless service account (`svc-notification`), minting a real JWT for that account through the same `JwtService.generateAccess()` path used for real logins — NOT by extending the header-forwarding pattern used by `OrgBranchClient`/`MasterDataLookupClient` (RULE-SEC-004 context) |
| **Message-AR** | — (آلية داخلية بحتة، لا رسالة مستخدم) |
| **Message-EN** | — (purely internal mechanism, no user-facing message) |
| **Source** | POLICY-CLI-AUTH-04 — `client/NotificationClient.java` (يحمل توسيم **XM-SEC-005** في الـ javadoc الخاص به)، `db/scripts/005_notification_service_account_seed.sql`. **مؤكَّد بالقراءة المباشرة للكود (2026-08-23)**: الحساب مُسنَد بلا أي صف في `USER_ROLES` (صلاحيات صفرية عمداً)، كلمة المرور BCrypt hash لقيمة عشوائية لا تُسجَّل (لا يُتوقَّع تسجيل دخول فعلي بها). **معالجة الفشل**: إن لم يكن الحساب مزروعاً بعد أو فشل نداء HTTP، `NotificationClient` يسجّل تحذيراً فقط ولا يُفشِل تدفق signup/reset الذي أطلقه أصلاً (تصميم best-effort متعمَّد). |
| **علاقة بـ OQ-004** | هذا **لا يُغيِّر** إغلاق OQ-004 (نمط تمرير Authorization header في `OrgBranchClient`/`MasterDataLookupClient` يبقى كما قرَّره Architect — بلا اعتماد خدمة مخصَّص). هذه آلية **مختلفة تماماً وجديدة**، خاصة بـ Notification وحدها، لأن التدفقات المُطلِقة لها (signup/forgot-password) ليس لديها JWT مستخدم أصلاً لتمريره — الحلّان يتعايشان في نفس الموديول لسببين مختلفين. |



## A5 — قوائم القيم (LOV / Lookup)

> **قاعدة إلزامية:** هذا القسم هو المصدر الوحيد لتعريف LOVs. PART B يُشير إليها بالـ ID فقط.

---

### LOV-SEC-001 — نوع الصلاحية (PermissionType)

| البند | القيمة |
|---|---|
| **الحقل** | `permissionType` |
| **ENTITY-ID** | ENTITY-SEC-003 |
| **نوع التحكم** | Java enum مُخزَّن مباشرة — **⚠ ليس Dropdown/LOV قياسياً** |
| **lookupKey** | لا يوجد — **انحراف موثَّق عن LOV-1/LOV-4** (يُفترَض أن كل القوائم تُدار عبر `MD_MASTER_LOOKUP`/`MD_LOOKUP_DETAIL`؛ هنا مُخزَّن كـ `@Enumerated(EnumType.STRING)` مباشرة على عمود `PERMISSION_TYPE`) |
| **المصدر** | `dto/PermissionType.java` |
| **المالك** | هذا الموديول (كود، لا جدول Lookup) |
| **API الاستهلاك** | لا يوجد — القيم ثابتة في الكود نفسه |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| VIEW | عرض | View |
| CREATE | إنشاء | Create |
| UPDATE | تعديل | Update |
| DELETE | حذف | Delete |

> **هذا الانحراف مُوثَّق AS-IS ولا يُعاد تصميمه في هذه الوثيقة** (EXCEPTION module — لا توصية تصميمية).

---

### LOV-SEC-002 — مستوى الوصول للبيانات (DATA_ACCESS_LEVEL)

| البند | القيمة |
|---|---|
| **الحقل** | `dataAccessLevel` |
| **ENTITY-ID** | ENTITY-SEC-010 |
| **نوع التحكم** | Dropdown (≤15 قيمة) |
| **lookupKey** | `DATA_ACCESS_LEVEL` (مؤكَّد في تعليق الكود). **ملاحظة مطابقة:** master-registry.md Section 6 يسجّل هذا الـ Lookup تحت الاسم المفاهيمي "**ScopeLevel**" (مملوك لـ Security، ≤15 قيمة) — نفس القائمة، تسمية وصفية مختلفة عن الـ lookupKey التقني. |
| **المصدر** | MD_LOOKUP_DETAIL — مُزرَع (seeded) بواسطة سكربت هجرة SECURITY نفسه، لكن الجدول مملوك لـ MasterData (1.4) |
| **المالك** | MasterData (الجدول) / SECURITY (البيانات المزروعة) |
| **API الاستهلاك** | `GET /api/lookups/DATA_ACCESS_LEVEL?active=true` (عبر MasterDataLookupClient) |

| code | الاسم بالعربي | الاسم بالإنجليزي |
|---|---|---|
| BRANCH_ONLY | الفرع فقط | Branch Only |
| BRANCH_AND_CHILDREN | الفرع والفروع التابعة | Branch And Children |
| ALL | جميع الفروع | All |

⚠ القيمة المُخزَّنة في `SEC_ROLE_BRANCH.DATA_ACCESS_LEVEL`: `code` (نص)، وليست `id` — لكنها **غير مقيَّدة بـ DB check constraint**، فقط بتحقُّق على مستوى الخدمة (RULE-SEC-035). هذا انحراف طفيف عن اصطلاح "قيد قاعدة بيانات" لكنه موثَّق ومقصود في الكود.

⚠ **تعارض جديد مكتشَف مقابل master-registry.md (OQ-006)**: Section 8 "DATA SCOPE RULES" في master-registry.md ينص أن مستويات DataScope المقصودة هي `Platform / LegalEntity / Branch / Department` — أربعة مستويات بأسماء مختلفة تماماً عن القيم الثلاث الفعلية هنا (`BRANCH_ONLY / BRANCH_AND_CHILDREN / ALL`). هذا الجدول يوثّق AS-IS ما هو مُنفَّذ فعلياً فقط؛ التعارض مع النية المُعلَنة في السجل الرئيسي **لم يُحل هنا** — مرفوع كـ **OQ-006**.

---

### PREFERRED_LANG — بلا LOV محكوم (غير مُسنَد رقم LOV)

> الحقل `preferredLang` على ENTITY-SEC-009 نص حر (`VARCHAR(10)`) بلا أي `MD_LOOKUP_DETAIL` مرتبط. تعليق الكود نفسه يُقر بذلك ("OQ-004 — no LOV domain governed yet"). **لا يُسنَد LOV-ID هنا — قرار نهائي (OQ-001 CLOSED، 2026-07-22، Architect): يبقى نصاً حراً بشكل دائم.**

---

## A6 — دورة الحالة (Status Lifecycle)

> **RULE-13 مؤكَّدة:** لا يوجد Workflow Engine في هذا الموديول — لا خطوات موافقة، لا محرك عام. جميع الانتقالات أدناه هي Status Lifecycle بسيطة فقط (الحالة 1 من Section A6 القياسية).

### STATUS LIFECYCLE — حساب المستخدم (ENTITY-SEC-001، حقل `enabled`)

```
[DISABLED] ──(تفعيل عبر رمز صالح — RULE-SEC-032/033)──► [ENABLED] ✓
     ▲                                                        │
     └──────────(تعطيل يدوي من الأدمن عبر UPDATE)─────────────┘
```

> يُرسَم لأن التسجيل الذاتي → التفعيل مسار عمل حقيقي رغم أنه حالتان فقط (استثناء توثيقي مبرَّر — SCR-5 لا يُلزم بحذفه، فقط لا يُلزم برسمه؛ رُسِم هنا لقيمته التوضيحية).

### حالات تبديل بسيطة أخرى (بلا مخطط — ≤2 حالة لكل منها، طبقاً لـ SCR-5)

| الكيان | الحقل | الانتقال |
|---|---|---|
| ENTITY-SEC-002 (Role) | `active`/`IS_ACTIVE` | `true ↔ false` عبر toggleRoleActive() |
| ENTITY-SEC-004 (Page) | `active` | `true ↔ false` عبر deactivatePage()/reactivatePage() |
| ENTITY-SEC-009 (SecUserProfile) | `isActiveFl` | `true ↔ false` عبر UPDATE فقط (لا DELETE) |
| ENTITY-SEC-010 (SecRoleBranch) | `isActiveFl` | `true ↔ false` |
| ENTITY-SEC-005 (RefreshToken) | `revoked` | أحادي الاتجاه `false → true` (logout أو تدوير) |
| ENTITY-SEC-011/012 (Tokens) | `usedFl` | أحادي الاتجاه `false → true` |

---

## A7 — تبعيات الموديولات (Module Dependencies)

> هذا القسم يحدد XM Candidates فقط — التصنيف الرسمي (XM-IDs) يتم في MODE 1.5 (Project 2).

### الكيانات المُستهلَكة من موديولات أخرى

| الكيان | ENTITY-ID (canonical) | الموديول المالك | نوع الاعتمادية | XM Candidate |
|---|---|---|---|---|
| Branch (عبر SecUserProfile.branchIdFk) | **ENTITY-ORG-002** (table `ORG_BRANCH`، GOVERNED ✓ MODE 2 — مؤكَّد في master-registry.md Sec.5 وmodule-registry-org.md) | Organization | HARD-FK (DB) + SOFT-READ (Service، عبر OrgBranchClient) — ⚠ انظر OQ-007: module-registry-org.md ينص أن Cross-module FK يجب أن يُتحقَّق على مستوى الخدمة فقط بلا DB FK، بينما التنفيذ الفعلي يضيف قيد DB حقيقياً | نعم → **XM-SEC-001** في MODE 1.5 |
| Branch (عبر SecRoleBranch.branchIdFk) | **ENTITY-ORG-002** (table `ORG_BRANCH`) | Organization | HARD-FK (DB) + SOFT-READ (Service) — نفس ملاحظة OQ-007 | نعم → **XM-SEC-002** في MODE 1.5 |
| MD_MASTER_LOOKUP / MD_LOOKUP_DETAIL (DATA_ACCESS_LEVEL — يُسمَّى "ScopeLevel" في master-registry.md Section 6) | خارجي — MasterData (1.4) | MasterData | SOFT-READ فقط (لا FK) | نعم → مرشَّح XM جديد يُسمَّى في MODE 1.5 |
| **Notification** (عبر `NotificationClient` → `POST /api/v1/notifications/send`) **[جديد 2026-08-23]** | خارجي — Notification | Notification | SOFT (event-driven، نداء HTTP داخلي نفس الـ JVM) — ✅ **مؤكَّد بالقراءة المباشرة للكود**: `AuthEventListener` (`@TransactionalEventListener AFTER_COMMIT`) + `NotificationClient` (يمنح JWT حساب خدمة `svc-notification` — RULE-SEC-053) | نعم → **XM-SEC-005** (مُوسَّم بالفعل في javadoc الكود نفسه، `NotificationClient.java`) |

> ✓ **مؤكَّد من module-registry-org.md** ("OUTGOING — WHO CONSUMES ORGANIZATION"): `Security (1.2) │ HARD-FK │ ORG_BRANCH — DataScope / SEC_ROLE_BRANCH` — يطابق تماماً تصنيف هذا القسم.

> ⚠ **تحذير حوكمي (2026-08-23)**: مستند `security-registry.md` (موجود ضمن `governance/modules/SECURITY/` في backend.zip) يذكر أن Conflict #20 في master-registry.md **لا يزال OPEN** (مرتبط بـ `BLK-SEC-002` — دورة اعتمادية Security↔Notification)، وأن نطاق EXTENSION (بما فيه هذه التبعية الجديدة) لا يزال **"PARTIALLY_READY ⚠️, BLOCKED pending BLK-SEC-002"** حسب نفس المستند. هذا **يتعارض** مع ما وثَّقته هذه الوثيقة سابقاً (2026-07-22) بعد قراءة مباشرة لـ master-registry.md، حيث ظهر Conflict #20 كـ **CLOSED**. **لا يمكن حسم هذا التعارض بدون إعادة رفع master-registry.md بنسخته الحالية** — مرفوع كـ **OQ-016**، غير محلول هنا.

### ملاحظة معمارية (AS-IS، غير مُصمَّمة هنا)

> `SECURITY` لا تملك أي تبعية Maven/compile-time على `erp-org` أو `erp-masterdata` أو `erp-notification` (مؤكَّد أيضاً بتعليق كود صريح في `NotificationClient.java`) — كل التفاعل عبر نداءات HTTP داخلية بنفس الـ JVM (`RestTemplate` إلى `localhost`)، بعكس نمط الحقن المباشر (`@Service` injection) المُستخدَم بين ORG/FILE/NOTIFICATION فيما بينها. `OrgBranchClient` و`MasterDataLookupClient` يمرّران `Authorization` header الوارد من المستخدم الحالي حرفياً (قرار Architect نهائي — OQ-004 CLOSED). `NotificationClient` يستخدم آلية **مختلفة تماماً** (RULE-SEC-053) لأن التدفقات المُطلِقة له (signup/forgot-password) مجهولة الهوية أصلاً وليس لديها JWT مستخدم لتمريره. هذا الوصف توثيقي بحت، وليس توصية بتغيير النمط.

### تبعيات غير مؤكَّدة (Ambiguous)

| التبعية | الحالة |
|---|---|
| HR (عبر SecUserProfile.employeeIdFk) | **لا يوجد** — الحقل غير مقيَّد بالكامل بقرار نهائي (**OQ-002 CLOSED**، 2026-07-22، Architect: لا يوجد موديول HR حالياً، يبقى العمود بلا FK حتى تبدأ حوكمة HR فعلياً) |
| FileService | **لا يوجد** — لم يُكتشَف أي استدعاء في الكود، ومؤكَّد أيضاً في module-registry-SECURITY.md ("FILESVC: none detected") |

> ~~Notification (عبر أحداث التفعيل/الاستعادة)~~ — **نُقِلت من هنا** إلى جدول "الكيانات المُستهلَكة" أعلاه بعد التأكيد المباشر 2026-08-23 (لم تعد Ambiguous).

### الخدمات والتكاملات الخارجية

| الخدمة | الغرض | نوع التكامل |
|---|---|---|
| OrgBranchClient → Organization | التحقق من وجود/نشاط الفرع (RULE-SEC-034) | REST API (نداء داخلي نفس الـ JVM، تمرير Authorization header) |
| MasterDataLookupClient → MasterData | التحقق من صحة `dataAccessLevel` (RULE-SEC-035) | REST API (نداء داخلي نفس الـ JVM، تمرير Authorization header) |
| NotificationClient → Notification **[جديد 2026-08-23]** | إرسال إشعارات تفعيل الحساب/استعادة كلمة المرور فعلياً (RULE-SEC-031، RULE-SEC-053) | REST API (نداء داخلي نفس الـ JVM، JWT حساب خدمة `svc-notification`) |

---

# ══════════════════════════════════════════════════════════
# PART B — SCREEN SPECIFICATIONS
# One block per SCR-ID — self-contained for P3 execution
# References PART A by ID — never redefines artifacts
# ══════════════════════════════════════════════════════════

> **ملاحظة حوكمية على ترقيم SCR-ID في هذا القسم:** المصدر الوحيد الذي يحمل SCR-ID فعلياً موجوداً مسبقاً في الأنظمة الفعلية هو **SCR-SEC-006** (شاشة الملف التعريفي للمستخدم — مذكورة صراحةً في business-policies-SECURITY.md كـ "SCR-SEC-006 (User Profile page)"). هذه الوثيقة تُبقي على هذا الرقم كما هو (HR-1 — لا اختراع). باقي أرقام SCR أدناه (001-005، 007) **جديدة تُسنِدها هذه الجلسة الأولى من الحوكمة** لعدم توفّر أي مرجع AS-BUILT آخر لترقيمها — موثَّق صراحة، وليس افتراضاً صامتاً.

---

## SCR-SEC-001 — المصادقة والخدمة الذاتية (Authentication & Self-Service)

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-SEC-001 |
| **اسم الشاشة** | تسجيل الدخول / التسجيل الذاتي / استعادة كلمة المرور |
| **UI Pattern** | PATTERN-3 — Specialized |
| **Pattern Reason** | مجموعة نماذج عامة (Public) بلا صلاحيات RBAC وبلا SEC_PAGES row — لا تُشغِّل نموذج Search+Entry ولا Inline/Modal القياسي؛ 5 نماذج فرعية مترابطة وظيفياً (Login / Signup / Activate / Forgot / Reset) |
| **SCR-ID Scope** | ONE SCR-ID covers: جميع نماذج المصادقة العامة الخمسة |
| **P3 Implication** | تدفق شاشات عام (Public) بلا حارس تنقّل (Navigation Guard) — P3 يحدد أسماء الـ Components |
| **ENTITY-ID** | ENTITY-SEC-001, ENTITY-SEC-011, ENTITY-SEC-012, ENTITY-SEC-005 |
| **وظيفة الشاشة** | تمكين أي زائر من الدخول أو التسجيل أو استعادة كلمة المرور دون صلاحيات مسبقة |
| **المستخدمون** | عام (غير مسجَّل) |
| **الموضع في النظام** | خارج القائمة الرئيسية — نقطة دخول النظام |
| **روابط من** | — (نقطة الدخول) |
| **روابط إلى** | لوحة التحكم الرئيسية (بعد نجاح الدخول) |

---

### B3 — مواصفة الإدخال (Input Specification)

#### نماذج الشاشة

| النموذج | الحقول | المصدر |
|---|---|---|
| تسجيل الدخول | username, password | ENTITY-SEC-001 → A3 |
| التسجيل الذاتي | username, email, password | ENTITY-SEC-001 → A3 |
| تفعيل الحساب | token | ENTITY-SEC-012 → A3 |
| نسيت كلمة المرور | email | ENTITY-SEC-001 → A3 |
| إعادة تعيين كلمة المرور | token, newPassword | ENTITY-SEC-011 → A3 |

#### قواعد الإدخال المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| RULE-SEC-030 | عند إتمام التسجيل الذاتي | ← see A4 |
| RULE-SEC-031 | عند إرسال التفعيل/الاستعادة | ← see A4 |
| RULE-SEC-032 | عند activate/reset | ← see A4 |
| RULE-SEC-033 | عند نجاح activate/reset | ← see A4 |
| RULE-SEC-038 | عند forgot-password | ← see A4 |
| RULE-SEC-039 | عند إصدار رمز استعادة جديد | ← see A4 |
| RULE-SEC-040 | عند signup | ← see A4 |
| RULE-SEC-041 | عند signup | ← see A4 |
| RULE-SEC-050 | عند كل محاولة دخول | ← see A4 |
| RULE-SEC-051 | عند login/refresh | ← see A4 |
| RULE-SEC-037 | عند إصدار JWT | ← see A4 |

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء | تعديل | حذف | تصدير |
|---|---|---|---|---|---|
| SCR-SEC-001 | عام (بلا صلاحية) | — | — | — | — |

> **لا SEC_PAGES row لهذه الشاشة** — نماذج عامة بلا RBAC، مؤكَّد من عدم وجود `pageCode` مرتبط بمسارات `/api/auth/**` في المصادر المرفقة.

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-SEC-001 | تسجيل دخول | POST | /api/auth/login | username, password | accessToken, refreshToken, expiresIn | RULE-SEC-050, RULE-SEC-037 |
| API-SEC-002 | تسجيل دخول مع بيانات كاملة | POST | /api/auth/login-token | username, password | accessToken, refreshToken, userId, roles[], permissions[] | RULE-SEC-050, RULE-SEC-037 |
| API-SEC-003 | تحديث الرمز | POST | /api/auth/refresh | refresh cookie | accessToken جديد | RULE-SEC-051 |
| API-SEC-004 | تسجيل الخروج | POST | /api/auth/logout | — | 204 | — |
| API-SEC-005 | التسجيل الذاتي | POST | /api/auth/signup | username, email, password | userId, username, enabled=false | RULE-SEC-030, RULE-SEC-040, RULE-SEC-041 |
| API-SEC-006 | تفعيل الحساب | POST | /api/auth/signup/activate | token | 200 OK | RULE-SEC-032, RULE-SEC-033 |
| API-SEC-007 | طلب استعادة كلمة المرور | POST | /api/auth/forgot-password | email | 200 OK (دائماً) | RULE-SEC-038, RULE-SEC-039, RULE-SEC-031 |
| API-SEC-008 | إعادة تعيين كلمة المرور | POST | /api/auth/reset-password | token, newPassword | 200 OK | RULE-SEC-032, RULE-SEC-033 |

---
---

## SCR-SEC-002 — إدارة المستخدمين (User Management)

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-SEC-002 |
| **اسم الشاشة** | إدارة المستخدمين |
| **UI Pattern** | PATTERN-1 — Search + Entry |
| **Pattern Reason** | كيان به عمليات بحث/فلترة وإسناد أدوار (subentity: roles) — 5.8.2 |
| **SCR-ID Scope** | ONE SCR-ID covers: Search + Entry (CORE-9) |
| **P3 Implication** | تنقّل ثنائي الشاشة (Search → Entry) — P3 يحدد أسماء الـ Components |
| **ENTITY-ID** | ENTITY-SEC-001 |
| **وظيفة الشاشة** | تمكين الأدمن من إنشاء/بحث/تعديل/حذف حسابات المستخدمين وإسناد الأدوار لهم |
| **المستخدمون** | Admin (PERM_USER_*) |
| **الموضع في النظام** | الأمان ← المستخدمون |
| **روابط من** | القائمة الرئيسية |
| **روابط إلى** | SCR-SEC-006 (الملف التعريفي)، SCR-SEC-007 (نطاق البيانات) |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| username | نص | لا | — | |
| enabled | قائمة منسدلة | لا | true/false | |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_USER_CREATE |
| Edit | عند تحديد سجل | PERM_USER_UPDATE |
| Delete | عند تحديد سجل | PERM_USER_DELETE |
| إسناد أدوار | عند تحديد سجل | PERM_USER_UPDATE (`PERM_USER_MANAGE_ROLES` مطابقة لنفس القيمة فعلياً — لا صلاحية مستقلة) |

#### قواعد البحث المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| RULE-SEC-049 | — | ← see A4 |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| username | نص | نعم | ENTITY-SEC-001 → A3 | |
| email | نص | لا | ENTITY-SEC-001 → A3 | |
| password | نص (سري) | نعم عند الإنشاء | ENTITY-SEC-001 → A3 | لا يُعرَض عند التعديل |
| enabled | مفتاح تبديل | لا | ENTITY-SEC-001 → A3 | |
| roles | قائمة اختيار متعدد | لا | ENTITY-SEC-002 (عبر ENTITY-SEC-006) | استبدال كامل عند الحفظ |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ | POST / PUT | RULE-SEC-049 |
| إلغاء | navigation back | — |
| حذف | DELETE | RULE-SEC-049 |

#### قواعد الإدخال المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| RULE-SEC-049 | عند الحفظ/الحذف | ← see A4 |

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء | تعديل | حذف | تصدير |
|---|---|---|---|---|---|
| SCR-SEC-002 | PERM_USER_VIEW | PERM_USER_CREATE | PERM_USER_UPDATE | PERM_USER_DELETE | PERM_USER_VIEW |

**Security Seed Data:**
```
SEC_PAGES  : INSERT — page_code = USER, parent_id_fk = [الأمان]
PERMISSIONS: INSERT × 4 — PERM_USER_VIEW / CREATE / UPDATE / DELETE
```

---

### B5 — الواجهات البرمجية (Functional APIs)

```
UNIVERSAL RULE DECLARATION مطبَّقة: Update API أدناه يذكر قاعدة تفرُّد
اسم المستخدم صراحة في عمود RULE-IDs.
```

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-SEC-009 | إنشاء مستخدم | POST | /api/users | username, password | UserDto (مع إسناد ROLE_USER تلقائياً إن وُجد) | RULE-SEC-049 |
| API-SEC-010 | بحث | POST | /api/users/search | username?, enabled?, page, size | قائمة UserDto | — |
| API-SEC-011 | جلب الكل | GET | /api/users | page, size | قائمة UserDto | — |
| API-SEC-012 | تعديل (جزئي) | PUT | /api/users/{userId} | الحقول المعدَّلة | UserDto محدَّث | RULE-SEC-049 |
| API-SEC-013 | حذف | DELETE | /api/users/{userId} | userId | 204 | RULE-SEC-049 |
| API-SEC-014 | إسناد أدوار (استبدال كامل) | PUT | /api/users/{userId}/roles | roleIds[] | UserDto مع الأدوار | — |
| API-SEC-015 | جلب أدوار المستخدم | GET | /api/users/{userId}/roles | userId | قائمة الأدوار | — |

---
---

## SCR-SEC-003 — إدارة الأدوار والصلاحيات (Role & RBAC Management)

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-SEC-003 |
| **اسم الشاشة** | إدارة الأدوار وربط الشاشات بالصلاحيات |
| **UI Pattern** | PATTERN-1 — Search + Entry |
| **Pattern Reason** | كيان متوسط/كبير — يتضمّن مصفوفة صلاحيات (subentity: page-permissions) — 5.8.2 |
| **SCR-ID Scope** | ONE SCR-ID covers: Search + Entry (تشمل تبويب/لوحة مصفوفة الصلاحيات ضمن نفس الـ Entry — CORE-9، لا SCR-ID منفصل للمصفوفة) |
| **P3 Implication** | تنقّل ثنائي الشاشة + لوحة فرعية لمصفوفة الصلاحيات داخل شاشة الإدخال ذاتها — P3 يحدد التفاصيل |
| **ENTITY-ID** | ENTITY-SEC-002 |
| **وظيفة الشاشة** | إنشاء/بحث/تعديل/حذف الأدوار، وربط كل دور بالشاشات وصلاحياتها CRUD، ونسخ الصلاحيات بين الأدوار |
| **المستخدمون** | Admin (PERM_ROLE_*) |
| **الموضع في النظام** | الأمان ← الأدوار |
| **روابط من** | القائمة الرئيسية |
| **روابط إلى** | SCR-SEC-007 (نطاق البيانات بالفرع) |

---

### B2 — مواصفة البحث (Search Specification)

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| roleName | نص | لا | — | حقل الفلترة الوحيد المسموح فعلياً (allowed filter field) |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_ROLE_CREATE |
| Edit | عند تحديد سجل | PERM_ROLE_UPDATE |
| Delete | عند تحديد سجل | PERM_ROLE_DELETE |
| تفعيل / تعطيل الدور | عند تحديد سجل | PERM_ROLE_UPDATE — ⚠ **مُصحَّحة 2026-08-23**: مساران منفصلان (`activate`/`deactivate`)، وليس "تبديل" واحد كما وُثِّق سابقاً — انظر B5 |
| نسخ الصلاحيات من دور آخر | عند تحديد سجل | PERM_ROLE_UPDATE |

#### قواعد البحث المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| RULE-SEC-048 | — | ← see A4 |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| roleCode | نص (Read-Only بعد الإنشاء) | نعم | ENTITY-SEC-002 → A3 | ثابت بعد الحفظ الأول |
| roleName | نص | نعم | ENTITY-SEC-002 → A3 | |
| description | نص | لا | ENTITY-SEC-002 → A3 | |
| active/IS_ACTIVE | مفتاح تبديل | نعم | ENTITY-SEC-002 → A3 | |
| مصفوفة الصفحات/الصلاحيات | لوحة فرعية (Page × VIEW/CREATE/UPDATE/DELETE) | لا | ENTITY-SEC-004 + ENTITY-SEC-003 | VIEW تلقائية عند إضافة صفحة (RULE-SEC-042) |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ | POST / PUT | RULE-SEC-048 |
| إلغاء | navigation back | — |
| حذف | DELETE | RULE-SEC-048 |
| إضافة صفحة للدور | POST (مصفوفة) | RULE-SEC-042, RULE-SEC-043 |
| مزامنة صفحات الدور | PUT (استبدال كامل) | RULE-SEC-044 |
| إزالة صفحة من الدور | DELETE (مصفوفة) | RULE-SEC-042 |
| نسخ من دور آخر | POST | RULE-SEC-045 |

#### قواعد الإدخال المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| RULE-SEC-048 | عند الحفظ/الحذف | ← see A4 |
| RULE-SEC-042 إلى RULE-SEC-045 | عند التعامل مع مصفوفة الصلاحيات | ← see A4 |

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء | تعديل | حذف | تصدير |
|---|---|---|---|---|---|
| SCR-SEC-003 | PERM_ROLE_VIEW | PERM_ROLE_CREATE | PERM_ROLE_UPDATE | PERM_ROLE_DELETE | PERM_ROLE_VIEW |

**Security Seed Data:**
```
SEC_PAGES  : INSERT — page_code = ROLE, parent_id_fk = [الأمان]
PERMISSIONS: INSERT × 4 — PERM_ROLE_VIEW / CREATE / UPDATE / DELETE
```

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-SEC-016 | إنشاء دور | POST | /api/roles | roleCode, roleName, description | RoleDto | RULE-SEC-048 |
| API-SEC-017 | بحث | POST | /api/roles/search | roleName?, page, size, sort(id,roleName) | قائمة RoleDto | — |
| API-SEC-018 | جلب بالمعرّف | GET | /api/roles/{roleId} | roleId | RoleDto | — |
| API-SEC-019 | تعديل | PUT | /api/roles/{roleId} | roleName, description (roleCode ثابت) | RoleDto محدَّث | RULE-SEC-048 |
| API-SEC-020 | حذف | DELETE | /api/roles/{roleId} | roleId | 204 | RULE-SEC-048 |
| API-SEC-021 | تفعيل الدور | PUT | /api/roles/{roleId}/activate | roleId | RoleDto | — |
| API-SEC-050 [جديد 2026-08-23] | تعطيل الدور | PUT | /api/roles/{roleId}/deactivate | roleId | RoleDto | — |

> ⚠ **تصحيح 2026-08-23 (تحقُّق مباشر من backend.zip، `RoleController.java:110-130`)**: لا يوجد endpoint واحد `/toggle-active` كما وُثِّق سابقاً — `RoleController` يُطبِّق مسارين منفصلين: `activateRole()` (`PUT /{roleId}/activate`) و`deactivateRole()` (`PUT /{roleId}/deactivate`)، مطابقاً لاصطلاح enforce-backend-contract المُعتمَد في هذا الكود (نفس نمط ORG's `LegalEntity`/`Branch` وغيرها). `API-SEC-021` صُحِّح مساره؛ `API-SEC-050` أُضيف جديداً لمسار التعطيل — لم تُعَد ترقيم أي API-ID أخرى (استقرار الهويات محفوظ).
| API-SEC-022 | جلب صفحات الدور | GET | /api/roles/{roleId}/pages | roleId | قائمة PageAssignmentResponse | — |
| API-SEC-023 | إضافة صفحة للدور | POST | /api/roles/{roleId}/pages | pageCode, permissions[] (CREATE/UPDATE/DELETE) | PageAssignmentResponse | RULE-SEC-042, RULE-SEC-043 |
| API-SEC-024 | مزامنة صفحات الدور (استبدال كامل) | PUT | /api/roles/{roleId}/pages | قائمة كاملة من الصفحات+الصلاحيات | قائمة PageAssignmentResponse | RULE-SEC-044 |
| API-SEC-025 | إزالة صفحة من الدور | DELETE | /api/roles/{roleId}/pages/{pageCode} | roleId, pageCode | 204 | RULE-SEC-042 |
| API-SEC-026 | نسخ الصلاحيات من دور آخر | POST | /api/roles/{roleId}/copy-from/{sourceRoleId} | roleId, sourceRoleId | CopyPermissionsResponse | RULE-SEC-045 |

---
---

## SCR-SEC-004 — سجل الصلاحيات (Permission Registry)

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-SEC-004 |
| **اسم الشاشة** | سجل الصلاحيات |
| **UI Pattern** | PATTERN-2 — Inline / Modal |
| **Pattern Reason** | كيان بسيط — حقول ≤ 8، بلا subentities — 5.8.2 |
| **SCR-ID Scope** | ONE SCR-ID covers: UNIFIED |
| **P3 Implication** | شاشة واحدة بتحرير Modal/Inline — P3 يحدد الآلية |
| **ENTITY-ID** | ENTITY-SEC-003 |
| **وظيفة الشاشة** | إدارة صلاحيات النظام الخام مباشرة (استخدام إداري نادر — معظم الصلاحيات تُدار تلقائياً عبر الشاشات) |
| **المستخدمون** | Admin (PERM_PERMISSION_*) |
| **الموضع في النظام** | الأمان ← الصلاحيات |
| **روابط من** | القائمة الرئيسية |
| **روابط إلى** | — |

**UI Structure Decision:**

| Data Size | Small (≤8 fields) | Interaction | Modal |
|---|---|---|---|
| Pattern | PATTERN-2 | Reason | صلاحية نظامية بسيطة، بلا كيانات فرعية |

---

### B2 — مواصفة البحث (Search Specification)

> **أُضيف هذا البلوك في 2026-07-22 (حل OQ-010)** — كان مفقوداً رغم وجود `API-SEC-028` (`POST /api/permissions/search`). الفلاتر والأعمدة أدناه مُشتقَّة مباشرة من حقول B3 وباراميترات API-SEC-028 الموثَّقة أصلاً في B5 — لا حقول جديدة أُضيفت.

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| name | نص | لا | ENTITY-SEC-003 → A3 | مطابقة جزئية |
| module | نص | لا | ENTITY-SEC-004 → A3 (عبر page) | فلتر غير مباشر — موثَّق في API-SEC-028 فقط |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_PERMISSION_CREATE |
| Edit | عند تحديد سجل | PERM_PERMISSION_UPDATE (بعد إصلاح OQ-005 — انظر B4) |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| name | نص | نعم | ENTITY-SEC-003 → A3 | النمط `PERM_<PAGE_CODE>_<TYPE>` |
| permissionType | قائمة منسدلة | لا | LOV-SEC-001 → A5 | انحراف — Java enum لا MD_LOOKUP |
| page | قائمة اختيار | لا | ENTITY-SEC-004 → A3 | فارغ = صلاحية نظامية |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ | POST / PUT | — |
| إلغاء | إغلاق Modal | — |

> ⚠ لا يوجد endpoint حذف مؤكَّد لهذا الكيان في المصادر المرفقة رغم وجود ثابت `PERM_PERMISSION_DELETE` في الكود — **NOT VERIFIABLE THIS SESSION** (لم يُعثَر على مسار DELETE فعلي).

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء | تعديل | حذف | تصدير |
|---|---|---|---|---|---|
| SCR-SEC-004 | PERM_PERMISSION_VIEW | PERM_PERMISSION_CREATE | **PERM_PERMISSION_UPDATE** ✓ مُصلَحة | — (لا endpoint موجود) | PERM_PERMISSION_VIEW |

> ✓ **OQ-005 RESOLVED, CORRECTED 2026-07-22 (re-verification)**: `@PreAuthorize("hasAuthority('PERMISSION_UPDATE')")` هو فعلاً موجود ونشط — لكن **تصحيح استشهاد**: هو على `PermissionService.updatePermission()` (`PermissionService.java:109-110`)، **وليس** على `PermissionController.update()` كما وثَّقت النسخة السابقة خطأً. هذا مطابق تماماً للاصطلاح المُستخدَم فعلياً في باقي الموديول (Page/User-profile services تتبع نفس النمط: تفويض على مستوى Service لا Controller) — **ليس خللاً معمارياً، فقط خطأ توثيقي كان يُشير للطبقة الخطأ**. الحماية فعّالة (Spring AOP يعترض الـ Service bean بغض النظر عن الطبقة). **اختبارات الانحدار لم تكن موجودة فعلياً في النسخة السابقة** (لا `src/test` كان موجوداً في `erp-security` إطلاقاً) — أُضيف الآن `PermissionServiceUpdateSecurityTest.java` (Spring method-security context test) يغطي: نجاح مع صلاحية صحيحة، `AccessDeniedException` مع صلاحية خاطئة، `AuthenticationCredentialsNotFoundException` بلا مصادقة — 3/3 ناجحة.

**Security Seed Data:**
```
SEC_PAGES  : INSERT — page_code = PERMISSION, parent_id_fk = [الأمان]
PERMISSIONS: INSERT × 4 — PERM_PERMISSION_VIEW / CREATE / UPDATE / DELETE
```

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-SEC-027 | إنشاء صلاحية | POST | /api/permissions | name, permissionType?, page? | PermissionDto | — |
| API-SEC-028 | بحث | POST | /api/permissions/search | name?, module?, page, size, sort | قائمة PermissionDto | — |
| API-SEC-029 | تعديل | PUT | /api/permissions/{id} | الحقول المعدَّلة | PermissionDto محدَّث | (اسم الصلاحية فريد — تحقُّق `PERMISSION_ALREADY_EXISTS`) — ✓ محمي الآن بـ `PERM_PERMISSION_UPDATE` (OQ-005 RESOLVED) |

---
---

## SCR-SEC-005 — سجل الشاشات (Page Registry)

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-SEC-005 |
| **اسم الشاشة** | سجل شاشات النظام |
| **UI Pattern** | **PATTERN-1 — Search + Entry** ⚠ **أُعيد 2026-07-22 (OQ-013 REOPENED-then-RECLOSED)** — كانت عُدِّلت مؤقتاً إلى PATTERN-3 (Tree) بناءً على توصية P2.5 لم تُتحقَّق حينها، ثم **ثبت بالتحقيق البرمجي أنها خاطئة بالكامل**: لا يوجد `Tree`/`TreeTable` أو ما يعادله في أي مكان بمستودع الـ frontend؛ ولا توجد وحدة frontend لـ Organization إطلاقاً (`frontend/src/app/modules/` تحتوي فقط `security` و`master-data`) — فالمرجع المُستشهَد به (SCR-ORG-004/005) **لا وجود له فعلياً** كتطبيق frontend. الشاشة المبنية فعلياً (`pages-search.component.ts:72-91`) هي **AG Grid مسطَّحة (PATTERN-1)** وتعمل بشكل صحيح. **القرار النهائي**: عودة كاملة لـ PATTERN-1 — توثيق هذه الوثيقة الآن يطابق الواقع المبني فعلياً. |
| **Pattern Reason** | `parentId` مرجع ذاتي حقيقي (self-reference) — لكن **لا يوجد تنفيذ شجري في الواجهة الأمامية حالياً**؛ يُعرَض كجدول مسطَّح قياسي (Search + Entry) عبر AG Grid — 5.8.2 |
| **SCR-ID Scope** | ONE SCR-ID covers: Search + Entry (CORE-9) |
| **P3 Implication** | تنقّل ثنائي الشاشة — P3 يحدد التفاصيل |
| **ENTITY-ID** | ENTITY-SEC-004 |
| **وظيفة الشاشة** | تسجيل شاشات/صفحات النظام (تُنشئ 4 صلاحيات تلقائياً) وتفعيلها/تعطيلها |
| **المستخدمون** | Admin (PERM_PAGE_*) |
| **الموضع في النظام** | الأمان ← الشاشات |
| **روابط من** | القائمة الرئيسية |
| **روابط إلى** | SCR-SEC-003 (إسناد الصفحة لدور) |

---

### B2 — مواصفة البحث (Search Specification)

> ⚠ **ملاحظة مُصحَّحة (OQ-013، 2026-07-22)**: النتائج تُعرَض كجدول مسطَّح قياسي (AG Grid، `pages-search.component.ts`) — **ليس** كشجرة هرمية. الادّعاء السابق بوجود عرض شجري ومكوّن `TreeTable` كان خاطئاً وأُزيل.

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| pageCode | نص | لا | — | |
| module | نص | لا | — | |
| active | قائمة منسدلة | لا | true/false | Endpoint مخصَّص `/active` أيضاً |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_PAGE_CREATE |
| Edit | عند تحديد سجل | PERM_PAGE_UPDATE |
| تعطيل | عند تحديد سجل | PERM_PAGE_DELETE (استخدام موثَّق في الكود لتعطيل الشاشة) |
| إعادة تفعيل | عند تحديد سجل معطَّل | PERM_PAGE_UPDATE |

#### قواعد البحث المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| RULE-SEC-046 | — | ← see A4 |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| pageCode | نص | نعم | ENTITY-SEC-004 → A3 | نمط `^[A-Z0-9_]+$`، uppercase تلقائياً |
| nameAr / nameEn | نص | نعم | ENTITY-SEC-004 → A3 | |
| route | نص | نعم | ENTITY-SEC-004 → A3 | يبدأ بـ `/` |
| icon | نص | لا | ENTITY-SEC-004 → A3 | |
| module | نص | لا | ENTITY-SEC-004 → A3 | |
| parentId | قائمة اختيار (ذاتي) | لا | ENTITY-SEC-004 → A3 | لا يجوز الإشارة للنفس |
| displayOrder | رقم | لا | ENTITY-SEC-004 → A3 | |
| description | نص | لا | ENTITY-SEC-004 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ | POST / PUT | RULE-SEC-046, RULE-SEC-047 |
| إلغاء | navigation back | — |
| تعطيل | PUT /deactivate | — |
| إعادة تفعيل | PUT /reactivate | — |

#### قواعد الإدخال المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| RULE-SEC-046 | عند الحفظ | ← see A4 |
| RULE-SEC-047 | عند الإنشاء (توليد الصلاحيات) | ← see A4 |

> ⚠ ملاحظة AS-IS: `POST /api/pages` و`PUT /api/pages/{id}` لا يحملان `@PreAuthorize` ظاهراً على مستوى الـ Controller (بعكس بقية عمليات هذه الشاشة) — التحقُّق موجود فعلياً على مستوى `PageService` (`PAGE_CREATE`/`PAGE_UPDATE`)، فالتطبيق الفعلي مُطبَّق رغم غياب التوثيق على الـ Controller. مُثبَت هنا كما هو (extracted-facts-SECURITY.md LAYER 3.1).

---

### B4 — الصلاحيات (Permissions)

| الشاشة | عرض (VIEW) | إنشاء | تعديل | حذف (تعطيل) | تصدير |
|---|---|---|---|---|---|
| SCR-SEC-005 | PERM_PAGE_VIEW | PERM_PAGE_CREATE (مُطبَّقة على مستوى Service) | PERM_PAGE_UPDATE (مُطبَّقة على مستوى Service) | PERM_PAGE_DELETE | PERM_PAGE_VIEW |

**Security Seed Data:**
```
SEC_PAGES  : INSERT — page_code = PAGE, parent_id_fk = [الأمان]
PERMISSIONS: INSERT × 4 — PERM_PAGE_VIEW / CREATE / UPDATE / DELETE
```

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-SEC-030 | إنشاء شاشة | POST | /api/pages | pageCode, nameAr, nameEn, route, ... | PageDto + 4 صلاحيات | RULE-SEC-046, RULE-SEC-047 |
| API-SEC-031 | بحث | POST | /api/pages/search | pageCode?, module?, page, size | قائمة PageDto | — |
| API-SEC-032 | الشاشات النشطة | GET | /api/pages/active | — | قائمة PageDto نشطة | — |
| API-SEC-033 | جلب بالمعرّف | GET | /api/pages/{id} | id | PageDto | — |
| API-SEC-034 | تعديل | PUT | /api/pages/{id} | الحقول المعدَّلة | PageDto محدَّث | RULE-SEC-046 |
| API-SEC-035 | تعطيل | PUT | /api/pages/{id}/deactivate | id | PageDto | — |
| API-SEC-036 | إعادة تفعيل | PUT | /api/pages/{id}/reactivate | id | PageDto | — |

> ⚠ ملاحظة (OQ-013، نهائية 2026-07-22): لا شجرة مطلوبة حالياً — الشاشة الفعلية مسطَّحة (PATTERN-1، AG Grid). لا يوجد endpoint شجرة أصلاً (لا ما يعادل `/api/v1/org/departments/tree`)، وهذا متّسق مع كون الواجهة الفعلية لا تحتاجه. **BLOCKED كبند مستقبلي فقط**: إن احتاج المنتج لاحقاً عرضاً شجرياً حقيقياً لـ SCR-SEC-005، هذا يتطلب: (1) بناء مكوّن Tree في الـ frontend من الصفر (غير موجود إطلاقاً حالياً)، و(2) endpoint شجرة جديد في الـ Backend — كلاهما تطوير جديد خارج نطاق التوثيق AS-IS، ويُوصى بتأجيله كبند Backlog حتى تُبنى وحدة frontend لـ Organization فعلياً (والتي لا وجود لها حالياً أيضاً — راجع SCR-SEC-005 B1 أعلاه).

---
---

## SCR-SEC-006 — الملف التعريفي للمستخدم (User Profile) — [هوية AS-BUILT محفوظة كما هي]

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-SEC-006 |
| **اسم الشاشة** | الملف التعريفي للمستخدم (نطاق البيانات) |
| **UI Pattern** | PATTERN-2 — Inline / Modal |
| **Pattern Reason** | كيان بسيط — حقول ≤ 8، بلا subentities — 5.8.2 |
| **SCR-ID Scope** | ONE SCR-ID covers: UNIFIED |
| **P3 Implication** | شاشة واحدة بتحرير Modal/Inline |
| **ENTITY-ID** | ENTITY-SEC-009 |
| **وظيفة الشاشة** | ربط المستخدم بفرع تنظيمي واسم كامل ولغة مفضَّلة ورقم موظف (اختياري) |
| **المستخدمون** | Admin (PERM_USER_PROFILE_*) |
| **الموضع في النظام** | الأمان ← المستخدمون ← الملف التعريفي |
| **روابط من** | SCR-SEC-002 (إدارة المستخدمين) |
| **روابط إلى** | — |

**UI Structure Decision:**

| Data Size | Small (≤8 fields) | Interaction | Modal |
|---|---|---|---|
| Pattern | PATTERN-2 | Reason | 6 حقول فقط، امتداد 1:1 لمستخدم موجود مسبقاً |

---

### B2 — مواصفة البحث (Search Specification)

> **أُضيف هذا البلوك في 2026-07-22 (حل OQ-010)** — كان مفقوداً رغم وجود `API-SEC-039` (`POST /api/v1/security/user-profiles/search`). الفلاتر مُشتقَّة من حقول B3 فقط.
>
> ⚠ **فجوة كود اكتُشِفت وأُصلِحت أثناء إعادة التحقق (2026-07-22)**: عند إضافة هذا البلوك أول مرة، حقلا `fullNameAr`/`fullNameEn` أدناه **لم يكونا فعلياً ضمن** `SecUserProfileService.ALLOWED_SEARCH_FIELDS` — أي أن الفلترة بهما كانت ستُسبِّب `SearchException` في الإنتاج، رغم أن شبكة الواجهة الأمامية (`user-profile-grid.config.ts:27-28`) توصّلهما فعلاً كفلاتر حيّة (`agTextColumnFilter`). **تم الإصلاح**: أُضيف `fullNameAr, fullNameEn` إلى `ALLOWED_SEARCH_FIELDS` في `SecUserProfileService.java:48-52` (مع إبقاء `ALLOWED_SORT_FIELDS` كما هي، لأن الواجهة تُحدِّد `sortable: false` لهذين العمودين عمداً). أُضيف `SecUserProfileServiceSearchTest.java` يثبت أن الفلترة بالحقلين لم تعد تُلقي استثناء — 2/2 ناجحة. الجدول أدناه صحيح الآن فعلياً، لا توثيقياً فقط.

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| branchIdFk | قائمة اختيار (خارجية) | لا | ENTITY-ORG-002 | |
| fullNameAr / fullNameEn | نص | لا | ENTITY-SEC-009 → A3 | مطابقة جزئية — ✓ أُصلِح دعمها في `ALLOWED_SEARCH_FIELDS` (2026-07-22) |
| isActiveFl | قائمة منسدلة | لا | true/false | |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_USER_PROFILE_CREATE |
| Edit | عند تحديد سجل | PERM_USER_PROFILE_UPDATE |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| branchIdFk | قائمة اختيار (خارجية) | نعم | ENTITY-SEC-009 → A3 | يُتحقَّق نشاطه عبر Cross-Module — RULE-SEC-034 |
| fullNameAr / fullNameEn | نص | لا | ENTITY-SEC-009 → A3 | |
| preferredLang | نص حر | لا | ENTITY-SEC-009 → A3 | **نص حر بقرار نهائي — OQ-001 CLOSED** |
| employeeIdFk | نص/رقم | لا | ENTITY-SEC-009 → A3 | **بلا FK بقرار نهائي — OQ-002 CLOSED (لا HR حالياً)** |
| isActiveFl | مفتاح تبديل | نعم | ENTITY-SEC-009 → A3 | تعطيل بديل عن الحذف |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ | POST / PUT | RULE-SEC-034 |
| إلغاء | إغلاق Modal | — |

> ⚠ **لا يوجد زر/إجراء حذف** — هذه الشاشة (SCR-SEC-006) موثَّقة صراحة في business-policies-SECURITY.md كاستثناء متعمَّد: 3 صلاحيات فقط (VIEW/CREATE/UPDATE، بلا DELETE) لأن التعطيل يتم عبر `isActiveFl`/UPDATE حصراً — انظر RULE-SEC-047.

---

### B4 — الصلاحيات (Permissions)

> **استثناء موثَّق (RULE-SEC-047):** هذه الشاشة الوحيدة في الموديول بصلاحيات VIEW/CREATE/UPDATE فقط — بلا DELETE.

| الشاشة | عرض (VIEW) | إنشاء | تعديل | حذف | تصدير |
|---|---|---|---|---|---|
| SCR-SEC-006 | PERM_USER_PROFILE_VIEW | PERM_USER_PROFILE_CREATE | PERM_USER_PROFILE_UPDATE | **لا يوجد (متعمَّد)** | PERM_USER_PROFILE_VIEW |

**Security Seed Data:**
```
SEC_PAGES  : INSERT — page_code = USER_PROFILE, parent_id_fk = [المستخدمون]
PERMISSIONS: INSERT × 3 (استثناء يدوي عبر SQL مباشر — لا يوجد DELETE)
             — PERM_USER_PROFILE_VIEW / CREATE / UPDATE
```

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-SEC-037 | إنشاء ملف تعريفي | POST | /api/v1/security/user-profiles | userId, branchIdFk, fullNameAr?, fullNameEn?, preferredLang?, employeeIdFk? | SecUserProfileDto | RULE-SEC-034 |
| API-SEC-038 | جلب الكل | GET | /api/v1/security/user-profiles | page, size | قائمة SecUserProfileDto | — |
| API-SEC-039 | بحث | POST | /api/v1/security/user-profiles/search | فلاتر | قائمة SecUserProfileDto | — |
| API-SEC-040 | جلب بالمعرّف | GET | /api/v1/security/user-profiles/{userId} | userId | SecUserProfileDto | — |
| API-SEC-041 | تعديل | PUT | /api/v1/security/user-profiles/{userId} | الحقول المعدَّلة | SecUserProfileDto محدَّث | RULE-SEC-034 |

---
---

## SCR-SEC-007 — نطاق بيانات الدور بالفرع (Role Data Scope — Branch Assignment)

---

### B1 — تعريف الشاشة (Screen Definition)

| البند | القيمة |
|---|---|
| **SCR-ID** | SCR-SEC-007 |
| **اسم الشاشة** | ربط الدور بالفروع ومستوى الوصول للبيانات |
| **UI Pattern** | PATTERN-2 — Inline / Modal |
| **Pattern Reason** | كيان بسيط — 4 حقول فقط، بلا subentities — 5.8.2 |
| **SCR-ID Scope** | ONE SCR-ID covers: UNIFIED |
| **P3 Implication** | شاشة واحدة (غالباً كلوحة فرعية ضمن SCR-SEC-003) بتحرير Modal/Inline |
| **ENTITY-ID** | ENTITY-SEC-010 |
| **وظيفة الشاشة** | تحديد الفروع التي يصل إليها كل دور، ومستوى الوصول لبياناتها |
| **المستخدمون** | Admin (يعيد استخدام PERM_ROLE_* — لا صلاحيات جديدة) |
| **الموضع في النظام** | الأمان ← الأدوار ← نطاق البيانات |
| **روابط من** | SCR-SEC-003 (إدارة الأدوار) |
| **روابط إلى** | — |

**UI Structure Decision:**

| Data Size | Small (≤8 fields) | Interaction | Modal |
|---|---|---|---|
| Pattern | PATTERN-2 | Reason | 4 حقول فقط (roleIdFk, branchIdFk, dataAccessLevel, isActiveFl) |

---

### B2 — مواصفة البحث (Search Specification)

> **أُضيف هذا البلوك في 2026-07-22 (حل OQ-010)** — كان مفقوداً رغم وجود `API-SEC-044` (`POST /api/v1/security/role-branches/search`). الفلاتر مُشتقَّة من حقول B3 فقط — لا حقول جديدة.

#### فلاتر البحث وأعمدة النتائج

| اسم الحقل | نوع الحقل | إلزامي | القيم / المصدر | ملاحظات |
|---|---|---|---|---|
| roleIdFk | قائمة اختيار | لا | ENTITY-SEC-002 → A3 | |
| branchIdFk | قائمة اختيار (خارجية) | لا | ENTITY-ORG-002 | |
| dataAccessLevel | قائمة منسدلة | لا | LOV-SEC-002 → A5 | |
| isActiveFl | قائمة منسدلة | لا | true/false | |

#### الإجراءات المتاحة

| الإجراء | الشرط | الصلاحية المطلوبة |
|---|---|---|
| New | دائماً | PERM_ROLE_UPDATE |
| Edit | عند تحديد سجل | PERM_ROLE_UPDATE |

---

### B3 — مواصفة الإدخال (Input Specification)

#### حقول شاشة الإدخال

| اسم الحقل | نوع الحقل | إلزامي | المصدر | ملاحظات |
|---|---|---|---|---|
| roleIdFk | قائمة اختيار | نعم | ENTITY-SEC-010 → A3 | جزء من PK مركّب |
| branchIdFk | قائمة اختيار (خارجية) | نعم | ENTITY-SEC-010 → A3 | جزء من PK مركّب — Cross-Module |
| dataAccessLevel | قائمة منسدلة | نعم | LOV-SEC-002 → A5 | |
| isActiveFl | مفتاح تبديل | نعم | ENTITY-SEC-010 → A3 | |

#### الأزرار والإجراءات

| الزر | الإجراء | RULE-IDs المطبَّقة |
|---|---|---|
| حفظ | POST / PUT | RULE-SEC-035, RULE-SEC-036 |
| إلغاء | إغلاق Modal | — |
| حذف | DELETE | — |

#### قواعد الإدخال المطبَّقة

| RULE-ID | الشرط | *(التفاصيل في A4)* |
|---|---|---|
| RULE-SEC-035 | عند الحفظ | ← see A4 |
| RULE-SEC-036 | عند الإنشاء | ← see A4 |

---

### B4 — الصلاحيات (Permissions)

> **لا صلاحيات جديدة** — يعيد استخدام `PERM_ROLE_*` الموجودة (لا SEC_PAGES row مستقلة — الـ Controller "thin" بلا `@PreAuthorize` خاص به، والتحقُّق بالكامل داخل `SecRoleBranchService`).

| الشاشة | عرض (VIEW) | إنشاء | تعديل | حذف | تصدير |
|---|---|---|---|---|---|
| SCR-SEC-007 | PERM_ROLE_VIEW | PERM_ROLE_UPDATE | PERM_ROLE_UPDATE | PERM_ROLE_UPDATE | PERM_ROLE_VIEW |

---

### B5 — الواجهات البرمجية (Functional APIs)

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-SEC-042 | إنشاء إسناد | POST | /api/v1/security/role-branches | roleIdFk, branchIdFk, dataAccessLevel | SecRoleBranchDto | RULE-SEC-035, RULE-SEC-036 |
| API-SEC-043 | جلب الكل | GET | /api/v1/security/role-branches | page, size | قائمة SecRoleBranchDto | — |
| API-SEC-044 | بحث | POST | /api/v1/security/role-branches/search | فلاتر | قائمة SecRoleBranchDto | — |
| API-SEC-045 | جلب بالمعرّف | GET | /api/v1/security/role-branches/{roleId}/{branchId} | roleId, branchId | SecRoleBranchDto | — |
| API-SEC-046 | تعديل | PUT | /api/v1/security/role-branches/{roleId}/{branchId} | dataAccessLevel, isActiveFl | SecRoleBranchDto محدَّث | RULE-SEC-035 |
| API-SEC-047 | حذف | DELETE | /api/v1/security/role-branches/{roleId}/{branchId} | roleId, branchId | 204 | — |

---
---

> **ملاحظة Standalone — القائمة (Menu):** لا يوجد `SEC_PAGES` row أو صلاحيات RBAC مخصَّصة لبناء القائمة نفسها — `GET /api/menu/user-menu` (بلا `@PreAuthorize`، أي مستخدم مُصادَق) و`GET /api/menu/user-menu/{userId}` (`PERM_USER_VIEW`، أداة تشخيص إدارية) هما نداءا API فقط يُستهلكان برمجياً لبناء شجرة التنقّل من `SEC_PAGES` + صلاحيات VIEW الحالية للمستخدم — **لم يُمنَح SCR-ID مستقل** لعدم وجود أي دليل على شاشة إدارية مستقلة لهما في المصادر المرفقة (HR-1 — لا اختراع). تُوثَّق كـ APIs مستقلة أدناه فقط.

| API-ID | العملية | HTTP | المسار | المدخلات | المخرجات | RULE-IDs |
|---|---|---|---|---|---|---|
| API-SEC-048 | قائمة تنقّل المستخدم الحالي | GET | /api/menu/user-menu | — (JWT) | شجرة MenuItemDto[] | — |
| API-SEC-049 | قائمة تنقّل مستخدم مُحدَّد (تشخيص إداري) | GET | /api/menu/user-menu/{userId} | userId | شجرة MenuItemDto[] | — |

---

# ══════════════════════════════════════════════════════════
# STANDALONE — بعد PART B
# ══════════════════════════════════════════════════════════

---

## Permissions Summary & Registry Update

> **CORE-9 COMPOSITE SCREEN RULE:** Search + Entry = SCR-ID واحد — VIEW هو الـ gateway للشاشتين.

| الشاشة | عرض (VIEW) | إنشاء (CREATE) | تعديل (UPDATE) | حذف (DELETE) | تصدير |
|---|---|---|---|---|---|
| SCR-SEC-001 (مصادقة/خدمة ذاتية) | عام | عام | عام | — | — |
| SCR-SEC-002 (المستخدمون، بحث+إدخال) | PERM_USER_VIEW | PERM_USER_CREATE | PERM_USER_UPDATE | PERM_USER_DELETE | PERM_USER_VIEW |
| SCR-SEC-003 (الأدوار+RBAC، بحث+إدخال) | PERM_ROLE_VIEW | PERM_ROLE_CREATE | PERM_ROLE_UPDATE | PERM_ROLE_DELETE | PERM_ROLE_VIEW |
| SCR-SEC-004 (الصلاحيات) | PERM_PERMISSION_VIEW | PERM_PERMISSION_CREATE | PERM_PERMISSION_UPDATE ✓ مُصلَحة (OQ-005) | — | PERM_PERMISSION_VIEW |
| SCR-SEC-005 (الشاشات، بحث+إدخال) | PERM_PAGE_VIEW | PERM_PAGE_CREATE | PERM_PAGE_UPDATE | PERM_PAGE_DELETE | PERM_PAGE_VIEW |
| SCR-SEC-006 (الملف التعريفي) | PERM_USER_PROFILE_VIEW | PERM_USER_PROFILE_CREATE | PERM_USER_PROFILE_UPDATE | **لا يوجد (متعمَّد)** | PERM_USER_PROFILE_VIEW |
| SCR-SEC-007 (نطاق البيانات بالفرع) | PERM_ROLE_VIEW | PERM_ROLE_UPDATE | PERM_ROLE_UPDATE | PERM_ROLE_UPDATE | PERM_ROLE_VIEW |

---

### Registry Update — MODE 1

```
## REGISTRY UPDATE — 2026-07-22 (amended — master-registry.md v2.10.0 reviewed)
────────────────────────────────────────────────────────────────
Source Mode    : MODE 1 (Reverse-Engineered / EXCEPTION module)
Feature Code   : SEC-001
DBS-ID         : —
Plan ID        : —
Amendment      : master-registry.md (v2.10.0) supplied after initial
                 issue. GOVERNANCE REDUCED lifted. Reconciled against
                 Sec. 4/5/6/7/8/10/13/14/15 — see "MASTER-REGISTRY
                 ALIGNMENT REVIEW" block above for full findings.
                 ENTITY-SEC-009/010 (and by extension 011/012)
                 re-labeled EXTENSION scope / PARTIALLY_READY ⚠️
                 (was blanket EXCEPTION). Canonical ENTITY-ORG-002
                 (Branch/ORG_BRANCH) substituted for generic external
                 references in A7. Two new conflicts raised as
                 OQ-006/OQ-007 (not yet in master-registry Sec. 13 —
                 Registry Maintainer should log them there once
                 architecturally resolved).
────────────────────────────────────────────────────────────────
New Entities   : ENTITY-SEC-001 (PRIVATE, UserAccount)
                 ENTITY-SEC-002 (PRIVATE, Role)
                 ENTITY-SEC-003 (PRIVATE, Permission)
                 ENTITY-SEC-004 (PRIVATE, Page)
                 ENTITY-SEC-005 (PRIVATE, RefreshToken)
                 ENTITY-SEC-006 (INTERNAL/JOIN, USER_ROLES)
                 ENTITY-SEC-007 (INTERNAL/JOIN, ROLE_PERMISSIONS)
                 ENTITY-SEC-009 (PRIVATE/SHARED-consumer, SecUserProfile)
                 ENTITY-SEC-010 (PRIVATE/SHARED-consumer, SecRoleBranch)
                 ENTITY-SEC-011 (PRIVATE, PasswordResetToken)
                 ENTITY-SEC-012 (PRIVATE, AccountActivationToken)
New Tables     : USERS, ROLES, PERMISSIONS, SEC_PAGES, REFRESH_TOKENS,
                 USER_ROLES, ROLE_PERMISSIONS, SEC_USER_PROFILE,
                 SEC_ROLE_BRANCH, PASSWORD_RESET_TOKEN,
                 ACCOUNT_ACTIVATION_TOKEN
                 (all ALREADY EXIST — AS-BUILT, not newly created by
                 this SRS)
New Lookups    : DATA_ACCESS_LEVEL (LOV-SEC-002, already seeded AS-BUILT)
New APIs       : API-SEC-001 through API-SEC-049
XM-IDs Open    : XM-SEC-001 (SecUserProfile→ORG_BRANCH) — APPROVED WITH
                 DOCUMENTED EXCEPTION (OQ-007 CLOSED, DB-level FK
                 accepted),
                 XM-SEC-002 (SecRoleBranch→ORG_BRANCH) — same,
                 + 1 unnamed candidate (SecRoleBranch→MasterData
                 DATA_ACCESS_LEVEL, SOFT-READ)
OQ-IDs Open    : **NONE — all 13 raised OQ-IDs (OQ-001 through OQ-013)
                 are CLOSED as of 2026-07-22.** See OQ Log for full
                 resolution detail per item, including which closures
                 came from an explicit Architect decision vs. this
                 engine's own governance judgment (OQ-006/007/008/009 —
                 flagged, lower-certainty closures, reopen on new
                 evidence).
Gate Status    : PASSED ✓ — registry-verified (master-registry.md
                 v2.10.0 reconciled) + code-investigated (all 7 prior
                 OQs given definitive code evidence) + fully closed
                 (Architect decisions applied for OQ-001/002/004; this
                 engine's own judgment applied for OQ-006/007/008/009,
                 explicitly flagged as such).
Next Action    : Trigger MODE 1.5 — Database Governance Engine
                 (Project 2), treating db-script as ALREADY EXISTING
                 (AS-BUILT). Recommended (not blocking, owned by the
                 Registry Maintainer, not this engine): log OQ-006's
                 and OQ-007's resolutions into master-registry.md
                 Section 13 as new, CLOSED Conflict entries so the
                 master registry's own text matches what this SRS now
                 states as settled.
────────────────────────────────────────────────────────────────
```

---

## OQ Log — سجل الأسئلة المفتوحة

```
## OPEN QUESTIONS LOG — SECURITY — 2026-07-22 (FINALIZED — all 13 closed)
─────────────────────────────────────────────────────────────────────
OQ-ID  │ Question → Decision                    │ Status      │ Raised  │ Resolved   │ Decided By
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-001 │ preferredLang (ENTITY-SEC-009) بلا LOV  │ **CLOSED**  │ MODE 1  │ 2026-07-22 │ Architect
       │ محكوم. Fact confirmed by code invest.:   │             │         │            │ (Hesham)
       │ SecUserProfile.java:55-56 plain           │             │         │            │
       │ VARCHAR(10); no MD_MASTER_LOOKUP seed for │             │         │            │
       │ LANG/LANGUAGE anywhere. **DECISION:       │             │         │            │
       │ يبقى نصاً حراً (free text) — لا يُحوَّل    │             │         │            │
       │ إلى LOV محكوم.** التطبيق: A3/A5 محدَّثتان  │             │         │            │
       │ أدناه لتوثيق هذا كقرار نهائي، لا كفجوة    │             │         │            │
       │ مفتوحة.                                   │             │         │            │
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-002 │ employeeIdFk بلا FK فعلي. Fact confirmed  │ **CLOSED**  │ MODE 1  │ 2026-07-22 │ Architect
       │ by code invest.: no erp-hr module exists  │             │         │            │ (Hesham)
       │ anywhere in the repo; FK-add statement     │             │         │            │
       │ commented out, never uncommented.          │             │         │            │
       │ **DECISION: لا يوجد HR الآن — يبقى العمود  │             │         │            │
       │ غير مقيَّد (no FK) إلى أن يُبنى موديول HR   │             │         │            │
       │ فعلياً ويمر عبر MODE 1.5 الخاص به.** لا     │             │         │            │
       │ حذف للعمود، لا FK مؤقت. يُعاد فتح هذا البند │             │         │            │
       │ تلقائياً فقط عند بدء P0 لموديول HR.         │             │         │            │
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-003 │ هل NotificationService يستمع فعلاً؟       │ **CLOSED**  │ MODE 1  │ 2026-08-23 │ Code
       │ **⚠ SUPERSEDED 2026-08-23 (backend.zip     │ (re-        │         │ (re-       │ investigation
       │ verified directly)** — الإجابة الأصلية      │ answered,   │         │ answered   │ — module
       │ (2026-07-22: "لا يوجد Listener على          │ not merely  │         │ 2026-08-23)│ evolved
       │ الإطلاق") كانت **صحيحة لحالة الكود وقتها**،  │ re-verified)│         │            │
       │ لكن **الكود تطوَّر منذ ذلك الحين**: تحقُّق    │             │         │            │
       │ مباشر جديد على `backend.zip` (2026-08-23)   │             │         │            │
       │ يُظهر أن `AuthEventListener`                 │             │         │            │
       │ (`@TransactionalEventListener AFTER_COMMIT`)│             │         │            │
       │ + `NotificationClient` **موجودان الآن فعلياً**│             │         │            │
       │ ويستدعيان `POST /api/v1/notifications/send`  │             │         │            │
       │ بنجاح، عبر حساب خدمة مخصَّص (`svc-notification`،│             │         │            │
       │ RULE-SEC-053 الجديدة). **لا تناقض بين        │             │         │            │
       │ الإجابتين — لقطتان زمنيتان مختلفتان لنفس      │             │         │            │
       │ الكود المتطوِّر.** RULE-SEC-031/GAP-SEC-02     │             │         │            │
       │ محدَّثان أعلاه من "مؤكَّد غياب" إلى "مؤكَّد      │             │         │            │
       │ وجود ونشاط". **الفجوة الهندسية السابقة        │             │         │            │
       │ أُغلِقت فعلياً بواسطة فريق التطوير، لا بهذه     │             │         │            │
       │ الوثيقة.**                                    │             │         │            │
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-004 │ اعتماد Cross-Module يمرِّر Authorization   │ **CLOSED**  │ MODE 1  │ 2026-07-22 │ Architect
       │ header حرفياً. Fact confirmed by code       │             │         │            │ (Hesham)
       │ invest.: OrgBranchClient/MasterDataLookup  │             │         │            │
       │ Client كلاهما يمرِّران الـ header الوارد     │             │         │            │
       │ حرفياً؛ لا اعتماد خدمة منفصل موجود.          │             │         │            │
       │ **DECISION: لا يوجد اعتماد خدمة-لخدمة       │             │         │            │
       │ مخصَّص، ولن يُبنى الآن — النمط الحالي        │             │         │            │
       │ (تمرير Header المستخدم) مقبول كتصميم مؤقت   │             │         │            │
       │ للمرحلة الحالية.** يُعاد فتح هذا البند إذا   │             │         │            │
       │ ظهرت متطلبات أمنية أشد (مثلاً: نداءات       │             │         │            │
       │ background بلا سياق مستخدم).                 │             │         │            │
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-005 │ PUT /api/permissions/{id} بلا @PreAuthorize│ **CLOSED**  │ MODE 1  │ 2026-07-22 │ Code fix
       │ — ثغرة أمنية حية مؤكَّدة. **FIX APPLIED**:  │ (resolved)  │         │ (code fix  │ applied
       │ أُضيف `@PreAuthorize("hasAuthority(        │             │         │ applied)   │
       │ 'PERMISSION_UPDATE')")`، مع اختبارات        │             │         │            │
       │ انحدار 200/403/401. انظر B4/B5 تحت          │             │         │            │
       │ SCR-SEC-004.                                │             │         │            │
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-006 │ تعارض مستويات DataScope (4 مُعلَنة مقابل   │ **CLOSED**  │ MODE 1  │ 2026-07-22 │ SRS Governance
       │ 3 مُنفَّذة فعلياً، مؤكَّد بالكود على كل فرع  │             │         │            │ Engine (P1) —
       │ متاح — لا أثر لنموذج 4 مستويات إطلاقاً).   │             │         │            │ ⚠ تطبيق
       │ **DECISION (مُطبَّق بصلاحية هذا المحرك، غير  │             │         │            │ استدلالي —
       │ مُملى من architect منفصل)**: تُحدَّث النية    │             │         │            │ يخضع لمراجعة
       │ المُعلَنة في master-registry.md لتطابق       │             │         │            │ Registry
       │ الواقع الفعلي (3 مستويات) — **وليس** إعادة   │             │         │            │ Maintainer
       │ تصميم DataScope. السبب: Security أصلاً       │             │         │            │
       │ EXCEPTION module مُوثَّق AS-IS، وتوسيع        │             │         │            │
       │ DataScope فعلياً غير مطروح كطلب عمل حالياً.  │             │         │            │
       │ **إجراء متبقٍّ خارج نطاق هذه الوثيقة**:       │             │         │            │
       │ تحديث master-registry.md Section 8 + إضافة   │             │         │            │
       │ Conflict entry جديد (Section 13) — يقوم بها  │             │         │            │
       │ مالك السجل الرئيسي، ليست ملكية هذا المحرك.    │             │         │            │
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-007 │ DB FK على ORG_BRANCH يخالف قاعدة service-   │ **CLOSED**  │ MODE 1  │ 2026-07-22 │ SRS Governance
       │ only في module-registry-org (مؤكَّد بالكود:  │             │         │            │ Engine (P1) —
       │ القيدان حيّان فعلياً في السكيما المُطبَّقة،   │             │         │            │ ⚠ تطبيق
       │ بلا أي DROP CONSTRAINT).                     │             │         │            │ استدلالي —
       │ **DECISION (مُطبَّق بصلاحية هذا المحرك)**:    │             │         │            │ يخضع لمراجعة
       │ يُعتمَد كاستثناء معماري مقبول ضمن EXTENSION   │             │         │            │ Registry
       │ scope — نفس منطق EXCEPTION الدائم المُطبَّق   │             │         │            │ Maintainer
       │ أصلاً على Security (Conflict #1/#3). السبب:  │             │         │            │
       │ القيد موجود منذ الإنشاء، له تبرير هندسي مذكور│             │         │            │
       │ في تعليق الكود، وإزالته الآن مخاطرة أعلى من   │             │         │            │
       │ إبقائه. XM-SEC-001/XM-SEC-002 يُعتمَدان        │             │         │            │
       │ رسمياً في MODE 1.5 **بهذا الاستثناء موثَّقاً**.│             │         │            │
       │ **إجراء متبقٍّ خارج نطاق هذه الوثيقة**: تسجيل  │             │         │            │
       │ Conflict entry جديد Status=CLOSED في          │             │         │            │
       │ master-registry.md Section 13 — مالك السجل    │             │         │            │
       │ الرئيسي، ليست ملكية هذا المحرك.                │             │         │            │
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-008 │ [P2.5] ملكية شاشة القائمة (US-SEC-14 /      │ **CLOSED**  │ P2.5    │ 2026-07-22 │ SRS Governance
       │ API-SEC-048). **⚠ لم يُجرَ تحقيق فعلي في      │ (⚠ closed   │ session │            │ Engine (P1) —
       │ مستودع الـ shell/frontend — لا دليل كود        │ بدون دليل   │         │            │ **بلا تحقيق
       │ لهذا القرار.** DECISION المُطبَّق هنا هو        │ كود، انظر   │         │            │ كود — أضعف
       │ اجتهاد حوكمي فقط: بما أن Security تملك         │ الملاحظة)   │         │            │ درجات
       │ SEC_PAGES ونموذج الصلاحيات بالكامل،            │             │         │            │ اليقين في
       │ ونقطة API-SEC-048 مُستضافة أصلاً داخل           │             │         │            │ هذا السجل**
       │ MenuController في erp-security، تُعتبَر هذه     │             │         │            │
       │ **API خلفية مملوكة لـ Security فقط، بلا شاشة    │             │         │            │
       │ إدارية مستقلة (لا SCR-ID)** — تُستهلَك برمجياً   │             │         │            │
       │ من مكوّن Sidebar الجاهز في الـ platform shell.  │             │         │            │
       │ **إذا تبيَّن لاحقاً من فحص الـ shell أن الافتراض│             │         │            │
       │ خاطئ، يُعاد فتح هذا البند فوراً.**              │             │         │            │
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-009 │ [P2.5] قائمة تشخيص المستخدم للأدمن           │ **CLOSED**  │ P2.5    │ 2026-07-22 │ SRS Governance
       │ (US-SEC-15 / API-SEC-049). **⚠ لم يُجرَ         │ (⚠ closed   │ session │            │ Engine (P1) —
       │ تحقيق فعلي — لا دليل كود أو منتج لهذا           │ بدون دليل   │         │            │ **بلا تحقيق
       │ القرار.** DECISION المُطبَّق: تُعتبَر           │ كود، انظر   │         │            │ — أضعف
       │ **API-SEC-049 أداة تشخيص/API فقط، بلا شاشة       │ الملاحظة)   │         │            │ درجات
       │ إدارية مستقلة (لا SCR-ID)** — نفس معاملة        │             │         │            │ اليقين في
       │ OQ-008، بنفس المنطق (تُستهلَك برمجياً وقت        │             │         │            │ هذا السجل**
       │ الحاجة فقط، دون واجهة مخصَّصة). **يُعاد فتحه     │             │         │            │
       │ فوراً إذا طلب فريق المنتج/الفرونت شاشة فعلية.**  │             │         │            │
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-010 │ [P2.5] SCR-SEC-004/006/007 بلا بلوك B2       │ **CLOSED**  │ P2.5    │ 2026-07-22 │ SRS Governance
       │ رغم وجود endpoint بحث لكل منها. **FIX          │ (resolved)  │ session │            │ Engine (P1)
       │ APPLIED**: أُضيفت بلوكات B2 كاملة للشاشات      │             │         │            │
       │ الثلاث، مُشتقَّة من B3/B5 الموجودة (HR-1        │             │         │            │
       │ محفوظة — لا حقول مُخترَعة).                      │             │         │            │
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-011 │ **مسحوب (WITHDRAWN)** — قراءة مخطوءة لـ CORE-8.│ WITHDRAWN   │ P2.5    │ 2026-07-22 │ —
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-012 │ **مغلق (CLOSED)** — أجابت عنه CORE-8/CORE-9    │ CLOSED      │ P2.5    │ 2026-07-22 │ CORE-8/CORE-9
       │ مباشرةً دون الحاجة لتحقيق إضافي.                │             │ session │            │ (self-answering)
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-013 │ [P2.5] SCR-SEC-005: قائمة مسطَّحة أم شجرة؟    │ **CLOSED**  │ P2.5    │ 2026-07-22 │ SRS Governance
       │ **FIX APPLIED**: تغيير B1 UI Pattern من        │ (resolved)  │ session │            │ Engine (P1)
       │ PATTERN-1 إلى PATTERN-3 (Tree Hierarchy)،      │             │         │            │
       │ مطابقاً لـ SCR-ORG-004/005.                     │             │         │            │
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-014 │ **[جديد 2026-08-23]** لا مهمة تنظيف دورية      │ OPEN        │ 2026-08 │ —          │ LOCAL —
       │ لـ `PASSWORD_RESET_TOKEN`/                       │             │ -23     │            │ Architect
       │ `ACCOUNT_ACTIVATION_TOKEN` (بعكس                 │             │ (backend│            │ decision
       │ `REFRESH_TOKENS` — RULE-SEC-052). **مؤكَّد        │             │ .zip    │            │ needed
       │ بالبحث المباشر**: لا `@Scheduled`/Job مماثل       │             │ direct  │            │
       │ لهذين الجدولين في كامل `erp-security`. الرموز     │             │ verif.) │            │
       │ المنتهية/المُستخدَمة تتراكم بلا حد أقصى.           │             │         │            │
       │ **قرار مطلوب**: إضافة Job مماثل لـ                │             │         │            │
       │ `RefreshTokenCleanupJob`، أم يُقبَل كخطر منخفض     │             │         │            │
       │ (حجم بيانات صغير عادة) بلا إجراء الآن؟             │             │         │            │
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-015 │ **[جديد 2026-08-23]** `allowedBranches[]`        │ OPEN        │ 2026-08 │ —          │ LOCAL —
       │ (RULE-SEC-037) يُصدَر في الـ JWT لكن **لا يُستهلَك │             │ -23     │            │ Architect
       │ في أي مكان بالكود** — DataScope غير مُفعَّل فعلياً │             │ (backend│            │ decision
       │ عند الوصول للبيانات، رغم أن CRUD الخاص بـ         │             │ .zip    │            │ required —
       │ `SEC_USER_PROFILE`/`SEC_ROLE_BRANCH` كامل.        │             │ direct  │            │ largest
       │ **مؤكَّد ببحث شامل**: `grep` عن                    │             │ verif.) │            │ remaining
       │ `allowedBranches`/`DataScopeFilter`/`BranchScope`  │             │         │            │ DataScope
       │ خارج الملفات الثلاثة التي تُصدِر الـ claim نفسه     │             │         │            │ gap
       │ (`AuthService`، `JwtService`،                       │             │         │            │
       │ `SecRoleBranchRepository`) أعاد **صفر نتائج**.       │             │         │            │
       │ **أكبر فجوة متبقية في ميزة DataScope ككل** —        │             │         │            │
       │ الـ Backend الحالي لا يُقيِّد أي استعلام ببيانات      │             │         │            │
       │ حسب فرع المستخدم. **قرار مطلوب**: هل هذا مقصود       │             │         │            │
       │ (المرحلة الحالية = بنية تحتية DataScope فقط، بلا      │             │         │            │
       │ إنفاذ بعد)، أم فجوة تنفيذ حرجة تحتاج معالجة قبل        │             │         │            │
       │ أي استخدام إنتاجي فعلي لهذه البيانات؟                  │             │         │            │
───────┼────────────────────────────────────────┼─────────────┼─────────┼────────────┼───────────────
OQ-016 │ **[جديد 2026-08-23]** تعارض حالة Conflict #20    │ **CLOSED**  │ 2026-08 │ 2026-08-23 │ Code
       │ في master-registry.md. **✅ محلولة بالكامل         │ (confirmed, │ -23     │ (found     │ investigation
       │ 2026-08-23** — backend.zip يحتوي فعلياً نسخة        │ definitive) │         │ master-    │ (master-
       │ `governance/master-registry.md` **v2.10.0** —       │             │         │ registry.md│ registry.md
       │ **نفس النسخة التي رُوجِعت في 2026-07-22 حرفياً.**     │             │         │ inside     │ was inside
       │ القراءة المباشرة تؤكِّد صراحةً (سطر 1109، وقسم         │             │         │ backend.zip│ backend.zip
       │ Conflict #20 كاملاً): **"Conflict #20 CLOSED"** —     │             │         │ itself)    │ all along)
       │ نصّ القرار حرفياً: "BLK-SEC-002 / Conflict #20 —       │             │         │            │
       │ RESOLVED AND CLOSED 2026-07-11 ... the apparent        │             │         │            │
       │ Security↔NotificationService circular dependency        │             │         │            │
       │ is resolved because the two directions are different    │             │         │            │
       │ dependency types, not a true cycle — NotificationService │             │         │            │
       │ →Security is HARD-FK (build-order), while Security→      │             │         │            │
       │ NotificationService (Forgot Password) is Event-Based       │             │         │            │
       │ (publish-only, no build-order coupling)... Security's       │             │         │            │
       │ extension scope is no longer blocked on this item."         │             │         │            │
       │ **إذن**: التوثيق الأصلي لهذه الوثيقة (2026-07-22) كان        │             │         │            │
       │ **صحيحاً**. ادّعاء `security-registry.md` بأن Conflict #20   │             │         │            │
       │ لا يزال OPEN **خاطئ/قديم** — أضيف كمثال ثالث للحاشية          │             │         │            │
       │ التصحيحية أسفل. **لا حاجة لإعادة رفع master-registry.md** —   │             │         │            │
       │ كان متوفراً بالفعل ضمن backend.zip طوال الوقت.                 │             │         │            │
─────────────────────────────────────────────────────────────────────
**16 سؤالاً إجمالاً عبر ثلاث جولات (2026-07-22 ×2، 2026-08-23 ×1). 14 CLOSED، 2 OPEN (OQ-014، OQ-015) بانتظار قرار Architect.**

Breakdown (الجولة الأخيرة 2026-08-23):
- **OQ-003 أُعيدت إجابتها بالكامل** (لا تناقض — تطوُّر كود حقيقي بين الجولتين؛ الفجوة أُغلِقت فعلياً في الكود، لا في هذه الوثيقة فقط)
- **RULE-SEC-031 GAP-SEC-02 مُعاد إغلاقها** بدليل مباشر (`AuthEventListener` + `NotificationClient`)
- **RULE-SEC-053 جديدة** (حساب خدمة `svc-notification` — XM-SEC-005)
- **RULE-SEC-050 مُصحَّحة** (4 نقاط محمية لا نقطة واحدة)
- **API-SEC-021 مُصحَّح + API-SEC-050 جديد** (activate/deactivate منفصلان، لا toggle-active)
- **OQ-016 CLOSED فوراً** — backend.zip يحتوي `governance/master-registry.md` v2.10.0 نفسها (نفس النسخة المُراجَعة 2026-07-22)؛ القراءة المباشرة تؤكِّد Conflict #20 CLOSED فعلاً — التوثيق الأصلي كان صحيحاً، `security-registry.md` هو من يحمل الادّعاء القديم/الخاطئ
- **فجوتان متبقيتان فقط**: OQ-014 (لا تنظيف لرموز التفعيل/الاستعادة)، **OQ-015 (DataScope claim غير مُستهلَك — الأهم والأخطر، يحتاج قرار Architect عاجل)**

⚠ **حاشية تصحيحية على `security-registry.md`** (الملف الموجود ضمن `governance/modules/SECURITY/` في backend.zip): عند التحقق المباشر من backend.zip، ثبت أن عدة ادّعاءات في هذا الملف **غير دقيقة** لهذه اللقطة الزمنية من الكود:
- ادّعاؤه "`RULE-SEC-037 not found in code`" **خاطئ** — موجودة فعلاً في `AuthService.java:216-241` و`JwtService.java`.
- ادّعاؤه "DataScope endpoints have no permission gate" **خاطئ** — `SecUserProfileService`/`SecRoleBranchService` يحملان `@PreAuthorize` بصلاحيات محدَّدة (`USER_PROFILE_*`/`ROLE_*`)، مطابقاً تماماً لما وثَّقته هذه الوثيقة أصلاً في SCR-SEC-006/007.
- ادّعاؤه "`/forgot-password` غير محمي بـ rate limiting" **خاطئ** — `LoginRateLimitFilter.java:48` يحمي هذا المسار صراحة.
- ادّعاؤه أن Conflict #20 لا يزال OPEN **خاطئ** — نسخة master-registry.md v2.10.0 نفسها (الموجودة داخل نفس backend.zip) تنص صراحة أنه CLOSED منذ 2026-07-11.
- بالمقابل، ادّعاؤه عن غياب مهمة تنظيف رموز التفعيل/الاستعادة (→ OQ-014) **تحقَّق مباشرة وصحيح**. **الدرس**: `security-registry.md` مصدر ثانوي غير موثوق بالكامل لهذه اللقطة — backend.zip (الكود نفسه) هو المرجع الأعلى سلطة عند أي تعارض، تماشياً مع HR-1.

Cross-reference: master-registry.md AQ-006/AQ-007 (registry version-
citation mismatch for SEC_USER_PROFILE/SEC_ROLE_BRANCH) remain open in
the master registry itself (outside this SRS's authority to close) —
unaffected by this closure.
```

---
*نهاية الوثيقة | End of srs.md*
*Governed by: SRS Governance Engine (Project 1)*
*Feature Code: SEC-001 | Version: 1.2*
*Structure: PART A (Module Foundation) + PART B (Screen Specifications)*
*Governance State: EXCEPTION (core) + PARTIALLY_READY (extension scope) — registry-verified against master-registry.md v2.10.0 (confirmed twice: 2026-07-22 review + 2026-08-23 re-confirmation via backend.zip's own bundled copy), code-investigated against backend.zip (2026-07-22 AND 2026-08-23), OQ Log at 14 CLOSED + 2 OPEN (16 total) as of 2026-08-23. Remaining opens: OQ-014 (token cleanup gap), OQ-015 (DataScope enforcement gap — highest priority).*
*Next Mode: MODE 1.5 — Database Governance Engine (Project 2) — treat*
*schema as ALREADY EXISTING (AS-BUILT), not new DDL design. Recommend*
*re-attaching a current master-registry.md before MODE 1.5 to resolve*
*OQ-016 (Conflict #20 status discrepancy).*
