# TestSprite AI Testing Report (MCP)

---

## 1️⃣ Document Metadata
- **Project Name:** backend
- **Date:** 2026-09-06
- **Prepared by:** TestSprite AI Team
- **Scope:** Full backend codebase (all modules), 10 auto-generated backend test cases
- **Target:** `http://localhost:7272` (Spring Boot, stateless JWT auth)
- **Auth model note:** Backend is JWT-only (no HTTP Basic). Tests authenticate via `POST /api/v1/security/auth/login` and send `Authorization: Bearer <accessToken>`.

> **UPDATE 2026-09-06 — all findings fixed; re-run is 10/10 PASS.** Original run was 4/10.
> See "§5 Resolution" at the bottom for the fixes applied and the final result.

---

## 2️⃣ Requirement Validation Summary

### Requirement: Authentication & Self-Service (SEC)
Login and self-scoped identity/navigation for the authenticated caller.

- **TC001 — POST /api/v1/security/auth/login with valid credentials**
  - **Test Code:** [TC001_post_api_v1_security_auth_login_with_valid_credentials.py](./TC001_post_api_v1_security_auth_login_with_valid_credentials.py)
  - **Status:** ✅ Passed
  - **Analysis / Findings:** Login returns a valid JWT (`data.accessToken`) for `admin/admin`. Core auth path is healthy; all other authenticated tests depend on this and it works.

- **TC002 — GET /api/v1/security/me/modules with valid bearer token**
  - **Test Code:** [TC002_get_api_v1_security_me_modules_with_valid_bearer_token.py](./TC002_get_api_v1_security_me_modules_with_valid_bearer_token.py)
  - **Status:** ✅ Passed
  - **Analysis / Findings:** Self dashboard-modules resolves correctly from the JWT principal and returns the wrapped `data` envelope.

### Requirement: Security Lookups (SEC)
Runtime LOV resolution by key.

- **TC003 — GET /api/v1/security/lookups/{lookupKey} with valid bearer token**
  - **Test Code:** [TC003_get_api_v1_security_lookups_lookupkey_with_valid_bearer_token.py](./TC003_get_api_v1_security_lookups_lookupkey_with_valid_bearer_token.py)
  - **Status:** ❌ Failed
  - **Error:** `LOOKUP_KEY_NOT_FOUND` — "Resource not found."
  - **Analysis / Findings:** **Test-data defect, not a backend bug.** The test requested `lookupKey = "USER_STATUS"`, but the only registered SEC keys are `SEC_USER_STATUS` and `SEC_PREFERRED_LANG` (see `LookupService.java:40-55`). The endpoint correctly returns a `404 / LOOKUP_KEY_NOT_FOUND` for an unknown key — this is correct behavior. The test should use `SEC_USER_STATUS`.

### Requirement: Security Registry CRUD (SEC — Modules, Pages, Roles)
Create Tier-1 modules, screen pages, and RBAC roles.

- **TC004 — POST /api/v1/security/modules with valid data**
  - **Test Code:** [TC004_post_api_v1_security_modules_with_valid_data.py](./TC004_post_api_v1_security_modules_with_valid_data.py)
  - **Status:** ❌ Failed
  - **Error:** "Module creation failed with status 201"
  - **Analysis / Findings:** **Test-assertion defect, not a backend bug.** The module was created successfully — the backend returns **HTTP 201 Created** with a valid `data` body (`success:true`). The test asserted `200`. Verified directly: `POST /api/v1/security/modules` → `HTTP 201`. Backend is correct.

- **TC005 — POST /api/v1/security/pages with valid data**
  - **Test Code:** [TC005_post_api_v1_security_pages_with_valid_data.py](./TC005_post_api_v1_security_pages_with_valid_data.py)
  - **Status:** ❌ Failed
  - **Error:** "Module creation failed: {data...success:true}" (fails in the setup step that creates a parent module)
  - **Analysis / Findings:** **Test-assertion defect, not a backend bug.** Same 201-vs-200 mismatch as TC004: the test's setup creates a module, receives a successful `201` envelope, and then wrongly asserts `200`, aborting before the page assertion is reached. The page-create endpoint itself was never exercised to failure. Fix the setup assertion to accept `201`.

- **TC007 — POST /api/v1/security/roles with valid data**
  - **Test Code:** [TC007_post_api_v1_security_roles_with_valid_data.py](./TC007_post_api_v1_security_roles_with_valid_data.py)
  - **Status:** ❌ Failed
  - **Error:** "Failed to create role: {data...success:true}"
  - **Analysis / Findings:** **Test-assertion defect, not a backend bug.** Role was created (`success:true`, `id:5`). Verified directly: `POST /api/v1/security/roles` → `HTTP 201`. Test asserted `200`. Backend is correct.

