package com.example.org.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.org.dto.*;
import com.example.org.service.OrgProfitCenterService;
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
@RequestMapping("/api/org/profit-centers")
@RequiredArgsConstructor
@Tag(name = "Profit Centers", description = "ORG - Profit Center Management")
public class OrgProfitCenterController {

    private final OrgProfitCenterService service;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create profit center")
    public ResponseEntity<ApiResponse<ProfitCenterResponse>> create(@Valid @RequestBody ProfitCenterCreateRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update profit center")
    public ResponseEntity<ApiResponse<ProfitCenterResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProfitCenterUpdateRequest request) {
        return operationCode.craftResponse(service.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get profit center by ID")
    public ResponseEntity<ApiResponse<ProfitCenterResponse>> getById(@PathVariable Long id) {
        return operationCode.craftResponse(service.getById(id));
    }

    @PostMapping("/search")
    @Operation(summary = "Search profit centers")
    public ResponseEntity<ApiResponse<Page<ProfitCenterResponse>>> search(@RequestBody ProfitCenterSearchRequest request) {
        return operationCode.craftResponse(service.search(request));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate profit center")
    public ResponseEntity<ApiResponse<ProfitCenterResponse>> activate(@PathVariable Long id) {
        return operationCode.craftResponse(service.activate(id));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate profit center")
    public ResponseEntity<ApiResponse<ProfitCenterResponse>> deactivate(@PathVariable Long id) {
        return operationCode.craftResponse(service.deactivate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete profit center")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/usage")
    @Operation(summary = "Get profit center usage")
    public ResponseEntity<ApiResponse<ProfitCenterUsageResponse>> getUsage(@PathVariable Long id) {
        return operationCode.craftResponse(service.getUsage(id));
    }

    @GetMapping("/options")
    @Operation(summary = "List active profit center options")
    public ResponseEntity<ApiResponse<List<ProfitCenterOptionResponse>>> listOptions(
            @RequestParam(required = false) Long legalEntityId) {
        return operationCode.craftResponse(service.listOptions(legalEntityId));
    }
}
