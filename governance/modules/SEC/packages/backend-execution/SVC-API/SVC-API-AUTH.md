<!-- Source: PHASE:SVC-API / SUB:SVC-API-AUTH -->
<!-- Context: see SVC-API-HEADER.md for phase-level strategy, registry table, and intro -->

<!-- API:API-SEC-001:START -->
### API-SEC-001 — Login
POST /api/v1/security/auth/login | Controller AuthController.login → AuthService.login
REQUEST LoginRequest{username, password} | RESPONSE 200 TokenResponse{accessToken, refreshToken, expiresIn}
VALIDATIONS: RULE-SEC-009 (block login when userStatusId≠ACTIVE — Message-AR: لا دخول لحساب غير نشط.);
  RULE-SEC-005 (lock after ⚠5 failed logins — Message-AR: قُفل بعد محاولات فاشلة.)
ERRORS: ERR-0011 → invalid credentials → 401; ERR-0004 → RULE-SEC-005 account locked → 423; ERR-0008 → RULE-SEC-009 non-active → 403
ORCHESTRATION: load user by username (QR-SEC-0002, EAGER roles DRV-003) → verify hash → on fail increment failedLoginCount / lock (RULE-SEC-005) → check status ACTIVE (RULE-SEC-009) → issue JWT + create refresh token (rotate).
  Note (SSO): the issued JWT is the single internal platform identity token; FILE/NOTIF trust SEC's JWT authority (auth-only; authorization stays per-module Tier-2).
REPO: QR-SEC-0002 FIND_ONE + QR-SEC-0019 SAVE(refresh) — READ_WRITE
SECURITY: public (pre-auth endpoint).
<!-- API:API-SEC-001:END -->
<!-- API:API-SEC-002:START -->
### API-SEC-002 — Refresh token
POST /auth/refresh | AuthController.refresh → AuthService.refresh
REQUEST {refreshToken} | RESPONSE 200 TokenResponse (new access + rotated refresh)
VALIDATIONS: RULE-SEC-006 (rotate refresh token; reject revoked/expired — Message-AR: يُدوَّر رمز التجديد.)
ERRORS: ERR-0005 → RULE-SEC-006 invalid/expired/revoked refresh → 401
ORCHESTRATION: hash-lookup token (QR-SEC-0019) → validate not revoked & not expired → revoke old, issue new (rotation).
REPO: QR-SEC-0019 — READ_WRITE | SECURITY: public (holds valid refresh token).
<!-- API:API-SEC-002:END -->
<!-- API:API-SEC-003:START -->
### API-SEC-003 — Logout
POST /auth/logout | AuthController.logout → AuthService.logout
REQUEST {refreshToken} | RESPONSE 204
VALIDATIONS: RULE-SEC-006 (revoke refresh token on logout)
ERRORS: ERR-0005 → RULE-SEC-006 token invalid → 401 (idempotent: already-revoked returns 204)
REPO: QR-SEC-0019 UPDATE revokedFl=1 — READ_WRITE | SECURITY: authenticated.
<!-- API:API-SEC-003:END -->
<!-- API:API-SEC-004:START -->
### API-SEC-004 — Forgot password (request reset)
POST /auth/forgot-password | AuthController.forgotPassword → AuthService.requestReset
REQUEST {email} | RESPONSE 202 (always neutral — no account enumeration)
VALIDATIONS: RULE-SEC-007 (single active, single-use reset token, TTL ⚠60m)
ERRORS: none surfaced to caller (neutral response); internal errors mapped platform-standard.
ORCHESTRATION: find active user by email → invalidate prior active reset tokens → create reset token (QR-SEC-0020) → publish CU event (NOTIF listens). SEC never calls NOTIF directly.
REPO: QR-SEC-0020 SAVE — READ_WRITE | SECURITY: public.
<!-- API:API-SEC-004:END -->
<!-- API:API-SEC-005:START -->
### API-SEC-005 — Reset password
POST /auth/reset-password | AuthController.resetPassword → AuthService.resetPassword
REQUEST {token, newPassword} | RESPONSE 200
VALIDATIONS: RULE-SEC-007 (valid, unused, unexpired token); RULE-SEC-003 (password complexity min ⚠8 letters+digits — Message-AR: كلمة المرور لا تحقق التعقيد.)
ERRORS: ERR-0006 → RULE-SEC-007 invalid/expired/used reset token → 400; ERR-0003 → RULE-SEC-003 complexity → 422
ORCHESTRATION: validate token (QR-SEC-0020) → enforce complexity (RULE-SEC-003) → set new passwordHash (RULE-SEC-004) → mark token usedFl=1.
REPO: QR-SEC-0020 UPDATE + QR-SEC-0004 UPDATE(user) — READ_WRITE | SECURITY: public (holds valid token).
<!-- API:API-SEC-005:END -->
<!-- API:API-SEC-006:START -->
### API-SEC-006 — Activate account
POST /auth/activate | AuthController.activate → AuthService.activate
REQUEST {token, newPassword?} | RESPONSE 200
VALIDATIONS: RULE-SEC-008 (valid, unused, unexpired activation token); RULE-SEC-009 (moves status PENDING_ACTIVATION→ACTIVE)
ERRORS: ERR-0007 → RULE-SEC-008 invalid/expired/used activation token → 400; ERR-0003 → RULE-SEC-003 complexity (if password set) → 422
ORCHESTRATION: validate token (QR-SEC-0021) → set userStatusId=ACTIVE → set password if provided → mark token usedFl=1.
REPO: QR-SEC-0021 UPDATE + QR-SEC-0004 UPDATE(user) — READ_WRITE | SECURITY: public.
<!-- API:API-SEC-006:END -->
