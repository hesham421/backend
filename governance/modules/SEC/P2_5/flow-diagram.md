# flow-diagram-SEC.md
## Security (SEC) — Navigation & Sequencing

```
Produced by     : UI/UX Design Engine (Project 2.5)
Governed by     : CONTRACT-11 (PRD↔SRS Reconciliation Gate)
Inputs          : prd-SEC.md v2 (US-SEC-001..011) + srs-SEC.md v1.3 (PART B: B1–B5)
Mode            : RECONCILE (both PRD and SRS attached)
Version         : 2.0 (CONTINUATION — updated from v1.0)
Status          : RECONCILED — Gate PASSED — pending human design approval (CONTRACT-12)
Date            : 2026-09-02
```

> **UPSTREAM CHANGE — SEC two-tier RBAC + internal SSO (domain-profile-ERP.md v2)**
> - **Triggered by:** srs-SEC.md **v1.3** (+ENTITY-SEC-010 Module, +ENTITY-SEC-011 RoleModule,
>   +moduleFk on Page, +RULE-SEC-013/014, +API-SEC-017..020, +SCR-SEC-004, updated SCR-SEC-002)
>   ← prd-SEC.md **v2** (US-SEC-008..011) ← domain-profile-ERP.md **v2** §GOVERNING RULES.
> - **Amended here (v1.0 → v2.0):** reconciled US-SEC-008..011; added FLOW-SEC-07 (Module
>   Registry, SCR-SEC-004) and FLOW-SEC-08 (module-scoped dashboard — platform shell); extended
>   FLOW-SEC-05 with a Tier-1 module picker + a Tier-2 permission picker constrained to granted
>   modules; added moduleFk to FLOW-SEC-06; added the internal-SSO note to FLOW-SEC-01. All prior
>   FLOW-IDs / SCR-IDs preserved verbatim.
> - **Downstream must re-align:** Claude Code (UI Shell) → Project 3.2 (F1/F4), per CONTRACT-12.

---

## RECONCILIATION GATE — RESULT (re-run for NEW US-IDs only; 001–007 stand from v1.0)

| US-ID | Intent (PRD v2) | SRS v1.3 counterpart | Reconciled |
|---|---|---|---|
| US-SEC-001 | Login + stay signed in (JWT + refresh) | API-SEC-001/002/003 · RULE-SEC-005/006/009 · public "Login" screen (PART B preamble) | ✓ via API-ID |
| US-SEC-002 | Create & manage user accounts | SCR-SEC-001 · API-SEC-007/008/009/010/012 | ✓ SCR-SEC-001 |
| US-SEC-003 | Define roles & assign to users | SCR-SEC-002 · API-SEC-011/012 | ✓ SCR-SEC-002 |
| US-SEC-004 | Define permissions & grant to roles (RBAC) | SCR-SEC-002 (permission picker) · API-SEC-014/015 | ✓ SCR-SEC-002 |
| US-SEC-005 | Control access to each screen via its permission | SCR-SEC-003 · API-SEC-013/014 | ✓ SCR-SEC-003 |
| US-SEC-006 | Self-service password reset | API-SEC-004/005 · RULE-SEC-007 · public "Forgot/Reset" screen | ✓ via API-ID |
| US-SEC-007 | Activate new account | API-SEC-006 · RULE-SEC-008 · public "Activate" screen | ✓ via API-ID |
| **US-SEC-008** | **Assign the set of modules a role may access (Tier-1)** | **SCR-SEC-002 (module picker → RoleModule SEC-011) + SCR-SEC-004 (module catalog) · API-SEC-017/018 · RULE-SEC-013** | **✓ SCR-SEC-002 + SCR-SEC-004** |
| **US-SEC-009** | **Dashboard shows only the user's granted modules** | **Platform-shell display filter · API-SEC-019 · RULE-SEC-013 (SRS PART B preamble — "P2.5 details the flow")** | **✓ via API-ID (shell)** |
| **US-SEC-010** | **Screen permissions grantable only within a granted module (Tier-2)** | **SCR-SEC-002 (permission picker constrained to granted modules) · API-SEC-015 · RULE-SEC-014 (no orphan screen permission)** | **✓ SCR-SEC-002** |
| **US-SEC-011** | **One internal login across all platform modules (SSO)** | **SRS A7 (single internal JWT authority, auth-only) · API-SEC-001 — no new screen/entity** | **✓ via note/API-ID** |

