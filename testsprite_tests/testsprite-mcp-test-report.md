# TestSprite AI Testing Report(MCP)

---

## 1️⃣ Document Metadata
- **Project Name:** backend
- **Date:** 2026-08-25
- **Prepared by:** TestSprite AI Team + Claude Code
- **Trigger:** Full re-run requested after extensive local changes (repo restructured from multi-module Maven to a single module under `src/main/java/com/erp/*`, Java bumped 21→25, packages renamed `com.example.*`→`com.erp.*`). Code summary, PRD, and test plan were regenerated from scratch against the current codebase before running.
- **Target:** `http://localhost:7272` (locally running Spring Boot app, `development` server mode)

---

## 2️⃣ Requirement Validation Summary

### Authentication

#### Test TC001 test_user_login_with_valid_credentials
- **Test Code:** [TC001_test_user_login_with_valid_credentials.py](./TC001_test_user_login_with_valid_credentials.py)
- **Status:** ✅ Passed
- **Analysis / Findings:** Login returns 200 with the JWT `accessToken`/`expiresIn` correctly nested under `data` (global `ApiResponseWrapper` envelope), and the token successfully authorizes a subsequent call to `GET /api/users`. No issues.

#### Test TC012 test_login_with_invalid_credentials_returns_401
- **Test Code:** [TC012_test_login_with_invalid_credentials_returns_401.py](./TC012_test_login_with_invalid_credentials_returns_401.py)
- **Status:** ✅ Passed
- **Analysis / Findings:** Invalid credentials are correctly rejected and protected endpoints correctly reject unauthenticated requests. No issues.

---

### User Management

#### Test TC002 test_create_new_user
- **Test Code:** [TC002_test_create_new_user.py](./TC002_test_create_new_user.py)
- **Test Error:** `AssertionError: Expected 200 OK on user creation, got 201`
- **Status:** ❌ Failed (test-script defect, not a backend bug)
- **Analysis / Findings:** `POST /api/users` correctly returns **201 Created** with the new `UserDto` (verified directly via curl). The generated test hardcoded an expectation of 200. This is a false failure — the backend follows correct REST convention for resource creation. Recommend updating the test plan's known-behavior notes so future generations expect 201 on all `POST .../create` endpoints, matching `POST /api/roles` and `POST /api/masterdata/master-lookups` (see TC003, TC006 below — same root cause).

---

### Roles & Permissions

#### Test TC003 test_create_role_with_valid_data
- **Test Code:** [TC003_test_create_role_with_valid_data.py](./TC003_test_create_role_with_valid_data.py)
- **Test Error:** `AssertionError: Role creation failed: {"data":{"id":1179,"roleCode":"TEST_ROLE_CODE_123",...},"success":true,...}`
- **Status:** ❌ Failed (test-script defect, not a backend bug)
- **Analysis / Findings:** Same 200-vs-201 mismatch as TC002 — the response body embedded in the error message shows the role was created successfully (`success: true`, all fields correct). Because the assertion failed **before** `role_id` was captured, the test's own `finally`-block cleanup never ran, leaving role `TEST_ROLE_CODE_123` orphaned in the database. This directly caused TC010's failure below (cascading test-data collision, not an independent bug).

#### Test TC010 test_assign_branch_scope_to_role
- **Test Code:** [TC010_test_assign_branch_scope_to_role.py](./TC010_test_assign_branch_scope_to_role.py)
- **Test Error:** `AssertionError: Create role failed with status 409`
- **Status:** ❌ Failed (cascading test-data collision from TC003, not a backend bug)
- **Analysis / Findings:** This test independently tries to create a role with the same hardcoded `roleCode` (`TEST_ROLE_CODE_123`) that TC003 left behind uncleaned. The 409 Conflict is the backend correctly enforcing role-code uniqueness — expected behavior given the dirty state. Recommend the test plan use unique/randomized `roleCode` values per test run (same pattern TC002 already uses for usernames via `uuid.uuid4()`).

---

### Org Structure

