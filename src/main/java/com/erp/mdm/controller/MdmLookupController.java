package com.erp.mdm.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.mdm.dto.LookupTypeCreateRequest;
import com.erp.mdm.dto.LookupTypeResponse;
import com.erp.mdm.dto.LookupTypeSearchRequest;
import com.erp.mdm.dto.LookupTypeUpdateRequest;
import com.erp.mdm.dto.LookupValueCreateRequest;
import com.erp.mdm.dto.LookupValueResponse;
import com.erp.mdm.dto.LookupValueSearchRequest;
import com.erp.mdm.dto.LookupValueUpdateRequest;
import com.erp.mdm.service.MdmLookupTypeService;
import com.erp.mdm.service.MdmLookupValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin controller for SCR-MDM-001 (Reference Data Lookup Management, DRV-007). Base path
 * {@code /api/v1/mdm}; each method declares its full sub-path so the SVC-API-LOOKUP-VALUE sub can
 * add the value endpoints ({@code /lookup-types/{typeId}/values}, {@code /lookup-values/...}) to
 * this same controller. This sub adds API-MDM-001..005 (LookupType master CRUD). DELETE maps onto
 * the service's soft deactivate (204).
 */
@RestController
@RequestMapping("/api/v1/mdm")
@RequiredArgsConstructor
@Tag(name = "MDM Lookup Management", description = "Reference data lookup management - إدارة القوائم المرجعية")
public class MdmLookupController {

    private final MdmLookupTypeService lookupTypeService;
    private final MdmLookupValueService lookupValueService;
    private final OperationCode operationCode;

    @PostMapping("/lookup-types")
    @Operation(summary = "Create lookup type", description = "إنشاء نوع قائمة مرجعية")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> createLookupType(
            @Valid @RequestBody LookupTypeCreateRequest request) {
        return operationCode.craftResponse(lookupTypeService.create(request));
    }

    @PostMapping("/lookup-types/search")
    @Operation(summary = "Search lookup types", description = "البحث في أنواع القوائم المرجعية")
    public ResponseEntity<ApiResponse<Page<LookupTypeResponse>>> searchLookupTypes(
            @Valid @RequestBody LookupTypeSearchRequest searchRequest) {
        return operationCode.craftResponse(lookupTypeService.search(searchRequest));
    }

    @PutMapping("/lookup-types/{id}")
    @Operation(summary = "Update lookup type", description = "تحديث نوع قائمة مرجعية حسب المعرف")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> updateLookupType(
            @PathVariable Long id,
            @Valid @RequestBody LookupTypeUpdateRequest request) {
        return operationCode.craftResponse(lookupTypeService.update(id, request));
    }

    @GetMapping("/lookup-types/{id}")
    @Operation(summary = "Get lookup type by ID", description = "جلب نوع قائمة مرجعية حسب المعرف")
    public ResponseEntity<ApiResponse<LookupTypeResponse>> getLookupType(@PathVariable Long id) {
        return operationCode.craftResponse(lookupTypeService.getById(id));
    }

    /**
     * API-MDM-004 DELETE — soft deactivate (isActiveFl = false). 204, void, no wrapped response;
     * the service enforces RULE-MDM-006 before flipping the flag.
     */
    @DeleteMapping("/lookup-types/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate lookup type", description = "إلغاء تفعيل نوع قائمة مرجعية حسب المعرف")
    public void deactivateLookupType(@PathVariable Long id) {
        lookupTypeService.deactivate(id);
    }

    // --- LookupValue (detail) endpoints — API-MDM-006..010 --------------------------------------

    @PostMapping("/lookup-types/{typeId}/values")
    @Operation(summary = "Create lookup value", description = "إنشاء قيمة قائمة مرجعية ضمن نوع")
    public ResponseEntity<ApiResponse<LookupValueResponse>> createLookupValue(
            @PathVariable Long typeId,
            @Valid @RequestBody LookupValueCreateRequest request) {
        return operationCode.craftResponse(lookupValueService.create(typeId, request));
    }

    /**
     * API-MDM-007 — parent-scoped list. Implemented as POST /search (not the plan's GET-with-query):
     * the platform's shared search contract is POST-body-only and build-create-controller A.6.6
     * forbids GET-search — same resolution as API-MDM-002 (recorded in execution-state api_doc_gaps).
     */
    @PostMapping("/lookup-types/{typeId}/values/search")
    @Operation(summary = "Search lookup values", description = "البحث في قيم نوع قائمة مرجعية")
    public ResponseEntity<ApiResponse<Page<LookupValueResponse>>> listLookupValues(
            @PathVariable Long typeId,
            @Valid @RequestBody LookupValueSearchRequest searchRequest) {
        return operationCode.craftResponse(lookupValueService.search(typeId, searchRequest));
    }

    @PutMapping("/lookup-values/{id}")
    @Operation(summary = "Update lookup value", description = "تحديث قيمة قائمة مرجعية حسب المعرف")
    public ResponseEntity<ApiResponse<LookupValueResponse>> updateLookupValue(
            @PathVariable Long id,
            @Valid @RequestBody LookupValueUpdateRequest request) {
        return operationCode.craftResponse(lookupValueService.update(id, request));
    }

    @GetMapping("/lookup-values/{id}")
    @Operation(summary = "Get lookup value by ID", description = "جلب قيمة قائمة مرجعية حسب المعرف")
    public ResponseEntity<ApiResponse<LookupValueResponse>> getLookupValue(@PathVariable Long id) {
        return operationCode.craftResponse(lookupValueService.getById(id));
    }

    /**
     * API-MDM-009 DELETE — soft deactivate (isActiveFl = false). 204, void, no wrapped response;
     * LookupValue is a leaf (DRV-008) so the service performs no child/usage check.
     */
    @DeleteMapping("/lookup-values/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate lookup value", description = "إلغاء تفعيل قيمة قائمة مرجعية حسب المعرف")
    public void deactivateLookupValue(@PathVariable Long id) {
        lookupValueService.deactivate(id);
    }
}
