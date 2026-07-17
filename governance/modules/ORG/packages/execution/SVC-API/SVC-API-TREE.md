<!-- Source: PHASE:SVC-API / SUB:TREE -->
<!-- Context: see SVC-API-HEADER.md for phase-level strategy, registry table, and intro -->

  ### API-ORG-020 — Get Department tree
  GET /api/v1/org/departments/tree?branchFk&isActiveFl? · Returns: full recursive tree structure (nested DTO: id, code, name, nodeType, children[])
  Service flow: QR-ORG-012 → map flat result set to nested tree DTO in service layer (recursive assembly, not DB-returned nesting).

  ### API-ORG-027 — Get CostCenter tree
  GET /api/v1/org/cost-centers/tree?branchFk&isActiveFl? · Mirrors API-ORG-020 — QR-ORG-015.
