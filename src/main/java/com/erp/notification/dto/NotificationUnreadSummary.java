package com.erp.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Contract shell only — the endpoint always throws {@code NOTIF_READ_TRACKING_UNAVAILABLE} today
 * (no read/unread column exists yet); this documents the intended shape.
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
