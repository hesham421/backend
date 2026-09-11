# SEC — backend implementation notes

Record of the non-obvious implementation decisions behind `src/main/java/com/erp/sec/`.
Extracted from the class Javadoc on 2026-09-11, when the SEC sources were trimmed to the
GOVERNANCE-RULES.md Javadoc limit (~5 lines, one non-obvious fact per block). The SEC classes
now link here instead of restating the reasoning in every file.

Sources referenced throughout:
`governance/modules/SEC/P2/db-script-sec.md`,
`governance/modules/SEC/packages/backend-execution/{CORE,DATA-DOM,SVC-API}*.md`,
`governance/modules/SEC/P1/srs-sec.md`,
`governance/modules/SEC/execution-state.json`,
`src/main/resources/db/migration/V16__sec_schema.sql`.

---

## 1. AuditableEntity exemptions

`build-create-entity` A.1.1 requires every entity to extend `com.erp.common.domain.AuditableEntity`.
SH.1 exempts a table that has no `created_by / created_at / updated_by / updated_at` columns.
Six of the thirteen SEC tables are in that position; extending `AuditableEntity` there would make
Hibernate write four columns that do not exist.

| Entity | Table | db-script evidence | Own lifecycle / system-set fields |
|---|---|---|---|
| `UserRoleAssignment` | `SEC_USER_ROLE` | §3 CREATE TABLE — five columns only | `assignedBy` / `assignedAt` |
| `RoleModuleGrant` | `SEC_ROLE_MODULE_GRANT` | §3 CREATE TABLE — five columns only | `grantedBy` / `grantedAt` |
| `RoleScreenGrant` | `SEC_ROLE_SCREEN_GRANT` | §3 CREATE TABLE — five columns only | `grantedBy` / `grantedAt` |
| `RoleActionGrant` | `SEC_ROLE_ACTION_GRANT` | §3 CREATE TABLE — five columns only | `grantedBy` / `grantedAt` |
| `ActiveSession` | `SEC_ACTIVE_SESSION` | §3 CREATE TABLE — eight columns only | `startedAt`, `lastActivityAt`, `terminatedAt` / `terminatedBy` |
| `AuditLogEntry` | `SEC_AUDIT_LOG` | §3 CREATE TABLE — no audit columns | `actorUserId`, `occurredAt` |
| `PasswordResetToken` | `SEC_PWD_RESET_TOKEN` | §3 CREATE TABLE — six columns only | `requestedAt`, `expiresAt`, `usedAt` |
| `SignupRequest` | `SEC_SIGNUP_REQUEST` | §3 CREATE TABLE — eight columns only | `submittedAt`, `reviewedBy` / `reviewedAt` |

CORE.md "Audit fields" declares each of those `*By` / `*At` pairs system-set (Field Registry
read-only = Yes), i.e. the per-table equivalent of the audit columns.

Two further consequences:

- **`SignupRequest.submittedAt`** has no authenticated principal to record: API-SEC-002 is a
  pre-authentication, self-service endpoint.
- **`AuditLogEntry`** is the strongest case — SRS A3 ENT-SEC-011 states it outright: "this entity
  has no createdBy/updatedBy — it IS the audit record; actorUserId + occurredAt serve that purpose".

### Tables with no `is_active_fl`

`build-create-entity` A.1.18's `activate()` / `deactivate()` pair applies only to a table that has
an `IS_ACTIVE_FL` column. It therefore does **not** apply to `UserRoleAssignment`, the three grant
tables, `ActiveSession`, `AuditLogEntry`, `PasswordResetToken` or `SignupRequest`. Those rows are
created and deleted, or carry a different lifecycle column; inventing an `isActiveFl` field there
would contradict the schema. The equivalents actually implemented are:

- `ActiveSession.terminate(..)` — lifecycle is `terminatedAt IS NULL` (active) / `IS NOT NULL`
  (terminated).
- `PasswordResetToken.markUsed()` — lifecycle is `requestedAt` / `expiresAt` / `usedAt`.
- `SignupRequest.approve(..)` / `reject(..)` — lifecycle is `statusCode` over SIGNUP_STATUS.

All three are pure field mutation; the guard is always a Domain decision taken first.

---

## 2. Append-only enforcement for `AuditLogEntry` (POL-SEC-009)

DATA-DOM-TRANSACTIONAL.md ENT-SEC-011 says the immutability "is enforced by omission — no
UPDATE/DELETE mapping exists on this repository at all, not by a DB trigger". What that means in
code:

- `AuditLogEntry` declares **no Lombok `@Setter`** (unlike every other SEC entity), no mutator, and
  no state transition. A row is built once through `@SuperBuilder` and never touched again.
