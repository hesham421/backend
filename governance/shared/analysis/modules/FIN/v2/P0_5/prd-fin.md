# PRD — الحسابات العامة / Finance (General Ledger) (FIN) — DELTA v2
══════════════════════════════════════════════════════════════════
Module          : FIN     Version : v2 (delta فوق v1 — change set CS-FIN-001)
Source artifacts: platform-summary, module-registry, business-policies (DELTA v2) + change manifest CS-FIN-001
Stories         : 14 (13 DRAFT + 1 DEFERRED)   Policies covered : 12/12 (POL-FIN-021…032)   Deferred : 1
Status          : DRAFT — awaiting prd-approval
Dialogue        : analysis-dialogue (claude:opus ↔ claude:sonnet) — round 3 (claude:opus) — CONVERGED
══════════════════════════════════════════════════════════════════

## DELTA (v2)
ADDED    : US-FIN-020 … US-FIN-033
MODIFIED : لا شيء
REMOVED  : لا شيء
كل القصص في هذا الإصدار مضافة. الأرقام تبدأ من US-FIN-020 لأن `_state/current-prd.md` لـ FIN يُظهر
أن v1 يملك بالفعل US-FIN-001 حتى US-FIN-019 (قرار الحوار PRD-v2-1)؛ لا تُعاد كتابة أي قصة من تلك
القصص ولا تُعدَّل. سياسات v1 من POL-FIN-001 إلى POL-FIN-020 منقولة دون تغيير، وتشير إليها قصص v2
حيثما تمدّها السياسات الجديدة.

## USER STORIES

US-FIN-020
  Title          : لا رمز حساب يعبر داخل الحدث / No account code travels inside the event
  Story          : بصفتي المراقب المالي، أحتاج أن تقرر FIN وحدها كل حساب في قيد اليومية المبني من
                   الحدث المحاسبي القياسي، انطلاقًا من الوقائع التجارية التي يصرّح بها الحدث لا من رمز
                   حساب يصل في حمولته، كي يبقى المعنى المحاسبي ملكًا لـ FIN ولا يبقى أي طريق يُدخل رمز
                   حساب عبر الحدث.
  Priority       : HIGH — change manifest §1 "blocking"
  Success metric : acceptance 1 — "A rule line resolves its account from a business value in the event, with no account code anywhere in the payload."
  Traces         : POL-FIN-021
  Source         : change manifest CS-FIN-001 Summary "no account code and no accounting concept of any kind may
                   travel inside an event payload"؛ §1 "DIRECT … breaks the boundary"؛ module-registry
                   ACCOUNT_DERIVATION_TYPE (REMOVED DIRECT، القرار v2-1)
  Status         : DRAFT

US-FIN-021
  Title          : الحساب النقدي أو البنكي حسب طريقة الدفع / Cash or bank account by payment method
  Story          : بصفتي المحاسب، أحتاج أن يُختار الحساب النقدي أو الحساب البنكي في القيد المبني من
                   الحدث بحسب طريقة الدفع التي يصرّح بها الحدث، ومنها الشيك، كي يُعاد إنتاج سلوك النظام
                   القديم الذي تحتاجه أحد عشر فرعًا من ستة عشر دون أن يحمل الحدث رمز حساب.
  Priority       : HIGH — change manifest §1 "blocking"
  Success metric : acceptance 1
  Traces         : POL-FIN-022
  Source         : change manifest CS-FIN-001 §1 "Cash versus bank … swaps it for the bank account when the payment
                   was by cheque … Eleven of the sixteen live legacy branches need this."؛ business-policies CUSTOM
                   LOOKUP VALUES (PAYMENT_METHOD — شيك / cheque)
  Status         : DRAFT

US-FIN-022
  Title          : حساب المصروف حسب نوع المصروف / Expense account by expense type
  Story          : بصفتي المحاسب، أحتاج أن يُختار حساب المصروف في القيد المبني من الحدث بحسب رمز نوع
                   المصروف الذي يحمله الحدث، كي يتبع كل نوع مصروف حسابه الصحيح كما في النظام القديم.
  Priority       : HIGH — change manifest §1 "blocking"
  Success metric : acceptance 1
  Traces         : POL-FIN-022
  Source         : change manifest CS-FIN-001 §1 "Expense account per expense type. The event carries an expense
                   type code and the account depends on it."
  Status         : DRAFT

