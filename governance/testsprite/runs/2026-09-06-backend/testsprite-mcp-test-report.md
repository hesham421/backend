# TestSprite AI Testing Report(MCP)

---

## 1️⃣ Document Metadata
- **Project Name:** backend
- **Scope:** CU (Configuration) module + FILE module (Categories, Documents, Lookups); `com.erp.common` has no REST endpoints of its own and is exercised indirectly
- **Date:** 2026-09-06
- **Prepared by:** TestSprite AI Team + Claude Code

---

## 2️⃣ Requirement Validation Summary

### Requirement: CU — Configuration Management (`/api/v1/common/configurations`)

#### Test TC001 create_new_configuration_entry
- **Test Code:** [TC001_create_new_configuration_entry.py](../governance/modules/CU/testsprite/tests/TC001_create_new_configuration_entry.py)
- **Status:** ❌ Failed
- **Test Error:** `Expected HTTP 200, got 403` — `ACCESS_DENIED`
- **Analysis / Findings:** The `admin` user's JWT carries no `CONFIG_*` authority. `ConfigurationService` requires `CONFIG_CREATE`/`CONFIG_VIEW`/`CONFIG_UPDATE`/`CONFIG_DEACTIVATE` (`PermissionConstants`), but no migration ever seeds these permission codes into any role. **The entire CU Configuration API is unreachable by any account**, including `admin`. Root cause: missing permission seed migration for CU, confirmed by `grep -rl "CONFIG_CREATE" src/main/resources/db/migration/` returning no results.
---

#### Test TC002 search_configuration_entries_with_pagination
- **Test Code:** [TC002_search_configuration_entries_with_pagination.py](../governance/modules/CU/testsprite/tests/TC002_search_configuration_entries_with_pagination.py)
- **Status:** ❌ Failed
- **Test Error:** `403 Client Error` on the setup `POST /api/v1/common/configurations` call
- **Analysis / Findings:** Same root cause as TC001 (missing `CONFIG_CREATE` seed) — blocks the search test's fixture setup before search itself can be exercised.
---

#### Test TC003 update_existing_configuration_entry
- **Test Code:** [TC003_update_existing_configuration_entry.py](../governance/modules/CU/testsprite/tests/TC003_update_existing_configuration_entry.py)
- **Status:** ❌ Failed
- **Test Error:** `ACCESS_DENIED` on setup create, then again on cleanup delete
- **Analysis / Findings:** Same root cause as TC001.
---

#### Test TC004 get_configuration_entry_by_key
- **Test Code:** [TC004_get_configuration_entry_by_key.py](../governance/modules/CU/testsprite/tests/TC004_get_configuration_entry_by_key.py)
- **Status:** ❌ Failed
- **Test Error:** `403 Client Error` on setup create
- **Analysis / Findings:** Same root cause as TC001.
---

#### Test TC005 deactivate_configuration_entry_by_key
- **Test Code:** [TC005_deactivate_configuration_entry_by_key.py](../governance/modules/CU/testsprite/tests/TC005_deactivate_configuration_entry_by_key.py)
- **Status:** ❌ Failed
- **Test Error:** Bare `AssertionError` in setup
- **Analysis / Findings:** Same root cause as TC001 (create step fails with 403 before deactivate is ever exercised).
---

### Requirement: FILE — Category Management (`/api/v1/files/categories`)

#### Test TC006 create_new_file_category
- **Test Code:** [TC006_create_new_file_category.py](../governance/modules/FILE/testsprite/tests/TC006_create_new_file_category.py)
- **Status:** ✅ Passed
- **Analysis / Findings:** Create works correctly with `categoryCode`/`nameAr`/`nameEn`.
---

