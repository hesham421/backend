# DB SCRIPT — موديول الهيكل التنظيمي | Organization Module

```
DBS-ID         : DBS-ORG-001
Module         : Organization (ORG-001)
SRS Feature Code: ORG-001
DB_TARGET      : POSTGRESQL_16
Schema         : no schema prefix
Generated      : 2026-06-28
Status         : GOVERNED ✓ MODE 1.5
SRS Source     : srs-org-001.md v1.0 (GOVERNED ✓ MODE 2 — ALIGN GATE PASSED ✓)
Run in         : psql / pgAdmin
Open Questions : 1 active (OQ-001 — DEFERRED, non-blocking) — see OQ Log
XM-IDs         : None — ROOT MODULE — zero outbound cross-module dependencies
```

---

## DB FIELD TRACEABILITY MATRIX — Organization — DBS-ID: DBS-ORG-001

```
══════════════════════════════════════════════════════════════════════════════════════════════════════════════
DBF-ID    │ Table Name          │ Column Name              │ DB Type          │ SRS Source
──────────┼─────────────────────┼──────────────────────────┼──────────────────┼────────────────────────────────
--- ORG_LEGAL_ENTITY (ENTITY-ORG-001) ---
DBF-0001  │ ORG_LEGAL_ENTITY    │ LEGAL_ENTITY_PK          │ BIGINT           │ ENTITY-ORG-001.legal_entity_pk
DBF-0002  │ ORG_LEGAL_ENTITY    │ LEGAL_ENTITY_CODE        │ VARCHAR(20)      │ ENTITY-ORG-001.legal_entity_code
DBF-0003  │ ORG_LEGAL_ENTITY    │ NAME_AR                  │ VARCHAR(200)     │ ENTITY-ORG-001.name_ar
DBF-0004  │ ORG_LEGAL_ENTITY    │ NAME_EN                  │ VARCHAR(100)     │ ENTITY-ORG-001.name_en
DBF-0005  │ ORG_LEGAL_ENTITY    │ ENTITY_TYPE_ID           │ VARCHAR(50)      │ ENTITY-ORG-001.entity_type_id (LOV-ORG-001)
DBF-0006  │ ORG_LEGAL_ENTITY    │ IS_ACTIVE_FL             │ SMALLINT         │ ENTITY-ORG-001.is_active_fl
DBF-0007  │ ORG_LEGAL_ENTITY    │ NOTES                    │ VARCHAR(2000)    │ ENTITY-ORG-001.notes
DBF-0008  │ ORG_LEGAL_ENTITY    │ CREATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-001.created_by
DBF-0009  │ ORG_LEGAL_ENTITY    │ CREATED_AT               │ TIMESTAMP        │ ENTITY-ORG-001.created_at
DBF-0010  │ ORG_LEGAL_ENTITY    │ UPDATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-001.updated_by
DBF-0011  │ ORG_LEGAL_ENTITY    │ UPDATED_AT               │ TIMESTAMP        │ ENTITY-ORG-001.updated_at
──────────┼─────────────────────┼──────────────────────────┼──────────────────┼────────────────────────────────
--- ORG_BRANCH (ENTITY-ORG-002) ---
DBF-0012  │ ORG_BRANCH          │ BRANCH_PK                │ BIGINT           │ ENTITY-ORG-002.branch_pk
DBF-0013  │ ORG_BRANCH          │ BRANCH_CODE              │ VARCHAR(20)      │ ENTITY-ORG-002.branch_code
DBF-0014  │ ORG_BRANCH          │ NAME_AR                  │ VARCHAR(200)     │ ENTITY-ORG-002.name_ar
DBF-0015  │ ORG_BRANCH          │ NAME_EN                  │ VARCHAR(100)     │ ENTITY-ORG-002.name_en
DBF-0016  │ ORG_BRANCH          │ LEGAL_ENTITY_FK          │ BIGINT           │ ENTITY-ORG-002.legal_entity_fk → ORG_LEGAL_ENTITY
DBF-0017  │ ORG_BRANCH          │ BRANCH_TYPE_ID           │ VARCHAR(50)      │ ENTITY-ORG-002.branch_type_id (LOV-ORG-002)
DBF-0018  │ ORG_BRANCH          │ IS_ACTIVE_FL             │ SMALLINT         │ ENTITY-ORG-002.is_active_fl
DBF-0019  │ ORG_BRANCH          │ NOTES                    │ VARCHAR(2000)    │ ENTITY-ORG-002.notes
DBF-0020  │ ORG_BRANCH          │ CREATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-002.created_by
DBF-0021  │ ORG_BRANCH          │ CREATED_AT               │ TIMESTAMP        │ ENTITY-ORG-002.created_at
DBF-0022  │ ORG_BRANCH          │ UPDATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-002.updated_by
DBF-0023  │ ORG_BRANCH          │ UPDATED_AT               │ TIMESTAMP        │ ENTITY-ORG-002.updated_at
──────────┼─────────────────────┼──────────────────────────┼──────────────────┼────────────────────────────────
--- ORG_REGION_TYPE (ENTITY-ORG-008 — PRIVATE Reference Table, parent of ORG_REGION FK) ---
DBF-0024  │ ORG_REGION_TYPE     │ REGION_TYPE_PK           │ BIGINT           │ ENTITY-ORG-008.region_type_pk
DBF-0025  │ ORG_REGION_TYPE     │ REGION_TYPE_CODE         │ VARCHAR(30)      │ ENTITY-ORG-008.region_type_code
DBF-0026  │ ORG_REGION_TYPE     │ NAME_AR                  │ VARCHAR(200)     │ ENTITY-ORG-008.name_ar
DBF-0027  │ ORG_REGION_TYPE     │ NAME_EN                  │ VARCHAR(100)     │ ENTITY-ORG-008.name_en
DBF-0028  │ ORG_REGION_TYPE     │ IS_ACTIVE_FL             │ SMALLINT         │ ENTITY-ORG-008.is_active_fl
DBF-0029  │ ORG_REGION_TYPE     │ CREATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-008.created_by
DBF-0030  │ ORG_REGION_TYPE     │ CREATED_AT               │ TIMESTAMP        │ ENTITY-ORG-008.created_at
DBF-0031  │ ORG_REGION_TYPE     │ UPDATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-008.updated_by
DBF-0032  │ ORG_REGION_TYPE     │ UPDATED_AT               │ TIMESTAMP        │ ENTITY-ORG-008.updated_at
──────────┼─────────────────────┼──────────────────────────┼──────────────────┼────────────────────────────────
--- ORG_REGION (ENTITY-ORG-003) ---
DBF-0033  │ ORG_REGION          │ REGION_PK                │ BIGINT           │ ENTITY-ORG-003.region_pk
DBF-0034  │ ORG_REGION          │ REGION_CODE              │ VARCHAR(20)      │ ENTITY-ORG-003.region_code
DBF-0035  │ ORG_REGION          │ NAME_AR                  │ VARCHAR(200)     │ ENTITY-ORG-003.name_ar
DBF-0036  │ ORG_REGION          │ NAME_EN                  │ VARCHAR(100)     │ ENTITY-ORG-003.name_en
DBF-0037  │ ORG_REGION          │ LEGAL_ENTITY_FK          │ BIGINT           │ ENTITY-ORG-003.legal_entity_fk → ORG_LEGAL_ENTITY
DBF-0038  │ ORG_REGION          │ REGION_TYPE_ID_FK        │ BIGINT           │ ENTITY-ORG-003.region_type_id_fk → ORG_REGION_TYPE
DBF-0039  │ ORG_REGION          │ IS_ACTIVE_FL             │ SMALLINT         │ ENTITY-ORG-003.is_active_fl
DBF-0040  │ ORG_REGION          │ NOTES                    │ VARCHAR(2000)    │ ENTITY-ORG-003.notes
DBF-0041  │ ORG_REGION          │ CREATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-003.created_by
DBF-0042  │ ORG_REGION          │ CREATED_AT               │ TIMESTAMP        │ ENTITY-ORG-003.created_at
DBF-0043  │ ORG_REGION          │ UPDATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-003.updated_by
DBF-0044  │ ORG_REGION          │ UPDATED_AT               │ TIMESTAMP        │ ENTITY-ORG-003.updated_at
──────────┼─────────────────────┼──────────────────────────┼──────────────────┼────────────────────────────────
--- ORG_DEPARTMENT (ENTITY-ORG-004) ---
DBF-0045  │ ORG_DEPARTMENT      │ DEPARTMENT_PK            │ BIGINT           │ ENTITY-ORG-004.department_pk
DBF-0046  │ ORG_DEPARTMENT      │ DEPARTMENT_CODE          │ VARCHAR(20)      │ ENTITY-ORG-004.department_code
DBF-0047  │ ORG_DEPARTMENT      │ NAME_AR                  │ VARCHAR(200)     │ ENTITY-ORG-004.name_ar
DBF-0048  │ ORG_DEPARTMENT      │ NAME_EN                  │ VARCHAR(100)     │ ENTITY-ORG-004.name_en
DBF-0049  │ ORG_DEPARTMENT      │ BRANCH_FK                │ BIGINT           │ ENTITY-ORG-004.branch_fk → ORG_BRANCH
DBF-0050  │ ORG_DEPARTMENT      │ PARENT_DEPARTMENT_FK     │ BIGINT           │ ENTITY-ORG-004.parent_department_fk → ORG_DEPARTMENT (self)
DBF-0051  │ ORG_DEPARTMENT      │ NODE_TYPE_ID             │ VARCHAR(50)      │ ENTITY-ORG-004.node_type_id (LOV-ORG-003)
DBF-0052  │ ORG_DEPARTMENT      │ IS_ACTIVE_FL             │ SMALLINT         │ ENTITY-ORG-004.is_active_fl
DBF-0053  │ ORG_DEPARTMENT      │ NOTES                    │ VARCHAR(2000)    │ ENTITY-ORG-004.notes
DBF-0054  │ ORG_DEPARTMENT      │ CREATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-004.created_by
DBF-0055  │ ORG_DEPARTMENT      │ CREATED_AT               │ TIMESTAMP        │ ENTITY-ORG-004.created_at
DBF-0056  │ ORG_DEPARTMENT      │ UPDATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-004.updated_by
DBF-0057  │ ORG_DEPARTMENT      │ UPDATED_AT               │ TIMESTAMP        │ ENTITY-ORG-004.updated_at
──────────┼─────────────────────┼──────────────────────────┼──────────────────┼────────────────────────────────
--- ORG_COST_CENTER (ENTITY-ORG-005) ---
DBF-0058  │ ORG_COST_CENTER     │ COST_CENTER_PK           │ BIGINT           │ ENTITY-ORG-005.cost_center_pk
DBF-0059  │ ORG_COST_CENTER     │ COST_CENTER_CODE         │ VARCHAR(20)      │ ENTITY-ORG-005.cost_center_code
DBF-0060  │ ORG_COST_CENTER     │ NAME_AR                  │ VARCHAR(200)     │ ENTITY-ORG-005.name_ar
DBF-0061  │ ORG_COST_CENTER     │ NAME_EN                  │ VARCHAR(100)     │ ENTITY-ORG-005.name_en
DBF-0062  │ ORG_COST_CENTER     │ BRANCH_FK                │ BIGINT           │ ENTITY-ORG-005.branch_fk → ORG_BRANCH
DBF-0063  │ ORG_COST_CENTER     │ PARENT_COST_CENTER_FK    │ BIGINT           │ ENTITY-ORG-005.parent_cost_center_fk → ORG_COST_CENTER (self)
DBF-0064  │ ORG_COST_CENTER     │ NODE_TYPE_ID             │ VARCHAR(50)      │ ENTITY-ORG-005.node_type_id (LOV-ORG-004)
DBF-0065  │ ORG_COST_CENTER     │ COST_CENTER_TYPE_ID      │ VARCHAR(50)      │ ENTITY-ORG-005.cost_center_type_id (LOV-ORG-005)
DBF-0066  │ ORG_COST_CENTER     │ IS_ACTIVE_FL             │ SMALLINT         │ ENTITY-ORG-005.is_active_fl
DBF-0067  │ ORG_COST_CENTER     │ NOTES                    │ VARCHAR(2000)    │ ENTITY-ORG-005.notes
DBF-0068  │ ORG_COST_CENTER     │ CREATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-005.created_by
DBF-0069  │ ORG_COST_CENTER     │ CREATED_AT               │ TIMESTAMP        │ ENTITY-ORG-005.created_at
DBF-0070  │ ORG_COST_CENTER     │ UPDATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-005.updated_by
DBF-0071  │ ORG_COST_CENTER     │ UPDATED_AT               │ TIMESTAMP        │ ENTITY-ORG-005.updated_at
──────────┼─────────────────────┼──────────────────────────┼──────────────────┼────────────────────────────────
--- ORG_PROFIT_CENTER (ENTITY-ORG-006) ---
DBF-0072  │ ORG_PROFIT_CENTER   │ PROFIT_CENTER_PK         │ BIGINT           │ ENTITY-ORG-006.profit_center_pk
DBF-0073  │ ORG_PROFIT_CENTER   │ PROFIT_CENTER_CODE       │ VARCHAR(20)      │ ENTITY-ORG-006.profit_center_code
DBF-0074  │ ORG_PROFIT_CENTER   │ NAME_AR                  │ VARCHAR(200)     │ ENTITY-ORG-006.name_ar
DBF-0075  │ ORG_PROFIT_CENTER   │ NAME_EN                  │ VARCHAR(100)     │ ENTITY-ORG-006.name_en
DBF-0076  │ ORG_PROFIT_CENTER   │ LEGAL_ENTITY_FK          │ BIGINT           │ ENTITY-ORG-006.legal_entity_fk → ORG_LEGAL_ENTITY
DBF-0077  │ ORG_PROFIT_CENTER   │ IS_ACTIVE_FL             │ SMALLINT         │ ENTITY-ORG-006.is_active_fl
DBF-0078  │ ORG_PROFIT_CENTER   │ NOTES                    │ VARCHAR(2000)    │ ENTITY-ORG-006.notes
DBF-0079  │ ORG_PROFIT_CENTER   │ CREATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-006.created_by
DBF-0080  │ ORG_PROFIT_CENTER   │ CREATED_AT               │ TIMESTAMP        │ ENTITY-ORG-006.created_at
DBF-0081  │ ORG_PROFIT_CENTER   │ UPDATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-006.updated_by
DBF-0082  │ ORG_PROFIT_CENTER   │ UPDATED_AT               │ TIMESTAMP        │ ENTITY-ORG-006.updated_at
──────────┼─────────────────────┼──────────────────────────┼──────────────────┼────────────────────────────────
--- ORG_LOCATION_SITE (ENTITY-ORG-007) ---
DBF-0083  │ ORG_LOCATION_SITE   │ LOCATION_SITE_PK         │ BIGINT           │ ENTITY-ORG-007.location_site_pk
DBF-0084  │ ORG_LOCATION_SITE   │ LOCATION_SITE_CODE       │ VARCHAR(20)      │ ENTITY-ORG-007.location_site_code
DBF-0085  │ ORG_LOCATION_SITE   │ NAME_AR                  │ VARCHAR(200)     │ ENTITY-ORG-007.name_ar
DBF-0086  │ ORG_LOCATION_SITE   │ NAME_EN                  │ VARCHAR(100)     │ ENTITY-ORG-007.name_en
DBF-0087  │ ORG_LOCATION_SITE   │ BRANCH_FK                │ BIGINT           │ ENTITY-ORG-007.branch_fk → ORG_BRANCH
DBF-0088  │ ORG_LOCATION_SITE   │ SITE_TYPE_ID             │ VARCHAR(50)      │ ENTITY-ORG-007.site_type_id (LOV-ORG-006)
DBF-0089  │ ORG_LOCATION_SITE   │ IS_ACTIVE_FL             │ SMALLINT         │ ENTITY-ORG-007.is_active_fl
DBF-0090  │ ORG_LOCATION_SITE   │ NOTES                    │ VARCHAR(2000)    │ ENTITY-ORG-007.notes
DBF-0091  │ ORG_LOCATION_SITE   │ CREATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-007.created_by
DBF-0092  │ ORG_LOCATION_SITE   │ CREATED_AT               │ TIMESTAMP        │ ENTITY-ORG-007.created_at
DBF-0093  │ ORG_LOCATION_SITE   │ UPDATED_BY               │ VARCHAR(255)     │ ENTITY-ORG-007.updated_by
DBF-0094  │ ORG_LOCATION_SITE   │ UPDATED_AT               │ TIMESTAMP        │ ENTITY-ORG-007.updated_at
══════════════════════════════════════════════════════════════════════════════════════════════════════════════
Total: 94 DBF-IDs across 8 tables
```

