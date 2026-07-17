# MASTER-REGISTRY.md

=====================================================
1. Project Information
=====================================================

- Project Name  : Enterprise Engine Platform
- Version       : 2.10.0
- Last Updated  : 2026-07-14
- Maintained By : System Architect
- Governance Level: LOCKED

LOCKED Definition (AI Instruction):
This registry is the single source of truth for the entire platform.
LOCKED means:
- No entity, lookup, or rule may be overridden or ignored
- Any conflict MUST be flagged and stopped immediately
- No new entity may be created without checking this registry first
- Any structural change requires a version increment and architect approval

Platform Vision:
- Build reusable enterprise capabilities once — reuse everywhere
- Focus on what is common across 70%+ of projects
- Five layers: Foundation → Engines → Operations → Reporting → Applications
- Every module follows the same internal pattern: Infrastructure → Domain → Application → Interface

Platform Implementation Policy:
Any module or feature that was implemented before this registry was established
is treated as a PERMANENT EXCEPTION — used AS-IS with no code changes required.
This applies to: Security module / MasterData Lookup feature.
Future modules and features MUST follow registry standards without exception.

=====================================================
2. Platform Layer Structure
=====================================================

LAYER-1 : Foundation          — الأساس المؤسسي
LAYER-2 : Smart Engines       — المحركات الذكية
LAYER-3 : Operational Modules — الوحدات التشغيلية
LAYER-4 : Reporting           — التحليل والتقارير
LAYER-5 : Applications        — التطبيقات

Rule: No module may depend on a module in a higher layer number.
Rule: Layer-1 modules are built first — they block everything else.
Rule: Dependency arrows always point downward (consumer → provider).

=====================================================
3. Modules Registry
=====================================================

-----------------------------------------------------
LAYER-1 — Foundation
-----------------------------------------------------

| #    | Module Name          | Type   | Shared | Status                  | Depends On              |
|------|----------------------|--------|--------|-------------------------|-------------------------|
| 1.1  | Organization         | Core   | Yes    | GOVERNED ✓ MODE 2       | —                       |
| 1.2  | Security             | Core   | Yes    | Active ⚠️ EXCEPTION      | 1.1                     |
| 1.3  | UserManagement       | Core   | Yes    | Merged → Security ⚠️ EXCEPTION                          |
| 1.4  | MasterData           | Core   | Yes    | Partial Active ⚠️        | 1.1 / 1.2               |
| 1.5  | CurrencyCalendar     | Core   | Yes    | Planned                 | 1.1 / 1.2               |
| 1.6  | NumberingEngine      | Core   | Yes    | Planned                 | 1.1 / 1.2 / 1.4         |
| 1.7  | IntegrationService   | Core   | Yes    | Planned                 | 1.1 / 1.2               |
| 1.8  | NotificationService  | Core   | Yes    | Active ⚠️ SVCAPI ✓      | 1.1 / 1.2 / 1.10        |
| 1.9  | AuditService         | Core   | Yes    | Planned                 | 1.1 / 1.2 / 1.10        |
| 1.10 | FileService          | Core   | Yes    | Active ⚠️ SVCAPI ✓      | 1.1 / 1.2               |

Organization Governance Note:
  GOVERNED ✓ MODE 2 (ALIGN GATE PASSED ✓) means:
  - SRS complete (srs-org-001.md v1.0) — 8 entities / 20 rules / 47 APIs / 7 screens
  - DB script complete (DBS-ORG-001) — 8 tables / 102 DBF-IDs
  - Execution plan complete (PLAN-ORG-001) — ALIGN GATE PASSED ✓
  - Next stage: MODE 2.5 → test-plan-org-001.md
  - ROOT MODULE — zero outbound XM dependencies

MasterData Status Note:
  Partial Active ⚠️ means:
  - Lookup feature (MD_MASTER_LOOKUP + MD_LOOKUP_DETAIL) → ACTIVE ✅ PERMANENT EXCEPTION
  - Item / Customer / Vendor / UnitOfMeasure / Country      → PLANNED (must follow standard)

FileService / NotificationService Status Note (added 2026-07-14):
  Active ⚠️ SVCAPI ✓ means:
  - Backend fully implemented and live-tested end-to-end against this dev
    environment — real Java services, DB migrations applied (V3 for File,
    V5 for Notification), all governed API-IDs reachable, permissions
    seeded and granted to SUPER_ADMIN.
  - DOC / INT-C / INT-R / F1-F3 (frontend) / SEC (full multi-role
    permission matrix) / ALIGN phases are still PENDING per each module's
    own execution-state.json — current_phase is deliberately left at DOC,
    not advanced, until those phases actually run.
  - Safe for other backend modules to integrate against today — see
    Section 8's FileService/NotificationService Consumption Rules for the
    actual verified endpoints, auth requirements, and integration
    patterns. No Angular/Flutter UI exists for either module yet.

LAYER-1 Internal Build Sequence:

| Step  | Modules (parallel within step)                                       | Prerequisite     |
|-------|----------------------------------------------------------------------|------------------|
| L1-1  | Organization                                                         | None             |
| L1-2  | Security                                                             | L1-1 complete    |
| L1-3  | MasterData, CurrencyCalendar, IntegrationService, FileService        | L1-2 complete    |
| L1-4  | NumberingEngine, NotificationService, AuditService                   | L1-3 complete    |

Note: NumberingEngine requires MasterData (L1-3).
Note: NotificationService and AuditService require FileService (L1-3).

-----------------------------------------------------
LAYER-2 — Smart Engines
-----------------------------------------------------

| #    | Module Name          | Type   | Shared | Status  | Depends On                        |
|------|----------------------|--------|--------|---------|-----------------------------------|
| 2.1  | PricingEngine        | Engine | Yes    | Planned | Layer-1 complete / 1.4 / 1.5      |
| 2.2  | TaxEngine            | Engine | Yes    | Planned | Layer-1 complete / 2.1            |
| 2.3  | CalculationEngine    | Engine | Yes    | Planned | Layer-1 complete / 2.1 / 2.2      |

LAYER-2 Internal Build Sequence:

| Step  | Module              | Prerequisite                |
|-------|---------------------|-----------------------------|
| L2-1  | PricingEngine       | Layer-1 fully complete      |
| L2-2  | TaxEngine           | L2-1 complete               |
| L2-3  | CalculationEngine   | L2-2 complete               |

-----------------------------------------------------
LAYER-3 — Operational Modules
-----------------------------------------------------

| #    | Module Name          | Type        | Shared | Status  | Depends On                                         |
|------|----------------------|-------------|--------|---------|----------------------------------------------------|
| 3.1  | Procurement          | Operational | No     | Planned | Layer-1 complete / Layer-2 complete                |
| 3.2  | Inventory            | Operational | No     | Planned | Layer-1 complete / Layer-2 complete / 3.1          |
| 3.3  | Sales                | Operational | No     | Planned | Layer-1 complete / Layer-2 complete / 3.2          |
| 3.4  | Finance              | Operational | No     | Planned | Layer-1 complete / Layer-2 complete / 3.1/3.2/3.3  |

LAYER-3 Internal Build Sequence:

| Step  | Module              | Prerequisite                            |
|-------|---------------------|-----------------------------------------|
| L3-1  | Procurement         | Layers 1 & 2 fully complete             |
| L3-2  | Inventory           | L3-1 complete                           |
| L3-3  | Sales               | L3-2 complete                           |
| L3-4  | Finance             | L3-1 + L3-2 + L3-3 complete            |

Note: Finance has the broadest dependency footprint in the platform.
Note: Finance is the last Layer-3 module buildable.
Note: Nothing in Layer-3 depends on Finance — it is the final consumer.

-----------------------------------------------------
LAYER-4 — Reporting
-----------------------------------------------------

| #    | Module Name          | Type      | Shared | Status  | Depends On               |
|------|----------------------|-----------|--------|---------|--------------------------|
| 4.1  | Reporting            | Reporting | Yes    | Planned | All Layers 1 / 2 / 3     |

Rule: Reporting reads from all layers. It NEVER writes to any module's data.

-----------------------------------------------------
LAYER-5 — Applications
-----------------------------------------------------

| #    | Module Name          | Type        | Shared | Status  | Depends On                   |
|------|----------------------|-------------|--------|---------|------------------------------|
| 5.1  | CommercialERP        | Application | No     | Planned | All Layers                   |
| 5.2  | IndustrialERP        | Application | No     | Planned | All Layers + Extensions      |
| 5.3  | ContractingERP       | Application | No     | Planned | All Layers + Extensions      |
| 5.4  | CustomApplication    | Application | No     | Planned | All Layers + Custom          |

-----------------------------------------------------
Module Type Definitions
-----------------------------------------------------

- Core        : Foundational — used by all modules
- Engine      : Reusable business logic — used by operational modules
- Operational : Day-to-day business operations
- Reporting   : Reads from all — writes to none
- Application : Assembles capabilities into a business solution
- Extension   : Industry-specific additions (defined per project)

=====================================================
4. Naming & Data Governance Rules
=====================================================

Primary Keys     : MUST end with "Pk"
Foreign Keys     : MUST end with "Fk"
Dropdown fields  : MUST end with "Id"
Flag fields      : MUST end with "Fl"

All entities MUST include:
  - createdAt
  - createdBy
  - updatedAt
  - updatedBy
  - isActiveFl OR statusId

No deviation from naming conventions is allowed.
Any violation MUST be flagged before SRS phase.

-----------------------------------------------------
PERMANENT EXCEPTION — Security Module
-----------------------------------------------------

Status      : PERMANENT EXCEPTION — used AS-IS — no code changes — ever.
Reason      : Module was implemented before this registry was established.
Scope       : All Security entities and their column names.

Security Module actual naming (for reference only — NOT a template):

| Convention      | Standard                | Security Actual                                 |
|-----------------|-------------------------|-------------------------------------------------|
| Primary Key     | ends with Pk            | USERS_PK, ROLES_PK, PERMISSIONS_PK, REFRESH_TOKENS_PK ✅ |
| Primary Key     | ends with Pk            | SEC_PAGES_PK ✅ |
| Foreign Key     | ends with Fk            | USER_ID_FK, ROLE_ID_FK, PERM_ID_FK (join tables) ✅ |
| Foreign Key     | ends with Fk            | PAGE_ID_FK, PARENT_ID_FK ✅                     |
| Flag Field      | ends with Fl            | ENABLED, IS_ACTIVE, REVOKED                     |
| Dropdown Field  | ends with Id            | PERMISSION_TYPE (stored as VARCHAR Enum)        |

