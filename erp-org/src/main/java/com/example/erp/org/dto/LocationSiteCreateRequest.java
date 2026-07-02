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
@Schema(description = "Request to create a new Location Site - طلب إنشاء موقع عمل جديد")
public class LocationSiteCreateRequest {

    @NotNull(message = "{validation.required}")
    @Schema(description = "Parent Branch ID - معرف الفرع الأب", example = "1")
    private Long branchFk;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربية", example = "المستودع الرئيسي")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزية", example = "Main Warehouse")
    private String nameEn;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Site type code (LOV-ORG-006: OFFICE, WAREHOUSE, FACTORY, SITE, RETAIL) - نوع الموقع", example = "WAREHOUSE")
    private String siteTypeId;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Notes - ملاحظات", example = "Central distribution hub")
    private String notes;
}
