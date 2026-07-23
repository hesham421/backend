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
@Schema(description = "FileCategory dropdown option - خيار تصنيف الملف (للاستخدام في القوائم المنسدلة)")
public class FileCategoryOptionResponse {

    @Schema(description = "File category ID - معرف تصنيف الملف")
    private Long fileCategoryPk;

    @Schema(description = "Category code - رمز التصنيف")
    private String categoryCode;

    @Schema(description = "Category name (Arabic) - اسم التصنيف بالعربي")
    private String nameAr;

    @Schema(description = "Category name (English) - اسم التصنيف بالإنجليزي")
    private String nameEn;

    @Schema(description = "Owning module code - رمز الوحدة المالكة")
    private String moduleCode;
}
