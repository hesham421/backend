<!-- Source: PHASE:F2 / SUB:SCR-ORG-004 -->

## F2 — SCR-ORG-004 — Department Screen (Tree)

```
SCREEN INIT:
  1. Check PERM_DEPARTMENT_VIEW
  2. Load LOV-ORG-003: GET /api/lookups/DEPARTMENT_NODE_TYPE?active=true → nodeTypeOptions
  3. Load active Branches (for tree filter)
  4. No auto-tree load — wait for branchFk selection

F2-FACADE — SCR-ORG-004 — Department Management
STATE: departmentTree (List<DeptTreeNode>), selectedBranchFk, selectedItem, isLoading,
       lastTreeFilter, nodeTypeOptions, branchOptions
OPERATIONS: loadDepartmentTree(branchFk, isActiveFl?) → API-ORG-022
            getDepartmentById(id) → API-ORG-024
            createDepartment(data) → API-ORG-021 → reload tree
            updateDepartment(id, data) → API-ORG-025 → reload tree
            deactivateDepartment(id) → API-ORG-026 → reload tree
            reactivateDepartment(id) → API-ORG-027 → reload tree
            loadNodeTypeOptions() | loadBranchOptions()
            loadParentDeptOptions(branchFk) → API-ORG-023 (active DETAIL depts for parent LOV)
```
