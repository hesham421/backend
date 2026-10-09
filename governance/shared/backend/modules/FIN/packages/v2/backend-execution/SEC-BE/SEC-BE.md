<!-- source: PHASE:SEC-BE -->
<!-- traces: REQ-FIN-038, REQ-FIN-044, REQ-FIN-045, REQ-FIN-054 -->
<!-- PHASE:SEC-BE:START traces=REQ-FIN-038,REQ-FIN-044,REQ-FIN-045,REQ-FIN-054 -->
## PHASE 7 — SEC-BE (security, backend half)

Permission matrix — one row per composite screen; a `✓` cell names the endpoints it serves and the permission for
that action. FIN publishes no `DELETE` endpoint, so the DELETE column is empty everywhere.

| Screen (page code) | ENT | VIEW | CREATE | UPDATE | DELETE | Custom |
|---|---|---|---|---|---|---|
| FIN_ACCOUNTS | ENT-FIN-001 | ✓ `PERM_FIN_ACCOUNTS_VIEW` (API-FIN-001) | ✓ `PERM_FIN_ACCOUNTS_CREATE` (API-FIN-002) | ✓ `PERM_FIN_ACCOUNTS_UPDATE` (API-FIN-003; API-FIN-004 deactivate) | — | — |
| FIN_DIMENSIONS | ENT-FIN-002, ENT-FIN-003 | ✓ `PERM_FIN_DIMENSIONS_VIEW` (API-FIN-005, API-FIN-008) | ✓ `PERM_FIN_DIMENSIONS_CREATE` (API-FIN-006, API-FIN-007) | ✓ `PERM_FIN_DIMENSIONS_UPDATE` (API-FIN-035, deactivate a dimension value — V28) | — | — |
| FIN_RULES | ENT-FIN-009, ENT-FIN-010, ENT-FIN-016 | ✓ `PERM_FIN_RULES_VIEW` (API-FIN-009) | ✓ `PERM_FIN_RULES_CREATE` (API-FIN-010) | ✓ `PERM_FIN_RULES_UPDATE` (API-FIN-011, add a line with its dimension tags; API-FIN-034, deactivate a rule) | — | — |
| FIN_RECURRING_TEMPLATES | ENT-FIN-011, ENT-FIN-012 | ✓ `PERM_FIN_RECURRING_TEMPLATES_VIEW` (API-FIN-012) | ✓ `PERM_FIN_RECURRING_TEMPLATES_CREATE` (API-FIN-013) | ✓ `PERM_FIN_RECURRING_TEMPLATES_UPDATE` (API-FIN-014, run; API-FIN-036, deactivate) | — | — |
| FIN_ALLOCATION_RULES | ENT-FIN-013, ENT-FIN-014 | ✓ `PERM_FIN_ALLOCATION_RULES_VIEW` (API-FIN-015) | ✓ `PERM_FIN_ALLOCATION_RULES_CREATE` (API-FIN-016) | ✓ `PERM_FIN_ALLOCATION_RULES_UPDATE` (API-FIN-017, run; API-FIN-037, deactivate) | — | — |
| FIN_JOURNAL_ENTRIES | ENT-FIN-004, ENT-FIN-005, ENT-FIN-006 | ✓ `PERM_FIN_JOURNAL_ENTRIES_VIEW` (API-FIN-018, API-FIN-022) | ✓ `PERM_FIN_JOURNAL_ENTRIES_CREATE` (API-FIN-019, API-FIN-020) | — | — | Reverse `PERM_FIN_JOURNAL_ENTRIES_REVERSE` (API-FIN-021) |
| FIN_PERIODS | ENT-FIN-007, ENT-FIN-008 | ✓ `PERM_FIN_PERIODS_VIEW` (API-FIN-033, and the gateway) | ✓ `PERM_FIN_PERIODS_CREATE` (API-FIN-023) | ✓ `PERM_FIN_PERIODS_UPDATE` (API-FIN-024, API-FIN-025) | — | Close-approve `PERM_FIN_PERIODS_CLOSE_APPROVE` (API-FIN-026, API-FIN-027 — the distinct permission RULE-FIN-015 requires) |
| FIN_ACCOUNT_LEDGER | ENT-FIN-005 | ✓ `PERM_FIN_ACCOUNT_LEDGER_VIEW` (API-FIN-028) | — | — | — | — |
| FIN_TRIAL_BALANCE | ENT-FIN-005 | ✓ `PERM_FIN_TRIAL_BALANCE_VIEW` (API-FIN-029) | — | — | — | — |
| FIN_BALANCE_SHEET | ENT-FIN-005 | ✓ `PERM_FIN_BALANCE_SHEET_VIEW` (API-FIN-030) | — | — | — | — |
| FIN_INCOME_STATEMENT | ENT-FIN-005 | ✓ `PERM_FIN_INCOME_STATEMENT_VIEW` (API-FIN-031) | — | — | — | — |
| FIN_DIMENSION_REPORTS | ENT-FIN-005, ENT-FIN-006 | ✓ `PERM_FIN_DIMENSION_REPORTS_VIEW` (API-FIN-032) | — | — | — | — |
| FIN_ACCOUNT_MAPPINGS (v2) | ENT-FIN-015 | ✓ `PERM_FIN_ACCOUNT_MAPPINGS_VIEW` (API-FIN-038) | ✓ `PERM_FIN_ACCOUNT_MAPPINGS_CREATE` (API-FIN-039) | ✓ `PERM_FIN_ACCOUNT_MAPPINGS_UPDATE` (API-FIN-040, change the account; API-FIN-041, deactivate) | — | — |

