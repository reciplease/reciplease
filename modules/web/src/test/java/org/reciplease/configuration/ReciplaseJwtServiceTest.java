package org.reciplease.configuration;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class ReciplaseJwtServiceTest {

    private final ReciplaseJwtService jwtService = new ReciplaseJwtService("a-sufficiently-long-test-signing-secret");

    @Test
    void mintedTokenRoundTripsToTheSameUserIdAndHandle() {
        var token = jwtService.mint("user-1", "some-handle");

        var parsed = jwtService.parse(token);

        assertThat(parsed.isPresent(), is(true));
        assertThat(parsed.get().userId(), is("user-1"));
        assertThat(parsed.get().handle(), is("some-handle"));
    }

    @Test
    void mintedTokenRoundTripsWithANullHandle() {
        var token = jwtService.mint("user-1", null);

        var parsed = jwtService.parse(token);

        assertThat(parsed.isPresent(), is(true));
        assertThat(parsed.get().handle(), is(nullValue()));
    }

    @Test
    void parseReturnsEmptyForGarbageInput() {
        var parsed = jwtService.parse("not-a-real-jwt");

        assertThat(parsed.isPresent(), is(false));
    }

    @Test
    void parseReturnsEmptyForATokenSignedWithADifferentSecret() {
        var otherService = new ReciplaseJwtService("a-completely-different-test-signing-secret");
        var token = otherService.mint("user-1", "some-handle");

        var parsed = jwtService.parse(token);

        assertThat(parsed.isPresent(), is(false));
    }

    @Test
    void parseReturnsEmptyForAnExpiredToken() {
        var key = Keys.hmacShaKeyFor("a-sufficiently-long-test-signing-secret".getBytes(StandardCharsets.UTF_8));
        var now = Instant.now();
        var expired = Jwts.builder()
                .subject("user-1")
                .issuedAt(Date.from(now.minus(Duration.ofHours(48))))
                .expiration(Date.from(now.minus(Duration.ofHours(24))))
                .signWith(key)
                .compact();

        var parsed = jwtService.parse(expired);

        assertThat(parsed.isPresent(), is(false));
    }
}
