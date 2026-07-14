package com.example.erp.notification.entity;

import com.example.erp.common.converter.BooleanNumberConverter;
import com.example.erp.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
 * JPA entity for NOTIF_CHANNEL_CONFIG (ENTITY-NOTIF-003, DBF-0031..0038). PRIVATE
 * (Configuration) — no lifecycle, no Business Code. 5 seed rows (one per channel),
 * Read/Update only — no Create/Delete from the user (module-registry-notif.md
 * AUTO-DECISIONS). No domain/ package — per CORE.md ("Domain behavior: embedded in
 * Entity methods" — 3-entity module scale, File Service precedent).
 */
@Entity
@Table(name = "NOTIF_CHANNEL_CONFIG",
    uniqueConstraints = {
        @UniqueConstraint(name = "UQ_NOTIF_CHANNEL_CONFIG_TYPE", columnNames = {"CHANNEL_TYPE_ID"})
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class NotificationChannelConfig extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notif_channel_config_seq")
    @SequenceGenerator(name = "notif_channel_config_seq", sequenceName = "SEQ_NOTIF_CHANNEL_CONFIG", allocationSize = 1)
    @Column(name = "NOTIFICATION_CHANNEL_CONFIG_PK")
    private Long id;

    // FIELD-0032 — LOV-NOTIF-001 (NOTIFICATION_CHANNEL), unique per row — one config row per channel
    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Column(name = "CHANNEL_TYPE_ID", length = 20, nullable = false)
    private String channelTypeId;

    // FIELD-0033 — default true (all 5 channels enabled Phase 1 — final decision 2026-07-11)
    @Column(name = "IS_ENABLED_FL", nullable = false)
    @Builder.Default
    @Convert(converter = BooleanNumberConverter.class)
    private Boolean isEnabledFl = Boolean.TRUE;

    // FIELD-0034 — provider adapter config, consumed by the channel/ dispatch adapters (CORE.md).
    // TEXT → String + @Lob per CORE-8 type mapping.
    @Lob
    @Column(name = "CONFIG_JSON")
    private String configJson;
}
