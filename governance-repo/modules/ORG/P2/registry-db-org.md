# REGISTRY EXTRACT — registry-db-ORG.md
══════════════════════════════════════════════════════════════════
Module          : Organization (ORG)
Source artifact : dbs-org-001.md
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4 audit
══════════════════════════════════════════════════════════════════

---

## HEADER

| Field         | Value            |
|---------------|------------------|
| Module Name   | Organization     |
| Module Prefix | ORG              |
| DBS-ID        | DBS-ORG-001      |

---

## TABLES

| DBS-ID      | Table Name          | Source ENTITY-ID |
|-------------|---------------------|------------------|
| DBS-ORG-001 | ORG_REGION_TYPE     | ENTITY-ORG-008   |
| DBS-ORG-001 | ORG_LEGAL_ENTITY    | ENTITY-ORG-001   |
| DBS-ORG-001 | ORG_BRANCH          | ENTITY-ORG-002   |
| DBS-ORG-001 | ORG_REGION          | ENTITY-ORG-003   |
| DBS-ORG-001 | ORG_DEPARTMENT      | ENTITY-ORG-004   |
| DBS-ORG-001 | ORG_COST_CENTER     | ENTITY-ORG-005   |
| DBS-ORG-001 | ORG_PROFIT_CENTER   | ENTITY-ORG-006   |
| DBS-ORG-001 | ORG_LOCATION_SITE   | ENTITY-ORG-007   |

---

## DB FIELD TRACEABILITY

