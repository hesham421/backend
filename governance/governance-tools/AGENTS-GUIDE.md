# ERP Governance Tools — دليل تشغيل الـ Agents (Backend)

أدوات حتمية (Python، بلا LLM) تعمل **بعد** إصدار تحليل الباك اند وخططه
(P0 → P3_1 execution plan و P3_5_BE test plan) و**قبل** تنفيذ الكود.
هذه المجموعة تعرف الباك اند فقط — لا يوجد frontend ولا track ولا P4.

## المتطلبات

```bash
python3 --version    # 3.10 أو أعلى
cd backend/governance/governance-tools
```

---

## الخطوة 0 (إلزامية) — التحقق من الماركرات قبل الأرشفة

فور توليد أي ملف من المحرّك، تحقّق منه مباشرةً — قبل Agent 1:

```bash
python3 agent3_splitter.py --validate-markers --file backend-execution-plan.md
python3 agent3_splitter.py --validate-markers --file backend-test-plan.md
```

- خروج 0 + «marker structure valid» = سليم.
- خروج 1 + قائمة أخطاء مرقّمة = أصلح المصدر وأعد التحقق. التحقق يشمل:
  البنية (أزواج/تداخل/تفرّد) **و** الدلالة (مفتاح طور قانوني، تأهيل الـ SUB
  بالطور، عدم وجود عنصر ذرّي يتيم).

**إصلاح آمن تلقائي:** لإصلاح الأخطاء الحتمية القابلة للإصلاح بأمان:
```bash
python3 agent3_splitter.py --fix-safe --file backend-execution-plan.md
```
يصلح فقط: خطأ فاصل مفتاح الطور (`SVC_API`→`SVC-API`) وتأهيل الـ SUB
بالطور (`SUB:CRUD`→`SUB:SVC-API-CRUD`)، ويحفظ الأصل في `<file>.orig`
ويعيد التحقق. لا يلمس المحتوى ولا يصلح ما يحتاج حكماً بشرياً (ماركر
غير مغلق، معرّف مكرّر، عنصر يتيم، مفتاح غامض مثل `DATADOM`) — يبلّغ عنه
ويُنهي بخروج 1.

---

## Agent 1 — إنشاء هيكل المجلدات

```bash
python3 agent1_create_structure.py --module ORG --dry-run     # معاينة (لا يكتب شيئاً)
python3 agent1_create_structure.py --module ORG               # تشغيل فعلي
python3 agent1_create_structure.py --module FIN --auto-register --description "Finance"
python3 agent1_create_structure.py --module ORG --new-version # v2, v3, ...
python3 agent1_create_structure.py --list-modules
```

**الناتج:** `modules/[MOD]/` يحوي `P0 P0_5 P1 P2 P2_5 P3_1 P3_5_BE` +
`packages/backend-execution/<مجلد لكل طور>` + `packages/backend-test/`
(حاوية بلا مجلدات فرعية) + `manifest.json`.

> `--dry-run` للقراءة فقط: لا يُسجّل موديولاً ولا ينشئ مجلداً (FINDING-22a).

---

## Agent 2 — أرشفة الملفات

ينسخ ملفات الـ artifacts من مجلد المصدر إلى الهيكل (بأسماء قياسية دقيقة).

```bash
python3 agent2_archive.py --module ORG --source ~/Desktop/ORG-files --dry-run
python3 agent2_archive.py --module ORG --source ~/Desktop/ORG-files
python3 agent2_archive.py --module ORG --source ~/Desktop/ORG-files --force
```

| الملف في المصدر | يُنسخ إلى |
|---|---|
| `platform-summary.md`, `module-registry-org.md`, `business-policies-org.md` | `P0/` |
| `prd-org.md` | `P0_5/` |
| `srs.md`, `registry-srs-org.md` | `P1/` |
| `db-script.md`, `registry-db-org.md` | `P2/` |
| `flow-diagram.md`, `ui-ux-spec.md` | `P2_5/` |
| `backend-execution-plan.md`, `registry-exec-be-org.md` | `P3_1/` |
| `backend-test-plan.md`, `test-execution-manifest.md`, `registry-test-be-org.md` | `P3_5_BE/` |

