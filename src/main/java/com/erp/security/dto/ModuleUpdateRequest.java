package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-020 update request body. Excludes moduleCode — immutable after creation (RULE-SEC-010,
 * structurally enforced by omission). isActiveFl, when present, drives reactivation/deactivation
 * via the entity's own helpers; omitted (null) means "no change".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update a security module registry entry - تحديث موديل في سجل الموديولات")
public class ModuleUpdateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Module name (Arabic) - اسم الموديل بالعربية", example = "الأمان")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "Module name (English) - اسم الموديل بالإنجليزية", example = "Security")
    private String nameEn;

    @Schema(description = "Active status; omit to leave unchanged - حالة التفعيل، اتركه فارغاً لعدم التغيير", example = "true")
    private Boolean isActiveFl;
}
