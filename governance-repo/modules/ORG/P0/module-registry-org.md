## MODULE REGISTRY — ORGANIZATION
══════════════════════════════════════════════════════════════════
Module Name    : Organization
Module Code    : ORG
Layer          : L1
Type           : Master Data (Core — Foundation)
Execution Tier : L1-1 (Step 1 of Layer 1 — no prerequisites)
P0 Date        : 2026-06-23  (REGENERATED — replaces 2026-06-16 original)
Readiness      : READY ✓
ERP Pattern    : platform-standards.md Section M.1
Source         : REGENERATED — rebuilt from master-registry v2.7.2 + Section M.1
                 (prior registry-srs-ORG.md / registry-db-ORG.md artifacts NOT uploaded —
                  clean rebuild from authoritative registry + vision + governance docs)
══════════════════════════════════════════════════════════════════

ENTITIES OWNED
──────────────────────────────────────────────────────────────────
LegalEntity    │ Master Data  │ SHARED (owner)
Branch         │ Master Data  │ SHARED (owner)
Region         │ Master Data  │ SHARED (owner)
Department     │ Master Data  │ SHARED (owner)    [tree — self-reference]
CostCenter     │ Master Data  │ SHARED (owner)    [tree — self-reference]
ProfitCenter   │ Master Data  │ SHARED (owner)
LocationSite   │ Master Data  │ SHARED (owner)
──────────────────────────────────────────────────────────────────
7 entities — all SHARED: every downstream module consumes via FK.
Note: Entity names only — ENTITY-IDs assigned by P1, not here.

DB TABLE NAMES (confirmed in master-registry v2.7.2 Section 5 — GOVERNED ✓)
──────────────────────────────────────────────────────────────────
LegalEntity   → ORG_LEGAL_ENTITY
Branch        → ORG_BRANCH
Region        → ORG_REGION
Department    → ORG_DEPARTMENT
CostCenter    → ORG_COST_CENTER
ProfitCenter  → ORG_PROFIT_CENTER
LocationSite  → ORG_LOCATION_SITE
──────────────────────────────────────────────────────────────────
DBS-ORG-001 confirmed in master-registry (GOVERNED ✓ MODE 1.5 status).
These table names are LOCKED — P1/P2 must not rename them.

ENTITY RELATIONSHIPS
──────────────────────────────────────────────────────────────────
LegalEntity    ROOT — no parent
Branch         → LegalEntity    1:M  NOT NULL  RESTRICT
Region         → LegalEntity    1:M  NOT NULL  RESTRICT
Department     → Branch         1:M  NOT NULL  RESTRICT
               → Department     self-ref  NULLABLE  (parent_departmentFk)
               → node_type: SUMMARY | DETAIL
CostCenter     → Branch         1:M  NOT NULL  RESTRICT
               → CostCenter     self-ref  NULLABLE  (parent_costCenterFk)
               → node_type: SUMMARY | DETAIL
ProfitCenter   → LegalEntity    1:M  NOT NULL  RESTRICT
LocationSite   → Branch         1:M  NOT NULL  RESTRICT
──────────────────────────────────────────────────────────────────
Delete behavior: RESTRICT on all. No CASCADE, no SET NULL on business data.
DEACTIVATE pattern: soft-delete (isActiveFl) — parent cannot deactivate if
children are active (enforced via operational rules below).

REFERENCE TABLE (NOT a Lookup Detail)
──────────────────────────────────────────────────────────────────
ORG_REGION_TYPE  │ Owner: Organization  │ > 15 values → Reference Table
                 │ Initial values: GEOGRAPHIC / SALES / OPERATIONAL
                 │ Extensible by Admin
                 │ Source: master-registry Section 5 Organization Reference Table Note
──────────────────────────────────────────────────────────────────
This is a separate DB table, NOT stored in MD_LOOKUP_DETAIL.
All other ORG lookups use MD_LOOKUP_DETAIL via lookupKey.

LOVs OWNED (stored in MD_LOOKUP_DETAIL via lookupKey)
──────────────────────────────────────────────────────────────────
LEGAL_ENTITY_TYPE    │ LegalEntity.entityTypeId   │ Lookup  │ Head Office / Branch Office / Subsidiary / Representative Office
BRANCH_TYPE          │ Branch.branchTypeId         │ Lookup  │ Main Branch / Sub-Branch / Operations Branch / Admin Branch
DEPARTMENT_NODE_TYPE │ Department.nodeTypeId       │ Lookup  │ SUMMARY / DETAIL
COST_CENTER_NODE_TYPE│ CostCenter.nodeTypeId       │ Lookup  │ SUMMARY / DETAIL
COST_CENTER_TYPE     │ CostCenter.costCenterTypeId │ Lookup  │ Direct / Indirect / Shared
LOCATION_SITE_TYPE   │ LocationSite.siteTypeId     │ Lookup  │ Office / Warehouse / Factory / Site / Retail
──────────────────────────────────────────────────────────────────
6 lookup keys confirmed in master-registry Section 6.
Note: REGION_TYPE is a Reference Table (separate row above) — not a lookup.
Note: LOV-IDs assigned by P1, not here.

