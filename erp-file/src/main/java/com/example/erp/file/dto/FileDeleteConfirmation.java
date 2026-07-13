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
@Schema(description = "File deletion confirmation - تأكيد حذف الملف")
public class FileDeleteConfirmation {

    @Schema(description = "File document ID - معرف مستند الملف", example = "501")
    private Long fileDocumentPk;

    @Schema(description = "File status (LOV-FILE-002) - حالة الملف", example = "DELETED")
    private String fileStatusId;
}
