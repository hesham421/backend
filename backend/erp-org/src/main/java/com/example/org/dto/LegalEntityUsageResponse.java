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
@Schema(description = "Usage information for Legal Entity - معلومات استخدام الكيان القانوني")
public class LegalEntityUsageResponse {

    @Schema(description = "Entity ID - المعرف")
    private Long id;

    @Schema(description = "Number of active branches - عدد الفروع النشطة")
    private long activeBranchCount;

    @Schema(description = "Number of active profit centers - عدد مراكز الربح النشطة")
    private long activeProfitCenterCount;

    @Schema(description = "Can entity be deleted - هل يمكن الحذف")
    private boolean canDelete;

    @Schema(description = "Can entity be deactivated - هل يمكن إلغاء التفعيل")
    private boolean canDeactivate;

    @Schema(description = "Reason if actions are blocked - سبب الحظر")
    private String reason;
}
