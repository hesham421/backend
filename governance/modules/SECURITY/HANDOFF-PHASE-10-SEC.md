# HANDOFF — PHASE 10 (SEC) — PLAN-SEC-002

SEC_PAGES row + generated permissions confirmed: **partial**

- `SEC_PAGES` row for `USER_PROFILE` (route `/security/user-profiles`, module `SECURITY`,
  display_order 5) — **inserted and confirmed live** via `mcp__postgres__query`, with your
  explicit approval.
- The 3 permission rows (`PERM_USER_PROFILE_VIEW` / `_CREATE` / `_UPDATE`) and the grant of
  those 3 to `SUPER_ADMIN` — **NOT yet applied**. The permission classifier blocked me from
  running these as a live RBAC write without you naming it explicitly; you chose to run
  `backend/erp-security/src/main/resources/db/scripts/003_sec_pages_permissions_seed.sql`
  yourself (BLOCK 2 + BLOCK 3 — BLOCK 1 is already applied, safe to re-run in full, it's
  idempotent). **Until that runs, `PERM_USER_PROFILE_*` do not exist anywhere in the DB, and
  nobody — not even SUPER_ADMIN — can pass `SecUserProfileService`'s new `@PreAuthorize`
  checks or the frontend's existing route guards for `/security/user-profiles`.** This is the
  single blocking follow-up before this phase's work is actually live.

@PreAuthorize wiring confirmed per endpoint: [list API-ID → permission]

- `SecUserProfileService` (all 5 public methods — API-SEC-032..035):
  `create` → `USER_PROFILE_CREATE`, `update` → `USER_PROFILE_UPDATE`,
  `getById`/`listProfiles`/`search` → `USER_PROFILE_VIEW`.
- `SecRoleBranchService` (all 6 public methods — API-SEC-036..039, reuses existing `PERM_ROLE_*`
  per CORE-9): `create` → `ROLE_CREATE`, `update` → `ROLE_UPDATE`, `delete` → `ROLE_DELETE`,
  `getById`/`listRoleBranches`/`search` → `ROLE_VIEW`.
- Placement: **service layer**, not controller — see Deviation #1 below.
- `SecurityConfig` already required authentication for both controllers' base paths
  (`.anyRequest().authenticated()`, `@EnableMethodSecurity` already present) — no
  `SecurityConfig` change needed, `@PreAuthorize` layers on top of that.

UI permission-hiding confirmed: **yes, already complete from Phase F1/F2** — verified by reading
source, not new work this phase:
- `security-routing.module.ts`: `/security/user-profiles` (+`create`/`edit/:id`) route guards
  already gated on `PERM_USER_PROFILE_VIEW`/`_CREATE`/`_UPDATE` via `[authGuard, permissionGuard]`.
- `user-profile-search.component.html` / `user-profile-actions-cell.component.ts`: Create/Update
  buttons already wrapped in `erpPermission="PERM_USER_PROFILE_CREATE"` / `_UPDATE`.
- `role-access-form.component.html`'s Branch Scope sub-tab: Assign/Update/Remove already wrapped
  in `erpPermission="ROLE.CREATE"` / `ROLE.UPDATE"` / `"ROLE.DELETE"` (dot-notation, normalized
  to `PERM_ROLE_*` by `PermissionService.normalize()` — confirmed, not a bug).
- These were built ahead of the backend actually enforcing the permissions (Contract Alignment
  Gate override, same as F1/F2/F3) — this phase is what makes them load-bearing for the first
  time.

allowedBranches[] claim verified in an actual token: **yes**
```json
{
  "sub": "admin",
  "userId": 4,
  "authorities": [ "...23 PERM_* + SUPER_ADMIN..." ],
  "allowedBranches": [],
  "iat": 1783696447,
  "exp": 1783782847
}
```
Logged in as `admin`/`admin` against the live restarted backend, decoded the real `accessToken`.
`allowedBranches` is `[]` — confirmed **correct**, not a bug: queried `sec_role_branch` live,
**0 rows exist in this dev DB** (no DataScope branch assignment has ever been created for any
role, including SUPER_ADMIN). The claim mechanism itself (repository query → active-role
filtering → claim injection) is proven to run without error on every login/refresh/login-token
call; its *emptiness* is a genuine reflection of empty source data, not an implementation gap.
The "ALL" sentinel branch was verified by code review only (no `dataAccessLevel = ALL` row
exists yet to exercise it live) — see "ALL sentinel used?" below.

