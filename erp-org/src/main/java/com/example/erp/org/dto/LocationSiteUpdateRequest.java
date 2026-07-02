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
@Schema(description = "Request to update a Location Site - طلب تحديث موقع عمل. Omitted fields are left unchanged.")
public class LocationSiteUpdateRequest {

    // NO locationSiteCode (RULE-ORG-011/014) — NO branchFk (immutable, not part of update contract)

    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربية", example = "المستودع الرئيسي")
    private String nameAr;

    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزية", example = "Main Warehouse")
    private String nameEn;

    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Site type code (LOV-ORG-006: OFFICE, WAREHOUSE, FACTORY, SITE, RETAIL) - نوع الموقع", example = "WAREHOUSE")
    private String siteTypeId;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Notes - ملاحظات", example = "Central distribution hub")
    private String notes;
}
