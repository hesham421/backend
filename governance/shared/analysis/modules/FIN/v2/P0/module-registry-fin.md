## MODULE REGISTRY — الحسابات العامة / Finance (General Ledger) (FIN) — DELTA v2
══════════════════════════════════════════════════════════════════
Module Code    : FIN   (profile.vocabulary.module_prefixes)
Bounded context: finance
Layer / Type   : L3 / transactional + reporting (دون تغيير عن v1)     Execution tier : 2.2 (دون تغيير) [KB:erp-domain-standards §1]
Source         : EXTENDED from prior registry (v1)
Knowledge      : [KB:erp-domain-standards §1–§6]؛ domain-profile §5 (G1، G4، G6، G7، G9)، §9 (R3)؛ change manifest FIN v2
Readiness      : READY — الحوار (claude:opus ↔ claude:sonnet) تقارب في الجولة 3 على التوصيات الخمس؛ بانتظار تأكيد المستخدم النهائي
══════════════════════════════════════════════════════════════════

CHANGE SUMMARY
المضاف: كيانان (ربط الحساب، بُعد سطر القاعدة)، ونوعا لوكب، وقيمة لوكب مخصّصة واحدة، وسياسات POL-FIN-021 حتى POL-FIN-032.
المعدَّل: سطر القاعدة (أنماط الاشتقاق)، بُعد سطر القيد (يكتبه محرك القواعد أيضًا)، نوع اللوكب ACCOUNT_DERIVATION_TYPE.
المحذوف (محسوم بالحوار): القيمة DIRECT من ACCOUNT_DERIVATION_TYPE — إزالة كاملة، لا تقييد ولا إهمال تدريجي.

ENTITIES OWNED — ADDED   (names only — entity IDs are assigned by P1)
| Entity (ar/en) | Kind | PRIVATE / SHARED | Source |
|---|---|---|---|
| ربط الحساب / AccountMapping | config | PRIVATE | change manifest FIN v2 §1 — مخزن يربط قيمة تجارية لحقل تجاري مسمّى، لكل نوع حدث، بحساب؛ مع علامة نشاط وحقول تدقيق وفق أعراف FIN |
| بُعد سطر القاعدة / RuleLineDimension | config | PRIVATE | change manifest FIN v2 §2 — لكل بُعد يصرّح به سطر القاعدة: القيمة إما ثابتة وإما مأخوذة من حقل تجاري مسمّى في الحدث |

ENTITIES OWNED — MODIFIED
| Entity (ar/en) | Kind | PRIVATE / SHARED | What changes | Source |
|---|---|---|---|---|
| سطر القاعدة / RuleLine | config | PRIVATE | نمط الاشتقاق MAPPING يصبح فعّالًا (كان مُصرَّحًا به ويرفض التنفيذ)؛ DIRECT يُحذف نهائيًا لاشتقاق الحساب؛ يحمل وسوم الأبعاد عبر بُعد سطر القاعدة | change manifest FIN v2 §1، §2 |
| بُعد سطر القيد / JournalLineDimension | transactional | PRIVATE | لا تغيير بنيوي؛ يكتبه محرك القواعد الآن للسطور المبنية من الأحداث بالشكل نفسه المستخدم للسطور اليدوية | change manifest FIN v2 §2 |

LOOKUPS OWNED — ADDED   (تُسجَّل في MDL كبيانات؛ مجموع مفاتيح FIN يصبح 15 بدل 13)
| Lookup key | Description | Initial values (only those the user named) | Source |
|---|---|---|---|
| FIN_EVENT_BUSINESS_FIELD | الحقول التجارية في الحدث التي يجوز لسطر القاعدة قراءتها لربط الحساب أو لقيمة البُعد | طريقة الدفع / payment method · رمز نوع المصروف / expense type code · رمز المؤسسة / organisation code · رمز الفرع / branch code | change manifest FIN v2 §1، §2؛ domain-profile G1، G4 |
| FIN_DIMENSION_VALUE_SOURCE | مصدر قيمة البُعد على سطر القاعدة | ثابت / constant · حقل تجاري / business field | change manifest FIN v2 §2 |
Rule (profile): all LOV values runtime-loaded from the lookup module; no hardcoded enums in APIs or field specs