---

## CROSS-MODULE DEPENDENCY REGISTER (XM REGISTER) — Organization — DBS-ID: DBS-ORG-001

```
══════════════════════════════════════════════════════════════════════════════════════════════════
XM-ID  │ Type  │ This Table  │ FK/Ref Column  │ Target Table  │ Target Module  │ Status
═══════════════════════════════════════════════════════════════════════════════════════════════════
— None — Organization is ROOT MODULE — zero outbound cross-module dependencies —
══════════════════════════════════════════════════════════════════════════════════════════════════
Total: 0 XM-IDs
Note: All FKs in this module are INTRA-MODULE (all referenced tables are owned by ORG-001).
      ORG_REGION_TYPE → ORG_REGION: intra-module HARD-FK
      ORG_LEGAL_ENTITY → ORG_BRANCH, ORG_REGION, ORG_PROFIT_CENTER: intra-module HARD-FK
      ORG_BRANCH → ORG_DEPARTMENT, ORG_COST_CENTER, ORG_LOCATION_SITE: intra-module HARD-FK
      ORG_DEPARTMENT → ORG_DEPARTMENT (self): intra-module self-reference
      ORG_COST_CENTER → ORG_COST_CENTER (self): intra-module self-reference
```

---

## FULL_DATABASE_SCRIPT

> Complete executable script for Organization Module — DBS-ID: DBS-ORG-001
> DB_TARGET: POSTGRESQL_16 | Schema: no schema prefix
> Generated: 2026-06-28 | SRS Feature Code: ORG-001
> Run in psql or pgAdmin against a clean schema.
> No manual editing required.

