package com.example.erp.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Department tree node - عقدة شجرة الأقسام (API-ORG-020)")
public class DepartmentTreeNodeResponse {

    @Schema(description = "Unique identifier - المعرف الفريد")
    private Long id;

    @Schema(description = "Business code - الرمز")
    private String code;

    @Schema(description = "Arabic name - الاسم بالعربية")
    private String nameAr;

    @Schema(description = "English name - الاسم بالإنجليزية")
    private String nameEn;

    @Schema(description = "Node type (LOV-ORG-003: SUMMARY, DETAIL) - نوع العقدة")
    private String nodeTypeId;

    @Schema(description = "Active status - حالة التفعيل")
    private Boolean isActive;

    @Builder.Default
    @Schema(description = "Child nodes - العقد الفرعية")
    private List<DepartmentTreeNodeResponse> children = new ArrayList<>();
}
