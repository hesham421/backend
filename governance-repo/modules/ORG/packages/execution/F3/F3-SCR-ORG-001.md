<!-- Source: PHASE:F3 / SUB:SCR-ORG-001 -->

## F3 — SCR-ORG-001 Validation Rules

```
F3-BC-RULE-1 : legalEntityCode — READ-ONLY on all screens — never an input field
F3-BC-RULE-2 : On create form — legalEntityCode shown as read-only display (empty until save)
F3-BC-RULE-3 : On edit form — legalEntityCode from GET response — shown never editable

F3-VALIDATION — RULE-ORG-012 — Business code uniqueness:
  Field          : legalEntityCode (system-generated)
  Validation type: BUSINESS_RULE (server-side — NumberingEngine handles; surface ERR-0012 on POST error)
  When evaluated : ON_SUBMIT (server returns 409)
  ERR-ID         : ERR-0012
  Message shown  : messageAr when locale=AR, messageEn otherwise

F3-VALIDATION — entityTypeId:
  Validation type: REQUIRED + LOV_VALID
  LOV-ID         : LOV-ORG-001
  LOOKUP_CODE    : LEGAL_ENTITY_TYPE
  LOV load method: loadEntityTypeOptions() (F2 facade)
  Endpoint       : GET /api/lookups/LEGAL_ENTITY_TYPE?active=true
  When evaluated : ON_SUBMIT

F3-VALIDATION — nameAr:
  Field          : nameAr | DB: NAME_AR | DBF-0011
  Validation type: REQUIRED | LENGTH(max=200)
  When evaluated : ON_BLUR + ON_SUBMIT

F3-VALIDATION — nameEn:
  Field          : nameEn | DB: NAME_EN | DBF-0012
  Validation type: REQUIRED | LENGTH(max=100)
  When evaluated : ON_BLUR + ON_SUBMIT

F3-LOC-RULE-1 : Error messages keyed by ERR-ID → Error Catalog — never hardcoded
F3-SEC-RULE-1 : canCreate=false → New button hidden | canEdit=false → fields read-only | canDelete=false → Deactivate hidden
```
