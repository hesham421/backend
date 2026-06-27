<!-- Source: PHASE:F1 / SUB:SCR-ORG-005 -->

## F1 — SCR-ORG-005 — إدارة مراكز التكلفة (Hierarchical Tree)

```
Pattern: SPECIALIZED PATTERN-3 (Tree Explorer + Entry Panel)

TREE FILTER MODEL — CostCenterTreeFilterModel:
  branchFk          : number   REQUIRED
  nameAr            : string   OPTIONAL
  nodeTypeId        : string   OPTIONAL (LOV-ORG-004)
  costCenterTypeId  : string   OPTIONAL (LOV-ORG-005)
  isActiveFl        : number   OPTIONAL

TREE VIEW: hierarchical CostCenterTreeNode[]
  Node display: costCenterCode + nameAr + nodeTypeId + costCenterTypeId indicators

ENTRY MODEL — CostCenterFormModel:
  costCenterCode      : string   READ-ONLY
  nameAr              : string   REQUIRED
  nameEn              : string   REQUIRED
  branchFk            : number   REQUIRED   — LOV active Branches
  parentCostCenterFk  : number   OPTIONAL   — LOV active CostCenters in same Branch
  nodeTypeId          : string   REQUIRED   — LOV-ORG-004
  costCenterTypeId    : string   REQUIRED   — LOV-ORG-005
  notes               : string   OPTIONAL
```
