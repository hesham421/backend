<!-- Source: PHASE:F2 / SUB:SCR-ORG-001 -->

## F2 — SCR-ORG-001 — Legal Entity Screen

```
SCREEN INIT — SCR-ORG-001:
  On init:
    1. Check PERM_LEGAL_ENTITY_VIEW → redirect to /unauthorized if false
    2. Load LOV-ORG-001: GET /api/lookups/LEGAL_ENTITY_TYPE?active=true → store as entityTypeOptions
    3. Execute default search (isActiveFl=1, page=0, size=20) via API-ORG-002

OBSERVABLE TYPES (Angular):
  legalEntities$   : Observable<Page<LegalEntityResponse>>
  entityTypeOptions$ : Observable<LookupDetail[]>
  isLoading$       : Observable<boolean>

F2-FACADE — SCR-ORG-001 — Legal Entity Management
─────────────────────────────────────────────────────────────────
Facade serves   : SCR-ORG-001
Delegates to    : LegalEntityService

STATE THIS FACADE OWNS:
  legalEntityList    — Page<LegalEntityResponse> — search results
  selectedItem       — LegalEntityResponse | null — edit target
  isLoading          — boolean — loading indicator
  lastSearchRequest  — LegalEntitySearchModel — last executed search
                       currentPage and pageSize DERIVED from this — not independent state
  entityTypeOptions  — LookupDetail[] — LOV-ORG-001 (LEGAL_ENTITY_TYPE)

OPERATIONS EXPOSED TO COMPONENT:
  searchLegalEntities(request)  → API-ORG-002 → updates legalEntityList
  getLegalEntityById(id)        → API-ORG-003 → updates selectedItem
  createLegalEntity(data)       → API-ORG-001 → refreshes legalEntityList
  updateLegalEntity(id, data)   → API-ORG-004 → updates selectedItem
  deactivateLegalEntity(id)     → checks usage via response → API-ORG-005
                                  if blocked (ERR-0001/ERR-0002): surfaces error — no confirmation
                                  if allowed: triggers confirmation → proceeds
  reactivateLegalEntity(id)     → API-ORG-006 → refreshes legalEntityList
  loadEntityTypeOptions()       → GET /api/lookups/LEGAL_ENTITY_TYPE?active=true → entityTypeOptions

BOUNDARIES:
  ✓ Component calls facade only — no direct service calls from component
  ✓ Facade calls service only — no direct HTTP from facade
  ✓ currentPage and pageSize derived from lastSearchRequest
─────────────────────────────────────────────────────────────────
```
