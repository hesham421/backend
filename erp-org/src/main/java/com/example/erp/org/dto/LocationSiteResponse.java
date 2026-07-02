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
@Schema(description = "Location Site response - استجابة موقع العمل")
public class LocationSiteResponse {

    @Schema(description = "Unique identifier - المعرف الفريد")
    private Long id;

    @Schema(description = "System-generated business code (LS-[BR_CODE]-NNNNN) - الرمز الآلي")
    private String locationSiteCode;

    @Schema(description = "Arabic name - الاسم بالعربية")
    private String nameAr;

    @Schema(description = "English name - الاسم بالإنجليزية")
    private String nameEn;

    @Schema(description = "Parent Branch ID - معرف الفرع الأب")
    private Long branchFk;

    @Schema(description = "Parent Branch business code - رمز الفرع الأب")
    private String branchCode;

    @Schema(description = "Parent Branch English name - اسم الفرع الأب")
    private String branchNameEn;

    @Schema(description = "Site type code (LOV-ORG-006) - نوع الموقع")
    private String siteTypeId;

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
