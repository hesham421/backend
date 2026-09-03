<!-- Source: PHASE:TEST-PLAN-BE / SUB:RULE-SCENARIOS -->

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
