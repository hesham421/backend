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
@Table(name = "ORG_REGION",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_ORG_RG_CODE_LE", columnNames = {"LEGAL_ENTITY_FK", "REGION_CODE"})
    },
    indexes = {
        @Index(name = "IDX_ORG_RG_LE_FK", columnList = "LEGAL_ENTITY_FK"),
        @Index(name = "IDX_ORG_RG_RT_FK", columnList = "REGION_TYPE_ID_FK"),
        @Index(name = "IDX_ORG_RG_IS_ACTIVE", columnList = "IS_ACTIVE_FL")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class OrgRegion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_region_seq")
    @SequenceGenerator(name = "org_region_seq", sequenceName = "ORG_REGION_SEQ", allocationSize = 1)
    @Column(name = "REGION_PK")
    private Long id;

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "REGION_CODE", length = 20, nullable = false)
    private String regionCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    @NotNull(message = "{validation.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LEGAL_ENTITY_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_RG_LE"))
    private OrgLegalEntity legalEntity;

    @NotNull(message = "{validation.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REGION_TYPE_ID_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_RG_RT"))
    private OrgRegionType regionType;

    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

    // TODO: OQ-001 — RULE-ORG-006/017 deactivation guard (region→branch FK not in DBS) — pending resolution

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) isActiveFl = Boolean.TRUE;
    }

    public void activate() { this.isActiveFl = Boolean.TRUE; }
    public void deactivate() { this.isActiveFl = Boolean.FALSE; }
}
