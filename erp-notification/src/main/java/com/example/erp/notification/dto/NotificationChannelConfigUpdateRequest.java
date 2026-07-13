package com.example.erp.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for API-NOTIF-012 (Update Channel Config). No {@code channelTypeId} — one fixed
 * row per channel already seeded (CORE.md: "no create/delete — only isEnabledFl toggle").
 * Toggling {@code isEnabledFl} to false is how RULE-NOTIF-005 (disabled-channel handling)
 * gets triggered downstream in {@code NotificationEventProcessor} — no rejection rule fires
 * on this toggle itself, per SVCAPI.md.
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
