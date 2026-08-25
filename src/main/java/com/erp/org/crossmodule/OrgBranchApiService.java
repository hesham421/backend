package com.erp.org.crossmodule;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.exception.LocalizedException;
import com.erp.org.dto.BranchResponse;
import com.erp.org.service.BranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Kept separate from {@link BranchService} so the cross-module contract surface stays
 * intentionally narrow and doesn't grow un-reviewed as that internal service evolves.
 */
@Service
@RequiredArgsConstructor
@Slf4j
class OrgBranchApiService implements OrgBranchApi {

    private final BranchService branchService;

    /**
     * Pure read, no write on this side — {@code REQUIRED} propagation (the default) is safe
     * and simpler than {@code REQUIRES_NEW} for callers that consult this before writing
     * anything themselves.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<OrgBranchView> findBranch(Long branchId) {
        try {
            ServiceResult<BranchResponse> result = branchService.getById(branchId);
            BranchResponse branch = result.getData();
            if (branch == null) {
                return Optional.empty();
            }
            return Optional.of(new OrgBranchView(branch.getId(), Boolean.TRUE.equals(branch.getIsActive())));
        } catch (LocalizedException notFound) {
            return Optional.empty();
        } catch (AccessDeniedException accessDenied) {
            // Known, pre-existing gap (see OrgBranchApi javadoc) — the calling principal may
            // lack BRANCH_VIEW. Unchanged from today's REST behavior (a 403 was already
            // treated as "not usable" here); this migration does not fix or worsen it.
            log.warn("Branch lookup for id {} denied by BRANCH_VIEW check — treating as unresolvable", branchId);
            return Optional.empty();
        }
    }
}