US-FIN-023
  Title          : الحساب المربوط يخضع لتحقق شجرة الحسابات نفسه / A mapped account is checked like a fixed one
  Story          : بصفتي المراقب المالي، أحتاج أن يخضع الحساب الذي يُحدَّد عبر ربط الحسابات للتحقق نفسه
                   الذي يخضع له الحساب الثابت في القاعدة، أي أن يكون ورقيًا ونشطًا وقابلًا للترحيل، كي
                   لا يفتح المسار الجديد ثغرة في شجرة الحسابات.
  Priority       : HIGH — change manifest §1 "blocking"
  Success metric : —
  Traces         : POL-FIN-023, POL-FIN-003
  Source         : change manifest CS-FIN-001 §1 "validates the resulting account exactly as it validates a
                   CONSTANT one — leaf, active, postable"
  Status         : DRAFT

US-FIN-024
  Title          : القيمة غير المربوطة تُرفض بسبب واضح / An unmapped value is refused with a clear reason
  Story          : بصفتي المحاسب، أحتاج أن أعرف أن الحدث الذي يحمل قيمة تجارية ليس لها ربط نشط لن
                   يُرحَّل منه أي قيد، وأن يظهر السبب خطأً مخصّصًا لهذه الحالة، لا حسابًا افتراضيًا ولا
                   تخمينًا، كي أكتشف الربط الناقص وأكمله بدل أن يُرحَّل قيد إلى حساب خاطئ.
  Priority       : HIGH — change manifest §1 "blocking"
  Success metric : acceptance 2 — "An unmapped business value produces a clear error and no posted entry."
  Traces         : POL-FIN-024
  Source         : change manifest CS-FIN-001 §1 "An unmapped value is an error and no entry posts; never a
                   default, never a guess. A dedicated error code is needed"
  Status         : DRAFT

US-FIN-025
  Title          : إدارة روابط الحسابات كبيانات عبر شاشة FIN / Managing account mappings as data through a FIN screen
  Story          : بصفتي مسؤول المالية، أحتاج أن أضيف روابط الحسابات وأعدّلها وأوقفها كبيانات عبر
                   شاشة في FIN، كسائر إعدادات الوحدة ودون أي تعديل في الشيفرة، وأن أطمئن إلى أن تعديل
                   الربط أو إيقافه لا يغيّر قيدًا رُحِّل من قبل، كي يُنقَل منطق اختيار الحساب في النظام
                   القديم، ومنه "نقد ما لم يكن شيكًا" الذي يصبح صفوف ربط صريحة، وكي يتطور هذا المنطق دون
                   إصدار جديد.
  Priority       : HIGH — change manifest §1 "blocking"
  Success metric : acceptance 3 — "Mappings are added and changed as data through a FIN screen, never in code."
  Traces         : POL-FIN-025, POL-FIN-013
  Source         : change manifest CS-FIN-001 §1 "a FIN-owned mapping store, managed as data through a FIN screen
                   like every other configuration in the module"؛ business-policies POL-FIN-024 Rationale (صف
                   ربط صريح لكل قيمة طريقة دفع)؛ module-registry AUTO-DECISIONS "تعديل الربط يسري على
                   الاشتقاقات اللاحقة فقط ولا يمس القيود المُرحَّلة" (acceptance 7)
  Status         : DRAFT

US-FIN-026
  Title          : إسناد المؤسسة والفرع على سطور القيد المبنية من الأحداث / Organisation and branch attribution on event-built lines
  Story          : بصفتي المراقب المالي خلال التشغيل المتوازي، أحتاج أن يحمل كل سطر قيد مبني من حدث
                   إسناد المؤسسة والفرع، مأخوذًا من الحدث أو مثبّتًا في القاعدة، كي تتطابق قيود FIN مع
                   قيود النظام القديم سطرًا بسطر في المقارنة المتوازية ويكون للتحقق في وضع الظل (shadow
                   mode) قيمة.
  Priority       : HIGH — change manifest §2 "blocking"
  Success metric : acceptance 4 — "Rule-driven lines carry validated dimension values derived from event fields."
  Traces         : POL-FIN-026
  Source         : change manifest CS-FIN-001 §2 "Legacy stamps organisation and branch on every journal entry … the
                   parallel-run comparison between legacy and FIN diverges on every row (which makes shadow-mode
                   verification worthless)"
  Status         : DRAFT

