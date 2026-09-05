<!-- DELTA BACKEND EXECUTION PLAN — IFA MODE (AMEND-P3-P) — Governed by Execution Plan Governance Engine (Project 3.1 / PASS 1) -->
<!-- v1.3 baseline (backend-execution-plan-SEC.md) is FROZEN and UNCHANGED by this artifact — see C2 -->

# BACKEND EXECUTION PLAN (DELTA) — Security (SEC) — CS-SEC-001

## CHANGE MANIFEST — CS-SEC-001
──────────────────────────────────────────────────────────────────
Module          : SEC        Module Version : v2   Baseline : v1 (backend-execution-plan-SEC.md, ALIGN-BE ✓, frozen)
Change type     : ADDITIVE (C5 gate: no existing element removed, renamed, or redefined — v1 keeps working untouched)
Plan ID         : PLAN-SEC-001 (continuation) | Triggered by: srs-SEC-v2-CS-SEC-001.md (OQ-SEC-006, OQ-SEC-007, OQ-SEC-008 — all RESOLVED)
DBS             : db-script-SEC.md v1.1 — UNCHANGED, no delta (no new/modified entity or field — confirmed by srs delta Registry Update)
DB_TARGET       : POSTGRESQL_16   BACKEND_STACK: SPRING_BOOT_JAVA
──────────────────────────────────────────────────────────────────
NEW        : RULE-SEC-015, RULE-SEC-016, RULE-SEC-017, RULE-SEC-018 (continue v1.3 sequence, ended at RULE-SEC-014)
             API-SEC-021, API-SEC-022 (continue v1.3 sequence, ended at API-SEC-020)
             QR-SEC-0030, QR-SEC-0031, QR-SEC-0032 (continue v1.3 sequence, ended at QR-SEC-0029)
MODIFIED   : — none (RULE-SEC-016 confirms pre-existing ENTITY-SEC-008 modeling; does not redefine it)
UNCHANGED  : all v1.3 phases/blocks not listed below — see backend-execution-plan-SEC.md v1.3 for full content
REMOVED    : — (ADDITIVE delta; C5 REMOVED section not applicable)
──────────────────────────────────────────────────────────────────
Affected phases (this pass) : SVC+API (new SUB:SVC-API-SESSION only) · ALIGN-BE (delta gate + regression assertion)
Untouched phases             : CORE, DATA+DOM, DOC, INT-C, INT-R, SEC-BE
──────────────────────────────────────────────────────────────────
Downstream must re-align : backend-test-plan.md + test-execution-manifest.md (Test Generation Engine — CONTRACT-13 / §16A) → Project 4.1 (Backend Audit Gate). (Frontend wiring of ProtectedRoute/useAuthStore/Sidebar is PASS 2 / P3.2, gated on real API Docs regeneration.)

---

## SECTION 1 — PLAN INDEX DELTA — SEC — PLAN-ID: PLAN-SEC-001 (CS-SEC-001)
══════════════════════════════════════════════════════════════════
No new ENTITY-ID, FIELD-ID, ERR-ID, or SCR-ID this delta.

NEW API REGISTRY ROWS
──────────────────────────────────────────────────────────────────
API-SEC-021  Self  GET  /api/v1/security/auth/me    | RULE-SEC-015, 016
API-SEC-022  Menu  GET  /api/v1/security/me/menu    | RULE-SEC-015, 017, 018

NEW RULE REGISTRY ROWS
──────────────────────────────────────────────────────────────────
RULE-SEC-015 · RULE-SEC-016 · RULE-SEC-017 · RULE-SEC-018 (all Message-AR defined — see SVC+API blocks below)

NEW QRC ROWS (agent reference only)
──────────────────────────────────────────────────────────────────
QR-SEC-0030 · QR-SEC-0031 · QR-SEC-0032 (see SVC+API blocks below)

DB ALIGNMENT : NOT AFFECTED — 56/56 FIELD↔DBF alignment from v1.3 unchanged (no new column consumed).
XM STATUS    : NOT AFFECTED — API-SEC-021/022 are an intra-SEC aggregation query over pre-existing
               SEC-001/002/003/004/009/010/011 — no new XM-ID, no cross-module dependency (srs delta A7 note).
══════════════════════════════════════════════════════════════════

---

### CORE — UNCHANGED from v1 — see v1.3 plan
### DATA+DOM — UNCHANGED from v1 — see v1.3 plan

