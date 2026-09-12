# FILE — api-verify problems report

Generated: 2026-09-12 · RUN_ID `223692` · tier **Full** (api-docs + P3_5_BE/test-execution-manifest.md present)
Target: `http://localhost:7272` (Dev/Test — localhost, per api-verify-config.md §4.1)

**42 passed · 0 failed** across 8 suites · 7 observation(s), which never affect the totals.

## Suites

| Suite | Passed | Failed |
|---|---|---|
| PREFLIGHT (stage A0) | 2 | 0 |
| FileCategory | 8 | 0 |
| FileDocument — upload (API-FILE-001) | 10 | 0 |
| FileDocument — metadata & list | 5 | 0 |
| FileDocument — access token & download | 5 | 0 |
| FileDocument — lifecycle (API-FILE-006) | 6 | 0 |
| FileLookups (API-FILE-008) | 3 | 0 |
| Auth delegation (RULE-FILE-004) | 3 | 0 |

## Failures

_None._

## Problem buckets (stage G)

### Likely real bug

- **TC-BE-FILE-009 — ERR-0004 `FILE_DOCUMENT_OWNERSHIP_REQUIRED` is unreachable over HTTP.** The governed triple RULE-FILE-005 → ERR-0004 → TC-BE-FILE-009 → 400 says an upload missing ownership is refused with this module error code. The service does raise it (`FileService.requireOwnership`), but the controller binds `@Valid @ModelAttribute UploadRequest`, whose `@NotNull`/`@NotBlank` fire first, so every HTTP caller gets the platform-generic `VALIDATION_ERROR` envelope with a `fieldErrors[]` entry instead. The HTTP status (400) and the bilingual message are correct; only the code differs. The module code stays reachable on the in-process provider path, which bypasses bean validation. Fix direction is a decision for the owner — either the plan's ERR-0004 expectation is narrowed to the in-process path, or the controller stops relying on bean validation for the ownership triple. **Not fixed here — production code is out of bounds for this run.**

### Test assumption mismatch

- _None outstanding._ One was found and corrected inside this script during the run: the download-size assertion originally compared the served body against a local 2 MiB constant while the generator emitted a slightly larger payload. It now compares against the `fileSize` the server itself recorded at upload, which is what the api-docs actually document.

### Infrastructure

- _None._ No connection or timeout failure occurred during the reported run.

### Documentation findings (not failures)

- **Upload multipart part names are documented in a spelling the endpoint does not accept.** `api-docs/endpoints/file-documents.md` renders the upload body as a nested `request` object (`request.ownerId`, `request.ownerType`, `request.moduleCode`, `request.fileCategoryFk`) with a matching JSON request example. The controller binds `@ModelAttribute`, i.e. FLAT form fields (`ownerId`, …); the documented nested spelling returns 400 `VALIDATION_ERROR` (recorded as an observation above). A consumer following the api-doc literally cannot upload a file. This is a generator rendering gap, not a hand-editable one — `api-docs/` is auto-generated.
- **FILE's error codes do not follow the `{MOD}-{http}[-{SLUG}]` format `api-verify-config.md` §3 states.** Every code in FILE's Known Error Codes table is `FILE_DOCUMENT_*` / `FILE_CATEGORY_*` / `FILE_ACCESS_TOKEN_*` / `FILE_LOOKUP_*` — SCREAMING_SNAKE, no HTTP segment — where FIN and SEC use `FIN-409-DIMENSION-DUP` style. FILE is a Legacy Path module, so this is most likely pre-split convention that predates the rule rather than a defect; this script asserts FILE's own documented codes, and the divergence is flagged here for a human decision (either `api-verify-config.md` records the legacy exception, or FILE's codes are migrated).
- **The `action` query parameter on `DELETE /api/v1/files/{id}` is documented without its allowed values.** The api-doc lists `action` as an optional string with no description and no enum; the implementation accepts exactly `ARCHIVE` (default) and `DELETE`. Anything else returns 400 `FILE_DOCUMENT_INVALID_TRANSITION` — a code whose documented Status is `BUSINESS_RULE_VIOLATION` (422), so the same code is served under two different HTTP statuses depending on the path that raises it.
- **Upload returns 201, the api-doc says 200.** `ServiceResult.success(..., Status.CREATED)` maps to 201 and the governed TCs say 201; the generated api-doc documents `Response 200 — OK` for `POST /api/v1/files` (and for `POST /api/v1/files/categories`, which also returns 201). Generator gap.

