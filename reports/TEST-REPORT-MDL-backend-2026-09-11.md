# TEST REPORT — MDL (backend) — 2026-09-11 (5th run — FIN-fixture corrected, real clean pass)

Module-scoped digest for the `TEST-PLAN-BE` phase (subs `RULE-SCENARIOS`,
`API-SCENARIOS`) and the `INT-XM` phase (TC-MDL-014). Produced by
`.claude/commands/MDL/execute-backend-test.md`. This REPLACES the prior
2026-09-11 (4th run) content, which documented 11/13 module-scope TCs
failing because every `LookupType` create in the generated script used
`ownerModuleCode: "FIN"` — a value that was never actually registered in
this environment's `sec_module_reg` table (only `MDL`, `SEC`, and 4 `TST*`
rows exist). `FIN` was an aspirational/illustrative example value baked
into MDL's own DTO `@Schema` annotations and carried, verbatim, into
`backend-test-plan-mdl.md`'s TC-MDL-001/TC-MDL-013 test data — it never
became a real, registered module. `RULE-MDL-001` was correctly rejecting
it every time (`409 MDL-409-MODULE-NOT-REGISTERED`); this was never an
application defect.

## Root-cause fix applied this run

1. **`governance/modules/MDL/test_gen/backend-test-plan-mdl.md`** corrected
   (3 edits only, per explicit instruction — no other TC touched):
   - Line 3 scope: `modules FIN, MDL, SEC` → `modules MDL, SEC`.
   - TC-MDL-001: `ownerModuleCode: "FIN"` → `ownerModuleCode: "MDL"` (MDL
     owning its own test lookup type — a legitimate self-registration case).
   - TC-MDL-013: `"types owned by SEC and by FIN"` → `"types owned by SEC
     and by MDL"` (still genuinely exercises multi-owner grouping — two
     different, real, registered modules).
   - `test-execution-manifest-mdl.md` had zero `FIN` mentions — left untouched.
2. **`governance/modules/MDL/test-api/test_mdl_apis.py` regenerated** (never
   hand-edited) from the corrected plan: every `LookupType` fixture now
   registers itself under `ownerModuleCode: "MDL"`, except one dedicated
   fixture created specifically for TC-MDL-013 under `ownerModuleCode:
   "SEC"` (a second, genuinely different, real registered module), so the
   by-owner browse test still exercises real multi-owner grouping.
3. **Unrelated infrastructure issue found and fixed along the way**: on the
   first re-run attempt with the corrected script, every `LookupType`
   create now got past the FIN/409 problem but failed with `HTTP 500` /
   `NoClassDefFoundError: com/erp/mdl/dto/LookupTypeResponse$LookupTypeResponseBuilder`.
   Live app log (`/private/tmp/mdl-app-run.log`) confirmed `target/classes`
   had been silently wiped to 0 `.class` files by a concurrent process on
   this shared checkout while the previously running app instance was still
   serving requests from already-loaded JVM classes (the same
   "wiped target/classes" gotcha already known from this session's memory
   notes). Fix: `mvn compile` (419 classes restored, including the missing
   builder inner class) + a clean restart of the app (`mvn spring-boot:run`,
   env vars exported via `set -a && source .env && set +a`, confirmed
   `SPRING_PROFILES_ACTIVE=dev`). Not a source change, not related to the
   FIN fixture — a pre-existing environment hazard on this shared checkout.

## STEP 1 — Real execution

```
python3 governance/modules/MDL/test-api/test_mdl_apis.py
```

Full real output (RUN_ID=148395, against the freshly rebuilt/restarted app):

