# REGISTRY EXTRACT — registry-srs-MDM
══════════════════════════════════════════════════════════════════
Module          : Master Data (MDM)
Source artifact : srs.md (v1.3, 2026-09-04)
Extracted by    : P-REG (mechanical extraction — not a governance artifact)
Status          : SESSION INPUT ONLY — not loaded as Project Instruction,
                  not a Truth Layer artifact, not subject to P4.1/P4.2 audit
══════════════════════════════════════════════════════════════════

## HEADER
Module name    : Master Data
Module Prefix  : MDM
OQ count       : 2 (both RESOLVED — see OQ LOG STATUS)

## ENTITIES
| ENTITY-ID | Entity Name | Type |
|---|---|---|
| ENTITY-MDM-001 | LookupType (MASTER) | PRIVATE |
| ENTITY-MDM-002 | LookupValue (DETAIL) | PRIVATE |

## RULES
| RULE-ID | Short Title | Test-Hint |
|---|---|---|
| RULE-MDM-001 | Type code uniqueness | — |
| RULE-MDM-002 | Type code immutable after creation | تحقّق أن محاولة تغيير typeCode عبر PUT تُرفَض حتى مع صلاحية UPDATE |
| RULE-MDM-003 | Value code uniqueness within type | تحقّق أن التفرُّد ضمن النوع فقط — نفس الرمز مسموح تحت نوع آخر |
| RULE-MDM-004 | Value code immutable after creation | — |
| RULE-MDM-005 | Both Arabic/English names mandatory | — |
| RULE-MDM-006 | Soft deactivation only; block deactivating type with active values | تحقّق أن تعطيل نوع/قيمة لا يحذف صفوفًا ولا يؤثّر على مراجع المستهلكين المخزّنة |

## LOVs
None. MDM owns no LOV-ID — its own entities (LookupType/LookupValue) ARE
the platform's shared lookup mechanism (srs §A5).

## LIFECYCLE STATES
Not applicable (SCR-5) — no statusId field; exclusion managed via
isActiveFl only. No Approval Flow (RULE-13 = OFF).

## DEPENDENCIES
| Type | Target ENTITY-ID / Note | Target Module | XM candidate |
|---|---|---|---|
| USES (library) | Common Utils (exceptions/config/events) | CU | No |
| SOFT-READ | createdBy audit identity | SEC | No — standard audit pattern, not XM |

## SCREENS
| SCR-ID | page_code | Screen Name | Pattern |
|---|---|---|---|
| SCR-MDM-001 | MDM_LOOKUP | إدارة القوائم المرجعية (Reference Data Lookup Management) | PATTERN-1 — Composite (Master+Detail), CORE-9 |

## APIs
| API-ID | Method | Endpoint | Owning SCR-ID |
|---|---|---|---|
| API-MDM-001 | POST | /api/v1/mdm/lookup-types | SCR-MDM-001 |
| API-MDM-002 | GET | /api/v1/mdm/lookup-types | SCR-MDM-001 |
| API-MDM-003 | PUT | /api/v1/mdm/lookup-types/{id} | SCR-MDM-001 |
| API-MDM-004 | DELETE | /api/v1/mdm/lookup-types/{id} | SCR-MDM-001 |
| API-MDM-005 | GET | /api/v1/mdm/lookup-types/{id} | SCR-MDM-001 |
| API-MDM-006 | POST | /api/v1/mdm/lookup-types/{typeId}/values | SCR-MDM-001 |
| API-MDM-007 | GET | /api/v1/mdm/lookup-types/{typeId}/values | SCR-MDM-001 |
| API-MDM-008 | PUT | /api/v1/mdm/lookup-values/{id} | SCR-MDM-001 |
| API-MDM-009 | DELETE | /api/v1/mdm/lookup-values/{id} | SCR-MDM-001 |
| API-MDM-010 | GET | /api/v1/mdm/lookup-values/{id} | SCR-MDM-001 |
| API-MDM-011 | GET | /api/v1/mdm/lookups/{typeCode} | SCR-MDM-001 (platform-wide consumption endpoint, auth-only per srs §B5) |

## PERMISSIONS
| PERM Name | Linked SCR-ID(s) |
|---|---|
| PERM_MDM_LOOKUP_VIEW | SCR-MDM-001 |
| PERM_MDM_LOOKUP_CREATE | SCR-MDM-001 |
| PERM_MDM_LOOKUP_UPDATE | SCR-MDM-001 |
| PERM_MDM_LOOKUP_DELETE | SCR-MDM-001 |

## OQ LOG STATUS
| OQ-ID | Status | One-line topic | Escalation |
|---|---|---|---|
| OQ-MDM-001 | RESOLVED | Admin surface: composite screen vs. provider-only | LOCAL |
| OQ-MDM-002 | RESOLVED | Governed source for FILE_FILE_TYPE seed enumeration | Architect-delegated |

---
*End of registry-srs-MDM.md*
