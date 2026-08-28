package com.erp.security.entity;

import com.erp.common.converter.BooleanNumberConverter;
import com.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * UI screen/page; auto-generates 4 CRUD permissions. Pages are the DETAIL in the RBAC model
 * (Roles are MASTER) — assigning a page to a role always includes VIEW.
 */
@Entity
@Table(name = "SEC_PAGES",
       uniqueConstraints = {
           @UniqueConstraint(name = "UK_PAGES_CODE", columnNames = {"PAGE_CODE"}),
           @UniqueConstraint(name = "UK_PAGES_ROUTE", columnNames = {"ROUTE"})
       },
       indexes = {
           @Index(name = "IDX_PAGES_MODULE", columnList = "MODULE"),
           @Index(name = "IDX_PAGES_ACTIVE", columnList = "IS_ACTIVE")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Page extends AuditableEntity {

    /**
     * PK constraint name (SEC_PAGES_PK) is set by 001_rename_pk_fk_to_standard.sql, not a
     * JPA annotation — Hibernate's naming-strategy hooks don't cover PRIMARY_KEY.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "page_seq")
    @SequenceGenerator(name = "page_seq", sequenceName = "SEC_PAGES_SEQ", allocationSize = 1)
    @Column(name = "SEC_PAGES_PK")
    private Long id;

    /** Unique page code (uppercase, e.g., USER, MENU, CONTRACT) */
    @Column(name = "PAGE_CODE", length = 50, nullable = false)
    private String pageCode;

    /** Arabic name for the page */
    @Column(name = "NAME_AR", length = 100, nullable = false)
    private String nameAr;

    /** English name for the page */
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    /** Angular route path (unique) */
    @Column(name = "ROUTE", length = 200, nullable = false)
    private String route;

    /** Icon class or name */
    @Column(name = "ICON", length = 50)
    private String icon;

    /** Module grouping (e.g., SECURITY, FINANCE, HR) */
    @Column(name = "MODULE", length = 50)
    private String module;

    /** Parent page ID for hierarchical structure */
    @Column(name = "PARENT_ID_FK")
    private Long parentId;

    /** Display order for sorting */
    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

    /** Active status */
    @Column(name = "IS_ACTIVE")
    @Convert(converter = BooleanNumberConverter.class)
    @Builder.Default
    private Boolean active = true;

    /** Optional description */
    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    /**
     * Not named isActive() deliberately — Hibernate would treat that as a boolean property
     * accessor and create a phantom 'ACTIVE' column mapping.
     */
    public Boolean getActiveStatus() {
        return active;
    }

    public void activate() {
        this.active = Boolean.TRUE;
    }

    public void deactivate() {
        this.active = Boolean.FALSE;
    }

    /** Business rules enforced before persist/update (RULE 24.8 safety net) */
    @PrePersist
    protected void onCreate() {
        if (active == null) {
            active = true;
        }
        if (pageCode != null) {
            pageCode = pageCode.toUpperCase().trim();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (pageCode != null) {
            pageCode = pageCode.toUpperCase().trim();
        }
    }
}
