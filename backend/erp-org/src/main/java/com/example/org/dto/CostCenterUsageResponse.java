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
@Schema(description = "Usage information for Cost Center - معلومات استخدام مركز التكلفة")
public class CostCenterUsageResponse {

    @Schema(description = "Cost Center ID - معرف مركز التكلفة")
    private Long id;

    @Schema(description = "Active child cost center count - عدد مراكز التكلفة الفرعية النشطة")
    private long activeChildCount;

    @Schema(description = "Can entity be deleted - هل يمكن الحذف")
    private boolean canDelete;

    @Schema(description = "Can entity be deactivated - هل يمكن إلغاء التفعيل")
    private boolean canDeactivate;

    @Schema(description = "Reason if blocked - سبب الحظر")
    private String reason;
}
