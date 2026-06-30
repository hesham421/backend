<!-- Source: PHASE:F1 / SUB:SCR-ORG-004 -->

  ### Model — Department (SCR-ORG-004)
  Fields mirror Response DTO + branchFk, parentDepartmentFk (nullable), nodeTypeId (readonly post-save). Additional `DepartmentTreeNode` model: { id, code, name, nodeType, children: DepartmentTreeNode[] } for tree view.
