<!-- Source: MARK:JUNIT / SUB:RULE-SCENARIOS -->
<!-- Context: see JUNIT-HEADER.md for mark-level intro and mandatory scenarios -->

### SUB:RULE-SCENARIOS — Per-RULE-ID coverage (TP-SEC-1)

<!-- TC:TC-NOTIF-001:START -->
```
TC-NOTIF-001 — Complete NotificationEvent accepted
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-001
RULE-ID      : RULE-NOTIF-001
SCR-ID       : —
ERR-ID       : —
LOV-ID       : LOV-NOTIF-001
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : NotificationEvent with recipientId, channelHint="EMAIL",
                templateCode (active), contextData, priority="MEDIUM" — all present
When          : POST /api/v1/notifications/send
Then          : HTTP 200 — NotificationSendConfirmation with 1 logEntryIds
                entry; NOTIF_LOG row created with notificationStatusId="PENDING"

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-001:END -->

<!-- TC:TC-NOTIF-002:START -->
```
TC-NOTIF-002 — Incomplete NotificationEvent rejected
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-001
RULE-ID      : RULE-NOTIF-001
SCR-ID       : —
ERR-ID       : ERR-NOTIF-0001
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : NotificationEvent missing templateCode
When          : POST /api/v1/notifications/send
Then          : HTTP 400 — ERR-NOTIF-0001 returned; no NOTIF_LOG row created

ERR-ID        : ERR-NOTIF-0001
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-002:END -->

<!-- TC:TC-NOTIF-003:START -->
```
TC-NOTIF-003 — No internal channel inference (structural guarantee)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-001
RULE-ID      : RULE-NOTIF-002
SCR-ID       : —
ERR-ID       : —
LOV-ID       : LOV-NOTIF-001
─────────────────────────────────────────────────────────────────
Scenario type : Edge case
Data class    : VALID

Given         : Two NotificationEvents from different moduleCode values
                ("PROCUREMENT" and "FINANCE") for the SAME templateCode,
                each with a DIFFERENT explicit channelHint ("EMAIL" vs "SMS")
When          : Both are POSTed to /api/v1/notifications/send
Then          : Each event is dispatched on exactly the channel(s) its own
                channelHint specified — moduleCode never influences channel
                selection (no internal module_code→channel mapping exists)

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-003:END -->

<!-- TC:TC-NOTIF-004:START -->
```
TC-NOTIF-004 — Independent fan-out: one disabled channel doesn't block others
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-001
RULE-ID      : RULE-NOTIF-003
SCR-ID       : —
ERR-ID       : —
LOV-ID       : LOV-NOTIF-001, LOV-NOTIF-002
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : channelHint = ["EMAIL","SMS"]; EMAIL enabled, SMS disabled
                (NotificationChannelConfig.isEnabledFl=false for SMS)
When          : POST /api/v1/notifications/send
Then          : HTTP 200 — 2 independent NOTIF_LOG rows created: one
                notificationTypeId="EMAIL" status="PENDING", one
                notificationTypeId="SMS" status="CHANNEL_DISABLED" — the
                EMAIL row is unaffected by SMS being disabled

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-004:END -->

<!-- TC:TC-NOTIF-005:START -->
```
TC-NOTIF-005 — Retry succeeds before exhausting the ceiling
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-001
RULE-ID      : RULE-NOTIF-004
SCR-ID       : —
ERR-ID       : —
LOV-ID       : LOV-NOTIF-002
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : A PENDING NOTIF_LOG row whose channel adapter fails on
                attempts 1–2 and succeeds on attempt 3
When          : The dispatch orchestration processes the row
Then          : retryCount reaches 2 during retries, then
                notificationStatusId transitions to "SENT" — backoff
                intervals observed as 2s then 3s between attempts

ERR-ID        : —
Language      : —
Test-Hint     : RULE-NOTIF-004 has explicit numeric thresholds (5 retries,
                2s/3s/4.5s/6.75s backoff) — boundary trigger satisfied
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-005:END -->

<!-- TC:TC-NOTIF-006:START -->
```
TC-NOTIF-006 — Retry ceiling exhausted → FAILED (boundary)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-001
RULE-ID      : RULE-NOTIF-004
SCR-ID       : —
ERR-ID       : —
LOV-ID       : LOV-NOTIF-002
─────────────────────────────────────────────────────────────────
Scenario type : Boundary
Data class    : BOUNDARY

Given         : A PENDING NOTIF_LOG row whose channel adapter fails on
                every attempt
When          : The dispatch orchestration processes the row through all
                5 retries (backoff 2s→3s→4.5s→6.75s between the 5 attempts)
Then          : After the 5th failed attempt, notificationStatusId
                transitions to "FAILED" (terminal); retryCount = 5;
                no notification is sent to the original sending module

