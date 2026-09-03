<!-- Source: PHASE:SEC-BE -->

## PHASE SEC-BE — Backend Security Specifications
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓ (re-passed v1.3)

CORE-9 — one SCR-ID = one SEC_PAGE row = 4 permissions. Permissions auto-generated (RULE-SEC-011), NOT seeded by name.
**Two-tier (v1.3): Tier-1 = SEC_ROLE_MODULE grant (dashboard filter + prerequisite, RULE-SEC-013); Tier-2 = SEC_ROLE_PERMISSION @PreAuthorize (real enforcement, unchanged CORE-9). Derivation RULE-SEC-014 enforced service-layer (AuthorizationGrantDomainService).**

### SEC-BE — SCR-SEC-001 — User Management (page_code SEC_USERS · module SEC)
  API enforcement: API-SEC-007 CREATE, 008 VIEW, 009 UPDATE, 010 DELETE, 012 UPDATE → @PreAuthorize per CORE-9 permission.
  Roles: VIEW/CREATE/UPDATE/DELETE → SYS_ADMIN (all).
### SEC-BE — SCR-SEC-002 — Roles·Modules·Permissions (page_code SEC_ROLES · module SEC) ⟵ updated v1.3
  API enforcement: API-SEC-011 (CRUD→VIEW/CREATE/UPDATE/DELETE), 014 VIEW, 015 UPDATE, **017 UPDATE (assign module), 018 UPDATE (revoke module)**. Roles: SYS_ADMIN.
### SEC-BE — SCR-SEC-003 — Page Registry (page_code SEC_PAGE_REGISTRY · module SEC)
  API enforcement: API-SEC-013 (CRUD→VIEW/CREATE/UPDATE/DELETE). Roles: SYS_ADMIN.
### SEC-BE — SCR-SEC-004 — Module Registry (page_code SEC_MODULES · module SEC) ⟵ v1.3
  API enforcement: API-SEC-020 (CRUD→VIEW/CREATE/UPDATE/DELETE). Roles: SYS_ADMIN.
Self-scoped (no screen permission): API-SEC-019 (/me/modules) — authenticated principal only.
Public (pre-auth, NO enforcement / NO SEC_PAGE row): API-SEC-001..006.

SECURITY SEED DATA REQUIREMENTS (SEC owns SEC_MODULE / SEC_PAGE / SEC_PERMISSION / SEC_ROLE_MODULE):
  SEC_MODULE rows (v1.3): at minimum module **SEC** (owns the four admin pages). Foundation platform modules FILE, NOTIF, CU may be seeded here as the Tier-1 registry as they come online (each module's pages set moduleFk accordingly). moduleCode UNIQUE.
  SEC_PAGE rows: SEC_USERS, SEC_ROLES, SEC_PAGE_REGISTRY, **SEC_MODULES** — **all with MODULE_FK → SEC module (NOT NULL)** (parent nav: Security).
  SEC_PERMISSION rows: 4 per page auto-generated at page registration (RULE-SEC-011) —
    PERM_SEC_USERS_{VIEW,CREATE,UPDATE,DELETE}, PERM_SEC_ROLES_{...}, PERM_SEC_PAGE_REGISTRY_{...}, **PERM_SEC_MODULES_{...}**.
  Tier-1 grant (SEC_ROLE_MODULE) (v1.3): **SEC_ROLE_MODULE(SYS_ADMIN, SEC)** — REQUIRED so SYS_ADMIN's screen-permission grants below satisfy RULE-SEC-014 (no orphan). Seed BEFORE the Tier-2 grants.
  Role grants (SEC_ROLE_PERMISSION): all 16 SEC screen permissions above → SYS_ADMIN (valid because SYS_ADMIN holds the SEC module grant).
SEC-BE Rules: SEC-IMPL-RULE-1 (every SCR API enforces permission), SEC-IMPL-RULE-3 (403 via LocalizedException), SEC-IMPL-RULE-4 (every SCR-ID present in SEC_PAGE), **SEC-IMPL-RULE-5 (v1.3): a Tier-2 grant is only written if the role holds the page's module (RULE-SEC-014); SEC-IMPL-RULE-6 (v1.3): dashboard modules come from SEC_ROLE_MODULE, never from screen permissions.**
─────────────────────────────────────────────────────────────────
