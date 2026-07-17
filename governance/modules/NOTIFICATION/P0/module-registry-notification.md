## MODULE REGISTRY — NOTIFICATION SERVICE
══════════════════════════════════════════════════════════════════
Module Name    : Notification Service
Module Code    : NOTIF
Layer          : L1 (Foundation)
Type           : Engine
Execution Tier : L1-4 (per master-registry Section 3 — requires L1-3 complete)
P0 Date        : 2026-07-11 (v3 — finalizes channel decisions, supersedes v1/v2)
Readiness      : PARTIALLY_READY ⚠️ — see master-registry.md §15
ERP Pattern    : platform-standards.md Section M.12 (adapted — see AUTO-DECISIONS)
Source         : NEW — no prior module-registry uploaded
Session Input  : ARCH-REF-1.8-NOTIFICATION-SERVICE.md v1.1.0 (2026-06-22 / updated 2026-07-11)
DB_TARGET      : POSTGRESQL_16 (per master-registry v2.8.0 / business-policies-org.md)
══════════════════════════════════════════════════════════════════

ENTITIES OWNED
──────────────────────────────────────────────────────────────────
NotificationLog          │ Transactional │ SHARED (consumer: AuditService, 1.9 — SOFT-READ)
NotificationTemplate     │ Master Data   │ PRIVATE (Phase 1: body stored inline — see AUTO-DECISIONS)
NotificationChannelConfig│ Master Data   │ PRIVATE
──────────────────────────────────────────────────────────────────
Note: Names only — ENTITY-IDs assigned by P1, not here.
Note: NotificationChannelConfig now registered in master-registry.md
Section 3 (Layer-1 Foundation Entities), added 2026-07-11.

