package com.erp.security.entity;

import com.erp.common.converter.BooleanNumberConverter;
import com.erp.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * ENTITY-SEC-004 — Page (screen registry, CORE-9 owner). Source: db-script-SEC.md DBS-SEC-001,
 * DATA-DOM-RBAC.md. Each page belongs to a module (moduleFk, NOT NULL) — the basis of the
 * RULE-SEC-014 derivation. Persistence-only; decisions live in PageDomain / domain services.
 */
@Entity
@Table(name = "SEC_PAGE",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_SEC_PAGE_PAGE_CODE", columnNames = {"PAGE_CODE"})
    },
    indexes = {
        @Index(name = "IDX_SEC_PAGE_MODULE_FK", columnList = "MODULE_FK"),
        @Index(name = "IDX_SEC_PAGE_PARENT_PAGE_FK", columnList = "PARENT_PAGE_FK")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Page extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "page_seq")
    @SequenceGenerator(name = "page_seq", sequenceName = "SEQ_SEC_PAGE", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "PAGE_CODE", length = 100, nullable = false)
    private String pageCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MODULE_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_SEC_PAGE_MODULE"))
    private Module module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_PAGE_FK",
        foreignKey = @ForeignKey(name = "FK_SEC_PAGE_PARENT"))
    private Page parentPage;

    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActive = Boolean.TRUE;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = Boolean.TRUE;
        }
        if (pageCode != null) {
            pageCode = pageCode.toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (pageCode != null) {
            pageCode = pageCode.toUpperCase();
        }
    }

    public void activate() {
        this.isActive = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActive = Boolean.FALSE;
    }
}
