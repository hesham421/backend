## MODULE REGISTRY — ORGANIZATION
══════════════════════════════════════════════════════════════════
Module Name    : Organization
Module Code    : ORG
Layer          : L1
Type           : Master Data (Core — Foundation)
Execution Tier : L1-1 (Step 1 of Layer 1 — no prerequisites)
P0 Date        : 2026-06-28  (UPDATED — reflects master-registry v2.7.4 + security-registry v2.2.0)
Readiness      : READY ✓
Pipeline State : GOVERNED ✓ MODE 2 — ALIGN GATE PASSED ✓
                 Next stage: MODE 2.5 → test-plan-org-001.md
DB_TARGET      : POSTGRESQL_16  (system-wide PG migration confirmed 2026-06-28)
ERP Pattern    : platform-standards.md Section M.1
Source         : UPDATED — rebuilt from master-registry v2.7.4 + security-registry v2.2.0
══════════════════════════════════════════════════════════════════

PIPELINE STATUS
──────────────────────────────────────────────────────────────────
P0   COMPLETE  : module-registry-ORG.md + business-policies-ORG.md  ✓
P1   COMPLETE  : srs-org-001.md v1.0 — 8 entities / 20 rules / 47 APIs / 7 screens  ✓
P2   COMPLETE  : DBS-ORG-001 — 8 tables / 102 DBF-IDs  ✓
P3   COMPLETE  : PLAN-ORG-001 — ALIGN GATE PASSED ✓  ✓
P2.5 PENDING   : test-plan-org-001.md  ← NEXT
P4   PENDING   : MODE 4A — Governance Audit Engine
──────────────────────────────────────────────────────────────────
Note: SRS entity count updated — master-registry v2.7.4 shows 8 entities
(previously 7 in v2.7.0 note). DBS-ORG-001 = 8 tables confirmed.

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
7 entities declared in P0. 8th entity in DBS-ORG-001 is the
ORG_REGION_TYPE reference table (treated as configuration, not a
named entity in this registry — governed separately below).
Note: ENTITY-IDs assigned by P1 (srs-org-001.md) — not reproduced here.

DB TABLE NAMES (confirmed GOVERNED ✓ — DBS-ORG-001 / master-registry v2.7.4 Section 5)
──────────────────────────────────────────────────────────────────
LegalEntity   → ORG_LEGAL_ENTITY
Branch        → ORG_BRANCH
Region        → ORG_REGION
Department    → ORG_DEPARTMENT
CostCenter    → ORG_COST_CENTER
ProfitCenter  → ORG_PROFIT_CENTER
LocationSite  → ORG_LOCATION_SITE
[8th table]   → ORG_REGION_TYPE  (reference table — see below)
──────────────────────────────────────────────────────────────────
DB_TARGET: POSTGRESQL_16. All DDL in DBS-ORG-001 uses PG syntax:
  PKs          : BIGINT with sequences (not IDENTITY — per CORE-8 rule)
  Flag fields  : SMALLINT DEFAULT 1 (not BOOLEAN — per CORE-8 rule)
  String fields: VARCHAR(N) (not VARCHAR2 — PG dialect)
  Audit fields : TIMESTAMP
These table names are LOCKED — no rename permitted without AQ + conflict log.

ENTITY RELATIONSHIPS
──────────────────────────────────────────────────────────────────
LegalEntity    ROOT — no parent
Branch         → LegalEntity    1:M  NOT NULL FK  RESTRICT
Region         → LegalEntity    1:M  NOT NULL FK  RESTRICT
Department     → Branch         1:M  NOT NULL FK  RESTRICT
               → Department     self-ref  NULLABLE FK  (parent_department_fk)
               → node_type_id: SUMMARY | DETAIL
CostCenter     → Branch         1:M  NOT NULL FK  RESTRICT
               → CostCenter     self-ref  NULLABLE FK  (parent_cost_center_fk)
               → node_type_id: SUMMARY | DETAIL
ProfitCenter   → LegalEntity    1:M  NOT NULL FK  RESTRICT
LocationSite   → Branch         1:M  NOT NULL FK  RESTRICT
──────────────────────────────────────────────────────────────────
Delete behavior: RESTRICT on all. No CASCADE or SET NULL on business entities.
Soft-delete via is_active_fl (SMALLINT 0/1). Deactivation blocked when active
children exist (enforced at application layer — see rules below).

