# REGISTRY EXTRACT — registry-exec-ORG.md
══════════════════════════════════════════════════════════════════
Module          : Organization (ORG)
Source artifact : execution-plan-org-001.md
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4 audit
══════════════════════════════════════════════════════════════════

---

## HEADER

| Field          | Value                           |
|----------------|---------------------------------|
| Module Name    | Organization                    |
| Module Prefix  | ORG                             |
| Plan ID        | PLAN-ORG-001                    |
| DBS-ID         | DBS-ORG-001                     |
| ALIGN Status   | ✓ PASSED (OVERALL ALIGN GATE: PASSED ✓) |

---

## FIELD-ID REGISTER (DB Alignment Manifest)

| FIELD-ID   | DBF-ID   | Plan Type          | FK/XM-ID                    | Match Status |
|------------|----------|--------------------|-----------------------------|--------------|
| FIELD-0001 | DBF-0001 | PK                 | —                           | ALIGNED ✓    |
| FIELD-0002 | DBF-0002 | Name-AR            | —                           | ALIGNED ✓    |
| FIELD-0003 | DBF-0003 | Name-EN            | —                           | ALIGNED ✓    |
| FIELD-0004 | DBF-0004 | FLAG               | —                           | ALIGNED ✓    |
| FIELD-0005 | DBF-0009 | PK                 | —                           | ALIGNED ✓    |
| FIELD-0006 | DBF-0010 | BUSINESS-CODE      | UQ_ORG_LE_CODE              | ALIGNED ✓    |
| FIELD-0007 | DBF-0011 | Name-AR            | —                           | ALIGNED ✓    |
| FIELD-0008 | DBF-0012 | Name-EN            | —                           | ALIGNED ✓    |
| FIELD-0009 | DBF-0013 | LOV (LOV-ORG-001)  | —                           | ALIGNED ✓    |
| FIELD-0010 | DBF-0014 | FLAG               | CHK_ORG_LE_ACTIVE           | ALIGNED ✓    |
| FIELD-0011 | DBF-0015 | TEXT               | —                           | ALIGNED ✓    |
| FIELD-0012 | DBF-0020 | PK                 | —                           | ALIGNED ✓    |
| FIELD-0013 | DBF-0021 | BUSINESS-CODE      | UQ_ORG_BR_CODE_LE           | ALIGNED ✓    |
| FIELD-0014 | DBF-0022 | Name-AR            | —                           | ALIGNED ✓    |
| FIELD-0015 | DBF-0023 | Name-EN            | —                           | ALIGNED ✓    |
| FIELD-0016 | DBF-0024 | FK                 | FK_ORG_BR_LE                | ALIGNED ✓    |
| FIELD-0017 | DBF-0025 | LOV (LOV-ORG-002)  | —                           | ALIGNED ✓    |
| FIELD-0018 | DBF-0026 | FLAG               | CHK_ORG_BR_ACTIVE           | ALIGNED ✓    |
| FIELD-0019 | DBF-0027 | TEXT               | —                           | ALIGNED ✓    |
| FIELD-0020 | DBF-0032 | PK                 | —                           | ALIGNED ✓    |
| FIELD-0021 | DBF-0033 | BUSINESS-CODE      | UQ_ORG_RG_CODE_LE           | ALIGNED ✓    |
| FIELD-0022 | DBF-0034 | Name-AR            | —                           | ALIGNED ✓    |
| FIELD-0023 | DBF-0035 | Name-EN            | —                           | ALIGNED ✓    |
| FIELD-0024 | DBF-0036 | FK                 | FK_ORG_RG_LE                | ALIGNED ✓    |
| FIELD-0025 | DBF-0037 | FK (Ref Table)     | FK_ORG_RG_RT / LOV-ORG-007  | ALIGNED ✓    |
| FIELD-0026 | DBF-0038 | FLAG               | CHK_ORG_RG_ACTIVE           | ALIGNED ✓    |
| FIELD-0027 | DBF-0039 | TEXT               | —                           | ALIGNED ✓    |
| FIELD-0028 | DBF-0044 | PK                 | —                           | ALIGNED ✓    |
| FIELD-0029 | DBF-0045 | BUSINESS-CODE      | UQ_ORG_DEP_CODE_BR          | ALIGNED ✓    |
| FIELD-0030 | DBF-0046 | Name-AR            | —                           | ALIGNED ✓    |
| FIELD-0031 | DBF-0047 | Name-EN            | —                           | ALIGNED ✓    |
| FIELD-0032 | DBF-0048 | FK                 | FK_ORG_DEP_BR               | ALIGNED ✓    |
| FIELD-0033 | DBF-0049 | FK-SELF (NULLABLE) | FK_ORG_DEP_SELF             | ALIGNED ✓    |
| FIELD-0034 | DBF-0050 | LOV (LOV-ORG-003)  | —                           | ALIGNED ✓    |
| FIELD-0035 | DBF-0051 | FLAG               | CHK_ORG_DEP_ACTIVE          | ALIGNED ✓    |
| FIELD-0036 | DBF-0052 | TEXT               | —                           | ALIGNED ✓    |
| FIELD-0037 | DBF-0057 | PK                 | —                           | ALIGNED ✓    |
| FIELD-0038 | DBF-0058 | BUSINESS-CODE      | UQ_ORG_CC_CODE_BR           | ALIGNED ✓    |
| FIELD-0039 | DBF-0059 | Name-AR            | —                           | ALIGNED ✓    |
| FIELD-0040 | DBF-0060 | Name-EN            | —                           | ALIGNED ✓    |
| FIELD-0041 | DBF-0061 | FK                 | FK_ORG_CC_BR                | ALIGNED ✓    |
| FIELD-0042 | DBF-0062 | FK-SELF (NULLABLE) | FK_ORG_CC_SELF              | ALIGNED ✓    |
| FIELD-0043 | DBF-0063 | LOV (LOV-ORG-004)  | —                           | ALIGNED ✓    |
| FIELD-0044 | DBF-0064 | LOV (LOV-ORG-005)  | —                           | ALIGNED ✓    |
| FIELD-0045 | DBF-0065 | FLAG               | CHK_ORG_CC_ACTIVE           | ALIGNED ✓    |
| FIELD-0046 | DBF-0066 | TEXT               | —                           | ALIGNED ✓    |
| FIELD-0047 | DBF-0071 | PK                 | —                           | ALIGNED ✓    |
| FIELD-0048 | DBF-0072 | BUSINESS-CODE      | UQ_ORG_PC_CODE_LE           | ALIGNED ✓    |
| FIELD-0049 | DBF-0073 | Name-AR            | —                           | ALIGNED ✓    |
| FIELD-0050 | DBF-0074 | Name-EN            | —                           | ALIGNED ✓    |
| FIELD-0051 | DBF-0075 | FK                 | FK_ORG_PC_LE                | ALIGNED ✓    |
| FIELD-0052 | DBF-0076 | FLAG               | CHK_ORG_PC_ACTIVE           | ALIGNED ✓    |
| FIELD-0053 | DBF-0077 | TEXT               | —                           | ALIGNED ✓    |
| FIELD-0054 | DBF-0082 | PK                 | —                           | ALIGNED ✓    |
| FIELD-0055 | DBF-0083 | BUSINESS-CODE      | UQ_ORG_LS_CODE_BR           | ALIGNED ✓    |
| FIELD-0056 | DBF-0084 | Name-AR            | —                           | ALIGNED ✓    |
| FIELD-0057 | DBF-0085 | Name-EN            | —                           | ALIGNED ✓    |
| FIELD-0058 | DBF-0086 | FK                 | FK_ORG_LS_BR                | ALIGNED ✓    |
| FIELD-0059 | DBF-0087 | LOV (LOV-ORG-006)  | —                           | ALIGNED ✓    |
| FIELD-0060 | DBF-0088 | FLAG               | CHK_ORG_LS_ACTIVE           | ALIGNED ✓    |
| FIELD-0061 | DBF-0089 | TEXT               | —                           | ALIGNED ✓    |