LOVs CONSUMED (from other modules)
──────────────────────────────────────────────────────────────────
None — Organization is ROOT. It consumes no external LOVs.
──────────────────────────────────────────────────────────────────

SHARED ENTITIES CONSUMED
──────────────────────────────────────────────────────────────────
None — Organization is ROOT. It consumes no external entities.
──────────────────────────────────────────────────────────────────

DEPENDENCIES
──────────────────────────────────────────────────────────────────
ROOT : YES — zero outbound dependencies confirmed (master-registry Section 7)
──────────────────────────────────────────────────────────────────

OUTGOING — WHO CONSUMES ORGANIZATION
──────────────────────────────────────────────────────────────────
Security (1.2)           │ HARD-FK  │ Branch via SEC_ROLE_BRANCH (M:M)
MasterData (1.4)         │ HARD-FK  │ LegalEntity, Branch
CurrencyCalendar (1.5)   │ HARD-FK  │ LegalEntity (fiscal year scope)
NumberingEngine (1.6)    │ HARD-FK  │ Branch (numbering scope)
FileService (1.10)       │ HARD-FK  │ Branch (scope)
NotificationService (1.8)│ HARD-FK  │ Branch (scope)
AuditService (1.9)       │ HARD-FK  │ Branch (scope)
All Layer-2 modules      │ HARD-FK  │ Branch, CostCenter (operational context)
All Layer-3 modules      │ HARD-FK  │ Branch, Department, CostCenter, ProfitCenter
Inventory (3.2)          │ HARD-FK  │ LocationSite (warehouse context)
Finance (3.4)            │ HARD-FK  │ CostCenter, ProfitCenter
──────────────────────────────────────────────────────────────────

STATUS LIFECYCLES
──────────────────────────────────────────────────────────────────
All 7 entities: is_active flag only (isActiveFl — NUMBER(1) default 1)
  Active (1) ↔ Inactive (0) — no additional states
  No workflow, no approval, no terminal states
  Deactivation blocked by operational rules below
──────────────────────────────────────────────────────────────────

OPERATIONAL RULES
──────────────────────────────────────────────────────────────────
RULE-ORG-01  : MUST prevent deactivation of LegalEntity with active Branches
RULE-ORG-02  : MUST prevent deactivation of LegalEntity with active ProfitCenters
RULE-ORG-03  : MUST prevent deactivation of Branch with active Departments
RULE-ORG-04  : MUST prevent deactivation of Branch with active CostCenters
RULE-ORG-05  : MUST prevent deactivation of Branch with active LocationSites
RULE-ORG-06  : MUST prevent deactivation of Region with active Branches referencing it
RULE-ORG-07  : MUST prevent circular parent reference in Department tree
RULE-ORG-08  : MUST prevent circular parent reference in CostCenter tree
RULE-ORG-09  : MUST prevent Department with node_type=SUMMARY from being assigned
               directly to transactional records (enforced by consumers — documented here)
RULE-ORG-10  : MUST prevent CostCenter with node_type=SUMMARY from being assigned
               directly to transactional records (enforced by consumers — documented here)
RULE-ORG-11  : Business codes (legalEntityCode, branchCode, deptCode, costCenterCode,
               profitCenterCode, locationCode) MUST be immutable after first save
RULE-ORG-12  : Business codes MUST be unique within their defined scope
               (LegalEntity: global | Branch: per LegalEntity |
                Department: per Branch | CostCenter: per Branch |
                ProfitCenter: per LegalEntity | Region: per LegalEntity |
                LocationSite: per Branch)
──────────────────────────────────────────────────────────────────
Note: RULE-ORG-IDs are P0 labels — P1 assigns formal RULE-IDs in srs.md.

PLATFORM CONVENTIONS
──────────────────────────────────────────────────────────────────
Audit Trail         : YES — all 7 entities (createdAt, createdBy, updatedAt, updatedBy)
Soft Delete         : YES — isActiveFl for all 7 entities
Business Code       : YES — all 7 entities carry an immutable code field
File Attachments    : NOT APPLICABLE — structural master data
Document Numbering  : NOT APPLICABLE — no transactional documents
Notifications       : NO — structural changes do not trigger notifications
──────────────────────────────────────────────────────────────────

