<!-- Source: PHASE:DOC -->

## PHASE DOC — Contract Stabilization
─────────────────────────────────────────────────────────────────
Gate Required    : SVC+API ✓
Gate This Phase  : DOC ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

### DOC-1: API Contract Summary
```
API-ID        │ Endpoint                                          │ Method │ Stability
──────────────┼────────────────────────────────────────────────────┼────────┼──────────
API-NOTIF-001 │ /api/v1/notifications/send                        │ POST   │ STABLE
API-NOTIF-002 │ /api/v1/notifications/schedule                    │ POST   │ STABLE
API-NOTIF-003 │ /api/v1/notifications/history/search              │ POST   │ STABLE
API-NOTIF-004 │ /api/v1/notifications/unread                      │ GET    │ UNSTABLE — DRV-NOTIF-003 (recommended SRS amendment)
API-NOTIF-005 │ /api/v1/notifications/{id}/read                   │ PUT    │ UNSTABLE — DRV-NOTIF-003 (recommended SRS amendment)
API-NOTIF-006 │ /api/v1/notifications/templates/search            │ POST   │ STABLE
API-NOTIF-007 │ /api/v1/notifications/templates                   │ POST   │ STABLE
API-NOTIF-008 │ /api/v1/notifications/templates/{id}               │ PUT    │ STABLE
API-NOTIF-009 │ /api/v1/notifications/templates/{id}/deactivate    │ PUT    │ STABLE
API-NOTIF-010 │ /api/v1/notifications/templates/{id}               │ GET    │ STABLE
API-NOTIF-011 │ /api/v1/notifications/channel-configs              │ GET    │ STABLE
API-NOTIF-012 │ /api/v1/notifications/channel-configs/{id}         │ PUT    │ STABLE
```
Unstable APIs: API-NOTIF-004, API-NOTIF-005 — blocked on DRV-NOTIF-003 (missing DB column — recommended SRS amendment, see Escalation Note).
Frontend-governed contracts: None.
Note: API-NOTIF-003 and API-NOTIF-006 are POST `.../search` (not the GET paths implied by
execution-plan.md's SVCAPI section) — both request DTOs extend `BaseSearchContractRequest`,
which platform rule A.6.6 mandates as POST + `@RequestBody`, never GET. Confirmed against
`NotificationInboxController`/`NotificationTemplateController`; consistent with the SVCAPI
Layer 2/3 fixes already logged in `execution-state.json` (`svcapi_history_search_method_fix`,
`svcapi_template_search_method_fix`). This is a contract-authoring inconsistency in
execution-plan.md, not a deviation from platform governance.

### DOC-2: DTO Typing Rules
LOV field typing: String (notificationTypeId, notificationStatusId, channelTypeId) — never ENUM.
Business Code: N/A — no entity in this module has a Business Code.

### DOC-3: Pagination & Filter Standards
Standard platform rule applies as-is. Used by API-NOTIF-003, API-NOTIF-006.

### DOC-4: Error Catalog

Source of truth: actual backend implementation — `NotificationErrorCodes.java`
(backend/erp-notification) + the shared resource bundles `erp-main/src/main/resources/i18n/
messages.properties` (EN) and `messages_ar.properties` (AR). execution-plan.md's ERROR CATALOG
(PLAN-NOTIF-001) was used as the starting structure but its ERR-NOTIF-0004 text ("Record not
found", a generic PLATFORM-STD placeholder) does not match what was actually implemented — the
real messages are entity-specific; corrected below to the live values.
```
ERR-ID          │ Code (backend)                    │ RULE-ID        │ API-ID              │ HTTP │ Message-AR                                        │ Message-EN
────────────────┼────────────────────────────────────┼────────────────┼─────────────────────┼──────┼──────────────────────────────────────────────────┼──────────────────────────────────────────────────────────
ERR-NOTIF-0001  │ NOTIF_EVENT_INCOMPLETE             │ RULE-NOTIF-001 │ API-NOTIF-001/002   │ 400  │ بيانات الحدث غير مكتملة                          │ Notification event data is incomplete
ERR-NOTIF-0002  │ NOTIF_TEMPLATE_BILINGUAL_REQUIRED  │ RULE-NOTIF-006 │ API-NOTIF-007/008   │ 400  │ يجب توفير نص القالب بالعربي والإنجليزي معاً     │ The template body must be provided in both Arabic and English
ERR-NOTIF-0003  │ NOTIF_TEMPLATE_CODE_DUPLICATE      │ RULE-NOTIF-007 │ API-NOTIF-007/008   │ 409  │ رمز القالب مستخدَم مسبقاً أو غير قابل للتعديل   │ Template code already exists or cannot be modified
ERR-NOTIF-0004a │ NOTIF_TEMPLATE_NOT_FOUND           │ PLATFORM-STD   │ API-NOTIF-008/009/010│ 404 │ لم يتم العثور على قالب الإشعار للمعرف {0}        │ Notification template not found for ID {0}
ERR-NOTIF-0004b │ NOTIF_CHANNEL_CONFIG_NOT_FOUND     │ PLATFORM-STD   │ API-NOTIF-012        │ 404  │ لم يتم العثور على إعدادات قناة الإشعار للمعرف {0} │ Notification channel configuration not found for ID {0}
```
Total governed errors: 4 (RULE-NOTIF-002/003/004/005 are internal/behavioral, reflected in
`NotificationLog` status rather than an HTTP rejection — matches execution-plan.md's own note).
ERR-NOTIF-0004 is split a/b above because the two 404 cases have distinct, entity-specific
messages in the live resource bundles rather than one shared generic string — same ERR-ID/RULE
classification, different text per PLATFORM-STD convention (parameterized "not found" per entity).

Two additional backend codes exist outside the governed catalog (no ERR-ID assigned, both tied
to blocked/deferred contract shells, not new rules):
```
NOTIF_CURRENT_USER_UNRESOLVED    │ API-NOTIF-003     │ 500 │ تعذر التعرف على هوية المستخدم الحالي              │ Unable to resolve the current user's identity
NOTIF_READ_TRACKING_UNAVAILABLE  │ API-NOTIF-004/005 │ 422 │ تتبع القراءة/عدم القراءة غير متاح حالياً          │ Read/unread tracking is not available yet
```
`NOTIF_READ_TRACKING_UNAVAILABLE` is the GOVERNANCE-NOTE-BLOCKED code per DRV-NOTIF-003 (see
`blocked[]` in execution-state.json).

Note: PLAN INDEX (execution-plan.md line 49) says "ERR-IDs: ERR-NOTIF-0001..0006 (6)" but the
plan's own ERROR CATALOG table only defines 4 and explicitly states "Total Errors: 4" — a
pre-existing count mismatch in the plan's index, not introduced here. Not fixed as part of this
phase (out of scope for DOC — flagging only).

**DOC GATE CHECK:**
```
[ ✓ ] All API-IDs from SVC+API appear in API Contract Summary
[ ✓ ] Error Catalog complete with Arabic + English messages
[ ✓ ] All APIs marked STABLE or explicitly UNSTABLE (2 UNSTABLE — documented)
[ ✓ ] Pagination standard declared
DOC Gate: PASSED ✓ (2 APIs UNSTABLE — does not fail the gate, tracked via DRV-NOTIF-003 / Escalation Note)
```
