package com.example.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO to update a SEC_ROLE_BRANCH assignment (API-SEC-038). roleIdFk/branchIdFk are
 * immutable (composite PK, path variables) and not included here.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a role branch scope assignment - طلب تحديث نطاق الفرع للدور")
public class UpdateSecRoleBranchRequest {

    @NotBlank(message = "{validation.required}")
    @Schema(description = "Data access level (LOV-SEC-002: BRANCH_ONLY / BRANCH_AND_CHILDREN / ALL)", example = "BRANCH_ONLY")
    private String dataAccessLevel;
}
