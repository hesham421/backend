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
@Schema(description = "Department response - استجابة القسم")
public class DepartmentResponse {

    @Schema(description = "Unique identifier - المعرف الفريد")
    private Long id;

    @Schema(description = "System-generated business code (DEP-[BR_CODE]-NNNNN) - الرمز الآلي")
    private String departmentCode;

    @Schema(description = "Arabic name - الاسم بالعربية")
    private String nameAr;

    @Schema(description = "English name - الاسم بالإنجليزية")
    private String nameEn;

    @Schema(description = "Parent Branch ID - معرف الفرع الأب")
    private Long branchFk;

    @Schema(description = "Parent Department ID — null for a root node - معرف القسم الأب")
    private Long parentDepartmentFk;

    @Schema(description = "Node type (LOV-ORG-003: SUMMARY, DETAIL) - نوع العقدة")
    private String nodeTypeId;

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
