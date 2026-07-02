package com.example.erp.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a Cost Center - طلب تحديث مركز تكلفة. Omitted fields are left unchanged.")
public class CostCenterUpdateRequest {

    // NO costCenterCode (RULE-ORG-011/014) — NO branchFk (immutable, not part of update contract)
    // NO nodeTypeId — immutable after first save (RULE-ORG-020)

    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربية", example = "مركز تكلفة الإنتاج")
    private String nameAr;

    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزية", example = "Production Cost Center")
    private String nameEn;

    @Schema(description = "New parent Cost Center ID (cycle-checked, RULE-ORG-008) - معرف مركز التكلفة الأب الجديد", example = "2")
    private Long parentCostCenterFk;

    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Cost center type (LOV-ORG-005: DIRECT, INDIRECT, SHARED) - نوع مركز التكلفة", example = "DIRECT")
    private String costCenterTypeId;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Notes - ملاحظات", example = "Main production line")
    private String notes;
}
