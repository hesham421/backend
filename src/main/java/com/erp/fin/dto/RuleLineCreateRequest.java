package com.erp.fin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-FIN-011 request body (SVC-API-CRUD.md): {@code {accountDerivationTypeCode,
 * accountDerivationValue, amountSourceTypeCode, amountSourceValue?, directionCode,
 * distributionTypeCode, isRemainderFl}}. The owning {@code eventTypeRuleId} travels in the path.
 * Excludes {ruleLinePk, lineNo, createdAt} — {@code lineNo} (DBF-FIN-100) is assigned by the
 * service as the next position within the rule, never submitted.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Add a line to an event-type rule - إضافة سطر إلى قاعدة نوع الحدث")
public class RuleLineCreateRequest {

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Schema(description = "Account derivation type, ACCOUNT_DERIVATION_TYPE lookup "
        + "- نوع اشتقاق الحساب", example = "CONSTANT")
    private String accountDerivationTypeCode;

    @NotBlank(message = "{validation.required}")
    @Schema(description = "Account derivation value - قيمة اشتقاق الحساب", example = "1101")
    private String accountDerivationValue;

    @NotBlank(message = "{validation.required}")
    @Size(max = 20, message = "{validation.size}")
    @Schema(description = "Amount source type, AMOUNT_SOURCE_TYPE lookup - نوع مصدر المبلغ",
        example = "FIELD")
    private String amountSourceTypeCode;

    @Schema(description = "Amount source value - قيمة مصدر المبلغ", example = "netAmount")
    private String amountSourceValue;

    @NotBlank(message = "{validation.required}")
    @Size(max = 10, message = "{validation.size}")
    @Schema(description = "Direction, DEBIT_CREDIT lookup - الاتجاه", example = "DEBIT")
    private String directionCode;

    @NotBlank(message = "{validation.required}")
    @Size(max = 15, message = "{validation.size}")
    @Schema(description = "Distribution type, DISTRIBUTION_TYPE lookup - نوع التوزيع",
        example = "FIXED")
    private String distributionTypeCode;

    @Schema(description = "Carries the remainder of a percentage distribution - سطر الباقي",
        example = "false")
    @Builder.Default
    private Boolean isRemainderFl = Boolean.FALSE;
}
