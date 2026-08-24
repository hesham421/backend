# MASTER-REGISTRY.md

=====================================================
1. Project Information
=====================================================

- Project Name    : Enterprise Engine Platform
- Version         : 2.11.0
- Last Updated    : 2026-08-24
- Maintained By   : System Architect
- Governance Level: LOCKED

LOCKED means:
- No entity, lookup, or rule may be overridden or ignored
- Any conflict MUST be flagged and stopped immediately
- No new entity may be created without checking this registry first
- Any structural change requires a version increment and architect approval

Platform Vision:
- Five layers: Foundation → Engines → Operations → Reporting → Applications
- Every module follows the same internal pattern: Infrastructure → Domain → Application → Interface
- Build reusable enterprise capabilities once — reuse everywhere

PERMANENT EXCEPTION Policy:
Any module implemented before this registry was established is used AS-IS — no code changes.
Applies to: Security module / MasterData Lookup feature.
Future modules MUST follow registry standards without exception.

=====================================================
2. Platform Layer Structure
=====================================================

LAYER-1 : Foundation          — Core platform (Security, Org, MasterData, Files, Notifications)
LAYER-2 : Smart Engines       — Pricing, Tax, Calculation
LAYER-3 : Operational Modules — Procurement, Inventory, Sales, Finance
LAYER-4 : Reporting           — Reads from all layers, writes to none
LAYER-5 : Applications        — Commercial ERP, Industrial ERP, etc.

Rules:
- No module may depend on a module in a higher layer number
- Layer-1 modules are built first — they block everything else
- Dependency arrows always point downward (consumer → provider)

Build Sequence:
| Step | Modules (parallel within step)                                    | Gate               |
|------|-------------------------------------------------------------------|--------------------|
| L1-1 | Organization                                                      | None               |
| L1-2 | Security                                                          | L1-1 complete      |
| L1-3 | MasterData / CurrencyCalendar / IntegrationService / FileService  | L1-2 complete      |
| L1-4 | NumberingEngine / NotificationService / AuditService              | L1-3 complete      |
| L2-1 | PricingEngine                                                     | L1 complete        |
| L2-2 | TaxEngine                                                         | L2-1 complete      |
| L2-3 | CalculationEngine                                                 | L2-2 complete      |
| L3-1 | Procurement                                                       | L2 complete        |
| L3-2 | Inventory                                                         | L3-1 complete      |
| L3-3 | Sales                                                             | L3-2 complete      |
| L3-4 | Finance                                                           | L3-1+L3-2+L3-3     |
| L4-1 | Reporting                                                         | L3 complete        |
| L5-1 | CommercialERP / IndustrialERP / ContractingERP / CustomApplication| L4 complete        |

=====================================================
3. Module Registry
=====================================================

| #    | Module Name         | Status                   | Depends On                              |
|------|---------------------|--------------------------|-----------------------------------------|
| 1.1  | Organization        | GOVERNED ✓ MODE 2        | —                                       |
| 1.2  | Security            | ACTIVE ⚠️ EXCEPTION       | 1.1                                     |
| 1.3  | UserManagement      | Merged → Security        | —                                       |
| 1.4  | MasterData          | Partial Active ⚠️         | 1.1 / 1.2                               |
| 1.5  | CurrencyCalendar    | Planned                  | 1.1 / 1.2                               |
| 1.6  | NumberingEngine     | Planned                  | 1.1 / 1.2 / 1.4                         |
| 1.7  | IntegrationService  | Planned                  | 1.1 / 1.2                               |
| 1.8  | NotificationService | ACTIVE ⚠️ SVCAPI ✓        | 1.1 / 1.2 / 1.10                        |
| 1.9  | AuditService        | Planned                  | 1.1 / 1.2 / 1.10                        |
| 1.10 | FileService         | ACTIVE ⚠️ SVCAPI ✓        | 1.1 / 1.2                               |
| 2.1  | PricingEngine       | Planned                  | L1 complete / 1.4 / 1.5                 |
| 2.2  | TaxEngine           | Planned                  | L1 complete / 2.1                       |
| 2.3  | CalculationEngine   | Planned                  | L1 complete / 2.1 / 2.2                 |
| 3.1  | Procurement         | Planned                  | L1+L2 complete                          |
| 3.2  | Inventory           | Planned                  | L1+L2 complete / 3.1                    |
| 3.3  | Sales               | Planned                  | L1+L2 complete / 3.2                    |
| 3.4  | Finance             | Planned                  | L1+L2 complete / 3.1 / 3.2 / 3.3       |
| 4.1  | Reporting           | Planned                  | All Layers 1-3                          |
| 5.1  | CommercialERP       | Planned                  | All Layers                              |
| 5.2  | IndustrialERP       | Planned                  | All Layers + Extensions                 |
| 5.3  | ContractingERP      | Planned                  | All Layers + Extensions                 |
| 5.4  | CustomApplication   | Planned                  | All Layers + Custom                     |

