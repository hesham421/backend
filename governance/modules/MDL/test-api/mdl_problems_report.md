# MDL api-verify problems report

Run: 223695  ·  Tier: FULL

**Totals: 30 PASS / 0 FAIL** (observations excluded)

## Suites
### 0. Setup — grant MDL permissions to SYS_ADMIN role — 9 pass / 0 fail
- [PASS] locate SYS_ADMIN role — SYS_ADMIN rolePk=1
- [PASS] locate MDL module/screens/actions in SEC registry — module_id=2, screens={'MDL_LOOKUPS': 10, 'MDL_TYPE_REGISTRY': 11}, actions=['PERM_MDL_LOOKUPS_CREATE', 'PERM_MDL_LOOKUPS_UPDATE', 'PERM_MDL_LOOKUPS_VIEW', 'PERM_MDL_TYPE_REGISTRY_VIEW']
- [PASS] grant MDL module to SYS_ADMIN — HTTP 409 SEC-409-GRANT-DUP (already granted)
- [PASS] grant screen MDL_LOOKUPS to SYS_ADMIN — HTTP 409 SEC-409-GRANT-DUP (already granted, MDL_LOOKUPS)
- [PASS] grant screen MDL_TYPE_REGISTRY to SYS_ADMIN — HTTP 409 SEC-409-GRANT-DUP (already granted, MDL_TYPE_REGISTRY)
- [PASS] grant action PERM_MDL_LOOKUPS_VIEW to SYS_ADMIN — HTTP 409 SEC-409-GRANT-DUP (already granted, PERM_MDL_LOOKUPS_VIEW)
- [PASS] grant action PERM_MDL_LOOKUPS_CREATE to SYS_ADMIN — HTTP 409 SEC-409-GRANT-DUP (already granted, PERM_MDL_LOOKUPS_CREATE)
- [PASS] grant action PERM_MDL_LOOKUPS_UPDATE to SYS_ADMIN — HTTP 409 SEC-409-GRANT-DUP (already granted, PERM_MDL_LOOKUPS_UPDATE)
- [PASS] grant action PERM_MDL_TYPE_REGISTRY_VIEW to SYS_ADMIN — HTTP 409 SEC-409-GRANT-DUP (already granted, PERM_MDL_TYPE_REGISTRY_VIEW)
### 1. LookupType — 11 pass / 0 fail
- [PASS] create LookupType main (TC-MDL-001) — HTTP 201, id=56, isActiveFl=True
- [PASS] create LookupType with unregistered owner module rejected (RULE-MDL-001/TC-MDL-002) — HTTP 409, error.code=MDL-409-MODULE-NOT-REGISTERED
- [PASS] update LookupType main — names change, key unchanged (TC-MDL-003 step 1) — HTTP 200, key=TEST_TYPE_223695 (expect unchanged TEST_TYPE_223695)
- [PASS] update LookupType main with an extra `key` field is ignored or rejected, never applied (TC-MDL-003 step 2) — HTTP 200, key=TEST_TYPE_223695 (expect key never changes)
- [PASS] create LookupType fixture: deactivation test (TC-MDL-004 setup) — HTTP 201, id=57
- [PASS] create LookupType fixture: payment method (TC-MDL-011 setup) — HTTP 201, id=58
- [PASS] create LookupType fixture: reorder (TC-MDL-010 setup) — HTTP 201, id=59
- [PASS] create LookupType fixture: search (TC-MDL-005 setup) — HTTP 201, id=60
- [PASS] create LookupType fixture: SEC-owned type (TC-MDL-013 setup) — HTTP 201, id=61
- [PASS] search lookup types by key (API-MDL-001) — HTTP 200, content len=1
- [PASS] browse lookup-type registry grouped by owner=MDL/SEC (TC-MDL-013) — HTTP 200, groups=5, MDL group contains TEST_TYPE_223695=True, SEC group contains TEST_SEC_OWNED_223695=True
### 2. LookupValue — 10 pass / 0 fail
- [PASS] create LookupValue under main type (TC-MDL-006) — HTTP 201, id=188, isActiveFl=True
- [PASS] create LookupValue with duplicate code under same type rejected (RULE-MDL-002/TC-MDL-007) — HTTP 409, error.code=MDL-409-VALUE-DUP
- [PASS] update LookupValue — names/sortOrder change, code+lookupTypeId unchanged (TC-MDL-008) — HTTP 200, code unchanged=True, lookupTypeId unchanged=True
- [PASS] search values of a type — 3 values, ordered by sortOrder (TC-MDL-005) — HTTP 200, content len=3, ordered by sortOrder=True
- [PASS] reorder lookup values — consumer read reflects new order (TC-MDL-010) — HTTP 200; consumer read order=['RV3_223695', 'RV1_223695', 'RV2_223695'] (expect ['RV3_223695', 'RV1_223695', 'RV2_223695'])
- [PASS] deactivate LookupValue (TC-MDL-009 step 1) — HTTP 200, isActiveFl=False
- [PASS] consumer read no longer returns the deactivated value (TC-MDL-009 step 2) — HTTP 200, deactivated value excluded=True
- [PASS] read active values by key, ordered — 2 active + 1 inactive (TC-MDL-011) — HTTP 200, codes=['PM1_223695', 'PM2_223695'] (expect exactly the 2 active, ordered: ['PM1_223695', 'PM2_223695'])
- [PASS] read lookups by unknown type key rejected (RULE-MDL-004/TC-MDL-012) — HTTP 404, error.code=MDL-404-TYPE-KEY
- [PASS] deactivating a type excludes its values from consumer reads (TC-MDL-004) — deactivate: HTTP 200; consumer read after deactivate: HTTP 404, error.code=MDL-404-TYPE-KEY (excluded via 404, not an empty 200 list)

