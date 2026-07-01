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
@Schema(description = "Usage information for Region Type - معلومات استخدام نوع المنطقة")
public class RegionTypeUsageResponse {

    @Schema(description = "Region Type ID - معرف نوع المنطقة")
    private Long id;

    @Schema(description = "Count of regions using this type - عدد المناطق المستخدمة لهذا النوع")
    private long activeRegionCount;

    @Schema(description = "Can entity be deleted - هل يمكن الحذف")
    private boolean canDelete;

    @Schema(description = "Can entity be deactivated - هل يمكن إلغاء التفعيل")
    private boolean canDeactivate;

    @Schema(description = "Reason if blocked - سبب الحظر")
    private String reason;
}
