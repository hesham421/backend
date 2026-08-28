package com.erp.common.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * Grants exactly {@link InternalCaller#AUTHORITY} and nothing else — deliberately does not
 * extend/reuse any authority a real JWT-authenticated principal can hold, so no HTTP request can
 * satisfy a {@code @PreAuthorize} check gated on it. Only {@link InternalCaller} constructs this.
 */
final class InternalCallerAuthentication extends AbstractAuthenticationToken {

    private static final String PRINCIPAL = "internal-trusted-caller";

    InternalCallerAuthentication() {
        super(AuthorityUtils.createAuthorityList(InternalCaller.AUTHORITY));
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return PRINCIPAL;
    }
}
