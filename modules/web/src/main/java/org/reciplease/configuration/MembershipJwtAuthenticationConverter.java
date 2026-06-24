package org.reciplease.configuration;

import lombok.RequiredArgsConstructor;
import org.reciplease.repository.HouseRepository;
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
 * Spring Security has already verified the token (signature, expiry) by the time this runs, so
 * here we only decide <em>whether</em> the user is allowed in:
 * <ul>
 *     <li><b>Identity</b> is the {@code sub} claim &mdash; our own internally generated, stable
 *     user id (the principal name / {@code authentication.getName()}).</li>
 *     <li><b>Authorization</b>: a user who belongs to at least one house gets
 *     {@code ROLE_RECIPLEASE}. House membership only happens by redeeming an invite, so it is
 *     itself the access grant — there is no separate allowlist.</li>
 * </ul>
 * User creation/linking happens only in {@code /api/auth/exchange}; this converter never writes.
 */
@Component
@Profile("cloud")
@RequiredArgsConstructor
public class MembershipJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String ROLE_RECIPLEASE = "ROLE_RECIPLEASE";

    private final HouseRepository houseRepository;

    @Override
    public AbstractAuthenticationToken convert(final Jwt jwt) {
        final String userId = jwt.getSubject();
        final boolean member = userId != null && !houseRepository.findAllForUser(userId).isEmpty();

        final List<GrantedAuthority> authorities = member
                ? List.of(new SimpleGrantedAuthority(ROLE_RECIPLEASE))
                : List.of();

        return new JwtAuthenticationToken(jwt, authorities, userId);
    }
}