"ALL" sentinel used?: **yes** — `AuthService.resolveAllowedBranches()`: if any active
`SEC_ROLE_BRANCH` row for the user's active roles has `dataAccessLevel = 'ALL'`, the claim is
the single element `["ALL"]` instead of enumerating every branch ID. DRV-SEC-004 confirms this
is an allowed (not mandated) optimization; documented in code (`AuthService.java` Javadoc on
`resolveAllowedBranches`) and in `JwtService.generateAccess`'s Javadoc.

Rate-limit filter extension verified (existing tests still pass): **yes**
- `LoginRateLimitFilterTest` (3 tests) + `LoginRateLimiterServiceTest` (2 tests) — all 5 green,
  unmodified, after the extension.
- Live-tested against the running server (not just unit tests): fired 6 rapid identical requests
  each at `/api/auth/forgot-password` (same email), `/api/auth/reset-password` (same token), and
  `/api/auth/login` (same username, for regression) — all three correctly allow 5 and 429 on the
  6th, matching `erp.security.rate-limit.login.max-attempts=5`. Confirms the per-path identifier
  extraction (`username` / `email` / `token`) and key construction both work end-to-end, not just
  the untouched bucket logic.

Ready for Phase 11 (ALIGN)? **no** — blocked on the pending SQL (see above). ALIGN's purpose is
a clean audit; starting it while `PERM_USER_PROFILE_*` don't exist yet would just produce a
known-stale report. Recommend running the rest of `003_sec_pages_permissions_seed.sql`, then a
quick re-verification (log in, confirm `PERM_USER_PROFILE_VIEW` etc. appear in the token's
`authorities[]`, hit `/security/user-profiles` in the frontend), before ALIGN.

---

## Deviations / judgment calls (not silent)

1. **`@PreAuthorize` initially placed on the controllers, corrected to the service layer
   mid-session at your explicit instruction.** I'd started by mirroring `PermissionController`/
   `PageController`'s controller-layer `@PreAuthorize("hasAuthority('...')")` pattern (which the
   research pass found as one of two co-existing conventions in this codebase). You flagged that
   your own convention puts it on the service, not the controller — checking
   `governance-repo/.github/skills/backend/enforce-backend-contract/SKILL.md` confirmed this is
   the actual governed contract: LAYER 5 (Service) checklist item **A.5.2 — "@PreAuthorize on
   EVERY public method"**, and LAYER 6 (Controller) checklist explicitly limits controllers to
   **A.6.3 "Injects ONLY service(s) + OperationCode"** / **A.6.12 "ZERO business logic"** — no
   `@PreAuthorize` mentioned there at all. Reverted both controllers to their original thin form
   and added `@PreAuthorize` to all 11 public methods across `SecUserProfileService` +
   `SecRoleBranchService` instead, using the `T(com.example.security.constants.SecurityPermissions
   ).XXX` SpEL form (matching `PageService`/`RoleAccessService`'s existing service-layer usage,
   the correct precedent). **How to apply:** the `PermissionController`/`PageController`
   controller-layer `@PreAuthorize` usages the research pass found are themselves inconsistent
   with A.6 — not something to replicate in future phases.