#### Test TC007 search_file_categories_with_pagination
- **Test Code:** [TC007_search_file_categories_with_pagination.py](../governance/modules/FILE/testsprite/tests/TC007_search_file_categories_with_pagination.py)
- **Status:** ❌ Failed
- **Test Error:** `500 Server Error` on `POST /api/v1/files/categories/search`
- **Analysis / Findings:** A manual reproduction with an empty `{}` filter body succeeded (200), so the 500 is payload-shape-dependent rather than universal — TestSprite's generated filter/sort payload triggers an unhandled exception that should instead be a 400. Needs the generated request body cross-checked against `CategorySearchRequest`/`SpecBuilder` to find which field/operator combination isn't validated before use.
---

#### Test TC008 update_file_category_by_id
- **Test Code:** [TC008_update_file_category_by_id.py](../governance/modules/FILE/testsprite/tests/TC008_update_file_category_by_id.py)
- **Status:** ✅ Passed
---

### Requirement: FILE — Document Management (`/api/v1/files`)

#### Test TC009 upload_file_with_metadata
- **Test Code:** [TC009_upload_file_with_metadata.py](../governance/modules/FILE/testsprite/tests/TC009_upload_file_with_metadata.py)
- **Status:** ❌ Failed
- **Test Error:** `500 INTERNAL_ERROR: An unexpected error occurred` on `POST /api/v1/files`
- **Analysis / Findings:** **Reproduced independently outside TestSprite** with a plain `curl` multipart upload carrying valid `ownerId`/`ownerType`/`moduleCode` — the endpoint fails for every upload, not a TestSprite payload artifact. Prime suspect: `FileDocument.fileContent` is annotated `@Lob @Basic(fetch = FetchType.LAZY)` on a raw `byte[]`; lazy-loading a non-entity `@Basic` attribute requires build-time bytecode enhancement (Hibernate enhance plugin) which this project does not appear to configure, and `@Lob` byte[] handling on PostgreSQL is version-sensitive across Hibernate ORM 7.2. Needs server-side stack trace (not visible from this session — the app runs as a detached `mvn spring-boot:run` process with no captured log file) to confirm.
---

#### Test TC010 issue_access_token_for_file_download
- **Test Code:** [TC010_issue_access_token_for_file_download.py](../governance/modules/FILE/testsprite/tests/TC010_issue_access_token_for_file_download.py)
- **Status:** ❌ Failed
- **Test Error:** `File upload failed: INTERNAL_ERROR` (fixture setup)
- **Analysis / Findings:** Downstream of TC009 — this test never gets to exercise access-token issuance because the upload fixture fails first.
---

## 3️⃣ Coverage & Matching Metrics

- **20%** of tests passed (2 / 10)

| Requirement                          | Total Tests | ✅ Passed | ❌ Failed |
|---------------------------------------|-------------|-----------|-----------|
| CU — Configuration Management          | 5           | 0         | 5         |
| FILE — Category Management             | 3           | 2         | 1         |
| FILE — Document Management             | 2           | 0         | 2         |

---

## 4️⃣ Key Gaps / Risks

1. **[High] CU Configuration API is completely inaccessible** — `CONFIG_VIEW`/`CONFIG_CREATE`/`CONFIG_UPDATE`/`CONFIG_DEACTIVATE` permissions are referenced in code (`PermissionConstants`, `ConfigurationService`) but never seeded into the database by any Flyway migration, so no role — including `admin` — can ever be granted them. This blocks all 5 endpoints under `/api/v1/common/configurations` for every user. Fix: add a new Flyway migration seeding these permission codes and granting them to the appropriate role(s), following the existing `V9__file_security_seed.sql` pattern for FILE.
2. **[High] File upload (`POST /api/v1/files`) always returns 500**, reproduced independently of TestSprite. Suspect the `@Lob @Basic(fetch = FetchType.LAZY)` mapping on `FileDocument.fileContent` (`byte[]`). This blocks upload, and transitively, access-token issuance and download testing (TC010).
3. **[Medium] `/api/v1/files/categories/search` returns 500 instead of 400** for at least one filter payload shape TestSprite generated — an unhandled exception path should be replaced with proper validation/error handling in `FileCategoryService.search` / the shared `SpecBuilder`.
4. **Test coverage caveat:** this run was deliberately scoped to CU + FILE only (per user request) using a hand-corrected `code_summary.yaml` — SECURITY, MDM, and NOTIF modules were not exercised in this run.

