package com.example.erp.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for API-NOTIF-011/012 — ENTITY-NOTIF-003 (NotificationChannelConfig), all
 * fields + audit.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification channel configuration - إعدادات قناة الإشعار")
public class NotificationChannelConfigResponse {

    @Schema(description = "Channel config ID")
    private Long id;

    @Schema(description = "Channel this config row governs (LOV-NOTIF-001)")
    private String channelTypeId;

    @Schema(description = "Whether this channel is currently enabled")
    private Boolean isEnabledFl;

    @Schema(description = "Provider-specific adapter configuration (free text)")
    private String configJson;

    @Schema(description = "Created timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @Schema(description = "Created by")
    private String createdBy;

    @Schema(description = "Updated timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    @Schema(description = "Updated by")
    private String updatedBy;
}
