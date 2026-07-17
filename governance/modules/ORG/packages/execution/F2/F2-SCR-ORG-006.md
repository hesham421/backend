<!-- Source: PHASE:F2 / SUB:SCR-ORG-006 -->
<!-- Context: see F2-HEADER.md for phase-level strategy, registry table, and intro -->

  ### F2-SERVICE — ProfitCenterService
  Service class       : `ProfitCenterService`
  Methods              : create, search, update, deactivate, activate, getById — mapped to API-ORG-033..038
  Observable type       : `Observable<ProfitCenter>` (create/update/getById/activate) · `Observable<Page<ProfitCenter>>` (search) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001, 0002, 0003, 0004 — no entity-specific deactivate guard (SRS A6: no internal dependency constraint on ProfitCenter)
  Loading state          : LOCAL
  Caching strategy       : NONE
  XM-ID impact            : None outbound — referenced by XM-INBOUND-STUB-1 (future Finance consumer), no impact on this service's own contract

  ### F2-FACADE — ProfitCenterFacade (SCR-ORG-006)
  Standard search/entry state shape, mirrors LegalEntityFacade.
