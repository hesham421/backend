# PRD — Notification Service (NOTIF)
══════════════════════════════════════════════════════════════════
Module          : Notification Service (NOTIF prefix)
Source artifacts: master-registry.md (platform-summary substitute — flagged),
                  module-registry-NOTIF.md,
                  business-policies-NOTIF.md
SRS context     : No srs-NOTIF.md attached — P1 not yet completed for
                  this module. Retrofit mode; CONTRACT-10 hard gate
                  suspended per established retrofit protocol.
Status          : DRAFT — awaiting Reconciliation Gate (Project 2.5)
Retrofit note   : platform-summary.md absent; master-registry fields
                  embedded in module-registry-NOTIF.md supply platform-tier
                  and dependency classification. Substitution noted and flagged
                  per established retrofit protocol.
Open AQs from P0: AQ-010 (SMS provider), AQ-011 (WhatsApp provider) —
                  non-blocking for PRD; carried forward from
                  module-registry-NOTIF.md §AQ-IDs.
══════════════════════════════════════════════════════════════════

## USER STORIES

---

US-NOTIF-001
  Story    : A business module needs to trigger a notification to a
             recipient across one or more channels (Email, SMS, WhatsApp,
             Push, Internal) by publishing a single event — without needing
             to know how each channel is individually delivered.
  Priority : —
  Success metric : —
  Source   : business-policies-NOTIF.md POLICY-CLI-01 (NotificationEvent
             contract: recipientId, channelHint, templateCode, contextData,
             priority); POLICY-CLI-02 (channel selection owned by publisher;
             fan-out per requested channel)
  Status   : DRAFT

---

US-NOTIF-002
  Story    : A publishing module needs each channel's delivery to be
             attempted and logged independently, so that a disabled or
             failed channel does not block delivery on the other requested
             channels.
  Priority : —
  Success metric : —
  Source   : business-policies-NOTIF.md POLICY-CLI-02 (one NotificationLog
             entry per channel; independent enabled-status check per channel);
             POLICY-CLI-04 (disabled channel logged as CHANNEL_DISABLED;
             no error raised to the sending module)
  Status   : DRAFT

---

US-NOTIF-003
  Story    : The system needs to automatically retry failed delivery
             attempts using a back-off interval, so that transient channel
             failures are recovered without manual intervention, and the
             sending module is not notified of individual retry attempts.
  Priority : —
  Success metric : —
  Source   : business-policies-NOTIF.md POLICY-CLI-03 (retry up to 5×
             with exponential backoff: 2s → 3s → 4.5s → 6.75s; mark FAILED
             after exhaustion without notifying the original sender)
  Status   : DRAFT

---

US-NOTIF-004
  Story    : A notification recipient needs to receive their notification
             in their preferred language (Arabic or English), and the system
             needs to fall back gracefully to a default template when the
             exact templateCode is unavailable — rather than failing the send.
  Priority : —
  Success metric : —
  Source   : business-policies-NOTIF.md POLICY-CLI-05 (bilingual template
             requirement — both AR and EN versions mandatory; language
             resolved from recipient's user language preference in Security;
             fallback to default template on missing templateCode)
  Status   : DRAFT

---

US-NOTIF-005
  Story    : A content administrator needs to create and manage bilingual
             notification templates (Arabic and English versions) so that
             business modules can reference templates by code without
             embedding message text in their own logic.
  Priority : —
  Success metric : —
  Source   : module-registry-NOTIF.md §ENTITIES OWNED (NotificationTemplate —
             Master Data, PRIVATE); business-policies-NOTIF.md POLICY-CLI-05
             (every template must carry both language versions)
  Status   : DRAFT

---

US-NOTIF-006
  Story    : An administrator needs to enable or disable individual
             notification channels (e.g., temporarily disable SMS while
             switching providers) without requiring a code change or
             redeployment.
  Priority : —
  Success metric : —
  Source   : module-registry-NOTIF.md §ENTITIES OWNED (NotificationChannelConfig —
             Master Data, PRIVATE); business-policies-NOTIF.md POLICY-CLI-04
             (is_enabled_fl per channel evaluated at send time); §CUSTOM LOV
             VALUES (Phase 1: all 5 channels enabled via NotificationChannelConfig
             seed data — final decision 2026-07-11)
  Status   : DRAFT

---

US-NOTIF-007
  Story    : An operations team needs to trace the delivery history of any
             notification — including which channels were attempted, their
             outcome (SENT / FAILED / CHANNEL_DISABLED), and retry activity —
             for support and audit purposes.
  Priority : —
  Success metric : —
  Source   : module-registry-NOTIF.md §ENTITIES OWNED (NotificationLog —
             Transactional, SHARED; append-only; SOFT-READ by AuditService);
             §AUTO-DECISIONS ("NOTIF_LOG is append-only — status/retry_count
             transitions only, dual role as operational record and SOFT-READ
             source for Audit Service")
  Status   : DRAFT

---

US-NOTIF-008
  Story    : When File Service becomes available, the system needs to migrate
             template bodies to file-based storage while retaining inline
             content as a resilience fallback, so that a transient File
             Service unavailability does not become a single point of failure
             for notification delivery.
  Priority : —
  Success metric : —
  Source   : business-policies-NOTIF.md POLICY-CLI-07 (Phase 1: inline
             storage in template_body_ar/en; RXE-NOTIF-[SEQ]-triggered
             migration when FileService DBS-ID gates; inline body columns
             retained post-migration as fallback); module-registry-NOTIF.md
             §AUTO-DECISIONS (AD-NOTIF-05 revised + AD-NOTIF-11)
  Status   : DRAFT

---

## OPEN ITEMS (ambiguous, not yet a story)

  ? AQ-NOTIF-PRD-001 — Is there any Admin-facing screen for viewing or
    searching the NotificationLog directly (operational monitoring dashboard),
    or is log visibility exclusively the concern of AuditService (1.9, NOT
    STARTED)? module-registry-NOTIF.md confirms NotificationLog is SHARED
    and consumed via SOFT-READ by AuditService, but whether Notification
    itself exposes a monitoring UI is not stated in any P0 artifact.
    Non-blocking — US-NOTIF-007 captures the traceability need at intent
    level; the screen-ownership question is deferred to P1.

  ? AQ-010 (carried from P0) — SMS provider selection (Twilio / Unifonic /
    local) — needed before P3 writes SmsChannelService's actual API
    integration. Non-blocking for PRD and P1.

  ? AQ-011 (carried from P0) — WhatsApp Business API provider selection
    (Meta Cloud API direct vs. BSP) — needed before P3 writes
    WhatsAppChannelService. Non-blocking for PRD and P1.

══════════════════════════════════════════════════════════════════
*End of prd-NOTIF.md*
*Next stage: Project 1 (SRS Engine) — requires this file as a hard
 gate (CONTRACT-10 v2.1) before srs-NOTIF.md generation begins.*
*Project 2.5 (UI/UX Design Engine) may begin drafting from this file
 alone in parallel with P1 (CONTRACT-11 v2.1).*
══════════════════════════════════════════════════════════════════
