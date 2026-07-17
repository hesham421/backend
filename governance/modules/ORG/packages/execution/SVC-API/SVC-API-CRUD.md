<!-- Source: PHASE:SVC-API / SUB:CRUD -->
<!-- Context: see SVC-API-HEADER.md for phase-level strategy, registry table, and intro -->

  ### API-ORG-001 — Create LegalEntity
  POST /api/v1/org/legal-entities · Body: nameAr, nameEn, entityTypeId, notes? · Returns: LegalEntity (full)
  Validations: RULE-ORG-012 (code uniqueness — system-generated, no client input), RULE-ORG-013 (NumberingEngine call), RULE-ORG-015 (name uniqueness — global scope), RULE-ORG-016 (reject if payload contains audit fields or legalEntityCode).
  ERR-carry: ERR-ORG-0001 (duplicate name), ERR-ORG-0002 (numbering conflict), ERR-ORG-0003 (audit/code field present in payload).
  Service flow: validate → call NumberingEngine (DRV-ORG-008) → map DTO→Entity (no audit fields set) → save → map Entity→Response DTO.

  ### API-ORG-002 — Search LegalEntity
  GET /api/v1/org/legal-entities?legalEntityCode&nameAr&nameEn&entityTypeId&isActiveFl&page&size · Returns: Page<LegalEntity>
  MANDATORY-J-2: empty result set → HTTP 200 with empty content array, never 404.

  ### API-ORG-003 — Update LegalEntity
  PUT /api/v1/org/legal-entities/{id} · Body: nameAr?, nameEn?, entityTypeId?, notes? (legalEntityCode excluded from DTO — RULE-ORG-014)
  Validations: RULE-ORG-011 (reject if code present), RULE-ORG-014, RULE-ORG-015, RULE-ORG-016. ERR-carry: ERR-ORG-0001, ERR-ORG-0003, ERR-ORG-0004 (record not found → LocalizedException, not NotFoundException).

  ### API-ORG-004 — Deactivate LegalEntity
  PUT /api/v1/org/legal-entities/{id}/deactivate · Validations: RULE-ORG-001, RULE-ORG-002. ERR-carry: ERR-ORG-0005 (active branches exist), ERR-ORG-0006 (active profit centers exist).

  ### API-ORG-005 — Activate LegalEntity
  PUT /api/v1/org/legal-entities/{id}/activate · No business-rule guard — sets isActiveFl=true.

  ### API-ORG-006 — Get LegalEntity by ID
  GET /api/v1/org/legal-entities/{id} · ERR-carry: ERR-ORG-0004.

  *(API-ORG-007..012 Branch, API-ORG-013..018 Region, API-ORG-019..025 Department, API-ORG-026..032 CostCenter, API-ORG-033..038 ProfitCenter, API-ORG-039..044 LocationSite follow the identical CRUD+Activate/Deactivate+GetById contract shape, with entity-specific validations bound per the RULE-ID columns in PLAN-INDEX API REGISTRY and ERR-ORG-IDs assigned contiguously in SECTION A. Tree-bearing entities additionally expose the GET .../tree endpoint per QR-ORG-012/015.)*
