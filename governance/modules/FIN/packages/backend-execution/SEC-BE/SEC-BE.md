<!-- source: PHASE:SEC-BE -->
<!-- traces: REQ-FIN-038, REQ-FIN-044 -->
<!-- PHASE:SEC-BE:START traces=REQ-FIN-038,REQ-FIN-044 -->
## PHASE 7 — SEC-BE (security, backend half)

| Screen (page code) | VIEW | CREATE | UPDATE | DELETE | Custom |
|---|---|---|---|---|---|
| FIN_ACCOUNTS | ✓ (API-FIN-001) | ✓ (API-FIN-002) | ✓ (API-FIN-003, and API-FIN-004 deactivate) | — | — |
| FIN_DIMENSIONS | ✓ (API-FIN-005,008) | ✓ (API-FIN-006,007) | ✓ (API-FIN-035, deactivate a dimension VALUE — `PERM_FIN_DIMENSIONS_UPDATE`, added by V28) | — | — |
| FIN_RULES | ✓ (API-FIN-009) | ✓ (API-FIN-010) | ✓ (API-FIN-011, add line; and API-FIN-034, deactivate rule) | — | — |
| FIN_RECURRING_TEMPLATES | ✓ (API-FIN-012) | ✓ (API-FIN-013) | ✓ (API-FIN-014, run) | — | — |
| FIN_ALLOCATION_RULES | ✓ (API-FIN-015) | ✓ (API-FIN-016) | ✓ (API-FIN-017, run) | — | — |
| FIN_JOURNAL_ENTRIES | ✓ (API-FIN-018,022) | ✓ (API-FIN-019,020) | — | — | Reverse (`PERM_FIN_JOURNAL_ENTRIES_REVERSE`, API-FIN-021) |
| FIN_PERIODS | ✓ (API-FIN-033, search periods — and still the gateway, see below) | ✓ (API-FIN-023, year) | ✓ (API-FIN-024,025) | — | Close-approve (`PERM_FIN_PERIODS_CLOSE_APPROVE`, API-FIN-026,027 — RULE-FIN-015 SoD) |
| FIN_ACCOUNT_LEDGER | ✓ (API-FIN-028) | — | — | — | — |
| FIN_TRIAL_BALANCE | ✓ (API-FIN-029) | — | — | — | — |
| FIN_BALANCE_SHEET | ✓ (API-FIN-030) | — | — | — | — |
| FIN_INCOME_STATEMENT | ✓ (API-FIN-031) | — | — | — | — |
| FIN_DIMENSION_REPORTS | ✓ (API-FIN-032) | — | — | — | — |

**DELETE column — deliberately empty everywhere.** FIN exposes no `DELETE` endpoint at all.
Deactivation is `PUT /{id}/deactivate` gated by the screen's UPDATE permission (see
`AccountService.deactivate`, `@PreAuthorize` on `PERM_FIN_ACCOUNTS_UPDATE`), exactly as
MDL_LOOKUPS models it, and neither V24 nor V28 seeds a `PERM_FIN_*_DELETE` row for any FIN screen.
The FIN_ACCOUNTS row above read "✓ deactivate (API-FIN-004)" under DELETE until ALIGN-BE moved it
to UPDATE; inventing DELETE rows here would create permanently-unreferenced registry data. The two
later deactivates follow the same modelling: API-FIN-034 (event-type rule) under FIN_RULES/UPDATE
and API-FIN-035 (dimension value) under FIN_DIMENSIONS/UPDATE. There are three deactivate
endpoints in FIN and no `activate` anywhere — API-FIN-034 and API-FIN-035 each deliberately omit a
counterpart, following the delivered `AccountService.deactivate` precedent.

**FIN_DIMENSIONS / UPDATE — the one cell V24 did not seed.** `PERM_FIN_DIMENSIONS_UPDATE` is
declared in `PermissionConstants` and registered by `V28__fin_dimensions_update_action.sql`, which
also grants it explicitly to `SYS_ADMIN`. The explicit grant is not optional: V25 grants SYS_ADMIN
its FIN actions with a `SELECT` over `SEC_ACTION_REG` and has already run everywhere, so Flyway
will never re-evaluate it against a row inserted later — registering without granting is exactly
the MDL failure V19/V21 had to repair. Tiers 1 and 2 need nothing new (V25 already grants SYS_ADMIN
the FIN module row and every FIN screen, FIN_DIMENSIONS included), and the RULE-SEC-007 gateway
holds because V24/V25 already registered and granted `PERM_FIN_DIMENSIONS_VIEW` on the same screen.
`FIN_CLOSE_APPROVER` (V27) deliberately gets nothing from V28 — its grants are scoped to
FIN_PERIODS and two permission codes.

**FIN_RULES / UPDATE — one permission, two endpoints.** API-FIN-034 reuses the pre-existing
`PERM_FIN_RULES_UPDATE` that V24 already seeds and V25 already granted, so it needed no new
constant, no new error code and no migration.

