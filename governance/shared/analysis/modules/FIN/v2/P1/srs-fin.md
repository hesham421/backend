# SRS — الحسابات العامة / Finance (General Ledger) (FIN) — DELTA v2
══════════════════════════════════════════════════════════════════
Module : FIN   Version : v2 (delta on v1 — change set CS-FIN-001)   Profile : erp
Inputs : prd, domain-profile, project-registry, change-manifest (PRD v2 approved 2026-09-23 by hesham421)
Counts : REQ 77 · AC 83 · ENT 16 · RULE 28 · SCR-REQ 13 · ADR 8 (this stage)   (current state, v1 + v2)
Delta  : ADDED — ENT 2 · REQ 31 · AC 37 · RULE 11 · SCR-REQ 1 · ADR 8
         MODIFIED — ENT-FIN-007, ENT-FIN-008 (A3 adaptation note only, pass-1) · ENT-FIN-010 · REQ-FIN-045 · AC-FIN-045 · RULE-FIN-004 · RULE-FIN-008 (wording only, pass-1) · SCR-REQ-FIN-003 · SCR-REQ-FIN-006
         REMOVED — none (the lookup value DIRECT leaves ACCOUNT_DERIVATION_TYPE; no ID is retired — ADR-FIN-010)
══════════════════════════════════════════════════════════════════

Delta reading note: this file re-emits only the ADDED and MODIFIED records. Each v1 record not
re-emitted here stays in force unchanged and is carried into `_state/current-srs.md` by `gov.py state`.
The sections that carry no ID (A1, A2, A3 preamble, A6, A7, A8, STANDALONE) replace their v1
counterparts in the folded state, so they are restated in full: v1 content unchanged, v2 content marked.

# PART A — MODULE FOUNDATION

## A1 — Document information
| Item | Value |
|---|---|
| Module | FIN — الحسابات العامة / Finance (General Ledger) |
| Feature code | FIN |
| Version | v2 (delta — CS-FIN-001, ADDITIVE) |
| Date | 2026-09-23 |
| Status | DRAFT (P1) |
| Prepared by | governance-factory (analysis lane) |
| Decisions applied count | 8 new ADRs (ADR-FIN-023 … ADR-FIN-027, ADR-FIN-037 … ADR-FIN-039 — the last three raised by this pass-1 revision) + 7 upstream v2 ADRs applied (ADR-FIN-010 … ADR-FIN-014, ADR-FIN-020, ADR-FIN-021) + 5 DEFAULTs (STANDALONE → Decisions applied) |

## A2 — Functional context

**In scope (v1, unchanged):** شجرة حسابات هرمية بأبعاد كبيانات، محرك قواعد يبني القيد من الحدث المحاسبي
القياسي، مصادر يومية أربعة (حدث/يدوي/متكرر-عكسي/توزيع) بدورة حياة موحدة (بناء → تحقق آلي →
ترحيل مباشر، لا اعتماد لكل قيد)، تصحيح بالعكس فقط، دورة حياة فترة محاسبية داخلية بالكامل
مع بوابة اعتماد واحدة عند الإغلاق، تقارير مالية مُشتقة حيًا مع تتبع نازل، تسجيل FIN في SEC
وMDL كبيانات.

**In scope (v2 — CS-FIN-001):** بناء قيد اليومية وترحيله من حدث محاسبي قياسي يصدره النظام القديم (Oracle)
كي يُوقَف الحساب في النظام القديم، في أربعة محاور لا يعيد أيٌّ منها فتح ثابت من ثوابت v1:
(1) اشتقاق الحساب بالربط — مخزن روابط حسابات تملكه FIN وتديره شاشة FIN كبيانات، يحوّل قيمة تجارية يحملها
الحدث (طريقة الدفع، رمز نوع المصروف) إلى حساب، مع حذف نمط DIRECT نهائيًا؛ (2) وسوم الأبعاد على سطور القاعدة —
قيمة كل بُعد إما ثابتة في القاعدة وإما مأخوذة من حقل تجاري مسمّى في الحدث، تخضع لتحقق الأبعاد القائم؛
(3) جواب الحدث المكرر دائمًا بخطأ الحدث المكرر؛ (4) رمز مميز موثَّق لرفض الفترة غير المفتوحة.

**Out of scope:** تعدد العملات، تعدد الدفاتر/الكيانات والقيود البينية، الحسابات الإحصائية،
التقويم متعدد الأنماط، المرفقات — استبعاد صريح [business-policies-fin.md → SCOPE
EXCEPTIONS]؛ الوحدة التجارية، مستهلك الأحداث، طبقة النقل — خارج النطاق تمامًا
[general-accounting-system-plan-en.md §15]. v2 يضيف: الطابور داخل قاعدة البيانات وخدمة المستهلك وكل ما داخل
Oracle القديم؛ تعبئة الروابط الفعلية (بيانات ترحيل)؛ مصدر أحداث ثانٍ أو وسيط رسائل؛ مصادقة المستدعي الآلي
(دلتا SEC منفصلة)؛ ترجمة رموز المؤسسة/الفرع القديمة إلى قيم أبعاد FIN (US-FIN-033 — DEFERRED، ADR-FIN-012).

**Module function (one paragraph):** FIN دفتر أستاذ عام قابل للتوصيل بأي نظام مضيف: يستقبل
حدثًا محاسبيًا قياسيًا فقط (لا قراءة ولا كتابة مباشرة لجداول المضيف)، يبني منه قيدًا متوازنًا
عبر قاعدة مُعرَّفة كبيانات، يرحّله مباشرة بعد تحقق آلي، ويشتق كل رصيد وتقرير من القيود
المُرحَّلة فقط — لا عمود رصيد مُخزَّن في أي مكان. في v2: الحدث يصرّح بوقائع تجارية فقط، وFIN وحدها تقرر
معناها المحاسبي (الحساب والأبعاد) — لا رمز حساب ولا مفهوم محاسبي يعبر داخل حمولة الحدث [POL-FIN-021].

**Detailed description (workflow narrative, roles):** حدث محاسبي يصل → المحرك يحدد قاعدته
عبر نوع الحدث → يبني سطور القيد (اشتقاق حساب + مصدر مبلغ + اتجاه لكل سطر، مع سطر باقٍ ضامن
عند التوزيع النسبي) → تحقق آلي (توازن، أوراق نشطة قابلة للترحيل، أبعاد صالحة، فترة مفتوحة)
→ ترحيل مباشر. محاسب يُدخل قيودًا يدوية بنفس المسار. مراقب مالي (منفصل عن منشئ القيود) يعتمد
إغلاق الفترة فقط — نقطة التحكم البشري الوحيدة. كل التقارير تُشتق حيًا من القيود المُرحَّلة.
إضافة v2 داخل خطوة بناء السطور: لسطر اشتقاقه MAPPING يقرأ المحرك قيمة الحقل التجاري المسمّى من الحدث ويبحث عن
الربط النشط لـ (نوع الحدث، الحقل التجاري، القيمة) فيأخذ حسابه؛ ولكل وسم بُعد على سطر القاعدة يضبط المحرك قيمة
البُعد من الثابت أو من الحقل التجاري؛ ثم يمر كل ذلك بالتحقق الآلي نفسه دون تجاوز. غياب الربط أو الحقل أو قيمة
البُعد يرفض الحدث كله ولا يُرحَّل أي قيد — لا قيمة افتراضية ولا تخمين. مسؤول المالية يدير الروابط عبر شاشة
روابط الحسابات. مشغّل تكامل الأحداث يقرأ رموز الرفض: المكرر = نجاح، الفترة غير المفتوحة = انتظار وإعادة
محاولة، وما سواهما = فشل يحتاج إصلاحًا.

**Current situation:** v1: لا نظام محاسبي سابق ضمن هذه الدفعة؛ FIN أول وحدة أعمال حقيقية تُبنى،
تستهلك SEC وMDL المكتملتين بالفعل. v2: النظام القديم Oracle ما زال يرحّل محاسبته بنفسه؛ يختار حساب النقد
ويستبدله بحساب البنك حين يكون الدفع بشيك (11 من 16 فرعًا حيًا)، ويختار حساب المصروف حسب نوع المصروف، ويختم
المؤسسة والفرع على كل قيد.

**Current difficulties:** الأنظمة القديمة المشابهة تعتمد على أعمدة رصيد مُخزَّنة تفقد تزامنها —
بالضبط ما يحله POL-FIN-009. في v2: محرك FIN v1 لا يعرف إلا CONSTANT (لا يتغير) وDIRECT (يُدخل رمز حساب في
الحمولة ويكسر الحد)، وMAPPING مُصرَّح به ويرفض التنفيذ؛ سطور القيد المبنية من الأحداث بلا أبعاد، فتتباعد
المقارنة المتوازية في كل صف وتفرغ تقارير الأبعاد؛ الحدث المكرر المتزامن يظهر خطأً عامًا من فئة 500 فيُعاد
إرساله وينتهي في طابور الرسائل الميتة.

**Proposed system and benefits:** محرك قواعد بيانات-محور يفصل منطق الحساب عن مصدر المبلغ،
دورة حياة قيد موحدة بلا اعتماد فردي يُسرّع التشغيل مع نقل الرقابة لمستوى الفترة، وأرصدة
مُشتقة دائمًا تضمن التطابق التام مع القيود الفعلية. v2: منطق اختيار الحساب القديم يصبح صفوف ربط صريحة
قابلة للتعديل دون إصدار، والقيود المبنية من الأحداث تحمل الإسناد نفسه الذي يحمله القيد اليدوي، والمستهلك يميّز
المكرر والفترة المغلقة عن الفشل الحقيقي — فيصبح التحقق في وضع الظل ذا معنى ويمكن إيقاف محاسبة النظام القديم.

**General notes (constraints, deferred items):** لا محرك سير عمل؛ لا رقم حساب داخل حمولة
الحدث؛ الالتزام بحدود idempotency عند الحد الخارجي مفترض لا مُعاد تنفيذه [§12.12]. v2: الحدث يسمّي كل واقعة
تجارية برمز من اللوكب FIN_EVENT_BUSINESS_FIELD (ADR-FIN-013)؛ ترجمة رموز الأبعاد مؤجلة (US-FIN-033)؛ تعبئة
الروابط بيانات ترحيل تُنتَج بعد تسليم المخزن؛ كل رفض جديد خطأ مميز يسمّي P3.1 رمزه، ويبقى رمزا الحدث المكرر
والفترة غير المفتوحة في v1 كما هما (ADR-FIN-027). كل مثال في هذا القسم يحمل الوقائع التجارية
داخل خريطة الحدث `fields`، مفتاحًا برمز FIN_EVENT_BUSINESS_FIELD (مثل `fields.PAYMENT_METHOD`)،
لا حقلاً مباشرًا باسم الواقعة نفسها — الناقل الذي يثبّته ADR-FIN-031 (ADR-FIN-037).

## A3 — Entities and fields

Standard fields per kind (profile.conventions.entity_defaults): master → nameAr, nameEn,
code, isActiveFl, createdBy, createdAt, updatedBy, updatedAt; transactional → docNo,
docDate, statusCode, fiscalYearId, periodId, createdBy, createdAt, updatedBy, updatedAt;
lookup → code, nameAr, nameEn, sortOrder, isActiveFl; config → key, valueAr, valueEn,
isActiveFl (adapted per entity below where the plan's own vocabulary differs, e.g. `code`
in place of a generic `key`, cited per field).

v2 changes to this section: ENT-FIN-015 and ENT-FIN-016 ADDED; ENT-FIN-010 MODIFIED (re-emitted below).
ENT-FIN-006 JournalLineDimension is unchanged in structure. In v2 the rules engine also writes it, for
event-built lines, in exactly the form a manual line uses (REQ-FIN-070). Its v1 record stands.

Adaptation of the master `entity_defaults` (ENT-FIN-007, ENT-FIN-008 — v1 debt, adapted here on the
folded state, ADR-FIN-038): FiscalYear's `code` (DBF-FIN-066) IS its label — a year is identified and
displayed by that one code, so it carries no separate `nameAr`/`nameEn`. FiscalPeriod's key is
`periodNo` together with its `fiscalYearId`, not a business `code`; its state lives in `statusCode`
(PERIOD_STATE), not in a master `isActiveFl`. Neither entity's schema changes — this records the
adaptation the profile's own defaults already allow, per entity, the way v2 records it for
ENT-FIN-015/016.

### ENT-FIN-010 — سطر القاعدة / RuleLine
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| config | PRIVATE | No | create (with rule, with its dimension tags), read, update, delete | none | general-accounting-system-plan-en.md §6.2, §6.3; CS-FIN-001 §1, §2 |