#### Test TC005 test_create_legal_entity
- **Test Code:** [TC005_test_create_legal_entity.py](./TC005_test_create_legal_entity.py)
- **Test Error:** `403 Client Error` — `{"error":{"code":"FORBIDDEN","details":"You don't have permission to access this resource"}}`
- **Status:** ❌ Failed — **genuine backend/data finding**
- **Analysis / Findings:** Reproduced independently via curl, not a test artifact. The seeded `admin` user's JWT carries `SUPER_ADMIN` plus a long list of `PERM_*` authorities, but **none for the Org module** (no `PERM_ORG_*`/legal-entity permissions) — confirmed by decoding the JWT. Either (a) `SUPER_ADMIN` is expected to universally bypass permission checks but currently doesn't for Org-module endpoints, or (b) the admin role's page/permission seed data was never extended to cover the Org module's pages. Either way, the platform's own admin account cannot manage Legal Entities/Regions/Branches/etc. through the API today. **Recommend engineering follow-up**, since this blocks a whole module for the only bootstrap account.

---

### Pages & Menu

#### Test TC004 test_create_page_and_autogenerate_permissions
- **Test Code:** [TC004_test_create_page_and_autogenerate_permissions.py](./TC004_test_create_page_and_autogenerate_permissions.py)
- **Test Error:** `AssertionError: pageCode mismatch`
- **Status:** ❌ Failed (test-script defect, not a backend bug)
- **Analysis / Findings:** `PageService.createPage()` intentionally normalizes `pageCode` to uppercase (`src/main/java/com/erp/security/service/PageService.java:102`, `pageCode.toUpperCase().trim()`) and validates the format. The test sent a lowercase `pageCode` and asserted the response echoed it verbatim — an incorrect expectation, not a bug in the backend, which is behaving as designed.

#### Test TC009 test_get_user_menu_for_current_user
- **Test Code:** [TC009_test_get_user_menu_for_current_user.py](./TC009_test_get_user_menu_for_current_user.py)
- **Status:** ✅ Passed
- **Analysis / Findings:** `GET /api/menu/user-menu` correctly returns the hierarchical, VIEW-permission-filtered menu for the authenticated user. No issues.

---

### Master Data (Lookups)

#### Test TC006 test_create_master_lookup
- **Test Code:** [TC006_test_create_master_lookup.py](./TC006_test_create_master_lookup.py)
- **Test Error:** `AssertionError: Create master lookup failed: {"data":{"id":21,"lookupKey":"TEST_LOOKUP_...",...},"success":true,...}`
- **Status:** ❌ Failed (test-script defect, not a backend bug)
- **Analysis / Findings:** Same 200-vs-201 root cause as TC002/TC003 — the embedded response shows the lookup was created successfully. No backend issue.

---

### File Management

#### Test TC007 test_issue_file_upload_token_and_upload_file
- **Test Code:** [TC007_test_issue_file_upload_token_and_upload_file.py](./TC007_test_issue_file_upload_token_and_upload_file.py)
- **Test Error:** `AssertionError: Uploaded file metadata missing file name`
- **Status:** ❌ Failed (test-script defect, not a backend bug)
- **Analysis / Findings:** Verified via curl — the upload endpoint returns the file's original name under the field `fileNameOriginal`, exactly matching the documented `FileUploadResponse` schema. The test only checked for `fileName` or `originalFileName`, missing the actual field name. Upload token issuance, multipart upload, and file metadata persistence all work correctly end-to-end.

---

### Notifications

#### Test TC008 test_send_notification_immediately
- **Test Code:** [TC008_test_send_notification_immediately.py](./TC008_test_send_notification_immediately.py)
- **Test Error:** `500 Server Error` — `{"error":{"code":"INTERNAL_ERROR","details":"An unexpected error occurred..."}}`
- **Status:** ❌ Failed — **genuine backend bug**
- **Analysis / Findings:** Reproduced independently via curl with multiple payload variations (including a valid existing `recipientId`), consistently returning HTTP 500. Server log (`logs/erp-security.log`) shows `org.springframework.transaction.UnexpectedRollbackException: Transaction silently rolled back because it has been marked as rollback-only` at `NotificationController.send` → `NotificationEventProcessor`, with **no underlying cause exception logged** — meaning the transaction was silently marked rollback-only by an inner call (likely the nested `@Transactional` cross-module call to `SecUserProfileService`/`resolvePreferredLanguage`) whose exception was swallowed before it reached the outer transaction boundary. Since `NOTIF_TEMPLATE_001` doesn't exist as an active template and both `email`/`sms` channels resolved as disabled/unconfigured (case-sensitivity: config likely stores `EMAIL`/`SMS` uppercase, request sent lowercase), the code path exercised the "channel disabled" branch, but the request never completes — every `POST /api/v1/notifications/send` call currently fails with a 500 regardless of payload validity. **Recommend engineering follow-up**: this endpoint is not currently usable at all.

