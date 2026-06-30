package com.example.org.domain;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "ORG_LOCATION_SITE",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_ORG_LS_CODE_BR", columnNames = {"BRANCH_FK", "LOCATION_SITE_CODE"})
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

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "LOCATION_SITE_CODE", length = 20, nullable = false)
    private String locationSiteCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    // RULE-ORG-019: parent Branch must be active at create time
    @NotNull(message = "{validation.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_LS_BR"))
    private OrgBranch branch;

    // LOV-ORG-006: OFFICE, WAREHOUSE, FACTORY, SITE, RETAIL
    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "SITE_TYPE_ID", length = 50, nullable = false)
    private String siteTypeId;

    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) isActiveFl = Boolean.TRUE;
    }

    public void activate() { this.isActiveFl = Boolean.TRUE; }
    public void deactivate() { this.isActiveFl = Boolean.FALSE; }
}
