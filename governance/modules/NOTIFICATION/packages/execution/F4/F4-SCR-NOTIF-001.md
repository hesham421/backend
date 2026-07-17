<!-- Source: PHASE:F4 / SUB:SCR-NOTIF-001 -->
<!-- Context: see F4-HEADER.md for phase-level strategy, registry table, and intro -->

### F4-SCREEN — SCR-NOTIF-001 — Notification Bell + History
─────────────────────────────────────────────────────────────────
**Deviation notice (DRV-NOTIF-009, DRV-NOTIF-010 — see Derivation Log):**
SCR-NOTIF-001 is a PATTERN-3 (Specialized) screen — no Search/Entry split
(F4-RULE-5 does not apply). It has two distinct UI surfaces: a header-embedded
bell dropdown (no route) and a full history list (has its own route, since
the SRS's history/filter requirements exceed what a dropdown can reasonably
hold).

Route path       : /notifications                                ← F4-RULE-1
                    (full history list view only; the bell dropdown itself
                    has no route — see Component Structure below)

Module           : NotificationModule — lazy-loaded                ← F4-RULE-2
Module path      : app/features/notifications/notifications.module.ts

Route guard      : [AuthGuard, PermissionGuard]                    ← F4-RULE-3
                    (applies to the /notifications route only — the header
                    bell component is part of the always-loaded app shell,
                    not a guarded lazy route, per DRV-NOTIF-010)
PERM_* required  : PERM_NOTIFICATION_INBOX_VIEW (list route)

Child routes     : NONE (single list view, no create/edit sub-routes —
                    mark-as-read is an inline row action, not a route)

COMPONENTS:
  NotificationBellComponent
    Path       : app/shared/components/notification-bell/notification-bell.component.ts
    Route      : N/A — embedded in the app shell header, not routed
                 (DRV-NOTIF-010 — always-loaded, shows a live unread
                 preview and links to /notifications for the full list)
    Facade     : NotificationInboxFacade
  NotificationHistoryComponent
    Path       : app/features/notifications/components/notification-history/
    Route      : /notifications
    Facade     : NotificationInboxFacade

SharedModule imports : CommonModule, SharedUiModule (badge/spinner primitives)
─────────────────────────────────────────────────────────────────
