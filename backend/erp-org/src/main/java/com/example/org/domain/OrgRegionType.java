package com.example.org.domain;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ORG_REGION_TYPE",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_ORG_RT_CODE", columnNames = {"REGION_TYPE_CODE"})
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

    @NotBlank(message = "{validation.required}")
    @Size(max = 30, message = "{validation.size}")
    @Column(name = "REGION_TYPE_CODE", length = 30, nullable = false)
    private String regionTypeCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 200, nullable = false)
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 100, nullable = false)
    private String nameEn;

    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

    @OneToMany(mappedBy = "regionType", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrgRegion> regions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (isActiveFl == null) isActiveFl = Boolean.TRUE;
        if (regionTypeCode != null) regionTypeCode = regionTypeCode.toUpperCase();
    }

    @PreUpdate
    protected void onUpdate() {
        if (regionTypeCode != null) regionTypeCode = regionTypeCode.toUpperCase();
    }

    public void activate() { this.isActiveFl = Boolean.TRUE; }
    public void deactivate() { this.isActiveFl = Boolean.FALSE; }
}
