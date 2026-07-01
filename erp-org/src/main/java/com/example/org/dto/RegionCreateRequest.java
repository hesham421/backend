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
@Schema(description = "Request to create a new Region - طلب إنشاء منطقة جديدة")
public class RegionCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Arabic name - الاسم بالعربي", example = "منطقة الرياض")
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "English name - الاسم بالإنجليزي", example = "Riyadh Region")
    private String nameEn;

    @NotNull(message = "{validation.required}")
    @Schema(description = "Legal entity ID - معرف الكيان القانوني", example = "1")
    private Long legalEntityId;

    @NotNull(message = "{validation.required}")
    @Schema(description = "Region type ID - معرف نوع المنطقة", example = "1")
    private Long regionTypeId;

    @Size(max = 2000, message = "{validation.size}")
    @Schema(description = "Optional notes - ملاحظات اختيارية")
    private String notes;
}
