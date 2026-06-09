package org.reciplease.configuration;

import org.junit.jupiter.api.Test;
import org.reciplease.model.User;
import org.reciplease.repository.AllowlistRepository;
import org.reciplease.repository.UserRepository;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AllowlistJwtAuthenticationConverterTest {

    private static final String EMAIL = "rhys.saldanha@gmail.com";
    private static final String SUBJECT = "1029384756";

    private final AllowlistRepository allowlistRepository = mock(AllowlistRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AllowlistJwtAuthenticationConverter converter =
            new AllowlistJwtAuthenticationConverter(allowlistRepository, userRepository);

    private static Jwt jwt(final Map<String, Object> claims) {
        final Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(SUBJECT)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600));
        claims.forEach(builder::claim);
        return builder.build();
    }

    @Test
    void grantsRoleForVerifiedAllowlistedEmail() {
        when(allowlistRepository.contains(EMAIL)).thenReturn(true);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final AbstractAuthenticationToken auth = converter.convert(
                jwt(Map.of("email", EMAIL, "email_verified", true)));

        assertThat(auth.getAuthorities().stream().map(Object::toString).toList(),
                contains("ROLE_RECIPLEASE"));
        assertThat(auth.getName(), is(SUBJECT));
        verify(userRepository).save(new User(SUBJECT, EMAIL));
    }

    @Test
    void deniesWhenEmailNotOnAllowlist() {
        when(allowlistRepository.contains(EMAIL)).thenReturn(false);

        final AbstractAuthenticationToken auth = converter.convert(
                jwt(Map.of("email", EMAIL, "email_verified", true)));

        assertThat(auth.getAuthorities(), is(emptyIterable()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deniesWhenEmailNotVerifiedEvenIfAllowlisted() {
        lenient().when(allowlistRepository.contains(EMAIL)).thenReturn(true);

        final AbstractAuthenticationToken auth = converter.convert(
                jwt(Map.of("email", EMAIL, "email_verified", false)));

        assertThat(auth.getAuthorities(), is(emptyIterable()));
    }

    @Test
    void deniesWhenEmailClaimMissing() {
        final AbstractAuthenticationToken auth = converter.convert(
                jwt(Map.of("email_verified", true)));

        assertThat(auth.getAuthorities(), is(emptyIterable()));
        assertThat(auth.getName(), is(SUBJECT));
    }
}
