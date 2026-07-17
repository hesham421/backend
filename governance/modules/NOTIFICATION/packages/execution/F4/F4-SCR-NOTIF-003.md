<!-- Source: PHASE:F4 / SUB:SCR-NOTIF-003 -->
<!-- Context: see F4-HEADER.md for phase-level strategy, registry table, and intro -->

### F4-SCREEN — SCR-NOTIF-003 — Channel Configuration
─────────────────────────────────────────────────────────────────
**Deviation notice (DRV-NOTIF-009 — see Derivation Log):** SCR-NOTIF-003 is
a PATTERN-2 (Inline Toggle List) screen — no Search/Entry split (F4-RULE-5
does not apply); 5 fixed rows are edited inline, no separate Entry route.

Route path       : /notification-channel-configs                   ← F4-RULE-1

Module           : NotificationModule — lazy-loaded                 ← F4-RULE-2
Module path      : app/features/notifications/notifications.module.ts

Route guard      : [AuthGuard, PermissionGuard]                     ← F4-RULE-3
PERM_* required  : PERM_NOTIFICATION_CHANNEL_CONFIG_VIEW (list route)
                    PERM_NOTIFICATION_CHANNEL_CONFIG_UPDATE (inline edit — no separate route)

Child routes     : NONE (single inline-editable list, no create/edit sub-routes)

COMPONENTS:
  NotificationChannelConfigComponent
    Path       : app/features/notifications/components/notification-channel-config/
    Route      : /notification-channel-configs
    Facade     : NotificationChannelConfigFacade

SharedModule imports : CommonModule, ReactiveFormsModule (JSON editor), SharedUiModule
─────────────────────────────────────────────────────────────────
