## BUSINESS POLICIES — NOTIFICATION SERVICE
══════════════════════════════════════════════════════════════════
Module      : Notification Service (1.8)
P0 Date     : 2026-07-11 (v2 — supersedes v1)
ERP Pattern : platform-standards.md Section M.12
DB_TARGET   : POSTGRESQL_16
Session Input: ARCH-REF-1.8-NOTIFICATION-SERVICE.md v1.1.0
P1 reads    : CLIENT-SPECIFIC entries → RULE-IDs marked "Source: Client"
              Standard ERP rules → applied by P1 Section 5.4.2 directly
══════════════════════════════════════════════════════════════════

CLIENT-SPECIFIC POLICIES
──────────────────────────────────────────────────────────────────
No policies were stated in vision.md specifically for Notification.
Entries below originate from the ARCH-REF-1.8 session input and this
session's decisions — marked "Source: ARCH-REF" / "Source: P0 session"
so P1 can distinguish them from vision-text-derived policies.

POLICY-CLI-01: Event contract for inbound notification requests
  Rule   : Every NotificationEvent published to Notification Service MUST
           carry: recipientId, channelHint, templateCode, contextData,
           priority (HIGH/MEDIUM/LOW).
  Trigger: On publish (Spring Event or RabbitMQ message)
  Source : ARCH-REF AD-NOTIF-01 / RULE-NOTIF-EVENT

POLICY-CLI-02: Channel selection ownership [UPDATED 2026-07-11]
  Rule   : channelHint MUST be a single channel, a list of channels, or
           "ALL" — set exclusively by the publishing (business) module.
           The system MUST NOT infer or hardcode which channel a given
           event type or module requires; that decision belongs entirely
           to the caller. For each requested channel, the system MUST
           create an independent NotificationLog entry and evaluate that
           channel's enabled status independently — one disabled channel
           MUST NOT block delivery on the other requested channels.
  Trigger: On event publish / on send
  Source : ARCH-REF AD-NOTIF-10 (new)

POLICY-CLI-03: Retry policy on delivery failure
  Rule   : The system MUST retry a failed delivery up to 5 times with
           exponential backoff (2s → 3s → 4.5s → 6.75s), then mark the
           notification FAILED in NotificationLog without notifying the
           original sender.
  Trigger: On channel send failure
  Source : ARCH-REF AD-NOTIF-01 / RULE-NOTIF-RETRY

POLICY-CLI-04: Disabled-channel handling
  Rule   : The system MUST NOT raise an error to the sending module when
           a target channel is disabled; it MUST log the event as
           CHANNEL_DISABLED in NotificationLog instead.
  Trigger: On send attempt where NotificationChannelConfig.is_enabled_fl = 0
  Source : ARCH-REF RULE-NOTIF-CHANNEL-DISABLED

POLICY-CLI-05: Template bilingual requirement
  Rule   : Every NotificationTemplate MUST have both an Arabic and an
           English version; template language MUST be resolved from the
           recipient's user language preference (Security). A missing
           templateCode MUST fall back to a default template rather than
           fail the send.
  Trigger: On template lookup (send / schedule)
  Source : ARCH-REF RULE-NOTIF-TEMPLATE

POLICY-CLI-06: No JWT validation inside the module
  Rule   : Notification Service MUST NOT independently validate JWT
           tokens; it trusts the Security module's filter chain.
  Trigger: On any incoming request
  Source : ARCH-REF ADAPT-NOTIF-03

POLICY-CLI-07: Template storage — Phase 1 inline, File Service migration
  Rule   : Template body MUST be stored inline (template_body_ar/en) in
           Phase 1. When File Service (1.10) becomes governed, an XM
           Resolution Event (RXE-NOTIF-[SEQ]) triggers migration to
           file_id-based storage per the standard CONTRACT-8 protocol.
           Inline body columns MUST be retained post-migration as a
           resilience fallback if File Service is transiently unavailable.
  Trigger: On template creation (Phase 1) / on RXE-NOTIF receipt (future)
  Source : ARCH-REF AD-NOTIF-05 (revised) + AD-NOTIF-11 (new)

──────────────────────────────────────────────────────────────────

CUSTOM LOV VALUES
──────────────────────────────────────────────────────────────────
NotificationChannel (master-registry Section 6, updated this session):
  EMAIL, SMS, WHATSAPP, PUSH, INTERNAL

