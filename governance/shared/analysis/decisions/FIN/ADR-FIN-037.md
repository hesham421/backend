# ADR-FIN-037 — Event business facts are named by their FIN_EVENT_BUSINESS_FIELD code inside the event's `fields` map; RULE-FIN-008's wording is corrected to remove the "may" ambiguity
Status      : ACCEPTED
Stage       : pass-1 revise        Module: FIN        Version: v2
Lane        : merge-review-notes
Decided     : 2026-09-23

## Decision
The v2 SRS's ACs for API-FIN-020's MAPPING-derived accounts and BUSINESS_FIELD-sourced dimension
tags are re-emitted so every business fact a test builds is carried as
`fields.<FIN_EVENT_BUSINESS_FIELD code>` (e.g. `fields.PAYMENT_METHOD = "CHEQUE"`,
`fields.BRANCH_CODE = "BR-07"`, `fields.EXPENSE_TYPE_CODE = "RENT"`), matching the carrier
ADR-FIN-031 and API-FIN-020 already implement. A sentence is added to A2's general notes making
that carrier explicit at the module level. Separately, RULE-FIN-008 is re-emitted (MODIFIED, wording
only) replacing "that close may only run once every period of the year is Hard Closed" with "that
close runs only after every period of the year is Hard Closed" — no behaviour change.

## Why
The v2 SRS ACs (AC-FIN-047, AC-FIN-050…053, AC-FIN-067, AC-FIN-068, AC-FIN-071…074) previously named
the business fact as a bare field (`paymentMethod`, `branchCode`, `expenseTypeCode`). A test built
from those ACs as written would submit a payload the implementation does not recognize and would get
`FIN-422-MISSING-BUSINESS-FIELD` instead of the intended posting, contradicting ADR-FIN-031 and the
backend execution plan. RULE-FIN-008's "may" reads as permissive/optional rather than as the
temporal gate it is (C5.13).

## Consequence
No behaviour changes. Every affected AC's Given/When lines are corrected; RULE-FIN-008's Statement
text changes with no change to Data source, Message or enforcement.

Source : merge-review-notes gate pass-1, findings G1 and G9
Traces : REQ-FIN-047, REQ-FIN-050, REQ-FIN-051, REQ-FIN-063, REQ-FIN-066, REQ-FIN-068, REQ-FIN-069, REQ-FIN-020, REQ-FIN-073