Status notes:
- GOVERNED ✓ MODE 2: SRS + DB script + Execution plan complete. Next: test-plan.
- ACTIVE ⚠️ EXCEPTION: Implemented before registry — used AS-IS, no code changes ever.
- ACTIVE ⚠️ SVCAPI ✓: Backend live-tested end-to-end. DOC/INT/SEC/ALIGN phases still pending.
- Partial Active ⚠️: Lookup feature ACTIVE (EXCEPTION). Other entities Planned (must follow standard).

Reading note (v2.11.0): "SVCAPI ✓" + "phases pending" means backend is live
and P0 is COMPLETE — the pending phases are later ones (DOC/INT/SEC/ALIGN)
only. NotificationService (1.8) and FileService (1.10) are DONE for
foundation/consumption. Section 12 "READY / PARTIALLY_READY" states the same
fact (P0 done) on a different axis. Do not read "phases pending" as "unfinished."

=====================================================
4. Naming & Data Governance Rules
=====================================================

Standard convention (all NEW modules MUST follow):
- Primary Keys   : end with Pk
- Foreign Keys   : end with Fk
- Dropdown fields: end with Id
- Flag fields    : end with Fl

All entities MUST include: createdAt / createdBy / updatedAt / updatedBy / isActiveFl OR statusId

-----------------------------------------------------
PERMANENT EXCEPTION — Security Module
-----------------------------------------------------

Status: PERMANENT EXCEPTION — used AS-IS — no code changes — ever.
Scope : All Security entities and their column names.

Security entities (actual names — NOT a template):

| DB Table                 | Java Entity            | PK                         | Status        |
|--------------------------|------------------------|----------------------------|---------------|
| USERS                    | UserAccount            | USERS_PK                   | ACTIVE ⚠️     |
| ROLES                    | Role                   | ROLES_PK                   | ACTIVE ⚠️     |
| PERMISSIONS              | Permission             | PERMISSIONS_PK             | ACTIVE ⚠️     |
| SEC_PAGES                | Page                   | SEC_PAGES_PK (SEQ)         | ACTIVE ⚠️     |
| REFRESH_TOKENS           | RefreshToken           | REFRESH_TOKENS_PK          | ACTIVE ⚠️     |
| USER_ROLES               | (join)                 | USER_ID_FK + ROLE_ID_FK    | ACTIVE ⚠️     |
| ROLE_PERMISSIONS         | (join)                 | ROLE_ID_FK + PERM_ID_FK    | ACTIVE ⚠️     |
| SEC_USER_PROFILE         | SecUserProfile         | USER_ID_FK (1:1 with USERS)| ACTIVE ⚠️ EXT |
| SEC_ROLE_BRANCH          | SecRoleBranch          | ROLE_ID_FK + BRANCH_ID_FK  | ACTIVE ⚠️ EXT |
| PASSWORD_RESET_TOKEN     | PasswordResetToken     | TOKEN_PK                   | ACTIVE ⚠️ EXT |
| ACCOUNT_ACTIVATION_TOKEN | AccountActivationToken | TOKEN_PK                   | ACTIVE ⚠️ EXT |

Security actual naming deviations:
- PKs: USERS_PK / ROLES_PK / PERMISSIONS_PK / REFRESH_TOKENS_PK / SEC_PAGES_PK
- FKs: USER_ID_FK / ROLE_ID_FK / PERM_ID_FK / PAGE_ID_FK / BRANCH_ID_FK
- Flags: ENABLED / IS_ACTIVE / REVOKED / USED_FL (not standard *Fl suffix)
- Role.roleCode and Role.description are persisted columns (ROLE_CODE / DESCRIPTION on ROLES)
- SEC_USER_PROFILE.BRANCH_ID_FK → ORG_BRANCH(BRANCH_PK) — HARD cross-module FK
- SEC_ROLE_BRANCH: composite PK (ROLE_ID_FK, BRANCH_ID_FK), DATA_ACCESS_LEVEL validated via LOV-SEC-002
- TOKEN tables: no AuditableEntity — plain @Data @Builder, USED_FL only
- System is permanently single-tenant — no TENANT_ID column exists

