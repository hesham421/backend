package com.example.org.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.org.dto.*;
import com.example.org.service.OrgBranchService;
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
@RequestMapping("/api/org/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "ORG - Branch Management")
public class OrgBranchController {

    private final OrgBranchService service;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create branch")
    public ResponseEntity<ApiResponse<BranchResponse>> create(@Valid @RequestBody BranchCreateRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update branch")
    public ResponseEntity<ApiResponse<BranchResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody BranchUpdateRequest request) {
        return operationCode.craftResponse(service.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get branch by ID")
    public ResponseEntity<ApiResponse<BranchResponse>> getById(@PathVariable Long id) {
        return operationCode.craftResponse(service.getById(id));
    }

    @PostMapping("/search")
    @Operation(summary = "Search branches")
    public ResponseEntity<ApiResponse<Page<BranchResponse>>> search(@RequestBody BranchSearchRequest request) {
        return operationCode.craftResponse(service.search(request));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate branch")
    public ResponseEntity<ApiResponse<BranchResponse>> activate(@PathVariable Long id) {
        return operationCode.craftResponse(service.activate(id));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate branch")
    public ResponseEntity<ApiResponse<BranchResponse>> deactivate(@PathVariable Long id) {
        return operationCode.craftResponse(service.deactivate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete branch")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/usage")
    @Operation(summary = "Get branch usage")
    public ResponseEntity<ApiResponse<BranchUsageResponse>> getUsage(@PathVariable Long id) {
        return operationCode.craftResponse(service.getUsage(id));
    }

    @GetMapping("/options")
    @Operation(summary = "List active branch options")
    public ResponseEntity<ApiResponse<List<BranchOptionResponse>>> listOptions(
            @RequestParam(required = false) Long legalEntityId) {
        return operationCode.craftResponse(service.listOptions(legalEntityId));
    }
}
