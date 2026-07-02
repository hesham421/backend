package com.example.erp.org.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profit Center response - استجابة مركز الربح")
public class ProfitCenterResponse {

    @Schema(description = "Unique identifier - المعرف الفريد")
    private Long id;

    @Schema(description = "System-generated business code (PC-[LE_CODE]-NNNNN) - الرمز الآلي")
    private String profitCenterCode;

    @Schema(description = "Arabic name - الاسم بالعربية")
    private String nameAr;

    @Schema(description = "English name - الاسم بالإنجليزية")
    private String nameEn;

    @Schema(description = "Parent Legal Entity ID - معرف الكيان القانوني الأب")
    private Long legalEntityFk;

    @Schema(description = "Parent Legal Entity business code - رمز الكيان القانوني الأب")
    private String legalEntityCode;

    @Schema(description = "Parent Legal Entity English name - اسم الكيان القانوني الأب")
    private String legalEntityNameEn;

    @Schema(description = "Active status - حالة التفعيل")
    private Boolean isActive;

    @Schema(description = "Notes - ملاحظات")
    private String notes;

    @Schema(description = "Created timestamp - تاريخ الإنشاء")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @Schema(description = "Created by - أنشئ بواسطة")
    private String createdBy;

    @Schema(description = "Updated timestamp - تاريخ التحديث")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    @Schema(description = "Updated by - حُدّث بواسطة")
    private String updatedBy;
}