Consumption rule: Any module referencing Security entities MUST use the actual column names above.

-----------------------------------------------------
PERMANENT EXCEPTION — MasterData Lookup
-----------------------------------------------------

Status: PERMANENT EXCEPTION — used AS-IS — no code changes — ever.
Scope : MD_MASTER_LOOKUP and MD_LOOKUP_DETAIL only.

| DB Column           | Standard Name   | Table            | Notes                          |
|---------------------|-----------------|------------------|--------------------------------|
| id_pk               | entityPk        | Both             | Auto-generated PK              |
| lookup_key          | lookupCode      | MD_MASTER_LOOKUP | UPPERCASE, immutable           |
| code                | detailCode      | MD_LOOKUP_DETAIL | Unique per parent, immutable   |
| name_ar / name_en   | detailValueAr/En| MD_LOOKUP_DETAIL | Display values                 |
| master_lookup_id_fk | masterLookupFk  | MD_LOOKUP_DETAIL | FK to MD_MASTER_LOOKUP         |
| is_active           | isActiveFl      | Both             | SMALLINT 0/1                   |
| extra_value         | —               | MD_LOOKUP_DETAIL | Flexible value field           |

Consumption rule:
- Consume via API ONLY: GET /api/lookups/{lookupKey}?active=true
- MUST NOT query MD_MASTER_LOOKUP or MD_LOOKUP_DETAIL directly
- lookup_key is the stable contract — id_pk is internal and MUST NOT be exposed

=====================================================
5. Entity Registry
=====================================================

⚠️ = Permanent Exception  ✓ = Governed (DBS-ID registered)

LAYER-1 — Active Entities:

| Entity                   | DB Table                  | Owner               | Status           |
|--------------------------|---------------------------|---------------------|------------------|
| LegalEntity              | ORG_LEGAL_ENTITY ✓        | Organization        | GOVERNED ✓       |
| Branch                   | ORG_BRANCH ✓              | Organization        | GOVERNED ✓       |
| Department               | ORG_DEPARTMENT ✓          | Organization        | GOVERNED ✓       |
| CostCenter               | ORG_COST_CENTER ✓         | Organization        | GOVERNED ✓       |
| ProfitCenter             | ORG_PROFIT_CENTER ✓       | Organization        | GOVERNED ✓       |
| Region                   | ORG_REGION ✓              | Organization        | GOVERNED ✓       |
| LocationSite             | ORG_LOCATION_SITE ✓       | Organization        | GOVERNED ✓       |
| User                     | USERS ⚠️                  | Security            | ACTIVE ⚠️        |
| Role                     | ROLES ⚠️                  | Security            | ACTIVE ⚠️        |
| Permission               | PERMISSIONS ⚠️            | Security            | ACTIVE ⚠️        |
| Page                     | SEC_PAGES ⚠️              | Security            | ACTIVE ⚠️        |
| RefreshToken             | REFRESH_TOKENS ⚠️         | Security            | ACTIVE ⚠️        |
| UserRole                 | USER_ROLES ⚠️             | Security            | ACTIVE ⚠️        |
| RolePermission           | ROLE_PERMISSIONS ⚠️       | Security            | ACTIVE ⚠️        |
| SecUserProfile           | SEC_USER_PROFILE ⚠️       | Security            | ACTIVE ⚠️ EXT    |
| SecRoleBranch            | SEC_ROLE_BRANCH ⚠️        | Security            | ACTIVE ⚠️ EXT    |
| PasswordResetToken       | PASSWORD_RESET_TOKEN ⚠️   | Security            | ACTIVE ⚠️ EXT    |
| AccountActivationToken   | ACCOUNT_ACTIVATION_TOKEN ⚠️| Security           | ACTIVE ⚠️ EXT    |
| MdMasterLookup           | MD_MASTER_LOOKUP ⚠️       | MasterData          | ACTIVE ⚠️        |
| MdLookupDetail           | MD_LOOKUP_DETAIL ⚠️       | MasterData          | ACTIVE ⚠️        |
| FileDocument             | FILE_DOCUMENT ✓           | FileService         | ACTIVE ✓ SVCAPI  |
| FileCategory             | FILE_CATEGORY ✓           | FileService         | ACTIVE ✓ SVCAPI  |
| Notification             | NOTIF_LOG ✓               | NotificationService | ACTIVE ✓ SVCAPI  |
| NotificationTemplate     | NOTIF_TEMPLATE ✓          | NotificationService | ACTIVE ✓ SVCAPI  |
| NotificationChannelConfig| NOTIF_CHANNEL_CONFIG ✓    | NotificationService | ACTIVE ✓ SVCAPI  |

