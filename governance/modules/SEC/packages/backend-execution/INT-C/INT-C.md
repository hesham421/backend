<!-- Source: PHASE:INT-C -->

## PHASE INT-C — Integration Contract Specifications
─────────────────────────────────────────────────────────────────
Gate Status: PASSED ✓ (re-passed v1.3)

## INT-C SUMMARY — SEC — PLAN-ID: PLAN-SEC-001
XM-ID │ Classification │ Target │ Interface │ Contract Status
──────┼────────────────┼────────┼───────────┼────────────────
(none outbound — SEC has no cross-module dependencies of its own — db-script-SEC §3. **Tier-1 Module/RoleModule is INTRA-SEC — introduces no XM.**)

INBOUND XM STUB NOTATION (SEC is the SOURCE/ROOT of the SHARED identity entity):
  XM-INBOUND-STUB-1
    Consumer module : FILE — SOFT-READ of SEC_USER_ACCOUNT (auth filter + created_by)
    XM-ID assignment : XM-FILE-001 (assigned by FILE, its own P2/P3.1) — NOT-YET-CONSUMED here
    Status : NOT-YET-ASSIGNED-BY-SEC (SEC never assigns consumer XM-IDs)
  XM-INBOUND-STUB-2
    Consumer module : NOTIF — SOFT-READ of SEC_USER_ACCOUNT (recipient identity)
    XM-ID assignment : XM-NOTIF-001 (assigned by NOTIF)
    Status : NOT-YET-ASSIGNED-BY-SEC
  Event contract: SEC publishes CU ApplicationEvents (PasswordResetRequested, AccountActivationRequested).
  NOTIF subscribes. SEC never calls NOTIF directly (srs-SEC A7). RULE-SEC-012 governs deactivation semantics for consumers.
  SSO note (v1.3): the single internal JWT authority is consumed by FILE/NOTIF as trusted auth — this is authentication trust, not an XM data dependency; no XM-ID.

INT-C GATE CHECK: [✓] all XM from DB Register accounted (0 outbound) [✓] no XM invented [✓] inbound stubs use INBOUND-STUB notation [✓] Tier-1 intra-SEC — no new XM [✓] Open RXEs none
INT-C Gate: PASSED ✓
─────────────────────────────────────────────────────────────────
