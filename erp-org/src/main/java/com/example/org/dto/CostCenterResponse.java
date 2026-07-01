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
@Schema(description = "Cost Center response - استجابة مركز التكلفة")
public class CostCenterResponse {

    @Schema(description = "Unique identifier - المعرف الفريد")
    private Long id;

    @Schema(description = "Business code (CC-BR_CODE-NNNNN) - رمز الأعمال")
    private String costCenterCode;

    @Schema(description = "Arabic name - الاسم بالعربي")
    private String nameAr;

    @Schema(description = "English name - الاسم بالإنجليزي")
    private String nameEn;

    @Schema(description = "Branch ID - معرف الفرع")
    private Long branchId;

    @Schema(description = "Branch name (English) - اسم الفرع")
    private String branchNameEn;

    @Schema(description = "Parent cost center ID - معرف مركز التكلفة الأب")
    private Long parentId;

    @Schema(description = "Parent cost center name (English) - اسم مركز التكلفة الأب")
    private String parentNameEn;

    @Schema(description = "Node type LOV code - نوع العقدة")
    private String nodeTypeId;

    @Schema(description = "Cost center type LOV code - نوع مركز التكلفة")
    private String costCenterTypeId;

    @Schema(description = "Active status - حالة التفعيل")
    private Boolean isActiveFl;

    @Schema(description = "Optional notes - ملاحظات")
    private String notes;

    @Schema(description = "Active child cost center count - عدد مراكز التكلفة الفرعية النشطة")
    private Integer activeChildCount;

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
