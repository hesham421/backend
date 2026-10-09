# ADR-FIN-030 — v2 error codes, and a uniqueness refusal answered as the business error it stands for

Module  : FIN     Version : v2     Stage raised : P3.1 (Backend Execution Plan)
Status  : ACCEPTED (non-breaking)
traces  : REQ-FIN-048, REQ-FIN-049, REQ-FIN-051, REQ-FIN-053, REQ-FIN-056, REQ-FIN-057, REQ-FIN-059, REQ-FIN-060, REQ-FIN-061, REQ-FIN-064, REQ-FIN-065, REQ-FIN-069, REQ-FIN-072, REQ-FIN-074, RULE-FIN-004, RULE-FIN-018, RULE-FIN-019, RULE-FIN-020, RULE-FIN-021, RULE-FIN-022, RULE-FIN-023, RULE-FIN-024, RULE-FIN-025, RULE-FIN-026, RULE-FIN-027, RULE-FIN-028, API-FIN-011, API-FIN-020, API-FIN-039, API-FIN-040, API-FIN-041

## Context
ADR-FIN-027 (P1) settled which v2 rejections are distinct errors and left the code strings to this stage. Three
more choices it did not cover have to be made here.
1. RULE-FIN-004 (MODIFIED) and RULE-FIN-022 each need a store's uniqueness refusal answered as their own error. The
   delivered platform forbids a service to catch `DataIntegrityViolationException`, and v1 therefore let such a
   refusal surface as a generic error.
2. On the event path, a date that no period covers used to answer the shared pipeline's FIN-404-PERIOD. RULE-FIN-028
   requires a distinct configuration error, never the retryable period-not-open code.
3. Updating or deactivating an unknown account mapping needs a not-found code.

## Decision
1. **Codes**, in the house format `FIN-{http}[-{SLUG}]`:
   FIN-422-UNSUPPORTED-DERIVATION (RULE-FIN-018), FIN-422-DERIVATION-SPEC (RULE-FIN-019), FIN-422-UNMAPPED-VALUE
   (RULE-FIN-020), FIN-422-MISSING-BUSINESS-FIELD (RULE-FIN-021), FIN-409-MAPPING-DUP (RULE-FIN-022),
   FIN-422-MAPPED-ACCOUNT-INELIGIBLE (RULE-FIN-023), FIN-422-INVALID-PAYMENT-METHOD (RULE-FIN-024),
   FIN-422-DIMENSION-TAG-DUP (RULE-FIN-025), FIN-422-DIMENSION-TAG-SOURCE (RULE-FIN-026),
   FIN-422-UNRESOLVED-DIMENSION-VALUE (RULE-FIN-027), FIN-422-NO-DEFINED-PERIOD (RULE-FIN-028), and
   FIN-404-ACCOUNT-MAPPING (PLATFORM-STD). A request that is well-formed but refused by a rule is 422; a clash with an
   existing row is 409, as FIN's other `…-DUP` codes are. FIN-409-DUPLICATE-EVENT and FIN-409-PERIOD-NOT-OPEN are kept
   verbatim (ADR-FIN-027). FIN-422-MAPPING-UNSUPPORTED is struck.
2. **Store refusals.** FIN gains one module-wide `FinPersistenceRefusalAdvisor`, placed like the delivered
   `FinForbiddenAdvisor`: one `DefaultPointcutAdvisor` over `com.erp.fin.service.*`, outside the transaction advisor,
   never a catch in a service and never a feature `@ControllerAdvice`. It maps a refusal by constraint name through a
   fixed registry — UQ_FIN_JOURNAL_ENTRY_EVENT_REF → FIN-409-DUPLICATE-EVENT, UQ_FIN_ACCOUNT_MAPPING_ACTIVE_KEY →
   FIN-409-MAPPING-DUP — and re-throws any other refusal unchanged. The two inserts are flushed immediately, so the
   refusal happens inside the call.
3. **Period on the event path.** API-FIN-020 resolves the period that covers the event date itself (QR-FIN-059), before
   the shared pipeline. None → FIN-422-NO-DEFINED-PERIOD. Found but not OPEN → FIN-409-PERIOD-NOT-OPEN, at post.
   FIN-404-PERIOD stays the answer of API-FIN-014 and API-FIN-017, whose behaviour v2 does not change.

## Consequences
- The consumer can tell the three classes apart by code alone (DOC phase): acknowledge, wait and retry, or fail.
- Registering further v1 constraints with the advisor would turn v1's generic-error races into their `…-DUP` codes
  (UQ_FIN_ACCOUNT_CODE, UQ_FIN_DIMENSION_CODE, UQ_FIN_DIMENSION_VALUE_DIM_CODE, UQ_FIN_EVENT_TYPE_RULE_CODE,
  UQ_FIN_FISCAL_YEAR_CODE). Each would be a one-line extension. It is not taken here, because no v2 requirement asks
  for it; each endpoint's Concurrency line records the gap.
- Override: fold FIN-422-MISSING-BUSINESS-FIELD into FIN-422-UNMAPPED-VALUE (ADR-FIN-027's stated override). Or answer a
  store refusal from a platform-wide handler instead of a FIN advisor — that is a platform decision.
