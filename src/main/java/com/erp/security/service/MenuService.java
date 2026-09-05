package com.erp.security.service;

import com.erp.common.domain.status.ServiceResult;
import com.erp.common.util.SecurityContextHelper;
import com.erp.security.domain.PermissionGenerationDomainService;
import com.erp.security.dto.MenuNodeResponse;
import com.erp.security.entity.Page;
import com.erp.security.entity.Permission;
import com.erp.security.repository.PageRepository;
import com.erp.security.repository.PermissionRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestration for API-SEC-022 (self nested navigation menu tree). Resolves the caller from the
 * JWT principal (RULE-SEC-015) and builds a parentPageFk-nested tree of the ACTIVE Pages that
 * belong to the caller's granted ACTIVE Modules (QR-SEC-0032), each node carrying a computed
 * {@code viewGrantedFl} accessibility indicator (RULE-SEC-017/018). An empty result is valid —
 * {@code 200 []}, not an error.
 *
 * <p><b>No {@code @PreAuthorize} (justified deviation from build-create-service A.5.2).</b> Same
 * reasoning as {@link DashboardService}: self-scoped, keyed by the caller's own JWT principal,
 * cannot leak another user's data, and the path is absent from SecurityConfig's public allow-list
 * so the JWT filter already requires an authenticated principal (401 otherwise, RULE-SEC-015).
 *
 * <p><b>Tree-build kept as plain service orchestration, not a Domain object.</b> RULE-SEC-017/018
 * are a multi-row aggregation/shaping concern — "nest this row set and label the orphan branch" —
 * not an "is this operation allowed?" gate over a single entity (the Domain Delegation Rule in
 * build-create-service, and the Domain Companion criterion in build-create-entity, both key off
 * that question). There is no create/update/deactivate decision being guarded here; per
 * gov-enforce-backend-contract A.0.7 a Domain Service is warranted only when a rule genuinely
 * spans several entities AS A DECISION GATE — this is read-only shaping of an already-authorized
 * row set, so it stays in the service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final PageRepository pageRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public ServiceResult<List<MenuNodeResponse>> buildTree() {
        String username = SecurityContextHelper.getCurrentUsername();
        log.debug("Building navigation menu tree for user: {}", username);

        List<Page> candidatePages = pageRepository.findActivePagesForGrantedModulesByUsername(username);
        if (candidatePages.isEmpty()) {
            return ServiceResult.success(List.of());
        }

        Set<String> grantedPermissionCodes = permissionRepository
            .findGrantedActivePermissionsByUsername(username)
            .stream()
            .map(Permission::getPermissionCode)
            .collect(Collectors.toSet());

        Map<Long, Page> pageById = new HashMap<>();
        for (Page page : candidatePages) {
            pageById.put(page.getId(), page);
        }

        // RULE-SEC-017: a page is directly accessible when the caller holds its own VIEW permission.
        Set<Long> viewGrantedIds = new HashSet<>();
        for (Page page : candidatePages) {
            String viewCode = PermissionGenerationDomainService.permissionCode(
                page.getPageCode(), PermissionGenerationDomainService.TYPE_VIEW);
            if (grantedPermissionCodes.contains(viewCode)) {
                viewGrantedIds.add(page.getId());
            }
        }

        // RULE-SEC-018: a directly-granted page pulls its whole ancestor chain in as non-clickable
        // structural labels (viewGrantedFl=false) so it stays reachable in the tree, even when an
        // ancestor itself carries no VIEW grant. Ancestors outside the candidate set (i.e. not
        // belonging to a granted module) are not reachable and are simply not added.
        Set<Long> includedIds = new HashSet<>(viewGrantedIds);
        for (Long grantedId : viewGrantedIds) {
            Long parentId = parentIdOf(pageById.get(grantedId));
            while (parentId != null && pageById.containsKey(parentId) && includedIds.add(parentId)) {
                parentId = parentIdOf(pageById.get(parentId));
            }
        }

        Map<Long, MenuNodeResponse> nodeById = new HashMap<>();
        for (Page page : candidatePages) {
            if (!includedIds.contains(page.getId())) {
                continue;
            }
            nodeById.put(page.getId(), MenuNodeResponse.builder()
                .id(page.getId())
                .pageCode(page.getPageCode())
                .nameAr(page.getNameAr())
                .nameEn(page.getNameEn())
                .viewGrantedFl(viewGrantedIds.contains(page.getId()))
                .children(new ArrayList<>())
                .build());
        }

        List<MenuNodeResponse> roots = new ArrayList<>();
        for (Page page : candidatePages) {
            if (!includedIds.contains(page.getId())) {
                continue;
            }
            MenuNodeResponse node = nodeById.get(page.getId());
            Long parentId = parentIdOf(page);
            MenuNodeResponse parentNode = parentId != null ? nodeById.get(parentId) : null;
            if (parentNode != null) {
                parentNode.getChildren().add(node);
            } else {
                roots.add(node);
            }
        }

        sortTree(roots);

        return ServiceResult.success(roots);
    }

    private Long parentIdOf(Page page) {
        return page != null && page.getParentPage() != null ? page.getParentPage().getId() : null;
    }

    private void sortTree(List<MenuNodeResponse> nodes) {
        nodes.sort(Comparator.comparing(MenuNodeResponse::getNameEn, Comparator.nullsLast(Comparator.naturalOrder())));
        for (MenuNodeResponse node : nodes) {
            sortTree(node.getChildren());
        }
    }
}
