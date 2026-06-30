<!-- Source: PHASE:F2 / SUB:SCR-ORG-002 -->

  ### F2-SERVICE — BranchService
  Service class       : `BranchService`
  Methods              : create, search, update, deactivate, activate, getById — mapped to API-ORG-007..012
  Observable type       : `Observable<Branch>` (create/update/getById/activate) · `Observable<Page<Branch>>` (search) · `Observable<void>` (deactivate/activate)
  Error handling         : ERR-ORG-0001, 0002, 0003, 0004, 0007, 0008, 0009 (deactivate guards), 0015 (RULE-ORG-018 inactive LegalEntity)
  Loading state          : LOCAL
  Caching strategy       : NONE
  XM-ID impact            : None — depends on LegalEntityService.search for FK picker (intra-module entity reference, not a LOV, not an XM-ID)

  ### F2-FACADE — BranchFacade (SCR-ORG-002)
  Same state/operation shape as LegalEntityFacade, additionally exposes legalEntityOptions$ for the FK dropdown.