NAMING CONVENTION COMPLIANCE
──────────────────────────────────────────────────────────────────
All ORG entities follow master-registry Section 4 standards:
  Primary keys    : end with Pk         (e.g. legalEntityPk)
  Foreign keys    : end with Fk         (e.g. legalEntityFk, parentDepartmentFk)
  Dropdown fields : end with Id         (e.g. entityTypeId, branchTypeId)
  Flag fields     : end with Fl         (e.g. isActiveFl)
No naming exceptions — Organization is a fully governed, new-build module.
──────────────────────────────────────────────────────────────────

SECURITY INTEGRATION NOTE
──────────────────────────────────────────────────────────────────
Per master-registry Section 4 + security-registry v2.0.0:
  PERM_LEGAL_ENTITY_VIEW is seeded in Security's SEC_PAGES.
  → ORG seeds its pages into SEC_PAGES at build time (PAGE_CODE per entity/screen)
  → Confirmed from vision.md 1.1 note: "يُؤخذ AS-IS كجزء من SEC_PAGES seeding"
  → ORG does not own SEC_PAGES rows — Security owns the table, ORG seeds into it
──────────────────────────────────────────────────────────────────

AUTO-DECISIONS
──────────────────────────────────────────────────────────────────
AUTO: 7 entities — LegalEntity, Branch, Region, Department, CostCenter,
      ProfitCenter, LocationSite
FROM: master-registry v2.7.2 Section 5 (GOVERNED ✓ — DBS-ORG-001)
IF WRONG: flag as conflict in master-registry Section 13 before P1

AUTO: Region entity retained (not deferred)
FROM: master-registry Section 5 — ORG_REGION table is GOVERNED ✓
      Despite AQ-003 (consumers TBD), Region exists and is owned by ORG
IF WRONG: would require removing ORG_REGION from DBS-ORG-001 — major change

AUTO: ORG_REGION_TYPE as Reference Table (not Lookup Detail)
FROM: master-registry Section 5 Organization Reference Table Note —
      explicitly declared as > 15 values → Reference Table
IF WRONG: change to Lookup Detail in master-registry Section 6 first

AUTO: Department and CostCenter as tree structures (self-reference)
FROM: Section M.1 + vision.md 1.1 (Departments listed as hierarchical) +
      master-registry Section 6 DEPARTMENT_NODE_TYPE / COST_CENTER_NODE_TYPE lookups
IF WRONG: remove self-reference FK and node_type field — notify all consumers

AUTO: LocationSite → Branch (flat, not tree)
FROM: Section M.1 "LocationSite as flat Day 1 entity" + vision.md 1.1 listing
      "المواقع والمستودعات" as a single flat concept
IF WRONG: add self-reference to LocationSite — notify Inventory module (primary consumer)

AUTO: ProfitCenter → LegalEntity (not Branch)
FROM: Section M.1 pattern + master-registry Section 5 (ProfitCenter owner = Organization)
      ProfitCenter is a financial reporting entity at legal-entity level
IF WRONG: change parent FK to Branch — notify Finance module

AUTO: All entities use isActiveFl (soft delete) — no status lifecycle
FROM: Section M.1 "Master Data → is_active only" + Section M.0 universal defaults
IF WRONG: define state machine and notify P1

AUTO: No approval flow for any ORG entity
FROM: RULE-13 (shared-governance-rules.md) — workflow deferred by default
      No explicit approval request in vision.md for Organization
IF WRONG: explicitly request per-entity approval in business-policies-ORG.md
──────────────────────────────────────────────────────────────────

INF-IDs
──────────────────────────────────────────────────────────────────
INF-ORG-01  │ PERM_LEGAL_ENTITY_VIEW seed exists in Security's SEC_PAGES
             │ Implies ORG had partial SEC_PAGES seeding done before formal P0
             │ Risk: Other ORG SEC_PAGES seeds may already exist in Security —
             │       P1 must audit existing PAGE_CODE entries for ORG module
             │       before generating new page seeds to avoid duplicates
             │ Source: vision.md 1.1 security note + security-registry v2.0.0
──────────────────────────────────────────────────────────────────

OPEN AQ-IDs (from master-registry Section 14)
──────────────────────────────────────────────────────────────────
AQ-003  │ DEFERRED — non-blocking
         │ Which modules consume ORG_REGION via SOFT-READ?
         │ Resolves automatically when first consuming module runs MODE 1.5
         │ ORG P0 READY status unaffected — P1 may proceed
──────────────────────────────────────────────────────────────────

══════════════════════════════════════════════════════════════════
Readiness : READY ✓
            AQ-003 is DEFERRED and non-blocking (master-registry Section 14 note)
            INF-ORG-01 is non-blocking (P1 converts to OQ if unresolved at MODE 1 start)
══════════════════════════════════════════════════════════════════
