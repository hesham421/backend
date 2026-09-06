package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-013 update request body. Excludes pageCode, moduleFk and parentPageFk — all immutable
 * after creation (RULE-SEC-010 / RULE-SEC-014 derivation stability), structurally enforced by
 * omission per build-create-dto A.3.6 ("UpdateRequest excludes immutable fields — natural keys,
 * FKs"). An update can only change the display names and the active flag; isActiveFl, when present,
 * drives reactivation/deactivation.
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

    @Schema(description = "Active status; omit to leave unchanged - حالة التفعيل، اتركه فارغاً لعدم التغيير", example = "true")
    private Boolean isActiveFl;
}