Total: 61 FIELD-IDs (FIELD-0001..FIELD-0061) — all ALIGNED ✓

---

## ERROR CATALOG (codes only)

| ERR-ID   | Source RULE-ID  | HTTP Status |
|----------|-----------------|-------------|
| ERR-0001 | RULE-ORG-001    | 409         |
| ERR-0002 | RULE-ORG-002    | 409         |
| ERR-0003 | RULE-ORG-003    | 409         |
| ERR-0004 | RULE-ORG-004    | 409         |
| ERR-0005 | RULE-ORG-005    | 409         |
| ERR-0006 | RULE-ORG-006    | 409         |
| ERR-0007 | RULE-ORG-007    | 409         |
| ERR-0008 | RULE-ORG-008    | 409         |
| ERR-0009 | RULE-ORG-009    | 400         |
| ERR-0010 | RULE-ORG-010    | 400         |
| ERR-0011 | RULE-ORG-011    | 400         |
| ERR-0012 | RULE-ORG-012    | 409         |
| ERR-0013 | RULE-ORG-013    | 400         |
| ERR-0014 | RULE-ORG-014    | 400         |
| ERR-0015 | RULE-ORG-015    | 400         |
| ERR-0016 | RULE-ORG-016    | — (arch — no user-facing message) |
| ERR-0017 | RULE-ORG-017    | 400         |
| ERR-0018 | RULE-ORG-018    | 400         |
| ERR-0019 | RULE-ORG-019    | 400         |
| ERR-0020 | RULE-ORG-020    | 400         |
| ERR-0100 | PLATFORM-STD    | 500         |
| ERR-0101 | PLATFORM-STD    | 404         |

Note: ERR-0016 unassigned — RULE-ORG-016 is architectural, no user-facing message.

