package com.erp.security.entity;

import com.erp.common.converter.BooleanNumberConverter;
import com.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;

/**
 * 1:1 profile/branch-assignment extension of USERS with a shared PK (USER_ID_FK is both PK
 * and FK, via {@code @MapsId}). branchIdFk is a plain scalar, not a JPA association — erp-security
 * has no Maven dependency on erp-org, so the active-branch check goes through ORG_BRANCH's REST API.
 *
 * <p>Implements {@link Persistable} because {@code userIdFk} is manually assigned and never
 * null: without it, Spring Data's default {@code isNew()} sees a non-null @Id and calls
 * {@code merge()} instead of {@code persist()}, which throws {@code AssertionFailure: null
 * identifier} on the transient {@code @MapsId} association. {@code createdAt} is the "not yet
 * saved" signal instead.
 */
@Entity
@Table(name = "SEC_USER_PROFILE",
    indexes = {
        @Index(name = "IDX_SEC_USER_PROFILE_BRANCH", columnList = "BRANCH_ID_FK"),
        @Index(name = "IDX_SEC_USER_PROFILE_EMPLOYEE", columnList = "EMPLOYEE_ID_FK")
    })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class SecUserProfile extends AuditableEntity implements Persistable<Long> {

    @Id
    @Column(name = "USER_ID_FK")
    private Long userIdFk;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "USER_ID_FK", foreignKey = @ForeignKey(name = "FK_SEC_USER_PROFILE_USER"))
    private UserAccount user;

    @Column(name = "BRANCH_ID_FK", nullable = false)
    private Long branchIdFk;

    @Column(name = "FULL_NAME_AR", length = 200)
    private String fullNameAr;

    @Column(name = "FULL_NAME_EN", length = 100)
    private String fullNameEn;

    // OQ-004 — no LOV domain governed yet for preferred language; plain nullable VARCHAR(10).
    @Column(name = "PREFERRED_LANG", length = 10)
    private String preferredLang;

    // OQ-005 — no HR module governed yet; unconstrained, deliberately no @JoinColumn FK.
    @Column(name = "EMPLOYEE_ID_FK")
    private Long employeeIdFk;

    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActiveFl = Boolean.TRUE;

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

    @Override
    public Long getId() {
        return userIdFk;
    }

    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }
}
