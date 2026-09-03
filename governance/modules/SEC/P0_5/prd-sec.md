# PRD — Security (SEC)
══════════════════════════════════════════════════════════════════
Module          : Security (SEC prefix)
Version         : 2 — CONTINUATION (updated from v1)
Source artifacts: platform-summary.md, module-registry-SEC.md,
                  business-policies-SEC.md; domain-profile-ERP.md v2 (amendment source)
Status          : DRAFT — awaiting Reconciliation Gate (Project 2.5)
Open Questions  : None — see OQ Log
══════════════════════════════════════════════════════════════════

UPSTREAM CHANGE — SEC two-tier RBAC + internal SSO (domain-profile-ERP.md v2)
  Triggered by : domain-profile-ERP.md v2 — GOVERNING RULES (two-tier RBAC over internal SSO)
  Amended here : + US-SEC-008 (Tier-1 role→modules) · US-SEC-009 (module-scoped
                 dashboard) · US-SEC-010 (Tier-2 screen perms within a granted
                 module) · US-SEC-011 (internal SSO). NEEDs only — no RULE/ENTITY.
  Downstream must re-align : P1 (srs-SEC) next; then P2.5 (UI/UX consumes PRD+SRS)
══════════════════════════════════════════════════════════════════

## USER STORIES

US-SEC-001
  Story    : كمستخدم، أحتاج تسجيل الدخول ببياناتي والبقاء مسجّلاً عبر
             الطلبات (جلسة JWT مع تجديد)، لأصل إلى المنصة بأمان.
  Priority : —
  Success metric : —
  Source   : module-registry-SEC.md §SCOPE NOTE (stateless JWT + refresh);
             entities UserAccount, RefreshToken
  Status   : DRAFT

US-SEC-002
  Story    : كأدمن، أحتاج إنشاء وإدارة حسابات المستخدمين، لأتحكم بمن
             يملك وصولاً إلى المنصة.
  Priority : —
  Success metric : —
  Source   : module-registry-SEC.md §ENTITIES OWNED (UserAccount);
             §SCOPE NOTE (user accounts)
  Status   : DRAFT

US-SEC-003
  Story    : كأدمن، أحتاج تعريف الأدوار وإسنادها للمستخدمين، لتنظيم
             الوصول حسب الدور.
  Priority : —
  Success metric : —
  Source   : module-registry-SEC.md §ENTITIES OWNED (Role, UserRole)
  Status   : DRAFT

US-SEC-004
  Story    : كأدمن، أحتاج تعريف الصلاحيات ومنحها للأدوار، ليحصل كل دور
             على ما يخصّه من صلاحيات (RBAC).
  Priority : —
  Success metric : —
  Source   : module-registry-SEC.md §ENTITIES OWNED (Permission, RolePermission)
  Status   : DRAFT

US-SEC-005
  Story    : كأدمن، أحتاج التحكم بالوصول إلى كل شاشة عبر صلاحية مرتبطة
             بها، ليصل المستخدمون فقط إلى الشاشات المصرّح بها لهم.
  Priority : —
  Success metric : —
  Source   : module-registry-SEC.md §ENTITIES OWNED (Page/Screen Registry);
             SHARED-GOVERNANCE-CORE CORE-9 (screen-registry owner)
  Status   : DRAFT

US-SEC-006
  Story    : كمستخدم، أحتاج إعادة تعيين كلمة المرور بنفسي عند نسيانها،
             لأستعيد الوصول دون تدخّل الأدمن.
  Priority : —
  Success metric : —
  Source   : module-registry-SEC.md §ENTITIES OWNED (PasswordResetToken);
             §SCOPE NOTE (forgot-password)
  Status   : DRAFT

US-SEC-007
  Story    : كمستخدم جديد، أحتاج تفعيل حسابي، لأتمكّن من استخدامه.
  Priority : —
  Success metric : —
  Source   : module-registry-SEC.md §ENTITIES OWNED (AccountActivationToken);
             §SCOPE NOTE (account-activation)
  Status   : DRAFT

US-SEC-008
  Story    : كأدمن، أحتاج تحديد مجموعة الموديولات التي يستطيع كل دور
             الوصول إليها، ليُدار وصول الموديولات على مستوى الدور (Tier 1).
  Priority : —
  Success metric : —
  Source   : domain-profile-ERP.md v2 §GOVERNING RULES (Tier 1 — Role → Modules)
  Status   : DRAFT

US-SEC-009
  Story    : كمستخدم، أحتاج أن تُظهر لي لوحة التحكم فقط الموديولات
             الممنوحة لدوري، لأرى ما يمكنني الوصول إليه فقط.
  Priority : —
  Success metric : —
  Source   : domain-profile-ERP.md v2 §GOVERNING RULES (Tier 1 — منح الموديل
             يحكم ظهوره على الداش بورد / display filter)
  Status   : DRAFT

US-SEC-010
  Story    : كأدمن، أحتاج أن تكون صلاحيات الشاشات قابلة للمنح فقط داخل
             موديل ممنوح للدور، لتبقى صلاحيات الشاشة متّسقة مع منح الموديل
             (دون صلاحية شاشة يتيمة).
  Priority : —
  Success metric : —
  Source   : domain-profile-ERP.md v2 §GOVERNING RULES (Tier 2 — Role → Screens؛
             الشاشات تُشتق من الموديل / no orphan screen permission)
  Status   : DRAFT

US-SEC-011
  Story    : كمستخدم، أحتاج تسجيل دخول داخلي واحد يعمل عبر كل موديولات
             المنصة، لأصادَق مرة واحدة بهوية داخلية موحّدة.
  Priority : —
  Success metric : —
  Source   : domain-profile-ERP.md v2 §GOVERNING RULES (SSO داخلي — هوية/توكن
             داخلي واحد للمصادقة فقط، منفصل عن التفويض)
  Status   : DRAFT

## STORIES EXCLUDED (justified)

  — UserRole / RolePermission (join entities) — جداول ربط داخلية؛
    الحاجة مغطّاة ضمن US-SEC-003 / US-SEC-004، لا قصة مستقلة.
  — RefreshToken — آلية جلسة داخلية، مطويّة في US-SEC-001.

## SCOPE EXCLUSIONS (خارج النطاق — من P0)

  — Branch/Organization DataScope (SecRoleBranch) — يعتمد على موديول
    Organization خارج نطاق Foundation. business-policies-SEC §SCOPE EXCEPTIONS.
  — Multi-tenant scoping — لا مفهوم tenant في نواة المصادقة.
  — External identity federation (SSO خارجي) — إضافة اختيارية لاحقة،
    خارج النطاق الآن. domain-profile-ERP.md v2 §GOVERNING RULES (SSO).

## OPEN ITEMS (ambiguous, not yet a story)

  ? قيم المصادقة الملموسة (token TTL, حد أدنى لطول كلمة المرور, عتبة
    قفل الحساب) غير محددة في P0 — business-policies-SEC ينصّ أن P1
    يربطها كـ Source: Client عند تحديدها. ليست قصة PRD.

══════════════════════════════════════════════════════════════════
*End of prd-SEC.md — v2 (CONTINUATION). US-SEC-001..011.*
*Next stage: Project 2.5 (UI/UX Design Engine) — requires this file
 AND srs.md together (CONTRACT-11). P1 (srs-SEC) re-aligns first.*
══════════════════════════════════════════════════════════════════
