<!-- source: PHASE:INT-C -->
<!-- traces: REQ-FIN-001, REQ-FIN-007, REQ-FIN-008, REQ-FIN-010, REQ-FIN-014, REQ-FIN-018, REQ-FIN-022, REQ-FIN-025, REQ-FIN-031 -->
<!-- PHASE:INT-C:START traces=REQ-FIN-001 -->
## PHASE 5 — INT-C (cross-module consume)

Two `XM-*` rows (XM-FIN-001, XM-FIN-002), below the split threshold (2 < 5) — no SUB opened.
XM-FIN-002 was assigned at ALIGN-BE: SEC-BE landed after this phase was written and added a
second, genuinely distinct cross-module consumption (see its block and the ADR-FIN-001 note
below).

<!-- XM:XM-FIN-001:START traces=REQ-FIN-001,REQ-FIN-007,REQ-FIN-008,REQ-FIN-010,REQ-FIN-014,REQ-FIN-018,REQ-FIN-022,REQ-FIN-025,REQ-FIN-031 -->
### XM-FIN-001 — validate/read lookup-backed codes against MDL
Target        : MDL · ENT-MDL-001/002 (LookupType/LookupValue) · classification SOFT-READ
Interface     : in-process Spring injection — FIN's service layer injects
`com.erp.mdl.crossmodule.MdlLookupApi` and calls `readActiveValuesByKey(typeKey)` for every
one of FIN's 13 owned keys, at the point each lookup-backed field is written or offered as a select-list
Contract      : data required = the submitted code exists as an active value under the
named type, checked by membership in the returned `List<LookupOptionView>` (code, labelAr,
labelEn, sortOrder); fallback if absent = reject with `FIN-400-INVALID-LOOKUP`; an unknown/
inactive `typeKey` raises MDL's `LocalizedException(NOT_FOUND, MDL_404_TYPE_KEY)`, which FIN
catches and translates to `FIN-400-INVALID-LOOKUP` (MDL's code never leaks out of FIN's API);
retry = none (same-process call, no network hop); idempotency
= read-only, naturally idempotent
Blocks        : none DEFERRED — MDL v1 is already gated (pass-1 APPROVE); ACTIVE from the
moment FIN v1 is created
<!-- XM:XM-FIN-001:END -->

<!-- XM:XM-FIN-002:START traces=REQ-FIN-037,REQ-FIN-038 -->
### XM-FIN-002 — read SEC's user→permission directory for the RULE-FIN-015 SoD fact
Target        : SEC · ENT-SEC-001 (User) + SEC's role/permission grant tables · classification READ
Interface     : in-process Spring injection — `FinSeparationOfDutiesService` injects
`com.erp.sec.crossmodule.SecUserDirectoryApi` and calls
`findUserIdsHoldingPermission(permissionCode)` twice, for
`PERM_FIN_PERIODS_CLOSE_APPROVE` and `PERM_FIN_JOURNAL_ENTRIES_CREATE`, on every API-FIN-026
hard-close and API-FIN-027 year-end close. Never HTTP.
Contract      : data required = the set of user ids holding each permission, across each user's
role union; FIN derives two booleans from them (`closeApprovePermissionHeld`,
`entryCreatePermissionShared`) and hands them to `FiscalPeriodDomain.assertCanHardClose`, which
owns the decision; fallback if absent = a failed or refused directory read is caught and
re-raised as `FIN-403-SOD-VIOLATION` — the close is REFUSED, never approved on an unverified
fact; retry = none (same-process call); idempotency = read-only, naturally idempotent
Blocks        : none DEFERRED — SEC is already gated; ACTIVE from the moment SEC-BE landed
<!-- XM:XM-FIN-002:END -->

FIN's dependency on SEC for identity/authorization (the principal on every request, the
`@PreAuthorize` gate, and FIN's own self-registration of its module/screens/actions into SEC)
remains NOT a formal `XM` row — ADR-FIN-001 (carried from P2), unchanged. XM-FIN-002 is a
different thing and is registered separately: it is not the platform's ambient authorization of
the caller but FIN's own business logic reading SEC's data *about other users* — a named FIN
service calling a named SEC `crossmodule` interface method, whose returned rows are an input to
a FIN business rule (RULE-FIN-015) and whose absence has a defined, FIN-owned failure code. That
is exactly the shape ADR-FIN-001 excludes only the ambient case from, and exactly the shape
XM-FIN-001 already has for MDL. Registering it costs nothing and makes FIN's dependency on
SEC's read model visible upstream; leaving it unregistered would hide a real consumption behind
an ADR written about a different one.
<!-- PHASE:INT-C:END -->
