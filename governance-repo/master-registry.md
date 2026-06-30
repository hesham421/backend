# MASTER-REGISTRY.md

=====================================================
1. Project Information
=====================================================

- Project Name  : Enterprise Engine Platform
- Version       : 2.7.4
- Last Updated  : 2026-06-28
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

Approved Amendment (2026-06-21):
The Security module's "no code changes ever" exception was amended once, by
explicit architect decision, to permit removal of the unused TENANT_ID column
system-wide (multi-tenancy cleanup). This is a single approved exception — no
other code changes to Security are authorized. See Section 13, Conflict #17.

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
| 1.8  | NotificationService  | Core   | Yes    | Planned                 | 1.1 / 1.2 / 1.10        |
| 1.9  | AuditService         | Core   | Yes    | Planned                 | 1.1 / 1.2 / 1.10        |
| 1.10 | FileService          | Core   | Yes    | Planned                 | 1.1 / 1.2               |

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
| Primary Key     | ends with Pk            | ID (USERS, ROLES, PERMISSIONS, REFRESH_TOKENS)  |
| Primary Key     | ends with Pk            | ID_PK (SEC_PAGES) ✅                            |
| Foreign Key     | ends with Fk            | USER_ID, ROLE_ID, PERM_ID                       |
| Foreign Key     | ends with Fk            | PAGE_ID_FK, PARENT_ID_FK ✅                     |
| Flag Field      | ends with Fl            | ENABLED, IS_ACTIVE, REVOKED                     |
| Dropdown Field  | ends with Id            | PERMISSION_TYPE (stored as VARCHAR Enum)        |
| DataScope       | Separate entity         | TENANT_ID column REMOVED system-wide (2026-06-21) — multi-tenancy eliminated, system permanently single-tenant. Code-level removal confirmed (7 tenant-related classes deleted from erp-common-utils; all 5 Security entities now extend AuditableEntity directly; tenant-scoped unique constraints renamed to single-column; JWT tenant claim removed). Physical DB column drop via `remove_tenant_id.sql` — pending manual DBA execution. Supersedes prior "INACTIVE-but-present" status. See Conflict #17. |
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
| Attachment           | (planned)               | FileService          | Planned             | All Operational Modules                |
| AuditLog             | (planned)               | AuditService         | Planned             | All                                    |
| Notification         | (planned)               | NotificationService  | Planned             | All                                    |
| NotificationTemplate | (planned)               | NotificationService  | Planned             | All                                    |

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
| FileType            | Lookup         | Lookup Details  | FileService         | <= 15      |
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
- Notification channels: InApp / Email / SMS / WhatsApp
- Templates MUST be defined per event type
- NotificationService owns delivery — modules only publish events

FILE RULES:
- All attachments MUST be stored via FileService
- No module may store files directly
- Every attachment MUST be linked to an entity (entityType + entityId)

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

| 2.7.1   | 2026-06-19 | Security TENANT_ID status corrected in Section 4.            | Architect   |
|         |            | TENANT_ID exists in all Security tables (inherited from      |             |
|         |            | TenantAuditableEntity) but is INACTIVE — not used in any     |             |
|         |            | query — system is single-tenant. Source: registry-security   |             |
|         |            | .md v1.1.0. Section 15: registry-security.md registered.    |             |

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

| 2.7.2   | 2026-06-21 | TENANT_ID REMOVED system-wide — Security PERMANENT           | Architect   |
|         |            | EXCEPTION amended (one-time, architect-approved) to permit   |             |
|         |            | this code change. 7 tenant classes deleted from              |             |
|         |            | erp-common-utils; Security entities now extend               |             |
|         |            | AuditableEntity directly; tenant-scoped unique constraints    |             |
|         |            | renamed to single-column; JWT tenant claim removed.           |             |
|         |            | Physical column drop (remove_tenant_id.sql) pending manual    |             |
|         |            | DBA execution. Section 4: TENANT_ID row corrected (was        |             |
|         |            | INACTIVE-but-present, now REMOVED). Section 13: Conflict #17  |             |
|         |            | added (CLOSED) — supersedes Conflict #4. Section 14: AQ-004   |             |
|         |            | added — RESOLVED. Section 15: source updated to               |             |
|         |            | registry-security.md v2.0.0. Source: registry-security.md     |             |
|         |            | v2.0.0 (Updated By: Claude Code).                              |             |
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
| 4  | DataScope not implemented as entity             | Security                       | TENANT_ID used — permanent acceptable pattern (SUPERSEDED — see #17)| CLOSED    |
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
| 17 | TENANT_ID removal vs Security "no code changes  | Security                       | CONFIRMED by architect (2026-06-21) — TENANT_ID column      | CLOSED    |
|    | ever" PERMANENT EXCEPTION policy                |                                 | eliminated system-wide as an approved, one-time exception    |           |
|    |                                                  |                                 | to the PERMANENT EXCEPTION policy. Supersedes Conflict #4.   |           |
|    |                                                  |                                 | Source: registry-security.md v2.0.0. Physical DB column drop |           |
|    |                                                  |                                 | still pending manual DBA execution of remove_tenant_id.sql.  |           |

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

Note: AQ-003 is non-blocking. Resolves automatically when the first consuming
module (TBD) runs its own MODE 1.5 session and declares its XM dependency on Region.

Previously resolved questions:

| AQ-ID  | Topic                  | Resolution                                                  | Date       |
|--------|------------------------|-------------------------------------------------------------|------------|
| AQ-001 | Naming Standard        | RESOLVED — lookupKey stays AS-IS permanently. No rename.    | 2026-05-24 |
| AQ-002 | extraValue Max Length  | RESOLVED — kept AS-IS permanently. No change required.      | 2026-05-24 |
| AQ-004 | TENANT_ID removal vs   | RESOLVED — architect confirmed TENANT_ID removal as an      | 2026-06-21 |
|        | PERMANENT EXCEPTION    | approved one-time exception to "no code changes ever."      |            |
|        |                        | See Conflict #17 (Section 13).                               |            |

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
| Security            | L1    | L1-2  | EXCEPTION  | EXCEPTION ⚠️       | registry-security.md v2.0.0                              | —                  |
| MasterData          | L1    | L1-3  | EXCEPTION  | PARTIAL EXCEP ⚠️   | — (Lookup AS-IS per Section 4)                           | —                  |
| CurrencyCalendar    | L1    | L1-3  | —          | NOT STARTED        | —                                                        | —                  |
| NumberingEngine     | L1    | L1-4  | —          | NOT STARTED        | —                                                        | —                  |
| IntegrationService  | L1    | L1-3  | —          | NOT STARTED        | —                                                        | —                  |
| FileService         | L1    | L1-3  | —          | NOT STARTED        | —                                                        | —                  |
| NotificationService | L1    | L1-4  | —          | NOT STARTED        | —                                                        | —                  |
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
