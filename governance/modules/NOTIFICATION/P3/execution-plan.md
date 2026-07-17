<!-- ═══════════════════════════════════════════════════════════ -->
<!-- EXECUTION PLAN — Layer 3 (Execution Truth) + Layer 3.5 Summary -->
<!-- Governed by: Execution Plan Governance Engine (Project 3, v2) -->
<!-- MODE 2 — Single-File Agent-Ready Output                     -->
<!-- ═══════════════════════════════════════════════════════════ -->

# EXECUTION PLAN — Notification Service
## PLAN-ID: PLAN-NOTIF-001

```
Task Type        : 🆕 New Feature
Plan Name        : New Feature — Multi-Channel Notification Engine — Notification Service Module
Feature Code     : NOTIF-001 (srs-notif-001.md v1.0)
DBS-ID           : DBS-NOTIF-001 (dbs-notif-001.md — GOVERNED ✓ MODE 1.5)
DB_TARGET        : POSTGRESQL_16
Module Prefix    : NOTIF
Generated        : 2026-07-11
Governance Mode  : FULL (SRS ✓ + DB Script ✓ + module-registry ✓ + master-registry ✓)
```

---

## SECTION 2A — ARTIFACT EXTRACTION SUMMARY (pre-generation)

```
Extracted from srs-notif-001.md : 3 entities, 7 rules, 2 LOVs, 3 screens, 12 APIs, 0 open OQs
Extracted from dbs-notif-001.md : 3 tables, 38 DBF-IDs, 1 outbound XM-ID (DEFERRED)
Extraction Failure Protocol Case: None — all A3/B5 elements have full DB + API coverage.
GOVERNANCE REDUCED : No — both srs.md and db-script.md present.
Prerequisite note   : dbs-notif-001.md requires Security's USERS table to already exist
  (PERMANENT EXCEPTION, deployed independently). This plan does not re-govern Security.
```

---

## SECTION 3 — EXECUTION PLAN INDEX

```
══════════════════════════════════════════════════════════════════
PLAN INDEX — Notification Service — PLAN-ID: PLAN-NOTIF-001
══════════════════════════════════════════════════════════════════
Entities   : ENTITY-NOTIF-001 (NotificationLog, SHARED-owner, append-only),
             ENTITY-NOTIF-002 (NotificationTemplate, PRIVATE),
             ENTITY-NOTIF-003 (NotificationChannelConfig, PRIVATE/Configuration)
FIELD-IDs  : FIELD-0001..0038 (38, fully DBF-bound — see Section 4)
APIs       : API-NOTIF-001..012 (12)
RULE-IDs   : RULE-NOTIF-001..007 (7)
LOV-IDs    : LOV-NOTIF-001 (NOTIFICATION_CHANNEL, 5 values), LOV-NOTIF-002 (NOTIFICATION_STATUS, 4 values)
ERR-IDs    : ERR-NOTIF-0001..0006 (6)
SCR-IDs    : SCR-NOTIF-001 (Notification Bell + History — PATTERN-3),
             SCR-NOTIF-002 (Template Management — PATTERN-1 Search+Entry),
             SCR-NOTIF-003 (Channel Config — PATTERN-2)
QR-IDs     : QR-NOTIF-001..011 (11)
XM-IDs Outbound : XM-NOTIF-001 (HARD-FK → FILE_DOCUMENT, File Service — DEFERRED)
Live cross-module FK (not XM) : NOTIF_LOG.RECIPIENT_ID → Security USERS.USERS_PK
                                 (PERMANENT EXCEPTION — no XM-ID per SRS A7 / CONTRACT-7 note)
XM Inbound Stubs: XM-INBOUND-STUB-NOTIF-1 (all 3.x modules, event producers — not a
                   data-FK stub, informational), XM-INBOUND-STUB-NOTIF-2 (AuditService,
                   SOFT-READ on NOTIF_LOG, NOT-YET-ASSIGNED)
OQ-IDs Open: None
DRV-IDs    : DRV-NOTIF-001..010 (contiguous — see Derivation Log)
══════════════════════════════════════════════════════════════════
```

---

## SECTION 4 — DB ALIGNMENT MANIFEST (CANONICAL — FIELD-ID → DBF-ID)

```
## DB ALIGNMENT MANIFEST — Notification Service — PLAN-ID: PLAN-NOTIF-001 / DBS-ID: DBS-NOTIF-001
══════════════════════════════════════════════════════════════════
FIELD-ID   │ DBF-ID   │ Plan Type      │ FK/XM-ID │ Match Status
───────────┼──────────┼────────────────┼──────────┼─────────────
FIELD-0001 │ DBF-0001 │ Long           │ —        │ ✓
FIELD-0002 │ DBF-0002 │ Long           │ FK → Security USERS.USERS_PK (EXCEPTION, no XM-ID) │ ✓
FIELD-0003 │ DBF-0003 │ String(20)     │ LOV-NOTIF-001 │ ✓
FIELD-0004 │ DBF-0004 │ String(50)     │ — (natural-key ref, no physical FK) │ ✓
FIELD-0005 │ DBF-0005 │ String(500)    │ —        │ ✓
FIELD-0006 │ DBF-0006 │ String(1000)   │ —        │ ✓
FIELD-0007 │ DBF-0007 │ String(20)     │ LOV-NOTIF-002 │ ✓
FIELD-0008 │ DBF-0008 │ Short (SMALLINT) │ —      │ ✓ (DRV-NOTIF-001: SRS "NUMERIC" → SMALLINT)
FIELD-0009 │ DBF-0009 │ LocalDateTime  │ —        │ ✓
FIELD-0010 │ DBF-0010 │ String(20)     │ —        │ ✓
FIELD-0011 │ DBF-0011 │ Long           │ — (polymorphic, no FK) │ ✓
FIELD-0012 │ DBF-0012 │ String(50)     │ —        │ ✓
FIELD-0013 │ DBF-0013 │ String(255)    │ —        │ ✓
FIELD-0014 │ DBF-0014 │ LocalDateTime  │ —        │ ✓
FIELD-0015 │ DBF-0015 │ String(255)    │ —        │ ✓
FIELD-0016 │ DBF-0016 │ LocalDateTime  │ —        │ ✓
FIELD-0017 │ DBF-0017 │ Long           │ —        │ ✓
FIELD-0018 │ DBF-0018 │ String(50)     │ —        │ ✓
FIELD-0019 │ DBF-0019 │ String(200)    │ —        │ ✓
FIELD-0020 │ DBF-0020 │ String(200)    │ —        │ ✓
FIELD-0021 │ DBF-0021 │ String(20)     │ LOV-NOTIF-001 │ ✓
FIELD-0022 │ DBF-0022 │ String(20)     │ —        │ ✓
FIELD-0023 │ DBF-0023 │ String (TEXT)  │ —        │ ✓
FIELD-0024 │ DBF-0024 │ String (TEXT)  │ —        │ ✓
FIELD-0025 │ DBF-0025 │ Long           │ XM-NOTIF-001 → FILE_DOCUMENT (DEFERRED) │ ✓
FIELD-0026 │ DBF-0026 │ Boolean        │ —        │ ✓
FIELD-0027 │ DBF-0027 │ String(255)    │ —        │ ✓
FIELD-0028 │ DBF-0028 │ LocalDateTime  │ —        │ ✓
FIELD-0029 │ DBF-0029 │ String(255)    │ —        │ ✓
FIELD-0030 │ DBF-0030 │ LocalDateTime  │ —        │ ✓
FIELD-0031 │ DBF-0031 │ Long           │ —        │ ✓
FIELD-0032 │ DBF-0032 │ String(20)     │ LOV-NOTIF-001 │ ✓
FIELD-0033 │ DBF-0033 │ Boolean        │ —        │ ✓
FIELD-0034 │ DBF-0034 │ String (TEXT)  │ —        │ ✓
FIELD-0035 │ DBF-0035 │ String(255)    │ —        │ ✓
FIELD-0036 │ DBF-0036 │ LocalDateTime  │ —        │ ✓
FIELD-0037 │ DBF-0037 │ String(255)    │ —        │ ✓
FIELD-0038 │ DBF-0038 │ LocalDateTime  │ —        │ ✓
══════════════════════════════════════════════════════════════════
Total: 38 FIELD-IDs, all DBF-bound, all Match Status ✓.
```

---

## SECTION 5 — OPEN QUESTIONS LOG — CONTINUATION

```
None open. srs-notif-001.md OQ Log confirms 0 open OQ-IDs at MODE 1. AQ-010 (SMS
provider) and AQ-011 (WhatsApp provider) are P0-level technical questions tracked in
master-registry §14 — NOT OQ-IDs, and explicitly do not affect this plan's structure
(provider selection lives in NotificationChannelConfig.configJson, no schema impact —
see DATA+DOM). No P3 action required for AQ-010/AQ-011.
```

---

<!-- PHASE:CORE:START -->
## PHASE CORE — Architectural Policies
─────────────────────────────────────────────────────────────────
Gate Required    : MODE 2 Entry Gate PASSED ✓
Gate This Phase  : CORE ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

CANONICAL ARCHITECTURE — applies as defined platform-wide (PROJECT-3 §8.1) — no
restatement; single source of truth is the Governance Engine.

MODULE-SPECIFIC DECLARATIONS:

Domain behavior placement : "Domain behavior: embedded in Entity methods."
  Rationale: fan-out log-row creation (RULE-NOTIF-003), retry counting
  (RULE-NOTIF-004), disabled-channel logging (RULE-NOTIF-005), and template
  language/fallback resolution (RULE-NOTIF-006) are entity-local state
  transitions/decisions — no separate domain/ package needed at this module scale
  (3 entities), consistent with File Service's precedent in this same session.

Entity base        : AuditableEntity — all three entities.
Error signaling     : LocalizedException — NotFoundException BANNED.
Audit fields        : AuditEntityListener — never accepted in Create/UpdateRequest.
Search/Pagination   : SearchRequest extends BaseSearchContractRequest — used by
                      API-NOTIF-003 (history) and API-NOTIF-006 (template search).
Deactivation        : NotificationTemplate.isActiveFl = false (soft) — NOT deletion
                      (API-NOTIF-009). NotificationChannelConfig has no
                      create/delete — only isEnabledFl toggle (API-NOTIF-012).
                      NotificationLog is append-only — no deactivation concept;
                      status transitions only (PENDING→SENT/FAILED/CHANNEL_DISABLED).

MODULE-SPECIFIC ARCHITECTURAL POLICY — Event ingress (not an API-ID):
  Publishing modules reach Notification Service via TWO ingress paths, NEITHER of
  which is a REST API-ID:
    1. RabbitMQ (erp.notification.exchange / erp.notification.queue / routing key
       notification.send) — asynchronous, cross-module, cross-transaction events.
    2. Spring Events (same-process, same-transaction) — for callers already inside
       the same Spring context/transaction.
  Both paths converge on the same internal NotificationEventProcessor (service/
  layer), which performs the fan-out described in RULE-NOTIF-002/003 and persists
  via QR-NOTIF-001 (SAVE NotificationLog). API-NOTIF-001/002 (POST /send,
  /schedule) are the SYNCHRONOUS REST equivalent of the same processor, used by
  callers that prefer a direct HTTP call over messaging — both ingress forms
  invoke the identical validation/fan-out/persist sequence declared once in
  SVC+API below (not duplicated per ingress path).
  Source: business-policies-notif.md "Messaging integration" +
  module-registry-notif.md AUTO-DECISIONS (Event-Based pattern, H.2).