FK naming: all FK columns end with _fk per master-registry Section 4.
  Examples: legal_entity_fk, parent_department_fk, parent_cost_center_fk

REFERENCE TABLE (separate from Lookup Detail)
──────────────────────────────────────────────────────────────────
ORG_REGION_TYPE  │ DB Table — GOVERNED ✓ under DBS-ORG-001
                 │ > 15 values → Reference Table (not MD_LOOKUP_DETAIL)
                 │ Owner: Organization
                 │ Used by: ORG_REGION.region_type_id_fk
                 │ Initial values: GEOGRAPHIC / SALES / OPERATIONAL
                 │ Extensible by Admin at runtime
                 │ Source: master-registry v2.7.4 Section 5 + Section 6
──────────────────────────────────────────────────────────────────

LOVs OWNED (consumed via MD_LOOKUP_DETAIL — lookup_key is the stable contract)
──────────────────────────────────────────────────────────────────
LEGAL_ENTITY_TYPE    │ LegalEntity.entity_type_id    │ Lookup  │ Head Office / Branch Office / Subsidiary / Representative Office
BRANCH_TYPE          │ Branch.branch_type_id          │ Lookup  │ Main Branch / Sub-Branch / Operations Branch / Admin Branch
DEPARTMENT_NODE_TYPE │ Department.node_type_id        │ Lookup  │ SUMMARY / DETAIL
COST_CENTER_NODE_TYPE│ CostCenter.node_type_id        │ Lookup  │ SUMMARY / DETAIL
COST_CENTER_TYPE     │ CostCenter.cost_center_type_id │ Lookup  │ Direct / Indirect / Shared
LOCATION_SITE_TYPE   │ LocationSite.site_type_id      │ Lookup  │ Office / Warehouse / Factory / Site / Retail
──────────────────────────────────────────────────────────────────
6 lookup keys registered in master-registry v2.7.4 Section 6 (LOV-ORG-001..006).
REGION_TYPE is a Reference Table — separate entry above, not in this list.
Consumption: GET /api/lookups/{lookup_key}?active=true
Direct DB query of MD_MASTER_LOOKUP / MD_LOOKUP_DETAIL: PROHIBITED.
PG column actuals: lookup_key (VARCHAR), name_ar (VARCHAR), name_en (VARCHAR),
  code (VARCHAR), extra_value (VARCHAR), is_active (SMALLINT), id_pk (BIGINT).
Note: LOV-IDs assigned by P1 (srs-org-001.md) — not reproduced here.

LOVs CONSUMED (from other modules)
──────────────────────────────────────────────────────────────────
None — Organization is ROOT. Consumes no external LOVs.
──────────────────────────────────────────────────────────────────

SHARED ENTITIES CONSUMED
──────────────────────────────────────────────────────────────────
None — Organization is ROOT. Zero outbound XM dependencies confirmed
(master-registry v2.7.4 Section 3 + Section 7).
──────────────────────────────────────────────────────────────────

DEPENDENCIES
──────────────────────────────────────────────────────────────────
ROOT : YES — no external dependencies
──────────────────────────────────────────────────────────────────

OUTGOING — WHO CONSUMES ORGANIZATION
──────────────────────────────────────────────────────────────────
Security (1.2)           │ HARD-FK  │ ORG_BRANCH — DataScope / SEC_ROLE_BRANCH
MasterData (1.4)         │ HARD-FK  │ ORG_LEGAL_ENTITY, ORG_BRANCH
CurrencyCalendar (1.5)   │ HARD-FK  │ ORG_LEGAL_ENTITY (fiscal year scope)
NumberingEngine (1.6)    │ HARD-FK  │ ORG_BRANCH (numbering scope)
FileService (1.10)       │ HARD-FK  │ ORG_BRANCH (access scope)
NotificationService (1.8)│ HARD-FK  │ ORG_BRANCH (scope)
AuditService (1.9)       │ HARD-FK  │ ORG_BRANCH (scope)
All Layer-2 modules      │ HARD-FK  │ ORG_BRANCH, ORG_COST_CENTER
All Layer-3 modules      │ HARD-FK  │ ORG_BRANCH, ORG_DEPARTMENT, ORG_COST_CENTER, ORG_PROFIT_CENTER
Inventory (3.2)          │ HARD-FK  │ ORG_LOCATION_SITE (physical site context)
Finance (3.4)            │ HARD-FK  │ ORG_COST_CENTER, ORG_PROFIT_CENTER
──────────────────────────────────────────────────────────────────
Note: Cross-module FK naming follows CORE-8 SOFT-READ rule —
  cross-module FKs are validated at service layer, not DB-level.
  Convention: suffix _fk (e.g., branch_fk, cost_center_fk, location_site_fk).

