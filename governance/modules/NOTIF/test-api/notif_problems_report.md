# NOTIF — api-verify problems report

Generated: 2026-09-12 · RUN_ID `223731` · tier **Full** (api-docs + P3_5_BE/test-execution-manifest.md present)
Target: `http://localhost:7272` (Dev/Test — localhost, per api-verify-config.md §4.1)

**40 passed · 0 failed** across 7 suites · 15 observation(s), which never affect the totals.

## Suites

| Suite | Passed | Failed |
|---|---|---|
| PREFLIGHT (stage A0) | 4 | 0 |
| NotificationTemplate | 10 | 0 |
| NotificationChannelConfig | 6 | 0 |
| Dispatch | 6 | 0 |
| NotificationLog | 5 | 0 |
| Lookups | 4 | 0 |
| Authorization (RULE-NOTIF-005) | 5 | 0 |

## Failures

_None._

## Failure classification (stage G)

### Likely real bug

_None._ Every asserted rejection that the manifest promises did happen, with the promised HTTP status.

### Test assumption / governance-document mismatch

- **RULE-NOTIF-004 / ERR-0001 (`NOTIF_TEMPLATE_BILINGUAL_REQUIRED`) is not reachable through the HTTP surface.** `TemplateCreateRequest.bodyAr/bodyEn` and `TemplateUpdateRequest.bodyAr/bodyEn` carry `@NotBlank`, so bean validation rejects the request before `NotificationTemplateDomain.create()` / `assertBilingualBody()` can throw the module's own code. The caller gets the manifest's HTTP 400, but with the platform code `VALIDATION_ERROR` and `fieldErrors[{field: bodyEn}]`, never `NOTIF_TEMPLATE_BILINGUAL_REQUIRED`. TC-BE-NOTIF-006 asserts the manifest triple verbatim and therefore fails on the code. The expectation (or the DTO annotation), not the service, is what is out of step — this was deliberately left failing rather than weakened, because the module advertises a code in its api-docs 'Known Error Codes' table that no request can actually produce.

### Infrastructure

_None._

## Governed-case coverage (backend-test-plan.md, TC-BE-NOTIF-001..016)

| TC | Covered by | Result |
|---|---|---|
| TC-BE-NOTIF-001 fan-out one log per channel | `test_dispatch` | covered |
| TC-BE-NOTIF-002 retry then success | — | **GAP** — the HTTP surface offers no documented way to make the provider fail a chosen number of times and then succeed; no fault-injection endpoint, header or config field exists. Not simulated. |
| TC-BE-NOTIF-003 exhausted retries -> FAILED | `test_dispatch` (observation only) | **GAP (partial)** — the terminal FAILED branch is observed (EMAIL dispatch with no destination address: status FAILED, retryCount 4, errorMessage recorded), but the 'provider fails 6 times' precondition cannot be arranged from HTTP, and the failure mode used is read out of the provider implementation, not out of a governed document. Recorded as an observation, never asserted. |
| TC-BE-NOTIF-004 disabled channel -> CHANNEL_DISABLED | `test_dispatch` | covered |
| TC-BE-NOTIF-005 bilingual template accepted | `test_template` | covered |
| TC-BE-NOTIF-006 missing bilingual body rejected | `test_template` | covered — **failing on the error code**, see the classification above |
| TC-BE-NOTIF-007 auth delegated to the Security filter | `test_authorization` | covered |
| TC-BE-NOTIF-008 unique codes accepted | `test_template`, `test_channel` | covered |
| TC-BE-NOTIF-009 duplicate code rejected (template + channel) | `test_template`, `test_channel` | covered |
| TC-BE-NOTIF-010 skip inactive recipient | `test_dispatch` (observation only) | **GAP** — `DefaultRecipientStatusReader` is the XM-NOTIF-001 DEFERRED stub and returns true for every id, so no recipient can be inactive from NOTIF's point of view. Observed (a dispatch to an inactive SEC user still creates a log) and left as an observation: asserting it would report a documented deferral as a defect. |
| TC-BE-NOTIF-011 dispatch happy -> {logIds} | `test_dispatch` | covered — **status is 200, not the plan's 202** (see the note below) |
| TC-BE-NOTIF-012 query logs happy + empty page | `test_logs` | covered |
| TC-BE-NOTIF-013 log by id + unknown id 404 | `test_logs` (plus the same ERR-0004 shape on template, channel and lookup) | covered |
| TC-BE-NOTIF-014 templates CRUD + permission | `test_template`, `test_authorization` | covered for CRUD; the **permission half is a GAP** — every NOTIF service's `@PreAuthorize(hasAuthority(PERM_NOTIF_*))` is commented out as SEC-PENDING, so there is no page permission to withhold and no 403 to provoke. Only the platform 401 is verifiable today. |
| TC-BE-NOTIF-015 channels CRUD/enable + toggle affects dispatch | `test_channel`, `test_dispatch` | covered |
| TC-BE-NOTIF-016 lookups happy + SQLi | `test_lookups`, `test_dispatch` | covered |

## Convention divergences from api-verify-config.md §3

