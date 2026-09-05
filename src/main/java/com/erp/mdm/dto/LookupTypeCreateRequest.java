package com.erp.mdm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-MDM-001 create request body (ENTITY-MDM-001 LookupType). typeCode is the create-only natural
 * key — immutable afterwards (RULE-MDM-002), so it is structurally absent from the update DTO.
 * Excludes id, isActiveFl (system default 1) and audit fields (AuditEntityListener).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create a reference-data lookup type - إنشاء نوع قائمة مرجعية")
public class LookupTypeCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Unique lookup type code - رمز النوع الفريد", example = "FILE_FILE_TYPE")
    private String typeCode;

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