### Requirement: Permission Registry (SEC)
Read-only Tier-2 permission listing.

- **TC006 — GET /api/v1/security/permissions with filters and bearer token**
  - **Test Code:** [TC006_get_api_v1_security_permissions_with_filters_and_bearer_token.py](./TC006_get_api_v1_security_permissions_with_filters_and_bearer_token.py)
  - **Status:** ✅ Passed
  - **Analysis / Findings:** Query-param filtered, paginated permission listing works and returns the wrapped page envelope.

### Requirement: User Management (SEC)
Create security user accounts.

- **TC008 — POST /api/v1/security/users with valid data**
  - **Test Code:** [TC008_post_api_v1_security_users_with_valid_data.py](./TC008_post_api_v1_security_users_with_valid_data.py)
  - **Status:** ❌ Failed
  - **Error:** `HTTP 500 / INTERNAL_ERROR` — "An unexpected error occurred"
  - **Analysis / Findings:** **REAL failure — runtime build drift (highest priority).** Server log shows:
    `java.lang.NoSuchMethodError: 'UserResponse$UserResponseBuilder UserResponse$UserResponseBuilder.failedLoginCount(java.lang.Short)'`.
    The **current source** of `UserResponse.java` no longer declares a `failedLoginCount` field, and `UserMapper.java` no longer references it — both are consistent, and `target/classes` no longer contains the method either. The **running JVM** is still executing an older, inconsistent build loaded at startup (both `UserResponse.java` and `UserMapper.java` are uncommitted/modified in the working tree). **Fix: rebuild and restart the app** (`mvn clean compile` + restart `spring-boot:run`). No current-source code change is required — this is stale-runtime, not a logic bug. Re-run TC008 after restart to confirm.

### Requirement: Common Configurations (CU)
Create key-value system configuration entries.

- **TC009 — POST /api/v1/common/configurations with valid data**
  - **Test Code:** [TC009_post_api_v1_common_configurations_with_valid_data.py](./TC009_post_api_v1_common_configurations_with_valid_data.py)
  - **Status:** ❌ Failed
  - **Error:** `HTTP 403 / ACCESS_DENIED` — "You do not have permission to perform this operation"
  - **Analysis / Findings:** **Authorization gap — needs a human decision.** The bootstrap `admin` account can create SEC modules/roles (201) but is denied `POST /api/v1/common/configurations`. Either (a) the `admin` seed is missing the CU "create configuration" permission grant, or (b) CU configuration writes are intentionally restricted to a role `admin` doesn't hold. Not a code crash — confirm the intended authorization policy for CU and, if a seeding gap, grant the permission to the bootstrap role.

### Requirement: File Management (FILE)
Upload a file via multipart.

- **TC010 — POST /api/v1/files with valid multipart data**
  - **Test Code:** [TC010_post_api_v1_files_with_valid_multipart_data.py](./TC010_post_api_v1_files_with_valid_multipart_data.py)
  - **Status:** ✅ Passed
  - **Analysis / Findings:** Multipart upload (`file` part + `ownerId`/`ownerType`/`moduleCode` form fields) succeeds and returns file metadata.

---

## 3️⃣ Coverage & Matching Metrics

- **40.00%** of tests passed (4 / 10).
- Of the 6 failures: **1 real backend/runtime issue** (TC008, stale build), **1 authorization gap needing a decision** (TC009), **3 test-assertion defects** (TC004/005/007 — creates return 201, tests asserted 200), **1 test-data defect** (TC003 — wrong lookup key). **Only 1 failure reflects a genuine defect in running code.**

| Requirement | Module | Total Tests | ✅ Passed | ❌ Failed | Failure nature |
|-------------|--------|-------------|-----------|-----------|----------------|
| Authentication & Self-Service | SEC | 2 | 2 | 0 | — |
| Security Lookups | SEC | 1 | 0 | 1 | Test-data (wrong key) |
| Security Registry CRUD (Modules/Pages/Roles) | SEC | 3 | 0 | 3 | Test-assertion (201 vs 200) |
| Permission Registry | SEC | 1 | 1 | 0 | — |
| User Management | SEC | 1 | 0 | 1 | **Real — stale build (500)** |
| Common Configurations | CU | 1 | 0 | 1 | Authorization gap (403) |
| File Management | FILE | 1 | 1 | 0 | — |
| **Total** | | **10** | **4** | **6** | |

**Coverage note:** This run covered SEC (8), CU (1), FILE (1). **MDM and NOTIF have no test cases** this run — the Starter plan capped the plan at 10 `TCnnn`, and generation weighted them to SEC/CU/FILE. A follow-up run (or a larger plan) is needed to cover MDM lookups and the NOTIF dispatch/channel/template/log endpoints.

