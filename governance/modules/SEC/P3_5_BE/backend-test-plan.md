<!-- backend-test-plan.md — Governed by Execution Plan Governance Engine (Project 3.1) | JUnit -->

# backend-test-plan.md — Security (SEC) — PLAN-ID: PLAN-SEC-001
══════════════════════════════════════════════════════════════════
Source: backend-execution-plan-SEC.md (v1.3, ALIGN-BE ✓ re-run) · srs-SEC.md (v1.3) · db-script-SEC.md (DBS-SEC-001, v1.1)
DB_TARGET: POSTGRESQL_16 | Open Questions: None
Plan Status: CONTINUATION — regenerated to cover the v1.3 amendment (two-tier RBAC + internal SSO): +RULE-SEC-013/014, +API-SEC-017..020, +ENTITY-SEC-010/011. Prior TC-BE-SEC-001..037 preserved verbatim — no renumber, no reuse.
TARGET TC COUNT: 14 rules (25 TC) + 20 API happy (20 TC) = 45 (was 37; +8 — over-engineering guard respected, see PLAN-SEC-001 SECTION D)
══════════════════════════════════════════════════════════════════

<!-- PHASE:TEST-PLAN-BE:START -->

 <!-- SUB:RULE-SCENARIOS:START -->
 <!-- TC:TC-BE-SEC-001:START -->
TC-BE-SEC-001 — Create user unique username (RULE-SEC-001 happy) | API-SEC-007 | VALID
 Given new username → When POST /security/users → Then 201; user PENDING_ACTIVATION.
 <!-- TC:TC-BE-SEC-001:END -->
 <!-- TC:TC-BE-SEC-002:START -->
TC-BE-SEC-002 — Duplicate username (RULE-SEC-001 violation) | API-SEC-007 | ERR-0001 | INVALID
 Given existing username → When POST → Then 409 ERR-0001; messageAr "اسم المستخدم مستخدَم مسبقاً." | BOTH
 <!-- TC:TC-BE-SEC-002:END -->
 <!-- TC:TC-BE-SEC-003:START -->
TC-BE-SEC-003 — Required fields present (RULE-SEC-002 happy) | API-SEC-007 | VALID
 Given username+email+fullName → When POST → Then 201.
 <!-- TC:TC-BE-SEC-003:END -->
 <!-- TC:TC-BE-SEC-004:START -->
TC-BE-SEC-004 — Missing core fields (RULE-SEC-002 violation) | API-SEC-007 | ERR-0002 | INVALID
 Given no fullName → When POST → Then 400 ERR-0002; messageAr "حقول الحساب الأساسية إلزامية." | BOTH
 <!-- TC:TC-BE-SEC-004:END -->
 <!-- TC:TC-BE-SEC-005:START -->
TC-BE-SEC-005 — Password complexity ok (RULE-SEC-003 happy) | API-SEC-005 | VALID
 Given password "Abc12345" → When POST /auth/reset-password → Then 200.
 <!-- TC:TC-BE-SEC-005:END -->
 <!-- TC:TC-BE-SEC-006:START -->
TC-BE-SEC-006 — Weak password (RULE-SEC-003 violation) | API-SEC-005 | ERR-0003 | INVALID/BOUNDARY
 Given password "abc" (<8, no digit) → When POST → Then 422 ERR-0003; messageAr "كلمة المرور لا تحقق التعقيد." | BOTH
 <!-- TC:TC-BE-SEC-006:END -->
 <!-- TC:TC-BE-SEC-007:START -->
TC-BE-SEC-007 — Password stored hashed only (RULE-SEC-004) | API-SEC-007 | VALID
 Given create user → When persisted → Then PASSWORD_HASH ≠ plaintext; passwordHash absent from all responses.
 <!-- TC:TC-BE-SEC-007:END -->
 <!-- TC:TC-BE-SEC-008:START -->
TC-BE-SEC-008 — Login under lock threshold (RULE-SEC-005 happy) | API-SEC-001 | VALID
 Given <5 prior fails → When correct login → Then 200; failedLoginCount reset.
 <!-- TC:TC-BE-SEC-008:END -->
 <!-- TC:TC-BE-SEC-009:START -->
TC-BE-SEC-009 — Account locked after N fails (RULE-SEC-005 violation) | API-SEC-001 | ERR-0004 | BOUNDARY
 Given 5 failed logins → When 6th attempt → Then 423 ERR-0004; messageAr "قُفل بعد محاولات فاشلة." | BOTH
 <!-- TC:TC-BE-SEC-009:END -->
 <!-- TC:TC-BE-SEC-010:START -->