STATUS LIFECYCLES
──────────────────────────────────────────────────────────────────
All 7 entities: is_active_fl flag only (SMALLINT DEFAULT 1 — PG)
  Active (1) ↔ Inactive (0) — no additional states
  No workflow, no approval, no terminal states
  Deactivation blocked by operational rules — see below
──────────────────────────────────────────────────────────────────

OPERATIONAL RULES (P0 labels — formal RULE-IDs in srs-org-001.md)
──────────────────────────────────────────────────────────────────
RULE-ORG-01  : MUST prevent deactivation of LegalEntity with active Branches
RULE-ORG-02  : MUST prevent deactivation of LegalEntity with active ProfitCenters
RULE-ORG-03  : MUST prevent deactivation of Branch with active Departments
RULE-ORG-04  : MUST prevent deactivation of Branch with active CostCenters
RULE-ORG-05  : MUST prevent deactivation of Branch with active LocationSites
RULE-ORG-06  : MUST prevent deactivation of Region with active Branches referencing it
RULE-ORG-07  : MUST prevent circular parent reference in Department tree
RULE-ORG-08  : MUST prevent circular parent reference in CostCenter tree
RULE-ORG-09  : SUMMARY Department MUST NOT be directly assigned to transactional records
               (enforced at application layer in consuming modules)
RULE-ORG-10  : SUMMARY CostCenter MUST NOT be directly assigned to transactional records
               (enforced at application layer in consuming modules)
RULE-ORG-11  : Business codes MUST be immutable after first save
RULE-ORG-12  : Business codes MUST be unique within their defined scope:
               LegalEntity: global | Branch: per LegalEntity |
               Department: per Branch | CostCenter: per Branch |
               ProfitCenter: per LegalEntity | Region: per LegalEntity |
               LocationSite: per Branch
──────────────────────────────────────────────────────────────────

NAMING CONVENTION COMPLIANCE
──────────────────────────────────────────────────────────────────
All ORG entities follow master-registry Section 4 standards (PG snake_case):
  Primary keys    : end with _pk    (e.g. legal_entity_pk  BIGINT)
  Foreign keys    : end with _fk    (e.g. legal_entity_fk, parent_department_fk)
  Dropdown fields : end with _id    (e.g. entity_type_id, branch_type_id)
  Flag fields     : end with _fl    (e.g. is_active_fl  SMALLINT DEFAULT 1)
  Audit fields    : created_at, created_by, updated_at, updated_by (TIMESTAMP / VARCHAR)
No naming exceptions — Organization is fully governed, new-build module.
──────────────────────────────────────────────────────────────────

PLATFORM CONVENTIONS
──────────────────────────────────────────────────────────────────
Audit Trail         : YES — all 7 entities (created_at, created_by, updated_at, updated_by)
Soft Delete         : YES — is_active_fl (SMALLINT 0/1) for all 7 entities
Business Code       : YES — all 7 entities carry an immutable code field
File Attachments    : NOT APPLICABLE — structural master data
Document Numbering  : NOT APPLICABLE — no transactional documents
Notifications       : NO — structural changes do not trigger notifications
Lookup Consumption  : Via GET /api/lookups/{lookup_key}?active=true — never direct DB query
──────────────────────────────────────────────────────────────────

SECURITY INTEGRATION
──────────────────────────────────────────────────────────────────
SEC_PAGES seeding:
  PERM_LEGAL_ENTITY_VIEW confirmed seeded in SecurityPermissions.java
  (security-registry v2.2.0 Section 5.1 — LEGAL_ENTITY_VIEW constant)
  → ORG seeds all its screens into SEC_PAGES at module build time
  → SEC_PAGES column types (PG): ID_PK BIGINT, PAGE_CODE VARCHAR(50),
    NAME_AR/NAME_EN VARCHAR(100), ROUTE VARCHAR(200), IS_ACTIVE SMALLINT,
    DISPLAY_ORDER BIGINT, PARENT_ID_FK BIGINT
  → Permission pattern: PERM_<PAGE_CODE>_VIEW/CREATE/UPDATE/DELETE
    (auto-generated by POST /api/pages per security-registry Section 2.4)
  → ORG does not own SEC_PAGES — Security owns table structure; ORG seeds rows

