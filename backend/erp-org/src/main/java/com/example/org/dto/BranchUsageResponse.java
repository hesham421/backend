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
@Schema(description = "Usage information for Branch - معلومات استخدام الفرع")
public class BranchUsageResponse {

    @Schema(description = "Branch ID - معرف الفرع")
    private Long id;

    @Schema(description = "Active department count - عدد الأقسام النشطة")
    private long activeDepartmentCount;

    @Schema(description = "Active cost center count - عدد مراكز التكلفة النشطة")
    private long activeCostCenterCount;

    @Schema(description = "Active location site count - عدد مواقع العمل النشطة")
    private long activeLocationSiteCount;

    @Schema(description = "Can entity be deleted - هل يمكن الحذف")
    private boolean canDelete;

    @Schema(description = "Can entity be deactivated - هل يمكن إلغاء التفعيل")
    private boolean canDeactivate;

    @Schema(description = "Reason if blocked - سبب الحظر")
    private String reason;
}
