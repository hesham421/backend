package com.erp.mdm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-MDM-001..005 response body (ENTITY-MDM-001 LookupType) — all FIELD-0001..0006 plus audit.
 * The entity property is {@code isActive}; the DTO exposes {@code isActiveFl} (the mapper bridges).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reference-data lookup type - نوع قائمة مرجعية")
public class LookupTypeResponse {

    @Schema(description = "Unique identifier - المعرف الفريد", example = "1")
    private Long id;

    @Schema(description = "Unique lookup type code - رمز النوع الفريد", example = "FILE_FILE_TYPE")
    private String typeCode;

    @Schema(description = "Lookup type name (Arabic) - اسم النوع بالعربية", example = "نوع الملف")
    private String nameAr;

    @Schema(description = "Lookup type name (English) - اسم النوع بالإنجليزية", example = "File Type")
    private String nameEn;

    @Schema(description = "Active status - حالة التفعيل", example = "true")
    private Boolean isActiveFl;

    @Schema(description = "Optional notes - ملاحظات اختيارية")
    private String notes;

    @Schema(description = "Created timestamp - تاريخ الإنشاء")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @Schema(description = "Created by - أنشئ بواسطة")
    private String createdBy;

    @Schema(description = "Updated timestamp - تاريخ التحديث")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    @Schema(description = "Updated by - حُدّث بواسطة")
    private String updatedBy;
}
