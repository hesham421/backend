package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Role response DTO; deliberately excludes the entity's lazy-loaded collections.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role information")
public class RoleDto {

    @Schema(description = "Role ID", example = "1")
    private Long id;

    @Schema(description = "Unique role code (uppercase)", example = "ADMIN")
    private String roleCode;

    @Schema(description = "Role display name", example = "System Administrator")
    private String roleName;

    @Schema(description = "Role description", example = "Full system access role")
    private String description;

    @Schema(description = "Active status", example = "true")
    private Boolean active;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Created by username")
    private String createdBy;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;

    @Schema(description = "Updated by username")
    private String updatedBy;
}
