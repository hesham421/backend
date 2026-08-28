package com.erp.security.entity;

import com.erp.common.domain.AuditableEntity;
import com.erp.security.dto.PermissionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * A single RBAC permission. Page-scoped permissions link to a Page via PAGE_ID_FK (for FK
 * integrity and JOIN-based queries instead of string parsing); system permissions leave it null.
 */
@Entity
@Table(name = "PERMISSIONS",
       uniqueConstraints = {@UniqueConstraint(name="UK_PERMS_NAME", columnNames={"NAME"})},
       indexes = {
           @Index(name = "IDX_PERMS_NAME", columnList = "NAME"),
           @Index(name = "IDX_PERMS_PAGE_FK", columnList = "PAGE_ID_FK"),
           @Index(name = "IDX_PERMS_TYPE", columnList = "PERMISSION_TYPE")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Permission extends AuditableEntity {

    /**
     * PK constraint name (PERMISSIONS_PK) is set by 001_rename_pk_fk_to_standard.sql, not a
     * JPA annotation — Hibernate's naming-strategy hooks don't cover PRIMARY_KEY.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permissions_seq")
    @SequenceGenerator(name = "permissions_seq", sequenceName = "PERMISSIONS_SEQ", allocationSize = 1)
    @Column(name = "PERMISSIONS_PK")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name; // PERM_<PAGE_CODE>_<TYPE>

    /**
     * Direct link to the Page entity
     * Nullable for system permissions that are not page-related
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAGE_ID_FK", referencedColumnName = "SEC_PAGES_PK",
        foreignKey = @ForeignKey(name = "FK_PERMS_PAGE"))
    private Page page;

    /**
     * Permission type: VIEW, CREATE, UPDATE, DELETE
     * Stored separately for efficient queries without string parsing
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "PERMISSION_TYPE", length = 20)
    private PermissionType permissionType;

    /**
     * Check if this is a page-related permission
     */
    public boolean isPagePermission() {
        return page != null;
    }

    /**
     * Check if this is a VIEW permission
     */
    public boolean isViewPermission() {
        return permissionType == PermissionType.VIEW;
    }
}
