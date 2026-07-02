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
@Schema(description = "Request to create a new Department - طلب إنشاء قسم جديد")
public class DepartmentCreateRequest {

    @NotNull(message = "{validation.required}")
    @Schema(description = "Parent Branch ID - معرف الفرع الأب", example = "1")
    private Long branchFk;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربية", example = "قسم المبيعات")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزية", example = "Sales Department")
    private String nameEn;

    @Schema(description = "Parent Department ID — null for a root node - معرف القسم الأب", example = "null")
    private Long parentDepartmentFk;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Node type (LOV-ORG-003: SUMMARY, DETAIL) - نوع العقدة. Immutable after save.", example = "DETAIL")
    private String nodeTypeId;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Notes - ملاحظات", example = "Handles regional sales")
    private String notes;
}
