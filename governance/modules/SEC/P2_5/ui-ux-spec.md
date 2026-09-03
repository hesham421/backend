# ui-ux-spec-SEC.md
## Security (SEC) — Component-Level Design Intent

```
Produced by     : UI/UX Design Engine (Project 2.5)
Governed by     : CONTRACT-11 · fields/permissions are SRS B3/B4 reference only
Inputs          : srs-SEC.md v1.3 (PART B: B1/B3/B4) + prd-SEC.md v2
Version         : 2.0 (CONTINUATION — updated from v1.0)
Status          : RECONCILED — pending human design approval (CONTRACT-12)
Date            : 2026-09-02
Scope note      : Component NAMES/CSS/routing here are PROPOSAL/intent only — the
                  binding technical spec remains Project 3.2's F1/F4 decision.
                  Stack: React / TS / Vite (FRONTEND_STACK). RTL/i18n: all strings
                  via t(); CSS logical properties only.
```

> **UPSTREAM CHANGE — SEC two-tier RBAC + internal SSO (domain-profile-ERP.md v2)**
> - **Triggered by:** srs-SEC.md **v1.3** (+ENTITY-SEC-010 Module, +ENTITY-SEC-011 RoleModule,
>   +moduleFk on Page, +RULE-SEC-013/014, +API-SEC-017..020, +SCR-SEC-004, updated SCR-SEC-002)
>   ← prd-SEC.md **v2** (US-SEC-008..011) ← domain-profile-ERP.md **v2**.
> - **Amended here (v1.0 → v2.0):** SCR-SEC-002 extended with a Tier-1 Module Picker and a Tier-2
>   Permission Picker constrained to granted modules; moduleFk added to SCR-SEC-003; new
>   SCR-SEC-004 (Module Registry); added a module-scoped dashboard shell block and an SSO note.
>   All prior SCR blocks / fields preserved verbatim.
> - **Downstream must re-align:** Claude Code (UI Shell) → Project 3.2 (F1/F4), per CONTRACT-12.

> **FIND-SEC-01 / FIND-SEC-02 — RESOLVED (Architect, 2026-09-02):** the three public
> pre-auth screens (Login, Forgot/Reset, Activate) and the module-scoped dashboard are
> confirmed to remain **SCR-less** — public / shell screens outside CORE-9 by design (no
> SCR-ID, no SEC_PAGES row, no permissions). Captured below as clearly-marked non-CORE-9 blocks.

---

```
Screen           : SCR-SEC-001 — User Management (إدارة المستخدمين)
UI Pattern       : PATTERN-2 (Search list + Side Drawer entry) — from SRS B1, unchanged
Create/Edit Container Pattern : SIDE_DRAWER
                   (bounded field count, no repeating child rows → SIDE_DRAWER
                    per AMEND-P3-O; matches SRS B1)
Fields shown     : List/filters — username, email, userStatusId (LOV-SEC-002),
                   isActiveFl.
                   Drawer (create/edit) — username, email, phone, fullName,
                   preferredLangId (LOV-SEC-001), userStatusId (LOV-SEC-002),
                   isActiveFl. (passwordHash is system-only, never a form field —
                   RULE-SEC-004.)
                   [SRS B3 — no additions, no omissions]
Permissions      : SEC_USERS → VIEW/CREATE/UPDATE/DELETE = SYS_ADMIN
                   (SRS B4 / CORE-9 — reference only; guard wired by P3.2. Grantable
                    only to a role that holds the SEC module — Tier-1, RULE-SEC-014.)
Empty state      : "No users yet." + primary "Create user" action (if CREATE granted).
Loading state    : Skeleton rows in the list; drawer shows field skeletons while an
                   edit record loads.
Error state      : Inline field errors from RULE-SEC-001/002/003; non-field failures
                   as a generic banner (ERR-ID mapping is P3.2's job).
Design intent note (PROPOSAL): LOV dropdowns (preferredLang, userStatus) show active
                   values only. Deactivate is a distinct confirmed action, not a
                   silent toggle — confirmation states no cascade and that history is
                   retained (RULE-SEC-012). Business codes N/A (username is the
                   natural key, shown read-only after create).
```

---

