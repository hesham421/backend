package com.erp.security.crossmodule;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.exception.LocalizedException;
import com.erp.security.dto.SecUserProfileDto;
import com.erp.security.service.SecUserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Kept separate from {@link SecUserProfileService} so the cross-module contract surface stays
 * intentionally narrow and doesn't grow un-reviewed as that internal service evolves.
 */
@Service
@RequiredArgsConstructor
class SecUserProfileApiService implements SecUserProfileApi {

    private final SecUserProfileService secUserProfileService;

    /**
     * Pure read, no write on this side — {@code REQUIRED} propagation (the default) is safe
     * and simpler than {@code REQUIRES_NEW} for callers that consult this before writing
     * anything themselves.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<SecUserProfileView> findById(Long userId) {
        try {
            ServiceResult<SecUserProfileDto> result = secUserProfileService.getById(userId);
            SecUserProfileDto dto = result.getData();
            if (dto == null) {
                return Optional.empty();
            }
            return Optional.of(new SecUserProfileView(dto.getUserIdFk(), dto.getPreferredLang()));
        } catch (LocalizedException notFound) {
            return Optional.empty();
        } catch (AccessDeniedException accessDenied) {
            // Known, pre-existing gap (see SecUserProfileApi javadoc) — the calling principal
            // may lack USER_PROFILE_VIEW. Unchanged from today's REST behavior (a 403 was
            // already swallowed into a DEFAULT_LANGUAGE fallback by the caller); this
            // migration does not fix or worsen it.
            return Optional.empty();
        }
    }
}
