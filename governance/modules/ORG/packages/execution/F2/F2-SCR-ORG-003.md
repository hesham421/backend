<!-- Source: PHASE:F2 / SUB:SCR-ORG-003 -->
<!-- Context: see F2-HEADER.md for phase-level strategy, registry table, and intro -->

  ### F2-SERVICE — RegionService
  Service class       : `RegionService`
  Methods              : create, search, update, deactivate, activate, getById — mapped to API-ORG-013..018
  Observable type       : `Observable<Region>` (create/update/getById/activate) · `Observable<Page<Region>>` (search) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001, 0002, 0003, 0004, 0010 (RULE-ORG-006), 0014 (RULE-ORG-017 SOFT-READ warning — non-blocking, surfaced as a warning banner not a form error)
  Loading state          : LOCAL
  Caching strategy       : NONE
  XM-ID impact            : None — RULE-ORG-017 warning relates to inbound consumer SOFT-READ (XM-INBOUND-STUB-4), not an outbound XM-ID owned by this service

  ### F2-FACADE — RegionFacade (SCR-ORG-003)
  Exposes legalEntityOptions$, regionTypeOptions$ (from RegionTypeService.search — Admin-only entity, no Deactivate per DRV-ORG-015). Deactivate flow surfaces the RULE-ORG-017 warning banner when present in the response (non-blocking, informational per OQ-001).
