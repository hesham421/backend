## MODULE REGISTRY — FILE SERVICE
══════════════════════════════════════════════════════════════════
Module Name    : File Service
Module Code    : FILE
Layer          : L1 (Foundation)
Type           : Engine
Execution Tier : L1-3 (per master-registry Section 3 — parallel with 1.4/1.5/1.7)
P0 Date        : 2026-07-11
Readiness      : READY ✓
ERP Pattern    : platform-standards.md Section M.14 (adapted — see AUTO-DECISIONS)
Source         : NEW — no prior module-registry uploaded
Session Input  : ARCH-REF-1.10-FILE-SERVICE.md v1.1.0 (2026-06-22 / updated 2026-07-11)
DB_TARGET      : POSTGRESQL_16 (per master-registry v2.8.1)
══════════════════════════════════════════════════════════════════

ENTITIES OWNED
──────────────────────────────────────────────────────────────────
FileDocument │ Internal │ SHARED (consumers: Notification 1.8 [DEFERRED],
             │          │ AuditService 1.9, all 3.x modules — HARD-FK)
FileCategory │ Master Data (Reference Table) │ SHARED (consumers: every
             │          │ module needing document categorization)
──────────────────────────────────────────────────────────────────
Note: Names only — ENTITY-IDs assigned by P1, not here.
Note: FileDocument maps to FILE_DOCUMENT (DB), FileCategory to
FILE_CATEGORY (DB) — see ARCH-REF-1.10 P2 section.
Note: FileDocument carries a Status Lifecycle (ACTIVE/ARCHIVED/DELETED
via FileStatus, >2 states) per platform-standards.md §C.1 — not a
simple is_active_fl flag. Deletion itself is still permanent at the
business-rule level (POLICY-CLI-04, business-policies-file.md) —
DELETED status exists for records whose binary content has been
purged but whose metadata/audit trail is retained.

LOVs OWNED
──────────────────────────────────────────────────────────────────
FileType   │ broad technical file classification              │ Dropdown │ IMAGE, DOCUMENT, SPREADSHEET, ARCHIVE, OTHER
FileStatus │ file lifecycle state (>2 states — Status Lifecycle,│ Dropdown │ ACTIVE, ARCHIVED, DELETED
           │ platform-standards.md §C.1, not a simple is_active_fl)│         │
──────────────────────────────────────────────────────────────────
Both confirmed in master-registry Section 6 (already registered,
consistent with Section M.14's base LOV pattern — corrects an
omission in this module-registry's first draft, which only carried
FileCategory forward from ARCH-REF and missed M.14's file_type/
file_status LOVs). FileCategory (below) is additional to these two,
not a replacement.

FileCategory is a Reference Table (see below), not a Lookup — kept
separate from FileType: FileType is the technical MIME-derived
classification (stable, ≤15, platform-owned); FileCategory is the
business document-type per consuming module (PASSPORT/CONTRACT/
NOTIFICATION_TEMPLATE/..., extensible, module_code-scoped, >15
across the platform).

LOVs CONSUMED (from other modules)
──────────────────────────────────────────────────────────────────
None identified.
──────────────────────────────────────────────────────────────────

SHARED ENTITIES CONSUMED
──────────────────────────────────────────────────────────────────
None. File Service is a Foundation-layer provider — it is consumed by
others, not a consumer of other modules' owned entities. (Standard
audit columns — createdBy/updatedBy — are VARCHAR per master-registry
Section 4 convention, not a FK to Security's USERS.)
──────────────────────────────────────────────────────────────────

DEPENDENCIES
──────────────────────────────────────────────────────────────────
Organization │ SOFT │ No entity/FK consumed directly — platform build-order
             │      │ convention only (H.2), no ORG_* FK on any File entity.