**Every API verifies its permission before processing.** Each service method carries `@PreAuthorize` naming the
cell's permission from `PermissionConstants`. Services hold no hard-coded role check. API-FIN-020 is a system call:
the event consumer's service principal must hold `PERM_FIN_JOURNAL_ENTRIES_CREATE`; how a machine caller
authenticates is SEC's separate change set (CS-FIN-001, out of scope).

**v2 — FIN_ACCOUNT_MAPPINGS.** One new SEC_PAGES row (page code FIN_ACCOUNT_MAPPINGS, parent FIN, name ar
"روابط الحسابات" / en "Account mappings") and three action rows, VIEW (the gateway), CREATE and UPDATE, following
`PERM_<PAGE_CODE>_<ACTION>`. `PermissionConstants` gains the three constants. Deactivate is modelled under UPDATE,
exactly as FIN_ACCOUNTS models API-FIN-004; there is no DELETE action for this page. They are delivered by a new
forward FIN migration — the next free Flyway version after V30 — that registers the page and its three actions AND
grants all three explicitly to SYS_ADMIN, following V28. The explicit grant is required: V25 granted SYS_ADMIN its FIN
actions with a `SELECT` over the action registry that has already run everywhere, so Flyway will never re-evaluate
it for a row inserted later. Registering without granting would leave every mapping endpoint answering
FIN-403-FORBIDDEN to every caller, the administrator included. The SRS role "finance administrator" is assigned
these permissions through SEC's role screens; it is not a FIN seed. `FIN_CLOSE_APPROVER` (V27) receives nothing new.

**v2 — FIN_RULES.** Dimension tags travel inside the add-rule-line submission and add no action (ADR-FIN-025):
API-FIN-011 keeps `PERM_FIN_RULES_UPDATE`, so no new constant, no new row and no migration.

**The DELETE column is empty by design.** Deactivation is `PUT /{id}/deactivate` under the screen's UPDATE
permission everywhere — API-FIN-004, 034, 035, 036, 037 and, in v2, API-FIN-041 — and no FIN screen has a
registered delete permission (V24, V28 and the v2 migration seed none). No deactivate has an `activate`
counterpart, following the delivered `AccountService.deactivate` precedent.

**FIN_DIMENSIONS / UPDATE — the one cell V24 did not seed** (as built). `PERM_FIN_DIMENSIONS_UPDATE` is registered by
`V28__fin_dimensions_update_action.sql`, which also grants it explicitly to SYS_ADMIN, for the V25 reason above. The
VIEW gateway on the same screen was already registered and granted by V24/V25.

**FIN_RULES, FIN_RECURRING_TEMPLATES and FIN_ALLOCATION_RULES / UPDATE — one permission, two endpoints each** (as
built). API-FIN-034, API-FIN-036 and API-FIN-037 reuse `PERM_FIN_RULES_UPDATE`,
`PERM_FIN_RECURRING_TEMPLATES_UPDATE` and `PERM_FIN_ALLOCATION_RULES_UPDATE`, seeded by V24 and granted by V25's
Tier-3 statement, so none needed a new constant, error code or migration.

**Deactivating a template or an allocation rule stops its run — closed 2026-09-12, by decision, not by rule.**
`RecurringTemplateDomain.assertCanRun()` (API-FIN-014) and `AllocationRuleDomain.assertCanRun()` (API-FIN-017) refuse
a deactivated definition with FIN-409-NOT-ACTIVE. No RULE-FIN-* states this gate; it rests on a recorded human
decision and must not be cited as pre-existing spec. What stays open on those two screens is only that neither can be
updated after creation (SRS SCR-REQ-FIN-004 §B4, SCR-REQ-FIN-005 §B4).

