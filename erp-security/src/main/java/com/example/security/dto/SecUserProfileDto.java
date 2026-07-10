package com.example.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * SEC_USER_PROFILE response DTO (ENTITY-SEC-009).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile / branch assignment - ملف تعريف المستخدم وإسناد الفرع")
public class SecUserProfileDto {

    @Schema(description = "User ID (shared PK with USERS)", example = "1")
    private Long userIdFk;

    @Schema(description = "Assigned active branch ID", example = "1")
    private Long branchIdFk;

    @Schema(description = "Full name (Arabic)")
    private String fullNameAr;

    @Schema(description = "Full name (English)")
    private String fullNameEn;

    @Schema(description = "Preferred language code")
    private String preferredLang;

    @Schema(description = "Employee ID (unconstrained — OQ-005, no HR module governed yet)")
    private Long employeeIdFk;

    @Schema(description = "Active status", example = "true")
    private Boolean isActiveFl;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Created by username")
    private String createdBy;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;

    @Schema(description = "Updated by username")
    private String updatedBy;
}