2. **`PageService.createPage()` cannot produce exactly 3 permissions (VIEW/CREATE/UPDATE, no
   DELETE) — it always generates all 4 CRUD permissions** (`createPermissionRecords()` loops
   unconditionally over `PermissionType.values()`, no flag to suppress one). Rather than change
   `PageService` (out of this phase's scope, and would affect every other page-creation caller),
   inserted the `SEC_PAGES` + `PERMISSIONS` rows directly via a new idempotent SQL script
   (`003_sec_pages_permissions_seed.sql`), matching the established convention (`001`/`002` in
   the same directory — no Flyway is wired up for `erp-security`, plain numbered scripts run
   manually by a DBA). This is the same convention Phase 2 (DATA-DOM) used for its own DDL.

3. **`PARENT_ID_FK` left `NULL` for the new `SEC_PAGES` row**, not set to a "الأمان (Security)
   group page" as the plan's Section 8.2 literally says. Queried the live `sec_pages` table
   first: **every existing row across all modules has `PARENT_ID_FK = NULL`** — the "الأمان"
   label is a frontend i18n string keyed off `MODULE = 'SECURITY'`
   (`frontend/src/assets/i18n/ar.json`), not an actual `SEC_PAGES` row. There is no real parent
   page to reference; inventing one would be a new, unprecedented pattern in this table.
   Followed existing data shape (NULL parent + MODULE grouping) instead.

4. **Rate-limit identifier for `/api/auth/reset-password` is the reset `token`, not `email`** —
   a deliberate deviation from Section 8.3's literal wording. Checked
   `ResetPasswordRequest.java`: its body is `(token, newPassword)` only, **no email field
   exists at all** — the plan's instruction is literally impossible to implement as written for
   this endpoint. Used the token itself as the identifier instead: it's the only
   user-identifying value in that request body, and rate-limiting by it still achieves the
   underlying goal (throttling scripted reset-token guessing from a single IP), which is a
   closer match to the endpoint's actual threat model than the literal instruction would have
   been even if it were implementable.

5. **`LoginRateLimitFilter`'s hardcoded `Set<String> PROTECTED_PATHS` + single `extractUsername()`
   method were generalized to a `Map<String, String> PROTECTED_PATH_IDENTIFIER_FIELD` +
   `extractField(request, fieldName)`**, since the 4 protected paths now need 3 different body
   field names (`username` for login/signup, `email` for forgot-password, `token` for
   reset-password) rather than one. Behavior for the 2 pre-existing paths is unchanged (same
   field name, same key construction) — confirmed by the 3 existing `LoginRateLimitFilterTest`
   cases passing unmodified.

6. **`AuthService.login()` and `AuthService.refresh()` switched their `userAccountRepo` lookup
   from `findByUsernameIgnoreCase()` to `findByUsernameWithRoles()`** (the eager-fetch query
   already used by `loginWithUserInfo()`) — needed to access `userEntity.getRoles()` for
   `resolveAllowedBranches()`. Same repository, same existing method, no new query — just using
   it in two more call sites that now need role data they didn't need before.

7. **STEP 0 CONTRACT ALIGNMENT GATE / current_phase mismatch — same out-of-order pattern as
   F1/F2/F3, but SEC is not one of the phases STEP 0.4 literally names.** I flagged the
   `current_phase: "DOC"` mismatch anyway (DOC/INT-C/INT-R still PENDING) given SEC's blast
   radius (JWT issuance, real `@PreAuthorize` enforcement on every DataScope endpoint), you
   confirmed "Force-start SEC anyway". `execution-state.json`'s `current_phase` remains `"DOC"`,
   same reasoning as before.

8. **Verification:** `mvn -pl erp-security -am clean compile` — BUILD SUCCESS, 114 source files,
   only pre-existing unrelated deprecation warnings. `mvn -pl erp-security test
   -Dtest=LoginRateLimitFilterTest,LoginRateLimiterServiceTest` — 5/5 green. Backend restarted
   live (captured running PID's exact classpath, killed, relaunched — same recipe as prior
   sessions) and re-verified via real HTTP: admin login + JWT decode, and 6-rapid-request rate
   limit checks against all 3 relevant paths (forgot-password, reset-password, login regression).
   **Could not verify permission-gated access itself** (a real 403 → grant → 200 round-trip for
   `PERM_USER_PROFILE_*`) — blocked on the pending SQL grant, see top of this handoff. No
   frontend code changed this phase, so no `ng build`/`tsc` run was needed.
