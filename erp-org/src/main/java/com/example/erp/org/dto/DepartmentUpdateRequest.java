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
@Schema(description = "Request to update a Department - طلب تحديث قسم. Omitted fields are left unchanged.")
public class DepartmentUpdateRequest {

    // NO departmentCode (RULE-ORG-011/014) — NO branchFk (immutable, not part of update contract)
    // NO nodeTypeId — immutable after first save (RULE-ORG-020)

    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربية", example = "قسم المبيعات")
    private String nameAr;

    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزية", example = "Sales Department")
    private String nameEn;

    @Schema(description = "New parent Department ID (cycle-checked, RULE-ORG-007) - معرف القسم الأب الجديد", example = "2")
    private Long parentDepartmentFk;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Notes - ملاحظات", example = "Handles regional sales")
    private String notes;
}
