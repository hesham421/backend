package com.erp.security.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Auto-generated screen permission (read-only) - صلاحية شاشة مُولَّدة")
public class PermissionResponse {

    @Schema(description = "Unique identifier - المعرف الفريد", example = "1")
    private Long id;

    @Schema(description = "Permission code (PERM_<PAGE_CODE>_<TYPE>) - رمز الصلاحية", example = "PERM_SEC_ROLES_VIEW")
    private String permissionCode;

    @Schema(description = "Permission type (VIEW/CREATE/UPDATE/DELETE) - نوع الصلاحية", example = "VIEW")
    private String permissionType;

    @Schema(description = "Permission name (Arabic) - اسم الصلاحية بالعربية", example = "الأدوار - VIEW")
    private String nameAr;

    @Schema(description = "Permission name (English) - اسم الصلاحية بالإنجليزية", example = "Roles - VIEW")
    private String nameEn;

    @Schema(description = "Active status - حالة التفعيل", example = "true")
    private Boolean isActiveFl;

    @Schema(description = "Owning page identifier - معرف الشاشة المالكة", example = "3")
    private Long pageFk;

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
