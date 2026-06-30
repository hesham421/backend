package com.example.org.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.org.dto.*;
import com.example.org.service.OrgLegalEntityService;
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
@RequestMapping("/api/org/legal-entities")
@RequiredArgsConstructor
@Tag(name = "Legal Entities", description = "ORG - Legal Entity Management")
public class OrgLegalEntityController {

    private final OrgLegalEntityService service;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create legal entity")
    public ResponseEntity<ApiResponse<LegalEntityResponse>> create(@Valid @RequestBody LegalEntityCreateRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update legal entity")
    public ResponseEntity<ApiResponse<LegalEntityResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody LegalEntityUpdateRequest request) {
        return operationCode.craftResponse(service.update(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get legal entity by ID")
    public ResponseEntity<ApiResponse<LegalEntityResponse>> getById(@PathVariable Long id) {
        return operationCode.craftResponse(service.getById(id));
    }

    @PostMapping("/search")
    @Operation(summary = "Search legal entities")
    public ResponseEntity<ApiResponse<Page<LegalEntityResponse>>> search(@RequestBody LegalEntitySearchRequest request) {
        return operationCode.craftResponse(service.search(request));
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Activate legal entity")
    public ResponseEntity<ApiResponse<LegalEntityResponse>> activate(@PathVariable Long id) {
        return operationCode.craftResponse(service.activate(id));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate legal entity")
    public ResponseEntity<ApiResponse<LegalEntityResponse>> deactivate(@PathVariable Long id) {
        return operationCode.craftResponse(service.deactivate(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete legal entity")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/usage")
    @Operation(summary = "Get legal entity usage")
    public ResponseEntity<ApiResponse<LegalEntityUsageResponse>> getUsage(@PathVariable Long id) {
        return operationCode.craftResponse(service.getUsage(id));
    }

    @GetMapping("/options")
    @Operation(summary = "List active legal entity options")
    public ResponseEntity<ApiResponse<List<LegalEntityOptionResponse>>> listOptions() {
        return operationCode.craftResponse(service.listOptions());
    }
}
