package com.erp.mdm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-MDM-006 create request body (ENTITY-MDM-002 LookupValue). valueCode is the create-only natural
 * key — immutable afterwards (RULE-MDM-004), unique within the parent type (RULE-MDM-003). The parent
 * lookupTypeFk comes from the path {typeId}, never the body. Excludes id, isActiveFl (system default
 * 1), notes and audit fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create a reference-data lookup value - إنشاء قيمة قائمة مرجعية")
public class LookupValueCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Value code, unique within its type - رمز القيمة الفريد ضمن النوع", example = "PDF")
    private String valueCode;

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
