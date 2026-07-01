package com.example.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Region dropdown option - خيار المنطقة")
public class RegionOptionResponse {

    @Schema(description = "ID - المعرف")
    private Long id;

    @Schema(description = "Business code - رمز الأعمال")
    private String code;

    @Schema(description = "Arabic name - الاسم بالعربي")
    private String nameAr;

    @Schema(description = "English name - الاسم بالإنجليزي")
    private String nameEn;

    @Schema(description = "Legal entity ID - معرف الكيان القانوني")
    private Long legalEntityId;
}
