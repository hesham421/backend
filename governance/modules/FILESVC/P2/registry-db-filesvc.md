# REGISTRY EXTRACT — registry-db-FILE
══════════════════════════════════════════════════════════════════
Module          : File Service (FILE prefix)
Source artifact : db-script.md
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4 audit
══════════════════════════════════════════════════════════════════

## HEADER
| Field | Value |
|---|---|
| Module Name | File Service |
| Module Prefix | FILE |
| DBS-ID | DBS-FILE-001 |
| DBF-ID Count | 27 |

## TABLES
| DBS-ID | Table Name | Source ENTITY-ID |
|---|---|---|
| DBS-FILE-001 | FILE_CATEGORY | ENTITY-FILE-002 |
| DBS-FILE-001 | FILE_DOCUMENT | ENTITY-FILE-001 |

## DB FIELD TRACEABILITY
| DBF-ID | Column Name | DB Type | Table | SRS Source |
|---|---|---|---|---|
| DBF-0001 | FILE_CATEGORY_PK | BIGINT | FILE_CATEGORY | ENTITY-FILE-002.fileCategoryPk |
| DBF-0002 | CATEGORY_CODE | VARCHAR(50) | FILE_CATEGORY | ENTITY-FILE-002.categoryCode |
| DBF-0003 | MODULE_CODE | VARCHAR(20) | FILE_CATEGORY | ENTITY-FILE-002.moduleCode |
| DBF-0004 | NAME_AR | VARCHAR(200) | FILE_CATEGORY | ENTITY-FILE-002.nameAr |
| DBF-0005 | NAME_EN | VARCHAR(100) | FILE_CATEGORY | ENTITY-FILE-002.nameEn |
| DBF-0006 | MAX_SIZE_BYTES_OVERRIDE | BIGINT | FILE_CATEGORY | ENTITY-FILE-002.maxSizeBytesOverride |
| DBF-0007 | ALLOWED_TYPES_NOTE | VARCHAR(500) | FILE_CATEGORY | ENTITY-FILE-002.allowedTypesNote |
| DBF-0008 | IS_ACTIVE_FL | SMALLINT | FILE_CATEGORY | ENTITY-FILE-002.isActiveFl |
| DBF-0009 | CREATED_BY | VARCHAR(255) | FILE_CATEGORY | ENTITY-FILE-002.createdBy |
| DBF-0010 | CREATED_AT | TIMESTAMP | FILE_CATEGORY | ENTITY-FILE-002.createdAt |
| DBF-0011 | UPDATED_BY | VARCHAR(255) | FILE_CATEGORY | ENTITY-FILE-002.updatedBy |
| DBF-0012 | UPDATED_AT | TIMESTAMP | FILE_CATEGORY | ENTITY-FILE-002.updatedAt |
| DBF-0013 | FILE_DOCUMENT_PK | BIGINT | FILE_DOCUMENT | ENTITY-FILE-001.fileDocumentPk |
| DBF-0014 | OWNER_ID | BIGINT | FILE_DOCUMENT | ENTITY-FILE-001.ownerId (polymorphic — no FK) |
| DBF-0015 | OWNER_TYPE | VARCHAR(100) | FILE_DOCUMENT | ENTITY-FILE-001.ownerType |
| DBF-0016 | MODULE_CODE | VARCHAR(20) | FILE_DOCUMENT | ENTITY-FILE-001.moduleCode |
| DBF-0017 | FILE_CATEGORY_FK | BIGINT | FILE_DOCUMENT | ENTITY-FILE-001.fileCategoryFk → ENTITY-FILE-002 |
| DBF-0018 | FILE_TYPE_ID | VARCHAR(50) | FILE_DOCUMENT | ENTITY-FILE-001.fileTypeId (LOV-FILE-001) |
| DBF-0019 | FILE_NAME_ORIGINAL | VARCHAR(255) | FILE_DOCUMENT | ENTITY-FILE-001.fileNameOriginal |
| DBF-0020 | MIME_TYPE | VARCHAR(100) | FILE_DOCUMENT | ENTITY-FILE-001.mimeType |
| DBF-0021 | FILE_SIZE_BYTES | BIGINT | FILE_DOCUMENT | ENTITY-FILE-001.fileSizeBytes |
| DBF-0022 | FILE_CONTENT | BYTEA | FILE_DOCUMENT | ENTITY-FILE-001.fileContent (RESOLUTION-01 — extends CORE-8) |
| DBF-0023 | FILE_STATUS_ID | VARCHAR(50) | FILE_DOCUMENT | ENTITY-FILE-001.fileStatusId (LOV-FILE-002) |
| DBF-0024 | CREATED_BY | VARCHAR(255) | FILE_DOCUMENT | ENTITY-FILE-001.createdBy |
| DBF-0025 | CREATED_AT | TIMESTAMP | FILE_DOCUMENT | ENTITY-FILE-001.createdAt |
| DBF-0026 | UPDATED_BY | VARCHAR(255) | FILE_DOCUMENT | ENTITY-FILE-001.updatedBy |
| DBF-0027 | UPDATED_AT | TIMESTAMP | FILE_DOCUMENT | ENTITY-FILE-001.updatedAt |

## LOV DDL REGISTER
| LOV-ID | Table/Type name | Code values |
|---|---|---|
| LOV-FILE-001 | FILE_TYPE (MD_LOOKUP_DETAIL) | IMAGE, DOCUMENT, SPREADSHEET, ARCHIVE, OTHER |
| LOV-FILE-002 | FILE_STATUS (MD_LOOKUP_DETAIL) | ACTIVE, ARCHIVED, DELETED |

## XM REGISTER
| XM-FILE-ID | Type | Target Table | Target Module | Initial Status |
|---|---|---|---|---|
| — | — | — | — | None — 0 outbound XM-IDs (File Service consumes no other module's SHARED entities) |
