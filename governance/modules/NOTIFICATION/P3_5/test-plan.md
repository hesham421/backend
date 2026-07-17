<!-- PHASE:HEADER:START -->
# TEST-PLAN — Notification Service — PLAN-ID: PLAN-NOTIF-001
══════════════════════════════════════════════════════════════════
Source artifacts:
  execution-plan.md : PLAN-NOTIF-001 — Gate ALIGN ✓ confirmed (post-4A-correction)
  srs.md             : srs-notif-001.md
  db-script.md       : dbs-notif-001.md
Open Questions: None (P3-owned). 1 Escalation Note on file (DRV-NOTIF-003)
  recommending a Project 1 OQ for the read/unread column gap — see
  execution-plan-notif-001.md. API-NOTIF-004/005 are DEFERRED in this test-plan
  accordingly (no TC generated against an unimplemented contract).
══════════════════════════════════════════════════════════════════

## MODE 2.5 ENTRY GATE

```
╔══════════════════════════════════════════════════════════════════╗
║                MODE 2.5 — TEST PLAN ENTRY GATE                   ║
╠══════════════════════════════════════════════════════════════════╣
║ execution-plan.md uploaded?          ║ ✓                         ║
║ Gate ALIGN ✓ confirmed?              ║ ✓                         ║
║ srs.md uploaded?                     ║ ✓                         ║
║ db-script.md uploaded?               ║ ✓                         ║
╠══════════════════════════════════════════════════════════════════╣
║ Entry Gate: PASSED ✓ — test-plan.md generation proceeds          ║
║ Note: API-NOTIF-004/005 excluded — DRV-NOTIF-003 (UNSTABLE)       ║
╚══════════════════════════════════════════════════════════════════╝
```

## TARGET TC COUNT

```
MARK:JUNIT       : (7 RULE-IDs × ~1.7) + (8 new API-IDs × 1) + 1 edge + 4 mandatory ≈ 27 TC
MARK:PLAYWRIGHT  : (3 SCR-ID × ~2.3) + 3 mandatory + 3 INT Flow ≈ 10 TC
─────────────────────────────────────────────────────────────────
Total target      : 37 TC — within 25–40 target range ✓ (not over-engineered)
```

---
<!-- PHASE:HEADER:END -->

<!-- PHASE:JUNIT:START -->
<!-- MARK:JUNIT:START -->
## MARK:JUNIT — Backend tests executed via JUnit

Threshold check: 27 TCs > 12 → SUB split applied (RULE-SCENARIOS / API-SCENARIOS).

<!-- SUB:RULE-SCENARIOS:START -->
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

<!-- SUB:RULE-SCENARIOS:END -->

<!-- SUB:API-SCENARIOS:START -->
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

<!-- SUB:API-SCENARIOS:END -->

**MANDATORY-J-1 / MANDATORY-J-2 (Business Code auto-generation / immutability): N/A** —
no entity in this module (NotificationLog, NotificationTemplate,
NotificationChannelConfig) has a Business Code (documented deviation, DATA+DOM
phase of execution-plan-notif-001.md). No TC generated — not invented.

**MANDATORY-J-6 (Soft deactivation with usage check): N/A** — srs-notif-001.md
defines no usage-check RULE-ID for NotificationTemplate deactivation
(API-NOTIF-009 simply sets isActiveFl=false unconditionally). Inventing a
usage-guard behavior not present in the governed SRS would violate the
no-invention rule. No TC generated.

**API-NOTIF-004 / API-NOTIF-005: DEFERRED** — both are UNSTABLE per
execution-plan-notif-001.md DOC phase (DRV-NOTIF-003 — missing read/unread DB
column). No TC generated against an unimplemented contract; will be added once
the recommended SRS amendment lands and MODE 2.5 is re-run for this module
(per Section 16.7 Continuation Protocol).
<!-- MARK:JUNIT:END -->
<!-- PHASE:JUNIT:END -->

---

<!-- PHASE:PLAYWRIGHT:START -->
<!-- MARK:PLAYWRIGHT:START -->
## MARK:PLAYWRIGHT — UI + Integration tests executed via Playwright