- It has **no Domain companion**: an append-only record makes no "is this operation allowed?"
  decision (A.0.1 Decision Test).
- `AuditLogEntryRepository` declares no update and no delete method. **Java cannot remove an
  inherited method**, so `JpaRepository`'s `delete`, `deleteById`, `deleteAll*` and the update half
  of `save` remain visible on the type — *no SEC service may call any of them*. This is a
  convention, not a compiler-enforced constraint, and is the one place where POL-SEC-009 could be
  violated without a build failure.
  **Forward action — deliberately deferred, not forgotten:** the sanctioned fix is a build-time
  ArchUnit guard failing if any SEC service calls a `delete*` method on `AuditLogEntryRepository`
  — the same pattern `build-create-service` prescribes for its internal trusted-caller gate. It is
  NOT written here because `governance/GOVERNANCE-RULES.md` forbids creating any file under
  `src/test/java/` outside an explicit request or the gated `execute-backend-test` phase. Add it
  when that phase runs; until then this bullet is the only thing standing between POL-SEC-009 and
  a silent violation.
- The only sanctioned write is an insert of a fresh instance (`save(newEntity)`), performed inline
  by each other service's orchestration step, with shape
  `{eventTypeCode, actorUserId, occurredAt=now(), targetRef, detailsAr, detailsEn, ipAddress}`.
- Search (QR-SEC-023, API-SEC-023) and export (QR-SEC-024, API-SEC-024) are both FIND_BY_CRITERIA
  and use the inherited `findAll(Specification, Pageable)` / `findAll(Specification, Sort)` driven
  by the shared `SpecBuilder` / `PageableBuilder` — no bespoke query is declared.

---

## 3. RULE-SEC-005 — the conflicting-pair finding (ABSENT, resolved without inventing a contract)

