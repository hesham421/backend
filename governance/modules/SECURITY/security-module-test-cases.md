# Security Module — Full Test Case Suite

Source: `index.md` API Catalog (49 endpoints, 8 groups) + `execution-plan-SEC-gaps.md` +
`security-registry.md` (confirmed runtime error codes). For use writing Playwright
automation (API testing via Playwright's `request` context, or UI if these back a real
front end).

> ⚠ **Governance note carried over from script generation**: the Self-Service Auth,
> Security DataScope – User Profiles, and Security DataScope – Role Branches sections
> (12 endpoints) are the SEC-002 gap package. `execution-plan-SEC-gaps.md` marks
> agent-execution against these as blocked pending "Conflict #19" architecture sign-off.
> These test cases are written anyway per your instruction — flagging again here since
> it's a fresh document, not just the script.

---

## 0. Conventions

| Field | Meaning |
|---|---|
| TC-ID | `TC-<MODULE>-###` |
| Type | Positive / Negative / Boundary / Exploratory (outcome undocumented) |
| Auth | Whether a Bearer token is required, and whose (Admin / Self / Public) |

All endpoints except the four under `/api/auth/**` (login, signup, activate,
forgot-password, reset-password — and by extension refresh/logout which need no *new*
login) require `Authorization: Bearer <token>`. Response envelope for every endpoint:
`{ success, message, data, error, timestamp }`; paginated endpoints wrap `data` in
`{ content, totalElements, totalPages, size, number, numberOfElements, first, last,
sort, pageable, empty }` (page index field is `number`, not `page`).

---

## 1. Suggested Execution / Dependency Order

Run suites in this order — later suites reuse IDs/codes created by earlier ones.

```
1. Authentication            (admin token — needed by everything below)
2. Page Management           (root entity)
3. Permission Management     (optional FK → Page)
4. Role Access Control       (root entity; Add/Sync Page needs a Page from step 2)
5. User Management           (root entity; Assign Roles needs a Role from step 4)
6. Menu Management           (needs a User from step 5)
7. Security DataScope – User Profiles   (needs a User from step 5; needs an external Branch ID)
8. Security DataScope – Role Branches   (needs a Role from step 4; needs an external Branch ID)
9. Self-Service Auth          (independent — creates its own throwaway users)
10. Cross-Module E2E Flows    (needs everything above)
```

---

## 2. Authentication (`/api/auth/login`, `/login-token`, `/refresh`, `/logout`)

| TC-ID | Title | Type | Steps | Expected |
|---|---|---|---|---|
| TC-AUTH-001 | Admin login — happy path | Positive | POST `/api/auth/login` with valid admin username/password | `200`; `data.accessToken`, `data.expiresIn` (900), `data.refreshToken`, `data.refreshExpiresIn` (604800) present |
| TC-AUTH-002 | Login — wrong password | Negative | POST `/api/auth/login` with valid username, wrong password | `401 UNAUTHORIZED` |
| TC-AUTH-003 | Login — nonexistent username | Negative | POST `/api/auth/login` with unknown username | `401 UNAUTHORIZED` |
| TC-AUTH-004 | Login — malformed JSON body | Negative | POST `/api/auth/login` with invalid JSON | `400 INVALID_JSON` (documented structural response) |
| TC-AUTH-005 | Login — omit password field | Exploratory | POST `/api/auth/login` with only `username` | Undocumented — observe actual status (likely `400`) |
| TC-AUTH-006 | Login With Token — happy path | Positive | POST `/api/auth/login-token` with valid credentials | `200`; `data` = full `UserInfo` incl. `roles[]`, `permissions[]`, `userId`, `enabled` |
| TC-AUTH-007 | Refresh access token — happy path | Positive | POST `/api/auth/refresh` with valid refresh-token cookie/token from a prior login | `200`; new `accessToken`/`refreshToken` pair |
| TC-AUTH-008 | Refresh — expired/revoked token | Negative | Call `/api/auth/refresh` after logout or after token TTL | `401` — security-registry.md documents `REFRESH_EXPIRED_OR_REVOKED` / `REFRESH_REVOKED` as the underlying reasons |
| TC-AUTH-009 | Logout — happy path | Positive | POST `/api/auth/logout` with valid token | `204 No Content`; all tokens for the session invalidated |
| TC-AUTH-010 | Logout — no token | Negative | POST `/api/auth/logout` with no Authorization header | `401 UNAUTHORIZED` |
| TC-AUTH-011 | Use access token after logout | Negative | Call any authenticated endpoint using the token from TC-AUTH-009 | Expect `401` (token invalidated) — confirms logout actually revokes |

---

## 3. Self-Service Auth (`/api/auth/signup`, `/signup/activate`, `/forgot-password`, `/reset-password`)

