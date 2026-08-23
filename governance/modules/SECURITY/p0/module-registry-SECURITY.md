**Project: P0 — Platform Inception Engine (RETROFIT MODE)**
*(Role note: this artifact belongs to Project 0, not Project 0.5 — issued
under the P0 identity explicitly, per user request, in the same session
that has otherwise been running as PRD Engine.)*

══════════════════════════════════════════════════════════════════
⚠ RETROFIT MODE — reconstructed from existing implemented code
══════════════════════════════════════════════════════════════════
Module              : SECURITY
Source              : extracted-facts-SECURITY.md, SECTION LAYER 0
                       + backend.zip (direct code verification, 2026-08-23)
This artifact DOCUMENTS an already-implemented backend module. It is
NOT a forward design document — content reflects what the code
currently does, not necessarily what was originally intended or what
"should" exist. Every element traces to the extraction file or to
direct backend.zip verification; gaps are preserved as open items
(AQ-IDs), never silently resolved.
══════════════════════════════════════════════════════════════════

## Classification

**1.2 Security → EXCEPTION** (per platform-standards §M.A.4: Security
Model is a default-EXCEPTION module type). Unchanged from the original
P0 pass — SECURITY is read AS-IS; no ENTITY-ID/LOV-ID candidate table
or business-policies.md is produced for it (see the companion note on
business-policies-SECURITY.md's retirement, issued separately).

SECURITY has no compile-time Maven dependency on ORG, MASTERDATA, or
NOTIFICATION — it reaches all three only via same-JVM internal HTTP
calls — while every other backend module is expected to depend on it
for auth/authorization. That asymmetry remains the structural signature
of an EXCEPTION module.

## Module boundary (AS-IS facts)

- Maven artifact `erp-security`, packaging jar, built as a library
  consumed by erp-main (modular monolith) — spring-boot-maven-plugin
  repackaging explicitly skipped.
- Declared dependencies: erp-common-utils, Spring Security/Web/JPA/
  Validation/Cache/Redis starters, PostgreSQL, bucket4j, jjwt libraries,
  springdoc-openapi. Test-only: spring-boot-starter-test,
  spring-security-test. No Maven dependency on erp-org, erp-masterdata,
  or erp-notification — confirmed both by pom.xml and by explicit code
  comments in OrgBranchClient.java, MasterDataLookupClient.java, and
  (new) NotificationClient.java.
- 40 main source files, 7 test files, scanned from the original
  extraction pass; unchanged in this correction pass except where noted
  below.

## Cross-module dependency map (CORRECTED — 2026-08-23, verified against backend.zip)

| Module | Direction | Type | Evidence |
|---|---|---|---|
| **ORG** (`ORG_BRANCH`) | SECURITY → ORG | DB-HARD-FK + code-level SOFT-READ | Real DB FK constraints (`FK_SEC_USER_PROFILE_BRANCH`, `FK_SEC_ROLE_BRANCH_BRANCH` → `ORG_BRANCH.BRANCH_PK`), tagged XM-SEC-001/XM-SEC-002, no JPA object-graph association (no Maven dependency on erp-org). Validated at runtime via `OrgBranchClient` (same-JVM HTTP). |
| **MASTERDATA** (`MD_MASTER_LOOKUP`/`MD_LOOKUP_DETAIL`) | SECURITY → MASTERDATA | SOFT-READ | No FK constraint; validated via `MasterDataLookupClient` REST call at the Service layer. |
| **NOTIFICATION** | SECURITY → NOTIFICATION | SOFT (event-driven, same-JVM HTTP) | **RESOLVED 2026-08-23 (was AQ-SEC-001).** `AuthEventListener` (inside erp-security, `@TransactionalEventListener(AFTER_COMMIT)`) reacts to `AccountActivationRequestedEvent` / `PasswordResetRequestedEvent` (published by `AuthService`, RULE-SEC-031) and calls `NotificationClient`, which POSTs to erp-notification's `POST /api/v1/notifications/send`. Same-JVM HTTP self-call, zero Maven dependency — same pattern as `OrgBranchClient`. Requires a dedicated roleless service account (`svc-notification`, seeded by `005_notification_service_account_seed.sql`) because the triggering flows (signup, forgot-password) are anonymous and have no caller JWT to forward. Tagged **XM-SEC-005** in code (`NotificationClient.java` javadoc). |
| **FILESVC** | none detected | — | Unchanged from original pass — no dependency found in code. |

Both HTTP-based cross-module clients (`OrgBranchClient`,
`MasterDataLookupClient`) still forward the caller's own inbound
Authorization header verbatim, a deliberate stopgap per their own code
comments. `NotificationClient` cannot do this (its callers are
anonymous flows with no inbound JWT), which is why it mints its own
service-account token instead — a materially different, newer pattern.

**Migration script numbering note:** the original extraction saw
`001`, `002`, `003`. The current tree (backend.zip) has `001`, `002`,
`004_convert_rbac_pk_to_sequence.sql`, and
`005_notification_service_account_seed.sql` — `003` no longer exists
under that name. Flagged, not resolved; a future extraction pass should
confirm what happened to `003`'s content (SEC_PAGES/permissions seed).

## Entity candidates (unchanged from original pass)

`UserAccount`, `Role`, `Permission`, `Page`, `RefreshToken`,
`SecRoleBranch` (ENTITY-SEC-010), `SecUserProfile` (ENTITY-SEC-009),
`AccountActivationToken` (ENTITY-SEC-012), `PasswordResetToken`
(ENTITY-SEC-011). Join tables only: `USER_ROLES`, `ROLE_PERMISSIONS`.
Dropped/legacy: `SEC_MENU_ITEM` (migration history only, never read or
written by the application).

## APIs found — RBAC section [CORRECTED — 2026-08-23]

```
Auth       : signup, signup/activate, login, login-token, logout,
             refresh, forgot-password, reset-password
Menu       : GET /api/menu/user-menu, GET /api/menu/user-menu/{userId}
Users      : GET/POST/PUT/DELETE /api/users, /api/users/{userId}/roles,
             /api/users/search
DataScope  : /api/v1/security/role-branches (+ /search),
             /api/v1/security/user-profiles (+ /search)
RBAC       : /api/roles (+ /pages, /copy-from, /search,
             /{roleId}/activate, /{roleId}/deactivate),
             /api/permissions (+ /search),
             /api/pages (+ /active, /deactivate, /reactivate, /search)
```
Correction: previously listed a single `/toggle-active` endpoint, which
does not exist. `RoleController.java` implements separate
`activateRole()` (`PUT /{roleId}/activate`) and `deactivateRole()`
(`PUT /{roleId}/deactivate`) methods. Confirmed against this repo's own
`enforce-backend-contract/SKILL.md`, which codifies split
activate/deactivate endpoints as the required convention —
`RoleController` correctly follows it; only this document's citation
was stale.

## Open Architectural Questions (AQ-IDs)

- **AQ-SEC-001**: **CLOSED 2026-08-23.** Resolved by direct code
  verification: NOTIFICATION is the confirmed listener/consumer of the
  activation/reset events, via `AuthEventListener` + `NotificationClient`
  inside erp-security itself — see Cross-module dependency map above.
- **AQ-SEC-002**: Still OPEN. Package-root naming
  (`com.example.security` vs. the `com.example.erp.*` convention used
  elsewhere) remains unexplained by any in-code comment. Not resolved
  in this pass.

## Registry Update Block

```
P0 REGISTRY UPDATE — master-registry.md (RETROFIT, AMENDED 2026-08-23)
Module     : SECURITY
Status     : EXCEPTION (unchanged)
AQ-IDs     : AQ-SEC-001 CLOSED (2026-08-23) | AQ-SEC-002 open
XM-IDs     : XM-SEC-001, XM-SEC-002 (ORG, unchanged) |
             XM-SEC-005 (NOTIFICATION, new — 2026-08-23)
```

## Governance note on this correction pass

A prior draft of this file referenced `legacy-gap-note-SECURITY.md`
and a `GAP-SEC-01..13` numbering scheme. Those references have been
removed here — that artifact/ID namespace does not exist in
SHARED-GOVERNANCE-CORE.md (CORE-4, CORE-7) or the retrofit protocol.
Open items belong in the existing AQ-ID (P0) or OQ-ID (P1) mechanisms,
per their respective owners — not in an unregistered ad hoc scheme.

## Next step

SECURITY remains EXCEPTION — no ENTITY-ID/LOV-ID table or
business-policies.md is produced for it. The RULE-SEC-030..041 and
POLICY-SEC-* content that appeared in the now-retired
business-policies-SECURITY.md belongs to Project 1 (SRS Governance
Engine)'s retrofit pass instead, where RULE-ID is the correct,
owned namespace.
