package com.example.erp.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for API-NOTIF-001/002 — one log entry ID per fan-out channel (RULE-NOTIF-003).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification send/schedule confirmation - تأكيد إرسال/جدولة الإشعار")
public class NotificationSendConfirmation {

    @Schema(description = "One NOTIF_LOG id per fan-out channel - معرفات سجلات الإشعار")
    private Long[] logEntryIds;
}
