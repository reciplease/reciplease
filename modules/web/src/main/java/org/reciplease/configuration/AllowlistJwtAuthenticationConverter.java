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
 * Turns a validated Google JWT into a Spring Security principal.
 * <p>
 * Spring Security has already verified the token (signature, issuer, audience, expiry) by the
 * time this runs, so here we only decide <em>who</em> the user is and <em>whether</em> they are
 * allowed in:
 * <ul>
 *     <li><b>Identity</b> is the {@code sub} claim &mdash; Google's stable, never-reused account
 *     id (the principal name / {@code authentication.getName()}).</li>
 *     <li><b>Authorization</b> is keyed by the {@code email} claim (which must be verified): a
 *     match in the {@code allowlist} collection grants {@code ROLE_RECIPLEASE}.</li>
 * </ul>
 * Email is used only as the human-administered allowlist key; {@code sub} is the identity we
 * would attach any per-user data to. Downstream code deals with a normal authenticated user
 * rather than raw JWT claims.
 */
@Component
@Profile("cloud")
@RequiredArgsConstructor
public class AllowlistJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String ROLE_RECIPLEASE = "ROLE_RECIPLEASE";

    private final AllowlistRepository allowlistRepository;

    @Override
    public AbstractAuthenticationToken convert(final Jwt jwt) {
        final String email = jwt.getClaimAsString("email");
        final boolean emailVerified = Boolean.TRUE.equals(jwt.getClaim("email_verified"));

        final List<GrantedAuthority> authorities = email != null
                && emailVerified
                && allowlistRepository.contains(email)
                ? List.of(new SimpleGrantedAuthority(ROLE_RECIPLEASE))
                : List.of();

        // Identity is the stable Google subject id; getClaimAsString("email") is only the gate key.
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}