> ⚠ SEC-002 gap package — see governance note at top of document.
> **Known limitation**: no endpoint returns the raw activation/reset token (delivered
> only via `AccountActivationRequestedEvent` / `PasswordResetRequestedEvent`, and no
> NotificationService subscriber exists yet per `security-registry.md` §2.7). Happy-path
> activate/reset **cannot be automated end-to-end** without direct DB/log access to read
> the issued token — flag this to whoever owns the test environment; these cases are
> written assuming that access exists, with a fallback "invalid token" alternative.

| TC-ID | Title | Type | Steps | Expected |
|---|---|---|---|---|
| TC-SSA-001 | Signup — happy path | Positive | POST `/api/auth/signup` with unique username (3-80), valid email (≤150), password (6-120) | `200`; `data = {userId, username, enabled}`; `data.enabled === false` (RULE-SEC-030) |
| TC-SSA-002 | Signup — duplicate username | Negative | Signup twice with the same username, different email | `409`; `error.code = SIGNUP_USERNAME_ALREADY_EXISTS` (confirmed runtime code, distinct from admin-created-user's `USERNAME_ALREADY_EXISTS`) |
| TC-SSA-003 | Signup — duplicate email | Negative | Signup twice with the same email, different username | `409`; `error.code = SIGNUP_EMAIL_ALREADY_EXISTS` |
| TC-SSA-004 | Signup — username under minLength (2 chars) | Boundary | `username: "ab"` | `400` expected (minLength=3) — confirm exact status |
| TC-SSA-005 | Signup — username at maxLength (80 chars) | Boundary | `username` = exactly 80 chars | `200` — should succeed at the limit |
| TC-SSA-006 | Signup — username over maxLength (81 chars) | Boundary | `username` = 81 chars | `400` expected |
| TC-SSA-007 | Signup — password under minLength (5 chars) | Boundary | `password: "abcde"` | `400` expected (minLength=6) |
| TC-SSA-008 | Signup — email over maxLength (151 chars) | Boundary | `email` = 151-char string (still valid email shape) | `400` expected (maxLength=150) |
| TC-SSA-009 | Signup — omit required email | Exploratory | Omit `email` field entirely | Undocumented — observe actual status |
| TC-SSA-010 | Signup — malformed email format | Exploratory | `email: "not-an-email"` | Undocumented whether format is validated — observe |
| TC-SSA-011 | Activate — happy path | Positive (needs token access) | Retrieve the real activation token issued in TC-SSA-001 (DB/log), POST `/api/auth/signup/activate` | `200`, empty body; user's `enabled` flips to `true`; subsequent login with that user succeeds |
| TC-SSA-012 | Activate — invalid/unknown token | Negative | POST `/api/auth/signup/activate` with a random UUID-shaped token | `400`; `error.code = ACTIVATION_TOKEN_INVALID_OR_EXPIRED` |
| TC-SSA-013 | Activate — already-used token | Negative | Re-submit the same token used successfully in TC-SSA-011 | `400`; `error.code = TOKEN_ALREADY_USED` |
| TC-SSA-014 | Activate — expired token | Negative (needs time control or short TTL env) | Wait past `activation-expiration-seconds` (default 24h) then activate | `400`; `error.code = ACTIVATION_TOKEN_INVALID_OR_EXPIRED` |
| TC-SSA-015 | Activate — omit token field | Exploratory | POST with empty body / no `token` | Undocumented — observe |
| TC-SSA-016 | Forgot Password — existing email | Positive | POST `/api/auth/forgot-password` with an email that exists | `200`, empty body (RULE-SEC-038 anti-enumeration — same response either way) |
| TC-SSA-017 | Forgot Password — nonexistent email | Positive | POST with an email that does not exist | `200`, empty body — **must be identical in shape/timing to TC-SSA-016**; this is the core anti-enumeration assertion, worth a dedicated Playwright check comparing both responses |
| TC-SSA-018 | Forgot Password — issuing a new token invalidates the prior one | Positive (needs token access) | Call forgot-password twice for the same user; attempt to use the *first* issued token | `400 TOKEN_ALREADY_USED` or `ACTIVATION`-equivalent — confirms RULE-SEC-039 |
| TC-SSA-019 | Reset Password — happy path | Positive (needs token access) | Retrieve real reset token, POST `/api/auth/reset-password` with valid `newPassword` | `200`, empty body; subsequent login with new password succeeds, old password fails |
| TC-SSA-020 | Reset Password — invalid/unknown token | Negative | POST with a random token | `400`; `error.code = RESET_TOKEN_INVALID_OR_EXPIRED` |
| TC-SSA-021 | Reset Password — already-used token | Negative | Re-submit a token already consumed | `400`; `error.code = TOKEN_ALREADY_USED` |
| TC-SSA-022 | Reset Password — newPassword under minLength | Boundary | `newPassword: "abcde"` (5 chars) with a valid token | `400` expected (minLength=6) |
| TC-SSA-023 | Reset Password — newPassword over maxLength (121 chars) | Boundary | 121-char password | `400` expected (maxLength=120) |
| TC-SSA-024 | Rate limiting on signup/login/forgot-password/reset-password | Exploratory | Rapidly repeat POSTs to each of these 4 endpoints from the same IP+identifier | `security-registry.md` §5.8 documents `LoginRateLimitFilter` covering login/signup, and forgot/reset-password as a *planned* extension (flagged as SEC-phase task, not confirmed shipped) — verify whether 429 or similar appears; if not, that's worth flagging back to the team, not assuming it's a bug |

---

## 4. Page Management (`/api/pages`)

| TC-ID | Title | Type | Steps | Expected |
|---|---|---|---|---|
| TC-PAGE-001 | Create Page — happy path | Positive | POST `/api/pages` with unique `pageCode`, `nameAr`, `nameEn`, `route` | `200/201`; response includes auto-generated `permissionKeys` (VIEW/CREATE/UPDATE/DELETE) |
| TC-PAGE-002 | Create Page — pageCode normalized to uppercase | Positive | POST with `pageCode: "user"` (lowercase) | Response `pageCode` should come back as `"USER"` |
| TC-PAGE-003 | Get Page by ID — happy path | Positive | GET `/api/pages/{id}` for the page from TC-PAGE-001 | `200`; full `PageResponse` |
| TC-PAGE-004 | Get Page by ID — not found | Negative | GET `/api/pages/999999999` | `404` |
| TC-PAGE-005 | Update Page — happy path | Positive | PUT `/api/pages/{id}` changing `nameEn`/`route` | `200`; `pageCode` unchanged (cannot be updated per doc) |
| TC-PAGE-006 | Update Page — attempt to change pageCode | Exploratory | PUT with a `pageCode` field included (not in `UpdatePageRequest` schema) | Should be ignored per doc ("pageCode cannot be changed") — verify it's silently dropped, not erroring |
| TC-PAGE-007 | Deactivate Page — happy path | Positive | PUT `/api/pages/{id}/deactivate` | `200`; `active: false` |
| TC-PAGE-008 | Reactivate Page — happy path | Positive | PUT `/api/pages/{id}/reactivate` | `200`; `active: true` |
| TC-PAGE-009 | Deactivate an already-deactivated page | Exploratory | Call deactivate twice in a row | Undocumented idempotency — observe (likely `200` no-op, or `409`) |
| TC-PAGE-010 | Get Active Pages | Positive | GET `/api/pages/active` | `200`; array excludes the page deactivated in TC-PAGE-007 |
| TC-PAGE-011 | Search Pages — no filters | Positive | POST `/api/pages/search` with empty `filters`/`sorts` | `200`; paginated envelope, `content` array present |
| TC-PAGE-012 | Search Pages — filter by pageCode | Positive | POST with `filters: [{field:"pageCode", operator:"EQ", value:"USER"}]` | `200`; only matching page(s) returned |
| TC-PAGE-013 | Search Pages — sort by nameEn desc | Positive | POST with `sorts: [{field:"nameEn", direction:"DESC"}]` | `200`; sort order respected |
| TC-PAGE-014 | Search Pages — pagination (`page`, `size`) | Positive | POST with `page:1, size:5` after creating 6+ pages | `200`; `number=1`, `size=5`, `content.length ≤ 5` |
| TC-PAGE-015 | Create Page — omit required nameEn | Exploratory | POST omitting `nameEn` | Undocumented status — observe (likely `400`) |
| TC-PAGE-016 | Create Page — pageCode at maxLength (50 chars) | Boundary | 50-char `pageCode` | Should succeed |
| TC-PAGE-017 | Create Page — pageCode over maxLength (51 chars) | Boundary | 51-char `pageCode` | Undocumented — observe |
| TC-PAGE-018 | Create Page — route at maxLength (200 chars) | Boundary | 200-char `route` | Should succeed |
| TC-PAGE-019 | Create Page — duplicate pageCode | Exploratory | POST twice with same `pageCode` | Undocumented (no ERR-ID in this catalog for this) — observe, likely `409` given it's described as "unique" |
| TC-PAGE-020 | Create Page with parentId — parent must exist | Exploratory | POST with `parentId: 999999999` | Undocumented in this catalog for Pages (a sibling rule "PARENT_PAGE_NOT_FOUND" exists in `security-registry.md` rule 20 for a different context — verify whether it also applies here) |
| TC-PAGE-021 | Auto-generated permissions exist after Create | Positive | After TC-PAGE-001, call Permission Management's Search filtered by the new `pageId` | 4 permission rows exist: VIEW/CREATE/UPDATE/DELETE, `pageCode` matches |
| TC-PAGE-022 | Unauthorized — no token | Negative | Call any Page endpoint without Authorization header | `401 UNAUTHORIZED` |
| TC-PAGE-023 | Forbidden — token lacks PAGE_CREATE/UPDATE/VIEW | Negative | Call with a token for a user/role missing the specific permission | `403 FORBIDDEN` |

---

## 5. Permission Management (`/api/permissions`)

| TC-ID | Title | Type | Steps | Expected |
|---|---|---|---|---|
| TC-PERM-001 | Create Permission — standalone (no pageId) | Positive | POST `/api/permissions` with `name` only | `200/201`; `pageId`/`pageCode` null |
| TC-PERM-002 | Create Permission — scoped to a page | Positive | POST with `pageId` from an existing Page, `permissionType: "VIEW"` | `200/201`; `pageCode` populated |
| TC-PERM-003 | Update Permission — happy path | Positive | PUT `/api/permissions/{id}` with new `name` | `200` |
| TC-PERM-004 | Update Permission — not found | Negative | PUT `/api/permissions/999999999` | Undocumented in catalog (no explicit 404 row) — observe |
| TC-PERM-005 | Search Permissions — filter by name | Positive | POST `/api/permissions/search` with `filters:[{field:"name",operator:"LIKE",value:"USER"}]` | `200`; matches auto-generated `PERM_USER_*` rows |
| TC-PERM-006 | Search Permissions — filter by module | Positive | POST with `filters:[{field:"module", operator:"EQ", value:"SECURITY"}]` | `200` |
| TC-PERM-007 | Create Permission — omit required name | Exploratory | POST omitting `name` | Undocumented — observe |
| TC-PERM-008 | Create Permission — duplicate name | Exploratory | POST twice with the same `name` | Undocumented (name likely unique given `PERM_USER_VIEW`-style constants) — observe |
| TC-PERM-009 | No delete/deactivate endpoint exists | N/A (documentation check) | Confirm no DELETE or deactivate path for Permissions anywhere in the API Catalog | Confirmed absent — created test permissions cannot be cleaned up via API; Playwright suite should use a dedicated test-only naming prefix (e.g. `PERM_PWTEST_*`) to make manual DB cleanup easy |

---

## 6. Role Access Control (`/api/roles`)

| TC-ID | Title | Type | Steps | Expected |
|---|---|---|---|---|
| TC-ROLE-001 | Create Role — happy path | Positive | POST `/api/roles` with `roleCode` matching `^[A-Z][A-Z0-9_]*$`, `roleName` | `200/201` |
| TC-ROLE-002 | Get Role by ID — happy path | Positive | GET `/api/roles/{id}` | `200` |
| TC-ROLE-003 | Get Role by ID — not found | Negative | GET `/api/roles/999999999` | `404` |
| TC-ROLE-004 | Update Role — happy path | Positive | PUT `/api/roles/{id}` with new `roleName`/`description` | `200`; `roleCode` unchanged |
| TC-ROLE-005 | Toggle Role Active — deactivate | Positive | PUT `/api/roles/{id}/toggle-active` `{active:false}` | `200`; `active:false` |
| TC-ROLE-006 | Toggle Role Active — reactivate | Positive | PUT `.../toggle-active` `{active:true}` | `200`; `active:true` |
| TC-ROLE-007 | Search Roles — filter by roleName | Positive | POST `/api/roles/search` with `filters:[{field:"roleName",operator:"LIKE",value:"Test"}]` | `200` |
| TC-ROLE-008 | Add Page to Role — happy path | Positive | POST `/api/roles/{roleId}/pages` with existing `pageCode`, `permissions:["CREATE","UPDATE"]` | `200`; response `permissions` excludes VIEW (implicit) but VIEW is granted |
| TC-ROLE-009 | Get Role Pages Matrix | Positive | GET `/api/roles/{roleId}/pages` | `200`; assignment from TC-ROLE-008 appears |
| TC-ROLE-010 | Bulk Sync Role Pages — full replace | Positive | PUT `/api/roles/{roleId}/pages` with a different assignment set | `200`; old assignments gone, only new set present |
| TC-ROLE-011 | Bulk Sync Role Pages — empty array removes all | Positive | PUT with `assignments: []` | `200`; `getRolePages` afterward returns empty list |
| TC-ROLE-012 | Remove Page From Role | Positive | DELETE `/api/roles/{roleId}/pages/{pageCode}` | `204` |
| TC-ROLE-013 | Copy Permissions From Role — happy path | Positive | Create source role with page assignments; POST `/api/roles/{targetId}/copy-from/{sourceId}` | `200`; target's page-scoped assignments now match source; target's system-level permissions (e.g. `PERM_SYSTEM_ADMIN`) untouched (privilege-escalation guard) |
| TC-ROLE-014 | Delete Role — happy path (no user assignments) | Positive | DELETE `/api/roles/{roleId}` for a role never assigned to a user | `204` |
| TC-ROLE-015 | Delete Role — blocked, has user assignments | Negative | Assign the role to a user (see User Management), then DELETE the role | `409` |
| TC-ROLE-016 | Create Role — roleCode violates pattern (lowercase) | Boundary | `roleCode: "admin_test"` | Undocumented exact status — observe (expect `400`) |
| TC-ROLE-017 | Create Role — roleCode violates pattern (starts with digit) | Boundary | `roleCode: "1ADMIN"` | Undocumented — observe |
| TC-ROLE-018 | Create Role — omit required roleName | Exploratory | POST omitting `roleName` | Undocumented — observe |
| TC-ROLE-019 | Create Role — duplicate roleCode | Exploratory | POST twice with same `roleCode` | Undocumented in this catalog — observe (likely `409`, mirrors "unique role code" framing) |
| TC-ROLE-020 | Add Page to Role — pageCode doesn't exist | Exploratory | POST with a random `pageCode` | Undocumented — observe |
| TC-ROLE-021 | Copy Permissions From Role — sourceRoleId doesn't exist | Exploratory | POST `/api/roles/{targetId}/copy-from/999999999` | Undocumented — observe |
| TC-ROLE-022 | Add Page to Role — omit required permissions array | Exploratory | POST with `pageCode` only, no `permissions` | Undocumented — observe (VIEW is "always added" per doc, so an empty/omitted array might still succeed) |

---

## 7. User Management (`/api/users`)

| TC-ID | Title | Type | Steps | Expected |
|---|---|---|---|---|
| TC-USER-001 | Create User — happy path (admin-created) | Positive | POST `/api/users` with unique `username` (3-80), `password` (6-120) | `200/201`; no `email` field in this flow (admin-created users don't have one, per security-registry.md §2.7 note) |
| TC-USER-002 | Create User — duplicate username | Negative | POST twice with same `username` | `409 USERNAME_ALREADY_EXISTS` (distinct from Signup's `SIGNUP_USERNAME_ALREADY_EXISTS`) |
| TC-USER-003 | List All Users — happy path | Positive | GET `/api/users?pageable=username,asc` | `200`; paginated envelope |
| TC-USER-004 | Search Users — filter by enabled | Positive | POST `/api/users/search` with `filters:[{field:"enabled",operator:"EQ",value:true}]` | `200` |
| TC-USER-005 | Search Users — LIKE operator on username | Positive | POST with `filters:[{field:"username",operator:"LIKE",value:"test"}]` | `200`; case-insensitive contains match per doc |
| TC-USER-006 | Update User — change enabled flag | Positive | PUT `/api/users/{id}` `{enabled:false}` | `200`; user can no longer log in |
| TC-USER-007 | Update User — change password | Positive | PUT with new `password` | `200`; login with old password fails, new password succeeds |
| TC-USER-008 | Assign Roles to User — happy path | Positive | PUT `/api/users/{id}/roles` `{roleNames:["TESTROLE_X"]}` | `200`; `data.roles` contains `TESTROLE_X` |
| TC-USER-009 | Assign Roles — full replace semantics | Positive | Assign `["ROLE_A"]`, then assign `["ROLE_B"]` | After 2nd call, user has ONLY `ROLE_B`, not both |
| TC-USER-010 | Assign Roles — empty array clears all roles | Positive | PUT `{roleNames:[]}` | `200`; `getUserRoles` afterward returns empty |
| TC-USER-011 | Get User Roles | Positive | GET `/api/users/{id}/roles` | `200`; array of role names |
| TC-USER-012 | Delete User — happy path (no dependencies) | Positive | DELETE `/api/users/{id}` for a user with no active refresh tokens | `204` |
| TC-USER-013 | Delete User — blocked, has active refresh token | Negative | Log in as the user (creating a refresh token), then DELETE without logging out first | Undocumented exact status in catalog — likely `409`; observe and confirm |
| TC-USER-014 | Create User — username under minLength (2 chars) | Boundary | `username: "ab"` | Undocumented exact status — observe (expect `400`) |
| TC-USER-015 | Create User — username at maxLength (80 chars) | Boundary | 80-char username | Should succeed |
| TC-USER-016 | Create User — password under minLength (5 chars) | Boundary | `password: "abcde"` | Undocumented — observe (expect `400`) |
| TC-USER-017 | Assign Roles — role name doesn't exist | Exploratory | PUT with `roleNames:["DOES_NOT_EXIST"]` | Undocumented — observe |
| TC-USER-018 | Unauthorized — assignRoles without USER_MANAGE_ROLES permission | Negative | Call with a token lacking `USER_MANAGE_ROLES` | `403 FORBIDDEN` |

---

## 8. Menu Management (`/api/menu`)

| TC-ID | Title | Type | Steps | Expected |
|---|---|---|---|---|
| TC-MENU-001 | Get Current User Menu — admin | Positive | GET `/api/menu/user-menu` as admin | `200`; tree includes every page admin has VIEW on |
| TC-MENU-002 | Get Current User Menu — reflects permission changes | Positive | Remove a page's VIEW-granting role from a test user (via Sync Role Pages), log in as that user, GET menu | The removed page's entry is absent |
| TC-MENU-003 | Get Menu For Specific User (Admin) — happy path | Positive | GET `/api/menu/user-menu/{userId}` for another user, as admin | `200`; matches that user's own menu |
| TC-MENU-004 | Get Menu For Specific User — forbidden without USER_VIEW | Negative | Call as a user without `USER_VIEW` permission | `403 FORBIDDEN` |
| TC-MENU-005 | Get Menu For Specific User — nonexistent userId | Exploratory | GET `/api/menu/user-menu/999999999` | Undocumented in catalog — observe (likely empty array rather than 404) |
| TC-MENU-006 | Menu tree parent/child structure | Positive | Create a page with `parentId` pointing at another page; check menu output | Child page's `parentId` in response links correctly to the parent's `id` |

---

## 9. Security DataScope – User Profiles (`/api/v1/security/user-profiles`)

> ⚠ SEC-002 gap package. Not permission-gated — any authenticated user can call these
> today (security-registry.md §2.8 — deliberate, Phase SEC work not yet done).
> Needs an existing, **active** external Branch (`branchIdFk` → `ORG_BRANCH`,
> Organization module) — no Branch CRUD exists in this catalog; coordinate with
> whoever owns Organization module test data.

| TC-ID | Title | Type | Steps | Expected |
|---|---|---|---|---|
| TC-UP-001 | Create User Profile — happy path | Positive | POST with `userIdFk` (existing user, no profile yet), `branchIdFk` (existing active branch) | `200/201`; `isActiveFl: true` |
| TC-UP-002 | Create User Profile — profile already exists for this user | Negative | POST again for the same `userIdFk` | `409`; `error.code = SEC_USER_PROFILE_ALREADY_EXISTS` |
| TC-UP-003 | Create User Profile — userIdFk doesn't exist | Negative | POST with `userIdFk: 999999999` | `404`; `error.code = USER_NOT_FOUND` |
| TC-UP-004 | Create User Profile — branchIdFk inactive/nonexistent | Negative | POST with `branchIdFk: 999999999` (or a branch known to be deactivated) | `400`; `error.code = SEC_USER_PROFILE_BRANCH_INACTIVE` (RULE-SEC-034) |
| TC-UP-005 | Get User Profile by ID — happy path | Positive | GET `/api/v1/security/user-profiles/{userId}` | `200` |
| TC-UP-006 | Get User Profile by ID — not found | Negative | GET `/{userId}` for a user with no profile | `404`; `error.code = SEC_USER_PROFILE_NOT_FOUND` |
| TC-UP-007 | Update User Profile — happy path | Positive | PUT with new `branchIdFk` (still active), `fullNameEn` | `200` |
| TC-UP-008 | Update User Profile — re-validates branch is active | Negative | PUT with an inactive/nonexistent `branchIdFk` | `400`; `error.code = SEC_USER_PROFILE_BRANCH_INACTIVE` |
| TC-UP-009 | List User Profiles — default sort | Positive | GET `?pageable=userIdFk,asc` | `200`; sorted ascending by `userIdFk` |
| TC-UP-010 | Search User Profiles — filter by branchIdFk | Positive | POST `/search` with `filters:[{field:"branchIdFk",operator:"EQ",value:1}]` | `200` |
| TC-UP-011 | fullNameAr over maxLength (201 chars) | Boundary | 201-char `fullNameAr` | `400` expected (maxLength=200) |
| TC-UP-012 | fullNameEn over maxLength (101 chars) | Boundary | 101-char `fullNameEn` | `400` expected (maxLength=100) |
| TC-UP-013 | preferredLang over maxLength (11 chars) | Boundary | 11-char `preferredLang` | `400` expected (maxLength=10) — note OQ-004: domain otherwise undefined |
| TC-UP-014 | employeeIdFk — fully unconstrained | Exploratory | POST/PUT with an arbitrary `employeeIdFk` (no HR module exists to validate against, OQ-005) | Should be accepted as-is, no FK validation — confirm no unexpected `404`/`400` |
| TC-UP-015 | No DELETE endpoint exists | N/A (documentation check) | Confirm absence of any delete/deactivate path | Confirmed absent — profiles created by automated tests are permanent; use dedicated test branch/users to make manual cleanup traceable |
| TC-UP-016 | Create User Profile — omit required branchIdFk | Exploratory | POST omitting `branchIdFk` | Undocumented exact status — observe (expect `400`) |

---

## 10. Security DataScope – Role Branches (`/api/v1/security/role-branches`)

> ⚠ SEC-002 gap package. Same permission-gating and external-Branch-dependency notes as
> User Profiles above (§9).

| TC-ID | Title | Type | Steps | Expected |
|---|---|---|---|---|
| TC-RB-001 | Assign Branch Scope to Role — happy path | Positive | POST with `roleIdFk`, `branchIdFk` (active), `dataAccessLevel: "BRANCH_ONLY"` | `200/201`; `isActiveFl: true` |
| TC-RB-002 | Assign Branch Scope — roleIdFk doesn't exist | Negative | POST with `roleIdFk: 999999999` | `404`; `error.code = ROLE_NOT_FOUND` |
| TC-RB-003 | Assign Branch Scope — duplicate (roleIdFk, branchIdFk) | Negative | POST twice with the same pair | `409`; `error.code = SEC_ROLE_BRANCH_DUPLICATE_ASSIGNMENT` (RULE-SEC-036) |
| TC-RB-004 | Assign Branch Scope — omit dataAccessLevel | Negative | POST omitting `dataAccessLevel` | `400`; `error.code = SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED` (RULE-SEC-035) |
| TC-RB-005 | Assign Branch Scope — invalid dataAccessLevel value | Negative | POST with `dataAccessLevel: "NOT_A_REAL_LEVEL"` | `400`; same `SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED` per doc ("or not a valid LOV-SEC-002 code") |
| TC-RB-006 | Get Role-Branch Assignment — happy path | Positive | GET `/{roleId}/{branchId}` | `200` |
| TC-RB-007 | Get Role-Branch Assignment — not found | Negative | GET for a (roleId, branchId) pair never assigned | `404`; `error.code = SEC_ROLE_BRANCH_NOT_FOUND` |
| TC-RB-008 | Update Role-Branch Assignment — happy path | Positive | PUT `/{roleId}/{branchId}` with `dataAccessLevel: "BRANCH_AND_CHILDREN"` | `200` |
| TC-RB-009 | Update Role-Branch Assignment — re-validates LOV | Negative | PUT with an invalid `dataAccessLevel` | `400`; `SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED` |
| TC-RB-010 | Delete Role-Branch Assignment — happy path | Positive | DELETE `/{roleId}/{branchId}` | `204` |
| TC-RB-011 | List Role-Branch Assignments — sort by dataAccessLevel | Positive | GET `?pageable=dataAccessLevel,asc` | `200` |
| TC-RB-012 | Search Role-Branch Assignments — filter by roleIdFk | Positive | POST `/search` with `filters:[{field:"roleIdFk",operator:"EQ",value:<id>}]` | `200` |
| TC-RB-013 | Composite-key uniqueness enforced at DB level too | Exploratory | Attempt to create the same (roleIdFk, branchIdFk) via two near-simultaneous requests (race test) | Exactly one should succeed; the other gets `409` — worth a dedicated concurrency test in Playwright |
| TC-RB-014 | All 3 documented dataAccessLevel values accepted | Positive | Create 3 separate assignments (different branches) with `BRANCH_ONLY`, `BRANCH_AND_CHILDREN`, `ALL` | All `200/201` |

---

## 11. Cross-Module End-to-End Flows

These are the flows worth automating as full Playwright scenarios rather than isolated
endpoint calls — they're where real regressions tend to hide.

| TC-ID | Title | Flow |
|---|---|---|
| TC-E2E-001 | New page → permissions auto-generate → role gets access → user sees it in menu | 1. Create Page (`PAGE_A`) → 2. Search Permissions filtered by the new page's `pageId`, confirm 4 rows (VIEW/CREATE/UPDATE/DELETE) exist → 3. Create Role → 4. Add Page to Role with `["CREATE","UPDATE"]` → 5. Create User → 6. Assign Role to User → 7. Login as that user → 8. GET `/api/menu/user-menu` as that user, confirm `PAGE_A` appears (VIEW is implicit) → 9. Get Role Pages Matrix, confirm CREATE+UPDATE (not DELETE) show for `PAGE_A` |
| TC-E2E-002 | Remove page access → user immediately loses menu item | Continuing from TC-E2E-001: 1. Remove Page From Role → 2. Re-login (or refresh) as the test user → 3. GET user-menu → confirm `PAGE_A` no longer present |
| TC-E2E-003 | Copy role permissions preserves system-level, replaces page-level | 1. Create `ROLE_SOURCE` with 2 page assignments + (if obtainable) a system-level permission like `PERM_SYSTEM_ADMIN` → 2. Create `ROLE_TARGET` with 1 different page assignment + its own system-level permission → 3. Copy From Role (`ROLE_SOURCE` → `ROLE_TARGET`) → 4. Get Role Pages Matrix for `ROLE_TARGET`: page-scoped assignments now match `ROLE_SOURCE`'s 2 pages, `ROLE_TARGET`'s original page assignment is gone, but `ROLE_TARGET`'s own system-level permission is still present (untouched) |
| TC-E2E-004 | Full self-service onboarding (signup → activate → login → menu) | 1. Signup → 2. Confirm `enabled:false` and login fails (`401`) → 3. Retrieve activation token (env-dependent) → 4. Activate → 5. Login succeeds → 6. GET user-menu for the new user — confirm it reflects whatever default role (if any) self-registered users get; if none, menu should be empty, not an error |
| TC-E2E-005 | Password reset flow end-to-end | 1. Create a user via signup+activate (or admin-created) → 2. Forgot Password → 3. Retrieve reset token (env-dependent) → 4. Reset Password with new value → 5. Login with OLD password → `401` → 6. Login with NEW password → `200` |
| TC-E2E-006 | Delete Role is blocked while a user holds it, unblocks after unassignment | 1. Create Role → 2. Create User → 3. Assign Role to User → 4. Attempt Delete Role → `409` → 5. Assign Roles to User with `[]` (clears it) → 6. Attempt Delete Role again → `204` |
| TC-E2E-007 | Role-Branch scope combined with User Profile branch | 1. Create Role, assign a Branch Scope (`BRANCH_ONLY`, branch X) → 2. Create User, assign the Role → 3. Create a User Profile for that user pointing at branch X → 4. (If any data-scoped endpoint exists elsewhere in the system) confirm the user's visible data is limited to branch X — this test case is a placeholder for whichever consuming module actually enforces `allowedBranches[]` from the JWT claim; Security module alone doesn't expose an endpoint that visibly proves this |
| TC-E2E-008 | Logout invalidates access across all suites | 1. Login → 2. Call any authenticated GET successfully → 3. Logout → 4. Repeat the same GET with the same (now stale) token → `401` |
| TC-E2E-009 | Duplicate-prevention consistency between admin-created and self-registered users | 1. Admin-create a user `alice` → 2. Attempt Signup with `username: "alice"` | Expect `409 SIGNUP_USERNAME_ALREADY_EXISTS` — confirms the two flows share the same `UK_USERS_USERNAME` constraint despite different error codes/messages |

---

## 12. Non-Functional / Cross-Cutting Cases (apply across all modules)

| TC-ID | Title | Type | Notes |
|---|---|---|---|
| TC-XCUT-001 | Every authenticated endpoint rejects missing token | Negative | `401 UNAUTHORIZED` — parametrize across all 49 endpoints |
| TC-XCUT-002 | Every authenticated endpoint rejects expired token | Negative | `401` |
| TC-XCUT-003 | Every permission-gated endpoint rejects insufficient permission | Negative | `403 FORBIDDEN` — only applies to endpoints with a documented `Required permission(s)` row (Page/Role/Menu-by-id/assignRoles); NOT the DataScope endpoints (deliberately ungated, see §9/§10 notes) or the `/api/auth/**` public group |
| TC-XCUT-004 | Every `@RequestBody` endpoint rejects malformed JSON | Negative | `400 INVALID_JSON` — documented structurally on every POST/PUT endpoint in the catalog |
| TC-XCUT-005 | Pagination `number` field, not `page` | Positive | Assert response envelope field name directly on any one search/list call — guards against a common wrong assumption |
| TC-XCUT-006 | Response envelope shape consistency | Positive | Assert `{success, message, data, error, timestamp}` top-level keys exist on every response, success or error |

---

## 13. Test Data Cleanup Notes for Automation

- **Hard-deletable** (safe to clean up after each run): Users (`DELETE /api/users/{id}`, unless it still has an active refresh token — logout first), Roles (`DELETE /api/roles/{id}`, unless still assigned to a user — unassign first), Role-Branch assignments (`DELETE /api/v1/security/role-branches/{roleId}/{branchId}`).
- **Soft-deactivate only** (will accumulate as deactivated records): Pages (`PUT /api/pages/{id}/deactivate`).
- **No cleanup path via API at all** (flag to the team, don't try to work around it in Playwright): Permissions, User Profiles. Recommend a naming convention (`PWTEST_` prefix) so these are identifiable for manual/DB cleanup later.
- Recommended Playwright fixture order: create in dependency order (§1), tear down in reverse, wrapped in a `test.afterEach`/`afterAll` hook so failures mid-suite don't leak data silently.
