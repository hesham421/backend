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
@Schema(description = "Request to update a Region - طلب تحديث منطقة. Omitted fields are left unchanged.")
public class RegionUpdateRequest {

    // NO regionCode (RULE-ORG-011/014) — NO legalEntityFk/regionTypeIdFk (immutable, not part of update contract)

    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربية", example = "منطقة القاهرة الكبرى")
    private String nameAr;

    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزية", example = "Greater Cairo Region")
    private String nameEn;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Notes - ملاحظات", example = "Covers all Cairo branches")
    private String notes;
}
