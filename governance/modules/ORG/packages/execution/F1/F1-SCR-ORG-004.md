<!-- Source: PHASE:F1 / SUB:SCR-ORG-004 -->
<!-- Context: see F1-HEADER.md for phase-level strategy, registry table, and intro -->

  ### Model — Department (SCR-ORG-004)
  Fields mirror Response DTO + branchFk, parentDepartmentFk (nullable), nodeTypeId (readonly post-save). Additional `DepartmentTreeNode` model: { id, code, name, nodeType, children: DepartmentTreeNode[] } for tree view.
