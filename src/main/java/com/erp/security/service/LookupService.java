package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.domain.UserAccountDomain;
import com.erp.security.dto.LookupResponse;
import com.erp.security.exception.SecErrorCodes;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for API-SEC-016 (Lookups / LOV values). Resolves a {@code lookupKey} to its LOV
 * entries from an in-code, immutable bilingual registry (QR-SEC-0022 — runtime codes, no lookup
 * table, no JPA entity/repository). The SEC_USER_STATUS codes are sourced from
 * {@link UserAccountDomain}'s status constants so the LOV can never drift from the lifecycle
 * state machine that enforces them (RULE-SEC-012).
 *
 * <p><b>No {@code @PreAuthorize} (justified deviation from build-create-service A.5.2).</b> The
 * SVC-API-LOOKUP spec classes this endpoint as "authenticated" with no specific CORE-9 authority.
 * Its path is deliberately absent from SecurityConfig's public allow-list, so the JWT filter
 * already requires an authenticated principal before the method is reached; adding a
 * {@code hasAuthority(...)} check would invent an authority the contract does not define. This
 * mirrors the same reasoning AuthController documents for its plain-authenticated logout endpoint.
 *
 * <p>Both labels are returned regardless of the caller's locale, so this method never consults
 * {@code LocaleContextHolder}. {@code @Transactional(readOnly = true)} is retained for annotation
 * consistency with the other read services even though no database is touched.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LookupService {

    /** LOV-SEC-001 — preferred UI language codes. */
    private static final String LOOKUP_PREFERRED_LANG = "SEC_PREFERRED_LANG";

    /** LOV-SEC-002 — user account lifecycle status codes. */
    private static final String LOOKUP_USER_STATUS = "SEC_USER_STATUS";

    /**
     * Immutable bilingual LOV registry keyed by lookupKey. Values are unmodifiable lists of
     * {@link LookupResponse}. SEC_USER_STATUS reuses {@link UserAccountDomain} code constants so
     * the codes never drift from the domain state machine.
     */
    private static final Map<String, List<LookupResponse>> REGISTRY = Map.of(
        LOOKUP_PREFERRED_LANG, List.of(
            new LookupResponse("AR", "العربية", "Arabic"),
            new LookupResponse("EN", "الإنجليزية", "English")
        ),
        LOOKUP_USER_STATUS, List.of(
            new LookupResponse(UserAccountDomain.STATUS_PENDING_ACTIVATION, "بانتظار التفعيل", "Pending activation"),
            new LookupResponse(UserAccountDomain.STATUS_ACTIVE, "نشط", "Active"),
            new LookupResponse(UserAccountDomain.STATUS_INACTIVE, "غير نشط", "Inactive")
        )
    );

    /**
     * Resolves the LOV entries for {@code lookupKey}. Unknown key → ERR-0012 (Resource not found,
     * HTTP 404) via {@link LocalizedException}.
     */
    @Transactional(readOnly = true)
    public ServiceResult<List<LookupResponse>> get(String lookupKey) {
        log.debug("Resolving SEC lookup key: {}", lookupKey);

        List<LookupResponse> entries = REGISTRY.get(lookupKey);
        if (entries == null) {
            throw new LocalizedException(Status.NOT_FOUND, SecErrorCodes.LOOKUP_KEY_NOT_FOUND, lookupKey);
        }

        return ServiceResult.success(entries);
    }
}
