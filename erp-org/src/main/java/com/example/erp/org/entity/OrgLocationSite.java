package com.example.erp.org.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * JPA entity for ORG_LOCATION_SITE (ENTITY-ORG-007, DBF-0083..0094). Persistence-only —
 * creation guard (RULE-ORG-019) lives on {@link com.example.erp.org.domain.OrgLocationSiteDomain}.
 * No deactivation guard on this entity — RULE-ORG-005 is the Branch-side guard already enforced
 * by {@link com.example.erp.org.domain.OrgBranchDomain}; retained here only as a documentation
 * cross-link, not a new validation.
 */
@Entity
@Table(name = "ORG_LOCATION_SITE",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_LS_CODE_BR", columnNames = {"BRANCH_FK", "LOCATION_SITE_CODE"})
    },
    indexes = {
        @Index(name = "IDX_ORG_LS_BR_FK", columnList = "BRANCH_FK"),
        @Index(name = "IDX_ORG_LS_SITE_TYPE", columnList = "SITE_TYPE_ID"),
        @Index(name = "IDX_ORG_LS_IS_ACTIVE", columnList = "IS_ACTIVE_FL")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class OrgLocationSite extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_location_site_seq")
    @SequenceGenerator(name = "org_location_site_seq", sequenceName = "ORG_LOCATION_SITE_SEQ", allocationSize = 1)
    @Column(name = "LOCATION_SITE_PK")
    private Long id;

    // DBF-0084 — Business code LS-[BR_CODE]-NNNNN. NumberingEngine only (RULE-ORG-013). Immutable (RULE-ORG-011/014).
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "LOCATION_SITE_CODE", length = 20, nullable = false)
    private String locationSiteCode;

    // DBF-0085 — Unique within same Branch (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    // DBF-0086 — Unique within same Branch (RULE-ORG-015)
    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    // DBF-0087 — Creation blocked if parent Branch inactive (RULE-ORG-019)
    @NotNull(message = "{validation.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_LS_BR"))
    private OrgBranch branch;

    // DBF-0088 — LOV code string (LOV-ORG-006: OFFICE, WAREHOUSE, FACTORY, SITE, RETAIL)
    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "SITE_TYPE_ID", length = 50, nullable = false)
    private String siteTypeId;

    // DBF-0089
    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    // DBF-0090
    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) {
            isActiveFl = Boolean.TRUE;
        }
        if (locationSiteCode != null) {
            locationSiteCode = locationSiteCode.toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (locationSiteCode != null) {
            locationSiteCode = locationSiteCode.toUpperCase();
        }
    }

    public void activate() {
        this.isActiveFl = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActiveFl = Boolean.FALSE;
    }
}