TC-BE-SEC-010 — Refresh rotates token (RULE-SEC-006 happy) | API-SEC-002 | VALID
 Given valid refresh token → When POST /auth/refresh → Then 200; old token revoked, new issued.
 <!-- TC:TC-BE-SEC-010:END -->
 <!-- TC:TC-BE-SEC-011:START -->
TC-BE-SEC-011 — Reused/expired refresh rejected (RULE-SEC-006 violation) | API-SEC-002 | ERR-0005 | INVALID
 Given revoked token → When POST /auth/refresh → Then 401 ERR-0005. | BOTH
 <!-- TC:TC-BE-SEC-011:END -->
 <!-- TC:TC-BE-SEC-012:START -->
TC-BE-SEC-012 — Reset with valid token (RULE-SEC-007 happy) | API-SEC-005 | VALID
 Given single active reset token → When POST /auth/reset-password → Then 200; token usedFl=1.
 <!-- TC:TC-BE-SEC-012:END -->
 <!-- TC:TC-BE-SEC-013:START -->
TC-BE-SEC-013 — Reused reset token rejected (RULE-SEC-007 violation) | API-SEC-005 | ERR-0006 | INVALID
 Given already-used reset token → When POST → Then 400 ERR-0006; messageAr "رمز إعادة تعيين واحد فعّال." | BOTH
 <!-- TC:TC-BE-SEC-013:END -->
 <!-- TC:TC-BE-SEC-014:START -->
TC-BE-SEC-014 — Activate with valid token (RULE-SEC-008 happy) | API-SEC-006 | VALID
 Given valid activation token → When POST /auth/activate → Then 200; userStatusId=ACTIVE; token usedFl=1.
 <!-- TC:TC-BE-SEC-014:END -->
 <!-- TC:TC-BE-SEC-015:START -->
TC-BE-SEC-015 — Reused activation token rejected (RULE-SEC-008 violation) | API-SEC-006 | ERR-0007 | INVALID
 Given used activation token → When POST → Then 400 ERR-0007; messageAr "رمز تفعيل واحد فعّال." | BOTH
 <!-- TC:TC-BE-SEC-015:END -->
 <!-- TC:TC-BE-SEC-016:START -->
TC-BE-SEC-016 — Login active account (RULE-SEC-009 happy) | API-SEC-001 | VALID
 Given ACTIVE user, correct creds → When login → Then 200 with tokens.
 <!-- TC:TC-BE-SEC-016:END -->
 <!-- TC:TC-BE-SEC-017:START -->
TC-BE-SEC-017 — Login blocked non-active (RULE-SEC-009 violation) | API-SEC-001 | ERR-0008 | INVALID
 Given PENDING_ACTIVATION user → When login → Then 403 ERR-0008; messageAr "لا دخول لحساب غير نشط." | BOTH
 <!-- TC:TC-BE-SEC-017:END -->
 <!-- TC:TC-BE-SEC-018:START -->
TC-BE-SEC-018 — Unique role code ok (RULE-SEC-010 happy) | API-SEC-011 | VALID
 Given new roleCode → When POST /security/roles → Then 201.
 <!-- TC:TC-BE-SEC-018:END -->
 <!-- TC:TC-BE-SEC-019:START -->
TC-BE-SEC-019 — Duplicate code rejected (RULE-SEC-010 violation) | API-SEC-011/013 | ERR-0009 | INVALID
 Given existing roleCode/pageCode → When POST → Then 409 ERR-0009; messageAr "الرموز فريدة." | BOTH
 <!-- TC:TC-BE-SEC-019:END -->
 <!-- TC:TC-BE-SEC-020:START -->
TC-BE-SEC-020 — Page registration auto-generates 4 permissions (RULE-SEC-011) | API-SEC-013 | VALID
 Given new page "SEC_TEST" → When POST /security/pages → Then 4 SEC_PERMISSION rows PERM_SEC_TEST_{VIEW,CREATE,UPDATE,DELETE} exist.
 <!-- TC:TC-BE-SEC-020:END -->
 <!-- TC:TC-BE-SEC-021:START -->
TC-BE-SEC-021 — Deactivation no cascade (RULE-SEC-012) | API-SEC-010 | VALID
 Given user referenced by NOTIF logs → When DELETE /security/users/{id} → Then 200; isActiveFl=0; NOTIF historical rows intact; reactivation allowed.
 <!-- TC:TC-BE-SEC-021:END -->
 <!-- TC:TC-BE-SEC-038:START -->
