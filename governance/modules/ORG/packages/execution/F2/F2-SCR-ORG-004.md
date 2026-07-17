<!-- Source: PHASE:F2 / SUB:SCR-ORG-004 -->
<!-- Context: see F2-HEADER.md for phase-level strategy, registry table, and intro -->

  ### F2-SERVICE — DepartmentService
  Service class       : `DepartmentService`
  Methods              : create, search, update, deactivate, activate, getById, getTree(branchFk, isActiveFl?) — mapped to API-ORG-019,021,022,023,024,025,020
  Observable type       : `Observable<Department>` (create/update/getById/activate) · `Observable<Page<Department>>` (search) · `Observable<DepartmentTreeNode[]>` (getTree) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001, 0003, 0004, 0011 (RULE-ORG-007 cycle), 0016 (RULE-ORG-019 inactive Branch), 0017 (RULE-ORG-020 nodeType immutable)
  Loading state          : LOCAL for CRUD/search · GLOBAL for getTree (full-subtree fetch blocks the tree panel — deviation from default, justified by DRV-ORG-020: a partially-rendered tree is more confusing than a brief full-panel spinner)
  Caching strategy       : NONE
  XM-ID impact            : None — no outbound XM-IDs

  ### F2-FACADE — DepartmentFacade (SCR-ORG-004)
  State includes treeData$ in addition to standard search/entry state; exposes expandNode/collapseNode helpers (client-side, no API).
