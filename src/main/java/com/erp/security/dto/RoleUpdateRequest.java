package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-011 update request body. Excludes roleCode — immutable after creation (RULE-SEC-010,
 * structurally enforced by omission). isActiveFl, when present, drives reactivation/deactivation
 * via the entity's own helpers; omitted (null) means "no change".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update an RBAC role - تحديث دور صلاحيات")
public class RoleUpdateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Role name (Arabic) - اسم الدور بالعربية", example = "مدير النظام")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "Role name (English) - اسم الدور بالإنجليزية", example = "System Administrator")
    private String nameEn;

    @Schema(description = "Active status; omit to leave unchanged - حالة التفعيل، اتركه فارغاً لعدم التغيير", example = "true")
    private Boolean isActiveFl;
}