`Role.roleCode` and `Role.description` are persisted columns (`ROLE_CODE`,
`DESCRIPTION` on `ROLES`).

Additional Security implementation status (non-naming, informational —
sourced from registry-security.md v2.4.0, does not require an exception
amendment since no naming/table structure is affected):
  - Login rate limiting (`LoginRateLimitFilter`) — IMPLEMENTED
  - Expired/revoked refresh-token cleanup job (`RefreshTokenCleanupJob`) — IMPLEMENTED
  - Role copy-permissions endpoint (`POST /api/roles/{roleId}/copy-from/{sourceRoleId}`) — IMPLEMENTED
| DataScope       | Separate entity         | System is permanently single-tenant — no TENANT_ID column exists. |
| UserManagement  | Separate module         | Merged into Security — no split will occur      |

Consumption Rule:
Any module referencing Security entities MUST use the actual column names above.
Standard convention names MUST NOT be assumed.

-----------------------------------------------------
PERMANENT EXCEPTION — MasterData Lookup Feature
-----------------------------------------------------

Status      : PERMANENT EXCEPTION — used AS-IS — no code changes — ever.
Reason      : Feature was implemented before this registry was established.
Scope       : MD_MASTER_LOOKUP and MD_LOOKUP_DETAIL tables and all their columns.
Future      : All other MasterData entities (Item, Customer, Vendor, etc.)
              MUST follow standard naming conventions — no exception.

MasterData Lookup actual naming (for reference only — NOT a template):

| Convention      | Standard          | MasterData Lookup Actual              | Table              |
|-----------------|-------------------|---------------------------------------|--------------------|
| Primary Key     | ends with Pk      | id_pk                                 | MD_MASTER_LOOKUP   |
| Primary Key     | ends with Pk      | id_pk                                 | MD_LOOKUP_DETAIL   |
| Foreign Key     | ends with Fk      | master_lookup_id_fk                   | MD_LOOKUP_DETAIL   |
| Business Code   | lookupCode        | lookup_key  (UPPERCASE, immutable)    | MD_MASTER_LOOKUP   |
| Business Code   | detailCode        | code       (unique per parent, immutable) | MD_LOOKUP_DETAIL|
| Value Arabic    | detailValueAr     | name_ar                               | MD_LOOKUP_DETAIL   |
| Value English   | detailValueEn     | name_en                               | MD_LOOKUP_DETAIL   |
| Flag Field      | isActiveFl        | is_active  (SMALLINT 0/1)             | Both               |
| System Flag     | isSystemFl        | NOT IMPLEMENTED — no plan to add      | Both               |
| Extra Field     | —                 | extra_value (flexible business value) | MD_LOOKUP_DETAIL   |
| Derived Field   | —                 | NOT IN DB — removed in PG migration   | MD_MASTER_LOOKUP   |

Consumption Rule:
Any module consuming MD_MASTER_LOOKUP or MD_LOOKUP_DETAIL MUST use
the actual column names above. Standard convention names MUST NOT be assumed.
All modules MUST consume lookup values via the API only:
  GET /api/lookups/{lookupKey}?active=true
Modules MUST NOT query MD_MASTER_LOOKUP or MD_LOOKUP_DETAIL directly.
lookup_key is the stable contract — id_pk is internal and MUST NOT be exposed.

=====================================================
5. Shared Core Entities
=====================================================

-----------------------------------------------------
LOOKUP ENTITIES — Cross-Platform (Used by All Modules)
-----------------------------------------------------

| Table Name        | Purpose                                           | Owner Module | Status              |
|-------------------|---------------------------------------------------|--------------|---------------------|
| MD_MASTER_LOOKUP  | Defines lookup categories (e.g. Status, ItemType) | MasterData   | ACTIVE ✅ EXCEPTION |
| MD_LOOKUP_DETAIL  | Stores the values for each lookup category        | MasterData   | ACTIVE ✅ EXCEPTION |

-----------------------------------------------------
MD_MASTER_LOOKUP — Actual Columns (PERMANENT EXCEPTION)
-----------------------------------------------------

| Actual Field Name   | Type          | Required | Notes                                              |
|---------------------|---------------|----------|----------------------------------------------------|
| id_pk               | BIGINT        | Yes      | Auto-generated PK                                  |
| lookup_key          | VARCHAR(50)   | Yes      | Unique business code. UPPERCASE. Immutable.        |
| lookup_name         | VARCHAR(200)  | Yes      | Arabic display name                                |
| lookup_name_en      | VARCHAR(200)  | No       | English display name                               |
| description         | VARCHAR(500)  | No       | Functional description                             |
| is_active           | SMALLINT(0/1) | Yes      | Active / Inactive state                            |
| created_at          | TIMESTAMP     | Yes      | Audit field                                        |
| created_by          | VARCHAR(100)  | Yes      | Audit field                                        |
| updated_at          | TIMESTAMP     | No       | Audit field                                        |
| updated_by          | VARCHAR(100)  | No       | Audit field                                        |

Business Rules on MD_MASTER_LOOKUP:
- lookup_key MUST be unique across all records
- lookup_key MUST be stored as UPPERCASE always
- lookup_key MUST NOT be modified after creation
- A lookup type MUST NOT be deactivated if it has active detail values
- A lookup type MUST NOT be deleted if it has any detail values

-----------------------------------------------------
MD_LOOKUP_DETAIL — Actual Columns (PERMANENT EXCEPTION)
-----------------------------------------------------

| Actual Field Name   | Type          | Required | Notes                                                        |
|---------------------|---------------|----------|--------------------------------------------------------------|
| id_pk               | BIGINT        | Yes      | Auto-generated PK                                            |
| master_lookup_id_fk | BIGINT        | Yes      | FK → MD_MASTER_LOOKUP(id_pk). Immutable after creation.      |
| code                | VARCHAR(50)   | Yes      | Unique per parent lookup. Immutable after creation.          |
| name_ar             | VARCHAR(200)  | Yes      | Arabic display value                                         |
| name_en             | VARCHAR(200)  | No       | English display value                                        |
| extra_value         | VARCHAR(255)  | No       | Flexible business value field. Used as-is.                   |
| sort_order          | INTEGER       | No       | Display order in dropdowns. Default: 0. Min: 0.              |
| is_active           | SMALLINT(0/1) | Yes      | Active / Inactive state                                      |
| created_at          | TIMESTAMP     | Yes      | Audit field                                                  |
| created_by          | VARCHAR(100)  | Yes      | Audit field                                                  |
| updated_at          | TIMESTAMP     | No       | Audit field                                                  |
| updated_by          | VARCHAR(100)  | No       | Audit field                                                  |

Composite Unique: master_lookup_id_fk + code (unique per parent lookup)

Business Rules on MD_LOOKUP_DETAIL:
- code MUST be unique within the same parent lookup type
- master_lookup_id_fk and code MUST NOT be modified after creation
- Edit allowed: name_ar / name_en / extra_value / sort_order only
- Delete may fail if the detail value is referenced by other entities

-----------------------------------------------------
Lookup Governance Rules (apply to all modules)
-----------------------------------------------------

- ALL dropdown fields across ALL modules MUST reference MD_LOOKUP_DETAIL
- No module may create its own dropdown or enum table
- No Java ENUMs for lookup values — ever
- lookup_key is the stable external contract — never changes after creation
- code is the stable detail contract — never changes after creation
- Max values per lookup category: <= 15  (if > 15 → Reference Table instead)
- All modules consume via API: GET /api/lookups/{lookup_key}?active=true
- Modules MUST NOT query lookup tables directly

-----------------------------------------------------
LAYER-1 — Foundation Entities
-----------------------------------------------------

⚠️ = Permanent Exception — uses actual names, not standard naming convention
✓  = GOVERNED — DB table name confirmed (DBS-ID registered)

| Entity               | DB Table Name           | Owner Module         | Status              | Used In                                |
|----------------------|-------------------------|----------------------|---------------------|----------------------------------------|
| LegalEntity          | ORG_LEGAL_ENTITY ✓      | Organization         | GOVERNED ✓          | All                                    |
| Branch               | ORG_BRANCH ✓            | Organization         | GOVERNED ✓          | All                                    |
| Department           | ORG_DEPARTMENT ✓        | Organization         | GOVERNED ✓          | Security / Finance / HR                |
| CostCenter           | ORG_COST_CENTER ✓       | Organization         | GOVERNED ✓          | Finance / Procurement / Projects       |
| ProfitCenter         | ORG_PROFIT_CENTER ✓     | Organization         | GOVERNED ✓          | Finance                                |
| Region               | ORG_REGION ✓            | Organization         | GOVERNED ✓          | TBD — see AQ-003                       |
| LocationSite         | ORG_LOCATION_SITE ✓     | Organization         | GOVERNED ✓          | Inventory                              |
| User                 | USERS  ⚠️               | Security             | Active ⚠️           | All                                    |
| Role                 | ROLES  ⚠️               | Security             | Active ⚠️           | All                                    |
| Permission           | PERMISSIONS  ⚠️         | Security             | Active ⚠️           | All                                    |
| Page                 | SEC_PAGES  ⚠️           | Security             | Active ⚠️           | All (each module seeds its pages here) |
| RefreshToken         | REFRESH_TOKENS  ⚠️      | Security             | Active ⚠️           | Security only (infrastructure)         |
| UserRole             | USER_ROLES  ⚠️          | Security             | Active ⚠️           | Security only (join table)             |
| RolePermission       | ROLE_PERMISSIONS ⚠️     | Security             | Active ⚠️           | Security only (join table)             |
| MdMasterLookup       | MD_MASTER_LOOKUP ⚠️     | MasterData           | Active ⚠️           | All modules (dropdown source)          |
| MdLookupDetail       | MD_LOOKUP_DETAIL ⚠️     | MasterData           | Active ⚠️           | All modules (dropdown values)          |
| Item                 | (planned)               | MasterData           | Planned             | Inventory / Sales / Procurement        |
| Customer             | (planned)               | MasterData           | Planned             | Sales / Finance                        |
| Vendor               | (planned)               | MasterData           | Planned             | Procurement / Finance                  |
| UnitOfMeasure        | (planned)               | MasterData           | Planned             | Inventory / Procurement / Sales        |
| Country              | (planned)               | MasterData           | Planned             | Organization / MasterData              |
| Currency             | (planned)               | CurrencyCalendar     | Planned             | Finance / Sales / Procurement          |
| ExchangeRate         | (planned)               | CurrencyCalendar     | Planned             | Finance / Sales / Procurement          |
| FiscalYear           | (planned)               | CurrencyCalendar     | Planned             | Finance / All Operational              |
| FiscalPeriod         | (planned)               | CurrencyCalendar     | Planned             | Finance / All Operational              |
| NumberingPattern     | (planned)               | NumberingEngine      | Planned             | All Operational Modules                |
| FileDocument         | FILE_DOCUMENT ✓         | FileService          | ACTIVE ✓ SVCAPI      | NotificationService / AuditService / All 3.x |
| FileCategory         | FILE_CATEGORY ✓         | FileService          | ACTIVE ✓ SVCAPI      | FileService only (per-module config)   |

