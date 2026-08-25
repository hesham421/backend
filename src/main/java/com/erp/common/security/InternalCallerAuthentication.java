package com.erp.common.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

/**
 * Marks a same-JVM, non-HTTP call path as trusted without impersonating a real user or minting a
 * credential. Grants exactly one authority, {@link InternalCaller#AUTHORITY}, and nothing else —
 * it deliberately does NOT extend/reuse any authority a real JWT-authenticated principal can hold,
 * so no HTTP request, however it's authenticated, can ever satisfy
 * {@code @PreAuthorize("hasAuthority('} {@value InternalCaller#AUTHORITY} {@code ')")} on its own.
 * The only way to obtain this authority is for trusted in-process code to call
 * {@link InternalCaller#call}/{@link InternalCaller#run} — there is no filter, login endpoint, or
 * token format that produces it.
 *
 * <p>Not for use as a general "system user" — this exists specifically for the "in-process caller,
 * no HTTP principal, target method still needs a {@code @PreAuthorize} check to exist" shape (see
 * {@code create-service/SKILL.md}'s "Cross-Module Calls (XM)" section).
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
