<!-- Source: PHASE:SECTION-A -->

# SECTION A — ERROR CATALOG (CANONICAL)

| ERR-ID | RULE-ID | HTTP | Message-AR | Message-EN |
|---|---|---|---|---|
| ERR-ORG-0001 | RULE-ORG-015 | 400 | الاسم مُستخدم مسبقاً ضمن نفس النطاق — يرجى اختيار اسم مختلف | Name already exists within the same parent scope — please choose a different name |
| ERR-ORG-0002 | RULE-ORG-012 | 409 | تعذّر إنشاء رمز الأعمال — تعارض في التسلسل. يرجى المحاولة مرة أخرى | Business Code generation failed due to sequence conflict. Please retry |
| ERR-ORG-0003 | RULE-ORG-014 / RULE-ORG-016 | 400 | رمز الأعمال / حقول التدقيق لا تُقبل ضمن طلبات الإنشاء أو التعديل | Business Code / audit fields are not accepted in create or update requests |
| ERR-ORG-0004 | — (LocalizedException, not NotFoundException) | 404 | السجل غير موجود | Record not found |
| ERR-ORG-0005 | RULE-ORG-001 | 409 | لا يمكن إلغاء تفعيل الكيان القانوني لوجود فروع نشطة مرتبطة به | Cannot deactivate Legal Entity: active branches exist |
| ERR-ORG-0006 | RULE-ORG-002 | 409 | لا يمكن إلغاء تفعيل الكيان القانوني لوجود مراكز ربح نشطة مرتبطة به | Cannot deactivate Legal Entity: active profit centers exist |
| ERR-ORG-0007 | RULE-ORG-003 | 409 | لا يمكن إلغاء تفعيل الفرع لوجود أقسام نشطة مرتبطة به | Cannot deactivate Branch: active departments exist |
| ERR-ORG-0008 | RULE-ORG-004 | 409 | لا يمكن إلغاء تفعيل الفرع لوجود مراكز تكلفة نشطة مرتبطة به | Cannot deactivate Branch: active cost centers exist |
| ERR-ORG-0009 | RULE-ORG-005 | 409 | لا يمكن إلغاء تفعيل الفرع لوجود مواقع عمل نشطة مرتبطة به | Cannot deactivate Branch: active location sites exist |
| ERR-ORG-0010 | RULE-ORG-006 | 409 | لا يمكن إلغاء تفعيل المنطقة لوجود فروع نشطة مرتبطة بها | Cannot deactivate Region: active branches reference it |
| ERR-ORG-0011 | RULE-ORG-007 | 400 | لا يمكن تعيين هذا القسم كقسم أب — سيؤدي إلى دورة في هيكل الأقسام | Cannot set parent department: circular reference detected |
| ERR-ORG-0012 | RULE-ORG-008 | 400 | لا يمكن تعيين مركز التكلفة هذا كأب — سيؤدي إلى دورة في هيكل مراكز التكلفة | Cannot set parent cost center: circular reference detected |
| ERR-ORG-0013 | RULE-ORG-011 | 400 | رمز الأعمال لا يمكن تعديله بعد الحفظ الأول — هذا الحقل محمي ونهائي | Business Code is immutable after first save |
| ERR-ORG-0014 | RULE-ORG-017 | 200 (warning, non-blocking) | تحذير: المنطقة مُستخدمة من موديولات أخرى — تأكد من مراجعة الأثر قبل إلغاء التفعيل | Warning: Region is referenced by other modules — review impact before deactivating |
| ERR-ORG-0015 | RULE-ORG-018 | 400 | لا يمكن إنشاء فرع تحت كيان قانوني غير نشط | Cannot create a Branch under an inactive Legal Entity |
| ERR-ORG-0016 | RULE-ORG-019 | 400 | لا يمكن إنشاء قسم أو مركز تكلفة أو موقع عمل تحت فرع غير نشط | Cannot create organizational unit under an inactive Branch |
| ERR-ORG-0017 | RULE-ORG-020 | 400 | لا يمكن تغيير نوع العقدة (ملخص/تفصيل) بعد الحفظ | Node type (SUMMARY/DETAIL) cannot be changed after initial save |
| ERR-ORG-0018 | RULE-ORG-009 / RULE-ORG-010 | 400 | لا يمكن استخدام عقدة من نوع (ملخص) في السجلات التشغيلية | Cannot assign a SUMMARY node to transactional records (enforced by consuming modules) |

SVC+API phase references this catalog by ERR-ID only — no duplicate table maintained elsewhere (CONTRACT-4 compliant, OPTION A).