FileService P0 Note (added 2026-07-11):
  P0 architecture convergence complete — module-registry-file.md +
  business-policies-file.md produced. "Attachment" (generic placeholder)
  formalized as FileDocument (table: FILE_DOCUMENT) + FileCategory
  (table: FILE_CATEGORY). Storage: PostgreSQL BYTEA (not Oracle BLOB —
  ARCH-REF-1.10 RESOLUTION-01). See Section 15 for readiness status.
FileService Implementation Note (added 2026-07-14):
  SVCAPI complete and live-verified — see Section 3's FileService /
  NotificationService Status Note and Section 8's FileService Consumption
  Rule for actual endpoints. FILE_CATEGORY has zero governed seed rows
  from FileService's own migration by design (srs-file A3: "no module
  decides which FileCategory rows another module needs — each consuming
  module decides its own categories") — a consuming module needing a new
  category must add its own seed row, same precedent as
  V3__file_service_schema_and_seed.sql Block 5b's dev/test row.
| AuditLog             | (planned)               | AuditService         | Planned             | All                                    |
| Notification         | NOTIF_LOG ✓             | NotificationService  | ACTIVE ✓ SVCAPI      | All                                    |
| NotificationTemplate | NOTIF_TEMPLATE ✓        | NotificationService  | ACTIVE ✓ SVCAPI      | All                                    |
| NotificationChannelConfig | NOTIF_CHANNEL_CONFIG ✓ | NotificationService  | ACTIVE ✓ SVCAPI  | NotificationService only (admin config) |

NotificationService P0 Note (added 2026-07-11):
  P0 architecture convergence complete — module-registry-notif.md +
  business-policies-notif.md produced. Entities above renamed at DDL
  level to NOTIF_LOG (Notification) / NOTIF_TEMPLATE / NOTIF_CHANNEL_CONFIG.
  See Section 15 for readiness status and open AQ-IDs.

Organization Reference Table Note:
  ORG_REGION_TYPE is a Reference Table (not a Lookup Detail) — governed under DBS-ORG-001.
  Owned by Organization. Used by: ORG_REGION. Initial values: GEOGRAPHIC / SALES / OPERATIONAL.
  Extensible by Admin. Ruled by > 15 values policy (Reference Table, not MD_LOOKUP_DETAIL).

-----------------------------------------------------
LAYER-2 — Engine Entities
-----------------------------------------------------

| Entity        | Owner Module   | Status  | Used In                              |
|---------------|----------------|---------|--------------------------------------|
| PriceList     | PricingEngine  | Planned | Sales / Procurement                  |
| Discount      | PricingEngine  | Planned | Sales / Procurement                  |
| TaxType       | TaxEngine      | Planned | Sales / Procurement / Finance        |
| TaxRule       | TaxEngine      | Planned | Sales / Procurement / Finance        |
| TaxExemption  | TaxEngine      | Planned | Sales / Procurement                  |

-----------------------------------------------------
LAYER-3 — Operational Entities
-----------------------------------------------------

| Entity            | Owner Module  | Status  | Used In                                    |
|-------------------|---------------|---------|--------------------------------------------|
| PurchaseRequest   | Procurement   | Planned | Procurement                                |
| PurchaseOrder     | Procurement   | Planned | Procurement / Inventory / Finance          |
| GoodsReceipt      | Procurement   | Planned | Inventory / Finance                        |
| VendorInvoice     | Procurement   | Planned | Finance                                    |
| StockMovement     | Inventory     | Planned | Inventory / Finance                        |
| Warehouse         | Inventory     | Planned | Inventory / Procurement / Sales            |
| StockCount        | Inventory     | Planned | Inventory                                  |
| SalesQuotation    | Sales         | Planned | Sales                                      |
| SalesOrder        | Sales         | Planned | Sales / Inventory / Finance                |
| Delivery          | Sales         | Planned | Inventory / Sales                          |
| CustomerInvoice   | Sales         | Planned | Finance                                    |
| ChartOfAccounts   | Finance       | Planned | Finance                                    |
| JournalEntry      | Finance       | Planned | Finance                                    |
| AccountPayable    | Finance       | Planned | Finance / Procurement                      |
| AccountReceivable | Finance       | Planned | Finance / Sales                            |
| Payment           | Finance       | Planned | Finance                                    |

=====================================================
6. Shared Lookup & Reference Tables
=====================================================

| Lookup Name         | Type           | Source Type     | Owner Module        | Max Values |
|---------------------|----------------|-----------------|---------------------|------------|
| Status              | Lookup         | Lookup Details  | Core                | <= 15      |
| DocumentType        | Lookup         | Lookup Details  | Core                | <= 15      |
| PaymentTerms        | Reference      | Reference Table | MasterData          | > 15       |
| ItemType            | Lookup         | Lookup Details  | MasterData          | <= 15      |
| TaxType             | Lookup         | Lookup Details  | TaxEngine           | <= 15      |
| CurrencyType        | Lookup         | Lookup Details  | CurrencyCalendar    | <= 15      |
| MovementType        | Lookup         | Lookup Details  | Inventory           | <= 15      |
| PriceListType       | Lookup         | Lookup Details  | PricingEngine       | <= 15      |
| NotificationChannel | Lookup         | Lookup Details  | NotificationService | <= 15      |
| NotificationStatus  | Lookup         | Lookup Details  | NotificationService | <= 15      |
| FileType            | Lookup         | Lookup Details  | FileService         | <= 15      |
| FileStatus          | Lookup         | Lookup Details  | FileService         | <= 15      |
| ScopeLevel          | Lookup         | Lookup Details  | Security            | <= 15      |
| LEGAL_ENTITY_TYPE   | Lookup         | Lookup Details  | Organization        | <= 15      |
| BRANCH_TYPE         | Lookup         | Lookup Details  | Organization        | <= 15      |
| DEPARTMENT_NODE_TYPE| Lookup         | Lookup Details  | Organization        | <= 15      |
| COST_CENTER_NODE_TYPE| Lookup        | Lookup Details  | Organization        | <= 15      |
| COST_CENTER_TYPE    | Lookup         | Lookup Details  | Organization        | <= 15      |
| LOCATION_SITE_TYPE  | Lookup         | Lookup Details  | Organization        | <= 15      |
| REGION_TYPE         | Reference      | Reference Table | Organization        | > 15       |

Rules:
- If values <= 15 → Lookup / Lookup Details
- If values > 15  → Reference Table (LOV)
- Detailed values defined in registry-[module].md

=====================================================
7. Module Dependency Matrix
=====================================================

-----------------------------------------------------
Dependency Summary Per Module
-----------------------------------------------------