**FIN_PERIODS / VIEW — a gateway that also has an endpoint** (as built). `PERM_FIN_PERIODS_VIEW` is load-bearing:
`MenuService.effectiveAuthorityCodes()` keeps a granted permission only if the same screen also carries a granted
VIEW, so V27's `FIN_CLOSE_APPROVER` must hold it for `PERM_FIN_PERIODS_CLOSE_APPROVE` to reach the caller. Since
API-FIN-033 it also gates the fiscal-period search; the constant was added with that endpoint, and no migration was
needed.

**SoD enforcement (RULE-FIN-015, POL-FIN-016) — as rewritten 2026-09-12, unchanged by v2.** The requirement, read at
source: the period-close approval is gated by a permission distinct from journal-entry creation, enforced through the
Security module, and the rule's `Data source` is DEFERRED — FIN has no permission entity to read. The delivered
`@PreAuthorize(PermissionConstants.PERM_FIN_PERIODS_CLOSE_APPROVE)` on `FiscalPeriodService.hardClose` (API-FIN-026)
and `FiscalYearService.yearEndClose` (API-FIN-027) satisfies it in full: it is a different code from
`PERM_FIN_JOURNAL_ENTRIES_CREATE`, and SEC's mechanism enforces it (`JwtAuthenticationFilter` builds the caller's
authorities from `MenuService`, which reads the grant table). Keeping the two permissions on different roles remains
sound administration, and `FIN_CLOSE_APPROVER` (V27) is how a deployment expresses it — a guideline, not a check in FIN
code.

**What this section used to specify, and must not specify again.** Earlier revisions said FIN's service layer also
checks at close time that no single user holds both permissions. That global user-set disjointness rule is stronger
than anything the SRS states. It was implemented as `FinSeparationOfDutiesService` reading SEC's user directory and
`FiscalPeriodDomain.assertCanHardClose` throwing `FIN-403-SOD-VIOLATION` whenever any user in the system held both —
refusing the close for every caller. Both were deleted on 2026-09-12 by recorded human decision. **No FIN service
reads SEC's user directory, and none may be specified here.** XM-FIN-002 is retired and FIN-403-SOD-VIOLATION is
struck as unreachable in the Error Catalog.

**Who can close, as seeded.** V25 had withheld `PERM_FIN_PERIODS_CLOSE_APPROVE` from SYS_ADMIN only to avoid tripping
the removed check; `V30__fin_sys_admin_close_approve_grant.sql` grants it, so the bootstrap `admin` user can hard-close
a period and run a year-end close. V27's `FIN_CLOSE_APPROVER` role (the `PERM_FIN_PERIODS_VIEW` gateway plus
`PERM_FIN_PERIODS_CLOSE_APPROVE`, and by construction no entry creation) stays valid as the least-privilege option.
V27's header text and V25's describe the removed mechanism as live; both are applied and immutable, so this paragraph
is the correction: there is no `resolveFacts()`, no `assertCanHardClose` and no thrower of `FIN-403-SOD-VIOLATION`
anywhere.

**Gateway**: every non-VIEW permission requires VIEW on the same screen first — SEC's own interceptor convention, not
a FIN-owned rule.

**Forbidden responses**: a `@PreAuthorize` denial on any `com.erp.fin.service.*` method is caught by
`com.erp.fin.security.FinForbiddenAdvisor` (one `DefaultPointcutAdvisor` at `Ordered.HIGHEST_PRECEDENCE`, never a
feature `@ControllerAdvice`) and re-raised as `LocalizedException(Status.FORBIDDEN, FinErrorCodes.FIN_403_FORBIDDEN)`,
so FIN-403-FORBIDDEN reaches the wire with both ar and en messages. Two boundaries hold, both read in source: a
denial raised by the Spring Security filter chain before any FIN service is entered still answers the platform's own
forbidden code — unreachable for FIN today, because the platform `SecurityConfig` authorizes with
`.anyRequest().authenticated()` and declares no FIN URL rule; and FIN-403-SOD-VIOLATION never was an access denial
and now has no throw site. The v2 mapping endpoints inherit this unchanged.

### BOOTSTRAP DATA — FIN v2
Every row a fresh deployment needs before a FIN endpoint can succeed, each with who produces it. Lookup tables belong
to MDL, yet the keys are FIN's, so FIN seeds them. Registering a permission is not a grant: every permission names the
role it is granted to (`profile.conventions.security_model.grant_target` = role).

