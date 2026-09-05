<!-- Source: PHASE:SEC-BE -->

## PHASE SEC-BE — Backend Security Specifications

### SEC-BE — SCR-MDM-001 — إدارة القوائم المرجعية (Reference Data Lookup Management)
─────────────────────────────────────────────────────────────────
API-level enforcement:
  API-MDM-001..010 each require permission verification before processing (see SECURITY block
  in each API contract above). API-MDM-011 is the one exception — auth-only, not screen-gated
  (DRV-006).

EXCEPTION module scope: None — Security (SEC) is not an EXCEPTION module; standard integration.
─────────────────────────────────────────────────────────────────

SECURITY SEED DATA REQUIREMENTS:

Tier 1 — Module grant (two-tier RBAC, per domain-profile.md v3 / SEC two-tier RBAC amendment):
  SEC_MODULE   : one row for this module —
                 moduleCode = MDM, moduleName(AR/EN) = البيانات المرجعية / Master Data
  SEC_ROLE_MODULE : grant R1 (مدير البيانات المرجعية / Master Data Administrator) → MDM
                     grant R2 (مدير النظام / System Administrator) → MDM
                     (module grant is a prerequisite for any screen permission inside it —
                      no orphan screen permission, per domain-profile.md two-tier rule)

Tier 2 — Screen registration:
  SEC_PAGES : one row for the composite screen —
              page_code = MDM_LOOKUP, page_name = إدارة القوائم المرجعية, module_fk = MDM
  PERMISSIONS (4 rows, CORE-9):
    ────────────────────────────────────────────────────────
    Permission Name              │ Roles Assigned
    ─────────────────────────────┼──────────────────────────
    PERM_MDM_LOOKUP_VIEW         │ R1, R2
    PERM_MDM_LOOKUP_CREATE       │ R1, R2
    PERM_MDM_LOOKUP_UPDATE       │ R1, R2
    PERM_MDM_LOOKUP_DELETE       │ R1, R2
    ────────────────────────────────────────────────────────
  Column names for SEC_MODULE / SEC_ROLE_MODULE / SEC_PAGES / PERMISSIONS come from
  db-script-SEC.md (owning module) — not redefined here; MDM only supplies the seed row values.

SEC-BE Governance Rules:
  SEC-IMPL-RULE-1 — Every SCR-ID (here: SCR-MDM-001) has permission verification enforced at
                    the API level — API-MDM-001..010, no exceptions.
  SEC-IMPL-RULE-3 — HTTP 403 responses mapped via LocalizedException.
  SEC-IMPL-RULE-4 — SCR-MDM-001 verified in SEC_PAGES before launch.

Note: canView/canCreate/canEdit/canDelete UI-level behavior is out of scope for this backend-only
pass — specified later in PROJECT-3-FRONTEND-ENGINE.md Phase SEC-FE, consuming this same seed data.
