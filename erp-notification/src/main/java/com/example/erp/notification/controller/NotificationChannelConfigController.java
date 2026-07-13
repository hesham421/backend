package com.example.erp.notification.controller;

import com.example.erp.common.web.ApiResponse;
import com.example.erp.common.web.OperationCode;
import com.example.erp.notification.dto.NotificationChannelConfigResponse;
import com.example.erp.notification.dto.NotificationChannelConfigUpdateRequest;
import com.example.erp.notification.service.NotificationChannelConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SCR-NOTIF-003 (إعدادات قنوات الإشعار) — API-NOTIF-011 (List, plain GET: fixed 5 rows, no
 * filters/pagination, so no A.6.6 concern here) + API-NOTIF-012 (Update).
 */
@RestController
@RequestMapping("/api/v1/notifications/channel-configs")
@RequiredArgsConstructor
@Tag(name = "Notification - Channel Config", description = "إعدادات قنوات الإشعار - Notification Channel Configuration")
public class NotificationChannelConfigController {

    private final NotificationChannelConfigService channelConfigService;
    private final OperationCode operationCode;

    @GetMapping
    @Operation(summary = "List channel configurations", description = "قائمة إعدادات قنوات الإشعار - API-NOTIF-011")
    public ResponseEntity<ApiResponse<List<NotificationChannelConfigResponse>>> list() {
        return operationCode.craftResponse(channelConfigService.list());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update channel configuration", description = "تحديث إعدادات قناة الإشعار - API-NOTIF-012")
    public ResponseEntity<ApiResponse<NotificationChannelConfigResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody NotificationChannelConfigUpdateRequest request) {
        return operationCode.craftResponse(channelConfigService.update(id, request));
    }
}
