<!-- Source: PHASE:F2 / SUB:SCR-ORG-007 -->
<!-- Context: see F2-HEADER.md for phase-level strategy, registry table, and intro -->

  ### F2-SERVICE — LocationSiteService
  Service class       : `LocationSiteService`
  Methods              : create, search, update, deactivate, activate, getById — mapped to API-ORG-039..044
  Observable type       : `Observable<LocationSite>` (create/update/getById/activate) · `Observable<Page<LocationSite>>` (search) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001, 0003, 0004, 0016 (RULE-ORG-019 inactive Branch) — no entity-specific deactivate guard
  Loading state          : LOCAL
  Caching strategy       : NONE — except siteTypeOptions$ (LOV-ORG-006), which uses the platform-shared lookup cache (SESSION-scoped, owned by the platform SYS module, not this service — non-default but pre-existing platform behavior, not a new deviation requiring its own DRV)
  XM-ID impact            : None outbound — referenced by XM-INBOUND-STUB-2 (future Inventory consumer), no impact on this service's own contract

  ### F2-FACADE — LocationSiteFacade (SCR-ORG-007)
  Standard shape, exposes siteTypeOptions$ (LOV-ORG-006 via platform lookup service) and branchOptions$.