MODIFIED in v2: `accountDerivationTypeCode` loses DIRECT (ADR-FIN-010); `accountDerivationValue` narrows to
the constant account code; `accountBusinessFieldCode` ADDED (ADR-FIN-023); dimension tags hang off the
line as ENT-FIN-016 (ADR-FIN-025). The update/delete named under Operations are v1's wording; the
as-built record at SCR-REQ-FIN-003 §B4 (no line update, no line delete) is unchanged.

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| ruleLineId | number | yes (system) | — | primary key | معرّف سطر القاعدة | Rule line id |
| eventTypeRuleId | reference | yes | ENT-FIN-009 | — | قاعدة الحدث | Event type rule |
| lineNo | number | yes | — | — | رقم السطر | Line number |
| accountDerivationTypeCode | lookup | yes | lookup key `ACCOUNT_DERIVATION_TYPE` (A6) — CONSTANT / MAPPING | DIRECT removed in v2 [RULE-FIN-018, ADR-FIN-010] | نوع اشتقاق الحساب | Account derivation type |
| accountDerivationValue | text | when CONSTANT | the constant account code; empty when MAPPING [RULE-FIN-019] | v1 "event field name / mapping-set key" readings dropped (ADR-FIN-023) | قيمة الاشتقاق (رمز الحساب الثابت) | Derivation value (constant account code) |
| accountBusinessFieldCode | lookup | when MAPPING | lookup key `FIN_EVENT_BUSINESS_FIELD` (A6); empty when CONSTANT [RULE-FIN-019] | ADDED v2 — the business field whose value in the event selects the account through ENT-FIN-015 | الحقل التجاري لربط الحساب | Account mapping business field |
| amountSourceTypeCode | lookup | yes | lookup key `AMOUNT_SOURCE_TYPE` (A6) | field / percentage / remainder [§6.2(b)] | نوع مصدر المبلغ | Amount source type |
| amountSourceValue | text | no | event field name or percentage figure | required unless amountSourceTypeCode=REMAINDER | قيمة مصدر المبلغ | Amount source value |
| directionCode | lookup | yes | lookup key `DEBIT_CREDIT` (A6) | — | الاتجاه | Direction |
| distributionTypeCode | lookup | yes | lookup key `DISTRIBUTION_TYPE` (A6) | fixed / percentage / remainder [§6.3] | نوع التوزيع | Distribution type |
| isRemainderFl | flag | yes | default false | exactly one true per rule when any line is PERCENTAGE [RULE-FIN-003] | سطر الباقي | Remainder line |
| createdAt | system | yes | — | — | — | — |

### ENT-FIN-015 — ربط الحساب / AccountMapping
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| config | PRIVATE | No — a mapping is identified by its key (event type, business field, business value); nothing outside the system quotes it [§3.3 test] | create, read (search), update (account only), deactivate | lookup codes read from MDL (as every FIN lookup field) | CS-FIN-001 §1; module-registry-fin.md v2 ENTITIES OWNED — ADDED; POL-FIN-022, POL-FIN-025 |

Config defaults adapted: `key` → the natural key (eventTypeCode, businessFieldCode, businessValue), unique
among active rows [RULE-FIN-022]; `valueAr`/`valueEn` → not stored: the bilingual label shown for a
mapping is the referenced account's `nameAr`/`nameEn` (single source, ARCH-2); `isActiveFl` kept. Audit
fields per FIN's existing conventions (CS-FIN-001 §1).

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| accountMappingPk | number | yes (system) | — | primary key | معرّف الربط | Mapping id |
| eventTypeCode | lookup | yes | lookup key `ACCOUNTING_EVENT_TYPE` (A6) | the event type the mapping serves; fixed after create (ADR-FIN-024) | نوع الحدث | Event type |
| businessFieldCode | lookup | yes | lookup key `FIN_EVENT_BUSINESS_FIELD` (A6) | the business field being mapped; fixed after create | الحقل التجاري | Business field |
| businessValue | text | yes | when businessFieldCode=PAYMENT_METHOD: an active `PAYMENT_METHOD` code [RULE-FIN-024]; otherwise the host's code stored as-is | stored as a code, never a display text (module-registry AUTO-DECISION); compared by exact code, leading and trailing blanks trimmed on save and on event read (ADR-FIN-039); fixed after create | القيمة التجارية | Business value |
| accountId | reference | yes | ENT-FIN-001; active leaf on save [RULE-FIN-023] | the only field an update changes (ADR-FIN-024) | الحساب | Account |
| isActiveFl | flag | yes | default true | deactivated, never hard-deleted [KB:erp-domain-standards §6] | نشط | Active |
| createdBy, createdAt, updatedBy, updatedAt | system | yes | — | standard audit fields | — | — |

### ENT-FIN-016 — بُعد سطر القاعدة / RuleLineDimension
| Kind | Ownership | Business number | Operations | Cross-module | Source |
|---|---|---|---|---|---|
| config | PRIVATE | No | create (with its rule line), read (with its rule line) — ADR-FIN-025 | lookup codes read from MDL | CS-FIN-001 §2; module-registry-fin.md v2 ENTITIES OWNED — ADDED; POL-FIN-026 |

Config defaults adapted: `key` → (ruleLineId, dimensionId), one tag per dimension per line [RULE-FIN-025];
`valueAr`/`valueEn` → not stored, the labels come from ENT-FIN-002 / ENT-FIN-003; `isActiveFl` → not
carried, the tag lives and retires with its rule line (ADR-FIN-025, same as ENT-FIN-010).

| Field | Logical type | Required | Values / source | Notes | Label-ar | Label-en |
|---|---|---|---|---|---|---|
| ruleLineDimensionPk | number | yes (system) | — | primary key | معرّف وسم البُعد | Dimension tag id |
| ruleLineId | reference | yes | ENT-FIN-010 | — | سطر القاعدة | Rule line |
| dimensionId | reference | yes | ENT-FIN-002 | unique per ruleLineId [RULE-FIN-025] | البُعد | Dimension |
| valueSourceCode | lookup | yes | lookup key `FIN_DIMENSION_VALUE_SOURCE` (A6) — CONSTANT / BUSINESS_FIELD | ADR-FIN-010 (dimensions keep their own source list) | مصدر القيمة | Value source |
| dimensionValueId | reference | when CONSTANT | ENT-FIN-003; must belong to dimensionId; empty when BUSINESS_FIELD [RULE-FIN-026] | — | القيمة الثابتة | Constant value |
| businessFieldCode | lookup | when BUSINESS_FIELD | lookup key `FIN_EVENT_BUSINESS_FIELD` (A6); empty when CONSTANT [RULE-FIN-026] | the event's value resolves by `DimensionValue.code` within dimensionId (ADR-FIN-026) | الحقل التجاري | Business field |
| createdAt | system | yes | — | written with the rule line | — | — |

## A4 — Functional requirements (EARS) and acceptance criteria

### REQ-FIN-045 — تسجيل قوائم FIN في MDL / Register FIN's lookup types into the Lookup module
Pattern    : event
Statement  : When FIN is deployed, the system shall register its 15 lookup types and their seeded values as data into the Lookup module, naming FIN as owner.
Traces     : US-FIN-018, US-FIN-020, US-FIN-021, US-FIN-026
Entities   : ENT-FIN-001, ENT-FIN-004, ENT-FIN-007, ENT-FIN-008, ENT-FIN-009, ENT-FIN-010, ENT-FIN-011, ENT-FIN-013, ENT-FIN-015, ENT-FIN-016
Rationale  : POL-FIN-017; MODIFIED v2 — 13 → 15 types (FIN_EVENT_BUSINESS_FIELD, FIN_DIMENSION_VALUE_SOURCE), DIRECT leaves ACCOUNT_DERIVATION_TYPE, PAYMENT_METHOD gains CHEQUE
Source     : general-accounting-system-plan-en.md §5.2-§5.3; module-registry-fin.md v2 LOOKUPS OWNED; ADR-FIN-010, ADR-FIN-013
Priority   : HIGH
#### AC-FIN-045 — [REQ-FIN-045]
Given  : MDL is gated and reachable, and FIN v2 is deployed
When   : FIN's onboarding and its v2 migration have run
Then   : MDL records 15 LookupType rows owned by FIN; ACCOUNT_DERIVATION_TYPE holds exactly 2 active values, CONSTANT and MAPPING; FIN_EVENT_BUSINESS_FIELD holds 4 values; FIN_DIMENSION_VALUE_SOURCE holds 2 values; PAYMENT_METHOD holds the value CHEQUE

### REQ-FIN-047 — لا رمز حساب من حمولة الحدث / No account code from the event payload
Pattern    : ubiquitous
Statement  : The system shall take the account of every journal line it builds from a canonical accounting event from the rule line's constant account code or from an account mapping, never from an account code carried in the event payload.
Traces     : US-FIN-020
Entities   : ENT-FIN-005, ENT-FIN-010, ENT-FIN-015
Rationale  : POL-FIN-021 — the event states a business fact; FIN alone decides its accounting meaning
Source     : CS-FIN-001 Summary, §1, acceptance 1; domain-profile G6; ADR-FIN-010
Priority   : HIGH
#### AC-FIN-047 — [REQ-FIN-047]
Given  : an active rule for event type "PAYMENT_RECEIVED" whose debit line is MAPPING on PAYMENT_METHOD, an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102, and an event "EVT-3001" that carries fields.PAYMENT_METHOD = "CHEQUE" plus an extra payload field accountCode "1101"
When   : the event arrives
Then   : the system posts the entry with its debit line on account 1102, and no line of the entry carries account 1101

### REQ-FIN-048 — رفض نوع اشتقاق غير مدعوم / Reject an unsupported account derivation type
Pattern    : unwanted
Statement  : If a finance administrator adds a rule line whose account derivation type is not an active ACCOUNT_DERIVATION_TYPE value, then the system shall reject the rule line.
Traces     : US-FIN-020
Entities   : ENT-FIN-010
Rationale  : RULE-FIN-018; DIRECT is removed outright (ADR-FIN-010) — no path left for an account code to enter through the payload
Source     : CS-FIN-001 §1 "DIRECT … breaks the boundary"; POL-FIN-021
Priority   : HIGH
#### AC-FIN-048 — [REQ-FIN-048]
Given  : ACCOUNT_DERIVATION_TYPE holds CONSTANT and MAPPING only
When   : a finance administrator adds a rule line with accountDerivationTypeCode=DIRECT
Then   : the system rejects the line with ar: "نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط" · en: "Unsupported account derivation type; only constant or mapping is allowed", and no RuleLine row is created

### REQ-FIN-049 — سطر قاعدة يشتق حسابه بالربط / A rule line that derives its account by mapping
Pattern    : event
Statement  : When a finance administrator adds a rule line whose account derivation type is MAPPING, the system shall record the business field whose value in the event selects the line's account.
Traces     : US-FIN-021, US-FIN-022
Entities   : ENT-FIN-010
Rationale  : RULE-FIN-019; POL-FIN-022 — the line names the fact, the mapping store names the account
Source     : CS-FIN-001 §1; ADR-FIN-023
Priority   : HIGH
#### AC-FIN-049 — [REQ-FIN-049]
Given  : an active event-type rule for "EXPENSE_PAID"
When   : a finance administrator adds a line with accountDerivationTypeCode=MAPPING, accountBusinessFieldCode=EXPENSE_TYPE_CODE and no accountDerivationValue
Then   : the system creates the RuleLine with accountBusinessFieldCode=EXPENSE_TYPE_CODE

