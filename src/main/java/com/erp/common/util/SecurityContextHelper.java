package com.erp.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityContextHelper {

    private static final String SYSTEM_USER = "system";

    private SecurityContextHelper() {
        throw new UnsupportedOperationException("Utility class — cannot be instantiated");
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return SYSTEM_USER;
        }
        return authentication.getName();
    }
}
