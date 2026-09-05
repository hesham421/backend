package com.erp.mdm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-MDM-008 update request body (ENTITY-MDM-002 LookupValue). Excludes valueCode — immutable after
 * creation (RULE-MDM-004, structurally enforced by omission) — and lookupTypeFk (immutable parent).
 * isActiveFl is excluded: this module exposes no reactivation endpoint, so activation state is never
 * changed through update.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update a reference-data lookup value - تحديث قيمة قائمة مرجعية")
public class LookupValueUpdateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Value name (Arabic) - اسم القيمة بالعربية", example = "ملف PDF")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "Value name (English) - اسم القيمة بالإنجليزية", example = "PDF File")
    private String nameEn;

    @Schema(description = "Sort order within the type - ترتيب العرض ضمن النوع", example = "10")
    private Short sortOrder;
}
