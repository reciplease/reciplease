package org.reciplease.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.reciplease.model.House;
import org.reciplease.repository.HouseRepository;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

class MembershipJwtAuthenticationConverterTest {

    private static final String SUBJECT = "user-1029384756";

    private final HouseRepository houseRepository = mock(HouseRepository.class);
    private final MembershipJwtAuthenticationConverter converter =
            new MembershipJwtAuthenticationConverter(houseRepository);

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
    void grantsRoleWhenTheUserBelongsToAtLeastOneHouse() {
        when(houseRepository.findAllForUser(SUBJECT)).thenReturn(List.of(new House("house-1", "Home", Instant.now())));

        final AbstractAuthenticationToken auth = converter.convert(jwt());

        assertThat(auth.getAuthorities().stream().map(Object::toString).toList(), contains("ROLE_RECIPLEASE"));
        assertThat(auth.getName(), is(SUBJECT));
    }

    @Test
    void deniesWhenTheUserBelongsToNoHouse() {
        when(houseRepository.findAllForUser(SUBJECT)).thenReturn(List.of());

        final AbstractAuthenticationToken auth = converter.convert(jwt());

        assertThat(auth.getAuthorities(), is(emptyIterable()));
        assertThat(auth.getName(), is(SUBJECT));
    }

    @Test
    void deniesWithoutQueryingMembershipWhenTheTokenHasNoSubject() {
        final var jwtWithoutSubject = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claim("not-sub", "irrelevant")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        final AbstractAuthenticationToken auth = converter.convert(jwtWithoutSubject);

        assertThat(auth.getAuthorities(), is(emptyIterable()));
        verifyNoInteractions(houseRepository);
    }
}
