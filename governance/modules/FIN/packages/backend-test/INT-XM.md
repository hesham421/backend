<!-- source: PHASE:INT-XM -->
<!-- traces: API-FIN-002, API-FIN-026, REQ-FIN-001, REQ-FIN-037, REQ-FIN-038, XM-FIN-001, XM-FIN-002 -->
<!-- PHASE:INT-XM:START traces=REQ-FIN-001,REQ-FIN-037,REQ-FIN-038,XM-FIN-001,XM-FIN-002 -->
FIN declares two XM: XM-FIN-001 (SOFT-READ → MDL's lookup values) and XM-FIN-002 (READ → SEC's
user→permission directory, registered at ALIGN-BE for the RULE-FIN-015 fact). MDL and SEC are
both in the current selection, so both are real linking atoms.

<!-- TC:TC-FIN-047:START traces=XM-FIN-001,REQ-FIN-001,API-FIN-002 -->
### TC-FIN-047 — MDL lookup-type not-found is translated into a FIN error code
Derived from : XM-FIN-001 (REQ-FIN-001) · Exercises: API-FIN-002 POST /api/v1/fin/accounts (representative of every lookup-validating API)
Rule / code  : XM-FIN-001 (SOFT-READ) → FIN-400-INVALID-LOOKUP (a defined error, never a 500/unhandled state, never MDL's raw code)
Scenario     : INTEGRATION · data class EDGE · language ALL
Preconditions: MDL's `ACCOUNT_TYPE` LookupType is absent or deactivated, so the injected `MdlLookupApi.readActiveValuesByKey("ACCOUNT_TYPE")` throws `LocalizedException(NOT_FOUND, MDL_404_TYPE_KEY)`. (XM-FIN-001 is in-process Spring injection inside the single deployable — there is no network hop, so the former "MDL unreachable / times out" precondition is not reachable and has been replaced by the real in-process failure mode.)
Steps        : 1. POST a new account (accountTypeCode requires MDL validation) while that lookup type is unavailable
Expected     : the request fails with FIN-400-INVALID-LOOKUP, message ar "القيمة المُدخلة غير صالحة" / en "The submitted value is not valid" — never a raw crash, and MDL's `MDL_404_TYPE_KEY` never surfaces to the caller
Test data    : any account payload; MDL's ACCOUNT_TYPE lookup type deactivated (or the MdlLookupApi test double configured to throw MDL_404_TYPE_KEY)
<!-- TC:TC-FIN-047:END -->

<!-- TC:TC-FIN-091:START traces=XM-FIN-002,REQ-FIN-037,REQ-FIN-038,API-FIN-026 -->
### TC-FIN-091 — a failed SEC directory read refuses the close instead of approving it
Derived from : XM-FIN-002 (REQ-FIN-037, REQ-FIN-038) · Exercises: API-FIN-026 PATCH /api/v1/fin/fiscal-periods/{id}/hard-close (same translation on API-FIN-027)
Rule / code  : XM-FIN-002 (READ) → FIN-403-SOD-VIOLATION — never a 500, and never SEC's own code
Scenario     : INTEGRATION · data class EDGE · language ALL
Preconditions: the RULE-FIN-015 fact comes from SEC's
  `SecUserDirectoryApi.findUserIdsHoldingPermission`, called twice (for
  PERM_FIN_PERIODS_CLOSE_APPROVE and PERM_FIN_JOURNAL_ENTRIES_CREATE). XM-FIN-002 is in-process
  Spring injection inside the single deployable — there is no network hop, so the failure mode to
  drive is the injected directory throwing, not a timeout: configure the `SecUserDirectoryApi` test
  double to raise on the first call. Also set up TC-FIN-059's approver, so the call would otherwise
  succeed and the 403 can only come from the failed read
Steps        : 1. hard-close a SOFT_CLOSE period while the directory read fails
Expected     : 403 FIN-403-SOD-VIOLATION with FIN's own ar/en messages; the period stays SOFT_CLOSE.
  The fallback is never "continue without SEC" — an unverified separation-of-duties fact refuses
  the close rather than approving it
Test data    : `SecUserDirectoryApi` double throwing on findUserIdsHoldingPermission; a soft-closed period
<!-- TC:TC-FIN-091:END -->
<!-- PHASE:INT-XM:END -->
