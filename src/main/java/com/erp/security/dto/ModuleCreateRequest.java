package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-020 create request body (ENTITY-SEC-010 Module). Excludes id and audit fields. moduleCode
 * is the create-only natural key — immutable afterwards (structurally absent from the update DTO).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create a security module registry entry - إنشاء موديل في سجل الموديولات")
public class ModuleCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Unique module code - رمز الموديل الفريد", example = "SEC")
    private String moduleCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Module name (Arabic) - اسم الموديل بالعربية", example = "الأمان")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "Module name (English) - اسم الموديل بالإنجليزية", example = "Security")
    private String nameEn;

    @Schema(description = "Active status; omit to default active - حالة التفعيل، اتركه فارغاً ليكون نشطاً", example = "true")
    private Boolean isActiveFl;
}
