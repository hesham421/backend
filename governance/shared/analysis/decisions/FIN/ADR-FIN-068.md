# ADR-FIN-068 — Test generation re-run for FIN v2: host catalogue values are created by the case that needs them, and cases that need one value in opposite states get their own
Status      : ACCEPTED (non-breaking)
Stage       : test-gen (Test Generation, standalone)        Module: FIN        Version: v2
Lane        : test-gen
Decided     : 2026-09-24
traces      : AC-FIN-050, AC-FIN-056, AC-FIN-057, AC-FIN-059, AC-FIN-061, AC-FIN-065, REQ-FIN-053, REQ-FIN-055, REQ-FIN-061, SCR-FIN-013

## Context

SRS A6 is explicit about two lookups:
- `ACCOUNTING_EVENT_TYPE`: "none seeded — host-specific, added as data".
- `PAYMENT_METHOD`: CHEQUE is seeded, and "every other value host-specific, added as data".

The v2 cases named PAYMENT_RECEIVED, EXPENSE_PAID, CASH and TRANSFER straight from the ACs, and no
case created them. The frontend's live suite could not run them until it added PAYMENT_RECEIVED
through MDL's API itself.

The cases also disagreed about TRANSFER:
- TC-FIN-102 needs no TRANSFER mapping in any state.
- TC-FIN-105 and TC-FIN-133 need an inactive TRANSFER mapping. RULE-FIN-024 saves one only while
  TRANSFER is an active PAYMENT_METHOD.
- TC-FIN-111 and TC-FIN-139 read as if PAYMENT_METHOD held exactly CASH and CHEQUE.

TC-FIN-103 needs CASH's only mapping inactive, while TC-FIN-135 and the other CASH cases need it
active.

The factory now checks both (C10.8 tc-data, C10.9 tc-consistency). Against the cases the gate
approved, tc-data flagged 46 uses in 30 cases and tc-consistency flagged 3 contradictions. MDL has
no operation that reactivates a lookup value, so once TRANSFER was deactivated live it could not be
used again. A shared host value cannot serve cases that need it in different states.

## Decision

1. **Every case that names a host value creates it.** Its `Host data` line names the key, the
   value, the state the case needs, and the call that creates it: API-MDL-006
   `POST /api/v1/mdl/lookup-types/{id}/values` on the key's type. Host data is site data (SRS A6), so
   it is never moved into a product seed, and no case assumes it exists.
2. **A case that needs a value in its own state gets its own value** (engine rule 7):
   - TC-FIN-102 uses `TRANSFER_102`, which is never created and appears on no mapping.
   - TC-FIN-105 and TC-FIN-133 use `TRANSFER_105` and `TRANSFER_133`. Each is created active, its
     mapping is saved, and then the mapping is deactivated.
   - TC-FIN-103 uses `CASH_103`, whose one mapping is saved and then deactivated.

   The AC's value is renamed, never its behaviour, and each case's `Test data` line says so. This is
   the one departure from "the values named in the AC" that the engine allows.
3. **An option set is asserted as inclusion, never as an exact list.** TC-FIN-111 and TC-FIN-139
   assert that PAYMENT_METHOD includes CASH and CHEQUE, and that "CHEQEU" is not among its values. On
   a shared catalogue with host data added, an exact set cannot hold.
4. **Refusal texts are unchanged.** The frontend plan now binds every routed code to its catalogue
   text (ADR-FIN-067, C10.7), so the cases that quote the SRS message agree with the screen by
   construction.
5. **Routed to the backend track:** MDL publishes no operation that reactivates a lookup value. Either
   publish one or confirm that deactivation is final. Point 2 makes these cases independent of the
   answer.

## Consequences

- tc-data, tc-consistency and msg-bound report no finding for FIN v2.
- The frontend's `PLAN_STALE` / UI-FLOWS item is answered: the suite's own host-data setup is now
  the plan's, and the TRANSFER contradiction is resolved by distinct values.
