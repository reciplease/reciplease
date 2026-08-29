package org.reciplease.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Mints and validates Reciplease's own HS256-signed JWTs. Reciplease is its own tiny
 * authorization server: rather than validating a provider's JWT directly (as we used to
 * for Google), {@code /api/auth/exchange} verifies a caller-supplied provider identity
 * out of band and hands back one of these tokens, which is what the rest of the API
 * actually authenticates against (see {@link CloudWebSecurityConfig}).
 */
@Component
public class ReciplaseJwtService {

    private final SecretKey signingKey;
    private final Duration accessTokenTtl;

    public ReciplaseJwtService(
            @Value("${reciplease.jwt.signing-secret}") final String signingSecret,
            @Value("${reciplease.jwt.access-token-ttl:PT20M}") final Duration accessTokenTtl) {
        this.signingKey = Keys.hmacShaKeyFor(signingSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = accessTokenTtl;
    }

    /**
     * Mints a fresh token identifying {@code userId}. The token carries only the user id — the
     * handle is mutable and authoritative only in the user record (read via {@code /api/me}), so
     * embedding it here just bakes in a stale copy that never refreshes.
     */
    public String mint(final String userId) {
        final var now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parses and validates {@code token}, returning the user id, or empty if the token is
     * malformed, unsigned, or expired.
     */
    public Optional<String> parse(final String token) {
        try {
            final Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (final JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