**FIN_PERIODS / VIEW — a gateway that now also has an endpoint.** `PERM_FIN_PERIODS_VIEW` is a
real V24 action row and is load-bearing: `MenuService.effectiveAuthorityCodes()` keeps a granted
permission only if the same screen also carries a granted gateway (VIEW) action, so V27's
FIN_CLOSE_APPROVER role must hold it for `PERM_FIN_PERIODS_CLOSE_APPROVE` to survive into the
caller's authorities. That much is unchanged.

What HAS changed: the row is no longer constant-less and no longer endpoint-less. API-FIN-033
(`POST /api/v1/fin/fiscal-periods/search`, `FiscalPeriodService.search`) is gated on
`PERM_FIN_PERIODS_VIEW`, and the matching `PermissionConstants` constant was added with it. No
migration was needed — the action row was already registered by V24 and already granted by V25 and
V27. This section previously recorded the opposite state ("no `PermissionConstants` constant,
because FIN publishes no fiscal-period read endpoint … §B5 and the API registry define no search
API"); that was accurate before API-FIN-033 and is superseded now. srs-fin.md SCR-REQ-FIN-007 §B5
carries the endpoint row.

**Seed data** (REQ-FIN-044): 12 SEC_PAGES rows registered via SEC's screen-registration
endpoint at FIN onboarding; one action row per action above via SEC's action-registration
endpoint, following `PERM_<PAGE_CODE>_<ACTION>` — including the two custom actions
(`PERM_FIN_JOURNAL_ENTRIES_REVERSE`, `PERM_FIN_PERIODS_CLOSE_APPROVE`). Delivered as migrations
V24 (registry) and V25 (SYS_ADMIN grants); V27 adds the dedicated `FIN_CLOSE_APPROVER` role; V28
adds the one later action row, `FIN_DIMENSIONS / UPDATE`, together with its own explicit SYS_ADMIN
grant.

**SoD enforcement (RULE-FIN-015, POL-FIN-016)**: `PERM_FIN_PERIODS_CLOSE_APPROVE` and
`PERM_FIN_JOURNAL_ENTRIES_CREATE` must never be held by the same role by platform
convention (an administrative guideline enforced by role design, not a database
constraint — SEC's RBAC model grants permissions per role, and FIN's service layer
additionally checks at hard-close/year-end-close time that no single *user* holds both,
per role union, Phase 1 CORE). The role that holds close-approval is
`FIN_CLOSE_APPROVER`, minted by V27: module grant FIN, screen grant FIN_PERIODS only, and
exactly two action grants (`PERM_FIN_PERIODS_VIEW` gateway + `PERM_FIN_PERIODS_CLOSE_APPROVE`),
so it structurally cannot carry entry creation. It is assigned to NO user by V27 on purpose —
the assignee must be someone who holds no role carrying `PERM_FIN_JOURNAL_ENTRIES_CREATE`, which
excludes the bootstrap `admin` (SYS_ADMIN), and that is an environment-specific administrative
act rather than schema. Until such an assignment exists, API-FIN-026 and API-FIN-027 answer
`FIN-403-SOD-VIOLATION` (nobody holds close-approval is a failure just as surely as one user
holding both). The SEC read behind this check is XM-FIN-002.

**Gateway**: every non-VIEW permission requires VIEW on the same screen first (platform
convention, SEC's own interceptor — not restated as a FIN-owned RULE).

**Forbidden responses**: `FIN-403-SOD-VIOLATION` maps through the `LocalizedException`
envelope, carrying a registered FIN code and both ar/en messages. `FIN-403-FORBIDDEN` still does
NOT, and that half is unchanged: an authorization failure at `@PreAuthorize` is rendered by
`com.erp.common.web.GlobalExceptionHandler.handleAccessDenied`, which emits the platform code
`ACCESS_DENIED`; `FIN-403-FORBIDDEN` is in no `FinErrorCodes` constant and in neither i18n bundle.
It is the catalog's *name* for that platform response, not a FIN code, and **FIN still has no
`FIN-403-FORBIDDEN` on the wire**.

What DID change, platform-wide: the 403 body is now localized. `handleAccessDenied` resolves its
message through the same `resolveMessage(...)` helper and `MessageSource` every other handler uses,
keyed on a new `CommonErrorCodes.ACCESS_DENIED` constant, and `ACCESS_DENIED` was added to BOTH
`messages.properties` and `messages_ar.properties`. The wire `code` is unchanged and the English
text is byte-identical to the string that was hardcoded before, so only Arabic callers observe any
difference. The earlier description of this response as carrying "a hardcoded English message,
bypassing `MessageSource`" is therefore no longer accurate; the separate point — that this is a
platform response and not a FIN code — still stands, as does the fact that routing it through
`LocalizedException` would change every module's 403 envelope and remains a platform decision.
<!-- PHASE:SEC-BE:END -->
