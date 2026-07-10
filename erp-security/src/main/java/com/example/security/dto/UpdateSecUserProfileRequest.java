package com.example.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO to update a SEC_USER_PROFILE (API-SEC-034). userIdFk is immutable
 * (path variable, shared PK with USERS) and not included here.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a user profile - طلب تحديث ملف تعريف مستخدم")
public class UpdateSecUserProfileRequest {

    @NotNull(message = "{validation.required}")
    @Schema(description = "Active branch ID assigned to the user (RULE-SEC-034)", example = "1")
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
