package com.example.org.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.org.dto.*;
import com.example.org.service.OrgCostCenterService;
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
@RequestMapping("/api/org/cost-centers")
@RequiredArgsConstructor
@Tag(name = "Cost Centers", description = "ORG - Cost Center Management")
public class OrgCostCenterController {

    private final OrgCostCenterService service;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create cost center")
    public ResponseEntity<ApiResponse<CostCenterResponse>> create(@Valid @RequestBody CostCenterCreateRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update cost center")
    public ResponseEntity<ApiResponse<CostCenterResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CostCenterUpdateRequest request) {
        return operationCode.craftResponse(service.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cost center by ID")
    public ResponseEntity<ApiResponse<CostCenterResponse>> getById(@PathVariable Long id) {
        return operationCode.craftResponse(service.getById(id));
    }

    @PostMapping("/search")
    @Operation(summary = "Search cost centers")
    public ResponseEntity<ApiResponse<Page<CostCenterResponse>>> search(@RequestBody CostCenterSearchRequest request) {
        return operationCode.craftResponse(service.search(request));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate cost center")
    public ResponseEntity<ApiResponse<CostCenterResponse>> activate(@PathVariable Long id) {
        return operationCode.craftResponse(service.activate(id));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate cost center")
    public ResponseEntity<ApiResponse<CostCenterResponse>> deactivate(@PathVariable Long id) {
        return operationCode.craftResponse(service.deactivate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete cost center")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/usage")
    @Operation(summary = "Get cost center usage")
    public ResponseEntity<ApiResponse<CostCenterUsageResponse>> getUsage(@PathVariable Long id) {
        return operationCode.craftResponse(service.getUsage(id));
    }

    @GetMapping("/options")
    @Operation(summary = "List active cost center options")
    public ResponseEntity<ApiResponse<List<CostCenterOptionResponse>>> listOptions(
            @RequestParam(required = false) Long branchId) {
        return operationCode.craftResponse(service.listOptions(branchId));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get cost center tree", description = "شجرة مراكز التكلفة الهرمية - Get full recursive cost center tree for a branch")
    public ResponseEntity<ApiResponse<List<CostCenterTreeNodeResponse>>> getTree(
            @RequestParam Long branchId,
            @RequestParam(required = false) Boolean isActiveFl) {
        return operationCode.craftResponse(service.getTree(branchId, isActiveFl));
    }
}
