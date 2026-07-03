package com.example.erp.org.controller;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.org.dto.ProfitCenterCreateRequest;
import com.example.erp.org.dto.ProfitCenterResponse;
import com.example.erp.org.dto.ProfitCenterSearchRequest;
import com.example.erp.org.dto.ProfitCenterUpdateRequest;
import com.example.erp.org.service.ProfitCenterService;
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
@RequestMapping("/api/v1/org/profit-centers")
@RequiredArgsConstructor
@Tag(name = "Profit Center Management", description = "إدارة مراكز الربح - Profit Center Management API")
public class ProfitCenterController {

    private final ProfitCenterService profitCenterService;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(operationId = "API-ORG-033", summary = "Create Profit Center", description = "إنشاء مركز ربح جديد")
    public ResponseEntity<ApiResponse<ProfitCenterResponse>> create(
            @Valid @RequestBody ProfitCenterCreateRequest request) {
        ServiceResult<ProfitCenterResponse> result = profitCenterService.create(request);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "API-ORG-035", summary = "Update Profit Center", description = "تحديث مركز ربح")
    public ResponseEntity<ApiResponse<ProfitCenterResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProfitCenterUpdateRequest request) {
        ServiceResult<ProfitCenterResponse> result = profitCenterService.update(id, request);
        return operationCode.craftResponse(result);
    }

    @GetMapping("/{id}")
    @Operation(operationId = "API-ORG-038", summary = "Get Profit Center by ID", description = "جلب مركز ربح بالمعرف")
    public ResponseEntity<ApiResponse<ProfitCenterResponse>> getById(@PathVariable Long id) {
        ServiceResult<ProfitCenterResponse> result = profitCenterService.getById(id);
        return operationCode.craftResponse(result);
    }

    @PostMapping("/search")
    @Operation(operationId = "API-ORG-034", summary = "Search Profit Centers", description = "بحث في مراكز الربح")
    public ResponseEntity<ApiResponse<Page<ProfitCenterResponse>>> search(
            @Valid @RequestBody ProfitCenterSearchRequest searchRequest) {
        ServiceResult<Page<ProfitCenterResponse>> result = profitCenterService.search(searchRequest);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/activate")
    @Operation(operationId = "API-ORG-037", summary = "Activate Profit Center", description = "تفعيل مركز ربح")
    public ResponseEntity<ApiResponse<ProfitCenterResponse>> activate(@PathVariable Long id) {
        ServiceResult<ProfitCenterResponse> result = profitCenterService.activate(id);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/deactivate")
    @Operation(operationId = "API-ORG-036", summary = "Deactivate Profit Center", description = "إلغاء تفعيل مركز ربح")
    public ResponseEntity<ApiResponse<ProfitCenterResponse>> deactivate(@PathVariable Long id) {
        ServiceResult<ProfitCenterResponse> result = profitCenterService.deactivate(id);
        return operationCode.craftResponse(result);
    }
}
