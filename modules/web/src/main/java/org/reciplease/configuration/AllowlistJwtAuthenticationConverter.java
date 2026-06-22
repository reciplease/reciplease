package org.reciplease.configuration;

import lombok.RequiredArgsConstructor;
import org.reciplease.repository.AllowlistRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Turns a validated Reciplease JWT into a Spring Security principal.
 * <p>
 * Spring Security has already verified the token (signature, expiry) by the time this runs,
 * so here we only decide <em>whether</em> the user is allowed in:
 * <ul>
 *     <li><b>Identity</b> is the {@code sub} claim &mdash; our own internally generated, stable
 *     user id (the principal name / {@code authentication.getName()}).</li>
 *     <li><b>Authorization</b> is keyed by that same user id: a match in the {@code allowlist}
 *     collection grants {@code ROLE_RECIPLEASE}.</li>
 * </ul>
 * User creation/linking happens only in {@code /api/auth/exchange}; this converter never writes
 * to the user collection.
 */
@Component
@Profile("cloud")
@RequiredArgsConstructor
public class AllowlistJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String ROLE_RECIPLEASE = "ROLE_RECIPLEASE";

    private final AllowlistRepository allowlistRepository;

    @Override
    public AbstractAuthenticationToken convert(final Jwt jwt) {
        final String userId = jwt.getSubject();
        final boolean allowlisted = userId != null && allowlistRepository.contains(userId);

        final List<GrantedAuthority> authorities = allowlisted
                ? List.of(new SimpleGrantedAuthority(ROLE_RECIPLEASE))
                : List.of();

        return new JwtAuthenticationToken(jwt, authorities, userId);
    }
}
