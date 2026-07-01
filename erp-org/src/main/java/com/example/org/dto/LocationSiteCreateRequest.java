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
@Schema(description = "Request to create a new Location Site - طلب إنشاء موقع عمل جديد")
public class LocationSiteCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربي", example = "مقر الرياض الرئيسي")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزي", example = "Riyadh Main Office")
    private String nameEn;

    @NotNull(message = "{validation.required}")
    @Schema(description = "Branch ID - معرف الفرع", example = "1")
    private Long branchId;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Site type LOV code (OFFICE|WAREHOUSE|FACTORY|SITE|RETAIL) - نوع الموقع", example = "OFFICE")
    private String siteTypeId;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Optional notes - ملاحظات اختيارية")
    private String notes;
}
