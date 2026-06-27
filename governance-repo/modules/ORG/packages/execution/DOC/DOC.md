<!-- Source: PHASE:DOC -->


# PHASE DOC — Contract Stabilization

## DOC-1: API Contract Summary

```
API CONTRACT SUMMARY — ORG-001 — PLAN-ORG-001
─────────────────────────────────────────────────────────────────
API-ID         │ Endpoint                                      │ Method │ Request DTO              │ Response DTO             │ Stability
───────────────┼───────────────────────────────────────────────┼────────┼──────────────────────────┼──────────────────────────┼──────────
API-ORG-001    │ /api/v1/org/legal-entities                    │ POST   │ LegalEntityCreateRequest │ LegalEntityResponse      │ STABLE
API-ORG-002    │ /api/v1/org/legal-entities                    │ GET    │ [query params]           │ Page<LegalEntityResponse>│ STABLE
API-ORG-003    │ /api/v1/org/legal-entities/{id}               │ GET    │ —                        │ LegalEntityResponse      │ STABLE
API-ORG-004    │ /api/v1/org/legal-entities/{id}               │ PUT    │ LegalEntityUpdateRequest │ LegalEntityResponse      │ STABLE
API-ORG-005    │ /api/v1/org/legal-entities/{id}               │ DELETE │ —                        │ confirmation             │ STABLE
API-ORG-006    │ /api/v1/org/legal-entities/{id}/reactivate    │ PUT    │ —                        │ LegalEntityResponse      │ STABLE
API-ORG-007    │ /api/v1/org/branches                          │ POST   │ BranchCreateRequest      │ BranchResponse           │ STABLE
API-ORG-008    │ /api/v1/org/branches                          │ GET    │ [query params]           │ Page<BranchResponse>     │ STABLE
API-ORG-009    │ /api/v1/org/branches/{id}                     │ GET    │ —                        │ BranchResponse           │ STABLE
API-ORG-010    │ /api/v1/org/branches/{id}                     │ PUT    │ BranchUpdateRequest      │ BranchResponse           │ STABLE
API-ORG-011    │ /api/v1/org/branches/{id}                     │ DELETE │ —                        │ confirmation             │ STABLE
API-ORG-012    │ /api/v1/org/branches/{id}/reactivate          │ PUT    │ —                        │ BranchResponse           │ STABLE
API-ORG-013    │ /api/v1/org/branches/by-legal-entity/{leId}   │ GET    │ isActiveFl?              │ List<BranchResponse>     │ STABLE
API-ORG-014    │ /api/v1/org/regions                           │ POST   │ RegionCreateRequest      │ RegionResponse           │ STABLE
API-ORG-015    │ /api/v1/org/regions                           │ GET    │ [query params]           │ Page<RegionResponse>     │ STABLE
API-ORG-016    │ /api/v1/org/regions/{id}                      │ GET    │ —                        │ RegionResponse           │ STABLE
API-ORG-017    │ /api/v1/org/regions/{id}                      │ PUT    │ RegionUpdateRequest      │ RegionResponse           │ STABLE
API-ORG-018    │ /api/v1/org/regions/{id}                      │ DELETE │ —                        │ confirmation             │ STABLE (OQ-001 may extend)
API-ORG-019    │ /api/v1/org/regions/{id}/reactivate           │ PUT    │ —                        │ RegionResponse           │ STABLE
API-ORG-020    │ /api/v1/org/region-types                      │ GET    │ isActiveFl?              │ List<RegionTypeResponse> │ STABLE
API-ORG-021    │ /api/v1/org/departments                       │ POST   │ DepartmentCreateRequest  │ DepartmentResponse       │ STABLE
API-ORG-022    │ /api/v1/org/departments/tree                  │ GET    │ branchFk, isActiveFl?    │ List<DeptTreeNode>       │ STABLE
API-ORG-023    │ /api/v1/org/departments                       │ GET    │ [query params]           │ Page<DepartmentResponse> │ STABLE
API-ORG-024    │ /api/v1/org/departments/{id}                  │ GET    │ —                        │ DepartmentResponse       │ STABLE
API-ORG-025    │ /api/v1/org/departments/{id}                  │ PUT    │ DepartmentUpdateRequest  │ DepartmentResponse       │ STABLE
API-ORG-026    │ /api/v1/org/departments/{id}                  │ DELETE │ —                        │ confirmation             │ STABLE
API-ORG-027    │ /api/v1/org/departments/{id}/reactivate       │ PUT    │ —                        │ DepartmentResponse       │ STABLE
API-ORG-028    │ /api/v1/org/cost-centers                      │ POST   │ CostCenterCreateRequest  │ CostCenterResponse       │ STABLE
API-ORG-029    │ /api/v1/org/cost-centers/tree                 │ GET    │ branchFk, isActiveFl?    │ List<CCTreeNode>         │ STABLE
API-ORG-030    │ /api/v1/org/cost-centers                      │ GET    │ [query params]           │ Page<CostCenterResponse> │ STABLE
API-ORG-031    │ /api/v1/org/cost-centers/{id}                 │ GET    │ —                        │ CostCenterResponse       │ STABLE
API-ORG-032    │ /api/v1/org/cost-centers/{id}                 │ PUT    │ CostCenterUpdateRequest  │ CostCenterResponse       │ STABLE
API-ORG-033    │ /api/v1/org/cost-centers/{id}                 │ DELETE │ —                        │ confirmation             │ STABLE
API-ORG-034    │ /api/v1/org/cost-centers/{id}/reactivate      │ PUT    │ —                        │ CostCenterResponse       │ STABLE
API-ORG-035    │ /api/v1/org/profit-centers                    │ POST   │ ProfitCenterCreateRequest│ ProfitCenterResponse     │ STABLE
API-ORG-036    │ /api/v1/org/profit-centers                    │ GET    │ [query params]           │ Page<ProfitCenterResponse>│ STABLE
API-ORG-037    │ /api/v1/org/profit-centers/{id}               │ GET    │ —                        │ ProfitCenterResponse     │ STABLE
API-ORG-038    │ /api/v1/org/profit-centers/{id}               │ PUT    │ ProfitCenterUpdateRequest│ ProfitCenterResponse     │ STABLE
API-ORG-039    │ /api/v1/org/profit-centers/{id}               │ DELETE │ —                        │ confirmation             │ STABLE
API-ORG-040    │ /api/v1/org/profit-centers/{id}/reactivate    │ PUT    │ —                        │ ProfitCenterResponse     │ STABLE
API-ORG-041    │ /api/v1/org/location-sites                    │ POST   │ LocationSiteCreateRequest│ LocationSiteResponse     │ STABLE
API-ORG-042    │ /api/v1/org/location-sites                    │ GET    │ [query params]           │ Page<LocationSiteResponse>│ STABLE
API-ORG-043    │ /api/v1/org/location-sites/{id}               │ GET    │ —                        │ LocationSiteResponse     │ STABLE
API-ORG-044    │ /api/v1/org/location-sites/{id}               │ PUT    │ LocationSiteUpdateRequest│ LocationSiteResponse     │ STABLE
API-ORG-045    │ /api/v1/org/location-sites/{id}               │ DELETE │ —                        │ confirmation             │ STABLE
API-ORG-046    │ /api/v1/org/location-sites/{id}/reactivate    │ PUT    │ —                        │ LocationSiteResponse     │ STABLE
API-ORG-047    │ /api/v1/org/location-sites/by-branch/{branchId}│ GET   │ isActiveFl?              │ List<LocationSiteResponse>│ STABLE
─────────────────────────────────────────────────────────────────
Unstable APIs: API-ORG-018 (may extend on OQ-001 resolution) — otherwise all STABLE
```

