# ERP Governance Tools — دليل تشغيل الـ Agents

## المتطلبات

```bash
python3 --version   # يجب أن يكون 3.10 أو أعلى
cd ~/governance-tools
```

---

## Agent 1 — إنشاء هيكل المجلدات

يقوم بإنشاء هيكل المجلدات الكامل للموديول داخل جذر المستودع `governance-repo/`.

```bash
# معاينة فقط بدون تغييرات
python3 agent1_create_structure.py --module ORG --dry-run

# تشغيل فعلي (ينشئ المجلدات)
python3 agent1_create_structure.py --module ORG

# عرض قائمة جميع الموديولات المسجلة
python3 agent1_create_structure.py --list-modules

# إنشاء إصدار جديد (v2, v3, ...)
python3 agent1_create_structure.py --module ORG --new-version

# تسجيل موديول جديد تلقائياً
python3 agent1_create_structure.py --module FIN --auto-register --description "Finance Module"
```

**الناتج:** مجلدات P0..P5 + packages/execution + packages/test + manifest.json

---

## Agent 2 — أرشفة الملفات

ينسخ ملفات الـ artifacts من مجلد المصدر إلى هيكل الـ repo.

```bash
# معاينة فقط بدون نسخ
python3 agent2_archive.py --module ORG --source ~/Downloads/ORG-staging --dry-run

# تشغيل فعلي (ينسخ الملفات)
python3 agent2_archive.py --module ORG --source ~/Downloads/ORG-staging

# إعادة أرشفة مع استبدال الملفات الموجودة
python3 agent2_archive.py --module ORG --source ~/Downloads/ORG-staging --force
```

**ملاحظة:** الملفات يجب أن تكون بأسماء قياسية داخل مجلد المصدر:

| الملف المطلوب | يُنسخ إلى |
|---|---|
| `srs.md` | P1/ |
| `db-script.md` | P2/ |
| `execution-plan.md` | P3/ |
| `test-plan.md` | P3_5/ |
| `registry-srs-org.md` | P1/ |
| `registry-db-org.md` | P2/ |
| `registry-exec-org.md` | P3/ |
| `registry-test-org.md` | P3_5/ |
| `business-policies-org.md` | P0/ |
| `module-registry-org.md` | P0/ |
| `master-registry.md` | repo root |

---

## Agent 3 — تقسيم الملفات (Splitter)

يقرأ `execution-plan.md` و`test-plan.md` ويقسمهما إلى ملفات صغيرة جاهزة للـ AI agents.

```bash
# تشغيل كامل (5 مراحل مع موافقة على كل مرحلة)
python3 agent3_splitter.py --module ORG

# تشغيل كامل بدون توقف للموافقة
printf "y\ny\ny\ny\ny\n" | python3 agent3_splitter.py --module ORG
```

**المراحل الخمس:**

| المرحلة | الوصف | الناتج |
|---|---|---|
| Stage 1 | Parse & Plan — قراءة وفحص الملفين | تقرير بعدد PHASE / MARK / TC |
| Stage 2 | Split execution-plan.md | 67 ملف في packages/execution/ |
| Stage 3 | Split test-plan.md | 104 ملف في packages/test/ |
| Stage 4 | Generate Index Files | 17 ملف index.md |
| Stage 5 | Verify Completeness | تحقق من عدم ضياع أي محتوى |

**شرط النجاح:** يجب أن تكون markers في test-plan.md بالتسلسل الصحيح:
```
PHASE → MARK → SUB → TC
```

---

## الترتيب الصحيح للتشغيل

```
Agent 1  →  Agent 2  →  Agent 3
(هيكل)      (أرشفة)     (تقسيم)
```

---

## تشغيل موديول جديد من الصفر

```bash
# 1. إنشاء الهيكل
python3 agent1_create_structure.py --module ORG

# 2. أرشفة الملفات من مجلد المصدر
python3 agent2_archive.py --module ORG --source ~/Downloads/ORG-staging

# 3. تقسيم الملفات الكبيرة
printf "y\ny\ny\ny\ny\n" | python3 agent3_splitter.py --module ORG
```
