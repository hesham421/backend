package com.example.erp.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for API-NOTIF-007 (Create Template). {@code fileFk} is deliberately never
 * accepted here — DEFERRED/unused Phase 1 (XM-NOTIF-001).
 *
 * <p>{@code templateBodyAr}/{@code templateBodyEn} intentionally have no {@code @NotBlank} —
 * RULE-NOTIF-006's bilingual-completeness check is enforced explicitly in {@code
 * NotificationTemplateService} with the plan's own ERR-NOTIF-0002 code (same precedent as
 * erp-file's {@code FILE_NAME_REQUIRED}), not the generic framework {@code VALIDATION_ERROR}
 * that a bean-validation annotation would produce instead.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a notification template - طلب إنشاء قالب إشعار")
public class NotificationTemplateCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Unique, immutable natural code - رمز القالب", example = "USER_WELCOME")
    private String templateCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Template display name (Arabic) - اسم القالب بالعربي")
    private String templateNameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Template display name (English) - اسم القالب بالإنجليزي")
    private String templateNameEn;

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Schema(description = "Target channel (LOV-NOTIF-001) - القناة المستهدفة", example = "EMAIL")
    private String channelTypeId;

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Schema(description = "Owning module code - رمز الموديول المالك", example = "SECURITY")
    private String moduleCode;

    @Schema(description = "Template body, Arabic — supports {{placeholder}} syntax - نص القالب بالعربي")
    private String templateBodyAr;

    @Schema(description = "Template body, English - نص القالب بالإنجليزي")
    private String templateBodyEn;
}
