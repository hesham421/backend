## BUSINESS POLICIES — ORGANIZATION
══════════════════════════════════════════════════════════════════
Module      : Organization (1.1)
P0 Date     : 2026-06-23  (REGENERATED — replaces 2026-06-16 original)
ERP Pattern : platform-standards.md Section M.1
P1 reads    : CLIENT-SPECIFIC entries → RULE-IDs marked "Source: Client"
              Standard ERP rules → applied by P1 Section 5.4.2 directly
══════════════════════════════════════════════════════════════════

CLIENT-SPECIFIC POLICIES
──────────────────────────────────────────────────────────────────
None stated — standard ERP rules apply via P1 Section 5.4.2.

No client-specific policies were provided in the vision document
for the Organization module. All structural rules (immutable codes,
deactivation blocks, tree integrity) are standard and applied
automatically by P1 from Section M.1 + M.0.
──────────────────────────────────────────────────────────────────

CUSTOM LOV VALUES
──────────────────────────────────────────────────────────────────
No values beyond Section M.1 were stated in vision text.
Standard initial values apply per module-registry-ORG.md LOVs OWNED section.

Values to seed at build time (confirmed from registry):

LEGAL_ENTITY_TYPE  : Head Office / Branch Office / Subsidiary / Representative Office
BRANCH_TYPE        : Main Branch / Sub-Branch / Operations Branch / Admin Branch
DEPARTMENT_NODE_TYPE : SUMMARY / DETAIL
COST_CENTER_NODE_TYPE : SUMMARY / DETAIL
COST_CENTER_TYPE   : Direct / Indirect / Shared
LOCATION_SITE_TYPE : Office / Warehouse / Factory / Site / Retail

ORG_REGION_TYPE (Reference Table — not Lookup Detail):
  Initial values : GEOGRAPHIC / SALES / OPERATIONAL
  Extensible     : YES — Admin may add values at runtime
──────────────────────────────────────────────────────────────────

SCOPE EXCEPTIONS
──────────────────────────────────────────────────────────────────
None — Section M.1 scope applies fully.

All 7 entities in scope: LegalEntity, Branch, Region, Department,
CostCenter, ProfitCenter, LocationSite.

Items NOT deferred (confirmed active in master-registry DBS-ORG-001):
  Region       — retained despite AQ-003 (consumers TBD)
  LocationSite — confirmed in scope per master-registry Section 5

Items confirmed out of scope for Organization module:
  BusinessUnit     — DEFERRED (INF-001, closed as DEFERRED in prior session)
  Country          — owned by MasterData (1.4), not Organization
  Warehouse        — owned by Inventory (3.2), not Organization
                     (LocationSite serves as the physical site concept at L1;
                      Warehouse is an Inventory-level operational entity)
──────────────────────────────────────────────────────────────────

INTEGRATION NOTES FOR P1
──────────────────────────────────────────────────────────────────
SEC_PAGES seeding:
  Organization seeds its screens into Security's SEC_PAGES table.
  PERM_LEGAL_ENTITY_VIEW already confirmed seeded (security-registry v2.0.0).
  P1 must inventory all existing ORG-related PAGE_CODE entries in SEC_PAGES
  before generating new ones — see INF-ORG-01 in module-registry-ORG.md.
  Do not create duplicate SEC_PAGES rows for screens already seeded.

DataScope (Security 1.2):
  Row-Level Security for ORG data enforced by Security module.
  Branch is the primary data-scope level (per AD locked in SEC session).
  Organization module does not implement its own scope filter —
  it relies on Security's DataScope mechanism.
──────────────────────────────────────────────────────────────────

══════════════════════════════════════════════════════════════════
