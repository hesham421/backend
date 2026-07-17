<!-- Source: PHASE:F2 / SUB:SCR-ORG-001 -->
<!-- Context: see F2-HEADER.md for phase-level strategy, registry table, and intro -->

  ### F2-SERVICE — LegalEntityService
  Service class       : `LegalEntityService`
  Methods              : create(dto), search(params), update(id,dto), deactivate(id), activate(id), getById(id)
  HTTP/Endpoint         : maps 1:1 to API-ORG-001..006
  Observable type       : `Observable<LegalEntity>` (create/update/getById/activate) · `Observable<Page<LegalEntity>>` (search) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001 (name dup), ERR-ORG-0002 (numbering), ERR-ORG-0003 (audit/code in payload), ERR-ORG-0004 (not found), ERR-ORG-0005, ERR-ORG-0006 (deactivate guards) — routed via global HTTP-status error interceptor to inline field errors
  Loading state          : LOCAL (per-operation loading flag on Facade, not GLOBAL spinner)
  Caching strategy       : NONE (default — always re-fetch)
  XM-ID impact            : None — no outbound XM-IDs

  ### F2-FACADE — LegalEntityFacade (SCR-ORG-001)
  State owned: searchResults$ (signal/BehaviorSubject), selectedEntity$, loading$, error$.
  Operations: loadSearch(filters), createEntity(dto), updateEntity(id, dto), toggleActivation(id, isActive). Mediates between LegalEntityComponent and LegalEntityService; never calls HttpClient directly.
