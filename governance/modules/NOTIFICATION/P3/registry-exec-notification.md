# REGISTRY EXTRACT — registry-exec-NOTIF
══════════════════════════════════════════════════════════════════
Module          : Notification Service (NOTIF prefix)
Source artifact : execution-plan.md
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4 audit
══════════════════════════════════════════════════════════════════

## HEADER
| Field | Value |
|---|---|
| Module Name | Notification Service |
| Module Prefix | NOTIF |
| PLAN-ID | PLAN-NOTIF-001 |
| DBS-ID | DBS-NOTIF-001 |

## FIELD-ID REGISTER (DB Alignment Manifest)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0001 | DBF-0001 | Long | — | ✓ |
| FIELD-0002 | DBF-0002 | Long | FK → Security USERS.USERS_PK (EXCEPTION, no XM-ID) | ✓ |
| FIELD-0003 | DBF-0003 | String(20) | LOV-NOTIF-001 | ✓ |
| FIELD-0004 | DBF-0004 | String(50) | — (natural-key ref, no physical FK) | ✓ |
| FIELD-0005 | DBF-0005 | String(500) | — | ✓ |
| FIELD-0006 | DBF-0006 | String(1000) | — | ✓ |
| FIELD-0007 | DBF-0007 | String(20) | LOV-NOTIF-002 | ✓ |
| FIELD-0008 | DBF-0008 | Short (SMALLINT) | — | ✓ (DRV-NOTIF-001) |
| FIELD-0009 | DBF-0009 | LocalDateTime | — | ✓ |
| FIELD-0010 | DBF-0010 | String(20) | — | ✓ |
| FIELD-0011 | DBF-0011 | Long | — (polymorphic, no FK) | ✓ |
| FIELD-0012 | DBF-0012 | String(50) | — | ✓ |
| FIELD-0013 | DBF-0013 | String(255) | — | ✓ |
| FIELD-0014 | DBF-0014 | LocalDateTime | — | ✓ |
| FIELD-0015 | DBF-0015 | String(255) | — | ✓ |
| FIELD-0016 | DBF-0016 | LocalDateTime | — | ✓ |
| FIELD-0017 | DBF-0017 | Long | — | ✓ |
| FIELD-0018 | DBF-0018 | String(50) | — | ✓ |
| FIELD-0019 | DBF-0019 | String(200) | — | ✓ |
| FIELD-0020 | DBF-0020 | String(200) | — | ✓ |
| FIELD-0021 | DBF-0021 | String(20) | LOV-NOTIF-001 | ✓ |
| FIELD-0022 | DBF-0022 | String(20) | — | ✓ |
| FIELD-0023 | DBF-0023 | String (TEXT) | — | ✓ |
| FIELD-0024 | DBF-0024 | String (TEXT) | — | ✓ |
| FIELD-0025 | DBF-0025 | Long | XM-NOTIF-001 → FILE_DOCUMENT (DEFERRED) | ✓ |
| FIELD-0026 | DBF-0026 | Boolean | — | ✓ |
| FIELD-0027 | DBF-0027 | String(255) | — | ✓ |
| FIELD-0028 | DBF-0028 | LocalDateTime | — | ✓ |
| FIELD-0029 | DBF-0029 | String(255) | — | ✓ |
| FIELD-0030 | DBF-0030 | LocalDateTime | — | ✓ |
| FIELD-0031 | DBF-0031 | Long | — | ✓ |
| FIELD-0032 | DBF-0032 | String(20) | LOV-NOTIF-001 | ✓ |
| FIELD-0033 | DBF-0033 | Boolean | — | ✓ |
| FIELD-0034 | DBF-0034 | String (TEXT) | — | ✓ |
| FIELD-0035 | DBF-0035 | String(255) | — | ✓ |
| FIELD-0036 | DBF-0036 | LocalDateTime | — | ✓ |
| FIELD-0037 | DBF-0037 | String(255) | — | ✓ |
| FIELD-0038 | DBF-0038 | LocalDateTime | — | ✓ |

## ERROR CATALOG
| ERR-ID | Source RULE-ID | HTTP Status |
|---|---|---|
| ERR-NOTIF-0001 | RULE-NOTIF-001 | 400 |
| ERR-NOTIF-0002 | RULE-NOTIF-006 | 400 |
| ERR-NOTIF-0003 | RULE-NOTIF-007 | 409 |
| ERR-NOTIF-0004 | PLATFORM-STD | 404 |

## INT SUMMARY (XM Execution Status)
| XM-NOTIF-ID | Execution Status | Blocks (API-IDs) | RXE-ID |
|---|---|---|---|
| XM-NOTIF-001 | DEFERRED ⏸ | FIELD-0025 (fileFk) | RXE-NOTIF (pending) |

## TC COVERAGE SUMMARY
| RULE-ID | Happy TC | Violation TC | Status |
|---|---|---|---|
| RULE-NOTIF-001 | TC-NOTIF-001 | TC-NOTIF-002 | COVERED ✓ |
| RULE-NOTIF-002 | TC-NOTIF-003 | — (design rule, no violation path) | COVERED ✓ |
| RULE-NOTIF-003 | TC-NOTIF-004 | — (behavioral) | COVERED ✓ |
| RULE-NOTIF-004 | TC-NOTIF-005 | — (behavioral) | COVERED ✓ |
| RULE-NOTIF-005 | TC-NOTIF-006 | — (behavioral) | COVERED ✓ |
| RULE-NOTIF-006 | TC-NOTIF-007 | TC-NOTIF-008 | COVERED ✓ |
| RULE-NOTIF-007 | TC-NOTIF-009 | TC-NOTIF-010 | COVERED ✓ |

Note: API-ID coverage table (API-NOTIF-001..012) also present in source, with 10/12 covered and 2 DEFERRED (API-NOTIF-004, API-NOTIF-005 — DRV-NOTIF-003); omitted here per RULE-ID-only extraction scope, see source SECTION D for full API coverage.

## MODULE GOVERNANCE INDEX
Note: Module Governance Index section not found in source — omitted.

## FIELD-ID / API-ID / PLAN-ID NAMESPACE
| ID Type | Last Assigned |
|---|---|
| FIELD-[NOTIF] | FIELD-0038 |
| API-[NOTIF] | API-NOTIF-012 |
| ERR-[NOTIF] | ERR-NOTIF-0004 |
| QR-[NOTIF] | QR-NOTIF-011 |
| PLAN-[NOTIF] | PLAN-NOTIF-001 |
