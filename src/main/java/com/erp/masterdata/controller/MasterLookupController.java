package com.erp.masterdata.controller;

import com.erp.common.search.SearchRequest;
import com.erp.common.domain.status.ServiceResult;
import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.masterdata.dto.*;
import com.erp.masterdata.service.LookupDetailService;
import com.erp.masterdata.service.MasterLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Unified controller for Master Lookup AND Lookup Detail management — both are one functional
 * screen in the frontend. All operations use PERM_MASTER_LOOKUP_* permissions.
 */
@RestController
@RequestMapping("/api/masterdata/master-lookups")
@RequiredArgsConstructor
@Tag(name = "Master Lookup Management", description = "إدارة القوائم المرجعية وقيمها - Master Data Lookup Types & Values")
public class MasterLookupController {

    private final MasterLookupService masterLookupService;
    private final LookupDetailService lookupDetailService;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create Master Lookup", description = "إنشاء نوع قائمة مرجعية جديد")
    public ResponseEntity<ApiResponse<MasterLookupResponse>> create(@Valid @RequestBody MasterLookupCreateRequest request) {
        ServiceResult<MasterLookupResponse> result = masterLookupService.create(request);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Master Lookup", description = "تحديث نوع قائمة مرجعية موجود")
    public ResponseEntity<ApiResponse<MasterLookupResponse>> update(
        @PathVariable Long id,
        @Valid @RequestBody MasterLookupUpdateRequest request
    ) {
        ServiceResult<MasterLookupResponse> result = masterLookupService.update(id, request);
        return operationCode.craftResponse(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Master Lookup by ID", description = "جلب تفاصيل نوع القائمة المرجعية")
    public ResponseEntity<ApiResponse<MasterLookupResponse>> getById(@PathVariable Long id) {
        ServiceResult<MasterLookupResponse> result = masterLookupService.getById(id);
        return operationCode.craftResponse(result);
    }

    /**
     * Allowed filters: lookupKey/lookupName/lookupNameEn (EQUALS/CONTAINS/STARTS_WITH), isActive
     * (EQUALS); sortable: id, lookupKey, lookupName, lookupNameEn, isActive, createdAt, updatedAt.
     */
    @PostMapping("/search")
    @Operation(
        summary = "Search Master Lookups", 
        description = "البحث في أنواع القوائم المرجعية مع الفلترة والترتيب والصفحات"
    )
    public ResponseEntity<ApiResponse<Page<MasterLookupResponse>>> search(@RequestBody MasterLookupSearchRequest searchRequest) {
        ServiceResult<Page<MasterLookupResponse>> result = masterLookupService.search(searchRequest.toCommonSearchRequest());
        return operationCode.craftResponse(result);
    }

    /**
     * Business Rule: cannot deactivate if there are active lookup details.
     */
    @PutMapping("/{id}/toggle-active")
    @Operation(summary = "Toggle Master Lookup Active Status", description = "تبديل حالة نشاط نوع القائمة المرجعية")
    public ResponseEntity<ApiResponse<MasterLookupResponse>> toggleActive(
        @PathVariable Long id,
        @Valid @RequestBody ToggleActiveRequest request
    ) {
        ServiceResult<MasterLookupResponse> result = masterLookupService.toggleActive(id, request.getActive());
        return operationCode.craftResponse(result);
    }

    /**
     * Cannot delete if it has lookup details (FK constraint) — returns HTTP 409 CONFLICT.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Master Lookup", description = "حذف نوع القائمة المرجعية")
    public void delete(@PathVariable Long id) {
        masterLookupService.delete(id);
    }

    @GetMapping("/{id}/usage")
    @Operation(summary = "Get Master Lookup Usage", description = "جلب معلومات استخدام نوع القائمة المرجعية")
    public ResponseEntity<ApiResponse<MasterLookupUsageResponse>> getUsage(@PathVariable Long id) {
        ServiceResult<MasterLookupUsageResponse> result = masterLookupService.getUsage(id);
        return operationCode.craftResponse(result);
    }

    @PostMapping("/details")
    @Operation(summary = "Create Lookup Detail", description = "إنشاء قيمة مرجعية جديدة")
    public ResponseEntity<ApiResponse<LookupDetailResponse>> createDetail(@Valid @RequestBody LookupDetailCreateRequest request) {
        ServiceResult<LookupDetailResponse> result = lookupDetailService.create (request);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/details/{id}")
    @Operation(summary = "Update Lookup Detail", description = "تحديث قيمة مرجعية موجودة")
    public ResponseEntity<ApiResponse<LookupDetailResponse>> updateDetail(
        @PathVariable Long id,
        @Valid @RequestBody LookupDetailUpdateRequest request
    ) {
        ServiceResult<LookupDetailResponse> result = lookupDetailService.update(id, request);
        return operationCode.craftResponse(result);
    }

    @GetMapping("/details/{id}")
    @Operation(summary = "Get Lookup Detail by ID", description = "جلب تفاصيل القيمة المرجعية")
    public ResponseEntity<ApiResponse<LookupDetailResponse>> getDetailById(@PathVariable Long id) {
        ServiceResult<LookupDetailResponse> result = lookupDetailService.getById(id);
        return operationCode.craftResponse(result);
    }

    /**
     * Allowed filters: masterLookupId (EQUALS, required), code/nameAr/nameEn
     * (EQUALS/CONTAINS/STARTS_WITH), isActive (EQUALS); default sort: sortOrder ASC.
     */
    @PostMapping("/details/search")
    @Operation(
        summary = "Search Lookup Details", 
        description = "البحث في القيم المرجعية مع الفلترة والترتيب والصفحات"
    )
    public ResponseEntity<ApiResponse<Page<LookupDetailResponse>>> searchDetails(@RequestBody LookupDetailSearchRequest searchRequest) {
        ServiceResult<Page<LookupDetailResponse>> result = lookupDetailService.search(
            searchRequest.getMasterLookupId(),
            searchRequest.toCommonSearchRequest()
        );
        return operationCode.craftResponse(result);
    }

    @GetMapping("/details/options/{lookupKey}")
    @Operation(
        summary = "Get Lookup Options by Key", 
        description = "جلب خيارات القائمة المنسدلة حسب مفتاح القائمة المرجعية"
    )
    public ResponseEntity<ApiResponse<List<LookupDetailOptionResponse>>> getDetailOptions(
        @PathVariable("lookupKey")
        @Parameter(description = "Master lookup key (e.g., COLOR, UOM, COUNTRY)", example = "COLOR")
        String lookupKey,
        
        @RequestParam(required = false, defaultValue = "true")
        @Parameter(description = "Filter by active status", example = "true")
        Boolean active
    ) {
        ServiceResult<List<LookupDetailOptionResponse>> result = lookupDetailService.getOptionsByLookupKey(lookupKey, active);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/details/{id}/toggle-active")
    @Operation(summary = "Toggle Lookup Detail Active Status", description = "تبديل حالة نشاط القيمة المرجعية")
    public ResponseEntity<ApiResponse<LookupDetailResponse>> toggleDetailActive(
        @PathVariable Long id,
        @Valid @RequestBody ToggleActiveRequest request
    ) {
        ServiceResult<LookupDetailResponse> result = lookupDetailService.toggleActive(id, request.getActive());
        return operationCode.craftResponse(result);
    }

    /**
     * Cannot delete if referenced by any entity (FK constraint) — returns HTTP 409 CONFLICT.
     */
    @DeleteMapping("/details/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete Lookup Detail", description = "حذف القيمة المرجعية")
    public void deleteDetail(@PathVariable Long id) {
        lookupDetailService.delete(id);
    }

    @GetMapping("/details/{id}/usage")
    @Operation(summary = "Get Lookup Detail Usage", description = "جلب معلومات استخدام القيمة المرجعية")
    public ResponseEntity<ApiResponse<LookupDetailUsageResponse>> getDetailUsage(@PathVariable Long id) {
        ServiceResult<LookupDetailUsageResponse> result = lookupDetailService.getUsage(id);
        return operationCode.craftResponse(result);
    }
}
