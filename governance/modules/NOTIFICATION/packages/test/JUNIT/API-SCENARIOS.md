<!-- Source: MARK:JUNIT / SUB:API-SCENARIOS -->
<!-- Context: see JUNIT-HEADER.md for mark-level intro and mandatory scenarios -->

### SUB:API-SCENARIOS — Per-API-ID happy path (TP-SEC-2) + edge cases + mandatory scenarios

<!-- TC:TC-NOTIF-014:START -->
```
TC-NOTIF-014 — Send immediate (API-NOTIF-001 happy path, distinct from TC-NOTIF-001)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-001
RULE-ID      : —
SCR-ID       : —
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Fully valid NotificationEvent, single channel EMAIL
When          : POST /api/v1/notifications/send
Then          : HTTP 200 — logEntryIds has exactly 1 entry

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-014:END -->

<!-- TC:TC-NOTIF-015:START -->
```
TC-NOTIF-015 — Schedule (API-NOTIF-002 happy path)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-002
RULE-ID      : —
SCR-ID       : —
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Valid NotificationEvent + scheduledAt = now + 1 hour
When          : POST /api/v1/notifications/schedule
Then          : HTTP 200 — NotificationSendConfirmation returned; no
                dispatch attempt occurs before scheduledAt is reached

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-015:END -->

<!-- TC:TC-NOTIF-016:START -->
```
TC-NOTIF-016 — Notification history (API-NOTIF-003 happy path)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-003
RULE-ID      : —
SCR-ID       : SCR-NOTIF-001
ERR-ID       : —
LOV-ID       : LOV-NOTIF-001, LOV-NOTIF-002
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Current user has 5 NOTIF_LOG rows, 2 of which are notificationStatusId="SENT"
When          : GET /api/v1/notifications/history?notificationStatusId=SENT&page=0&size=10
Then          : HTTP 200 — Page<NotificationLogResponse> with exactly 2 items

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-016:END -->

<!-- TC:TC-NOTIF-017:START -->
```
TC-NOTIF-017 — Template search (API-NOTIF-006 happy path)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-006
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : LOV-NOTIF-001
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : 3 templates exist, 1 with channelTypeId="SMS"
When          : GET /api/v1/notifications/templates?channelTypeId=SMS
Then          : HTTP 200 — Page<NotificationTemplateResponse> with exactly 1 item

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-017:END -->

<!-- TC:TC-NOTIF-018:START -->
```
TC-NOTIF-018 — Update template (API-NOTIF-008 happy path)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-008
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Existing template; UpdateRequest changes templateNameEn only
                (templateCode untouched)
When          : PUT /api/v1/notifications/templates/{id}
Then          : HTTP 200 — NotificationTemplateResponse reflects the new
                templateNameEn; templateCode unchanged

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-018:END -->

<!-- TC:TC-NOTIF-019:START -->
```
TC-NOTIF-019 — Deactivate template (API-NOTIF-009 happy path)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-009
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Existing active template
When          : PUT /api/v1/notifications/templates/{id}/deactivate
Then          : HTTP 200 — isActiveFl becomes false; row NOT removed
                (soft deactivation, DATA+DOM confirms no delete operation exists)

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-019:END -->

<!-- TC:TC-NOTIF-020:START -->
```
TC-NOTIF-020 — Get template by ID (API-NOTIF-010 happy path)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-010
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Existing template with a known notificationTemplatePk
When          : GET /api/v1/notifications/templates/{id}
Then          : HTTP 200 — full NotificationTemplateResponse returned
                matching the stored row

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-020:END -->

<!-- TC:TC-NOTIF-021:START -->
```
TC-NOTIF-021 — List channel configs (API-NOTIF-011 happy path)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-011
RULE-ID      : —
SCR-ID       : SCR-NOTIF-003
ERR-ID       : —
LOV-ID       : LOV-NOTIF-001
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Standard seed data — 5 NotificationChannelConfig rows
                (EMAIL/SMS/WHATSAPP/PUSH/INTERNAL)
