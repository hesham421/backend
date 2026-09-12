package com.erp.fin.entity;

import com.erp.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * ENT-FIN-011 — RecurringTemplate (FIN_RECURRING_TEMPLATE). Source: db-script-fin.md §1
 * DBF-FIN-109..121 / §3 BLOCK 2, DATA-DOM-LOOKUP.md ENT-FIN-011.
 *
 * <p>No Domain companion: DATA-DOM-LOOKUP.md records "DOMAIN RULES: none scoped alone" — a
 * template's run posts through the same shared pipeline as any other entry (RULE-FIN-006..010,
 * decided by the JournalEntry domain built in a later sub), so A.0.7 forbids manufacturing a
 * Domain object here just to have one.
 *
 * <p>{@code scheduleTypeCode} and {@code frequencyCode} are lookup-backed (XM-FIN-001,
 * RECURRING_SCHEDULE_TYPE / RECURRING_FREQUENCY): plain VARCHAR codes validated against MDL in
 * the service layer. The db-script declares no unique constraint and no index on this table.
 *
 * <p>Caching is prohibited across FIN.
 */
@Entity
@Table(name = "FIN_RECURRING_TEMPLATE")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class RecurringTemplate extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fin_recurring_template_seq")
    @SequenceGenerator(name = "fin_recurring_template_seq",
        sequenceName = "SEQ_FIN_RECURRING_TEMPLATE", allocationSize = 1)
    @Column(name = "RECURRING_TEMPLATE_PK")
    private Long recurringTemplatePk;

    @NotBlank(message = "{validation.required}")
    @Size(max = 150, message = "{validation.size}")
    @Column(name = "NAME_AR", length = 150, nullable = false)
    private String nameAr;

    @NotBlank(message = "{validation.required}")
    @Size(max = 150, message = "{validation.size}")
    @Column(name = "NAME_EN", length = 150, nullable = false)
    private String nameEn;

    /** DBF-FIN-112 — lookup RECURRING_SCHEDULE_TYPE (XM-FIN-001). */
    @NotBlank(message = "{validation.required}")
    @Size(max = 15, message = "{validation.size}")
    @Column(name = "SCHEDULE_TYPE_CODE", length = 15, nullable = false)
    private String scheduleTypeCode;

    /** DBF-FIN-113 — lookup RECURRING_FREQUENCY (XM-FIN-001); nullable per the db-script. */
    @Size(max = 15, message = "{validation.size}")
    @Column(name = "FREQUENCY_CODE", length = 15)
    private String frequencyCode;

    @NotNull(message = "{validation.required}")
    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "{validation.required}")
    @Column(name = "NEXT_RUN_DATE", nullable = false)
    private LocalDate nextRunDate;

    /** DBF-FIN-116 — nullable per the db-script (an open-ended template). */
    @Column(name = "END_DATE")
    private LocalDate endDate;

    /** DBF-FIN-117 — native postgres BOOLEAN NOT NULL DEFAULT TRUE; no converter (A.1.6). */
    @Column(name = "IS_ACTIVE_FL", nullable = false)
    @Builder.Default
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
}
