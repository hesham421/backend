package com.example.erp.org.controller;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.org.dto.RegionCreateRequest;
import com.example.erp.org.dto.RegionResponse;
import com.example.erp.org.dto.RegionSearchRequest;
import com.example.erp.org.dto.RegionUpdateRequest;
import com.example.erp.org.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/org/regions")
@RequiredArgsConstructor
@Tag(name = "Region Management", description = "إدارة المناطق - Region Management API")
public class RegionController {

    private final RegionService regionService;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create Region", description = "إنشاء منطقة جديدة")
    public ResponseEntity<ApiResponse<RegionResponse>> create(
            @Valid @RequestBody RegionCreateRequest request) {
        ServiceResult<RegionResponse> result = regionService.create(request);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Region", description = "تحديث منطقة")
    public ResponseEntity<ApiResponse<RegionResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RegionUpdateRequest request) {
        ServiceResult<RegionResponse> result = regionService.update(id, request);
        return operationCode.craftResponse(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Region by ID", description = "جلب منطقة بالمعرف")
    public ResponseEntity<ApiResponse<RegionResponse>> getById(@PathVariable Long id) {
        ServiceResult<RegionResponse> result = regionService.getById(id);
        return operationCode.craftResponse(result);
    }

    @PostMapping("/search")
    @Operation(summary = "Search Regions", description = "بحث في المناطق")
    public ResponseEntity<ApiResponse<Page<RegionResponse>>> search(
            @Valid @RequestBody RegionSearchRequest searchRequest) {
        ServiceResult<Page<RegionResponse>> result = regionService.search(searchRequest);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate Region", description = "تفعيل منطقة")
    public ResponseEntity<ApiResponse<RegionResponse>> activate(@PathVariable Long id) {
        ServiceResult<RegionResponse> result = regionService.activate(id);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate Region", description = "إلغاء تفعيل منطقة")
    public ResponseEntity<ApiResponse<RegionResponse>> deactivate(@PathVariable Long id) {
        ServiceResult<RegionResponse> result = regionService.deactivate(id);
        return operationCode.craftResponse(result);
    }
}