LAYER-1 — Planned Entities:

| Entity           | Owner            | Status  |
|------------------|------------------|---------|
| Item             | MasterData       | Planned |
| Customer         | MasterData       | Planned |
| Vendor           | MasterData       | Planned |
| UnitOfMeasure    | MasterData       | Planned |
| Country          | MasterData       | Planned |
| Currency         | CurrencyCalendar | Planned |
| ExchangeRate     | CurrencyCalendar | Planned |
| FiscalYear       | CurrencyCalendar | Planned |
| FiscalPeriod     | CurrencyCalendar | Planned |
| NumberingPattern | NumberingEngine  | Planned |
| AuditLog         | AuditService     | Planned |

LAYER-2 — Planned Entities:

| Entity      | Owner         |
|-------------|---------------|
| PriceList   | PricingEngine |
| Discount    | PricingEngine |
| TaxType     | TaxEngine     |
| TaxRule     | TaxEngine     |
| TaxExemption| TaxEngine     |

LAYER-3 — Planned Entities:

| Entity            | Owner       |
|-------------------|-------------|
| PurchaseRequest   | Procurement |
| PurchaseOrder     | Procurement |
| GoodsReceipt      | Procurement |
| VendorInvoice     | Procurement |
| StockMovement     | Inventory   |
| Warehouse         | Inventory   |
| StockCount        | Inventory   |
| SalesQuotation    | Sales       |
| SalesOrder        | Sales       |
| Delivery          | Sales       |
| CustomerInvoice   | Sales       |
| ChartOfAccounts   | Finance     |
| JournalEntry      | Finance     |
| AccountPayable    | Finance     |
| AccountReceivable | Finance     |
| Payment           | Finance     |

=====================================================
6. Shared Lookup & Reference Tables
=====================================================

| Lookup Key           | Type      | Owner               | Values / Notes                              |
|----------------------|-----------|---------------------|---------------------------------------------|
| Status               | Lookup    | Core                | <= 15 values                                |
| DocumentType         | Lookup    | Core                | <= 15 values                                |
| PaymentTerms         | Reference | MasterData          | > 15 values (Reference Table)               |
| ItemType             | Lookup    | MasterData          | <= 15 values                                |
| TaxType              | Lookup    | TaxEngine           | <= 15 values                                |
| CurrencyType         | Lookup    | CurrencyCalendar    | <= 15 values                                |
| MovementType         | Lookup    | Inventory           | <= 15 values                                |
| PriceListType        | Lookup    | PricingEngine       | <= 15 values                                |
| NotificationChannel  | Lookup    | NotificationService | <= 15 values                                |
| NotificationStatus   | Lookup    | NotificationService | <= 15 values                                |
| FileType             | Lookup    | FileService         | <= 15 values                                |
| FileStatus           | Lookup    | FileService         | <= 15 values                                |
| ScopeLevel           | Lookup    | Security            | <= 15 values                                |
| DATA_ACCESS_LEVEL    | Lookup    | Security            | BRANCH_ONLY / BRANCH_AND_CHILDREN / ALL     |
| LEGAL_ENTITY_TYPE    | Lookup    | Organization        | <= 15 values                                |
| BRANCH_TYPE          | Lookup    | Organization        | <= 15 values                                |
| DEPARTMENT_NODE_TYPE | Lookup    | Organization        | <= 15 values                                |
| COST_CENTER_NODE_TYPE| Lookup    | Organization        | <= 15 values                                |
| COST_CENTER_TYPE     | Lookup    | Organization        | <= 15 values                                |
| LOCATION_SITE_TYPE   | Lookup    | Organization        | <= 15 values                                |
| REGION_TYPE          | Reference | Organization        | > 15 values — GEOGRAPHIC/SALES/OPERATIONAL  |

