<!-- Source: PHASE:CORE -->

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
