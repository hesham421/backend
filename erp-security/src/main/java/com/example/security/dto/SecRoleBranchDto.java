package com.example.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * SEC_ROLE_BRANCH response DTO (ENTITY-SEC-010, DataScope).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role branch scope (DataScope) - نطاق الفرع للدور")
public class SecRoleBranchDto {

    @Schema(description = "Role ID (composite PK part 1)", example = "1")
    private Long roleIdFk;

    @Schema(description = "Branch ID (composite PK part 2)", example = "1")
    private Long branchIdFk;

    @Schema(description = "Data access level (LOV-SEC-002: BRANCH_ONLY / BRANCH_AND_CHILDREN / ALL)", example = "BRANCH_ONLY")
    private String dataAccessLevel;

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
