package org.reciplease.model;

import java.time.Instant;

/**
 * A rotating, revocable refresh token. {@code tokenHash} is the SHA-256 digest of the raw token
 * — the raw value itself is never persisted, only returned once at issuance. {@code tokenPrefix}
 * is a short, non-secret slice of the raw token used to look up the matching record without
 * scanning every stored hash (same reasoning as {@link ApiKey#keyPrefix()}).
 * <p>
 * {@code familyId} groups every token descended from the same original issuance (i.e. every
 * token minted by successive rotations of one login). {@code usedAt} is set the moment a token
 * is redeemed via rotation — a later attempt to redeem the same token again is refresh-token
 * reuse, a signal the token was stolen, and revokes the whole family (see
 * {@code RefreshTokenService.rotate}).
 */
public record RefreshTokenRecord(
        String id,
        String userId,
        String familyId,
        String tokenPrefix,
        String tokenHash,
        Instant issuedAt,
        Instant expiresAt,
        Instant usedAt,
        Instant revokedAt)
        implements Identifiable {}
