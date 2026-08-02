package org.reciplease.configuration;

import jakarta.servlet.http.HttpServletRequest;
import org.reciplease.service.ApiKeyGenerator;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.web.util.WebUtils;

/**
 * Resolves the Reciplease JWT from the {@code reciplease-session} cookie rather than the
 * {@code Authorization} header — the Next.js frontend now sends the token as a cookie (so its
 * own generic API proxy can forward it verbatim without decoding/re-encoding it as a header on
 * every request). Falls back to the standard {@code Authorization: Bearer} header so any caller
 * still using that convention (e.g. a frontend deploy mid-rollout) keeps working unchanged.
 * <p>
 * Never resolves an {@code rcpl_}-prefixed API key: those aren't JWTs, and {@link
 * ApiKeyAuthenticationFilter} — which runs earlier in the chain — is the only thing that should
 * ever attempt to decode one. Returning it here would otherwise reach the JWT decoder and 401 an
 * otherwise-valid key (or an already-authenticated request) as a malformed token.
 */
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final String COOKIE_NAME = "reciplease-session";

    private final BearerTokenResolver headerResolver = new DefaultBearerTokenResolver();

    @Override
    public String resolve(final HttpServletRequest request) {
        final var cookie = WebUtils.getCookie(request, COOKIE_NAME);
        final var token = cookie != null ? cookie.getValue() : headerResolver.resolve(request);
        return token != null && token.startsWith(ApiKeyGenerator.PREFIX) ? null : token;
    }
}
