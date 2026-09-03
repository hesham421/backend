<!-- Source: PHASE:SVC-API / SUB:SVC-API-MODULES -->
<!-- Context: see SVC-API-HEADER.md for phase-level strategy, registry table, and intro -->

<!-- API:API-SEC-020:START -->
### API-SEC-020 — Modules CRUD (Module Registry) ⟵ v1.3
POST/GET/PUT/DELETE /api/v1/security/modules(/{id}) | ModuleController → ModuleService
REQUEST ModuleCreate/UpdateRequest{moduleCode(create-only, immutable after),nameAr,nameEn,isActiveFl}
RESPONSE 201/200 ModuleResponse; search → Page<ModuleResponse>
VALIDATIONS: RULE-SEC-010 (unique moduleCode — Message-AR: الرموز فريدة.); nameAr,nameEn required
ERRORS: ERR-0009 → RULE-SEC-010 dup moduleCode → 409; ERR-0012 → NOT_FOUND → 404
REPO: QR-SEC-0023 SAVE(SEQ_SEC_MODULE) + QR-SEC-0024 find/search + QR-SEC-0025 update/EXISTS moduleCode — mixed | ALLOWED_SORT_FIELDS={moduleCode,nameAr,createdAt}
SECURITY: SCR-SEC-004 (VIEW/CREATE/UPDATE/DELETE).
<!-- API:API-SEC-020:END -->
<!-- API:API-SEC-017:START -->
### API-SEC-017 — Assign module to role (Tier-1 grant) ⟵ v1.3
POST /api/v1/security/roles/{id}/modules | RoleController.assignModule → AuthorizationGrantDomainService.grantModule
REQUEST {moduleId} | RESPONSE 200
VALIDATIONS: existence of role & module (idempotent insert into SEC_ROLE_MODULE); RULE-SEC-013 (grant = dashboard display filter + prerequisite — Message-AR: منح الموديل للدور يُظهره على الداشبورد وهو شرط مسبق لأي صلاحية شاشة داخله.)
ERRORS: ERR-0012 → NOT_FOUND (role or module) → 404
ORCHESTRATION: validate role & module active → idempotent insert SEC_ROLE_MODULE (QR-SEC-0026). No module-level runtime gate created (display filter only).
REPO: QR-SEC-0026 SAVE(join) — READ_WRITE | SECURITY: SCR-SEC-002 UPDATE.
<!-- API:API-SEC-017:END -->
<!-- API:API-SEC-018:START -->
### API-SEC-018 — Revoke module from role ⟵ v1.3
DELETE /api/v1/security/roles/{id}/modules/{moduleId} | RoleController.revokeModule → AuthorizationGrantDomainService.revokeModule
RESPONSE 200/204
VALIDATIONS: RULE-SEC-013/014 — revoke MUST preserve the no-orphan invariant. **DRV-007: BLOCK if the role still holds any screen permission for a page in this module** (would leave orphan Tier-2 grants).
ERRORS: ERR-0012 → NOT_FOUND (grant/role/module) → 404; **ERR-0014 → RULE-SEC-014 revoke would orphan screen permissions → 409 — Message-AR: لا يمكن سحب الموديل: الدور لا يزال يملك صلاحيات شاشات داخله.**
ORCHESTRATION: **EXISTS SEC_ROLE_PERMISSION for role within this module (join SEC_ROLE_PERMISSION→SEC_PERMISSION→SEC_PAGE.moduleFk) (QR-SEC-0029); if present → ERR-0014** → else delete SEC_ROLE_MODULE row (QR-SEC-0026). Admin removes the module's screen permissions first.
REPO: QR-SEC-0026 DELETE(join) + QR-SEC-0029(dependents EXISTS) — READ_WRITE | SECURITY: SCR-SEC-002 UPDATE.
<!-- API:API-SEC-018:END -->
<!-- API:API-SEC-019:START -->
### API-SEC-019 — Dashboard modules (current user) ⟵ v1.3
GET /api/v1/security/me/modules | MeController.modules → DashboardService.grantedModules
REQUEST (none — principal from JWT) | RESPONSE 200 [ModuleResponse] (distinct active modules granted to any of the caller's roles; empty → 200 [])
VALIDATIONS: RULE-SEC-013 (returns only granted modules — the dashboard DISPLAY FILTER)
ERRORS: none (authenticated principal always resolvable; empty list is valid)
ORCHESTRATION: from JWT principal → user roles (SEC_USER_ROLE) → SEC_ROLE_MODULE → distinct active SEC_MODULE (QR-SEC-0028).
REPO: QR-SEC-0028 FIND granted modules for user — READ_ONLY | SECURITY: authenticated (self-scoped; no screen permission required).
<!-- API:API-SEC-019:END -->
