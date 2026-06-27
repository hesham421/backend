package com.example.org.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

// DRV-ORG-001: regionTypeFk is stored as NUMBER FK to ORG_REGION_TYPE — NOT a DETAIL_CODE (VARCHAR2)
@Entity
@Table(name = "ORG_REGION",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_RG_CODE_LE", columnNames = {"REGION_CODE", "LEGAL_ENTITY_FK"})
    },
    indexes = {
        @Index(name = "IDX_ORG_RG_LEGAL_ENTITY", columnList = "LEGAL_ENTITY_FK"),
        @Index(name = "IDX_ORG_RG_REGION_TYPE", columnList = "REGION_TYPE_FK"),
        @Index(name = "IDX_ORG_RG_ACTIVE", columnList = "IS_ACTIVE_FL")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Region extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "region_seq")
    @SequenceGenerator(name = "region_seq", sequenceName = "SEQ_ORG_REGION", allocationSize = 1)
    @Column(name = "REGION_PK")
    private Long regionPk;

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "REGION_CODE", length = 20, nullable = false, updatable = false)
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
    private LegalEntity legalEntity;

    // DRV-ORG-001: FK to ORG_REGION_TYPE reference table — stored as NUMBER, not VARCHAR2 DETAIL_CODE
    @NotNull(message = "{validation.required}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REGION_TYPE_FK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ORG_RG_RT"))
    private RegionType regionType;

    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    @Size(max = 2000, message = "{validation.size}")
    @Column(name = "NOTES", length = 2000)
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) {
            isActiveFl = Boolean.TRUE;
        }
    }

    public void activate() {
        this.isActiveFl = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActiveFl = Boolean.FALSE;
    }
}
