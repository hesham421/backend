<!-- Source: PHASE:SVCAPI -->

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
