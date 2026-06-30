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
@Schema(description = "Request to update a Cost Center - طلب تعديل مركز تكلفة")
public class CostCenterUpdateRequest {

    // costCenterCode excluded — RULE-ORG-014 (immutable)
    // branchId excluded — FK immutable after creation
    // nodeTypeId excluded — RULE-ORG-020 (immutable after first save)

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربي", example = "مركز تكلفة التشغيل")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزي", example = "Operations Cost Center")
    private String nameEn;

    @Schema(description = "Parent cost center ID (null for root) - معرف مركز التكلفة الأب", example = "5")
    private Long parentId;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Cost center type LOV code (DIRECT|INDIRECT|SHARED) - نوع مركز التكلفة", example = "DIRECT")
    private String costCenterTypeId;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Optional notes - ملاحظات اختيارية")
    private String notes;
}
