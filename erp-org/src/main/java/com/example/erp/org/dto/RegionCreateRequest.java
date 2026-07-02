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
@Schema(description = "Request to create a new Region - طلب إنشاء منطقة جديدة")
public class RegionCreateRequest {

    @NotNull(message = "{validation.required}")
    @Schema(description = "Parent Legal Entity ID - معرف الكيان القانوني الأب", example = "1")
    private Long legalEntityFk;

    @NotNull(message = "{validation.required}")
    @Schema(description = "Region Type ID - معرف نوع المنطقة", example = "1")
    private Long regionTypeIdFk;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربية", example = "منطقة القاهرة الكبرى")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزية", example = "Greater Cairo Region")
    private String nameEn;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Notes - ملاحظات", example = "Covers all Cairo branches")
    private String notes;
}