## DOC-2: DTO Typing Rules
```
LOV field typing : String (stores DETAIL_CODE from MD_LOOKUP_DETAIL) — never ENUM
                   Exception: regionTypeFk in Region is Long (FK to ORG_REGION_TYPE) — DRV-ORG-001
Business Code    : String — always in ResponseDTO — never in CreateRequest/UpdateRequest
```

## DOC-3: Pagination & Filter Standards
```
PAGINATION & FILTER STANDARDS (PROJECT-STANDARD)
─────────────────────────────────────────────────────────────────
Backend strategy : JPA Page<T> — NO custom pagination wrapper
Request contract : SearchRequest extends BaseSearchContractRequest
                   → PageableBuilder.from(request, ALLOWED_SORT_FIELDS)
Empty result     : HTTP 200 with empty content — NEVER HTTP 404
Filter types     : EXACT (=) for IDs/flags/codes | LIKE (%value%) for name/code text search
Angular frontend : currentPage and pageSize derived from lastSearchRequest — NOT independent state
List endpoints   : API-ORG-013, 020, 022, 029, 047 — return List (not Page) — DRV-ORG-003
─────────────────────────────────────────────────────────────────
```

## ERROR CATALOG — ORG-001

```
ERROR CATALOG — Organization (ORG-001) — PLAN-ORG-001
══════════════════════════════════════════════════════════════════════════
ERR-ID   │ RULE-ID         │ HTTP │ Message-AR                                                            │ Message-EN
─────────┼─────────────────┼──────┼───────────────────────────────────────────────────────────────────────┼────────────────────────────────────────────────────────────────────
ERR-0001 │ RULE-ORG-001    │ 409  │ لا يمكن تعطيل الكيان القانوني لوجود فروع نشطة مرتبطة به. يرجى تعطيل جميع الفروع أولاً. │ Cannot deactivate Legal Entity: active branches exist. Please deactivate all branches first.
ERR-0002 │ RULE-ORG-002    │ 409  │ لا يمكن تعطيل الكيان القانوني لوجود مراكز ربح نشطة مرتبطة به. يرجى تعطيل جميع مراكز الربح أولاً. │ Cannot deactivate Legal Entity: active profit centers exist. Please deactivate all profit centers first.
ERR-0003 │ RULE-ORG-003    │ 409  │ لا يمكن تعطيل الفرع لوجود أقسام نشطة مرتبطة به. يرجى تعطيل جميع الأقسام أولاً. │ Cannot deactivate Branch: active departments exist. Please deactivate all departments first.
ERR-0004 │ RULE-ORG-004    │ 409  │ لا يمكن تعطيل الفرع لوجود مراكز تكلفة نشطة مرتبطة به. يرجى تعطيل جميع مراكز التكلفة أولاً. │ Cannot deactivate Branch: active cost centers exist. Please deactivate all cost centers first.
ERR-0005 │ RULE-ORG-005    │ 409  │ لا يمكن تعطيل الفرع لوجود مواقع جغرافية نشطة مرتبطة به. يرجى تعطيل جميع المواقع أولاً. │ Cannot deactivate Branch: active location sites exist. Please deactivate all location sites first.
ERR-0006 │ RULE-ORG-006    │ 409  │ لا يمكن تعطيل المنطقة لوجود فروع نشطة مرتبطة بها. يرجى إلغاء ربط الفروع أولاً. │ Cannot deactivate Region: active branches reference it. Please unlink branches first.
ERR-0007 │ RULE-ORG-007    │ 409  │ لا يمكن تعيين هذا القسم أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري. │ Cannot set this department as parent: circular reference detected in department hierarchy.
ERR-0008 │ RULE-ORG-008    │ 409  │ لا يمكن تعيين مركز التكلفة هذا أباً لأن ذلك سيُنشئ حلقة دائرية في الهيكل الشجري. │ Cannot set this cost center as parent: circular reference detected in cost center hierarchy.
ERR-0009 │ RULE-ORG-009    │ 400  │ لا يمكن ربط قسم تجميعي (SUMMARY) بسجل معاملة مباشرة. يرجى اختيار قسم تفصيلي (DETAIL). │ Cannot assign a SUMMARY department to a transaction. Please select a DETAIL department.
ERR-0010 │ RULE-ORG-010    │ 400  │ لا يمكن ربط مركز تكلفة تجميعي (SUMMARY) بسجل معاملة مباشرة. يرجى اختيار مركز تكلفة تفصيلي (DETAIL). │ Cannot assign a SUMMARY cost center to a transaction. Please select a DETAIL cost center.
ERR-0011 │ RULE-ORG-011    │ 400  │ رمز الأعمال لا يمكن تعديله بعد الإنشاء الأول — هذه القيمة ثابتة نهائياً. │ Business code is immutable after creation and cannot be modified.
ERR-0012 │ RULE-ORG-012    │ 409  │ رمز الأعمال المُنشأ تلقائياً موجود مسبقاً. يرجى المحاولة مجدداً. │ Generated business code already exists. Please retry the operation.
ERR-0013 │ RULE-ORG-013    │ 400  │ يرجى اختيار كيان قانوني نشط لربط الفرع به. │ A valid active Legal Entity must be selected before saving a Branch.
ERR-0014 │ RULE-ORG-014    │ 400  │ يرجى اختيار فرع نشط لربط القسم به. │ A valid active Branch must be selected before saving a Department.
ERR-0015 │ RULE-ORG-015    │ 400  │ يرجى اختيار فرع نشط لربط مركز التكلفة به. │ A valid active Branch must be selected before saving a CostCenter.
ERR-0017 │ RULE-ORG-017    │ 400  │ لا يمكن تعيين قسم غير نشط أباً للقسم. يرجى اختيار قسم نشط. │ Cannot set an inactive department as parent. Please select an active department.
ERR-0018 │ RULE-ORG-018    │ 400  │ لا يمكن تعيين مركز تكلفة غير نشط أباً لمركز التكلفة. يرجى اختيار مركز تكلفة نشط. │ Cannot set an inactive cost center as parent. Please select an active cost center.
ERR-0019 │ RULE-ORG-019    │ 400  │ يرجى اختيار كيان قانوني نشط لربط المنطقة به. │ A valid active Legal Entity must be selected before saving a Region.
ERR-0020 │ RULE-ORG-020    │ 400  │ يرجى اختيار كيان قانوني نشط لربط مركز الربح به. │ A valid active Legal Entity must be selected before saving a ProfitCenter.
ERR-0100 │ PLATFORM-STD    │ 500  │ حدث خطأ غير متوقع. يرجى المحاولة مجدداً أو التواصل مع الدعم الفني. │ An unexpected error occurred. Please try again or contact support.
ERR-0101 │ PLATFORM-STD    │ 404  │ السجل المطلوب غير موجود. │ The requested record was not found.
══════════════════════════════════════════════════════════════════════════
Note: ERR-0016 not assigned (RULE-ORG-016 is architectural — no user-facing message).
Note: ERR-0009, ERR-0010 registered for consumer module use — enforced externally.
Note: ERR-0100, ERR-0101 are PLATFORM-STD (DRV-ORG-002) — no RULE-ID.
```

**DOC Gate: PASSED ✓**
```
[ ✓ ] All 47 API-IDs appear in API Contract Summary
[ ✓ ] Error Catalog complete with Arabic + English messages
[ ✓ ] All APIs marked STABLE (one noted OQ-001 extension)
[ ✓ ] Pagination standard declared
[ ✓ ] List vs Page endpoints differentiated (DRV-ORG-003)
```

