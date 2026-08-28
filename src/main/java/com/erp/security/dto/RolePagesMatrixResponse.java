package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Contract: role-access.contract.md - GET/PUT /api/roles/{roleId}/pages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role pages matrix response with all page assignments")
public class RolePagesMatrixResponse {

    @Schema(description = "Role ID", example = "1")
    private Long roleId;

    @Schema(description = "Role display name", example = "System Administrator")
    private String roleName;

    @Schema(description = "List of page assignments with permissions")
    private List<PageAssignmentResponse> assignments;
}