---

<!-- PHASE:SVC-API:START -->
## PHASE SVC+API (DELTA) — Service & API Contract Specifications — CS-SEC-001
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓ (delta re-run against srs-SEC-v2-CS-SEC-001.md)
(v1.3 phase content — SUB:SVC-API-AUTH / USERS / RBAC / MODULES / LOOKUP — UNCHANGED, see v1.3 plan.
 This delta ADDS one new SUB only: SUB:SVC-API-SESSION.)

  <!-- SUB:SVC-API-SESSION:START -->
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
  <!-- SUB:SVC-API-SESSION:END -->

API Governance (delta): RULE-ERR-CARRY ✓ (no new ERR-ID needed — both endpoints delegate unauthenticated-401 to the existing platform JWT filter, same pattern as API-SEC-014/016/019); LOC ✓ (RULE-SEC-015/017/018 all carry Message-AR + Message-EN). API-SEC-021/022 complement pre-existing API-SEC-019 (flat dashboard-module list) with no duplication: 019 = lightweight dashboard filter, 021 = full profile/roles/permissions for guards, 022 = full nested navigation tree (srs delta A2 note).
─────────────────────────────────────────────────────────────────
<!-- PHASE:SVC-API:END -->

### DOC — UNCHANGED from v1 — see v1.3 plan
### INT-C — UNCHANGED from v1 — see v1.3 plan (no new XM-ID; intra-SEC aggregation only)
### INT-R — UNCHANGED from v1 — see v1.3 plan
### SEC-BE — UNCHANGED from v1 — see v1.3 plan (API-SEC-021/022 are self-scoped, no screen permission — same declared pattern as API-SEC-019, no new SCR-ID)

---

<!-- PHASE:ALIGN-BE:START -->
## PHASE ALIGN-BE (DELTA) — Backend Internal Self-Consistency Gate — CS-SEC-001
─────────────────────────────────────────────────────────────────
## ALIGN-BE GATE — SEC — PLAN-ID: PLAN-SEC-001 (delta re-run against srs-SEC-v2-CS-SEC-001.md; db-script-SEC.md v1.1 unchanged)
═══════════════════════════════════════════════════════════════════════════
TRACEABILITY CHECKS (delta scope)                                    │ Status
─────────────────────────────────────────────────────────────────────┼───────
API-SEC-021/022 appear in Plan Index Delta                          │ ✓
RULE-SEC-015..018 appear in Plan Index Delta                        │ ✓
QR-SEC-0030..0032 appear in QRC (agent reference)                   │ ✓
No new FIELD-ID / ERR-ID / SCR-ID needed — confirmed (no DB change) │ ✓
DB Structural Alignment: unaffected, 56/56 (v1.3, unchanged)         │ ✓
─────────────────────────────────────────────────────────────────────┼───────
LOCALIZATION CHECKS                                                  │ Status
─────────────────────────────────────────────────────────────────────┼───────
RULE-SEC-015/017/018 have Message-AR defined                        │ ✓
No API error response introduced (401 delegated to platform filter) │ ✓
─────────────────────────────────────────────────────────────────────┼───────
SECURITY CHECKS                                                      │ Status
─────────────────────────────────────────────────────────────────────┼───────
API-SEC-021/022 declared self-scoped (no screen permission), consistent
  with pre-existing API-SEC-019 pattern — no new SCR-ID required     │ ✓
─────────────────────────────────────────────────────────────────────┼───────
QUERY REFERENCE CATALOG CHECKS                                       │ Status
─────────────────────────────────────────────────────────────────────┼───────
Every new API has QR-ID(s) in QRC                                    │ ✓
QR-SEC-0028 reuse correctly cited (not duplicated as a new ID)        │ ✓
No QR entry joins to a lookups table                                  │ ✓
─────────────────────────────────────────────────────────────────────┼───────
REGRESSION ASSERTION (AMEND-P3-P step 5 — mandatory for every IFA gate) │ Status
─────────────────────────────────────────────────────────────────────┼───────
No NEW/MODIFIED element breaks an existing v1.3 ALIGN-BE mapping       │ ✓
v1.3 IDs preserved verbatim: ENTITY-SEC-001..011, FIELD-0001..0056,
  API-SEC-001..020, RULE-SEC-001..014, ERR-0001..0014, QR-SEC-0001..0029,
  SCR-SEC-001..004, LOV-SEC-001/002, DRV-001..008 — no renumber, no reuse │ ✓