ERR-ID        : —
Language      : —
Test-Hint     : RULE-NOTIF-004 explicit numeric threshold (5 retries) —
                boundary trigger satisfied
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-006:END -->

<!-- TC:TC-NOTIF-007:START -->
```
TC-NOTIF-007 — Disabled channel logs CHANNEL_DISABLED, no error to sender
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-012
RULE-ID      : RULE-NOTIF-005
SCR-ID       : —
ERR-ID       : —
LOV-ID       : LOV-NOTIF-001, LOV-NOTIF-002
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : PUT /api/v1/notifications/channel-configs/{id} sets
                isEnabledFl=false for WHATSAPP; then a NotificationEvent
                with channelHint="WHATSAPP" is published
When          : The event is processed
Then          : API-NOTIF-012 call returns HTTP 200 (config updated); the
                subsequent send call ALSO returns HTTP 200 (no error
                surfaced to the sending module) — NOTIF_LOG row created
                directly with notificationStatusId="CHANNEL_DISABLED"

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-007:END -->

<!-- TC:TC-NOTIF-008:START -->
```
TC-NOTIF-008 — Bilingual template body saved successfully
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-007
RULE-ID      : RULE-NOTIF-006
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : LOV-NOTIF-001
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : templateBodyAr and templateBodyEn both non-blank,
                well-formed with {{placeholder}} tokens
When          : POST /api/v1/notifications/templates
Then          : HTTP 201 — NotificationTemplateResponse returned with
                both bodies persisted verbatim

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : ⏸ DEFERRED XM-NOTIF-001 — fileFk remains NULL, not written
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-008:END -->

<!-- TC:TC-NOTIF-009:START -->
```
TC-NOTIF-009 — Missing one language body rejected
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-007
RULE-ID      : RULE-NOTIF-006
SCR-ID       : SCR-NOTIF-002
ERR-ID       : ERR-NOTIF-0002
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : templateBodyEn is blank/empty string, templateBodyAr is populated
When          : POST /api/v1/notifications/templates
Then          : HTTP 400 — ERR-NOTIF-0002 returned; no NOTIF_TEMPLATE row created

ERR-ID        : ERR-NOTIF-0002
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-009:END -->

<!-- TC:TC-NOTIF-010:START -->
```
TC-NOTIF-010 — Missing templateCode at send time falls back to default
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-001
RULE-ID      : RULE-NOTIF-006
SCR-ID       : —
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Edge case
Data class    : EDGE_CASE

Given         : A NotificationEvent whose templateCode does not match any
                active NOTIF_TEMPLATE row
When          : POST /api/v1/notifications/send
Then          : HTTP 200 — send does NOT fail; the platform-default
                fallback template is used for subject/body resolution
                (per RULE-NOTIF-006's fallback clause), NOT an ERR-ID rejection

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-010:END -->

<!-- TC:TC-NOTIF-011:START -->
```
TC-NOTIF-011 — Unique templateCode accepted (also covers API-NOTIF-007 happy path)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-007
RULE-ID      : RULE-NOTIF-007
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : templateCode = "PO_APPROVED_V1" — does not already exist
When          : POST /api/v1/notifications/templates
Then          : HTTP 201 — template created with UQ_NOTIF_TEMPLATE_CODE
                satisfied

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-011:END -->

<!-- TC:TC-NOTIF-012:START -->
```
TC-NOTIF-012 — Duplicate templateCode rejected
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-007
RULE-ID      : RULE-NOTIF-007
SCR-ID       : SCR-NOTIF-002
ERR-ID       : ERR-NOTIF-0003
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Validation failure
Data class    : INVALID

Given         : templateCode = "PO_APPROVED_V1" already exists (from TC-NOTIF-011)
When          : POST /api/v1/notifications/templates with the same templateCode
Then          : HTTP 409 — ERR-NOTIF-0003 returned; no second row created

ERR-ID        : ERR-NOTIF-0003
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-012:END -->

<!-- TC:TC-NOTIF-013:START -->
```
TC-NOTIF-013 — templateCode immutable on update
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-008
RULE-ID      : RULE-NOTIF-007
SCR-ID       : SCR-NOTIF-002
ERR-ID       : ERR-NOTIF-0003
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Security attack
Data class    : ATTACK

Given         : Existing template with templateCode="PO_APPROVED_V1"; a
                crafted PUT body forcing templateCode="PO_APPROVED_V2"
                (field not exposed in the normal UpdateRequest DTO, sent
                via direct curl)
When          : PUT /api/v1/notifications/templates/{id} with the forced field
Then          : HTTP 409 — ERR-NOTIF-0003 returned; templateCode column
                unchanged in DB

ERR-ID        : ERR-NOTIF-0003
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-013:END -->