```
Screen           : SCR-SEC-002 — Roles · Modules · Permissions (الأدوار والصلاحيات)  ⟵ updated v2.0
UI Pattern       : PATTERN-2 (Search list + Side Drawer entry) — from SRS B1
Create/Edit Container Pattern : SIDE_DRAWER
                   (role has a bounded field set + a Tier-1 module multi-select + a
                    Tier-2 permission selector; both are bounded selectors, not computed
                    line-item grids → SIDE_DRAWER, matches SRS B1)
Fields shown     : List/filters — roleCode, nameAr, isActiveFl.
                   Drawer — roleCode, nameAr, nameEn, isActiveFl (ENTITY-SEC-002)
                   + Tier-1 MODULE PICKER (reads Module SEC-010; writes RoleModule
                     SEC-011) → RULE-SEC-013
                   + Tier-2 PERMISSION PICKER, constrained to granted modules only
                     (reads Permission SEC-003; writes RolePermission SEC-009)
                     → RULE-SEC-014.
                   [SRS B3]
Permissions      : SEC_ROLES → VIEW/CREATE/UPDATE/DELETE = SYS_ADMIN (SRS B4)
Empty state      : "No roles defined." + "Create role". Module picker empty →
                   "No modules registered yet — add modules first (Module Registry)."
                   Permission picker (before any module granted) → "Grant a module to
                   see its screens' permissions."
Loading state    : List skeleton; module picker + permission picker each show a loading
                   list while modules / per-page permissions load.
Error state      : roleCode uniqueness (RULE-SEC-010) inline; module grant/revoke
                   (RULE-SEC-013) and permission grant blocked-by-derivation
                   (RULE-SEC-014) surfaced as picker-level notices.
Design intent note (PROPOSAL): Two nested tiers in one drawer.
                   • Tier-1 Module Picker — multi-select of ACTIVE modules; granting a
                     module makes its pages eligible below.
                   • Tier-2 Permission Picker — grouped by GRANTED module → page → the
                     4 auto-generated permissions (VIEW/CREATE/UPDATE/DELETE). Pages of
                     ungranted modules are hidden/disabled; revoking a module removes or
                     greys its pages' permission rows (derivation, RULE-SEC-014). This
                     is a READ of existing permissions only — the screen never creates
                     permissions (Page registration does, CORE-9). roleCode read-only
                     after create.
```

---

```
Screen           : SCR-SEC-003 — Screen / Page Registry (سجل الشاشات)  ⟵ updated v2.0
UI Pattern       : PATTERN-2 (Search list + Side Drawer entry) — from SRS B1
                   (SRS explicitly flags PATTERN-3/Tree as an Architect-approval-only
                    alternative, P3-RULE-2 — NOT adopted here)
Create/Edit Container Pattern : SIDE_DRAWER
                   (Module selector + self-referential parentPageFk selector inside the
                    drawer, not a tree editor — matches SRS B1 default.
                    TREE_MASTER_DETAIL deferred to Architect decision.)
Fields shown     : List/filters — pageCode, nameAr, moduleFk, parentPageFk, isActiveFl.
                   Drawer — pageCode, nameAr, nameEn, moduleFk (mandatory — select from
                   ENTITY-SEC-010 active modules, API-SEC-020 lookup), parentPageFk
                   (self, LOV of active pages), isActiveFl.
                   [SRS B3]
Permissions      : SEC_PAGE_REGISTRY → VIEW/CREATE/UPDATE/DELETE = SYS_ADMIN (SRS B4)
Empty state      : "No pages registered." + "Register page".
Loading state    : List skeleton; Module selector and parent-page selector load active
                   records lazily.
Error state      : pageCode uniqueness (RULE-SEC-010) inline; on successful register,
                   confirm "4 permissions generated for <pageCode>" (RULE-SEC-011).
Design intent note (PROPOSAL): Module selector is mandatory — a page's module membership
                   (moduleFk) is what enables the Tier-2 derivation (RULE-SEC-014).
                   Selectors list ACTIVE records only; parent-page prevents self-selection.
                   Registering a page triggers CORE-9 auto-generation of its 4 permissions
                   server-side — surfaced as a confirmation, never as editable rows here.
```

---

```
Screen           : SCR-SEC-004 — Module Registry (إدارة الموديولات)  ⟵ new v2.0
UI Pattern       : PATTERN-2 (Search list + Side Drawer entry) — from SRS B1
Create/Edit Container Pattern : SIDE_DRAWER
                   (Module is a small reference entity — no child rows/hierarchy →
                    SIDE_DRAWER per AMEND-P3-O; matches SRS B1)
Fields shown     : List/filters — moduleCode, nameAr, isActiveFl.
                   Drawer (create/edit) — moduleCode, nameAr, nameEn, isActiveFl.
                   [SRS B3 — ENTITY-SEC-010]
Permissions      : SEC_MODULES → VIEW/CREATE/UPDATE/DELETE = SYS_ADMIN (SRS B4)
Empty state      : "No modules yet." + primary "Add module" action (if CREATE granted).
Loading state    : Skeleton rows in the list; drawer field skeletons while an edit
                   record loads.
Error state      : moduleCode uniqueness (RULE-SEC-010 pattern) as an inline error.
Design intent note (PROPOSAL): Module is the grantable unit for Tier-1 (RULE-SEC-013);
                   its active records feed the Module Picker on SCR-SEC-002 and the
                   Module selector on SCR-SEC-003. moduleCode is the natural key —
                   read-only after create. Deactivating a module (isActiveFl) removes it
                   from those LOVs (active-only) without hard delete.
```

