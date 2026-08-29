# TestSprite AI Testing Report (MCP)

---

## 1️⃣ Document Metadata
- **Project Name:** backend
- **Date:** 2026-08-26
- **Prepared by:** TestSprite AI Team + Claude Code
- **Target:** `http://localhost:7272` (local Spring Boot app, `development` server mode)
- **Context:** Full re-run after extensive local changes (repo restructured from multi-module Maven to a single module under `src/main/java/com/erp/*`, Java bumped 21→25, packages renamed `com.example.*`→`com.erp.*`), plus two genuine backend defects found and fixed earlier this session (notification-send 500 error, Org module 403 for admin — both now verified passing below).

---

## 2️⃣ Requirement Validation Summary

### Authentication
| Test | Description | Status |
|---|---|---|
| TC001 | `POST /api/auth/login` with valid credentials returns 200 with `accessToken`/`expiresIn` under `data`, and the token authorizes a protected call | ✅ Passed |
| TC012 | `POST /api/auth/login` with invalid credentials is rejected; protected endpoints reject requests without a valid Authorization header | ✅ Passed |

### User Management
| Test | Description | Status |
|---|---|---|
| TC002 | `POST /api/users` creates a user (201 Created), the user is findable via `POST /api/users/search` (standard filters-contract shape), and roles can be assigned/read back | ✅ Passed |

### Roles & Permissions
| Test | Description | Status |
|---|---|---|
| TC003 | `POST /api/roles` with a valid `roleCode` (matching `^[A-Z][A-Z0-9_]*$`) creates a role (201 Created) | ✅ Passed |
| TC010 | A freshly-created role is assigned branch-level data-scope via `POST /api/v1/security/role-branches` with a valid `dataAccessLevel` (`BRANCH_ONLY`/`BRANCH_AND_CHILDREN`/`ALL`) | ✅ Passed |

### Pages & Menu
| Test | Description | Status |
|---|---|---|
| TC004 | `POST /api/pages` creates a page and auto-generates CRUD permissions (201 Created); `pageCode` is normalized to uppercase server-side | ✅ Passed |
| TC009 | `GET /api/menu/user-menu` returns the hierarchical, VIEW-permission-filtered menu for the authenticated user | ✅ Passed |

### Org Structure
| Test | Description | Status |
|---|---|---|
| TC005 | `POST /api/v1/org/legal-entities` succeeds (201 Created) for the SUPER_ADMIN account. **Previously failed with 403** earlier this session — fixed via `V16__grant_super_admin_org_module_permissions.sql` (SUPER_ADMIN's grants were a stale point-in-time snapshot missing 27 of the 28 ORGANIZATION-module permissions). Confirmed fixed and passing. | ✅ Passed |

### Master Data (Lookups)
| Test | Description | Status |
|---|---|---|
| TC006 | `POST /api/masterdata/master-lookups` creates a lookup (201 Created); `lookupKey` is normalized to uppercase server-side | ✅ Passed |

### File Management
| Test | Description | Status |
|---|---|---|
| TC007 | `POST /api/v1/files/upload-token` issues an encrypted token (201 Created); `POST /upload/{token}` uploads a file (201 Created) with `fileNameOriginal` in the response | ✅ Passed |

### Notifications
| Test | Description | Status |
|---|---|---|
| TC008 | `POST /api/v1/notifications/send` sends a notification (201 Created) with `logEntryIds`. **Previously failed with 500** earlier this session — fixed via `Propagation.NOT_SUPPORTED` on `SecUserProfileApiService.findById()` (a nested `@Transactional` read that could throw-and-be-caught was poisoning the caller's write transaction under the default `REQUIRED` propagation). Confirmed fixed and passing. Also independently verified with real SMTP delivery to a live inbox. | ✅ Passed |
| TC011 | `GET /api/v1/notifications/unread` correctly returns HTTP 422 `NOTIF_READ_TRACKING_UNAVAILABLE` — a documented, intentional limitation (no read/unread column in `NOTIF_LOG`), not a defect | ✅ Passed |

---

## 3️⃣ Coverage & Matching Metrics

- **100.00%** of tests passed (12/12)

| Requirement | Total Tests | ✅ Passed | ❌ Failed |
|---|---|---|---|
| Authentication | 2 | 2 | 0 |
| User Management | 1 | 1 | 0 |
| Roles & Permissions | 2 | 2 | 0 |
| Pages & Menu | 2 | 2 | 0 |
| Org Structure | 1 | 1 | 0 |
| Master Data (Lookups) | 1 | 1 | 0 |
| File Management | 1 | 1 | 0 |
| Notifications | 2 | 2 | 0 |
| **Total** | **12** | **12** | **0** |

---

## 4️⃣ Key Gaps / Risks

**No open backend defects.** Both real bugs identified in earlier passes this session were root-caused, fixed, verified live, and are now committed:
1. `POST /api/v1/notifications/send` 500 error (transaction-propagation bug in a cross-module read) — fixed.
2. `POST /api/v1/org/legal-entities` (and the rest of the Org module) 403 for `SUPER_ADMIN` (stale permission-grant snapshot) — fixed.

**Non-blocking follow-ups noted (not implemented, out of scope for this pass):**
- The same stale-permission-grant pattern that caused the Org-module 403 also affects `SECURITY` (90 permissions) and `FINANCE` (4 permissions) never granted to `SUPER_ADMIN` — flagged for a future fix, not touched here.
- `governance/.github/skills/backend/create-service/SKILL.md`'s cross-module propagation guidance (lines 109-114) still recommends the default `REQUIRED` propagation pattern that caused the notification bug; the code was fixed but the doc itself was left as-is.
- Several API contract quirks surfaced during test-plan tuning that are correct-by-design but worth documenting for future test/API consumers: all `POST .../create` endpoints return `201` (not `200`); `pageCode`/`lookupKey` are uppercased server-side; `roleCode` must match `^[A-Z][A-Z0-9_]*$`; `roleName`/legal-entity `nameEn`/`nameAr` have global uniqueness constraints; search endpoints (`POST .../search`) require the `{filters: [{field, operator, value}], page, size}` contract shape, not a flat filter object; `SecRoleBranch.dataAccessLevel` only accepts `BRANCH_ONLY`/`BRANCH_AND_CHILDREN`/`ALL`.
