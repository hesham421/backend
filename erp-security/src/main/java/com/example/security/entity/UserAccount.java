package com.example.security.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "USERS",
       uniqueConstraints = {@UniqueConstraint(name="UK_USERS_USERNAME", columnNames={"USERNAME"})},
       indexes = {
       @Index(name = "IDX_USERS_ENABLED", columnList = "ENABLED"),
       @Index(name = "IDX_USERS_USERNAME", columnList = "USERNAME")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class UserAccount extends AuditableEntity {

    /**
     * PK constraint name: USERS_PK (matches the column name below). Naming the
     * constraint itself isn't expressible via a JPA annotation on @Id (unlike
     * @ForeignKey for FKs) — Hibernate's naming-strategy hooks only cover
     * FOREIGN_KEY/UNIQUE_KEY/INDEX, never PRIMARY_KEY — so the constraint name
     * is enforced in the live DB by 001_rename_pk_fk_to_standard.sql instead.
     */
    @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "USERS_PK")
    private Long id;

    @Column(name = "USERNAME", nullable=false, length=80)
    private String username;

    // FIELD-SEC-0032 — nullable (pre-existing rows have no value); UK_USERS_EMAIL
    // added by 002_datascope_selfservice_auth_schema.sql per RULE-SEC-041.
    @Column(name = "EMAIL", length = 150)
    private String email;

    @Column(name = "PASSWORD", nullable=false, length=200)
    private String password;

    @Column(name = "ENABLED", nullable=false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean enabled = Boolean.TRUE;

    public boolean isEnabled() {
      return Boolean.TRUE.equals(enabled);
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "USER_ROLES",
      joinColumns = @JoinColumn(name="USER_ID_FK", referencedColumnName = "USERS_PK",
          foreignKey = @ForeignKey(name = "FK_UR_USER")),
      inverseJoinColumns = @JoinColumn(name="ROLE_ID_FK", referencedColumnName = "ROLES_PK",
          foreignKey = @ForeignKey(name = "FK_UR_ROLE")))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
