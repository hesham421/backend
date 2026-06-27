# ERP Governance Tools — Stage 2 (Agents)

## الملفات
```
config.py                    ← الإعدادات المشتركة (يُقرأ من كل الـ agents)
marker_parser.py              ← محرك قراءة Markers (يُستخدم داخلياً بواسطة Agent 3)
agent1_create_structure.py   ← ينشئ هيكل المجلدات لموديول
agent2_archive.py             ← يؤرشف ملفات P0-P4 المولّدة إلى الهيكل
agent3_splitter.py            ← يقسّم execution-plan.md / test-plan.md حسب Markers
```

## الإعداد
```bash
mkdir -p ~/governance-tools
# انسخ الملفات الخمسة هنا
cd ~/governance-tools
python3 --version   # تأكد من 3.10+
```

## الاستخدام

### Agent 1 — إنشاء الهيكل
```bash
python3 agent1_create_structure.py --module ORG
python3 agent1_create_structure.py --module ORG --dry-run
python3 agent1_create_structure.py --module NEW --auto-register --description "New Module"
python3 agent1_create_structure.py --module ORG --new-version
python3 agent1_create_structure.py --list-modules
```

### Agent 2 — أرشفة الملفات
```bash
python3 agent2_archive.py --module ORG --source ~/Desktop/ORG-files
python3 agent2_archive.py --module ORG --source ~/Desktop/ORG-files --dry-run
python3 agent2_archive.py --module ORG --source ~/Desktop/ORG-files --force
```

### Agent 3 — التقسيم (staged, approve-gated)
```bash
python3 agent3_splitter.py --module ORG                # كل الـ 5 stages بالترتيب
python3 agent3_splitter.py --module ORG --stage 1       # stage واحدة فقط
python3 agent3_splitter.py --module ORG --resume        # إكمال من حيث توقف
python3 agent3_splitter.py --module ORG --status        # عرض التقدم
```

## التسلسل الكامل لموديول جديد
```
1. وَلِّد ملفات P0→P4 من مشاريع claude.ai
2. python3 agent1_create_structure.py --module FIN
3. python3 agent2_archive.py --module FIN --source ~/Desktop/FIN-files
4. python3 agent3_splitter.py --module FIN
5. النتيجة في: ~/governance-repo/modules/FIN/packages/
```

## هيكل المخرجات
```
~/governance-repo/
├── master-registry.md
├── modules-registry.json
└── modules/
    └── [MOD]/
        ├── manifest.json
        ├── P0/ P1/ P2/ P3/ P3_5/ P4/
        └── packages/
            ├── execution/  (CORE, DATA-DOM, SVC-API/API, INT-C/XM, ...)
            └── test/       (JUNIT, PLAYWRIGHT)
```
