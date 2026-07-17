<!-- Source: PHASE:F2 / SUB:SCR-ORG-005 -->
<!-- Context: see F2-HEADER.md for phase-level strategy, registry table, and intro -->

  ### F2-SERVICE — CostCenterService
  Service class       : `CostCenterService`
  Methods              : create, search, update, deactivate, activate, getById, getTree(branchFk, isActiveFl?) — mapped to API-ORG-026..032 (tree via API-ORG-027)
  Observable type       : `Observable<CostCenter>` (create/update/getById/activate) · `Observable<Page<CostCenter>>` (search) · `Observable<CostCenterTreeNode[]>` (getTree) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001, 0003, 0004, 0012 (RULE-ORG-008 cycle), 0016 (RULE-ORG-019), 0017 (RULE-ORG-020)
  Loading state          : LOCAL for CRUD/search · GLOBAL for getTree (mirrors DepartmentService rationale)
  Caching strategy       : NONE
  XM-ID impact            : None — no outbound XM-IDs (inbound stub XM-INBOUND-STUB-3 relates to future Layer-3 consumers, not this service)

  ### F2-FACADE — CostCenterFacade (SCR-ORG-005)
  Mirrors DepartmentFacade (treeData$, expandNode/collapseNode).
