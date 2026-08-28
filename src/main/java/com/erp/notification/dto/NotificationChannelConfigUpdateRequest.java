package com.erp.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * No {@code channelTypeId} — rows are pre-seeded, one per channel. Toggling {@code isEnabledFl}
 * to false is what triggers RULE-NOTIF-005's disabled-channel handling downstream.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a notification channel configuration - طلب تحديث إعدادات قناة الإشعار")
public class NotificationChannelConfigUpdateRequest {

    @NotNull(message = "{validation.required}")
    @Schema(description = "Whether this channel should be enabled", example = "true")
    private Boolean isEnabledFl;

    @Schema(description = "Provider-specific adapter configuration (free text, e.g. SMS/WhatsApp provider credentials — AQ-010/AQ-011)")
    private String configJson;
}