LOOKUPS OWNED — MODIFIED
| Lookup key | Description | Change | Source |
|---|---|---|---|
| ACCOUNT_DERIVATION_TYPE | نوع اشتقاق الحساب / account derivation type | القيم في v1: CONSTANT، DIRECT، MAPPING → في v2: CONSTANT، MAPPING. تُحذف DIRECT نهائيًا (لا تعطيل ولا تقييد)؛ MAPPING يصبح فعّالًا | change manifest FIN v2 §1؛ domain-profile G6؛ v1 SCOPE EXCEPTIONS "Account numbers in event payloads — never" |
| PAYMENT_METHOD | طريقة الدفع / payment method | قيمة مخصّصة سمّاها المستخدم: شيك / cheque (كان v1 بلا قيم مسمّاة) | change manifest FIN v2 §1 "when the payment was by cheque" |

LOOKUPS CONSUMED
دون تغيير عن v1.

SHARED ENTITIES CONSUMED — MODIFIED usage
| Entity | Owner code | HARD-FK / SOFT-READ | Why |
|---|---|---|---|
| ScreenRegistry / ActionRegistry | SEC | HARD-FK (تسجيل كبيانات) — النوع كما في v1 | تسجيل شاشة روابط الحسابات وإجراءاتها كبيانات [domain-profile G2، G3] |
| LookupType / LookupValue | MDL | HARD-FK (تسجيل + قراءة) — النوع كما في v1؛ الشكل الفيزيائي SOFT-READ وفق ADR-FIN-001 | تسجيل نوعَي اللوكب الجديدين وقيمة PAYMENT_METHOD المضافة وقراءة القيم [domain-profile G4، G5] |

DEPENDENCIES
| Module code | HARD / SOFT / LOOKUP | What is consumed |
|---|---|---|
| SEC | HARD | دون تغيير في النوع؛ + تسجيل شاشة روابط الحسابات وإجراءاتها |
| MDL | HARD | دون تغيير في النوع؛ + نوعا لوكب جديدان وقيمة لوكب مضافة |
External (not registry modules): host (Oracle القديم) — EVENT — الحدث المحاسبي القياسي فقط؛ وقائع تجارية، لا رمز حساب.
ROOT: NO

POLICIES ADDED (defined in business-policies-fin.md)
| Policy | Subject |
|---|---|
| POL-FIN-021 | لا رمز حساب في حمولة الحدث |
| POL-FIN-022 | اشتقاق الحساب بالربط |
| POL-FIN-023 | التحقق من الحساب المُشتق بالربط |
| POL-FIN-024 | رفض القيمة غير المربوطة |
| POL-FIN-025 | الروابط بيانات لا شيفرة |
| POL-FIN-026 | وسوم الأبعاد على سطور القاعدة |
| POL-FIN-027 | التحقق من قيم الأبعاد المشتقة |
| POL-FIN-028 | رفض قيمة البُعد غير القابلة للتحديد |
| POL-FIN-029 | شكل أبعاد موحّد |
| POL-FIN-030 | خطأ الحدث المكرر دائمًا |
| POL-FIN-031 | رمز مميز لرفض الفترة غير المفتوحة |
| POL-FIN-032 | ثوابت v1 على المسار الجديد |

RELATION TO v1 POLICIES (v1 text unchanged — none superseded)
| v2 policy | v1 policy | Relation |
|---|---|---|
| POL-FIN-021 | POL-FIN-014، POL-FIN-020 | يطبّق الحدّين على اشتقاق الحساب؛ ويحقّق استثناء النطاق في v1 "Account numbers in event payloads — never" الذي كانت القيمة DIRECT تناقضه |
| POL-FIN-023 | POL-FIN-003 | التحقق نفسه على الحساب المُشتق بالربط |
| POL-FIN-027 | POL-FIN-011 | الأبعاد جزء من هوية الترحيل للسطور المبنية من الأحداث أيضًا |
| POL-FIN-030 | POL-FIN-012 | يحدّد الجواب: خطأ الحدث المكرر أيًّا كان موضع اكتشاف التكرار |
| POL-FIN-031 | POL-FIN-004 | يحدّد الجواب عند فشل بوابة الفترة: رمز مميز قابل لإعادة المحاولة |
| POL-FIN-032 | POL-FIN-001، POL-FIN-003، POL-FIN-004، POL-FIN-005، POL-FIN-006، POL-FIN-009، POL-FIN-012، POL-FIN-013 | يسري كل منها على المسار الجديد دون إعادة فتح |