US-FIN-027
  Title          : قيم الأبعاد من الحدث تخضع لتحقق السطر اليدوي / Event dimension values get the manual-line checks
  Story          : بصفتي المراقب المالي، أحتاج أن تخضع قيمة البُعد المأخوذة من الحدث لقواعد الأبعاد
                   نفسها المطبّقة على سطر القيد المُدخل يدويًا، أي أن تنتمي إلى البُعد وأن تكون نشطة، كي
                   لا يتجاوز المسار الآلي التحقق القائم وتبقى الأبعاد جزءًا موثوقًا من هوية الترحيل.
  Priority       : HIGH — change manifest §2 "blocking"
  Success metric : acceptance 4
  Traces         : POL-FIN-027, POL-FIN-011
  Source         : change manifest CS-FIN-001 §2 "The resolved value is validated against FIN's existing dimension
                   rules (belongs to that dimension, active); the existing validation is not bypassed."
  Status         : DRAFT

US-FIN-028
  Title          : رفض الحدث حين تتعذّر قيمة البُعد / An event is refused when a dimension value cannot be resolved
  Story          : بصفتي المحاسب، أحتاج أن أعرف أن الحدث الذي تغيب عنه قيمة بُعد يصرّح بها سطر القاعدة،
                   أو تكون قيمته غير صالحة، لن يُرحَّل منه أي قيد، كي لا يدخل الأستاذ قيد بلا إسناد مؤسسة
                   أو فرع فيُفسد المقارنة المتوازية كما يفسدها الحساب الخاطئ.
  Priority       : HIGH — change manifest §2 "blocking"
  Success metric : —
  Traces         : POL-FIN-028
  Source         : change manifest CS-FIN-001 §2 "using the same discipline as accounts"، §1 "never a default,
                   never a guess"؛ القرار المحسوم v2-2
  Status         : DRAFT

US-FIN-029
  Title          : تقارير واحدة لسطور الأحداث والسطور اليدوية / One reporting treatment for event and manual lines
  Story          : بصفتي المراقب المالي، أحتاج أن تعامل تقارير الأستاذ وتقارير الأبعاد سطور
                   القيد المبنية من الأحداث والسطور المُدخلة يدويًا معاملة واحدة، كي لا تفرغ تقارير
                   الأبعاد من أهم القيود ولا تختلف النتيجة باختلاف مصدر القيد.
  Priority       : HIGH — change manifest §2 "blocking"
  Success metric : —
  Traces         : POL-FIN-029, POL-FIN-011
  Source         : change manifest CS-FIN-001 §2 "Event-built and manually-built lines must end up with the same
                   dimension shape so that ledger and dimension reports treat them identically."؛ "FIN's own
                   dimension reports are empty for exactly the entries that matter most"
  Status         : DRAFT

US-FIN-030
  Title          : الحدث المكرر يُجاب دائمًا بأنه مكرر / A duplicate event is always answered as a duplicate
  Story          : بصفتي مشغّل تكامل الأحداث، أحتاج أن يُجاب الحدث المكرر دائمًا بخطأ الحدث المكرر
                   أيًّا كان موضع اكتشاف التكرار، كي يعدّه المستهلك نجاحًا ويؤكد استلام الرسالة، بدل أن
                   يبدو عطلًا فيُعاد إرساله وينتهي في طابور الرسائل الميتة وأضطر إلى مطابقة يدوية لحدث
                   رُحِّل صحيحًا من المرة الأولى.
  Priority       : —
  Success metric : acceptance 5 — "A duplicate event reference always answers the duplicate-event error."
  Traces         : POL-FIN-030, POL-FIN-012
  Source         : change manifest CS-FIN-001 §3 "For the consumer a duplicate is success, not failure … the consumer
                   retries, the message lands in the dead-letter queue, and someone reconciles by hand"
  Status         : DRAFT

US-FIN-031
  Title          : رمز خاص لرفض الفترة غير المفتوحة / A distinct code for a non-open period rejection
  Story          : بصفتي مشغّل تكامل الأحداث، أحتاج أن يُعرَف رفض الترحيل إلى فترة غير مفتوحة برمز خاص
                   به، مفهرس وموثَّق ومنفصل عن أخطاء التحقق والإعداد، كي يُبقي المستهلك الحدث منتظرًا
                   ويعيد المحاولة بعد فتح الفترة بدل أن يسمه فاشلًا.
  Priority       : —
  Success metric : acceptance 6 — "A closed-period rejection has its own distinct, documented code."
  Traces         : POL-FIN-031, POL-FIN-004
  Source         : change manifest CS-FIN-001 §4 "it is an operational state that resolves itself when someone opens
                   the period, so the event should wait and be retried rather than be marked failed"؛ القرار v2-5
  Status         : DRAFT

