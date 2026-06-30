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
@Schema(description = "Usage information for Profit Center - معلومات استخدام مركز الربح")
public class ProfitCenterUsageResponse {

    @Schema(description = "Profit Center ID - معرف مركز الربح")
    private Long id;

    @Schema(description = "Can entity be deleted - هل يمكن الحذف")
    private boolean canDelete;

    @Schema(description = "Can entity be deactivated - هل يمكن إلغاء التفعيل")
    private boolean canDeactivate;

    @Schema(description = "Reason if blocked - سبب الحظر")
    private String reason;
}
