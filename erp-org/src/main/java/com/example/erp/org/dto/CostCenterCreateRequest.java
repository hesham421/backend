package com.example.erp.org.dto;

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
@Schema(description = "Request to create a new Cost Center - طلب إنشاء مركز تكلفة جديد")
public class CostCenterCreateRequest {

    @NotNull(message = "{validation.required}")
    @Schema(description = "Parent Branch ID - معرف الفرع الأب", example = "1")
    private Long branchFk;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربية", example = "مركز تكلفة الإنتاج")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزية", example = "Production Cost Center")
    private String nameEn;

    @Schema(description = "Parent Cost Center ID — null for a root node - معرف مركز التكلفة الأب", example = "null")
    private Long parentCostCenterFk;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Node type (LOV-ORG-004: SUMMARY, DETAIL) - نوع العقدة. Immutable after save.", example = "DETAIL")
    private String nodeTypeId;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Cost center type (LOV-ORG-005: DIRECT, INDIRECT, SHARED) - نوع مركز التكلفة", example = "DIRECT")
    private String costCenterTypeId;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Notes - ملاحظات", example = "Main production line")
    private String notes;
}