No existing endpoint's contract, permission, or error mapping changed   │ ✓
═══════════════════════════════════════════════════════════════════════════
ALIGN-BE GATE RESULT: PASSED ✓ (delta)
Auto-correction applied: None
═══════════════════════════════════════════════════════════════════════════
─────────────────────────────────────────────────────────────────
<!-- PHASE:ALIGN-BE:END -->

---

## SECTION C — REGISTRY UPDATE BLOCK (delta)
══════════════════════════════════════════════════════════════════
## REGISTRY UPDATE — 2026-09-05 (Amendment — SEC self-scoped session endpoints)
Source: Project 3.1 PASS 1 (Backend) | Feature Code SEC-001 | Plan PLAN-SEC-001 (continuation, CS-SEC-001)
Upstream : srs-SEC-v2-CS-SEC-001.md (Baseline v1.3) — db-script-SEC.md v1.1 UNCHANGED (no delta)
New Entities: — none
New Fields: — none
New APIs: API-SEC-021 (/auth/me), API-SEC-022 (/me/menu) — API total 22.
New Rules: RULE-SEC-015 (JWT-only derivation), RULE-SEC-016 (multi-role union confirmation), RULE-SEC-017 (menu VIEW+module filter), RULE-SEC-018 (orphan-branch label) — rule total 18.
New Errors: — none (both endpoints delegate 401 to the existing platform JWT filter) — error total remains 14.
New QR-IDs: QR-SEC-0030..0032 — QR total 32.
New Screen: — none (self-scoped, no SEC_PAGE row).
New DRV: — none.
XM-IDs Open: None (intra-SEC aggregation only; no new cross-module dependency).
OQ-IDs Open: None (OQ-SEC-006, OQ-SEC-007, OQ-SEC-008 all RESOLVED by this delta, per srs-SEC-v2-CS-SEC-001.md).
Gate Status: ALIGN-BE PASSED ✓ (delta) | Next: regenerate backend-test-plan.md + test-execution-manifest.md (Test Generation Engine, CONTRACT-13 / §16A) → Project 4.1.
Registry Event Log line to append (for human registry maintainer — P3.1 does not write project-registry.md directly):
  | 2026-09-05 | P3.1 delta — backend-execution-plan-SEC-v2-CS-SEC-001.md (PLAN-SEC-001, CS-SEC-001): self-scoped session endpoints per srs-SEC-v2-CS-SEC-001.md; +API-SEC-021/022, +RULE-SEC-015..018, +QR-SEC-0030..0032; db-script-SEC.md v1.1 unchanged (no DB delta); ALIGN-BE ✓ (delta). Downstream: regenerate backend-test-plan + manifest → P4.1 | P3.1 |
══════════════════════════════════════════════════════════════════

---

## AGENT HANDOFF ADDENDUM (delta) — not a phase
Agent-ready. This delta adds exactly two self-scoped, read-only GET endpoints on top of the frozen v1.3 SEC backend: GET /auth/me (identity + roleCodes[]/roleNames[] + grantedModules[]/grantedPermissions[], all derived solely from the JWT principal, unioned across the caller's active Roles) and GET /me/menu (a nested Sidebar-ready Page tree, filtered by VIEW-or-orphan-branch-label + module grant, each node carrying a viewGrantedFl-style accessibility indicator). Neither endpoint takes a target-user parameter; neither enforces a CORE-9 screen permission (self-scoped, same declared pattern as the pre-existing API-SEC-019). No new entity, field, error, or screen; no DB migration. Implement inside the existing AuthDomainService / a small new query-only service — no new domain service class required for logic this thin. Regenerate backend-test-plan.md + test-execution-manifest.md before Project 4.1; run api-doc-generator before PASS 2 so P3.2 can wire ProtectedRoute/useAuthStore/Sidebar to these two endpoints for real.

*End of backend-execution-plan-SEC-v2-CS-SEC-001.md — SEC — PLAN-SEC-001 — CS-SEC-001 delta (IFA, AMEND-P3-P) — ALIGN-BE ✓ (delta)*
*Upstream: srs-SEC-v2-CS-SEC-001.md (Baseline v1.3) · db-script-SEC.md v1.1 (unchanged, no delta) · Downstream: backend-test-plan/manifest regen → P4.1*
