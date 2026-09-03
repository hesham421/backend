package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-011 create request body (ENTITY-SEC-002 Role). Excludes id and audit fields. roleCode is
 * the create-only natural key — immutable afterwards (structurally absent from the update DTO).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create an RBAC role - إنشاء دور صلاحيات")
public class RoleCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Unique role code - رمز الدور الفريد", example = "SYS_ADMIN")
    private String roleCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Role name (Arabic) - اسم الدور بالعربية", example = "مدير النظام")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "Role name (English) - اسم الدور بالإنجليزية", example = "System Administrator")
    private String nameEn;

    @Schema(description = "Active status; omit to default active - حالة التفعيل، اتركه فارغاً ليكون نشطاً", example = "true")
    private Boolean isActiveFl;
}
