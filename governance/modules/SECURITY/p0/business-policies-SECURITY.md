⚠ REVERSE-ENGINEERING PROVENANCE NOTICE
Same notice as module-registry-SECURITY.md — this document is
reverse-constructed from the as-built erp-security codebase, not from
an upfront vision/policy session. Every RULE-SEC-ID below already
exists as a code comment/citation in the actual implementation; this
document only consolidates them into one place. See
legacy-gap-note-SECURITY.md for open items.

⚠ SCOPE DISCLAIMER (added 2026-08-22, per review): RULE-ID is
Project 1 (P1/SRS)-owned namespace per SHARED-GOVERNANCE-CORE.md
CORE-4. This document does not assign or invent any RULE-SEC-ID — it
compiles IDs that already exist verbatim in code comments (see each
entry's "Evidence" line for the exact file/line). It is NOT a
substitute for a formal P1 SRS session and should not be cited as one;
treat it as a reading aid pointing at where each rule already lives in
code, not as the canonical governed home for these rules. POLICY-CLI-*
naming below matches the convention already used in
business-policies-org.md / -filesvc.md / -notification.md for
consistency — note that POLICY-CLI itself is also not a CORE-4/CORE-7
registered ID type, just a convention shared across these
non-canonical bootstrap documents.

## BUSINESS POLICIES — SECURITY (REVERSE-ENGINEERED)
══════════════════════════════════════════════════════════════════
Module      : Security Model + Users (1.2 / 1.3)
Status      : EXCEPTION — AS-IS, already implemented
Source      : extracted-facts-SECURITY.md (code + migration script
              extraction), cross-checked against rbac.md /
              users-datascope.md / auth-menu.md (real API Docs)
══════════════════════════════════════════════════════════════════

RULE-SEC POLICIES (already assigned in code — consolidated here)
──────────────────────────────────────────────────────────────────
RULE-SEC-030: Self-registered accounts MUST start disabled
  (enabled=false) until activated.
  Evidence: AuthService.signup(), service/AuthService.java:324.

RULE-SEC-031: Password-reset/account-activation notifications are
  triggered via published Spring ApplicationEvents, NOT a direct
  NotificationService call.
  Evidence: AuthService.java lines 337-338, 399.
  Stated rationale (in code): architectural decoupling — Security does
  not call Notification directly.
  UPDATE (2026-08-22, confirmed against backend.zip): the listener is
  AuthEventListener (erp-security itself), @TransactionalEventListener
  AFTER_COMMIT, which calls NotificationClient → POST
  erp-notification's /api/v1/notifications/send. GAP-SEC-02 CLOSED —
  see POLICY-CLI-AUTH-04 below for the auth mechanism this required.

RULE-SEC-032: Account activation / password reset requires a valid,
  unused, non-expired token.
  Evidence: AuthService.activateAccount() lines 356-361,
  AuthService.resetPassword() lines 419-424.

RULE-SEC-033: Activation/reset tokens are marked used immediately on
  success and rejected if already used.
  Evidence: same methods as RULE-SEC-032, token.setUsed(true) calls.

RULE-SEC-034: SEC_USER_PROFILE.branchIdFk must reference an existing,
  active ORG_BRANCH row — validated via cross-module HTTP call
  (OrgBranchClient.assertActiveBranch()), not JPA.
  Evidence: SecUserProfileService.create()/update(),
  client/OrgBranchClient.java:46.

RULE-SEC-035: SEC_ROLE_BRANCH.dataAccessLevel is required and must be
  a valid, active LOV-SEC-002 (DATA_ACCESS_LEVEL) code.
  Evidence: SecRoleBranchService.assertValidDataAccessLevel(),
  service/SecRoleBranchService.java:143-148.

RULE-SEC-036: No duplicate (roleIdFk, branchIdFk) assignment in
  SEC_ROLE_BRANCH — enforced both by a pre-check
  (existsByRoleIdFkAndBranchIdFk) and by the composite PK itself
  (belt-and-suspenders).
  Evidence: SecRoleBranchService.create() line 68;
  SecRoleBranchId.java javadoc.

RULE-SEC-037: JWT allowedBranches[] claim is derived from the user's
  active SEC_ROLE_BRANCH assignments across their active roles, with
  an "ALL" sentinel collapsing an unbounded branch list into one
  element.
  Evidence: AuthService.resolveAllowedBranches(),
  service/AuthService.java:225-244.

RULE-SEC-038 (anti-enumeration): forgotPassword always responds
  identically regardless of whether the email exists.
  Evidence: AuthService.forgotPassword() lines 384-404; code comment:
  "response is identical whether or not the email existed; nothing
  beyond this point may branch on the Optional above."
  Stated rationale (in code): explicit anti-enumeration security
  measure.

RULE-SEC-039: Issuing a new password-reset token invalidates any
  prior unexpired token for the same user.
  Evidence: AuthService.forgotPassword() lines 385-388.

RULE-SEC-040/041: Signup username/email must be globally unique.
  Evidence: AuthService.signup() lines 313-317
  (SIGNUP_USERNAME_ALREADY_EXISTS / SIGNUP_EMAIL_ALREADY_EXISTS),
  UserAccountRepository.existsByEmailIgnoreCase().
──────────────────────────────────────────────────────────────────

RBAC POLICIES (governance tag "BE-REQ-ROLEACCESS-001" in code —
no formal RULE-SEC-ID assigned yet, see GAP-SEC-01)
──────────────────────────────────────────────────────────────────
POLICY-CLI-RBAC-01: VIEW permission is always auto-added when a Page
  is assigned to a Role; VIEW is not independently removable —
  removing a page removes VIEW + all CRUD together.
  Evidence: RoleAccessService.java lines 112-117, 169, 265-274.

POLICY-CLI-RBAC-02: CRUD permission values in role-page assignment
  requests are restricted to CREATE/UPDATE/DELETE only.
  Evidence: RoleAccessService.java lines 104-108, 172-176
  (INVALID_PERMISSION_TYPE).

POLICY-CLI-RBAC-03: syncRolePages is a full-replace of a role's
  page-scoped permissions; system-level permissions (no page FK) are
  left untouched.
  Evidence: RoleAccessService.java lines 150-213.

POLICY-CLI-RBAC-04: copyPermissionsFromRole copies only page-scoped
  permissions; a role's system-level permissions are never
  overwritten; copying from a role with zero page-scoped permissions
  or self-copy both throw specific errors
  (NO_PERMISSIONS_TO_COPY / INVALID_OPERATION).
  Evidence: RoleAccessService.java lines 295-348.

POLICY-CLI-PAGE-01: pageCode must match ^[A-Z0-9_]+$, 2-50 chars;
  route must start with / and match ^/[a-zA-Z0-9/_-]+$; both must be
  unique; parentId, if given, must reference an existing page and not
  self-reference.
  Evidence: PageService.java lines 106-113, 463-469, 116-127, 186-193.

POLICY-CLI-PAGE-02: Every Page creation auto-generates exactly 4
  Permission records (VIEW/CREATE/UPDATE/DELETE), named
  PERM_<PAGE_CODE>_<TYPE>.
  Evidence: PageService.java lines 383-418.
  Known documented exception: SCR-SEC-006 (User Profile page) needed
  only 3 permissions (no DELETE — profiles deactivate via UPDATE, not
  DELETE); worked around with direct SQL since PageService has no flag
  to suppress DELETE generation (see GAP-SEC-03).

POLICY-CLI-ROLE-01: roleCode and roleName must each be unique;
  roleCode is immutable after creation; a role with existing user
  assignments cannot be deleted (ROLE_IN_USE, 409).
  Evidence: RoleService.java lines 73-83, 156-174, 194.

POLICY-CLI-USER-01: username must be unique (case-insensitive) on
  both create and update; a user with active refresh tokens cannot be
  deleted (USER_HAS_ACTIVE_REFRESH_TOKENS, 409); new users via
  POST /api/users are auto-assigned ROLE_USER if it exists, silently
  skipped if absent.
  Evidence: UserService.java lines 62, 235-241, 200-207, 72-74.

POLICY-CLI-AUTH-01: Login rate limiting — in-memory bucket keyed by
  ip|username, blocks further attempts after a configured max with a
  configured lockout window.
  Evidence: security/LoginRateLimiterService.java.
  Known limitation (explicit in code): NOT safe for horizontal
  scaling — "must move to Redis if the backend is ever horizontally
  scaled" (see GAP-SEC-04).

POLICY-CLI-AUTH-02: Every login/refresh call revokes the prior refresh
  token and issues a new JTI (refresh-token rotation).
  Evidence: AuthService.refresh() lines 134-136.

POLICY-CLI-AUTH-03: Scheduled cleanup deletes (a) all expired refresh
  tokens regardless of revoked status, and (b) revoked refresh tokens
  older than a configured retention window.
  Evidence: scheduler/RefreshTokenCleanupJob.java.

POLICY-CLI-AUTH-04 [ADDED 2026-08-22]: Cross-module calls triggered by
  anonymous flows (signup, forgot-password) — which have no caller JWT
  to forward — authenticate via a dedicated, roleless service account
  (svc-notification). NotificationClient mints a real JWT for that
  account via JwtService.generateAccess(), the same code path used for
  real logins, and sends it as a normal Bearer token to erp-notification.
  The account is intentionally assigned zero roles (USER_ROLES has no
  row for it) — it can authenticate but holds no authorities beyond
  that, matching the minimal-privilege shape of any other service
  account. Password is a BCrypt hash of a random, never-recorded value
  — this account is never expected to log in via username/password.
  Evidence: client/NotificationClient.java (XM-SEC-005 in its own
  javadoc), db/scripts/005_notification_service_account_seed.sql.
  Failure handling: if the service account isn't seeded yet, or the
  HTTP call itself fails, NotificationClient logs a warning and
  returns — it never fails the already-committed signup/reset flow
  that triggered it.
──────────────────────────────────────────────────────────────────

CUSTOM LOV VALUES
──────────────────────────────────────────────────────────────────
DATA_ACCESS_LEVEL (LOV-SEC-002, seeded by SECURITY into MASTERDATA's
tables): BRANCH_ONLY, BRANCH_AND_CHILDREN, ALL.
PermissionType (Java enum, not a governed LOV): VIEW, CREATE, UPDATE,
DELETE.
──────────────────────────────────────────────────────────────────

SCOPE / ARCHITECTURE NOTES
──────────────────────────────────────────────────────────────────
- SECURITY has zero Maven/compile-time dependency on ORG or
  MasterData — all cross-module calls are same-JVM internal HTTP
  (RestTemplate to localhost), not @Service injection. This differs
  from the ORG/FILE/NOTIF direct-injection convention.
- Bootstrap seed data (migration 001 STEP 4) is explicitly destructive
  (deletes all rows from 7 tables before reseeding) and creates
  well-known admin/admin credentials — explicitly flagged
  "local/dev bootstrap — never run against staging or production."
- Migrations are NOT run by an automated tool (no Flyway wired up) —
  explicitly "run manually by DBA (psql/pgAdmin)."
──────────────────────────────────────────────────────────────────

══════════════════════════════════════════════════════════════════