Rules:
- <= 15 values → MD_LOOKUP_DETAIL (Lookup)
- >  15 values → Reference Table (LOV)
- All modules consume via API: GET /api/lookups/{lookupKey}?active=true
- No module may create its own dropdown or enum table

=====================================================
7. Module Dependencies
=====================================================

| Module              | Direct Dependencies                                          |
|---------------------|--------------------------------------------------------------|
| Organization        | None — root module                                           |
| Security            | Organization (+ NotificationService EVENT-BASED for ForgotPw)|
| MasterData          | Organization / Security                                      |
| CurrencyCalendar    | Organization / Security                                      |
| NumberingEngine     | Organization / Security / MasterData                         |
| IntegrationService  | Organization / Security                                      |
| FileService         | Organization / Security                                      |
| NotificationService | Organization / Security / FileService                        |
| AuditService        | Organization / Security / FileService                        |
| PricingEngine       | Layer-1 complete / MasterData / CurrencyCalendar             |
| TaxEngine           | Layer-1 complete / PricingEngine                             |
| CalculationEngine   | Layer-1 complete / PricingEngine / TaxEngine                 |
| Procurement         | Layer-1+2 complete                                           |
| Inventory           | Layer-1+2 complete / Procurement                             |
| Sales               | Layer-1+2 complete / Inventory                               |
| Finance             | Layer-1+2 complete / Procurement / Inventory / Sales         |
| Reporting           | Layer-1+2+3 complete                                         |
| Applications (5.x)  | All Layers complete                                          |

