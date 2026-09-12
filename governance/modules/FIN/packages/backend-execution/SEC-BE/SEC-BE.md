<!-- source: PHASE:SEC-BE -->
<!-- traces: REQ-FIN-038, REQ-FIN-044 -->
<!-- PHASE:SEC-BE:START traces=REQ-FIN-038,REQ-FIN-044 -->
## PHASE 7 — SEC-BE (security, backend half)

| Screen (page code) | VIEW | CREATE | UPDATE | DELETE | Custom |
|---|---|---|---|---|---|
| FIN_ACCOUNTS | ✓ (API-FIN-001) | ✓ (API-FIN-002) | ✓ (API-FIN-003, and API-FIN-004 deactivate) | — | — |
| FIN_DIMENSIONS | ✓ (API-FIN-005,008) | ✓ (API-FIN-006,007) | — | — | — |
| FIN_RULES | ✓ (API-FIN-009) | ✓ (API-FIN-010) | ✓ (API-FIN-011, add line) | — | — |
| FIN_RECURRING_TEMPLATES | ✓ (API-FIN-012) | ✓ (API-FIN-013) | ✓ (API-FIN-014, run) | — | — |
| FIN_ALLOCATION_RULES | ✓ (API-FIN-015) | ✓ (API-FIN-016) | ✓ (API-FIN-017, run) | — | — |
| FIN_JOURNAL_ENTRIES | ✓ (API-FIN-018,022) | ✓ (API-FIN-019,020) | — | — | Reverse (`PERM_FIN_JOURNAL_ENTRIES_REVERSE`, API-FIN-021) |
| FIN_PERIODS | ✓ (no API — gateway row only, see below) | ✓ (API-FIN-023, year) | ✓ (API-FIN-024,025) | — | Close-approve (`PERM_FIN_PERIODS_CLOSE_APPROVE`, API-FIN-026,027 — RULE-FIN-015 SoD) |
| FIN_ACCOUNT_LEDGER | ✓ (API-FIN-028) | — | — | — | — |
| FIN_TRIAL_BALANCE | ✓ (API-FIN-029) | — | — | — | — |
| FIN_BALANCE_SHEET | ✓ (API-FIN-030) | — | — | — | — |
| FIN_INCOME_STATEMENT | ✓ (API-FIN-031) | — | — | — | — |
| FIN_DIMENSION_REPORTS | ✓ (API-FIN-032) | — | — | — | — |

**DELETE column — deliberately empty everywhere.** FIN exposes no `DELETE` endpoint at all.
Deactivation is `PUT /{id}/deactivate` gated by the screen's UPDATE permission (see
`AccountService.deactivate`, `@PreAuthorize` on `PERM_FIN_ACCOUNTS_UPDATE`), exactly as
MDL_LOOKUPS models it, and V24 seeds no `PERM_FIN_*_DELETE` row for any FIN screen. The
FIN_ACCOUNTS row above read "✓ deactivate (API-FIN-004)" under DELETE until ALIGN-BE moved it
to UPDATE; inventing DELETE rows here would create permanently-unreferenced registry data.

**FIN_PERIODS / VIEW — a registered gateway with no endpoint.** `PERM_FIN_PERIODS_VIEW` is a
real V24 action row and is load-bearing: `MenuService.effectiveAuthorityCodes()` keeps a granted
permission only if the same screen also carries a granted gateway (VIEW) action, so V27's
FIN_CLOSE_APPROVER role must hold it for `PERM_FIN_PERIODS_CLOSE_APPROVE` to survive into the
caller's authorities. It is nevertheless the one of 26 action rows with no `PermissionConstants`
constant, because FIN publishes no fiscal-period read endpoint — SCR-REQ-FIN-007's §B2 names
period list filters, but §B5 and the API registry define no search API for them, and the screen
gets its rows from API-FIN-023's response. Recorded as an open ALIGN-BE gap.

**Seed data** (REQ-FIN-044): 12 SEC_PAGES rows registered via SEC's screen-registration
endpoint at FIN onboarding; one action row per action above via SEC's action-registration
endpoint, following `PERM_<PAGE_CODE>_<ACTION>` — including the two custom actions
(`PERM_FIN_JOURNAL_ENTRIES_REVERSE`, `PERM_FIN_PERIODS_CLOSE_APPROVE`). Delivered as migrations
V24 (registry) and V25 (SYS_ADMIN grants); V27 adds the dedicated `FIN_CLOSE_APPROVER` role.

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
envelope, carrying a registered FIN code and both ar/en messages. `FIN-403-FORBIDDEN` does
NOT: an authorization failure at `@PreAuthorize` is rendered by
`com.erp.common.web.GlobalExceptionHandler.handleAccessDenied`, which builds an `ApiError` with
the hardcoded code `ACCESS_DENIED` and a hardcoded English message, bypassing `MessageSource`;
`FIN-403-FORBIDDEN` is in no `FinErrorCodes` constant and in neither i18n bundle. It is the
catalog's *name* for that platform response, not a FIN code. This is platform-wide (SEC, MDL,
CU, NOTIF, FILE behave identically), so changing it is a platform decision — recorded as an
open ALIGN-BE finding, not fixed here.
<!-- PHASE:SEC-BE:END -->