LOVs OWNED
──────────────────────────────────────────────────────────────────
NotificationChannel │ EMAIL/SMS/WHATSAPP/PUSH/INTERNAL channel type │ Dropdown │ EMAIL, SMS, WHATSAPP, PUSH, INTERNAL
NotificationStatus  │ delivery status of a notification             │ Dropdown │ PENDING, SENT, FAILED, CHANNEL_DISABLED
──────────────────────────────────────────────────────────────────
Note: Both confirmed in master-registry Section 6 (v2.8.0).
NotificationChannel updated from 4 to 5 values this session
(Conflict #21, master-registry.md).

LOVs CONSUMED (from other modules)
──────────────────────────────────────────────────────────────────
None identified. Recipient language preference is read as a user
attribute from Security (USERS), not a lookup value.
──────────────────────────────────────────────────────────────────

SHARED ENTITIES CONSUMED
──────────────────────────────────────────────────────────────────
FILE_DOCUMENT (FileService, 1.10) │ HARD-FK (DEFERRED) │ template body storage — Phase 1 workaround: inline storage, see AUTO-DECISIONS
USERS (Security, 1.2)             │ HARD-FK            │ recipient_id → USERS_PK (actual column name per PERMANENT EXCEPTION, master-registry Section 4)
──────────────────────────────────────────────────────────────────

DEPENDENCIES
──────────────────────────────────────────────────────────────────
Organization │ SOFT  │ No entity/FK consumed directly — platform build-order
             │       │ convention only (H.2), no ORG_* FK on any Notification entity.
Security     │ HARD  │ recipient_id → USERS (actual columns per EXCEPTION rules).
             │       │ SEC_PAGES seeding for module registration (MODULE = 'NOTIFICATION').
             │       │ No JWT validation inside module — trusts Security filter (ADAPT-NOTIF-03).
FileService  │ HARD  │ FILE_DOCUMENT — template body, Phase-1 DEFERRED via inline storage
             │ (DEFERRED)│ (AD-NOTIF-05 revised + AD-NOTIF-11). Not blocking P1.
──────────────────────────────────────────────────────────────────
ROOT: NO — see above (Security HARD, FileService HARD-DEFERRED)

OUTBOUND XM CANDIDATES (for future P1/P1.5 formalization)
──────────────────────────────────────────────────────────────────
XM-NOTIF-[TBD] → FileService FILE_DOCUMENT │ HARD-FK │ DEFERRED
  Unblock mechanism: RXE-NOTIF-[SEQ] per SHARED-ARTIFACT-CONTRACTS.md
  CONTRACT-8, fired when FileService DBS-ID is gated (MODE 1.5 passed).
  Migration steps documented in ARCH-REF-1.8 AD-NOTIF-11. Not a manual
  ad-hoc process — follows the platform's standard RXE protocol.
XM-NOTIF-[TBD] → Security USERS              │ HARD-FK │ READY (Security is EXCEPTION — usable AS-IS)
──────────────────────────────────────────────────────────────────

INBOUND XM CANDIDATES
──────────────────────────────────────────────────────────────────
XM-INBOUND-STUB-NOTIF-1 │ All 3.x modules (Procurement/Inventory/Sales/Finance) │
  Producers of NotificationEvents via RabbitMQ (erp.notification.exchange) —
  Event-Based pattern per platform-standards.md H.2. Each producing module
  determines channelHint itself (AD-NOTIF-10) — Notification does not
  embed business logic deciding which channel a given event needs.
XM-INBOUND-STUB-NOTIF-2 │ AuditService (1.9) │
  SOFT-READ consumer of NOTIF_LOG (AD-NOTIF-04). Status: NOT-YET-ASSIGNED —
  AuditService itself is NOT STARTED.
──────────────────────────────────────────────────────────────────

AUTO-DECISIONS
──────────────────────────────────────────────────────────────────
AUTO: Event-Based integration pattern for all inbound module events (RabbitMQ
      erp.notification.exchange, retry max 5 / 2s initial / 1.5x multiplier).
FROM: platform-standards.md H.2 + ARCH-REF AD-NOTIF-01.

AUTO: Five channels — Email, SMS, WhatsApp, Push (Firebase FCM), Internal.
      All five fully implemented and enabled in Phase 1 — no deferral
      for any channel (final decision 2026-07-11, supersedes the
      earlier Push-deferred assumption).
FROM: ARCH-REF AD-NOTIF-02 (final revision 2026-07-11) — resolves
      Conflict #21 (channel list mismatch) and AQ-012 (Push phase).
IF WRONG: Toggle NotificationChannelConfig.is_enabled_fl per channel —
      no schema change needed either direction.

AUTO: Channel selection is owned by the publishing business module, not by
      Notification Service. channelHint on NotificationEvent is a single
      value, a list, or "ALL". Notification fans out one NOTIF_LOG row
      per requested channel, independently checking enabled-status and
      logging CHANNEL_DISABLED per channel without failing the whole event.
FROM: ARCH-REF AD-NOTIF-10 (new, 2026-07-11) — enforces platform-standards
      §A.3 module ownership boundary (no cross-cutting business logic
      inside a neutral Foundation/Engine module).
IF WRONG: Would require Notification to encode module_code→channel
      mapping internally — re-introduces the coupling AD-NOTIF-10 exists
      to prevent.

AUTO: Templates stored inline (NOTIF_TEMPLATE.template_body_ar/en) in
      Phase 1 instead of via File Service. file_id column present but
      NULLABLE and unused until FileService is governed.
FROM: ARCH-REF AD-NOTIF-05 (revised) — resolves Conflict #22
      (NotificationService HARD-FK on ungoverned FileService).
IF WRONG: N/A — this is the explicit Phase-1 decision; reversal means
      re-blocking on FileService, which was the state being fixed.

AUTO: Migration from inline template storage to File Service is governed
      by the platform's standard XM Resolution Event (RXE) mechanism —
      not a module-specific ad-hoc migration script.
FROM: ARCH-REF AD-NOTIF-11 (new, 2026-07-11) + SHARED-ARTIFACT-CONTRACTS.md
      CONTRACT-8 + SHARED-GOVERNANCE-RULES.md RULE-12.
IF WRONG: Would need a bespoke migration protocol outside governance —
      rejected because CONTRACT-8 already covers this exact scenario
      (a module consuming a not-yet-governed SHARED entity).

AUTO: Post-migration, template_body_ar/en are retained as a resilience
      fallback (not deleted) — Notification tries file_id first, falls
      back to inline body if File Service is transiently unavailable.
FROM: ARCH-REF AD-NOTIF-11, consistent with AD-NOTIF-01 retry philosophy.
IF WRONG: Delete template_body_* post-migration — simpler storage, but
      couples every send to File Service's availability with no fallback.

AUTO: Email via Apache Camel route (DB-polling) inside the module process;
      Push via Firebase Admin SDK direct dependency (not heac-fcm).
FROM: ARCH-REF AD-NOTIF-06, ADAPT-NOTIF-01, ADAPT-NOTIF-06.

AUTO: Oracle UCP guidance (ARCH-REF AD-NOTIF-07) does not apply — DB_TARGET
      is POSTGRESQL_16. Standard Spring Boot HikariCP pool applies; actual
      DDL/pool decisions belong to P2 per CORE-8.
FROM: SHARED-GOVERNANCE-CORE.md CORE-8 + master-registry Section 1.

AUTO: NOTIF_LOG is append-only (status/retry_count transitions only),
      given its dual role as operational record and SOFT-READ source for
      Audit Service.
FROM: ARCH-REF AD-NOTIF-04.

AUTO: No tenant_id / org_unit_id NOT NULL requirement on any Notification
      entity — platform-wide single-tenant service, same posture as
      AuditService/FileService.
FROM: Conflict #17 CLOSED (business-policies-org.md) — TENANT_ID removed
      platform-wide.

AUTO: recipient_id references Security's actual USERS_PK column name, not
      standard "userId" convention.
FROM: master-registry Section 4, PERMANENT EXCEPTION — Security Module.

AUTO: No Workflow Engine / approval flow for delivery or retry handling —
      retry is a Status Lifecycle concern (PENDING→SENT/FAILED/
      CHANNEL_DISABLED), not an approval flow.
FROM: SHARED-GOVERNANCE-RULES.md RULE-13.
──────────────────────────────────────────────────────────────────

INF-IDs
──────────────────────────────────────────────────────────────────
INF-NOTIF-01 │ Event ingress: Spring Events (same-process) + RabbitMQ
             (async cross-module). │ Risk if wrong: modules become
             tightly coupled to Notification's internal API.
INF-NOTIF-03 │ Email timerPeriod is configurable
             (notification.mail.timer-period-ms), not hardcoded. │
             Resolved in P1 as a RULE with Test-Hint.
──────────────────────────────────────────────────────────────────
Note: INF-NOTIF-02 (channel count/set) is now fully resolved — see
Conflict #21 and AUTO-DECISIONS above. Removed from open INF list.

AQ-IDs (this session — all non-blocking, tracked in master-registry §14)
──────────────────────────────────────────────────────────────────
AQ-010 │ SMS provider selection (Twilio/Unifonic/local) — needed before
       P3 writes SmsChannelService's actual API integration.
AQ-011 │ WhatsApp Business API provider selection (Meta Cloud API direct
       vs. BSP) — needed before P3 writes WhatsAppChannelService.
──────────────────────────────────────────────────────────────────
Note: AQ-012 (Push phase confirmation) is RESOLVED — no deferral for
any channel (2026-07-11). Push is fully implemented Phase 1, identical
treatment to Email/SMS/WhatsApp/Internal.
──────────────────────────────────────────────────────────────────
BLK-NOTIF-001: CLOSED 2026-07-11 (see Conflict #22, master-registry.md).
Downgraded to a tracked DEFERRED XM dependency (XM-NOTIF-[TBD]→
FILE_DOCUMENT) with an explicit RXE-based unblock mechanism — no longer
blocks P1.
──────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════
*End of module-registry-notif.md (v2 — 2026-07-11)*
