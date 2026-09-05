# REGISTRY EXTRACT — registry-exec-be-MDM
══════════════════════════════════════════════════════════════════
Module          : Master Data (MDM)
Source artifact : backend-execution-plan-MDM.md (PLAN-MDM-001, Project 3.1 PASS 1, 2026-09-04)
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4.1/P4.2 audit
══════════════════════════════════════════════════════════════════

## HEADER
Module name    : Master Data
Module Prefix  : MDM

## FIELD-ID REGISTER (DB Alignment Manifest)
| FIELD-ID | DBF-ID | Plan Type | FK/XM-ID | Match Status |
|---|---|---|---|---|
| FIELD-0001 | DBF-0001 | Long | — | ✓ |
| FIELD-0002 | DBF-0002 | String(50) | — | ✓ |
| FIELD-0003 | DBF-0003 | String(200) | — | ✓ |
| FIELD-0004 | DBF-0004 | String(100) | — | ✓ |
| FIELD-0005 | DBF-0005 | Boolean | — | ✓ |
| FIELD-0006 | DBF-0006 | String(2000) | — | ✓ |
| FIELD-0007 | DBF-0007 | Long | — | ✓ |
| FIELD-0008 | DBF-0008 | Long | intra-module FK → ENTITY-MDM-001 | ✓ |
| FIELD-0009 | DBF-0009 | String(50) | — | ✓ |
| FIELD-0010 | DBF-0010 | String(200) | — | ✓ |
| FIELD-0011 | DBF-0011 | String(100) | — | ✓ |
| FIELD-0012 | DBF-0012 | Short | — | ✓ |
| FIELD-0013 | DBF-0013 | Boolean | — | ✓ |
| FIELD-0014 | DBF-0014 | String(2000) | — | ✓ |
Result: 14/14 aligned, 0 mismatches, 0 deferred.

## ERROR CATALOG
| ERR-ID | Source RULE-ID | HTTP Status |
|---|---|---|
| ERR-0001 | RULE-MDM-001 | 409 |
| ERR-0002 | RULE-MDM-005 | 400 |
| ERR-0003 | RULE-MDM-002 | 409 |
| ERR-0004 | RULE-MDM-005 | 400 |
| ERR-0005 | RULE-MDM-006 | 409 |
| ERR-0006 | PLATFORM-STD | 404 |
| ERR-0007 | PLATFORM-STD | 404 |
| ERR-0008 | RULE-MDM-003 | 409 |
| ERR-0009 | RULE-MDM-005 | 400 |
| ERR-0010 | RULE-MDM-004 | 409 |
| ERR-0011 | RULE-MDM-005 | 400 |
| ERR-0012 | PLATFORM-STD | 404 |
Total: 12 ERR-IDs.

## INT SUMMARY (XM execution status)
None — 0 XM-IDs in this module (MDM is a pure provider consumed via
REST API; see db-script §3 XM Register).

## TC COVERAGE SUMMARY — BACKEND
Note: SECTION D (TC coverage summary) not found in source — omitted.
backend-test-plan-MDM.md has not been generated yet (Test Generation
Engine runs separately, outside this pipeline, per source's own
Agent Handoff Summary).

## MODULE GOVERNANCE INDEX (state snapshot)
Note: MODULE GOVERNANCE INDEX section not found in source — omitted.

## FIELD-ID / API-ID / PLAN-ID NAMESPACE
FIELD-MDM : last = FIELD-0014
API-MDM   : last = API-MDM-011
RULE-MDM  : last = RULE-MDM-006 (RULE-MDM-007 cited in db-script is a
            DB-structural FK constraint, not promoted to a domain
            RULE-ID — see source's DRV-003)
ERR-MDM   : last = ERR-0012
QR-MDM    : last = QR-MDM-0015
PLAN-MDM  : PLAN-MDM-001

---
*End of registry-exec-be-MDM.md*
