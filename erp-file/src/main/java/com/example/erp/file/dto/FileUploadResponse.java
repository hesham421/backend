package com.example.erp.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Uploaded file result - نتيجة رفع الملف")
public class FileUploadResponse {

    @Schema(description = "File document ID - معرف مستند الملف", example = "501")
    private Long fileDocumentPk;

    @Schema(description = "Original file name - اسم الملف الأصلي", example = "invoice.pdf")
    private String fileNameOriginal;

    @Schema(description = "Detected file type (LOV-FILE-001) - نوع الملف المكتشف", example = "DOCUMENT")
    private String fileTypeId;

    @Schema(description = "File size in bytes - حجم الملف بالبايت", example = "204800")
    private Long fileSizeBytes;

    @Schema(description = "File status (LOV-FILE-002) - حالة الملف", example = "ACTIVE")
    private String fileStatusId;
}