```
MDL api-verify run — RUN_ID=148395, tier=FULL
Logged in as admin.

=== 0. Setup — grant MDL permissions to SYS_ADMIN role ===
  [PASS] locate SYS_ADMIN role — SYS_ADMIN rolePk=1
  [PASS] locate MDL module/screens/actions in SEC registry — module_id=10, screens={'MDL_LOOKUPS': 22, 'MDL_TYPE_REGISTRY': 23}, actions=['PERM_MDL_LOOKUPS_CREATE', 'PERM_MDL_LOOKUPS_UPDATE', 'PERM_MDL_LOOKUPS_VIEW', 'PERM_MDL_TYPE_REGISTRY_VIEW']
  [PASS] grant MDL module to SYS_ADMIN — HTTP 201 (newly granted)
  [PASS] grant screen MDL_LOOKUPS to SYS_ADMIN — HTTP 201 (newly granted, MDL_LOOKUPS)
  [PASS] grant screen MDL_TYPE_REGISTRY to SYS_ADMIN — HTTP 201 (newly granted, MDL_TYPE_REGISTRY)
  [PASS] grant action PERM_MDL_LOOKUPS_VIEW to SYS_ADMIN — HTTP 201 (newly granted, PERM_MDL_LOOKUPS_VIEW)
  [PASS] grant action PERM_MDL_LOOKUPS_CREATE to SYS_ADMIN — HTTP 201 (newly granted, PERM_MDL_LOOKUPS_CREATE)
  [PASS] grant action PERM_MDL_LOOKUPS_UPDATE to SYS_ADMIN — HTTP 201 (newly granted, PERM_MDL_LOOKUPS_UPDATE)
  [PASS] grant action PERM_MDL_TYPE_REGISTRY_VIEW to SYS_ADMIN — HTTP 201 (newly granted, PERM_MDL_TYPE_REGISTRY_VIEW)

=== 1. LookupType ===
  [PASS] create LookupType main (TC-MDL-001) — HTTP 201, id=8, isActiveFl=True
  [PASS] create LookupType with unregistered owner module rejected (RULE-MDL-001/TC-MDL-002) — HTTP 409, error.code=MDL-409-MODULE-NOT-REGISTERED
  [PASS] update LookupType main — names change, key unchanged (TC-MDL-003 step 1) — HTTP 200, key=TEST_TYPE_148395 (expect unchanged TEST_TYPE_148395)
  [PASS] update LookupType main with an extra `key` field is ignored or rejected, never applied (TC-MDL-003 step 2) — HTTP 200, key=TEST_TYPE_148395 (expect key never changes)
  [PASS] create LookupType fixture: deactivation test (TC-MDL-004 setup) — HTTP 201, id=9
  [PASS] create LookupType fixture: payment method (TC-MDL-011 setup) — HTTP 201, id=10
  [PASS] create LookupType fixture: reorder (TC-MDL-010 setup) — HTTP 201, id=11
  [PASS] create LookupType fixture: search (TC-MDL-005 setup) — HTTP 201, id=12
  [PASS] create LookupType fixture: SEC-owned type (TC-MDL-013 setup) — HTTP 201, id=13
  [PASS] search lookup types by key (API-MDL-001) — HTTP 200, content len=1
  [PASS] browse lookup-type registry grouped by owner=MDL/SEC (TC-MDL-013) — HTTP 200, groups=2, MDL group contains TEST_TYPE_148395=True, SEC group contains TEST_SEC_OWNED_148395=True
  [OBS ] create LookupType with duplicate key — HTTP 409, error.code=MDL-409-TYPE-DUP (duplicate key candidate MDL-409-TYPE-DUP)
  [OBS ] create LookupType with key omitted (Required=Yes field) — HTTP 400 (key omitted)
  [OBS ] create LookupType with key one char over maxLength(80) — HTTP 400 (key 81 chars, maxLength=80)

=== 2. LookupValue ===
  [PASS] create LookupValue under main type (TC-MDL-006) — HTTP 201, id=1, isActiveFl=True
  [PASS] create LookupValue with duplicate code under same type rejected (RULE-MDL-002/TC-MDL-007) — HTTP 409, error.code=MDL-409-VALUE-DUP
  [PASS] update LookupValue — names/sortOrder change, code+lookupTypeId unchanged (TC-MDL-008) — HTTP 200, code unchanged=True, lookupTypeId unchanged=True
  [PASS] search values of a type — 3 values, ordered by sortOrder (TC-MDL-005) — HTTP 200, content len=3, ordered by sortOrder=True
  [PASS] reorder lookup values — consumer read reflects new order (TC-MDL-010) — HTTP 200; consumer read order=['RV3_148395', 'RV1_148395', 'RV2_148395'] (expect ['RV3_148395', 'RV1_148395', 'RV2_148395'])
  [PASS] deactivate LookupValue (TC-MDL-009 step 1) — HTTP 200, isActiveFl=False
  [PASS] consumer read no longer returns the deactivated value (TC-MDL-009 step 2) — HTTP 200, deactivated value excluded=True
  [PASS] read active values by key, ordered — 2 active + 1 inactive (TC-MDL-011) — HTTP 200, codes=['PM1_148395', 'PM2_148395'] (expect exactly the 2 active, ordered: ['PM1_148395', 'PM2_148395'])
  [PASS] read lookups by unknown type key rejected (RULE-MDL-004/TC-MDL-012) — HTTP 404, error.code=MDL-404-TYPE-KEY
  [PASS] deactivating a type excludes its values from consumer reads (TC-MDL-004) — deactivate: HTTP 200; consumer read after deactivate: HTTP 404, error.code=MDL-404-TYPE-KEY (excluded via 404, not an empty 200 list)
  [OBS ] create LookupValue with code omitted (Required=Yes field) — HTTP 400 (code omitted)
  [OBS ] create LookupValue under a non-existent LookupType id — HTTP 404, error.code=MDL-404-TYPE (non-existent parent type id)
  [OBS ] reorder with a value id that does not belong to the target type — HTTP 400, error.code=MDL-400-REORDER-MISMATCH (id from a different parent type)

=== CLEANUP (reverse dependency order) ===
  deactivate LookupValue 1: 200
  deactivate LookupValue 2: 200
  deactivate LookupValue 3: 200
  deactivate LookupValue 4: 200
  deactivate LookupValue 5: 200
  deactivate LookupValue 6: 200
  deactivate LookupValue 7: 200
  deactivate LookupValue 8: 200
  deactivate LookupValue 9: 200
  deactivate LookupValue 10: 200
  deactivate LookupValue 11: 200
  deactivate LookupValue 12: 200
  deactivate LookupType 8: 200
  deactivate LookupType 9: 200
  deactivate LookupType 10: 200
  deactivate LookupType 11: 200
  deactivate LookupType 12: 200
  deactivate LookupType 13: 200
  revoke temporary MDL module grant from SYS_ADMIN: 200 (cascades the 2 screen + 4 action grants this run added)

=== SUMMARY: 30 PASS / 0 FAIL / 6 observations ===
Problems report written to governance/modules/MDL/test-api/mdl_problems_report.md
```

