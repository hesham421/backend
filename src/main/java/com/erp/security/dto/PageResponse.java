package com.erp.security.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Screen/page registry entry - شاشة في السجل")
public class PageResponse {

    @Schema(description = "Unique identifier - المعرف الفريد", example = "1")
    private Long id;

    @Schema(description = "Unique page code - رمز الشاشة الفريد", example = "SEC_ROLES")
    private String pageCode;

    @Schema(description = "Page name (Arabic) - اسم الشاشة بالعربية", example = "الأدوار")
    private String nameAr;

    @Schema(description = "Page name (English) - اسم الشاشة بالإنجليزية", example = "Roles")
    private String nameEn;

    @Schema(description = "Owning module identifier - معرف الموديل المالك", example = "1")
    private Long moduleFk;

    @Schema(description = "Parent page identifier (hierarchy) - معرف الشاشة الأصل", example = "2")
    private Long parentPageFk;

    @Schema(description = "Active status - حالة التفعيل", example = "true")
    private Boolean isActiveFl;

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
