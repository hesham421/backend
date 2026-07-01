package com.example.org.dto;

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
@Schema(description = "Request to create a new Department - طلب إنشاء قسم جديد")
public class DepartmentCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربي", example = "قسم الموارد البشرية")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزي", example = "Human Resources Department")
    private String nameEn;

    @NotNull(message = "{validation.required}")
    @Schema(description = "Branch ID - معرف الفرع", example = "1")
    private Long branchId;

    @Schema(description = "Parent department ID (null for root) - معرف القسم الأب", example = "5")
    private Long parentId;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Node type LOV code (DEPT_NODE_TYPE) - نوع العقدة", example = "SECTION")
    private String nodeTypeId;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Optional notes - ملاحظات اختيارية")
    private String notes;
}
