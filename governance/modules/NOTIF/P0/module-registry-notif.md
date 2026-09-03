## MODULE REGISTRY — NOTIFICATION SERVICE
══════════════════════════════════════════════════════════════════
Module Name    : Notification Service
Module Code    : NOTIF
Layer          : L1
Type           : Service (Foundation — multi-channel notification dispatch)
Execution Tier : T1 (built last of the Foundation set — needs CU + SEC + FILE)
P0 Date        : 2026-09-01
Readiness      : READY
Domain KB      : none supplied — derived from domain-profile-ERP.md + ARCH-REF-1.8 (idea source)
Source         : NEW  (fresh — ARCH-REF-1.8-NOTIFICATION-SERVICE.md used as IDEA reference only)
══════════════════════════════════════════════════════════════════

SCOPE NOTE
──────────────────────────────────────────────────────────────────
Reusable multi-channel notification foundation. Five channels, all built
and enabled in full: EMAIL, SMS, WHATSAPP, PUSH, INTERNAL. Unified table
design (one log, one template, one channel-config table) with a channel_type
discriminator — no per-channel tables. The SENDING module chooses channels
via a channelHint on the event; Notification stays business-logic-neutral
and fans out one log row per requested channel. Adapted from the HEAC
reference for Modular Monolith + medium complexity: RabbitMQ → in-process
Spring events (via Common Utils); Apache Camel email routing → plain Spring
JavaMailSender (no heavy integration framework); provider creds live in
channel config JSON, not in code.

ENTITIES OWNED
──────────────────────────────────────────────────────────────────
NotificationLog           │ Transactional │ PRIVATE  (one row per channel per event)
NotificationTemplate      │ Config/Master │ PRIVATE  (inline body_ar/en; optional file_id attachment)
NotificationChannelConfig │ Config        │ PRIVATE  (per-channel enable flag + provider config_json)
──────────────────────────────────────────────────────────────────
Note: names only — ENTITY-IDs assigned by P1. Fields (recipient_id,
notification_type, template_code, status, retry_count, module_code,
reference_id/type, etc.) detailed at P1, not here.

LOVs OWNED
──────────────────────────────────────────────────────────────────
NotificationChannel │ EMAIL / SMS / WHATSAPP / PUSH / INTERNAL          │ channel discriminator
NotificationStatus  │ PENDING / SENT / FAILED / CHANNEL_DISABLED        │ log status lifecycle
──────────────────────────────────────────────────────────────────
Note: LOV-IDs assigned by P1. Owned locally (no MasterData module in
this domain).

LOVs CONSUMED (from other modules)
──────────────────────────────────────────────────────────────────
(none)
──────────────────────────────────────────────────────────────────

SHARED ENTITIES CONSUMED
──────────────────────────────────────────────────────────────────
UserAccount │ Owner: SEC │ SOFT-READ  (recipient identity)
──────────────────────────────────────────────────────────────────
Note: attachments reference File Service via its API (file_id, nullable) —
a service call, not a governed shared-table read.

DEPENDENCIES
──────────────────────────────────────────────────────────────────
Common Utils │ USES (library) │ Events (NotificationEvent), config, exceptions
Security     │ SOFT           │ recipient identity (UserAccount)
File Service │ SOFT / service │ optional attachments via File Service API (file_id)
──────────────────────────────────────────────────────────────────
ROOT: NO — depends on CU (lib) + SEC (SOFT) + FILE (SOFT/service).
Top of the Foundation build order.

AUTO-DECISIONS
──────────────────────────────────────────────────────────────────
AUTO: Five channels (EMAIL/SMS/WHATSAPP/PUSH/INTERNAL) all built and
      enabled (is_enabled_fl=1) from the start.
FROM: ARCH-REF RESOLUTION-01 + domain rule "no partial/deferred work".
IF WRONG: an operator can disable a channel at runtime via config (data, not code).

AUTO: Unified tables (Log/Template/ChannelConfig) with channel_type
      discriminator — no per-channel tables.
FROM: ARCH-REF RESOLUTION-01.
IF WRONG: n/a — splitting per channel is the rejected design.

AUTO: In-process Spring events (via Common Utils Events) — NOT RabbitMQ.
FROM: Modular Monolith (ADAPT-NOTIF-02) + domain "no heavy framework".
IF WRONG: durable async broker opened as a new decision only if needed.

AUTO: Email via Spring JavaMailSender directly — NOT Apache Camel.
FROM: domain "medium complexity, no heavy integration framework"
      (fresh adaptation — reference used Camel from heac-mailservice).
IF WRONG: revisit only if complex routing/enterprise integration is required.

AUTO: Sending module chooses channels via channelHint (single | list | ALL);
      fan-out = one NotificationLog row per channel.
FROM: ARCH-REF AD-NOTIF-10 (business logic stays out of Notification).
IF WRONG: n/a — core neutrality principle.

AUTO: Templates stored inline (body_ar/en); file_id references File Service
      for optional ATTACHMENTS only (not template text).
FROM: ARCH-REF AD-NOTIF-05 + File Service now in-scope.
IF WRONG: move template text to File Service if runtime file-managed templates wanted (P1).

AUTO: Push via Firebase Admin SDK; SMS/WhatsApp via adapter pattern with
      provider creds in NotificationChannelConfig.config_json.
FROM: ARCH-REF ADAPT-NOTIF-06 + AD-NOTIF-02.
IF WRONG: n/a for shape. Concrete SMS/WhatsApp provider = P3 technical
          decision (non-blocking; table shape is provider-independent).

AUTO: Notification does NOT validate JWT — trusts Security's filter.
FROM: ARCH-REF ADAPT-NOTIF-03.
IF WRONG: n/a — single JWT authority = Security.

INF-IDs
──────────────────────────────────────────────────────────────────
(none — all decisions traced to ARCH-REF + domain adaptation via
 AUTO-DECISIONS above; no unresolved gap)
──────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════