---

## 5️⃣ Fix Log (post-run, same day)

Applied via the fix-bugs.md diagnose → fix → re-run loop, against the same running instance:

| # | Root cause | Fix | File(s) | Verified |
|---|---|---|---|---|
| 1 | `FILE_CONTENT` is BYTEA but `@Lob` on `byte[]` made Hibernate bind it as a LOB (OID/bigint) | Removed `@Lob` | `file/entity/FileDocument.java` | TC009 upload now 201, re-run PASS |
| 2 | Malformed/wrong-shaped JSON body threw unhandled `HttpMessageNotReadableException` → generic 500 instead of 400 | Added `@ExceptionHandler(HttpMessageNotReadableException.class)` → 400 VALIDATION_ERROR | `common/web/GlobalExceptionHandler.java` | TC007's real payload-shape bug (see #4) now surfaces as 400, not 500 |
| 3 | `findMetadataById`/`findMetadataByOwner` (alias-select JPQL → `FileMetadataView` interface projection) failed with `Failed to convert from type [Object[]] to type [FileMetadataView]` under this project's Spring Data JPA 4.0.1 / Hibernate 7.2 combination — broke `GET /files/{id}`, `GET /files`, `POST /files/{id}/access-token` | Switched both queries to return `Tuple`; added `FileMetadataView.from(Tuple)` (a local record satisfying the interface via matching accessor names) | `file/repository/FileDocumentRepository.java`, `file/repository/FileMetadataView.java`, `file/service/FileService.java` | TC010 access-token step now 200, re-run PASS |
| 4 | TC007's own payload used a wrong `filters` shape (nested object instead of `List<SearchFilter>`) and a DTO-name field (`isActiveFl`) instead of the entity property (`isActive`) — a **test bug**, exposed as a 500 until fix #2 turned it into a clear 400 | Corrected `filters` to `[{field, operator, value}, ...]` with `isActive` | `governance/modules/FILE/testsprite/tests/TC007_search_file_categories_with_pagination.py` | Re-run PASS |
| 5 | TC010 asserted upload returns `200` (actual, correct contract: `201 Created`) and asserted response field `token` (actual field: `accessToken`) — **test bugs** | Corrected both assertions | `governance/modules/FILE/testsprite/tests/TC010_issue_access_token_for_file_download.py` | Re-run PASS |

**Result:** FILE module archived suite — **5/5 PASS** after fixes (was 2/5).

**CU module — NOT fixed, flagged instead:** `CONFIG_VIEW/CREATE/UPDATE/DEACTIVATE` cannot be seeded under the current SEC schema. `SEC_PERMISSION.PAGE_FK` is `NOT NULL` (`V2__sec_security_schema.sql`), and CU is explicitly documented as a **no-screens, backend-only** module (`governance/modules/CU/P1/srs.md` PART B: "لا SCR-IDs، لا SEC_PAGES، CORE-9 لا ينطبق"; SEC-BE.md: "SECURITY SEED DATA REQUIREMENTS: none"). A prior precedent for the identical structural conflict exists in `V7__notif_security_seed.sql` (NOTIF dispatch), which deliberately left the permission unseeded rather than force a page onto it. Seeding CU's authorities would require either (a) inventing a `SEC_PAGE` for a module explicitly documented as screen-less — contradicting that doc, or (b) relaxing `@PreAuthorize` to `isAuthenticated()` only (matching the NOTIF dispatch pattern) — a deliberate authorization-strength decision, not a bug fix. Per `fix-bugs.md`'s STOP rule for requirements conflicts, this needs an explicit human decision and was not changed. CU archived suite remains 0/5 PASS, unchanged (no regression — same root cause as the original run).
