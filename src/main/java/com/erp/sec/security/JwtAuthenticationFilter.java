package com.erp.sec.security;

import com.erp.sec.entity.ActiveSession;
import com.erp.sec.entity.User;
import com.erp.sec.repository.ActiveSessionRepository;
import com.erp.sec.repository.UserRepository;
import com.erp.sec.service.MenuService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * REQ-SEC-033's single entry point: it authenticates the bearer token and installs the caller's
 * effective permission codes as authorities, so every {@code @PreAuthorize} downstream is a plain
 * set lookup. An absent or rejected token leaves the context anonymous — the authorization layer,
 * not this filter, decides what that means. See
 * governance/project-artifacts/sec-implementation-notes.md for the authority model.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    /** USER_STATUS code a caller must carry to authenticate (REQ-SEC-001, CHK_SEC_USER_STATUS). */
    private static final String STATUS_ACTIVE = "ACTIVE";

    /** DBF-SEC-050's shape, {@code PERM_<PAGE_CODE>_<ACTION_CODE>} — see {@code RegistryService}. */
    private static final String PERMISSION_PREFIX = "PERM_";

    /** {@code profile.conventions.security_model.gateway_action}, cited by RULE-SEC-007. */
    private static final String GATEWAY_ACTION_CODE = "VIEW";

    private final JwtTokenValidator tokenValidator;
    private final UserRepository userRepository;
    private final ActiveSessionRepository activeSessionRepository;
    private final MenuService menuService;

    /**
     * {@code @Lazy} keeps the JPA and method-security infrastructure out of the security-config
     * bootstrap: this filter is built while the filter chain is, long before those are ready.
     */
    public JwtAuthenticationFilter(JwtTokenValidator tokenValidator,
                                   @Lazy UserRepository userRepository,
                                   @Lazy ActiveSessionRepository activeSessionRepository,
                                   @Lazy MenuService menuService) {
        this.tokenValidator = tokenValidator;
        this.userRepository = userRepository;
        this.activeSessionRepository = activeSessionRepository;
        this.menuService = menuService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)
            && SecurityContextHolder.getContext().getAuthentication() == null) {
            tokenValidator.parse(header.substring(BEARER_PREFIX.length()))
                .ifPresent(this::authenticate);
        }
        chain.doFilter(request, response);
    }

    /**
     * REQ-SEC-028: the token's {@code jti} is the session's {@code tokenRef}, so a terminated or
     * unknown session makes the token unusable for every subsequent request, which is what
     * API-SEC-026 relies on. A non-ACTIVE or deactivated user is rejected for the same reason.
     */
    private void authenticate(Claims claims) {
        String username = claims.getSubject();
        String tokenRef = claims.getId();
        if (username == null || username.isBlank() || tokenRef == null || tokenRef.isBlank()) {
            return;
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !STATUS_ACTIVE.equals(user.getStatusCode())
            || !Boolean.TRUE.equals(user.getIsActiveFl())) {
            return;
        }

        ActiveSession session = activeSessionRepository.findByTokenRef(tokenRef).orElse(null);
        if (session == null || session.getTerminatedAt() != null) {
            log.debug("Rejecting a token whose session is unknown or terminated");
            return;
        }

        try {
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of()));
            Set<String> codes = menuService.effectivePermissionCodes().getData();
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, toAuthorities(codes)));
        } catch (RuntimeException e) {
            SecurityContextHolder.clearContext();
            log.warn("Failed to resolve the effective grants of an otherwise valid caller", e);
        }
    }

    /**
     * RULE-SEC-007 applied once for every endpoint: a non-VIEW permission counts only while its
     * screen's VIEW is also held. {@link InternalCallerContext#INTERNAL_AUTHORITY} is stripped here
     * so no request can ever acquire it, whatever a registry row might say.
     */
    private List<GrantedAuthority> toAuthorities(Set<String> codes) {
        Set<String> held = codes.stream()
            .filter(code -> !InternalCallerContext.INTERNAL_AUTHORITY.equals(code))
            .collect(Collectors.toUnmodifiableSet());
        return held.stream()
            .filter(code -> passesGateway(code, held))
            .map(code -> (GrantedAuthority) new SimpleGrantedAuthority(code))
            .toList();
    }

    /** A code that does not carry the registry's shape has no screen to gate on, so it is kept. */
    private boolean passesGateway(String code, Set<String> held) {
        int separator = code.lastIndexOf('_');
        if (!code.startsWith(PERMISSION_PREFIX) || separator <= PERMISSION_PREFIX.length()) {
            return true;
        }
        if (GATEWAY_ACTION_CODE.equals(code.substring(separator + 1))) {
            return true;
        }
        return held.contains(code.substring(0, separator + 1) + GATEWAY_ACTION_CODE);
    }
}
