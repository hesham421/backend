package com.example.erp.org.controller;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.org.dto.CostCenterCreateRequest;
import com.example.erp.org.dto.CostCenterResponse;
import com.example.erp.org.dto.CostCenterSearchRequest;
import com.example.erp.org.dto.CostCenterTreeNodeResponse;
import com.example.erp.org.dto.CostCenterUpdateRequest;
import com.example.erp.org.service.CostCenterService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Flat CRUD endpoints for CostCenter (API-ORG-026,028..032) plus the recursive tree read
 * (API-ORG-027, GET /tree), unified under the same controller per A.6.9.
 */
@RestController
@RequestMapping("/api/v1/org/cost-centers")
@RequiredArgsConstructor
@Tag(name = "Cost Center Management", description = "إدارة مراكز التكلفة - Cost Center Management API")
public class CostCenterController {

    private final CostCenterService costCenterService;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create Cost Center", description = "إنشاء مركز تكلفة جديد")
    public ResponseEntity<ApiResponse<CostCenterResponse>> create(
            @Valid @RequestBody CostCenterCreateRequest request) {
        ServiceResult<CostCenterResponse> result = costCenterService.create(request);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Cost Center", description = "تحديث مركز تكلفة")
    public ResponseEntity<ApiResponse<CostCenterResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CostCenterUpdateRequest request) {
        ServiceResult<CostCenterResponse> result = costCenterService.update(id, request);
        return operationCode.craftResponse(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Cost Center by ID", description = "جلب مركز تكلفة بالمعرف")
    public ResponseEntity<ApiResponse<CostCenterResponse>> getById(@PathVariable Long id) {
        ServiceResult<CostCenterResponse> result = costCenterService.getById(id);
        return operationCode.craftResponse(result);
    }

    @PostMapping("/search")
    @Operation(summary = "Search Cost Centers", description = "بحث في مراكز التكلفة")
    public ResponseEntity<ApiResponse<Page<CostCenterResponse>>> search(
            @Valid @RequestBody CostCenterSearchRequest searchRequest) {
        ServiceResult<Page<CostCenterResponse>> result = costCenterService.search(searchRequest);
        return operationCode.craftResponse(result);
    }

    @GetMapping("/tree")
    @Operation(summary = "Get Cost Center tree", description = "جلب الهيكل الشجري لمراكز التكلفة")
    public ResponseEntity<ApiResponse<List<CostCenterTreeNodeResponse>>> getTree(
            @RequestParam Long branchFk,
            @RequestParam(required = false) Boolean isActiveFl) {
        ServiceResult<List<CostCenterTreeNodeResponse>> result = costCenterService.getTree(branchFk, isActiveFl);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate Cost Center", description = "تفعيل مركز تكلفة")
    public ResponseEntity<ApiResponse<CostCenterResponse>> activate(@PathVariable Long id) {
        ServiceResult<CostCenterResponse> result = costCenterService.activate(id);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate Cost Center", description = "إلغاء تفعيل مركز تكلفة")
    public ResponseEntity<ApiResponse<CostCenterResponse>> deactivate(@PathVariable Long id) {
        ServiceResult<CostCenterResponse> result = costCenterService.deactivate(id);
        return operationCode.craftResponse(result);
    }
}
