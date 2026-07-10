package com.example.security.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.Persistable;

/**
 * SEC_USER_PROFILE (ENTITY-SEC-009) — 1:1 profile/branch-assignment extension of USERS.
 * Shared PK: USER_ID_FK is both the PK and the FK to USERS.USERS_PK — mapped via
 * {@code @MapsId} per execution-plan-SEC-gaps.md Section 3, not a separate surrogate PK.
 * branchIdFk stays a plain scalar (no JPA association to OrgBranch): erp-security has no
 * Maven dependency on erp-org, and RULE-SEC-034's active-branch check is a Service-layer
 * concern (Phase SVC+API) that consumes ORG_BRANCH over its REST API (XM-SEC-001), not a
 * shared JPA object graph. Referential integrity is enforced at the DB layer instead
 * (FK_SEC_USER_PROFILE_BRANCH in the migration script).
 *
 * Implements {@link Persistable} because {@code userIdFk} is manually assigned (never null,
 * even for a brand-new instance) — without this, Spring Data's default {@code isNew()} check
 * sees a non-null @Id and calls {@code merge()} instead of {@code persist()} on save(), which
 * throws {@code org.hibernate.AssertionFailure: null identifier} when merge cascades into the
 * transient {@code @MapsId} "user" association. {@code createdAt} (set only by
 * {@code AuditEntityListener}'s @PrePersist) is a reliable "not yet saved" signal.
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