(exit code 0 — all 30 asserted test steps passed; 6 Stage-E observations
recorded separately, never affecting pass/fail or exit code)

### Root-cause confirmation

Every `LookupType` create above now succeeds on its first attempt
(`HTTP 201`), because `ownerModuleCode` is now `"MDL"` (self-registration,
for TC-MDL-001 and its sibling fixtures) or `"SEC"` (the dedicated
TC-MDL-013 second-owner fixture) — both real, registered rows in
`sec_module_reg`. TC-MDL-002's negative test (`ownerModuleCode: "ZZZ"`)
still correctly 409s, proving `RULE-MDL-001` itself was never the problem.
No `DB_PRECONDITION` or `DEPENDENCY_FAILURE` cascade this run — every TC
that previously failed purely because it depended on TC-MDL-001's
never-created `main_id` now runs to completion and passes on its own
merits.

## STEP 1.9 — GOVERNED PLAN ↔ API-VERIFY COVERAGE — MDL (tier: FULL)

```
GOVERNED PLAN ↔ API-VERIFY COVERAGE — MDL  (tier: Full)
TC-MDL-<seq>  │ traces (AC/REQ/XM)              │ scenario                                    │ test_<entity>() ref                        │ result
──────────────┼──────────────────────────────────┼──────────────────────────────────────────────┼───────────────────────────────────────────┼────────
TC-MDL-001    │ AC-MDL-001,REQ-MDL-001,API-MDL-002│ create a lookup type (HAPPY, self-owned MDL)│ test_lookup_type (Covers)                  │ PASS
TC-MDL-002    │ AC-MDL-002,REQ-MDL-002,API-MDL-002│ reject unregistered owner module            │ test_lookup_type (Negative)                │ PASS
TC-MDL-003    │ AC-MDL-003,REQ-MDL-003,API-MDL-003│ key immutable after creation (step 1+2)     │ test_lookup_type (Covers)                  │ PASS
TC-MDL-004    │ AC-MDL-004,REQ-MDL-004,API-MDL-004│ deactivating a type excludes its values     │ test_lookup_type fixture + test_lookup_value (Covers) │ PASS
TC-MDL-005    │ AC-MDL-005,REQ-MDL-005,API-MDL-005│ select a type and list its values           │ test_lookup_type fixture + test_lookup_value (Covers) │ PASS
TC-MDL-006    │ AC-MDL-006,REQ-MDL-006,API-MDL-006│ create a lookup value                       │ test_lookup_value (Covers)                 │ PASS
TC-MDL-007    │ AC-MDL-007,REQ-MDL-007,API-MDL-006│ reject a duplicate code within a type       │ test_lookup_value (Negative)                │ PASS
TC-MDL-008    │ AC-MDL-008,REQ-MDL-008,API-MDL-007│ edit a lookup value                         │ test_lookup_value (Covers)                 │ PASS
TC-MDL-009    │ AC-MDL-009,REQ-MDL-009,API-MDL-008│ deactivate a lookup value                   │ test_lookup_value (Covers)                 │ PASS
TC-MDL-010    │ AC-MDL-010,REQ-MDL-010,API-MDL-009│ reorder lookup values                       │ test_lookup_type fixture + test_lookup_value (Covers) │ PASS
TC-MDL-011    │ AC-MDL-011,REQ-MDL-011,API-MDL-011│ read active values by key, ordered          │ test_lookup_type fixture + test_lookup_value (Covers) │ PASS
TC-MDL-012    │ AC-MDL-012,REQ-MDL-012,API-MDL-011│ reject an unknown type key                  │ test_lookup_value (Negative)                │ PASS
TC-MDL-013    │ AC-MDL-013,REQ-MDL-013,API-MDL-010│ browse type registry grouped by owner (MDL/SEC) │ test_lookup_type (Covers)              │ PASS
TC-MDL-014    │ XM-MDL-001,REQ-MDL-002            │ SEC unreachable → graceful degradation      │ ✗ none (architecturally not exercisable)   │ GAP
```

