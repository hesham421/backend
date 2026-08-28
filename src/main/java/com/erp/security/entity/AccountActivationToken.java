package com.erp.security.entity;

import com.erp.common.converter.BooleanNumberConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Single-use self-registration activation token (usedFl flips true on consumption, no
 * soft-delete). Deliberately plain-audit-field style like {@link RefreshToken}, not
 * AuditableEntity — no CREATED_BY/UPDATED_AT/UPDATED_BY on this table.
 */
@Entity
@Table(name = "ACCOUNT_ACTIVATION_TOKEN",
    uniqueConstraints = {
        @UniqueConstraint(name = "UK_ACCOUNT_ACTIVATION_TOKEN_TOKEN", columnNames = {"TOKEN"})
    },
    indexes = {
        @Index(name = "IDX_ACCT_ACTIVATION_TOKEN_USER", columnList = "USER_ID_FK"),
        @Index(name = "IDX_ACCT_ACTIVATION_TOKEN_EXPIRES", columnList = "EXPIRES_AT")
    })
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_activation_token_seq")
    @SequenceGenerator(name = "account_activation_token_seq", sequenceName = "ACCOUNT_ACTIVATION_TOKEN_SEQ", allocationSize = 1)
    @Column(name = "TOKEN_PK")
    private Long tokenPk;

    @Column(name = "TOKEN", nullable = false, length = 64)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID_FK", referencedColumnName = "USERS_PK", nullable = false,
        foreignKey = @ForeignKey(name = "FK_ACCOUNT_ACTIVATION_TOKEN_USER"))
    private UserAccount user;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "EXPIRES_AT", nullable = false)
    private Instant expiresAt;

    @Builder.Default
    @Column(name = "USED_FL", nullable = false)
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean usedFl = Boolean.FALSE;

    public boolean isUsed() {
        return Boolean.TRUE.equals(usedFl);
    }

    public void setUsed(boolean used) {
        this.usedFl = used;
    }
}
