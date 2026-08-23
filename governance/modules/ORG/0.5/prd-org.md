# PRD — Organization (ORG)
══════════════════════════════════════════════════════════════════
Module          : Organization (ORG prefix)
Source artifacts: master-registry.md (Section 15, in place of platform-summary.md —
                  see note below), module-registry-org.md, business-policies-org.md
Status          : DRAFT — awaiting Reconciliation Gate (Project 2.5)
══════════════════════════════════════════════════════════════════

Note on inputs: platform-summary.md was not supplied as a standalone file
this session. master-registry.md Sections 3 and 15 were used in its place,
since they carry the equivalent platform-tier and dependency-classification
content (Organization = Layer-1, ROOT module, zero outbound XM dependencies,
READY ✓). If a dedicated platform-summary.md exists, re-run this engine with
it attached to confirm nothing here would change.

## USER STORIES

US-ORG-001
  Story    : As an administrator, I need to create and manage Legal Entities,
             since they are the top-level organizational unit everything else
             in the module sits under.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "ENTITY RELATIONSHIPS" (LegalEntity ROOT — no parent)
  Status   : DRAFT

US-ORG-002
  Story    : As an administrator, I need to create and manage Branches under
             a Legal Entity, so operations can be organized by physical/legal branch.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "ENTITY RELATIONSHIPS" (Branch → LegalEntity)
  Status   : DRAFT

US-ORG-003
  Story    : As an administrator, I need to create and manage Regions under
             a Legal Entity, so branches and other units can be grouped
             geographically or operationally.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "ENTITY RELATIONSHIPS" (Region → LegalEntity)
  Status   : DRAFT

US-ORG-004
  Story    : As an administrator, I need to build and manage a Department
             hierarchy within a Branch, so departments can roll up into
             parent departments where needed.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "ENTITY RELATIONSHIPS" (Department self-reference tree)
  Status   : DRAFT

US-ORG-005
  Story    : As an administrator, I need to build and manage a Cost Center
             hierarchy within a Branch, so cost centers can roll up into
             parent cost centers where needed.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "ENTITY RELATIONSHIPS" (CostCenter self-reference tree)
  Status   : DRAFT

US-ORG-006
  Story    : As an administrator, I need to create and manage Profit Centers
             under a Legal Entity, so profitability can be tracked at that level.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "ENTITY RELATIONSHIPS" (ProfitCenter → LegalEntity)
  Status   : DRAFT

US-ORG-007
  Story    : As an administrator, I need to create and manage Location Sites
             under a Branch, so physical sites (offices, warehouses, etc.)
             are tracked against the branch that owns them.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "ENTITY RELATIONSHIPS" (LocationSite → Branch)
  Status   : DRAFT

US-ORG-008
  Story    : As an administrator, I need to classify a Legal Entity by type
             (Head Office, Branch Office, Subsidiary, Representative Office),
             so entities can be told apart by their legal role.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "LOVs OWNED" (LEGAL_ENTITY_TYPE)
  Status   : DRAFT

US-ORG-009
  Story    : As an administrator, I need to classify a Branch by type
             (Main, Sub, Operations, Admin), so branches can be told apart
             by their operational role.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "LOVs OWNED" (BRANCH_TYPE)
  Status   : DRAFT

US-ORG-010
  Story    : As an administrator, I need to mark a Department node as
             "summary" or "detail," so summary-level departments aren't
             mistakenly used as a posting target for transactions.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — LOVs (DEPARTMENT_NODE_TYPE) and
             OPERATIONAL RULES (RULE-ORG-09), restated at need level
  Status   : DRAFT

US-ORG-011
  Story    : As an administrator, I need to mark a Cost Center node as
             "summary" or "detail," for the same reason — so summary-level
             cost centers aren't mistakenly used as a posting target.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — LOVs (COST_CENTER_NODE_TYPE) and
             OPERATIONAL RULES (RULE-ORG-10), restated at need level
  Status   : DRAFT

US-ORG-012
  Story    : As an administrator, I need to classify a Cost Center as
             Direct, Indirect, or Shared, so cost allocation logic elsewhere
             can rely on that distinction.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "LOVs OWNED" (COST_CENTER_TYPE)
  Status   : DRAFT

US-ORG-013
  Story    : As an administrator, I need to classify a Location Site by type
             (Office, Warehouse, Factory, Site, Retail), so its physical
             nature is clear to anyone consuming it.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "LOVs OWNED" (LOCATION_SITE_TYPE)
  Status   : DRAFT

US-ORG-014
  Story    : As an administrator, I need to classify a Region by type and be
             able to add new region types later, without needing a code
             change, since region types may evolve over time.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "REFERENCE TABLE" (ORG_REGION_TYPE,
             extensible by Admin at runtime)
  Status   : DRAFT

US-ORG-015
  Story    : As an administrator, I need to be stopped from deactivating an
             org unit (Legal Entity, Branch, Region) if it still has active
             units depending on it underneath, so I don't accidentally break
             the structure other modules rely on.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "OPERATIONAL RULES" (RULE-ORG-01
             through RULE-ORG-06), restated at need level, not as the
             enforceable rule text itself
  Status   : DRAFT

US-ORG-016
  Story    : As an administrator, I need to trust that an org unit's business
             code won't change after I save it, and won't collide with
             another unit's code in the same scope, so codes stay reliable
             identifiers over time.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "OPERATIONAL RULES" (RULE-ORG-11,
             RULE-ORG-12), restated at need level
  Status   : DRAFT

US-ORG-017
  Story    : As a user, I need to only see and act on organization data for
             the branches I'm scoped to, so I'm not exposed to or able to
             affect data outside my area.
  Priority : —
  Success metric : —
  Source   : business-policies-org.md — "DataScope / Security"
             (ORG_BRANCH is the primary DataScope boundary)
  Status   : DRAFT

US-ORG-018
  Story    : As a user, I need my access to each organization screen (view,
             create, update, delete) to depend on the permissions I've been
             granted, so I can't do more than I'm authorized to.
  Priority : —
  Success metric : —
  Source   : business-policies-org.md — "SEC_PAGES seeding" (auto-generated
             PERM_<PAGE_CODE>_VIEW/CREATE/UPDATE/DELETE permissions)
  Status   : DRAFT

US-ORG-019
  Story    : As an administrator, I need to see who created or last changed
             any organization record, and when, so changes stay accountable.
  Priority : —
  Success metric : —
  Source   : module-registry-org.md — "PLATFORM CONVENTIONS" (Audit Trail:
             YES, all 7 entities)
  Status   : DRAFT

## OPEN ITEMS (ambiguous, not yet a story)

  ? BusinessUnit — noted as DEFERRED in business-policies-org.md and absent
    from master-registry Section 5. No story written until it's confirmed
    back in scope.

  ? Region SOFT-READ consumers (AQ-003, DEFERRED/non-blocking) — which
    downstream modules will consume Region data, and what they'll need from
    it, is still open. No consumer-side story written to avoid inventing
    an unsourced need.

══════════════════════════════════════════════════════════════════
*End of prd-org.md*
*Next stage: Project 2.5 (UI/UX Design Engine) — requires this file
 AND srs.md together (CONTRACT-11). Does not gate Project 1.*
══════════════════════════════════════════════════════════════════
