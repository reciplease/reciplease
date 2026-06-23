package org.reciplease.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.web.util.WebUtils;

/**
 * Resolves the Reciplease JWT from the {@code reciplease-session} cookie rather than the
 * {@code Authorization} header — the Next.js frontend now sends the token as a cookie (so its
 * own generic API proxy can forward it verbatim without decoding/re-encoding it as a header on
 * every request). Falls back to the standard {@code Authorization: Bearer} header so any caller
 * still using that convention (e.g. a frontend deploy mid-rollout) keeps working unchanged.
 */
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final String COOKIE_NAME = "reciplease-session";

    private final BearerTokenResolver headerResolver = new DefaultBearerTokenResolver();

    @Override
    public String resolve(final HttpServletRequest request) {
        final var cookie = WebUtils.getCookie(request, COOKIE_NAME);
        return cookie != null ? cookie.getValue() : headerResolver.resolve(request);
    }
}