| Module               | Direct Dependencies                                                    |
|----------------------|------------------------------------------------------------------------|
| Organization         | None — root module                                                     |
| Security             | Organization                                                           |
|                      | (+ NotificationService, EVENT-BASED ‡, for Forgot Password — Conflict #20)|
| MasterData           | Organization / Security                                                |
| CurrencyCalendar     | Organization / Security                                                |
| NumberingEngine      | Organization / Security / MasterData                                   |
| IntegrationService   | Organization / Security                                                |
| FileService          | Organization / Security                                                |
| NotificationService  | Organization / Security / FileService                                  |
| AuditService         | Organization / Security / FileService                                  |
| PricingEngine        | Layer-1 complete / MasterData ★ / CurrencyCalendar ★                  |
| TaxEngine            | Layer-1 complete / PricingEngine                                       |
| CalculationEngine    | Layer-1 complete / PricingEngine / TaxEngine                           |
| Procurement          | Layer-1 complete / Layer-2 complete                                    |
| Inventory            | Layer-1 complete / Layer-2 complete / Procurement                      |
| Sales                | Layer-1 complete / Layer-2 complete / Inventory                        |
| Finance              | Layer-1 complete / Layer-2 complete / Procurement / Inventory / Sales  |
| Reporting            | Layer-1 complete / Layer-2 complete / Layer-3 complete                 |
| Applications (5.x)   | All Layers complete                                                    |

★ Specifically called out — key consumers within the full Layer-1 dependency.
‡ EVENT-BASED dependency (publish-only, no build-order/HARD-FK coupling —
  H.2 pattern) — NOT added as a ● cell in the Full Dependency Matrix below,
  since that matrix tracks build-order/HARD dependencies only. See
  Conflict #20 (Section 13) for the full resolution of why this does not
  create a circular HARD dependency with NotificationService→Security.

-----------------------------------------------------
Full Dependency Matrix
-----------------------------------------------------

Rows = consumers. Columns = providers.
● = direct dependency.  — = no dependency.

| Module               | 1.1 Org | 1.2 Sec | 1.4 MDat | 1.5 Cur | 1.6 Num | 1.7 Int | 1.8 Notif | 1.9 Audit | 1.10 File | 2.1 Price | 2.2 Tax | 2.3 Calc | 3.1 Proc | 3.2 Inv | 3.3 Sales | 4.1 Rep |
|----------------------|---------|---------|----------|---------|---------|---------|-----------|-----------|-----------|-----------|---------|----------|----------|---------|-----------|---------|
| 1.2  Security        |    ●    |    —    |    —     |    —    |    —    |    —    |     —     |     —     |     —     |     —     |    —    |    —     |    —     |    —    |     —     |    —    |
| 1.4  MasterData      |    ●    |    ●    |    —     |    —    |    —    |    —    |     —     |     —     |     —     |     —     |    —    |    —     |    —     |    —    |     —     |    —    |
| 1.5  CurrencyCalendar|    ●    |    ●    |    —     |    —    |    —    |    —    |     —     |     —     |     —     |     —     |    —    |    —     |    —     |    —    |     —     |    —    |
| 1.6  NumberingEngine |    ●    |    ●    |    ●     |    —    |    —    |    —    |     —     |     —     |     —     |     —     |    —    |    —     |    —     |    —    |     —     |    —    |
| 1.7  IntegrationSvc  |    ●    |    ●    |    —     |    —    |    —    |    —    |     —     |     —     |     —     |     —     |    —    |    —     |    —     |    —    |     —     |    —    |
| 1.8  NotificationSvc |    ●    |    ●    |    —     |    —    |    —    |    —    |     —     |     —     |     ●     |     —     |    —    |    —     |    —     |    —    |     —     |    —    |
| 1.9  AuditService    |    ●    |    ●    |    —     |    —    |    —    |    —    |     —     |     —     |     ●     |     —     |    —    |    —     |    —     |    —    |     —     |    —    |
| 1.10 FileService     |    ●    |    ●    |    —     |    —    |    —    |    —    |     —     |     —     |     —     |     —     |    —    |    —     |    —     |    —    |     —     |    —    |
| 2.1  PricingEngine   |    ●    |    ●    |    ●     |    ●    |    ●    |    ●    |     ●     |     ●     |     ●     |     —     |    —    |    —     |    —     |    —    |     —     |    —    |
| 2.2  TaxEngine       |    ●    |    ●    |    ●     |    ●    |    ●    |    ●    |     ●     |     ●     |     ●     |     ●     |    —    |    —     |    —     |    —    |     —     |    —    |
| 2.3  CalcEngine      |    ●    |    ●    |    ●     |    ●    |    ●    |    ●    |     ●     |     ●     |     ●     |     ●     |    ●    |    —     |    —     |    —    |     —     |    —    |
| 3.1  Procurement     |    ●    |    ●    |    ●     |    ●    |    ●    |    ●    |     ●     |     ●     |     ●     |     ●     |    ●    |    ●     |    —     |    —    |     —     |    —    |
| 3.2  Inventory       |    ●    |    ●    |    ●     |    ●    |    ●    |    ●    |     ●     |     ●     |     ●     |     ●     |    ●    |    ●     |    ●     |    —    |     —     |    —    |
| 3.3  Sales           |    ●    |    ●    |    ●     |    ●    |    ●    |    ●    |     ●     |     ●     |     ●     |     ●     |    ●    |    ●     |    —     |    ●    |     —     |    —    |
| 3.4  Finance         |    ●    |    ●    |    ●     |    ●    |    ●    |    ●    |     ●     |     ●     |     ●     |     ●     |    ●    |    ●     |    ●     |    ●    |     ●     |    —    |
| 4.1  Reporting       |    ●    |    ●    |    ●     |    ●    |    ●    |    ●    |     ●     |     ●     |     ●     |     ●     |    ●    |    ●     |    ●     |    ●    |     ●     |    —    |
| 5.x  Applications    |    ●    |    ●    |    ●     |    ●    |    ●    |    ●    |     ●     |     ●     |     ●     |     ●     |    ●    |    ●     |    ●     |    ●    |     ●     |    ●    |

-----------------------------------------------------
Full Platform Execution Sequence
-----------------------------------------------------

| Phase        | Step  | Modules (parallel within step)                                    | Gate                      |
|--------------|-------|-------------------------------------------------------------------|---------------------------|
| LAYER-1      | L1-1  | Organization                                                      | None                      |
|              | L1-2  | Security                                                          | L1-1 complete             |
|              | L1-3  | MasterData / CurrencyCalendar / IntegrationService / FileService  | L1-2 complete             |
|              | L1-4  | NumberingEngine / NotificationService / AuditService              | L1-3 complete             |
| LAYER-2      | L2-1  | PricingEngine                                                     | Layer-1 fully complete    |
|              | L2-2  | TaxEngine                                                         | L2-1 complete             |
|              | L2-3  | CalculationEngine                                                 | L2-2 complete             |
| LAYER-3      | L3-1  | Procurement                                                       | Layer-2 fully complete    |
|              | L3-2  | Inventory                                                         | L3-1 complete             |
|              | L3-3  | Sales                                                             | L3-2 complete             |
|              | L3-4  | Finance                                                           | L3-1 + L3-2 + L3-3 done  |
| LAYER-4      | L4-1  | Reporting                                                         | Layer-3 fully complete    |
| LAYER-5      | L5-1  | CommercialERP / IndustrialERP / ContractingERP / CustomApplication| Layer-4 fully complete    |

Total sequential steps: 12
Maximum parallelism: L1-3 (4 modules) / L1-4 (3 modules) / L5-1 (4 applications)

=====================================================
8. Cross-Module Integration Rules
=====================================================

FINANCIAL RULES:
- Finance MUST be the central accounting module
- All financial impacts MUST produce a JournalEntry in Finance
- Procurement financial impact  → Finance (AP + JournalEntry)
- Sales financial impact        → Finance (AR + JournalEntry)
- Inventory financial impact    → Finance (JournalEntry if costed)
- CostCenter MUST be referenced in every financial transaction

DATA SCOPE RULES:
- Security defines the data scope — not the module
- Every module MUST enforce DataScope at query level
- No module may bypass DataScope to access another company's data
- DataScope levels: Platform / LegalEntity / Branch / Department

NUMBERING RULES:
- Every document MUST get its number from NumberingEngine
- No module may implement its own numbering logic
- Pattern format: [Prefix]-[LegalEntity]-[Branch]-[Year]-[Sequence]

AUDIT RULES:
- Every state change MUST produce an AuditLog entry
- AuditLog MUST capture: entityType / entityId / fieldName / oldValue / newValue / userId / timestamp
- No silent updates allowed

NOTIFICATION RULES:
- Every business event MAY produce a Notification
- Notification channels: InApp / Email / SMS / WhatsApp (+ Push — 5 total, see Section 6)
- Templates MUST be defined per event type
- NotificationService owns delivery — modules only publish events

NotificationService Consumption Rule (added 2026-07-14, verified against live
backend code + a live end-to-end test run):
  Two real ingress paths exist today. The RabbitMQ path described in
  module-registry-notif.md's CORE.md ("erp.notification.exchange /
  erp.notification.queue / routing key notification.send") is
  architecturally documented but NOT YET IMPLEMENTED — no RabbitMQ
  dependency, listener, or exchange exists anywhere in this backend as of
  this note. Do not assume it works; use one of the two paths below.
    1. REST — POST /api/v1/notifications/send (or /schedule; /schedule
       currently dispatches immediately like /send, see DRV-NOTIF-004 —
       true deferred/durable scheduling is not implemented yet). JWT
       required, gated only by isAuthenticated() — any logged-in caller
       may send, no PERM_NOTIFICATION_* permission is enforced on this
       endpoint specifically.
       Body: { recipientId, channelHint: [...], templateCode, contextData,
       priority: HIGH|MEDIUM|LOW, moduleCode, referenceId?, referenceType? }
       recipientId MUST be a real USERS.users_pk — NOTIF_LOG.recipient_id
       carries a live FK constraint to USERS; an unknown id fails with
       DB_CONSTRAINT_VIOLATION (409), not a friendly validation error.
    2. Same-JVM Spring Event — publish a
       com.example.erp.notification.event.NotificationRequestedEvent
       (wraps the same NotificationSendRequest) via
       ApplicationEventPublisher; consumed synchronously, same
       transaction, by NotificationRequestedEventListener. Preferred for
       any module already running inside the same Spring context/
       transaction — no HTTP round-trip, no separate auth context needed.
  channelHint is decided by the PUBLISHING module — NotificationService
  never infers a channel from event type or moduleCode (Conflict #23,
  CLOSED). If templateCode does not resolve to a real NOTIF_TEMPLATE row,
  RULE-NOTIF-006 falls back to a generic in-memory template rather than
  failing the send (DRV-NOTIF-002) — no SYSTEM_DEFAULT template is seeded
  anywhere yet, so this fallback is always in play until one is.
  Read-tracking is NOT implemented: GET /unread and PUT /{id}/read
  (API-NOTIF-004/005) are governed contract shells that always throw
  NOTIF_READ_TRACKING_UNAVAILABLE (422), pending an SRS/DB amendment
  (DRV-NOTIF-003 — NOTIF_LOG has no read/unread column).
  Querying history: POST /api/v1/notifications/history/search requires
  PERM_NOTIFICATION_INBOX_VIEW; recipientId defaults to the caller's own
  resolved user id if omitted (any holder of INBOX_VIEW may currently pass
  an explicit recipientId to query others — no distinct admin-tier
  permission is seeded yet for that restriction, flagged not silently
  decided).
  No module may query NOTIF_LOG / NOTIF_TEMPLATE / NOTIF_CHANNEL_CONFIG
  directly — same rule as MD_MASTER_LOOKUP (Section 4).

FILE RULES:
- All attachments MUST be stored via FileService
- No module may store files directly
- Every attachment MUST be linked to an entity (entityType + entityId)

FileService Consumption Rule (added 2026-07-14, verified against live
backend code + a live end-to-end test run):
  FileService is NOT a plain CRUD API — it is a short-lived, encrypted,
  single-use-token-gated flow. Consuming modules MUST follow this
  sequence; never assume /upload or /download can be called without a
  freshly issued token:
    1. POST /api/v1/files/upload-token — JWT required, gated only by
       isAuthenticated() (PERM_FILE_ATTACHMENT_* permissions exist and are
       seeded per SCR-FILE-001, but are not actually enforced on this
       call). Body: { ownerId, ownerType, moduleCode, fileCategoryFk }.
       fileCategoryFk MUST be a pre-existing FILE_CATEGORY row —
       FileCategory has NO Create/Update API (DRV-FILE-001, DB-seeded
       reference table only; srs-file A3 explicitly says "no module
       decides which FileCategory rows another module needs — each
       consuming module decides its own categories"). A module needing a
       new category must add its own migration seed row — see
       V3__file_service_schema_and_seed.sql Block 5b for the precedent.
    2. POST /upload/{encryptedToken} (multipart) — permitAll, NO JWT; the
       token itself is the authorization. Single-use, ~100-minute TTL
       (RULE-FILE-002).
    3. To read back later: POST /api/v1/files/{fileDocumentPk}/access-token
       ?action=DOWNLOAD (JWT required, isAuthenticated() only) → then
       GET /download/{encryptedToken} (permitAll, token-only).
    4. To delete: POST .../access-token?action=DELETE requires
       PERM_SYSTEM_ADMIN specifically — NOT PERM_FILE_ATTACHMENT_DELETE.
       RULE-FILE-007's "owner-or-Admin" restriction is Admin-only today
       (OQ-FILE-001 — a non-owner-vs-owner authorization check across
       modules is not designed yet) → then DELETE /{encryptedToken}
       (permitAll, token-only).
  GET /api/v1/files/{ownerId} lists FileDocument rows for a given owner
  (JWT required).
  No module may query FILE_DOCUMENT / FILE_CATEGORY directly — same rule
  as MD_MASTER_LOOKUP (Section 4).

INTEGRATION RULES:
- All external connections MUST go through IntegrationService
- No module may call an external system directly
- Data transformation MUST happen inside IntegrationService

CROSS-MODULE FK RULES:
- Cross-module FK MUST follow naming convention (Fk suffix)
- Cross-module FK MUST be validated at service layer
- Cross-module FK MUST NOT rely on DB-level foreign key constraints

LOOKUP CONSUMPTION RULES:
- All modules consume lookup values via: GET /api/lookups/{lookup_key}?active=true
- Modules MUST NOT query MD_MASTER_LOOKUP or MD_LOOKUP_DETAIL directly
- lookup_key is the only stable external contract — id_pk is internal and MUST NOT be shared
- Any field referencing a lookup value stores the detail id internally

=====================================================
9. Internal Module Pattern (Mandatory)
=====================================================

Every module — without exception — MUST follow this internal structure:

  Layer-4 : Interface Layer      (Thin Controller — no logic)
  Layer-3 : Application Layer    (Use Case per operation)
  Layer-2 : Domain Layer         (Entity + Validator — no technology)
  Layer-1 : Infrastructure Layer (Repository per entity — save/find/update/delete)

Rules:
- Domain Layer MUST NOT know about Database or API
- Infrastructure Layer MUST NOT contain business logic
- Application Layer MUST NOT contain business rules
- Interface Layer MUST NOT contain business rules or DB logic
- One Use Case per operation — no shared Use Cases between operations

=====================================================
10. Data Ownership & Authority
=====================================================

| Entity               | DB Table Name           | Owner Module         | Editable By                      |
|----------------------|-------------------------|----------------------|----------------------------------|
| MD_MASTER_LOOKUP     | MD_MASTER_LOOKUP ⚠️     | MasterData           | Admin Only                       |
| MD_LOOKUP_DETAIL     | MD_LOOKUP_DETAIL ⚠️     | MasterData           | Admin Only                       |
| LegalEntity          | ORG_LEGAL_ENTITY ✓      | Organization         | Admin Only                       |
| Branch               | ORG_BRANCH ✓            | Organization         | Admin Only                       |
| Department           | ORG_DEPARTMENT ✓        | Organization         | Admin Only                       |
| CostCenter           | ORG_COST_CENTER ✓       | Organization         | Admin Only                       |
| ProfitCenter         | ORG_PROFIT_CENTER ✓     | Organization         | Admin Only                       |
| Region               | ORG_REGION ✓            | Organization         | Admin Only                       |
| LocationSite         | ORG_LOCATION_SITE ✓     | Organization         | Admin Only                       |
| User                 | USERS  ⚠️               | Security             | Admin Only                       |
| Role                 | ROLES  ⚠️               | Security             | Admin Only                       |
| Permission           | PERMISSIONS  ⚠️         | Security             | Admin Only                       |
| Page                 | SEC_PAGES  ⚠️           | Security             | Admin Only                       |
| Currency             | (planned)               | CurrencyCalendar     | Admin Only                       |
| ExchangeRate         | (planned)               | CurrencyCalendar     | Admin / Finance                  |
| FiscalYear           | (planned)               | CurrencyCalendar     | Admin / Finance                  |
| FiscalPeriod         | (planned)               | CurrencyCalendar     | Admin / Finance                  |
| Item                 | (planned)               | MasterData           | Admin / Inventory                |
| Customer             | (planned)               | MasterData           | Admin / Sales                    |
| Vendor               | (planned)               | MasterData           | Admin / Procurement              |
| UnitOfMeasure        | (planned)               | MasterData           | Admin Only                       |
| Country              | (planned)               | MasterData           | Admin Only                       |
| NumberingPattern     | (planned)               | NumberingEngine      | Admin Only                       |
| PriceList            | (planned)               | PricingEngine        | Admin / Sales                    |
| TaxType              | (planned)               | TaxEngine            | Admin Only                       |
| TaxRule              | (planned)               | TaxEngine            | Admin Only                       |
| ChartOfAccounts      | (planned)               | Finance              | Admin / Finance                  |

⚠️ = Permanent Exception — uses actual names, not standard naming convention
✓  = GOVERNED — DB table name confirmed (DBS-ID registered)

Rules:
- Each entity MUST have a single owner module
- Cross-module updates MUST respect ownership
- Admin always has override access

=====================================================
11. Deferred Modules (Out of Scope — Current Vision)
=====================================================

These modules are architecturally planned but deferred.
They MUST be built ON TOP of the current platform without redesign.

| Module               | Reason Deferred                                      | Depends On When Added              |
|----------------------|------------------------------------------------------|------------------------------------|
| IntegrationService   | No operational module requires external integration  | Organization / Security            |
|                      | in current scope — activation per explicit request   |                                    |
| ApprovalEngine       | < 70% project usage                                  | Layer-1 complete                   |
| WorkflowEngine       | < 70% project usage                                  | Layer-1 + ApprovalEngine           |
| AI Engine            | Advanced phase                                       | Layer-1 + Layer-2                  |
| HRModule             | < 70% project usage                                  | Layer-1 complete                   |
| ProjectManagement    | < 70% project usage                                  | Layer-1 + Layer-2 + Finance        |

Rule: Deferred modules MUST NOT block current platform development.
Rule: Current platform design MUST NOT assume deferred modules exist.
Rule: All deferred modules MUST be buildable on top without redesigning any core layer.

=====================================================
12. Versioning & Change Control
=====================================================

| Version | Date       | Change Description                                           | Approved By |
|---------|------------|--------------------------------------------------------------|-------------|
| 1.0.0   | 2026-04-07 | Initial Registry Definition                                  | Architect   |
| 1.0.1   | 2026-04-07 | Clarified LOCKED definition + Approval ref                   | Architect   |
| 2.0.0   | 2026-05-21 | Full restructure — Enterprise Engine Platform Vision v3.0    | Architect   |
|         |            | 5 layers / 22 modules / new engines added                    |             |
|         |            | Security / Numbering / Integration / Notification / Audit    |             |
|         |            | File / Pricing / Tax / Calculation engines                   |             |
|         |            | CostCenter added to Organization                             |             |
|         |            | DataScope model defined. Internal pattern enforced.          |             |
| 2.1.0   | 2026-05-21 | MD_MASTER_LOOKUP / MD_LOOKUP_DETAIL registered as shared     | Architect   |
|         |            | platform entities. Lookup rules and ownership defined.       |             |
| 2.2.0   | 2026-05-21 | Security module registered as PERMANENT EXCEPTION.           | Architect   |
|         |            | UserManagement merged into Security — no split.              |             |
|         |            | Actual DB table names documented.                            |             |
| 2.3.0   | 2026-05-21 | Module dependency matrix expanded — full matrix added.       | Architect   |
|         |            | Internal build sequence per layer with parallel steps.       |             |
| 2.4.0   | 2026-05-24 | MasterData Lookup feature registered as ACTIVE.              | Architect   |
|         |            | Actual DB columns documented vs registry standard.           |             |
|         |            | Naming deviations identified and logged.                     |             |
|         |            | Source: registry-masterdata.md v1.0.0                        |             |
| 2.4.1   | 2026-05-24 | MasterData Lookup promoted to PERMANENT EXCEPTION.           | Architect   |
|         |            | Decision: all implemented code kept AS-IS — no changes.      |             |
|         |            | Same policy as Security module — applies to all future       |             |
|         |            | implemented-before-registry features.                        |             |
|         |            | AQ-001 RESOLVED: lookupKey stays as-is permanently.          |             |
|         |            | AQ-002 RESOLVED: extraValue kept as-is permanently.          |             |
|         |            | Section 14 (Open Questions) closed — no open questions.      |             |
|         |            | Platform Implementation Policy added to Section 1.           |             |
| 2.5.0   | 2026-05-24 | Added Section 15 — P0 Architecture Convergence Status.       | Architect   |
|         |            | Tracks P0 readiness per module. Gates P1 entry.              |             |
|         |            | Security and MasterData Lookup pre-registered as             |             |
|         |            | EXCEPTION and PARTIAL EXCEP respectively.                    |             |
| 2.6.0   | 2026-06-01 | IntegrationService moved to Section 11 — Deferred.           | Architect   |
|         |            | No operational module in current scope requires external     |             |
|         |            | integration. Activation trigger: explicit project request.   |             |
| 2.7.0   | 2026-06-16 | Organization module MODE 1.5 COMPLETE — GOVERNED ✓.          | Architect   |
|         |            | Source: DBS-ORG-001 / PLAN-ORG-001 / srs-org-001.md v1.0    |             |
|         |            | Section 3: Organization status → GOVERNED ✓ MODE 1.5.        |             |
|         |            | Section 5: 7 ORG entities promoted — DB table names          |             |
|         |            |   confirmed (ORG_LEGAL_ENTITY..ORG_LOCATION_SITE).           |             |
|         |            |   ProfitCenter / Region / LocationSite added to registry.    |             |
|         |            |   ORG_REGION_TYPE reference table registered.                |             |
|         |            | Section 6: 7 ORG lookup keys registered (LOV-ORG-001..007).  |             |
|         |            |   REGION_TYPE added as Reference Table entry.                |             |
|         |            | Section 10: ORG entity table names updated — 5 entities       |             |
|         |            |   promoted from (planned), 3 new entities added.             |             |
|         |            | Section 14: AQ-003 added — Region SOFT-READ consumer TBD.    |             |
|         |            | Section 15: Organization → READY ✓ — P1 gate OPEN.           |             |

| 2.7.4   | 2026-06-28 | MasterData Lookup columns updated — Oracle → PostgreSQL migration. | Architect   |
|         |            | Column names updated to PostgreSQL snake_case actuals:            |             |
|         |            | id→id_pk, lookupKey→lookup_key, masterLookup→master_lookup_id_fk,|             |
|         |            | nameAr→name_ar, nameEn→name_en, extraValue→extra_value,          |             |
|         |            | isActive→is_active. detailCountFormula removed (not in PG DB).   |             |
|         |            | Security module: Oracle→PG type migration noted (no registry      |             |
|         |            | change required — table names unchanged). Source:                 |             |
|         |            | security-registry.md v2.2.0 / MD DDL PostgreSQL script.          |             |

| 2.7.3   | 2026-06-26 | Organization promoted MODE 1.5 → MODE 2 (ALIGN GATE PASSED ✓).  | Architect   |
|         |            | Source: registry-exec-org.md (PLAN-ORG-001).                 |             |

| 2.7.6   | 2026-07-09 | Applied registry-update-blocks-SEC.md (partial — see below).  | Registry    |
|         |            | Section 13: added a new conflict entry, #20 = proposed         | Builder     |
|         |            | Security(1.2)→NotificationService(1.8) dependency conflicts     |             |
|         |            | with existing NotificationService→Security dependency           |             |
|         |            | (two-way cycle) — left OPEN, NOT added to Section 7 matrix.     |             |
|         |            | Also folded in: Organization's XM Inbound Stub gap re:          |             |
|         |            | Security/Branch. Section 14: AQ-007 added (OPEN), linked to     |             |
|         |            | AQ-006 (still OPEN, not closed). Section 15: Security row       |             |
|         |            | split into core EXCEPTION vs extension PARTIALLY_READY ⚠️       |             |
|         |            | (BLOCKED on BLK-SEC-002), with explanatory note added. NOT       |             |
|         |            | applied: Section 7 dependency rows and a dedicated "Blocking     |             |
|         |            | Dependencies" section/BLK-SEC-002 entry — source file            |             |
|         |            | registry-update-blocks-SEC.md was not provided in full, only     |             |
|         |            | described; the described NotificationService dependency turned  |             |
|         |            | out to conflict rather than being a clean addition. Pending:     |             |
|         |            | (a) the actual registry-update-blocks-SEC.md file, (b) a         |             |
|         |            | resolution for the Security↔NotificationService cycle.           |             |

| 2.7.7   | 2026-07-09 | Reference-only update: Section 15 registry-security.md         | Registry    |
|         |            | citation bumped v2.4.0 → v2.4.1, reflecting the EMAIL data-     | Builder     |
|         |            | consistency fix applied there (EMAIL added to USERS §1.1,       |             |
|         |            | removed duplicate from SEC_USER_PROFILE §8.1 — no architectural |             |
|         |            | decision changed). No other content touched: AQ-006, AQ-007,   |             |
|         |            | Conflict #20, BLK-SEC-002 all unchanged.                        |             |

| 2.8.0   | 2026-07-11 | P0 Phase 2 complete for NotificationService (1.8).             | Hesham /    |
|         |            | Section 3: added NotificationChannelConfig entity row.          | P0 session  |
|         |            | Section 6: added NotificationStatus lookup.                     |             |
|         |            | Section 13: added Conflict #21 (channel list mismatch, CLOSED)  |             |
|         |            | #22 (NotificationService→FileService HARD-FK softened to        |             |
|         |            | DEFERRED via inline template storage + RXE migration path,      |             |
|         |            | CLOSED), and #23 (channel-selection ownership assigned to       |             |
|         |            | publishing business module, not Notification, CLOSED).          |             |
|         |            | Section 14: AQ-008, AQ-009 resolved and moved to "Previously     |             |
|         |            | resolved". AQ-010 (SMS provider), AQ-011 (WhatsApp provider),   |             |
|         |            | AQ-012 (Push phase confirmation) added as OPEN, non-blocking.   |             |
|         |            | Section 15: NotificationService NOT STARTED → PARTIALLY_READY ⚠️.|             |
|         |            | Source: ARCH-REF-1.8-NOTIFICATION-SERVICE.md v1.1.0,             |             |
|         |            | module-registry-notif.md, business-policies-notif.md.            |             |