DataScope:
  Row-Level Security enforced by Security module at query layer.
  ORG_BRANCH is the primary DataScope boundary entity.
  ORG module does not implement its own scope filter.

JWT:
  No tenant claim (removed 2026-06-21 — TENANT_ID eliminated system-wide).
  Access token claims: sub (username), authorities[], userId.
  Session: STATELESS.
──────────────────────────────────────────────────────────────────

AUTO-DECISIONS (updated for PG migration)
──────────────────────────────────────────────────────────────────
AUTO: DB_TARGET = POSTGRESQL_16 applied to all ORG DDL in DBS-ORG-001
FROM: master-registry v2.7.4 versioning log (2026-06-28 PG migration)
      + security-registry v2.2.0 (PG types confirmed)
IF WRONG: revert DBS-ORG-001 to ORACLE_19C and re-run P2

AUTO: All flag fields use SMALLINT DEFAULT 1 (not BOOLEAN, not NUMBER(1))
FROM: CORE-8 DB_TARGET Syntax Mapping — SMALLINT for boolean flags in PG
      Consistent with Security PG migration (IS_ACTIVE SMALLINT in all tables)
IF WRONG: change flag type in DBS-ORG-001 — notify all consumers

AUTO: All PK columns use BIGINT with explicit sequences (not GENERATED IDENTITY)
FROM: CORE-8 rule — "SERIAL / GENERATED ALWAYS AS IDENTITY NOT used; sequences
      always created explicitly"
      Note: Security uses BIGINT GENERATED ALWAYS AS IDENTITY (PERMANENT EXCEPTION)
      ORG is a governed new-build — must follow CORE-8 PG sequence rule
IF WRONG: verify DBS-ORG-001 sequence definitions match PG sequence standard

AUTO: 7 entities (P0 layer) + ORG_REGION_TYPE as 8th table in DBS-ORG-001
FROM: master-registry v2.7.4 Section 3 ("8 tables") confirming ORG_REGION_TYPE
      is a full DB table, not a lookup row
IF WRONG: reclassify ORG_REGION_TYPE as MD_LOOKUP_DETAIL entry — requires
          master-registry Section 6 + Section 5 update and DBS-ORG-001 amendment

AUTO: No approval flow for any ORG entity
FROM: RULE-13 (shared-governance-rules.md) — workflow deferred by default
      No approval request in vision.md for Organization
IF WRONG: declare exception per RULE-13 protocol in business-policies-ORG.md
──────────────────────────────────────────────────────────────────

INF-IDs
──────────────────────────────────────────────────────────────────
INF-ORG-01  │ PERM_LEGAL_ENTITY_VIEW confirmed seeded in SecurityPermissions.java
             │ (security-registry v2.2.0 Section 5.1)
             │ Risk: Other ORG SEC_PAGES entries may already exist in DB from
             │ prior seeding activity. P2.5 / P4 should audit existing PAGE_CODE
             │ entries for MODULE='ORGANIZATION' (or similar) to confirm no
             │ duplicate pages before test-plan generation.
             │ Non-blocking — ORG already at MODE 2.
──────────────────────────────────────────────────────────────────

OPEN AQ-IDs (master-registry v2.7.4 Section 14)
──────────────────────────────────────────────────────────────────
AQ-003  │ DEFERRED — non-blocking
         │ Which modules consume ORG_REGION via SOFT-READ?
         │ Impact of Region deactivation on consumers?
         │ Resolves automatically when first consuming module runs MODE 1.5
         │ ORG currently at MODE 2 — AQ-003 does not block MODE 2.5
──────────────────────────────────────────────────────────────────

══════════════════════════════════════════════════════════════════
Readiness   : READY ✓
Pipeline    : GOVERNED ✓ MODE 2 — ALIGN GATE PASSED ✓
Next Stage  : MODE 2.5 → test-plan-org-001.md
              (upload srs-org-001.md + execution-plan-org-001.md + this registry)
══════════════════════════════════════════════════════════════════
