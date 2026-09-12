package com.erp.fin.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.fin.dto.FiscalYearCreateRequest;
import com.erp.fin.dto.FiscalYearResponse;
import com.erp.fin.dto.YearEndCloseResponse;
import com.erp.fin.service.FiscalYearService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin controller for API-FIN-023 (create a fiscal year with its periods) and API-FIN-027 (run
 * year-end close) — SVC-API-INT.md. The year's periods travel inside the create response and are
 * acted on individually through {@link FiscalPeriodController}, so they need no create endpoint of
 * their own.
 *
 * <p>Both endpoints sit on screen FIN_PERIODS; year-end close is gated by the custom, SoD-gated
 * {@code PERM_FIN_PERIODS_CLOSE_APPROVE}, enforced in the service together with RULE-FIN-015's
 * own user-union check. Searching fiscal years belongs to SVC-API-SEARCH.
 */
@RestController
@RequestMapping("/api/v1/fin/fiscal-years")
@RequiredArgsConstructor
@Tag(name = "FIN Fiscal Year Management", description = "Fiscal years - السنوات المالية")
public class FiscalYearController {

    private final FiscalYearService service;
    private final OperationCode operationCode;

    @PostMapping
    @Operation(summary = "Create fiscal year and generate its periods",
        description = "إنشاء سنة مالية وتوليد فتراتها")
    public ResponseEntity<ApiResponse<FiscalYearResponse>> create(
            @Valid @RequestBody FiscalYearCreateRequest request) {
        return operationCode.craftResponse(service.create(request));
    }

    @PostMapping("/{id}/year-end-close")
    @Operation(summary = "Run year-end close",
        description = "تشغيل إقفال نهاية السنة وتوليد قيدي الإقفال والافتتاح")
    public ResponseEntity<ApiResponse<YearEndCloseResponse>> yearEndClose(
            @PathVariable Long id) {
        return operationCode.craftResponse(service.yearEndClose(id));
    }
}
