package com.example.erp.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for API-NOTIF-004 (Unread Notifications). Contract shell only per SVCAPI.md —
 * the endpoint always throws {@code NOTIF_READ_TRACKING_UNAVAILABLE} today (DRV-NOTIF-003, no
 * read/unread column exists); this DTO documents the intended shape for when that SRS/DB
 * amendment lands.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Unread notification summary - ملخص الإشعارات غير المقروءة")
public class NotificationUnreadSummary {

    @Schema(description = "Unread count")
    private Long count;

    @Schema(description = "Unread notification log entries")
    private List<NotificationLogResponse> items;
}
