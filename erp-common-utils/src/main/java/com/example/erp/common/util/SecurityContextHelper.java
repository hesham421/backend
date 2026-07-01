package com.example.erp.common.util;

import com.example.erp.common.exception.CommonErrorCodes;
import com.example.erp.common.exception.LocalizedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Helper class for Security Context operations
 * Provides centralized access to current user, roles, and authorities
 *
 * @author ERP System
 * @version 1.0.0
 */
public final class SecurityContextHelper {

    private SecurityContextHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ========== Authentication Methods ==========

    /**
     * Get current Authentication object
     *
     * @return Optional of Authentication
     */
    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Get current Authentication or throw exception if not authenticated
     *
     * @return Authentication (never null)
     * @throws LocalizedException if not authenticated
     */
    public static Authentication requireAuthentication() {
        return getAuthentication()
                .orElseThrow(() -> new LocalizedException(HttpStatus.UNAUTHORIZED, "NOT_AUTHENTICATED"));
    }

    /**
     * Check if user is authenticated
     *
     * @return true if authentication exists and is authenticated
     */
    public static boolean isAuthenticated() {
        return getAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }

    // ========== Username Methods ==========

    /**
     * Get current authenticated username
     *
     * @return Optional of username
     */
    public static Optional<String> getUsername() {
        return getAuthentication()
                .map(Authentication::getName);
    }

    /**
     * Get current username or throw exception if not authenticated
     *
     * @return username (never null)
     * @throws LocalizedException if not authenticated
     */
    public static String requireUsername() {
        return getUsername()
                .orElseThrow(() -> new LocalizedException(HttpStatus.UNAUTHORIZED, "NOT_AUTHENTICATED"));
    }

    /**
     * Get current username or return default value
     *
     * @param defaultUsername default username if not authenticated
     * @return username or default
     */
    public static String getUsernameOrDefault(String defaultUsername) {
        return getUsername().orElse(defaultUsername);
    }

    /**
     * Get current username or return "system" as default
     * Useful for audit fields when no user is logged in
     *
     * @return username or "system"
     */
    public static String getUsernameOrSystem() {
        return getUsernameOrDefault("system");
    }

    // ========== Principal Methods ==========

    /**
     * Get authentication principal (usually UserDetails)
     *
     * @return Optional of principal object
     */
    public static Optional<Object> getPrincipal() {
        return getAuthentication()
                .map(Authentication::getPrincipal);
    }

    /**
     * Get principal casted to specific type
     *
     * @param <T> expected type
     * @param type class of expected type
     * @return Optional of typed principal
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getPrincipalAs(Class<T> type) {
        return getPrincipal()
                .filter(type::isInstance)
                .map(principal -> (T) principal);
    }

    // ========== Authority/Role Methods ==========

    /**
     * Get all granted authorities of current user
     *
     * @return Set of authority names (empty if not authenticated)
     */
    public static Set<String> getAuthorities() {
        return getAuthentication()
                .map(Authentication::getAuthorities)
                .stream()
                .flatMap(Collection::stream)
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    /**
     * Check if current user has specific authority/role
     *
     * @param authority authority name to check
     * @return true if user has the authority
     */
    public static boolean hasAuthority(String authority) {
        if (authority == null || authority.isBlank()) {
            return false;
        }
        return getAuthorities().contains(authority);
    }

    /**
     * Check if current user has any of the specified authorities
     *
     * @param authorities authorities to check
     * @return true if user has at least one authority
     */
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

    /**
     * Check if current user has all specified authorities
     *
     * @param authorities authorities to check
     * @return true if user has all authorities
     */
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
     * Require user to have specific authority or throw exception
     *
     * @param authority required authority
     * @throws LocalizedException if user doesn't have the authority
     */
    public static void requireAuthority(String authority) {
        if (!hasAuthority(authority)) {
            throw new LocalizedException(HttpStatus.FORBIDDEN, CommonErrorCodes.ACCESS_DENIED);
        }
    }
}
