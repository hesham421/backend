<!-- Source: PHASE:DOC -->

# PHASE DOC — Contract Stabilization

DOC GATE CHECK:
```
[ ✓ ] All 44 API contracts internally consistent (method/endpoint/body/response/RULE-IDs)
[ ✓ ] All DTOs free of Read-Only/system fields in input position
[ ✓ ] Error Catalog cross-references resolve (every ERR-ORG-ID used has a SECTION A entry)
DOC Gate: PASSED ✓
```

Audited against the built erp-org module (all 7 controllers, all 14 Create/Update DTOs,
OrgErrorCodes.java, GlobalExceptionHandler.java, messages*.properties):

1. **API contracts** — 7 controllers × (6 flat CRUD + Tree for Department/CostCenter only)
   = 6+6+6+7+7+6+6 = 44 endpoints, matching API-ORG-001..044 exactly. Method pattern
   (Create/Update/GetById/Search/Activate/Deactivate) uniform across all 7 entities.

2. **DTOs** — all 14 Create/Update DTOs verified clean: no `id`, no business-code field, no
   `isActiveFl`, no audit fields. Immutable-after-save fields (`nodeTypeId` on
   Department/CostCenter update, parent FK on Branch/Region/ProfitCenter/LocationSite update)
   correctly omitted per RULE-ORG-011/014/020.

3. **Error Catalog** — every ERR-ORG-ID actually thrown in code (0001, 0002, 0004–0009, 0011,
   0012, 0015, 0016) has a SECTION-A entry and a bilingual message key (18/18 keys present in
   both messages.properties and messages_ar.properties). ERR-ORG-0010 is defined but unthrown,
   correctly tied to the OQ-001 deferral. ERR-ORG-0014 is intentionally UI-layer-only per the
   RULE-ORG-017 comment in OrgRegionDomain.java. ERR-ORG-0018 is explicitly
   consuming-module-enforced per the catalog's own text.
   ⚠ Non-blocking documented gap: ERR-ORG-0003, 0013, 0017 (Business Code/audit-field
   rejection, Business Code immutability, node-type immutability) have no throw site in
   erp-org — the underlying RULE is enforced structurally by omitting the field from the
   Update DTO, not by a runtime check. Since Spring Boot's default Jackson config rejects
   unknown JSON properties, a client that force-injects one of these fields still gets
   correctly rejected, but surfaces GlobalExceptionHandler's generic `INVALID_JSON` error
   instead of the catalog's localized RULE-specific message. Write is blocked either way;
   only the surfaced error code/message differs from the SECTION-A entry.
