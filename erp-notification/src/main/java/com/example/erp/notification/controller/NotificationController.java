package com.example.erp.notification.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.notification.dto.NotificationScheduleRequest;
import com.example.erp.notification.dto.NotificationSendConfirmation;
import com.example.erp.notification.dto.NotificationSendRequest;
import com.example.erp.notification.service.NotificationEventProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Thin controller for API-NOTIF-001 (Send Immediate) and API-NOTIF-002 (Schedule) — all logic
 * lives in {@link NotificationEventProcessor}.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification - Send/Schedule", description = "إرسال وجدولة الإشعارات - Notification Send/Schedule API")
public class NotificationController {

    private final NotificationEventProcessor eventProcessor;
    private final OperationCode operationCode;

    @PostMapping("/send")
    @Operation(summary = "Send notification immediately", description = "إرسال إشعار فوري - API-NOTIF-001")
    public ResponseEntity<ApiResponse<NotificationSendConfirmation>> send(
            @Valid @RequestBody NotificationSendRequest request) {
        return operationCode.craftResponse(eventProcessor.send(request));
    }

    @PostMapping("/schedule")
    @Operation(summary = "Schedule a future notification", description = "جدولة إشعار مستقبلي - API-NOTIF-002")
    public ResponseEntity<ApiResponse<NotificationSendConfirmation>> schedule(
            @Valid @RequestBody NotificationScheduleRequest request) {
        return operationCode.craftResponse(eventProcessor.schedule(request));
    }
}
