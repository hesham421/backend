package com.erp.fin.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.fin.dto.AllocationRuleCreateRequest;
import com.erp.fin.dto.AllocationRuleResponse;
import com.erp.fin.dto.AllocationRuleSearchRequest;
import com.erp.fin.dto.JournalEntryResponse;
import com.erp.fin.service.AllocationRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin controller for API-FIN-016 (SVC-API-CRUD.md). The rule's targets travel inside the create
 * body, so they need no endpoint of their own. SVC-API-INT adds API-FIN-017 (run a rule);
 * searching rules (API-FIN-015) belongs to SVC-API-SEARCH.
 */
@RestController
@RequestMapping("/api/v1/fin/allocation-rules")
@RequiredArgsConstructor
@Tag(name = "FIN Allocation Rule Management",
    description = "Cost-allocation rules - قواعد توزيع التكلفة")
public class AllocationRuleController {

    private final AllocationRuleService service;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create allocation rule", description = "إنشاء قاعدة توزيع تكلفة")
    public ResponseEntity<ApiResponse<AllocationRuleResponse>> create(
            @Valid @RequestBody AllocationRuleCreateRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    /**
     * API-FIN-017 — running a rule is an update-class custom action (SEC-BE.md matrix,
     * FIN_ALLOCATION_RULES / UPDATE).
     */
    @PostMapping("/{id}/run")
    @Operation(summary = "Run an allocation rule", description = "تشغيل قاعدة توزيع تكلفة")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> run(@PathVariable Long id) {
        return operationCode.craftResponse(service.run(id));
    }

    @PostMapping("/search")
    @Operation(summary = "Search allocation rules", description = "بحث في قواعد التوزيع")
    public ResponseEntity<ApiResponse<Page<AllocationRuleResponse>>> search(
            @Valid @RequestBody AllocationRuleSearchRequest searchRequest) {
        return operationCode.craftResponse(service.search(searchRequest));
    }
}
