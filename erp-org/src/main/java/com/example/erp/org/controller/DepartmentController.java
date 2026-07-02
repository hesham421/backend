package com.example.erp.org.controller;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.org.dto.DepartmentCreateRequest;
import com.example.erp.org.dto.DepartmentResponse;
import com.example.erp.org.dto.DepartmentSearchRequest;
import com.example.erp.org.dto.DepartmentTreeNodeResponse;
import com.example.erp.org.dto.DepartmentUpdateRequest;
import com.example.erp.org.service.DepartmentService;
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
 * Flat CRUD endpoints for Department (API-ORG-019,021..025) plus the recursive tree read
 * (API-ORG-020, GET /tree), unified under the same controller per A.6.9.
 */
@RestController
@RequestMapping("/api/v1/org/departments")
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "إدارة الأقسام - Department Management API")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create Department", description = "إنشاء قسم جديد")
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(
            @Valid @RequestBody DepartmentCreateRequest request) {
        ServiceResult<DepartmentResponse> result = departmentService.create(request);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Department", description = "تحديث قسم")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        ServiceResult<DepartmentResponse> result = departmentService.update(id, request);
        return operationCode.craftResponse(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Department by ID", description = "جلب قسم بالمعرف")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(@PathVariable Long id) {
        ServiceResult<DepartmentResponse> result = departmentService.getById(id);
        return operationCode.craftResponse(result);
    }

    @PostMapping("/search")
    @Operation(summary = "Search Departments", description = "بحث في الأقسام")
    public ResponseEntity<ApiResponse<Page<DepartmentResponse>>> search(
            @Valid @RequestBody DepartmentSearchRequest searchRequest) {
        ServiceResult<Page<DepartmentResponse>> result = departmentService.search(searchRequest);
        return operationCode.craftResponse(result);
    }

    @GetMapping("/tree")
    @Operation(summary = "Get Department tree", description = "جلب الهيكل الشجري للأقسام")
    public ResponseEntity<ApiResponse<List<DepartmentTreeNodeResponse>>> getTree(
            @RequestParam Long branchFk,
            @RequestParam(required = false) Boolean isActiveFl) {
        ServiceResult<List<DepartmentTreeNodeResponse>> result = departmentService.getTree(branchFk, isActiveFl);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate Department", description = "تفعيل قسم")
    public ResponseEntity<ApiResponse<DepartmentResponse>> activate(@PathVariable Long id) {
        ServiceResult<DepartmentResponse> result = departmentService.activate(id);
        return operationCode.craftResponse(result);
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate Department", description = "إلغاء تفعيل قسم")
    public ResponseEntity<ApiResponse<DepartmentResponse>> deactivate(@PathVariable Long id) {
        ServiceResult<DepartmentResponse> result = departmentService.deactivate(id);
        return operationCode.craftResponse(result);
    }
}