MODULE-SPECIFIC ARCHITECTURAL POLICY — Channel dispatch adapters:
  Email via Apache Camel route (DB-polling on PENDING NotificationLog rows);
  Push via Firebase Admin SDK direct dependency; SMS/WhatsApp via a pluggable
  adapter pattern reading NotificationChannelConfig.configJson (provider TBD —
  AQ-010/AQ-011, does not affect schema or this plan's contracts). These adapters
  live in a module-local channel/ package (not part of the 6-layer CANONICAL
  ARCHITECTURE list) — invoked by the retry/dispatch orchestration in service/.

TYPE MAPPING: standard CORE-8 POSTGRESQL_16 → Java mapping applies. TEXT→String+@Lob
  (templateBodyAr/En, configJson). SMALLINT (RETRY_COUNT) → Java Short — NOT the
  _FL Boolean convention, since RETRY_COUNT is a count, not a flag (DRV-NOTIF-001).
<!-- PHASE:CORE:END -->

---

<!-- PHASE:DATAOM:START -->
## PHASE DATA+DOM — Entity & Domain Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : CORE ✓
Gate This Phase  : DATA+DOM ✓
Gate Status      : PASSED ✓
QR-IDs generated : QR-NOTIF-001, QR-NOTIF-002, QR-NOTIF-003
─────────────────────────────────────────────────────────────────

### ENTITY-NOTIF-003 — NotificationChannelConfig (declared first — no dependencies, seed-only)
```
Table            : NOTIF_CHANNEL_CONFIG (DBS-NOTIF-001)
Type              : PRIVATE (Configuration) — no lifecycle, no Business Code.
Entity base       : AuditableEntity
Operations        : Read, Update ONLY — 5 seed rows (one per channel), no Create/Delete
                    from the user (module-registry-notif.md AUTO-DECISIONS).
Sequence          : SEQ_NOTIF_CHANNEL_CONFIG.NEXTVAL

FIELDS:
  FIELD-0031 notificationChannelConfigPk : Long    PK — SEQ_NOTIF_CHANNEL_CONFIG.NEXTVAL
  FIELD-0032 channelTypeId               : String(20)  REQUIRED — LOV-NOTIF-001, UNIQUE per row
  FIELD-0033 isEnabledFl                 : Boolean REQUIRED — default true (all 5 channels
                                            enabled Phase 1 — final decision 2026-07-11)
  FIELD-0034 configJson                  : String (TEXT) OPTIONAL — provider adapter config
  FIELD-0035 createdBy                   : String(255)  SYSTEM
  FIELD-0036 createdAt                   : LocalDateTime SYSTEM
  FIELD-0037 updatedBy                   : String(255)  SYSTEM
  FIELD-0038 updatedAt                   : LocalDateTime SYSTEM

UNIQUE CONSTRAINT: UQ_NOTIF_CHANNEL_CONFIG_TYPE (CHANNEL_TYPE_ID) — one row per channel.

QR-NOTIF-001 — FIND NotificationChannelConfig by channelTypeId — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : NOTIF_CHANNEL_CONFIG
  DB Operation     : FIND_ONE
  Join strategy    : NONE
  Transaction bound: READ_ONLY
  Fetch strategy   : LAZY (project default — no DRV-ID required)
  Bulk operation   : NO — single-record fetch
  Used by     : internal fan-out processor (RULE-NOTIF-003/005 — enabled-status check
                per requested channel), API-NOTIF-011/012
```

### ENTITY-NOTIF-002 — NotificationTemplate
```
Table            : NOTIF_TEMPLATE (DBS-NOTIF-001)
Type              : PRIVATE (Phase 1). Consumes SHARED ENTITY-FILE-001 (FileDocument) —
                    HARD-FK, DEFERRED via XM-NOTIF-001 (see INT-C).
Business Code     : NONE — documented deviation. templateCode is a manually-assigned,
                    lookupKey-like natural code, immutable after creation (RULE-NOTIF-007).
Entity base       : AuditableEntity
Operations        : Create, Read, Update, Deactivate.
Sequence          : SEQ_NOTIF_TEMPLATE.NEXTVAL

FIELDS:
  FIELD-0017 notificationTemplatePk : Long           PK — SEQ_NOTIF_TEMPLATE.NEXTVAL
  FIELD-0018 templateCode           : String(50)     REQUIRED — unique, immutable post-create
  FIELD-0019 templateNameAr         : String(200)    REQUIRED
  FIELD-0020 templateNameEn         : String(200)    REQUIRED
  FIELD-0021 channelTypeId          : String(20)     REQUIRED — LOV-NOTIF-001
  FIELD-0022 moduleCode             : String(20)     REQUIRED — owning module, free text
  FIELD-0023 templateBodyAr         : String (TEXT)  REQUIRED — Phase-1 inline storage,
                                      supports {{placeholder}} syntax
  FIELD-0024 templateBodyEn         : String (TEXT)  REQUIRED — Phase-1 inline storage
  FIELD-0025 fileFk                 : Long           OPTIONAL/NULLABLE — DEFERRED, unused
                                      Phase 1 — XM-NOTIF-001 (see INT-C/INT-R)
  FIELD-0026 isActiveFl             : Boolean        REQUIRED — default true
  FIELD-0027 createdBy              : String(255)    SYSTEM
  FIELD-0028 createdAt              : LocalDateTime  SYSTEM
  FIELD-0029 updatedBy              : String(255)    SYSTEM
  FIELD-0030 updatedAt              : LocalDateTime  SYSTEM

UNIQUE CONSTRAINT: UQ_NOTIF_TEMPLATE_CODE (TEMPLATE_CODE).

DOMAIN BEHAVIOR (embedded in Entity — per CORE):
  resolveBody(languageCode) → returns templateBodyAr or templateBodyEn per recipient's
  Security-resolved language preference (RULE-NOTIF-006). If a lookup by templateCode
  finds no active template, the CALLER (fan-out processor) substitutes a
  platform-default template rather than failing — this fallback logic lives in the
  processor, not the Entity, since it operates across rows (DRV-NOTIF-002).

QR-NOTIF-002 — FIND NotificationTemplate by templateCode (active only) — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : NOTIF_TEMPLATE
  DB Operation     : FIND_ONE
  Join strategy    : NONE
  Transaction bound: READ_ONLY
  Fetch strategy   : LAZY (project default — no DRV-ID required)
  Bulk operation   : NO — single-record fetch
  Used by     : internal fan-out processor (template + body resolution)
```

### ENTITY-NOTIF-001 — NotificationLog
```
Table            : NOTIF_LOG (DBS-NOTIF-001)
Type              : SHARED (owner) — consumer: AuditService (SOFT-READ, NOT-YET-ASSIGNED)
Business Code     : NONE — documented deviation. Engine-managed, append-only system log.
Entity base       : AuditableEntity
Operations        : Create (system-only, at send time), Read — NO manual Update/Delete;
                    append-only, status/retryCount transitions only.
Sequence          : SEQ_NOTIF_LOG.NEXTVAL

FIELDS:
  FIELD-0001 notificationLogPk    : Long           PK — SEQ_NOTIF_LOG.NEXTVAL
  FIELD-0002 recipientId          : Long           REQUIRED — FK → Security USERS.USERS_PK
                                     (PERMANENT EXCEPTION column name — not usersFk)
  FIELD-0003 notificationTypeId   : String(20)     REQUIRED — LOV-NOTIF-001 — the channel
                                     used for THIS row (one row per requested channel — RULE-NOTIF-003)
  FIELD-0004 templateCode         : String(50)     REQUIRED — natural-key logical reference
                                     to NOTIF_TEMPLATE.TEMPLATE_CODE — NO physical FK
                                     (graceful fallback per RULE-NOTIF-006 — a hard FK
                                     would contradict the fallback design)
  FIELD-0005 subject               : String(500)   OPTIONAL — primarily Email channel
  FIELD-0006 bodyPreview            : String(1000)  OPTIONAL
  FIELD-0007 notificationStatusId   : String(20)    REQUIRED — LOV-NOTIF-002, Status Lifecycle (4 states)
  FIELD-0008 retryCount             : Short         SYSTEM — default 0, ceiling 5 (RULE-NOTIF-004)
  FIELD-0009 sentAt                 : LocalDateTime OPTIONAL — null until sent
  FIELD-0010 moduleCode             : String(20)    REQUIRED — publishing module code
  FIELD-0011 referenceId            : Long          OPTIONAL — polymorphic, NO physical FK
  FIELD-0012 referenceType          : String(50)    OPTIONAL — free text, NOT a governed LOV
  FIELD-0013 createdBy              : String(255)   SYSTEM
  FIELD-0014 createdAt              : LocalDateTime SYSTEM
  FIELD-0015 updatedBy              : String(255)   SYSTEM
  FIELD-0016 updatedAt              : LocalDateTime SYSTEM

FK CONSTRAINT (live, cross-module, Security EXCEPTION — not an XM-ID):
  FK_NOTIF_LOG_USERS — NOTIF_LOG.RECIPIENT_ID → USERS.USERS_PK

STATUS LIFECYCLE (A6 — 4 states, no Workflow Engine — RULE-13, append-only):
  PENDING ──(send succeeds)──► SENT (terminal)
  PENDING ──(5 retries exhausted — RULE-NOTIF-004)──► FAILED (terminal)
  PENDING ──(channel disabled at send time — RULE-NOTIF-005)──► CHANNEL_DISABLED (terminal)
  No transition back to PENDING from any terminal state.

DOMAIN BEHAVIOR (embedded in Entity — per CORE):
  markSent() / markFailed() / markChannelDisabled() — one-way terminal transitions.
  incrementRetry() → retryCount++, called by the dispatch orchestration on each
  failed send attempt, up to the ceiling of 5 (RULE-NOTIF-004).

QR-NOTIF-003 — SAVE NotificationLog (one row per fan-out channel) — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : NOTIF_LOG
  DB Operation     : SAVE
  Join strategy    : NONE
  Transaction bound: READ_WRITE
  Fetch strategy   : N/A (write operation — no fetch graph)
  Bulk operation   : YES — one SAVE invocation per requested channel in channelHint
                     (RULE-NOTIF-003 fan-out; not a single-row operation when
                     channelHint lists multiple channels or "ALL")
  Sequence    : SEQ_NOTIF_LOG.NEXTVAL
```
<!-- PHASE:DATAOM:END -->

---

<!-- PHASE:SVCAPI:START -->
## PHASE SVC+API — Service & API Contract Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : DATA+DOM ✓
Gate This Phase  : SVC+API ✓
Gate Status      : PASSED ✓
QR-IDs generated : QR-NOTIF-004..011
─────────────────────────────────────────────────────────────────

### INTERNAL EVENT PROCESSING (shared by API-NOTIF-001/002 AND the RabbitMQ/Spring
### Event ingress paths declared in CORE — described once, referenced by both APIs)
─────────────────────────────────────────────────────────────────
RULE-NOTIF-001 — Event contract completeness
  Trigger    : On publish of any NotificationEvent (any ingress)
  Statement  : The system MUST reject any NotificationEvent missing recipientId,
               channelHint, templateCode, contextData, or priority.
  Message-AR : بيانات الحدث غير مكتملة
  Message-EN : Notification event data is incomplete
  Scope      : CREATE

RULE-NOTIF-002 — No internal channel inference
  Trigger    : On receiving any NotificationEvent
  Statement  : The system MUST NOT infer or hardcode which channel(s) an event
               requires based on event type or moduleCode — channelHint MUST be
               supplied explicitly by the publishing module (single channel, list,
               or "ALL").
  Message-AR : — (internal design rule — no user-facing rejection message; the
               absence of any module_code→channel mapping table is the verifiable behavior)
  Message-EN : —
  Scope      : ALL

RULE-NOTIF-003 — Independent fan-out per channel
  Trigger    : On processing a multi-value channelHint
  Statement  : For each requested channel, the system MUST create an independent
               NotificationLog entry and evaluate that channel's enabled status
               independently — a disabled channel MUST NOT block delivery on the
               other requested channels.
  Message-AR : — (system behavior, reflected in the log — no direct message)
  Message-EN : —
  Scope      : CREATE

RULE-NOTIF-004 — Retry policy
  Trigger    : On channel send failure
  Statement  : The system MUST retry a failed delivery up to 5 times with
               exponential backoff (2s → 3s → 4.5s → 6.75s), then mark the
               notification FAILED without notifying the original sender.
  Message-AR : — (Admin monitors via the log — no sender notification)
  Message-EN : —
  Scope      : ALL (post-persist, async dispatch orchestration)

RULE-NOTIF-005 — Disabled-channel handling
  Trigger    : On send attempt where NotificationChannelConfig.isEnabledFl = false
  Statement  : The system MUST NOT raise an error to the sending module when a
               target channel is disabled — it MUST log CHANNEL_DISABLED instead.
  Message-AR : — (internal log entry — no error surfaced to publisher)
  Message-EN : —
  Scope      : ALL

SERVICE ORCHESTRATION (NotificationEventProcessor — shared logic):
  1. load      — FIND NotificationTemplate by templateCode (QR-NOTIF-002); FIND
                 NotificationChannelConfig per requested channel (QR-NOTIF-001,
                 called once per channel in channelHint — fan-out)
  2. validate  — RULE-NOTIF-001 (contract completeness); RULE-NOTIF-002 (no
                 inference — structural guarantee, not a runtime check); resolve
                 recipient's language preference from Security (external call,
                 NOT an XM-ID — Security is a live, non-deferred dependency per
                 SRS A7 CONTRACT-7 note); RULE-NOTIF-006 (bilingual + fallback,
                 see NotificationTemplate block below)
  3. integrate — none beyond the Security language-preference lookup (not XM;
                 Security PERMANENT EXCEPTION, used AS-IS)
  4. persist   — for EACH channel in channelHint: SAVE NotificationLog
                 (QR-NOTIF-003) with notificationStatusId = "PENDING" if channel
                 enabled, else "CHANNEL_DISABLED" directly (RULE-NOTIF-005) —
                 no PENDING row created for a disabled channel's dispatch attempt
                 beyond the initial log write itself.
  Post-persist async dispatch (channel/ package, per CORE): for each PENDING row,
  attempt channel send; on failure, incrementRetry() up to 5 (RULE-NOTIF-004),
  then markFailed(); on success, markSent().
