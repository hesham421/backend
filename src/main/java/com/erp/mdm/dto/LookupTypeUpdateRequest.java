package com.erp.mdm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-MDM-003 update request body. Excludes typeCode — immutable after creation (RULE-MDM-002,
 * structurally enforced by omission). isActiveFl is excluded: this module exposes no reactivation
 * endpoint, so activation state is never changed through update.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update a reference-data lookup type - تحديث نوع قائمة مرجعية")
public class LookupTypeUpdateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Lookup type name (Arabic) - اسم النوع بالعربية", example = "نوع الملف")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "Lookup type name (English) - اسم النوع بالإنجليزية", example = "File Type")
    private String nameEn;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Optional notes - ملاحظات اختيارية")
    private String notes;
}