RULE-SEC-005 ("the system shall prevent assigning a user, by any combination of roles, both
actions of a module-declared conflicting pair") and QR-SEC-031 both hinge on two actions being
*declared* a conflicting pair by their owning module. **No SEC v1 artifact says where that
declaration lives.** Checked and empty: srs-sec.md A3 entities, A6 lookups, A7 defaults/state
machines, A8 cross-module, Part B screens B3/B5, §7.1 permissions, db-script-sec.md's 104 DBF
across 13 tables, and API-SEC-020's action-registration request contract. There is no ENT, no DBF,
no table and no API for a conflict register.

Supporting facts:

- REQ-SEC-020 is `Pattern : optional` — "Where a consumer module declares two of its actions as
  conflicting" — an EARS precondition that SEC v1 never establishes.
- The platform's only real conflicting pair is **FIN-owned and FIN-enforced**, not SEC-enforced:
  `governance/modules/FIN/P3_1/backend-execution-plan-fin.md:1061-1066` separates
  `PERM_FIN_PERIODS_CLOSE_APPROVE` from `PERM_FIN_JOURNAL_ENTRIES_CREATE` "by platform convention
  (an administrative guideline enforced by role design, not a database constraint)", with FIN's own
  service layer checking at hard-close / year-end-close time that no single user holds both, per
  role union — reading the two roles' user sets through SEC's role/grant read APIs (:383-386).
- RULE-FIN-015 (`srs-fin.md:1012`) is scoped to ENT-FIN-008 and carries FIN's own error code
  `FIN-403-SOD-VIOLATION` (403). No FIN-owned P1/P2/P3_1 artifact ever routes through
  `SEC-409-SOD-CONFLICT`.

**Consequence for the implementation.** API-SEC-008 and API-SEC-017 are implemented exactly as
their Orchestration lines read, with the RULE-SEC-005 step driven by a conflicting-counterpart set
that is **empty in SEC v1** (a private service-side resolver returning empty). The Domain guards
(`UserRoleAssignmentDomain`, `RoleActionGrantDomain`) and both QR-SEC-031 repository methods
therefore keep a real caller, and A.2.9 still holds at ALIGN-BE. No table, column, route, error
code or rule was invented.

**Open, human-only question** (a P1/P2 amendment, not an execution decision): whether SEC v1 should
own a conflicting-pair register at all, rather than leaving REQ-SEC-020's precondition permanently
unestablished. Also recorded in `governance/modules/SEC/execution-state.json` → `api_doc_gaps[]`.

---

## 4. Convention deviations

### 4.1 SEQUENCE primary keys, not `GENERATED ALWAYS AS IDENTITY`

`db-script-sec.md` §3 declares every PK as `GENERATED ALWAYS AS IDENTITY` and its BLOCK 1
("sequences") as "none". The repo's entity contract mandates the opposite: `build-create-entity`
A.1.3 / A.1.4 require `GenerationType.SEQUENCE` + `@SequenceGenerator`, and
`GenerationType.IDENTITY` is an automatic rejection trigger for `gov-enforce-backend-contract`.
Every other module here (CU / NOTIF / FILE, and the old SEC `V2`) uses explicit `SEQ_<TABLE>`
sequences.

Decided at DATA-DOM-MASTER and recorded in the `V16__sec_schema.sql` header. PK columns are plain
`BIGINT NOT NULL`, fed by the 13 `SEQ_SEC_*` sequences created in that migration's BLOCK 1
(`allocationSize = 1`, matching `CACHE 1` on the DB side). Every table / column / constraint /
index name is otherwise verbatim from the db-script.

### 4.2 Native `BOOLEAN` columns mapped to plain `Boolean`, no converter

`db-script-sec.md` gives `IS_ACTIVE_FL` as a native Postgres `BOOLEAN NOT NULL DEFAULT TRUE`
(DBF-SEC-009 User, -020 Role, -034 ModuleRegistry, -044 ScreenRegistry, -055 ActionRegistry). The
shared converters `com.erp.common.converter.BooleanNumberConverter` and `BooleanCharYNConverter`
exist for **numeric** and **CHAR(1)** flag columns respectively and would break a native `BOOLEAN`
column. The five SEC flags are therefore mapped as a plain `Boolean` field with no
`@Convert`. The rationale is stated once on `User.isActiveFl`; the other four carry
"no converter (see User)".

### 4.3 Unique-constraint naming

The `@UniqueConstraint` names in the SEC entities are verbatim from `db-script-sec.md` §5b and
match `build-create-entity` A.1.13's `UQ_<TABLE>_<DESC>` form — `UQ_SEC_USER_USERNAME`,
`UQ_SEC_ROLE_CODE`, `UQ_SEC_USER_ROLE_USER_ROLE`, `UQ_SEC_ROLE_{MODULE,SCREEN,ACTION}_GRANT_*`,
and so on. The only liberty taken, inherited from the db-script rather than introduced here, is an
abbreviated `<DESC>` on two of them: `UQ_SEC_ACTION_REG_PERM` covers `permission_code` and
`UQ_SEC_SCREEN_REG_PAGE` covers `page_code`. Entity, db-script and `V16__sec_schema.sql` all agree;
do not "normalise" these names — the constraint identifier is what a Postgres integrity-violation
message reports and what the SVC-API duplication mapping keys on.

### 4.4 `SecErrorCodes` constant naming

Unlike `NotifErrorCodes` / `CuErrorCodes`, where the constant name equals its value, the SEC
catalog codes contain hyphens — legal on the wire, illegal in a Java identifier. The constant name
is therefore the code with `-` replaced by `_` (`SEC_409_USER_DUP` = `"SEC-409-USER-DUP"`). The
literal hyphenated strings are what `governance/modules/SEC/test_gen/backend-test-plan-sec.md`
asserts on, so they must not be normalised. One format is used consistently across the whole module
(gov-enforce-error-handling CHECK 3), never mixed with the descriptive `<ENTITY>_<SCENARIO>` form.
Every PLATFORM-STD row is covered by ADR-SEC-002 (a single umbrella decision, no per-row ADR).

---

## 5. Layer-placement decisions worth remembering

- **`RoleModuleGrantDomain` does not own RULE-SEC-003.** The cascade revoke of dependent screen and
  action grants is an *action* taken on delete, not a permit/deny decision;
  DATA-DOM-TRANSACTIONAL.md ENT-SEC-007 puts its "owner layer: service". It is SVC-API
  orchestration in API-SEC-015. What the Domain object does own is API-SEC-014's duplication guard
  (`UQ_SEC_ROLE_MODULE_GRANT_ROLE_MODULE` → `SEC-409-GRANT-DUP`). API-SEC-014's other half — "role
  and module must be active" — has no catalog code of its own; its Errors line offers only
  `SEC-404-ROLE` / `SEC-404-MODULE`, so an inactive role or module is resolved by the service as a
  load-time not-found, and no new error code was invented for it.

- **`ActionRegistryDomain` does not derive `permissionCode`.** `PERM_<pageCode>_<actionCode>` is a
  pure string derivation, not a decision about whether an operation is permitted, so it fails the
  A.0.1 Decision Test. API-SEC-020's Orchestration line places it in the service ("resolve screen
  by pageCode → derive permissionCode → validate uniqueness"), which is also the only layer holding
  the request's `pageCode`. The service derives it and passes the finished code in; the entity's
  `@PrePersist` still owns case normalization (A.1.17).

- **`UserDomain` and `SignupRequestDomain` exist despite "DOMAIN RULES: none scoped alone".**
  That line in DATA-DOM-MASTER.md / DATA-DOM-TRANSACTIONAL.md refers to the SRS §A5 `RULE-SEC-*`
  register only. `gov-enforce-backend-contract` LAYER 0 is unconditional and names state-transition
  checks explicitly (A.0.1), so the reactivation guard (API-SEC-010) and the PENDING-only signup
  transition (API-SEC-011) each require a Domain object. `ActiveSessionDomain` exists for the same
  reason (API-SEC-026's `terminatedAt IS NULL` guard).

- **QR-SEC-038 is a fetch, not an EXISTS.** The Query Reference Catalog states it as
  "EXISTS — token unexpired and unused", but that predicate *is* RULE-SEC-006's verdict, and
  `build-create-repository` forbids a query predicate that decides a business outcome. The row is
  fetched by `PasswordResetTokenRepository.findByTokenHash` and judged by
  `PasswordResetTokenDomain.assertUsable(now)` — which is what API-SEC-004 needs anyway, since it
  must load the token to reach its user and stamp `usedAt`. The comparison clock is passed in
  rather than read from `Instant.now()` inside the Domain object, keeping the decision
  deterministic and testable. An unknown token and an expired one both report
  `SEC-409-RESET-TOKEN-INVALID`, so a caller cannot distinguish them.

- **`ActiveSessionDomain` has `from(..)` but no `create(..)`.** A session row is minted by the login
  flow itself (API-SEC-001) with no permit/deny decision attached.

- **Both QR-SEC-031 shapes live on `RoleActionGrantRepository`.** The catalog entry is shared
  between API-SEC-008 (does this *user* already hold the action, through any assigned role?) and
  API-SEC-017 (does *any user holding this role* already hold it?), and is catalogued against
  ENT-SEC-009, so it is declared once there rather than duplicated on
  `UserRoleAssignmentRepository`.

- **`RoleActionGrantDomain.GATEWAY_ACTION_CODE = "VIEW"`** is passed into the QR-SEC-030 query as a
  parameter rather than hard-coded in JPQL, so the query carries no business constant. Its source
  is `profile.conventions.security_model.gateway_action`, cited by RULE-SEC-007.

---

## 6. Plan-vs-schema discrepancies resolved during execution

All three are also recorded in `governance/modules/SEC/execution-state.json` → `api_doc_gaps[]`.

1. **`RoleModuleGrant.grantedAt` (DBF-SEC-064).** DATA-DOM-TRANSACTIONAL.md's ENT-SEC-007 FIELDS
   table spells the column `grant_at`; `db-script-sec.md` says `granted_at` in all three places
   (§1 registry row, §3 CREATE TABLE, BLOCK 4 COMMENT ON), matching the sibling grant tables.
   Resolved against the schema as ground truth — entity and `V16__sec_schema.sql` both use
   `granted_at`; the plan's `grant_at` is a typo.
2. **SEC error-code format.** CORE.md declares the runtime code format as `SEC-<3-digit-sequence>`
   (e.g. `SEC-001`), but no §Error Catalog row uses that shape — every row is
   `SEC-<HTTP>-<SCENARIO>`, and `test_gen/backend-test-plan-sec.md` asserts on those literal
   strings. The catalog's literal codes are authoritative and are the constant values in
   `SecErrorCodes`; CORE.md's prose is a stale generalisation, not a competing contract.
3. **RULE-SEC-005's conflicting-pair source** — see §3 above.

---

## 7. SVC-API-SEARCH — the GET-transport deviation and its consequences

`build-create-controller` A.6.6 mandates `POST /search` with a `@RequestBody`, and A.3.9–A.3.11
mandate a `<Entity>SearchRequest extends BaseSearchContractRequest`. **SEC's seven read-only APIs
deviate deliberately**: every `Endpoint :` line in `SVC-API-SEARCH.md` specifies `GET`, and
`test_gen/backend-test-plan-sec.md` asserts the GET form literally (TC-SEC-021 `GET /api/v1/sec/menu`,
TC-SEC-022/023 `GET /api/v1/sec/dashboard`, TC-SEC-025 `GET /api/v1/sec/audit-log`, TC-SEC-027
`GET /api/v1/sec/sessions`). A `POST /search` implementation would fail those tests. CU/NOTIF/FILE
keep the `POST /search` form and were not touched.

Consequences, applied consistently:

- The seven endpoints are `@GetMapping` with `@RequestParam(required = false)` values. No
  `*SearchRequest` body DTO exists anywhere in SEC, so A.3.9/A.3.10/A.3.11 are **not applicable**
  rather than violated.
- The shared search layer is still mandatory. Each service builds a
  `com.erp.common.search.SearchRequest` programmatically and drives `SpecBuilder.build(..)` +
  `PageableBuilder.from(..)` with a `SetAllowedFields` whitelist, exactly as a body-driven search
  would. `SecSearchSupport` (package-private, `com.erp.sec.service`) is the stand-in for
  `BaseSearchContractRequest.toCommonSearchRequest()` that the missing DTO would have provided.

### `SEC-400-INVALID-SORT` is raised by SEC, because the shared layer does not signal

`PageableBuilder.from(..)` **silently drops** a sort field that is not in the whitelist (it returns
an unsorted `PageRequest`), and `SpecBuilder.build(..)` silently skips a filter on a non-whitelisted
field. Neither throws. `SEC-400-INVALID-SORT` already exists in `SecErrorCodes` and both bundles, so
`SecSearchSupport.commonRequest(..)` checks the incoming sort field against the same
`ALLOWED_SORT_FIELDS` set and throws `LocalizedException(VALIDATION_ERROR, SEC_400_INVALID_SORT, field)`
before `PageableBuilder` is reached. No shared class was modified and no error code was added. This
is structural input validation, not a business rule (`build-create-dto` scope note), so it is not an
A.5.18 Domain-delegation case.

### `ALLOWED_SORT_FIELDS` per API, from each screen's SRS B2

CORE.md's Search contract fixes the whitelist as "exactly the columns listed as filters in each
screen's SRS B2":

| API | Screen | B2 filter line | `ALLOWED_SORT_FIELDS` |
|---|---|---|---|
| API-SEC-005 | SEC_USERS | username/email, fullName, statusCode | `username, email, fullNameAr, fullNameEn, statusCode` |
| API-SEC-012 | SEC_ROLES | role code/name, active flag | `code, nameAr, nameEn, isActiveFl` |
| API-SEC-021 | SEC_MODULE_REGISTRY | module code, screen page code | `code` only — see below |
| API-SEC-023 | SEC_AUDIT_LOG | eventTypeCode, actorUserId, date range | `eventTypeCode, actor, occurredAt` |
| API-SEC-025 | SEC_SESSIONS | user, IP address | `user, ipAddress` |

Two B2 entries expand to two columns each (`fullName` → `fullNameAr`/`fullNameEn`, role `name` →
`nameAr`/`nameEn`) because the module stores both languages; the request parameter stays singular
and matches either column. API-SEC-021's paged root is the **module**, so `pageCode` — a column of
the child screen — cannot be a sort field there; it stays a filter only.

### The three predicates the shared layer cannot express

`SearchOperator` has no `OR` and no `IS NULL`, so three predicates are written directly and
`.and(..)`-ed onto the `SpecBuilder` result rather than replacing it:

- `UserService.fullNameMatches` / `RoleService.nameMatches` — the bilingual OR above.
- `SessionService.notTerminated` — `terminatedAt IS NULL`, applied unconditionally as a
  server-side invariant (API-SEC-025 Validations); a client cannot switch it off or reach a
  terminated session through the endpoint.
- `RegistryService.hasScreenWithPageCode` — an `EXISTS` subquery on the child screen (A.5.17's
  explicit-join case).

### One effective-grant read path, two consumers

`MenuService` owns the single read path CORE.md's REQ-SEC-033 paragraph names: `effective()` is
API-SEC-027, and `effectivePermissionCodes()` is the same traversal projected to
`ActionRegistry.permissionCode`, consumed by API-SEC-022's per-widget filter (REQ-SEC-023, whose
Entities line includes ENT-SEC-009 — so a widget's "screen VIEW" is an **action** grant, not a
screen grant). Both are gated `@PreAuthorize("isAuthenticated()")` as a plain string literal:
SRS B4 gives SCR-REQ-SEC-010 no page code, so there is no constant to reference — its content, not
a permission on itself, is the boundary.

Both queries exclude an **inactive role** (`SCR-REQ-SEC-005 B4` makes deactivation the role's only
DELETE, so an inactive role is a withdrawn one) and inactive registry rows (`SCR-REQ-SEC-006 B3`
calls deactivation "deactivating a stale row"). API-SEC-021 filters screens and actions to active
per its own Response line, but not modules, which that line does not scope.

**Ordering dependency for SEC-BE.** `com.erp.main.config.SecurityConfig` is still `permitAll` with
method security disabled, and `SecurityContextHelper.getCurrentUsername()` falls back to the literal
`"system"`. With no principal, `GET /api/v1/sec/menu` answers `200` with `data: []` and
`GET /api/v1/sec/dashboard` answers `200` with every widget omitted, because `"system"` matches no
`SEC_USER` row. Both become correct the moment SEC-BE installs the JWT validating filter and the
REQ-SEC-033 gateway; neither was worked around here, and `SecurityConfig` was not modified.

### REQ-SEC-023 is implemented by omission, not by zeroing

`DashboardResponse` carries `@JsonInclude(JsonInclude.Include.NON_NULL)` and every sub-figure is a
nullable object. A widget the caller has no source-screen VIEW for is simply never set, so it
disappears from the JSON rather than serializing as `null` or `0` — which is what AC-SEC-023 and
TC-SEC-023 assert. The six widgets map to four source screens: `usersOverview` and
`onboardingFunnel` → SEC_USERS (its B3 "Pending sign-ups" tab owns the SignupRequest rows),
`failedLogins24h` and `recentActivity` → SEC_AUDIT_LOG, `activeSessions` → SEC_SESSIONS,
`rolesPermissionsSummary` → SEC_ROLES. Nothing is cached (`gov-enforce-caching-rules`' approved
register is empty, and REQ-SEC-022 requires every figure computed at that moment).

The three figures API-SEC-022 names but never defines — `recentActivity`'s N,
`privilegedRoleCount` and `stalledCount` — are recorded in `execution-state.json` → `api_doc_gaps[]`
and implemented as named constants in `DashboardService`, not as configuration properties.

---

## 8. SVC-API-SEARCH — the GET transport reversed to `POST /search` (2026-09-11)

§7's deviation was accepted too quickly and has been reversed for the five criteria-driven
searches. It rested on the claim that the governed test plan pins the GET form; in fact
`test_gen/backend-test-plan-sec.md` contains no test at all for API-SEC-005, API-SEC-012 or
API-SEC-021, and the only two it does cover (TC-SEC-025 → API-SEC-023, TC-SEC-027 → API-SEC-025)
assert nothing about the verb beyond their own `Exercises` line.

### The split: criteria-driven searches move, parameterless resources stay

| Now `POST` + `<Entity>SearchRequest` | Still `GET` |
|---|---|
| API-SEC-005 `POST /api/v1/sec/users/search` | API-SEC-022 `GET /api/v1/sec/dashboard` — no criteria |
| API-SEC-012 `POST /api/v1/sec/roles/search` | API-SEC-027 `GET /api/v1/sec/menu` — no criteria |
| API-SEC-021 `POST /api/v1/sec/registry/search` | API-SEC-024 `GET /api/v1/sec/audit-log/export` — file download |
| API-SEC-023 `POST /api/v1/sec/audit-log/search` | |
| API-SEC-025 `POST /api/v1/sec/sessions/search` | |

Rationale: the GET form bypassed `BaseSearchContractRequest`, the platform's uniform search
contract. Each filter's field *and* operator were fixed in the service signature, so a client could
not express `statusCode != DISABLED`, adding a filter meant changing controller, service and plan
together, and Arabic filter text went through URL encoding rather than a JSON body. CU, NOTIF and
FILE already used `POST /search`; SEC was the only module out of line. A.3.9, A.3.10, A.3.11 and
A.6.6 now pass instead of being recorded deviations.

### The three behaviours the move had to preserve

- **Bilingual OR** — `fullName` (API-SEC-005) and `name` (API-SEC-012) still match either language
  column. The client sends them as ordinary filters; the DTO lifts them out of the generic set
  (`toCommonSearchRequest(Set.of(..))`) and the service keeps the `.and(fullNameMatches/nameMatches)`
  predicate, because `SearchOperator` still has no OR.
- **`terminatedAt IS NULL`** (API-SEC-025) is still applied unconditionally, outside the
  client-supplied filter set, and `terminatedAt` is in neither whitelist — so no request body can
  switch it off or reach a terminated session.
- **`pageCode`** (API-SEC-021) is still an EXISTS subquery on the child screen row, and
  `ALLOWED_SORT_FIELDS` is still `{code}`.

### Consequences inside the services

`SecSearchSupport` survives with a narrower job: the SEC-400-INVALID-SORT guard (`PageableBuilder`
still silently drops an unknown sort field, so the check must precede it), the `addFilter` helper
that API-SEC-024's export still needs, and an `Instant` value converter, because a JSON body carries
`occurredAt` as a string where a query parameter arrived already typed. An association filter
(`ActiveSession.user`, `AuditLogEntry.actor`) cannot take a raw id through `SpecBuilder`, so those
two searches keep the whole filter set as `ALLOWED_SORT_FIELDS` but expose a narrower
`ALLOWED_FILTER_FIELDS` to client-supplied filters and resolve `userId` / `actorUserId` to a
reference themselves — the A.3.10/A.3.11 parent-id-extractor shape.

One behaviour did change: API-SEC-021's `moduleCode` was uppercased server-side before comparison
and no longer is, since the client now names the field and operator itself. An `EQUALS` filter on
`code` matches the stored (uppercase) form exactly; `LIKE` is case-insensitive in `SpecBuilder` and
covers the lenient case.

### Documents realigned the same day

`P3_1/backend-execution-plan-sec.md` (base plan — API table rows, `Endpoint :` and `Request :`
lines, API contract summary), `packages/backend-execution/SVC-API/SVC-API-SEARCH.md` (the same five
`Endpoint :` and `Request :` lines) and `test_gen/backend-test-plan-sec.md` (TC-SEC-025 and
TC-SEC-027 verb/endpoint only — no status code, error code or ar/en message text touched). The P1
SRS B5 tables still show the GET form for these five operations; they were left alone because they
are requirements text, not the implementation contract.

---

## 9. SEC-BE — authentication and authorization switched ON (2026-09-11)

Until this phase `com.erp.main.config.SecurityConfig` carried `anyRequest().permitAll()` and no
`@EnableMethodSecurity`, so all 27 `@PreAuthorize` annotations in `com.erp.sec` were inert and every
request was permitted. That is no longer true. SEC-BE was executed ahead of DOC / INT-C / INT-R at
the user's explicit instruction; those three remain PENDING and `current_phase` was not advanced.

### What the app now enforces

| Situation | Result |
|---|---|
| No bearer token on any path but the four public ones | `401` + `SEC-401-INVALID-CREDENTIALS` |
| Token whose signature or expiry fails | same `401` (the context stays anonymous) |
| Token whose session row is missing or `terminatedAt IS NOT NULL` | same `401` (REQ-SEC-028) |
| Token of a user who is not ACTIVE / not `isActiveFl` | same `401` |
| Authenticated caller missing the endpoint's permission | `403` + `SEC-403-FORBIDDEN`, both catalog messages |

`SEC-401-INVALID-CREDENTIALS` is reused for the missing-token case because the §Error Catalog has no
"no credential presented" row and `GOVERNANCE-RULES.md` forbids inventing one. Its PLATFORM-STD
scope (ADR-SEC-002, "wrong/unknown credentials") covers a request that presents no usable credential.

### The authority model

An authority string **is** an `ActionRegistry.permissionCode` — `PERM_<PAGE_CODE>_<ACTION_CODE>`,
the same value `RegistryService` derives and `PermissionConstants` declares. `JwtAuthenticationFilter`
resolves them through `MenuService.effectivePermissionCodes()`, the single effective-grant read path
CORE.md's REQ-SEC-033 paragraph names — no second resolution query was written. The filter installs a
provisional zero-authority authentication first, purely so that method's own
`@PreAuthorize("isAuthenticated()")` can be satisfied while the real authority set is being built; it
is replaced before the chain proceeds and no request is ever handled under it.

That query joins role → action grant only. It does not re-check the module and screen grants
REQ-SEC-033 also names, and does not need to: RULE-SEC-001 and RULE-SEC-002 are enforced at grant
time (API-SEC-016 / API-SEC-017), so an action grant already implies its screen grant and its module
grant. The seed satisfies the same chain by construction.

### RULE-SEC-007's placement

The gateway is applied **once, while the authority set is built**, not per endpoint: a non-VIEW
permission is dropped unless the same screen's `PERM_<PAGE_CODE>_VIEW` is also held. The screen is
recovered from the code's own shape (everything between `PERM_` and the last `_`), mirroring
`RegistryService.PERMISSION_CODE_FORMAT` rather than issuing another query — which is why an action
code containing an underscore would not be gateable, and none exists. A code that does not carry the
registry shape at all has no screen to gate on and is passed through unchanged.

Proven at runtime on a throwaway database: with `PERM_SEC_USERS_VIEW` and `PERM_SEC_USERS_UPDATE`
both granted, `PATCH /api/v1/sec/users/1` reached the business layer (`409`
`SEC-409-INVALID-TRANSITION`); after revoking **only** the VIEW grant, the identical call answered
`403` `SEC-403-FORBIDDEN` while `PERM_SEC_USERS_UPDATE` was still present in `SEC_ROLE_ACTION_GRANT`.

### Why a denial carries the catalog code

`GlobalExceptionHandler`'s `AccessDeniedException` handler emits the generic `ACCESS_DENIED`, and it
is shared infrastructure this phase must not modify; a per-module `@ControllerAdvice` is forbidden
(CU.7). `SecForbiddenAdvisor` closes the gap instead: one `DefaultPointcutAdvisor`, bean-role
`ROLE_INFRASTRUCTURE`, ordered `HIGHEST_PRECEDENCE` so it sits outside the `@PreAuthorize`
interceptor, matching only `com.erp.sec.service.*`. It catches the denial and re-raises
`LocalizedException(Status.FORBIDDEN, SecErrorCodes.SEC_403_FORBIDDEN)`, which the shared handler
already maps correctly. This is CORE.md's "single method-level interceptor … declared once here,
never re-implemented per endpoint" — nothing was added per endpoint. Denials raised inside the filter
chain never reach the dispatcher, so `SecSecurityErrorHandler` writes the same envelope by hand for
those (Jackson's converters are not available that early).

### The seed and the bootstrap credential

`V17__sec_security_seed.sql` — V16 creates the 13 SEC tables and zero rows, so switching security on
without it would lock every caller out. It seeds 1 module, 9 screens, 13 actions, 1 role
(`SYS_ADMIN`), 1 module grant, 6 screen grants, 13 action grants, 1 user and 1 role assignment.

- The three public screens (`SEC_LOGIN`, `SEC_SIGNUP`, `SEC_PWD_RESET`) are registered but carry no
  action row and no grant — the matrix marks them public with no permission.
- Both cells the matrix marks "reserved" are seeded, because the matrix names a permission code for
  each. `PERM_SEC_ROLES_DELETE` is genuinely dormant (no v1 delete-role endpoint; deactivation is
  UPDATE) and is the one seeded code with no `PermissionConstants` constant.
  `PERM_SEC_MODULE_REGISTRY_UPDATE` is only "reserved" for row deactivation — `RegistryService`
  gates API-SEC-018/019/020 on it today, so omitting it would have made those three unreachable.
- `V10__sec_bootstrap_admin_user.sql` seeds into `SEC_USER_ACCOUNT`, which `V14` dropped; it is dead
  for this schema and is never edited (Flyway checksums). V17 is the forward fix, and reuses V10's
  BCrypt hash verbatim so the documented **dev-only `admin` / `admin`** credential keeps working.
  It is a published, well-known secret — change or disable it before any real environment.

### The internal-caller pattern (api_doc_gaps #5)

`com.erp.sec.security.InternalCallerContext` installs a synthetic authentication carrying one
authority, `INTERNAL_TRUSTED_CALLER`, for the duration of one in-process call and restores the
previous context in a `finally`. `PasswordResetService` wraps only the
`NotificationDispatchApi.dispatch(..)` call site in it, so the anonymous API-SEC-003 can satisfy
NOTIF's `@PreAuthorize("isAuthenticated()")` without NOTIF being touched. No request can obtain the
authority: `JwtAuthenticationFilter` is the only authentication entry point and strips that exact
string from every authority set it builds, and the chain configures no other mechanism that grants
authorities. It lives in `com.erp.sec.security` rather than `com.erp.common.util` because SEC is its
only consumer and `com.erp.common.**` is shared foundation this phase was scoped not to change.

The prescribed build-time ArchUnit guard (no `@RestController`/`@Controller` reaches the gated
method) is **deferred for the same reason as §2's append-only guard**: `GOVERNANCE-RULES.md` forbids
creating anything under `src/test/java/` outside the gated `execute-backend-test` phase, and this
repo has no `src/test` tree at all. Add both together when that phase runs.

The transaction half of gap #5 is narrowed, not closed — see `execution-state.json` for the detail:
NOTIF's `dispatch` is `@Transactional` REQUIRED and joins SEC's transaction, so a failure raised
inside it now marks the shared transaction rollback-only and REQ-SEC-006's generic 200 becomes a 500.
The escape is a NOTIF-owned contract change, out of scope here.

### Blast radius outside SEC

`@EnableMethodSecurity` activates every `@PreAuthorize` in the deployable. Four live ones exist
outside `com.erp.sec`, all `isAuthenticated()` and all satisfied by any authenticated caller:
`DispatchService.dispatch`, `NotificationLookupService.get`, `FileLookupService.get`,
`FileService.retrieve`. Everything else in CU / NOTIF / FILE sits behind `// TODO: SEC-PENDING`
comments and stays inert — nothing was added or removed in those modules. The practical change for
them is the chain's own `anyRequest().authenticated()`: their endpoints now answer `401` without a
token where they previously answered `200`.