---

## INT SUMMARY (XM execution status)

XM Register: EMPTY — ROOT MODULE
No XM-IDs to report. Organization has zero outbound cross-module dependencies.

Inbound stubs (informational — deferred):
- INBOUND-STUB-ORG-001 : Finance → CostCenter / ProfitCenter | DEFERRED
- INBOUND-STUB-ORG-002 : Inventory → LocationSite | DEFERRED

INT-C Gate: PASSED ✓ | INT-R Gate: PASSED ✓

---

## TC COVERAGE SUMMARY

| RULE-ID       | Happy TC         | Violation TC       | Status                                        |
|---------------|------------------|--------------------|-----------------------------------------------|
| RULE-ORG-001  | TC-ORG-001       | TC-ORG-002         | COVERED ✓                                     |
| RULE-ORG-002  | TC-ORG-001       | TC-ORG-003         | COVERED ✓                                     |
| RULE-ORG-003  | TC-ORG-004       | TC-ORG-005         | COVERED ✓                                     |
| RULE-ORG-004  | TC-ORG-004       | TC-ORG-006         | COVERED ✓                                     |
| RULE-ORG-005  | TC-ORG-004       | TC-ORG-007         | COVERED ✓                                     |
| RULE-ORG-006  | TC-ORG-008       | TC-ORG-009         | COVERED ✓                                     |
| RULE-ORG-007  | TC-ORG-010       | TC-ORG-011         | COVERED ✓                                     |
| RULE-ORG-008  | TC-ORG-012       | TC-ORG-013         | COVERED ✓                                     |
| RULE-ORG-009  | DEFERRED ⏸       | DEFERRED ⏸         | DEFERRED ⚠                                    |
| RULE-ORG-010  | DEFERRED ⏸       | DEFERRED ⏸         | DEFERRED ⚠                                    |
| RULE-ORG-011  | TC-ORG-014       | TC-ORG-015         | COVERED ✓ (Test-Hint+MANDATORY-J-2)           |
| RULE-ORG-012  | TC-ORG-017       | TC-ORG-018         | COVERED ✓                                     |
| RULE-ORG-013  | TC-ORG-019       | TC-ORG-020         | COVERED ✓                                     |
| RULE-ORG-014  | TC-ORG-010       | TC-ORG-021         | COVERED ✓                                     |
| RULE-ORG-015  | TC-ORG-012       | TC-ORG-022         | COVERED ✓                                     |
| RULE-ORG-016  | TC-ORG-017       | —                  | COVERED ✓ (arch — happy only)                 |
| RULE-ORG-017  | TC-ORG-023       | TC-ORG-024         | COVERED ✓                                     |
| RULE-ORG-018  | TC-ORG-025       | TC-ORG-026         | COVERED ✓                                     |
| RULE-ORG-019  | TC-ORG-027       | TC-ORG-028         | COVERED ✓                                     |
| RULE-ORG-020  | TC-ORG-029       | TC-ORG-030         | COVERED ✓                                     |

Summary: 18/20 covered | 2 DEFERRED (RULE-ORG-009, RULE-ORG-010) | 0 gaps

---

## MODULE GOVERNANCE INDEX (state snapshot)

| Field                  | Value                                      |
|------------------------|--------------------------------------------|
| Module                 | Organization (ORG-001)                     |
| Plan ID                | PLAN-ORG-001                               |
| DBS-ID                 | DBS-ORG-001                               |
| SRS Source             | srs-org-001.md v1.0                       |
| Governance Status      | GOVERNED ✓ MODE 2 — PLAN-ORG-001 (ALIGN GATE PASSED ✓) |
| Version                | 1.0                                        |
| Align Gate             | PASSED ✓                                   |
| Date                   | 2026-06-23                                 |
| Entities               | 8 (ENTITY-ORG-001..008)                    |
| Fields                 | 61 (FIELD-0001..0061)                      |
| APIs                   | 47 (API-ORG-001..047)                      |
| Rules                  | 20 (RULE-ORG-001..020)                     |
| ERR-IDs                | 20 business + 2 platform                   |
| LOVs                   | 7 (LOV-ORG-001..007)                       |
| Screens                | 7 (SCR-ORG-001..007)                       |
| XM Dependencies        | 0 (ROOT MODULE)                            |
| Open Questions         | 1 (OQ-001 — deferred)                      |
| Next Stage             | MODE 2.5 → test-plan-org-001.md            |

---

## FIELD-ID / API-ID / PLAN-ID NAMESPACE

| ID Type  | Last Assigned       |
|----------|---------------------|
| FIELD-ORG | last = FIELD-0061  |
| API-ORG   | last = API-ORG-047 |
| PLAN-ORG  | last = PLAN-ORG-001|
| ERR       | last = ERR-0020 (business); ERR-0101 (platform) |
| QR-ORG    | last = QR-ORG-0048 |
| DRV-ORG   | last = DRV-ORG-009 |
