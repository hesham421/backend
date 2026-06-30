package com.example.org.dto;

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
@Schema(description = "Branch response - استجابة الفرع")
public class BranchResponse {

    @Schema(description = "Unique identifier - المعرف الفريد")
    private Long id;

    @Schema(description = "Business code (BR-LE_CODE-NNNNN) - رمز الأعمال")
    private String branchCode;

    @Schema(description = "Arabic name - الاسم بالعربي")
    private String nameAr;

    @Schema(description = "English name - الاسم بالإنجليزي")
    private String nameEn;

    @Schema(description = "Legal entity ID - معرف الكيان القانوني")
    private Long legalEntityId;

    @Schema(description = "Legal entity name (English) - اسم الكيان القانوني")
    private String legalEntityNameEn;

    @Schema(description = "Branch type LOV code - نوع الفرع")
    private String branchTypeId;

    @Schema(description = "Active status - حالة التفعيل")
    private Boolean isActiveFl;

    @Schema(description = "Optional notes - ملاحظات")
    private String notes;

    @Schema(description = "Active department count - عدد الأقسام النشطة")
    private Integer activeDepartmentCount;

    @Schema(description = "Active cost center count - عدد مراكز التكلفة النشطة")
    private Integer activeCostCenterCount;

    @Schema(description = "Active location site count - عدد مواقع العمل النشطة")
    private Integer activeLocationSiteCount;

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