> بدون `--force` لا يُستبدل أي ملف موجود — يُبقى كما هو ويُبلّغ عنه (kept).
> `--force` وحده يستبدل. (لا يوجد P3/P3_5/P4 — تلك بقايا الموديل القديم.)
> Agent 2 ينشئ الهيكل تلقائياً إن لم يُشغَّل Agent 1 أولاً — لا تبعية صارمة.

---

## Agent 3 — التقسيم (5 مراحل، بموافقة على كل مرحلة، قابلة للاستئناف)

يقرأ `backend-execution-plan.md` و`backend-test-plan.md` ويقسّمهما إلى ملفات
حزم موجَّهة للـ AI agents.

```bash
python3 agent3_splitter.py --module ORG                 # الخمس مراحل بالترتيب
python3 agent3_splitter.py --module ORG --stage 1        # مرحلة واحدة
python3 agent3_splitter.py --module ORG --resume         # إكمال من آخر نقطة
python3 agent3_splitter.py --module ORG --status         # عرض التقدم
python3 agent3_splitter.py --module ORG --dry-run        # معاينة بلا كتابة
python3 agent3_splitter.py --module ORG --strict-thresholds  # تحويل تحذيرات العتبة إلى أخطاء حاجبة
```

> **تحذيرات العتبة (auto ومرن):** إن كان طور فوق عتبة التقسيم (SVC-API فيه
> ‏≥ 8 APIs، أو INT-C/INT-R فيه ≥ 5 XMs، أو TEST-PLAN-BE فيه > 12 TCs) بلا
> ‏SUB — أو طور «لا يُقسَّم» يحمل SUB — يُبلَّغ عنه كتحذير **غير حاجب**
> افتراضياً (القرار دلالي). ‏`--strict-thresholds` يجعله حاجباً. العتبات معطى
> في `config.PHASE_SPLIT_THRESHOLDS` — تُعدَّل بسطر واحد.

| المرحلة | الوصف | الناتج |
|---|---|---|
| Stage 1 | Parse & Plan — قراءة + تحقق بنيوي **ودلالي** حاجب، مع **تحذيرات عتبة** استشارية | تقرير بعدد PHASE/SUB/API/XM/TC |
| Stage 2 | تقسيم `backend-execution-plan.md` | ملفات في `packages/backend-execution/` (مجلد لكل طور) + `_SECTIONS.md` للمحتوى خارج الأطوار |
| Stage 3 | تقسيم `backend-test-plan.md` | ملفات مسطّحة في `packages/backend-test/` (`RULE-SCENARIOS.md` / `API-SCENARIOS.md`، أو ملف طور كامل إن كانت TCs ≤ 12) |
| Stage 4 | Generate Index | `index.md` لكل مجلد حزمة |
| Stage 5 | Verify | مطابقة SHA-256 لكل API/XM/TC مقابل المصدر المؤرشف |

**مبدأ جوهري:** عدد الملفات يتناسب مع **بنية** الأطوار/الأطوار الفرعية، لا مع
عدد العناصر. موديول فيه 104 TC ينتج بضعة ملفات فقط — العناصر الذرّية تبقى
مضمّنة داخل ملف مجموعتها للبحث الموضعي، والماركرات عنونة لا أمر تقسيم.

**تسمية ملفات SUB:** تُشتق من تسمية الـ SUB نفسها (المؤهَّلة بالطور مسبقاً،
AMEND-P3-N) — مثل `SVC-API-CRUD.md`. لا تُضاف بادئة الطور مرة ثانية.

---

## الترتيب الكامل لموديول جديد

```bash
# 0) وَلِّد P0 → P3_5_BE من مشاريع claude.ai، ثم:
python3 agent3_splitter.py --validate-markers --file backend-execution-plan.md
python3 agent3_splitter.py --validate-markers --file backend-test-plan.md
# 1) الهيكل
python3 agent1_create_structure.py --module FIN --auto-register --description "Finance"
# 2) الأرشفة
python3 agent2_archive.py --module FIN --source ~/Desktop/FIN-files
# 3) التقسيم
python3 agent3_splitter.py --module FIN
# النتيجة: modules/FIN/packages/  (نسبةً لـ backend/governance/)
```

---

## الاختبارات

```bash
python3 -m pytest tests/      # من داخل governance-tools/
```

يغطّي: المُحلِّل البنيوي، المُتحقِّق الدلالي، خط التقسيم الكامل، وحُرّاس
`--dry-run` و`--force`. كلها معزولة ولا تلمس شجرة الحوكمة الحقيقية.