```
Reconciled, no rework needed : 11 / 11 user stories  (4 new this pass: US-SEC-008..011)
Flagged for rework           : 0
Blocked (OQ)                 : 0
Contradictions (OQ)          : 0
```

**Grouping decision (this engine):** SCR-SEC-002 now carries three concerns from SRS B3 —
role definition (US-SEC-003), the **Tier-1 module grant** (US-SEC-008, writes RoleModule
SEC-011), and the **Tier-2 permission grant constrained to granted modules** (US-SEC-004 +
US-SEC-010, RULE-SEC-014). The **grantable module catalog** (US-SEC-008 definitions) is the
new **SCR-SEC-004 — Module Registry** (SEC_MODULES). US-SEC-009 (dashboard) and US-SEC-011
(SSO) are platform-shell/auth behaviors with **no SEC_PAGES row** by SRS design — not CRUD
screens; this engine does not invent SCR-IDs for them.

**Reconciliation finding — FIND-SEC-01 / FIND-SEC-02 — RESOLVED (Architect decision,
2026-09-02):** the three PUBLIC pre-auth screens (Login, Forgot/Reset, Activate) and the
module-scoped dashboard (US-SEC-009) are **confirmed to remain SCR-less** — public / shell
screens outside CORE-9 by design (no SCR-ID, no SEC_PAGES row, no permissions). Their flows
stay traced to API-IDs + RULE-IDs. No SRS (P1) change is required and no SCR-IDs are minted.
This closes both findings.

---

## MODULE NAVIGATION MAP

```
[Login] (public — one internal SSO identity across all platform modules, US-SEC-011)
   │  authenticate (API-SEC-001) → issue JWT + refresh
   ▼
[Landing / App Shell]
   │  loads the user's granted modules only (API-SEC-019, RULE-SEC-013) → renders
   │  the dashboard as a DISPLAY FILTER (module hidden if not granted to any of the
   │  user's roles) — Tier-1. Screen access inside a module still enforced by CORE-9.
   └─ Security (nav parent: الأمن)   [visible only if SEC module granted]
        ├─ SCR-SEC-001  User Management        (SEC_USERS)
        ├─ SCR-SEC-002  Roles · Modules · Permissions (SEC_ROLES)
        ├─ SCR-SEC-003  Screen / Page Registry (SEC_PAGE_REGISTRY)
        └─ SCR-SEC-004  Module Registry        (SEC_MODULES)   ⟵ new v2.0

Public / pre-auth (reachable without a session, no nav parent):
   [Forgot Password] → [Reset Password]      (API-SEC-004 → API-SEC-005)
   [Activate Account]                         (API-SEC-006)
```

---

## FLOWS

### PUBLIC / PRE-AUTH FLOWS (SRS-declared, no SCR-ID — see FIND-SEC-01)

```
FLOW-SEC-01 — Authentication (Login / Refresh / Logout)
  Screens involved : Login (public — no SEC_PAGES row)
  Sequence         : [Login form] → submit → [Landing / App Shell] (on success)
                     └ on ≥5 failures → account locked (RULE-SEC-005, lockedUntil)
                     └ refresh in background (API-SEC-002); logout (API-SEC-003)
  Trigger          : Unauthenticated user opens the app
  Source US-ID(s)  : US-SEC-001, US-SEC-011
  Source SCR-ID(s) : — (public auth screen — SRS PART B preamble; no SCR-ID by SRS design)
                     Traceable counterpart: API-SEC-001/002/003 · RULE-SEC-005/006/009
  Priority         : — (not stated in PRD)
  Status           : RECONCILED (via API-ID)
  SSO note         : SEC is the platform's single authentication authority — one internal
                     JWT identity works across all modules (US-SEC-011, SRS A7). This is
                     authentication only (who you are), separate from the two-tier
                     authorization below. No external federation now (out of scope).
```

