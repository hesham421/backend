<!-- Source: PHASE:F3 / SUB:SCR-ORG-005 -->

## F3 — SCR-ORG-005 Validation Rules (Tree Screen)

```
F3-BC-RULE-1 : costCenterCode — READ-ONLY

F3-VALIDATION — RULE-ORG-015 — Branch required:
  ERR-ID: ERR-0015 | Message-AR: يرجى اختيار فرع نشط لربط مركز التكلفة به.

F3-VALIDATION — RULE-ORG-018 — Parent must be active:
  ERR-ID: ERR-0018 | Message-AR: لا يمكن تعيين مركز تكلفة غير نشط أباً لمركز التكلفة. يرجى اختيار مركز تكلفة نشط.

F3-VALIDATION — RULE-ORG-008 — Circular reference:
  ERR-ID: ERR-0008 | Message-AR: لا يمكن تعيين مركز التكلفة هذا أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري.

F3-VALIDATION — nodeTypeId: LOV-ORG-004 | costCenterTypeId: LOV-ORG-005 — both REQUIRED
```
