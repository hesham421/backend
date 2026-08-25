package com.erp.security.crossmodule;

import com.erp.common.search.Op;
import com.erp.common.search.SearchFilter;
import com.erp.common.search.SearchRequest;
import com.erp.common.domain.status.ServiceResult;
import com.erp.security.dto.UserDto;
import com.erp.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Kept separate from {@link UserService} so the cross-module contract surface stays
 * intentionally narrow and doesn't grow un-reviewed as that internal service evolves.
 */
@Service
@RequiredArgsConstructor
class SecurityUserApiService implements SecurityUserApi {

    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public Optional<SecurityUserView> findByUsername(String username) {
        return searchOne("username", username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SecurityUserView> findById(Long userId) {
        return searchOne("id", userId);
    }

    /**
     * Pure read, no write on this side — {@code REQUIRED} propagation (the default) is safe
     * and simpler than {@code REQUIRES_NEW} for callers that consult this before writing
     * anything themselves.
     */
    private Optional<SecurityUserView> searchOne(String field, Object value) {
        SearchRequest request = new SearchRequest();
        request.addFilter(new SearchFilter(field, Op.EQ, value));
        request.setPage(0);
        request.setSize(1);
        try {
            ServiceResult<Page<UserDto>> result = userService.searchUsers(request);
            Page<UserDto> page = result.getData();
            if (page == null || page.isEmpty()) {
                return Optional.empty();
            }
            UserDto dto = page.getContent().get(0);
            return Optional.of(new SecurityUserView(dto.id(), dto.username(), dto.email()));
        } catch (AccessDeniedException accessDenied) {
            // Known, pre-existing gap (see SecurityUserApi javadoc) — the calling principal
            // may lack USER_VIEW. Unchanged from today's REST behavior (a 403 was already
            // swallowed into Optional.empty() here); this migration does not fix or worsen it.
            return Optional.empty();
        }
    }
}