When          : GET /api/v1/notifications/channel-configs
Then          : HTTP 200 — array of exactly 5 NotificationChannelConfigResponse
                items, one per channelTypeId

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-021:END -->

<!-- TC:TC-NOTIF-022:START -->
```
TC-NOTIF-022 — Update on a non-existent template returns 404 (edge case)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-008
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : ERR-NOTIF-0004
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Edge case
Data class    : INVALID

Given         : notificationTemplatePk = 999999 (does not exist)
When          : PUT /api/v1/notifications/templates/999999
Then          : HTTP 404 — ERR-NOTIF-0004 returned (PLATFORM-STD via
                LocalizedException — NotFoundException banned)

ERR-ID        : ERR-NOTIF-0004
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-022:END -->

<!-- TC:TC-NOTIF-023:START -->
```
TC-NOTIF-023 — MANDATORY-J-3: Arabic error message present (API level)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-007
RULE-ID      : RULE-NOTIF-006
SCR-ID       : —
ERR-ID       : ERR-NOTIF-0002
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Arabic message
Data class    : INVALID

Given         : templateBodyAr blank (same setup family as TC-NOTIF-009)
When          : POST /api/v1/notifications/templates
Then          : Response body contains messageAr = "يجب توفير نص القالب
                بالعربي والإنجليزي معاً" (exact match) AND messageEn =
                "The template body must be provided in both Arabic and
                English" (exact match) — both present

ERR-ID        : ERR-NOTIF-0002
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-023:END -->

<!-- TC:TC-NOTIF-024:START -->
```
TC-NOTIF-024 — MANDATORY-J-4: Invalid channelTypeId LOV value rejected
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-007
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : LOV-NOTIF-001
─────────────────────────────────────────────────────────────────
Scenario type : LOV
Data class    : INVALID

Given         : channelTypeId = "CARRIER_PIGEON" (not one of EMAIL/SMS/
                WHATSAPP/PUSH/INTERNAL)
When          : POST /api/v1/notifications/templates
Then          : HTTP 400 — request rejected (PLATFORM-STD lookup-value
                validation; no dedicated ERR-NOTIF-ID exists in the
                Error Catalog for this generic case — not invented here)

ERR-ID        : — (PLATFORM-STD, no dedicated ERR-ID in this module's catalog)
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-024:END -->

<!-- TC:TC-NOTIF-025:START -->
```
TC-NOTIF-025 — MANDATORY-J-5: Permission enforcement (API level)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-007
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Permission
Data class    : INVALID

Given         : Authenticated user WITHOUT PERM_NOTIFICATION_TEMPLATE_CREATE
When          : POST /api/v1/notifications/templates with an otherwise-valid body
Then          : HTTP 403 returned

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-025:END -->

<!-- TC:TC-NOTIF-026:START -->
```
TC-NOTIF-026 — MANDATORY-J-7: Empty history result returns 200, not 404
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-003
RULE-ID      : —
SCR-ID       : SCR-NOTIF-001
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Edge case
Data class    : EDGE_CASE

Given         : Current user has zero NOTIF_LOG rows
When          : GET /api/v1/notifications/history
Then          : HTTP 200 — Page<NotificationLogResponse> with empty
                content and totalElements=0 — NOT HTTP 404

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-026:END -->

<!-- TC:TC-NOTIF-027:START -->
```
TC-NOTIF-027 — MANDATORY-J-8: SQL injection resistance
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-001
RULE-ID      : —
SCR-ID       : —
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Security attack
Data class    : ATTACK

Given         : Valid NotificationEvent with subject = "test' OR '1'='1"
When          : POST /api/v1/notifications/send
Then          : HTTP 200 — value stored as a literal string in the
                NOTIF_LOG.SUBJECT column (parameterized query); DB not
                affected, no other rows altered or leaked

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-027:END -->