| 2.8.1   | 2026-07-11 | Push (Firebase) channel — AQ-012 resolved: no deferral for any | Hesham /    |
|         |            | channel. Push moved from structure-ready/deferred to fully      | P0 session  |
|         |            | implemented Phase 1, same as Email/SMS/WhatsApp/Internal.        |             |
|         |            | Section 14: AQ-012 moved to "Previously resolved". Section 15:  |             |
|         |            | NotificationService Open AQ-IDs reduced to AQ-010/011 only       |             |
|         |            | (technical provider choices — not phase questions).              |             |
|         |            | Source: ARCH-REF-1.8-NOTIFICATION-SERVICE.md v1.1.0 (final).    |             |

| 2.9.0   | 2026-07-11 | P0 Phase 2 complete for FileService (1.10).                     | Hesham /    |
|         |            | Section 3: "Attachment" placeholder formalized as FileDocument  | P0 session  |
|         |            | (FILE_DOCUMENT) + FileCategory (FILE_CATEGORY).                 |             |
|         |            | Section 6: added FileStatus lookup.                              |             |
|         |            | Section 13: added Conflict #24 (Oracle BLOB/UCP/PDFBox →         |             |
|         |            | PostgreSQL BYTEA/HikariCP, PDFBox deferred, CLOSED).             |             |
|         |            | Section 15: FileService NOT STARTED → READY ✓, no open AQ-IDs.  |             |
|         |            | Source: ARCH-REF-1.10-FILE-SERVICE.md v1.1.0,                   |             |
|         |            | module-registry-file.md, business-policies-file.md.              |             |

