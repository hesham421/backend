package com.example.erp.file.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * API-FILE-005 list-row DTO — {@code fileContent} is NEVER included (metadata only, per
 * CORE.md's "sole binary field" note and the plan's own explicit statement).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "File summary for owner record listing - ملخص الملف لقائمة السجل المالك")
public class FileDocumentSummaryResponse {

    @Schema(description = "File document ID - معرف مستند الملف", example = "501")
    private Long fileDocumentPk;

    @Schema(description = "Original file name - اسم الملف الأصلي", example = "invoice.pdf")
    private String fileNameOriginal;

    @Schema(description = "File category ID - معرف تصنيف الملف", example = "3")
    private Long fileCategoryFk;

    @Schema(description = "File category name (Arabic) - اسم تصنيف الملف بالعربي")
    private String fileCategoryNameAr;

    @Schema(description = "File category name (English) - اسم تصنيف الملف بالإنجليزي")
    private String fileCategoryNameEn;

    @Schema(description = "Detected file type (LOV-FILE-001) - نوع الملف المكتشف", example = "DOCUMENT")
    private String fileTypeId;

    @Schema(description = "File size in bytes - حجم الملف بالبايت", example = "204800")
    private Long fileSizeBytes;

    @Schema(description = "File status (LOV-FILE-002) - حالة الملف", example = "ACTIVE")
    private String fileStatusId;

    @Schema(description = "Created timestamp - تاريخ الإنشاء")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
}
