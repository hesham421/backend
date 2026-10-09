## BUSINESS POLICIES — الحسابات العامة / Finance (General Ledger) (FIN) — DELTA v2
══════════════════════════════════════════════════════════════════
Module   : FIN     Source of truth : user change request (FIN v2 change manifest) + dialogue resolutions
Read by  : P0.5 (every user story cites the policies it serves)
Delta    : السياسات المضافة تُكمل تسلسل v1 (انتهى v1 عند السياسة العشرين)؛ لا إعادة ترقيم ولا إعادة
           استخدام؛ لا تُلغى أي سياسة من v1 ولا يتغير نصّها — العلاقة بسياسات v1 مذكورة في Source لكل سياسة
Dialogue : analysis-dialogue (claude:opus ↔ claude:sonnet) — round 3 — CONVERGED؛ بانتظار تأكيد المستخدم النهائي
══════════════════════════════════════════════════════════════════

CLIENT-SPECIFIC POLICIES — ADDED

POL-FIN-021 — لا رمز حساب في حمولة الحدث / No account code in the event payload
  Statement : [en] The system shall derive every account of a journal entry built from a canonical accounting event without reading an account code from the event payload.
              [ar] يجب على النظام أن يشتق كل حساب في قيد اليومية المبني من حدث محاسبي قياسي دون قراءة أي رمز حساب من حمولة الحدث.
  Pattern   : ubiquitous
  Trigger   : Post (event)
  Rationale : الحدث يصرّح بواقعة تجارية وFIN وحدها تقرر معناها المحاسبي
  Source    : change manifest FIN v2 — الحد الحاكم + acceptance 1؛ domain-profile G6؛ يطبّق POL-FIN-014 وPOL-FIN-020
              على اشتقاق الحساب ويحقّق استثناء النطاق في v1 "Account numbers in event payloads — never"
  Status    : CONFIRMED

POL-FIN-022 — اشتقاق الحساب بالربط / Account derivation by mapping
  Statement : [en] When a rule line derives its account by mapping, the system shall resolve the account from the value of the named business field in the event through the FIN account mapping store for that event type.
              [ar] عندما يشتق سطر القاعدة حسابه بالربط، يجب على النظام أن يحدد الحساب من قيمة الحقل التجاري المسمّى في الحدث عبر مخزن ربط الحسابات في FIN لنوع الحدث ذاك.
  Pattern   : event
  Trigger   : Post (event)
  Rationale : النقد مقابل البنك حسب طريقة الدفع (11 من 16 فرعًا قديمًا) والحساب حسب نوع المصروف
  Source    : change manifest FIN v2 §1، acceptance 1
  Status    : CONFIRMED

POL-FIN-023 — التحقق من الحساب المُشتق بالربط / Mapped account validated like a constant one
  Statement : [en] When an account is resolved through the account mapping store, the system shall validate it as a leaf, active and postable account exactly as it validates an account given as a constant.
              [ar] عندما يُحدَّد حساب عبر مخزن ربط الحسابات، يجب على النظام أن يتحقق من أنه حساب ورقي ونشط وقابل للترحيل بالطريقة نفسها التي يتحقق بها من الحساب المُعطى كقيمة ثابتة.
  Pattern   : event
  Trigger   : Post (event)
  Rationale : المسار الجديد لا يفتح ثغرة في تحقق شجرة الحسابات
  Source    : change manifest FIN v2 §1؛ يطبّق POL-FIN-003 على الحساب المُشتق بالربط
  Status    : CONFIRMED

POL-FIN-024 — رفض القيمة غير المربوطة / Unmapped business value rejected
  Statement : [en] If the business value carried by the event has no active account mapping for that event type and business field, then the system shall reject the event with a dedicated unmapped-value error and post no journal entry.
              [ar] إذا لم يكن للقيمة التجارية التي يحملها الحدث ربط حساب نشط لنوع الحدث والحقل التجاري ذاتهما، فعندئذٍ يجب على النظام أن يرفض الحدث بخطأ مخصّص للقيمة غير المربوطة وألّا يرحّل أي قيد يومية.
  Pattern   : unwanted
  Trigger   : Post (event)
  Rationale : لا قيمة افتراضية ولا تخمين أبدًا؛ السلوك الافتراضي في النظام القديم (نقد ما لم يكن شيكًا) يصبح صف ربط
              صريحًا لكل قيمة طريقة دفع، يُعبَّأ ضمن بيانات الترحيل
  Source    : change manifest FIN v2 §1، acceptance 2
  Status    : CONFIRMED

