package com.example.erp.org.controller;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.org.dto.BranchCreateRequest;
import com.example.erp.org.dto.BranchResponse;
import com.example.erp.org.dto.BranchSearchRequest;
import com.example.erp.org.dto.BranchUpdateRequest;
import com.example.erp.org.service.BranchService;
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
@RequestMapping("/api/v1/org/branches")
@RequiredArgsConstructor
@Tag(name = "Branch Management", description = "إدارة الفروع - Branch Management API")
public class BranchController {

    private final BranchService branchService;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create Branch", description = "إنشاء فرع جديد")
    public ResponseEntity<ApiResponse<BranchResponse>> create(
            @Valid @RequestBody BranchCreateRequest request) {
        ServiceResult<BranchResponse> result = branchService.create(request);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Branch", description = "تحديث فرع")
    public ResponseEntity<ApiResponse<BranchResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BranchUpdateRequest request) {
        ServiceResult<BranchResponse> result = branchService.update(id, request);
        return operationCode.craftResponse(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Branch by ID", description = "جلب فرع بالمعرف")
    public ResponseEntity<ApiResponse<BranchResponse>> getById(@PathVariable Long id) {
        ServiceResult<BranchResponse> result = branchService.getById(id);
        return operationCode.craftResponse(result);
    }

    @PostMapping("/search")
    @Operation(summary = "Search Branches", description = "بحث في الفروع")
    public ResponseEntity<ApiResponse<Page<BranchResponse>>> search(
            @Valid @RequestBody BranchSearchRequest searchRequest) {
        ServiceResult<Page<BranchResponse>> result = branchService.search(searchRequest);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate Branch", description = "تفعيل فرع")
    public ResponseEntity<ApiResponse<BranchResponse>> activate(@PathVariable Long id) {
        ServiceResult<BranchResponse> result = branchService.activate(id);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate Branch", description = "إلغاء تفعيل فرع")
    public ResponseEntity<ApiResponse<BranchResponse>> deactivate(@PathVariable Long id) {
        ServiceResult<BranchResponse> result = branchService.deactivate(id);
        return operationCode.craftResponse(result);
    }
}