Note: Security→NotificationService (Forgot Password) is EVENT-BASED (publish-only, no build-order
coupling). Not a circular dependency — different direction types (see Conflict #20, CLOSED).

=====================================================
8. Cross-Module Integration Rules
=====================================================

FINANCIAL:
- Finance is the central accounting module
- All financial impacts MUST produce a JournalEntry in Finance
- CostCenter MUST be referenced in every financial transaction

DATA SCOPE:
- Security defines data scope — not the module
- Every module MUST enforce DataScope at query level
- DataScope levels: Platform / LegalEntity / Branch / Department

NUMBERING:
- Every document MUST get its number from NumberingEngine
- No module may implement its own numbering logic
- Pattern: [Prefix]-[LegalEntity]-[Branch]-[Year]-[Sequence]

AUDIT:
- Every state change MUST produce an AuditLog entry
- Captures: entityType / entityId / fieldName / oldValue / newValue / userId / timestamp

NOTIFICATION:
- Active channels: Email ✅ / InApp ✅ — SMS ❌ / WhatsApp ❌ / Push ❌ (disabled, no free provider)
- Email provider: Gmail SMTP — spring.mail.host=smtp.gmail.com / port=587 / STARTTLS
- Gmail setup: Google Account → Security → 2-Step Verification ON → App Passwords
- channelHint is decided by the PUBLISHING module — NotificationService never infers channel
- Two ingress paths: REST (POST /api/v1/notifications/send) or Spring Event (NotificationRequestedEvent)
- RabbitMQ path documented in CORE.md is NOT YET IMPLEMENTED — do not use
- No module may query NOTIF_LOG / NOTIF_TEMPLATE / NOTIF_CHANNEL_CONFIG directly

FILE:
- All attachments MUST be stored via FileService (token-gated flow — not plain CRUD)
- Flow: POST /upload-token → POST /upload/{token} → POST /access-token → GET /download/{token}
- DELETE requires PERM_SYSTEM_ADMIN (not PERM_FILE_ATTACHMENT_DELETE)
- No module may query FILE_DOCUMENT / FILE_CATEGORY directly

LOOKUP:
- All dropdown fields MUST reference MD_LOOKUP_DETAIL
- Consume via API only: GET /api/lookups/{lookupKey}?active=true
- No Java ENUMs for lookup values

CROSS-MODULE FK:
- Must end with Fk suffix
- Validated at service layer — NOT enforced by DB-level foreign key constraints
- Exception: Security's cross-module FKs DO have DB-level constraints (PERMANENT EXCEPTION)

FILE CATEGORY:
- FileCategory has no Create/Update API — DB-seeded reference table only
- Each consuming module seeds its own FILE_CATEGORY rows via migration

=====================================================
9. Internal Module Pattern (Mandatory)
=====================================================

Every module MUST follow this internal structure:

  Layer-4 : Interface Layer      (Thin Controller — no logic)
  Layer-3 : Application Layer    (Use Case per operation)
  Layer-2 : Domain Layer         (Entity + Validator — no technology)
  Layer-1 : Infrastructure Layer (Repository per entity)

Rules:
- Domain Layer MUST NOT know about Database or API
- Infrastructure Layer MUST NOT contain business logic
- Application Layer MUST NOT contain business rules
- One Use Case per operation — no shared Use Cases

=====================================================
10. Data Ownership
=====================================================

| Entity                   | DB Table                  | Owner               | Editable By              |
|--------------------------|---------------------------|---------------------|--------------------------|
| MD_MASTER_LOOKUP         | MD_MASTER_LOOKUP ⚠️       | MasterData          | Admin Only               |
| MD_LOOKUP_DETAIL         | MD_LOOKUP_DETAIL ⚠️       | MasterData          | Admin Only               |
| LegalEntity              | ORG_LEGAL_ENTITY ✓        | Organization        | Admin Only               |
| Branch                   | ORG_BRANCH ✓              | Organization        | Admin Only               |
| Department               | ORG_DEPARTMENT ✓          | Organization        | Admin Only               |
| CostCenter               | ORG_COST_CENTER ✓         | Organization        | Admin Only               |
| ProfitCenter             | ORG_PROFIT_CENTER ✓       | Organization        | Admin Only               |
| Region                   | ORG_REGION ✓              | Organization        | Admin Only               |
| LocationSite             | ORG_LOCATION_SITE ✓       | Organization        | Admin Only               |
| User                     | USERS ⚠️                  | Security            | Admin Only               |
| Role                     | ROLES ⚠️                  | Security            | Admin Only               |
| Permission               | PERMISSIONS ⚠️            | Security            | Admin Only               |
| Page                     | SEC_PAGES ⚠️              | Security            | Admin Only               |
| SecUserProfile           | SEC_USER_PROFILE ⚠️       | Security            | Admin / User (own)       |
| SecRoleBranch            | SEC_ROLE_BRANCH ⚠️        | Security            | Admin Only               |
| PasswordResetToken       | PASSWORD_RESET_TOKEN ⚠️   | Security            | System Only              |
| AccountActivationToken   | ACCOUNT_ACTIVATION_TOKEN ⚠️| Security           | System Only              |
| Currency                 | (planned)                 | CurrencyCalendar    | Admin Only               |
| ExchangeRate             | (planned)                 | CurrencyCalendar    | Admin / Finance          |
| FiscalYear               | (planned)                 | CurrencyCalendar    | Admin / Finance          |
| Item                     | (planned)                 | MasterData          | Admin / Inventory        |
| Customer                 | (planned)                 | MasterData          | Admin / Sales            |
| Vendor                   | (planned)                 | MasterData          | Admin / Procurement      |
| NumberingPattern         | (planned)                 | NumberingEngine     | Admin Only               |
| ChartOfAccounts          | (planned)                 | Finance             | Admin / Finance          |

Rules:
- Each entity has a single owner module
- Cross-module updates MUST respect ownership
- Admin always has override access

=====================================================
11. Open Architectural Questions
=====================================================

| AQ-ID  | Topic                      | Question / Status                                                        | Status   |
|--------|----------------------------|--------------------------------------------------------------------------|----------|
| AQ-003 | Region SOFT-READ consumers | Which modules consume ORG_REGION? Impact of deactivation on consumers?   | DEFERRED |
|        |                            | Non-blocking. Resolves when first consuming module runs MODE 1.5.        |          |

No other open questions. All previous AQs resolved.

=====================================================
12. P0 Architecture Convergence Status
=====================================================

Rule: No P1 session begins until row shows READY / PARTIALLY_READY / EXCEPTION.

| Module              | Layer | Step | P0 Date    | Readiness                                         | Source Files                          |
|---------------------|-------|------|------------|---------------------------------------------------|---------------------------------------|
| Organization        | L1    | L1-1 | 2026-06-16 | READY ✓                                           | registry-srs/db/exec-ORG.md           |
| Security            | L1    | L1-2 | EXCEPTION  | EXCEPTION ⚠️ (core) / PARTIALLY_READY ⚠️ (ext)   | registry-security.md v2.5.0           |
| MasterData          | L1    | L1-3 | EXCEPTION  | PARTIAL EXCEP ⚠️                                  | — (Lookup AS-IS per Section 4)        |
| FileService         | L1    | L1-3 | 2026-07-11 | READY ✓                                           | module-registry-file.md / ARCH-REF-1.10 v1.1.0 |
| NotificationService | L1    | L1-4 | 2026-07-11 | PARTIALLY_READY ⚠️                                | module-registry-notif.md / ARCH-REF-1.8 v1.1.0 |
| CurrencyCalendar    | L1    | L1-3 | —          | NOT STARTED                                       | —                                     |
| NumberingEngine     | L1    | L1-4 | —          | NOT STARTED                                       | —                                     |
| IntegrationService  | L1    | L1-3 | —          | NOT STARTED                                       | —                                     |
| AuditService        | L1    | L1-4 | —          | NOT STARTED                                       | —                                     |
| PricingEngine       | L2    | L2-1 | —          | NOT STARTED                                       | —                                     |
| TaxEngine           | L2    | L2-2 | —          | NOT STARTED                                       | —                                     |
| CalculationEngine   | L2    | L2-3 | —          | NOT STARTED                                       | —                                     |
| Procurement         | L3    | L3-1 | —          | NOT STARTED                                       | —                                     |
| Inventory           | L3    | L3-2 | —          | NOT STARTED                                       | —                                     |
| Sales               | L3    | L3-3 | —          | NOT STARTED                                       | —                                     |
| Finance             | L3    | L3-4 | —          | NOT STARTED                                       | —                                     |
| Reporting           | L4    | L4-1 | —          | NOT STARTED                                       | —                                     |

Readiness States:
- NOT STARTED    : P0 session not yet run
- READY ✓        : All clear — P1 may begin
- PARTIALLY_READY: P1 may begin, with open non-blocking items
- BLOCKED ✗      : Open AQ-IDs exist — P1 prohibited
- EXCEPTION ⚠️   : Pre-existing module — used AS-IS per Section 4
- PARTIAL EXCEP  : Module partially implemented — AS-IS parts noted
=====================================================
13. Progress Snapshot (2026-08-24)
=====================================================

Navigational summary only — Sections 1-12 govern. Records where the work
stands so a future session resumes without re-deriving state.

DONE (P0 complete):
  1.1  Organization        — GOVERNED ✓ MODE 2
  1.2  Security            — EXCEPTION ⚠️ (AS-IS; GL_* permission seeds present)
  1.10 FileService         — P0 complete + backend live (SVCAPI ✓)
  1.8  NotificationService — P0 complete + backend live. Active channel:
                             Email ONLY (Gmail SMTP). SMS / WhatsApp / Push
                             NOT enabled now (confirmed 2026-08-24 — matches
                             Section 8). The module-registry-notification.md /
                             business-policies-notification.md "all-5-channels"
                             wording is superseded and must be corrected there.
  1.4  MasterData          — Lookup only (EXCEPTION); rest Planned

CURRENT TRACK — Finance / General Accounts (3.4): STARTED.
  Built as a unified, reusable General-Ledger module; first consumer is an
  external project via a DEFERRED integration layer (architect plan pending).
  Prerequisites — built first, kept as SEPARATE modules (NO merge):
    • CurrencyCalendar (1.5) — Currency / ExchangeRate / FiscalYear /
      FiscalPeriod. Finance CONSUMES these; it does not own them.
    • NumberingEngine (1.6) — journal / payment document numbers
      (Section 8 hard rule: no module numbers its own documents).
  Consumed AS-IS / mandatory to Finance:
    • Security GL_* permission seeds (AS-IS).
    • Cost Centers (ORG 1.1) — mandatory on every financial transaction (Section 8).
  Deferred (do NOT build yet):
    • External-project integration layer.
    • Auto-posting FROM this platform's own 3.1 / 3.2 / 3.3 — incl.
      VendorInvoice (3.1-owned) and CustomerInvoice (3.3-owned) — until those exist.
  Sequencing: starting 3.4 ahead of the standard L2 + 3.1/3.2/3.3 gate is an
  architect-approved deviation. Module numbering, ownership and separation
  are unchanged — nothing is merged.

=====================================================