```
LOOKUP KEY  ACCOUNT_TYPE               │ seed source: FIN onboarding into MDL (REQ-FIN-045, delivered in v1) — 5 values
LOOKUP KEY  DEBIT_CREDIT               │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 2 values
LOOKUP KEY  PERIOD_STATE               │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 4 values
LOOKUP KEY  FISCAL_YEAR_STATUS         │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 2 values
LOOKUP KEY  JOURNAL_TYPE               │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 5 values, plus CLOSING and OPENING added as data at v1 ALIGN-BE
LOOKUP KEY  JOURNAL_STATUS             │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 3 values
LOOKUP KEY  ACCOUNTING_EVENT_TYPE      │ seed source: the type by FIN onboarding (v1); its values are host data, added in MDL by the deploying organization before its first rule or mapping (SRS A6 — none seeded)
LOOKUP KEY  PAYMENT_METHOD             │ seed source: the type by FIN onboarding (v1); CHEQUE by db-script v2 BLOCK 8 (idempotent SQL into MDL's tables, ADR-FIN-028); every other method is host data
LOOKUP KEY  ACCOUNT_DERIVATION_TYPE    │ seed source: the type, CONSTANT and MAPPING by FIN onboarding (v1); DIRECT deactivated by db-script v2 BLOCK 8
LOOKUP KEY  AMOUNT_SOURCE_TYPE         │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 3 values
LOOKUP KEY  DISTRIBUTION_TYPE          │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 3 values
LOOKUP KEY  RECURRING_SCHEDULE_TYPE    │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 2 values
LOOKUP KEY  RECURRING_FREQUENCY        │ seed source: FIN onboarding into MDL (REQ-FIN-045, v1) — 4 values
LOOKUP KEY  FIN_EVENT_BUSINESS_FIELD   │ seed source: db-script v2 BLOCK 8 — the type and 4 values (ADR-FIN-013)
LOOKUP KEY  FIN_DIMENSION_VALUE_SOURCE │ seed source: db-script v2 BLOCK 8 — the type and 2 values (ADR-FIN-010)
PERMISSION  PERM_FIN_ACCOUNTS_VIEW                │ grant target: role SYS_ADMIN — registered by V24, granted by V25
PERMISSION  PERM_FIN_ACCOUNTS_CREATE              │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ACCOUNTS_UPDATE              │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_DIMENSIONS_VIEW              │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_DIMENSIONS_CREATE            │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_DIMENSIONS_UPDATE            │ grant target: role SYS_ADMIN — registered and granted by V28
PERMISSION  PERM_FIN_RULES_VIEW                   │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RULES_CREATE                 │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RULES_UPDATE                 │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RECURRING_TEMPLATES_VIEW     │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RECURRING_TEMPLATES_CREATE   │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_RECURRING_TEMPLATES_UPDATE   │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ALLOCATION_RULES_VIEW        │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ALLOCATION_RULES_CREATE      │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ALLOCATION_RULES_UPDATE      │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_JOURNAL_ENTRIES_VIEW         │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_JOURNAL_ENTRIES_CREATE       │ grant target: role SYS_ADMIN — V24, V25; the event consumer's service principal needs a role holding it (SEC change set)
PERMISSION  PERM_FIN_JOURNAL_ENTRIES_REVERSE      │ grant target: role SYS_ADMIN — V24 (custom action), V25
PERMISSION  PERM_FIN_PERIODS_VIEW                 │ grant target: roles SYS_ADMIN (V25) and FIN_CLOSE_APPROVER (V27) — V24 registers it
PERMISSION  PERM_FIN_PERIODS_CREATE               │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_PERIODS_UPDATE               │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_PERIODS_CLOSE_APPROVE        │ grant target: roles SYS_ADMIN (V30) and FIN_CLOSE_APPROVER (V27) — V24 registers it
PERMISSION  PERM_FIN_ACCOUNT_LEDGER_VIEW          │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_TRIAL_BALANCE_VIEW           │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_BALANCE_SHEET_VIEW           │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_INCOME_STATEMENT_VIEW        │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_DIMENSION_REPORTS_VIEW       │ grant target: role SYS_ADMIN — V24, V25
PERMISSION  PERM_FIN_ACCOUNT_MAPPINGS_VIEW        │ grant target: role SYS_ADMIN — registered and granted by the v2 FIN security migration (after V30)
PERMISSION  PERM_FIN_ACCOUNT_MAPPINGS_CREATE      │ grant target: role SYS_ADMIN — the v2 FIN security migration
PERMISSION  PERM_FIN_ACCOUNT_MAPPINGS_UPDATE      │ grant target: role SYS_ADMIN — the v2 FIN security migration
REQUIRED    FIN_ACCOUNT.is_retained_earnings_fl  │ seed source: marked on exactly one account as data by the deploying organization before the first year-end close (API-FIN-027 answers FIN-404-ACCOUNT otherwise); never written by an API
DATA        FIN_ACCOUNT_MAPPING rows            │ seed source: the migration-data load of CS-FIN-001, produced once the store exists (out of scope); until then every MAPPING line answers FIN-422-UNMAPPED-VALUE
```
<!-- PHASE:SEC-BE:END -->