Threshold check: 10 TCs > 8 → SUB split applied (UI-FLOWS / INT-FLOW).

<!-- SUB:UI-FLOWS:START -->
### SUB:UI-FLOWS — Per-SCR-ID coverage (TP-SEC-3) + mandatory UI scenarios

<!-- TC:TC-NOTIF-028:START -->
```
TC-NOTIF-028 — SCR-NOTIF-001 happy UI flow (view history)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-003
RULE-ID      : —
SCR-ID       : SCR-NOTIF-001
ERR-ID       : —
LOV-ID       : LOV-NOTIF-001, LOV-NOTIF-002
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : Current user has 4 notifications in history
When          : SCR-NOTIF-001 bell dropdown opened
Then          : History list renders 4 rows with notificationTypeId,
                subject, notificationStatusId, sentAt/createdAt visible

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-028:END -->

<!-- TC:TC-NOTIF-029:START -->
```
TC-NOTIF-029 — SCR-NOTIF-002 happy UI flow (search templates)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-006
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : LOV-NOTIF-001
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : 3 templates exist
When          : SCR-NOTIF-002 opens, user filters by channelTypeId="EMAIL"
Then          : Result grid shows only EMAIL-channel templates; pagination
                controls reflect the filtered count

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-029:END -->

<!-- TC:TC-NOTIF-030:START -->
```
TC-NOTIF-030 — SCR-NOTIF-002 create via UI (template form submit → success)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-007
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : LOV-NOTIF-001
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : User has PERM_NOTIFICATION_TEMPLATE_CREATE, on the Entry
                form (navigated from Search view)
When          : User fills templateCode, both name fields, channelTypeId,
                moduleCode, and both body fields, clicks Save
Then          : Form closes, new template appears in the search results
                grid without a full page reload

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-030:END -->

<!-- TC:TC-NOTIF-031:START -->
```
TC-NOTIF-031 — MANDATORY-P-1 + rule violation on UI: missing bilingual body
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-007
RULE-ID      : RULE-NOTIF-006
SCR-ID       : SCR-NOTIF-002
ERR-ID       : ERR-NOTIF-0002
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Arabic message
Data class    : INVALID

Given         : User locale = AR, Entry form open, templateBodyAr filled,
                templateBodyEn left empty
When          : User clicks Save
Then          : Inline error shows "يجب توفير نص القالب بالعربي والإنجليزي
                معاً" (Arabic, primary) with the English equivalent also
                visible; form remains open, no template created

ERR-ID        : ERR-NOTIF-0002
Language      : BOTH
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-031:END -->

<!-- TC:TC-NOTIF-032:START -->
```
TC-NOTIF-032 — SCR-NOTIF-003 happy UI flow (toggle a channel)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-011, API-NOTIF-012
RULE-ID      : —
SCR-ID       : SCR-NOTIF-003
ERR-ID       : —
LOV-ID       : LOV-NOTIF-001
─────────────────────────────────────────────────────────────────
Scenario type : Happy path
Data class    : VALID

Given         : SCR-NOTIF-003 open, all 5 channels listed, user has
                PERM_NOTIFICATION_CHANNEL_CONFIG_UPDATE
When          : User toggles SMS's isEnabledFl off and saves
Then          : SMS row shows disabled state; API-NOTIF-012 called with
                isEnabledFl=false; other 4 rows unaffected

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-032:END -->

<!-- TC:TC-NOTIF-033:START -->
```
TC-NOTIF-033 — MANDATORY-P-2: Composite Screen UX separation (CORE-9)
─────────────────────────────────────────────────────────────────
API-ID       : —
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Edge case
Data class    : VALID

Given         : SCR-NOTIF-002 (PATTERN-1 Search+Entry) opened
Then          : Search view renders only filter inputs + result grid;
                the Entry form (templateBodyAr/En, etc.) is NOT rendered
                on the Search view; Entry is reached only via a
                navigation action (row click or "New" button)

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-033:END -->