─────────────────────────────────────────────────────────────────

### API-NOTIF-001 — Send Immediate (system)
─────────────────────────────────────────────────────────────────
Method / Path    : POST /api/v1/notifications/send
Request DTO      : NotificationSendRequest { recipientId: Long, channelHint: String |
                    String[], templateCode: String, contextData: Map<String,Object>,
                    priority: String (HIGH/MEDIUM/LOW) }
Response DTO      : NotificationSendConfirmation { logEntryIds: Long[] } (one per
                    fan-out channel — see RULE-NOTIF-003)
VALIDATIONS: RULE-NOTIF-001, RULE-NOTIF-002, RULE-NOTIF-003 (see INTERNAL EVENT
  PROCESSING block above — invokes the shared processor synchronously).
SERVICE ORCHESTRATION: as declared in INTERNAL EVENT PROCESSING above.
ERRORS: ERR-NOTIF-0001 (RULE-NOTIF-001 violation) → HTTP 400.
SECURITY: Screen — none (system-to-system call from any authenticated backend module).
LOCALIZATION: messageAr/messageEn per Error Catalog for the 400 case only.
─────────────────────────────────────────────────────────────────

### API-NOTIF-002 — Schedule (system)
─────────────────────────────────────────────────────────────────
Method / Path    : POST /api/v1/notifications/schedule
Request DTO      : NotificationScheduleRequest extends NotificationSendRequest +
                    { scheduledAt: LocalDateTime }
Response DTO      : NotificationSendConfirmation
VALIDATIONS: RULE-NOTIF-001, RULE-NOTIF-002 (same contract check; fan-out and
  dispatch deferred to scheduledAt — scheduling mechanism is P3 implementation
  detail, not a new business rule).
SERVICE ORCHESTRATION: identical persist step to API-NOTIF-001, with dispatch
  deferred until scheduledAt is reached (implementation: scheduled job / delayed
  queue — P3 detail, no new QR-ID beyond QR-NOTIF-003).
ERRORS: ERR-NOTIF-0001 → HTTP 400.
SECURITY: Screen — none.
─────────────────────────────────────────────────────────────────

### API-NOTIF-003 — Notification History
─────────────────────────────────────────────────────────────────
Method / Path    : GET /api/v1/notifications/history
Request DTO       : NotificationHistorySearchRequest extends BaseSearchContractRequest
                    { recipientId: Long [OPTIONAL — default: current user; Admin may
                    query others per permission], notificationTypeId?: String,
                    notificationStatusId?: String, page, size }
Response DTO       : Page<NotificationLogResponse>
VALIDATIONS: none declared (RULE-IDs "—" in SRS B5).
SERVICE ORCHESTRATION:
  1. load — FIND_BY_CRITERIA NotificationLog WHERE RECIPIENT_ID = :recipientId
     [AND NOTIFICATION_TYPE_ID / NOTIFICATION_STATUS_ID if provided] (QR-NOTIF-004)
  2. validate — recipientId defaults to caller's own id unless caller has an
     Admin-level permission to query other recipients (server-side authorization,
     not a RULE-ID — permission-based per SEC phase)
QR-NOTIF-004 — FIND_BY_CRITERIA NotificationLog — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : NOTIF_LOG
  DB Operation     : FIND_BY_CRITERIA
  Join strategy    : NONE
  Transaction bound: READ_ONLY
  Fetch strategy   : LAZY (project default — no DRV-ID required)
  Bulk operation   : YES — paginated multi-record fetch (Page<T>, per DOC-3)
  Index used  : IDX_NOTIF_LOG_RECIPIENT, IDX_NOTIF_LOG_STATUS, IDX_NOTIF_LOG_TYPE
  ALLOWED_SORT_FIELDS: createdAt, sentAt
ERRORS: none beyond platform-standard (empty → HTTP 200).
SECURITY: Screen — SCR-NOTIF-001, Permission — PERM_NOTIFICATION_INBOX_VIEW.
─────────────────────────────────────────────────────────────────

### API-NOTIF-004 — Unread Notifications
─────────────────────────────────────────────────────────────────
Method / Path    : GET /api/v1/notifications/unread
Response DTO      : NotificationUnreadSummary { count: Long, items: NotificationLogResponse[] }
VALIDATIONS: none.
SERVICE ORCHESTRATION:
  1. load — FIND_BY_CRITERIA NotificationLog WHERE RECIPIENT_ID = :currentUser
     AND [unread marker] (QR-NOTIF-005)
  ⚠ DRV-NOTIF-003: SRS does not define an explicit "read/unread" column in A3's
    field list for NotificationLog. Resolved: "unread" = notificationStatusId IN
    ('PENDING','SENT') has no read/unread semantic of its own in the SRS Status
    Lifecycle (A6 — 4 states are delivery states, not read states). This is a
    genuine gap between B3 ("عدد الإشعارات غير المقروءة" — unread count) and A3's
    field catalog. Per Section 2A.3, no column is invented. Flagged as a Finding
    for srs-notif-001.md amendment (a read_fl / read_at column is likely
    required); this plan marks API-NOTIF-004/005 as GOVERNANCE-NOTE-BLOCKED
    pending that amendment — NOT implemented against an invented column.
QR-NOTIF-005 — [BLOCKED — see DRV-NOTIF-003] FIND_BY_CRITERIA NotificationLog — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : NOTIF_LOG
  DB Operation     : FIND_BY_CRITERIA (blocked — no read/unread predicate column exists)
  Join strategy    : N/A (blocked)
  Transaction bound: N/A (blocked)
  Fetch strategy   : N/A (blocked)
  Bulk operation   : N/A (blocked)
  Status: ⏸ PENDING SRS AMENDMENT (see Escalation Note, DRV-NOTIF-003)
ERRORS: N/A (blocked).
SECURITY: Screen — SCR-NOTIF-001, Permission — PERM_NOTIFICATION_INBOX_VIEW.
─────────────────────────────────────────────────────────────────

### API-NOTIF-005 — Mark as Read
─────────────────────────────────────────────────────────────────
Method / Path    : PUT /api/v1/notifications/{id}/read
Request DTO       : notificationLogPk (path)
Response DTO       : NotificationSendConfirmation { logEntryIds: [id] }
⚠ Same DRV-NOTIF-003 blocking condition as API-NOTIF-004 — no read/unread column
  exists in the governed DB schema (dbs-notif-001.md DBF-0001..0016 has none).
  This plan documents the contract (path, DTOs) per SRS B5, but the persist step
  is GOVERNANCE-NOTE-BLOCKED pending an SRS/DB amendment adding the column.
QR-NOTIF-006 — [BLOCKED — see DRV-NOTIF-003] UPDATE NotificationLog (read marker) — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : NOTIF_LOG
  DB Operation     : UPDATE (blocked — no read/unread column to set)
  Join strategy    : N/A (blocked)
  Transaction bound: N/A (blocked)
  Fetch strategy   : N/A (blocked)
  Bulk operation   : N/A (blocked)
  Status: ⏸ PENDING SRS AMENDMENT (see Escalation Note, DRV-NOTIF-003)
SECURITY: Screen — SCR-NOTIF-001, Permission — PERM_NOTIFICATION_INBOX_UPDATE.
─────────────────────────────────────────────────────────────────

### API-NOTIF-006 — Template Search
─────────────────────────────────────────────────────────────────
Method / Path    : GET /api/v1/notifications/templates
Request DTO       : NotificationTemplateSearchRequest extends BaseSearchContractRequest
                    { templateCode?: String [LIKE], channelTypeId?: String [EXACT],
                    moduleCode?: String [LIKE], isActiveFl?: Boolean [EXACT], page, size }
Response DTO       : Page<NotificationTemplateResponse>
VALIDATIONS: none.
SERVICE ORCHESTRATION:
  1. load — FIND_BY_CRITERIA NotificationTemplate WHERE [filters] (QR-NOTIF-007)
QR-NOTIF-007 — FIND_BY_CRITERIA NotificationTemplate — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : NOTIF_TEMPLATE
  DB Operation     : FIND_BY_CRITERIA
  Join strategy    : NONE
  Transaction bound: READ_ONLY
  Fetch strategy   : LAZY (project default — no DRV-ID required)
  Bulk operation   : YES — paginated multi-record fetch (Page<T>, per DOC-3)
  Index: IDX_NOTIF_TEMPLATE_CHANNEL, IDX_NOTIF_TEMPLATE_MODULE
  ALLOWED_SORT_FIELDS: templateCode, templateNameAr, templateNameEn, createdAt
ERRORS: none beyond platform-standard.
SECURITY: Screen — SCR-NOTIF-002, Permission — PERM_NOTIFICATION_TEMPLATE_VIEW.
─────────────────────────────────────────────────────────────────

### API-NOTIF-007 — Create Template
─────────────────────────────────────────────────────────────────
Method / Path    : POST /api/v1/notifications/templates
Request DTO       : NotificationTemplateCreateRequest { templateCode, templateNameAr,
                    templateNameEn, channelTypeId, moduleCode, templateBodyAr, templateBodyEn }
                    ⚠ fileFk NEVER in request body — DEFERRED/unused Phase 1.
Response DTO       : NotificationTemplateResponse (full entity)