| 2.9.1   | 2026-07-11 | Conflict #20 (Security↔NotificationService circular    | Hesham /    |
|         |            | dependency, BLK-SEC-002) RESOLVED and CLOSED — kept in the      | P0 session  |
|         |            | log per LOCKED governance audit-trail requirement (not          |             |
|         |            | deleted; conflicts are closed, never removed). Resolution:      |             |
|         |            | Security→NotificationService (Forgot Password) reclassified     |             |
|         |            | as Event-Based, not HARD-FK — same pattern as every other        |             |
|         |            | module's relationship to Notification (H.2). Section 7:          |             |
|         |            | added Event-Based (‡) footnote for Security row. Section 15:    |             |
|         |            | Security extension scope PARTIALLY_READY ⚠️, no longer           |             |
|         |            | BLOCKED. AQ-006/AQ-007 remain open (separate, non-blocking       |             |
|         |            | issue — registry version-citation mismatch, untouched).          |             |

| 2.10.0  | 2026-07-14 | FileService (1.10) and NotificationService (1.8)                | Hesham /    |
|         |            | backend SVCAPI implementation complete and live-verified         | live SVCAPI |
|         |            | end-to-end (all governed API-IDs reachable, DB migrations V3/V5  | test run    |
|         |            | applied, permissions seeded and granted to SUPER_ADMIN). This    |             |
|         |            | entry documents verified runtime behavior, not new architecture  |             |
|         |            | decisions — no Conflict/AQ entries added.                        |             |
|         |            | Section 3: status Planned → Active ⚠️ SVCAPI ✓ for both modules; |             |
|         |            | Status Note added explaining what SVCAPI-complete does and does  |             |
|         |            | not cover (DOC/INT-C/INT-R/F1-F3/SEC/ALIGN still PENDING).        |             |
|         |            | Section 5: FileDocument/FileCategory/Notification/                |             |
|         |            | NotificationTemplate/NotificationChannelConfig promoted from      |             |
|         |            | (planned) to ACTIVE ✓ SVCAPI with confirmed DB table names        |             |
|         |            | (FILE_DOCUMENT, FILE_CATEGORY, NOTIF_LOG, NOTIF_TEMPLATE,         |             |
|         |            | NOTIF_CHANNEL_CONFIG). Implementation notes added under each.     |             |
|         |            | Section 8: FILE RULES / NOTIFICATION RULES expanded with          |             |
|         |            | concrete, verified Consumption Rule blocks (same pattern as       |             |
|         |            | Section 4's Security/MasterData Lookup blocks) — actual           |             |
|         |            | endpoints, auth/permission requirements per endpoint, and         |             |
|         |            | integration patterns for other modules to call these two          |             |
|         |            | services correctly. Flags that NotificationService's documented   |             |
|         |            | RabbitMQ ingress path is NOT implemented (Spring Events + REST    |             |
|         |            | are the only real ingress paths today) and that FileService's     |             |
|         |            | DELETE access-token action requires PERM_SYSTEM_ADMIN, not        |             |
|         |            | PERM_FILE_ATTACHMENT_DELETE. Source: live backend source read     |             |
|         |            | (FileController/NotificationController and services) + a full     |             |
|         |            | test_file_apis.py / test_notification_apis.py run against this    |             |
|         |            | dev environment, both 100% green (25/25, 18/18).                  |             |

- Major changes (new layer / new module) → increment major version (X.0.0)
- Minor changes (new entity / new rule) → increment minor version (X.Y.0)
- Patches (corrections / resolutions) → increment patch version (X.Y.Z)
- Changes MUST be approved before usage

=====================================================
13. Conflict & Resolution Log
=====================================================

| #  | Conflict Description                            | Modules Affected               | Resolution                                                  | Status    |
|----|-------------------------------------------------|--------------------------------|-------------------------------------------------------------|-----------|
| 1  | Security module pre-dates registry              | Security / UserManagement      | PERMANENT EXCEPTION — used AS-IS, no code changes ever      | CLOSED    |
| 2  | UserManagement separation planned               | Security                       | Merged into Security — no split will ever occur             | CLOSED    |
| 3  | Naming convention violations in Security        | Security (USERS/ROLES/etc.)    | PERMANENT EXCEPTION — other modules must comply             | CLOSED    |
| 4  | DataScope not implemented as entity             | Security                       | System is permanently single-tenant — no TENANT_ID column   | CLOSED    |
| 5  | Lookup tables defined per module                | All modules                    | Centralized in MD_MASTER_LOOKUP / MD_LOOKUP_DETAIL          | CLOSED    |
| 6  | Item ownership conflict                         | Inventory / Sales              | Assigned to MasterData                                      | CLOSED    |
| 7  | Numbering logic scattered across modules        | Procurement / Sales / Finance  | Centralized in NumberingEngine                              | CLOSED    |
| 8  | File storage in each module                     | All operational modules        | Centralized in FileService                                  | CLOSED    |
| 9  | Data scope enforcement per module               | All modules                    | Centralized in Security / DataScope                         | CLOSED    |
| 10 | Tax logic in Sales and Procurement              | Sales / Procurement            | Centralized in TaxEngine                                    | CLOSED    |
| 11 | MasterData Lookup naming deviations (id, etc.)  | MasterData (Lookup feature)    | PERMANENT EXCEPTION — implemented before registry, AS-IS    | CLOSED    |
| 12 | lookupKey vs lookupCode naming                  | MasterData (Lookup feature)    | PERMANENT EXCEPTION — lookupKey stays, no rename ever       | CLOSED    |
| 13 | isActive vs isActiveFl in Lookup tables         | MasterData (Lookup feature)    | PERMANENT EXCEPTION — isActive stays, no rename ever        | CLOSED    |
| 14 | extraValue field not in original registry       | MasterData (Lookup feature)    | ACCEPTED — field kept AS-IS, registered in Section 5        | CLOSED    |
| 15 | isSystemFl in registry but not in code          | MasterData (Lookup feature)    | NOT REQUIRED — no plan to implement, removed from spec      | CLOSED    |
| 16 | extraValue max length inconsistency 255 vs 500  | MasterData (Lookup feature)    | PERMANENT EXCEPTION — kept AS-IS, no change required        | CLOSED    |
| 20 | Circular dependency introduced by proposed       | Security / NotificationService | RESOLVED — Security(1.2)→NotificationService(1.8) for        | CLOSED    |
|    | Security(1.2)→NotificationService(1.8) dependency|                                 | Forgot-Password is reclassified as Event-Based (publish-only  |           |
|    | (Forgot Password), which conflicts with the      |                                 | via RabbitMQ/erp.notification.exchange), not HARD-FK. This    |           |
|    | existing NotificationService(1.8)→Security(1.2)  |                                 | is the SAME pattern every other module already uses to talk   |           |
|    | dependency and L1-2/L1-4 build sequence          |                                 | to Notification (platform-standards.md H.2: "Any module →     |           |
|    |                                                  |                                 | Audit/Notification → Event-Based always"; see also            |           |
|    |                                                  |                                 | XM-INBOUND-STUB-NOTIF-1, module-registry-notif.md, which       |           |
|    |                                                  |                                 | already documents all 3.x modules as event producers).         |           |
|    |                                                  |                                 | Two dependencies in opposite directions with DIFFERENT types   |           |
|    |                                                  |                                 | (NotificationService→Security = HARD-FK on USERS, synchronous, |           |
|    |                                                  |                                 | build-order L1-2 before L1-4; Security→NotificationService =   |           |
|    |                                                  |                                 | Event-Based, fire-and-forget, no build-order or data coupling) |           |
|    |                                                  |                                 | is NOT a circular HARD-FK dependency — Event-Based pattern     |           |
|    |                                                  |                                 | exists specifically to decouple this kind of relationship      |           |
|    |                                                  |                                 | (H.1: "Effect: full decoupling — each side is independent").   |           |
|    |                                                  |                                 | BLK-SEC-002 CLOSED — Security's extension scope (Forgot         |           |
|    |                                                  |                                 | Password) is no longer blocked pending this resolution.        |           |
|    |                                                  |                                 | Section 7 matrix updated with an Event-Based footnote (not a   |           |
|    |                                                  |                                 | new ● cell — Event-Based deps are not build-order HARD deps).  |           |
|    |                                                  |                                 | SEPARATE, still-open sub-item (not resolved by this entry):    |           |
|    |                                                  |                                 | Organization's XM Inbound Stub list still does not list        |           |
|    |                                                  |                                 | Security as a consumer of Branch (SEC_USER_PROFILE/            |           |
|    |                                                  |                                 | SEC_ROLE_BRANCH HARD-FK on ORG_BRANCH) — tracked separately,    |           |
|    |                                                  |                                 | to be added to registry-exec-ORG.md's XM-INBOUND-STUB list on  |           |
|    |                                                  |                                 | Organization's next registry touch, not blocking. Decided by:  |           |
|    |                                                  |                                 | Hesham, 2026-07-11.                                             |           |
| 21 | Notification channel list mismatch — master-     | NotificationService             | RESOLVED — union of both lists adopted: Email + SMS +        | CLOSED    |
|    | registry §8 ("InApp/Email/SMS/WhatsApp") vs      |                                  | WhatsApp + Push + Internal (5 channels), single unified      |           |
|    | ARCH-REF-1.8-NOTIFICATION-SERVICE.md §2          |                                  | table (NOTIF_LOG.notification_type / NOTIF_CHANNEL_CONFIG    |           |
|    | ("Email/SMS/Firebase Push/Internal")             |                                  | .channel_type). Phase 1: all 5 channels fully implemented —  |           |
|    |                                                  |                                  | no deferral for any channel (final decision 2026-07-11).     |           |
|    |                                                  |                                  | Source: ARCH-REF-1.8 v1.1.0 SECTION 0 RESOLUTION-01.          |           |
|    |                                                  |                                  | Decided by: Hesham, 2026-07-11.                                |           |
| 22 | NotificationService(1.8) HARD-FK on File Service | NotificationService / FileService| RESOLVED (architectural workaround) — File Service is         | CLOSED    |
|    | (1.10) templates (AD-NOTIF-05 "MANDATORY") while |                                  | NOT STARTED. NOTIF_TEMPLATE stores template body inline       |           |
|    | FileService shows NOT STARTED in Section 15      |                                  | (template_body_ar/en) for Phase 1; file_id kept NULLABLE      |           |
|    |                                                  |                                  | for migration once FileService is governed. Migration is      |           |
|    |                                                  |                                  | governed by the standard XM Resolution Event mechanism         |           |
|    |                                                  |                                  | (RXE-NOTIF-[SEQ], per shared-artifact-contracts.md CONTRACT-8) |           |
|    |                                                  |                                  | — not an ad-hoc protocol. XM-NOTIF-[TBD]→FILE_DOCUMENT         |           |
|    |                                                  |                                  | downgraded from BLOCKED to DEFERRED. Source: ARCH-REF-1.8      |           |
|    |                                                  |                                  | v1.1.0 SECTION 0 RESOLUTION-02 + AD-NOTIF-11. Decided by:      |           |
|    |                                                  |                                  | Hesham, 2026-07-11.                                            |           |
| 23 | Which module decides notification channel(s)     | NotificationService              | RESOLVED — the publishing business module (Procurement/       | CLOSED    |
|    | per event? (implicit gap — not previously logged)|                                  | Sales/Inventory/Finance/...) decides via channelHint (single/  |           |
|    |                                                  |                                  | list/"ALL") on every published event. Notification Service     |           |
|    |                                                  |                                  | contains no logic mapping event type or module_code to a       |           |
|    |                                                  |                                  | channel — enforces platform-standards.md §A.3 module           |           |
|    |                                                  |                                  | ownership boundary. Source: ARCH-REF-1.8 v1.1.0 AD-NOTIF-10.   |           |
|    |                                                  |                                  | Decided by: Hesham, 2026-07-11.                                |           |
| 24 | ARCH-REF-1.10-FILE-SERVICE.md prescribes Oracle  | FileService                      | RESOLVED — same PostgreSQL adaptation pattern as               | CLOSED    |
|    | BLOB + Oracle UCP (AD-FILE-01/04), and includes  |                                   | Notification (AD-NOTIF-07 precedent): BLOB → BYTEA (5MB        |           |
|    | PDFBox with no defined use case (AD-FILE-08),    |                                   | ceiling makes Large Objects unnecessary), Oracle UCP →         |           |
|    | while DB_TARGET = POSTGRESQL_16 and PDFBox has   |                                   | HikariCP. PDFBox NOT added Phase 1 — no use case exists        |           |
|    | no confirmed requirement anywhere in the platform|                                   | even in the source reference (fails platform-standards §M      |           |
|    |                                                  |                                   | Day-1 test), tracked as an explicit reopening condition        |           |
|    |                                                  |                                   | rather than silently dropped. Source: ARCH-REF-1.10 v1.1.0     |           |
|    |                                                  |                                   | SECTION 0 RESOLUTION-01/02/03. Decided by: Hesham, 2026-07-11. |           |