```sql
-- ============================================================
-- ORGANIZATION MODULE — COMPLETE DATABASE SCRIPT
-- DBS-ID     : DBS-ORG-001
-- SRS Code   : ORG-001
-- DB_TARGET  : POSTGRESQL_16
-- Generated  : 2026-06-28
-- Tables     : ORG_LEGAL_ENTITY, ORG_BRANCH, ORG_REGION_TYPE,
--              ORG_REGION, ORG_DEPARTMENT, ORG_COST_CENTER,
--              ORG_PROFIT_CENTER, ORG_LOCATION_SITE
-- DBF-IDs    : DBF-0001 through DBF-0094
-- XM-IDs     : None (ROOT MODULE)
-- ============================================================

-- ============================================================
-- BLOCK 1: SEQUENCES
-- ============================================================

CREATE SEQUENCE ORG_LEGAL_ENTITY_SEQ
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

CREATE SEQUENCE ORG_BRANCH_SEQ
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

CREATE SEQUENCE ORG_REGION_TYPE_SEQ
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

CREATE SEQUENCE ORG_REGION_SEQ
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

CREATE SEQUENCE ORG_DEPARTMENT_SEQ
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

CREATE SEQUENCE ORG_COST_CENTER_SEQ
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

CREATE SEQUENCE ORG_PROFIT_CENTER_SEQ
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

CREATE SEQUENCE ORG_LOCATION_SITE_SEQ
  START WITH 1
  INCREMENT BY 1
  NO CACHE
  NO CYCLE;

-- ============================================================
-- BLOCK 2: PARENT TABLES (no intra-module FK dependencies)
-- ============================================================

-- DBF-0001 to DBF-0011 — ORG_LEGAL_ENTITY (ENTITY-ORG-001)
-- Referenced by: ORG_BRANCH, ORG_REGION, ORG_PROFIT_CENTER
CREATE TABLE ORG_LEGAL_ENTITY (
    LEGAL_ENTITY_PK   BIGINT          NOT NULL,
    LEGAL_ENTITY_CODE VARCHAR(20)     NOT NULL,
    NAME_AR           VARCHAR(200)    NOT NULL,
    NAME_EN           VARCHAR(100)    NOT NULL,
    ENTITY_TYPE_ID    VARCHAR(50)     NOT NULL,
    IS_ACTIVE_FL      SMALLINT        NOT NULL DEFAULT 1,
    NOTES             VARCHAR(2000),
    CREATED_BY        VARCHAR(255)    NOT NULL,
    CREATED_AT        TIMESTAMP       NOT NULL,
    UPDATED_BY        VARCHAR(255),
    UPDATED_AT        TIMESTAMP
);

-- DBF-0024 to DBF-0032 — ORG_REGION_TYPE (ENTITY-ORG-008 — PRIVATE Reference Table)
-- Referenced by: ORG_REGION
-- > 15 potential values → Reference Table (not MD_LOOKUP_DETAIL)
CREATE TABLE ORG_REGION_TYPE (
    REGION_TYPE_PK    BIGINT          NOT NULL,
    REGION_TYPE_CODE  VARCHAR(30)     NOT NULL,
    NAME_AR           VARCHAR(200)    NOT NULL,
    NAME_EN           VARCHAR(100)    NOT NULL,
    IS_ACTIVE_FL      SMALLINT        NOT NULL DEFAULT 1,
    CREATED_BY        VARCHAR(255)    NOT NULL,
    CREATED_AT        TIMESTAMP       NOT NULL,
    UPDATED_BY        VARCHAR(255),
    UPDATED_AT        TIMESTAMP
);

-- ============================================================
-- BLOCK 3: CHILD TABLES (intra-module FK dependencies)
-- Order: ORG_BRANCH (→ ORG_LEGAL_ENTITY)
--        ORG_REGION (→ ORG_LEGAL_ENTITY, ORG_REGION_TYPE)
--        ORG_PROFIT_CENTER (→ ORG_LEGAL_ENTITY)
--        ORG_DEPARTMENT (→ ORG_BRANCH, self)
--        ORG_COST_CENTER (→ ORG_BRANCH, self)
--        ORG_LOCATION_SITE (→ ORG_BRANCH)
-- ============================================================

-- DBF-0012 to DBF-0023 — ORG_BRANCH (ENTITY-ORG-002)
CREATE TABLE ORG_BRANCH (
    BRANCH_PK         BIGINT          NOT NULL,
    BRANCH_CODE       VARCHAR(20)     NOT NULL,
    NAME_AR           VARCHAR(200)    NOT NULL,
    NAME_EN           VARCHAR(100)    NOT NULL,
    LEGAL_ENTITY_FK   BIGINT          NOT NULL,
    BRANCH_TYPE_ID    VARCHAR(50)     NOT NULL,
    IS_ACTIVE_FL      SMALLINT        NOT NULL DEFAULT 1,
    NOTES             VARCHAR(2000),
    CREATED_BY        VARCHAR(255)    NOT NULL,
    CREATED_AT        TIMESTAMP       NOT NULL,
    UPDATED_BY        VARCHAR(255),
    UPDATED_AT        TIMESTAMP
);

-- DBF-0033 to DBF-0044 — ORG_REGION (ENTITY-ORG-003)
CREATE TABLE ORG_REGION (
    REGION_PK         BIGINT          NOT NULL,
    REGION_CODE       VARCHAR(20)     NOT NULL,
    NAME_AR           VARCHAR(200)    NOT NULL,
    NAME_EN           VARCHAR(100)    NOT NULL,
    LEGAL_ENTITY_FK   BIGINT          NOT NULL,
    REGION_TYPE_ID_FK BIGINT          NOT NULL,
    IS_ACTIVE_FL      SMALLINT        NOT NULL DEFAULT 1,
    NOTES             VARCHAR(2000),
    CREATED_BY        VARCHAR(255)    NOT NULL,
    CREATED_AT        TIMESTAMP       NOT NULL,
    UPDATED_BY        VARCHAR(255),
    UPDATED_AT        TIMESTAMP
);

-- DBF-0072 to DBF-0082 — ORG_PROFIT_CENTER (ENTITY-ORG-006)
CREATE TABLE ORG_PROFIT_CENTER (
    PROFIT_CENTER_PK   BIGINT         NOT NULL,
    PROFIT_CENTER_CODE VARCHAR(20)    NOT NULL,
    NAME_AR            VARCHAR(200)   NOT NULL,
    NAME_EN            VARCHAR(100)   NOT NULL,
    LEGAL_ENTITY_FK    BIGINT         NOT NULL,
    IS_ACTIVE_FL       SMALLINT       NOT NULL DEFAULT 1,
    NOTES              VARCHAR(2000),
    CREATED_BY         VARCHAR(255)   NOT NULL,
    CREATED_AT         TIMESTAMP      NOT NULL,
    UPDATED_BY         VARCHAR(255),
    UPDATED_AT         TIMESTAMP
);

-- DBF-0045 to DBF-0057 — ORG_DEPARTMENT (ENTITY-ORG-004) — tree: self-ref NULLABLE
CREATE TABLE ORG_DEPARTMENT (
    DEPARTMENT_PK         BIGINT       NOT NULL,
    DEPARTMENT_CODE       VARCHAR(20)  NOT NULL,
    NAME_AR               VARCHAR(200) NOT NULL,
    NAME_EN               VARCHAR(100) NOT NULL,
    BRANCH_FK             BIGINT       NOT NULL,
    PARENT_DEPARTMENT_FK  BIGINT,
    NODE_TYPE_ID          VARCHAR(50)  NOT NULL,
    IS_ACTIVE_FL          SMALLINT     NOT NULL DEFAULT 1,
    NOTES                 VARCHAR(2000),
    CREATED_BY            VARCHAR(255) NOT NULL,
    CREATED_AT            TIMESTAMP    NOT NULL,
    UPDATED_BY            VARCHAR(255),
    UPDATED_AT            TIMESTAMP
);

-- DBF-0058 to DBF-0071 — ORG_COST_CENTER (ENTITY-ORG-005) — tree: self-ref NULLABLE
CREATE TABLE ORG_COST_CENTER (
    COST_CENTER_PK       BIGINT        NOT NULL,
    COST_CENTER_CODE     VARCHAR(20)   NOT NULL,
    NAME_AR              VARCHAR(200)  NOT NULL,
    NAME_EN              VARCHAR(100)  NOT NULL,
    BRANCH_FK            BIGINT        NOT NULL,
    PARENT_COST_CENTER_FK BIGINT,
    NODE_TYPE_ID         VARCHAR(50)   NOT NULL,
    COST_CENTER_TYPE_ID  VARCHAR(50)   NOT NULL,
    IS_ACTIVE_FL         SMALLINT      NOT NULL DEFAULT 1,
    NOTES                VARCHAR(2000),
    CREATED_BY           VARCHAR(255)  NOT NULL,
    CREATED_AT           TIMESTAMP     NOT NULL,
    UPDATED_BY           VARCHAR(255),
    UPDATED_AT           TIMESTAMP
);

-- DBF-0083 to DBF-0094 — ORG_LOCATION_SITE (ENTITY-ORG-007)
CREATE TABLE ORG_LOCATION_SITE (
    LOCATION_SITE_PK    BIGINT         NOT NULL,
    LOCATION_SITE_CODE  VARCHAR(20)    NOT NULL,
    NAME_AR             VARCHAR(200)   NOT NULL,
    NAME_EN             VARCHAR(100)   NOT NULL,
    BRANCH_FK           BIGINT         NOT NULL,
    SITE_TYPE_ID        VARCHAR(50)    NOT NULL,
    IS_ACTIVE_FL        SMALLINT       NOT NULL DEFAULT 1,
    NOTES               VARCHAR(2000),
    CREATED_BY          VARCHAR(255)   NOT NULL,
    CREATED_AT          TIMESTAMP      NOT NULL,
    UPDATED_BY          VARCHAR(255),
    UPDATED_AT          TIMESTAMP
);

-- ============================================================
-- BLOCK 4: COMMENTS
-- ============================================================

-- ORG_LEGAL_ENTITY
COMMENT ON TABLE ORG_LEGAL_ENTITY IS 'الكيانات القانونية المسجلة للمنشأة — Legal entities registered in the system. ROOT entity. ENTITY-ORG-001.';
COMMENT ON COLUMN ORG_LEGAL_ENTITY.LEGAL_ENTITY_PK   IS 'DBF-0001 — PK. Populated by framework from ORG_LEGAL_ENTITY_SEQ.';
COMMENT ON COLUMN ORG_LEGAL_ENTITY.LEGAL_ENTITY_CODE IS 'DBF-0002 — Business code LE-NNNNN. Generated by NumberingEngine. Immutable after first save.';
COMMENT ON COLUMN ORG_LEGAL_ENTITY.NAME_AR           IS 'DBF-0003 — Arabic name. Unique within global scope (RULE-ORG-015).';
COMMENT ON COLUMN ORG_LEGAL_ENTITY.NAME_EN           IS 'DBF-0004 — English name. Unique within global scope (RULE-ORG-015).';
COMMENT ON COLUMN ORG_LEGAL_ENTITY.ENTITY_TYPE_ID    IS 'DBF-0005 — Stores LOV code. lookupKey: LEGAL_ENTITY_TYPE (LOV-ORG-001). Values: HEAD_OFFICE, BRANCH_OFFICE, SUBSIDIARY, REPRESENTATIVE_OFFICE.';
COMMENT ON COLUMN ORG_LEGAL_ENTITY.IS_ACTIVE_FL      IS 'DBF-0006 — 1=Active, 0=Inactive. Deactivation governed by RULE-ORG-001 and RULE-ORG-002.';
COMMENT ON COLUMN ORG_LEGAL_ENTITY.NOTES             IS 'DBF-0007 — Optional free-text notes.';
COMMENT ON COLUMN ORG_LEGAL_ENTITY.CREATED_BY        IS 'DBF-0008 — Audit. Populated by AuditEntityListener. Not accepted in DTO.';
COMMENT ON COLUMN ORG_LEGAL_ENTITY.CREATED_AT        IS 'DBF-0009 — Audit. Populated by AuditEntityListener. Not accepted in DTO.';
COMMENT ON COLUMN ORG_LEGAL_ENTITY.UPDATED_BY        IS 'DBF-0010 — Audit. Populated by AuditEntityListener. Not accepted in DTO.';
COMMENT ON COLUMN ORG_LEGAL_ENTITY.UPDATED_AT        IS 'DBF-0011 — Audit. Populated by AuditEntityListener. Not accepted in DTO.';

-- ORG_BRANCH
COMMENT ON TABLE ORG_BRANCH IS 'فروع الكيانات القانونية — Branches of legal entities. Primary DataScope boundary. ENTITY-ORG-002.';
COMMENT ON COLUMN ORG_BRANCH.BRANCH_PK       IS 'DBF-0012 — PK. Populated by framework from ORG_BRANCH_SEQ.';
COMMENT ON COLUMN ORG_BRANCH.BRANCH_CODE     IS 'DBF-0013 — Business code BR-[LE_CODE]-NNNNN. Generated by NumberingEngine. Immutable after first save.';
COMMENT ON COLUMN ORG_BRANCH.NAME_AR         IS 'DBF-0014 — Arabic name. Unique within same LegalEntity (RULE-ORG-015).';
COMMENT ON COLUMN ORG_BRANCH.NAME_EN         IS 'DBF-0015 — English name. Unique within same LegalEntity (RULE-ORG-015).';
COMMENT ON COLUMN ORG_BRANCH.LEGAL_ENTITY_FK IS 'DBF-0016 — FK to ORG_LEGAL_ENTITY. NOT NULL. Creation blocked if parent LegalEntity inactive (RULE-ORG-018).';
COMMENT ON COLUMN ORG_BRANCH.BRANCH_TYPE_ID  IS 'DBF-0017 — Stores LOV code. lookupKey: BRANCH_TYPE (LOV-ORG-002). Values: MAIN_BRANCH, SUB_BRANCH, OPERATIONS_BRANCH, ADMIN_BRANCH.';
COMMENT ON COLUMN ORG_BRANCH.IS_ACTIVE_FL    IS 'DBF-0018 — 1=Active, 0=Inactive. Deactivation governed by RULE-ORG-003, 004, 005.';
COMMENT ON COLUMN ORG_BRANCH.NOTES           IS 'DBF-0019 — Optional free-text notes.';
COMMENT ON COLUMN ORG_BRANCH.CREATED_BY      IS 'DBF-0020 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_BRANCH.CREATED_AT      IS 'DBF-0021 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_BRANCH.UPDATED_BY      IS 'DBF-0022 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_BRANCH.UPDATED_AT      IS 'DBF-0023 — Audit. Populated by AuditEntityListener.';

-- ORG_REGION_TYPE
COMMENT ON TABLE ORG_REGION_TYPE IS 'أنواع المناطق — Region type reference table. PRIVATE entity (owner: Organization). > 15 potential values. ENTITY-ORG-008.';
COMMENT ON COLUMN ORG_REGION_TYPE.REGION_TYPE_PK   IS 'DBF-0024 — PK. Populated by framework from ORG_REGION_TYPE_SEQ.';
COMMENT ON COLUMN ORG_REGION_TYPE.REGION_TYPE_CODE IS 'DBF-0025 — Unique business code. e.g. GEOGRAPHIC, SALES, OPERATIONAL.';
COMMENT ON COLUMN ORG_REGION_TYPE.NAME_AR          IS 'DBF-0026 — Arabic name.';
COMMENT ON COLUMN ORG_REGION_TYPE.NAME_EN          IS 'DBF-0027 — English name.';
COMMENT ON COLUMN ORG_REGION_TYPE.IS_ACTIVE_FL     IS 'DBF-0028 — 1=Active, 0=Inactive.';
COMMENT ON COLUMN ORG_REGION_TYPE.CREATED_BY       IS 'DBF-0029 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_REGION_TYPE.CREATED_AT       IS 'DBF-0030 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_REGION_TYPE.UPDATED_BY       IS 'DBF-0031 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_REGION_TYPE.UPDATED_AT       IS 'DBF-0032 — Audit. Populated by AuditEntityListener.';

-- ORG_REGION
COMMENT ON TABLE ORG_REGION IS 'المناطق الجغرافية / المبيعاتية — Regions linked to a legal entity. ENTITY-ORG-003. OQ-001: SOFT-READ impact on deactivation — DEFERRED.';
COMMENT ON COLUMN ORG_REGION.REGION_PK         IS 'DBF-0033 — PK. Populated by framework from ORG_REGION_SEQ.';
COMMENT ON COLUMN ORG_REGION.REGION_CODE       IS 'DBF-0034 — Business code RG-[LE_CODE]-NNNNN. Generated by NumberingEngine. Immutable after first save.';
COMMENT ON COLUMN ORG_REGION.NAME_AR           IS 'DBF-0035 — Arabic name. Unique within same LegalEntity (RULE-ORG-015).';
COMMENT ON COLUMN ORG_REGION.NAME_EN           IS 'DBF-0036 — English name. Unique within same LegalEntity (RULE-ORG-015).';
COMMENT ON COLUMN ORG_REGION.LEGAL_ENTITY_FK   IS 'DBF-0037 — FK to ORG_LEGAL_ENTITY. NOT NULL.';
COMMENT ON COLUMN ORG_REGION.REGION_TYPE_ID_FK IS 'DBF-0038 — FK to ORG_REGION_TYPE. NOT NULL.';
COMMENT ON COLUMN ORG_REGION.IS_ACTIVE_FL      IS 'DBF-0039 — 1=Active, 0=Inactive. Deactivation governed by RULE-ORG-006 (active branches check) and RULE-ORG-017 (SOFT-READ consumer warning). OQ-001 DEFERRED.';
COMMENT ON COLUMN ORG_REGION.NOTES             IS 'DBF-0040 — Optional free-text notes.';
COMMENT ON COLUMN ORG_REGION.CREATED_BY        IS 'DBF-0041 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_REGION.CREATED_AT        IS 'DBF-0042 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_REGION.UPDATED_BY        IS 'DBF-0043 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_REGION.UPDATED_AT        IS 'DBF-0044 — Audit. Populated by AuditEntityListener.';

-- ORG_DEPARTMENT
COMMENT ON TABLE ORG_DEPARTMENT IS 'الأقسام — Hierarchical department tree per branch. ENTITY-ORG-004. Nodes: SUMMARY (aggregate) / DETAIL (leaf). Circular reference prevented by RULE-ORG-007.';
COMMENT ON COLUMN ORG_DEPARTMENT.DEPARTMENT_PK        IS 'DBF-0045 — PK. Populated by framework from ORG_DEPARTMENT_SEQ.';
COMMENT ON COLUMN ORG_DEPARTMENT.DEPARTMENT_CODE      IS 'DBF-0046 — Business code DEP-[BR_CODE]-NNNNN. Generated by NumberingEngine. Immutable after first save.';
COMMENT ON COLUMN ORG_DEPARTMENT.NAME_AR              IS 'DBF-0047 — Arabic name. Unique within same Branch (RULE-ORG-015).';
COMMENT ON COLUMN ORG_DEPARTMENT.NAME_EN              IS 'DBF-0048 — English name. Unique within same Branch (RULE-ORG-015).';
COMMENT ON COLUMN ORG_DEPARTMENT.BRANCH_FK            IS 'DBF-0049 — FK to ORG_BRANCH. NOT NULL. Creation blocked if Branch inactive (RULE-ORG-019).';
COMMENT ON COLUMN ORG_DEPARTMENT.PARENT_DEPARTMENT_FK IS 'DBF-0050 — Self-reference FK to ORG_DEPARTMENT. NULLABLE (root nodes have no parent). Circular reference blocked by RULE-ORG-007.';
COMMENT ON COLUMN ORG_DEPARTMENT.NODE_TYPE_ID         IS 'DBF-0051 — lookupKey: DEPARTMENT_NODE_TYPE (LOV-ORG-003). Values: SUMMARY, DETAIL. Immutable after first save (RULE-ORG-020). SUMMARY nodes cannot be assigned to transactional records (RULE-ORG-009).';
COMMENT ON COLUMN ORG_DEPARTMENT.IS_ACTIVE_FL         IS 'DBF-0052 — 1=Active, 0=Inactive.';
COMMENT ON COLUMN ORG_DEPARTMENT.NOTES                IS 'DBF-0053 — Optional free-text notes.';
COMMENT ON COLUMN ORG_DEPARTMENT.CREATED_BY           IS 'DBF-0054 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_DEPARTMENT.CREATED_AT           IS 'DBF-0055 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_DEPARTMENT.UPDATED_BY           IS 'DBF-0056 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_DEPARTMENT.UPDATED_AT           IS 'DBF-0057 — Audit. Populated by AuditEntityListener.';

-- ORG_COST_CENTER
COMMENT ON TABLE ORG_COST_CENTER IS 'مراكز التكلفة — Hierarchical cost center tree per branch. ENTITY-ORG-005. Nodes: SUMMARY / DETAIL. Finance (3.4) primary consumer via HARD-FK.';
COMMENT ON COLUMN ORG_COST_CENTER.COST_CENTER_PK       IS 'DBF-0058 — PK. Populated by framework from ORG_COST_CENTER_SEQ.';
COMMENT ON COLUMN ORG_COST_CENTER.COST_CENTER_CODE     IS 'DBF-0059 — Business code CC-[BR_CODE]-NNNNN. Generated by NumberingEngine. Immutable after first save.';
COMMENT ON COLUMN ORG_COST_CENTER.NAME_AR              IS 'DBF-0060 — Arabic name. Unique within same Branch (RULE-ORG-015).';
COMMENT ON COLUMN ORG_COST_CENTER.NAME_EN              IS 'DBF-0061 — English name. Unique within same Branch (RULE-ORG-015).';
COMMENT ON COLUMN ORG_COST_CENTER.BRANCH_FK            IS 'DBF-0062 — FK to ORG_BRANCH. NOT NULL. Creation blocked if Branch inactive (RULE-ORG-019).';
COMMENT ON COLUMN ORG_COST_CENTER.PARENT_COST_CENTER_FK IS 'DBF-0063 — Self-reference FK to ORG_COST_CENTER. NULLABLE (root nodes have no parent). Circular reference blocked by RULE-ORG-008.';
COMMENT ON COLUMN ORG_COST_CENTER.NODE_TYPE_ID         IS 'DBF-0064 — lookupKey: COST_CENTER_NODE_TYPE (LOV-ORG-004). Values: SUMMARY, DETAIL. Immutable after first save (RULE-ORG-020). SUMMARY nodes cannot be assigned to transactional records (RULE-ORG-010).';
COMMENT ON COLUMN ORG_COST_CENTER.COST_CENTER_TYPE_ID  IS 'DBF-0065 — lookupKey: COST_CENTER_TYPE (LOV-ORG-005). Values: DIRECT, INDIRECT, SHARED.';
COMMENT ON COLUMN ORG_COST_CENTER.IS_ACTIVE_FL         IS 'DBF-0066 — 1=Active, 0=Inactive.';
COMMENT ON COLUMN ORG_COST_CENTER.NOTES                IS 'DBF-0067 — Optional free-text notes.';
COMMENT ON COLUMN ORG_COST_CENTER.CREATED_BY           IS 'DBF-0068 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_COST_CENTER.CREATED_AT           IS 'DBF-0069 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_COST_CENTER.UPDATED_BY           IS 'DBF-0070 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_COST_CENTER.UPDATED_AT           IS 'DBF-0071 — Audit. Populated by AuditEntityListener.';

-- ORG_PROFIT_CENTER
COMMENT ON TABLE ORG_PROFIT_CENTER IS 'مراكز الربح — Profit centers linked to a legal entity. ENTITY-ORG-006. Owned by Organization — consumed by Finance (3.4) via HARD-FK.';
COMMENT ON COLUMN ORG_PROFIT_CENTER.PROFIT_CENTER_PK   IS 'DBF-0072 — PK. Populated by framework from ORG_PROFIT_CENTER_SEQ.';
COMMENT ON COLUMN ORG_PROFIT_CENTER.PROFIT_CENTER_CODE IS 'DBF-0073 — Business code PC-[LE_CODE]-NNNNN. Generated by NumberingEngine. Immutable after first save.';
COMMENT ON COLUMN ORG_PROFIT_CENTER.NAME_AR            IS 'DBF-0074 — Arabic name. Unique within same LegalEntity (RULE-ORG-015).';
COMMENT ON COLUMN ORG_PROFIT_CENTER.NAME_EN            IS 'DBF-0075 — English name. Unique within same LegalEntity (RULE-ORG-015).';
COMMENT ON COLUMN ORG_PROFIT_CENTER.LEGAL_ENTITY_FK    IS 'DBF-0076 — FK to ORG_LEGAL_ENTITY. NOT NULL.';
COMMENT ON COLUMN ORG_PROFIT_CENTER.IS_ACTIVE_FL       IS 'DBF-0077 — 1=Active, 0=Inactive. No internal deactivation constraints (RULE-ORG-002 applies at LegalEntity level).';
COMMENT ON COLUMN ORG_PROFIT_CENTER.NOTES              IS 'DBF-0078 — Optional free-text notes.';
COMMENT ON COLUMN ORG_PROFIT_CENTER.CREATED_BY         IS 'DBF-0079 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_PROFIT_CENTER.CREATED_AT         IS 'DBF-0080 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_PROFIT_CENTER.UPDATED_BY         IS 'DBF-0081 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_PROFIT_CENTER.UPDATED_AT         IS 'DBF-0082 — Audit. Populated by AuditEntityListener.';

-- ORG_LOCATION_SITE
COMMENT ON TABLE ORG_LOCATION_SITE IS 'مواقع العمل — Physical work locations per branch. ENTITY-ORG-007. L1 physical location concept consumed by Inventory (3.2) via HARD-FK.';
COMMENT ON COLUMN ORG_LOCATION_SITE.LOCATION_SITE_PK   IS 'DBF-0083 — PK. Populated by framework from ORG_LOCATION_SITE_SEQ.';
COMMENT ON COLUMN ORG_LOCATION_SITE.LOCATION_SITE_CODE IS 'DBF-0084 — Business code LS-[BR_CODE]-NNNNN. Generated by NumberingEngine. Immutable after first save.';
COMMENT ON COLUMN ORG_LOCATION_SITE.NAME_AR            IS 'DBF-0085 — Arabic name. Unique within same Branch (RULE-ORG-015).';
COMMENT ON COLUMN ORG_LOCATION_SITE.NAME_EN            IS 'DBF-0086 — English name. Unique within same Branch (RULE-ORG-015).';
COMMENT ON COLUMN ORG_LOCATION_SITE.BRANCH_FK          IS 'DBF-0087 — FK to ORG_BRANCH. NOT NULL. Creation blocked if Branch inactive (RULE-ORG-019).';
COMMENT ON COLUMN ORG_LOCATION_SITE.SITE_TYPE_ID       IS 'DBF-0088 — Stores LOV code. lookupKey: LOCATION_SITE_TYPE (LOV-ORG-006). Values: OFFICE, WAREHOUSE, FACTORY, SITE, RETAIL.';
COMMENT ON COLUMN ORG_LOCATION_SITE.IS_ACTIVE_FL       IS 'DBF-0089 — 1=Active, 0=Inactive.';
COMMENT ON COLUMN ORG_LOCATION_SITE.NOTES              IS 'DBF-0090 — Optional free-text notes.';
COMMENT ON COLUMN ORG_LOCATION_SITE.CREATED_BY         IS 'DBF-0091 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_LOCATION_SITE.CREATED_AT         IS 'DBF-0092 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_LOCATION_SITE.UPDATED_BY         IS 'DBF-0093 — Audit. Populated by AuditEntityListener.';
COMMENT ON COLUMN ORG_LOCATION_SITE.UPDATED_AT         IS 'DBF-0094 — Audit. Populated by AuditEntityListener.';

-- ============================================================
-- BLOCK 5: CONSTRAINTS
-- ============================================================

-- 5a: Primary Keys
ALTER TABLE ORG_LEGAL_ENTITY   ADD CONSTRAINT PK_ORG_LEGAL_ENTITY   PRIMARY KEY (LEGAL_ENTITY_PK);
ALTER TABLE ORG_BRANCH         ADD CONSTRAINT PK_ORG_BRANCH         PRIMARY KEY (BRANCH_PK);
ALTER TABLE ORG_REGION_TYPE    ADD CONSTRAINT PK_ORG_REGION_TYPE    PRIMARY KEY (REGION_TYPE_PK);
ALTER TABLE ORG_REGION         ADD CONSTRAINT PK_ORG_REGION         PRIMARY KEY (REGION_PK);
ALTER TABLE ORG_DEPARTMENT     ADD CONSTRAINT PK_ORG_DEPARTMENT     PRIMARY KEY (DEPARTMENT_PK);
ALTER TABLE ORG_COST_CENTER    ADD CONSTRAINT PK_ORG_COST_CENTER    PRIMARY KEY (COST_CENTER_PK);
ALTER TABLE ORG_PROFIT_CENTER  ADD CONSTRAINT PK_ORG_PROFIT_CENTER  PRIMARY KEY (PROFIT_CENTER_PK);
ALTER TABLE ORG_LOCATION_SITE  ADD CONSTRAINT PK_ORG_LOCATION_SITE  PRIMARY KEY (LOCATION_SITE_PK);

-- 5b: Unique Constraints
-- Business codes are globally unique within their scope (RULE-ORG-012)
ALTER TABLE ORG_LEGAL_ENTITY   ADD CONSTRAINT UQ_ORG_LE_CODE        UNIQUE (LEGAL_ENTITY_CODE);
ALTER TABLE ORG_REGION_TYPE    ADD CONSTRAINT UQ_ORG_RT_CODE        UNIQUE (REGION_TYPE_CODE);

-- Branch code unique per LegalEntity
ALTER TABLE ORG_BRANCH         ADD CONSTRAINT UQ_ORG_BR_CODE_LE     UNIQUE (LEGAL_ENTITY_FK, BRANCH_CODE);

-- Region code unique per LegalEntity
ALTER TABLE ORG_REGION         ADD CONSTRAINT UQ_ORG_RG_CODE_LE     UNIQUE (LEGAL_ENTITY_FK, REGION_CODE);

-- Department code unique per Branch
ALTER TABLE ORG_DEPARTMENT     ADD CONSTRAINT UQ_ORG_DEP_CODE_BR    UNIQUE (BRANCH_FK, DEPARTMENT_CODE);

-- Cost center code unique per Branch
ALTER TABLE ORG_COST_CENTER    ADD CONSTRAINT UQ_ORG_CC_CODE_BR     UNIQUE (BRANCH_FK, COST_CENTER_CODE);

-- Profit center code unique per LegalEntity
ALTER TABLE ORG_PROFIT_CENTER  ADD CONSTRAINT UQ_ORG_PC_CODE_LE     UNIQUE (LEGAL_ENTITY_FK, PROFIT_CENTER_CODE);

-- Location site code unique per Branch
ALTER TABLE ORG_LOCATION_SITE  ADD CONSTRAINT UQ_ORG_LS_CODE_BR     UNIQUE (BRANCH_FK, LOCATION_SITE_CODE);

-- 5c: Check Constraints
ALTER TABLE ORG_LEGAL_ENTITY   ADD CONSTRAINT CHK_ORG_LE_ACTIVE     CHECK (IS_ACTIVE_FL IN (0, 1));
ALTER TABLE ORG_BRANCH         ADD CONSTRAINT CHK_ORG_BR_ACTIVE     CHECK (IS_ACTIVE_FL IN (0, 1));
ALTER TABLE ORG_REGION_TYPE    ADD CONSTRAINT CHK_ORG_RT_ACTIVE     CHECK (IS_ACTIVE_FL IN (0, 1));
ALTER TABLE ORG_REGION         ADD CONSTRAINT CHK_ORG_RG_ACTIVE     CHECK (IS_ACTIVE_FL IN (0, 1));
ALTER TABLE ORG_DEPARTMENT     ADD CONSTRAINT CHK_ORG_DEP_ACTIVE    CHECK (IS_ACTIVE_FL IN (0, 1));
ALTER TABLE ORG_DEPARTMENT     ADD CONSTRAINT CHK_ORG_DEP_NODE_TYPE CHECK (NODE_TYPE_ID IN ('SUMMARY', 'DETAIL'));
ALTER TABLE ORG_COST_CENTER    ADD CONSTRAINT CHK_ORG_CC_ACTIVE     CHECK (IS_ACTIVE_FL IN (0, 1));
ALTER TABLE ORG_COST_CENTER    ADD CONSTRAINT CHK_ORG_CC_NODE_TYPE  CHECK (NODE_TYPE_ID IN ('SUMMARY', 'DETAIL'));
ALTER TABLE ORG_COST_CENTER    ADD CONSTRAINT CHK_ORG_CC_TYPE       CHECK (COST_CENTER_TYPE_ID IN ('DIRECT', 'INDIRECT', 'SHARED'));
ALTER TABLE ORG_PROFIT_CENTER  ADD CONSTRAINT CHK_ORG_PC_ACTIVE     CHECK (IS_ACTIVE_FL IN (0, 1));
ALTER TABLE ORG_LOCATION_SITE  ADD CONSTRAINT CHK_ORG_LS_ACTIVE     CHECK (IS_ACTIVE_FL IN (0, 1));
ALTER TABLE ORG_LOCATION_SITE  ADD CONSTRAINT CHK_ORG_LS_SITE_TYPE  CHECK (SITE_TYPE_ID IN ('OFFICE', 'WAREHOUSE', 'FACTORY', 'SITE', 'RETAIL'));

-- 5d: Intra-Module Foreign Keys
-- ORG_BRANCH → ORG_LEGAL_ENTITY (RESTRICT: cannot deactivate LE with active branches — enforced at app layer per RULE-ORG-001)
ALTER TABLE ORG_BRANCH ADD CONSTRAINT FK_ORG_BR_LE
    FOREIGN KEY (LEGAL_ENTITY_FK) REFERENCES ORG_LEGAL_ENTITY (LEGAL_ENTITY_PK);

-- ORG_REGION → ORG_LEGAL_ENTITY
ALTER TABLE ORG_REGION ADD CONSTRAINT FK_ORG_RG_LE
    FOREIGN KEY (LEGAL_ENTITY_FK) REFERENCES ORG_LEGAL_ENTITY (LEGAL_ENTITY_PK);

-- ORG_REGION → ORG_REGION_TYPE
ALTER TABLE ORG_REGION ADD CONSTRAINT FK_ORG_RG_RT
    FOREIGN KEY (REGION_TYPE_ID_FK) REFERENCES ORG_REGION_TYPE (REGION_TYPE_PK);

-- ORG_PROFIT_CENTER → ORG_LEGAL_ENTITY
ALTER TABLE ORG_PROFIT_CENTER ADD CONSTRAINT FK_ORG_PC_LE
    FOREIGN KEY (LEGAL_ENTITY_FK) REFERENCES ORG_LEGAL_ENTITY (LEGAL_ENTITY_PK);

-- ORG_DEPARTMENT → ORG_BRANCH
ALTER TABLE ORG_DEPARTMENT ADD CONSTRAINT FK_ORG_DEP_BR
    FOREIGN KEY (BRANCH_FK) REFERENCES ORG_BRANCH (BRANCH_PK);

-- ORG_DEPARTMENT → ORG_DEPARTMENT (self-reference — NULLABLE)
ALTER TABLE ORG_DEPARTMENT ADD CONSTRAINT FK_ORG_DEP_PARENT
    FOREIGN KEY (PARENT_DEPARTMENT_FK) REFERENCES ORG_DEPARTMENT (DEPARTMENT_PK);

-- ORG_COST_CENTER → ORG_BRANCH
ALTER TABLE ORG_COST_CENTER ADD CONSTRAINT FK_ORG_CC_BR
    FOREIGN KEY (BRANCH_FK) REFERENCES ORG_BRANCH (BRANCH_PK);

-- ORG_COST_CENTER → ORG_COST_CENTER (self-reference — NULLABLE)
ALTER TABLE ORG_COST_CENTER ADD CONSTRAINT FK_ORG_CC_PARENT
    FOREIGN KEY (PARENT_COST_CENTER_FK) REFERENCES ORG_COST_CENTER (COST_CENTER_PK);

-- ORG_LOCATION_SITE → ORG_BRANCH
ALTER TABLE ORG_LOCATION_SITE ADD CONSTRAINT FK_ORG_LS_BR
    FOREIGN KEY (BRANCH_FK) REFERENCES ORG_BRANCH (BRANCH_PK);

-- ============================================================
-- BLOCK 6: TRIGGERS
-- (No audit triggers — AuditEntityListener handles audit fields)
-- (No auto-PK triggers — PK population handled by framework)
-- ============================================================

-- ============================================================
-- BLOCK 7: INDEXES
-- ============================================================

-- ORG_LEGAL_ENTITY
CREATE INDEX IDX_ORG_LE_ENTITY_TYPE  ON ORG_LEGAL_ENTITY  (ENTITY_TYPE_ID);
CREATE INDEX IDX_ORG_LE_IS_ACTIVE    ON ORG_LEGAL_ENTITY  (IS_ACTIVE_FL);

-- ORG_BRANCH
CREATE INDEX IDX_ORG_BR_LE_FK        ON ORG_BRANCH        (LEGAL_ENTITY_FK);
CREATE INDEX IDX_ORG_BR_IS_ACTIVE    ON ORG_BRANCH        (IS_ACTIVE_FL);
CREATE INDEX IDX_ORG_BR_TYPE         ON ORG_BRANCH        (BRANCH_TYPE_ID);

-- ORG_REGION_TYPE
CREATE INDEX IDX_ORG_RT_IS_ACTIVE    ON ORG_REGION_TYPE   (IS_ACTIVE_FL);

-- ORG_REGION
CREATE INDEX IDX_ORG_RG_LE_FK        ON ORG_REGION        (LEGAL_ENTITY_FK);
CREATE INDEX IDX_ORG_RG_RT_FK        ON ORG_REGION        (REGION_TYPE_ID_FK);
CREATE INDEX IDX_ORG_RG_IS_ACTIVE    ON ORG_REGION        (IS_ACTIVE_FL);

-- ORG_DEPARTMENT
CREATE INDEX IDX_ORG_DEP_BR_FK       ON ORG_DEPARTMENT    (BRANCH_FK);
CREATE INDEX IDX_ORG_DEP_PARENT_FK   ON ORG_DEPARTMENT    (PARENT_DEPARTMENT_FK);
CREATE INDEX IDX_ORG_DEP_NODE_TYPE   ON ORG_DEPARTMENT    (NODE_TYPE_ID);
CREATE INDEX IDX_ORG_DEP_IS_ACTIVE   ON ORG_DEPARTMENT    (IS_ACTIVE_FL);

-- ORG_COST_CENTER
CREATE INDEX IDX_ORG_CC_BR_FK        ON ORG_COST_CENTER   (BRANCH_FK);
CREATE INDEX IDX_ORG_CC_PARENT_FK    ON ORG_COST_CENTER   (PARENT_COST_CENTER_FK);
CREATE INDEX IDX_ORG_CC_NODE_TYPE    ON ORG_COST_CENTER   (NODE_TYPE_ID);
CREATE INDEX IDX_ORG_CC_TYPE         ON ORG_COST_CENTER   (COST_CENTER_TYPE_ID);
CREATE INDEX IDX_ORG_CC_IS_ACTIVE    ON ORG_COST_CENTER   (IS_ACTIVE_FL);

-- ORG_PROFIT_CENTER
CREATE INDEX IDX_ORG_PC_LE_FK        ON ORG_PROFIT_CENTER (LEGAL_ENTITY_FK);
CREATE INDEX IDX_ORG_PC_IS_ACTIVE    ON ORG_PROFIT_CENTER (IS_ACTIVE_FL);

-- ORG_LOCATION_SITE
CREATE INDEX IDX_ORG_LS_BR_FK        ON ORG_LOCATION_SITE (BRANCH_FK);
CREATE INDEX IDX_ORG_LS_SITE_TYPE    ON ORG_LOCATION_SITE (SITE_TYPE_ID);
CREATE INDEX IDX_ORG_LS_IS_ACTIVE    ON ORG_LOCATION_SITE (IS_ACTIVE_FL);

-- ============================================================
-- BLOCK 8: LOOKUP SEED DATA
-- LOV-ORG-001 through LOV-ORG-006 → MD_MASTER_LOOKUP + MD_LOOKUP_DETAIL
-- Using actual column names per master-registry Section 4 PERMANENT EXCEPTION:
--   MD_MASTER_LOOKUP : id_pk, lookup_key, lookup_name, lookup_name_en, is_active
--   MD_LOOKUP_DETAIL : id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active
-- Idempotent pattern: INSERT ... WHERE NOT EXISTS
-- ============================================================

-- ──────────────────────────────────────────────────────────
-- LOV-ORG-001 — نوع الكيان القانوني (LEGAL_ENTITY_TYPE)
-- Field: ENTITY-ORG-001.ENTITY_TYPE_ID
-- ──────────────────────────────────────────────────────────
INSERT INTO MD_MASTER_LOOKUP (id_pk, lookup_key, lookup_name, lookup_name_en, is_active, created_by, created_at)
SELECT nextval('MD_MASTER_LOOKUP_SEQ'), 'LEGAL_ENTITY_TYPE', 'نوع الكيان القانوني', 'Legal Entity Type', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_MASTER_LOOKUP WHERE lookup_key = 'LEGAL_ENTITY_TYPE');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'LEGAL_ENTITY_TYPE'),
       'HEAD_OFFICE', 'المقر الرئيسي', 'Head Office', 1, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'LEGAL_ENTITY_TYPE' AND ld.code = 'HEAD_OFFICE');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'LEGAL_ENTITY_TYPE'),
       'BRANCH_OFFICE', 'مكتب فرعي', 'Branch Office', 2, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'LEGAL_ENTITY_TYPE' AND ld.code = 'BRANCH_OFFICE');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'LEGAL_ENTITY_TYPE'),
       'SUBSIDIARY', 'شركة تابعة', 'Subsidiary', 3, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'LEGAL_ENTITY_TYPE' AND ld.code = 'SUBSIDIARY');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'LEGAL_ENTITY_TYPE'),
       'REPRESENTATIVE_OFFICE', 'مكتب تمثيلي', 'Representative Office', 4, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'LEGAL_ENTITY_TYPE' AND ld.code = 'REPRESENTATIVE_OFFICE');

-- ──────────────────────────────────────────────────────────
-- LOV-ORG-002 — نوع الفرع (BRANCH_TYPE)
-- Field: ENTITY-ORG-002.BRANCH_TYPE_ID
-- ──────────────────────────────────────────────────────────
INSERT INTO MD_MASTER_LOOKUP (id_pk, lookup_key, lookup_name, lookup_name_en, is_active, created_by, created_at)
SELECT nextval('MD_MASTER_LOOKUP_SEQ'), 'BRANCH_TYPE', 'نوع الفرع', 'Branch Type', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_MASTER_LOOKUP WHERE lookup_key = 'BRANCH_TYPE');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'BRANCH_TYPE'),
       'MAIN_BRANCH', 'فرع رئيسي', 'Main Branch', 1, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'BRANCH_TYPE' AND ld.code = 'MAIN_BRANCH');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'BRANCH_TYPE'),
       'SUB_BRANCH', 'فرع فرعي', 'Sub-Branch', 2, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'BRANCH_TYPE' AND ld.code = 'SUB_BRANCH');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'BRANCH_TYPE'),
       'OPERATIONS_BRANCH', 'فرع العمليات', 'Operations Branch', 3, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'BRANCH_TYPE' AND ld.code = 'OPERATIONS_BRANCH');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'BRANCH_TYPE'),
       'ADMIN_BRANCH', 'فرع إداري', 'Admin Branch', 4, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'BRANCH_TYPE' AND ld.code = 'ADMIN_BRANCH');

-- ──────────────────────────────────────────────────────────
-- LOV-ORG-003 — نوع عقدة القسم (DEPARTMENT_NODE_TYPE)
-- Field: ENTITY-ORG-004.NODE_TYPE_ID
-- ──────────────────────────────────────────────────────────
INSERT INTO MD_MASTER_LOOKUP (id_pk, lookup_key, lookup_name, lookup_name_en, is_active, created_by, created_at)
SELECT nextval('MD_MASTER_LOOKUP_SEQ'), 'DEPARTMENT_NODE_TYPE', 'نوع عقدة القسم', 'Department Node Type', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_MASTER_LOOKUP WHERE lookup_key = 'DEPARTMENT_NODE_TYPE');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'DEPARTMENT_NODE_TYPE'),
       'SUMMARY', 'ملخص', 'Summary', 1, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'DEPARTMENT_NODE_TYPE' AND ld.code = 'SUMMARY');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'DEPARTMENT_NODE_TYPE'),
       'DETAIL', 'تفصيل', 'Detail', 2, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'DEPARTMENT_NODE_TYPE' AND ld.code = 'DETAIL');

-- ──────────────────────────────────────────────────────────
-- LOV-ORG-004 — نوع عقدة مركز التكلفة (COST_CENTER_NODE_TYPE)
-- Field: ENTITY-ORG-005.NODE_TYPE_ID
-- ──────────────────────────────────────────────────────────
INSERT INTO MD_MASTER_LOOKUP (id_pk, lookup_key, lookup_name, lookup_name_en, is_active, created_by, created_at)
SELECT nextval('MD_MASTER_LOOKUP_SEQ'), 'COST_CENTER_NODE_TYPE', 'نوع عقدة مركز التكلفة', 'Cost Center Node Type', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_MASTER_LOOKUP WHERE lookup_key = 'COST_CENTER_NODE_TYPE');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'COST_CENTER_NODE_TYPE'),
       'SUMMARY', 'ملخص', 'Summary', 1, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'COST_CENTER_NODE_TYPE' AND ld.code = 'SUMMARY');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'COST_CENTER_NODE_TYPE'),
       'DETAIL', 'تفصيل', 'Detail', 2, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'COST_CENTER_NODE_TYPE' AND ld.code = 'DETAIL');

-- ──────────────────────────────────────────────────────────
-- LOV-ORG-005 — نوع مركز التكلفة (COST_CENTER_TYPE)
-- Field: ENTITY-ORG-005.COST_CENTER_TYPE_ID
-- ──────────────────────────────────────────────────────────
INSERT INTO MD_MASTER_LOOKUP (id_pk, lookup_key, lookup_name, lookup_name_en, is_active, created_by, created_at)
SELECT nextval('MD_MASTER_LOOKUP_SEQ'), 'COST_CENTER_TYPE', 'نوع مركز التكلفة', 'Cost Center Type', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_MASTER_LOOKUP WHERE lookup_key = 'COST_CENTER_TYPE');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'COST_CENTER_TYPE'),
       'DIRECT', 'مباشر', 'Direct', 1, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'COST_CENTER_TYPE' AND ld.code = 'DIRECT');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'COST_CENTER_TYPE'),
       'INDIRECT', 'غير مباشر', 'Indirect', 2, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'COST_CENTER_TYPE' AND ld.code = 'INDIRECT');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'COST_CENTER_TYPE'),
       'SHARED', 'مشترك', 'Shared', 3, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'COST_CENTER_TYPE' AND ld.code = 'SHARED');

-- ──────────────────────────────────────────────────────────
-- LOV-ORG-006 — نوع موقع العمل (LOCATION_SITE_TYPE)
-- Field: ENTITY-ORG-007.SITE_TYPE_ID
-- ──────────────────────────────────────────────────────────
INSERT INTO MD_MASTER_LOOKUP (id_pk, lookup_key, lookup_name, lookup_name_en, is_active, created_by, created_at)
SELECT nextval('MD_MASTER_LOOKUP_SEQ'), 'LOCATION_SITE_TYPE', 'نوع موقع العمل', 'Location Site Type', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_MASTER_LOOKUP WHERE lookup_key = 'LOCATION_SITE_TYPE');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'LOCATION_SITE_TYPE'),
       'OFFICE', 'مكتب', 'Office', 1, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'LOCATION_SITE_TYPE' AND ld.code = 'OFFICE');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'LOCATION_SITE_TYPE'),
       'WAREHOUSE', 'مستودع', 'Warehouse', 2, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'LOCATION_SITE_TYPE' AND ld.code = 'WAREHOUSE');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'LOCATION_SITE_TYPE'),
       'FACTORY', 'مصنع', 'Factory', 3, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'LOCATION_SITE_TYPE' AND ld.code = 'FACTORY');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'LOCATION_SITE_TYPE'),
       'SITE', 'موقع', 'Site', 4, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'LOCATION_SITE_TYPE' AND ld.code = 'SITE');

INSERT INTO MD_LOOKUP_DETAIL (id_pk, master_lookup_id_fk, code, name_ar, name_en, sort_order, is_active, created_by, created_at)
SELECT nextval('MD_LOOKUP_DETAIL_SEQ'),
       (SELECT id_pk FROM MD_MASTER_LOOKUP WHERE lookup_key = 'LOCATION_SITE_TYPE'),
       'RETAIL', 'متجر', 'Retail', 5, 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM MD_LOOKUP_DETAIL ld
                  JOIN MD_MASTER_LOOKUP ml ON ld.master_lookup_id_fk = ml.id_pk
                  WHERE ml.lookup_key = 'LOCATION_SITE_TYPE' AND ld.code = 'RETAIL');

-- ──────────────────────────────────────────────────────────
-- ORG_REGION_TYPE — Seed Data (initial values)
-- ENTITY-ORG-008 — Reference Table (not MD_LOOKUP_DETAIL)
-- source: master-registry Section 5 (GEOGRAPHIC / SALES / OPERATIONAL)
-- ──────────────────────────────────────────────────────────
INSERT INTO ORG_REGION_TYPE (REGION_TYPE_PK, REGION_TYPE_CODE, NAME_AR, NAME_EN, IS_ACTIVE_FL, CREATED_BY, CREATED_AT)
SELECT nextval('ORG_REGION_TYPE_SEQ'), 'GEOGRAPHIC', 'جغرافية', 'Geographic', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ORG_REGION_TYPE WHERE REGION_TYPE_CODE = 'GEOGRAPHIC');

INSERT INTO ORG_REGION_TYPE (REGION_TYPE_PK, REGION_TYPE_CODE, NAME_AR, NAME_EN, IS_ACTIVE_FL, CREATED_BY, CREATED_AT)
SELECT nextval('ORG_REGION_TYPE_SEQ'), 'SALES', 'مبيعاتية', 'Sales', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ORG_REGION_TYPE WHERE REGION_TYPE_CODE = 'SALES');

INSERT INTO ORG_REGION_TYPE (REGION_TYPE_PK, REGION_TYPE_CODE, NAME_AR, NAME_EN, IS_ACTIVE_FL, CREATED_BY, CREATED_AT)
SELECT nextval('ORG_REGION_TYPE_SEQ'), 'OPERATIONAL', 'تشغيلية', 'Operational', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM ORG_REGION_TYPE WHERE REGION_TYPE_CODE = 'OPERATIONAL');

-- ──────────────────────────────────────────────────────────
-- SEC_PAGES seed — Organization Module (7 screens)
-- Source: SRS Permissions Summary + security-registry.md Section 1.3
-- SEC_PAGES actual column names (PERMANENT EXCEPTION — AS-IS):
--   ID_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, MODULE, IS_ACTIVE, CREATED_BY, CREATED_AT
-- PK from SEC_PAGES_SEQ
-- Security Engine auto-generates 4 PERMISSIONS per page (PERM_*_VIEW/CREATE/UPDATE/DELETE)
-- ──────────────────────────────────────────────────────────

INSERT INTO SEC_PAGES (ID_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, MODULE, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('SEC_PAGES_SEQ'), 'LEGAL_ENTITY', 'الكيانات القانونية', 'Legal Entities',
       '/org/legal-entities', 'ORGANIZATION', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM SEC_PAGES WHERE PAGE_CODE = 'LEGAL_ENTITY');

INSERT INTO SEC_PAGES (ID_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, MODULE, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('SEC_PAGES_SEQ'), 'BRANCH', 'الفروع', 'Branches',
       '/org/branches', 'ORGANIZATION', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM SEC_PAGES WHERE PAGE_CODE = 'BRANCH');

INSERT INTO SEC_PAGES (ID_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, MODULE, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('SEC_PAGES_SEQ'), 'REGION', 'المناطق', 'Regions',
       '/org/regions', 'ORGANIZATION', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM SEC_PAGES WHERE PAGE_CODE = 'REGION');

INSERT INTO SEC_PAGES (ID_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, MODULE, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('SEC_PAGES_SEQ'), 'DEPARTMENT', 'الأقسام', 'Departments',
       '/org/departments', 'ORGANIZATION', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM SEC_PAGES WHERE PAGE_CODE = 'DEPARTMENT');

INSERT INTO SEC_PAGES (ID_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, MODULE, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('SEC_PAGES_SEQ'), 'COST_CENTER', 'مراكز التكلفة', 'Cost Centers',
       '/org/cost-centers', 'ORGANIZATION', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM SEC_PAGES WHERE PAGE_CODE = 'COST_CENTER');

INSERT INTO SEC_PAGES (ID_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, MODULE, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('SEC_PAGES_SEQ'), 'PROFIT_CENTER', 'مراكز الربح', 'Profit Centers',
       '/org/profit-centers', 'ORGANIZATION', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM SEC_PAGES WHERE PAGE_CODE = 'PROFIT_CENTER');

INSERT INTO SEC_PAGES (ID_PK, PAGE_CODE, NAME_AR, NAME_EN, ROUTE, MODULE, IS_ACTIVE, CREATED_BY, CREATED_AT)
SELECT nextval('SEC_PAGES_SEQ'), 'LOCATION_SITE', 'مواقع العمل', 'Location Sites',
       '/org/location-sites', 'ORGANIZATION', 1, 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM SEC_PAGES WHERE PAGE_CODE = 'LOCATION_SITE');

-- ──────────────────────────────────────────────────────────
-- PERMISSIONS seed — Organization Module (7 pages × 4 perms = 28 rows)
-- Source: security-registry.md Section 1.4 — PERMISSIONS table
-- PERMISSIONS actual columns (PERMANENT EXCEPTION):
--   ID (BIGINT GENERATED ALWAYS AS IDENTITY), NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT
-- NOTE: PERMISSIONS.ID uses GENERATED ALWAYS AS IDENTITY — no manual PK insert
-- NOTE: PAGE_ID_FK resolved via SEC_PAGES.ID_PK lookup by PAGE_CODE
-- Security Engine auto-generates on page creation; this block covers manual seeding / idempotent runs
-- ──────────────────────────────────────────────────────────

-- LEGAL_ENTITY permissions
INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_LEGAL_ENTITY_VIEW', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'LEGAL_ENTITY'), 'VIEW', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_LEGAL_ENTITY_VIEW');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_LEGAL_ENTITY_CREATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'LEGAL_ENTITY'), 'CREATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_LEGAL_ENTITY_CREATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_LEGAL_ENTITY_UPDATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'LEGAL_ENTITY'), 'UPDATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_LEGAL_ENTITY_UPDATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_LEGAL_ENTITY_DELETE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'LEGAL_ENTITY'), 'DELETE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_LEGAL_ENTITY_DELETE');

-- BRANCH permissions
INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_BRANCH_VIEW', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'BRANCH'), 'VIEW', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_BRANCH_VIEW');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_BRANCH_CREATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'BRANCH'), 'CREATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_BRANCH_CREATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_BRANCH_UPDATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'BRANCH'), 'UPDATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_BRANCH_UPDATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_BRANCH_DELETE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'BRANCH'), 'DELETE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_BRANCH_DELETE');

-- REGION permissions
INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_REGION_VIEW', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'REGION'), 'VIEW', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_REGION_VIEW');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_REGION_CREATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'REGION'), 'CREATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_REGION_CREATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_REGION_UPDATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'REGION'), 'UPDATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_REGION_UPDATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_REGION_DELETE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'REGION'), 'DELETE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_REGION_DELETE');

-- DEPARTMENT permissions
INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_DEPARTMENT_VIEW', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'DEPARTMENT'), 'VIEW', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_DEPARTMENT_VIEW');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_DEPARTMENT_CREATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'DEPARTMENT'), 'CREATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_DEPARTMENT_CREATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_DEPARTMENT_UPDATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'DEPARTMENT'), 'UPDATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_DEPARTMENT_UPDATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_DEPARTMENT_DELETE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'DEPARTMENT'), 'DELETE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_DEPARTMENT_DELETE');

-- COST_CENTER permissions
INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_COST_CENTER_VIEW', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'COST_CENTER'), 'VIEW', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_COST_CENTER_VIEW');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_COST_CENTER_CREATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'COST_CENTER'), 'CREATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_COST_CENTER_CREATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_COST_CENTER_UPDATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'COST_CENTER'), 'UPDATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_COST_CENTER_UPDATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_COST_CENTER_DELETE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'COST_CENTER'), 'DELETE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_COST_CENTER_DELETE');

-- PROFIT_CENTER permissions
INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_PROFIT_CENTER_VIEW', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'PROFIT_CENTER'), 'VIEW', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_PROFIT_CENTER_VIEW');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_PROFIT_CENTER_CREATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'PROFIT_CENTER'), 'CREATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_PROFIT_CENTER_CREATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_PROFIT_CENTER_UPDATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'PROFIT_CENTER'), 'UPDATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_PROFIT_CENTER_UPDATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_PROFIT_CENTER_DELETE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'PROFIT_CENTER'), 'DELETE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_PROFIT_CENTER_DELETE');

-- LOCATION_SITE permissions
INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_LOCATION_SITE_VIEW', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'LOCATION_SITE'), 'VIEW', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_LOCATION_SITE_VIEW');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_LOCATION_SITE_CREATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'LOCATION_SITE'), 'CREATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_LOCATION_SITE_CREATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_LOCATION_SITE_UPDATE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'LOCATION_SITE'), 'UPDATE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_LOCATION_SITE_UPDATE');

INSERT INTO PERMISSIONS (NAME, PAGE_ID_FK, PERMISSION_TYPE, CREATED_BY, CREATED_AT)
SELECT 'PERM_LOCATION_SITE_DELETE', (SELECT ID_PK FROM SEC_PAGES WHERE PAGE_CODE = 'LOCATION_SITE'), 'DELETE', 'SYSTEM', NOW()
WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS WHERE NAME = 'PERM_LOCATION_SITE_DELETE');

COMMIT;

-- ============================================================
-- BLOCK 9: VIEWS
-- (None required for this module)
-- ============================================================

-- ============================================================
-- BLOCK 10: FUNCTIONS AND PROCEDURES
-- (None required for this module)
-- ============================================================

-- ============================================================
-- BLOCK 11: DEFERRED FK PATCH BLOCKS
-- (None — ROOT MODULE, zero outbound cross-module dependencies)
-- All FKs in this module are INTRA-MODULE — applied in BLOCK 5d above.
-- ============================================================

-- ============================================================
-- END OF ORGANIZATION MODULE DATABASE SCRIPT
-- DBS-ID: DBS-ORG-001 | DB_TARGET: POSTGRESQL_16
-- 8 sequences | 8 tables | 94 DBF-IDs | 0 XM-IDs
-- 9 intra-module FKs | 26 indexes | 6 LOV seed sets | 3 RegionType seeds
-- 7 SEC_PAGES rows | 28 PERMISSIONS rows
-- ============================================================
```

