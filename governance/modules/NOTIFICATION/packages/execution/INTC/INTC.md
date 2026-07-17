<!-- Source: PHASE:INTC -->

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
