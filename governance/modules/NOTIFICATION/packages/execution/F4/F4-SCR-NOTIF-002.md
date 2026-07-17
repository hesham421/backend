<!-- Source: PHASE:F4 / SUB:SCR-NOTIF-002 -->
<!-- Context: see F4-HEADER.md for phase-level strategy, registry table, and intro -->

### F4-SCREEN — SCR-NOTIF-002 — Template Management
─────────────────────────────────────────────────────────────────
Route path       : /notification-templates                        ← F4-RULE-1
                    /notification-templates/new
                    /notification-templates/:id
                    /notification-templates/:id/edit
                    (no /tree route — NotificationTemplate is not
                    self-referencing, not tree-bearing)

Module           : NotificationModule — lazy-loaded                ← F4-RULE-2
Module path      : app/features/notifications/notifications.module.ts

Route guard      : [AuthGuard, PermissionGuard]                    ← F4-RULE-3
PERM_* required  : PERM_NOTIFICATION_TEMPLATE_VIEW (list route + entry-view mode)
                    PERM_NOTIFICATION_TEMPLATE_CREATE (new route)
                    PERM_NOTIFICATION_TEMPLATE_UPDATE (edit route)

Child routes     : :id resolves NotificationTemplateEntryComponent in VIEW
                    mode; :id/edit resolves it in EDIT mode

COMPONENTS:                                                        ← F4-RULE-4, F4-RULE-5
  NotificationTemplateSearchComponent
    Path       : app/features/notifications/components/notification-template-search/
    Route      : /notification-templates
    Facade     : NotificationTemplateFacade                        ← F4-RULE-6
  NotificationTemplateEntryComponent
    Path       : app/features/notifications/components/notification-template-entry/
    Route      : /notification-templates/new, /:id, /:id/edit
    Mode       : CREATE | EDIT | VIEW — resolved from ActivatedRoute  ← F4-RULE-7
    Facade     : NotificationTemplateFacade

SharedModule imports : CommonModule, ReactiveFormsModule, SharedUiModule
─────────────────────────────────────────────────────────────────
