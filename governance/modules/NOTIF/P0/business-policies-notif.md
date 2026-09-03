## BUSINESS POLICIES — NOTIFICATION SERVICE
══════════════════════════════════════════════════════════════════
Module      : Notification Service (NOTIF)
P0 Date     : 2026-09-01
Domain KB   : none supplied — derived from domain-profile-ERP.md + ARCH-REF-1.8
P1 reads    : CLIENT-SPECIFIC entries → RULE-IDs marked "Source: Client"
              Standard rules → applied by P1 directly
══════════════════════════════════════════════════════════════════

CLIENT-SPECIFIC POLICIES
──────────────────────────────────────────────────────────────────
The cross-cutting design policies (POLICY-CLI-01..03 in business-policies-
CU.md) also apply.

POLICY-CLI-01: Five channels, all active from the start
  Rule   : EMAIL, SMS, WHATSAPP, PUSH, INTERNAL are all built and enabled
           (nothing deferred). Runtime enable/disable is data, per channel.
  Trigger: Dispatch.
  Source : User decision 2026-09-01 (all in scope) + ARCH-REF RESOLUTION-01.

POLICY-CLI-02: Sending module owns channel choice
  Rule   : The module raising the event decides the channel(s) via
           channelHint; Notification never embeds business rules such as
           "overdue invoices need SMS". One log row per requested channel.
  Trigger: Event publish.
  Source : ARCH-REF AD-NOTIF-10.

POLICY-CLI-03: Retry then fail
  Rule   : On send failure, retry up to 5 times (2s initial, 1.5x backoff);
           after that the channel row is marked FAILED. A disabled channel
           is logged as CHANNEL_DISABLED (not retried).
  Trigger: Dispatch failure / disabled channel.
  Source : ARCH-REF AD-NOTIF-01 (reference default — tune at P1).

──────────────────────────────────────────────────────────────────
CUSTOM LOV VALUES
──────────────────────────────────────────────────────────────────
NotificationChannel : EMAIL, SMS, WHATSAPP, PUSH, INTERNAL
NotificationStatus  : PENDING, SENT, FAILED, CHANNEL_DISABLED
(owned locally by Notification — see module-registry-NOTIF.md)

──────────────────────────────────────────────────────────────────
SCOPE EXCEPTIONS
──────────────────────────────────────────────────────────────────
Excluded : Apache Camel — email uses Spring JavaMailSender directly
           (medium complexity, no heavy integration framework).
Excluded : RabbitMQ / external message broker — dispatch is triggered by
           in-process Spring events via Common Utils.
Deferred : SMS / WhatsApp / Push concrete provider selection (Twilio /
           Unifonic / Meta Cloud API / BSP, Firebase project) — a P3
           TECHNICAL config decision, NON-BLOCKING for P0/P1. Channel table
           shape is provider-independent; creds live in
           NotificationChannelConfig.config_json. Not deferred WORK on the
           module — only the vendor pick.
══════════════════════════════════════════════════════════════════