### REQ-FIN-050 — اشتقاق الحساب من الربط عند البناء / Resolve the account through the mapping at build time
Pattern    : event
Statement  : When the system builds a journal line from a rule line whose account derivation type is MAPPING, the system shall take the line's account from the active account mapping that matches the rule's event type, the rule line's business field and that field's value in the event.
Traces     : US-FIN-020, US-FIN-021, US-FIN-022
Entities   : ENT-FIN-005, ENT-FIN-009, ENT-FIN-010, ENT-FIN-015
Rationale  : RULE-FIN-020; POL-FIN-022 — cash versus bank by payment method, expense account by expense type
Source     : CS-FIN-001 §1, acceptance 1
Priority   : HIGH
#### AC-FIN-050 — [REQ-FIN-050]
Given  : active mappings (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH") → account 1101 (cash) and (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102 (bank), and a rule for PAYMENT_RECEIVED whose debit line is MAPPING on PAYMENT_METHOD
When   : event "EVT-3002" of type PAYMENT_RECEIVED arrives with fields.PAYMENT_METHOD = "CHEQUE" and amount 500.00
Then   : the system posts an entry whose debit line is 500.00 on account 1102
#### AC-FIN-051 — [REQ-FIN-050]
Given  : the same mappings and rule as AC-FIN-050
When   : event "EVT-3003" of type PAYMENT_RECEIVED arrives with fields.PAYMENT_METHOD = "CASH" and amount 500.00
Then   : the system posts an entry whose debit line is 500.00 on account 1101
#### AC-FIN-052 — [REQ-FIN-050]
Given  : active mappings (EXPENSE_PAID, EXPENSE_TYPE_CODE, "TRAVEL") → account 5210 and (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT") → account 5220, and a rule for EXPENSE_PAID whose debit line is MAPPING on EXPENSE_TYPE_CODE
When   : event "EVT-3004" of type EXPENSE_PAID arrives with fields.EXPENSE_TYPE_CODE = "RENT" and amount 1200.00
Then   : the system posts an entry whose debit line is 1200.00 on account 5220

### REQ-FIN-051 — رفض حدث بلا الحقل التجاري المطلوب للحساب / Reject an event missing the business field its account needs
Pattern    : unwanted
Statement  : If a canonical accounting event does not carry the business field that a MAPPING rule line names, then the system shall reject the event and post no journal entry.
Traces     : US-FIN-021, US-FIN-022, US-FIN-024
Entities   : ENT-FIN-004, ENT-FIN-010
Rationale  : RULE-FIN-021; never a default, never a guess — a missing fact is not "cash"
Source     : CS-FIN-001 §1 "never a default, never a guess"; POL-FIN-024; ADR-FIN-027
Priority   : HIGH
#### AC-FIN-053 — [REQ-FIN-051]
Given  : the PAYMENT_RECEIVED rule of AC-FIN-050
When   : event "EVT-3005" of type PAYMENT_RECEIVED arrives with no fields.PAYMENT_METHOD key
Then   : the system rejects the event with the missing-business-field error, ar: "الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة" · en: "The event does not carry the business field the rule requires", and no JournalEntry exists with eventReference "EVT-3005"

### REQ-FIN-052 — رفض حساب مربوط غير مؤهَّل للترحيل / Reject a mapped account that cannot be posted to
Pattern    : unwanted
Statement  : If an account taken from an account mapping is not a leaf, not active, or not marked as accepting direct posting, then the system shall reject the event and post no journal entry.
Traces     : US-FIN-023
Entities   : ENT-FIN-001, ENT-FIN-005, ENT-FIN-015
Rationale  : RULE-FIN-007 applied unchanged to the mapped account; POL-FIN-023 — the new path opens no gap in the chart's integrity
Source     : CS-FIN-001 §1 "validates the resulting account exactly as it validates a CONSTANT one — leaf, active, postable"
Priority   : HIGH
#### AC-FIN-054 — [REQ-FIN-052]
Given  : mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102, and account 1102 has since received a child account (isLeafFl=false)
When   : event "EVT-3006" of type PAYMENT_RECEIVED arrives with fields.PAYMENT_METHOD = "CHEQUE"
Then   : the system rejects the event with ar: "الحساب المستهدف لا يقبل ترحيلاً مباشرًا" · en: "The target account does not accept direct posting", and no JournalEntry exists with eventReference "EVT-3006"
#### AC-FIN-055 — [REQ-FIN-052]
Given  : mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102, and account 1102 has since been deactivated (isActiveFl=false)
When   : event "EVT-3007" of type PAYMENT_RECEIVED arrives with fields.PAYMENT_METHOD = "CHEQUE"
Then   : the system rejects the event with the same message as AC-FIN-054, and no JournalEntry exists with eventReference "EVT-3007"

### REQ-FIN-053 — رفض القيمة التجارية غير المربوطة / Reject an unmapped business value
Pattern    : unwanted
Statement  : If the business value in a canonical accounting event has no active account mapping for the rule's event type and the rule line's business field, then the system shall reject the event with the dedicated unmapped-value error and post no journal entry.
Traces     : US-FIN-024
Entities   : ENT-FIN-004, ENT-FIN-010, ENT-FIN-015
Rationale  : RULE-FIN-020; POL-FIN-024 — never a default, never a guess; the missing mapping is found and completed rather than an entry posting to a wrong account
Source     : CS-FIN-001 §1, acceptance 2; ADR-FIN-027
Priority   : HIGH
#### AC-FIN-056 — [REQ-FIN-053]
Given  : the PAYMENT_RECEIVED rule and mappings of AC-FIN-050, and no mapping for the PAYMENT_METHOD value "TRANSFER"
When   : event "EVT-3008" of type PAYMENT_RECEIVED arrives with fields.PAYMENT_METHOD = "TRANSFER"
Then   : the system rejects the event with the unmapped-value error, ar: "لا يوجد ربط حساب نشط للقيمة التجارية في الحدث" · en: "No active account mapping exists for the business value in the event", and no JournalEntry exists with eventReference "EVT-3008"
#### AC-FIN-057 — [REQ-FIN-053]
Given  : mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH") → account 1101 with isActiveFl=false, and no other mapping for "CASH"
When   : event "EVT-3009" of type PAYMENT_RECEIVED arrives with fields.PAYMENT_METHOD = "CASH"
Then   : the system rejects the event with the unmapped-value error of AC-FIN-056, and no JournalEntry exists with eventReference "EVT-3009"

### REQ-FIN-054 — إنشاء ربط حساب / Create an account mapping
Pattern    : event
Statement  : When a finance administrator creates an account mapping, the system shall record its event type, business field, business value and account as an active mapping.
Traces     : US-FIN-025
Entities   : ENT-FIN-015
Rationale  : POL-FIN-025 — mappings are FIN-owned data managed through a FIN screen, never code
Source     : CS-FIN-001 §1, acceptance 3
Priority   : HIGH
#### AC-FIN-058 — [REQ-FIN-054]
Given  : no active mapping for (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE"), and account 1102 is an active leaf
When   : a finance administrator submits the mapping form with those three values and account 1102
Then   : the system creates an AccountMapping with isActiveFl=true and accountId = account 1102

### REQ-FIN-055 — بحث روابط الحسابات / Search account mappings
Pattern    : event
Statement  : When a finance administrator searches account mappings, the system shall list the mappings that match the given event type, business field, business value, account and active-flag filters.
Traces     : US-FIN-025
Entities   : ENT-FIN-015
Rationale  : the administrator finds the mapping to change or deactivate, and checks coverage before the migration data is loaded
Source     : CS-FIN-001 §1; [KB:erp-domain-standards §6] server-side search and paging
Priority   : HIGH
#### AC-FIN-059 — [REQ-FIN-055]
Given  : 3 mappings for PAYMENT_RECEIVED (CASH active, CHEQUE active, TRANSFER inactive) and 2 for EXPENSE_PAID
When   : the administrator searches with eventTypeCode=PAYMENT_RECEIVED and isActiveFl=true
Then   : the system returns exactly 2 rows, CASH and CHEQUE

### REQ-FIN-056 — تغيير حساب الربط / Change a mapping's account
Pattern    : event
Statement  : When a finance administrator changes the account of an account mapping, the system shall record the new account on that mapping.
Traces     : US-FIN-025
Entities   : ENT-FIN-015
Rationale  : POL-FIN-025 — logic evolves without a release; the key stays fixed (ADR-FIN-024)
Source     : CS-FIN-001 §1, acceptance 3
Priority   : HIGH
#### AC-FIN-060 — [REQ-FIN-056]
Given  : mapping (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT") → account 5220, and account 5225 is an active leaf
When   : the administrator changes its account to 5225
Then   : the system updates the mapping to accountId = account 5225 and leaves its eventTypeCode, businessFieldCode and businessValue unchanged

### REQ-FIN-057 — إيقاف ربط حساب / Deactivate an account mapping
Pattern    : event
Statement  : When a finance administrator deactivates an account mapping, the system shall exclude it from every later account resolution.
Traces     : US-FIN-025
Entities   : ENT-FIN-015
Rationale  : soft delete [KB:erp-domain-standards §6]; a deactivated mapping resolves nothing, so an event needing it is rejected by REQ-FIN-053
Source     : CS-FIN-001 §1 "an active flag"; POL-FIN-025
Priority   : HIGH
#### AC-FIN-061 — [REQ-FIN-057]
Given  : an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CASH") → account 1101
When   : the administrator deactivates it
Then   : the system sets isActiveFl=false on the mapping, and the next PAYMENT_RECEIVED event with fields.PAYMENT_METHOD = "CASH" is rejected as in AC-FIN-057

### REQ-FIN-058 — تعديل الربط لا يمس القيود المُرحَّلة / A mapping change never alters a posted entry
Pattern    : event
Statement  : When a finance administrator changes or deactivates an account mapping, the system shall leave the account of every already-posted journal line unchanged.
Traces     : US-FIN-025
Entities   : ENT-FIN-005, ENT-FIN-015
Rationale  : POL-FIN-013 (immutable posted entries) and POL-FIN-009 (derived balances) hold on the new path; a mapping governs later derivations only (module-registry AUTO-DECISION, ADR-FIN-021)
Source     : CS-FIN-001 acceptance 7; US-FIN-025
Priority   : HIGH
#### AC-FIN-062 — [REQ-FIN-058]
Given  : entry "JV-2026-000500", posted from event "EVT-3010" with its debit line on account 5220 through mapping (EXPENSE_PAID, EXPENSE_TYPE_CODE, "RENT")
When   : the administrator changes that mapping's account to 5225
Then   : the debit line of "JV-2026-000500" still shows account 5220, and the account ledger of 5220 still contains that line

### REQ-FIN-059 — رفض ربط نشط مكرر / Reject a duplicate active mapping
Pattern    : unwanted
Statement  : If a finance administrator saves an account mapping whose event type, business field and business value already have an active mapping, then the system shall reject it.
Traces     : US-FIN-025
Entities   : ENT-FIN-015
Rationale  : RULE-FIN-022 — two candidate accounts for one fact would make the engine guess (module-registry AUTO-DECISION)
Source     : CS-FIN-001 §1 "never a guess"
Priority   : HIGH
#### AC-FIN-063 — [REQ-FIN-059]
Given  : an active mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1102
When   : the administrator creates a second mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQUE") → account 1103
Then   : the system rejects it with ar: "يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها" · en: "An active mapping already exists for this event type, business field and value", and the count of active mappings for that key stays 1

### REQ-FIN-060 — رفض ربط بحساب غير ورقي أو غير نشط / Reject a mapping to a non-leaf or inactive account
Pattern    : unwanted
Statement  : If a finance administrator saves an account mapping whose account is not an active leaf account, then the system shall reject it.
Traces     : US-FIN-025, US-FIN-023
Entities   : ENT-FIN-001, ENT-FIN-015
Rationale  : RULE-FIN-023 — the gap is caught at configuration, not first discovered as a failed event; the post-time check (REQ-FIN-052) still stands
Source     : POL-FIN-023; ADR-FIN-024
Priority   : MEDIUM
#### AC-FIN-064 — [REQ-FIN-060]
Given  : account 1100 is a parent (rollup) account with isLeafFl=false
When   : the administrator saves a mapping whose account is 1100
Then   : the system rejects it with ar: "الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا" · en: "The mapped account must be an active leaf account", and no AccountMapping row is written

### REQ-FIN-061 — رفض قيمة طريقة دفع غير معرَّفة / Reject an undefined payment-method value
Pattern    : unwanted
Statement  : If a finance administrator saves an account mapping for the PAYMENT_METHOD business field whose business value is not an active PAYMENT_METHOD code, then the system shall reject it.
Traces     : US-FIN-025, US-FIN-021
Entities   : ENT-FIN-015
Rationale  : RULE-FIN-024 — a misspelt payment method would never match an event and would silently leave the method unmapped
Source     : module-registry-fin.md v2 AUTO-DECISION (business value stored as a code; PAYMENT_METHOD → its lookup)
Priority   : MEDIUM
#### AC-FIN-065 — [REQ-FIN-061]
Given  : PAYMENT_METHOD holds CASH and CHEQUE as active values
When   : the administrator saves a mapping (PAYMENT_RECEIVED, PAYMENT_METHOD, "CHEQEU")
Then   : the system rejects it with ar: "القيمة ليست طريقة دفع معرَّفة" · en: "The value is not a defined payment method", and no AccountMapping row is written

### REQ-FIN-062 — وسوم الأبعاد على سطر القاعدة / Dimension tags on a rule line
Pattern    : event
Statement  : When a finance administrator adds a rule line with dimension tags, the system shall record for each tag its dimension, its value source and either its constant dimension value or its business field.
Traces     : US-FIN-026
Entities   : ENT-FIN-010, ENT-FIN-016
Rationale  : POL-FIN-026 — the same discipline as accounts: constant or named business field; RULE-FIN-025, RULE-FIN-026
Source     : CS-FIN-001 §2; ADR-FIN-025
Priority   : HIGH
#### AC-FIN-066 — [REQ-FIN-062]
Given  : dimensions ORGANISATION (value "ORG-1") and BRANCH, both active
When   : a finance administrator adds a line with tags {ORGANISATION: CONSTANT "ORG-1"} and {BRANCH: BUSINESS_FIELD BRANCH_CODE}
Then   : the system creates the RuleLine and 2 RuleLineDimension rows, one per tag, with those sources

### REQ-FIN-063 — ضبط الأبعاد على سطر القيد المبني من الحدث / Set the dimensions on an event-built journal line
Pattern    : optional
Statement  : Where a rule line carries a dimension tag, the system shall set that dimension on the journal line built from the rule line, with the tag's constant dimension value or with the dimension value whose code equals the named business field's value in the event.
Traces     : US-FIN-026
Entities   : ENT-FIN-003, ENT-FIN-005, ENT-FIN-006, ENT-FIN-016
Rationale  : POL-FIN-026 — organisation and branch attribution on every event-built line makes the parallel-run comparison meaningful; RULE-FIN-027; ADR-FIN-026
Source     : CS-FIN-001 §2, acceptance 4
Priority   : HIGH
#### AC-FIN-067 — [REQ-FIN-063]
Given  : the rule line of AC-FIN-066 on the PAYMENT_RECEIVED rule
When   : event "EVT-3011" arrives with fields.BRANCH_CODE = "BR-07"
Then   : the posted journal line built from that rule line carries ORGANISATION = "ORG-1"
#### AC-FIN-068 — [REQ-FIN-063]
Given  : the rule line of AC-FIN-066, and BRANCH holds an active value with code "BR-07"
When   : event "EVT-3011" arrives with fields.BRANCH_CODE = "BR-07"
Then   : the posted journal line built from that rule line carries BRANCH = the DimensionValue whose code is "BR-07"

### REQ-FIN-064 — رفض وسم البُعد نفسه مرتين / Reject two tags for the same dimension
Pattern    : unwanted
Statement  : If a rule line carries two dimension tags for the same dimension, then the system shall reject the rule line.
Traces     : US-FIN-026
Entities   : ENT-FIN-016
Rationale  : RULE-FIN-025 — a journal line holds one value per dimension (ENT-FIN-006, v1 shape)
Source     : CS-FIN-001 §2 "per dimension the value is either a constant or …"
Priority   : MEDIUM
#### AC-FIN-069 — [REQ-FIN-064]
Given  : an event-type rule
When   : the administrator adds a line tagged {BRANCH: CONSTANT "BR-01"} and {BRANCH: BUSINESS_FIELD BRANCH_CODE}
Then   : the system rejects the line with ar: "لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة" · en: "A rule line cannot tag the same dimension twice", and no RuleLine row is created

### REQ-FIN-065 — رفض وسم لا يطابق مصدره / Reject a tag inconsistent with its source
Pattern    : unwanted
Statement  : If a dimension tag does not carry exactly the value its source requires, then the system shall reject the rule line.
Traces     : US-FIN-026
Entities   : ENT-FIN-016
Rationale  : RULE-FIN-026 — a CONSTANT tag needs a value of its own dimension, a BUSINESS_FIELD tag needs a business field, never both
Source     : CS-FIN-001 §2; ADR-FIN-010
Priority   : MEDIUM
#### AC-FIN-070 — [REQ-FIN-065]
Given  : dimensions BRANCH and ORGANISATION, and "ORG-1" is a value of ORGANISATION
When   : the administrator adds a line tagged {BRANCH: CONSTANT "ORG-1"}
Then   : the system rejects the line with ar: "وسم البُعد لا يطابق مصدر قيمته" · en: "The dimension tag does not match its value source", and no RuleLine row is created

### REQ-FIN-066 — تحقق قيمة البُعد المشتقة من الحدث / Validate an event-derived dimension value
Pattern    : event
Statement  : When the system resolves a dimension value for a journal line built from an event, the system shall validate that the value belongs to the tagged dimension and is active, with the same check it applies to a manually entered line.
Traces     : US-FIN-027
Entities   : ENT-FIN-003, ENT-FIN-006, ENT-FIN-016
Rationale  : RULE-FIN-009 reused unchanged; POL-FIN-027 — the existing dimension validation is not bypassed
Source     : CS-FIN-001 §2 "the existing validation is not bypassed"
Priority   : HIGH
#### AC-FIN-071 — [REQ-FIN-066]
Given  : the rule line of AC-FIN-066, and "ORG-1" and "BR-07" both active
When   : event "EVT-3012" arrives with fields.BRANCH_CODE = "BR-07"
Then   : the system validates both resolved values and posts the entry with 2 JournalLineDimension rows on that line

### REQ-FIN-067 — رفض قيمة بُعد غير نشطة من الحدث / Reject an inactive event-derived dimension value
Pattern    : unwanted
Statement  : If a dimension value resolved for an event-built journal line is inactive, then the system shall reject the event and post no journal entry.
Traces     : US-FIN-027, US-FIN-028
Entities   : ENT-FIN-003, ENT-FIN-006
Rationale  : RULE-FIN-009; POL-FIN-028 — an invalid value rejects the event, as a wrong account does
Source     : CS-FIN-001 §2; resolved decision v2-2 (ADR-FIN-011)
Priority   : HIGH
#### AC-FIN-072 — [REQ-FIN-067]
Given  : the rule line of AC-FIN-066, and BRANCH value "BR-09" is inactive
When   : event "EVT-3013" arrives with fields.BRANCH_CODE = "BR-09"
Then   : the system rejects the event with ar: "قيمة البُعد غير صالحة" · en: "The dimension value is invalid", and no JournalEntry exists with eventReference "EVT-3013"

### REQ-FIN-068 — رفض حدث بلا الحقل التجاري المطلوب للبُعد / Reject an event missing a tag's business field
Pattern    : unwanted
Statement  : If a canonical accounting event does not carry the business field that a dimension tag names, then the system shall reject the event and post no journal entry.
Traces     : US-FIN-028
Entities   : ENT-FIN-004, ENT-FIN-016
Rationale  : RULE-FIN-021; POL-FIN-028 — an entry without organisation or branch attribution corrupts the parallel-run comparison
Source     : CS-FIN-001 §1 "never a default", §2; ADR-FIN-011
Priority   : HIGH
#### AC-FIN-073 — [REQ-FIN-068]
Given  : the rule line of AC-FIN-066
When   : event "EVT-3014" arrives with no fields.BRANCH_CODE key
Then   : the system rejects the event with ar: "الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة" · en: "The event does not carry the business field the rule requires", and no JournalEntry exists with eventReference "EVT-3014"

### REQ-FIN-069 — رفض قيمة بُعد غير معرَّفة من الحدث / Reject an event dimension value that matches no defined value
Pattern    : unwanted
Statement  : If the value of a dimension tag's business field in the event matches no dimension value code of the tagged dimension, then the system shall reject the event and post no journal entry.
Traces     : US-FIN-028
Entities   : ENT-FIN-003, ENT-FIN-016
Rationale  : RULE-FIN-027; no translation, no guess (ADR-FIN-012, ADR-FIN-026)
Source     : CS-FIN-001 §2; POL-FIN-028
Priority   : HIGH
#### AC-FIN-074 — [REQ-FIN-069]
Given  : the rule line of AC-FIN-066, and BRANCH holds no value with code "BR-99"
When   : event "EVT-3015" arrives with fields.BRANCH_CODE = "BR-99"
Then   : the system rejects the event with ar: "قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد" · en: "The dimension value in the event matches no defined value of this dimension", and no JournalEntry exists with eventReference "EVT-3015"

### REQ-FIN-070 — شكل أبعاد واحد لكل السطور / One dimension shape for every line
Pattern    : ubiquitous
Statement  : The system shall record each dimension value of an event-built journal line as a journal line dimension of that line, in the same form as for a manually entered journal line.
Traces     : US-FIN-029
Entities   : ENT-FIN-005, ENT-FIN-006
Rationale  : POL-FIN-029 — ledger and dimension reports read one shape, whatever the source
Source     : CS-FIN-001 §2 "the same dimension shape"
Priority   : HIGH
#### AC-FIN-075 — [REQ-FIN-070]
Given  : entry A posted from event "EVT-3016" with BRANCH "BR-07" on its debit line, and manual entry B posted with BRANCH "BR-07" on its debit line
When   : both entries are read
Then   : each debit line holds exactly 1 JournalLineDimension row with dimension BRANCH and the same dimensionValueId

### REQ-FIN-071 — تقارير واحدة لسطور الأحداث واليدوية / One reporting treatment for event and manual lines
Pattern    : event
Statement  : When a financial controller opens the account ledger or a dimension report, the system shall group and filter event-built and manually entered journal lines by the same account-and-dimension combination regardless of the entry's journal type.
Traces     : US-FIN-029
Entities   : ENT-FIN-005, ENT-FIN-006
Rationale  : POL-FIN-029, POL-FIN-011 — dimension reports stop being empty for the entries that matter most; REQ-FIN-039 and REQ-FIN-043 unchanged, this states that they cover event lines
Source     : CS-FIN-001 §2 "ledger and dimension reports treat them identically"
Priority   : HIGH
#### AC-FIN-076 — [REQ-FIN-071]
Given  : the two entries of AC-FIN-075, each debiting account 5100 with 100.00 under BRANCH "BR-07"
When   : the controller opens the dimension report by BRANCH
Then   : the system shows 1 row for (5100, "BR-07") with a debit total of 200.00

### REQ-FIN-072 — الحدث المكرر يُجاب دائمًا بأنه مكرر / A duplicate event is always answered as a duplicate
Pattern    : unwanted
Statement  : If a canonical accounting event arrives with an event reference that is already recorded, then the system shall answer the duplicate-event error whether the pre-check or the store's uniqueness guarantee detects the repeat.
Traces     : US-FIN-030
Entities   : ENT-FIN-004
Rationale  : RULE-FIN-004 (MODIFIED v2); POL-FIN-030 — for the consumer a duplicate is success; a generic server error causes retries, a dead-letter queue and manual reconciliation
Source     : CS-FIN-001 §3, acceptance 5; ADR-FIN-027
Priority   : —
#### AC-FIN-077 — [REQ-FIN-072]
Given  : a POSTED entry with eventReference "EVT-4001"
When   : another event with reference "EVT-4001" arrives
Then   : the system answers the duplicate-event error with ar: "تم بالفعل ترحيل قيد لهذا المرجع" · en: "An entry for this event reference has already been posted", and exactly 1 JournalEntry exists with eventReference "EVT-4001"
#### AC-FIN-078 — [REQ-FIN-072]
Given  : no entry with eventReference "EVT-4002"
When   : two events with reference "EVT-4002" arrive at the same time, so both pass the pre-check and the store's uniqueness guarantee refuses the second
Then   : the system posts exactly 1 entry for "EVT-4002" and answers the other with the duplicate-event error of AC-FIN-077, never a generic server error

### REQ-FIN-073 — رمز مميز لرفض الفترة غير المفتوحة / A distinct code for a non-open period
Pattern    : unwanted
Statement  : If a canonical accounting event's posting targets an existing accounting period that is not Open, then the system shall reject it with the period-not-open error, distinct from every validation and configuration error.
Traces     : US-FIN-031
Entities   : ENT-FIN-004, ENT-FIN-008
Rationale  : RULE-FIN-008 (unchanged); POL-FIN-031 — an operational state that clears when the period is opened, so the consumer waits and retries; confirmed as the existing v1 code (ADR-FIN-027, ADR-FIN-014)
Source     : CS-FIN-001 §4, acceptance 6
Priority   : —
#### AC-FIN-079 — [REQ-FIN-073]
Given  : the period covering 2026-10-05 is SOFT_CLOSE
When   : event "EVT-4003" dated 2026-10-05 arrives
Then   : the system rejects it with the period-not-open error, ar: "الفترة المستهدفة غير مفتوحة" · en: "The target period is not open", whose code differs from every validation and configuration error code, and no JournalEntry exists with eventReference "EVT-4003"

### REQ-FIN-074 — تاريخ بلا فترة معرَّفة خطأ إعداد / A date with no defined period is a configuration error
Pattern    : unwanted
Statement  : If a canonical accounting event's date falls within no defined accounting period, then the system shall reject the event with a configuration error distinct from the period-not-open error.
Traces     : US-FIN-031
Entities   : ENT-FIN-004, ENT-FIN-008
Rationale  : RULE-FIN-028; a calendar setup gap is not cleared by opening a period, so it is never the retry signal (ADR-FIN-014)
Source     : CS-FIN-001 §4 "distinct from validation and configuration errors"; resolved decision v2-5
Priority   : —
#### AC-FIN-080 — [REQ-FIN-074]
Given  : no FiscalPeriod whose startDate…endDate covers 2027-01-15
When   : event "EVT-4004" dated 2027-01-15 arrives
Then   : the system rejects it with ar: "لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث" · en: "No accounting period is defined for the event date", with a code different from the period-not-open code, and no JournalEntry exists with eventReference "EVT-4004"

### REQ-FIN-075 — توثيق رمز الفترة غير المفتوحة للمستهلك / Document the period-not-open code for the consumer
Pattern    : ubiquitous
Statement  : The system shall list the period-not-open error code in FIN's published error catalogue as the rejection that clears once the target period is opened.
Traces     : US-FIN-031
Entities   : ENT-FIN-008
Rationale  : POL-FIN-031 "catalogued and documented" — the consumer depends on the code, so it is published, not inferred
Source     : CS-FIN-001 §4 "the code is documented for the consumer to depend on"
Priority   : —
#### AC-FIN-081 — [REQ-FIN-075]
Given  : FIN's published API documentation
When   : the consumer's integrator reads the error catalogue
Then   : the catalogue lists the period-not-open code exactly once, with its ar/en message and the note that the rejection clears when the period is opened

### REQ-FIN-076 — ثوابت v1 على المسار الجديد عند الترحيل / v1 posting checks hold on the new path
Pattern    : unwanted
Statement  : If a journal entry built through account mapping or dimension tags fails any v1 posting check, then the system shall reject posting it exactly as it rejects an entry from any other source.
Traces     : US-FIN-032
Entities   : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006
Rationale  : POL-FIN-032 — RULE-FIN-006 (balance), RULE-FIN-007 (leaf and active), RULE-FIN-008 (period gate at post), RULE-FIN-009 (dimension validity), RULE-FIN-003 and RULE-FIN-010 (single remainder line, positive amounts with direction), RULE-FIN-004 (duplicate) apply unchanged; inherited, not reopened
Source     : CS-FIN-001 acceptance 7
Priority   : —
#### AC-FIN-082 — [REQ-FIN-076]
Given  : a rule distributing the event amount as 33% + 33% + remainder, whose remainder line is MAPPING on PAYMENT_METHOD, and an active mapping for "CHEQUE"
When   : event "EVT-4005" arrives with amount 100.00 and fields.PAYMENT_METHOD = "CHEQUE"
Then   : the two percentage lines total 66.00, the remainder line is exactly 34.00 on the mapped account, and the posted entry's debits equal its credits at 100.00

### REQ-FIN-077 — القيد المُرحَّل عبر المسار الجديد كغيره / A posted entry from the new path behaves as any other
Pattern    : ubiquitous
Statement  : The system shall apply the v1 post-posting invariants of lock, reversal-only correction and derived balances to a posted journal entry built through account mapping or dimension tags.
Traces     : US-FIN-032
Entities   : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006
Rationale  : POL-FIN-032 with POL-FIN-007, POL-FIN-009, POL-FIN-013 — RULE-FIN-016 and RULE-FIN-011 apply unchanged
Source     : CS-FIN-001 acceptance 7
Priority   : —
#### AC-FIN-083 — [REQ-FIN-077]
Given  : entry "JV-2026-000600" posted from event "EVT-4006" with a mapped debit account and BRANCH "BR-07" on its lines
When   : an accountant reverses it
Then   : the system posts a reversal whose lines carry the same accounts, amounts and dimension values with opposite direction, the original stays POSTED, and every account and BRANCH balance nets to 0.00

## A5 — Business rules

### RULE-FIN-004 — رفض مرجع حدث مكرر / Reject a duplicate event reference
Scope      : ENT-FIN-004
Trigger    : on create (event-sourced entry) — by the pre-check before building, and again when the store's uniqueness guarantee on eventReference refuses a concurrent second write
Statement  : The system shall reject building an entry for an event reference that has already produced a posted entry, and shall answer that rejection with this rule's duplicate-event error and message whichever of the two detections catches the repeat; a uniqueness refusal from the store is never surfaced as a generic data-integrity or server error.
Data source: ENT-FIN-004.eventReference, ENT-FIN-004.statusCode
Message    : ar: "تم بالفعل ترحيل قيد لهذا المرجع" · en: "An entry for this event reference has already been posted"
Traces     : REQ-FIN-011, REQ-FIN-072
Source     : general-accounting-system-plan-en.md §12.12; MODIFIED v2 — POL-FIN-030, CS-FIN-001 §3; ADR-FIN-027 (code unchanged)
Test-Hint  : the concurrent path is the one that used to leak a 500 — two simultaneous submissions of one reference, exactly one posts

### RULE-FIN-008 — بوابة الفترة عند الترحيل / Period gate at post time
Scope      : ENT-FIN-004, ENT-FIN-008
Trigger    : on post (any source)
Statement  : The system shall reject posting an entry whose period is not Open at that moment, except the year-end closing and opening entries generated by the year-end close (REQ-FIN-036, API-FIN-027), which are exempt from this gate: that close runs only after every period of the year is Hard Closed, so no Open period exists for its own entries by construction. The exemption is part of this rule and applies to no other entry and to no other rule — RULE-FIN-006, RULE-FIN-007 and RULE-FIN-009 apply to both generated entries in full.
Data source: ENT-FIN-004.periodId, ENT-FIN-008.statusCode
Message    : ar: "الفترة المستهدفة غير مفتوحة" · en: "The target period is not open"
Traces     : REQ-FIN-020, REQ-FIN-073
Source     : general-accounting-system-plan-en.md §12.4; MODIFIED v2 — wording only ("may only run once" → "runs only after"), no behaviour change (ADR-FIN-037)

### RULE-FIN-018 — نوعا اشتقاق الحساب المسموحان فقط / Only the two permitted account derivation types
Scope      : ENT-FIN-010
Trigger    : on create (rule line)
Statement  : The system shall reject a rule line whose account derivation type is not an active ACCOUNT_DERIVATION_TYPE value, so that CONSTANT and MAPPING are the only accepted derivations and no derivation reads an account code from the event.
Data source: ENT-FIN-010.accountDerivationTypeCode
Message    : ar: "نوع اشتقاق الحساب غير مدعوم؛ المسموح قيمة ثابتة أو ربط" · en: "Unsupported account derivation type; only constant or mapping is allowed"
Traces     : REQ-FIN-048
Source     : POL-FIN-021; ADR-FIN-010

### RULE-FIN-019 — اكتمال مواصفة الاشتقاق / Derivation spec matches its type
Scope      : ENT-FIN-010
Trigger    : on create (rule line)
Statement  : The system shall require a CONSTANT rule line to carry an account code in accountDerivationValue and no business field, and a MAPPING rule line to carry a business field in accountBusinessFieldCode and no account code.
Data source: ENT-FIN-010.accountDerivationTypeCode, ENT-FIN-010.accountDerivationValue, ENT-FIN-010.accountBusinessFieldCode
Message    : ar: "مواصفة اشتقاق الحساب لا تطابق نوعه" · en: "The account derivation spec does not match its type"
Traces     : REQ-FIN-049
Source     : POL-FIN-022; ADR-FIN-023

### RULE-FIN-020 — القيمة التجارية يجب أن تكون مربوطة / The business value must be mapped
Scope      : ENT-FIN-015, ENT-FIN-010
Trigger    : on build (event-sourced entry, per MAPPING rule line)
Statement  : The system shall reject the event with the dedicated unmapped-value error when no active account mapping exists for the rule's event type, the rule line's business field and the event's value for that field; it never substitutes a default account.
Data source: ENT-FIN-015.eventTypeCode, ENT-FIN-015.businessFieldCode, ENT-FIN-015.businessValue, ENT-FIN-015.isActiveFl, ENT-FIN-015.accountId, ENT-FIN-010.accountBusinessFieldCode, ENT-FIN-009.eventTypeCode
Message    : ar: "لا يوجد ربط حساب نشط للقيمة التجارية في الحدث" · en: "No active account mapping exists for the business value in the event"
Traces     : REQ-FIN-050, REQ-FIN-053
Source     : POL-FIN-022, POL-FIN-024; CS-FIN-001 §1; ADR-FIN-027
Test-Hint  : an inactive mapping counts as no mapping

### RULE-FIN-021 — الحدث يحمل الحقل التجاري المطلوب / The event carries the business field the rule names
Scope      : ENT-FIN-010, ENT-FIN-016
Trigger    : on build (event-sourced entry, per MAPPING rule line and per BUSINESS_FIELD dimension tag)
Statement  : The system shall reject the event with the missing-business-field error when the event does not carry the business field named by a MAPPING rule line or by a BUSINESS_FIELD dimension tag.
Data source: ENT-FIN-010.accountBusinessFieldCode, ENT-FIN-016.businessFieldCode, ENT-FIN-016.valueSourceCode
Message    : ar: "الحدث لا يحمل الحقل التجاري الذي تطلبه القاعدة" · en: "The event does not carry the business field the rule requires"
Traces     : REQ-FIN-051, REQ-FIN-068
Source     : CS-FIN-001 §1 "never a default, never a guess"; POL-FIN-024, POL-FIN-028; ADR-FIN-027

### RULE-FIN-022 — ربط نشط واحد لكل مفتاح / One active mapping per key
Scope      : ENT-FIN-015
Trigger    : on create (account mapping)
Statement  : The system shall reject an account mapping whose event type, business field and business value already have an active mapping.
Data source: ENT-FIN-015.eventTypeCode, ENT-FIN-015.businessFieldCode, ENT-FIN-015.businessValue, ENT-FIN-015.isActiveFl
Message    : ar: "يوجد بالفعل ربط نشط لنوع الحدث والحقل التجاري والقيمة نفسها" · en: "An active mapping already exists for this event type, business field and value"
Traces     : REQ-FIN-059
Source     : module-registry-fin.md v2 AUTO-DECISION (one active mapping per key); ADR-FIN-024

### RULE-FIN-023 — الحساب المربوط ورقي نشط عند الحفظ / A mapped account is an active leaf on save
Scope      : ENT-FIN-015
Trigger    : on create / update (account mapping)
Statement  : The system shall reject an account mapping whose account is not a leaf account or is inactive.
Data source: ENT-FIN-015.accountId, ENT-FIN-001.isLeafFl, ENT-FIN-001.isActiveFl
Message    : ar: "الحساب المربوط يجب أن يكون حسابًا ورقيًا نشطًا" · en: "The mapped account must be an active leaf account"
Traces     : REQ-FIN-060
Source     : POL-FIN-023; ADR-FIN-024
Test-Hint  : does not replace RULE-FIN-007 at post time — an account deactivated after the mapping was saved is still caught there

### RULE-FIN-024 — قيمة طريقة الدفع رمز معرَّف / A payment-method value is a defined code
Scope      : ENT-FIN-015
Trigger    : on create (account mapping)
Statement  : The system shall reject an account mapping for the PAYMENT_METHOD business field whose business value is not an active PAYMENT_METHOD code.
Data source: ENT-FIN-015.businessFieldCode, ENT-FIN-015.businessValue
Message    : ar: "القيمة ليست طريقة دفع معرَّفة" · en: "The value is not a defined payment method"
Traces     : REQ-FIN-061
Source     : module-registry-fin.md v2 AUTO-DECISION (business value stored as a code; PAYMENT_METHOD → its lookup)

### RULE-FIN-025 — وسم واحد لكل بُعد على سطر القاعدة / One tag per dimension on a rule line
Scope      : ENT-FIN-016
Trigger    : on create (rule line with its dimension tags)
Statement  : The system shall reject a rule line that carries more than one dimension tag for the same dimension.
Data source: ENT-FIN-016.ruleLineId, ENT-FIN-016.dimensionId
Message    : ar: "لا يجوز وسم البُعد نفسه مرتين على سطر القاعدة" · en: "A rule line cannot tag the same dimension twice"
Traces     : REQ-FIN-064, REQ-FIN-062
Source     : CS-FIN-001 §2; ENT-FIN-006 v1 shape (one value per dimension per line)

### RULE-FIN-026 — وسم البُعد يطابق مصدر قيمته / A dimension tag matches its value source
Scope      : ENT-FIN-016
Trigger    : on create (rule line with its dimension tags)
Statement  : The system shall require a CONSTANT dimension tag to carry a dimension value that belongs to the tagged dimension and no business field, and a BUSINESS_FIELD dimension tag to carry a business field and no dimension value.
Data source: ENT-FIN-016.valueSourceCode, ENT-FIN-016.dimensionId, ENT-FIN-016.dimensionValueId, ENT-FIN-016.businessFieldCode, ENT-FIN-003.dimensionId
Message    : ar: "وسم البُعد لا يطابق مصدر قيمته" · en: "The dimension tag does not match its value source"
Traces     : REQ-FIN-065, REQ-FIN-062
Source     : POL-FIN-026; ADR-FIN-010

### RULE-FIN-027 — قيمة البُعد من الحدث قابلة للتحديد / An event dimension value resolves to a defined value
Scope      : ENT-FIN-016, ENT-FIN-003
Trigger    : on build (event-sourced entry, per BUSINESS_FIELD dimension tag)
Statement  : The system shall reject the event with the unresolvable-dimension-value error when the event's value for a tag's business field equals the code of no dimension value of the tagged dimension.
Data source: ENT-FIN-016.dimensionId, ENT-FIN-016.businessFieldCode, ENT-FIN-003.code, ENT-FIN-003.dimensionId
Message    : ar: "قيمة البُعد في الحدث لا تطابق أي قيمة معرَّفة لهذا البُعد" · en: "The dimension value in the event matches no defined value of this dimension"
Traces     : REQ-FIN-069, REQ-FIN-063
Source     : POL-FIN-028; ADR-FIN-012, ADR-FIN-026, ADR-FIN-027
Test-Hint  : a matching but inactive value is RULE-FIN-009's rejection, not this one

### RULE-FIN-028 — تاريخ الحدث داخل فترة معرَّفة / The event date falls in a defined period
Scope      : ENT-FIN-008
Trigger    : on build (event-sourced entry)
Statement  : The system shall reject the event with the no-defined-period configuration error when no accounting period's start and end dates cover the event's date, and shall never answer this case with the period-not-open error.
Data source: ENT-FIN-008.startDate, ENT-FIN-008.endDate
Message    : ar: "لا توجد فترة محاسبية معرَّفة تغطي تاريخ الحدث" · en: "No accounting period is defined for the event date"
Traces     : REQ-FIN-074
Source     : POL-FIN-031 rationale; resolved decision v2-5; ADR-FIN-014, ADR-FIN-027

## A6 — Lookups

All 15 lookup types below are owned by FIN and registered into MDL (REQ-FIN-045). v1 had 13; v2 ADDS
2 and MODIFIES 2. module-registry-fin.md (LOOKUPS OWNED, v1 + v2 delta) carries the full source
citation per key; values are runtime-loaded from MDL, never hardcoded.

| Key | Values | v2 |
|---|---|---|
| ACCOUNT_TYPE | ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE | unchanged |
| DEBIT_CREDIT | DEBIT, CREDIT | unchanged |
| PERIOD_STATE | OPEN, SOFT_CLOSE, HARD_CLOSE, YEAR_END_CLOSE | unchanged |
| FISCAL_YEAR_STATUS | OPEN, CLOSED | unchanged |
| JOURNAL_TYPE | EVENT_GENERATED, MANUAL, RECURRING, ALLOCATION, REVERSAL | unchanged |
| JOURNAL_STATUS | DRAFT, POSTED, VOID (VOID is seeded in MDL by V26 and declared as `JournalEntry.STATUS_VOID`, but is now UNREACHABLE: classic reversal leaves the original POSTED and no code path writes VOID — see §A7) | unchanged |
| ACCOUNTING_EVENT_TYPE | none seeded — host-specific, added as data [§3, §6.4] | unchanged; now also used by ENT-FIN-015.eventTypeCode |
| PAYMENT_METHOD | CHEQUE (شيك / cheque) seeded; every other value host-specific, added as data | MODIFIED — CHEQUE named by the user; now used by ENT-FIN-015.businessValue [RULE-FIN-024] |
| ACCOUNT_DERIVATION_TYPE | CONSTANT, MAPPING | MODIFIED — DIRECT removed outright (ADR-FIN-010); MAPPING now implemented |
| AMOUNT_SOURCE_TYPE | FIELD, PERCENTAGE, REMAINDER | unchanged |
| DISTRIBUTION_TYPE | FIXED, PERCENTAGE, REMAINDER | unchanged |
| RECURRING_SCHEDULE_TYPE | RECURRING, REVERSING | unchanged |
| RECURRING_FREQUENCY | MONTHLY, QUARTERLY, ANNUALLY, WEEKLY | unchanged |
| FIN_EVENT_BUSINESS_FIELD | PAYMENT_METHOD, EXPENSE_TYPE_CODE, ORGANISATION_CODE, BRANCH_CODE | ADDED |
| FIN_DIMENSION_VALUE_SOURCE | CONSTANT, BUSINESS_FIELD | ADDED |

v2 lookups in the §6 block form:

```
FIN_EVENT_BUSINESS_FIELD — owner: FIN (ADR-FIN-013)
  used by   : ENT-FIN-010.accountBusinessFieldCode · ENT-FIN-015.businessFieldCode · ENT-FIN-016.businessFieldCode
  control   : lookup (fixed short list, grows as data when a new business fact is needed)
  values    : PAYMENT_METHOD    — طريقة الدفع / Payment method
              EXPENSE_TYPE_CODE — رمز نوع المصروف / Expense type code
              ORGANISATION_CODE — رمز المؤسسة / Organisation code
              BRANCH_CODE       — رمز الفرع / Branch code
  meaning   : the event names each business fact it carries by one of these codes; no code names an account
  source    : business-policies-fin.md v2 CUSTOM LOOKUP VALUES; CS-FIN-001 §1, §2

FIN_DIMENSION_VALUE_SOURCE — owner: FIN (ADR-FIN-010)
  used by   : ENT-FIN-016.valueSourceCode
  control   : lookup (fixed short list)
  values    : CONSTANT       — ثابت / Constant
              BUSINESS_FIELD — حقل تجاري / Business field
  source    : business-policies-fin.md v2 CUSTOM LOOKUP VALUES; CS-FIN-001 §2

ACCOUNT_DERIVATION_TYPE (MODIFIED) — owner: FIN
  used by   : ENT-FIN-010.accountDerivationTypeCode
  values    : CONSTANT — قيمة ثابتة / Constant
              MAPPING  — ربط / Mapping
  removed   : DIRECT (no data used it — v1 never delivered; ADR-FIN-010)

PAYMENT_METHOD (MODIFIED) — owner: FIN
  used by   : ENT-FIN-015.businessValue (when businessFieldCode=PAYMENT_METHOD)
  values    : CHEQUE — شيك / Cheque (named by the user); every other method host-defined, added as data
```

Consumed lookups: none — FIN owns every coded value list it uses.

## A7 — Status lifecycle

v2 adds no status field: ENT-FIN-015 carries only the binary `isActiveFl` and ENT-FIN-016 carries none
(not applicable). The v1 lifecycles below stand unchanged and are restated so the folded state keeps them.

**JournalEntry.statusCode — ENT-FIN-004 (JOURNAL_STATUS, 3 states)**
```
DRAFT  --(REQ-FIN-017, automatic validation passes)--> POSTED   (terminal)
POSTED --(REQ-FIN-028, reverse action)----------------> POSTED  (no transition — classic reversal: the original STAYS POSTED; its reversal is itself a new POSTED entry, and the two are linked through originalEntryId/reversalEntryId)
```
Classic reversal is the convention FIN follows: reversing an entry posts an equal, opposite
mirror entry and leaves the original POSTED and untouched, so the net effect on every account
balance is zero and both entries stay visible in the ledger and its audit trail. VOIDing the
original instead would remove it from the POSTED-only reports while the mirror remained,
netting to −(original). It also means RULE-FIN-016 (a POSTED row is never modified) holds
without exception — the reverse action's only write back to the original is the
reversalEntryId link.

DRAFT is transient (build → validate happens in one orchestration, RULE-FIN-016 locks
POSTED immediately); a DRAFT that fails validation is never saved (REQ-FIN-015/018-021
reject before any row is written) — not a stored intermediate state a user can browse. v2: the
same holds for every new event rejection (REQ-FIN-051, 052, 053, 067, 068, 069, 073, 074) — each is
raised before any row is written, so no entry and no line exists for the rejected event.

**FiscalPeriod.statusCode — ENT-FIN-008 (PERIOD_STATE, 4 states)**
```
OPEN        --(REQ-FIN-033, soft-close)--------> SOFT_CLOSE
SOFT_CLOSE  --(REQ-FIN-032, reopen)------------> OPEN
SOFT_CLOSE  --(REQ-FIN-034, hard-close)--------> HARD_CLOSE
HARD_CLOSE  --(REQ-FIN-036, year-end close, all periods of the year)--> YEAR_END_CLOSE
```
HARD_CLOSE and YEAR_END_CLOSE are terminal (RULE-FIN-014 — never reopened). v2: any state other than
OPEN answers an event posting with the period-not-open error (REQ-FIN-073); only SOFT_CLOSE can
later return to OPEN, so it is the case the consumer's retry resolves.

**FiscalYear.statusCode — ENT-FIN-007 (FISCAL_YEAR_STATUS, 2 states)** — binary
(OPEN/CLOSED, set by REQ-FIN-036) — not applicable for a diagram.

All other statuses in this module (isActiveFl flags) are binary — not applicable.

## A8 — Module dependencies

| Consumed entity | Owner ENT id | Owner module | HARD-FK / SOFT-READ | XM candidate (assigned by P2) |
|---|---|---|---|---|
| User (identity, principal string on audit columns) | ENT-SEC-001 | SEC | HARD-FK (per SEC's own precedent: principal string, not numeric FK — see AUTO-DECISIONS) | assigned by P2 |
| ModuleRegistry, ScreenRegistry, ActionRegistry | ENT-SEC-004, ENT-SEC-005, ENT-SEC-006 | SEC | HARD-FK (registration as data) — v2: + the Account mappings screen and its actions (SCR-REQ-FIN-013) | assigned by P2 |
| LookupType, LookupValue | ENT-MDL-001, ENT-MDL-002 | MDL | HARD-FK (registration + runtime read) — v2: + 2 lookup types, CHEQUE value, DIRECT value retired; the physical shape stays the v1 SOFT-READ lookup validation (ADR-FIN-001), now over 15 keys | assigned by P2 |

| External service | Purpose | Integration kind |
|---|---|---|
| Notifications (ready, external) | period-close-awaiting notice | SOFT / optional, per general-accounting-system-plan-en.md §2.3 |
| File Service (ready, external) | statement export | SOFT / optional, per general-accounting-system-plan-en.md §2.3 |
| Legacy Oracle system (host, v2) | emits the canonical accounting event carrying business facts only — never an account code | EVENT only, through the out-of-scope consumer; no table read or write in either direction |

# PART B — SCREEN REQUIREMENTS

v2: SCR-REQ-FIN-013 ADDED; SCR-REQ-FIN-003 and SCR-REQ-FIN-006 MODIFIED (re-emitted in full below);
the other ten screen requirements stand unchanged from v1.

## SCR-REQ-FIN-003 — قواعد المحرك / Engine rules
### B1 — Definition
Purpose      : إدارة قواعد ربط أنواع الأحداث بسطور القيد، ومنها سطور تشتق حسابها بالربط وتحمل وسوم الأبعاد.
Entities     : ENT-FIN-009, ENT-FIN-010, ENT-FIN-016
Operations   : search, create, deactivate (rule — API-FIN-034); create (line, with its dimension tags). As
               built there is no update and no by-id read on either, and no line delete — see B4
Users        : مسؤول مالي
Navigation   : FIN → Setup → Engine rules; to: SCR-REQ-FIN-013 (the mappings a MAPPING line resolves through)
Content shape: header + repeating lines (rule header + its lines), each line with its own repeating dimension tags
Traces       : REQ-FIN-007, REQ-FIN-008, REQ-FIN-009, REQ-FIN-048, REQ-FIN-049, REQ-FIN-062, REQ-FIN-064, REQ-FIN-065
Composite    : Master (rules) + Detail (lines, with their tags) = ONE screen requirement
MODIFIED v2  : MAPPING lines name a business field; DIRECT no longer offered; dimension tags per line
### B2 — Search / list
Filters: eventTypeCode(EXACT), isActiveFl(EXACT) — correspond to result columns.
### B3 — Input
Rule fields: eventTypeCode, nameAr, nameEn (ENT-FIN-009). Line fields: accountDerivationTypeCode,
accountDerivationValue (CONSTANT only), accountBusinessFieldCode (MAPPING only), amountSourceTypeCode,
amountSourceValue, directionCode, distributionTypeCode, isRemainderFl (ENT-FIN-010; RULE-FIN-003 blocks
saving until exactly one remainder line exists when a percentage line is present; RULE-FIN-018 admits
only CONSTANT and MAPPING; RULE-FIN-019 pairs the derivation value or business field with the type).
Dimension tags per line (repeating, ENT-FIN-016): dimensionId, valueSourceCode, dimensionValueId
(CONSTANT only), businessFieldCode (BUSINESS_FIELD only); RULE-FIN-025 one tag per dimension, RULE-FIN-026
tag matches its source.
### B4 — Access
Page code: FIN_RULES. Actions: VIEW, CREATE, UPDATE. Roles: مسؤول المالية / finance administrator holds all three.

**UPDATE covers two endpoints**, both gated by the single `PERM_FIN_RULES_UPDATE` that V24 already
seeds — no new permission constant and no migration were needed: API-FIN-011 (add a rule line) and
API-FIN-034, `PUT /api/v1/fin/event-rules/{id}/deactivate` (note the base path is
`/api/v1/fin/event-rules`, not `event-type-rules`). An unknown rule id answers the pre-existing
`FIN-404-RULE`. API-FIN-034 exists because until it landed no rule could ever be retired, which
left `FIN-404-NO-ACTIVE-RULE` (RULE-FIN-005, raised by API-FIN-020) unreachable. **Stated
limitation**, recorded in `EventTypeRuleService`'s own javadoc: deactivating a rule does NOT free
its event type for a replacement rule, because `EventTypeRuleService.create` guards uniqueness with
`existsByEventTypeCode`, which is not scoped to the active flag. No `activate` counterpart was
added, matching the delivered `AccountService.deactivate` precedent.

**Not built, and why.** `EventTypeRuleService` publishes exactly `create`, `deactivate` and
`search`; `RuleLineService` publishes `create` alone. So the rule has no update and no by-id read
endpoint, and the line has none of update, by-id read or delete. The rule-line *delete* named under
B1 Operations is a deliberate v1 exclusion rather than an oversight: ENT-FIN-010 carries no active
flag to soft-delete against, and FIN publishes no `DELETE` endpoint on any screen — V24 seeds no
`PERM_FIN_RULES_DELETE` row for this or any other FIN screen.

**v2 — dimension tags need no new action.** Tags travel inside the add-rule-line submission and follow
the line's lifecycle (ADR-FIN-025), so they are gated by the same UPDATE permission as the line itself;
no tag-level update or delete exists, inheriting the line's limitation stated above.
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| search rules | POST | /api/v1/fin/event-rules/search | filters, paging (request body) | Page\<EventTypeRule\> | — | REQ-FIN-007 |
| create rule | POST | /api/v1/fin/event-rules | eventTypeCode, nameAr, nameEn | EventTypeRule | — | REQ-FIN-007 |
| add rule line (v2: with accountBusinessFieldCode and dimension tags) | POST | /api/v1/fin/event-rules/{id}/lines | line fields + dimension tags [] | RuleLine with its tags | RULE-FIN-003, RULE-FIN-018, RULE-FIN-019, RULE-FIN-025, RULE-FIN-026 | REQ-FIN-008, REQ-FIN-009, REQ-FIN-048, REQ-FIN-049, REQ-FIN-062, REQ-FIN-064, REQ-FIN-065 |
| deactivate rule | PUT | /api/v1/fin/event-rules/{id}/deactivate | id | EventTypeRule (isActiveFl=false) | — | REQ-FIN-007 |

## SCR-REQ-FIN-006 — قيود اليومية / Journal entries (view + manual entry + reverse)
### B1 — Definition
Purpose      : عرض قيود اليومية بكل مصادرها، إدخال يدوي، وعكس قيد مُرحَّل.
Entities     : ENT-FIN-004, ENT-FIN-005, ENT-FIN-006
Operations   : search, create (manual), read, reverse; system operation: build an entry from a canonical event (no screen affordance)
Users        : محاسب
Navigation   : FIN → Operations → Journal entries
Content shape: header + repeating lines with totals (debit/credit totals shown live while entering)
Traces       : REQ-FIN-010, REQ-FIN-011, REQ-FIN-012, REQ-FIN-013, REQ-FIN-014, REQ-FIN-015, REQ-FIN-016, REQ-FIN-017, REQ-FIN-018, REQ-FIN-019, REQ-FIN-020, REQ-FIN-021, REQ-FIN-027, REQ-FIN-028, REQ-FIN-029, REQ-FIN-030, REQ-FIN-047, REQ-FIN-050, REQ-FIN-051, REQ-FIN-052, REQ-FIN-053, REQ-FIN-063, REQ-FIN-066, REQ-FIN-067, REQ-FIN-068, REQ-FIN-069, REQ-FIN-070, REQ-FIN-072, REQ-FIN-073, REQ-FIN-074, REQ-FIN-076, REQ-FIN-077
Composite    : Search + Entry (with a "Reverse" action on a posted entry) = ONE screen requirement
MODIFIED v2  : the event-build operation resolves mapped accounts and dimension tags, and answers the
               duplicate, period-not-open and no-defined-period cases distinctly; the screen itself is unchanged —
               event-built lines show their dimension values exactly as manual lines do (REQ-FIN-070)
### B2 — Search / list
Filters: docNo(LIKE), docDate(DATE_RANGE), periodId(EXACT), statusCode(EXACT), journalTypeCode(EXACT) — correspond to result columns.
### B3 — Input
Header fields: docDate, periodId, descriptionAr, descriptionEn (ENT-FIN-004; docNo/statusCode/
journalTypeCode system-set). Line fields (repeating): accountId, amount, directionCode,
dimension value(s) per JournalLineDimension, descriptionAr, descriptionEn (ENT-FIN-005/006).
Buttons: "Post" → REQ-FIN-017 (validates REQ-FIN-018..021 first, REQ-FIN-015 shows failures);
"Reverse" (on a POSTED row) → REQ-FIN-028/029/030.
### B4 — Access
Page code: FIN_JOURNAL_ENTRIES. Actions: VIEW, CREATE (incl. Post), UPDATE (Reverse, modeled
as an update-class action per §7.1 custom-action convention — `PERM_FIN_JOURNAL_ENTRIES_REVERSE`).
v2 adds no action here: the event-build operation is a system call, and the machine caller's
authentication is a separate SEC change set (CS-FIN-001 Out of scope).
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| search entries | POST | /api/v1/fin/journal-entries/search | filters, paging (request body) | Page\<JournalEntry\> | — | REQ-FIN-027 |
| create manual entry | POST | /api/v1/fin/journal-entries | header + lines | JournalEntry (DRAFT then POSTED) | RULE-FIN-006, RULE-FIN-007, RULE-FIN-008, RULE-FIN-009, RULE-FIN-017 | REQ-FIN-014, REQ-FIN-015, REQ-FIN-017, REQ-FIN-018, REQ-FIN-019, REQ-FIN-020, REQ-FIN-021 |
| build event entry (system) | POST | /api/v1/fin/journal-entries/from-event | canonical event payload — business facts named by FIN_EVENT_BUSINESS_FIELD codes, never an account code | JournalEntry | RULE-FIN-004 (both detection paths), RULE-FIN-005, RULE-FIN-006..009, RULE-FIN-010, RULE-FIN-020, RULE-FIN-021, RULE-FIN-027, RULE-FIN-028 | REQ-FIN-010, REQ-FIN-011, REQ-FIN-012, REQ-FIN-013, REQ-FIN-017..021, REQ-FIN-047, REQ-FIN-050..053, REQ-FIN-063, REQ-FIN-066..070, REQ-FIN-072..074, REQ-FIN-076 |
| reverse entry | POST | /api/v1/fin/journal-entries/{id}/reverse | id | JournalEntry (the new reversal) | RULE-FIN-011, RULE-FIN-012, RULE-FIN-013 | REQ-FIN-028, REQ-FIN-029, REQ-FIN-030, REQ-FIN-077 |
| read entry | GET | /api/v1/fin/journal-entries/{id} | id | JournalEntry with lines | — | REQ-FIN-016, REQ-FIN-027 |

Rejection answers of the event-build operation, each distinct (ADR-FIN-027): duplicate event (RULE-FIN-004 —
the consumer acknowledges), period not open (RULE-FIN-008 — the consumer waits and retries; published per
REQ-FIN-075), and — each a failure needing a fix — unmapped value (RULE-FIN-020), missing business field
(RULE-FIN-021), unresolvable dimension value (RULE-FIN-027), no defined period (RULE-FIN-028), plus the v1
rejections. No rejection writes any row.

## SCR-REQ-FIN-013 — روابط الحسابات / Account mappings
### B1 — Definition
Purpose      : إدارة روابط الحسابات كبيانات: لكل نوع حدث وحقل تجاري وقيمة تجارية حسابٌ واحد نشط، يُضاف ويُعدَّل حسابه ويُوقف دون أي تعديل في الشيفرة.
Entities     : ENT-FIN-015
Operations   : search, create, update (account only), deactivate
Users        : مسؤول المالية / finance administrator
Navigation   : FIN → Setup → Account mappings; from: SCR-REQ-FIN-003 (a MAPPING rule line); to: SCR-REQ-FIN-001 (the mapped account)
Content shape: flat record
Traces       : REQ-FIN-054, REQ-FIN-055, REQ-FIN-056, REQ-FIN-057, REQ-FIN-058, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061
Composite    : Search + Entry = ONE screen requirement
### B2 — Search / list
Filters: eventTypeCode(EXACT, lookup ACCOUNTING_EVENT_TYPE), businessFieldCode(EXACT, lookup
FIN_EVENT_BUSINESS_FIELD), businessValue(LIKE), accountId(EXACT), isActiveFl(EXACT) — correspond to the
result columns eventTypeCode, businessFieldCode, businessValue, account (code + nameAr/nameEn from
ENT-FIN-001), isActiveFl. Server-side paging [KB:erp-domain-standards §6].
### B3 — Input
Fields: eventTypeCode, businessFieldCode, businessValue, accountId (ENT-FIN-015). When businessFieldCode is
PAYMENT_METHOD, businessValue is chosen from the PAYMENT_METHOD lookup (RULE-FIN-024); otherwise it is the
host's code typed as-is. accountId offers active leaf accounts (RULE-FIN-023). eventTypeCode,
businessFieldCode and businessValue are read-only once saved (ADR-FIN-024).
Buttons: Save (new) → REQ-FIN-054, applies RULE-FIN-022, RULE-FIN-023, RULE-FIN-024; Save (existing) →
REQ-FIN-056 and REQ-FIN-058, applies RULE-FIN-023; Deactivate → REQ-FIN-057 and REQ-FIN-058.
### B4 — Access
Page code: FIN_ACCOUNT_MAPPINGS (one SEC_PAGES row, registered by REQ-FIN-044). Actions: VIEW (gateway),
CREATE (new mapping), UPDATE (change account, and deactivate — as FIN_ACCOUNTS models deactivate).
Roles: مسؤول المالية / finance administrator holds VIEW, CREATE, UPDATE. No DELETE: mappings are
deactivated, never hard-deleted.
### B5 — API expectations
| Operation | Verb | Path | Inputs | Outputs | RULEs | Traces (REQ) |
|---|---|---|---|---|---|---|
| search account mappings | POST | /api/v1/fin/account-mappings/search | filters (eventTypeCode, businessFieldCode, businessValue, accountId, isActiveFl), paging (request body) | Page\<AccountMapping\> | — | REQ-FIN-055 |
| create account mapping | POST | /api/v1/fin/account-mappings | eventTypeCode, businessFieldCode, businessValue, accountId | AccountMapping | RULE-FIN-022, RULE-FIN-023, RULE-FIN-024 | REQ-FIN-054, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061 |
| update account mapping | PUT | /api/v1/fin/account-mappings/{id} | accountId | AccountMapping | RULE-FIN-023 | REQ-FIN-056, REQ-FIN-058, REQ-FIN-060 |
| deactivate account mapping | PUT | /api/v1/fin/account-mappings/{id}/deactivate | id | AccountMapping (isActiveFl=false) | — | REQ-FIN-057, REQ-FIN-058 |

Verb and path shapes follow FIN's v1 house style (POST `/search` with a request body; deactivate as
`PUT /{id}/deactivate` under UPDATE), not the generic DELETE-as-deactivate of engine §8, so the new
screen reads like its eleven siblings.

# STANDALONE

## Traceability matrix
Current state (v1 rows restated unchanged so the folded state keeps them; v2 rows ADDED; REQ-FIN-045 row MODIFIED).

| P0.5 | REQ | AC | RULE | ENT | SCR-REQ |
|---|---|---|---|---|---|
| US-FIN-001 | REQ-FIN-001, REQ-FIN-002, REQ-FIN-003 | AC-FIN-001…003 | RULE-FIN-001 | ENT-FIN-001 | SCR-REQ-FIN-001 |
| US-FIN-002 | REQ-FIN-004, REQ-FIN-005, REQ-FIN-006 | AC-FIN-004…006 | RULE-FIN-002 | ENT-FIN-002, ENT-FIN-003 | SCR-REQ-FIN-002 |
| US-FIN-003 | REQ-FIN-007, REQ-FIN-008, REQ-FIN-009 | AC-FIN-007…009 | RULE-FIN-003 | ENT-FIN-009, ENT-FIN-010 | SCR-REQ-FIN-003 |
| US-FIN-004 | REQ-FIN-010, REQ-FIN-011, REQ-FIN-012, REQ-FIN-013 | AC-FIN-010…013 | RULE-FIN-004, RULE-FIN-005, RULE-FIN-010 | ENT-FIN-004, ENT-FIN-005, ENT-FIN-009, ENT-FIN-010 | SCR-REQ-FIN-006 |
| US-FIN-005 | REQ-FIN-014, REQ-FIN-015 | AC-FIN-014, AC-FIN-015 | — | ENT-FIN-004, ENT-FIN-005 | SCR-REQ-FIN-006 |
| US-FIN-006 | REQ-FIN-022, REQ-FIN-023, REQ-FIN-024 | AC-FIN-022…024 | — | ENT-FIN-011, ENT-FIN-012 | SCR-REQ-FIN-004 |
| US-FIN-007 | REQ-FIN-025, REQ-FIN-026 | AC-FIN-025, AC-FIN-026 | RULE-FIN-010 | ENT-FIN-013, ENT-FIN-014 | SCR-REQ-FIN-005 |
| US-FIN-008 | REQ-FIN-016, REQ-FIN-027 | AC-FIN-016, AC-FIN-027 | RULE-FIN-016 | ENT-FIN-004 | SCR-REQ-FIN-006 |
| US-FIN-009 | REQ-FIN-028, REQ-FIN-029, REQ-FIN-030 | AC-FIN-028…030 | RULE-FIN-011, RULE-FIN-012, RULE-FIN-013 | ENT-FIN-004, ENT-FIN-005 | SCR-REQ-FIN-006 |
| US-FIN-010 | REQ-FIN-031, REQ-FIN-032, REQ-FIN-033, REQ-FIN-034, REQ-FIN-035, REQ-FIN-036 | AC-FIN-031…036 | RULE-FIN-014 | ENT-FIN-007, ENT-FIN-008 | SCR-REQ-FIN-007 |
| US-FIN-011 | REQ-FIN-037, REQ-FIN-038 | AC-FIN-037, AC-FIN-038 | RULE-FIN-015 | ENT-FIN-008 | SCR-REQ-FIN-007 |
| US-FIN-012 | REQ-FIN-039 | AC-FIN-039 | — | ENT-FIN-005 | SCR-REQ-FIN-008 |
| US-FIN-013 | REQ-FIN-040 | AC-FIN-040 | RULE-FIN-006 | ENT-FIN-005 | SCR-REQ-FIN-009 |
| US-FIN-014 | REQ-FIN-041 | AC-FIN-041 | — | ENT-FIN-005 | SCR-REQ-FIN-010 |
| US-FIN-015 | REQ-FIN-042 | AC-FIN-042 | — | ENT-FIN-005 | SCR-REQ-FIN-011 |
| US-FIN-016 | REQ-FIN-043 | AC-FIN-043 | — | ENT-FIN-005, ENT-FIN-006 | SCR-REQ-FIN-012 |
| US-FIN-017 | REQ-FIN-044 | AC-FIN-044 | — | ENT-FIN-004 | — (onboarding, no screen) |
| US-FIN-018 | REQ-FIN-045 (MODIFIED v2) | AC-FIN-045 (MODIFIED v2) | — | ENT-FIN-001, ENT-FIN-004, ENT-FIN-007, ENT-FIN-008, ENT-FIN-009, ENT-FIN-010, ENT-FIN-011, ENT-FIN-013, ENT-FIN-015, ENT-FIN-016 | — (onboarding, no screen) |
| US-FIN-019 | REQ-FIN-046 | AC-FIN-046 | — | ENT-FIN-004, ENT-FIN-005 | SCR-REQ-FIN-008, SCR-REQ-FIN-009, SCR-REQ-FIN-010 |
| US-FIN-020 | REQ-FIN-047, REQ-FIN-048, REQ-FIN-050, REQ-FIN-045 | AC-FIN-047, AC-FIN-048, AC-FIN-050…052, AC-FIN-045 | RULE-FIN-018, RULE-FIN-020 | ENT-FIN-005, ENT-FIN-010, ENT-FIN-015 | SCR-REQ-FIN-003, SCR-REQ-FIN-006 |
| US-FIN-021 | REQ-FIN-049, REQ-FIN-050, REQ-FIN-051, REQ-FIN-061, REQ-FIN-045 | AC-FIN-049, AC-FIN-050, AC-FIN-051, AC-FIN-053, AC-FIN-065, AC-FIN-045 | RULE-FIN-019, RULE-FIN-020, RULE-FIN-021, RULE-FIN-024 | ENT-FIN-010, ENT-FIN-015 | SCR-REQ-FIN-003, SCR-REQ-FIN-006, SCR-REQ-FIN-013 |
| US-FIN-022 | REQ-FIN-049, REQ-FIN-050, REQ-FIN-051 | AC-FIN-049, AC-FIN-052, AC-FIN-053 | RULE-FIN-019, RULE-FIN-020, RULE-FIN-021 | ENT-FIN-010, ENT-FIN-015 | SCR-REQ-FIN-003, SCR-REQ-FIN-006 |
| US-FIN-023 | REQ-FIN-052, REQ-FIN-060 | AC-FIN-054, AC-FIN-055, AC-FIN-064 | RULE-FIN-007, RULE-FIN-023 | ENT-FIN-001, ENT-FIN-015 | SCR-REQ-FIN-006, SCR-REQ-FIN-013 |
| US-FIN-024 | REQ-FIN-051, REQ-FIN-053 | AC-FIN-053, AC-FIN-056, AC-FIN-057 | RULE-FIN-020, RULE-FIN-021 | ENT-FIN-010, ENT-FIN-015 | SCR-REQ-FIN-006 |
| US-FIN-025 | REQ-FIN-054, REQ-FIN-055, REQ-FIN-056, REQ-FIN-057, REQ-FIN-058, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061 | AC-FIN-058…065 | RULE-FIN-022, RULE-FIN-023, RULE-FIN-024 | ENT-FIN-015 | SCR-REQ-FIN-013 |
| US-FIN-026 | REQ-FIN-062, REQ-FIN-063, REQ-FIN-064, REQ-FIN-065, REQ-FIN-045 | AC-FIN-066…070, AC-FIN-045 | RULE-FIN-025, RULE-FIN-026, RULE-FIN-027 | ENT-FIN-006, ENT-FIN-016 | SCR-REQ-FIN-003, SCR-REQ-FIN-006 |
| US-FIN-027 | REQ-FIN-066, REQ-FIN-067 | AC-FIN-071, AC-FIN-072 | RULE-FIN-009 | ENT-FIN-003, ENT-FIN-006, ENT-FIN-016 | SCR-REQ-FIN-006 |
| US-FIN-028 | REQ-FIN-067, REQ-FIN-068, REQ-FIN-069 | AC-FIN-072, AC-FIN-073, AC-FIN-074 | RULE-FIN-009, RULE-FIN-021, RULE-FIN-027 | ENT-FIN-003, ENT-FIN-016 | SCR-REQ-FIN-006 |
| US-FIN-029 | REQ-FIN-070, REQ-FIN-071 | AC-FIN-075, AC-FIN-076 | — | ENT-FIN-005, ENT-FIN-006 | SCR-REQ-FIN-006, SCR-REQ-FIN-008, SCR-REQ-FIN-012 (the two report screens unchanged — they already aggregate every POSTED line) |
| US-FIN-030 | REQ-FIN-072 | AC-FIN-077, AC-FIN-078 | RULE-FIN-004 | ENT-FIN-004 | SCR-REQ-FIN-006 |
| US-FIN-031 | REQ-FIN-073, REQ-FIN-074, REQ-FIN-075 | AC-FIN-079, AC-FIN-080, AC-FIN-081 | RULE-FIN-008, RULE-FIN-028 | ENT-FIN-004, ENT-FIN-008 | SCR-REQ-FIN-006 |
| US-FIN-032 | REQ-FIN-076, REQ-FIN-077 | AC-FIN-082, AC-FIN-083 | RULE-FIN-003, RULE-FIN-004, RULE-FIN-006, RULE-FIN-007, RULE-FIN-008, RULE-FIN-009, RULE-FIN-010, RULE-FIN-011, RULE-FIN-016 | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | SCR-REQ-FIN-006 |
| US-FIN-033 | — DEFERRED (out of v2 scope, ADR-FIN-012) | — | — | — | — |

Plus REQ-FIN-017 through REQ-FIN-021 (unified validation, traced to US-FIN-004/005/006/007/009
collectively — see each REQ's own Traces line) and RULE-FIN-006 through RULE-FIN-009 (the four
post-time checks). Every non-deferred story traces to ≥1 REQ; every REQ traces to ≥1 story; every AC
to ≥1 REQ; every RULE to ≥1 REQ; every SCR-REQ to ≥1 REQ. US-FIN-033 is DEFERRED in the approved PRD
and deliberately has no REQ. No orphan, no dangling id.

## Decisions applied
| DEFAULT / ADR | What | Source | Override / status |
|---|---|---|---|
| DEFAULT (v1) | `amount` type `NUMERIC(18,4)` | [KB:erp-domain-standards §6] platform default for money | non-breaking |
| DEFAULT (v1) | RecurringTemplateLine/AllocationTarget carry a single optional dimension value, not the header's full multi-dimension model | v1 P1, applying §9.3.3 "as data" pragmatically | non-breaking; extend to a full JournalLineDimension-style child table if multi-dimension templates are needed later |
| DEFAULT (v2) | AccountMapping is soft-deleted through `isActiveFl`, never hard-deleted | [KB:erp-domain-standards §6]; module-registry v2 AUTO-DECISION | hard delete with audit record |
| DEFAULT (v2) | Account-mapping search is server-side with paging (page size 20, max 200) | [KB:erp-domain-standards §6] | client-side filtering |
| DEFAULT (v2) | The legacy "cash unless cheque" behaviour becomes one explicit mapping row per payment-method value; no default account exists anywhere | CS-FIN-001 §1 "never a default"; POL-FIN-024; module-registry v2 AUTO-DECISION | none — a default is forbidden by the change request |
| ADR-FIN-023 | MAPPING rule line names its business field in `accountBusinessFieldCode`; `accountDerivationValue` = constant account code only | this stage | ACCEPTED (non-breaking) |
| ADR-FIN-024 | AccountMapping references its account by id; key fixed after create; update changes the account only | this stage | ACCEPTED (non-breaking) |
| ADR-FIN-025 | Dimension tags are submitted with their rule line and follow its lifecycle | this stage | ACCEPTED (non-breaking) |
| ADR-FIN-026 | A business-field dimension value resolves by exact `DimensionValue.code` within the tagged dimension | this stage | ACCEPTED (non-breaking) |
| ADR-FIN-027 | Four new distinct rejection errors; FIN-409-DUPLICATE-EVENT and FIN-409-PERIOD-NOT-OPEN confirmed unchanged; the MAPPING-unsupported code retired | this stage | ACCEPTED (non-breaking) |
| ADR-FIN-010 (applied) | DIRECT removed outright; dimensions use FIN_DIMENSION_VALUE_SOURCE | P0 v2 dialogue | RESOLVED-IN-DIALOGUE; confirmed at prd-approval |
| ADR-FIN-011 (applied) | A missing or invalid dimension value rejects the event | P0 v2 dialogue | RESOLVED-IN-DIALOGUE |
| ADR-FIN-012 (applied) | Legacy dimension-code translation deferred (US-FIN-033) | P0 v2 dialogue | RESOLVED-IN-DIALOGUE; confirmed at prd-approval |
| ADR-FIN-013 (applied) | Business-field names come from FIN_EVENT_BUSINESS_FIELD | P0 v2 dialogue | RESOLVED-IN-DIALOGUE; confirmed at prd-approval |
| ADR-FIN-014 (applied) | One period-not-open code for every existing non-open period; a date with no period is a configuration error | P0 v2 dialogue | RESOLVED-IN-DIALOGUE |
| ADR-FIN-020 (applied) | v2 roles reuse v1 role names (finance administrator, financial controller); event integration operator is narrative only | P0.5 v2 dialogue | RESOLVED-IN-DIALOGUE |
| ADR-FIN-021 (applied) | A mapping change never alters a posted entry (REQ-FIN-058) | P0.5 v2 dialogue | RESOLVED-IN-DIALOGUE |
| ADR-FIN-037 | Event business fields travel in `fields.<FIN_EVENT_BUSINESS_FIELD code>` (e.g. `fields.PAYMENT_METHOD`), never as a bare field named after the fact itself; every affected AC re-emitted to match; RULE-FIN-008's wording changed from "may" to "runs only after" with no behaviour change | pass-1 review (merge-review-notes) | ACCEPTED (non-breaking) |
| ADR-FIN-038 | ERP-1's master `entity_defaults` are adapted for ENT-FIN-007 (code is its own label, no separate name) and ENT-FIN-008 (periodNo+fiscalYearId is the key, state lives in statusCode) — v1 debt, recorded rather than schema-changed | pass-1 review | ACCEPTED (non-breaking) |
| ADR-FIN-039 | AccountMapping.businessValue matching is exact-code with leading/trailing blanks trimmed on save and on event read, for every business field, not PAYMENT_METHOD alone | pass-1 review | ACCEPTED (non-breaking) |

No BLOCKED ADR: every v2 choice is additive and contradicts no approved story, policy, registry fact or
closed decision.

## Access summary
| Page code | Screen | VIEW | CREATE | UPDATE | DELETE | Custom |
|---|---|---|---|---|---|---|
| FIN_ACCOUNTS | Chart of accounts | role-granted | role-granted | role-granted (incl. deactivate) | — | — |
| FIN_DIMENSIONS | Dimensions | role-granted | role-granted | role-granted (dimension-VALUE deactivate only — API-FIN-035; `PERM_FIN_DIMENSIONS_UPDATE`, registered and granted by V28) | — | — |
| FIN_RULES | Engine rules | role-granted | role-granted | role-granted (add line — API-FIN-011, v2: with its dimension tags; and deactivate rule — API-FIN-034; one permission, `PERM_FIN_RULES_UPDATE`) | — | — |
| FIN_RECURRING_TEMPLATES | Recurring/reversing templates | role-granted | role-granted | role-granted (run — API-FIN-014; and deactivate template — API-FIN-036; one permission, `PERM_FIN_RECURRING_TEMPLATES_UPDATE`) | — | — |
| FIN_ALLOCATION_RULES | Allocation rules | role-granted | role-granted | role-granted (run — API-FIN-017; and deactivate rule — API-FIN-037; one permission, `PERM_FIN_ALLOCATION_RULES_UPDATE`) | — | — |
| FIN_JOURNAL_ENTRIES | Journal entries | role-granted | role-granted (incl. Post) | — | — | Reverse (`PERM_FIN_JOURNAL_ENTRIES_REVERSE`) |
| FIN_PERIODS | Fiscal periods & years | role-granted (gateway, and now also the read endpoint API-FIN-033) | role-granted (year) | role-granted (open/soft-close) | — | Close-approve (`PERM_FIN_PERIODS_CLOSE_APPROVE` — the distinct permission RULE-FIN-015 requires. Since V30 it is granted to `SYS_ADMIN`, so the bootstrap `admin` can close; V27's dedicated `FIN_CLOSE_APPROVER` role remains valid as a least-privilege alternative but is no longer required — see the SoD note below) |
| FIN_ACCOUNT_LEDGER | Account ledger | role-granted | — | — | — | — |
| FIN_TRIAL_BALANCE | Trial balance | role-granted | — | — | — | — |
| FIN_BALANCE_SHEET | Balance sheet | role-granted | — | — | — | — |
| FIN_INCOME_STATEMENT | Income statement | role-granted | — | — | — | — |
| FIN_DIMENSION_REPORTS | Dimension reports | role-granted | — | — | — | — |
| FIN_ACCOUNT_MAPPINGS (v2 ADDED) | Account mappings | finance administrator | finance administrator | finance administrator (change account, and deactivate) | — | — |
Every action beyond VIEW additionally requires VIEW on the same screen (platform gateway
convention, `profiles/erp.yaml → conventions.security_model.gateway_action`, enforced by
SEC's own mechanism — not restated as a FIN-owned RULE). The new page's permissions are derived by SEC
from its page code; this SRS enumerates no permission seed data for it.

**DELETE column, as built**: empty for every FIN screen. FIN publishes no `DELETE` endpoint;
deactivation, where it exists, is `PUT /{id}/deactivate` gated by the screen's UPDATE permission,
and V24 seeds no `PERM_FIN_*_DELETE` row (nor does V28, which adds only
`PERM_FIN_DIMENSIONS_UPDATE`). The DELETE ✓ marks above were pre-implementation and were corrected
at ALIGN-BE. v2 keeps this: FIN_ACCOUNT_MAPPINGS deactivates under UPDATE and has no DELETE.

**Deactivate endpoints, as built**: five — API-FIN-004 (account), API-FIN-034 (event-type rule), the
dimension-value one API-FIN-035, API-FIN-036 (recurring/reversing template) and API-FIN-037
(allocation rule). None has an `activate` counterpart, following the delivered
`AccountService.deactivate` precedent. Each is `PUT /{id}/deactivate` gated by its screen's UPDATE
permission; API-FIN-036 and API-FIN-037 reuse `PERM_FIN_RECURRING_TEMPLATES_UPDATE` and
`PERM_FIN_ALLOCATION_RULES_UPDATE`, both already seeded by V24 and already granted by V25's blanket
Tier-3 grant, so neither needed a new permission, a new error code or a migration. Deliberately NOT
built, and not pending: a deactivate on the parent `Dimension` (no REQ/AC/RULE requires it and
`Dimension.isActiveFl` drives no behaviour) and a rule-line delete (ENT-FIN-010 has no active-flag
column and FIN publishes no `DELETE` endpoint on any screen) — see SCR-REQ-FIN-002 §B4 and
SCR-REQ-FIN-003 §B4. v2 adds a sixth deactivate, on the account mapping (SCR-REQ-FIN-013), with no
`activate` counterpart either (ADR-FIN-024).

**DEFECT CLOSED 2026-09-12 — on those two screens, deactivating now DOES stop the run.** This
supersedes the entry that used to stand here reporting the run as ungated. `RecurringTemplateDomain
.assertCanRun()` (a new Domain companion) and `AllocationRuleDomain.assertCanRun()` are each called
first thing in their service's `run`, so a deactivated template or allocation rule is refused with
`FIN-409-NOT-ACTIVE` (409) and posts nothing. The UPDATE cells above cover run AND deactivate for
these two screens, and "deactivate" there now means a real gate, not only a record-keeping state.
**The gate is a recorded human decision, not stated requirement: no RULE-FIN-* asks for it**, and
closing the gap required inventing an error code, status and ar+en messages that no REQ, AC or RULE
provides — do not read `FIN-409-NOT-ACTIVE` back out of these tables as pre-existing spec. See
SCR-REQ-FIN-004 §B4 and SCR-REQ-FIN-005 §B4.

**SoD on FIN_PERIODS — what was removed, and what still holds (2026-09-12).** RULE-FIN-015 stands
and is enforced by the `PERM_FIN_PERIODS_CLOSE_APPROVE` gate alone, which is exactly what the rule
asks for. A stricter, unrequested check — FIN service code that refused the close for every caller
whenever any single user in the system held both that permission and
`PERM_FIN_JOURNAL_ENTRIES_CREATE` — was deleted by recorded human decision, together with the SEC
user-directory read behind it; `FIN-403-SOD-VIOLATION` is struck as unreachable. Migration V30 then
granted `PERM_FIN_PERIODS_CLOSE_APPROVE` to `SYS_ADMIN`, which V25 had deliberately withheld, so
the bootstrap `admin` can now close. Note that V27's header, being applied and immutable, still
describes the removed mechanism as live in both its numbered "OPERATIONAL PRECONDITIONS" — both are
now false; see the SEC-BE phase document for the correction in full.

**OPEN DEFECT — `update` is still missing on the same two screens.** This is now the only open half
of the original gap: a template or allocation rule created with wrong content can be retired — and,
since 2026-09-12, retiring it does stop it running — but it still cannot be corrected. Neither update was built because a
correct one must decide the fate of the aggregate's children — a template's existing lines, a
rule's existing targets and hence RULE-FIN-003's remainder-target set — which is a design question
no REQ, AC or RULE answers. Recorded as a known gap, not a reasoned exclusion. See SCR-REQ-FIN-004
§B4 and SCR-REQ-FIN-005 §B4.
══════════════════════════════════════════════════════════════════