| DBF-ID   | Column Name             | DB Type        | Table (DBS-ID / Table Name)          | SRS Source                         |
|----------|-------------------------|----------------|--------------------------------------|------------------------------------|
| DBF-0001 | REGION_TYPE_PK          | NUMBER(10)     | DBS-ORG-001 / ORG_REGION_TYPE        | ENTITY-ORG-008.regionTypePk        |
| DBF-0002 | NAME_AR                 | VARCHAR2(200)  | DBS-ORG-001 / ORG_REGION_TYPE        | ENTITY-ORG-008.nameAr              |
| DBF-0003 | NAME_EN                 | VARCHAR2(100)  | DBS-ORG-001 / ORG_REGION_TYPE        | ENTITY-ORG-008.nameEn              |
| DBF-0004 | IS_ACTIVE_FL            | NUMBER(1)      | DBS-ORG-001 / ORG_REGION_TYPE        | ENTITY-ORG-008.isActiveFl          |
| DBF-0005 | CREATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_REGION_TYPE        | ENTITY-ORG-008.createdBy           |
| DBF-0006 | CREATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_REGION_TYPE        | ENTITY-ORG-008.createdAt           |
| DBF-0007 | UPDATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_REGION_TYPE        | ENTITY-ORG-008.updatedBy           |
| DBF-0008 | UPDATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_REGION_TYPE        | ENTITY-ORG-008.updatedAt           |
| DBF-0009 | LEGAL_ENTITY_PK         | NUMBER(10)     | DBS-ORG-001 / ORG_LEGAL_ENTITY       | ENTITY-ORG-001.legalEntityPk       |
| DBF-0010 | LEGAL_ENTITY_CODE       | VARCHAR2(20)   | DBS-ORG-001 / ORG_LEGAL_ENTITY       | ENTITY-ORG-001.legalEntityCode     |
| DBF-0011 | NAME_AR                 | VARCHAR2(200)  | DBS-ORG-001 / ORG_LEGAL_ENTITY       | ENTITY-ORG-001.nameAr              |
| DBF-0012 | NAME_EN                 | VARCHAR2(100)  | DBS-ORG-001 / ORG_LEGAL_ENTITY       | ENTITY-ORG-001.nameEn              |
| DBF-0013 | ENTITY_TYPE_ID          | VARCHAR2(50)   | DBS-ORG-001 / ORG_LEGAL_ENTITY       | ENTITY-ORG-001.entityTypeId (LOV-ORG-001) |
| DBF-0014 | IS_ACTIVE_FL            | NUMBER(1)      | DBS-ORG-001 / ORG_LEGAL_ENTITY       | ENTITY-ORG-001.isActiveFl          |
| DBF-0015 | NOTES                   | VARCHAR2(2000) | DBS-ORG-001 / ORG_LEGAL_ENTITY       | ENTITY-ORG-001.notes               |
| DBF-0016 | CREATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_LEGAL_ENTITY       | ENTITY-ORG-001.createdBy           |
| DBF-0017 | CREATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_LEGAL_ENTITY       | ENTITY-ORG-001.createdAt           |
| DBF-0018 | UPDATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_LEGAL_ENTITY       | ENTITY-ORG-001.updatedBy           |
| DBF-0019 | UPDATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_LEGAL_ENTITY       | ENTITY-ORG-001.updatedAt           |
| DBF-0020 | BRANCH_PK               | NUMBER(10)     | DBS-ORG-001 / ORG_BRANCH             | ENTITY-ORG-002.branchPk            |
| DBF-0021 | BRANCH_CODE             | VARCHAR2(20)   | DBS-ORG-001 / ORG_BRANCH             | ENTITY-ORG-002.branchCode          |
| DBF-0022 | NAME_AR                 | VARCHAR2(200)  | DBS-ORG-001 / ORG_BRANCH             | ENTITY-ORG-002.nameAr              |
| DBF-0023 | NAME_EN                 | VARCHAR2(100)  | DBS-ORG-001 / ORG_BRANCH             | ENTITY-ORG-002.nameEn              |
| DBF-0024 | LEGAL_ENTITY_FK         | NUMBER(10)     | DBS-ORG-001 / ORG_BRANCH             | ENTITY-ORG-002.legalEntityFk → ENTITY-ORG-001 |
| DBF-0025 | BRANCH_TYPE_ID          | VARCHAR2(50)   | DBS-ORG-001 / ORG_BRANCH             | ENTITY-ORG-002.branchTypeId (LOV-ORG-002) |
| DBF-0026 | IS_ACTIVE_FL            | NUMBER(1)      | DBS-ORG-001 / ORG_BRANCH             | ENTITY-ORG-002.isActiveFl          |
| DBF-0027 | NOTES                   | VARCHAR2(2000) | DBS-ORG-001 / ORG_BRANCH             | ENTITY-ORG-002.notes               |
| DBF-0028 | CREATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_BRANCH             | ENTITY-ORG-002.createdBy           |
| DBF-0029 | CREATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_BRANCH             | ENTITY-ORG-002.createdAt           |
| DBF-0030 | UPDATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_BRANCH             | ENTITY-ORG-002.updatedBy           |
| DBF-0031 | UPDATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_BRANCH             | ENTITY-ORG-002.updatedAt           |
| DBF-0032 | REGION_PK               | NUMBER(10)     | DBS-ORG-001 / ORG_REGION             | ENTITY-ORG-003.regionPk            |
| DBF-0033 | REGION_CODE             | VARCHAR2(20)   | DBS-ORG-001 / ORG_REGION             | ENTITY-ORG-003.regionCode          |
| DBF-0034 | NAME_AR                 | VARCHAR2(200)  | DBS-ORG-001 / ORG_REGION             | ENTITY-ORG-003.nameAr              |
| DBF-0035 | NAME_EN                 | VARCHAR2(100)  | DBS-ORG-001 / ORG_REGION             | ENTITY-ORG-003.nameEn              |
| DBF-0036 | LEGAL_ENTITY_FK         | NUMBER(10)     | DBS-ORG-001 / ORG_REGION             | ENTITY-ORG-003.legalEntityFk → ENTITY-ORG-001 |
| DBF-0037 | REGION_TYPE_FK          | NUMBER(10)     | DBS-ORG-001 / ORG_REGION             | ENTITY-ORG-003.regionTypeId → ENTITY-ORG-008 |
| DBF-0038 | IS_ACTIVE_FL            | NUMBER(1)      | DBS-ORG-001 / ORG_REGION             | ENTITY-ORG-003.isActiveFl          |
| DBF-0039 | NOTES                   | VARCHAR2(2000) | DBS-ORG-001 / ORG_REGION             | ENTITY-ORG-003.notes               |
| DBF-0040 | CREATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_REGION             | ENTITY-ORG-003.createdBy           |
| DBF-0041 | CREATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_REGION             | ENTITY-ORG-003.createdAt           |
| DBF-0042 | UPDATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_REGION             | ENTITY-ORG-003.updatedBy           |
| DBF-0043 | UPDATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_REGION             | ENTITY-ORG-003.updatedAt           |
| DBF-0044 | DEPARTMENT_PK           | NUMBER(10)     | DBS-ORG-001 / ORG_DEPARTMENT         | ENTITY-ORG-004.departmentPk        |
| DBF-0045 | DEPT_CODE               | VARCHAR2(20)   | DBS-ORG-001 / ORG_DEPARTMENT         | ENTITY-ORG-004.deptCode            |
| DBF-0046 | NAME_AR                 | VARCHAR2(200)  | DBS-ORG-001 / ORG_DEPARTMENT         | ENTITY-ORG-004.nameAr              |
| DBF-0047 | NAME_EN                 | VARCHAR2(100)  | DBS-ORG-001 / ORG_DEPARTMENT         | ENTITY-ORG-004.nameEn              |
| DBF-0048 | BRANCH_FK               | NUMBER(10)     | DBS-ORG-001 / ORG_DEPARTMENT         | ENTITY-ORG-004.branchFk → ENTITY-ORG-002 |
| DBF-0049 | PARENT_DEPARTMENT_FK    | NUMBER(10)     | DBS-ORG-001 / ORG_DEPARTMENT         | ENTITY-ORG-004.parentDepartmentFk (self-ref, NULLABLE) |
| DBF-0050 | NODE_TYPE_ID            | VARCHAR2(50)   | DBS-ORG-001 / ORG_DEPARTMENT         | ENTITY-ORG-004.nodeTypeId (LOV-ORG-003) |
| DBF-0051 | IS_ACTIVE_FL            | NUMBER(1)      | DBS-ORG-001 / ORG_DEPARTMENT         | ENTITY-ORG-004.isActiveFl          |
| DBF-0052 | NOTES                   | VARCHAR2(2000) | DBS-ORG-001 / ORG_DEPARTMENT         | ENTITY-ORG-004.notes               |
| DBF-0053 | CREATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_DEPARTMENT         | ENTITY-ORG-004.createdBy           |
| DBF-0054 | CREATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_DEPARTMENT         | ENTITY-ORG-004.createdAt           |
| DBF-0055 | UPDATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_DEPARTMENT         | ENTITY-ORG-004.updatedBy           |
| DBF-0056 | UPDATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_DEPARTMENT         | ENTITY-ORG-004.updatedAt           |
| DBF-0057 | COST_CENTER_PK          | NUMBER(10)     | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.costCenterPk        |
| DBF-0058 | COST_CENTER_CODE        | VARCHAR2(20)   | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.costCenterCode      |
| DBF-0059 | NAME_AR                 | VARCHAR2(200)  | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.nameAr              |
| DBF-0060 | NAME_EN                 | VARCHAR2(100)  | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.nameEn              |
| DBF-0061 | BRANCH_FK               | NUMBER(10)     | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.branchFk → ENTITY-ORG-002 |
| DBF-0062 | PARENT_COST_CENTER_FK   | NUMBER(10)     | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.parentCostCenterFk (self-ref, NULLABLE) |
| DBF-0063 | NODE_TYPE_ID            | VARCHAR2(50)   | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.nodeTypeId (LOV-ORG-004) |
| DBF-0064 | COST_CENTER_TYPE_ID     | VARCHAR2(50)   | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.costCenterTypeId (LOV-ORG-005) |
| DBF-0065 | IS_ACTIVE_FL            | NUMBER(1)      | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.isActiveFl          |
| DBF-0066 | NOTES                   | VARCHAR2(2000) | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.notes               |
| DBF-0067 | CREATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.createdBy           |
| DBF-0068 | CREATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.createdAt           |
| DBF-0069 | UPDATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.updatedBy           |
| DBF-0070 | UPDATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_COST_CENTER        | ENTITY-ORG-005.updatedAt           |
| DBF-0071 | PROFIT_CENTER_PK        | NUMBER(10)     | DBS-ORG-001 / ORG_PROFIT_CENTER      | ENTITY-ORG-006.profitCenterPk      |
| DBF-0072 | PROFIT_CENTER_CODE      | VARCHAR2(20)   | DBS-ORG-001 / ORG_PROFIT_CENTER      | ENTITY-ORG-006.profitCenterCode    |
| DBF-0073 | NAME_AR                 | VARCHAR2(200)  | DBS-ORG-001 / ORG_PROFIT_CENTER      | ENTITY-ORG-006.nameAr              |
| DBF-0074 | NAME_EN                 | VARCHAR2(100)  | DBS-ORG-001 / ORG_PROFIT_CENTER      | ENTITY-ORG-006.nameEn              |
| DBF-0075 | LEGAL_ENTITY_FK         | NUMBER(10)     | DBS-ORG-001 / ORG_PROFIT_CENTER      | ENTITY-ORG-006.legalEntityFk → ENTITY-ORG-001 |
| DBF-0076 | IS_ACTIVE_FL            | NUMBER(1)      | DBS-ORG-001 / ORG_PROFIT_CENTER      | ENTITY-ORG-006.isActiveFl          |
| DBF-0077 | NOTES                   | VARCHAR2(2000) | DBS-ORG-001 / ORG_PROFIT_CENTER      | ENTITY-ORG-006.notes               |
| DBF-0078 | CREATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_PROFIT_CENTER      | ENTITY-ORG-006.createdBy           |
| DBF-0079 | CREATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_PROFIT_CENTER      | ENTITY-ORG-006.createdAt           |
| DBF-0080 | UPDATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_PROFIT_CENTER      | ENTITY-ORG-006.updatedBy           |
| DBF-0081 | UPDATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_PROFIT_CENTER      | ENTITY-ORG-006.updatedAt           |
| DBF-0082 | LOCATION_SITE_PK        | NUMBER(10)     | DBS-ORG-001 / ORG_LOCATION_SITE      | ENTITY-ORG-007.locationSitePk      |
| DBF-0083 | LOCATION_CODE           | VARCHAR2(20)   | DBS-ORG-001 / ORG_LOCATION_SITE      | ENTITY-ORG-007.locationCode        |
| DBF-0084 | NAME_AR                 | VARCHAR2(200)  | DBS-ORG-001 / ORG_LOCATION_SITE      | ENTITY-ORG-007.nameAr              |
| DBF-0085 | NAME_EN                 | VARCHAR2(100)  | DBS-ORG-001 / ORG_LOCATION_SITE      | ENTITY-ORG-007.nameEn              |
| DBF-0086 | BRANCH_FK               | NUMBER(10)     | DBS-ORG-001 / ORG_LOCATION_SITE      | ENTITY-ORG-007.branchFk → ENTITY-ORG-002 |
| DBF-0087 | SITE_TYPE_ID            | VARCHAR2(50)   | DBS-ORG-001 / ORG_LOCATION_SITE      | ENTITY-ORG-007.siteTypeId (LOV-ORG-006) |
| DBF-0088 | IS_ACTIVE_FL            | NUMBER(1)      | DBS-ORG-001 / ORG_LOCATION_SITE      | ENTITY-ORG-007.isActiveFl          |
| DBF-0089 | NOTES                   | VARCHAR2(2000) | DBS-ORG-001 / ORG_LOCATION_SITE      | ENTITY-ORG-007.notes               |
| DBF-0090 | CREATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_LOCATION_SITE      | ENTITY-ORG-007.createdBy           |
| DBF-0091 | CREATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_LOCATION_SITE      | ENTITY-ORG-007.createdAt           |
| DBF-0092 | UPDATED_BY              | VARCHAR2(100)  | DBS-ORG-001 / ORG_LOCATION_SITE      | ENTITY-ORG-007.updatedBy           |
| DBF-0093 | UPDATED_AT              | TIMESTAMP      | DBS-ORG-001 / ORG_LOCATION_SITE      | ENTITY-ORG-007.updatedAt           |