```
FLOW-SEC-02 — Forgot & Reset Password
  Screens involved : Forgot Password, Reset Password (public)
  Sequence         : [Forgot form: email/username] → request (API-SEC-004)
                     → CU event → NOTIF sends reset link
                     → [Reset form: token + new password] → submit (API-SEC-005)
                     → [Login]
  Trigger          : User cannot sign in / forgot password
  Source US-ID(s)  : US-SEC-006
  Source SCR-ID(s) : — (public — no SCR-ID by SRS design)
                     Traceable counterpart: API-SEC-004/005 · RULE-SEC-003/007
  Priority         : —
  Status           : RECONCILED (via API-ID)
  Note             : single active, single-use reset token (RULE-SEC-007). SEC never
                     calls NOTIF directly — emits a CU event NOTIF listens to.
```

```
FLOW-SEC-03 — Account Activation
  Screens involved : Activate Account (public)
  Sequence         : new account created (userStatusId = PENDING_ACTIVATION)
                     → CU event → NOTIF sends activation link
                     → [Activate form: token] → submit (API-SEC-006)
                     → userStatusId = ACTIVE → [Login]
  Trigger          : New user follows activation link
  Source US-ID(s)  : US-SEC-007
  Source SCR-ID(s) : — (public — no SCR-ID by SRS design)
                     Traceable counterpart: API-SEC-006 · RULE-SEC-008/009
  Priority         : —
  Status           : RECONCILED (via API-ID)
```

### ADMIN FLOWS (restricted — CORE-9)

```
FLOW-SEC-04 — User Management
  Screens involved : SCR-SEC-001
  Sequence         : [Users list + filters] → New / row-select
                     → [Side Drawer: create/edit] → save → back to list
                     └ Deactivate action → confirm → soft-deactivate (no cascade)
  Trigger          : Admin manages platform user accounts
  Source US-ID(s)  : US-SEC-002
  Source SCR-ID(s) : SCR-SEC-001 (page_code SEC_USERS)
  Priority         : —
  Status           : RECONCILED
  Rules in flow    : save → RULE-SEC-001/002/003/004 · deactivate → RULE-SEC-009,
                     RULE-SEC-012 (allowed, no cascade to SOFT consumers, history
                     retained, reactivation permitted — resolves OQ-SEC-001)
```

```
FLOW-SEC-05 — Roles · Modules · Permissions  (updated v2.0)
  Screens involved : SCR-SEC-002
  Sequence         : [Roles list + filters] → New / row-select
                     → [Side Drawer: role fields]
                       ├ Tier-1 — Module Picker: grant/revoke modules for this role
                       │   (reads Module SEC-010, writes RoleModule SEC-011;
                       │    API-SEC-017 grant / API-SEC-018 revoke → RULE-SEC-013)
                       └ Tier-2 — Permission Picker: CONSTRAINED to the modules just
                           granted — only pages whose module is granted appear; each
                           page exposes its 4 CORE-9 permissions
                           (reads Permission SEC-003, writes RolePermission SEC-009;
                            API-SEC-015 → RULE-SEC-014, no orphan screen permission)
                     → save → back to list
                     └ Assign role to user handled from user context (API-SEC-012)
  Trigger          : Admin defines roles, grants their modules (Tier-1) and their
                     screen permissions within those modules (Tier-2)
  Source US-ID(s)  : US-SEC-003, US-SEC-004, US-SEC-008, US-SEC-010
  Source SCR-ID(s) : SCR-SEC-002 (page_code SEC_ROLES)
  Priority         : —
  Status           : RECONCILED
  Rules in flow    : save → RULE-SEC-010 · grant module → RULE-SEC-013 · grant screen
                     permission → RULE-SEC-014 (blocked unless the page's module is
                     granted) · permission auto-generation → RULE-SEC-011
  Design note      : the Tier-2 picker MUST react to the Tier-1 selection — revoking a
                     module removes/greys its pages' permissions (derivation, RULE-SEC-014).
                     Exact nesting interaction is this engine's proposal; see ui-ux-spec.
```

