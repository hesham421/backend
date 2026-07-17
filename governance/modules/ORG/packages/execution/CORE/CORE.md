<!-- Source: PHASE:CORE -->

# PHASE CORE — Architectural Policies & Package Structure

```
Backend  : Controller / Service / Mapper / Domain / Repository / Entity
Frontend : Models / Services / Facades / Helpers / Components
```

Backend package root: `com.[org].erp.org` (module: org)
  - `org.controller` — REST controllers, 1 per SCR-ID-aligned resource (LegalEntityController, BranchController, RegionController, DepartmentController, CostCenterController, ProfitCenterController, LocationSiteController)
  - `org.service` — interface + impl per entity; orchestrates validation (RULE-IDs), NumberingEngine call, Mapper, Repository
  - `org.mapper` — MapStruct or manual Entity↔DTO mapping; never sets audit fields
  - `org.domain` — JPA entities extending `AuditableEntity`
  - `org.repository` — `JpaRepository<Entity, Long>` + Specification/custom query support for search and tree retrieval
  - `org.dto` — Create/Update/Response/Search DTOs per entity (Business Code and audit fields excluded from Create/Update input where Read-Only)
  - `org.exception` — module-specific `LocalizedException` subclasses bound to ERR-ORG-IDs (SECTION A)

Frontend package root: `src/app/org/`
  - `models/` — TS interfaces per entity (mirrors Response DTOs)
  - `services/` — Angular injectable HTTP clients (1 per entity), `Observable<T>` returns (DRV-ORG-013)
  - `facades/` — per-SCR-ID state owners (F2-FACADE, see PHASE F2)
  - `helpers/` — shared validators (e.g., tree-cycle pre-check on UI), formatters
  - `components/` — per-SCR-ID Search component + Entry component (PATTERN-1 separation, MANDATORY-P-2)

Entity base: `AuditableEntity` — uniform across all 8 entities (DRV-ORG-001).
Error signaling: `LocalizedException` — `NotFoundException` BANNED (DRV-ORG-002).
Audit Fields: `AuditEntityListener` — never set in Mapper or Service (DRV-ORG-003 / RULE-ORG-016).
Optimistic Locking: not used — no VERSION column (DRV-ORG-004).
Search/Pagination: `BaseSearchContractRequest` + `ALLOWED_SORT_FIELDS` allow-list per entity; JPA `Page<T>` directive.
Deactivation: `isActiveFl = false` via dedicated activate/deactivate endpoints — usage pre-check enforced per RULE-ORG-001..006 (DRV-ORG-006).
Bilingual: every `ERR-ORG-*`, validation message, and UI-facing string carries AR + EN.
