package org.reciplease.configuration;

import org.junit.jupiter.api.Test;
import org.reciplease.repository.AllowlistRepository;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AllowlistJwtAuthenticationConverterTest {

    private static final String SUBJECT = "user-1029384756";

    private final AllowlistRepository allowlistRepository = mock(AllowlistRepository.class);
    private final AllowlistJwtAuthenticationConverter converter =
            new AllowlistJwtAuthenticationConverter(allowlistRepository);

    private static Jwt jwt() {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(SUBJECT)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .claim("handle", "some-handle")
                .build();
    }

    @Test
    void grantsRoleForAllowlistedUser() {
        when(allowlistRepository.contains(SUBJECT)).thenReturn(true);

        final AbstractAuthenticationToken auth = converter.convert(jwt());

        assertThat(auth.getAuthorities().stream().map(Object::toString).toList(),
                contains("ROLE_RECIPLEASE"));
        assertThat(auth.getName(), is(SUBJECT));
    }

    @Test
    void deniesWhenUserNotOnAllowlist() {
        when(allowlistRepository.contains(SUBJECT)).thenReturn(false);

        final AbstractAuthenticationToken auth = converter.convert(jwt());

        assertThat(auth.getAuthorities(), is(emptyIterable()));
        assertThat(auth.getName(), is(SUBJECT));
    }

    @Test
    void deniesWithoutConsultingTheAllowlistWhenTheTokenHasNoSubject() {
        final var jwtWithoutSubject = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("not-sub", "irrelevant")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        final AbstractAuthenticationToken auth = converter.convert(jwtWithoutSubject);

        assertThat(auth.getAuthorities(), is(emptyIterable()));
    }
}
