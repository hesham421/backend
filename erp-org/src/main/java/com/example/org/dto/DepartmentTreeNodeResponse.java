package com.example.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Department tree node - عقدة شجرة القسم")
public class DepartmentTreeNodeResponse {

    @Schema(description = "Department ID - معرف القسم", example = "1")
    private Long id;

    @Schema(description = "Department code - رمز القسم", example = "DEP-HQ-00001")
    private String code;

    @Schema(description = "Arabic name - الاسم بالعربي", example = "قسم الموارد البشرية")
    private String nameAr;

    @Schema(description = "English name - الاسم بالإنجليزي", example = "HR Department")
    private String nameEn;

    @Schema(description = "Node type: SUMMARY or DETAIL - نوع العقدة", example = "SUMMARY")
    private String nodeType;

    @Schema(description = "Child department nodes - الأقسام الفرعية")
    private List<DepartmentTreeNodeResponse> children;
}