POL-FIN-025 — الروابط بيانات لا شيفرة / Account mappings are data, not code
  Statement : [en] The system shall let authorised users add, change and deactivate account mappings as FIN-owned data through a FIN screen, without any code change.
              [ar] يجب على النظام أن يتيح للمستخدمين المخوَّلين إضافة روابط الحسابات وتعديلها وإيقافها كبيانات مملوكة لـ FIN عبر شاشة في FIN، دون أي تعديل في الشيفرة.
  Pattern   : ubiquitous
  Trigger   : Create / Update / Deactivate
  Rationale : كل ما يمكن أن يتغير بيانات مُعرَّفة، كسائر إعدادات FIN
  Source    : change manifest FIN v2 §1، acceptance 3؛ domain-profile G1
  Status    : CONFIRMED

POL-FIN-026 — وسوم الأبعاد على سطور القاعدة / Dimension tags on rule-driven lines
  Statement : [en] Where a rule line declares a value for a dimension, the system shall set that dimension on the journal line it builds from the rule's constant value or from the value of the named business field in the event.
              [ar] حيثما يصرّح سطر القاعدة بقيمة لبُعد ما، يجب على النظام أن يضبط ذلك البُعد على سطر القيد الذي يبنيه من القيمة الثابتة في القاعدة أو من قيمة الحقل التجاري المسمّى في الحدث.
  Pattern   : optional
  Trigger   : Post (event)
  Rationale : النظام القديم يختم المؤسسة والفرع على كل قيد؛ بدونها تفشل المقارنة المتوازية وتفرغ تقارير الأبعاد
  Source    : change manifest FIN v2 §2، acceptance 4
  Status    : CONFIRMED

POL-FIN-027 — التحقق من قيم الأبعاد المشتقة / Event-derived dimension values validated
  Statement : [en] When a dimension value is resolved for a journal line built from an event, the system shall validate that the value belongs to that dimension and is active, applying the same dimension rules as for a manually built line.
              [ar] عندما تُحدَّد قيمة بُعد لسطر قيد مبني من حدث، يجب على النظام أن يتحقق من أن القيمة تنتمي إلى ذلك البُعد وأنها نشطة، مطبّقًا قواعد الأبعاد نفسها المطبّقة على السطر المُدخل يدويًا.
  Pattern   : event
  Trigger   : Post (event)
  Rationale : تحقق الأبعاد القائم لا يُتجاوز
  Source    : change manifest FIN v2 §2؛ يمدّ POL-FIN-011 (الأبعاد جزء من هوية الترحيل) إلى السطور المبنية من الأحداث
  Status    : CONFIRMED

POL-FIN-028 — رفض قيمة البُعد غير القابلة للتحديد / Unresolvable dimension value rejected
  Statement : [en] If a dimension value declared by a rule line is missing from the event or fails dimension validation, then the system shall reject the event and post no journal entry.
              [ar] إذا كانت قيمة البُعد التي يصرّح بها سطر القاعدة غائبة عن الحدث أو فشلت في التحقق، فعندئذٍ يجب على النظام أن يرفض الحدث وألّا يرحّل أي قيد يومية.
  Pattern   : unwanted
  Trigger   : Post (event)
  Rationale : القيد بلا إسناد فرع يُفسد المقارنة المتوازية كما يفسدها الحساب الخاطئ
  Source    : change manifest FIN v2 §2 "using the same discipline as accounts" و"the existing validation is not
              bypassed"، مع §1 "never a default, never a guess"؛ resolved decision v2-2
  Status    : CONFIRMED