Rules:
- All conflicts MUST be logged
- No silent resolution allowed
- CLOSED = decision is final and permanent

=====================================================
14. Open Architectural Questions
=====================================================

| AQ-ID  | Topic                        | Question                                                                 | Raised By        | Date       | Status   |
|--------|------------------------------|--------------------------------------------------------------------------|------------------|------------|----------|
| AQ-003 | Region SOFT-READ consumers   | Which modules consume ORG_REGION via SOFT-READ? What is the impact       | ORG MODE 1.5     | 2026-06-16 | DEFERRED |
|        |                              | of Region deactivation on those consumers? (OQ-001 escalation — ARCH-8) | (OQ-001 escalation) |         |          |
| AQ-006 | Registry version mismatch   | registry-security.md v2.4.0 cites "Architecture decision LOCKED         | Registry Builder | 2026-07-09 | OPEN     |
|        |                              | (master-registry v2.9.0)" for SEC_USER_PROFILE / SEC_ROLE_BRANCH        | (analysis session)|           |          |
|        |                              | (§8.1–8.2), but this registry is v2.7.5 and has no record of that       |                  |            |          |
|        |                              | decision. Is there an intermediate registry version not yet supplied,   |                  |            |          |
|        |                              | or is the security document's version citation in error?                |                  |            |          |
| AQ-007 | Registry version mismatch   | registry-update-blocks-SEC.md targets a version bump v2.9.0 → v2.10.0,  | Registry Builder | 2026-07-09 | OPEN     |
|        | (extension, linked to AQ-006)| but this registry is v2.7.5 — there is no record of v2.8.0 or v2.9.0    | (analysis session)|           |          |
|        |                              | between them. Were those two versions lost in upload, or is the         |                  |            |          |
|        |                              | v2.9.0 citation (also underlying AQ-006) wrong from the start? Linked   |                  |            |          |
|        |                              | to AQ-006 — do not close AQ-006 until AQ-007 is resolved.               |                  |            |          |
| AQ-010 | SMS provider selection      | Which SMS provider (Twilio / Unifonic / local provider)? Not            | ARCH-REF-1.8 P0  | 2026-07-11 | OPEN     |
|        | (NotificationService)       | architecture-blocking — provider lives in NOTIF_CHANNEL_CONFIG          | session          |            |          |
|        |                              | .config_json, structure is provider-agnostic. Needed before P3          |                  |            |          |
|        |                              | (Execution Plan) writes the actual B2 API integration.                  |                  |            |          |
| AQ-011 | WhatsApp Business API       | Meta Cloud API directly, or via a BSP (Twilio / 360dialog)? Same        | ARCH-REF-1.8 P0  | 2026-07-11 | OPEN     |
|        | provider selection          | non-blocking status as AQ-010 — needed before P3 writes                 | session          |            |          |
|        | (NotificationService)       | WhatsAppChannelService integration.                                     |                  |            |          |

