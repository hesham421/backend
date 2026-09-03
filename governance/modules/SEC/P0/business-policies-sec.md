## BUSINESS POLICIES — SECURITY
══════════════════════════════════════════════════════════════════
Module      : Security (SEC)
P0 Date     : 2026-09-01
Domain KB   : none supplied — derived from domain-profile-ERP.md
P1 reads    : CLIENT-SPECIFIC entries → RULE-IDs marked "Source: Client"
              Standard auth rules → applied by P1 directly
══════════════════════════════════════════════════════════════════

CLIENT-SPECIFIC POLICIES
──────────────────────────────────────────────────────────────────
No Security-specific client policies were stated. Standard authentication
rules (password complexity, token TTLs, refresh rotation, lockout on
repeated failures, single active reset/activation token) are applied by
P1 as standard defaults — not client overrides. The cross-cutting design
policies (POLICY-CLI-01..03 in business-policies-CU.md: medium complexity;
reusable/configurable/integrable/composable; full independent build) apply
to Security as to every Foundation module.

If specific values are wanted (e.g. token TTL, password min length,
lockout threshold), state them and P1 will bind them as Source: Client.

──────────────────────────────────────────────────────────────────
CUSTOM LOV VALUES
──────────────────────────────────────────────────────────────────
None stated — permission TYPE is a CORE-9 code convention, not a DB LOV.

──────────────────────────────────────────────────────────────────
SCOPE EXCEPTIONS
──────────────────────────────────────────────────────────────────
Excluded : Role-based branch/organization DataScope (reference
           SecRoleBranch / ENTITY-SEC-010). It depends on an Organization
           module, which is out of the Foundation domain. Business domains
           that need row-level scoping introduce their own scope dimension
           later and compose it with this auth core.
Excluded : Multi-tenant scoping — platform is single-domain foundation;
           no tenant concept in the auth core.
══════════════════════════════════════════════════════════════════
