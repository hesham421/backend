## BUSINESS POLICIES — FILE SERVICE
══════════════════════════════════════════════════════════════════
Module      : File Service (FILE)
P0 Date     : 2026-09-01
Domain KB   : none supplied — derived from domain-profile-ERP.md + ARCH-REF-1.10
P1 reads    : CLIENT-SPECIFIC entries → RULE-IDs marked "Source: Client"
              Standard rules → applied by P1 directly
══════════════════════════════════════════════════════════════════

CLIENT-SPECIFIC POLICIES
──────────────────────────────────────────────────────────────────
Concrete defaults carried from the ARCH-REF (reviewable per consumer);
the cross-cutting design policies (POLICY-CLI-01..03 in business-policies-
CU.md) also apply.

POLICY-CLI-01: File size limits
  Rule   : Content ≤ 5MB (application enforced); HTTP request ≤ 10MB
           (multipart). Overridable per FileCategory.
  Trigger: Upload.
  Source : ARCH-REF AD-FILE-05 (reference default).

POLICY-CLI-02: Accepted file types
  Rule   : JPG/JPEG/PNG, PDF, DOC/DOCX, TXT, XLS/XLSX, ZIP/RAR/7Z;
           others → application/octet-stream. MIME auto-detected (not
           trusted from client header).
  Trigger: Upload.
  Source : ARCH-REF AD-FILE-06 (reference default).

POLICY-CLI-03: Time-limited access links
  Rule   : Every upload/download requires a freshly generated encrypted
           token; a link is invalid after its TTL (~100 min) — not reusable.
  Trigger: Upload / Download.
  Source : ARCH-REF AD-FILE-02 (reference default).

──────────────────────────────────────────────────────────────────
CUSTOM LOV VALUES
──────────────────────────────────────────────────────────────────
FileType   : IMAGE, DOCUMENT, SPREADSHEET, ARCHIVE, OTHER
FileStatus : ACTIVE, ARCHIVED, DELETED
(owned locally by File Service — see module-registry-FILE.md)

──────────────────────────────────────────────────────────────────
SCOPE EXCEPTIONS
──────────────────────────────────────────────────────────────────
Excluded : PDF processing/preview (PDFBox) — ARCH-REF RESOLUTION-03.
Excluded : Async file pipeline / message broker (RabbitMQ) — RESOLUTION-04;
           all file ops are synchronous via @Service injection.
Excluded : External filesystem storage — bytes live in the DB (BYTEA).
Deferred : Delete semantics (permanent vs soft via status=DELETED) — a P1
           decision; the status LOV supports either. Not deferred WORK,
           just a design choice for P1 to fix.
══════════════════════════════════════════════════════════════════