Note: AQ-003 is non-blocking. Resolves automatically when the first consuming
module (TBD) runs its own MODE 1.5 session and declares its XM dependency on Region.

Note: AQ-006 is non-blocking for Security's PERMANENT EXCEPTION status but
should be resolved before SEC_USER_PROFILE / SEC_ROLE_BRANCH are treated as
having a LOCKED architecture decision in this registry.

Previously resolved questions:

| AQ-ID  | Topic                  | Resolution                                                  | Date       |
|--------|------------------------|-------------------------------------------------------------|------------|
| AQ-001 | Naming Standard        | RESOLVED — lookupKey stays AS-IS permanently. No rename.    | 2026-05-24 |
| AQ-002 | extraValue Max Length  | RESOLVED — kept AS-IS permanently. No change required.      | 2026-05-24 |
| AQ-008 | SMS Phase-1 timing     | RESOLVED — SMS fully implemented in Phase 1 (not deferred). | 2026-07-11 |
|        | (NotificationService)  | See Conflict #21. Provider selection itself remains open    |            |
|        |                        | as AQ-010 (non-blocking).                                    |            |
| AQ-009 | Notification channel   | RESOLVED — union of master-registry §8 and ARCH-REF-1.8      | 2026-07-11 |
|        | list mismatch          | lists adopted: Email/SMS/WhatsApp/Push/Internal (5 channels). |            |
|        | (NotificationService)  | See Conflict #21.                                             |            |
| AQ-012 | Push (Firebase) channel| RESOLVED — no deferral for any channel. Push is fully         | 2026-07-11 |
|        | phase confirmation     | implemented in Phase 1, same as Email/SMS/WhatsApp/Internal.  |            |
|        | (NotificationService)  | All 5 channels ship enabled (is_enabled_fl = 1).               |            |

New questions raised during analysis must follow this format before being added:

| AQ-ID  | Topic | Question | Raised By | Date | Status |
|--------|-------|----------|-----------|------|--------|
| AQ-XXX | —     | —        | —         | —    | OPEN   |

=====================================================
15. P0 Architecture Convergence Status
=====================================================

Rule: No P1 session begins for any module until its row shows
      READY, PARTIALLY_READY, or EXCEPTION in this table.
Rule: BLOCKED = P1 prohibited until all linked AQ-IDs are CLOSED.
Rule: EXCEPTION modules are pre-existing — P0 reads them, does not process them.
Rule: This table is updated by P0 REGISTRY UPDATE BLOCKS only.

| Module              | Layer | Step  | P0 Date    | Readiness          | module-registry files                                    | Open AQ-IDs        |
|---------------------|-------|-------|------------|--------------------|----------------------------------------------------------|--------------------|
| Organization        | L1    | L1-1  | 2026-06-16 | READY ✓            | registry-srs-ORG.md / registry-db-ORG.md / registry-exec-ORG.md | AQ-003 (DEFERRED) |
| Security            | L1    | L1-2  | EXCEPTION  | EXCEPTION ⚠️ (core) / PARTIALLY_READY ⚠️ (extension scope — unblocked, Conflict #20 CLOSED) | registry-security.md v2.4.1 | AQ-006, AQ-007 (OPEN, non-blocking) |
| MasterData          | L1    | L1-3  | EXCEPTION  | PARTIAL EXCEP ⚠️   | — (Lookup AS-IS per Section 4)                           | —                  |
| CurrencyCalendar    | L1    | L1-3  | —          | NOT STARTED        | —                                                        | —                  |
| NumberingEngine     | L1    | L1-4  | —          | NOT STARTED        | —                                                        | —                  |
| IntegrationService  | L1    | L1-3  | —          | NOT STARTED        | —                                                        | —                  |
| FileService         | L1    | L1-3  | 2026-07-11 | READY ✓            | module-registry-file.md / business-policies-file.md / ARCH-REF-1.10-FILE-SERVICE.md v1.1.0 | — |
| NotificationService | L1    | L1-4  | 2026-07-11 | PARTIALLY_READY ⚠️ | module-registry-notif.md / business-policies-notif.md / ARCH-REF-1.8-NOTIFICATION-SERVICE.md v1.1.0 | AQ-010, AQ-011 (OPEN, non-blocking) |
| AuditService        | L1    | L1-4  | —          | NOT STARTED        | —                                                        | —                  |
| PricingEngine       | L2    | L2-1  | —          | NOT STARTED        | —                                                        | —                  |
| TaxEngine           | L2    | L2-2  | —          | NOT STARTED        | —                                                        | —                  |
| CalculationEngine   | L2    | L2-3  | —          | NOT STARTED        | —                                                        | —                  |
| Procurement         | L3    | L3-1  | —          | NOT STARTED        | —                                                        | —                  |
| Inventory           | L3    | L3-2  | —          | NOT STARTED        | —                                                        | —                  |
| Sales               | L3    | L3-3  | —          | NOT STARTED        | —                                                        | —                  |
| Finance             | L3    | L3-4  | —          | NOT STARTED        | —                                                        | —                  |
| Reporting           | L4    | L4-1  | —          | NOT STARTED        | —                                                        | —                  |

Readiness States:
  NOT STARTED      : P0 session not yet run for this module
  READY ✓          : All clear — P1 may begin
  PARTIALLY_READY ⚠️: INF-IDs present — P1 begins with OQ-IDs
  BLOCKED ✗        : Open AQ-IDs exist — P1 prohibited
  EXCEPTION ⚠️     : Pre-existing module — used AS-IS per Section 4
  PARTIAL EXCEP ⚠️ : Module partially implemented — AS-IS parts noted

Note on AQ-003 and Organization READY status:
  AQ-003 is DEFERRED and non-blocking. Organization P1 may proceed.
  AQ-003 resolves when the first Region-consuming module runs MODE 1.5.

Note on FileService readiness (added 2026-07-11):
  READY ✓ — P0 architecture convergence complete, no open AQ-IDs.
  ARCH-REF-1.10's Oracle-specific decisions (BLOB, UCP) adapted to
  PostgreSQL (BYTEA, HikariCP — Conflict #24); PDFBox deferred with an
  explicit reopening condition rather than silently dropped. FileService
  has no HARD dependency on any ungoverned module — Organization/
  Security are both usable now (Organization READY, Security EXCEPTION).
  This does not automatically close NotificationService's XM-NOTIF-[TBD]
  →FILE_DOCUMENT (Conflict #22) — the RXE-NOTIF migration trigger fires
  at FileService's MODE 1.5 gate (DBS-ID confirmed), not at this P0
  completion. P1/P2 must still run for FileService before that RXE fires.

Note on NotificationService readiness (added 2026-07-11):
  PARTIALLY_READY ⚠️ — P0 architecture convergence complete
  (module-registry-notif.md + business-policies-notif.md produced).
  Not BLOCKED: the original HARD-FK on FileService (1.10) for templates
  was downgraded to DEFERRED via an architectural workaround (inline
  template storage — Conflict #22), with a standard RXE-based migration
  path (RXE-NOTIF-[SEQ], CONTRACT-8) documented for when FileService
  becomes governed. Open AQ-010/011/012 are non-blocking technical/
  confirmation items (SMS & WhatsApp provider selection; Push channel
  phase confirmation) — P1 may begin for this module.
  Five notification channels confirmed: Email/SMS/WhatsApp/Push/Internal
  ALL fully implemented Phase 1 (2026-07-11 — "no deferral for any
  channel"). Remaining open items (AQ-010/011) are technical provider
  choices for SMS/WhatsApp, not phase/deferral questions — they do not
  affect whether a channel sends. Channel selection per event is owned
  exclusively by the publishing business module via channelHint, never
  inferred by Notification itself (Conflict #23).
Note on Security's split readiness (added 2026-07-09, updated 2026-07-11):
  Security's CORE (USERS/ROLES/PERMISSIONS/SEC_PAGES/REFRESH_TOKENS) remains
  EXCEPTION ⚠️ — used AS-IS per Section 4, unaffected by this note.
  Security's EXTENSION scope (DataScope: SEC_USER_PROFILE/SEC_ROLE_BRANCH;
  Forgot Password; Sign Up — see registry-security.md §8) is new development
  under an EXCEPTION-status module, tracked as PARTIALLY_READY ⚠️.
  BLK-SEC-002 / Conflict #20 — RESOLVED AND CLOSED 2026-07-11 (Section 13):
  the apparent Security(1.2)↔NotificationService(1.8) circular dependency
  is resolved because the two directions are different dependency types,
  not a true cycle — NotificationService→Security is HARD-FK (USERS data,
  build-order), while Security→NotificationService (Forgot Password) is
  Event-Based (publish-only via RabbitMQ, no build-order coupling), the
  same pattern every other module already uses to reach Notification.
  Security's extension scope is no longer blocked on this item. Remaining
  open items for the extension scope are AQ-006/AQ-007 only (registry
  version-citation mismatch — non-blocking, does not affect this scope).