US-FIN-032
  Title          : ثوابت v1 سارية على المسار الجديد / v1 invariants hold on the new path
  Story          : بصفتي المراقب المالي، أحتاج أن تبقى كل ثوابت الترحيل في v1 سارية دون تغيير على
                   القيود المبنية عبر ربط الحسابات أو وسوم الأبعاد، كي لا يُضعف المسار الجديد سلامة
                   الأستاذ التي أرساها v1.
  Priority       : —
  Success metric : acceptance 7 — "Every FIN v1 invariant still holds for entries produced through the new path … These are inherited, not reopened."
  Traces         : POL-FIN-032, POL-FIN-001, POL-FIN-003, POL-FIN-004, POL-FIN-005, POL-FIN-006, POL-FIN-009, POL-FIN-012, POL-FIN-013
  Source         : change manifest CS-FIN-001 acceptance 7؛ business-policies POL-FIN-032
  Status         : DRAFT

US-FIN-033
  Title          : ترجمة رموز المؤسسة والفرع القديمة إلى قيم أبعاد FIN / Translating legacy organisation and branch codes into FIN dimension values
  Story          : بصفتي المراقب المالي خلال التشغيل المتوازي، أحتاج، إن ثبت أن رموز المؤسسة والفرع في النظام القديم
                   تختلف عن قيم الأبعاد في FIN، أن تُترجَم تلك الرموز إلى قيم أبعاد FIN، كي يبقى الإسناد
                   صحيحًا في المقارنة المتوازية.
  Priority       : —
  Success metric : —
  Traces         : POL-FIN-026
  Source         : platform-summary DEFERRED "ربط قيم الأبعاد"؛ business-policies SCOPE EXCEPTIONS "Deferred — ربط
                   قيم الأبعاد"؛ القرار v2-3؛ change manifest CS-FIN-001 §2 (المطلوب قيمة ثابتة أو حقل تجاري
                   مسمّى فقط)
  Status         : DEFERRED — خارج نطاق v2

## TRACEABILITY — story → policy
| US | Traces (POL) | Source |
|---|---|---|
| US-FIN-020 | POL-FIN-021 | CS-FIN-001 Summary، §1، acceptance 1 |
| US-FIN-021 | POL-FIN-022 | CS-FIN-001 §1 (Cash versus bank) |
| US-FIN-022 | POL-FIN-022 | CS-FIN-001 §1 (Expense account per expense type) |
| US-FIN-023 | POL-FIN-023, POL-FIN-003 | CS-FIN-001 §1 |
| US-FIN-024 | POL-FIN-024 | CS-FIN-001 §1، acceptance 2 |
| US-FIN-025 | POL-FIN-025, POL-FIN-013 | CS-FIN-001 §1، acceptance 3، acceptance 7 |
| US-FIN-026 | POL-FIN-026 | CS-FIN-001 §2، acceptance 4 |
| US-FIN-027 | POL-FIN-027, POL-FIN-011 | CS-FIN-001 §2 |
| US-FIN-028 | POL-FIN-028 | CS-FIN-001 §1، §2؛ v2-2 |
| US-FIN-029 | POL-FIN-029, POL-FIN-011 | CS-FIN-001 §2 |
| US-FIN-030 | POL-FIN-030, POL-FIN-012 | CS-FIN-001 §3، acceptance 5 |
| US-FIN-031 | POL-FIN-031, POL-FIN-004 | CS-FIN-001 §4، acceptance 6 |
| US-FIN-032 | POL-FIN-032, POL-FIN-001, POL-FIN-003, POL-FIN-004, POL-FIN-005, POL-FIN-006, POL-FIN-009, POL-FIN-012, POL-FIN-013 | CS-FIN-001 acceptance 7 |
| US-FIN-033 (DEFERRED) | POL-FIN-026 | platform-summary DEFERRED؛ v2-3 |

تغطية سياسات v2: كل سياسة من POL-FIN-021 إلى POL-FIN-032 مغطاة بقصة واحدة على الأقل (12/12).