AUTO-DECISIONS
AUTO: ربط الحساب من نوع config وPRIVATE  FROM: STEP 1 — سابقة v1 (قاعدة نوع الحدث وسطر القاعدة من نوع config وPRIVATE)  IF WRONG: master
AUTO: بُعد سطر القاعدة من نوع config وPRIVATE  FROM: STEP 1 — سابقة v1 (سطر القاعدة config)  IF WRONG: دمجه في سطر القاعدة بدل كيان مستقل
AUTO: ربط نشط واحد فقط لكل (نوع حدث، حقل تجاري، قيمة تجارية)  FROM: STEP 4 — change manifest §1 "never a guess": تعدد الروابط تخمين  IF WRONG: إضافة سطر القاعدة إلى مفتاح الربط حين يحتاج سطران من القاعدة نفسها حسابين مختلفين للقيمة نفسها
AUTO: الربط يُعطَّل ولا يُحذف فعليًا  FROM: STEP 3 — [KB:erp-domain-standards §6] soft delete  IF WRONG: حذف فعلي مع سجل تدقيق
AUTO: تعديل الربط يسري على الاشتقاقات اللاحقة فقط ولا يمس القيود المُرحَّلة  FROM: STEP 4 — domain-profile G9 + ثبات القيد المُرحَّل (acceptance 7)  IF WRONG: لا بديل — يتعارض مع ثابت v1
AUTO: القيمة التجارية تُخزَّن كرمز لا كنص عرض؛ حين يكون للحقل التجاري لوكب تملكه FIN (طريقة الدفع → PAYMENT_METHOD) تكون القيمة رمزًا من ذلك اللوكب، وإلا (رمز نوع المصروف) تُخزَّن رمزَ المضيف كما هو دون إنشاء لوكب له في FIN  FROM: STEP 1 — v1 LOOKUPS OWNED (PAYMENT_METHOD)؛ STEP 3 — [KB:erp-domain-standards §2.3]؛ domain-profile G6 (FIN لا تملك معنى عالم المضيف)  IF WRONG: قيمة حرّة لطريقة الدفع أيضًا
AUTO: السلوك الافتراضي في النظام القديم (نقد ما لم يكن شيكًا) يصبح صف ربط صريحًا لكل قيمة طريقة دفع؛ لا حساب افتراضي  FROM: change manifest §1 "never a default"؛ POL-FIN-024  IF WRONG: لا بديل — القيمة الافتراضية ممنوعة نصًّا
AUTO: إدارة الروابط عبر شاشة FIN مركّبة واحدة  FROM: STEP 3 — [KB:erp-domain-standards §4]  IF WRONG: شاشتان منفصلتان
AUTO: أسماء رموز الأخطاء (قيمة غير مربوطة، فترة غير مفتوحة) تُحدَّد في P1/P3.1  FROM: change manifest §1 "FIN's analysis owns its name"؛ حدود المرحلة §7  IF WRONG: —

RESOLVED DECISIONS (dialogue, this module — CONVERGED round 3)
| # | Point | Recommended | Confirmed by user | Sources |
|---|---|---|---|---|
| v2-1 | مصير نمط DIRECT لاشتقاق الحساب | حذف نهائي (لا تقييد ولا إهمال تدريجي)؛ الأبعاد تستخدم مصدر قيمة خاصًا بها (ثابت / حقل تجاري) | — طلب المستخدم قرارًا مسجَّلًا (ADR)؛ بانتظار تأكيده | domain-profile G6؛ change manifest §1؛ v1 SCOPE EXCEPTIONS؛ project-registry PIPELINE STATUS |
| v2-2 | قيمة بُعد غائبة أو غير صالحة | رفض الحدث وعدم ترحيل أي قيد | نعم ضمنًا — نص الطلب §2 "the same discipline as accounts" و"the existing validation is not bypassed" | change manifest §2، acceptance 4؛ POL-FIN-024 |
| v2-3 | ترجمة رموز الأبعاد القديمة | تأجيل؛ النمطان المطلوبان فقط (ثابت / حقل تجاري) | — بانتظار تأكيد المستخدم | change manifest §2؛ domain-profile G6 |
| v2-4 | مرجعية أسماء الحقول التجارية | نوع لوكب FIN_EVENT_BUSINESS_FIELD مملوك لـ FIN في MDL | — بانتظار تأكيد المستخدم | domain-profile G1، G4، G5؛ [KB:erp-domain-standards §2 rule 3، §4] |
| v2-5 | نطاق رمز رفض الفترة غير المفتوحة | رمز واحد لكل فترة موجودة لا تقبل الترحيل؛ التاريخ بلا فترة معرَّفة خطأ إعداد | نعم ضمنًا — نص الطلب §4 "distinct from validation and configuration errors" | change manifest §4؛ domain-profile §7.1؛ [KB:erp-domain-standards §6] |
══════════════════════════════════════════════════════════════════
