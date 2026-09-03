package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for API-SEC-016 — a single LOV entry ({@code {code, labelAr, labelEn}}) resolved
 * at runtime for a SEC lookup key (LOV-SEC-001 SEC_PREFERRED_LANG / LOV-SEC-002 SEC_USER_STATUS).
 *
 * <p>This is a read-only projection over runtime code registries (QR-SEC-0022 — no lookup table,
 * no JPA entity), so it carries neither {@code id}, audit fields, nor validation annotations
 * (build-create-dto: request-only concern). It intentionally exposes BOTH labels rather than a
 * single locale-resolved label, because the client picks the display language — the service never
 * consults {@code LocaleContextHolder}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lookup (LOV) value entry - قيمة قائمة مرجعية")
public class LookupResponse {

    @Schema(description = "Stable code value - الرمز الثابت", example = "ACTIVE")
    private String code;

    @Schema(description = "Arabic display label - التسمية بالعربية", example = "نشط")
    private String labelAr;

    @Schema(description = "English display label - التسمية بالإنجليزية", example = "Active")
    private String labelEn;
}
