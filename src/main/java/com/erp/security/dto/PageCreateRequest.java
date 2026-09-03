package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-013 create request body (ENTITY-SEC-004 Page). Excludes id and audit fields. pageCode is
 * the create-only natural key — immutable afterwards (structurally absent from the update DTO).
 * moduleFk is required (the owning module → SEC_MODULE) so RULE-SEC-014 can resolve the page's
 * module; parentPageFk is optional (self-hierarchy).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create a screen/page registry entry - إنشاء شاشة في السجل")
public class PageCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "Unique page code - رمز الشاشة الفريد", example = "SEC_ROLES")
    private String pageCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Page name (Arabic) - اسم الشاشة بالعربية", example = "الأدوار")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "Page name (English) - اسم الشاشة بالإنجليزية", example = "Roles")
    private String nameEn;

    @NotNull(message = "{validation.required}")
    @Schema(description = "Owning module identifier - معرف الموديل المالك", example = "1")
    private Long moduleFk;

    @Schema(description = "Parent page identifier (hierarchy) - معرف الشاشة الأصل", example = "2")
    private Long parentPageFk;

    @Schema(description = "Active status; omit to default active - حالة التفعيل، اتركه فارغاً ليكون نشطاً", example = "true")
    private Boolean isActiveFl;
}
