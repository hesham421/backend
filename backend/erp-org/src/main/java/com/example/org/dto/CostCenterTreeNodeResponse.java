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
@Schema(description = "Cost center tree node - عقدة شجرة مركز التكلفة")
public class CostCenterTreeNodeResponse {

    @Schema(description = "Cost center ID - معرف مركز التكلفة", example = "1")
    private Long id;

    @Schema(description = "Cost center code - رمز مركز التكلفة", example = "CC-HQ-00001")
    private String code;

    @Schema(description = "Arabic name - الاسم بالعربي", example = "مركز تكلفة الإنتاج")
    private String nameAr;

    @Schema(description = "English name - الاسم بالإنجليزي", example = "Production Cost Center")
    private String nameEn;

    @Schema(description = "Node type: SUMMARY or DETAIL - نوع العقدة", example = "SUMMARY")
    private String nodeType;

    @Schema(description = "Child cost center nodes - مراكز التكلفة الفرعية")
    private List<CostCenterTreeNodeResponse> children;
}
