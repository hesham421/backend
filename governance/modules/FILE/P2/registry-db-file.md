# REGISTRY EXTRACT — registry-db-FILE
══════════════════════════════════════════════════════════════════
Module          : File Service (FILE)
Source artifact : db-script-FILE.md
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4.1/P4.2 audit
══════════════════════════════════════════════════════════════════

## HEADER
Module name : File Service
Module Prefix : FILE

## TABLES (DBS-ID register)
| DBS-ID | Table Name | Source ENTITY-ID |
|---|---|---|
| DBS-FILE-001 | FILE_DOCUMENT | ENTITY-FILE-001 |
| DBS-FILE-001 | FILE_CATEGORY | ENTITY-FILE-002 |

## DB FIELD TRACEABILITY (compact)
| DBF-ID | Column Name | DB Type | Table | SRS Source |
|---|---|---|---|---|
| DBF-0001 | ID | BIGINT | FILE_DOCUMENT | ENTITY-FILE-001 |
| DBF-0002 | OWNER_ID | BIGINT | FILE_DOCUMENT | ENTITY-FILE-001 (polymorphic) |
| DBF-0003 | OWNER_TYPE | VARCHAR(100) | FILE_DOCUMENT | ENTITY-FILE-001 |
| DBF-0004 | MODULE_CODE | VARCHAR(50) | FILE_DOCUMENT | ENTITY-FILE-001 |
| DBF-0005 | FILE_NAME | VARCHAR(255) | FILE_DOCUMENT | ENTITY-FILE-001 |
| DBF-0006 | CONTENT_TYPE | VARCHAR(150) | FILE_DOCUMENT | ENTITY-FILE-001 |
| DBF-0007 | FILE_SIZE | BIGINT | FILE_DOCUMENT | ENTITY-FILE-001 |
| DBF-0008 | FILE_CONTENT | BYTEA | FILE_DOCUMENT | ENTITY-FILE-001 |
| DBF-0009 | FILE_TYPE_ID | VARCHAR(50) | FILE_DOCUMENT | ENTITY-FILE-001 |
| DBF-0010 | FILE_STATUS_ID | VARCHAR(50) | FILE_DOCUMENT | ENTITY-FILE-001 |
| DBF-0011 | FILE_CATEGORY_FK | BIGINT | FILE_DOCUMENT | ENTITY-FILE-001 → ENTITY-FILE-002 |
| DBF-0012 | ID | BIGINT | FILE_CATEGORY | ENTITY-FILE-002 |
| DBF-0013 | CATEGORY_CODE | VARCHAR(50) | FILE_CATEGORY | ENTITY-FILE-002 |
| DBF-0014 | NAME_AR | VARCHAR(200) | FILE_CATEGORY | ENTITY-FILE-002 |
| DBF-0015 | NAME_EN | VARCHAR(100) | FILE_CATEGORY | ENTITY-FILE-002 |
| DBF-0016 | MAX_SIZE_BYTES | BIGINT | FILE_CATEGORY | ENTITY-FILE-002 |
| DBF-0017 | ALLOWED_CONTENT_TYPES | TEXT | FILE_CATEGORY | ENTITY-FILE-002 |
| DBF-0018 | IS_ACTIVE_FL | SMALLINT | FILE_CATEGORY | ENTITY-FILE-002 |
Total: 18 DBF-IDs across 2 tables.

## LOV DDL REGISTER
| LOV-ID | Table/Type name | Code values |
|---|---|---|
| LOV-FILE-001 | FILE_FILE_TYPE (fileTypeId, runtime-loaded, no DDL table) | IMAGE, DOCUMENT, SPREADSHEET, ARCHIVE, OTHER |
| LOV-FILE-002 | FILE_FILE_STATUS (fileStatusId, runtime-loaded, no DDL table) | ACTIVE, ARCHIVED, DELETED |

## XM REGISTER
| XM-ID | Type | Target Table | Target Module | Initial Status |
|---|---|---|---|---|
| XM-FILE-001 | SOFT-READ | SEC_USER_ACCOUNT | Security | ACTIVE |

---
*End of registry-db-FILE.md*
