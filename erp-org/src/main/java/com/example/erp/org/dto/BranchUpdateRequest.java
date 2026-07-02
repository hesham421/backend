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
@Schema(description = "Request to update a Branch - طلب تحديث فرع. Omitted fields are left unchanged.")
public class BranchUpdateRequest {

    // NO branchCode (RULE-ORG-011/014) — NO legalEntityFk (parent is immutable, not part of contract)

    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربية", example = "الفرع الرئيسي")
    private String nameAr;

    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزية", example = "Main Branch")
    private String nameEn;

    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Branch type code (LOV-ORG-002) - نوع الفرع", example = "MAIN_BRANCH")
    private String branchTypeId;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Notes - ملاحظات", example = "Head office branch")
    private String notes;
}
