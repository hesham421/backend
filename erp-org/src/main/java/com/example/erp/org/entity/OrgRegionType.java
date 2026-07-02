package com.example.erp.org.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
 * JPA entity for ORG_REGION_TYPE (ENTITY-ORG-008, DBF-0024..0032). PRIVATE reference table
 * (parent FK of ORG_REGION). Admin-only Create/Read/Update — no Deactivate API in this plan
 * (DRV-ORG-015). No RULE-IDs answer "is this operation allowed?" for this entity, so it has no
 * Domain companion object — see {@code create-entity} skill, "pure reference/lookup table".
 */
@Entity
@Table(name = "ORG_REGION_TYPE",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_ORG_RT_CODE", columnNames = {"REGION_TYPE_CODE"})
    },
    indexes = {
        @Index(name = "IDX_ORG_RT_IS_ACTIVE", columnList = "IS_ACTIVE_FL")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class OrgRegionType extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "org_region_type_seq")
    @SequenceGenerator(name = "org_region_type_seq", sequenceName = "ORG_REGION_TYPE_SEQ", allocationSize = 1)
    @Column(name = "REGION_TYPE_PK")
    private Long id;

    // DBF-0025 — Unique business code, e.g. GEOGRAPHIC, SALES, OPERATIONAL
    @NotBlank(message = "{validation.required}")
    @Size(max = 30, message = "{validation.size}")
    @Column(name = "REGION_TYPE_CODE", length = 30, nullable = false)
    private String regionTypeCode;

    // DBF-0026
    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    // DBF-0027
    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    // DBF-0028
    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) {
            isActiveFl = Boolean.TRUE;
        }
        if (regionTypeCode != null) {
            regionTypeCode = regionTypeCode.toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (regionTypeCode != null) {
            regionTypeCode = regionTypeCode.toUpperCase();
        }
    }

    public void activate() {
        this.isActiveFl = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActiveFl = Boolean.FALSE;
    }
}