---

## DB REGISTRY UPDATE — 2026-06-28

```
## REGISTRY UPDATE — 2026-06-28
────────────────────────────────────────────────────────────────
Source Mode    : MODE 1.5
Feature Code   : ORG-001
DBS-ID         : DBS-ORG-001
DB_TARGET      : POSTGRESQL_16
Plan ID        : —
────────────────────────────────────────────────────────────────
New Tables     : ORG_LEGAL_ENTITY, ORG_BRANCH, ORG_REGION_TYPE,
                 ORG_REGION, ORG_DEPARTMENT, ORG_COST_CENTER,
                 ORG_PROFIT_CENTER, ORG_LOCATION_SITE
New Lookups    : LEGAL_ENTITY_TYPE, BRANCH_TYPE, DEPARTMENT_NODE_TYPE,
                 COST_CENTER_NODE_TYPE, COST_CENTER_TYPE, LOCATION_SITE_TYPE
DBF-IDs        : DBF-0001 through DBF-0094 (94 total)
XM-IDs Open   : None — ROOT MODULE — zero outbound XM dependencies
OQ-IDs Open   : OQ-001 (DEFERRED — non-blocking)
Gate Status    : PASSED ✓
Next Action    : Upload srs-org-001.md + dbs-org-001.md to Project 3
                 → Trigger MODE 2 — Execution Plan Governance Engine
────────────────────────────────────────────────────────────────
Registry cascade rule (REG-3):
  DBS-ID confirmed for Organization (ROOT MODULE).
  No DEFERRED XM-IDs in Global XM Dependency Index target Organization
  at this stage (consumer modules not yet gated).
  Cascade evaluation: N/A — no consumers have reached MODE 1.5 yet.
────────────────────────────────────────────────────────────────
```

