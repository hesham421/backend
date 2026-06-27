<!-- Source: PHASE:F3 / SUB:SCR-ORG-002 -->

## F3 — SCR-ORG-002 Validation Rules

```
F3-BC-RULE-1 : branchCode — READ-ONLY always

F3-VALIDATION — RULE-ORG-013 — LegalEntity required:
  Field          : legalEntityFk | DB: LEGAL_ENTITY_FK | DBF-0024
  Validation type: REQUIRED + LOV_VALID (active LegalEntity)
  ERR-ID         : ERR-0013
  When evaluated : ON_SUBMIT (server-side — also client REQUIRED check)
  Message shown  : messageAr: يرجى اختيار كيان قانوني نشط لربط الفرع به.

F3-VALIDATION — branchTypeId:
  Validation type: REQUIRED + LOV_VALID
  LOV-ID: LOV-ORG-002 | LOOKUP_CODE: BRANCH_TYPE
  LOV endpoint: GET /api/lookups/BRANCH_TYPE?active=true

F3-VALIDATION — nameAr/nameEn: REQUIRED + LENGTH constraints (same as SCR-ORG-001)

F3-VALIDATION — RULE-ORG-003/004/005 on Deactivate:
  Triggered on DELETE response — surface ERR-0003/0004/0005 inline with Arabic message
```
