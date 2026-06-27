<!-- Source: PHASE:DATA-DOM / SUB:REFERENCE -->


## DATA+DOM — Reference Table (ENTITY-ORG-008)

### ENTITY-ORG-008 — RegionType (Reference Table — PRIVATE)

```
Java Class       : RegionType extends AuditableEntity
DB Table         : ORG_REGION_TYPE
PK Generation    : SEQ_ORG_REGION_TYPE.NEXTVAL
Type             : PRIVATE Reference Table — Admin-managed, not MD_LOOKUP_DETAIL

FIELD DECLARATIONS:
  FIELD-0001 │ regionTypePk │ DB: REGION_TYPE_PK │ NUMBER(10) NOT NULL │ @Id
  FIELD-0002 │ nameAr       │ DB: NAME_AR        │ VARCHAR2(200) NOT NULL
  FIELD-0003 │ nameEn       │ DB: NAME_EN        │ VARCHAR2(100) NOT NULL
  FIELD-0004 │ isActiveFl   │ DB: IS_ACTIVE_FL   │ NUMBER(1) DEFAULT 1

No Business Code (Reference Table).
No LOV fields.
No complex domain rules — simple CRUD + deactivation.

SEED DATA (from SRS A3 ENTITY-ORG-008):
  nameAr: جغرافي  | nameEn: GEOGRAPHIC
  nameAr: مبيعات  | nameEn: SALES
  nameAr: تشغيلي  | nameEn: OPERATIONAL

REPOSITORY OPERATIONS REQUIRED:
  → QR-ORG-0048 : FIND_ALL active (API-ORG-020 — for LOV loading)
```

**DATA+DOM Governance Rules:**
```
BC-DOM-RULE-1 — All 7 business codes use NumberingEngine exclusively (RULE-ORG-016)
BC-DOM-RULE-2 — All business codes immutable after creation (RULE-ORG-011)
LOC-DOM-RULE-1 — nameAr AND nameEn mandatory NOT NULL on all entities
SEC-DOM-RULE-1 — Soft deactivation: isActiveFl = 0 (no physical delete)
BIND-RULE-1   — Every DB column reference uses exact Oracle name from DBF-ID
BIND-RULE-2   — Every sequence uses SEQ_[EXACT_TABLE_NAME].NEXTVAL
BIND-RULE-3   — LOV-ORG-001..006 stored as DETAIL_CODE (VARCHAR2)
              — LOV-ORG-007 stored as NUMBER FK (DRV-ORG-001)
BIND-RULE-4   — All RULE text is exact from SRS — not paraphrase
```

**DATA+DOM Gate: PASSED ✓**
```
[ ✓ ] 8 entities fully declared
[ ✓ ] All FIELD-IDs assigned (FIELD-0001..0061)
[ ✓ ] All sequences named exactly from db-script.md
[ ✓ ] All LOV LOOKUP_CODEs exact from SRS A5
[ ✓ ] All RULE texts exact from SRS A4
[ ✓ ] DRV-ORG-001 documented (RegionType FK vs LOV pattern)
[ ✓ ] Tree entities (ENTITY-ORG-004, 005) circular reference rules declared
[ ✓ ] 48 QR-IDs assigned (QR-ORG-0001..0048)
```

