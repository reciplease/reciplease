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

class ReciplaseJwtServiceTest {

    private final ReciplaseJwtService jwtService =
            new ReciplaseJwtService("a-sufficiently-long-test-signing-secret", Duration.ofHours(24));

    @Test
    void mintedTokenRoundTripsToTheSameUserId() {
        var token = jwtService.mint("user-1");

        var parsed = jwtService.parse(token);

        assertThat(parsed.isPresent(), is(true));
        assertThat(parsed.get(), is("user-1"));
    }

    @Test
    void parseReturnsEmptyForGarbageInput() {
        var parsed = jwtService.parse("not-a-real-jwt");

        assertThat(parsed.isPresent(), is(false));
    }

    @Test
    void parseReturnsEmptyForATokenSignedWithADifferentSecret() {
        var otherService = new ReciplaseJwtService("a-completely-different-test-signing-secret", Duration.ofHours(24));
        var token = otherService.mint("user-1");

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

    @Test
    void mintRespectsTheConfiguredAccessTokenTtl() {
        var shortLivedService = new ReciplaseJwtService("a-sufficiently-long-test-signing-secret", Duration.ofMinutes(20));
        var before = Instant.now();

        var token = shortLivedService.mint("user-1");

        var claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor("a-sufficiently-long-test-signing-secret".getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        var expiry = claims.getExpiration().toInstant();

        assertThat(expiry.isAfter(before.plus(Duration.ofMinutes(19))), is(true));
        assertThat(expiry.isBefore(before.plus(Duration.ofMinutes(21))), is(true));
    }
}
