package com.example.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO to create a SEC_USER_PROFILE (API-SEC-032).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a user profile / branch assignment - طلب إنشاء ملف تعريف مستخدم")
public class CreateSecUserProfileRequest {

    @NotNull(message = "{validation.required}")
    @Schema(description = "User ID to create the profile for (shared PK with USERS)", example = "1", required = true)
    private Long userIdFk;

    @NotNull(message = "{validation.required}")
    @Schema(description = "Active branch ID assigned to the user (RULE-SEC-034)", example = "1", required = true)
    private Long branchIdFk;

    @Size(max = 200, message = "{validation.size}")
    @Schema(description = "Full name (Arabic)")
    private String fullNameAr;

    @Size(max = 100, message = "{validation.size}")
    @Schema(description = "Full name (English)")
    private String fullNameEn;

    @Size(max = 10, message = "{validation.size}")
    @Schema(description = "Preferred language code")
    private String preferredLang;

    @Schema(description = "Employee ID (unconstrained — OQ-005, no HR module governed yet)")
    private Long employeeIdFk;
}
