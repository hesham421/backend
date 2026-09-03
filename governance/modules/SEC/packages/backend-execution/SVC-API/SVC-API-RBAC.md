<!-- Source: PHASE:SVC-API / SUB:SVC-API-RBAC -->
<!-- Context: see SVC-API-HEADER.md for phase-level strategy, registry table, and intro -->

<!-- API:API-SEC-011:START -->
### API-SEC-011 — Roles CRUD
POST/GET/PUT/DELETE /api/v1/security/roles(/{id}) | RoleController → RoleService
REQUEST RoleCreate/UpdateRequest{roleCode(create-only, immutable after),nameAr,nameEn,isActiveFl}
RESPONSE 201/200 RoleResponse; search → Page<RoleResponse>
VALIDATIONS: RULE-SEC-010 (unique roleCode — Message-AR: الرموز فريدة.); RULE-SEC-002-equiv (nameAr,nameEn required — LOC)
ERRORS: ERR-0009 → RULE-SEC-010 dup code → 409; ERR-0012 → NOT_FOUND → 404
REPO: QR-SEC-0007..0010 (find_one/search/save/update, EXISTS roleCode) — mixed | ALLOWED_SORT_FIELDS={roleCode,nameAr,createdAt}
SECURITY: SCR-SEC-002 (VIEW/CREATE/UPDATE/DELETE).
<!-- API:API-SEC-011:END -->
<!-- API:API-SEC-013:START -->
### API-SEC-013 — Pages CRUD (Screen Registry, CORE-9 owner)
POST/GET/PUT/DELETE /api/v1/security/pages(/{id}) | PageController → PageService
REQUEST PageCreate/UpdateRequest{pageCode(create-only),nameAr,nameEn,**moduleFk (required, → SEC_MODULE)**,parentPageFk?,isActiveFl}
VALIDATIONS: RULE-SEC-010 (unique pageCode); RULE-SEC-011 (on CREATE, auto-generate 4 permissions PERM_<pageCode>_VIEW/CREATE/UPDATE/DELETE — Message-AR: تُولَّد أربع صلاحيات لكل شاشة.); **moduleFk existence (→ SEC_MODULE, active) — required so RULE-SEC-014 can resolve the page's owning module.**
ERRORS: ERR-0009 → RULE-SEC-010 dup pageCode → 409; ERR-0012 → NOT_FOUND (page or moduleFk) → 404
ORCHESTRATION (create): validate moduleFk (QR-SEC-0024) → save page with moduleFk (QR-SEC-0013) → PermissionGenerationDomainService creates 4 SEC_PERMISSION rows (QR-SEC-0016) — RULE-SEC-011.
REPO: QR-SEC-0011..0014 + QR-SEC-0016 + QR-SEC-0024(module existence) — READ_WRITE | ALLOWED_SORT_FIELDS={pageCode,nameAr,moduleFk,createdAt}
SECURITY: SCR-SEC-003 (VIEW/CREATE/UPDATE/DELETE).
<!-- API:API-SEC-013:END -->
<!-- API:API-SEC-014:START -->
### API-SEC-014 — List permissions
GET /api/v1/security/permissions | PermissionController.search → PermissionService.search
REQUEST params: pageFk?(EXACT), permissionType?(EXACT), **moduleFk?(EXACT — via page, for Tier-2 picker scoped to granted modules)**, page,size | RESPONSE 200 Page<PermissionResponse>
VALIDATIONS: RULE-SEC-011 (permissions are system-generated; read-only listing) | ERRORS: none (empty → 200 [])
REPO: QR-SEC-0015 FIND_BY_CRITERIA — READ_ONLY | SECURITY: SCR-SEC-002 VIEW.
<!-- API:API-SEC-014:END -->
<!-- API:API-SEC-015:START -->
### API-SEC-015 — Grant / revoke permission to role (Tier-2)
POST|DELETE /api/v1/security/roles/{id}/permissions | RoleController.grant/revoke → RolePermissionService
REQUEST {permissionId} | RESPONSE 200
VALIDATIONS: existence of role & permission (idempotent join write on SEC_ROLE_PERMISSION);
  **RULE-SEC-014 (grant only): the role MUST hold the module of this permission's page (SEC_PERMISSION→SEC_PAGE.moduleFk) in SEC_ROLE_MODULE — no orphan screen permission — Message-AR: لا تُمنح صلاحية شاشة لدور ما لم يُمنَح الدور موديل الشاشة.**
ERRORS: ERR-0012 → NOT_FOUND (role or permission) → 404; **ERR-0013 → RULE-SEC-014 module-not-granted → 422 (grant only)**
ORCHESTRATION (grant): resolve permission → its page → page.moduleFk → **EXISTS SEC_ROLE_MODULE(roleId, moduleFk) (QR-SEC-0027); if absent → ERR-0013** → else idempotent insert (QR-SEC-0018). (revoke): idempotent delete (QR-SEC-0018) — no derivation check on revoke of a single permission.
REPO: QR-SEC-0018 SAVE/DELETE(join) + QR-SEC-0027(EXISTS grant) — READ_WRITE | SECURITY: SCR-SEC-002 UPDATE.
<!-- API:API-SEC-015:END -->
