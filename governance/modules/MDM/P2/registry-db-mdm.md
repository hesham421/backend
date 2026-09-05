# REGISTRY EXTRACT — registry-db-MDM
══════════════════════════════════════════════════════════════════
Module          : Master Data (MDM)
Source artifact : db-script-MDM.md (DBS-MDM-001, GATE PASSED, 2026-09-04)
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4.1/P4.2 audit
══════════════════════════════════════════════════════════════════

## HEADER
Module name    : Master Data
Module Prefix  : MDM

## TABLES
| DBS-ID | Table Name | Source ENTITY-ID |
|---|---|---|
| DBS-MDM-001 | MDM_LOOKUP_TYPE | ENTITY-MDM-001 |
| DBS-MDM-001 | MDM_LOOKUP_VALUE | ENTITY-MDM-002 |

## DB FIELD TRACEABILITY
| DBF-ID | Column Name | DB Type | Table (DBS-ID) | SRS Source |
|---|---|---|---|---|
| DBF-0001 | ID | BIGINT | MDM_LOOKUP_TYPE (DBS-MDM-001) | ENTITY-MDM-001.lookupTypePk |
| DBF-0002 | TYPE_CODE | VARCHAR(50) | MDM_LOOKUP_TYPE (DBS-MDM-001) | ENTITY-MDM-001.typeCode |
| DBF-0003 | NAME_AR | VARCHAR(200) | MDM_LOOKUP_TYPE (DBS-MDM-001) | ENTITY-MDM-001.nameAr |
| DBF-0004 | NAME_EN | VARCHAR(100) | MDM_LOOKUP_TYPE (DBS-MDM-001) | ENTITY-MDM-001.nameEn |
| DBF-0005 | IS_ACTIVE_FL | SMALLINT | MDM_LOOKUP_TYPE (DBS-MDM-001) | ENTITY-MDM-001.isActiveFl |
| DBF-0006 | NOTES | VARCHAR(2000) | MDM_LOOKUP_TYPE (DBS-MDM-001) | ENTITY-MDM-001.notes |
| DBF-0007 | ID | BIGINT | MDM_LOOKUP_VALUE (DBS-MDM-001) | ENTITY-MDM-002.lookupValuePk |
| DBF-0008 | LOOKUP_TYPE_FK | BIGINT | MDM_LOOKUP_VALUE (DBS-MDM-001) | ENTITY-MDM-002.lookupTypeFk → ENTITY-MDM-001 |
| DBF-0009 | VALUE_CODE | VARCHAR(50) | MDM_LOOKUP_VALUE (DBS-MDM-001) | ENTITY-MDM-002.valueCode |
| DBF-0010 | NAME_AR | VARCHAR(200) | MDM_LOOKUP_VALUE (DBS-MDM-001) | ENTITY-MDM-002.nameAr |
| DBF-0011 | NAME_EN | VARCHAR(100) | MDM_LOOKUP_VALUE (DBS-MDM-001) | ENTITY-MDM-002.nameEn |
| DBF-0012 | SORT_ORDER | SMALLINT | MDM_LOOKUP_VALUE (DBS-MDM-001) | ENTITY-MDM-002.sortOrder |
| DBF-0013 | IS_ACTIVE_FL | SMALLINT | MDM_LOOKUP_VALUE (DBS-MDM-001) | ENTITY-MDM-002.isActiveFl |
| DBF-0014 | NOTES | VARCHAR(2000) | MDM_LOOKUP_VALUE (DBS-MDM-001) | ENTITY-MDM-002.notes |

Total: 14 DBF-IDs across 2 tables. Audit columns (CREATED_BY/AT,
UPDATED_BY/AT) on both tables: no DBF-ID (AuditEntityListener).

## LOV DDL REGISTER
None — MDM owns no LOV-ID of its own (this module IS the shared lookup
provider). Note: BLOCK 8 of the DDL seeds 4 governed lookup TYPES as
data rows (not LOV-IDs): NOTIF_CHANNEL (5 values), NOTIF_STATUS (4
values), FILE_FILE_STATUS (3 values), FILE_FILE_TYPE (5 values) — 17
values total, taken verbatim from the owning modules' SRS (srs-NOTIF,
srs-FILE) per srs-MDM v1.2/v1.3 §A2 governed seed.

## XM REGISTER
None. MDM has zero cross-module dependencies — consumed via REST API
only (provider pattern); consumers hold the value **code** as a SOFT
reference, no FK, no XM-ID in either direction.

---
*End of registry-db-MDM.md*
