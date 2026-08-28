package com.erp.security.entity;

import com.erp.common.converter.BooleanNumberConverter;
import com.erp.common.domain.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.HashSet;
import java.util.Set;

/**
 * System role; the MASTER side of the Role-Pages relationship (each Role can be assigned
 * multiple Pages with VIEW + optional CRUD permissions).
 */
@Entity
@Table(name = "ROLES",
       uniqueConstraints = {
           @UniqueConstraint(name = "UK_ROLES_NAME", columnNames = {"NAME"}),
           @UniqueConstraint(name = "UK_ROLES_ROLE_CODE", columnNames = {"ROLE_CODE"})
       },
       indexes = {
           @Index(name = "IDX_ROLES_IS_ACTIVE", columnList = "IS_ACTIVE")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Role extends AuditableEntity {

    /**
     * PK constraint name (ROLES_PK) is set by 001_rename_pk_fk_to_standard.sql, not a
     * JPA annotation — Hibernate's naming-strategy hooks don't cover PRIMARY_KEY.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roles_seq")
    @SequenceGenerator(name = "roles_seq", sequenceName = "ROLES_SEQ", allocationSize = 1)
    @Column(name = "ROLES_PK")
    private Long id;

    /** Role display name */
    @Column(name = "NAME", length = 60, nullable = false)
    private String roleName;

    /** Unique role code (uppercase, e.g. ADMIN) */
    @Column(name = "ROLE_CODE", length = 60, nullable = false)
    private String roleCode;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    /** Maps to IS_ACTIVE NUMBER(1) in the DB (1=active, 0=inactive). */
    @Column(name = "IS_ACTIVE", nullable = false)
    @Convert(converter = BooleanNumberConverter.class)
    @Builder.Default
    private Boolean active = Boolean.TRUE;

    @JsonIgnore  // Prevent lazy loading exception during JSON serialization
    @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = "ROLE_PERMISSIONS",
            joinColumns = @JoinColumn(name = "ROLE_ID_FK", referencedColumnName = "ROLES_PK",
                foreignKey = @ForeignKey(name = "FK_RP_ROLE")),
            inverseJoinColumns = @JoinColumn(name = "PERM_ID_FK", referencedColumnName = "PERMISSIONS_PK",
                foreignKey = @ForeignKey(name = "FK_RP_PERM")))
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    // Legacy compatibility - maps to roleCode for existing code
    @Deprecated
    public String getName() {
        return roleName;
    }

    @Deprecated
    public void setName(String name) {
        this.roleName = name;
    }

    /**
     * Not named isActive() deliberately — Hibernate would treat that as a boolean property
     * accessor and create a phantom 'ACTIVE' column mapping. Defaults to true when unset.
     */
    public Boolean getActiveStatus() {
        return active != null ? active : Boolean.TRUE;
    }

    /**
     * Activate this role
     */
    public void activate() {
        this.active = Boolean.TRUE;
    }

    /**
     * Deactivate this role
     */
    public void deactivate() {
        this.active = Boolean.FALSE;
    }
}
