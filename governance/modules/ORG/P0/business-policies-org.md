## BUSINESS POLICIES — ORGANIZATION
══════════════════════════════════════════════════════════════════
Module      : Organization (1.1)
P0 Date     : 2026-06-28  (UPDATED — reflects master-registry v2.7.4 + security-registry v2.2.0)
ERP Pattern : platform-standards.md Section M.1
DB_TARGET   : POSTGRESQL_16
Pipeline    : GOVERNED ✓ MODE 2 — ALIGN GATE PASSED ✓
P1 reads    : CLIENT-SPECIFIC entries → RULE-IDs marked "Source: Client"
              Standard ERP rules → applied by P1 Section 5.4.2 directly
══════════════════════════════════════════════════════════════════

CLIENT-SPECIFIC POLICIES
──────────────────────────────────────────────────────────────────
None stated — standard ERP rules apply via P1 Section 5.4.2.

No client-specific policies were provided in vision.md for Organization.
All structural rules (immutable codes, deactivation blocks, tree integrity)
are standard and already formalized in srs-org-001.md RULE-IDs.
──────────────────────────────────────────────────────────────────

CUSTOM LOV VALUES
──────────────────────────────────────────────────────────────────
No values beyond Section M.1 were stated in vision text.
Standard seed values (already implemented in DBS-ORG-001):

MD_LOOKUP_DETAIL seeds (consumed via lookup_key contract):
  LEGAL_ENTITY_TYPE  : Head Office / Branch Office / Subsidiary / Representative Office
  BRANCH_TYPE        : Main Branch / Sub-Branch / Operations Branch / Admin Branch
  DEPARTMENT_NODE_TYPE : SUMMARY / DETAIL
  COST_CENTER_NODE_TYPE : SUMMARY / DETAIL
  COST_CENTER_TYPE   : Direct / Indirect / Shared
  LOCATION_SITE_TYPE : Office / Warehouse / Factory / Site / Retail

PG column actuals for seed inserts:
  MD_LOOKUP_DETAIL: id_pk (BIGINT), master_lookup_id_fk (BIGINT),
  code (VARCHAR), name_ar (VARCHAR), name_en (VARCHAR),
  extra_value (VARCHAR), sort_order (INTEGER), is_active (SMALLINT DEFAULT 1)

ORG_REGION_TYPE (Reference Table — separate DB table in DBS-ORG-001):
  Initial values : GEOGRAPHIC / SALES / OPERATIONAL
  Extensible     : YES — Admin may add rows at runtime
  Not a Lookup Detail — no lookup_key contract; consumed directly via FK
──────────────────────────────────────────────────────────────────

SCOPE EXCEPTIONS
──────────────────────────────────────────────────────────────────
None — Section M.1 scope applies in full.

All 7 entities in scope and governed (DBS-ORG-001):
  LegalEntity, Branch, Region, Department,
  CostCenter, ProfitCenter, LocationSite

Confirmed out of scope for Organization:
  BusinessUnit  — not in master-registry Section 5 — DEFERRED
  Country       — owned by MasterData (1.4)
  Warehouse     — owned by Inventory (3.2)
                  (LocationSite is the L1 physical site concept;
                   Warehouse is an Inventory-level operational entity)
──────────────────────────────────────────────────────────────────

PLATFORM INTEGRATION NOTES
──────────────────────────────────────────────────────────────────
Database target:
  All ORG DDL is POSTGRESQL_16 (system-wide PG migration 2026-06-28).
  VARCHAR(N) not VARCHAR2. SMALLINT not NUMBER(1). BIGINT PKs with sequences.
  CORE-8 PG rules enforced in DBS-ORG-001.

SEC_PAGES seeding:
  PERM_LEGAL_ENTITY_VIEW confirmed in SecurityPermissions.java
  (security-registry v2.2.0 Section 5.1).
  All ORG screens seed into SEC_PAGES via POST /api/pages at build time.
  Security owns the table; ORG owns its rows (MODULE = 'ORGANIZATION').
  Auto-generated permissions per page: VIEW / CREATE / UPDATE / DELETE.
  SEC_PAGES PG types: ID_PK BIGINT, PAGE_CODE VARCHAR(50),
  NAME_AR/NAME_EN VARCHAR(100), ROUTE VARCHAR(200),
  DISPLAY_ORDER BIGINT, IS_ACTIVE SMALLINT, PARENT_ID_FK BIGINT.

Lookup consumption:
  All 6 ORG lookup keys consumed via GET /api/lookups/{lookup_key}?active=true.
  Direct DB queries to MD_MASTER_LOOKUP / MD_LOOKUP_DETAIL: PROHIBITED.
  PG actual columns: lookup_key, name_ar, name_en, code, is_active, id_pk.

DataScope / Security:
  ORG_BRANCH is the primary DataScope boundary.
  Row-Level Security delegated entirely to Security module.
  ORG module does not implement its own scope filter.
  No tenant context — system is permanently single-tenant
  (TENANT_ID removed 2026-06-21 — Conflict #17 CLOSED).
──────────────────────────────────────────────────────────────────

══════════════════════════════════════════════════════════════════
