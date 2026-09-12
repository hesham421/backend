# CU — api-verify problems report

Generated: 2026-09-12 · RUN_ID `223685` · tier **Full** (api-docs + P3_5_BE/test-execution-manifest.md both present)
Target: `http://localhost:7272` (Dev/Test — localhost, per api-verify-config.md §4.1)

**22 passed · 0 failed** across 2 suites · 13 observation(s), which never affect the totals.

## Suites

| Suite | Passed | Failed |
|---|---|---|
| PREFLIGHT (stage A0) | 3 | 0 |
| AppConfiguration | 19 | 0 |

## Failures

_None._

## Stage G — failure classification

_No failures to classify._

## Documentation findings (no assertion attached)

- **api-docs, all four write endpoints** — `POST /api/v1/common/configurations` is documented as `Response 200 — OK`, but the endpoint returns **201 Created** (`ServiceResult.success(..., Status.CREATED)`, and index.md's own 'Status -> HTTP Status Reference' maps CREATED -> 201). This is springdoc's default 200 leaking into the generated doc; the test asserts 201, per the test plan and that mapping table.
- **api-verify-config.md §3 vs CU's runtime codes** — the config states the error-code format `{MOD}-{http}[-{SLUG}]` (e.g. `SEC-409-USER-DUP`). CU emits descriptive codes instead (`APP_CONFIGURATION_KEY_DUPLICATE`, ...), deliberately: `CuErrorCodes`' javadoc cites gov-enforce-error-handling's `<ENTITY>_<SCENARIO>` format. Two governance documents disagree about the project-wide code format; SEC's own codes (observed here: `SEC-401-INVALID-CREDENTIALS`) follow the config's format, so the divergence is real and module-by-module, not a CU quirk. Needs a human reconciliation.
- **api-verify-config.md §3 vs the actual error envelope** — the config describes `{code, messageAr, messageEn}`. The live envelope is `{code, message, fieldErrors}` — a single `message`, resolved from the request's `Accept-Language` (verified: the same 404 returns 'الإعداد غير موجود.' under `ar` and 'Configuration not found.' under `en`). Both languages are reachable, but not simultaneously in one response, so an `messageAr`/`messageEn` assertion would be wrong against this stack. The language assertions here send two requests instead.
- **backend-test-plan TC-BE-CU-007 / TC-BE-CU-012** — both describe search as `GET /api/v1/common/configurations?configKey=...&page=0&size=10`. No such GET endpoint exists; the api-docs (and the controller) document `POST /api/v1/common/configurations/search` with a filters/sort/page body. The api-docs are authoritative per the skill, so the script uses POST /search with a `{field: configKey, operator: LIKE}` filter. Documentation finding against the plan.
- **backend-test-plan TC-BE-CU-001** — expects `appConfigurationPk` and `isActiveFl` in the response. The live `ConfigurationResponse` exposes `id` and `isActive` (`isActiveFl` is the DB column name, per api-verify-config.md §3's DELETE-semantics row). The script asserts the api-docs' field names.
- **Permission gating absent** — api-verify-config.md §3 describes a `PERM_<PAGE_CODE>_<ACTION>` permission model for every module. CU has none in force: every `@PreAuthorize(hasAuthority(PermissionConstants.CONFIG_*))` in `ConfigurationService` is commented out behind a `TODO: SEC-PENDING` marker, CU has no row in `SEC_MODULE_REG`, and no `CONFIG_*` screen or action is registered. Any authenticated user can therefore create, update and deactivate platform configuration. Unauthenticated access IS blocked (401, observed). Flagged as a governance gap, not asserted — no artifact states CU's intended permission codes.
- **index.md pagination envelope** — the table omits `content`, which every search response carries (verified in preflight). Minor generator gap.

## Preconditions (stage A0)

_All preconditions satisfied. CU's payloads reference no externally-owned value (no FK, no lookup key, no owner-module code), so stage A0 verified reachability, key availability and the documented page envelope only._

## Observations (stage E — not pass/fail)

- **stage E — RULE-CU-002 violated with a blank configValue rather than an omitted one** — blank (whitespace) configValue -> HTTP 400 (VALIDATION_ERROR) — same generic code as the omitted-field case
- **stage E — an unknown property (configKey) in the update body** — unknown property `configKey` in the update body -> HTTP 200 (accepted, silently ignored) — the client is told the rename succeeded
- **stage E — configKey exactly at maxLength 150** — configKey at the documented 150-char limit -> HTTP 201 (accepted)
- **stage E — configKey one character over maxLength 150** — configKey one over maxLength 150 -> HTTP 400 (VALIDATION_ERROR)
- **stage E — notes one character over maxLength 2000 (update)** — notes one over maxLength 2000 -> HTTP 400 (VALIDATION_ERROR)
- **stage E — isActive sent with the wrong type** — isActive sent as a string -> HTTP 400 (VALIDATION_ERROR)
- **stage E — update with the required configValue omitted** — update with configValue omitted -> HTTP 400 (VALIDATION_ERROR)
- **stage E — requested page size above the documented maximum of 200** — size=500 (documented max 200) -> HTTP 200; effective size=200
- **stage E — an unknown sort field on search** — unknown sortField -> HTTP 200 (None)
- **stage E — an unknown search FILTER field** — unknown filter field -> HTTP 200, totalElements=34 (silently ignored vs rejected)
- **stage E — an unauthenticated call to a CU endpoint** — unauthenticated GET -> HTTP 401 (SEC-401-INVALID-CREDENTIALS)
- **stage E — deactivate an already-deactivated configuration** — second DELETE on an already-inactive row -> HTTP 204 (None)
- **stage E — reactivating through the update endpoint's isActive field** — update with isActive=true on an inactive row -> HTTP 200; isActive=True (CU documents no dedicated activate endpoint)

## Surviving records

- AppConfiguration `MAIL_SMTP_HOST_223685` (id=31) — deactivated (soft, isActive=false); CU documents no hard delete, so the row remains in CU_APP_CONFIGURATION.
- AppConfiguration `MAIL_SMTP_HOST_B_223685` (id=32) — deactivated (soft, isActive=false); CU documents no hard delete, so the row remains in CU_APP_CONFIGURATION.
- AppConfiguration `MAIL_SMTP_HOST_SQLI_223685` (id=33) — deactivated (soft, isActive=false); CU documents no hard delete, so the row remains in CU_APP_CONFIGURATION.
- AppConfiguration `AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA_223685` (id=34) — deactivated (soft, isActive=false); CU documents no hard delete, so the row remains in CU_APP_CONFIGURATION.

### Residue from this session outside the script's own tracking

- `PROBE_AUDIT_12644` (id=5) — created by a manual `curl` probe while diagnosing the TC-BE-CU-008 finding, not by this script, so the tracked-id set never held it. Retired afterwards through the documented `DELETE /api/v1/common/configurations/{key}` (HTTP 204); `CU_APP_CONFIGURATION` held zero active rows after that call.
- `PROBE_KEY_61874` (id=14) — an **ACTIVE** row this run did not create and did not touch. It appeared in `CU_APP_CONFIGURATION` between two iterations of this script, so it belongs to another session running against the same database. Disclosed rather than cleaned up: the api-verify skill forbids mutating a record the run did not create. A human may retire it with `DELETE /api/v1/common/configurations/PROBE_KEY_61874`.

### Permanent residue (reference data other modules read)

_None — every row this run created was confirmed deactivated._

## Privileges

No permission grant was created or revoked by this run, and no grant journal was written. CU's endpoints are authenticated but not permission-gated at the time of this run: every `@PreAuthorize(hasAuthority(PermissionConstants.CONFIG_*))` on `ConfigurationService` is commented out behind a `TODO: SEC-PENDING` marker, and CU registers no module row in `SEC_MODULE_REG` and no `CONFIG_*` screen or action in SEC's registries — so no CU permission exists to grant or to hold. Stage I was skipped entirely and **no standing privilege was left behind**.

