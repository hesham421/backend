package com.erp.security.crossmodule;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.exception.LocalizedException;
import com.erp.security.dto.SecUserProfileDto;
import com.erp.security.service.SecUserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
     * {@code NOT_SUPPORTED}, not {@code REQUIRED}: this method catches the not-found/access-denied
     * exception from the nested {@link SecUserProfileService#getById} call and returns empty, but
     * under {@code REQUIRED} that nested call would already mark the *shared* transaction
     * rollback-only before the catch runs — poisoning any caller that later commits a write in the
     * same transaction with an {@code UnexpectedRollbackException}. {@code NOT_SUPPORTED} suspends
     * the caller's transaction so that can't happen.
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
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
