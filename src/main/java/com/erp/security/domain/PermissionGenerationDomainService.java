package com.erp.security.domain;

import com.erp.common.domain.status.Status;
import com.erp.common.exception.LocalizedException;
import com.erp.security.entity.Page;
import com.erp.security.entity.Permission;
import com.erp.security.exception.SecErrorCodes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Domain service for RULE-SEC-011 (CORE-9): registering a Page auto-generates the four screen
 * permissions PERM_&lt;PAGE_CODE&gt;_{VIEW,CREATE,UPDATE,DELETE}. Pure decision/construction logic —
 * no Spring, no JPA behaviour, no repository. Given a persisted Page and the set of
 * permissionCodes already present (supplied by the service), it returns the four Permission
 * entities to persist, or throws when a generated code would collide. Spans Page → Permission,
 * so it is a domain service rather than an entity-bound Domain companion (contract A.0.7).
 * Permission names are deterministic non-null defaults; display labels resolve at runtime via
 * API-SEC-016 / CU i18n (SRS: no PERM_* name seed).
 */
public final class PermissionGenerationDomainService {

    // CORE-9 fixed permissionType convention (DB CHECK) — not a runtime LOV.
    public static final String TYPE_VIEW = "VIEW";
    public static final String TYPE_CREATE = "CREATE";
    public static final String TYPE_UPDATE = "UPDATE";
    public static final String TYPE_DELETE = "DELETE";

    private static final List<String> PERMISSION_TYPES = List.of(TYPE_VIEW, TYPE_CREATE, TYPE_UPDATE, TYPE_DELETE);

    // Physical column limits of SEC_PERMISSION.NAME_EN / NAME_AR (see Permission entity @Size and
    // V2 migration). The generated "<page name> - <TYPE>" label is truncated to fit so a long page
    // name can never overflow the permission columns and roll back the whole page-create tx.
    private static final int NAME_EN_MAX = 100;
    private static final int NAME_AR_MAX = 200;

    private PermissionGenerationDomainService() {
        throw new UnsupportedOperationException("Domain service — cannot be instantiated");
    }

    /**
     * RULE-SEC-011 — builds the four Permission entities for the given page. {@code page} must be
     * a persisted Page (non-null pageCode); {@code existingPermissionCodes} are the codes already
     * present, pre-fetched by the service to enforce RULE-SEC-010 (unique permissionCode).
     */
    public static List<Permission> generateForPage(Page page, Set<String> existingPermissionCodes) {
        if (page == null || page.getPageCode() == null || page.getPageCode().isBlank()) {
            throw new LocalizedException(Status.VALIDATION_ERROR, SecErrorCodes.PAGE_FIELDS_REQUIRED);
        }
        Set<String> existing = existingPermissionCodes == null ? Set.of() : existingPermissionCodes;
        String pageCode = page.getPageCode();
        List<Permission> generated = new ArrayList<>(PERMISSION_TYPES.size());
        for (String type : PERMISSION_TYPES) {
            String code = permissionCode(pageCode, type);
            if (existing.contains(code)) {
                throw new LocalizedException(Status.ALREADY_EXISTS, SecErrorCodes.PERMISSION_CODE_DUPLICATE, code);
            }
            generated.add(Permission.builder()
                .permissionCode(code)
                .permissionType(type)
                .nameAr(buildName(page.getNameAr(), type, NAME_AR_MAX))
                .nameEn(buildName(page.getNameEn(), type, NAME_EN_MAX))
                .isActive(Boolean.TRUE)
                .page(page)
                .build());
        }
        return generated;
    }

    /**
     * Builds the "&lt;page name&gt; - &lt;TYPE&gt;" permission label, truncating the page-name portion so the
     * result never exceeds {@code max} — the page name and permission name columns share the same
     * length, so a page name at its own limit would otherwise overflow once the " - TYPE" suffix is
     * appended and fail the insert.
     */
    private static String buildName(String pageName, String type, int max) {
        String suffix = " - " + type;
        String base = pageName == null ? "" : pageName;
        int budget = Math.max(0, max - suffix.length());
        if (base.length() > budget) {
            base = base.substring(0, budget);
        }
        return base + suffix;
    }

    /** CORE-9 code convention: PERM_&lt;PAGE_CODE&gt;_&lt;TYPE&gt;. */
    public static String permissionCode(String pageCode, String permissionType) {
        return "PERM_" + pageCode + "_" + permissionType;
    }
}
