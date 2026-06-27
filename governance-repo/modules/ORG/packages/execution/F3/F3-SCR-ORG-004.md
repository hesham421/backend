<!-- Source: PHASE:F3 / SUB:SCR-ORG-004 -->

## F3 — SCR-ORG-004 Validation Rules (Tree Screen)

```
F3-BC-RULE-1 : deptCode — READ-ONLY

F3-VALIDATION — RULE-ORG-014 — Branch required:
  Field: branchFk | ERR-ID: ERR-0014
  Message-AR: يرجى اختيار فرع نشط لربط القسم به.
  Validation: REQUIRED on create — branchFk from tree context (pre-selected when adding child)

F3-VALIDATION — RULE-ORG-017 — Parent must be active:
  Field: parentDepartmentFk | ERR-ID: ERR-0017
  Message-AR: لا يمكن تعيين قسم غير نشط أباً للقسم. يرجى اختيار قسم نشط.
  Validation: LOV_VALID — parent dropdown loads only active departments

F3-VALIDATION — RULE-ORG-007 — Circular reference:
  Field: parentDepartmentFk | ERR-ID: ERR-0007
  Message-AR: لا يمكن تعيين هذا القسم أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري.
  Validation: server-side (409 response on submit)

F3-VALIDATION — nodeTypeId:
  LOV-ID: LOV-ORG-003 | LOOKUP_CODE: DEPARTMENT_NODE_TYPE
  Values: SUMMARY / DETAIL — REQUIRED
```
