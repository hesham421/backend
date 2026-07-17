<!-- Source: PHASE:SEC -->

## PHASE SEC — Security Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : F4 ✓
Gate This Phase  : SEC ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

### SEC — SCR-NOTIF-001 — Notification Bell + History
─────────────────────────────────────────────────────────────────
Screen guard: canView = true (every registered user — header component, no navigation
  route gate in the traditional sense, but permission-gated rendering).
canCreate = N/A (no manual create). canEdit = mark-as-read only (once unblocked).
API-level: API-NOTIF-003 requires PERM_NOTIFICATION_INBOX_VIEW; API-NOTIF-004/005
  will require the same once the recommended SRS amendment (DRV-NOTIF-003) is adopted.
─────────────────────────────────────────────────────────────────

### SEC — SCR-NOTIF-002 — Template Management
─────────────────────────────────────────────────────────────────
Screen guard: canView = true required (Admin only).
canCreate=false → New hidden; canEdit=false → fields readonly; canDelete=false →
  Deactivate hidden.
API-level: API-NOTIF-006/010 → PERM_NOTIFICATION_TEMPLATE_VIEW; API-NOTIF-007 →
  PERM_NOTIFICATION_TEMPLATE_CREATE; API-NOTIF-008 → PERM_NOTIFICATION_TEMPLATE_UPDATE;
  API-NOTIF-009 → PERM_NOTIFICATION_TEMPLATE_DELETE.
─────────────────────────────────────────────────────────────────

### SEC — SCR-NOTIF-003 — Channel Configuration
─────────────────────────────────────────────────────────────────
Screen guard: canView = true required (Admin only).
canEdit=false → toggle/JSON editor disabled.
API-level: API-NOTIF-011 → PERM_NOTIFICATION_CHANNEL_CONFIG_VIEW; API-NOTIF-012 →
  PERM_NOTIFICATION_CHANNEL_CONFIG_UPDATE.
PERM_NOTIFICATION_CHANNEL_CONFIG_CREATE/DELETE are auto-generated (CORE-9) but
  functionally unused (5 fixed seed rows, no create/delete — SRS B4 confirms).
─────────────────────────────────────────────────────────────────

SECURITY SEED DATA REQUIREMENTS (already present in dbs-notif-001.md — referenced,
not redefined; SEC_PAGES rows: NOTIFICATION_INBOX (parent NULL), NOTIFICATION_TEMPLATE
and NOTIFICATION_CHANNEL_CONFIG (parent [NOTIFICATION_SETTINGS] — per SRS B4). 12
PERMISSIONS rows total (4 per screen × 3 screens).

SEC Governance Rules: SEC-IMPL-RULE-1..4 apply as platform-standard.