NotificationStatus (master-registry Section 6, added this session):
  PENDING, SENT, FAILED, CHANNEL_DISABLED

Phase 1 enablement (NOT a LOV value — NotificationChannelConfig seed data):
  EMAIL = enabled, SMS = enabled, WHATSAPP = enabled, PUSH = enabled,
  INTERNAL = enabled. All 5 channels fully implemented — no deferral
  for any channel (final decision 2026-07-11).
──────────────────────────────────────────────────────────────────

SCOPE EXCEPTIONS
──────────────────────────────────────────────────────────────────
Excluded (per ARCH-REF Section 5 — "WHAT DOES NOT APPLY"):
  Eureka service discovery        : N/A — platform is a Modular Monolith
  mailservice as separate service : Email is a channel inside this module
  heac-fcm library                 : replaced by Firebase Admin SDK directly
  Multi-schema (heac_util)         : N/A — single PostgreSQL schema platform-wide
  Spring Boot Admin Client         : DevOps decision — out of P0 scope

Deferred (technical decision, non-architectural — activation trigger noted):
  SMS provider selection      : deferred pending AQ-010. Channel ships
    enabled with a pluggable adapter (NOTIF_CHANNEL_CONFIG.config_json);
    provider name does not change table structure.
  WhatsApp provider selection : deferred pending AQ-011. Same adapter
    pattern as SMS.
  File Service integration    : deferred — inline template storage now,
    RXE-NOTIF-[SEQ]-triggered migration later (POLICY-CLI-07).

Note: Push (Firebase) is NOT deferred — fully implemented Phase 1
(final decision 2026-07-11, "no deferral for any channel"). Provider is
already fixed (Firebase Admin SDK, ADAPT-NOTIF-06) — only the
service-account JSON is an infrastructure/secrets item, not an open AQ.

Confirmed out of scope for this module (ownership boundary):
  JWT validation                — owned by Security (1.2)
  Deciding WHICH channel an event needs — owned by the publishing
    business module (POLICY-CLI-02), never by Notification itself
  Template file bytes (post-migration) — owned by File Service (1.10);
    this module owns only NOTIF_TEMPLATE metadata + inline body/pointer
  Audit trail of business entities — owned by Audit Service (1.9);
    this module owns only its own delivery log
──────────────────────────────────────────────────────────────────

PLATFORM INTEGRATION NOTES
──────────────────────────────────────────────────────────────────
Database target:
  DB_TARGET = POSTGRESQL_16 — same as Organization. ARCH-REF's Oracle
  19c + Oracle UCP guidance does not transfer; standard Spring Boot
  HikariCP pooling applies. P2 applies CORE-8 type mapping to NOTIF_LOG /
  NOTIF_TEMPLATE / NOTIF_CHANNEL_CONFIG.

Messaging integration:
  RabbitMQ exchange erp.notification.exchange / queue
  erp.notification.queue / routing key notification.send.

SEC_PAGES seeding:
  Notification screens seed into SEC_PAGES with MODULE = 'NOTIFICATION',
  same pattern as Organization. PERM_* auto-generated per page.

Channel ownership boundary (POLICY-CLI-02):
  Every producing module (Procurement/Sales/Inventory/Finance/...) is
  responsible for deciding channelHint on every event it publishes.
  Notification Service performs no per-module or per-event-type channel
  inference — this is a hard module-boundary rule, not a convenience
  default that can be silently overridden by adding lookup tables inside
  Notification later.

File Service migration protocol (POLICY-CLI-07 / AD-NOTIF-11):
  Governed via the platform's standard RXE mechanism (CONTRACT-8), fired
  when FileService's DBS-ID gates in MODE 1.5. Migration itself (reading
  inline bodies, creating FILE_DOCUMENT rows, updating file_id) is P3
  execution work for Notification Service, performed upon RXE-NOTIF
  receipt — not P0/P1 scope. Documented here so P1/P3 do not treat it as
  an unplanned gap.

Firebase Push:
  app.firebase-configuration-file service-account JSON — infrastructure/
  secrets concern, out of P0 architectural scope.
──────────────────────────────────────────────────────────────────

══════════════════════════════════════════════════════════════════
*End of business-policies-notif.md (v2 — 2026-07-11)*
