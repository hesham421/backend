package com.example.erp.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to issue an upload token - طلب إصدار رمز رفع الملف")
public class FileUploadTokenRequest {

    @NotNull(message = "{validation.required}")
    @Schema(description = "Owning record ID - معرف السجل المالك", example = "1001")
    private Long ownerId;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "Owning record type - نوع السجل المالك", example = "PURCHASE_ORDER")
    private String ownerType;

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Schema(description = "Requesting module code - رمز الوحدة الطالبة", example = "PRC")
    private String moduleCode;

    @NotNull(message = "{validation.required}")
    @Schema(description = "File category ID - معرف تصنيف الملف", example = "3")
    private Long fileCategoryFk;
}