---

## 4️⃣ Key Gaps / Risks

1. **[HIGH — real] Stale running build causes 500 on user creation (TC008).** The live app runs bytecode inconsistent with the current source (`NoSuchMethodError` for a removed `UserResponse.failedLoginCount(Short)` builder method). Any endpoint touching the old `UserMapper`/`UserResponse` pairing is at risk until the app is rebuilt and restarted. **Action: `mvn clean compile` + restart, then re-run TC008.**

2. **[MEDIUM — decision] CU configuration create denied to the bootstrap admin (TC009, 403).** Clarify whether CU writes are intentionally restricted; if not, the admin/bootstrap role is missing the CU create-configuration permission grant.

3. **[LOW — test quality] Create endpoints return 201, several generated tests assert 200 (TC004/005/007).** The backend is correct and consistent (201 Created for creates, 204 for deletes). The generated tests (and the code-summary hint fed to the generator) should treat create success as 201. These are archived tests to correct on the next regeneration, not code bugs.

4. **[LOW — test quality] Lookup test used an unregistered key (TC003).** Use `SEC_USER_STATUS` / `SEC_PREFERRED_LANG`. The endpoint's 404-on-unknown-key behavior is correct.

5. **[COVERAGE] MDM and NOTIF untested this run.** Plan capped at 10 cases; neither module received a case. Schedule a targeted run for `/api/v1/mdm/*` and `/api/v1/notifications/*`. Note the SMTP mail health indicator is DOWN locally (no `MAIL_PASSWORD`), so live NOTIF email-send scenarios may fail regardless.

---

---

## 5️⃣ Resolution (2026-09-06 — "fix all")

Every finding was fixed and verified by re-executing all 10 tests against the live app: **10/10 PASS.**

### Backend fixes
1. **TC008 — user-create HTTP 500 (real, highest priority).** Root cause: the running JVM held **stale compiled classes** (a `NoSuchMethodError` for a removed `UserResponse.failedLoginCount(Short)` builder). Fix: `mvn clean compile` + app restart. The current source was already self-consistent, so **no source change** was needed. Verified: `POST /api/v1/security/users` → `201`.
2. **TC009 — CU config-create HTTP 403 (real, latent design gap).** Root cause: `ConfigurationService` enforces `hasAuthority(CONFIG_*)`, but those permissions were never seeded or granted to any role, making the endpoints unreachable by everyone. Fix: added **`V13__cu_security_seed.sql`** — seeds the `CU` module, a backend-only `CU_CONFIGURATIONS` page, the four `CONFIG_VIEW/CREATE/UPDATE/DEACTIVATE` permissions (codes == `PermissionConstants`), a `CU_ADMIN` role, and Tier-1/Tier-2 grants to both `CU_ADMIN` and `SYS_ADMIN`. This is the SEC-side mechanism that CU's `SEC-BE.md` explicitly deferred to SEC; the one backend-only holder page (for a governance-screenless module) is a documented, human-approved deviation recorded in the migration header. Verified: `POST /api/v1/common/configurations` → `201`.

### Test-script/test-data fixes (backend was already correct)
3. **TC003** — used unregistered lookup key `USER_STATUS`; changed to `SEC_USER_STATUS`.
4. **TC004 / TC005 / TC007** — natural-key codes (`moduleCode`/`pageCode`/`roleCode`) are normalized to **UPPERCASE** server-side (RULE-SEC-010, `*.trim().toUpperCase()`); tests now generate uppercase codes so the equality assertion matches.
5. **TC004 / TC005 / TC007 / TC008 / TC009** — create endpoints return **HTTP 201** (not 200); assertions corrected accordingly.

### Final result

| TC | Module | Before | After |
|----|--------|--------|-------|
| TC001 login | SEC | ✅ | ✅ |
| TC002 me/modules | SEC | ✅ | ✅ |
| TC003 lookups | SEC | ❌ | ✅ |
| TC004 create module | SEC | ❌ | ✅ |
| TC005 create page | SEC | ❌ | ✅ |
| TC006 permissions | SEC | ✅ | ✅ |
| TC007 create role | SEC | ❌ | ✅ |
| TC008 create user | SEC | ❌ (500) | ✅ |
| TC009 create config | CU | ❌ (403) | ✅ |
| TC010 upload file | FILE | ✅ | ✅ |
| **Total** | | **4/10** | **10/10** |

Re-run method: the archived, corrected `.py` files were executed directly against `http://localhost:7272` (self-contained `requests` scripts). MDM and NOTIF remain uncovered this run (plan capped at 10 cases).
