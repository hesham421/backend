## FIN v2 · P0 · round 3 (claude:opus)

I've updated all four files in `governance-shared/analysis/modules/FIN/v2/P0/`. I checked them against `gov.py`'s definition rule: the only POL definitions are POL-FIN-021…032, and there are no `[QUESTION]` or `<!-- OPEN -->` markers. I didn't run `gov.py analyze`; the orchestrator runs it.

**Problems in round 2 that this round fixes:**
- **No ADRs were saved for round 2.** Its response contained no `DECISION:` blocks, and `analysis/decisions/FIN/` holds only ADR-FIN-001…009, all from v1. The five decisions are issued below so the orchestrator can save them.
- **The MDL dependency was labelled wrong.** Round 2 called it `LOOKUP` while also saying it was unchanged. In v1 it is `HARD`, and that label is now restored everywhere.
- **Two gaps left "for the merge" are now closed**, using `_state/current-*` (this version's generated current state, not a version folder):
  - FIN's v1 position is 2.2, layer L3, type transactional + reporting.
  - No v1 policy ever allowed DIRECT. It was only an automatically added value of `ACCOUNT_DERIVATION_TYPE`, and it contradicted v1's own rule "Account numbers in event payloads — never". So no v1 policy is superseded, and the note asking the orchestrator to check this is removed.

**Refinements:**
- Each new policy now names the v1 policy it builds on (for example, POL-FIN-030 → POL-FIN-012 and POL-FIN-031 → POL-FIN-004).
- `ACCOUNT_DERIVATION_TYPE` is modified: its values go from CONSTANT, DIRECT, MAPPING to CONSTANT, MAPPING.
- The v1 lookup `PAYMENT_METHOD` gains the value you named: cheque.
- Two automatic decisions are added:
  - A payment-method mapping value is a `PAYMENT_METHOD` code. An expense-type value stays as the legacy system's own code.
  - The legacy rule "cash unless cheque" becomes one explicit mapping row per payment method, loaded with the migration data. There is no default account.
- POL-FIN-028 is now CONFIRMED because it follows from your request: §2 says "same discipline as accounts" and "validation is not bypassed", and §1 says "never a default, never a guess".
- The platform decision rows are renamed PS-v2-1 and PS-v2-2 so they aren't confused with the module decisions v2-n.

---

DECISION: DIRECT account derivation is removed outright, not deprecated or restricted; dimension values use their own source list (constant or business field)
Accepted from round 1 (option A); round 2 agreed. DIRECT is the only way an account code could come in through the event payload. That breaks domain-profile G6, POL-FIN-014, POL-FIN-020 and v1's own rule "Account numbers in event payloads — never", so v1 never had a legitimate use for it. Deprecating it would protect nothing: v1 was never delivered (project-registry pipeline status: "deliver BLOCKED"), so no configuration data uses DIRECT. Restricting it would leave one value that is allowed for dimensions but forbidden for accounts, and every later stage would have to handle that ambiguity. `ACCOUNT_DERIVATION_TYPE` keeps CONSTANT and MAPPING only, and dimensions use `FIN_DIMENSION_VALUE_SOURCE`. Sources: change manifest §1; domain-profile G6, G1.
traces: POL-FIN-021, POL-FIN-022, POL-FIN-026

DECISION: A dimension value that a rule line declares, if missing from the event or failing validation, rejects the event and nothing is posted
Accepted, and upgraded from a recommendation to CONFIRMED. The request text requires it: §2 says "same discipline as accounts" and "existing validation is not bypassed", and §1 says "never a default, never a guess". Posting the line without the dimension would produce entries with no branch, which is exactly the parallel-run mismatch this change set is meant to prevent. A rule line that declares no dimension still produces a line without one, the same as a manual line. Sources: change manifest §1, §2, acceptance 4.
traces: POL-FIN-027, POL-FIN-028, POL-FIN-024

DECISION: Translating legacy organisation and branch codes into FIN dimension values is deferred; only constant and business-field sources are in scope
Accepted. You asked for exactly these two modes. Organisation and branch codes are business facts, so reading them directly stays within G6. Widening the mapping store to cover dimensions would add scope nobody requested. It becomes due when legacy codes turn out to differ from FIN's dimension value codes. Sources: change manifest §2; domain-profile G6.
traces: POL-FIN-026

DECISION: The event business-field names come from a FIN-owned lookup, FIN_EVENT_BUSINESS_FIELD, registered in MDL
Accepted. G1 says anything that can change is defined data, and G4 and G5 put every coded list in MDL, owned by the module that registers it. With free text, a typo would only show up when an event fails to post. The initial values are the four you named: payment method, expense type code, organisation code and branch code. Sources: domain-profile G1, G4, G5; [KB:erp-domain-standards §2 rule 3].
traces: POL-FIN-022, POL-FIN-026

DECISION: One distinct, catalogued period-not-open code covers every existing period whose state refuses posting; a date with no defined period is a configuration error, not this code
Accepted. The request text supports it: §4 asks for a code "distinct from validation and configuration errors". The consumer needs one signal that means "retry later". Separate codes per period state would be API detail for P3.1. A missing period is a calendar setup fault that opening a period won't fix, so it doesn't belong under a retry signal. Error-code names are set at P1/P3.1. Sources: change manifest §4, acceptance 6; domain-profile §7.1.
traces: POL-FIN-031, POL-FIN-004

---

```
✓ Finance (FIN) v2 — P0 complete (dialogue converged, round 3)
  Next : P0.5 reads platform-summary.md · module-registry-fin.md · business-policies-fin.md
  Precondition for P0.5: HARD dependencies SEC, MDL present in the registry (both v1, pass-1 APPROVE)
  Another module? None pending in this change set
```

Your decision is needed on three items: v2-1 (removing DIRECT), v2-3 (deferring dimension translation) and v2-4 (the business-field lookup). v2-2 and v2-5 follow from your request text.

<!-- CONVERGED -->