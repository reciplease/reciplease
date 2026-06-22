package org.reciplease.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Mints and validates Reciplease's own HS256-signed JWTs. Reciplease is its own tiny
 * authorization server: rather than validating a provider's JWT directly (as we used to
 * for Google), {@code /api/auth/exchange} verifies a caller-supplied provider identity
 * out of band and hands back one of these tokens, which is what the rest of the API
 * actually authenticates against (see {@link CloudWebSecurityConfig}).
 */
@Component
public class ReciplaseJwtService {

    private static final String HANDLE_CLAIM = "handle";
    private static final Duration EXPIRY = Duration.ofHours(24);

    private final SecretKey signingKey;

    public ReciplaseJwtService(@Value("${reciplease.jwt.signing-secret}") final String signingSecret) {
        this.signingKey = Keys.hmacShaKeyFor(signingSecret.getBytes(StandardCharsets.UTF_8));
    }

    /** Mints a fresh token for {@code userId}, embedding {@code handle} (may be {@code null}). */
    public String mint(final String userId, final String handle) {
        final var now = Instant.now();
        return Jwts.builder()
                .subject(userId)
                .claim(HANDLE_CLAIM, handle)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(EXPIRY)))
                .signWith(signingKey)
                .compact();
    }

    /** Parses and validates {@code token}, returning empty if it is malformed, unsigned, or expired. */
    public Optional<ParsedToken> parse(final String token) {
        try {
            final Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new ParsedToken(claims.getSubject(), claims.get(HANDLE_CLAIM, String.class)));
        } catch (final JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public record ParsedToken(String userId, String handle) {}
}
