# REGISTRY EXTRACT — registry-exec-FILE
══════════════════════════════════════════════════════════════════
Module          : File Service (FILE prefix)
Source artifact : execution-plan.md
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4 audit
══════════════════════════════════════════════════════════════════

## HEADER
| Field | Value |
|---|---|
| Module Name | File Service |
| Module Prefix | FILE |
| PLAN-ID | PLAN-FILE-001 |
| DBS-ID | DBS-FILE-001 |

## FIELD-ID REGISTER (DB Alignment Manifest)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0001 | DBF-0001 | Long | — | ✓ |
| FIELD-0002 | DBF-0002 | String(50) | — | ✓ |
| FIELD-0003 | DBF-0003 | String(20) | — | ✓ |
| FIELD-0004 | DBF-0004 | String(200) | — | ✓ |
| FIELD-0005 | DBF-0005 | String(100) | — | ✓ |
| FIELD-0006 | DBF-0006 | Long | — | ✓ |
| FIELD-0007 | DBF-0007 | String(500) | — | ✓ |
| FIELD-0008 | DBF-0008 | Boolean | — | ✓ |
| FIELD-0009 | DBF-0009 | String(255) | — | ✓ |
| FIELD-0010 | DBF-0010 | LocalDateTime | — | ✓ |
| FIELD-0011 | DBF-0011 | String(255) | — | ✓ |
| FIELD-0012 | DBF-0012 | LocalDateTime | — | ✓ |
| FIELD-0013 | DBF-0013 | Long | — | ✓ |
| FIELD-0014 | DBF-0014 | Long | — (polymorphic, no FK) | ✓ |
| FIELD-0015 | DBF-0015 | String(100) | — | ✓ |
| FIELD-0016 | DBF-0016 | String(20) | — | ✓ |
| FIELD-0017 | DBF-0017 | Long | FK → FILE_CATEGORY (intra-module) | ✓ |
| FIELD-0018 | DBF-0018 | String(50) | LOV-FILE-001 | ✓ |
| FIELD-0019 | DBF-0019 | String(255) | — | ✓ |
| FIELD-0020 | DBF-0020 | String(100) | — | ✓ |
| FIELD-0021 | DBF-0021 | Long | — | ✓ |
| FIELD-0022 | DBF-0022 | Binary (BYTEA) | — (RESOLUTION-01, extends CORE-8) | ✓ |
| FIELD-0023 | DBF-0023 | String(50) | LOV-FILE-002 | ✓ |
| FIELD-0024 | DBF-0024 | String(255) | — | ✓ |
| FIELD-0025 | DBF-0025 | LocalDateTime | — | ✓ |
| FIELD-0026 | DBF-0026 | String(255) | — | ✓ |
| FIELD-0027 | DBF-0027 | LocalDateTime | — | ✓ |

## ERROR CATALOG
| ERR-ID | Source RULE-ID | HTTP Status |
|---|---|---|
| ERR-FILE-0001 | RULE-FILE-001 | 400 |
| ERR-FILE-0002 | RULE-FILE-002 | 401 |
| ERR-FILE-0003 | RULE-FILE-003 | 401/403 |
| ERR-FILE-0004 | RULE-FILE-004 | 401 |
| ERR-FILE-0005 | RULE-FILE-005 | N/A (informational) |
| ERR-FILE-0006 | RULE-FILE-006 | N/A (client confirm) |
| ERR-FILE-0007 | RULE-FILE-007 | 403 |
| ERR-FILE-0008 | PLATFORM-STD | 410 |
| ERR-FILE-0009 | PLATFORM-STD | 404 |

## INT SUMMARY (XM Execution Status)
| XM-FILE-ID | Execution Status | Blocks (API-IDs) | RXE-ID |
|---|---|---|---|
| — | None — 0 outbound XM-IDs for this module | — | — |

## TC COVERAGE SUMMARY
| RULE-ID | Happy TC | Violation TC | Status |
|---|---|---|---|
| RULE-FILE-001 | TC-FILE-001 | TC-FILE-002 | COVERED ✓ |
| RULE-FILE-002 | — | TC-FILE-003 | COVERED ✓ (violation-only rule) |
| RULE-FILE-003 | — | TC-FILE-004 | COVERED ✓ (violation-only rule) |
| RULE-FILE-004 | — | TC-FILE-005 | COVERED ✓ (violation-only rule) |
| RULE-FILE-005 | TC-FILE-006 | — | COVERED ✓ (detection-only, no violation path) |
| RULE-FILE-006 | TC-FILE-007 | — | COVERED ✓ (behavioral — content purge verified) |
| RULE-FILE-007 | — | TC-FILE-008 | COVERED ✓ (violation-only rule) |

Note: API-ID coverage table (API-FILE-001..005, 5/5 covered, 0 deferred) also present in source; omitted here per RULE-ID-only extraction scope, see source SECTION D for full API coverage.

## MODULE GOVERNANCE INDEX
Note: Module Governance Index section not found in source — omitted.

## FIELD-ID / API-ID / PLAN-ID NAMESPACE
| ID Type | Last Assigned |
|---|---|
| FIELD-[FILE] | FIELD-0027 |
| API-[FILE] | API-FILE-005 |
| ERR-[FILE] | ERR-FILE-0009 |
| QR-[FILE] | QR-FILE-007 |
| PLAN-[FILE] | PLAN-FILE-001 |
