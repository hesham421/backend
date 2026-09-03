package com.erp.security.entity;

import com.erp.common.converter.BooleanNumberConverter;
import com.erp.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * ENTITY-SEC-001 — UserAccount (SHARED identity entity, owner SEC).
 * Source: db-script-SEC.md DBS-SEC-001, DATA-DOM-IDENTITY.md ENTITY-SEC-001.
 * Persistence-only; every "is this operation allowed?" decision lives in UserAccountDomain.
 */
@Entity
@Table(name = "SEC_USER_ACCOUNT",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_SEC_USER_ACCOUNT_USERNAME", columnNames = {"USERNAME"}),
        @UniqueConstraint(name = "UQ_SEC_USER_ACCOUNT_EMAIL", columnNames = {"EMAIL"})
    },
    indexes = {
        @Index(name = "IDX_SEC_USER_ACCOUNT_STATUS", columnList = "USER_STATUS_ID")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class UserAccount extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_account_seq")
    @SequenceGenerator(name = "user_account_seq", sequenceName = "SEQ_SEC_USER_ACCOUNT", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "{validation.required}")
    @Size(max = 100, message = "{validation.size}")
    @Column(name = "USERNAME", length = 100, nullable = false)
    private String username;

    // System-managed (RULE-SEC-004): only a hash is ever stored; set via activation/reset flow.
    @Size(max = 255, message = "{validation.size}")
    @Column(name = "PASSWORD_HASH", length = 255, nullable = false)
    private String passwordHash;

    @NotBlank(message = "{validation.required}")
    @Size(max = 255, message = "{validation.size}")
    @Column(name = "EMAIL", length = 255, nullable = false)
    private String email;

    @Size(max = 30, message = "{validation.size}")
    @Column(name = "PHONE", length = 30)
    private String phone;

    @NotBlank(message = "{validation.required}")
    @Size(max = 200, message = "{validation.size}")
    @Column(name = "FULL_NAME", length = 200, nullable = false)
    private String fullName;

    @NotBlank(message = "{validation.required}")
    @Size(max = 10, message = "{validation.size}")
    @Column(name = "PREFERRED_LANG_ID", length = 10, nullable = false)
    private String preferredLangId;

    @NotBlank(message = "{validation.required}")
    @Size(max = 50, message = "{validation.size}")
    @Column(name = "USER_STATUS_ID", length = 50, nullable = false)
    private String userStatusId;

    @Column(name = "FAILED_LOGIN_COUNT", nullable = false)
    @Builder.Default
    private Short failedLoginCount = (short) 0;

    @Column(name = "LOCKED_UNTIL")
    private LocalDateTime lockedUntil;

    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isActive = Boolean.TRUE;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = Boolean.TRUE;
        }
        if (failedLoginCount == null) {
            failedLoginCount = (short) 0;
        }
    }

    public void activate() {
        this.isActive = Boolean.TRUE;
    }

    public void deactivate() {
        this.isActive = Boolean.FALSE;
    }
}
