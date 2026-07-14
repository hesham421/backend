package com.example.erp.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for API-NOTIF-008 (Update Template) — all fields EXCEPT {@code templateCode}
 * (RULE-NOTIF-007 — immutable post-create). See {@link NotificationTemplateCreateRequest}'s
 * javadoc for why templateBodyAr/En have no {@code @NotBlank}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a notification template - طلب تحديث قالب إشعار")
public class NotificationTemplateUpdateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Template display name (Arabic)")
    private String templateNameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Template display name (English)")
    private String templateNameEn;

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Schema(description = "Target channel (LOV-NOTIF-001)", example = "EMAIL")
    private String channelTypeId;

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Schema(description = "Owning module code", example = "SECURITY")
    private String moduleCode;

    @Schema(description = "Template body, Arabic")
    private String templateBodyAr;

    @Schema(description = "Template body, English")
    private String templateBodyEn;
}
