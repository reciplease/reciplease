package org.reciplease.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.reciplease.service.ApiKeyGenerator;
import org.reciplease.service.ApiKeyService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates requests bearing a house service-account API key (see {@link ApiKeyService}),
 * recognised by its {@code rcpl_} prefix rather than the three-dot-separated shape of a JWT.
 * Runs before {@code BearerTokenAuthenticationFilter} in {@link CloudWebSecurityConfig} so a
 * successful match here short-circuits JWT decoding entirely; {@link CookieBearerTokenResolver}
 * additionally refuses to hand an {@code rcpl_} token to the JWT resolver at all, so an invalid
 * API key can't fall through and get misinterpreted as a malformed JWT.
 */
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_PREFIX = "Bearer ";

    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {
        final var token = resolveToken(request);
        if (token != null) {
            apiKeyService
                    .authenticate(token)
                    .ifPresent(principal -> SecurityContextHolder.getContext()
                            .setAuthentication(new ApiKeyAuthenticationToken(principal)));
        }
        filterChain.doFilter(request, response);
    }

    private static String resolveToken(final HttpServletRequest request) {
        final var header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(HEADER_PREFIX)) {
            return null;
        }
        final var token = header.substring(HEADER_PREFIX.length());
        return token.startsWith(ApiKeyGenerator.PREFIX) ? token : null;
    }
}