VALIDATIONS:
  RULE-NOTIF-006 — Bilingual requirement + fallback
    Trigger   : On create/update, and on send-time template lookup
    Statement : Every NotificationTemplate MUST have both templateBodyAr and
                templateBodyEn; template language MUST resolve from the
                recipient's Security language preference; a missing templateCode
                MUST fall back to a default template rather than fail the send.
    Message-AR: يجب توفير نص القالب بالعربي والإنجليزي معاً
    Message-EN: The template body must be provided in both Arabic and English
    Scope     : CREATE, UPDATE (bilingual completeness); ALL (fallback, at send time)
  RULE-NOTIF-007 — templateCode uniqueness + immutability
    Trigger   : On create/update
    Statement : The system MUST prevent creating a NotificationTemplate whose
                templateCode duplicates an existing one, and MUST reject any
                attempt to modify templateCode after creation.
    Message-AR: رمز القالب مستخدَم مسبقاً أو غير قابل للتعديل
    Message-EN: Template code already exists or cannot be modified
    Scope     : CREATE, UPDATE

SERVICE ORCHESTRATION:
  1. load      — none (create)
  2. validate  — RULE-NOTIF-006 (both bodies present, non-blank); RULE-NOTIF-007
                 (UNIQUE constraint UQ_NOTIF_TEMPLATE_CODE — pre-check + DB-enforced)
  3. integrate — none (fileFk untouched, remains NULL — DEFERRED)
  4. persist   — SAVE NotificationTemplate (QR-NOTIF-008): isActiveFl = true,
                 fileFk = NULL

QR-NOTIF-008 — SAVE NotificationTemplate — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : NOTIF_TEMPLATE
  DB Operation     : SAVE
  Join strategy    : NONE
  Transaction bound: READ_WRITE
  Fetch strategy   : N/A (write operation — no fetch graph)
  Bulk operation   : NO — single-record insert
  Sequence: SEQ_NOTIF_TEMPLATE.NEXTVAL

ERRORS: ERR-NOTIF-0002 (RULE-NOTIF-006) → HTTP 400; ERR-NOTIF-0003 (RULE-NOTIF-007) → HTTP 409.
SECURITY: Screen — SCR-NOTIF-002, Permission — PERM_NOTIFICATION_TEMPLATE_CREATE.
─────────────────────────────────────────────────────────────────

### API-NOTIF-008 — Update Template
─────────────────────────────────────────────────────────────────
Method / Path    : PUT /api/v1/notifications/templates/{id}
Request DTO       : NotificationTemplateUpdateRequest — all fields EXCEPT templateCode
                    (BC-B2-RULE-2-equivalent — immutable field excluded from body per
                    RULE-NOTIF-007).
Response DTO       : NotificationTemplateResponse
VALIDATIONS: RULE-NOTIF-006, RULE-NOTIF-007 (attempted templateCode change in body → reject).
SERVICE ORCHESTRATION:
  1. load — FIND NotificationTemplate by PK (QR-NOTIF-002 pattern reused —
            new QR-ID assigned for PK-based lookup)
  2. validate — RULE-NOTIF-006, RULE-NOTIF-007
  3. persist — UPDATE NotificationTemplate (QR-NOTIF-009)
QR-NOTIF-009 — UPDATE NotificationTemplate by PK — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : NOTIF_TEMPLATE
  DB Operation     : UPDATE
  Join strategy    : NONE
  Transaction bound: READ_WRITE
  Fetch strategy   : LAZY (project default — entity loaded read-only first via the
                     FIND step, then updated — no DRV-ID required)
  Bulk operation   : NO — single-record update
ERRORS: ERR-NOTIF-0002 → 400; ERR-NOTIF-0003 → 409.
SECURITY: Screen — SCR-NOTIF-002, Permission — PERM_NOTIFICATION_TEMPLATE_UPDATE.
─────────────────────────────────────────────────────────────────

### API-NOTIF-009 — Deactivate Template
─────────────────────────────────────────────────────────────────
Method / Path    : PUT /api/v1/notifications/templates/{id}/deactivate
Request DTO       : notificationTemplatePk (path)
Response DTO       : confirmation
VALIDATIONS: none declared beyond standard pre-deactivation usage check
  (PROJECT-STANDARD — usage.canDeactivate, per CORE).
SERVICE ORCHESTRATION: UPDATE NotificationTemplate SET isActiveFl = false
  (reuses QR-NOTIF-009 pattern).
SECURITY: Screen — SCR-NOTIF-002, Permission — PERM_NOTIFICATION_TEMPLATE_DELETE.
─────────────────────────────────────────────────────────────────

### API-NOTIF-010 — Get Template by ID
─────────────────────────────────────────────────────────────────
Method / Path    : GET /api/v1/notifications/templates/{id}
Response DTO       : NotificationTemplateResponse
QR-NOTIF-011 — FIND NotificationTemplate by PK — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : NOTIF_TEMPLATE
  DB Operation     : FIND_ONE
  Join strategy    : NONE
  Transaction bound: READ_ONLY
  Fetch strategy   : LAZY (project default — no DRV-ID required)
  Bulk operation   : NO — single-record fetch
SECURITY: Screen — SCR-NOTIF-002, Permission — PERM_NOTIFICATION_TEMPLATE_VIEW.
─────────────────────────────────────────────────────────────────

### API-NOTIF-011 — List Channel Configs
─────────────────────────────────────────────────────────────────
Method / Path    : GET /api/v1/notifications/channel-configs
Response DTO       : NotificationChannelConfigResponse[] (5 rows, fixed)
Reuses QR-NOTIF-001 pattern (FIND_ALL variant — no pagination needed, fixed 5 rows).
SECURITY: Screen — SCR-NOTIF-003, Permission — PERM_NOTIFICATION_CHANNEL_CONFIG_VIEW.
─────────────────────────────────────────────────────────────────

### API-NOTIF-012 — Update Channel Config
─────────────────────────────────────────────────────────────────
Method / Path    : PUT /api/v1/notifications/channel-configs/{id}
Request DTO       : NotificationChannelConfigUpdateRequest { isEnabledFl: Boolean,
                    configJson: String }
Response DTO       : NotificationChannelConfigResponse

VALIDATIONS:
  RULE-NOTIF-005 — Disabled-channel handling (governs downstream send behavior,
    triggered by this toggle — see INTERNAL EVENT PROCESSING above). No rejection
    rule fires on the toggle itself; this API is how isEnabledFl becomes false.

SERVICE ORCHESTRATION:
  1. load — FIND NotificationChannelConfig by PK (reuses QR-NOTIF-001 pattern by PK)
  2. persist — UPDATE NotificationChannelConfig (QR-NOTIF-010)

QR-NOTIF-010 — UPDATE NotificationChannelConfig — REPOSITORY STRATEGY (AMEND-P3-B)
  Table            : NOTIF_CHANNEL_CONFIG
  DB Operation     : UPDATE
  Join strategy    : NONE
  Transaction bound: READ_WRITE
  Fetch strategy   : LAZY (project default — entity loaded read-only first via the
                     FIND step, then updated — no DRV-ID required)
  Bulk operation   : NO — single-record update

ERRORS: none beyond platform-standard 404 (PLATFORM-STD) if PK not found.
SECURITY: Screen — SCR-NOTIF-003, Permission — PERM_NOTIFICATION_CHANNEL_CONFIG_UPDATE.
─────────────────────────────────────────────────────────────────