- **Error-code format.** The config states `{MOD}-{http}[-{SLUG}]` (SEC emits `SEC-401-INVALID-CREDENTIALS`). NOTIF emits descriptive `SCREAMING_SNAKE` codes with no module/status prefix — `NOTIF_TEMPLATE_NOT_FOUND`, `NOTIF_LOOKUP_KEY_UNKNOWN`. The module's own api-docs document the snake form, so every assertion here uses it; the divergence itself is the finding.
- **Error envelope.** The config states `{code, messageAr, messageEn}`. The live envelope is `{code, message, fieldErrors}` — one already-negotiated message, not a bilingual pair — so no bilingual-message assertion was possible and none was invented.
- **Dispatch status code.** `backend-test-plan.md` TC-BE-NOTIF-011 and the manifest say 202. The service returns 200, and `DispatchService` records the divergence as a known `api_doc_gap` (the shared `Status` taxonomy has no 202). The api-docs are authoritative per api-verify-config.md §1, so 200 is what is asserted.

## Preconditions (stage A0)

_All preconditions satisfied._

## Observations (stage E — not pass/fail)

- **stage E — templateCode one over its documented maxLength(80)** — templateCode at 81 chars (max 80) -> HTTP 400/VALIDATION_ERROR
- **stage E — DELETE idempotency on an already-deactivated template** — second DELETE on an already-deactivated template -> HTTP 204/None
- **stage E — an undocumented sortField on template search** — unknown sortField -> HTTP 200/None
- **stage E — page size one over the documented maximum (200)** — size=201 (PageableBuilder max 200) -> HTTP 200, returned size=200
- **stage E — channelTypeId one over its documented maxLength(20)** — channelTypeId at 21 chars (max 20) -> HTTP 400/VALIDATION_ERROR
- **stage E — required channelTypeId omitted on create** — required channelTypeId omitted -> HTTP 400/VALIDATION_ERROR (NOTIF_CHANNEL_TYPE_REQUIRED is the module's own code for this)
- **stage E — a channelTypeId outside the NOTIF_CHANNEL lookup** — channelTypeId 'ZZ223731' is not an active NOTIF_CHANNEL lookup value -> HTTP 201/None
- **stage E — terminal FAILED branch (TC-BE-NOTIF-003 is not arrangeable as written)** — EMAIL dispatch with no destination address -> log 127 notificationStatusId=FAILED, retryCount=4, errorMessage='missing recipient email address'
- **stage E — dispatch to an inactive recipient (TC-BE-NOTIF-010, XM-NOTIF-001 DEFERRED)** — dispatch to inactive SEC user 15 -> HTTP 200, logIds=[128] (RULE-NOTIF-007 expects none; XM-NOTIF-001 is DEFERRED)
- **stage E — dispatch over a lookup-valid but unconfigured channel** — WHATSAPP: a valid NOTIF_CHANNEL value with no channel config row of its own -> notificationStatusId=CHANNEL_DISABLED
- **stage E — channelHint value that is not an LOV-NOTIF-001 code** — channelHint value outside the NOTIF_CHANNEL lookup -> HTTP 200/None, logIds=[130]
- **stage E — required channelHint present but empty** — required channelHint sent empty -> HTTP 400/VALIDATION_ERROR, logIds=[]
- **stage E — required recipientId omitted** — required recipientId omitted -> HTTP 400/VALIDATION_ERROR
- **stage E — SQLi payload used as a log-search filter value** — SQLi string as a filter value -> HTTP 200, totalElements=6 (parameterised, not executed)
- **stage E — filtering on an undocumented (non-whitelisted) field** — a response field that is not in the filter whitelist -> HTTP 200/None, totalElements=130

## Surviving records

- NotificationLog: 9 row(s) [122, 123, 124, 125, 126, 127, 128, 129, 130] — the ENTITY CRUD CHECKLIST marks NOTIF_LOG a read-only screen with no delete and no deactivate endpoint, so they survive. All carry referenceType='USER_ACCOUNT_223731'. Cleanup SQL: DELETE FROM NOTIF_LOG WHERE REFERENCE_TYPE = 'USER_ACCOUNT_223731';
- NotificationTemplate: [14, 15] — deactivated (soft, isActiveFl=false); NOTIF documents no hard delete. Codes are RUN_ID-suffixed ('%_223731'). Cleanup SQL: DELETE FROM NOTIF_TEMPLATE WHERE TEMPLATE_CODE LIKE '%\_223731';

### Permanent residue (reference tables other modules' behaviour reads)

- NOTIF_CHANNEL_CONFIG rows [10, 11] (channelTypeId ['SMS', 'ZZ223731']) — disabled via the documented DELETE, but the rows persist and CHANNEL_TYPE_ID is UNIQUE, so a later run creating the same channel type gets 409. This table is read by dispatch for every module that sends a notification. Cleanup SQL: DELETE FROM NOTIF_CHANNEL_CONFIG WHERE CHANNEL_TYPE_ID IN ('SMS', 'ZZ223731');

## Privileges

No permission grant was created or revoked by this run, and no grant journal was written. Every NOTIF service method's authority check is currently commented out (`// TODO: SEC-PENDING — re-add @PreAuthorize(hasAuthority(PermissionConstants.PERM_NOTIF_*))`), so no NOTIF endpoint is gated by a PERM_NOTIF_* permission and there was nothing for stage I to grant. No standing privilege was left behind.