---

## PLATFORM SHELL — MODULE-SCOPED DASHBOARD (no SCR-ID — FIND-SEC-02)

> Not a CORE-9 CRUD screen. SRS PART B preamble declares this shell-level and leaves the
> detailed flow to P2.5. Captured here as design intent so the UI Shell is complete.

```
Shell block D-SEC-1 — Module-Scoped Dashboard (US-SEC-009)
Behavior         : On mount after login, the app shell calls API-SEC-019 (granted modules
                   for the current user) and renders ONLY those modules as navigation /
                   tiles — a DISPLAY FILTER (Tier-1, RULE-SEC-013). Ungranted modules are
                   not shown.
Permissions      : none as a screen — visibility is derived from the union of the user's
                   roles' granted modules (RoleModule SEC-011). Screen access WITHIN a
                   module remains enforced by CORE-9 (Tier-2).
Empty/Loading/Error : loading → module-tile skeletons; empty → "No modules assigned to
                   your account yet — contact your administrator"; error → generic retry.
Design intent note (PROPOSAL): Tier-1 is visibility only — there is no separate runtime
                   module gate. Confirmed SCR-less shell behavior (FIND-SEC-02 RESOLVED,
                   Architect 2026-09-02).
```

---

## PUBLIC PRE-AUTH SCREENS — PROPOSAL BLOCKS (no SCR-ID — FIND-SEC-01)

> Not CORE-9 screens. Included as design intent so the UI Shell is complete;
> awaiting Architect confirmation on whether to assign formal public SCR-IDs.

```
Proposal P-SEC-1 — Login (public)
UI Pattern       : Centered single-card auth form (proposal)
Fields shown     : username, password; "Forgot password?" link; submit.
                   Traceable to API-SEC-001; RULE-SEC-005 (lock after failures),
                   RULE-SEC-009 (block non-ACTIVE).
Permissions      : none (public)
Empty/Loading/Error : n/a empty · button spinner on submit · generic invalid-credentials
                   message (never reveal which field failed); locked-account notice
                   when lockedUntil is in effect.
SSO note (US-SEC-011): a single successful login yields one internal identity/token that
                   works across ALL platform modules (SEC is the sole auth authority).
                   Authentication only — authorization is the two-tier model above. No
                   external federation now.
Design intent note (PROPOSAL): no self-registration surface (accounts are admin-created
                   + activation flow). Confirmed public / SCR-less (FIND-SEC-01 RESOLVED,
                   Architect 2026-09-02).
```

```
Proposal P-SEC-2 — Forgot / Reset Password (public, two-step)
Fields shown     : Step 1 (Forgot) — email or username → request (API-SEC-004).
                   Step 2 (Reset) — reset token + new password + confirm
                   → submit (API-SEC-005). RULE-SEC-003 (complexity), RULE-SEC-007
                   (single active, single-use token).
Permissions      : none (public)
States           : Step 1 success → neutral "if the account exists, a link was sent"
                   (no account enumeration). Reset: token-expired / already-used notice.
Design intent note (PROPOSAL): confirmed public / SCR-less (FIND-SEC-01 RESOLVED, Architect 2026-09-02).
```

```
Proposal P-SEC-3 — Activate Account (public)
Fields shown     : activation token (+ optional set-password if the flow requires it)
                   → submit (API-SEC-006) → userStatusId ACTIVE. RULE-SEC-008.
Permissions      : none (public)
States           : token-expired / already-used notice; success → CTA to Login.
Design intent note (PROPOSAL): confirmed public / SCR-less (FIND-SEC-01 RESOLVED, Architect 2026-09-02).
```

---

*End of ui-ux-spec-SEC.md v2.0 — every field/permission traced to srs-SEC.md v1.3 B3/B4.*
*Component names/CSS/routing are PROPOSAL intent, not binding (CONTRACT-11).*