TC-BE-SEC-038 — Dashboard shows only granted modules (RULE-SEC-013) | API-SEC-019 | VALID
 Given role R granted modules {SEC,FILE} via SEC_ROLE_MODULE, current user holds role R → When GET /security/me/modules → Then 200 [SEC,FILE] only; ungranted modules excluded; display filter — no violation path.
 <!-- TC:TC-BE-SEC-038:END -->
 <!-- TC:TC-BE-SEC-039:START -->
TC-BE-SEC-039 — Grant screen permission when role holds the module (RULE-SEC-014 happy) | API-SEC-015 | VALID
 Given role R holds SEC_ROLE_MODULE(R,SEC) → When POST /security/roles/{id}/permissions {permissionId ∈ a SEC page} → Then 200; SEC_ROLE_PERMISSION row created.
 <!-- TC:TC-BE-SEC-039:END -->
 <!-- TC:TC-BE-SEC-040:START -->
TC-BE-SEC-040 — Grant screen permission without module grant rejected (RULE-SEC-014 violation) | API-SEC-015 | ERR-0013 | INVALID
 Given role R does NOT hold SEC_ROLE_MODULE for the permission's page module → When POST /security/roles/{id}/permissions → Then 422 ERR-0013; messageAr "لا تُمنح صلاحية شاشة لدور ما لم يُمنَح الدور موديل الشاشة." | BOTH
 <!-- TC:TC-BE-SEC-040:END -->
 <!-- TC:TC-BE-SEC-041:START -->
TC-BE-SEC-041 — Revoke module blocked while screen permissions remain (RULE-SEC-014 violation) | API-SEC-018 | ERR-0014 | INVALID
 Given role R holds SEC_ROLE_MODULE(R,SEC) and at least one SEC_ROLE_PERMISSION for a page in module SEC → When DELETE /security/roles/{id}/modules/{moduleId} → Then 409 ERR-0014; messageAr "لا يمكن سحب الموديل: الدور لا يزال يملك صلاحيات شاشات داخله." | BOTH
 <!-- TC:TC-BE-SEC-041:END -->
 <!-- SUB:RULE-SCENARIOS:END -->

 <!-- SUB:API-SCENARIOS:START -->
 <!-- TC:TC-BE-SEC-022:START -->
TC-BE-SEC-022 — API-SEC-001 Login happy | VALID → 200 tokens.
 <!-- TC:TC-BE-SEC-022:END -->
 <!-- TC:TC-BE-SEC-023:START -->
TC-BE-SEC-023 — API-SEC-002 Refresh happy | VALID → 200 rotated tokens.
 <!-- TC:TC-BE-SEC-023:END -->
 <!-- TC:TC-BE-SEC-024:START -->
TC-BE-SEC-024 — API-SEC-003 Logout happy | VALID → 204; refresh revoked.
 <!-- TC:TC-BE-SEC-024:END -->
 <!-- TC:TC-BE-SEC-025:START -->
TC-BE-SEC-025 — API-SEC-004 Forgot-password happy | VALID → 202 neutral; reset token created; CU event published.
 <!-- TC:TC-BE-SEC-025:END -->
 <!-- TC:TC-BE-SEC-026:START -->
TC-BE-SEC-026 — API-SEC-005 Reset-password happy | VALID → 200; new hash set.
 <!-- TC:TC-BE-SEC-026:END -->
 <!-- TC:TC-BE-SEC-027:START -->
TC-BE-SEC-027 — API-SEC-006 Activate happy | VALID → 200; status ACTIVE.
 <!-- TC:TC-BE-SEC-027:END -->
 <!-- TC:TC-BE-SEC-028:START -->
TC-BE-SEC-028 — API-SEC-007 Create user happy | VALID → 201; activation token issued.
 <!-- TC:TC-BE-SEC-028:END -->
 <!-- TC:TC-BE-SEC-029:START -->
TC-BE-SEC-029 — API-SEC-008 Search users happy + empty→200 (MANDATORY-J-7) | VALID/EDGE → 200 Page; empty filter → 200 [].
 <!-- TC:TC-BE-SEC-029:END -->
 <!-- TC:TC-BE-SEC-030:START -->
TC-BE-SEC-030 — API-SEC-009 Update user happy | VALID → 200; username unchanged.
 <!-- TC:TC-BE-SEC-030:END -->
 <!-- TC:TC-BE-SEC-031:START -->
TC-BE-SEC-031 — API-SEC-010 Deactivate user happy | VALID → 200/204; isActiveFl=0.
 <!-- TC:TC-BE-SEC-031:END -->
 <!-- TC:TC-BE-SEC-032:START -->
