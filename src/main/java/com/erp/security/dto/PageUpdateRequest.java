package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-013 update request body. Excludes pageCode — immutable after creation (RULE-SEC-010,
 * structurally enforced by omission). moduleFk stays part of the representation (the owning module
 * is NOT NULL) and is re-validated/re-assigned by the service; PageDomain rejects a null moduleFk.
 * parentPageFk is optional. isActiveFl, when present, drives reactivation/deactivation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update a screen/page registry entry - تحديث شاشة في السجل")
public class PageUpdateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Page name (Arabic) - اسم الشاشة بالعربية", example = "الأدوار")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "Page name (English) - اسم الشاشة بالإنجليزية", example = "Roles")
    private String nameEn;

    @Schema(description = "Owning module identifier - معرف الموديل المالك", example = "1")
    private Long moduleFk;

    @Schema(description = "Parent page identifier (hierarchy) - معرف الشاشة الأصل", example = "2")
    private Long parentPageFk;

    @Schema(description = "Active status; omit to leave unchanged - حالة التفعيل، اتركه فارغاً لعدم التغيير", example = "true")
    private Boolean isActiveFl;
}
