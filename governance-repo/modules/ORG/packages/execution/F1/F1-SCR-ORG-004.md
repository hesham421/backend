<!-- Source: PHASE:F1 / SUB:SCR-ORG-004 -->

## F1 — SCR-ORG-004 — إدارة الأقسام (Hierarchical Tree)

```
Pattern: SPECIALIZED PATTERN-3 (Tree Explorer + Entry Panel)

TREE FILTER MODEL — DepartmentTreeFilterModel:
  branchFk   : number   REQUIRED — filter tree by Branch
  nameAr     : string   OPTIONAL — partial search on tree nodes
  nodeTypeId : string   OPTIONAL — SUMMARY / DETAIL filter
  isActiveFl : number   OPTIONAL (default 1)

TREE VIEW: hierarchical display of DepartmentTreeNode[]
  Node display: deptCode + nameAr + nodeTypeId indicator (SUMMARY/DETAIL visual badge)
  Actions on node: Edit (PERM_DEPARTMENT_UPDATE), Deactivate (PERM_DEPARTMENT_DELETE)
  Add Root: PERM_DEPARTMENT_CREATE — no parent
  Add Child: PERM_DEPARTMENT_CREATE — with parentDepartmentFk set

ENTRY MODEL — DepartmentFormModel:
  deptCode            : string   READ-ONLY
  nameAr              : string   REQUIRED
  nameEn              : string   REQUIRED
  branchFk            : number   REQUIRED   — LOV from API-ORG-013 (active Branches)
  parentDepartmentFk  : number   OPTIONAL   — LOV from active Departments in same Branch
  nodeTypeId          : string   REQUIRED   — LOV-ORG-003 (DEPARTMENT_NODE_TYPE)
  notes               : string   OPTIONAL

⚠ parentDepartmentFk dropdown: shows only ACTIVE departments in same branch — excludes current record
```
