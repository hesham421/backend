package com.erp.mdm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-MDM-006..010 response body (ENTITY-MDM-002 LookupValue) — all business fields plus audit.
 * Carries {@code notes} even though the create request omits it (build-create-dto A.3.7). The entity
 * property is {@code isActive}; the DTO exposes {@code isActiveFl} (the mapper bridges).
 * {@code lookupTypeFk} is the parent LookupType id read off the (lazy) association.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reference-data lookup value - قيمة قائمة مرجعية")
public class LookupValueResponse {

    @Schema(description = "Unique identifier - المعرف الفريد", example = "1")
    private Long id;

    @Schema(description = "Parent lookup type identifier - معرف النوع الأب", example = "1")
    private Long lookupTypeFk;

    @Schema(description = "Value code, unique within its type - رمز القيمة الفريد ضمن النوع", example = "PDF")
    private String valueCode;

    @Schema(description = "Value name (Arabic) - اسم القيمة بالعربية", example = "ملف PDF")
    private String nameAr;

    @Schema(description = "Value name (English) - اسم القيمة بالإنجليزية", example = "PDF File")
    private String nameEn;

    @Schema(description = "Sort order within the type - ترتيب العرض ضمن النوع", example = "10")
    private Short sortOrder;

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
