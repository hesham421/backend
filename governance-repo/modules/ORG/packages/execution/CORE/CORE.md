<!-- Source: PHASE:CORE -->


---

# PHASE CORE — Architecture & Project Standards Declaration

## CORE — Canonical Architecture (MANDATORY — Agent MUST read before writing any code)

### Backend Architecture

```
Layer              │ Class Pattern                        │ Responsibility
───────────────────┼──────────────────────────────────────┼──────────────────────────────────────
Controller         │ [Entity]Controller                   │ HTTP routing, permission check, DTO in/out
Service            │ [Entity]Service / [Entity]ServiceImpl│ Orchestration, business rule enforcement
Mapper             │ [Entity]Mapper (MapStruct)           │ Entity ↔ DTO conversion ONLY — no logic
Domain / Entity    │ [Entity] extends AuditableEntity     │ JPA entity, field declaration, relations
Repository         │ [Entity]Repository extends JpaRepository│ Data access — JPA queries only
```

**Package structure (agent infers exact package from project convention):**
```
com.[project].org
  ├── controller
  ├── service
  ├── mapper
  ├── domain (entity classes)
  └── repository
```

### Frontend Architecture (Angular)

```
Layer       │ Angular Artifact               │ Responsibility
────────────┼────────────────────────────────┼──────────────────────────────────────
Models      │ [entity].model.ts              │ TypeScript interfaces for all DTOs
Services    │ [entity].service.ts            │ HTTP calls only — no state
Facades     │ [entity].facade.ts             │ State ownership + orchestration
Helpers     │ [entity].helper.ts             │ Pure transform/formatting functions
Components  │ [entity]-list / [entity]-form  │ UI rendering + user events only
```

### Entity Base Class

```
ALL entities in this module extend: AuditableEntity
  ✗ TenantAuditableEntity — RETIRED 2026-06-21 — DO NOT USE
  ✗ Do NOT add tenantId column — multi-tenancy eliminated system-wide
AuditEntityListener sets: createdBy, createdAt, updatedBy, updatedAt
  ✗ NEVER set audit fields in Mapper or Service
  ✗ NEVER accept audit fields in Create/Update DTOs
```

### Error Handling Standard

```
Exception type   : LocalizedException (project standard)
  ✗ NotFoundException — BANNED
  ✗ Generic RuntimeException — BANNED for business errors
  ✓ throw new LocalizedException(ErrorCodes.[ERR_CONSTANT], messageAr, messageEn)

ERR-ID 4-point registration (every new ERR-ID must be registered in ALL 4):
  1. ErrorCodes.[ERR_CONSTANT] — constant definition class
  2. messages_ar.properties / messages_en.properties — message catalog
  3. erp-errors.json — frontend error code registry
  4. ErpErrorMapper — HTTP status code mapping
```

### Deactivation Policy (Soft Delete)

```
Deactivation = set isActiveFl = 0 (NOT physical DELETE)
Pre-check sequence (for all entities with dependants):
  1. Check all child/referencing entities for isActiveFl = 1
  2. If any found: throw LocalizedException with RULE-ID message
  3. If none: set isActiveFl = 0 and save
Reactivation = set isActiveFl = 1 (no pre-checks required)
```

### Search / Pagination Standard

```
Backend  : SearchRequest extends BaseSearchContractRequest
           → fields: page (0-based), size, sortBy, sortDir
           → ALLOWED_SORT_FIELDS: Set<String> declared per Service
           → PageableBuilder.from(request, ALLOWED_SORT_FIELDS) builds Pageable
           → Returns: JPA Page<T> — DO NOT create custom pagination wrapper
           → Empty results: HTTP 200 + empty content — NEVER HTTP 404

Frontend : currentPage and pageSize are DERIVED from lastSearchRequest
           ✗ NEVER declare currentPage / pageSize as independent state
```

### NumberingEngine Integration

```
All 7 master entities (ENTITY-ORG-001..007) use NumberingEngine exclusively.
Service layer calls:
  NumberingEngine.generate(entityType, legalEntityFk, branchFk)
  → Returns the formatted business code string
  → Business code is then set on the entity before persist
No module implements its own numbering logic (RULE-ORG-016).
```

### LOV Loading Standard

```
MD_LOOKUP_DETAIL lookups (LOV-ORG-001..006):
  GET /api/lookups/{lookupKey}?active=true
  Stored in DB as DETAIL_CODE (VARCHAR2) — never a numeric FK
  Frontend: load once on screen init → store in facade state as [lovName]Options

RegionType Reference Table (LOV-ORG-007):
  GET /api/v1/org/region-types?active=true (via API-ORG-020)
  Stored in DB as REGION_TYPE_FK NUMBER(10) → FK to ORG_REGION_TYPE.REGION_TYPE_PK
  ⚠ DRV-ORG-001: Region.regionTypeId is stored as FK (NUMBER) not DETAIL_CODE (VARCHAR2)
    This deviates from standard LOV pattern because RegionType is a Reference Table,
    not a MD_LOOKUP_DETAIL entry. Agent must map regionTypeFk as a FK relationship.
```

### Inbound Stubs (INBOUND-STUB)

```
This module is ROOT — zero outbound XM dependencies.
Inbound consumers (other modules reference ORG entities):
  INBOUND-STUB-ORG-001 : Finance module will reference CostCenter / ProfitCenter
                          Status: DEFERRED — Finance module not yet built
  INBOUND-STUB-ORG-002 : Inventory module will reference LocationSite
                          Status: DEFERRED — Inventory module not yet built
These stubs are informational — no implementation required in this module.
```

**CORE Gate: PASSED ✓**

```
[ ✓ ] Backend architecture declared (Controller/Service/Mapper/Domain/Repository)
[ ✓ ] Frontend architecture declared (Models/Services/Facades/Helpers/Components)
[ ✓ ] AuditableEntity base declared — TenantAuditableEntity BANNED
[ ✓ ] LocalizedException declared — NotFoundException BANNED
[ ✓ ] AuditEntityListener declared — never in Mapper/Service
[ ✓ ] Search/Pagination standard declared
[ ✓ ] Deactivation policy declared
[ ✓ ] NumberingEngine integration declared
[ ✓ ] LOV loading standard declared
[ ✓ ] DRV-ORG-001: RegionType FK deviation documented
```

