# REGISTRY EXTRACT — registry-exec-be-SEC
══════════════════════════════════════════════════════════════════
Module          : Security (SEC)
Source artifact : backend-execution-plan-SEC.md (PLAN-SEC-001)
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4.1/P4.2 audit
══════════════════════════════════════════════════════════════════

## HEADER
Module name : Security
Module Prefix : SEC
(ALIGN-BE status and readiness/usability are not extracted here — they remain with
backend-execution-plan-SEC.md and P4.1, per P-REG's non-readiness policy.)

## FIELD-ID REGISTER (DB Alignment Manifest — compact)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0001..0011 | DBF-0001..0011 | (per column) | — | ✓ |
| FIELD-0017 | DBF-0017 | Long | — | ✓ |
| FIELD-0023 | DBF-0023 | Long | FK PAGE_FK → SEC_PAGE | ✓ |
| FIELD-0029 | DBF-0029 | Long | FK self PARENT_PAGE_FK | ✓ |
| FIELD-0034 | DBF-0034 | Long | FK USER_ACCOUNT_FK | ✓ |
| FIELD-0039 | DBF-0039 | Long | FK USER_ACCOUNT_FK | ✓ |
| FIELD-0044 | DBF-0044 | Long | FK USER_ACCOUNT_FK | ✓ |
| FIELD-0045..0046 | DBF-0045..0046 | Long | FK (join SEC_USER_ROLE) | ✓ |
| FIELD-0047..0048 | DBF-0047..0048 | Long | FK (join SEC_ROLE_PERMISSION) | ✓ |
| FIELD-0049 | DBF-0049 | Long | FK MODULE_FK → SEC_MODULE (NOT NULL) | ✓ |
| FIELD-0050..0054 | DBF-0050..0054 | (per column) | — (SEC_MODULE base) | ✓ |
| FIELD-0055..0056 | DBF-0055..0056 | Long | FK (join SEC_ROLE_MODULE) | ✓ |
All remaining FIELD-IDs align 1:1 to their DBF-ID. 56/56 aligned.

## ERROR CATALOG (codes only)
| ERR-ID | Source RULE-ID | HTTP Status |
|---|---|---|
| ERR-0001 | RULE-SEC-001 | 409 |
| ERR-0002 | RULE-SEC-002 | 400 |
| ERR-0003 | RULE-SEC-003 | 422 |
| ERR-0004 | RULE-SEC-005 | 423 |
| ERR-0005 | RULE-SEC-006 | 401 |
| ERR-0006 | RULE-SEC-007 | 400 |
| ERR-0007 | RULE-SEC-008 | 400 |
| ERR-0008 | RULE-SEC-009 | 403 |
| ERR-0009 | RULE-SEC-010 | 409 |
| ERR-0010 | RULE-SEC-001 | 409 |
| ERR-0011 | PLATFORM-STD | 401 |
| ERR-0012 | PLATFORM-STD | 404 |
| ERR-0013 | RULE-SEC-014 | 422 |
| ERR-0014 | RULE-SEC-014 | 409 |

## INT SUMMARY (XM execution status — Backend only)
| XM-SEC-ID | Execution Status | Blocks (API-IDs) | RXE-ID |
|---|---|---|---|
| (none) | — | — | — |
SEC has no outbound XM; Tier-1 (Module/RoleModule) is intra-SEC.

## TC COVERAGE SUMMARY — BACKEND (from SECTION D, summary rows only)
| RULE-ID | Happy TC-BE-ID | Violation TC-BE-ID | Status |
|---|---|---|---|
| RULE-SEC-001 | TC-BE-SEC-001 | TC-BE-SEC-002 | COVERED |
| RULE-SEC-002 | TC-BE-SEC-003 | TC-BE-SEC-004 | COVERED |
| RULE-SEC-003 | TC-BE-SEC-005 | TC-BE-SEC-006 | COVERED |
| RULE-SEC-004 | TC-BE-SEC-007 | — | COVERED |
| RULE-SEC-005 | TC-BE-SEC-008 | TC-BE-SEC-009 | COVERED |
| RULE-SEC-006 | TC-BE-SEC-010 | TC-BE-SEC-011 | COVERED |
| RULE-SEC-007 | TC-BE-SEC-012 | TC-BE-SEC-013 | COVERED |
| RULE-SEC-008 | TC-BE-SEC-014 | TC-BE-SEC-015 | COVERED |
| RULE-SEC-009 | TC-BE-SEC-016 | TC-BE-SEC-017 | COVERED |
| RULE-SEC-010 | TC-BE-SEC-018 | TC-BE-SEC-019 | COVERED |
| RULE-SEC-011 | TC-BE-SEC-020 | — | COVERED |
| RULE-SEC-012 | TC-BE-SEC-021 | — | COVERED |
| RULE-SEC-013 | TC-BE-SEC-038 | — | COVERED |
| RULE-SEC-014 | TC-BE-SEC-039 | TC-BE-SEC-040 / TC-BE-SEC-041 | COVERED |
(Reference placeholders per CONTRACT-9, as written in the source plan's SECTION D.)

## MODULE GOVERNANCE INDEX (state snapshot)
Note: MODULE GOVERNANCE INDEX section not found in source — omitted.

## FIELD-ID / API-ID / PLAN-ID NAMESPACE
FIELD-SEC : last = FIELD-0056
API-SEC   : last = API-SEC-020
RULE-SEC  : last = RULE-SEC-014
ERR-SEC   : last = ERR-0014
QR-SEC    : last = QR-SEC-0029
PLAN-SEC  : PLAN-SEC-001

---
*End of registry-exec-be-SEC.md*