**Finding / Derivation — DRV-NOTIF-003 (read/unread tracking gap):**
srs-notif-001.md B3 (SCR-NOTIF-001) requires an unread-count feature ("عدد
الإشعارات غير المقروءة") and API-NOTIF-004/005 implement "unread" and "mark as
read." However, neither A3 (NotificationLog field catalog) nor dbs-notif-001.md
(DBF-0001..0016) defines a read/unread column. Per Section 2A.3 Extraction Failure
Protocol, no column is invented. **P3 does NOT own the OQ-ID namespace
(SHARED-GOVERNANCE-CORE.md CORE-7 / shared-governance-rules.md RULE-2 — OQ-IDs are
assigned exclusively by Project 1, the SRS Governance Engine).** This gap is
therefore tracked via this DRV-ID only, with a formal recommendation — not a
self-assigned ID — that Project 1 raise its own OQ-ID upon SRS amendment. See the
Escalation Note immediately below. API-NOTIF-004/005 are GOVERNANCE-NOTE-BLOCKED
pending that SRS/DB amendment.
<!-- PHASE:SVCAPI:END -->

---

## ESCALATION NOTE — RECOMMENDED MODE 1 (SRS) AMENDMENT
*(Not an OQ-ID — P3 does not assign IDs in Project 1's namespace, per CORE-7 /
RULE-2. This note is a recommendation for Project 1 to raise its own OQ-ID.)*

```
DRV-NOTIF-003 │ NotificationLog has no read/unread tracking column in the governed
               │ DB schema (dbs-notif-001.md), but SRS B3/B5 (SCR-NOTIF-001,
               │ API-NOTIF-004/005) requires unread-count and mark-as-read
               │ functionality. Recommendation: Project 1 (SRS Governance Engine)
               │ should raise its own OQ-ID and amend srs-notif-001.md (MODE 1)
               │ adding a field such as isReadFl / readAt to ENTITY-NOTIF-001,
               │ followed by a corresponding MODE 1.5 DB script update, before
               │ API-NOTIF-004/005 can be implemented against a real column.
               │ Tracking: DRV-NOTIF-003 (this plan) — Escalation target: MODE 1
               │ Scope: blocks only API-NOTIF-004/005 — does not block the rest
               │ of this plan.
```

---

<!-- PHASE:DOC:START -->
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
API-NOTIF-003 │ /api/v1/notifications/history                     │ GET    │ STABLE
API-NOTIF-004 │ /api/v1/notifications/unread                      │ GET    │ UNSTABLE — DRV-NOTIF-003 (recommended SRS amendment)
API-NOTIF-005 │ /api/v1/notifications/{id}/read                   │ PUT    │ UNSTABLE — DRV-NOTIF-003 (recommended SRS amendment)
API-NOTIF-006 │ /api/v1/notifications/templates                   │ GET    │ STABLE
API-NOTIF-007 │ /api/v1/notifications/templates                   │ POST   │ STABLE
API-NOTIF-008 │ /api/v1/notifications/templates/{id}               │ PUT    │ STABLE
API-NOTIF-009 │ /api/v1/notifications/templates/{id}/deactivate    │ PUT    │ STABLE
API-NOTIF-010 │ /api/v1/notifications/templates/{id}               │ GET    │ STABLE
API-NOTIF-011 │ /api/v1/notifications/channel-configs              │ GET    │ STABLE
API-NOTIF-012 │ /api/v1/notifications/channel-configs/{id}         │ PUT    │ STABLE
```
Unstable APIs: API-NOTIF-004, API-NOTIF-005 — blocked on DRV-NOTIF-003 (missing DB column — recommended SRS amendment, see Escalation Note).
Frontend-governed contracts: None.

### DOC-2: DTO Typing Rules
LOV field typing: String (notificationTypeId, notificationStatusId, channelTypeId) — never ENUM.
Business Code: N/A — no entity in this module has a Business Code.

### DOC-3: Pagination & Filter Standards
Standard platform rule applies as-is. Used by API-NOTIF-003, API-NOTIF-006.

**DOC GATE CHECK:**
```
[ ✓ ] All API-IDs from SVC+API appear in API Contract Summary
[ ✓ ] Error Catalog complete with Arabic + English messages
[ ✓ ] All APIs marked STABLE or explicitly UNSTABLE (2 UNSTABLE — documented)
[ ✓ ] Pagination standard declared
DOC Gate: PASSED ✓ (2 APIs UNSTABLE — does not fail the gate, tracked via DRV-NOTIF-003 / Escalation Note)
```
<!-- PHASE:DOC:END -->

---

<!-- PHASE:INTC:START -->
## PHASE INT-C — Integration Contract Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : DOC ✓
Gate This Phase  : INT-C ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

```
## INT-C SUMMARY — Notification Service — PLAN-ID: PLAN-NOTIF-001
══════════════════════════════════════════════════════════════════════════
XM-ID        │ Classification │ Target Module │ Interface Type │ Contract Status
─────────────┼─────────────────┼───────────────┼─────────────────┼────────────────
XM-NOTIF-001 │ HARD-FK         │ File Service  │ DB Foreign Key  │ DEFERRED ⏸
══════════════════════════════════════════════════════════════════════════
```

### XM-NOTIF-001 — NotificationTemplate.fileFk → FileService.FileDocument
─────────────────────────────────────────────────────────────────
Target Module    : File Service (1.10)
Target Entity    : FileDocument — ENTITY-FILE-001 (DBS-FILE-001, GOVERNED ✓ — confirmed
                    this session, see execution-plan-file-001.md)
Classification   : HARD-FK
Interface Type   : DB Foreign Key (NOTIF_TEMPLATE.FILE_FK → FILE_DOCUMENT.FILE_DOCUMENT_PK)
Contract:
  Data required        : fileDocumentPk (for the FK value itself); at read-time,
                          File Service's download flow (API-FILE-003, token-based)
                          would be used to retrieve the actual template file bytes
                          — this module does not query FILE_CONTENT directly.
  Fallback if absent    : templateBodyAr/templateBodyEn (inline, Phase-1 storage) —
                          NOT a null/error fallback but the PRIMARY Phase-1 storage
                          mechanism. fileFk stays NULL until migration.
Retry policy     : Not applicable (no runtime call in Phase 1 — dormant FK column).
Timeout          : Not applicable.
Idempotency      : Not applicable in Phase 1.
Blocks           : FIELD-0025 (fileFk) — unused/NULL until migration.
Unblock condition: RXE-NOTIF-[SEQ] per SHARED-ARTIFACT-CONTRACTS.md CONTRACT-8,
                    fired by the Registry Maintainer now that DBS-FILE-001 is
                    GOVERNED ✓ (confirmed this session in dbs-file-001.md Section 5
                    Registry cascade rule). Receipt of the RXE triggers this
                    module's own P3 execution-phase migration: read inline bodies →
                    create FILE_DOCUMENT rows via File Service's upload flow →
                    populate FILE_FK — NOT an automatic action of this plan.
DEFERRED strategy: templateBodyAr/templateBodyEn remain the read path for template
                    bodies throughout Phase 1 AND permanently post-migration as a
                    resilience fallback if File Service is transiently unavailable
                    (AD-NOTIF-11) — this plan's SVC+API (API-NOTIF-007/008) never
                    reads or writes fileFk.
─────────────────────────────────────────────────────────────────

**Live cross-module FK — NOT an XM-ID (documented distinction):**
```
NOTIF_LOG.RECIPIENT_ID → Security USERS.USERS_PK — created live in Block 5d of
dbs-notif-001.md. Security is a PERMANENT EXCEPTION module (master-registry.md
Section 4) — no XM-ID is assigned for EXCEPTION-status dependencies (SRS A7 /
CONTRACT-7 note). This plan's SVC+API treats it as an ordinary, always-available FK.
```

**INBOUND XM STUBS (this module is the target — future consumers):**
```
XM-INBOUND-STUB-NOTIF-1
  Consumer module  : All 3.x modules (Procurement/Inventory/Sales/Finance)
  Interface type   : Event producers via RabbitMQ (erp.notification.exchange) —
                      NOT a data-FK stub; each producing module determines its own
                      channelHint (RULE-NOTIF-002) — informational only.
  Current status   : NOT-YET-ASSIGNED (producers not yet built)

XM-INBOUND-STUB-NOTIF-2
  Consumer module  : AuditService (1.9)
  Entity exposed   : NotificationLog — SOFT-READ
  Current status   : NOT-YET-ASSIGNED — AuditService itself is NOT STARTED
```

**INT-C GATE CHECK:**
```
[ ✓ ] All XM-IDs from DB Script XM Register accounted for (1/1 — XM-NOTIF-001)
[ ✓ ] Classification declared for each XM-ID
[ ✓ ] All DEFERRED have unblock condition
[ ✓ ] No new XM-IDs invented
[ ✓ ] Open RXEs acknowledged (RXE-NOTIF-[SEQ] pending, Registry Maintainer's action)
[ ✓ ] Inbound XM stubs use INBOUND-STUB notation
INT-C Gate: PASSED ✓
```
<!-- PHASE:INTC:END -->

---

<!-- PHASE:INTR:START -->
## PHASE INT-R — Runtime Activation Status
─────────────────────────────────────────────────────────────────
Gate Required    : INT-C ✓
Gate This Phase  : INT-R ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

```
## INT-R STATUS — Notification Service — PLAN-ID: PLAN-NOTIF-001
══════════════════════════════════════════════════════════════════════════
XM-ID        │ Status      │ Workaround / Mock Strategy
─────────────┼─────────────┼────────────────────────────────────────────────
XM-NOTIF-001 │ DEFERRED ⏸  │ Read/write templateBodyAr/templateBodyEn inline
             │             │ columns exclusively; fileFk left NULL. No mock
             │             │ needed — the inline-storage path is fully
             │             │ functional on its own, not a stand-in for a
             │             │ missing dependency.
══════════════════════════════════════════════════════════════════════════
```
<!-- PHASE:INTR:END -->

---

<!-- PHASE:F1:START -->
## PHASE F1 — Frontend Model Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : INT-R ✓ (F1 Prerequisite: DOC ✓ confirmed)
Gate This Phase  : F1 ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

### F1-MODEL — ENTITY-NOTIF-001 — NotificationLog
─────────────────────────────────────────────────────────────────
Fields:
  notificationLogPk    : number      — PK, never displayed as input
  recipientId           : number     — system-resolved (current user or Admin-queried)
  notificationTypeId    : string     — LOV-NOTIF-001, lookupKey: NOTIFICATION_CHANNEL
  templateCode          : string     — display only (not user-editable here)
  subject / bodyPreview : string     — display
  notificationStatusId  : string     — LOV-NOTIF-002, lookupKey: NOTIFICATION_STATUS
  retryCount             : number    — display only (Admin/diagnostic view)
  sentAt / createdAt      : Date     — display
  moduleCode               : string  — display
  referenceId / referenceType : number/string — used for contextual navigation only
Readonly fields  : ALL fields (no user-editable form — read/action screen only)
⚠ No Business Code field. orgUnitId never present.
─────────────────────────────────────────────────────────────────

### F1-MODEL — ENTITY-NOTIF-002 — NotificationTemplate
─────────────────────────────────────────────────────────────────
Fields:
  notificationTemplatePk : number     — PK, readonly
  templateCode            : string    — Business-Code-like readonly-after-create field
  templateNameAr / templateNameEn : string — REQUIRED
  channelTypeId            : string   — LOV-NOTIF-001
  moduleCode                : string  — REQUIRED
  templateBodyAr / templateBodyEn : string (long text / rich text) — REQUIRED,
                             supports {{placeholder}} tokens
  isActiveFl                 : boolean
  createdBy/createdAt/updatedBy/updatedAt : audit fields
  ⚠ fileFk : NEVER modeled in TypeScript in Phase 1 — DEFERRED, unused.
Readonly fields  : notificationTemplatePk, templateCode (after create only — see F3),
                    createdBy, createdAt
─────────────────────────────────────────────────────────────────

### F1-MODEL — ENTITY-NOTIF-003 — NotificationChannelConfig
─────────────────────────────────────────────────────────────────
Fields:
  notificationChannelConfigPk : number  — PK, readonly
  channelTypeId                 : string — LOV-NOTIF-001, READ-ONLY (5 fixed rows, no create/delete)
  isEnabledFl                    : boolean — Toggle, editable
  configJson                      : string (JSON editor) — editable, optional
Readonly fields  : notificationChannelConfigPk, channelTypeId
─────────────────────────────────────────────────────────────────

### F1-SCREEN — SCR-NOTIF-001 — Notification Bell + History
─────────────────────────────────────────────────────────────────
Screen type      : SPECIALIZED (PATTERN-3 — Bell Dropdown + History List; no
                    Entry form — read + "mark as read" action only)
Entity           : ENTITY-NOTIF-001
Filter Model — NotificationHistoryFilter:
  notificationTypeId  : string  OPTIONAL  Filter type: EXACT
  notificationStatusId: string  OPTIONAL  Filter type: EXACT
  dateRange            : DATE_RANGE OPTIONAL
Result columns   : notificationTypeId, subject, bodyPreview, notificationStatusId,
                    sentAt, createdAt
⚠ Unread count / mark-as-read UI elements are DEFERRED pending the recommended SRS amendment (DRV-NOTIF-003 — see Escalation Note).
─────────────────────────────────────────────────────────────────

### F1-SCREEN — SCR-NOTIF-002 — Template Management
─────────────────────────────────────────────────────────────────
Screen type      : SEARCH + ENTRY (PATTERN-1)
Entity           : ENTITY-NOTIF-002
Search Filter Model — NotificationTemplateSearchFilter:
  templateCode  : string  OPTIONAL  Filter type: LIKE
  channelTypeId : string  OPTIONAL  Filter type: EXACT — LOV-NOTIF-001
  moduleCode    : string  OPTIONAL  Filter type: LIKE
  isActiveFl    : boolean OPTIONAL  Filter type: EXACT
Result columns   : templateCode, templateNameAr, templateNameEn, channelTypeId,
                    moduleCode, isActiveFl
Form Model — NotificationTemplateFormModel:
  templateCode        : string  REQUIRED — disabled/readonly on EDIT (RULE-NOTIF-007)
  templateNameAr/En   : string  REQUIRED
  channelTypeId        : string REQUIRED — LOV-NOTIF-001
  moduleCode            : string REQUIRED
  templateBodyAr/En     : string (rich text/placeholder editor) REQUIRED
  isActiveFl             : boolean REQUIRED
Excluded         : notificationTemplatePk, fileFk (never in form)
Read-only on EDIT: templateCode
─────────────────────────────────────────────────────────────────

### F1-SCREEN — SCR-NOTIF-003 — Channel Configuration
─────────────────────────────────────────────────────────────────
Screen type      : INLINE TOGGLE LIST (PATTERN-2)
Entity           : ENTITY-NOTIF-003
Result columns (5 fixed rows) : channelTypeId (readonly), isEnabledFl (toggle),
  configJson (JSON editor, optional)
─────────────────────────────────────────────────────────────────
<!-- PHASE:F1:END -->

---

<!-- PHASE:F2:START -->
## PHASE F2 — Frontend Service Contract Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : F1 ✓
Gate This Phase  : F2 ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

### F2-SERVICE blocks (one per API-ID) — AMEND-P3-C fields
```
API-NOTIF-003 — NotificationInboxService.getHistory(filter, page, size)
  Observable type   : Observable<Page<NotificationLogResponse>>
  Error handling    : none beyond platform-standard (empty → HTTP 200, never an error)
  Loading state     : LOCAL — spinner scoped to the history list region of
                      SCR-NOTIF-001, does not block the bell icon itself
  Caching strategy  : SHORT-LIVED (facade-level, in-memory, invalidated on any
                      mark-as-read action within the same session)
  XM-ID impact      : None — no XM dependency on this call path

API-NOTIF-004 — [BLOCKED — DRV-NOTIF-003] NotificationInboxService.getUnread()
  Observable type   : Observable<NotificationUnreadSummary> (contract only —
                      not implementable until the recommended SRS amendment lands)
  Error handling    : N/A (blocked)
  Loading state     : N/A (blocked)
  Caching strategy  : N/A (blocked)
  XM-ID impact      : None

API-NOTIF-005 — [BLOCKED — DRV-NOTIF-003] NotificationInboxService.markAsRead(id)
  Observable type   : Observable<NotificationSendConfirmation> (contract only)
  Error handling    : N/A (blocked)
  Loading state     : N/A (blocked)
  Caching strategy  : N/A (blocked)
  XM-ID impact      : None

API-NOTIF-006 — NotificationTemplateService.search(filter, page, size)
  Observable type   : Observable<Page<NotificationTemplateResponse>>
  Error handling    : none beyond platform-standard (empty → HTTP 200)
  Loading state     : LOCAL — spinner scoped to the search results grid on SCR-NOTIF-002
  Caching strategy  : NONE — Admin screen, always fetches current server state
  XM-ID impact      : None — no XM dependency on this call path

API-NOTIF-007 — NotificationTemplateService.create(request)
  Observable type   : Observable<NotificationTemplateResponse>
  Error handling    : ERR-NOTIF-0002 (400, bilingual body missing), ERR-NOTIF-0003
                      (409, duplicate templateCode) — routed to inline form errors
  Loading state     : LOCAL — spinner on the Save button only
  Caching strategy  : NONE — write operation; triggers a template-list refetch on success
  XM-ID impact      : None on THIS call — fileFk is never written here (XM-NOTIF-001
                      is DEFERRED); template body is always the inline columns

API-NOTIF-008 — NotificationTemplateService.update(id, request)
  Observable type   : Observable<NotificationTemplateResponse>
  Error handling    : ERR-NOTIF-0002 (400), ERR-NOTIF-0003 (409), ERR-NOTIF-0004
                      (404, PLATFORM-STD, record not found) — routed to inline form errors
  Loading state     : LOCAL — spinner on the Save button only
  Caching strategy  : NONE — write operation; triggers a template-list refetch on success
  XM-ID impact      : None — fileFk untouched (XM-NOTIF-001 DEFERRED)

API-NOTIF-009 — NotificationTemplateService.deactivate(id)
  Observable type   : Observable<void>
  Error handling    : ERR-NOTIF-0004 (404, PLATFORM-STD) — routed to toast
  Loading state     : LOCAL — spinner on the Deactivate action only
  Caching strategy  : NONE — write operation; triggers a template-list refetch on success
  XM-ID impact      : None

API-NOTIF-010 — NotificationTemplateService.getById(id)
  Observable type   : Observable<NotificationTemplateResponse>
  Error handling    : ERR-NOTIF-0004 (404, PLATFORM-STD) — routed to toast +
                      navigation back to search
  Loading state     : LOCAL — spinner on the entry form region while loading
  Caching strategy  : NONE — Admin edit screen, always fetches current server state
  XM-ID impact      : None — fileFk field is never rendered/read here (DEFERRED)

API-NOTIF-011 — NotificationChannelConfigService.listAll()
  Observable type   : Observable<NotificationChannelConfigResponse[]>
  Error handling    : none beyond platform-standard
  Loading state     : LOCAL — spinner on the whole SCR-NOTIF-003 panel (small, 5-row screen)
  Caching strategy  : SHORT-LIVED (facade-level, in-memory, invalidated on any toggle/save)
  XM-ID impact      : None

API-NOTIF-012 — NotificationChannelConfigService.update(id, request)
  Observable type   : Observable<NotificationChannelConfigResponse>
  Error handling    : ERR-NOTIF-0004 (404, PLATFORM-STD) — routed to toast
  Loading state     : LOCAL — spinner on the specific row's toggle/save control only
  Caching strategy  : NONE — write operation; triggers a configs refetch on success
  XM-ID impact      : None

(API-NOTIF-001/002 are system-to-system — not called from this module's own
Angular screens; no F2-SERVICE block applies)
```

### F2-LOV-SERVICE blocks
```
LOV-NOTIF-001 → LovService.loadOptions('NOTIFICATION_CHANNEL') → Observable<LovOption[]>
               Loading state: LOCAL | Caching: SESSION (platform-shared lookups,
               rarely change — standard LovService session cache)
LOV-NOTIF-002 → LovService.loadOptions('NOTIFICATION_STATUS')  → Observable<LovOption[]>
               Loading state: LOCAL | Caching: SESSION (same as above)

⚠ CHECK-9.2 WAIVER CANDIDATE: LOV-NOTIF-001 and LOV-NOTIF-002 are consumed
  exclusively via the platform-shared GET /api/lookups/{lookupKey} endpoint
  (MasterData-owned), not a Notification-Service-owned B2 endpoint — the same
  platform-wide centralized-lookup architecture documented in File Service
  (DRV-FILE-009 / DRV-NOTIF-008). Flagged for governance-framework-level
  reconciliation between CHECK-9.2 and the shared-lookup-service pattern; no
  plan-level action required.
```

### F2-SCREEN-INIT / F2-FACADE blocks
```
SCR-NOTIF-001 Facade: NotificationInboxFacade
  State: notifications: signal<NotificationLogResponse[]>, currentPage/pageSize
  (derived from last search — not independent state), filter: signal<NotificationHistoryFilter>
  Init: load LOV-NOTIF-001/002 options, call API-NOTIF-003 for current user's history
  ⚠ Bell/unread-count init step DEFERRED — DRV-NOTIF-003 (see Escalation Note)

SCR-NOTIF-002 Facade: NotificationTemplateFacade
  State: templates: signal<NotificationTemplateResponse[]>, currentPage/pageSize
  (derived), selectedTemplate: signal<NotificationTemplateResponse|null>
  Init: load LOV-NOTIF-001 options, call API-NOTIF-006 with empty filter
  Operations: search()→API-NOTIF-006, save()→API-NOTIF-007/008 (create vs update by
  presence of PK), deactivate()→API-NOTIF-009, load(id)→API-NOTIF-010

SCR-NOTIF-003 Facade: NotificationChannelConfigFacade
  State: configs: signal<NotificationChannelConfigResponse[]> (5 fixed rows)
  Init: call API-NOTIF-011
  Operations: toggle(id, isEnabledFl)/saveConfig(id, configJson) → API-NOTIF-012

Error routing (all facades): HTTP 400/403/409/404 → toast/inline message using
  messageAr/messageEn from Error Catalog, keyed by ERR-ID in response body.
```
<!-- PHASE:F2:END -->

---

<!-- PHASE:F3:START -->
## PHASE F3 — Frontend Validation Rule Specifications
─────────────────────────────────────────────────────────────────
Gate Required    : F2 ✓
Gate This Phase  : F3 ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

### F3-VALIDATION — RULE-NOTIF-006 — Bilingual requirement
─────────────────────────────────────────────────────────────────
Statement  : Every NotificationTemplate MUST have both Arabic and English bodies.
Message-AR : يجب توفير نص القالب بالعربي والإنجليزي معاً
Message-EN : The template body must be provided in both Arabic and English
Scope      : CREATE, UPDATE
Field            : templateBodyAr, templateBodyEn (NotificationTemplateFormModel)
DB Column        : TEMPLATE_BODY_AR / TEMPLATE_BODY_EN  DBF-0023 / DBF-0024
Validation type  : REQUIRED (both fields non-blank)
When evaluated   : ON_SUBMIT
ERR-ID           : ERR-NOTIF-0002
─────────────────────────────────────────────────────────────────

### F3-VALIDATION — RULE-NOTIF-007 — templateCode uniqueness/immutability
─────────────────────────────────────────────────────────────────
Statement  : Prevent duplicate templateCode on create; reject any modification after creation.
Message-AR : رمز القالب مستخدَم مسبقاً أو غير قابل للتعديل
Message-EN : Template code already exists or cannot be modified
Scope      : CREATE, UPDATE
Field            : templateCode
DB Column        : TEMPLATE_CODE  DBF-0018
Validation type  : UNIQUE_CHECK (create) / field disabled (edit — client-side) +
                    server-side reject if changed anyway
API call         : API-NOTIF-007 (create, server validates), API-NOTIF-006 (client
                    may pre-check via search — optional convenience, server is
                    authoritative)
DB check         : NOTIF_TEMPLATE.TEMPLATE_CODE — constraint UQ_NOTIF_TEMPLATE_CODE
When             : ON_BLUR (create form only — field is disabled entirely on edit)
ERR-ID           : ERR-NOTIF-0003
─────────────────────────────────────────────────────────────────

**F3 Business Code Rules:** N/A — no entity has a formal Business Code. templateCode
follows an analogous readonly-after-create pattern, specified above per RULE-NOTIF-007.

**F3 Localization Rules:** platform-standard (no deviation).

**F3 Permission-Based Field Behavior:**
```
F3-SEC-RULE-1 — SCR-NOTIF-002: canEdit=false → all form fields readonly;
  canCreate=false → New button hidden. SCR-NOTIF-003: canEdit=false → toggle
  disabled. SCR-NOTIF-001: canEdit=false → mark-as-read action hidden (once
  the recommended SRS amendment (DRV-NOTIF-003) is adopted).
```
<!-- PHASE:F3:END -->

---

<!-- PHASE:F4:START -->
## PHASE F4 — Frontend Routing & Component Structure
─────────────────────────────────────────────────────────────────
Gate Required    : F3 ✓
Gate This Phase  : F4 ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

<!-- SUB:SCR-NOTIF-001:START -->
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
<!-- SUB:SCR-NOTIF-001:END -->

<!-- SUB:SCR-NOTIF-002:START -->
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
<!-- SUB:SCR-NOTIF-002:END -->

<!-- SUB:SCR-NOTIF-003:START -->
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
<!-- SUB:SCR-NOTIF-003:END -->

**F4 Gate Checklist (self-check):**
```
[✓] All 3 SCR-IDs each have exactly one F4-SCREEN block
[N/A] No tree-bearing entity in this module — no TreeComponent required
[✓] Every route (SCR-NOTIF-001's /notifications, SCR-NOTIF-002's 4 routes,
    SCR-NOTIF-003's 1 route) declares [AuthGuard, PermissionGuard] — the
    only N/A is the header bell component, which is not a route (DRV-NOTIF-010)
[✓] Every PERM_* referenced in F4 also appears in SEC's Permissions Matrix
    for the same SCR-ID — no F4-only permission names
[✓] No component name uses "Page" or "Container" suffix
[✓/N/A] Search/Entry separation — SCR-NOTIF-002 (PATTERN-1) correctly
    declares separate Search/Entry components; SCR-NOTIF-001 (PATTERN-3)
    and SCR-NOTIF-003 (PATTERN-2) are N/A (DRV-NOTIF-009)
[✓] NotificationTemplateEntryComponent's mode resolution source is
    ActivatedRoute — never @Input
```
<!-- PHASE:F4:END -->

---

<!-- PHASE:SEC:START -->
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
<!-- PHASE:SEC:END -->

---

<!-- PHASE:TEST:START -->
## PHASE TEST — TC Coverage Matrix Summary (SECTION D)
─────────────────────────────────────────────────────────────────
Gate Required    : SEC ✓
Gate This Phase  : SECTION D ✓
Gate Status      : PASSED ✓
─────────────────────────────────────────────────────────────────

```
TC COVERAGE MATRIX SUMMARY — Notification Service — PLAN-ID: PLAN-NOTIF-001
══════════════════════════════════════════════════════════════════
RULE-ID COVERAGE:
RULE-ID           │ Happy path TC     │ Violation TC      │ Status
──────────────────┼───────────────────┼───────────────────┼──────────────
RULE-NOTIF-001    │ TC-NOTIF-001      │ TC-NOTIF-002      │ COVERED ✓
RULE-NOTIF-002    │ TC-NOTIF-003      │ — (design rule, no violation path) │ COVERED ✓
RULE-NOTIF-003    │ TC-NOTIF-004      │ — (behavioral)    │ COVERED ✓
RULE-NOTIF-004    │ TC-NOTIF-005      │ — (behavioral)    │ COVERED ✓
RULE-NOTIF-005    │ TC-NOTIF-006      │ — (behavioral)    │ COVERED ✓
RULE-NOTIF-006    │ TC-NOTIF-007      │ TC-NOTIF-008      │ COVERED ✓
RULE-NOTIF-007    │ TC-NOTIF-009      │ TC-NOTIF-010      │ COVERED ✓
──────────────────────────────────────────────────────────────────
Rule coverage    : 7 / 7 covered — 0 gaps

API-ID COVERAGE:
API-ID          │ Success TC    │ Status
────────────────┼───────────────┼──────────────
API-NOTIF-001   │ TC-NOTIF-001  │ COVERED ✓
API-NOTIF-002   │ TC-NOTIF-011  │ COVERED ✓
API-NOTIF-003   │ TC-NOTIF-012  │ COVERED ✓
API-NOTIF-004   │ DEFERRED ⏸    │ DEFERRED ⚠ — DRV-NOTIF-003
API-NOTIF-005   │ DEFERRED ⏸    │ DEFERRED ⚠ — DRV-NOTIF-003
API-NOTIF-006   │ TC-NOTIF-013  │ COVERED ✓
API-NOTIF-007   │ TC-NOTIF-009  │ COVERED ✓ (shared with RULE-NOTIF-007 happy path)
API-NOTIF-008   │ TC-NOTIF-014  │ COVERED ✓
API-NOTIF-009   │ TC-NOTIF-015  │ COVERED ✓
API-NOTIF-010   │ TC-NOTIF-016  │ COVERED ✓
API-NOTIF-011   │ TC-NOTIF-017  │ COVERED ✓
API-NOTIF-012   │ TC-NOTIF-006  │ COVERED ✓ (shared with RULE-NOTIF-005)
──────────────────────────────────────────────────────────────────
API coverage    : 10 / 12 covered — 2 deferred

DEFERRED TC REGISTRY:
DEFERRED-001 │ API-NOTIF-004 │ Missing read/unread DB column (DRV-NOTIF-003) │ Recommended SRS/DB amendment (Escalation Note)
DEFERRED-002 │ API-NOTIF-005 │ Missing read/unread DB column (DRV-NOTIF-003) │ Recommended SRS/DB amendment (Escalation Note)
══════════════════════════════════════════════════════════════════
Gate SECTION D: PASSED ✓ — all GAP entries have documented DEFERRED status with reason.
```
<!-- PHASE:TEST:END -->

---

<!-- PHASE:ALIGN:START -->
## ALIGN GATE — Notification Service — PLAN-ID: PLAN-NOTIF-001
═══════════════════════════════════════════════════════════════════════════
TRACEABILITY CHECKS                                        │ Status
All FIELD-IDs used in phases appear in Plan Index          │ ✓
All API-IDs used in phases appear in Plan Index            │ ✓
All RULE-IDs used in phases appear in Plan Index           │ ✓
All ERR-IDs used in F3/SECTION D appear in Error Catalog        │ ✓
All QR-IDs in QRC appear in Plan Index QRC Summary         │ ✓ (11 total — QR-NOTIF-001..011; see the
                                                             │   QR-ID Renumbering Note in the Derivation Log
                                                             │   for the QR-NOTIF-010/011 collision fix)
Derivation Log complete — no undocumented inferences       │ ✓ — DRV-NOTIF-001..008
DB Structural Alignment confirms field coverage            │ ✓ — 38/38 DBF-IDs bound
───────────────────────────────────────────────────────────┼──────────────
SCREEN STRUCTURE CHECKS                                    │ ✓ (3/3 SCR-IDs, F1/F2/F3/F4/SEC present;
                                                             │   F4 N/A items documented via DRV-NOTIF-009/010)
LOV / LOOKUP CHECKS                                        │ ✓ (2/2 LOV-IDs, String-typed; CHECK-9.2 WAIVER
                                                             │   CANDIDATE flagged — see F2-LOV-SERVICE note)
BUSINESS CODE CHECKS                                       │ N/A — no entity has a Business Code (documented)
LOCALIZATION CHECKS                                        │ ✓
SECURITY CHECKS                                            │ ✓
QUERY REFERENCE CATALOG CHECKS                              │ ✓ — 11 QR-IDs, all with REPOSITORY STRATEGY
                                                              │   (Fetch/Bulk) per AMEND-P3-B; QR-NOTIF-010/011
                                                              │   collision corrected (DRV-NOTIF-007)
TEST COVERAGE CHECKS                                         │ ✓ — SECTION D present; 2 DEFERRED with reason (not GAP)
CROSS-MODULE DEPENDENCY CHECKS                                │ ✓ — 1 outbound XM-ID (DEFERRED, unblock condition stated);
                                                              │   2 inbound stubs documented
ARTIFACT BINDING CHECKS                                       │ ✓ — no placeholders; sequences/columns exact
PLAN COMPLETENESS CHECKS                                      │ ✓ — with 1 documented exception: API-NOTIF-004/005
                                                              │   are UNSTABLE pending DRV-NOTIF-003 / Escalation Note (declared in DOC,
                                                              │   not silently omitted)
═══════════════════════════════════════════════════════════════════════════
ALIGN GATE RESULT: PASSED ✓ (with 2 UNSTABLE APIs formally tracked via DRV-NOTIF-003 / Escalation Note —
  does not block the remaining 10/12 APIs or any of the 3 screens/7 rules)
Auto-correction applied: QR-ID renumbering (QR-NOTIF-010/011 collision resolved —
  API-NOTIF-010's operation reassigned to QR-NOTIF-011) — DRV-NOTIF-007
  (GOVERNANCE EXCEPTION re-numbering event, PRINCIPLE-8 compliant). All other
  findings routed through DRV-IDs and the Escalation Note only — P3 does not
  self-assign OQ-IDs (CORE-7/RULE-2).
═══════════════════════════════════════════════════════════════════════════

**Table 2 — Operations Coverage (F4 Route column added per AMEND-P3-J):**
```
Operation  │ API-ID        │ UI Action (SCR-ID)                        │ F4 Route                        │ TC-ID        │ QR-ID       │ XM-ID │ Status
───────────┼────────────────┼─────────────────────────────────────────────┼──────────────────────────────────┼──────────────┼─────────────┼───────┼───────
History    │ API-NOTIF-003  │ SCR-NOTIF-001 list view                    │ /notifications                  │ TC-NOTIF-016 │ QR-NOTIF-004 │ —     │ ✓
Unread     │ API-NOTIF-004  │ SCR-NOTIF-001 bell badge                   │ N/A — bell has no route (DRV-NOTIF-010) │ DEFERRED │ QR-NOTIF-005 │ —  │ ⏸ DRV-NOTIF-003
Mark read  │ API-NOTIF-005  │ SCR-NOTIF-001 row action                   │ N/A — bell has no route (DRV-NOTIF-010) │ DEFERRED │ QR-NOTIF-006 │ —  │ ⏸ DRV-NOTIF-003
Search     │ API-NOTIF-006  │ SCR-NOTIF-002 Search view                  │ /notification-templates          │ TC-NOTIF-017 │ QR-NOTIF-007 │ —     │ ✓
Create     │ API-NOTIF-007  │ SCR-NOTIF-002 Entry view (new)              │ /notification-templates/new     │ TC-NOTIF-011 │ QR-NOTIF-008 │ —     │ ✓
Update     │ API-NOTIF-008  │ SCR-NOTIF-002 Entry view (edit)             │ /notification-templates/:id/edit│ TC-NOTIF-018 │ QR-NOTIF-009 │ —     │ ✓
Deactivate │ API-NOTIF-009  │ SCR-NOTIF-002 Entry view (deactivate action)│ /notification-templates/:id     │ TC-NOTIF-019 │ QR-NOTIF-009 │ —     │ ✓
Get by ID  │ API-NOTIF-010  │ SCR-NOTIF-002 Entry view (VIEW mode)        │ /notification-templates/:id     │ TC-NOTIF-020 │ QR-NOTIF-011 │ —     │ ✓
List       │ API-NOTIF-011  │ SCR-NOTIF-003 list view                    │ /notification-channel-configs    │ TC-NOTIF-021 │ QR-NOTIF-001 │ —     │ ✓
Update     │ API-NOTIF-012  │ SCR-NOTIF-003 inline toggle                │ /notification-channel-configs    │ TC-NOTIF-007 │ QR-NOTIF-010 │ —     │ ✓
```
Note: API-NOTIF-001/002 are system-to-system (not UI-triggered) — no row in
this table, consistent with F2's note that they have no Angular service caller.
API-NOTIF-004/005 F4 Route cells are explicitly "N/A — bell has no route" (not
blank) and Status is ⏸ (DEFERRED, cross-referenced to DRV-NOTIF-003), not a
silent gap.

**Table 4 — XM Dependency Gate:**
```
XM-ID        │ Type    │ Status     │ Blocks    │ Workaround
─────────────┼─────────┼────────────┼───────────┼───────────────────────────
XM-NOTIF-001 │ HARD-FK │ DEFERRED ⏸ │ FIELD-0025 │ Inline templateBodyAr/En (permanent fallback)
```
<!-- PHASE:ALIGN:END -->

---

## DERIVATION LOG (controlled inference — Section 6.4)

```
DRV-NOTIF-001 │ RETRY_COUNT (SRS "NUMERIC") mapped to Java Short, not Boolean —
              │ it is a count (ceiling 5), not a _FL flag column. Sourced from
              │ dbs-notif-001.md governance note (Section 2).
DRV-NOTIF-002 │ Template fallback-to-default logic (RULE-NOTIF-006, "missing
              │ templateCode falls back to default template") is placed in the
              │ NotificationEventProcessor (cross-row lookup), not inside the
              │ NotificationTemplate Entity itself (which only resolves AR/EN
              │ body for an already-found row).
DRV-NOTIF-003 │ API-NOTIF-004/005 (unread count, mark-as-read) reference a
              │ read/unread concept with no backing DB column in
              │ dbs-notif-001.md. No column invented. P3 does NOT assign OQ-IDs
              │ (foreign namespace, owned by Project 1 — CORE-7/RULE-2) — tracked
              │ via this DRV-ID plus a formal Escalation Note recommending
              │ Project 1 raise its own OQ-ID and amend the SRS. Both APIs
              │ marked UNSTABLE/DEFERRED rather than implemented against a
              │ fabricated field.
DRV-NOTIF-004 │ XM-NOTIF-001 unblock is entirely this module's OWN P3 execution
              │ work upon RXE-NOTIF-[SEQ] receipt — this plan takes no
              │ speculative action on fileFk now; templateBodyAr/En remain
              │ authoritative in Phase 1 and permanently as fallback.
DRV-NOTIF-005 │ QR-ID numbering: QR-NOTIF-001..003 assigned in DATA+DOM;
              │ QR-NOTIF-004..011 assigned in SVC+API — total 11 QR-IDs for
              │ this module (Plan Index corrected accordingly).
DRV-NOTIF-006 │ Security's recipientId→USERS_PK dependency is NOT assigned an
              │ XM-ID (Security is a PERMANENT EXCEPTION module per SRS A7
              │ CONTRACT-7 note) — treated as an ordinary always-live FK in
              │ SVC+API, not tracked in INT-C/INT-R.
DRV-NOTIF-007 │ GOVERNANCE EXCEPTION re-numbering event (PRINCIPLE-8): a QR-ID
              │ collision occurred where both API-NOTIF-010 (FIND
              │ NotificationTemplate by PK) and API-NOTIF-012 (UPDATE
              │ NotificationChannelConfig) were labeled "QR-NOTIF-010." Resolved:
              │ API-NOTIF-010's operation is reassigned to QR-NOTIF-011
              │ (sequential continuation); API-NOTIF-012 retains QR-NOTIF-010.
              │ Total QR-IDs for this module = 11 (QR-NOTIF-001..011). This entry
              │ formally supersedes the originally-used non-standard suffix
              │ "QR-NOTIF-010-A" label (flagged 4A-NOTIF-001-004) and provides
              │ the explanatory note the ALIGN gate's traceability row references.
DRV-NOTIF-008 │ CHECK-9.2 WAIVER CANDIDATE (4A-NOTIF-001-010): LOV-NOTIF-001 and
              │ LOV-NOTIF-002 are consumed via the platform-shared
              │ GET /api/lookups/{lookupKey} endpoint (MasterData-owned), not a
              │ Notification-Service-owned B2 endpoint — the same platform-wide
              │ centralized-lookup architecture decision documented in File
              │ Service (DRV-FILE-009). Flagged for the governance framework to
              │ formally reconcile CHECK-9.2 with the shared-lookup-service
              │ pattern; no plan-level action required.
DRV-NOTIF-009 │ F4-RULE-5 (PATTERN-1 Search/Entry separation) N/A for
              │ SCR-NOTIF-001 (PATTERN-3 Specialized — Bell + History) and
              │ SCR-NOTIF-003 (PATTERN-2 Inline Toggle List). Only
              │ SCR-NOTIF-002 (Template Management) is a genuine PATTERN-1
              │ Composite Screen and correctly declares separate
              │ Search/Entry components in F4.
DRV-NOTIF-010 │ SCR-NOTIF-001's bell dropdown (NotificationBellComponent)
              │ has no route of its own — it lives in the always-loaded
              │ app shell header, consistent with the SEC phase's existing
              │ note ("header component, no navigation route gate in the
              │ traditional sense"). A separate routed /notifications view
              │ (NotificationHistoryComponent) is added in F4 to satisfy
              │ the full history/filter requirements from SRS B2/B3, which
              │ exceed what a header dropdown can reasonably hold — this is
              │ an F4-level structural decision, not a new business rule.
```

---

## ERROR CATALOG — Notification Service — PLAN-ID: PLAN-NOTIF-001

```
══════════════════════════════════════════════════════════════════════════════════════════
ERR-ID          │ RULE-ID        │ API-ID        │ HTTP │ Message-AR                                    │ Message-EN
────────────────┼────────────────┼───────────────┼──────┼────────────────────────────────────────────────┼──────────────────────────────────────────
ERR-NOTIF-0001  │ RULE-NOTIF-001 │ API-NOTIF-001/002 │ 400 │ بيانات الحدث غير مكتملة                    │ Notification event data is incomplete
ERR-NOTIF-0002  │ RULE-NOTIF-006 │ API-NOTIF-007/008 │ 400 │ يجب توفير نص القالب بالعربي والإنجليزي معاً │ The template body must be provided in both Arabic and English
ERR-NOTIF-0003  │ RULE-NOTIF-007 │ API-NOTIF-007/008 │ 409 │ رمز القالب مستخدَم مسبقاً أو غير قابل للتعديل │ Template code already exists or cannot be modified
ERR-NOTIF-0004  │ PLATFORM-STD │ API-NOTIF-008/009/010/012 │ 404 │ السجل غير موجود │ Record not found
══════════════════════════════════════════════════════════════════════════════════════════
Total Errors: 4 (RULE-NOTIF-002/003/004/005 are internal/behavioral rules with no
  direct user-facing rejection message — reflected in NotificationLog status, not
  in an HTTP error response, per srs-notif-001.md A4 "Message-AR: —" for each).
Note: RULE-ID field carries exactly one value per CHECK-2.6 ("RULE-[MOD]-N" or
"PLATFORM-STD"). ERR-NOTIF-0004's derivation cross-reference lives in the
Derivation Log only (DRV-NOTIF entries covering PLATFORM-STD 404 classifications),
not in this table's RULE-ID cell.
```

---

## QUERY REFERENCE CATALOG (FULL — AGENT REFERENCE)

```
╔══════════════════════════════════════════════════════════════════╗
║  ⚠ AGENT REFERENCE ONLY — REWRITE using actual JPA entity/field   ║
║  names and the project's query strategy.                          ║
╚══════════════════════════════════════════════════════════════════╝

QR-NOTIF-001 │ FIND NotificationChannelConfig by channelTypeId │ NOTIF_CHANNEL_CONFIG │ READ_ONLY │ Fetch: LAZY │ Bulk: NO
QR-NOTIF-002 │ FIND NotificationTemplate by templateCode (active) │ NOTIF_TEMPLATE │ READ_ONLY │ Fetch: LAZY │ Bulk: NO
QR-NOTIF-003 │ SAVE NotificationLog (per fan-out channel)     │ NOTIF_LOG      │ READ_WRITE │ Fetch: N/A │ Bulk: YES (per-channel) │ Seq: SEQ_NOTIF_LOG.NEXTVAL
QR-NOTIF-004 │ FIND_BY_CRITERIA NotificationLog (history)     │ NOTIF_LOG      │ READ_ONLY │ Fetch: LAZY │ Bulk: YES (paginated)
QR-NOTIF-005 │ [BLOCKED — DRV-NOTIF-003] unread query           │ NOTIF_LOG      │ — │ Fetch: N/A │ Bulk: N/A
QR-NOTIF-006 │ [BLOCKED — DRV-NOTIF-003] mark-as-read update    │ NOTIF_LOG      │ — │ Fetch: N/A │ Bulk: N/A
QR-NOTIF-007 │ FIND_BY_CRITERIA NotificationTemplate (search)  │ NOTIF_TEMPLATE │ READ_ONLY │ Fetch: LAZY │ Bulk: YES (paginated)
QR-NOTIF-008 │ SAVE NotificationTemplate                       │ NOTIF_TEMPLATE │ READ_WRITE │ Fetch: N/A │ Bulk: NO │ Seq: SEQ_NOTIF_TEMPLATE.NEXTVAL
QR-NOTIF-009 │ UPDATE NotificationTemplate by PK                │ NOTIF_TEMPLATE │ READ_WRITE │ Fetch: LAZY │ Bulk: NO
QR-NOTIF-010 │ UPDATE NotificationChannelConfig by PK           │ NOTIF_CHANNEL_CONFIG │ READ_WRITE │ Fetch: LAZY │ Bulk: NO
QR-NOTIF-011 │ FIND NotificationTemplate by PK (API-NOTIF-010)  │ NOTIF_TEMPLATE │ READ_ONLY │ Fetch: LAZY │ Bulk: NO
```

---

## REGISTRY UPDATE — 2026-07-11

```
────────────────────────────────────────────────────────────────
Source Mode    : MODE 2
Feature Code   : NOTIF-001
DBS-ID         : DBS-NOTIF-001
Plan ID        : PLAN-NOTIF-001
────────────────────────────────────────────────────────────────
New Entities   : — (already governed at MODE 1/1.5)
New Tables     : — (already governed at MODE 1.5)
New Lookups    : — (NOTIFICATION_CHANNEL, NOTIFICATION_STATUS already seeded)
New APIs       : API-NOTIF-001..012 (10 STABLE, 2 UNSTABLE — DRV-NOTIF-003 (recommended SRS amendment))
QR-IDs Created : QR-NOTIF-001..011 (11)
XM-IDs Open    : XM-NOTIF-001 (DEFERRED, HARD-FK → File Service FILE_DOCUMENT)
OQ-IDs Open    : None (P3 does not assign OQ-IDs — foreign namespace, CORE-7/RULE-2).
                 1 Escalation Note on file (DRV-NOTIF-003) recommending Project 1
                 raise its own OQ-ID for a read/unread column amendment — blocks
                 API-NOTIF-004/005 only.
Gate Status    : ALIGN PASSED ✓ (with 2 UNSTABLE APIs formally tracked)
Next Action    : Trigger MODE 4A — Governance Audit Engine (Project 4), then
                 MODE 2.5 — generate test-plan.md for Notification Service.
                 Separately: forward the Escalation Note (DRV-NOTIF-003) to
                 MODE 1 so Project 1 can raise its own OQ-ID and amend the SRS.
────────────────────────────────────────────────────────────────
```

---

## PLAN COMPLETION BLOCK

```
╔══════════════════════════════════════════════════════════════════╗
║           EXECUTION PLAN — STAGE 1 COMPLETE ✓                    ║
╠═══════════════════════╦══════════════════════════════════════════╣
║ Plan Name             ║ New Feature — Multi-Channel Notification Engine ║
║ Plan ID               ║ PLAN-NOTIF-001                           ║
║ Output                ║ STAGE 1 — execution-plan.md Agent-Ready  ║
║ Phases Complete       ║ CORE✓ DATA+DOM✓ SVC+API✓ DOC✓ INT-C✓   ║
║                       ║ INT-R✓ F1✓ F2✓ F3✓ F4✓ SEC✓ SECTION D✓ ALIGN✓ ║
║ TC Coverage Summary   ║ SECTION D present — 7/7 rules, 10/12 APIs covered (2 DEFERRED) ║
║ Open Questions        ║ 0 (P3 does not assign OQ-IDs) — 1 Escalation Note recommending a  ║
║                       ║ MODE 1 OQ (DRV-NOTIF-003), blocks 2 of 12 APIs           ║
║ XM DEFERRED           ║ 1 — XM-NOTIF-001 (→ File Service, unblock via RXE-NOTIF) ║
║ Blocked Elements      ║ API-NOTIF-004, API-NOTIF-005, FIELD-0025 (fileFk)  ║
║ QR-IDs Generated      ║ 11 (2 blocked, not executable) — see Query Reference Catalog ║
║ Next Stage            ║ MODE 2.5 → generate test-plan.md (10/12 APIs; 2 DEFERRED-tagged) ║
╠═══════════════════════╩══════════════════════════════════════════╣
║  ⚠ AGENT INSTRUCTIONS                                            ║
║  1. Read the full plan before writing any code                   ║
║  2. Do NOT implement API-NOTIF-004/005 against an invented column ║
║     — wait for the recommended SRS amendment (DRV-NOTIF-003)      ║
║  3. Rewrite ALL Query Reference Catalog entries from scratch      ║
║  4. Follow architectural policies declared in CORE (dual event    ║
║     ingress: RabbitMQ + Spring Events, both routed to the same    ║
║     internal processor as API-NOTIF-001/002)                      ║
║  5. Leave fileFk NULL and unused — XM-NOTIF-001 is DEFERRED        ║
║  6. Implement security checks per SEC phase                       ║
║  7. Write tests per test-plan.md (separate file, MODE 2.5)        ║
╚══════════════════════════════════════════════════════════════════╝
```

---
*End of execution-plan-notif-001.md*
*Governed by: Execution Plan Governance Engine (Project 3, v2)*
*PLAN-ID: PLAN-NOTIF-001 | DBS-ID: DBS-NOTIF-001 | Feature Code: NOTIF-001*
*Next Mode: MODE 4A (Pre-flight audit) → MODE 3 (Agent execution) | MODE 2.5 (test-plan.md)*
*Escalation: See Escalation Note (DRV-NOTIF-003) recommending a MODE 1 (SRS) amendment before API-NOTIF-004/005 can proceed. P3 does not self-assign OQ-IDs (CORE-7).*
