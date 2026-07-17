# TEST REPORT — SECURITY MODULE

**Date:** 2026-07-11 | **Total:** 168 | **Passed:** 149 | **Failed:** 4 | **Skipped:** 15

**Source:** `governance-repo/modules/SECURITY/security-module-test-cases.md` (all 10 groups + Cross-Module E2E + Non-Functional/Cross-Cutting). Suite lives at `frontend/e2e/security/` (Playwright, UI-driven POM where a real screen exists, API-level via `SecurityApiClient` where it doesn't — see `frontend/e2e/security/specs/*.spec.ts`).

## Summary

| Status | Count | % |
|---|---|---|
| ✅ SUCCESS | 149 | 89% |
| ❌ FAIL | 4 | 2% |
| ⏭ SKIPPED | 15 | 9% |

| Spec file | Group(s) | Pass | Fail | Skip |
|---|---|---|---|---|
| `auth.spec.ts` | 2 — Authentication | 11 | 0 | 0 |
| `self-service-auth.spec.ts` | 3 — Self-Service Auth | 18 | 0 | 6 |
| `pages.spec.ts` | 4 — Page Management | 11 | 1 | 0 |
| `permissions.spec.ts` | 5 — Permission Management | 8 | 1 | 0 |
| `roles.spec.ts` | 6 — Role Access Control | 21 | 0 | 0 |
| `users.spec.ts` | 7 — User Management | 15 | 0 | 0 |
| `menu.spec.ts` | 8 — Menu Management | 5 | 0 | 0 |
| `user-profiles.spec.ts` | 9 — DataScope User Profiles | 2 | 0 | 9 |
| `role-branches.spec.ts` | 10 — DataScope Role Branches | 14 | 0 | 0 |
| `e2e-flows.spec.ts` | 11 — Cross-Module E2E | 9 | 2 | 0 |
| `cross-cutting.spec.ts` | 12 — Non-Functional | 35 | 0 | 0 |

**Methodology note:** groups were authored and run in parallel by 4 sub-agents plus the orchestrator; each file's numbers above reflect that file's own final, isolated re-run (`--retries=0`, one file at a time) against the shared dev DB (`erp_db` at `localhost:7272`) — not one single simultaneous full-suite pass. An early run mixing multiple spec files concurrently against one dev server produced 60-second timeouts from resource contention (not real bugs); every file was subsequently re-run in isolation for the numbers reported here.

---

## 🔴 Real, Confirmed Application Bugs (highest priority — not test artifacts)

Every item below was reproduced at least once via a **raw `curl`/direct API call independent of Playwright**, so these are not test-authoring mistakes.

| # | Severity | Component | Bug |
|---|---|---|---|
| 1 | 🔴 HIGH | `PUT /api/pages/{id}` | **Update Page is completely broken for every user.** Rejects any syntactically valid JSON body as `400 INVALID_JSON`, including a hand-written plain-ASCII curl payload matching the documented shape exactly. `POST /api/pages` (create) works fine with the same fields — isolated to the update endpoint. |
| 2 | 🔴 HIGH | Add User drawer (frontend) | The real "Add User" UI always includes `enabled` in its `POST /api/users` body; the backend has `fail-on-unknown-properties=true` globally and `enabled` isn't in `CreateUserRequest`. **Every UI-driven user creation 400s** — Add User is non-functional end-to-end today (API-level creation without `enabled` works fine). |
| 3 | 🔴 HIGH | `RoleBranchApiService.searchActiveBranches()` | Sends filter field `isActive`; backend only recognizes `isActiveFl` (confirmed: `isActive`→400 `SEARCH_ERROR`, `isActiveFl`→200). Silently swallowed client-side, so **the Assign-Branch modal's branch dropdown is always empty**, for every role, on the real Role edit screen. Same bug class hit `/api/v1/org/branches/search` (User Profile branch picker) and `/api/roles/search`'s hardcoded `search` field (Users/Role-Access Advanced Filters panel) — three independent screens broken by the same field-naming mismatch pattern. |
| 4 | 🔴 HIGH | `NavCollapseComponent` / `MenuService.java` | `isEnabled` defaults `false` and is never flipped true because the backend's `MenuItemDto` never sets `permCode`. **Every sidebar menu entry with children (parent/collapse type) is permanently unclickable app-wide.** Previously invisible because no existing page had children until this test suite created one. |
| 5 | 🟡 MEDIUM | `/api/permissions/search` | Filtering by `module` (the doc's own §5 example) throws `500 DB_ERROR` ("Could not resolve attribute 'module' of Permission") — `module` lives on `Page`, not `Permission`, with no join through the `pageId` FK. |
| 6 | 🟡 MEDIUM | `/api/permissions/search` | Rejects both `pageId` and `pageCode` as search fields outright ("Field 'X' is not allowed for searching") — doc's TC-PAGE-021 assumes `pageId` is filterable. Only `name`/`module` work, and generated permission names embed the pageCode (`PERM_<PAGECODE>_<TYPE>`), so `name LIKE <pageCode>` is the only working equivalent. |
| 7 | 🟡 MEDIUM | Auth / session | Access tokens remain valid for their full 15-minute TTL after logout — `GET` with a just-logged-out token still returns `200`, not `401`. Logout only revokes the refresh token (confirmed twice, independently: `auth.spec.ts` TC-AUTH-011 and `e2e-flows.spec.ts` TC-E2E-008). Contradicts the doc's "all tokens for the session invalidated" and its own TC-AUTH-011 expectation. Common for stateless JWT designs but worth an explicit security sign-off. |
| 8 | 🟡 MEDIUM | `POST /api/auth/logout` | Returns `204` even with **no** Authorization header at all (doc expects `401`) — logout is implemented as unconditional/idempotent cookie-clearing, not an authenticated operation. |
| 9 | 🟡 MEDIUM | `PUT /api/users/{id}/roles`, `PUT /api/users/{id}` | `roleNames` resolves against `Role.roleName` (display name), **not** `roleCode` — the identifier used everywhere else in the Security module (Role-Pages assignment, URLs, etc.) and implied by the doc's code-shaped example (`["TESTROLE_X"]`). Passing a roleCode silently 404s as `ROLE_NOT_FOUND`. Confirmed independently by 3 different spec authors before they found the workaround. |
| 10 | 🟢 LOW | `POST /api/roles` | Role **names** must be unique too (`409 DUPLICATE_ROLE_NAME`), not just `roleCode` — undocumented in the test-cases doc. |
| 11 | 🟢 LOW | Users screen Advanced Filters | `UserFacade.convertSpecFiltersToSearchFilters()` sends `{field, op, value}`; backend expects `operator` — the panel 400s whenever used. |
| 12 | ℹ️ Doc-only | `/api/auth/login` | Refresh token is delivered via an `HttpOnly refresh_token` cookie (`Max-Age=604800`), **not** in the JSON body as `data.refreshToken`/`data.refreshExpiresIn` per the doc. `/api/auth/login-token` (what the real login screen uses) does put it in the body — the two login endpoints have different contracts. |
| 13 | ℹ️ Doc-only | Response envelope | Shape is conditional, not fixed: a success response omits `error` entirely and an error response omits `data` entirely (rather than both keys always being present, one of them `null`); both carry an undocumented `correlationId`. |

---

## Known Environment Gaps (not code bugs — infrastructure/data/process)

| Area | Gap | Evidence |
|---|---|---|
| §9 DataScope User Profiles | `PERM_USER_PROFILE_*` permissions were never seeded via the DB grant script — even **SUPER_ADMIN** gets `403`/redirected to `/access-denied` on every User Profile screen and endpoint. | 9 tests SKIPPED (`DB_PRECONDITION`); matches this project's existing tracked gap (`PLAN-SEC-002 Phase 3` memory: "PERM_USER_PROFILE_* grant pending manual SQL before ALIGN") — **not a new discovery**, but now has live-test confirmation. |
| §3 Self-Service Auth | No endpoint returns the raw activation/reset token (only via an internal event with no notification subscriber wired up), and this Playwright harness has no direct DB driver (`pg`) to read it live mid-test. | 6 tests SKIPPED (`DB_PRECONDITION`) for TC-SSA-011/013/014/018/019/021 — all manually verified end-to-end via `mcp__postgres__query` + curl during authoring; genuinely correct behavior, just not automatable without new test infra. |
| §11 TC-E2E-007 | No consuming module anywhere in the codebase enforces branch-scope (`allowedBranches[]` from the JWT claim) — confirmed via repo-wide grep, zero references outside SECURITY. Role-Branch + User Profile data persists correctly; nothing downstream reads it. | Matches the doc's own caveat in §11 ("this test case is a placeholder…"). `MISSING_IMPLEMENTATION`, not a regression. |
| TC-ROLE-013 | Could not positively demonstrate the privilege-escalation guard (system-level permission untouched by Copy-From) — no role in this DB currently holds a system-level (`page_id_fk IS NULL`) permission, and no endpoint grants one. | `DB_PRECONDITION` for that one sub-assertion; the core copy-from mechanics pass. |
| TC-RB-013 | Concurrency/race test not forced through the UI — the Assign-Branch confirm button disables for the duration of the save, making a UI double-click race structurally impossible. | Ran as two concurrent raw API `POST`s instead: exactly one `201` + one `409`, matching the doc's expected invariant — verified, just via a different mechanism than literally specified. |
| Rate limiting (TC-SSA-024) | **Positive finding, contradicts the doc's caution:** 429 rate limiting confirmed working on **all four** endpoints (login, signup, forgot-password, reset-password). Doc §5.8 said forgot/reset-password limiting was only "planned, not confirmed shipped." | Good news — flagging the doc as stale here, not the app. |

---

## Frontend Validators Stricter Than Backend (test-design note, not a bug)

Several UI forms reject input server-side documentation says is valid, meaning some documented backend behaviors are **unreachable through the real UI** and had to be tested at the API level instead:
- Page Code field: client pattern `^[A-Z][A-Z0-9_]*$` blocks the lowercase input needed to exercise the backend's uppercase-normalization behavior (TC-PAGE-002).
- Route field: client pattern `^\/[a-z0-9\-\/]*$` forbids underscores/uppercase the backend accepts fine.
- Role edit form has no rename path in the UI at all (roleCode/roleName both effectively fixed post-creation in practice) — TC-ROLE-004 tested via API.

---

## DB Validation Summary (representative sample)

| Check | Result |
|---|---|
| `pwtest*`/`PWTEST_*` users created across all runs | 204 rows, all identifiable by prefix per doc §13 cleanup convention |
| `PWTEST_*` roles created across all runs | 292 rows, all identifiable |
| Page create → 4 permission rows (`VIEW`/`CREATE`/`UPDATE`/`DELETE`) auto-generated | Confirmed via `name LIKE` search + direct `SELECT` |
| `sec_pages.parent_id_fk` linkage (API/UI/DB agreement) | Confirmed matching |
| Soft-deactivated pages excluded from `/api/pages/active` | Confirmed |
| Role-Branch composite-key uniqueness under concurrent writes | Confirmed (1× `201`, 1× `409` on simultaneous inserts) |
| Signup → `enabled:false` until activation (RULE-SEC-030) | Confirmed |
| Forgot-password anti-enumeration (RULE-SEC-038) | Confirmed — identical response for existing/non-existing email |

No orphaned or unexpected mutations found in any before/after check.

---

## Action Required

| Priority | Item | Action |
|---|---|---|
| 🔴 HIGH | Bug #1 | Fix `PUT /api/pages/{id}` — Update Page is fully broken |
| 🔴 HIGH | Bug #2 | Fix `CreateUserRequest` to accept (or the drawer to omit) `enabled` — Add User is fully broken via UI |
| 🔴 HIGH | Bug #3 | Fix `isActive`→`isActiveFl` (and the analogous `search`→`roleName`) field-naming mismatches — 3 broken UI features (Assign Branch ×2 screens, Advanced Filters) |
| 🔴 HIGH | Bug #4 | Have `MenuService.java` populate `permCode` on parent/collapse menu items — sidebar navigation with sub-items is unusable app-wide |
| 🟡 MEDIUM | Bug #5, #6 | Fix or scope `/api/permissions/search`'s allowed filter fields (`module` 500s; `pageId`/`pageCode` should probably be allowed, matching the doc's intent) |
| 🟡 MEDIUM | Bug #7, #8 | Security sign-off needed: is post-logout access-token validity and unauthenticated-logout-204 intentional? If not, needs a token blacklist or shorter TTL |
| 🟡 MEDIUM | Bug #9 | Decide whether `roleNames` should resolve by code (consistent with the rest of the module) or update the doc/API contract to clarify it's by name |
| 🟠 BLOCKER | User Profiles gap | Run the pending `PERM_USER_PROFILE_*` grant SQL (tracked separately in `PLAN-SEC-002 Phase 3`) — unblocks 9 currently-skipped tests and the entire §9 feature for all users including admin |
| 🟢 LOW | Bugs #10, #11 | Minor fixes when convenient (duplicate role-name check documentation; Users Advanced Filters `operator` field) |
| 🟢 LOW | Doc updates | #12/#13 (login response contracts), TC-SSA-024 rate-limiting status — update `security-module-test-cases.md` to match confirmed reality |

---

## Suite Artifacts

- Spec files: `frontend/e2e/security/specs/*.spec.ts` (11 files)
- Page Objects: `frontend/e2e/security/pages/*.ts`
- Shared support: `frontend/e2e/security/support/auth.ts` (API-based session seeding + forced-English-locale fix — app defaults to Arabic), `support/api-client.ts`
- Config changes: `frontend/playwright.config.ts` — added `channel: 'chrome'` (system Chrome; Playwright's own Chromium/headless-shell download was unreliable on this network) and a default `baseURL`
- Screenshots/traces: `frontend/test-results/` (note: overwritten between runs since multiple spec files share one output directory — not preserved historically for every individual run referenced above)
