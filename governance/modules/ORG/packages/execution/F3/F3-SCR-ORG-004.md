<!-- Source: PHASE:F3 / SUB:SCR-ORG-004 -->
<!-- Context: see F3-HEADER.md for phase-level strategy, registry table, and intro -->

  Department entry form: nameAr/nameEn required, branchFk required (active Branches only — RULE-ORG-019 client pre-check), parentDepartmentFk optional (tree picker excludes self and descendants — client-side cycle pre-check mirroring RULE-ORG-007), nodeTypeId required on create, disabled on edit (RULE-ORG-020).
