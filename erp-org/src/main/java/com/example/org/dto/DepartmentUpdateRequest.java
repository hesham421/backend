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
@Schema(description = "Request to update a Department - طلب تعديل قسم")
public class DepartmentUpdateRequest {

    // departmentCode excluded — RULE-ORG-014 (immutable)
    // branchId excluded — FK immutable after creation
    // nodeTypeId excluded — RULE-ORG-020 (immutable after first save)

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربي", example = "قسم الموارد البشرية")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزي", example = "Human Resources Department")
    private String nameEn;

    @Schema(description = "Parent department ID (null for root) - معرف القسم الأب", example = "5")
    private Long parentId;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Optional notes - ملاحظات اختيارية")
    private String notes;
}