TC-BE-SEC-032 — API-SEC-011 Roles CRUD happy | VALID → create/read/update/search all 2xx.
 <!-- TC:TC-BE-SEC-032:END -->
 <!-- TC:TC-BE-SEC-033:START -->
TC-BE-SEC-033 — API-SEC-012 Assign role happy | VALID → 200; SEC_USER_ROLE row present.
 <!-- TC:TC-BE-SEC-033:END -->
 <!-- TC:TC-BE-SEC-034:START -->
TC-BE-SEC-034 — API-SEC-013 Pages CRUD happy | VALID → 201 + 4 permissions generated.
 <!-- TC:TC-BE-SEC-034:END -->
 <!-- TC:TC-BE-SEC-035:START -->
TC-BE-SEC-035 — API-SEC-014 List permissions happy | VALID → 200 Page.
 <!-- TC:TC-BE-SEC-035:END -->
 <!-- TC:TC-BE-SEC-036:START -->
TC-BE-SEC-036 — API-SEC-015 Grant/revoke permission happy | VALID → 200; SEC_ROLE_PERMISSION row present/removed.
 <!-- TC:TC-BE-SEC-036:END -->
 <!-- TC:TC-BE-SEC-037:START -->
TC-BE-SEC-037 — API-SEC-016 Lookups happy + permission enforcement (MANDATORY-J-5) + SQLi (MANDATORY-J-8)
 VALID → GET /security/lookups/SEC_USER_STATUS → 200 [{code,labelAr,labelEn}].
 ATTACK → user without VIEW on SCR-SEC-001 hitting API-SEC-008 → 403; username="x' OR '1'='1" stored literal.
 <!-- TC:TC-BE-SEC-037:END -->
 <!-- TC:TC-BE-SEC-042:START -->
TC-BE-SEC-042 — API-SEC-017 Assign module to role happy | VALID → 200; SEC_ROLE_MODULE row created; idempotent on repeat.
 <!-- TC:TC-BE-SEC-042:END -->
 <!-- TC:TC-BE-SEC-043:START -->
TC-BE-SEC-043 — API-SEC-018 Revoke module from role happy (no dependents) | VALID → 200/204; SEC_ROLE_MODULE row removed.
 <!-- TC:TC-BE-SEC-043:END -->
 <!-- TC:TC-BE-SEC-044:START -->
TC-BE-SEC-044 — API-SEC-019 Dashboard modules happy + empty→200 (MANDATORY-J-7) | VALID/EDGE → 200 [ModuleResponse]; user with no module grants → 200 [].
 <!-- TC:TC-BE-SEC-044:END -->
 <!-- TC:TC-BE-SEC-045:START -->
TC-BE-SEC-045 — API-SEC-020 Modules CRUD happy | VALID → create/read/update/search all 2xx; duplicate moduleCode → 409 ERR-0009 (RULE-SEC-010, already exercised at TC-BE-SEC-019).
 <!-- TC:TC-BE-SEC-045:END -->
 <!-- SUB:API-SCENARIOS:END -->

<!-- PHASE:TEST-PLAN-BE:END -->

## TC TRACEABILITY INDEX (BACKEND) — SEC
══════════════════════════════════════════════════════════════════
RULE→TC: SEC-001→001/002 · SEC-002→003/004 · SEC-003→005/006 · SEC-004→007 · SEC-005→008/009 · SEC-006→010/011 · SEC-007→012/013 · SEC-008→014/015 · SEC-009→016/017 · SEC-010→018/019 · SEC-011→020 · SEC-012→021 · SEC-013→038 · SEC-014→039/040/041
API→TC: 001→022 · 002→023 · 003→024 · 004→025 · 005→026 · 006→027 · 007→028 · 008→029 · 009→030 · 010→031 · 011→032 · 012→033 · 013→034 · 014→035 · 015→036 · 016→037 · 017→042 · 018→043 · 019→044 · 020→045
ERR→TC: ERR-0001→002 · 0002→004 · 0003→006 · 0004→009 · 0005→011 · 0006→013 · 0007→015 · 0008→017 · 0009→019 · 0011/0012→037 · 0013→040 · 0014→041
══════════════════════════════════════════════════════════════════
Coverage: RULE 14/14 · API 20/20 · Total 45 TCs
══════════════════════════════════════════════════════════════════

*End of backend-test-plan.md — SEC — PLAN-SEC-001 (v2 — regenerated for v1.3 two-tier RBAC/SSO amendment; superseded v1 archived to [BACKUP]/SEC__backend-test-plan-SEC.md__2026-09-03.md)*
