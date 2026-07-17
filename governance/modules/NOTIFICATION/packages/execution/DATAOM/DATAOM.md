<!-- Source: PHASE:DATAOM -->

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