<!-- TC:TC-NOTIF-034:START -->
```
TC-NOTIF-034 — MANDATORY-P-3: Permission enforcement (UI level)
─────────────────────────────────────────────────────────────────
API-ID       : —
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Permission
Data class    : VALID

Given         : User WITHOUT PERM_NOTIFICATION_TEMPLATE_CREATE
When          : SCR-NOTIF-002 renders
Then          : "New Template" button is not rendered; Search/list
                remains visible (VIEW permission still present)

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-034:END -->

**Note on SCR-NOTIF-001 (Bell + History):** No dedicated "create via UI" or
"rule violation on UI" TC is generated for this screen — it is a read + action
screen (PATTERN-3) with no Entry form to submit invalid data against (SRS B2/B4
confirm no manual create exists for NotificationLog). TC-NOTIF-028 (happy UI
flow) is its sole UI-Flow coverage; not invented beyond what the screen supports.

<!-- SUB:UI-FLOWS:END -->

<!-- SUB:INT-FLOW:START -->
### SUB:INT-FLOW — Module Integration Flow (TP-SEC-4, adapted to NotificationTemplate lifecycle — the only entity with a full Create→Update→Deactivate flow in this module)

<!-- TC:TC-NOTIF-035:START -->
```
TC-NOTIF-035 — MANDATORY-P-4, part 1: Create → Search (verify appears)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-007, API-NOTIF-006
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : Edge case
Data class    : VALID

Given         : Clean module state (no template with templateCode="INT_FLOW_TEST")
When          : User creates a template with templateCode="INT_FLOW_TEST" via
                the Entry form, then returns to Search and filters by that code
Then          : Exactly 1 result appears, matching the created template

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-035:END -->

<!-- TC:TC-NOTIF-036:START -->
```
TC-NOTIF-036 — MANDATORY-P-4, part 2: Update → Search (verify updated)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-008, API-NOTIF-006
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : State transition
Data class    : VALID

Given         : Continuing from TC-NOTIF-035 — "INT_FLOW_TEST" template exists
When          : User opens it from Search, changes templateNameEn, saves,
                re-searches
Then          : The updated templateNameEn is reflected in the search results grid

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-036:END -->

<!-- TC:TC-NOTIF-037:START -->
```
TC-NOTIF-037 — MANDATORY-P-4, part 3: Deactivate → Search (verify removed from active list)
─────────────────────────────────────────────────────────────────
API-ID       : API-NOTIF-009, API-NOTIF-006
RULE-ID      : —
SCR-ID       : SCR-NOTIF-002
ERR-ID       : —
LOV-ID       : —
─────────────────────────────────────────────────────────────────
Scenario type : State transition
Data class    : VALID

Given         : Continuing from TC-NOTIF-036 — "INT_FLOW_TEST" template active
When          : User clicks Deactivate, confirms, then re-searches with
                isActiveFl=true filter (default active-only view)
Then          : "INT_FLOW_TEST" no longer appears in the active-only
                results (row still exists in DB with isActiveFl=false,
                per DATA+DOM — not deleted)

ERR-ID        : —
Language      : —
Test-Hint     : —
XM-impact     : —
─────────────────────────────────────────────────────────────────
```
<!-- TC:TC-NOTIF-037:END -->

<!-- SUB:INT-FLOW:END -->
<!-- MARK:PLAYWRIGHT:END -->
<!-- PHASE:PLAYWRIGHT:END -->

---

<!-- PHASE:TRACEABILITY:START -->
## TP-SEC-5 — TC TRACEABILITY INDEX (mandatory)

```
TC TRACEABILITY INDEX — Notification Service
══════════════════════════════════════════════════════════════════

MARK:JUNIT
──────────────────────────────────────────────────────────────────
RULE-ID → TC-IDs:
RULE-NOTIF-001 → TC-NOTIF-001 (happy) | TC-NOTIF-002 (violation)
RULE-NOTIF-002 → TC-NOTIF-003 (structural guarantee — no violation path)
RULE-NOTIF-003 → TC-NOTIF-004 (happy/behavioral — no violation path)
RULE-NOTIF-004 → TC-NOTIF-005 (happy) | TC-NOTIF-006 (boundary — ceiling exhausted)
RULE-NOTIF-005 → TC-NOTIF-007 (happy/behavioral — no violation path)
RULE-NOTIF-006 → TC-NOTIF-008 (happy) | TC-NOTIF-009 (violation) | TC-NOTIF-010 (fallback edge case) | TC-NOTIF-023 (AR message)
RULE-NOTIF-007 → TC-NOTIF-011 (happy) | TC-NOTIF-012 (violation) | TC-NOTIF-013 (immutability attack)

