## MODULE REGISTRY — SECURITY
══════════════════════════════════════════════════════════════════
Module Name    : Security
Module Code    : SEC
Layer          : L1
Type           : Engine (Foundation — authN/authZ, roles, permissions)
Execution Tier : T1 (built after Common Utils)
P0 Date        : 2026-09-01
Readiness      : READY
Domain KB      : none supplied — derived from domain-profile-ERP.md
Source         : NEW  (fresh — nothing implemented; srs-SECURITY.md used as IDEA reference only, re-derived for Foundation scope)
══════════════════════════════════════════════════════════════════

SCOPE NOTE
──────────────────────────────────────────────────────────────────
Reusable authentication + authorization foundation: user accounts,
roles, permissions, screen(page) registry, JWT sessions with refresh,
plus self-service forgot-password and account-activation. Stateless
(JWT). Built in full (no partial/deferred parts). Adapted from the
government reference: branch/org-based DataScope is DROPPED (it depends
on an Organization module that is out of the Foundation domain — business
domains introduce their own scoping later). Medium complexity — basic
user profile fields fold into UserAccount rather than a separate profile
entity.

ENTITIES OWNED
──────────────────────────────────────────────────────────────────
UserAccount            │ Master        │ SHARED  (consumed SOFT by NOTIF, FILE)
Role                   │ Master        │ PRIVATE
Permission             │ Config        │ PRIVATE
Page (Screen Registry) │ Config/Ref    │ PRIVATE  (CORE-9 screen-registry owner)
RefreshToken           │ Internal      │ PRIVATE
PasswordResetToken     │ Internal      │ PRIVATE
AccountActivationToken │ Internal      │ PRIVATE
UserRole               │ INTERNAL/JOIN │ (User×Role — @JoinTable, no standalone id)
RolePermission         │ INTERNAL/JOIN │ (Role×Permission — @JoinTable, no standalone id)
──────────────────────────────────────────────────────────────────
Note: names only — ENTITY-IDs assigned by P1. UserAccount is the one
SHARED entity (canonical owner = Security, RULE-10); consumers read it
SOFT at the application layer (no cross-module FK from NOTIF/FILE).

LOVs OWNED
──────────────────────────────────────────────────────────────────
(none by default)
──────────────────────────────────────────────────────────────────
Note: permission TYPE follows the CORE-9 naming convention
PERM_<PAGE>_<VIEW|CREATE|UPDATE|DELETE> (a code convention, not a DB LOV).
preferredLang (AR/EN) handling — free-text vs small LOV — is a P1 decision.

LOVs CONSUMED (from other modules)
──────────────────────────────────────────────────────────────────
(none)
──────────────────────────────────────────────────────────────────

SHARED ENTITIES CONSUMED
──────────────────────────────────────────────────────────────────
(none — Security is the identity owner; it consumes no other module's entity)
──────────────────────────────────────────────────────────────────

DEPENDENCIES
──────────────────────────────────────────────────────────────────
Common Utils │ USES (library) │ exceptions, config, events, specification/filtering
──────────────────────────────────────────────────────────────────
ROOT: NO — depends on Common Utils only. No Organization dependency.

AUTO-DECISIONS
──────────────────────────────────────────────────────────────────
AUTO: Stateless JWT auth (access token) + RefreshToken rotation.
FROM: reference AD (heac security) + standard Spring Security practice.
IF WRONG: switch session model at P1 (e.g. opaque tokens) — entity set unaffected.

AUTO: DROP branch/org DataScope (reference ENTITY-SEC-010 SecRoleBranch).
FROM: no Organization module in the Foundation domain (out of scope).
IF WRONG: if a generic data-scope extension point is wanted in the reusable
          auth core, re-open as an explicit design decision at P1.

AUTO: Basic profile fields (name/email/phone/preferredLang) live on UserAccount;
      no separate UserProfile entity (reference ENTITY-SEC-009).
FROM: medium-complexity rule (avoid over-engineering).
IF WRONG: split into a UserProfile entity at P1 if profile data grows.

AUTO: Include forgot-password (PasswordResetToken) + account-activation
      (AccountActivationToken) as owned entities.
FROM: domain-profile "full independent build — no partial/deferred work".
IF WRONG: n/a — these complete the auth surface.

AUTO: Page(screen) registry + permission binding follow CORE-9 Composite
      Screen Governance (Security owns the screen-registry + permission tables).
FROM: SHARED-GOVERNANCE-CORE CORE-9.
IF WRONG: n/a — CORE-9 is mandatory logic.

INF-IDs
──────────────────────────────────────────────────────────────────
(none — all decisions traced to domain-profile / reference / CORE-9 via
 AUTO-DECISIONS above; no unresolved gap)
──────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════