Security     │ HARD │ No JWT validation inside module (ADAPT-03) — File Service
             │      │ has its own Encrypted Token security model (AES/GCM),
             │      │ independent of JWT. Dependency exists for: SEC_PAGES
             │      │ seeding (MODULE = 'FILE'), and delete-permission checks
             │      │ ("owner or Admin only" — RULE-FILE-DELETE, resolved via
             │      │ Security's role/permission model at P1).
──────────────────────────────────────────────────────────────────
ROOT: NO — Security HARD (trust-boundary, not data-FK)

OUTBOUND XM CANDIDATES
──────────────────────────────────────────────────────────────────
None — File Service consumes no other module's SHARED entity by FK.
──────────────────────────────────────────────────────────────────

INBOUND XM CANDIDATES (declared for downstream awareness)
──────────────────────────────────────────────────────────────────
XM-INBOUND-STUB-FILE-1 │ NotificationService (1.8) │
  HARD-FK on FILE_DOCUMENT for template body storage. Currently DEFERRED
  on the Notification side (module-registry-notif.md XM-NOTIF-[TBD]) —
  Notification stores template bodies inline until this module gates.
  Unblock mechanism: RXE-NOTIF-[SEQ] fires when this module's DBS-ID is
  confirmed (SHARED-ARTIFACT-CONTRACTS.md CONTRACT-8). This is the
  trigger event this module's own P2/P3 sessions must be aware of.
XM-INBOUND-STUB-FILE-2 │ AuditService (1.9) │
  For archival exports. AuditService itself is NOT STARTED — status
  NOT-YET-ASSIGNED.
XM-INBOUND-STUB-FILE-3 │ All 3.x modules (Procurement/Inventory/Sales/Finance) │
  Generic attachment storage. Consumers not yet built — status
  NOT-YET-ASSIGNED.
──────────────────────────────────────────────────────────────────

AUTO-DECISIONS
──────────────────────────────────────────────────────────────────
AUTO: FileType and FileStatus adopted as governed Lookups per Section
      M.14's base pattern (file_type, file_status Dropdowns) — corrects
      an initial gap where this module-registry's first draft carried
      only ARCH-REF's FILE_CATEGORY forward and missed M.14's own two
      LOVs. Reconciled against master-registry.md Section 6, which
      already had both registered.
FROM: platform-standards.md Section M.14 (base pattern) — self-correction
      during this P0 session, cross-checked against master-registry.md.

AUTO: File content stored as PostgreSQL BYTEA (not Large Objects/lo).
FROM: ARCH-REF AD-FILE-01 (revised) — user decision 2026-07-11,
      justified by the 5MB size ceiling (AD-FILE-05) making BYTEA's
      simpler lifecycle preferable to lo's GB-scale-oriented design.
IF WRONG: Migrating BYTEA→lo later requires a data migration script;
      not a simple flag flip. Confirmed low-risk given file size cap.

AUTO: Oracle UCP guidance (ARCH-REF AD-FILE-04) does not apply — standard
      HikariCP pool. DDL/pool tuning is P2 scope per CORE-8.
FROM: SHARED-GOVERNANCE-CORE.md CORE-8 + master-registry Section 1.

AUTO: PDFBox excluded entirely — not deferred, not included as an unused
      dependency.
FROM: User decision 2026-07-11 (resolves ARCH-REF's own open AQ-FILE-01).
IF WRONG: A future genuine PDF-processing need (e.g., Reporting 4.1)
      opens as a fresh, scoped AQ — not a revival of this one.

AUTO: RabbitMQ excluded from the core File Service integration path.
      All Module→FileService calls are direct @Service injection
      (synchronous), consistent with Modular Monolith architecture.
FROM: ARCH-REF's own internal inconsistency between AD-FILE-07
      (RabbitMQ) and ADAPT-02 (direct calls for monolith) — resolved in
      favor of ADAPT-02, since upload/download/delete are inherently
      synchronous (caller needs immediate file_id or binary stream).
IF WRONG: A genuine async use case (virus scanning, thumbnail
      generation) would need its own AD and likely its own queue —
      not a reason to route core CRUD through RabbitMQ.

AUTO: Encrypted Token (AES/GCM, URL-embedded) security model adopted
      as-is from ARCH-REF AD-FILE-02 — independent from JWT/Security
      module. Used for browser/mobile-facing upload/download links only;
      internal module-to-module calls use direct method calls, not
      tokens (ADAPT-02).
FROM: ARCH-REF AD-FILE-02, ADAPT-02, ADAPT-03.

AUTO: Token payload uses ownerId + ownerType + moduleCode (not
      studentId/contentType from the reference) — generalized for a
      multi-entity, multi-module platform.
FROM: ARCH-REF ADAPT-01.

AUTO: Standard PK (auto-generated BIGINT) + owner_id/owner_type columns,
      not the reference's Composite PK (studentId+contentType) — a
      single owning entity can have multiple files of the same category.
FROM: ARCH-REF ADAPT-05.

AUTO: owner_type is a free-text convention (the producing module's
      entity name, e.g. "PURCHASE_ORDER"), NOT a governed MD_LOOKUP_DETAIL
      value and NOT a File-Service-owned Reference Table. Adding a new
      entity type to the platform must never require touching File
      Service's own registry rows.
FROM: Consistency with Notification's own reference_type field
      (NOTIF_LOG) — same precedent, same reasoning: avoids coupling a
      Foundation/Engine module to every future module's entity list.

AUTO: FileCategory is a Reference Table (module_code-scoped, extensible
      by Admin), not an MD_LOOKUP_DETAIL entry.
FROM: master-registry Lookup Governance Rules ("Max values per lookup
      category <= 15 → else Reference Table") — category values summed
      across all consuming modules will exceed 15. Same governance class
      as ORG_REGION_TYPE (Organization's own Reference Table precedent).

AUTO: No tenant_id NOT NULL requirement (platform-wide single-tenant,
      Conflict #17 CLOSED). org_unit_id not required either — File
      Service scoping follows the owning entity's own scope, not a
      direct org_unit_id column on FILE_DOCUMENT.
FROM: business-policies-org.md Conflict #17 + M.0 Universal Defaults
      (Internal-type entities are not bound to the Master
      Data/Transactional tenant/org_unit requirement).

AUTO: No Workflow Engine / approval flow for file operations.
FROM: SHARED-GOVERNANCE-RULES.md RULE-13.
──────────────────────────────────────────────────────────────────

INF-IDs
──────────────────────────────────────────────────────────────────
INF-FILE-02 │ Encrypted Token payload structure carries
            ownerId/ownerType/moduleCode/action/ts — adopted as-is
            from ARCH-REF with ERP-specific field renaming (ADAPT-01).
INF-FILE-04 │ File size limits (5MB content / 10MB request) adopted as
            Day-1 defaults, explicitly noted as per-module-overridable
            via FILE_CATEGORY.max_size_bytes.
──────────────────────────────────────────────────────────────────

AQ-IDs / BLK-IDs
──────────────────────────────────────────────────────────────────
None open for this module. AQ-FILE-01 (PDFBox) and the RabbitMQ
question are both resolved (see AUTO-DECISIONS) — not carried forward
as open items.
──────────────────────────────────────────────────────────────────
══════════════════════════════════════════════════════════════════
*End of module-registry-file.md*
