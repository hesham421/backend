<!-- source: PHASE:INT-C -->
<!-- traces: REQ-FIN-001, REQ-FIN-007, REQ-FIN-008, REQ-FIN-010, REQ-FIN-014, REQ-FIN-018, REQ-FIN-022, REQ-FIN-025, REQ-FIN-031, REQ-FIN-045, REQ-FIN-048, REQ-FIN-049, REQ-FIN-054, REQ-FIN-061, REQ-FIN-062 -->
<!-- PHASE:INT-C:START traces=REQ-FIN-001,REQ-FIN-045,REQ-FIN-048,REQ-FIN-054,REQ-FIN-061,REQ-FIN-062 -->
## PHASE 5 — INT-C (cross-module consume)

One live `XM-*` row, below the split threshold (1 < 5), so no SUB. XM-FIN-002 stays below as a retired, struck
historical block; it is not a live dependency. v2 mints no XM: the six new lookup-backed columns widen XM-FIN-001,
and the new screen's registration into SEC is platform integration under ADR-FIN-001, like every v1 screen.

| XM | Classification | Target | Interface (in_process — one deployable) | What the target publishes | Status |
|---|---|---|---|---|---|
| XM-FIN-001 | SOFT-READ | MDL — ENT-MDL-001, ENT-MDL-002 | injected `com.erp.mdl.crossmodule.MdlLookupApi` | `readActiveValuesByKey(typeKey)` → `List<LookupOptionView>` | ACTIVE (MODIFIED v2 — 15 keys) |
| XM-FIN-002 | READ (historical) | SEC | none — the consuming service was deleted | — | RETIRED 2026-09-12 |

<!-- XM:XM-FIN-001:START traces=REQ-FIN-001,REQ-FIN-007,REQ-FIN-008,REQ-FIN-010,REQ-FIN-014,REQ-FIN-018,REQ-FIN-022,REQ-FIN-025,REQ-FIN-031,REQ-FIN-045,REQ-FIN-048,REQ-FIN-049,REQ-FIN-054,REQ-FIN-061,REQ-FIN-062 -->
### XM-FIN-001 — validate and read lookup-backed codes against MDL (MODIFIED v2 — 15 keys, 6 more columns)
Target        : MDL · ENT-MDL-001 (LookupType), ENT-MDL-002 (LookupValue) · classification SOFT-READ
Interface     : in-process (profile.conventions.module_interface = in_process) — FIN's services inject MDL's published
                `com.erp.mdl.crossmodule.MdlLookupApi` and call `readActiveValuesByKey(typeKey)`, consuming
                `LookupOptionView` (code, labelAr, labelEn, sortOrder) and never an MDL entity. One deployable: no
                HTTP client, no base path, no timeout, no network error path
Contract      : data required = the submitted code is an active value of the named FIN-owned key. The v1 columns
                are unchanged; v2 adds six: the rule line's accountBusinessFieldCode, the mapping's eventTypeCode,
                businessFieldCode and — only when businessFieldCode is PAYMENT_METHOD — businessValue, and the tag's
                valueSourceCode and businessFieldCode (DBF-FIN-148, DBF-FIN-150, DBF-FIN-151, DBF-FIN-152,
                DBF-FIN-160, DBF-FIN-161). Fallback if absent = reject with FIN-400-INVALID-LOOKUP, except
                ACCOUNT_DERIVATION_TYPE (RULE-FIN-018 → FIN-422-UNSUPPORTED-DERIVATION) and PAYMENT_METHOD as a
                business value (RULE-FIN-024 → FIN-422-INVALID-PAYMENT-METHOD). MDL's own not-found for an unknown
                key is caught and answered as FIN-400-INVALID-LOOKUP; MDL's code never leaks out of FIN's API.
                Retry: none (same process). Idempotency: read-only
Blocks        : none DEFERRED — MDL v1 is gated, the row is ACTIVE. v2 depends on db-script BLOCK 8 (2 new keys, 6
                values, CHEQUE, DIRECT retired) having run before API-FIN-011 or API-FIN-039 is first called —
                listed with its producer in SEC-BE's bootstrap rows. Accepted risk, unchanged: a value deactivated
                in MDL after a FIN row cites it is not retro-invalidated (db-script §2.1)
<!-- XM:XM-FIN-001:END -->

FIN's dependency on SEC for identity and authorization — the principal on every request, the `@PreAuthorize` gate,
and FIN's own registration of its module, screens and actions into SEC, the account-mappings screen included —
remains platform integration and not an `XM` row (ADR-FIN-001, carried from P2). Since 2026-09-12 it is the only
relationship FIN has with SEC: XM-FIN-002 was registered for FIN's read of SEC's data about other users, and that
read was deleted with the service that made it.
<!-- PHASE:INT-C:END -->
