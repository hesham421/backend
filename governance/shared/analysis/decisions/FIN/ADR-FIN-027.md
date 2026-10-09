# ADR-FIN-027 — Error identities for the v2 rejections: four new distinct errors, two v1 codes confirmed unchanged

Module  : FIN     Version : v2     Stage raised : P1 (SRS)
Status  : ACCEPTED (non-breaking)
traces  : REQ-FIN-051, REQ-FIN-053, REQ-FIN-068, REQ-FIN-069, REQ-FIN-072, REQ-FIN-073, REQ-FIN-074, REQ-FIN-075, RULE-FIN-004, RULE-FIN-008, RULE-FIN-020, RULE-FIN-021, RULE-FIN-027, RULE-FIN-028, US-FIN-024, US-FIN-030, US-FIN-031

## Context
The change manifest says FIN's analysis owns the name of the dedicated unmapped-value error (§1). It
also asks to "confirm" that a non-open period has its own catalogued code, and to add one only if
none exists (§4). The consumer branches on these answers: a duplicate is acknowledged, a closed
period waits and retries, and every other rejection fails. So each rejection has to be
distinguishable from the others. Error-code strings are catalogued by P3.1. What this stage settles
is which rejections are distinct errors.

## Decision
1. **Confirmed unchanged (US-FIN-030, US-FIN-031).** The v1 catalogue already carries
   `FIN-409-DUPLICATE-EVENT` (RULE-FIN-004) and `FIN-409-PERIOD-NOT-OPEN` (RULE-FIN-008). Each is
   distinct from every validation and configuration error. Consumers will depend on these strings,
   so they are kept verbatim. v2 changes only when they are raised. A uniqueness refusal from the
   store answers the duplicate error too (RULE-FIN-004, as modified). The period-not-open answer
   covers every existing period whose state is not Open (ADR-FIN-014).
2. **Four new distinct errors.** Each gets its own code in FIN's existing `FIN-{status}-{NAME}`
   house style, named by P3.1:
   - unmapped business value (RULE-FIN-020): configuration gap. The fix is to add a mapping.
   - missing business field (RULE-FIN-021): payload gap. The fix is at the producer.
   - unresolvable dimension value (RULE-FIN-027): a value with no FIN dimension value.
   - no defined period for the event's date (RULE-FIN-028): calendar setup. Per ADR-FIN-014 this is
     never the period-not-open code.
   These rejections are kept apart because each is fixed by a different party, and a consumer or
   operator reading only the code has to know which fix applies.
3. **Retired.** The v1 code for "MAPPING is unsupported" no longer has a cause. MAPPING is now
   implemented (REQ-FIN-050), and DIRECT is gone (ADR-FIN-010). P3.1 retires the code from the
   catalogue, and no v2 requirement raises it.

## Consequences
- P3.1 catalogues the four new codes, each with the ar/en message of its RULE. It documents
  `FIN-409-PERIOD-NOT-OPEN` as the retryable rejection for the consumer (REQ-FIN-075).
- Override: fold the missing-business-field case into the unmapped-value error if one
  configuration signal is preferred. The distinctness of the period-not-open and duplicate answers
  is required by POL-FIN-030 and POL-FIN-031 and cannot be overridden.