## Observations (Stage E, never pass/fail)
- create LookupType with duplicate key — HTTP 409, error.code=MDL-409-TYPE-DUP (duplicate key candidate MDL-409-TYPE-DUP)
- create LookupType with key omitted (Required=Yes field) — HTTP 400 (key omitted)
- create LookupType with key one char over maxLength(80) — HTTP 400 (key 81 chars, maxLength=80)
- create LookupValue with code omitted (Required=Yes field) — HTTP 400 (code omitted)
- create LookupValue under a non-existent LookupType id — HTTP 404, error.code=MDL-404-TYPE (non-existent parent type id)
- reorder with a value id that does not belong to the target type — HTTP 400, error.code=MDL-400-REORDER-MISMATCH (id from a different parent type)

## Problems (bucketed)
### Likely real bug
- none
### Test assumption mismatch
- TC-MDL-010: manifest text says "sortOrder persisted as 3,1,2"; actual implementation (LookupValueService#reorder) assigns 0-based list position — not asserted literally, the behavioral read-order check is asserted instead.
- TC-MDL-004/009 step 2: manifest text says the deactivated/excluded value(s) "no longer returned" / "returns no values"; for a deactivated TYPE this surfaces as 404 MDL-404-TYPE-KEY (same path as an unknown key), not a 200 empty array — asserted against the actual source-confirmed behavior.
### Infrastructure
- none

## TC-MDL-014 (INT-XM) — not exercised, documented reason
XM-MDL-001 (MDL -> SEC module-registry check) is implemented as a direct in-process Spring bean injection (`SecModuleRegistryApi`), confirmed in `LookupTypeService` — not a network call. There is no HTTP-level way for this script to make 'SEC unreachable' true; that would require a unit/integration test that mocks the injected bean, which is outside api-verify's real-HTTP-only scope. This matches the pre-existing `api_doc_gaps[]` entry in execution-state.json recorded during the DOC/INT-C phases. TC-MDL-014 is reported as a GAP, not a fabricated PASS.