POL-FIN-029 — شكل أبعاد موحّد / One dimension shape for all lines
  Statement : [en] The system shall record the dimension values of event-built journal lines in the same form as those of manually built journal lines, so that ledger and dimension reports treat both identically.
              [ar] يجب على النظام أن يسجّل قيم الأبعاد لسطور القيد المبنية من الأحداث بالشكل نفسه المستخدم لسطور القيد المُدخلة يدويًا، بحيث تعاملها تقارير الأستاذ والأبعاد معاملة واحدة.
  Pattern   : ubiquitous
  Trigger   : Post (event)
  Rationale : تقارير الأبعاد يجب ألّا تفرّق بين مصدر القيد
  Source    : change manifest FIN v2 §2
  Status    : CONFIRMED

POL-FIN-030 — خطأ الحدث المكرر دائمًا / Duplicate event always answered as duplicate
  Statement : [en] If a canonical accounting event arrives with an event reference that is already recorded, then the system shall answer the duplicate-event error whether the duplicate is detected by the pre-check or by the uniqueness guarantee of the store.
              [ar] إذا وصل حدث محاسبي قياسي بمرجع حدث مسجَّل مسبقًا، فعندئذٍ يجب على النظام أن يجيب بخطأ الحدث المكرر سواء اكتُشف التكرار بالفحص المسبق أو بضمان التفرّد في المخزن.
  Pattern   : unwanted
  Trigger   : Post (event)
  Rationale : التكرار نجاح عند المستهلك؛ الخطأ العام من فئة 500 يسبب إعادة محاولة وطابور رسائل ميتة ومطابقة يدوية
  Source    : change manifest FIN v2 §3، acceptance 5؛ domain-profile R3؛ يحدّد جواب الرفض الذي يفرضه POL-FIN-012
  Status    : CONFIRMED

POL-FIN-031 — رمز مميز لرفض الفترة غير المفتوحة / Distinct code for a non-open period
  Statement : [en] If an event posting targets an accounting period that is not open for posting, then the system shall reject it with a distinct, catalogued and documented period-not-open error that is separate from every validation and configuration error.
              [ar] إذا استهدف ترحيل حدث فترة محاسبية غير مفتوحة للترحيل، فعندئذٍ يجب على النظام أن يرفضه برمز خطأ مميز ومفهرس وموثَّق للفترة غير المفتوحة، منفصل عن كل أخطاء التحقق والإعداد.
  Pattern   : unwanted
  Trigger   : Post (event)
  Rationale : الفترة المغلقة حالة تشغيلية تزول بفتح الفترة؛ على المستهلك الانتظار وإعادة المحاولة لا وسم الحدث فاشلًا. تاريخ بلا فترة معرَّفة أصلًا خطأ إعداد منفصل، لا يُعالَج بإعادة المحاولة
  Source    : change manifest FIN v2 §4، acceptance 6؛ resolved decision v2-5؛ يحدّد جواب بوابة الفترة التي يفرضها POL-FIN-004
  Status    : CONFIRMED

POL-FIN-032 — ثوابت v1 على المسار الجديد / v1 invariants hold on the new path
  Statement : [en] The system shall apply every v1 posting invariant, namely balanced debits and credits, leaf and active accounts only, the period gate at post time, positive amounts with direction, the single remainder line, duplicate rejection, derived balances and the immutable audit trail, to journal entries built through account mapping or dimension tags.
              [ar] يجب على النظام أن يطبّق كل ثوابت الترحيل في v1، وهي تساوي المدين والدائن، والحسابات الورقية النشطة فقط، وبوابة الفترة عند الترحيل، والمبالغ الموجبة مع الاتجاه، وسطر الفرق الواحد، ورفض التكرار، والأرصدة المشتقة، وسجل التدقيق غير القابل للتعديل، على قيود اليومية المبنية عبر ربط الحسابات أو وسوم الأبعاد.
  Pattern   : ubiquitous
  Trigger   : Post (event)
  Rationale : ثوابت v1 موروثة ولا يُعاد فتحها
  Source    : change manifest FIN v2 acceptance 7؛ domain-profile G7، G9؛ سياسات v1 المعنية: POL-FIN-001، POL-FIN-003،
              POL-FIN-004، POL-FIN-005، POL-FIN-006، POL-FIN-009، POL-FIN-012، POL-FIN-013
  Status    : CONFIRMED