## Preconditions (stage A0)

_All preconditions satisfied._

## Observations (stage E — not pass/fail)

- **stage A0 note — moduleCode/ownerType are unvalidated free strings** — owner triple (943692/PURCHASE_ORDER/PROC) readable: HTTP 200, totalElements=0 — moduleCode is an unconstrained string here (no FK / registry validation on upload), so it is not a precondition row
- **stage E — deactivate an already-deactivated category (idempotency)** — second DELETE on an already-inactive category -> HTTP 204 /None (no artifact states the expected outcome)
- **stage E — api-docs show a nested `request.*` multipart spelling** — documented nested spelling (request.ownerId/...) -> HTTP 400/VALIDATION_ERROR; the flat spelling the controller binds is what succeeds
- **stage E — two access tokens issued for the same file** — two concurrently-issued tokens: first -> HTTP 200, second -> HTTP 200 (no artifact states whether issuing a new token invalidates an unconsumed one)
- **stage E — a DELETED file's bytes cannot be re-read over HTTP** — access token for a DELETED file: HTTP 404/FILE_DOCUMENT_NOT_FOUND — a soft-deleted file is treated as gone, so the download route cannot re-prove byte retention for this path
- **stage E — an undocumented `action` value on DELETE /files/{id}** — undocumented action value -> HTTP 400/FILE_DOCUMENT_INVALID_TRANSITION (api-docs do not enumerate the allowed `action` values)
- **stage E — an MDL key owned by another module** — a real MDL key FILE does not own -> HTTP 404/FILE_LOOKUP_KEY_UNKNOWN (FILE is not a generic MDL pass-through)

## Coverage gaps (not executed, not claimed as passing)

- FileCategory has no documented activate endpoint (api-docs lists only GET/PUT/DELETE/POST/search), so the deactivate->activate round trip named in the ENTITY CRUD CHECKLIST cannot be exercised over HTTP; deactivated rows stay inactive.
- TC-BE-FILE-006 (expired half) — the access token TTL is 10 minutes (FileAccessTokenDomainService.TOKEN_TTL), so proving expiry needs a 10-minute idle wait inside the run. The REUSED half of the same TC is exercised end-to-end (issue -> download -> download again -> 401); expiry is NOT executed and is not claimed as passing.
- TC-BE-FILE-019 (permission half, MANDATORY-J-5: 'no VIEW -> 403') — NOT executed. Every FILE service method carries its @PreAuthorize commented out behind a `TODO: SEC-PENDING` marker (FileService.store/issueAccessToken/getMetadata/listByOwner/softDelete, all five FileCategoryService methods), so a 403 is not reachable for any caller and creating a de-privileged user would only prove that. The 2xx half of TC-BE-FILE-019 IS covered by the FileCategory suite.

## Surviving records

- FILE_DOCUMENT rows [26, 27, 28, 29, 30] — set to fileStatusId=DELETED (soft). FILE documents no hard delete, and RULE-FILE-006 RETAINS the bytes, so the BYTEA FILE_CONTENT column of each of these rows still holds this run's uploaded payload (~2048 KB for the largest). Purge SQL: DELETE FROM FILE_DOCUMENT WHERE ID IN (26, 27, 28, 29, 30);
- FILE_CATEGORY rows [16, 17, 18] (codes CONTRACTS_223692, IMAGES_223692, RETIRED_223692) — deactivated (soft); FILE documents no hard delete and no activate endpoint. Purge SQL: DELETE FROM FILE_CATEGORY WHERE ID IN (16, 17, 18);

### Permanent residue (reference tables other modules' rules read)

_None. FILE_CATEGORY and FILE_DOCUMENT are module-local; FILE reads MDL's lookups but never writes to them._

## Privileges

No permission grant was created or revoked by this run, and no grant journal was written. Stage I was skipped because there is nothing to grant: every FILE service method has its `@PreAuthorize` commented out behind a `TODO: SEC-PENDING` marker, so the module enforces no FILE permission at all — the only live authorization is `isAuthenticated()` on download and lookups. No standing privilege was left behind.

