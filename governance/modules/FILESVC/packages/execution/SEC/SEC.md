<!-- Source: PHASE:SEC -->

## PHASE SEC — Security Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : F4 ✓
Gate This Phase  : SEC ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

### SEC — SCR-FILE-001 — Attachment Panel
─────────────────────────────────────────────────────────────────
Screen guard     : canView = true required for the panel to render within its host screen.
Permission-based UI behavior:
  canView   = false → panel not rendered

  canCreate = false → upload control hidden
  canEdit   = false → N/A (no Update operation exists for FileDocument)
  canDelete = false → delete button hidden
  canApprove= false → N/A (no approval workflow)

API-level enforcement: API-FILE-002 requires PERM_FILE_ATTACHMENT_CREATE (+ token
  validity); API-FILE-003/005 require PERM_FILE_ATTACHMENT_VIEW (+ token validity for
  003); API-FILE-004 requires PERM_FILE_ATTACHMENT_DELETE (+ token validity +
  RULE-FILE-007 ownership/Admin check — composite, not permission-alone).

EXCEPTION module scope: This module has NO JWT validation of its own (POLICY-CLI-06).
  Screen-level permission checks (canView/canCreate/canDelete) are still evaluated
  against Security's standard permission model — the deviation is only that the
  Encrypted Token layer (not JWT) additionally gates /upload, /download, /{token}
  routes.
─────────────────────────────────────────────────────────────────

SECURITY SEED DATA REQUIREMENTS (already present in dbs-file-001.md Block "SECURITY SEED"
— referenced here, not redefined):
```
SEC_PAGES  : page_code = FILE_ATTACHMENT, parent_id_fk = NULL (Shared Component)
PERMISSIONS: PERM_FILE_ATTACHMENT_VIEW / CREATE / UPDATE / DELETE
  PERM_FILE_ATTACHMENT_UPDATE is auto-generated (CORE-9) but functionally unused —
  no Update API exists for FileDocument (documented in SRS B4).
```

SEC Governance Rules: SEC-IMPL-RULE-1..4 apply as platform-standard (no deviation).
