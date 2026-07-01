package com.example.org.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.org.dto.*;
import com.example.org.service.OrgDepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org/departments")
@RequiredArgsConstructor
@Tag(name = "Departments", description = "ORG - Department Management")
public class OrgDepartmentController {

    private final OrgDepartmentService service;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> create(@Valid @RequestBody DepartmentCreateRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest request) {
        return operationCode.craftResponse(service.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getById(@PathVariable Long id) {
        return operationCode.craftResponse(service.getById(id));
    }

    @PostMapping("/search")
    @Operation(summary = "Search departments")
    public ResponseEntity<ApiResponse<Page<DepartmentResponse>>> search(@RequestBody DepartmentSearchRequest request) {
        return operationCode.craftResponse(service.search(request));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> activate(@PathVariable Long id) {
        return operationCode.craftResponse(service.activate(id));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate department")
    public ResponseEntity<ApiResponse<DepartmentResponse>> deactivate(@PathVariable Long id) {
        return operationCode.craftResponse(service.deactivate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete department")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/usage")
    @Operation(summary = "Get department usage")
    public ResponseEntity<ApiResponse<DepartmentUsageResponse>> getUsage(@PathVariable Long id) {
        return operationCode.craftResponse(service.getUsage(id));
    }

    @GetMapping("/options")
    @Operation(summary = "List active department options")
    public ResponseEntity<ApiResponse<List<DepartmentOptionResponse>>> listOptions(
            @RequestParam(required = false) Long branchId) {
        return operationCode.craftResponse(service.listOptions(branchId));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get department tree", description = "شجرة الأقسام الهرمية - Get full recursive department tree for a branch")
    public ResponseEntity<ApiResponse<List<DepartmentTreeNodeResponse>>> getTree(
            @RequestParam Long branchId,
            @RequestParam(required = false) Boolean isActiveFl) {
        return operationCode.craftResponse(service.getTree(branchId, isActiveFl));
    }
}
