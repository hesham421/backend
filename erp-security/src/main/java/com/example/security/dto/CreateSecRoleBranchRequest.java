package com.example.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO to create a SEC_ROLE_BRANCH assignment (API-SEC-036).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to assign a branch scope to a role - طلب إسناد فرع لدور")
public class CreateSecRoleBranchRequest {

    @NotNull(message = "{validation.required}")
    @Schema(description = "Role ID", example = "1", required = true)
    private Long roleIdFk;

    @NotNull(message = "{validation.required}")
    @Schema(description = "Branch ID", example = "1", required = true)
    private Long branchIdFk;

    // No @NotBlank here deliberately: blank/omitted values must surface the RULE-SEC-035
    // business error (SEC_ROLE_BRANCH_DATA_ACCESS_LEVEL_REQUIRED) via
    // SecRoleBranchService.assertValidDataAccessLevel(), not a generic bean-validation error.
    @Schema(description = "Data access level (LOV-SEC-002: BRANCH_ONLY / BRANCH_AND_CHILDREN / ALL)", example = "BRANCH_ONLY", required = true)
    private String dataAccessLevel;
}
