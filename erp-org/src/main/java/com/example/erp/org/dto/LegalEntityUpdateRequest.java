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
@Schema(description = "Request to update a Legal Entity - طلب تحديث كيان قانوني. Omitted fields are left unchanged.")
public class LegalEntityUpdateRequest {

    // NO legalEntityCode — immutable after first save (RULE-ORG-011/014)

    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربية", example = "الشركة القابضة")
    private String nameAr;

    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزية", example = "Holding Company")
    private String nameEn;

    @Size(max = 50, message = "{validation.size}")
    @Schema(description = "Entity type code (LOV-ORG-001) - نوع الكيان", example = "HEAD_OFFICE")
    private String entityTypeId;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Notes - ملاحظات", example = "Primary holding entity")
    private String notes;
}