CUSTOM LOOKUP VALUES — ADDED
| Lookup key | Added values | Source |
|---|---|---|
| FIN_EVENT_BUSINESS_FIELD | طريقة الدفع / payment method · رمز نوع المصروف / expense type code · رمز المؤسسة / organisation code · رمز الفرع / branch code | change manifest FIN v2 §1، §2 |
| FIN_DIMENSION_VALUE_SOURCE | ثابت / constant · حقل تجاري / business field | change manifest FIN v2 §2 |
| PAYMENT_METHOD (v1 key) | شيك / cheque | change manifest FIN v2 §1 "when the payment was by cheque" |

SCOPE EXCEPTIONS — ADDED / REMOVED
| Excluded / Deferred | Statement | Activation trigger | Source |
|---|---|---|---|
| REMOVED — القيمة DIRECT في ACCOUNT_DERIVATION_TYPE | لا يقبل النظام رمز حساب مأخوذًا من حقل في الحدث؛ إزالة كاملة، لا تقييد ولا إهمال تدريجي. استثناء v1 "Account numbers in event payloads — never" يبقى كما هو ويصبح متحققًا في البنية | لا تفعيل — الحد دائم | change manifest FIN v2 §1؛ domain-profile G6 |
| Deferred — ربط قيم الأبعاد | قيم الأبعاد ثابتة أو من الحقل التجاري مباشرة، دون جدول ترجمة | اختلاف رموز المؤسسة/الفرع القديمة عن قيم أبعاد FIN | change manifest FIN v2 §2 |
| Deferred — تعبئة الروابط الفعلية | بيانات ترحيل وليست جزءًا من الإصدار | تسليم مخزن الربط | change manifest FIN v2 — Out of scope |
| Excluded — الطابور، المستهلك، ما داخل Oracle القديم | خارج FIN | — | change manifest FIN v2 — Out of scope |
| Deferred — مصدر ثانٍ أو وسيط رسائل | يُضاف كمحوّل لاحقًا | طلب مصدر ثانٍ | change manifest FIN v2 — Out of scope |
| Excluded — مصادقة المستدعي الآلي | دلتا SEC منفصلة | تغيير SEC المنفصل | change manifest FIN v2 — Out of scope |
| Unchanged — استبعادات v1 | تبقى كما هي | — | domain-profile §1 |

RESOLVED DECISIONS (dialogue, this module — CONVERGED round 3)
| # | Question | Recommended answer | Confirmed by user | Sources |
|---|---|---|---|---|
| v2-1 | مصير DIRECT | حذف نهائي من اشتقاق الحساب؛ مصدر قيمة البُعد لوكب مستقل | — طلب المستخدم قرارًا مسجَّلًا (ADR)؛ بانتظار تأكيده | domain-profile G6؛ change manifest §1؛ v1 SCOPE EXCEPTIONS؛ project-registry PIPELINE STATUS |
| v2-2 | قيمة بُعد غائبة أو غير صالحة | رفض الحدث (POL-FIN-028) | نعم ضمنًا — نص الطلب §2 | change manifest §1، §2؛ POL-FIN-024 |
| v2-3 | ترجمة رموز الأبعاد | تأجيل | — بانتظار تأكيد المستخدم | change manifest §2؛ domain-profile G6 |
| v2-4 | مرجعية أسماء الحقول التجارية | لوكب FIN_EVENT_BUSINESS_FIELD | — بانتظار تأكيد المستخدم | domain-profile G1، G4، G5 |
| v2-5 | نطاق رمز الفترة غير المفتوحة | كل فترة موجودة لا تقبل الترحيل؛ غياب الفترة خطأ إعداد (POL-FIN-031) | نعم ضمنًا — نص الطلب §4 | change manifest §4؛ [KB:erp-domain-standards §6] |
══════════════════════════════════════════════════════════════════
