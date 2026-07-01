package com.example.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a Branch - طلب تعديل فرع")
public class BranchUpdateRequest {

    // branchCode excluded — RULE-ORG-014 (immutable)
    // legalEntityId excluded — FK immutable after creation

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربي", example = "الفرع الرئيسي")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزي", example = "Main Branch")
    private String nameEn;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Branch type LOV code (BRANCH_TYPE) - نوع الفرع", example = "MAIN_BRANCH")
    private String branchTypeId;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Optional notes - ملاحظات اختيارية")
    private String notes;
}