#### Test TC011 test_notification_unread_endpoint_returns_known_422
- **Test Code:** [TC011_test_notification_unread_endpoint_returns_known_422.py](./TC011_test_notification_unread_endpoint_returns_known_422.py)
- **Status:** ✅ Passed
- **Analysis / Findings:** `GET /api/v1/notifications/unread` correctly returns HTTP 422 `NOTIF_READ_TRACKING_UNAVAILABLE`, matching the documented, intentional limitation (`NOTIF_LOG` has no read/unread column — see `governance/modules/NOTIFICATION/execution-state.json`, DRV-NOTIF-003). Behaves as designed.

---

## 3️⃣ Coverage & Matching Metrics

- **33.33%** of tests passed as originally asserted (4/12) — but **8/12 (66.7%)** of endpoints exercised are actually working correctly once test-script defects are accounted for. Only **2 endpoints have genuine backend defects.**

| Requirement            | Total Tests | ✅ Passed | ❌ Failed (test-script bug) | ❌ Failed (real backend issue) |
|-------------------------|:-----------:|:---------:|:----------------------------:|:-------------------------------:|
| Authentication          | 2           | 2         | 0                             | 0                                |
| User Management         | 1           | 0         | 1 (TC002)                     | 0                                |
| Roles & Permissions     | 2           | 0         | 2 (TC003, TC010)              | 0                                |
| Org Structure           | 1           | 0         | 0                              | 1 (TC005 — 403, missing perms)  |
| Pages & Menu            | 2           | 1         | 1 (TC004)                     | 0                                |
| Master Data (Lookups)   | 1           | 0         | 1 (TC006)                     | 0                                |
| File Management         | 1           | 0         | 1 (TC007)                     | 0                                |
| Notifications           | 2           | 1         | 0                              | 1 (TC008 — 500 error)           |
| **Total**               | **12**      | **4**     | **6**                          | **2**                            |

---

## 4️⃣ Key Gaps / Risks

**Real backend defects requiring engineering follow-up:**
1. **`POST /api/v1/org/legal-entities` (and likely the rest of the Org module: Regions, Branches, Departments, Cost Centers, Location Sites, Profit Centers) returns 403 for the seeded `admin` account.** The admin JWT has `SUPER_ADMIN` but no `PERM_ORG_*` authorities — either `SUPER_ADMIN` isn't wired as a universal bypass, or Org-module pages were never assigned to the admin role. This blocks the entire Org module via API for the only bootstrap account.
2. **`POST /api/v1/notifications/send` always fails with HTTP 500** (`UnexpectedRollbackException`, root cause exception swallowed before reaching the outer `@Transactional` boundary). The send/notification feature is currently non-functional end-to-end. Needs investigation into the nested transactional call chain (`NotificationEventProcessor.process()` → `resolvePreferredLanguage()` → cross-module `SecUserProfileApi` call) and why an inner exception isn't propagating or is being caught after the transaction was already marked rollback-only.

**Test-generation notes (fixed at the harness level, not the backend):**
- The generated test plan didn't know that all `POST .../create` endpoints in this API return **201 Created**, not 200 — this caused 3 false failures (TC002, TC003, TC006). The underlying code summary has been corrected to note this global `ApiResponseWrapper` envelope and 201 convention for future runs.
- `pageCode` and similar natural-key codes are normalized to uppercase server-side — test assertions must account for this (TC004).
- `FileUploadResponse` uses `fileNameOriginal`, not `fileName`/`originalFileName` (TC007).
- Tests using hardcoded (non-unique) fixture values like `roleCode` can collide across runs/tests when an earlier test fails before its cleanup step executes (TC003 → TC010 cascade); recommend randomized fixture data everywhere, matching the pattern already used for usernames.

**Note on this session:** an earlier full pass (10/10 failing) was caused entirely by an inaccurate code summary that claimed `AuthController` responses were *not* wrapped in the standard `ApiResponse` envelope — in reality a global `ApiResponseWrapper` (`@RestControllerAdvice`) wraps every JSON response including Auth. That summary was corrected mid-session and the suite re-run, which is why this report reflects the corrected, more informative second pass rather than the initial false 0%-pass run.
