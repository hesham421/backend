package com.example.erp.notification.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.notification.dto.NotificationHistorySearchRequest;
import com.example.erp.notification.dto.NotificationLogResponse;
import com.example.erp.notification.dto.NotificationSendConfirmation;
import com.example.erp.notification.dto.NotificationUnreadSummary;
import com.example.erp.notification.service.NotificationLogQueryService;
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
 * SCR-NOTIF-001 (لوحة إشعاراتي) — API-NOTIF-003 (History), API-NOTIF-004 (Unread), API-NOTIF-005
 * (Mark as Read). History's method is POST + @RequestBody, not the GET SVCAPI.md's prose names
 * for it — {@code NotificationHistorySearchRequest extends BaseSearchContractRequest}, and this
 * platform's own enforce-backend-contract A.6.6 rule mandates POST /search + @RequestBody for
 * any BaseSearchContractRequest-shaped search, never GET (see execution-state.json note).
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification - Inbox", description = "لوحة إشعاراتي - Notification Inbox API")
public class NotificationInboxController {

    private final NotificationLogQueryService queryService;
    private final OperationCode operationCode;

    @PostMapping("/history/search")
    @Operation(summary = "Search notification history", description = "بحث سجل الإشعارات - API-NOTIF-003")
    public ResponseEntity<ApiResponse<Page<NotificationLogResponse>>> searchHistory(
            @Valid @RequestBody NotificationHistorySearchRequest searchRequest) {
        return operationCode.craftResponse(queryService.search(searchRequest));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "الإشعارات غير المقروءة - API-NOTIF-004 (GOVERNANCE-NOTE-BLOCKED, see DRV-NOTIF-003)")
    public ResponseEntity<ApiResponse<NotificationUnreadSummary>> unread() {
        return operationCode.craftResponse(queryService.getUnread());
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "تعليم كمقروء - API-NOTIF-005 (GOVERNANCE-NOTE-BLOCKED, see DRV-NOTIF-003)")
    public ResponseEntity<ApiResponse<NotificationSendConfirmation>> markAsRead(@PathVariable Long id) {
        return operationCode.craftResponse(queryService.markAsRead(id));
    }
}
