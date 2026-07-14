package com.example.erp.notification.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.notification.dto.NotificationTemplateCreateRequest;
import com.example.erp.notification.dto.NotificationTemplateResponse;
import com.example.erp.notification.dto.NotificationTemplateSearchRequest;
import com.example.erp.notification.dto.NotificationTemplateUpdateRequest;
import com.example.erp.notification.service.NotificationTemplateService;
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

/**
 * SCR-NOTIF-002 (إدارة قوالب الإشعارات) — API-NOTIF-006..010. Search is POST + @RequestBody
 * (not SVCAPI.md's literal GET) for the same A.6.6 reason documented on
 * {@code NotificationInboxController} — {@code NotificationTemplateSearchRequest} extends
 * {@code BaseSearchContractRequest}, which this platform always exposes via POST /search.
 */
@RestController
@RequestMapping("/api/v1/notifications/templates")
@RequiredArgsConstructor
@Tag(name = "Notification - Templates", description = "إدارة قوالب الإشعارات - Notification Template Management")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;
    private final OperationCode operationCode;

    @PostMapping("/search")
    @Operation(summary = "Search notification templates", description = "بحث في قوالب الإشعارات - API-NOTIF-006")
    public ResponseEntity<ApiResponse<Page<NotificationTemplateResponse>>> search(
            @Valid @RequestBody NotificationTemplateSearchRequest searchRequest) {
        return operationCode.craftResponse(templateService.search(searchRequest));
    }

    @PostMapping
    @Operation(summary = "Create notification template", description = "إنشاء قالب إشعار - API-NOTIF-007")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> create(
            @Valid @RequestBody NotificationTemplateCreateRequest request) {
        return operationCode.craftResponse(templateService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update notification template", description = "تحديث قالب إشعار - API-NOTIF-008")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody NotificationTemplateUpdateRequest request) {
        return operationCode.craftResponse(templateService.update(id, request));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate notification template", description = "إلغاء تفعيل قالب إشعار - API-NOTIF-009")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> deactivate(@PathVariable Long id) {
        return operationCode.craftResponse(templateService.deactivate(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification template by ID", description = "جلب قالب إشعار بالمعرف - API-NOTIF-010")
    public ResponseEntity<ApiResponse<NotificationTemplateResponse>> getById(@PathVariable Long id) {
        return operationCode.craftResponse(templateService.getById(id));
    }
}
