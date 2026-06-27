<!-- Source: PHASE:F2 / SUB:SCR-ORG-005 -->

## F2 — SCR-ORG-005 — CostCenter Screen (Tree)

```
SCREEN INIT:
  1. Check PERM_COST_CENTER_VIEW
  2. Load LOV-ORG-004 (COST_CENTER_NODE_TYPE) → nodeTypeOptions
  3. Load LOV-ORG-005 (COST_CENTER_TYPE) → costCenterTypeOptions
  4. Load active Branches

F2-FACADE — SCR-ORG-005 — CostCenter Management
STATE: costCenterTree, selectedBranchFk, selectedItem, isLoading, lastTreeFilter,
       nodeTypeOptions, costCenterTypeOptions, branchOptions
OPERATIONS: loadCostCenterTree(branchFk) → API-ORG-029
            getCostCenterById(id) → API-ORG-031
            createCostCenter(data) → API-ORG-028
            updateCostCenter(id, data) → API-ORG-032
            deactivateCostCenter(id) → API-ORG-033
            reactivateCostCenter(id) → API-ORG-034
            loadParentCCOptions(branchFk) → API-ORG-030 (active CostCenters for parent LOV)
```