API-ID → TC-IDs:
API-NOTIF-001  → TC-NOTIF-001 | TC-NOTIF-002 | TC-NOTIF-003 | TC-NOTIF-004 |
                  TC-NOTIF-010 | TC-NOTIF-014 (happy) | TC-NOTIF-027 (attack)
API-NOTIF-002  → TC-NOTIF-015 (happy)
API-NOTIF-003  → TC-NOTIF-016 (happy) | TC-NOTIF-026 (empty result)
API-NOTIF-004  → DEFERRED (DRV-NOTIF-003)
API-NOTIF-005  → DEFERRED (DRV-NOTIF-003)
API-NOTIF-006  → TC-NOTIF-017 (happy)
API-NOTIF-007  → TC-NOTIF-008 | TC-NOTIF-009 | TC-NOTIF-011 (happy) | TC-NOTIF-012 |
                  TC-NOTIF-023 | TC-NOTIF-024 (LOV) | TC-NOTIF-025 (permission)
API-NOTIF-008  → TC-NOTIF-013 (attack) | TC-NOTIF-018 (happy) | TC-NOTIF-022 (404 edge)
API-NOTIF-009  → TC-NOTIF-019 (happy)
API-NOTIF-010  → TC-NOTIF-020 (happy)
API-NOTIF-011  → TC-NOTIF-021 (happy)
API-NOTIF-012  → TC-NOTIF-007 (happy)

ERR-ID → TC-IDs:
ERR-NOTIF-0001 → TC-NOTIF-002
ERR-NOTIF-0002 → TC-NOTIF-009 | TC-NOTIF-023 | TC-NOTIF-031 (Playwright)
ERR-NOTIF-0003 → TC-NOTIF-012 | TC-NOTIF-013
ERR-NOTIF-0004 → TC-NOTIF-022

MARK:PLAYWRIGHT
──────────────────────────────────────────────────────────────────
SCR-ID → TC-IDs (UI Flows):
SCR-NOTIF-001  → TC-NOTIF-028 (happy flow only — no Entry form on this screen)
SCR-NOTIF-002  → TC-NOTIF-029 (search flow) | TC-NOTIF-030 (create flow) |
                  TC-NOTIF-031 (rule violation) | TC-NOTIF-033 (CORE-9 separation) |
                  TC-NOTIF-034 (permission)
SCR-NOTIF-003  → TC-NOTIF-032 (toggle flow)

Module INT Flow → TC-IDs:
NOTIF lifecycle (Template) → TC-NOTIF-035 (create→search) |
                              TC-NOTIF-036 (update→search) |
                              TC-NOTIF-037 (deactivate→search)

══════════════════════════════════════════════════════════════════
Coverage summary:
  RULE-IDs covered  : 7 / 7
  API-IDs covered   : 10 / 12 (API-NOTIF-004/005 DEFERRED — DRV-NOTIF-003)
  SCR-IDs covered   : 3 / 3
  Total TCs         : 37 — within target range 25–40 ✓
  JUNIT TCs         : 27
  PLAYWRIGHT TCs    : 10
══════════════════════════════════════════════════════════════════
```

---
*End of test-plan-notif-001.md*
*Governed by: Execution Plan Governance Engine (Project 3, v2), Section 16*
*PLAN-ID: PLAN-NOTIF-001 | Next Mode: MODE 4A (audit this test-plan.md against CHECK-4) → MODE 3 (agent execution)*
*Continuation note: once DRV-NOTIF-003's Escalation Note is resolved (SRS amendment
lands, execution-plan-notif-001.md updated), re-run MODE 2.5 to append TCs for
API-NOTIF-004/005 per Section 16.7 Continuation Protocol — do not regenerate
TC-NOTIF-001..037 above.*
<!-- PHASE:TRACEABILITY:END -->
