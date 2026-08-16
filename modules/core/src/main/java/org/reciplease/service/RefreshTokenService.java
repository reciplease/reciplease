package org.reciplease.service;

import org.reciplease.model.RefreshTokenRecord;
import org.reciplease.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Issues and rotates refresh tokens: a rotating, revocable, reuse-detected credential redeemed
 * via the {@code reciplease-refresh} cookie to mint a fresh short-lived access token (see
 * {@code AuthController}), mirroring {@link ApiKeyService}'s hashed-secret + prefix-lookup
 * pattern.
 * <p>
 * Every token belongs to a "family": the chain of tokens descended from one original login.
 * Rotating a token marks it used and mints its successor in the same family. Redeeming an
 * already-used token again ({@link ReuseDetected}) means the token was stolen and replayed, so
 * the whole family is revoked — both the legitimate holder and the attacker are locked out,
 * forcing a fresh login.
 */
@Service
public class RefreshTokenService {

    private static final int PREFIX_LENGTH = 12;

    private final RefreshTokenRepository repository;
    private final RefreshTokenGenerator generator;
    private final Clock clock;
    private final Duration refreshTokenTtl;

    public RefreshTokenService(
            final RefreshTokenRepository repository,
            final RefreshTokenGenerator generator,
            final Clock clock,
            @Value("${reciplease.jwt.refresh-token-ttl}") final Duration refreshTokenTtl) {
        this.repository = repository;
        this.generator = generator;
        this.clock = clock;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    /** Issues a brand-new refresh token, starting a fresh family. */
    public IssuedRefreshToken issue(final String userId) {
        return issueInFamily(userId, UUID.randomUUID().toString());
    }

    private IssuedRefreshToken issueInFamily(final String userId, final String familyId) {
        final var rawToken = generator.generate();
        final var now = clock.instant();
        final var expiresAt = now.plus(refreshTokenTtl);
        final var record = new RefreshTokenRecord(
                null, userId, familyId, prefixOf(rawToken), ApiKeyHasher.hash(rawToken), now, expiresAt, null, null);
        repository.save(record);
        return new IssuedRefreshToken(rawToken, expiresAt);
    }

    /**
     * Redeems {@code rawToken}: if valid and unused, marks it used and mints its successor in
     * the same family. If it's already been used, that's reuse — the whole family is revoked.
     */
    public RotateResult rotate(final String rawToken) {
        if (rawToken.length() < PREFIX_LENGTH) {
            return new Invalid();
        }
        final var candidateHash = ApiKeyHasher.hash(rawToken);
        final var record = repository.findByPrefix(prefixOf(rawToken)).orElse(null);
        if (record == null || record.expiresAt().isBefore(clock.instant()) || record.revokedAt() != null) {
            return new Invalid();
        }
        if (!constantTimeEquals(record.tokenHash(), candidateHash)) {
            return new Invalid();
        }
        if (record.usedAt() != null) {
            repository.revokeFamily(record.familyId(), clock.instant());
            return new ReuseDetected(record.userId());
        }

        repository.markUsed(record.id(), clock.instant());
        final var issued = issueInFamily(record.userId(), record.familyId());
        return new Rotated(record.userId(), issued.rawToken(), issued.expiresAt());
    }

    /** Revokes every refresh token belonging to {@code userId}, across every family. */
    public void revokeAllForUser(final String userId) {
        repository.revokeAllForUser(userId, clock.instant());
    }

    /**
     * Revokes every refresh token belonging to the user {@code rawToken} was issued to. Used by
     * logout — deliberately does not call {@link #rotate}, which has different semantics (mints
     * a successor, detects reuse) that don't apply here.
     */
    public boolean revokeByRawToken(final String rawToken) {
        if (rawToken.length() < PREFIX_LENGTH) {
            return false;
        }
        final var candidateHash = ApiKeyHasher.hash(rawToken);
        final Optional<RefreshTokenRecord> found = repository.findByPrefix(prefixOf(rawToken))
                .filter(record -> constantTimeEquals(record.tokenHash(), candidateHash));
        found.ifPresent(record -> revokeAllForUser(record.userId()));
        return found.isPresent();
    }

    private static String prefixOf(final String rawToken) {
        return rawToken.substring(0, PREFIX_LENGTH);
    }

    private static boolean constantTimeEquals(final String a, final String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    public record IssuedRefreshToken(String rawToken, Instant expiresAt) {
    }

    public sealed interface RotateResult permits Rotated, Invalid, ReuseDetected {
    }

    public record Rotated(String userId, String rawToken, Instant expiresAt) implements RotateResult {
    }

    public record Invalid() implements RotateResult {
    }

    public record ReuseDetected(String userId) implements RotateResult {
    }
}
