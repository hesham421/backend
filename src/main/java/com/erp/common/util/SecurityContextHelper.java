package com.erp.common.util;

import com.erp.common.exception.CommonErrorCodes;
import com.erp.common.exception.LocalizedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Centralized access to the current user, roles, and authorities via Spring's SecurityContext.
 */
public final class SecurityContextHelper {

    private SecurityContextHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * @throws LocalizedException if not authenticated
     */
    public static Authentication requireAuthentication() {
        return getAuthentication()
                .orElseThrow(() -> new LocalizedException(HttpStatus.UNAUTHORIZED, "NOT_AUTHENTICATED"));
    }

    public static boolean isAuthenticated() {
        return getAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }

    public static Optional<String> getUsername() {
        return getAuthentication()
                .map(Authentication::getName);
    }

    /**
     * @throws LocalizedException if not authenticated
     */
    public static String requireUsername() {
        return getUsername()
                .orElseThrow(() -> new LocalizedException(HttpStatus.UNAUTHORIZED, "NOT_AUTHENTICATED"));
    }

    public static String getUsernameOrDefault(String defaultUsername) {
        return getUsername().orElse(defaultUsername);
    }

    /**
     * Useful for audit fields when no user is logged in.
     */
    public static String getUsernameOrSystem() {
        return getUsernameOrDefault("system");
    }

    public static Optional<Object> getPrincipal() {
        return getAuthentication()
                .map(Authentication::getPrincipal);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getPrincipalAs(Class<T> type) {
        return getPrincipal()
                .filter(type::isInstance)
                .map(principal -> (T) principal);
    }

    public static Set<String> getAuthorities() {
        return getAuthentication()
                .map(Authentication::getAuthorities)
                .stream()
                .flatMap(Collection::stream)
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    public static boolean hasAuthority(String authority) {
        if (authority == null || authority.isBlank()) {
            return false;
        }
        return getAuthorities().contains(authority);
    }

    public static boolean hasAnyAuthority(String... authorities) {
        if (authorities == null || authorities.length == 0) {
            return false;
        }
        Set<String> userAuthorities = getAuthorities();
        for (String authority : authorities) {
            if (userAuthorities.contains(authority)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAllAuthorities(String... authorities) {
        if (authorities == null || authorities.length == 0) {
            return true;
        }
        Set<String> userAuthorities = getAuthorities();
        for (String authority : authorities) {
            if (!userAuthorities.contains(authority)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @throws LocalizedException if user doesn't have the authority
     */
    public static void requireAuthority(String authority) {
        if (!hasAuthority(authority)) {
            throw new LocalizedException(HttpStatus.FORBIDDEN, CommonErrorCodes.ACCESS_DENIED);
        }
    }
}
