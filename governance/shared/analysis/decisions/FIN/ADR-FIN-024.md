# ADR-FIN-024 — An account mapping references its account by id, and its key is fixed once created

Module  : FIN     Version : v2     Stage raised : P1 (SRS)
Status  : ACCEPTED (non-breaking)
traces  : ENT-FIN-015, REQ-FIN-054, REQ-FIN-056, REQ-FIN-060, RULE-FIN-023, US-FIN-025

## Context
The change manifest (§1) describes each mapping entry as holding "the account code it resolves to".
It leaves two choices open. The first is whether the entry stores the account's code as text or a
reference to the Account entity (ENT-FIN-001). The second is which fields an administrator can
change after the entry is created. US-FIN-025 asks for mappings to be added, changed and
deactivated.

## Decision
1. `AccountMapping.accountId` is a reference to ENT-FIN-001. The mapping screen displays the
   account's code and names from the Account itself. A text code would duplicate the chart of
   accounts (ARCH-2), and a typo would not surface until an event failed to post.
2. The mapping's key is `eventTypeCode`, `businessFieldCode` and `businessValue`. The key is fixed
   once the mapping is created. A change (REQ-FIN-056) replaces the account only. To move a mapping
   to another key, deactivate it and create a new one. A key edited in place would silently
   redirect a business value that other lines rely on. Deactivate-and-create leaves both rows in the
   audit trail, and RULE-FIN-022 (one active mapping per key) needs no update-time variant.
3. Deactivation is soft (`isActiveFl`) [KB:erp-domain-standards §6], and there is no reactivation.
   A deactivated key is restored by creating a new mapping, which RULE-FIN-022 allows because the
   old row is inactive.

## Consequences
- The update request carries `accountId` alone. P3.1 publishes no key edit.
- RULE-FIN-023 validates the referenced account (active leaf) when a mapping is saved. RULE-FIN-007
  still validates it at post time, because the account can be deactivated after the mapping is
  saved (REQ-FIN-052).
- Override: allow a key edit on update and add an update-time uniqueness check to RULE-FIN-022.
