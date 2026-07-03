package com.example.erp.org.controller;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.org.dto.LocationSiteCreateRequest;
import com.example.erp.org.dto.LocationSiteResponse;
import com.example.erp.org.dto.LocationSiteSearchRequest;
import com.example.erp.org.dto.LocationSiteUpdateRequest;
import com.example.erp.org.service.LocationSiteService;
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
@RequestMapping("/api/v1/org/location-sites")
@RequiredArgsConstructor
@Tag(name = "Location Site Management", description = "إدارة مواقع العمل - Location Site Management API")
public class LocationSiteController {

    private final LocationSiteService locationSiteService;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(operationId = "API-ORG-039", summary = "Create Location Site", description = "إنشاء موقع عمل جديد")
    public ResponseEntity<ApiResponse<LocationSiteResponse>> create(
            @Valid @RequestBody LocationSiteCreateRequest request) {
        ServiceResult<LocationSiteResponse> result = locationSiteService.create(request);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "API-ORG-041", summary = "Update Location Site", description = "تحديث موقع عمل")
    public ResponseEntity<ApiResponse<LocationSiteResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody LocationSiteUpdateRequest request) {
        ServiceResult<LocationSiteResponse> result = locationSiteService.update(id, request);
        return operationCode.craftResponse(result);
    }

    @GetMapping("/{id}")
    @Operation(operationId = "API-ORG-044", summary = "Get Location Site by ID", description = "جلب موقع عمل بالمعرف")
    public ResponseEntity<ApiResponse<LocationSiteResponse>> getById(@PathVariable Long id) {
        ServiceResult<LocationSiteResponse> result = locationSiteService.getById(id);
        return operationCode.craftResponse(result);
    }

    @PostMapping("/search")
    @Operation(operationId = "API-ORG-040", summary = "Search Location Sites", description = "بحث في مواقع العمل")
    public ResponseEntity<ApiResponse<Page<LocationSiteResponse>>> search(
            @Valid @RequestBody LocationSiteSearchRequest searchRequest) {
        ServiceResult<Page<LocationSiteResponse>> result = locationSiteService.search(searchRequest);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/activate")
    @Operation(operationId = "API-ORG-043", summary = "Activate Location Site", description = "تفعيل موقع عمل")
    public ResponseEntity<ApiResponse<LocationSiteResponse>> activate(@PathVariable Long id) {
        ServiceResult<LocationSiteResponse> result = locationSiteService.activate(id);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/deactivate")
    @Operation(operationId = "API-ORG-042", summary = "Deactivate Location Site", description = "إلغاء تفعيل موقع عمل")
    public ResponseEntity<ApiResponse<LocationSiteResponse>> deactivate(@PathVariable Long id) {
        ServiceResult<LocationSiteResponse> result = locationSiteService.deactivate(id);
        return operationCode.craftResponse(result);
    }
}