```
FLOW-SEC-06 — Screen / Page Registry  (updated v2.0)
  Screens involved : SCR-SEC-003
  Sequence         : [Pages list + filters (incl. Module)] → New / row-select
                     → [Side Drawer: page fields + Module selector (mandatory, from
                        ENTITY-SEC-010, API-SEC-020 lookup) + Parent-Page selector (self-ref)]
                     → save → on register, Security auto-generates the 4 permissions
                       for the page_code (RULE-SEC-011, CORE-9)
  Trigger          : Admin registers/edits a screen in the CORE-9 registry
  Source US-ID(s)  : US-SEC-005
  Source SCR-ID(s) : SCR-SEC-003 (page_code SEC_PAGE_REGISTRY)
  Priority         : —
  Status           : RECONCILED
  Rules in flow    : save → RULE-SEC-010 · register → RULE-SEC-011 · module membership
                     (moduleFk) is what enables the Tier-2 derivation → RULE-SEC-014
  Design note      : SRS B1 fixes PATTERN-2 (SIDE_DRAWER + Module + Parent-Page selectors)
                     as the default; the PATTERN-3 (Tree) alternative requires explicit
                     Architect approval (P3-RULE-2). This engine honors the SRS default.
```

```
FLOW-SEC-07 — Module Registry  (new v2.0)
  Screens involved : SCR-SEC-004
  Sequence         : [Modules list + filters] → New / row-select
                     → [Side Drawer: moduleCode, nameAr, nameEn, isActiveFl]
                     → save → back to list
  Trigger          : Admin maintains the catalog of grantable platform modules
                     (the unit granted to roles in Tier-1)
  Source US-ID(s)  : US-SEC-008
  Source SCR-ID(s) : SCR-SEC-004 (page_code SEC_MODULES)
  Priority         : —
  Status           : RECONCILED
  Rules in flow    : save → unique moduleCode (RULE-SEC-010 pattern) · module is the
                     grantable unit for Tier-1 (RULE-SEC-013)
  Design note      : moduleCode read-only after create (natural key). LOV of active
                     modules feeds the Module Picker (SCR-SEC-002) and the Module
                     selector (SCR-SEC-003).
```

```
FLOW-SEC-08 — Module-Scoped Dashboard  (new v2.0 — platform shell, no SEC_PAGES row)
  Screens involved : App Shell / Dashboard (shell behavior — no SCR-ID, see FIND-SEC-02)
  Sequence         : [post-login shell mounts] → GET granted modules for current user
                     (API-SEC-019) → render only granted modules as navigable tiles/nav
                     (DISPLAY FILTER); ungranted modules are not shown
  Trigger          : Any authenticated user lands on the app shell
  Source US-ID(s)  : US-SEC-009
  Source SCR-ID(s) : — (platform-shell display filter — SRS PART B preamble states this
                     is shell-level, "P2.5 details the flow"; no SEC_PAGES row by design)
                     Traceable counterpart: API-SEC-019 · RULE-SEC-013
  Priority         : —
  Status           : RECONCILED (via API-ID)
  Design note      : Tier-1 is a VISIBILITY filter only — there is no separate runtime
                     module gate; real enforcement stays at screen level (CORE-9, Tier-2).
                     A user seeing zero granted modules gets an empty-state shell.
```

---

## CROSS-MODULE (UXD) NOTES

SEC is the identity owner; its screens display only SEC-owned data (UserAccount, Role,
Permission, Page, and the new Module SEC-010 / RoleModule SEC-011) — all sourced from SEC's
own APIs. **No UXD-SEC-* IDs are assigned here**, including for the module-scoped dashboard,
whose granted-modules query (API-SEC-019) reads SEC's own Module data. The reverse dependency
(NOTIF and FILE screens displaying SEC's UserAccount) remains owned by those modules' specs
(UXD-NOTIF-001, UXD-FILE-001) per Section 3A.

---

*End of flow-diagram-SEC.md v2.0 — RECONCILED, Gate PASSED, pending human approval.*
*No field, permission, or screen introduced beyond srs-SEC.md v1.3 B1–B5 (CONTRACT-11).*