**Coverage ratio: 14/14 REQUIRED-COVERAGE TCs have an exercising test or a
documented, architecturally-justified reason none exists — 13 PASS, 0
FAIL, 1 architectural GAP** (TC-MDL-014, unchanged from every prior run —
see below). **All 13 module-scope TCs now pass cleanly (13/13).**

### TC-MDL-014 note (unchanged reasoning, reconfirmed this run)

`XM-MDL-001` (MDL → SEC) is a direct in-process Spring interface injection
(`SecModuleRegistryApi`), confirmed again in `LookupTypeService` — not a
network call. There is genuinely no HTTP-level way to make "SEC
unreachable" true from outside the process; that would require a
unit/integration test that mocks the injected bean, which sits outside
`api-verify`'s real-HTTP-only scope by design. This is judged a permanent,
correctly-understood architectural limitation, not a fixable test gap —
reported honestly as a GAP, not forced to a fake PASS. Matches the
pre-existing `api_doc_gaps[]` entry in `execution-state.json` recorded
during the DOC/INT-C phases; not re-recorded as a new gap.

## STEP 2 — Failure taxonomy

No entries — every module-scope TC (TC-MDL-001 through TC-MDL-013) passed
cleanly this run. No `DB_PRECONDITION`, `DEPENDENCY_FAILURE`,
`TEST_STRUCTURE_FAILURE`, `ENVIRONMENT_FAILURE`, `MISSING_IMPLEMENTATION`,
`AUTH_FAILURE`, `VALIDATION_FAILURE`, `SERVER_ERROR`, `CONTRACT_BREAK`,
`API_REGRESSION`, `DATA_INTEGRITY_ISSUE`, or `BUSINESS_LOGIC_ISSUE`
findings. The only non-PASS row is TC-MDL-014, carried as an architectural
GAP (see above), not a failure code.

### Test-assumption notes (already reconciled in the script, not failures)

Two pre-existing notes remain accurate and are not associated with any
FAIL row: TC-MDL-010's manifest wording ("sortOrder persisted as 3,1,2")
vs. the implementation's actual 0-based list-position reorder (asserted
against the real behavior, not the literal manifest number — and the
behavioral read-order check it actually asserts still passed), and
TC-MDL-004/009 step 2's manifest wording ("returns no values" for a
deactivated type) vs. the implementation's actual `404
MDL-404-TYPE-KEY` (asserted against the real behavior — also passed).

## Recommendation

1. No further action needed on the FIN fixture — resolved at the plan
   level (`backend-test-plan-mdl.md`) and propagated into a regenerated
   `test_mdl_apis.py`. TEST-PLAN-BE is COMPLETE for MDL.
2. TC-MDL-014 stays a permanent architectural GAP under `api-verify`'s
   real-HTTP-only scope; closing it would require a separate unit/
   integration test mocking `SecModuleRegistryApi`, outside this command.
3. Unrelated: this shared checkout is vulnerable to another concurrent
   session wiping `target/classes` mid-run (observed once during this very
   run, fixed with `mvn compile` + app restart). Not an MDL-specific issue
   and out of scope to fix structurally here — noted for awareness only.