Total: 93 DBF-IDs across 8 tables (DBF-0001..DBF-0093)

---

## LOV DDL REGISTER

| LOV-ID      | Table/Type Name       | Code Values                                                  |
|-------------|-----------------------|--------------------------------------------------------------|
| LOV-ORG-001 | LEGAL_ENTITY_TYPE     | HEAD_OFFICE, BRANCH_OFFICE, SUBSIDIARY, REPRESENTATIVE_OFFICE |
| LOV-ORG-002 | BRANCH_TYPE           | MAIN_BRANCH, SUB_BRANCH, OPERATIONS_BRANCH, ADMIN_BRANCH    |
| LOV-ORG-003 | DEPARTMENT_NODE_TYPE  | SUMMARY, DETAIL                                              |
| LOV-ORG-004 | COST_CENTER_NODE_TYPE | SUMMARY, DETAIL                                              |
| LOV-ORG-005 | COST_CENTER_TYPE      | DIRECT, INDIRECT, SHARED                                     |
| LOV-ORG-006 | LOCATION_SITE_TYPE    | OFFICE, WAREHOUSE, FACTORY, SITE, RETAIL                     |
| LOV-ORG-007 | ORG_REGION_TYPE       | GEOGRAPHIC, SALES, OPERATIONAL (Reference Table — own table) |

---

## XM REGISTER

XM Register: EMPTY — ROOT MODULE
Organization has zero outbound cross-module FK dependencies.
Consumer modules assign XM-IDs targeting ORG tables in their own MODE 1.5 sessions.
