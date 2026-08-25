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
     * {@code NOT_SUPPORTED}, not the default {@code REQUIRED}: this read can throw (via
     * {@link SecUserProfileService#getById}'s not-found {@code orElseThrow}), and this method
     * deliberately catches that exception and returns {@link Optional#empty()} instead of
     * propagating it. Under {@code REQUIRED} that's not actually safe — Spring's transactional
     * advice for the nested {@code getById} call marks the *shared* transaction rollback-only
     * the moment the exception crosses its proxy boundary, regardless of it being caught here
     * afterward. A caller that writes nothing itself never notices; a caller that later commits
     * a write in the same transaction (e.g. {@code NotificationEventProcessor.process}) gets an
     * {@code UnexpectedRollbackException} on commit even though this method "handled" the
     * not-found case. {@code NOT_SUPPORTED} suspends any caller transaction for the duration of
     * this call so a not-found/access-denied result here can never poison the caller's own
     * transaction.
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
