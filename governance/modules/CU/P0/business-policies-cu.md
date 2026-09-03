## BUSINESS POLICIES — COMMON UTILS
══════════════════════════════════════════════════════════════════
Module      : Common Utils (CU)
P0 Date     : 2026-09-01
Domain KB   : none supplied — derived from domain-profile-ERP.md
P1 reads    : CLIENT-SPECIFIC entries → RULE-IDs marked "Source: Client"
              Standard rules → applied by P1 directly
══════════════════════════════════════════════════════════════════

CLIENT-SPECIFIC POLICIES
──────────────────────────────────────────────────────────────────
These are cross-cutting DESIGN policies from domain-profile-ERP.md that
CU, as the shared foundation layer, is the natural home for. P1 converts
them into RULE-IDs where they become enforceable, and otherwise carries
them as design constraints on every Foundation module.

POLICY-CLI-01: Medium complexity — no over-engineering
  Rule   : Analysis and components MUST prefer the simplest solution that
           satisfies the requirement; avoid excess structure/abstraction.
  Trigger: All design/analysis phases (P1→P3) for every Foundation module.
  Source : User stated in domain-profile GOVERNING RULES.

POLICY-CLI-02: Reusable + Configurable + Integrable + Composable by design
  Rule   : Every Foundation module MUST be usable standalone, configurable,
           integrable with external systems, and composable with others —
           via clear, uniform contracts (APIs/events) with minimal glue code
           and no heavy integration framework.
  Trigger: Contract/API/event design for every Foundation module.
  Source : User stated in domain-profile GOVERNING RULES.

POLICY-CLI-03: Full independent build — no partial/deferred work
  Rule   : Each module MUST be developed completely and independently; no
           deferred sub-parts within the Foundation scope.
  Trigger: Scope planning for every Foundation module.
  Source : User stated in domain-profile GOVERNING RULES (confirmed 2026-09-01).

──────────────────────────────────────────────────────────────────
CUSTOM LOV VALUES
──────────────────────────────────────────────────────────────────
None — CU owns no LOVs by default (see module-registry-CU.md).

──────────────────────────────────────────────────────────────────
SCOPE EXCEPTIONS
──────────────────────────────────────────────────────────────────
None — Common Utils is fully in scope and built in full. Business modules
(Accounting/HR/E-Commerce/…) are a separate future domain, not a CU exclusion.
══════════════════════════════════════════════════════════════════
