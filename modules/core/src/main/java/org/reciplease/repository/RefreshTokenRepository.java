package org.reciplease.repository;

import java.time.Instant;
import java.util.Optional;
import org.reciplease.model.RefreshTokenRecord;

public interface RefreshTokenRepository {

    /** Persists a brand-new refresh token. */
    RefreshTokenRecord save(RefreshTokenRecord record);

    Optional<RefreshTokenRecord> findByPrefix(String tokenPrefix);

    void markUsed(String id, Instant usedAt);

    void revokeFamily(String familyId, Instant revokedAt);

    void revokeAllForUser(String userId, Instant revokedAt);
}