## RESOLVED DECISIONS (dialogue)
| # | Question | Recommended | Confirmed by user | Sources |
|---|---|---|---|---|
| PRD-v2-1 | تسلسل US وتصنيف الدلتا | يتابع التسلسل بعد US-FIN-019 (أعلى رقم موجود لـ FIN في `_state/current-prd.md`)، فتبدأ قصص v2 من US-FIN-020 وتنتهي عند US-FIN-033، وكلها ADDED | يؤكَّد عند prd-approval | `_state/current-prd.md` (FIN، v1: US-FIN-001…019)؛ engine §2 SEQUENCE RULE؛ analyze.py `ids-continue` |
| PRD-v2-2 | الأولويات | HIGH لقصص §1 و§2 ("blocking")، و"—" لقصص §3 و§4 ولـ US-FIN-032 (v1 invariants)، لأن "small" تصف الحجم لا الأولوية | يؤكَّد عند prd-approval | change manifest §1–§4؛ engine §2 (Priority) |
| PRD-v2-3 | دور قصص المستهلك | مشغّل تكامل الأحداث (دور بشري)؛ المستهلك يُذكر داخل القدرة. أدوار القصص سردية ولا تنشئ أدوارًا أو صلاحيات في SEC | يؤكَّد عند prd-approval | change manifest §3 "someone reconciles by hand"، §4؛ POL-FIN-015؛ [KB:erp-domain-standards §4] |
| PRD-v2-4 | قرارات P0 التي تنتظر التأكيد (v2-1، v2-3، v2-4) | تُحمَل في هذا الملف؛ اعتماد PRD هو تأكيد المستخدم لها. أي رفض عند البوابة يعود دلتا على P0 قبل P1 | يؤكَّد عند prd-approval | engine §4، §9؛ platform-summary OPEN ITEMS؛ module-registry RESOLVED DECISIONS |
| PRD-v2-5 | إغلاق تأجيلات v2 | قصة DEFERRED واحدة (US-FIN-033) لترجمة رموز الأبعاد؛ باقي الاستبعادات خارج FIN أو بيانات ترحيل، فتُذكر في نطاق الملف دون قصص | يؤكَّد عند prd-approval | engine §4؛ platform-summary DEFERRED؛ business-policies SCOPE EXCEPTIONS |
| PRD-v2-6 | مفردات الأدوار في قصص v2 | تُستعمل أدوار v1 نفسها حيث الفاعل نفسه: مسؤول المالية / finance administrator لإعداد الروابط (كما في قصة v1 لقواعد أنواع الأحداث)، والمراقب المالي / financial controller للتقارير وللمقارنة المتوازية (كما في قصة v1 للتقارير حسب البُعد)؛ الدور الجديد الوحيد مشغّل تكامل الأحداث (PRD-v2-3) لأنه ليس دورًا ماليًا. يُحذف "مسؤول إعداد FIN" و"مستخدم التقارير المالية" و"مراجع التشغيل المتوازي" كي لا يستنتج P1 أدوارًا لم يطلبها أحد | يؤكَّد عند prd-approval | `_state/current-prd.md` (أدوار قصص v1)؛ engine §10 (المفردات حرفيًا)؛ POL-FIN-015 |
| v2-1 (P0) | مصير DIRECT | حذف نهائي من اشتقاق الحساب؛ مصدر قيمة البُعد لوكب مستقل | يؤكَّد عند prd-approval (PRD-v2-4) | module-registry v2-1 |
| v2-2 (P0) | قيمة بُعد غائبة أو غير صالحة | رفض الحدث (POL-FIN-028) | نعم ضمنًا — نص الطلب §2 | business-policies v2-2 |
| v2-3 (P0) | ترجمة رموز الأبعاد | تأجيل (US-FIN-033) | يؤكَّد عند prd-approval (PRD-v2-4) | business-policies v2-3 |
| v2-4 (P0) | مرجعية أسماء الحقول التجارية | لوكب FIN_EVENT_BUSINESS_FIELD | يؤكَّد عند prd-approval (PRD-v2-4) | module-registry v2-4 |
| v2-5 (P0) | نطاق رمز الفترة غير المفتوحة | كل فترة موجودة لا تقبل الترحيل؛ غياب الفترة خطأ إعداد | نعم ضمنًا — نص الطلب §4 | business-policies v2-5 |

## DEFERRED
| US | Reason | Activation trigger |
|---|---|---|
| US-FIN-033 | القرار المحسوم v2-3: v2 يطلب قيمة ثابتة أو حقلًا تجاريًا مسمّى فقط، دون جدول ترجمة | ثبوت اختلاف رموز المؤسسة/الفرع القديمة عن قيم أبعاد FIN |

خارج النطاق دون قصص (مصدرها platform-summary DEFERRED و change manifest "Out of scope"): طابور قاعدة
البيانات وخدمة المستهلك وما داخل نظام Oracle القديم؛ تعبئة الروابط الفعلية (بيانات ترحيل)؛ مصدر ثانٍ أو وسيط
رسائل؛ مصادقة المستدعي الآلي (دلتا SEC)؛ استبعادات v1.

## APPROVAL
Approved by : —   Date : —
Once approved, no stage may raise a question; P1 onward self-resolve
per the ambiguity rule (shared/GOVERNANCE-CORE.md).
══════════════════════════════════════════════════════════════════