---

## MODULE GOVERNANCE INDEX — Organization (ORG-001)

```
MODULE          : Organization
FEATURE-CODE    : ORG-001
DBS-ID          : DBS-ORG-001
PLAN-ID         : PLAN-ORG-001 (GOVERNED ✓ — ALIGN GATE PASSED ✓)
GOVERNANCE-STATE: FULL
LAST-VERIFIED   : 2026-06-28
VERIFIED-BY     : MODE 1.5 (DB Script generation — POSTGRESQL_16)

PIPELINE STATUS:
  MODE 0   : ✓ COMPLETE
  MODE 1   : ✓ COMPLETE — srs-org-001.md v1.0 GOVERNED ✓
  MODE 1.5 : ✓ COMPLETE — dbs-org-001.md GOVERNED ✓ (POSTGRESQL_16)
  MODE 2   : ✓ COMPLETE — PLAN-ORG-001 ALIGN GATE PASSED ✓
  MODE 4A  : ⏳ PENDING — Pre-flight audit not yet executed
  MODE 3   : ⏳ PENDING — Awaiting MODE 4A clearance

ATTACHED ARTIFACTS:
  srs-org-001.md        : GOVERNED ✓ v1.0
  dbs-org-001.md        : GOVERNED ✓ (this document — POSTGRESQL_16)
  execution-plan.md     : PLAN-ORG-001 GOVERNED ✓ ALIGN GATE PASSED ✓
  test-plan.md          : NOT YET GENERATED

OPEN ITEMS:
  OQ-001   : DEFERRED (non-blocking) — Region deactivation / SOFT-READ consumer impact
  XM-IDs   : None
  Findings : None (MODE 4A not yet executed)

EXECUTION READINESS: READY for MODE 4A
  Next safe action: Upload srs-org-001.md + dbs-org-001.md + execution-plan
                    to Project 4 → trigger MODE 4A pre-flight audit

NOTES:
  - ROOT MODULE: zero outbound cross-module dependencies
  - DB_TARGET migrated from ORACLE_19C to POSTGRESQL_16 (2026-06-28)
  - All 94 DBF-IDs confirmed in POSTGRESQL_16 syntax
  - TENANT_ID: removed system-wide (Conflict #17 CLOSED 2026-06-21)
  - ORG_REGION_TYPE: PRIVATE Reference Table (> 15 values — not MD_LOOKUP_DETAIL)
  - Seed data: idempotent INSERT ... WHERE NOT EXISTS pattern throughout
  - PERMISSIONS.ID: uses GENERATED ALWAYS AS IDENTITY (AS-IS PERMANENT EXCEPTION)
    — seed inserts omit ID column per security-registry.md Section 8 note
```

---

*End of dbs-org-001.md*
*DBS-ID: DBS-ORG-001 | DB_TARGET: POSTGRESQL_16 | Feature Code: ORG-001*
*8 tables | 94 DBF-IDs | 0 XM-IDs | 6 LOVs + 1 Reference Table*
*Governed by: Database Governance Engine (Project 2)*
*SRS Source: srs-org-001.md v1.0 GOVERNED ✓*
