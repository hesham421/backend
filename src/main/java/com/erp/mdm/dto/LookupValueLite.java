package com.erp.mdm.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lean read projection for API-MDM-011 (platform-wide lookup consumption). A Spring Data
 * interface-based projection — NOT a JPA entity or a full response DTO. Carries only the four
 * fields a consuming module needs ({@code valueCode}, bilingual names, {@code sortOrder}); it
 * deliberately excludes the PK, active flag and audit columns, since consumers store only the
 * code per the srs-MDM §A2/A7 SOFT-reference pattern. Populated by
 * {@code LookupValueRepository.findActiveByTypeCode} — its getter names MUST match the query
 * aliases.
 */
@Schema(description = "Lean active lookup value - قيمة مرجعية نشطة مختصرة")
public interface LookupValueLite {

    @Schema(description = "Value code (natural key within its type) - رمز القيمة", example = "SAR")
    String getValueCode();

    @Schema(description = "Arabic display name - الاسم بالعربية", example = "ريال سعودي")
    String getNameAr();

    @Schema(description = "English display name - الاسم بالإنجليزية", example = "Saudi Riyal")
    String getNameEn();

    @Schema(description = "Display sort order - ترتيب العرض", example = "10")
    Short getSortOrder();
}
