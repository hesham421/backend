<!-- Source: PHASE:SVC-API / SUB:SVC-API-SESSION -->
<!-- Context: see SVC-API-HEADER.md for phase-level strategy, registry table, and intro -->

<!-- API:API-SEC-021:START -->
### API-SEC-021 — Self identity, roles & granted modules/permissions
GET /api/v1/security/auth/me | Controller MeController.me → AuthMeService.getSelf
REQUEST (none — principal from JWT) | RESPONSE 200 MeResponse{username, fullName, roleCodes[], roleNames[], grantedModules[], grantedPermissions[]}
VALIDATIONS: RULE-SEC-015 (MUST derive all data solely from the authenticated JWT principal — no path/query parameter identifies the target user; MUST return 401 when unauthenticated — Message-AR: "تُشتق البيانات من هوية JWT فقط دون أي معامل يحدد مستخدماً آخر؛ 401 لغير المصادَق عليه.");
  RULE-SEC-016 (CONFIRMS existing ENTITY-SEC-008 many-to-many modeling — a UserAccount MAY hold multiple simultaneously active Roles; roleCodes[]/roleNames[] MUST be returned as arrays; grantedModules/grantedPermissions MUST be the UNION of Tier-1 (RoleModule) and Tier-2 (RolePermission) grants across all the caller's active Roles — Message-AR: "يجوز أن يملك الحساب أكثر من دور نشط في آن؛ تُعاد كمصفوفات؛ الموديولات والصلاحيات الممنوحة = اتحاد كل الأدوار.")
ERRORS: none surfaced (401 via platform JWT filter when unauthenticated — RULE-SEC-015; not a new ERR-ID, mirrors existing "authenticated" endpoints e.g. API-SEC-014/016)
ORCHESTRATION: from JWT principal → load caller's active SEC_USER_ROLE→SEC_ROLE (QR-SEC-0030) → union SEC_ROLE_MODULE→SEC_MODULE across roles (QR-SEC-0028, reused from API-SEC-019) → union SEC_ROLE_PERMISSION→SEC_PERMISSION across roles (QR-SEC-0031).
REPO: QR-SEC-0030 FIND active roles (codes+names) for user + QR-SEC-0028 FIND granted modules (reused) + QR-SEC-0031 FIND granted permissions for user — READ_ONLY
SECURITY: authenticated (self-scoped; no screen permission required — mirrors API-SEC-019 pattern, SCR-SEC-none).
<!-- API:API-SEC-021:END -->
<!-- API:API-SEC-022:START -->
### API-SEC-022 — Self nested navigation menu tree
GET /api/v1/security/me/menu | Controller MeController.menu → MenuService.buildTree
REQUEST (none — principal from JWT) | RESPONSE 200 [MenuNodeResponse] (nested tree by parentPageFk; each node carries a computed accessibility indicator; empty → 200 [])
VALIDATIONS: RULE-SEC-015 (JWT-principal-only; 401 if unauthenticated — same as API-SEC-021);
  RULE-SEC-017 (MUST include a Page only when the caller holds PERM_<pageCode>_VIEW OR is reachable under RULE-SEC-018, AND the Page's moduleFk is among the caller's granted Modules (consistent with RULE-SEC-013/014); MUST nest Pages by parentPageFk into a ready-to-render tree; each node MUST carry a computed accessibility indicator (VIEW granted directly vs. structural label only — RULE-SEC-018); ordering follows the existing display/sort convention if any, else nameEn ascending; an empty result returns 200 with an empty array (not an error) — Message-AR: "تُدرَج الصفحة بشرط VIEW عليها (أو ظهورها كعنصر تنقّل هيكلي فقط — RULE-SEC-018) وانتماء موديولها لموديولات مُمنوحة؛ تُبنى شجرة متداخلة عبر parentPageFk مع مؤشر إتاحة لكل عنصر؛ نتيجة فارغة → 200 بمصفوفة فارغة.");
  RULE-SEC-018 (orphan branch: when the caller holds VIEW on a child Page but not its parent, MUST surface the parent Page as a non-clickable navigation label — no VIEW implied, not a link — so the child stays reachable in the tree; the parent node MUST carry an explicit accessibility indicator, e.g. viewGrantedFl=false, distinguishing it from a directly granted, clickable Page — Message-AR: "يظهر الأب كعنصر تنقّل غير قابل للنقر لإبقاء الابن قابلاً للوصول؛ يُميَّز بمؤشر صريح (viewGrantedFl=false) عن صفحة ممنوحة مباشرة." — Architect decision, Hesham, 2026-09-05, accepted P1 recommendation; resolves OQ-SEC-008)
ERRORS: none surfaced (401 via platform JWT filter when unauthenticated — RULE-SEC-015)
ORCHESTRATION: from JWT principal → resolve granted modules (QR-SEC-0028, reused) → resolve granted permissions (QR-SEC-0031, reused) → build full SEC_PAGE tree with per-node viewGrantedFl / structural-label computation against SEC_PERMISSION/SEC_ROLE_PERMISSION/SEC_PAGE.moduleFk (QR-SEC-0032) → nest by parentPageFk → sort.
REPO: QR-SEC-0032 FIND page tree with per-node accessibility computation for user — READ_ONLY
SECURITY: authenticated (self-scoped; no screen permission required — mirrors API-SEC-019 pattern, SCR-SEC-none).
<!-- API:API-SEC-022:END -->
