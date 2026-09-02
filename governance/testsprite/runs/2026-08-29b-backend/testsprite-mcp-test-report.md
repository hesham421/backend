# TestSprite AI Testing Report (MCP) — Organization Module Follow-up

---

## 1️⃣ Document Metadata
- **Project Name:** backend
- **Module Focus:** ORG — targeted follow-up on two bugs surfaced by the same-day frontend TestSprite run (see `frontend/governance/testsprite/runs/2026-08-29c-frontend/`)
- **Date:** 2026-08-29
- **Prepared by:** TestSprite AI Team + Claude Code
- **Server Mode:** production (Spring Boot app run via `java -jar`), `http://localhost:7272`
- **Test Account:** `admin` (password rotated mid-session to `admin123` by an earlier automated run — see note below; restored access rather than resetting the DB)
- **Note:** The `admin` account's password was found changed from the originally-seeded `admin`/`admin` partway through today's testing (DB `updated_at`/`updated_by` showed a recent self-update, most likely a side effect of an earlier auto-generated test in this same session). `admin123` was confirmed to work and used for this run instead of resetting the database.

---

## 2️⃣ Requirement Validation Summary

### Requirement: Organization — Regions

#### TC001 GET region should return region with regionType — ❌ Failed
[TC001_post_api_v1_org_regions_id_get_should_return_region_with_regiontype.py](./TC001_post_api_v1_org_regions_id_get_should_return_region_with_regiontype.py)
- **Error:** Test setup failed before it could reach its actual assertion — fetching region-type options via `GET /api/lookups/REGION_TYPE` returned `400 Bad Request`.
- **Root cause (confirmed via direct API + DB check):** The `REGION_TYPE` lookup key **does not exist** in `md_master_lookup` at all. Every other Organization entity has its own type lookup seeded (`BRANCH_TYPE`, `COST_CENTER_TYPE`, `LEGAL_ENTITY_TYPE`, `LOCATION_SITE_TYPE`, `DEPARTMENT_NODE_TYPE`) — `REGION_TYPE` is simply missing. Calling `GET /api/lookups/REGION_TYPE` for a nonexistent key doesn't return a clean empty list or 404; it throws `Cache 'lookupValues' does not allow 'null' values` (Spring's `@Cacheable` isn't configured with `unless="#result == null"`, so a null lookup result crashes the cache layer instead of being handled), surfaced to the client as an opaque `400`.
- **This directly explains the frontend bug found in the same-day frontend run (TC010 `Create_a_region_with_a_legal_entity`):** the Region form's "نوع المنطقة" (Region Type) dropdown has no options to select from server-side, so whatever the UI appears to let a user pick never actually sets a real value, and the field saves blank.
- **Two real, separate backend bugs:**
  1. **Missing seed data:** add a `REGION_TYPE` master lookup with detail values, matching the pattern of every other org entity's type lookup.
  2. **Lookup endpoint error handling:** `GET /api/lookups/{lookupCode}` should return an empty array (or a clean 404) for an unknown/empty lookup code, not a 400 caching exception — fix the `@Cacheable` config on the lookup-values cache to allow/handle null results.

### Requirement: Organization — Departments

#### TC002 PUT department deactivate should flip isActive flag — ✅ Passed
[TC002_put_api_v1_org_departments_id_deactivate_should_flip_isactive_flag.py](./TC002_put_api_v1_org_departments_id_deactivate_should_flip_isactive_flag.py)
- **Confirms the backend correctly flips `isActive` to false on `PUT /api/v1/org/departments/{id}/deactivate`.**
- **This narrows the frontend bug found in the same-day frontend run (TC017 `Edit_and_deactivate_a_department_in_the_branch_tree`) to the frontend layer only** — the API does its job; the department list UI in `src/pages/Organization/Departments.tsx` / `src/departments/hooks.ts` isn't reflecting the updated state (most likely a missing query-cache invalidation after the deactivate mutation, unlike Branches' `useDeactivateBranch`, which does invalidate correctly).

---

## 3️⃣ Coverage & Matching Metrics

**1 / 2 passed — 50%**

| Requirement                    | Total Tests | ✅ Passed | ❌ Failed |
|----------------------------------|:-----------:|:---------:|:---------:|
| Organization — Regions           | 1           | 0         | 1         |
| Organization — Departments       | 1           | 1         | 0         |
| **Total**                         | **2**       | **1**     | **1**     |

This was a small, deliberately targeted run (2 cases) aimed at root-causing the two bugs found in the same-day frontend run, not a broad regression sweep — see `frontend/governance/testsprite/runs/2026-08-29c-frontend/` for the 30-case frontend run this follows up on.

---

## 4️⃣ Key Gaps / Risks

1. **`REGION_TYPE` lookup entirely missing from seed data (confirmed bug, backend).** Blocks the Region Type field everywhere it's used (Region create/edit form, any report or picker keyed on it). Needs a Flyway migration adding the lookup + detail rows, following the pattern of `BRANCH_TYPE`/`LOCATION_SITE_TYPE`/etc.
2. **Lookup-values endpoint mishandles unknown codes (confirmed bug, backend).** `GET /api/lookups/{code}` throws a raw caching exception (400) instead of an empty list/404 when the code doesn't resolve — will resurface as a confusing error for any future lookup code that's misspelled or not yet seeded, not just this one.
3. **Departments deactivate is backend-correct, frontend-broken (confirmed bug, frontend only).** No further backend investigation needed here — fix belongs in `src/departments/hooks.ts` / `Departments.tsx`'s list refresh after deactivate.
4. **Operational note, not a product bug:** the `admin` account's password was mutated by an earlier automated test run today. If TestSprite runs against this backend continue to assume `admin`/`admin`, they'll fail on auth alone — worth checking whether one of the generated test scripts includes an unintended password-change step that should be idempotent/reverting, or whether the seed data should be reset before the next run.
5. **Not covered by this run:** the remaining 5 Organization entities (Legal Entities, Branches, Cost Centers, Profit Centers, Location Sites) at the backend/API level — the same-day frontend run exercised them through the UI with no failures, but no direct backend-level assertions were run for them today.
